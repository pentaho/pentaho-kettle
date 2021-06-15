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

import org.eclipse.rap.rwt.internal.theme.css.StyleSheet;
import org.eclipse.rap.rwt.service.ResourceLoader;


final class ThemeableWidget {

  final String className;
  final ResourceLoader loader;
  CssElement[] elements;
  StyleSheet defaultStyleSheet;

  ThemeableWidget( String className, ResourceLoader loader ) {
    this.className = className;
    this.loader = loader;
  }

}
