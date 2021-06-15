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
package org.eclipse.rap.rwt.internal.engine;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.rap.rwt.service.UISession;


public class PostDeserialization {

  private static final String ATTR_PROCESSORS = PostDeserialization.class.getName() + "#processors";

  public static void runProcessors( UISession uiSession ) {
    List<Runnable> processors = getProcessors( uiSession );
    if( processors != null ) {
      clearProcessors( uiSession );
      for( Runnable processor : processors ) {
        processor.run();
      }
    }
  }

  public static void addProcessor( UISession uiSession, Runnable processor ) {
    List<Runnable> processorsList = getProcessors( uiSession );
    if( processorsList == null ) {
      processorsList = new LinkedList<>();
      uiSession.setAttribute( ATTR_PROCESSORS, processorsList );
    }
    processorsList.add( processor );
  }

  @SuppressWarnings("unchecked")
  private static List<Runnable> getProcessors( UISession uiSession ) {
    return ( List<Runnable> )uiSession.getAttribute( ATTR_PROCESSORS );
  }

  private static void clearProcessors( UISession uiSession ) {
    uiSession.removeAttribute( ATTR_PROCESSORS );
  }

  private PostDeserialization() {
    // prevent instantiation
  }

}
