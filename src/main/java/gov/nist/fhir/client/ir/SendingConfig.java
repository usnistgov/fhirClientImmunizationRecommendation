/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.fhir.client.ir;

import java.util.Collection;

/**
 *
 * @author mccaffrey
 */
public class SendingConfig {
    
    private String testCaseNumber = null;
    private String gender = null;
    private String birthdate = null;
    private String assessmentDate = null;
    private Collection<Immunization> immunizationData = null;

    /**
     * @return the gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * @param gender the gender to set
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * @return the birthdate
     */
    public String getBirthdate() {
        return birthdate;
    }

    /**
     * @param birthdate the birthdate to set
     */
    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    /**
     * @return the immunizationData
     */
    public Collection<Immunization> getImmunizationData() {
        return immunizationData;
    }

    /**
     * @param immunizationData the immunizationData to set
     */
    public void setImmunizationData(Collection<Immunization> immunizationData) {
        this.immunizationData = immunizationData;
    }

    /**
     * @return the assessmentDate
     */
    public String getAssessmentDate() {
        return assessmentDate;
    }

    /**
     * @param assessmentDate the assessmentDate to set
     */
    public void setAssessmentDate(String assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    /**
     * @return the testCaseNumber
     */
    public String getTestCaseNumber() {
        return testCaseNumber;
    }

    /**
     * @param testCaseNumber the testCaseNumber to set
     */
    public void setTestCaseNumber(String testCaseNumber) {
        this.testCaseNumber = testCaseNumber;
    }
    
    
}
