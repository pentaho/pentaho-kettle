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

package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.di.core.namedcluster.model.NamedCluster;
import org.pentaho.di.repository.Repository;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UINamedCluster extends XulEventSourceAdapter {

  protected NamedCluster namedCluster;
  // inheriting classes may need access to the repository
  protected Repository rep;

  public UINamedCluster() {
    super();
  }

  public UINamedCluster( NamedCluster namedCluster, Repository rep ) {
    super();
    this.namedCluster = namedCluster;
    this.rep = rep;
  }

  public String getName() {
    if ( namedCluster != null ) {
      return namedCluster.getName();
    }
    return null;
  }

  public String getDisplayName() {
    return getName();
  }

  public String getType() {
    return NamedCluster.class.getSimpleName();
  }

  public String getDateModified() {
    return SimpleDateFormat.getDateTimeInstance().format( new Date( namedCluster.getLastModifiedDate() ) );
  }

  public NamedCluster getNamedCluster() {
    return namedCluster;
  }

}
