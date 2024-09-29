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

package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Created by bmorrise on 2/6/17.
 */
public class ColumnsResizer implements Listener {
  private int[] weights;
  private boolean resizing;

  public ColumnsResizer( int... weights ) {
    this.weights = weights;
  }

  @Override
  public void handleEvent( Event event ) {
    Table table = (Table) event.widget;
    applyWeigths( table );
  }

  public void addColumnResizeListeners( Table table ) {
    TableColumn[] columns = table.getColumns();
    int len = Math.min( weights.length, columns.length );
    for ( int i = 0; i < len - 1; i++ ) {
      if ( weights[i] > 0 ) {
        columns[i].addListener( SWT.Resize, getColumnResizeListener( i ) );
      }
    }
  }

  private Listener getColumnResizeListener( final int colIndex ) {
    return new Listener() {
      private int colIdx = colIndex;

      @Override
      public void handleEvent( Event event ) {
        if ( resizing ) {
          return;
        }

        TableColumn column = (TableColumn) event.widget;
        Table table = column.getParent();
        TableColumn[] columns = table.getColumns();
        int firstWidth = 0, restWidth = 0;
        int len = Math.min( weights.length, columns.length );
        for ( int i = 0; i <= colIdx; i++ ) {
          firstWidth += columns[i].getWidth();
        }
        float restWeightsBefore = 0;
        for ( int i = colIdx + 1; i < len; i++ ) {
          restWeightsBefore += weights[i];
          restWidth += columns[i].getWidth();
        }
        int tableWidth = getTableWidth( table );

        final int minWeight = 4;
        for ( int i = 0; i <= colIdx; i++ ) {
          if ( weights[i] > 0 ) {
            weights[i] = columns[i].getWidth();
          }
        }
        int columnsWidth = firstWidth + restWidth;
        int shortening = columnsWidth - tableWidth;
        float newRestWidth = restWidth - shortening;
        for ( int i = colIdx + 1; i < len; i++ ) {
          if ( weights[i] > 0 ) {
            float w = weights[i];
            w = w / restWeightsBefore * newRestWidth;
            weights[i] = Math.max( Math.round( w ), minWeight );
          }
        }
        applyWeigths( table );
      }
    };
  }

  protected void applyWeigths( Table table ) {
    if ( resizing ) {
      return;
    }
    float width = getTableWidth( table );

    TableColumn[] columns = table.getColumns();

    int f = 0;
    for ( int w : weights ) {
      f += w;
    }
    int len = Math.min( weights.length, columns.length );
    resizing = true;
    for ( int i = 0; i < len; i++ ) {
      int cw = weights[ i ] == 0 ? 0 : Math.round( width / f * weights[ i ] );
      width -= cw + 1;
      columns[ i ].setWidth( cw );
      f -= weights[ i ];
    }
    resizing = false;
  }

  protected int getTableWidth( Table table ) {
    int width = table.getSize().x - 2;
    if ( table.getVerticalBar() != null && table.getVerticalBar().isVisible() ) {
      width -= table.getVerticalBar().getSize().x;
    }
    return width;
  }

}
