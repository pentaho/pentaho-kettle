/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.loadfileinput;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Node;

/**
 * Describes a field
 *
 * @author Samatar
 * @since 20-06-2007
 *
 */
public class LoadFileInputField implements Cloneable {
  private static Class<?> PKG = LoadFileInputMeta.class; // for i18n purposes, needed by Translator2!!

  public static final int TYPE_TRIM_NONE = 0;
  public static final int TYPE_TRIM_LEFT = 1;
  public static final int TYPE_TRIM_RIGHT = 2;
  public static final int TYPE_TRIM_BOTH = 3;

  public static final String[] trimTypeCode = { "none", "left", "right", "both" };

  public static final String[] trimTypeDesc = {
    BaseMessages.getString( PKG, "LoadFileInputField.TrimType.None" ),
    BaseMessages.getString( PKG, "LoadFileInputField.TrimType.Left" ),
    BaseMessages.getString( PKG, "LoadFileInputField.TrimType.Right" ),
    BaseMessages.getString( PKG, "LoadFileInputField.TrimType.Both" ) };

  public static final int ELEMENT_TYPE_FILECONTENT = 0;
  public static final int ELEMENT_TYPE_FILESIZE = 1;

  public static final String[] ElementTypeCode = { "content", "size" };

  public static final String[] ElementTypeDesc = {
    BaseMessages.getString( PKG, "LoadFileInputField.ElementType.FileContent" ),
    BaseMessages.getString( PKG, "LoadFileInputField.ElementType.FileSize" ), };

  private static final String XML_TAG_NAME = "name";
  private static final String XML_TAG_ELEMENT_TYPE = "element_type";
  private static final String XML_TAG_TYPE = "type";
  private static final String XML_TAG_FORMAT = "format";
  private static final String XML_TAG_CURRENCY = "currency";
  private static final String XML_TAG_DECIMAL = "decimal";
  private static final String XML_TAG_GROUP = "group";
  private static final String XML_TAG_LENGTH = "length";
  private static final String XML_TAG_PRECISION = "precision";
  private static final String XML_TAG_TRIM_TYPE = "trim_type";
  private static final String XML_TAG_REPEAT = "repeat";
  private static final String XML_TAG_INDENT2 = "        ";
  private static final String XML_TAG_INDENT = "      ";

  private String name;
  private int type;
  private int length;
  private String format;
  private int trimType;
  private int elementType;
  private int precision;
  private String currencySymbol;
  private String decimalSymbol;
  private String groupSymbol;
  private boolean repeat;

  public LoadFileInputField( String fieldname ) {
    this.name = fieldname;
    this.elementType = ELEMENT_TYPE_FILECONTENT;
    this.length = -1;
    this.type = ValueMetaInterface.TYPE_STRING;
    this.format = "";
    this.trimType = TYPE_TRIM_NONE;
    this.groupSymbol = "";
    this.decimalSymbol = "";
    this.currencySymbol = "";
    this.precision = -1;
    this.repeat = false;
  }

  public LoadFileInputField() {
    this( "" );
  }

  public String getXML() {
    String retval = "";

    retval += XML_TAG_INDENT + "<field>" + Const.CR;
    retval += XML_TAG_INDENT2 + XMLHandler.addTagValue( XML_TAG_NAME, getName() );
    retval += XML_TAG_INDENT2 + XMLHandler.addTagValue( XML_TAG_ELEMENT_TYPE, getElementTypeCode() );
    retval += XML_TAG_INDENT2 + XMLHandler.addTagValue( XML_TAG_TYPE, getTypeDesc() );
    retval += XML_TAG_INDENT2 + XMLHandler.addTagValue( XML_TAG_FORMAT, getFormat() );
    retval += XML_TAG_INDENT2 + XMLHandler.addTagValue( XML_TAG_CURRENCY, getCurrencySymbol() );
    retval += XML_TAG_INDENT2 + XMLHandler.addTagValue( XML_TAG_DECIMAL, getDecimalSymbol() );
    retval += XML_TAG_INDENT2 + XMLHandler.addTagValue( XML_TAG_GROUP, getGroupSymbol() );
    retval += XML_TAG_INDENT2 + XMLHandler.addTagValue( XML_TAG_LENGTH, getLength() );
    retval += XML_TAG_INDENT2 + XMLHandler.addTagValue( XML_TAG_PRECISION, getPrecision() );
    retval += XML_TAG_INDENT2 + XMLHandler.addTagValue( XML_TAG_TRIM_TYPE, getTrimTypeCode() );
    retval += XML_TAG_INDENT2 + XMLHandler.addTagValue( XML_TAG_REPEAT, isRepeated() );
    retval += XML_TAG_INDENT + "</field>" + Const.CR;

    return retval;
  }

