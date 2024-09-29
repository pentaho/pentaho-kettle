/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.step;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.file.BaseFileInputMeta;

public abstract class BaseFileStepDialog<T extends BaseFileInputMeta<?, ?, ?>> extends BaseStepDialog implements
    StepDialogInterface {
  protected final Class<?> PKG = getClass();

  protected T input;
  protected ModifyListener lsMod;

  public BaseFileStepDialog( Shell parent, T in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = in;
  }

  @Override
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    createUI();

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData( input );
    setSize();

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  protected void cancel() {
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  protected void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    getInfo( input, false );
    dispose();
  }

  protected abstract void createUI();

  /**
   * Read the data from the meta object and show it in this dialog.
   *
   * @param meta
   *          The meta object to obtain the data from.
   */
  protected abstract void getData( T meta );

  /**
   * Fill meta object from UI options.
   *
   * @param meta
   *          meta object
   * @param preview
   *          flag for preview or real options should be used. Currently, only one option is differ for preview - EOL
   *          chars. It uses as "mixed" for be able to preview any file.
   */
  protected abstract void getInfo( T meta, boolean preview );

  /**
   * Class for apply layout settings to SWT controls.
   */
  public static class FD {
    private final Control control;
    private final FormData fd;

    public FD( Control control ) {
      this.control = control;
      fd = new FormData();
    }

    public FD width( int width ) {
      fd.width = width;
      return this;
    }

    public FD height( int height ) {
      fd.height = height;
      return this;
    }

    public FD top( int numerator, int offset ) {
      fd.top = new FormAttachment( numerator, offset );
      return this;
    }

    public FD top( Control control, int offset ) {
      fd.top = new FormAttachment( control, offset );
      return this;
    }

    public FD bottom( int numerator, int offset ) {
      fd.bottom = new FormAttachment( numerator, offset );
      return this;
    }

    public FD bottom( Control control, int offset ) {
      fd.bottom = new FormAttachment( control, offset );
      return this;
    }

    public FD left( int numerator, int offset ) {
      fd.left = new FormAttachment( numerator, offset );
      return this;
    }

    public FD left( Control control, int offset ) {
      fd.left = new FormAttachment( control, offset );
      return this;
    }

    public FD right( int numerator, int offset ) {
      fd.right = new FormAttachment( numerator, offset );
      return this;
    }

    public FD right( Control control, int offset ) {
      fd.right = new FormAttachment( control, offset );
      return this;
    }

    public void apply() {
      control.setLayoutData( fd );
    }
  }
}
