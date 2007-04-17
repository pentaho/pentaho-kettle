package be.ibridge.kettle.trans.step.webservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.webservices.wsdl.XsdType;

public class WebService extends BaseStep implements StepInterface
{
    public static final String NS_PREFIX = "ns";

    private WebServiceData data;

    private WebServiceMeta meta;

    private StringBuffer xml;

    private int nbRowProcess;

    private long requestTime;

    private SimpleDateFormat heureFormat = new SimpleDateFormat("HH:mm:ss");

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private SimpleDateFormat dateHeureFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private DecimalFormat decFormat = new DecimalFormat("00");

    private Date dateRef;

    public WebService(StepMeta aStepMeta, StepDataInterface aStepData, int value, TransMeta aTransMeta, Trans aTrans)
    {
        super(aStepMeta, aStepData, value, aTransMeta, aTrans);

        // Reference date used to format hours
        try
        {
            dateRef = heureFormat.parse("00:00:00");
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public boolean processRow(StepMetaInterface metaInterface, StepDataInterface dataInterface) throws KettleException
    {
        meta = (WebServiceMeta) metaInterface;
        data = (WebServiceData) dataInterface;

        Row vCurrentRow = getRow();
        if (nbRowProcess == 0)
        {
            startXML();
        }

        if (vCurrentRow != null)
        {
            parseRow(vCurrentRow);

            nbRowProcess++;
        }

        if ((vCurrentRow == null && (nbRowProcess % meta.getCallStep() != 0)) || (vCurrentRow != null && ((nbRowProcess > 0 && nbRowProcess % meta.getCallStep() == 0)))
            || (vCurrentRow == null && (!meta.hasFieldsIn())))
        {
            endXML();
            requestSOAP();

            startXML();
        }

        if (vCurrentRow == null)
        {
            setOutputDone();
        }
        return vCurrentRow != null;
    }

    private void parseRow(Row vCurrentRow)
    {
        if (meta.getInFieldArgumentName() != null)
        {
            xml.append("        <" + NS_PREFIX + ":").append(meta.getInFieldArgumentName()).append(">\n");
        }

        for (int i = 0; i < vCurrentRow.getFieldNames().length; ++i)
        {
            Value vCurrentValue = vCurrentRow.getValue(i);
            WebServiceField field = meta.getFieldInFromName(vCurrentValue.getName());
            if (field != null)
            {
                if (!vCurrentValue.isNull())
                {
                    xml.append("          <").append(NS_PREFIX).append(":").append(field.getWsName()).append(">");
                    if (XsdType.TIME.equals(field.getXsdType()))
                    {
                        // Allow to deal with hours like 36:12:12 (> 24h)
                        long millis = vCurrentValue.getDate().getTime() - dateRef.getTime();
                        xml.append(decFormat.format(millis / 3600000) + ":"
                                   + decFormat.format((millis % 3600000) / 60000)
                                   + ":"
                                   + decFormat.format(((millis % 60000) / 1000)));
                    }
                    else if (XsdType.DATE.equals(field.getXsdType()))
                    {
                        xml.append(dateFormat.format(vCurrentValue.getDate()));
                    }
                    else if (XsdType.DATE_TIME.equals(field.getXsdType()))
                    {
                        xml.append(dateHeureFormat.format(vCurrentValue.getDate()));
                    }
                    else if (vCurrentValue.getType() == Value.VALUE_TYPE_NUMBER)
                    {
                        xml.append(vCurrentValue.toString().trim().replace(',', '.'));
                    }
                    else
                    {
                        xml.append(vCurrentValue.toString().trim());
                    }
                    xml.append("</").append(NS_PREFIX).append(":").append(field.getWsName()).append(">\n");
                }
                else
                {
                    xml.append("          <").append(NS_PREFIX).append(":").append(field.getWsName()).append(" xsi:nil=\"true\"/>\n");
                }
            }
        }
        if (meta.getInFieldArgumentName() != null)
        {
            xml.append("        </" + NS_PREFIX + ":").append(meta.getInFieldArgumentName()).append(">\n");
        }
    }

    private void startXML()
    {
        xml = new StringBuffer();

        // TODO We only manage one namespace for all the elements. See in the
        // future how to manage multiple namespaces
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ns=\"");
        xml.append(meta.getOperationNamespace());
        xml.append("\">\n");

        xml.append("  <soapenv:Header/>\n");
        xml.append("  <soapenv:Body>\n");

        xml.append("    <" + NS_PREFIX + ":").append(meta.getOperationName()).append(">\n");
        if (meta.getInFieldContainerName() != null)
        {
            xml.append("      <" + NS_PREFIX + ":" + meta.getInFieldContainerName() + ">\n");
        }

    }

    private void endXML()
    {
        if (meta.getInFieldContainerName() != null)
        {
            xml.append("      </" + NS_PREFIX + ":" + meta.getInFieldContainerName() + ">\n");
        }
        xml.append("    </" + NS_PREFIX + ":").append(meta.getOperationName()).append(">\n");
        xml.append("  </soapenv:Body>\n");
        xml.append("</soapenv:Envelope>\n");
    }

    private void requestSOAP() throws KettleStepException
    {
        // désactivation des logs
        Level saveLogLevel = Logger.getRootLogger().getLevel();
        Logger.getRootLogger().setLevel(Level.ERROR);

        HttpClient vHttpClient = new HttpClient();
        String vURLSansVariable = StringUtil.environmentSubstitute(meta.getUrl());
        String vURLService = vURLSansVariable.substring(0, vURLSansVariable.lastIndexOf("?"));
        PostMethod vHttpMethod = new PostMethod(vURLService);
        HostConfiguration vHostConfiguration = new HostConfiguration();

        String httpLogin = StringUtil.environmentSubstitute(meta.getHttpLogin());
        if (httpLogin != null && !"".equals(httpLogin))
        {
            vHttpClient.getParams().setAuthenticationPreemptive(true);
            Credentials defaultcreds = new UsernamePasswordCredentials(httpLogin, StringUtil.environmentSubstitute(meta.getHttpPassword()));
            vHttpClient.getState().setCredentials(AuthScope.ANY, defaultcreds);
        }

        String proxyHost = StringUtil.environmentSubstitute(meta.getProxyHost());
        if (proxyHost != null && !"".equals(proxyHost))
        {
            vHostConfiguration.setProxy(proxyHost, Const.toInt(StringUtil.environmentSubstitute(meta.getProxyPort()), 8080));
        }

        try
        {
            vHttpMethod.setURI(new URI(vURLService, false));
            vHttpMethod.setRequestHeader("Content-Type", "text/xml;charset=UTF-8");
            vHttpMethod.setRequestHeader("SOAPAction", "\"" + meta.getOperationNamespace() + meta.getOperationName() + "\"");

            RequestEntity requestEntity = new ByteArrayRequestEntity(xml.toString().getBytes("UTF-8"), "UTF-8");
            vHttpMethod.setRequestEntity(requestEntity);
            long currentRequestTime = Const.nanoTime();
            int responseCode = vHttpClient.executeMethod(vHostConfiguration, vHttpMethod);
            if (responseCode == 200)
            {
                processRows(vHttpMethod.getResponseBodyAsStream());
            }
            else
            {
                logError(new String(vHttpMethod.getResponseBody()));
                throw new KettleStepException(Messages.getString("WebServices.ERROR0001.ServerError"));
            }
            requestTime += Const.nanoTime() - currentRequestTime;
        }
        catch (URIException e)
        {
            throw new KettleStepException(Messages.getString("WebServices.ERROR0002.InvalidURI"));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new KettleStepException(Messages.getString("WebServices.ERROR0003.UnsupportedEncoding"));
        }
        catch (HttpException e)
        {
            throw new KettleStepException(Messages.getString("WebServices.ERROR0004.HttpException"));
        }
        catch (UnknownHostException e)
        {
            throw new KettleStepException(Messages.getString("WebServices.ERROR0011.UnknownHost"));
        }
        catch (IOException e)
        {
            throw new KettleStepException(Messages.getString("WebServices.ERROR0005.IOException"));
        }
        finally
        {
            vHttpMethod.releaseConnection();
        }

        Logger.getRootLogger().setLevel(saveLogLevel);

    }

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (WebServiceMeta) smi;
        data = (WebServiceData) sdi;

        return super.init(smi, sdi);
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (WebServiceMeta) smi;
        data = (WebServiceData) sdi;

        super.dispose(smi, sdi);
    }

