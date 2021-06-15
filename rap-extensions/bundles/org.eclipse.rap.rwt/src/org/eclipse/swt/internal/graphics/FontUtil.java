/*******************************************************************************
 * Copyright (c) 2011 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.graphics;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;


public class FontUtil {
  
  public static FontData getData( Font font ) {
    return font.getFontData()[ 0 ];
  }
  
  private FontUtil() {
    // prevent instance creation
  }
}
