/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.addxml;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Converts input rows to one or more XML files.
 * 
 * @author Matt
 * @since 14-jan-2006
 */
public class AddXML extends BaseStep implements StepInterface
{
	private static Class<?> PKG = AddXML.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private AddXMLMeta meta;
    private AddXMLData data;
    
    private DOMImplementation domImplentation; 
    private Transformer serializer;
     
     
    public AddXML(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(AddXMLMeta)smi;
        data=(AddXMLData)sdi;

        Object[] r = getRow();       // This also waits for a row to be finished.
        if (r==null)  // no more input to be expected...
        {
            setOutputDone();
            return false;
        }
        
        if (first) 
        {
        	first=false;
        	
        	data.outputRowMeta = getInputRowMeta().clone();
        	meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
        	
        	// Cache the field name indexes
        	//
        	data.fieldIndexes = new int[meta.getOutputFields().length];
        	for (int i=0;i<data.fieldIndexes.length;i++)
        	{
        		data.fieldIndexes[i] = getInputRowMeta().indexOfValue(meta.getOutputFields()[i].getFieldName());
        		if (data.fieldIndexes[i]<0) 
        		{
        			throw new KettleException(BaseMessages.getString(PKG, "AddXML.Exception.FieldNotFound")); //$NON-NLS-1$
        		}
        	}
        }
        
        Document xmldoc = getDomImplentation().createDocument(null, meta.getRootNode(), null);
        Element root = xmldoc.getDocumentElement();
        for (int i=0;i<meta.getOutputFields().length;i++)
        {
            XMLField outputField = meta.getOutputFields()[i];
            String fieldname = outputField.getFieldName();
            
            ValueMetaInterface v = getInputRowMeta().getValueMeta( data.fieldIndexes[i]);
            Object valueData = r[ data.fieldIndexes[i] ];
            
            if (!meta.isOmitNullValues() || !v.isNull(valueData)) {
              String value = formatField(v, valueData, outputField);
  
              String element = outputField.getElementName();
              if(element == null || element.length() == 0)
                  element = fieldname;
              
              if(element == null || element.length() == 0) {
                  throw new KettleException("XML does not allow empty strings for element names."); //$NON-NLS-1$
              }
              if(outputField.isAttribute() ){
              	String attributeParentName = outputField.getAttributeParentName();
              	
              	Element node;
              	
              	if (attributeParentName == null || attributeParentName.length() == 0){
              		node = root;
              	}
              	else{
              		NodeList nodelist = root.getElementsByTagName(attributeParentName);
              		if(nodelist.getLength()> 0){
              			node = (Element)nodelist.item(0);
              		}
              		else{
              			node = root;
              		}
              	}
              	
              	node.setAttribute(element, value);
              	
              }
              else { /* encode as subnode */
              	if(!element.equals(meta.getRootNode())){
  	                Element e = xmldoc.createElement(element);
  	                Node n = xmldoc.createTextNode(value);
  	                e.appendChild(n);
  	                root.appendChild(e);
              	}
              	else{
              		Node n = xmldoc.createTextNode(value);
              		root.appendChild(n);
              	}
              }
            }
        }
        
        StringWriter sw = new StringWriter();
        DOMSource domSource = new DOMSource(xmldoc);
        try 
        {
            this.getSerializer().transform(domSource, new StreamResult(sw));
            
        } catch (TransformerException e) {
          throw new KettleException(e);
        } catch (Exception e) {
          throw new KettleException(e);            
        } 

        Object[] outputRowData = RowDataUtil.addValueData(r, getInputRowMeta().size(), sw.toString());
        
        putRow(data.outputRowMeta, outputRowData);
        
        return true;
    }    

