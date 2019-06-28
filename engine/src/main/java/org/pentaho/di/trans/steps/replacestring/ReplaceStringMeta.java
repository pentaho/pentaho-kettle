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

package org.pentaho.di.trans.steps.replacestring;

import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
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

@InjectionSupported( localizationPrefix = "ReplaceString.Injection.", groups = { "FIELDS" } )
public class ReplaceStringMeta extends BaseStepMeta implements StepMetaInterface {

  private static Class<?> PKG = ReplaceStringMeta.class; // for i18n purposes, needed by Translator2!!

  @Injection( name = "FIELD_IN_STREAM", group = "FIELDS" )
  private String[] fieldInStream;

  @Injection( name = "FIELD_OUT_STREAM", group = "FIELDS" )
  private String[] fieldOutStream;

  @Injection( name = "USE_REGEX", group = "FIELDS" )
  private boolean[] useRegEx;

  @Injection( name = "REPLACE_STRING", group = "FIELDS" )
  private String[] replaceString;

  @Injection( name = "REPLACE_BY", group = "FIELDS" )
  private String[] replaceByString;

  /** Flag : set empty string **/
  @Injection( name = "EMPTY_STRING", group = "FIELDS" )
  private boolean[] setEmptyString;

  @Injection( name = "REPLACE_WITH_FIELD", group = "FIELDS" )
  private String[] replaceFieldByString;

  @Injection( name = "REPLACE_WHOLE_WORD", group = "FIELDS" )
  private boolean[] wholeWord;

  @Injection( name = "CASE_SENSITIVE", group = "FIELDS" )
  private boolean[] caseSensitive;

  @Injection( name = "IS_UNICODE", group = "FIELDS" )
  private boolean[] isUnicode;

