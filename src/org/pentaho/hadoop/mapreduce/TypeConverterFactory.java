/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.hadoop.mapreduce;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.pentaho.di.core.row.ValueMetaInterface;

public class TypeConverterFactory {
	private static TypeConverterFactory instance;
	
	protected TypeConverterFactory() {
	}
	
	public static TypeConverterFactory getInstance() {
		if(instance == null) {
			instance = new TypeConverterFactory();
		}
		
		return instance;
	}
	
	public ITypeConverter getConverter(Class<?> from, Class<?> to) {
		if(from == null || to == null) {
			return new ConvertToNull();
		} else {

		  if(IntegerToText.is(from, to)) { return new IntegerToText(); }
			if(IntegerToIntWritable.is(from, to)) { return new IntegerToIntWritable(); }
			if(LongToIntWritable.is(from, to)) { return new LongToIntWritable(); }
			if(LongToLongWritable.is(from, to)) { return new LongToLongWritable(); }
			if(IntWritableToLong.is(from, to)) { return new IntWritableToLong(); }
			if(StringToText.is(from, to)) { return new StringToText(); }
			if(StringToIntWritable.is(from, to)) { return new StringToIntWritable(); }
			if(LongWritableToText.is(from, to)) { return new LongWritableToText(); }
			if(LongWritableToLong.is(from, to)) {return new LongWritableToLong(); }
			if(LongToText.is(from, to)) {return new LongToText(); }
      if(TextToLong.is(from, to)) {return new TextToLong(); }
      if(TextToInt.is(from, to)) {return new TextToInt(); }
      if(TextToString.is(from, to)) {return new TextToString(); }
            
			
			// MUST GO LAST. THIS IS VERY GENERIC!!!
			if(ObjectToString.is(from, to)) { return new ObjectToString(); }
		}
		
		//  Do we want our own ConverterNotAvailableException?
		throw new RuntimeException("No converter available for " + from + " to " + to); 
	}
	
	private static class ConvertToNull implements ITypeConverter {
		public Object convert(ValueMetaInterface meta, Object o) throws Exception {
			return null;
		}
	}
	
	private static class IntegerToText implements ITypeConverter {
		public static boolean is(Class<?> from, Class<?> to) {
			return from.equals(Integer.class) && to.equals(Text.class) ? true : false;
		}
		
		public Object convert(ValueMetaInterface meta, Object obj) throws Exception {
			Text result = new Text();
			
			result.set(obj.toString());
			
			return result;
		}
	}
	
	private static class IntegerToIntWritable implements ITypeConverter {
		public static boolean is(Class<?> from, Class<?> to) {
			return from.equals(Integer.class) && to.equals(IntWritable.class) ? true : false;
		}
		
		public Object convert(ValueMetaInterface meta, Object obj) throws Exception {
			IntWritable result = new IntWritable();
			
			result.set((Integer)obj);
			
			return result;
		}
	}
	
	private static class LongToLongWritable implements ITypeConverter {
        public static boolean is(Class<?> from, Class<?> to) {
            return from.equals(Long.class) && to.equals(LongWritable.class) ? true : false;
        }
        
        public Object convert(ValueMetaInterface meta, Object obj) throws Exception {
            LongWritable result = new LongWritable();
            
            result.set((Long)obj);
            
            return result;
        }
    }
	
	private static class LongToIntWritable implements ITypeConverter {
		public static boolean is(Class<?> from, Class<?> to) {
			return from.equals(Long.class) && to.equals(IntWritable.class) ? true : false;
		}
		
		public Object convert(ValueMetaInterface meta, Object obj) throws Exception {
			IntWritable result = new IntWritable();
			
			result.set(((Long)obj).intValue());
			
			return result;
		}
	}
	
	private static class StringToText implements ITypeConverter {
		public static boolean is(Class<?> from, Class<?> to) {
			return from.equals(String.class) && to.equals(Text.class) ? true : false;
		}
		
