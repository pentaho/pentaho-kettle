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

package org.pentaho.di.plugins.fileopensave.providers.model;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryDirectory;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;

import java.util.List;

public class TestRepositoryDirectory {
  @Test
  public void testRepositoryDirectory() {

    RepositoryDirectoryInterface testRepositoryDirectoryInterface = new RepositoryDirectoryInterface() {
      @Override public List<RepositoryDirectoryInterface> getChildren() {
        return null;
      }

      @Override public void setChildren( List<RepositoryDirectoryInterface> children ) {

      }

      @Override public List<RepositoryElementMetaInterface> getRepositoryObjects() {
        return null;
      }

      @Override public void setRepositoryObjects( List<RepositoryElementMetaInterface> children ) {

      }

      @Override public boolean isVisible() {
        return false;
      }

      @Override public String[] getPathArray() {
        return new String[ 0 ];
      }

      @Override public RepositoryDirectoryInterface findDirectory( String path ) {
        return null;
      }

      @Override public RepositoryDirectoryInterface findDirectory( ObjectId id_directory ) {
        return null;
      }

      @Override public RepositoryDirectoryInterface findDirectory( String[] path ) {
        return null;
      }

      @Override public ObjectId[] getDirectoryIDs() {
        return new ObjectId[ 0 ];
      }

      @Override public String getPath() {
        return "/directory1";
      }

      @Override public int getNrSubdirectories() {
        return 0;
      }

      @Override public org.pentaho.di.repository.RepositoryDirectory getSubdirectory( int i ) {
        return null;
      }

      @Override public boolean isRoot() {
        return false;
      }

      @Override public RepositoryDirectoryInterface findRoot() {
        return null;
      }

      @Override public void clear() {

      }

      @Override public void addSubdirectory( RepositoryDirectoryInterface subdir ) {

      }

      @Override public void setParent( RepositoryDirectoryInterface parent ) {

      }

      @Override public RepositoryDirectoryInterface getParent() {
        return null;
      }

      @Override public void setObjectId( ObjectId id ) {

      }

      @Override public void setName( String directoryname ) {

      }

      @Override public String getPathObjectCombination( String transName ) {
        return null;
      }

      @Override public RepositoryDirectoryInterface findChild( String name ) {
        return null;
      }

      @Override public String getName() {
        return "directory1";
      }

      @Override public ObjectId getObjectId() {
        return () -> "/";
      }
    };

    RepositoryDirectory testRepositoryDirectory;
    //Test passing parentPath as null
    testRepositoryDirectory = RepositoryDirectory.build( null, testRepositoryDirectoryInterface );
    Assert.assertEquals( testRepositoryDirectory.getPath(), "/directory1" );
    //Test passing parentPath as "/" for Local File Repository
    testRepositoryDirectory = RepositoryDirectory.build( "/", testRepositoryDirectoryInterface );
    Assert.assertEquals( testRepositoryDirectory.getPath(), "/directory1" );
    //Test passing parentPath as "/home"
    testRepositoryDirectory = RepositoryDirectory.build( "/home", testRepositoryDirectoryInterface );
    Assert.assertEquals( testRepositoryDirectory.getPath(), "/home/directory1" );
  }
}
