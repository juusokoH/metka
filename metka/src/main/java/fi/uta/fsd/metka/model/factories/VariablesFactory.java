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

package fi.uta.fsd.metka.model.factories;

import fi.uta.fsd.Logger;
import fi.uta.fsd.metka.enums.ConfigurationType;
import fi.uta.fsd.metka.enums.Language;
import fi.uta.fsd.metka.model.access.calls.ContainerDataFieldCall;
import fi.uta.fsd.metka.model.access.calls.ValueDataFieldCall;
import fi.uta.fsd.metka.model.access.enums.StatusCode;
import fi.uta.fsd.metka.model.configuration.Configuration;
import fi.uta.fsd.metka.model.data.RevisionData;
import fi.uta.fsd.metka.model.data.container.ContainerDataField;
import fi.uta.fsd.metka.model.data.container.DataRow;
import fi.uta.fsd.metka.model.data.container.ValueDataField;
import fi.uta.fsd.metka.model.data.value.Value;
import fi.uta.fsd.metka.model.general.DateTimeUserPair;
import fi.uta.fsd.metka.model.interfaces.DataFieldContainer;
import fi.uta.fsd.metka.names.Fields;
import fi.uta.fsd.metka.storage.repository.enums.ReturnResult;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.LocalDateTime;

/**
 * Factory related to study variables.
 * Provides initial data methods for study variables and a study variable
 */
public class VariablesFactory extends DataFactory {

    public Pair<ReturnResult, RevisionData> newStudyVariables(Long id, Integer no, Configuration configuration, String studyId, String fileId, String varfileid,
            String varfiletype, String language) {
        if(configuration.getKey().getType() != ConfigurationType.STUDY_VARIABLES) {
            Logger.error(getClass(), "Called StudyVariablesFactory with type " + configuration.getKey().getType() + " configuration");
            return new ImmutablePair<>(ReturnResult.INCORRECT_TYPE_FOR_OPERATION, null);
        }

        DateTimeUserPair info = DateTimeUserPair.build(new LocalDateTime());

        RevisionData data = createDraftRevision(id, no, configuration.getKey());
        data.dataField(ValueDataFieldCall.set(Fields.STUDY, new Value(studyId), Language.DEFAULT).setInfo(info));
        data.dataField(ValueDataFieldCall.set(Fields.FILE, new Value(fileId), Language.DEFAULT).setInfo(info));
        data.dataField(ValueDataFieldCall.set(Fields.LANGUAGE, new Value(language), Language.DEFAULT).setInfo(info));
        data.dataField(ValueDataFieldCall.set(Fields.VARFILEID, new Value(varfileid), Language.DEFAULT).setInfo(info));
        data.dataField(ValueDataFieldCall.set(Fields.VARFILETYPE, new Value(varfiletype), Language.DEFAULT).setInfo(info));
        data.dataField(ValueDataFieldCall.set(Fields.VARFILENO, new Value("F1"), Language.DEFAULT).setInfo(info));

        return new ImmutablePair<>(ReturnResult.REVISION_CREATED, data);
    }

    public Pair<ReturnResult, RevisionData> newVariable(Long id, Integer no, Configuration configuration, String variablesId, String studyId, String varName, String varId, String language) {
        if(configuration.getKey().getType() != ConfigurationType.STUDY_VARIABLE) {
            Logger.error(getClass(), "Called StudyVariablesFactory with type "+configuration.getKey().getType()+" configuration");
            return new ImmutablePair<>(ReturnResult.INCORRECT_TYPE_FOR_OPERATION, null);
        }

        DateTimeUserPair info = DateTimeUserPair.build(new LocalDateTime());

        RevisionData data = createDraftRevision(id, no, configuration.getKey());
        data.dataField(ValueDataFieldCall.set(Fields.VARIABLES, new Value(variablesId), Language.DEFAULT).setInfo(info));
        data.dataField(ValueDataFieldCall.set(Fields.LANGUAGE, new Value(language), Language.DEFAULT).setInfo(info));
        data.dataField(ValueDataFieldCall.set(Fields.STUDY, new Value(studyId), Language.DEFAULT).setInfo(info));
        // Set varid field
        data.dataField(ValueDataFieldCall.set(Fields.VARNAME, new Value(varName), Language.DEFAULT).setInfo(info));
        data.dataField(ValueDataFieldCall.set(Fields.VARID, new Value(varId), Language.DEFAULT).setInfo(info));

        return new ImmutablePair<>(ReturnResult.REVISION_CREATED, data);
    }

    private boolean doesContainerHaveTranslation(DataFieldContainer revision, String key, Language l) {
            Pair<StatusCode, ContainerDataField> container = revision.dataField(ContainerDataFieldCall.get(key));
            if(container.getLeft() == StatusCode.FIELD_FOUND && container.getRight().hasRowsFor(l)) {
                for(DataRow row : container.getRight().getRowsFor(l)) {
                    if(!row.getRemoved()) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean doesValueHaveTranslation(DataFieldContainer revision, String key, Language l) {
            Pair<StatusCode, ValueDataField> value = revision.dataField(ValueDataFieldCall.get(key));
            return value.getLeft() == StatusCode.FIELD_FOUND && value.getRight().hasValueFor(l);
        }
}
