/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.shapefilereader;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.xbaseinput.XBase;
import org.w3c.dom.Node;

/**
 * Contains the meta-data for the ShapeFileReader plugin
 *
 * @author Matt
 * @since 25-apr-2005
 */

@Step( id = "ShapeFileReader",
  image = "ESRI.svg",
  i18nPackageName = "org.pentaho.di.shapefilereader",
  name = "ShapeFileReader.Step.Name",
  description = "ShapeFileReader.Step.Description",
  documentationUrl = "http://wiki.pentaho.com/display/EAI/ESRI+Shapefile+Reader",
  categoryDescription = "Input" )
public class ShapeFileReaderMeta extends BaseStepMeta implements StepMetaInterface {
  /**
   * The filename of the shape file
   */
  public String shapeFilename;

  /**
   * The filename of the DBF file
   */
  public String dbfFilename;

  /**
   * File encoding
   */
  private String encoding;

  public ShapeFileReaderMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the shapeFilename.
   */
  public String getShapeFilename() {
    return shapeFilename;
  }

  /**
   * @param shapeFilename The shapeFilename to set.
   */
  public void setShapeFilename( String shapeFilename ) {
    this.shapeFilename = shapeFilename;
  }

  /**
   * @return Returns the dbfFilename.
   */
  public String getDbfFilename() {
    return dbfFilename;
  }

  /**
   * @param dbfFilename The dbfFilename to set.
   */
  public void setDbfFilename( String dbfFilename ) {
    this.dbfFilename = dbfFilename;
  }

  /**
   * @return Returns the encoding.
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * @param encoding The encoding to set.
   */
  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }


  public void loadXML( Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters )
    throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    ShapeFileReaderMeta retval = (ShapeFileReaderMeta) super.clone();

    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      shapeFilename = XMLHandler.getTagValue( stepnode, "shapefilename" );
      dbfFilename = XMLHandler.getTagValue( stepnode, "dbffilename" );
      encoding = XMLHandler.getTagValue( stepnode, "encoding" );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void setDefault() {
    shapeFilename = "";
    dbfFilename = "";
    encoding = "";
  }

  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
                         VariableSpace space ) throws KettleStepException {

    // The filename...
    ValueMetaInterface filename = new ValueMeta( "filename", ValueMetaInterface.TYPE_STRING );
    filename.setOrigin( name );
    filename.setLength( 255 );
    row.addValueMeta( filename );

    // The file type
    ValueMetaInterface ft = new ValueMeta( "filetype", ValueMetaInterface.TYPE_STRING );
    ft.setLength( 50 );
    ft.setOrigin( name );
    row.addValueMeta( ft );

    // The shape nr
    ValueMetaInterface shnr = new ValueMeta( "shapenr", ValueMetaInterface.TYPE_INTEGER );
    shnr.setOrigin( name );
    row.addValueMeta( shnr );

    // The part nr
    ValueMetaInterface pnr = new ValueMeta( "partnr", ValueMetaInterface.TYPE_INTEGER );
    pnr.setOrigin( name );
    row.addValueMeta( pnr );

    // The part nr
    ValueMetaInterface nrp = new ValueMeta( "nrparts", ValueMetaInterface.TYPE_INTEGER );
    nrp.setOrigin( name );
    row.addValueMeta( nrp );

    // The point nr
    ValueMetaInterface ptnr = new ValueMeta( "pointnr", ValueMetaInterface.TYPE_INTEGER );
    ptnr.setOrigin( name );
    row.addValueMeta( ptnr );

    // The nr of points
    ValueMetaInterface nrpt = new ValueMeta( "nrpointS", ValueMetaInterface.TYPE_INTEGER );
    nrpt.setOrigin( name );
    row.addValueMeta( nrpt );

    // The X coordinate
    ValueMetaInterface x = new ValueMeta( "x", ValueMetaInterface.TYPE_NUMBER );
    x.setOrigin( name );
    row.addValueMeta( x );

    // The Y coordinate
    ValueMetaInterface y = new ValueMeta( "y", ValueMetaInterface.TYPE_NUMBER );
    y.setOrigin( name );
    row.addValueMeta( y );

    // The measure
    ValueMetaInterface m = new ValueMeta( "measure", ValueMetaInterface.TYPE_NUMBER );
    m.setOrigin( name );
    row.addValueMeta( m );

    String dbFilename = getDbfFilename();
    if ( dbFilename != null ) {
      if ( space != null ) {
        dbFilename = space.environmentSubstitute( dbFilename );
        if ( dbFilename.startsWith( "file:" ) ) {
          dbFilename = dbFilename.substring( 5 );
        }
      }

      XBase xbase = new XBase( getLog(), dbFilename );
      try {
        xbase.setDbfFile( dbFilename );
        xbase.open();

        //Set encoding
        if ( StringUtils.isNotBlank( encoding ) ) {
          xbase.getReader().setCharactersetName( encoding );
        }

        RowMetaInterface fields = xbase.getFields();
        for ( int i = 0; i < fields.size(); i++ ) {
          fields.getValueMeta( i ).setOrigin( name );
          row.addValueMeta( fields.getValueMeta( i ) );
        }

      } catch ( Throwable e ) {
        throw new KettleStepException( "Unable to read from DBF file", e );
      } finally {
        xbase.close();
      }
    } else {
      throw new KettleStepException( "Unable to read from DBF file: no filename specfied" );
    }
  }

  public String getXML() {
    String retval = "";

    retval += "    " + XMLHandler.addTagValue( "shapefilename", shapeFilename );
    retval += "    " + XMLHandler.addTagValue( "dbffilename", dbfFilename );
    retval += "    " + XMLHandler.addTagValue( "encoding", encoding );

    return retval;
  }

  public void readRep( Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters )
    throws KettleException {
    try {
      shapeFilename = rep.getStepAttributeString( id_step, "shapefilename" );
      dbfFilename = rep.getStepAttributeString( id_step, "dbffilename" );
      encoding = rep.getStepAttributeString( id_step, "encoding" );
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }

  }

  public void saveRep( Repository rep, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "shapefilename", shapeFilename );
      rep.saveStepAttribute( id_transformation, id_step, "dbffilename", dbfFilename );
      rep.saveStepAttribute( id_transformation, id_step, "encoding", encoding );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev,
                     String[] input, String[] output, RowMetaInterface info ) {
    CheckResult cr;

    // See if we get input...
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, "This step is not expecting nor reading any input", stepinfo );
      remarks.add( cr );
    } else {
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, "Not receiving any input from other steps.", stepinfo );
      remarks.add( cr );
    }

    if ( shapeFilename == null || dbfFilename == null || shapeFilename.length() == 0 || dbfFilename.length() == 0 ) {
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, "No files can be found to read.", stepinfo );
      remarks.add( cr );
    } else {
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, "Both shape file and the DBF file are defined.", stepinfo );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta si, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans disp ) {
    return new ShapeFileReader( si, stepDataInterface, cnr, tr, disp );
  }

  public StepDataInterface getStepData() {
    return new ShapeFileReaderData();
  }
}
