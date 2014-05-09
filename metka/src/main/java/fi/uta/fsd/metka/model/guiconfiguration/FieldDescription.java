package fi.uta.fsd.metka.model.guiconfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.uta.fsd.metka.data.enums.FieldType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties("_comment")
public class FieldDescription {
    @XmlElement private final String key;
    @XmlElement private FieldType displayType; // Can be ignored for now, only defined here for completeness
    @XmlElement private Boolean multiline;
    @XmlElement private Boolean multichoice; // Can be ignored for now, only defined here for completeness
    @XmlElement private final List<String> columnFields = new ArrayList<>();
    @XmlElement private Boolean showSaveInfo;
    @XmlElement private Boolean showReferenceValue;

    @JsonCreator
    public FieldDescription(@JsonProperty("key")String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public FieldType getDisplayType() {
        return displayType;
    }

    public void setDisplayType(FieldType displayType) {
        this.displayType = displayType;
    }

    public Boolean getMultiline() {
        return multiline;
    }

    public void setMultiline(Boolean multiline) {
        this.multiline = multiline;
    }

    public Boolean getMultichoice() {
        return multichoice;
    }

    public void setMultichoice(Boolean multichoice) {
        this.multichoice = multichoice;
    }

    public List<String> getColumnFields() {
        return columnFields;
    }

    public Boolean getShowSaveInfo() {
        return showSaveInfo;
    }

    public void setShowSaveInfo(Boolean showSaveInfo) {
        this.showSaveInfo = showSaveInfo;
    }

    public Boolean getShowReferenceValue() {
        return showReferenceValue;
    }

    public void setShowReferenceValue(Boolean showReferenceValue) {
        this.showReferenceValue = showReferenceValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldDescription that = (FieldDescription) o;

        if (!key.equals(that.key)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}