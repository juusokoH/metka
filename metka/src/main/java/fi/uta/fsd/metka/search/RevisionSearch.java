package fi.uta.fsd.metka.search;

import fi.uta.fsd.metka.storage.repository.enums.ReturnResult;
import fi.uta.fsd.metka.transfer.revision.*;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface RevisionSearch {
    public Pair<ReturnResult, List<RevisionSearchResult>> search(RevisionSearchRequest request);

    public Pair<ReturnResult, List<RevisionSearchResult>> studyIdSearch(String studyId);

    Pair<ReturnResult,List<RevisionSearchResult>> collectRevisionHistory(RevisionHistoryRequest request);

    List<RevisionCompareResponseRow> compareRevisions(RevisionCompareRequest request);
}
