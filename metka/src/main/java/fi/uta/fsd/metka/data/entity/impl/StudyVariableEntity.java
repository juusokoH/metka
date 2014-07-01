package fi.uta.fsd.metka.data.entity.impl;

import fi.uta.fsd.metka.data.entity.RevisionableEntity;
import fi.uta.fsd.metka.data.enums.ConfigurationType;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Entity class for study variable revisionable objects.
 * Unlike most of the revisionable entities this contains additional information namely the id of a
 * study variables revisionable object this variable belongs to and id of the variable saved using this object.
 * These are exclusively used to speed up data manipulation operations and should not be used outside of variable parsing.
 * STUDY_VARIABLES_ID is not a key field (primary or foreign) and it is assumed to be filled when a new
 * study variable is created.
 *
 * TODO: Add study id as a straight reference so that no joins need to be created to find all variables of a study. The link is immutable anyway so it doesn't hurt to have it here.
 */
@Entity
@DiscriminatorValue(ConfigurationType.Values.STUDY_VARIABLE)
public class StudyVariableEntity extends RevisionableEntity {
    @Column(name = "STUDY_VARIABLES_ID")
    private Long studyVariablesId;

    @Column(name = "VARIABLE_ID")
    private String variableId;

    public Long getStudyVariablesId() {
        return studyVariablesId;
    }

    public void setStudyVariablesId(Long studyVariablesId) {
        this.studyVariablesId = studyVariablesId;
    }

    public String getVariableId() {
        return variableId;
    }

    public void setVariableId(String variableId) {
        this.variableId = variableId;
    }
}