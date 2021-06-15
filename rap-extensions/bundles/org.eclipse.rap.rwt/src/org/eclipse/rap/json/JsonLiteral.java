/*******************************************************************************
 * Copyright (c) 2013 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial implementation and API
 ******************************************************************************/
package org.eclipse.rap.json;

import java.io.IOException;


@SuppressWarnings( "serial" ) // use default serial UID
class JsonLiteral extends JsonValue {

  private final String value;

  JsonLiteral( String value ) {
    this.value = value;
  }

  @Override
  protected void write( JsonWriter writer ) throws IOException {
    writer.write( value );
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public boolean asBoolean() {
    return isBoolean() ? isTrue() : super.asBoolean();
  }

  @Override
  public boolean isNull() {
    return this == NULL;
  }

  @Override
  public boolean isBoolean() {
    return this == TRUE || this == FALSE;
  }

  @Override
  public boolean isTrue() {
    return this == TRUE;
  }

  @Override
  public boolean isFalse() {
    return this == FALSE;
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean equals( Object object ) {
    if( this == object ) {
      return true;
    }
    if( object == null ) {
      return false;
    }
    if( getClass() != object.getClass() ) {
      return false;
    }
    JsonLiteral other = (JsonLiteral)object;
    return value.equals( other.value );
  }

}
