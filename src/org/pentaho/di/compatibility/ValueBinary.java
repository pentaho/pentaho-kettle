/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.compatibility;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * This class contains a Value of type Binary. It's supposed to contain
 * CLOBS, LOBS, ... GIF data, jpg's, ...
 */
public class ValueBinary implements ValueInterface, Cloneable {

    protected byte[] bytes;
    private int length;

	public ValueBinary()
	{
		this.bytes   = null;
		this.length  = -1;
	}    

    public ValueBinary(byte[] bytes) {
        this.bytes = bytes;
        this.length = -1;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] b) {
    	this.bytes = b;
    }

    public int getPrecision() {
        return 0;
    }

    public String getString() {
        return (bytes != null) ? new String(bytes) : null;
    }

    public int getType() {
        return Value.VALUE_TYPE_BINARY;
    }

    public String getTypeDesc() {
        return "Binary";
    }

    public Object clone() {
        try {
            ValueBinary retval = (ValueBinary) super.clone();
            return retval;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public void setLength(int length) {
    	this.length = length;
    }

    public void setLength(int length, int precision) {
    	this.length = length;
    }

    public int getLength() {
        return length;
    }

    //These dont do anything but are needed for the ValueInterface
	public Serializable getSerializable() {
		return null;
	}

    public void setBigNumber(BigDecimal number) {
    }

    public void setBoolean(boolean bool) {
    }

    public void setDate(Date date) {
    }

    public void setInteger(long number) {
    }

    public void setNumber(double number) {
    }

    public void setPrecision(int precision) {
    }

    public void setString(String string) {
    }

    public void setSerializable(Serializable ser) {        
    }

    public BigDecimal getBigNumber() {
        return null;
    }

    public boolean getBoolean() {
        return false;
    }

    public Date getDate() {
        return null;
    }

    public long getInteger() {
        return 0;
    }

    public double getNumber() {
        return 0;
    }    
}
