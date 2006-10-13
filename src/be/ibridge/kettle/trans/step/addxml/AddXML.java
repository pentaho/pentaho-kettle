/***********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 

package be.ibridge.kettle.trans.step.addxml;

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

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Converts input rows to one or more XML files.
 * 
 * @author Matt
 * @since 14-jan-2006
 */
public class AddXML extends BaseStep implements StepInterface
{
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

        Row r = getRow();       // This also waits for a row to be finished.
        
        if (r==null)  // no more input to be expected...
        {
            setOutputDone();
            return false;
        }
        
        Document xmldoc = getDomImplentation().createDocument(null, meta.getRootNode(), null);
        Element root = xmldoc.getDocumentElement();
        for (int i=0;i<meta.getOutputFields().length;i++)
        {
            XMLField outputField = meta.getOutputFields()[i];
            String fieldname = outputField.getFieldName();
            
            Value v = r.searchValue(fieldname);
            String value = formatField(v, outputField);

            String element = outputField.getElementName();
            if(element == null || element.length() == 0)
                element = fieldname;
            
            if(element == null || element.length() == 0) {
                throw new KettleException("XML does not allow empty strings for element names.");
            }
            if(outputField.isAttribute() ){
                root.setAttribute(element, value);
            }
            else { /* encode as subnode */
                Element e = xmldoc.createElement(element);
                Node n = xmldoc.createTextNode(value);
                e.appendChild(n);
                root.appendChild(e);
            }
        }
        
        StringWriter sw = new StringWriter();
        DOMSource domSource = new DOMSource(xmldoc);
        try 
        {
            this.getSerializer().transform(domSource, new StreamResult(sw));
        } catch (TransformerException e) {
            throw new KettleException(e);
        }
        Value v = new Value(meta.getValueName(), sw.toString());
        r.addValue(v);
        
        
        putRow(r);
        return true;
    }

    private String formatField(Value v, XMLField field)
    {
        String retval="";
        if(field == null)
            return "";

        if(v == null || v.isNull()) 
        {
            String defaultNullValue = field.getNullString();
            return Const.isEmpty(defaultNullValue) ? "" : defaultNullValue ;
        }

        if (v.isNumeric())
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

            if (v.isBigNumber())
            {
                retval=data.df.format(v.getBigNumber());
            }
            else if (v.isNumber())
            {
                retval=data.df.format(v.getNumber());
            }
            else // Integer
            {
                retval=data.df.format(v.getInteger());
            }
        }
        else
        if (v.isDate())
        {
            if (field!=null && !Const.isEmpty(field.getFormat()) && v.getDate()!=null)
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
                retval= data.daf.format(v.getDate());
            }
            else
            {
                if (v.isNull() || v.getDate()==null) 
                {
                    if (field!=null && !Const.isEmpty(field.getNullString()))
                    {
                        retval=field.getNullString();
                    }
                }
                else
                {
                    retval=v.toString();
                }
            }
        }
        else
        if (v.isString())
        {
            retval=v.toString();
        }
        else if (v.isBinary())
        {
            if (v.isNull())
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
                    retval=new String(v.getBytes(), "UTF-8");
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
            retval=v.toString();
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

            //TODO - implement XML content type declaration
            
            if(meta.isOmitXMLheader()) {
                getSerializer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
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
    
    //
    // Run is were the action happens!
    public void run()
    {
        try
        {
            logBasic("Starting to run...");
            while (processRow(meta, data) && !isStopped())
                ;
        }
        catch(Exception e)
        {
            logError("Unexpected error : "+e.toString());
            logError(Const.getStackTracker(e));
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
