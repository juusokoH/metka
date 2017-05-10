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

package fi.uta.fsd.metka.model.guiconfiguration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fi.uta.fsd.metka.enums.ContainerType;
import fi.uta.fsd.metka.enums.ContentType;
import fi.uta.fsd.metka.enums.SectionState;
import fi.uta.fsd.metka.model.general.TranslationObject;

import java.util.*;
/**
 * Specification and documentation is found from uml/gui_config/uml_json_gui_configuration_container.graphml
 */
@JsonIgnoreProperties("_comment")
public class Container {
    private static int MAX_COLUMNS = 4;
    private String id;
    private TranslationObject title;
    private ContainerType type;
    private Boolean hidden = false;
    private Boolean readOnly = null;
    private Boolean important = false;
    private SectionState defaultState = SectionState.CLOSE;
    private Integer columns = 1;
    private Boolean required = false;
    private Integer colspan = 1;
    private Boolean horizontal = false;
    private final Map<String, Container> subfieldConfiguration = new HashMap<>();
    private Boolean hidePageButtons = false;

    private Set<String> permissions = new HashSet<>();

    private ContentType contentType = ContentType.FIELD;
    private FieldDescription field = null;
    private Button button = null;

    private final List<Container> content = new ArrayList<>();
    private final List<Container> rows = new ArrayList<>();
    private final List<Container> cells = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ContainerType getType() {
        return type;
    }

    public void setType(ContainerType type) {
        this.type = type;
    }

    public TranslationObject getTitle() {
        return title;
    }

    public void setTitle(TranslationObject title) {
        this.title = title;
    }

    public Boolean getHidden() {
        return (hidden == null) ? false : hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = (hidden == null) ? false : hidden;
    }

    public Boolean getHorizontal() {
        return (horizontal == null) ? false : horizontal;
    }

    public void setHorizontal(Boolean horizontal) {
        this.horizontal = (horizontal == null) ? false : horizontal;
    }

    public Boolean getReadOnly() {
        return (readOnly != null && !readOnly) ? null : readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = (readOnly != null && !readOnly) ? null : readOnly;
    }

    public Boolean getImportant() {
        return (important == null) ? false : important;
    }

    public void setImportant(Boolean important) {
        this.important = (important == null) ? false : important;
    }

    public SectionState getDefaultState() {
        return (defaultState == null) ? SectionState.CLOSE : defaultState;
    }

    public void setDefaultState(SectionState defaultState) {
        this.defaultState = (defaultState == null) ? SectionState.CLOSE : defaultState;
    }

    public Integer getColumns() {
        return columns;
    }

    public void setColumns(Integer columns) {
        this.columns = columns > MAX_COLUMNS
                ? MAX_COLUMNS
                : (columns < 1 ? 1 : columns);
    }

    public Boolean getRequired() {
        return (required == null) ? false : required;
    }

    public void setRequired(Boolean required) {
        this.required = (required == null) ? false : required;
    }

    public Integer getColspan() {
        return colspan;
    }

    public void setColspan(Integer colspan) {
        this.colspan = colspan > MAX_COLUMNS
                ? MAX_COLUMNS
                : (colspan < 1 ? 1 : colspan);
    }

    public FieldDescription getField() {
        return field;
    }

    public void setField(FieldDescription field) {
        this.field = field;
    }

    public List<Container> getContent() {
        return content;
    }

    public List<Container> getRows() {
        return rows;
    }

    public List<Container> getCells() {
        return cells;
    }

    public Map<String, Container> getSubfieldConfiguration() {
        return subfieldConfiguration;
    }

    public Boolean getHidePageButtons() {
        return hidePageButtons == null ? false : hidePageButtons;
    }

    public void setHidePageButtons(Boolean hidePageButtons) {
        this.hidePageButtons = hidePageButtons == null ? false : hidePageButtons;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions == null ? new HashSet<String>() : permissions;
    }

    public ContentType getContentType() {
        return contentType == null ? ContentType.FIELD : contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType == null ? ContentType.FIELD : contentType;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }
}
