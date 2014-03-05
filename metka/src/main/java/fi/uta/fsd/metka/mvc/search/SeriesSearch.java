package fi.uta.fsd.metka.mvc.search;

import fi.uta.fsd.metka.mvc.domain.simple.series.SeriesSearchSO;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Transactional(readOnly = true)
public interface SeriesSearch {
    public List<String> findAbbreviations() throws IOException;

    /**
     * Return all series matching the given search terms.
     * @param query User defined search terms for finding series
     * @return List of Object[2]. First column contains the RevisionData object second column contains the removed value of the series.
     * @throws IOException
     */
    public List<RevisionDataRemovedContainer> findSeries(SeriesSearchSO query) throws IOException;
}
