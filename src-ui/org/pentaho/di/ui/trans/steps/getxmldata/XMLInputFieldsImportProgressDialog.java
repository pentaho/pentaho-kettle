/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
/*
 *
 *
 */

package org.pentaho.di.ui.trans.steps.getxmldata;

import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.getxmldata.GetXMLDataField;
import org.pentaho.di.trans.steps.getxmldata.GetXMLDataMeta;
import org.pentaho.di.trans.steps.getxmldata.IgnoreDTDEntityResolver;
import org.pentaho.di.ui.core.dialog.ErrorDialog;


/**
 * Takes care of displaying a dialog that will handle the wait while we're finding out loop nodes
 * for an XML file
 * 
 * @author Samatar
 * @since 07-apr-2010
 */
public class XMLInputFieldsImportProgressDialog
{
	private static Class<?> PKG = GetXMLDataMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static String VALUE_NAME= "Name";
	private static String VALUE_PATH= "Path";
	private static String VALUE_ELEMENT= "Element";
	private static String VALUE_TYPE= "Type";
	private static String VALUE_FORMAT= "Format";
	
    private Shell             shell;

    private GetXMLDataMeta     meta;
	
	private String filename;
	private String encoding;
	
	private int nr;
	
	private String loopXPath;
	private     HashSet<String> list;
	
	private List<RowMetaAndData> fieldsList;
	private RowMetaAndData[] fields;
   
	private String xml;
	private String url;
	
