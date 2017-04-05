package gov.nist.fhir.client.ir;

import ca.uhn.fhir.context.FhirContext;
import fhir.util.Serialize;
import gov.nist.healthcare.cds.domain.wrapper.ActualForecast;
import gov.nist.healthcare.cds.domain.Event;
import gov.nist.healthcare.cds.domain.FixedDate;
import gov.nist.healthcare.cds.domain.SoftwareConfig;
import gov.nist.healthcare.cds.domain.TestCase;
import gov.nist.healthcare.cds.domain.VaccinationEvent;
import gov.nist.healthcare.cds.domain.exception.ConnectionException;
import gov.nist.healthcare.cds.domain.wrapper.EngineResponse;
import gov.nist.healthcare.cds.domain.wrapper.ResponseVaccinationEvent;
import gov.nist.healthcare.cds.domain.wrapper.TestCasePayLoad;
import gov.nist.healthcare.cds.domain.wrapper.TestCasePayLoad.VaccinationEventPayLoad;
import gov.nist.healthcare.cds.domain.wrapper.VaccineRef;
import gov.nist.healthcare.cds.enumeration.FHIRAdapter;
import gov.nist.healthcare.cds.enumeration.Gender;
import gov.nist.healthcare.cds.service.TestRunnerService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.emfjson.jackson.annotations.EcoreReferenceInfo;
import org.hl7.fhir.Bundle;
import org.hl7.fhir.BundleEntry;
import org.hl7.fhir.ResourceContainer;
import org.hl7.fhir.ImmunizationRecommendation;
import org.hl7.fhir.ImmunizationRecommendationRecommendation;
import org.hl7.fhir.Parameters;
import org.hl7.fhir.ParametersParameter;
import org.hl7.fhir.dstu3.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 *
 * @author mccaffrey
 */
public class TestRunnerServiceFhirImpl implements TestRunnerService {

    private String adapterUrl = null;
    private boolean useAdapter = false;

    public TestRunnerServiceFhirImpl() {

    }

    public TestRunnerServiceFhirImpl(String adapterUrl) {
        this.setAdapterUrl(adapterUrl);
    }

