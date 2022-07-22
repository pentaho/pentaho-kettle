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

package org.pentaho.di.trans.steps.ldapinput;

import java.util.HashSet;

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Describes an LDAP Input
 *
 * @author Samatar Hassan
 * @since 21-09-2007
 */
public class LDAPInputField implements Cloneable {
  private static Class<?> PKG = LDAPInputMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String ATTRIBUTE_OBJECT_SID = "objectSid";

  public static final int FETCH_ATTRIBUTE_AS_STRING = 0;
  public static final int FETCH_ATTRIBUTE_AS_BINARY = 1;

  public static final String[] FetchAttributeAsCode = { "string", "binary" };

  public static final String[] FetchAttributeAsDesc = {
    BaseMessages.getString( PKG, "LDAPInputField.FetchAttributeAs.String" ),
    BaseMessages.getString( PKG, "LDAPInputField.FetchAttributeAs.Binary" ) };

  public static final int TYPE_TRIM_NONE = 0;
  public static final int TYPE_TRIM_LEFT = 1;
  public static final int TYPE_TRIM_RIGHT = 2;
  public static final int TYPE_TRIM_BOTH = 3;

  public static final String[] trimTypeCode = { "none", "left", "right", "both" };

  public static final String[] trimTypeDesc = {
    BaseMessages.getString( PKG, "LDAPInputField.TrimType.None" ),
    BaseMessages.getString( PKG, "LDAPInputField.TrimType.Left" ),
    BaseMessages.getString( PKG, "LDAPInputField.TrimType.Right" ),
    BaseMessages.getString( PKG, "LDAPInputField.TrimType.Both" ) };

  private String name;
  private String attribute;

  private int fetchAttributeAs;
  private int type;
  private int length;
  private String format;
  private int trimtype;
  private int precision;
  private String currencySymbol;
  private String decimalSymbol;
  private String groupSymbol;
  private boolean repeat;
  private String realAttribute;
  private boolean objectSid;
  private boolean sortedKey;

  private String[] samples;

  public static HashSet<String> binaryAttributes;

  public LDAPInputField( String fieldname ) {
    this.name = fieldname;
    this.attribute = "";
    this.length = -1;
    this.fetchAttributeAs = FETCH_ATTRIBUTE_AS_STRING;
    this.type = ValueMetaInterface.TYPE_STRING;
    this.format = "";
    this.trimtype = TYPE_TRIM_NONE;
    this.groupSymbol = "";
    this.decimalSymbol = "";
    this.currencySymbol = "";
    this.precision = -1;
    this.repeat = false;
    this.realAttribute = "";
    this.sortedKey = false;
  }

  public LDAPInputField() {
    this( null );
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

  public static final int getFetchAttributeAsByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < FetchAttributeAsCode.length; i++ ) {
      if ( FetchAttributeAsCode[i].equalsIgnoreCase( tt ) ) {
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

  public static final int getFetchAttributeAsByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < FetchAttributeAsDesc.length; i++ ) {
      if ( FetchAttributeAsDesc[i].equalsIgnoreCase( tt ) ) {
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

  public static final String getFetchAttributeAsCode( int i ) {
    if ( i < 0 || i >= FetchAttributeAsCode.length ) {
      return FetchAttributeAsCode[0];
    }
    return FetchAttributeAsCode[i];
  }

  public static final String getTrimTypeDesc( int i ) {
    if ( i < 0 || i >= trimTypeDesc.length ) {
      return trimTypeDesc[0];
    }
    return trimTypeDesc[i];
  }

  public static final String getFetchAttributeAsDesc( int i ) {
    if ( i < 0 || i >= FetchAttributeAsDesc.length ) {
      return FetchAttributeAsDesc[0];
    }
    return FetchAttributeAsDesc[i];
  }

  @Override
  public Object clone() {
    try {
      LDAPInputField retval = (LDAPInputField) super.clone();

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

  public String getAttribute() {
    return attribute;
  }

  public void setAttribute( String fieldattribute ) {
    this.attribute = fieldattribute;
  }

  public void setName( String fieldname ) {
    this.name = fieldname;
  }

  public int getType() {
    return type;
  }

  public int getReturnType() {
    return fetchAttributeAs;
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

  public void setSamples( String[] samples ) {
    this.samples = samples;
  }

  public String[] getSamples() {
    return samples;
  }

  public int getTrimType() {
    return trimtype;
  }

  public String getTrimTypeCode() {
    return getTrimTypeCode( trimtype );
  }

  public String getFetchAttributeAsCode() {
    return getFetchAttributeAsCode( fetchAttributeAs );
  }

  public String getTrimTypeDesc() {
    return getTrimTypeDesc( trimtype );
  }

  public String getFetchAttributeAsDesc() {
    return getFetchAttributeAsDesc( fetchAttributeAs );
  }

  public void setTrimType( int trimtype ) {
    this.trimtype = trimtype;
  }

  public boolean isSortedKey() {
    return sortedKey;
  }

  public void setSortedKey( boolean value ) {
    this.sortedKey = value;
  }

  public void setFetchAttributeAs( int fetchAttributeAs ) {
    this.fetchAttributeAs = fetchAttributeAs;
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

  public String getRealAttribute() {
    return this.realAttribute;
  }

  public void setRealAttribute( String realfieldattribute ) {
    this.realAttribute = realfieldattribute;
    if ( !Utils.isEmpty( realfieldattribute ) ) {
      if ( realfieldattribute.equals( ATTRIBUTE_OBJECT_SID ) ) {
        this.objectSid = true;
      }
    }
  }

  public boolean isObjectSid() {
    return this.objectSid;
  }

  static {
    binaryAttributes = new HashSet<String>();
    binaryAttributes.add( "photo" );
    binaryAttributes.add( "personalSignature" );
    binaryAttributes.add( "audio" );
    binaryAttributes.add( "jpegPhoto" );
    binaryAttributes.add( "javaSerializedData" );
    binaryAttributes.add( "thumbnailPhoto" );
    binaryAttributes.add( "thumbnailLogo" );
    binaryAttributes.add( "userPassword" );
    binaryAttributes.add( "userCertificate" );
    binaryAttributes.add( "cACertificate" );
    binaryAttributes.add( "authorityRevocationList" );
    binaryAttributes.add( "certificateRevocationList" );
    binaryAttributes.add( "crossCertificatePair" );
    binaryAttributes.add( "x500UniqueIdentifier" );
    binaryAttributes.add( "objectSid" );
    binaryAttributes.add( "objectGUID" );
    binaryAttributes.add( "GUID" );
  }

}
