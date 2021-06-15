/*******************************************************************************
 * Copyright (c) 2012, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function(){

rwt.define( "rwt.scripting", {} );

/**
 * Objects of this type are given to the handleEvent function (JavaScript) of
 * org.eclipse.rap.rwt.scripting.ClientListener (Java) instances.
 *
 * @private
 * @class RWT Scripting analoge to org.eclipse.swt.widgets.Event
 * @exports rwt.scripting.EventProxy as Event
 * @since 2.2
 * @see SWT
 */
// TODO [rst] Define directly using rwt.define, remove surrounding function scope
rwt.scripting.EventProxy = function( eventType, originalTarget, originalEvent ) {
  this.widget = rwt.scripting.WidgetProxyFactory.getWidgetProxy( originalTarget );
  this.type = eventType;
  switch( eventType ) {
    case SWT.KeyDown:
    case SWT.KeyUp:
      initKeyEvent( this, originalEvent );
    break;
    case SWT.MouseDown:
    case SWT.MouseWheel:
    case SWT.MouseUp:
    case SWT.MouseMove:
    case SWT.MouseEnter:
    case SWT.MouseExit:
    case SWT.MouseDoubleClick:
      initMouseEvent( this, originalEvent );
    break;
    case SWT.Verify:
      initVerifyEvent( this, originalEvent );
    break;
    case SWT.Paint:
      initPaintEvent( this, originalTarget );
    break;
  }
};

rwt.scripting.EventProxy.prototype = {

  /**
   * an object representing the widget that issued the event.
   *
   * @see rap.getObject
   */
  widget : null,

  /**
   * depending on the event, a flag indicating whether the operation should be
   * allowed. Setting this field to false will cancel the operation.
   *
   * Effective on KeyDown, KeyUp and Verify.
   */
  doit : true,

  /**
   * depending on the event, the character represented by the key that was
   * typed. This is the final character that results after all modifiers have
   * been applied. For non-printable keys (like arrow-keys) this field is not set.
   * Changing its value has no effect.
   *
   * Set for KeyDown, KeyUp and Verify.
   */
  character : '\u0000',

  /**
   * depending on the event, the key code of the key that was typed, as defined
   * by the key code constants in class <code>SWT</code>. When the character
   * field of the event is ambiguous, this field contains the unaffected value
   * of the original character. For example, typing Shift+M or M result in different
   * characters ( 'M' and 'm' ), but the same keyCode (109, character code for 'm').
   */
  keyCode : 0,

  /**
   * the type of event, as defined by the event type constants in the {@link SWT} object.
   */
  type : 0,

  /**
   * depending on the event, the state of the keyboard modifier keys and mouse
   * masks at the time the event was generated.
   *
   * Set for KeyDown, KeyUp, MouseDown, MouseUp, MouseMove, MouseEvnet, MouseExit and MouseDoubleClick.
   */
  stateMask : 0,

  /**
   * the button that was pressed or released; 1 for the first button, 2 for the
   * second button, and 3 for the third button, etc.
   *
   * Set for MouseDown, MouseUp, MouseMove, MouseEvnet, MouseExit and MouseDoubleClick.
   */
  button : 0,

  /**
   * x coordinate of the pointer at the time of the event
   *
   * Set for MouseDown, MouseUp, MouseMove, MouseEvnet, MouseExit and MouseDoubleClick.
   */
  x : 0,

  /**
   * y coordinate of the pointer at the time of the event
   *
   * Set for MouseDown, MouseUp, MouseMove, MouseEvnet, MouseExit and MouseDoubleClick.
   */
  y : 0,

  /**
   * depending on the event, the range of text being modified. Setting these
   * fields has no effect.
   *
   * Set for Verify.
   */
  start : 0,
  end : 0,

  /**
   * depending on the event, the new text that will be inserted.
   * Setting this field will change the text that is about to
   * be inserted or deleted.
   *
   * Set for Verify.
   */
  text : "",

  /**
   * the graphics context to use when painting.
   * <p>
   * It supports a subset of the <a href="http://www.w3.org/TR/2dcontext/">HTML5 Canvas API</a>.
   * </p>
   * Fields:
   * <ul>
   *  <li>strokeStyle</li>
   *  <li>fillStyle</li>
   *  <li>lineWidth</li>
   *  <li>lineJoin</li>
   *  <li>lineCap</li>
   *  <li>miterLimit</li>
   *  <li>globalAlpha</li>
   *</ul>
   * Methods:
   * <ul>
   *  <li>save</li>
   *  <li>restore</li>
   *  <li>beginPath</li>
   *  <li>closePath</li>
   *  <li>clearRect (Limitation: in IE 7/8 arguments are ignored, the entire canvas is cleared)</li>
   *  <li>stroke</li>
   *  <li>fill</li>
   *  <li>moveTo</li>
   *  <li>lineTo</li>
   *  <li>quadraticCurveTo</li>
   *  <li>bezierCurveTo</li>
   *  <li>rect</li>
   *  <li>arc</li>
   *  <li>drawImage</li>
   *  <li>createLinearGradient (Limitations: In IE 7/8, the gradient can be only be drawn either
   *                          vertically or horizontally. Calls to "addColorStop" must be in the
   *                          order of the offsets and can not overwrite previous colorsStops)</li>
   * </ul>
   *
   * More methods are supported on modern browser, but for IE 7/8 these are all.
   *
   * Set on Paint.
   */
  gc : null

};

rwt.scripting.EventProxy.disposeEventProxy = function( eventProxy ) {
  eventProxy.widget = null;
};

var WrapperHelper = function() {};

