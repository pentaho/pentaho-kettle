/*
 *   This software is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This software is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this software.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Copyright 2011 De Bortoli Wines Pty Limited (Australia)
 *   Portions Copyright 2011-2018 Hitachi Vantara
 */

package org.pentaho.di.trans.steps.openerp.objectdelete;

import java.util.List;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

@Step( id = "OpenERPObjectDelete", image = "ui/images/deprecated.svg",
  i18nPackageName = "org.pentaho.di.trans.steps.openerp.objectdelete", name = "OpenERPObjectDelete.TransName",
  description = "OpenERPObjectDelete.TransDescription", documentationUrl = "http://wiki.pentaho.com/display/EAI/OpenERP+Object+Delete",
  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Deprecated" )
public class OpenERPObjectDeleteMeta extends BaseStepMeta implements StepMetaInterface {

  private DatabaseMeta databaseMeta;
  private String modelName;
  private int commitBatchSize = 1000;
  private String idFieldName = "";

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
                                TransMeta transMeta, Trans trans ) {
    return new OpenERPObjectDelete( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    try {
      return new OpenERPObjectDeleteData( this.databaseMeta );
    } catch ( Exception e ) {
      return null;
    }
  }

  public final String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append( "    " ).append(
      XMLHandler.addTagValue( "connection", this.databaseMeta == null ? "" : this.databaseMeta.getName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "modelName", this.modelName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "readBatchSize", this.commitBatchSize ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "idFieldName", this.idFieldName ) );

    return retval.toString();
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId idStep, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      this.databaseMeta = rep.loadDatabaseMetaFromStepAttribute( idStep, "connection", databases );
      this.modelName = rep.getStepAttributeString( idStep, "modelName" );
      this.commitBatchSize = Integer.parseInt( rep.getStepAttributeString( idStep, "readBatchSize" ) );
      this.idFieldName = rep.getStepAttributeString( idStep, "idFieldName" );

    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idTransformation, ObjectId idStep )
    throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( idTransformation, idStep, "connection", this.databaseMeta );
      rep.saveStepAttribute( idTransformation, idStep, "modelName", this.modelName );
      rep.saveStepAttribute( idTransformation, idStep, "readBatchSize", this.commitBatchSize );
      rep.saveStepAttribute( idTransformation, idStep, "idFieldName", this.idFieldName );

    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for idStep=" + idStep, e );
    }
  }

  @Override
  public void setDefault() {
    // TODO Auto-generated method stub

  }

  private void readData( final Node stepnode, final List<? extends SharedObjectInterface> databases )
    throws KettleXMLException {
    try {
      this.databaseMeta = DatabaseMeta.findDatabase( databases, XMLHandler.getTagValue( stepnode, "connection" ) );
      this.modelName = XMLHandler.getTagValue( stepnode, "modelName" );
      this.commitBatchSize = Integer.parseInt( XMLHandler.getTagValue( stepnode, "readBatchSize" ) );
      this.idFieldName = XMLHandler.getTagValue( stepnode, "idFieldName" );

    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public final void setDatabaseMeta( final DatabaseMeta database ) {
    this.databaseMeta = database;
  }

  public final DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  public void setModelName( String modelName ) {
    this.modelName = modelName;
  }

  public String getModelName() {
    return modelName;
  }

  public void setCommitBatchSize( int commitBatchSize ) {
    this.commitBatchSize = commitBatchSize;
  }

  public int getCommitBatchSize() {
    return commitBatchSize;
  }

  public void setIdFieldName( String idFieldName ) {
    this.idFieldName = idFieldName;
  }

  public String getIdFieldName() {
    return ( idFieldName == null ? "" : idFieldName );
  }
}
