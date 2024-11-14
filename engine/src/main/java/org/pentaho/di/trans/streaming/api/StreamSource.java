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
