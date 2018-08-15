/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
