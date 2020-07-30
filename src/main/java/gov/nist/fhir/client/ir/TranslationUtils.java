/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.fhir.client.ir;

import gov.nist.healthcare.cds.domain.FixedDate;
import gov.nist.healthcare.cds.domain.wrapper.ActualEvaluation;
import gov.nist.healthcare.cds.domain.wrapper.ActualForecast;
import gov.nist.healthcare.cds.domain.wrapper.ResponseVaccinationEvent;
import gov.nist.healthcare.cds.domain.wrapper.VaccineRef;
import gov.nist.healthcare.cds.enumeration.EvaluationStatus;
import gov.nist.healthcare.cds.enumeration.SerieStatus;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Immunization.ImmunizationVaccinationProtocolComponent;
import org.hl7.fhir.dstu3.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent;
import org.hl7.fhir.dstu3.model.Reference;

/**
 *
 * @author mccaffrey
 */
public class TranslationUtils {

    // TODO: Surely this must already exist somewhere else???
    public static final String IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_DUE = "due";
    public static final String IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_EARLIEST = "earliest";
    public static final String IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_OVERDUE = "overdue";
    public static final String IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_LATEST = "latest";
    public static final String IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_RECOMMENDED = "recommended";
    
    public static String translateCsdiDateToFhirDate(FixedDate date) {
        SimpleDateFormat print = new SimpleDateFormat("yyyy-MM-dd");
        return print.format(date.asDate());
    }
    
    public static Date translateHl7DateToJavaDate(String date) throws ParseException {
        
        DateFormat df = new SimpleDateFormat("yyy-MM-dd");
        return df.parse(date);
    }
    
    public static String translateJavaDateToFhirDate(Date date) {
        SimpleDateFormat print = new SimpleDateFormat("yyyy-MM-dd");
        return print.format(date);
    }
    
    public static FixedDate translateTchDateToFhirDate(String date) {
        
        String year = date.substring(0, 4);
        String month = date.substring(5, 7);
        String day = date.substring(8, 10);
        
        return new FixedDate(month + '/' + day + '/' + year);
        
    }

