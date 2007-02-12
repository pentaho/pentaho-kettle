package be.ibridge.kettle.trans.step.webservices.wsdl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMParsing 
{
	public Set/*<WSDLService>*/ parse(String anURI) throws Exception
	{
		// TODO Remplacer DOM par StAx la nouvelle API pour de meilleurs perfs
		Set/*<WSDLService>*/ vRet = new HashSet/*<WSDLService>*/();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(anURI);
		
		NodeList vPortTypeList = doc.getElementsByTagName("wsdl:portType");
        if (vPortTypeList.getLength()==0) vPortTypeList = doc.getElementsByTagName("portType");
		
		//Si on a au moins une définition !
		if(vPortTypeList.getLength() > 0)
		{
			for(int i = 0; i < vPortTypeList.getLength(); ++i)
			{
				Node vPortType = vPortTypeList.item(i);
				//GESTION DES SERVICES
				WSDLService vWSDLService = new WSDLService(vPortType.getAttributes().getNamedItem("name").getNodeValue());
				vRet.add(vWSDLService);
				//Parcours des opérations
				for(int j = 0; j < vPortType.getChildNodes().getLength(); ++j)
				{
					Node vOperation = vPortType.getChildNodes().item(j);
					if (vOperation.getNodeName().equals("wsdl:operation") || vOperation.getNodeName().equals("operation") )
					{
						if(vOperation.getAttributes() != null && vOperation.getAttributes().getLength() > 0)
						{
							//GESTION DES OPERATIONS
							WSDLOperation vWSDLOperation = new WSDLOperation(vOperation.getAttributes().getNamedItem("name").getNodeValue());
							vWSDLService.getOperations().add(vWSDLOperation);
							getOperations(doc, vOperation, vWSDLOperation);
						}
					}
				}
			}
		}
		
		
		return vRet;
	}
	
	/**
	 * Création de l'opération 
	 * @param aDoc
	 * @param aNodeOperation
	 * @param aWSDLOperation
	 */
	private void getOperations(Document aDoc, Node aNodeOperation, WSDLOperation aWSDLOperation)
	{
		String requestName = "";
		String responseName = "";
		
		//Récupération du input et des paramètres
		for(int k = 0; k < aNodeOperation.getChildNodes().getLength(); ++k)
		{
			Node vInputOutput = aNodeOperation.getChildNodes().item(k);
			if (vInputOutput.getNodeName().equals("wsdl:input") || vInputOutput.getNodeName().equals("input"))
			{
				requestName = vInputOutput.getAttributes().getNamedItem("message").getNodeValue();
				requestName = requestName.substring(requestName.indexOf(":") + 1);
			}
			else if (vInputOutput.getNodeName().equals("wsdl:output") || vInputOutput.getNodeName().equals("output"))
			{
				responseName = vInputOutput.getAttributes().getNamedItem("message").getNodeValue();
				responseName = responseName.substring(responseName.indexOf(":") + 1);
			}
			
		}
		getRequestResponseParam(aDoc, requestName, true, aWSDLOperation);
		getRequestResponseParam(aDoc, responseName, false, aWSDLOperation);
	}
	
	private void getRequestResponseParam(Document aDoc, String aRequestName, boolean request, WSDLOperation aWSDLOperation)
	{
//		GESTION DES PARAMETRES 
		List/*<String>*/ paramName = new ArrayList/*<String>*/();
		
		//Si on a trouvé le nom de la request !
		if(!aRequestName.equals(""))
		{
			NodeList vMessageList = aDoc.getElementsByTagName("wsdl:message");
            if (vMessageList.getLength()==0) vMessageList = aDoc.getElementsByTagName("message");
			if(vMessageList.getLength() > 0)
			{
				for(int l = 0; l < vMessageList.getLength(); l ++)
				{
					Node vMessage = vMessageList.item(l);
					if(vMessage.getAttributes() != null && vMessage.getAttributes().getLength() > 0)
					{
						if(vMessage.getAttributes().getNamedItem("name").getNodeValue().equals(aRequestName))
						{
							for(int m = 0; m < vMessage.getChildNodes().getLength(); ++m)
							{
								Node vParameter = vMessage.getChildNodes().item(m);
								if(vParameter.getAttributes() != null)
								{
									if (vParameter.getAttributes().getNamedItem("name").getNodeValue().equals("parameters"))
									{
										paramName.add(vParameter.getAttributes().getNamedItem("element").getNodeValue());
									}
								}
							}
						}
					}
				}
			}
		}
		//Obtention des paramètres de l'opération !
		getParams(aDoc, paramName, aWSDLOperation, request);
	}
	
	
	private List/*<WSDLArgument>*/ getArguments(WSDLOperation aWSDLOperation, boolean request)
	{
		List/*<WSDLArgument>*/ vRet = null;
		if(request)
		{
			vRet = aWSDLOperation.getArguments();
		}
		else
		{
			vRet = aWSDLOperation.getReturns();
		}
		return vRet;
	}
	
	
	/**
	 * Gestion des paramètres en entrée
	 * @param aDoc
	 * @param aListParam
	 * @param aWSDLOperation
	 */
	private void getParams(Document aDoc, List/*<String>*/ aListParam, WSDLOperation aWSDLOperation, boolean request)
	{
		for (Iterator iter = aListParam.iterator(); iter.hasNext();)
        {
            String vParamName = (String) iter.next();
			
			//Chaque paramètre peut être un ensemble de type complexe
			List/*<String>*/ vComplexTypeList = new ArrayList/*<String>*/();
			
			NodeList vElementList = aDoc.getElementsByTagName("xsd:element");
			for(int l = 0; l < vElementList.getLength(); ++l)
			{
				Node vElement = vElementList.item(l);
				
				if(vElement.getAttributes() != null)
				{
					if(vElement.getAttributes().getNamedItem("name").getNodeValue().equals(vParamName.substring(vParamName.indexOf(":") + 1)))
					{
						Node vParentNode = vElement.getParentNode();
						if(vParentNode.getAttributes() != null)
						{
							aWSDLOperation.setTargetNamespace(vParentNode.getAttributes().getNamedItem("targetNamespace").getNodeValue());
						}
						WSDLArgument vWSDLArgument = new WSDLArgument("");
						
						getArguments(aWSDLOperation, request).add(vWSDLArgument);
						
						//Normalement on doit être sur un complex type
						for(int m = 0; m < vElement.getChildNodes().getLength(); ++m)
						{
							Node vComplexType = vElement.getChildNodes().item(m);
							if(vComplexType.getNodeName().equals("xsd:complexType"))
							{
								//Normalement on doit avoir un sequence 
								for(int n = 0; n < vElement.getChildNodes().getLength(); ++n)
								{
									Node vSequence = vComplexType.getChildNodes().item(n);
									if(vSequence != null && vSequence.getNodeName().equals("xsd:sequence"))
									{
										//Normalement on doit avoir un xsd sequence
										for(int o = 0; o < vSequence.getChildNodes().getLength() && vWSDLArgument.isMultiple(); o ++)
										{
											Node vElementRef = vSequence.getChildNodes().item(o);
											if(vElementRef.getAttributes() != null && vElementRef.getAttributes().getNamedItem("type") != null)
											{
												//Attention bidouille pour ne pas gérer les type non multiple !
												if(vElementRef.getAttributes().getNamedItem("type").getNodeValue().startsWith("xsd"))
												{
													vWSDLArgument.setMultiple(false);
													for(int p = 0; p < vSequence.getChildNodes().getLength(); p++)
													{
														Node vElementParam = vSequence.getChildNodes().item(p);
														if(vElementParam.getAttributes() != null && vElementParam.getAttributes().getNamedItem("name") != null)
														{
															String name = vElementParam.getAttributes().getNamedItem("name").getNodeValue();
															String type = vElementParam.getAttributes().getNamedItem("type").getNodeValue();
															WSDLParameter vWSDLParameter = new WSDLParameter(name, type);
															vWSDLArgument.getParameters().add(vWSDLParameter);
														}
													}
												}
												else
												{
													vComplexTypeList.add(vElementRef.getAttributes().getNamedItem("type").getNodeValue());	
												}
												
											}
										}
									}
								}
							}
						}
					}
				}
			}
			String vFinalType = "";
			//Pour chaque complex type liste on récupère les ensembles
			int i = 0;

			for (Iterator iterator = vComplexTypeList.iterator(); iterator.hasNext();)
            {
                String vComplexType = (String) iterator.next();
				NodeList vElementNodeList = getElementForComplexType(vComplexType.substring(vComplexType.indexOf(":") + 1), aDoc);
				//Normalement on doit avoir un xsd sequence
				for(int o = 0; o < vElementNodeList.getLength(); o ++)
				{
					Node vElementRef = vElementNodeList.item(o);
					if(vElementRef.getAttributes() != null && vElementRef.getAttributes().getNamedItem("type") != null)
					{
						vFinalType = vElementRef.getAttributes().getNamedItem("type").getNodeValue();
						
						Node vParentNode = vElementRef.getParentNode().getParentNode().getParentNode();
						if(vParentNode.getAttributes() != null)
						{
							((WSDLArgument)getArguments(aWSDLOperation, request).get(i)).setTargetNamespace(vParentNode.getAttributes().getNamedItem("targetNamespace").getNodeValue());
                            ((WSDLArgument)getArguments(aWSDLOperation, request).get(i)).setName(vFinalType.substring(vFinalType.indexOf(":") + 1));
						}
					}
				}
				i ++;
			}
			
			//Si on a trouvé le type final !!
			if(!vFinalType.equals(""))
			{
				for(i = 0; i < vComplexTypeList.size(); ++i)
				{

                    ((WSDLArgument)getArguments(aWSDLOperation, request).get(i)).setMultiple(true);
					NodeList vElementNodeList = getElementForComplexType(vFinalType.substring(vFinalType.indexOf(":") + 1), aDoc);
					//Normalement on doit avoir un xsd sequence
					for(int o = 0; o < vElementNodeList.getLength(); o ++)
					{
						Node vElementRef = vElementNodeList.item(o);
						if(vElementRef.getAttributes() != null && vElementRef.getAttributes().getNamedItem("name") != null)
						{
							String name = vElementRef.getAttributes().getNamedItem("name").getNodeValue();
							String type = vElementRef.getAttributes().getNamedItem("type").getNodeValue();
							WSDLParameter vWSDLParameter = new WSDLParameter(name, type);
                            ((WSDLArgument)getArguments(aWSDLOperation, request).get(i)).getParameters().add(vWSDLParameter);
						}
					}
				}
			}
		}
	}
	
	private static NodeList getElementForComplexType(String aComplexType, Document aDocument)
	{
		NodeList vRet = null;
		NodeList vNodeList = aDocument.getElementsByTagName("xsd:complexType");
		for(int i = 0; i < vNodeList.getLength(); ++i)
		{
			Node vNodeComplexType = vNodeList.item(i);
			if(vNodeComplexType.getAttributes() != null)
			{
				if(vNodeComplexType.getAttributes().getNamedItem("name") != null)
				{
					if(vNodeComplexType.getAttributes().getNamedItem("name").getNodeValue().equals(aComplexType))
					{
						//On choppe la séquence avec l'élément !
						for(int n = 0; n < vNodeComplexType.getChildNodes().getLength(); ++n)
						{
							Node vSequence = vNodeComplexType.getChildNodes().item(n);
							if(vSequence.getNodeName().equals("xsd:sequence"))
							{
								//Normalement on doit avoir un xsd sequence
								vRet = vSequence.getChildNodes();
							}
						}
					}
				}
			}
		}
		return vRet;
	}
}
