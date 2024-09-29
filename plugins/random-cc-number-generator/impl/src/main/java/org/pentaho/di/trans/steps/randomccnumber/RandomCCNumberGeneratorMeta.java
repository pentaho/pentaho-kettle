/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.randomccnumber;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
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
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Generate random credit card number.
 *
 * @author Samatar
 * @since 01-4-2010
 */

@Step( id = "RandomCCNumberGenerator", name = "BaseStep.TypeLongDesc.RandomCCNumberGenerator",
        description = "BaseStep.TypeTooltipDesc.RandomCCNumberGenerator",
        categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Input",
        image = "CCG.svg",
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Generate+random+credit+card+numbers",
        i18nPackageName = "org.pentaho.di.trans.steps.randomccnumber" )

public class RandomCCNumberGeneratorMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = RandomCCNumberGeneratorMeta.class; // for i18n purposes, needed by Translator2!!

  private String[] fieldCCType;
  private String[] fieldCCLength;
  private String[] fieldCCSize;

  private String cardNumberFieldName;
  private String cardLengthFieldName;
  private String cardTypeFieldName;

  public RandomCCNumberGeneratorMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the fieldCCType.
   */
  public String[] getFieldCCType() {
    return fieldCCType;
  }

  /**
   * @return Returns the cardNumberFieldName.
   */
  public String getCardNumberFieldName() {
    return cardNumberFieldName;
  }

  /**
   * @param cardNumberFieldName
   *          The cardNumberFieldName to set.
   */
  public void setCardNumberFieldName( String cardNumberFieldName ) {
    this.cardNumberFieldName = cardNumberFieldName;
  }

  /**
   * @return Returns the cardLengthFieldName.
   */
  public String getCardLengthFieldName() {
    return cardLengthFieldName;
  }

  /**
   * @param cardLengthFieldName
   *          The cardLengthFieldName to set.
   */
  public void setCardLengthFieldName( String cardLengthFieldName ) {
    this.cardLengthFieldName = cardLengthFieldName;
  }

  /**
   * @return Returns the cardTypeFieldName.
   */
  public String getCardTypeFieldName() {
    return cardTypeFieldName;
  }

  /**
   * @param cardTypeFieldName
   *          The cardTypeFieldName to set.
   */
  public void setCardTypeFieldName( String cardTypeFieldName ) {
    this.cardTypeFieldName = cardTypeFieldName;
  }

  /**
   * @param fieldName
   *          The fieldCCType to set.
   */
  public void setFieldCCType( String[] fieldName ) {
    this.fieldCCType = fieldName;
  }

  /**
   * @return Returns the fieldType.
   */
  public String[] getFieldCCLength() {
    return fieldCCLength;
  }

  /**
   * @return Returns the fieldCCSize.
   */
  public String[] getFieldCCSize() {
    return fieldCCSize;
  }

  public void setFieldCCSize( String[] ccSize ) {
    this.fieldCCSize = ccSize;
  }

  /**
   * @deprecated Use setFieldCCLength instead
   */
  @Deprecated
  public void setFieldType( String[] fieldType ) {
    this.fieldCCLength = fieldType;
  }

  public void setFieldCCLength( String[] ccLength ) {
    this.fieldCCLength = ccLength;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int count ) {
    fieldCCType = new String[count];
    fieldCCLength = new String[count];
    fieldCCSize = new String[count];
  }

  public Object clone() {
    RandomCCNumberGeneratorMeta retval = (RandomCCNumberGeneratorMeta) super.clone();

    int count = fieldCCType.length;

    retval.allocate( count );
    System.arraycopy( fieldCCType, 0, retval.fieldCCType, 0, count );
    System.arraycopy( fieldCCLength, 0, retval.fieldCCLength, 0, count );
    System.arraycopy( fieldCCSize, 0, retval.fieldCCSize, 0, count );

    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int count = XMLHandler.countNodes( fields, "field" );

      allocate( count );

      for ( int i = 0; i < count; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        fieldCCType[i] = XMLHandler.getTagValue( fnode, "cctype" );
        fieldCCLength[i] = XMLHandler.getTagValue( fnode, "cclen" );
        fieldCCSize[i] = XMLHandler.getTagValue( fnode, "ccsize" );
      }

      cardNumberFieldName = XMLHandler.getTagValue( stepnode, "cardNumberFieldName" );
      cardLengthFieldName = XMLHandler.getTagValue( stepnode, "cardLengthFieldName" );
      cardTypeFieldName = XMLHandler.getTagValue( stepnode, "cardTypeFieldName" );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to read step information from XML", e );
    }
  }

  public void setDefault() {
    int count = 0;

    allocate( count );

    for ( int i = 0; i < count; i++ ) {
      fieldCCType[i] = "field" + i;
      fieldCCLength[i] = "";
      fieldCCSize[i] = "";
    }
    cardNumberFieldName = BaseMessages.getString( PKG, "RandomCCNumberGeneratorMeta.CardNumberField" );
    cardLengthFieldName = BaseMessages.getString( PKG, "RandomCCNumberGeneratorMeta.CardLengthField" );
    cardTypeFieldName = BaseMessages.getString( PKG, "RandomCCNumberGeneratorMeta.CardTypeField" );
  }

  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    ValueMetaInterface v = new ValueMetaString( cardNumberFieldName );
    v.setOrigin( name );
    row.addValueMeta( v );

    if ( !Utils.isEmpty( getCardTypeFieldName() ) ) {
      v = new ValueMetaString( cardTypeFieldName );
      v.setOrigin( name );
      row.addValueMeta( v );
    }

    if ( !Utils.isEmpty( getCardLengthFieldName() ) ) {
      v = new ValueMetaInteger( cardLengthFieldName );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }

  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );

    retval.append( "    <fields>" ).append( Const.CR );

    for ( int i = 0; i < fieldCCType.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "cctype", fieldCCType[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "cclen", fieldCCLength[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "ccsize", fieldCCSize[i] ) );
      retval.append( "      </field>" ).append( Const.CR );
    }
    retval.append( "    </fields>" + Const.CR );

    retval.append( "    " + XMLHandler.addTagValue( "cardNumberFieldName", cardNumberFieldName ) );
    retval.append( "    " + XMLHandler.addTagValue( "cardLengthFieldName", cardLengthFieldName ) );
    retval.append( "    " + XMLHandler.addTagValue( "cardTypeFieldName", cardTypeFieldName ) );
    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      int nrfields = rep.countNrStepAttributes( id_step, "cctype" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        fieldCCType[i] = rep.getStepAttributeString( id_step, i, "cctype" );
        fieldCCLength[i] = rep.getStepAttributeString( id_step, i, "cclen" );
        fieldCCSize[i] = rep.getStepAttributeString( id_step, i, "ccsize" );
      }
      cardNumberFieldName = rep.getStepAttributeString( id_step, "cardNumberFieldName" );
      cardLengthFieldName = rep.getStepAttributeString( id_step, "cardLengthFieldName" );
      cardTypeFieldName = rep.getStepAttributeString( id_step, "cardTypeFieldName" );

    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      for ( int i = 0; i < fieldCCType.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "cctype", fieldCCType[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "cclen", fieldCCLength[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "ccsize", fieldCCSize[i] );
      }
      rep.saveStepAttribute( id_transformation, id_step, "cardNumberFieldName", cardNumberFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "cardLengthFieldName", cardLengthFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "cardTypeFieldName", cardTypeFieldName );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }

  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    // See if we have input streams leading to this step!
    int nrRemarks = remarks.size();
    for ( int i = 0; i < fieldCCType.length; i++ ) {
      int len = Const.toInt( transMeta.environmentSubstitute( getFieldCCLength()[i] ), -1 );
      if ( len < 0 ) {
        CheckResult cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "RandomCCNumberGeneratorMeta.CheckResult.WrongLen", String.valueOf( i ) ), stepMeta );
        remarks.add( cr );
      }
      int size = Const.toInt( transMeta.environmentSubstitute( getFieldCCSize()[i] ), -1 );
      if ( size < 0 ) {
        CheckResult cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "RandomCCNumberGeneratorMeta.CheckResult.WrongSize", String.valueOf( i ) ), stepMeta );
        remarks.add( cr );
      }
    }
    if ( remarks.size() == nrRemarks ) {
      CheckResult cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "RandomCCNumberGeneratorMeta.CheckResult.AllTypesSpecified" ), stepMeta );
      remarks.add( cr );
    }

    if ( Utils.isEmpty( getCardNumberFieldName() ) ) {
      CheckResult cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "RandomCCNumberGeneratorMeta.CheckResult.CardNumberFieldMissing" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new RandomCCNumberGenerator( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new RandomCCNumberGeneratorData();
  }

  /**
   * Returns the Input/Output metadata for this step. The generator step only produces output, does not accept input!
   */
  public StepIOMetaInterface getStepIOMeta() {
    return new StepIOMeta( false, true, false, false, false, false );
  }
}
