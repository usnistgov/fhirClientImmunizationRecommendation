/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.fhir.client.ir;

import gov.nist.healthcare.cds.domain.wrapper.ActualForecast;
import gov.nist.healthcare.cds.domain.Date;
import gov.nist.healthcare.cds.domain.Event;
import gov.nist.healthcare.cds.domain.FixedDate;
import gov.nist.healthcare.cds.domain.Patient;
import gov.nist.healthcare.cds.domain.SoftwareConfig;
import gov.nist.healthcare.cds.domain.TestCase;
import gov.nist.healthcare.cds.domain.VaccinationEvent;
import gov.nist.healthcare.cds.domain.wrapper.ActualEvaluation;
import gov.nist.healthcare.cds.domain.wrapper.EngineResponse;
import gov.nist.healthcare.cds.domain.wrapper.ResponseVaccinationEvent;
import gov.nist.healthcare.cds.enumeration.Gender;
import gov.nist.healthcare.cds.service.TestRunnerService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.emf.common.util.EList;
import org.hl7.fhir.Bundle;
import org.hl7.fhir.BundleEntry;
import org.hl7.fhir.ResourceContainer;
import org.hl7.fhir.ImmunizationRecommendation;

/**
 *
 * @author mccaffrey
 */
public class TestRunnerServiceFhirImpl implements TestRunnerService {

    private String adapterUrl = null;

    public TestRunnerServiceFhirImpl(String adapterUrl) {
        this.setAdapterUrl(adapterUrl);
    }

    @Override
    public EngineResponse run(SoftwareConfig config, TestCase tc) {

        EngineResponse response = new EngineResponse();
        List<ActualForecast> forecasts = new ArrayList<>();
        response.setForecasts(forecasts);
        List<ResponseVaccinationEvent> evaluatedEvents = new ArrayList<>();
        response.setEvaluatedEvents(evaluatedEvents);
        ImmunizationRecommendationClient irc = new ImmunizationRecommendationClient();
        Routing routing = new Routing();
        routing.setFhirAdapterUrl(this.getAdapterUrl());
        routing.setForecastType(config.getUser());
        routing.setForecastUrl(config.getEndPoint());

        SendingConfig sendingConfig = new SendingConfig();
        //sendingConfig.setAssessmentDate("2016-12-01T10:57:34+01:00");
        //sendingConfig.setBirthdate("2016-11-01T10:57:34+01:00");

        FixedDate assessmentDate = (FixedDate) tc.getEvalDate();
        FixedDate dob = (FixedDate) tc.getPatient().getDob();

        sendingConfig.setAssessmentDate(TranslationUtils.translateCsdiDateToFhirDate(assessmentDate));
        sendingConfig.setBirthdate(TranslationUtils.translateCsdiDateToFhirDate(dob));
        sendingConfig.setGender(tc.getPatient().getGender().getDetails());

        Set<Event> events = tc.getEvents();
        Collection<Immunization> imms = new ArrayList<Immunization>();
        if (events != null && events.size() > 0) {
            Iterator<Event> it = events.iterator();

            while (it.hasNext()) {
                VaccinationEvent event = (VaccinationEvent) it.next();
                Immunization imm = new Immunization();

                FixedDate immDate = (FixedDate) event.getDate();
                imm.setDate(TranslationUtils.translateCsdiDateToFhirDate(immDate));                
                imm.setPatientReference(tc.getPatient().getId());
                imm.setVaccineCode(event.getMVX());
                imms.add(imm);
            }
        }        
        sendingConfig.setImmunizationData(imms);
        Bundle result = null;
        try {
            result = (Bundle) irc.getImmunizationRecommendation(routing, sendingConfig);
        } catch (IOException ex) {
            Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            return null;
        }
        
        EList<BundleEntry> entries = result.getEntry();
        Iterator<BundleEntry> it = entries.iterator();
        while(it.hasNext()) {
            BundleEntry entry = it.next();
            ResourceContainer resource = entry.getResource();
            ImmunizationRecommendation ir = resource.getImmunizationRecommendation();
            if(ir != null) {
                ActualForecast forecast = TranslationUtils.translateImmunizationRecommendationToActualForecast(ir);
                response.getForecasts().add(forecast);
            }
            org.hl7.fhir.Immunization imm = resource.getImmunization();
            
            if(imm != null) {
                ResponseVaccinationEvent rve = TranslationUtils.translateImmunizationToResponseVaccinationEvent(imm);
                response.getEvaluatedEvents().add(rve);
            }
            
            
        }
        ResponseVaccinationEvent rve = new ResponseVaccinationEvent();
        
        ActualEvaluation actual = new ActualEvaluation();
        
        
        
        //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.  
        return response;
    }

    /**
     * @return the adapterUrl
     */
    public String getAdapterUrl() {
        return adapterUrl;
    }

    /**
     * @param adapterUrl the adapterUrl to set
     */
    public void setAdapterUrl(String adapterUrl) {
        this.adapterUrl = adapterUrl;
    }

    public static void main(String[] args) {
        TestRunnerService test = new TestRunnerServiceFhirImpl("http://localhost:8080/forecast/ImmunizationRecommendations");

        SoftwareConfig config = new SoftwareConfig();
        TestCase tc = new TestCase();

        config.setUser("TCH");
        config.setEndPoint("http://tchforecasttester.org/fv/forecast");

        Patient patient = new Patient();
        Date dob = new FixedDate("01/01/2016");

        patient.setDob(dob);
        patient.setGender(Gender.F);
        tc.setPatient(patient);
        Date evalDate = new FixedDate("01/01/2017");
        tc.setEvalDate(evalDate);
        
        VaccinationEvent ve1 = new VaccinationEvent();
        Set<Event> events = new HashSet<Event>();
        ve1.setMVX("110");
        ve1.setDate(new FixedDate("01/01/2017"));
        
        events.add(ve1);
        
//        VaccinationEvent ve2 = new VaccinationEvent();
        
    //    ve2.setMVX("116");
      //  ve2.setDate(new FixedDate("01/01/2017"));
        
        //events.add(ve2);
        
  //              VaccinationEvent ve3 = new VaccinationEvent();
        
//        ve3.setMVX("133");
//        ve3.setDate(new FixedDate("01/01/2017"));
        
        //events.add(ve3);
              
        // http://tchforecasttester.org/fv/forecast?evalDate=20170101&evalSchedule=&resultFormat=text&patientDob=20160101&patientSex=F&vaccineDate1=20170101&vaccineCvx1=110
        tc.setEvents(events);
        EngineResponse run = test.run(config, tc);
System.out.println(run.getForecasts().size());
     //   List<ActualForecast> actual = run.getForecasts();
//        actual.get(0).getVaccine();
  //      actual.get(0);
    //    List<ResponseVaccinationEvent> rve = run.getEvaluatedEvents();
     
    }

}
