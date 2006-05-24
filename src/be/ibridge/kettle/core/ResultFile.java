package be.ibridge.kettle.core;

import java.io.File;
import java.util.Date;

import be.ibridge.kettle.core.value.Value;

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
	
	public static final String[] fileTypeDesc = { "General", "Log", "Error line", "Error", "Warning" };
	
	private int type;
	private File   file;
	private String originParent;
	private String origin;
	private String comment;
	private Date   timestamp;
	
	/**
	 * Construct a new result file
	 * @param type The type of file : FILE_TYPE_GENERAL, ...
	 * @param name The filename
	 * @param originParent The transformation or job that has generated this result file
	 * @param origin The step or job entry that has generated this result file
	 */
	public ResultFile(int type, File file, String originParent, String origin)
	{
		this.type = type;
		this.file = file;
		this.originParent = originParent;
		this.origin = origin;
		this.timestamp = new Date();
	}
	
	protected Object clone() throws CloneNotSupportedException
	{
		return super.clone();
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
	public File getFile()
	{
		return file;
	}

	/**
	 * @param file The file to set.
	 */
	public void setFile(File file)
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
	public Row getRow()
	{
		Row row = new Row();

		// First the type
		row.addValue( new Value("type", getTypeDesc()));

		// The filename
		row.addValue( new Value("filename", file.getName()));

		// The path
		row.addValue( new Value("path", file.getPath()));

		// The origin parent
		row.addValue( new Value("parentorigin", originParent));

		// The origin
		row.addValue( new Value("origin", origin));

		// The comment
		row.addValue( new Value("comment", comment));

		// The timestamp
		row.addValue( new Value("timestamp",   timestamp));

		return row;
	}

}
