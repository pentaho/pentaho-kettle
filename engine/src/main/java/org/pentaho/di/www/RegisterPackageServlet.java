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
import java.io.OutputStream;
import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class RegisterPackageServlet extends BaseJobServlet {

  public static final String CONTEXT_PATH = "/kettle/registerPackage";

  private static final long serialVersionUID = -7582587179862317791L;

  public static final String PARAMETER_LOAD = "load";
  public static final String PARAMETER_TYPE = "type";
  public static final String TYPE_JOB = "job";
  public static final String TYPE_TRANS = "trans";

  private static final String ZIP_CONT = "zip:{0}!{1}";

  @Override
  public String getContextPath() {
    return CONTEXT_PATH;
  }

  @Override
  WebResult generateBody( HttpServletRequest request, HttpServletResponse response, boolean useXML ) throws KettleException, IOException  {
    FileObject tempFile = KettleVFS.createTempFile( "export", ".zip", System.getProperty( "java.io.tmpdir" ) );
    OutputStream out = KettleVFS.getOutputStream( tempFile, false );
    IOUtils.copy( request.getInputStream(), out );
    out.flush();
    IOUtils.closeQuietly( out );

    String archiveUrl = tempFile.getName().toString();
    String load = request.getParameter( PARAMETER_LOAD ); // the resource to load

    if ( !Utils.isEmpty( load ) ) {
      String fileUrl = MessageFormat.format( ZIP_CONT, archiveUrl, load );
      boolean isJob = TYPE_JOB.equalsIgnoreCase( request.getParameter( PARAMETER_TYPE ) );
      String resultId;

      if ( isJob ) {
        Node node =
            getConfigNodeFromZIP( archiveUrl, Job.CONFIGURATION_IN_EXPORT_FILENAME, JobExecutionConfiguration.XML_TAG );
        JobExecutionConfiguration jobExecutionConfiguration = new JobExecutionConfiguration( node );

        JobMeta jobMeta = new JobMeta( fileUrl, jobExecutionConfiguration.getRepository() );
        JobConfiguration jobConfiguration = new JobConfiguration( jobMeta, jobExecutionConfiguration );

        Job job = createJob( jobConfiguration );
        resultId = job.getContainerObjectId();
      } else {
        Node node =
            getConfigNodeFromZIP( archiveUrl, Trans.CONFIGURATION_IN_EXPORT_FILENAME,
                TransExecutionConfiguration.XML_TAG );
        TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration( node );

        TransMeta transMeta = new TransMeta( fileUrl, transExecutionConfiguration.getRepository() );
        TransConfiguration transConfiguration = new TransConfiguration( transMeta, transExecutionConfiguration );

        Trans trans = createTrans( transConfiguration );
        resultId = trans.getContainerObjectId();
      }

      return new WebResult( WebResult.STRING_OK, fileUrl, resultId );
    }

    return null;
  }

  @Override
  protected boolean useXML( HttpServletRequest request ) {
    // always XML
    return true;
  }

  protected Node getConfigNodeFromZIP( Object archiveUrl, Object fileName, String xml_tag ) throws KettleXMLException {
    String configUrl = MessageFormat.format( ZIP_CONT, archiveUrl, fileName );
    Document configDoc = XMLHandler.loadXMLFile( configUrl );
    return XMLHandler.getSubNode( configDoc, xml_tag );
  }
}
