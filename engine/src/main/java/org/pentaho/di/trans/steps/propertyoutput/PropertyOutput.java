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

package org.pentaho.di.trans.steps.propertyoutput;

import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Output rows to Properties file and create a file.
 *
 * @author Samatar
 * @since 13-Apr-2008
 */

public class PropertyOutput extends BaseStep implements StepInterface {
  private static Class<?> PKG = PropertyOutputMeta.class; // for i18n purposes, needed by Translator2!!

  private PropertyOutputMeta meta;
  private PropertyOutputData data;

  public PropertyOutput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (PropertyOutputMeta) smi;
    data = (PropertyOutputData) sdi;

    Object[] r = getRow(); // this also waits for a previous step to be finished.

    if ( r == null ) { // no more input to be expected...
      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;
      data.inputRowMeta = getInputRowMeta();
      data.outputRowMeta = data.inputRowMeta.clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // Let's take the index of Key field ...
      data.indexOfKeyField = data.inputRowMeta.indexOfValue( meta.getKeyField() );
      if ( data.indexOfKeyField < 0 ) {
        // The field is unreachable !
        logError( BaseMessages.getString( PKG, "PropertyOutput.Log.ErrorFindingField", meta.getKeyField() ) );
        throw new KettleException( BaseMessages.getString( PKG, "PropertyOutput.Log.ErrorFindingField", meta.getKeyField() ) );
      }

      // Let's take the index of Key field ...
      data.indexOfValueField = data.inputRowMeta.indexOfValue( meta.getValueField() );
      if ( data.indexOfValueField < 0 ) {
        // The field is unreachable !
        logError( BaseMessages.getString( PKG, "PropertyOutput.Log.ErrorFindingField", meta.getValueField() ) );
        throw new KettleException( BaseMessages.getString( PKG, "PropertyOutput.Log.ErrorFindingField", meta.getValueField() ) );
      }

      if ( meta.isFileNameInField() ) {
        String realFieldName = environmentSubstitute( meta.getFileNameField() );
        if ( Utils.isEmpty( realFieldName ) ) {
          logError( BaseMessages.getString( PKG, "PropertyOutput.Log.FilenameInFieldEmpty" ) );
          throw new KettleException( BaseMessages.getString( PKG, "PropertyOutput.Log.FilenameInFieldEmpty" ) );
        }
        data.indexOfFieldfilename = data.inputRowMeta.indexOfValue( realFieldName );
        if ( data.indexOfFieldfilename < 0 ) {
          // The field is unreachable !
          logError( BaseMessages.getString( PKG, "PropertyOutput.Log.ErrorFindingField", meta.getValueField() ) );
          throw new KettleException( BaseMessages.getString( PKG, "PropertyOutput.Log.ErrorFindingField", meta
            .getValueField() ) );
        }
      } else {
        // Let's check for filename...
        data.filename = buildFilename();
        // Check if filename is empty..
        if ( Utils.isEmpty( data.filename ) ) {
          logError( BaseMessages.getString( PKG, "PropertyOutput.Log.FilenameEmpty" ) );
          throw new KettleException( BaseMessages.getString( PKG, "PropertyOutput.Log.FilenameEmpty" ) );
        }
        openNewFile();
      }
    } // end first

    // Get value field
    String propkey = data.inputRowMeta.getString( r, data.indexOfKeyField );
    String propvalue = data.inputRowMeta.getString( r, data.indexOfValueField );

    try {
      if ( meta.isFileNameInField() ) {
        data.filename = data.inputRowMeta.getString( r, data.indexOfFieldfilename );
        if ( Utils.isEmpty( data.filename ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "PropertyOutputMeta.Log.FileNameEmty" ) );
        }
        if ( !checkSameFile() ) {
          // close previous file
          closeFile();
          // Open new file
          openNewFile();
        }
      }

