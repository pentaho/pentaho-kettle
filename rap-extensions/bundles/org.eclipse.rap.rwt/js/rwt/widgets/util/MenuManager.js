/*******************************************************************************
 * Copyright (c) 2009, 2018 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.util.MenuManager", {

  extend : rwt.util.ObjectManager,

  statics : {

    getInstance : function() {
      return rwt.runtime.Singletons.get( rwt.widgets.util.MenuManager );
    }

  },

  members :  {

    // Note: This function is called on alle mousedown and mouseup events,
    //       and also on "esc" and "tab" key-events and on window-blur.
    update : function( target, eventType ) {
      var menus = this.getAll();
      var isMouseDown = eventType == "mousedown";
      var isMouseUp = eventType == "mouseup";
      var isMouseEvent = isMouseDown || isMouseUp;
      var eventHasTarget = target != null;
      var targetHasMenu =    eventHasTarget
                          && target.getMenu
                          && target.getMenu() != null;
      var targetMenuBar = this._getMenuBar( target );

      for ( var hash in menus ) {
        var menu = menus[ hash ];
        var hide = false;
        // 1. AutoHide supported?
        if ( menu.getAutoHide() ) {
          // Gathering data
          var targetIsOpener = menu.getOpener() === target;
          var isContextMenu =
            !( menu.getOpener() instanceof rwt.widgets.MenuItem );
          var notSameMenuBar = targetMenuBar != this._getMenuBar( menu );
          // 2. Global event like keydown or blur?
          if ( !eventHasTarget || !isMouseEvent ) {
            hide = true;
          }
          // 3. Click on another menubar?
          if( isMouseDown && notSameMenuBar ) {
            hide = true;
          }
          // 4. Click on the opener of a context menu?
          if( isMouseDown && isContextMenu && targetIsOpener ) {
            hide = true;
          }
          // Ignore other events that are handled by the menu itself
          if( !targetHasMenu && !targetIsOpener ) {
            // 5. mousedown somwhere outside the menu
            if ( isMouseDown && !menu.isSubElement( target ) ) {
              hide = true;
            }
            // 6. Execute of an menuItem
            if (    isMouseUp
                 && menu.isSubElement( target, true )
                 && target.isEnabled()
                 && target.getParentMenu().isPressed() )
            {
              hide = true;
            }
          }
        }
        if( hide ) {
          menu.hide();
        }
      }
    },

    _getMenuBar : function( widget ) {
      var menu = null;
      var menuBar = null;
      if( widget instanceof rwt.widgets.MenuItem ) {
        menu = widget.getParentMenu();
      } else {
        if(    widget instanceof rwt.widgets.Menu
            || widget instanceof rwt.widgets.MenuBar
        ) {
          menu = widget;
        }
      }
      while ( menuBar == null && menu != null ) {
        if( menu instanceof rwt.widgets.MenuBar ) {
          menuBar = menu;
        } else {
          var hasOpener =
            menu.getOpener() instanceof rwt.widgets.MenuItem;
            menu = hasOpener ? menu.getOpener().getParentMenu() : null;
        }
      }
      return menuBar;
    }

  }
} );
