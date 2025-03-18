/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package com.pentaho.repository.importexport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.missing.MissingEntry;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.pur.JobDelegate;
import org.pentaho.di.ui.job.entries.missing.MissingEntryDialog;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.ConverterException;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.w3c.dom.Document;

/**
 * Converts stream of binary or character data.
 * 
 * @author mlowery
 */
public class StreamToJobNodeConverter implements Converter {

  IUnifiedRepository unifiedRepository;
  private static final Log logger = LogFactory.getLog( StreamToJobNodeConverter.class );

  /** The package name, used for internationalization of messages. */
  private static Class<?> PKG = MissingEntryDialog.class; // for i18n purposes, needed by Translator2!!

  /**
   * 
   * @param unifiedRepository
   */
  public StreamToJobNodeConverter( IUnifiedRepository unifiedRepository ) {
    this.unifiedRepository = unifiedRepository;
  }

  /**
   * 
   * @param data
   * @return
   */
  public InputStream convert( final IRepositoryFileData data ) {
    throw new UnsupportedOperationException();
  }

  /**
   * 
   * @param fileId
   * @return
   */
  public InputStream convert( final Serializable fileId ) {
    if ( fileId == null ) {
      return null;
    }
    try {
      return loadJobAsStream( fileId );
    } catch ( Exception e ) {
      logger.error( e );
      return null;
    }
  }

  private InputStream loadJobAsStream( Serializable fileId ) throws KettleException {
    Repository repository = connectToRepository();
    RepositoryFile file = unifiedRepository.getFileById( fileId );
    if ( file == null ) {
      return null;
    }
    try {
      return getJobStream( repository, fileId );
    } catch ( KettleException e ) {
      logger.error( e );
      // file is there and may be legacy, attempt simple export
      return getLegacyJobStream( fileId, file );
    }
  }

  private InputStream getJobStream( Repository repository, Serializable fileId ) throws KettleException {
    JobMeta jobMeta = repository.loadJob( new StringObjectId( fileId.toString() ), null );
    if ( jobMeta == null ) {
      return null;
    }
    String xml = XMLHandler.getXMLHeader() + filterPrivateDatabases( jobMeta ).getXML();
    return new ByteArrayInputStream( xml.getBytes( StandardCharsets.UTF_8 ) );
  }

  private InputStream getLegacyJobStream( Serializable fileId, RepositoryFile file ) {
    SimpleRepositoryFileData fileData = unifiedRepository.getDataForRead( fileId, SimpleRepositoryFileData.class );
    if ( fileData == null ) {
      return null;
    }
    logger.warn( "Reading as legacy CE job " + file.getName() + "." );
    return fileData.getInputStream();
  }

  @VisibleForTesting
  JobMeta filterPrivateDatabases( JobMeta jobMeta ) {
    Set<String> privateDatabases = jobMeta.getPrivateDatabases();
    if ( privateDatabases != null ) {
      // keep only private transformation databases
      for ( Iterator<DatabaseMeta> it = jobMeta.getDatabases().iterator(); it.hasNext(); ) {
        DatabaseMeta databaseMeta = it.next();
        String databaseName = databaseMeta.getName();
        if ( !privateDatabases.contains( databaseName ) && !jobMeta.isDatabaseConnectionUsed( databaseMeta ) ) {
          it.remove();
        }
      }
    }
    return jobMeta;
  }

  // package-local visibility for testing purposes
  Repository connectToRepository() throws KettleException {
    return PDIImportUtil.connectToRepository( null );
  }

  /**
   * 
   * @param inputStream
   * @param charset
   * @param mimeType
   * @return
   */
  public IRepositoryFileData convert( final InputStream inputStream, final String charset, final String mimeType ) {
    try {
      BoundedInputStream bis = BoundedInputStream.builder().setInputStream( inputStream ).get();

      JobMeta jobMeta = new JobMeta();
      Repository repository = connectToRepository();
      Document doc = PDIImportUtil.loadXMLFrom( bis );
      if ( doc != null ) {
        jobMeta.loadXML( doc.getDocumentElement(), repository, null );
        if ( jobMeta.hasMissingPlugins() ) {
          KettleMissingPluginsException
            missingPluginsException =
            new KettleMissingPluginsException( getErrorMessage( jobMeta.getMissingEntries() ) );
          throw new ConverterException( missingPluginsException );
        }
        JobDelegate delegate = new JobDelegate( repository, this.unifiedRepository );
        delegate.saveSharedObjects( jobMeta, null );
        return new NodeRepositoryFileData( delegate.elementToDataNode( jobMeta ), bis.getCount() );
      } else {
        return null;
      }
    } catch ( IOException | KettleException e ) {
      return null;
    }
  }

  private String getErrorMessage( List<MissingEntry> missingEntries ) {
    StringBuilder entries = new StringBuilder();
    for ( MissingEntry entry : missingEntries ) {
      entries.append( "- " + entry.getName() + " - " + entry.getMissingPluginId() + "\n" );
      if ( missingEntries.indexOf( entry ) == missingEntries.size() - 1 ) {
        entries.append( '\n' );
      }
    }
    return BaseMessages.getString( PKG, "MissingEntryDialog.MissingJobEntries", entries.toString() );
  }

  public void saveSharedObjects( final Repository repo, final RepositoryElementInterface element )
    throws KettleException {
    JobMeta jobMeta = (JobMeta) element;
    // First store the databases and other depending objects in the transformation.
    List<String> databaseNames = Arrays.asList( repo.getDatabaseNames( true ) );

    int dbIndex = 0;
    int indexToReplace = 0;
    boolean updateMeta = Boolean.FALSE;

    for ( DatabaseMeta databaseMeta : jobMeta.getDatabases() ) {
      if ( !databaseNames.contains( databaseMeta.getName() ) ) {
        if ( databaseMeta.getObjectId() == null || !StringUtils.isEmpty( databaseMeta.getHostname() ) ) {
          repo.save( databaseMeta, null, null );
        }
      } else if ( databaseMeta.getObjectId() == null ) {
        indexToReplace = dbIndex;
        updateMeta = Boolean.TRUE;
      }

      dbIndex++;
    }

    // if db already exists in repo, get that object id and put it
    // in the transMeta db collection
    if ( updateMeta ) {
      DatabaseMeta dbMetaToReplace = jobMeta.getDatabase( indexToReplace );
      dbMetaToReplace.setObjectId( repo.getDatabaseID( dbMetaToReplace.getName() ) );
      jobMeta.removeDatabase( indexToReplace );
      jobMeta.addDatabase( dbMetaToReplace );
    }
    // Store the slave servers...
    //
    for ( SlaveServer slaveServer : jobMeta.getSlaveServers() ) {
      if ( slaveServer.getObjectId() == null ) {
        repo.save( slaveServer, null, null );
      }
    }

  }
}
