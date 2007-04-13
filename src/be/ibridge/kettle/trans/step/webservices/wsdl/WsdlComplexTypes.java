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

import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a map of all named complex types in the WSDL.
 */
public final class WsdlComplexTypes implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private HashMap _complexTypes = new HashMap();

    /**
     * Create a new instance, parse the WSDL file for named complex types.
     *
     * @param wsdlTypes Namespace resolver.
     */
    protected WsdlComplexTypes(WsdlTypes wsdlTypes) {

        List schemas = wsdlTypes.getSchemas();
        for (Iterator itr = schemas.iterator(); itr.hasNext();) {
            ExtensibilityElement schema = (ExtensibilityElement) itr.next();
            Element schemaRoot = ((Schema) schema).getElement();

            List types = DomUtils.getChildElementsByName(schemaRoot, WsdlUtils.COMPLEX_TYPE_NAME);
            for (Iterator itrElem = types.iterator(); itrElem.hasNext();) {
                Element t = (Element) itrElem.next();
                String schemaTypeName = t.getAttribute(WsdlUtils.NAME_ATTR);
                _complexTypes.put(schemaTypeName, new ComplexType(t, wsdlTypes));
            }
        }
    }

    /**
     * Get the complex type specifed by complexTypeName.
     *
     * @param complexTypeName Name of complex type.
     * @return ComplexType instance, null if complex type was not defined in the wsdl file.
     */
    public ComplexType getComplexType(String complexTypeName) {
        return (ComplexType) _complexTypes.get(complexTypeName);
    }
}
