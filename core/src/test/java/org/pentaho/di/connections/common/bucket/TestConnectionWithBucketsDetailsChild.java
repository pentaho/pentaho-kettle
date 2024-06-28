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

package org.pentaho.di.connections.common.bucket;

import org.pentaho.metastore.persist.MetaStoreAttribute;

/**
 * Child class to validate encrypted fields in Parent Classes getting encrypted without failure
 */
public class TestConnectionWithBucketsDetailsChild extends TestConnectionWithBucketsDetails {

  @MetaStoreAttribute
  private String password3;

  public void setPassword3( String password3 ) {
    this.password3 = password3;
  }

  public String getPassword3() {
    return password3;
  }
}
