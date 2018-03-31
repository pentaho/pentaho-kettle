/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.trans;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class TransPreviewExtension {

  private Composite previewTab;
  private Control previewToolbar;
  private Composite preview;

  /**
   * 
   * @param previewTab
   * @param previewToolbar
   */
  public TransPreviewExtension( Composite previewTab, Control previewToolbar, Composite preview ) {
    this.preview = preview;
    this.previewTab = previewTab;
    this.previewToolbar = previewToolbar;
  }

  public Composite getPreviewTab() {
    return previewTab;
  }

  public Control getPreviewToolbar() {
    return previewToolbar;
  }

  public Composite getPreview() {
    return preview;
  }
}
