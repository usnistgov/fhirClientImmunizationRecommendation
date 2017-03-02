/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.fhir.client.ir;

import fhir.util.DeSerialize;
import fhir.util.FHIRUtil;
import fhir.util.Serialize;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import org.eclipse.emf.ecore.EObject;

import org.hl7.fhir.Bundle;
import org.hl7.fhir.Code;
import org.hl7.fhir.CodeableConcept;
import org.hl7.fhir.Coding;
import org.hl7.fhir.Date;
import org.hl7.fhir.DateTime;
import org.hl7.fhir.FhirFactory;
import org.hl7.fhir.Id;
import org.hl7.fhir.Parameters;
import org.hl7.fhir.ParametersParameter;
import org.hl7.fhir.ResourceContainer;
import org.hl7.fhir.impl.BundleImpl;

/**
 *
 * @author mccaffrey
 */
public class ImmunizationRecommendationClient {

    public static final String PARAMETER_NAME_GENDER = "gender";
    public static final String PARAMETER_NAME_BIRTH_DATE = "birthDate";
    public static final String PARAMETER_NAME_SERVICE_TYPE = "serviceType";
    public static final String PARAMETER_NAME_SERVICE_URL = "serviceURL";
    public static final String PARAMETER_NAME_ASSESSMENT_DATE = "assessmentDate";
    public static final String PARAMETER_NAME_IMMUNIZATIONS = "immunizations";

    //TODO: Change from strings to objects
    private static String generateXml(Routing routing, SendingConfig sendingConfig) {

        Parameters parameters = FhirFactory.eINSTANCE.createParameters();
        Id id = FhirFactory.eINSTANCE.createId();
        id.setValue(UUID.randomUUID().toString());
        parameters.setId(id);
        ParametersParameter genderParameter = FhirFactory.eINSTANCE.createParametersParameter();
        genderParameter.setName(FHIRUtil.convert(PARAMETER_NAME_GENDER));
        Code genderValue = FhirFactory.eINSTANCE.createCode();
        genderValue.setValue(sendingConfig.getGender());
        genderParameter.setValueCode(genderValue);
        parameters.getParameter().add(genderParameter);

        ParametersParameter dobParameter = FhirFactory.eINSTANCE.createParametersParameter();
        dobParameter.setName(FHIRUtil.convert(PARAMETER_NAME_BIRTH_DATE));
        Date dobValue = FhirFactory.eINSTANCE.createDate();
        dobValue.setValue(FHIRUtil.convert2XMLCalendar(sendingConfig.getBirthdate()));
        dobParameter.setValueDate(dobValue);
        parameters.getParameter().add(dobParameter);

        ParametersParameter serviceTypeParameter = FhirFactory.eINSTANCE.createParametersParameter();
        serviceTypeParameter.setName(FHIRUtil.convert(PARAMETER_NAME_SERVICE_TYPE));
        org.hl7.fhir.String serviceTypeString = FhirFactory.eINSTANCE.createString();
        serviceTypeString.setValue(routing.getForecastType());
        serviceTypeParameter.setValueString(serviceTypeString);
        parameters.getParameter().add(serviceTypeParameter);

        ParametersParameter serviceUrlParameter = FhirFactory.eINSTANCE.createParametersParameter();
        serviceUrlParameter.setName(FHIRUtil.convert(PARAMETER_NAME_SERVICE_URL));
        org.hl7.fhir.String serviceUrlString = FhirFactory.eINSTANCE.createString();
        serviceUrlString.setValue(routing.getForecastUrl());
        serviceUrlParameter.setValueString(serviceUrlString);
        parameters.getParameter().add(serviceUrlParameter);

        ParametersParameter assessmentDateParameter = FhirFactory.eINSTANCE.createParametersParameter();
        assessmentDateParameter.setName(FHIRUtil.convert(PARAMETER_NAME_ASSESSMENT_DATE));
        Date assessmentDateValue = FhirFactory.eINSTANCE.createDate();
        assessmentDateValue.setValue(FHIRUtil.convert2XMLCalendar(sendingConfig.getAssessmentDate()));
        assessmentDateParameter.setValueDate(assessmentDateValue);
        parameters.getParameter().add(assessmentDateParameter);

        Collection<Immunization> immunizations = sendingConfig.getImmunizationData();
        if (immunizations != null) {
            ParametersParameter immunizationsParameter = FhirFactory.eINSTANCE.createParametersParameter();
            immunizationsParameter.setName(FHIRUtil.convert(PARAMETER_NAME_IMMUNIZATIONS));
            Iterator<Immunization> iterator = immunizations.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                Immunization immunization = iterator.next();

                org.hl7.fhir.Immunization fhirImmunization = FhirFactory.eINSTANCE.createImmunization();
                Id immunizationId = FhirFactory.eINSTANCE.createId();
                immunizationId.setValue(UUID.randomUUID().toString());
                fhirImmunization.setId(immunizationId);
                DateTime immunizationDate = FhirFactory.eINSTANCE.createDateTime();
                immunizationDate.setValue(FHIRUtil.convert2XMLCalendar(immunization.getDate()));
                fhirImmunization.setDate(immunizationDate);
                //Narrative codeText = FhirFactory.eINSTANCE.createNarrative();
                //NarrativeStatus codeTextStatus = FhirFactory.eINSTANCE.createNarrativeStatus();                
                //codeTextStatus.setValue(NarrativeStatusList.EMPTY.);
                //codeText.setStatus();
                CodeableConcept immCC = FhirFactory.eINSTANCE.createCodeableConcept();
                immCC.setText(FHIRUtil.convert(immunization.getVaccineCode()));
                Coding immCoding = FhirFactory.eINSTANCE.createCoding();
                Code immCode = FhirFactory.eINSTANCE.createCode();                
                immCode.setValue(immunization.getVaccineCode());
                immCoding.setCode(immCode);
                immCC.getCoding().add(immCoding);
                fhirImmunization.setVaccineCode(immCC);
                
                ParametersParameter currentParam = FhirFactory.eINSTANCE.createParametersParameter();                                
                ResourceContainer rc = FhirFactory.eINSTANCE.createResourceContainer();
                rc.setImmunization(fhirImmunization);
                
                currentParam.setResource(rc);
                immunizationsParameter.getPart().add(currentParam);
                i++;
            }
            parameters.getParameter().add(immunizationsParameter);
        }
        Serialize seri = new Serialize();
        String xml = seri.it(parameters, "sut.xml");
        //System.out.println("GENERATED OBJECT HERE ----->\n" + xml);

