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


package org.pentaho.di.ui.core.widget.tree;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;

/**
 * Created by bmorrise on 6/27/18.
 */
public class TreeToolbar extends Composite {

  private PropsUI props = PropsUI.getInstance();
  private Text selectionFilter;
  private ToolItem refreshButton;
  private ToolItem expandAll;
  private ToolItem collapseAll;

  public TreeToolbar( Composite composite, int i ) {
    this( composite, i, false );
  }
  
  public TreeToolbar( Composite composite, int i, boolean includeRefresh ) {
    super( composite, i );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 0;
    formLayout.marginHeight = 0;
    formLayout.marginTop = 0;
    formLayout.marginBottom = 0;

    this.setLayout( formLayout );

    Label sep3 = new Label( this, SWT.SEPARATOR | SWT.HORIZONTAL );
    sep3.setBackground( GUIResource.getInstance().getColorWhite() );
    FormData fdSep3 = new FormData();
    fdSep3.left = new FormAttachment( 0, 0 );
    fdSep3.right = new FormAttachment( 100, 0 );
    fdSep3.top = new FormAttachment( 0 );
    sep3.setLayoutData( fdSep3 );

    ToolBar treeTb = new ToolBar( this, SWT.HORIZONTAL | SWT.FLAT );
    props.setLook( treeTb, Props.WIDGET_STYLE_TOOLBAR );
    /*
    This contains a map with all the unnamed transformation (just a filename)
   */
    if ( includeRefresh ) {
      refreshButton = new ToolItem( treeTb, SWT.PUSH );
      refreshButton.setImage( GUIResource.getInstance().getImageRefreshEnabled() );
    }
    expandAll = new ToolItem( treeTb, SWT.PUSH );
    expandAll.setImage( GUIResource.getInstance().getImageExpandAll() );
    collapseAll = new ToolItem( treeTb, SWT.PUSH );
    collapseAll.setImage( GUIResource.getInstance().getImageCollapseAll() );

    FormData fdTreeToolbar = new FormData();
    if ( Const.isLinux() ) {
      fdTreeToolbar.top = new FormAttachment( sep3, 3 );
    } else {
      fdTreeToolbar.top = new FormAttachment( sep3, 5 );
    }
    fdTreeToolbar.right = new FormAttachment( 100, -10 );
    treeTb.setLayoutData( fdTreeToolbar );

    ToolBar selectionFilterTb = new ToolBar( this, SWT.HORIZONTAL | SWT.FLAT );
    props.setLook( selectionFilterTb, Props.WIDGET_STYLE_TOOLBAR );

    ToolItem clearSelectionFilter = new ToolItem( selectionFilterTb, SWT.PUSH );
    clearSelectionFilter.setImage( GUIResource.getInstance().getImageClearText() );
    clearSelectionFilter.setDisabledImage( GUIResource.getInstance().getImageClearTextDisabled() );

    FormData fdSelectionFilterToolbar = new FormData();
    if ( Const.isLinux() ) {
      fdSelectionFilterToolbar.top = new FormAttachment( sep3, 3 );
    } else {
      fdSelectionFilterToolbar.top = new FormAttachment( sep3, 5 );
    }
    fdSelectionFilterToolbar.right = new FormAttachment( treeTb, -20 );
    selectionFilterTb.setLayoutData( fdSelectionFilterToolbar );

    selectionFilter = new Text( this, SWT.SINGLE | SWT.BORDER | SWT.LEFT | SWT.SEARCH );
    FormData fdSelectionFilter = new FormData();
    int offset = -( GUIResource.getInstance().getImageClearTextDisabled().getBounds().height + 6 );
    if ( Const.isLinux() ) {
      offset = -( GUIResource.getInstance().getImageClearTextDisabled().getBounds().height + 13 );
    }

    fdSelectionFilter.top = new FormAttachment( selectionFilterTb, offset );
    fdSelectionFilter.right = new FormAttachment( selectionFilterTb, 0 );
    fdSelectionFilter.left = new FormAttachment( 0, 10 );
    selectionFilter.setLayoutData( fdSelectionFilter );

    clearSelectionFilter.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        selectionFilter.setText( "" );
      }
    } );

    clearSelectionFilter.setEnabled( !Utils.isEmpty( selectionFilter.getText() ) );

    selectionFilter.addModifyListener( modifyEvent -> {
      clearSelectionFilter.setEnabled( !Utils.isEmpty( selectionFilter.getText() ) );
    } );

    Label sep4 = new Label( this, SWT.SEPARATOR | SWT.HORIZONTAL );
    sep4.setBackground( GUIResource.getInstance().getColorWhite() );
    FormData fdSep4 = new FormData();
    fdSep4.left = new FormAttachment( 0, 0 );
    fdSep4.right = new FormAttachment( 100, 0 );
    fdSep4.top = new FormAttachment( treeTb, 5 );
    sep4.setLayoutData( fdSep4 );
  }

  public void setSearchTooltip( String tooltip ) {
    selectionFilter.setToolTipText( tooltip );
  }

  public void setSearchPlaceholder( String searchPlaceholder ) {
    selectionFilter.setMessage( searchPlaceholder );
  }

  public void addSearchModifyListener( ModifyListener modifyListener ) {
    selectionFilter.addModifyListener( modifyListener );
  }

  public void addRefreshListener( SelectionAdapter selectionAdapter ) {
    refreshButton.addSelectionListener( selectionAdapter );
  }

  public void addExpandAllListener( SelectionAdapter selectionAdapter ) {
    expandAll.addSelectionListener( selectionAdapter );
  }

  public void addCollapseAllListener( SelectionAdapter selectionAdapter ) {
    collapseAll.addSelectionListener( selectionAdapter );
  }

  public String getSearchText() {
    return selectionFilter.getText();
  }

  @Override
  public boolean setFocus() {
    return selectionFilter.setFocus();
  }

  public void clear() {
    selectionFilter.setText( "" );
  }
}
