/*******************************************************************************
 * Copyright (c) 2008, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme.css;


/**
 * See <a href="http://www.w3.org/TR/CSS21/cascade.html#specificity">specificity</a>
 */
public interface Specific {

  /**
   * Factor for b variable in specificity algorithm, see <a
   * href="http://www.w3.org/TR/CSS21/cascade.html#specificity">http://www.w3.org/TR/CSS21/cascade.html#specificity</a>.
   */
  static int ID_SPEC = 1 << 16;

  /**
   * Factor for c variable in specificity algorithm, see <a
   * href="http://www.w3.org/TR/CSS21/cascade.html#specificity">http://www.w3.org/TR/CSS21/cascade.html#specificity</a>.
   */
  static int ATTR_SPEC = 1 << 8;

  /**
   * Factor for d variable in specificity algorithm, see <a
   * href="http://www.w3.org/TR/CSS21/cascade.html#specificity">http://www.w3.org/TR/CSS21/cascade.html#specificity</a>.
   */
  static int ELEMENT_SPEC = 1;

  int getSpecificity();
}
