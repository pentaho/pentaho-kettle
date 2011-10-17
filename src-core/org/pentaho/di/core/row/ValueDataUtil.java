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
package org.pentaho.di.core.row;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.zip.CheckedInputStream;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.security.MessageDigest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.codec.language.RefinedSoundex;
import org.apache.commons.codec.language.Soundex;

import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLCheck;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.provider.local.LocalFile;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.fileinput.CharsetToolkit;

import com.wcohen.ss.Jaro;
import com.wcohen.ss.JaroWinkler;
import com.wcohen.ss.NeedlemanWunsch;

public class ValueDataUtil
{
    /**
     * @deprecated Use {@link Const#ltrim(String)} instead
     */
    public static final String leftTrim(String string)
    {
        return Const.ltrim(string);
    }
    
    /**
     * @deprecated Use {@link Const#rtrim(String)} instead
     */
    public static final String rightTrim(String string)
    {
        return Const.rtrim(string);
    }

    /**
     * Determines whether or not a character is considered a space.
     * A character is considered a space in Kettle if it is a space, a tab, a newline or a cariage return.
     * @param c The character to verify if it is a space.
     * @return true if the character is a space. false otherwise. 
     * @deprecated Use {@link Const#isSpace(char)} instead
     */
    public static final boolean isSpace(char c)
    {
        return Const.isSpace(c);
    }
    
    /**
     * Trims a string: removes the leading and trailing spaces of a String.
     * @param string The string to trim
     * @return The trimmed string.
     * @deprecated Use {@link Const#trim(String)} instead
     */
    public static final String trim(String string)
    {
        return Const.trim(string);
    }
    
    /**Levenshtein distance (LD) is a measure of the similarity between two strings, 
     * which we will refer to as the source string (s) and the target string (t). 
     * The distance is the number of deletions, insertions, or substitutions required to transform s into t. 
     */
    public static Long getLevenshtein_Distance(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB)
    {
    	if(dataA==null || dataB==null) return null;
    	return new Long(StringUtils.getLevenshteinDistance(dataA.toString(),dataB.toString()));
    }
    /**DamerauLevenshtein distance is a measure of the similarity between two strings, 
     * which we will refer to as the source string (s) and the target string (t). 
     * The distance is the number of deletions, insertions, or substitutions required to transform s into t. 
     */
    public static Long getDamerauLevenshtein_Distance(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB)
    {
    	if(dataA==null || dataB==null) return null;
    	return new Long(Utils.getDamerauLevenshteinDistance(dataA.toString(),dataB.toString()));
    }
    /**NeedlemanWunsch distance is a measure of the similarity between two strings, 
     * which we will refer to as the source string (s) and the target string (t). 
     * The distance is the number of deletions, insertions, or substitutions required to transform s into t. 
     */
    public static Long getNeedlemanWunsch_Distance(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB)
    {
    	if(dataA==null || dataB==null) return null;
    	return new Long((int) new NeedlemanWunsch().score(dataA.toString(),dataB.toString()));
    }
    /**Jaro similitude is a measure of the similarity between two strings, 
     * which we will refer to as the source string (s) and the target string (t).  
     */
    public static Double getJaro_Similitude(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB)
    {
    	if(dataA==null || dataB==null) return null;
    	return new Double(new Jaro().score(dataA.toString(),dataB.toString()));
    }
    /**JaroWinkler similitude is a measure of the similarity between two strings, 
     * which we will refer to as the source string (s) and the target string (t).  
     */
    public static Double getJaroWinkler_Similitude(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB)
    {
    	if(dataA==null || dataB==null) return null;
    	return new Double(new JaroWinkler().score(dataA.toString(),dataB.toString()));
    }
    public static String get_Metaphone(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return (new Metaphone()).metaphone(dataA.toString());
    }
    
    public static String get_Double_Metaphone(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return (new DoubleMetaphone()).doubleMetaphone(dataA.toString());
    }
    public static String get_SoundEx(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return (new Soundex()).encode(dataA.toString());
    }
    public static String get_RefinedSoundEx(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return (new RefinedSoundex()).encode(dataA.toString());
    }
    public static String initCap(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return Const.initCap(dataA.toString());
    }
    public static String upperCase(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return dataA.toString().toUpperCase();
    }
    public static String lowerCase(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return dataA.toString().toLowerCase();
    }
    public static String escapeXML(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return Const.escapeXML(dataA.toString());
    }
    public static String unEscapeXML(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return Const.unEscapeXml(dataA.toString());
    }
    public static String escapeHTML(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return Const.escapeHtml(dataA.toString());
    }
    public static String unEscapeHTML(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return Const.unEscapeHtml(dataA.toString());
    }
    public static String escapeSQL(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return Const.escapeSQL(dataA.toString());
    }
    public static String useCDATA(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return "<![CDATA["+dataA.toString()+"]]>";
    	
    }
    public static String removeCR(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return Const.removeCR(dataA.toString());
    }
    public static String removeLF(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return Const.removeLF(dataA.toString());
    }
    public static String removeCRLF(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return Const.removeCRLF(dataA.toString());
    }
    public static String removeTAB(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return Const.removeTAB(dataA.toString());
    }
    public static String getDigits(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return Const.getDigitsOnly(dataA.toString());
    }
    public static String removeDigits(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return null;
    	return Const.removeDigits(dataA.toString());
    }
    public static long stringLen(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return 0;
    	return dataA.toString().length();
    }
    public static String createChecksum(ValueMetaInterface metaA, Object dataA, String type)
    {
    	String md5Hash = null;
    	FileInputStream in=null;
    	try {
    		in = new FileInputStream(dataA.toString());
    		int bytes = in.available();
    		byte[] buffer = new byte[bytes];
    		in.read(buffer);
    		
    		StringBuffer md5HashBuff = new StringBuffer(32);
    		byte[] b = MessageDigest.getInstance(type).digest(buffer);
    		int len = b.length;
    		for (int x=0; x<len; x++) {
    			md5HashBuff.append(String.format("%02x",b[x]));
    		}
    		
    		md5Hash=md5HashBuff.toString();
    		
        }catch (Exception e){}
        finally{try{if(in!=null) in.close();}catch(Exception e){};}
        
    	return md5Hash;
    }

