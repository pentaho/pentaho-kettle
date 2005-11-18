 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 
package be.ibridge.kettle.core;
import java.math.BigInteger;

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

}
