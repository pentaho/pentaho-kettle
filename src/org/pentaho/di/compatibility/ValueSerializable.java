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
