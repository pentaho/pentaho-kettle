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


package org.pentaho.di.ui.core.dialog;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.SwtUniversalImage;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.shared.SharedObjectsIO.SharedObjectType;
import org.pentaho.di.shared.SharedObjectUtil.ComparedState;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.util.Map;
import java.util.TreeMap;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class ChangedSharedObjectsDialog {
  private static Class<?> PKG = ChangedSharedObjectsDialog.class; // for i18n purposes, needed by Translator2!!

  private static final PropsUI props = PropsUI.getInstance();

  private Shell parentShell;
  private Shell shell;
  private Display display;
  private GUIResource guiResource;

  private Map<SharedObjectType, Map<String, ComparedState>> changes;
  private boolean retval;

  public ChangedSharedObjectsDialog( Shell parentShell, GUIResource guiResource,
    Map<SharedObjectType, Map<String, ComparedState>> changes ) {
    this.parentShell = parentShell;
    this.display = parentShell.getDisplay();
    this.guiResource = guiResource;
    this.changes = changes;
  }

  public boolean open() {
    shell = new Shell( parentShell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    shell.setImage( GUIResource.getInstance().getImageLogoSmall() );
    shell.setText( BaseMessages.getString( PKG, "ChangedSharedObjectsDialog.Shell.Title" ) );

    int margin = Const.MARGIN;

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;
    shell.setLayout( formLayout );

    Button wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.Continue" ) );
    wOK.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        ok();
      }
    } );

    Button wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    wCancel.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        cancel();
      }
    } );

    BaseStepDialog.positionBottomButtons( shell, new Button[] { wOK, wCancel }, margin,
      BaseStepDialog.BUTTON_ALIGNMENT_RIGHT, null );

    Label lMessage1 = new Label( shell, SWT.WRAP );
    lMessage1.setText( BaseMessages.getString( PKG, "ChangedSharedObjectsDialog.Message1.Label" ) );
    props.setLook( lMessage1 );
    // 385 matches the text layout on the mockup from the UX team on one platform. The intention was to have both
    // labels be 2 lines. This helps set the initial size of the dialog, but everything can be resized based on
    // other contents and user resizing.
    lMessage1.setLayoutData(
      new FormDataBuilder().left( 0, margin ).top( 0, margin * 2 ).right( 100, -margin ).width( 385 ).result() );

    Label lMessage2 = new Label( shell, SWT.WRAP );
    lMessage2.setText( BaseMessages.getString( PKG, "ChangedSharedObjectsDialog.Message2.Label" ) );
    lMessage2.setFont( JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT ) );
    props.setLook( lMessage2 );
    lMessage2.setLayoutData(
      new FormDataBuilder().left( 0, margin ).top( lMessage1, margin * 2 ).right( 100, -margin ).width( 385 ).result() );

    Composite viewTreeComposite = new Composite( shell, SWT.NONE );
    viewTreeComposite.setLayout( new FillLayout() );

    FormData fdViewTreeComposite = new FormData();
    fdViewTreeComposite.left = new FormAttachment( 0, margin );
    fdViewTreeComposite.top = new FormAttachment( lMessage2, margin * 2 );
    fdViewTreeComposite.right = new FormAttachment( 100, -margin );
    fdViewTreeComposite.bottom = new FormAttachment( wOK, -margin *2 );
    viewTreeComposite.setLayoutData( fdViewTreeComposite );

    Tree selectionTree = new Tree( viewTreeComposite, SWT.SINGLE | SWT.BORDER );
    props.setLook( selectionTree );
    selectionTree.setLayout( new FillLayout() );
    populateTree( selectionTree );
    selectionTree.addListener( SWT.MeasureItem, new Listener() {
        @Override
        public void handleEvent( Event event ) {
          TreeItem item = (TreeItem) event.item;
          SwtUniversalImage svtImage = (SwtUniversalImage) item.getData();
          // this is not the same height that we use in PaintItem, below. Calling getItemHeight() calls this method,
          // though, so we can't use that. So this is basically an estimate. We may need to pad this at some point if
          // the estimate is wildly different from the resulting item height. in testing, we have 21 here and 26 below.
          // margin is close to that difference, so use that in PaintItem (use it anyway for buffer)
          int height = event.height;
          if ( height > 0 && svtImage != null ) {
            Image image = scaleToHeight( svtImage, height );
            event.width = event.width + image.getBounds().width + margin;
          }
        }
      } );
    selectionTree.addListener( SWT.PaintItem, new Listener() {
        @Override
        public void handleEvent( Event event ) {
          TreeItem item = (TreeItem) event.item;
          SwtUniversalImage svtImage = (SwtUniversalImage) item.getData();
          if ( svtImage != null ) {
            // allow space between images
            int height = selectionTree.getItemHeight() - margin;
            Image image = scaleToHeight( svtImage, height );
            event.gc.drawImage( image, event.x + event.width + margin, event.y );
          }
        }
      } );

    viewTreeComposite.layout();

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    BaseStepDialog.setSize( shell );

    shell.open();

    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }

    return retval;


  }

  private void populateTree( Tree tree ) {
    populateSubTree( tree, BaseMessages.getString( Spoon.class, "Spoon.STRING_CONNECTIONS" ),
      guiResource.getImageConnectionTree(), this.changes.get( SharedObjectType.CONNECTION ) );
    populateSubTree( tree, BaseMessages.getString( Spoon.class, "Spoon.STRING_CLUSTERS" ),
      guiResource.getImageClusterMedium(), this.changes.get( SharedObjectType.CLUSTERSCHEMA ) );
    populateSubTree( tree, BaseMessages.getString( Spoon.class, "Spoon.STRING_PARTITIONS" ),
      guiResource.getImagePartitionSchema(), this.changes.get( SharedObjectType.PARTITIONSCHEMA ) );
    populateSubTree( tree, BaseMessages.getString( Spoon.class, "Spoon.STRING_SLAVES" ),
      guiResource.getImageSlaveTree(), this.changes.get( SharedObjectType.SLAVESERVER ) );
  }

  private void populateSubTree( Tree tree, String groupName, Image childImage, Map<String, ComparedState> changes ) {
    if ( changes != null && !changes.isEmpty() ) {
      TreeItem root = new TreeItem( tree, SWT.NONE );
      root.setText( groupName );
      root.setImage( guiResource.getImageConfigurations() );
      root.setExpanded( true );

      // sort.
      Map<String, ComparedState> sorted = new TreeMap<>( String.CASE_INSENSITIVE_ORDER );
      sorted.putAll( changes );
      for ( Map.Entry<String, ComparedState> entry : sorted.entrySet() ) {
        TreeItem child = new TreeItem( root, SWT.NONE );
        String level = "File"; // TODO get from i18n
        // NOTE: no image for "new" items
        if ( entry.getValue() == ComparedState.MODIFIED ) {
          child.setData( guiResource.getSwtImageSharedOverwrite() );
        } else if ( entry.getValue() == ComparedState.UNMODIFIED ) {
          child.setData( guiResource.getSwtImageSharedIdentical() );
        }
        child.setText( entry.getKey() + " [" + level + "]" );
        child.setImage( childImage );
      }
    }
  }

  private Image scaleToHeight( SwtUniversalImage orig, int newHeight ) {
    // Scale to fit in height. Let width be proportional.
    // note that SwtUniversalImage does caching, so calling this repetitively should be pretty cheap.
    Image origImage = orig.getAsBitmap( display );
    int newWidth = ( origImage.getImageData().width * newHeight ) / origImage.getImageData().height;
    return orig.getAsBitmapForSize( display, newWidth, newHeight );
  }

  public void dispose() {
    WindowProperty winprop = new WindowProperty( shell );
    props.setScreen( winprop );

    shell.dispose();
  }

  public void ok() {
    retval = true;

    dispose();
  }

  public void cancel() {
    retval = false;
    dispose();
  }

}
