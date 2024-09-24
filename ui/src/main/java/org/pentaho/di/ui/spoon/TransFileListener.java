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

package org.pentaho.di.ui.spoon;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.jobexecutor.JobExecutorMeta;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.trans.steps.missing.MissingTransDialog;
import org.w3c.dom.Node;

import java.util.Locale;

public class TransFileListener implements FileListener, ConnectionListener {

  private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!

  public boolean open( Node transNode, String fname, boolean importfile ) throws KettleMissingPluginsException {
    return open( transNode, fname, null, importfile );
  }

  public boolean open( Node transNode, String fname, String connection, boolean importfile )
    throws KettleMissingPluginsException {
    final Spoon spoon = Spoon.getInstance();
    final PropsUI props = PropsUI.getInstance();
    try {
      // Call extension point(s) before the file has been opened
      ExtensionPointHandler.callExtensionPoint( spoon.getLog(), KettleExtensionPoint.TransBeforeOpen.id, fname );

      TransMeta transMeta = new TransMeta();
      transMeta.loadXML(
        transNode, fname, spoon.getMetaStore(), spoon.getRepository(), true, new Variables(),
        new OverwritePrompter() {

          public boolean overwritePrompt( String message, String rememberText, String rememberPropertyName ) {
            MessageDialogWithToggle.setDefaultImage( GUIResource.getInstance().getImageSpoon() );
            Object[] res =
              spoon.messageDialogWithToggle(
                BaseMessages.getString( PKG, "System.Button.Yes" ), null, message, Const.WARNING,
                new String[] {
                  BaseMessages.getString( PKG, "System.Button.Yes" ),
                  BaseMessages.getString( PKG, "System.Button.No" ) }, 1, rememberText, !props
                  .askAboutReplacingDatabaseConnections() );
            int idx = ( (Integer) res[ 0 ] ).intValue();
            boolean toggleState = ( (Boolean) res[ 1 ] ).booleanValue();
            props.setAskAboutReplacingDatabaseConnections( !toggleState );

            return ( ( idx & 0xFF ) == 0 ); // Yes means: overwrite
          }

        } );

      if ( transMeta.hasMissingPlugins() ) {
        StepMeta stepMeta = transMeta.getStep( 0 );
        MissingTransDialog missingDialog =
          new MissingTransDialog( spoon.getShell(), transMeta.getMissingTrans(), stepMeta.getStepMetaInterface(),
            transMeta, stepMeta.getName() );
        if ( missingDialog.open() == null ) {
          return true;
        }
      }
      transMeta.setRepositoryDirectory( spoon.getDefaultSaveLocation( transMeta ) );
      transMeta.setRepository( spoon.getRepository() );
      transMeta.setMetaStore( spoon.getMetaStore() );
      if ( connection != null ) {
        transMeta.setVariable( Spoon.CONNECTION, connection );
      }
      spoon.setTransMetaVariables( transMeta );
      spoon.getProperties().addLastFile( LastUsedFile.FILE_TYPE_TRANSFORMATION, fname, null, false, null );
      spoon.addMenuLast();

      // If we are importing into a repository we need to fix 
      // up the references to other jobs and transformations
      // if any exist.
      if ( importfile ) {
        if ( spoon.getRepository() != null ) {
          transMeta = fixLinks( transMeta );
        }
      } else {
        transMeta.clearChanged();
      }

      transMeta.setFilename( fname );
      spoon.addTransGraph( transMeta );
      spoon.sharedObjectsFileMap.put( transMeta.getSharedObjects().getFilename(), transMeta.getSharedObjects() );

      // Call extension point(s) now that the file has been opened
      ExtensionPointHandler.callExtensionPoint( spoon.getLog(), KettleExtensionPoint.TransAfterOpen.id, transMeta );

      SpoonPerspectiveManager.getInstance().activatePerspective( MainSpoonPerspective.class );
      spoon.refreshTree();
      return true;

    } catch ( KettleMissingPluginsException e ) {
      throw e;
    } catch ( KettleException e ) {
      new ErrorDialog(
        spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorOpening.Title" ), BaseMessages
        .getString( PKG, "Spoon.Dialog.ErrorOpening.Message" )
        + fname, e );
    }
    return false;
  }

