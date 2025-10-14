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


package org.pentaho.amazon;

import org.pentaho.amazon.s3.S3AVfsFileChooserDialog;
import org.pentaho.amazon.s3.S3NVfsFileChooserDialog;
import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.gui.GUIOption;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

@LifecyclePlugin( id = "AmazonSpoonPlugin", name = "Amazon Spoon Plugin EE" )
public class AmazonSpoonPlugin implements LifecycleListener, GUIOption<Object> {

  private static final int S3N_PRIORITY = 120;
  private static final int S3A_PRIORITY = 125;

  public void onStart( LifeEventHandler arg0 ) throws LifecycleException {
    VfsFileChooserDialog dialog = Spoon.getInstance().getVfsFileChooserDialog( null, null );
    Spoon.getInstance().getVfsFileChooserDialog( null, null )
      .addVFSUIPanel( S3N_PRIORITY, new S3NVfsFileChooserDialog( dialog, null, null ) );
    Spoon.getInstance().getVfsFileChooserDialog( null, null )
      .addVFSUIPanel( S3A_PRIORITY, new S3AVfsFileChooserDialog( dialog, null, null ) );
  }

  public void onExit( LifeEventHandler arg0 ) throws LifecycleException {
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
