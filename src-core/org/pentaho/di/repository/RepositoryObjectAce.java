package org.pentaho.di.repository;

import java.util.EnumSet;

public class RepositoryObjectAce implements ObjectAce{

	
    private ObjectRecipient recipient;

    private EnumSet<ObjectPermission> permissions;

    public RepositoryObjectAce(ObjectRecipient recipient) {
    	this.recipient = recipient;
      }
   
    public RepositoryObjectAce(ObjectRecipient recipient, ObjectPermission first, ObjectPermission... rest) {
      this(recipient, EnumSet.of(first, rest));
    }

    public RepositoryObjectAce(ObjectRecipient recipient, EnumSet<ObjectPermission> permissions) {
      this(recipient);    	
      this.permissions = permissions;
    }

    public ObjectRecipient getRecipient() {
      return recipient;
    }

    public EnumSet<ObjectPermission> getPermissions() {
      return permissions;
    }

	public void setRecipient(ObjectRecipient recipient) {
		this.recipient = recipient;
	}

	public void setPermissions(EnumSet<ObjectPermission> permissions) {
		this.permissions = permissions;
	}

	public void setPermissions(ObjectPermission first, ObjectPermission... rest) {
		this.permissions = EnumSet.of(first, rest);
	}

}
