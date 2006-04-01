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
 

package be.ibridge.kettle.core;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeItem;

import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.job.entry.JobEntryCopy;
import be.ibridge.kettle.job.entry.JobEntryInterface;



/**
 * This class is used to define a number of default values for various settings throughout Kettle.
 * It also contains a number of static final methods to make your life easier.
 *
 * @author Matt 
 * @since 07-05-2003
 *
 */
public class Const 
{
	/**
	 *  Version number
	 */
	public static final String  VERSION = "2.3.0";
	
	/**
	 * Sleep time waiting when buffer is empty
	 */
	public static final int  SLEEP_EMPTY_NANOS   = 1;

	/**
	 * Sleep time waiting when buffer is full
	 */
	public static final int  SLEEP_FULL_NANOS    = 1; // luxury problem!
	
	/**
	 * print update every ... lines
	 */
	public static final int  ROWS_UPDATE    = 5000;
	
	/**
	 * Size of rowset: bigger = faster for large amounts of data
	 */
	public static final int  ROWS_IN_ROWSET = 350; 
	
	/**
	 * Fetch size in rows when querying a database
	 */
	public static final int  FETCH_SIZE = 100;
	
	/**
	 * Sort size: how many rows do we sort in memory at once?
	 */
	public static final int SORT_SIZE = 5000;
	
	/**
	 * What's the file systems file separator on this operating system?
	 */
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");

	/**
	 * CR: operating systems specific Cariage Return
	 */
	public static final String CR = System.getProperty("line.separator");
	
	/**
	 * The Java runtime version
	 */
	 public static final String JAVA_VERSION = System.getProperty("java.vm.version");

	/**
	 * The images directory
	 */
	public static final String IMAGE_DIRECTORY = "/be/ibridge/kettle/images/"; //+FILE_SEPARATOR;
	
	/**
	 *  Public directory containing external steps plugins
	 */
	public static final String PLUGIN_STEPS_DIRECTORY_PUBLIC = "plugins"+FILE_SEPARATOR+"steps";

	/**
	 *  Private directory containing external steps plugins
	 */
	public static final String PLUGIN_STEPS_DIRECTORY_PRIVATE = getKettleDirectory()+FILE_SEPARATOR+"plugins"+FILE_SEPARATOR+"steps"; 

    /**
     *  Public directory containing external job entries plugins
     */
    public static final String PLUGIN_JOBENTRIES_DIRECTORY_PUBLIC = "plugins"+FILE_SEPARATOR+"jobentries"; 

    /**
     *  Private directory containing external job entries plugins
     */
    public static final String PLUGIN_JOBENTRIES_DIRECTORY_PRIVATE = getKettleDirectory()+FILE_SEPARATOR+"plugins"+FILE_SEPARATOR+"jobentries";

	/**
	 * Default minimum date range...
	 */
	public static final Date MIN_DATE = new Date(  -2208992400000L);  // 1900/01/01 00:00:00.000
	
	/**
	 * Default maximum date range...
	 */
	public static final Date MAX_DATE = new Date(   7258114799468L);  // 2199/12/31 23:59:59.999

	/**
	 * The default minimum year in a dimension date range
	 */
	public static final int  MIN_YEAR = 1900;

	/**
	 * The default maximum year in a dimension date range
	 */
	public static final int  MAX_YEAR = 2199;
			
	/**
	 * Specifies the number of pixels to the right we have to go in dialog boxes.
	 */
	public static final int RIGHT  = 400;

	/**
	 * Specifies the length (width) of fields in a number of pixels in dialog boxes.
	 */
	public static final int LENGTH = 350;
	
	/**
	 * The margin between the different dialog components & widgets
	 */
	public static final int MARGIN = 5;
	
	/**
	 * The default percentage of the width of screen where we consider the middle of a dialog.
	 */
	public static final int MIDDLE_PCT = 35;
	
	/**
	 * The default width of an arrow in the Graphical Views
	 */
	public static final int ARROW_WIDTH = 1;
	
	/**
	 * The horizontal and vertical margin of a dialog box.
	 */
	public static final int FORM_MARGIN = 3;
	
	/**
	 * The default shadow size on the graphical view.
	 */
	public static final int SHADOW_SIZE = 4;

	/**
	 *  The size of relationship symbols
	 */
	public static final int SYMBOLSIZE = 10;
	
	/**
	 * Max nr. of files to remember
	 */
	public static final int MAX_FILE_HIST = 9;

	/**
	 * The default locale for the kettle environment (system defined)
	 */
	public static final Locale DEFAULT_LOCALE = Locale.getDefault(); // new Locale("nl", "BE");
	
	/**
	 * The default decimal separator . or ,
	 */
	public static final char   DEFAULT_DECIMAL_SEPARATOR  = (new DecimalFormatSymbols(DEFAULT_LOCALE)).getDecimalSeparator();
	
	/**
	 * The default grouping separator , or .
	 */
	public static final char   DEFAULT_GROUPING_SEPARATOR = (new DecimalFormatSymbols(DEFAULT_LOCALE)).getGroupingSeparator();
	
	/**
	 * The default currency symbol
	 */
	public static final String DEFAULT_CURRENCY_SYMBOL    = (new DecimalFormatSymbols(DEFAULT_LOCALE)).getCurrencySymbol();
	
	/**
	 * The default number format
	 */
	public static final String DEFAULT_NUMBER_FORMAT      = ((DecimalFormat)(NumberFormat.getInstance())).toPattern();
	
	/**
	 * Default string representing Null String values (empty)
	 */
	public static final String NULL_STRING = "";

	/**
	 * Default string representing Null Number values (empty)
	 */
	public static final String NULL_NUMBER = "";

