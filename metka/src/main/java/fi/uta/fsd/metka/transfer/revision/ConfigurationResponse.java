package fi.uta.fsd.metka.transfer.revision;

import fi.uta.fsd.metka.model.configuration.Configuration;
import fi.uta.fsd.metka.storage.repository.enums.ReturnResult;

public class ConfigurationResponse {
    private final ReturnResult result;
    private final Configuration configuration;

    public ConfigurationResponse(ReturnResult result, Configuration configuration) {
        this.result = result;
        this.configuration = configuration;
    }

    public ReturnResult getResult() {
        return result;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
