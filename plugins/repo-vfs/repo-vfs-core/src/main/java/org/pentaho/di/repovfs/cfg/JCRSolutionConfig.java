package org.pentaho.di.repovfs.cfg;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Read-only access to configuration values */
public class JCRSolutionConfig {

  private static Logger log = LoggerFactory.getLogger( JCRSolutionConfig.class );
  private static final String PROP_FILE_LOCATION = "/config.properties";

  private final Properties properties;
  private final ConfigReader cfg;

  public JCRSolutionConfig() {
    properties = new Properties();
    try ( InputStream is = JCRSolutionConfig.class.getResourceAsStream( PROP_FILE_LOCATION ) ) {
      properties.load( is );
    } catch ( IOException e ) {
      log.error( "Unable to read properties file {}", PROP_FILE_LOCATION, e);
    }
    cfg = new ConfigReader( properties::getProperty );
  }

  /** Server path to delete files or folders */
  public String getDeleteFileOrFolderUrl() {
    return cfg.getMandatoryProperty( "URL.Path.DeleteFileOrFolder" );
  }

  /** Server path to get full repository tree */
  public String getRepositorySvc() {
    return cfg.getMandatoryProperty( "URL.Path.GetRepositorySvc" );
  }

  /** Server path to get only the root of the repository tree */
  public String getRepositoryPartialRootSvc() {
    return cfg.getMandatoryProperty( "URL.Path.GetRepositoryPartialRootSvc" );
  }

  /** Server path to get only one level of the repository tree */
  public String getRepositoryPartialSvc( String path ) {
    return MessageFormat.format( cfg.getMandatoryProperty( "URL.Path.GetRepositoryPartialSvc" ), path );
  }

  /** Service to download file contents */
  public String getDownloadSvc( String path ) {
    return MessageFormat.format( cfg.getMandatoryProperty( "URL.Path.DownloadSvc" ), path );
  }

  /** Service to upload file contents */
  public String getUploadSvc( String path ) {
    return MessageFormat.format( cfg.getMandatoryProperty( "URL.Path.UploadSvc" ), path );
  }

  /** Service to create folder */
  public String getCreateFolderSvc( String path ) {
    return MessageFormat.format( cfg.getMandatoryProperty( "URL.Path.CreateFolderSvc" ), path );
  }

  /** Service to move file or folder to inside the specified folder */
  public String getMoveToSvc( String destPath ) {
    return MessageFormat.format( cfg.getMandatoryProperty( "URL.Path.MoveToSvc" ), destPath );
  }

  /** Service to rename file or folder in place */
  public String getRenameSvc( String path, String newName ) {
    return MessageFormat.format( cfg.getMandatoryProperty( "URL.Path.RenameSvc" ), path, newName );
  }

  /** Timeout, in ms, to wait for server requests */
  public int getTimeOut() {
    return cfg.parseProperty( "Request.TimeOut.ms", Integer::parseInt ).orElse( 0 );
  }

  /** Fetch repository file listing level by level instead of all at once */
  public boolean isPartialLoading() {
    return cfg.parseProperty( "PartialLoading", Boolean::parseBoolean ).orElse( true );
  }

}
