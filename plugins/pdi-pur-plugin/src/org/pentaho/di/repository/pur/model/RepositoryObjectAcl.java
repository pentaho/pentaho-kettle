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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.repository.ObjectRecipient;

public class RepositoryObjectAcl implements ObjectAcl, java.io.Serializable {

  private static final long serialVersionUID = 3717895033941725273L; /* EESOURCE: UPDATE SERIALVERUID */

	private List<ObjectAce> aces = new ArrayList<ObjectAce>();

	@Override
  public boolean equals(Object obj) {
	  if(obj != null) {
  	  RepositoryObjectAcl acl = (RepositoryObjectAcl) obj;
  	  if(aces != null && owner != null) {
  	    return aces.equals(acl.getAces()) && owner.equals(acl.getOwner()) && entriesInheriting == acl.isEntriesInheriting();
  	  } else if(aces == null && acl.getAces() == null) {
  	    return owner.equals(acl.getOwner()) && entriesInheriting == acl.isEntriesInheriting();
  	  } else if(owner == null && acl.getOwner() == null) {
        return aces.equals(acl.getAces()) && entriesInheriting == acl.isEntriesInheriting();
      } else {
        return false;
      }
	  } else {
	    return false;
	  }
  }

  private ObjectRecipient owner;

	private boolean entriesInheriting = true;

	// ~ Constructors
	// ====================================================================================================

	public RepositoryObjectAcl(ObjectRecipient owner) {
		this.owner = owner;
	}

	// ~ Methods
	// =========================================================================================================

	public List<ObjectAce> getAces() {
		return aces;
	}

	public ObjectRecipient getOwner() {
		return owner;
	}

	public boolean isEntriesInheriting() {
		return entriesInheriting;
	}

	public void setAces(List<ObjectAce> aces) {
		this.aces = aces;
	}

	public void setOwner(ObjectRecipient owner) {
		this.owner = owner;
	}

	public void setEntriesInheriting(boolean entriesInheriting) {
		this.entriesInheriting = entriesInheriting;
	}

  @Override
  public String toString() {
    return owner.getName();
  }
}
