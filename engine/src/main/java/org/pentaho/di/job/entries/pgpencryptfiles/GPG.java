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

package org.pentaho.di.job.entries.pgpencryptfiles;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;

/**
 * This defines a GnuPG wrapper class.
 *
 * @author Samatar
 * @since 25-02-2011
 *
 */

public class GPG {

  private static Class<?> PKG = JobEntryPGPEncryptFiles.class; // for i18n purposes, needed by Translator2!!

  private LogChannelInterface log;

  private final String gnuPGCommand = "--batch --armor ";

  /** gpg program location **/
  private String gpgexe = "/usr/local/bin/gpg";

  /** temporary file create when running command **/
  private File tmpFile;

  /**
   * Reads an output stream from an external process. Implemented as a thread.
   */
  class ProcessStreamReader extends Thread {
    StringBuilder stream;
    InputStreamReader in;

    static final int BUFFER_SIZE = 1024;

    /**
     * Creates new ProcessStreamReader object.
     *
     * @param in
     */
    ProcessStreamReader( InputStream in ) {
      super();

      this.in = new InputStreamReader( in );

      this.stream = new StringBuilder();
    }

    public void run() {
      try {
        int read;
        char[] c = new char[BUFFER_SIZE];

        while ( ( read = in.read( c, 0, BUFFER_SIZE - 1 ) ) > 0 ) {
          stream.append( c, 0, read );
          if ( read < BUFFER_SIZE - 1 ) {
            break;
          }
        }
      } catch ( IOException io ) {
        // Ignore read errors
      }
    }

    String getString() {
      return stream.toString();
    }
  }

