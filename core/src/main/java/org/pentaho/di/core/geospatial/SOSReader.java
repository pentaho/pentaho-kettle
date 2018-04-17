package org.pentaho.di.core.geospatial;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.xml.sax.InputSource;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @author shudongping
 * @date 2018/04/17
 */
public class SOSReader {

    private URL 				SOSUrl;
    private URL 				urlObs;
    private String 				method;
    private String 				SOSVersion;
    private ArrayList<Element>	parseResult;
    private DateFormat 			dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public SOSReader(URL url, String method, String version) throws KettleException{
        this.SOSUrl = url;
        this.method = method;
        this.SOSVersion = version;
    }

    public void setVersion(String version){
        this.SOSVersion=version;
    }

    public String getVersion(){
        return SOSVersion;
    }

    public void setURLObs(URL urlObs){
        this.urlObs=urlObs;
    }

    public URL getURLObs(){
        return urlObs;
    }

    public void setMethod(String method){
        this.method=method;
    }

    public String getMethod(){
        return method;
    }

    public void setURL(URL url){
        this.SOSUrl=url;
    }

    public URL getURL(){
        return SOSUrl;
    }

    public String getCapabilities(String [] sections) throws KettleException{
        if(method.equals("GET"))
            return SOSGet(buildGetCapabilitiesGetQuery(sections));
        else
            return SOSPost(buildGetCapabilitiesPostQuery(sections), SOSUrl);
    }

    public ArrayList<Object[]> getObservations(String offering, String[] procedures, String time1, String time2, String[] observedProperties) throws KettleException{
        String response;
        if(method.equals("GET"))
            response = SOSGet(buildGetObsGetQuery(offering, procedures, time1, time2, observedProperties));
        else
            response =  SOSPost(buildGetObsPostQuery(offering, procedures, time1, time2, observedProperties), urlObs);
        return getObservationRows(response, offering);
    }

    public String[] getOfferings() throws KettleException{
        ArrayList<Element> offeringElements = getOffs(getCapabilities(new String [] {"Contents"}));
        String[] offerings = new String[offeringElements.size()];
        for(int i = 0 ;i<offeringElements.size();i++){
            offerings[i] = getAttributeValue(offeringElements.get(i), "id", "http://www.opengis.net/gml");
        }
        return offerings;
    }

    public String[] getProcedures(String offering) throws KettleException{
        ArrayList<Element> proceduresElements = getProcs(getCapabilities(new String [] {"Contents"}), offering);
        String[] procedures = new String[proceduresElements.size()];
        for(int i = 0 ;i<proceduresElements.size();i++){
            procedures[i] = getAttributeValue(proceduresElements.get(i), "href", "http://www.w3.org/1999/xlink");
        }
        return procedures;
    }

    public String[] getObservedProperties(String offering) throws KettleException{
        ArrayList<Element> observedPropertyElements = getObsProperties(getCapabilities(new String [] {"Contents"}), offering);
        String[] observedProperties = new String[observedPropertyElements.size()];
        for(int i = 0 ;i<observedPropertyElements.size();i++){
            observedProperties[i] = getAttributeValue(observedPropertyElements.get(i), "href", "http://www.w3.org/1999/xlink");
        }
        return observedProperties;
    }

