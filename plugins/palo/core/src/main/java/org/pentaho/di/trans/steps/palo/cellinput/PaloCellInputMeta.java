/*
 *   This file is part of PaloKettlePlugin.
 *
 *   PaloKettlePlugin is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PaloKettlePlugin is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with PaloKettlePlugin.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Portions Copyright 2008 Stratebi Business Solutions, S.L.
 *   Portions Copyright 2011 De Bortoli Wines Pty Limited (Australia)
 *   Portions Copyright 2011-2018 Hitachi Vantara
 */

package org.pentaho.di.trans.steps.palo.cellinput;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.palo.core.DimensionField;
import org.pentaho.di.palo.core.PaloHelper;
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
// BACKLOG-38582 Remove specific deprecated steps and job entries from PDI, Please uncomment to enable plugin if needed.
//@Step( id = "PaloCellInput", image = "ui/images/deprecated.svg",
//    i18nPackageName = "org.pentaho.di.trans.steps.palo.cellinput",
//    name = "PaloCellInput.TransName", description = "PaloCellInput.TransDescription",
//    documentationUrl = "http://wiki.pentaho.com/display/EAI/Palo+Cell+Input",
//    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Deprecated" )
public class PaloCellInputMeta extends BaseStepMeta implements StepMetaInterface {
  private DatabaseMeta databaseMeta = null;
  private String cube = "";
  private DimensionField cubeMeasure = new DimensionField( "", "", "" );
  private List<DimensionField> fields = new ArrayList<DimensionField>();

