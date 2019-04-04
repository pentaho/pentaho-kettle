/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.gui;

import java.util.List;

import org.pentaho.di.core.AddUndoPositionInterface;

public class SnapAllignDistribute {
  private List<? extends GUIPositionInterface> elements;
  private AddUndoPositionInterface addUndoPositionInterface;
  private int[] indices;
  private Redrawable redrawable;
  private UndoInterface undoInterface;

  public SnapAllignDistribute( UndoInterface undoInterface, List<? extends GUIPositionInterface> elements,
    int[] indices, AddUndoPositionInterface addUndoPositionInterface, Redrawable redrawable ) {
    this.undoInterface = undoInterface;
    this.elements = elements;
    this.indices = indices;
    this.addUndoPositionInterface = addUndoPositionInterface;
    this.redrawable = redrawable;
  }

  public void snaptogrid( int size ) {
    if ( elements.isEmpty() ) {
      return;
    }

    // First look for the minimum x coordinate...

    GUIPositionInterface[] elemArray = new GUIPositionInterface[elements.size()];

    Point[] before = new Point[elements.size()];
    Point[] after = new Point[elements.size()];

    for ( int i = 0; i < elements.size(); i++ ) {
      GUIPositionInterface positionInterface = elements.get( i );

      elemArray[i] = positionInterface;
      Point p = positionInterface.getLocation();
      before[i] = new Point( p.x, p.y );

      // What's the modulus ?
      int dx = p.x % size;
      int dy = p.y % size;

      // Correct the location to the nearest grid line!
      // This means for size = 10
      // x = 3: dx=3, dx<=5 --> x=3-3 = 0;
      // x = 7: dx=7, dx> 5 --> x=3+10-3 = 10;
      // x = 10: dx=0, dx<=5 --> x=10-0 = 10;

      if ( dx > size / 2 ) {
        p.x += size - dx;
      } else {
        p.x -= dx;
      }
      if ( dy > size / 2 ) {
        p.y += size - dy;
      } else {
        p.y -= dy;
      }
      after[i] = new Point( p.x, p.y );
    }

    if ( addUndoPositionInterface != null ) {
      addUndoPositionInterface.addUndoPosition( undoInterface, elemArray, indices, before, after );
    }
    redrawable.redraw();
  }

  public void allignleft() {
    if ( elements.isEmpty() ) {
      return;
    }

    GUIPositionInterface[] elemArray = elements.toArray( new GUIPositionInterface[elements.size()] );

    Point[] before = new Point[elements.size()];
    Point[] after = new Point[elements.size()];

    int min = 99999;

    // First look for the minimum x coordinate...
    for ( int i = 0; i < elements.size(); i++ ) {
      GUIPositionInterface element = elements.get( i );
      Point p = element.getLocation();
      if ( p.x < min ) {
        min = p.x;
      }
    }

    // Then apply the coordinate...
    for ( int i = 0; i < elements.size(); i++ ) {
      GUIPositionInterface element = elements.get( i );

      Point p = element.getLocation();
      before[i] = new Point( p.x, p.y );
      element.setLocation( min, p.y );
      after[i] = new Point( min, p.y );
    }

    if ( addUndoPositionInterface != null ) {
      addUndoPositionInterface.addUndoPosition( undoInterface, elemArray, indices, before, after );
    }
    redrawable.redraw();
  }

  public void allignright() {
    if ( elements.isEmpty() ) {
      return;
    }

    GUIPositionInterface[] elemArray = elements.toArray( new GUIPositionInterface[elements.size()] );

    Point[] before = new Point[elements.size()];
    Point[] after = new Point[elements.size()];

    int max = -99999;

    // First look for the maximum x coordinate...
    for ( int i = 0; i < elements.size(); i++ ) {
      GUIPositionInterface element = elements.get( i );

      Point p = element.getLocation();
      if ( p.x > max ) {
        max = p.x;
      }
    }
    // Then apply the coordinate...
    for ( int i = 0; i < elements.size(); i++ ) {
      GUIPositionInterface stepMeta = elements.get( i );

      Point p = stepMeta.getLocation();
      before[i] = new Point( p.x, p.y );
      stepMeta.setLocation( max, p.y );
      after[i] = new Point( max, p.y );
    }

    if ( addUndoPositionInterface != null ) {
      addUndoPositionInterface.addUndoPosition( undoInterface, elemArray, indices, before, after );
    }
    redrawable.redraw();
  }

  public void alligntop() {
    if ( elements.isEmpty() ) {
      return;
    }

    GUIPositionInterface[] elemArray = elements.toArray( new GUIPositionInterface[elements.size()] );

    Point[] before = new Point[elements.size()];
    Point[] after = new Point[elements.size()];

    int min = 99999;

    // First look for the minimum y coordinate...
    for ( int i = 0; i < elements.size(); i++ ) {
      GUIPositionInterface element = elements.get( i );
      Point p = element.getLocation();
      if ( p.y < min ) {
        min = p.y;
      }
    }
    // Then apply the coordinate...
    for ( int i = 0; i < elements.size(); i++ ) {
      GUIPositionInterface element = elements.get( i );

      Point p = element.getLocation();
      before[i] = new Point( p.x, p.y );
      element.setLocation( p.x, min );
      after[i] = new Point( p.x, min );
    }

    if ( addUndoPositionInterface != null ) {
      addUndoPositionInterface.addUndoPosition( undoInterface, elemArray, indices, before, after );
    }
    redrawable.redraw();
  }

