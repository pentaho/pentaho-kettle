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
