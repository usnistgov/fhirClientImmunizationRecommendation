/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.fhir.client.ir;

import ca.uhn.fhir.context.FhirContext;

import gov.nist.healthcare.cds.domain.exception.ConnectionException;
import gov.nist.healthcare.cds.domain.wrapper.ActualForecast;
import gov.nist.healthcare.cds.domain.wrapper.EngineResponse;
import gov.nist.healthcare.cds.domain.wrapper.ResponseVaccinationEvent;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.emf.common.util.EList;
import org.hl7.fhir.Bundle;
import org.hl7.fhir.BundleEntry;
import org.hl7.fhir.ResourceContainer;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 *
 * @author mccaffrey
 */
public class Standalone {
    
    
    public static EngineResponse parseResponseFromXml(String xml) throws ConnectionException {
        
        EngineResponse response = new EngineResponse();
/*
        response.setResponse(xml);
        List<ActualForecast> afs = new ArrayList<ActualForecast>();
        response.setForecasts(afs);
        List<ResponseVaccinationEvent> rves = new ArrayList<ResponseVaccinationEvent>();
        response.setEvents(rves);
        
        
                    Bundle result = null;
                    
        DeSerialize deserial = new DeSerialize();
        result = (Bundle) deserial.it(new StringReader(xml), "*.xml");

           // response.setRequest(ImmunizationRecommendationClient.generateXml(routing, sendingConfig, useAdapter));
            Serialize serial = new Serialize();
            response.setResponse(serial.it(result, "sut.xml"));

            EList<BundleEntry> entries = result.getEntry();
            Iterator<BundleEntry> it = entries.iterator();
            while (it.hasNext()) {
                BundleEntry entry = it.next();
                ResourceContainer resource = entry.getResource();
                org.hl7.fhir.ImmunizationRecommendation ir = resource.getImmunizationRecommendation();
                if (ir != null) {
                    ActualForecast forecast = TranslationUtils.translateImmunizationRecommendationToActualForecast(ir);
                    response.getForecasts().add(forecast);
                }
                org.hl7.fhir.Immunization imm = resource.getImmunization();
                if (imm != null) {
                    ResponseVaccinationEvent rve = TranslationUtils.translateImmunizationToResponseVaccinationEvent(imm);
                    response.getEvents().add(rve);
                }

            }
        
        
        
*/
        return response;
    }

    
    public static final void main(String[] arg) throws IOException, ConnectionException {
        
      //  String fileString = new String(Files.readAllBytes(Paths.get("/home/mccaffrey/working/cdsiFhirResponse20170317.xml")), StandardCharsets.UTF_8);
            String fileString = new String(Files.readAllBytes(Paths.get("/home/mccaffrey/working/temp.xml")), StandardCharsets.UTF_8);
        EngineResponse response = Standalone.parseResponseFromXml(fileString);
        
        System.out.println(response.getEvents().size());
        System.out.println(response.getForecasts().size());
        
        
    }
    
}
