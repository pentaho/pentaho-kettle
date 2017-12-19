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

package org.pentaho.di.trans.steps.sort;

import java.io.File;
import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.injection.AfterInjection;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
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
 * Created on 02-jun-2003
 */
@InjectionSupported( localizationPrefix = "SortRows.Injection.", groups = { "FIELDS" } )
public class SortRowsMeta extends BaseStepMeta implements StepMetaInterface, Serializable {
  private static final long serialVersionUID = -9075883720765645655L;
  private static Class<?> PKG = SortRowsMeta.class; // for i18n purposes, needed by Translator2!!

  /** order by which fields? */
  @Injection( name = "NAME", group = "FIELDS" )
  private String[] fieldName;

  /** false : descending, true=ascending */
  @Injection( name = "SORT_ASCENDING", group = "FIELDS" )
  private boolean[] ascending;

  /** false : case insensitive, true=case sensitive */
  @Injection( name = "IGNORE_CASE", group = "FIELDS" )
  private boolean[] caseSensitive;

  /** false : collator disabeld, true=collator enabled */
  @Injection( name = "COLLATOR_ENABLED", group = "FIELDS" )
  private boolean[] collatorEnabled;

  // collator strength, 0,1,2,3
  @Injection( name = "COLLATOR_STRENGTH", group = "FIELDS" )
  private int[] collatorStrength;

  /** false : not a presorted field, true=presorted field */
  @Injection( name = "PRESORTED", group = "FIELDS" )
  private boolean[] preSortedField;
  private List<String> groupFields;

  /** Directory to store the temp files */
  @Injection( name = "SORT_DIRECTORY" )
  private String directory;

  /** Temp files prefix... */
  @Injection( name = "SORT_FILE_PREFIX" )
  private String prefix;

  /** The sort size: number of rows sorted and kept in memory */
  @Injection( name = "SORT_SIZE_ROWS" )
  private String sortSize;

  /** The free memory limit in percentages in case we don't use the sort size */
  @Injection( name = "FREE_MEMORY_TRESHOLD" )
  private String freeMemoryLimit;

  /** only pass unique rows to the output stream(s) */
  @Injection( name = "ONLY_PASS_UNIQUE_ROWS" )
  private boolean onlyPassingUniqueRows;

  /**
   * Compress files: if set to true, temporary files are compressed, thus reducing I/O at the cost of slightly higher
   * CPU usage
   */
  @Injection( name = "COMPRESS_TEMP_FILES" )
  private boolean compressFiles;

  /** The variable to use to set the compressFiles option boolean */
  private String compressFilesVariable;

  public SortRowsMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the ascending.
   */
  public boolean[] getAscending() {
    return ascending;
  }

  /**
   * @param ascending
   *          The ascending to set.
   */
  public void setAscending( boolean[] ascending ) {
    this.ascending = ascending;
  }

  /**
   * @return Returns the directory.
   */
  public String getDirectory() {
    return directory;
  }

  /**
   * @param directory
   *          The directory to set.
   */
  public void setDirectory( String directory ) {
    this.directory = directory;
  }

  /**
   * @return Returns the fieldName.
   */
  public String[] getFieldName() {
    return fieldName;
  }

  /**
   * @param fieldName
   *          The fieldName to set.
   */
  public void setFieldName( String[] fieldName ) {
    this.fieldName = fieldName;
  }

  /**
   * @return Returns the prefix.
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * @param prefix
   *          The prefix to set.
   */
  public void setPrefix( String prefix ) {
    this.prefix = prefix;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int nrfields ) {
    fieldName = new String[nrfields]; // order by
    ascending = new boolean[nrfields];
    caseSensitive = new boolean[nrfields];
    collatorEnabled = new boolean[nrfields];
    collatorStrength = new int[nrfields];
    preSortedField = new boolean[nrfields];
    groupFields = null;
  }

