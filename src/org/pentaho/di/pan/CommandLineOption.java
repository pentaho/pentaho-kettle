
package org.pentaho.di.pan;

import java.util.List;

import org.pentaho.di.core.Const;

/**
 * 
 * This class allows you to define command-line options
 * 
 * @author matt
 */
public class CommandLineOption
{
	private String option;
	private String description; 
	private boolean yesNo;
	
	private StringBuffer argument;
	private boolean hiddenOption;

	/**
	 * @return Returns the argument.
	 */
	public StringBuffer getArgument()
	{
		return argument;
	}

	/**
	 * @param argument The argument to set.
	 */
	public void setArgument(StringBuffer argument)
	{
		this.argument = argument;
	}

	/**
	 * Creates a new command line option 
	 * @param option The option string
	 * @param description the description of the option
	 * @param argument the StringBuffer that will contain the argument later
	 * @param yesNo true if this is a Yes/No flag
	 * @param hiddenOption true if this option should not be shown in the usage list. 
	 */
	public CommandLineOption(String option, String description, StringBuffer argument, boolean yesNo, boolean hiddenOption)
	{
		super();

		this.option = option;
		this.description = description;
		this.argument = argument;
		this.yesNo = yesNo;
		this.hiddenOption = hiddenOption;
	}
	
	/**
	 * Creates a new normal command line option 
	 * @param option The option string
	 * @param description the description of the option
	 */
	public CommandLineOption(String option, String description, StringBuffer argument)
	{
		this(option, description, argument, false, false);
	}

	/**
	 * Creates a new normal command line option without a description 
	 * @param option The option string
	 */
	public CommandLineOption(String option)
	{
		this(option, null, new StringBuffer(), false, false);
	}

	/**
	 * @return Returns the option.
	 */
	public String getOption()
	{
		return option;
	}

	/**
	 * @param option The option to set.
	 */
	public void setOption(String option)
	{
		this.option = option;
	}

	/**
	 * @return Returns wether or not this is a  Yes/No flag
	 */
	public boolean isYesNo()
	{
		return yesNo;
	}

	/**
	 * @param yesNo sets wether or not this is a  Yes/No flag
	 */
	public void setYesNo(boolean yesNo)
	{
		this.yesNo = yesNo;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * @return the usage description
	 *
	 */
	public String getUsageDescription()
	{
        String optionStart;
        String optionDelim;
        if (Const.isWindows()) 
        {
            optionStart = "  /";
            optionDelim = " : ";
        }
        else
        {
            optionStart = "  -";
            optionDelim = " = ";
        }

		return optionStart+Const.rightPad(option, 10)+optionDelim+description;
	}
	
	/**
	 * Gets the value of a commandline option 
	 * @param arg The command line argument
	 * @return The value of the commandline option specified.
	 */
	public String extractAndSetArgument(String arg)
	{
		String optionStart[] = new String[] { "-", "/" };
		String optionDelim[] = new String[] { "=", ":" };

		for (int s = 0; s < optionStart.length; s++)
		{
			int osLength = optionStart[s].length();
			for (int d = 0; d < optionDelim.length; d++)
			{
				int optLength = optionDelim[d].length();
				if (arg != null && arg.length()>osLength && arg.toUpperCase().substring(osLength).startsWith(option.toUpperCase()))
				{
					// OK, this is it.
					// Do we expect anything after this? 
					// after the start, the option and the delimiter?
					//
					String value =null;
					int valueStart = osLength+option.length()+optLength;

					// System.out.println("Arg: ["+arg+"], option: ["+option+"], valueStart: "+valueStart);
					//
					if (arg.length()>=valueStart)
					{
						value = arg.substring(valueStart);
					}
					
					// If it's a Y/N kind of deal: set it to Y if nothing was specified
					if ((value==null || value.length()==0) && yesNo)
					{
						value="Y";
					}
					
					// Add it to the StringBuffer
					argument.append(value);
					
					return value;
				}
			}
		}
		return null;
	}

	/**
	 * @return Returns the hiddenOption.
	 */
	public boolean isHiddenOption()
	{
		return hiddenOption;
	}

	/**
	 * @param hiddenOption The hiddenOption to set.
	 */
	public void setHiddenOption(boolean hiddenOption)
	{
		this.hiddenOption = hiddenOption;
	}

	/**
	 * Parse and set the command line arguments using the defined options
	 * @param args The list of arguments to parse
	 * @param options The command line options to use
	 */
	public static void parseArguments(List<String> args, CommandLineOption[] options)
	{
		for (int i=0;i<options.length;i++)
		{
			boolean found=false;
			for (int j=0;j<args.size() && !found;j++)
			{
				String argument = options[i].extractAndSetArgument((String)args.get(j));
				if (argument!=null) 
				{
					found=true;
					args.remove(j); // We covered it: remove from the list
				}
			}
		}
    }

	/**
	 * Print the usage of an application using the command line options.
	 * @param options the options to use
	 */
	public static void printUsage(CommandLineOption[] options)
	{
	    System.out.println("Options:");
	    for (int i=0;i<options.length;i++) 
	    {
	    	if (!options[i].isHiddenOption()) System.out.println(options[i].getUsageDescription());
	    }
	    System.out.println("");
	}
}
