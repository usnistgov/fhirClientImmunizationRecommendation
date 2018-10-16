/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.fhir.client.ir.tch;

import gov.nist.fhir.TCHUtils;
import gov.nist.fhir.client.ir.Routing;
import gov.nist.fhir.client.ir.SendingConfig;
import gov.nist.healthcare.cds.domain.wrapper.ExecutionIssue;
import gov.nist.healthcare.cds.enumeration.IssueCategory;
import gov.nist.healthcare.cds.enumeration.IssueLevel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.tch.fc.model.ForecastEngineIssue;
import org.tch.fc.model.ForecastEngineIssueLevel;
import org.tch.fc.model.ForecastEngineIssueType;
import org.tch.fc.model.Service;
import org.tch.fc.model.Software;
import org.tch.fc.model.TestCase;

/**
 *
 * @author mccaffrey
 */
public class TchUtils {

    static Software createSoftware(Routing routing) {
        Software software = new Software();
        software.setServiceUrl(routing.getForecastUrl());
        Service service = Service.getService(routing.getForecastType());
        software.setService(service);
        return software;
    }

    static TestCase createTestCase(SendingConfig config) {
        
        TestCase testCase = new TestCase();
        /*
        try {
            ParametersParameter ppAssessment = findParametersParameter(
                    ForecastUtil.FORECAST_PARAMETERs.ASSESMENT_DATE.code, parameters);
            testCase.setEvalDate(FHIRUtil.convert(ppAssessment.getValueDate()));
            ParametersParameter ppBirthDate = findParametersParameter(ForecastUtil.FORECAST_PARAMETERs.BIRTH_DATE.code,
                    parameters);
            testCase.setPatientDob(FHIRUtil.convert(ppBirthDate.getValueDate()));
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        ParametersParameter ppGender = findParametersParameter(ForecastUtil.FORECAST_PARAMETERs.GENDER.code,
                parameters);
        testCase.setPatientSex(ppGender.getValueCode().getValue().substring(0, 1));

        Date date = new GregorianCalendar();
        
        testCase.setPatientDob(config.getBirthdate());
        
        List<TestEvent> events = createTestEvents(parameters);
        testCase.setTestEventList(events);
        */
        return testCase;
    }

    public static List<ExecutionIssue> convertStringToExecutionIssues(String string) {
        List<ForecastEngineIssue> issuesOrig = TCHUtils.convertStringToIssues(string);
        List<ExecutionIssue> issuesNew = new ArrayList<>();
        
        if (issuesOrig == null || issuesOrig.size() == 0) return issuesNew;
        Iterator<ForecastEngineIssue> it = issuesOrig.iterator();
        while(it.hasNext()) {
            ForecastEngineIssue issueOrig = it.next();
            ExecutionIssue issueNew = new ExecutionIssue();            
            switch (issueOrig.getIssueLevel()) {
                case ERROR:
                    issueNew.setLevel(IssueLevel.ERROR);
                    break;
                case WARNING:
                    issueNew.setLevel(IssueLevel.WARNING);
                    break;
                case INFORMATION:
                    issueNew.setLevel(IssueLevel.INFORMATIONAL);
                    break;
                default:
                    issueNew.setLevel(IssueLevel.INFORMATIONAL);
                    break;                    
            }
            /*
            IssueCategory.AUTHENTICATION;
            IssueCategory.AVAILABILITY;
            IssueCategory.ENGINE_FAILURE;
            IssueCategory.FORMAT;
            IssueCategory.TIMEOUT;
            
            ForecastEngineIssueType.AUTHENTICATION_FAILURE;
            ForecastEngineIssueType.ENGINE_NOT_AVAILABLE;
            ForecastEngineIssueType.MATCH_NOT_FOUND;
            ForecastEngineIssueType.UNEXPECTED_FORMAT;
              */    
            switch(issueOrig.getIssueType()) {
                
                case AUTHENTICATION_FAILURE:
                    issueNew.setCategory(IssueCategory.AUTHENTICATION);
                    break;
                case ENGINE_NOT_AVAILABLE:
                    issueNew.setCategory(IssueCategory.AVAILABILITY);
                    break;
                case MATCH_NOT_FOUND:
                    issueNew.setCategory(IssueCategory.MATCH_NOT_FOUND);
                    break;
                case UNEXPECTED_FORMAT:
                    issueNew.setCategory(IssueCategory.FORMAT);
                    break;
                default:
                    issueNew.setCategory(IssueCategory.AVAILABILITY);
                    break;
            }
                    
            issueNew.setMessage(issueOrig.getDescription());
            issuesNew.add(issueNew);
            
        }
       
        return issuesNew;
    }
   
}
