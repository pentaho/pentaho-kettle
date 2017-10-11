/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.owasp.encoder.Encode;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepInterface;


public class SniffStepServlet extends BaseHttpServlet implements CartePluginInterface {
  private static Class<?> PKG = GetTransStatusServlet.class; // for i18n purposes, needed by Translator2!!

  private static final long serialVersionUID = 3634806745372015720L;
  public static final String CONTEXT_PATH = "/kettle/sniffStep";

  public static final String TYPE_INPUT = "input";
  public static final String TYPE_OUTPUT = "output";

  public static final String XML_TAG = "step-sniff";

  final class MetaAndData {
    public RowMetaInterface bufferRowMeta;
    public List<Object[]> bufferRowData;
  }

  public SniffStepServlet() {
  }

  public SniffStepServlet( TransformationMap transformationMap ) {
    super( transformationMap );
  }

  /**
<div id="mindtouch">
    <h1>/kettle/sniffStep</h1>
    <a name="GET"></a>
    <h2>GET</h2>
    <p>Sniff metadata and data from the specified step of the specified transformation.</p>

    <p><b>Example Request:</b><br />
    <pre function="syntax.xml">
    GET /kettle/sniffStep?trans=dummy-trans&step=tf&xml=Y&lines=10
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
    <td>trans</td>
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
    <td>lines</td>
    <td>Number of lines to collect and include into response. If not provided 0 lines will be collected.</td>
    <td>integer, optional</td>
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
      logDebug( BaseMessages.getString( PKG, "TransStatusServlet.Log.SniffStepRequested" ) );
    }

    String transName = request.getParameter( "trans" );
    String id = request.getParameter( "id" );
    String stepName = request.getParameter( "step" );
    int copyNr = Const.toInt( request.getParameter( "copynr" ), 0 );
    final int nrLines = Const.toInt( request.getParameter( "lines" ), 0 );
    String type = Const.NVL( request.getParameter( "type" ), TYPE_OUTPUT );
    boolean useXML = "Y".equalsIgnoreCase( request.getParameter( "xml" ) );

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
    if ( Utils.isEmpty( id ) ) {
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

    if ( trans != null ) {

      // Find the step to look at...
      //
      StepInterface step = null;
      List<StepInterface> stepInterfaces = trans.findBaseSteps( stepName );
      for ( int i = 0; i < stepInterfaces.size(); i++ ) {
        StepInterface look = stepInterfaces.get( i );
        if ( look.getCopy() == copyNr ) {
          step = look;
        }
      }
      if ( step != null ) {

        // Add a listener to the transformation step...
        //
        final boolean read = type.equalsIgnoreCase( TYPE_INPUT );
        final boolean written = type.equalsIgnoreCase( TYPE_OUTPUT ) || !read;
        final MetaAndData metaData = new MetaAndData();

        metaData.bufferRowMeta = null;
        metaData.bufferRowData = new ArrayList<Object[]>();

        RowListener rowListener = new RowListener() {
          public void rowReadEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
            if ( read && metaData.bufferRowData.size() < nrLines ) {
              metaData.bufferRowMeta = rowMeta;
              metaData.bufferRowData.add( row );
            }
          }

          public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
            if ( written && metaData.bufferRowData.size() < nrLines ) {
              metaData.bufferRowMeta = rowMeta;
              metaData.bufferRowData.add( row );
            }
          }

          public void errorRowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
          }
        };

        step.addRowListener( rowListener );

        // Wait until we have enough rows...
        //
        while ( metaData.bufferRowData.size() < nrLines
          && step.isRunning() && !trans.isFinished() && !trans.isStopped() ) {

          try {
            Thread.sleep( 100 );
          } catch ( InterruptedException e ) {
            // Ignore
            //
            break;
          }
        }

        // Remove the row listener
        //
        step.removeRowListener( rowListener );

        // Pass along the rows of data...
        //
        if ( useXML ) {

          // Send the result back as XML
          //
          response.setContentType( "text/xml" );
          response.setCharacterEncoding( Const.XML_ENCODING );
          out.print( XMLHandler.getXMLHeader( Const.XML_ENCODING ) );

          out.println( XMLHandler.openTag( XML_TAG ) );

          if ( metaData.bufferRowMeta != null ) {

            // Row Meta data
            //
            out.println( metaData.bufferRowMeta.getMetaXML() );

            // Nr of lines
            //
            out.println( XMLHandler.addTagValue( "nr_rows", metaData.bufferRowData.size() ) );

            // Rows of data
            //
            for ( int i = 0; i < metaData.bufferRowData.size(); i++ ) {
              Object[] rowData = metaData.bufferRowData.get( i );
              out.println( metaData.bufferRowMeta.getDataXML( rowData ) );
            }
          }

          out.println( XMLHandler.closeTag( XML_TAG ) );

        } else {
          response.setContentType( "text/html;charset=UTF-8" );

          out.println( "<HTML>" );
          out.println( "<HEAD>" );
          out.println( "<TITLE>" + BaseMessages.getString( PKG, "SniffStepServlet.SniffResults" ) + "</TITLE>" );
          out.println( "<META http-equiv=\"Refresh\" content=\"10;url="
            + convertContextPath( CONTEXT_PATH ) + "?name=" + URLEncoder.encode( transName, "UTF-8" ) + "&id="
            + URLEncoder.encode( id, "UTF-8" ) + "\">" );
          out.println( "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" );
          out.println( "</HEAD>" );
          out.println( "<BODY>" );
          out.println( "<H1>"
            + Encode.forHtml( BaseMessages.getString(
              PKG, "SniffStepServlet.SniffResultsForStep", stepName ) ) + "</H1>" );

          try {
            out.println( "<table border=\"1\">" );

            if ( metaData.bufferRowMeta != null ) {
              // Print a header row containing all the field names...
              //
              out.print( "<tr><th>#</th>" );
              for ( ValueMetaInterface valueMeta : metaData.bufferRowMeta.getValueMetaList() ) {
                out.print( "<th>" + valueMeta.getName() + "</th>" );
              }
              out.println( "</tr>" );

              // Now output the data rows...
              //
              for ( int r = 0; r < metaData.bufferRowData.size(); r++ ) {
                Object[] rowData = metaData.bufferRowData.get( r );
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
            out.println( Encode.forHtml( Const.getStackTracker( ex ) ) );
            out.println( "</pre>" );
          }

          out.println( "<p>" );
          out.println( "</BODY>" );
          out.println( "</HTML>" );
        }
      } else {
        if ( useXML ) {
          out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
            PKG, "SniffStepServlet.Log.CoundNotFindSpecStep", stepName ) ).getXML() );
        } else {
          out.println( "<H1>"
            + Encode.forHtml( BaseMessages.getString(
              PKG, "SniffStepServlet.Log.CoundNotFindSpecStep", stepName ) ) + "</H1>" );
          out.println( "<a href=\""
            + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">"
            + BaseMessages.getString( PKG, "TransStatusServlet.BackToStatusPage" ) + "</a><p>" );
        }
      }
    } else {
      if ( useXML ) {
        out.println( new WebResult( WebResult.STRING_ERROR, BaseMessages.getString(
          PKG, "SniffStepServlet.Log.CoundNotFindSpecTrans", transName ) ).getXML() );
      } else {
        out.println( "<H1>"
          + Encode.forHtml( BaseMessages.getString(
            PKG, "SniffStepServlet.Log.CoundNotFindTrans", transName ) ) + "</H1>" );
        out.println( "<a href=\""
          + convertContextPath( GetStatusServlet.CONTEXT_PATH ) + "\">"
          + BaseMessages.getString( PKG, "TransStatusServlet.BackToStatusPage" ) + "</a><p>" );
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
