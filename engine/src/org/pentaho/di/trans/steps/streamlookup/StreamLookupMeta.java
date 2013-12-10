/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.streamlookup;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
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
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

public class StreamLookupMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = StreamLookupMeta.class; // for i18n purposes, needed by Translator2!!

  /** fields in input streams with which we look up values */
  private String[] keystream;

  /** fields in lookup stream with which we look up values */
  private String[] keylookup;

  /** return these field values from lookup */
  private String[] value;

  /** rename to this after lookup */
  private String[] valueName;

  /** default value in case not found... */
  private String[] valueDefault;

  /** type of default value */
  private int[] valueDefaultType;

  /** Indicate that the input is considered sorted! */
  private boolean inputSorted;

  /** Indicate that we need to preserve memory by serializing objects */
  private boolean memoryPreservationActive;

  /** Indicate that we want to use a sorted list vs. a hashtable */
  private boolean usingSortedList;

  /** The content of the key and lookup is a single Integer (long) */
  private boolean usingIntegerPair;

  public StreamLookupMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the inputSorted.
   */
  public boolean isInputSorted() {
    return inputSorted;
  }

  /**
   * @param inputSorted
   *          The inputSorted to set.
   */
  public void setInputSorted( boolean inputSorted ) {
    this.inputSorted = inputSorted;
  }

  /**
   * @return Returns the keylookup.
   */
  public String[] getKeylookup() {
    return keylookup;
  }

  /**
   * @param keylookup
   *          The keylookup to set.
   */
  public void setKeylookup( String[] keylookup ) {
    this.keylookup = keylookup;
  }

  /**
   * @return Returns the keystream.
   */
  public String[] getKeystream() {
    return keystream;
  }

  /**
   * @param keystream
   *          The keystream to set.
   */
  public void setKeystream( String[] keystream ) {
    this.keystream = keystream;
  }

  /**
   * @return Returns the value.
   */
  public String[] getValue() {
    return value;
  }

  /**
   * @param value
   *          The value to set.
   */
  public void setValue( String[] value ) {
    this.value = value;
  }

  /**
   * @return Returns the valueDefault.
   */
  public String[] getValueDefault() {
    return valueDefault;
  }

  /**
   * @param valueDefault
   *          The valueDefault to set.
   */
  public void setValueDefault( String[] valueDefault ) {
    this.valueDefault = valueDefault;
  }

  /**
   * @return Returns the valueDefaultType.
   */
  public int[] getValueDefaultType() {
    return valueDefaultType;
  }

  /**
   * @param valueDefaultType
   *          The valueDefaultType to set.
   */
  public void setValueDefaultType( int[] valueDefaultType ) {
    this.valueDefaultType = valueDefaultType;
  }

  /**
   * @return Returns the valueName.
   */
  public String[] getValueName() {
    return valueName;
  }

  /**
   * @param valueName
   *          The valueName to set.
   */
  public void setValueName( String[] valueName ) {
    this.valueName = valueName;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore )
    throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int nrkeys, int nrvalues ) {
    keystream = new String[nrkeys];
    keylookup = new String[nrkeys];

    value = new String[nrvalues];
    valueName = new String[nrvalues];
    valueDefault = new String[nrvalues];
    valueDefaultType = new int[nrvalues];
  }

  public Object clone() {
    StreamLookupMeta retval = (StreamLookupMeta) super.clone();
    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      String dtype;
      int nrkeys, nrvalues;

      String lookupFromStepname = XMLHandler.getTagValue( stepnode, "from" );
      StreamInterface infoStream = getStepIOMeta().getInfoStreams().get( 0 );
      infoStream.setSubject( lookupFromStepname );

      inputSorted = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "input_sorted" ) );
      memoryPreservationActive = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "preserve_memory" ) );
      usingSortedList = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "sorted_list" ) );
      usingIntegerPair = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "integer_pair" ) );

      Node lookup = XMLHandler.getSubNode( stepnode, "lookup" );
      nrkeys = XMLHandler.countNodes( lookup, "key" );
      nrvalues = XMLHandler.countNodes( lookup, "value" );

      allocate( nrkeys, nrvalues );

      for ( int i = 0; i < nrkeys; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( lookup, "key", i );

        keystream[i] = XMLHandler.getTagValue( knode, "name" );
        keylookup[i] = XMLHandler.getTagValue( knode, "field" );
      }

      for ( int i = 0; i < nrvalues; i++ ) {
        Node vnode = XMLHandler.getSubNodeByNr( lookup, "value", i );

        value[i] = XMLHandler.getTagValue( vnode, "name" );
        valueName[i] = XMLHandler.getTagValue( vnode, "rename" );
        if ( valueName[i] == null ) {
          valueName[i] = value[i]; // default: same name to return!
        }
        valueDefault[i] = XMLHandler.getTagValue( vnode, "default" );
        dtype = XMLHandler.getTagValue( vnode, "type" );
        valueDefaultType[i] = ValueMeta.getType( dtype );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "StreamLookupMeta.Exception.UnableToLoadStepInfoFromXML" ), e );
    }
  }

  @Override
  public void searchInfoAndTargetSteps( List<StepMeta> steps ) {
    for ( StreamInterface stream : getStepIOMeta().getInfoStreams() ) {
      stream.setStepMeta( StepMeta.findStep( steps, (String) stream.getSubject() ) );
    }
  }

  public void setDefault() {
    int nrkeys, nrvalues;

    keystream = null;
    value = null;
    valueDefault = null;

    memoryPreservationActive = true;
    usingSortedList = false;
    usingIntegerPair = false;

    nrkeys = 0;
    nrvalues = 0;

    allocate( nrkeys, nrvalues );

    for ( int i = 0; i < nrkeys; i++ ) {
      keystream[i] = "key" + i;
      keylookup[i] = "keyfield" + i;
    }

    for ( int i = 0; i < nrvalues; i++ ) {
      value[i] = "value" + i;
      valueName[i] = "valuename" + i;
      valueDefault[i] = "default" + i;
      valueDefaultType[i] = ValueMetaInterface.TYPE_NUMBER;
    }
  }

  public void getFields( RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    if ( info != null && info.length == 1 && info[0] != null ) {
      for ( int i = 0; i < valueName.length; i++ ) {
        ValueMetaInterface v = info[0].searchValueMeta( value[i] );
        if ( v != null ) {
          // Configuration error/missing resources...

          v.setName( valueName[i] );
          v.setOrigin( origin );
          v.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL ); // Only normal storage goes into the cache
          row.addValueMeta( v );
        } else {
          throw new KettleStepException( BaseMessages.getString(
            PKG, "StreamLookupMeta.Exception.ReturnValueCanNotBeFound", value[i] ) );
        }
      }
    } else {
      for ( int i = 0; i < valueName.length; i++ ) {
        try {
          ValueMetaInterface v = ValueMetaFactory.createValueMeta( valueName[i], valueDefaultType[i] );
          v.setOrigin( origin );
          row.addValueMeta( v );
        } catch ( Exception e ) {
          throw new KettleStepException( e );
        }
      }
    }
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer();

    StreamInterface infoStream = getStepIOMeta().getInfoStreams().get( 0 );
    retval.append( "    " + XMLHandler.addTagValue( "from", infoStream.getStepname() ) );
    retval.append( "    " + XMLHandler.addTagValue( "input_sorted", inputSorted ) );
    retval.append( "    " + XMLHandler.addTagValue( "preserve_memory", memoryPreservationActive ) );
    retval.append( "    " + XMLHandler.addTagValue( "sorted_list", usingSortedList ) );
    retval.append( "    " + XMLHandler.addTagValue( "integer_pair", usingIntegerPair ) );

    retval.append( "    <lookup>" + Const.CR );
    for ( int i = 0; i < keystream.length; i++ ) {
      retval.append( "      <key>" + Const.CR );
      retval.append( "        " + XMLHandler.addTagValue( "name", keystream[i] ) );
      retval.append( "        " + XMLHandler.addTagValue( "field", keylookup[i] ) );
      retval.append( "      </key>" + Const.CR );
    }

    for ( int i = 0; i < value.length; i++ ) {
      retval.append( "      <value>" + Const.CR );
      retval.append( "        " + XMLHandler.addTagValue( "name", value[i] ) );
      retval.append( "        " + XMLHandler.addTagValue( "rename", valueName[i] ) );
      retval.append( "        " + XMLHandler.addTagValue( "default", valueDefault[i] ) );
      retval.append( "        " + XMLHandler.addTagValue( "type", ValueMeta.getTypeDesc( valueDefaultType[i] ) ) );
      retval.append( "      </value>" + Const.CR );
    }
    retval.append( "    </lookup>" + Const.CR );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      String lookupFromStepname = rep.getStepAttributeString( id_step, "lookup_from_step" );
      StreamInterface infoStream = getStepIOMeta().getInfoStreams().get( 0 );
      infoStream.setSubject( lookupFromStepname );

      inputSorted = rep.getStepAttributeBoolean( id_step, "input_sorted" );
      memoryPreservationActive = rep.getStepAttributeBoolean( id_step, "preserve_memory" );
      usingSortedList = rep.getStepAttributeBoolean( id_step, "sorted_list" );
      usingIntegerPair = rep.getStepAttributeBoolean( id_step, "integer_pair" );

      int nrkeys = rep.countNrStepAttributes( id_step, "lookup_key_name" );
      int nrvalues = rep.countNrStepAttributes( id_step, "return_value_name" );

      allocate( nrkeys, nrvalues );

      for ( int i = 0; i < nrkeys; i++ ) {
        keystream[i] = rep.getStepAttributeString( id_step, i, "lookup_key_name" );
        keylookup[i] = rep.getStepAttributeString( id_step, i, "lookup_key_field" );
      }

      for ( int i = 0; i < nrvalues; i++ ) {
        value[i] = rep.getStepAttributeString( id_step, i, "return_value_name" );
        valueName[i] = rep.getStepAttributeString( id_step, i, "return_value_rename" );
        valueDefault[i] = rep.getStepAttributeString( id_step, i, "return_value_default" );
        valueDefaultType[i] = ValueMeta.getType( rep.getStepAttributeString( id_step, i, "return_value_type" ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "StreamLookupMeta.Exception.UnexpecteErrorReadingStepInfoFromRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      StreamInterface infoStream = getStepIOMeta().getInfoStreams().get( 0 );
      rep.saveStepAttribute( id_transformation, id_step, "lookup_from_step", infoStream.getStepname() );
      rep.saveStepAttribute( id_transformation, id_step, "input_sorted", inputSorted );
      rep.saveStepAttribute( id_transformation, id_step, "preserve_memory", memoryPreservationActive );
      rep.saveStepAttribute( id_transformation, id_step, "sorted_list", usingSortedList );
      rep.saveStepAttribute( id_transformation, id_step, "integer_pair", usingIntegerPair );

      for ( int i = 0; i < keystream.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "lookup_key_name", keystream[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "lookup_key_field", keylookup[i] );
      }

      for ( int i = 0; i < value.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "return_value_name", value[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "return_value_rename", valueName[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "return_value_default", valueDefault[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "return_value_type", ValueMeta
          .getTypeDesc( valueDefaultType[i] ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "StreamLookupMeta.Exception.UnableToSaveStepInfoToRepository" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    if ( prev != null && prev.size() > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "StreamLookupMeta.CheckResult.StepReceivingFields", prev.size() + "" ), stepMeta );
      remarks.add( cr );

      String error_message = "";
      boolean error_found = false;

      // Starting from selected fields in ...
      // Check the fields from the previous stream!
      for ( int i = 0; i < keystream.length; i++ ) {
        int idx = prev.indexOfValue( keystream[i] );
        if ( idx < 0 ) {
          error_message += "\t\t" + keystream[i] + Const.CR;
          error_found = true;
        }
      }
      if ( error_found ) {
        error_message =
          BaseMessages.getString( PKG, "StreamLookupMeta.CheckResult.FieldsNotFound" )
            + Const.CR + Const.CR + error_message;

        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "StreamLookupMeta.CheckResult.AllFieldsFound" ), stepMeta );
        remarks.add( cr );
      }
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "StreamLookupMeta.CheckResult.CouldNotFindFieldsFromPreviousSteps" ), stepMeta );
      remarks.add( cr );
    }

    if ( info != null && info.size() > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "StreamLookupMeta.CheckResult.StepReceivingLookupData", info.size() + "" ), stepMeta );
      remarks.add( cr );

      String error_message = "";
      boolean error_found = false;

      // Check the fields from the lookup stream!
      for ( int i = 0; i < keylookup.length; i++ ) {
        int idx = info.indexOfValue( keylookup[i] );
        if ( idx < 0 ) {
          error_message += "\t\t" + keylookup[i] + Const.CR;
          error_found = true;
        }
      }
      if ( error_found ) {
        error_message =
          BaseMessages.getString( PKG, "StreamLookupMeta.CheckResult.FieldsNotFoundInLookupStream" )
            + Const.CR + Const.CR + error_message;

        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "StreamLookupMeta.CheckResult.AllFieldsFoundInTheLookupStream" ), stepMeta );
        remarks.add( cr );
      }

      // Check the values to retrieve from the lookup stream!
      for ( int i = 0; i < value.length; i++ ) {
        int idx = info.indexOfValue( value[i] );
        if ( idx < 0 ) {
          error_message += "\t\t" + value[i] + Const.CR;
          error_found = true;
        }
      }
      if ( error_found ) {
        error_message =
          BaseMessages.getString( PKG, "StreamLookupMeta.CheckResult.FieldsNotFoundInLookupStream2" )
            + Const.CR + Const.CR + error_message;

        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "StreamLookupMeta.CheckResult.AllFieldsFoundInTheLookupStream2" ), stepMeta );
        remarks.add( cr );
      }
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "StreamLookupMeta.CheckResult.FieldsNotFoundFromInLookupSep" ), stepMeta );
      remarks.add( cr );
    }

    // See if the source step is filled in!
    StreamInterface infoStream = getStepIOMeta().getInfoStreams().get( 0 );
    if ( infoStream.getStepMeta() == null ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "StreamLookupMeta.CheckResult.SourceStepNotSelected" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "StreamLookupMeta.CheckResult.SourceStepIsSelected" ), stepMeta );
      remarks.add( cr );

      // See if the step exists!
      //
      if ( info != null ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "StreamLookupMeta.CheckResult.SourceStepExist", infoStream.getStepname() ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "StreamLookupMeta.CheckResult.SourceStepDoesNotExist", infoStream.getStepname() ), stepMeta );
        remarks.add( cr );
      }
    }

    // See if we have input streams leading to this step!
    if ( input.length >= 2 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "StreamLookupMeta.CheckResult.StepReceivingInfoFromInputSteps", input.length + "" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "StreamLookupMeta.CheckResult.NeedAtLeast2InputStreams", Const.CR, Const.CR ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new StreamLookup( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new StreamLookupData();
  }

  public boolean isMemoryPreservationActive() {
    return memoryPreservationActive;
  }

  public void setMemoryPreservationActive( boolean memoryPreservationActive ) {
    this.memoryPreservationActive = memoryPreservationActive;
  }

  public boolean isUsingSortedList() {
    return usingSortedList;
  }

  public void setUsingSortedList( boolean usingSortedList ) {
    this.usingSortedList = usingSortedList;
  }

  /**
   * @return the usingIntegerPair
   */
  public boolean isUsingIntegerPair() {
    return usingIntegerPair;
  }

  /**
   * @param usingIntegerPair
   *          the usingIntegerPair to set
   */
  public void setUsingIntegerPair( boolean usingIntegerPair ) {
    this.usingIntegerPair = usingIntegerPair;
  }

  public boolean excludeFromRowLayoutVerification() {
    return true;
  }

  /**
   * Returns the Input/Output metadata for this step. The generator step only produces output, does not accept input!
   */
  public StepIOMetaInterface getStepIOMeta() {
    if ( ioMeta == null ) {

      ioMeta = new StepIOMeta( true, true, false, false, false, false );

      StreamInterface stream =
        new Stream( StreamType.INFO, null, BaseMessages.getString(
          PKG, "StreamLookupMeta.InfoStream.Description" ), StreamIcon.INFO, null );
      ioMeta.addStream( stream );
    }

    return ioMeta;
  }

  public void resetStepIoMeta() {
    // Do nothing, don't reset as there is no need to do this.
  }
}
