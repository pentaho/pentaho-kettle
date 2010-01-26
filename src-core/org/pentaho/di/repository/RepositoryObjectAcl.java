package org.pentaho.di.repository;

import java.util.ArrayList;
import java.util.List;

public class RepositoryObjectAcl implements ObjectAcl{

	private List<ObjectAce> aces = new ArrayList<ObjectAce>();

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
}
