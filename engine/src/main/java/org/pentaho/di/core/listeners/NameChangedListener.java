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
 * A listener that will signal when the name of an object changes.
 *
 * @author Matt Casters (mcasters@pentaho.org)
 *
 */
public interface NameChangedListener {
  /**
   * The method that is executed when the name of an object changes
   *
   * @param object
   *          The object for which there is a name change
   * @param oldName
   *          the old name
   * @param newName
   *          the new name
   */
  public void nameChanged( Object object, String oldName, String newName );
}
