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

package org.pentaho.di.trans.steps.palo.dimoutput;

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

/**
 *
 */
// BACKLOG-38582 Remove specific deprecated steps and job entries from PDI, Please uncomment to enable plugin if needed.
//@Step( id = "PaloDimOutput", image = "ui/images/deprecated.svg",
//    i18nPackageName = "org.pentaho.di.trans.steps.palo.dimoutput", name = "PaloDimOutput.TransName",
//    description = "PaloDimOutput.TransDescription",
//    documentationUrl = "http://wiki.pentaho.com/display/EAI/Palo+Dimension+Output",
//    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Deprecated" )
public class PaloDimOutputMeta extends BaseStepMeta implements StepMetaInterface {

  private DatabaseMeta databaseMeta;
  private String dimension = "";
  private String elementType = "";
  private boolean createNewDimension;
  private boolean clearConsolidations;
  private boolean clearDimension;
  private boolean recreateDimension;
  private boolean enableElementCache = true;
  private boolean preloadElementCache = true;

  private List<PaloDimensionLevel> levels = new ArrayList<PaloDimensionLevel>();

  public PaloDimOutputMeta() {
    super();
  }

  /**
   * @return Returns the database.
   */
  public final DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * @param database
   *          The database to set.
   */
  public final void setDatabaseMeta( final DatabaseMeta database ) {
    this.databaseMeta = database;
  }

  public final void loadXML( final Node stepnode, final List<DatabaseMeta> databases, final IMetaStore metaStore )
    throws KettleXMLException {
    readData( stepnode, databases );
  }

  public final Object clone() {
    PaloDimOutputMeta retval = (PaloDimOutputMeta) super.clone();
    return retval;
  }

