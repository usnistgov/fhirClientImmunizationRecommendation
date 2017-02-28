/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.fhir.client.ir;

import org.tch.fc.model.TestEvent;

/**
 *
 * @author mccaffrey
 */
public class Immunization {
    
    private String status = null;
    private String vaccineCode = null;
    private String patientReference = null;
    private String date = null;
    private Boolean wasNotGiven = null;
    private Boolean reported = null;
    private TestEvent testEvent = null;

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the vaccineCode
     */
    public String getVaccineCode() {
        return vaccineCode;
    }

    /**
     * @param vaccineCode the vaccineCode to set
     */
    public void setVaccineCode(String vaccineCode) {
        this.vaccineCode = vaccineCode;
    }

    /**
     * @return the patientReference
     */
    public String getPatientReference() {
        return patientReference;
    }

    /**
     * @param patientReference the patientReference to set
     */
    public void setPatientReference(String patientReference) {
        this.patientReference = patientReference;
    }

    /**
     * @return the wasNotGiven
     */
    public Boolean getWasNotGiven() {
        return wasNotGiven;
    }

    /**
     * @param wasNotGiven the wasNotGiven to set
     */
    public void setWasNotGiven(Boolean wasNotGiven) {
        this.wasNotGiven = wasNotGiven;
    }

    /**
     * @return the reported
     */
    public Boolean getReported() {
        return reported;
    }

    /**
     * @param reported the reported to set
     */
    public void setReported(Boolean reported) {
        this.reported = reported;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }
    
    
    
    
    
}
