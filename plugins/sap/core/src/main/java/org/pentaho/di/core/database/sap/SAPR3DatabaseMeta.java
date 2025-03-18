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


package org.pentaho.di.core.database.sap;

import org.eclipse.jface.wizard.WizardPage;
import org.pentaho.di.core.database.BaseDatabaseMeta;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.plugins.DatabaseMetaPlugin;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.ui.core.database.wizard.CreateDatabaseWizardPageSAPR3;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.wizard.WizardPageFactory;

import java.util.Map;

import static org.pentaho.di.core.util.Utils.setStringValueFromMap;

/**
 * Contains SAP ERP system specific information through static final members
 *
 * @author Matt
 * @since 03-07-2005
 */

@DatabaseMetaPlugin( type = "SAPR3", typeDescription = "SAP ERP System" )
public class SAPR3DatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface, WizardPageFactory {
  public static final String ATTRIBUTE_SAP_LANGUAGE = "SAPLanguage";
  public static final String ATTRIBUTE_SAP_SYSTEM_NUMBER = "SAPSystemNumber";
  public static final String ATTRIBUTE_SAP_CLIENT = "SAPClient";
  public static final String SAPR3 = "SAPR3";

  @Override
  public int[] getAccessTypeList() {
    return new int[] { DatabaseMeta.TYPE_ACCESS_PLUGIN };
  }

  @Override
  public int getDefaultDatabasePort() {
    return -1;
  }

  /**
   * @return Whether or not the database can use auto increment type of fields (pk)
   */
  @Override
  public boolean supportsAutoInc() {
    return false;
  }

  @Override
  public String getDriverClass() {
    return null;
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    return null;
  }

  /**
   * @return true if the database supports bitmap indexes
   */
  @Override
  public boolean supportsBitmapIndex() {
    return false;
  }

  /**
   * @return true if the database supports synonyms
   */
  @Override
  public boolean supportsSynonyms() {
    return false;
  }

  /**
   * Generates the SQL statement to add a column to the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param useAutoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to add a column to the specified table
   */
  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    return null;
  }

  /**
   * Generates the SQL statement to modify a column in the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param useAutoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to modify a column in the specified table
   */
  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    return null;
  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean useAutoinc,
                                    boolean addFieldName, boolean addCr ) {
    return null;
  }

  @Override
  public String[] getReservedWords() {
    return null;
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] {};
  }

  @Override
  public String getDatabaseFactoryName() {
    return org.pentaho.di.trans.steps.sapinput.sap.SAPConnectionFactory.class.getName();
  }

  /**
   * @return true if this is a relational database you can explore. Return false for SAP, PALO, etc.
   */
  @Override
  public boolean isExplorable() {
    return false;
  }

  @Override
  public boolean canTest() {
    return false;
  }

  @Override
  public boolean requiresName() {
    return false;
  }

  @Override public WizardPage createWizardPage( PropsUI props, DatabaseMeta info ) {
    return new CreateDatabaseWizardPageSAPR3( SAPR3, props, info );
  }

  @Override
  public void setConnectionSpecificInfoFromAttributes( Map<String, String> attributes ) {
    addAttribute( ATTRIBUTE_SAP_LANGUAGE, setStringValueFromMap( attributes, ATTRIBUTE_SAP_LANGUAGE ) );
    addAttribute( ATTRIBUTE_SAP_SYSTEM_NUMBER, setStringValueFromMap( attributes, ATTRIBUTE_SAP_SYSTEM_NUMBER ) );
    addAttribute( ATTRIBUTE_SAP_CLIENT, setStringValueFromMap( attributes, ATTRIBUTE_SAP_CLIENT ) );
  }
}
