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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.RuleReturnScope;
import org.antlr.runtime.TokenStream;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.apache.commons.lang.StringEscapeUtils;

@SuppressWarnings( { "all", "warnings", "unchecked" } )
public class FastSimpleGenericEdifactDirectXMLParser extends Parser {
  public static final String[] tokenNames = new String[] {
    "<invalid>", "<EOR>", "<DOWN>", "<UP>", "COMPLEX_ELEMENT_ITEM_SEPARATOR", "ELEMENT_SEPARATOR",
    "RELEASE_CHARACTER", "SEGMENT_TERMINATOR", "TEXT_DATA", "' '", "'UNA:+,? \\''", "'UNA:+.? \\''", "'\\n'",
    "'\\r'", "'\\t'" };

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
  public Parser[] getDelegates() {
    return new Parser[] {};
  }

  // delegators

  public FastSimpleGenericEdifactDirectXMLParser( TokenStream input ) {
    this( input, new RecognizerSharedState() );
  }

  public FastSimpleGenericEdifactDirectXMLParser( TokenStream input, RecognizerSharedState state ) {
    super( input, state );
  }

  protected StringTemplateGroup templateLib = new StringTemplateGroup(
    "FastSimpleGenericEdifactDirectXMLParserTemplates", AngleBracketTemplateLexer.class );

  public void setTemplateLib( StringTemplateGroup templateLib ) {
    this.templateLib = templateLib;
  }

  public StringTemplateGroup getTemplateLib() {
    return templateLib;
  }

  /**
   * allows convenient multi-value initialization: "new STAttrMap().put(...).put(...)"
   */
  public static class STAttrMap extends HashMap<String, Object> {
    public STAttrMap put( String attrName, Object value ) {
      super.put( attrName, value );
      return this;
    }

    public STAttrMap put( String attrName, int value ) {
      super.put( attrName, new Integer( value ) );
      return this;
    }
  }

  public String[] getTokenNames() {
    return FastSimpleGenericEdifactDirectXMLParser.tokenNames;
  }

  public String getGrammarFileName() {
    return "C:\\workspace-sts\\Kettle trunk - "
      + "restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\"
      + "FastSimpleGenericEdifactDirectXML.g";
  }

  public static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
  public static final String TAG_EDIFACT = "<edifact>\n";
  public static final String TAG_EDIFACT_END = "</edifact>";
  public static final String TAG_ELEMENT = "\t\t<element>\n";
  public static final String TAG_ELEMENT_END = "\t\t</element>\n";
  public static final String TAG_VALUE = "\t\t\t<value>";
  public static final String TAG_VALUE_END = "</value>\n";

  public LinkedList<Object> tagIndexes = new LinkedList<Object>();

  // helper functions to sanitize incoming input
  public String sanitizeText( String txt ) {

    // resolve all RELEASE characters
    if ( txt.indexOf( "?" ) >= 0 ) {
      txt = txt.replace( "?+", "+" );
      txt = txt.replace( "?:", ":" );
      txt = txt.replace( "?'", "'" );
      txt = txt.replace( "??", "?" );
    }

    // enocde XML entities
    return StringEscapeUtils.escapeXml( txt );
  }

  // assume about 8k for an edifact message
  public StringBuilder buf = new StringBuilder( 8192 );

  // helper method for writing tag indexes to the stream
  public void appendIndexes() {

    if ( tagIndexes.size() == 0 ) {
      return;
    }

    // System.out.println(tagIndexes);
    for ( Object i : tagIndexes ) {
      String s = (String) i;
      buf.append( "\t\t<index>" + s + "</index>\n" );
    }
  }

  // error handling overrides -> just exit
  protected void mismatch( IntStream input, int ttype, BitSet follow ) throws RecognitionException {
    throw new MismatchedTokenException( ttype, input );
  }

  public Object recoverFromMismatchedSet( IntStream input, RecognitionException e, BitSet follow ) throws RecognitionException {
    throw e;
  }

  public static class edifact_return extends ParserRuleReturnScope {
    public StringTemplate st;

