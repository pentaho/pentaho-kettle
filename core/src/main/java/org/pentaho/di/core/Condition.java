/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.repository.ObjectId;
import org.w3c.dom.Node;

/**
 * This class describes a condition in a general meaning.
 *
 * A condition can either be
 * <p>
 * <p>
 * 1) Atomic (a=10, B='aa')
 * <p>
 * 2) Composite ( NOT Condition1 AND Condition2 OR Condition3 )
 * <p>
 * <p>
 * If the nr of atomic conditions is 0, the condition is atomic, otherwise it's Composit.
 * <p>
 * Precedence doesn't exist. Conditions are evaluated in the order in which they are found.
 * <p>
 * A condition can be negated or not.
 * <p>
 * <p>
 *
 * @author Matt
 * @since 8-06-2004
 */

public class Condition implements Cloneable, XMLInterface {
  public static final String XML_TAG = "condition";

  public static final String[] operators = new String[] { "-", "OR", "AND", "NOT", "OR NOT", "AND NOT", "XOR" };
  public static final int OPERATOR_NONE = 0;
  public static final int OPERATOR_OR = 1;
  public static final int OPERATOR_AND = 2;
  public static final int OPERATOR_NOT = 3;
  public static final int OPERATOR_OR_NOT = 4;
  public static final int OPERATOR_AND_NOT = 5;
  public static final int OPERATOR_XOR = 6;

  public static final String[] functions = new String[] {
    "=", "<>", "<", "<=", ">", ">=", "REGEXP", "IS NULL", "IS NOT NULL", "IN LIST", "CONTAINS", "STARTS WITH",
    "ENDS WITH", "LIKE", "TRUE" };

  public static final int FUNC_EQUAL = 0;
  public static final int FUNC_NOT_EQUAL = 1;
  public static final int FUNC_SMALLER = 2;
  public static final int FUNC_SMALLER_EQUAL = 3;
  public static final int FUNC_LARGER = 4;
  public static final int FUNC_LARGER_EQUAL = 5;
  public static final int FUNC_REGEXP = 6;
  public static final int FUNC_NULL = 7;
  public static final int FUNC_NOT_NULL = 8;
  public static final int FUNC_IN_LIST = 9;
  public static final int FUNC_CONTAINS = 10;
  public static final int FUNC_STARTS_WITH = 11;
  public static final int FUNC_ENDS_WITH = 12;
  public static final int FUNC_LIKE = 13;
  public static final int FUNC_TRUE = 14;

  //
  // These parameters allow for:
  // value = othervalue
  // value = 'A'
  // NOT value = othervalue
  //

  private ObjectId id;

  private boolean negate;
  private int operator;
  private String leftValuename;
  private int function;
  private String rightValuename;
  private ValueMetaAndData rightExact;
  private ObjectId idRightExact;

  private int leftFieldnr;
  private int rightFieldnr;

  private List<Condition> list;

  private String rightString;

  /**
   * Temporary variable, no need to persist this one. Contains the sorted array of strings in an IN LIST condition
   */
  private String[] inList;

  public Condition() {
    list = new ArrayList<>();
    this.operator = OPERATOR_NONE;
    this.negate = false;

    leftFieldnr = -2;
    rightFieldnr = -2;

    id = null;
  }

  public Condition( String valuename, int function, String valuename2, ValueMetaAndData exact ) {
    this();
    this.leftValuename = valuename;
    this.function = function;
    this.rightValuename = valuename2;
    this.rightExact = exact;

    clearFieldPositions();
  }

  public Condition( int operator, String valuename, int function, String valuename2, ValueMetaAndData exact ) {
    this();
    this.operator = operator;
    this.leftValuename = valuename;
    this.function = function;
    this.rightValuename = valuename2;
    this.rightExact = exact;

    clearFieldPositions();
  }

  public Condition( boolean negate, String valuename, int function, String valuename2, ValueMetaAndData exact ) {
    this( valuename, function, valuename2, exact );
    this.negate = negate;
  }

  /**
   * Returns the database ID of this Condition if a repository was used before.
   *
   * @return the ID of the db connection.
   */
  public ObjectId getObjectId() {
    return id;
  }

