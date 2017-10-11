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
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.imp.rule.ImportRuleInterface;
import org.pentaho.di.imp.rule.ImportValidationFeedback;
import org.pentaho.di.imp.rule.ImportValidationResultType;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.w3c.dom.Node;

public class DatabaseConfigurationImportRule extends BaseImportRule implements ImportRuleInterface {

  private DatabaseMeta databaseMeta;

  public DatabaseConfigurationImportRule() {
    super();
    databaseMeta = null; // not configured.
  }

  public boolean isUnique() {
    return false;
  }

  @Override
  public ImportRuleInterface clone() {
    DatabaseConfigurationImportRule rule = new DatabaseConfigurationImportRule();
    rule.setId( getId() );
    rule.setEnabled( isEnabled() );
    if ( databaseMeta != null ) {
      rule.setDatabaseMeta( (DatabaseMeta) databaseMeta.clone() );
    }
    return rule;
  }

  @Override
  public List<ImportValidationFeedback> verifyRule( Object subject ) {

    List<ImportValidationFeedback> feedback = new ArrayList<ImportValidationFeedback>();

    if ( !isEnabled() ) {
      return feedback;
    }

    if ( databaseMeta == null ) {
      feedback.add( new ImportValidationFeedback(
        this, ImportValidationResultType.ERROR, "This rule contains no database to validate against." ) );
      return feedback;
    }

    DatabaseMeta verify = null;

    if ( subject instanceof HasDatabasesInterface ) {
      HasDatabasesInterface dbs = (HasDatabasesInterface) subject;
      verify = dbs.findDatabase( databaseMeta.getName() );
    } else if ( subject instanceof DatabaseMeta ) {
      // See if this is the database to verify! If it's not, simply ignore it.
      //
      if ( databaseMeta.getName().equals( ( (DatabaseMeta) subject ).getName() ) ) {
        verify = (DatabaseMeta) subject;
      }
    }

    if ( verify == null ) {
      return feedback;
    }

    // Verify the database name if it's non-empty
    //
    if ( !Utils.isEmpty( databaseMeta.getDatabaseName() ) ) {
      if ( !databaseMeta.getDatabaseName().equals( verify.getDatabaseName() ) ) {
        feedback.add( new ImportValidationFeedback(
          this, ImportValidationResultType.ERROR, "The name of the database is not set to the expected value '"
            + databaseMeta.getDatabaseName() + "'." ) );
      }
    }

    // Verify the host name if it's non-empty
    //
    if ( !Utils.isEmpty( databaseMeta.getHostname() ) ) {
      if ( !databaseMeta.getHostname().equals( verify.getHostname() ) ) {
        feedback
          .add( new ImportValidationFeedback(
            this, ImportValidationResultType.ERROR,
            "The host name of the database is not set to the expected value '"
              + databaseMeta.getHostname() + "'." ) );
      }
    }

    // Verify the database port number if it's non-empty
    //
    if ( !Utils.isEmpty( databaseMeta.getDatabasePortNumberString() ) ) {
      if ( !databaseMeta.getDatabasePortNumberString().equals( verify.getDatabasePortNumberString() ) ) {
        feedback.add( new ImportValidationFeedback(
          this, ImportValidationResultType.ERROR,
          "The database port of the database is not set to the expected value '"
            + databaseMeta.getDatabasePortNumberString() + "'." ) );
      }
    }

    // Verify the user name if it's non-empty
    //
    if ( !Utils.isEmpty( databaseMeta.getUsername() ) ) {
      if ( !databaseMeta.getUsername().equals( verify.getUsername() ) ) {
        feedback
          .add( new ImportValidationFeedback(
            this, ImportValidationResultType.ERROR,
            "The username of the database is not set to the expected value '"
              + databaseMeta.getUsername() + "'." ) );
      }
    }

    // Verify the password if it's non-empty
    //
    if ( !Utils.isEmpty( databaseMeta.getPassword() ) ) {
      if ( !databaseMeta.getPassword().equals( verify.getPassword() ) ) {
        feedback.add( new ImportValidationFeedback(
          this, ImportValidationResultType.ERROR,
          "The password of the database is not set to the expected value." ) );
      }
    }

    if ( feedback.isEmpty() ) {
      feedback.add( new ImportValidationFeedback(
        this, ImportValidationResultType.APPROVAL, "The database connection was found and verified." ) );
    }

    return feedback;
  }

  @Override
  public String getXML() {

    StringBuilder xml = new StringBuilder();
    xml.append( XMLHandler.openTag( XML_TAG ) );

    xml.append( super.getXML() ); // id, enabled

    if ( databaseMeta != null ) {
      xml.append( databaseMeta.getXML() );
    }

    xml.append( XMLHandler.closeTag( XML_TAG ) );
    return xml.toString();
  }

  @Override
  public void loadXML( Node ruleNode ) throws KettleException {
    super.loadXML( ruleNode );

    Node connectionNode = XMLHandler.getSubNode( ruleNode, DatabaseMeta.XML_TAG );
    if ( connectionNode != null ) {
      databaseMeta = new DatabaseMeta( connectionNode );
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
  public void setDatabaseMeta( DatabaseMeta databaseMeta ) {
    this.databaseMeta = databaseMeta;
  }
}
