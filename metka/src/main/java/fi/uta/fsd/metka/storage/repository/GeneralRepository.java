package fi.uta.fsd.metka.storage.repository;

import fi.uta.fsd.metka.enums.ConfigurationType;
import fi.uta.fsd.metka.enums.repositoryResponses.DraftRemoveResponse;
import fi.uta.fsd.metka.enums.repositoryResponses.LogicalRemoveResponse;
import fi.uta.fsd.metka.model.data.RevisionData;
import fi.uta.fsd.metka.storage.entity.SequenceEntity;
import fi.uta.fsd.metka.storage.entity.key.RevisionKey;
import fi.uta.fsd.metka.storage.repository.enums.ReturnResult;
import fi.uta.fsd.metka.storage.response.RemovedInfo;
import javassist.NotFoundException;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.MissingResourceException;

/**
 * Contains methods for general operations that don't require their own repositories.
 */
@Transactional(readOnly = true, noRollbackFor = {NotFoundException.class, MissingResourceException.class})
public interface GeneralRepository {

    /**
     * Returns information on if revisionable object with given id has been set to removed state.
     * @param id Id of the revisionable object to be checked
     * @return Pair<Boolean, LocalDateTime> with left value being the removal state of the revisionable (true if removed) and right value being the removal time of said object.
     */
    public Pair<ReturnResult, RemovedInfo> getRevisionableRemovedInfo(Long id);

    /**
     * Returns the revisionable id of the object with given type adjacent to the object with given id.
     * Returns null if no such revisionable can be found. Type of the revisionable with current id is never checked.
     *
     * @param currentId Id of the revisionable from where the search starts
     * @param type ConfigurationType of the requested adjacent revisionable
     * @param forward Boolean telling if the search should be made forward (higher id) or backwards (lower id)
     * @return Revisionable id of the adjacent object with given type and direction from current object or null if no such revisionable can be found.
     */
    public Pair<ReturnResult, Long> getAdjacentRevisionableId(Long currentId, String type, boolean forward);

    /**
     * Remove draft revision from given revisionable object.
     * This performs an actual removal to the revision.
     * If DRAFT exists for the revisionable then it is removed from database and latest revision number of
     * revisionable is set to current approved revision number (since there can always be only one DRAFT).
     * After this, if there is no more revisions on the revisionable (i.e. current approved number was null)
     * then the whole revisionable is removed from database.
     *
     * @param type
     * @param id
     * @return
     */
    @Transactional(readOnly = false) public DraftRemoveResponse removeDraft(String type, Long id);

    /**
     * Performs a logical removal on requested revisionable object.
     * If the revisionable doesn't have any DRAFT revisions then it is marked as removed and
     * removal time is set to database.
     * TODO: Indicate this on the revision data somehow if need be
     * @param type
     * @param id
     * @return
     */
    @Transactional(readOnly = false) public LogicalRemoveResponse removeLogical(String type, Long id);

    // TODO: Reverse logical removal

    /**
     * Returns the revision data with given id and number.
     * Forwards the call to revision key variant with null type.
     * @param id RevisionableId of the requested revision
     * @param no Revision number of the requested revision
     * @return Pair with ReturnResult in the left value and returned RevisionData in the right value, or null if no RevisionData is returned.
     */
    public Pair<ReturnResult, RevisionData> getRevisionData(Long id, Integer no);

    /**
     * Returns the revision data with given id and number and checks that it is of the requested type.
     * Forwards the call to revision key variant.
     * @param id RevisionableId of the requested revision
     * @param no Revision number of the requested revision
     * @param type Type the requested revision should be,
     * @return Pair with ReturnResult in the left value and returned RevisionData in the right value, or null if no RevisionData is returned.
     */
    public Pair<ReturnResult, RevisionData> getRevisionDataOfType(Long id, Integer no, ConfigurationType type);

    /**
     * Returns the revision data with given id and number and checks that it is of the requested type.
     * If revision is found with the given id and no pair then the type of it is checked if it matches
     * the provided type, if it does or no type is provided then the RevisionData is returned, otherwise
     * or if no revision was found null is returned.
     * @param key RevisionKey of the requested revision
     * @param type Type the requested revision should be,
     * @return Pair with ReturnResult in the left value and returned RevisionData in the right value, or null if no RevisionData is returned.
     */
    public Pair<ReturnResult, RevisionData> getRevisionDataOfType(RevisionKey key, ConfigurationType type);

    /**
     * Returns the latest approved or draft revision for given revisionable id depending on the value of approvedOnly parameter.
     * If ConfigurationType is present then checks to see that returned revision matches given type.
     * @param id Id of the revisionable object for which a revision is requested.
     * @param approvedOnly If only approved revisions should be allowed
     * @return Pair<ReturnResult, RevisionData> with left value being the result code of the operation and right value being the returned RevisionData or null if return was unsuccessful
     */
    public Pair<ReturnResult, RevisionData> getLatestRevisionForIdAndType(Long id, boolean approvedOnly, ConfigurationType type);

    /**
     * Returns a revision number for given id.
     * If approvedOnly is true then returns the current approved number, otherwise returns latest revision number.
     * If type is present then checks to see that type of revisionable matches given type and if not a correct error value is returned
     * @param id Requested revisionable id
     * @param approvedOnly Disregards DRAFT revisions if true
     * @param type Requested type of revisionable, can be null in which case this check is omitted
     * @return
     */
    public Pair<ReturnResult, Integer> getLatestRevisionNoForIdAndType(Long id, boolean approvedOnly, ConfigurationType type);

    public Pair<ReturnResult, String> getRevisionDataString(Long id, Integer revision);

    public List<Integer> getAllRevisionNumbers(Long id);

    @Transactional(readOnly = false) public SequenceEntity getNewSequenceValue(String key);
    @Transactional(readOnly = false) public SequenceEntity getNewSequenceValue(String key, Long initialValue);

    /**
     * Takes provided RevisionData, serializes it and tries to insert it into database
     * @param revision RevisionData to be serialized and updated to database
     * @return ReturnResult informing if the operation was successful or not
     */
    @Transactional(readOnly = false) public ReturnResult updateRevisionData(RevisionData revision);
}