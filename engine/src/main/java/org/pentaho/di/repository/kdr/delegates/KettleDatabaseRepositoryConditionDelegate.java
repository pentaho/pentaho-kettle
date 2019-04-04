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

package org.pentaho.di.repository.kdr.delegates;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

public class KettleDatabaseRepositoryConditionDelegate extends KettleDatabaseRepositoryBaseDelegate {

  // private static Class<?> PKG = Condition.class; // for i18n purposes, needed by Translator2!!

  public KettleDatabaseRepositoryConditionDelegate( KettleDatabaseRepository repository ) {
    super( repository );
  }

  public RowMetaAndData getCondition( ObjectId id_condition ) throws KettleException {
    return repository.connectionDelegate.getOneRow(
      quoteTable( KettleDatabaseRepository.TABLE_R_CONDITION ),
      quote( KettleDatabaseRepository.FIELD_CONDITION_ID_CONDITION ), id_condition );
  }

  /**
   *
   * Read a condition from the repository.
   *
   * @param id_condition
   *          The condition id
   * @throws KettleException
   *           if something goes wrong.
   */
  public Condition loadCondition( ObjectId id_condition ) throws KettleException {
    Condition condition = new Condition();

    try {
      RowMetaAndData r = getCondition( id_condition );
      if ( r != null ) {
        condition.setNegated( r.getBoolean( "NEGATED", false ) );
        condition.setOperator( Condition.getOperator( r.getString( "OPERATOR", null ) ) );

        long conditionId = r.getInteger( "ID_CONDITION", -1L );
        if ( conditionId > 0 ) {
          condition.setObjectId( new LongObjectId( conditionId ) );
        } else {
          condition.setObjectId( null );
        }

        ObjectId[] subids = repository.getSubConditionIDs( condition.getObjectId() );
        if ( subids.length == 0 ) {
          condition.setLeftValuename( r.getString( "LEFT_NAME", null ) );
          condition.setFunction( Condition.getFunction( r.getString( "CONDITION_FUNCTION", null ) ) );
          condition.setRightValuename( r.getString( "RIGHT_NAME", null ) );

          long id_value = r.getInteger( "ID_VALUE_RIGHT", -1L );
          if ( id_value > 0 ) {
            ValueMetaAndData v = repository.loadValueMetaAndData( new LongObjectId( id_value ) );
            condition.setRightExact( v );
          }
        } else {
          for ( int i = 0; i < subids.length; i++ ) {
            condition.addCondition( loadCondition( subids[i] ) );
          }
        }

        return condition;
      } else {
        throw new KettleException( "Condition with id_condition="
          + id_condition + " could not be found in the repository" );
      }
    } catch ( KettleException dbe ) {
      throw new KettleException(
        "Error loading condition from the repository (id_condition=" + id_condition + ")", dbe );
    }
  }

  public ObjectId saveCondition( Condition condition ) throws KettleException {
    return saveCondition( condition, null );
  }

  public ObjectId saveCondition( Condition condition, ObjectId id_condition_parent ) throws KettleException {
    try {
      condition.setObjectId( insertCondition( id_condition_parent, condition ) );
      for ( int i = 0; i < condition.nrConditions(); i++ ) {
        Condition subc = condition.getCondition( i );
        repository.saveCondition( subc, condition.getObjectId() );
      }

      return condition.getObjectId();
    } catch ( KettleException dbe ) {
      throw new KettleException( "Error saving condition to the repository.", dbe );
    }
  }

  public synchronized ObjectId insertCondition( ObjectId id_condition_parent, Condition condition ) throws KettleException {
    ObjectId id = repository.connectionDelegate.getNextConditionID();

    String tablename = KettleDatabaseRepository.TABLE_R_CONDITION;
    RowMetaAndData table = new RowMetaAndData();
    table.addValue( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_CONDITION_ID_CONDITION ), id );
    table.addValue(
      new ValueMetaInteger(
        KettleDatabaseRepository.FIELD_CONDITION_ID_CONDITION_PARENT ),
      id_condition_parent );
    table.addValue( new ValueMetaBoolean(
      KettleDatabaseRepository.FIELD_CONDITION_NEGATED ), Boolean
      .valueOf( condition.isNegated() ) );
    table.addValue( new ValueMetaString(
      KettleDatabaseRepository.FIELD_CONDITION_OPERATOR ), condition
      .getOperatorDesc() );
    table.addValue( new ValueMetaString(
      KettleDatabaseRepository.FIELD_CONDITION_LEFT_NAME ), condition
      .getLeftValuename() );
    table.addValue( new ValueMetaString(
      KettleDatabaseRepository.FIELD_CONDITION_CONDITION_FUNCTION ), condition
      .getFunctionDesc() );
    table.addValue( new ValueMetaString(
      KettleDatabaseRepository.FIELD_CONDITION_RIGHT_NAME ), condition
      .getRightValuename() );

    ObjectId id_value = null;
    ValueMetaAndData v = condition.getRightExact();

