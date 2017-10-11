//CHECKSTYLE:Indentation:OFF
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

// $ANTLR 3.4 C:\\workspace-sts\\Kettle trunk -
// restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
// FastSimpleGenericEdifactDirectXML.g 2012-12-06 11:16:38

package org.pentaho.di.trans.steps.edi2xml.grammar;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

@SuppressWarnings( { "all", "warnings", "unchecked" } )
public class FastSimpleGenericEdifactDirectXMLLexer extends Lexer {
  public static final int EOF = -1;
  public static final int T__9 = 9;
  public static final int T__10 = 10;
  public static final int T__11 = 11;
  public static final int T__12 = 12;
  public static final int T__13 = 13;
  public static final int T__14 = 14;
  public static final int COMPLEX_ELEMENT_ITEM_SEPARATOR = 4;
  public static final int ELEMENT_SEPARATOR = 5;
  public static final int RELEASE_CHARACTER = 6;
  public static final int SEGMENT_TERMINATOR = 7;
  public static final int TEXT_DATA = 8;

  // delegates
  // delegators
  public Lexer[] getDelegates() {
    return new Lexer[] {};
  }

  public FastSimpleGenericEdifactDirectXMLLexer() {
  }

  public FastSimpleGenericEdifactDirectXMLLexer( CharStream input ) {
    this( input, new RecognizerSharedState() );
  }

  public FastSimpleGenericEdifactDirectXMLLexer( CharStream input, RecognizerSharedState state ) {
    super( input, state );
  }

  public String getGrammarFileName() {
    return "C:\\workspace-sts\\Kettle trunk - "
      + "restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g";
  }

  // $ANTLR start "T__9"
  public final void mT__9() throws RecognitionException {
    int _type = T__9;
    int _channel = DEFAULT_TOKEN_CHANNEL;
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:6:6:
    // ( ' ' )
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:6:8:
    // ' '

    match( ' ' );
    state.type = _type;
    state.channel = _channel;
  }

  // $ANTLR end "T__9"

  // $ANTLR start "T__10"
  public final void mT__10() throws RecognitionException {
    int _type = T__10;
    int _channel = DEFAULT_TOKEN_CHANNEL;
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:7:7:
    // ( 'UNA:+,? \\'' )
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:7:9:
    // 'UNA:+,? \\''

    match( "UNA:+,? '" );
    state.type = _type;
    state.channel = _channel;
  }

  // $ANTLR end "T__10"

  // $ANTLR start "T__11"
  public final void mT__11() throws RecognitionException {
    int _type = T__11;
    int _channel = DEFAULT_TOKEN_CHANNEL;
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:8:7:
    // ( 'UNA:+.? \\'' )
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:8:9:
    // 'UNA:+.? \\''

    match( "UNA:+.? '" );

    state.type = _type;
    state.channel = _channel;
  }

  // $ANTLR end "T__11"

  // $ANTLR start "T__12"
  public final void mT__12() throws RecognitionException {
    int _type = T__12;
    int _channel = DEFAULT_TOKEN_CHANNEL;
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:9:7:
    // ( '\\n' )
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:9:9:
    // '\\n'

    match( '\n' );
    state.type = _type;
    state.channel = _channel;
  }

  // $ANTLR end "T__12"

  // $ANTLR start "T__13"
  public final void mT__13() throws RecognitionException {
    int _type = T__13;
    int _channel = DEFAULT_TOKEN_CHANNEL;
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
    // FastSimpleGenericEdifactDirectXML.g:10:7: ( '\\r' )
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
    // FastSimpleGenericEdifactDirectXML.g:10:9: '\\r'

    match( '\r' );
    state.type = _type;
    state.channel = _channel;
  }

  // $ANTLR end "T__13"

  // $ANTLR start "T__14"
  public final void mT__14() throws RecognitionException {
    int _type = T__14;
    int _channel = DEFAULT_TOKEN_CHANNEL;
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
    // FastSimpleGenericEdifactDirectXML.g:11:7:( '\\t' )
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
    // FastSimpleGenericEdifactDirectXML.g:11:9:'\\t'

    match( '\t' );
    state.type = _type;
    state.channel = _channel;
  }

  // $ANTLR end "T__14"

