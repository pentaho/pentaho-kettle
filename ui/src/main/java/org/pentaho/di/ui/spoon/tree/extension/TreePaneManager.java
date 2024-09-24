/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.ui.spoon.tree.extension;

import org.pentaho.di.core.exception.KettleException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;

/** Manages the panes on a tree tab */
public class TreePaneManager {

  private boolean acceptingExtensions = true;
  private final List<TreePaneExtension> extensions = new ArrayList<>();

  public void addPane( TreePaneExtension extension ) throws KettleException {
    if ( acceptingExtensions ) {
      extensions.add( extension );
    } else {
      throw new KettleException( new IllegalStateException( "Cannot add extensions after pane creation" ) );
    }
  }

  public void buildPanes( SashForm main ) {
    acceptingExtensions = false;
    int len = extensions.size();

    List<Composite> views =
        extensions.stream().map( ext -> new Composite( main, SWT.BORDER ) ).collect( Collectors.toList() );
    PaneManager paneMgr = new PaneManager( main );

    for ( int i = 0; i < len; i++ ) {
      boolean enabled = extensions.get( i ).createPane( views.get( i ), new ExpandController( paneMgr, i ) );
      if ( !enabled ) {
        paneMgr.hide( i );
      }
    }
  }

  private static class ExpandController implements TreePaneExtension.ExpandController {
    private final int itemIdx;
    private final PaneManager paneMgr;

    public ExpandController( PaneManager paner, int idx ) {
      this.itemIdx = idx;
      this.paneMgr = paner;
    }

    @Override
    public void show() {
      paneMgr.show( itemIdx );
    }

    @Override
    public void hide() {
      paneMgr.hide( itemIdx );
    }

  }

  /** Keeps consistent sizes as panes are shown/hidden */
  static class PaneManager {
    private final SashResizer resizer;
    private final SashForm form;
    private final int len;

    public PaneManager( SashForm form ) {
      this.form = form;
      int[] iniWeights = form.getWeights();
      this.resizer = new SashResizer( iniWeights );
      this.len = iniWeights.length;
      if ( len == 1 ) {
        form.setMaximizedControl( form.getChildren()[0] );
      }
    }

    public void show( int idx ) {
      int[] weights = resizer.enable( idx, form.getWeights() );
      applyWeights( weights, form );
    }

    public void hide( int idx ) {
      int[] weights = resizer.disable( idx, form.getWeights() );
      applyWeights( weights, form );
    }

    private static void applyWeights( int[] weights, SashForm form ) {
      for ( int i = 0; i < weights.length; i++ ) {
        if ( weights[i] == 100 ) {
          // better than having one at 100
          form.setMaximizedControl( form.getChildren()[i] );
          return;
        }
      }
      form.setMaximizedControl( null );
      form.setWeights( weights );
    }

  }
}