  /**
   * Set the database ID for this Condition in the repository.
   *
   * @param id
   *          The ID to set on this condition.
   *
   */
  public void setObjectId( ObjectId id ) {
    this.id = id;
  }

  @Override
  public Object clone() {
    Condition retval = null;

    retval = new Condition();
    retval.negate = negate;
    retval.operator = operator;

    if ( isComposite() ) {
      for ( int i = 0; i < nrConditions(); i++ ) {
        Condition c = getCondition( i );
        Condition cCopy = (Condition) c.clone();
        retval.addCondition( cCopy );
      }
    } else {
      retval.negate = negate;
      retval.leftValuename = leftValuename;
      retval.operator = operator;
      retval.rightValuename = rightValuename;
      retval.function = function;
      if ( rightExact != null ) {
        retval.rightExact = (ValueMetaAndData) rightExact.clone();
      } else {
        retval.rightExact = null;
      }
    }

    return retval;
  }

  public void setOperator( int operator ) {
    this.operator = operator;
  }

  public int getOperator() {
    return operator;
  }

  public String getOperatorDesc() {
    return Const.rightPad( operators[operator], 7 );
  }

  public static final int getOperator( String description ) {
    if ( description == null ) {
      return OPERATOR_NONE;
    }

    for ( int i = 1; i < operators.length; i++ ) {
      if ( operators[i].equalsIgnoreCase( Const.trim( description ) ) ) {
        return i;
      }
    }
    return OPERATOR_NONE;
  }

  public static final String[] getOperators() {
    String[] retval = new String[operators.length - 1];
    for ( int i = 1; i < operators.length; i++ ) {
      retval[i - 1] = operators[i];
    }
    return retval;
  }

  public static final String[] getRealOperators() {
    return new String[] { "OR", "AND", "OR NOT", "AND NOT", "XOR" };
  }

  public void setLeftValuename( String leftValuename ) {
    this.leftValuename = leftValuename;
  }

  public String getLeftValuename() {
    return leftValuename;
  }

  public int getFunction() {
    return function;
  }

  public void setFunction( int function ) {
    this.function = function;
  }

  public String getFunctionDesc() {
    return functions[function];
  }

  public static final int getFunction( String description ) {
    for ( int i = 1; i < functions.length; i++ ) {
      if ( functions[i].equalsIgnoreCase( Const.trim( description ) ) ) {
        return i;
      }
    }
    return FUNC_EQUAL;
  }

  public void setRightValuename( String rightValuename ) {
    this.rightValuename = rightValuename;
  }

  public String getRightValuename() {
    return rightValuename;
  }

  public void setRightExact( ValueMetaAndData rightExact ) {
    this.rightExact = rightExact;
  }

  public ValueMetaAndData getRightExact() {
    return rightExact;
  }

  public String getRightExactString() {
    if ( rightExact == null ) {
      return null;
    }
    return rightExact.toString();
  }

  /**
   * Get the id of the RightExact Value in the repository
   *
   * @return The id of the RightExact Value in the repository
   */
  public ObjectId getRightExactID() {
    return idRightExact;
  }

  /**
   * Set the database ID for the RightExact Value in the repository.
   *
   * @param idRightExact
   *          The ID to set on this Value.
   *
   */
  public void setRightExactID( ObjectId idRightExact ) {
    this.idRightExact = idRightExact;
  }

  public boolean isAtomic() {
    return list.isEmpty();
  }

  public boolean isComposite() {
    return !list.isEmpty();
  }

  public boolean isNegated() {
    return negate;
  }

  public void setNegated( boolean negate ) {
    this.negate = negate;
  }

  public void negate() {
    setNegated( !isNegated() );
  }

  /**
   * A condition is empty when the condition is atomic and no left field is specified.
   */
  public boolean isEmpty() {
    return ( isAtomic() && leftValuename == null );
  }

  /**
   * We cache the position of a value in a row. If ever we want to change the rowtype, we need to clear these cached
   * field positions...
   */
  public void clearFieldPositions() {
    leftFieldnr = -2;
    rightFieldnr = -2;
  }

