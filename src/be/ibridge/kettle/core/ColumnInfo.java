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
 
 
package be.ibridge.kettle.core;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;

/**
 * Used to define the behaviour and the content of a Table column in a TableView object.
 * 
 * @author Matt
 * @since 27-05-2003
 *
 */
public class ColumnInfo 
{
	public static final int COLUMN_TYPE_NONE    =  0;
	public static final int COLUMN_TYPE_TEXT    =  1;
	public static final int COLUMN_TYPE_CCOMBO  =  2;
	public static final int COLUMN_TYPE_BUTTON  =  3;
	public static final int COLUMN_TYPE_ICON    =  4;
	
	private int      type;
	private String   name;
	private String   defvalue;
	private String[] combovals;
	private boolean  number; 
	private String   tooltip;
	private int      allignement;
	private boolean  readonly;
	private String   button_text;
	private boolean  hide_negative;
	
	private SelectionAdapter selButton;

	// Simple String
	public ColumnInfo(String colname, int coltype, String def)
	{
		name=colname;
		type=coltype;
		defvalue=def;
		combovals=null;
		number=false;
		tooltip=null;
		allignement=SWT.LEFT;
		readonly=false;
		hide_negative=false;
	}
	
	public ColumnInfo(String colname, int coltype, String def, String[] combo)
	{
		this(colname, coltype, def);
		combovals=combo;
		number=false;
		tooltip=null;
		allignement=SWT.LEFT;
		readonly=false;
		hide_negative=false;
	}

	public ColumnInfo(String colname, int coltype, String def, boolean num)
	{
		this(colname, coltype, def);
		combovals=null;
		number=num;
		tooltip=null;
		allignement=SWT.LEFT;
		readonly=false;
		hide_negative=false;
	}

	public ColumnInfo(String colname, int coltype, String def, String[] combo, boolean ro)
	{
		this(colname, coltype, def, combo);
		readonly=ro;
	}

	public ColumnInfo(String colname, int coltype, String def, boolean num, boolean ro)
	{
		this(colname, coltype, def, num);
		readonly=ro;
	}
	
	// For buttons:
	public ColumnInfo(String colname, int coltype, String def, String button)
	{
		this(colname, coltype, def);
		button_text = button;
	}

	
	public void     setToolTip(String tip)      { tooltip=tip; }
	public void     setReadOnly(boolean ro)     { readonly=ro; }
	public void     setAllignement(int allign)  { allignement=allign; }
	public void     setComboValues(String cv[]) { combovals=cv; }
	public void     setButtonText(String bt)    { button_text=bt; }

	public String   getName()        { return name;        }
	public int      getType()        { return type;        }
	public String   getDefault()     { return defvalue;    }
	public String[] getComboValues() 
	{
		String retval[] = combovals; // Copy structure!
		return retval;   
	}
	public boolean  isNumber()       { return number;      }
	public String   getToolTip()     { return tooltip;     }       
	public int      getAllignement() { return allignement; }
	public boolean  isReadOnly()     { return readonly;    }
	public String   getButtonText()  { return button_text; }       
	
	public void setSelectionAdapter(SelectionAdapter sb)
	{
		selButton = sb;
	}
	
	public SelectionAdapter getSelectionAdapter()
	{
		return selButton;
	}
	
	public void hideNegative()
	{
		hide_negative = true;
	}
	
	public void showNegative()
	{
		hide_negative = false;
	}
	
	public boolean isNegativeHidden()
	{
		return hide_negative;
	}
}
