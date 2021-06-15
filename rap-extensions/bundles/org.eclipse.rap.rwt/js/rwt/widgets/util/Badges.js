/*******************************************************************************
 * Copyright (c) 2014, 2017 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function(){

  var $ = rwt.util.RWTQuery;
  var BADGE_ELEMENT = "rwt.widgets.util.Badges.BADGE_ELEMENT";
  var BADGE = "Widget-Badge";
  var LINE_HEIGHT_FACTOR = 1.1;
  var PADDING = 1;

  rwt.define( "rwt.widgets.util.Badges", {

    setBadge : function( widget, text ) {
      if( text ) {
        var badge = this._getBadgeElement( widget );
        var element = $( widget ).get( 0 ); // add to OUTER element, do not use append diretly
        $( element ).append( badge );
        $( badge ).text( text );
        widget.addEventListener( "appear", this._renderBadgePosition, widget );
        widget.addEventListener( "flush", this._renderBadgePosition, widget );
        widget.addEventListener( "changeCustomVariant", this._renderBadgeStyle, widget );
        this._renderBadgeStyle.apply( widget );
        this._renderBadgePosition.apply( widget );
      } else {
        this._removeBadge( widget );
      }
      widget.dispatchSimpleEvent( "changeBadge" );
    },

    getBadge : function( widget ) {
      var badge = widget.getUserData( BADGE_ELEMENT );
      if( badge ) {
        return $( badge ).text();
      }
      return null;
    },

    _getBadgeElement : function( widget ) {
      var badge = widget.getUserData( BADGE_ELEMENT );
      if( badge ) {
        return badge;
      }
      widget.enableEnhancedBorder();
      badge = document.createElement( "div" );
      $( badge ).css( {
        "position" : "absolute",
        "textAlign" : "center",
        "lineHeight" : LINE_HEIGHT_FACTOR
      } );
      widget.setUserData( BADGE_ELEMENT, badge );
      return badge;
    },

    _removeBadge : function( widget ) {
      var badge = widget.getUserData( BADGE_ELEMENT );
      if( badge ) {
        $( badge ).text( "" );
        $( badge ).detach();
      }
    },

    _renderBadgePosition : function( event ) {
      var widget = this;
      var updateNeeded = event && event.getData ? event.getData().updateBadgePosition : true;
      var badge = widget.getUserData( BADGE_ELEMENT );
      if(    updateNeeded
          && widget.isSeeable()
          && badge
          && badge.parentNode
          && widget.computeBadgePosition )
      {
        var size = [ badge.offsetWidth, badge.offsetHeight ];
        var position = widget.computeBadgePosition( size );
        // NOTE: 2 of these values should be "auto", but we don't enforce it here
        $( badge ).css( {
          "top" : position[ 0 ],
          "right" : position[ 1 ],
          "bottom" : position[ 2 ],
          "left" : position[ 3 ]
        } );
      }
    },

    _renderBadgeStyle : function() {
      var widget = this;
      var badge = widget.getUserData( BADGE_ELEMENT );
      if( badge ) {
        var states = {};
        if( widget._customVariant !== null ) {
          states[ widget._customVariant ] = true;
        }
        var themeValues = new rwt.theme.ThemeValues( states );
        $( badge ).css( {
          "color" : themeValues.getCssColor( BADGE, "color" ),
          "backgroundColor" : themeValues.getCssColor( BADGE, "background-color" ),
          "border" : themeValues.getCssBorder( BADGE, "border" ),
          "font" : themeValues.getCssFont( BADGE, "font" )
        } );
        var height = parseInt( badge.style.fontSize, 10 ) * LINE_HEIGHT_FACTOR + 2 * PADDING;
        $( badge ).css( {
          "minWidth" : height,
          "paddingLeft" : Math.floor( height * 0.3 ),
          "paddingRight" : Math.floor( height * 0.3 ),
          "paddingTop" : PADDING,
          "paddingBottom" : PADDING
        } );
      }
    }

  } );

}());
