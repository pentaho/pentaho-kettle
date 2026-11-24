/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.core.vfs;

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Copy of a caller's variables, used as the {@code pvfs} file system option.
 * <p>
 * Commons VFS caches file systems by their {@code FileSystemOptions}, comparing option values by {@code hashCode()}.
 * Callers such as job entries ({@code JobEntryBase}) and transformation/job metas ({@code AbstractMeta}) hash by
 * name, not by their variable values, so a later execution of the same job entry or transformation with different
 * variable values (e.g. Carte request parameters) would reuse the file system of the first execution, together with
 * its variables (PDI-20586). A snapshot hashes by the variable values instead, so equal values share a file system
 * and different values get their own.
 */
final class VariableSpaceSnapshot extends Variables {

  private final Map<String, String> values;

  VariableSpaceSnapshot( VariableSpace source ) {
    Map<String, String> copy = new TreeMap<>();
    for ( String name : source.listVariables() ) {
      String value = source.getVariable( name );
      if ( value != null ) {
        copy.put( name, value );
        setVariable( name, value );
      }
    }
    values = Collections.unmodifiableMap( copy );
  }

  @Override
  public boolean equals( Object other ) {
    return other instanceof VariableSpaceSnapshot && values.equals( ( (VariableSpaceSnapshot) other ).values );
  }

  @Override
  public int hashCode() {
    return values.hashCode();
  }
}
