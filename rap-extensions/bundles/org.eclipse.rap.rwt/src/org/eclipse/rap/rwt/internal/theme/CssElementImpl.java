/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme;

import java.util.ArrayList;
import java.util.List;


public class CssElementImpl implements CssElement {

  private final String name;
  private final List<String> properties;
  private final List<String> styles;
  private final List<String> states;

  public CssElementImpl( String name ) {
    this.name = name;
    properties = new ArrayList<>();
    styles = new ArrayList<>();
    states = new ArrayList<>();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String[] getProperties() {
    return properties.toArray( new String[ properties.size() ]);
  }

  @Override
  public String[] getStyles() {
    return styles.toArray( new String[ styles.size() ] );
  }

  @Override
  public String[] getStates() {
    return states.toArray( new String[ states.size() ] );
  }

  public CssElementImpl addProperty( String property ) {
    properties.add( property );
    return this;
  }

  public CssElementImpl addStyle( String style ) {
    styles.add( style );
    return this;
  }

  public CssElementImpl addState( String state ) {
    states.add( state );
    return this;
  }

}