  // $ANTLR start "RELEASE_CHARACTER"
  public final void mRELEASE_CHARACTER() throws RecognitionException {
    int _type = RELEASE_CHARACTER;
    int _channel = DEFAULT_TOKEN_CHANNEL;
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
    // FastSimpleGenericEdifactDirectXML.g:125:21:
    // ( '?' )
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
    // FastSimpleGenericEdifactDirectXML.g:125:23:
    // '?'

    match( '?' );
    state.type = _type;
    state.channel = _channel;
  }

  // $ANTLR end "RELEASE_CHARACTER"

  // $ANTLR start "ELEMENT_SEPARATOR"
  public final void mELEMENT_SEPARATOR() throws RecognitionException {
    int _type = ELEMENT_SEPARATOR;
    int _channel = DEFAULT_TOKEN_CHANNEL;
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
    // FastSimpleGenericEdifactDirectXML.g:126:21:
    // ( '+' )
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
    // FastSimpleGenericEdifactDirectXML.g:126:23:
    // '+'

    match( '+' );
    state.type = _type;
    state.channel = _channel;
  }

  // $ANTLR end "ELEMENT_SEPARATOR"

  // $ANTLR start "SEGMENT_TERMINATOR"
  public final void mSEGMENT_TERMINATOR() throws RecognitionException {
    int _type = SEGMENT_TERMINATOR;
    int _channel = DEFAULT_TOKEN_CHANNEL;
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
    // FastSimpleGenericEdifactDirectXML.g:127:22:
    // ( '\\'' )
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
    // FastSimpleGenericEdifactDirectXML.g:127:24:
    // '\\''

    match( '\'' );
    state.type = _type;
    state.channel = _channel;
  }

  // $ANTLR end "SEGMENT_TERMINATOR"

  // $ANTLR start "COMPLEX_ELEMENT_ITEM_SEPARATOR"
  public final void mCOMPLEX_ELEMENT_ITEM_SEPARATOR() throws RecognitionException {
    int _type = COMPLEX_ELEMENT_ITEM_SEPARATOR;
    int _channel = DEFAULT_TOKEN_CHANNEL;
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
    // FastSimpleGenericEdifactDirectXML.g:128:33:
    // ( ':' )
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
    // FastSimpleGenericEdifactDirectXML.g:128:35:
    // ':'

    match( ':' );
    state.type = _type;
    state.channel = _channel;
  }

  // $ANTLR end "COMPLEX_ELEMENT_ITEM_SEPARATOR"

