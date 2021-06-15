/*******************************************************************************
 * Copyright (c) 2002, 2013 Innoopract Informationssysteme GmbH and others.
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


/**
 * Events of this type signal a state change of a UI session.
 *
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class UISessionEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new instance of this class.
   *
   * @param uiSession the UI session for this event
   */
  public UISessionEvent( UISession uiSession ) {
    super( uiSession );
  }

  /**
   * Returns the UI session that this event is related to.
   *
   * @return the UI session
   */
  public UISession getUISession() {
    return ( UISession )getSource();
  }

}
