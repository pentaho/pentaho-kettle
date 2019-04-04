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

package org.pentaho.di.trans.steps.normaliser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
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
 * Created on 30-okt-2003
 *
 */

/*

 DATE      PRODUCT1_NR  PRODUCT1_SL  PRODUCT2_NR PRODUCT2_SL PRODUCT3_NR PRODUCT3_SL
 20030101            5          100           10         250           4         150

 DATE      PRODUCT    Sales   Number
 20030101  PRODUCT1     100        5
 20030101  PRODUCT2     250       10
 20030101  PRODUCT3     150        4

 --> we need a mapping of fields with occurances.  (PRODUCT1_NR --> "PRODUCT1", PRODUCT1_SL --> "PRODUCT1", ...)
 --> List of Fields with the type and the new fieldname to fill
 --> PRODUCT1_NR, "PRODUCT1", Number
 --> PRODUCT1_SL, "PRODUCT1", Sales
 --> PRODUCT2_NR, "PRODUCT2", Number
 --> PRODUCT2_SL, "PRODUCT2", Sales
 --> PRODUCT3_NR, "PRODUCT3", Number
 --> PRODUCT3_SL, "PRODUCT3", Sales

 --> To parse this, we loop over the occurances of type: "PRODUCT1", "PRODUCT2" and "PRODUCT3"
 --> For each of the occurance, we insert a record.

 **/

