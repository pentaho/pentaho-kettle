/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Pentaho : http://www.pentaho.com
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

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Created by bmorrise on 2/6/17.
 */
public class ColumnsResizer implements Listener {
  private int[] weights;

  public ColumnsResizer( int... weights ) {
    this.weights = weights;
  }

  @Override
  public void handleEvent( Event event ) {
    Table table = (Table) event.widget;
    float width = table.getSize().x - 2;
    TableColumn[] columns = table.getColumns();

    int f = 0;
    for ( int w : weights ) {
      f += w;
    }
    int len = Math.min( weights.length, columns.length );
    for ( int i = 0; i < len; i++ ) {
      int cw = weights[ i ] == 0 ? 0 : Math.round( width / f * weights[ i ] );
      width -= cw + 1;
      columns[ i ].setWidth( cw );
      f -= weights[ i ];
    }
  }
}
