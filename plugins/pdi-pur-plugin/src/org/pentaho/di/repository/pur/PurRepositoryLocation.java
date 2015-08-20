/*!
* Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
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

package org.pentaho.di.repository.pur;

public class PurRepositoryLocation implements java.io.Serializable {

  private static final long serialVersionUID = 2380968812271105007L; /* EESOURCE: UPDATE SERIALVERUID */
  private String url;

  public PurRepositoryLocation( String url ) {
    this.url = url;
  }

  /**
   * 
   * @return URL or <b>null</b>
   */
  public String getUrl() {
    return url;
  }

  public void setUrl( String url ) {
    this.url = url;
  }
}
