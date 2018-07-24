/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.file;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A base implementation for all output file based metas.
 */
public abstract class BaseFileOutputMeta extends BaseFileMeta {

  /**
   * Flag: add the stepnr in the filename
   */
  @Injection( name = "INC_STEPNR_IN_FILENAME" )
  protected boolean stepNrInFilename;

  /**
   * Flag: add the partition number in the filename
   */
  @Injection( name = "INC_PARTNR_IN_FILENAME" )
  protected boolean partNrInFilename;

  /**
   * Flag: add the date in the filename
   */
  @Injection( name = "INC_DATE_IN_FILENAME" )
  protected boolean dateInFilename;

  /**
   * Flag: add the time in the filename
   */
  @Injection( name = "INC_TIME_IN_FILENAME" )
  protected boolean timeInFilename;

  /**
   * The file extention in case of a generated filename
   */
  @Injection( name = "EXTENSION" )
  protected String extension;

  /**
   * The base name of the output file
   */
  @Injection( name = "FILENAME" )
  protected String fileName;

  /**
   * Whether to treat this as a command to be executed and piped into
   */
  @Injection( name = "RUN_AS_COMMAND" )
  private boolean fileAsCommand;

  /**
   * Flag : Do not open new file when transformation start
   */
  @Injection( name = "SPECIFY_DATE_FORMAT" )
  private boolean specifyingFormat;

  /**
   * The date format appended to the file name
   */
  @Injection( name = "DATE_FORMAT" )
  private String dateTimeFormat;

  /**
   * The file compression: None, Zip or Gzip
   */
  @Injection( name = "COMPRESSION" )
  private String fileCompression;

  public String getExtension() {
    return extension;
  }

