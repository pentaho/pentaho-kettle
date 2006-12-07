 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 

package be.ibridge.kettle.trans.step.streamlookup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.hash.ByteArrayHashIndex;
import be.ibridge.kettle.core.hash.LongHashIndex;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * @author Matt
 * @since 24-jan-2005
 */
public class StreamLookupData extends BaseStepData implements StepDataInterface
{
    /** used to store values in used to look up things */
	public Map look;
    
    public List list;
	
	/** nrs of keys-values in row. */
	public int    keynrs[];
	
	/** First row in lookup-set */
	public Row    firstrow;
	
	/**default string converted to values...*/
	public Value nullIf[];
	
	/** Flag to indicate that we have to read lookup values from the info step */
	public boolean readLookupValues;

    /** Stores the first row of the lookup-values to later determine if the types are the same as the input row lookup values.*/
    public Row keyTypes;

    public Row keyMeta;

    public Row valueMeta;

    public Comparator comparator;

    public ByteArrayHashIndex hashIndex;
    public LongHashIndex longIndex;
	
	public StreamLookupData()
	{
        super();
        look = new HashMap();
        hashIndex = new ByteArrayHashIndex();
        longIndex = new LongHashIndex();
        list = new ArrayList();
        comparator = new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                KeyValue k1 = (KeyValue) o1;
                KeyValue k2 = (KeyValue) o2;
                
                return k1.getKey().getRow(keyMeta).compare(k2.getKey().getRow(keyMeta));
            }
        };
	}

}
