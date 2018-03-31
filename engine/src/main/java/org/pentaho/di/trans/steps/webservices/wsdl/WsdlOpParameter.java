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

package org.pentaho.di.trans.steps.webservices.wsdl;

import java.util.Objects;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * WSDL operation parameter abstraction.
 */
public final class WsdlOpParameter extends WsdlOpReturnType implements java.io.Serializable {

  private static final long serialVersionUID = 1L;

  public static class ParameterMode {
    private String mode;
    public static final ParameterMode IN = new ParameterMode( "IN" );
    public static final ParameterMode OUT = new ParameterMode( "OUT" );
    public static final ParameterMode INOUT = new ParameterMode( "INOUT" );
    public static final ParameterMode UNDEFINED = new ParameterMode( "UNDEFINED" );

    private ParameterMode( String mode ) {
      this.mode = mode;
    }

    public String toString() {
      return mode;
    }
  }

  private QName _name;
  private ParameterMode _mode;
  private boolean _isHeader;
  private boolean _elementFormQualified;

  /**
   * Create operation parameters whose types do not need to be unwrapped. Typically used for RPC style parameters but
   * also may be used for DOCUMENT style when parameter style is BARE.
   *
   * @param name
   *          QName of the parameter.
   * @param xmlType
   *          XML type of the parameter.
   * @param schemaTypeElement
   *          The type element from the schema for this parameter, will be null if xmlType is a built-in schema type.
   * @param wsdlTypes
   *          Wsdl types abstraction.
   */
  WsdlOpParameter( String name, QName xmlType, Element schemaTypeElement, WsdlTypes wsdlTypes ) {

    setName( name, wsdlTypes );
    _xmlType = xmlType;
    _itemXmlType = getArrayItemType( schemaTypeElement, wsdlTypes );
    _isArray = _itemXmlType != null;
    _isHeader = false;
    _mode = ParameterMode.UNDEFINED;
  }

  /**
   * Create a new WsdlOpParameter for a simple schema type. For pararmeters of simple type, the name of the parameter
   * corresponds to the name of the message part which defines it.
   *
   * @param name
   *          Name of the attribute, if a namespace ref is included it will be resolved.
   * @param e
   *          The schema element which defines the XML type of this attribute.
   * @param wsdlTypes
   *          Wsdl types abstraction.
   */
  WsdlOpParameter( String name, Element e, WsdlTypes wsdlTypes ) {

    this( e, wsdlTypes );
    setName( name, wsdlTypes );
  }

  /**
   * Create a new WsdlOpParameter for a complex type.
   *
   * @param e
   *          The schema element which defines the XML type of this attribute.
   * @param wsdlTypes
   *          Wsdl types abstraction.
   */
  WsdlOpParameter( Element e, WsdlTypes wsdlTypes ) {

    _mode = ParameterMode.UNDEFINED;
    _isArray = isArray( e );
    _isHeader = false;

    if ( e.hasAttribute( WsdlUtils.NAME_ATTR ) && e.hasAttribute( WsdlUtils.ELEMENT_TYPE_ATTR ) ) {
      setName( e.getAttribute( WsdlUtils.NAME_ATTR ), wsdlTypes );
      _xmlType = wsdlTypes.getTypeQName( e.getAttribute( WsdlUtils.ELEMENT_TYPE_ATTR ) );
    } else if ( e.hasAttribute( WsdlUtils.ELEMENT_REF_ATTR ) ) {
      _xmlType = wsdlTypes.getTypeQName( e.getAttribute( WsdlUtils.ELEMENT_REF_ATTR ) );
      _name = new QName( "", _xmlType.getLocalPart() );
    } else if ( e.hasAttribute( WsdlUtils.NAME_ATTR ) ) {
      setName( e.getAttribute( WsdlUtils.NAME_ATTR ), wsdlTypes );
      _xmlType = getElementType( e, wsdlTypes );
    } else {
      throw new RuntimeException( "invalid element: " + e.getNodeName() );
    }

    // check to see if the xml type of this element is an array type
    Element t = wsdlTypes.findNamedType( _xmlType );
    if ( t != null && WsdlUtils.COMPLEX_TYPE_NAME.equals( t.getLocalName() ) ) {
      _itemXmlType = getArrayItemType( t, wsdlTypes );
      _isArray = _itemXmlType != null;
      if ( _itemXmlType != null ) {
        _itemComplexType = wsdlTypes.getNamedComplexTypes().getComplexType( _itemXmlType.getLocalPart() );
      }
    }
  }

