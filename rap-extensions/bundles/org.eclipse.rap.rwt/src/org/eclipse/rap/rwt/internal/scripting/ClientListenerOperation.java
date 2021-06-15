/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.scripting;

import org.eclipse.rap.rwt.scripting.ClientListener;


public abstract class ClientListenerOperation {

  private final int eventType;
  private final ClientListener listener;

  public ClientListenerOperation( int eventType, ClientListener listener ) {
    this.eventType = eventType;
    this.listener = listener;
  }

  public int getEventType() {
    return eventType;
  }

  public ClientListener getListener() {
    return listener;
  }

  public static final class AddListener extends ClientListenerOperation {

    public AddListener( int eventType, ClientListener listener ) {
      super( eventType, listener );
    }

  }

  public static final class RemoveListener extends ClientListenerOperation {

    public RemoveListener( int eventType, ClientListener listener ) {
      super( eventType, listener );
    }

  }

}
