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

package org.pentaho.di.core.gui;

/**
 * Classes implementing this interface have a chance to manage their internal representation states using the options
 * dialog in Kettle.
 *
 * Instances of this class are automatically added to the EnterOptionsDialog.
 *
 * @author Alex Silva
 *
 */
public interface GUIOption<E> {
  /**
   * How the GUI should display the preference represented by this class.
   *
   * @author Alex Silva
   *
   */
  enum DisplayType {
    CHECK_BOX, TEXT_FIELD, ACTION_BUTTON
  }

  public E getLastValue();

  /**
   * Sets the value; should also persist it.
   *
   * @param value
   */
  public void setValue( E value );

  public DisplayType getType();

  String getLabelText();

}
