/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

/*
 * Created on 19-jun-2003
 *
 */

package pdi.jobentry.dummy;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;



/**
 * This dialog allows you to edit the SQL job entry settings. (select the connection and the sql script to be executed)
 *
 * @author Matt
 * @since  19-06-2003
 */
public class JobEntryDummyDialog extends JobEntryDialog implements JobEntryDialogInterface {

  private Label        wlName;
  private Text         wName;
  private FormData     fdlName, fdName;

  private Label        wlSourceDirectory;
  private TextVar      wSourceDirectory;
  private FormData     fdlSourceDirectory, fdSourceDirectory;

  private Label        wlTargetDirectory;
  private TextVar      wTargetDirectory;
  private FormData     fdlTargetDirectory, fdTargetDirectory;

  private Label        wlWildcard;
  private TextVar      wWildcard;
  private FormData     fdlWildcard, fdWildcard;

  private Button wOK, wCancel;
  private Listener lsOK, lsCancel;

  private JobEntryDummy jobEntry;
  private Shell shell;
  private PropsUI props;

  private SelectionAdapter lsDef;

  private boolean changed;

  public JobEntryDummyDialog( Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta ) {
    super( parent, jobEntryInt, rep, jobMeta );
    props = PropsUI.getInstance();
    this.jobEntry = (JobEntryDummy) jobEntryInt;

    if ( this.jobEntry.getName() == null ) {
      this.jobEntry.setName( jobEntryInt.getName() );
    }
  }

  @Override
  public JobEntryInterface open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    JobDialog.setShellImage( shell, jobEntry );

    ModifyListener lsMod = new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        jobEntry.setChanged();
      }
    };
    changed = jobEntry.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( Messages.getString( "DummyPluginDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Filename line
    wlName = new Label( shell, SWT.RIGHT );
    wlName.setText( "Job entry name " );
    props.setLook( wlName );
    fdlName = new FormData();
    fdlName.left = new FormAttachment( 0, 0 );
    fdlName.right = new FormAttachment( middle, -margin );
    fdlName.top = new FormAttachment( 0, margin );
    wlName.setLayoutData( fdlName );
    wName = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wName );
    wName.addModifyListener( lsMod );
    fdName = new FormData();
    fdName.left = new FormAttachment( middle, 0 );
    fdName.top = new FormAttachment( 0, margin );
    fdName.right = new FormAttachment( 100, 0 );
    wName.setLayoutData( fdName );

    // SourceDirectory line
    wlSourceDirectory = new Label( shell, SWT.RIGHT );
    wlSourceDirectory.setText( "Source directory" );
    props.setLook( wlSourceDirectory );
    fdlSourceDirectory = new FormData();
    fdlSourceDirectory.left = new FormAttachment( 0, 0 );
    fdlSourceDirectory.top = new FormAttachment( wName, margin );
    fdlSourceDirectory.right = new FormAttachment( middle, -margin );
    wlSourceDirectory.setLayoutData( fdlSourceDirectory );
    wSourceDirectory = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSourceDirectory );
    wSourceDirectory.addModifyListener( lsMod );
    fdSourceDirectory = new FormData();
    fdSourceDirectory.left = new FormAttachment( middle, 0 );
    fdSourceDirectory.top = new FormAttachment( wName, margin );
    fdSourceDirectory.right = new FormAttachment( 100, 0 );
    wSourceDirectory.setLayoutData( fdSourceDirectory );

    // TargetDirectory line
    wlTargetDirectory = new Label( shell, SWT.RIGHT );
    wlTargetDirectory.setText( "Target directory" );
    props.setLook( wlTargetDirectory );
    fdlTargetDirectory = new FormData();
    fdlTargetDirectory.left = new FormAttachment( 0, 0 );
    fdlTargetDirectory.top = new FormAttachment( wSourceDirectory, margin );
    fdlTargetDirectory.right = new FormAttachment( middle, -margin );
    wlTargetDirectory.setLayoutData( fdlTargetDirectory );
    wTargetDirectory = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTargetDirectory );
    wTargetDirectory.addModifyListener( lsMod );
    fdTargetDirectory = new FormData();
    fdTargetDirectory.left = new FormAttachment( middle, 0 );
    fdTargetDirectory.top = new FormAttachment( wSourceDirectory, margin );
    fdTargetDirectory.right = new FormAttachment( 100, 0 );
    wTargetDirectory.setLayoutData( fdTargetDirectory );

    // Wildcard line
    wlWildcard = new Label( shell, SWT.RIGHT );
    wlWildcard.setText( "Wildcard (regular expression)" );
    props.setLook( wlWildcard );
    fdlWildcard = new FormData();
    fdlWildcard.left = new FormAttachment( 0, 0 );
    fdlWildcard.top = new FormAttachment( wTargetDirectory, margin );
    fdlWildcard.right = new FormAttachment( middle, -margin );
    wlWildcard.setLayoutData( fdlWildcard );
    wWildcard = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wWildcard );
    wWildcard.addModifyListener( lsMod );
    fdWildcard = new FormData();
    fdWildcard.left = new FormAttachment( middle, 0 );
    fdWildcard.top = new FormAttachment( wTargetDirectory, margin );
    fdWildcard.right = new FormAttachment( 100, 0 );
    wWildcard.setLayoutData( fdWildcard );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( " &OK " );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( " &Cancel " );

    BaseStepDialog.positionBottomButtons( shell, new Button[] { wOK, wCancel }, margin, wWildcard );

    // Add listeners
    lsCancel = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsOK = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );

    lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wSourceDirectory.addSelectionListener( lsDef );
    wTargetDirectory.addSelectionListener( lsDef );
    wWildcard.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();

    BaseStepDialog.setSize( shell );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return jobEntry;
  }

  public void dispose() {
    WindowProperty winprop = new WindowProperty( shell );
    props.setScreen( winprop );
    shell.dispose();
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if ( jobEntry.getName() != null ) {
      wName.setText( jobEntry.getName() );
    }
    wName.selectAll();

    wSourceDirectory.setText( Const.NVL( jobEntry.getSourceDirectory(), "" ) );
    wTargetDirectory.setText( Const.NVL( jobEntry.getTargetDirectory(), "" ) );
    wWildcard.setText( Const.NVL( jobEntry.getWildcard(), "" ) );
  }

  private void cancel() {
    jobEntry.setChanged( changed );
    jobEntry = null;
    dispose();
  }

  private void ok() {
    jobEntry.setName( wName.getText() );
    jobEntry.setSourceDirectory( wSourceDirectory.getText() );
    jobEntry.setTargetDirectory( wTargetDirectory.getText() );
    jobEntry.setWildcard( wWildcard.getText() );

    dispose();
  }

  @Override
  public String toString() {
    return this.getClass().getName();
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return false;
  }

}