	/**
	 * Default string representing Null Date values (empty)
	 */
	public static final String NULL_DATE   = "";

    /**
     * Default string representing Null BigNumber values (empty)
     */
    public static final String NULL_BIGNUMBER   = "";

	/**
	 * Default string representing Null Boolean values (empty)
	 */
	public static final String NULL_BOOLEAN= "";

	/**
	 * Default string representing Null Integer values (empty)
	 */
	public static final String NULL_INTEGER= "";

	/**
	 * Default string representing Null Undefined values (empty)
	 */
	public static final String NULL_NONE   = "";
	
	/**
	 * Default font name for the fixed width font
	 */
	public static final String FONT_FIXED_NAME = "Courier";

	/**
	 * Default font size for the fixed width font
	 */
	public static final int    FONT_FIXED_SIZE = 9;

	/**
	 * Default font type for the fixed width font
	 */
	public static final int    FONT_FIXED_TYPE = SWT.NORMAL;
	
	/**
	 * Default icon size
	 */
	public static final int    ICON_SIZE  = 32;

	/**
	 * Default line width for arrows & around icons
	 */
	public static final int    LINE_WIDTH =  1;
	
	/**
	 * Default grid size to which the graphical views snap.
	 */
	public static final int    GRID_SIZE = 20;
	
	/**
	 * The minimal size of a note on a graphical view (width & height)
	 */
	public static final int    NOTE_MIN_SIZE = 20;
	
	/**
	 * The margin between the text of a note and its border.
	 */
	public static final int    NOTE_MARGIN   =  5;

	/**
	 * The default red-component of the background color
	 */
	public static final int    COLOR_BACKGROUND_RED   = 255;

	/**
	 * The default green-component of the background color
	 */
	public static final int    COLOR_BACKGROUND_GREEN = 255;

	/**
	 * The default blue-component of the background color
	 */
	public static final int    COLOR_BACKGROUND_BLUE  = 255;


	/**
	 * The default red-component of the graph background color
	 */
	public static final int    COLOR_GRAPH_RED   = 255;

	/**
	 * The default green-component of the graph background color
	 */
	public static final int    COLOR_GRAPH_GREEN = 255;

	/**
	 * The default blue-component of the graph background color
	 */
	public static final int    COLOR_GRAPH_BLUE  = 255;
	

	/**
	 * The default red-component of the tab selected color
	 */
	public static final int    COLOR_TAB_RED   = 200;

	/**
	 * The default green-component of the tab selected color
	 */
	public static final int    COLOR_TAB_GREEN = 200;

	/**
	 * The default blue-component of the tab selected color
	 */
	public static final int    COLOR_TAB_BLUE  = 255;

	/**
	 * The default undo level for Kettle
	 */	
	public static final int    MAX_UNDO = 100;

	/**
	 * Path to the users home directory
	 */
	public static final String USER_HOME_DIRECTORY = System.getProperty("user.home");
	
	/**
	 * The base name of the Chef logfile
	 */
	public static final String CHEF_LOG_FILE  = "chef";
	

	/**
	 * The base name of the Spoon logfile
	 */
	public static final String SPOON_LOG_FILE = "spoon";

	/**
	 * The base name of the Menu logfile
	 */
	public static final String MENU_LOG_FILE = "menu";


	/**
	 * A number of tips that are shown when the application first starts.
	 */
	public static final String tips[] = 
		{
            "You can create a new step, simply by dragging the step onto the graph view."+Const.CR+
            "You can find steps and plugins to use in the tree on the left side of the screen under 'Base step types'",
			
            "You can define a new hop between 2 steps by using the middle or scrollwheel button."+Const.CR+
            "Middle-click on the first step and drag onto the second."+Const.CR+
            "You can also: "+Const.CR+
            " - use SHIFT+Click and drag from one step to another"+Const.CR+
            " - Select 2 steps and right click on one of them, then select 'new hop'"+Const.CR+
            " - Drag 'Hops' onto the canvas",
            
			"Inserting a step between 2 steps that are already connected with a hop:"+Const.CR+
            "simply move the step over the arrow until the arrow becomes drawn in bold."+Const.CR+
            "Release the mouse-button and you'll be asked if you want to insert the step.",
            
			"Edit the step description by double clicking on a step using the middle-button",
            
			"Explore a database by clicking right on a connection and selecting 'explore database'",
            
            "Preview the rows that are passing throuqh a step (output) by selecting the step and by pressing F10 (preview)",
            
            "Check the settings of steps by first selecting one or more of them."+Const.CR+
            "Then by using the right-click option 'Check selected steps'",
            
            "Click right on the first column in any dialog table (grid) for a list of all the options."
		}
		;

	/**
	 * An array of date conversion formats
	 */
	public static final String dateFormats[] = new String[] 
		{
			"yyyy/MM/dd HH:mm:ss.SSS", 
			"yyyy/MM/dd HH:mm:ss",
			"yyyyMMddHHmmss",
			"dd/MM/yyyy",
			"dd-MM-yyyy",
			"yyyy/MM/dd",
			"yyyy-MM-dd",
			"yyyyMMdd",
			"ddMMyyyy",
			"d-M-yyyy",
			"d/M/yyyy",
			"d-M-yy",
			"d/M/yy",
		}
		;

	/**
	 * An array of number conversion formats
	 */
	public static final String numberFormats[] = new String[] 
	{
		DEFAULT_NUMBER_FORMAT,
		"0.00",
		"0000000000000",
		"#.#",
		"#",
		"###,###,###.#######", 
		"###############.###############",
		"#####.###############%",
	}
	;

    /**
     * Default we store our information in Unicode UTF-8 character set.
     */
    public static final String XML_ENCODING = "UTF-8";

