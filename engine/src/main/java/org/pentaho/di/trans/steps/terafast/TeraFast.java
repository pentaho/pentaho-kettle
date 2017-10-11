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

package org.pentaho.di.trans.steps.terafast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.AbstractStep;
import org.pentaho.di.core.util.ConfigurableStreamLogger;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * @author <a href="mailto:michael.gugerell@aschauer-edv.at">Michael Gugerell(asc145)</a>
 *
 */
public class TeraFast extends AbstractStep implements StepInterface {

  private static Class<?> PKG = TeraFastMeta.class; // for i18n purposes, needed by Translator2!!

  private TeraFastMeta meta;

  private Process process;

  private OutputStream fastload;

  private OutputStream dataFile;

  private PrintStream dataFilePrintStream;

  private List<Integer> columnSortOrder;

  private RowMetaInterface tableRowMeta;

  private SimpleDateFormat simpleDateFormat;

  /**
   * Constructor.
   *
   * @param stepMeta
   *          the stepMeta.
   * @param stepDataInterface
   *          the stepDataInterface.
   * @param copyNr
   *          the copyNr.
   * @param transMeta
   *          the transMeta.
   * @param trans
   *          the trans.
   */
  public TeraFast( final StepMeta stepMeta, final StepDataInterface stepDataInterface, final int copyNr,
    final TransMeta transMeta, final Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  /**
   * Create the command line for a fastload process depending on the meta information supplied.
   *
   * @return The string to execute.
   *
   * @throws KettleException
   *           Upon any exception
   */
  public String createCommandLine() throws KettleException {
    if ( StringUtils.isBlank( this.meta.getFastloadPath().getValue() ) ) {
      throw new KettleException( "Fastload path not set" );
    }
    final StringBuilder builder = new StringBuilder();
    try {
      final FileObject fileObject =
        KettleVFS.getFileObject( environmentSubstitute( this.meta.getFastloadPath().getValue() ) );
      final String fastloadExec = KettleVFS.getFilename( fileObject );
      builder.append( fastloadExec );
    } catch ( Exception e ) {
      throw new KettleException( "Error retrieving fastload application string", e );
    }
    // Add log error log, if set.
    if ( StringUtils.isNotBlank( this.meta.getLogFile().getValue() ) ) {
      try {
        FileObject fileObject =
          KettleVFS.getFileObject( environmentSubstitute( this.meta.getLogFile().getValue() ) );
        builder.append( " -e " );
        builder.append( "\"" + KettleVFS.getFilename( fileObject ) + "\"" );
      } catch ( Exception e ) {
        throw new KettleException( "Error retrieving logfile string", e );
      }
    }
    return builder.toString();
  }

  /**
   * {@inheritDoc}
   *
   * @see org.pentaho.di.trans.step.BaseStep#init(org.pentaho.di.trans.step.StepMetaInterface,
   *      org.pentaho.di.trans.step.StepDataInterface)
   */
  @Override
  public boolean init( final StepMetaInterface smi, final StepDataInterface sdi ) {
    this.meta = (TeraFastMeta) smi;
    // this.data = (GenericStepData) sdi;
    simpleDateFormat = new SimpleDateFormat( FastloadControlBuilder.DEFAULT_DATE_FORMAT );
    return super.init( smi, sdi );
  }

  /**
   * {@inheritDoc}
   *
   * @see org.pentaho.di.trans.step.BaseStep#processRow(org.pentaho.di.trans.step.StepMetaInterface,
   *      org.pentaho.di.trans.step.StepDataInterface)
   */
  @Override
  public boolean processRow( final StepMetaInterface smi, final StepDataInterface sdi ) throws KettleException {
    this.meta = (TeraFastMeta) smi;
    // this.data = (GenericStepData) sdi;

    Object[] row = getRow();
    if ( row == null ) {

      this.dataFilePrintStream.close();

      IOUtils.closeQuietly( this.dataFile );

      this.execute();

      setOutputDone();

      try {
        logBasic( BaseMessages.getString( PKG, "TeraFast.Log.WatingForFastload" ) );
        if ( this.process != null ) {
          final int exitVal = this.process.waitFor();
          if ( exitVal != 0 ) {
            setErrors( DEFAULT_ERROR_CODE );
          }
          logBasic( BaseMessages.getString( PKG, "TeraFast.Log.ExitValueFastloadPath", "" + exitVal ) );
        }
      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "TeraFast.Log.ErrorInStep" ), e );
        this.setDefaultError();
        stopAll();
      }

      return false;
    }

