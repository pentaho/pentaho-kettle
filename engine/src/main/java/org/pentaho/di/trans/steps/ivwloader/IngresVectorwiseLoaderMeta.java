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

package org.pentaho.di.trans.steps.ivwloader;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ProvidesDatabaseConnectionInformation;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Metadata for the VectorWise bulk loader.
 */
public class IngresVectorwiseLoaderMeta extends BaseStepMeta implements StepMetaInterface,
  ProvidesDatabaseConnectionInformation {

  /** For i18n purposes, needed by Translator2!! */
  private static Class<?> PKG = IngresVectorwiseLoaderMeta.class;

  private DatabaseMeta databaseMeta;
  private String tablename;

  /** Fields containing the values in the input stream to insert */
  private String[] fieldStream;

  /** Fields in the table to insert */
  private String[] fieldDatabase;

  /** Column format specifiers */
  private String[] fieldFormat;

  /** The name of the FIFO file to create */
  private String fifoFileName;

  /** The name of the file to write the error log to */
  private String errorFileName;

  /** Flag to enable Copy Error Handling */
  private boolean continueOnError;

  /** Path to the Ingres "sql" utility */
  private String sqlPath;

  /** Use standard formatting for Date and Number fields */
  private boolean useStandardConversion;

  /** Use authentication */
  private boolean useAuthentication;

  /** Encoding to use */
  private String encoding;

  /** The delimiter to use */
  private String delimiter;

  private boolean useSSV;

  private boolean rejectErrors = false;

  // connect with dynamic VNode build from JDBC Connection
  private boolean useDynamicVNode;

  /**
   * Set to true if special characters need to be escaped in the input Strings. (", \n, \r)
   */
  private boolean escapingSpecialCharacters;

  /** NIO buffer size */
  private String bufferSize;

  /**
   * Use the "vwload" utility
   */
  private boolean usingVwload;

  /** The maximum number of errors after which we want to abort vwload */
  private String maxNrErrors;

  /**
   * truncate table prior to load?
   */
  private boolean truncatingTable;

  /**
   * Default constructor.
   */
  public IngresVectorwiseLoaderMeta() {
    super();
  }

  /**
   * {@inheritDoc}
   *
   * @see org.pentaho.di.trans.step.StepMetaInterface#getStep(org.pentaho.di.trans.step.StepMeta,
   *      org.pentaho.di.trans.step.StepDataInterface, int, org.pentaho.di.trans.TransMeta, org.pentaho.di.trans.Trans)
   */
  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    IngresVectorwiseLoader loader = new IngresVectorwiseLoader( stepMeta, stepDataInterface, cnr, tr, trans );
    return loader;
  }

  /**
   * {@inheritDoc}
   *
   * @see org.pentaho.di.trans.step.StepMetaInterface#getStepData()
   */
  @Override
  public StepDataInterface getStepData() {
    return new IngresVectorwiseLoaderData();
  }

  /**
   * {@inheritDoc}
   *
   * @see org.pentaho.di.trans.step.BaseStepMeta#clone()
   */
  @Override
  public Object clone() {
    IngresVectorwiseLoaderMeta retval = (IngresVectorwiseLoaderMeta) super.clone();
    return retval;
  }

  @Override
  public void setDefault() {
    allocate( 0 );
    sqlPath = "/opt/Ingres/IngresVW/ingres/bin/sql";
    delimiter = "|";
    fifoFileName = "${java.io.tmpdir}/fifoVW-${Internal.Step.CopyNr}";
    useStandardConversion = false;
    useAuthentication = false;
    continueOnError = false;
    useDynamicVNode = false;
    escapingSpecialCharacters = true;
    usingVwload = false;
    maxNrErrors = "50";
    truncatingTable = false;
    useSSV = false;
    bufferSize = "5000";
  }

  /** @return the rejectErrors */
  public boolean isRejectErrors() {
    return rejectErrors;
  }

  /**
   * @param rejectErrors
   *          the rejectErrors to set.
   */
  public void setRejectErrors( boolean rejectErrors ) {
    this.rejectErrors = rejectErrors;
  }

  public void allocate( int nrRows ) {
    fieldStream = new String[nrRows];
    fieldDatabase = new String[nrRows];
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    " ).append(
      XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "table", tablename ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "fifo_file_name", fifoFileName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sql_path", sqlPath ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "encoding", encoding ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "delimiter", delimiter ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "continue_on_error", continueOnError ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "error_file_name", errorFileName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "use_standard_conversion", useStandardConversion ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "use_authentication", useAuthentication ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "use_dynamic_vnode", useDynamicVNode ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "use_SSV_delimiter", useSSV ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "escape_special_characters", escapingSpecialCharacters ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "use_vwload", usingVwload ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "truncate_table", truncatingTable ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "max_errors", maxNrErrors ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "buffer_size", bufferSize ) );

    retval.append( "    <fields>" ).append( Const.CR );

    for ( int i = 0; i < fieldDatabase.length; i++ ) {
      retval.append( "        <field>" ).append( Const.CR );
      retval.append( "          " ).append( XMLHandler.addTagValue( "column_name", fieldDatabase[i] ) );
      retval.append( "          " ).append( XMLHandler.addTagValue( "stream_name", fieldStream[i] ) );
      retval.append( "        </field>" ).append( Const.CR );
    }
    retval.append( "    </fields>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    try {
      String con = XMLHandler.getTagValue( stepnode, "connection" );
      databaseMeta = DatabaseMeta.findDatabase( databases, con );
      tablename = XMLHandler.getTagValue( stepnode, "table" );
      fifoFileName = XMLHandler.getTagValue( stepnode, "fifo_file_name" );
      sqlPath = XMLHandler.getTagValue( stepnode, "sql_path" );
      encoding = XMLHandler.getTagValue( stepnode, "encoding" );
      delimiter = XMLHandler.getTagValue( stepnode, "delimiter" );
      continueOnError = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "continue_on_error" ) );
      errorFileName = XMLHandler.getTagValue( stepnode, "error_file_name" );
      useStandardConversion = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "use_standard_conversion" ) );
      useAuthentication = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "use_authentication" ) );
      useDynamicVNode = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "use_dynamic_vnode" ) );
      useSSV = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "use_SSV_delimiter" ) );
      String escape = XMLHandler.getTagValue( stepnode, "escape_special_characters" );
      escapingSpecialCharacters = Utils.isEmpty( escape ) ? true : "Y".equalsIgnoreCase( escape );
      usingVwload = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "use_vwload" ) );
      maxNrErrors = XMLHandler.getTagValue( stepnode, "max_errors" );
      truncatingTable = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "truncate_table" ) );
      bufferSize = XMLHandler.getTagValue( stepnode, "buffer_size" );

      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrRows = XMLHandler.countNodes( fields, "field" );

      allocate( nrRows );

      for ( int i = 0; i < nrRows; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( fields, "field", i );

        fieldDatabase[i] = XMLHandler.getTagValue( knode, "column_name" );
        fieldStream[i] = XMLHandler.getTagValue( knode, "stream_name" );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );
      tablename = rep.getStepAttributeString( id_step, "table" );
      fifoFileName = rep.getStepAttributeString( id_step, "fifo_file_name" );
      sqlPath = rep.getStepAttributeString( id_step, "sql_path" );
      encoding = rep.getStepAttributeString( id_step, "encoding" );
      delimiter = rep.getStepAttributeString( id_step, "delimiter" );
      continueOnError = rep.getStepAttributeBoolean( id_step, "continue_on_error" );
      errorFileName = rep.getStepAttributeString( id_step, "error_file_name" );
      useStandardConversion = rep.getStepAttributeBoolean( id_step, "use_standard_conversion" );
      useAuthentication = rep.getStepAttributeBoolean( id_step, "use_authentication" );
      useDynamicVNode = rep.getStepAttributeBoolean( id_step, "use_dynamic_vnode" );
      useSSV = rep.getStepAttributeBoolean( id_step, "use_SSV_delimiter" );
      escapingSpecialCharacters = rep.getStepAttributeBoolean( id_step, 0, "escape_special_characters", true );
      usingVwload = rep.getStepAttributeBoolean( id_step, "use_vwload" );
      maxNrErrors = rep.getStepAttributeString( id_step, "max_errors" );
      truncatingTable = rep.getStepAttributeBoolean( id_step, "truncate_table" );
      bufferSize = rep.getStepAttributeString( id_step, "buffer_size" );

      int nrCols = rep.countNrStepAttributes( id_step, "column_name" );
      int nrStreams = rep.countNrStepAttributes( id_step, "stream_name" );

      int nrRows = ( nrCols < nrStreams ? nrStreams : nrCols );
      allocate( nrRows );

      for ( int idx = 0; idx < nrRows; idx++ ) {
        fieldDatabase[idx] = Const.NVL( rep.getStepAttributeString( id_step, idx, "column_name" ), "" );
        fieldStream[idx] = Const.NVL( rep.getStepAttributeString( id_step, idx, "stream_name" ), "" );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", databaseMeta );
      rep.saveStepAttribute( id_transformation, id_step, "table", tablename );
      rep.saveStepAttribute( id_transformation, id_step, "fifo_file_name", fifoFileName );
      rep.saveStepAttribute( id_transformation, id_step, "sql_path", sqlPath );
      rep.saveStepAttribute( id_transformation, id_step, "encoding", encoding );
      rep.saveStepAttribute( id_transformation, id_step, "delimiter", delimiter );
      rep.saveStepAttribute( id_transformation, id_step, "continue_on_error", continueOnError );
      rep.saveStepAttribute( id_transformation, id_step, "error_file_name", errorFileName );
      rep.saveStepAttribute( id_transformation, id_step, "use_standard_conversion", useStandardConversion );
      rep.saveStepAttribute( id_transformation, id_step, "use_authentication", useAuthentication );
      rep.saveStepAttribute( id_transformation, id_step, "use_dynamic_vnode", useDynamicVNode );
      rep.saveStepAttribute( id_transformation, id_step, "use_SSV_delimiter", useSSV );
      rep.saveStepAttribute( id_transformation, id_step, "escape_special_characters", escapingSpecialCharacters );
      rep.saveStepAttribute( id_transformation, id_step, "use_vwload", usingVwload );
      rep.saveStepAttribute( id_transformation, id_step, "max_errors", maxNrErrors );
      rep.saveStepAttribute( id_transformation, id_step, "truncate_table", truncatingTable );
      rep.saveStepAttribute( id_transformation, id_step, "buffer_size", bufferSize );

      int nrRows = ( fieldDatabase.length < fieldStream.length ? fieldStream.length : fieldDatabase.length );
      for ( int idx = 0; idx < nrRows; idx++ ) {
        String columnName = ( idx < fieldDatabase.length ? fieldDatabase[idx] : "" );
        String streamName = ( idx < fieldStream.length ? fieldStream[idx] : "" );
        rep.saveStepAttribute( id_transformation, id_step, idx, "column_name", columnName );
        rep.saveStepAttribute( id_transformation, id_step, idx, "stream_name", streamName );
      }

      // Also, save the step-database relationship!
      if ( databaseMeta != null ) {
        rep.insertStepDatabase( id_transformation, id_step, databaseMeta.getObjectId() );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  /**
   * @return the databaseMeta
   */
  @Override
  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * @param databaseMeta
   *          the databaseMeta to set
   */
  public void setDatabaseMeta( DatabaseMeta databaseMeta ) {
    this.databaseMeta = databaseMeta;
  }

  /**
   * @return the tablename
   */
  @Override
  public String getTableName() {
    return tablename;
  }

  /**
   * @param tablename
   *          the tablename to set
   */
  public void setTablename( String tablename ) {
    this.tablename = tablename;
  }

  /**
   * @return the fieldStream
   */
  public String[] getFieldStream() {
    return fieldStream;
  }

  /**
   * @param fieldStream
   *          the fieldStream to set
   */
  public void setFieldStream( String[] fieldStream ) {
    this.fieldStream = fieldStream;
  }

  /**
   * @return the fieldDatabase
   */
  public String[] getFieldDatabase() {
    return fieldDatabase;
  }

  /**
   * @param fieldDatabase
   *          the fieldDatabase to set
   */
  public void setFieldDatabase( String[] fieldDatabase ) {
    this.fieldDatabase = fieldDatabase;
  }

  /**
   * @return the fieldFormat
   */
  public String[] getFieldFormat() {
    return fieldFormat;
  }

  /**
   * @param fieldFormat
   *          the fieldFormat to set
   */
  public void setFieldFormat( String[] fieldFormat ) {
    this.fieldFormat = fieldFormat;
  }

  @Override
  public SQLStatement getSQLStatements( TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
    Repository repository, IMetaStore metaStore ) {
    SQLStatement retval = new SQLStatement( stepMeta.getName(), databaseMeta, null ); // default:
                                                                                      // nothing
                                                                                      // to
                                                                                      // do!

    if ( databaseMeta != null ) {
      if ( prev != null && prev.size() > 0 ) {
        if ( !Utils.isEmpty( tablename ) ) {
          Database db = new Database( loggingObject, databaseMeta );
          db.shareVariablesWith( transMeta );
          try {
            db.connect();

            String schemaTable = databaseMeta.getQuotedSchemaTableCombination( null, tablename );
            String cr_table = db.getDDL( schemaTable, prev );

            // Squeeze in the VECTORWISE col store clause...
            // TODO: move this to the database dialog and make it user
            // configurable.
            //
            String VW_CLAUSE = "WITH STRUCTURE=VECTORWISE";

            if ( cr_table.toUpperCase().contains( "CREATE TABLE" ) ) {
              int scIndex = cr_table.indexOf( ';' );
              if ( scIndex < 0 ) {
                cr_table += VW_CLAUSE;
              } else {
                cr_table = cr_table.substring( 0, scIndex ) + VW_CLAUSE + cr_table.substring( scIndex );
              }
            }

            // Empty string means: nothing to do: set it to null...
            if ( cr_table == null || cr_table.length() == 0 ) {
              cr_table = null;
            }

            retval.setSQL( cr_table );
          } catch ( KettleDatabaseException dbe ) {
            retval.setError( BaseMessages.getString( PKG, "IngresVectorWiseLoaderMeta.Error.ErrorConnecting", dbe
              .getMessage() ) );
          } finally {
            db.disconnect();
          }
        } else {
          retval.setError( BaseMessages.getString( PKG, "IngresVectorWiseLoaderMeta.Error.NoTable" ) );
        }
      } else {
        retval.setError( BaseMessages.getString( PKG, "IngresVectorWiseLoaderMeta.Error.NoInput" ) );
      }
    } else {
      retval.setError( BaseMessages.getString( PKG, "IngresVectorWiseLoaderMeta.Error.NoConnection" ) );
    }

    return retval;
  }

  /**
   * @return the fifoFileName
   */
  public String getFifoFileName() {
    return fifoFileName;
  }

  /**
   * @param fifoFileName
   *          the fifoFileName to set
   */
  public void setFifoFileName( String fifoFileName ) {
    this.fifoFileName = fifoFileName;
  }

  /**
   * @return the sqlPath
   */
  public String getSqlPath() {
    return sqlPath;
  }

  /**
   * @param sqlPath
   *          the sqlPath to set
   */
  public void setSqlPath( String sqlPath ) {
    this.sqlPath = sqlPath;
  }

  /**
   * @return the encoding
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * @param encoding
   *          the encoding to set
   */
  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

  /**
   * @return the delimiter
   */
  public String getDelimiter() {
    return delimiter;
  }

  /**
   * @param delimiter
   *          the delimiter to set
   */
  public void setDelimiter( String delimiter ) {
    this.delimiter = delimiter;
  }

  public String getErrorFileName() {
    return errorFileName;
  }

  public void setErrorFileName( String errorFileName ) {
    this.errorFileName = errorFileName;
  }

  public boolean isContinueOnError() {
    return continueOnError;
  }

  public void setContinueOnError( boolean continueOnError ) {
    this.continueOnError = continueOnError;
  }

  public boolean isUseStandardConversion() {
    return useStandardConversion;
  }

  public void setUseStandardConversion( boolean useStandardConversion ) {
    this.useStandardConversion = useStandardConversion;
  }

  public boolean isUseDynamicVNode() {
    return useDynamicVNode;
  }

  public void setUseDynamicVNode( boolean createDynamicVNode ) {
    this.useDynamicVNode = createDynamicVNode;
  }

  public boolean isUseSSV() {
    return useSSV;
  }

  public void setUseSSV( boolean useSSV ) {
    this.useSSV = useSSV;
  }

  /**
   * @return the escapingSpecialCharacters
   */
  public boolean isEscapingSpecialCharacters() {
    return escapingSpecialCharacters;
  }

  /**
   * @param escapingSpecialCharacters
   *          the escapingSpecialCharacters to set
   */
  public void setEscapingSpecialCharacters( boolean escapingSpecialCharacters ) {
    this.escapingSpecialCharacters = escapingSpecialCharacters;
  }

  /**
   * @return the bufferSize
   */
  public String getBufferSize() {
    return bufferSize;
  }

  /**
   * @param bufferSize
   *          the bufferSize to set
   */
  public void setBufferSize( String bufferSize ) {
    this.bufferSize = bufferSize;
  }

  /**
   * @return the useAuthentication
   */
  public boolean isUseAuthentication() {
    return useAuthentication;
  }

  /**
   * @param useAuthentication
   *          the useAuthentication to set
   */
  public void setUseAuthentication( boolean useAuthentication ) {
    this.useAuthentication = useAuthentication;
  }

  public boolean isUsingVwload() {
    return usingVwload;
  }

  public void setUsingVwload( boolean usingVwload ) {
    this.usingVwload = usingVwload;
  }

  public boolean isTruncatingTable() {
    return truncatingTable;
  }

  public void setTruncatingTable( boolean truncatingTable ) {
    this.truncatingTable = truncatingTable;
  }

  public String getMaxNrErrors() {
    return maxNrErrors;
  }

  public void setMaxNrErrors( String maxNrErrors ) {
    this.maxNrErrors = maxNrErrors;
  }

  @Override
  public String getSchemaName() {
    return null;
  }

  @Override
  public String getMissingDatabaseConnectionInformationMessage() {
    return null;
  }

  @Override
  public DatabaseMeta[] getUsedDatabaseConnections() {
    if ( databaseMeta != null ) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

}
