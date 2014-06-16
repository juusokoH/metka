package fi.uta.fsd.metka.mvc.domain;

import fi.uta.fsd.metka.data.enums.ConfigurationType;
import fi.uta.fsd.metka.data.enums.UIRevisionState;
import fi.uta.fsd.metka.data.repository.StudyRepository;
import fi.uta.fsd.metka.model.access.calls.SavedDataFieldCall;
import fi.uta.fsd.metka.model.configuration.Configuration;
import fi.uta.fsd.metka.model.data.RevisionData;
import fi.uta.fsd.metka.mvc.domain.simple.RevisionViewDataContainer;
import fi.uta.fsd.metka.mvc.domain.simple.transfer.SearchResult;
import fi.uta.fsd.metka.mvc.domain.simple.transfer.TransferObject;
import fi.uta.fsd.metka.mvc.domain.simple.study.StudySearchSO;
import fi.uta.fsd.metka.mvc.search.GeneralSearch;
import fi.uta.fsd.metka.mvc.search.RevisionDataRemovedContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static fi.uta.fsd.metka.data.util.ModelValueUtil.*;

@Service
public class StudyService {
    @Autowired
    private StudyRepository repository;
    @Autowired
    private GeneralSearch generalSearch;

    @Autowired
    private ConfigurationService configService;

    public List<SearchResult> searchForStudies(StudySearchSO query) {
        List<SearchResult> resultList = new ArrayList<>();
        // TODO: Find searched studies and create search result objects for them.

        // For now return all studies with their latest approved and draft revisions
        List<RevisionDataRemovedContainer> datas = null;
        try {
            datas = generalSearch.tempFindAllStudies();
        } catch(Exception ex) {
            // TODO: better exception handling with messages to the user
            ex.printStackTrace();
            return resultList;
        }

        for(RevisionDataRemovedContainer container : datas) {
            SearchResult study = resultSOFromRevisionData(container.getData());
            if(study != null) {
                if(container.isRemoved()) {
                    study.setState(UIRevisionState.REMOVED);
                }
                resultList.add(study);
            }
        }



        return resultList;
    }

    // Helper functions
    private SearchResult resultSOFromRevisionData(RevisionData data) {
        // check if data is for series
        if(data == null || data.getConfiguration().getType() != ConfigurationType.STUDY) {
            return null;
        }

        SearchResult so = new SearchResult();
        so.setId(data.getKey().getId());
        so.setRevision(data.getKey().getRevision());
        so.setState(UIRevisionState.fromRevisionState(data.getState()));
        so.setByKey("id", extractStringSimpleValue(data.dataField(SavedDataFieldCall.get("id")).getRight()));
        so.setByKey("title", extractStringSimpleValue(data.dataField(SavedDataFieldCall.get("title")).getRight()));

        return so;
    }

    /**
     * Return a default revision number for requested revisionable
     * @param id Revisionable id
     * @return
     */
    public Integer findSingleRevisionNo(Long id) {
        Integer revision = generalSearch.findSingleRevisionNo(id);
        return revision;
    }

    /**
     * Find requested revision data and convert it to SeriesSingle simple object
     * @param id Revisionable id
     * @param revision Revision number
     * @return Revision data converted to TransferObject
     */
    public RevisionViewDataContainer findSingleRevision(Long id, Integer revision) {
        RevisionData data = null;

        // Check for FileLinkQueue events.
        // If given id/revision belongs to a draft revision then process possible FileLinkQueue events.
        // This makes sure that any recently added file references are found from their respective REFERENCECONTAINERs.
        // Also if there is a new POR file present then that is parsed and the data is added
        try {
            repository.checkFileLinkQueue(id, revision);
        } catch(IOException ex) {
            // TODO: better exception handling with messages to the user
            ex.printStackTrace();
        }

        try {
            data = generalSearch.findSingleRevision(id, revision, ConfigurationType.STUDY);
        } catch(IOException ex) {
            // TODO: better exception handling with messages to the user
            ex.printStackTrace();
            return null;
        }

        if(data == null) {
            return null;
        }

        Configuration config = configService.findByTypeAndVersion(data.getConfiguration());
        TransferObject single = TransferObject.buildTransferObjectFromRevisionData(data);

        return new RevisionViewDataContainer(single, config);
    }

    public RevisionViewDataContainer newStudy(Long acquisition_number) {
        RevisionData data;
        try {
            data = repository.getNew(acquisition_number);
        } catch(IOException ex) {
            // TODO: better exception handling with messages to the user
            ex.printStackTrace();
            return null;
        }
        Configuration config = configService.findByTypeAndVersion(data.getConfiguration());
        TransferObject single = TransferObject.buildTransferObjectFromRevisionData(data);

        return new RevisionViewDataContainer(single, config);
    }

    public RevisionViewDataContainer editStudy(Long id) {
        try {
            RevisionData data = repository.editStudy(id);
            Configuration config = configService.findByTypeAndVersion(data.getConfiguration());
            TransferObject single = TransferObject.buildTransferObjectFromRevisionData(data);
            return new RevisionViewDataContainer(single, config);
        } catch(Exception ex) {
            // TODO: better exception handling with messages to the user
            ex.printStackTrace();
            return null;
        }
    }

    public boolean saveStudy(TransferObject to) {
        try {
            boolean result = repository.saveStudy(to);
            // Check for FileLinkQueue events.
            // If given id/revision belongs to a draft revision (and it should when we are saving) then process possible FileLinkQueue events.
            // This makes sure that any recently added file references are found from their respective REFERENCECONTAINERs.
            // Also if there is a new POR file present then that is parsed and the data is added
            if(result) {
                try {
                    repository.checkFileLinkQueue(to.getId(), to.getRevision());
                } catch(IOException ex) {
                    // TODO: better exception handling with messages to the user
                    ex.printStackTrace();
                }
            }
            return result;
        } catch(Exception ex) {
            // TODO: better exception handling with messages to the user
            ex.printStackTrace();
            return false;
        }
    }

    public boolean approveStudy(TransferObject single) {
        try {
            return repository.approveStudy(single.getId());
        } catch(Exception ex) {
            // TODO: better exception handling with messages to the user
            ex.printStackTrace();
            return false;
        }
    }
}