    public Object getTemplate() {
      return st;
    }

    public String toString() {
      return st == null ? null : st.toString();
    }
  };

  // $ANTLR start "edifact"
  // C:\\workspace-sts\\Kettle trunk -
  // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:77:1:
  // edifact : ( una )? ( segment )* ;
  public final FastSimpleGenericEdifactDirectXMLParser.edifact_return edifact() throws RecognitionException {
    FastSimpleGenericEdifactDirectXMLParser.edifact_return retval =
      new FastSimpleGenericEdifactDirectXMLParser.edifact_return();
    retval.start = input.LT( 1 );

    try {
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:81:4:
      // ( ( una )? ( segment )* )
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:81:6:
      // ( una )? ( segment )*

      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:81:6:
      // ( una )?
      int alt1 = 2;
      int LA1_0 = input.LA( 1 );
      if ( ( ( LA1_0 >= 10 && LA1_0 <= 11 ) ) ) {
        alt1 = 1;
      }
      switch ( alt1 ) {
        case 1:
          // C:\\workspace-sts\\Kettle trunk -
          // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
          // FastSimpleGenericEdifactDirectXML.g:81:6:
          // una

          pushFollow( FOLLOW_una_in_edifact64 );
          una();
          state._fsp--;
          break;

      }
      buf = new StringBuilder( 8192 );
      buf.append( XML_HEAD );
      buf.append( TAG_EDIFACT );
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:83:4:
      // ( segment )*
      loop2: do {
        int alt2 = 2;
        int LA2_0 = input.LA( 1 );

        if ( ( LA2_0 == TEXT_DATA ) ) {
          alt2 = 1;
        }

        switch ( alt2 ) {
          case 1:
            // C:\\workspace-sts\\Kettle trunk -
            // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
            // FastSimpleGenericEdifactDirectXML.g:83:4:
            // segment

            pushFollow( FOLLOW_segment_in_edifact76 );
            segment();
            state._fsp--;
            break;

          default:
            break loop2;
        }
      } while ( true );
      buf.append( TAG_EDIFACT_END );
      retval.stop = input.LT( -1 );

      // System.out.println(buf.toString());

    } catch ( RecognitionException e ) {
      // do not try to recover from parse errors, propagate the error instead
      throw e;
    }
    return retval;
  }

  // $ANTLR end "edifact"

  public static class una_return extends ParserRuleReturnScope {
    public StringTemplate st;

    public Object getTemplate() {
      return st;
    }

    public String toString() {
      return st == null ? null : st.toString();
    }
  };

  // $ANTLR start "una"
  // C:\\workspace-sts\\Kettle trunk -
  // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:89:1:
  // una : ( 'UNA:+.? \\'' | 'UNA:+,? \\'' );
  public final FastSimpleGenericEdifactDirectXMLParser.una_return una() throws RecognitionException {
    FastSimpleGenericEdifactDirectXMLParser.una_return retval =
      new FastSimpleGenericEdifactDirectXMLParser.una_return();
    retval.start = input.LT( 1 );

    try {
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:89:7:
      // ( 'UNA:+.? \\'' | 'UNA:+,? \\'' )
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:

      if ( ( input.LA( 1 ) >= 10 && input.LA( 1 ) <= 11 ) ) {
        input.consume();
        state.errorRecovery = false;
      } else {
        MismatchedSetException mse = new MismatchedSetException( null, input );
        throw mse;
      }
      retval.stop = input.LT( -1 );

    } catch ( RecognitionException e ) {
      // do not try to recover from parse errors, propagate the error instead
      throw e;
    }

    return retval;
  }

  // $ANTLR end "una"

  public static class segment_return extends ParserRuleReturnScope {
    public StringTemplate st;

    public Object getTemplate() {
      return st;
    }

    public String toString() {
      return st == null ? null : st.toString();
    }
  };

