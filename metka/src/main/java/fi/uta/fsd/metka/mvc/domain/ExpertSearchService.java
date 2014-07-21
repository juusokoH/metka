package fi.uta.fsd.metka.mvc.domain;

import fi.uta.fsd.metka.data.enums.UIRevisionState;
import fi.uta.fsd.metka.data.repository.ConfigurationRepository;
import fi.uta.fsd.metka.data.repository.GeneralRepository;
import fi.uta.fsd.metka.data.repository.SavedSearchRepository;
import fi.uta.fsd.metka.model.access.calls.SavedDataFieldCall;
import fi.uta.fsd.metka.model.data.RevisionData;
import fi.uta.fsd.metka.model.data.container.SavedDataField;
import fi.uta.fsd.metka.transfer.expert.*;
import fi.uta.fsd.metkaSearch.SearcherComponent;
import fi.uta.fsd.metkaSearch.commands.searcher.SearchCommand;
import fi.uta.fsd.metkaSearch.commands.searcher.expert.ExpertRevisionSearchCommand;
import fi.uta.fsd.metkaSearch.results.ResultList;
import fi.uta.fsd.metkaSearch.results.RevisionResult;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ExpertSearchService {
    @Autowired
    private SearcherComponent searcher;

    @Autowired
    private GeneralRepository general;

    @Autowired
    private ConfigurationRepository configurations;

    @Autowired
    private SavedSearchRepository savedSearch;

    public ExpertSearchQueryResponse performQuery(ExpertSearchQueryRequest request) {
        ExpertSearchQueryResponse response = new ExpertSearchQueryResponse();
        response.setOperation(ExpertSearchOperation.QUERY);
        try {
            SearchCommand<RevisionResult> command = ExpertRevisionSearchCommand.build(request.getQuery(), configurations);
            List<RevisionResult> results = searcher.executeSearch(command).getResults();

            for(RevisionResult result : results) {
                try {
                    Pair<Boolean, LocalDateTime> info = general.getRevisionableRemovedInfo(result.getId());
                    RevisionData revision = general.getRevision(result.getId(), result.getNo());
                    ExpertSearchRevisionQueryResult qr = new ExpertSearchRevisionQueryResult();
                    if(info.getLeft()) {
                        qr.setState(UIRevisionState.REMOVED);
                    } else {
                        qr.setState(UIRevisionState.fromRevisionState(revision.getState()));
                    }
                    qr.setId(revision.getKey().getId());
                    qr.setNo(revision.getKey().getRevision());
                    qr.setType(revision.getConfiguration().getType());
                    // TODO: Maybe generalize this better
                    SavedDataField field;
                    switch(revision.getConfiguration().getType()) {
                        case STUDY:
                            field = revision.dataField(SavedDataFieldCall.get("title")).getRight();
                            if(field != null) {
                                qr.setTitle(field.getActualValue());
                            }
                            break;
                        case SERIES:
                            field = revision.dataField(SavedDataFieldCall.get("seriesname")).getRight();
                            if(field != null) {
                                qr.setTitle(field.getActualValue());
                            }
                            break;
                    }
                    response.getResults().add(qr);
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        } catch(UnsupportedOperationException uoe) {
            uoe.printStackTrace();
        } catch (QueryNodeException qne) {
            qne.printStackTrace();
        }
        return response;
    }

    public ExpertSearchListResponse listSavedSearcher() {
        ExpertSearchListResponse response = new ExpertSearchListResponse();
        List<SavedExpertSearchItem> items = savedSearch.listSavedSearches();
        for(SavedExpertSearchItem item : items) {
            response.getQueries().add(item);
        }
        return response;
    }

    public SavedExpertSearchItem saveExpertSearch(SavedExpertSearchItem item) {
        return savedSearch.saveExpertSearch(item);
    }
}