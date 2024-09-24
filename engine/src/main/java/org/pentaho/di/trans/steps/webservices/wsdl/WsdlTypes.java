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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.namespace.QName;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Element;

/**
 * WsdlTypes provides utilities for getting information about the &lt;types&gt; section of the WSDL.
 */
public final class WsdlTypes implements Serializable {

  private static Class<?> PKG = WsdlTypes.class; // for i18n purposes, needed by Translator2!!

  private static final long serialVersionUID = 1L;
  private final String _targetNamespace;
  private final Types _types;
  private HashSet<String> _elementFormQualifiedNamespaces;
  private Map<String, String> _prefixMappings;
  private WsdlComplexTypes _namedComplexTypes;

  /**
   * Create a new for WsdlTypes instance for the specified WSDL definition.
   *
   * @param wsdlDefinition
   *          The WSDL definition.
   */
  @SuppressWarnings( "unchecked" )
  protected WsdlTypes( Definition wsdlDefinition ) {

    _types = wsdlDefinition.getTypes();
    _targetNamespace = wsdlDefinition.getTargetNamespace();
    _prefixMappings = wsdlDefinition.getNamespaces();
    _elementFormQualifiedNamespaces = new HashSet<String>( getElementFormQualifiedNamespaces() );
    _namedComplexTypes = new WsdlComplexTypes( this );
  }

  /**
   * Find a named &lt;element&gt; in the types section of the WSDL.
   *
   * @param elementName
   *          Name of element to find.
   * @return The element node.
   * @throws KettleStepException
   *           If schema or element in schema can't be found for the given element name
   */
  protected Element findNamedElement( QName elementName ) throws KettleStepException {

    Element namedElement = null;
    Schema s = getSchema( elementName.getNamespaceURI() );
    if ( s == null ) {
      throw new KettleStepException( BaseMessages
        .getString( PKG, "Wsdl.Error.MissingSchemaException", elementName ) );
    }

    Element schemaRoot = s.getElement();
    List<Element> elements = DomUtils.getChildElementsByName( schemaRoot, WsdlUtils.ELEMENT_NAME );

    for ( Element e : elements ) {
      String schemaElementName = e.getAttribute( WsdlUtils.NAME_ATTR );
      if ( elementName.getLocalPart().equals( schemaElementName ) ) {
        namedElement = e;
        break;
      }
    }

    if ( namedElement == null ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "Wsdl.Error.ElementMissingException", elementName ) );
    }
    return namedElement;
  }

  /**
   * Find a named &lt;complexType&gt; or &lt;simpleType&gt; in the types section of the WSDL.
   *
   * @param typeName
   *          Name of the type to find.
   * @return null if type not found.
   */
  protected Element findNamedType( QName typeName ) {

    Schema s = getSchema( typeName.getNamespaceURI() );
    if ( s == null ) {
      return null;
    }

    Element schemaRoot = s.getElement();

    // get all simple and complex types defined at the top-level.
    //
    List<Element> types = DomUtils.getChildElementsByName( schemaRoot, WsdlUtils.COMPLEX_TYPE_NAME );
    types.addAll( DomUtils.getChildElementsByName( schemaRoot, WsdlUtils.SIMPLE_TYPE_NAME ) );

    Element namedType = null;
    for ( Element t : types ) {
      String schemaTypeName = t.getAttribute( WsdlUtils.NAME_ATTR );
      if ( typeName.getLocalPart().equals( schemaTypeName ) ) {
        namedType = t;
        break;
      }
    }
    return namedType;
  }

  /**
   * Get the map of named complex types defined in the WSDL.
   *
   * @return Wsdl's named complex types.
   */
  protected WsdlComplexTypes getNamedComplexTypes() {
    return _namedComplexTypes;
  }

  /**
   * Get the target namespace of the wsdl.
   *
   * @return String contianing the target namespace.
   */
  protected String getTargetNamespace() {
    return _targetNamespace;
  }

  /**
   * Get the type qname for the type parameter. Resolve namespace references if present, if a namespace prefix is not
   * found the WSDL's target namespace will be used.
   *
   * @param type
   *          Name of type.
   * @return A QName for the type name.
   */
  protected QName getTypeQName( String type ) {

    if ( type.indexOf( ':' ) > -1 ) {
      String prefix = type.substring( 0, type.indexOf( ':' ) );
      type = type.substring( type.indexOf( ':' ) + 1 );
      return new QName( _prefixMappings.get( prefix ), type );
    } else {
      return new QName( _targetNamespace, type );
    }
  }

  /**
   * Return a list of of all schemas defined by the WSDL definition.
   *
   * @return A list of javax.wsdl.extension.schema.Schema elements.
   */
  protected List<ExtensibilityElement> getSchemas() {
    if ( _types == null ) {
      return Collections.emptyList();
    }
    return WsdlUtils.findExtensibilityElements( _types, WsdlUtils.SCHEMA_ELEMENT_NAME );
  }

  /**
   * Determine if the namespace URI is element form qualifed.
   *
   * @param namespaceURI
   *          Namespace URI string.
   * @return true If element form is qualified.
   */
  public boolean isElementFormQualified( String namespaceURI ) {
    return _elementFormQualifiedNamespaces.contains( namespaceURI );
  }

  /**
   * Build a list of schema target name spaces which are element form qualified.
   *
   * @return All target name spaces for schemas defined in the WSDL which are element form qualified.
   */
  private List<String> getElementFormQualifiedNamespaces() {

    List<String> namespaces = new ArrayList<String>();
    List<ExtensibilityElement> schemas = getSchemas();

    for ( ExtensibilityElement schema : schemas ) {
      Element schemaElement = ( (Schema) schema ).getElement();

      if ( schemaElement.hasAttribute( WsdlUtils.ELEMENT_FORM_DEFAULT_ATTR ) ) {
        String v = schemaElement.getAttribute( WsdlUtils.ELEMENT_FORM_DEFAULT_ATTR );
        if ( WsdlUtils.ELEMENT_FORM_QUALIFIED.equalsIgnoreCase( v ) ) {
          namespaces.add( schemaElement.getAttribute( WsdlUtils.TARGET_NAMESPACE_ATTR ) );
        }
      }
    }
    return namespaces;
  }

  /**
   * Get the schema with the specified target namespace.
   *
   * @param targetNamespace
   *          target namespace of the schema to get.
   * @return null if not found.
   */
  private Schema getSchema( String targetNamespace ) {

    if ( _types == null ) {
      return null;
    }

    List<ExtensibilityElement> schemas = WsdlUtils.findExtensibilityElements( _types, "schema" );

    for ( ExtensibilityElement e : schemas ) {
      Element schemaRoot = ( (Schema) e ).getElement();
      String tns = schemaRoot.getAttribute( "targetNamespace" );
      if ( targetNamespace.equals( tns ) ) {
        return (Schema) e;
      }
    }
    return null;
  }
}
