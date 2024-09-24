/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.avro.input;

import org.pentaho.di.trans.steps.avro.AvroSpec;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AvroInputField extends BaseFormatInputField implements IAvroInputField {

  private List<String> pathParts;
  private List<String> indexedVals = new ArrayList<>();


  private ValueMetaInterface tempValueMeta;
  private List<String> tempParts;

  int getOutputIndex() {
    return outputIndex;
  }

  void setOutputIndex( int outputIndex ) {
    this.outputIndex = outputIndex;
  }

  private int outputIndex; // the index that this field is in the output
  // row structure

  void setPathParts( List<String> pathParts ) {
    this.pathParts = pathParts;
  }

  public List<String> getPathParts() {

    return pathParts;
  }

  ValueMetaInterface getTempValueMeta() {
    return tempValueMeta;
  }

  void setTempValueMeta( ValueMetaInterface tempValueMeta ) {
    this.tempValueMeta = tempValueMeta;
  }

  List<String> getTempParts() {
    return tempParts;
  }

  void setTempParts( List<String> tempParts ) {
    this.tempParts = tempParts;
  }

  public void setIndexedVals( List<String> indexedVals ) {
    this.indexedVals = indexedVals;
  }

  private void initIndexedVals() {
    int bracketPos = formatFieldName.indexOf( '[' );
    if ( indexedVals.isEmpty() && bracketPos > -1 ) {
      int closeBracketPos = formatFieldName.indexOf( ']' );
      String values = formatFieldName.substring( bracketPos + 1, closeBracketPos );
      indexedVals = Arrays.asList( values.split( "\\s*,\\s*" ) );
    }
  }

  public List<String> getIndexedVals() {
    initIndexedVals();
    return indexedVals;
  }

  public String getIndexedValues() {
    return String.join( " , ", getIndexedVals() );
  }

  public void setIndexedValues( String indexedValues ) {
    setIndexedVals( Arrays.asList( indexedValues.split( "\\s*,\\s*" ) ) );
  }

  @Override
  public String getAvroFieldName() {
    return formatFieldName;
  }

  @Override
  public void setFormatFieldName( String formatFieldName ) {
    setAvroFieldName( formatFieldName );
  }

  @Override public void setAvroFieldName( String avroFieldName ) {
    this.formatFieldName = avroFieldName;
    initIndexedVals();
  }

  @Override
  public AvroSpec.DataType getAvroType() {
    return AvroSpec.DataType.getDataType( getFormatType() );
  }

  @Override
  public void setAvroType( AvroSpec.DataType avroType ) {
    setFormatType( avroType.getId() );
  }

  @Override
  public void setAvroType( String avroType ) {
    for ( AvroSpec.DataType tmpType : AvroSpec.DataType.values() ) {
      if ( tmpType.getName().equalsIgnoreCase( avroType ) ) {
        setFormatType( tmpType.getId() );
        break;
      }
    }
  }

  @Override
  public String getDisplayableAvroFieldName() {

    return formatFieldName;
  }

}
