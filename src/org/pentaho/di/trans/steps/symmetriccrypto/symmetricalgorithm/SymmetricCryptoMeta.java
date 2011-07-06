 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm;

import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;


/**
 * Symmetric algorithm 
 * @author Samatar
 * @since 01-4-2011
 */
public class SymmetricCryptoMeta {
   
	 private static Class<?> PKG = SymmetricCryptoMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	 private SymmetricCryptoInterface cryptographyInterface;
	 private static SymmetricCryptoInterface[] allSymmetricCryptoInterface;
	 
	 public static final String[] TYPE_ALGORYTHM_CODE = new String[]{
		 "DES", "DESede", "AES"
	 };
	 
	/**
	 * Indicates that the algorithm doesn't point to a type of algorithm yet.
	 */
	public static final int TYPE_ALGORYTHM_NONE        =  0;
	public static final int TYPE_ALGORYTHM_DES        =  1;
	public static final int TYPE_ALGORYTHM_TRIPLE_DES  =  2;
	public static final int TYPE_ALGORYTHM_AES        =  3;

 
	public static final String ALGORYTHM_DES        =  "DES";
	public static final String ALGORYTHM_TRIPLE_DES =  "DESede";
	public static final String ALGORYTHM_AES        =  "AES";
	
 
	/**
	 * Construct a new Database Connection
	 * @param inf The Database Connection Info to construct the connection with.
	 */
	public SymmetricCryptoMeta(String algorythm) throws CryptoException {
		cryptographyInterface=getSymmetricCryptoInterface(algorythm);
	}

    
	/**
	 * Search for the right type of DatabaseInterface object and clone it.
	 * 
	 * @param databaseType the type of DatabaseInterface to look for (description)
	 * @return The requested DatabaseInterface
	 * 
	 * @throws KettleDatabaseException when the type could not be found or referenced.
	 */
	public static final SymmetricCryptoInterface getSymmetricCryptoInterface(String cryptoname) 
	throws CryptoException {
		return (SymmetricCryptoInterface)findSymmetricCryptoInterface(cryptoname);
		
	}
	/**
	 * Search for the right type of DatabaseInterface object and return it.
	 * 
	 * @param databaseType the type of DatabaseInterface to look for (description)
	 * @return The requested DatabaseInterface
	 * 
	 * @throws KettleDatabaseException when the type could not be found or referenced.
	 */
	private static final SymmetricCryptoInterface findSymmetricCryptoInterface(String cryptograhname) 
	throws CryptoException {
		SymmetricCryptoInterface di[] = getSymmetricCryptoInterfaces();
		for (int i=0;i<di.length;i++) {
			if (di[i].getAlgorithm().equalsIgnoreCase(cryptograhname)) {
				 return di[i];
			}
		}
		
		throw new CryptoException(BaseMessages.getString(PKG, "SymmetricCryptoMeta.CouldNotFoundAlgorithm", cryptograhname));
	}


	public static final SymmetricCryptoInterface[] getSymmetricCryptoInterfaces() {
		if (allSymmetricCryptoInterface!=null) return allSymmetricCryptoInterface;
		
		Class<?> ic[] = SymmetricCryptoInterface.implementingClasses;
		allSymmetricCryptoInterface = new SymmetricCryptoInterface[ic.length];
		for (int i=0;i<ic.length;i++) {
			try {
				Class.forName(ic[i].getName());
				allSymmetricCryptoInterface[i] = (SymmetricCryptoInterface)ic[i].newInstance();
			} catch(Exception e){
				throw new RuntimeException("Error creating class for : "+ic[i].getName(), e);
			}
		}
		return allSymmetricCryptoInterface;
	}

	  public String getAlgorithm() {
		 return cryptographyInterface.getAlgorithm(); 
	  }
	  
	  public int getAlgorithmType() {
		 return cryptographyInterface.getAlgorithmType(); 
	  }
	  
	  public String getDefaultScheme() {
		 return cryptographyInterface.getDefaultScheme(); 
	  }
	  
	  public static int getAlgorithmTypeFromCode(String code) {
		  if(!Const.isEmpty(code)) {
			  int nr  = TYPE_ALGORYTHM_CODE.length;
			  for(int i=0; i<nr; i++) {
				  if(TYPE_ALGORYTHM_CODE[i].equals(code)) return i+1;
			  }
		  }
		  return TYPE_ALGORYTHM_NONE;
	  }
		  
}