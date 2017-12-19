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

package org.pentaho.di.imp.rules;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.imp.rule.ImportRuleInterface;
import org.pentaho.di.imp.rule.ImportValidationFeedback;
import org.pentaho.di.imp.rule.ImportValidationResultType;
import org.pentaho.di.job.JobMeta;
import org.w3c.dom.Node;

/**
 * This rule verifies that a job contains a certain job log table configuration.
 *
 * @author matt
 *
 */
public class JobHasJobLogConfiguredImportRule extends BaseImportRule implements ImportRuleInterface {

  private String schemaName;
  private String tableName;
  private String connectionName;

  public JobHasJobLogConfiguredImportRule() {
    super();
  }

  @Override
  public List<ImportValidationFeedback> verifyRule( Object subject ) {

    List<ImportValidationFeedback> feedback = new ArrayList<ImportValidationFeedback>();

    if ( !isEnabled() ) {
      return feedback;
    }
    if ( !( subject instanceof JobMeta ) ) {
      return feedback;
    }

    JobMeta jobMeta = (JobMeta) subject;
    JobLogTable jobLogTable = jobMeta.getJobLogTable();

    if ( !jobLogTable.isDefined() ) {
      feedback.add( new ImportValidationFeedback(
        this, ImportValidationResultType.ERROR, "The logging table is not defined" ) );
    } else {
      if ( !Utils.isEmpty( schemaName ) ) {
        if ( schemaName.equals( jobLogTable.getSchemaName() ) ) {
          feedback.add( new ImportValidationFeedback(
            this, ImportValidationResultType.APPROVAL, "The schema name is set to: " + schemaName ) );
        } else {
          feedback.add( new ImportValidationFeedback(
            this, ImportValidationResultType.ERROR, "The schema name is not set to: " + schemaName ) );
        }
      }

      if ( !Utils.isEmpty( tableName ) ) {
        if ( tableName.equals( jobLogTable.getTableName() ) ) {
          feedback.add( new ImportValidationFeedback(
            this, ImportValidationResultType.APPROVAL, "The table name is set to: " + tableName ) );
        } else {
          feedback.add( new ImportValidationFeedback(
            this, ImportValidationResultType.ERROR, "The table name is not set to: " + tableName ) );
        }
      }

      if ( !Utils.isEmpty( connectionName ) ) {
        if ( connectionName.equals( jobLogTable.getDatabaseMeta().getName() ) ) {
          feedback.add( new ImportValidationFeedback(
            this, ImportValidationResultType.APPROVAL, "The database connection used for logging is: "
              + connectionName ) );
        } else {
          feedback.add( new ImportValidationFeedback(
            this, ImportValidationResultType.ERROR, "The database connection used for logging is not: "
              + connectionName ) );
        }
      }

      if ( feedback.isEmpty() ) {
        feedback.add( new ImportValidationFeedback(
          this, ImportValidationResultType.APPROVAL, "The logging table is correctly defined" ) );
      }
    }

    return feedback;
  }

  @Override
  public String getXML() {

    StringBuilder xml = new StringBuilder();
    xml.append( XMLHandler.openTag( XML_TAG ) );

    xml.append( super.getXML() ); // id, enabled

    xml.append( XMLHandler.addTagValue( "schema_name", schemaName ) );
    xml.append( XMLHandler.addTagValue( "table_name", tableName ) );
    xml.append( XMLHandler.addTagValue( "connection_name", connectionName ) );

    xml.append( XMLHandler.closeTag( XML_TAG ) );
    return xml.toString();
  }

  @Override
  public void loadXML( Node ruleNode ) throws KettleException {
    super.loadXML( ruleNode );

    schemaName = XMLHandler.getTagValue( ruleNode, "schema_name" );
    tableName = XMLHandler.getTagValue( ruleNode, "table_name" );
    connectionName = XMLHandler.getTagValue( ruleNode, "connection_name" );
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

  /**
   * @return the tableName
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @param tableName
   *          the tableName to set
   */
  public void setTableName( String tableName ) {
    this.tableName = tableName;
  }

  /**
   * @return the connectionName
   */
  public String getConnectionName() {
    return connectionName;
  }

  /**
   * @param connectionName
   *          the connectionName to set
   */
  public void setConnectionName( String connectionName ) {
    this.connectionName = connectionName;
  }
}