    if ( v != null ) {

      // We have to make sure that all data is saved irrespective of locale differences.
      // Here is where we force that
      //
      ValueMetaInterface valueMeta = v.getValueMeta();
      valueMeta.setDecimalSymbol( ValueMetaAndData.VALUE_REPOSITORY_DECIMAL_SYMBOL );
      valueMeta.setGroupingSymbol( ValueMetaAndData.VALUE_REPOSITORY_GROUPING_SYMBOL );
      switch ( valueMeta.getType() ) {
        case ValueMetaInterface.TYPE_NUMBER:
          valueMeta.setConversionMask( ValueMetaAndData.VALUE_REPOSITORY_NUMBER_CONVERSION_MASK );
          break;
        case ValueMetaInterface.TYPE_INTEGER:
          valueMeta.setConversionMask( ValueMetaAndData.VALUE_REPOSITORY_INTEGER_CONVERSION_MASK );
          break;
        case ValueMetaInterface.TYPE_DATE:
          valueMeta.setConversionMask( ValueMetaAndData.VALUE_REPOSITORY_DATE_CONVERSION_MASK );
          break;
        default:
          break;
      }
      String stringValue = valueMeta.getString( v.getValueData() );

      id_value =
        insertValue( valueMeta.getName(), valueMeta.getTypeDesc(), stringValue, valueMeta.isNull( v
          .getValueData() ), condition.getRightExactID() );
      condition.setRightExactID( id_value );
    }
    table.addValue( new ValueMetaInteger(
      KettleDatabaseRepository.FIELD_CONDITION_ID_VALUE_RIGHT ), id_value );

    repository.connectionDelegate.getDatabase().prepareInsert( table.getRowMeta(), tablename );
    repository.connectionDelegate.getDatabase().setValuesInsert( table );
    repository.connectionDelegate.getDatabase().insertRow();
    repository.connectionDelegate.getDatabase().closeInsert();

    return id;
  }

  public synchronized ObjectId insertValue( String name, String type, String value_str, boolean isnull,
    ObjectId id_value_prev ) throws KettleException {
    ObjectId id_value = lookupValue( name, type, value_str, isnull );
    // if it didn't exist yet: insert it!!

    if ( id_value == null ) {
      id_value = repository.connectionDelegate.getNextValueID();

      // Let's see if the same value is not yet available?
      String tablename = KettleDatabaseRepository.TABLE_R_VALUE;
      RowMetaAndData table = new RowMetaAndData();
      table.addValue( new ValueMetaInteger(
        KettleDatabaseRepository.FIELD_VALUE_ID_VALUE ), id_value );
      table.addValue(
        new ValueMetaString( KettleDatabaseRepository.FIELD_VALUE_NAME ), name );
      table.addValue( new ValueMetaString(
        KettleDatabaseRepository.FIELD_VALUE_VALUE_TYPE ), type );
      table.addValue( new ValueMetaString(
        KettleDatabaseRepository.FIELD_VALUE_VALUE_STR ), value_str );
      table.addValue(
        new ValueMetaBoolean( KettleDatabaseRepository.FIELD_VALUE_IS_NULL ), Boolean
          .valueOf( isnull ) );

      repository.connectionDelegate.getDatabase().prepareInsert( table.getRowMeta(), tablename );
      repository.connectionDelegate.getDatabase().setValuesInsert( table );
      repository.connectionDelegate.getDatabase().insertRow();
      repository.connectionDelegate.getDatabase().closeInsert();
    }

    return id_value;
  }

  public synchronized ObjectId lookupValue( String name, String type, String value_str, boolean isnull ) throws KettleException {
    RowMetaAndData table = new RowMetaAndData();
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_VALUE_NAME ), name );
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_VALUE_VALUE_TYPE ), type );
    table
      .addValue(
        new ValueMetaString( KettleDatabaseRepository.FIELD_VALUE_VALUE_STR ),
        value_str );
    table.addValue(
      new ValueMetaBoolean( KettleDatabaseRepository.FIELD_VALUE_IS_NULL ), Boolean
        .valueOf( isnull ) );

    String sql =
      "SELECT "
        + quote( KettleDatabaseRepository.FIELD_VALUE_ID_VALUE ) + " FROM "
        + quoteTable( KettleDatabaseRepository.TABLE_R_VALUE ) + " ";
    sql += "WHERE " + quote( KettleDatabaseRepository.FIELD_VALUE_NAME ) + "       = ? ";
    sql += "AND   " + quote( KettleDatabaseRepository.FIELD_VALUE_VALUE_TYPE ) + " = ? ";
    sql += "AND   " + quote( KettleDatabaseRepository.FIELD_VALUE_VALUE_STR ) + "  = ? ";
    sql += "AND   " + quote( KettleDatabaseRepository.FIELD_VALUE_IS_NULL ) + "    = ? ";

    RowMetaAndData result = repository.connectionDelegate.getOneRow( sql, table.getRowMeta(), table.getData() );
    if ( result != null && result.getData() != null && result.isNumeric( 0 ) ) {
      return new LongObjectId( result.getInteger( 0, 0L ) );
    } else {
      return null;
    }
  }

}
