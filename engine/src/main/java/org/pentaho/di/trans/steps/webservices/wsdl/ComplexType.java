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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/*
 This looks a little scary, but isn't so bad.  Pretty much all that needs to be done here is to
 parse a NAMED complex type in the wsdl's types section.  We really only care about the <element>'s
 contained within the complex type.  The semantics don't matter (choice, sequence, etc).  The end result
 should be a ComplexType object which contains only elements.  This type will be used during client
 type registration.
 */

/**
 * A ComplexType contians a map of the elementName -> elementXmlType of all the elements in a named complex type.
 */
public final class ComplexType implements java.io.Serializable {

  private static final long serialVersionUID = 1L;
  private final HashMap<String, QName> _elements = new HashMap<String, QName>();
  private final List<String> _elementNames = new ArrayList<String>();
  private final String _name;
  private WsdlTypes _wsdlTypes;

  /**
   * Create a new complex type for the specified element.
   *
   * @param type
   *          DOM element of the complex type.
   * @param wsdlTypes
   *          Namespace resolver instance.
   */
  ComplexType( Element type, WsdlTypes wsdlTypes ) {

    _name = type.getAttribute( "name" );
    _wsdlTypes = wsdlTypes;

    // annotation?, (simpleContent | complexContent
    // | ((group | all | choice | sequence)?, (attribute | attributeGroup)*, anyAttribute?))
    Element child;
    if ( ( child = DomUtils.getChildElementByName( type, "simpleContent" ) ) != null ) {
      processSimpleContent( child );
    } else if ( ( child = DomUtils.getChildElementByName( type, "complexContent" ) ) != null ) {
      processComplexContent( child );
    } else if ( ( child = DomUtils.getChildElementByName( type, "group" ) ) != null ) {
      processGroup( child );
    } else if ( ( child = DomUtils.getChildElementByName( type, "all" ) ) != null ) {
      processAll( child );
    } else if ( ( child = DomUtils.getChildElementByName( type, "choice" ) ) != null ) {
      processChoice( child );
    } else if ( ( child = DomUtils.getChildElementByName( type, "sequence" ) ) != null ) {
      processSequence( child );
    }

    // release the resolver, we don't need it after the parse is complete
    _wsdlTypes = null;
  }

  /**
   * Get the complex type name.
   *
   * @return String containing name of complex type.
   */
  public String getName() {
    return _name;
  }

  /**
   * Given the name of an element contained within the complex type, get its xml type.
   *
   * @param elementName
   *          Name of element contained within complex type.
   * @return Xmltype of the element or null if element can not be found in the complex type.
   */
  public QName getElementType( String elementName ) {
    return _elements.get( elementName.toLowerCase() );
  }

  /**
   * Get the set of all element names contained in this complex type.
   *
   * @return Set.
   */
  public List<String> getElementNames() {
    return _elementNames;
  }

  /* ---- Private Methods --- */

  /**
   * Process an 'all' element.
   *
   * @param all
   */
  private void processAll( Element all ) {
    // annotation?, element*
    List<Element> elements = DomUtils.getChildElementsByName( all, "element" );
    for ( Iterator<Element> itr = elements.iterator(); itr.hasNext(); ) {
      processElement( itr.next() );
    }
  }

  /**
   * Process an 'any' element.
   *
   * @param any
   */
  private void processAny( Element any ) {
    // annotation?
    // *** noop ***
  }

  // private void processAttribute(Element attribute) {
  // // <complexType name="ArrayOf_xsd_short">
  // // <complexContent>
  // // <restriction base="soapenc:Array">
  // // <attribute ref="soapenc:arrayType" wsdl:arrayType="xsd:short[]"/>
  // // </restriction>
  // // </complexContent>
  // // </complexType>
  // if (attribute.hasAttribute("wsdl:arrayType")) {
  // String attributeName = attribute.getAttribute("ref");
  // String arrayType = attribute.getAttribute("wsdl:arrayType");
  // _elements.put(attributeName, _wsdlTypes.getTypeQName(arrayType));
  // }
  // }

  /**
   * Process a 'choice' element.
   *
   * @param choice
   */
  private void processChoice( Element choice ) {
    // annotation?, (element | group | choice | sequence | any)*
    // just call process sequence, same elements
    processSequence( choice );
  }

  /**
   * Process a 'complexContent' element.
   *
   * @param complexContent
   */
  private void processComplexContent( Element complexContent ) {
    // annotation?, (extension | restriction)
    Element child;
    if ( ( child = DomUtils.getChildElementByName( complexContent, "extension" ) ) != null ) {
      processComplexExtension( child );
    } else if ( ( child = DomUtils.getChildElementByName( complexContent, "restriction" ) ) != null ) {
      processComplexRestriction( child );
    }
  }

