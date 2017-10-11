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

package org.pentaho.di.trans.steps.sqlfileoutput;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/*
 * Created on 26-may-2007
 *
 */

public class SQLFileOutputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = SQLFileOutputMeta.class; // for i18n purposes, needed by Translator2!!

  private DatabaseMeta databaseMeta;
  private String schemaName;
  private String tablename;
  private boolean truncateTable;

  private boolean AddToResult;

  private boolean createTable;

  /** The base name of the output file */
  private String fileName;

  /** The file extention in case of a generated filename */
  private String extension;

  /** if this value is larger then 0, the text file is split up into parts of this number of lines */
  private int splitEvery;

  /** Flag to indicate the we want to append to the end of an existing file (if it exists) */
  private boolean fileAppended;

  /** Flag: add the stepnr in the filename */
  private boolean stepNrInFilename;

  /** Flag: add the partition number in the filename */
  private boolean partNrInFilename;

  /** Flag: add the date in the filename */
  private boolean dateInFilename;

  /** Flag: add the time in the filename */
  private boolean timeInFilename;

  /** The encoding to use for reading: null or empty string means system default encoding */
  private String encoding;

  /** The date format */
  private String dateformat;

  /** Start New line for each statement */
  private boolean StartNewLine;

  /** Flag: create parent folder if needed */
  private boolean createparentfolder;

  private boolean DoNotOpenNewFileInit;

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public Object clone() {

    SQLFileOutputMeta retval = (SQLFileOutputMeta) super.clone();

    return retval;
  }

  /**
   * @return Returns the database.
   */
  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * @param database
   *          The database to set.
   */
  public void setDatabaseMeta( DatabaseMeta database ) {
    this.databaseMeta = database;
  }

  /**
   * @return Returns the extension.
   */
  public String getExtension() {
    return extension;
  }

  /**
   * @param extension
   *          The extension to set.
   */
  public void setExtension( String extension ) {
    this.extension = extension;
  }

  /**
   * @return Returns the fileAppended.
   */
  public boolean isFileAppended() {
    return fileAppended;
  }

  /**
   * @param fileAppended
   *          The fileAppended to set.
   */
  public void setFileAppended( boolean fileAppended ) {
    this.fileAppended = fileAppended;
  }

  /**
   * @return Returns the fileName.
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * @return Returns the splitEvery.
   */
  public int getSplitEvery() {
    return splitEvery;
  }

  /**
   * @param splitEvery
   *          The splitEvery to set.
   */
  public void setSplitEvery( int splitEvery ) {
    this.splitEvery = splitEvery;
  }

  /**
   * @return Returns the stepNrInFilename.
   */
  public boolean isStepNrInFilename() {
    return stepNrInFilename;
  }

  /**
   * @param stepNrInFilename
   *          The stepNrInFilename to set.
   */
  public void setStepNrInFilename( boolean stepNrInFilename ) {
    this.stepNrInFilename = stepNrInFilename;
  }

  /**
   * @return Returns the timeInFilename.
   */
  public boolean isTimeInFilename() {
    return timeInFilename;
  }

  /**
   * @return Returns the dateInFilename.
   */
  public boolean isDateInFilename() {
    return dateInFilename;
  }

  /**
   * @param dateInFilename
   *          The dateInFilename to set.
   */
  public void setDateInFilename( boolean dateInFilename ) {
    this.dateInFilename = dateInFilename;
  }

  /**
   * @param timeInFilename
   *          The timeInFilename to set.
   */
  public void setTimeInFilename( boolean timeInFilename ) {
    this.timeInFilename = timeInFilename;
  }

  /**
   * @param fileName
   *          The fileName to set.
   */
  public void setFileName( String fileName ) {
    this.fileName = fileName;
  }

  /**
   * @return The desired encoding of output file, null or empty if the default system encoding needs to be used.
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * @return The desired date format.
   */
  public String getDateFormat() {
    return dateformat;
  }

  /**
   * @param encoding
   *          The desired encoding of output file, null or empty if the default system encoding needs to be used.
   */
  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

  /**
   * @param dateFormat
   *          The desired date format of output field date used.
   */
  public void setDateFormat( String dateFormat ) {
    this.dateformat = dateFormat;
  }

  /**
   * @return Returns the table name.
   */
  public String getTablename() {
    return tablename;
  }

  /**
   * @param tablename
   *          The table name to set.
   */
  public void setTablename( String tablename ) {
    this.tablename = tablename;
  }

  /**
   * @return Returns the truncate table flag.
   */
  public boolean truncateTable() {
    return truncateTable;
  }

  /**
   * @return Returns the Add to result filesname flag.
   */
  public boolean AddToResult() {
    return AddToResult;
  }

  /**
   * @return Returns the Start new line flag.
   */
  public boolean StartNewLine() {
    return StartNewLine;
  }

  public boolean isDoNotOpenNewFileInit() {
    return DoNotOpenNewFileInit;
  }

  public void setDoNotOpenNewFileInit( boolean DoNotOpenNewFileInit ) {
    this.DoNotOpenNewFileInit = DoNotOpenNewFileInit;
  }

  /**
   * @return Returns the create table flag.
   */
  public boolean createTable() {
    return createTable;
  }

  /**
   * @param truncateTable
   *          The truncate table flag to set.
   */
  public void setTruncateTable( boolean truncateTable ) {
    this.truncateTable = truncateTable;
  }

  /**
   * @param AddToResult
   *          The Add file to result to set.
   */
  public void setAddToResult( boolean AddToResult ) {
    this.AddToResult = AddToResult;
  }

  /**
   * @param StartNewLine
   *          The Start NEw Line to set.
   */
  public void setStartNewLine( boolean StartNewLine ) {
    this.StartNewLine = StartNewLine;
  }

  /**
   * @param createTable
   *          The create table flag to set.
   */
  public void setCreateTable( boolean createTable ) {
    this.createTable = createTable;
  }

  /**
   * @return Returns the create parent folder flag.
   */
  public boolean isCreateParentFolder() {
    return createparentfolder;
  }

  /**
   * @param createparentfolder
   *          The create parent folder flag to set.
   */
  public void setCreateParentFolder( boolean createparentfolder ) {
    this.createparentfolder = createparentfolder;
  }

  public String[] getFiles( String fileName ) {
    int copies = 1;
    int splits = 1;
    int parts = 1;

    if ( stepNrInFilename ) {
      copies = 3;
    }

    if ( partNrInFilename ) {
      parts = 3;
    }

    if ( splitEvery != 0 ) {
      splits = 3;
    }

    int nr = copies * parts * splits;
    if ( nr > 1 ) {
      nr++;
    }

    String[] retval = new String[nr];

    int i = 0;
    for ( int copy = 0; copy < copies; copy++ ) {
      for ( int part = 0; part < parts; part++ ) {
        for ( int split = 0; split < splits; split++ ) {
          retval[i] = buildFilename( fileName, copy, split );
          i++;
        }
      }
    }
    if ( i < nr ) {
      retval[i] = "...";
    }

    return retval;
  }

  public String buildFilename( String fileName, int stepnr, int splitnr ) {
    SimpleDateFormat daf = new SimpleDateFormat();

    // Replace possible environment variables...
    String retval = fileName;

    Date now = new Date();

    if ( dateInFilename ) {
      daf.applyPattern( "yyyMMdd" );
      String d = daf.format( now );
      retval += "_" + d;
    }
    if ( timeInFilename ) {
      daf.applyPattern( "HHmmss" );
      String t = daf.format( now );
      retval += "_" + t;
    }
    if ( stepNrInFilename ) {
      retval += "_" + stepnr;
    }

    if ( splitEvery > 0 ) {
      retval += "_" + splitnr;
    }

    if ( extension != null && extension.length() != 0 ) {
      retval += "." + extension;
    }

    return retval;
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {

      String con = XMLHandler.getTagValue( stepnode, "connection" );
      databaseMeta = DatabaseMeta.findDatabase( databases, con );
      schemaName = XMLHandler.getTagValue( stepnode, "schema" );
      tablename = XMLHandler.getTagValue( stepnode, "table" );
      truncateTable = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "truncate" ) );
      createTable = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "create" ) );
      encoding = XMLHandler.getTagValue( stepnode, "encoding" );
      dateformat = XMLHandler.getTagValue( stepnode, "dateformat" );
      AddToResult = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "AddToResult" ) );

      StartNewLine = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "StartNewLine" ) );

      fileName = XMLHandler.getTagValue( stepnode, "file", "name" );
      createparentfolder =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "create_parent_folder" ) );
      extension = XMLHandler.getTagValue( stepnode, "file", "extention" );
      fileAppended = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "append" ) );
      stepNrInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "split" ) );
      partNrInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "haspartno" ) );
      dateInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "add_date" ) );
      timeInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "add_time" ) );
      splitEvery = Const.toInt( XMLHandler.getTagValue( stepnode, "file", "splitevery" ), 0 );
      DoNotOpenNewFileInit =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "DoNotOpenNewFileInit" ) );

    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void setDefault() {
    databaseMeta = null;
    tablename = "";
    createparentfolder = false;
    DoNotOpenNewFileInit = false;

  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    "
      + XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " + XMLHandler.addTagValue( "schema", schemaName ) );
    retval.append( "    " + XMLHandler.addTagValue( "table", tablename ) );
    retval.append( "    " + XMLHandler.addTagValue( "truncate", truncateTable ) );
    retval.append( "    " + XMLHandler.addTagValue( "create", createTable ) );
    retval.append( "    " + XMLHandler.addTagValue( "encoding", encoding ) );
    retval.append( "    " + XMLHandler.addTagValue( "dateformat", dateformat ) );
    retval.append( "    " + XMLHandler.addTagValue( "addtoresult", AddToResult ) );

    retval.append( "    " + XMLHandler.addTagValue( "startnewline", StartNewLine ) );

    retval.append( "    <file>" + Const.CR );
    retval.append( "      " + XMLHandler.addTagValue( "name", fileName ) );
    retval.append( "      " + XMLHandler.addTagValue( "extention", extension ) );
    retval.append( "      " + XMLHandler.addTagValue( "append", fileAppended ) );
    retval.append( "      " + XMLHandler.addTagValue( "split", stepNrInFilename ) );
    retval.append( "      " + XMLHandler.addTagValue( "haspartno", partNrInFilename ) );
    retval.append( "      " + XMLHandler.addTagValue( "add_date", dateInFilename ) );
    retval.append( "      " + XMLHandler.addTagValue( "add_time", timeInFilename ) );
    retval.append( "      " + XMLHandler.addTagValue( "splitevery", splitEvery ) );
    retval.append( "      " + XMLHandler.addTagValue( "create_parent_folder", createparentfolder ) );
    retval.append( "      " + XMLHandler.addTagValue( "DoNotOpenNewFileInit", DoNotOpenNewFileInit ) );

    retval.append( "      </file>" + Const.CR );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );
      schemaName = rep.getStepAttributeString( id_step, "schema" );
      tablename = rep.getStepAttributeString( id_step, "table" );
      truncateTable = rep.getStepAttributeBoolean( id_step, "truncate" );
      createTable = rep.getStepAttributeBoolean( id_step, "create" );
      encoding = rep.getStepAttributeString( id_step, "encoding" );
      dateformat = rep.getStepAttributeString( id_step, "dateformat" );
      AddToResult = rep.getStepAttributeBoolean( id_step, "addtoresult" );
      StartNewLine = rep.getStepAttributeBoolean( id_step, "startnewline" );

      fileName = rep.getStepAttributeString( id_step, "file_name" );
      extension = rep.getStepAttributeString( id_step, "file_extention" );
      fileAppended = rep.getStepAttributeBoolean( id_step, "file_append" );
      splitEvery = (int) rep.getStepAttributeInteger( id_step, "file_split" );
      stepNrInFilename = rep.getStepAttributeBoolean( id_step, "file_add_stepnr" );
      partNrInFilename = rep.getStepAttributeBoolean( id_step, "file_add_partnr" );
      dateInFilename = rep.getStepAttributeBoolean( id_step, "file_add_date" );
      timeInFilename = rep.getStepAttributeBoolean( id_step, "file_add_time" );
      createparentfolder = rep.getStepAttributeBoolean( id_step, "create_parent_folder" );
      DoNotOpenNewFileInit = rep.getStepAttributeBoolean( id_step, "DoNotOpenNewFileInit" );

    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", databaseMeta );
      rep.saveStepAttribute( id_transformation, id_step, "schema", schemaName );
      rep.saveStepAttribute( id_transformation, id_step, "table", tablename );
      rep.saveStepAttribute( id_transformation, id_step, "truncate", truncateTable );
      rep.saveStepAttribute( id_transformation, id_step, "create", createTable );
      rep.saveStepAttribute( id_transformation, id_step, "encoding", encoding );
      rep.saveStepAttribute( id_transformation, id_step, "dateformat", dateformat );
      rep.saveStepAttribute( id_transformation, id_step, "addtoresult", AddToResult );
      rep.saveStepAttribute( id_transformation, id_step, "startnewline", StartNewLine );

      rep.saveStepAttribute( id_transformation, id_step, "file_name", fileName );
      rep.saveStepAttribute( id_transformation, id_step, "file_extention", extension );
      rep.saveStepAttribute( id_transformation, id_step, "file_append", fileAppended );
      rep.saveStepAttribute( id_transformation, id_step, "file_split", splitEvery );
      rep.saveStepAttribute( id_transformation, id_step, "file_add_stepnr", stepNrInFilename );
      rep.saveStepAttribute( id_transformation, id_step, "file_add_partnr", partNrInFilename );
      rep.saveStepAttribute( id_transformation, id_step, "file_add_date", dateInFilename );
      rep.saveStepAttribute( id_transformation, id_step, "file_add_time", timeInFilename );
      rep.saveStepAttribute( id_transformation, id_step, "create_parent_folder", createparentfolder );
      rep.saveStepAttribute( id_transformation, id_step, "DoNotOpenNewFileInit", DoNotOpenNewFileInit );

      // Also, save the step-database relationship!
      if ( databaseMeta != null ) {
        rep.insertStepDatabase( id_transformation, id_step, databaseMeta.getObjectId() );
      }

    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    if ( databaseMeta != null ) {
      CheckResult cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SQLFileOutputMeta.CheckResult.ConnectionExists" ), stepMeta );
      remarks.add( cr );

      Database db = new Database( loggingObject, databaseMeta );
      try {
        db.connect();

        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "SQLFileOutputMeta.CheckResult.ConnectionOk" ), stepMeta );
        remarks.add( cr );

        if ( !Utils.isEmpty( tablename ) ) {
          String schemaTable = databaseMeta.getQuotedSchemaTableCombination( schemaName, tablename );
          // Check if this table exists...
          if ( db.checkTableExists( schemaTable ) ) {
            cr =
              new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
                PKG, "SQLFileOutputMeta.CheckResult.TableAccessible", schemaTable ), stepMeta );
            remarks.add( cr );

            RowMetaInterface r = db.getTableFields( schemaTable );
            if ( r != null ) {
              cr =
                new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
                  PKG, "SQLFileOutputMeta.CheckResult.TableOk", schemaTable ), stepMeta );
              remarks.add( cr );

              String error_message = "";
              boolean error_found = false;
              // OK, we have the table fields.
              // Now see what we can find as previous step...
              if ( prev != null && prev.size() > 0 ) {
                cr =
                  new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
                    PKG, "SQLFileOutputMeta.CheckResult.FieldsReceived", "" + prev.size() ), stepMeta );
                remarks.add( cr );

                // Starting from prev...
                for ( int i = 0; i < prev.size(); i++ ) {
                  ValueMetaInterface pv = prev.getValueMeta( i );
                  int idx = r.indexOfValue( pv.getName() );
                  if ( idx < 0 ) {
                    error_message += "\t\t" + pv.getName() + " (" + pv.getTypeDesc() + ")" + Const.CR;
                    error_found = true;
                  }
                }
                if ( error_found ) {
                  error_message =
                    BaseMessages.getString(
                      PKG, "SQLFileOutputMeta.CheckResult.FieldsNotFoundInOutput", error_message );

                  cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
                  remarks.add( cr );
                } else {
                  cr =
                    new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
                      PKG, "SQLFileOutputMeta.CheckResult.AllFieldsFoundInOutput" ), stepMeta );
                  remarks.add( cr );
                }

                // Starting from table fields in r...
                for ( int i = 0; i < r.size(); i++ ) {
                  ValueMetaInterface rv = r.getValueMeta( i );
                  int idx = prev.indexOfValue( rv.getName() );
                  if ( idx < 0 ) {
                    error_message += "\t\t" + rv.getName() + " (" + rv.getTypeDesc() + ")" + Const.CR;
                    error_found = true;
                  }
                }
                if ( error_found ) {
                  error_message =
                    BaseMessages.getString( PKG, "SQLFileOutputMeta.CheckResult.FieldsNotFound", error_message );

                  cr = new CheckResult( CheckResult.TYPE_RESULT_WARNING, error_message, stepMeta );
                  remarks.add( cr );
                } else {
                  cr =
                    new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
                      PKG, "SQLFileOutputMeta.CheckResult.AllFieldsFound" ), stepMeta );
                  remarks.add( cr );
                }
              } else {
                cr =
                  new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
                    PKG, "SQLFileOutputMeta.CheckResult.NoFields" ), stepMeta );
                remarks.add( cr );
              }
            } else {
              cr =
                new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
                  PKG, "SQLFileOutputMeta.CheckResult.TableNotAccessible" ), stepMeta );
              remarks.add( cr );
            }
          } else {
            cr =
              new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
                PKG, "SQLFileOutputMeta.CheckResult.TableError", schemaTable ), stepMeta );
            remarks.add( cr );
          }
        } else {
          cr =
            new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
              PKG, "SQLFileOutputMeta.CheckResult.NoTableName" ), stepMeta );
          remarks.add( cr );
        }
      } catch ( KettleException e ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "SQLFileOutputMeta.CheckResult.UndefinedError", e.getMessage() ), stepMeta );
        remarks.add( cr );
      } finally {
        db.disconnect();
      }
    } else {
      CheckResult cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SQLFileOutputMeta.CheckResult.NoConnection" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      CheckResult cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SQLFileOutputMeta.CheckResult.ExpectedInputOk" ), stepMeta );
      remarks.add( cr );
    } else {
      CheckResult cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SQLFileOutputMeta.CheckResult.ExpectedInputError" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new SQLFileOutput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new SQLFileOutputData();
  }

  public void analyseImpact( List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, Repository repository,
    IMetaStore metaStore ) {
    if ( truncateTable ) {
      DatabaseImpact ii =
        new DatabaseImpact(
          DatabaseImpact.TYPE_IMPACT_TRUNCATE, transMeta.getName(), stepMeta.getName(), databaseMeta
            .getDatabaseName(), tablename, "", "", "", "", "Truncate of table" );
      impact.add( ii );

    }
    // The values that are entering this step are in "prev":
    if ( prev != null ) {
      for ( int i = 0; i < prev.size(); i++ ) {
        ValueMetaInterface v = prev.getValueMeta( i );
        DatabaseImpact ii =
          new DatabaseImpact(
            DatabaseImpact.TYPE_IMPACT_WRITE, transMeta.getName(), stepMeta.getName(), databaseMeta
              .getDatabaseName(), tablename, v.getName(), v.getName(), v != null ? v.getOrigin() : "?", "",
            "Type = " + v.toStringMeta() );
        impact.add( ii );
      }
    }
  }

  public SQLStatement getSQLStatements( TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
    Repository repository, IMetaStore metaStore ) {
    SQLStatement retval = new SQLStatement( stepMeta.getName(), databaseMeta, null ); // default: nothing to do!

    if ( databaseMeta != null ) {
      if ( prev != null && prev.size() > 0 ) {
        if ( !Utils.isEmpty( tablename ) ) {
          Database db = new Database( loggingObject, databaseMeta );
          db.shareVariablesWith( transMeta );
          try {
            db.connect();

            String schemaTable = databaseMeta.getQuotedSchemaTableCombination( schemaName, tablename );
            String cr_table = db.getDDL( schemaTable, prev );

            // Empty string means: nothing to do: set it to null...
            if ( cr_table == null || cr_table.length() == 0 ) {
              cr_table = null;
            }

            retval.setSQL( cr_table );
          } catch ( KettleDatabaseException dbe ) {
            retval.setError( BaseMessages.getString( PKG, "SQLFileOutputMeta.Error.ErrorConnecting", dbe
              .getMessage() ) );
          } finally {
            db.disconnect();
          }
        } else {
          retval.setError( BaseMessages.getString( PKG, "SQLFileOutputMeta.Exception.TableNotSpecified" ) );
        }
      } else {
        retval.setError( BaseMessages.getString( PKG, "SQLFileOutputMeta.Error.NoInput" ) );
      }
    } else {
      retval.setError( BaseMessages.getString( PKG, "SQLFileOutputMeta.Error.NoConnection" ) );
    }

    return retval;
  }

  public RowMetaInterface getRequiredFields( VariableSpace space ) throws KettleException {
    String realTableName = space.environmentSubstitute( tablename );
    String realSchemaName = space.environmentSubstitute( schemaName );

    if ( databaseMeta != null ) {
      Database db = new Database( loggingObject, databaseMeta );
      try {
        db.connect();

        if ( !Utils.isEmpty( realTableName ) ) {
          String schemaTable = databaseMeta.getQuotedSchemaTableCombination( realSchemaName, realTableName );

          // Check if this table exists...
          if ( db.checkTableExists( schemaTable ) ) {
            return db.getTableFields( schemaTable );
          } else {
            throw new KettleException( BaseMessages.getString( PKG, "SQLFileOutputMeta.Exception.TableNotFound" ) );
          }
        } else {
          throw new KettleException( BaseMessages.getString( PKG, "SQLFileOutputMeta.Exception.TableNotSpecified" ) );
        }
      } catch ( Exception e ) {
        throw new KettleException(
          BaseMessages.getString( PKG, "SQLFileOutputMeta.Exception.ErrorGettingFields" ), e );
      } finally {
        db.disconnect();
      }
    } else {
      throw new KettleException( BaseMessages.getString( PKG, "SQLFileOutputMeta.Exception.ConnectionNotDefined" ) );
    }
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    if ( databaseMeta != null ) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  /**
   * @return the schemaName
   */
  public String getSchemaName() {
    return schemaName;
  }

  /**
   * @param schemaName
   *          the schemaName to set
   */
  public void setSchemaName( String schemaName ) {
    this.schemaName = schemaName;
  }

  public boolean supportsErrorHandling() {
    return true;
  }

  /**
   * Since the exported transformation that runs this will reside in a ZIP file, we can't reference files relatively. So
   * what this does is turn the name of files into absolute paths OR it simply includes the resource in the ZIP file.
   * For now, we'll simply turn it into an absolute path and pray that the file is on a shared drive or something like
   * that.
   *
   * @param space
   *          the variable space to use
   * @param definitions
   * @param resourceNamingInterface
   * @param repository
   *          The repository to optionally load other resources from (to be converted to XML)
   * @param metaStore
   *          the metaStore in which non-kettle metadata could reside.
   *
   * @return the filename of the exported resource
   */
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
    ResourceNamingInterface resourceNamingInterface, Repository repository, IMetaStore metaStore ) throws KettleException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the filename from relative to absolute by grabbing the file object...
      //
      // From : ${Internal.Transformation.Filename.Directory}/../foo/bar.data
      // To : /home/matt/test/files/foo/bar.data
      //
      FileObject fileObject = KettleVFS.getFileObject( space.environmentSubstitute( fileName ), space );

      // If the file doesn't exist, forget about this effort too!
      //
      if ( fileObject.exists() ) {
        // Convert to an absolute path...
        //
        fileName = resourceNamingInterface.nameResource( fileObject, space, true );

        return fileName;
      }
      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

}
