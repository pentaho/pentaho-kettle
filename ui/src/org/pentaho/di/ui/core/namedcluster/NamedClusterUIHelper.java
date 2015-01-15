/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.core.namedcluster;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.namedcluster.NamedClusterManager;
import org.pentaho.di.core.namedcluster.model.NamedCluster;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

public class NamedClusterUIHelper {

  public static List<NamedCluster> getNamedClusters() {
    try {
      return NamedClusterManager.getInstance().list( Spoon.getInstance().getMetaStore() );
    } catch ( MetaStoreException e ) {
      return new ArrayList<NamedCluster>();
    }
  }  
  
  public static NamedCluster getNamedCluster( String namedCluster ) throws MetaStoreException {
    return NamedClusterManager.getInstance().read( namedCluster, Spoon.getInstance().getMetaStore() );
  }    
  
}
