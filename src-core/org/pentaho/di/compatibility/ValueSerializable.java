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

public class ValueSerializable implements ValueInterface, Cloneable {
    
    protected Serializable serializable;
    
    public ValueSerializable(Serializable ser) {
        this.serializable = ser;
    }
    
    public Serializable getSerializable() {
        return serializable;
    }

    public int getPrecision() {
        return 0;
    }

    public String getString() {
        return (serializable != null) ? serializable.toString() : null;
    }

    public int getType() {
        return Value.VALUE_TYPE_SERIALIZABLE;
    }

    public String getTypeDesc() {
        return "Object";
    }

    public Object clone() {
        try {
            ValueSerializable retval = (ValueSerializable) super.clone();
            return retval;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }


    //These dont do anything but are needed for the ValueInterface
    public void setBigNumber(BigDecimal number) {
    }

    public void setBoolean(boolean bool) {
    }

    public void setDate(Date date) {
    }

    public void setInteger(long number) {
    }

    public void setLength(int length, int precision) {
    }

    public void setLength(int length) {
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

    public int getLength() {
        return 0;
    }

    public double getNumber() {
        return 0;
    }

	public byte[] getBytes() {
		return null;
	}

	public void setBytes(byte[] b) {
	}
}
