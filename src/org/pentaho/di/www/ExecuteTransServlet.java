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
package org.pentaho.di.www;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransAdapter;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

public class ExecuteTransServlet extends BaseHttpServlet implements CarteServletInterface {

  private static Class<?>   PKG         = ExecuteTransServlet.class;  // i18n
  
  private static final long serialVersionUID  = -5879219287669847357L;

  public static final String  CONTEXT_PATH    = "/kettle/executeTrans";

  public ExecuteTransServlet() {
  }

  public ExecuteTransServlet(TransformationMap transformationMap) {
    super(transformationMap);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (isJettyMode() && !request.getContextPath().startsWith(CONTEXT_PATH)) {
      return;
    }

    if (log.isDebug())
      logDebug(BaseMessages.getString(PKG, "ExecuteTransServlet.Log.ExecuteTransRequested"));

    // Options taken from PAN
    //
    String[] knownOptions = new String[] { "rep", "user", "pass", "trans", "level", };
    
    String repOption = request.getParameter("rep");
    String userOption = request.getParameter("user");
    String passOption = Encr.decryptPasswordOptionallyEncrypted( request.getParameter("pass") );
    String transOption = request.getParameter("trans");
    String levelOption = request.getParameter("level");

    response.setStatus(HttpServletResponse.SC_OK);

    PrintWriter out = response.getWriter();
    
    try {
      
      final Repository repository = openRepository(repOption, userOption, passOption);
      final TransMeta transMeta = loadTransformation(repository, transOption);

      // Set the servlet parameters as variables in the transformation
      //
      String[] parameters = transMeta.listParameters();
      Enumeration<?> parameterNames = request.getParameterNames();
      while (parameterNames.hasMoreElements()) {
        String parameter = (String) parameterNames.nextElement();
        String[] values = request.getParameterValues(parameter);
        
        // Ignore the known options. set the rest as variables
        //
        if (Const.indexOfString(parameter, knownOptions)<0) {
          // If it's a trans parameter, set it, otherwise simply set the variable
          //
          if (Const.indexOfString(parameter, parameters)<0) {
            transMeta.setParameterValue(parameter, values[0]);
          } else {
            transMeta.setVariable(parameter, values[0]);
          }
        }
      }
      
      TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();
      LogLevel logLevel = LogLevel.getLogLevelForCode(levelOption);
      transExecutionConfiguration.setLogLevel(logLevel);
      TransConfiguration transConfiguration = new TransConfiguration(transMeta, transExecutionConfiguration);
      
      String carteObjectId = UUID.randomUUID().toString();
      SimpleLoggingObject servletLoggingObject = new SimpleLoggingObject(CONTEXT_PATH, LoggingObjectType.CARTE, null);
      servletLoggingObject.setContainerObjectId(carteObjectId);
      servletLoggingObject.setLogLevel(logLevel);
      
      // Create the transformation and store in the list...
      //
      final Trans trans = new Trans(transMeta, servletLoggingObject);
      
      trans.setRepository(repository);
      trans.setSocketRepository(getSocketRepository());

      getTransformationMap().addTransformation(transMeta.getName(), carteObjectId, trans, transConfiguration);
      trans.setContainerObjectId(carteObjectId);

      if (repository != null) {
        // The repository connection is open: make sure we disconnect from the repository once we
        // are done with this transformation.
        //
        trans.addTransListener(new TransAdapter() {
          public void transFinished(Trans trans) {
            repository.disconnect();
          }
        });
      }
      
      // Pass the servlet print writer to the transformation...
      //
      trans.setServletPrintWriter(out);
      
      try {
        // Execute the transformation...
        //
        trans.prepareExecution(null);
        trans.startThreads();
        trans.waitUntilFinished();
        
        out.flush();
        
      } catch(Exception executionException) {
        String logging = CentralLogStore.getAppender().getBuffer(trans.getLogChannelId(), false).toString();
        throw new KettleException("Error executing transformation: "+logging, executionException);
      }
    } catch (Exception ex) {
      
      out.println(new WebResult(WebResult.STRING_ERROR, BaseMessages.getString(PKG, "ExecuteTransServlet.Error.UnexpectedError", Const.CR + Const.getStackTracker(ex))));
    }
  }

  private TransMeta loadTransformation(Repository repository, String trans) throws KettleException {
    
    if (repository==null) {
      
      // Without a repository it's a filename --> file:///foo/bar/trans.ktr
      //
      TransMeta transMeta = new TransMeta(trans);
      return transMeta;
      
    } else {
      
      // With a repository we need to load it from /foo/bar/Transformation
      // We need to extract the folder name from the path in front of the name...
      //
      String directoryPath;
      String name;
      int lastSlash = trans.lastIndexOf(RepositoryDirectory.DIRECTORY_SEPARATOR);
      if (lastSlash<0) {
        directoryPath = "/";
        name=trans;
      } else {
        directoryPath = trans.substring(0, lastSlash);
        name=trans.substring(lastSlash+1);
      }
      RepositoryDirectoryInterface directory = repository.loadRepositoryDirectoryTree().findDirectory(directoryPath);
      
      ObjectId transformationID = repository.getTransformationID(name, directory);
      TransMeta transMeta = repository.loadTransformation(transformationID, null);
      return transMeta;
    }
  }

  private Repository openRepository(String repositoryName, String user, String pass) throws KettleException {
    
    if (Const.isEmpty(repositoryName)) return null;
    
    RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
    repositoriesMeta.readData();
    RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( repositoryName );
    PluginRegistry registry = PluginRegistry.getInstance();
    Repository repository = registry.loadClass(
           RepositoryPluginType.class,
           repositoryMeta,
           Repository.class
      );
    repository.init(repositoryMeta);
    
    return repository;
  }

  public String toString() {
    return "Start transformation";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }
}
