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

package org.pentaho.di.pkg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.version.BuildVersion;

public class JarfileGenerator {
  private static LogChannelInterface log;

  public static final String TRANSFORMATION_FILENAME = "transformation.xml";

  public static final void generateJarFile( TransMeta transMeta ) {
    log = new LogChannel( "Jar file generator" );
    KettleDependencies deps = new KettleDependencies( transMeta );

    File kar = new File( "kar" );
    if ( kar.exists() ) {
      log.logBasic( "Jar generator", "Removing directory: " + kar.getPath() );
      deleteDirectory( kar );
    }
    kar.mkdir();

    String filename = "kettle-engine-3.0.jar";
    if ( !Utils.isEmpty( transMeta.getFilename() ) ) {
      filename = Const.replace( transMeta.getFilename(), " ", "_" ).toLowerCase() + ".kar";
    }

    File karFile = new File( filename );

    try {
      // The manifest file
      String strManifest = "";
      strManifest += "Manifest-Version: 1.0" + Const.CR;
      strManifest += "Created-By: Kettle version " + BuildVersion.getInstance().getVersion() + Const.CR;
      strManifest += Attributes.Name.MAIN_CLASS.toString() + ": " + ( JarPan.class.getName() ) + Const.CR;

      // Create a new manifest file in the root.
      File manifestFile = new File( kar.getPath() + "/" + "manifest.mf" );
      FileOutputStream fos = new FileOutputStream( manifestFile );
      fos.write( strManifest.getBytes() );
      fos.close();
      log.logBasic( "Jar generator", "Wrote manifest file: " + manifestFile.getPath() );

      // The transformation, also in the kar directory...
      String strTrans = XMLHandler.getXMLHeader( Const.XML_ENCODING ) + transMeta.getXML();
      File transFile = new File( kar.getPath() + "/" + TRANSFORMATION_FILENAME );
      fos = new FileOutputStream( transFile );
      fos.write( strTrans.getBytes( Const.XML_ENCODING ) );
      fos.close();
      log.logBasic( "Jar generator", "Wrote transformation file: " + transFile.getPath() );

      // Execute the jar command...
      executeJarCommand( kar, karFile, new File( "manifest.mf" ), new File( TRANSFORMATION_FILENAME ), deps
        .getLibraryFiles() );
    } catch ( Exception e ) {
      log.logError( JarfileGenerator.class.getName(), "Error zipping files into archive ["
        + karFile.getPath() + "] : " + e.toString() );
      log.logError( JarfileGenerator.class.getName(), Const.getStackTracker( e ) );
    }
  }

  private static final void executeJarCommand( File karDirectory, File karFile, File manifestFile, File transFile,
    String[] libs ) throws IOException, InterruptedException {
    for ( int i = 0; i < libs.length; i++ ) {
      List<String> commands = new ArrayList<String>();
      commands.add( "jar" );
      commands.add( "xf" );
      commands.add( "../" + libs[i] );

      String[] cmd = commands.toArray( new String[commands.size()] );
      executeCommand( cmd, karDirectory );
    }

    List<String> commands = new ArrayList<String>();
    commands.add( "jar" );
    commands.add( "cf" );
    commands.add( karFile.getPath() );
    commands.add( "-m" );
    commands.add( manifestFile.getPath() );
    commands.add( transFile.getPath() );
    commands.add( "build_version.txt" );
    commands.add( "log4j.xml" );
    String[] directories = getSubdirectories( karDirectory );
    for ( int i = 0; i < directories.length; i++ ) {
      if ( !directories[i].toUpperCase().equals( "META-INF" ) ) {
        commands.add( directories[i] );
      }
    }
    String[] cmd = commands.toArray( new String[commands.size()] );
    executeCommand( cmd, karDirectory );
  }

  private static void executeCommand( String[] cmd, File directory ) throws IOException, InterruptedException {
    String command = "";
    for ( int i = 0; i < cmd.length; i++ ) {
      command += " " + cmd[i];
    }
    log.logBasic( "Jar generator", "Executing command : " + command );

    Runtime runtime = java.lang.Runtime.getRuntime();
    Process proc = runtime.exec( cmd, EnvUtil.getEnvironmentVariablesForRuntimeExec(), directory );

    // any error message?
    StreamLogger errorLogger = new StreamLogger( log, proc.getErrorStream(), "Jar generator (stderr)" );

    // any output?
    StreamLogger outputLogger = new StreamLogger( log, proc.getInputStream(), "Jar generator (stdout)" );

    // kick them off
    new Thread( errorLogger ).start();
    new Thread( outputLogger ).start();

    proc.waitFor();
    log.logDetailed( "Jar generator", "command [" + cmd[0] + "] has finished" );

    // What's the exit status?
    if ( proc.exitValue() != 0 ) {
      log.logDetailed( "Jar generator", "Exit status of jar command was " + proc.exitValue() );
    }

    // close the streams
    // otherwise you get "Too many open files, java.io.IOException" after a lot of iterations
    try {
      proc.getErrorStream().close();
      proc.getInputStream().close();
    } catch ( IOException e ) {
      log.logDetailed( "Jar generator", "Warning: Error closing streams: " + e.getMessage() );
    }
  }

  private static void deleteDirectory( File dir ) {
    File[] files = dir.listFiles();
    for ( int i = 0; i < files.length; i++ ) {
      if ( files[i].isDirectory() ) {
        deleteDirectory( files[i] );
      }
      files[i].delete();
    }
    dir.delete();
  }

  private static String[] getSubdirectories( File dir ) {
    List<String> directories = new ArrayList<String>();
    File[] files = dir.listFiles();
    for ( int i = 0; i < files.length; i++ ) {
      if ( files[i].isDirectory() ) {
        directories.add( files[i].getName() );
      }
    }

    return directories.toArray( new String[directories.size()] );
  }
}
