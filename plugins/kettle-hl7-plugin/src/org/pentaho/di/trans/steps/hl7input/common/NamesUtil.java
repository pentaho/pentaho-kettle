package org.pentaho.di.trans.steps.hl7input.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NamesUtil {
  
  private Map<String, List<String>> map;
  
  private static NamesUtil util;
  
  public static NamesUtil getInstance() {
    if (util==null) {
      util = new NamesUtil();
    }
    
    return util;
  }
  
  private NamesUtil() {
    map = new HashMap<String, List<String>>();
  
    populateHD();
    populateDT();
    populateID();
    populateIS();
    populateSI();
    populateVID();
    populatePT();
    populateMSG();
    populateTS();
    populateST();
    populateXAD();
    populateCE();
    populateCX();
    populateXPN();
    populateXCN();
    populateXTN();
    populateXON();
    populatePL();
    populatePTA();
    populateRMC();
    populateEI();
    populateFC();
    populateCK();
    populateCM_MSG();
    populateCM_PAT_ID();
    populatePN();
    populateCN();
    populateTN();
    populateNM();
    populateAD();
    populateCQ();
    populateCM_LICENSE_NO();
    populateCM_INTERNAL_LOCATION();
  }

  private void populateST() {
    List<String> list = new ArrayList<String>();
    list.add("single value");
    map.put("ST", list);
  }

  private void populateTN() {
    List<String> list = new ArrayList<String>();
    list.add("telephone number");
    map.put("TN", list);
  }
  
  private void populateCQ() {
    List<String> list = new ArrayList<String>();
    list.add("quantity");
    list.add("units");
    map.put("CQ", list);
  }

  private void populateCM_INTERNAL_LOCATION() {
    List<String> list = new ArrayList<String>();
    list.add("nurse unit (Station)");
    list.add("Room");
    list.add("Bed");
    list.add("Facility ID");
    list.add("Bed Status");
    list.add("Etage");
    list.add("Klinik");
    list.add("Zentrum");
    map.put("CM_INTERNAL_LOCATION", list);
  }

  private void populateCM_LICENSE_NO() {
    List<String> list = new ArrayList<String>();
    list.add("License Number");
    list.add("issuing state,province,country");
    map.put("CM_LICENSE_NO", list);
  }


  private void populateNM() {
    List<String> list = new ArrayList<String>();
    list.add("name");
    map.put("NM", list);
  }


  private void populateAD() {
    List<String> list = new ArrayList<String>();
    list.add("street address");
    list.add("other designation");
    list.add("city");
    list.add("state or province");
    list.add("zip or postal code");
    list.add("country");
    list.add("address type");
    list.add("other geographic designation");
    map.put("AD", list);
  }

  private void populateFC() {
    List<String> list = new ArrayList<String>();
    list.add("Financial Class");
    list.add("Effective Date");
    map.put("FC", list);
  }
  
  private void populateCN() {
    List<String> list = new ArrayList<String>();
    list.add("ID number");
    list.add("family name");
    list.add("given name");
    list.add("second and further given names or initials thereof");
    list.add("suffix (e.g., JR or III)");
    list.add("prefix (e.g., DR)");
    list.add("degree (e.g., MD)");
    list.add("source table");
    list.add("assigning authority");
    map.put("CN", list);
  }
  
  
  private void populatePN() {
    List<String> list = new ArrayList<String>();
    list.add("family name");
    list.add("given name");
    list.add("second and further given names or initials thereof");
    list.add("suffix (e.g., JR or III)");
    list.add("prefix (e.g., DR)");
    list.add("degree (e.g., MD)");
    map.put("PN", list);
  }
  

  private void populateCM_PAT_ID() {
    List<String> list = new ArrayList<String>();
    list.add("ID number");
    list.add("Check digit");
    list.add("Check digit scheme");
    list.add("Facility ID");
    list.add("type");
    map.put("CM_PAT_ID", list);
  }



  private void populateCM_MSG() {
    List<String> list = new ArrayList<String>();
    list.add("message type");
    list.add("Trigger Event");
    map.put("CM_MSG", list);
  }

  private void populateCK() {
    List<String> list = new ArrayList<String>();
    list.add("ID number");
    list.add("check digit");
    list.add("code identifying the check digit scheme employed");
    list.add("assigning authority");
    map.put("CK", list);
  }
  
  private void populateEI() {
    List<String> list = new ArrayList<String>();
    list.add("entity identifier");
    list.add("namespace ID");
    list.add("universal ID");
    list.add("universal ID type");
    map.put("EI", list);
  }
  
  
  private void populatePTA() {
    List<String> list = new ArrayList<String>();
    list.add("policy type");
    list.add("amount class");
    list.add("amount");
    map.put("PTA", list);
  }
  
  private void populateRMC() {
    List<String> list = new ArrayList<String>();
    list.add("room type");
    list.add("amount type");
    list.add("coverage amount");
    map.put("RMC", list);
  }

  private void populateDT() {
    List<String> list = new ArrayList<String>();
    list.add("date-value");
    map.put("DT", list);
  }

  private void populateID() {
    List<String> list = new ArrayList<String>();
    list.add("single value");
    map.put("ID", list);
  }

  private void populateIS() {
    List<String> list = new ArrayList<String>();
    list.add("code");
    list.add("description");
    map.put("IS", list);
  }

  private void populateSI() {
    List<String> list = new ArrayList<String>();
    list.add("single value");
    map.put("SI", list);
  }

  private void populateCX() {
    List<String> list = new ArrayList<String>();
    list.add("ID");
    list.add("check digit");
    list.add("code identifying the check digit scheme employed");
    list.add("assigning authority");
    list.add("identifier type code");
    list.add("assigning facility");
    list.add("effective date");
    list.add("expiration date");
    map.put("CX", list);
  }
  
  private void populatePT() {
    List<String> list = new ArrayList<String>();
    list.add("processing ID");
    list.add("processing mode");
    map.put("PT", list);
  }

  private void populateVID() {
    List<String> list = new ArrayList<String>();
    list.add("version ID");
    list.add("internationalization code");
    list.add("international version ID");
    map.put("VID", list);
  }

  
  private void populateMSG() {
    List<String> list = new ArrayList<String>();
    list.add("message type");
    list.add("trigger event");
    list.add("message structure");
    map.put("MSG", list);
  }
  
  private void populateTS() {
    List<String> list = new ArrayList<String>();
    list.add("time of an event");
    list.add("degree of precision");
    map.put("TS", list);
  }

  private void populateHD() {
    List<String> list = new ArrayList<String>();
    list.add("namespace ID");
    list.add("universal ID");
    list.add("universal ID type");
    map.put("HD", list);
  }
  
  private void populateXAD() {
    List<String> list = new ArrayList<String>();
    list.add("street address");
    list.add("other designation");
    list.add("city");
    list.add("state or province");
    list.add("zip or postal code");
    list.add("country");
    list.add("address type");
    list.add("other geographic designation");
    list.add("county/parish code");
    list.add("census tract");
    list.add("address representation code");
    list.add("address validity range");
    map.put("XAD", list);
  }

  private void populateCE() {
    List<String> list = new ArrayList<String>();
    list.add("identifier");
    list.add("text");
    list.add("name of coding system");
    list.add("alternate identifier");
    list.add("alternate text");
    list.add("name of alternate coding system");
    map.put("CE", list);
  }
  
  private void populateXPN() {
    List<String> list = new ArrayList<String>();
    list.add("family name");
    list.add("given name");
    list.add("second and further given names or initials thereof");
    list.add("suffix (e.g., JR or III)");
    list.add("prefix (e.g., DR)");
    list.add("degree (e.g., MD)");
    list.add("name type code");
    list.add("Name Representation code");
    list.add("name context");
    list.add("name validity range");
    list.add("name assembly order");
    map.put("XPN", list);
  }
  
  private void populateXCN() {
    List<String> list = new ArrayList<String>();
    list.add("family name");
    list.add("ID number");
    list.add("family name");
    list.add("given name");
    list.add("second and further given names or initials thereof");
    list.add("suffix (e.g., JR or III)");
    list.add("prefix (e.g., DR)");
    list.add("degree (e.g., MD)");
    list.add("source table");
    list.add("assigning authority");
    list.add("name type code");
    list.add("identifier check digit");
    list.add("code identifying the check digit scheme employed");
    list.add("identifier type code");
    list.add("assigning facility");
    list.add("Name Representation code");
    list.add("name context");
    list.add("name validity range");
    list.add("name assembly order");
    map.put("XCN", list);
  }

  
  private void populatePL() {
    List<String> list = new ArrayList<String>();
    list.add("family name");
    list.add("point of care");
    list.add("room");
    list.add("bed");
    list.add("facility");
    list.add("location status");
    list.add("person location type");
    list.add("building");
    list.add("floor");
    list.add("Location description");
    map.put("PL", list);
  }

  private void populateXTN() {
    List<String> list = new ArrayList<String>();
    list.add("[(999)] 999-9999 [X99999][C any text]");
    list.add("telecommunication use code");
    list.add("telecommunication equipment type");
    list.add("Email address");
    list.add("Country Code");
    list.add("Area/city code");
    list.add("Phone number");
    list.add("Extension");
    list.add("any text");
    map.put("XTN", list);
  }
  
  private void populateXON() {
    List<String> list = new ArrayList<String>();
    list.add("organization name");
    list.add("organization name type code");
    list.add("ID number");
    list.add("check digit");
    list.add("code identifying the check digit scheme employed");
    list.add("assigning authority");
    list.add("identifier type code");
    list.add("assigning facility ID");
    list.add("Name Representation code");
    map.put("XON", list);
  }
  
  public Map<String, List<String>> getMap() {
    return map;
  }
  
}