    public static Long ChecksumCRC32(ValueMetaInterface metaA, Object dataA)
    {
        long checksum =0;
        FileObject file=null;
        try {
        	file=KettleVFS.getFileObject(dataA.toString());
            CheckedInputStream cis = null;
            
            // Computer CRC32 checksum
            cis = new CheckedInputStream( (FileInputStream)((LocalFile)file).getInputStream(), new CRC32());
            byte[] buf = new byte[128];
            while(cis.read(buf) >= 0) {
            }

            checksum = cis.getChecksum().getValue();

        } catch (Exception e) {
	    }finally
	    {
	    	if(file!=null) try{file.close();file=null;}catch(Exception e){};
	    }
        return checksum;
    }
    public static Long ChecksumAdler32(ValueMetaInterface metaA, Object dataA)
    {
    long checksum =0;
    FileObject file=null;
    try {
    	file=KettleVFS.getFileObject(dataA.toString());
        CheckedInputStream cis = null;
   
        // Computer Adler-32 checksum
        cis = new CheckedInputStream( (FileInputStream)((LocalFile)file).getInputStream(), new Adler32());
        
        byte[] buf = new byte[128];
        while(cis.read(buf) >= 0) {
        }
	    checksum = cis.getChecksum().getValue();

	    } catch (Exception e) {
	    	//throw new Exception(e);
	    }finally
	    {
	    	if(file!=null) try{file.close();file=null;}catch(Exception e){};
	    }
    return checksum;
}
   
