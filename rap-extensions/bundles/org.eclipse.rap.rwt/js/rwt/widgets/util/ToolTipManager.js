/*******************************************************************************
 * Copyright (c) 2004, 2014 1&1 Internet AG, Germany, http://www.1und1.de,
 *                          EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/

(function() {

var getToolTip = function() {
  return rwt.widgets.base.WidgetToolTip.getInstance();
};

rwt.qx.Class.define( "rwt.widgets.util.ToolTipManager", {

  extend : rwt.qx.Object,

  statics : {

    getInstance : function() {
      return rwt.runtime.Singletons.get( rwt.widgets.util.ToolTipManager );
    }

  },

  properties : {
    currentToolTipTarget : {
      nullable : true,
      apply : "_applyCurrentToolTipTarget"
    }
  },

  members : {

    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     * @return {void | Boolean} TODOC
     */
    _applyCurrentToolTipTarget : function(value, old) {

      // If old tooltip existing, hide it and clear widget binding
      if (old)
      {
        getToolTip()._stopShowTimer();
        getToolTip()._startHideTimer();
        old.removeEventListener( "updateToolTip", this._updateEventHandler );
      }

      getToolTip().setBoundToWidget( value );

      // If new tooltip is not null, set it up and start the timer
      if (value) {
        getToolTip()._startShowTimer();
        value.addEventListener( "updateToolTip", this._updateEventHandler );
      }
    },

    handleMouseEvent : function( event ) {
      var type = event.getType();
      if( type === "mouseover" ) {
        this._handleMouseOver( event );
      } else if ( type === "mouseout" ) {
        this._handleMouseOut( event );
      } else if ( type === "mousemove" ) {
        this._handleMouseMove( event );
      }
    },

    _handleMouseOver : function( e ) {
      var vTarget = e.getTarget();
      if( vTarget === getToolTip() || getToolTip().contains( vTarget ) ) {
        this.setCurrentToolTipTarget( getToolTip().getLastWidget() );
        return;
      }
      // Allows us to use DOM Nodes as tooltip target :)
      if (!(vTarget instanceof rwt.widgets.base.Widget) && vTarget.nodeType == 1) {
        vTarget = rwt.event.EventHandlerUtil.getTargetObject(vTarget);
      }
      while (vTarget != null && vTarget.getToolTipText() === null ) {
        vTarget = vTarget.getParent();
      }
      this.setCurrentToolTipTarget( vTarget );
    },

    _handleMouseOut : function( e ) {
      var vTarget = e.getTarget();
      var vRelatedTarget = e.getRelatedTarget();
      if( vTarget === getToolTip() ) {
        return;
      }
      var tTarget = this.getCurrentToolTipTarget();
      if (tTarget && (vRelatedTarget == getToolTip() || getToolTip().contains(vRelatedTarget))) {
        return;
      }
      if (vRelatedTarget && vTarget && vTarget.contains(vRelatedTarget)) {
        return;
      }
      if (tTarget && !vRelatedTarget) {
        this.setCurrentToolTipTarget( null );
      }
    },

    _handleMouseMove : function( e ) {
      if( this.getCurrentToolTipTarget() ) {
        getToolTip()._handleMouseMove( e );
      }
    },

    handleFocus : function() {
      // nothing to do
    },

    handleBlur : function(e)
    {
      var vTarget = e.getTarget();

      if (!vTarget) {
        return;
      }

      var tTarget = this.getCurrentToolTipTarget();

      if (tTarget === vTarget) {
        getToolTip()._stopShowTimer();
        getToolTip()._startHideTimer();
      }
    },

    handleKeyEvent : function( event ) {
      switch( event.getKeyIdentifier() ) {
        case "Control":
        case "Alt":
        case "Shift":
        case "Meta":
        case "Win":
        break;
        default:
          getToolTip()._stopShowTimer();
          getToolTip()._quickHide();
        break;
      }
    },

    _updateEventHandler : function( target ) {
      if( getToolTip().getBoundToWidget() === target ) {
        getToolTip().updateText();
      }
    }

  }

} );

}() );