    public void run()
    {

        nbRowProcess = 0;
        logBasic("Starting to run...");
        try
        {
            requestTime = 0;
            while (processRow(meta, data) && !isStopped())
                ;
        }
        catch (Exception e)
        {
            logError("Unexpected error in : " + e.toString());
            setErrors(1);
            stopAll();
        }
        finally
        {
            dispose(meta, data);
            logBasic("Finished, processing " + linesRead + " rows");
            markStop();
        }
    }

    private void processRows(InputStream anXml) throws KettleStepException
    {
        // TODO Very empirical : see if we can do something better here
        try
        {
            XMLInputFactory vFactory = XMLInputFactory.newInstance();
            XMLStreamReader vReader = vFactory.createXMLStreamReader(anXml);
            Row r = null;
            boolean processing = false;
            boolean oneValueRowProcessing = false;
            for (int event = vReader.next(); event != XMLStreamConstants.END_DOCUMENT; event = vReader.next())
            {
                switch (event)
                {
                    case XMLStreamConstants.START_ELEMENT:
                        // If we start the xml element named like the return type,
                        // we start a new row
                        if ((meta.getOutFieldArgumentName() == null && meta.getOutFieldContainerName().equals(vReader.getLocalName())))
                        {
                            oneValueRowProcessing = true;
                        }
                        else if (meta.getOutFieldArgumentName() != null && meta.getOutFieldArgumentName().equals(vReader.getLocalName()))
                        {
                            r = new Row();
                            WebServiceField field = meta.getFieldOutFromWsName(vReader.getLocalName());
                            if (meta.getFieldsOut().size() == 1 && field != null)
                            {
                                Value value = new Value(field.getName(), field.getType());
                                setValue(vReader.getElementText(), value, field);
                                r.addValue(value);
                                putRow(r);
                            }
                            else
                            {
                                for (Iterator itrField = meta.getFieldsOut().iterator(); itrField.hasNext();)
                                {
                                    WebServiceField curField = (WebServiceField) itrField.next();
                                    r.addValue(new Value(curField.getName(), curField.getType()));
                                }
                                processing = true;
                            }
                        }
                        else if (processing)
                        {
                            WebServiceField field = meta.getFieldOutFromWsName(vReader.getLocalName());
                            if (field != null)
                            {
                                setValue(vReader.getElementText(), r.searchValue(field.getName()), field);
                            }
                        }
                        else if (oneValueRowProcessing)
                        {
                            WebServiceField field = meta.getFieldOutFromWsName(vReader.getLocalName());
                            if (field != null)
                            {
                                r = new Row();
                                Value value = new Value(field.getName(), field.getType());
                                setValue(vReader.getElementText(), value, field);
                                r.addValue(value);
                                putRow(r);
                            }
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        // If we end the xml element named as the return type, we
                        // finish a row
                        if ((meta.getOutFieldArgumentName() == null && meta.getOperationName().equals(vReader.getLocalName())))
                        {
                            oneValueRowProcessing = false;
                        }
                        else if (meta.getOutFieldArgumentName() != null && meta.getOutFieldArgumentName().equals(vReader.getLocalName()))
                        {
                            putRow(r);
                            processing = false;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        catch (Exception e)
        {
            //System.out.println(xml.toString());
            throw new KettleStepException(Messages.getString("WebServices.ERROR0010.OutputParsingError"), e);
        }
    }
    
    private void setValue(String vNodeValue, Value value, WebServiceField field) throws XMLStreamException, ParseException
    {
        if (vNodeValue == null)
        {
            value.setNull();
        }
        else
        {
            if (XsdType.BOOLEAN.equals(field.getXsdType()))
            {
                value.setValue(Boolean.valueOf(vNodeValue));
            }
            else if (XsdType.DATE.equals(field.getXsdType()))
            {
                try
                {
                    value.setValue(dateFormat.parse(vNodeValue));
                }
                catch (ParseException e)
                {
                    value.setNull();
                }
            }
            else if (XsdType.TIME.equals(field.getXsdType()))
            {
                try
                {
                    value.setValue(heureFormat.parse(vNodeValue));
                }
                catch (ParseException e)
                {
                    value.setNull();
                }
            }
            else if (XsdType.DATE_TIME.equals(field.getXsdType()))
            {
                value.setValue(dateHeureFormat.parse(vNodeValue));
            }
            else if (XsdType.INTEGER.equals(field.getXsdType()) || XsdType.SHORT.equals(field.getXsdType()))
            {
                try
                {
                    value.setValue(Integer.parseInt(vNodeValue));
                }
                catch (NumberFormatException nfe)
                {
                    value.setNull();
                }
            }
            else if (XsdType.FLOAT.equals(field.getXsdType()) || XsdType.DOUBLE.equals(field.getXsdType()))
            {
                try
                {
                    value.setValue(Double.parseDouble(vNodeValue));
                }
                catch (NumberFormatException nfe)
                {
                    value.setNull();
                }
            }
            else if (XsdType.BINARY.equals(field.getXsdType()))
            {
                value.setValue(Base64.decodeBase64(vNodeValue.getBytes()));
            }
            else
            {
                value.setValue(vNodeValue);
            }
        }
    }
}
