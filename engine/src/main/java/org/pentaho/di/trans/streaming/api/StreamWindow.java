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

