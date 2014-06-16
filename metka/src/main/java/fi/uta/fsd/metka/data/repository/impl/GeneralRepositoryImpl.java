package fi.uta.fsd.metka.data.repository.impl;

import fi.uta.fsd.metka.data.entity.RevisionEntity;
import fi.uta.fsd.metka.data.entity.RevisionableEntity;
import fi.uta.fsd.metka.data.entity.SequenceEntity;
import fi.uta.fsd.metka.data.enums.ConfigurationType;
import fi.uta.fsd.metka.data.enums.RevisionState;
import fi.uta.fsd.metka.data.enums.repositoryResponses.DraftRemoveResponse;
import fi.uta.fsd.metka.data.enums.repositoryResponses.LogicalRemoveResponse;
import fi.uta.fsd.metka.data.repository.GeneralRepository;
import fi.uta.fsd.metka.data.util.JSONUtil;
import fi.uta.fsd.metka.model.data.RevisionData;
import javassist.NotFoundException;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

@Repository
public class GeneralRepositoryImpl implements GeneralRepository {
    @PersistenceContext(name = "entityManager")
    private EntityManager em;
    @Autowired
    private JSONUtil json;

    @Override
    public Long getAdjancedRevisionableId(Long currentId, String type, boolean forward)
            throws NotFoundException, MissingResourceException {
        List<RevisionableEntity> list = em.createQuery("SELECT r FROM RevisionableEntity r " +
                    "WHERE r.id "+(forward?">":"<")+" :id AND r.type = :type " +
                    "AND r.removed = false " +
                    "AND r.curApprovedNo IS NOT NULL " +
                    "ORDER BY r.id "+(forward?"ASC":"DESC"), RevisionableEntity.class)
                .setParameter("id", currentId)
                .setParameter("type", ConfigurationType.valueOf(type.toUpperCase()))
                .setMaxResults(1)
                .getResultList();

        if(list.size() == 0) {
            throw new NotFoundException("");
        }
        return list.get(0).getId();
    }

    @Override
    public DraftRemoveResponse removeDraft(String type, Long id) {
        List<RevisionableEntity> list = em.createQuery("SELECT r FROM RevisionableEntity r " +
                "WHERE r.id = :id AND r.type = :type", RevisionableEntity.class)
                .setParameter("id", id)
                .setParameter("type", ConfigurationType.valueOf(type.toUpperCase()))
                .getResultList();
        if(list.size() == 0) {
            return DraftRemoveResponse.NO_REVISIONABLE;
        }
        RevisionableEntity entity = list.get(0);
        if(!entity.hasDraft()) {
            return DraftRemoveResponse.NO_DRAFT;
        }
        RevisionEntity rev = em.find(RevisionEntity.class, entity.latestRevisionKey());

        if(!rev.getState().equals(RevisionState.DRAFT)) {
            // TODO: Log error since there is data discrepancy
            return DraftRemoveResponse.NO_DRAFT;
        }

        em.remove(rev);

        if(entity.getCurApprovedNo() == null) {
            // No revisions remaining, remove the whole revisionable entity.
            em.remove(entity);
            return DraftRemoveResponse.FINAL_REVISION;
        } else {
            entity.setLatestRevisionNo(entity.getCurApprovedNo());
            return DraftRemoveResponse.SUCCESS;
        }
    }

    @Override
    public LogicalRemoveResponse removeLogical(String type, Long id) {
        List<RevisionableEntity> list = em.createQuery("SELECT r FROM RevisionableEntity r " +
                "WHERE r.id = :id AND r.type = :type", RevisionableEntity.class)
                .setParameter("id", id)
                .setParameter("type", type.toUpperCase())
                .getResultList();
        if(list.size() == 0) {
            return LogicalRemoveResponse.NO_REVISIONABLE;
        }
        RevisionableEntity entity = list.get(0);
        if(entity.getCurApprovedNo() == null) {
            return LogicalRemoveResponse.NO_APPROVED;
        }
        if(entity.hasDraft()) {
            return LogicalRemoveResponse.OPEN_DRAFT;
        }

        entity.setRemoved(true);
        entity.setRemovalDate(new LocalDateTime());
        return LogicalRemoveResponse.SUCCESS;
    }

    @Override
    public List<RevisionData> getLatestRevisionsForType(ConfigurationType type, Boolean approvedOnly) throws IOException {
        List<RevisionData> dataList = new ArrayList<>();
        List<RevisionableEntity> revisionables = em.createQuery("SELECT r FROM RevisionableEntity r"+(approvedOnly? " WHERE r.curApprovedNo IS NOT NULL":""), RevisionableEntity.class).getResultList();

        RevisionEntity revision;
        for(RevisionableEntity entity : revisionables) {
            if(approvedOnly) {
                revision = em.find(RevisionEntity.class, entity.currentApprovedRevisionKey());
            } else {
                revision = em.find(RevisionEntity.class, entity.latestRevisionKey());
            }
            if(revision != null) {
                dataList.add(json.readRevisionDataFromString(revision.getData()));
            }
        }

        return dataList;
    }

    @Override
    public RevisionData getRevision(Long id, Integer revision) throws IOException {
        List<RevisionEntity> revisions =
                em.createQuery(
                    "SELECT r FROM RevisionEntity r " +
                    "WHERE r.key.revisionableId = :id AND r.key.revisionNo = :revision",
                    RevisionEntity.class)
                .setParameter("id", id)
                .setParameter("revision", revision)
                .getResultList();

        RevisionEntity ent = DataAccessUtils.requiredSingleResult(revisions);

        RevisionData data = json.readRevisionDataFromString(ent.getData());

        return data;
    }

    @Override
    public String getRevisionData(Long id, Integer revision) {
        List<RevisionEntity> revisions =
                em.createQuery(
                        "SELECT r FROM RevisionEntity r " +
                                "WHERE r.key.revisionableId = :id AND r.key.revisionNo = :revision",
                        RevisionEntity.class)
                        .setParameter("id", id)
                        .setParameter("revision", revision)
                        .getResultList();

        RevisionEntity ent = DataAccessUtils.requiredSingleResult(revisions);

        return (ent != null) ? ent.getData() : null;
    }

    @Override
    public SequenceEntity getNewSequenceValue(String key) {
        return getNewSequenceValue(key, 1L);
    }

    @Override
    public SequenceEntity getNewSequenceValue(String key, Long initialValue) {
        SequenceEntity seq = em.find(SequenceEntity.class, key);
        if(seq == null) {
            seq = new SequenceEntity();
            seq.setKey(key);
            seq.setSequence(initialValue);
            em.persist(seq);
        } else {
            seq.setSequence(seq.getSequence()+1);
        }
        return seq;
    }
}
