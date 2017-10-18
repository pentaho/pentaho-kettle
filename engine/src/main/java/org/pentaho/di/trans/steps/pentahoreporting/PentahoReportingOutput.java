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

package org.pentaho.di.trans.steps.pentahoreporting;

import java.awt.GraphicsEnvironment;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.pentahoreporting.PentahoReportingOutputMeta.ProcessorType;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.gui.common.StatusType;
import org.pentaho.reporting.engine.classic.core.modules.gui.csv.CSVTableExportTask;
import org.pentaho.reporting.engine.classic.core.modules.gui.html.HtmlStreamExportTask;
import org.pentaho.reporting.engine.classic.core.modules.gui.pdf.PdfExportTask;
import org.pentaho.reporting.engine.classic.core.modules.gui.rtf.RTFExportTask;
import org.pentaho.reporting.engine.classic.core.modules.gui.xls.ExcelExportTask;
import org.pentaho.reporting.engine.classic.core.modules.gui.xls.XSSFExcelExportTask;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlReportUtil;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterDefinitionEntry;
import org.pentaho.reporting.engine.classic.core.parameters.ReportParameterDefinition;
import org.pentaho.reporting.engine.classic.core.util.ReportParameterValues;
import org.pentaho.reporting.libraries.base.config.ModifiableConfiguration;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;
import org.pentaho.reporting.libraries.fonts.LibFontBoot;
import org.pentaho.reporting.libraries.resourceloader.LibLoaderBoot;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * Outputs a stream/series of rows to a file, effectively building a sort of (compressed) microcube.
 *
 * @author Matt
 * @since 4-apr-2003
 */

public class PentahoReportingOutput extends BaseStep implements StepInterface {
  private static Class<?> PKG = PentahoReportingOutput.class; // for i18n purposes, needed by Translator2!!

  private PentahoReportingOutputMeta meta;
  private PentahoReportingOutputData data;

