package org.pentaho.di.compatibility;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * This class contains a Value of type Geometry (for GIS data)
 * 
 * @author edube
 * @since 25-09-2008
 *
 */
public class ValueGeometry implements ValueInterface, Cloneable
{
    protected Geometry geom;
    // private int length;

	public ValueGeometry()
	{
		this.geom = null;
		// this.length  = -1;
	}    

    public ValueGeometry(Geometry g) {
        this.geom = g;
        // this.length = -1;
    }

    public Geometry getGeometry() {
        return this.geom;
    }

    public void setGeometry(Geometry g) {
    	this.geom = g;
    }

    // implement with WKB support?
    public byte[] getBytes() {
    	return null;
    }
    
    public void setBytes(byte[] b) {
    }

    // implement with WKT support?
    public String getString() {
    	return WKTFromGeometry(geom);
    }

    public void setString(String string) {
    	geom = GeometryFromWKT(string);
    }
    
    public int getPrecision() {
        return -1;
    }
    
    public int getType() {
        return Value.VALUE_TYPE_GEOMETRY;
    }

    public String getTypeDesc() {
        return "Geometry";
    }

    public Object clone() {
        try {
            ValueGeometry retval = (ValueGeometry) super.clone();
            return retval;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public void setLength(int length) {
    	// this.length = length;
    }

    public void setLength(int length, int precision) {
    	// this.length = length;
    }

    public int getLength() {
        // return length;
    	return -1;
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

    public static Geometry GeometryFromWKT(String wkt) {
    	if(wkt==null) return null;
		Geometry geom;
    	try {
			geom = new WKTReader().read(wkt);
		} catch (com.vividsolutions.jts.io.ParseException e) {
			// TODO: log error
			// e.printStackTrace();
			geom = null;
		}
		return geom;
    }
    
    public static String WKTFromGeometry(Geometry g) {
    	if (g==null) return null;
    	return g.toText();
    }
}