  // $ANTLR start "TEXT_DATA"
  public final void mTEXT_DATA() throws RecognitionException {
    int _type = TEXT_DATA;
    int _channel = DEFAULT_TOKEN_CHANNEL;
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
    // FastSimpleGenericEdifactDirectXML.g:129:14:
    // ( (~ ( RELEASE_CHARACTER | SEGMENT_TERMINATOR | COMPLEX_ELEMENT_ITEM_SEPARATOR | ELEMENT_SEPARATOR ) | (
    // RELEASE_CHARACTER ELEMENT_SEPARATOR ) | ( RELEASE_CHARACTER RELEASE_CHARACTER ) | ( RELEASE_CHARACTER
    // COMPLEX_ELEMENT_ITEM_SEPARATOR ) | ( RELEASE_CHARACTER SEGMENT_TERMINATOR ) )+ )
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
    // FastSimpleGenericEdifactDirectXML.g:129:16:
    // (~ ( RELEASE_CHARACTER | SEGMENT_TERMINATOR | COMPLEX_ELEMENT_ITEM_SEPARATOR | ELEMENT_SEPARATOR ) | (
    // RELEASE_CHARACTER ELEMENT_SEPARATOR ) | ( RELEASE_CHARACTER RELEASE_CHARACTER ) | ( RELEASE_CHARACTER
    // COMPLEX_ELEMENT_ITEM_SEPARATOR ) | ( RELEASE_CHARACTER SEGMENT_TERMINATOR ) )+

    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
    // FastSimpleGenericEdifactDirectXML.g:129:16:
    // (~ ( RELEASE_CHARACTER | SEGMENT_TERMINATOR | COMPLEX_ELEMENT_ITEM_SEPARATOR | ELEMENT_SEPARATOR ) | (
    // RELEASE_CHARACTER ELEMENT_SEPARATOR ) | ( RELEASE_CHARACTER RELEASE_CHARACTER ) | ( RELEASE_CHARACTER
    // COMPLEX_ELEMENT_ITEM_SEPARATOR ) | ( RELEASE_CHARACTER SEGMENT_TERMINATOR ) )+
    int cnt1 = 0;
    loop1: do {
      int alt1 = 6;
      int LA1_0 = input.LA( 1 );

      if ( ( ( LA1_0 >= '\u0000' && LA1_0 <= '&' )
        || ( LA1_0 >= '(' && LA1_0 <= '*' ) || ( LA1_0 >= ',' && LA1_0 <= '9' )
        || ( LA1_0 >= ';' && LA1_0 <= '>' ) || ( LA1_0 >= '@' && LA1_0 <= '\uFFFF' ) ) ) {
        alt1 = 1;
      } else if ( ( LA1_0 == '?' ) ) {
        switch ( input.LA( 2 ) ) {
          case '+':
            alt1 = 2;
            break;
          case '?':
            alt1 = 3;
            break;
          case ':':
            alt1 = 4;
            break;
          case '\'':
            alt1 = 5;
            break;

        }

      }

      switch ( alt1 ) {
        case 1:
          // C:\\workspace-sts\\Kettle trunk -
          // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
          // FastSimpleGenericEdifactDirectXML.g:129:17:
          // ~ ( RELEASE_CHARACTER | SEGMENT_TERMINATOR | COMPLEX_ELEMENT_ITEM_SEPARATOR | ELEMENT_SEPARATOR )

          if ( ( input.LA( 1 ) >= '\u0000' && input.LA( 1 ) <= '&' )
            || ( input.LA( 1 ) >= '(' && input.LA( 1 ) <= '*' )
            || ( input.LA( 1 ) >= ',' && input.LA( 1 ) <= '9' )
            || ( input.LA( 1 ) >= ';' && input.LA( 1 ) <= '>' )
            || ( input.LA( 1 ) >= '@' && input.LA( 1 ) <= '\uFFFF' ) ) {
            input.consume();
          } else {
            MismatchedSetException mse = new MismatchedSetException( null, input );
            recover( mse );
            throw mse;
          }
          break;
        case 2:
          // C:\\workspace-sts\\Kettle trunk -
          // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
          // FastSimpleGenericEdifactDirectXML.g:129:106:
          // ( RELEASE_CHARACTER ELEMENT_SEPARATOR )

          // C:\\workspace-sts\\Kettle trunk -
          // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
          // FastSimpleGenericEdifactDirectXML.g:129:106:
          // ( RELEASE_CHARACTER ELEMENT_SEPARATOR )
          // C:\\workspace-sts\\Kettle trunk -
          // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
          // FastSimpleGenericEdifactDirectXML.g:129:107:
          // RELEASE_CHARACTER ELEMENT_SEPARATOR

          mRELEASE_CHARACTER();
          mELEMENT_SEPARATOR();
          break;
        case 3:
          // C:\\workspace-sts\\Kettle trunk -
          // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
          // FastSimpleGenericEdifactDirectXML.g:129:144:
          // ( RELEASE_CHARACTER RELEASE_CHARACTER )

          // C:\\workspace-sts\\Kettle trunk -
          // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
          // FastSimpleGenericEdifactDirectXML.g:129:144:
          // ( RELEASE_CHARACTER RELEASE_CHARACTER )
          // C:\\workspace-sts\\Kettle trunk -
          // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
          // FastSimpleGenericEdifactDirectXML.g:129:145:
          // RELEASE_CHARACTER RELEASE_CHARACTER

          mRELEASE_CHARACTER();
          mRELEASE_CHARACTER();
          break;
        case 4:
          // C:\\workspace-sts\\Kettle trunk -
          // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
          // FastSimpleGenericEdifactDirectXML.g:129:182:
          // ( RELEASE_CHARACTER COMPLEX_ELEMENT_ITEM_SEPARATOR )

          // C:\\workspace-sts\\Kettle trunk -
          // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
          // FastSimpleGenericEdifactDirectXML.g:129:182:
          // ( RELEASE_CHARACTER COMPLEX_ELEMENT_ITEM_SEPARATOR )
          // C:\\workspace-sts\\Kettle trunk -
          // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
          // FastSimpleGenericEdifactDirectXML.g:129:183:
          // RELEASE_CHARACTER COMPLEX_ELEMENT_ITEM_SEPARATOR

          mRELEASE_CHARACTER();
          mCOMPLEX_ELEMENT_ITEM_SEPARATOR();
          break;
        case 5:
          // C:\\workspace-sts\\Kettle trunk -
          // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
          // FastSimpleGenericEdifactDirectXML.g:129:233:
          // ( RELEASE_CHARACTER SEGMENT_TERMINATOR )

          mRELEASE_CHARACTER();
          mSEGMENT_TERMINATOR();
          break;

        default:
          if ( cnt1 >= 1 ) {
            break loop1;
          }
          EarlyExitException eee = new EarlyExitException( 1, input );
          throw eee;
      }
      cnt1++;
    } while ( true );
    state.type = _type;
    state.channel = _channel;
  }

