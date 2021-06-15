/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

namespace( "rwt.util" );

rwt.util.Encoding = {

  _escapeRegExp : /(&|<|>|\")/g,
  _escapeRegExpMnemonics : /(&&|&|<|>|")/g,
  _newlineRegExp : /(\r\n|\n|\r)/g,
  _outerWhitespaceRegExp : /(^ {1,1}| +$)/g,
  _outerWhitespaceRegExp2 : /(^ {1,}| +$)/g,
  _whitespaceRegExp : / {2,}/g,

  _escapeResolver : null,
  _escapeResolverMnemonics : null,
  _mnemonicFound : false,

  _escapeMap : {
    "<" : "&lt;",
    ">" : "&gt;",
    "\"" : "&quot;",
    "&&" : "&amp;",
    "&" : "&amp;"
  },

  /**
   * Replaces all occurrences of the characters <,>,&," with their corresponding HTML entities.
   * When the parameter mnemonic is set to true, this method handles ampersand characters in the
   * text as mnemonics in the same manner as SWT does.
   * Note: In contrast to SWT, the characters following an ampersand are currently not underlined.
   *
   * @param text the input text
   * @param mnemonics if true, the function removes the firest "&"
   *                  if a numbner, underlines that character (other option will be removed)
   *
   * @return the resulting text
   */
  // Note [rst]: Single quotes are not escaped as the entity &apos; is not
  //             defined in HTML 4. They should be handled by this method once
  //             we produce XHTML output.
  escapeText : function( text, mnemonics ) {
    if( text === null ) {
      throw new Error( "escapeText with parameter null not allowed" );
    }
    var result;
    this._mnemonicFound = false; // first found mnemonic may be resolved
    if( mnemonics === true ) {
      result = text.replace( this._escapeRegExpMnemonics, this._getEscapeResolverMnemonics() );
    } else {
      if( typeof mnemonics === "number" ) {
        result = this._escapeWithMnemonic( text, mnemonics );
      } else {
        result = text.replace( this._escapeRegExp, this._getEscapeResolver() );
      }
    }
    return this.truncateAtZero( result );
  },

  truncateAtZero : function( text ) {
    var result = text;
    var index = result.indexOf( String.fromCharCode( 0 ) );
    if( index !== -1 ) {
      result = result.substring( 0, index );
    }
    return result;
  },

  /**
   * Replaces all newline characters in the specified input string with the
   * given replacement string. All common newline characters are replaced (Unix,
   * Windows, and MacOS).
   *
   * @param input the string to process
   * @param replacement the string to replace line feeds with.
   * @return a copy of the input string with all newline characters replaced
   */
  replaceNewLines : function( text, optionalReplacement ) {
    var replacement = arguments.length > 1 ? optionalReplacement : "\\n";
    return text.replace( this._newlineRegExp, replacement );
  },

  /**
   * Replaces white spaces in the specified input string with &nbsp;.
   * For correct word wrapping, the last white space in a sequence of white
   * spaces is not replaced, if there is a different character following.
   * A single white space between words is not replaced whereas a single
   * leading white space is replaced.
   *
   * @param input the string to process
   * @return a copy of the input string with white spaces replaced
   */
  replaceWhiteSpaces : function( text ) {
    var result = text.replace( this._outerWhitespaceRegExp, this._outerWhitespaceResolver );
    result = result.replace( this._whitespaceRegExp, this._whitespaceResolver );
    return result;
  },

  /**
   * Escapes all leading and trailing spaces in the given input string.
   *
   * @param text input the string to process
   * @return a copy of the input string with all leading and trailing spaces
   * replaced
   */
  escapeLeadingTrailingSpaces : function( text ) {
    return text.replace( this._outerWhitespaceRegExp2, this._outerWhitespaceResolver );
  },


  /**
   * Escapes all chars that have a special meaning in regular expressions
   *
   * @type static
   * @param str {String} the string where to escape the chars.
   * @return {String} the string with the escaped chars.
   */
  escapeRegexpChars : function( str ) {
    return str.replace( /([\\\.\(\)\[\]\{\}\^\$\?\+\*])/g, "\\$1" );
  },


  /**
   * Unescapes a string containing entity escapes to a string
   * containing the actual Unicode characters corresponding to the
   * escapes. Supports HTML 4.0 entities.
   *
   * For example, the string "&amp;lt;Fran&amp;ccedil;ais&amp;gt;"
   * will become "&lt;Fran&ccedil;ais&gt;"
   *
   * If an entity is unrecognized, it is left alone, and inserted
   * verbatim into the result string. e.g. "&amp;gt;&amp;zzzz;x" will
   * become "&gt;&amp;zzzz;x".
   *
   * @type static
   * @param str {String} the String to unescape, may be null
   * @return {var} a new unescaped String
   * @see #escape
   */
  unescape : function( str ) {
    return this._unescapeEntities( str, rwt.html.Entity.TO_CHARCODE );
  },

  removeAmpersandControlCharacters : function( text ) {
    return text.replace(/(&&|&)/g, function( match ) {
      if( match === "&&" ) {
        return "&";
      }
      return "";
    } );
  },

  /////////
  // Helper

  _escapeWithMnemonic : function( text, index ) {
    var split = [
      text.slice( 0, index ).replace( this._escapeRegExp, this._getEscapeResolver() ),
      "<span style=\"text-decoration:underline\">",
      text.charAt( index ).replace( this._escapeRegExp, this._getEscapeResolver() ),
      "</span>",
      text.slice( index + 1 ).replace( this._escapeRegExp, this._getEscapeResolver() )
    ];
    return split.join( "" );
  },

  _getEscapeResolverMnemonics : function() {
    if( this._escapeResolverMnemonics ===  null ) {
      this._getEscapeResolver(); // implicitly create default resolver
      var EncodingUtil = this;
      this._escapeResolverMnemonics = function( match ) {
        var result;
        if( match === "&" && !EncodingUtil._mnemonicFound ) {
          result = "";
          EncodingUtil._mnemonicFound = true;
        } else {
          result = EncodingUtil._escapeResolver( match );
        }
        return result;
      };
    }
    return this._escapeResolverMnemonics;
  },

  _getEscapeResolver : function() {
    if( this._escapeResolver === null ) {
      var EncodingUtil = this;
      this._escapeResolver = function( match ) {
        return EncodingUtil._escapeMap[ match ];
      };
    }
    return this._escapeResolver;
  },

  _outerWhitespaceResolver : function( match ) {
    return match.replace( / /g, "&nbsp;" );
  },

  _whitespaceResolver : function( match ) {
    return match.slice( 1 ).replace( / /g, "&nbsp;" ) + " ";
  },

  _unescapeEntities : function( str, entitiesToCharCode ) {
    return str.replace( /&[#\w]+;/gi, function( entity ) {
      var chr = entity;
      var entity = entity.substring( 1, entity.length - 1 );
      var code = entitiesToCharCode[ entity ];
      if( code ) {
        chr = String.fromCharCode( code );
      } else {
        if( entity.charAt( 0 ) === '#' ) {
          if( entity.charAt(1).toUpperCase() === 'X' ) {
            code = entity.substring( 2 );
            // match hex number
            if( code.match( /^[0-9A-Fa-f]+$/gi ) ) {
              chr = String.fromCharCode( parseInt( code, 16 ) );
            }
          } else {
            code = entity.substring( 1 );
            // match integer
            if( code.match( /^\d+$/gi ) ) {
              chr = String.fromCharCode( parseInt( code, 10 ) );
            }
          }
        }
      }
      return chr;
    } );
  }


};
