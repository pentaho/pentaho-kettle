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

package org.pentaho.di.compatibility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Node;

/**
 * This class describes a single row in a stream of data. A row is a array/list of Value objects.
 *
 * Note that most methods in this class assume that a value with a certain name only occurs once in the Row.
 *
 * @author Matt
 * @since Beginning 2003.
 * @see Value
 *
 */
public class Row implements XMLInterface, Comparable<Row>, Serializable {
  private static Class<?> PKG = Row.class; // for i18n purposes, needed by Translator2!!

  public static final String XML_TAG = "row";

  private final List<Value> list = new ArrayList<Value>();
  public static final long serialVersionUID = 0x8D8EA0264F7A1C30L;

  /** Ignore this row? */
  private boolean ignore;

  private List<ValueUsedListener> usedValueListeners = new ArrayList<ValueUsedListener>();

  /**
   * Create a new empty row (with 0 values)
   */
  public Row() {
    ignore = false;
  }

  /**
   * Creates a new row as a duplicate of the given row. The values are copied one by one into new values.
   *
   * @param r
   *          The row to be copied.
   */
  // Copy values in row!
  public Row( Row r ) {
    this();

    for ( int i = 0; i < r.size(); i++ ) {
      addValue( new Value( r.getValue( i ) ) );
    }
    setIgnore( r.isIgnored() );
  }

  /**
   * Get the value on a given position in the row.
   *
   * @param index
   *          The position to look for
   * @return The Value on in the given index
   */
  public Value getValue( int index ) {
    Value value = list.get( index );

    // Fire off the used value listeners
    //
    for ( ValueUsedListener listener : usedValueListeners ) {
      listener.valueIsUsed( index, value );
    }

    return value;
  }

  /**
   * Add a value after the last value of the row
   *
   * @param v
   *          The value to add to the row
   */
  public void addValue( Value v ) {
    list.add( v );
  }

  /**
   * Set the value on index idx.
   *
   * @param idx
   *          The index
   * @param v
   *          The value to set
   */
  public void setValue( int idx, Value v ) {
    list.set( idx, v );
  }

  /**
   * Add a value on a certain location in the row.
   *
   * @param idx
   *          The index where the value needs to be put in the row
   * @param v
   *          The value to add to the row
   */
  public void addValue( int idx, Value v ) {
    list.add( idx, v );
  }

  /**
   * Add an object to the row.
   *
   * @param obj
   *          the object to add
   *
   * @deprecated
   */
  @Deprecated
  public void add( Object obj ) {
    list.add( (Value) obj );
  }

  /**
   * Get an object from the row.
   *
   * @param idx
   *          the index to get the object from
   * @return the object
   *
   * @deprecated
   */
  @Deprecated
  public Object get( int idx ) {
    return list.get( idx );
  }

  /**
   * Remove an object in the row on index idx.
   *
   * @param idx
   *          The object to remove
   *
   * @deprecated
   */
  @Deprecated
  public void remove( int idx ) {
    list.remove( idx );
  }

  /**
   * Remove a value with a given name from the row.
   *
   * @param valname
   *          The value name to remove from this row
   *
   * @return true if the value was found and removed, false if the value wasn't found.
   */
  public boolean removeValue( String valname ) {
    int idx = searchValueIndex( valname );
    if ( idx < 0 ) {
      return false;
    }
    list.remove( idx );
    return true;
  }

  /**
   * Remove a value on a certain index.
   *
   * @param idx
   *          the index to remove in the row
   */
  public void removeValue( int idx ) {
    list.remove( idx );
  }

  /**
   * Removes all values from the row.
   */
  public void clear() {
    list.clear();
  }

  /**
   * Add all the values of row r to the Row.
   *
   * @param r
   *          The row to be added to this row.
   */
  public void addRow( Row r ) {
    if ( r == null ) {
      return;
    }

    int i;
    for ( i = 0; i < r.size(); i++ ) {
      Value v1 = r.getValue( i );
      addValue( v1 );
    }
  }

  /**
   * Merge the values of row r to this Row. Merge means: only the values that are not yet in the row are added
   * (comparing on the value name).
   *
   * @param r
   *          The row to be merged with this row
   */
  public void mergeRow( Row r ) {
    if ( r == null ) {
      return;
    }

    for ( int x = 0; x < r.size(); x++ ) {
      Value field = r.getValue( x );
      if ( searchValue( field.getName() ) == null ) {
        addValue( field ); // Not in list yet: add
      }
    }
  }