    /** The extention of a Kettle transformation XML file */
    public static final String STRING_TRANS_DEFAULT_EXT    = ".ktr";
    
    /** The possible extentions a transformation XML file can have. */
    public static final String STRING_TRANS_FILTER_EXT[]   = new String[] { "*.ktr;*.xml", "*.xml", "*.*" };

    /** The discriptions of the possible extentions a transformation XML file can have. */
    public static final String STRING_TRANS_FILTER_NAMES[] = new String[] { "Kettle Transformations", "XML Files", "All files" };


    /** The extention of a Kettle job XML file */
    public static final String STRING_JOB_DEFAULT_EXT    = ".kjb";
    
    /** The possible extentions a job XML file can have. */
    public static final String STRING_JOB_FILTER_EXT[]   = new String[] { "*.kjb;*.xml", "*.xml", "*.*" };

    /** The discriptions of the possible extentions a job XML file can have. */
    public static final String STRING_JOB_FILTER_NAMES[] = new String[] { "Kettle Jobs", "XML Files", "All files" };






    /** rounds double f to any number of places after decimal point
     *  Does arithmetic using BigDecimal class to avoid integer overflow while rounding
     *  TODO: make the rounding itself optional in the Props for performance reasons.
     *  
     * @param f The value to round
     * @param places The number of decimal places
     * @return The rounded floating point value
     */
    
    public static final double round(double f, int places) 
    { 
        java.math.BigDecimal bdtemp = new java.math.BigDecimal(f); 
        bdtemp = bdtemp.setScale(places, java.math.BigDecimal.ROUND_HALF_EVEN); 
        return bdtemp.doubleValue(); 
    } 
    
    /* OLD code: caused a lot of problems with very small and very large numbers.
     * It's a miracle it worked at all.
     * Go ahead, have a laugh...
    public static final float round(double f, int places)
    {
        float temp = (float) (f * (Math.pow(10, places)));

        temp = (Math.round(temp));

        temp = temp / (int) (Math.pow(10, places));

        return temp;

    }
    */

	/**
	 * Convert a String into an integer.  If the conversion fails, assign a default value.
	 * @param str The String to convert to an integer 
	 * @param def The default value
	 * @return The converted value or the default.
	 */
	public static final int toInt(String str, int def)
	{
		int retval;
		try { retval = Integer.parseInt(str); } catch(Exception e) { retval=def; };
		return retval;
	}

	/**
	 * Convert a String into a long integer.  If the conversion fails, assign a default value.
	 * @param str The String to convert to a long integer
	 * @param def The default value
	 * @return The converted value or the default.
	 */
	public static final long toLong(String str, long def)
	{
		long retval;
		try { retval = Long.parseLong(str); } catch(Exception e) { retval=def; };
		return retval;
	}

	/**
	 * Convert a String into a double.  If the conversion fails, assign a default value.
	 * @param str The String to convert to a double
	 * @param def The default value
	 * @return The converted value or the default.
	 */
	public static final double toDouble(String str, double def)
	{
		double retval;
		try { retval = Double.parseDouble(str); } catch(Exception e) { retval=def; };
		return retval;
	}
	
