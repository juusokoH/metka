package fi.uta.fsd.metka.model.guiconfiguration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.uta.fsd.metka.model.interfaces.ModelBase;
import fi.uta.fsd.metka.model.general.ConfigurationKey;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "guiConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties("_comment")
public class GUIConfiguration implements ModelBase {
    @XmlElement private ConfigurationKey key;
    @XmlElement private final List<Container> content = new ArrayList<>();
    @XmlElement private final List<Button> buttons = new ArrayList<>();
    @XmlElement private final Map<String, FieldTitle> fieldTitles = new HashMap<>();

    public ConfigurationKey getKey() {
        return key;
    }

    public void setKey(ConfigurationKey key) {
        this.key = key;
    }

    public List<Container> getContent() {
        return content;
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public Map<String, FieldTitle> getFieldTitles() {
        return fieldTitles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GUIConfiguration that = (GUIConfiguration) o;

        if (!key.equals(that.key)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
