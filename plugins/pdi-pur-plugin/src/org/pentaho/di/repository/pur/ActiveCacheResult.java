/*!
* Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/
package org.pentaho.di.repository.pur;

public class ActiveCacheResult<Value> {
  private final Exception exception;

  private final Value value;

  private long timeLoaded;

  public ActiveCacheResult( Value value, Exception exception ) {
    this.value = value;
    this.exception = exception;
    this.timeLoaded = System.currentTimeMillis();
  }

  public Exception getException() {
    return exception;
  }

  public Value getValue() {
    return value;
  }

  public long getTimeLoaded() {
    return timeLoaded;
  }
}
