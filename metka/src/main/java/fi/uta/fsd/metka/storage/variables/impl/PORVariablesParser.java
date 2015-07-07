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

package fi.uta.fsd.metka.storage.variables.impl;

import fi.uta.fsd.Logger;
import fi.uta.fsd.metka.enums.*;
import fi.uta.fsd.metka.model.access.calls.*;
import fi.uta.fsd.metka.model.access.enums.StatusCode;
import fi.uta.fsd.metka.model.data.RevisionData;
import fi.uta.fsd.metka.model.data.change.Change;
import fi.uta.fsd.metka.model.data.container.*;
import fi.uta.fsd.metka.model.data.value.Value;
import fi.uta.fsd.metka.model.general.DateTimeUserPair;
import fi.uta.fsd.metka.model.transfer.TransferData;
import fi.uta.fsd.metka.names.Fields;
import fi.uta.fsd.metka.storage.repository.*;
import fi.uta.fsd.metka.storage.repository.enums.RemoveResult;
import fi.uta.fsd.metka.storage.repository.enums.ReturnResult;
import fi.uta.fsd.metka.storage.response.OperationResponse;
import fi.uta.fsd.metka.storage.response.RevisionableInfo;
import fi.uta.fsd.metka.storage.util.ChangeUtil;
import fi.uta.fsd.metka.storage.variables.enums.ParseResult;
import fi.uta.fsd.metka.transfer.revision.RevisionCreateRequest;
import fi.uta.fsd.metkaAuthentication.AuthenticationUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import fi.uta.fsd.metka.spssio.por.PORFile;
import fi.uta.fsd.metka.spssio.por.input.PORReader;

import java.io.IOException;
import java.util.*;

import static fi.uta.fsd.metka.storage.variables.impl.StudyVariablesParserImpl.checkResultForUpdate;
import static fi.uta.fsd.metka.storage.variables.impl.StudyVariablesParserImpl.resultCheck;

class PORVariablesParser implements VariablesParser {
    private final RevisionData variablesData;
    private final DateTimeUserPair info;
    private final String studyId;
    private final RevisionRemoveRepository remove;
    private final RevisionRepository revisions;
    private final RevisionCreationRepository create;
    private final RevisionEditRepository edit;

    private final String softwareName;
    private final String softwareVersion;
    private final int sizeX;
    private final int sizeY;
    private final List<PORUtil.PORVariableHolder> variables;
    private final Language varLang;
    private final List<RevisionData> variableRevisions;

    PORVariablesParser(String path, Language varLang, RevisionData variablesData, DateTimeUserPair info, String studyId,
                       RevisionRepository revisions, RevisionRemoveRepository remove, RevisionCreationRepository create, RevisionEditRepository edit) {
        this.varLang = varLang;
        this.remove = remove;
        this.revisions = revisions;
        this.create = create;
        this.edit = edit;
        this.variablesData = variablesData;
        this.info = info;
        this.studyId = studyId;

        PORReader reader = new PORReader();
        PORFile por;
        try {
            por = reader.parse(path);
        } catch(IOException ioe) {
            ioe.printStackTrace();
            Logger.error(getClass(), "IOException while reading POR-file with path " + path);
            throw new UnsupportedOperationException("Could not parse POR file");
        }

        this.variableRevisions = revisions.getVariableRevisionsOfVariables(variablesData.getKey().getId());

        // Group variables to list
        variables = PORUtil.groupVariables(por);

        // Group answers under variables list
        PORUtil.PORAnswerMapper visitor = new PORUtil.PORAnswerMapper(variables);
        por.data.accept(visitor);

        String[] software = por.getSoftware().split("\\s");
        softwareVersion = software.length > 1 ? software[software.length-1] : "";
        if(software.length == 1) {
            softwareName = software[0];
        } else if(software.length > 1) {
            String temp = "";
            for(int i = 0; i < software.length - 1; i++) {
                if(i > 0) {
                    temp += " ";
                }
                temp += software[i];
            }
            softwareName = temp;
        } else {
            softwareName = "";
        }

        sizeX = por.data.sizeX();
        sizeY = por.data.sizeY();
    }

