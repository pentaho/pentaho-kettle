/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.vfs;

import org.pentaho.metastore.persist.MetaStoreAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class to put common fields for all types of VFS Connection Details to avoid code duplication
 */
public abstract class BaseVFSConnectionDetails implements VFSConnectionDetails {


  @MetaStoreAttribute
  private List<String> baRoles = new ArrayList<>();


  @Override
  public List<String> getBaRoles() {
    return baRoles;
  }

  @Override
  public Map<String, String> getProperties() {
    Map<String, String> props = new HashMap<>();
    fillProperties( props );
    return props;
  }

  /**
   * Adds base/default properties to properties of connection instance.
   * <p>
   * @param props The properties map
   */
  protected void fillProperties( Map<String, String> props ) {
    props.put( "baRoles", getBaRoles().toString() );
  }
}