    if ( this.first ) {
      this.first = false;
      try {
        final File tempDataFile = new File( resolveFileName( this.meta.getDataFile().getValue() ) );
        this.dataFile = FileUtils.openOutputStream( tempDataFile );
        this.dataFilePrintStream = new PrintStream( dataFile );
      } catch ( IOException e ) {
        throw new KettleException( "Cannot open data file [path=" + this.dataFile + "]", e );
      }

      // determine column sort order according to field mapping
      // thus the columns in the generated datafile are always in the same order and have the same size as in the
      // targetTable
      this.tableRowMeta = this.meta.getRequiredFields( this.getTransMeta() );
      RowMetaInterface streamRowMeta = this.getTransMeta().getPrevStepFields( this.getStepMeta() );
      this.columnSortOrder = new ArrayList<Integer>( this.tableRowMeta.size() );
      for ( int i = 0; i < this.tableRowMeta.size(); i++ ) {
        ValueMetaInterface column = this.tableRowMeta.getValueMeta( i );
        int tableIndex = this.meta.getTableFieldList().getValue().indexOf( column.getName() );
        if ( tableIndex >= 0 ) {
          String streamField = this.meta.getStreamFieldList().getValue().get( tableIndex );
          this.columnSortOrder.add( streamRowMeta.indexOfValue( streamField ) );
        }
      }
    }

