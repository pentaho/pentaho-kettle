/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.getfilesrowscount;

public class GetFilesRowsCountMetaMock extends GetFilesRowsCountMeta {
  /*
   * This is required because of how the method fileRequired works. Unfortunately,
   * that method doesn't simply accept the setter. Rather, it's expecting the
   * receiving array to have already been allocated (which it won't have been
   * for the load/save tester). This subclass simply calls allocate which
   * avoids the NPE. 
   */
  public GetFilesRowsCountMetaMock( ) {
    super();
    this.allocate( 5 );
  }
}