  // $ANTLR start "segment"
  // C:\\workspace-sts\\Kettle trunk -
  // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:91:1:
  // segment : tag ( data_element )* SEGMENT_TERMINATOR ( ' ' | '\\n' | '\\r' | '\\t' )* ;
  public final FastSimpleGenericEdifactDirectXMLParser.segment_return segment() throws RecognitionException {
    FastSimpleGenericEdifactDirectXMLParser.segment_return retval =
      new FastSimpleGenericEdifactDirectXMLParser.segment_return();
    retval.start = input.LT( 1 );

    FastSimpleGenericEdifactDirectXMLParser.tag_return tag1 = null;

    try {
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:91:11:
      // ( tag ( data_element )* SEGMENT_TERMINATOR ( ' ' | '\\n' | '\\r' | '\\t' )* )
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:91:13:
      // tag ( data_element )* SEGMENT_TERMINATOR ( ' ' | '\\n' | '\\r' | '\\t' )*

      pushFollow( FOLLOW_tag_in_segment107 );
      tag1 = tag();
      state._fsp--;
      buf.append( "\t<" + ( tag1 != null ? tag1.name : null ) + ">\n" );
      appendIndexes();
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:92:4:
      // ( data_element )*
      loop3: do {
        int alt3 = 2;
        int LA3_0 = input.LA( 1 );

        if ( ( LA3_0 == ELEMENT_SEPARATOR ) ) {
          alt3 = 1;
        }

        switch ( alt3 ) {
          case 1:
            // C:\\workspace-sts\\Kettle trunk -
            // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
            // FastSimpleGenericEdifactDirectXML.g:92:4:
            // data_element

            pushFollow( FOLLOW_data_element_in_segment114 );
            data_element();
            state._fsp--;
            break;

          default:
            break loop3;
        }
      } while ( true );
      match( input, SEGMENT_TERMINATOR, FOLLOW_SEGMENT_TERMINATOR_in_segment117 );
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:92:37:
      // ( ' ' | '\\n' | '\\r' | '\\t' )*
      loop4: do {
        int alt4 = 2;
        int LA4_0 = input.LA( 1 );

        if ( ( LA4_0 == 9 || ( LA4_0 >= 12 && LA4_0 <= 14 ) ) ) {
          alt4 = 1;
        }

        switch ( alt4 ) {
          case 1:
            // C:\\workspace-sts\\Kettle trunk -
            // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
            // FastSimpleGenericEdifactDirectXML.g:

            if ( input.LA( 1 ) == 9 || ( input.LA( 1 ) >= 12 && input.LA( 1 ) <= 14 ) ) {
              input.consume();
              state.errorRecovery = false;
            } else {
              MismatchedSetException mse = new MismatchedSetException( null, input );
              throw mse;
            }
            break;

          default:
            break loop4;
        }
      } while ( true );
      buf.append( "\t</" + ( tag1 != null ? tag1.name : null ) + ">\n" );
      retval.stop = input.LT( -1 );

    } catch ( RecognitionException e ) {
      // do not try to recover from parse errors, propagate the error instead
      throw e;
    }

    return retval;
  }

  // $ANTLR end "segment"

  public static class data_element_return extends ParserRuleReturnScope {
    public StringTemplate st;

    public Object getTemplate() {
      return st;
    }

    public String toString() {
      return st == null ? null : st.toString();
    }
  };

  // $ANTLR start "data_element"
  // C:\\workspace-sts\\Kettle trunk -
  // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:96:1:
  // data_element : ss data_element_payload ;
  public final FastSimpleGenericEdifactDirectXMLParser.data_element_return data_element() throws RecognitionException {
    FastSimpleGenericEdifactDirectXMLParser.data_element_return retval =
      new FastSimpleGenericEdifactDirectXMLParser.data_element_return();
    retval.start = input.LT( 1 );

    try {
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:96:15:
      // ( ss data_element_payload )
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:96:17:
      // ss data_element_payload

      pushFollow( FOLLOW_ss_in_data_element143 );
      ss();
      state._fsp--;
      pushFollow( FOLLOW_data_element_payload_in_data_element145 );
      data_element_payload();
      state._fsp--;
      retval.stop = input.LT( -1 );

    } catch ( RecognitionException e ) {
      // do not try to recover from parse errors, propagate the error instead
      throw e;
    }
    return retval;
  }

