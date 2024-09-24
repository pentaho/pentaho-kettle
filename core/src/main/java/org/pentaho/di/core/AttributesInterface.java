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

package org.pentaho.di.core;

import java.util.Map;

public interface AttributesInterface {

  /**
   * Pass a map of group attributes
   *
   * @param attributesMap
   *          The map of group attributes
   */
  public void setAttributesMap( Map<String, Map<String, String>> attributesMap );

  /**
   * @return All attributes for all attribute groups in one single map.
   */
  public Map<String, Map<String, String>> getAttributesMap();

  /**
   * Add a map of attributes to an attribute group.
   *
   * @param groupName
   *          The group to add the attributes to
   * @param attributes
   *          the attributes to add
   */
  public void setAttributes( String groupName, Map<String, String> attributes );

  /**
   * Add an attribute to an attribute group
   *
   * @param groupName
   *          The group to add the attribute to
   * @param key
   *          The key of the attribute
   * @param value
   *          the value of the attribute
   */
  public void setAttribute( String groupName, String key, String value );

  /**
   * Get an attributes map for a group
   *
   * @param groupName
   *          The name of the attributes group to retrieve
   * @return The map representing the attributes group
   */
  public Map<String, String> getAttributes( String groupName );

  /**
   * Get a value for an attribute in an attribute group
   *
   * @param groupName
   *          The group to get the attribute from
   * @param key
   *          The attribute key
   * @return The attribute value of null if the group of key doesn't exist
   */
  public String getAttribute( String groupName, String key );

}
