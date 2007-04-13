/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Header:$
 */

package be.ibridge.kettle.trans.step.webservices.wsdl;

import org.w3c.dom.Element;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * WsdlTypes provides utilities for getting information about the &lt;types&gt; section of the WSDL.
 */
public final class WsdlTypes implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String _targetNamespace;
    private final Types _types;
    private HashSet _elementFormQualifiedNamespaces;
    private Map/*<String, String>*/ _prefixMappings;
    private WsdlComplexTypes _namedComplexTypes;

    /**
     * Create a new for WsdlTypes instance for the specified WSDL definition.
     *
     * @param wsdlDefinition The WSDL definition.
     */
    protected WsdlTypes(Definition wsdlDefinition) {

        _types = wsdlDefinition.getTypes();
        _targetNamespace = wsdlDefinition.getTargetNamespace();
        _prefixMappings = wsdlDefinition.getNamespaces();
        _elementFormQualifiedNamespaces = new HashSet(getElementFormQualifiedNamespaces());
        _namedComplexTypes = new WsdlComplexTypes(this);
    }

    /**
     * Find a named &lt;element&gt; in the types section of the WSDL.
     *
     * @param elementName Name of element to find.
     * @return The element node.
     * @throws IllegalArgumentException if element cannot be found in the schema.
     */
    protected Element findNamedElement(QName elementName) {

        Schema s = getSchema(elementName.getNamespaceURI());
        Element schemaRoot = s.getElement();
        List elements = DomUtils.getChildElementsByName(schemaRoot, WsdlUtils.ELEMENT_NAME);

        Element namedElement = null;
        for (Iterator itr = elements.iterator(); itr.hasNext();) {
            Element e = (Element) itr.next();
            String schemaElementName = e.getAttribute(WsdlUtils.NAME_ATTR);
            if (elementName.getLocalPart().equals(schemaElementName)) {
                namedElement = e;
                break;
            }
        }

        if (namedElement == null) {
            throw new IllegalArgumentException("Could not find element in schema!");
        }
        return namedElement;
    }

    /**
     * Find a named &lt;complexType&gt; or &lt;simpleType&gt; in the types section of the WSDL.
     *
     * @param typeName Name of the type to find.
     * @return null if type not found.
     */
    protected Element findNamedType(QName typeName) {

        Schema s = getSchema(typeName.getNamespaceURI());
        if (s == null) {
            return null;
        }

        Element schemaRoot = s.getElement();
        // get all simple and complex types defined at the top-level.
        List types = DomUtils.getChildElementsByName(schemaRoot, WsdlUtils.COMPLEX_TYPE_NAME);
        types.addAll(DomUtils.getChildElementsByName(schemaRoot, WsdlUtils.SIMPLE_TYPE_NAME));

        Element namedType = null;
        for (Iterator itr = types.iterator(); itr.hasNext();) {
            Element t = (Element) itr.next();
            String schemaTypeName = t.getAttribute(WsdlUtils.NAME_ATTR);
            if (typeName.getLocalPart().equals(schemaTypeName)) {
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
     * Get the type qname for the type parameter.  Resolve namespace references if present,
     * if a namespace prefix is not found the WSDL's target namespace will be used.
     *
     * @param type Name of type.
     * @return A QName for the type name.
     */
    protected QName getTypeQName(String type) {

        if (type.indexOf(':') > -1) {
            String prefix = type.substring(0, type.indexOf(':'));
            type = type.substring(type.indexOf(':') + 1);
            return new QName((String) _prefixMappings.get(prefix), type);
        }
        else {
            return new QName(_targetNamespace, type);
        }
    }

    /**
     * Return a list of of all schemas defined by the WSDL definition.
     *
     * @return A list of  javax.wsdl.extension.schema.Schema elements.
     */
    protected List getSchemas() {
        if (_types == null) {
            return Collections.EMPTY_LIST;
        }
        return WsdlUtils.findExtensibilityElements(_types, WsdlUtils.SCHEMA_ELEMENT_NAME);
    }

    /**
     * Determine if the namespace URI is element form qualifed.
     *
     * @param namespaceURI Namespace URI string.
     * @return true If element form is qualified.
     */
    protected boolean isElementFormQualified(String namespaceURI) {
        return _elementFormQualifiedNamespaces.contains(namespaceURI);
    }

    /**
     * Build a list of schema target namespaces which are element form qualified.
     *
     * @return All target namespaces for schemas defined in the WSDL which are element form qualified.
     */
    private List getElementFormQualifiedNamespaces() {

        List namespaces = new ArrayList();
        List schemas = getSchemas();
        for (Iterator itr = schemas.iterator(); itr.hasNext();) {
            Element schemaElement = ((Schema) itr.next()).getElement();

            if (schemaElement.hasAttribute(WsdlUtils.ELEMENT_FORM_DEFAULT_ATTR)) {
                String v = schemaElement.getAttribute(WsdlUtils.ELEMENT_FORM_DEFAULT_ATTR);
                if (WsdlUtils.ELEMENT_FORM_QUALIFIED.equalsIgnoreCase(v)) {
                    namespaces.add(schemaElement.getAttribute(WsdlUtils.TARGET_NAMESPACE_ATTR));
                }
            }
        }
        return namespaces;
    }

    /**
     * Get the schema with the specified target namespace.
     *
     * @param targetNamespace target namespace of the schema to get.
     * @return null if not found.
     */
    private Schema getSchema(String targetNamespace) {

        if (_types == null) {
            return null;
        }

        List schemas =
                WsdlUtils.findExtensibilityElements(_types, "schema");

        for (Iterator itr = schemas.iterator(); itr.hasNext();) {
            ExtensibilityElement e = (ExtensibilityElement) itr.next();
            Element schemaRoot = ((Schema) e).getElement();
            String tns = schemaRoot.getAttribute("targetNamespace");
            if (targetNamespace.equals(tns)) {
                return (Schema) e;
            }
        }
        return null;
    }
}
