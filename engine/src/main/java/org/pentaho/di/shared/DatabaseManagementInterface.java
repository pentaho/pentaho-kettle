package org.pentaho.di.shared;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;

import java.util.List;

/**
 * This is the management interface used by the UI to perform CRUD operation. The implementors of this interface will
 * be scoped based on the bowl and can be retrieved using bowl's getManager()
 *
 */
public interface DatabaseManagementInterface {

  /**
   * Add the database connection to global or project specific file store(shared.xml) depending on the bowl
   * @param databaseMeta
   * @return boolean
   * @throws KettleException
   */
  public boolean addDatabase(DatabaseMeta databaseMeta) throws KettleException;

  /**
   * Get the list of databases connection based  on the current bowl
   * @return List<DatabaseMeta> Returns the list of DatabaseMeta
   * @throws KettleXMLException
   */
  public List<DatabaseMeta> getDatabases() throws KettleXMLException;

}
