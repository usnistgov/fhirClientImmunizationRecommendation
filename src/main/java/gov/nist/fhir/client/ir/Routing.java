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
public class Routing {
    
    private String forecastUrl = null;
    private String forecastType = null;
    private String fhirAdapterUrl = null;
    private String userId = null;
    private String facilityId = null;
    private String password = null;

    /**
     * @return the forecastUrl
     */
    public String getForecastUrl() {
        return forecastUrl;
    }

    /**
     * @param forecastUrl the forecastUrl to set
     */
    public void setForecastUrl(String forecastUrl) {
        this.forecastUrl = forecastUrl;
    }

    /**
     * @return the forecastType
     */
    public String getForecastType() {
        return forecastType;
    }

    /**
     * @param forecastType the forecastType to set
     */
    public void setForecastType(String forecastType) {
        this.forecastType = forecastType;
    }

    /**
     * @return the fhirAdapterUrl
     */
    public String getFhirAdapterUrl() {
        return fhirAdapterUrl;
    }

    /**
     * @param fhirAdapterUrl the fhirAdapterUrl to set
     */
    public void setFhirAdapterUrl(String fhirAdapterUrl) {
        this.fhirAdapterUrl = fhirAdapterUrl;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the facilityId
     */
    public String getFacilityId() {
        return facilityId;
    }

    /**
     * @param facilityId the facilityId to set
     */
    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
}
