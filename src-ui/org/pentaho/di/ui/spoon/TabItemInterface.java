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

package org.pentaho.di.ui.spoon;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;

public interface TabItemInterface
{
    /** 
     * Closes the content behind the tab, de-allocates resources.
     * 
     * @return true if the tab was closed, false if it was prevented by the user. (are you sure dialog)
     */
    public boolean canBeClosed();
    public boolean canHandleSave();
    public Object getManagedObject();
    public boolean hasContentChanged();
    public ChangedWarningInterface getChangedWarning();
    public int showChangedWarning() throws KettleException;
    public boolean applyChanges() throws KettleException;
    public EngineMetaInterface getMeta();
    public void setControlStates();
    public boolean setFocus();
    
}