    /*
    public static ResponseVaccinationEvent translateImmunizationToResponseVaccinationEvent(Immunization imm) {

        ResponseVaccinationEvent rve = new ResponseVaccinationEvent();
        VaccineRef vaccineRef = new VaccineRef();

        if (imm.getVaccineCode() != null && imm.getVaccineCode().getCoding() != null
                && imm.getVaccineCode().getCoding().get(0) != null
                && imm.getVaccineCode().getCoding().get(0).getCode() != null) {
            vaccineRef.setCvx(imm.getVaccineCode().getCoding().get(0).getCode().getValue());
        }
        rve.setAdministred(vaccineRef);

        rve.setDate(TranslationUtils.translateTchDateToFhirDate(imm.getDate().getValue().toString()));
        rve.setEvaluations(new HashSet<ActualEvaluation>());
        EList<ImmunizationVaccinationProtocol> vaccinationProtocols = imm.getVaccinationProtocol();
        Iterator<ImmunizationVaccinationProtocol> it = vaccinationProtocols.iterator();
        while (it.hasNext()) {
            ImmunizationVaccinationProtocol ivp = it.next();
            ActualEvaluation ae = new ActualEvaluation();
            String status = "";
            if (ivp.getDoseStatus() != null && ivp.getDoseStatus().getCoding() != null
                    && ivp.getDoseStatus().getCoding().get(0) != null
                    && ivp.getDoseStatus().getCoding().get(0).getCode() != null) {
                status = ivp.getDoseStatus().getCoding().get(0).getCode().getValue();
            }
            if ("Y".equalsIgnoreCase(status)) {
                ae.setStatus(EvaluationStatus.VALID);
            } else {
                ae.setStatus(EvaluationStatus.INVALID);
            }
            VaccineRef vr = new VaccineRef();
            if (ivp.getSeries() != null) {
                vr.setCvx(ivp.getSeries().getValue());
            }
            ae.setVaccine(vr);
            rve.getEvaluations().add(ae);
        }
        return rve;
    }
     */
    public static ResponseVaccinationEvent translateImmunizationToResponseVaccinationEvent(org.hl7.fhir.dstu3.model.Immunization imm) {
        
        ResponseVaccinationEvent rve = new ResponseVaccinationEvent();
        VaccineRef vaccineRef = new VaccineRef();
        
        if (imm.getVaccineCode() != null && imm.getVaccineCode().getCoding() != null
                && imm.getVaccineCode().getCoding().get(0) != null
                && imm.getVaccineCode().getCoding().get(0).getCode() != null) {
            vaccineRef.setCvx(imm.getVaccineCode().getCoding().get(0).getCode());
        }
        rve.setAdministred(vaccineRef);
        if(imm.getDate() != null)
            rve.setDate(new FixedDate(imm.getDate()));
        rve.setEvaluations(new HashSet<ActualEvaluation>());
        
        List<ImmunizationVaccinationProtocolComponent> vaccinationProtocols = imm.getVaccinationProtocol();
        Iterator<ImmunizationVaccinationProtocolComponent> it = vaccinationProtocols.iterator();
        while (it.hasNext()) {
            ImmunizationVaccinationProtocolComponent ivp = it.next();            
            ActualEvaluation ae = new ActualEvaluation();
            boolean safeToSend = true;
            String status = "";
            if (ivp.getDoseStatus() != null && ivp.getDoseStatus().getCoding() != null
            		&& ivp.getDoseStatus().getCoding().size() > 0
                    && ivp.getDoseStatus().getCoding().get(0) != null
                    && ivp.getDoseStatus().getCoding().get(0).getCode() != null) {
                status = ivp.getDoseStatus().getCoding().get(0).getCode();
            }
            
            System.out.println("Status for " + ivp.getSeries() + " = " + status);
            if ("Valid".equalsIgnoreCase(status)) {
                ae.setStatus(EvaluationStatus.VALID);
            } else if ("Not Valid".equalsIgnoreCase(status)) {
                ae.setStatus(EvaluationStatus.INVALID);
                System.out.println("Setting " + ivp.getSeries() + " to EvaluationStatus.INVALID");
            } else if ("Extraneous".equalsIgnoreCase(status)) {
                ae.setStatus(EvaluationStatus.EXTRANEOUS);
            } else if ("Sub standard".equalsIgnoreCase(status)) {
                ae.setStatus(EvaluationStatus.SUBSTANDARD);
            } else if ("Y".equalsIgnoreCase(status)) {
                ae.setStatus(EvaluationStatus.VALID);
             } else if ("Invalid".equalsIgnoreCase(status)) {
                ae.setStatus(EvaluationStatus.INVALID);
            } else {
                // ae.setStatus(EvaluationStatus.INVALID);
                //Remove default 11/7/2018
                // Also 11/7/2018, if no EvaluationStatus, DO NOT SEND
                safeToSend = false;
            }
            VaccineRef vr = new VaccineRef();
            
            /*
            if (ivp.getSeries() != null) {
                vr.setCvx(ivp.getSeries());
            }*/
            if(ivp.getTargetDiseaseFirstRep() != null && ivp.getTargetDiseaseFirstRep().getCodingFirstRep() != null
                    && ivp.getTargetDiseaseFirstRep().getCodingFirstRep().getCode() !=  null) {
                vr.setCvx(ivp.getTargetDiseaseFirstRep().getCodingFirstRep().getCode());
            }
            
            ae.setVaccine(vr);            
            if(safeToSend) {
                rve.getEvaluations().add(ae);
            }
        }
        return rve;
    }

