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

package org.pentaho.di.trans.steps.excelwriter;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Injection support for the Excel Writer step.
 * <p/>
 * Injection only supported for attributes of the output fields.
 *
 * @author Gretchen Moran
 */
public class ExcelWriterMetaInjection implements StepMetaInjectionInterface {

  private static Class<?> PKG = ExcelWriterStepMeta.class; // for i18n purposes, needed by Translator2!!

  private ExcelWriterStepMeta meta;

  public ExcelWriterMetaInjection( ExcelWriterStepMeta meta ) {
    this.meta = meta;
  }

  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    StepInjectionMetaEntry fieldsEntry =
      new StepInjectionMetaEntry( "FIELDS",
        ValueMetaInterface.TYPE_NONE, BaseMessages.getString( PKG, "ExcelWriterMetaInjection.AllFields" ) );
    all.add( fieldsEntry );

    StepInjectionMetaEntry fieldEntry =
      new StepInjectionMetaEntry( "FIELD",
        ValueMetaInterface.TYPE_NONE, BaseMessages.getString( PKG, "ExcelWriterMetaInjection.AllFields" ) );
    fieldsEntry.getDetails().add( fieldEntry );

    for ( Entry entry : Entry.values() ) {
      if ( entry.getValueType() != ValueMetaInterface.TYPE_NONE ) {
        StepInjectionMetaEntry metaEntry =
          new StepInjectionMetaEntry( entry.name(), entry.getValueType(), entry.getDescription() );
        fieldEntry.getDetails().add( metaEntry );
      }
    }

    return all;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> all ) throws KettleException {

    List<ExcelWriterStepField> excelOutputFields = new ArrayList<ExcelWriterStepField>();

    // Parse the fields in the Excel Step, setting the metadata based on values passed.

    for ( StepInjectionMetaEntry lookFields : all ) {
      Entry fieldsEntry = Entry.findEntry( lookFields.getKey() );
      if ( fieldsEntry != null ) {
        if ( fieldsEntry == Entry.FIELDS ) {
          for ( StepInjectionMetaEntry lookField : lookFields.getDetails() ) {
            Entry fieldEntry = Entry.findEntry( lookField.getKey() );
            if ( fieldEntry != null ) {
              if ( fieldEntry == Entry.FIELD ) {

                ExcelWriterStepField excelOutputField = new ExcelWriterStepField();

                List<StepInjectionMetaEntry> entries = lookField.getDetails();
                for ( StepInjectionMetaEntry entry : entries ) {
                  Entry metaEntry = Entry.findEntry( entry.getKey() );
                  if ( metaEntry != null ) {
                    Object value = entry.getValue();
                    if ( value != null ) {
                      switch ( metaEntry ) {
                        case NAME:
                          excelOutputField.setName( (String) value );
                          break;
                        case TYPE:
                          excelOutputField.setType( (String) value );
                          break;
                        case FORMAT:
                          excelOutputField.setFormat( (String) value );
                          break;
                        case STYLECELL:
                          excelOutputField.setStyleCell( (String) value );
                          break;
                        case FIELDTITLE:
                          excelOutputField.setTitle( (String) value );
                          break;
                        case TITLESTYLE:
                          excelOutputField.setTitleStyleCell( (String) value );
                          break;
                        case FORMULA:
                          excelOutputField.setFormula( (Boolean) value );
                          break;
                        case HYPERLINKFIELD:
                          excelOutputField.setHyperlinkField( (String) value );
                          break;
                        case CELLCOMMENT:
                          excelOutputField.setCommentField( (String) value );
                          break;
                        case COMMENTAUTHOR:
                          excelOutputField.setCommentAuthorField( (String) value );
                          break;
                        default:
                          break;
                      }
                    }
                  }
                }

                excelOutputFields.add( excelOutputField );
              }
            }
          }
        }
      }
    }

    meta.setOutputFields( excelOutputFields.toArray( new ExcelWriterStepField[excelOutputFields.size()] ) );

  }

  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  public ExcelWriterStepMeta getMeta() {
    return meta;
  }

  private enum Entry {

    FIELDS( ValueMetaInterface.TYPE_NONE,
      BaseMessages.getString( PKG, "ExcelWriterMetaInjection.AllFields" ) ),
    FIELD( ValueMetaInterface.TYPE_NONE,
      BaseMessages.getString( PKG, "ExcelWriterMetaInjection.AllFields" ) ),

    NAME( ValueMetaInterface.TYPE_STRING,
      BaseMessages.getString( PKG, "ExcelWriterMetaInjection.FieldName" ) ),
    TYPE( ValueMetaInterface.TYPE_STRING,
      BaseMessages.getString( PKG, "ExcelWriterMetaInjection.FieldType" ) ),
    FORMAT( ValueMetaInterface.TYPE_STRING,
      BaseMessages.getString( PKG, "ExcelWriterDialog.FormatColumn.Column" ) ),
    STYLECELL( ValueMetaInterface.TYPE_STRING,
      BaseMessages.getString( PKG, "ExcelWriterDialog.UseStyleCell.Column" ) ),
    FIELDTITLE( ValueMetaInterface.TYPE_STRING,
      BaseMessages.getString( PKG, "ExcelWriterDialog.TitleColumn.Column" ) ),
    TITLESTYLE( ValueMetaInterface.TYPE_STRING,
      BaseMessages.getString( PKG, "ExcelWriterDialog.UseTitleStyleCell.Column" ) ),
    FORMULA( ValueMetaInterface.TYPE_BOOLEAN,
      BaseMessages.getString( PKG, "ExcelWriterDialog.FormulaField.Column" ) ),
    HYPERLINKFIELD( ValueMetaInterface.TYPE_STRING,
      BaseMessages.getString( PKG, "ExcelWriterDialog.HyperLinkField.Column" ) ),
    CELLCOMMENT( ValueMetaInterface.TYPE_STRING,
      BaseMessages.getString( PKG, "ExcelWriterDialog.CommentField.Column" ) ),
    COMMENTAUTHOR( ValueMetaInterface.TYPE_STRING,
      BaseMessages.getString( PKG, "ExcelWriterDialog.CommentAuthor.Column" ) );


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

}
