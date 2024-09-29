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

package org.pentaho.di.trans.steps.jsoninput;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.steps.file.BaseFileField;
import org.w3c.dom.Node;

/**
 * Describes a JsonPath field.
 *
 * @author Samatar
 * @since 20-06-20010
 */
public class JsonInputField extends BaseFileField implements Cloneable {

  @Deprecated
  public static final int TYPE_TRIM_NONE = ValueMetaInterface.TRIM_TYPE_NONE;
  @Deprecated
  public static final int TYPE_TRIM_LEFT = ValueMetaInterface.TRIM_TYPE_LEFT;
  @Deprecated
  public static final int TYPE_TRIM_RIGHT = ValueMetaInterface.TRIM_TYPE_RIGHT;
  @Deprecated
  public static final int TYPE_TRIM_BOTH = ValueMetaInterface.TRIM_TYPE_BOTH;

  @Deprecated
  public static final String[] trimTypeCode = ValueMetaBase.trimTypeCode;

  @Deprecated
  public static final String[] trimTypeDesc = ValueMetaBase.trimTypeDesc;

  @Injection( name = "FIELD_PATH", group = "FIELDS" )
  private String path;

  public JsonInputField( String fieldname ) {
    super();
    setName( fieldname );
  }

  public JsonInputField() {
    this( "" );
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer( 400 );

    retval.append( "      <field>" ).append( Const.CR );
    retval.append( "        " ).append( XMLHandler.addTagValue( "name", getName() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "path", getPath() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "type", getTypeDesc() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "format", getFormat() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "currency", getCurrencySymbol() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "decimal", getDecimalSymbol() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "group", getGroupSymbol() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "length", getLength() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "precision", getPrecision() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "trim_type", getTrimTypeCode() ) );
    retval.append( "        " ).append( XMLHandler.addTagValue( "repeat", isRepeated() ) );

    retval.append( "      </field>" ).append( Const.CR );

    return retval.toString();
  }

  public JsonInputField( Node fnode ) throws KettleValueException {
    setName( XMLHandler.getTagValue( fnode, "name" ) );
    setPath( XMLHandler.getTagValue( fnode, "path" ) );
    setType( ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( fnode, "type" ) ) );
    setFormat( XMLHandler.getTagValue( fnode, "format" ) );
    setCurrencySymbol( XMLHandler.getTagValue( fnode, "currency" ) );
    setDecimalSymbol( XMLHandler.getTagValue( fnode, "decimal" ) );
    setGroupSymbol( XMLHandler.getTagValue( fnode, "group" ) );
    setLength( Const.toInt( XMLHandler.getTagValue( fnode, "length" ), -1 ) );
    setPrecision( Const.toInt( XMLHandler.getTagValue( fnode, "precision" ), -1 ) );
    setTrimType( getTrimTypeByCode( XMLHandler.getTagValue( fnode, "trim_type" ) ) );
    setRepeated( !"N".equalsIgnoreCase( XMLHandler.getTagValue( fnode, "repeat" ) ) );
  }

  public ValueMetaInterface toValueMeta( String fieldOriginStepName, VariableSpace vspace ) throws KettlePluginException {
    int type = getType();
    if ( type == ValueMetaInterface.TYPE_NONE ) {
      type = ValueMetaInterface.TYPE_STRING;
    }
    ValueMetaInterface v =
        ValueMetaFactory.createValueMeta( vspace != null ? vspace.environmentSubstitute( getName() ) : getName(), type );
    v.setLength( getLength() );
    v.setPrecision( getPrecision() );
    v.setOrigin( fieldOriginStepName );
    v.setConversionMask( getFormat() );
    v.setDecimalSymbol( getDecimalSymbol() );
    v.setGroupingSymbol( getGroupSymbol() );
    v.setCurrencySymbol( getCurrencySymbol() );
    v.setTrimType( getTrimType() );
    return v;
  }

  @Deprecated
  public static final int getTrimTypeByCode( String tt ) {
    return ValueMetaBase.getTrimTypeByCode( tt );
  }

  @Deprecated
  public static final int getTrimTypeByDesc( String tt ) {
    return ValueMetaBase.getTrimTypeByDesc( tt );
  }

  @Deprecated
  public static final String getTrimTypeCode( int i ) {
    return ValueMetaBase.getTrimTypeCode( i );
  }

  @Deprecated
  public static final String getTrimTypeDesc( int i ) {
    return ValueMetaBase.getTrimTypeDesc( i );
  }

  public JsonInputField clone() {
    JsonInputField retval = (JsonInputField) super.clone();
    return retval;
  }

  public String getPath() {
    return path;
  }

  public void setPath( String value ) {
    this.path = value;
  }

}