  // $ANTLR end "data_element"

  public static class data_element_payload_return extends ParserRuleReturnScope {
    public StringTemplate st;

    public Object getTemplate() {
      return st;
    }

    public String toString() {
      return st == null ? null : st.toString();
    }
  };

  // $ANTLR start "data_element_payload"
  // C:\\workspace-sts\\Kettle trunk -
  // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:98:1:
  // data_element_payload : ( composite_data_item ds )* composite_data_item ;
  public final FastSimpleGenericEdifactDirectXMLParser.data_element_payload_return data_element_payload() throws RecognitionException {
    FastSimpleGenericEdifactDirectXMLParser.data_element_payload_return retval =
      new FastSimpleGenericEdifactDirectXMLParser.data_element_payload_return();
    retval.start = input.LT( 1 );

    try {
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:98:22:
      // ( ( composite_data_item ds )* composite_data_item )
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:98:24:
      // ( composite_data_item ds )* composite_data_item

      buf.append( TAG_ELEMENT );
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:99:4:
      // ( composite_data_item ds )*
      loop5: do {
        int alt5 = 2;
        int LA5_0 = input.LA( 1 );

        if ( ( LA5_0 == TEXT_DATA ) ) {
          int LA5_1 = input.LA( 2 );

          if ( ( LA5_1 == COMPLEX_ELEMENT_ITEM_SEPARATOR ) ) {
            alt5 = 1;
          }

        } else if ( ( LA5_0 == COMPLEX_ELEMENT_ITEM_SEPARATOR ) ) {
          alt5 = 1;
        }

        switch ( alt5 ) {
          case 1:
            // C:\\workspace-sts\\Kettle trunk -
            // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
            // FastSimpleGenericEdifactDirectXML.g:99:5:
            // composite_data_item ds

            pushFollow( FOLLOW_composite_data_item_in_data_element_payload160 );
            composite_data_item();
            state._fsp--;
            pushFollow( FOLLOW_ds_in_data_element_payload162 );
            ds();
            state._fsp--;
            break;

          default:
            break loop5;
        }
      } while ( true );
      pushFollow( FOLLOW_composite_data_item_in_data_element_payload166 );
      composite_data_item();
      state._fsp--;
      buf.append( TAG_ELEMENT_END );
      retval.stop = input.LT( -1 );

    } catch ( RecognitionException e ) {
      // do not try to recover from parse errors, propagate the error instead
      throw e;
    }
    return retval;
  }

  // $ANTLR end "data_element_payload"

  public static class composite_data_item_return extends ParserRuleReturnScope {
    public StringTemplate st;

    public Object getTemplate() {
      return st;
    }

    public String toString() {
      return st == null ? null : st.toString();
    }
  };

  // $ANTLR start "composite_data_item"
  // C:\\workspace-sts\\Kettle trunk -
  // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:102:1:
  // composite_data_item : composite_data_item_val ;
  public final FastSimpleGenericEdifactDirectXMLParser.composite_data_item_return composite_data_item() throws RecognitionException {
    FastSimpleGenericEdifactDirectXMLParser.composite_data_item_return retval =
      new FastSimpleGenericEdifactDirectXMLParser.composite_data_item_return();
    retval.start = input.LT( 1 );

    FastSimpleGenericEdifactDirectXMLParser.composite_data_item_val_return composite_data_item_val2 = null;

    try {
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:102:21:
      // ( composite_data_item_val )
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:102:23:
      // composite_data_item_val

      pushFollow( FOLLOW_composite_data_item_val_in_composite_data_item180 );
      composite_data_item_val2 = composite_data_item_val();
      state._fsp--;
      buf.append( TAG_VALUE );
      buf.append( sanitizeText( ( composite_data_item_val2 != null ? input.toString(
        composite_data_item_val2.start, composite_data_item_val2.stop ) : null ) ) );
      buf.append( TAG_VALUE_END );
      retval.stop = input.LT( -1 );

    } catch ( RecognitionException e ) {
      // do not try to recover from parse errors, propagate the error instead
      throw e;
    }
    return retval;
  }

