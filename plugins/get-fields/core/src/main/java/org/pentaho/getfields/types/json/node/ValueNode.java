/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.getfields.types.json.node;

/**
 * Created by bmorrise on 8/24/18.
 */
public class ValueNode<T> extends Node {
  private T value;

  public ValueNode( String key, T value ) {
    super( key );
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  public void setValue( T value ) {
    this.value = value;
  }

  public String getType() {
    if ( value != null ) {
      return value.getClass().getSimpleName();
    }
    return null;
  }

  @Override
  public String toString() {
    String output = value instanceof String ? "" + String.valueOf( value ) + "" : String.valueOf( value );
    return key + ": " + output + "\n";
  }

  @Override
  public void dedupe() {
    // Do nothing
  }
}