    public ParseResult parse() {
        ParseResult result = ParseResult.NO_CHANGES;
        result = variablesBaseProperties(result);
        result = variablesParsing(result);
        return result;
    }

    // TODO: Can values be set to default irregardles of actual language?
    private ParseResult variablesBaseProperties(ParseResult result) {
        // Set software field
        Pair<StatusCode, ValueDataField> fieldPair = variablesData.dataField(
                ValueDataFieldCall.set(Fields.SOFTWARE, new Value(softwareName), Language.DEFAULT).setInfo(info));
        result = checkResultForUpdate(fieldPair, result);

        // Set softwareversion
        fieldPair = variablesData.dataField(
                ValueDataFieldCall.set(Fields.SOFTWAREVERSION, new Value(softwareVersion), Language.DEFAULT).setInfo(info));
        result = checkResultForUpdate(fieldPair, result);

        // Set varquantity field
        fieldPair = variablesData.dataField(ValueDataFieldCall.set(Fields.VARQUANTITY, new Value(sizeX + ""), Language.DEFAULT).setInfo(info));
        result = checkResultForUpdate(fieldPair, result);

        // Set casequantity field
        fieldPair = variablesData.dataField(ValueDataFieldCall.set(Fields.CASEQUANTITY, new Value(sizeY + ""), Language.DEFAULT).setInfo(info));
        result = checkResultForUpdate(fieldPair, result);

        return result;
    }