  /**
   * Evaluate the condition...
   *
   * @param rowMeta
   *          the row metadata
   * @param r
   *          the row data
   * @return true if the condition evaluates to true.
   **/
  public boolean evaluate( RowMetaInterface rowMeta, Object[] r ) {
    // Start of evaluate
    boolean retval = false;

    // If we have 0 items in the list, evaluate the current condition
    // Otherwise, evaluate all sub-conditions
    //
    try {
      if ( isAtomic() ) {

        if ( function == FUNC_TRUE ) {
          return !negate;
        }

        // Get fieldnrs left value
        //
        // Check out the fieldnrs if we don't have them...
        if ( leftValuename != null && leftValuename.length() > 0 ) {
          leftFieldnr = rowMeta.indexOfValue( leftValuename );
        }

        // Get fieldnrs right value
        //
        if ( rightValuename != null && rightValuename.length() > 0 ) {
          rightFieldnr = rowMeta.indexOfValue( rightValuename );
        }

        // Get fieldnrs left field
        ValueMetaInterface fieldMeta = null;
        Object field = null;
        if ( leftFieldnr >= 0 ) {
          fieldMeta = rowMeta.getValueMeta( leftFieldnr );
          field = r[ leftFieldnr ];
        } else {
          return false; // no fields to evaluate
        }

        // Get fieldnrs right exact
        ValueMetaInterface fieldMeta2 = rightExact != null ? rightExact.getValueMeta() : null;
        Object field2 = rightExact != null ? rightExact.getValueData() : null;
        if ( field2 == null && rightFieldnr >= 0 ) {
          fieldMeta2 = rowMeta.getValueMeta( rightFieldnr );
          field2 = r[ rightFieldnr ];
        }

        // Evaluate
        switch ( function ) {
          case FUNC_EQUAL:
            retval = ( fieldMeta.compare( field, fieldMeta2, field2 ) == 0 );
            break;
          case FUNC_NOT_EQUAL:
            retval = ( fieldMeta.compare( field, fieldMeta2, field2 ) != 0 );
            break;
          case FUNC_SMALLER:
            if ( fieldMeta.isNull( field ) ) {
              // BACKLOG-18831
              retval = false;
            } else {
              retval = ( fieldMeta.compare( field, fieldMeta2, field2 ) < 0 );
            }
            break;
          case FUNC_SMALLER_EQUAL:
            if ( fieldMeta.isNull( field ) ) {
              retval = false;
            } else {
              retval = ( fieldMeta.compare( field, fieldMeta2, field2 ) <= 0 );
            }
            break;
          case FUNC_LARGER:
            retval = ( fieldMeta.compare( field, fieldMeta2, field2 ) > 0 );
            break;
          case FUNC_LARGER_EQUAL:
            retval = ( fieldMeta.compare( field, fieldMeta2, field2 ) >= 0 );
            break;
          case FUNC_REGEXP:
            if ( fieldMeta.isNull( field ) || field2 == null ) {
              retval = false;
            } else {
              retval =
                Pattern
                  .matches( fieldMeta2.getCompatibleString( field2 ), fieldMeta.getCompatibleString( field ) );
            }
            break;
          case FUNC_NULL:
            retval = ( fieldMeta.isNull( field ) );
            break;
          case FUNC_NOT_NULL:
            retval = ( !fieldMeta.isNull( field ) );
            break;
          case FUNC_IN_LIST:
            // performance reason: create the array first or again when it is against a field and not a constant
            //
            if ( inList == null || rightFieldnr >= 0 ) {
              inList = Const.splitString( fieldMeta2.getString( field2 ), ';', true );
              for ( int i = 0; i < inList.length; i++ ) {
                inList[i] = inList[i] == null ? null : inList[i].replace( "\\", "" );
              }
              Arrays.sort( inList );
            }
            String searchString = fieldMeta.getCompatibleString( field );
            int inIndex = -1;
            if ( searchString != null ) {
              inIndex = Arrays.binarySearch( inList, searchString );
            }
            retval = inIndex >= 0;
            break;
          case FUNC_CONTAINS:
            String fm2CompatibleContains = fieldMeta2.getCompatibleString( field2 );
            retval = Optional.ofNullable( fieldMeta.getCompatibleString( field ) )
              .filter( s -> s.contains( fm2CompatibleContains ) ).isPresent();
            break;
          case FUNC_STARTS_WITH:
            String fm2CompatibleStarts = fieldMeta2.getCompatibleString( field2 );
            retval = Optional.ofNullable( fieldMeta.getCompatibleString( field ) )
              .filter( s -> s.startsWith( fm2CompatibleStarts ) ).isPresent();
            break;
          case FUNC_ENDS_WITH:
            String string = fieldMeta.getCompatibleString( field );
            if ( !Utils.isEmpty( string ) ) {
              if ( rightString == null && field2 != null ) {
                rightString = fieldMeta2.getCompatibleString( field2 );
              }
              if ( rightString != null ) {
                retval = string.endsWith( fieldMeta2.getCompatibleString( field2 ) );
              } else {
                retval = false;
              }
            } else {
              retval = false;
            }
            break;
          case FUNC_LIKE:
            // Converts to a regular expression
            // TODO: optimize the patterns and String replacements
            //
            if ( fieldMeta.isNull( field ) || field2 == null ) {
              retval = false;
            } else {
              String regex = fieldMeta2.getCompatibleString( field2 );
              regex = regex.replace( "%", ".*" );
              regex = regex.replace( "?", "." );
              retval = Pattern.matches( regex, fieldMeta.getCompatibleString( field ) );
            }
            break;
          default:
            break;
        }

        // Only NOT makes sense, the rest doesn't, so ignore!!!!
        // Optionally negate
        //
        if ( isNegated() ) {
          retval = !retval;
        }
      } else {
        // Composite : get first
        Condition cb0 = list.get( 0 );
        retval = cb0.evaluate( rowMeta, r );

        // Loop over the conditions listed below.
        //
        for ( int i = 1; i < list.size(); i++ ) {
          // Composite : #i
          // Get right hand condition
          Condition cb = list.get( i );

          // Evaluate the right hand side of the condition cb.evaluate() within
          // the switch statement
          // because the condition may be short-circuited due to the left hand
          // side (retval)
          switch ( cb.getOperator() ) {
            case Condition.OPERATOR_OR:
              retval = retval || cb.evaluate( rowMeta, r );
              break;
            case Condition.OPERATOR_AND:
              retval = retval && cb.evaluate( rowMeta, r );
              break;
            case Condition.OPERATOR_OR_NOT:
              retval = retval || ( !cb.evaluate( rowMeta, r ) );
              break;
            case Condition.OPERATOR_AND_NOT:
              retval = retval && ( !cb.evaluate( rowMeta, r ) );
              break;
            case Condition.OPERATOR_XOR:
              retval = retval ^ cb.evaluate( rowMeta, r );
              break;
            default:
              break;
          }
        }

        // Composite: optionally negate
        if ( isNegated() ) {
          retval = !retval;
        }
      }
    } catch ( Exception e ) {
      throw new RuntimeException( "Unexpected error evaluation condition [" + toString() + "]", e );
    }

    return retval;
  }