  /**
   * Process an 'extension' element whose parent is 'complexContent'.
   *
   * @param complexExtension
   */
  private void processComplexExtension( Element complexExtension ) {
    // annotation?, (group, | all | choice | sequence)?, (attribute | attributeGroup)*, anyAttribute?
    Element child;
    if ( ( child = DomUtils.getChildElementByName( complexExtension, "group" ) ) != null ) {
      processGroup( child );
    } else if ( ( child = DomUtils.getChildElementByName( complexExtension, "all" ) ) != null ) {
      processAll( child );
    } else if ( ( child = DomUtils.getChildElementByName( complexExtension, "choice" ) ) != null ) {
      processChoice( child );
    } else if ( ( child = DomUtils.getChildElementByName( complexExtension, "sequence" ) ) != null ) {
      processSequence( child );
    }
  }

  /**
   * Process a 'restriction' element whose parent is 'complexContent'.
   *
   * @param complexRestriction
   */
  private void processComplexRestriction( Element complexRestriction ) {
    // annotation?, (group | all | choice | sequence)?, (attribute | attributeGroup)*, anyAttribute?
    Element child;
    if ( ( child = DomUtils.getChildElementByName( complexRestriction, "group" ) ) != null ) {
      processGroup( child );
    } else if ( ( child = DomUtils.getChildElementByName( complexRestriction, "all" ) ) != null ) {
      processAll( child );
    } else if ( ( child = DomUtils.getChildElementByName( complexRestriction, "choice" ) ) != null ) {
      processChoice( child );
    } else if ( ( child = DomUtils.getChildElementByName( complexRestriction, "sequence" ) ) != null ) {
      processSequence( child );
    }
    // else if (DomUtils.getChildElementByName(complexRestriction, "attribute") != null) {
    // List<Element> attributes = DomUtils.getChildElementsByName(complexRestriction, "attribute");
    // for (Element attribute : attributes) {
    // processAttribute(attribute);
    // }
    // }
  }

  /**
   * Process an 'element'.
   *
   * @param element
   */
  private void processElement( Element element ) {
    // annotation?
    if ( element.hasAttribute( "name" ) ) {
      String elementName = element.getAttribute( "name" );
      String elementType = element.getAttribute( "type" );
      _elements.put( elementName.toLowerCase(), _wsdlTypes.getTypeQName( elementType ) );
      _elementNames.add( elementName );
    }
  }

  /**
   * Process a 'group' element.
   *
   * @param group
   */
  private void processGroup( Element group ) {
    // annotation?, (all | choice | sequence)
    Element child;
    if ( ( child = DomUtils.getChildElementByName( group, "all" ) ) != null ) {
      processAll( child );
    } else if ( ( child = DomUtils.getChildElementByName( group, "choice" ) ) != null ) {
      processChoice( child );
    } else if ( ( child = DomUtils.getChildElementByName( group, "sequence" ) ) != null ) {
      processSequence( child );
    }
  }

  /**
   * Process a 'sequence' element.
   *
   * @param sequence
   */
  private void processSequence( Element sequence ) {
    // annotation?, (element | group | choice | sequence | any)*
    List<Element> elements = DomUtils.getChildElementsByName( sequence, "element" );
    for ( Iterator<Element> itr = elements.iterator(); itr.hasNext(); ) {
      processElement( itr.next() );
    }

    elements = DomUtils.getChildElementsByName( sequence, "group" );
    for ( Iterator<Element> itr = elements.iterator(); itr.hasNext(); ) {
      processGroup( itr.next() );
    }

    elements = DomUtils.getChildElementsByName( sequence, "choice" );
    for ( Iterator<Element> itr = elements.iterator(); itr.hasNext(); ) {
      processChoice( itr.next() );
    }

    elements = DomUtils.getChildElementsByName( sequence, "sequence" );
    for ( Iterator<Element> itr = elements.iterator(); itr.hasNext(); ) {
      processSequence( itr.next() );
    }

    elements = DomUtils.getChildElementsByName( sequence, "any" );
    for ( Iterator<Element> itr = elements.iterator(); itr.hasNext(); ) {
      processAny( itr.next() );
    }
  }

  /**
   * Process a 'simpleContent' element.
   *
   * @param simpleContent
   */
  private void processSimpleContent( Element simpleContent ) {
    // annotation?, (extension | restriction)
    Element child;
    if ( ( child = DomUtils.getChildElementByName( simpleContent, "extension" ) ) != null ) {
      processSimpleExtension( child );
    } else if ( ( child = DomUtils.getChildElementByName( simpleContent, "restriction" ) ) != null ) {
      processSimpleRestriction( child );
    }
  }

  /**
   * Process an 'extension' element whose parent is 'simpleContent'.
   *
   * @param any
   */
  private void processSimpleExtension( Element any ) {
    // annotation?, (attribute | attributeGroup)*, anyAttribute?
    // *** noop ***
  }

  /**
   * Process a 'restriction' element whose parent is 'simpleContent'.
   *
   * @param simpleRestriction
   */
  private void processSimpleRestriction( Element simpleRestriction ) {
    // annotation?, simpleType?, (enumeration | length | maxExclusive | maxInclusive
    // | maxLength | minExclusive | minInclusive | minLength | pattern | fractionDigits
    // | totalDigits | whiteSpace)*, (attribute | attributeGroup)*, anyAttribute?
    // *** noop ***
  }
}