        public static ResponseVaccinationEvent translateImmunizationEvaluationToResponseVaccinationEventCurrentFhir(org.hl7.fhir.r4.model.ImmunizationEvaluation imm) {
       
        
        ResponseVaccinationEvent rve = new ResponseVaccinationEvent();
        /*
        VaccineRef vaccineRef = new VaccineRef();
        
        if (imm.getVaccineCode() != null && imm.getVaccineCode().getCoding() != null
                && imm.getVaccineCode().getCoding().get(0) != null
                && imm.getVaccineCode().getCoding().get(0).getCode() != null) {
            vaccineRef.setCvx(imm.getVaccineCode().getCoding().get(0).getCode());
        }
        rve.setAdministred(vaccineRef);
        */
        if(imm.getDate() != null)
            rve.setDate(new FixedDate(imm.getDate()));
        rve.setEvaluations(new HashSet<ActualEvaluation>());
        
        if(imm.getDoseNumberPositiveIntType() != null
                && imm.getDoseNumberPositiveIntType().getValue() != null) {
            rve.setDoseNumber(imm.getDoseNumberPositiveIntType().getValue());
        }
/*
        List<org.hl7.fhir.r4.model.Immunization.ImmunizationProtocolAppliedComponent> vaccinationProtocols = imm.getProtocolApplied();
        Iterator<org.hl7.fhir.r4.model.Immunization.ImmunizationProtocolAppliedComponent> it = vaccinationProtocols.iterator();
        while (it.hasNext()) {
            org.hl7.fhir.r4.model.Immunization.ImmunizationProtocolAppliedComponent ivp = it.next();            
            ActualEvaluation ae = new ActualEvaluation();
            boolean safeToSend = true;
            String status = "";
          
            if (ivp.getDoseStatus() != null && ivp.getDoseStatus().getCoding() != null
            		&& ivp.getDoseStatus().getCoding().size() > 0
                    && ivp.getDoseStatus().getCoding().get(0) != null
                    && ivp.getDoseStatus().getCoding().get(0).getCode() != null) {
                status = ivp.getDoseStatus().getCoding().get(0).getCode();
            }
            
            System.out.println("Status for " + ivp.getSeries() + " = " + status);
        */

            ActualEvaluation ae = new ActualEvaluation();
            boolean safeToSend = true;
            String status = "";
            if(imm.getDoseStatus() != null && imm.getDoseStatus().getCoding() != null
                    && imm.getDoseStatus().getCoding().size() > 0
                    && imm.getDoseStatus().getCoding().get(0) != null
                    && imm.getDoseStatus().getCoding().get(0).getCode() != null) {
                status = imm.getDoseStatus().getCoding().get(0).getCode();

            if ("Valid".equalsIgnoreCase(status)) {
                ae.setStatus(EvaluationStatus.VALID);
            } else if ("Not Valid".equalsIgnoreCase(status)) {
                ae.setStatus(EvaluationStatus.INVALID);
//                System.out.println("Setting " + ivp.getSeries() + " to EvaluationStatus.INVALID");
            } else if ("Extraneous".equalsIgnoreCase(status)) {
                ae.setStatus(EvaluationStatus.EXTRANEOUS);
            } else if ("Sub standard".equalsIgnoreCase(status)) {
                ae.setStatus(EvaluationStatus.SUBSTANDARD);
            } else if ("Y".equalsIgnoreCase(status)) {
                ae.setStatus(EvaluationStatus.VALID);
             } else if ("Invalid".equalsIgnoreCase(status)) {
                ae.setStatus(EvaluationStatus.INVALID);
            } else {
                // ae.setStatus(EvaluationStatus.INVALID);
                //Remove default 11/7/2018
                // Also 11/7/2018, if no EvaluationStatus, DO NOT SEND
                safeToSend = false;
            }
            VaccineRef vr = new VaccineRef();
            
            /*
            if (ivp.getSeries() != null) {
                vr.setCvx(ivp.getSeries());
            }*/
            if(imm.getTargetDisease() != null 
                    && imm.getTargetDisease().getCoding() != null
                    && imm.getTargetDisease().getCoding().get(0) != null
                    && imm.getTargetDisease().getCoding().get(0).getCode() != null ) {
                
                
                vr.setCvx(imm.getTargetDisease().getCoding().get(0).getCode());
            }
            
            ae.setVaccine(vr);            
            if(safeToSend) {
                rve.getEvaluations().add(ae);
            }
        }
        return rve;
    }

    
    /*
    
    public static ResponseVaccinationEvent translateImmunizationRecommendationRecommendationToResponseVaccinationEvent(
            ImmunizationRecommendationRecommendation irr) {
        ResponseVaccinationEvent rve = new ResponseVaccinationEvent();
        VaccineRef vaccineRef = new VaccineRef();
        // TODO: Error checking
        vaccineRef.setCvx(irr.getVaccineCode().getCoding().get(0).getCode().getValue());
        rve.setAdministred(vaccineRef);
        // TODO: Error checking
        rve.setDate(TranslationUtils.translateTchDateToFhirDate(irr.getDate().getValue().toString()));
        ActualEvaluation ae = new ActualEvaluation();
        String status = "";
        // TODO: Error checking
        status = irr.getForecastStatus().getCoding().get(0).getCode().getValue();
        // EvaluationStatus.

        return rve;
    }
     */
 /*
    public static ActualForecast translateImmunizationRecommendationToActualForecast(ImmunizationRecommendation ir) {
        ActualForecast forecast = new ActualForecast();
        if (ir.getRecommendation() == null || ir.getRecommendation().get(0) == null) {
            return forecast;
        }
        ImmunizationRecommendationRecommendation irr = ir.getRecommendation().get(0);
        if (irr.getDoseNumber() != null && irr.getDoseNumber().getValue() != null) {
            forecast.setDoseNumber(irr.getDoseNumber().getValue().toString());
        }
        VaccineRef vaccineRef = new VaccineRef();
        if (irr.getVaccineCode() != null && irr.getVaccineCode().getCoding() != null
                && irr.getVaccineCode().getCoding().get(0) != null
                && irr.getVaccineCode().getCoding().get(0).getCode() != null) {
            vaccineRef.setCvx(irr.getVaccineCode().getCoding().get(0).getCode().getValue());
        }
        forecast.setVaccine(vaccineRef);
        EList<ImmunizationRecommendationDateCriterion> dateCriterions = irr.getDateCriterion();
        Iterator<ImmunizationRecommendationDateCriterion> it = dateCriterions.iterator();
        while (it.hasNext()) {
            ImmunizationRecommendationDateCriterion dateCriterion = it.next();
            if (dateCriterion.getValue() != null && dateCriterion.getValue().getValue() != null) {
                FixedDate date = TranslationUtils
                        .translateTchDateToFhirDate(dateCriterion.getValue().getValue().toString());

                // TODO: Error checking
                String status = "";
                if (dateCriterion.getCode() != null && dateCriterion.getCode().getCoding() != null
                        && dateCriterion.getCode().getCoding().get(0) != null
                        && dateCriterion.getCode().getCoding().get(0).getCode() != null) {
                    status = dateCriterion.getCode().getCoding().get(0).getCode().getValue();
                }                
                switch (status) {
                    // TODO: Some systems are using "due", others "recommended". This will probably be fixed to just one.
                    case IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_RECOMMENDED:
                        forecast.setRecommended(date.getDate());
                        break;
                    case IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_DUE:
                        forecast.setRecommended(date.getDate());
                        break;
                    case IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_EARLIEST:
                        forecast.setEarliest(date.getDate());
                        break;
                    case IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_OVERDUE:
                        forecast.setPastDue(date.getDate());
                        break;
                    case IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_LATEST:
                        forecast.setComplete(date.getDate());
                        break;
                }

            }
        }
        if (irr.getForecastStatus() != null && irr.getForecastStatus().getCoding() != null
                && irr.getForecastStatus().getCoding().get(0) != null
                && irr.getForecastStatus().getCoding().get(0).getCode() != null) {
            forecast.setSerieStatus(
                    SerieStatus.valueOf(irr.getForecastStatus().getCoding().get(0).getCode().getValue()));
        }
        return forecast;
    }
     */
 /*
    public static ActualForecast translateImmunizationRecommendationRecommendationToActualForecast(
            ImmunizationRecommendationRecommendation irr) {
        ActualForecast af = new ActualForecast();
        if (irr.getDoseNumber() != null && irr.getDoseNumber().getValue() != null) {
            af.setDoseNumber(irr.getDoseNumber().getValue().toString());
        }
        VaccineRef vaccineRef = new VaccineRef();
        if (irr.getVaccineCode() != null && irr.getVaccineCode().getCoding() != null
                && irr.getVaccineCode().getCoding().get(0) != null
                && irr.getVaccineCode().getCoding().get(0).getCode() != null) {
            vaccineRef.setCvx(irr.getVaccineCode().getCoding().get(0).getCode().getValue());
        }
        af.setVaccine(vaccineRef);
        EList<ImmunizationRecommendationDateCriterion> dateCriterions = irr.getDateCriterion();
        Iterator<ImmunizationRecommendationDateCriterion> it = dateCriterions.iterator();
        while (it.hasNext()) {
            ImmunizationRecommendationDateCriterion dateCriterion = it.next();
            if (dateCriterion.getValue() != null && dateCriterion.getValue().getValue() != null) {
                FixedDate date = TranslationUtils
                        .translateTchDateToFhirDate(dateCriterion.getValue().getValue().toString());

                // TODO: Error checking
                String status = "";
                if (dateCriterion.getCode() != null && dateCriterion.getCode().getCoding() != null
                        && dateCriterion.getCode().getCoding().get(0) != null
                        && dateCriterion.getCode().getCoding().get(0).getCode() != null) {
                    status = dateCriterion.getCode().getCoding().get(0).getCode().getValue();
                }
                switch (status) {
                    case IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_DUE:
                        af.setRecommended(date.getDate());
                        break;
                    case IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_EARLIEST:
                        af.setEarliest(date.getDate());
                        break;
                    case IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_OVERDUE:
                        af.setPastDue(date.getDate());
                        break;
                    case IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_LATEST:
                        af.setComplete(date.getDate());
                        break;
                }
            }
        }
        if (irr.getForecastStatus() != null && irr.getForecastStatus().getCoding() != null
                && irr.getForecastStatus().getCoding().get(0) != null
                && irr.getForecastStatus().getCoding().get(0).getCode() != null) {
            String status = irr.getForecastStatus().getCoding().get(0).getCode().getValue();

            // TODO: Is this work around needed? Or is one just wrong?
            if (status.equals("Not Complete")) {
                af.setSerieStatus(SerieStatus.E);
            } else if (status.equals("Aged Out")) {
                af.setSerieStatus(SerieStatus.G);
            } else {
                af.setSerieStatus(SerieStatus.valueOf(status));
            }
        }
        return af;

    }
     */
    public static ActualForecast translateImmunizationRecommendationRecommendationToActualForecast(
            org.hl7.fhir.dstu3.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent irr) {
        
        /* Removing March 19th, 2019. Even if there is no date, try to create the Actual Forecast anyway
        if (irr.getDate() == null || "".equals(irr.getDate())) {
            return null;
        }
        */
        
        ActualForecast af = new ActualForecast();
        //TODO: Error checking
        af.setDoseNumber(Integer.toString(irr.getDoseNumber()));
        VaccineRef vaccineRef = new VaccineRef();
        if (irr.getVaccineCode() != null && irr.getVaccineCode().getCoding() != null
                && irr.getVaccineCode().getCoding().size() > 0
                && irr.getVaccineCode().getCoding().get(0) != null
                && irr.getVaccineCode().getCoding().get(0).getCode() != null) {
            Coding ct = irr.getVaccineCode().getCoding().get(0);
            vaccineRef.setCvx(irr.getVaccineCode().getCoding().get(0).getCode());
            
        }
        af.setVaccine(vaccineRef);
        
        List<ImmunizationRecommendationRecommendationDateCriterionComponent> dateCriterions = null; 
        if (irr.getDateCriterion() != null)
            dateCriterions = irr.getDateCriterion();
        else
            dateCriterions = new ArrayList();
        
        Iterator<ImmunizationRecommendationRecommendationDateCriterionComponent> it = dateCriterions.iterator();
        while (it.hasNext()) {
            
            ImmunizationRecommendationRecommendationDateCriterionComponent dateCriterion = it.next();
            
            if (dateCriterion.getValue() != null && dateCriterion.getValue() != null) {
                FixedDate date = new FixedDate(dateCriterion.getValue());

                // TODO: Error checking
                String status = "";
                if (dateCriterion.getCode() != null && dateCriterion.getCode().getCoding() != null
                        && dateCriterion.getCode().getCoding().get(0) != null
                        && dateCriterion.getCode().getCoding().get(0).getCode() != null) {
                    status = dateCriterion.getCode().getCoding().get(0).getCode();
                }
                switch (status) {
                    //case IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_DUE:
//                        af.setRecommended(date.getDate());
  //                      break;
                    case IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_EARLIEST:
                        af.setEarliest(date.asDate());
                        break;
                    case IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_OVERDUE:
                        af.setPastDue(date.asDate());
                        break;
                    case IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_LATEST:
                        af.setComplete(date.asDate());
                        break;
                    case IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_RECOMMENDED:
                        af.setRecommended(date.asDate());
                        break;                    
                }
            }
        }
        if (irr.getForecastStatus() != null && irr.getForecastStatus().getCoding() != null
                && irr.getForecastStatus().getCoding().size() > 0
                && irr.getForecastStatus().getCoding().get(0) != null
                && irr.getForecastStatus().getCoding().get(0).getCode() != null) {
            String status = irr.getForecastStatus().getCoding().get(0).getCode();

            
            try {
                if (status.equalsIgnoreCase("assumed complete or immune")){
                    af.setSerieStatus(SerieStatus.A);
                } else if (status.equalsIgnoreCase("complete")) {
                    af.setSerieStatus(SerieStatus.C);
                } else if (status.equalsIgnoreCase("due")) {
                    af.setSerieStatus(SerieStatus.D);
                    //TODO : Find out what happened to E and R?
                    
//                } else if(status.equalsIgnoreCase("error")) {
//                    af.setSerieStatus(SerieStatus.E);
                } else if(status.equalsIgnoreCase("finished")) {
                    af.setSerieStatus(SerieStatus.F);
                } else if(status.equalsIgnoreCase("aged out")) {
                    af.setSerieStatus(SerieStatus.G);                    
                } else if(status.equalsIgnoreCase("immune")) {
                    af.setSerieStatus(SerieStatus.I);
                } else if(status.equalsIgnoreCase("due later")) {
                    af.setSerieStatus(SerieStatus.L);
                } else if(status.equalsIgnoreCase("not complete")) {
                    af.setSerieStatus(SerieStatus.N);                    
                } else if(status.equalsIgnoreCase("overdue")) {
                    af.setSerieStatus(SerieStatus.O);                    
//                } else if(status.equalsIgnoreCase("no results")) {
//                    af.setSerieStatus(SerieStatus.R);
                } else if(status.equalsIgnoreCase("complete for season")) {
                    af.setSerieStatus(SerieStatus.S);
                } else if(status.equalsIgnoreCase("unknown")) {
                    af.setSerieStatus(SerieStatus.U);
                } else if(status.equalsIgnoreCase("Consider")) {
                    af.setSerieStatus(SerieStatus.V);
                } else if(status.equalsIgnoreCase("waivered")) {
                    af.setSerieStatus(SerieStatus.W);
                } else if(status.equalsIgnoreCase("contraindicated")) {
                    af.setSerieStatus(SerieStatus.X);
                } else if(status.equalsIgnoreCase("recommended but not required")) {
                    af.setSerieStatus(SerieStatus.Z);
                }

            } catch (Exception e) {
                //TODO better error checking
                System.out.println("Unexpected dose status = " + status);
            }
        }
        return af;
        
    }
    
