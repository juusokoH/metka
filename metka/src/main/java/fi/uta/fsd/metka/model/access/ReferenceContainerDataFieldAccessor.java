package fi.uta.fsd.metka.model.access;

import fi.uta.fsd.metka.model.access.enums.ConfigCheck;
import fi.uta.fsd.metka.model.access.enums.StatusCode;
import fi.uta.fsd.metka.model.configuration.Configuration;
import fi.uta.fsd.metka.model.data.container.DataField;
import fi.uta.fsd.metka.model.data.container.ReferenceContainerDataField;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.StringUtils;

import java.util.Map;

import static fi.uta.fsd.metka.model.access.DataFieldAccessor.getDataField;
import static fi.uta.fsd.metka.model.access.DataFieldOperationChecks.FieldCheck;

final class ReferenceContainerDataFieldAccessor {
    // Disable instantiation
    private ReferenceContainerDataFieldAccessor() {}

    /**
     * Returns requested field as ReferenceContainerDataField.
     * See getSavedDataField for more details
     *
     * @param fieldMap Map of DataFields
     * @param key Field key
     * @param config Configuration
     * @return See getSavedDataField for more details
     */
    static Pair<StatusCode, ReferenceContainerDataField> getReferenceContainerDataField(Map<String, DataField> fieldMap, String key, Configuration config,
                                                                      ConfigCheck[] configChecks) {
        if(fieldMap == null || StringUtils.isEmpty(key)) {
            return new ImmutablePair<>(StatusCode.INCORRECT_PARAMETERS, null);
        }

        Pair<StatusCode, DataField> pair = getDataField(
                fieldMap,
                key,
                config,
                ArrayUtils.add(configChecks, ConfigCheck.TYPE_CONTAINER),
                new FieldCheck[]{FieldCheck.REFERENCE_CONTAINER_DATA_FIELD}
        );
        return new ImmutablePair<>(pair.getLeft(), (ReferenceContainerDataField)pair.getRight());
    }
}