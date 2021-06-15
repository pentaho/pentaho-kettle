/*******************************************************************************
 * Copyright (c) 2002, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.Shell", {

  extend : rwt.widgets.base.Window,

  include : rwt.animation.VisibilityAnimationMixin,

  construct : function( styles ) {
    this.base( arguments );
    this.setShowMinimize( styles.MIN === true );
    this.setAllowMinimize( styles.MIN === true  );
    this.setShowMaximize( styles.MAX === true  );
    this.setAllowMaximize(styles.MAX === true  );
    this.setShowClose( styles.CLOSE === true  );
    this.setAllowClose( styles.CLOSE === true  );
    this.setResizableWest( styles.RESIZE === true  );
    this.setResizableNorth( styles.RESIZE === true  );
    this.setResizableEast( styles.RESIZE === true  );
    this.setResizableSouth( styles.RESIZE === true  );
    this.setOverflow( "hidden" );
    // Note: This prevents a laoyut-glitch on the ipad:
    this.setRestrictToPageOnOpen( false );
    // TODO [rh] HACK to set mode on Label that shows the caption, _captionTitle
    //      is a 'protected' field on class Window
    this._captionTitle.setMode( "html" );
    this._activeControl = null;
    this._focusControl = null;
    this._parentShell = null;
    this._renderZIndex = true;
    this._sendBoundsTimer = new rwt.client.Timer( 0 );
    this._sendBoundsTimer.addEventListener( "interval", this._sendBounds, this );
    this._sendMoveFlag  = false;
    this._sendResizeFlag = false;
    this._sendResizeDelayed = false;
    this.addEventListener( "changeActiveChild", this._onChangeActiveChild );
    this.addEventListener( "changeFocusedChild", this._onChangeFocusedChild );
    this.addEventListener( "changeActive", this._onChangeActive );
    this.addEventListener( "changeLeft", this._onChangeLocation, this );
    this.addEventListener( "changeTop", this._onChangeLocation, this );
    this.addEventListener( "changeWidth", this._onChangeSize, this );
    this.addEventListener( "changeHeight", this._onChangeSize, this );
    this.addEventListener( "keydown", this._onKeydown );
    var req = rwt.remote.Connection.getInstance();
    req.addEventListener( "send", this._onSend, this );
    this.getCaptionBar().setWidth( "100%" );
    // [if] Listen for DOM event instead of qooxdoo event - see bug 294846.
    this.removeEventListener( "mousedown", this._onwindowmousedown );
    this.addEventListener( "create", this._onCreate, this );
    this.__onwindowmousedown = rwt.util.Functions.bind( this._onwindowmousedown, this );
    this.addToDocument();
  },

  statics : {
    TOP_LEFT : "topLeft",
    TOP_RIGHT : "topRight",
    BOTTOM_LEFT : "bottomLeft",
    BOTTOM_RIGHT : "bottomRight",
    CORNER_NAMES : [
      "topLeft",
      "topRight",
      "bottomLeft",
      "bottomRight"
    ],

    _onParentClose : function() {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        this.doClose();
      }
    },

    reorderShells : function( vWindowManager ) {
      var shells = rwt.util.Objects.getValues( vWindowManager.getAll() );
      shells = shells.sort( rwt.widgets.Shell._compareShells );
      var vLength = shells.length;
      var upperModalShell = null;
      if( vLength > 0 ) {
        var vTop = shells[ 0 ].getTopLevelWidget();
        var vZIndex = rwt.widgets.Shell.MIN_ZINDEX;
        for( var i = 0; i < vLength; i++ ) {
          vZIndex += 10;
          shells[ i ].setZIndex( vZIndex );
          if( shells[ i ]._appModal && shells[ i ].getVisibility() && shells[ i ].getDisplay() ) {
            upperModalShell = shells[ i ];
          }
        }
        if( upperModalShell != null ) {
          this._copyStates( upperModalShell, vTop._getBlocker() );
          vTop._getBlocker().show();
          vTop._getBlocker().setZIndex( upperModalShell.getZIndex() - 1 );
        } else {
          vTop._getBlocker().hide();
        }
      }
      rwt.widgets.Shell._upperModalShell = upperModalShell;
    },

    _copyStates : function( source, target ) {
      target.__states = {};
      for( var state in source.__states ) {
        if( source._isRelevantState( state ) ) {
          target.addState( state );
        }
      }
      target._renderAppearance();
      rwt.widgets.base.Widget.removeFromGlobalLayoutQueue( target );
    },

    /*
     * Compares two Shells regarding their desired z-order.
     *
     * Result is
     * - positive if sh1 is higher
     * - negative if sh2 is higher
     * - zero if equal
     */
    _compareShells : function( sh1, sh2 ) {
      var result = 0;
      // check for dialog relationship
      if( sh1.isDialogOf( sh2 ) ) {
        result = 1;
      } else if( sh2.isDialogOf( sh1 ) ) {
        result = -1;
      }
      // compare by onTop property
      if( result === 0 ) {
        result = ( sh1._onTop ? 1 : 0 ) - ( sh2._onTop ? 1 : 0 );
      }
      // compare by appModal property
      if( result === 0 ) {
        result = ( sh1._appModal ? 1 : 0 ) - ( sh2._appModal ? 1 : 0 );
      }
      // compare by top-level parent's z-order
      if( result === 0 ) {
        var top1 = sh1.getTopLevelShell();
        var top2 = sh2.getTopLevelShell();
        result = top1.getZIndex() - top2.getZIndex();
      }
      // compare by actual z-order
      if( result === 0 ) {
        result = sh1.getZIndex() - sh2.getZIndex();
      }
      return result;
    },

    MIN_ZINDEX : 1e5,

    MAX_ZINDEX : 1e7
  },

  destruct : function() {
    this.setParentShell( null );
    var connection = rwt.remote.Connection.getInstance();
    connection.removeEventListener( "send", this._onSend, this );
    var document = rwt.widgets.base.ClientDocument.getInstance();
    document.removeEventListener( "windowresize", this._onWindowResize, this );
    if( this.isCreated() ) {
      this.getElement().removeEventListener( "mousedown", this.__onwindowmousedown, false );
    }
    this._disposeObjects( "_sendBoundsTimer" );
  },

  members : {

    destroy : function() {
      this.doClose();
      this.getWindowManager().remove( this );
      this.base( arguments );
    },

    _onCreate : function() {
      this.getElement().addEventListener( "mousedown", this.__onwindowmousedown, false );
      this.removeEventListener( "create", this._onCreate, this );
    },

    // [if] Override to prevent the new open shell to automaticaly become
    // an active shell (see bug 297167).
    _beforeAppear : function() {
      rwt.widgets.base.Parent.prototype._beforeAppear.call( this );
      rwt.widgets.util.PopupManager.getInstance().update();
      var activeWindow = this.getWindowManager().getActiveWindow();
      this.getWindowManager().add( this );
      this.getWindowManager().setActiveWindow( activeWindow );
    },

    setDefaultButton : function( value ) {
      this._defaultButton = value;
      this._renderHighlightButton();
    },

    _renderHighlightButton : function() {
      var button = this._defaultButton;
      var focused = this.getFocusedChild();
      if ( focused instanceof rwt.widgets.Button && focused.hasState( "push" ) ) {
        button = focused;
      }
      if( this._highlightButton != null ) {
        this._highlightButton.removeState( "default" );
      }
      this._highlightButton = button;
      if( this._highlightButton != null ) {
        this._highlightButton.addState( "default" );
      }
    },

    getDefaultButton : function() {
      return this._defaultButton;
    },

    setParentShell : function( parentShell ) {
      var oldParentShell = this._parentShell;
      this._parentShell = parentShell;
      var listener = rwt.widgets.Shell._onParentClose;
      if( oldParentShell != null ) {
        oldParentShell.removeEventListener( "close", listener, this );
      }
      if( parentShell != null ) {
        parentShell.addEventListener( "close", listener, this );
      }
      this.dispatchSimpleEvent( "parentShellChanged" );
    },

    isDisableResize : function() {
      return this._disableResize ? true : false;
    },

    setActiveControl : function( control ) {
      this._activeControl = control;
    },

    /** To be called after rwt_XXX states are set */
    initialize : function() {
      this.setShowCaption( this.hasState( "rwt_TITLE" ) );
      this._onTop = ( this._parentShell != null && this._parentShell._onTop )
                    || this.hasState( "rwt_ON_TOP" );
      this._appModal =    this.hasState( "rwt_APPLICATION_MODAL" )
                       || this.hasState( "rwt_PRIMARY_MODAL" )
                       || this.hasState( "rwt_SYSTEM_MODAL" );
      if( this._appModal ) {
        this.setStyleProperty( "position", "fixed" );
      }
    },

    // TODO [rst] Find a generic solution for state inheritance
    addState : function( state ) {
      this.base( arguments, state );
      if( this._isRelevantState( state ) ) {
        this._captionBar.addState( state );
        this._captionTitle.addState( state );
        this._minimizeButton.addState( state );
        this._maximizeButton.addState( state );
        this._restoreButton.addState( state );
        this._closeButton.addState( state );
        var blocker = this._getClientDocumentBlocker();
        if( blocker != null ) {
          blocker.addState( state );
        }
      }
    },

    removeState : function( state ) {
      this.base( arguments, state );
      if( this._isRelevantState( state ) ) {
        this._captionBar.removeState( state );
        this._captionTitle.removeState( state );
        this._minimizeButton.removeState( state );
        this._maximizeButton.removeState( state );
        this._restoreButton.removeState( state );
        this._closeButton.removeState( state );
        var blocker = this._getClientDocumentBlocker();
        if( blocker != null ) {
          blocker.removeState( state );
        }
      }
    },

    _getClientDocumentBlocker : function() {
      var result = null;
      if(    this._appModal
          && rwt.widgets.Shell._upperModalShell == this )
      {
        result = this.getTopLevelWidget()._getBlocker();
      }
      return result;
    },

    _isRelevantState : function( state ) {
      return    state == "active"
             || state == "maximized"
             || state == "minimized"
             || state.substr( 0, 8 ) == "variant_"
             || state.substr( 0, 4 ) == "rwt_";
    },

    /**
     * Overrides rwt.widgets.base.Window#close()
     *
     * Called when user tries to close the shell.
     */
    close : function() {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        rwt.remote.Connection.getInstance().getRemoteObject( this ).notify( "Close" );
      }
    },

    /**
     * Really closes the shell.
     */
    doClose : function() {
      // Note [rst]: Fixes bug 232977
      // Background: There are situations where a shell is disposed twice, thus
      // doClose is called on an already disposed shell at the second time
      if( !this.isDisposed() ) {
        this.hide();
        if( this.hasEventListeners( "close" ) ) {
          var event = new rwt.event.DataEvent( "close", this );
          this.dispatchEvent( event, true );
        }
        var wm = this.getWindowManager();
        rwt.widgets.Shell.reorderShells( wm );
      }
    },

    _onChangeActiveChild : function( evt ) {
      // Work around qooxdoo bug #254: the changeActiveChild is fired twice when
      // a widget was activated by keyboard (getData() is null in this case)
      var widget = this._getParentControl( evt.getValue() );
      if(    !rwt.remote.EventUtil.getSuspended()
          && widget != null
          && widget !== this._activeControl )
      {
        this._notifyDeactivate( this._activeControl, widget );
        var id = rwt.remote.WidgetManager.getInstance().findIdByWidget( widget );
        var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( this );
        remoteObject.set( "activeControl", id );
        this._notifyActivate( this._activeControl, widget );
        this._activeControl = widget;
      }
    },

    _notifyDeactivate : function( oldActive, newActive ) {
      var target = oldActive;
      while( target != null && !this._hasDeactivateListener( target ) ) {
        if( target.getParent ) {
          target = target.getParent();
        } else {
          target = null;
        }
      }
      if( target != null && !target.contains( newActive ) ) {
        var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( target );
        remoteObject.notify( "Deactivate" );
      }
    },

    _notifyActivate : function( oldActive, newActive ) {
      var target = newActive;
      while( target != null && !this._hasActivateListener( target ) ) {
        if( target.getParent ) {
          target = target.getParent();
        } else {
          target = null;
        }
      }
      if( target != null && !target.contains( oldActive ) ) {
        var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( target );
        remoteObject.notify( "Activate" );
      }
    },

    _hasDeactivateListener : function( widget ) {
      return widget.getUserData( "deactivateListener" ) === true;
    },

    _hasActivateListener : function( widget ) {
      return widget.getUserData( "activateListener" ) === true;
    },

    _onChangeFocusedChild : function() {
      if( rwt.remote.EventUtil.getSuspended() ) {
        this._focusControl = this.getFocusedChild();
      }
      this._renderHighlightButton();
    },

    _onChangeActive : function( evt ) {
      // TODO [rst] This hack is a workaround for bug 345 in qooxdoo, remove this
      //      block as soon as the bug is fixed.
      //      See http://bugzilla.qooxdoo.org/show_bug.cgi?id=345
      if( !this.getActive() && !isFinite( this.getZIndex() ) ) {
        this.setZIndex( 1e8 );
      }
      // end of workaround
      if( !rwt.remote.EventUtil.getSuspended() && this.getActive() ) {
        rwt.remote.Connection.getInstance().getRemoteObject( this ).notify( "Activate" );
      }
      var active = evt.getValue();
      if( active ) {
        // workaround: Do not activate Shells that are blocked by a modal Shell
        var modalShell = rwt.widgets.Shell._upperModalShell;
        if( modalShell != null && modalShell.getZIndex() > this.getZIndex() ) {
          this.setActive( false );
          modalShell.setActive( true );
        }
        // end of workaround
      }
    },

    _applyMode : function( value, oldValue ) {
      var mode = value == null ? "normal" : value;
      rwt.remote.Connection.getInstance().getRemoteObject( this ).set( "mode", mode );
      var document = rwt.widgets.base.ClientDocument.getInstance();
      if( value == "maximized" ) {
        // User may change browser window size during long running request (before windowresize
        // listener is attached). Sync current shell bounds back to server - see bug 440948.
        this._sendBoundsTimer.start();
        document.addEventListener( "windowresize", this._onWindowResize, this );
      } else {
        document.removeEventListener( "windowresize", this._onWindowResize, this );
      }
      this.base( arguments, value, oldValue );
    },

    _onWindowResize : function() {
      this._sendResizeDelayed = true;
      this._sendResizeFlag = true;
      this._sendBounds();
    },

    _onChangeSize : function() {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        this._sendResizeFlag = true;
        this._sendBoundsTimer.start();
      }
    },

    _onChangeLocation : function() {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        this._sendMoveFlag = true;
        this._sendBoundsTimer.start();
      }
    },

    _sendBounds : function() {
      this._sendBoundsTimer.stop();
      var left = this._parseNumber( this.getLeft() );
      var top = this._parseNumber( this.getTop() );
      var height = this._parseNumber( this.getHeightValue() );
      var width = this._parseNumber( this.getWidthValue() );
      var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( this );
      remoteObject.set( "bounds", [ left, top, width, height ] );
      if( this._sendMoveFlag ) {
        remoteObject.notify( "Move", {} );
      }
      if( this._sendResizeFlag ) {
        remoteObject.notify( "Resize", {}, this._sendResizeDelayed ? 500 : undefined );
      }
      this._sendMoveFlag  = false;
      this._sendResizeFlag = false;
      this._sendResizeDelayed = false;
    },

    _parseNumber : function( value ) {
      var result = parseInt( value, 10 );
      return isNaN( result ) ? 0 : result;
    },

    _onKeydown : function( evt ) {
      var keyId = evt.getKeyIdentifier();
      if(    keyId == "Enter"
          && !evt.isShiftPressed()
          && !evt.isAltPressed()
          && !evt.isCtrlPressed()
          && !evt.isMetaPressed() )
      {
        var defButton = this.getDefaultButton();
        if( defButton != null && defButton.isSeeable() && defButton.getEnabled() ) {
          defButton.setFocused( true );
          defButton.execute();
        }
      } else if( keyId == "Escape" && this._parentShell != null ) {
        this.close();
      }
    },

    _onSend : function() {
      if( this.getActive() ) {
        var focusedChild = this.getFocusedChild();
        if( focusedChild != null && focusedChild != this._focusControl ) {
          this._focusControl = focusedChild;
          var widgetManager = rwt.remote.WidgetManager.getInstance();
          var focusedChildId = widgetManager.findIdByWidget( focusedChild );
          var server = rwt.remote.Connection.getInstance();
          var serverDisplay = server.getRemoteObject( rwt.widgets.Display.getCurrent() );
          serverDisplay.set( "focusControl", focusedChildId );
        }
      }
    },

    /**
     * Returns the parent Control for the given widget. If widget is a Control
     * itself, the widget is returned. Otherwise its parent is returned or null
     * if there is no parent
     */
    _getParentControl : function( widget ) {
      var widgetMgr = rwt.remote.WidgetManager.getInstance();
      var result = widget;
      while( result != null && !widgetMgr.isControl( result ) ) {
        if( result.getParent ) {
          result = result.getParent();
        } else {
          result = null;
        }
      }
      return result;
    },

    /**
     * Returns true if the receiver is a dialog shell of the given parent shell,
     * directly or indirectly.
     */
    isDialogOf : function( shell ) {
      var result = false;
      var parentShell = this._parentShell;
      while( !result && parentShell != null ) {
        result = shell === parentShell;
        parentShell = parentShell._parentShell;
      }
      return result;
    },

    /**
     * Returns the top-level shell if the receiver is a dialog or the shell
     * itself if it is a top-level shell.
     */
    getTopLevelShell : function() {
      var result = this;
      while( result._parentShell != null ) {
        result = result._parentShell;
      }
      return result;
    },

    /* TODO [rst] Revise when upgrading: overrides the _sendTo() function in
     *      superclass Window to allow for always-on-top.
     *      --> http://bugzilla.qooxdoo.org/show_bug.cgi?id=367
     */
    _sendTo : function() {
      rwt.widgets.Shell.reorderShells( this.getWindowManager() );
    },

    /*
     * Overwrites Popup#bringToFront
     */
    bringToFront : function() {
      var targetShell = this;
      while( targetShell._parentShell != null ) {
        targetShell = targetShell._parentShell;
      }
      this._setRenderZIndex( false );
      this.setZIndex( rwt.widgets.Shell.MAX_ZINDEX + 1 );
      targetShell.setZIndex( rwt.widgets.Shell.MAX_ZINDEX + 1 );
      rwt.widgets.Shell.reorderShells( this.getWindowManager() );
      this._setRenderZIndex( true );
    },

    _applyZIndex : function( newValue, oldValue ) {
      if( this._renderZIndex ) {
        this.base( arguments, newValue, oldValue );
      }
    },

    _setRenderZIndex : function( value ) {
       // Needed to prevent flickering during display-overlay animations.
      this._renderZIndex = value;
      if( value ) {
        this._applyZIndex( this.getZIndex() );
      }
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      var isRTL = value === "rtl";
      this.getCaptionBar().setReverseChildrenOrder( isRTL );
      this.getCaptionBar().setHorizontalChildrenAlign( isRTL ? "right" : "left" );
      this.getLayoutImpl().setMirror( isRTL );
    },

    /*
     * E X P E R I M E N T A L
     * (for future PRIMARY_MODAL support)
     */
    setBlocked : function( blocked ) {
      if( blocked ) {
        if( !this._blocker ) {
          this._blocker = new rwt.widgets.base.Parent();
          this._blocker.setAppearance( "client-document-blocker" );
          this.add( this._blocker );
        }
        this._blocker.setSpace( 0, 0, 10000, 10000 );
        this._blocker.setZIndex( 1000 );
      } else {
        if( this._blocker ) {
          this.remove( this._blocker );
          this._blocker.destroy();
          this._blocker = null;
        }
      }
    },

    setFullScreen : function( fullScreen ) {
      if( fullScreen ) {
        this._captionBar.setDisplay( false );
      } else {
        this._captionBar.setDisplay( this.hasState( "rwt_TITLE" ) );
      }
    }

  }
} );
