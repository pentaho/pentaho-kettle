package org.pentaho.di.trans.steps.textfileinput;

/**
 * Processor of Filters. Kind of inversion principle, and to make
 * unit testing easier.
 * 
 * @author Sven Boden
 */
public class TextFileFilterProcessor
{
    /** The filters to process */
    private TextFileFilter filters[];
    
    private boolean stopProcessing;

    /**
     * @param filters        The filters to process
     */
    public TextFileFilterProcessor(TextFileFilter filters[])
    {
       this.filters = filters;
       this.stopProcessing = false;
       
       if ( filters.length == 0 )
       {
    	   // This makes processing faster in case there are no filters.
    	   filters = null;
       }
    }

    public boolean doFilters(String line)
    {    	
    	if ( filters == null )
    		return true;
    	
    	boolean filterOK = true;  // if false: skip this row
    	
	    for (int f = 0; f < filters.length && filterOK; f++)
	    {
		    TextFileFilter filter = filters[f];
		    if (filter.getFilterString() != null && filter.getFilterString().length() > 0)
		    {
			    int from = filter.getFilterPosition();
			    if (from >= 0)
			    {
				    int to = from + filter.getFilterString().length();
				    if (line.length() >= from && line.length() >= to)
				    {
					    String sub = line.substring(filter.getFilterPosition(), to);
					    if (sub.equalsIgnoreCase(filter.getFilterString()))
					    {
						    filterOK = false; // skip this one!
					    }
				    }
			    }
			    else
			    // anywhere on the line
			    {
				    int idx = line.indexOf(filter.getFilterString());
				    if (idx >= 0)
				    {
  					    filterOK = false;
				    }
			    }

			    if (!filterOK)
			    {
			    	boolean isFilterLastLine=filter.isFilterLastLine();
				    if ( isFilterLastLine )
				    {
					    stopProcessing = true;
				    }
				}
			}
		}
	    return filterOK;
    }

    /**
     * Was processing requested to be stopped. Can only be true when doFilters was
     * false.
     * 
     * @return == true: processing should stop, == false: processing should continue.
     */
	public boolean isStopProcessing() {
		return stopProcessing;
	}    
}