  public static final String[] flagDescriptor = new String[] {
    BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG, "System.Combo.Yes" ) };

  public ReplaceStringMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the fieldInStream.
   */
  public String[] getFieldInStream() {
    return fieldInStream;
  }

  /**
   * @param keyStream
   *          The fieldInStream to set.
   */
  public void setFieldInStream( String[] keyStream ) {
    this.fieldInStream = keyStream;
  }

  public boolean[] getCaseSensitive() {
    return caseSensitive;
  }

  public boolean[] isUnicode() {
    return isUnicode;
  }

  public boolean[] getWholeWord() {
    return wholeWord;
  }

  public void setWholeWord( boolean[] wholeWord ) {
    this.wholeWord = wholeWord;
  }

  public boolean[] getUseRegEx() {
    return useRegEx;
  }

  public void setUseRegEx( boolean[] useRegEx ) {
    this.useRegEx = useRegEx;
  }

  /**
   * @return the setEmptyString
   */
  public boolean[] isSetEmptyString() {
    return setEmptyString;
  }

  /**
   * @param setEmptyString
   *          the setEmptyString to set
   */
  public void setEmptyString( boolean[] setEmptyString ) {
    this.setEmptyString = setEmptyString;
  }

  /**
   * @return Returns the fieldOutStream.
   */
  public String[] getFieldOutStream() {
    return fieldOutStream;
  }

  /**
   * @param keyStream
   *          The fieldOutStream to set.
   */
  public void setFieldOutStream( String[] keyStream ) {
    this.fieldOutStream = keyStream;
  }

  public String[] getReplaceString() {
    return replaceString;
  }

  public void setReplaceString( String[] replaceString ) {
    this.replaceString = replaceString;
  }

  public String[] getReplaceByString() {
    return replaceByString;
  }

  public void setReplaceByString( String[] replaceByString ) {
    this.replaceByString = replaceByString;
  }

  public String[] getFieldReplaceByString() {
    return replaceFieldByString;
  }

  public void setFieldReplaceByString( String[] replaceFieldByString ) {
    this.replaceFieldByString = replaceFieldByString;
  }

  public void setCaseSensitive( boolean[] caseSensitive ) {
    this.caseSensitive = caseSensitive;
  }

  public void setIsUnicode( boolean[] isUnicode ) {
    this.isUnicode = isUnicode;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public void allocate( int nrkeys ) {
    fieldInStream = new String[nrkeys];
    fieldOutStream = new String[nrkeys];
    useRegEx = new boolean[nrkeys];
    replaceString = new String[nrkeys];
    replaceByString = new String[nrkeys];
    setEmptyString = new boolean[nrkeys];
    replaceFieldByString = new String[nrkeys];
    wholeWord = new boolean[nrkeys];
    caseSensitive = new boolean[nrkeys];
    isUnicode = new boolean[nrkeys];
  }

  public Object clone() {
    ReplaceStringMeta retval = (ReplaceStringMeta) super.clone();
    int nrkeys = fieldInStream.length;

    retval.allocate( nrkeys );

    System.arraycopy( fieldInStream, 0, retval.fieldInStream, 0, nrkeys );
    System.arraycopy( fieldOutStream, 0, retval.fieldOutStream, 0, nrkeys );
    System.arraycopy( useRegEx, 0, retval.useRegEx, 0, nrkeys );
    System.arraycopy( replaceString, 0, retval.replaceString, 0, nrkeys );
    System.arraycopy( replaceByString, 0, retval.replaceByString, 0, nrkeys );
    System.arraycopy( setEmptyString, 0, retval.setEmptyString, 0, nrkeys );
    System.arraycopy( replaceFieldByString, 0, retval.replaceFieldByString, 0, nrkeys );
    System.arraycopy( wholeWord, 0, retval.wholeWord, 0, nrkeys );
    System.arraycopy( caseSensitive, 0, retval.caseSensitive, 0, nrkeys );
    System.arraycopy( isUnicode, 0, retval.isUnicode, 0, nrkeys );
    return retval;
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      int nrkeys;

      Node lookup = XMLHandler.getSubNode( stepnode, "fields" );
      nrkeys = XMLHandler.countNodes( lookup, "field" );

      allocate( nrkeys );

      for ( int i = 0; i < nrkeys; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( lookup, "field", i );

        fieldInStream[i] = Const.NVL( XMLHandler.getTagValue( fnode, "in_stream_name" ), "" );
        fieldOutStream[i] = Const.NVL( XMLHandler.getTagValue( fnode, "out_stream_name" ), "" );
        useRegEx[i] = getFlagFromString( Const.NVL( XMLHandler.getTagValue( fnode, "use_regex" ), "" ) );
        replaceString[i] = Const.NVL( XMLHandler.getTagValue( fnode, "replace_string" ), "" );
        replaceByString[i] = Const.NVL( XMLHandler.getTagValue( fnode, "replace_by_string" ), "" );
        String emptyString = XMLHandler.getTagValue( fnode, "set_empty_string" );

        setEmptyString[i] = !Utils.isEmpty( emptyString ) && "Y".equalsIgnoreCase( emptyString );
        replaceFieldByString[i] = Const.NVL( XMLHandler.getTagValue( fnode, "replace_field_by_string" ), "" );
        wholeWord[i] = getFlagFromString( Const.NVL( XMLHandler.getTagValue( fnode, "whole_word" ), "" ) );
        caseSensitive[i] =
          getFlagFromString( Const.NVL( XMLHandler.getTagValue( fnode, "case_sensitive" ), "" ) );
        isUnicode[i] =
          getFlagFromString( Const.NVL( XMLHandler.getTagValue( fnode, "is_unicode" ), "" ) );

      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "ReplaceStringMeta.Exception.UnableToReadStepInfoFromXML" ), e );
    }
  }

  private static boolean getFlagFromString( String tt ) {
    return ( "Y".equalsIgnoreCase( tt ) || "yes".equalsIgnoreCase( tt )  || "true".equalsIgnoreCase( tt ) );
  }

  public void setDefault() {
    fieldInStream = null;
    fieldOutStream = null;
    int nrkeys = 0;

    allocate( nrkeys );
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 500 );

    retval.append( "    <fields>" ).append( Const.CR );

    for ( int i = 0; i < fieldInStream.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "in_stream_name", fieldInStream[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "out_stream_name", fieldOutStream[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "use_regex", getFlagTagValue( useRegEx[i] ) ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "replace_string", replaceString[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "replace_by_string", replaceByString[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "set_empty_string", setEmptyString[i] ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( "replace_field_by_string", replaceFieldByString[i] ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "whole_word", getFlagTagValue( wholeWord[i] ) ) );
      retval.append( "        " )
        .append( XMLHandler.addTagValue( "case_sensitive", getFlagTagValue( caseSensitive[i] ) ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( "is_unicode", getFlagTagValue( isUnicode[i] ) ) );
      retval.append( "      </field>" ).append( Const.CR );
    }

    retval.append( "    </fields>" ).append( Const.CR );

    return retval.toString();
  }

  /** BACKLOG-27839 Outputs Flags that were previously int as "yes" or "no" for Backwards Compatibility in XML. **/
  private static String getFlagTagValue( boolean flag ) {
    return ( flag ? "yes" : "no" );
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      int nrkeys = rep.countNrStepAttributes( id_step, "in_stream_name" );

      allocate( nrkeys );
      for ( int i = 0; i < nrkeys; i++ ) {
        fieldInStream[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "in_stream_name" ), "" );
        fieldOutStream[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "out_stream_name" ), "" );
        useRegEx[i] =
          getFlagFromString( Const.NVL( rep.getStepAttributeString( id_step, i, "use_regex" ), "" ) );
        replaceString[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "replace_string" ), "" );
        replaceByString[i] = Const.NVL( rep.getStepAttributeString( id_step, i, "replace_by_string" ), "" );
        setEmptyString[i] = rep.getStepAttributeBoolean( id_step, i, "set_empty_string", false );
        replaceFieldByString[i] =
          Const.NVL( rep.getStepAttributeString( id_step, i, "replace_field_by_string" ), "" );
        wholeWord[i] =
          getFlagFromString( Const.NVL( rep.getStepAttributeString( id_step, i, "whole_world" ), "" ) );
        caseSensitive[i] =
          getFlagFromString( Const.NVL( rep.getStepAttributeString( id_step, i, "case_sensitive" ), "" ) );
        isUnicode[i] =
          getFlagFromString( Const.NVL( rep.getStepAttributeString( id_step, i, "is_unicode" ), "" ) );

      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "ReplaceStringMeta.Exception.UnexpectedErrorInReadingStepInfo" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      for ( int i = 0; i < fieldInStream.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "in_stream_name", fieldInStream[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "out_stream_name", fieldOutStream[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "use_regex", useRegEx[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "replace_string", replaceString[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "replace_by_string", replaceByString[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "set_empty_string", setEmptyString[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "replace_field_by_string", replaceFieldByString[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "whole_world", wholeWord[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "case_sensitive", caseSensitive[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "is_unicode", isUnicode[i] );

      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "ReplaceStringMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    int nrFields = fieldInStream == null ? 0 : fieldInStream.length;
    for ( int i = 0; i < nrFields; i++ ) {
      String fieldName = space.environmentSubstitute( fieldOutStream[i] );
      ValueMetaInterface valueMeta;
      if ( !Utils.isEmpty( fieldOutStream[i] ) ) {
        // We have a new field
        valueMeta = new ValueMetaString( fieldName );
        valueMeta.setOrigin( name );
        //set encoding to new field from source field http://jira.pentaho.com/browse/PDI-11839
        ValueMetaInterface sourceField = inputRowMeta.searchValueMeta( fieldInStream[i] );
        if ( sourceField != null ) {
          valueMeta.setStringEncoding( sourceField.getStringEncoding() );
        }
        inputRowMeta.addValueMeta( valueMeta );
      } else {
        valueMeta = inputRowMeta.searchValueMeta( fieldInStream[i] );
        if ( valueMeta == null ) {
          continue;
        }
        valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
      }
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

    CheckResult cr;
    String error_message = "";
    boolean first = true;
    boolean error_found = false;

    if ( prev == null ) {
      error_message += BaseMessages.getString( PKG, "ReplaceStringMeta.CheckResult.NoInputReceived" ) + Const.CR;
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo );
      remarks.add( cr );
    } else {

      for ( int i = 0; i < fieldInStream.length; i++ ) {
        String field = fieldInStream[i];

        ValueMetaInterface v = prev.searchValueMeta( field );
        if ( v == null ) {
          if ( first ) {
            first = false;
            error_message +=
              BaseMessages.getString( PKG, "ReplaceStringMeta.CheckResult.MissingInStreamFields" ) + Const.CR;
          }
          error_found = true;
          error_message += "\t\t" + field + Const.CR;
        }
      }
      if ( error_found ) {
        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "ReplaceStringMeta.CheckResult.FoundInStreamFields" ), stepinfo );
      }
      remarks.add( cr );

      // Check whether all are strings
      first = true;
      error_found = false;
      for ( int i = 0; i < fieldInStream.length; i++ ) {
        String field = fieldInStream[i];

        ValueMetaInterface v = prev.searchValueMeta( field );
        if ( v != null ) {
          if ( v.getType() != ValueMetaInterface.TYPE_STRING ) {
            if ( first ) {
              first = false;
              error_message +=
                BaseMessages.getString( PKG, "ReplaceStringMeta.CheckResult.OperationOnNonStringFields" )
                  + Const.CR;
            }
            error_found = true;
            error_message += "\t\t" + field + Const.CR;
          }
        }
      }
      if ( error_found ) {
        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "ReplaceStringMeta.CheckResult.AllOperationsOnStringFields" ), stepinfo );
      }
      remarks.add( cr );

      if ( fieldInStream.length > 0 ) {
        for ( int idx = 0; idx < fieldInStream.length; idx++ ) {
          if ( Utils.isEmpty( fieldInStream[idx] ) ) {
            cr =
              new CheckResult(
                CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
                  PKG, "ReplaceStringMeta.CheckResult.InStreamFieldMissing", new Integer( idx + 1 )
                    .toString() ), stepinfo );
            remarks.add( cr );

          }
        }
      }

      // Check if all input fields are distinct.
      for ( int idx = 0; idx < fieldInStream.length; idx++ ) {
        for ( int jdx = 0; jdx < fieldInStream.length; jdx++ ) {
          if ( fieldInStream[idx].equals( fieldInStream[jdx] ) && idx != jdx && idx < jdx ) {
            error_message =
              BaseMessages.getString( PKG, "ReplaceStringMeta.CheckResult.FieldInputError", fieldInStream[idx] );
            cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo );
            remarks.add( cr );
          }
        }
      }

    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new ReplaceString( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new ReplaceStringData();
  }

  public boolean supportsErrorHandling() {
    return true;
  }

  private void nullToEmpty( String [] strings ) {
    for ( int i = 0; i < strings.length; i++ ) {
      if ( strings[ i ] == null ) {
        strings[ i ] = StringUtils.EMPTY;
      }
    }
  }
  /**
   * If we use injection we can have different arrays lengths.
   * We need synchronize them for consistency behavior with UI
   */
  @AfterInjection
  public void afterInjectionSynchronization() {
    int nrFields = ( fieldInStream == null ) ? -1 : fieldInStream.length;
    if ( nrFields <= 0 ) {
      return;
    }
    String[][] rtnStringArrays = Utils.normalizeArrays( nrFields, fieldOutStream, replaceString, replaceByString, replaceFieldByString );
    fieldOutStream = rtnStringArrays[ 0 ];
    replaceString = rtnStringArrays[ 1 ];
    replaceByString = rtnStringArrays[ 2 ];
    replaceFieldByString = rtnStringArrays[ 3 ];

    nullToEmpty( fieldOutStream );
    nullToEmpty( replaceString );
    nullToEmpty( replaceByString );
    nullToEmpty( replaceFieldByString );

    boolean[][] rtnBooleanArrays =
      Utils.normalizeArrays( nrFields, useRegEx, setEmptyString, wholeWord, caseSensitive, isUnicode );
    useRegEx = rtnBooleanArrays[ 0 ];
    setEmptyString = rtnBooleanArrays[ 1 ];
    wholeWord = rtnBooleanArrays[ 2 ];
    caseSensitive = rtnBooleanArrays[ 3 ];
    isUnicode = rtnBooleanArrays[ 4 ];
  }

}