    private String buildGetCapabilitiesGetQuery(String [] sections){
        StringBuffer sb = new StringBuffer(SOSUrl.toString());
        sb.append("?");
        sb.append("service=SOS");
        sb.append("&request=getcapabilities");
        sb.append("&acceptversions=");
        sb.append(SOSVersion);
        sb.append("&sections=");
        for (int i=0;i<sections.length;i++){
            sb.append(sections[i]);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    private String buildGetCapabilitiesPostQuery(String [] sections){
        String query = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        query += "<GetCapabilities service=\"SOS\" ";
        query += "xmlns=\"http://www.opengis.net/sos/1.0\" ";
        query += "xmlns:ows=\"http://www.opengis.net/ows/1.1\" ";
        query += "xmlns:ogc=\"http://www.opengis.net/ogc\" ";
        query += "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ";
        query += "xsi:schemaLocation=\"http://www.opengis.net/sos/1.0";
        query += " http://schemas.opengis.net/sos/1.0.0/sosGetCapabilities.xsd\">";
        query += "<ows:AcceptVersions>";
        query += "<ows:Version>"+SOSVersion+"</ows:Version>";
        query += "</ows:AcceptVersions>";
        query += "<ows:Sections>";
        for (int i=0;i<sections.length;i++){
            query += "<ows:Section>"+sections[i]+"</ows:Section>";
        }
        query += "</ows:Sections>";
        query += "</GetCapabilities>";
        return query;
    }

    private String buildGetObsGetQuery(String offering, String[] procedures, String time1, String time2, String[] observedProperties){
        String query = urlObs.toString();
        query += "?";
        query += "service=SOS";
        query += "&version="+SOSVersion;
        query += "&request=getobservation";
        query += "&offering="+offering;
        if (!Const.isEmpty(time1) || !Const.isEmpty(time2)){
            query += "&eventtime=";
            if(!Const.isEmpty(time1) && !Const.isEmpty(time2))
                query += time1+"/"+time2;
            else {
                query += !Const.isEmpty(time1) ? time1:time2;
            }
        }
        if(procedures!=null){
            query += "&procedure=";
            for (int i = 0; i<procedures.length;i++){
                query += i>=1 ? ","+procedures[i]:procedures[i];
            }
        }
        query += "&observedProperty=";
        for (int i = 0; i<observedProperties.length;i++){
            if (i>=1)
                query += ","+observedProperties[i];
            else
                query += observedProperties[i];
        }
        query += "&responseformat=text/xml;subtype=\"om/1.0.0\"";
        query += "&resultModel=om:Measurement";
        return query;
    }

    private String buildGetObsPostQuery(String offering, String[] procedures, String time1, String time2, String[] observedProperties){
        String query = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        query += "<GetObservation service=\"SOS\" version=\""+SOSVersion+"\" ";
        query += "xmlns=\"http://www.opengis.net/sos/1.0\" ";
        query += "xmlns:ows=\"http://www.opengis.net/ows/1.1\" ";
        query += "xmlns:ogc=\"http://www.opengis.net/ogc\" ";
        query += "xmlns:om=\"http://www.opengis.net/om/1.0\" ";
        query += "xmlns:sos=\"http://www.opengis.net/sos/1.0\" ";
        query += "xmlns:gml=\"http://www.opengis.net/gml\" ";;
        query += "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ";
        query += "xsi:schemaLocation=\"http://www.opengis.net/sos/1.0";
        query += " http://schemas.opengis.net/sos/1.0.0/sosGetObservation.xsd\">";
        query += "<offering>"+offering+"</offering>";
        if (!Const.isEmpty(time1) || !Const.isEmpty(time2)){
            query += "<eventTime>";
            if(!Const.isEmpty(time1) && !Const.isEmpty(time2)){
                query += "<ogc:TM_During><ogc:PropertyName>om:samplingTime</ogc:PropertyName><gml:TimePeriod>";
                query += "<gml:beginPosition>"+time1+"</gml:beginPosition>";
                query += "<gml:endPosition>"+time2+"</gml:endPosition>";
                query += "</gml:TimePeriod></ogc:TM_During>";
            }else {
                query += "<ogc:TM_Equals><ogc:PropertyName>om:samplingTime</ogc:PropertyName><gml:TimeInstant><gml:timePosition>";
                query += !Const.isEmpty(time1) ? time1:time2;
                query += "</gml:timePosition></gml:TimeInstant></ogc:TM_Equals>";
            }
            query += "</eventTime>";
        }
        if(procedures!=null){
            for (int i = 0; i<procedures.length;i++){
                query += "<procedure>"+procedures[i]+"</procedure>";
            }
        }
        for (int i = 0; i<observedProperties.length;i++){
            query += "<observedProperty>"+observedProperties[i]+"</observedProperty>";
        }
        query += "<responseFormat>text/xml;subtype=&quot;om/1.0.0&quot;</responseFormat>";
        query += "<resultModel>om:Measurement</resultModel>";
        query += "</GetObservation>";
        return query;
    }


    private String SOSGet(String query) throws KettleException{
        HttpMethod httpMethod = new GetMethod(query);
        try {
            //Prepare HTTP Get
            HttpClient httpclient = new HttpClient();

            //Execute request
            httpclient.executeMethod(httpMethod);

            // the response
            InputStream inputStream = httpMethod.getResponseBodyAsStream();
            StringBuffer bodyBuffer = new StringBuffer();
            int c;
            while ( (c=inputStream.read())!=-1) bodyBuffer.append((char)c);
            inputStream.close();

            return bodyBuffer.toString();
        }catch (IOException e) {
            throw new KettleException("Error connecting to SOS...", e);
        }finally{
            httpMethod.releaseConnection();
        }
    }

    private String SOSPost(String query, URL url) throws KettleException{
        try {
            // Send request
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"");
            conn.setDoOutput(true);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(query);
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = "";
            String line;
            while ((line = rd.readLine()) != null) response += line;
            wr.close();
            rd.close();
            return response;
        }catch (Exception e) {
            throw new KettleException("Error connecting to SOS...", e);
        }
    }

    private ArrayList<Element> getOffs(String response) throws KettleException{
        SAXBuilder parser = new SAXBuilder();
        try {
            return findElement(parser.build(new InputSource(new StringReader(response))).getRootElement(), "ObservationOffering");
        } catch (Exception e) {
            throw new KettleException("Error parsing SOS capabilities...", e);
        }
    }

    private ArrayList<Element> getProcs(String response, String offering) throws KettleException{
        SAXBuilder parser = new SAXBuilder();
        ArrayList<Element> procedureElements = null;
        try {
            ArrayList<Element> offerings = findElement(parser.build(new InputSource(new StringReader(response))).getRootElement(), "ObservationOffering");
            for(Element offeringEl:offerings){
                if (getAttributeValue(offeringEl, "id", "http://www.opengis.net/gml").equals(offering))
                    procedureElements = findElement(offeringEl, "procedure");
            }
        } catch (Exception e) {
            throw new KettleException("Error parsing SOS capabilities...", e);
        }
        return procedureElements;
    }

    private ArrayList<Element> getObsProperties(String response, String offering) throws KettleException{
        SAXBuilder parser = new SAXBuilder();
        ArrayList<Element> observedPropertyElement = null;
        try {
            ArrayList<Element> offerings = findElement(parser.build(new InputSource(new StringReader(response))).getRootElement(), "ObservationOffering");
            for(Element offeringEl:offerings){
                if (getAttributeValue(offeringEl, "id", "http://www.opengis.net/gml").equals(offering))
                    observedPropertyElement = findElement(offeringEl, "observedProperty");
            }
        } catch (Exception e) {
            throw new KettleException("Error parsing SOS capabilities...", e);
        }
        return observedPropertyElement;
    }

    private ArrayList<Element> findElement(Element element, String elementName)throws ServletException, IOException{

        parseResult = new ArrayList<Element>();

        // Traverse the tree
        recurse_findElement(element.getChildren(), elementName);

        return parseResult;
    }

    private void recurse_findElement(List<?> elements, String elementName)throws IOException{
        // Cycle through all the child nodes of the root
        Iterator<?> iter = elements.iterator();
        while (iter.hasNext()){
            Element el = (Element) iter.next();

            if (el.getName().equals(elementName)) parseResult.add((Element) el);

            // If the node has children, call this method with the node
            if (el.getChildren()!=null) recurse_findElement(el.getChildren(), elementName);
        }
    }

    private String getAttributeValue(Element element, String attributeName, String namespace){
        return element.getAttributeValue(attributeName, Namespace.getNamespace(namespace));
    }

    private ArrayList<Object[]> getObservationRows(String response, String offering) throws KettleException{
        SAXBuilder parser = new SAXBuilder();
        ArrayList<Element> measurementElements;
        try {
            Document doc = parser.build(new InputSource(new StringReader(response)));
            measurementElements = findElement(doc.getRootElement(), "Measurement");
            if (measurementElements.isEmpty()) return null;
        } catch (Exception e) {
            throw new KettleException("Error parsing SOS response...", e);
        }

        ArrayList<Object[]> rows = new ArrayList<Object[]>();

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        WKTReader reader = new WKTReader(geometryFactory);

        for (Element measurementEl : measurementElements){
            try {
                String timeStr = findElement(measurementEl,"timePosition").get(0).getValue();
                Date time = dateFormat.parse((timeStr.substring(0, timeStr.lastIndexOf(":"))).concat(timeStr.substring(timeStr.lastIndexOf(":")+1)));

                String procedure = getAttributeValue(findElement(measurementEl,"procedure").get(0), "href", "http://www.w3.org/1999/xlink");

                String observedProperty = getAttributeValue(findElement(measurementEl,"observedProperty").get(0), "href", "http://www.w3.org/1999/xlink");

                //////////////////////Feature of interest//////////////////////
                ArrayList<Element> featureOfInterestElements = findElement(measurementEl,"featureOfInterest");

                boolean hasGeometry = featureOfInterestElements.isEmpty()?false:true;

                String featureID = null;
                String featureName = null;
                Geometry featureGeom = null;

                if (hasGeometry){
                    Element featureOfInterestEl = featureOfInterestElements.get(0);

                    Element samplingEl;
                    String geomPrimitive;
                    String geomWKT;
                    String srs;
                    int srsInt;

                    //Currently supports sampling points and sampling surface (gml) only
                    if(!findElement(featureOfInterestEl,"SamplingPoint").isEmpty()){
                        geomPrimitive = "POINT";
                        samplingEl = findElement(featureOfInterestEl,"SamplingPoint").get(0);
                        featureID = getAttributeValue(samplingEl,"id","http://www.opengis.net/gml");
                        featureName = findElement(samplingEl,"name").get(0).getValue();

                        geomWKT = geomPrimitive + " (";
                        geomWKT += findElement(samplingEl,"pos").get(0).getValue();
                        geomWKT += ")";

                        srs = getAttributeValue(findElement(samplingEl,"pos").get(0), "srsName", null);
                        srsInt = Integer.parseInt(srs.substring(srs.lastIndexOf(":")+1, srs.length()));
                        featureGeom = (Geometry) reader.read(geomWKT);
                        featureGeom.setSRID(srsInt);
                    }else if(!findElement(featureOfInterestEl,"SamplingSurface").isEmpty()){
                        geomPrimitive = "MULTIPOLYGON";
                        samplingEl = findElement(featureOfInterestEl,"SamplingSurface").get(0);
                        featureID = getAttributeValue(samplingEl,"id","http://www.opengis.net/gml");
                        featureName = findElement(samplingEl,"name").get(0).getValue();

                        ArrayList<Element> polygonElements = findElement(samplingEl,"Polygon");
                        geomWKT = geomPrimitive + " (((";
                        for(Element polygonEl:polygonElements){
                            Element exteriorRingEl = findElement(polygonEl,"exterior").get(0);
                            geomWKT += findElement(exteriorRingEl,"coordinates").get(0).getValue();
                            geomWKT += ")";
                            if (!findElement(polygonEl,"interior").isEmpty()){
                                ArrayList<Element> interiorRingElements = findElement(polygonEl,"interior");
                                for(Element interiorRingEl:interiorRingElements){
                                    geomWKT += ",(";
                                    geomWKT += findElement(interiorRingEl,"coordinates").get(0).getValue();
                                    geomWKT += ")";
                                }
                            }
                            geomWKT += ")";
                        }
                        geomWKT += ")";
                        srs = getAttributeValue(findElement(samplingEl,"Polygon").get(0), "srsName", null);
                        srsInt = Integer.parseInt(srs.substring(srs.lastIndexOf(":")+1, srs.length()));
                        featureGeom = (Geometry) reader.read(geomWKT);
                        featureGeom.setSRID(srsInt);
                    }
                }
                //////////////////////////////////////////////////////////////////

                Element resultEl = findElement(measurementEl,"result").get(0);
                String uom = getAttributeValue(resultEl, "uom", null);
                Double measure = Double.parseDouble(resultEl.getValue());

                Object[] r = new Object[9];
                r[0]= procedure;
                r[1]= offering;
                r[2]= observedProperty;
                r[3]= time;
                r[4]= featureID;
                r[5]= featureName;
                r[6]= featureGeom;
                r[7]= measure;
                r[8]= uom;

                rows.add(r);
            }catch (Exception e){
                throw new KettleException("Error parsing SOS response...", e);
            }
        }
        return rows;
    }

}