		public Object convert(ValueMetaInterface meta, Object obj) throws Exception {
			Text result = new Text();
			
			result.set((String)obj);
			
			return result;
		}
	}
	
	private static class StringToIntWritable implements ITypeConverter {
		public static boolean is(Class<?> from, Class<?> to) {
			return from.equals(String.class) && to.equals(IntWritable.class) ? true : false;
		}
		
		public Object convert(ValueMetaInterface meta, Object obj) throws Exception {
			IntWritable result = new IntWritable();
			
			result.set(Integer.parseInt((String)obj));
			
			return result;
		}
	}
	
	private static class LongWritableToText implements ITypeConverter {
		public static boolean is(Class<?> from, Class<?> to) {
			return from.equals(LongWritable.class) && to.equals(Text.class) ? true : false;
		}
		
		public Object convert(ValueMetaInterface meta, Object obj) throws Exception {
			Text result = new Text();
			
			result.set(((LongWritable)obj).toString());
			
			return result;
		}
	}
	
	private static class IntWritableToLong implements ITypeConverter {
		public static boolean is(Class<?> from, Class<?> to) {
			return from.equals(IntWritable.class) && to.equals(Long.class) ? true : false;
		}
		
		public Object convert(ValueMetaInterface meta, Object obj) throws Exception {
			Long result = new Long(0);
			
			result = new Integer(((IntWritable)obj).get()).longValue();
			
			return result;
		}
	}
	
    private static class LongWritableToLong implements ITypeConverter {
        public static boolean is(Class<?> from, Class<?> to) {
            return from.equals(LongWritable.class) && to.equals(Long.class) ? true : false;
        }
        
        public Object convert(ValueMetaInterface meta, Object obj) throws Exception {
            Long result = new Long(0);
            
            result = new Long(((LongWritable)obj).get()).longValue();
            
            return result;
        }
    }	
    
    private static class LongToText implements ITypeConverter {
        public static boolean is(Class<?> from, Class<?> to) {
            return from.equals(Long.class) && to.equals(Text.class) ? true : false;
        }
        
        public Object convert(ValueMetaInterface meta, Object obj) throws Exception {
            Text result = new Text();
            
            result.set(Long.toString((Long)obj));
            
            return result;
        }
    }   
    
  private static class TextToLong implements ITypeConverter {
      public static boolean is(Class<?> from, Class<?> to) {
          return from.equals(Text.class) && to.equals(Long.class) ? true : false;
      }
      
      public Object convert(ValueMetaInterface meta, Object obj) throws Exception {
        Long result = new Long(Long.parseLong(obj.toString()));
        return result;
      }
  }    
    
  private static class TextToInt implements ITypeConverter {
      public static boolean is(Class<?> from, Class<?> to) {
          return from.equals(Text.class) && to.equals(Integer.class) ? true : false;
      }
      
      public Object convert(ValueMetaInterface meta, Object obj) throws Exception {
        Integer result = new Integer(Integer.parseInt(obj.toString()));
        return result;
      }
  }    
	
  private static class TextToString implements ITypeConverter {
      public static boolean is(Class<?> from, Class<?> to) {
          return from.equals(Text.class) && to.equals(String.class) ? true : false;
      }
      
      public Object convert(ValueMetaInterface meta, Object obj) throws Exception {
        return obj.toString();
      }
  }    
    
	private static class ObjectToString implements ITypeConverter {
		public static boolean is(Class<?> from, Class<?> to) {
			return Object.class.isAssignableFrom(from) && to.equals(String.class) ? true : false;
		}
		
		public Object convert(ValueMetaInterface meta, Object obj) throws Exception {
//			MRUtil.logMessage("ObjectToString:");
//			MRUtil.logMessage("[" + obj.getClass() + "]");
//			MRUtil.logMessage(obj.toString());
			
			return obj.toString();
		}
	}
}
