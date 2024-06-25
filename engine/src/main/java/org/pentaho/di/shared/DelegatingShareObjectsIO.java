/*!
 * Copyright 2024 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.pentaho.di.shared;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DelegatingShareObjectsIO implements SharedObjectsIO {

  List<SharedObjectsIO> sharedObjectsIOList;
  public DelegatingShareObjectsIO( SharedObjectsIO... sharedObjectsIOs ) {
    this.sharedObjectsIOList = new ArrayList<>( Arrays.asList( sharedObjectsIOs ) );
  }

  @Override
  public Map<String, Node> getSharedObjects( String type ) throws KettleXMLException {
    Map<String, Node> sharedObjectNodes = new HashMap<>();
    for ( SharedObjectsIO sharedObjectIO : sharedObjectsIOList ) {
      Map<String, Node> localSharedObjectNodes = sharedObjectIO.getSharedObjects( type );
      for ( String key: localSharedObjectNodes.keySet() ) {
        // Add only the keys that was not previously added
        if ( !sharedObjectNodes.containsKey( key ) ) {
          sharedObjectNodes.put( key, localSharedObjectNodes.get( key ) );
        }
      }
    }
    return sharedObjectNodes;
  }

  @Override
  public void saveSharedObject( String type, String name, Node node ) throws IOException, KettleException {
    // read-only operation
  }

  @Override
  public Node getSharedObject( String type, String name ) throws KettleXMLException {
    Map<String, Node> sharedObjectsNodes = getSharedObjects( type );
    return sharedObjectsNodes.get( name );
  }

  @Override
  public void delete( String type, String name ) throws KettleXMLException {
    // read-only operation
  }
}
