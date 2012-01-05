/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.clipboard;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Strips off the BITMAPFILEHEADER from a ImageLoader.save() call
 *
 * Used when converting from ImageData to CF_DIB (pasting on clipboard)
 *
 * @author Philip Schatz ( www.philschatz.com )
 */
class RemoveWinBMPHeaderFilterOutputStream extends OutputStream {
        private final OutputStream out;
        private int counter = 0;

        public RemoveWinBMPHeaderFilterOutputStream(OutputStream out) {
                this.out = out;
        }
        public void write(int b) throws IOException {
                //ignore the bmp file header
                if (this.counter < PrependWinBMPHeaderFilterInputStream.BITMAPFILEHEADER_SIZEOF) {
                        this.counter++;
                } else {
                        this.out.write(b);
                }
        }
}