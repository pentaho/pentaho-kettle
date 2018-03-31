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

package org.pentaho.di.trans.steps.mondrianinput;

import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
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
 * Created on 2-jun-2003
 *
 */
public class MondrianInputMeta extends BaseStepMeta implements StepMetaInterface {
  private DatabaseMeta databaseMeta;
  private String sql;
  private String catalog;
  private String role;

  private boolean variableReplacementActive;

  public MondrianInputMeta() {
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
  public void setDatabaseMeta( DatabaseMeta database ) {
    this.databaseMeta = database;
  }

  /**
   * @return Returns the variableReplacementActive.
   */
  public boolean isVariableReplacementActive() {
    return variableReplacementActive;
  }

  /**
   * @param variableReplacementActive
   *          The variableReplacementActive to set.
   */
  public void setVariableReplacementActive( boolean variableReplacementActive ) {
    this.variableReplacementActive = variableReplacementActive;
  }

  /**
   * @return Returns the sql.
   */
  public String getSQL() {
    return sql;
  }

  /**
   * @param sql
   *          The sql to set.
   */
  public void setSQL( String sql ) {
    this.sql = sql;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public Object clone() {
    MondrianInputMeta retval = (MondrianInputMeta) super.clone();
    return retval;
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      databaseMeta = DatabaseMeta.findDatabase( databases, XMLHandler.getTagValue( stepnode, "connection" ) );
      sql = XMLHandler.getTagValue( stepnode, "sql" );
      catalog = XMLHandler.getTagValue( stepnode, "catalog" );
      role = XMLHandler.getTagValue( stepnode, "role" );
      variableReplacementActive = "Y".equals( XMLHandler.getTagValue( stepnode, "variables_active" ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void setDefault() {
    databaseMeta = null;
    sql =
      "select\n"
        + " {([Gender].[F], [Measures].[Unit Sales]),\n" + "  ([Gender].[M], [Measures].[Store Sales]),\n"
        + "  ([Gender].[F], [Measures].[Unit Sales])} on columns,\n"
        + " CrossJoin([Marital Status].Members,\n" + "           [Product].Children) on rows\n"
        + "from [Sales]";
    variableReplacementActive = false;
  }

  public void getFields( RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    if ( databaseMeta == null ) {
      return; // TODO: throw an exception here
    }

    RowMetaInterface add = null;

    try {
      String mdx = getSQL();
      if ( isVariableReplacementActive() ) {
        mdx = space.environmentSubstitute( mdx );
      }
      MondrianHelper helper = new MondrianHelper( databaseMeta, catalog, mdx, space );
      add = helper.getCachedRowMeta();
      if ( add == null ) {
        helper.openQuery();
        helper.createRectangularOutput();
        add = helper.getOutputRowMeta();
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleStepException( "Unable to get query result for MDX query: " + Const.CR + sql, dbe );
    }

    // Set the origin
    //
    for ( int i = 0; i < add.size(); i++ ) {
      ValueMetaInterface v = add.getValueMeta( i );
      v.setOrigin( origin );
    }

    row.addRowMeta( add );
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    "
      + XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " + XMLHandler.addTagValue( "sql", sql ) );
    retval.append( "    " + XMLHandler.addTagValue( "catalog", catalog ) );
    retval.append( "    " + XMLHandler.addTagValue( "role", role ) );
    retval.append( "    " + XMLHandler.addTagValue( "variables_active", variableReplacementActive ) );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );

      sql = rep.getStepAttributeString( id_step, "sql" );
      catalog = rep.getStepAttributeString( id_step, "catalog" );
      role = rep.getStepAttributeString( id_step, "role" );
      variableReplacementActive = rep.getStepAttributeBoolean( id_step, "variables_active" );
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( id_transformation, id_step, "id_connection", databaseMeta );
      rep.saveStepAttribute( id_transformation, id_step, "sql", sql );
      rep.saveStepAttribute( id_transformation, id_step, "catalog", catalog );
      rep.saveStepAttribute( id_transformation, id_step, "role", role );
      rep.saveStepAttribute( id_transformation, id_step, "variables_active", variableReplacementActive );

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
    CheckResult cr;

    if ( databaseMeta != null ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, "Connection exists", stepMeta );
      remarks.add( cr );

      // TODO: perform lookup to see if it all works fine.
    } else {
      cr =
        new CheckResult(
          CheckResultInterface.TYPE_RESULT_ERROR, "Please select or create a connection to use", stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new MondrianInput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new MondrianData();
  }

  public void analyseImpact( List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, Repository repository,
    IMetaStore metaStore ) throws KettleStepException {
    // you can't really analyze the database impact since it runs on a Mondrian server
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    if ( databaseMeta != null ) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  /**
   * @return the catalog
   */
  public String getCatalog() {
    return catalog;
  }

  /**
   * @param catalog
   *          the catalog to set
   */
  public void setCatalog( String catalog ) {
    this.catalog = catalog;
  }

  public String getRole() {
    return role;
  }

  public void setRole( String role ) {
    this.role = role;
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
      // In case the name of the file comes from previous steps, forget about this!
      //
      if ( Utils.isEmpty( catalog ) ) {
        // From : ${Internal.Transformation.Filename.Directory}/../foo/bar.csv
        // To : /home/matt/test/files/foo/bar.csv
        //
        FileObject fileObject = KettleVFS.getFileObject( space.environmentSubstitute( catalog ), space );

        // If the file doesn't exist, forget about this effort too!
        //
        if ( fileObject.exists() ) {
          // Convert to an absolute path...
          //
          catalog = resourceNamingInterface.nameResource( fileObject, space, true );

          return catalog;
        }
      }
      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

}