	/**
	 * Convert a String into a date.  
	 * The date format is <code>yyyy/MM/dd HH:mm:ss.SSS</code>.  
	 * If the conversion fails, assign a default value.
	 * @param str The String to convert into a Date
	 * @param def The default value
	 * @return The converted value or the default.
	 */
	public static final Date toDate(String str, Date def)
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		try
		{
			return df.parse(str);
		}
		catch(ParseException e)
		{
			return def;
		}
	}

	/**
	 * Right trim: remove spaces to the right of a string
	 * @param str The string to right trim
	 * @return The trimmed string.
	 */
	public static final String rtrim(String str)
	{
		int max=str.length();
		while(max>0 && isSpace(str.charAt(max-1))) max--;
		
		return str.substring(0,max);
	}
	
	/**
	 * Determines whether or not a character is considered a space.
	 * A character is considered a space in Kettle if it is a space, a tab, a newline or a cariage return.
	 * @param c The character to verify if it is a space.
	 * @return true if the character is a space. false otherwise. 
	 */
	public static final boolean isSpace(char c)
	{
		return c==' ' || c=='\t' || c=='\r' || c=='\n';
	}

	/**
	 * Left trim: remove spaces to the left of a String.
	 * @param str The String to left trim
	 * @return The left trimmed String
	 */
	public static final String ltrim(String str)
	{
		int from=0;
		while(from<str.length() && isSpace(str.charAt(from)) ) from++;
		
		return str.substring(from);
	}

	/**
	 * Trims a string: removes the leading and trailing spaces of a String.
	 * @param str The string to trim
	 * @return The trimmed string.
	 */
	public static final String trim(String str)
	{
		int max=str.length()-1;
		int min=0;

		while (min<=max && isSpace(str.charAt(min))) min++;
		while(max>=0 && isSpace(str.charAt(max))) max--;
		
		if (max<min) return "";
		
		return str.substring(min,max+1);
	}
	
	/**
	 * Right pad a string: adds spaces to a string until a certain length.
	 * If the length is smaller then the limit specified, the String is truncated.
	 * @param ret The string to pad
	 * @param limit The desired length of the padded string.
	 * @return The padded String.
	 */
	public static final String rightPad(String ret, int limit)
	{
		if (ret==null) return rightPad(new StringBuffer(), limit);
		else           return rightPad(new StringBuffer(ret), limit);
	}
	
	/**
	 * Right pad a StringBuffer: adds spaces to a string until a certain length.
	 * If the length is smaller then the limit specified, the String is truncated.
	 * @param ret The StringBuffer to pad
	 * @param limit The desired length of the padded string.
	 * @return The padded String.
	 */
	public static final String rightPad(StringBuffer ret, int limit)
	{
		int len=ret.length();
		int l;
		
		if (len>limit)
		{
		  ret.setLength(limit);
		}
		else
		{
		  for (l=len;l<limit;l++) ret.append(' ');
		}
		return ret.toString();
	}
	
	/**
	 * Replace values in a String with another.
	 * @param string The original String.
	 * @param repl The text to replace
	 * @param with The new text bit
	 * @return The resulting string with the text pieces replaced.
	 */
	public static final String replace(String string, String repl, String with)
	{
		StringBuffer str=new StringBuffer(string);
		for (int i=str.length()-1;i>=0;i--)
		{
			if (str.substring(i).startsWith(repl))
			{
				str.delete(i, i+repl.length());
				str.insert(i, with);
			}
		}
		return str.toString();
	}

	/**
	 * Alternate faster version of string replace using a stringbuffer as input.
	 * 
	 * @param str The string where we want to replace in
	 * @param code The code to search for
	 * @param repl The replacement string for code
	 */
	public static void repl(StringBuffer str, String code, String repl)
	{
		int clength = code.length();
		
		int i=str.length()-clength;
	
		while (i>=0)
		{
			String look=str.substring(i, i+clength);
			if (look.equalsIgnoreCase(code)) // Look for a match!
			{
				str.replace(i, i+clength, repl);
			}
			i--;
		}
	}

	/**
	 * Count the number of spaces to the left of a text. (leading)
	 * @param field The text to examine
	 * @return The number of leading spaces found.
	 */
	public static final int nrSpacesBefore(String field)
	{	
		int nr = 0;
		int len = field.length();
		while (nr<len && field.charAt(nr)==' ')
		{
			nr++;
		}
		return nr;
	}

	/**
	 * Count the number of spaces to the right of a text. (trailing)
	 * @param field The text to examine
	 * @return The number of trailing spaces found.
	 */
	public static final int nrSpacesAfter(String field)
	{	
		int nr = 0;
		int len = field.length();
		while (nr<len && field.charAt(field.length()-1-nr)==' ')
		{
			nr++;
		}
		return nr;
	}
	
	/**
	 * Checks whether or not a String consists only of spaces.
	 * @param str The string to check
	 * @return true if the string has nothing but spaces.
	 */
	public static final boolean onlySpaces(String str)
	{
		for (int i=0;i<str.length();i++) if (!isSpace(str.charAt(i))) return false;
		return true;
	}

	
	/**
	 * determine the OS name
	 * @return The name of the OS
	 */
	public static final String getOS()
	{
		return System.getProperty("os.name");
	}

    /** 
     * @return True if the OS is a Windows diravate. 
     */
    public static final boolean isWindows()
    {
        return getOS().startsWith("Windows");
    }

	/**
	 * Determine the hostname of the machine Kettle is running on
	 * @return The hostname
	 */
	public static final String getHostname()
	{	
		try
		{
			Enumeration en = NetworkInterface.getNetworkInterfaces();
			while (en.hasMoreElements())
			{
				NetworkInterface nwi = (NetworkInterface)en.nextElement();
				//System.out.println("nwi : "+nwi.getName()+" ("+nwi.toString()+")");
				Enumeration ip = nwi.getInetAddresses();
				
				while (ip.hasMoreElements())
				{
					InetAddress in = (InetAddress)ip.nextElement();
					//System.out.println("  ip address bound : "+in.getHostAddress());
					//System.out.println("  hostname         : "+in.getHostName());
					//System.out.println("  Cann.hostname    : "+in.getCanonicalHostName());
					//System.out.println("  ip string        : "+in.toString());
					if (!in.getHostName().equalsIgnoreCase("localhost"))
					{
						return in.getHostName();
					}
				}
			}
		}
		catch(SocketException e)
		{
				
		}
		
		return "localhost";
	}

	/**
	 * Determins the IP address of the machine Kettle is running on.
	 * @return The IP address
	 */
	public static final String getIPAddress()
	{
		try
		{
			Enumeration enumInterfaces = NetworkInterface.getNetworkInterfaces();
			while (enumInterfaces.hasMoreElements())
			{
				NetworkInterface nwi = (NetworkInterface)enumInterfaces.nextElement();
				Enumeration ip = nwi.getInetAddresses();
				while (ip.hasMoreElements())
				{
					InetAddress in = (InetAddress)ip.nextElement();
					if (!in.isLoopbackAddress() && in.toString().indexOf(":")<0)
					{
						return in.getHostAddress();
					}
				}
			}
		}
		catch(SocketException e)
		{
				
		}
		
		return "127.0.0.1";
	}
	
	/**
	 * Tries to determine the MAC address of the machine Kettle is running on.
	 * @return The MAC address.
	 */
	public static final String getMACAddress()
	{
		String ip = getIPAddress();
		String mac = "none";
		String os = getOS();
		String s = "";
		
		//System.out.println("os = "+os+", ip="+ip);
		
		if (os.equalsIgnoreCase("Windows NT") ||
		    os.equalsIgnoreCase("Windows 2000") ||
		    os.equalsIgnoreCase("Windows XP") ||
		    os.equalsIgnoreCase("Windows 95") ||
		    os.equalsIgnoreCase("Windows 98") ||
		    os.equalsIgnoreCase("Windows Me") ||
		    os.startsWith("Windows")
		   )
		{
			try
			{
                // System.out.println("EXEC> nbtstat -a "+ip);
                
				Process p = Runtime.getRuntime().exec("nbtstat -a "+ip);

				// read the standard output of the command
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
				while (!procDone(p))
				{
					while((s=stdInput.readLine()) !=null)
					{
                       // System.out.println("NBTSTAT> "+s);
					   if (s.indexOf("MAC")>=0)
					   {
							int idx = s.indexOf("=");
							mac = s.substring(idx+2);
					   }
					}
				}
				stdInput.close();
			}
			catch(Exception e)
			{
				
			}
		}
		else
		if (os.equalsIgnoreCase("Linux"))
		{
			try
			{
				Process p = Runtime.getRuntime().exec("/sbin/ifconfig -a");

				// read the standard output of the command
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
				while (!procDone(p))
				{
					while((s=stdInput.readLine()) !=null)
					{
					   int idx = s.indexOf("HWaddr");
					   if (idx>=0)
					   {
							mac = s.substring(idx+7);
					   }
					}
				}
				stdInput.close();
			}
			catch(Exception e)
			{
				
			}	
		}
		else
		if (os.equalsIgnoreCase("Solaris"))
		{	
			try
			{
				Process p = Runtime.getRuntime().exec("/usr/sbin/ifconfig -a");

				// read the standard output of the command
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
				while (!procDone(p))
				{
					while((s=stdInput.readLine()) !=null)
					{
					   int idx = s.indexOf("ether");
					   if (idx>=0)
					   {
							mac = s.substring(idx+6);
					   }
					}
				}
				stdInput.close();
			}
			catch(Exception e)
			{
				
			}
		}
		else
		if (os.equalsIgnoreCase("HP-UX"))
		{
			try
			{
				Process p = Runtime.getRuntime().exec("/usr/sbin/lanscan -a");

				// read the standard output of the command
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
				while (!procDone(p))
				{
					while((s=stdInput.readLine()) !=null)
					{
					   if (s.indexOf("MAC")>=0)
					   {
							int idx = s.indexOf("0x");
							mac = s.substring(idx+2);
					   }
					}
				}
				stdInput.close();
			}
			catch(Exception e)
			{
				
			}
		}
		
		return Const.trim(mac);
	}

	private static final boolean procDone(Process p)
	{
		try 
		{
			p.exitValue();
			return true;
		}
		catch(IllegalThreadStateException e) 
		{
			return false;
		}
	}
	
	/**
	 * Determine the level of where the TreeItem is position in a tree.
	 * @param ti The TreeItem
	 * @return The level of the item in the tree
	 */
	public static final int getTreeLevel(TreeItem ti)
	{
		int level = 0;
		TreeItem parent = ti.getParentItem();
		while (parent!=null)
		{
			level++;
			parent = parent.getParentItem();
		}
		
		return level;
	}

	/**
	 * Get an array of strings containing the path from the given TreeItem to the parent.
	 * @param ti The TreeItem to look at
	 * @return An array of string describing the path to the TreeItem.
	 */
	public static final String[] getTreeStrings(TreeItem ti)
	{
		int nrlevels = getTreeLevel(ti) + 1;
		String retval[] = new String[nrlevels];
		int level = 0;
		
		retval[nrlevels-1] = ti.getText();
		TreeItem parent = ti.getParentItem();
		while (parent!=null)
		{
			level++;
			retval[nrlevels-level-1]=parent.getText();
			parent = parent.getParentItem();
		}
		
		return retval;
	}
	
	/**
	 * Return the tree path seperated by Const.FILE_SEPARATOR, starting from a certain depth in the tree.
	 *
	 * @param ti The TreeItem to get the path for 
	 * @param from The depth to start at, use 0 to get the complete tree.
	 * @return The tree path.
	 */
	public static final String getTreePath(TreeItem ti, int from)
	{
		String path[] = getTreeStrings(ti);
		
		if (path==null) return null;
		
		String retval="";
		
		for (int i=from;i<path.length;i++) 
		{
			if (!path[i].equalsIgnoreCase(Const.FILE_SEPARATOR))
			{
				retval+=Const.FILE_SEPARATOR+path[i];
			}
		}

		return retval;
	}

	/**
	 * Flips the TreeItem from expanded to not expanded or vice-versa.
	 * @param ti The TreeItem to flip.
	 */
	public static final void flipExpanded(TreeItem ti)
	{
		ti.setExpanded(!ti.getExpanded());
	}
	
	/**
	 * Finds a TreeItem with a certain label (name) in a (part of a) tree.
	 * @param parent The TreeItem where we start looking.
	 * @param name The name or item label to look for.
	 * @return The TreeItem if the label was found, null if nothing was found.
	 */
	public static final TreeItem findTreeItem(TreeItem parent, String name)
	{
		if (parent.getText().equalsIgnoreCase(name)) return parent;

		TreeItem ti[] = parent.getItems();
		for (int i=0;i<ti.length;i++)
		{
			TreeItem child = findTreeItem(ti[i], name);
			if (child!=null) return child;
		}
		return null;
	}


	/**
	 * Determines the Kettle directory in the user's home directory.
	 * @return The Kettle directory.
	 */
	public static final String getKettleDirectory()
	{
		return System.getProperty("user.home")+FILE_SEPARATOR+".kettle";
	}
	
	/**
	 * Returns the path to the Kettle local (current directory) repositories XML file.
	 * @return The local repositories file.
	 */
	public static final String getKettleLocalRepositoriesFile()
	{
		return "repositories.xml";
	}

	
	/**
	 * Returns the full path to the Kettle repositories XML file.
	 * @return The Kettle repositories file.
	 */
	public static final String getKettleUserRepositoriesFile()
	{
		return getKettleDirectory()+FILE_SEPARATOR+getKettleLocalRepositoriesFile();
	}


	/**
	 * Find a database with a certain name in an arraylist of databases.
	 * @param databases The ArrayList of databases
	 * @param dbname The name of the database connection
	 * @return The database object if one was found, null otherwise.
	 */
	public static final DatabaseMeta findDatabase(ArrayList databases, String dbname)
	{
		if (databases==null) return null;
		
		for (int i=0;i<databases.size();i++)
		{
			DatabaseMeta ci = (DatabaseMeta)databases.get(i);
			if (ci.getName().equalsIgnoreCase(dbname)) return ci;
		}
		return null;
	}

	/**
	 * Find a database with a certain ID in an arraylist of databases.
	 * @param databases The ArrayList of databases
	 * @param id The id of the database connection
	 * @return The database object if one was found, null otherwise.
	 */
	public static final DatabaseMeta findDatabase(ArrayList databases, long id)
	{
		if (databases==null) return null;
		
		for (int i=0;i<databases.size();i++)
		{
			DatabaseMeta ci = (DatabaseMeta)databases.get(i);
			if (ci.getID() == id) return ci;
		}
		return null;
	}
    
    
    /**
     * Select the SAP R/3 databases in the List of databases.
     * @param databases All the databases
     * @return SAP R/3 databases in a List of databases.
     */
	public static final ArrayList selectSAPR3Databases(ArrayList databases)
    {
        ArrayList sap = new ArrayList();
        
        Iterator it = databases.iterator();
        while (it.hasNext())
        {
            DatabaseMeta db = (DatabaseMeta) it.next();
            if (db.getDatabaseType()==DatabaseMeta.TYPE_DATABASE_SAPR3)
            {
                sap.add(db);
            }
        }
        
        return sap;
    }

	/**
	 * Find a jobentry with a certain ID in an arraylist of job entries.
	 * @param jobentries The ArrayList of jobentries
	 * @param id_jobentry The id of the jobentry
	 * @return The JobEntry object if one was found, null otherwise.
	 */
	public static final JobEntryInterface findJobEntry(ArrayList jobentries, long id_jobentry)
	{
		if (jobentries==null) return null;
		
		for (int i=0;i<jobentries.size();i++)
		{
			JobEntryInterface je = (JobEntryInterface)jobentries.get(i);
			if (je.getID() == id_jobentry) return je;
		}
		return null;
	}

	/**
	 * Find a jobentrycopy with a certain ID in an arraylist of job entry copies.
	 * @param jobcopies The ArrayList of jobentry copies
	 * @param id_jobentry_copy The id of the jobentry copy
	 * @return The JobEntryCopy object if one was found, null otherwise.
	 */
	public static final JobEntryCopy findJobEntryCopy(ArrayList jobcopies, long id_jobentry_copy)
	{
		if (jobcopies==null) return null;
		
		for (int i=0;i<jobcopies.size();i++)
		{
			JobEntryCopy jec = (JobEntryCopy)jobcopies.get(i);
			if (jec.getID() == id_jobentry_copy) return jec;
		}
		return null;
	}

	/**
	 * Gets the value of a commandline option 
	 * @param args The command line arguments
	 * @param option The option to look for
	 * @return The value of the commandline option specified.
	 */
	public static final String getCommandlineOption(ArrayList args, String option)
	{
		String optionStart[] = new String[] { "-", "/" };
        String optionDelim[] = new String[] { "=", ":" };
		
        for (int s=0;s<optionStart.length;s++)
        {
            for (int d=0;d<optionDelim.length;d++)
            {
                String optstr = optionStart[s]+option+optionDelim[d];
                String retval=searchCommandLineOption(args, optstr);
                if (retval!=null) return retval;
            }
        }
        return null;
	}
    
    private static final String searchCommandLineOption(ArrayList args, String prefix)
    {
        String retval=null;
        
        for (int i=args.size()-1;i>=0;i--)
        {
            String arg = (String)args.get(i);
            if (arg!=null && arg.toUpperCase().startsWith(prefix.toUpperCase()) ) 
            {
                retval=arg.substring(prefix.length());
                
                // remove this options from the arguments list...
                // This is why we go from back to front...
                args.remove(i);
                
                // System.out.println("Option ["+prefix+"] found: ["+retval+"]");
            }
        }
        return retval;
    }

	/**
	 * Retrieves the content of an environment variable
	 * 
	 * @param variable The name of the environment variable
	 * @param deflt The default value in case no value was found
	 * @return The value of the environment variable or the value of deflt in case no variable was defined.
	 */
	public static String getEnvironmentVariable(String variable, String deflt)
	{
		return System.getProperty(variable, deflt);
	}

	/**
	 * Replaces environment variables in a string.
	 * For example if you set KETTLE_HOME as an environment variable, you can 
	 * use %%KETTLE_HOME%% in dialogs etc. to refer to this value.
	 * This procedures looks for %%...%% pairs and replaces them including the 
	 * name of the environment variable with the actual value.
	 * In case the variable was not set, nothing is replaced!
	 * 
	 * @param string The source string where text is going to be replaced.
	 *  
	 * @return The expanded string.
	 * 
	 */
	public static final String replEnv(String string)
	{
		if (string==null) return null;
		StringBuffer str = new StringBuffer(string);
		
		int idx=str.indexOf("%%");
		while ( idx >= 0)
		{
			//OK, so we found a marker, look for the next one...
			int to = str.indexOf("%%", idx+2);
			if (to>=0)
			{
				// OK, we found the other marker also...
				String marker = str.substring(idx, to+2);
				String var    = str.substring(idx+2, to);
				
				if (var!=null && var.length()>0)
				{
					// Get the environment variable
					String newval = getEnvironmentVariable(var, null);
					
					if (newval!=null)
					{
						// Replace the whole bunch
						str.replace(idx, to+2, newval);
						//System.out.println("Replaced ["+marker+"] with ["+newval+"]");

						// The last position has changed...
						to+=newval.length()-marker.length();
					}
				}
				
			}
			else // We found the start, but NOT the ending %% without closing %%
			{
				to=idx;
			}
			
			// Look for the next variable to replace...
			idx=str.indexOf("%%", to+1);
		}
		
		return str.toString();
	}
	
	/**
	 * Replaces environment variables in an array of strings.<p>
	 * See also: replEnv(String string)
	 * @param string The array of strings that wants its variables to be replaced.
	 * @return the array with the environment variables replaced.
	 * 
	 */
	public static final String[] replEnv(String string[])
	{
		String retval[] = new String[string.length];
		for (int i=0;i<string.length;i++)
		{
			retval[i] = replEnv(string[i]);
		}
		return retval;
	}
	
	/**
	 * Implements Oracle style NVL function
	 * @param source The source argument
	 * @param def The default value in case source is null
	 * @return source if source is not null, otherwise return def
	 */
	public static final String NVL(String source, String def)
	{
		if (source==null) return def;
		return source;
	}
	
	/**
	 * Search for a string in an array of strings and return the index.
	 * @param lookup The string to search for
	 * @param array The array of strings to look in
	 * @return The index of a search string in an array of strings. -1 if not found.
	 */
	public static final int indexOfString(String lookup, String array[])
	{
		if (array==null) return -1;
		if (lookup==null) return -1;
		
		for (int i=0;i<array.length;i++)
		{
			if (lookup.equalsIgnoreCase(array[i])) return i;
		}
		return -1;
	}
	
	/**
	 * Search for a string in an ArrayList of strings and return the index.
	 * @param lookup The string to search for
	 * @param array The ArrayList of strings to look in
	 * @return The index of a search string in an array of strings. -1 if not found.
	 */
	public static final int indexOfString(String lookup, ArrayList array)
	{
		if (array==null) return -1;
		
		for (int i=0;i<array.size();i++)
		{
			String compare = (String)array.get(i);
			if (lookup.equalsIgnoreCase(compare)) return i;
		}
		return -1;
	}

	/**
	 * Sort the strings of an array in alphabetical order.
	 * @param input The array of strings to sort.
	 * @return The sorted array of strings.
	 */
	public static final String[] sortStrings(String input[])
	{
		for (int i=0;i<input.length;i++)
		{
			for (int j = 0; j < input.length-1; j++)
			{
				if (input[j].compareTo(input[j+1])>0)
				{
					String dummy = input[j];
					input[j] = input[j+1];
					input[j+1] = dummy;
				}
			}
		}
		return input;
	}

	/**
	 * Convert strings separated by a string into an array of strings.<p>
	 * <code>
		 Example: a;b;c;d    ==  new String[] { a, b, c, d }
	 * </code>
	 *  
	 * @param string The string to split
	 * @param separator The separator used.
	 * @return the string split into an array of strings
	 */
	public static final String[] splitString(String string, String separator)
	{
		/*
		 *           0123456
		 *   Example a;b;c;d    -->    new String[] { a, b, c, d }
		 */
		// System.out.println("splitString ["+path+"] using ["+separator+"]");

	    ArrayList list = new ArrayList();
	    
		if (string==null || string.length()==0)
		{
			return new String[] { };
		}
		
		int sepLen = separator.length();
		int from = 0;
		
		for (int i=from;i<string.length();i+=sepLen)
		{
			if (string.substring(i, i+sepLen).equalsIgnoreCase(separator))
			{
			    // OK, we found a separator, the string to add to the list
			    // is [from, i[
			    list.add(NVL(string.substring(from, i), ""));
				from=i+sepLen;
			}
		}
		
		// Wait, if the string didn't end with a separator, we still have information at the end of the string...
		// In our example that would be "d"...
		if (from+sepLen<=string.length())
		{
		    list.add(NVL(string.substring(from,string.length()), ""));
		}
				
		return (String[])list.toArray(new String[list.size()]);
	}

	/**
	 * Convert strings separated by a string into an array of strings.<p>
	 * <code>
	 *   Example /a/b/c --> new String[] { a, b, c }
	 * </code>
	 *  
	 * @param path The string to split
	 * @param separator The separator used.
	 * @return the string split into an array of strings
	 */
	public static final String[] splitPath(String path, String separator)
	{
		/*
		 *           012345
		 *   Example /a/b/c    -->    new String[] { a, b, c }
		 */
		// System.out.println("splitString ["+path+"] using ["+separator+"]");

		if (path==null || path.length()==0 || path.equals(separator))
		{
			return new String[] { };
		}
		int sepLen = separator.length();
		int nr_separators = 0;
		int from = path.startsWith(separator)?sepLen:0;
		if (from!=0) nr_separators++;
		
		for (int i=from;i<path.length();i+=sepLen)
		{
			if (path.substring(i, i+sepLen).equalsIgnoreCase(separator))
			{
				nr_separators++;
			}
		}
		
		String spath[] = new String[nr_separators];
		int nr=0;
		for (int i=from;i<path.length();i+=sepLen)
		{
			if (path.substring(i, i+sepLen).equalsIgnoreCase(separator))
			{
				spath[nr]=path.substring(from, i);
				// System.out.println(nr+" --> ["+spath[nr]+"], (from,to)=("+from+", "+i+")");
				nr++;
				
				from=i+sepLen;
			}
		}
		if (nr<spath.length) 
		{
			spath[nr]=path.substring(from);
			// System.out.println(nr+" --> ["+spath[nr]+"], (from,to)=("+from+", "+path.length()+")");
		}
		
		// 
		// a --> { a }
		//
		if (spath.length==0 && path.length()>0)
		{
		    spath=new String[] { path };
		}
		
		return spath;
	}
	
	
	/** 
	 * Sort the entire ArrayList, if it is not empty
	 */
	public static final void quickSort(ArrayList elements)
	{
		if (! elements.isEmpty())
		{
			quickSort(elements, 0, elements.size()-1);
		}
	}


	/**
	 * QuickSort.java by Henk Jan Nootenboom, 9 Sep 2002
	 * Copyright 2002-2003 SUMit. All Rights Reserved.
	 *
	 * Algorithm designed by prof C. A. R. Hoare, 1962
	 * See http://www.sum-it.nl/en200236.html
	 * for algorithm improvement by Henk Jan Nootenboom, 2002.
	 *
	 * Recursive Quicksort, sorts (part of) a Vector by
	 *  1.  Choose a pivot, an element used for comparison
	 *  2.  dividing into two parts:
	 *      - less than-equal pivot
	 *      - and greater than-equal to pivot.
	 *      A element that is equal to the pivot may end up in any part.
	 *      See www.sum-it.nl/en200236.html for the theory behind this.
	 *  3. Sort the parts recursively until there is only one element left.
	 *
	 * www.sum-it.nl/QuickSort.java this source code
	 * www.sum-it.nl/quicksort.php3 demo of this quicksort in a java applet
	 *
	 * Permission to use, copy, modify, and distribute this java source code
	 * and its documentation for NON-COMMERCIAL or COMMERCIAL purposes and
	 * without fee is hereby granted.
	 * See http://www.sum-it.nl/security/index.html for copyright laws.
	 */
	  private static final void quickSort(ArrayList elements, int lowIndex, int highIndex)
	  {
		int lowToHighIndex;
		int highToLowIndex;
		int pivotIndex;
		Comparable pivotValue;
		Comparable lowToHighValue;
		Comparable highToLowValue;
		Comparable parking;
		int newLowIndex;
		int newHighIndex;
		int compareResult;

		lowToHighIndex = lowIndex;
		highToLowIndex = highIndex;
		
		/** Choose a pivot, remember it's value
		 *  No special action for the pivot element itself.
		 *  It will be treated just like any other element.
		 */
		pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
		pivotValue = (Comparable)elements.get(pivotIndex);

		/** Split the Vector in two parts.
		 *
		 *  The lower part will be lowIndex - newHighIndex,
		 *  containing elements <= pivot Value
		 *
		 *  The higher part will be newLowIndex - highIndex,
		 *  containting elements >= pivot Value
		 * 
		 */
		newLowIndex = highIndex + 1;
		newHighIndex = lowIndex - 1;
		// loop until low meets high
		while ((newHighIndex + 1) < newLowIndex) // loop until partition complete
		{ // loop from low to high to find a candidate for swapping
		  lowToHighValue = (Comparable)elements.get(lowToHighIndex);
		  while (lowToHighIndex < newLowIndex
			& lowToHighValue.compareTo(pivotValue)<0 )
		  { 
			newHighIndex = lowToHighIndex; // add element to lower part
			lowToHighIndex ++;
			lowToHighValue = (Comparable)elements.get(lowToHighIndex);
		  }

		  // loop from high to low find other candidate for swapping
		  highToLowValue = (Comparable)elements.get(highToLowIndex);
		  while (newHighIndex <= highToLowIndex
			& (highToLowValue.compareTo(pivotValue)>0)
			)
		  { 
			newLowIndex = highToLowIndex; // add element to higher part
			highToLowIndex --;
			highToLowValue = (Comparable)elements.get(highToLowIndex);
		  }

		  // swap if needed
		  if (lowToHighIndex == highToLowIndex) // one last element, may go in either part
		  { 
			newHighIndex = lowToHighIndex; // move element arbitrary to lower part
		  }
		  else if (lowToHighIndex < highToLowIndex) // not last element yet
		  { 
			compareResult = lowToHighValue.compareTo(highToLowValue);
			if (compareResult >= 0) // low >= high, swap, even if equal
			{ 
			  parking = lowToHighValue;
			  elements.set(lowToHighIndex, highToLowValue);
			  elements.set(highToLowIndex, parking);

			  newLowIndex = highToLowIndex;
			  newHighIndex = lowToHighIndex;

			  lowToHighIndex ++;
			  highToLowIndex --;
			}
		  }
		}

		// Continue recursion for parts that have more than one element
		if (lowIndex < newHighIndex)
		{ 
			quickSort(elements, lowIndex, newHighIndex); // sort lower subpart
		}
		if (newLowIndex < highIndex)
		{ 
			quickSort(elements, newLowIndex, highIndex); // sort higher subpart
		}
	  }

    /**
     * @return An array of all default conversion formats, to be used in dialogs etc.
     */
    public static String[] getConversionFormats()
    {
        String dats[] = Const.dateFormats;
        String nums[] = Const.numberFormats;
        int totsize = dats.length + nums.length;
        String formats[] = new String[totsize];
        for (int x=0;x<dats.length;x++) formats[x] = dats[x];
        for (int x=0;x<nums.length;x++) formats[dats.length+x] = nums[x];
        
        return formats;
    }
    
    /**
     * Sorts the array of Strings, determines the uniquely occuring strings.  
     * @param strings the array that you want to do a distinct on
     * @return a sorted array of uniquely occuring strings
     */
    public static final String[] getDistinctStrings(String[] strings)
    {
        if (strings==null) return null;
        if (strings.length==0) return new String[] {};
        
        String[] sorted = sortStrings(strings);
        List result = new ArrayList();
        String previous = "";
        for (int i=0;i<sorted.length;i++)
        {
            if (!sorted[i].equalsIgnoreCase(previous))
            {
                result.add(sorted[i]);
            }
            previous=sorted[i];
        }
        
        return (String[])result.toArray(new String[result.size()]);
    }

}
