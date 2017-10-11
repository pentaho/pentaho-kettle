/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.ldifinput;

import java.util.Date;
import java.util.Enumeration;

import netscape.ldap.LDAPAttribute;
import netscape.ldap.util.LDIF;
import netscape.ldap.util.LDIFAttributeContent;
import netscape.ldap.util.LDIFContent;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
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
 * Read all LDIF files, convert them to rows and writes these to one or more output streams.
 *
 * @author Samatar
 * @since 24-05-2007
 */
public class LDIFInput extends BaseStep implements StepInterface {
  private static Class<?> PKG = LDIFInputMeta.class; // for i18n purposes, needed by Translator2!!

  private LDIFInputMeta meta;
  private LDIFInputData data;

  public LDIFInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private Object[] getOneRow() throws KettleException {

    try {
      if ( meta.isFileField() ) {
        while ( ( data.readrow == null || ( ( data.recordLDIF = data.InputLDIF.nextRecord() ) == null ) ) ) {
          if ( !openNextFile() ) {
            return null;
          }
        }
      } else {
        while ( ( data.file == null ) || ( ( data.recordLDIF = data.InputLDIF.nextRecord() ) == null ) ) {
          if ( !openNextFile() ) {
            return null;
          }
        }
      }

    } catch ( Exception IO ) {
      return null;
    }

    // Get LDIF Content
    LDIFContent contentLDIF = data.recordLDIF.getContent();
    String contentTYPE = "ATTRIBUTE_CONTENT";

    switch ( contentLDIF.getType() ) {
      case LDIFContent.DELETE_CONTENT:
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "LDIFInput.Log.ContentType", "DELETE_CONTENT" ) );
        }
        contentTYPE = "DELETE_CONTENT";
        break;
      case LDIFContent.ADD_CONTENT:
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "LDIFInput.Log.ContentType", "ADD_CONTENT" ) );
        }
        contentTYPE = "ADD_CONTENT";
        break;
      case LDIFContent.MODDN_CONTENT:
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "LDIFInput.Log.ContentType", "MODDN_CONTENT" ) );
        }
        contentTYPE = "MODDN_CONTENT";
        break;
      case LDIFContent.MODIFICATION_CONTENT:
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "LDIFInput.Log.ContentType", "MODIFICATION_CONTENT" ) );
        }
        contentTYPE = "MODIFICATION_CONTENT";
        break;
      default:
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "LDIFInput.Log.ContentType", "ATTRIBUTE_CONTENT" ) );
        }
        break;
    }

    // Get only ATTRIBUTE_CONTENT
    LDIFAttributeContent attrContentLDIF = (LDIFAttributeContent) contentLDIF;
    data.attributes_LDIF = attrContentLDIF.getAttributes();

    // Build an empty row based on the meta-data
    Object[] outputRowData = buildEmptyRow();

    // Create new row or clone
    if ( meta.isFileField() ) {
      System.arraycopy( data.readrow, 0, outputRowData, 0, data.readrow.length );
    }

    try {
      // Execute for each Input field...
      for ( int i = 0; i < meta.getInputFields().length; i++ ) {
        LDIFInputField ldifInputField = meta.getInputFields()[i];
        // Get the Attribut to look for
        String AttributValue = environmentSubstitute( ldifInputField.getAttribut() );

        String Value = GetValue( data.attributes_LDIF, AttributValue );

        // Do trimming
        switch ( ldifInputField.getTrimType() ) {
          case LDIFInputField.TYPE_TRIM_LEFT:
            Value = Const.ltrim( Value );
            break;
          case LDIFInputField.TYPE_TRIM_RIGHT:
            Value = Const.rtrim( Value );
            break;
          case LDIFInputField.TYPE_TRIM_BOTH:
            Value = Const.trim( Value );
            break;
          default:
            break;
        }

        // Do conversions
        //
        ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta( data.totalpreviousfields + i );
        ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta( data.totalpreviousfields + i );
        outputRowData[data.totalpreviousfields + i] = targetValueMeta.convertData( sourceValueMeta, Value );

        // Do we need to repeat this field if it is null?
        if ( meta.getInputFields()[i].isRepeated() ) {
          if ( data.previousRow != null && Utils.isEmpty( Value ) ) {
            outputRowData[data.totalpreviousfields + i] = data.previousRow[data.totalpreviousfields + i];
          }
        }
      } // End of loop over fields...
      int rowIndex = data.totalpreviousfields + meta.getInputFields().length;

      // See if we need to add the filename to the row...
      if ( meta.includeFilename() && !Utils.isEmpty( meta.getFilenameField() ) ) {
        outputRowData[rowIndex++] = data.filename;
      }
      // See if we need to add the row number to the row...
      if ( meta.includeRowNumber() && !Utils.isEmpty( meta.getRowNumberField() ) ) {
        outputRowData[data.totalpreviousfields + rowIndex++] = new Long( data.rownr );
      }

      // See if we need to add the content type to the row...
      if ( meta.includeContentType() && !Utils.isEmpty( meta.getContentTypeField() ) ) {
        outputRowData[data.totalpreviousfields + rowIndex++] = contentTYPE;
      }

      // See if we need to add the DN to the row...
      if ( meta.IncludeDN() && !Utils.isEmpty( meta.getDNField() ) ) {
        outputRowData[data.totalpreviousfields + rowIndex++] = data.recordLDIF.getDN();
      }
      // Possibly add short filename...
      if ( meta.getShortFileNameField() != null && meta.getShortFileNameField().length() > 0 ) {
        outputRowData[rowIndex++] = data.shortFilename;
      }
      // Add Extension
      if ( meta.getExtensionField() != null && meta.getExtensionField().length() > 0 ) {
        outputRowData[rowIndex++] = data.extension;
      }
      // add path
      if ( meta.getPathField() != null && meta.getPathField().length() > 0 ) {
        outputRowData[rowIndex++] = data.path;
      }
      // Add Size
      if ( meta.getSizeField() != null && meta.getSizeField().length() > 0 ) {
        outputRowData[rowIndex++] = new Long( data.size );
      }
      // add Hidden
      if ( meta.isHiddenField() != null && meta.isHiddenField().length() > 0 ) {
        outputRowData[rowIndex++] = new Boolean( data.hidden );
      }
      // Add modification date
      if ( meta.getLastModificationDateField() != null && meta.getLastModificationDateField().length() > 0 ) {
        outputRowData[rowIndex++] = data.lastModificationDateTime;
      }
      // Add Uri
      if ( meta.getUriField() != null && meta.getUriField().length() > 0 ) {
        outputRowData[rowIndex++] = data.uriName;
      }
      // Add RootUri
      if ( meta.getRootUriField() != null && meta.getRootUriField().length() > 0 ) {
        outputRowData[rowIndex++] = data.rootUriName;
      }
      RowMetaInterface irow = getInputRowMeta();

      data.previousRow = irow == null ? outputRowData : irow.cloneRow( outputRowData ); // copy it to make
      // surely the next step doesn't change it in between...

      incrementLinesInput();
      data.rownr++;

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "LDIFInput.Exception.UnableToReadFile", data.file
        .toString() ), e );
    }

    return outputRowData;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    Object[] r = null;

    boolean sendToErrorRow = false;
    String errorMessage = null;

    try {
      // Grab one row
      Object[] outputRowData = getOneRow();
      if ( outputRowData == null ) {
        setOutputDone(); // signal end to receiver(s)
        return false; // end of data or error.
      }

      putRow( data.outputRowMeta, outputRowData ); // copy row to output rowset(s);

      if ( meta.getRowLimit() > 0 && data.rownr > meta.getRowLimit() ) { // limit has been reached: stop now.
        setOutputDone();
        return false;
      }
    } catch ( KettleException e ) {
      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "LDIFInput.ErrorInStepRunning", e.getMessage() ) );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, null, "LDIFInput001" );
      }
    }
    return true;
  }

  private boolean openNextFile() {
    try {
      if ( !meta.isFileField() ) {
        if ( data.filenr >= data.files.nrOfFiles() ) {
          // finished processing!

          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "LDIFInput.Log.FinishedProcessing" ) );
          }
          return false;
        }

        // Is this the last file?
        data.last_file = ( data.filenr == data.files.nrOfFiles() - 1 );
        data.file = data.files.getFile( data.filenr );

        // Move file pointer ahead!
        data.filenr++;
      } else {
        data.readrow = getRow(); // Get row from input rowset & set row busy!
        if ( data.readrow == null ) {
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "LDIFInput.Log.FinishedProcessing" ) );
          }
          return false;
        }

        if ( first ) {
          first = false;

          data.inputRowMeta = getInputRowMeta();
          data.outputRowMeta = data.inputRowMeta.clone();
          meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

          // Get total previous fields
          data.totalpreviousfields = data.inputRowMeta.size();

          // Create convert meta-data objects that will contain Date & Number formatters
          data.convertRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );

          // Check is filename field is provided
          if ( Utils.isEmpty( meta.getDynamicFilenameField() ) ) {
            logError( BaseMessages.getString( PKG, "LDIFInput.Log.NoField" ) );
            throw new KettleException( BaseMessages.getString( PKG, "LDIFInput.Log.NoField" ) );
          }

          // cache the position of the field
          if ( data.indexOfFilenameField < 0 ) {
            data.indexOfFilenameField = getInputRowMeta().indexOfValue( meta.getDynamicFilenameField() );
            if ( data.indexOfFilenameField < 0 ) {
              // The field is unreachable !
              logError( BaseMessages.getString( PKG, "LDIFInput.Log.ErrorFindingField" )
                + "[" + meta.getDynamicFilenameField() + "]" );
              throw new KettleException( BaseMessages.getString(
                PKG, "LDIFInput.Exception.CouldnotFindField", meta.getDynamicFilenameField() ) );
            }
          }

        } // End if first
        String filename = getInputRowMeta().getString( data.readrow, data.indexOfFilenameField );
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "LDIFInput.Log.FilenameInStream", meta
            .getDynamicFilenameField(), filename ) );
        }

        data.file = KettleVFS.getFileObject( filename, getTransMeta() );
      }
      data.filename = KettleVFS.getFilename( data.file );
      // Add additional fields?
      if ( meta.getShortFileNameField() != null && meta.getShortFileNameField().length() > 0 ) {
        data.shortFilename = data.file.getName().getBaseName();
      }
      try {

        if ( meta.getPathField() != null && meta.getPathField().length() > 0 ) {
          data.path = KettleVFS.getFilename( data.file.getParent() );
        }
        if ( meta.isHiddenField() != null && meta.isHiddenField().length() > 0 ) {
          data.hidden = data.file.isHidden();
        }
        if ( meta.getExtensionField() != null && meta.getExtensionField().length() > 0 ) {
          data.extension = data.file.getName().getExtension();
        }
        if ( meta.getLastModificationDateField() != null && meta.getLastModificationDateField().length() > 0 ) {
          data.lastModificationDateTime = new Date( data.file.getContent().getLastModifiedTime() );
        }
        if ( meta.getUriField() != null && meta.getUriField().length() > 0 ) {
          data.uriName = data.file.getName().getURI();
        }
        if ( meta.getRootUriField() != null && meta.getRootUriField().length() > 0 ) {
          data.rootUriName = data.file.getName().getRootURI();
        }
        if ( meta.getSizeField() != null && meta.getSizeField().length() > 0 ) {
          data.size = new Long( data.file.getContent().getSize() );
        }
      } catch ( Exception e ) {
        throw new KettleException( e );
      }
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "LDIFInput.Log.OpeningFile", data.file.toString() ) );
      }

      if ( meta.AddToResultFilename() ) {
        // Add this to the result file names...
        ResultFile resultFile =
          new ResultFile( ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname() );
        resultFile.setComment( BaseMessages.getString( PKG, "LDIFInput.Log.FileAddedResult" ) );
        addResultFile( resultFile );
      }

      data.InputLDIF = new LDIF( KettleVFS.getFilename( data.file ) );

      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "LDIFInput.Log.FileOpened", data.file.toString() ) );
      }

    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "LDIFInput.Log.UnableToOpenFile", "" + data.filenr, data.file
        .toString(), e.toString() ) );
      stopAll();
      setErrors( 1 );
      return false;
    }
    return true;
  }

  @SuppressWarnings( "unchecked" )
  private String GetValue( LDAPAttribute[] attributes_LDIF, String AttributValue ) {
    String Stringvalue = null;
    int i = 0;

    for ( int j = 0; j < attributes_LDIF.length; j++ ) {
      LDAPAttribute attribute_DIF = attributes_LDIF[j];
      if ( attribute_DIF.getName().equalsIgnoreCase( AttributValue ) ) {
        Enumeration<String> valuesLDIF = attribute_DIF.getStringValues();

        while ( valuesLDIF.hasMoreElements() ) {
          String valueLDIF = valuesLDIF.nextElement();
          if ( i == 0 ) {
            Stringvalue = valueLDIF;
          } else {
            Stringvalue = Stringvalue + data.multiValueSeparator + valueLDIF;
          }
          i++;
        }
      }
    }
    return Stringvalue;
  }

  /**
   * Build an empty row based on the meta-data.
   *
   * @return
   */
  private Object[] buildEmptyRow() {
    Object[] rowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );

    return rowData;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (LDIFInputMeta) smi;
    data = (LDIFInputData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( !meta.isFileField() ) {
        data.files = meta.getFiles( this );
        if ( data.files.nrOfFiles() == 0 && data.files.nrOfMissingFiles() == 0 ) {
          logError( BaseMessages.getString( PKG, "LDIFInput.Log.NoFiles" ) );
          return false;
        }
        try {
          // Create the output row meta-data
          data.outputRowMeta = new RowMeta();

          meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

          // Create convert meta-data objects that will contain Date & Number formatters
          data.convertRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );

          data.nrInputFields = meta.getInputFields().length;
          data.multiValueSeparator = environmentSubstitute( meta.getMultiValuedSeparator() );
        } catch ( Exception e ) {
          logError( "Error initializing step: " + e.toString() );
          logError( Const.getStackTracker( e ) );
          return false;
        }
      }
      data.rownr = 1L;
      data.totalpreviousfields = 0;
      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (LDIFInputMeta) smi;
    data = (LDIFInputData) sdi;
    if ( data.file != null ) {
      try {
        data.file.close();
      } catch ( Exception e ) {
        // Ignore errors
      }
    }
    if ( data.InputLDIF != null ) {
      data.InputLDIF = null;
    }
    if ( data.attributes_LDIF != null ) {
      data.attributes_LDIF = null;
    }
    if ( data.recordLDIF != null ) {
      data.recordLDIF = null;
    }
    super.dispose( smi, sdi );
  }

}
