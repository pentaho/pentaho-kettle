/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.amazon.s3.fileoutput;

import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.gui.GUIOption;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

/** Adds s3a to the "Look in" choices of Spoon's VFS file chooser, used by the step's browse button. */
@LifecyclePlugin( id = "AmazonSpoonPlugin", name = "Amazon Spoon Plugin EE" )
public class AmazonSpoonPlugin implements LifecycleListener, GUIOption<Object> {

  private static final int S3A_PRIORITY = 125;

  public void onStart( LifeEventHandler arg0 ) {
    VfsFileChooserDialog dialog = Spoon.getInstance().getVfsFileChooserDialog( null, null );
    dialog.addVFSUIPanel( S3A_PRIORITY, new S3AVfsFileChooserDialog( dialog, null, null ) );
  }

  public void onExit( LifeEventHandler arg0 ) {
    // I dutifully inform sonar that this is empty because no use was found for it
  }

  public String getLabelText() {
    return null;
  }

  public Object getLastValue() {
    return null;
  }

  public DisplayType getType() {
    return null;
  }

  public void setValue( Object value ) {
  }
}
