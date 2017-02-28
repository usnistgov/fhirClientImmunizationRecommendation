/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.fhir.client.ir;

/**
 *
 * @author mccaffrey
 */
public class ImmunizationRecommendation {
    
    private String date = null;
    private String dose = null;
    private String vaccineCode = null;

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

    /**
     * @return the dose
     */
    public String getDose() {
        return dose;
    }

    /**
     * @param dose the dose to set
     */
    public void setDose(String dose) {
        this.dose = dose;
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
    
    
}
