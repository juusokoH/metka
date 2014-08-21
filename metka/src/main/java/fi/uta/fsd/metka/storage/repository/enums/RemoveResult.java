package fi.uta.fsd.metka.storage.repository.enums;

/**
 * Special enumeration just for Revision remove and restore operation results since these are so different from other common results
 */
public enum RemoveResult {
    NOT_FOUND,
    WRONG_USER,
    SUCCESS_DRAFT,
    SUCCESS_LOGICAL,
    SUCCESS_RESTORE,
    FINAL_REVISION,
    OPEN_DRAFT,
    NOT_REMOVED
}