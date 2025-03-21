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


package org.pentaho.di.trans.steps.cubeinput;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
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
@Step( id = "CubeInput", i18nPackageName = "org.pentaho.di.trans.steps.cubeinput", name = "CubeInput.Name",
  description = "CubeInput.Description",
  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Input" )
public class CubeInputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = CubeInputMeta.class; // for i18n purposes, needed by Translator2!!

  private String filename;
  private String rowLimit;
  private boolean addfilenameresult;

  public CubeInputMeta() {
    super(); // allocate BaseStepMeta
  }

  @Override public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore )
    throws KettleXMLException {
    readData( stepnode );
  }

  /**
   * @return Returns the filename.
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @param filename
   *          The filename to set.
   */
  public void setFilename( String filename ) {
    this.filename = filename;
  }

  /**
   * @param rowLimit
   *          The rowLimit to set.
   */
  @Deprecated
  public void setRowLimit( int rowLimit ) {
    this.rowLimit = String.valueOf( rowLimit );
  }

  /**
   * @param rowLimit
   *          The rowLimit to set.
   */
  public void setRowLimit( String rowLimit ) {
    this.rowLimit = rowLimit;
  }

  /**
   * @return Returns the rowLimit.
   */
  public String getRowLimit() {
    return rowLimit;
  }

  /**
   * @return Returns the addfilenameresult.
   */
  public boolean isAddResultFile() {
    return addfilenameresult;
  }

  /**
   * @param addfilenameresult
   *          The addfilenameresult to set.
   */
  public void setAddResultFile( boolean addfilenameresult ) {
    this.addfilenameresult = addfilenameresult;
  }

  @Override public Object clone() {
    CubeInputMeta retval = (CubeInputMeta) super.clone();
    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      filename = XMLHandler.getTagValue( stepnode, "file", "name" );
      rowLimit = XMLHandler.getTagValue( stepnode, "limit" );
      addfilenameresult = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "addfilenameresult" ) );

    } catch ( Exception e ) {
      throw new KettleXMLException(
        BaseMessages.getString( PKG, "CubeInputMeta.Exception.UnableToLoadStepInfo" ), e );
    }
  }

  @Override public void setDefault() {
    filename = "file";
    rowLimit = "0";
    addfilenameresult = false;
  }

  @Override public void getFields( Bowl bowl, RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
                                   VariableSpace space, Repository repository,
                                   IMetaStore metaStore ) throws KettleStepException {
    GZIPInputStream fis = null;
    DataInputStream dis = null;
    try {
      InputStream is = KettleVFS.getInstance( bowl ).getInputStream( space.environmentSubstitute( filename ), space );
      fis = new GZIPInputStream( is );
      dis = new DataInputStream( fis );

      RowMetaInterface add = new RowMeta( dis );
      for ( int i = 0; i < add.size(); i++ ) {
        add.getValueMeta( i ).setOrigin( name );
      }
      r.mergeRowMeta( add );
    } catch ( KettleFileException kfe ) {
      throw new KettleStepException(
        BaseMessages.getString( PKG, "CubeInputMeta.Exception.UnableToReadMetaData" ), kfe );
    } catch ( IOException e ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "CubeInputMeta.Exception.ErrorOpeningOrReadingCubeFile" ), e );
    } finally {
      try {
        if ( fis != null ) {
          fis.close();
        }
        if ( dis != null ) {
          dis.close();
        }
      } catch ( IOException ioe ) {
        throw new KettleStepException( BaseMessages.getString(
          PKG, "CubeInputMeta.Exception.UnableToCloseCubeFile" ), ioe );
      }
    }
  }

  @Override public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( "    <file>" ).append( Const.CR );
    retval.append( "      " ).append( XMLHandler.addTagValue( "name", filename ) );
    retval.append( "    </file>" ).append( Const.CR );
    retval.append( "    " ).append( XMLHandler.addTagValue( "limit", rowLimit ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "addfilenameresult", addfilenameresult ) );

    return retval.toString();
  }

  @Override public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      filename = rep.getStepAttributeString( id_step, "file_name" );
      try {
        rowLimit = rep.getStepAttributeString( id_step, "limit" );
      } catch ( KettleException readOldAttributeType ) {
        // PDI-12897
        rowLimit = String.valueOf( rep.getStepAttributeInteger( id_step, "limit" ) );
      }
      addfilenameresult = rep.getStepAttributeBoolean( id_step, "addfilenameresult" );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "CubeInputMeta.Exception.UnexpectedErrorWhileReadingStepInfo" ), e );
    }
  }

  @Override public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "file_name", filename );
      rep.saveStepAttribute( id_transformation, id_step, "limit", rowLimit );
      rep.saveStepAttribute( id_transformation, id_step, "addfilenameresult", addfilenameresult );

    } catch ( KettleException e ) {
      throw new KettleException( BaseMessages.getString( PKG, "CubeInputMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  @Override public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
                               RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info,
                               VariableSpace space, Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    cr =
      new CheckResult( CheckResult.TYPE_RESULT_COMMENT, BaseMessages.getString(
        PKG, "CubeInputMeta.CheckResult.FileSpecificationsNotChecked" ), stepMeta );
    remarks.add( cr );
  }

  @Override public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
                                          Trans trans ) {
    return new CubeInput( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override public StepDataInterface getStepData() {
    return new CubeInputData();
  }

  /**
   * @param executionBowl
   *          For file access
   * @param globalManagementBowl
   *          if needed for access to the current "global" (System or Repository) level config for export. If null, no
   *          global config will be exported.
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
  @Override
  public String exportResources( Bowl executionBowl, Bowl globalManagementBowl, VariableSpace space,
      Map<String, ResourceDefinition> definitions, ResourceNamingInterface namingInterface,
      Repository repository, IMetaStore metaStore ) throws KettleException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the filename from relative to absolute by grabbing the file object...
      //
      // From : ${Internal.Transformation.Filename.Directory}/../foo/bar.data
      // To : /home/matt/test/files/foo/bar.data
      //
      FileObject fileObject = KettleVFS.getInstance( executionBowl )
        .getFileObject( space.environmentSubstitute( filename ), space );

      // If the file doesn't exist, forget about this effort too!
      //
      if ( fileObject.exists() ) {
        // Convert to an absolute path...
        //
        filename = namingInterface.nameResource( fileObject, space, true );

        return filename;
      }
      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

}
