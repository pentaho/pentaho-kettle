/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016 - 2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.dialog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.capabilities.api.ICapability;
import org.pentaho.capabilities.impl.DefaultCapabilityManager;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by nbaker on 4/7/15.
 */
public class CapabilityManagerDialog {

  private final Shell parent;
  private final PropsUI props;
  private Shell shell;
  private List<Button> buttons = new ArrayList<Button>();

  public CapabilityManagerDialog( Shell parent ) {
    this.parent = parent;
    props = PropsUI.getInstance();
  }

  public static void main( String[] args ) {

    Display display = new Display(  );
    try {
      KettleEnvironment.init();

      PropsUI.init( display, Props.TYPE_PROPERTIES_SPOON );

      KettleLogStore
          .init( PropsUI.getInstance().getMaxNrLinesInLog(), PropsUI.getInstance().getMaxLogLineTimeoutMinutes() );

    } catch ( KettleException e ) {
      e.printStackTrace();
    }

    KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.SPOON );
    Shell shell = new Shell( display, SWT.DIALOG_TRIM );
    shell.open();
    CapabilityManagerDialog capabilityManagerDialog = new CapabilityManagerDialog( shell );
    capabilityManagerDialog.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  public void open() {
    final Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    shell.setImage( GUIResource.getInstance().getImageSpoon() );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( getClass(), "CapabilityManager.Dialog.Title" ) );


    int margin = Const.MARGIN;

    Button closeButton = new Button( shell, SWT.PUSH );
    closeButton.setText( BaseMessages.getString( getClass(), "System.Button.Close" ) );

    BaseStepDialog.positionBottomButtons( shell, new Button[] { closeButton, }, margin, null );

    // Add listeners
    closeButton.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        shell.dispose();
      }
    } );

    ScrolledComposite scrollpane = new ScrolledComposite( shell, SWT.BORDER | SWT.V_SCROLL );

    FormData treeFormData = new FormData();
    treeFormData.left = new FormAttachment( 0, 0 ); // To the right of the label
    treeFormData.top = new FormAttachment( 0, 0 );
    treeFormData.right = new FormAttachment( 100, 0 );
    Label label = new Label( shell, SWT.NONE );
    label.setText( "Capabilities:" );
    label.setLayoutData( treeFormData );

    treeFormData = new FormData();
    treeFormData.left = new FormAttachment( 0, 0 ); // To the right of the label
    treeFormData.top = new FormAttachment( label, 0 );
    treeFormData.right = new FormAttachment( 100, 0 );
    treeFormData.bottom = new FormAttachment( closeButton, -margin * 2 );
    scrollpane.setLayoutData( treeFormData );
    scrollpane.setExpandVertical( true );
    scrollpane.setExpandHorizontal( true );
    scrollpane.setAlwaysShowScrollBars( true );


    Composite mainPanel = new Composite( scrollpane, SWT.NONE );
    scrollpane.setContent( mainPanel );
    scrollpane.setSize( 250, 400 );
    mainPanel.setLayout( new GridLayout( 1, false ) );

    Set<ICapability> allCapabilities = DefaultCapabilityManager.getInstance().getAllCapabilities();
    SortedSet<ICapability> capabilitySortedSet = new TreeSet<ICapability>( allCapabilities );

    for ( final ICapability capability : capabilitySortedSet ) {
      final Button button = new Button( mainPanel, SWT.CHECK );
      button.setLayoutData( new GridData( GridData.FILL_BOTH, SWT.BEGINNING, false, false ) );
      button.setSelection( capability.isInstalled() );
      button.setText( capability.getId()  );
      buttons.add( button );
      button.addSelectionListener( new SelectionAdapter() {
        @Override public void widgetSelected( SelectionEvent selectionEvent ) {
          final boolean selected = ( (Button) selectionEvent.widget ).getSelection();
          new Thread( new Runnable() {
            @Override public void run() {
              final Future<Boolean> future = ( selected ) ? capability.install() : capability.uninstall();
              try {
                final Boolean successful = future.get();
                display.asyncExec( new Runnable() {
                  @Override public void run() {
                    button.setSelection( successful );
                    if ( !successful ) {
                      MessageDialog dialog = new MessageDialog( shell, "Capability Install Error", null,
                          "Error Installing Capability:\n\n" + capability.getId(), MessageDialog.ERROR,
                          new String[] { "OK" }, 0 );
                      dialog.open();
                    } else {
                      MessageDialog dialog = new MessageDialog( shell, "Capability Install Success", null,
                          capability.getId() + " " + ( ( !selected ) ? "un" : "" ) + "installed successfully", MessageDialog.INFORMATION,
                            new String[] { "OK" }, 0 );
                      dialog.open();
                    }
                    updateAllCheckboxes();
                  }
                } );
              } catch ( InterruptedException e ) {
                e.printStackTrace();
              } catch ( ExecutionException e ) {
                e.printStackTrace();
              }

            }
          } ).run();

        }
      } );

    }
    mainPanel.setSize( mainPanel.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
    scrollpane.setMinSize( mainPanel.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
    BaseStepDialog.setSize( shell, 250, 400, false );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }
  private void updateAllCheckboxes() {
    DefaultCapabilityManager capabilityManager = DefaultCapabilityManager.getInstance();
    for ( Button button : buttons ) {
      button.setSelection( capabilityManager.getCapabilityById( button.getText() ).isInstalled() );
    }
  }
}
