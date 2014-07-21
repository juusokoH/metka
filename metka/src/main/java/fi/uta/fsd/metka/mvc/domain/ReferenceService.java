package fi.uta.fsd.metka.mvc.domain;

import fi.uta.fsd.metka.data.collecting.ReferenceCollecting;
import fi.uta.fsd.metka.data.enums.FieldType;
import fi.uta.fsd.metka.data.enums.SelectionListType;
import fi.uta.fsd.metka.data.repository.ConfigurationRepository;
import fi.uta.fsd.metka.model.access.calls.SavedDataFieldCall;
import fi.uta.fsd.metka.model.access.enums.StatusCode;
import fi.uta.fsd.metka.model.configuration.Configuration;
import fi.uta.fsd.metka.model.configuration.Field;
import fi.uta.fsd.metka.model.configuration.Reference;
import fi.uta.fsd.metka.model.configuration.SelectionList;
import fi.uta.fsd.metka.model.data.RevisionData;
import fi.uta.fsd.metka.model.data.container.SavedDataField;
import fi.uta.fsd.metka.transfer.reference.ReferenceOptionsRequest;
import fi.uta.fsd.metka.transfer.reference.ReferenceOption;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * Contains communication pertaining to Reference objects
 */
@Service
public class ReferenceService {

    @Autowired
    private ReferenceCollecting references;

    @Autowired
    private ConfigurationRepository configurations;

    /**
     * Returns the current option depicting the value and title of given reference.
     * This is mainly used for indexing to get the actual text that needs to be indexed.
     * @param data
     * @param path
     * @return
     * @throws IOException
     */
    public ReferenceOption getCurrentFieldOption(RevisionData data, String path) throws IOException {
        Configuration config = configurations.findConfiguration(data.getConfiguration());
        String[] splits = path.split(".");
        if(splits.length == 0) {
            splits = new String[1];
            splits[0] = path;
        }

        // Check that the final path element points to a field that can be a reference with a value, deal with referencecontainers separately with a different call
        Field field = config.getField(splits[splits.length-1]);
        if(field == null) {
            return null;
        }
        if(!(field.getType() == FieldType.REFERENCE || field.getType() == FieldType.SELECTION)) {
            return null;
        }
        if(field.getType() == FieldType.SELECTION) {
            SelectionList list = config.getRootSelectionList(field.getSelectionList());
            if(list.getType() != SelectionListType.REFERENCE) {
                return null;
            }
        }

        // TODO: If the RevisionData is already approved then the text should be contained in the data itself once approval lock down is completed. Check for that value

        // We are certain that the field is a field with a reference and can contain a value, next check that if the field currently has a value
        // If the field doesn't have a value then it's unnecessary to check for option since there's no selected option in any case
        Pair<StatusCode, SavedDataField> pair = null;
        if(splits.length == 1) {
            // We already have the correct field, it's a top level field and it's either a reference of a selection
            // Perform the saved data field call
            if(field.getSubfield()) {
                return null;
            }
            pair = data.dataField(SavedDataFieldCall.get(field.getKey()).setConfiguration(config));
        } else {
            // Field should be in a container (since we're not dealing with reference containers here)
            if(!field.getSubfield()) {
                return null;
            }
            for(int i = 0; i < splits.length; i++) {
                String key = splits[i];

            }
        }
        if(pair == null) {
            return null;
        }
        if(pair.getLeft() != StatusCode.FIELD_FOUND) {
            // Since no value was found we have nothing to index
            return null;
        }
        SavedDataField saved = pair.getRight();
        if(StringUtils.isEmpty(saved.getActualValue())) {
            // No value, don't care
            return null;
        }
        // Get the value from the saved data field, this is used to identify the correct option from returned options
        String value = saved.getActualValue();
        // Check if the reference is a dependency time and if so then collect the dependency value for request
        String dependency = null;
        // TODO: get dependency value

        // We have value, we can search for options and select one.
        ReferenceOptionsRequest request = new ReferenceOptionsRequest();
        request.setKey(saved.getKey());
        request.setConfType(config.getKey().getType().toValue());
        request.setConfVersion(config.getKey().getVersion());
        request.setDependencyValue(dependency);
        List<ReferenceOption> options = collectReferenceOptions(request);
        for(ReferenceOption option : options) {
            if(option.getValue().equals(value)) {
                return option;
            }
        }
        // Didn't find value, so much for that
        return null;
    }

    public List<ReferenceOption> collectReferenceOptions(ReferenceOptionsRequest request) throws IOException {

        Configuration config = configurations.findConfiguration(request.getConfType(), request.getConfVersion());
        if(config == null) {
            return null;
        }
        Field field = config.getField(request.getKey());
        // Add types as needed, default is to return null if type can not contain a reference
        Reference reference = null;
        switch(field.getType()) {
            case REFERENCE:
            case REFERENCECONTAINER:
                reference = config.getReference(field.getReference());
                break;
            case SELECTION:
                SelectionList list = config.getRootSelectionList(field.getSelectionList());
                if(list == null || list.getType() != SelectionListType.REFERENCE) {
                    return null;
                }
                reference = config.getReference(list.getReference());
                break;
            default:
                break;
        }

        return references.referenceOptionCollecting(reference, field, config, request);

    }
}
