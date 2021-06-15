/*******************************************************************************
 * Copyright (c) 2009, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.dnd;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Control;


public final class DNDUtil {

  private static final String PREFIX = DNDUtil.class.getName();
  private static final String CANCEL = PREFIX.concat( "#cancel" );
  private static final String DETAIL_CHANGED_VALUE = PREFIX.concat( "#detailChangedValue" );
  private static final String DETAIL_CHANGED_CONTROL = PREFIX.concat( "#detailChangedControl" );
  private static final String FEEDBACK_CHANGED_VALUE = PREFIX.concat( "#feedbackChangedValue" );
  private static final String FEEDBACK_CHANGED_CONTROL = PREFIX.concat( "#feedbackChangedControl" );
  private static final String DATATYPE_CHANGED_VALUE = PREFIX.concat( "#dataTypeChangedValue" );
  private static final String DATATYPE_CHANGED_CONTROL = PREFIX.concat( "#dataTypeChangedControl" );

  public static void cancel() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    serviceStore.setAttribute( CANCEL, Boolean.TRUE );
    cancelDetailChanged();
    cancelFeedbackChanged();
    cancelDataTypeChanged();
  }

  public static boolean isCanceled() {
    return ContextProvider.getServiceStore().getAttribute( CANCEL ) != null;
  }

  public static void setDetailChanged( Control control, int detail ) {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    serviceStore.setAttribute( DETAIL_CHANGED_VALUE, Integer.valueOf( detail ) );
    serviceStore.setAttribute( DETAIL_CHANGED_CONTROL, control );
  }

  public static void cancelDetailChanged() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    serviceStore.setAttribute( DETAIL_CHANGED_VALUE, null );
    serviceStore.setAttribute( DETAIL_CHANGED_CONTROL, null );
  }

  public static boolean hasDetailChanged() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    Object value = serviceStore.getAttribute( DETAIL_CHANGED_VALUE );
    return value != null;
  }

  public static int getDetailChangedValue() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    Integer value = ( Integer )serviceStore.getAttribute( DETAIL_CHANGED_VALUE );
    return value.intValue();
  }

  public static Control getDetailChangedControl() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    return ( Control )serviceStore.getAttribute( DETAIL_CHANGED_CONTROL );
  }

  public static void setFeedbackChanged( Control control, int feedback ) {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    serviceStore.setAttribute( FEEDBACK_CHANGED_VALUE, Integer.valueOf( feedback ) );
    serviceStore.setAttribute( FEEDBACK_CHANGED_CONTROL, control );
  }

  public static void cancelFeedbackChanged() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    serviceStore.setAttribute( FEEDBACK_CHANGED_VALUE, null );
    serviceStore.setAttribute( FEEDBACK_CHANGED_CONTROL, null );
  }

  public static boolean hasFeedbackChanged() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    Object value = serviceStore.getAttribute( FEEDBACK_CHANGED_VALUE );
    return value != null;
  }

  public static int getFeedbackChangedValue() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    Integer value = ( Integer )serviceStore.getAttribute( FEEDBACK_CHANGED_VALUE );
    return value.intValue();
  }

  public static Control getFeedbackChangedControl() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    return ( Control )serviceStore.getAttribute( FEEDBACK_CHANGED_CONTROL );
  }

  public static void setDataTypeChanged( Control control, TransferData dataType ) {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    serviceStore.setAttribute( DATATYPE_CHANGED_VALUE, dataType );
    serviceStore.setAttribute( DATATYPE_CHANGED_CONTROL, control );
  }

  public static void cancelDataTypeChanged() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    serviceStore.setAttribute( DATATYPE_CHANGED_VALUE, null );
    serviceStore.setAttribute( DATATYPE_CHANGED_CONTROL, null );
  }

  public static boolean hasDataTypeChanged() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    Object value = serviceStore.getAttribute( DATATYPE_CHANGED_VALUE );
    return value != null;
  }

  public static TransferData getDataTypeChangedValue() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    return ( TransferData )serviceStore.getAttribute( DATATYPE_CHANGED_VALUE );
  }

  public static Control getDataTypeChangedControl() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    return ( Control )serviceStore.getAttribute( DATATYPE_CHANGED_CONTROL );
  }

  public static JsonArray convertTransferTypes( Transfer[] transfer ) {
    JsonArray array = new JsonArray();
    for( int i = 0; i < transfer.length; i++ ) {
      TransferData[] supported = transfer[ i ].getSupportedTypes();
      for( int j = 0; j < supported.length; j++ ) {
        array.add( Integer.toString( supported[ j ].type ) );
      }
    }
    return array;
  }

  public static JsonArray convertOperations( int operations ) {
    JsonArray array = new JsonArray();
    if( ( operations & DND.DROP_COPY ) != 0  ) {
      array.add( "DROP_COPY" );
    }
    if( ( operations & DND.DROP_MOVE ) != 0  ) {
      array.add( "DROP_MOVE" );
    }
    if( ( operations & DND.DROP_LINK ) != 0  ) {
      array.add( "DROP_LINK" );
    }
    return array;
  }

  private DNDUtil() {
    // prevent instantiation
  }

}
