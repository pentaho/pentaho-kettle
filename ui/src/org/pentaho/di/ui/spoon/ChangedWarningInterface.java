/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
   * @return The decision of the user: SWT.YES; SWT.NO; SWT.CANCEL
   * @throws Exception 
   */
  public int show() throws Exception;
  
  public int show(String fileName) throws Exception;
}
