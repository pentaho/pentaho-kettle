
package be.ibridge.kettle.trans.step.webservices;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.webservices.wsdl.DOMParsing;
import be.ibridge.kettle.trans.step.webservices.wsdl.WSDLArgument;
import be.ibridge.kettle.trans.step.webservices.wsdl.WSDLOperation;
import be.ibridge.kettle.trans.step.webservices.wsdl.WSDLParameter;
import be.ibridge.kettle.trans.step.webservices.wsdl.WSDLService;
import be.ibridge.kettle.trans.step.webservices.wsdl.XsdType;

/**
 * 
 * @author bonneau
 *
 */
public class WebServicePlugin extends BaseStep implements StepInterface
{
	private WebServicePluginData data;
	
	private WebServicePluginMeta meta;
	
	private StringBuffer xml;
	
	private int nbRowProcess;
	
	private WSDLOperation vWSDLOperation = null;
	
	private Map/*<String, String>*/ mapParameters = new HashMap/*<String, String>*/();
	
	private Map/*<String, String>*/ mapType = new HashMap/*<String, String>*/();
	
	private final static int STEP = 10000;
	
	// private long processingTime;
	
	private long requestTime;
	
	private SimpleDateFormat heureFormat = new SimpleDateFormat("HH:mm:ss");
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	private SimpleDateFormat dateHeureFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	
	public WebServicePlugin(StepMeta aStepMeta, StepDataInterface aStepData, int value, TransMeta aTransMeta, Trans aTrans) 
	{
		super(aStepMeta, aStepData, value, aTransMeta, aTrans);
	}

	public boolean processRow(StepMetaInterface metaInterface, StepDataInterface dataInterface) throws KettleException 
	{
		meta=(WebServicePluginMeta)metaInterface;
		data=(WebServicePluginData)dataInterface;

		Row vCurrentRow = getRow();
		//Si le nombre de row est égale à 0
		if(nbRowProcess == 0)
		{
			try
			{
				initRowParsing(vCurrentRow);	
			}
			catch(Exception e)
			{
				new KettleException(e);
			}
			startXML();
		}

		if(vCurrentRow != null)
		{
			parseRow(vCurrentRow);
			
			nbRowProcess ++;
		}

		boolean valuesUsed = meta.getValueInWebService() != null && meta.getValueInWebService().size() > 0;
		boolean fieldsUsed = meta.getFieldInWebService() != null && meta.getFieldInWebService().size() > 0;
		
		//Si on est sur la dernière et que l'on n'est pas sur un STEP et que l'on n'est pas en mode field en entrée
		// ou si on est sur un step ou sur 
		// ou si aucune données en entrée
		// si il n'y a aucune row on ne fait rien : on ne fait pas d'appel au serveur
		if( (vCurrentRow == null && (nbRowProcess % STEP != 0 && !valuesUsed)) || 
		    (vCurrentRow != null && ( (nbRowProcess > 0 && nbRowProcess % STEP == 0) || valuesUsed)) ||
		    (vCurrentRow == null && !valuesUsed && !fieldsUsed))
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
		//Gestion de la row courante !
		String vPrefix = "ech";
		if(meta.getFieldInLinkWebServiceFieldList() != null && meta.getFieldInLinkWebServiceFieldList().size() > 0)
		{
			xml.append("        <ech:").append(((WSDLOperation)vWSDLOperation.getArguments().get(0)).getName()).append(">\n");
		}
		
		for(int i = 0; i < vCurrentRow.getFieldNames().length; ++i)
		{
            Value vCurrentValue = vCurrentRow.getValue(i);
            String vType = (String) mapType.get(mapParameters.get(vCurrentValue.getName()));
            if(vType != null)
            {
                if (!vCurrentValue.isNull())
                {
                    xml.append("          <").append(vPrefix).append(":").append(mapParameters.get(vCurrentValue.getName())).append(">");
                    //Si on est sur une heure
                    if(vType.toLowerCase().equals("xsd:time") && vCurrentValue.getType() == Value.VALUE_TYPE_DATE)
                    {
                        xml.append(heureFormat.format(vCurrentValue.getDate()));
                    }
                    //Si on est sur une date
                    else if(vType.toLowerCase().equals("xsd:date") && vCurrentValue.getType() == Value.VALUE_TYPE_DATE)
                    {
                        xml.append(dateFormat.format(vCurrentValue.getDate()));
                    }
                    //TODO à valider si on est sur une date heure
                    else if(vType.toLowerCase().equals("xsd:dateTime") && vCurrentValue.getType() == Value.VALUE_TYPE_DATE)
                    {
                        xml.append(dateHeureFormat.format(vCurrentValue.getDate()));
                    }
                    //Autrement pas de conversion ! 
                    else
                    {
                        xml.append(vCurrentValue.toString().trim());    
                    }
                    xml.append("</").append(vPrefix).append(":").append(mapParameters.get(vCurrentValue.getName())).append(">\n");
                }
                else
                {
                    xml.append("          <").append(vPrefix).append(":").append(mapParameters.get(vCurrentValue.getName())).append(" xsi:nil=\"true\"/>\n");
                }
            }
		}
		if(meta.getFieldInLinkWebServiceFieldList() != null && meta.getFieldInLinkWebServiceFieldList().size() > 0)
		{
			xml.append("        </ech:").append(((WSDLOperation)vWSDLOperation.getArguments().get(0)).getName()).append(">\n");	
		}
	}
	
