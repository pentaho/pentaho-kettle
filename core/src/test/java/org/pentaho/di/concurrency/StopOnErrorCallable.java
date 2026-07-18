/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A convenient class to derive from as it takes care of handling crashes during execution of callable
 *
 * @author Andrey Khayrutdinov
 */
abstract class StopOnErrorCallable<T> implements Callable<T> {

  final AtomicBoolean condition;

  StopOnErrorCallable( AtomicBoolean condition ) {
    this.condition = condition;
  }

  @Override
  public T call() throws Exception {
    try {
      return doCall();
    } catch ( Exception e ) {
      condition.set( false );
      throw e;
    }
  }

  abstract T doCall() throws Exception;
}
