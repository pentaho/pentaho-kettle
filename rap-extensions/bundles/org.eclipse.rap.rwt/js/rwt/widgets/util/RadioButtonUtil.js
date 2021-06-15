/*******************************************************************************
 * Copyright (c) 2002, 2013 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipsevent.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/


/**
 * This class contains static functions for radio buttons
 */
rwt.qx.Class.define( "rwt.widgets.util.RadioButtonUtil", {

  statics : {

    registerExecute : function( button ) {
      button.addEventListener( "execute", this._onSelection, this );
    },

    registerKeypress : function( button ) {
      button.addEventListener( "keypress", this._onKeypress, this );
    },

    _onKeypress : function( event ) {
      var widget = event.getTarget();
      switch( event.getKeyIdentifier() ) {
        case "Left":
        case "Up":
          this._setNextOrPrevious( widget, "previous" );
          event.preventDefault();
          event.stopPropagation();
          break;
        case "Right":
        case "Down":
          this._setNextOrPrevious( widget, "next" );
          event.preventDefault();
          event.stopPropagation();
          break;
        case "Home":
        case "End":
        case "PageDown":
        case "PageUp":
          event.preventDefault();
          event.stopPropagation();
        break;
      }
    },

    _onSelection : function( event ) {
      this._unselectSiblings( event.getTarget() );
    },

    _isRadioElement : function( widget ) {
      return widget.hasState( "radio" );
    },

    _getRadioGroup: function( widget ) {
      var group = [];
      if( !widget.getNoRadioGroup() ) {
        var siblings = widget.getParent().getChildren();
        var length = siblings.length;
        // For Radio-Buttons all Radio-items of the group count,
        // else the group is bounded by any non-radio items
        if( widget.classname == "rwt.widgets.Button" ) {
          for( var i = 0; i < length; i++ ) {
            if( this._isRadioElement( siblings[ i ] ) ) {
              group.push( siblings[ i ] );
            }
          }
        } else {
          var isCurrentGroup = false;
          var i = 0;
          while( i < length && ( !isCurrentGroup || this._isRadioElement( siblings[ i ] ) ) ) {
            if( !isCurrentGroup ) {
              isCurrentGroup = siblings[ i ] == widget;
            }
            if( this._isRadioElement( siblings[ i ] ) ) {
              group.push( siblings[ i ] );
            } else {
              group = [];
            }
            i++;
          }
        }
      }
      return group;
    },

    // Set the "checked" property and focus on the following (next or previous)
    // radio button of the same group, after a corresponding key press.
    _setNextOrPrevious : function( widget, command ) {
      var allRadioButtons = this._getRadioGroup( widget );
      if( allRadioButtons.length > 0 ) {
        var currentRbIndex;
        for( var j = 0; j < allRadioButtons.length; j++ ) {
          if( allRadioButtons[ j ] == widget ) {
            currentRbIndex = j;
          }
        }
        // assign a value to 'nextSelectedRbIndex',
        // in case the 'command' is unrecognizable
        var nextSelectedRbIndex = currentRbIndex;
        if ( command == "next" ) {
          do {
            nextSelectedRbIndex++;
            if( nextSelectedRbIndex >= allRadioButtons.length ) {
              nextSelectedRbIndex = 0;
            }
          } while( !allRadioButtons[ nextSelectedRbIndex ].getEnabled() );
        }
        if ( command == "previous" ) {
          do {
            nextSelectedRbIndex--;
            if( nextSelectedRbIndex < 0 ) {
              nextSelectedRbIndex = allRadioButtons.length - 1;
            }
          } while( !allRadioButtons[ nextSelectedRbIndex ].getEnabled() );
        }
        if( nextSelectedRbIndex !== currentRbIndex ) {
          var nextRb = allRadioButtons[ nextSelectedRbIndex ];
          this._unselectSiblings( nextRb );
          nextRb.setSelection( true );
          nextRb.setFocused( true );
        }
      }
    },

    _unselectSiblings : function( widget ) {
      var group = this._getRadioGroup( widget );
      for( var i = 0; i < group.length; i++ ) {
        if( group[ i ] !== widget ) {
          group[ i ].setSelection( false );
        }
      }
    }

  }

} );
