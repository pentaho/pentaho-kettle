/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.www;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.Semaphore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepInterface;

public class SniffRowsServlet extends BaseHttpServlet implements CartePluginInterface {
  	private static Class<?> PKG = GetTransStatusServlet.class; // for i18n purposes, needed by Translator2!!

  	private static final long serialVersionUID = 3634806745372015720L;
  	public static final String CONTEXT_PATH = "/kettle/sniffRows";

  	public static final String TYPE_INPUT = "input";
  	public static final String TYPE_OUTPUT = "output";

  	public static final String XML_TAG = "step-sniff";

  	private final int defaultBufferSize = 50;

	final class CircularQueue<T> {
		private T[] arr;
    		private boolean wrapped;
    		private int next_insert;

		@SuppressWarnings("unchecked")
    		public CircularQueue(int size) {
			next_insert = 0;
			wrapped = false;
			arr = (T[]) new Object[size];
		}

		public void push(T elem) {
			arr[next_insert++] = elem;
			if (next_insert == arr.length) {
				next_insert = 0;
				wrapped = true;
			}
    		}

    		public ArrayList<T> get(){
			ArrayList<T> ret = new ArrayList<T>();
			if (!wrapped) {
				for (int i = 0; i < next_insert; ++ i)
					ret.add(arr[i]);
			} else {
				int arr_pos = next_insert;
				for (int i=0; i < arr.length; ++i) {
					ret.add(arr[arr_pos++]);
					if (arr_pos == arr.length)
						arr_pos = 0;
				}
			}
			return ret;
		}

		public int size() {
			if (wrapped)
				return arr.length;
			else
				return next_insert;
		}
	}


  	final class MetaAndData {
    		public RowMetaInterface bufferRowMeta;
    		public CircularQueue<Object[]> bufferRowData;
    		public String transName;
    		public String id;
    		public String stepName;
    		public int copyNr;
    		public String type;
    		public RowListener rowListener;
  	}

    	public Semaphore mutex;
  	public List<MetaAndData> clients;

  	public SniffRowsServlet() {
    		mutex = new Semaphore (1);
    		clients = new ArrayList<MetaAndData>();
  	}

  	public SniffRowsServlet( TransformationMap transformationMap ) {
    		super( transformationMap );
    		mutex = new Semaphore (1);
    		clients = new ArrayList<MetaAndData>();
  	}

