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

package org.pentaho.di.trans.steps.metainject;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

@ExtensionPoint(
    id = "RepositoryExporterPatchTransStep",
    description = "Patch the step in a transformation during repository export",
    extensionPointId = "RepositoryExporterPatchTransStep" )
public class RepositoryExporterExtension implements ExtensionPointInterface {

  @Override
  public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {

    Object[] metaInjectObjectArray = (Object[]) object;
    TransMeta transMeta = (TransMeta) metaInjectObjectArray[0];
    Class<?> PKG = (Class<?>) metaInjectObjectArray[1];
    KettleFileRepository fileRep = (KettleFileRepository) metaInjectObjectArray[2];
    StepMeta stepMeta = (StepMeta) metaInjectObjectArray[3];

    if ( stepMeta.isEtlMetaInject() ) {
      MetaInjectMeta metaInjectMeta = (MetaInjectMeta) stepMeta.getStepMetaInterface();
      // convert to a named based reference.
      //
      if ( metaInjectMeta.getSpecificationMethod() == ObjectLocationSpecificationMethod.FILENAME ) {
        try {
          TransMeta meta =
              MetaInjectMeta.loadTransformationMeta( metaInjectMeta, fileRep, fileRep.metaStore, transMeta );
          FileObject fileObject = KettleVFS.getFileObject( meta.getFilename() );
          metaInjectMeta.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
          metaInjectMeta.setFileName( null );
          metaInjectMeta.setTransName( meta.getName() );
          metaInjectMeta.setDirectoryPath( Const.NVL( calcRepositoryDirectory( fileRep, fileObject ), "/" ) );
        } catch ( Exception e ) {
          log.logError( BaseMessages.getString( PKG, "Repository.Exporter.Log.UnableToLoadTransInMDI",
              metaInjectMeta.getName() ), e );
        }
      }
    }
  }

  private String calcRepositoryDirectory( KettleFileRepository fileRep, FileObject fileObject ) throws FileSystemException {
    String path = fileObject.getParent().getName().getPath();
    String baseDirectory = fileRep.getRepositoryMeta().getBaseDirectory();
    // Double check!
    //
    if ( path.startsWith( baseDirectory ) ) {
      return path.substring( baseDirectory.length() );
    } else {
      return path;
    }
  }
}
