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
package org.pentaho.di.ui.job.entry;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TypedListener;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.PackageMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.WidgetUtils;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public abstract class JobStepDialog<T extends JobEntryInterface> extends JobEntryDialog implements
    JobEntryDialogInterface {

  protected static final int LARGE_MARGIN = 15;
  protected static final int FIELD_WIDTH = 60;
  protected static final int BUTTON_WIDTH = 80;

  protected PackageMessages messages;
  protected PackageMessages systemMessages = new PackageMessages( this.getClass(), "System." );

  protected Button wCancel;
  protected Button wOK;

  private boolean bigIcon;

  private final T entry;

  private final TypedListener DEFAULT_FINISH_EVENT = new TypedListener( new SelectionAdapter() {
    public void widgetDefaultSelected( SelectionEvent e ) {
      ok();
    }
  } );

  private final TypedListener CHANGE_MODIFY_LISTENER = new TypedListener( new ModifyListener() {
    @Override
    public void modifyText( ModifyEvent paramModifyEvent ) {
      dialogModified();
    }
  } );

  private final TypedListener CHANGE_SELECT_LISTENER = new TypedListener( new SelectionAdapter() {
    @Override
    public void widgetSelected( SelectionEvent paramSelectionEvent ) {
      dialogModified();
    }
  } );

  private void dialogModified() {
    entry.setChanged();
  }

  @SuppressWarnings( "unchecked" )
  public JobStepDialog( final Shell parent, final JobEntryInterface jobEntryInt, final Repository rep,
      final JobMeta jobMeta, boolean bigIcon ) {
    super( parent, jobEntryInt, rep, jobMeta );
    entry = (T) jobEntryInt;
    this.bigIcon = bigIcon;
    initMessages();
    if ( entry.getName() == null ) {
      entry.setName( messages.getString( "Title" ) );
    }
  }

  protected void initMessages() {
    messages = new PackageMessages( this.getClass() );
  }

  public T getEntry() {
    return entry;
  }

  @Override
  public JobEntryInterface open() {

    shell = new Shell( getParent(), props.getJobsDialogStyle() );
    props.setLook( shell );
    WidgetUtils.setFormLayout( shell, LARGE_MARGIN );
    JobDialog.setShellImage( shell, entry );
    shell.setText( messages.getString( "Title" ) );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    if ( bigIcon ) {
      Label lIcon = new Label( shell, SWT.RIGHT );
      lIcon.setLayoutData( new FormDataBuilder().right().result() );
      lIcon.setImage( JobDialog.getImage( shell, JobDialog.getPlugin( getEntry() ) ) );
    }

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( systemMessages.getString( "Button.Cancel" ) );
    wCancel.setLayoutData( new FormDataBuilder().bottom().right().width( BUTTON_WIDTH ).result() );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( systemMessages.getString( "Button.OK" ) );
    wOK.setLayoutData( new FormDataBuilder().bottom().right( wCancel, -ConstUI.SMALL_MARGIN ).width( BUTTON_WIDTH )
        .result() );

    doOpen();

    getData();

    handleChilds( shell );

    BaseStepDialog.setSize( shell );

    wCancel.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    } );
    wOK.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    } );

    shell.open();

    Display display = getParent().getDisplay();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }

    return getEntry();
  }

  private void handleChilds( Composite composite ) {
    for ( Control el : composite.getChildren() ) {
      handleElement( el );
      if ( el instanceof Composite ) {
        handleChilds( (Composite) el );
      }
    }
  }

  protected void handleElement( Control el ) {
    handleElement( el, Props.WIDGET_STYLE_DEFAULT );
  }

  protected void handleElement( Control el, int style ) {
    props.setLook( el, style );
    addDefaultFinishEvent( el );
    addModifyListener( el );
  }

  private void addModifyListener( Control el ) {
    if ( el instanceof Text ) {
      addListener( el, 24, CHANGE_MODIFY_LISTENER );
    }
    if ( el instanceof Button ) {
      addListener( el, 13, CHANGE_SELECT_LISTENER );
    }
  }

  private void addDefaultFinishEvent( Control el ) {
    addListener( el, 14, DEFAULT_FINISH_EVENT );
  }

  private void addListener( Control el, int event, TypedListener listener ) {
    if ( ArrayUtils.contains( el.getListeners( event ), listener ) ) {
      return;
    }
    el.addListener( event, listener );
  }

  private void dispose() {
    WindowProperty winprop = new WindowProperty( shell );
    props.setScreen( winprop );
    shell.dispose();
  }

  protected void ok() {
    if ( entry.hasChanged() ) {
      doOk();
    }
    dispose();
  }

  protected void cancel() {
    entry.setChanged( false );
    dispose();
  }

  protected abstract void getData();

  protected abstract void doOk();

  protected abstract void doOpen();

}
