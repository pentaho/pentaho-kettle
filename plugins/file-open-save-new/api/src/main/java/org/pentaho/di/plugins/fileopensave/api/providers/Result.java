/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.api.providers;

/**
 * Created by bmorrise on 3/4/19.
 */
public class Result {

  public enum Status {
    SUCCESS,
    PENDING,
    FILE_COLLISION,
    ERROR
  }

  private Status status;
  private String message;
  private Object data;

  public Result( Status status, String message, Object data ) {
    this.status = status;
    this.message = message;
    this.data = data;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus( Status status ) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public Object getData() {
    return data;
  }

  public void setData( Object data ) {
    this.data = data;
  }

  public static Result success( String message, Object object ) {
    return new Result( Status.SUCCESS, message, object );
  }

  public static Result error( String message, Object object ) {
    return new Result( Status.ERROR, message, object );
  }

  public static Result pending( String message, Object object ) {
    return new Result( Status.PENDING, message, object );
  }

  public static Result fileCollision( String message, Object object ) {
    return new Result( Status.FILE_COLLISION, message, object );
  }
}
