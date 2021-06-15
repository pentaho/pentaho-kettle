/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.lifecycle;

import static org.eclipse.rap.rwt.internal.service.ContextProvider.getUISession;

import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.swt.internal.SerializableCompatibility;


public final class RequestCounter implements SerializableCompatibility {

  private int requestId;

  public static RequestCounter getInstance() {
    return SingletonUtil.getUniqueInstance( RequestCounter.class, getUISession() );
  }

  public int nextRequestId() {
    return ++requestId;
  }

  public int currentRequestId() {
    return requestId;
  }

}
