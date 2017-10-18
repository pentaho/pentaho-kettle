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

package org.pentaho.di.job.entries.http;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines an HTTP job entry.
 *
 * @author Matt
 * @since 05-11-2003
 *
 */
public class JobEntryHTTP extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryHTTP.class; // for i18n purposes, needed by Translator2!!

  private static final String URL_FIELDNAME = "URL";
  private static final String UPLOADFILE_FIELDNAME = "UPLOAD";
  private static final String TARGETFILE_FIELDNAME = "DESTINATION";


  // Base info
  private String url;

  private String targetFilename;

  private boolean fileAppended;

  private boolean dateTimeAdded;

  private String targetFilenameExtension;

  // Send file content to server?
  private String uploadFilename;

  // The fieldname that contains the URL
  // Get it from a previous transformation with Result.
  private String urlFieldname;
  private String uploadFieldname;
  private String destinationFieldname;

  private boolean runForEveryRow;

  // Proxy settings
  private String proxyHostname;

  private String proxyPort;

  private String nonProxyHosts;

  private String username;

  private String password;

  private boolean addfilenameresult;

  private String[] headerName;

  private String[] headerValue;

  public JobEntryHTTP( String n ) {
    super( n, "" );
    url = null;
    addfilenameresult = true;
  }

  public JobEntryHTTP() {
    this( "" );
  }

  private void allocate( int nrHeaders ) {
    headerName = new String[nrHeaders];
    headerValue = new String[nrHeaders];
  }

  @Override
  public Object clone() {
    JobEntryHTTP je = (JobEntryHTTP) super.clone();
    if ( headerName != null ) {
      int nrHeaders = headerName.length;
      je.allocate( nrHeaders );
      System.arraycopy( headerName, 0, je.headerName, 0, nrHeaders );
      System.arraycopy( headerValue, 0, je.headerValue, 0, nrHeaders );
    }
    return je;
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( super.getXML() );

    retval.append( "      " ).append( XMLHandler.addTagValue( "url", url ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "targetfilename", targetFilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "file_appended", fileAppended ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "date_time_added", dateTimeAdded ) );
    retval
      .append( "      " ).append( XMLHandler.addTagValue( "targetfilename_extension", targetFilenameExtension ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "uploadfilename", uploadFilename ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "run_every_row", runForEveryRow ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "url_fieldname", urlFieldname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "upload_fieldname", uploadFieldname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "dest_fieldname", destinationFieldname ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "username", username ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( password ) ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "proxy_host", proxyHostname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxy_port", proxyPort ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "non_proxy_hosts", nonProxyHosts ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "addfilenameresult", addfilenameresult ) );
    retval.append( "      <headers>" ).append( Const.CR );
    if ( headerName != null ) {
      for ( int i = 0; i < headerName.length; i++ ) {
        retval.append( "        <header>" ).append( Const.CR );
        retval.append( "          " ).append( XMLHandler.addTagValue( "header_name", headerName[i] ) );
        retval.append( "          " ).append( XMLHandler.addTagValue( "header_value", headerValue[i] ) );
        retval.append( "        </header>" ).append( Const.CR );
      }
    }
    retval.append( "      </headers>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      url = XMLHandler.getTagValue( entrynode, "url" );
      targetFilename = XMLHandler.getTagValue( entrynode, "targetfilename" );
      fileAppended = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "file_appended" ) );
      dateTimeAdded = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "date_time_added" ) );
      targetFilenameExtension = Const.NVL( XMLHandler.getTagValue( entrynode, "targetfilename_extension" ),
          XMLHandler.getTagValue( entrynode, "targetfilename_extention" ) );

      uploadFilename = XMLHandler.getTagValue( entrynode, "uploadfilename" );

      urlFieldname = XMLHandler.getTagValue( entrynode, "url_fieldname" );
      uploadFieldname = XMLHandler.getTagValue( entrynode, "upload_fieldname" );
      destinationFieldname = XMLHandler.getTagValue( entrynode, "dest_fieldname" );
      runForEveryRow = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "run_every_row" ) );

      username = XMLHandler.getTagValue( entrynode, "username" );
      password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "password" ) );

      proxyHostname = XMLHandler.getTagValue( entrynode, "proxy_host" );
      proxyPort = XMLHandler.getTagValue( entrynode, "proxy_port" );
      nonProxyHosts = XMLHandler.getTagValue( entrynode, "non_proxy_hosts" );
      addfilenameresult =
        "Y".equalsIgnoreCase( Const.NVL( XMLHandler.getTagValue( entrynode, "addfilenameresult" ), "Y" ) );
      Node headers = XMLHandler.getSubNode( entrynode, "headers" );

      // How many field headerName?
      int nrHeaders = XMLHandler.countNodes( headers, "header" );
      allocate( nrHeaders );
      for ( int i = 0; i < nrHeaders; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( headers, "header", i );
        headerName[i] = XMLHandler.getTagValue( fnode, "header_name" );
        headerValue[i] = XMLHandler.getTagValue( fnode, "header_value" );
      }
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'HTTP' from XML node", xe );
    }
  }

  @Override
  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      url = rep.getJobEntryAttributeString( id_jobentry, "url" );
      targetFilename = rep.getJobEntryAttributeString( id_jobentry, "targetfilename" );
      fileAppended = rep.getJobEntryAttributeBoolean( id_jobentry, "file_appended" );
      dateTimeAdded = rep.getJobEntryAttributeBoolean( id_jobentry, "date_time_added" );
      targetFilenameExtension = Const.NVL( rep.getJobEntryAttributeString( id_jobentry, "targetfilename_extension" ),
          rep.getJobEntryAttributeString( id_jobentry, "targetfilename_extention" ) );

      uploadFilename = rep.getJobEntryAttributeString( id_jobentry, "uploadfilename" );

      urlFieldname = rep.getJobEntryAttributeString( id_jobentry, "url_fieldname" );
      uploadFieldname = rep.getJobEntryAttributeString( id_jobentry, "upload_fieldname" );
      destinationFieldname = rep.getJobEntryAttributeString( id_jobentry, "dest_fieldname" );
      runForEveryRow = rep.getJobEntryAttributeBoolean( id_jobentry, "run_every_row" );

      username = rep.getJobEntryAttributeString( id_jobentry, "username" );
      password =
        Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "password" ) );

      proxyHostname = rep.getJobEntryAttributeString( id_jobentry, "proxy_host" );
      proxyPort = rep.getJobEntryAttributeString( id_jobentry, "proxy_port" ); // backward compatible.

      nonProxyHosts = rep.getJobEntryAttributeString( id_jobentry, "non_proxy_hosts" );
      addfilenameresult =
        "Y".equalsIgnoreCase( Const
          .NVL( rep.getJobEntryAttributeString( id_jobentry, "addfilenameresult" ), "Y" ) );

      // How many headerName?
      int argnr = rep.countNrJobEntryAttributes( id_jobentry, "header_name" );
      allocate( argnr );

      for ( int a = 0; a < argnr; a++ ) {
        headerName[a] = rep.getJobEntryAttributeString( id_jobentry, a, "header_name" );
        headerValue[a] = rep.getJobEntryAttributeString( id_jobentry, a, "header_value" );
      }
    } catch ( KettleException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'HTTP' from the repository for id_jobentry="
        + id_jobentry, dbe );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "url", url );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "targetfilename", targetFilename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "file_appended", fileAppended );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "date_time_added", dateTimeAdded );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "targetfilename_extension", targetFilenameExtension );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "uploadfilename", uploadFilename );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "url_fieldname", urlFieldname );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "upload_fieldname", uploadFieldname );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "dest_fieldname", destinationFieldname );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "run_every_row", runForEveryRow );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "username", username );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "password", Encr
        .encryptPasswordIfNotUsingVariables( password ) );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxy_host", proxyHostname );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxy_port", proxyPort );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "non_proxy_hosts", nonProxyHosts );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "addfilenameresult", addfilenameresult );
      if ( headerName != null ) {
        for ( int i = 0; i < headerName.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "header_name", headerName[i] );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "header_value", headerValue[i] );
        }
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to load job entry of type 'HTTP' to the repository for id_job=" + id_job, dbe );
    }
  }

  /**
   * @return Returns the URL.
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url
   *          The URL to set.
   */
  public void setUrl( String url ) {
    this.url = url;
  }

  /**
   * @return Returns the target filename.
   */
  public String getTargetFilename() {
    return targetFilename;
  }

  /**
   * @param targetFilename
   *          The target filename to set.
   */
  public void setTargetFilename( String targetFilename ) {
    this.targetFilename = targetFilename;
  }

  public String getNonProxyHosts() {
    return nonProxyHosts;
  }

  public void setNonProxyHosts( String nonProxyHosts ) {
    this.nonProxyHosts = nonProxyHosts;
  }

  public boolean isAddFilenameToResult() {
    return addfilenameresult;
  }

  public void setAddFilenameToResult( boolean addfilenameresult ) {
    this.addfilenameresult = addfilenameresult;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword( String password ) {
    this.password = password;
  }

  public String getProxyHostname() {
    return proxyHostname;
  }

  public void setProxyHostname( String proxyHostname ) {
    this.proxyHostname = proxyHostname;
  }

  public String getProxyPort() {
    return proxyPort;
  }

  public void setProxyPort( String proxyPort ) {
    this.proxyPort = proxyPort;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername( String username ) {
    this.username = username;
  }

  public String[] getHeaderName() {
    return headerName;
  }

  public void setHeaderName( String[] headerName ) {
    this.headerName = headerName;
  }

  public String[] getHeaderValue() {
    return headerValue;
  }

  public void setHeaderValue( String[] headerValue ) {
    this.headerValue = headerValue;
  }

  /**
   * We made this one synchronized in the JVM because otherwise, this is not thread safe. In that case if (on an
   * application server for example) several HTTP's are running at the same time, you get into problems because the
   * System.setProperty() calls are system wide!
   */
  @Override
  public synchronized Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setResult( false );

    logBasic( BaseMessages.getString( PKG, "JobHTTP.StartJobEntry" ) );

    // Get previous result rows...
    List<RowMetaAndData> resultRows;
    String urlFieldnameToUse, uploadFieldnameToUse, destinationFieldnameToUse;

    if ( Utils.isEmpty( urlFieldname ) ) {
      urlFieldnameToUse = URL_FIELDNAME;
    } else {
      urlFieldnameToUse = urlFieldname;
    }

    if ( Utils.isEmpty( uploadFieldname ) )  {
      uploadFieldnameToUse = UPLOADFILE_FIELDNAME;
    } else {
      uploadFieldnameToUse = uploadFieldname;
    }

    if ( Utils.isEmpty( destinationFieldname ) )  {
      destinationFieldnameToUse = TARGETFILE_FIELDNAME;
    } else {
      destinationFieldnameToUse = destinationFieldname;
    }

    if ( runForEveryRow ) {
      resultRows = previousResult.getRows();
      if ( resultRows == null ) {
        result.setNrErrors( 1 );
        logError( BaseMessages.getString( PKG, "JobHTTP.Error.UnableGetResultPrevious" ) );
        return result;
      }
    } else {
      resultRows = new ArrayList<RowMetaAndData>();
      RowMetaAndData row = new RowMetaAndData();
      row.addValue(
        new ValueMetaString( urlFieldnameToUse ), environmentSubstitute( url ) );
      row.addValue(
        new ValueMetaString( uploadFieldnameToUse ), environmentSubstitute( uploadFilename ) );
      row.addValue(
        new ValueMetaString( destinationFieldnameToUse ), environmentSubstitute( targetFilename ) );
      resultRows.add( row );
    }

    URL server = null;

    String beforeProxyHost = System.getProperty( "http.proxyHost" );
    String beforeProxyPort = System.getProperty( "http.proxyPort" );
    String beforeNonProxyHosts = System.getProperty( "http.nonProxyHosts" );

    for ( int i = 0; i < resultRows.size() && result.getNrErrors() == 0; i++ ) {
      RowMetaAndData row = resultRows.get( i );

      OutputStream outputFile = null;
      OutputStream uploadStream = null;
      BufferedInputStream fileStream = null;
      InputStream input = null;

      try {
        String urlToUse = environmentSubstitute( row.getString( urlFieldnameToUse, "" ) );
        String realUploadFile = environmentSubstitute( row.getString( uploadFieldnameToUse, "" ) );
        String realTargetFile = environmentSubstitute( row.getString(  destinationFieldnameToUse,  "" ) );

        logBasic( BaseMessages.getString( PKG, "JobHTTP.Log.ConnectingURL", urlToUse ) );

        if ( !Utils.isEmpty( proxyHostname ) ) {
          System.setProperty( "http.proxyHost", environmentSubstitute( proxyHostname ) );
          System.setProperty( "http.proxyPort", environmentSubstitute( proxyPort ) );
          if ( nonProxyHosts != null ) {
            System.setProperty( "http.nonProxyHosts", environmentSubstitute( nonProxyHosts ) );
          }
        }

        if ( !Utils.isEmpty( username ) ) {
          Authenticator.setDefault( new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
              String realPassword = Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( password ) );
              return new PasswordAuthentication( environmentSubstitute( username ), realPassword != null
                ? realPassword.toCharArray() : new char[] {} );
            }
          } );
        }

        if ( dateTimeAdded ) {
          SimpleDateFormat daf = new SimpleDateFormat();
          Date now = new Date();

          daf.applyPattern( "yyyMMdd" );
          realTargetFile += "_" + daf.format( now );
          daf.applyPattern( "HHmmss" );
          realTargetFile += "_" + daf.format( now );

          if ( !Utils.isEmpty( targetFilenameExtension ) ) {
            realTargetFile += "." + environmentSubstitute( targetFilenameExtension );
          }
        }

        // Create the output File...
        outputFile = KettleVFS.getOutputStream( realTargetFile, this, fileAppended );

        // Get a stream for the specified URL
        server = new URL( urlToUse );
        URLConnection connection = server.openConnection();

        // if we have HTTP headers, add them
        if ( !Utils.isEmpty( headerName ) ) {
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "JobHTTP.Log.HeadersProvided" ) );
          }
          for ( int j = 0; j < headerName.length; j++ ) {
            if ( !Utils.isEmpty( headerValue[j] ) ) {
              connection.setRequestProperty(
                environmentSubstitute( headerName[j] ), environmentSubstitute( headerValue[j] ) );
              if ( log.isDebug() ) {
                log.logDebug( BaseMessages.getString(
                  PKG, "JobHTTP.Log.HeaderSet", environmentSubstitute( headerName[j] ),
                  environmentSubstitute( headerValue[j] ) ) );
              }
            }
          }
        }

        connection.setDoOutput( true );

        // See if we need to send a file over?
        if ( !Utils.isEmpty( realUploadFile ) ) {
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobHTTP.Log.SendingFile", realUploadFile ) );
          }

          // Grab an output stream to upload data to web server
          uploadStream = connection.getOutputStream();
          fileStream = new BufferedInputStream( new FileInputStream( new File( realUploadFile ) ) );
          try {
            int c;
            while ( ( c = fileStream.read() ) >= 0 ) {
              uploadStream.write( c );
            }
          } finally {
            // Close upload and file
            if ( uploadStream != null ) {
              uploadStream.close();
              uploadStream = null;
            }
            if ( fileStream != null ) {
              fileStream.close();
              fileStream = null;
            }
          }
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobHTTP.Log.FinishedSendingFile" ) );
          }
        }

        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobHTTP.Log.StartReadingReply" ) );
        }

        // Read the result from the server...
        input = connection.getInputStream();
        Date date = new Date( connection.getLastModified() );
        logBasic( BaseMessages.getString( PKG, "JobHTTP.Log.ReplayInfo", connection.getContentType(), date ) );

        int oneChar;
        long bytesRead = 0L;
        while ( ( oneChar = input.read() ) != -1 ) {
          outputFile.write( oneChar );
          bytesRead++;
        }

        logBasic( BaseMessages.getString( PKG, "JobHTTP.Log.FinisedWritingReply", bytesRead, realTargetFile ) );

        if ( addfilenameresult ) {
          // Add to the result files...
          ResultFile resultFile =
            new ResultFile(
              ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject( realTargetFile, this ), parentJob
                .getJobname(), toString() );
          result.getResultFiles().put( resultFile.getFile().toString(), resultFile );
        }

        result.setResult( true );
      } catch ( MalformedURLException e ) {
        result.setNrErrors( 1 );
        logError( BaseMessages.getString( PKG, "JobHTTP.Error.NotValidURL", url, e.getMessage() ) );
        logError( Const.getStackTracker( e ) );
      } catch ( IOException e ) {
        result.setNrErrors( 1 );
        logError( BaseMessages.getString( PKG, "JobHTTP.Error.CanNotSaveHTTPResult", e.getMessage() ) );
        logError( Const.getStackTracker( e ) );
      } catch ( Exception e ) {
        result.setNrErrors( 1 );
        logError( BaseMessages.getString( PKG, "JobHTTP.Error.ErrorGettingFromHTTP", e.getMessage() ) );
        logError( Const.getStackTracker( e ) );
      } finally {
        // Close it all
        try {
          if ( uploadStream != null ) {
            uploadStream.close(); // just to make sure
          }
          if ( fileStream != null ) {
            fileStream.close(); // just to make sure
          }

          if ( input != null ) {
            input.close();
          }
          if ( outputFile != null ) {
            outputFile.close();
          }
        } catch ( Exception e ) {
          logError( BaseMessages.getString( PKG, "JobHTTP.Error.CanNotCloseStream", e.getMessage() ) );
          result.setNrErrors( 1 );
        }

        // Set the proxy settings back as they were on the system!
        System.setProperty( "http.proxyHost", Const.NVL( beforeProxyHost, "" ) );
        System.setProperty( "http.proxyPort", Const.NVL( beforeProxyPort, "" ) );
        System.setProperty( "http.nonProxyHosts", Const.NVL( beforeNonProxyHosts, "" ) );
      }

    }

    return result;
  }

  @Override
  public boolean evaluates() {
    return true;
  }

  public String getUploadFilename() {
    return uploadFilename;
  }

  public void setUploadFilename( String uploadFilename ) {
    this.uploadFilename = uploadFilename;
  }

  /**
   * @return Returns the Result URL Fieldname.
   */
  public String getUrlFieldname() {
    return urlFieldname;
  }

  /**
   * @param getFieldname
   *          The Result URL Fieldname to set.
   */
  public void setUrlFieldname( String getFieldname ) {
    this.urlFieldname = getFieldname;
  }

  /*
   * @return Returns the Upload File Fieldname
   */
  public String getUploadFieldname() {
    return uploadFieldname;
  }

  /*
   * @param uploadFieldName
   *         The Result Upload Fieldname to use
   */
  public void setUploadFieldname( String uploadFieldname ) {
    this.uploadFieldname = uploadFieldname;
  }

  /*
   * @return Returns the Result Destination Path Fieldname
   */
  public String getDestinationFieldname() {
    return destinationFieldname;
  }

  /*
   * @param destinationFieldname
   *           The Result Destination Fieldname to set.
   */
  public void setDestinationFieldname( String destinationFieldname ) {
    this.destinationFieldname = destinationFieldname;
  }

  /**
   * @return Returns the runForEveryRow.
   */
  public boolean isRunForEveryRow() {
    return runForEveryRow;
  }

  /**
   * @param runForEveryRow
   *          The runForEveryRow to set.
   */
  public void setRunForEveryRow( boolean runForEveryRow ) {
    this.runForEveryRow = runForEveryRow;
  }

  /**
   * @return Returns the fileAppended.
   */
  public boolean isFileAppended() {
    return fileAppended;
  }

  /**
   * @param fileAppended
   *          The fileAppended to set.
   */
  public void setFileAppended( boolean fileAppended ) {
    this.fileAppended = fileAppended;
  }

  /**
   * @return Returns the dateTimeAdded.
   */
  public boolean isDateTimeAdded() {
    return dateTimeAdded;
  }

  /**
   * @param dateTimeAdded
   *          The dateTimeAdded to set.
   */
  public void setDateTimeAdded( boolean dateTimeAdded ) {
    this.dateTimeAdded = dateTimeAdded;
  }

  /**
   * @return Returns the uploadFilenameExtension.
   */
  public String getTargetFilenameExtension() {
    return targetFilenameExtension;
  }

  /**
   * @param uploadFilenameExtension
   *          The uploadFilenameExtension to set.
   */
  public void setTargetFilenameExtension( String uploadFilenameExtension ) {
    this.targetFilenameExtension = uploadFilenameExtension;
  }

  /**
   * @return Returns the uploadFilenameExtension.
   * @deprecated Use {@link JobEntryHTTP#getTargetFilenameExtension()} instead
   */
  @Deprecated
  public String getTargetFilenameExtention() {
    return targetFilenameExtension;
  }

  /**
   * @param uploadFilenameExtension
   *          The uploadFilenameExtension to set.
   * @deprecated Use {@link JobEntryHTTP#setTargetFilenameExtension( String uploadFilenameExtension )} instead
   */
  @Deprecated
  public void setTargetFilenameExtention( String uploadFilenameExtension ) {
    this.targetFilenameExtension = uploadFilenameExtension;
  }

  @Override
  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    String realUrl = jobMeta.environmentSubstitute( url );
    ResourceReference reference = new ResourceReference( this );
    reference.getEntries().add( new ResourceEntry( realUrl, ResourceType.URL ) );
    references.add( reference );
    return references;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    JobEntryValidatorUtils.andValidator().validate( this, "targetFilename", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "targetFilenameExtention", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "uploadFilename", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "proxyPort", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.integerValidator() ) );
  }

}
