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


/**
 * Setting store listeners are notified when an attribute in the setting store has been changed
 * (i.e. added, modified, removed).
 * <p>
 *
 * @see SettingStore#addSettingStoreListener(SettingStoreListener)
 * @see SettingStore#removeSettingStoreListener(SettingStoreListener) <p>
 * @since 2.0
 */
public interface SettingStoreListener {

  /**
   * This method is invoked when an attribute in the setting store has been changed (i.e. added,
   * modified, removed).
   *
   * @param event an event object that contains details about the attribute change
   */
  void settingChanged( SettingStoreEvent event );

}
