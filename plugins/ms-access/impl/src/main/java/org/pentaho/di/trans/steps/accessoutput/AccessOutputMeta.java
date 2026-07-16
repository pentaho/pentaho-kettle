/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.accessoutput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.bowl.Bowl;
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
import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;

/*
 * Created on 2-jun-2003
 *
 */

@Step( id = "AccessOutput", name = "BaseStep.TypeLongDesc.AccessOutput",
       categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Output",
       description = "BaseStep.TypeTooltipDesc.AccessOutput",
       image = "images/ACO.svg",
       documentationUrl = "http://wiki.pentaho.com/display/EAI/Access+Output",
       i18nPackageName = "org.pentaho.di.trans.steps.accessoutput" )
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
    try {
      if ( !file.exists() || !file.isFile() ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "AccessOutputMeta.Exception.FileDoesNotExist", realFilename ) );
      }

      // Open the database in read-only mode and get the table metadata.
      try ( Database db = new DatabaseBuilder( file ).setReadOnly( true ).open() ) {
        String realTablename = space.environmentSubstitute( tablename );
        Table table = db.getTable( realTablename );
        if ( table == null ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "AccessOutputMeta.Exception.TableDoesNotExist", realTablename ) );
        }

        return getLayout( table );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "AccessOutputMeta.Exception.ErrorGettingFields" ), e );
    }
  }

  public static final RowMetaInterface getLayout( Table table ) throws KettleStepException {
    RowMetaInterface row = new RowMeta();
    List<? extends Column> columns = table.getColumns();
    for ( int i = 0; i < columns.size(); i++ ) {
      ColumnLayout layout = resolveColumnLayout( columns.get( i ) );
      row.addValueMeta( createValueMeta( columns.get( i ).getName(), layout ) );
    }

    return row;
  }

  private static ColumnLayout resolveColumnLayout( Column column ) throws KettleStepException {
    int type = getSqlType( column );
    switch ( type ) {
      case java.sql.Types.CHAR:
      case java.sql.Types.VARCHAR:
      case java.sql.Types.LONGVARCHAR:
        return new ColumnLayout( ValueMetaInterface.TYPE_STRING, column.getLength(), -1 );
      case java.sql.Types.CLOB:
        return new ColumnLayout( ValueMetaInterface.TYPE_STRING, DatabaseMeta.CLOB_LENGTH, -1 );
      case java.sql.Types.BIGINT:
        return new ColumnLayout( ValueMetaInterface.TYPE_INTEGER, 15, 0 );
      case java.sql.Types.INTEGER:
        return new ColumnLayout( ValueMetaInterface.TYPE_INTEGER, 9, 0 );
      case java.sql.Types.SMALLINT:
        return new ColumnLayout( ValueMetaInterface.TYPE_INTEGER, 4, 0 );
      case java.sql.Types.TINYINT:
        return new ColumnLayout( ValueMetaInterface.TYPE_INTEGER, 2, 0 );
      case java.sql.Types.DECIMAL:
      case java.sql.Types.DOUBLE:
      case java.sql.Types.FLOAT:
      case java.sql.Types.REAL:
      case java.sql.Types.NUMERIC:
        return resolveNumericColumnLayout( column, type );
      case java.sql.Types.DATE:
      case java.sql.Types.TIME:
      case java.sql.Types.TIMESTAMP:
        return new ColumnLayout( ValueMetaInterface.TYPE_DATE, -1, -1 );
      case java.sql.Types.BOOLEAN:
      case java.sql.Types.BIT:
        return new ColumnLayout( ValueMetaInterface.TYPE_BOOLEAN, -1, -1 );
      case java.sql.Types.BINARY:
      case java.sql.Types.BLOB:
      case java.sql.Types.VARBINARY:
      case java.sql.Types.LONGVARBINARY:
        return new ColumnLayout( ValueMetaInterface.TYPE_BINARY, -1, -1 );
      default:
        return new ColumnLayout( ValueMetaInterface.TYPE_STRING, column.getLength(), -1 );
    }
  }

  private static int getSqlType( Column column ) throws KettleStepException {
    try {
      return column.getType().getSQLType();
    } catch ( IOException e ) {
      throw new KettleStepException( BaseMessages.getString( PKG, "AccessOutputMeta.Exception.ErrorGettingFields" ), e );
    }
  }

  private static ColumnLayout resolveNumericColumnLayout( Column column, int type ) {
    int valueType = ValueMetaInterface.TYPE_NUMBER;
    int length = normalizeLengthOrPrecision( column.getLength() );
    int precision = normalizeLengthOrPrecision( column.getPrecision() );

    if ( isFloatingPointType( type ) ) {
      if ( precision == 0 ) {
        precision = -1;
      }
    } else if ( precision == 0 && length < 18 && length > 0 ) {
      valueType = ValueMetaInterface.TYPE_INTEGER;
    }

    if ( length > 18 || precision > 18 ) {
      valueType = ValueMetaInterface.TYPE_BIGNUMBER;
    }

    return new ColumnLayout( valueType, length, precision );
  }

  private static boolean isFloatingPointType( int type ) {
    return type == java.sql.Types.DOUBLE || type == java.sql.Types.FLOAT || type == java.sql.Types.REAL;
  }

  private static int normalizeLengthOrPrecision( int value ) {
    return value >= 126 ? -1 : value;
  }

  private static ValueMetaInterface createValueMeta( String columnName, ColumnLayout layout ) throws KettleStepException {
    try {
      ValueMetaInterface valueMeta = ValueMetaFactory.createValueMeta( columnName, layout.valueType );
      valueMeta.setLength( layout.length, layout.precision );
      return valueMeta;
    } catch ( KettlePluginException e ) {
      throw new KettleStepException( e );
    }
  }

  private static final class ColumnLayout {
    private final int valueType;
    private final int length;
    private final int precision;

    private ColumnLayout( int valueType, int length, int precision ) {
      this.valueType = valueType;
      this.length = length;
      this.precision = precision;
    }
  }

  public static final List<ColumnBuilder> getColumns( RowMetaInterface row ) {
    List<ColumnBuilder> list = new ArrayList<>();

    for ( int i = 0; i < row.size(); i++ ) {
      ValueMetaInterface value = row.getValueMeta( i );
      ColumnBuilder column = new ColumnBuilder( value.getName() );
      ColumnSpec columnSpec = resolveColumnSpec( value );
      column.setType( columnSpec.dataType );
      if ( columnSpec.length >= 0 ) {
        column.setLength( columnSpec.length );
      }
      if ( value.getPrecision() >= 1 && value.getPrecision() <= 28 ) {
        column.setPrecision( value.getPrecision() );
      }

      list.add( column );
    }

    return list;
  }

  private static ColumnSpec resolveColumnSpec( ValueMetaInterface value ) {
    int length = value.getLength();
    switch ( value.getType() ) {
      case ValueMetaInterface.TYPE_INTEGER:
        return resolveIntegerColumnSpec( length );
      case ValueMetaInterface.TYPE_NUMBER:
        return new ColumnSpec( DataType.DOUBLE, DataType.DOUBLE.getFixedSize() );
      case ValueMetaInterface.TYPE_DATE:
        return new ColumnSpec( DataType.SHORT_DATE_TIME, DataType.SHORT_DATE_TIME.getFixedSize() );
      case ValueMetaInterface.TYPE_STRING:
        return resolveStringColumnSpec( length );
      case ValueMetaInterface.TYPE_BINARY:
        return new ColumnSpec( DataType.BINARY, length );
      case ValueMetaInterface.TYPE_BOOLEAN:
        return new ColumnSpec( DataType.BOOLEAN, DataType.BOOLEAN.getFixedSize() );
      case ValueMetaInterface.TYPE_BIGNUMBER:
        return new ColumnSpec( DataType.NUMERIC, DataType.NUMERIC.getFixedSize() );
      default:
        return new ColumnSpec( DataType.TEXT, length );
    }
  }

  private static ColumnSpec resolveIntegerColumnSpec( int length ) {
    if ( length < 3 ) {
      return new ColumnSpec( DataType.BYTE, DataType.BYTE.getFixedSize() );
    }
    if ( length < 5 ) {
      return new ColumnSpec( DataType.INT, DataType.INT.getFixedSize() );
    }
    return new ColumnSpec( DataType.LONG, DataType.LONG.getFixedSize() );
  }

  private static ColumnSpec resolveStringColumnSpec( int length ) {
    if ( length < 255 ) {
      return new ColumnSpec( DataType.TEXT, length * DataType.TEXT.getUnitSize() );
    }
    return new ColumnSpec( DataType.MEMO, length * DataType.MEMO.getUnitSize() );
  }

  private static final class ColumnSpec {
    private final DataType dataType;
    private final int length;

    private ColumnSpec( DataType dataType, int length ) {
      this.dataType = dataType;
      this.length = length;
    }
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
            values[i] = Byte.valueOf( valueMeta.getInteger( valueData ).byteValue() );
          } else {
            if ( length < 5 ) {
              values[i] = Short.valueOf( valueMeta.getInteger( valueData ).shortValue() );
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

  @Override
  public String[] getUsedLibraries() {
    return new String[] {
      "jackcess-4.0.11.jar", "commons-lang3-3.18.0.jar" };
  }

  /**
   * @param executionBowl
   *          For file access
   * @param globalManagementBowl
   *          if needed for access to the current "global" (System or Repository) level config for export. If null, no
   *          global config will be exported.
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
  @Override
  public String exportResources( Bowl executionBowl, Bowl globalManagementBowl, VariableSpace space,
      Map<String, ResourceDefinition> definitions, ResourceNamingInterface namingInterface,
      Repository repository, IMetaStore metaStore ) throws KettleException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the filename from relative to absolute by grabbing the file object...
      //
      if ( !Utils.isEmpty( filename ) ) {
        FileObject fileObject = KettleVFS.getInstance( executionBowl )
          .getFileObject( space.environmentSubstitute( filename ), space );
        filename = namingInterface.nameResource( fileObject, space, true );
      }

      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

}
