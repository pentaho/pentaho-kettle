/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.job.entries.missing.MissingEntryDialog;
import org.w3c.dom.Node;

import java.util.Date;
import java.util.Locale;

public class JobFileListener implements FileListener, ConnectionListener {

  private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!

  public boolean open( Node jobNode, String fname, boolean importfile ) {
    return open( jobNode, fname, null, importfile );
  }

  public boolean open( Node jobNode, String fname, String connection, boolean importfile ) {
    Spoon spoon = Spoon.getInstance();
    try {
      // Call extension point(s) before the file has been opened
      ExtensionPointHandler.callExtensionPoint( spoon.getLog(), KettleExtensionPoint.JobBeforeOpen.id, fname );

      JobMeta jobMeta = new JobMeta();
      jobMeta.loadXML( jobNode, fname, spoon.getRepository(), spoon.getMetaStore(), false, spoon );
      if ( jobMeta.hasMissingPlugins() ) {
        MissingEntryDialog missingDialog = new MissingEntryDialog( spoon.getShell(), jobMeta.getMissingEntries() );
        if ( missingDialog.open() == null ) {
          return true;
        }
      }
      //same fix as in PDI-18786 where the clearCurrentDirectoryChangedListeners method was added
      clearCurrentDirectoryChangedListenersWhenImporting( importfile, jobMeta );
      jobMeta.setRepositoryDirectory( spoon.getDefaultSaveLocation( jobMeta ) );
      jobMeta.setRepository( spoon.getRepository() );
      jobMeta.setMetaStore( spoon.getMetaStore() );
      if ( connection != null ) {
        jobMeta.setVariable( Spoon.CONNECTION, connection );
      }
      spoon.setJobMetaVariables( jobMeta );
      spoon.getProperties()
        .addLastFile( LastUsedFile.FILE_TYPE_JOB, fname, null, false, null, null, new Date(), connection );
      spoon.addMenuLast();

      // If we are importing into a repository we need to fix 
      // up the references to other jobs and transformations
      // if any exist.
      if ( importfile ) {
        if ( spoon.getRepository() != null ) {
          jobMeta = fixLinks( jobMeta );
        }
      } else {
        jobMeta.clearChanged();
      }

      jobMeta.setFilename( fname );
      spoon.delegates.jobs.addJobGraph( jobMeta );

      // Call extension point(s) now that the file has been opened
      ExtensionPointHandler.callExtensionPoint( spoon.getLog(), KettleExtensionPoint.JobAfterOpen.id, jobMeta );

      spoon.refreshTree();
      SpoonPerspectiveManager.getInstance().activatePerspective( MainSpoonPerspective.class );
      return true;
    } catch ( KettleException e ) {
      new ErrorDialog(
        spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.ErrorOpening.Title" ), BaseMessages
        .getString( PKG, "Spoon.Dialog.ErrorOpening.Message" )
        + fname, e );
    }
    return false;
  }

  @VisibleForTesting
  void clearCurrentDirectoryChangedListenersWhenImporting( boolean importfile, JobMeta jobMeta ) {
    if ( importfile ) {
      jobMeta.clearCurrentDirectoryChangedListeners();
    }
  }

  private JobMeta fixLinks( JobMeta jobMeta ) {
    jobMeta = processLinkedJobs( jobMeta );
    jobMeta = processLinkedTrans( jobMeta );
    return jobMeta;
  }