  /**
   * Merge the data of row r to this Row. That means: All fields in row r that do exist in this row (same name and same
   * type) and have non-empty values will have their values written into this row, if the value of that field is empty
   * in this row.
   *
   * @param r
   *          The row to be merged with this row
   */
  public void mergeData( Row r ) {
    if ( r == null ) {
      return;
    }

    for ( int x = 0; x < r.size(); x++ ) {
      Value other = r.getValue( x );
      Value value = searchValue( other.getName() );
      if ( value != null ) {
        value.merge( other );
      }
    }
  }

  /**
   * Search the Value by name in the row, return the Values index.
   *
   * @param name
   *          the value name to search for.
   * @return the index of the value with the given name, -1 is nothing was found.
   */
  public int searchValueIndex( String name ) {
    if ( name == null ) {
      return -1;
    }

    for ( int i = 0; i < size(); i++ ) {
      Value v = getValue( i );
      if ( v.getName().equalsIgnoreCase( name ) ) {
        return i;
      }
    }

    return -1;
  }

  /**
   * Search the Value by name in the row.
   *
   * @param name
   *          the value name to search for
   * @return the Value with the given name, null if nothing was found.
   */
  public Value searchValue( String name ) {
    if ( name == null ) {
      return null;
    }

    for ( int i = 0; i < size(); i++ ) {
      Value v = getValue( i );

      String nm = v.getName();
      if ( nm != null && nm.equalsIgnoreCase( name ) ) {
        return v;
      }
    }

    return null;
  }

  /**
   * Return number of Values in Row.
   *
   * @return number of Values.
   */
  public int size() {
    return list.size();
  }

  /**
   * Print the names and the String representations of the values of the Values in row to stdout.
   */
  public void print() {
    int i;
    for ( i = 0; i < size(); i++ ) {
      System.out.println( "Element[" + i + "] = [" + getValue( i ).getName() + "] = " + getValue( i ).toString() );
    }
  }

  /**
   * Convert the row to a String representation.
   *
   * @return the row as a String.
   */
  @Override
  public String toString() {
    StringBuilder retval = new StringBuilder( 128 );

    retval.append( '[' );
    for ( int i = 0; i < size(); i++ ) {
      Value value = getValue( i );

      if ( i != 0 ) {
        retval.append( ", " );
      }
      if ( value != null ) {
        retval.append( value.getName() ).append( '=' ).append( value.toString( false ) );
      } else {
        retval.append( "NULL" );
      }
    }
    retval.append( ']' );

    return retval.toString();
  }

  /**
   * Return the meta-data of a row as a String.
   *
   * @return the meta-data of a row as a String
   */
  public String toStringMeta() {
    int i;
    StringBuilder retval = new StringBuilder( 128 );

    retval.append( '[' );
    for ( i = 0; i < size(); i++ ) {
      if ( i != 0 ) {
        retval.append( ", " );
      }
      if ( getValue( i ) != null ) {
        Value v = getValue( i );
        retval.append( v.getName() ).append( '(' );
        retval.append( v.getTypeDesc() );
        if ( v.getLength() > 0 ) {
          retval.append( '(' );
          retval.append( v.getLength() );
          if ( v.getPrecision() > 0 ) {
            retval.append( ',' ).append( v.getPrecision() );
          }
          retval.append( ')' );
        }
        retval.append( ')' );
      } else {
        retval.append( "NULL" );
      }
    }
    retval.append( ']' );

    return retval.toString();
  }

  /**
   * Marks this row as to be ignored by the next steps.
   */
  public void setIgnore() {
    ignore = true;
  }

  /**
   * Marks this row as to be ignored or not by the next steps.
   *
   * @param i
   *          true: ignore this row, false: don't ignore.
   */
  public void setIgnore( boolean i ) {
    ignore = i;
  }

  /**
   * Check wether or not this row should be ignored...
   *
   * @return true if the row should be ignored.
   */
  public boolean isIgnored() {
    return ignore;
  }

  /**
   * Write the object to an ObjectOutputStream.
   *
   * @param out
   * @throws IOException
   */
  private void writeObject( java.io.ObjectOutputStream out ) throws IOException {
    writeObj( new DataOutputStream( out ) );
  }

  private void readObject( java.io.ObjectInputStream in ) throws IOException {
    readObj( new DataInputStream( in ) );
  }

  private void writeObj( DataOutputStream dos ) throws IOException {
    // First handle the number of fields in a row
    dos.writeInt( size() );

    // Write all values in the row
    for ( int i = 0; i < size(); i++ ) {
      getValue( i ).writeObj( dos );
    }
  }

