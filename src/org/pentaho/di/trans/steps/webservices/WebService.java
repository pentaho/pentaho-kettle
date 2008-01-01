package org.pentaho.di.trans.steps.webservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.webservices.wsdl.XsdType;

import com.ctc.wstx.exc.WstxParsingException;

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

        Object[] vCurrentRow = getRow();

        if (vCurrentRow != null)
        {
        	if (first)
        	{
        		first=false;
        		
        		data.outputRowMeta = getInputRowMeta().clone();
        		meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
        		
        		defineIndexList(getInputRowMeta(), vCurrentRow);
        		startXML();
        	}
            parseRow(getInputRowMeta(), vCurrentRow);

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

    private List<Integer> indexList;
    
    private void defineIndexList(RowMetaInterface rowMeta, Object[] vCurrentRow)
    {
        indexList = new ArrayList<Integer>();
        for (WebServiceField curField : meta.getFieldsIn())
        {
            int index = rowMeta.indexOfValue(curField.getName());
            if (index>=0)
            {
            	indexList.add(index);
            }
        }
    }
    
    private void parseRow(RowMetaInterface rowMeta, Object[] vCurrentRow) throws KettleValueException
    {
        if (meta.getInFieldArgumentName() != null)
        {
            xml.append("        <" + NS_PREFIX + ":").append(meta.getInFieldArgumentName()).append(">\n");
        }

        for (Integer index : indexList)
        {
            ValueMetaInterface vCurrentValue = rowMeta.getValueMeta(index);
            Object data = vCurrentRow[index];
            
            WebServiceField field = meta.getFieldInFromName(vCurrentValue.getName());
            if (field != null)
            {
                if (!vCurrentValue.isNull(data))
                {
                    xml.append("          <").append(NS_PREFIX).append(":").append(field.getWsName()).append(">");
                    if (XsdType.TIME.equals(field.getXsdType()))
                    {
                        // Allow to deal with hours like 36:12:12 (> 24h)
                        long millis = vCurrentValue.getDate(data).getTime() - dateRef.getTime();
                        xml.append(decFormat.format(millis / 3600000) + ":"
                                   + decFormat.format((millis % 3600000) / 60000)
                                   + ":"
                                   + decFormat.format(((millis % 60000) / 1000)));
                    }
                    else if (XsdType.DATE.equals(field.getXsdType()))
                    {
                        xml.append(dateFormat.format(vCurrentValue.getDate(data)));
                    }
                    else if (XsdType.DATE_TIME.equals(field.getXsdType()))
                    {
                        xml.append(dateHeureFormat.format(vCurrentValue.getDate(data)));
                    }
                    else if (vCurrentValue.isNumber())
                    {
                    	// TODO: To Fix !! This is very bad coding...
                    	//
                        xml.append(vCurrentValue.getString(data).trim().replace(',', '.'));
                    }
                    else
                    {
                        xml.append(ValueDataUtil.trim(vCurrentValue.getString(data)));
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

        // TODO We only manage one name space for all the elements. See in the
        // future how to manage multiple name spaces
        //
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
        // desactivation des logs
    	//
        Level saveLogLevel = Logger.getRootLogger().getLevel();
        Logger.getRootLogger().setLevel(Level.ERROR);

        HttpClient vHttpClient = new HttpClient();
        String vURLSansVariable = environmentSubstitute(meta.getUrl());
        String vURLService;
        int questionMarkIndex = vURLSansVariable.lastIndexOf("?");
        if (questionMarkIndex<0) vURLService = vURLSansVariable;
        else vURLService = vURLSansVariable.substring(0, questionMarkIndex);
        PostMethod vHttpMethod = new PostMethod(vURLService);
        HostConfiguration vHostConfiguration = new HostConfiguration();

        String httpLogin = environmentSubstitute(meta.getHttpLogin());
        if (httpLogin != null && !"".equals(httpLogin))
        {
            vHttpClient.getParams().setAuthenticationPreemptive(true);
            Credentials defaultcreds = new UsernamePasswordCredentials(httpLogin, environmentSubstitute(meta.getHttpPassword()));
            vHttpClient.getState().setCredentials(AuthScope.ANY, defaultcreds);
        }

        String proxyHost = environmentSubstitute(meta.getProxyHost());
        if (proxyHost != null && !"".equals(proxyHost))
        {
            vHostConfiguration.setProxy(proxyHost, Const.toInt(environmentSubstitute(meta.getProxyPort()), 8080));
        }

        try
        {
            vHttpMethod.setURI(new URI(vURLService, false));
            vHttpMethod.setRequestHeader("Content-Type", "text/xml;charset=UTF-8");
            vHttpMethod.setRequestHeader("SOAPAction", "\"" + meta.getOperationNamespace() + "/" + meta.getOperationName() + "\"");

            RequestEntity requestEntity = new ByteArrayRequestEntity(xml.toString().getBytes("UTF-8"), "UTF-8");
            vHttpMethod.setRequestEntity(requestEntity);
            long currentRequestTime = Const.nanoTime();
            int responseCode = vHttpClient.executeMethod(vHostConfiguration, vHttpMethod);
            if (responseCode == 200)
            {
                processRows(vHttpMethod.getResponseBodyAsStream());
            }
            else if (responseCode == 401)
            {
                throw new KettleStepException(Messages.getString("WebServices.ERROR0011.Authentication"));
            }
            else if (responseCode == 404)
            {
            	throw new KettleStepException(Messages.getString("WebServices.ERROR0012.NotFound"));
            }
            else
            {
            	throw new KettleStepException(Messages.getString("WebServices.ERROR0001.ServerError", Integer.toString(responseCode), Const.NVL(new String(vHttpMethod.getResponseBody()), "")) );
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

    private void processRows(InputStream anXml) throws KettleStepException
    {
    	// First we should get the complete string
    	// The problem is that the string can contain XML or any other format such as HTML saying the service is no longer available.
    	// We're talking about a WEB service here.
    	// As such, to keep the original parsing scheme, we first read the content.
    	// Then we create an input stream from the content again.
    	// It's elaborate, but that way we can report on the failure more correctly.
    	//
    	
		StringBuffer response = new StringBuffer();
		try
    	{
    		int c=anXml.read();
    		while (c>=0)
    		{
    			response.append((char)c);
    			c=anXml.read();
    		}
    		anXml.close();
    	}
    	catch(Exception e)
    	{
    		throw new KettleStepException("Unable to read web service response data from input stream", e);
    	}
    	
    	// Create a new reader to feed into the XML Input Factory below...
    	//
    	StringReader stringReader = new StringReader(response.toString());
    	
    	// TODO Very empirical : see if we can do something better here
        try
        {
            XMLInputFactory vFactory = XMLInputFactory.newInstance();
            XMLStreamReader vReader = vFactory.createXMLStreamReader(stringReader);
            
            Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
            int outputIndex = 0;
            
            boolean processing = false;
            boolean oneValueRowProcessing = false;
            for (int event = vReader.next(); vReader.hasNext(); event = vReader.next())
            {
                switch (event)
                {
                    case XMLStreamConstants.START_ELEMENT:
                    	
                    	// Start new code
                    	//START_ELEMENT= 1
                    	//
                        System.out.print("START_ELEMENT / ");
                        System.out.print(vReader.getAttributeCount());
                        System.out.print(" / ");
                        System.out.println(vReader.getNamespaceCount());
                        
                        // If we start the xml element named like the return type,
                        // we start a new row
                        //
                        System.out.print("vReader.getLocalName = ");
                        System.out.println(vReader.getLocalName());
                        if ( Const.isEmpty(meta.getOutFieldArgumentName()) )
                        {
                        	//getOutFieldArgumentName() == null
                        	if (oneValueRowProcessing)
                            {
                                WebServiceField field = meta.getFieldOutFromWsName(vReader.getLocalName());
                                if (field != null)
                                {
                                    outputRowData[outputIndex++] = getValue(vReader.getElementText(), field);
                                    putRow(data.outputRowMeta, outputRowData);
                                    oneValueRowProcessing = false;
                                }
                                else
                                {
                                	if (meta.getOutFieldContainerName().equals(vReader.getLocalName()))
                                	{
                                		// meta.getOutFieldContainerName() = vReader.getLocalName()
                                        System.out.print("OutFieldContainerName = ");
                                        System.out.println(meta.getOutFieldContainerName());
                                        oneValueRowProcessing = true;
                                	}
                                }
                            }
                        }
                        else
                        {
                        	//getOutFieldArgumentName() != null
                            System.out.print("OutFieldArgumentName = ");
                            System.out.println(meta.getOutFieldArgumentName());
                            if (meta.getOutFieldArgumentName().equals(vReader.getLocalName()))
                            {
                                System.out.print("vReader.getLocalName = ");
                                System.out.print("OutFieldArgumentName = ");
                                System.out.println(vReader.getLocalName());
                                if (processing)
                                {
                                	WebServiceField field = meta.getFieldOutFromWsName(vReader.getLocalName());
                                    if (field != null)
                                    {
                                    	int index = data.outputRowMeta.indexOfValue(field.getName());
                                        if (index>=0)
                                        {
                                            outputRowData[index] = getValue(vReader.getElementText(), field);
                                        }
                                    }
                                    processing = false;
                                }
                                else
                                {
                                	WebServiceField field = meta.getFieldOutFromWsName(vReader.getLocalName());
                                    if (meta.getFieldsOut().size() == 1 && field != null)
                                    {
                                    	// This can be either a simple return element, or a complex type...
                                    	//
                                    	try
                                    	{
                                    		outputRowData[outputIndex++] = getValue(vReader.getElementText(), field);
                                    		putRow(data.outputRowMeta, outputRowData);
                                    	}
                                    	catch(WstxParsingException e)
                                    	{
                                    		throw new KettleStepException("Unable to get value for field ["+field.getName()+"].  Verify that this is not a complex data type by looking at the response XML.", e);
                                    	}
                                    }
                                    else
                                    {
                                        for (WebServiceField curField : meta.getFieldsOut())
                                        {
                                            if ( !Const.isEmpty(curField.getName()) )
                                            {
                                            	outputRowData[outputIndex++] = getValue(vReader.getElementText(), curField);
                                            }
                                        }
                                        processing = true;
                                    }
                                }
                            	
                            }
                            else
                            {
                                System.out.print("vReader.getLocalName = ");
                                System.out.println(vReader.getLocalName());
                                System.out.print("OutFieldArgumentName = ");
                                System.out.println(meta.getOutFieldArgumentName());
                            }
                        }
                        break;
                        
                    case XMLStreamConstants.END_ELEMENT:
                    	//END_ELEMENT= 2
                        System.out.println("END_ELEMENT");
                        // If we end the xml element named as the return type, we
                        // finish a row
                        if ((meta.getOutFieldArgumentName() == null && meta.getOperationName().equals(vReader.getLocalName())))
                        {
                            oneValueRowProcessing = false;
                        }
                        else if (meta.getOutFieldArgumentName() != null && meta.getOutFieldArgumentName().equals(vReader.getLocalName()))
                        {
                            putRow(data.outputRowMeta, outputRowData);
                            processing = false;
                        }
                        break;
                    case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    	//PROCESSING_INSTRUCTION= 3
                        System.out.println("PROCESSING_INSTRUCTION");
                        break;
                    case XMLStreamConstants.CHARACTERS:
                    	//CHARACTERS= 4
                        System.out.println("CHARACTERS");
                        break;
                    case XMLStreamConstants.COMMENT:
                    	//COMMENT= 5
                        System.out.println("COMMENT");
                        break;
                    case XMLStreamConstants.SPACE:
                    	//PROCESSING_INSTRUCTION= 6
                        System.out.println("PROCESSING_INSTRUCTION");
                        break;
                    case XMLStreamConstants.START_DOCUMENT:
                    	//START_DOCUMENT= 7
                        System.out.println("START_DOCUMENT");
                        System.out.println(vReader.getText());
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                    	//END_DOCUMENT= 8
                        System.out.println("END_DOCUMENT");
                        break;
                    case XMLStreamConstants.ENTITY_REFERENCE:
                    	//ENTITY_REFERENCE= 9
                        System.out.println("ENTITY_REFERENCE");
                        break;
                    case XMLStreamConstants.ATTRIBUTE:
                    	//ATTRIBUTE= 10
                        System.out.println("ATTRIBUTE");
                        break;
                    case XMLStreamConstants.DTD:
                    	//DTD= 11
                        System.out.println("DTD");
                        break;
                    case XMLStreamConstants.CDATA:
                    	//CDATA= 12
                        System.out.println("CDATA");
                        break;
                    case XMLStreamConstants.NAMESPACE:
                    	//NAMESPACE= 13
                        System.out.println("NAMESPACE");
                        break;
                    case XMLStreamConstants.NOTATION_DECLARATION:
                    	//NOTATION_DECLARATION= 14
                        System.out.println("NOTATION_DECLARATION");
                        break;
                    case XMLStreamConstants.ENTITY_DECLARATION:
                    	//ENTITY_DECLARATION= 15
                        System.out.println("ENTITY_DECLARATION");
                        break;
                    default:
                        break;
                }
            }
        }
        catch (Exception e)
        {
            throw new KettleStepException(Messages.getString("WebServices.ERROR0010.OutputParsingError", response.toString()), e);
        }
    }
    
    private Object getValue(String vNodeValue, WebServiceField field) throws XMLStreamException, ParseException
    {
        if (vNodeValue == null)
        {
            return null;
        }
        else
        {
            if (XsdType.BOOLEAN.equals(field.getXsdType()))
            {
                return Boolean.valueOf(vNodeValue);
            }
            else if (XsdType.DATE.equals(field.getXsdType()))
            {
                try
                {
                    return dateFormat.parse(vNodeValue);
                }
                catch (ParseException e)
                {
                	System.out.println(Const.getStackTracker(e));
                    return null;
                }
            }
            else if (XsdType.TIME.equals(field.getXsdType()))
            {
                try
                {
                    return heureFormat.parse(vNodeValue);
                }
                catch (ParseException e)
                {
                	System.out.println(Const.getStackTracker(e));
                    return null;
                }
            }
            else if (XsdType.DATE_TIME.equals(field.getXsdType()))
            {
                try
                {
                    return dateHeureFormat.parse(vNodeValue);
                }
                catch (ParseException e)
                {
                	System.out.println(Const.getStackTracker(e));
                    return null;
                }
            }
            else if (XsdType.INTEGER.equals(field.getXsdType()) || XsdType.SHORT.equals(field.getXsdType()))
            {
                try
                {
                    return Integer.parseInt(vNodeValue);
                }
                catch (NumberFormatException e)
                {
                	System.out.println(Const.getStackTracker(e));
                    return null;
                }
            }
            else if (XsdType.FLOAT.equals(field.getXsdType()) || XsdType.DOUBLE.equals(field.getXsdType()))
            {
                try
                {
                    return Double.parseDouble(vNodeValue);
                }
                catch (NumberFormatException e)
                {
                	System.out.println(Const.getStackTracker(e));
                    return null;
                }
            }
            else if (XsdType.BINARY.equals(field.getXsdType()))
            {
                return Base64.decodeBase64(vNodeValue.getBytes());
            }
            else if (XsdType.DECIMAL.equals(field.getXsdType()))
            {
                return new BigDecimal(vNodeValue);
            }
            else
            {
                return vNodeValue;
            }
        }
    }
    
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("System.Log.StartingToRun")); //$NON-NLS-1$
			
			while (processRow(meta, data) && !isStopped());
		}
		catch(Throwable t)
		{
			logError(Messages.getString("System.Log.UnexpectedError")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(t));
            setErrors(1);
			stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}    
}