  	/**
	  <div id="mindtouch">
    	  <h1>/kettle/sniffRows</h1>
    	  <a name="GET"></a>
    	  <h2>GET</h2>
    	  <p>Sniff metadata and data from the specified step of the specified transformation.</p>

    	  <p><b>Example Request:</b><br />
    	  <pre function="syntax.xml">
    	  GET /kettle/sniffRows?trans=dummy-trans&step=tf&xml=Y
    	  </pre>

    	  </p>
    	  <h3>Parameters</h3>
    	  <table class="pentaho-table">
    	  <tbody>
    	  <tr>
      	  <th>name</th>
      	  <th>description</th>
      	  <th>type</th>
    	  </tr>
    	  <tr>
    	  <td>transName</td>
    	  <td>Name of the transformation containing required step.</td>
    	  <td>query</td>
    	  </tr>
    	  <tr>
    	  <td>stepName</td>
    	  <td>Name of the transformation step to collect data for.</td>
    	  <td>query</td>
    	  </tr>
    	  <tr>
    	  <td>copynr</td>
    	  <td>Copy number of the step to be used for collecting data. If not provided 0 is used.</td>
    	  <td>integer, optional</td>
    	  </tr>
    	  <tr>
    	  <td>type</td>
    	  <td>Type of the data to be collected (<code>input</code> or <code>output</code>). 
    	  If not provided output data is collected.</td>
    	  <td>query, optional</td>
    	  </tr>
    	  <tr>
    	  <td>xml</td>
    	  <td>Boolean flag which defines output format <code>Y</code> forces XML output to be generated. 
  	  HTML is returned otherwise.</td>
    	  <td>boolean, optional</td>
    	  </tr>
    	  <tr>
    	  <td>id</td>
    	  <td>Carte id of the transformation to be used for step lookup.</td>
    	  <td>query, optional</td>
    	  </tr>
    	  <tr>
    	  <td>cmd</td>
    	  <td>If equal to "stop" it stops listening the specified step. If missing, it starts intercepting rows.</td>
    	  <td>optional</td>
    	  </tr>
    	  </tbody>
    	  </table>

  	  <h3>Response Body</h3>

  	  <table class="pentaho-table">
    	  <tbody>
      	  <tr>
          <td align="right">element:</td>
          <td>(custom)</td>
      	  </tr>
      	  <tr>
          <td align="right">media types:</td>
        <td>text/xml, text/html</td>
      		</tr>
    		</tbody>
  		</table>
    		<p>Response XML or HTML response containing data and metadata of the step.
  		If an error occurs during method invocation <code>result</code> field of the response 
  		will contain <code>ERROR</code> status.</p>

    		<p><b>Example Response:</b></p>
    		<pre function="syntax.xml">
    		<?xml version="1.0" encoding="UTF-8"?>
    		<step-sniff>
      		<row-meta>
        	<value-meta><type>String</type>
          	<storagetype>normal</storagetype>
          	<name>Field1</name>
          	<length>0</length>
          	<precision>-1</precision>
          	<origin>tf</origin>
          	<comments/>
          	<conversion_Mask/>
          	<decimal_symbol>.</decimal_symbol>
          	<grouping_symbol>,</grouping_symbol>
          	<currency_symbol>&#x24;</currency_symbol>
          	<trim_type>none</trim_type>
          	<case_insensitive>N</case_insensitive>
          	<sort_descending>N</sort_descending>
          	<output_padding>N</output_padding>
          	<date_format_lenient>Y</date_format_lenient>
          	<date_format_locale>en_US</date_format_locale>
          	<date_format_timezone>America&#x2f;Bahia</date_format_timezone>
          	<lenient_string_to_number>N</lenient_string_to_number>
        	</value-meta>
      		</row-meta>
      		<nr_rows>10</nr_rows>

      		<row-data><value-data>my-data</value-data>
      		</row-data>
      		<row-data><value-data>my-data </value-data>
      		</row-data>
      		<row-data><value-data>my-data</value-data>
      		</row-data>
      		<row-data><value-data>my-data</value-data>
      		</row-data>
      		<row-data><value-data>my-data</value-data>
      		</row-data>
      		<row-data><value-data>my-data</value-data>
      		</row-data>
      		<row-data><value-data>my-data</value-data>
      		</row-data>
      		<row-data><value-data>my-data</value-data>
      		</row-data>
      		<row-data><value-data>my-data</value-data>
      		</row-data>
      		<row-data><value-data>my-data</value-data>
      		</row-data>
    		</step-sniff>
    		</pre>

    		<h3>Status Codes</h3>
    		<table class="pentaho-table">
  		<tbody>
    		<tr>
      		<th>code</th>
      		<th>description</th>
    		</tr>
    		<tr>
      		<td>200</td>
      		<td>Request was processed.</td>
    		</tr>
    		<tr>
      		<td>500</td>
      		<td>Internal server error occurs during request processing.</td>
    		</tr>
  		</tbody>
		</table>
		</div>
  		*/
  	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
    	       IOException {
    		       if ( isJettyMode() && !request.getContextPath().startsWith( CONTEXT_PATH ) ) {
      			       return;
    		       }

    		       if ( log.isDebug() ) {
      			       logDebug("Sniff Rows requested");
    		       }

    		       String transName = request.getParameter( "trans" );
    		       String id = request.getParameter( "id" );
    		       String stepName = request.getParameter( "step" );
    		       String cmd = request.getParameter("cmd" );
    		       if ( Const.isEmpty( cmd ) ) {
			       cmd = new String("");
		       }

    		       int copyNr = Const.toInt( request.getParameter( "copynr" ), 0 );
    		       int bufferSize = Const.toInt( request.getParameter( "buffer" ), 0 );
    		       if (bufferSize == 0)
				bufferSize = defaultBufferSize;
    		       final int nrLines = Const.toInt( request.getParameter( "lines" ), 0 );
    		       String type = Const.NVL( request.getParameter( "type" ), TYPE_OUTPUT );
    		       boolean useXML = "Y".equalsIgnoreCase( request.getParameter( "xml" ) );
    		       // System.out.println("=======================");
    		       // System.out.println("Trans: " + transName);
    		       // System.out.println("id: " + id);
    		       // System.out.println("stepName: " + stepName);
    		       // System.out.println("copyNr: " + copyNr);
    		       // System.out.println("type: " + type);
    		       // System.out.println("useXML: " + useXML);
    		       // System.out.println("cmd: " + cmd);

    		       response.setStatus( HttpServletResponse.SC_OK );

    		       if ( useXML ) {
      			       response.setContentType( "text/xml" );
      			       response.setCharacterEncoding( Const.XML_ENCODING );
    		       } else {
      			       response.setContentType( "text/html;charset=UTF-8" );
    		       }

    		       PrintWriter out = response.getWriter();

    		       // ID is optional...
    		       //
    		       Trans trans;
    		       CarteObjectEntry entry;
    		       if ( Const.isEmpty( id ) ) {
      			       // get the first transformation that matches...
      			       //
      			       entry = getTransformationMap().getFirstCarteObjectEntry( transName );
      			       if ( entry == null ) {
        			       trans = null;
      			       } else {
        			       id = entry.getId();
        			       trans = getTransformationMap().getTransformation( entry );
      			       }
    		       } else {
      			       // Take the ID into account!
      			       //
      			       entry = new CarteObjectEntry( transName, id );
      			       trans = getTransformationMap().getTransformation( entry );
    		       }

    		       Encoder encoder = ESAPI.encoder();

    		       if ( trans != null ) {
			       // System.out.println("Trans found.");

      			       // Find the step to look at...
      			       StepInterface step = null;
      			       List<StepInterface> stepInterfaces = trans.findBaseSteps( stepName );
      			       for ( int i = 0; i < stepInterfaces.size(); i++ ) {
        			       StepInterface look = stepInterfaces.get( i );
        			       if ( look.getCopy() == copyNr ) {
          				       step = look;
        			       }
      			       }
      			       if ( step != null ) {
				       try {
				       	       // System.out.println("Step found.");

				       	       // Get lock
				       	       // System.out.println("Acquiring mutex...");
				       	       mutex.acquire();

				       	       // Check if a listener already exists
				       	       // System.out.println("Searching listener...");
				       	       MetaAndData metaData = null;
				       	       for (int i = 0; i < clients.size(); ++i) {
					       	       MetaAndData curr = clients.get(i);
					       	       if (curr.transName.equals(transName) &&
							       	       curr.id.equals(id) &&
							       	       curr.stepName.equals(stepName) &&
							       	       (curr.copyNr == copyNr) &&
							       	       curr.type.equalsIgnoreCase(type)) {
						       	       // System.out.println("Listener found");
						       	       metaData = curr;
						       	       break;
							       	       }
				       	       }

					       // STOP
					       if (cmd.equalsIgnoreCase("stop")) {
				       	       	       if (metaData != null) {
							       step.removeRowListener(metaData.rowListener);
				       	       		       for (int i = 0; i < clients.size(); ++i) {
								       if (clients.get(i) == metaData) {
									       clients.remove(i);
									       break;
								       }
							       }
						       } else {
					       	       	       // System.out.println("Stop: metadata is null");
					       	       }

        		       			       if ( useXML ) {
          			       			       out.println( new WebResult( WebResult.STRING_OK, "Stopped" ).getXML() );
        		       			       } else {
          			       			       out.println( "<HTML>" );
          			       			       out.println( "<HEAD>" );
          			       			       out.println( "<TITLE> Preview data response</TITLE>" );
          			       			       out.println( "</HEAD>" );
          			       			       out.println( "<BODY>" );
          			       			       out.println( "Stopped" );
          			       			       out.println( "<p>" );
          				       		       out.println( "<a href=\""
            						       		       + convertContextPath( GetTransStatusServlet.CONTEXT_PATH ) + "?name="
            						       		       + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" ) + "\">"
            						       		       + BaseMessages.getString( PKG, "TransStatusServlet.BackToTransStatusPage" ) + "</a><p>" );
          			       			       out.println( "</BODY>" );
          			       			       out.println( "</HTML>" );
        		       			       }

					       } else {
				       	       	       if (metaData == null) {
					       	       	       // N => create listener
					       	       	       // System.out.println("Metadata is null");
					       	       	       final MetaAndData newMetaData = new MetaAndData();
        			       	       	       	       newMetaData.bufferRowMeta = null;
        			       	       	       	       newMetaData.bufferRowData = new CircularQueue<Object[]>(bufferSize);
        			       	       	       	       newMetaData.transName = transName;
        			       	       	       	       newMetaData.id = id;
        			       	       	       	       newMetaData.stepName = stepName;
        			       	       	       	       newMetaData.copyNr = copyNr;
        			       	       	       	       newMetaData.type = type;

					       	       	       if (type.equalsIgnoreCase(TYPE_INPUT)) {
					       	       	       	       // System.out.println("Adding read listener...");
						       	       	       newMetaData.rowListener = new RowListener() {
          				       		       	       	       public void rowReadEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
										       // System.out.println("Row read event.");
					       			       	       	       try {
				       				       	       	       	       mutex.acquire();
              					       		       	       	       	       newMetaData.bufferRowMeta = rowMeta;
              					       		       	       	       	       newMetaData.bufferRowData.push(row);
              					       		       	       	       	       mutex.release();
              					       		       	       	       } catch (Exception ex) {
					       			       	       	       	       System.out.println("Exception in row read event.");
              					       		       	       	       }
            					       	       	       	       }
          				       	       	       	       	       public void errorRowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
          				       	       	       	       	       }
          				       	       	       	       	       public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
          				       	       	       	       	       }
          				       	       	       	       };
          				       	       	       } else {
					       	       	       	       // System.out.println("Adding write listener...");
						       	       	       newMetaData.rowListener = new RowListener() {
          				       		       	       	       public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
										       // System.out.println("Row written event.");
					       			       	       	       try {
				       				       	       	       	       mutex.acquire();
              					       		       	       	       	       newMetaData.bufferRowMeta = rowMeta;
              					       		       	       	       	       newMetaData.bufferRowData.push(row);
              					       		       	       	       	       mutex.release();
              					       		       	       	       } catch (Exception ex) {
					       			       	       	       	       System.out.println("Exception in row written event.");
              					       		       	       	       }
            					       	       	       	       }
          				       	       	       	       	       public void errorRowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
          				       	       	       	       	       }
          				       	       	       	       	       public void rowReadEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
          				       	       	       	       	       }
          				       	       	       	       };
          				       	       	       }
          				       	       	       clients.add(newMetaData);
					       	       	       // System.out.println("Adding listener to step...");
        		       		       	       	       step.addRowListener(newMetaData.rowListener);
        		       		       	       	       List<RowListener> list = step.getRowListeners();
        		       		       	       	       // System.out.println("Current number of listeners for this step: " + list.size());
        		       		       	       	       metaData = newMetaData;
          			       	       	       }
				       	       	       // send data, clean, release lock

        		       	       	       	       // Pass along the rows of data...
        		       	       	       	       if ( useXML ) {

          			       	       	       	       // Send the result back as XML
          			       	       	       	       response.setContentType( "text/xml" );
          			       	       	       	       response.setCharacterEncoding( Const.XML_ENCODING );
          			       	       	       	       out.print( XMLHandler.getXMLHeader( Const.XML_ENCODING ) );

          			       	       	       	       out.println( XMLHandler.openTag( XML_TAG ) );

          			       	       	       	       if ( metaData.bufferRowMeta != null ) {

            				       	       	       	       // Row Meta data
            				       	       	       	       out.println( metaData.bufferRowMeta.getMetaXML() );

            				       	       	       	       List<Object[]> data = metaData.bufferRowData.get();

            				       	       	       	       // Nr of lines
            				       	       	       	       out.println( XMLHandler.addTagValue( "nr_rows", data.size() ) );

            				       	       	       	       // Rows of data
            				       	       	       	       for (int i = 0; i < data.size(); i++ ) {
              					       	       	       	       out.println( metaData.bufferRowMeta.getDataXML(data.get(i)));
            				       	       	       	       }
          			       	       	       	       }

          			       	       	       	       out.println( XMLHandler.closeTag( XML_TAG ) );

        		       	       	       	       } else {
          			       	       	       	       response.setContentType( "text/html;charset=UTF-8" );

          			       	       	       	       out.println( "<HTML>" );
          			       	       	       	       out.println( "<HEAD>" );
          			       	       	       	       out.println( "<TITLE> Preview " + metaData.type + " rows for step " + stepName + "</TITLE>" );
          			       	       	       	       out.println( "<META http-equiv=\"Refresh\" content=\"1;url="
            					       	       	       	       + convertContextPath( CONTEXT_PATH )
            					       	       	       	       + "?trans=" + URLEncoder.encode( transName, "UTF-8" ) 
            					       	       	       	       + "&id=" + URLEncoder.encode( id, "UTF-8" ) 
            					       	       	       	       + "&type=" + URLEncoder.encode( type, "UTF-8" ) 
            					       	       	       	       + "&step=" + URLEncoder.encode(stepName, "UTF-8")
            					       	       	       	       + "#bottom\">" );
          			       	       	       	       out.println( "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" );
          			       	       	       	       out.println( "</HEAD>" );
          			       	       	       	       out.println( "<BODY>" );
          			       	       	       	       out.println( "<H1> Preview " + metaData.type + " rows for step " + stepName + "</H1>" );

          			       	       	       	       try {
            				       	       	       	       out.println( "<table border=\"1\">" );

            				       	       	       	       if ( metaData.bufferRowMeta != null ) {
              					       	       	       	       // Print a header row containing all the field names...
              					       	       	       	       out.print( "<tr><th>#</th>" );
              					       	       	       	       for ( ValueMetaInterface valueMeta : metaData.bufferRowMeta.getValueMetaList() ) {
                					       	       	       	       out.print( "<th>" + valueMeta.getName() + "</th>" );
              					       	       	       	       }
              					       	       	       	       out.println( "</tr>" );

              					       	       	       	       // Now output the data rows...
            				       	       	       	       	       List<Object[]> data = metaData.bufferRowData.get();
              					       	       	       	       for ( int r = 0; r < data.size(); r++ ) {
                					       	       	       	       Object[] rowData = data.get(r);
                					       	       	       	       out.print( "<tr>" );

                					       	       	       	       out.println( "<td>" + ( r + 1 ) + "</td>" );

                					       	       	       	       for ( int v = 0; v < metaData.bufferRowMeta.size(); v++ ) {
                  						       	       	       	       ValueMetaInterface valueMeta = metaData.bufferRowMeta.getValueMeta( v );
                  						       	       	       	       Object valueData = rowData[v];
                  						       	       	       	       out.println( "<td>" + valueMeta.getString( valueData ) + "</td>" );
                					       	       	       	       }
                					       	       	       	       out.println( "</tr>" );
              					       	       	       	       }
            				       	       	       	       }

            				       	       	       	       out.println( "</table>" );

            				       	       	       	       out.println( "<p>" );

          			       	       	       	       } catch ( Exception ex ) {
            				       	       	       	       out.println( "<p>" );
            				       	       	       	       out.println( "<pre>" );
            				       	       	       	       out.println( encoder.encodeForHTML( Const.getStackTracker( ex ) ) );
            				       	       	       	       out.println( "</pre>" );
          			       	       	       	       }

          			       	       	       	       out.println( "<p>" );
          			       	       	       	       out.println( "<a name=\"bottom\"></a>" );
          			       	       	       	       out.println( "</BODY>" );
          			       	       	       	       out.println( "</HTML>" );
				       	       	       }
        		       	       	       }
				       	       mutex.release();
			       	       } catch (Exception ex) {
				       	       System.out.println("Exception!");
            			       	       out.println(Const.getStackTracker( ex ));
          			       	       out.println( "<HTML>" );
          			       	       out.println( "<HEAD>" );
          			       	       out.println( "<TITLE> Preview data response</TITLE>" );
          			       	       out.println( "</HEAD>" );
          			       	       out.println( "<BODY>" );
          			       	       out.println( "ERROR: Exception: " + ex.toString());
            			       	       out.println( encoder.encodeForHTML( Const.getStackTracker( ex ) ) );
          			       	       out.println( "</BODY>" );
          			       	       out.println( "</HTML>" );
          			       	       mutex.release();

			       	       }
      		       	       } else {
			       	       System.out.println("Step not found.");
        		       	       if ( useXML ) {
          			       	       out.println( new WebResult( WebResult.STRING_ERROR, "Could Not Find Step", stepName ).getXML() );
        		       	       } else {
          			       	       out.println( "<HTML>" );
          			       	       out.println( "<HEAD>" );
          			       	       out.println( "<TITLE> Preview data response</TITLE>" );
          			       	       out.println( "</HEAD>" );
          			       	       out.println( "<BODY>" );
          			       	       out.println( "ERROR: Could not find step " + stepName );
          			       	       out.println( "<p>" );
          			       	       out.println( "<a href=\""
            					       	       + convertContextPath( GetTransStatusServlet.CONTEXT_PATH ) + "?name="
            					       	       + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" ) + "\">"
            					       	       + BaseMessages.getString( PKG, "TransStatusServlet.BackToTransStatusPage" ) + "</a><p>" );
          			       	       out.println( "</BODY>" );
          			       	       out.println( "</HTML>" );
        		       	       }
      		       	       }
    	       	       } else {
		       	       System.out.println("Trans not found.");
        	       	       if ( useXML ) {
          		       	       out.println( new WebResult( WebResult.STRING_ERROR, "Could Not Find Trans", transName ).getXML() );
        	       	       } else {
          		       	       out.println( "<HTML>" );
          		       	       out.println( "<HEAD>" );
          		       	       out.println( "<TITLE> Preview data response</TITLE>" );
          		       	       out.println( "</HEAD>" );
          		       	       out.println( "<BODY>" );
          		       	       out.println( "ERROR: Could not find Trans " + transName );
          		       	       out.println( "<p>" );
          		       	       out.println( "<a href=\""
            				       	       + convertContextPath( GetTransStatusServlet.CONTEXT_PATH ) + "?name="
            				       	       + URLEncoder.encode( transName, "UTF-8" ) + "&id=" + URLEncoder.encode( id, "UTF-8" ) + "\">"
            				       	       + BaseMessages.getString( PKG, "TransStatusServlet.BackToTransStatusPage" ) + "</a><p>" );
          		       	       out.println( "</BODY>" );
          		       	       out.println( "</HTML>" );
        	       	       }
    	       	       }
	}

	public String toString() {
    		return "Trans Status Handler";
	}

	public String getService() {
    		return CONTEXT_PATH + " (" + toString() + ")";
	}

	public String getContextPath() {
    		return CONTEXT_PATH;
	}

}