  /**
   * Write the content of the row to a DataOutputStream.
   *
   * @param dos
   *          The DataOutputStream to write to
   * @throws KettleFileException
   *           if an error occurs.
   */
  public void write( DataOutputStream dos ) throws KettleFileException {
    try {
      writeObj( dos );
    } catch ( Exception e ) {
      throw new KettleFileException( BaseMessages.getString( PKG, "Row.ErrorWritingRow" ), e );
    }
  }

  private void readObj( DataInputStream dis ) throws IOException {
    // First handle the number of fields in a row
    int size = dis.readInt();

    // get all values in the row
    for ( int i = 0; i < size; i++ ) {
      Value v = new Value();
      v.readObj( dis );
      addValue( v );
    }
  }

  /**
   * Read a row of Values from an input-stream.
   *
   * @param dis
   *          The DataInputStream to read from
   */
  public Row( DataInputStream dis ) throws KettleFileException {
    try {
      readObj( dis );
    } catch ( EOFException e ) {
      throw new KettleEOFException( BaseMessages.getString( PKG, "Row.EndOfFileReached" ), e );
    } catch ( Exception e ) {
      throw new KettleFileException( BaseMessages.getString( PKG, "Row.ErrorReadingRowData" ), e );
    }
  }

  /**
   * Read a number of Values without meta-data into a row.
   *
   * @param dis
   *          The DataInputStream to read from
   * @param meta
   *          The description (name, type, length, precision) of the values to be read (the same number of values are
   *          read)
   * @throws KettleFileException
   *           if the row couldn't be created by reading from the data input stream.
   */
  public Row( DataInputStream dis, Row meta ) throws KettleFileException {
    this( dis, meta.size(), meta );
  }

  /**
   * Read a number of Values without meta-data into a row.
   *
   * @param dis
   *          The DataInputStream to read from
   * @param size
   *          the number or values to read
   * @param meta
   *          The description (name, type, length, precision) of the values to be read
   * @throws KettleFileException
   *           if the row couldn't be created by reading from the data input stream.
   */
  public Row( DataInputStream dis, int size, Row meta ) throws KettleFileException {
    try {
      // get all values in the row
      for ( int i = 0; i < size; i++ ) {
        addValue( new Value( meta.getValue( i ), dis ) );
      }
    } catch ( KettleEOFException e ) {
      throw new KettleEOFException( BaseMessages.getString( PKG, "Row.EndOfFileReadingRow" ), e );
    } catch ( Exception e ) {
      throw new KettleFileException( BaseMessages.getString( PKG, "Row.RowError" ), e );
    }
  }

  /**
   * Write a row of Values to a DataOutputStream, without saving the meta-data.
   *
   * @param dos
   *          The DataOutputStream to write to
   * @return true if the row was written successfuly, false if something went wrong.
   */
  public boolean writeData( DataOutputStream dos ) throws KettleFileException {
    // get all values in the row
    for ( int i = 0; i < size(); i++ ) {
      Value v = getValue( i );
      v.writeData( dos );
    }

    return true;
  }

  /**
   * Compare 2 rows with each other using certain values in the rows and also considering an ascending clause.
   *
   * @param r
   *          The row to compare with
   * @param fieldnrs
   *          The indexes of the values to compare
   * @param ascending
   *          an entry for each value to compare where true means and normal compare, false the reverse.
   * @return -1 if the row is smaller, 0 if they are equal and 1 if the row is larger.
   */
  public int compare( Row r, int[] fieldnrs, boolean[] ascending ) {
    return compare( r, fieldnrs, ascending, null );
  }

  public int compare( Row r, int[] fieldnrs, boolean[] ascending, boolean[] caseInsensitive ) {
    return compare( r, fieldnrs, fieldnrs, ascending, caseInsensitive );
  }

  /**
   * Compare 2 rows with each other using certain values in the rows and also considering an ascending clause.
   *
   * @param r
   *          The row to compare with
   * @param fieldnrs
   *          The indexes of the values to compare in the source row (this)
   * @param fieldnrs2
   *          The indexes of the values to compare with.
   * @param ascending
   *          an entry for each value to compare where true means and normal compare, false the reverse.
   * @return -1 if the row is smaller, 0 if they are equal and 1 if the row is larger.
   */
  public int compare( Row r, int[] fieldnrs1, int[] fieldnrs2, boolean[] ascending, boolean[] caseInsensitive ) {
    int retval = 0;
    int i;
    int len = ( fieldnrs1.length < fieldnrs2.length ) ? fieldnrs1.length : fieldnrs2.length;
    Value v1, v2;

    for ( i = 0; i < len; i++ ) {
      v1 = getValue( fieldnrs1[i] );
      v2 = r.getValue( fieldnrs2[i] );

      if ( caseInsensitive != null ) {
        retval = v1.compare( v2, caseInsensitive[i] );
      } else {
        retval = v1.compare( v2 );
      }

      if ( ascending != null && !ascending[i] ) {
        retval = -retval;
      }

      if ( retval != 0 ) {
        return retval;
      }
    }

    return retval;
  }

