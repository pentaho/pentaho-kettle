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
 *   Portions Copyright 2011 - 2018 Hitachi Vantara
 */

package org.pentaho.di.trans.steps.palo.celloutput;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
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
//@Step( id = "PaloCellOutput", image = "ui/images/deprecated.svg",
//  i18nPackageName = "org.pentaho.di.trans.steps.palo.celloutput", name = "PaloCellOutput.TransName",
//    description = "PaloCellOutput.TransDescription",
//    documentationUrl = "http://wiki.pentaho.com/display/EAI/Palo+Cell+Output",
//    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Deprecated" )
public class PaloCellOutputMeta extends BaseStepMeta implements StepMetaInterface {

  private DatabaseMeta databaseMeta;
  private String cube = "";
  private String measureType = "";
  private String updateMode = "SET";
  private String splashMode = "DISABLED";
  private boolean clearCube;
  private List<DimensionField> fields = new ArrayList<DimensionField>();
  private DimensionField measureField = new DimensionField( "", "", "" );
  private int commitSize = 1000;
  private boolean enableDimensionCache = true;
  private boolean preloadDimensionCache = true;

  public PaloCellOutputMeta() {
    super();
  }

  public void setDefault() {
    if ( updateMode == null ) {
      updateMode = "SET";
    }
    if ( splashMode == null ) {
      splashMode = "DISABLED";
    }
  }

  /**
   * @return Returns the database.
   */
  public final DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * @param database The database to set.
   */
  public final void setDatabaseMeta( final DatabaseMeta database ) {
    this.databaseMeta = database;
  }

  public final void loadXML( final Node stepnode, final List<DatabaseMeta> databases, final IMetaStore metaStore )
    throws KettleXMLException {
    readData( stepnode, databases );
  }

  public final Object clone() {
    PaloCellOutputMeta retval = (PaloCellOutputMeta) super.clone();
    return retval;
  }

  private void readData( final Node stepnode, final List<? extends SharedObjectInterface> databases )
    throws KettleXMLException {
    try {
      databaseMeta = DatabaseMeta.findDatabase( databases, XMLHandler.getTagValue( stepnode, "connection" ) );
      this.cube = XMLHandler.getTagValue( stepnode, "cube" );
      measureType = XMLHandler.getTagValue( stepnode, "measuretype" );
      updateMode = XMLHandler.getTagValue( stepnode, "updateMode" );
      splashMode = XMLHandler.getTagValue( stepnode, "splashMode" );
      clearCube = XMLHandler.getTagValue( stepnode, "clearcube" ).equals( "Y" ) ? true : false;

      /* For backwards compatibility */
      try {
        enableDimensionCache = XMLHandler.getTagValue( stepnode, "enableDimensionCache" ).equals( "Y" ) ? true : false;
        preloadDimensionCache =
          XMLHandler.getTagValue( stepnode, "preloadDimensionCache" ).equals( "Y" ) ? true : false;
        commitSize = Integer.parseInt( XMLHandler.getTagValue( stepnode, "commitSize" ) );

      } catch ( Exception e ) {
        enableDimensionCache = false;
        preloadDimensionCache = false;
        commitSize = 1000;
      }

      this.fields = new ArrayList<DimensionField>();

      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrFields = XMLHandler.countNodes( fields, "field" );
      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        String dimensionName = XMLHandler.getTagValue( fnode, "dimensionname" );
        String fieldName = XMLHandler.getTagValue( fnode, "fieldname" );
        String fieldType = XMLHandler.getTagValue( fnode, "fieldtype" );
        this.fields.add( new DimensionField( dimensionName, fieldName, fieldType ) );
      }

      Node measures = XMLHandler.getSubNode( stepnode, "measures" );
      int nrMeasures = XMLHandler.countNodes( measures, "measure" );

      // v4 code review (matt):
      // --------------------------
      // It's a bit weird to break in the for loop.
      // I think this was done for compatibility reasons.
      //
      for ( int i = 0; i < nrMeasures; ) {
        Node fnode = XMLHandler.getSubNodeByNr( measures, "measure", i );
        String measureName = XMLHandler.getTagValue( fnode, "measurename" );
        String fieldName = XMLHandler.getTagValue( fnode, "measurefieldname" );
        String fieldType = XMLHandler.getTagValue( fnode, "measurefieldtype" );
        this.measureField = new DimensionField( measureName, fieldName, fieldType );
        break;
      }

      setDefault();

    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public final String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append( "    " ).append(
      XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "cube", this.cube ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "measuretype", measureType ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "updateMode", updateMode ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "splashMode", splashMode ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "clearcube", clearCube ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "enableDimensionCache", enableDimensionCache ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "preloadDimensionCache", preloadDimensionCache ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "commitSize", commitSize ) );

