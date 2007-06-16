package org.pentaho.di.trans;

import java.util.ArrayList;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.repository.Repository;

import be.ibridge.kettle.core.exception.KettleException;


public interface HasDatabasesInterface
{

    /**
     * Get an ArrayList of defined DatabaseInfo objects.
     *
     * @return an ArrayList of defined DatabaseInfo objects.
     */
    public ArrayList getDatabases();

    /**
     * @param databases The databases to set.
     */
    public void setDatabases(ArrayList databases);

    /**
     * Add a database connection to the transformation.
     *
     * @param databaseMeta The database connection information.
     */
    public void addDatabase(DatabaseMeta databaseMeta);

    /**
     * Add a database connection to the transformation if that connection didn't exists yet.
     * Otherwise, replace the connection in the transformation
     *
     * @param databaseMeta The database connection information.
     */
    public void addOrReplaceDatabase(DatabaseMeta databaseMeta);

    /**
     * Add a database connection to the transformation on a certain location.
     *
     * @param p The location
     * @param ci The database connection information.
     */
    public void addDatabase(int p, DatabaseMeta ci);

    /**
     * Retrieves a database connection information a a certain location.
     *
     * @param i The database number.
     * @return The database connection information.
     */
    public DatabaseMeta getDatabase(int i);

    /**
     * Removes a database from the transformation on a certain location.
     *
     * @param i The location
     */
    public void removeDatabase(int i);

    /**
     * Count the nr of databases in the transformation.
     *
     * @return The nr of databases
     */
    public int nrDatabases();

    /**
     * Searches the list of databases for a database with a certain name
     *
     * @param name The name of the database connection
     * @return The database connection information or null if nothing was found.
     */
    public DatabaseMeta findDatabase(String name);

    /**
     * Read the database connections in the repository and add them to this transformation if they are not yet present.
     *
     * @param rep The repository to load the database connections from.
     * @param overWriteShared if an object with the same name exists, overwrite
     */
    public void readDatabases(Repository rep, boolean overWriteShared) throws KettleException;

    /**
     * Find the location of database
     *
     * @param ci The database queried
     * @return The location of the database, -1 if nothing was found.
     */
    public int indexOfDatabase(DatabaseMeta ci);

    /**
     * Checks whether or not the connections have changed.
     *
     * @return True if the connections have been changed.
     */
    public boolean haveConnectionsChanged();
}
