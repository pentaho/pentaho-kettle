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

package org.pentaho.di.trans.steps.accessoutput;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

/*
 * Created on 2-jun-2003
 *
 */
public class AccessOutputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = AccessOutputMeta.class; // for i18n purposes, needed by Translator2!!

  private String filename;
  private boolean fileCreated;
  private String tablename;
  private boolean tableCreated;
  private boolean tableTruncated;
  private int commitSize;
  /** Flag: add the filenames to result filenames */
  private boolean addToResultFilenames;
  /** Flag : Do not open new file when transformation start */
  private boolean doNotOpeNnewFileInit;

  public AccessOutputMeta() {
    super(); // allocate BaseStepMeta
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public Object clone() {
    AccessOutputMeta retval = (AccessOutputMeta) super.clone();
    return retval;
  }

  /**
   * @return Returns the tablename.
   */
  public String getTablename() {
    return tablename;
  }

  /**
   * @param tablename
   *          The tablename to set.
   */
  public void setTablename( String tablename ) {
    this.tablename = tablename;
  }

  /**
   * @return Returns the truncate table flag.
   */
  public boolean truncateTable() {
    return tableTruncated;
  }

  /**
   * @param truncateTable
   *          The truncate table flag to set.
   */
  public void setTableTruncated( boolean truncateTable ) {
    this.tableTruncated = truncateTable;
  }

  private void readData( Node stepnode, List<DatabaseMeta> databases ) throws KettleXMLException {
    try {
      filename = XMLHandler.getTagValue( stepnode, "filename" );
      tablename = XMLHandler.getTagValue( stepnode, "table" );
      tableTruncated = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "truncate" ) );
      fileCreated = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "create_file" ) );
      tableCreated = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "create_table" ) );
      commitSize = Const.toInt( XMLHandler.getTagValue( stepnode, "commit_size" ), AccessOutput.COMMIT_SIZE );
      String addToResultFiles = XMLHandler.getTagValue( stepnode, "add_to_result_filenames" );
      if ( Utils.isEmpty( addToResultFiles ) ) {
        addToResultFilenames = true;
      } else {
        addToResultFilenames = "Y".equalsIgnoreCase( addToResultFiles );
      }

      doNotOpeNnewFileInit = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "do_not_open_newfile_init" ) );

    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void setDefault() {
    fileCreated = true;
    tableCreated = true;
    tableTruncated = false;
    commitSize = AccessOutput.COMMIT_SIZE;
    doNotOpeNnewFileInit = false;
    addToResultFilenames = true;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( "    " ).append( XMLHandler.addTagValue( "filename", filename ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "table", tablename ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "truncate", tableTruncated ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "create_file", fileCreated ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "create_table", tableCreated ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "commit_size", commitSize ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "add_to_result_filenames", addToResultFilenames ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "do_not_open_newfile_init", doNotOpeNnewFileInit ) );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      filename = rep.getStepAttributeString( id_step, "filename" );
      tablename = rep.getStepAttributeString( id_step, "table" );
      tableTruncated = rep.getStepAttributeBoolean( id_step, "truncate" );
      fileCreated = rep.getStepAttributeBoolean( id_step, "create_file" );
      tableCreated = rep.getStepAttributeBoolean( id_step, "create_table" );
      commitSize = (int) rep.getStepAttributeInteger( id_step, "commit_size" );
      String addToResultFiles = rep.getStepAttributeString( id_step, "add_to_result_filenames" );
      if ( Utils.isEmpty( addToResultFiles ) ) {
        addToResultFilenames = true;
      } else {
        addToResultFilenames = rep.getStepAttributeBoolean( id_step, "add_to_result_filenames" );
      }
      doNotOpeNnewFileInit = rep.getStepAttributeBoolean( id_step, "do_not_open_newfile_init" );

    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "filename", filename );
      rep.saveStepAttribute( id_transformation, id_step, "table", tablename );
      rep.saveStepAttribute( id_transformation, id_step, "truncate", tableTruncated );
      rep.saveStepAttribute( id_transformation, id_step, "create_file", fileCreated );
      rep.saveStepAttribute( id_transformation, id_step, "create_table", tableCreated );
      rep.saveStepAttribute( id_transformation, id_step, "commit_size", commitSize );
      rep.saveStepAttribute( id_transformation, id_step, "add_to_result_filenames", addToResultFilenames );
      rep.saveStepAttribute( id_transformation, id_step, "do_not_open_newfile_init", doNotOpeNnewFileInit );

    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

    // TODO: add file checking in case we don't create a table.

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      CheckResult cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "AccessOutputMeta.CheckResult.ExpectedInputOk" ), stepMeta );
      remarks.add( cr );
    } else {
      CheckResult cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "AccessOutputMeta.CheckResult.ExpectedInputError" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new AccessOutput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new AccessOutputData();
  }

  public RowMetaInterface getRequiredFields( VariableSpace space ) throws KettleException {
    String realFilename = space.environmentSubstitute( filename );
    File file = new File( realFilename );
    Database db = null;
    try {
      if ( !file.exists() || !file.isFile() ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "AccessOutputMeta.Exception.FileDoesNotExist", realFilename ) );
      }

      // open the database and get the table
      db = Database.open( file );
      String realTablename = space.environmentSubstitute( tablename );
      Table table = db.getTable( realTablename );
      if ( table == null ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "AccessOutputMeta.Exception.TableDoesNotExist", realTablename ) );
      }

      RowMetaInterface layout = getLayout( table );
      return layout;
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "AccessOutputMeta.Exception.ErrorGettingFields" ), e );
    } finally {
      try {
        if ( db != null ) {
          db.close();
        }
      } catch ( IOException e ) {
        throw new KettleException(
          BaseMessages.getString( PKG, "AccessOutputMeta.Exception.ErrorClosingDatabase" ), e );
      }
    }
  }

  public static final RowMetaInterface getLayout( Table table ) throws SQLException, KettleStepException {
    RowMetaInterface row = new RowMeta();
    List<Column> columns = table.getColumns();
    for ( int i = 0; i < columns.size(); i++ ) {
      Column column = columns.get( i );

      int valtype = ValueMetaInterface.TYPE_STRING;
      int length = -1;
      int precision = -1;

      int type = column.getType().getSQLType();
      switch ( type ) {
        case java.sql.Types.CHAR:
        case java.sql.Types.VARCHAR:
        case java.sql.Types.LONGVARCHAR: // Character Large Object
          valtype = ValueMetaInterface.TYPE_STRING;
          length = column.getLength();
          break;

        case java.sql.Types.CLOB:
          valtype = ValueMetaInterface.TYPE_STRING;
          length = DatabaseMeta.CLOB_LENGTH;
          break;

        case java.sql.Types.BIGINT:
          valtype = ValueMetaInterface.TYPE_INTEGER;
          precision = 0; // Max 9.223.372.036.854.775.807
          length = 15;
          break;

        case java.sql.Types.INTEGER:
          valtype = ValueMetaInterface.TYPE_INTEGER;
          precision = 0; // Max 2.147.483.647
          length = 9;
          break;

        case java.sql.Types.SMALLINT:
          valtype = ValueMetaInterface.TYPE_INTEGER;
          precision = 0; // Max 32.767
          length = 4;
          break;

        case java.sql.Types.TINYINT:
          valtype = ValueMetaInterface.TYPE_INTEGER;
          precision = 0; // Max 127
          length = 2;
          break;

        case java.sql.Types.DECIMAL:
        case java.sql.Types.DOUBLE:
        case java.sql.Types.FLOAT:
        case java.sql.Types.REAL:
        case java.sql.Types.NUMERIC:
          valtype = ValueMetaInterface.TYPE_NUMBER;
          length = column.getLength();
          precision = column.getPrecision();
          if ( length >= 126 ) {
            length = -1;
          }
          if ( precision >= 126 ) {
            precision = -1;
          }

          if ( type == java.sql.Types.DOUBLE || type == java.sql.Types.FLOAT || type == java.sql.Types.REAL ) {
            if ( precision == 0 ) {
              precision = -1; // precision is obviously incorrect if the type if Double/Float/Real
            }
          } else {
            if ( precision == 0 && length < 18 && length > 0 ) { // Among others Oracle is affected here.
              valtype = ValueMetaInterface.TYPE_INTEGER;
            }
          }
          if ( length > 18 || precision > 18 ) {
            valtype = ValueMetaInterface.TYPE_BIGNUMBER;
          }

          break;

        case java.sql.Types.DATE:
        case java.sql.Types.TIME:
        case java.sql.Types.TIMESTAMP:
          valtype = ValueMetaInterface.TYPE_DATE;
          break;

        case java.sql.Types.BOOLEAN:
        case java.sql.Types.BIT:
          valtype = ValueMetaInterface.TYPE_BOOLEAN;
          break;

        case java.sql.Types.BINARY:
        case java.sql.Types.BLOB:
        case java.sql.Types.VARBINARY:
        case java.sql.Types.LONGVARBINARY:
          valtype = ValueMetaInterface.TYPE_BINARY;
          break;

        default:
          valtype = ValueMetaInterface.TYPE_STRING;
          length = column.getLength();
          break;
      }

      ValueMetaInterface v;
      try {
        v = ValueMetaFactory.createValueMeta( column.getName(), valtype );
      } catch ( KettlePluginException e ) {
        throw new KettleStepException( e );
      }
      v.setLength( length, precision );
      row.addValueMeta( v );
    }

    return row;
  }

  public static final List<Column> getColumns( RowMetaInterface row ) {
    List<Column> list = new ArrayList<Column>();

    for ( int i = 0; i < row.size(); i++ ) {
      ValueMetaInterface value = row.getValueMeta( i );

      Column column = new Column();
      column.setName( value.getName() );

      int length = value.getLength();

      switch ( value.getType() ) {
        case ValueMetaInterface.TYPE_INTEGER:
          if ( length < 3 ) {
            column.setType( DataType.BYTE );
            length = DataType.BYTE.getFixedSize();
          } else {
            if ( length < 5 ) {
              column.setType( DataType.INT );
              length = DataType.INT.getFixedSize();
            } else {
              column.setType( DataType.LONG );
              length = DataType.LONG.getFixedSize();
            }
          }
          break;
        case ValueMetaInterface.TYPE_NUMBER:
          column.setType( DataType.DOUBLE );
          length = DataType.DOUBLE.getFixedSize();
          break;
        case ValueMetaInterface.TYPE_DATE:
          column.setType( DataType.SHORT_DATE_TIME );
          length = DataType.SHORT_DATE_TIME.getFixedSize();
          break;
        case ValueMetaInterface.TYPE_STRING:
          if ( length < 255 ) {
            column.setType( DataType.TEXT );
            length *= DataType.TEXT.getUnitSize();
          } else {
            column.setType( DataType.MEMO );
            length *= DataType.MEMO.getUnitSize();
          }
          break;
        case ValueMetaInterface.TYPE_BINARY:
          column.setType( DataType.BINARY );
          break;
        case ValueMetaInterface.TYPE_BOOLEAN:
          column.setType( DataType.BOOLEAN );
          length = DataType.BOOLEAN.getFixedSize();
          break;
        case ValueMetaInterface.TYPE_BIGNUMBER:
          column.setType( DataType.NUMERIC );
          length = DataType.NUMERIC.getFixedSize();
          break;
        default:
          break;
      }

      if ( length >= 0 ) {
        column.setLength( (short) length );
      }
      if ( value.getPrecision() >= 1 && value.getPrecision() <= 28 ) {
        column.setPrecision( (byte) value.getPrecision() );
      }

      list.add( column );
    }

    return list;
  }

  public static Object[] createObjectsForRow( RowMetaInterface rowMeta, Object[] rowData ) throws KettleValueException {
    Object[] values = new Object[rowMeta.size()];
    for ( int i = 0; i < rowMeta.size(); i++ ) {
      ValueMetaInterface valueMeta = rowMeta.getValueMeta( i );
      Object valueData = rowData[i];

      // Prevent a NullPointerException below
      if ( valueData == null || valueMeta == null ) {
        values[i] = null;
        continue;
      }

      int length = valueMeta.getLength();

      switch ( valueMeta.getType() ) {
        case ValueMetaInterface.TYPE_INTEGER:
          if ( length < 3 ) {
            values[i] = new Byte( valueMeta.getInteger( valueData ).byteValue() );
          } else {
            if ( length < 5 ) {
              values[i] = new Short( valueMeta.getInteger( valueData ).shortValue() );
            } else {
              values[i] = valueMeta.getInteger( valueData );
            }
          }
          break;
        case ValueMetaInterface.TYPE_NUMBER:
          values[i] = valueMeta.getNumber( valueData );
          break;
        case ValueMetaInterface.TYPE_DATE:
          values[i] = valueMeta.getDate( valueData );
          break;
        case ValueMetaInterface.TYPE_STRING:
          values[i] = valueMeta.getString( valueData );
          break;
        case ValueMetaInterface.TYPE_BINARY:
          values[i] = valueMeta.getBinary( valueData );
          break;
        case ValueMetaInterface.TYPE_BOOLEAN:
          values[i] = valueMeta.getBoolean( valueData );
          break;
        case ValueMetaInterface.TYPE_BIGNUMBER:
          values[i] = valueMeta.getNumber( valueData );
          break;
        default:
          break;
      }
    }
    return values;
  }

  /**
   * @return the fileCreated
   */
  public boolean isFileCreated() {
    return fileCreated;
  }

  /**
   * @param fileCreated
   *          the fileCreated to set
   */
  public void setFileCreated( boolean fileCreated ) {
    this.fileCreated = fileCreated;
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @param filename
   *          the filename to set
   */
  public void setFilename( String filename ) {
    this.filename = filename;
  }

  /**
   * @return the tableCreated
   */
  public boolean isTableCreated() {
    return tableCreated;
  }

  /**
   * @param tableCreated
   *          the tableCreated to set
   */
  public void setTableCreated( boolean tableCreated ) {
    this.tableCreated = tableCreated;
  }

  /**
   * @return the tableTruncated
   */
  public boolean isTableTruncated() {
    return tableTruncated;
  }

  /**
   * @return the commitSize
   */
  public int getCommitSize() {
    return commitSize;
  }

  /**
   * @param commitSize
   *          the commitSize to set
   */
  public void setCommitSize( int commitSize ) {
    this.commitSize = commitSize;
  }

  /**
   * @return Returns the add to result filesname.
   */
  public boolean isAddToResultFiles() {
    return addToResultFilenames;
  }

  /**
   * @param addtoresultfilenamesin
   *          The addtoresultfilenames to set.
   */
  public void setAddToResultFiles( boolean addtoresultfilenamesin ) {
    this.addToResultFilenames = addtoresultfilenamesin;
  }

  /**
   * @return Returns the "do not open new file init" flag
   */
  public boolean isDoNotOpenNewFileInit() {
    return doNotOpeNnewFileInit;
  }

  /**
   * @param doNotOpenNewFileInit
   *          The "do not open new file init" flag to set.
   */
  public void setDoNotOpenNewFileInit( boolean doNotOpenNewFileInit ) {
    this.doNotOpeNnewFileInit = doNotOpenNewFileInit;
  }

  public String[] getUsedLibraries() {
    return new String[] {
      "jackcess-1.1.13.jar", "commons-collections-3.1.jar", "commons-logging.jar", "commons-lang-2.2.jar",
      "commons-dbcp-1.2.1.jar", "commons-pool-1.3.jar", };
  }

  /**
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
      if ( !Utils.isEmpty( filename ) ) {
        FileObject fileObject = KettleVFS.getFileObject( space.environmentSubstitute( filename ), space );
        filename = resourceNamingInterface.nameResource( fileObject, space, true );
      }

      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

}
