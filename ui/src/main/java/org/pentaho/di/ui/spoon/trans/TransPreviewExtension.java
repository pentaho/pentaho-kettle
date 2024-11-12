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
