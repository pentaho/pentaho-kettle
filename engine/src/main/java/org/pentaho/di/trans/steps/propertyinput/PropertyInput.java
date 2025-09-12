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


package org.pentaho.di.trans.steps.propertyinput;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
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
 * Read all Properties files (& INI files) , convert them to rows and writes these to one or more output streams.
 *
 * @author Samatar
 * @since 24-03-2008
 */
public class PropertyInput extends BaseStep implements StepInterface {
  private static Class<?> PKG = PropertyInputMeta.class; // for i18n purposes, needed by Translator2!!

  private PropertyInputMeta meta;
  private PropertyInputData data;

  public PropertyInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    if ( first && !meta.isFileField() ) {
      data.files = meta.getFiles( this );
      if ( data.files == null || data.files.nrOfFiles() == 0 ) {
        throw new KettleException( BaseMessages.getString( PKG, "PropertyInput.Log.NoFiles" ) );
      }

      handleMissingFiles();

      // Create the output row meta-data
      data.outputRowMeta = new RowMeta();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore ); // get the metadata
                                                                                                    // populated

      // Create convert meta-data objects that will contain Date & Number formatters
      //
      data.convertRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );
    }
    Object[] r = null;

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
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, e.toString(), null, "PropertyInput001" );
      } else {
        logError( BaseMessages.getString( PKG, "PropertyInput.ErrorInStepRunning", e.getMessage() ) );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
    }
    return true;
  }

  private void handleMissingFiles() throws KettleException {
    List<FileObject> nonExistantFiles = data.files.getNonExistantFiles();
    if ( !nonExistantFiles.isEmpty() ) {
      String message = FileInputList.getRequiredFilesDescription( nonExistantFiles );
      logError( BaseMessages.getString( PKG, "PropertyInput.Log.RequiredFilesTitle" ), BaseMessages.getString(
        PKG, "PropertyInput.Log.RequiredFiles", message ) );

      throw new KettleException( BaseMessages.getString( PKG, "PropertyInput.Log.RequiredFilesMissing", message ) );
    }

    List<FileObject> nonAccessibleFiles = data.files.getNonAccessibleFiles();
    if ( !nonAccessibleFiles.isEmpty() ) {
      String message = FileInputList.getRequiredFilesDescription( nonAccessibleFiles );
      logError( BaseMessages.getString( PKG, "PropertyInput.Log.RequiredFilesTitle" ), BaseMessages.getString(
        PKG, "PropertyInput.Log.RequiredNotAccessibleFiles", message ) );

      throw new KettleException( BaseMessages.getString(
        PKG, "PropertyInput.Log.RequiredNotAccessibleFilesMissing", message ) );
    }
  }

  private Object[] getOneRow() throws KettleException {
    try {
      if ( meta.isFileField() ) {
        while ( ( data.readrow == null )
          || ( ( data.propfiles && !data.propIt.hasNext() ) || ( !data.propfiles && !data.iniIt.hasNext() ) ) ) {

          // In case we read all sections
          // maybe we have to change section for ini files...
          if ( !data.propfiles && data.realSection == null && data.readrow != null && data.iniSectionIt.hasNext() ) {
            data.currentSection = data.iniSectionIt.next();
            data.iniSection = data.iniConf.getSection( data.currentSection );
            data.iniIt = data.iniSection.getKeys();
          } else {
            if ( !openNextFile() ) {
              return null;
            }
          }
        }
      } else {
        while ( ( data.file == null )
          || ( ( data.propfiles && !data.propIt.hasNext() ) || ( !data.propfiles && !data.iniIt.hasNext() ) ) ) {
          // In case we read all sections
          // maybe we have to change section for ini files...
          if ( !data.propfiles && data.realSection == null && data.file != null && data.iniSectionIt.hasNext() ) {
            data.currentSection = data.iniSectionIt.next();
            data.iniSection = data.iniConf.getSection( data.currentSection );
            data.iniIt = data.iniSection.getKeys();
          } else {
            if ( !openNextFile() ) {
              return null;
            }
          }
        }
      }
    } catch ( Exception ex ) {
      logError( "Unable to read row from file : " + ex.getMessage() );
      return null;
    }
    // Build an empty row based on the meta-data
    Object[] r = buildEmptyRow();

    // Create new row or clone
    if ( meta.isFileField() ) {
      r = data.readrow.clone();
    }

    try {
      String key = null;
      if ( data.propfiles ) {
        key = data.propIt.next().toString();
      } else {
        key = data.iniIt.next();
      }

      // Execute for each Input field...
      for ( int i = 0; i < meta.getInputFields().length; i++ ) {
        // Get field value
        String value = null;

        if ( meta.getInputFields()[i].getColumnCode().equals(
          PropertyInputField.ColumnCode[PropertyInputField.COLUMN_KEY] ) ) {
          value = key;
        } else {
          if ( meta.isResolveValueVariable() ) {
            if ( data.propfiles ) {
              value = environmentSubstitute( data.pro.getProperty( key ) );
            } else {
              value = environmentSubstitute( data.iniSection.getString( key ) ); // for INI files
            }
          } else {
            if ( data.propfiles ) {
              value = data.pro.getProperty( key );
            } else {
              value = data.iniSection.getString( key ); // for INI files
            }
          }
        }

        // DO Trimming!
        switch ( meta.getInputFields()[i].getTrimType() ) {
          case PropertyInputField.TYPE_TRIM_LEFT:
            value = Const.ltrim( value );
            break;
          case PropertyInputField.TYPE_TRIM_RIGHT:
            value = Const.rtrim( value );
            break;
          case PropertyInputField.TYPE_TRIM_BOTH:
            value = Const.trim( value );
            break;
          default:
            break;
        }

        if ( meta.isFileField() ) {
          // Add result field to input stream
          r = RowDataUtil.addValueData( r, data.totalpreviousfields + i, value );
        }

        // DO CONVERSIONS...
        //
        ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta( data.totalpreviousfields + i );
        ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta( data.totalpreviousfields + i );
        r[data.totalpreviousfields + i] = targetValueMeta.convertData( sourceValueMeta, value );

        // Do we need to repeat this field if it is null?
        if ( meta.getInputFields()[ i ].isRepeated() && data.previousRow != null && Utils.isEmpty( value ) ) {
          r[ data.totalpreviousfields + i ] = data.previousRow[ data.totalpreviousfields + i ];
        }
      } // End of loop over fields...

      int rowIndex = meta.getInputFields().length;

      // See if we need to add the filename to the row...
      if ( meta.includeFilename() && !Utils.isEmpty( meta.getFilenameField() ) ) {
        r[data.totalpreviousfields + rowIndex++] = data.filename;
      }

      // See if we need to add the row number to the row...
      if ( meta.includeRowNumber() && !Utils.isEmpty( meta.getRowNumberField() ) ) {
        r[data.totalpreviousfields + rowIndex++] = data.rownr;
      }

      // See if we need to add the section for INI files ...
      if ( meta.includeIniSection() && !Utils.isEmpty( meta.getINISectionField() ) ) {
        r[data.totalpreviousfields + rowIndex++] = environmentSubstitute( data.currentSection );
      }
      // Possibly add short filename...
      if ( !Utils.isEmpty( meta.getShortFileNameField() ) ) {
        r[data.totalpreviousfields + rowIndex++] = data.shortFilename;
      }
      // Add Extension
      if ( !Utils.isEmpty( meta.getExtensionField() ) ) {
        r[data.totalpreviousfields + rowIndex++] = data.extension;
      }
      // add path
      if ( !Utils.isEmpty( meta.getPathField() ) ) {
        r[data.totalpreviousfields + rowIndex++] = data.path;
      }
      // Add Size
      if ( !Utils.isEmpty( meta.getSizeField() ) ) {
        r[data.totalpreviousfields + rowIndex++] = data.size;
      }
      // add Hidden
      if ( !Utils.isEmpty( meta.isHiddenField() ) ) {
        r[data.totalpreviousfields + rowIndex++] = data.hidden;
      }
      // Add modification date
      if ( !Utils.isEmpty( meta.getLastModificationDateField() ) ) {
        r[data.totalpreviousfields + rowIndex++] = data.lastModificationDateTime;
      }
      // Add Uri
      if ( !Utils.isEmpty( meta.getUriField() ) ) {
        r[data.totalpreviousfields + rowIndex++] = data.uriName;
      }
      // Add RootUri
      if ( !Utils.isEmpty( meta.getRootUriField() ) ) {
        r[data.totalpreviousfields + rowIndex++] = data.rootUriName;
      }

      RowMetaInterface iRow = getInputRowMeta();

      data.previousRow = iRow == null ? r : iRow.cloneRow( r ); // copy it to make
      // surely the next step doesn't change it in between...

      incrementLinesInput();
      data.rownr++;
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "PropertyInput.Error.CanNotReadFromFile", data.file
        .toString() ), e );
    }

    return r;
  }

  private boolean openNextFile() {
    try {
      if ( !meta.isFileField() ) {
        if ( data.filenr >= data.files.nrOfFiles() ) { // finished processing!

          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "PropertyInput.Log.FinishedProcessing" ) );
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
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "PropertyInput.Log.FinishedProcessing" ) );
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
            logError( BaseMessages.getString( PKG, "PropertyInput.Log.NoField" ) );
            throw new KettleException( BaseMessages.getString( PKG, "PropertyInput.Log.NoField" ) );
          }

          // cache the position of the field
          if ( data.indexOfFilenameField < 0 ) {
            data.indexOfFilenameField = getInputRowMeta().indexOfValue( meta.getDynamicFilenameField() );
            if ( data.indexOfFilenameField < 0 ) {
              // The field is unreachable !
              logError( BaseMessages.getString( PKG, "PropertyInput.Log.ErrorFindingField" )
                + '[' + meta.getDynamicFilenameField() + ']' );
              throw new KettleException( BaseMessages.getString(
                PKG, "PropertyInput.Exception.CouldnotFindField", meta.getDynamicFilenameField() ) );
            }
          }
        } // End if first

        String filename = getInputRowMeta().getString( data.readrow, data.indexOfFilenameField );
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "PropertyInput.Log.FilenameInStream", meta
            .getDynamicFilenameField(), filename ) );
        }

        data.file = KettleVFS.getFileObject( filename, getTransMeta() );
        // Check if file exists!
      }

      data.filename = KettleVFS.getFilename( data.file );

      // Add additional fields?
      if ( !Utils.isEmpty( meta.getShortFileNameField() ) ) {
        data.shortFilename = data.file.getName().getBaseName();
      }
      if ( !Utils.isEmpty( meta.getPathField() ) ) {
        data.path = KettleVFS.getFilename( data.file.getParent() );
      }
      if ( !Utils.isEmpty( meta.isHiddenField() ) ) {
        data.hidden = data.file.isHidden();
      }
      if ( !Utils.isEmpty( meta.getExtensionField() ) ) {
        data.extension = data.file.getName().getExtension();
      }
      if ( !Utils.isEmpty( meta.getLastModificationDateField() ) ) {
        data.lastModificationDateTime = new Date( data.file.getContent().getLastModifiedTime() );
      }
      if ( !Utils.isEmpty( meta.getUriField() ) ) {
        data.uriName = Const.optionallyDecodeUriString( data.file.getName().getURI() );
      }
      if ( !Utils.isEmpty( meta.getRootUriField() ) ) {
        data.rootUriName = data.file.getName().getRootURI();
      }
      if ( !Utils.isEmpty( meta.getSizeField() ) ) {
        data.size = data.file.getContent().getSize();
      }

      if ( meta.resetRowNumber() ) {
        data.rownr = 0;
      }

      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "PropertyInput.Log.OpeningFile", data.file.toString() ) );
      }

      if ( meta.isAddResultFile() ) {
        // Add this to the result file names...
        ResultFile resultFile =
          new ResultFile( ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname() );
        resultFile.setComment( BaseMessages.getString( PKG, "PropertyInput.Log.FileAddedResult" ) );
        addResultFile( resultFile );
      }

      if ( data.propfiles ) {
        loadPropertiesFile();
      } else {
        loadIniFile();
      }

      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "PropertyInput.Log.FileOpened", data.file.toString() ) );
        logDetailed( BaseMessages.getString( PKG, "PropertyInput.log.TotalKey", ""
          + ( data.propfiles ? data.pro.size() : data.iniSection.size() ), KettleVFS.getFilename( data.file ) ) );
      }
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "PropertyInput.Log.UnableToOpenFile", "" + data.filenr, data.file
        .toString(), e.toString() ) );
      stopAll();
      setErrors( 1 );
      return false;
    }
    return true;
  }

  private void loadPropertiesFile() throws IOException {
    try ( InputStream fis = data.file.getContent().getInputStream() ) {
      // load properties file
      data.pro = new Properties();
      data.pro.load( fis );
      data.propIt = data.pro.keySet().iterator();
    }
  }

  /**
   * Load an INI file using Apache Commons Configuration
   *
   * @throws ConfigurationException
   * @throws IOException
   */
  private void loadIniFile() throws ConfigurationException, IOException, KettleException {
    data.iniConf = new INIConfiguration();
    FileHandler handler = new FileHandler( data.iniConf );

    try ( InputStream inputStream = KettleVFS.getInputStream( data.file ) ) {
      if ( !Utils.isEmpty( data.realEncoding ) ) {
        handler.setEncoding( data.realEncoding );
      }
      handler.load( inputStream );
      Set<String> sections = data.iniConf.getSections();

      if ( data.realSection != null ) {
        // just one section
        data.iniSection = data.iniConf.getSection( data.realSection );
        if ( data.iniSection == null ) {
          throw new KettleException(
            BaseMessages.getString( PKG, "PropertyInput.Error.CanNotFindSection", data.realSection,
              "" + data.file.getName() ) );
        }
      } else {
        // We need to fetch all sections
        data.iniSectionIt = sections.iterator();
        data.currentSection = data.iniSectionIt.next();
        data.iniSection = data.iniConf.getSection( data.currentSection );
      }
      data.iniIt = data.iniSection.getKeys();
    }
  }

  /**
   * Build an empty row based on the meta-data...
   *
   * @return an empty row with the correct size
   */
  private Object[] buildEmptyRow() {
    return RowDataUtil.allocateRowData( data.outputRowMeta.size() );
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (PropertyInputMeta) smi;
    data = (PropertyInputData) sdi;

    if ( super.init( smi, sdi ) ) {
      //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
      if ( getTransMeta().getNamedClusterEmbedManager() != null ) {
        getTransMeta().getNamedClusterEmbedManager()
          .passEmbeddedMetastoreKey( this, getTransMeta().getEmbeddedMetastoreProviderKey() );
      }

      String realEncoding = environmentSubstitute( meta.getEncoding() );
      if ( !Utils.isEmpty( realEncoding ) ) {
        data.realEncoding = realEncoding;
      }
      String realSection = environmentSubstitute( meta.getSection() );
      if ( !Utils.isEmpty( realSection ) ) {
        data.realSection = realSection;
      }
      data.propfiles =
        ( PropertyInputMeta.getFileTypeByDesc( meta.getFileType() ) == PropertyInputMeta.FILE_TYPE_PROPERTY );
      data.rownr = 1L;
      data.totalpreviousfields = 0;

      return true;
    }
    return false;
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (PropertyInputMeta) smi;
    data = (PropertyInputData) sdi;

    if ( data.readrow != null ) {
      data.readrow = null;
    }
    if ( data.iniSection != null ) {
      data.iniSection.clear();
    }
    data.iniSection = null;
    if ( data.iniSectionIt != null ) {
      data.iniSectionIt = null;
    }
    if ( data.file != null ) {
      try {
        data.file.close();
        data.file = null;
      } catch ( Exception e ) {
        // Ignore errors
      }
    }

    super.dispose( smi, sdi );
  }
}
