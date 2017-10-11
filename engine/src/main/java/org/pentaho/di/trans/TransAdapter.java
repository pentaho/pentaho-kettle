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

package org.pentaho.di.trans;

import org.pentaho.di.core.exception.KettleException;

/**
 * Utility class to allow only certain methods of TransListener to be overridden.
 *
 * @author matt
 *
 */
public class TransAdapter implements TransListener {

  public void transStarted( Trans trans ) throws KettleException {

  }

  public void transActive( Trans trans ) {
  }

  public void transFinished( Trans trans ) throws KettleException {
  }

}
