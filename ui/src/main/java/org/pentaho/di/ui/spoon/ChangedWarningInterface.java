/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.spoon;

/**
 * Facilitate overriding of the ChangedWarning dialog used by implementors of TabItemInterface
 *
 * @author cboyden
 */
public interface ChangedWarningInterface {
  /**
   * Display a dialog asking the user if they want to save their work before closing the tab
   *
   * @return The decision of the user: SWT.YES; SWT.NO; SWT.CANCEL
   * @throws Exception
   */
  public int show() throws Exception;

  public int show( String fileName ) throws Exception;
}