  public void addCondition( Condition cb ) {
    if ( isAtomic() && getLeftValuename() != null ) {
      /*
       * Copy current atomic setup...
       */
      Condition current = new Condition( getLeftValuename(), getFunction(), getRightValuename(), getRightExact() );
      current.setNegated( isNegated() );
      setNegated( false );
      list.add( current );
    } else {
      // Set default operator if not on first position...
      if ( isComposite() && !list.isEmpty() && cb.getOperator() == OPERATOR_NONE ) {
        cb.setOperator( OPERATOR_AND );
      }
    }
    list.add( cb );
  }

  public void addCondition( int idx, Condition cb ) {
    if ( isAtomic() && getLeftValuename() != null ) {
      /*
       * Copy current atomic setup...
       */
      Condition current = new Condition( getLeftValuename(), getFunction(), getRightValuename(), getRightExact() );
      current.setNegated( isNegated() );
      setNegated( false );
      list.add( current );
    } else {
      // Set default operator if not on first position...
      if ( isComposite() && idx > 0 && cb.getOperator() == OPERATOR_NONE ) {
        cb.setOperator( OPERATOR_AND );
      }
    }
    list.add( idx, cb );
  }

  public void removeCondition( int nr ) {
    if ( isComposite() ) {
      Condition c = list.get( nr );
      list.remove( nr );

      // Nothing left or only one condition left: move it to the parent: make it atomic.

      boolean moveUp = isAtomic() || nrConditions() == 1;
      if ( nrConditions() == 1 ) {
        c = getCondition( 0 );
      }

      if ( moveUp ) {
        setLeftValuename( c.getLeftValuename() );
        setFunction( c.getFunction() );
        setRightValuename( c.getRightValuename() );
        setRightExact( c.getRightExact() );
        setNegated( isNegated() ^ c.isNegated() );
      }
    }
  }