    writeToDataFile( getInputRowMeta(), row );
    return true;
  }

  /**
   * Write a single row to the temporary data file.
   *
   * @param rowMetaInterface
   *          describe the row of data
   *
   * @param row
   *          row entries
   * @throws KettleException
   *           ...
   */
  public void writeToDataFile( RowMetaInterface rowMetaInterface, Object[] row ) throws KettleException {
    // Write the data to the output
    ValueMetaInterface valueMeta = null;

    for ( int i = 0; i < row.length; i++ ) {
      if ( row[i] == null ) {
        break; // no more rows
      }
      valueMeta = rowMetaInterface.getValueMeta( i );
      if ( row[i] != null ) {
        switch ( valueMeta.getType() ) {
          case ValueMetaInterface.TYPE_STRING:
            String s = rowMetaInterface.getString( row, i );
            dataFilePrintStream.print( pad( valueMeta, s.toString() ) );
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            Long l = rowMetaInterface.getInteger( row, i );
            dataFilePrintStream.print( pad( valueMeta, l.toString() ) );
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            Double d = rowMetaInterface.getNumber( row, i );
            dataFilePrintStream.print( pad( valueMeta, d.toString() ) );
            break;
          case ValueMetaInterface.TYPE_BIGNUMBER:
            BigDecimal bd = rowMetaInterface.getBigNumber( row, i );
            dataFilePrintStream.print( pad( valueMeta, bd.toString() ) );
            break;
          case ValueMetaInterface.TYPE_DATE:
            Date dt = rowMetaInterface.getDate( row, i );
            dataFilePrintStream.print( simpleDateFormat.format( dt ) );
            break;
          case ValueMetaInterface.TYPE_BOOLEAN:
            Boolean b = rowMetaInterface.getBoolean( row, i );
            if ( b.booleanValue() ) {
              dataFilePrintStream.print( "Y" );
            } else {
              dataFilePrintStream.print( "N" );
            }
            break;
          case ValueMetaInterface.TYPE_BINARY:
            byte[] byt = rowMetaInterface.getBinary( row, i );
            dataFilePrintStream.print( byt );
            break;
          default:
            throw new KettleException( BaseMessages.getString(
              PKG, "TeraFast.Exception.TypeNotSupported", valueMeta.getType() ) );
        }
      }
      dataFilePrintStream.print( FastloadControlBuilder.DATAFILE_COLUMN_SEPERATOR );
    }
    dataFilePrintStream.print( Const.CR );
  }

  private String pad( ValueMetaInterface valueMetaInterface, String data ) {
    StringBuilder padding = new StringBuilder( data );
    int padLength = valueMetaInterface.getLength() - data.length();
    int currentPadLength = 0;
    while ( currentPadLength < padLength ) {
      padding.append( " " );
      currentPadLength++;

    }
    return padding.toString();
  }

  /**
   * Execute fastload.
   *
   * @throws KettleException
   *           ...
   */
  public void execute() throws KettleException {
    if ( this.meta.getTruncateTable().getValue() ) {
      Database db = new Database( this, this.meta.getDbMeta() );
      db.connect();
      db.truncateTable( this.meta.getTargetTable().getValue() );
      db.commit();
      db.disconnect();
    }
    startFastLoad();

    if ( this.meta.getUseControlFile().getValue() ) {
      this.invokeLoadingControlFile();
    } else {
      this.invokeLoadingCommand();
    }
  }

  /**
   * Start fastload command line tool and initialize streams.
   *
   * @throws KettleException
   *           ...
   */
  private void startFastLoad() throws KettleException {
    final String command = this.createCommandLine();
    this.logBasic( "About to execute: " + command );
    try {
      this.process = Runtime.getRuntime().exec( command );
      new Thread( new ConfigurableStreamLogger(
        getLogChannel(), this.process.getErrorStream(), LogLevel.ERROR, "ERROR" ) ).start();
      new Thread( new ConfigurableStreamLogger(
        getLogChannel(), this.process.getInputStream(), LogLevel.DETAILED, "OUTPUT" ) ).start();
      this.fastload = this.process.getOutputStream();
    } catch ( Exception e ) {
      throw new KettleException( "Error while setup: " + command, e );
    }
  }

  /**
   * Invoke loading with control file.
   *
   * @throws KettleException
   *           ...
   */
  private void invokeLoadingControlFile() throws KettleException {
    File controlFile = null;
    final InputStream control;
    final String controlContent;
    try {
      controlFile = new File( resolveFileName( this.meta.getControlFile().getValue() ) );
      control = FileUtils.openInputStream( controlFile );
      controlContent = environmentSubstitute( FileUtils.readFileToString( controlFile ) );
    } catch ( IOException e ) {
      throw new KettleException( "Cannot open control file [path=" + controlFile + "]", e );
    }
    try {
      IOUtils.write( controlContent, this.fastload );
      this.fastload.flush();
    } catch ( IOException e ) {
      throw new KettleException( "Cannot pipe content of control file to fastload [path=" + controlFile + "]", e );
    } finally {
      IOUtils.closeQuietly( control );
      IOUtils.closeQuietly( this.fastload );
    }
  }

  /**
   * Invoke loading with loading commands.
   *
   * @throws KettleException
   *           ...
   */
  private void invokeLoadingCommand() throws KettleException {
    final FastloadControlBuilder builder = new FastloadControlBuilder();
    builder.setSessions( this.meta.getSessions().getValue() );
    builder.setErrorLimit( this.meta.getErrorLimit().getValue() );
    builder.logon( this.meta.getDbMeta().getHostname(), this.meta.getDbMeta().getUsername(), this.meta
      .getDbMeta().getPassword() );
    builder.setRecordFormat( FastloadControlBuilder.RECORD_VARTEXT );
    try {
      builder.define(
        this.meta.getRequiredFields( this.getTransMeta() ), meta.getTableFieldList(), resolveFileName( this.meta
          .getDataFile().getValue() ) );
    } catch ( Exception ex ) {
      throw new KettleException( "Error defining data file!", ex );
    }
    builder.show();
    builder.beginLoading( this.meta.getDbMeta().getPreferredSchemaName(), this.meta.getTargetTable().getValue() );

    builder.insert( this.meta.getRequiredFields( this.getTransMeta() ), meta.getTableFieldList(), this.meta
      .getTargetTable().getValue() );
    builder.endLoading();
    builder.logoff();
    final String control = builder.toString();
    try {
      logDetailed( "Control file: " + control );
      IOUtils.write( control, this.fastload );
      // this.fastload.flush();
    } catch ( IOException e ) {
      throw new KettleException( "Error while execution control command [controlCommand=" + control + "]", e );
    } finally {
      IOUtils.closeQuietly( this.fastload );
    }
  }

  /**
   * {@inheritDoc}
   *
   * @see org.pentaho.di.trans.step.BaseStep#dispose(org.pentaho.di.trans.step.StepMetaInterface,
   *      org.pentaho.di.trans.step.StepDataInterface)
   */
  @Override
  public void dispose( final StepMetaInterface smi, final StepDataInterface sdi ) {
    this.meta = (TeraFastMeta) smi;
    // this.data = (GenericStepData) sdi;

    try {
      if ( this.fastload != null ) {
        IOUtils.write( new FastloadControlBuilder().endLoading().toString(), this.fastload );
      }
    } catch ( IOException e ) {
      logError( "Unexpected error encountered while issuing END LOADING", e );
    }
    IOUtils.closeQuietly( this.dataFile );
    IOUtils.closeQuietly( this.fastload );
    try {
      if ( this.process != null ) {
        int exitValue = this.process.waitFor();
        logDetailed( "Exit value for the fastload process was : " + exitValue );
        if ( exitValue != 0 ) {
          logError( "Exit value for the fastload process was : " + exitValue );
          setErrors( DEFAULT_ERROR_CODE );
        }
      }
    } catch ( InterruptedException e ) {
      setErrors( DEFAULT_ERROR_CODE );
      logError( "Unexpected error encountered while finishing the fastload process", e );
    }

    super.dispose( smi, sdi );
  }

  /**
   * @param fileName
   *          the filename to resolve. may contain Kettle Environment variables.
   * @return the data file name.
   * @throws IOException
   *           ...
   */
  private String resolveFileName( final String fileName ) throws KettleException {
    final FileObject fileObject = KettleVFS.getFileObject( environmentSubstitute( fileName ) );
    return KettleVFS.getFilename( fileObject );
  }
}
