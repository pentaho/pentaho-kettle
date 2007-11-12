 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

 

package org.pentaho.di.compatibility;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * This interface provides a way to look at a Number, String, Integer, Date... the same way.
 * The methods mentioned in this interface are common to all Value types.
 *  
 * @author Matt
 * @since  15-10-2004
 */
public interface ValueInterface
{
	public int        getType();
	public String     getTypeDesc();

	public String     getString();
	public double     getNumber();
	public Date       getDate();
	public boolean    getBoolean();
	public long       getInteger();
    public BigDecimal getBigNumber();
	public Serializable getSerializable();
    public byte[]     getBytes();
    
	public void       setString(String string);
	public void       setNumber(double number);
	public void       setDate(Date date);
	public void       setBoolean(boolean bool);
	public void       setInteger(long number);
    public void       setBigNumber(BigDecimal number);
	public void       setSerializable(Serializable ser);
	public void       setBytes(byte[] b);
        
	public int        getLength();
	public int        getPrecision();
	public void       setLength(int length);
	public void       setPrecision(int precision);
	public void       setLength(int length, int precision);
	public Object     clone();
}
