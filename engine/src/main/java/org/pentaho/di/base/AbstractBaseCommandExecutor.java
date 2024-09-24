/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.di.version.BuildVersion;
import org.pentaho.metastore.api.IMetaStore;

public abstract class AbstractBaseCommandExecutor {

  private SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss.SSS" );

  public static final String YES = "Y";

  private LogChannelInterface log;
  private Class<?> pkgClazz;
  IMetaStore metaStore = MetaStoreConst.getDefaultMetastore();

  private Result result = new Result();

  protected Result exitWithStatus( final int exitStatus ) {
    getResult().setExitStatus( exitStatus );
    return getResult();
  }

  protected void logDebug( final String messageKey ) {
    if ( getLog().isDebug() ) {
      getLog().logDebug( BaseMessages.getString( getPkgClazz(), messageKey ) );
    }
  }

  protected void logDebug( final String messageKey, String... messageTokens ) {
    if ( getLog().isDebug() ) {
      getLog().logDebug( BaseMessages.getString( getPkgClazz(), messageKey, messageTokens ) );
    }
  }

  protected int calculateAndPrintElapsedTime( Date start, Date stop, String startStopMsgTkn, String processingEndAfterMsgTkn,
                                              String processingEndAfterLongMsgTkn, String processingEndAfterLongerMsgTkn,
                                              String processingEndAfterLongestMsgTkn ) {

    String begin = getDateFormat().format( start );
    String end = getDateFormat().format( stop );

    getLog().logMinimal( BaseMessages.getString( getPkgClazz(), startStopMsgTkn, begin, end ) );

    long millis = stop.getTime() - start.getTime();
    int seconds = (int) ( millis / 1000 );
    if ( seconds <= 60 ) {
      getLog().logMinimal( BaseMessages.getString( getPkgClazz(), processingEndAfterMsgTkn, String.valueOf( seconds ) ) );
    } else if ( seconds <= 60 * 60 ) {
      int min = ( seconds / 60 );
      int rem = ( seconds % 60 );
      getLog().logMinimal( BaseMessages.getString( getPkgClazz(), processingEndAfterLongMsgTkn, String.valueOf( min ),
                    String.valueOf( rem ), String.valueOf( seconds ) ) );
    } else if ( seconds <= 60 * 60 * 24 ) {
      int rem;
      int hour = ( seconds / ( 60 * 60 ) );
      rem = ( seconds % ( 60 * 60 ) );
      int min = rem / 60;
      rem = rem % 60;
      getLog().logMinimal( BaseMessages.getString(  getPkgClazz(), processingEndAfterLongerMsgTkn, String.valueOf( hour ),
                    String.valueOf( min ), String.valueOf( rem ), String.valueOf( seconds ) ) );
    } else {
      int rem;
      int days = ( seconds / ( 60 * 60 * 24 ) );
      rem = ( seconds % ( 60 * 60 * 24 ) );
      int hour = rem / ( 60 * 60 );
      rem = rem % ( 60 * 60 );
      int min = rem / 60;
      rem = rem % 60;
      getLog().logMinimal( BaseMessages.getString( getPkgClazz(), processingEndAfterLongestMsgTkn, String.valueOf( days ),
                    String.valueOf( hour ), String.valueOf( min ), String.valueOf( rem ), String.valueOf( seconds ) ) );
    }

    return seconds;
  }

  protected void printVersion( String kettleVersionMsgTkn ) {
    BuildVersion buildVersion = BuildVersion.getInstance();
    getLog().logBasic( BaseMessages.getString( getPkgClazz(), kettleVersionMsgTkn, buildVersion.getVersion(),
            buildVersion.getRevision(), buildVersion.getBuildDate() ) );
  }

  public RepositoryMeta loadRepositoryConnection( final String repoName, String loadingAvailableRepMsgTkn,
                                                     String noRepsDefinedMsgTkn, String findingRepMsgTkn ) throws KettleException {

    RepositoriesMeta repsinfo;

    if ( Utils.isEmpty( repoName ) || ( repsinfo = loadRepositoryInfo( loadingAvailableRepMsgTkn, noRepsDefinedMsgTkn ) ) == null ) {
      return null;
    }

    logDebug( findingRepMsgTkn, repoName );
    return repsinfo.findRepository( repoName );
  }

  public RepositoriesMeta loadRepositoryInfo( String loadingAvailableRepMsgTkn, String noRepsDefinedMsgTkn ) throws KettleException {

    RepositoriesMeta repsinfo = new RepositoriesMeta();
    repsinfo.getLog().setLogLevel( getLog().getLogLevel() );

    logDebug( loadingAvailableRepMsgTkn );

    try {
      repsinfo.readData();
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( getPkgClazz(), noRepsDefinedMsgTkn ), e );
    }