	private void initRowParsing(Row aCurrentRow) throws Exception
	{
		//Initialisation de la map de paramètre
		// TODO Voir avec David ce que l'on fait ici : voir si c'est comme ça qu'il faut faire en export
		if (aCurrentRow != null)
		{
			mapParameters.clear();
			List/*<FieldLinkWebServiceField>*/ vListField = null;
			//On utilise des valeurs en entrées
			if(meta.getValueInLinkWebServiceFieldList() != null && meta.getValueInLinkWebServiceFieldList().size() > 0)
			{
				vListField = meta.getValueInLinkWebServiceFieldList();
			}
			else
			{
				vListField = meta.getFieldInLinkWebServiceFieldList();
			}
			for(int i = 0; i < aCurrentRow.getFieldNames().length; ++i)
			{
				Value vValue = aCurrentRow.getValue(i);
				FieldLinkWebServiceField vFieldLinkWebServiceField = null;
				for(Iterator/*<FieldLinkWebServiceField>*/ vIt = vListField.iterator(); vIt.hasNext() && vFieldLinkWebServiceField == null;)
				{
					FieldLinkWebServiceField vCurrent = (FieldLinkWebServiceField) vIt.next(); 
					if(vValue.getName().equals(vCurrent.getField().getName()))
					{
						vFieldLinkWebServiceField = vCurrent;
						mapParameters.put(vValue.getName(), vCurrent.getWebServiceField());
					}
				}
			}
		}		
		//Initialisation du flux XML
		DOMParsing vDomParsing = new DOMParsing();
		Set/*<WSDLService>*/ vSetService = vDomParsing.parse(StringUtil.environmentSubstitute(meta.getUrlWebService()));
		
		//Parcours des services à la recherche de l'opération qui va bien !
		for(Iterator/*<WSDLService>*/ vItService = vSetService.iterator(); vItService.hasNext() && vWSDLOperation == null;)
		{
			WSDLService vCurrentWSDLService = (WSDLService) vItService.next();
			for(Iterator/*<WSDLOperation>*/ vItOperation = vCurrentWSDLService.getOperations().iterator(); vItOperation.hasNext() && vWSDLOperation == null;)
			{
				WSDLOperation vCurrentWSDLOperation = (WSDLOperation) vItOperation.next();
				if(vCurrentWSDLOperation.getName().equals(meta.getOperationNameWebService()))
				{
					vWSDLOperation = vCurrentWSDLOperation;
				}
			}
		}
		
		//Liaison entre le nom et le type
        for (Iterator iter = vWSDLOperation.getArguments().iterator(); iter.hasNext();)
        {
            WSDLArgument vArgument = (WSDLArgument) iter.next();
			for (Iterator iterator = vArgument.getParameters().iterator(); iterator.hasNext();)
            {
                WSDLParameter vParameter = (WSDLParameter) iterator.next();
				mapType.put(vParameter.getName(), vParameter.getType());
			}
		}
	}
	
	private void startXML()
	{
		xml = new StringBuffer();
		
		//TODO Si on n'a pas trouvé l'opération il faudrait géré une ptite erreur ...
		xml.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ech=\"");
		xml.append(vWSDLOperation.getTargetNamespace());
		xml.append("\" xmlns:mod=\"");
		//Pour l'instant on ne gère qu'un seul argument ?
		xml.append(((WSDLOperation)vWSDLOperation.getArguments().get(0)).getTargetNamespace());
		xml.append("\">\n");
		
