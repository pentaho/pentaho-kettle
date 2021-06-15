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
package org.eclipse.swt.internal.widgets;

import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;


public interface IDisplayAdapter {

  void setBounds( Rectangle bounds );
  void setCursorLocation( int x, int y );
  void setActiveShell( Shell shell );
  void setFocusControl( Control control, boolean fireEvents );
  void invalidateFocus();
  boolean isFocusInvalidated();
  Shell[] getShells();
  UISession getUISession();

  void attachThread();
  void detachThread();
  boolean isValidThread();

  boolean isBeepCalled();
  void resetBeep();

  void notifyListeners( int eventType, Event event );
  boolean isListening( int eventType );

}
