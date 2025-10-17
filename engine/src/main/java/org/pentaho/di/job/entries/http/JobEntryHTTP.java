/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.job.entries.http;

import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This defines an HTTP job entry.
 *
 * @author Matt
 * @since 05-11-2003
 */
public class JobEntryHTTP extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryHTTP.class; // for i18n purposes, needed by Translator2!!

  private static final String URL_FIELDNAME = "URL";
  private static final String UPLOADFILE_FIELDNAME = "UPLOAD";
  private static final String TARGETFILE_FIELDNAME = "DESTINATION";
  private static final String SCHEMA_FILE = "file";

  private static final String STR_10_SPACES = "          ";
  private static final String STR_6_SPACES = "      ";

  private static final String TAG_URL = "url";
  private static final String TAG_TARGET_FILENAME = "targetfilename";
  private static final String TAG_FILE_APPENDED = "file_appended";
  private static final String TAG_DATE_TIME_ADDED = "date_time_added";
  private static final String TAG_TARGET_FILENAME_EXTENSION = "targetfilename_extension";
  private static final String TAG_TARGET_FILENAME_EXTENTION = "targetfilename_extention";
  private static final String TAG_UPLOAD_FILENAME = "uploadfilename";
  private static final String TAG_RUN_EVERY_ROW = "run_every_row";
  private static final String TAG_URL_FIELDNAME = "url_fieldname";
  private static final String TAG_UPLOAD_FIELDNAME = "upload_fieldname";
  private static final String TAG_DEST_FIELDNAME = "dest_fieldname";
  private static final String TAG_USERNAME = "username";
  private static final String TAG_PASSWORD = "password";
  private static final String TAG_PROXY_HOST = "proxy_host";
  private static final String TAG_PROXY_PORT = "proxy_port";
  private static final String TAG_NON_PROXY_HOSTS = "non_proxy_hosts";
  private static final String TAG_ADD_FILENAME_RESULT = "addfilenameresult";
  private static final String TAG_HEADERS = "headers";
  private static final String TAG_HEADER = "header";
  private static final String TAG_HEADER_NAME = "header_name";
  private static final String TAG_HEADER_VALUE = "header_value";

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

  // Response status
  private int responseStatusCode = 0;

  public JobEntryHTTP( String n ) {
    super( n, "" );
    url = null;
    addfilenameresult = true;
  }

  public JobEntryHTTP() {
    this( "" );
  }

  private void allocate( int nrHeaders ) {
    headerName = new String[ nrHeaders ];
    headerValue = new String[ nrHeaders ];
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

    retval.append( STR_6_SPACES ).append(XMLHandler.addTagValue(TAG_URL, url));
    retval.append( STR_6_SPACES ).append(XMLHandler.addTagValue(TAG_TARGET_FILENAME, targetFilename));
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_FILE_APPENDED, fileAppended ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_DATE_TIME_ADDED, dateTimeAdded ) );
    retval
      .append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_TARGET_FILENAME_EXTENSION, targetFilenameExtension ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_UPLOAD_FILENAME, uploadFilename ) );

    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_RUN_EVERY_ROW, runForEveryRow ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_URL_FIELDNAME, urlFieldname ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_UPLOAD_FIELDNAME, uploadFieldname ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_DEST_FIELDNAME, destinationFieldname ) );

    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_USERNAME, username ) );
    retval.append( STR_6_SPACES ).append(
      XMLHandler.addTagValue( TAG_PASSWORD, Encr.encryptPasswordIfNotUsingVariables( password ) ) );

    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_PROXY_HOST, proxyHostname ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_PROXY_PORT, proxyPort ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_NON_PROXY_HOSTS, nonProxyHosts ) );
    retval.append( STR_6_SPACES ).append( XMLHandler.addTagValue( TAG_ADD_FILENAME_RESULT, addfilenameresult ) );
    retval.append( "      <" ).append( TAG_HEADERS ).append( '>' ).append( Const.CR );
    if ( headerName != null ) {
      for ( int i = 0; i < headerName.length; i++ ) {
        retval.append( "        <" ).append( TAG_HEADER ).append( '>' ).append( Const.CR );
        retval.append( STR_10_SPACES ).append( XMLHandler.addTagValue( TAG_HEADER_NAME, headerName[ i ] ) );
        retval.append( STR_10_SPACES ).append( XMLHandler.addTagValue( TAG_HEADER_VALUE, headerValue[ i ] ) );
        retval.append( "        </" ).append( TAG_HEADER ).append( '>' ).append( Const.CR );
      }
    }
    retval.append( "      </" ).append( TAG_HEADERS ).append( '>' ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
                       Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      url = XMLHandler.getTagValue( entrynode, TAG_URL );
      targetFilename = XMLHandler.getTagValue( entrynode, TAG_TARGET_FILENAME );
      fileAppended = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_FILE_APPENDED ) );
      dateTimeAdded = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_DATE_TIME_ADDED ) );
      targetFilenameExtension = Const.NVL( XMLHandler.getTagValue( entrynode, TAG_TARGET_FILENAME_EXTENSION ),
        XMLHandler.getTagValue( entrynode, TAG_TARGET_FILENAME_EXTENTION ) );

      uploadFilename = XMLHandler.getTagValue( entrynode, TAG_UPLOAD_FILENAME );

      urlFieldname = XMLHandler.getTagValue( entrynode, TAG_URL_FIELDNAME );
      uploadFieldname = XMLHandler.getTagValue( entrynode, TAG_UPLOAD_FIELDNAME );
      destinationFieldname = XMLHandler.getTagValue( entrynode, TAG_DEST_FIELDNAME );
      runForEveryRow = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, TAG_RUN_EVERY_ROW ) );

      username = XMLHandler.getTagValue( entrynode, TAG_USERNAME );
      password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, TAG_PASSWORD ) );

      proxyHostname = XMLHandler.getTagValue( entrynode, TAG_PROXY_HOST );
      proxyPort = XMLHandler.getTagValue( entrynode, TAG_PROXY_PORT );
      nonProxyHosts = XMLHandler.getTagValue( entrynode, TAG_NON_PROXY_HOSTS );
      addfilenameresult =
        "Y".equalsIgnoreCase( Const.NVL( XMLHandler.getTagValue( entrynode, TAG_ADD_FILENAME_RESULT ), "Y" ) );
      Node headers = XMLHandler.getSubNode( entrynode, TAG_HEADERS );

      // How many field headerName?
      int nrHeaders = XMLHandler.countNodes( headers, TAG_HEADER );
      allocate( nrHeaders );
      for ( int i = 0; i < nrHeaders; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( headers, TAG_HEADER, i );
        headerName[ i ] = XMLHandler.getTagValue( fnode, TAG_HEADER_NAME );
        headerValue[ i ] = XMLHandler.getTagValue( fnode, TAG_HEADER_VALUE );
      }
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'HTTP' from XML node", xe );
    }
  }

  @Override
  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId idJobEntry, List<DatabaseMeta> databases,
                       List<SlaveServer> slaveServers ) throws KettleException {
    try {
      url = rep.getJobEntryAttributeString( idJobEntry, TAG_URL );
      targetFilename = rep.getJobEntryAttributeString( idJobEntry, TAG_TARGET_FILENAME );
      fileAppended = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_FILE_APPENDED );
      dateTimeAdded = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_DATE_TIME_ADDED );
      targetFilenameExtension = Const.NVL( rep.getJobEntryAttributeString( idJobEntry, TAG_TARGET_FILENAME_EXTENSION ),
        rep.getJobEntryAttributeString( idJobEntry, TAG_TARGET_FILENAME_EXTENTION ) );

      uploadFilename = rep.getJobEntryAttributeString( idJobEntry, TAG_UPLOAD_FILENAME );

      urlFieldname = rep.getJobEntryAttributeString( idJobEntry, TAG_URL_FIELDNAME );
      uploadFieldname = rep.getJobEntryAttributeString( idJobEntry, TAG_UPLOAD_FIELDNAME );
      destinationFieldname = rep.getJobEntryAttributeString( idJobEntry, TAG_DEST_FIELDNAME );
      runForEveryRow = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_RUN_EVERY_ROW );

      username = rep.getJobEntryAttributeString( idJobEntry, TAG_USERNAME );
      password =
        Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( idJobEntry, TAG_PASSWORD ) );

      proxyHostname = rep.getJobEntryAttributeString( idJobEntry, TAG_PROXY_HOST );
      proxyPort = rep.getJobEntryAttributeString( idJobEntry, TAG_PROXY_PORT ); // backward compatible.

      nonProxyHosts = rep.getJobEntryAttributeString( idJobEntry, TAG_NON_PROXY_HOSTS );
      addfilenameresult = rep.getJobEntryAttributeBoolean( idJobEntry, TAG_ADD_FILENAME_RESULT );

      // How many headerName?
      int argnr = rep.countNrJobEntryAttributes( idJobEntry, TAG_HEADER_NAME );
      allocate( argnr );

      for ( int a = 0; a < argnr; a++ ) {
        headerName[ a ] = rep.getJobEntryAttributeString( idJobEntry, a, TAG_HEADER_NAME );
        headerValue[ a ] = rep.getJobEntryAttributeString( idJobEntry, a, TAG_HEADER_VALUE );
      }
    } catch ( KettleException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'HTTP' from the repository for idJobEntry="
        + idJobEntry, dbe );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idJob ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( idJob, getObjectId(), TAG_URL, url );
      rep.saveJobEntryAttribute( idJob, getObjectId(), TAG_TARGET_FILENAME, targetFilename );
      rep.saveJobEntryAttribute( idJob, getObjectId(), TAG_FILE_APPENDED, fileAppended );
      rep.saveJobEntryAttribute( idJob, getObjectId(), TAG_DATE_TIME_ADDED, dateTimeAdded );
      rep.saveJobEntryAttribute( idJob, getObjectId(), TAG_TARGET_FILENAME_EXTENSION, targetFilenameExtension );

      rep.saveJobEntryAttribute( idJob, getObjectId(), TAG_UPLOAD_FILENAME, uploadFilename );

      rep.saveJobEntryAttribute( idJob, getObjectId(), TAG_URL_FIELDNAME, urlFieldname );
      rep.saveJobEntryAttribute( idJob, getObjectId(), TAG_UPLOAD_FIELDNAME, uploadFieldname );
      rep.saveJobEntryAttribute( idJob, getObjectId(), TAG_DEST_FIELDNAME, destinationFieldname );
      rep.saveJobEntryAttribute( idJob, getObjectId(), TAG_RUN_EVERY_ROW, runForEveryRow );

      rep.saveJobEntryAttribute( idJob, getObjectId(), TAG_USERNAME, username );
      rep.saveJobEntryAttribute( idJob, getObjectId(), TAG_PASSWORD, Encr
        .encryptPasswordIfNotUsingVariables( password ) );

      rep.saveJobEntryAttribute( idJob, getObjectId(), TAG_PROXY_HOST, proxyHostname );
      rep.saveJobEntryAttribute( idJob, getObjectId(), TAG_PROXY_PORT, proxyPort );
      rep.saveJobEntryAttribute( idJob, getObjectId(), TAG_NON_PROXY_HOSTS, nonProxyHosts );
      rep.saveJobEntryAttribute( idJob, getObjectId(), TAG_ADD_FILENAME_RESULT, addfilenameresult );
      if ( headerName != null ) {
        for ( int i = 0; i < headerName.length; i++ ) {
          rep.saveJobEntryAttribute( idJob, getObjectId(), i, TAG_HEADER_NAME, headerName[ i ] );
          rep.saveJobEntryAttribute( idJob, getObjectId(), i, TAG_HEADER_VALUE, headerValue[ i ] );
        }
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to load job entry of type 'HTTP' to the repository for idJob=" + idJob, dbe );
    }
  }

  /**
   * @return Returns the URL.
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param url The URL to set.
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
   * @param targetFilename The target filename to set.
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

  public void setAddFilenameToResult( boolean addFilenameResult ) {
    this.addfilenameresult = addFilenameResult;
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

  public int getResponseStatusCode() {
    return responseStatusCode;
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
    responseStatusCode = 0;

    logBasic( BaseMessages.getString( PKG, "JobHTTP.StartJobEntry" ) );

    // Get previous result rows...
    List<RowMetaAndData> resultRows;

    String urlFieldnameToUse = Utils.isEmpty( urlFieldname ) ? URL_FIELDNAME : urlFieldname;
    String uploadFieldnameToUse = Utils.isEmpty( uploadFieldname ) ? UPLOADFILE_FIELDNAME : uploadFieldname;
    String destinationFieldnameToUse = Utils.isEmpty( destinationFieldname ) ? TARGETFILE_FIELDNAME : destinationFieldname;

    if ( runForEveryRow ) {
      resultRows = previousResult.getRows();
      if ( resultRows == null ) {
        result.setNrErrors( 1 );
        logError( BaseMessages.getString( PKG, "JobHTTP.Error.UnableGetResultPrevious" ) );
        return result;
      }
    } else {
      resultRows = new ArrayList<>();
      RowMetaAndData row = new RowMetaAndData();
      row.addValue( new ValueMetaString( urlFieldnameToUse ), environmentSubstitute( url ) );
      row.addValue( new ValueMetaString( uploadFieldnameToUse ), environmentSubstitute( uploadFilename ) );
      row.addValue( new ValueMetaString( destinationFieldnameToUse ), environmentSubstitute( targetFilename ) );
      resultRows.add( row );
    }

    for ( int i = 0; i < resultRows.size() && result.getNrErrors() == 0; i++ ) {
      RowMetaAndData row = resultRows.get( i );

      OutputStream outputFile = null;
      InputStream input = null;

      try {
        String urlToUse = environmentSubstitute( row.getString( urlFieldnameToUse, "" ) );
        String realUploadFile = environmentSubstitute( row.getString( uploadFieldnameToUse, "" ) );
        String realTargetFile = environmentSubstitute( row.getString( destinationFieldnameToUse, "" ) );

        logBasic( BaseMessages.getString( PKG, "JobHTTP.Log.ConnectingURL", urlToUse ) );

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
        outputFile = KettleVFS.getInstance( parentJobMeta.getBowl() )
          .getOutputStream( realTargetFile, this, fileAppended );

        URI uri = (new URIBuilder( urlToUse )).build();

        if ( SCHEMA_FILE.equalsIgnoreCase( uri.getScheme() ) ) {
          input = getResultFromFileSchema( uri );
        } else {
          input = getResultFromHttpSchema( realUploadFile, uri );
        }

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
              ResultFile.FILE_TYPE_GENERAL, KettleVFS.getInstance( parentJobMeta.getBowl() )
                .getFileObject( realTargetFile, this ), parentJob
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
      }
    }

    return result;
  }

  private InputStream getResultFromFileSchema( URI uri ) throws IOException {
    URLConnection connection = uri.toURL().openConnection();
    connection.setDoOutput( true );
    Date date = new Date( connection.getLastModified() );
    logBasic( BaseMessages.getString( PKG, "JobHTTP.Log.ReplayInfo", connection.getContentType(), date ) );
    return connection.getInputStream();
  }

  private InputStream getResultFromHttpSchema( String realUploadFile, URI uri ) throws IOException, KettleException {
    HttpClient client = null;
    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    if ( !Utils.isEmpty( username ) ) {
      String realPassword = Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( password ) );
      String realUser = environmentSubstitute( username );
      UsernamePasswordCredentials credentials =
        new UsernamePasswordCredentials( realUser, realPassword != null ? realPassword : "" );
      CredentialsProvider provider = new BasicCredentialsProvider();
      provider.setCredentials( AuthScope.ANY, credentials );
      clientBuilder.setDefaultCredentialsProvider( provider );
    }

    String proxyHostnameValue = environmentSubstitute( proxyHostname );
    String proxyPortValue = environmentSubstitute( proxyPort );
    String nonProxyHostsValue = environmentSubstitute( nonProxyHosts );
    if ( !Utils.isEmpty( proxyHostnameValue ) ) {
      HttpHost proxy = new HttpHost( proxyHostnameValue, Integer.parseInt( proxyPortValue ) );
      clientBuilder.setProxy( proxy );
      if ( !Utils.isEmpty( nonProxyHostsValue ) ) {
        HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner( proxy ) {
          @Override
          public HttpRoute determineRoute(
                  final HttpHost host,
                  final HttpRequest request,
                  final HttpContext context ) throws HttpException {
            String hostName = host.getHostName();
            if ( hostName.matches( nonProxyHostsValue ) ) {
              // Return direct route
              return new HttpRoute( host );
            }
            return super.determineRoute( host, request, context );
          }
        };
        clientBuilder.setRoutePlanner( routePlanner );
      }
    }

    client = clientBuilder.build();

    HttpRequestBase httpRequestBase;
    // See if we need to send a file over?
    if ( !Utils.isEmpty( realUploadFile ) ) {
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobHTTP.Log.SendingFile", realUploadFile ) );
      }
      httpRequestBase = new HttpPost( uri );

      // Get content of file
      String content = new String( Files.readAllBytes( Paths.get( realUploadFile ) ) );

      // upload data to web server

      StringEntity requestEntity = new StringEntity( content );
      requestEntity.setContentType( "application/x-www-form-urlencoded" );
      ( (HttpPost) httpRequestBase ).setEntity( requestEntity );
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobHTTP.Log.FinishedSendingFile" ) );
      }
    } else {
      httpRequestBase = new HttpGet( uri );
    }


    // if we have HTTP headers, add them
    if ( !Utils.isEmpty( headerName ) ) {
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "JobHTTP.Log.HeadersProvided" ) );
      }
      for ( int j = 0; j < headerName.length; j++ ) {
        if ( !Utils.isEmpty( headerValue[ j ] ) ) {
          httpRequestBase
            .addHeader( environmentSubstitute( headerName[ j ] ), environmentSubstitute( headerValue[ j ] ) );
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString(
              PKG, "JobHTTP.Log.HeaderSet", environmentSubstitute( headerName[ j ] ),
              environmentSubstitute( headerValue[ j ] ) ) );
          }
        }
      }
    }

    // Get a stream for the specified URL
    HttpResponse response = null;
    if ( !Utils.isEmpty( proxyHostname ) ) {
      HttpHost target = new HttpHost( uri.getHost(), uri.getPort(), uri.getScheme() );
      // Create AuthCache instance
      AuthCache authCache = new BasicAuthCache();
      // Generate BASIC scheme object and add it to the local auth cache
      BasicScheme basicAuth = new BasicScheme();
      authCache.put( target, basicAuth );
      // Add AuthCache to the execution context
      HttpClientContext localContext = HttpClientContext.create();
      localContext.setAuthCache( authCache );
      response = client.execute( target, httpRequestBase, localContext );
    } else {
      response = client.execute( httpRequestBase );
    }
    responseStatusCode = response.getStatusLine().getStatusCode();

    if ( HttpStatus.SC_OK != responseStatusCode ) {
      throw new KettleException( "StatusCode: " + responseStatusCode );
    }

    if ( log.isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobHTTP.Log.StartReadingReply" ) );
    }

    logBasic( BaseMessages.getString( PKG, "JobHTTP.Log.ReplayInfo",
      response.getEntity().getContentType(),
      response.getLastHeader( HttpHeaders.DATE ).getValue() ) );

    // Read the result from the server...
    return response.getEntity().getContent();
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
   * @param getFieldname The Result URL Fieldname to set.
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
   * @param runForEveryRow The runForEveryRow to set.
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
   * @param fileAppended The fileAppended to set.
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
   * @param dateTimeAdded The dateTimeAdded to set.
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
   * @param uploadFilenameExtension The uploadFilenameExtension to set.
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
   * @param uploadFilenameExtension The uploadFilenameExtension to set.
   * @deprecated Use {@link JobEntryHTTP#setTargetFilenameExtension(String uploadFilenameExtension)} instead
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
    JobEntryValidatorUtils.andValidator().validate( jobMeta.getBowl(), this, "targetFilename", remarks,
      AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( jobMeta.getBowl(), this, "targetFilenameExtention", remarks,
      AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( jobMeta.getBowl(), this, "uploadFilename", remarks,
      AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( jobMeta.getBowl(), this, "proxyPort", remarks,
      AndValidator.putValidators( JobEntryValidatorUtils.integerValidator() ) );
  }
}