    // All languages are handled in exactly the same way
    private ParseResult variablesParsing(ParseResult result) {
        VariableParser parser = new VariableParser(info, varLang);

        Logger.debug(getClass(), "Gathering entities for parsing");
        long start = System.currentTimeMillis(); // Debug timer
        List<Pair<RevisionData, PORUtil.PORVariableHolder>> listOfEntitiesAndHolders = new ArrayList<>();
        for(PORUtil.PORVariableHolder variable : variables) {
            RevisionData variableRevision = null;
            for(Iterator<RevisionData> i = variableRevisions.iterator(); i.hasNext(); ) {
                variableRevision = i.next();
                Pair<StatusCode, ValueDataField> fieldPair = variableRevision.dataField(ValueDataFieldCall.get(Fields.VARID));
                if(fieldPair.getRight().getActualValueFor(Language.DEFAULT).equals(studyId + "_" + parser.getVarName(variable))) {
                    i.remove();
                    break;
                }
                variableRevision = null;
            }
            listOfEntitiesAndHolders.add(new ImmutablePair<>(variableRevision, variable));
        }
        Logger.debug(getClass(), "Entities gathered. Took "+(System.currentTimeMillis()-start)+"ms");

        // Variables container and variable groups in STUDY_VARIABLES
        ReferenceContainerDataField variablesContainer = variablesData.dataField(ReferenceContainerDataFieldCall.get(Fields.VARIABLES)).getRight();
        ContainerDataField variableGroups = variablesData.dataField(ContainerDataFieldCall.get(Fields.VARGROUPS)).getRight();

        // Perform removal
        for(RevisionData variableRevision : variableRevisions) {
            // All remaining rows in variableEntities should be removed since no variable was found for them in the current POR-file

            // If removal of the revision returns SUCCESS_DRAFT this means that there's more revisions to remove and second call with new latest revision should clear out any remaining revisions.
            if(RemoveResult.valueOf(remove.remove(TransferData.buildFromRevisionData(variableRevision, RevisionableInfo.FALSE), info).getResult()) == RemoveResult.SUCCESS_DRAFT) {
                Pair<ReturnResult, RevisionData> dataPair = revisions.getLatestRevisionForIdAndType(variableRevision.getKey().getId(), false, ConfigurationType.STUDY_VARIABLE);
                remove.remove(TransferData.buildFromRevisionData(dataPair.getRight(), RevisionableInfo.FALSE), info);
            }

            // Remove from variables container
            if(variablesContainer != null) {
                // See that respective rows are removed from STUDY_VARIABLES
                //    Remove from variables list
                ReferenceRow reference = variablesContainer.getReferenceWithValue(variableRevision.getKey().getId().toString()).getRight();
                if(reference != null) {
                    StatusCode status = variablesContainer.removeReference(reference.getRowId(), variablesData.getChanges(), info).getLeft();
                    if(status == StatusCode.ROW_CHANGE || status == StatusCode.ROW_REMOVED) {
                        result = resultCheck(result, ParseResult.REVISION_CHANGES);
                    }
                }
            }

            // Remove from groups
            if(variableGroups != null) {
                for(DataRow row : variableGroups.getRowsFor(Language.DEFAULT)) {
                    ReferenceContainerDataField varGroupVars = row.dataField(ReferenceContainerDataFieldCall.get(Fields.VARGROUPVARS)).getRight();
                    if(varGroupVars != null) {
                        // See that respective rows are removed from VARGROUPVARS
                        //    Remove from variables list
                        ReferenceRow reference = varGroupVars.getReferenceWithValue(variableRevision.getKey().getId().toString()).getRight();
                        if(reference != null) {
                            StatusCode status = varGroupVars.removeReference(reference.getRowId(), variablesData.getChanges(), info).getLeft();
                            if(status == StatusCode.ROW_CHANGE || status == StatusCode.ROW_REMOVED) {
                                result = resultCheck(result, ParseResult.REVISION_CHANGES);
                            }
                            // Since variable should always be only in one group at a time we can break out.
                            break;
                        }
                    }
                }
            }
        }

        // listOfEntitiesAndHolders should contain all variables in the POR-file as well as their existing revisionables. No revisionable is provided if it's a new variable
        if(listOfEntitiesAndHolders.size() > 0 && variablesContainer == null) {
            Pair<StatusCode, ReferenceContainerDataField> containerPair = variablesData.dataField(ReferenceContainerDataFieldCall.set(Fields.VARIABLES));
            result = checkResultForUpdate(containerPair, result);
            variablesContainer = containerPair.getRight();
        }

        if(variablesContainer == null) {
            Logger.error(getClass(), "Missing variables container even though it should be present or created");
            return resultCheck(result, ParseResult.NO_VARIABLES_CONTAINER);
        }

        Pair<StatusCode, ValueDataField> studyField = variablesData.dataField(ValueDataFieldCall.get(Fields.STUDY));
        Logger.debug(getClass(), listOfEntitiesAndHolders.size()+" variables to parse.");
        List<ReferenceRow> varRows = gatherAndClear(variablesContainer);

        // Debug variables
        int counter = 0;
        long timeSpent = 0L;
        for(Pair<RevisionData, PORUtil.PORVariableHolder> pair : listOfEntitiesAndHolders) {
            // Iterate through entity/holder pairs. There should always be a holder but missing entity indicates that this is a new variable.
            // After all variables are handled there should be one non removed revisionable per variable in the current por-file.
            // Each revisionable should have an open draft revision (this is a shortcut but it would require doing actual change checking for all variable content to guarantee that no
            // unnecessary revisions are created. This is not required and so a new draft is provided per revisionable).
            // Variables entity should have an open draft revision that includes references to all variables as well as non grouped references for all variables that previously were
            // not in any groups.

            start = System.currentTimeMillis(); // Debug timer

            RevisionData variableData = pair.getLeft();
            PORUtil.PORVariableHolder variable = pair.getRight();
            String varName = parser.getVarName(variable);
            String varId = studyId + "_" + parser.getVarName(variable);

            if(variableData == null) {
                RevisionCreateRequest request = new RevisionCreateRequest();
                request.setType(ConfigurationType.STUDY_VARIABLE);
                request.getParameters().put(Fields.STUDY, studyField.getRight().getActualValueFor(Language.DEFAULT));
                request.getParameters().put(Fields.VARIABLESID, variablesData.getKey().getId().toString());
                request.getParameters().put(Fields.VARNAME, varName);
                request.getParameters().put(Fields.VARID, varId);
                request.getParameters().put(Fields.LANGUAGE, varLang.toValue());
                Pair<ReturnResult, RevisionData> dataPair = create.create(request);
                if(dataPair.getLeft() != ReturnResult.REVISION_CREATED) {
                    Logger.error(getClass(), "Couldn't create new variable revisionable for study "+studyField.getRight().getActualValueFor(Language.DEFAULT)+" and variables "+variablesData.toString());
                    return resultCheck(result, ParseResult.COULD_NOT_CREATE_VARIABLES);
                }
                variableData = dataPair.getRight();
            }

            if(variableData.getState() != RevisionState.DRAFT) {
                Pair<OperationResponse, RevisionData> dataPair = edit.edit(variableData.getKey(), info);
                if(!dataPair.getLeft().getResult().equals(ReturnResult.REVISION_CREATED.name())) {
                    Logger.error(getClass(), "Couldn't create new DRAFT revision for "+variableData.getKey().toString());
                    return resultCheck(result, ParseResult.COULD_NOT_CREATE_VARIABLE_DRAFT);
                }
                variableData = dataPair.getRight();
            }

            if(!AuthenticationUtil.isHandler(variableData)) {
                variableData.setHandler(AuthenticationUtil.getUserName());
                revisions.updateRevisionData(variableData);
            }

            // Merge variable to variable revision
            ParseResult mergeResult = parser.mergeToData(variableData, variable);

            if(mergeResult == ParseResult.REVISION_CHANGES) {
                ReturnResult updateResult = revisions.updateRevisionData(variableData);
                if(updateResult != ReturnResult.REVISION_UPDATE_SUCCESSFUL) {
                    Logger.error(getClass(), "Could not update revision data for "+variableData.toString()+" with result "+updateResult);
                }
            }

            // Insert row back to variables container or create row if not there before
            popOrCreateAndInsertRowTo(variablesContainer, varRows, variableData.getKey().getId()+"", variablesData.getChanges(), info);

            counter++;
            long end = System.currentTimeMillis()-start;
            Logger.debug(getClass(), "Parsed variable in "+end+"ms. Still "+(listOfEntitiesAndHolders.size()-counter)+" variables to parse.");
            timeSpent += end;
        }
        Logger.debug(getClass(), "Parsed variables in "+timeSpent+"ms");

        return result;
    }