  /**
   * Get the name of this parameter.
   *
   * @return QName.
   */
  public QName getName() {
    return _name;
  }

  /**
   * Get the mode of this parameter.
   *
   * @return ParameterMode
   */
  public ParameterMode getMode() {
    return _mode;
  }

  /**
   * Is this paramter's name element form qualified?
   *
   * @return True if element form qualified.
   */
  public boolean isNameElementFormQualified() {
    return _elementFormQualified;
  }

  /**
   * Is this parameter a SOAP header parameter?
   *
   * @return true if it is.
   */
  public boolean isHeader() {
    return _isHeader;
  }

  /**
   * Mark this parameter as a SOAP header parameter.
   */
  protected void setHeader() {
    _isHeader = true;
  }

  /**
   * Set the mode of this parameter (IN/OUT/INOUT).
   *
   * @param mode
   *          the mode to set.
   */
  protected void setMode( ParameterMode mode ) {
    _mode = mode;
  }

  /**
   * Set the name of this parameter.
   *
   * @param name
   *          parameter name.
   * @param wsdlTypes
   *          Wsdl types abstraction.
   */
  protected void setName( String name, WsdlTypes wsdlTypes ) {
    _name = wsdlTypes.getTypeQName( name );
    _elementFormQualified = wsdlTypes.isElementFormQualified( _name.getNamespaceURI() );
  }

  /**
   * Does this element represent an array type?
   *
   * @param e
   *          Element to check.
   * @return true if this element represents an array type.
   */
  private boolean isArray( Element e ) {

    if ( e.hasAttribute( WsdlUtils.MAXOCCURS_ATTR ) && !"1".equals( e.getAttribute( WsdlUtils.MAXOCCURS_ATTR ) ) ) {
      return true;
    }

    if ( e.hasAttribute( WsdlUtils.MINOCCURS_ATTR ) ) {
      String minOccurs = e.getAttribute( WsdlUtils.MINOCCURS_ATTR );
      try {
        int i = Integer.parseInt( minOccurs );
        if ( i > 1 ) {
          return true;
        }
      } catch ( NumberFormatException nfe ) {
        // don't fail - just means minOccurs isn't set
      }
    }
    return false;
  }

  /**
   * Get the xml type of an element.
   *
   * @param element
   *          Element to determine the xml type of.
   * @param wsdlTypes
   *          Wsdl types abstraction.
   * @return QName of the element's xml type, null if type cannot be determined.
   */
  private QName getElementType( Element element, WsdlTypes wsdlTypes ) {

    /*
     * Get the type of an element when the element is in the form of: <element name="foo"> <complexType> <sequence>
     * <element name="bar" ref="s:schema"/> </sequence> </complexType> </element>
     *
     * or
     *
     * <element name="foo"> <complexType> <sequence> <any/> </sequence> </complexType> </element>
     *
     * This code is extremely brittle, this is a construct used by dot net when DataTypes are employed. The code will
     * need to be enhanced if other samples of this construct arise.
     */
    Element child;
    if ( ( child = DomUtils.getChildElementByName( element, WsdlUtils.COMPLEX_TYPE_NAME ) ) != null ) {
      if ( ( child = DomUtils.getChildElementByName( child, WsdlUtils.SEQUENCE_TAG_NAME ) ) != null ) {
        Element childElement = DomUtils.getChildElementByName( child, WsdlUtils.ELEMENT_NAME );
        if ( childElement != null ) {
          if ( child.hasAttribute( WsdlUtils.ELEMENT_REF_ATTR ) ) {
            return wsdlTypes.getTypeQName( child.getAttribute( WsdlUtils.ELEMENT_REF_ATTR ) );
          } else if ( child.hasAttribute( WsdlUtils.ELEMENT_TYPE_ATTR ) ) {
            return wsdlTypes.getTypeQName( child.getAttribute( WsdlUtils.ELEMENT_TYPE_ATTR ) );
          }
        } else if ( ( childElement = DomUtils.getChildElementByName( child, WsdlUtils.ANY_TAG_NAME ) ) != null ) {
          return new QName( childElement.getNamespaceURI(), childElement.getLocalName() );
        }
      }
    } else {
      // no children / no type map to 'any'
      return new QName( "http://www.w3.org/2001/XMLSchema", "any" );
    }
    return new QName( "http://www.w3.org/2001/XMLSchema", "String" );
  }

