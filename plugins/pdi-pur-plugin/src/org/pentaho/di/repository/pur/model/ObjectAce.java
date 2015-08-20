/*!
* Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

package org.pentaho.di.repository.pur.model;

import java.util.EnumSet;

import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

public interface ObjectAce {

    public ObjectRecipient getRecipient();
    public EnumSet<RepositoryFilePermission> getPermissions();
  	public void setRecipient(ObjectRecipient recipient);
  	public void setPermissions(RepositoryFilePermission first, RepositoryFilePermission... rest);
  	public void setPermissions(EnumSet<RepositoryFilePermission> permissions);
}
