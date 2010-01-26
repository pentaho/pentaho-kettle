package org.pentaho.di.repository;

import java.util.EnumSet;

public interface ObjectAce {

    public ObjectRecipient getRecipient();
    public EnumSet<ObjectPermission> getPermissions();
  	public void setRecipient(ObjectRecipient recipient);
  	public void setPermissions(ObjectPermission first, ObjectPermission... rest);
  	public void setPermissions(EnumSet<ObjectPermission> permissions);
}