  /**
   * Compare 2 rows with each other using one value in the rows and also considering an ascending clause.
   *
   * @param r
   *          The row to compare with
   * @param fieldnr
   *          The indexe of the values to compare
   * @param sort_desc
   *          true means and normal compare, false the reverse.
   * @return -1 if the row is smaller, 0 if they are equal and 1 if the row is larger.
   */
  public int compare( Row r, int fieldnr, boolean sort_desc ) {
    int retval = 0;
    Value v1, v2;

    v1 = getValue( fieldnr );
    v2 = r.getValue( fieldnr );

    retval = v1.compare( v2 );

    if ( sort_desc ) {
      retval = -retval;
    }

    return retval;
  }

  /**
   * Compare 2 complete rows of values with each other. Strings are compared in a case insensitive way
   *
   * @param r
   *          the row to compare with
   * @return -1 if the row is smaller, 0 if both rows are equal, 1 if the row is larger.
   */
  public int compare( Row r ) {
    return compare( r, true );
  }

  /**
   * Compare 2 complete rows of values with each other
   *
   * @param r
   *          the row to compare with
   * @return -1 if the row is smaller, 0 if both rows are equal, 1 if the row is larger.
   */
  public int compare( Row r, boolean caseInsensitive ) {
    int retval = 0;
    int i;
    int len = r.size();
    Value v1, v2;

    for ( i = 0; i < len; i++ ) {
      v1 = getValue( i );
      v2 = r.getValue( i );

      retval = v1.compare( v2, caseInsensitive );

      if ( retval != 0 ) {
        return retval;
      }
    }

    return 0;
  }

  @Override
  public int compareTo( Row obj ) {
    return compare( obj );
  }

  @Override
  public boolean equals( Object r ) {
    Row row = (Row) r;
    return ( compare( row ) == 0 );
  }

  @Override
  public int hashCode() {
    int hash = 0;
    int i;

    for ( i = 0; i < size(); i++ ) {
      hash ^= getValue( i ).hashCode();
    }

    return hash;
  }

  /**
   * Returns an exact copy of this row. On purpose not the real clone() method (notice the uppercase 'C') because of
   * (rhino) javascript problems with it.
   *
   * @return an exact copy of this row
   */
  public synchronized Row Clone() {
    return new Row( this );
  }

  /*
   * Sets the logging date to "now" for this row.
   *
   * public void setLogdate() { logdate=new Date(); }
   */

  /*
   * Get the logging date of this row.
   *
   * @return the logging date of this row. public Date getLogdate() { return logdate; }
   */

  /*
   * Get the logging time for this row.
   *
   * @return the logging time for this row. public long getLogtime() { if (logdate==null) return 0L; return
   * logdate.getTime(); }
   */

  /**
   * Checks whether or not the row is empty A row is empty if all the values in the row are null A row is empty if there
   * are no values in the row.
   *
   * @return true if the row is considered empty, false if the row is not empty.
   */
  public boolean isEmpty() {
    boolean empty = true;
    for ( int i = 0; i < size(); i++ ) {
      Value v = getValue( i );
      if ( v != null && !v.isNull() ) {
        empty = false;
        break;
      }
    }
    return empty;
  }

  /**
   * Get an array of the names of all the Values in the Row.
   *
   * @return an array of Strings: the names of all the Values in the Row.
   */
  public String[] getFieldNames() {
    String[] retval = new String[size()];

    for ( int i = 0; i < size(); i++ ) {
      retval[i] = getValue( i ).getName();
    }

    return retval;
  }

  /**
   * Get an array of strings showing the name of the values in the row padded to a maximum length, followed by the types
   * of the values.
   *
   * @param maxlen
   *          The length to which the name will be padded.
   * @return an array of strings: the names and the types of the fieldnames in the row.
   */
  public String[] getFieldNamesAndTypes( int maxlen ) {
    String[] retval = new String[size()];

    for ( int i = 0; i < size(); i++ ) {
      Value v = getValue( i );
      retval[i] = Const.rightPad( v.getName(), maxlen ) + "   (" + v.getTypeDesc() + ")";
    }

    return retval;
  }

