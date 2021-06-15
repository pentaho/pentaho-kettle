/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.scripting;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.rwt.internal.scripting.ClientListenerOperation.AddListener;
import org.eclipse.rap.rwt.internal.scripting.ClientListenerOperation.RemoveListener;
import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Widget;


public class ClientListenerUtil {

  private static final String OPERATIONS = "rwt.clientListenerOperations";

  public static String getRemoteId( ClientFunction function ) {
    return function.getRemoteId();
  }

  public static String getEventType( int bindingType ) {
    switch( bindingType ) {
      case SWT.KeyUp:
        return "KeyUp";
      case SWT.KeyDown:
        return "KeyDown";
      case SWT.FocusIn:
        return "FocusIn";
      case SWT.FocusOut:
        return "FocusOut";
      case SWT.MouseDown:
        return "MouseDown";
      case SWT.MouseUp:
        return "MouseUp";
      case SWT.MouseEnter:
        return "MouseEnter";
      case SWT.MouseExit:
        return "MouseExit";
      case SWT.MouseMove:
        return "MouseMove";
      case SWT.MouseDoubleClick:
        return "MouseDoubleClick";
      case SWT.Modify:
        return "Modify";
      case SWT.Show:
        return "Show";
      case SWT.Hide:
        return "Hide";
      case SWT.Verify:
        return "Verify";
      case SWT.Paint:
        return "Paint";
      case SWT.Resize:
        return "Resize";
      case SWT.Selection:
        return "Selection";
      case SWT.DefaultSelection:
        return "DefaultSelection";
      case SWT.MouseWheel:
        return "MouseWheel";
      default:
        throw new IllegalArgumentException( "Unsupported event type " + bindingType );
    }
  }

  public static void clientListenerAdded( Widget widget, int eventType, ClientListener listener ) {
    List<ClientListenerOperation> operations = getClientListenerOperations( widget );
    if( operations == null ) {
      operations = new ArrayList<>( 1 );
      widget.setData( OPERATIONS, operations );
    }
    operations.add( new AddListener( eventType, listener ) );
  }

  public static void clientListenerRemoved( Widget widget, int eventType, ClientListener listener )
  {
    List<ClientListenerOperation> operations = getClientListenerOperations( widget );
    if( operations == null ) {
      operations = new ArrayList<>( 1 );
      widget.setData( OPERATIONS, operations );
    }
    operations.add( new RemoveListener( eventType, listener ) );
  }

  @SuppressWarnings( "unchecked" )
  public static List<ClientListenerOperation> getClientListenerOperations( Widget widget ) {
    return ( List<ClientListenerOperation> )widget.getData( OPERATIONS );
  }

  public static void clearClientListenerOperations( Widget widget ) {
    widget.setData( OPERATIONS, null );
  }

}
