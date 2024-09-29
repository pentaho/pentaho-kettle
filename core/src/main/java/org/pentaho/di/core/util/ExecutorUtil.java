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


package org.pentaho.di.core.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorUtil {
  public static final String SIMPLE_NAME = ExecutorUtil.class.getSimpleName();
  private static final AtomicInteger threadNum = new AtomicInteger( 1 );
  private static final ExecutorService executor = init();

  private static ExecutorService init() {
    ExecutorService executorService = Executors.newCachedThreadPool( new ThreadFactory() {
      @Override public Thread newThread( Runnable r ) {
        Thread thread = Executors.defaultThreadFactory().newThread( r );
        thread.setDaemon( true );
        thread.setName( SIMPLE_NAME + " thread " + threadNum.getAndIncrement() );
        return thread;
      }
    } );
    return executorService;
  }

  public static ExecutorService getExecutor() {
    return executor;
  }
}
