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

package org.pentaho.di.trans.steps.rssinput;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Node;

/**
 * Describes an RssInput field
 *
 * @author Samatar Hassan
 * @since 13-10-2007
 */
public class RssInputField implements Cloneable {
  private static Class<?> PKG = RssInput.class; // for i18n purposes, needed by Translator2!!

  public static final int TYPE_TRIM_NONE = 0;
  public static final int TYPE_TRIM_LEFT = 1;
  public static final int TYPE_TRIM_RIGHT = 2;
  public static final int TYPE_TRIM_BOTH = 3;

  public static final int COLUMN_TITLE = 0;
  public static final int COLUMN_LINK = 1;
  public static final int COLUMN_DESCRIPTION_AS_TEXT = 2;
  public static final int COLUMN_DESCRIPTION_AS_HTML = 3;
  public static final int COLUMN_COMMENTS = 4;
  public static final int COLUMN_GUID = 5;
  public static final int COLUMN_PUB_DATE = 6;

  public static final String[] trimTypeCode = { "none", "left", "right", "both" };

  public static final String[] trimTypeDesc = {
    BaseMessages.getString( PKG, "RssInput.Field.TrimType.None" ),
    BaseMessages.getString( PKG, "RssInput.Field.TrimType.Left" ),
    BaseMessages.getString( PKG, "RssInput.Field.TrimType.Right" ),
    BaseMessages.getString( PKG, "RssInput.Field.TrimType.Both" ) };

  public static final String[] ColumnCode = {
    "title", "link", "descriptiontext", "descriptionhtml", "comments", "guid", "pubdate" };

  public static final String[] ColumnDesc = {
    BaseMessages.getString( PKG, "RssInput.Column.Title" ),
    BaseMessages.getString( PKG, "RssInput.Column.Link" ),
    BaseMessages.getString( PKG, "RssInput.Column.DescriptionAsText" ),
    BaseMessages.getString( PKG, "RssInput.Column.DescriptionAsHtml" ),
    BaseMessages.getString( PKG, "RssInput.Column.Comments" ),
    BaseMessages.getString( PKG, "RssInput.Column.Guid" ),
    BaseMessages.getString( PKG, "RssInput.Column.PubDate" ) };

  private String name;
  private int column;
  private int type;
  private int length;
  private String format;
  private int trimtype;
  private int precision;
  private String currencySymbol;
  private String decimalSymbol;
  private String groupSymbol;
  private boolean repeat;

  public RssInputField( String fieldname ) {
    this.name = fieldname;
    this.column = COLUMN_TITLE;
    this.length = -1;
    this.type = ValueMetaInterface.TYPE_STRING;
    this.format = "";
    this.trimtype = TYPE_TRIM_NONE;
    this.groupSymbol = "";
    this.decimalSymbol = "";
    this.currencySymbol = "";
    this.precision = -1;
    this.repeat = false;
  }

  public RssInputField() {
    this( "" );
  }

  public String getXML() {
    String retval = "";

    retval += "      <field>" + Const.CR;
    retval += "        " + XMLHandler.addTagValue( "name", getName() );
    retval += "        " + XMLHandler.addTagValue( "column", getColumnCode() );
    retval += "        " + XMLHandler.addTagValue( "type", getTypeDesc() );
    retval += "        " + XMLHandler.addTagValue( "format", getFormat() );
    retval += "        " + XMLHandler.addTagValue( "currency", getCurrencySymbol() );
    retval += "        " + XMLHandler.addTagValue( "decimal", getDecimalSymbol() );
    retval += "        " + XMLHandler.addTagValue( "group", getGroupSymbol() );
    retval += "        " + XMLHandler.addTagValue( "length", getLength() );
    retval += "        " + XMLHandler.addTagValue( "precision", getPrecision() );
    retval += "        " + XMLHandler.addTagValue( "trim_type", getTrimTypeCode() );
    retval += "        " + XMLHandler.addTagValue( "repeat", isRepeated() );

    retval += "      </field>" + Const.CR;

    return retval;
  }

  public RssInputField( Node fnode ) throws KettleValueException {
    setName( XMLHandler.getTagValue( fnode, "name" ) );
    setColumn( getColumnByCode( XMLHandler.getTagValue( fnode, "column" ) ) );
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

  public static final int getTrimTypeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < trimTypeCode.length; i++ ) {
      if ( trimTypeCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static final int getColumnByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < ColumnCode.length; i++ ) {
      if ( ColumnCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static final int getTrimTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < trimTypeDesc.length; i++ ) {
      if ( trimTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static final int getColumnByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < ColumnDesc.length; i++ ) {
      if ( ColumnDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static final String getColumnDesc( int i ) {
    if ( i < 0 || i >= ColumnDesc.length ) {
      return ColumnDesc[0];
    }
    return ColumnDesc[i];
  }

  public static final String getTrimTypeCode( int i ) {
    if ( i < 0 || i >= trimTypeCode.length ) {
      return trimTypeCode[0];
    }
    return trimTypeCode[i];
  }

  public static final String getColumnCode( int i ) {
    if ( i < 0 || i >= ColumnCode.length ) {
      return ColumnCode[0];
    }
    return ColumnCode[i];
  }

  public static final String getTrimTypeDesc( int i ) {
    if ( i < 0 || i >= trimTypeDesc.length ) {
      return trimTypeDesc[0];
    }
    return trimTypeDesc[i];
  }

  @Override
  public Object clone() {
    try {
      RssInputField retval = (RssInputField) super.clone();

      return retval;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  public int getLength() {
    return length;
  }

  public void setLength( int length ) {
    this.length = length;
  }

  public String getName() {
    return name;
  }

  public void setName( String fieldname ) {
    this.name = fieldname;
  }

  public int getType() {
    return type;
  }

  public String getTypeDesc() {
    return ValueMetaFactory.getValueMetaName( type );
  }

  public void setType( int type ) {
    this.type = type;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat( String format ) {
    this.format = format;
  }

  public int getTrimType() {
    return trimtype;
  }

  public int getColumn() {
    return column;
  }

  public String getTrimTypeCode() {
    return getTrimTypeCode( trimtype );
  }

  public String getColumnCode() {
    return getColumnCode( column );
  }

  public String getTrimTypeDesc() {
    return getTrimTypeDesc( trimtype );
  }

  public String getColumnDesc() {
    return getColumnDesc( column );
  }

  public void setTrimType( int trimtype ) {
    this.trimtype = trimtype;
  }

  public void setColumn( int column ) {
    this.column = column;
  }

  public String getGroupSymbol() {
    return groupSymbol;
  }

  public void setGroupSymbol( String group_symbol ) {
    this.groupSymbol = group_symbol;
  }

  public String getDecimalSymbol() {
    return decimalSymbol;
  }

  public void setDecimalSymbol( String decimal_symbol ) {
    this.decimalSymbol = decimal_symbol;
  }

  public String getCurrencySymbol() {
    return currencySymbol;
  }

  public void setCurrencySymbol( String currency_symbol ) {
    this.currencySymbol = currency_symbol;
  }

  public int getPrecision() {
    return precision;
  }

  public void setPrecision( int precision ) {
    this.precision = precision;
  }

  public boolean isRepeated() {
    return repeat;
  }

  public void setRepeated( boolean repeat ) {
    this.repeat = repeat;
  }

  public void flipRepeated() {
    repeat = !repeat;
  }

  public void guess() {
  }

}