        StringBuilder parameterXml = new StringBuilder();
        parameterXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Parameters xmlns=\"http://hl7.org/fhir\">");
        parameterXml.append("<id value=\"" + UUID.randomUUID().toString() + "\"/>");
        parameterXml.append("<parameter>");
        parameterXml.append("<name value=\"gender\"/>");
        parameterXml.append("<valueCode value=\"" + sendingConfig.getGender() + "\"/>");
        parameterXml.append("</parameter> ");
        parameterXml.append("<parameter>");
        parameterXml.append("<name value=\"birthDate\"/>");
        parameterXml.append("<valueDate value=\"" + sendingConfig.getBirthdate() + "\"/>");
        parameterXml.append("</parameter> ");
        parameterXml.append("<parameter>");
        parameterXml.append("<name value=\"serviceType\"/>");
        parameterXml.append("<valueString value=\"" + routing.getForecastType() + "\"/>");
        parameterXml.append("</parameter> ");
        parameterXml.append("<parameter>");
        parameterXml.append("<name value=\"serviceURL\"/>");
        parameterXml.append("<valueString value=\"" + routing.getForecastUrl() + "\"/>");
        parameterXml.append("</parameter> ");
        parameterXml.append("<parameter>");
        parameterXml.append("<name value=\"assessmentDate\"/>");
        parameterXml.append("<valueDate value=\"" + sendingConfig.getAssessmentDate() + "\"/>");
        //    + "<valueDate value=\"2016-12-01\"/>"
        parameterXml.append("</parameter> ");
        Collection<Immunization> imms = sendingConfig.getImmunizationData();
        if (imms != null) {
            Iterator<Immunization> it = imms.iterator();
            parameterXml.append("<parameter>");
            parameterXml.append("<name value=\"immunizations\" />");
            while (it.hasNext()) {
                Immunization imm = it.next();
                parameterXml.append("<part><name/>");
                parameterXml.append("<resource>");
                parameterXml.append("<immunization>");
                parameterXml.append("<id value=\"" + UUID.randomUUID().toString() + "\"/>");
                parameterXml.append("<date value=\"" + imm.getDate() + "\"/>");
                parameterXml.append("  <vaccineCode>");
                parameterXml.append("<coding>");
                parameterXml.append("<code value=\"" + imm.getVaccineCode() + "\"/>");
                parameterXml.append("</coding>");
                parameterXml.append("<text value=\"" + imm.getVaccineCode() + "\"/>");
                parameterXml.append("</vaccineCode>");
                parameterXml.append(" </immunization>");
                parameterXml.append("</resource>");
                parameterXml.append("</part>");
            }
            parameterXml.append("</parameter> ");
        }

        parameterXml.append("</Parameters>");

        //System.out.println("OTHER XML HERE!" + parameterXml.toString());
        
        /*
                 This is what the schema says we should have produced...
        Update: schema was wrong(!)
                    + "<Immunization>"
                    + "<status value=\"completed\"/>"
                    + " <vaccineCode><coding><code value=\"143\"/></coding></vaccineCode> "
                    + "<patient><reference value=\"123\"/></patient>"
                    + "<wasNotGiven value=\"true\"/>"
                    + "<reported value=\"true\"/>"
                
                    + "</Immunization>"
         */
        //return parameterXml.toString();
        return xml;
    }

    private static Response sendImmunizationInformation(Routing routing, SendingConfig sendingConfig) throws UnsupportedEncodingException, IOException {

        Response response = new Response();
        HttpPost request = new HttpPost(routing.getFhirAdapterUrl());
        StringEntity paramsXml = new StringEntity(ImmunizationRecommendationClient.generateXml(routing, sendingConfig));
        //System.out.println(convertStreamToString(paramsXml.getContent()));
        request.addHeader("content-type", "application/xml; charset=utf8");
        request.addHeader("accept", "application/xml");
        request.setEntity(paramsXml);

        HttpClient httpClient = HttpClientBuilder.create().build();             

        HttpResponse httpResponse = httpClient.execute(request);
        response.setHttpCode(String.valueOf(httpResponse.getStatusLine().getStatusCode()));

        //TODO: Improve this.
        String body = convertStreamToString(httpResponse.getEntity().getContent());
        String xml = body.substring(body.indexOf("<"));
        response.setPayload(xml);

        return response;
    }

    /*
public static EObject loadEObjectFromString(String myModelXml, EPackage ePackage) throws IOException { 
    // Create a ResourceSet
    ResourceSet resourceSet = new ResourceSetImpl();
    // register XMIRegistryResourceFactoryIml
    resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put
    (Resource.Factory.Registry.DEFAULT_EXTENSION, 
     new XMIResourceFactoryImpl());
     // register your epackage to the resource set so it has a reference to your ecore
     // you can get an instance to your epackage by calling YourEPackageClass.getInstace();
    resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
    Resource resource = resourceSet.createResource(URI.createURI("*.modelextension"));
    resource.load(new URIConverter.ReadableInputStream(myModelXml), null);
    // return the root model object and there you have it, all you need is to
    // cast it to the right EObject based on your model 
    return resource.getContents().get(0);
}    
     */
    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public Bundle getImmunizationRecommendation(Routing routing, SendingConfig sendingConfig) throws IOException {

        //EngineResponse engineResponse = new EngineResponse();
        //engineResponse.setForecasts(new ArrayList<ActualForecast>());
        //engineResponse.setEvaluatedEvents(new ArrayList<ResponseVaccinationEvent>());
//        Collection<ImmunizationRecommendation> recommendations = new ArrayList<ImmunizationRecommendation>();
        //      Collection<Immunization> immunizations = new ArrayList<Immunization>();
        Response response = ImmunizationRecommendationClient.sendImmunizationInformation(routing, sendingConfig);

        String xml = response.getPayload();
        String filename = UUID.randomUUID().toString();
        File tempFile = new File(filename + ".xml");
        PrintWriter writer = new PrintWriter(tempFile);
        writer.write(xml);
        writer.flush();
        writer.close();
        URL url = tempFile.toURI().toURL();
        //    resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new XMLResourceFactoryImpl());
        String path = url.toString();
        if (path.startsWith("file:")) {
            path = path.substring(5);
        }
        DeSerialize deserial = new DeSerialize();
        EObject s1 = deserial.it(url);

        //System.out.println(s1.toString() + "!!!" + s1.eResource());
        BundleImpl bundle = (BundleImpl) s1;
        //System.out.println(xml);
        /*
        EList<BundleEntry> entries = bundle.getEntry();

        for (int i = 0; i < entries.size(); i++) {
            System.out.println("entry #" + i);
            BundleEntry entry = entries.get(i);
            ResourceContainer container = entry.getResource();
            org.hl7.fhir.ImmunizationRecommendation recommend = container.getImmunizationRecommendation();
            if (recommend != null) {
                EList<ImmunizationRecommendationRecommendation> recommend2 = recommend.getRecommendation();

                for (int j = 0; j < recommend2.size(); j++) {
                    ImmunizationRecommendationRecommendation irr = recommend2.get(j);
                    System.out.println("Recommendation #" + j);
                    if (irr.getDate() != null && irr.getDate().getValue() != null) {
                        System.out.println("Date = " + irr.getDate().getValue().toString());
                    } else {
                        System.out.println("Date = null");
                    }
                    if (irr.getDoseNumber() != null && irr.getDoseNumber().getValue() != null) {
                        System.out.println("DoseNumber = " + irr.getDoseNumber().getValue().toString());
                    } else {
                        System.out.println("DoseNumber = null");
                    }
                    //System.out.println("Forecast Status = " + irr.getForecastStatus().getText());
                    //System.out.println("Protocol = " + irr.getProtocol().toString());
                    System.out.println("Vaccine Code = " + irr.getVaccineCode().getText().getValue());
                    System.out.println("");
                    System.out.println("");
                    System.out.println("");
                    System.out.println("");
                    System.out.println("");
                    ImmunizationRecommendation ir = new ImmunizationRecommendation();
                    if (irr.getDate() != null && irr.getDate().getValue() != null) {
                        ir.setDate(irr.getDate().getValue().toString());
                    }
                    if (irr.getDoseNumber() != null && irr.getDoseNumber().getValue() != null) {
                        ir.setDose(irr.getDoseNumber().getValue().toString());
                    }
                    ir.setVaccineCode(irr.getVaccineCode().getText().getValue());
                    recommendations.add(ir);
                }
            } else {
                org.hl7.fhir.Immunization imm = container.getImmunization();
                if(imm != null) {
                    
                    Immunization im = new Immunization();
                    im.setDate(FHIRUtil.convert(imm.getDate()).toString());
                   // im.set
                    
                }
            }
        }
         */
        tempFile.delete();
        //return recommendations;
        return bundle;
        //return engineResponse;

    }

    public static void main(String[] args) throws IOException {

        ImmunizationRecommendationClient irc = new ImmunizationRecommendationClient();
        Routing routing = new Routing();
        routing.setFhirAdapterUrl("http://localhost:8080/forecast/ImmunizationRecommendations");
        routing.setForecastType("TCH");
        routing.setForecastUrl("http://tchforecasttester.org/fv/forecast");

        SendingConfig sendingConfig = new SendingConfig();
        //sendingConfig.setAssessmentDate("2016-12-01T10:57:34+01:00");
        //sendingConfig.setBirthdate("2016-11-01T10:57:34+01:00");

        sendingConfig.setAssessmentDate("2016-12-01");
        sendingConfig.setBirthdate("2015-11-01");
        sendingConfig.setGender("female");

        Collection<Immunization> imms = new ArrayList<Immunization>();
        /*
        Immunization imm = new Immunization();
        imm.setDate("2017-01-01");
        imm.setVaccineCode("123");
        imms.add(imm);
         */
        Immunization imm2 = new Immunization();
        imm2.setDate("2016-01-02");
        imm2.setVaccineCode("110");
        imms.add(imm2);

        sendingConfig.setImmunizationData(imms);

        irc.getImmunizationRecommendation(routing, sendingConfig);

    }

}
