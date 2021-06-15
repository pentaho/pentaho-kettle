/*******************************************************************************
 * Copyright (c) 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.client.service;

import java.util.Locale;


/**
 * The ClientInfo service provides basic information about the client device.
 *
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ClientInfo extends ClientService {

  /**
   * Returns the offset between the client's local time and UTC.
   *
   * @return the offset in minutes
   */
  public int getTimezoneOffset();

  /**
   * Returns the preferred locale of the client, based on the Accept-Language HTTP header of the
   * first client request. If the client does not provide this information, this method returns
   * <code>null</code>.
   *
   * @return the client locale, or <code>null</code>
   */
  public Locale getLocale();

  /**
   * Returns the locales accepted by the client, based on the Accept-Language HTTP header. The
   * locales are ordered by preference, beginning with the preferred locale. If the client does not
   * provide this information, this method returns an empty array.
   *
   * @return an array containing the client locales, may be empty, but never <code>null</code>
   */
  public Locale[] getLocales();

}
