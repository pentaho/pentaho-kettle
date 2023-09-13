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

package org.pentaho.di.trans.steps.palo.diminput;

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
import org.pentaho.di.palo.core.PaloDimensionLevel;
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
//@Step( id = "PaloDimInput", image = "ui/images/deprecated.svg",
//    i18nPackageName = "org.pentaho.di.trans.steps.palo.diminput",
//    name = "PaloDimInput.TransName", description = "PaloDimInput.TransDescription",
//    documentationUrl = "http://wiki.pentaho.com/display/EAI/Palo+Dimension+Input",
//    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Deprecated" )
public class PaloDimInputMeta extends BaseStepMeta implements StepMetaInterface {
  private DatabaseMeta databaseMeta;
  private String dimension = "";
  private boolean baseElementsOnly;
  private List<PaloDimensionLevel> levels = new ArrayList<PaloDimensionLevel>();

  public PaloDimInputMeta() {
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

  public void loadXML( final Node stepnode, final List<DatabaseMeta> databases, final IMetaStore metaStore )
    throws KettleXMLException {
    readData( stepnode, databases );
  }

  public Object clone() {
    PaloDimInputMeta retval = (PaloDimInputMeta) super.clone();
    return retval;
  }

  private void readData( final Node stepnode, final List<? extends SharedObjectInterface> databases )
    throws KettleXMLException {
    try {
      this.databaseMeta = DatabaseMeta.findDatabase( databases, XMLHandler.getTagValue( stepnode, "connection" ) );
      this.dimension = XMLHandler.getTagValue( stepnode, "dimension" );
      baseElementsOnly =
          ( XMLHandler.getTagValue( stepnode, "baseElementsOnly" ) == null ? false : XMLHandler.getTagValue( stepnode,
              "baseElementsOnly" ).equals( "Y" ) ? true : false );

      this.levels = new ArrayList<PaloDimensionLevel>();

      Node levels = XMLHandler.getSubNode( stepnode, "levels" );
      int nrLevels = XMLHandler.countNodes( levels, "level" );

      for ( int i = 0; i < nrLevels; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( levels, "level", i );

        String levelName = XMLHandler.getTagValue( fnode, "levelname" );
        String levelNumber = XMLHandler.getTagValue( fnode, "levelnumber" );
        String fieldName = XMLHandler.getTagValue( fnode, "fieldname" );
        String fieldType = XMLHandler.getTagValue( fnode, "fieldtype" );
        this.levels.add( new PaloDimensionLevel( levelName, Integer.parseInt( levelNumber ), fieldName, fieldType ) );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void setDefault() {
  }

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
        final RowMetaInterface rowMeta =
            helper.getDimensionRowMeta( this.getDimension(), this.getLevels(), this.getBaseElementsOnly() );
        row.addRowMeta( rowMeta );
      } finally {
        helper.disconnect();
      }
    } catch ( Exception e ) {
      throw new KettleStepException( e );
    }
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append( "    " ).append(
        XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "dimension", dimension ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "baseElementsOnly", baseElementsOnly ) );

    retval.append( "    <levels>" ).append( Const.CR );
    for ( PaloDimensionLevel level : levels ) {
      retval.append( "      <level>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "levelname", level.getLevelName() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "levelnumber", level.getLevelNumber() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "fieldname", level.getFieldName() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "fieldtype", level.getFieldType() ) );
      retval.append( "      </level>" ).append( Const.CR );
    }
    retval.append( "    </levels>" ).append( Const.CR );
    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId idStep, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      this.databaseMeta = rep.loadDatabaseMetaFromStepAttribute( idStep, "connection", databases );
      this.dimension = rep.getStepAttributeString( idStep, "dimension" );
      this.baseElementsOnly = rep.getStepAttributeBoolean( idStep, "baseElementsOnly" );

      int nrLevels = rep.countNrStepAttributes( idStep, "levelname" );

      for ( int i = 0; i < nrLevels; i++ ) {
        String levelName = rep.getStepAttributeString( idStep, i, "levelname" );
        int levelNumber = (int) rep.getStepAttributeInteger( idStep, i, "levelnumber" );
        String fieldName = rep.getStepAttributeString( idStep, i, "fieldname" );
        String fieldType = rep.getStepAttributeString( idStep, i, "fieldtype" );
        this.levels.add( new PaloDimensionLevel( levelName, levelNumber, fieldName, fieldType ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idTransformation, ObjectId idStep )
    throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute( idTransformation, idStep, "connection", databaseMeta );
      rep.saveStepAttribute( idTransformation, idStep, "dimension", this.dimension );
      rep.saveStepAttribute( idTransformation, idStep, "baseElementsOnly", this.baseElementsOnly );

      for ( int i = 0; i < levels.size(); i++ ) {
        rep.saveStepAttribute( idTransformation, idStep, i, "levelname", this.levels.get( i ).getLevelName() );
        rep.saveStepAttribute( idTransformation, idStep, i, "levelnumber", this.levels.get( i ).getLevelNumber() );
        rep.saveStepAttribute( idTransformation, idStep, i, "fieldname", this.levels.get( i ).getFieldName() );
        rep.saveStepAttribute( idTransformation, idStep, i, "fieldtype", this.levels.get( i ).getFieldType() );
      }

    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for idStep=" + idStep, e );
    }
  }

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

        if ( !Utils.isEmpty( dimension ) ) {
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_OK, "The name of the dimension is entered", stepMeta );
          remarks.add( cr );
        } else {
          cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "The name of the dimension is missing.",
                  stepMeta );
          remarks.add( cr );
        }

        if ( this.levels == null || this.levels.size() == 0 ) {
          cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Dimension Input Fields are empty.", stepMeta );
          remarks.add( cr );
        } else {
          for ( PaloDimensionLevel level : this.levels ) {
            if ( Utils.isEmpty( level.getLevelName() ) ) {
              cr =
                  new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Level Name for Level "
                      + level.getLevelNumber() + " is empty.", stepMeta );
              remarks.add( cr );
            }
            if ( Utils.isEmpty( level.getFieldName() ) ) {
              cr =
                  new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Output Field Name for Level "
                      + level.getLevelNumber() + " is empty.", stepMeta );
              remarks.add( cr );
            }
            if ( Utils.isEmpty( level.getFieldType() ) ) {
              cr =
                  new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Level Type for Level "
                      + level.getLevelNumber() + " is empty.", stepMeta );
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

  public StepInterface getStep( final StepMeta stepMeta, final StepDataInterface stepDataInterface, final int cnr,
      final TransMeta transMeta, final Trans trans ) {
    return new PaloDimInput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    try {
      return new PaloDimInputData( this.databaseMeta );
    } catch ( Exception e ) {
      return null;
    }
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    if ( databaseMeta != null ) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  /**
   * @return the dimension name
   */
  public String getDimension() {
    return dimension;
  }

  /**
   * @param dimension
   *          the dimension name to set
   */
  public void setDimension( String dimension ) {
    this.dimension = dimension;
  }

  public List<PaloDimensionLevel> getLevels() {
    return this.levels;
  }

  public void setLevels( List<PaloDimensionLevel> levels ) {
    this.levels = levels;
  }

  public void setBaseElementsOnly( boolean baseElementsOnly ) {
    this.baseElementsOnly = baseElementsOnly;
  }

  public boolean getBaseElementsOnly() {
    return baseElementsOnly;
  }
}