  public PentahoReportingOutput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
    TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );

    // To prevent CGLGraphicsConfig.getConfig() hang forever on mac
    if ( Const.isOSX() ) {
      GraphicsEnvironment.getLocalGraphicsEnvironment();
    }
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (PentahoReportingOutputMeta) smi;
    data = (PentahoReportingOutputData) sdi;

    boolean result = true;

    // For every row we read, we execute a report
    //
    Object[] r = getRow();

    // All done, signal this to the next steps...
    //
    if ( r == null ) {
      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      data.inputFieldIndex = getInputRowMeta().indexOfValue( meta.getInputFileField() );
      if ( data.inputFieldIndex < 0 ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "PentahoReportingOutput.Exception.CanNotFindField", meta.getInputFileField() ) );
      }
      data.outputFieldIndex = getInputRowMeta().indexOfValue( meta.getOutputFileField() );
      if ( data.inputFieldIndex < 0 ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "PentahoReportingOutput.Exception.CanNotFindField", meta.getOutputFileField() ) );
      }

      performPentahoReportingBoot( log, getClass() );
    }

    String sourceFilename = getInputRowMeta().getString( r, data.inputFieldIndex );
    String targetFilename = getInputRowMeta().getString( r, data.outputFieldIndex );
    processReport( r, sourceFilename, targetFilename, meta.getOutputProcessorType(), meta.getCreateParentfolder() );

    // in case we want the input data to go to more steps.
    //
    putRow( getInputRowMeta(), r );

    if ( checkFeedback( getLinesOutput() ) ) {
      logBasic( BaseMessages.getString( PKG, "PentahoReportingOutput.Log.LineNumber" ) + getLinesOutput() );
    }

    return result;
  }

  public static void performPentahoReportingBoot( LogChannelInterface log, Class<?> referenceClass ) {
    // Boot the Pentaho reporting engine!
    //
    if ( ClassicEngineBoot.getInstance().isBootDone() == false ) {

      ObjectUtilities.setClassLoader( referenceClass.getClassLoader() );
      ObjectUtilities.setClassLoaderSource( ObjectUtilities.CLASS_CONTEXT );

      LibLoaderBoot.getInstance().start();
      LibFontBoot.getInstance().start();
      ClassicEngineBoot.getInstance().start();

      Exception exception = ClassicEngineBoot.getInstance().getBootFailureReason();
      if ( exception != null ) {
        log.logError( "Error booting the Pentaho reporting engine", exception );
      }

    }
  }

  public static MasterReport loadMasterReport( String sourceFilename ) throws Exception {
    ResourceManager manager = new ResourceManager();
    manager.registerDefaults();
    FileObject fileObject = KettleVFS.getFileObject( sourceFilename );
    URL url = new URL( fileObject.getName().getURI() );
    Resource resource = manager.createDirectly( url, MasterReport.class );
    MasterReport report = (MasterReport) resource.getResource();

    return report;
  }

  private void processReport( Object[] r, String sourceFilename, String targetFilename,
    ProcessorType outputProcessorType, Boolean createParentFolder ) throws KettleException {
    try {

      // Load the master report from the PRPT
      //
      MasterReport report = loadMasterReport( sourceFilename );

      // Set the parameters values that are present in the various fields...
      //
      ReportParameterValues values = report.getParameterValues();
      ReportParameterDefinition definition = report.getParameterDefinition();

      for ( String parameterName : meta.getParameterFieldMap().keySet() ) {
        String fieldName = meta.getParameterFieldMap().get( parameterName );
        if ( fieldName != null ) {
          int index = getInputRowMeta().indexOfValue( fieldName );
          if ( index < 0 ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "PentahoReportingOutput.Exception.CanNotFindField", fieldName ) );
          }

          Class<?> clazz = findParameterClass( definition, parameterName );
          Object value = null;
          if ( clazz != null ) {
            if ( clazz.equals( String.class ) ) {
              value = getInputRowMeta().getString( r, index );
            } else if ( clazz.equals( ( new String[0] ).getClass() ) ) {
              value = getInputRowMeta().getString( r, index ).split( "\t" );
            } else if ( clazz.equals( Date.class ) ) {
              value = getInputRowMeta().getDate( r, index );
            } else if ( clazz.equals( byte.class ) || clazz.equals( Byte.class ) ) {
              value = getInputRowMeta().getInteger( r, index ).byteValue();
            } else if ( clazz.equals( Short.class ) || clazz.equals( short.class ) ) {
              value = getInputRowMeta().getInteger( r, index ).shortValue();
            } else if ( clazz.equals( Integer.class ) || clazz.equals( int.class ) ) {
              value = getInputRowMeta().getInteger( r, index ).intValue();
            } else if ( clazz.equals( Long.class ) || clazz.equals( long.class ) ) {
              value = getInputRowMeta().getInteger( r, index );
            } else if ( clazz.equals( Double.class ) || clazz.equals( double.class ) ) {
              value = getInputRowMeta().getNumber( r, index );
            } else if ( clazz.equals( Float.class ) || clazz.equals( float.class ) ) {
              value = getInputRowMeta().getNumber( r, index ).floatValue();
            } else if ( clazz.equals( Number.class ) ) {
              value = getInputRowMeta().getBigNumber( r, index ).floatValue();
            } else if ( clazz.equals( Boolean.class ) || clazz.equals( boolean.class ) ) {
              value = getInputRowMeta().getBoolean( r, index );
            } else if ( clazz.equals( BigDecimal.class ) ) {
              value = getInputRowMeta().getBigNumber( r, index );
            } else if ( clazz.equals( ( new byte[0] ).getClass() ) ) {
              value = getInputRowMeta().getBinary( r, index );
            } else {
              value = getInputRowMeta().getValueMeta( index ).convertToNormalStorageType( r[index] );
            }

            values.put( parameterName, value );

          } else {
            // This parameter was not found, log this as a warning...
            //
            logBasic( BaseMessages.getString(
              PKG, "PentahoReportingOutput.Log.ParameterNotFoundInReport", parameterName, sourceFilename ) );
          }
        }
      }

      ModifiableConfiguration modifiableConfiguration = (ModifiableConfiguration) report.getConfiguration();
      String property;
      Runnable exportTask;
      PentahoReportingSwingGuiContext context = new PentahoReportingSwingGuiContext();

      switch ( outputProcessorType ) {
        case PDF:
          property = "org.pentaho.reporting.engine.classic.core.modules.gui.pdf.TargetFileName";
          modifiableConfiguration.setConfigProperty( property, targetFilename );
          property = "org.pentaho.reporting.engine.classic.core.modules.gui.pdf.CreateParentFolder";
          modifiableConfiguration.setConfigProperty( property, createParentFolder.toString() );
          exportTask = new PdfExportTask( report, null, context );
          break;
        case CSV:
          property = "org.pentaho.reporting.engine.classic.core.modules.gui.csv.FileName";
          modifiableConfiguration.setConfigProperty( property, targetFilename );
          property = "org.pentaho.reporting.engine.classic.core.modules.gui.csv.CreateParentFolder";
          modifiableConfiguration.setConfigProperty( property, createParentFolder.toString() );
          exportTask = new CSVTableExportTask( report, null, context );
          break;
        case Excel:
          property = "org.pentaho.reporting.engine.classic.core.modules.gui.xls.FileName";
          modifiableConfiguration.setConfigProperty( property, targetFilename );
          property = "org.pentaho.reporting.engine.classic.core.modules.gui.xls.CreateParentFolder";
          modifiableConfiguration.setConfigProperty( property, createParentFolder.toString() );
          exportTask = new ExcelExportTask( report, null, context );
          break;
        case Excel_2007:
          property = "org.pentaho.reporting.engine.classic.core.modules.gui.xls.FileName";
          modifiableConfiguration.setConfigProperty( property, targetFilename );
          property = "org.pentaho.reporting.engine.classic.core.modules.gui.xls.CreateParentFolder";
          modifiableConfiguration.setConfigProperty( property, createParentFolder.toString() );
          exportTask = new XSSFExcelExportTask( report, null, context );
          break;
        case StreamingHTML:
          property = "org.pentaho.reporting.engine.classic.core.modules.gui.html.stream.TargetFileName";
          modifiableConfiguration.setConfigProperty( property, targetFilename );
          property = "org.pentaho.reporting.engine.classic.core.modules.gui.html.stream.CreateParentFolder";
          modifiableConfiguration.setConfigProperty( property, createParentFolder.toString() );
          exportTask = new HtmlStreamExportTask( report, null, context );
          break;
        case PagedHTML:
          property = "org.pentaho.reporting.engine.classic.core.modules.gui.html.paged.CreateParentFolder";
          modifiableConfiguration.setConfigProperty( property, createParentFolder.toString() );
          HtmlReportUtil.createDirectoryHTML( report, targetFilename );
          exportTask = null;
          break;

        case RTF:
          property = "org.pentaho.reporting.engine.classic.core.modules.gui.rtf.FileName";
          modifiableConfiguration.setConfigProperty( property, targetFilename );
          property = "org.pentaho.reporting.engine.classic.core.modules.gui.rtf.CreateParentFolder";
          modifiableConfiguration.setConfigProperty( property, createParentFolder.toString() );
          exportTask = new RTFExportTask( report, null, context );
          break;
        default:
          exportTask = null;
          break;
      }

      if ( exportTask != null ) {
        exportTask.run();
      }

      if ( context.getStatusType() == StatusType.ERROR ) {
        KettleVFS.getFileObject( targetFilename, getTransMeta() ).delete();
        if ( context.getCause() != null ) {
          throw context.getCause();
        }
        throw new KettleStepException( context.getMessage() );
      }

      ResultFile resultFile =
        new ResultFile(
          ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject( targetFilename, getTransMeta() ),
          getTransMeta().getName(), getStepname() );
      resultFile.setComment( "This file was created with a Pentaho Reporting Output step" );
      addResultFile( resultFile );

    } catch ( Throwable e ) {

      throw new KettleException( BaseMessages.getString(
        PKG, "PentahoReportingOutput.Exception.UnexpectedErrorRenderingReport", sourceFilename, targetFilename,
        outputProcessorType.getDescription() ), e );
    }
  }

  private Class<?> findParameterClass( ReportParameterDefinition definition, String parameterName ) {
    for ( int i = 0; i < definition.getParameterCount(); i++ ) {
      ParameterDefinitionEntry entry = definition.getParameterDefinition( i );
      if ( parameterName.equals( entry.getName() ) ) {

        return entry.getValueType();
      }
    }
    return null;
  }

}
