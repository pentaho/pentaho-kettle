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
package org.eclipse.rap.rwt.internal.lifecycle;

import static org.eclipse.rap.rwt.internal.service.ContextProvider.getUISession;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


public final class WidgetDataUtil {

  private static final String ATTR_DATA_KEYS = WidgetUtil.class.getName() + "#dataKeys";

  public static void registerDataKeys( String... keys ) {
    Collection<String> dataKeys = getDataKeys( true );
    for( String key : keys ) {
      if( key != null ) {
        dataKeys.add( key );
      }
    }
  }

  public static Collection<String> getDataKeys() {
    Set<String> dataKeys =  getDataKeys( false );
    if( dataKeys != null ) {
      return Collections.unmodifiableSet( dataKeys );
    }
    return Collections.emptySet();
  }

  @SuppressWarnings( "unchecked" )
  private static Set<String> getDataKeys( boolean create ) {
    Set<String> dataKeys = ( Set<String> )getUISession().getAttribute( ATTR_DATA_KEYS );
    if( dataKeys == null && create ) {
      dataKeys = new LinkedHashSet<>();
      getUISession().setAttribute( ATTR_DATA_KEYS, dataKeys );
    }
    return dataKeys;
  }

  private WidgetDataUtil() {
    // prevent instantiation
  }

}
