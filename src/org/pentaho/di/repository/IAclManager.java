package org.pentaho.di.repository;

import org.pentaho.di.core.exception.KettleException;

public interface IAclManager extends IRepositoryService{
  /**
   * Get the Permissions of a repository object.
   * 
   * @param Object Id of the repository object
   * @param forceParentInheriting retrieve the effective ACLs as if 'inherit from parent' were true
   * 
   * @return The permissions.
   * @throws KettleException in case something goes horribly wrong
   */
  public ObjectAcl getAcl(ObjectId id, boolean forceParentInheriting) throws KettleException;

  /**
   * Set the Permissions of a repository element.
   * 
   * @param Acl object that needs to be set.
   * @param Object Id of a file for which the acl are being set.
   * 
   * @throws KettleException in case something goes horribly wrong
   */
  public  void setAcl(ObjectId id, ObjectAcl aclObject) throws KettleException;

}