    @Override
    public EngineResponse run(SoftwareConfig config, TestCasePayLoad tc) throws ConnectionException {

        EngineResponse response = new EngineResponse();
        if (config.getConnector().equals(FHIRAdapter.FHIR)) {
            this.setUseAdapter(false);
        } else {
            this.setUseAdapter(true);
        }
        List<ActualForecast> forecasts = new ArrayList<>();
        response.setForecasts(forecasts);
        List<ResponseVaccinationEvent> evaluatedEvents = new ArrayList<>();
        response.setEvents(evaluatedEvents);
        //response.setEvaluatedEvents(evaluatedEvents);
        ImmunizationRecommendationClient irc = new ImmunizationRecommendationClient();
        Routing routing = new Routing();
        routing.setFhirAdapterUrl(this.getAdapterUrl());
        routing.setForecastType(config.getConnector().toString());
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
        if (useAdapter) {
            Bundle result = null;
            try {
                result = (Bundle) irc.getImmunizationRecommendation(routing, sendingConfig, useAdapter);
            } catch (IOException ex) {
                Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (KeyStoreException ex) {
                Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (KeyManagementException ex) {
                Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

           // response.setRequest(ImmunizationRecommendationClient.generateXml(routing, sendingConfig, useAdapter));
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
        } else {

            org.hl7.fhir.dstu3.model.Parameters parameters = null;
            try {
                parameters = (org.hl7.fhir.dstu3.model.Parameters) irc.getImmunizationRecommendation(routing, sendingConfig, useAdapter);
            } catch (IOException ex) {
                Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (KeyStoreException ex) {
                Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (KeyManagementException ex) {
                Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            FhirContext ctx = FhirContext.forDstu3();
            String raw = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(parameters);
            response.setResponse(raw);
            //TODO: Error checking
            org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent parameter = parameters.getParameter().get(0);

            org.hl7.fhir.dstu3.model.ImmunizationRecommendation ir = (org.hl7.fhir.dstu3.model.ImmunizationRecommendation) parameter.getResource();
            List<ImmunizationRecommendationRecommendationComponent> irrs = ir.getRecommendation();
            Iterator<ImmunizationRecommendationRecommendationComponent> it = irrs.iterator();
            while (it.hasNext()) {
                ImmunizationRecommendationRecommendationComponent irr = it.next();
                ActualForecast af = TranslationUtils.translateImmunizationRecommendationRecommendationToActualForecast(irr);
                response.getForecasts().add(af);
            }

            List<Resource> containeds = ir.getContained();
            //TODO: Error checking
            Iterator<Resource> itRc = containeds.iterator();
            while (itRc.hasNext()) {

                Resource rc = itRc.next();
                if (rc instanceof org.hl7.fhir.dstu3.model.Immunization) {
                    org.hl7.fhir.dstu3.model.Immunization imm = (org.hl7.fhir.dstu3.model.Immunization) rc;
                    if (imm != null) {
                        ResponseVaccinationEvent rve = TranslationUtils.translateImmunizationToResponseVaccinationEvent(imm);
                        response.getEvents().add(rve);
                    }
                }

            }
            /*

            Parameters parameters = null;
            try {
                parameters = (Parameters) irc.getImmunizationRecommendation(routing, sendingConfig, useAdapter);
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
            response.setResponse(serial.it(parameters, "sut.xml"));
            // TODO: Error checking
            ParametersParameter parameter = parameters.getParameter().get(0);
            // TODO: Error checking
            ImmunizationRecommendation ir = parameter.getResource().getImmunizationRecommendation();
            EList<ImmunizationRecommendationRecommendation> irrs = ir.getRecommendation();
            Iterator<ImmunizationRecommendationRecommendation> it = irrs.iterator();
            while (it.hasNext()) {
                ImmunizationRecommendationRecommendation irr = it.next();
                //  if(TranslationUtils.doesRecommendationHaveDateCriterion(irr)) {
                ActualForecast af = TranslationUtils.translateImmunizationRecommendationRecommendationToActualForecast(irr);
                response.getForecasts().add(af);
//                } else {
                //                  ResponseVaccinationEvent rve = TranslationUtils.translateImmunizationRecommendationRecommendationToResponseVaccinationEvent(irr);
                //                response.getEvents().add(rve);
                //          }

            }
            EList<ResourceContainer> containeds = ir.getContained();
            //TODO: Error checking
            Iterator<ResourceContainer> itRc = containeds.iterator();
            while (itRc.hasNext()) {

                ResourceContainer rc = itRc.next();
                org.hl7.fhir.Immunization imm = rc.getImmunization();
                if (imm != null) {
                    ResponseVaccinationEvent rve = TranslationUtils.translateImmunizationToResponseVaccinationEvent(imm);
                    response.getEvents().add(rve);
                }

            }
             */
        }
        return response;
    }

    public static EngineResponse parseResponseFromXml(String xml) throws ConnectionException {
        EngineResponse response = new EngineResponse();

        response.setResponse(xml);
        List<ActualForecast> afs = new ArrayList<ActualForecast>();
        response.setForecasts(afs);
        List<ResponseVaccinationEvent> rves = new ArrayList<ResponseVaccinationEvent>();
        response.setEvents(rves);
        
        
        org.hl7.fhir.dstu3.model.Parameters parameters = null;
        FhirContext ctx = FhirContext.forDstu3();
        IBaseResource parsed = ctx.newXmlParser().parseResource(xml);

        if (!(parsed instanceof org.hl7.fhir.dstu3.model.Parameters)) {
            throw new ConnectionException(null, "Unexpected content found");
        }
        parameters = (org.hl7.fhir.dstu3.model.Parameters) parsed;
        
        String raw = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(parameters);
        response.setResponse(raw);
        //TODO: Error checking
        org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent parameter = parameters.getParameter().get(0);

        org.hl7.fhir.dstu3.model.ImmunizationRecommendation ir = (org.hl7.fhir.dstu3.model.ImmunizationRecommendation) parameter.getResource();
        List<ImmunizationRecommendationRecommendationComponent> irrs = ir.getRecommendation();
        Iterator<ImmunizationRecommendationRecommendationComponent> it = irrs.iterator();
        while (it.hasNext()) {
            ImmunizationRecommendationRecommendationComponent irr = it.next();
            ActualForecast af = TranslationUtils.translateImmunizationRecommendationRecommendationToActualForecast(irr);
            response.getForecasts().add(af);
        }

        List<Resource> containeds = ir.getContained();
        //TODO: Error checking
        Iterator<Resource> itRc = containeds.iterator();
        while (itRc.hasNext()) {

            Resource rc = itRc.next();
            if (rc instanceof org.hl7.fhir.dstu3.model.Immunization) {
                org.hl7.fhir.dstu3.model.Immunization imm = (org.hl7.fhir.dstu3.model.Immunization) rc;
                if (imm != null) {
                    ResponseVaccinationEvent rve = TranslationUtils.translateImmunizationToResponseVaccinationEvent(imm);
                    response.getEvents().add(rve);
                }
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

    public static void main(String[] args) throws IOException, ConnectionException {
        
        String fileString = new String(Files.readAllBytes(Paths.get("/home/mccaffrey/working/cdsiFhirResponse20170317.xml")), StandardCharsets.UTF_8);
        System.out.println("Contents (Java 7 with character encoding ) : " + fileString);
        
        
        
        
        
        EngineResponse response = TestRunnerServiceFhirImpl.parseResponseFromXml(fileString);
        
        System.out.println(response.getEvents().size());
        System.out.println(response.getForecasts().size());
        
        //   TestRunnerService test = new TestRunnerServiceFhirImpl("http://localhost:8080/forecast/ImmunizationRecommendations");
        //    TestRunnerService test = new TestRunnerServiceFhirImpl("https://p860556.campus.nist.gov:8443/forecast/ImmunizationRecommendations");
/*
        //TestRunnerService test = new TestRunnerServiceFhirImpl("http://localhost:8084/fhir/ImmunizationRecommendation");
        TestRunnerService test = new TestRunnerServiceFhirImpl("http://localhost:8084/fhir/Parameters/$IR");
        //TestRunnerService test = new TestRunnerServiceFhirImpl();
        SoftwareConfig config = new SoftwareConfig();
        TestCasePayLoad tc = new TestCasePayLoad();
        config.setConnector(FHIRAdapter.TCH);
        //config.setConnector(FHIRAdapter.FHIR);
        config.setUser("TCH");
        config.setEndPoint("http://tchforecasttester.orgg/fv/forecast");
        //config.setEndPoint("http://test-cdsi.rhcloud.com/CDSi/cds-forecast");

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
        dobCal.set(2009, 8, 9);
        Date dobDate = dobCal.getTime();

        tc.setEvaluationDate(evalDate);
        tc.setDateOfBirth(dobDate);

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
        /*
        Calendar immCal1 = Calendar.getInstance();
        immCal1.set(2009, 10, 9);
        Date immDate1 = immCal1.getTime();

        Calendar immCal2 = Calendar.getInstance();
        immCal2.set(2009, 12, 9);
        Date immDate2 = immCal2.getTime();

        Calendar immCal3 = Calendar.getInstance();
        immCal3.set(2010, 4, 9);
        Date immDate3 = immCal3.getTime();

        Calendar immCal4 = Calendar.getInstance();
        immCal4.set(2010, 10, 5);
        Date immDate4 = immCal4.getTime();

        VaccineRef vr1 = new VaccineRef();
        vr1.setCvx("110");
        tc.addImmunization(vr1, immDate1);
        /*
        VaccineRef vr2 = new VaccineRef();
        vr2.setCvx("110");
        tc.addImmunization(vr2, immDate2);

        VaccineRef vr3 = new VaccineRef();
        vr3.setCvx("110");
        tc.addImmunization(vr3, immDate3);

        VaccineRef vr4 = new VaccineRef();
        vr4.setCvx("110");
        tc.addImmunization(vr4, immDate4);
         */
        // http://tchforecasttester.org/fv/forecast?evalDate=20170101&evalSchedule=&resultFormat=text&patientDob=20160101&patientSex=F&vaccineDate1=20170101&vaccineCvx1=110
        //tc.setEvents(events);
        //tc.setImmunizations(events);
        /*
        EngineResponse run = null;
        try {
            run = test.run(config, tc);
        } catch (ConnectionException ex) {
            Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Exception\nStatus Code = " + ex.getStatusCode());
            System.out.println("Status Text = " + ex.getStatusText());
        }
        System.out.println(run.getForecasts().size());
        System.out.println(run.getEvents().size());
        //System.out.println(run.getResponse());
        */
    }

    /**
     * @return the useAdapter
     */
    public boolean isUseAdapter() {
        return useAdapter;
    }

    /**
     * @param useAdapter the useAdapter to set
     */
    public void setUseAdapter(boolean useAdapter) {
        this.useAdapter = useAdapter;
    }

}
