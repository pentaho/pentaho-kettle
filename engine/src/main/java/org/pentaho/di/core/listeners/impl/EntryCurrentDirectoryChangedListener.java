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

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.listeners.CurrentDirectoryChangedListener;
import org.pentaho.di.core.variables.Variables;

import com.google.common.base.Objects;

/**
 * Updates directory references referencing {@link Const#INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY}
 */
public class EntryCurrentDirectoryChangedListener implements CurrentDirectoryChangedListener {

  public interface PathReference {
    ObjectLocationSpecificationMethod getSpecification();
    String getPath();
    void setPath( String path );
  }

  private PathReference[] references;

  public EntryCurrentDirectoryChangedListener( PathReference... refs ) {
    references = refs;
  }

  public EntryCurrentDirectoryChangedListener(
      Supplier<ObjectLocationSpecificationMethod> specMethodGetter,
      Supplier<String> pathGetter,
      Consumer<String> pathSetter ) {
    this( new PathReference() {

      @Override
      public ObjectLocationSpecificationMethod getSpecification() {
        return specMethodGetter.get();
      }

      @Override
      public String getPath() {
        return pathGetter.get();
      }

      @Override
      public void setPath( String path ) {
        pathSetter.accept( path );
      }
    } );
  }

  @Override
  public void directoryChanged( Object origin, String oldCurrentDir, String newCurrentDir ) {
    for ( PathReference ref : references ) {
      ObjectLocationSpecificationMethod specMethod = ref.getSpecification();
      String path = ref.getPath();
      if ( ( specMethod == ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME
          || specMethod == ObjectLocationSpecificationMethod.FILENAME )
        && StringUtils.contains( path, Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY )
        && !Objects.equal( oldCurrentDir, newCurrentDir ) ) {
        path = reapplyCurrentDir( oldCurrentDir, newCurrentDir, path );
        ref.setPath( path );
      }
    }
  }

  private String reapplyCurrentDir( String oldCurrentDir, String newCurrentDir, String path ) {
    Variables vars = new Variables();
    vars.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, oldCurrentDir );
    String newPath = vars.environmentSubstitute( path );
    return getPath( newCurrentDir, newPath );
  }

  private static String getPath( String parentPath, String path ) {
    if ( !parentPath.equals( "/" ) && path.startsWith( parentPath ) ) {
      path = path.replace( parentPath, "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}" );
    }
    return path;
  }

}
