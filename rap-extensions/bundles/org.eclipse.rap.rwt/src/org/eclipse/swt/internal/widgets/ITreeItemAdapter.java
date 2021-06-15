/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 *     EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;


public interface ITreeItemAdapter {

  String[] getTexts();
  Image[] getImages();
  Color[] getCellBackgrounds();
  Color[] getCellForegrounds();
  Font[] getCellFonts();
  boolean isParentDisposed();

}
