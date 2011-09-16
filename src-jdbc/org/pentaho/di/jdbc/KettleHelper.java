/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/lgpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Bayon Technologies, Inc.  All rights reserved. 
 */

package org.pentaho.di.jdbc;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

public class KettleHelper {
	private Map<String, RowMetaInterface> rowMetaMap = new Hashtable<String, RowMetaInterface>();

	public KettleHelper() {
		init();
	}

	private void init() {
		try {
			KettleEnvironment.init();
		} catch (KettleException e) {
			throw new RuntimeException("Unable to initialize Kettle", e);
		}		
	}

	public static ColInfo[] convert(RowMeta rm, String columnStr) {

		StringTokenizer st = new StringTokenizer(columnStr, ",");
		ColInfo[] r = new ColInfo[st.countTokens()];
		int i = 0;
		while (st.hasMoreTokens()) {
			ColInfo c = new ColInfo();
			String tmpStr = st.nextToken();
			int index = tmpStr.indexOf("as");
			if (index != -1) {
				String name = tmpStr.substring(index + 2);

				String realName = tmpStr.substring(0, index);
				realName = realName.trim();
				int kindex = realName.lastIndexOf(".");
				if(kindex!=-1)
				{
					realName = realName.substring(kindex+1);
				}
				name = name.trim();
				// remove the token "\""
//				name = name.substring(1, name.length() - 1);
				// System.out.println("name="+name);
				// c.setRealName(realName);
//				c.setRealName(Sanitizer.doFilter(name));
//				c.setName(Sanitizer.doFilter(name));
				c.setRealName(Sanitizer.doFilter(name));
				c.setName(Sanitizer.doFilter(name));
				ValueMetaInterface v = rm.searchValueMeta(realName);
				c.setJdbcType(translateType(v.getType()));
			} else {
				tmpStr = tmpStr.trim();
//				System.out.println("before deleting for tmpStr="+tmpStr);
				//if tmpStr likes "examplecs"."for_pentaho"."PresentsNickReceived", we just get the "PresentsNickReceived"
				//and remove the "examplecs"."for_pentaho".
				int kindex = tmpStr.lastIndexOf(".");
				if(kindex!=-1)
				{
					tmpStr = tmpStr.substring(kindex+1);
				}
//				System.out.println("after deleting for tmpStr="+tmpStr);
//				System.out.println(rm.toString());
				ValueMetaInterface v = rm.searchValueMeta(tmpStr);
				if(v!=null)
					c.setJdbcType(translateType(v.getType()));
				c.setRealName(Sanitizer.doFilter(tmpStr));
				c.setName(Sanitizer.doFilter(tmpStr));
			}

			r[i] = c;
			i++;
		}
		return r;
	}
	

	public static ColInfo[] convert(RowMeta rm) {
		ColInfo[] cols = new ColInfo[rm.size()];
		for (int i = 0; i < cols.length; i++) {
			cols[i] = new ColInfo();

			ValueMetaInterface v = rm.getValueMetaList().get(i);
			cols[i].setRealName(Sanitizer.doFilter(v.getName()));
			cols[i].setJdbcType(v.getType());

		}
		return cols;
	}

	public Map<String, String[]> visitDirectory(File f) {
		Map<String, String[]> stepsMap = new HashMap<String, String[]>();
		File[] files = f.listFiles();
		if (files != null) {

			for (int i = 0; i < files.length; i++) {
				try {
					if (files[i].getName().lastIndexOf("ktr") != -1) {

						TransMeta tm = new TransMeta(files[i].getAbsolutePath());
//						System.out.println("visitDirectory:"
//								+ java.util.Arrays.toString(tm.getStepNames()));

						String name = files[i].getName();
						name = name.substring(0, name.length() - 4);
						stepsMap.put(name, tm.getStepNames());
						StepMeta retval[] = tm.getStepsArray();
						for (int j = 0; j < retval.length; j++) {
							StepMeta s = retval[j];
							// StepMetaInterface si = s.getStepMetaInterface();
							String sname = s.getName();
							RowMetaInterface ri = tm.getStepFields(sname);
							this.rowMetaMap.put(sname, ri);
						}

					}
				} catch (Exception e) {

					e.printStackTrace();

				}
			}
		}
		return stepsMap;

	}

	public Map<String, String[]> getSteps(File f) {

		Map<String, String[]> stepsMap = new HashMap<String, String[]>();

		try {
			if (f.getName().lastIndexOf("ktr") != -1) {

				TransMeta tm = new TransMeta(f.getAbsolutePath());
//				TransMeta tm = new TransMeta("file://E:\\project\\kettlejdbc-google\\trunk\\samples\\simple.ktr");
				// System.out.println(java.util.Arrays.toString(tm.getStepNames()));
				String name = f.getName();
				name = name.substring(0, name.length() - 4);
				stepsMap.put(name, tm.getStepNames());
				StepMeta retval[] = tm.getStepsArray();
				for (int j = 0; j < retval.length; j++) {
					StepMeta s = retval[j];
					// StepMetaInterface si = s.getStepMetaInterface();
					String sname = s.getName();
					RowMetaInterface ri = tm.getStepFields(sname);
					this.rowMetaMap.put(sname, ri);
				}
			}
		} catch (Exception e) {

			e.printStackTrace();

		}

		return stepsMap;
	}

	RowMeta getRowMeta(String stepName) {
		RowMeta rm = null;
		Object obj = this.rowMetaMap.get(stepName);
		if (obj != null) {
			rm = (RowMeta) obj;
		}
		return rm;
	}
	
	public static void main(String[] args) throws Exception {
		String s = "examplecsv.Sort rows by Year.Year as Year2,     examplecsv.Sort rows by Year.PresentsRequested,  examplecsv.Sort rows by Year.PresentsNickReceived";
		StringTokenizer st = new StringTokenizer(s,",");
		System.out.println(st.countTokens());
		KettleHelper.parse();
	}
	
	private static void parse() throws Exception
	{
		String vfsFilename ="file://E:\\project\\kettlejdbc-google\\trunk\\samples\\simple.ktr";
		// Document doc=null;
    /*doc = */ XMLHandler.loadXMLFile(KettleVFS.getFileObject(vfsFilename));
	}
	
	private static int translateType (int kettleType){
		switch (kettleType) {
		case ValueMetaInterface.TYPE_BIGNUMBER: return java.sql.Types.NUMERIC;
		case ValueMetaInterface.TYPE_BINARY: return java.sql.Types.BINARY;
		case ValueMetaInterface.TYPE_BOOLEAN: return java.sql.Types.BOOLEAN;
		case ValueMetaInterface.TYPE_DATE: return java.sql.Types.TIMESTAMP;
		case ValueMetaInterface.TYPE_INTEGER: return java.sql.Types.INTEGER;
		case ValueMetaInterface.TYPE_NONE: return java.sql.Types.JAVA_OBJECT;
		case ValueMetaInterface.TYPE_NUMBER: return java.sql.Types.NUMERIC;
		case ValueMetaInterface.TYPE_SERIALIZABLE: return java.sql.Types.LONGVARCHAR;
		case ValueMetaInterface.TYPE_STRING: return java.sql.Types.VARCHAR;
		default: return java.sql.Types.OTHER;
		}
	}

}