  public LoadFileInputField( Node fnode ) throws KettleValueException {
    setName( XMLHandler.getTagValue( fnode, XML_TAG_NAME ) );
    setElementType( getElementTypeByCode( XMLHandler.getTagValue( fnode, XML_TAG_ELEMENT_TYPE ) ) );
    setType( ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( fnode, XML_TAG_TYPE ) ) );
    setFormat( XMLHandler.getTagValue( fnode, XML_TAG_FORMAT ) );
    setCurrencySymbol( XMLHandler.getTagValue( fnode, XML_TAG_CURRENCY ) );
    setDecimalSymbol( XMLHandler.getTagValue( fnode, XML_TAG_DECIMAL ) );
    setGroupSymbol( XMLHandler.getTagValue( fnode, XML_TAG_GROUP ) );
    setLength( Const.toInt( XMLHandler.getTagValue( fnode, XML_TAG_LENGTH ), -1 ) );
    setPrecision( Const.toInt( XMLHandler.getTagValue( fnode, XML_TAG_PRECISION ), -1 ) );
    setTrimType( getTrimTypeByCode( XMLHandler.getTagValue( fnode, XML_TAG_TRIM_TYPE ) ) );
    setRepeated( !"N".equalsIgnoreCase( XMLHandler.getTagValue( fnode, XML_TAG_REPEAT ) ) );
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

  public static final int getElementTypeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < ElementTypeCode.length; i++ ) {
      if ( ElementTypeCode[i].equalsIgnoreCase( tt ) ) {
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

  public static final int getElementTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < ElementTypeDesc.length; i++ ) {
      if ( ElementTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static final String getTrimTypeCode( int i ) {
    if ( i < 0 || i >= trimTypeCode.length ) {
      return trimTypeCode[0];
    }
    return trimTypeCode[i];
  }

  public static final String getElementTypeCode( int i ) {
    if ( i < 0 || i >= ElementTypeCode.length ) {
      return ElementTypeCode[0];
    }
    return ElementTypeCode[i];
  }

  public static final String getTrimTypeDesc( int i ) {
    if ( i < 0 || i >= trimTypeDesc.length ) {
      return trimTypeDesc[0];
    }
    return trimTypeDesc[i];
  }

  public static final String getElementTypeDesc( int i ) {
    if ( i < 0 || i >= ElementTypeDesc.length ) {
      return ElementTypeDesc[0];
    }
    return ElementTypeDesc[i];
  }

  @Override
  public Object clone() {
    try {
      LoadFileInputField retval = (LoadFileInputField) super.clone();

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
    return trimType;
  }

  public int getElementType() {
    return elementType;
  }

  public String getTrimTypeCode() {
    return getTrimTypeCode( trimType );
  }

  public String getElementTypeCode() {
    return getElementTypeCode( elementType );
  }

  public String getTrimTypeDesc() {
    return getTrimTypeDesc( trimType );
  }

  public String getElementTypeDesc() {
    return getElementTypeDesc( elementType );
  }

  public void setTrimType( int trimType ) {
    this.trimType = trimType;
  }

  public void setElementType( int elementType ) {
    this.elementType = elementType;
  }

  public String getGroupSymbol() {
    return groupSymbol;
  }

  public void setGroupSymbol( String groupSymbol ) {
    this.groupSymbol = groupSymbol;
  }

  public String getDecimalSymbol() {
    return decimalSymbol;
  }

  public void setDecimalSymbol( String decimalSymbol ) {
    this.decimalSymbol = decimalSymbol;
  }

  public String getCurrencySymbol() {
    return currencySymbol;
  }

  public void setCurrencySymbol( String currencySymbol ) {
    this.currencySymbol = currencySymbol;
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

  public String getFieldPositionsCode() {
    return "";
  }

  public void guess() {
  }
}
