/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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
import java.util.Map;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
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
import org.w3c.dom.Node;

/**
 * Metadata for the VectorWise bulk loader.
 */
public class IngresVectorwiseLoaderMeta extends BaseStepMeta implements StepMetaInterface {

  private static Class<?> PKG          = IngresVectorwiseLoaderMeta.class; // for
                                                                           // i18n
                                                                           // purposes,
                                                                           // needed
                                                                           // by
                                                                           // Translator2!!
                                                                           // $NON-NLS-1$

  private DatabaseMeta    databaseMeta;
  private String          tablename;

  /** Fields containing the values in the input stream to insert */
  private String[]        fieldStream;

  /** Fields in the table to insert */
  private String[]        fieldDatabase;

  /** Column format specifiers */
  private String[]        fieldFormat;

  /** The name of the FIFO file to create */
  private String          fifoFileName;

  /** The name of the file to write the error log to */
  private String          errorFileName;

  /** Flag to enable Copy Error Handling */
  private boolean         continueOnError;

  /** Path to the Ingres "sql" utility */
  private String          sqlPath;

  /** Use standard formatting for Date and Number fields */
  private boolean         useStandardConversion;

  /** Encoding to use */
  private String          encoding;

  /** The delimiter to use */
  private String          delimiter;

  private boolean         useSSV;

  private boolean         rejectErrors = false;

  // connect with dynamic VNode build from JDBC Connection
  private boolean         useDynamicVNode;

