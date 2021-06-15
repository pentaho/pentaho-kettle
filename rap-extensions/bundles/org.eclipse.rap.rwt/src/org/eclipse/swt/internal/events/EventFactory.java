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
package org.eclipse.swt.internal.events;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;


public class EventFactory {
  
  public static Event newSelectionEvent( Widget widget,
                                         Widget item,
                                         Rectangle bounds,
                                         int stateMask,
                                         String text,
                                         boolean doit,
                                         int detail )
  {
    Event event = newEvent( widget, SWT.Selection );
    event.item = item;
    event.setBounds( bounds );
    event.stateMask = stateMask;
    event.text = text;
    event.doit = doit;
    event.detail = detail;
    return event;
  }
  
  public static Event newEvent( Widget widget, int eventType ) {
    Event event = new Event();
    event.type = eventType;
    event.widget = widget;
    event.display = widget.getDisplay();
    return event;
  }

}
