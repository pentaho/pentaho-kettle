/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.trans;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.logging.MetricsPainter;
import org.pentaho.di.core.logging.MetricsPainter.MetricsDrawArea;
import org.pentaho.di.core.metrics.MetricsDuration;
import org.pentaho.di.core.metrics.MetricsUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.SWTGC;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;

public class TransMetricsDelegate extends SpoonDelegate {
  private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!

  // private static final LogWriter log = LogWriter.getInstance();

  private TransGraph transGraph;

  private CTabItem transMetricsTab;

  private Canvas canvas;
  private Image image;

  private ScrolledComposite sMetricsComposite;
  private Composite metricsComposite;
  private boolean emptyGraph;

  private List<MetricsDrawArea> drawAreas;

  /**
   * @param spoon
   * @param transGraph
   */
  public TransMetricsDelegate( Spoon spoon, TransGraph transGraph ) {
    super( spoon );
    this.transGraph = transGraph;
  }

  public void addTransMetrics() {
    // First, see if we need to add the extra view...
    //
    if ( transGraph.extraViewComposite == null || transGraph.extraViewComposite.isDisposed() ) {
      transGraph.addExtraView();
    } else {
      if ( transMetricsTab != null && !transMetricsTab.isDisposed() ) {
        // just set this one active and get out...
        //
        transGraph.extraViewTabFolder.setSelection( transMetricsTab );
        return;
      }
    }

    // Add a transMetricsTab : displays the metrics information in a graphical way...
    //
    transMetricsTab = new CTabItem( transGraph.extraViewTabFolder, SWT.NONE );
    transMetricsTab.setImage( GUIResource.getInstance().getImageGantt() );
    transMetricsTab.setText( BaseMessages.getString( PKG, "Spoon.TransGraph.MetricsTab.Name" ) );

    sMetricsComposite = new ScrolledComposite( transGraph.extraViewTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
    sMetricsComposite.setLayout( new FillLayout() );

    // Create a composite, slam everything on there like it was in the history tab.
    //
    metricsComposite = new Composite( sMetricsComposite, SWT.NONE );
    metricsComposite.setBackground( GUIResource.getInstance().getColorBackground() );
    metricsComposite.setLayout( new FormLayout() );

    spoon.props.setLook( metricsComposite );

    setupContent();

    sMetricsComposite.setContent( metricsComposite );
    sMetricsComposite.setExpandHorizontal( true );
    sMetricsComposite.setExpandVertical( true );
    sMetricsComposite.setMinWidth( 800 );
    sMetricsComposite.setMinHeight( 400 );

    transMetricsTab.setControl( sMetricsComposite );

    transGraph.extraViewTabFolder.setSelection( transMetricsTab );

    transGraph.extraViewTabFolder.addSelectionListener( new SelectionAdapter() {

      public void widgetSelected( SelectionEvent arg0 ) {
        layoutMetricsComposite();
        updateGraph();
      }
    } );
  }

  public void setupContent() {

    if ( metricsComposite.isDisposed() ) {
      return;
    }

    // Remove anything on the perf composite, like an empty page message
    //
    for ( Control control : metricsComposite.getChildren() ) {
      if ( !control.isDisposed() ) {
        control.dispose();
      }
    }

    emptyGraph = false;

    canvas = new Canvas( metricsComposite, SWT.NONE );
    spoon.props.setLook( canvas );
    FormData fdCanvas = new FormData();
    fdCanvas.left = new FormAttachment( 0, 0 );
    fdCanvas.right = new FormAttachment( 100, 0 );
    fdCanvas.top = new FormAttachment( 0, 0 );
    fdCanvas.bottom = new FormAttachment( 100, 0 );
    canvas.setLayoutData( fdCanvas );

    metricsComposite.addControlListener( new ControlAdapter() {
      public void controlResized( ControlEvent event ) {
        updateGraph();
      }
    } );

    metricsComposite.addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent event ) {
        if ( image != null ) {
          image.dispose();
        }
      }
    } );

    canvas.addPaintListener( new PaintListener() {

      public void paintControl( final PaintEvent event ) {
        if ( transGraph.trans != null && transGraph.trans.isFinished() ) {
          refreshImage( event.gc );

          if ( !Const.isRunningOnWebspoonMode() && image != null && !image.isDisposed() ) {
            event.gc.drawImage( image, 0, 0 );
          }
        } else {
          Rectangle bounds = canvas.getBounds();
          if ( bounds.width <= 0 || bounds.height <= 0 ) {
            return;
          }

          event.gc.setForeground( GUIResource.getInstance().getColorWhite() );
          event.gc.setBackground( GUIResource.getInstance().getColorWhite() );
          event.gc.fillRectangle( new Rectangle( 0, 0, bounds.width, bounds.height ) );
          event.gc.setForeground( GUIResource.getInstance().getColorBlack() );
          String metricsMessage =
            BaseMessages.getString( PKG, "TransMetricsDelegate.TransformationIsNotRunning.Message" );
          org.eclipse.swt.graphics.Point extent = event.gc.textExtent( metricsMessage );
          event.gc.drawText( metricsMessage, ( bounds.width - extent.x ) / 2, ( bounds.height - extent.y ) / 2 );
        }
      }
    } );

    // Refresh automatically every 5 seconds as well.
    //
    final Timer timer = new Timer( "TransMetricsDelegate Timer" );
    timer.schedule( new TimerTask() {
      public void run() {
        updateGraph();
      }
    }, 0, 5000 );

    // When the tab is closed, we remove the update timer
    //
    transMetricsTab.addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent arg0 ) {
        timer.cancel();
      }
    } );

    if ( Const.isRunningOnWebspoonMode() ) {
      // When the browser tab/window is closed, we remove the update timer
      transGraph.getDisplay().disposeExec( new Runnable() {
        @Override
        public void run() {
          timer.cancel();
        }
      } );
    }

    // Show tool tips with details...
    //
    canvas.addMouseListener( new MouseAdapter() {
      @Override
      public void mouseDown( MouseEvent event ) {
        if ( drawAreas == null ) {
          return;
        }

        for ( int i = drawAreas.size() - 1; i >= 0; i-- ) {
          MetricsDrawArea drawArea = drawAreas.get( i );
          if ( drawArea.getArea().contains( event.x, event.y ) ) {
            MetricsDuration duration = drawArea.getDuration();
            if ( duration == null ) {
              continue;
            }
            System.out.println( duration.toString() );
            LoggingObjectInterface loggingObject =
              LoggingRegistry.getInstance().getLoggingObject( duration.getLogChannelId() );
            if ( loggingObject == null ) {
              continue;
            }

            System.out.println( loggingObject.getObjectType() + " : " + loggingObject.getObjectName() );
          }
        }
      }
    } );

    /*
     * canvas.addControlListener(new ControlAdapter() {
     *
     * @Override public void controlResized(ControlEvent arg0) { lastRefreshTime=0; // force a refresh } });
     */
  }

  public void showMetricsView() {
    // What button?
    //
    // XulToolbarButton showLogXulButton =
    // toolbar.getButtonById("trans-show-log");
    // ToolItem toolBarButton = (ToolItem) showLogXulButton.getNativeObject();

    if ( transMetricsTab == null || transMetricsTab.isDisposed() ) {
      addTransMetrics();
    } else {
      transMetricsTab.dispose();

      transGraph.checkEmptyExtraView();
    }
  }

  public void updateGraph() {
    if ( Const.isRunningOnWebspoonMode() && transGraph.getDisplay().isDisposed() ) {
      return;
    }

    transGraph.getDisplay().asyncExec( new Runnable() {
      public void run() {

        if ( metricsComposite != null
          && !metricsComposite.isDisposed() && canvas != null && !canvas.isDisposed() && transMetricsTab != null
          && !transMetricsTab.isDisposed() ) {
          if ( transMetricsTab.isShowing() ) {
            canvas.redraw();
          }
        }
      }
    } );
  }

  private long lastRefreshTime;

  public void resetLastRefreshTime() {
    lastRefreshTime = 0L;
  }

  private void refreshImage( GC canvasGc ) {
    List<MetricsDuration> durations = MetricsUtil.getAllDurations( transGraph.trans.getLogChannelId() );
    if ( Utils.isEmpty( durations ) ) {
      // In case of an empty durations or null there is nothing to draw
      return;
    }

    // Sort the metrics.
    Collections.sort( durations, new Comparator<MetricsDuration>() {
      @Override
      public int compare( MetricsDuration o1, MetricsDuration o2 ) {
        return o1.getDate().compareTo( o2.getDate() );
      }
    } );

    Rectangle bounds = canvas.getBounds();
    if ( bounds.width <= 0 || bounds.height <= 0 ) {
      return;
    }

    if ( transGraph.trans == null ) {
      image = null;
      return;
    }

    // For performance reasons, only ever refresh this image at most every 5 seconds...
    //
    if ( image != null && ( System.currentTimeMillis() - lastRefreshTime ) < 5000 ) {
      return;
    }
    lastRefreshTime = System.currentTimeMillis();

    if ( image != null ) {
      image.dispose(); // prevent out of memory...
      image = null;
    }

    // Correct size of canvas.
    org.eclipse.swt.graphics.Point textExtent = canvasGc.textExtent( "AagKkiw" );
    int height = textExtent.y + 8;

    // Make the height larger if needed for clarify
    //
    bounds.height = Math.max( durations.size() * height, bounds.height );
    canvas.setSize( bounds.width, bounds.height );

    SWTGC gc;
    if ( Const.isRunningOnWebspoonMode() ) {
      gc = new SWTGC( canvasGc, new Point( bounds.width, bounds.height ), PropsUI.getInstance().getIconSize() );
    } else {
      gc = new SWTGC( Display.getCurrent(), new Point( bounds.width, bounds.height ), PropsUI.getInstance().getIconSize() );
    }
    MetricsPainter painter = new MetricsPainter( gc, height );
    drawAreas = painter.paint( durations );
    if ( !Const.isRunningOnWebspoonMode() ) {
      image = (Image) gc.getImage();
    }

    // refresh the scrolled composite
    //
    sMetricsComposite.setMinHeight( bounds.height );
    sMetricsComposite.setMinWidth( bounds.width );
    sMetricsComposite.layout( true, true );

    // Draw the image on the canvas...
    //
    if ( !Const.isRunningOnWebspoonMode() ) {
      canvas.redraw();
    }

    // close shop on the SWT GC side.
    //
    gc.dispose();
  }

  /**
   * @return the trans Metrics Tab
   */
  public CTabItem getTransMetricsTab() {
    return transMetricsTab;
  }

  /**
   * @return the emptyGraph
   */
  public boolean isEmptyGraph() {
    return emptyGraph;
  }

  public void layoutMetricsComposite() {
    if ( !metricsComposite.isDisposed() ) {
      metricsComposite.layout( true, true );
    }
  }

}
