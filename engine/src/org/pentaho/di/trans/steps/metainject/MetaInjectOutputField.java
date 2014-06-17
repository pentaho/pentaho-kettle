package org.pentaho.di.trans.steps.metainject;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;

public class MetaInjectOutputField {
  private String name;
  private int type;
  private int length;
  private int precision;

  public MetaInjectOutputField( String name, int type, int length, int precision ) {
    super();
    this.name = name;
    this.type = type;
    this.length = length;
    this.precision = precision;
  }

  public String getTypeDescription() {
    return ValueMetaFactory.getValueMetaName( type );
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public int getType() {
    return type;
  }

  public void setType( int type ) {
    this.type = type;
  }

  public int getLength() {
    return length;
  }

  public void setLength( int length ) {
    this.length = length;
  }

  public int getPrecision() {
    return precision;
  }

  public void setPrecision( int precision ) {
    this.precision = precision;
  }

  public ValueMetaInterface createValueMeta() throws KettlePluginException {
    return ValueMetaFactory.createValueMeta( name, type, length, precision );
  }
}
