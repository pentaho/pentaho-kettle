/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsoninput.reader;

import java.io.InputStream;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.jsoninput.JsonInputField;

public interface IJsonReader {

  /**
   * Store and compile fields.
   * 
   * @param fields
   * @throws UnsupportedJsonPathException
   */
  void setFields( JsonInputField[] fields ) throws KettleException;

  boolean isIgnoreMissingPath();

  void setIgnoreMissingPath( boolean value );

  /**
   * parse compiled fields into a rowset
   */
  public RowSet parse( InputStream in ) throws KettleException;

}
