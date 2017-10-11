/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.listeners.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;

public class EntryCurrentDirectoryChangedListenerTest {
  private static final String VAR = "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}";

  @Test
  public void testDirectoryChanged() {
    final String path1 = VAR + "/ahoy/file";
    TestPathRef pathRefRepName = new TestPathRef();
    pathRefRepName.setSpecification( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    pathRefRepName.setPath( path1 );
    TestPathRef pathRefRepRef = new TestPathRef();
    pathRefRepRef.setSpecification( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    pathRefRepRef.setPath( VAR );
    TestPathRef pathRefFile = new TestPathRef();
    pathRefFile.setSpecification( ObjectLocationSpecificationMethod.FILENAME );
    pathRefFile.setPath( path1 );

    EntryCurrentDirectoryChangedListener listener =
        new EntryCurrentDirectoryChangedListener( pathRefRepName, pathRefRepRef, pathRefFile );
    listener.directoryChanged( new Object(), "/home/user", "/home/user/ahoy" );
    final String path1After = VAR + "/file";
    assertEquals( path1After, pathRefRepName.getPath() );
    assertEquals( path1After, pathRefFile.getPath() );
    assertEquals( VAR, pathRefRepRef.getPath() );

  }

  @Test
  public void testDirectoryChangedNotAncestor() {
    final String path1 = VAR + "/file";
    TestPathRef pathRefRepName = new TestPathRef();
    pathRefRepName.setSpecification( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    pathRefRepName.setPath( path1 );
    EntryCurrentDirectoryChangedListener listener =
        new EntryCurrentDirectoryChangedListener(
            pathRefRepName::getSpecification,
            pathRefRepName::getPath,
            pathRefRepName::setPath );
    listener.directoryChanged( new Object(), "/home/user/ahoy", "/some/where/over/the/rainbow" );
    assertEquals( "/home/user/ahoy/file", pathRefRepName.getPath() );
  }

  public class TestPathRef implements EntryCurrentDirectoryChangedListener.PathReference {

    private String path;
    private ObjectLocationSpecificationMethod spec;

    @Override
    public ObjectLocationSpecificationMethod getSpecification() {
      return spec;
    }

    public void setSpecification( ObjectLocationSpecificationMethod value ) {
      spec = value;
    }

    @Override
    public String getPath() {
      return path;
    }

    @Override
    public void setPath( String value ) {
      path = value;
    }
  }

}