  // $ANTLR end "composite_data_item"

  public static class composite_data_item_val_return extends ParserRuleReturnScope {
    public StringTemplate st;

    public Object getTemplate() {
      return st;
    }

    public String toString() {
      return st == null ? null : st.toString();
    }
  };

  // $ANTLR start "composite_data_item_val"
  // C:\\workspace-sts\\Kettle trunk -
  // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:105:1:
  // composite_data_item_val : ( txt |);
  public final FastSimpleGenericEdifactDirectXMLParser.composite_data_item_val_return composite_data_item_val() throws RecognitionException {
    FastSimpleGenericEdifactDirectXMLParser.composite_data_item_val_return retval =
      new FastSimpleGenericEdifactDirectXMLParser.composite_data_item_val_return();
    retval.start = input.LT( 1 );

    try {
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:105:25:
      // ( txt |)
      int alt6 = 2;
      int LA6_0 = input.LA( 1 );

      if ( ( LA6_0 == TEXT_DATA ) ) {
        alt6 = 1;
      } else if ( ( ( LA6_0 >= COMPLEX_ELEMENT_ITEM_SEPARATOR && LA6_0 <= ELEMENT_SEPARATOR )
      || LA6_0 == SEGMENT_TERMINATOR ) ) {
        alt6 = 2;
      } else {
        NoViableAltException nvae = new NoViableAltException( "", 6, 0, input );

        throw nvae;

      }
      switch ( alt6 ) {
        case 1:
          // C:\\workspace-sts\\Kettle trunk -
          // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
          // FastSimpleGenericEdifactDirectXML.g:105:27:
          // txt

          pushFollow( FOLLOW_txt_in_composite_data_item_val193 );
          txt();
          state._fsp--;
          break;
        case 2:
          // C:\\workspace-sts\\Kettle trunk -
          // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
          // FastSimpleGenericEdifactDirectXML.g:105:31:
          break;

      }
      retval.stop = input.LT( -1 );

    } catch ( RecognitionException e ) {
      // do not try to recover from parse errors, propagate the error instead
      throw e;
    }
    return retval;
  }

  // $ANTLR end "composite_data_item_val"

  public static class tag_return extends ParserRuleReturnScope {
    public String name;
    public List<Object> indexes;
    public StringTemplate st;

    public Object getTemplate() {
      return st;
    }

    public String toString() {
      return st == null ? null : st.toString();
    }
  };

  // $ANTLR start "tag"
  // C:\\workspace-sts\\Kettle trunk -
  // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:108:1:
  // tag returns [String name, List indexes] : tag_name ( ds i+= tag_index_id )* ;
  public final FastSimpleGenericEdifactDirectXMLParser.tag_return tag() throws RecognitionException {
    FastSimpleGenericEdifactDirectXMLParser.tag_return retval =
      new FastSimpleGenericEdifactDirectXMLParser.tag_return();
    retval.start = input.LT( 1 );

    List<Object> list_i = null;
    FastSimpleGenericEdifactDirectXMLParser.tag_name_return tag_name3 = null;

    RuleReturnScope i = null;
    try {
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:108:41:
      // ( tag_name ( ds i+= tag_index_id )* )
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:108:43:
      // tag_name ( ds i+= tag_index_id )*

      pushFollow( FOLLOW_tag_name_in_tag208 );
      tag_name3 = tag_name();
      state._fsp--;
      tagIndexes.clear();
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:108:74:
      // ( ds i+= tag_index_id )*
      loop7: do {
        int alt7 = 2;
        int LA7_0 = input.LA( 1 );

        if ( ( LA7_0 == COMPLEX_ELEMENT_ITEM_SEPARATOR ) ) {
          alt7 = 1;
        }

        switch ( alt7 ) {
          case 1:
            // C:\\workspace-sts\\Kettle trunk -
            // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
            // FastSimpleGenericEdifactDirectXML.g:108:75:
            // ds i+= tag_index_id

            pushFollow( FOLLOW_ds_in_tag213 );
            ds();
            state._fsp--;
            pushFollow( FOLLOW_tag_index_id_in_tag217 );
            i = tag_index_id();
            state._fsp--;
            if ( list_i == null ) {
              list_i = new ArrayList<Object>();
            }
            list_i.add( i.getTemplate() );
            break;

          default:
            break loop7;
        }
      } while ( true );
      retval.name = ( tag_name3 != null ? input.toString( tag_name3.start, tag_name3.stop ) : null ).trim();
      retval.stop = input.LT( -1 );

    } catch ( RecognitionException e ) {
      // do not try to recover from parse errors, propagate the error instead
      throw e;
    }
    return retval;
  }

