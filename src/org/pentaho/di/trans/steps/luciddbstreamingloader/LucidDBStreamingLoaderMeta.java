/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * Copyright (c) 2010 DynamoBI Corporation.  All rights reserved.
 * This software was developed by DynamoBI Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is LucidDB 
 * Streaming Loader.  The Initial Developer is DynamoBI Corporation.
 * 
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.di.trans.steps.luciddbstreamingloader;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/**
 * Description: Hold data for Farrago Streaming loader dialog/UI
 * 
 * @author Ray Zhang
 * @since Jan-05-2010
 * 
 */
public class LucidDBStreamingLoaderMeta extends BaseStepMeta implements
    StepMetaInterface {
  private static Class<?> PKG = LucidDBStreamingLoaderMeta.class; // for i18n

  // purposes,
  // needed by
  // Translator2!!
  // $NON-NLS-1$

  /** what's the schema for the target? */
  private String schemaName;

  /** what's the table for the target? */
  private String tableName;

  /** database connection */
  private DatabaseMeta databaseMeta;

  /** Host URL */
  private String host;

  /** Host port */
  private String port;

  /** DB Operation */
  private String operation;

  /** Field name of the target table in tabitem Keys */
  private String fieldTableForKeys[];

  /** Field name in the stream in tabitem Keys */
  private String fieldStreamForKeys[];

  /** Field name of the target table in tabitem Fields */
  private String fieldTableForFields[];

  /** Field name in the stream in tabitem Fields */
  private String fieldStreamForFields[];

  /** flag to indicate Insert or Update operation for LucidDB in tabitem Fields */
  private boolean insOrUptFlag[];

  /** It holds custom sql statements in CUSTOM Tab */
  private String custom_sql;

  /** DML sql_statment */
  private String sql_statement;

  /** It keep whether all components in tab is enable or not */
  private boolean tabIsEnable[];

  /**
   * if flag is true, check the target table in db, if not create it
   * automatically
   */
  private boolean autoCreateTbFlag;

  /** for automatically create table */
  private String selectStmt;

  public boolean isAutoCreateTbFlag() {
    return autoCreateTbFlag;
  }

  public void setAutoCreateTbFlag(boolean autoCreateTbFlag) {
    this.autoCreateTbFlag = autoCreateTbFlag;
  }

  public boolean[] getTabIsEnable() {
    return tabIsEnable;
  }

  public void setTabIsEnable(boolean[] tabIsEnable) {
    this.tabIsEnable = tabIsEnable;
  }

  public String getSql_statement() {
    return sql_statement;
  }

  public void setSql_statement(String sql_statement) {
    this.sql_statement = sql_statement;
  }

  public LucidDBStreamingLoaderMeta() {
    super();
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
  public void setDatabaseMeta(DatabaseMeta database) {
    this.databaseMeta = database;
  }

  /**
   * @return Returns the tableName.
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @param tableName
   *          The tableName to set.
   */
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public void loadXML(Node stepnode, List<DatabaseMeta> databases,
      Map<String, Counter> counters) throws KettleXMLException {
    readData(stepnode, databases);
  }

  public void allocate(int nrKeyMapping, int nrFieldMapping, int nrTabIsEnable) {
    // for Keys Tab
    fieldTableForKeys = new String[nrKeyMapping];
    fieldStreamForKeys = new String[nrKeyMapping];
    // for Fields Tab
    fieldTableForFields = new String[nrFieldMapping];
    fieldStreamForFields = new String[nrFieldMapping];
    insOrUptFlag = new boolean[nrFieldMapping];

    tabIsEnable = new boolean[nrTabIsEnable];
  }

  public Object clone() {
    LucidDBStreamingLoaderMeta retval = (LucidDBStreamingLoaderMeta) super
        .clone();
    int nrKeyMapping = fieldTableForKeys.length;
    int nrFieldMapping = fieldTableForFields.length;
    int nrTabIsEnable = tabIsEnable.length;
    retval.allocate(nrKeyMapping, nrFieldMapping, nrTabIsEnable);

    for (int i = 0; i < nrKeyMapping; i++) {
      retval.fieldTableForKeys[i] = fieldTableForKeys[i];
      retval.fieldStreamForKeys[i] = fieldStreamForKeys[i];
    }

    for (int i = 0; i < nrFieldMapping; i++) {
      retval.fieldTableForFields[i] = fieldTableForFields[i];
      retval.fieldStreamForFields[i] = fieldStreamForFields[i];
      retval.insOrUptFlag[i] = insOrUptFlag[i];
    }

    for (int i = 0; i < nrTabIsEnable; i++) {
      retval.tabIsEnable[i] = tabIsEnable[i];

    }

    return retval;
  }

  private void readData(Node stepnode,
      List<? extends SharedObjectInterface> databases)
      throws KettleXMLException {
    try {
      String con = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
      databaseMeta = DatabaseMeta.findDatabase(databases, con);
      schemaName = XMLHandler.getTagValue(stepnode, "schema"); //$NON-NLS-1$
      tableName = XMLHandler.getTagValue(stepnode, "table"); //$NON-NLS-1$
      autoCreateTbFlag = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode,
          "auto_create_table"));
      host = XMLHandler.getTagValue(stepnode, "host"); //$NON-NLS-1$
      port = XMLHandler.getTagValue(stepnode, "port"); //$NON-NLS-1$
      operation = XMLHandler.getTagValue(stepnode, "operation"); //$NON-NLS-1$
      custom_sql = XMLHandler.getTagValue(stepnode, "custom_sql"); //$NON-NLS-1$
      sql_statement = XMLHandler.getTagValue(stepnode, "sql_statement"); //$NON-NLS-1$
      selectStmt = XMLHandler.getTagValue(stepnode, "select_stmt"); //$NON-NLS-1$
      int nrKeyMapping = XMLHandler.countNodes(stepnode, "keys_mapping"); //$NON-NLS-1$
      int nrFieldMapping = XMLHandler.countNodes(stepnode, "fields_mapping"); //$NON-NLS-1$
      int nrTabIsEnable = XMLHandler.countNodes(stepnode,
          "tab_is_enable_mapping"); //$NON-NLS-1$
      allocate(nrKeyMapping, nrFieldMapping, nrTabIsEnable);

      for (int i = 0; i < nrKeyMapping; i++) {
        Node vnode = XMLHandler.getSubNodeByNr(stepnode, "keys_mapping", i); //$NON-NLS-1$

        fieldTableForKeys[i] = XMLHandler.getTagValue(vnode, "key_field_name"); //$NON-NLS-1$
        fieldStreamForKeys[i] = XMLHandler
            .getTagValue(vnode, "key_stream_name"); //$NON-NLS-1$
        if (fieldStreamForKeys[i] == null)
          fieldStreamForKeys[i] = fieldTableForKeys[i]; // default:
        // the same
        // name!

      }
      for (int i = 0; i < nrFieldMapping; i++) {
        Node vnode = XMLHandler.getSubNodeByNr(stepnode, "fields_mapping", i); //$NON-NLS-1$

        fieldTableForFields[i] = XMLHandler.getTagValue(vnode,
            "field_field_name"); //$NON-NLS-1$
        fieldStreamForFields[i] = XMLHandler.getTagValue(vnode,
            "field_stream_name"); //$NON-NLS-1$
        if (fieldStreamForFields[i] == null)
          fieldStreamForFields[i] = fieldTableForFields[i]; // default:
        // the
        // same
        // name!
        insOrUptFlag[i] = "Y".equalsIgnoreCase(XMLHandler.getTagValue(vnode, "insert_or_update_flag")); //$NON-NLS-1$

      }

      for (int i = 0; i < nrTabIsEnable; i++) {
        Node vnode = XMLHandler.getSubNodeByNr(stepnode,
            "tab_is_enable_mapping", i); //$NON-NLS-1$
        tabIsEnable[i] = "Y".equalsIgnoreCase(XMLHandler.getTagValue(vnode, "tab_is_enable")); //$NON-NLS-1$

      }

    } catch (Exception e) {
      throw new KettleXMLException(
          BaseMessages
              .getString(PKG,
                  "LucidDBStreamingLoaderMeta.Exception.UnableToReadStepInfoFromXML"), e); //$NON-NLS-1$
    }
  }

  public void setDefault() {
    databaseMeta = null;
    schemaName = ""; //$NON-NLS-1$
    tableName = BaseMessages.getString(PKG,
        "LucidDBStreamingLoaderMeta.DefaultTableName"); //$NON-NLS-1$      
    host = "http://";
    port = "0";
    operation = "MERGE";
    allocate(0, 0, 0);
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(300);

    retval
        .append("    ").append(XMLHandler.addTagValue("connection", databaseMeta == null ? "" : databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    retval.append("    ").append(XMLHandler.addTagValue("schema", schemaName)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("table", tableName)); //$NON-NLS-1$ //$NON-NLS-2$
    retval
        .append("    ").append(XMLHandler.addTagValue("auto_create_table", ((autoCreateTbFlag == true) ? "Y" : "N"))); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("host", host)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("port", port)); //$NON-NLS-1$ //$NON-NLS-2$
    retval
        .append("    ").append(XMLHandler.addTagValue("operation", operation)); //$NON-NLS-1$ //$NON-NLS-2$
    retval
        .append("    ").append(XMLHandler.addTagValue("custom_sql", custom_sql)); //$NON-NLS-1$ //$NON-NLS-2$
    retval
        .append("    ").append(XMLHandler.addTagValue("sql_statement", sql_statement)); //$NON-NLS-1$ //$NON-NLS-2$
    retval
        .append("    ").append(XMLHandler.addTagValue("select_stmt", selectStmt)); //$NON-NLS-1$ //$NON-NLS-2$

    for (int i = 0; i < fieldTableForKeys.length; i++) {
      retval.append("      <keys_mapping>").append(Const.CR); //$NON-NLS-1$
      retval
          .append("        ").append(XMLHandler.addTagValue("key_field_name", fieldTableForKeys[i])); //$NON-NLS-1$ //$NON-NLS-2$
      retval
          .append("        ").append(XMLHandler.addTagValue("key_stream_name", fieldStreamForKeys[i])); //$NON-NLS-1$ //$NON-NLS-2$
      retval.append("      </keys_mapping>").append(Const.CR); //$NON-NLS-1$
    }

    for (int i = 0; i < fieldTableForFields.length; i++) {
      retval.append("      <fields_mapping>").append(Const.CR); //$NON-NLS-1$
      retval
          .append("        ").append(XMLHandler.addTagValue("field_field_name", fieldTableForFields[i])); //$NON-NLS-1$ //$NON-NLS-2$
      retval
          .append("        ").append(XMLHandler.addTagValue("field_stream_name", fieldStreamForFields[i])); //$NON-NLS-1$ //$NON-NLS-2$
      retval
          .append("        ").append(XMLHandler.addTagValue("insert_or_update_flag", insOrUptFlag[i])); //$NON-NLS-1$ //$NON-NLS-2$
      retval.append("      </fields_mapping>").append(Const.CR); //$NON-NLS-1$
    }

    for (int i = 0; i < tabIsEnable.length; i++) {
      retval.append("      <tab_is_enable_mapping>").append(Const.CR); //$NON-NLS-1$
      retval
          .append("        ").append(XMLHandler.addTagValue("tab_is_enable", tabIsEnable[i])); //$NON-NLS-1$ //$NON-NLS-2$
      retval.append("      </tab_is_enable_mapping>").append(Const.CR); //$NON-NLS-1$
    }

    return retval.toString();
  }

  public void readRep(Repository rep, ObjectId id_step,
      List<DatabaseMeta> databases, Map<String, Counter> counters)
      throws KettleException {
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute(id_step,
          "id_connection", databases);
      schemaName = rep.getStepAttributeString(id_step, "schema"); //$NON-NLS-1$
      tableName = rep.getStepAttributeString(id_step, "table"); //$NON-NLS-1$
      autoCreateTbFlag = "Y".equalsIgnoreCase(rep.getStepAttributeString(
          id_step, "auto_create_table"));
      host = rep.getStepAttributeString(id_step, "host"); //$NON-NLS-1$
      port = rep.getStepAttributeString(id_step, "port"); //$NON-NLS-1$
      operation = rep.getStepAttributeString(id_step, "operation"); //$NON-NLS-1$
      custom_sql = rep.getStepAttributeString(id_step, "custom_sql"); //$NON-NLS-1$
      sql_statement = rep.getStepAttributeString(id_step, "sql_statement"); //$NON-NLS-1$
      selectStmt = rep.getStepAttributeString(id_step, "select_stmt");
      int nrKeyMapping = rep.countNrStepAttributes(id_step, "keys_mapping"); //$NON-NLS-1$
      int nrFieldMapping = rep.countNrStepAttributes(id_step, "fields_mapping"); //$NON-NLS-1$
      int nrTabIsEnable = rep.countNrStepAttributes(id_step,
          "tab_is_enable_mapping"); //$NON-NLS-1$

      allocate(nrKeyMapping, nrFieldMapping, nrTabIsEnable);

      for (int i = 0; i < nrKeyMapping; i++) {
        fieldTableForKeys[i] = rep.getStepAttributeString(id_step, i,
            "key_field_name"); //$NON-NLS-1$
        fieldStreamForKeys[i] = rep.getStepAttributeString(id_step, i,
            "key_stream_name"); //$NON-NLS-1$
        if (fieldStreamForKeys[i] == null)
          fieldStreamForKeys[i] = fieldTableForKeys[i];

      }

      for (int i = 0; i < nrFieldMapping; i++) {
        fieldTableForFields[i] = rep.getStepAttributeString(id_step, i,
            "field_field_name"); //$NON-NLS-1$
        fieldStreamForFields[i] = rep.getStepAttributeString(id_step, i,
            "field_stream_name"); //$NON-NLS-1$
        if (fieldStreamForFields[i] == null)
          fieldStreamForFields[i] = fieldTableForFields[i];
        insOrUptFlag[i] = rep.getStepAttributeBoolean(id_step, i,
            "insert_or_update_flag"); //$NON-NLS-1$
      }

      for (int i = 0; i < nrTabIsEnable; i++) {

        tabIsEnable[i] = rep.getStepAttributeBoolean(id_step, i,
            "tab_is_enable"); //$NON-NLS-1$
      }
    } catch (Exception e) {
      throw new KettleException(
          BaseMessages
              .getString(
                  PKG,
                  "LucidDBStreamingLoaderMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository"), e); //$NON-NLS-1$
    }
  }

  public void saveRep(Repository rep, ObjectId id_transformation,
      ObjectId id_step) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute(id_transformation, id_step,
          "id_connection", databaseMeta);
      rep.saveStepAttribute(id_transformation, id_step, "schema", schemaName); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, "table", tableName); //$NON-NLS-1$  
      rep.saveStepAttribute(id_transformation, id_step,
          "auto_create_table", ((autoCreateTbFlag == true) ? "Y" : "N")); //$NON-NLS-1$  
      rep.saveStepAttribute(id_transformation, id_step, "host", host); //$NON-NLS-1
      rep.saveStepAttribute(id_transformation, id_step, "port", port); //$NON-NLS-1
      rep.saveStepAttribute(id_transformation, id_step, "operation", operation); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step,
          "custom_sql", custom_sql); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step,
          "sql_statement", sql_statement); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step,
          "select_stmt", selectStmt); //$NON-NLS-1$            

      for (int i = 0; i < fieldTableForKeys.length; i++) {
        rep.saveStepAttribute(id_transformation, id_step, i,
            "key_field_name", fieldTableForKeys[i]); //$NON-NLS-1$
        rep.saveStepAttribute(id_transformation, id_step, i,
            "key_stream_name", fieldStreamForKeys[i]); //$NON-NLS-1$

      }

      for (int i = 0; i < fieldTableForFields.length; i++) {
        rep.saveStepAttribute(id_transformation, id_step, i,
            "field_field_name", fieldTableForFields[i]); //$NON-NLS-1$
        rep.saveStepAttribute(id_transformation, id_step, i,
            "field_stream_name", fieldStreamForFields[i]); //$NON-NLS-1$
        rep.saveStepAttribute(id_transformation, id_step, i,
            "insert_or_update_flag", insOrUptFlag[i]); //$NON-NLS-1$
      }

      for (int i = 0; i < tabIsEnable.length; i++) {

        rep.saveStepAttribute(id_transformation, id_step, i,
            "tab_is_enable", tabIsEnable[i]); //$NON-NLS-1$
      }

      // Also, save the step-database relationship!
      if (databaseMeta != null)
        rep.insertStepDatabase(id_transformation, id_step, databaseMeta
            .getObjectId());
    } catch (Exception e) {
      throw new KettleException(
          BaseMessages
              .getString(PKG,
                  "LucidDBStreamingLoaderMeta.Exception.UnableToSaveStepInfoToRepository") + id_step, e); //$NON-NLS-1$
    }
  }

  public void getFields(RowMetaInterface rowMeta, String origin,
      RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
      throws KettleStepException {
    // Default: nothing changes to rowMeta
  }

  // TODO: In future, we need to implement it to do double-check.
  public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
      StepMeta stepMeta, RowMetaInterface prev, String input[],
      String output[], RowMetaInterface info) {

  }

  /**
   * Create DML Sql Statements for remote_rows
   * 
   * @param prev
   * @return
   * @throws KettleStepException
   */
  public String getSQLStatements(RowMetaInterface prev)
      throws KettleStepException {
    String ret = "";

    try {

      StringBuffer myString = new StringBuffer(300);
      StringBuffer myMatchCondtion = new StringBuffer(300);

      if (fieldStreamForKeys != null) {

        for (int i = 0; i < fieldStreamForKeys.length; i++) {

          ValueMetaInterface fieldStream = prev
              .searchValueMeta(fieldStreamForKeys[i]);
          myString.append("cast(null as " + getSQLDataType(fieldStream)
              + ") as \"" + fieldStreamForKeys[i] + "\"," + Const.CR);

          if ((fieldStreamForKeys.length - 1) != i) {

            myMatchCondtion.append("src." + fieldStreamForKeys[i] + " = tgt."
                + fieldTableForKeys[i] + " AND" + Const.CR);

          } else {

            myMatchCondtion.append("src." + fieldStreamForKeys[i] + " = tgt."
                + fieldTableForKeys[i] + Const.CR);
          }
        }

      }

      StringBuffer myUpdateStatement = new StringBuffer(300);

      StringBuffer tgtColumns = new StringBuffer(300);
      StringBuffer srcColumns = new StringBuffer(300);
      if (fieldStreamForFields != null) {

        for (int i = 0; i < fieldStreamForFields.length; i++) {

          ValueMetaInterface fieldStream = prev
              .searchValueMeta(fieldStreamForFields[i]);
          if ((fieldStreamForFields.length - 1) != i) {

            myString.append("cast(null as " + getSQLDataType(fieldStream)
                + ") as \"" + fieldStreamForFields[i] + "\"," + Const.CR);

            tgtColumns.append(fieldTableForFields[i] + ",");
            srcColumns.append("src." + fieldStreamForFields[i] + ",");
            if (insOrUptFlag[i]) {

              myUpdateStatement.append(fieldTableForFields[i] + " = src."
                  + fieldStreamForFields[i] + "," + Const.CR);
            }

          } else {

            myString.append("cast(null as " + getSQLDataType(fieldStream)
                + ") as \"" + fieldStreamForFields[i] + "\"" + Const.CR);
            tgtColumns.append(fieldTableForFields[i]);
            srcColumns.append("src." + fieldStreamForFields[i]);
            if (insOrUptFlag[i]
                && !(BaseMessages.getString(PKG,
                    "LucidDBStreamingLoaderDialog.Operation.CCombo.Item2")
                    .equals(operation))) {

              myUpdateStatement.append(fieldTableForFields[i] + " = src."
                  + fieldStreamForFields[i] + Const.CR);
            }

          }

        }

      }

      StringBuffer cursor = new StringBuffer(300);
      cursor.append("select * from table ( applib.remote_rows (" + Const.CR)
          .append("cursor (" + Const.CR).append("select" + Const.CR).append(
              myString.toString()).append("from" + Const.CR).append(
              "(values(0))" + Const.CR).append(")," + Const.CR).append(
              port + "," + Const.CR).append("false" + Const.CR).append(
              ")" + Const.CR).append(")" + Const.CR);

      StringBuffer mysql = new StringBuffer(300);
      String qualifiedTableName = "\"" + schemaName + "\"" + ".\"" + tableName
          + "\"";
      ;

      // for MERGE
      if (BaseMessages.getString(PKG,
          "LucidDBStreamingLoaderDialog.Operation.CCombo.Item1").equals(
          operation)) {

        mysql.append("MERGE INTO" + Const.CR).append(
            qualifiedTableName + Const.CR).append("as tgt" + Const.CR).append(
            "USING (" + Const.CR).append(cursor.toString()).append(
            ") as src ON" + Const.CR).append(
            myMatchCondtion.toString() + Const.CR).append(
            "WHEN MATCHED THEN" + Const.CR);

        if (myUpdateStatement.length() != 0) {

          mysql.append("UPDATE SET ");

        }

        mysql.append(myUpdateStatement.toString() + Const.CR).append(
            "WHEN NOT MATCHED THEN" + Const.CR).append(
            "INSERT" + "(" + tgtColumns.toString() + ") values(" + srcColumns
                + ")");
        // for INSERT
      } else if (BaseMessages.getString(PKG,
          "LucidDBStreamingLoaderDialog.Operation.CCombo.Item2").equals(
          operation)) {

        mysql.append("INSERT INTO" + Const.CR).append(
            qualifiedTableName + Const.CR).append(
            "(" + tgtColumns.toString() + ")" + Const.CR).append(
            cursor.toString());
        // for UPDATE
      } else if (BaseMessages.getString(PKG,
          "LucidDBStreamingLoaderDialog.Operation.CCombo.Item3").equals(
          operation)) {

        mysql.append("MERGE INTO" + Const.CR).append(
            qualifiedTableName + Const.CR).append("as tgt" + Const.CR).append(
            "USING (" + Const.CR).append(cursor.toString()).append(
            ") as src ON" + Const.CR).append(
            myMatchCondtion.toString() + Const.CR).append(
            "WHEN MATCHED THEN" + Const.CR);

        if (myUpdateStatement.length() != 0) {

          mysql.append("UPDATE SET ");

        }

        mysql.append(myUpdateStatement.toString() + Const.CR);
      } else if (BaseMessages.getString(PKG,
          "LucidDBStreamingLoaderDialog.Operation.CCombo.Item4").equals(
          operation)) {
        mysql.append(qualifiedTableName + Const.CR).append(cursor.toString());

      }
      // TODO: In future, we need to implement CUSTOM's logic here.
      ret = mysql.toString();
      return ret;

    } catch (Exception ex) {

      throw new KettleStepException(ex);
    }

  }

  public String getselectStmtForCreateTb(RowMetaInterface prev)
      throws KettleStepException {
    String ret = "";

    try {

      StringBuffer myString = new StringBuffer(300);
      if (fieldStreamForKeys != null) {

        for (int i = 0; i < fieldStreamForKeys.length; i++) {

          ValueMetaInterface fieldStream = prev
              .searchValueMeta(fieldStreamForKeys[i]);
          myString.append("cast(null as " + getSQLDataType(fieldStream)
              + ") as \"" + fieldStreamForKeys[i] + "\"," + Const.CR);
        }

      }
      if (fieldStreamForFields != null) {

        for (int i = 0; i < fieldStreamForFields.length; i++) {
          ValueMetaInterface fieldStream = prev
              .searchValueMeta(fieldStreamForFields[i]);
          if ((fieldStreamForFields.length - 1) != i) {
            myString.append("cast(null as " + getSQLDataType(fieldStream)
                + ") as \"" + fieldStreamForFields[i] + "\"," + Const.CR);
          } else {
            myString.append("cast(null as " + getSQLDataType(fieldStream)
                + ") as \"" + fieldStreamForFields[i] + "\"" + Const.CR);
          }
        }
      }
      StringBuffer cursor = new StringBuffer(300);
      cursor.append("select" + Const.CR).append(myString.toString()).append(
          "from " + "(values(0))");
      ret = cursor.toString();
      return ret;

    } catch (Exception ex) {

      throw new KettleStepException(ex);
    }

  }

  private String getSQLDataType(ValueMetaInterface field) {

    String dataType = "";

    int length = field.getLength();
    // TODO: add more data type mapping java <==> SQL
    switch (field.getType()) {
      case ValueMetaInterface.TYPE_STRING:
        dataType = "VARCHAR(" + Integer.toString(length) + ")";
        break;
      case ValueMetaInterface.TYPE_INTEGER:
        dataType = "INT";
        break;
      case ValueMetaInterface.TYPE_DATE:
        dataType = "DATE";
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        dataType = "BOOLEAN";
        break;

    }
    return dataType;

  }

  // TODO: Not know the purpose of this method yet so far.
  public void analyseImpact(List<DatabaseImpact> impact, TransMeta transMeta,
      StepMeta stepMeta, RowMetaInterface prev, String input[],
      String output[], RowMetaInterface info) throws KettleStepException {

  }

  public StepInterface getStep(StepMeta stepMeta,
      StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans) {
    return new LucidDBStreamingLoader(stepMeta, stepDataInterface, cnr,
        transMeta, trans);
  }

  public StepDataInterface getStepData() {
    return new LucidDBStreamingLoaderData();
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    if (databaseMeta != null) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  public RowMetaInterface getRequiredFields(VariableSpace space)
      throws KettleException {
    String realTableName = space.environmentSubstitute(tableName);
    String realSchemaName = space.environmentSubstitute(schemaName);

    if (databaseMeta != null) {
      Database db = new Database(loggingObject, databaseMeta);
      try {
        db.connect();

        if (!Const.isEmpty(realTableName)) {
          String schemaTable = databaseMeta.getQuotedSchemaTableCombination(
              realSchemaName, realTableName);

          // Check if this table exists...
          if (db.checkTableExists(schemaTable)) {
            return db.getTableFields(schemaTable);
          } else {
            throw new KettleException(BaseMessages.getString(PKG,
                "LucidDBStreamingLoaderMeta.Exception.TableNotFound"));
          }
        } else {
          throw new KettleException(BaseMessages.getString(PKG,
              "LucidDBStreamingLoaderMeta.Exception.TableNotSpecified"));
        }
      } catch (Exception e) {
        throw new KettleException(BaseMessages.getString(PKG,
            "LucidDBStreamingLoaderMeta.Exception.ErrorGettingFields"), e);
      } finally {
        db.disconnect();
      }
    } else {
      throw new KettleException(BaseMessages.getString(PKG,
          "LucidDBStreamingLoaderMeta.Exception.ConnectionNotDefined"));
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
  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public boolean[] getInsOrUptFlag() {
    return insOrUptFlag;
  }

  public void setInsOrUptFlag(boolean[] insOrUptFlag) {
    this.insOrUptFlag = insOrUptFlag;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String[] getFieldStreamForFields() {
    return fieldStreamForFields;
  }

  public void setFieldStreamForFields(String[] fieldStreamForFields) {
    this.fieldStreamForFields = fieldStreamForFields;
  }

  public String[] getFieldStreamForKeys() {
    return fieldStreamForKeys;
  }

  public void setFieldStreamForKeys(String[] fieldStreamForKeys) {
    this.fieldStreamForKeys = fieldStreamForKeys;
  }

  public String[] getFieldTableForFields() {
    return fieldTableForFields;
  }

  public void setFieldTableForFields(String[] fieldTableForFields) {
    this.fieldTableForFields = fieldTableForFields;
  }

  public String[] getFieldTableForKeys() {
    return fieldTableForKeys;
  }

  public void setFieldTableForKeys(String[] fieldTableForKeys) {
    this.fieldTableForKeys = fieldTableForKeys;
  }

  public String getCustom_sql() {
    return custom_sql;
  }

  public void setCustom_sql(String custom_sql) {
    this.custom_sql = custom_sql;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getSelectStmt() {
    return selectStmt;
  }

  public void setSelectStmt(String selectStmt) {
    this.selectStmt = selectStmt;
  }

}
