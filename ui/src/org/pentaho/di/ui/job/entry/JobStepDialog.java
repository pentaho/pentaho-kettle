package org.pentaho.di.ui.job.entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

import java.lang.reflect.Method;

public abstract class JobStepDialog<T extends JobEntryInterface> extends JobEntryDialog implements JobEntryDialogInterface {

//  protected static final int LEFT_PLACEMENT = 0;
//  protected static final int RIGHT_PLACEMENT = 100;
  protected static final int LARGE_MARGIN = 15;
  protected static final int FIELD_WIDTH = 60;
  protected static final int BUTTON_WIDTH = 80;

  protected PackageMessages messages;
  protected PackageMessages systemMessages = new PackageMessages( this.getClass(), "System." );

  protected Button wCancel;
  protected Button wOK;

  private boolean bigIcon;

  private final T entry;

  private final SelectionAdapter DEFAULT_FINISH_EVENT = new SelectionAdapter() {
    public void widgetDefaultSelected( SelectionEvent e ) {
      ok();
    }
  };

  private final ModifyListener DEFAULT_MODIFY_EVENT = new ModifyListener() {
    public void modifyText( ModifyEvent e ) {
      entry.setChanged();
    }
  };

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
      lIcon.setImage( JobDialog.getImage( JobDialog.getPlugin( getEntry() ) ) );
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
    try {
      Method m = null;
      try {
        m = el.getClass().getMethod( "addModifyListener", ModifyListener.class );
      } catch ( Exception e ) {
        // not found
      }
      if ( m != null ) {
        m.invoke( el, DEFAULT_MODIFY_EVENT );
      }
    } catch ( Exception e1 ) {
      // nothing
    }
  }

  private void addDefaultFinishEvent( Control el ) {
    try {
      Method m = null;
      try {
        m = el.getClass().getMethod( "addSelectionListener", SelectionAdapter.class );
      } catch ( Exception e ) {
        m = el.getClass().getMethod( "addSelectionListener", SelectionListener.class );
      }
      if ( m != null ) {
        m.invoke( el, DEFAULT_FINISH_EVENT );
      }
    } catch ( Exception e1 ) {
      // nothing
    }
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
