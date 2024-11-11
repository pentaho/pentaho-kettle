/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