  /**
   * Search for a value, if it doesn't occur in the row, return the default value.
   *
   * @param valuename
   *          The valuename to look for
   * @param def
   *          The default value to return
   * @return The boolean representation of the value found or the default
   */
  public boolean getBoolean( String valuename, boolean def ) {
    Value v = searchValue( valuename );
    if ( v == null ) {
      return def;
    }
    return v.getBoolean();
  }

  /**
   * Search for a value, if it doesn't occur in the row, return the default value.
   *
   * @param valuename
   *          The valuename to look for
   * @param def
   *          The default value to return
   * @return The String representation of the value found or the default
   */
  public String getString( String valuename, String def ) {
    Value v = searchValue( valuename );
    if ( v == null ) {
      return def;
    }
    return v.getString();
  }

  /**
   * Search for a value, if it doesn't occur in the row, return the default value.
   *
   * @param valuename
   *          The valuename to look for
   * @param def
   *          The default value to return
   * @return The Date representation of the value found or the default
   */
  public Date getDate( String valuename, Date def ) {
    Value v = searchValue( valuename );
    if ( v == null ) {
      return def;
    }
    return v.getDate();
  }

  /**
   * Search for a value, if it doesn't occur in the row, return the default value.
   *
   * @param valuename
   *          The valuename to look for
   * @param def
   *          The default value to return
   * @return The double representation of the value found or the default
   */
  public double getNumber( String valuename, double def ) {
    Value v = searchValue( valuename );
    if ( v == null ) {
      return def;
    }
    return v.getNumber();
  }

  /**
   * Search for a value, if it doesn't occur in the row, return the default value.
   *
   * @param valuename
   *          The valuename to look for
   * @param def
   *          The default value to return
   * @return The long integer representation of the value found or the default
   */
  public long getInteger( String valuename, long def ) {
    Value v = searchValue( valuename );
    if ( v == null ) {
      return def;
    }
    return v.getInteger();
  }

  /**
   * Search for a value, if it doesn't occur in the row, return the default value.
   *
   * @param valuename
   *          The valuename to look for
   * @param def
   *          The default value to return
   * @return The short integer representation of the value found or the default
   */
  public long getShort( String valuename, int def ) {
    Value v = searchValue( valuename );
    if ( v == null ) {
      return def;
    }
    return (int) v.getInteger();
  }

  /**
   * Return the XML representation of a row.
   *
   * @return The XML representation of this row
   */
  @Override
  public String getXML() {
    StringBuilder xml = new StringBuilder();

    xml.append( "<" ).append( XML_TAG ).append( ">" );

    for ( int i = 0; i < size(); i++ ) {
      xml.append( getValue( i ).getXML() );
    }

    xml.append( "</" ).append( XML_TAG ).append( ">" );

    return xml.toString();
  }

  public Row( Node rowNode ) {
    int nrValues = XMLHandler.countNodes( rowNode, Value.XML_TAG );
    for ( int i = 0; i < nrValues; i++ ) {
      Node valueNode = XMLHandler.getSubNodeByNr( rowNode, Value.XML_TAG, i );
      addValue( new Value( valueNode ) );
    }
  }

  public static final void sortRows( List<Row> rows, int[] fieldNrs, boolean[] ascDesc ) {
    final int[] fieldNumbers = fieldNrs;
    final boolean[] ascending = ascDesc;

    Comparator<Row> comparator = new Comparator<Row>() {
      @Override
      public int compare( Row one, Row two ) {
        return one.compare( two, fieldNumbers, ascending );
      }
    };

    Collections.sort( rows, comparator );
  }

  public static final byte[] extractData( Row row ) {
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      DataOutputStream dataOutputStream = new DataOutputStream( byteArrayOutputStream );
      row.writeData( dataOutputStream );
      dataOutputStream.close();
      byteArrayOutputStream.close();
      return byteArrayOutputStream.toByteArray();
    } catch ( Exception e ) {
      throw new RuntimeException( BaseMessages.getString( PKG, "Row.ErrorSerializing" ) + row, e );
    }
  }

  public static final Row getRow( byte[] data, Row metadata ) {
    try {
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream( data );
      DataInputStream dataInputStream = new DataInputStream( byteArrayInputStream );
      return new Row( dataInputStream, metadata.size(), metadata );
    } catch ( Exception e ) {
      throw new RuntimeException( BaseMessages.getString( PKG, "Row.ErrorDeserializing" ), e );
    }
  }

  /**
   * @return the usedValueListeners
   */
  public List<ValueUsedListener> getUsedValueListeners() {
    return usedValueListeners;
  }

  /**
   * @param usedValueListeners
   *          the usedValueListeners to set
   */
  public void setUsedValueListeners( List<ValueUsedListener> usedValueListeners ) {
    this.usedValueListeners = usedValueListeners;
  }

}
