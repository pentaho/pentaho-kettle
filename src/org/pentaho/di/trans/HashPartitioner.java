package org.pentaho.di.trans;

import java.math.BigDecimal;
import java.util.Date;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;

public class HashPartitioner extends BasePartitioner {

	public HashPartitioner( StepPartitioningMeta meta ) {
		super( meta );
	}
	
		//this method is required by the hashword method that calculates the hash
		//variable c contains the input myKey
		//when mixing is complete, c also contains the hash result
		//variables 'a' and 'b' contain initialized random bits
	  	private int mix(int a, int b, int c)
		{
			
	  		a-=b; a=a^(c >>> 16); a-=c;
	  		a-=b; a=a^(c >>> 4);  a-=c;
	  		
	  		b-=c;  b=b^(a << 4); b-=a;
	  		b-=c;  b=b^(a << 8); b-=a;
	  		b-=c;  b=b^(a << 19);b-=a;
	  		
	  	
	  		c-=a;  c=c^(b >>> 6); c-=b;
			c-=a;  c=c^(b >>> 8);c-=b;
		
	  		return c;
		}
		//this method is required by the hashword method that calculates the hash
		private int finalMix(int a, int b, int c)
		{
			c=c^b; c=c-(b >>> 14);
			a=a^c; a=a-(c >>> 11);
			b=b^a; b=b-(a >>> 25);
			c=c^b; c=c-(b >>> 16);
			a=a^c; a=a-(c >>> 4); 
	  		b=b^a; b=b-(a >>> 14); 
	  		c=c^b; c=c-(b >>> 24); 
	  	
	  		return c;
		}
		
		//calculates the hash
		private int hashword(int[] myKey, int length, int initVal)
		{
			int a, b, c;
		  /* Set up the internal state */
	  		a = b = c = 0xdeadbeef + (((int)length)<<2) + initVal;

	 	 /*------------------------------------------------- handle most of the myKey*/
	   		int i =0;
	  		while (length > 3)
	  		{
		   		a += myKey[i];
	    		b += myKey[i+1];
	    		c += myKey[i+2];
	    		c=mix(a,b,c);
	    		length -= 3;
	    		i=i+3;
	  		}

	  		/*------------------------------------------- handle the last 3 int's */
	  		switch(length)                     /* all the case statements fall through */
	  		{ 
	  			case 3 : c+=myKey[i+2];
	  			case 2 : b+=myKey[i+1];
	  			case 1 : a+=myKey[i];
	    		c=finalMix(a,b,c);
	  			case 0:     /* case 0: nothing left to add */
	    		break;
	  		}
	  		/*------------------------------------------------------ report the result */
	  		return c;	
		}
		//Convert String to an int array 
		
		private int[] StringToInt(String key1,boolean caseInsensitive)
		{
			int[] sum = new int[key1.length()];
	 		if(key1==null) throw new IllegalArgumentException();
	 		
			if(caseInsensitive) //Y = true not case sensitive
			{
				key1=key1.toUpperCase();		
			}
			char[] tokenString=key1.toCharArray();
	 		for(int i=0;i<key1.length();i++)
	 		{
		 		sum[i]=((int)tokenString[i]);
	 		}
	 		return sum;
		}
		 private int[] byteArrayToIntArray(byte[] b) 
		 {
		        int[] value = new int[4];
		        int j=0;
		        for (int i = 0; i < 4; i++) {
		            int shift = (4 - 1 - i) * 8;
		            value[j++] = (b[i] & 0x000000FF) << shift;
		        }
		        return value;
		 }
		 
		public int getPartition(RowMetaInterface rowMeta, Object[] r ) throws KettleException
		{
			init(rowMeta);
			
            ValueMetaInterface vmeta = rowMeta.getValueMeta( partitionColumnIndex );
            
		    int targetLocation=0;
		    int hashOut =0;
		
		    if (r==null)  // no more input to be expected...
			{
				return -1;
			}
	        
		    int length = 1; 
		    // TODO only support a single hashing column at the moment
		    Object value = r[partitionColumnIndex];
	        int[] key = null;
	    	for(int i=0;i<length;i++)
	        { 
	            //get the type of the field, convert to int
	        	int type=vmeta.getType();
	        	boolean caseInsensitive = true;
	            switch(type)
	            { 		//convert to int[] based on type
	            
	            	case ValueMeta.TYPE_BIGNUMBER: 	BigDecimal bg =vmeta.getBigNumber(value);
	            									key = new int[1];
	            									key[0]=bg.intValue();
	            									break;
	            	//Does this correspond to raw type? May have to be removed
	            	case ValueMeta.TYPE_BINARY:		byte[] bArray = vmeta.getBinary(value);
	            									key = byteArrayToIntArray(bArray);
	            									//key = StringToInt(new String(bArray)); another way to convert to int[]
	            									break;
	            								
	            	case ValueMeta.TYPE_BOOLEAN:	Boolean b = vmeta.getBoolean(value);
	            									key = StringToInt(b.toString(),caseInsensitive);
	            									break;
	            	case ValueMeta.TYPE_DATE:		Date d =vmeta.getDate(value);
	            									key = StringToInt(d.toString(),caseInsensitive);
	            									break;
	            	case ValueMeta.TYPE_INTEGER:	Long l = vmeta.getInteger(value);
	            									key = new int[1];
	            									key[0]=l.intValue();
	            									break;
	            	case ValueMeta.TYPE_NONE:		throw new IllegalArgumentException("Cannot hash this field "+vmeta.getName());
	            	case ValueMeta.TYPE_NUMBER:		Double dd = vmeta.getNumber(value);
	            									key = new int[1];
	            									key[0]=dd.intValue();
	            									break;
	            	case ValueMeta.TYPE_SERIALIZABLE:
	            	case ValueMeta.TYPE_STRING: 	key=StringToInt(vmeta.getString(value),caseInsensitive);
													break;
	            					            
	            }
	            hashOut+=Math.abs(hashword(key,key.length,7)); //7 has been chosen randomly. Any prime number > 3 can be substituted
	           
	             
	        }//end for
	    	
       		targetLocation = hashOut % nrPartitions;
				
			return targetLocation;
		}

		public int getNrPartitions() {
			return nrPartitions;
		}

		public void setNrPartitions(int nrPartitions) {
			this.nrPartitions = nrPartitions;
		}

	
}
