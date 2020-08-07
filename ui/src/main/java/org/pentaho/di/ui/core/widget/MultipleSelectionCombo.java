/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2020 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.util.StringUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MultipleSelectionCombo extends Composite {
  private Text displayText = null;
  private String[] displayItems;
  private int[] comboSelection;
  private Shell floatShell = null;
  private List list = null;
  private String[] selectedItemLabels;
  private Composite bottomRow;
  private MouseAdapter exitAction;

  public MultipleSelectionCombo( Composite parent, int style ) {
    super( parent, style );
    comboSelection = new int[]{};
    selectedItemLabels = new String[]{};
    displayItems = new String[]{};
    init();
  }

  private void init() {
    GridLayout masterGridLayout = new GridLayout( 1, true );
    masterGridLayout.marginBottom = 0;
    masterGridLayout.marginTop = 0;
    masterGridLayout.verticalSpacing = 0;
    setLayout( masterGridLayout );
    Composite topRow = new Composite( this, SWT.NONE );
    topRow.setLayout( new GridLayout( 3, false ) );

    displayText = new Text( topRow, SWT.BORDER );
    GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
    gridData.minimumWidth = 200;
    displayText.setLayoutData( gridData );

    Button arrow = new Button( topRow, SWT.ARROW | SWT.DOWN );
    arrow.setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_BLUE ) );
    arrow.setSize( 25, 25 );

    arrow.addMouseListener( new MouseAdapter() {
      @Override
      public void mouseDown( MouseEvent event ) {
        super.mouseDown( event );
        if ( floatShell == null || floatShell.isDisposed() ) {
          closeOtherFloatShells();
          initFloatShell();
        } else {
          closeShellAndUpdate();
        }
      }
    } );

    Button add = new Button( topRow, SWT.PUSH );
    add.setText( "ADD" );

    bottomRow = new Composite( this, SWT.NONE );
    GridLayout gridLayout = new GridLayout( 2, true );
    gridLayout.marginBottom = 0;
    gridLayout.marginLeft = 0;
    gridLayout.marginRight = 0;
    gridLayout.marginTop = 0;
    bottomRow.setLayout( gridLayout );

    GridData rowGridData = new GridData( SWT.NONE );
    rowGridData.widthHint = 297;
    rowGridData.minimumWidth = 297;
    bottomRow.setLayoutData( rowGridData );

    exitAction = new MouseAdapter() {
      @Override
      public void mouseUp( MouseEvent e ) {
        super.mouseUp( e );

        String labelText = ((SelectionLabel) ((Label) e.widget).getParent()).getLabelText();
        addRemovedTagBackToListUI( labelText );

        selectedItemLabels = removeItemFromSelectedList( labelText );

        SelectionLabel removedItem = (SelectionLabel) ((Label) e.widget).getParent();

        Composite selectionArea = removedItem.getParent();
        int decreasedHeight = calculateTotalHeight( removedItem );
        removedItem.dispose();
        selectionArea.layout( true, true );
        int numRemainingItems = selectionArea.getChildren().length;
        if ( numRemainingItems % 2 == 0 ) {
          updateTagsUI( decreasedHeight );
        }
      }
    };

    add.addMouseListener( new MouseAdapter() {
      @Override
      public void mouseUp( MouseEvent e ) {
        super.mouseUp( e );
        if ( floatShell != null
                && comboSelection != null
                && comboSelection.length > 0 ) {
          Set<String> selectedItems = new HashSet<>( comboSelection.length );

          SelectionLabel ref = null;
          for ( int i = 0; i < comboSelection.length; i++ ) {
            ref = new SelectionLabel( bottomRow, SWT.BORDER, displayItems[comboSelection[i]], exitAction );
            selectedItems.add( displayItems[comboSelection[i]] );
          }

          //remove from display list
          displayItems = Arrays.stream( displayItems )
                  .filter( item -> !selectedItems.contains( item ) )
                  .toArray( String[]::new );

          selectedItemLabels = addToSelectedTags( selectedItems );
          if ( ref != null ) {
            updateTagsUI( calculateTotalHeight( ref ) );
          }

          comboSelection = new int[]{};
          floatShell.dispose();
        }
      }
    } );
  }

  private void closeOtherFloatShells() {
    Composite parent = this.getParent();

    while ( !( parent instanceof Shell ) ) {
      Arrays.stream( parent.getChildren() )
              .filter( c -> c instanceof MultipleSelectionCombo )
              .forEach( c -> ((MultipleSelectionCombo) c).triggerDropdownClose() );

      parent = parent.getParent();
    }
  }

  private void addRemovedTagBackToListUI( String labelText ) {
    String[] tempItems = new String[displayItems.length + 1];
    AtomicInteger idx = new AtomicInteger();
    Arrays.stream( displayItems )
            .forEach( str -> tempItems[idx.getAndIncrement()] = str );

    tempItems[tempItems.length - 1] = labelText;
    displayItems = tempItems;
    Arrays.sort( displayItems );
  }

  private String[] removeItemFromSelectedList( String labelText ) {
    String[] tempSelectedItems = new String[selectedItemLabels.length - 1];
    int tempIdx = 0;
    for ( int i = 0; i < selectedItemLabels.length; i++ ) {
      if ( !selectedItemLabels[i].equals( labelText ) ) {
        tempSelectedItems[tempIdx++] = selectedItemLabels[i];
      }
    }

    return tempSelectedItems;
  }

  private int calculateTotalHeight( SelectionLabel label ) {
    GridLayout layout = (GridLayout) label.getLayout();

    return layout.marginHeight + label.getHeight();
  }

  private void updateTagsUI( int height ) {
    int numRows = ( selectedItemLabels.length / 2 ) + ( selectedItemLabels.length % 2 );
    GridData newData = (GridData) bottomRow.getLayoutData();

    newData.minimumHeight = numRows * height;
    newData.heightHint = numRows * height;
    bottomRow.setLayoutData( newData );

    triggerShellResize();
  }

  private String[] addToSelectedTags( Set<String> selectedItems ) {
    if ( selectedItemLabels.length == 0 ) {
      selectedItemLabels = new String[selectedItems.size()];

      return selectedItems.toArray( selectedItemLabels );
    } else {
      String[] tempLabels = new String[selectedItemLabels.length + selectedItems.size()];
      int tempIdx = 0;
      for ( int i = 0; i < selectedItemLabels.length; i++ ) {
        tempLabels[tempIdx++] = selectedItemLabels[i];
      }

      String[] selectedAry = new String[selectedItems.size()];
      selectedItems.toArray( selectedAry );
      for ( int i = 0; i < selectedAry.length; i++ ) {
        tempLabels[tempIdx++] = selectedAry[i];
      }

      return tempLabels;
    }
  }

  private void triggerShellResize() {
    Composite scrollFinder = findScrollingParent();

    if ( scrollFinder != null ) {
      scrollFinder.layout( true, true );
      Point p = scrollFinder.getSize();
      final Point size = scrollFinder.computeSize( p.x, p.y, true );
      scrollFinder.setSize( size );
    }
  }

  private Composite findScrollingParent() {
    Composite finder = this.getParent();
    while ( finder != null && !( finder instanceof ScrolledComposite ) ) {
      finder = finder.getParent();
    }

    return finder;
  }

  private void initFloatShell() {
    Point p = displayText.getParent().toDisplay( displayText.getLocation() );
    Point size = displayText.getSize();
    Rectangle shellRect = new Rectangle( p.x, p.y + size.y, size.x, 0 );
    floatShell = new Shell( MultipleSelectionCombo.this.getShell(),
            SWT.NO_TRIM );

    GridLayout gl = new GridLayout();
    gl.marginBottom = 2;
    gl.marginTop = 2;
    gl.marginRight = 0;
    gl.marginLeft = 0;
    gl.marginWidth = 0;
    gl.marginHeight = 0;
    floatShell.setLayout( gl );

    list = new List( floatShell, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL
            | SWT.V_SCROLL );
    for ( String value : displayItems ) {
      list.add( value );
    }

    GridData gd = new GridData( GridData.FILL_BOTH );
    list.setLayoutData( gd );
    floatShell.setSize( shellRect.width, 100 );
    floatShell.setLocation( shellRect.x, shellRect.y );
    list.addMouseListener( new MouseAdapter() {
      @Override
      public void mouseUp( MouseEvent event ) {
        super.mouseUp( event );
        comboSelection = list.getSelectionIndices();
      }
    } );

    final Listener listener = event -> floatShell.dispose();

    Composite scroll = findScrollingParent();

    if ( scroll != null ) {
      scroll.getVerticalBar().addListener( SWT.Selection, listener );
    }

    this.getShell().addListener( SWT.Resize, listener );
    this.getShell().addListener( SWT.Move, listener );

    floatShell.open();
  }

  private void closeShellAndUpdate() {
    if ( floatShell != null && !floatShell.isDisposed() ) {
      comboSelection = list.getSelectionIndices();
      floatShell.dispose();
    }
  }

  private void bindDataToUI() {
    Set<String> selectedSet = new HashSet<>( selectedItemLabels.length );
    for ( String label : selectedItemLabels ) {
      new SelectionLabel( bottomRow, SWT.BORDER, label, exitAction );
      selectedSet.add( label );
    }

    displayItems = Arrays.stream( displayItems )
            .filter( item -> !selectedSet.contains( item ) )
            .toArray( String[]::new );

    triggerShellResize();
  }

  public void setItems( String[] items ) {
    Arrays.sort( items );
    this.displayItems = Arrays.stream( items ).toArray( String[]::new );
  }

  /**
   * Serializes all selected tags in comma separated list to be returned
   * and saved in the steps metadata
   *
   * @return comma separated string of all selected tags
   */
  public String getSelectedItems() {
    return String.join( ",", selectedItemLabels );
  }

  /**
   * Takes a comma separated string of tags and binds it to the data object
   * Then updates the UI for both the tag dropdown and the selected items
   *
   * @param selectedItems
   */
  public void setSelectedItems( String selectedItems ) {
    if ( !StringUtil.isEmpty( selectedItems ) ) {
      this.selectedItemLabels = selectedItems.split( "," );
      bindDataToUI();
    }
  }

  /**
   * Public interface for other dropdowns or components
   * to trigger open dropdowns to close
   */
  public void triggerDropdownClose() {
    if ( floatShell != null && !floatShell.isDisposed() ) {
      floatShell.dispose();
    }
  }

  /**
   * Simply a convenience interface to keep backward compatibility
   */
  @Deprecated
  public String getText() {
    return this.getSelectedItems();
  }

  /**
   * Simply a convenience interface to keep backward compatibility
   * @param selectedItems
   */
  @Deprecated
  public void setText( String selectedItems ) {
    this.setSelectedItems( selectedItems );
  }
}