  // $ANTLR end "tag"

  public static class tag_name_return extends ParserRuleReturnScope {
    public StringTemplate st;

    public Object getTemplate() {
      return st;
    }

    public String toString() {
      return st == null ? null : st.toString();
    }
  };

  // $ANTLR start "tag_name"
  // C:\\workspace-sts\\Kettle trunk -
  // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:112:1:
  // tag_name : txt ;
  public final FastSimpleGenericEdifactDirectXMLParser.tag_name_return tag_name() throws RecognitionException {
    FastSimpleGenericEdifactDirectXMLParser.tag_name_return retval =
      new FastSimpleGenericEdifactDirectXMLParser.tag_name_return();
    retval.start = input.LT( 1 );

    try {
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:112:11:
      // ( txt )
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:112:13:
      // txt

      pushFollow( FOLLOW_txt_in_tag_name239 );
      txt();
      state._fsp--;
      retval.stop = input.LT( -1 );

    } catch ( RecognitionException e ) {
      // do not try to recover from parse errors, propagate the error instead
      throw e;
    }
    return retval;
  }

  // $ANTLR end "tag_name"

  public static class tag_index_id_return extends ParserRuleReturnScope {
    public StringTemplate st;

    public Object getTemplate() {
      return st;
    }

    public String toString() {
      return st == null ? null : st.toString();
    }
  };

  // $ANTLR start "tag_index_id"
  // C:\\workspace-sts\\Kettle trunk -
  // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:115:1:
  // tag_index_id : tag_index_id_val ;
  public final FastSimpleGenericEdifactDirectXMLParser.tag_index_id_return tag_index_id() throws RecognitionException {
    FastSimpleGenericEdifactDirectXMLParser.tag_index_id_return retval =
      new FastSimpleGenericEdifactDirectXMLParser.tag_index_id_return();
    retval.start = input.LT( 1 );

    FastSimpleGenericEdifactDirectXMLParser.tag_index_id_val_return tag_index_id_val4 = null;

    try {
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:115:15:
      // ( tag_index_id_val )
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:115:17:
      // tag_index_id_val

      pushFollow( FOLLOW_tag_index_id_val_in_tag_index_id249 );
      tag_index_id_val4 = tag_index_id_val();
      state._fsp--;
      tagIndexes.add( ( tag_index_id_val4 != null ? input.toString(
        tag_index_id_val4.start, tag_index_id_val4.stop ) : null ) );
      retval.stop = input.LT( -1 );

    } catch ( RecognitionException e ) {
      // do not try to recover from parse errors, propagate the error instead
      throw e;
    }
    return retval;
  }

  // $ANTLR end "tag_index_id"

  public static class tag_index_id_val_return extends ParserRuleReturnScope {
    public StringTemplate st;

    public Object getTemplate() {
      return st;
    }

    public String toString() {
      return st == null ? null : st.toString();
    }
  };

