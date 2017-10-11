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

package org.pentaho.di.core.listeners;

/**
 * This listener will be called by the parent object when its content changes.
 *
 * @author matt
 *
 */
public interface ContentChangedListener {

  /**
   * This method will be called when the parent object to which this listener is added, has been changed.
   *
   * @param parentObject
   *          The changed object.
   */
  public void contentChanged( Object parentObject );

  /**
   * This method will be called when the parent object has been declared safe (or saved, persisted, ...)
   *
   * @param parentObject
   *          The safe object.
   */
  public void contentSafe( Object parentObject );
}