  private TransMeta fixLinks( TransMeta transMeta ) {
    transMeta = processLinkedJobs( transMeta );
    transMeta = processLinkedTrans( transMeta );

    return transMeta;
  }

  protected TransMeta processLinkedJobs( TransMeta transMeta ) {
    for ( StepMeta stepMeta : transMeta.getSteps() ) {
      if ( stepMeta.getStepID().equalsIgnoreCase( "JobExecutor" ) ) {
        JobExecutorMeta jem = (JobExecutorMeta) stepMeta.getStepMetaInterface();
        ObjectLocationSpecificationMethod specMethod = jem.getSpecificationMethod();
        // If the reference is by filename, change it to Repository By Name. Otherwise it's fine so leave it alone
        if ( specMethod == ObjectLocationSpecificationMethod.FILENAME ) {
          jem.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
          String filename = jem.getFileName();
          String jobname = filename.substring( filename.lastIndexOf( "/" ) + 1, filename.lastIndexOf( '.' ) );
          String directory = filename.substring( 0, filename.lastIndexOf( "/" ) );
          jem.setJobName( jobname );
          jem.setDirectoryPath( directory );
        }
      }
    }
    return transMeta;
  }

  protected TransMeta processLinkedTrans( TransMeta transMeta ) {
    for ( StepMeta stepMeta : transMeta.getSteps() ) {
      if ( stepMeta.getStepID().equalsIgnoreCase( "TransExecutor" ) ) {
        TransExecutorMeta tem = (TransExecutorMeta) stepMeta.getStepMetaInterface();
        ObjectLocationSpecificationMethod specMethod = tem.getSpecificationMethod();
        // If the reference is by filename, change it to Repository By Name. Otherwise it's fine so leave it alone
        if ( specMethod == ObjectLocationSpecificationMethod.FILENAME ) {
          tem.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
          String filename = tem.getFileName();
          String jobname = filename.substring( filename.lastIndexOf( "/" ) + 1, filename.lastIndexOf( '.' ) );
          String directory = filename.substring( 0, filename.lastIndexOf( "/" ) );
          tem.setTransName( jobname );
          tem.setDirectoryPath( directory );
        }
      }
    }
    return transMeta;
  }

  public boolean save( EngineMetaInterface meta, String fname, boolean export ) {
    Spoon spoon = Spoon.getInstance();
    EngineMetaInterface lmeta;
    if ( export ) {
      lmeta = (TransMeta) ( (TransMeta) meta ).realClone( false );
    } else {
      lmeta = meta;
    }

    try {
      ExtensionPointHandler.callExtensionPoint( spoon.getLog(), KettleExtensionPoint.TransBeforeSave.id, lmeta );
    } catch ( KettleException e ) {
      // fails gracefully
    }

    boolean saveStatus = spoon.saveMeta( lmeta, fname );

    if ( saveStatus ) {
      try {
        ExtensionPointHandler.callExtensionPoint( spoon.getLog(), KettleExtensionPoint.TransAfterSave.id, lmeta );
      } catch ( KettleException e ) {
        // fails gracefully
      }
    }

    return saveStatus;
  }

  public void syncMetaName( EngineMetaInterface meta, String name ) {
    ( (TransMeta) meta ).setName( name );
  }

  public boolean accepts( String fileName ) {
    if ( fileName == null || fileName.indexOf( '.' ) == -1 ) {
      return false;
    }
    String extension = fileName.substring( fileName.lastIndexOf( '.' ) + 1 );
    return extension.equals( "ktr" );
  }

  public boolean acceptsXml( String nodeName ) {
    if ( "transformation".equals( nodeName ) ) {
      return true;
    }
    return false;
  }

  public String[] getFileTypeDisplayNames( Locale locale ) {
    return new String[] { "Transformations", "XML" };
  }

  public String getRootNodeName() {
    return "transformation";
  }

  public String[] getSupportedExtensions() {
    return new String[] { "ktr", "xml" };
  }

}
