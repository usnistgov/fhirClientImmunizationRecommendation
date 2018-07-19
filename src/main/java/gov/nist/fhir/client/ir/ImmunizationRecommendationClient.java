/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.fhir.client.ir;

import ca.uhn.fhir.context.FhirContext;
import gov.nist.fhir.Consts;
import gov.nist.healthcare.cds.domain.exception.ConnectionException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import org.hl7.fhir.dstu3.model.DateType;
import org.hl7.fhir.dstu3.model.Enumerations;

/**
 *
 * @author mccaffrey
 */
public class ImmunizationRecommendationClient {

    /*
    public static final String PARAMETER_NAME_GENDER = "gender";
    public static final String Consts.PARAMETER_NAME_BIRTH_DATE = "birthDate";
    public static final String Consts.PARAMETER_NAME_SERVICE_TYPE = "serviceType";
    public static final String Consts.PARAMETER_NAME_SERVICE_URL = "serviceURL";
    public static final String Consts.PARAMETER_NAME_ASSESSMENT_DATE = "assessmentDate";
    public static final String Consts.PARAMETER_NAME_IMMUNIZATION = "Immunization";
    public static final String Consts.PARAMETER_NAME_IMMUNIZATION_ADAPTER = "Immunizations";
    public static final String Consts.PARAMETER_NAME_PATIENT = "patient";
     */
    public static String generatePayload(Routing routing, SendingConfig sendingConfig, boolean useAdapter, FormatEnum format) {

        // Parameters parameters = FhirFactory.eINSTANCE.createParameters();
        //Id id = FhirFactory.eINSTANCE.createId();
        //id.setValue(UUID.randomUUID().toString());
        //parameters.setId(id);
        //Date dobValue = FhirFactory.eINSTANCE.createDate();
        //dobValue.setValue(FHIRUtil.convert2XMLCalendar(sendingConfig.getBirthdate()));
        String patientId = UUID.randomUUID().toString();
        //patientId.setValue(UUID.randomUUID().toString());

        FhirContext ctx = FhirContext.forDstu3();
        org.hl7.fhir.dstu3.model.Parameters parametersFhir = new org.hl7.fhir.dstu3.model.Parameters();
        /*
        if (useAdapter) {
            ParametersParameter genderParameter = FhirFactory.eINSTANCE.createParametersParameter();
            genderParameter.setName(FHIRUtil.convert(Consts.PARAMETER_NAME_GENDER));
            Code genderValue = FhirFactory.eINSTANCE.createCode();
            genderValue.setValue(sendingConfig.getGender().toLowerCase());
            genderParameter.setValueCode(genderValue);
            parameters.getParameter().add(genderParameter);

            ParametersParameter dobParameter = FhirFactory.eINSTANCE.createParametersParameter();
            dobParameter.setName(FHIRUtil.convert(Consts.PARAMETER_NAME_BIRTH_DATE));

            dobParameter.setValueDate(dobValue);
            parameters.getParameter().add(dobParameter);
        } else {
         */
 /*
            ParametersParameter patientParameter = FhirFactory.eINSTANCE.createParametersParameter();
            patientParameter.setName(FHIRUtil.convert(Consts.PARAMETER_NAME_PATIENT));
            Patient patient = FhirFactory.eINSTANCE.createPatient();
            patient.setId(patientId);
            Code genderCode = FhirFactory.eINSTANCE.createCode();
            genderCode.setValue(sendingConfig.getGender().toLowerCase());           
            patient.setGender(genderCode);
            patient.setBirthDate(dobValue);
            ResourceContainer patientRC = FhirFactory.eINSTANCE.createResourceContainer();
            patientRC.setPatient(patient);
            patientParameter.setResource(patientRC);
            parameters.getParameter().add(patientParameter);
         */
        org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent patientParametersParameterFhir = new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent();
        patientParametersParameterFhir.setName(Consts.PARAMETER_NAME_PATIENT);
        org.hl7.fhir.dstu3.model.Patient patientFhir = new org.hl7.fhir.dstu3.model.Patient();
        patientFhir.setId(patientId);
        char gender = sendingConfig.getGender().toLowerCase().charAt(0);
        switch (gender) {
            case 'm':
                patientFhir.setGender(Enumerations.AdministrativeGender.MALE);
                break;
            case 'f':
                patientFhir.setGender(Enumerations.AdministrativeGender.FEMALE);
                break;
            case 'o':
                patientFhir.setGender(Enumerations.AdministrativeGender.OTHER);
                break;
            case 'u':
                patientFhir.setGender(Enumerations.AdministrativeGender.UNKNOWN);
                break;
            default:
                patientFhir.setGender(Enumerations.AdministrativeGender.NULL);
                break;
        }
        try {
            java.util.Date dob = TranslationUtils.translateHl7DateToJavaDate(sendingConfig.getBirthdate());
            patientFhir.setBirthDate(dob);
        } catch (ParseException ex) {
            Logger.getLogger(ImmunizationRecommendationClient.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            //TODO: Better error reporting
        }
        patientParametersParameterFhir.setResource(patientFhir);
        parametersFhir.addParameter(patientParametersParameterFhir);
        /*
        Set<String> mvx = ImmunizationRecommendationClient.getUniqueMvx(sendingConfig);
        if(mvx != null && !mvx.isEmpty()) {
            Iterator<String> mvxIt = mvx.iterator();
            while(mvxIt.hasNext()) {
                String mvxString = mvxIt.next();
                org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent mvxParametersParameterFhir = new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent();    
                mvxParametersParameterFhir.setName(mvxString);
                org.hl7.fhir.dstu3.model.Organization organizationFhir = new org.hl7.fhir.dstu3.model.Organization();  
                organizationFhir.setId(mvxString);
                org.hl7.fhir.dstu3.model.Identifier identifierFhir = new org.hl7.fhir.dstu3.model.Identifier();
                identifierFhir.setValue(mvxString);
                organizationFhir.addIdentifier(identifierFhir);
                mvxParametersParameterFhir.setResource(organizationFhir);
                parametersFhir.addParameter(mvxParametersParameterFhir);
            }
        }
        */
        //  }

        if (useAdapter) {
            org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent serviceTypeParameterFhir = new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent();
            serviceTypeParameterFhir.setName(Consts.PARAMETER_NAME_SERVICE_TYPE);

            org.hl7.fhir.dstu3.model.StringType serviceTypeString = new org.hl7.fhir.dstu3.model.StringType();
            serviceTypeString.setValue(routing.getForecastType());
            serviceTypeParameterFhir.setValue(serviceTypeString);
            parametersFhir.addParameter(serviceTypeParameterFhir);

            org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent serviceUrlParameterFhir = new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent();
            serviceUrlParameterFhir.setName(Consts.PARAMETER_NAME_SERVICE_URL);
            org.hl7.fhir.dstu3.model.StringType serviceUrlString = new org.hl7.fhir.dstu3.model.StringType();
            serviceUrlString.setValue(routing.getForecastUrl());
            serviceUrlParameterFhir.setValue(serviceUrlString);
            parametersFhir.addParameter(serviceUrlParameterFhir);

            /*
                    
            ParametersParameter serviceTypeParameter = FhirFactory.eINSTANCE.createParametersParameter();
            serviceTypeParameter.setName(FHIRUtil.convert(Consts.PARAMETER_NAME_SERVICE_TYPE));
            org.hl7.fhir.String serviceTypeString = FhirFactory.eINSTANCE.createString();
            serviceTypeString.setValue(routing.getForecastType());
            serviceTypeParameter.setValueString(serviceTypeString);
            parameters.getParameter().add(serviceTypeParameter);

            ParametersParameter serviceUrlParameter = FhirFactory.eINSTANCE.createParametersParameter();
            serviceUrlParameter.setName(FHIRUtil.convert(Consts.PARAMETER_NAME_SERVICE_URL));
            org.hl7.fhir.String serviceUrlString = FhirFactory.eINSTANCE.createString();
            serviceUrlString.setValue(routing.getForecastUrl());
            serviceUrlParameter.setValueString(serviceUrlString);
            parameters.getParameter().add(serviceUrlParameter);
             */
        }

        /* if(useAdapter) {
        
            ParametersParameter assessmentDateParameter = FhirFactory.eINSTANCE.createParametersParameter();
            assessmentDateParameter.setName(FHIRUtil.convert(Consts.PARAMETER_NAME_ASSESSMENT_DATE));
            Date assessmentDateValue = FhirFactory.eINSTANCE.createDate();
            assessmentDateValue.setValue(FHIRUtil.convert2XMLCalendar(sendingConfig.getAssessmentDate()));
            assessmentDateParameter.setValueDate(assessmentDateValue);
            parameters.getParameter().add(assessmentDateParameter);

        } else {
         */
        org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent assessmentDateParametersParameterFhir = new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent();
        assessmentDateParametersParameterFhir.setName(Consts.PARAMETER_NAME_ASSESSMENT_DATE);
        DateType assessmentDateFhir = new DateType();

        try {
            java.util.Date assessmentDate = TranslationUtils.translateHl7DateToJavaDate(sendingConfig.getAssessmentDate());
            assessmentDateFhir.setValue(assessmentDate);

            //assessmentDateParametersParameterFhir.castToDate(assessmentDateFhir);
            assessmentDateParametersParameterFhir.setValue(assessmentDateFhir);
        } catch (ParseException ex) {
            Logger.getLogger(ImmunizationRecommendationClient.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            // TODO: Better error reporting
        } catch (Exception ex) {
            Logger.getLogger(ImmunizationRecommendationClient.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        parametersFhir.addParameter(assessmentDateParametersParameterFhir);

        //   }
        /*
        if (useAdapter) {

            Collection<Immunization> immunizations = sendingConfig.getImmunizationData();
            if (immunizations != null) {
                ParametersParameter immunizationsParameter = FhirFactory.eINSTANCE.createParametersParameter();
                immunizationsParameter.setName(FHIRUtil.convert(Consts.PARAMETER_NAME_IMMUNIZATION_ADAPTER));
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
        } else {
         */
        Collection<Immunization> immunizations = sendingConfig.getImmunizationData();
        if (immunizations != null) {
            Iterator<Immunization> it = immunizations.iterator();
            while (it.hasNext()) {
                Immunization immunization = it.next();
                org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent immunizationParametersParameterFhir = new org.hl7.fhir.dstu3.model.Parameters.ParametersParameterComponent();
                immunizationParametersParameterFhir.setName(Consts.PARAMETER_NAME_IMMUNIZATION);
                org.hl7.fhir.dstu3.model.Immunization immunizationFhir = new org.hl7.fhir.dstu3.model.Immunization();
                System.out.println("OUTSIDE!");
                // Adding MVX org
               org.hl7.fhir.dstu3.model.Organization mvxOrg = new org.hl7.fhir.dstu3.model.Organization();

                if(immunization.getManufactorer() != null && !immunization.getManufactorer().isEmpty()) {
                    System.out.println("INSIDE!");
                    
                    
                    mvxOrg.getIdentifierFirstRep().setValue(immunization.getManufactorer());
                    
                    
                    
                    /*
                    org.hl7.fhir.dstu3.model.Organization mvxOrg = new org.hl7.fhir.dstu3.model.Organization();
                    mvxOrg.setId(immunization.getManufactorer());
                    org.hl7.fhir.dstu3.model.Identifier mvxIdentifier = new org.hl7.fhir.dstu3.model.Identifier();
                    mvxIdentifier.setValue(immunization.getManufactorer());
                    ArrayList<org.hl7.fhir.dstu3.model.Identifier> identifiers = new ArrayList<org.hl7.fhir.dstu3.model.Identifier>();
                    identifiers.add(mvxIdentifier);
                    mvxOrg.setIdentifier(identifiers);
                    ArrayList<org.hl7.fhir.dstu3.model.Resource> organizations = new ArrayList<org.hl7.fhir.dstu3.model.Resource>();
                    organizations.add(mvxOrg);
                    immunizationFhir.setContained(organizations);
                    
                    
                    System.out.println("Has contained??? " + immunizationFhir.hasContained());
                    
                    System.out.println("OUTPUTHERE!!!" + ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(immunizationFhir));
                    */
                    
                }
                
                immunizationFhir.setId(UUID.randomUUID().toString());
                try {
                    immunizationFhir.setDate(TranslationUtils.translateHl7DateToJavaDate(immunization.getDate()));
                } catch (ParseException ex) {
                    Logger.getLogger(ImmunizationRecommendationClient.class.getName()).log(Level.SEVERE, null, ex);
                    // TODO: Better error reporting
                }
                org.hl7.fhir.dstu3.model.CodeableConcept vaccineFhir = new org.hl7.fhir.dstu3.model.CodeableConcept();

                org.hl7.fhir.dstu3.model.Coding code = new org.hl7.fhir.dstu3.model.Coding();
                code.setCode(immunization.getVaccineCode());

                vaccineFhir.addCoding(code);
                immunizationFhir.setVaccineCode(vaccineFhir);
                immunizationFhir.setStatus(org.hl7.fhir.dstu3.model.Immunization.ImmunizationStatus.COMPLETED);
                if(immunization.getManufactorer() != null && !immunization.getManufactorer().isEmpty()) {
                    //org.hl7.fhir.dstu3.model.Reference manufacturerReference = new org.hl7.fhir.dstu3.model.Reference();
                    //manufacturerReference.setReference(immunization.getManufactorer());
                    //immunizationFhir.setManufacturer(manufacturerReference);
                    immunizationFhir.getManufacturer().setResource(mvxOrg);
                            
                            
                            //patient.getManagingOrganization().setResource(org);
                }
                
                org.hl7.fhir.dstu3.model.Reference patientReference = new org.hl7.fhir.dstu3.model.Reference();
                patientReference.setReference(patientId);
                immunizationFhir.setPatient(patientReference);
                immunizationFhir.setNotGiven(false);
                immunizationParametersParameterFhir.setResource(immunizationFhir);
                parametersFhir.addParameter(immunizationParametersParameterFhir);

                //TODO: Reported???
                //ParametersParameter immParam = FhirFactory.eINSTANCE.createParametersParameter();
                //immParam.setName(FHIRUtil.convert(Consts.PARAMETER_NAME_IMMUNIZATION));
                //ResourceContainer resource = FhirFactory.eINSTANCE.createResourceContainer();
                //org.hl7.fhir.Immunization fhirImmunization = FhirFactory.eINSTANCE.createImmunization();
                //Id immunizationId = FhirFactory.eINSTANCE.createId();
                //immunizationId.setValue(UUID.randomUUID().toString());
                //fhirImmunization.setId(immunizationId);
                //DateTime immunizationDate = FhirFactory.eINSTANCE.createDateTime();
                //immunizationDate.setValue(FHIRUtil.convert2XMLCalendar(immunization.getDate()));
                //fhirImmunization.setDate(immunizationDate);
                //Narrative codeText = FhirFactory.eINSTANCE.createNarrative();
                //NarrativeStatus codeTextStatus = FhirFactory.eINSTANCE.createNarrativeStatus();                
                //codeTextStatus.setValue(NarrativeStatusList.EMPTY.);
                //codeText.setStatus();
                //CodeableConcept immCC = FhirFactory.eINSTANCE.createCodeableConcept();
                //immCC.setText(FHIRUtil.convert(immunization.getVaccineCode()));
                //Coding immCoding = FhirFactory.eINSTANCE.createCoding();
                //Code immCode = FhirFactory.eINSTANCE.createCode();
                //immCode.setValue(immunization.getVaccineCode());
                //immCoding.setCode(immCode);
                //immCC.getCoding().add(immCoding);
                //fhirImmunization.setVaccineCode(immCC);
                //clean up
                //Code status = FhirFactory.eINSTANCE.createCode();
                // TODO put in final variable
                //status.setValue("completed");
                //fhirImmunization.setStatus(status);
                //Reference patient = FhirFactory.eINSTANCE.createReference();
                //patient.setReference(FHIRUtil.convert(patientId.getValue()));
                //fhirImmunization.setPatient(patient);
                // fhirImmunization.setWasNotGiven(FHIRUtil.BOOLEAN.FALSE.bool);
//                    fhirImmunization.setReported(FHIRUtil.BOOLEAN.TRUE.bool);
                //                  resource.setImmunization(fhirImmunization);
                //                immParam.setResource(resource);
                //              parameters.getParameter().add(immParam);
            }
        }

        //   }
        String payload = null;

//        if(useAdapter) {
        //          Serialize seri = new Serialize();
        //        xml = seri.it(parameters, "sut.xml");
        //  } else {
        if (format != null && format.equals(FormatEnum.JSON)) {
            payload = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(parametersFhir);
        } else {
            payload = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(parametersFhir);
        }

//        }
        //       System.out.println(seri.it(patientParameter, "sut.xml"));
        //System.out.println("GENERATED OBJECT HERE ----->\n" + xml);
        /*
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
         */
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
        //    System.out.println(xml);
        return payload;
    }

    private static Response sendImmunizationInformation(Routing routing, SendingConfig sendingConfig, boolean useAdapter, FormatEnum format) throws UnsupportedEncodingException, ConnectionException {

        Response response = new Response();
        HttpPost request = null;
        if (useAdapter) {
            request = new HttpPost(routing.getFhirAdapterUrl());
        } else {
            request = new HttpPost(routing.getForecastUrl());
        }
        String outgoingXml = ImmunizationRecommendationClient.generatePayload(routing, sendingConfig, useAdapter, format);
        StringEntity paramsXml = new StringEntity(outgoingXml);
        try {
            System.out.println("OUTGOING? " + convertStreamToString(paramsXml.getContent()));
        } catch (IOException ex) {
            Logger.getLogger(ImmunizationRecommendationClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        request.addHeader("content-type", "application/xml; charset=utf8");
        request.addHeader("accept", "application/xml");
        request.setEntity(paramsXml);

        // HttpClient httpClient = HttpClientBuilder.create().build();             
        HttpClient httpClient = null;
        try {
            httpClient = HttpClients.custom()
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(SSLContexts.custom()
                            .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                            .build()
                    )
                    ).build();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ImmunizationRecommendationClient.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectionException(null, "Certificate Error: NoSuchAlgorithmException " + ex.getMessage());
        } catch (KeyStoreException ex) {
            Logger.getLogger(ImmunizationRecommendationClient.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectionException(null, "Certificate Error: KeyStoreException " + ex.getMessage());
        } catch (KeyManagementException ex) {
            Logger.getLogger(ImmunizationRecommendationClient.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectionException(null, "Certificate Error: KeyManagementException " + ex.getMessage());
        }

        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(request);
        } catch (NoHttpResponseException ex) {
            Logger.getLogger(ImmunizationRecommendationClient.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectionException(null, "Connection Error: NoHttpResponseException " + ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(ImmunizationRecommendationClient.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectionException(null, "I/O Error: IOException " + ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(ImmunizationRecommendationClient.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectionException(null, "Connection Error: Exception " + ex.getMessage());
        }

        if (!String.valueOf(httpResponse.getStatusLine().getStatusCode()).startsWith("2")) {
            throw new ConnectionException(String.valueOf(httpResponse.getStatusLine().getStatusCode()), httpResponse.getStatusLine().getReasonPhrase());
        }

        response.setHttpCode(String.valueOf(httpResponse.getStatusLine().getStatusCode()));

        //TODO: Improve this.
        String body;

        try {
            body = convertStreamToString(httpResponse.getEntity().getContent());
        } catch (IOException ex) {
            Logger.getLogger(ImmunizationRecommendationClient.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectionException(null, "I/O Error: IOException " + ex.getMessage());
        } catch (UnsupportedOperationException ex) {
            Logger.getLogger(ImmunizationRecommendationClient.class.getName()).log(Level.SEVERE, null, ex);
            throw new ConnectionException(null, "Certificate Error: KeyManagementException " + ex.getMessage());
        }
        if (body.trim().isEmpty()) {
            throw new ConnectionException(null, "Empty message returned.");
        }
        //System.out.println("COMING BACK!!!! " + body);

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

    private static Set<String> getUniqueMvx(SendingConfig sendingConfig) {
        
        Set<String> mvx = new HashSet<>();
        Collection<Immunization> imms = sendingConfig.getImmunizationData();        
        if (imms == null) return mvx;        
        Iterator<Immunization> it = imms.iterator();
        while(it.hasNext()) {            
            Immunization imm = it.next();
            if(imm.getManufactorer() != null && !imm.getManufactorer().isEmpty())
                mvx.add(imm.getManufactorer());            
        }
        
        return mvx;
        
        
    }

    public Object getImmunizationRecommendation(Routing routing, SendingConfig sendingConfig, boolean useAdapter, FormatEnum format) throws IOException, UnsupportedEncodingException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, ConnectionException {

        Response response = ImmunizationRecommendationClient.sendImmunizationInformation(routing, sendingConfig, useAdapter, format);

        //TODO: This workaround is no longer needed.  Fix it.
        String xml = response.getPayload();
        Object s1 = null;
        /*
        if(useAdapter) {
        DeSerialize deserial = new DeSerialize();
        s1 = deserial.it(new StringReader(xml), "*.xml");
        //System.out.println(s1.toString() + "!!!" + s1.eResource());
        //  BundleImpl bundle = (BundleImpl) s1;
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
        //   tempFile.delete();
        //return recommendations;

        //    } else {
        FhirContext ctx = FhirContext.forDstu3();
        s1 = ctx.newXmlParser().parseResource(xml);
        //org.hl7.fhir.dstu3.model.Bundle bundle = new org.hl7.fhir.dstu3.model.Bundle();
        //bundle = (org.hl7.fhir.dstu3.model.Bundle) s2;
        //s1 = bundle;
        //  }
        return s1;
        //return engineResponse;

    }

    public static void main(String[] args) throws IOException, UnsupportedEncodingException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, ConnectionException {

        ImmunizationRecommendationClient irc = new ImmunizationRecommendationClient();
        Routing routing = new Routing();
        routing.setFhirAdapterUrl("http://localhost:8084/forecast/ImmunizationRecommendations");
        //routing.setFhirAdapterUrl("http://hit-dev.nist.gov:11080/fhirAdapter/fhir/Parameters/$cds-forecast");
        routing.setForecastType("TCH");
        //  routing.setForecastUrl("http://tchforecasttester.org/fv/forecast");
        routing.setForecastUrl("https://test-cdsi.rhcloud.com/CDSi/cds-forecast");

        SendingConfig sendingConfig = new SendingConfig();
        //sendingConfig.setAssessmentDate("2016-12-01T10:57:34+01:00");
        //sendingConfig.setBirthdate("2016-11-01T10:57:34+01:00");

        sendingConfig.setBirthdate("2009-08-09");
        sendingConfig.setAssessmentDate("2017-03-13");
        sendingConfig.setGender("female");

        Collection<Immunization> imms = new ArrayList<Immunization>();

        Immunization imm = new Immunization();
        imm.setDate("2017-01-01");
        imm.setVaccineCode("110");
        imms.add(imm);

        Immunization imm2 = new Immunization();
        imm2.setDate("2016-01-02");
        imm2.setVaccineCode("116");
        imms.add(imm2);

        sendingConfig.setImmunizationData(imms);

        /*
        EObject bundle = (EObject) irc.getImmunizationRecommendation(routing, sendingConfig, false);
        //   Serialize ser = new Serialize();

        // System.out.println(ser.it(bundle,"*.xml"));
        org.hl7.fhir.ImmunizationRecommendation ir = FhirFactory.eINSTANCE.createImmunizationRecommendation();
        Id id = FhirFactory.eINSTANCE.createId();
        id.setValue(UUID.randomUUID().toString());
        ir.setId(id);

        Parameters params = FhirFactory.eINSTANCE.createParameters();
        ParametersParameter param = FhirFactory.eINSTANCE.createParametersParameter();
        params.getParameter().add(param);
        ParametersParameter param2 = params.getParameter().get(0);
        param.setId("HERE");
        org.hl7.fhir.String name = FhirFactory.eINSTANCE.createString();
        name.setValue("name");
        param.setName(name);
        param.setValueId(id);
        ResourceContainer rc = FhirFactory.eINSTANCE.createResourceContainer();
        rc.setImmunizationRecommendation(ir);
        param.setResource(rc);

      //  Serialize ser = new Serialize();
//        String irString = ser.it(ir, "*.xml");
  //      String paramsString = ser.it(params, "*.xml");
        //   String paramsString;
        //try {
    //    paramsString = ser.it(params, "*.xml"); //ser.xmlFromParameter(params);
      //  String paramString = ser.it(param, "*.xml");
        //String param2String = ser.it(param2, "*.xml");

        // XMLHelperImpl.saveString(options, contents, irString, helper)
        //System.out.println(irString);
        //System.out.println(paramsString);
        //System.out.println(paramString);
        //System.out.println(param2String);
        //} catch (ParserConfigurationException ex) {
//            Logger.getLogger(ImmunizationRecommendationClient.class.getName()).log(Level.SEVERE, null, ex);
        //      }
         */
    }

}
