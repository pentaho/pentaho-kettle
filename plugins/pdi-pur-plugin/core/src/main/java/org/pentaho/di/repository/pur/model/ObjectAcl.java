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

import java.util.List;

import org.pentaho.di.repository.ObjectRecipient;

public interface ObjectAcl {

  public List<ObjectAce> getAces();

  public ObjectRecipient getOwner();

  public boolean isEntriesInheriting();

  public void setAces( List<ObjectAce> aces );

  public void setOwner( ObjectRecipient owner );

  public void setEntriesInheriting( boolean entriesInheriting );
}
