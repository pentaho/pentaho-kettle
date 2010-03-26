/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.ui.spoon;

/**
 * Facilitate overriding of the ChangedWarning dialog used by implementors of TabItemInterface
 * 
 * @author cboyden
 */
public interface ChangedWarningInterface {
  /**
   * Display a dialog asking the user if they want to save their work before closing the tab 
   * @return The decision of the user: SWT.YES; SWT.NO; SWT.CANCEL
   * @throws Exception 
   */
  public int show() throws Exception;
  
  public int show(String fileName) throws Exception;
}
