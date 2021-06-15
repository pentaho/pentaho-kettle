/*******************************************************************************
 * Copyright (c) 2007, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.browser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Event;


/**
 * A <code>LocationEvent</code> is sent by a {@link Browser} to
 * {@link LocationListener}'s when the <code>Browser</code>
 * navigates to a different URL. This notification typically
 * occurs when the application navigates to a new location with
 * {@link Browser#setUrl(String)} or when the user activates a
 * hyperlink.
 *
 * @since 1.0
 */
public class LocationEvent extends TypedEvent {

  private static final long serialVersionUID = 1L;

  /** current location */
  public String location;

  /**
   * A flag indicating whether the location opens in the top frame
   * or not.
   */
  public boolean top;

  /**
   * A flag indicating whether the location loading should be allowed.
   * Setting this field to <code>false</code> will cancel the operation.
   */
  public boolean doit = true;

  
  LocationEvent( Event event ) {
    super( event );
    location = event.text;
    top = event.detail == SWT.TOP;
  }

}
