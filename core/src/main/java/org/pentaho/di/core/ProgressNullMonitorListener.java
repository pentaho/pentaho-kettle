//CHECKSTYLE:EmptyBlock:OFF
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

package org.pentaho.di.core;

public class ProgressNullMonitorListener implements ProgressMonitorListener {

  @Override
  public void beginTask( String message, int nrWorks ) {
  }

  @Override
  public void subTask( String message ) {
  }

  @Override
  public boolean isCanceled() {
    return false;
  }

  @Override
  public void worked( int nrWorks ) {
  }

  @Override
  public void done() {
  }

  @Override
  public void setTaskName( String taskName ) {
  }
}
