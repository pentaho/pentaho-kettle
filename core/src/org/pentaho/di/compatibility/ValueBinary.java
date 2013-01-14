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
