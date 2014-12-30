/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import org.pentaho.di.core.namedconfig.model.NamedConfiguration;
import org.pentaho.di.repository.Repository;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UINamedConfiguration extends XulEventSourceAdapter {

  protected NamedConfiguration namedConfiguration;
  // inheriting classes may need access to the repository
  protected Repository rep;

  public UINamedConfiguration() {
    super();
  }

  public UINamedConfiguration( NamedConfiguration namedConfiguration, Repository rep ) {
    super();
    this.namedConfiguration = namedConfiguration;
    this.rep = rep;
  }

  public String getName() {
    if ( namedConfiguration != null ) {
      return namedConfiguration.getName();
    }
    return null;
  }

  public String getDisplayName() {
    return getName();
  }

  public String getType() {
    if ( namedConfiguration != null ) {
      return namedConfiguration.getType();
    }
    return null;
  }

  public String getDateModified() {
    return SimpleDateFormat.getDateTimeInstance().format( new Date( namedConfiguration.getLastModifiedDate() ) );
  }

  public NamedConfiguration getNamedConfiguration() {
    return namedConfiguration;
  }

}
