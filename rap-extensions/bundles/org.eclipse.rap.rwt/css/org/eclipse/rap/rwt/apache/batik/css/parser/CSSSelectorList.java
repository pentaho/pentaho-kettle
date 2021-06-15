/*

   Copyright 2000-2001  The Apache Software Foundation 

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.eclipse.rap.rwt.apache.batik.css.parser;

import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;

/**
 * This class implements the {@link SelectorList} interface.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: CSSSelectorList.java,v 1.1 2010/04/14 15:16:16 rherrmann Exp $
 */
public class CSSSelectorList implements SelectorList {

    /**
     * The list.
     */
    protected Selector[] list = new Selector[3];

    /**
     * The list length.
     */
    protected int length;

    /**
     * <b>SAC</b>: Returns the length of this selector list
     */    
    public int getLength() {
        return length;
    }

    /**
     * <b>SAC</b>: Returns the selector at the specified index, or
     * <code>null</code> if this is not a valid index.  
     */
    public Selector item(int index) {
        if (index < 0 || index >= length) {
            return null;
        }
        return list[index];
    }

    /**
     * Appends an item to the list.
     */
    public void append(Selector item) {
        if (length == list.length) {
            Selector[] tmp = list;
            list = new Selector[list.length * 3 / 2];
            for (int i = 0; i < tmp.length; i++) {
                list[i] = tmp[i];
            }
        }
        list[length++] = item;
    }
}