    public static Object plus(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;
        
        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_STRING    : 
            return metaA.getString(dataA)+metaB.getString(dataB);
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( metaA.getNumber(dataA).doubleValue()+metaB.getNumber(dataB).doubleValue());
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( metaA.getInteger(dataA).longValue()+metaB.getInteger(dataB).longValue());
        case ValueMetaInterface.TYPE_BOOLEAN   : 
            return Boolean.valueOf( metaA.getBoolean(dataA).booleanValue() || metaB.getBoolean(dataB).booleanValue());
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return metaA.getBigNumber(dataA).add( metaB.getBigNumber(dataB));
            
        default: throw new KettleValueException("The 'plus' function only works on numeric data and Strings." );
        }
    }
    public static Object plus3(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB, ValueMetaInterface metaC, Object dataC) throws KettleValueException
    {
        if (dataA==null || dataB==null || dataC==null) return null;
        
        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_STRING    : 
            return metaA.getString(dataA)+metaB.getString(dataB)+metaC.getString(dataC);
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( metaA.getNumber(dataA).doubleValue()+metaB.getNumber(dataB).doubleValue()+metaC.getNumber(dataC).doubleValue());
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( metaA.getInteger(dataA).longValue()+metaB.getInteger(dataB).longValue()+metaC.getInteger(dataC).longValue());
        case ValueMetaInterface.TYPE_BOOLEAN   : 
            return Boolean.valueOf( metaA.getBoolean(dataA).booleanValue() || metaB.getBoolean(dataB).booleanValue() || metaB.getBoolean(dataC).booleanValue());
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return metaA.getBigNumber(dataA).add( metaB.getBigNumber(dataB).add( metaC.getBigNumber(dataC)));
            
        default: throw new KettleValueException("The 'plus' function only works on numeric data and Strings." );
        }
    }
    public static Object sum(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null && dataB==null) return null;
        if (dataA==null && dataB!=null) return metaA.convertData(metaB, dataB);
        if (dataA!=null && dataB==null) return dataA;
        
        return plus(metaA, dataA, metaB, dataB);
    }
    public static Object loadFileContentInBinary(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;
        
        FileObject file=null;
        FileInputStream fis=null;
        try {
        	file=KettleVFS.getFileObject(dataA.toString());
        	fis=(FileInputStream)((LocalFile)file).getInputStream();
        	int fileSize=(int)file.getContent().getSize();
        	byte[] content=Const.createByteArray(fileSize);
			fis.read(content, 0,fileSize);
			return content;
        } catch (Exception e) 
        {
        	throw new KettleValueException(e);	
        }
        finally
	    {
	    	try{
	    		if(fis!=null) fis.close(); fis=null;
	    		if(file!=null) file.close(); file=null;
	    	}catch(Exception e){};
	    }
    }
    public static Object minus(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;
        
        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( metaA.getNumber(dataA).doubleValue()-metaB.getNumber(dataB).doubleValue());
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( metaA.getInteger(dataA).longValue()-metaB.getInteger(dataB).longValue());
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return metaA.getBigNumber(dataA).subtract( metaB.getBigNumber(dataB));
        case ValueMetaInterface.TYPE_DATE    : 
            return new Long( metaA.getInteger(dataA).longValue()-metaB.getInteger(dataB).longValue());
            
        default: throw new KettleValueException("The 'minus' function only works on numeric data." );
        }
    }

    public static Object multiply(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;

        if ((metaB.isString() && metaA.isNumeric()) || (metaB.isNumeric() && metaA.isString()))
        {
            return multiplyString(metaA, dataA, metaB, dataB);
        }

        return multiplyNumeric(metaA, dataA, metaB, dataB);
    }

    protected static Object multiplyNumeric(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        switch(metaA.getType())
        {
            case ValueMetaInterface.TYPE_NUMBER    : 
                return multiplyDoubles(metaA.getNumber(dataA), metaB.getNumber(dataB));
            case ValueMetaInterface.TYPE_INTEGER   : 
                return multiplyLongs(metaA.getInteger(dataA), metaB.getInteger(dataB));
            case ValueMetaInterface.TYPE_BIGNUMBER : 
                return multiplyBigDecimals(metaA.getBigNumber(dataA), metaB.getBigNumber(dataB), null);

            default: throw new KettleValueException("The 'multiply' function only works on numeric data optionally multiplying strings." );
        }
    }
    public static Double multiplyDoubles(Double a, Double b)
    {
        return new Double(a.doubleValue() * b.doubleValue());
    }
    public static Long multiplyLongs(Long a, Long b)
    {
        return new Long(a.longValue() * b.longValue());
    }
    public static BigDecimal multiplyBigDecimals(BigDecimal a, BigDecimal b, MathContext mc)
    {
        if (mc == null) mc = MathContext.DECIMAL64;
        return a.multiply(b, mc);
    }

    protected static Object multiplyString(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        StringBuffer s;
        String append="";
        int n;
        if (metaB.isString())
        {
            s=new StringBuffer(metaB.getString(dataB));
            append=metaB.getString(dataB);
            n=metaA.getInteger(dataA).intValue();
        }
        else
        {
            s=new StringBuffer(metaA.getString(dataA));
            append=metaA.getString(dataA);
            n=metaB.getInteger(dataB).intValue();
        }

        if (n==0) s.setLength(0);
        else
            for (int i=1;i<n;i++) s.append(append);

        return s.toString();
    }

    public static Object divide(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;

        switch(metaA.getType())
        {
            case ValueMetaInterface.TYPE_NUMBER    : 
                return divideDoubles(metaA.getNumber(dataA), metaB.getNumber(dataB));
            case ValueMetaInterface.TYPE_INTEGER   : 
                return divideLongs(metaA.getInteger(dataA), metaB.getInteger(dataB));
            case ValueMetaInterface.TYPE_BIGNUMBER : 
                return divideBigDecimals(metaA.getBigNumber(dataA), metaB.getBigNumber(dataB), null);
            
        default: throw new KettleValueException("The 'divide' function only works on numeric data." );
        }
    }
    public static Double divideDoubles(Double a, Double b)
    {
        return new Double(a.doubleValue() / b.doubleValue());
    }
    public static Long divideLongs(Long a, Long b)
    {
        return new Long(a.longValue() / b.longValue());
    }
    public static BigDecimal divideBigDecimals(BigDecimal a, BigDecimal b, MathContext mc)
    {
        if (mc == null) mc = MathContext.DECIMAL64;
        return a.divide(b, mc);
    }
    
    public static Object sqrt(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( Math.sqrt( metaA.getNumber(dataA).doubleValue()) );
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( Math.round( Math.sqrt( metaA.getNumber(dataA).doubleValue()) ) );
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return BigDecimal.valueOf( Math.sqrt( metaA.getNumber(dataA).doubleValue()) );
            
        default: throw new KettleValueException("The 'sqrt' function only works on numeric data." );
        }
    }
    
    /**
     * 100 * A / B
     *  
     * @param metaA
     * @param dataA
     * @param metaB
     * @param dataB
     * @return
     * @throws KettleValueException
     */
    public static Object percent1(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return divideDoubles(multiplyDoubles(100.0D, metaA.getNumber(dataA)), metaB.getNumber(dataB));
        case ValueMetaInterface.TYPE_INTEGER   : 
            return divideLongs(multiplyLongs(100L, metaA.getInteger(dataA)), metaB.getInteger(dataB));
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return divideBigDecimals(multiplyBigDecimals(metaA.getBigNumber(dataA), new BigDecimal(100), null), metaB.getBigNumber(dataB), null);
            
        default: throw new KettleValueException("The 'A/B in %' function only works on numeric data" );
        }
    }
    
    /**
     * A - ( A * B / 100 )
     *  
     * @param metaA
     * @param dataA
     * @param metaB
     * @param dataB
     * @return
     * @throws KettleValueException
     */
    public static Object percent2(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( metaA.getNumber(dataA).doubleValue()  - divideDoubles(multiplyDoubles(metaA.getNumber(dataA), metaB.getNumber(dataB)), 100.0D));
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( metaA.getInteger(dataA).longValue() - divideLongs(multiplyLongs(metaA.getInteger(dataA), metaB.getInteger(dataB)), 100L));
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return metaA.getBigNumber(dataA).subtract( divideBigDecimals(metaA.getBigNumber(dataA), multiplyBigDecimals(metaB.getBigNumber(dataB), new BigDecimal(100), null), null));
        default: throw new KettleValueException("The 'A-B%' function only works on numeric data" );
        }
    }
    
    /**
     * A + ( A * B / 100 )
     *  
     * @param metaA
     * @param dataA
     * @param metaB
     * @param dataB
     * @return
     * @throws KettleValueException
     */
    public static Object percent3(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;

        switch(metaA.getType())
        {
            case ValueMetaInterface.TYPE_NUMBER    : 
                return new Double( metaA.getNumber(dataA).doubleValue() + divideDoubles(multiplyDoubles(metaA.getNumber(dataA), metaB.getNumber(dataB)), 100.0D));
            case ValueMetaInterface.TYPE_INTEGER   : 
                return new Long( metaA.getInteger(dataA).longValue() + divideLongs(multiplyLongs(metaA.getInteger(dataA), metaB.getInteger(dataB)), 100L));
            case ValueMetaInterface.TYPE_BIGNUMBER : 
                return metaA.getBigNumber(dataA).add( divideBigDecimals(metaA.getBigNumber(dataA), multiplyBigDecimals(metaB.getBigNumber(dataB), new BigDecimal(100), null), null));
        default: throw new KettleValueException("The 'A+B%' function only works on numeric data" );
        }
    }   
    
    /**
     * A + B * C
     *  
     * @param metaA
     * @param dataA
     * @param metaB
     * @param dataB
     * @return
     * @throws KettleValueException
     */
    public static Object combination1(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB, ValueMetaInterface metaC, Object dataC) throws KettleValueException
    {
        if (dataA==null || dataB==null || dataC==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( metaA.getNumber(dataA).doubleValue()  + ( metaB.getNumber(dataB).doubleValue() * metaC.getNumber(dataC).doubleValue() ));
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( metaA.getInteger(dataA).longValue() + ( metaB.getInteger(dataB).longValue() * metaC.getInteger(dataC).longValue() ));
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return metaA.getBigNumber(dataA).add(multiplyBigDecimals(metaB.getBigNumber(dataB), metaC.getBigNumber(dataC), null));
            
        default: throw new KettleValueException("The 'combination1' function only works on numeric data" );
        }
    }
    
    
    
     /**
     * SQRT( A*A + B*B )
     *  
     * @param metaA
     * @param dataA
     * @param metaB
     * @param dataB
     * @return
     * @throws KettleValueException
     */
    public static Object combination2(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( Math.sqrt( metaA.getNumber(dataA).doubleValue() * metaA.getNumber(dataA).doubleValue() + metaB.getNumber(dataB).doubleValue() * metaB.getNumber(dataB).doubleValue() ));
            
        case ValueMetaInterface.TYPE_INTEGER   : 
            return new Long( Math.round( Math.sqrt( metaA.getInteger(dataA).longValue() * metaA.getInteger(dataA).longValue() + metaB.getInteger(dataB).longValue() / metaB.getInteger(dataB).longValue() )));

        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return BigDecimal.valueOf( Math.sqrt( metaA.getNumber(dataA).doubleValue() * metaA.getNumber(dataA).doubleValue() + metaB.getNumber(dataB).doubleValue() * metaB.getNumber(dataB).doubleValue() ));
            
        default: throw new KettleValueException("The 'combination2' function only works on numeric data" );
        }
    }
    
    public static Object round(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( Math.round( metaA.getNumber(dataA).doubleValue()) );
        case ValueMetaInterface.TYPE_INTEGER   : 
            return metaA.getInteger(dataA);
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return new BigDecimal( Math.round( metaA.getNumber(dataA).doubleValue()) );
            
        default: throw new KettleValueException("The 'round' function only works on numeric data" );
        }
    }
    public static Object abs(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( Math.abs(metaA.getNumber(dataA).doubleValue()) );
        case ValueMetaInterface.TYPE_INTEGER   : 
            return metaA.getInteger(Math.abs(metaA.getNumber(dataA).longValue()) );
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            return new BigDecimal( Math.abs( metaA.getNumber(dataA).doubleValue()) );
            
        default: throw new KettleValueException("The 'abs' function only works on numeric data" );
        }
    }
    
    public static Object round(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (dataA==null || dataB==null) return null;

        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_NUMBER    : 
            return new Double( Const.round( metaA.getNumber(dataA).doubleValue(), metaB.getInteger(dataB).intValue()) );
        case ValueMetaInterface.TYPE_INTEGER   : 
            return metaA.getInteger(dataA);
        case ValueMetaInterface.TYPE_BIGNUMBER : 
            
            // Round it to the desired number of digits.
            BigDecimal number = metaA.getBigNumber(dataA);
            return number.setScale( metaB.getInteger(dataB).intValue(), BigDecimal.ROUND_HALF_EVEN); 
            
        default: throw new KettleValueException("The 'round' function only works on numeric data" );
        }
    }
    
    public static Object nvl(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        switch(metaA.getType())
        {
        case ValueMetaInterface.TYPE_STRING: 
            if (dataA==null) return metaB.getString(dataB); else return metaA.getString(dataA);

        case ValueMetaInterface.TYPE_NUMBER    :
            if (dataA==null) return metaB.getNumber(dataB); else return metaA.getNumber(dataA);
            
        case ValueMetaInterface.TYPE_INTEGER   : 
            if (dataA==null) return metaB.getInteger(dataB); else return metaA.getInteger(dataA);

        case ValueMetaInterface.TYPE_BIGNUMBER : 
            if (dataA==null) return metaB.getBigNumber(dataB); else return metaA.getBigNumber(dataA);

        case ValueMetaInterface.TYPE_DATE: 
            if (dataA==null) return metaB.getDate(dataB); else return metaA.getDate(dataA);

        case ValueMetaInterface.TYPE_BOOLEAN: 
            if (dataA==null) return metaB.getBoolean(dataB); else return metaA.getBoolean(dataA);

        case ValueMetaInterface.TYPE_BINARY: 
            if (dataA==null) return metaB.getBinary(dataB); else return metaA.getBinary(dataA);

        default: throw new KettleValueException("The 'nvl' function doesn't know how to handle data type "+metaA.getType() );
        }
    }
    public static Object removeTimeFromDate(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (metaA.isDate())
        {
            Calendar cal = Calendar.getInstance();
            Date date = metaA.getDate(dataA);
            if (date!=null)
            {
                cal.setTime(date);
                return Const.removeTimeFromDate(date);
            }else
            	return null;
        }

        throw new KettleValueException("The 'removeTimeFromDate' function only works with a date");
    }
    public static Object addTimeToDate(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB,ValueMetaInterface metaC, Object dataC) 
    throws KettleValueException
    {
    	if(dataA==null) return null;
        if (metaA.isDate())
        {
        	try{
        		if(dataC==null)
        			return Const.addTimeToDate(metaA.getDate(dataA), metaB.getString(dataB), null);
        		else
        			return Const.addTimeToDate(metaA.getDate(dataA), metaB.getString(dataB), metaC.getString(dataC));
        	}catch(Exception e) {throw new KettleValueException(e);}
        }

        throw new KettleValueException("The 'addTimeToDate' function only works with a date");
    }
    public static Object addDays(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (metaA.isDate() && metaB.isInteger())
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(metaA.getDate(dataA));
            cal.add(Calendar.DAY_OF_YEAR, metaB.getInteger(dataB).intValue());
            
            return cal.getTime();
        }

        throw new KettleValueException("The 'addDays' function only works with a date and an integer");
    }

    public static Object addMonths(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
    	if (metaA.isDate() && metaB.isInteger())
		{
    		if (dataA!=null && dataB!=null)
			{
				Calendar cal = Calendar.getInstance();
				cal.setTime(metaA.getDate(dataA));
				int year  = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH);
				int day   = cal.get(Calendar.DAY_OF_MONTH);
				
				month+=metaB.getInteger(dataB).intValue();
				
				int newyear =  year+(int)Math.floor(month/12);
				int newmonth   = month%12;
				
				cal.set(newyear, newmonth, 1);
				int newday = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				if (newday<day) cal.set(Calendar.DAY_OF_MONTH, newday);
				else            cal.set(Calendar.DAY_OF_MONTH, day);

				return( cal.getTime() );
			}
		}

    	throw new KettleValueException("The 'add_months' function only works on a dates");
	}
    /**
     * This method introduces rounding errors based on time of day and timezones. It should not be used
     * except for the case where this rounding error is desired.
     * @deprecated
     */
    public static Object DateDiffLegacy(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (metaA.isDate() && metaB.isDate())
        {
           if (dataA!=null && dataB!=null)
             {
                 // Get msec from each, and subtract.
                 long diff = metaA.getDate(dataA).getTime() - metaB.getDate(dataB).getTime();
                 return new Long(diff / (1000 * 60 * 60 * 24));  
             }else
               return null;
        }

        throw new KettleValueException("The 'DateDiff' function only works with dates");
    }
    /**
     * Returns the number of days that have elapsed between dataA and dataB.
     * 
     * @param metaA
     * @param dataA The "end date"
     * @param metaB
     * @param dataB The "start date"
     * @return Number of days
     * @throws KettleValueException
     */

    public static Object DateDiff(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (metaA.isDate() && metaB.isDate())
        {
          if (dataA!=null && dataB!=null)
          {
              Date startDate = metaA.getDate(dataA);
              Date endDate = metaB.getDate(dataB);

  			Calendar stDateCal = Calendar.getInstance();
  			Calendar endDateCal = Calendar.getInstance();
  			stDateCal.setTime(startDate);
  			endDateCal.setTime(endDate);

			long endL = endDateCal.getTimeInMillis() + endDateCal.getTimeZone().getOffset( endDateCal.getTimeInMillis() );
			long startL = stDateCal.getTimeInMillis() + stDateCal.getTimeZone().getOffset( stDateCal.getTimeInMillis() );

			return new Long(((endL - startL) / 86400000));
          } else {
            return null;
          }
        }

        throw new KettleValueException("The 'DateDiff' function only works with dates");
    }
    public static Object DateWorkingDiff(ValueMetaInterface metaA, Object dataA, ValueMetaInterface metaB, Object dataB) throws KettleValueException
    {
        if (metaA.isDate() && metaB.isDate())
        {
        	 if (dataA!=null && dataB!=null)
             {
        		Date fromDate = metaA.getDate(dataA) ;
             	Date toDate=metaB.getDate(dataB);
             	boolean singminus=false;
             	
 				if (fromDate.after(toDate)) {
 					singminus=true;
 					Date temp = fromDate;
 					fromDate = toDate;
 					toDate = temp;
 				}
 				Calendar calFrom = Calendar.getInstance();
 				calFrom.setTime(fromDate);
 				Calendar calTo = Calendar.getInstance();
 				calTo.setTime(toDate);
 				int iNoOfWorkingDays = 0;
 				do {
 					if (calFrom.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
 					&& calFrom.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
 						iNoOfWorkingDays += 1;
 					}
 					calFrom.add(Calendar.DATE, 1);
 				} while (calFrom.getTimeInMillis() < calTo.getTimeInMillis());
 				return new Long( singminus?-iNoOfWorkingDays:iNoOfWorkingDays);
             }else
            	 return null;
        }

        throw new KettleValueException("The 'DateDiff' function only works with dates");
    }
    public static Object yearOfDate(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( metaA.getDate(dataA) );
            return new Long( calendar.get(Calendar.YEAR) );
        }
        
        throw new KettleValueException("The 'yearOfDate' function only works with dates");
    }
    
    public static Object monthOfDate(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( metaA.getDate(dataA) );
            return new Long( calendar.get(Calendar.MONTH) + 1 );
        }
        
        throw new KettleValueException("The 'monthOfDate' function only works with dates");
    }
    public static Object quarterOfDate(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( metaA.getDate(dataA) );
            return new Long((calendar.get(Calendar.MONTH) + 3) / 3);
        }
        
        throw new KettleValueException("The 'monthOfDate' function only works with dates");
    }
    public static Object dayOfYear(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( metaA.getDate(dataA) );
            return new Long( calendar.get(Calendar.DAY_OF_YEAR) );
        }
        
        throw new KettleValueException("The 'dayOfYear' function only works with dates");
    }
    
    public static Object dayOfMonth(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( metaA.getDate(dataA) );
            return new Long( calendar.get(Calendar.DAY_OF_MONTH) );
        }
        
        throw new KettleValueException("The 'dayOfMonth' function only works with dates");
    }
    
    public static Object dayOfWeek(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( metaA.getDate(dataA) );
            return new Long( calendar.get(Calendar.DAY_OF_WEEK) );
        }
        
        throw new KettleValueException("The 'dayOfWeek' function only works with dates");
    }
    
    public static Object weekOfYear(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime( metaA.getDate(dataA) );
            return new Long( calendar.get(Calendar.WEEK_OF_YEAR) );
        }
        
        throw new KettleValueException("The 'weekOfYear' function only works with dates");
    }
    
    public static Object weekOfYearISO8601(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
            calendar.setMinimalDaysInFirstWeek(4);
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.setTime( metaA.getDate(dataA) );
            return new Long( calendar.get(Calendar.WEEK_OF_YEAR) );
        }
        
        throw new KettleValueException("The 'weekOfYearISO8601' function only works with dates");
    }
    
    public static Object yearOfDateISO8601(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        if (metaA.isDate())
        {
            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
            calendar.setMinimalDaysInFirstWeek(4);
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.setTime( metaA.getDate(dataA) );
            
            int week  = calendar.get(Calendar.WEEK_OF_YEAR);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            
            // fix up for the year taking into account ISO8601 weeks
            if ( week >= 52 && month == 0  ) year--;
            if ( week <= 2  && month == 11 ) year++;
            
            return new Long( year );
        }
        
        throw new KettleValueException("The 'yearOfDateISO8601' function only works with dates");
    }
    
    /**
     * Change a hexadecimal string into normal ASCII representation. E.g. if Value
     * contains string "61" afterwards it would contain value "a". If the
     * hexadecimal string is of odd length a leading zero will be used.
     *
     * Note that only the low byte of a character will be processed, this
     * is for binary transformations.
     *
     * @return Value itself
     * @throws KettleValueException  
     */    
    public static String hexToByteDecode(ValueMetaInterface meta, Object data) throws KettleValueException 
    {
        if (meta.isNull(data)) 
        {           
            return null;
        }
        
        String hexString = meta.getString(data);
        
        int len = hexString.length();
        char chArray[] = new char[(len + 1) / 2];
        boolean evenByte = true;
        int nextByte = 0;
        
        // we assume a leading 0 if the length is not even.
        if ((len % 2) == 1)
            evenByte = false;
        
        int nibble;
        int i, j;
        for (i = 0, j = 0; i < len; i++)
        {
            char    c = hexString.charAt(i);
            
            if ((c >= '0') && (c <= '9'))
                nibble = c - '0';
            else if ((c >= 'A') && (c <= 'F'))
                nibble = c - 'A' + 0x0A;
            else if ((c >= 'a') && (c <= 'f'))
                nibble = c - 'a' + 0x0A;
            else
                throw new KettleValueException("invalid hex digit '" + c + "'.");
            
            if (evenByte)
            {
                nextByte = (nibble << 4);
            }
            else
            {
                nextByte += nibble;
                chArray[j] = (char)nextByte;
                j++;
            }
            
            evenByte = ! evenByte;
        }
        return new String(chArray);
    }
    
    /**
     * Change a string into its hexadecimal representation. E.g. if Value
     * contains string "a" afterwards it would contain value "0061".
     * 
     * Note that transformations happen in groups of 4 hex characters, so
     * the value of a characters is always in the range 0-65535.
     *  
     * @return 
     * @throws KettleValueException
     */
    public static String byteToHexEncode(ValueMetaInterface metaA, Object dataA) throws KettleValueException
    {
        if (dataA==null) return null;

        final char hexDigits[] =
        { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };
        
        String hex = metaA.getString(dataA);
        
        char[] s = hex.toCharArray();
        StringBuffer hexString = new StringBuffer(2 * s.length);
        
        for (int i = 0; i < s.length; i++)
        {
            hexString.append(hexDigits[(s[i] & 0x00F0) >> 4]); // hi nibble
            hexString.append(hexDigits[s[i] & 0x000F]);        // lo nibble
        }
        
        return hexString.toString();
    }
    
    /**
     * Change a string into its hexadecimal representation. E.g. if Value
     * contains string "a" afterwards it would contain value "0061".
     * 
     * Note that transformations happen in groups of 4 hex characters, so
     * the value of a characters is always in the range 0-65535.
     *  
     * @return A string with Hex code
     * @throws KettleValueException In case of a data conversion problem.
     */
    public static String charToHexEncode(ValueMetaInterface meta, Object data) throws KettleValueException
    {
        final char hexDigits[] = { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };

        if (meta.isNull(data)) 
        {
            return null;
        }
        
        String hex = meta.getString(data);
        
        char[] s = hex.toCharArray();
        StringBuffer hexString = new StringBuffer(2 * s.length);
        
        for (int i = 0; i < s.length; i++)
        {
            hexString.append(hexDigits[(s[i] & 0xF000) >> 12]); // hex 1
            hexString.append(hexDigits[(s[i] & 0x0F00) >> 8]);  // hex 2
            hexString.append(hexDigits[(s[i] & 0x00F0) >> 4]);  // hex 3
            hexString.append(hexDigits[s[i] & 0x000F]);         // hex 4
        }
        
        return hexString.toString();
    }

    /**
     * Change a hexadecimal string into normal ASCII representation. E.g. if Value
     * contains string "61" afterwards it would contain value "a". If the
     * hexadecimal string is of a wrong length leading zeroes will be used.
     *
     * Note that transformations happen in groups of 4 hex characters, so
     * the value of a characters is always in the range 0-65535.
     *
     * @return A hex-to-char decoded String 
     * @throws KettleValueException  
     */    
    public static String hexToCharDecode(ValueMetaInterface meta, Object data) throws KettleValueException 
    {
        if (meta.isNull(data)) 
        {           
            return null;
        }
        
        String hexString = meta.getString(data);
        
        int len = hexString.length();
        char chArray[] = new char[(len + 3) / 4];
        int charNr;
        int nextChar = 0;
        
        // we assume a leading 0s if the length is not right.
        charNr = (len % 4);
        if ( charNr == 0 ) charNr = 4;
        
        int nibble;
        int i, j;
        for (i = 0, j = 0; i < len; i++)
        {
            char    c = hexString.charAt(i);
            
            if ((c >= '0') && (c <= '9'))
                nibble = c - '0';
            else if ((c >= 'A') && (c <= 'F'))
                nibble = c - 'A' + 0x0A;
            else if ((c >= 'a') && (c <= 'f'))
                nibble = c - 'a' + 0x0A;
            else
                throw new KettleValueException("invalid hex digit '" + c + "'.");

            if (charNr == 4)
            {
                nextChar = (nibble << 12);
                charNr--;
            }           
            else if (charNr == 3)
            {
                nextChar += (nibble << 8);
                charNr--;
            }
            else if (charNr == 2)
            {
                nextChar += (nibble << 4);
                charNr--;
            }
            else // charNr == 1
            {
                nextChar += nibble;
                chArray[j] = (char)nextChar;
                charNr = 4;
                j++;
            }
        }
        
        return new String(chArray);
    }

    

    /**
     * Right pad a string: adds spaces to a string until a certain length.
     * If the length is smaller then the limit specified, the String is truncated.
     * @param ret The string to pad
     * @param limit The desired length of the padded string.
     * @return The padded String.
     */
    public static final String rightPad(String ret, int limit)
    {
        if (ret == null)
            return rightPad(new StringBuffer(), limit);
        else
            return rightPad(new StringBuffer(ret), limit);
    }

    /**
     * Right pad a StringBuffer: adds spaces to a string until a certain length.
     * If the length is smaller then the limit specified, the String is truncated.
     * @param ret The StringBuffer to pad
     * @param limit The desired length of the padded string.
     * @return The padded String.
     */
    public static final String rightPad(StringBuffer ret, int limit)
    {
        int len = ret.length();
        int l;

        if (len > limit)
        {
            ret.setLength(limit);
        } else
        {
            for (l = len; l < limit; l++)
                ret.append(' ');
        }
        return ret.toString();
    }

    /**
     * Replace value occurances in a String with another value.
     * @param string The original String.
     * @param repl The text to replace
     * @param with The new text bit
     * @return The resulting string with the text pieces replaced.
     */
    public static final String replace(String string, String repl, String with)
    {
        StringBuffer str = new StringBuffer(string);
        for (int i = str.length() - 1; i >= 0; i--)
        {
            if (str.substring(i).startsWith(repl))
            {
                str.delete(i, i + repl.length());
                str.insert(i, with);
            }
        }
        return str.toString();
    }

    /**
     * Alternate faster version of string replace using a stringbuffer as input.
     * 
     * @param str The string where we want to replace in
     * @param code The code to search for
     * @param repl The replacement string for code
     */
    public static void replaceBuffer(StringBuffer str, String code, String repl)
    {
        int clength = code.length();

        int i = str.length() - clength;

        while (i >= 0)
        {
            String look = str.substring(i, i + clength);
            if (look.equalsIgnoreCase(code)) // Look for a match!
            {
                str.replace(i, i + clength, repl);
            }
            i--;
        }
    }

    /**
     * Count the number of spaces to the left of a text. (leading)
     * @param field The text to examine
     * @return The number of leading spaces found.
     */
    public static final int nrSpacesBefore(String field)
    {
        int nr = 0;
        int len = field.length();
        while (nr < len && field.charAt(nr) == ' ')
        {
            nr++;
        }
        return nr;
    }

    /**
     * Count the number of spaces to the right of a text. (trailing)
     * @param field The text to examine
     * @return The number of trailing spaces found.
     */
    public static final int nrSpacesAfter(String field)
    {
        int nr = 0;
        int len = field.length();
        while (nr < len && field.charAt(field.length() - 1 - nr) == ' ')
        {
            nr++;
        }
        return nr;
    }

    /**
     * Checks whether or not a String consists only of spaces.
     * @param str The string to check
     * @return true if the string has nothing but spaces.
     */
    public static final boolean onlySpaces(String str)
    {
        for (int i = 0; i < str.length(); i++)
            if (!isSpace(str.charAt(i)))
                return false;
        return true;
    }
    
    /**
     * Checks an xml file is well formed.
     * @param metaA The ValueMetaInterface 
     *  @param dataA The value (filename) 
     * @return true if the file is well formed.
     */
    public static boolean isXMLFileWellFormed(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return false;
    	String filename=dataA.toString();
    	FileObject file=null;
    	try {
    		file=KettleVFS.getFileObject(filename);
    		return XMLCheck.isXMLFileWellFormed(file);
    	}catch(Exception e) {
    	}finally {
    		if(file!=null) try{file.close();}catch(Exception e){};
    	}
    	return false;
    }
    /**
     * Checks an xml string is well formed.
     * @param metaA The ValueMetaInterface 
     *  @param dataA The value (filename) 
     * @return true if the file is well formed.
     */
    public static boolean isXMLWellFormed(ValueMetaInterface metaA, Object dataA)
    {
    	if(dataA==null) return false;
    	try {
    		return XMLCheck.isXMLWellFormed(new ByteArrayInputStream(metaA.getBinary(dataA)));
    	}catch(Exception e) {}
    	return false;
    }
    /**
     * Get file encoding.
     * @param metaA The ValueMetaInterface 
     * @param dataA The value (filename) 
     * @return file encoding.
     */
    public static String getFileEncoding(ValueMetaInterface metaA, Object dataA)  throws KettleValueException
    {
    	if(dataA==null) return null;
    	try {
			 return CharsetToolkit.guessEncodingName(new File(metaA.getString(dataA)));
    	}catch(Exception e) {
    		throw new KettleValueException(e);
    	}
    }
}