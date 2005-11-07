 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/
 

package be.ibridge.kettle.core.value;


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
	
	public void       setString(String string);
	public void       setNumber(double number);
	public void       setDate(Date date);
	public void       setBoolean(boolean bool);
	public void       setInteger(long number);
    public void       setBigNumber(BigDecimal number);
	
	public int        getLength();
	public int        getPrecision();
	public void       setLength(int length);
	public void       setPrecision(int precision);
	public void       setLength(int length, int precision);
	public Object     clone();
}