  public PaloCellInputMeta() {
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
  public void setDatabaseMeta( final DatabaseMeta database ) {
    this.databaseMeta = database;
  }

  @Override
  public void loadXML( final Node stepnode, final List<DatabaseMeta> databases, final IMetaStore metaStore )
    throws KettleXMLException {
    readData( stepnode, databases );
  }

  @Override
  public Object clone() {
    PaloCellInputMeta retval = (PaloCellInputMeta) super.clone();
    return retval;
  }

  private void readData( final Node stepnode, final List<? extends SharedObjectInterface> databases )
    throws KettleXMLException {
    try {
      this.databaseMeta = DatabaseMeta.findDatabase( databases, XMLHandler.getTagValue( stepnode, "connection" ) );
      this.cube = XMLHandler.getTagValue( stepnode, "cube" );
      String cubeMeasureName = XMLHandler.getTagValue( stepnode, "cubemeasurename" );
      String cubeMeasureType = XMLHandler.getTagValue( stepnode, "cubemeasuretype" );
      this.cubeMeasure = new DimensionField( "Measure", cubeMeasureName, cubeMeasureType );

      this.fields = new ArrayList<DimensionField>();

      Node levels = XMLHandler.getSubNode( stepnode, "fields" );
      int nrLevels = XMLHandler.countNodes( levels, "field" );

      for ( int i = 0; i < nrLevels; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( levels, "field", i );

        String dimensionName = XMLHandler.getTagValue( fnode, "dimensionname" );
        String fieldName = XMLHandler.getTagValue( fnode, "fieldname" );
        String fieldType = XMLHandler.getTagValue( fnode, "fieldtype" );
        this.fields.add( new DimensionField( dimensionName, fieldName, fieldType ) );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  @Override
  public void setDefault() {
  }

  @Override
  public void getFields( final RowMetaInterface row, final String origin, final RowMetaInterface[] info,
      final StepMeta nextStep, final VariableSpace space, Repository repository, IMetaStore metaStore )
    throws KettleStepException {
    if ( databaseMeta == null ) {
      throw new KettleStepException( "There is no Palo database server connection defined" );
    }
    final PaloHelper helper = new PaloHelper( databaseMeta, DefaultLogLevel.getLogLevel() );
    try {
      helper.connect();
      try {
        final RowMetaInterface rowMeta = helper.getCellRowMeta( this.cube, this.fields, this.cubeMeasure );
        row.addRowMeta( rowMeta );
      } finally {
        helper.disconnect();
      }
    } catch ( Exception e ) {
      throw new KettleStepException( e );
    }
  }

  @Override
  public String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append( "    " ).append(
        XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "cube", this.cube ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "cubemeasurename", this.cubeMeasure.getFieldName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "cubemeasuretype", this.cubeMeasure.getFieldType() ) );

    retval.append( "    <fields>" ).append( Const.CR );
    for ( DimensionField field : this.fields ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "dimensionname", field.getDimensionName() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "fieldname", field.getFieldName() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "fieldtype", field.getFieldType() ) );
      retval.append( "      </field>" ).append( Const.CR );
    }
    retval.append( "    </fields>" ).append( Const.CR );
    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId idStep, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      this.databaseMeta = rep.loadDatabaseMetaFromStepAttribute( idStep, "connection", databases );
      this.cube = rep.getStepAttributeString( idStep, "cube" );
      String cubeMeasureName = rep.getStepAttributeString( idStep, "cubemeasurename" );
      String cubeMeasureType = rep.getStepAttributeString( idStep, "cubemeasuretype" );
      this.cubeMeasure = new DimensionField( "Measure", cubeMeasureName, cubeMeasureType );

      int nrFields = rep.countNrStepAttributes( idStep, "dimensionname" );

      for ( int i = 0; i < nrFields; i++ ) {
        String dimensionName = rep.getStepAttributeString( idStep, i, "dimensionname" );
        String fieldName = rep.getStepAttributeString( idStep, i, "fieldname" );
        String fieldType = rep.getStepAttributeString( idStep, i, "fieldtype" );
        this.fields.add( new DimensionField( dimensionName, fieldName, fieldType ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step" + " information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idTransformation, ObjectId idStep )
    throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( idTransformation, idStep, "connection", databaseMeta );
      rep.saveStepAttribute( idTransformation, idStep, "cube", this.cube );
      rep.saveStepAttribute( idTransformation, idStep, "cubemeasurename", this.cubeMeasure.getFieldName() );
      rep.saveStepAttribute( idTransformation, idStep, "cubemeasuretype", this.cubeMeasure.getFieldType() );

      for ( int i = 0; i < this.fields.size(); i++ ) {
        rep.saveStepAttribute( idTransformation, idStep, i, "dimensionname", this.fields.get( i ).getDimensionName() );
        rep.saveStepAttribute( idTransformation, idStep, i, "fieldname", this.fields.get( i ).getFieldName() );
        rep.saveStepAttribute( idTransformation, idStep, i, "fieldtype", this.fields.get( i ).getFieldType() );
      }

    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for idStep=" + idStep, e );
    }
  }

  @Override
  public void check( final List<CheckResultInterface> remarks, final TransMeta transMeta, final StepMeta stepMeta,
      final RowMetaInterface prev, final String[] input, final String[] output, final RowMetaInterface info,
      VariableSpace space, Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    if ( databaseMeta != null ) {
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, "Connection exists", stepMeta );
      remarks.add( cr );

      final PaloHelper helper = new PaloHelper( databaseMeta, DefaultLogLevel.getLogLevel() );
      try {
        helper.connect();
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, "Connection to database OK", stepMeta );
        remarks.add( cr );

        if ( !Utils.isEmpty( cube ) ) {
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, "The name of the cube is entered", stepMeta );
          remarks.add( cr );
        } else {
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "The name of the cube is missing.", stepMeta );
          remarks.add( cr );
        }
        if ( this.cubeMeasure == null ) {
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Measure field is empty.", stepMeta );
          remarks.add( cr );
        } else {
          if ( Utils.isEmpty( this.cubeMeasure.getFieldName() ) ) {
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Measure field Name is empty.", stepMeta );
            remarks.add( cr );
          }
          if ( Utils.isEmpty( this.cubeMeasure.getFieldType() ) ) {
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Measure field Type is empty.", stepMeta );
            remarks.add( cr );
          }
        }
        if ( this.fields == null || this.fields.size() == 0 ) {
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Cell Input Fields are empty.", stepMeta );
          remarks.add( cr );
        } else {
          for ( DimensionField field : this.fields ) {
            if ( Utils.isEmpty( field.getFieldName() ) ) {
              cr =
                  new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Output field for dimension "
                      + field.getDimensionName() + " is empty.", stepMeta );
              remarks.add( cr );
            }
            if ( Utils.isEmpty( field.getFieldType() ) ) {
              cr =
                  new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Output field type for dimension "
                      + field.getDimensionName() + " is empty.", stepMeta );
              remarks.add( cr );
            }
          }
        }
      } catch ( KettleException e ) {
        cr = new CheckResult(
            CheckResultInterface.TYPE_RESULT_ERROR, "An error occurred: " + e.getMessage(), stepMeta );
        remarks.add( cr );
      } finally {
        helper.disconnect();
      }
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Please select or create a connection to use",
              stepMeta );
      remarks.add( cr );
    }

  }

  @Override
  public StepInterface getStep( final StepMeta stepMeta, final StepDataInterface stepDataInterface, final int cnr,
      final TransMeta transMeta, final Trans trans ) {
    return new PaloCellInput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    try {
      return new PaloCellInputData( this.databaseMeta );
    } catch ( Exception e ) {
      return null;
    }
  }

  @Override
  public DatabaseMeta[] getUsedDatabaseConnections() {
    if ( databaseMeta != null ) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  /**
   * @return the cube name
   */
  public String getCube() {
    return this.cube;
  }

  /**
   * @param cube
   *          the cube name to set
   */
  public void setCube( String cube ) {
    this.cube = cube;
  }

  public List<DimensionField> getFields() {
    return this.fields;
  }

  public void setLevels( List<DimensionField> fields ) {
    this.fields = fields;
  }

  /**
   * @return the cube measure name
   */
  public DimensionField getCubeMeasure() {
    return this.cubeMeasure;
  }

  /**
   * @param cube
   *          the cube name to set
   */
  public void setCubeMeasureName( DimensionField cubeMeasure ) {
    this.cubeMeasure = cubeMeasure;
  }

}