  /**
   * Constructs a new GnuPG
   *
   * @param gpgFilename
   *          gpg program location
   * @param logInterface
   *          LogChannelInterface
   * @throws KettleException
   */
  public GPG( String gpgFilename, LogChannelInterface logInterface ) throws KettleException {
    this.log = logInterface;
    this.gpgexe = gpgFilename;
    // Let's check GPG filename
    if ( Utils.isEmpty( getGpgExeFile() ) ) {
      // No filename specified
      throw new KettleException( BaseMessages.getString( PKG, "GPG.GPGFilenameMissing" ) );
    }
    // We have a filename, we need to check
    FileObject file = null;
    try {
      file = KettleVFS.getFileObject( getGpgExeFile() );

      if ( !file.exists() ) {
        throw new KettleException( BaseMessages.getString( PKG, "GPG.GPGFilenameNotFound" ) );
      }
      // The file exists
      if ( !file.getType().equals( FileType.FILE ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "GPG.GPGNotAFile", getGpgExeFile() ) );
      }

      // Ok we have a real file
      // Get the local filename
      this.gpgexe = KettleVFS.getFilename( file );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "GPG.ErrorCheckingGPGFile", getGpgExeFile() ), e );
    } finally {
      try {
        if ( file != null ) {
          file.close();
        }
      } catch ( Exception e ) {
        // Ignore close errors
      }
    }
  }

  /**
   * Returns GPG program location
   *
   * @return GPG filename
   */
  public String getGpgExeFile() {
    return this.gpgexe;
  }

  /**
   * Runs GnuPG external program
   *
   * @param commandArgs
   *          command line arguments
   * @param inputStr
   *          key ID of the key in GnuPG's key database
   * @param fileMode
   * @return result
   * @throws KettleException
   */
  private String execGnuPG( String commandArgs, String inputStr, boolean fileMode ) throws KettleException {
    Process p;
    String command = getGpgExeFile() + " " + ( fileMode ? "" : gnuPGCommand + " " ) + commandArgs;

    if ( log.isDebug() ) {
      log.logDebug( BaseMessages.getString( PKG, "GPG.RunningCommand", command ) );
    }
    String retval;

    try {
      if ( Const.isWindows() ) {
        p = Runtime.getRuntime().exec( command );
      } else {
        ProcessBuilder processBuilder = new ProcessBuilder( "/bin/sh", "-c", command );
        p = processBuilder.start();
      }
    } catch ( IOException io ) {
      throw new KettleException( BaseMessages.getString( PKG, "GPG.IOException" ), io );
    }

    ProcessStreamReader psr_stdout = new ProcessStreamReader( p.getInputStream() );
    ProcessStreamReader psr_stderr = new ProcessStreamReader( p.getErrorStream() );
    psr_stdout.start();
    psr_stderr.start();
    if ( inputStr != null ) {
      BufferedWriter out = new BufferedWriter( new OutputStreamWriter( p.getOutputStream() ) );
      try {
        out.write( inputStr );
      } catch ( IOException io ) {
        throw new KettleException( BaseMessages.getString( PKG, "GPG.ExceptionWrite" ), io );
      } finally {
        if ( out != null ) {
          try {
            out.close();
          } catch ( Exception e ) {
            // Ignore
          }
        }
      }
    }

    try {
      p.waitFor();

      psr_stdout.join();
      psr_stderr.join();
    } catch ( InterruptedException i ) {
      throw new KettleException( BaseMessages.getString( PKG, "GPG.ExceptionWait" ), i );
    }

    try {
      if ( p.exitValue() != 0 ) {
        throw new KettleException( BaseMessages.getString( PKG, "GPG.Exception.ExistStatus", psr_stderr
          .getString() ) );
      }
    } catch ( IllegalThreadStateException itse ) {
      throw new KettleException( BaseMessages.getString( PKG, "GPG.ExceptionillegalThreadStateException" ), itse );
    } finally {
      p.destroy();
    }

    retval = psr_stdout.getString();

    return retval;

  }

  /**
   * Decrypt a file
   *
   * @param cryptedFilename
   *          crypted filename
   * @param passPhrase
   *          passphrase for the personal private key to sign with
   * @param decryptedFilename
   *          decrypted filename
   * @throws KettleException
   */
  public void decryptFile( FileObject cryptedFilename, String passPhrase, FileObject decryptedFilename ) throws KettleException {

    decryptFile( KettleVFS.getFilename( cryptedFilename ), passPhrase, KettleVFS.getFilename( decryptedFilename ) );
  }

  /**
   * Decrypt a file
   *
   * @param cryptedFilename
   *          crypted filename
   * @param passPhrase
   *          passphrase for the personal private key to sign with
   * @param decryptedFilename
   *          decrypted filename
   * @throws KettleException
   */
  public void decryptFile( String cryptedFilename, String passPhrase, String decryptedFilename ) throws KettleException {

    try {
      execGnuPG( "--batch --yes "
        + ( Utils.isEmpty( passPhrase ) ? "" : "--passphrase " + "\"" + passPhrase + "\" " ) + "--output "
        + "\"" + decryptedFilename + "\" " + "--decrypt " + "\"" + cryptedFilename + "\"", null, true );

    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  /**
   * Encrypt a file
   *
   * @param filename
   *          file to encrypt
   * @param userID
   *          specific user id key
   * @param cryptedFilename
   *          crypted filename
   * @param asciiMode
   *          output ASCII file
   * @throws KettleException
   */
  public void encryptFile( FileObject filename, String userID, FileObject cryptedFilename, boolean asciiMode ) throws KettleException {
    encryptFile( KettleVFS.getFilename( filename ), userID, KettleVFS.getFilename( cryptedFilename ), asciiMode );
  }

  /**
   * Encrypt a file
   *
   * @param filename
   *          file to encrypt
   * @param userID
   *          specific user id key
   * @param cryptedFilename
   *          crypted filename
   * @param asciiMode
   *          output ASCII file
   * @throws KettleException
   */
  public void encryptFile( String filename, String userID, String cryptedFilename, boolean asciiMode ) throws KettleException {
    try {
      execGnuPG( "--batch --yes"
        + ( asciiMode ? " -a" : "" ) + " -r " + "\"" + Const.NVL( userID, "" ) + "\" " + "--output " + "\""
        + cryptedFilename + "\" " + "--encrypt  " + "\"" + filename + "\"", null, true );

    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  /**
   * Sign and encrypt a file
   *
   * @param file
   *          file to encrypt
   * @param userID
   *          specific user id key
   * @param cryptedFile
   *          crypted filename
   * @param asciiMode
   *          output ASCII file
   * @throws KettleException
   */
  public void signAndEncryptFile( FileObject file, String userID, FileObject cryptedFile, boolean asciiMode ) throws KettleException {
    signAndEncryptFile( KettleVFS.getFilename( file ), userID, KettleVFS.getFilename( cryptedFile ), asciiMode );
  }

  /**
   * Sign and encrypt a file
   *
   * @param filename
   *          file to encrypt
   * @param userID
   *          specific user id key
   * @param cryptedFilename
   *          crypted filename
   * @param asciiMode
   *          output ASCII file
   * @throws KettleException
   */
  public void signAndEncryptFile( String filename, String userID, String cryptedFilename, boolean asciiMode ) throws KettleException {

    try {

      execGnuPG(
        "--batch --yes"
          + ( asciiMode ? " -a" : "" ) + ( Utils.isEmpty( userID ) ? "" : " -r " + "\"" + userID + "\"" )
          + " " + "--output " + "\"" + cryptedFilename + "\" " + "--encrypt --sign " + "\"" + filename + "\"",
        null, true );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  /**
   * Sign a file
   *
   * @param filename
   *          file to encrypt
   * @param userID
   *          specific user id key
   * @param cryptedFilename
   *          crypted filename
   * @param asciiMode
   *          output ASCII file
   * @throws KettleException
   */
  public void signFile( String filename, String userID, String signedFilename, boolean asciiMode ) throws KettleException {
    try {
      execGnuPG( "--batch --yes"
        + ( asciiMode ? " -a" : "" ) + ( Utils.isEmpty( userID ) ? "" : " -r " + "\"" + userID + "\"" ) + " "
        + "--output " + "\"" + signedFilename + "\" " + ( asciiMode ? "--clearsign " : "--sign " ) + "\""
        + filename + "\"", null, true );

    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  /**
   * Sign a file
   *
   * @param file
   *          file to encrypt
   * @param userID
   *          specific user id key
   * @param signedFile
   *          crypted filename
   * @param asciiMode
   *          output ASCII file
   * @throws KettleException
   */
  public void signFile( FileObject file, String userID, FileObject signedFile, boolean asciiMode ) throws KettleException {
    try {
      signFile( KettleVFS.getFilename( file ), userID, KettleVFS.getFilename( signedFile ), asciiMode );

    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  /**
   * Verify a signature
   *
   * @param filename
   *          filename
   * @throws KettleException
   */
  public void verifySignature( FileObject filename ) throws KettleException {
    verifySignature( KettleVFS.getFilename( filename ) );
  }

  /**
   * Verify a signature
   *
   * @param filename
   *          filename
   * @originalFilename fill this value in case of detached signature
   * @throws KettleException
   */
  public void verifySignature( String filename ) throws KettleException {

    execGnuPG( "--batch --verify " + "\"" + filename + "\"", null, true );
  }

  /**
   * Verify a signature for detached file
   *
   * @param signatureFilename
   *          filename
   * @param originalFilenamefill
   *          this value in case of detached signature
   * @throws KettleException
   */
  public void verifyDetachedSignature( String signatureFilename, String originalFilename ) throws KettleException {
    execGnuPG( "--batch --verify " + "\"" + signatureFilename + "\" " + "\"" + originalFilename + "\"", null, true );
  }

  /**
   * Verify a signature for detached file
   *
   * @param signatureFile
   *          filename
   * @param originalFile
   *          fill this value in case of detached signature
   * @throws KettleException
   */
  public void verifyDetachedSignature( FileObject signatureFile, FileObject originalFile ) throws KettleException {
    verifyDetachedSignature( KettleVFS.getFilename( signatureFile ), KettleVFS.getFilename( originalFile ) );
  }

  /**
   * Encrypt a string
   *
   * @param plainText
   *          input string to encrypt
   * @param keyID
   *          key ID of the key in GnuPG's key database to encrypt with
   * @return encrypted string
   * @throws KettleException
   */
  public String encrypt( String plainText, String keyID ) throws KettleException {
    return execGnuPG( "-r \"" + keyID + "\" --encrypt ", plainText, false );

  }

  /**
   * Signs and encrypts a string
   *
   * @param plainText
   *          input string to encrypt
   * @param userID
   *          key ID of the key in GnuPG's key database to encrypt with
   * @param passPhrase
   *          passphrase for the personal private key to sign with
   * @return encrypted string
   * @throws KettleException
   */
  public String signAndEncrypt( String plainText, String userID, String passPhrase ) throws KettleException {
    try {
      createTempFile( plainText );

      return execGnuPG(
        "-r \"" + userID + "\" --passphrase-fd 0 -se \"" + getTempFileName() + "\"", passPhrase, false );
    } finally {

      deleteTempFile();
    }

  }

  /**
   * Sign
   *
   * @param stringToSign
   *          input string to sign
   * @param passPhrase
   *          passphrase for the personal private key to sign with
   * @throws KettleException
   */
  public String sign( String stringToSign, String passPhrase ) throws KettleException {
    String retval;
    try {

      createTempFile( stringToSign );

      retval = execGnuPG( "--passphrase-fd 0 --sign \"" + getTempFileName() + "\"", passPhrase, false );

    } finally {
      deleteTempFile();
    }
    return retval;
  }

  /**
   * Decrypt a string
   *
   * @param cryptedText
   *          input string to decrypt
   * @param passPhrase
   *          passphrase for the personal private key to sign with
   * @return plain text
   * @throws KettleException
   */
  public String decrypt( String cryptedText, String passPhrase ) throws KettleException {
    try {
      createTempFile( cryptedText );

      return execGnuPG( "--passphrase-fd 0 --decrypt \"" + getTempFileName() + "\"", passPhrase, false );

    } finally {
      deleteTempFile();
    }
  }

  /**
   * Create a unique temporary file when needed by one of the main methods. The file handle is store in tmpFile object
   * var.
   *
   * @param content
   *          data to write into the file
   * @throws KettleException
   */
  private void createTempFile( String content ) throws KettleException {
    this.tmpFile = null;
    FileWriter fw;

    try {
      this.tmpFile = File.createTempFile( "GnuPG", null );
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "GPG.TempFileCreated", getTempFileName() ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "GPG.ErrorCreatingTempFile" ), e );
    }

    try {
      fw = new FileWriter( this.tmpFile );
      fw.write( content );
      fw.flush();
      fw.close();
    } catch ( Exception e ) {
      // delete our file:
      deleteTempFile();

      throw new KettleException( BaseMessages.getString( PKG, "GPG.ErrorWritingTempFile" ), e );
    }
  }

  /**
   * Delete temporary file.
   *
   * @throws KettleException
   */
  private void deleteTempFile() {
    if ( this.tmpFile != null ) {
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "GPG.DeletingTempFile", getTempFileName() ) );
      }
      this.tmpFile.delete();
    }
  }

  /**
   * Returns temporary filename.
   *
   * @return temporary filename
   */
  private String getTempFileName() {
    return this.tmpFile.getAbsolutePath();
  }

  public String toString() {
    return "GPG";
  }

}
