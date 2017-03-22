/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.fhir.client.ir.tch;

import fhir.util.FHIRUtil;
import forecast.util.ForecastUtil;
import gov.nist.fhir.client.ir.Routing;
import gov.nist.fhir.client.ir.SendingConfig;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.hl7.fhir.Parameters;
import org.hl7.fhir.ParametersParameter;
import org.tch.fc.model.Service;
import org.tch.fc.model.Software;
import org.tch.fc.model.TestCase;
import org.tch.fc.model.TestEvent;

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

}
