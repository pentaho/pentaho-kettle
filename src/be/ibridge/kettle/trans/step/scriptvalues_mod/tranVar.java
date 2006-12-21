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
 /**********************************************************************
 **                                                                   **
 ** This Script has been modified for higher performance              **
 ** and more functionality in December-2006,                          **
 ** by proconis GmbH / Germany                                        **
 **                                                                   ** 
 ** http://www.proconis.de                                            **
 ** info@proconis.de                                                  **
 **                                                                   **
 **********************************************************************/

package be.ibridge.kettle.trans.step.scriptvalues_mod;

import java.util.Date;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import be.ibridge.kettle.core.value.Value;

public class tranVar extends ScriptableObject {

	public static final long serialVersionUID = 2L;
	
	// The zero-argument constructor used by Rhino runtime to create instances
    Scriptable parent;
    Scriptable proto;

	private String tName;
	private Value tValue;
	
	// Functions for navtive Values
	public tranVar() { 
		super();
	}
	
	public tranVar(String tName) {
		super();
		this.tName = tName;
	}
	
	public tranVar(Value tValue) {
		super();
		this.tValue = tValue;
	}
	
	public tranVar(Object tValue) {
		super();
		this.tValue = (Value)tValue;
	}
	
	public String getName(){
		return tName;
	}
	public int getType(){
		return tValue.getType();
	}
	
	public void setValue(Value tValue){
		this.tValue = tValue;
	}
	
	public Value getValue(){
		return tValue;
	}
	
	public int getLength(){
		return tValue.getLength();
	}
	
     public void jsConstructor(String tName) { 
    	 this.tName = tName; 
     }
 
     // The class name is defined by the getClassName method
     public String getClassName() { 
    	 return "tranVar"; 
    }
     
     
    // Functions for Scripting behavior 
    public  Object getDefaultValue(Class hint){
    	if(this.tValue!=null && !this.tValue.isNull()){
    		switch(tValue.getType()){
    	 		case Value.VALUE_TYPE_BIGNUMBER:   
    	 			return new Double(tValue.getNumber()); 
    	 		case Value.VALUE_TYPE_BOOLEAN:
    	 			return new Boolean(tValue.getBoolean());
    	 		case Value.VALUE_TYPE_DATE:
    	 			return tValue.getDate();
    	 		case Value.VALUE_TYPE_INTEGER:
    	 			return new Double(tValue.getNumber());  
    	 		case Value.VALUE_TYPE_NONE:
    	 			return tValue.getString(); 
    	 		case Value.VALUE_TYPE_NUMBER:
    	 			return new Double(tValue.getNumber()); 
    	 		case Value.VALUE_TYPE_STRING:
    	 			return (Object)tValue.getString();
    	    	 	default: 
    	    	 		return NOT_FOUND;
    		}
    	 }else{
    		 return null;
    	 }
    	
     };
     
     
     
     public void jsSet_value(String value){
    	 try{
    		 Value vx = new Value("x");
    		 vx.setType(Value.VALUE_TYPE_STRING);
    		 vx.setValue(value);
    		 this.tValue =  vx;
    	 }catch(Exception e){
    		 System.out.println("Error setValue" + e.toString());
    	 }
     }

     public Object jsGet_value(){
    	 return this.tValue;
     }
     
     
     public Object get(String propertyName, Scriptable scriptable ) {
     	Object result = super.get( propertyName, scriptable );
         if (result != NOT_FOUND) return result;
         else  return NOT_FOUND;
     }
 	
 	 public void put( String propertyName, Scriptable scriptable, Object value ) {
 		 super.put( propertyName, scriptable, value );
 	 }  
     
     
     public String jsFunction_getString(){
    	 if(tValue.isNull()) return null;
    	 else return tValue.getString().trim();
     }
     
     public void jsFunction_setString(Object strIn){
    	 if(strIn!=null){
    		 try{
    			 this.tValue.setValue(Context.toString(strIn));
    		 }catch(Exception e){
    			 this.tValue.setNull();
    		 }
    	 }else{
    		 this.tValue.setNull();
    	 }
     }
     
     
     public Object jsFunction_getDate(){
    	 if(tValue.isNull()) return null;
    	 return new Date(this.tValue.getDate().getTime());
     }

     public void jsFunction_setDate(Object dIn){
    	 try{
    		 this.tValue.setValue((java.util.Date)Context.toType(dIn, java.util.Date.class));
    	 }catch(Exception e){
    		 this.tValue.setNull();
    	 }
     }
     
     public Double jsFunction_getNumber(){
    	 if(tValue.isNull()) return null;
    	 return new Double(this.tValue.getNumber());
     }

     public void jsFunction_setNumber(double dValue){
    	 if(!Double.isNaN(dValue)) this.tValue.setValue(dValue);
    	 else this.tValue.setNull();
     }

     
     public Object jsFunction_getInt(){
    	 if(this.tValue.isNull()) return null;
    	 else return new Double(this.tValue.getInteger());
     }
     
     public void jsFunction_setInt(double dValue){
    	 if(!Double.isNaN(dValue)) this.tValue.setValue(dValue);
    	 else this.tValue.setNull();
     }
     
     public Object jsFunction_getBool(){
    	 if(tValue.isNull()) return null;
    	 return new Boolean(this.tValue.getBoolean());
     }
     
     public void jsFunction_setBool(Object bIn){
    	 try{
    		 this.tValue.setValue(Context.toBoolean(bIn));
    	 }catch(Exception e){
    		 this.tValue.setNull();
    	 }
     }

     public int jsFunction_getLength(){
    	 if(this.getType()==Value.VALUE_TYPE_STRING){
    		 return this.tValue.getString().length();	 
    	 }else{
    		 return -1;
    	 }
     }
     
     public void jsSet_setValue(Object value){
    	 try{
    		 Value vx = new Value((Value)value);
    		 //vx.setType(Value.VALUE_TYPE_STRING);
    		 //vx.setValue(value);
    		 this.tValue =  vx;
    	 }catch(Exception e){
    		 System.out.println("Error setValue" + e.toString());
    	 }
     }

     public Object jsGet_getValue(){
    	 return this.tValue;
     }
     
}


