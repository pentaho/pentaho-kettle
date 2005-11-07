
package be.ibridge.kettle.chef;

import java.util.ArrayList;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.job.JobEntryResult;
import be.ibridge.kettle.job.entry.JobEntryCopy;
import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellEditorCombo;
import de.kupzog.ktable.KTableCellEditorText;
import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableModel;

/**
 * Implements the ChefLog KTable model.
 * 
 * @author Matt
 * @since  16-mrt-2005
 */
public class ChefLogTableModel implements KTableModel
{
	/**
	 * Remember the column widths
	 */
	private int[] columnWidth;

	/**
	 * The row height
	 */
	private int rowHeight;
	
	/**
	 * The information on the columns in the table
	 */
	private ColumnInfo[] columnInfo;
	
	/**
	 * The list of jobEntryResults...
	 */
	private ArrayList jobEntryResults;

	/**
	 * Drawing object will come to the rescue when drawing icons and stuff.
	 */
	private ChefGraph chefGraph;
	
	/**
	 * Create a new ChefLogTableModel
	 */
	public ChefLogTableModel(ColumnInfo[] columnInfo, ArrayList jobEntryResults, ChefGraph chefGraph)
	{
		this.columnInfo = columnInfo;
		this.jobEntryResults = jobEntryResults;
		this.chefGraph = chefGraph;
		
		columnWidth = new int[columnInfo.length];
		columnWidth[0] = 30;
		for (int i=1;i<columnInfo.length;i++) 
		{
			if (columnInfo[i].getType()==ColumnInfo.COLUMN_TYPE_ICON)
			{
				columnWidth[i]=Const.ICON_SIZE;
			}
			else
			{
				columnWidth[i]=100;
			}
		}
		
		rowHeight = Const.ICON_SIZE;
	}

	/**
	 * @see de.kupzog.ktable.KTableModel#setContentAt(int, int, java.lang.Object)
	 */
	public void setContentAt(int col, int row, Object value)
	{
	}

	/**
	 * @see de.kupzog.ktable.KTableModel#getContentAt(int, int)
	 */
	public Object getContentAt(int col, int row)
	{
		if (row==0) // header row!
		{
			// System.out.println("column #"+col+" --> "+columnInfo[col].getName());
			return columnInfo[col].getName();
		}
		
		Object content;
		
		JobEntryResult jer = (JobEntryResult) jobEntryResults.get(row-1);
		JobEntryCopy thisEntry = jer.getThisJobEntry();
		JobEntryCopy prevEntry = jer.getPrevJobEntry();
		
		Result result = jer.getResult();
		
		switch(col)
		{
		case 0:	content = result!=null?(""+result.getEntryNr()):"0"; break;
		case 1: content = chefGraph.getIcon(thisEntry); break; 
		case 2:	content = Const.NVL(jer.getJobName(),""); break;
		case 3: content = thisEntry!=null?thisEntry.toString():"-"; break;
		case 4: content = thisEntry==null || !thisEntry.isStart() ? ( result!=null?(""+result.getResult()):"???"):"-"; break;
		case 5: content = prevEntry!=null?prevEntry.toString():"-"; break;
		case 6: content = jer.getComment()!=null?jer.getComment():"-"; break;
		default: content = null;
		}
		return content;
	}


	/**
	 * @see de.kupzog.ktable.KTableModel#getCellEditor(int, int)
	 */
	public KTableCellEditor getCellEditor(int col, int row)
	{
		switch(columnInfo[col].getType())
		{
		case ColumnInfo.COLUMN_TYPE_CCOMBO: 
			KTableCellEditorCombo combo = new KTableCellEditorCombo();
			combo.setItems(columnInfo[col].getComboValues());
			return combo;
		case ColumnInfo.COLUMN_TYPE_TEXT:
			KTableCellEditorText text = new KTableCellEditorText();
			return text;
		default: break;
		}
		return null;
	}
	
	
	/**
	 * @see de.kupzog.ktable.KTableModel#getCellRenderer(int, int)
	 */
	public KTableCellRenderer getCellRenderer(int col, int row)
	{
		if (columnInfo[col].getType()==ColumnInfo.COLUMN_TYPE_ICON) 
		{
			// System.out.println("col = "+col);
			return new KTableImageRenderer();
		}
		return KTableCellRenderer.defaultRenderer;
	}
	
	/**
	 * @see de.kupzog.ktable.KTableModel#getColumnCount()
	 */
	public int getColumnCount()
	{
		return columnInfo.length;
	}
	
	/**
	 * @see de.kupzog.ktable.KTableModel#getColumnWidth(int)
	 */
	public int getColumnWidth(int col)
	{
		return columnWidth[col];
	}

	/**
	 * @see de.kupzog.ktable.KTableModel#getFirstRowHeight()
	 */
	public int getFirstRowHeight()
	{
		return 22;
	}

	/**
	 * @see de.kupzog.ktable.KTableModel#getFixedColumnCount()
	 */
	public int getFixedColumnCount()
	{
		return 1;
	}

	/**
	 * @see de.kupzog.ktable.KTableModel#getFixedRowCount()
	 */
	public int getFixedRowCount()
	{
		return 1;
	}

	/**
	 * @see de.kupzog.ktable.KTableModel#getRowCount()
	 */
	public int getRowCount()
	{
		return jobEntryResults.size()+1;
	}

	/**
	 * @see de.kupzog.ktable.KTableModel#getRowHeight()
	 */
	public int getRowHeight()
	{
		return rowHeight;
	}

	/**
	 * @see de.kupzog.ktable.KTableModel#getRowHeightMinimum()
	 */
	public int getRowHeightMinimum()
	{
		return 18;
	}

	/**
	 * @see de.kupzog.ktable.KTableModel#isColumnResizable(int)
	 */
	public boolean isColumnResizable(int col)
	{
		if (columnInfo[col].getType()==ColumnInfo.COLUMN_TYPE_ICON) return false;
		return true;
	}

	/**
	 * @see de.kupzog.ktable.KTableModel#isRowResizable()
	 */
	public boolean isRowResizable()
	{
		return true;
	}
	
	/**
	 * @see de.kupzog.ktable.KTableModel#setColumnWidth(int, int)
	 */
	public void setColumnWidth(int col, int value)
	{
		columnWidth[col] = value;
	}

	
	/**
	 * @see de.kupzog.ktable.KTableModel#setRowHeight(int)
	 */
	public void setRowHeight(int value)
	{
		rowHeight = value;
	}
	
	
	/**
	 * @param jobEntryResults The jobEntryResults to set.
	 */
	public void setJobEntryResults(ArrayList jobEntryResults)
	{
		this.jobEntryResults = jobEntryResults;
	}
	
	/**
	 * @return Returns the jobEntryResults.
	 */
	public ArrayList getJobEntryResults()
	{
		return jobEntryResults;
	}
}