    private String formatField(ValueMetaInterface valueMeta, Object valueData, XMLField field) throws KettleValueException
    {
        String retval=""; //$NON-NLS-1$
        if(field == null)
            return ""; //$NON-NLS-1$

        if(valueMeta == null || valueMeta.isNull(valueData)) 
        {
            String defaultNullValue = field.getNullString();
            return Const.isEmpty(defaultNullValue) ? "" : defaultNullValue ; //$NON-NLS-1$
        }

        if (valueMeta.isNumeric())
        {
            // Formatting
            if ( !Const.isEmpty(field.getFormat()) )
            {
                data.df.applyPattern(field.getFormat());
            }
            else
            {
                data.df.applyPattern(data.defaultDecimalFormat.toPattern());
            }
            // Decimal 
            if ( !Const.isEmpty( field.getDecimalSymbol()) )
            {
                data.dfs.setDecimalSeparator( field.getDecimalSymbol().charAt(0) );
            }
            else
            {
                data.dfs.setDecimalSeparator( data.defaultDecimalFormatSymbols.getDecimalSeparator() );
            }
            // Grouping
            if ( !Const.isEmpty( field.getGroupingSymbol()) )
            {
                data.dfs.setGroupingSeparator( field.getGroupingSymbol().charAt(0) );
            }
            else
            {
                data.dfs.setGroupingSeparator( data.defaultDecimalFormatSymbols.getGroupingSeparator() );
            }
            // Currency symbol
            if ( !Const.isEmpty( field.getCurrencySymbol()) ) 
            {
                data.dfs.setCurrencySymbol( field.getCurrencySymbol() );
            }
            else
            {
                data.dfs.setCurrencySymbol( data.defaultDecimalFormatSymbols.getCurrencySymbol() );
            }

            data.df.setDecimalFormatSymbols(data.dfs);

            if (valueMeta.isBigNumber())
            {
                retval=data.df.format(valueMeta.getBigNumber(valueData));
            }
            else if (valueMeta.isNumber())
            {
                retval=data.df.format(valueMeta.getNumber(valueData));
            }
            else // Integer
            {
                retval=data.df.format(valueMeta.getInteger(valueData));
            }
        }
        else
        if (valueMeta.isDate())
        {
            if (field!=null && !Const.isEmpty(field.getFormat()) && valueMeta.getDate(valueData)!=null)
            {
                if (!Const.isEmpty(field.getFormat()))
                {
                    data.daf.applyPattern( field.getFormat() );
                }
                else
                {
                    data.daf.applyPattern( data.defaultDateFormat.toLocalizedPattern() );
                }
                data.daf.setDateFormatSymbols(data.dafs);
                retval= data.daf.format(valueMeta.getDate(valueData));
            }
            else
            {
                if (valueMeta.isNull(valueData)) 
                {
                    if (field!=null && !Const.isEmpty(field.getNullString()))
                    {
                        retval=field.getNullString();
                    }
                }
                else
                {
                    retval=valueMeta.getString(valueData);
                }
            }
        }
        else
        if (valueMeta.isString())
        {
            retval=valueMeta.getString(valueData);
        }
        else if (valueMeta.isBinary())
        {
            if (valueMeta.isNull(valueData))
            {
                if (!Const.isEmpty(field.getNullString()))
                {
                    retval=field.getNullString();
                }
                else
                {
                    retval=Const.NULL_BINARY;
                }
            }
            else
            {                   
                try 
                {
                    retval=new String(valueMeta.getBinary(valueData), "UTF-8"); //$NON-NLS-1$
                } 
                catch (UnsupportedEncodingException e) 
                {
                    // chances are small we'll get here. UTF-8 is
                    // mandatory.
                    retval=Const.NULL_BINARY;   
                }                   
            }
        }        
        else // Boolean
        {
            retval=valueMeta.getString(valueData);
        }

        return retval;
    }
    
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(AddXMLMeta)smi;
        data=(AddXMLData)sdi;
        if(!super.init(smi, sdi))
            return false;

        try {
            setSerializer(TransformerFactory.newInstance().newTransformer());

            setDomImplentation(DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation());

            if(meta.getEncoding()!=null) {
            	getSerializer().setOutputProperty(OutputKeys.ENCODING, meta.getEncoding());
            }
            
            if(meta.isOmitXMLheader()) {
                getSerializer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); //$NON-NLS-1$
            }
        } catch (TransformerConfigurationException e) {
            return false;
        } catch (ParserConfigurationException e) {
            return false;
        }

        return true;
    }
    
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(AddXMLMeta)smi;
        data=(AddXMLData)sdi;
        
        super.dispose(smi, sdi);
        
    }    

    private void setDomImplentation(DOMImplementation domImplentation) {
        this.domImplentation = domImplentation;
    }

    private DOMImplementation getDomImplentation() {
        return domImplentation;
    }

    private void setSerializer(Transformer serializer) {
        this.serializer = serializer;
    }

    private Transformer getSerializer() {
        return serializer;
    }
      
}