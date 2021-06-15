/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.service;

import java.util.EventObject;

import org.eclipse.rap.rwt.internal.util.ParamCheck;


/**
 * An event that contains the details of a single attribute change in the setting store. The change
 * can be one of the following:
 * <ul>
 * <li>an attribute was added (in this case the old value is <code>null</code>)</li>
 * <li>and attribute was modified</li>
 * <li>an attribute was removed (in this case the new value is <code>null</code>)
 * </ul>
 * <strong>Note:</strong> Clients are responsible for using the {@link #getAttributeName()} method
 * to check if the changed attribute is of interest to them.
 * <p>
 *
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public final class SettingStoreEvent extends EventObject {

  private static final long serialVersionUID = 1L;

  private final String attributeName;
  private final String oldValue;
  private final String newValue;

  public SettingStoreEvent( SettingStore source,
                            String attributeName,
                            String oldValue,
                            String newValue )
  {
    super( source );
    ParamCheck.notNull( attributeName, "attributeName" );
    this.attributeName = attributeName;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  /**
   * Returns the name of the changed attribute.
   *
   * @return the attribute name, never <code>null</code>
   */
  public String getAttributeName() {
    return attributeName;
  }

  /**
   * Return the value of the attribute after the change.
   *
   * @return the new attribute value, may be <code>null</code> if the attribute has been removed
   */
  public String getNewValue() {
    return newValue;
  }

  /**
   * Returns the value of the attribute before the change.
   *
   * @return the previous attribute value, may be <code>null</code> if the attribute has been added
   */
  public String getOldValue() {
    return oldValue;
  }

}
