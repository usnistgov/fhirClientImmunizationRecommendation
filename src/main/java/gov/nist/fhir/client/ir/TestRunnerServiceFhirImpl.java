package gov.nist.fhir.client.ir;

import ca.uhn.fhir.context.FhirContext;
import gov.nist.fhir.client.ir.tch.TchUtils;
import gov.nist.healthcare.cds.domain.wrapper.ActualForecast;
import gov.nist.healthcare.cds.domain.SoftwareConfig;
import gov.nist.healthcare.cds.domain.exception.ConnectionException;
import gov.nist.healthcare.cds.domain.wrapper.EngineResponse;
import gov.nist.healthcare.cds.domain.wrapper.ExecutionIssue;
import gov.nist.healthcare.cds.domain.wrapper.ResponseVaccinationEvent;
import gov.nist.healthcare.cds.domain.wrapper.TestCasePayLoad;
import gov.nist.healthcare.cds.domain.wrapper.TestCasePayLoad.VaccinationEventPayLoad;
import gov.nist.healthcare.cds.domain.wrapper.VaccineRef;
import gov.nist.healthcare.cds.enumeration.FHIRAdapter;
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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hl7.fhir.dstu3.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.StringType;

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
        routing.setUserId(config.getUserId());
        routing.setFacilityId(config.getFacilityId());
        routing.setPassword(config.getPassword());

        SendingConfig sendingConfig = new SendingConfig();

        Date assessmentDate = tc.getEvaluationDate();
        Date dob = tc.getDateOfBirth();

        sendingConfig.setAssessmentDate(TranslationUtils.translateJavaDateToFhirDate(assessmentDate));
        sendingConfig.setBirthdate(TranslationUtils.translateJavaDateToFhirDate(dob));
        sendingConfig.setGender(tc.getGender().getDetails());
        sendingConfig.setTestCaseNumber(tc.getTestCaseNumber());

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
                imm.setManufactorer(event.getRef().getMvx());
                imms.add(imm);
            }
        }
        sendingConfig.setImmunizationData(imms);
        /*
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

           // response.setRequest(ImmunizationRecommendationClient.generatePayload(routing, sendingConfig, useAdapter));
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
         */

        if (useAdapter) {

            org.hl7.fhir.dstu3.model.Parameters parameters = null;
            try {
                parameters = (org.hl7.fhir.dstu3.model.Parameters) irc.getImmunizationRecommendation(routing, sendingConfig, useAdapter, FormatEnum.XML);
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

            //      System.out.println("COMING BACK = \n" + raw);
            //TODO: Error checking
            org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent parameter = null;
            try {
                parameter = parameters.getParameter().get(0);
            } catch (IndexOutOfBoundsException ioobe) {
                System.out.println("LOG: No immunization information in response.");
            }
            org.hl7.fhir.dstu3.model.ImmunizationRecommendation ir = null;
            if (parameter != null) {
                ir = (org.hl7.fhir.dstu3.model.ImmunizationRecommendation) parameter.getResource();
            }
            if (ir != null) {
                List<ImmunizationRecommendationRecommendationComponent> irrs = ir.getRecommendation();
                Iterator<ImmunizationRecommendationRecommendationComponent> it = irrs.iterator();
                while (it.hasNext()) {
                    ImmunizationRecommendationRecommendationComponent irr = it.next();
                    ActualForecast af = TranslationUtils.translateImmunizationRecommendationRecommendationToActualForecast(irr);
                    if (af != null) {
                        response.getForecasts().add(af);
                    }
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
                            if (rve != null) {
                                response.getEvents().add(rve);
                            }
                        }
                    }

                }
            } else {
                System.out.println("LOG: No immunization information in response.");
            }

            try {
                //TODO: Do better than going by order of paramters
                org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent parameterLog = parameters.getParameter().get(1);

                StringType log = (StringType) parameterLog.getValue();
                //System.out.println("This is the TCH log = " + log.getValue());
                if (log != null) {
                    response.setLogs(log.getValue());
                } else {
                    System.out.println("LOG: TCH Log does not exist.");
                }
            } catch (IndexOutOfBoundsException e) {

                System.out.println("LOG: TCH Log does not exist.");
            }

            try {
                //TODO: Do better than going by order of paramters
                org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent parameterSrs = parameters.getParameter().get(2);

                StringType srs = (StringType) parameterSrs.getValue();
                //System.out.println("This is the TCH log = " + log.getValue());
//            if (srs != null) {
//                response.setSoftwareResultStatus(srs.getValue());
//            } else {
//                System.out.println("LOG: Software Status Result does not exist.");
//            }
            } catch (IndexOutOfBoundsException e) {

                System.out.println("LOG:  Software Status Result does not exist.");
            }

            try {
                //TODO: Do better than going by order of paramters
                org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent parameterIssue = parameters.getParameter().get(3);

                StringType issues = (StringType) parameterIssue.getValue();
                if (issues != null) {
                    String issueString = issues.getValue();
                    List<ExecutionIssue> executionIssues = TchUtils.convertStringToExecutionIssues(issueString);

                    response.setIssues(executionIssues);
                } else {
                    response.setIssues(new ArrayList<>());
                }

            } catch (IndexOutOfBoundsException e) {
                System.out.println("LOG: No issues.");
            }
        } else {
            // Not using the adapter means to use the newest FHIR CDSi spec

            org.hl7.fhir.r4.model.Parameters parameters = null;
            org.hl7.fhir.r4.model.Bundle bundle = null;
            Object returned = null;
            try {
                //  org.hl7.fhir.r4.model.Bundle bundle = (org.hl7.fhir.r4.model.Bundle) irc.getImmunizationRecommendation(routing, sendingConfig, useAdapter, FormatEnum.XML);
                //    System.out.println("BUNDLE" + bundle.toString());

                //               FhirContext ctx = FhirContext.forR4();
                // String raw = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle);
                //System.out.println("raw = " + raw);
                returned = irc.getImmunizationRecommendation(routing, sendingConfig, useAdapter, FormatEnum.XML);
                parameters = (org.hl7.fhir.r4.model.Parameters) returned;
            } catch (IOException ex) {
                Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (KeyStoreException ex) {
                Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (KeyManagementException ex) {
                Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassCastException ex) {
                Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
                bundle = (org.hl7.fhir.r4.model.Bundle) returned;
// TODO: If there's a class cast exception find a better way of returning the HTTP body
            }

            if (parameters == null) {
                FhirContext ctx = FhirContext.forR4();
                String raw = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle);
                response.setResponse(raw);

                return response;
            }

            FhirContext ctx = FhirContext.forR4();
            String raw = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(parameters);
            response.setResponse(raw);

            //      System.out.println("COMING BACK = \n" + raw);
            //TODO: Error checking
            org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent parameter = null;
            try {
                parameter = parameters.getParameter().get(0);
            } catch (IndexOutOfBoundsException ioobe) {
                System.out.println("LOG: No immunization information in response.");
            }

            List<org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent> parametersComp = parameters.getParameter();

            Iterator<org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent> it = parametersComp.iterator();
            while (it.hasNext()) {

                org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent paramComp = it.next();
                org.hl7.fhir.r4.model.Resource resource = paramComp.getResource();

                if (resource instanceof org.hl7.fhir.r4.model.ImmunizationEvaluation) {
                    org.hl7.fhir.r4.model.ImmunizationEvaluation ie = (org.hl7.fhir.r4.model.ImmunizationEvaluation) resource;
                    ResponseVaccinationEvent rve = TranslationUtils.translateImmunizationEvaluationToResponseVaccinationEventCurrentFhir(ie);

                    response.getEvents().add(rve);
                 //   System.out.println("PARSED!!!" + rve.getAdministred() + " --- " + rve.getDoseNumber() + rve.getEvaluations().toArray()[0].toString());
                } else if (resource instanceof org.hl7.fhir.r4.model.ImmunizationRecommendation) {
                    org.hl7.fhir.r4.model.ImmunizationRecommendation ir = (org.hl7.fhir.r4.model.ImmunizationRecommendation) resource;
                    
                    List<org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent> recComp = ir.getRecommendation();
                    Iterator<org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent> itIr = recComp.iterator();
                    
                    while(itIr.hasNext()) {
                        org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent irrc = itIr.next();
                        ActualForecast af = TranslationUtils.translateImmunizationRecommendationRecommendationToActualForecastCurrentFhir(irrc);
                        if (af != null) {
                            response.getForecasts().add(af);
                        //    System.out.println("AF RESPONCE " + af.getSerieStatus() + " " + af.getEarliest());
                        }
                        
                    }
                    
                }
            }

            // END HERE
            /*
            
            org.hl7.fhir.r4.model.ImmunizationEvaluation ie = null;
            if (parameter != null && ie instanceof org.hl7.fhir.r4.model.ImmunizationEvaluation) {
                ie = (org.hl7.fhir.r4.model.ImmunizationEvaluation) parameter.getResource();
            }
            if (ie != null) {
                translateImmunizationEvaluationToResponseVaccinationEventCurrentFhir(ie);
                List<org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent> irrs = ie.getRecommendation();
                Iterator<org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent> it = irrs.iterator();
                while (it.hasNext()) {
                    org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent irr = it.next();
                    ActualForecast af = TranslationUtils.translateImmunizationRecommendationRecommendationToActualForecastCurrentFhir(irr);
                    if (af != null) {
                        response.getForecasts().add(af);
                    }
                }

                List<org.hl7.fhir.r4.model.Resource> containeds = ir.getContained();
                //TODO: Error checking
                Iterator<org.hl7.fhir.r4.model.Resource> itRc = containeds.iterator();
                while (itRc.hasNext()) {

                    org.hl7.fhir.r4.model.Resource rc = itRc.next();
                    if (rc instanceof org.hl7.fhir.r4.model.ImmunizationEvaluation) {
                        org.hl7.fhir.r4.model.ImmunizationEvaluation imm = (org.hl7.fhir.r4.model.ImmunizationEvaluation) rc;
                        if (imm != null) {
                            ResponseVaccinationEvent rve = TranslationUtils.translateImmunizationEvaluationToResponseVaccinationEventCurrentFhir(imm);
                            if (rve != null) {
                                response.getEvents().add(rve);
                            }
                        }
                    }

                }
            } else {
                System.out.println("LOG: No immunization information in response.");
            }
            
            try {
                //TODO: Do better than going by order of paramters
                org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent parameterLog = parameters.getParameter().get(1);

                org.hl7.fhir.r4.model.StringType log = (org.hl7.fhir.r4.model.StringType) parameterLog.getValue();
                //System.out.println("This is the TCH log = " + log.getValue());
                if (log != null) {
                    response.setLogs(log.getValue());
                } else {
                    System.out.println("LOG: TCH Log does not exist.");
                }
            } catch (IndexOutOfBoundsException e) {

                System.out.println("LOG: TCH Log does not exist.");
            }

            try {
                //TODO: Do better than going by order of paramters
                org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent parameterSrs = parameters.getParameter().get(2);

                org.hl7.fhir.r4.model.StringType srs = (org.hl7.fhir.r4.model.StringType) parameterSrs.getValue();
                //System.out.println("This is the TCH log = " + log.getValue());
//            if (srs != null) {
//                response.setSoftwareResultStatus(srs.getValue());
//            } else {
//                System.out.println("LOG: Software Status Result does not exist.");
//            }
            } catch (IndexOutOfBoundsException e) {

                System.out.println("LOG:  Software Status Result does not exist.");
            }

            try {
                //TODO: Do better than going by order of paramters
                org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent parameterIssue = parameters.getParameter().get(3);

                org.hl7.fhir.r4.model.StringType issues = (org.hl7.fhir.r4.model.StringType) parameterIssue.getValue();
                if (issues != null) {
                    String issueString = issues.getValue();
                    List<ExecutionIssue> executionIssues = TchUtils.convertStringToExecutionIssues(issueString);

                    response.setIssues(executionIssues);
                } else {
                    response.setIssues(new ArrayList<>());
                }

            } catch (IndexOutOfBoundsException e) {
                System.out.println("LOG: No issues.");
            }
             */
        }
        /*
         try {
             //TODO: Do better than going by order of paramters
             org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent parameterIssue = parameters.getParameter().get(2);
             
             StringType issues = (StringType) parameterIssue.getValue();
             //System.out.println("This is the TCH log = " + log.getValue());  
             if(issues != null) {
                 String issueString = issues.getValue();
                 List<ExecutionIssue> executionIssues = TchUtils.convertStringToExecutionIssues(issueString);
                 
                 response.setIssues(executionIssues);
             } else {
                 response.setIssues(new ArrayList<>());
             }
 //            response.setLogs(log.getValue());
   //          response.s
         } catch (IndexOutOfBoundsException e)  {
             //TODO: If no log... Make this better
             IndexOutOfBoundsException
         }
         */
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
        // }
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

    public static void main(String[] args) throws IOException, ConnectionException {

        //   TestRunnerService test = new TestRunnerServiceFhirImpl("http://localhost:8080/forecast/ImmunizationRecommendations");
        //    TestRunnerService test = new TestRunnerServiceFhirImpl("https://p860556.campus.nist.gov:8443/forecast/ImmunizationRecommendations");
        //TestRunnerService test = new TestRunnerServiceFhirImpl("http://localhost:8084/fhir/ImmunizationRecommendation");
//        TestRunnerService test = new TestRunnerServiceFhirImpl("http://localhost:8084/fhir/Parameters/$IR");
        //  TestRunnerService test = new TestRunnerServiceFhirImpl("http://p860556.campus.nist.gov:8084/fhirAdapter/fhir/Parameters/$IR");
        //         TestRunnerService test = new TestRunnerServiceFhirImpl("https://p860556.campus.nist.gov:9443/fhirAdapter/fhir/Parameters/$cds-forecast");
        //TestRunnerService test = new TestRunnerServiceFhirImpl("http://localhost:8084/fhirAdapter/fhir/Parameters/$cds-forecast");
        //  TestRunnerService test = new TestRunnerServiceFhirImpl("http://localhost:9080/fhirAdapter/fhir/Parameters/$cds-forecast");
        //TestRunnerService test = new TestRunnerServiceFhirImpl("https://hit-dev.nist.gov:11080/fhirAdapter/fhir/Parameters/$cds-forecast");
        //TestRunnerService test = new TestRunnerServiceFhirImpl("https://hit-dev.nist.gov:11080/fhirAdapter/fhir/Parameters/$cds-forecast");
        //       TestRunnerService test = new TestRunnerServiceFhirImpl("http://129.6.18.21:15000/fhirAdapter/fhir/Parameters/$cds-forecast");
        TestRunnerService test = new TestRunnerServiceFhirImpl("https://hit-dev.nist.gov:15000/fhirAdapter/fhir/Parameters/$cds-forecast");
        //   TestRunnerService test = new TestRunnerServiceFhirImpl("http://localhost:8080/fhirAdapter/fhir/Parameters/$cds-forecast");

        //  TestRunnerService test = new TestRunnerServiceFhirImpl("http://localhost:9080/fhirAdapter/fhir/Parameters/$cds-forecast");
        // TestRunnerService test = new TestRunnerServiceFhirImpl("http://localhost:8084/fhirAdapter/fhir/Parameters/$cds-forecast");
//TestRunnerService test = new TestRunnerServiceFhirImpl();
        SoftwareConfig config = new SoftwareConfig();
        TestCasePayLoad tc = new TestCasePayLoad();
        //    config.setConnector(FHIRAdapter.MA);      
        //  config.setConnector(FHIRAdapter.SWP);      
        //   config.setConnector(FHIRAdapter.LSVF);

        // config.setConnector(FHIRAdapter.SWP);
        //    config.setConnector(FHIRAdapter.HL7);
        //  config.setConnector(FHIRAdapter.ICE);
        //config.setConnector(FHIRAdapter.STC);
        config.setConnector(FHIRAdapter.FHIR);
        //      config.setUser("TCH");
        //config.setUser("ice");
        //config.setUser("stc");
        //    config.setEndPoint("http://testws.swpartners.com/vfmservice/VFMWebService?wsdl");
        //config.setEndPoint("http://testws.swpartners.com/vfmservice/VFMWebService");
        //               config.setEndPoint("http://tchforecasttester.org/fv/forecast");
        // config.setEndPoint("http://florence.immregistries.org/aart/soap");
        //  config.setEndPoint("http://florence.immregistries.org/iis-sandbox/soap");

        config.setEndPoint("http://florence.immregistries.org/lonestar/fhir/$immds-forecast");

        //    config.setEndPoint("https://app.immregistries.org/aart/soap");
        //   config.setEndPoint("http://immlab.pagekite.me/aart/soap");
        //config.setEndPoint("http://testws.swpartners.com/mdsservice/mds");
        //  config.setEndPoint("http://imm.pagekite.me/aart/soap");
        //    config.setEndPoint("http://imm.pagekite.me/iis-kernel/soap");
        //      config.setEndPoint("https://cds.hln.com/opencds-decision-support-service/evaluate?wsdl");
        //config.setEndPoint("http://epicenter.stchome.com/safdemo/soa/forecast/getForecast.wsdl");
        //      config.setEndPoint("http://test-cdsi.rhcloud.com/CDSi/cds-forecast");
//config.setEndPoint("http://69.64.70.10:8080/vfmservice/VFMWebService");
//config.setEndPoint("http://immlab.pagekite.me/opencds-decision-support-service/evaluate?wsdl");
        config.setUserId("FITS");
        config.setFacilityId("FITS");
        config.setPassword("password1234");
        //Patient patient = new Patient();
        //Date dob = new FixedDate("01/01/2016");
        //patient.setDob(dob);
        //patient.setGender(Gender.F);
        //tc.setPatient(patient);        
        tc.setGender(Gender.F);
        tc.setTestCaseNumber("123");
        Calendar evalCal = Calendar.getInstance();
        evalCal.set(2019, 5, 13);
        Date evalDate = evalCal.getTime();

        Calendar dobCal = Calendar.getInstance();
        dobCal.set(2007, 9, 2);
        Date dobDate = dobCal.getTime();

        tc.setEvaluationDate(evalDate);
        tc.setDateOfBirth(dobDate);

        //List<VaccinationEventPayLoad> vaccinationEvents = new ArrayList<VaccinationEventPayLoad>();
        Calendar immCal1 = Calendar.getInstance();
        immCal1.set(2007, 11, 2);
        Date immDate1 = immCal1.getTime();

        Calendar immCal2 = Calendar.getInstance();
        immCal2.set(2008, 1, 2);
        Date immDate2 = immCal2.getTime();

        Calendar immCal3 = Calendar.getInstance();
        immCal3.set(2008, 3, 2);
        Date immDate3 = immCal3.getTime();

        Calendar immCal4 = Calendar.getInstance();
        immCal4.set(2008, 12, 2);
        Date immDate4 = immCal4.getTime();

        Calendar immCal5 = Calendar.getInstance();
        immCal5.set(2013, 2, 5);
        Date immDate5 = immCal5.getTime();

        Calendar immCal6 = Calendar.getInstance();
        immCal6.set(2019, 5, 13);
        Date immDate6 = immCal1.getTime();

        /*
        Calendar immCal2 = Calendar.getInstance();
        immCal2.set(2009, 12, 9);
        Date immDate2 = immCal2.getTime();
        
        Calendar immCal3 = Calendar.getInstance();
        immCal3.set(2010, 4, 9);
        Date immDate3 = immCal3.getTime();

        Calendar immCal4 = Calendar.getInstance();
        immCal4.set(2010, 10, 5);
        Date immDate4 = immCal4.getTime();
         */
        VaccineRef vr1 = new VaccineRef();
        vr1.setCvx("107");
        tc.addImmunization(vr1, immDate1);

        VaccineRef vr2 = new VaccineRef();
        vr2.setCvx("107");
        tc.addImmunization(vr2, immDate2);
        VaccineRef vr3 = new VaccineRef();
        vr3.setCvx("107");
        tc.addImmunization(vr3, immDate3);
        VaccineRef vr4 = new VaccineRef();
        vr4.setCvx("107");
        tc.addImmunization(vr4, immDate4);
        VaccineRef vr5 = new VaccineRef();
        vr5.setCvx("107");
        tc.addImmunization(vr5, immDate5);

        VaccineRef vr6 = new VaccineRef();
        vr6.setCvx("09");

        tc.addImmunization(vr6, immDate6);

        /*
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
        EngineResponse run = null;
        try {
            run = test.run(config, tc);
        } catch (ConnectionException ex) {
            Logger.getLogger(TestRunnerServiceFhirImpl.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Exception\nStatus Code = " + ex.getStatusCode());
            System.out.println("Status Text = " + ex.getStatusText());
        }

        System.out.println(run.getResponse());

        System.out.println(run.getForecasts().size());
        System.out.println(run.getEvents().size());
        //System.out.println(run.getResponse());

        System.out.println("First CVX = " + run.getForecasts().get(0).getVaccine().getCvx());

        System.out.println("These are the logs of... " + run.getLogs());

        System.out.println("issues length = " + run.getIssues().size());
//        System.out.println("Status = " + run.getSoftwareResultStatus());

    }

}