@InjectionSupported( localizationPrefix = "NormaliserMeta.Injection.", groups = { "FIELDS" } )
public class NormaliserMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = NormaliserMeta.class; // for i18n purposes, needed by Translator2!!

  private String typeField; // Name of the new type-field.

  @InjectionDeep
  private NormaliserField[] normaliserFields = {};

  public NormaliserMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the typeField.
   */
  public String getTypeField() {
    return typeField;
  }

  /**
   * @param typeField
   *          The typeField to set.
   */
  public void setTypeField( String typeField ) {
    this.typeField = typeField;
  }

  public NormaliserField[] getNormaliserFields() {
    return normaliserFields;
  }

  public void setNormaliserFields( NormaliserField[] normaliserFields ) {
    this.normaliserFields = normaliserFields;
  }

  public Set<String> getFieldNames() {
    Set<String> fieldNames = new HashSet<>( );
    String s;
    for ( int i = 0; i < normaliserFields.length; i++ ) {
      s = normaliserFields[i].getName();
      if ( s != null ) {
        fieldNames.add( s.toLowerCase() );
      }
    }
    return fieldNames;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int nrfields ) {
    normaliserFields = new NormaliserField[nrfields];
    for ( int i = 0; i < nrfields; i++ ) {
      normaliserFields[i] = new NormaliserField();
    }
  }

  @Override
  public Object clone() {
    NormaliserMeta retval = (NormaliserMeta) super.clone();

    int nrfields = normaliserFields.length;

    retval.allocate( nrfields );

    for ( int i = 0; i < nrfields; i++ ) {
      retval.normaliserFields[i] = (NormaliserField) normaliserFields[i].clone();
    }

    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      typeField = XMLHandler.getTagValue( stepnode, "typefield" );

      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrfields = XMLHandler.countNodes( fields, "field" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        normaliserFields[i].setName( XMLHandler.getTagValue( fnode, "name" ) );
        normaliserFields[i].setValue( XMLHandler.getTagValue( fnode, "value" ) );
        normaliserFields[i].setNorm( XMLHandler.getTagValue( fnode, "norm" ) );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG,
          "NormaliserMeta.Exception.UnableToLoadStepInfoFromXML" ), e );
    }
  }

  @Override
  public void setDefault() {
    typeField = "typefield";

    int nrfields = 0;

    allocate( nrfields );

    for ( int i = 0; i < nrfields; i++ ) {
      normaliserFields[i].setName( "field" + i );
      normaliserFields[i].setValue( "value" + i );
      normaliserFields[i].setNorm( "value" + i );
    }
  }

  @Override
  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    // Get a unique list of the occurrences of the type
    //
    List<String> norm_occ = new ArrayList<>();
    List<String> field_occ = new ArrayList<>();
    int maxlen = 0;
    for ( int i = 0; i < normaliserFields.length; i++ ) {
      if ( !norm_occ.contains( normaliserFields[i].getNorm() ) ) {
        norm_occ.add( normaliserFields[i].getNorm() );
        field_occ.add( normaliserFields[i].getName() );
      }

      if ( normaliserFields[i].getValue().length() > maxlen ) {
        maxlen = normaliserFields[i].getValue().length();
      }
    }

    // Then add the type field!
    //
    ValueMetaInterface typefield_value = new ValueMetaString( typeField );
    typefield_value.setOrigin( name );
    typefield_value.setLength( maxlen );
    row.addValueMeta( typefield_value );

    // Loop over the distinct list of fieldNorm[i]
    // Add the new fields that need to be created.
    // Use the same data type as the original fieldname...
    //
    for ( int i = 0; i < norm_occ.size(); i++ ) {
      String normname = norm_occ.get( i );
      String fieldname = field_occ.get( i );
      ValueMetaInterface v = row.searchValueMeta( fieldname );
      if ( v != null ) {
        v = v.clone();
      } else {
        throw new KettleStepException( BaseMessages.getString( PKG, "NormaliserMeta.Exception.UnableToFindField", fieldname ) );
      }
      v.setName( normname );
      v.setOrigin( name );
      row.addValueMeta( v );
    }

    // Now remove all the normalized fields...
    //
    for ( int i = 0; i < normaliserFields.length; i++ ) {
      int idx = row.indexOfValue( normaliserFields[i].getName() );
      if ( idx >= 0 ) {
        row.removeValueMeta( idx );
      }
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "   " + XMLHandler.addTagValue( "typefield", typeField ) );

    retval.append( "    <fields>" );
    for ( int i = 0; i < normaliserFields.length; i++ ) {
      retval.append( "      <field>" );
      retval.append( "        " + XMLHandler.addTagValue( "name", normaliserFields[i].getName() ) );
      retval.append( "        " + XMLHandler.addTagValue( "value", normaliserFields[i].getValue() ) );
      retval.append( "        " + XMLHandler.addTagValue( "norm", normaliserFields[i].getNorm() ) );
      retval.append( "        </field>" );
    }
    retval.append( "      </fields>" );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {

    try {
      typeField = rep.getStepAttributeString( id_step, "typefield" );

      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        normaliserFields[i].setName( rep.getStepAttributeString( id_step, i, "field_name" ) );
        normaliserFields[i].setValue( rep.getStepAttributeString( id_step, i, "field_value" ) );
        normaliserFields[i].setNorm( rep.getStepAttributeString( id_step, i, "field_norm" ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
          "NormaliserMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "typefield", typeField );

      for ( int i = 0; i < normaliserFields.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", normaliserFields[i].getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_value", normaliserFields[i].getValue() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_norm", normaliserFields[i].getNorm() );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
          "NormaliserMeta.Exception.UnableToSaveStepInfoToRepository" ) + id_step, e );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {

    String error_message = "";
    CheckResult cr;

    // Look up fields in the input stream <prev>
    if ( prev != null && prev.size() > 0 ) {
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "NormaliserMeta.CheckResult.StepReceivingFieldsOK", prev.size() + "" ), stepMeta );
      remarks.add( cr );

      boolean first = true;
      error_message = "";
      boolean error_found = false;

      for ( int i = 0; i < normaliserFields.length; i++ ) {
        String lufield = normaliserFields[i].getName();

        ValueMetaInterface v = prev.searchValueMeta( lufield );
        if ( v == null ) {
          if ( first ) {
            first = false;
            error_message += BaseMessages.getString( PKG, "NormaliserMeta.CheckResult.FieldsNotFound" ) + Const.CR;
          }
          error_found = true;
          error_message += "\t\t" + lufield + Const.CR;
        }
      }
      if ( error_found ) {
        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      } else {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                "NormaliserMeta.CheckResult.AllFieldsFound" ), stepMeta );
      }
      remarks.add( cr );
    } else {
      error_message =
          BaseMessages.getString( PKG, "NormaliserMeta.CheckResult.CouldNotReadFieldsFromPreviousStep" ) + Const.CR;
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "NormaliserMeta.CheckResult.StepReceivingInfoOK" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "NormaliserMeta.CheckResult.NoInputReceivedError" ), stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans ) {
    return new Normaliser( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new NormaliserData();
  }

  public static class NormaliserField implements Cloneable {

    @Injection( name = "NAME", group = "FIELDS" )
    private String name;

    @Injection( name = "VALUE", group = "FIELDS" )
    private String value;

    @Injection( name = "NORMALISED", group = "FIELDS" )
    private String norm;

    public NormaliserField() {
    }

    /**
     * @return the name
     */
    public String getName() {
      return name;
    }

    /**
     * @param name
     *          the name to set
     */
    public void setName( String name ) {
      this.name = name;
    }

    /**
     * @return the value
     */
    public String getValue() {
      return value;
    }

    /**
     * @param value
     *          the value to set
     */
    public void setValue( String value ) {
      this.value = value;
    }

    /**
     * @return the norm
     */
    public String getNorm() {
      return norm;
    }

    /**
     * @param norm
     *          the norm to set
     */
    public void setNorm( String norm ) {
      this.norm = norm;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
      result = prime * result + ( ( norm == null ) ? 0 : norm.hashCode() );
      result = prime * result + ( ( value == null ) ? 0 : value.hashCode() );
      return result;
    }

    @Override
    public boolean equals( Object obj ) {
      if ( this == obj ) {
        return true;
      }
      if ( obj == null ) {
        return false;
      }
      if ( getClass() != obj.getClass() ) {
        return false;
      }
      NormaliserField other = (NormaliserField) obj;
      if ( name == null ) {
        if ( other.name != null ) {
          return false;
        }
      } else if ( !name.equals( other.name ) ) {
        return false;
      }
      if ( norm == null ) {
        if ( other.norm != null ) {
          return false;
        }
      } else if ( !norm.equals( other.norm ) ) {
        return false;
      }
      if ( value == null ) {
        if ( other.value != null ) {
          return false;
        }
      } else if ( !value.equals( other.value ) ) {
        return false;
      }
      return true;
    }

    @Override
    public Object clone() {
      try {
        NormaliserField retval = (NormaliserField) super.clone();
        return retval;
      } catch ( CloneNotSupportedException e ) {
        throw new RuntimeException( e );
      }
    }
  }
}
