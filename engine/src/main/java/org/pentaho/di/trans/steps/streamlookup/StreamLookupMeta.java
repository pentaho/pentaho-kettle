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

package org.pentaho.di.trans.steps.streamlookup;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.AfterInjection;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
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

@InjectionSupported( localizationPrefix = "StreamLookupMeta.Injection." )
public class StreamLookupMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = StreamLookupMeta.class; // for i18n purposes, needed by Translator2!!

  /** fields in input streams with which we look up values */
  @Injection( name = "KEY_STREAM" )
  private String[] keystream;

  /** fields in lookup stream with which we look up values */
  @Injection( name = "KEY_LOOKUP" )
  private String[] keylookup;

  /** return these field values from lookup */
  @Injection( name = "RETRIEVE_VALUE" )
  private String[] value;

  /** rename to this after lookup */
  @Injection( name = "RETRIEVE_VALUE_NAME" )
  private String[] valueName;

  /** default value in case not found... */
  @Injection( name = "RETRIEVE_VALUE_DEFAULT" )
  private String[] valueDefault;

  /** type of default value */
  @Injection( name = "RETRIEVE_DEFAULT_TYPE" )
  private int[] valueDefaultType;

  /** Indicate that the input is considered sorted! */
  private boolean inputSorted;

  /** Indicate that we need to preserve memory by serializing objects */
  @Injection( name = "PRESERVE_MEMORY" )
  private boolean memoryPreservationActive;

  /** Indicate that we want to use a sorted list vs. a hashtable */
  @Injection( name = "SORTED_LIST" )
  private boolean usingSortedList;

  /** The content of the key and lookup is a single Integer (long) */
  @Injection( name = "INTEGER_PAIR" )
  private boolean usingIntegerPair;

  public StreamLookupMeta() {
    super(); // allocate BaseStepMeta
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int nrkeys, int nrvalues ) {
    setKeystream( new String[nrkeys] );
    setKeylookup( new String[nrkeys] );

    setValue( new String[nrvalues] );
    setValueName( new String[nrvalues] );
    setValueDefault( new String[nrvalues] );
    setValueDefaultType( new int[nrvalues] );
  }

  @Override
  public Object clone() {
    StreamLookupMeta retval = (StreamLookupMeta) super.clone();

    if ( getStepIOMeta() != null
        && getStepIOMeta().getInfoStreams() != null
        && retval.getStepIOMeta() != null
        && getStepIOMeta().getInfoStreams() != null ) {
      retval.getStepIOMeta().getInfoStreams().get( 0 ).setStreamType(
        getStepIOMeta().getInfoStreams().get( 0 ).getStreamType() );
      retval.getStepIOMeta().getInfoStreams().get( 0 ).setStepMeta(
        getStepIOMeta().getInfoStreams().get( 0 ).getStepMeta() );
      retval.getStepIOMeta().getInfoStreams().get( 0 ).setDescription(
        getStepIOMeta().getInfoStreams().get( 0 ).getDescription() );
      retval.getStepIOMeta().getInfoStreams().get( 0 ).setStreamIcon(
        getStepIOMeta().getInfoStreams().get( 0 ).getStreamIcon() );
      retval.getStepIOMeta().getInfoStreams().get( 0 ).setSubject(
        getStepIOMeta().getInfoStreams().get( 0 ).getSubject() );
    }

    int nrkeys = keystream.length;
    int nrvals = value.length;
    retval.allocate( nrkeys, nrvals );
    System.arraycopy( keystream, 0, retval.keystream, 0, nrkeys );
    System.arraycopy( keylookup, 0, retval.keylookup, 0, nrkeys );
    System.arraycopy( value, 0, retval.value, 0, nrvals );
    System.arraycopy( valueName, 0, retval.valueName, 0, nrvals );
    System.arraycopy( valueDefault, 0, retval.valueDefault, 0, nrvals );
    System.arraycopy( valueDefaultType, 0, retval.valueDefaultType, 0, nrvals );
    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      String dtype;
      int nrkeys, nrvalues;

      String lookupFromStepname = XMLHandler.getTagValue( stepnode, "from" );
      StreamInterface infoStream = getStepIOMeta().getInfoStreams().get( 0 );
      infoStream.setSubject( lookupFromStepname );

      setInputSorted( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "input_sorted" ) ) );
      setMemoryPreservationActive( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "preserve_memory" ) ) );
      setUsingSortedList( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "sorted_list" ) ) );
      setUsingIntegerPair( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "integer_pair" ) ) );

      Node lookup = XMLHandler.getSubNode( stepnode, "lookup" );
      nrkeys = XMLHandler.countNodes( lookup, "key" );
      nrvalues = XMLHandler.countNodes( lookup, "value" );

      allocate( nrkeys, nrvalues );

      for ( int i = 0; i < nrkeys; i++ ) {
        Node knode = XMLHandler.getSubNodeByNr( lookup, "key", i );
        // CHECKSTYLE:Indentation:OFF
        getKeystream()[i] = XMLHandler.getTagValue( knode, "name" );
        getKeylookup()[i] = XMLHandler.getTagValue( knode, "field" );
        // CHECKSTYLE:Indentation:ON
      }

      for ( int i = 0; i < nrvalues; i++ ) {
        Node vnode = XMLHandler.getSubNodeByNr( lookup, "value", i );
        // CHECKSTYLE:Indentation:OFF
        getValue()[i] = XMLHandler.getTagValue( vnode, "name" );
        getValueName()[i] = XMLHandler.getTagValue( vnode, "rename" );
        if ( getValueName()[i] == null ) {
          getValueName()[i] = getValue()[i]; // default: same name to return!
        }

        getValueDefault()[i] = XMLHandler.getTagValue( vnode, "default" );
        dtype = XMLHandler.getTagValue( vnode, "type" );
        getValueDefaultType()[i] = ValueMetaFactory.getIdForValueMeta( dtype );
        // CHECKSTYLE:Indentation:ON
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

  @Override
  public void setDefault() {
    setMemoryPreservationActive( true );
    setUsingSortedList( false );
    setUsingIntegerPair( false );

    allocate( 0, 0 );
  }

  @Override
  public void getFields( RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    if ( info != null && info.length == 1 && info[0] != null ) {
      for ( int i = 0; i < getValueName().length; i++ ) {
        ValueMetaInterface v = info[0].searchValueMeta( getValue()[i] );
        if ( v != null ) {
          // Configuration error/missing resources...
          v.setName( getValueName()[i] );
          v.setOrigin( origin );
          row.addValueMeta( v );
        } else {
          throw new KettleStepException( BaseMessages.getString( PKG,
              "StreamLookupMeta.Exception.ReturnValueCanNotBeFound", getValue()[i] ) );
        }
      }
    } else {
      for ( int i = 0; i < getValueName().length; i++ ) {
        try {
          ValueMetaInterface v = ValueMetaFactory.createValueMeta( getValueName()[i], getValueDefaultType()[i] );
          v.setOrigin( origin );
          row.addValueMeta( v );
        } catch ( Exception e ) {
          throw new KettleStepException( e );
        }
      }
    }
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();

    StreamInterface infoStream = getStepIOMeta().getInfoStreams().get( 0 );
    retval.append( "    " ).append( XMLHandler.addTagValue( "from", infoStream.getStepname() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "input_sorted", isInputSorted() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "preserve_memory", isMemoryPreservationActive() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sorted_list", isUsingSortedList() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "integer_pair", isUsingIntegerPair() ) );

    retval.append( "    <lookup>" ).append( Const.CR );
    for ( int i = 0; i < getKeystream().length; i++ ) {
      retval.append( "      <key>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", getKeystream()[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "field", getKeylookup()[i] ) );
      retval.append( "      </key>" ).append( Const.CR );
    }

    for ( int i = 0; i < getValue().length; i++ ) {
      retval.append( "      <value>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", getValue()[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "rename", getValueName()[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "default", getValueDefault()[i] ) );
      retval.append( "        " ).append(
          XMLHandler.addTagValue( "type", ValueMetaFactory.getValueMetaName( getValueDefaultType()[i] ) ) );
      retval.append( "      </value>" ).append( Const.CR );
    }
    retval.append( "    </lookup>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      String lookupFromStepname = rep.getStepAttributeString( id_step, "lookup_from_step" );
      StreamInterface infoStream = getStepIOMeta().getInfoStreams().get( 0 );
      infoStream.setSubject( lookupFromStepname );

      setInputSorted( rep.getStepAttributeBoolean( id_step, "input_sorted" ) );
      setMemoryPreservationActive( rep.getStepAttributeBoolean( id_step, "preserve_memory" ) );
      setUsingSortedList( rep.getStepAttributeBoolean( id_step, "sorted_list" ) );
      setUsingIntegerPair( rep.getStepAttributeBoolean( id_step, "integer_pair" ) );

      int nrkeys = rep.countNrStepAttributes( id_step, "lookup_key_name" );
      int nrvalues = rep.countNrStepAttributes( id_step, "return_value_name" );

      allocate( nrkeys, nrvalues );

      for ( int i = 0; i < nrkeys; i++ ) {
        // CHECKSTYLE:Indentation:OFF
        getKeystream()[i] = rep.getStepAttributeString( id_step, i, "lookup_key_name" );
        getKeylookup()[i] = rep.getStepAttributeString( id_step, i, "lookup_key_field" );
        // CHECKSTYLE:Indentation:ON
      }

      for ( int i = 0; i < nrvalues; i++ ) {
        // CHECKSTYLE:Indentation:OFF
        getValue()[i] = rep.getStepAttributeString( id_step, i, "return_value_name" );
        getValueName()[i] = rep.getStepAttributeString( id_step, i, "return_value_rename" );
        getValueDefault()[i] = rep.getStepAttributeString( id_step, i, "return_value_default" );
        getValueDefaultType()[i] =
          ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, i, "return_value_type" ) );
        // CHECKSTYLE:Indentation:ON
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "StreamLookupMeta.Exception.UnexpecteErrorReadingStepInfoFromRepository" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      StreamInterface infoStream = getStepIOMeta().getInfoStreams().get( 0 );
      rep.saveStepAttribute( id_transformation, id_step, "lookup_from_step", infoStream.getStepname() );
      rep.saveStepAttribute( id_transformation, id_step, "input_sorted", isInputSorted() );
      rep.saveStepAttribute( id_transformation, id_step, "preserve_memory", isMemoryPreservationActive() );
      rep.saveStepAttribute( id_transformation, id_step, "sorted_list", isUsingSortedList() );
      rep.saveStepAttribute( id_transformation, id_step, "integer_pair", isUsingIntegerPair() );

      for ( int i = 0; i < getKeystream().length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "lookup_key_name", getKeystream()[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "lookup_key_field", getKeylookup()[i] );
      }

      for ( int i = 0; i < getValue().length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "return_value_name", getValue()[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "return_value_rename", getValueName()[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "return_value_default", getValueDefault()[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "return_value_type",
          ValueMetaFactory.getValueMetaName( getValueDefaultType()[i] ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "StreamLookupMeta.Exception.UnableToSaveStepInfoToRepository" )
        + id_step, e );
    }
  }

  @Override
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
      for ( String aKeystream : getKeystream() ) {
        int idx = prev.indexOfValue( aKeystream );
        if ( idx < 0 ) {
          error_message += "\t\t" + aKeystream + Const.CR;
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
      for ( String aKeylookup : getKeylookup() ) {
        int idx = info.indexOfValue( aKeylookup );
        if ( idx < 0 ) {
          error_message += "\t\t" + aKeylookup + Const.CR;
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
      for ( String aValue : getValue() ) {
        int idx = info.indexOfValue( aValue );
        if ( idx < 0 ) {
          error_message += "\t\t" + aValue + Const.CR;
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

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new StreamLookup( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new StreamLookupData();
  }

  @Override
  public boolean excludeFromRowLayoutVerification() {
    return true;
  }

  /**
   * Returns the Input/Output metadata for this step. The generator step only produces output, does not accept input!
   */
  @Override
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

  @Override
  public void resetStepIoMeta() {
    // Do nothing, don't reset as there is no need to do this.
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

  /**
   * If we use injection we can have different arrays lengths.
   * We need synchronize them for consistency behavior with UI
   */
  @AfterInjection
  public void afterInjectionSynchronization() {
    if ( value == null || value.length == 0 ) {
      return;
    }
    int nrFields = value.length;
    //PDI-16110
    if ( valueDefaultType.length < nrFields ) {
      int[] newValueDefaultType = new int[ nrFields ];
      System.arraycopy( valueDefaultType, 0, newValueDefaultType, 0, valueDefaultType.length );
      for ( int i = valueDefaultType.length; i < newValueDefaultType.length; i++ ) {
        newValueDefaultType[i] = -1; //set a undefined value (<0). It will be correct processed in a handleNullIf method
      }
      valueDefaultType = newValueDefaultType;
    }
    if ( valueName.length < nrFields ) {
      String[] newValueName = new String[ nrFields ];
      System.arraycopy( valueName, 0, newValueName, 0, valueName.length );
      valueName = newValueName;
    }

    if ( valueDefault.length < nrFields ) {
      String[] newValueDefault = new String[ nrFields ];
      System.arraycopy( valueDefault, 0, newValueDefault, 0, valueDefault.length );
      valueDefault = newValueDefault;
    }

  }
}
