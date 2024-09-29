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


package org.pentaho.di.trans.streaming.api;

import io.reactivex.Flowable;

/** A StreamWindow governs buffering and sending rows to a sub-transformation.
 *
 *  Windowing strategies could include sending buffered rows
 *    * after a given interval
 *    * on a sliding interval
 *    * after a fixed number of rows have been read.
 *    *
 * **/
public interface StreamWindow<I, O> {

  /**
   * Takes an iterable (would typically be a {@link StreamSource#flowable()}}
   * call) and buffers it according to the window strategy.
   *
   * Returns an iterable of data for the window.  Depending on stream implementation,
   * the output could also be transformed.  For example, the
   * {@link org.pentaho.di.trans.streaming.common.FixedTimeStreamWindow}
   * will pass windowed data to a subtransformation, and return the
   * transformed results.
   */
  Iterable<O> buffer( Flowable<I> flowable );
}

