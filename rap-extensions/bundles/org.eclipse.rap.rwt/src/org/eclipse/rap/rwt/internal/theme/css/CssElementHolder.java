/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSoource - ongoing development
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme.css;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.internal.theme.CssElement;


/**
 * Holds all registered {@link CssElement}s.
 */
public class CssElementHolder {

  private final Map<String, CssElement> elements;

  public CssElementHolder() {
    elements = new HashMap<>();
  }

  public void addElement( CssElement element ) {
    if( elements.containsKey( element.getName() ) ) {
      String message = "An element with this name is already defined: " + element.getName();
      throw new IllegalArgumentException( message );
    }
    elements.put( element.getName(), element );
  }

  public CssElement[] getAllElements() {
    Collection<CssElement> values = elements.values();
    return values.toArray( new CssElement[ values.size() ] );
  }

  public void clear() {
    elements.clear();
  }

}
