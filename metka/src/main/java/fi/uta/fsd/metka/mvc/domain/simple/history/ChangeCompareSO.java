package fi.uta.fsd.metka.mvc.domain.simple.history;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: lasseku
 * Date: 1/20/14
 * Time: 1:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChangeCompareSO {
    private Integer id;
    private Integer begin;
    private Integer end;
    private Collection<ChangeSO> changes;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBegin() {
        return begin;
    }

    public void setBegin(Integer begin) {
        this.begin = begin;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Collection<ChangeSO> getChanges() {
        return changes;
    }

    public void setChanges(Collection<ChangeSO> changes) {
        this.changes = changes;
    }
}
