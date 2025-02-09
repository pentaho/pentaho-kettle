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

package com.pentaho.repository.importexport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.pur.TransDelegate;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.missing.MissingTrans;
import org.pentaho.di.ui.trans.steps.missing.MissingTransDialog;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.ConverterException;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.w3c.dom.Document;

/**
 * Converts stream of binary or character data.
 * 
 * @author rmansoor
 */
public class StreamToTransNodeConverter implements Converter {
  private static final boolean WRITE_AS_STREAM = true;
  IUnifiedRepository unifiedRepository;

  private static final Log logger = LogFactory.getLog( StreamToTransNodeConverter.class );

  /** The package name, used for internationalization of messages. */
  private static Class<?> PKG = MissingTransDialog.class; // for i18n purposes, needed by Translator2!!

  public StreamToTransNodeConverter( IUnifiedRepository unifiedRepository ) {
    this.unifiedRepository = unifiedRepository;
  }

  public InputStream convert( final IRepositoryFileData data ) {
    throw new UnsupportedOperationException();
  }

  /**
   * 
   * @param fileId
   * @return
   */
  public InputStream convert( final Serializable fileId ) {
    try {
      // this will change in the future if PDI no longer has its
      // own repository. For now, get the reference
      if ( fileId != null ) {
        Repository repository = connectToRepository();
        RepositoryFile file = unifiedRepository.getFileById( fileId );
        if ( file != null ) {
          try {
            TransMeta transMeta = repository.loadTransformation( new StringObjectId( fileId.toString() ), null );
            if ( transMeta != null ) {
              return new ByteArrayInputStream( transMeta.getXML().getBytes() );
            }
          } catch ( KettleException e ) {
            logger.error( e );
            // file is there and may be legacy, attempt simple export
            SimpleRepositoryFileData fileData =
                unifiedRepository.getDataForRead( fileId, SimpleRepositoryFileData.class );
            if ( fileData != null ) {
              logger.warn( "Reading as legacy CE tranformation " + file.getName() + "." );
              return fileData.getInputStream();
            }
          }
        }
      }
    } catch ( Exception e ) {
      logger.error( e );
    }
    return null;
  }

  // package-local visibility for testing purposes
  Repository connectToRepository() throws KettleException {
    return PDIImportUtil.connectToRepository( null );
  }

  public IRepositoryFileData convert( final InputStream inputStream, final String charset, final String mimeType ) {
    if ( WRITE_AS_STREAM ) {
      return new SimpleRepositoryFileData( inputStream, charset, mimeType );
    }
    try {
      long size = inputStream.available();
      TransMeta transMeta = new TransMeta();
      Repository repository = connectToRepository();
      Document doc = PDIImportUtil.loadXMLFrom( inputStream );
      transMeta.loadXML( doc.getDocumentElement(), repository, false );

      if ( transMeta.hasMissingPlugins() ) {
        KettleMissingPluginsException
          missingPluginsException =
          new KettleMissingPluginsException( getErrorMessage( transMeta.getMissingTrans() ) );
        throw new ConverterException( missingPluginsException );
      }

      TransDelegate delegate = new TransDelegate( repository, this.unifiedRepository );
      saveSharedObjects( repository, transMeta );
      return new NodeRepositoryFileData( delegate.elementToDataNode( transMeta ), size );
    } catch ( IOException | KettleException e ) {
      logger.error( e );
      return null;
    }
  }

  private String getErrorMessage( List<MissingTrans> missingTrans ) {
    StringBuilder entries = new StringBuilder();
    for ( MissingTrans entry : missingTrans ) {
      entries.append( "- " + entry.getStepName() + " - " + entry.getMissingPluginId() + "\n" );
      if ( missingTrans.indexOf( entry ) == missingTrans.size() - 1 ) {
        entries.append( '\n' );
      }
    }
    return BaseMessages.getString( PKG, "MissingTransDialog.MissingTransSteps", entries.toString() );
  }

  private void saveSharedObjects( final Repository repo, final RepositoryElementInterface element )
    throws KettleException {
    TransMeta transMeta = (TransMeta) element;
    // First store the databases and other depending objects in the transformation.
    List<String> databaseNames = Arrays.asList( repo.getDatabaseNames( true ) );

    int dbIndex = 0;
    boolean updateMeta = Boolean.FALSE;

    List<Integer> transMetaDatabasesToUpdate = new ArrayList<Integer>();

    synchronized ( repo ) {
      for ( DatabaseMeta databaseMeta : transMeta.getDatabases() ) {
        if ( !databaseNames.contains( databaseMeta.getName() ) ) {
          if ( databaseMeta.getObjectId() == null || !StringUtils.isEmpty( databaseMeta.getHostname() ) ) {
            repo.save( databaseMeta, null, null );
          }
        } else if ( databaseMeta.getObjectId() == null ) {
          // add this to the list to update object Ids later
          transMetaDatabasesToUpdate.add( dbIndex );
          updateMeta = Boolean.TRUE;
        }

        dbIndex++;
      }

      if ( updateMeta ) {
        // make sure to update object ids in the transmeta db collection
        for ( Integer databaseMetaIndex : transMetaDatabasesToUpdate ) {
          transMeta.getDatabase( databaseMetaIndex ).setObjectId(
            repo.getDatabaseID( transMeta.getDatabase( databaseMetaIndex ).getName() ) );
        }
      }

      // Store the slave servers...
      //
      for ( SlaveServer slaveServer : transMeta.getSlaveServers() ) {
        if ( slaveServer.hasChanged() || slaveServer.getObjectId() == null ) {
          repo.save( slaveServer, null, null );
        }
      }

      // Store the cluster schemas
      //
      for ( ClusterSchema clusterSchema : transMeta.getClusterSchemas() ) {
        if ( clusterSchema.hasChanged() || clusterSchema.getObjectId() == null ) {
          repo.save( clusterSchema, null, null );
        }
      }

      // Save the partition schemas
      //
      for ( PartitionSchema partitionSchema : transMeta.getPartitionSchemas() ) {
        if ( partitionSchema.hasChanged() || partitionSchema.getObjectId() == null ) {
          repo.save( partitionSchema, null, null );
        }
      }
    }
  }

  public void convertPostRepoSave( RepositoryFile repositoryFile ) {
    if ( repositoryFile != null ) {
      Repository repo;
      TransMeta transMeta = null;
      try {
        repo = connectToRepository();
        try {
          if ( repo != null ) {
            transMeta =
              repo.loadTransformation( new StringObjectId( repositoryFile.getId().toString() ), null );
          }
        } catch ( Exception e ) {
          logger.error( KettleExtensionPoint.TransImportAfterSaveToRepo.id, e );
          // file is there and may be legacy, attempt simple export
          SimpleRepositoryFileData fileData =
            unifiedRepository.getDataForRead( repositoryFile.getId(), SimpleRepositoryFileData.class );
          if ( fileData != null ) {
            transMeta = new TransMeta( fileData .getInputStream(), repo, false, null, null );
          }
        }
        if ( transMeta != null ) {
          ExtensionPointHandler.callExtensionPoint( new LogChannel( this ),
            KettleExtensionPoint.TransImportAfterSaveToRepo.id, transMeta );
        }
      } catch ( Exception e ) {
        logger.error( KettleExtensionPoint.TransImportAfterSaveToRepo.id, e );
      }
    }
  }
}
