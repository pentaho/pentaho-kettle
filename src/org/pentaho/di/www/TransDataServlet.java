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

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.jdbc.ThinConnection;
import org.pentaho.di.core.jdbc.ThinDriver;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.sql.SqlTransExecutor;
import org.pentaho.di.trans.step.RowAdapter;

/**
 * This servlet allows a user to get data from a "service" which is a transformation step.
 * 
 * @author matt
 *
 */
public class TransDataServlet extends BaseHttpServlet implements CartePluginInterface {
  private static Class<?> PKG = TransDataServlet.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private static final long serialVersionUID = 3634806745372015720L;

  public static final String CONTEXT_PATH = ThinDriver.SERVICE_NAME+"/sql";

  AtomicLong dataSize = new AtomicLong(0L);

  public TransDataServlet() {
  }
  
  public TransDataServlet(TransformationMap transformationMap, JobMap jobMap) {
    super(transformationMap, jobMap);
  }

  public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }
  
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH)) {
      return;
    }
    
    if (log.isDebug()) logDebug(BaseMessages.getString(PKG, "GetStatusServlet.StatusRequested"));
    response.setStatus(HttpServletResponse.SC_OK);

    response.setContentType("binary/jdbc");
    response.setBufferSize(10000);
    // response.setHeader("Content-Length", Integer.toString(Integer.MAX_VALUE));

    final OutputStream outputStream = response.getOutputStream();
    final DataOutputStream dos = new DataOutputStream(outputStream);

    String sqlQuery = request.getHeader("SQL");
    final int maxRows = Const.toInt(request.getHeader("MaxRows"), -1);
    
    final String debugTransFile = request.getHeader(ThinConnection.ARG_DEBUGTRANS);
    
    // Parse the variables in the request header...
    //
    Map<String, String> parameters = getParametersFromRequestHeader(request);
    
    try {
      // Execute the SQL using a few transformations...
      //
      final SqlTransExecutor executor = new SqlTransExecutor(sqlQuery, getTransformationMap().getSlaveServerConfig().getServices(), parameters);
            
      // First write the service name and the metadata
      //
      dos.writeUTF(executor.getServiceName());

      // Then send the transformation names and carte container IDs
      //
      dos.writeUTF(SqlTransExecutor.calculateTransname(executor.getSql(), true));
      String serviceContainerObjectId = UUID.randomUUID().toString();
      dos.writeUTF(serviceContainerObjectId);
      dos.writeUTF(SqlTransExecutor.calculateTransname(executor.getSql(), false));
      String genContainerObjectId = UUID.randomUUID().toString();
      dos.writeUTF(genContainerObjectId);
      
      final AtomicBoolean firstRow = new AtomicBoolean(true);
      
      // Now execute the query transformation(s) and pass the data to the output stream...
      //
      // TODO: allow global repository configuration in the services config file
      //
      final AtomicInteger rowCounter = new AtomicInteger(0);
      final AtomicBoolean wroteRowMeta = new AtomicBoolean(false);
      
      executor.executeQuery(new RowAdapter() { @Override
      public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
        
        // On the first row, write the metadata...
        //
        try {
          if (firstRow.get()) {
            firstRow.set(false);
            rowMeta.writeMeta(dos);
            wroteRowMeta.set(true);
          }
          rowMeta.writeData(dos, row);
          if (maxRows>0 && rowCounter.incrementAndGet()>maxRows) {
             executor.getServiceTrans().stopAll();
          }
          dataSize.set(dos.size());
        } catch (Exception e) {
          if (!executor.getServiceTrans().isStopped()) {
            throw new KettleStepException(e);
          }
        }
      } });

      
      // For logging and tracking purposes, let's expose both the service transformation as well 
      // as the generated transformation on this very carte instance
      //
      TransMeta serviceTransMeta = executor.getGenTransMeta();
      Trans serviceTrans = executor.getServiceTrans();
      if (serviceTrans!=null) {
        // not dual
        TransConfiguration serviceTransConfiguration = new TransConfiguration(serviceTransMeta, new TransExecutionConfiguration());
        transformationMap.addTransformation(serviceTransMeta.getName(), serviceContainerObjectId, serviceTrans, serviceTransConfiguration);
      }

      // And the generated transformation...
      //
      TransMeta genTransMeta = executor.getGenTransMeta();
      Trans genTrans = executor.getGenTrans();
      TransConfiguration genTransConfiguration = new TransConfiguration(genTransMeta, new TransExecutionConfiguration());
      transformationMap.addTransformation(genTransMeta.getName(), genContainerObjectId, genTrans, genTransConfiguration);
      
      // Log the generated transformation if needed
      //
      if (!Const.isEmpty(debugTransFile)) {
        // Store it to temp file for debugging!
        //
        try {
          FileOutputStream fos = new FileOutputStream(debugTransFile);
          fos.write(XMLHandler.getXMLHeader(Const.XML_ENCODING).getBytes(Const.XML_ENCODING));
          fos.write(genTransMeta.getXML().getBytes(Const.XML_ENCODING));
          fos.close();
        } catch (Exception fnfe) {
          throw new KettleException(fnfe);
        }
      }
      
      executor.waitUntilFinished();
      
      // Check if no row metadata was written.  The client is still going to expect it...
      // Since we know it, we'll pass it.
      //
      if (!wroteRowMeta.get()) {
        RowMetaInterface stepFields = executor.getGenTransMeta().getStepFields(executor.getResultStepName());
        stepFields.writeMeta(dos);
      }
      
      // The client has to come back to the GetTransStatus servlet to check for errors
      // So that's all we do here...
      //
      
    } catch(Exception e) {
      log.logError("Error executing SQL query: "+sqlQuery, e);
      response.sendError(500, Const.getStackTracker(e));
    }
    finally {
      System.out.println("bytes written: "+dataSize);
    }
  }

  public static Map<String, String> getParametersFromRequestHeader(HttpServletRequest request) {
    Map<String, String> parameters = new HashMap<String, String>();
    Enumeration<?> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = (String) headerNames.nextElement();
      if (headerName.startsWith("PARAMETER_")) {
        String parameterName = headerName.substring("PARAMETER_".length());
        String value = request.getHeader(headerName);
        if (!Const.isEmpty(parameterName)) {
          parameters.put(parameterName, Const.NVL(value, ""));
        }
      }
    }

    return parameters;
  }

  public String toString() {
    return "Transformation data service";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
  
  public String getContextPath() {
    return CONTEXT_PATH;
  }

}