  public void allignbottom() {
    if ( elements.isEmpty() ) {
      return;
    }

    GUIPositionInterface[] elemArray = elements.toArray( new GUIPositionInterface[elements.size()] );

    Point[] before = new Point[elements.size()];
    Point[] after = new Point[elements.size()];

    int max = -99999;

    // First look for the maximum y coordinate...
    for ( int i = 0; i < elements.size(); i++ ) {
      GUIPositionInterface element = elements.get( i );

      Point p = element.getLocation();
      if ( p.y > max ) {
        max = p.y;
      }
    }

    // Then apply the coordinate...
    for ( int i = 0; i < elements.size(); i++ ) {
      GUIPositionInterface element = elements.get( i );

      Point p = element.getLocation();
      before[i] = new Point( p.x, p.y );
      element.setLocation( p.x, max );
      after[i] = new Point( p.x, max );
    }

    if ( addUndoPositionInterface != null ) {
      addUndoPositionInterface.addUndoPosition( undoInterface, elemArray, indices, before, after );
    }
    redrawable.redraw();
  }

  public void distributehorizontal() {
    if ( elements.size() <= 1 ) {
      return;
    }

    GUIPositionInterface[] elemArray = elements.toArray( new GUIPositionInterface[elements.size()] );

    Point[] before = new Point[elements.size()];
    Point[] after = new Point[elements.size()];

    int min = 99999;
    int max = -99999;

    int[] order = new int[elements.size()];

    // First look for the minimum & maximum x coordinate...
    for ( int i = 0; i < elements.size(); i++ ) {
      GUIPositionInterface element = elements.get( i );

      Point p = element.getLocation();
      if ( p.x < min ) {
        min = p.x;
      }
      if ( p.x > max ) {
        max = p.x;
      }
      order[i] = i;
    }

    // Difficult to keep the steps in the correct order.
    // If you just set the x-coordinates, you get special effects.
    // Best is to keep the current order of things.
    // First build an arraylist and store the order there.
    // Then sort order[], based upon the coordinate of the step.
    for ( int i = 0; i < elements.size(); i++ ) {
      for ( int j = 0; j < elements.size() - 1; j++ ) {
        Point p1 = ( elements.get( order[j] ) ).getLocation();
        Point p2 = ( elements.get( order[j + 1] ) ).getLocation();
        if ( p1.x > p2.x ) { // swap

          int dummy = order[j];
          order[j] = order[j + 1];
          order[j + 1] = dummy;

          dummy = indices[j];
          indices[j] = indices[j + 1];
          indices[j + 1] = dummy;
        }
      }
    }

    // The distance between two steps becomes.
    int distance = ( max - min ) / ( elements.size() - 1 );

    for ( int i = 0; i < elements.size(); i++ ) {
      GUIPositionInterface element = elements.get( order[i] );

      Point p = element.getLocation();
      before[i] = new Point( p.x, p.y );
      p.x = min + ( i * distance );
      after[i] = new Point( p.x, p.y );
      elemArray[i] = element;
    }

    // Undo!
    if ( addUndoPositionInterface != null ) {
      addUndoPositionInterface.addUndoPosition( undoInterface, elemArray, indices, before, after );
    }
    redrawable.redraw();
  }

  public void distributevertical() {
    if ( elements.size() <= 1 ) {
      return;
    }

    GUIPositionInterface[] elemArray = elements.toArray( new GUIPositionInterface[elements.size()] );

    Point[] before = new Point[elements.size()];
    Point[] after = new Point[elements.size()];

    int min = 99999;
    int max = -99999;

    int[] order = new int[elements.size()];

    // First look for the minimum & maximum y coordinate...
    for ( int i = 0; i < elements.size(); i++ ) {
      GUIPositionInterface element = elements.get( i );

      Point p = element.getLocation();
      if ( p.y < min ) {
        min = p.y;
      }
      if ( p.y > max ) {
        max = p.y;
      }
      order[i] = i;
    }

    // Difficult to keep the steps in the correct order.
    // If you just set the y-coordinates, you get special effects.
    // Best is to keep the current order of things.
    // First build an arraylist and store the order there.
    // Then sort order[], based upon the coordinate of the step.
    for ( int i = 0; i < elements.size(); i++ ) {
      for ( int j = 0; j < elements.size() - 1; j++ ) {
        Point p1 = ( elements.get( order[j] ) ).getLocation();
        Point p2 = ( elements.get( order[j + 1] ) ).getLocation();
        if ( p1.y > p2.y ) { // swap

          int dummy = order[j];
          order[j] = order[j + 1];
          order[j + 1] = dummy;

          dummy = indices[j];
          indices[j] = indices[j + 1];
          indices[j + 1] = dummy;
        }
      }
    }

    // The distance between two steps becomes.
    int distance = ( max - min ) / ( elements.size() - 1 );

    for ( int i = 0; i < elements.size(); i++ ) {
      GUIPositionInterface element = elements.get( order[i] );

      Point p = element.getLocation();
      before[i] = new Point( p.x, p.y );
      p.y = min + ( i * distance );
      after[i] = new Point( p.x, p.y );
      elemArray[i] = element;
    }

    // Undo!
    if ( addUndoPositionInterface != null ) {
      addUndoPositionInterface.addUndoPosition( undoInterface, elemArray, indices, before, after );
    }
    redrawable.redraw();
  }
}
