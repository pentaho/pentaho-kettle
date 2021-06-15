/*******************************************************************************
 * Copyright (c) 2012, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.widgets;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.internal.SerializableCompatibility;


/**
 * This callback interface is used to inform application code that the result of script execution
 * or evaluation is available.
 *
 * @since 2.0
 * @see Browser#evaluate(String, BrowserCallback)
 */
public interface BrowserCallback extends SerializableCompatibility {

  /**
   * This method is called when the execution of the script succeeded.
   *
   * @param result the return value, if any, of executing the script
   * @see org.eclipse.swt.browser.Browser Browser
   */
  void evaluationSucceeded( Object result );

  /**
   * This method is called when the execution of the script failed.
   *
   * @param exception the reason for the failing script execution.
   * @see org.eclipse.swt.browser.Browser Browser
   */
  void evaluationFailed( Exception exception );

}
