/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.serverpush;

import java.util.LinkedList;
import java.util.List;


class ServerPushRequestTracker {

  private transient List<Thread> callBackRequests;

  ServerPushRequestTracker() {
    callBackRequests = new LinkedList<>();
  }

  void deactivate( Thread thread ) {
    callBackRequests.remove( thread );
  }

  void activate( Thread thread ) {
    callBackRequests.add( 0, thread );
  }

  boolean hasActive() {
    return callBackRequests.isEmpty();
  }

  boolean isActive( Thread thread ) {
    return !hasActive() && callBackRequests.get( 0 ) == thread;
  }

}
