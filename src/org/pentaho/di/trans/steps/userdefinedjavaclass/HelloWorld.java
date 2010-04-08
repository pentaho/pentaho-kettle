package org.pentaho.di.trans.steps.userdefinedjavaclass;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Sample Hello World class
 * 
 * @author matt
 */
public class HelloWorld extends TransformClassBase {

	public HelloWorld(UserDefinedJavaClass parent, UserDefinedJavaClassMeta meta, UserDefinedJavaClassData data)
					throws KettleStepException {
		super(parent, meta, data);
	}
	
	/* All this Manipulator/FieldsUpdater stuff is just some magic to let me show you an example
	 * when I have no idea what input or output fields you are going to connect to the step.
	 * It looks for all the output fields and fiddles with them in some field type appropriate way.
	 * Hopefully your code will look a lot simpler and be more useful. ;) -Daniel
	 */
	private interface Manipulator {
		public Object manipulate(Object in);
		public Object create();
	}
	public static final class FieldsUpdater {
		public static final FieldsUpdater NUMBER = new FieldsUpdater(new Manipulator() {
			double numRecords = 0;
			long startTime = System.currentTimeMillis();
			public Object manipulate(Object in) {
				if (in == null) return create();
				return (Double)in + Math.random();
			}
			public Object create() {
				return numRecords++ / (System.currentTimeMillis() - startTime);
			}
		});
		public static final FieldsUpdater STRING = new FieldsUpdater(new Manipulator() {
			public Object manipulate(Object in) {
				if (in == null) return create();
				return "Input was "+((String)in).length()+" characters.";
			}
			public Object create() { return "Hello World!"; }
		});
		public static final FieldsUpdater DATE = new FieldsUpdater(new Manipulator() {
			Calendar cal = Calendar.getInstance();
			Date myBirthday = new Date(156517500000L);
			public Object manipulate(Object in) {
				if (in == null) return create();
				cal.setTime((Date)in);
				cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
				return cal.getTime();
			}
			public Object create() {
				return myBirthday;
			}
		});
		public static final FieldsUpdater BOOLEAN = new FieldsUpdater(new Manipulator() {
			Boolean curBool = Boolean.FALSE;
			public Object manipulate(Object in) {
				if (in == null) return create();
				return (Boolean)in ? Boolean.TRUE : Boolean.FALSE;
			}
			public Object create() {
				curBool = curBool ? Boolean.FALSE : Boolean.TRUE;
				return curBool;
			}
		});
		public static final FieldsUpdater INTEGER = new FieldsUpdater(new Manipulator() {
			int numRows = 0;
			public Object manipulate(Object in) {
				if (in == null) return create();
				return ((Long)in) * -1;
			}
			public Object create() {
				return numRows++;
			}
		});
		public static final FieldsUpdater BIGNUMBER = new FieldsUpdater(new Manipulator() {
			final BigDecimal three = new BigDecimal(3);
			BigDecimal frac = new BigDecimal(Integer.MAX_VALUE, MathContext.DECIMAL128);
			public Object manipulate(Object in) {
				if (in == null) return create();
				return ((BigDecimal)in).movePointLeft(1);
			}
			public Object create() {
				frac = frac.divide(three);
				return frac;
			}
		});
		
		private final Manipulator m;
		FieldsUpdater(Manipulator m) {
			this.m = m;
		}
		public Object manipulate(Object in) { return m.manipulate(in); }
		public static FieldsUpdater valueOf(String name) throws KettleStepException {
			if ("Number".equals(name)) return NUMBER;
			else if ("String".equals(name)) return STRING;
			else if ("Date".equals(name)) return DATE;
			else if ("Boolean".equals(name)) return BOOLEAN;
			else if ("Integer".equals(name)) return INTEGER;
			else if ("BigNumber".equals(name)) return BIGNUMBER;
			else throw new KettleStepException("Unknown field data type "+name);
		}
	}
	
  private final HashMap fieldsToUpdate = new HashMap();
	private int rowsLeftForGenerateMode = -1;
	private int outputRowSize = 0;

  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
	    // First, get a row from the default input hop
		Object[] r = getRow();

	    // If the row object is null, we are done processing.
		if (r == null && !first) {
			setOutputDone();
			return false;
		}

	    // If the global "first" flag is true, perform some initialization that can only happen
	    // once we have read the first row of input data
	    if (first) {
	        first = false;

	        // Set up the list of fields that will be available after this step
	        // Normally, this is simpler, but in the HelloWorld sample, I don't know if
	        // there is an input step connected or not.
	        if (r == null) {
	        	rowsLeftForGenerateMode = 100;
	        }
			outputRowSize = data.outputRowMeta.size();
			
			// Again, an extra complicated block of code to make up for the fact that I don't know
			// how you are connecting this sample to an existing transformation.
			for (int i = 0; i < outputRowSize; i++) {
				ValueMetaInterface valueMeta = data.outputRowMeta.getValueMeta(i);
				
				FieldsUpdater mapKey = FieldsUpdater.valueOf(valueMeta.getTypeDesc());

				List fieldsForType = (List)fieldsToUpdate.get(mapKey);
				if (fieldsForType == null) {
					fieldsForType = new ArrayList();
					fieldsToUpdate.put(mapKey, fieldsForType);
				}
				fieldsForType.add(valueMeta.getName());
			}
		}

	    // It is always safest to call createOutputRow() to ensure that your output row's Object[] is large
	    // enough to handle any new fields you are creating in this step.
	    r = createOutputRow(r, outputRowSize);
	    
	    /* Now for the last piece of HellowWorld magic.  We will loop through all the fields
	     * that we will be outputing, and call the appropriate manipulate() for that type of field.
	     * In the real world, this is *much* simpler:
	     * 
	     * FieldHelper fh = get(Field.Out, "myinputfield");
	     * String oldFieldValue = fh.getString(r);
	     * fh.setValue(r, "Adding to value: "+oldFieldValue);
	     * 
	     * get(Field.Out, "mynewfield").setValue(r,"Hello World!");
	     */
	    Set entrySet = fieldsToUpdate.entrySet();
	    for (Iterator entryIter = entrySet.iterator(); entryIter.hasNext();) {
			Entry entry = (Entry)entryIter.next();
			FieldsUpdater fieldsUpdater = (FieldsUpdater)entry.getKey();
			for (Iterator listIter = ((List)entry.getValue()).iterator(); listIter.hasNext();) {
				String fieldName = (String)listIter.next();
				FieldHelper fieldHelper = get(Fields.Out, fieldName);
				fieldHelper.setValue(r, fieldsUpdater.manipulate(fieldHelper.getObject(r)));
			}
		}
	    
	    // putRow will send the row on to the default output hop.
	    putRow(data.outputRowMeta, r);

	    // This method will be continuously called until it returns false (i.e. when all rows are processed).
	    // Normally, you'd just return true if you are handling input rows or you'd return false when you are done
	    // generating new rows.  In this case, we have the below fancy test to figure out when to stop in either case.
		return (rowsLeftForGenerateMode == -1 || rowsLeftForGenerateMode-- > 0);
	}
}
