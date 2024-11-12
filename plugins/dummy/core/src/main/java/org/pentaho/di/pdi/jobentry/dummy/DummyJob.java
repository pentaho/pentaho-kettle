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


/*
 * Created on 17-Feb-07
 * Actualis Center
 *
 */
package org.pentaho.di.pdi.jobentry.dummy;

import org.pentaho.di.core.exception.*;
import org.pentaho.di.core.logging.*;
import org.pentaho.di.core.util.*;

import java.io.*;
import java.util.regex.*;

public class DummyJob {

  private String _wildcard;
  private String _targetDir;
  private String _sourceDir;

  public DummyJob( String source, String target, String wildcard ) {
    _sourceDir = source;
    _targetDir = target;
    _wildcard = wildcard;
  }

  public long process() throws KettleJobException, FileNotFoundException {
    LogChannelInterface log = new LogChannel( this );
    File srcDir = getDir( _sourceDir );
    Pattern pattern = null;
    if ( !Utils.isEmpty( _wildcard ) ) {
      pattern = Pattern.compile( _wildcard );
    }
    final Pattern fpat = pattern;
    FileFilter regexFiler = new FileFilter() {
      @Override
      public boolean accept( File pathname ) {
        if ( fpat == null ) {
          return true;
        }
        if ( fpat.matcher( pathname.getName() ).matches() ) {
          return true;
        }
        return false;
      }
    };
    long files = 0;
    File[] allFiles = srcDir.listFiles( regexFiler );
    File outDir = new File( _targetDir );
    outDir.mkdirs();
    for ( int i = 0; i < allFiles.length; i++ ) {
      File cFile = allFiles[i];
      log.logDetailed( toString(), "processing file '" + cFile + "'" );
      processFile( cFile, outDir );
    }
    return files;
  }

  public File getDir( String dirname ) throws KettleJobException {
    File fl = new File( dirname );
    if ( !fl.isDirectory() ) {
      throw new KettleJobException( "'" + dirname + "' is not a directory" );
    }
    return fl;
  }

  public void processFile( File fl, File outDir ) throws FileNotFoundException {
    // do something with the file here

  }
}