rwt.scripting.EventProxy.wrapAsProto = function( object ) {
  WrapperHelper.prototype = object;
  var result = new WrapperHelper();
  WrapperHelper.prototype = null;
  return result;
};

rwt.scripting.EventProxy.postProcessEvent = function( event, wrappedEvent, originalEvent ) {
  switch( event.type ) {
    case SWT.Verify:
      postProcessVerifyEvent( event, wrappedEvent, originalEvent );
    break;
    case SWT.MouseWheel:
      postProcessMouseWheelEvent( event, wrappedEvent, originalEvent );
    break;
    case SWT.KeyDown:
    case SWT.KeyUp:
      postProcessKeyEvent( event, wrappedEvent, originalEvent );
    break;
  }
};

function initKeyEvent( event, originalEvent ) {
  var charCode = originalEvent.getCharCode();
  if( charCode !== 0 ) {
    event.character = String.fromCharCode( charCode );
    // TODO [tb] : keyCode will be off when character is not a-z
    event.keyCode = event.character.toLowerCase().charCodeAt( 0 );
  } else {
    var keyCode = getLastKeyCode();
    switch( keyCode ) {
      case 16:
        event.keyCode = SWT.SHIFT;
      break;
      case 17:
        event.keyCode = SWT.CTRL;
      break;
      case 18:
        event.keyCode = SWT.ALT;
      break;
      case 224:
        event.keyCode = SWT.COMMAND;
      break;
      default:
        event.keyCode = keyCode;
      break;
    }
  }
  setStateMask( event, originalEvent );
}

function initMouseEvent( event, originalEvent ) {
  var target = originalEvent.getTarget()._getTargetNode();
  var offset = rwt.html.Location.get( target, "scroll" );
  event.x = originalEvent.getPageX() - offset.left;
  event.y = originalEvent.getPageY() - offset.top;
  if( originalEvent.isLeftButtonPressed() ) {
    event.button = 1;
  } else if( originalEvent.isRightButtonPressed() ) {
    event.button = 3;
  } if( originalEvent.isMiddleButtonPressed() ) {
    event.button = 2;
  }
  setStateMask( event, originalEvent );
}

function initPaintEvent( event, target ) {
  var gc = rwt.widgets.util.WidgetUtil.getGC( target );
  event.gc = gc.getNativeContext();
}

function initVerifyEvent( event, originalEvent ) {
  var text = originalEvent.getTarget();
  if( text instanceof rwt.widgets.base.BasicText ) {
    var keyCode = getLastKeyCode();
    var newValue = text.getComputedValue();
    var oldValue = text.getValue();
    var oldSelection = text.getSelection();
    var diff = getDiff( newValue, oldValue, oldSelection, keyCode );
    if(    diff[ 0 ].length === 1
        && diff[ 1 ] === diff[ 2 ]
        && diff[ 0 ] === originalEvent.getData()
    ) {
      event.keyCode = keyCode;
      event.character = diff[ 0 ];
    }
    event.text = diff[ 0 ];
    event.start = diff[ 1 ];
    event.end = diff[ 2 ];
  }
}

function getLastKeyCode() {
  // NOTE : While this is a private field, this mechanism must be integrated with
  // KeyEventSupport anyway to support the doit flag better.
  return rwt.remote.KeyEventSupport.getInstance()._currentKeyCode;
}

function getDiff( newValue, oldValue, oldSel, keyCode ) {
  var start;
  var end;
  var text;
  if( newValue.length >= oldValue.length || oldSel[ 0 ] !== oldSel[ 1 ] ) {
    start = oldSel[ 0 ];
    end = oldSel[ 1 ];
    text = newValue.slice( start, newValue.length - ( oldValue.length - oldSel[ 1 ] ) );
  } else {
    text = "";
    if(    oldSel[ 0 ] === oldSel[ 1 ]
        && keyCode === 8 // backspace
        && ( oldValue.length - 1 ) === newValue.length
    ) {
      start = oldSel[ 0 ] - 1;
      end = oldSel[ 0 ];
    } else {
      start = oldSel[ 0 ];
      end = start + oldValue.length - newValue.length;
    }
  }
  return [ text, start, end ];
}

function setStateMask( event, originalEvent ) {
  event.stateMask |= originalEvent.isShiftPressed() ? SWT.SHIFT : 0;
  event.stateMask |= originalEvent.isCtrlPressed() ? SWT.CTRL : 0;
  event.stateMask |= originalEvent.isAltPressed() ? SWT.ALT : 0;
  event.stateMask |= originalEvent.isMetaPressed() ? SWT.COMMAND : 0;
}

function postProcessVerifyEvent( event, wrappedEvent, originalEvent ) {
  var widget = originalEvent.getTarget();
  if( wrappedEvent.doit !== false ) {
    if( event.text !== wrappedEvent.text && event.text !== "" ) {
      // insert replacement text
      originalEvent.preventDefault();
      var currentText = widget.getValue();
      var textLeft = currentText.slice( 0, event.start );
      var textRight = currentText.slice( event.end, currentText.length );
      var carret = textLeft.length + wrappedEvent.text.length;
      widget.setValue( textLeft + wrappedEvent.text + textRight );
      widget.setSelection( [ carret, carret ] );
    }
  } else {
    // undo any change
    originalEvent.preventDefault();
    widget._renderValue();
    widget._renderSelection();
  }
}

function postProcessKeyEvent( event, wrappedEvent, originalEvent ) {
  if( wrappedEvent.doit === false ) {
    originalEvent.preventDefault();
  }
}

function postProcessMouseWheelEvent( event, wrappedEvent, originalEvent ) {
  if( wrappedEvent.doit === false ) {
    originalEvent.preventDefault();
  }
}

}());