		xml.append("  <soapenv:Header/>\n");
		xml.append("  <soapenv:Body>\n");
		
	    xml.append("    <ech:").append(vWSDLOperation.getName()).append(">\n");
//	    for (FieldLinkWebServiceField field : meta.getValueInLinkWebServiceFieldList())
//	    {
//		    xml.append("      <ech:" + field.getWebServiceField() + ">");
//		 
//		    //Gestion des variables 
//		    String vFieldValue = field.getField().getString();
//		    
//		    if(vFieldValue.startsWith("${") && vFieldValue.endsWith("}"))
//		    {
//		    	vFieldValue = StringUtil.environmentSubstitute(vFieldValue);
//		    }
//		    
//		    // TODO Factoriser le code entre ici et les valeurs des rows
//			if(field.getField().getType() == Value.VALUE_TYPE_DATE)
//			{
//				xml.append(heureFormat.format(new Value(vFieldValue).getDate()));
//			}
//			//Si on est sur une date
//			else if(field.getField().getType() == Value.VALUE_TYPE_DATE)
//			{
//				xml.append(dateFormat.format(new Value(vFieldValue).getDate()));
//			}
//			//TODO à valider si on est sur une date heure
//			else if(field.getField().getType() == Value.VALUE_TYPE_DATE)
//			{
//				xml.append(dateHeureFormat.format(new Value(vFieldValue).getDate()));
//			}
//			else if(field.getField().getType() == Value.VALUE_TYPE_BINARY)
//			{
//				xml.append(new String(Base64.encodeBase64(vFieldValue.getBytes())));
//			}
//			//Autrement pas de conversion ! 
//			else
//			{
//				xml.append(vFieldValue);	
//			}
//		    
//			xml.append("</ech:" + field.getWebServiceField() + ">\n");
//	    }
	    if (meta.getFieldInWebService().size() > 0)
	    {
	    	xml.append("      <ech:" + meta.getInFieldArgumentNameWebService() + ">\n");
	    }

	}
	
	private void endXML()
	{
		//On fini le xml !  
	    if (meta.getFieldInWebService().size() > 0)
	    {
	    	xml.append("      </ech:" + meta.getInFieldArgumentNameWebService() + ">\n");
	    }
		xml.append("    </ech:").append(vWSDLOperation.getName()).append(">\n");
		xml.append("  </soapenv:Body>\n");
		xml.append("</soapenv:Envelope>\n");
	}
	
	private void requestSOAP()
	{
		//désactivation des logs
		Level saveLogLevel = Logger.getRootLogger().getLevel();
		Logger.getRootLogger().setLevel(Level.ERROR);
		
		HttpClient vHttpClient = new HttpClient();
		String vURLSansVariable = StringUtil.environmentSubstitute(meta.getUrlWebService());
		String vURLService = vURLSansVariable.substring(0, vURLSansVariable.lastIndexOf("?"));
		PostMethod vHttpMethod = new PostMethod(vURLService);
		HostConfiguration vHostConfiguration = new HostConfiguration();
		
		
		try 
		{
			vHttpMethod.setURI(new URI(vURLService, false));
			vHttpMethod.setRequestHeader("Content-Type", "text/xml;charset=UTF-8");
			vHttpMethod.setRequestHeader("SOAPAction", "\"\"");


			((PostMethod)vHttpMethod).setRequestEntity(new ByteArrayRequestEntity(xml.toString().getBytes("UTF-8")));
			long currentRequestTime = Const.nanoTime();
			int responseCode = vHttpClient.executeMethod(vHostConfiguration, vHttpMethod);
			if (responseCode == 200)
			{
				processRows(vHttpMethod.getResponseBodyAsStream());
			}
			else
			{
				// TODO Gestion des codes d'erreur
			}
			requestTime += Const.nanoTime() - currentRequestTime;
			//System.out.println(responseCode + " " + ((PostMethod)vHttpMethod).getResponseBodyAsString());
		} 
		catch (Exception e) 
		{
            logError(Const.getStackTracker(e));
		}
		finally
		{
			vHttpMethod.releaseConnection();
		}
		System.out.println("*********************************************************");
		System.out.println("*********************************************************");
		System.out.println("Traitement de " + xml.toString());
		
		System.out.println("*********************************************************");
		System.out.println("*********************************************************");
		
		//réactivation des logs
		Logger.getRootLogger().setLevel(saveLogLevel);
		

	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) 
	{
		meta=(WebServicePluginMeta)smi;
		data=(WebServicePluginData)sdi;
		
		return super.init(smi, sdi);
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) 
	{
		meta=(WebServicePluginMeta)smi;
		data=(WebServicePluginData)sdi;
	
		super.dispose(smi, sdi);
	}
	
	public void run() 
	{
		
		nbRowProcess = 0;
		logBasic("Starting to run...");
		try
		{
			// processingTime = Const.nanoTime();
			requestTime = 0;
			while (processRow(meta, data) && !isStopped());
			// System.out.println(String.format("Processing time = %.3fs", new Object[]{(System.nanoTime() - processingTime) / 1e9}));
			// System.out.println(String.format("Requesting time = %.3fs", new Object[]{requestTime / 1e9}));
		}
		catch(Exception e)
		{
			logError("Unexpected error in : "+e.toString());
			setErrors(1);
			stopAll();
		}
		finally
		{
		    dispose(meta, data);
			logBasic("Finished, processing "+linesRead+" rows");
			markStop();
		}
	}

	private void processRows(InputStream anXml)
	{
		try
		{
			XMLInputFactory vFactory = XMLInputFactory.newInstance();
			XMLStreamReader vReader = vFactory.createXMLStreamReader(anXml);
			Row r = null;
			boolean processing = false;
			for(int event = vReader.next(); event != XMLStreamConstants.END_DOCUMENT; event = vReader.next())
			{
				switch (event) {
				  case XMLStreamConstants.START_ELEMENT:
					//Si on commence l'élément de retour on commence une nouvelle row  
					if(meta.getOutFieldArgumentNameWebService().equals(vReader.getLocalName()))
					{
						r = new Row();
						processing = true;
					}
					else if(processing)
					{
						FieldLinkWebServiceField field = meta.getWsOutLink(vReader.getLocalName());
						String vNodeValue = vReader.getElementText();
						if (field != null)
						{
							WSDLParameter param = meta.getWsOutField(vReader.getLocalName());
							Value value = new Value(field.getField());
							if (vNodeValue == null)
							{
								value.setNull();
							}
							else
							{
								if (XsdType.BOOLEAN.equals(param.getType()))
								{
									value.setValue(Boolean.valueOf(vNodeValue));
								}
								else if (XsdType.DATE.equals(param.getType()))
								{
									try
									{
										value.setValue(dateFormat.parse(vNodeValue));	
									}
									catch(ParseException e)
									{
										value.setNull();
									}
								}
								else if (XsdType.TIME.equals(param.getType()))
								{
									try
									{
										value.setValue(heureFormat.parse(vNodeValue));	
									}
									catch(ParseException e)
									{
										value.setNull();
									}
								}
								else if (XsdType.DATE_TIME.equals(param.getType()))
								{
									value.setValue(dateHeureFormat.parse(vNodeValue));
								}
								else if (XsdType.INTEGER.equals(param.getType()) || XsdType.SHORT.equals(param.getType()))
								{
									try
									{
										value.setValue(Integer.parseInt(vNodeValue));	
									}
									catch(NumberFormatException nfe)
									{
										value.setNull();
									}
								}
								else if (XsdType.FLOAT.equals(param.getType()) || XsdType.DOUBLE.equals(param.getType()))
								{
									try
									{
										value.setValue(Double.parseDouble(vNodeValue));	
									}
									catch(NumberFormatException nfe)
									{
										value.setNull();
									}
								}
								else if(XsdType.BINARY.equals(param.getType()))
								{
									value.setValue(Base64.decodeBase64(vNodeValue.getBytes()));
								}
								else
								{
									value.setValue(vNodeValue);
								}
							}
							r.addValue(value);
						}
					}
				    break;
				  case XMLStreamConstants.END_ELEMENT:
					  //Si on fini l'élément de retour alors on fini la row
					  if(meta.getOutFieldArgumentNameWebService().equals(vReader.getLocalName()))
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
		catch(Exception e)
		{
            logError(Const.getStackTracker(e));
		}
	}
}
