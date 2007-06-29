package org.pentaho.di.core;

import java.util.Date;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * This is a result file: a file as a result of the execution of a job entry, a transformation step, etc.
 * 
 * @author matt
 *
 */
public class ResultFile implements Cloneable
{
	public static final int FILE_TYPE_GENERAL   = 0;
	public static final int FILE_TYPE_LOG       = 1;
	public static final int FILE_TYPE_ERRORLINE = 2;
	public static final int FILE_TYPE_ERROR     = 3;
	public static final int FILE_TYPE_WARNING   = 4;
	
	public static final String[] fileTypeCode = { "GENERAL", "LOG", "ERRORLINE", "ERROR", "WARNING" };
	
	public static final String[] fileTypeDesc = {
                                                 Messages.getString("ResultFile.FileType.General"),
                                                 Messages.getString("ResultFile.FileType.Log"),
                                                 Messages.getString("ResultFile.FileType.ErrorLine"),
                                                 Messages.getString("ResultFile.FileType.Error"),
                                                 Messages.getString("ResultFile.FileType.Warning")
    };
    
	private int type;
	private FileObject file;
	private String originParent;
	private String origin;
	private String comment;
	private Date   timestamp;
	
	/**
	 * Construct a new result file
	 * @param type The type of file : FILE_TYPE_GENERAL, ...
	 * @param file The file to use
	 * @param originParent The transformation or job that has generated this result file
	 * @param origin The step or job entry that has generated this result file
	 */
	public ResultFile(int type, FileObject file, String originParent, String origin)
	{
		this.type = type;
		this.file = file;
		this.originParent = originParent;
		this.origin = origin;
		this.timestamp = new Date();
	}
	
	protected ResultFile clone() throws CloneNotSupportedException
	{
		return (ResultFile) super.clone();
	}

	/**
	 * @return Returns the comment.
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * @return Returns the file.
	 */
	public FileObject getFile()
	{
		return file;
	}

	/**
	 * @param file The file to set.
	 */
	public void setFile(FileObject file)
	{
		this.file = file;
	}

	/**
	 * @return Returns the origin : the step or job entry that generated this result file
	 */
	public String getOrigin()
	{
		return origin;
	}

	/**
	 * @param origin The origin to set : the step or job entry that generated this result file
	 */
	public void setOrigin(String origin)
	{
		this.origin = origin;
	}

	/**
	 * @return Returns the originParent : the transformation or job that generated this result file
	 */
	public String getOriginParent()
	{
		return originParent;
	}

	/**
	 * @param originParent The originParent to set : the transformation or job that generated this result file
	 */
	public void setOriginParent(String originParent)
	{
		this.originParent = originParent;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(int type)
	{
		this.type = type;
	}
	
	/**
	 * @return The description of this result files type.
	 */
	public String getTypeDesc()
	{
		return fileTypeDesc[type];
	}
	
	public String getTypeCode()
	{
		return fileTypeCode[type];
	}

	/**
	 * Search for the result file type, looking in both the descriptions (i18n depending) and the codes
	 * @param typeString the type string to search for
	 * @return the result file type
	 */
	public static final int getType(String typeString)
	{
		int idx = Const.indexOfString(typeString, fileTypeDesc);
		if (idx>=0) return idx;
		idx = Const.indexOfString(typeString, fileTypeCode);
		if (idx>=0) return idx;
		
		return FILE_TYPE_GENERAL;
	}
	
	/**
	 * @param fileType the result file type
	 * @return the result file type code
	 */
	public static final String getTypeCode(int fileType)
	{
		return fileTypeCode[fileType];
	}

	/**
	 * @param fileType the result file type
	 * @return the result file type description
	 */
	public static final String getTypeDesc(int fileType)
	{
		return fileTypeDesc[fileType];
	}
	
	public static final String[] getAllTypeDesc()
	{
		return fileTypeDesc;
	}

	/**
	 * @return Returns the timestamp.
	 */
	public Date getTimestamp()
	{
		return timestamp;
	}

	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}
	
	/**
	 * @return an output Row for this Result File object.
	 */
	public RowMetaAndData getRow()
	{
        RowMetaAndData row = new RowMetaAndData();

		// First the type
		row.addValue( new ValueMeta("type", ValueMetaInterface.TYPE_STRING), getTypeDesc());

		// The filename
		row.addValue( new ValueMeta("filename", ValueMetaInterface.TYPE_STRING), file.getName().getBaseName());

		// The path
		row.addValue( new ValueMeta("path", ValueMetaInterface.TYPE_STRING), file.getName().getURI());

		// The origin parent
		row.addValue( new ValueMeta("parentorigin", ValueMetaInterface.TYPE_STRING), originParent);

		// The origin
		row.addValue( new ValueMeta("origin", ValueMetaInterface.TYPE_STRING), origin);

		// The comment
		row.addValue( new ValueMeta("comment", ValueMetaInterface.TYPE_STRING), comment);

		// The timestamp
		row.addValue( new ValueMeta("timestamp", ValueMetaInterface.TYPE_DATE), timestamp);

		return row;
	}

}
