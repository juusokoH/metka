package fi.uta.fsd.metka.model.transfer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.uta.fsd.metka.enums.RevisionState;
import fi.uta.fsd.metka.model.data.RevisionData;
import fi.uta.fsd.metka.model.data.container.DataField;
import fi.uta.fsd.metka.model.general.ConfigurationKey;
import fi.uta.fsd.metka.model.general.RevisionKey;
import fi.uta.fsd.metka.model.interfaces.ModelBase;
import fi.uta.fsd.metka.model.interfaces.TransferFieldContainer;
import fi.uta.fsd.metka.storage.response.RevisionableInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "transferData")
public class TransferData implements ModelBase, TransferFieldContainer {
    @XmlElement private final RevisionKey key;
    @XmlElement private final ConfigurationKey configuration;
    @XmlElement private final Map<String, TransferField> fields = new HashMap<>();
    @XmlElement private final TransferState state = new TransferState();

    @JsonCreator
    public TransferData(@JsonProperty("key")RevisionKey key, @JsonProperty("configuration")ConfigurationKey configuration) {
        this.key = key;
        this.configuration = configuration;
    }

    public RevisionKey getKey() {
        return key;
    }

    public ConfigurationKey getConfiguration() {
        return configuration;
    }

    public Map<String, TransferField> getFields() {
        return fields;
    }

    public TransferState getState() {
        return state;
    }

    @JsonIgnore
    public boolean hasField(String key) {
        return fields.containsKey(key) && fields.get(key) != null;
    }

    @JsonIgnore
    public TransferField getField(String key) {
        return fields.get(key);
    }

    public static TransferData buildFromRevisionData(RevisionData revision, RevisionableInfo info) {
        // Create new TransferData copying the revision data's revision key and configuration key
        TransferData data = new TransferData(revision.getKey().copy(), revision.getConfiguration().copy());

        // Set TransferData state. TransferState has certain initial values so only situations that differ from that state have to be checked.
        if(info.getRemoved()) {
            data.state.setRemoved(true);
            data.state.setRemovalDate(info.getRemovedAt());
            data.state.setRemovedBy(info.getRemovedBy());
        }
        if(revision.getState() == RevisionState.APPROVED) {
            data.state.setApproved(true);
            data.state.setApprovalDate(revision.getApprovalDate());
            data.state.setApprovedBy(revision.getApprovedBy());
        }
        if(revision.getState() == RevisionState.DRAFT) {
            data.state.setDraft(true);
            data.state.setHandler(revision.getHandler());
        }
        if(revision.getLastSaved() != null) {
            data.state.setSaved(true);
            data.state.setSavedDate(revision.getLastSaved());
            data.state.setSavedBy(revision.getLastSavedBy());
        }

        for(DataField field : revision.getFields().values()) {
            TransferField transferField = TransferField.buildFromDataField(field);
            if(transferField != null) {
                data.fields.put(transferField.getKey(), transferField);
            }
        }

        return data;
    }
}