      if ( !data.KeySet.contains( propkey ) ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "PropertyOutput.Log.Key", propkey ) );
          logDetailed( BaseMessages.getString( PKG, "PropertyOutput.Log.Value", propvalue ) );
        }
        // Update property
        data.pro.setProperty( propkey, propvalue );
        putRow( data.outputRowMeta, r ); // in case we want it to go further...
        incrementLinesOutput();

        if ( checkFeedback( getLinesRead() ) ) {
          if ( log.isBasic() ) {
            logBasic( "linenr " + getLinesRead() );
          }
        }
        data.KeySet.add( propkey );
      }
    } catch ( KettleStepException e ) {
      boolean sendToErrorRow = false;
      String errorMessage = null;
      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "PropertyOutputMeta.Log.ErrorInStep" ) + e.getMessage() );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        putError( data.outputRowMeta, r, 1L, errorMessage, null, "PROPSOUTPUTO001" );
      }
    }
    return true;
  }

  public boolean checkSameFile() throws KettleException {
    return data.previousFileName.equals( data.filename );
  }

  private void openNewFile() throws KettleException {
    try ( FileObject newFile = KettleVFS.getFileObject( data.filename, getTransMeta() ) ) {
      data.pro = new Properties();
      data.KeySet.clear();

      data.file = newFile;
      if ( meta.isAppend() && data.file.exists() ) {
        data.pro.load( KettleVFS.getInputStream( data.file ) );
      }
      // Create parent folder if needed...
      createParentFolder();
      //save processing file
      data.previousFileName = data.filename;
    } catch ( Exception e ) {
      throw new KettleException( "Error opening file [" + data.filename + "]!", e );
    }
  }

  private void createParentFolder() throws KettleException {
    if ( meta.isCreateParentFolder() ) {
      FileObject parentfolder = null;
      try {
        // Do we need to create parent folder ?

        // Check for parent folder
        // Get parent folder
        parentfolder = data.file.getParent();
        if ( !parentfolder.exists() ) {
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "PropertyOutput.Log.ParentFolderExists", parentfolder.getName().toString() ) );
          }
          parentfolder.createFolder();
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "PropertyOutput.Log.CanNotCreateParentFolder", parentfolder.getName().toString() ) );
          }
        }
      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "PropertyOutput.Log.CanNotCreateParentFolder", parentfolder.getName().toString() ) );
        throw new KettleException( BaseMessages.getString( PKG, "PropertyOutput.Log.CanNotCreateParentFolder", parentfolder.getName().toString() ) );
      } finally {
        if ( parentfolder != null ) {
          try {
            parentfolder.close();
          } catch ( Exception ex ) { /* Ignore */
          }
        }
      }
    }
  }

  private boolean closeFile() {
    if ( data.file == null ) {
      return true;
    }
    boolean retval = false;
    try ( OutputStream propsFile = KettleVFS.getOutputStream( data.file, false ) ) {
      data.pro.store( propsFile, environmentSubstitute( meta.getComment() ) );

      if ( meta.isAddToResult() ) {
        // Add this to the result file names...
        ResultFile resultFile = new ResultFile( ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname() );
        resultFile.setComment( BaseMessages.getString( PKG, "PropertyOutput.Log.FileAddedResult" ) );
        addResultFile( resultFile );
      }
      data.KeySet.clear();
      retval = true;
    } catch ( Exception e ) {
      logError( "Exception trying to close file [" + data.file.getName() + "]! :" + e.toString() );
      setErrors( 1 );
    } finally {
      if ( data.file != null ) {
        try {
          data.file.close();
          data.file = null;
        } catch ( Exception e ) { /* Ignore */
          logDetailed( "Exception trying to close file [" + data.file.getName() + "]! :", e );
        }
      }
      if ( data.pro != null ) {
        data.pro = null;
      }
    }
    return retval;
  }

  public String buildFilename() {
    return meta.buildFilename( this, getCopy() );
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (PropertyOutputMeta) smi;
    data = (PropertyOutputData) sdi;

    if ( super.init( smi, sdi ) ) {
      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (PropertyOutputMeta) smi;
    data = (PropertyOutputData) sdi;
    closeFile();

    setOutputDone();
    super.dispose( smi, sdi );
  }

}