        public static ActualForecast translateImmunizationRecommendationRecommendationToActualForecastCurrentFhir(
            org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent irr) {
        
        /* Removing March 19th, 2019. Even if there is no date, try to create the Actual Forecast anyway
        if (irr.getDate() == null || "".equals(irr.getDate())) {
            return null;
        }
        */
        
        ActualForecast af = new ActualForecast();
        if(irr.getDoseNumberStringType() != null)
            af.setDoseNumber(irr.getDoseNumberStringType().toString());
        //af.setDoseNumber(Integer.toString(irr.getDoseNumber()));
        VaccineRef vaccineRef = new VaccineRef();
        if (irr.getVaccineCode() != null && irr.getVaccineCode().listIterator() != null
                && irr.getVaccineCode().size() > 0
                && irr.getVaccineCode().get(0) != null
                && irr.getVaccineCode().get(0).getText() != null) {
            org.hl7.fhir.r4.model.CodeableConcept ct = irr.getVaccineCode().get(0);
            vaccineRef.setCvx(irr.getVaccineCode().get(0).getText());
            
        }
        af.setVaccine(vaccineRef);
        
        List<org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent> dateCriterions = null; 
        if (irr.getDateCriterion() != null)
            dateCriterions = irr.getDateCriterion();
        else
            dateCriterions = new ArrayList();
        
        Iterator<org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent> it = dateCriterions.iterator();
        while (it.hasNext()) {
            
            org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent dateCriterion = it.next();
            
            if (dateCriterion.getValue() != null && dateCriterion.getValue() != null) {
                FixedDate date = new FixedDate(dateCriterion.getValue());

                // TODO: Error checking
                String status = "";
                if (dateCriterion.getCode() != null && dateCriterion.getCode().getCoding() != null
                        && dateCriterion.getCode().getCoding().get(0) != null
                        && dateCriterion.getCode().getCoding().get(0).getCode() != null) {
                    status = dateCriterion.getCode().getCoding().get(0).getCode();
                }
                switch (status) {
                    //case IMMUNIZATION_RECOMMENDATION_DATE_CRITERION_DUE:
//                        af.setRecommended(date.getDate());
  //                      break;
                    
                    //TODO make this Consts
                    
                    case "30981-5": //earliest
                        af.setEarliest(date.asDate());
                        break;
                    case "59778-1": // over due
                        af.setPastDue(date.asDate());
                        break;
                    case "59777-3": //latest
                        af.setComplete(date.asDate());
                        break;
                    case "30980-7": // DATE DUE
                        af.setRecommended(date.asDate());
                        break;                    
                }
            }
        }
        if (irr.getForecastStatus() != null && irr.getForecastStatus().getCoding() != null
                && irr.getForecastStatus().getCoding().size() > 0
                && irr.getForecastStatus().getCoding().get(0) != null
                && irr.getForecastStatus().getCoding().get(0).getCode() != null) {
            String status = irr.getForecastStatus().getCoding().get(0).getCode();

            
            try {
                if (status.equalsIgnoreCase("assumed complete or immune")){
                    af.setSerieStatus(SerieStatus.A);
                } else if (status.equalsIgnoreCase("complete")) {
                    af.setSerieStatus(SerieStatus.C);
                } else if (status.equalsIgnoreCase("due")) {
                    af.setSerieStatus(SerieStatus.D);
                    //TODO : Find out what happened to E and R?
                    
//                } else if(status.equalsIgnoreCase("error")) {
//                    af.setSerieStatus(SerieStatus.E);
                } else if(status.equalsIgnoreCase("finished")) {
                    af.setSerieStatus(SerieStatus.F);
                } else if(status.equalsIgnoreCase("aged out")) {
                    af.setSerieStatus(SerieStatus.G);                    
                } else if(status.equalsIgnoreCase("immune")) {
                    af.setSerieStatus(SerieStatus.I);
                } else if(status.equalsIgnoreCase("due later")) {
                    af.setSerieStatus(SerieStatus.L);
                } else if(status.equalsIgnoreCase("not complete")) {
                    af.setSerieStatus(SerieStatus.N);                    
                } else if(status.equalsIgnoreCase("overdue")) {
                    af.setSerieStatus(SerieStatus.O);                    
//                } else if(status.equalsIgnoreCase("no results")) {
//                    af.setSerieStatus(SerieStatus.R);
                } else if(status.equalsIgnoreCase("complete for season")) {
                    af.setSerieStatus(SerieStatus.S);
                } else if(status.equalsIgnoreCase("unknown")) {
                    af.setSerieStatus(SerieStatus.U);
                } else if(status.equalsIgnoreCase("Consider")) {
                    af.setSerieStatus(SerieStatus.V);
                } else if(status.equalsIgnoreCase("waivered")) {
                    af.setSerieStatus(SerieStatus.W);
                } else if(status.equalsIgnoreCase("contraindicated")) {
                    af.setSerieStatus(SerieStatus.X);
                } else if(status.equalsIgnoreCase("recommended but not required")) {
                    af.setSerieStatus(SerieStatus.Z);
                }

            } catch (Exception e) {
                //TODO better error checking
                System.out.println("Unexpected dose status = " + status);
            }
        }
        return af;
        
    }

    
    
    /*
    public static boolean doesRecommendationHaveDateCriterion(ImmunizationRecommendationRecommendation irr) {

        if (irr.getDateCriterion() == null) {
            return false;
        }
        if (irr.getDateCriterion().isEmpty()) {
            return false;
        }
        return true;

    }
     */
    
    public static void main(String[] args) {
        org.hl7.fhir.dstu3.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent irr = null;
        
        
        //org.hl7.fhir.dstu3.model.Immunization imm = (org.hl7.fhir.dstu3.model.Immunization) irr.getSupportingImmunizationFirstRep();
        Reference ref = irr.getSupportingImmunizationFirstRep();
    
                
    }
    
    
}
