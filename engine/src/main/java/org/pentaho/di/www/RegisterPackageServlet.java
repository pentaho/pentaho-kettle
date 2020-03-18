/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.UUIDUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.www.service.zip.ZipService;
import org.pentaho.di.www.service.zip.ZipServiceKettle;
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

  private static Class<?> PKG = RegisterPackageServlet.class; // for i18n purposes, needed by Translator2!!

  public static final String CONTEXT_PATH = "/kettle/registerPackage";

  private static final long serialVersionUID = -7582587179862317791L;

  public static final String PARAMETER_LOAD = "load";
  public static final String PARAMETER_TYPE = "type";
  public static final String TYPE_JOB = "job";
  public static final String TYPE_TRANS = "trans";

  public RegisterPackageServlet() {
    // empty on purpose.
  }

  private static ZipService zipService;

  /*
    Initialize the servlet and services.
   */
  @Override
  public void setup( TransformationMap transformationMap, JobMap jobMap, SocketRepository socketRepository,
                     List<SlaveServerDetection> detections ) {
    super.setup( transformationMap, jobMap, socketRepository, detections );
    setZipService( new ZipServiceKettle() );
  }

  @Override
  public String getContextPath() {
    return CONTEXT_PATH;
  }

  @Override
  WebResult generateBody( HttpServletRequest request, HttpServletResponse response, boolean useXML ) throws KettleException {
    String archiveUrl = copyRequestToDirectory( request, createTempDirString() );

    String load = request.getParameter( PARAMETER_LOAD ); // the resource to load

    String zipBaseUrl = extract( archiveUrl );

    if ( !Utils.isEmpty( load ) ) {
      String fileUrl = getStartFileUrl( zipBaseUrl, load );
      String resultId;

      if ( isJob( request ) ) {
        Node node =
            getConfigNode( zipBaseUrl, Job.CONFIGURATION_IN_EXPORT_FILENAME, JobExecutionConfiguration.XML_TAG );
        JobExecutionConfiguration jobExecutionConfiguration = new JobExecutionConfiguration( node );

        JobMeta jobMeta = new JobMeta( fileUrl, jobExecutionConfiguration.getRepository() );
        JobConfiguration jobConfiguration = new JobConfiguration( jobMeta, jobExecutionConfiguration );

        Job job = createJob( jobConfiguration );
        resultId = job.getContainerObjectId();
      } else {
        Node node =
            getConfigNode( zipBaseUrl, Trans.CONFIGURATION_IN_EXPORT_FILENAME,
                TransExecutionConfiguration.XML_TAG );
        TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration( node );

        TransMeta transMeta = new TransMeta( fileUrl, transExecutionConfiguration.getRepository() );
        TransConfiguration transConfiguration = new TransConfiguration( transMeta, transExecutionConfiguration );

        Trans trans = createTrans( transConfiguration );
        resultId = trans.getContainerObjectId();
      }

      deleteArchive( archiveUrl ); // zip file no longer needed, contents were extracted
      return new WebResult( WebResult.STRING_OK, fileUrl, resultId );
    }

    return null;
  }

  /**
   * Determines if <code>request</code> is kettle job.
   * @param request http request with parameters.
   * @return true if a job, false otherwise.
   */
  protected boolean isJob( HttpServletRequest request ) {
    return isJob( request.getParameter( PARAMETER_TYPE ) );
  }

  /**
   * Determines if <code>parameterTypeValue</code> is kettle job.
   * @param parameterTypeValue http parameter type value
   * @return
   */
  protected boolean isJob( String parameterTypeValue ) {
    return TYPE_JOB.equalsIgnoreCase( parameterTypeValue );
  }

  @Override
  protected boolean useXML( HttpServletRequest request ) {
    // always XML
    return true;
  }

  /**
   * Determine the start file url for the ktr/kjb.
   * @param archiveUrl
   * @param requestLoad
   * @return file path.
   */
  protected String getStartFileUrl( String archiveUrl, String requestLoad ) {
    return concat( archiveUrl,  requestLoad );
  }

  /**
   * Combine the two paths with with a separator such as: <code>basePath</code> + / + <code>relativePath</code>.
   * @param basePath full path to root of directory.
   * @param relativePath a non-absolute path.
   * @return combined file path.
   */
  protected String concat( String basePath,  String relativePath ) {
    return FilenameUtils.concat( basePath, relativePath );
  }

  /**
   * Retrieve config xml, <code>xmlTag</code> from combined path of  <code>archiveUrl</code> and <code>fileName</code>.
   * @param archiveUrl root file path.
   * @param fileName xml configuration file at root of <code>archiveUrl</code>.
   * @param xmlTag xml root tag.
   * @return configuration node.
   * @throws KettleXMLException
   */
  protected Node getConfigNode( String archiveUrl, String fileName, String xmlTag ) throws KettleXMLException {
    String configUrl = concat( archiveUrl, fileName );
    Document configDoc = XMLHandler.loadXMLFile( configUrl );
    return XMLHandler.getSubNode( configDoc, xmlTag );
  }

  /**
   * Create temporary directory with unique random folder at base.
   * @return unique temporary directory path.
   */
  protected String createTempDirString() {
    return createTempDirString( System.getProperty( "java.io.tmpdir" ) );
  }

  /**
   * Create temporary directory with unique random folder at base.
   * @param baseDirectory base file path directory.
   * @return unique temporary directory path.
   */
  protected String createTempDirString( String baseDirectory ) {
    return concat( baseDirectory, UUIDUtil.getUUIDAsString() );
  }

  /**
   * Create temporary directory with unique random folder at base.
   * <p>
   * Format : <code>baseDirectory</code> + / + <code>folderName</code>
   * @param baseDirectory base file path directory.
   * @param folderName
   * @return unique temporary directory path.
   */
  protected String createTempDirString( String baseDirectory, String folderName ) {
    return concat( baseDirectory, folderName );
  }

  /**
   * Copy file specified in <code>request</code> to <code>directory</code>.
   * @param request http request with payload.
   * @param directory local destination directory.
   * @return copied file path.
   * @throws KettleException
   */
  protected String copyRequestToDirectory( HttpServletRequest request, String directory ) throws KettleException {
    try {
      return copyRequestToDirectory( request.getInputStream(), directory );
    } catch ( IOException ioe ) {
      throw new KettleException( BaseMessages.getString( PKG, "RegisterPackageServlet.Exception.CopyRequest",  directory ), ioe );
    }
  }

  /**
   * Copy contents of <code>inputStream</code> to <code>directory</code>. Expecting zip file.
   * @param inputStream zip file input stream.
   * @param directory local destination directory.
   * @return copied file path.
   * @throws KettleException
   */
  protected String copyRequestToDirectory( InputStream inputStream, String directory ) throws KettleException {
    String copiedFilePath;
    try {
      FileObject foDirectory = KettleVFS.getFileObject( directory );
      if ( !foDirectory.exists() ) {
        foDirectory.createFolder();
      }
      FileObject tempZipFile = KettleVFS.createTempFile( "export", ".zip", directory );
      OutputStream outputStream = KettleVFS.getOutputStream( tempZipFile, false );
      copyAndClose( inputStream, outputStream );
      copiedFilePath = tempZipFile.getName().getPath();
    } catch ( IOException ioe ) {
      throw new KettleException( BaseMessages.getString( PKG, "RegisterPackageServlet.Exception.CopyRequest",  directory ), ioe );
    }

    return copiedFilePath;
  }

  /**
   * Copy contents from <code>inputStream</code> over to <code>outputStream</code> and close <code>outputStream</code>.
   * @param inputStream
   * @param outputStream
   * @throws IOException
   */
  protected void copyAndClose( InputStream inputStream, OutputStream outputStream ) throws IOException {
    IOUtils.copy( inputStream, outputStream );
    outputStream.flush();
    IOUtils.closeQuietly( outputStream );
  }

  /**
   * Decompress zip file.
   * @param zipFilePath zip file path.
   * @return returns path to new directory containing files.
   * @throws KettleException
   */
  protected String extract( String zipFilePath ) throws KettleException {
    File fileZip = new File( zipFilePath );
    File parentDir = fileZip.getParentFile();

    extract( fileZip.getPath(), parentDir.getPath() );
    return parentDir.toString();
  }

  /**
   * Decompress zip file.
   * @param zipFilePath zip file.
   * @param destinationDirectory destination directory.
   * @throws KettleException
   */
  protected void extract( String zipFilePath, String destinationDirectory ) throws KettleException {
    getZipService().extract( zipFilePath, destinationDirectory );
  }

  /**
   * Remove file.
   * @param file path to file.
   */
  protected void deleteArchive( String file ) {
    FileUtils.deleteQuietly( new File( file ) );
  }

  protected static void setZipService( ZipService aZipService ) {
    zipService = aZipService;
  }

  protected static ZipService getZipService() {
    return zipService;
  }

}