    private static List<ReferenceRow> gatherAndClear(ReferenceContainerDataField field) {
        if(field.getReferences() == null) {
            return new ArrayList<>();
        }
        List<ReferenceRow> rows = new ArrayList<>(field.getReferences());
        field.getReferences().clear();
        return rows;
    }

    /**
     * Helper method for handling and organising container rows.
     * Searches given collection for a row with given value in given field.
     * If row was not found then creates a new row and inserts it into provided container.
     * No change handling is necessary since some set operation should follow always after
     * calling this method.
     *
     * @param target Target container where the row will be set
     * @param rows Collection of rows to search through for correct existing row
     * @param value Value to search for
     * @param changeMap Map where this rows containers should reside
     * @return Either an existing or newly created ReferenceRow that has been inserted to the given container already
     */
    private ReferenceRow popOrCreateAndInsertRowTo(ReferenceContainerDataField target, Collection<ReferenceRow> rows, String value, Map<String, Change> changeMap, DateTimeUserPair info) {
        ReferenceRow row = popRowWithFieldValue(rows, value);
        if(row == null) {
            row = ReferenceRow.build(target, new Value(value), info);
            ChangeUtil.insertChange(changeMap, target, row);
        }

        target.getReferences().add(row);
        return row;
    }

    /**
     * Helper method for handling and organising container rows.
     * Takes a collection of rows, finds a row based on a field and removes it from the given
     * collection.
     * Assumption is that the collection is not the actual rows list of a ContainerDataField
     * but some other collection used for organising rows during operations.
     *
     * @param rows Collection of rows to search through
     * @param value Value to be searched for, should be non empty string
     * @return First ReferenceRow to match the given value, null if no row was found
     */
    private ReferenceRow popRowWithFieldValue(Collection<ReferenceRow> rows, String value) {
        for(Iterator<ReferenceRow> i = rows.iterator(); i.hasNext(); ) {
            ReferenceRow row = i.next();
            if(row.valueEquals(value)) {
                i.remove();
                return row;
            }
        }
        return null;
    }
}
