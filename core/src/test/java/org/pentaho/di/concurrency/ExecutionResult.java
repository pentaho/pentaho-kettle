/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.concurrency;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Andrey Khayrutdinov
 */
class ExecutionResult<T> {
  static <T> ExecutionResult<T> from( Future<? extends T> future ) {
    try {
      return new ExecutionResult<T>( future.get(), null );
    } catch ( InterruptedException e ) {
      throw new IllegalArgumentException( e );
    } catch ( ExecutionException e ) {
      return new ExecutionResult<T>( null, e.getCause() );
    }
  }

  private final T result;
  private final Throwable throwable;

  ExecutionResult( T result, Throwable throwable ) {
    this.result = result;
    this.throwable = throwable;
  }

  boolean isError() {
    return ( throwable != null );
  }

  T getResult() {
    return result;
  }

  Throwable getThrowable() {
    return throwable;
  }
}