  // $ANTLR start "tag_index_id_val"
  // C:\\workspace-sts\\Kettle trunk -
  // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:116:1:
  // tag_index_id_val : ( txt |);
  public final FastSimpleGenericEdifactDirectXMLParser.tag_index_id_val_return tag_index_id_val() throws RecognitionException {
    FastSimpleGenericEdifactDirectXMLParser.tag_index_id_val_return retval =
      new FastSimpleGenericEdifactDirectXMLParser.tag_index_id_val_return();
    retval.start = input.LT( 1 );

    try {
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:116:18:
      // ( txt |)
      int alt8 = 2;
      int LA8_0 = input.LA( 1 );

      if ( ( LA8_0 == TEXT_DATA ) ) {
        alt8 = 1;
      } else if ( ( ( LA8_0 >= COMPLEX_ELEMENT_ITEM_SEPARATOR && LA8_0 <= ELEMENT_SEPARATOR )
      || LA8_0 == SEGMENT_TERMINATOR ) ) {
        alt8 = 2;
      } else {
        NoViableAltException nvae = new NoViableAltException( "", 8, 0, input );

        throw nvae;

      }
      switch ( alt8 ) {
        case 1:
          // C:\\workspace-sts\\Kettle trunk -
          // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
          // FastSimpleGenericEdifactDirectXML.g:116:20:
          // txt

          pushFollow( FOLLOW_txt_in_tag_index_id_val258 );
          txt();
          state._fsp--;
          break;
        case 2:
          // C:\\workspace-sts\\Kettle trunk -
          // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
          // FastSimpleGenericEdifactDirectXML.g:116:24:
          break;

      }
      retval.stop = input.LT( -1 );

    } catch ( RecognitionException e ) {
      // do not try to recover from parse errors, propagate the error instead
      throw e;
    }
    return retval;
  }

  // $ANTLR end "tag_index_id_val"

  public static class ds_return extends ParserRuleReturnScope {
    public StringTemplate st;

    public Object getTemplate() {
      return st;
    }

    public String toString() {
      return st == null ? null : st.toString();
    }
  };

  // $ANTLR start "ds"
  // C:\\workspace-sts\\Kettle trunk -
  // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:119:1:
  // ds : COMPLEX_ELEMENT_ITEM_SEPARATOR ;
  public final FastSimpleGenericEdifactDirectXMLParser.ds_return ds() throws RecognitionException {
    FastSimpleGenericEdifactDirectXMLParser.ds_return retval =
      new FastSimpleGenericEdifactDirectXMLParser.ds_return();
    retval.start = input.LT( 1 );

    try {
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:119:6:
      // ( COMPLEX_ELEMENT_ITEM_SEPARATOR )
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:119:8:
      // COMPLEX_ELEMENT_ITEM_SEPARATOR

      match( input, COMPLEX_ELEMENT_ITEM_SEPARATOR, FOLLOW_COMPLEX_ELEMENT_ITEM_SEPARATOR_in_ds271 );
      retval.stop = input.LT( -1 );

    } catch ( RecognitionException e ) {
      // do not try to recover from parse errors, propagate the error instead
      throw e;
    }
    return retval;
  }

  // $ANTLR end "ds"

  public static class ss_return extends ParserRuleReturnScope {
    public StringTemplate st;

    public Object getTemplate() {
      return st;
    }

    public String toString() {
      return st == null ? null : st.toString();
    }
  };

  // $ANTLR start "ss"
  // C:\\workspace-sts\\Kettle trunk -
  // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:120:1:
  // ss : ELEMENT_SEPARATOR ;
  public final FastSimpleGenericEdifactDirectXMLParser.ss_return ss() throws RecognitionException {
    FastSimpleGenericEdifactDirectXMLParser.ss_return retval =
      new FastSimpleGenericEdifactDirectXMLParser.ss_return();
    retval.start = input.LT( 1 );

    try {
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:120:6:
      // ( ELEMENT_SEPARATOR )
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:120:8:
      // ELEMENT_SEPARATOR

      match( input, ELEMENT_SEPARATOR, FOLLOW_ELEMENT_SEPARATOR_in_ss280 );
      retval.stop = input.LT( -1 );

    } catch ( RecognitionException e ) {
      // do not try to recover from parse errors, propagate the error instead
      throw e;
    }
    return retval;
  }

  // $ANTLR end "ss"

  public static class txt_return extends ParserRuleReturnScope {
    public StringTemplate st;

    public Object getTemplate() {
      return st;
    }

    public String toString() {
      return st == null ? null : st.toString();
    }
  };

