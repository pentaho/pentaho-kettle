/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.encryption;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.mortbay.jetty.security.Password;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.StringUtil;


/**
 * This class handles basic encryption of passwords in Kettle.
 * Note that it's not really encryption, it's more obfuscation.  Passwords are <b>difficult</b> to read, not impossible.
 * 
 * @author Matt
 * @since 17-12-2003
 *
 */ 
public class Encr 
{
	private static final int RADIX = 16;
	private static final String SEED = "0933910847463829827159347601486730416058";
	
	public Encr() 
	{
	}
	
	public boolean init()
	{
		return true;
	}
	
	public String buildSignature(String mac, String username, String company, String products)
	{
		try
		{
			BigInteger bi_mac      = new BigInteger(mac.getBytes());
			BigInteger bi_username = new BigInteger(username.getBytes());
			BigInteger bi_company  = new BigInteger(company.getBytes());
			BigInteger bi_products = new BigInteger(products.getBytes());
			
			BigInteger bi_r0 = new BigInteger(SEED);
			BigInteger bi_r1 = bi_r0.xor(bi_mac);
			BigInteger bi_r2 = bi_r1.xor(bi_username);
			BigInteger bi_r3 = bi_r2.xor(bi_company);
			BigInteger bi_r4 = bi_r3.xor(bi_products);
			
			return bi_r4.toString(RADIX);
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	public static final boolean checkSignatureShort(String signature, String verify)
	{
		return getSignatureShort(signature).equalsIgnoreCase(verify);
	}

	public static final String getSignatureShort(String signature)
	{
		String retval="";
		if (signature==null) return retval;
		int len = signature.length();
		if (len<6) return retval;
		retval=signature.substring(len-5, len);
		
 		return retval;
	}
	
	
	public static final String encryptPassword(String password)
	{
		if (password==null) return "";
		if (password.length()==0) return "";
		
		BigInteger bi_passwd = new BigInteger(password.getBytes());
		
		BigInteger bi_r0  = new BigInteger(SEED);
		BigInteger bi_r1  = bi_r0.xor(bi_passwd);
		
		return bi_r1.toString(RADIX); 
	}

	public static final String decryptPassword(String encrypted)
	{
		if (encrypted==null) return "";
		if (encrypted.length()==0) return "";
		
		BigInteger bi_confuse  = new BigInteger(SEED);
		
		try
		{
			BigInteger bi_r1 = new BigInteger(encrypted, RADIX);
			BigInteger bi_r0 = bi_r1.xor(bi_confuse);
			
			return new String(bi_r0.toByteArray()); 
		}
		catch(Exception e)
		{
			return "";
		}
	}
    
    /** The word that is put before a password to indicate an encrypted form.  If this word is not present, the password is considered to be NOT encrypted */
    public  static final String PASSWORD_ENCRYPTED_PREFIX = "Encrypted ";
    
    /**
     * Encrypt the password, but only if the password doesn't contain any variables.
     * @param password The password to encrypt
     * @return The encrypted password or the  
     */
    public static final String encryptPasswordIfNotUsingVariables(String password)
    {
        String encrPassword = "";
        List<String> varList = new ArrayList<String>();
        StringUtil.getUsedVariables(password, varList, true);
        if (varList.isEmpty())
        {
            encrPassword = PASSWORD_ENCRYPTED_PREFIX+Encr.encryptPassword(password);
        }
        else
        {
            encrPassword = password;
        }
        
        return encrPassword;
    }

    /**
     * Decrypts a password if it contains the prefix "Encrypted "
     * @param password The encrypted password
     * @return The decrypted password or the original value if the password doesn't start with "Encrypted "
     */
    public static final String decryptPasswordOptionallyEncrypted(String password)
    {
        if (!Const.isEmpty(password) && password.startsWith(PASSWORD_ENCRYPTED_PREFIX)) 
        {
            return Encr.decryptPassword( password.substring(PASSWORD_ENCRYPTED_PREFIX.length()) );
        }
        return password;
    }
    
    /**
     * Create an encrypted password
     * 
     * @param args the password to encrypt
     */
    public static void main(String[] args) {
		if (args.length!=2) {
			printOptions();
			System.exit(9);
		}

		String option = args[0];
		String password = args[1];

		if (Const.trim(option).substring(1).equalsIgnoreCase("kettle")) {
			// Kettle password obfuscation
			//
			String obfuscated = Encr.encryptPassword(password);
			System.out.println(PASSWORD_ENCRYPTED_PREFIX+obfuscated);
			System.exit(0);
			
		} else if (Const.trim(option).substring(1).equalsIgnoreCase("carte")) {
			// Jetty password obfuscation
			//
			String obfuscated = Password.obfuscate(password);
			System.out.println(obfuscated);
			System.exit(0);
			
		} else {
			// Unknown option, print usage
			//
			System.err.println("Unknown option '"+option+"'\n");
			printOptions();
			System.exit(1);
		}

		
		
	}

	private static void printOptions() {
		System.err.println("encr usage:\n");
		System.err.println("  encr <-kettle|-carte> <password>");
		System.err.println("  Options:");
		System.err.println("    -kettle: generate an obfuscated password to include in Kettle XML files");
		System.err.println("    -carte: generate an obfuscated password to include in the carte password file 'pwd/kettle.pwd'");
		System.err.println("\nThis command line tool obfuscates a plain text password for use in XML and password files.");
		System.err.println("Make sure to also copy the '"+PASSWORD_ENCRYPTED_PREFIX+"' prefix to indicate the obfuscated nature of the password.");
		System.err.println("Kettle will then be able to make the distinction between regular plain text passwords and obfuscated ones.");
		System.err.println();
	}
}