  @Override
  public Object clone() {
    SortRowsMeta retval = (SortRowsMeta) super.clone();

    int nrfields = fieldName.length;

    retval.allocate( nrfields );
    System.arraycopy( fieldName, 0, retval.fieldName, 0, nrfields );
    System.arraycopy( ascending, 0, retval.ascending, 0, nrfields );
    System.arraycopy( caseSensitive, 0, retval.caseSensitive, 0, nrfields );
    System.arraycopy( collatorEnabled, 0, retval.collatorEnabled, 0, nrfields );
    System.arraycopy( collatorStrength, 0, retval.collatorStrength, 0, nrfields );
    System.arraycopy( preSortedField, 0, retval.preSortedField, 0, nrfields );

    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      directory = XMLHandler.getTagValue( stepnode, "directory" );
      prefix = XMLHandler.getTagValue( stepnode, "prefix" );
      sortSize = XMLHandler.getTagValue( stepnode, "sort_size" );
      freeMemoryLimit = XMLHandler.getTagValue( stepnode, "free_memory" );
      compressFiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "compress" ) );
      compressFilesVariable = XMLHandler.getTagValue( stepnode, "compress_variable" );
      onlyPassingUniqueRows = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "unique_rows" ) );

      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrfields = XMLHandler.countNodes( fields, "field" );

      allocate( nrfields );
      String defaultStrength = Integer.toString( this.getDefaultCollationStrength() );

      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        fieldName[i] = XMLHandler.getTagValue( fnode, "name" );
        String asc = XMLHandler.getTagValue( fnode, "ascending" );
        ascending[i] = "Y".equalsIgnoreCase( asc );
        String sens = XMLHandler.getTagValue( fnode, "case_sensitive" );
        String coll = Const.NVL( XMLHandler.getTagValue( fnode, "collator_enabled" ), "N" );
        caseSensitive[i] = Utils.isEmpty( sens ) || "Y".equalsIgnoreCase( sens );
        collatorEnabled[i] = "Y".equalsIgnoreCase( coll );
        collatorStrength[i] =
            Integer.parseInt( Const.NVL( XMLHandler.getTagValue( fnode, "collator_strength" ), defaultStrength ) );
        String presorted = XMLHandler.getTagValue( fnode, "presorted" );
        preSortedField[i] = "Y".equalsIgnoreCase( presorted );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  @Override
  public void setDefault() {
    directory = "%%java.io.tmpdir%%";
    prefix = "out";
    sortSize = "1000000";
    freeMemoryLimit = null;
    compressFiles = false;
    compressFilesVariable = null;
    onlyPassingUniqueRows = false;

    int nrfields = 0;

    allocate( nrfields );

    for ( int i = 0; i < nrfields; i++ ) {
      fieldName[i] = "field" + i;
      caseSensitive[i] = true;
      collatorEnabled[i] = false;
      collatorStrength[i] = 0;
      preSortedField[i] = false;
    }
  }

  @VisibleForTesting
  protected void registerUrlWithDirectory() {
    parentStepMeta.getParentTransMeta().getNamedClusterEmbedManager().registerUrl( directory );
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 256 );
    registerUrlWithDirectory();
    retval.append( "      " ).append( XMLHandler.addTagValue( "directory", directory ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "prefix", prefix ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "sort_size", sortSize ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "free_memory", freeMemoryLimit ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "compress", compressFiles ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "compress_variable", compressFilesVariable ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "unique_rows", onlyPassingUniqueRows ) );

    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < fieldName.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", fieldName[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "ascending", ascending[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "case_sensitive", caseSensitive[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "collator_enabled", collatorEnabled[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "collator_strength", collatorStrength[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "presorted", preSortedField[i] ) );
      retval.append( "      </field>" ).append( Const.CR );
    }
    retval.append( "    </fields>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      directory = rep.getStepAttributeString( id_step, "directory" );
      prefix = rep.getStepAttributeString( id_step, "prefix" );
      sortSize = rep.getStepAttributeString( id_step, "sort_size" );
      freeMemoryLimit = rep.getStepAttributeString( id_step, "free_memory" );

      compressFiles = rep.getStepAttributeBoolean( id_step, "compress" );
      compressFilesVariable = rep.getStepAttributeString( id_step, "compress_variable" );

      onlyPassingUniqueRows = rep.getStepAttributeBoolean( id_step, "unique_rows" );

      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );

      allocate( nrfields );

      String defaultStrength = Integer.toString( this.getDefaultCollationStrength() );

      for ( int i = 0; i < nrfields; i++ ) {
        fieldName[i] = rep.getStepAttributeString( id_step, i, "field_name" );
        ascending[i] = rep.getStepAttributeBoolean( id_step, i, "field_ascending" );
        caseSensitive[i] = rep.getStepAttributeBoolean( id_step, i, "field_case_sensitive", true );
        collatorEnabled[i] = rep.getStepAttributeBoolean( id_step, i, "field_collator_enabled", false );
        collatorStrength[i] =
            Integer.parseInt( Const.NVL( rep.getStepAttributeString( id_step, i, "field_collator_strength" ),
                defaultStrength ) );
        preSortedField[i] = rep.getStepAttributeBoolean( id_step, i, "field_presorted", false );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  // Returns the default collation strength based on the users' default locale.
  // Package protected for testing purposes
  int getDefaultCollationStrength() {
    return getDefaultCollationStrength( Locale.getDefault() );
  }

  // Returns the collation strength based on the passed in locale.
  // Package protected for testing purposes
  int getDefaultCollationStrength( Locale aLocale ) {
    int defaultStrength = Collator.IDENTICAL;
    if ( aLocale != null ) {
      Collator curDefCollator = Collator.getInstance( aLocale );
      if ( curDefCollator != null ) {
        defaultStrength = curDefCollator.getStrength();
      }
    }
    return defaultStrength;
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "directory", directory );
      rep.saveStepAttribute( id_transformation, id_step, "prefix", prefix );
      rep.saveStepAttribute( id_transformation, id_step, "sort_size", sortSize );
      rep.saveStepAttribute( id_transformation, id_step, "free_memory", freeMemoryLimit );
      rep.saveStepAttribute( id_transformation, id_step, "compress", compressFiles );
      rep.saveStepAttribute( id_transformation, id_step, "compress_variable", compressFilesVariable );
      rep.saveStepAttribute( id_transformation, id_step, "unique_rows", onlyPassingUniqueRows );

      for ( int i = 0; i < fieldName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", fieldName[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_ascending", ascending[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_case_sensitive", caseSensitive[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_collator_enabled", collatorEnabled[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_collator_strength", collatorStrength[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_presorted", preSortedField[i] );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  @Override
  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Set the sorted properties: ascending/descending
    assignSortingCriteria( inputRowMeta );
  }

  @SuppressWarnings( "WeakerAccess" )
  public void assignSortingCriteria( RowMetaInterface inputRowMeta ) {
    for ( int i = 0; i < fieldName.length; i++ ) {
      int idx = inputRowMeta.indexOfValue( fieldName[i] );
      if ( idx >= 0 ) {
        ValueMetaInterface valueMeta = inputRowMeta.getValueMeta( idx );
        // On all these valueMetas, check to see if the value actually exists before we try to
        // set them.
        if ( ascending.length > i ) {
          valueMeta.setSortedDescending( !ascending[i] );
        }
        if ( caseSensitive.length > i ) {
          valueMeta.setCaseInsensitive( !caseSensitive[i] );
        }
        if ( collatorEnabled.length > i ) {
          valueMeta.setCollatorDisabled( !collatorEnabled[i] );
        }
        if ( collatorStrength.length > i ) {
          valueMeta.setCollatorStrength( collatorStrength[i] );
        }
        // Also see if lazy conversion is active on these key fields.
        // If so we want to automatically convert them to the normal storage type.
        // This will improve performance, see also: PDI-346
        //
        valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
        valueMeta.setStorageMetadata( null );
      }
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    CheckResult cr;

    if ( prev != null && prev.size() > 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "SortRowsMeta.CheckResult.FieldsReceived", "" + prev.size() ), stepMeta );
      remarks.add( cr );

      String error_message = "";
      boolean error_found = false;

      // Starting from selected fields in ...
      for ( int i = 0; i < fieldName.length; i++ ) {
        int idx = prev.indexOfValue( fieldName[i] );
        if ( idx < 0 ) {
          error_message += "\t\t" + fieldName[i] + Const.CR;
          error_found = true;
        }
      }
      if ( error_found ) {
        error_message = BaseMessages.getString( PKG, "SortRowsMeta.CheckResult.SortKeysNotFound", error_message );

        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        if ( fieldName.length > 0 ) {
          cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                  "SortRowsMeta.CheckResult.AllSortKeysFound" ), stepMeta );
          remarks.add( cr );
        } else {
          cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                  "SortRowsMeta.CheckResult.NoSortKeysEntered" ), stepMeta );
          remarks.add( cr );
        }
      }

      // Check the sort directory
      String realDirectory = transMeta.environmentSubstitute( directory );

      File f = new File( realDirectory );
      if ( f.exists() ) {
        if ( f.isDirectory() ) {
          cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                  "SortRowsMeta.CheckResult.DirectoryExists", realDirectory ), stepMeta );
          remarks.add( cr );
        } else {
          cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                  "SortRowsMeta.CheckResult.ExistsButNoDirectory", realDirectory ), stepMeta );
          remarks.add( cr );
        }
      } else {
        cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                "SortRowsMeta.CheckResult.DirectoryNotExists", realDirectory ), stepMeta );
        remarks.add( cr );
      }
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "SortRowsMeta.CheckResult.NoFields" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "SortRowsMeta.CheckResult.ExpectedInputOk" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "SortRowsMeta.CheckResult.ExpectedInputError" ), stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans ) {
    return new SortRows( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new SortRowsData();
  }

  /**
   * @return Returns the sortSize.
   */
  public String getSortSize() {
    return sortSize;
  }

  /**
   * @param sortSize
   *          The sortSize to set.
   */
  public void setSortSize( String sortSize ) {
    this.sortSize = sortSize;
  }

  /**
   * @return Returns whether temporary files should be compressed
   */
  public boolean getCompressFiles() {
    return compressFiles;

  }

  /**
   * @param compressFiles
   *          Whether to compress temporary files created during sorting
   */
  public void setCompressFiles( boolean compressFiles ) {
    this.compressFiles = compressFiles;
  }

  /**
   * @return the onlyPassingUniqueRows
   */
  public boolean isOnlyPassingUniqueRows() {
    return onlyPassingUniqueRows;
  }

  /**
   * @param onlyPassingUniqueRows
   *          the onlyPassingUniqueRows to set
   */
  public void setOnlyPassingUniqueRows( boolean onlyPassingUniqueRows ) {
    this.onlyPassingUniqueRows = onlyPassingUniqueRows;
  }

  /**
   * @return the compressFilesVariable
   */
  public String getCompressFilesVariable() {
    return compressFilesVariable;
  }

  /**
   * @param compressFilesVariable
   *          the compressFilesVariable to set
   */
  public void setCompressFilesVariable( String compressFilesVariable ) {
    this.compressFilesVariable = compressFilesVariable;
  }

  /**
   * @return the caseSensitive
   */
  public boolean[] getCaseSensitive() {
    return caseSensitive;
  }

  /**
   * @param caseSensitive
   *          the caseSensitive to set
   */
  public void setCaseSensitive( boolean[] caseSensitive ) {
    this.caseSensitive = caseSensitive;
  }

  /**
   * @return the collatorEnabled
   */
  public boolean[] getCollatorEnabled() {
    return collatorEnabled;
  }

  /**
   * @param collatorEnabled
   *          the collatorEnabled to set
   */
  public void setCollatorEnabled( boolean[] collatorEnabled ) {
    this.collatorEnabled = collatorEnabled;
  }

  /**
   * @return the collatorStrength
   */
  public int[] getCollatorStrength() {
    return collatorStrength;
  }

  /**
   * @param collatorStrength
   *          the collatorStrength to set
   */
  public void setCollatorStrength( int[] collatorStrength ) {
    this.collatorStrength = collatorStrength;
  }

  /**
   * @return the freeMemoryLimit
   */
  public String getFreeMemoryLimit() {
    return freeMemoryLimit;
  }

  /**
   * @param freeMemoryLimit
   *          the freeMemoryLimit to set
   */
  public void setFreeMemoryLimit( String freeMemoryLimit ) {
    this.freeMemoryLimit = freeMemoryLimit;
  }

  /**
   * @return the preSortedField
   */
  public boolean[] getPreSortedField() {
    return preSortedField;
  }

  /**
   * @param preSorted
   *          the preSorteField to set
   */
  public void setPreSortedField( boolean[] preSorted ) {
    preSortedField = preSorted;
  }

  public List<String> getGroupFields() {
    if ( this.groupFields == null ) {
      for ( int i = 0; i < preSortedField.length; i++ ) {
        if ( preSortedField[i] == true ) {
          if ( groupFields == null ) {
            groupFields = new ArrayList<String>();
          }
          groupFields.add( this.fieldName[i] );
        }
      }
    }
    return groupFields;
  }

  public boolean isGroupSortEnabled() {
    return ( this.getGroupFields() != null ) ? true : false;
  }

  /**
   * If we use injection we can have different arrays lengths.
   * We need synchronize them for consistency behavior with UI
   */
  @AfterInjection
  public void afterInjectionSynchronization() {
    int nrFields = ( fieldName == null ) ? -1 : fieldName.length;
    if ( nrFields <= 0 ) {
      return;
    }
    boolean[][] rtnBooleanArrays = Utils.normalizeArrays( nrFields, ascending, caseSensitive, collatorEnabled, preSortedField );
    ascending = rtnBooleanArrays[ 0 ];
    caseSensitive = rtnBooleanArrays[ 1 ];
    collatorEnabled = rtnBooleanArrays[ 2 ];
    preSortedField = rtnBooleanArrays[ 3 ];

    int[][] rtnIntArrays = Utils.normalizeArrays( nrFields, collatorStrength );
    collatorStrength = rtnIntArrays[ 0 ];

  }

}