  // $ANTLR start "txt"
  // C:\\workspace-sts\\Kettle trunk -
  // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\FastSimpleGenericEdifactDirectXML.g:121:1:
  // txt : TEXT_DATA ;
  public final FastSimpleGenericEdifactDirectXMLParser.txt_return txt() throws RecognitionException {
    FastSimpleGenericEdifactDirectXMLParser.txt_return retval =
      new FastSimpleGenericEdifactDirectXMLParser.txt_return();
    retval.start = input.LT( 1 );

    try {
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:121:7:
      // ( TEXT_DATA )
      // C:\\workspace-sts\\Kettle trunk -
      // restruct\\engine\\src\\org\\pentaho\\di\\trans\\steps\\edi2xml\\grammar\\
      // FastSimpleGenericEdifactDirectXML.g:121:9:
      // TEXT_DATA

      match( input, TEXT_DATA, FOLLOW_TEXT_DATA_in_txt289 );
      retval.stop = input.LT( -1 );

    } catch ( RecognitionException e ) {
      // do not try to recover from parse errors, propagate the error instead
      throw e;
    }
    return retval;
  }

  // $ANTLR end "txt"

  // Delegated rules

  public static final BitSet FOLLOW_una_in_edifact64 = new BitSet( new long[] { 0x0000000000000102L } );
  public static final BitSet FOLLOW_segment_in_edifact76 = new BitSet( new long[] { 0x0000000000000102L } );
  public static final BitSet FOLLOW_tag_in_segment107 = new BitSet( new long[] { 0x00000000000000A0L } );
  public static final BitSet FOLLOW_data_element_in_segment114 = new BitSet( new long[] { 0x00000000000000A0L } );
  public static final BitSet FOLLOW_SEGMENT_TERMINATOR_in_segment117 = new BitSet(
    new long[] { 0x0000000000007202L } );
  public static final BitSet FOLLOW_ss_in_data_element143 = new BitSet( new long[] { 0x0000000000000100L } );
  public static final BitSet FOLLOW_data_element_payload_in_data_element145 = new BitSet(
    new long[] { 0x0000000000000002L } );
  public static final BitSet FOLLOW_composite_data_item_in_data_element_payload160 = new BitSet(
    new long[] { 0x0000000000000010L } );
  public static final BitSet FOLLOW_ds_in_data_element_payload162 =
    new BitSet( new long[] { 0x0000000000000100L } );
  public static final BitSet FOLLOW_composite_data_item_in_data_element_payload166 = new BitSet(
    new long[] { 0x0000000000000002L } );
  public static final BitSet FOLLOW_composite_data_item_val_in_composite_data_item180 = new BitSet(
    new long[] { 0x0000000000000002L } );
  public static final BitSet FOLLOW_txt_in_composite_data_item_val193 = new BitSet(
    new long[] { 0x0000000000000002L } );
  public static final BitSet FOLLOW_tag_name_in_tag208 = new BitSet( new long[] { 0x0000000000000012L } );
  public static final BitSet FOLLOW_ds_in_tag213 = new BitSet( new long[] { 0x0000000000000100L } );
  public static final BitSet FOLLOW_tag_index_id_in_tag217 = new BitSet( new long[] { 0x0000000000000012L } );
  public static final BitSet FOLLOW_txt_in_tag_name239 = new BitSet( new long[] { 0x0000000000000002L } );
  public static final BitSet FOLLOW_tag_index_id_val_in_tag_index_id249 = new BitSet(
    new long[] { 0x0000000000000002L } );
  public static final BitSet FOLLOW_txt_in_tag_index_id_val258 = new BitSet( new long[] { 0x0000000000000002L } );
  public static final BitSet FOLLOW_COMPLEX_ELEMENT_ITEM_SEPARATOR_in_ds271 = new BitSet(
    new long[] { 0x0000000000000002L } );
  public static final BitSet FOLLOW_ELEMENT_SEPARATOR_in_ss280 = new BitSet( new long[] { 0x0000000000000002L } );
  public static final BitSet FOLLOW_TEXT_DATA_in_txt289 = new BitSet( new long[] { 0x0000000000000002L } );

}
