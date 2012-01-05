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

package org.pentaho.di.www;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepStatus;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;

public class GetTransStatusServlet extends BaseHttpServlet implements CarteServletInterface {
  private static Class<?> PKG = GetTransStatusServlet.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private static final long serialVersionUID = 3634806745372015720L;
  public static final String CONTEXT_PATH = "/kettle/transStatus";

  public GetTransStatusServlet() {
  }
  
  public GetTransStatusServlet(TransformationMap transformationMap) {
    super(transformationMap);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH)) {
      return;
    }

    if (log.isDebug())
      logDebug(BaseMessages.getString(PKG, "TransStatusServlet.Log.TransStatusRequested"));

    String transName = request.getParameter("name");
    String id = request.getParameter("id");
    boolean useXML = "Y".equalsIgnoreCase(request.getParameter("xml"));
    int startLineNr = Const.toInt(request.getParameter("from"), 0);

    response.setStatus(HttpServletResponse.SC_OK);

    if (useXML) {
      response.setContentType("text/xml");
      response.setCharacterEncoding(Const.XML_ENCODING);
    } else {
      response.setCharacterEncoding("UTF-8");
      response.setContentType("text/html;charset=UTF-8");
    }

    PrintWriter out = response.getWriter();

    // ID is optional...
    //
    Trans trans;
    CarteObjectEntry entry;
    if (Const.isEmpty(id)) {
    	// get the first transformation that matches...
    	//
    	entry = getTransformationMap().getFirstCarteObjectEntry(transName);
    	if (entry==null) {
    		trans = null;
    	} else {
    		id = entry.getId();
    		trans = getTransformationMap().getTransformation(entry);
    	}
    } else {
    	// Take the ID into account!
    	//
    	entry = new CarteObjectEntry(transName, id);
    	trans = getTransformationMap().getTransformation(entry);
    }
    
    if (trans != null) {
      String status = trans.getStatus();
      int lastLineNr = CentralLogStore.getLastBufferLineNr();
      String logText = CentralLogStore.getAppender().getBuffer(trans.getLogChannel().getLogChannelId(), false, startLineNr, lastLineNr).toString();

      if (useXML) {
        response.setContentType("text/xml");
        response.setCharacterEncoding(Const.XML_ENCODING);
        out.print(XMLHandler.getXMLHeader(Const.XML_ENCODING));

        SlaveServerTransStatus transStatus = new SlaveServerTransStatus(transName, entry.getId(), status);
        transStatus.setFirstLoggingLineNr(startLineNr);
        transStatus.setLastLoggingLineNr(lastLineNr);

        for (int i = 0; i < trans.nrSteps(); i++) {
          StepInterface baseStep = trans.getRunThread(i);
          if ((baseStep.isRunning()) || baseStep.getStatus() != StepExecutionStatus.STATUS_EMPTY) {
            StepStatus stepStatus = new StepStatus(baseStep);
            transStatus.getStepStatusList().add(stepStatus);
          }
        }

        // The log can be quite large at times, we are going to put a base64 encoding around a compressed stream
        // of bytes to handle this one.

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = new GZIPOutputStream(baos);
        gzos.write(logText.getBytes());
        gzos.close();

        String loggingString = new String(Base64.encodeBase64(baos.toByteArray()));
        transStatus.setLoggingString(loggingString);

        // Also set the result object...
        //
        transStatus.setResult(trans.getResult());

        // Is the transformation paused?
        //
        transStatus.setPaused(trans.isPaused());

        // Send the result back as XML
        //
        try {
          out.println(transStatus.getXML());
        } catch (KettleException e) {
          throw new ServletException("Unable to get the transformation status in XML format", e);
        }
      } else {
        response.setContentType("text/html;charset=UTF-8");

        out.println("<HTML>");
        out.println("<HEAD>");
        out.println("<TITLE>" + BaseMessages.getString(PKG, "TransStatusServlet.KettleTransStatus") + "</TITLE>");
        out.println("<META http-equiv=\"Refresh\" content=\"10;url=" + convertContextPath(CONTEXT_PATH) + "?name=" + URLEncoder.encode(transName, "UTF-8") + "\">");
        out.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        out.println("</HEAD>");
        out.println("<BODY>");
        out.println("<H1>" + BaseMessages.getString(PKG, "TransStatusServlet.TopTransStatus", transName) + "</H1>");

        try {
          out.println("<table border=\"1\">");
          out.print("<tr> <th>" + 
        		BaseMessages.getString(PKG, "TransStatusServlet.TransName") + "</th> <th>" + 
        		BaseMessages.getString(PKG, "TransStatusServlet.CarteObjectId") + "</th> <th>" +
        		BaseMessages.getString(PKG, "TransStatusServlet.TransStatus") + "</th> </tr>"
        	);

          out.print("<tr>");
          out.print("<td>" + transName + "</td>");
          out.print("<td>" + id + "</td>");
          out.print("<td>" + status + "</td>");
          out.print("</tr>");
          out.print("</table>");

          out.print("<p>");

          if ((trans.isFinished() && trans.isRunning()) || (!trans.isRunning() && !trans.isPreparing() && !trans.isInitializing())) {
            out.print("<a href=\"" + convertContextPath(StartTransServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(transName, "UTF-8") + "&id="+id+"\">"
                + BaseMessages.getString(PKG, "TransStatusServlet.StartTrans") + "</a>");
            out.print("<p>");
            out.print("<a href=\"" + convertContextPath(PrepareExecutionTransServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(transName, "UTF-8") + "&id="+id+"\">"
                + BaseMessages.getString(PKG, "TransStatusServlet.PrepareTrans") + "</a><br>");
          } else if (trans.isRunning()) {
            out.print("<a href=\"" + convertContextPath(PauseTransServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(transName, "UTF-8") + "&id="+id+"\">"
                + BaseMessages.getString(PKG, "PauseStatusServlet.PauseResumeTrans") + "</a><br>");
            out.print("<a href=\"" + convertContextPath(StopTransServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(transName, "UTF-8") + "&id="+id+"\">"
                + BaseMessages.getString(PKG, "TransStatusServlet.StopTrans") + "</a>");
            out.print("<p>");
          }
          out.print("<a href=\"" + convertContextPath(CleanupTransServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(transName, "UTF-8") + "&id="+id+"\">"
              + BaseMessages.getString(PKG, "TransStatusServlet.CleanupTrans") + "</a>");
          out.print("<p>");

          out.println("<table border=\"1\">");
          out.print("<tr> <th>" + BaseMessages.getString(PKG, "TransStatusServlet.Stepname") + "</th> <th>"
              + BaseMessages.getString(PKG, "TransStatusServlet.CopyNr") + "</th> <th>" + BaseMessages.getString(PKG, "TransStatusServlet.Read") + "</th> <th>"
              + BaseMessages.getString(PKG, "TransStatusServlet.Written") + "</th> <th>" + BaseMessages.getString(PKG, "TransStatusServlet.Input")
              + "</th> <th>" + BaseMessages.getString(PKG, "TransStatusServlet.Output") + "</th> " + "<th>"
              + BaseMessages.getString(PKG, "TransStatusServlet.Updated") + "</th> <th>" + BaseMessages.getString(PKG, "TransStatusServlet.Rejected")
              + "</th> <th>" + BaseMessages.getString(PKG, "TransStatusServlet.Errors") + "</th> <th>"
              + BaseMessages.getString(PKG, "TransStatusServlet.Active") + "</th> <th>" + BaseMessages.getString(PKG, "TransStatusServlet.Time") + "</th> "
              + "<th>" + BaseMessages.getString(PKG, "TransStatusServlet.Speed") + "</th> <th>" + BaseMessages.getString(PKG, "TransStatusServlet.prinout")
              + "</th> </tr>");

          for (int i = 0; i < trans.nrSteps(); i++) {
            StepInterface step = trans.getRunThread(i);
            if ((step.isRunning()) || step.getStatus() != StepExecutionStatus.STATUS_EMPTY) {
              StepStatus stepStatus = new StepStatus(step);
              
              if (step.isRunning() && !step.isStopped() && !step.isPaused()) {
	              String sniffLink = " <a href=\""+
	              						convertContextPath(SniffStepServlet.CONTEXT_PATH) + 
	              						"?trans=" + URLEncoder.encode(transName, "UTF-8") + 
	              						"&id="+id+
	              						"&lines=50"+
	              						"&copynr="+step.getCopy()+
	              						"&type="+SniffStepServlet.TYPE_OUTPUT+
	              						"&step=" + URLEncoder.encode(step.getStepname(), "UTF-8") + 
	              						"\">"+stepStatus.getStepname()+"</a>";
	              stepStatus.setStepname(sniffLink);
              }
              
              out.print(stepStatus.getHTMLTableRow());
            }
          }
          out.println("</table>");
          out.println("<p>");

          out.print("<a href=\"" + convertContextPath(GetTransStatusServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(transName, "UTF-8") + "&id="+id+"&xml=y\">"
              + BaseMessages.getString(PKG, "TransStatusServlet.ShowAsXml") + "</a><br>");
          out.print("<a href=\"" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "\">" + BaseMessages.getString(PKG, "TransStatusServlet.BackToStatusPage") + "</a><br>");
          out.print("<p><a href=\"" + convertContextPath(GetTransStatusServlet.CONTEXT_PATH) + "?name=" + URLEncoder.encode(transName, "UTF-8") + "&id="+id+"\">"
              + BaseMessages.getString(PKG, "TransStatusServlet.Refresh") + "</a>");

          // Put the logging below that.

          out.println("<p>");
          out.println("<textarea id=\"translog\" cols=\"120\" rows=\"20\" wrap=\"off\" name=\"Transformation log\" readonly=\"readonly\">" + logText
              + "</textarea>");

          out.println("<script type=\"text/javascript\"> ");
          out.println("  translog.scrollTop=translog.scrollHeight; ");
          out.println("</script> ");
          out.println("<p>");
        } catch (Exception ex) {
          out.println("<p>");
          out.println("<pre>");
          ex.printStackTrace(out);
          out.println("</pre>");
        }

        out.println("<p>");
        out.println("</BODY>");
        out.println("</HTML>");
      }
    } else {
      if (useXML) {
        out.println(new WebResult(WebResult.STRING_ERROR, BaseMessages.getString(PKG, "TransStatusServlet.Log.CoundNotFindSpecTrans", transName)));
      } else {
        out.println("<H1>" + BaseMessages.getString(PKG, "TransStatusServlet.Log.CoundNotFindTrans", transName) + "</H1>");
        out.println("<a href=\"" + convertContextPath(GetStatusServlet.CONTEXT_PATH) + "\">" + BaseMessages.getString(PKG, "TransStatusServlet.BackToStatusPage") + "</a><p>");
      }
    }
  }

  public String toString() {
    return "Trans Status Handler";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
