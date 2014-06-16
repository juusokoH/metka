package fi.uta.fsd.metka.model.data.container;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.uta.fsd.metka.data.util.ModelAccessUtil;
import fi.uta.fsd.metka.model.access.DataFieldOperator;
import fi.uta.fsd.metka.model.access.calls.DataFieldCall;
import fi.uta.fsd.metka.model.access.calls.DataFieldCallBase;
import fi.uta.fsd.metka.model.access.enums.ConfigCheck;
import fi.uta.fsd.metka.model.access.enums.StatusCode;
import fi.uta.fsd.metka.model.data.RevisionData;
import fi.uta.fsd.metka.model.data.change.Change;
import fi.uta.fsd.metka.model.data.change.ContainerChange;
import fi.uta.fsd.metka.model.data.change.RowChange;
import fi.uta.fsd.metka.model.interfaces.DataFieldContainer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Single row of fields in a container is saved through this
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DataRow implements ModelAccessUtil.PathNavigable, DataFieldContainer {
    @XmlElement private final String key;
    @XmlElement private final Integer rowId;
    @XmlElement private final Map<String, DataField> fields = new HashMap<>();
    @XmlElement private Boolean removed = false;
    @XmlElement private LocalDateTime savedAt;
    @XmlElement private String savedBy;

    @JsonCreator
    public DataRow(@JsonProperty("key") String key, @JsonProperty("rowId") Integer rowId) {
        this.key = key;
        this.rowId = rowId;
    }

    public String getKey() {
        return key;
    }

    public Integer getRowId() {
        return rowId;
    }

    public Boolean isRemoved() {
        return (removed == null ? false : removed);
    }

    public void setRemoved(Boolean removed) {
        this.removed = (removed == null ? false : removed);
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }

    public String getSavedBy() {
        return savedBy;
    }

    public void setSavedBy(String savedBy) {
        this.savedBy = savedBy;
    }

    public Map<String, DataField> getFields() {
        return fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DataRow that = (DataRow) o;

        if (!getKey().equals(that.getKey())) return false;
        if (!rowId.equals(that.rowId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getKey().hashCode();
        result = 31 * result + rowId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Json[name="+this.getClass().getSimpleName()+", key="+getKey()+", rowId="+rowId+"]";
    }

    // Helper methods
    @JsonIgnore
    public DataField getField(String key) {
        return fields.get(key);
    }

    @JsonIgnore
    public void putField(DataField field) {
        fields.put(field.getKey(), field);
    }

    public static DataRow build(ContainerDataField container, RevisionData revision) {
        return new DataRow(container.getKey(), revision.getNewRowId());
    }

    public DataRow copy() {
        DataRow row = new DataRow(getKey(), rowId);
        row.setSavedAt(new LocalDateTime(savedAt));
        row.setSavedBy(savedBy);
        for(DataField field : fields.values()) {
            row.putField(field.copy());
        }
        return row;
    }

    public void normalize() {
        for(DataField field : fields.values()) {
            field.normalize();
        }
    }

    /**
     * Performs DataField related DataFieldCall operations on this row.
     * It is assumed that the changeMap in the call object is the map from where this row's container's change should be found
     * since we don't want to check beforehand if there are changes or not.
     * This method will create the change objects if they are missing and add them to the map if necessary.
     * @param call DataFieldCall object defining what operation to process
     * @return Pair object where left side is a status code describing the result and right side is the resulting DataField object typed by DataFieldCall
     */
    @Override
    public <T extends DataField> Pair<StatusCode, T> dataField(DataFieldCall<T> call) {
        switch(call.getCallType()) {
            case GET:
                return DataFieldOperator.getDataFieldOperation(getFields(), call, new ConfigCheck[]{ConfigCheck.IS_SUBFIELD});
            case SET:
                if(call.getChangeMap() == null) {
                    // We don't need to continue since this is the result anyway
                    return new ImmutablePair<>(StatusCode.INCORRECT_PARAMETERS, null);
                }
                // Get original change map from the call object and get ContainerChange and RowChange or create them if not present
                Map<String, Change> original = call.getChangeMap();
                ContainerChange container = (ContainerChange)original.get(getKey());
                if(container == null) {
                    container = new ContainerChange(getKey());
                }
                RowChange row = container.get(getRowId());
                if(row == null) {
                    row = new RowChange(getRowId());
                }
                // Set row's change map to the call object instead
                ((DataFieldCallBase<T>)call).setChangeMap(row.getChanges());
                Pair<StatusCode, T> pair = DataFieldOperator.setDataFieldOperation(getFields(), call, new ConfigCheck[]{ConfigCheck.IS_SUBFIELD});
                // If we have a value and there's been a value change then add the change objects
                if(pair.getRight() != null && pair.getLeft() != StatusCode.NO_CHANGE_IN_VALUE) {
                    // Put row change to container change and put container change to the original change map.
                    // Both of these are maps so we can just put the values without worrying about overwriting something. If they already were in the maps then we are using the same objects anyway
                    container.put(row);
                    original.put(container.getKey(), container);
                }
                return pair;
            default:
                return new ImmutablePair<>(StatusCode.INCORRECT_PARAMETERS, null);
        }
    }
}
