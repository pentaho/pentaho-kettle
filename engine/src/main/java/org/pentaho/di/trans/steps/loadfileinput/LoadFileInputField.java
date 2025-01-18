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

  private String name;
  private int type;
  private int length;
  private String format;
  private int trimtype;
  private int elementtype;
  private int precision;
  private String currencySymbol;
  private String decimalSymbol;
  private String groupSymbol;
  private boolean repeat;

  public LoadFileInputField( String fieldname ) {
    this.name = fieldname;
    this.elementtype = ELEMENT_TYPE_FILECONTENT;
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

  public LoadFileInputField() {
    this( "" );
  }

  public String getXML() {
    String retval = "";

    retval += "      <field>" + Const.CR;
    retval += "        " + XMLHandler.addTagValue( "name", getName() );
    retval += "        " + XMLHandler.addTagValue( "element_type", getElementTypeCode() );
    retval += "        " + XMLHandler.addTagValue( "type", getTypeDesc() );
    retval += "        " + XMLHandler.addTagValue( "format", getFormat() );
    retval += "        " + XMLHandler.addTagValue( "currency", getCurrencySymbol() );
    retval += "        " + XMLHandler.addTagValue( "decimal", getDecimalSymbol() );
    retval += "        " + XMLHandler.addTagValue( "group", getGroupSymbol() );
    retval += "        " + XMLHandler.addTagValue( "length", getLength() );
    retval += "        " + XMLHandler.addTagValue( "precision", getPrecision() );
    retval += "        " + XMLHandler.addTagValue( "trim_type", getTrimTypeCode() );
    retval += "        " + XMLHandler.addTagValue( "repeat", isRepeated() );

    retval += "        </field>" + Const.CR;

    return retval;
  }

  public LoadFileInputField( Node fnode ) throws KettleValueException {
    setName( XMLHandler.getTagValue( fnode, "name" ) );
    setElementType( getElementTypeByCode( XMLHandler.getTagValue( fnode, "element_type" ) ) );
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
    return trimtype;
  }

  public int getElementType() {
    return elementtype;
  }

  public String getTrimTypeCode() {
    return getTrimTypeCode( trimtype );
  }

  public String getElementTypeCode() {
    return getElementTypeCode( elementtype );
  }

  public String getTrimTypeDesc() {
    return getTrimTypeDesc( trimtype );
  }

  public String getElementTypeDesc() {
    return getElementTypeDesc( elementtype );
  }

  public void setTrimType( int trimtype ) {
    this.trimtype = trimtype;
  }

  public void setElementType( int element_type ) {
    this.elementtype = element_type;
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
    String enc = "";

    return enc;
  }

  public void guess() {
  }
}
