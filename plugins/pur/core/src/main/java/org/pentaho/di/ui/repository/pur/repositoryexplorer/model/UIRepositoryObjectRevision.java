/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.di.ui.repository.pur.repositoryexplorer.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIRepositoryObjectRevision extends XulEventSourceAdapter implements java.io.Serializable {

  private static final long serialVersionUID = 302159542242909806L; /* EESOURCE: UPDATE SERIALVERUID */

  protected ObjectRevision obj;

  public UIRepositoryObjectRevision() {
    super();
  }

  public UIRepositoryObjectRevision( ObjectRevision obj ) {
    super();
    this.obj = obj;
  }

  public String getName() {
    return obj.getName();
  }

  public String getComment() {
    return obj.getComment();
  }

  public Date getCreationDate() {
    return obj.getCreationDate();
  }

  public String getFormatCreationDate() {
    Date date = getCreationDate();
    String str = null;
    if ( date != null ) {
      SimpleDateFormat sdf = new SimpleDateFormat( "d MMM yyyy HH:mm:ss z" );
      str = sdf.format( date );
    }
    return str;
  }

  public String getLogin() {
    return obj.getLogin();
  }

}
