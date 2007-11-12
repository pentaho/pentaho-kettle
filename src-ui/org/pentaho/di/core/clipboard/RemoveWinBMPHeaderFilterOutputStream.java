/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
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