    return repsinfo;
  }

  public RepositoryDirectoryInterface loadRepositoryDirectory( Repository repository, String dirName, String noRepoProvidedMsgTkn,
                                                                  String allocateAndConnectRepoMsgTkn, String cannotFindDirMsgTkn ) throws KettleException {

    if ( repository == null ) {
      System.out.println( BaseMessages.getString( getPkgClazz(), noRepoProvidedMsgTkn ) );
      return null;
    }

    RepositoryDirectoryInterface directory;

    // Default is the root directory
    logDebug( allocateAndConnectRepoMsgTkn );
    directory = repository.loadRepositoryDirectoryTree();

    if ( !StringUtils.isEmpty( dirName ) ) {

      directory = directory.findDirectory( dirName ); // Find the directory name if one is specified...

      if ( directory == null ) {
        System.out.println( BaseMessages.getString( getPkgClazz(), cannotFindDirMsgTkn, "" + dirName ) );
      }
    }
    return directory;
  }

  public Repository establishRepositoryConnection( RepositoryMeta repositoryMeta, final String username, final String password,
                                                     final RepositoryOperation... operations ) throws KettleException {

    Repository rep = PluginRegistry.getInstance().loadClass( RepositoryPluginType.class, repositoryMeta, Repository.class );
    rep.init( repositoryMeta );
    rep.getLog().setLogLevel( getLog().getLogLevel() );
    rep.connect( username != null ? username : null, password != null ? password : null );

    if ( operations != null ) {
      // throws KettleSecurityException if username does does have permission for given operations
      rep.getSecurityProvider().validateAction( operations );
    }

    return rep;
  }

  public void printRepositoryDirectories( Repository repository, RepositoryDirectoryInterface directory ) throws KettleException {

    String[] directories = repository.getDirectoryNames( directory.getObjectId() );

    if ( directories != null ) {
      for ( String dir :  directories ) {
        System.out.println( dir );
      }
    }
  }

  protected void printParameter( String name, String value, String defaultValue, String description ) {
    if ( Utils.isEmpty( defaultValue ) ) {
      System.out.println( "Parameter: " + name + "=" + Const.NVL( value, "" ) + " : " + Const.NVL( description, "" ) );
    } else {
      System.out.println( "Parameter: " + name + "=" + Const.NVL( value, "" ) + ", default=" + defaultValue + " : " + Const.NVL( description, "" ) );
    }
  }

  protected String[] convert( Map<String, String> map ) {

    List<String> list = new ArrayList<>();

    if ( map != null ) {
      map.keySet().forEach( key -> list.add( key + "=" + map.get( key ) ) );
    }

    return list.toArray( new String[] {} );
  }

  public boolean isEnabled( final String value ) {
    return YES.equalsIgnoreCase( value ) || Boolean.parseBoolean( value ); // both are NPE safe, both are case-insensitive
  }

  /**
   * Decodes the provided base64String into a default path. Resulting zip file is UUID-named for concurrency sake.
   *
   * @param base64Zip BASE64 representation of a file
   * @param deleteOnJvmExit true if we want this newly generated file to be marked for deletion on JVM termination, false otherwise
   * @return File the newly created File
   */
  public File decodeBase64ToZipFile( Serializable base64Zip, boolean deleteOnJvmExit ) throws IOException {

    String basePath = !StringUtils.isEmpty( Const.getUserHomeDirectory() ) ? Const.getUserHomeDirectory() : new File( "." ).getAbsolutePath();
    String zipFilePath = basePath + File.separator + java.util.UUID.randomUUID().toString() + ".zip";
    File f = decodeBase64ToZipFile( base64Zip, zipFilePath );

    if ( f != null && deleteOnJvmExit ) {
      f.deleteOnExit();
    }

    return f;
  }

  /**
   * Decodes the provided base64String into the specified filePath. Parent directories must already exist.
   *
   * @param base64Zip BASE64 representation of a file
   * @param filePath String The path to which the base64String is to be decoded
   * @return File the newly created File
   */
  public File decodeBase64ToZipFile( Serializable base64Zip, String filePath ) throws IOException {

    if ( base64Zip == null || Utils.isEmpty( base64Zip.toString() ) ) {
      return null;
    }

    //Decode base64String to byte[]
    byte[] decodedBytes = Base64.getDecoder().decode( base64Zip.toString() );
    File file = new File( filePath );

    //Try-with-resources, write to file, ensure fos is always closed
    try ( FileOutputStream fos = new FileOutputStream( file ) ) {
      fos.write( decodedBytes );
    } catch ( IOException e ) {
      throw e;
    }

    return file;
  }

  public LogChannelInterface getLog() {
    return log;
  }

  public void setLog( LogChannelInterface log ) {
    this.log = log;
  }

  public Class<?> getPkgClazz() {
    return pkgClazz;
  }

  public void setPkgClazz( Class<?> pkgClazz ) {
    this.pkgClazz = pkgClazz;
  }

  public IMetaStore getMetaStore() {
    return metaStore;
  }

  public void setMetaStore( IMetaStore metaStore ) {
    this.metaStore = metaStore;
  }

  public SimpleDateFormat getDateFormat() {
    return dateFormat;
  }

  public void setDateFormat( SimpleDateFormat dateFormat ) {
    this.dateFormat = dateFormat;
  }

  public Result getResult() {
    return result;
  }

  public void setResult( Result result ) {
    this.result = result;
  }
}
