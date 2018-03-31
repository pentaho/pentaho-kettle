/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.compress.zip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.compress.CompressionOutputStream;
import org.pentaho.di.core.compress.CompressionProvider;

public class ZIPCompressionOutputStream extends CompressionOutputStream {

  public ZIPCompressionOutputStream( OutputStream out, CompressionProvider provider ) {
    super( getDelegate( out ), provider );
  }

  protected static ZipOutputStream getDelegate( OutputStream out ) {
    ZipOutputStream delegate;
    if ( out instanceof ZipOutputStream ) {
      delegate = (ZipOutputStream) out;
    } else {
      delegate = new ZipOutputStream( out );
    }
    return delegate;
  }

  @Override
  public void close() throws IOException {
    ZipOutputStream zos = (ZipOutputStream) delegate;
    zos.flush();
    zos.closeEntry();
    zos.finish();
    zos.close();
  }

  @Override
  public void addEntry( String filename, String extension ) throws IOException {
    // remove folder hierarchy
    int index = filename.lastIndexOf( Const.FILE_SEPARATOR );
    String entryPath;
    if ( index != -1 ) {
      entryPath = filename.substring( index + 1 );
    } else {
      entryPath = filename;
    }

    // remove ZIP extension
    index = entryPath.toLowerCase().lastIndexOf( ".zip" );
    if ( index != -1 ) {
      entryPath = entryPath.substring( 0, index ) + entryPath.substring( index + ".zip".length() );
    }

    // add real extension if needed
    if ( !Utils.isEmpty( extension ) ) {
      entryPath += "." + extension;
    }

    ZipEntry zipentry = new ZipEntry( entryPath );
    zipentry.setComment( "Compressed by Kettle" );
    ( (ZipOutputStream) delegate ).putNextEntry( zipentry );
  }
}
