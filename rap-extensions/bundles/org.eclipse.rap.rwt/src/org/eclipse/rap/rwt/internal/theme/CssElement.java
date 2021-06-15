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


/**
 * A DOM element that represents a themeable widget or a sub-widget and can be referred to in CSS
 * style sheets.
 */
public interface CssElement {

  String getName();

  String[] getProperties();

  String[] getStyles();

  String[] getStates();

}