  /**
   * This method moves up atomic conditions if there is only one sub-condition.
   *
   * @return true if there was a simplification.
   */
  public boolean simplify() {

    if ( nrConditions() == 1 ) {
      Condition condition = getCondition( 0 );
      if ( condition.isAtomic() ) {
        return simplify( condition, this );
      }
    }

    boolean changed = false;
    for ( int i = 0; i < nrConditions(); i++ ) {
      Condition condition = getCondition( i );
      changed |= condition.simplify();
      if ( i == 0 ) {
        condition.setOperator( OPERATOR_NONE );
      }
    }
    return changed;
  }

  private boolean simplify( Condition condition, Condition parent ) {
    // If condition is atomic
    // AND
    // if parent only contain a single child: simplify
    //
    if ( condition.isAtomic() && parent.nrConditions() == 1 ) {
      parent.setLeftValuename( condition.getLeftValuename() );
      parent.setFunction( condition.getFunction() );
      parent.setRightValuename( condition.getRightValuename() );
      parent.setRightExact( condition.getRightExact() );
      parent.setNegated( condition.isNegated() ^ parent.isNegated() );
      parent.list.clear();
      return true;
    }
    return false;
  }

  public int nrConditions() {
    return list.size();
  }

  public Condition getCondition( int i ) {
    return list.get( i );
  }

  public void setCondition( int i, Condition subCondition ) {
    list.set( i, subCondition );
  }

  @Override
  public String toString() {
    return toString( 0, true, true );
  }

  public String toString( int level, boolean showNegate, boolean showOperator ) {
    StringBuilder retval = new StringBuilder();

    if ( isAtomic() ) {
      for ( int i = 0; i < level; i++ ) {
        retval.append( "  " );
      }

      if ( showOperator && getOperator() != OPERATOR_NONE ) {
        retval.append( getOperatorDesc() ).append( " " );
      } else {
        retval.append( "        " );
      }

      // Atomic is negated?
      if ( isNegated() && ( showNegate || level > 0 ) ) {
        retval.append( "NOT ( " );
      } else {
        retval.append( "      " );
      }

      if ( function == FUNC_TRUE ) {
        retval.append( " TRUE" );
      } else {
        retval.append( leftValuename ).append( " " ).append( getFunctionDesc() );
        if ( function != FUNC_NULL && function != FUNC_NOT_NULL ) {
          if ( rightValuename != null ) {
            retval.append( " " ).append( rightValuename );
          } else {
            retval.append( " [" ).append( getRightExactString() == null ? "" : getRightExactString() ).append( "]" );
          }
        }
      }

      if ( isNegated() && ( showNegate || level > 0 ) ) {
        retval.append( " )" );
      }

      retval.append( Const.CR );
    } else {
      // Group is negated?
      if ( isNegated() && ( showNegate || level > 0 ) ) {
        for ( int i = 0; i < level; i++ ) {
          retval.append( "  " );
        }
        retval.append( "NOT" ).append( Const.CR );
      }
      // Group is preceded by an operator:
      if ( getOperator() != OPERATOR_NONE && ( showOperator || level > 0 ) ) {
        for ( int i = 0; i < level; i++ ) {
          retval.append( "  " );
        }
        retval.append( getOperatorDesc() ).append( Const.CR );
      }
      for ( int i = 0; i < level; i++ ) {
        retval.append( "  " );
      }
      retval.append( "(" ).append( Const.CR );
      for ( int i = 0; i < list.size(); i++ ) {
        Condition cb = list.get( i );
        retval.append( cb.toString( level + 1, true, i > 0 ) );
      }
      for ( int i = 0; i < level; i++ ) {
        retval.append( "  " );
      }
      retval.append( ")" ).append( Const.CR );
    }

    return retval.toString();
  }

