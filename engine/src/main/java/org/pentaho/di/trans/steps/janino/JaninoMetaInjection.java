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

package org.pentaho.di.trans.steps.janino;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInjectionUtil;
import org.pentaho.di.trans.step.StepMetaInjectionEntryInterface;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This takes care of the external metadata injection into the JaninoMeta class
 *
 * @author Chris
 */
public class JaninoMetaInjection implements StepMetaInjectionInterface {

  public enum Entry implements StepMetaInjectionEntryInterface {

    EXPRESSION_FIELDS( ValueMetaInterface.TYPE_NONE, "The formula fields" ),
      EXPRESSION_FIELD( ValueMetaInterface.TYPE_NONE, "One formula field" ),
      NEW_FIELDNAME( ValueMetaInterface.TYPE_STRING, "New field" ),
      JAVA_EXPRESSION( ValueMetaInterface.TYPE_STRING, "Java expression" ),
      VALUE_TYPE( ValueMetaInterface.TYPE_STRING, "Value type (For valid values go to http://wiki.pentaho.com/display/EAI/User+Defined+Java+Expression)" ),
      LENGTH( ValueMetaInterface.TYPE_STRING, "Length" ),
      PRECISION( ValueMetaInterface.TYPE_STRING, "Precision" ),
      REPLACE_VALUE( ValueMetaInterface.TYPE_STRING, "Replace value" );

    private int valueType;
    private String description;

    private Entry( int valueType, String description ) {
      this.valueType = valueType;
      this.description = description;
    }

    /**
     * @return the valueType
     */
    public int getValueType() {
      return valueType;
    }

    /**
     * @return the description
     */
    public String getDescription() {
      return description;
    }

    public static Entry findEntry( String key ) {
      return Entry.valueOf( key );
    }
  }

  private JaninoMeta meta;

  public JaninoMetaInjection( JaninoMeta meta ) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    // The fields
    //
    StepInjectionMetaEntry fieldsEntry =
      new StepInjectionMetaEntry(
        Entry.EXPRESSION_FIELDS.name(), ValueMetaInterface.TYPE_NONE, Entry.EXPRESSION_FIELDS.description );
    all.add( fieldsEntry );
    StepInjectionMetaEntry fieldEntry =
      new StepInjectionMetaEntry(
        Entry.EXPRESSION_FIELD.name(), ValueMetaInterface.TYPE_NONE, Entry.EXPRESSION_FIELD.description );
    fieldsEntry.getDetails().add( fieldEntry );

    Entry[] fieldsEntries = new Entry[] { Entry.NEW_FIELDNAME, Entry.JAVA_EXPRESSION, Entry.VALUE_TYPE,
      Entry.LENGTH, Entry.PRECISION, Entry.REPLACE_VALUE, };
    for ( Entry entry : fieldsEntries ) {
      StepInjectionMetaEntry metaEntry =
        new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
      fieldEntry.getDetails().add( metaEntry );
    }

    return all;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {

    List<String> fieldNames = new ArrayList<String>();
    List<String> javaExpressions = new ArrayList<String>();
    List<String> valueTypes = new ArrayList<String>();
    List<String> lengths = new ArrayList<String>();
    List<String> precisions = new ArrayList<String>();
    List<String> replaceValues = new ArrayList<String>();

    // Parse the fields, inject into the meta class..
    //
    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry == null ) {
        continue;
      }

      switch ( fieldsEntry ) {
        case EXPRESSION_FIELDS:
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry == Entry.EXPRESSION_FIELD ) {

              String newFieldname = null;
              String javaExpression = null;
              String valueType = null;
              String length = null;
              String precision = null;
              String replaceValue = null;

              List<StepInjectionMetaEntry> entries = lookField.getDetails();
              for ( StepInjectionMetaEntry entry : entries ) {
                Entry metaEntry = Entry.findEntry( entry.getKey() );
                if ( metaEntry != null ) {
                  String value = (String) entry.getValue();
                  switch ( metaEntry ) {
                    case NEW_FIELDNAME:
                      newFieldname = value;
                      break;
                    case JAVA_EXPRESSION:
                      javaExpression = value;
                      break;
                    case VALUE_TYPE:
                      valueType = value;
                      break;
                    case LENGTH:
                      length = value;
                      break;
                    case PRECISION:
                      precision = value;
                      break;
                    case REPLACE_VALUE:
                      replaceValue = value;
                      break;
                    default:
                      break;
                  }
                }
              }
              fieldNames.add( newFieldname );
              javaExpressions.add( javaExpression );
              valueTypes.add( valueType );
              lengths.add( length );
              precisions.add( precision );
              replaceValues.add( replaceValue );

            }
          }
          break;
        default:
          break;
      }
    }

    // Pass the grid to the step metadata
    //
    if ( fieldNames.size() > 0 ) {
      JaninoMetaFunction[] fields = new JaninoMetaFunction[ fieldNames.size() ];

      Iterator<String> iFieldNames = fieldNames.iterator();
      Iterator<String> iJavaExpressions = javaExpressions.iterator();
      Iterator<String> iValueTypes = valueTypes.iterator();
      Iterator<String> iLengths = lengths.iterator();
      Iterator<String> iPrecisions = precisions.iterator();
      Iterator<String> iReplaceValues = replaceValues.iterator();

      int i = 0;

      while ( iFieldNames.hasNext() ) {
        fields[i] = new JaninoMetaFunction( iFieldNames.next(), iJavaExpressions.next(),
          ValueMetaFactory.getIdForValueMeta( iValueTypes.next() ), Const.toInt( iLengths.next(), -1 ),
            Const.toInt( iPrecisions.next(), -1 ), iReplaceValues.next() );

        i++;
      }

      meta.setFormula( fields );
    }
  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> list = new ArrayList<StepInjectionMetaEntry>();

    StepInjectionMetaEntry fieldsEntry = StepInjectionUtil.getEntry( Entry.EXPRESSION_FIELDS );
    list.add( fieldsEntry );
    for ( int i = 0; i < meta.getFormula().length; i++ ) {
      StepInjectionMetaEntry fieldEntry = StepInjectionUtil.getEntry( Entry.EXPRESSION_FIELD );
      List<StepInjectionMetaEntry> details = fieldEntry.getDetails();
      details.add( StepInjectionUtil.getEntry( Entry.NEW_FIELDNAME, meta.getFormula()[i].getFieldName() ) );
      details.add( StepInjectionUtil.getEntry( Entry.JAVA_EXPRESSION, meta.getFormula()[i].getFormula() ) );
      details.add( StepInjectionUtil.getEntry( Entry.VALUE_TYPE, ValueMetaFactory.getValueMetaName( meta.getFormula()[i].getValueType() ) ) );
      details.add( StepInjectionUtil.getEntry( Entry.LENGTH, meta.getFormula()[i].getValueLength() ) );
      details.add( StepInjectionUtil.getEntry( Entry.PRECISION, meta.getFormula()[i].getValuePrecision() ) );
      details.add( StepInjectionUtil.getEntry( Entry.REPLACE_VALUE, meta.getFormula()[i].getReplaceField() ) );


      fieldsEntry.getDetails().add( fieldEntry );
    }

    return list;
  }

  public JaninoMeta getMeta() {
    return meta;
  }
}
