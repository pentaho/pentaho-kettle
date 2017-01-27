package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.engine.api.model.Row;
import org.pentaho.di.engine.api.RowException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class KettleRow implements Row {

  private transient RowMetaInterface rowMeta;
  private final Object[] values;
  private final List<String> names;

  public KettleRow( RowMetaInterface rowMeta, Object[] values ) {
    this.rowMeta = rowMeta;
    this.values = Optional.ofNullable( values )
    .map( v -> v.clone() )
    .orElse( new Object[0] );
    this.names = Arrays.asList( rowMeta.getFieldNames() );
  }

  @Override public int size() {
    return values.length;
  }

  @Override public Optional<String> getString( int index ) throws RowException {
    preconditions( index );
    try {
      return Optional.ofNullable( rowMeta.getString( values, index ) );
    } catch ( KettleValueException e ) {
      throw new RowException( "failed to get string" , e );
    }
  }

  private void preconditions( int index ) throws RowException {
    if ( index < 0 || index >= values.length ) {
      throw new RowException( "Invalid index:  "  + index );
    }
  }

  @Override public Optional<Long> getLong( int index ) {
    return null;
  }

  @Override public Optional<Double> getNumber( int index ) {
    return null;
  }

  @Override public Optional<Date> getDate( int index ) {
    return null;
  }

  @Override public Optional<BigDecimal> getBigNumber( int index ) {
    return null;
  }

  @Override public Optional<Boolean> getBoolean( int index ) {
    return null;
  }

  @Override public Optional<byte[]> getBinary( int index ) {
    return null;
  }

  @Override public Optional<Object> getObject( int index ) {
    return null;
  }

  @Override public Optional<String> getString( String name ) throws RowException {
    return null;
  }

  @Override public Optional<Long> getLong( String name ) throws RowException {
    return null;
  }

  @Override public Optional<Double> getNumber( String name ) throws RowException {
    return null;
  }

  @Override public Optional<Date> getDate( String name ) throws RowException {
    return null;
  }

  @Override public Optional<BigDecimal> getBigNumber( String name ) throws RowException {
    return null;
  }

  @Override public Optional<Boolean> getBoolean( String name ) throws RowException {
    return null;
  }

  @Override public Optional<byte[]> getBinary( String name ) throws RowException {
    return null;
  }

  @Override public Optional<Object> getObject( String name ) throws RowException {
    return null;
  }

  @Override public Optional<Object[]> getObjects() {
    return Optional.of( values.clone() );
  }

  @Override public List<String> getColumnNames() {
    return names;
  }

  @Override public List<Class> getColumnTypes() {
    return rowMeta.getValueMetaList().stream()
      .map( v -> {
        try {
          return v.getNativeDataTypeClass();
        } catch ( KettleValueException e ) {
          throw new RuntimeException( e );
        }
      } )
      .collect( Collectors.toList() );
  }

  public RowMetaInterface getRowMeta() {
    return rowMeta;
  }



  private void writeObject(ObjectOutputStream oos)
    throws IOException {
    oos.defaultWriteObject();
    oos.writeObject( rowMeta.getMetaXML() );
  }

  private void readObject(ObjectInputStream ois)
    throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    try {
      String xml = ois.readObject( ).toString();
      Document doc = XMLHandler.loadXMLString( xml );
      Node rowMetaNode = XMLHandler.getSubNode( doc, "row-meta" );
      rowMeta = new RowMeta( rowMetaNode );

    } catch ( KettleException e ) {
      e.printStackTrace();
    }
  }


}
