/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.streaming.api;


import io.reactivex.Flowable;

/**
 * Defines a source of streaming data.  A StreamSource implementation is used
 * by {@link org.pentaho.di.trans.streaming.common.BaseStreamStep} when
 * loading data from an external stream.
 */
public interface StreamSource<R>  {

  /**
   * Returns the rows of data as an iterable.
   */
  Flowable<R> flowable();

  /**
   * Signals this stream is no longer in use and can clean up
   * resources.
   */
  void close();

  /**
   * Causes the stream to stop accepting new input.
   */
  void pause();

  /**
   * Resumes accepting input if paused, otherwise noop.
   */
  void resume();

  /**
   * Open the source for loading rows.
   * Used for initializing resources required to load the stream.
   */
  void open();
}