  private void readData( final Node stepnode, final List<? extends SharedObjectInterface> databases )
    throws KettleXMLException {
    try {
      databaseMeta = DatabaseMeta.findDatabase( databases, XMLHandler.getTagValue( stepnode, "connection" ) );
      dimension = XMLHandler.getTagValue( stepnode, "dimension" );
      elementType = XMLHandler.getTagValue( stepnode, "elementtype" );
      createNewDimension = XMLHandler.getTagValue( stepnode, "createdimension" ).equals( "Y" ) ? true : false;
      clearDimension = XMLHandler.getTagValue( stepnode, "cleardimension" ).equals( "Y" ) ? true : false;
      clearConsolidations =
          ( XMLHandler.getTagValue( stepnode, "clearconsolidations" ) == null ? false : XMLHandler.getTagValue(
              stepnode, "clearconsolidations" ).equals( "Y" ) ? true : false );
      recreateDimension =
          ( XMLHandler.getTagValue( stepnode, "recreatedimension" ) == null ? false : XMLHandler.getTagValue( stepnode,
              "recreatedimension" ).equals( "Y" ) ? true : false );
      enableElementCache =
          ( XMLHandler.getTagValue( stepnode, "enableElementCache" ) == null ? false : XMLHandler.getTagValue(
              stepnode, "enableElementCache" ).equals( "Y" ) ? true : false );
      preloadElementCache =
          ( XMLHandler.getTagValue( stepnode, "preloadElementCache" ) == null ? false : XMLHandler.getTagValue(
              stepnode, "preloadElementCache" ).equals( "Y" ) ? true : false );

      Node levels = XMLHandler.getSubNode( stepnode, "levels" );
      int nrLevels = XMLHandler.countNodes( levels, "level" );

      for ( int i = 0; i < nrLevels; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( levels, "level", i );

        String levelName = XMLHandler.getTagValue( fnode, "levelname" );
        String levelNumber = XMLHandler.getTagValue( fnode, "levelnumber" );
        String fieldName = XMLHandler.getTagValue( fnode, "fieldname" );
        String fieldType = XMLHandler.getTagValue( fnode, "fieldtype" );
        String consolidationField = XMLHandler.getTagValue( fnode, "consolidationfieldname" );
        this.levels.add( new PaloDimensionLevel( levelName, Integer.parseInt( levelNumber ), fieldName, fieldType,
            consolidationField ) );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void setDefault() {
  }

  public final String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append( "    " ).append(
        XMLHandler.addTagValue( "connection", databaseMeta == null ? "" : databaseMeta.getName() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "dimension", dimension ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "elementtype", elementType ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "createdimension", createNewDimension ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "cleardimension", clearDimension ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "clearconsolidations", clearConsolidations ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "recreatedimension", recreateDimension ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "enableElementCache", enableElementCache ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "preloadElementCache", preloadElementCache ) );

    retval.append( "    <levels>" ).append( Const.CR );
    for ( PaloDimensionLevel level : levels ) {
      retval.append( "      <level>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "levelname", level.getLevelName() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "levelnumber", level.getLevelNumber() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "fieldname", level.getFieldName() ) );
      retval.append( "        " ).append(
          XMLHandler.addTagValue( "consolidationfieldname", level.getConsolidationFieldName() ) );
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
      this.elementType = rep.getStepAttributeString( idStep, "elementtype" );
      this.createNewDimension = rep.getStepAttributeBoolean( idStep, "createdimension" );
      this.clearDimension = rep.getStepAttributeBoolean( idStep, "cleardimension" );
      this.clearConsolidations = rep.getStepAttributeBoolean( idStep, "clearconsolidations" );
      this.recreateDimension = rep.getStepAttributeBoolean( idStep, "recreatedimension" );
      this.enableElementCache = rep.getStepAttributeBoolean( idStep, "enableElementCache" );
      this.preloadElementCache = rep.getStepAttributeBoolean( idStep, "preloadElementCache" );

      int nrLevels = rep.countNrStepAttributes( idStep, "levelname" );

      for ( int i = 0; i < nrLevels; i++ ) {
        String levelName = rep.getStepAttributeString( idStep, i, "levelname" );
        int levelNumber = (int) rep.getStepAttributeInteger( idStep, i, "levelnumber" );
        String fieldName = rep.getStepAttributeString( idStep, i, "fieldname" );
        String fieldType = rep.getStepAttributeString( idStep, i, "fieldtype" );
        String consolidationField = rep.getStepAttributeString( idStep, i, "consolidationfieldname" );
        this.levels.add( new PaloDimensionLevel( levelName, levelNumber, fieldName, fieldType, consolidationField ) );
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
      rep.saveStepAttribute( idTransformation, idStep, "elementtype", this.elementType );
      rep.saveStepAttribute( idTransformation, idStep, "createdimension", this.createNewDimension );
      rep.saveStepAttribute( idTransformation, idStep, "cleardimension", this.clearDimension );
      rep.saveStepAttribute( idTransformation, idStep, "clearconsolidations", this.clearConsolidations );
      rep.saveStepAttribute( idTransformation, idStep, "recreatedimension", this.recreateDimension );
      rep.saveStepAttribute( idTransformation, idStep, "enableElementCache", this.enableElementCache );
      rep.saveStepAttribute( idTransformation, idStep, "preloadElementCache", this.preloadElementCache );

      for ( int i = 0; i < levels.size(); i++ ) {
        rep.saveStepAttribute( idTransformation, idStep, i, "levelname", this.levels.get( i ).getLevelName() );
        rep.saveStepAttribute( idTransformation, idStep, i, "levelnumber", this.levels.get( i ).getLevelNumber() );
        rep.saveStepAttribute( idTransformation, idStep, i, "fieldname", this.levels.get( i ).getFieldName() );
        rep.saveStepAttribute( idTransformation, idStep, i, "fieldtype", this.levels.get( i ).getFieldType() );
        rep.saveStepAttribute( idTransformation, idStep, i, "consolidationfieldname", this.levels.get( i )
            .getConsolidationFieldName() );
      }

    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for idStep=" + idStep, e );
    }
  }

  public final void check( final List<CheckResultInterface> remarks, final TransMeta transMeta,
      final StepMeta stepMeta, final RowMetaInterface prev, final String[] input, final String[] output,
      final RowMetaInterface info, VariableSpace space, Repository repository, IMetaStore metaStore ) {

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
          cr = new CheckResult(
              CheckResultInterface.TYPE_RESULT_ERROR, "Dimension Output Fields are empty.", stepMeta );
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
                  new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, "Input Field Name for Level "
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

  public final StepInterface getStep( final StepMeta stepMeta, final StepDataInterface stepDataInterface,
      final int cnr, final TransMeta transMeta, final Trans trans ) {

    return new PaloDimOutput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public final StepDataInterface getStepData() {
    try {
      return new PaloDimOutputData( this.databaseMeta );
    } catch ( Exception e ) {
      return null;
    }
  }

  public final DatabaseMeta[] getUsedDatabaseConnections() {
    if ( databaseMeta != null ) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  /**
   * @return the dimension name
   */
  public final String getDimension() {
    return dimension;
  }

  /**
   * @param dimension
   *          the dimension name to set
   */
  public final void setDimension( String dimension ) {
    this.dimension = dimension;
  }

  public final String getElementType() {
    return elementType;
  }

  public final void setElementType( String elementType ) {
    this.elementType = elementType;
  }

  public List<PaloDimensionLevel> getLevels() {
    return this.levels;
  }

  public void setLevels( List<PaloDimensionLevel> levels ) {
    this.levels = levels;
  }

  public void setCreateNewDimension( boolean create ) {
    this.createNewDimension = create;
  }

  public boolean getCreateNewDimension() {
    return this.createNewDimension;
  }

  public void setClearDimension( boolean create ) {
    this.clearDimension = create;
  }

  public boolean getClearDimension() {
    return this.clearDimension;
  }

  public void setClearConsolidations( boolean clear ) {
    this.clearConsolidations = clear;
  }

  public boolean getClearConsolidations() {
    return this.clearConsolidations;
  }

  public void setRecreateDimension( boolean recreateDimension ) {
    this.recreateDimension = recreateDimension;
  }

  public boolean getRecreateDimension() {
    return recreateDimension;
  }

  public void setEnableElementCache( boolean enableElementCache ) {
    this.enableElementCache = enableElementCache;
  }

  public boolean getEnableElementCache() {
    return enableElementCache;
  }

  public void setPreloadElementCache( boolean preloadElementCache ) {
    this.preloadElementCache = preloadElementCache;
  }

  public boolean getPreloadElementCache() {
    return preloadElementCache;
  }
}