  protected JobMeta processLinkedJobs( JobMeta jobMeta ) {
    for ( int i = 0; i < jobMeta.nrJobEntries(); i++ ) {
      JobEntryCopy jec = jobMeta.getJobEntry( i );
      if ( jec.getEntry() instanceof JobEntryJob ) {
        JobEntryJob jej = (JobEntryJob) jec.getEntry();
        ObjectLocationSpecificationMethod specMethod = jej.getSpecificationMethod();
        // If the reference is by filename, change it to Repository By Name. Otherwise it's fine so leave it alone
        if ( specMethod == ObjectLocationSpecificationMethod.FILENAME ) {
          jej.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
          String filename = jej.getFilename();
          // if the filename is a missing variable name, it will not contain a slash - we don't want to fail
          // ungracefully in this case, simply set the job name to the whole filename value and let the run-time
          // handle any exceptions that arise from this
          if ( filename != null ) {
            if ( filename.indexOf( "/" ) > -1 ) {
              String jobname = filename.substring( filename.lastIndexOf( "/" ) + 1, filename.lastIndexOf( '.' ) );
              String directory = filename.substring( 0, filename.lastIndexOf( "/" ) );
              jej.setJobName( jobname );
              jej.setDirectory( directory );
            } else {
              jej.setJobName( filename );
            }
          }
          jobMeta.setJobEntry( i, jec );
        }
      }
    }
    return jobMeta;
  }

  protected JobMeta processLinkedTrans( JobMeta jobMeta ) {
    for ( int i = 0; i < jobMeta.nrJobEntries(); i++ ) {
      JobEntryCopy jec = jobMeta.getJobEntry( i );
      if ( jec.getEntry() instanceof JobEntryTrans ) {
        JobEntryTrans jet = (JobEntryTrans) jec.getEntry();
        ObjectLocationSpecificationMethod specMethod = jet.getSpecificationMethod();
        // If the reference is by filename, change it to Repository By Name. Otherwise it's fine so leave it alone
        if ( specMethod == ObjectLocationSpecificationMethod.FILENAME ) {
          jet.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
          String filename = jet.getFilename();
          // if the filename is a missing variable name, it will not contain a slash - we don't want to fail
          // ungracefully in this case, simply set the trans name to the whole filename value and let the run-time
          // handle any exceptions that arise from this
          if ( filename != null ) {
            if ( filename.indexOf( "/" ) > -1 ) {
              String jobname = filename.substring( filename.lastIndexOf( "/" ) + 1, filename.lastIndexOf( '.' ) );
              String directory = filename.substring( 0, filename.lastIndexOf( "/" ) );
              jet.setTransname( jobname );
              jet.setDirectory( directory );
            } else {
              jet.setTransname( filename );
            }
          }
          jobMeta.setJobEntry( i, jec );
        }
      }
    }
    return jobMeta;
  }

  public boolean save( EngineMetaInterface meta, String fname, boolean export ) {
    Spoon spoon = Spoon.getInstance();

    EngineMetaInterface lmeta;
    if ( export ) {
      lmeta = (JobMeta) ( (JobMeta) meta ).realClone( false );
    } else {
      lmeta = meta;
    }

    try {
      ExtensionPointHandler.callExtensionPoint( spoon.getLog(), KettleExtensionPoint.JobBeforeSave.id, lmeta );
    } catch ( KettleException e ) {
      // fails gracefully
    }

    boolean saveStatus = spoon.saveMeta( lmeta, fname );

    if ( saveStatus ) {
      try {
        ExtensionPointHandler.callExtensionPoint( spoon.getLog(), KettleExtensionPoint.JobAfterSave.id, lmeta );
      } catch ( KettleException e ) {
        // fails gracefully
      }
    }

    return saveStatus;
  }

  public void syncMetaName( EngineMetaInterface meta, String name ) {
    ( (JobMeta) meta ).setName( name );
  }

  public boolean accepts( String fileName ) {
    if ( fileName == null || fileName.indexOf( '.' ) == -1 ) {
      return false;
    }
    String extension = fileName.substring( fileName.lastIndexOf( '.' ) + 1 );
    return extension.equals( "kjb" );
  }

  public boolean acceptsXml( String nodeName ) {
    return "job".equals( nodeName );
  }

  public String[] getFileTypeDisplayNames( Locale locale ) {
    return new String[] { "Jobs", "XML" };
  }

  public String[] getSupportedExtensions() {
    return new String[] { "kjb", "xml" };
  }

  public String getRootNodeName() {
    return "job";
  }

}
