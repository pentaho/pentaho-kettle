/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
