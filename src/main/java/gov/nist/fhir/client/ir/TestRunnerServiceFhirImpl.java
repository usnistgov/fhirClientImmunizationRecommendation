package gov.nist.fhir.client.ir;

import fhir.util.Serialize;
import gov.nist.healthcare.cds.domain.wrapper.ActualForecast;
import gov.nist.healthcare.cds.domain.Event;
import gov.nist.healthcare.cds.domain.FixedDate;
import gov.nist.healthcare.cds.domain.SoftwareConfig;
import gov.nist.healthcare.cds.domain.TestCase;
import gov.nist.healthcare.cds.domain.VaccinationEvent;
import gov.nist.healthcare.cds.domain.wrapper.EngineResponse;
import gov.nist.healthcare.cds.domain.wrapper.ResponseVaccinationEvent;
import gov.nist.healthcare.cds.domain.wrapper.TestCasePayLoad;
import gov.nist.healthcare.cds.domain.wrapper.TestCasePayLoad.VaccinationEventPayLoad;
import gov.nist.healthcare.cds.domain.wrapper.VaccineRef;
import gov.nist.healthcare.cds.enumeration.Gender;
import gov.nist.healthcare.cds.service.TestRunnerService;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
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
    public EngineResponse run(SoftwareConfig config, TestCasePayLoad tc) {

        EngineResponse response = new EngineResponse();
        List<ActualForecast> forecasts = new ArrayList<>();
        response.setForecasts(forecasts);
        List<ResponseVaccinationEvent> evaluatedEvents = new ArrayList<>();
        response.setEvents(evaluatedEvents);
        //response.setEvaluatedEvents(evaluatedEvents);
        ImmunizationRecommendationClient irc = new ImmunizationRecommendationClient();
        Routing routing = new Routing();
        routing.setFhirAdapterUrl(this.getAdapterUrl());
        routing.setForecastType(config.getUser());
        routing.setForecastUrl(config.getEndPoint());

        SendingConfig sendingConfig = new SendingConfig();

        Date assessmentDate = tc.getEvaluationDate(); 
        Date dob = tc.getDateOfBirth();

        sendingConfig.setAssessmentDate(TranslationUtils.translateJavaDateToFhirDate(assessmentDate));
        sendingConfig.setBirthdate(TranslationUtils.translateJavaDateToFhirDate(dob));
        sendingConfig.setGender(tc.getGender().getDetails());

        //Set<Event> events = tc.getEvents();
        List<VaccinationEventPayLoad> events = tc.getImmunizations();
        Collection<Immunization> imms = new ArrayList<Immunization>();
        if (events != null && events.size() > 0) {
            Iterator<VaccinationEventPayLoad> it = events.iterator();

            while (it.hasNext()) {
                VaccinationEventPayLoad event = (VaccinationEventPayLoad) it.next();
                Immunization imm = new Immunization();

                Date immDate = event.getAdministred();
                imm.setDate(TranslationUtils.translateJavaDateToFhirDate(immDate));
                // TODO: Check. Reference to patient no longer exists???
                //imm.setPatientReference(tc.getPatient().getId());
                imm.setVaccineCode(event.getRef().getCvx());
                imms.add(imm);
            }
        }
        sendingConfig.setImmunizationData(imms);
        Bundle result = null;
        try {
            result = (Bundle) irc.getImmunizationRecommendation(routing, sendingConfig);
        } catch (IOException ex) { 
            Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        Serialize serial = new Serialize();
        response.setResponse(serial.it(result, "sut.xml"));
        
        EList<BundleEntry> entries = result.getEntry();
        Iterator<BundleEntry> it = entries.iterator();
        while (it.hasNext()) {
            BundleEntry entry = it.next();
            ResourceContainer resource = entry.getResource();
            ImmunizationRecommendation ir = resource.getImmunizationRecommendation();
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
     //   TestRunnerService test = new TestRunnerServiceFhirImpl("http://localhost:8080/forecast/ImmunizationRecommendations");
        TestRunnerService test = new TestRunnerServiceFhirImpl("https://localhost:8443/forecast/ImmunizationRecommendations");

        SoftwareConfig config = new SoftwareConfig();
        TestCasePayLoad tc = new TestCasePayLoad();

        config.setUser("TCH");
        config.setEndPoint("http://tchforecasttester.org/fv/forecast");

        //Patient patient = new Patient();
        //Date dob = new FixedDate("01/01/2016");

        //patient.setDob(dob);
        //patient.setGender(Gender.F);
        //tc.setPatient(patient);        
        tc.setGender(Gender.F);
        
        Calendar evalCal = Calendar.getInstance();
        evalCal.set(2017, 1, 1);
        Date evalDate = evalCal.getTime();
        
        Calendar dobCal = Calendar.getInstance();
        dobCal.set(2016, 1, 1);
        Date dobDate = dobCal.getTime();
                
        tc.setEvaluationDate(evalDate);
        tc.setDateOfBirth(dobDate);

        Calendar immCal = Calendar.getInstance();
        immCal.set(2017, 1, 1);
        Date immDate = immCal.getTime();
        
        
        /*
        VaccinationEvent ve1 = new VaccinationEvent();
        Set<Event> events = new HashSet<Event>();
        ve1.setMVX("110");
        ve1.setDate(new FixedDate("01/01/2017"));

        events.add(ve1);

        VaccinationEvent ve2 = new VaccinationEvent();
        ve2.setMVX("116");
        ve2.setDate(new FixedDate("01/01/2017"));
        events.add(ve2);

        VaccinationEvent ve3 = new VaccinationEvent();
        ve3.setMVX("133");
        ve3.setDate(new FixedDate("01/01/2017"));
        events.add(ve3);
*/
        //List<VaccinationEventPayLoad> vaccinationEvents = new ArrayList<VaccinationEventPayLoad>();
        VaccineRef vr1 = new VaccineRef();
        vr1.setCvx("110");
        tc.addImmunization(vr1, immDate);
        
        VaccineRef vr2 = new VaccineRef();
        vr2.setCvx("116");
        tc.addImmunization(vr2, immDate);
        
        VaccineRef vr3 = new VaccineRef();
        vr3.setCvx("133");
        tc.addImmunization(vr3, immDate);
        
                        
        // http://tchforecasttester.org/fv/forecast?evalDate=20170101&evalSchedule=&resultFormat=text&patientDob=20160101&patientSex=F&vaccineDate1=20170101&vaccineCvx1=110
        //tc.setEvents(events);
        //tc.setImmunizations(events);
        EngineResponse run = test.run(config, tc);
        System.out.println(run.getForecasts().size());
        //System.out.println(run.getEvaluatedEvents().size());
        System.out.println(run.getEvents().size());        
        System.out.println(run.getResponse());
    }

}
