/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.custom;

import org.eclipse.swt.internal.SerializableCompatibility;

class SashFormData implements SerializableCompatibility {

  long weight;

  String getName() {
    String string = getClass().getName();
    int index = string.lastIndexOf( '.' );
    if( index == -1 )
      return string;
    return string.substring( index + 1, string.length() );
  }

  /**
   * Returns a string containing a concise, human-readable description of the
   * receiver.
   * 
   * @return a string representation of the event
   */
  public String toString() {
    return getName() + " {weight=" + weight + "}"; //$NON-NLS-2$
  }
}
