package org.pentaho.di.repository;

import java.util.List;


public interface ObjectAcl {

	public List<ObjectAce> getAces();
	public ObjectRecipient getOwner();
	public boolean isEntriesInheriting();
	public void setAces(List<ObjectAce> aces);
	public void setOwner(ObjectRecipient owner);
	public void setEntriesInheriting(boolean entriesInheriting);
}
