/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.database;

/**
 * Class to contain the information needed to parition (cluster): id, hostname, port, database
 *
 * @author Matt
 *
 */
public class PartitionDatabaseMeta {
  String partitionId;

  String hostname;
  String port;
  String databaseName;
  String username;
  String password;

  public PartitionDatabaseMeta() {
  }

  /**
   * @param partitionId
   * @param hostname
   * @param port
   * @param database
   */
  public PartitionDatabaseMeta( String partitionId, String hostname, String port, String database ) {
    super();

    this.partitionId = partitionId;
    this.hostname = hostname;
    this.port = port;
    this.databaseName = database;
  }

  /**
   * @return the partitionId
   */
  public String getPartitionId() {
    return partitionId;
  }

  /**
   * @param partitionId
   *          the partitionId to set
   */
  public void setPartitionId( String partitionId ) {
    this.partitionId = partitionId;
  }

  /**
   * @return the database
   */
  public String getDatabaseName() {
    return databaseName;
  }

  /**
   * @param database
   *          the database to set
   */
  public void setDatabaseName( String database ) {
    this.databaseName = database;
  }

  /**
   * @return the hostname
   */
  public String getHostname() {
    return hostname;
  }

  /**
   * @param hostname
   *          the hostname to set
   */
  public void setHostname( String hostname ) {
    this.hostname = hostname;
  }

  /**
   * @return the port
   */
  public String getPort() {
    return port;
  }

  /**
   * @param port
   *          the port to set
   */
  public void setPort( String port ) {
    this.port = port;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password
   *          the password to set
   */
  public void setPassword( String password ) {
    this.password = password;
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username
   *          the username to set
   */
  public void setUsername( String username ) {
    this.username = username;
  }

}
