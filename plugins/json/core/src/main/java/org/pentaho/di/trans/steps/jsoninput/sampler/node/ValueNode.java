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

package org.pentaho.di.trans.steps.jsoninput.sampler.node;

/**
 * A node with just a value with a type
 *
 * Created by bmorrise on 7/27/18.
 */
public class ValueNode<T> implements Node {
  private T value;

  public ValueNode( T value ) {
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  public void setValue( T value ) {
    this.value = value;
  }

  public String getType() {
    return value.getClass().getSimpleName();
  }

  @Override
  public String output() {
    return String.valueOf( value );
  }

  @Override
  public void dedupe() {
    // Do nothing
  }
}
