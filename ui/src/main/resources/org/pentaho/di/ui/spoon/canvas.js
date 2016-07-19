//# sourceURL=canvas.js
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016-2018 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
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
"use strict";
var x1, x2, y1, y2;
var clicked = null;

var handleEvent = function( event ) {
  var mode = event.widget.getData( "mode" );
  var nodes = event.widget.getData( "nodes" );
  var hops = event.widget.getData( "hops" );
  var notes = event.widget.getData( "notes" );
  var props = event.widget.getData( "props" );
  var magnification = props.magnification;
  var gridsize = props.gridsize;
  var iconsize = props.iconsize;

  switch( event.type ) {
  case SWT.MouseDown:
    x1 = event.x / magnification;
    y1 = event.y / magnification;

    // Determine which node is clicked if any
    for ( var key in nodes ) {
      var node = nodes[key];
      if ( node.x <= x1 && x1 < node.x + iconsize
        && node.y <= y1 && y1 < node.y + iconsize ) {
        clicked = node;
      }
    }
    break;
  case SWT.MouseUp:
    clicked = null;
    break;
  case SWT.MouseMove:
    x2 = event.x / magnification;
    y2 = event.y / magnification;
    if ( mode == null ) {
      break;
    }
    if ( mode != "null" ) {
      event.widget.redraw();
    }
    break;
  case SWT.Paint:
    // Client-side does not redraw when first-drawing (null) and after mouseup ("null")
    if ( mode == null || mode == "null" ) {
      break;
    }
    var gc = event.gc;
    var dx = x2 - x1;
    var dy = y2 - y1;

    // Clear the canvas
    gc.clearRect( 0, 0, gc.canvas.width / magnification, gc.canvas.height / magnification );

    // Draw grids
    if ( gridsize > 1 ) {
      gc.fillStyle = 'black';
      gc.beginPath();
      gc.setLineDash( [ 1, gridsize - 1 ] );
      // vertical grid
      for ( var i = gridsize; i < gc.canvas.width / magnification; i += gridsize ) {
        gc.moveTo( i, 0 );
        gc.lineTo( i, gc.canvas.height / magnification );
      }
      // horizontal grid
      for ( var j = gridsize; j < gc.canvas.height / magnification; j += gridsize ) {
        gc.moveTo( 0, j );
        gc.lineTo( gc.canvas.width / magnification, j );
      }
      gc.stroke();
      gc.setLineDash( [] );
      gc.fillStyle = 'white';
    }

    // Draw hops
    hops.forEach( function ( hop ) {
      gc.beginPath();
      if ( mode == "drag" && nodes[hop.from].selected ) {
        gc.moveTo( snapToGrid( nodes[hop.from].x + dx, gridsize ) + iconsize / 2,
                   snapToGrid( nodes[hop.from].y + dy, gridsize ) + iconsize / 2 );
      } else {
        gc.moveTo( nodes[hop.from].x + iconsize / 2, nodes[hop.from].y + iconsize / 2 );
      }
      if ( mode == "drag" && nodes[hop.to].selected ) {
        gc.lineTo( snapToGrid( nodes[hop.to].x + dx, gridsize ) + iconsize / 2,
                   snapToGrid( nodes[hop.to].y + dy, gridsize ) + iconsize / 2 );
      } else {
        gc.lineTo( nodes[hop.to].x + iconsize / 2, nodes[hop.to].y + iconsize / 2 );
      }
      gc.stroke();
    } );

    for ( var key in nodes ) {
      var node = nodes[key];
      var x = node.x;
      var y = node.y;

      // Move selected nodes
      if ( mode == "drag" && ( node.selected || node == clicked ) ) {
        x = snapToGrid( node.x + dx, gridsize );
        y = snapToGrid( node.y + dy, gridsize );
      }
      // Draw a icon background
      gc.fillRect( x, y, iconsize, iconsize );

      // Draw node icon
      var img = new Image();
      img.src = 'rwt-resources/' + node.img;
      gc.drawImage( img, x, y, iconsize, iconsize );

      // Draw a bounding rectangle
      if ( node.selected || node == clicked ) {
        gc.lineWidth = 3;
        gc.strokeStyle = 'rgb(0, 93, 166)';
      } else {
        gc.strokeStyle = 'rgb(61, 99, 128)'; //colorCrystalTextPentaho
      }
      drawRoundRectangle( gc, x - 1, y - 1, iconsize + 1, iconsize + 1, 8, 8, false );
      gc.strokeStyle = 'black';
      gc.lineWidth = 1;

      // Draw node name
//      gc.fillStyle = 'black';
//      gc.fillText( key, x + iconsize / 2 - gc.measureText(key).width / 2, y + iconsize + 7 );
//      gc.fillStyle = 'white';
    }

    // Draw notes
    notes.forEach( function ( note ) {
      gc.beginPath();
      // margin = 10 see org.pentaho.di.core.gui.BasePainter.drawNote(NotePadMeta)
      if ( mode == "drag" && note.selected ) {
        gc.rect( snapToGrid( note.x + dx, gridsize), snapToGrid( note.y + dy, gridsize), note.width + 10, note.height + 10 );
      } else {
        gc.rect( note.x, note.y, note.width + 10, note.height + 10 );
      }
      gc.stroke();
    } );

    // Draw a new hop
    if ( mode == "hop" && clicked ) {
      gc.beginPath();
      gc.moveTo( clicked.x + iconsize / 2, clicked.y + iconsize / 2 );
      gc.lineTo( x2, y2 );
      gc.stroke();
    }

    // Draw a selection rectangle
    if ( mode == "select" ) {
      gc.beginPath();
      gc.rect( x1, y1, dx, dy );
      gc.stroke();
    }
    break;
  }
};

function snapToGrid( x, gridsize ) {
  return gridsize * Math.floor( x / gridsize );
}

/*
 * Port of GCOperationWriter#drawRoundRectangle
 */
function drawRoundRectangle( gc, x, y, w, h, arcWidth, arcHeight, fill ) {
  var offset = 0;
  if( !fill && gc.lineWidth % 2 != 0 ) {
    offset = 0.5;
  }
  x = x + offset;
  y = y + offset;
  var rx = arcWidth / 2 + 1;
  var ry = arcHeight / 2 + 1;
  gc.beginPath();
  gc.moveTo( x, y + ry );
  gc.lineTo( x, y + h - ry );
  gc.quadraticCurveTo( x, y + h, x + rx, y + h );
  gc.lineTo( x + w - rx, y + h );
  gc.quadraticCurveTo( x + w, y + h, x + w, y + h - ry );
  gc.lineTo( x + w, y + ry );
  gc.quadraticCurveTo( x + w, y, x + w - rx, y );
  gc.lineTo( x + rx, y );
  gc.quadraticCurveTo( x, y, x, y + ry );
  if ( fill ) {
    gc.fill();
  } else {
    gc.stroke();
  }
}