  /**
   * Set to true if special characters need to be escaped in the input Strings.
   * (", \n, \r)
   */
  private boolean         escapingSpecialCharacters;

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
   *      org.pentaho.di.trans.step.StepDataInterface, int,
   *      org.pentaho.di.trans.TransMeta, org.pentaho.di.trans.Trans)
   */
  public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans) {
    IngresVectorwiseLoader loader = new IngresVectorwiseLoader(stepMeta, stepDataInterface, cnr, tr, trans);
    return loader;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.pentaho.di.trans.step.StepMetaInterface#getStepData()
   */
  public StepDataInterface getStepData() {
    return new IngresVectorwiseLoaderData();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.pentaho.di.trans.step.BaseStepMeta#clone()
   */
  public Object clone() {
    IngresVectorwiseLoaderMeta retval = (IngresVectorwiseLoaderMeta) super.clone();
    return retval;
  }

  public void setDefault() {
    allocate(0);
    sqlPath = "/opt/Ingres/IngresVW/ingres/bin/sql";
    delimiter = "|";
    fifoFileName = "${java.io.tmpdir}/fifoVW";
    useStandardConversion = false;
    continueOnError = false;
    useDynamicVNode = false;
    escapingSpecialCharacters = true;
    useSSV = false;
  }

  /** @return the rejectErrors */
  public boolean isRejectErrors() {
    return rejectErrors;
  }

  /**
   * @param rejectErrors
   *          the rejectErrors to set.
   */
  public void setRejectErrors(boolean rejectErrors) {
    this.rejectErrors = rejectErrors;
  }

  public void allocate(int nrRows) {
    fieldStream = new String[nrRows];
    fieldDatabase = new String[nrRows];
  }

  @Override
  public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) {
    // TODO Auto-generated method stub

  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append("    ").append(XMLHandler.addTagValue("connection", databaseMeta == null ? "" : databaseMeta.getName()));
    retval.append("    ").append(XMLHandler.addTagValue("table", tablename));
    retval.append("    ").append(XMLHandler.addTagValue("fifo_file_name", fifoFileName)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("sql_path", sqlPath)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("encoding", encoding)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("delimiter", delimiter)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("continue_on_error", continueOnError)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("error_file_name", errorFileName)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("use_standard_conversion", useStandardConversion)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("use_dynamic_vnode", useDynamicVNode)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("use_SSV_delimiter", useSSV)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("escape_special_characters", escapingSpecialCharacters)); //$NON-NLS-1$ //$NON-NLS-2$

    retval.append("    <fields>").append(Const.CR); //$NON-NLS-1$

    for (int i = 0; i < fieldDatabase.length; i++) {
      retval.append("        <field>").append(Const.CR); //$NON-NLS-1$
      retval.append("          ").append(XMLHandler.addTagValue("column_name", fieldDatabase[i])); //$NON-NLS-1$ //$NON-NLS-2$
      retval.append("          ").append(XMLHandler.addTagValue("stream_name", fieldStream[i])); //$NON-NLS-1$ //$NON-NLS-2$
      retval.append("        </field>").append(Const.CR); //$NON-NLS-1$
    }
    retval.append("    </fields>").append(Const.CR); //$NON-NLS-1$

    return retval.toString();
  }

  @Override
  public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
    try {
      String con = XMLHandler.getTagValue(stepnode, "connection");
      databaseMeta = DatabaseMeta.findDatabase(databases, con);
      tablename = XMLHandler.getTagValue(stepnode, "table");
      fifoFileName = XMLHandler.getTagValue(stepnode, "fifo_file_name"); //$NON-NLS-1$
      sqlPath = XMLHandler.getTagValue(stepnode, "sql_path"); //$NON-NLS-1$
      encoding = XMLHandler.getTagValue(stepnode, "encoding"); //$NON-NLS-1$
      delimiter = XMLHandler.getTagValue(stepnode, "delimiter"); //$NON-NLS-1$
      continueOnError = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "continue_on_error")); //$NON-NLS-1$
      errorFileName = XMLHandler.getTagValue(stepnode, "error_file_name"); //$NON-NLS-1$
      useStandardConversion = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "use_standard_conversion")); //$NON-NLS-1$
      useDynamicVNode = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "use_dynamic_vnode")); //$NON-NLS-1$
      useSSV = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "use_SSV_delimiter")); //$NON-NLS-1$
      String escape = XMLHandler.getTagValue(stepnode, "escape_special_characters");
      escapingSpecialCharacters = Const.isEmpty(escape) ? true : "Y".equalsIgnoreCase(escape); //$NON-NLS-1$
      
      Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
      int nrRows = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$

      allocate(nrRows);

      for (int i = 0; i < nrRows; i++) {
        Node knode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$

        fieldDatabase[i] = XMLHandler.getTagValue(knode, "column_name"); //$NON-NLS-1$
        fieldStream[i] = XMLHandler.getTagValue(knode, "stream_name"); //$NON-NLS-1$
      }
    } catch (Exception e) {
      throw new KettleXMLException("Unable to load step info from XML", e);
    }
  }

  public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases); //$NON-NLS-1$
      tablename = rep.getStepAttributeString(id_step, "table");
      fifoFileName = rep.getStepAttributeString(id_step, "fifo_file_name"); //$NON-NLS-1$
      sqlPath = rep.getStepAttributeString(id_step, "sql_path"); //$NON-NLS-1$
      encoding = rep.getStepAttributeString(id_step, "encoding"); //$NON-NLS-1$
      delimiter = rep.getStepAttributeString(id_step, "delimiter"); //$NON-NLS-1$
      continueOnError = rep.getStepAttributeBoolean(id_step, "continue_on_error"); //$NON-NLS-1$
      errorFileName = rep.getStepAttributeString(id_step, "error_file_name"); //$NON-NLS-1$
      useStandardConversion = rep.getStepAttributeBoolean(id_step, "use_standard_conversion"); //$NON-NLS-1$
      useDynamicVNode = rep.getStepAttributeBoolean(id_step, "use_dynamic_vnode"); //$NON-NLS-1$
      useSSV = rep.getStepAttributeBoolean(id_step, "use_SSV_delimiter"); //$NON-NLS-1$
      escapingSpecialCharacters = rep.getStepAttributeBoolean(id_step, 0, "escape_special_characters", true); //$NON-NLS-1$
      
      int nrCols = rep.countNrStepAttributes(id_step, "column_name"); //$NON-NLS-1$
      int nrStreams = rep.countNrStepAttributes(id_step, "stream_name"); //$NON-NLS-1$

      int nrRows = (nrCols < nrStreams ? nrStreams : nrCols);
      allocate(nrRows);

      for (int idx = 0; idx < nrRows; idx++) {
        fieldDatabase[idx] = Const.NVL(rep.getStepAttributeString(id_step, idx, "column_name"), ""); //$NON-NLS-1$ //$NON-NLS-2$
        fieldStream[idx] = Const.NVL(rep.getStepAttributeString(id_step, idx, "stream_name"), ""); //$NON-NLS-1$ //$NON-NLS-2$
      }
    } catch (Exception e) {
      throw new KettleException("Unexpected error reading step information from the repository", e);
    }
  }

  public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", databaseMeta);
      rep.saveStepAttribute(id_transformation, id_step, "table", tablename);
      rep.saveStepAttribute(id_transformation, id_step, "fifo_file_name", fifoFileName); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, "sql_path", sqlPath); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, "encoding", encoding); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, "delimiter", delimiter); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, "continue_on_error", continueOnError); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, "error_file_name", errorFileName); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, "use_standard_conversion", useStandardConversion); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, "use_dynamic_vnode", useDynamicVNode); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, "use_SSV_delimiter", useSSV); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, "escape_special_characters", escapingSpecialCharacters); //$NON-NLS-1$
      
      int nrRows = (fieldDatabase.length < fieldStream.length ? fieldStream.length : fieldDatabase.length);
      for (int idx = 0; idx < nrRows; idx++) {
        String columnName = (idx < fieldDatabase.length ? fieldDatabase[idx] : "");
        String streamName = (idx < fieldStream.length ? fieldStream[idx] : "");
        rep.saveStepAttribute(id_transformation, id_step, idx, "column_name", columnName); //$NON-NLS-1$
        rep.saveStepAttribute(id_transformation, id_step, idx, "stream_name", streamName); //$NON-NLS-1$
      }

      // Also, save the step-database relationship!
      if (databaseMeta != null) {
        rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getObjectId());
      }
    } catch (Exception e) {
      throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
    }
  }

  /**
   * @return the databaseMeta
   */
  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * @param databaseMeta
   *          the databaseMeta to set
   */
  public void setDatabaseMeta(DatabaseMeta databaseMeta) {
    this.databaseMeta = databaseMeta;
  }

  /**
   * @return the tablename
   */
  public String getTablename() {
    return tablename;
  }

  /**
   * @param tablename
   *          the tablename to set
   */
  public void setTablename(String tablename) {
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
  public void setFieldStream(String[] fieldStream) {
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
  public void setFieldDatabase(String[] fieldDatabase) {
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
  public void setFieldFormat(String[] fieldFormat) {
    this.fieldFormat = fieldFormat;
  }

  public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev) {
    SQLStatement retval = new SQLStatement(stepMeta.getName(), databaseMeta, null); // default:
                                                                                    // nothing
                                                                                    // to
                                                                                    // do!

    if (databaseMeta != null) {
      if (prev != null && prev.size() > 0) {
        if (!Const.isEmpty(tablename)) {
          Database db = new Database(loggingObject, databaseMeta);
          db.shareVariablesWith(transMeta);
          try {
            db.connect();

            String schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
            String cr_table = db.getDDL(schemaTable, prev);

            // Squeeze in the VECTORWISE col store clause...
            // TODO: move this to the database dialog and make it user
            // configurable.
            //
            String VW_CLAUSE = "WITH STRUCTURE=VECTORWISE";

            if (cr_table.toUpperCase().contains("CREATE TABLE")) {
              int scIndex = cr_table.indexOf(';');
              if (scIndex < 0) {
                cr_table += VW_CLAUSE;
              } else {
                cr_table = cr_table.substring(0, scIndex) + VW_CLAUSE + cr_table.substring(scIndex);
              }
            }

            // Empty string means: nothing to do: set it to null...
            if (cr_table == null || cr_table.length() == 0)
              cr_table = null;

            retval.setSQL(cr_table);
          } catch (KettleDatabaseException dbe) {
            retval.setError(BaseMessages.getString(PKG, "IngresVectorWiseLoaderMeta.Error.ErrorConnecting", dbe.getMessage()));
          } finally {
            db.disconnect();
          }
        } else {
          retval.setError(BaseMessages.getString(PKG, "IngresVectorWiseLoaderMeta.Error.NoTable"));
        }
      } else {
        retval.setError(BaseMessages.getString(PKG, "IngresVectorWiseLoaderMeta.Error.NoInput"));
      }
    } else {
      retval.setError(BaseMessages.getString(PKG, "IngresVectorWiseLoaderMeta.Error.NoConnection"));
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
  public void setFifoFileName(String fifoFileName) {
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
  public void setSqlPath(String sqlPath) {
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
  public void setEncoding(String encoding) {
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
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  public String getErrorFileName() {
    return errorFileName;
  }

  public void setErrorFileName(String errorFileName) {
    this.errorFileName = errorFileName;
  }

  public boolean isContinueOnError() {
    return continueOnError;
  }

  public void setContinueOnError(boolean continueOnError) {
    this.continueOnError = continueOnError;
  }

  public boolean isUseStandardConversion() {
    return useStandardConversion;
  }

  public void setUseStandardConversion(boolean useStandardConversion) {
    this.useStandardConversion = useStandardConversion;
  }

  public boolean isUseDynamicVNode() {
    return useDynamicVNode;
  }

  public void setUseDynamicVNode(boolean createDynamicVNode) {
    this.useDynamicVNode = createDynamicVNode;
  }

  public boolean isUseSSV() {
    return useSSV;
  }

  public void setUseSSV(boolean useSSV) {
    this.useSSV = useSSV;
  }

  /**
   * @return the escapingSpecialCharacters
   */
  public boolean isEscapingSpecialCharacters() {
    return escapingSpecialCharacters;
  }

  /**
   * @param escapingSpecialCharacters the escapingSpecialCharacters to set
   */
  public void setEscapingSpecialCharacters(boolean escapingSpecialCharacters) {
    this.escapingSpecialCharacters = escapingSpecialCharacters;
  }
}
