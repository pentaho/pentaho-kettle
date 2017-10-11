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

package org.pentaho.di.job;

import org.pentaho.di.core.exception.KettleException;

/**
 * Utility class to allow only certain methods of JobListener to be overridden.
 *
 * @author Marc
 *
 */

public class JobAdapter implements JobListener {

  @Override
  public void jobFinished( Job job ) throws KettleException {
    // NoOp

  }

  @Override
  public void jobStarted( Job job ) throws KettleException {
    // NoOp

  }

}