    retval.append( "    <fields>" ).append( Const.CR );
    for ( DimensionField field : this.fields ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "dimensionname", field.getDimensionName() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "fieldname", field.getFieldName() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "fieldtype", field.getFieldType() ) );
      retval.append( "      </field>" ).append( Const.CR );
    }
    retval.append( "    </fields>" ).append( Const.CR );

    retval.append( "    <measures>" ).append( Const.CR );
    // The two condition checks for the Measure is a workaround for a bug.
    // If you opened a transformation where the measure was not completely defined
    // then Kettle displayed an error saying the argument can not be null.
    if ( measureField.getDimensionName() != "" ) {
      retval.append( "      <measure>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "measurename", measureField.getDimensionName() ) );
      if ( measureField.getFieldName() == "" ) {
        retval.append( "        " ).append( XMLHandler.addTagValue( "measurefieldname", "CHOOSE FIELD" ) );
      } else {
        retval.append( "        " ).append( XMLHandler.addTagValue( "measurefieldname", measureField.getFieldName() ) );
      }
      retval.append( "        " ).append( XMLHandler.addTagValue( "measurefieldtype", measureField.getFieldType() ) );
      retval.append( "      </measure>" ).append( Const.CR );
    }
    retval.append( "    </measures>" ).append( Const.CR );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId idStep, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      this.databaseMeta = rep.loadDatabaseMetaFromStepAttribute( idStep, "connection", databases );
      this.cube = rep.getStepAttributeString( idStep, "cube" );
      this.measureType = rep.getStepAttributeString( idStep, "measuretype" );
      this.updateMode = rep.getStepAttributeString( idStep, "updateMode" );
      this.splashMode = rep.getStepAttributeString( idStep, "splashMode" );
      this.clearCube = rep.getStepAttributeBoolean( idStep, "clearcube" );

      /* For backwards compatibility */
      try {
        this.enableDimensionCache = rep.getStepAttributeBoolean( idStep, "enableDimensionCache" );
        this.preloadDimensionCache = rep.getStepAttributeBoolean( idStep, "preloadDimensionCache" );
        this.commitSize = (int) rep.getStepAttributeInteger( idStep, "commitSize" );
        if ( this.commitSize <= 0 ) {
          this.setCommitSize( 1000 );
        }
      } catch ( Exception e ) {
        enableDimensionCache = false;
        preloadDimensionCache = false;
        commitSize = 1000;
      }

      int nrFields = rep.countNrStepAttributes( idStep, "dimensionname" );

      for ( int i = 0; i < nrFields; i++ ) {
        String dimensionName = rep.getStepAttributeString( idStep, i, "dimensionname" );
        String fieldName = rep.getStepAttributeString( idStep, i, "fieldname" );
        String fieldType = rep.getStepAttributeString( idStep, i, "fieldtype" );
        this.fields.add( new DimensionField( dimensionName, fieldName, fieldType ) );
      }

      String measureName = rep.getStepAttributeString( idStep, "measurename" );
      String measureFieldName = rep.getStepAttributeString( idStep, "measurefieldname" );
      String measureFieldType = rep.getStepAttributeString( idStep, "measurefieldtype" );
      this.measureField = new DimensionField( measureName, measureFieldName, measureFieldType );

      setDefault();
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idTransformation, ObjectId idStep )
    throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( idTransformation, idStep, "connection", databaseMeta );
      rep.saveStepAttribute( idTransformation, idStep, "cube", this.cube );
      rep.saveStepAttribute( idTransformation, idStep, "measuretype", this.measureType );
      rep.saveStepAttribute( idTransformation, idStep, "updateMode", this.updateMode );
      rep.saveStepAttribute( idTransformation, idStep, "splashMode", this.splashMode );
      rep.saveStepAttribute( idTransformation, idStep, "clearcube", this.clearCube );
      rep.saveStepAttribute( idTransformation, idStep, "preloadDimensionCache", this.preloadDimensionCache );
      rep.saveStepAttribute( idTransformation, idStep, "enableDimensionCache", this.enableDimensionCache );
      rep.saveStepAttribute( idTransformation, idStep, "commitSize", this.commitSize );

      rep.saveStepAttribute( idTransformation, idStep, "measurename", this.measureField.getDimensionName() );
      rep.saveStepAttribute( idTransformation, idStep, "measurefieldname", this.measureField.getFieldName() );
      rep.saveStepAttribute( idTransformation, idStep, "measurefieldtype", this.measureField.getFieldType() );

      for ( int i = 0; i < this.fields.size(); i++ ) {
        rep.saveStepAttribute( idTransformation, idStep, i, "dimensionname", this.fields.get( i ).getDimensionName() );
        rep.saveStepAttribute( idTransformation, idStep, i, "fieldname", this.fields.get( i ).getFieldName() );
        rep.saveStepAttribute( idTransformation, idStep, i, "fieldtype", this.fields.get( i ).getFieldType() );
      }

    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for idStep=" + idStep, e );
    }
  }

  public final void check( final List<CheckResultInterface> remarks, final TransMeta transMeta,
                           final StepMeta stepMeta, final RowMetaInterface prev,
                           final String[] input, final String[] output, final RowMetaInterface info,
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

        if ( !Utils.isEmpty( this.cube ) ) {
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, "The name of the cube is entered", stepMeta );
          remarks.add( cr );
        } else {
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "The name of the cube is missing.", stepMeta );
          remarks.add( cr );
        }

        if ( this.measureField == null || Utils.isEmpty( this.measureField.getFieldName() ) ) {
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Measure field is empty.", stepMeta );
          remarks.add( cr );
        } else {
          if ( Utils.isEmpty( this.measureField.getFieldType() ) ) {
            cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Measure field type is empty.", stepMeta );
            remarks.add( cr );
          }
        }
        if ( this.fields == null || this.fields.size() == 0 ) {
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Cell Output Fields are empty.", stepMeta );
          remarks.add( cr );
        } else {
          for ( DimensionField field : this.fields ) {
            if ( Utils.isEmpty( field.getFieldName() ) ) {
              cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Input field for dimension "
                  + field.getDimensionName() + " is empty.", stepMeta );
              remarks.add( cr );
            }
            if ( Utils.isEmpty( field.getFieldType() ) ) {
              cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Input field type for dimension "
                  + field.getDimensionName() + " is empty.", stepMeta );
              remarks.add( cr );
            }
          }
        }

      } catch ( KettleException e ) {
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "An error occurred: " + e.getMessage(), stepMeta );
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

  public final StepInterface getStep( final StepMeta stepMeta, final StepDataInterface stepDataInterface,
                                      final int cnr, final TransMeta transMeta, final Trans trans ) {

    return new PaloCellOutput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public final StepDataInterface getStepData() {
    try {
      return new PaloCellOutputData( this.databaseMeta );
    } catch ( Exception e ) {
      return null;
    }
  }

  public final DatabaseMeta[] getUsedDatabaseConnections() {
    if ( databaseMeta != null ) {
      return new DatabaseMeta[]{ databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  public String getCube() {
    return this.cube;
  }

  /**
   * @param cube the cube name to set
   */
  public void setCube( String cube ) {
    this.cube = cube;
  }

  public final String getMeasureType() {
    return measureType;
  }

  public final void setMeasureType( String measureType ) {
    this.measureType = measureType;
  }

  public List<DimensionField> getFields() {
    return this.fields;
  }

  public void setLevels( List<DimensionField> fields ) {
    this.fields = fields;
  }

  public DimensionField getMeasure() {
    return this.measureField;
  }

  public void setMeasureField( DimensionField measureField ) {
    this.measureField = measureField;
  }

  public String getUpdateMode() {
    return updateMode;
  }

  public void setUpdateMode( String updateMode ) {
    this.updateMode = updateMode;
  }

  public String getSplashMode() {
    return splashMode;
  }

  public void setSplashMode( String splashMode ) {
    this.splashMode = splashMode;
  }

  public void setClearCube( boolean create ) {
    this.clearCube = create;
  }

  public boolean getClearCube() {
    return this.clearCube;
  }

  public void setCommitSize( int commitSize ) {
    this.commitSize = commitSize;
  }

  public int getCommitSize() {
    return commitSize;
  }

  public void setEnableDimensionCache( boolean enableDimensionCache ) {
    this.enableDimensionCache = enableDimensionCache;
  }

  public boolean getEnableDimensionCache() {
    return enableDimensionCache;
  }

  public void setPreloadDimensionCache( boolean preloadDimensionCache ) {
    this.preloadDimensionCache = preloadDimensionCache;
  }

  public boolean getPreloadDimensionCache() {
    return preloadDimensionCache;
  }

}
