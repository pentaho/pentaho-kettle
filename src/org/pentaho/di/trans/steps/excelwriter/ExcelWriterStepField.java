package org.pentaho.di.trans.steps.excelwriter;

import org.pentaho.di.core.row.ValueMeta;

public class ExcelWriterStepField implements Cloneable
{
	private String 	name;
	private int 	type;
	private String 	format;
	private String  title;
	private boolean formula;
	private String  hyperlinkField;
	private String  commentField;
	private String  commentAuthorField;
	private String  titleStyleCell;
	private String  styleCell;
	
	public String getCommentAuthorField() {
		return commentAuthorField;
	}

	public void setCommentAuthorField(String commentAuthorField) {
		this.commentAuthorField = commentAuthorField;
	}
	
	public ExcelWriterStepField(String name, int type, String format)
	{
		this.name      		= name;
		this.type			= type;
		this.format			= format;
	}
	
	public ExcelWriterStepField()
	{
	}

	
	public int compare(Object obj)
	{
		ExcelWriterStepField field = (ExcelWriterStepField)obj;
		
		return name.compareTo(field.getName());
	}

	public boolean equal(Object obj)
	{
		ExcelWriterStepField field = (ExcelWriterStepField)obj;
		
		return name.equals(field.getName());
	}
	
	public Object clone()
	{
		try
		{
			Object retval = super.clone();
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String fieldname)
	{
		this.name = fieldname;
	}

	public int getType()
	{
		return type;
	}

	public String getTypeDesc()
	{
		return ValueMeta.getTypeDesc(type);
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public void setType(String typeDesc)
	{
	    this.type = ValueMeta.getType(typeDesc);
	}
	
	public String getFormat()
	{
		return format;
	}
	
	public void setFormat(String format)
	{
		this.format = format;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isFormula() {
		return formula;
	}

	public void setFormula(boolean formula) {
		this.formula = formula;
	}

	public String getHyperlinkField() {
		return hyperlinkField;
	}

	public void setHyperlinkField(String hyperlinkField) {
		this.hyperlinkField = hyperlinkField;
	}

	public String getCommentField() {
		return commentField;
	}

	public void setCommentField(String commentField) {
		this.commentField = commentField;
	}

	public String getTitleStyleCell() {
		return titleStyleCell;
	}

	public void setTitleStyleCell(String formatCell) {
		this.titleStyleCell = formatCell;
	}

	public String getStyleCell() {
		return styleCell;
	}

	public void setStyleCell(String styleCell) {
		this.styleCell = styleCell;
	}

	public String toString()
	{
		return name+":"+getTypeDesc();
	}	
}

