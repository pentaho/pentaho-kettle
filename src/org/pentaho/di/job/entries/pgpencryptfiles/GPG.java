
package org.pentaho.di.job.entries.pgpencryptfiles;

import java.io.BufferedWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
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
	
	private static Class<?> PKG = JobEntryPGPEncryptFiles.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private LogChannelInterface	log;

	private final String gnuPGCommand = "--batch --armor --output -";
	
	/** gpg program location **/
	private String	gpgexe="/usr/local/bin/gpg";
	
	
	/**
	 * Reads an output stream from an external process.
	 * Implemented as a thread.
	 */
	class ProcessStreamReader extends Thread {
		StringBuffer		stream;
		InputStreamReader	in;
		
		final static int BUFFER_SIZE = 1024;
		
		/**
		 *	Creates new ProcessStreamReader object.
		 *	
		 *	@param	in
		 */
		ProcessStreamReader (InputStream in) {
			super();
			
			this.in = new InputStreamReader(in);

			this.stream = new StringBuffer();
		}
		
		public void run() {
			try {       
				int read;
				char[] c = new char[BUFFER_SIZE];
				
				while ((read = in.read(c, 0, BUFFER_SIZE - 1)) > 0) {
					stream.append(c, 0, read);
					if (read < BUFFER_SIZE - 1) break;
				}
			}
			catch(IOException io) {}
		}
		
		String getString() {
				return stream.toString();
		}
	}
	
	
	/**
	 * Constructs a new GnuPG
	 *
	 * @param	gpgFilename	gpg program location
	 * @param	logInterface	LogChannelInterface
	 * @throws  KettleException
	 */
	public GPG(String gpgFilename, LogChannelInterface logInterface) throws KettleException {
		this.log=logInterface;
		this.gpgexe=gpgFilename;
		if(Const.isEmpty(getGpgExeFile())) {
			throw new KettleException(BaseMessages.getString(PKG, "GPG.GPGFilenameMissing"));
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
	 * @param	commandArgs	command line arguments
	 * @param	inputStr	key ID of the key in GnuPG's key database
	 * @param	fileMode	
	 * @return	result
	 * @throws KettleException
	 */
	private String execGnuPG (String commandArgs, String inputStr, boolean fileMode) 
	throws KettleException {
		Process		p;
		String		command = getGpgExeFile() + " " + (fileMode?"":gnuPGCommand + " ")  + commandArgs;
	
		if(log.isDebug()) {
			log.logDebug(toString(), BaseMessages.getString(PKG, "GPG.RunningCommand", command));
		}
		String retval;

		try {
			p = Runtime.getRuntime().exec(command);
		} catch(IOException io) {
			throw new KettleException(BaseMessages.getString(PKG, "GPG.IOException"), io);
		}
		
		ProcessStreamReader psr_stdout = new ProcessStreamReader(p.getInputStream());
		ProcessStreamReader psr_stderr = new ProcessStreamReader(p.getErrorStream());
		psr_stdout.start();
		psr_stderr.start();
		if (inputStr != null) {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			try {
				out.write(inputStr);
			} catch(IOException io) {
				throw new KettleException(BaseMessages.getString(PKG, "GPG.ExceptionWrite"), io);
			}finally {
				if(out!=null) 	{
					try {
						out.close();
					}catch(Exception e){};
				}
			}
		}
		
		try {
			p.waitFor();
			
			psr_stdout.join();
			psr_stderr.join();
		} catch(InterruptedException i) {
			throw new KettleException(BaseMessages.getString(PKG, "GPG.ExceptionWait"),i);
		}
		
		try {
			if(p.exitValue ()!=0) {
				throw new KettleException(BaseMessages.getString(PKG, "GPG.Exception.ExistStatus", psr_stderr.getString()));
			}
		} catch (IllegalThreadStateException itse) {
			throw new KettleException(BaseMessages.getString(PKG, "GPG.ExceptionillegalThreadStateException"), itse);
		} finally {
			p.destroy();
		}
		
		retval = psr_stdout.getString();
		
		return retval;
		
	}
	
	
	/**
	 * Decrypt a file
	 *
	 * @param	cryptedFilename		crypted filename
	 * @param	passPhrase		passphrase for the personal private key to sign with
	 * @param	decryptedFilename		decrypted filename
	 * @throws KettleException
	 */
	public void decryptFile (FileObject cryptedFilename, String passPhrase, FileObject decryptedFilename) 
	throws KettleException {
		
		decryptFile (KettleVFS.getFilename(cryptedFilename), passPhrase, KettleVFS.getFilename(decryptedFilename)); 
	}
	
	/**
	 * Decrypt a file
	 *
	 * @param	cryptedFilename		crypted filename
	 * @param	passPhrase		passphrase for the personal private key to sign with
	 * @param	decryptedFilename		decrypted filename
	 * @throws KettleException
	 */
	public void decryptFile (String cryptedFilename, String passPhrase, String decryptedFilename) 
	throws KettleException {

		try {
			 execGnuPG ("--batch --yes " 
					 + (Const.isEmpty(passPhrase)?"":"--passphrase " + "\"" + passPhrase + "\" ")
					 + "--output " + "\"" + decryptedFilename + "\" " 
					 + "--decrypt " + "\"" + cryptedFilename + "\"", 
					 null, true);

		}catch(Exception e) {
			throw new KettleException(e);
		}
	}
	

	
	/**
	 * Encrypt a file
	 *
	 * @param	filename		file to encrypt
	 * @param 	userID  specific user id key
	 * @param	cryptedFilename crypted filename
	 * @param	asciiMode output ASCII file
	 * @throws  KettleException
	 */
	public void encryptFile (FileObject filename, String userID, FileObject cryptedFilename, boolean asciiMode) 
	throws KettleException {
		encryptFile (KettleVFS.getFilename(filename), userID, KettleVFS.getFilename(cryptedFilename), asciiMode); 
	}
	
	/**
	 * Encrypt a file
	 *
	 * @param	filename		file to encrypt
	 * @param 	userID  specific user id key
	 * @param	cryptedFilename crypted filename
	 * @param	asciiMode output ASCII file
	 * @throws  KettleException
	 */
	public void encryptFile (String filename, String userID, String cryptedFilename, boolean asciiMode) 
	throws KettleException {
		try {
			 execGnuPG ("--batch --yes" + (asciiMode?" -a":"") + " -r " + "\"" + Const.NVL(userID, "") + "\" " 
					+ "--output " + "\"" + cryptedFilename + "\" " 
					+ "--encrypt  " + "\"" + filename + "\"", 
					 null, true);

		}catch(Exception e) {
			throw new KettleException(e);
		}
	}
	
	
	
	/**
	 * Sign and encrypt a file
	 *
	 * @param	file		file to encrypt
	 * @param 	userID  specific user id key
	 * @param	cryptedFile crypted filename
	 * @param	asciiMode output ASCII file
	 * @throws 	KettleException
	 */
	public void signAndEncryptFile (FileObject file, String userID, FileObject cryptedFile, boolean asciiMode) 
	throws KettleException {
		signAndEncryptFile (KettleVFS.getFilename(file), userID, KettleVFS.getFilename(cryptedFile), asciiMode); 
	}
	
	/**
	 * Sign and encrypt a file
	 *
	 * @param	filename		file to encrypt
	 * @param 	userID  specific user id key
	 * @param	cryptedFilename crypted filename
	 * @param	asciiMode output ASCII file
	 * @throws 	KettleException
	 */
	public void signAndEncryptFile (String filename, String userID, String cryptedFilename, boolean asciiMode) 
	throws KettleException {

		try {

			execGnuPG ("--batch --yes" + (asciiMode?" -a":"") + (Const.isEmpty(userID)?"": " -r " + "\"" + userID + "\"") + " "  
					+ "--output " + "\"" + cryptedFilename + "\" " 
					+ "--encrypt --sign " + "\"" + filename + "\"", 
					 null, true);
		}catch(Exception e) {
			throw new KettleException(e);
		}
	}
	
	/**
	 * Sign  a file
	 *
	 * @param	filename		file to encrypt
	 * @param 	userID  specific user id key
	 * @param	cryptedFilename crypted filename
	 * @param	asciiMode output ASCII file
	 * @throws 	KettleException
	 */
	public void signFile (String filename, String userID, String signedFilename, boolean asciiMode) 
	throws KettleException {
		try {
			execGnuPG ("--batch --yes" +  (asciiMode?" -a":"") + (Const.isEmpty(userID)?"": " -r " + "\"" + userID + "\"") + " " 
					+ "--output " + "\"" + signedFilename + "\" " 
					+ (asciiMode?"--clearsign ":"--sign ") + "\"" + filename + "\"", 
					 null, true);

		}catch(Exception e) {
			throw new KettleException(e);
		}
	}
	/**
	 * Sign  a file
	 *
	 * @param	file		file to encrypt
	 * @param 	userID  specific user id key
	 * @param	signedFile crypted filename
	 * @param	asciiMode output ASCII file
	 * @throws 	KettleException
	 */
	public void signFile (FileObject file, String userID, FileObject signedFile, boolean asciiMode) 
	throws KettleException {
		try {
			signFile (KettleVFS.getFilename(file), userID, KettleVFS.getFilename(signedFile), asciiMode); 

		}catch(Exception e) {
			throw new KettleException(e);
		}
	}
	
	/**
	 * Verify a signature
	 *
	 * @param	filename filename
	 * @throws KettleException
	 */
	public void verifySignature (FileObject filename) 
	throws KettleException {
		verifySignature(KettleVFS.getFilename(filename));
	}
	
	/**
	 * Verify a signature
	 *
	 * @param	filename filename
	 * @originalFilename fill this value in case of detached signature
	 * @throws KettleException
	 */
	public void verifySignature (String filename) 
	throws KettleException {

		execGnuPG ("--batch --verify " +   "\"" + filename + "\"", null, true);
	}
	
	/**
	 * Verify a signature for detached file
	 *
	 * @param  signatureFilename filename
	 * @param  originalFilenamefill this value in case of detached signature
	 * @throws KettleException
	 */
	public void verifyDetachedSignature (String signatureFilename, String originalFilename) 
	throws KettleException {
		execGnuPG ("--batch --verify " +   "\"" + signatureFilename + "\" "
				+ "\"" + originalFilename + "\"",  
				 null, true);
	}
	
	/**
	 * Verify a signature for detached file
	 *
	 * @param signatureFile filename
	 * @param originalFile fill this value in case of detached signature
	 * @throws KettleException
	 */
	public void verifyDetachedSignature (FileObject signatureFile, FileObject originalFile) 
	throws KettleException {
		verifyDetachedSignature(KettleVFS.getFilename(signatureFile), KettleVFS.getFilename(originalFile));
	}
	
	public String toString() {
	  return "GPG";
	}

}