  public void setExtension( String extension ) {
    this.extension = extension;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName( String fileName ) {
    this.fileName = fileName;
  }

  public abstract int getSplitEvery();

  public int getSplitEvery( VariableSpace space ) {
    return getSplitEvery();
  }

  public abstract void setSplitEvery( int splitEvery );

  public boolean isFileAsCommand() {
    return fileAsCommand;
  }

  public void setFileAsCommand( boolean fileAsCommand ) {
    this.fileAsCommand = fileAsCommand;
  }

  public boolean isSpecifyingFormat() {
    return specifyingFormat;
  }

  public void setSpecifyingFormat( boolean specifyingFormat ) {
    this.specifyingFormat = specifyingFormat;
  }

  public String getDateTimeFormat() {
    return dateTimeFormat;
  }

  public void setDateTimeFormat( String dateTimeFormat ) {
    this.dateTimeFormat = dateTimeFormat;
  }

  public boolean isTimeInFilename() {
    return timeInFilename;
  }

  public boolean isDateInFilename() {
    return dateInFilename;
  }

  public boolean isPartNrInFilename() {
    return partNrInFilename;
  }

  public boolean isStepNrInFilename() {
    return stepNrInFilename;
  }

  public String getFileCompression() {
    return fileCompression;
  }

  public void setFileCompression( String fileCompression ) {
    this.fileCompression = fileCompression;
  }


  public String[] getFiles( final VariableSpace space ) {
    return getFiles( space, true );
  }

  private String[] getFiles( final VariableSpace space, final boolean showSamples ) {

    String realFileName = space.environmentSubstitute( fileName );
    String realExtension = space.environmentSubstitute( extension );

    return getFiles( realFileName, realExtension, showSamples );
  }

  @VisibleForTesting
  String[] getFiles( final String realFileName, final String realExtension, final boolean showSamples ) {
    final Date now = new Date();

    if ( showSamples ) {
      int copies = 1;
      int splits = 1;
      int parts = 1;

      if ( isStepNrInFilename() ) {
        copies = 3;
      }

      if ( isPartNrInFilename() ) {
        parts = 3;
      }

      if ( getSplitEvery() != 0 ) {
        splits = 3;
      }

      int nr = copies * parts * splits;
      if ( nr > 1 ) {
        nr++;
      }

      String[] retval = new String[ nr ];

      int i = 0;
      for ( int step = 0; step < copies; step++ ) {
        for ( int part = 0; part < parts; part++ ) {
          for ( int split = 0; split < splits; split++ ) {
            retval[ i ] = buildFilename(
              realFileName, realExtension, step + "", getPartPrefix() + part, split + "", now, false, showSamples );
            i++;
          }
        }
      }
      if ( i < nr ) {
        retval[ i ] = "...";
      }

      return retval;
    } else {
      return new String[] { buildFilename( realFileName, realExtension, "<step>", "<partition>", "<split>", now, false,
        showSamples ) };
    }
  }

  protected String getPartPrefix() {
    return "";
  }

  public String buildFilename(
    final VariableSpace space, final String stepnr, final String partnr, final String splitnr,
    final boolean ziparchive ) {
    return buildFilename( space, stepnr, partnr, splitnr, ziparchive, true );
  }

  public String buildFilename(
    final VariableSpace space, final String stepnr, final String partnr, final String splitnr,
    final boolean ziparchive, final boolean showSamples ) {

    String realFileName = space.environmentSubstitute( fileName );
    String realExtension = space.environmentSubstitute( extension );

    return buildFilename( realFileName, realExtension, stepnr, partnr, splitnr, new Date(), ziparchive, showSamples );
  }

  private String buildFilename(
    final String realFileName, final String realExtension, final String stepnr, final String partnr,
    final String splitnr,
    final Date date, final boolean ziparchive, final boolean showSamples ) {
    return buildFilename( realFileName, realExtension, stepnr, partnr, splitnr, date, ziparchive, showSamples, this );
  }


  protected String buildFilename(
    final String realFileName, final String realExtension, final String stepnr, final String partnr,
    final String splitnr, final Date date, final boolean ziparchive, final boolean showSamples,
    final BaseFileOutputMeta meta ) {
    return buildFilename( null, realFileName, realExtension, stepnr, partnr, splitnr, date, ziparchive, showSamples,
      meta );
  }

  protected String buildFilename(
    final VariableSpace space, final String realFileName, final String realExtension, final String stepnr,
    final String partnr, final String splitnr, final Date date, final boolean ziparchive, final boolean showSamples,
    final BaseFileOutputMeta meta ) {

    SimpleDateFormat daf = new SimpleDateFormat();

    // Replace possible environment variables...
    String retval = realFileName;

    if ( meta.isFileAsCommand() ) {
      return retval;
    }

    Date now = date == null ? new Date() : date;

    if ( meta.isSpecifyingFormat() && !Utils.isEmpty( meta.getDateTimeFormat() ) ) {
      daf.applyPattern( meta.getDateTimeFormat() );
      String dt = daf.format( now );
      retval += dt;
    } else {
      if ( meta.isDateInFilename() ) {
        if ( showSamples ) {
          daf.applyPattern( "yyyMMdd" );
          String d = daf.format( now );
          retval += "_" + d;
        } else {
          retval += "_<yyyMMdd>";
        }
      }
      if ( meta.isTimeInFilename() ) {
        if ( showSamples ) {
          daf.applyPattern( "HHmmss" );
          String t = daf.format( now );
          retval += "_" + t;
        } else {
          retval += "_<HHmmss>";
        }
      }
    }
    if ( meta.isStepNrInFilename() ) {
      retval += "_" + stepnr;
    }
    if ( meta.isPartNrInFilename() ) {
      retval += "_" + partnr;
    }
    if ( meta.getSplitEvery( space ) > 0 ) {
      retval += "_" + splitnr;
    }

    if ( "Zip".equals( meta.getFileCompression() ) ) {
      if ( ziparchive ) {
        retval += ".zip";
      } else {
        if ( realExtension != null && realExtension.length() != 0 ) {
          retval += "." + realExtension;
        }
      }
    } else {
      if ( realExtension != null && realExtension.length() != 0 ) {
        retval += "." + realExtension;
      }
      if ( "GZip".equals( meta.getFileCompression() ) ) {
        retval += ".gz";
      }
    }
    return retval;
  }

  @Override
  public String[] getFilePaths( final boolean showSamples ) {
    final StepMeta parentStepMeta = getParentStepMeta();
    if ( parentStepMeta != null ) {
      final TransMeta parentTransMeta = parentStepMeta.getParentTransMeta();
      if ( parentTransMeta != null ) {
        return getFiles( parentTransMeta, showSamples );
      }
    }
    return new String[] {};
  }
}
