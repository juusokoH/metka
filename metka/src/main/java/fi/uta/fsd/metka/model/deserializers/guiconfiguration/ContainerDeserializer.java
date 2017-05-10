/**************************************************************************************
 * Copyright (c) 2013-2015, Finnish Social Science Data Archive/University of Tampere *
 *                                                                                    *
 * All rights reserved.                                                               *
 *                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification,   *
 * are permitted provided that the following conditions are met:                      *
 * 1. Redistributions of source code must retain the above copyright notice, this     *
 *    list of conditions and the following disclaimer.                                *
 * 2. Redistributions in binary form must reproduce the above copyright notice,       *
 *    this list of conditions and the following disclaimer in the documentation       *
 *    and/or other materials provided with the distribution.                          *
 * 3. Neither the name of the copyright holder nor the names of its contributors      *
 *    may be used to endorse or promote products derived from this software           *
 *    without specific prior written permission.                                      *
 *                                                                                    *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND    *
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED      *
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE             *
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR   *
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES     *
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;       *
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON     *
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT            *
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                       *
 **************************************************************************************/

package fi.uta.fsd.metka.model.deserializers.guiconfiguration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import fi.uta.fsd.metka.enums.ContainerType;
import fi.uta.fsd.metka.enums.ContentType;
import fi.uta.fsd.metka.enums.SectionState;
import fi.uta.fsd.metka.model.deserializers.ObjectDeserializer;
import fi.uta.fsd.metka.model.general.TranslationObject;
import fi.uta.fsd.metka.model.guiconfiguration.Button;
import fi.uta.fsd.metka.model.guiconfiguration.Container;
import fi.uta.fsd.metka.model.guiconfiguration.FieldDescription;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ContainerDeserializer extends ObjectDeserializer<Container> {

    @Override
    public Container doDeserialize(ObjectCodec oc, JsonNode node, JsonParser jp, DeserializationContext ctxt) throws IOException {
        Container con = new Container();

        // set id
        JsonNode id = node.get("id");
        con.setId((id != null && StringUtils.hasText(id.textValue()) ? id.textValue() : null));

        // set type
        JsonNode type = node.get("type");
        con.setType(ContainerType.valueOf(type.textValue()));

        // Set dialogTitle
        JsonNode title = node.get("title");
        if(title == null) {
            title = node.get("&title");
        }

        TranslationObject loc = null;
        if(title != null) {
            loc = oc.treeToValue(title, TranslationObject.class);
        }
        con.setTitle(loc);

        // Set hidden
        JsonNode hidden = node.get("hidden");
        if(hidden != null && hidden.getNodeType() == JsonNodeType.BOOLEAN) {
            con.setHidden(hidden.booleanValue());
        }

        // Set horizontal
        JsonNode horizontal = node.get("horizontal");
        if(horizontal != null && horizontal.getNodeType() == JsonNodeType.BOOLEAN) {
            con.setHorizontal(horizontal.booleanValue());
        }

        // Set readOnly
        JsonNode readOnly = node.get("readOnly");
        if(readOnly != null && readOnly.getNodeType() == JsonNodeType.BOOLEAN) {
            con.setReadOnly(readOnly.booleanValue());
        }

        // Set permissions
        JsonNode permissions = node.get("permissions");
        if(permissions != null && permissions.getNodeType() == JsonNodeType.ARRAY) {
            Set<String> perms = new HashSet<>();
            for (JsonNode permission: permissions){
                perms.add(permission.textValue());
            }
            con.setPermissions(perms);
        }

        // set important
        if(con.getType() == ContainerType.SECTION || con.getType() == ContainerType.CELL) {
            JsonNode important = node.get("important");
            if(important != null && important.getNodeType() == JsonNodeType.BOOLEAN) {
                con.setImportant(important.booleanValue());
            }
        }

        // set defaultState
        if(con.getType() == ContainerType.SECTION) {
            JsonNode defaultState = node.get("defaultState");
            if(defaultState != null && defaultState.getNodeType() == JsonNodeType.STRING) {
                con.setDefaultState(SectionState.valueOf(defaultState.textValue()));
            }
        }

        // set columns
        if(con.getType() == ContainerType.COLUMN) {
            JsonNode columns = node.get("columns");
            if(columns != null && columns.getNodeType() == JsonNodeType.NUMBER) {
                con.setColumns(columns.intValue());
            }
        }

        // Set hidePageButtons
        if(con.getType() == ContainerType.TAB) {
            JsonNode hideButtons = node.get("hidePageButtons");
            if(hideButtons != null && hideButtons.getNodeType() == JsonNodeType.BOOLEAN) {
                con.setHidePageButtons(hideButtons.booleanValue());
            }
        }

        // set content
        if(con.getType() == ContainerType.TAB || con.getType() == ContainerType.SECTION) {
            JsonNode content = node.get("content");
            if(content != null && content.getNodeType() == JsonNodeType.ARRAY) {
                for(JsonNode container : content) {
                    Container c = oc.treeToValue(container, Container.class);
                    if(con.getType().canContain(c.getType())) {
                        con.getContent().add(c);
                    }
                }
            }

        }

        // set rows
        if(con.getType() == ContainerType.COLUMN) {
            JsonNode rows = node.get("rows");
            if(rows != null && rows.getNodeType() == JsonNodeType.ARRAY) {
                for(JsonNode row : rows) {
                    Container c = oc.treeToValue(row, Container.class);
                    if(con.getType().canContain(c.getType())) {
                        con.getRows().add(c);
                    }
                }
            }
        }
        // set cells
        if(con.getType() == ContainerType.ROW) {
            JsonNode cells = node.get("cells");
            if(cells != null && cells.getNodeType() == JsonNodeType.ARRAY) {
                for(JsonNode cell : cells) {
                    Container c = oc.treeToValue(cell, Container.class);
                    if(con.getType().canContain(c.getType())) {
                        con.getCells().add(c);
                    }
                }
            }
        }

        if(con.getType() == ContainerType.CELL) {
            // set required
            JsonNode required = node.get("required");
            if(required != null && required.getNodeType() == JsonNodeType.BOOLEAN) {
                con.setRequired(required.booleanValue());
            }

            // set colspan
            JsonNode colspan = node.get("colspan");
            if(colspan != null && colspan.getNodeType() == JsonNodeType.NUMBER) {
                con.setColspan(colspan.intValue());
            }

            JsonNode contentType = node.get("contentType");
            if(contentType != null && contentType.getNodeType() == JsonNodeType.STRING) {
                con.setContentType(ContentType.valueOf(contentType.textValue()));
            }

            if(con.getContentType() == ContentType.FIELD) {
                // set field
                JsonNode field = node.get("field");
                if(field != null && field.getNodeType() == JsonNodeType.OBJECT) {
                    con.setField(oc.treeToValue(field, FieldDescription.class));
                }
            } else if(con.getContentType() == ContentType.BUTTON) {
                JsonNode button = node.get("button");
                if(button != null && button.getNodeType() == JsonNodeType.OBJECT) {
                    con.setButton(oc.treeToValue(button, Button.class));
                }
            }



            // Set extra dialog config
            JsonNode dialog = node.get("subfieldConfiguration");
            if(dialog != null && dialog.getNodeType() == JsonNodeType.OBJECT) {
                for(Iterator<String> i = dialog.fieldNames(); i.hasNext(); ){
                    String key = i.next();
                    JsonNode field = dialog.get(key);
                    if(field != null && field.getNodeType() == JsonNodeType.OBJECT) {
                        ObjectNode o = (ObjectNode)field;
                        o.set("type", new TextNode("CELL"));
                        Container c = oc.treeToValue(o, Container.class);
                        con.getSubfieldConfiguration().put(key, c);
                    }
                }
            }
        }

        return con;
    }
}