  // $ANTLR end "TEXT_DATA"

  public void mTokens() throws RecognitionException {
    // C:\\workspace-sts\\Kettle trunk -
    // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:1:8:
    // ( T__9 | T__10 | T__11 | T__12 | T__13 | T__14 | RELEASE_CHARACTER | ELEMENT_SEPARATOR | SEGMENT_TERMINATOR |
    // COMPLEX_ELEMENT_ITEM_SEPARATOR | TEXT_DATA )
    int alt2 = 11;
    int LA2_0 = input.LA( 1 );

    if ( ( LA2_0 == ' ' ) ) {
      int LA2_1 = input.LA( 2 );

      if ( ( ( LA2_1 >= '\u0000' && LA2_1 <= '&' )
        || ( LA2_1 >= '(' && LA2_1 <= '*' ) || ( LA2_1 >= ',' && LA2_1 <= '9' )
        || ( LA2_1 >= ';' && LA2_1 <= '\uFFFF' ) ) ) {
        alt2 = 11;
      } else {
        alt2 = 1;
      }
    } else if ( ( LA2_0 == 'U' ) ) {
      int LA2_2 = input.LA( 2 );

      if ( ( LA2_2 == 'N' ) ) {
        int LA2_12 = input.LA( 3 );

        if ( ( LA2_12 == 'A' ) ) {
          int LA2_17 = input.LA( 4 );

          if ( ( LA2_17 == ':' ) ) {
            int LA2_18 = input.LA( 5 );

            if ( ( LA2_18 == '+' ) ) {
              int LA2_19 = input.LA( 6 );

              if ( ( LA2_19 == ',' ) ) {
                alt2 = 2;
              } else if ( ( LA2_19 == '.' ) ) {
                alt2 = 3;
              } else {
                NoViableAltException nvae = new NoViableAltException( "", 2, 19, input );

                throw nvae;

              }
            } else {
              NoViableAltException nvae = new NoViableAltException( "", 2, 18, input );

              throw nvae;

            }
          } else {
            alt2 = 11;
          }
        } else {
          alt2 = 11;
        }
      } else {
        alt2 = 11;
      }
    } else if ( ( LA2_0 == '\n' ) ) {
      int LA2_3 = input.LA( 2 );

      if ( ( ( LA2_3 >= '\u0000' && LA2_3 <= '&' )
        || ( LA2_3 >= '(' && LA2_3 <= '*' ) || ( LA2_3 >= ',' && LA2_3 <= '9' )
        || ( LA2_3 >= ';' && LA2_3 <= '\uFFFF' ) ) ) {
        alt2 = 11;
      } else {
        alt2 = 4;
      }
    } else if ( ( LA2_0 == '\r' ) ) {
      int LA2_4 = input.LA( 2 );

      if ( ( ( LA2_4 >= '\u0000' && LA2_4 <= '&' )
        || ( LA2_4 >= '(' && LA2_4 <= '*' ) || ( LA2_4 >= ',' && LA2_4 <= '9' )
        || ( LA2_4 >= ';' && LA2_4 <= '\uFFFF' ) ) ) {
        alt2 = 11;
      } else {
        alt2 = 5;
      }
    } else if ( ( LA2_0 == '\t' ) ) {
      int LA2_5 = input.LA( 2 );

      if ( ( ( LA2_5 >= '\u0000' && LA2_5 <= '&' )
        || ( LA2_5 >= '(' && LA2_5 <= '*' ) || ( LA2_5 >= ',' && LA2_5 <= '9' )
        || ( LA2_5 >= ';' && LA2_5 <= '\uFFFF' ) ) ) {
        alt2 = 11;
      } else {
        alt2 = 6;
      }
    } else if ( ( LA2_0 == '?' ) ) {
      int LA2_6 = input.LA( 2 );

      if ( ( LA2_6 == '\'' || LA2_6 == '+' || LA2_6 == ':' || LA2_6 == '?' ) ) {
        alt2 = 11;
      } else {
        alt2 = 7;
      }
    } else if ( ( LA2_0 == '+' ) ) {
      alt2 = 8;
    } else if ( ( LA2_0 == '\'' ) ) {
      alt2 = 9;
    } else if ( ( LA2_0 == ':' ) ) {
      alt2 = 10;
    } else if ( ( ( LA2_0 >= '\u0000' && LA2_0 <= '\b' )
      || ( LA2_0 >= '\u000B' && LA2_0 <= '\f' ) || ( LA2_0 >= '\u000E' && LA2_0 <= '\u001F' )
      || ( LA2_0 >= '!' && LA2_0 <= '&' ) || ( LA2_0 >= '(' && LA2_0 <= '*' )
      || ( LA2_0 >= ',' && LA2_0 <= '9' )
      || ( LA2_0 >= ';' && LA2_0 <= '>' ) || ( LA2_0 >= '@' && LA2_0 <= 'T' )
      || ( LA2_0 >= 'V' && LA2_0 <= '\uFFFF' ) ) ) {
      alt2 = 11;
    } else {
      NoViableAltException nvae = new NoViableAltException( "", 2, 0, input );

      throw nvae;

    }
    switch ( alt2 ) {
      case 1:
        // C:\\workspace-sts\\Kettle trunk -
        // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
        // FastSimpleGenericEdifactDirectXML.g:1:10:
        // T__9

        mT__9();
        break;
      case 2:
        // C:\\workspace-sts\\Kettle trunk -
        // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
        // FastSimpleGenericEdifactDirectXML.g:1:15:
        // T__10

        mT__10();
        break;
      case 3:
        // C:\\workspace-sts\\Kettle trunk -
        // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
        // FastSimpleGenericEdifactDirectXML.g:1:21:
        // T__11

        mT__11();
        break;
      case 4:
        // C:\\workspace-sts\\Kettle trunk -
        // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
        // FastSimpleGenericEdifactDirectXML.g:1:27:
        // T__12

        mT__12();
        break;
      case 5:
        // C:\\workspace-sts\\Kettle trunk -
        // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
        // FastSimpleGenericEdifactDirectXML.g:1:33:
        // T__13

        mT__13();
        break;
      case 6:
        // C:\\workspace-sts\\Kettle trunk -
        // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
        // FastSimpleGenericEdifactDirectXML.g:1:39:
        // T__14

        mT__14();
        break;
      case 7:
        // C:\\workspace-sts\\Kettle trunk -
        // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
        // FastSimpleGenericEdifactDirectXML.g:1:45:
        // RELEASE_CHARACTER

        mRELEASE_CHARACTER();
        break;
      case 8:
        // C:\\workspace-sts\\Kettle trunk -
        // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
        // FastSimpleGenericEdifactDirectXML.g:1:63:
        // ELEMENT_SEPARATOR

        mELEMENT_SEPARATOR();
        break;
      case 9:
        // C:\\workspace-sts\\Kettle trunk -
        // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
        // FastSimpleGenericEdifactDirectXML.g:1:81:
        // SEGMENT_TERMINATOR

        mSEGMENT_TERMINATOR();
        break;
      case 10:
        // C:\\workspace-sts\\Kettle trunk -
        // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
        // FastSimpleGenericEdifactDirectXML.g:1:100:
        // COMPLEX_ELEMENT_ITEM_SEPARATOR

        mCOMPLEX_ELEMENT_ITEM_SEPARATOR();
        break;
      case 11:
        // C:\\workspace-sts\\Kettle trunk -
        // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
        // FastSimpleGenericEdifactDirectXML.g:1:131:
        // TEXT_DATA

        mTEXT_DATA();
        break;

    }

  }

}
