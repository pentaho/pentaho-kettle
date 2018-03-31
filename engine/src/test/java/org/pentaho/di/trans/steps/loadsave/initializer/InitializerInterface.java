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

package org.pentaho.di.trans.steps.loadsave.initializer;

public interface InitializerInterface<T> {

  /**
   * Perform in-place modifications to the stepMeta before
   * FieldLoadSaveValidator classes are called on the stepMeta
   * 
   * @deprecated The stepMeta class should be updated so that
   * developers can instantiate the stepMeta, and immediately
   * call setter methods.  Commonly, this is used for steps
   * that define an allocate method, which pre-populate
   * empty arrays
   * 
   * @param stepMeta The stepMeta class
   */
  @Deprecated
  public void modify( T object );
}