  /**
   * This method differs from the isArray(e) method in that it is checking a schema type to see if it is an array type.
   * In order to be an array type it must be a complex type which includes a sequence of a single element which has it's
   * minoccurs and/or maxoccures attributes set to values which denote an array.
   *
   * @param type
   *          Either a complexType or a simpleType node from the schema.
   * @return The QName of the array's item type, null if the type is not an array,
   */
  private QName getArrayItemType( Element type, WsdlTypes wsdlTypes ) {

    if ( type == null || "simpleElement".equals( type.getLocalName() ) ) {
      return null;
    }

    Element sequence = DomUtils.getChildElementByName( type, "sequence" );
    if ( sequence != null ) {
      return getArrayItemTypeFromSequence( sequence, wsdlTypes );
    }

    Element complexContent = DomUtils.getChildElementByName( type, "complexContent" );
    if ( complexContent != null ) {
      return getArrayItemTypeFromComplexContent( complexContent, wsdlTypes );
    }

    return null;
  }

  /**
   * Get an array items xml type from a sequence element.
   *
   * @param sequenceElement
   *          Sequence element.
   * @param wsdlTypes
   *          Wsdl types abstraction.
   * @return QName QName of the array item xml type.
   */
  private QName getArrayItemTypeFromSequence( Element sequenceElement, WsdlTypes wsdlTypes ) {

    Element element = DomUtils.getChildElementByName( sequenceElement, "element" );
    if ( element == null ) {
      return null;
    }
    if ( !isArray( element ) ) {
      return null;
    }
    return wsdlTypes.getTypeQName( element.getAttribute( "type" ) );
  }

  /**
   * Get an array items xml type from a complexContent element.
   *
   * @param ccElement
   *          Complex content element.
   * @param wsdlTypes
   *          Wsdl types abstraction.
   * @return QName QName of the array item xml type.
   */
  private QName getArrayItemTypeFromComplexContent( Element ccElement, WsdlTypes wsdlTypes ) {

    Element restriction = DomUtils.getChildElementByName( ccElement, "restriction" );
    if ( restriction == null ) {
      return null;
    }

    String base = restriction.getAttribute( "base" );
    if ( !"soapenc:Array".equals( base ) ) {
      return null;
    }

    Element attribute = DomUtils.getChildElementByName( restriction, "attribute" );
    if ( attribute == null ) {
      return null;
    }

    String arrayType = attribute.getAttribute( "wsdl:arrayType" );
    if ( arrayType == null ) {
      return null;
    }
    return wsdlTypes.getTypeQName( arrayType );
  }

  /**
   * Override the equals method.
   *
   * @param o
   *          Object to compare to.
   * @return true if equal
   */
  public boolean equals( Object o ) {
    if ( o instanceof WsdlOpParameter ) {
      return _name.equals( ( (WsdlOpParameter) o ).getName() );
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode( _name );
  }
}
