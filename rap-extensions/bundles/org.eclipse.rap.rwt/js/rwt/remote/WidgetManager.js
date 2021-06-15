/*******************************************************************************
 * Copyright (c) 2002, 2014 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

/**
 * Maps widget id's to their respective object references. Allows for
 * adding, removing and disposing of widgets and their id. In addition
 * the mapping of widgets and their respective id's can be queried.
 *
 * TODO [tb] : use ObjectRegistry/WidgetUtil instead, remove class
 *
 */
rwt.qx.Class.define( "rwt.remote.WidgetManager", {

  extend : rwt.qx.Object,

  statics : {

    getInstance : function() {
      return rwt.runtime.Singletons.get( rwt.remote.WidgetManager );
    }

  },

  construct : function() {
    this.base( arguments );
  },

  members : {
    /**
     * Disposes of the widget that is registered with the given id. The widget
     * is disconnected from its parent, its 'dispose' method is called and it is
     * removed from this WidgetManager (see remove).
     * No action is taken if there is no widget registered for the given id or
     * the widget was already disposed of.
     */
    dispose : function( id ) {
      var widget = this.findWidgetById( id );
      if( widget != null ) {
        this.remove( widget );
        if( !widget.getDisposed() ) {
          this._removeToolTipPopup( widget );
          widget.destroy();
        }
      }
    },

    /**
     * Registers the given widget under the given id at the WidgetManager.
     */
    add : function( widget, id, isControl, adapter ) {
      if( isControl === true ) {
        widget.setUserData( "isControl", true );
      }
      rwt.remote.ObjectRegistry.add( id, widget, adapter );
    },

    /**
     * Unregisters the given widget at the WidgetManager. Note that the widget is
     * not disposed of.
     */
    remove : function( widget ) {
      var id = this.findIdByWidget( widget );
      rwt.remote.ObjectRegistry.remove( id );
    },

    /**
     * Returns the widget for the given id or null if there is no widget
     * registered for the given id exists.
     */
    findWidgetById : function( id ) {
      return rwt.remote.ObjectRegistry.getObject( id );
    },

    /**
     * Returns the id (string) for the given widget or null if the widget is not
     * registered.
     */
    findIdByWidget : function( widget ) {
      return rwt.remote.ObjectRegistry.getId( widget );
    },

    /**
     * Determines whether the given widget represents a server-side instance of
     * Control (or one of its subclasses)
     */
    isControl : function( widget ) {
      var data = null;
      if( widget != null ) {
        data = widget.getUserData( "isControl" );
      }
      return data === true;
    },

    /**
     * Returns the nearest SWT-control in the hierarchy for the given qxWidget
     * or null if no parent control could be found. If the given qxWidget
     * represents a control, it is returned.
     */
    findControl : function( qxWidget ) {
      var parent = qxWidget;
      while( parent != null && !this.isControl( parent ) ) {
        parent = parent.getParent ? parent.getParent() : null;
      }
      return parent;
    },

    findEnabledControl : function( qxWidget ) {
      var parent = qxWidget;
      while( parent != null && !( this.isControl( parent ) && parent.getEnabled() ) ) {
        parent = parent.getParent ? parent.getParent() : null;
      }
      return parent;
    },

    ////////////////////
    // ToolTip handling

    /**
     * Sets the toolTipText for the given widget. An empty or null toolTipText
     * removes the tool tip of the widget.
     */
    setToolTip : function( widget, toolTipText ) {
      rwt.widgets.base.WidgetToolTip.setToolTipText( widget, toolTipText );
    },

    _removeToolTipPopup : function( widget ) {
      widget.setToolTipText( null );
    }

  }
} );