    /**
     * Creates a new dialog that will handle the wait while we're finding out loop nodes
     * for an XML file
     */
    public XMLInputFieldsImportProgressDialog(Shell shell, GetXMLDataMeta meta, String filename, 
    		String encoding, String loopXPath)
    {
        this.shell       = shell;
        this.meta        = meta;
        this.fields = null;
        this.filename=filename;
        this.encoding=encoding;
        this.nr=0;
        this.loopXPath=loopXPath;
        this.list = new HashSet<String> ();
        this.fieldsList= new ArrayList<RowMetaAndData>();
    }
    public XMLInputFieldsImportProgressDialog(Shell shell, GetXMLDataMeta meta, String xmlSource, boolean useUrl,
    		String loopXPath)
    {
        this.shell       = shell;
        this.meta        = meta;
        this.fields = null;
        this.filename=null;
        this.encoding=null;
        this.nr=0;
        this.loopXPath=loopXPath;
        this.list = new HashSet<String> ();
        this.fieldsList= new ArrayList<RowMetaAndData>();
        if(useUrl) {
        	this.xml=null;
        	this.url=xmlSource;
        }else {
        	this.xml=xmlSource;
        	this.url=null;
        }
    }
    public RowMetaAndData[] open()
    {
        IRunnableWithProgress op = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                try
                {
                	fields = doScan(monitor);
                }
                catch (Exception e)
                {
                	e.printStackTrace();
                    throw new InvocationTargetException(e, BaseMessages.getString(PKG, "GetXMLDateLoopNodesImportProgressDialog.Exception.ErrorScanningFile", filename, e.toString()));
                }
            }
        };

        try
        {
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
            pmd.run(true, true, op);
        }
        catch (InvocationTargetException e)
        {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "GetXMLDateLoopNodesImportProgressDialog.ErrorScanningFile.Title"), BaseMessages.getString(PKG, "GetXMLDateLoopNodesImportProgressDialog.ErrorScanningFile.Message"), e);
        }
        catch (InterruptedException e)
        {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "GetXMLDateLoopNodesImportProgressDialog.ErrorScanningFile.Title"), BaseMessages.getString(PKG, "GetXMLDateLoopNodesImportProgressDialog.ErrorScanningFile.Message"), e);
        }
  
        return fields;
    }

    @SuppressWarnings("unchecked")
	private RowMetaAndData[] doScan(IProgressMonitor monitor) throws Exception
    {
        monitor.beginTask(BaseMessages.getString(PKG, "GetXMLDateLoopNodesImportProgressDialog.Task.ScanningFile", filename), 1);

        SAXReader reader = new SAXReader();
	    monitor.worked(1);
	    if(monitor.isCanceled()) return null;
        // Validate XML against specified schema?
		if(meta.isValidating())
		{
			reader.setValidation(true);
			reader.setFeature("http://apache.org/xml/features/validation/schema", true);
		}
		else
		{
			// Ignore DTD
			reader.setEntityResolver(new IgnoreDTDEntityResolver());
		}
	    monitor.worked(1);
	    monitor.beginTask(BaseMessages.getString(PKG, "GetXMLDateLoopNodesImportProgressDialog.Task.ReadingDocument"), 1);
	    if(monitor.isCanceled()) return null;
		InputStream is= null;
		try {

			Document document=null;
			if(!Const.isEmpty(filename)) {
				is=KettleVFS.getInputStream(filename);
				document  = reader.read( is, encoding);	
			}else {
				if(!Const.isEmpty(xml)) {
					document  = reader.read(new StringReader(xml));
				}else {
					document  = reader.read(new URL(url));
				}
			} 
			
			monitor.worked(1);
		    monitor.beginTask(BaseMessages.getString(PKG, "GetXMLDateLoopNodesImportProgressDialog.Task.DocumentOpened"), 1);
			monitor.worked(1);
		    monitor.beginTask(BaseMessages.getString(PKG, "GetXMLDateLoopNodesImportProgressDialog.Task.ReadingNode"), 1);
		    
		    if(monitor.isCanceled()) return null;
			List<Node> nodes = document.selectNodes(this.loopXPath);
			monitor.worked(1);
			monitor.subTask(BaseMessages.getString(PKG, "GetXMLDateLoopNodesImportProgressDialog.Task.FetchNodes"));
		    
			if(monitor.isCanceled()) return null;
			 for (Node node : nodes) 
			 {
				 if(monitor.isCanceled()) return null;

				 nr++;
				 monitor.subTask(BaseMessages.getString(PKG, "GetXMLDateLoopNodesImportProgressDialog.Task.FetchNodes", String.valueOf(nr)));
				 monitor.subTask(BaseMessages.getString(PKG, "GetXMLDateLoopNodesImportProgressDialog.Task.FetchNodes", node.getPath()));
 			     setNodeField(node, monitor); 
 			     childNode(node, monitor);

			 }
		    monitor.worked(1);
		}finally {
			try {
				if(is!=null) is.close();
			}catch(Exception e){};
		}


		RowMetaAndData[] listFields = fieldsList.toArray(new RowMetaAndData[fieldsList.size()]);
		
        monitor.setTaskName(BaseMessages.getString(PKG, "GetXMLDateLoopNodesImportProgressDialog.Task.NodesReturned"));
        
        monitor.done();
        
        return listFields;

    }
    @SuppressWarnings("unchecked")
	private void setNodeField(Node node, IProgressMonitor monitor)
	{
		Element e = (Element) node; 
		// get all attributes
		List<Attribute> lista = e.attributes(); 
		for(int i=0;i<lista.size();i++)
		{
			 setAttributeField(lista.get(i), monitor);
		}

		// Get Node Name
		String nodename=node.getName();
		String nodenametxt=cleanString(node.getPath());
		
		if(!Const.isEmpty(nodenametxt) && !list.contains(nodenametxt))
		{	
			 nr++;
			 monitor.subTask(BaseMessages.getString(PKG, "GetXMLDataXMLInputFieldsImportProgressDialog.Task.FetchFields", String.valueOf(nr)));
			 monitor.subTask(BaseMessages.getString(PKG, "GetXMLDataXMLInputFieldsImportProgressDialog.Task.AddingField",nodename));
		
			 RowMetaAndData row = new RowMetaAndData();
			 row.addValue(VALUE_NAME, Value.VALUE_TYPE_STRING, nodename);
			 row.addValue(VALUE_PATH, Value.VALUE_TYPE_STRING, nodenametxt);
			 row.addValue(VALUE_ELEMENT, Value.VALUE_TYPE_STRING, GetXMLDataField.ElementTypeDesc[0]);

            // Get Node value
            String valueNode=node.getText();
            
			// Try to get the Type

            if(IsDate(valueNode)) {
    			row.addValue(VALUE_TYPE, Value.VALUE_TYPE_STRING, "Date");
    			row.addValue(VALUE_FORMAT, Value.VALUE_TYPE_STRING,  "yyyy/MM/dd");
            } else if(IsInteger(valueNode)) {
            	row.addValue(VALUE_TYPE, Value.VALUE_TYPE_STRING, "Integer");
    			row.addValue(VALUE_FORMAT, Value.VALUE_TYPE_STRING, null);
            } else if(IsNumber(valueNode)) {
             	row.addValue(VALUE_TYPE, Value.VALUE_TYPE_STRING, "Number");   		
        		row.addValue(VALUE_FORMAT, Value.VALUE_TYPE_STRING, null);
            } else {
             	row.addValue(VALUE_TYPE, Value.VALUE_TYPE_STRING, "String");  
        		row.addValue(VALUE_FORMAT, Value.VALUE_TYPE_STRING, null);
            }
            fieldsList.add(row);
            list.add(nodenametxt);
           
		}// end if
	}
    private void setAttributeField(Attribute attribute, IProgressMonitor monitor)
	{
		// Get Attribute Name
		String attributname=attribute.getName();
		String attributnametxt=cleanString(attribute.getPath());
		if(!Const.isEmpty(attributnametxt) && !list.contains(attribute.getPath()))
		{
			 nr++;
			 monitor.subTask(BaseMessages.getString(PKG, "GetXMLDataXMLInputFieldsImportProgressDialog.Task.FetchFields", String.valueOf(nr)));
			 monitor.subTask(BaseMessages.getString(PKG, "GetXMLDataXMLInputFieldsImportProgressDialog.Task.AddingField",attributname));
			
			RowMetaAndData row = new RowMetaAndData();
			row.addValue(VALUE_NAME, Value.VALUE_TYPE_STRING, attributname);
			row.addValue(VALUE_PATH, Value.VALUE_TYPE_STRING, attributnametxt);
			row.addValue(VALUE_ELEMENT, Value.VALUE_TYPE_STRING, GetXMLDataField.ElementTypeDesc[1]);
			
            // Get attribute value
            String valueAttr =attribute.getText();
            
            // Try to get the Type

            if(IsDate(valueAttr)) {
    			row.addValue(VALUE_TYPE, Value.VALUE_TYPE_STRING, "Date");
    			row.addValue(VALUE_FORMAT, Value.VALUE_TYPE_STRING, "yyyy/MM/dd");
            }  else if(IsInteger(valueAttr)) {
        		row.addValue(VALUE_TYPE, Value.VALUE_TYPE_STRING, "Integer");
    			row.addValue(VALUE_FORMAT, Value.VALUE_TYPE_STRING, null);
            } else if(IsNumber(valueAttr)) {
        		row.addValue(VALUE_TYPE, Value.VALUE_TYPE_STRING, "Number");
    			row.addValue(VALUE_FORMAT, Value.VALUE_TYPE_STRING, null);
            } else {
        		row.addValue(VALUE_TYPE, Value.VALUE_TYPE_STRING, "String");
        		row.addValue(VALUE_FORMAT, Value.VALUE_TYPE_STRING, null);
            }
            list.add(attribute.getPath());
		}// end if
	            
	            
	}
    private String cleanString(String inputstring)
	{
		String retval=inputstring;
		retval=retval.replace(this.loopXPath, "");
		while(retval.startsWith(GetXMLDataMeta.N0DE_SEPARATOR))
		{
			retval=retval.substring(1, retval.length());
		}
		
		return retval;
	}
	
    private boolean IsDate(String str)
	{
		  // TODO: What about other dates? Maybe something for a CRQ
		  try 
		  {
		        SimpleDateFormat fdate = new SimpleDateFormat("yyyy/MM/dd");
		        fdate.setLenient(false);
		        fdate.parse(str);
		  }
		  catch(Exception e)   {return false; }
		  return true;
	}

    private boolean IsInteger(String str)
	{
		  try 
		  {
		     Integer.parseInt(str);
		  }
		  catch(NumberFormatException e)   {return false; }
		  return true;
	}

	private boolean IsNumber(String str)
	{
		  try 
		  {
		     Float.parseFloat(str);
		  }
		  catch(Exception e)   {return false; }
		  return true;
	}
	private boolean childNode(Node node, IProgressMonitor monitor)
	{
		 boolean rc=false; //true: we found child nodes
		 Element ce = (Element) node;
		 // List child 
		 for(int j=0;j<ce.nodeCount();j++)
		 {
			 Node cnode=ce.node(j);
			 if(!Const.isEmpty(cnode.getName()))
			 {
				 Element cce = (Element) cnode;
				 if(cce.nodeCount()>1)
				 {
					 if(childNode(cnode, monitor)==false){
						// We do not have child nodes ...
						 setNodeField(cnode, monitor);
						 rc=true;
					 }
				 }else
				 {
					 setNodeField(cnode, monitor);
					 rc=true;
				 }
			 } 
		 }
		 return rc;
	}
	
}