  @Override
  public String getXML() throws KettleValueException {
    return getXML( 0 );
  }

  public String getXML( int level ) throws KettleValueException {
    StringBuilder retval = new StringBuilder();
    String indent1 = Const.rightPad( " ", level );
    String indent2 = Const.rightPad( " ", level + 1 );
    String indent3 = Const.rightPad( " ", level + 2 );

    retval.append( indent1 ).append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );

    retval.append( indent2 ).append( XMLHandler.addTagValue( "negated", isNegated() ) );

    if ( getOperator() != OPERATOR_NONE ) {
      retval.append( indent2 ).append( XMLHandler.addTagValue( "operator", Const.rtrim( getOperatorDesc() ) ) );
    }

    if ( isAtomic() ) {
      retval.append( indent2 ).append( XMLHandler.addTagValue( "leftvalue", getLeftValuename() ) );
      retval.append( indent2 ).append( XMLHandler.addTagValue( "function", getFunctionDesc() ) );
      retval.append( indent2 ).append( XMLHandler.addTagValue( "rightvalue", getRightValuename() ) );
      if ( getRightExact() != null ) {
        retval.append( indent2 ).append( getRightExact().getXML() );
      }
    } else {
      retval.append( indent2 ).append( "<conditions>" ).append( Const.CR );
      for ( int i = 0; i < nrConditions(); i++ ) {
        Condition c = getCondition( i );
        retval.append( c.getXML( level + 2 ) );
      }
      retval.append( indent3 ).append( "</conditions>" ).append( Const.CR );
    }

    retval.append( indent2 ).append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );

    return retval.toString();
  }

  public Condition( String xml ) throws KettleXMLException {
    this( XMLHandler.loadXMLString( xml, Condition.XML_TAG ) );
  }

  /**
   * Build a new condition using an XML Document Node
   *
   * @param condnode
   * @throws KettleXMLException
   */
  public Condition( Node condnode ) throws KettleXMLException {
    this();

    list = new ArrayList<>();
    try {
      String strNegated = XMLHandler.getTagValue( condnode, "negated" );
      setNegated( "Y".equalsIgnoreCase( strNegated ) );

      String strOperator = XMLHandler.getTagValue( condnode, "operator" );
      setOperator( getOperator( strOperator ) );

      Node conditions = XMLHandler.getSubNode( condnode, "conditions" );
      int nrconditions = XMLHandler.countNodes( conditions, XML_TAG );
      if ( nrconditions == 0 ) {
        // ATOMIC!
        setLeftValuename( XMLHandler.getTagValue( condnode, "leftvalue" ) );
        setFunction( getFunction( XMLHandler.getTagValue( condnode, "function" ) ) );
        setRightValuename( XMLHandler.getTagValue( condnode, "rightvalue" ) );
        Node exactnode = XMLHandler.getSubNode( condnode, ValueMetaAndData.XML_TAG );
        if ( exactnode != null ) {
          ValueMetaAndData exact = new ValueMetaAndData( exactnode );
          setRightExact( exact );
        }
      } else {
        for ( int i = 0; i < nrconditions; i++ ) {
          Node subcondnode = XMLHandler.getSubNodeByNr( conditions, XML_TAG, i );
          Condition c = new Condition( subcondnode );
          addCondition( c );
        }
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to create condition using xml: " + Const.CR + condnode, e );
    }
  }

  public String[] getUsedFields() {
    Map<String, String> fields = new HashMap<>();
    getUsedFields( fields );
    return fields.keySet().toArray( new String[0] );
  }

  public void getUsedFields( Map<String, String> fields ) {
    if ( isAtomic() ) {
      if ( getLeftValuename() != null ) {
        fields.put( getLeftValuename(), "-" );
      }
      if ( getRightValuename() != null ) {
        fields.put( getRightValuename(), "-" );
      }
    } else {
      for ( int i = 0; i < nrConditions(); i++ ) {
        Condition subc = getCondition( i );
        subc.getUsedFields( fields );
      }
    }
  }

  public List<Condition> getChildren() {
    return list;
  }
}
