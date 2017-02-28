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
public class Response {
    
    private String httpCode = null;
    private String payload = null;

    /**
     * @return the httpCode
     */
    public String getHttpCode() {
        return httpCode;
    }

    /**
     * @param httpCode the httpCode to set
     */
    public void setHttpCode(String httpCode) {
        this.httpCode = httpCode;
    }

    /**
     * @return the payload
     */
    public String getPayload() {
        return payload;
    }

    /**
     * @param payload the payload to set
     */
    public void setPayload(String payload) {
        this.payload = payload;
    }
    
}
