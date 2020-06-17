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

package org.pentaho.di.job.entries.simpleeval;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines a 'simple evaluation' job entry.
 *
 * @author Samatar Hassan
 * @since 01-01-2009
 */

public class JobEntrySimpleEval extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntrySimpleEval.class; // for i18n purposes, needed by Translator2!!

  public static final String[] valueTypeDesc = new String[] {
    BaseMessages.getString( PKG, "JobSimpleEval.EvalPreviousField.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.EvalVariable.Label" ),

  };
  public static final String[] valueTypeCode = new String[] { "field", "variable" };
  public static final int VALUE_TYPE_FIELD = 0;
  public static final int VALUE_TYPE_VARIABLE = 1;
  public int valuetype;

  public static final String[] successConditionDesc = new String[] {
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenEqual.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenDifferent.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenContains.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenNotContains.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenStartWith.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenNotStartWith.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenEndWith.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenNotEndWith.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenRegExp.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenInList.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenNotInList.Label" ) };
  public static final String[] successConditionCode = new String[] {
    "equal", "different", "contains", "notcontains", "startswith", "notstatwith", "endswith", "notendwith",
    "regexp", "inlist", "notinlist" };

  public static final int SUCCESS_CONDITION_EQUAL = 0;
  public static final int SUCCESS_CONDITION_DIFFERENT = 1;
  public static final int SUCCESS_CONDITION_CONTAINS = 2;
  public static final int SUCCESS_CONDITION_NOT_CONTAINS = 3;
  public static final int SUCCESS_CONDITION_START_WITH = 4;
  public static final int SUCCESS_CONDITION_NOT_START_WITH = 5;
  public static final int SUCCESS_CONDITION_END_WITH = 6;
  public static final int SUCCESS_CONDITION_NOT_END_WITH = 7;
  public static final int SUCCESS_CONDITION_REGEX = 8;
  public static final int SUCCESS_CONDITION_IN_LIST = 9;
  public static final int SUCCESS_CONDITION_NOT_IN_LIST = 10;

  public int successcondition;

  public static final String[] fieldTypeDesc = new String[] {
    BaseMessages.getString( PKG, "JobSimpleEval.FieldTypeString.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.FieldTypeNumber.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.FieldTypeDateTime.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.FieldTypeBoolean.Label" ),

  };
  public static final String[] fieldTypeCode = new String[] { "string", "number", "datetime", "boolean" };
  public static final int FIELD_TYPE_STRING = 0;
  public static final int FIELD_TYPE_NUMBER = 1;
  public static final int FIELD_TYPE_DATE_TIME = 2;
  public static final int FIELD_TYPE_BOOLEAN = 3;

  public int fieldtype;

  public static final String[] successNumberConditionDesc = new String[] {
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenEqual.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenDifferent.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenSmallThan.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenSmallOrEqualThan.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenGreaterThan.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenGreaterOrEqualThan.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessBetween.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenInList.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenNotInList.Label" ), };
  public static final String[] successNumberConditionCode = new String[] {
    "equal", "different", "smaller", "smallequal", "greater", "greaterequal", "between", "inlist", "notinlist" };
  public static final int SUCCESS_NUMBER_CONDITION_EQUAL = 0;
  public static final int SUCCESS_NUMBER_CONDITION_DIFFERENT = 1;
  public static final int SUCCESS_NUMBER_CONDITION_SMALLER = 2;
  public static final int SUCCESS_NUMBER_CONDITION_SMALLER_EQUAL = 3;
  public static final int SUCCESS_NUMBER_CONDITION_GREATER = 4;
  public static final int SUCCESS_NUMBER_CONDITION_GREATER_EQUAL = 5;
  public static final int SUCCESS_NUMBER_CONDITION_BETWEEN = 6;
  public static final int SUCCESS_NUMBER_CONDITION_IN_LIST = 7;
  public static final int SUCCESS_NUMBER_CONDITION_NOT_IN_LIST = 8;

  public int successnumbercondition;

  public static final String[] successBooleanConditionDesc = new String[] {
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenTrue.Label" ),
    BaseMessages.getString( PKG, "JobSimpleEval.SuccessWhenFalse.Label" )

  };
  public static final String[] successBooleanConditionCode = new String[] { "true", "false" };
  public static final int SUCCESS_BOOLEAN_CONDITION_TRUE = 0;
  public static final int SUCCESS_BOOLEAN_CONDITION_FALSE = 1;

  public int successbooleancondition;

  private String fieldname;
  private String variablename;
  private String mask;
  private String comparevalue;
  private String minvalue;
  private String maxvalue;

  private boolean successwhenvarset;

  public JobEntrySimpleEval( String n ) {
    super( n, "" );
    valuetype = VALUE_TYPE_FIELD;
    successcondition = SUCCESS_CONDITION_EQUAL;
    successnumbercondition = SUCCESS_NUMBER_CONDITION_EQUAL;
    successbooleancondition = SUCCESS_BOOLEAN_CONDITION_FALSE;
    minvalue = null;
    maxvalue = null;
    comparevalue = null;
    fieldname = null;
    variablename = null;
    fieldtype = FIELD_TYPE_STRING;
    mask = null;
    successwhenvarset = false;
  }

  public JobEntrySimpleEval() {
    this( "" );
  }

  @Override
  public Object clone() {
    JobEntrySimpleEval je = (JobEntrySimpleEval) super.clone();
    return je;
  }

  private static String getValueTypeCode( int i ) {
    if ( i < 0 || i >= valueTypeCode.length ) {
      return valueTypeCode[0];
    }
    return valueTypeCode[i];
  }

  private static String getFieldTypeCode( int i ) {
    if ( i < 0 || i >= fieldTypeCode.length ) {
      return fieldTypeCode[0];
    }
    return fieldTypeCode[i];
  }

  private static String getSuccessConditionCode( int i ) {
    if ( i < 0 || i >= successConditionCode.length ) {
      return successConditionCode[0];
    }
    return successConditionCode[i];
  }

  public static String getSuccessNumberConditionCode( int i ) {
    if ( i < 0 || i >= successNumberConditionCode.length ) {
      return successNumberConditionCode[0];
    }
    return successNumberConditionCode[i];
  }

  private static String getSuccessBooleanConditionCode( int i ) {
    if ( i < 0 || i >= successBooleanConditionCode.length ) {
      return successBooleanConditionCode[0];
    }
    return successBooleanConditionCode[i];
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "valuetype", getValueTypeCode( valuetype ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "fieldname", fieldname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "variablename", variablename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "fieldtype", getFieldTypeCode( fieldtype ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "mask", mask ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "comparevalue", comparevalue ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "minvalue", minvalue ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "maxvalue", maxvalue ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "successcondition", getSuccessConditionCode( successcondition ) ) );
    retval
      .append( "      " ).append(
        XMLHandler.addTagValue(
          "successnumbercondition", getSuccessNumberConditionCode( successnumbercondition ) ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue(
        "successbooleancondition", getSuccessBooleanConditionCode( successbooleancondition ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "successwhenvarset", successwhenvarset ) );
    return retval.toString();
  }

  private static int getValueTypeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < valueTypeCode.length; i++ ) {
      if ( valueTypeCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getSuccessNumberByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < successNumberConditionCode.length; i++ ) {
      if ( successNumberConditionCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getSuccessBooleanByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < successBooleanConditionCode.length; i++ ) {
      if ( successBooleanConditionCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getFieldTypeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < fieldTypeCode.length; i++ ) {
      if ( fieldTypeCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getSuccessConditionByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < successConditionCode.length; i++ ) {
      if ( successConditionCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public void setSuccessWhenVarSet( boolean successwhenvarset ) {
    this.successwhenvarset = successwhenvarset;
  }

  public boolean isSuccessWhenVarSet() {
    return this.successwhenvarset;
  }

  public static int getSuccessNumberConditionByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < successNumberConditionCode.length; i++ ) {
      if ( successNumberConditionCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getSuccessBooleanConditionByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < successBooleanConditionCode.length; i++ ) {
      if ( successBooleanConditionCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  @Override
  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );

      valuetype = getValueTypeByCode( Const.NVL( XMLHandler.getTagValue( entrynode, "valuetype" ), "" ) );
      fieldname = XMLHandler.getTagValue( entrynode, "fieldname" );
      fieldtype = getFieldTypeByCode( Const.NVL( XMLHandler.getTagValue( entrynode, "fieldtype" ), "" ) );
      variablename = XMLHandler.getTagValue( entrynode, "variablename" );
      mask = XMLHandler.getTagValue( entrynode, "mask" );
      comparevalue = XMLHandler.getTagValue( entrynode, "comparevalue" );
      minvalue = XMLHandler.getTagValue( entrynode, "minvalue" );
      maxvalue = XMLHandler.getTagValue( entrynode, "maxvalue" );
      successcondition =
        getSuccessConditionByCode( Const.NVL( XMLHandler.getTagValue( entrynode, "successcondition" ), "" ) );
      successnumbercondition =
        getSuccessNumberConditionByCode( Const.NVL(
          XMLHandler.getTagValue( entrynode, "successnumbercondition" ), "" ) );
      successbooleancondition =
        getSuccessBooleanConditionByCode( Const.NVL( XMLHandler.getTagValue(
          entrynode, "successbooleancondition" ), "" ) );
      successwhenvarset = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "successwhenvarset" ) );
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException(
        BaseMessages.getString( PKG, "JobEntrySimple.Error.Exception.UnableLoadXML" ), xe );
    }
  }

  @Override
  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      valuetype = getValueTypeByCode( Const.NVL( rep.getJobEntryAttributeString( id_jobentry, "valuetype" ), "" ) );
      fieldname = rep.getJobEntryAttributeString( id_jobentry, "fieldname" );
      variablename = rep.getJobEntryAttributeString( id_jobentry, "variablename" );
      fieldtype = getFieldTypeByCode( Const.NVL( rep.getJobEntryAttributeString( id_jobentry, "fieldtype" ), "" ) );
      mask = rep.getJobEntryAttributeString( id_jobentry, "mask" );
      comparevalue = rep.getJobEntryAttributeString( id_jobentry, "comparevalue" );
      minvalue = rep.getJobEntryAttributeString( id_jobentry, "minvalue" );
      maxvalue = rep.getJobEntryAttributeString( id_jobentry, "maxvalue" );
      successcondition =
        getSuccessConditionByCode( Const.NVL(
          rep.getJobEntryAttributeString( id_jobentry, "successcondition" ), "" ) );
      successnumbercondition =
        getSuccessNumberConditionByCode( Const.NVL( rep.getJobEntryAttributeString(
          id_jobentry, "successnumbercondition" ), "" ) );
      successbooleancondition =
        getSuccessBooleanConditionByCode( Const.NVL( rep.getJobEntryAttributeString(
          id_jobentry, "successbooleancondition" ), "" ) );
      successwhenvarset = rep.getJobEntryAttributeBoolean( id_jobentry, "successwhenvarset" );
    } catch ( KettleException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntrySimple.Error.Exception.UnableLoadRep" )
        + id_jobentry, dbe );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "valuetype", getValueTypeCode( valuetype ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "fieldname", fieldname );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "variablename", variablename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "fieldtype", getFieldTypeCode( fieldtype ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "mask", mask );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "comparevalue", comparevalue );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "minvalue", minvalue );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "maxvalue", maxvalue );
      rep.saveJobEntryAttribute(
        id_job, getObjectId(), "successcondition", getSuccessConditionCode( successcondition ) );
      rep
        .saveJobEntryAttribute(
          id_job, getObjectId(), "successnumbercondition",
          getSuccessNumberConditionCode( successnumbercondition ) );
      rep.saveJobEntryAttribute(
        id_job, getObjectId(), "successbooleancondition",
        getSuccessBooleanConditionCode( successbooleancondition ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "successwhenvarset", successwhenvarset );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntrySimple.Error.Exception.UnableSaveRep" )
        + id_job, dbe );
    }
  }

  @Override
  public Result execute( Result previousResult, int nr ) throws KettleException {
    Result result = previousResult;

    result.setNrErrors( 1 );
    result.setResult( false );

    String sourcevalue = null;
    switch ( valuetype ) {
      case VALUE_TYPE_FIELD:
        List<RowMetaAndData> rows = result.getRows();
        RowMetaAndData resultRow = null;
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntrySimpleEval.Log.ArgFromPrevious.Found", ( rows != null
            ? rows.size() : 0 )
            + "" ) );
        }

        if ( rows.size() == 0 ) {
          rows = null;
          logError( BaseMessages.getString( PKG, "JobEntrySimpleEval.Error.NoRows" ) );
          return result;
        }
        // get first row
        resultRow = rows.get( 0 );
        String realfieldname = environmentSubstitute( fieldname );
        int indexOfField = -1;
        indexOfField = resultRow.getRowMeta().indexOfValue( realfieldname );
        if ( indexOfField == -1 ) {
          logError( BaseMessages.getString( PKG, "JobEntrySimpleEval.Error.FieldNotExist", realfieldname ) );
          resultRow = null;
          rows = null;
          return result;
        }
        sourcevalue = resultRow.getString( indexOfField, null );
        if ( sourcevalue == null ) {
          sourcevalue = "";
        }
        resultRow = null;
        rows = null;
        break;
      case VALUE_TYPE_VARIABLE:

        if ( Utils.isEmpty( variablename ) ) {
          logError( BaseMessages.getString( PKG, "JobEntrySimpleEval.Error.VariableMissing" ) );
          return result;
        }
        if ( isSuccessWhenVarSet() ) {
          // return variable name
          // remove specifications if needed
          String variableName = StringUtil.getVariableName( Const.NVL( getVariableName(), "" ) );
          // Get value, if the variable is not set, Null will be returned
          String value = getVariable( variableName );

          if ( value != null ) {
            if ( isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "JobEntrySimpleEval.VariableSet", variableName ) );
            }
            result.setResult( true );
            result.setNrErrors( 0 );
            return result;
          } else {
            if ( isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "JobEntrySimpleEval.VariableNotSet", variableName ) );
            }
            // PDI-6943: this job entry does not set errors upon evaluation, independently of the outcome of the check
            result.setNrErrors( 0 );
            return result;
          }
        }
        sourcevalue = environmentSubstitute( getVariableWithSpec() );
        break;
      default:
        break;
    }

    if ( isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobSimpleEval.Log.ValueToevaluate", sourcevalue ) );
    }

    boolean success = false;
    String realCompareValue = environmentSubstitute( comparevalue );
    if ( realCompareValue == null ) {
      realCompareValue = "";
    }
    String realMinValue = environmentSubstitute( minvalue );
    String realMaxValue = environmentSubstitute( maxvalue );

    switch ( fieldtype ) {
      case FIELD_TYPE_STRING:
        switch ( successcondition ) {
          case SUCCESS_CONDITION_EQUAL: // equal
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            success = ( sourcevalue.equals( realCompareValue ) );
            if ( valuetype == VALUE_TYPE_VARIABLE && !success ) {
              // make the empty value evaluate to true when compared to a not set variable
              if ( Utils.isEmpty( realCompareValue ) ) {
                String variableName = StringUtil.getVariableName( variablename );
                if ( getVariable( variableName ) == null ) {
                  success = true;
                }
              }
            }
            break;
          case SUCCESS_CONDITION_DIFFERENT: // different
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            success = ( !sourcevalue.equals( realCompareValue ) );
            break;
          case SUCCESS_CONDITION_CONTAINS: // contains
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            success = ( sourcevalue.contains( realCompareValue ) );
            break;
          case SUCCESS_CONDITION_NOT_CONTAINS: // not contains
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            success = ( !sourcevalue.contains( realCompareValue ) );
            break;
          case SUCCESS_CONDITION_START_WITH: // starts with
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            success = ( sourcevalue.startsWith( realCompareValue ) );
            break;
          case SUCCESS_CONDITION_NOT_START_WITH: // not start with
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            success = ( !sourcevalue.startsWith( realCompareValue ) );
            break;
          case SUCCESS_CONDITION_END_WITH: // ends with
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            success = ( sourcevalue.endsWith( realCompareValue ) );
            break;
          case SUCCESS_CONDITION_NOT_END_WITH: // not ends with
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            success = ( !sourcevalue.endsWith( realCompareValue ) );
            break;
          case SUCCESS_CONDITION_REGEX: // regexp
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            success = ( Pattern.compile( realCompareValue ).matcher( sourcevalue ).matches() );
            break;
          case SUCCESS_CONDITION_IN_LIST: // in list
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            realCompareValue = Const.NVL( realCompareValue, "" );
            String[] parts = realCompareValue.split( "," );
            for ( int i = 0; i < parts.length && !success; i++ ) {
              success = ( sourcevalue.equals( parts[i].trim() ) );
            }
            break;
          case SUCCESS_CONDITION_NOT_IN_LIST: // not in list
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            realCompareValue = Const.NVL( realCompareValue, "" );
            parts = realCompareValue.split( "," );
            success = true;
            for ( int i = 0; i < parts.length && success; i++ ) {
              success = !( sourcevalue.equals( parts[i].trim() ) );
            }
            break;
          default:
            break;
        }
        break;
      case FIELD_TYPE_NUMBER:
        double valuenumber;
        try {
          valuenumber = Double.parseDouble( sourcevalue );
        } catch ( Exception e ) {
          logError( BaseMessages.getString( PKG, "JobEntrySimpleEval.Error.UnparsableNumber", sourcevalue, e
            .getMessage() ) );
          return result;
        }

        double valuecompare;
        switch ( successnumbercondition ) {
          case SUCCESS_NUMBER_CONDITION_EQUAL: // equal
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            try {
              valuecompare = Double.parseDouble( realCompareValue );
            } catch ( Exception e ) {
              logError( BaseMessages.getString(
                PKG, "JobEntrySimpleEval.Error.UnparsableNumber", realCompareValue, e.getMessage() ) );
              return result;
            }
            success = ( valuenumber == valuecompare );
            break;
          case SUCCESS_NUMBER_CONDITION_DIFFERENT: // different
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            try {
              valuecompare = Double.parseDouble( realCompareValue );
            } catch ( Exception e ) {
              logError( BaseMessages.getString(
                PKG, "JobEntrySimpleEval.Error.UnparsableNumber", realCompareValue, e.getMessage() ) );
              return result;
            }
            success = ( valuenumber != valuecompare );
            break;
          case SUCCESS_NUMBER_CONDITION_SMALLER: // smaller
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            try {
              valuecompare = Double.parseDouble( realCompareValue );
            } catch ( Exception e ) {
              logError( BaseMessages.getString(
                PKG, "JobEntrySimpleEval.Error.UnparsableNumber", realCompareValue, e.getMessage() ) );
              return result;
            }
            success = ( valuenumber < valuecompare );
            break;
          case SUCCESS_NUMBER_CONDITION_SMALLER_EQUAL: // smaller or equal
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            try {
              valuecompare = Double.parseDouble( realCompareValue );
            } catch ( Exception e ) {
              logError( BaseMessages.getString(
                PKG, "JobEntrySimpleEval.Error.UnparsableNumber", realCompareValue, e.getMessage() ) );
              return result;
            }
            success = ( valuenumber <= valuecompare );
            break;
          case SUCCESS_NUMBER_CONDITION_GREATER: // greater
            try {
              valuecompare = Double.parseDouble( realCompareValue );
            } catch ( Exception e ) {
              logError( BaseMessages.getString(
                PKG, "JobEntrySimpleEval.Error.UnparsableNumber", realCompareValue, e.getMessage() ) );
              return result;
            }
            success = ( valuenumber > valuecompare );
            break;
          case SUCCESS_NUMBER_CONDITION_GREATER_EQUAL: // greater or equal
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            try {
              valuecompare = Double.parseDouble( realCompareValue );
            } catch ( Exception e ) {
              logError( BaseMessages.getString(
                PKG, "JobEntrySimpleEval.Error.UnparsableNumber", realCompareValue, e.getMessage() ) );
              return result;
            }
            success = ( valuenumber >= valuecompare );
            break;
          case SUCCESS_NUMBER_CONDITION_BETWEEN: // between min and max
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValues", realMinValue, realMaxValue ) );
            }
            double valuemin;
            try {
              valuemin = Double.parseDouble( realMinValue );
            } catch ( Exception e ) {
              logError( BaseMessages.getString( PKG, "JobEntrySimpleEval.Error.UnparsableNumber", realMinValue, e
                .getMessage() ) );
              return result;
            }
            double valuemax;
            try {
              valuemax = Double.parseDouble( realMaxValue );
            } catch ( Exception e ) {
              logError( BaseMessages.getString( PKG, "JobEntrySimpleEval.Error.UnparsableNumber", realMaxValue, e
                .getMessage() ) );
              return result;
            }

            if ( valuemin >= valuemax ) {
              logError( BaseMessages.getString(
                PKG, "JobEntrySimpleEval.Error.IncorrectNumbers", realMinValue, realMaxValue ) );
              return result;
            }
            success = ( valuenumber >= valuemin && valuenumber <= valuemax );
            break;
          case SUCCESS_NUMBER_CONDITION_IN_LIST: // in list
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            String[] parts = realCompareValue.split( "," );

            for ( int i = 0; i < parts.length && !success; i++ ) {
              try {
                valuecompare = Double.parseDouble( parts[i] );
              } catch ( Exception e ) {
                logError( toString(), BaseMessages.getString(
                  PKG, "JobEntrySimpleEval.Error.UnparsableNumber", parts[i], e.getMessage() ) );
                return result;
              }
              success = ( valuenumber == valuecompare );
            }
            break;
          case SUCCESS_NUMBER_CONDITION_NOT_IN_LIST: // not in list
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            realCompareValue = Const.NVL( realCompareValue, "" );
            parts = realCompareValue.split( "," );
            success = true;
            for ( int i = 0; i < parts.length && success; i++ ) {
              try {
                valuecompare = Double.parseDouble( parts[i] );
              } catch ( Exception e ) {
                logError( toString(), BaseMessages.getString(
                  PKG, "JobEntrySimpleEval.Error.UnparsableNumber", parts[i], e.getMessage() ) );
                return result;
              }

              success = ( valuenumber != valuecompare );
            }
            break;
          default:
            break;
        }
        break;
      case FIELD_TYPE_DATE_TIME:
        String realMask = environmentSubstitute( mask );
        SimpleDateFormat df = new SimpleDateFormat();
        if ( !Utils.isEmpty( realMask ) ) {
          df.applyPattern( realMask );
        }

        Date datevalue = null;
        try {
          datevalue = convertToDate( sourcevalue, realMask, df );
        } catch ( Exception e ) {
          logError( e.getMessage() );
          return result;
        }

        Date datecompare;
        switch ( successnumbercondition ) {
          case SUCCESS_NUMBER_CONDITION_EQUAL: // equal
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            try {
              datecompare = convertToDate( realCompareValue, realMask, df );
            } catch ( Exception e ) {
              logError( e.getMessage() );
              return result;
            }
            success = ( datevalue.equals( datecompare ) );
            break;
          case SUCCESS_NUMBER_CONDITION_DIFFERENT: // different
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            try {
              datecompare = convertToDate( realCompareValue, realMask, df );
            } catch ( Exception e ) {
              logError( e.getMessage() );
              return result;
            }
            success = ( !datevalue.equals( datecompare ) );
            break;
          case SUCCESS_NUMBER_CONDITION_SMALLER: // smaller
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            try {
              datecompare = convertToDate( realCompareValue, realMask, df );
            } catch ( Exception e ) {
              logError( e.getMessage() );
              return result;
            }
            success = ( datevalue.before( datecompare ) );
            break;
          case SUCCESS_NUMBER_CONDITION_SMALLER_EQUAL: // smaller or equal
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            try {
              datecompare = convertToDate( realCompareValue, realMask, df );
            } catch ( Exception e ) {
              logError( e.getMessage() );
              return result;
            }
            success = ( datevalue.before( datecompare ) || datevalue.equals( datecompare ) );
            break;
          case SUCCESS_NUMBER_CONDITION_GREATER: // greater
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            try {
              datecompare = convertToDate( realCompareValue, realMask, df );
            } catch ( Exception e ) {
              logError( e.getMessage() );
              return result;
            }
            success = ( datevalue.after( datecompare ) );
            break;
          case SUCCESS_NUMBER_CONDITION_GREATER_EQUAL: // greater or equal
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            try {
              datecompare = convertToDate( realCompareValue, realMask, df );
            } catch ( Exception e ) {
              logError( e.getMessage() );
              return result;
            }
            success = ( datevalue.after( datecompare ) || datevalue.equals( datecompare ) );
            break;
          case SUCCESS_NUMBER_CONDITION_BETWEEN: // between min and max
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValues", realMinValue, realMaxValue ) );
            }
            Date datemin;
            try {
              datemin = convertToDate( realMinValue, realMask, df );
            } catch ( Exception e ) {
              logError( e.getMessage() );
              return result;
            }

            Date datemax;
            try {
              datemax = convertToDate( realMaxValue, realMask, df );
            } catch ( Exception e ) {
              logError( e.getMessage() );
              return result;
            }

            if ( datemin.after( datemax ) || datemin.equals( datemax ) ) {
              logError( BaseMessages.getString(
                PKG, "JobEntrySimpleEval.Error.IncorrectDates", realMinValue, realMaxValue ) );
              return result;
            }

            success =
              ( ( datevalue.after( datemin )
              || datevalue.equals( datemin ) ) && ( datevalue.before( datemax )
              || datevalue.equals( datemax ) ) );
            break;
          case SUCCESS_NUMBER_CONDITION_IN_LIST: // in list
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            String[] parts = realCompareValue.split( "," );

            for ( int i = 0; i < parts.length && !success; i++ ) {
              try {
                datecompare = convertToDate( realCompareValue, realMask, df );
              } catch ( Exception e ) {
                logError( toString(), e.getMessage() );
                return result;
              }
              success = ( datevalue.equals( datecompare ) );
            }
            break;
          case SUCCESS_NUMBER_CONDITION_NOT_IN_LIST: // not in list
            if ( isDebug() ) {
              logDebug( BaseMessages.getString(
                PKG, "JobSimpleEval.Log.CompareWithValue", sourcevalue, realCompareValue ) );
            }
            realCompareValue = Const.NVL( realCompareValue, "" );
            parts = realCompareValue.split( "," );
            success = true;
            for ( int i = 0; i < parts.length && success; i++ ) {
              try {
                datecompare = convertToDate( realCompareValue, realMask, df );
              } catch ( Exception e ) {
                logError( toString(), e.getMessage() );
                return result;
              }
              success = ( !datevalue.equals( datecompare ) );
            }
            break;
          default:
            break;
        }
        df = null;
        break;
      case FIELD_TYPE_BOOLEAN:
        boolean valuebool;
        try {
          valuebool = ValueMetaString.convertStringToBoolean( sourcevalue );
        } catch ( Exception e ) {
          logError( BaseMessages.getString( PKG, "JobEntrySimpleEval.Error.UnparsableBoolean", sourcevalue, e
            .getMessage() ) );
          return result;
        }

        switch ( successbooleancondition ) {
          case SUCCESS_BOOLEAN_CONDITION_FALSE: // false
            success = ( !valuebool );
            break;
          case SUCCESS_BOOLEAN_CONDITION_TRUE: // true
            success = ( valuebool );
            break;
          default:
            break;
        }
        break;
      default:
        break;
    }

    result.setResult( success );
    // PDI-6943: this job entry does not set errors upon evaluation, independently of the outcome of the check
    result.setNrErrors( 0 );
    return result;
  }

  /*
   * Returns variable with specifications
   */
  private String getVariableWithSpec() {
    String variable = getVariableName();
    if ( ( !variable.contains( StringUtil.UNIX_OPEN ) && !variable.contains( StringUtil.WINDOWS_OPEN ) && !variable
      .contains( StringUtil.HEX_OPEN ) )
      && ( ( !variable.contains( StringUtil.UNIX_CLOSE ) && !variable.contains( StringUtil.WINDOWS_CLOSE ) && !variable
        .contains( StringUtil.HEX_CLOSE ) ) ) ) {
      // Add specifications to variable
      variable = StringUtil.UNIX_OPEN + variable + StringUtil.UNIX_CLOSE;
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntrySimpleEval.CheckingVariable", variable ) );
      }
    }
    return variable;
  }

  private Date convertToDate( String valueString, String mask, SimpleDateFormat df ) throws KettleException {
    Date datevalue = null;
    try {
      datevalue = df.parse( valueString );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "JobEntrySimpleEval.Error.UnparsableDate", valueString ) );
    }
    return datevalue;
  }

  public static String getValueTypeDesc( int i ) {
    if ( i < 0 || i >= valueTypeDesc.length ) {
      return valueTypeDesc[0];
    }
    return valueTypeDesc[i];
  }

  public static String getFieldTypeDesc( int i ) {
    if ( i < 0 || i >= fieldTypeDesc.length ) {
      return fieldTypeDesc[0];
    }
    return fieldTypeDesc[i];
  }

  public static String getSuccessConditionDesc( int i ) {
    if ( i < 0 || i >= successConditionDesc.length ) {
      return successConditionDesc[0];
    }
    return successConditionDesc[i];
  }

  public static String getSuccessNumberConditionDesc( int i ) {
    if ( i < 0 || i >= successNumberConditionDesc.length ) {
      return successNumberConditionDesc[0];
    }
    return successNumberConditionDesc[i];
  }

  public static String getSuccessBooleanConditionDesc( int i ) {
    if ( i < 0 || i >= successBooleanConditionDesc.length ) {
      return successBooleanConditionDesc[0];
    }
    return successBooleanConditionDesc[i];
  }

  public static int getValueTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < valueTypeDesc.length; i++ ) {
      if ( valueTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getValueTypeByCode( tt );
  }

  public static int getFieldTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < fieldTypeDesc.length; i++ ) {
      if ( fieldTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getFieldTypeByCode( tt );
  }

  public static int getSuccessConditionByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < successConditionDesc.length; i++ ) {
      if ( successConditionDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getSuccessConditionByCode( tt );
  }

  public static int getSuccessNumberConditionByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < successNumberConditionDesc.length; i++ ) {
      if ( successNumberConditionDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getSuccessNumberByCode( tt );
  }

  public static int getSuccessBooleanConditionByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < successBooleanConditionDesc.length; i++ ) {
      if ( successBooleanConditionDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }

    // If this fails, try to match using the code.
    return getSuccessBooleanByCode( tt );
  }

  public void setMinValue( String minvalue ) {
    this.minvalue = minvalue;
  }

  public String getMinValue() {
    return minvalue;
  }

  public void setCompareValue( String comparevalue ) {
    this.comparevalue = comparevalue;
  }

  public String getMask() {
    return mask;
  }

  public void setMask( String mask ) {
    this.mask = mask;
  }

  public String getFieldName() {
    return fieldname;
  }

  public void setFieldName( String fieldname ) {
    this.fieldname = fieldname;
  }

  public String getVariableName() {
    return variablename;
  }

  public void setVariableName( String variablename ) {
    this.variablename = variablename;
  }

  public String getCompareValue() {
    return comparevalue;
  }

  public void setMaxValue( String maxvalue ) {
    this.maxvalue = maxvalue;
  }

  public String getMaxValue() {
    return maxvalue;
  }

  @Override
  public boolean evaluates() {
    return true;
  }

}
