/**
 * 
 */
package be.ibridge.kettle.chef;

import java.util.ArrayList;
import java.util.List;

import be.ibridge.kettle.job.JobEntryResult;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryCopy;

/**
 * Responsible for tracking the execution of a job as a hierarchy.
 * 
 * @author Matt
 * @since  30-mar-2006
 *
 */
public class JobTracker
{
    /** The job that is doing the tracking */
    private JobMeta jobMeta;
    
    /** The trackers for each individual job entry */
    private List jobTrackers;
    
    /** If the jobTrackers list is empty, then this is the result */
    private JobEntryResult result;
    
    /** The parent job tracker, null if this is the root */
    private JobTracker parentJobTracker;
    
    public JobTracker(JobMeta jobMeta)
    {
        this.jobMeta = jobMeta;
        jobTrackers = new ArrayList();
    }
    /**
     * Creates a jobtracker with a single result
     * @param result the job entry result to track.
     */
    public JobTracker(JobMeta jobMeta, JobEntryResult result)
    {
        this(jobMeta);
        this.result = result;
    }
    
    public void addJobTracker(JobTracker jobTracker)
    {
        jobTrackers.add(jobTracker);
    }
    
    public JobTracker getJobTracker(int i)
    {
        return (JobTracker)jobTrackers.get(i);
    }

    public int nrJobTrackers()
    {
        return jobTrackers.size();
    }
    
     /**
     * @return Returns the jobTrackers.
     */
    public List getJobTrackers()
    {
        return jobTrackers;
    }

    /**
     * @param jobTrackers The jobTrackers to set.
     */
    public void setJobTrackers(List jobTrackers)
    {
        this.jobTrackers = jobTrackers;
    }

    /**
     * @return Returns the result.
     */
    public JobEntryResult getJobEntryResult()
    {
        return result;
    }

    /**
     * @param result The result to set.
     */
    public void setJobEntryResult(JobEntryResult result)
    {
        this.result = result;
    }

    public void clear()
    {
        jobTrackers.clear();
        result = null;
    }

    /**
     * @return Returns the jobMeta.
     */
    public JobMeta getJobMeta()
    {
        return jobMeta;
    }

    /**
     * @param jobMeta The jobMeta to set.
     */
    public void setJobMeta(JobMeta jobMeta)
    {
        this.jobMeta = jobMeta;
    }
    
    /**
     * Finds the JobTracker for the job entry specified.
     * Use this to 
     * @param jobEntryCopy The entry to search the job tracker for
     * @return The JobTracker of null if none could be found...
     */
    public JobTracker findJobTracker(JobEntryCopy jobEntryCopy)
    {
        for (int i=0;i<jobTrackers.size();i++)
        {
            JobTracker tracker = getJobTracker(i);
            JobEntryResult result = tracker.getJobEntryResult();
            if (result!=null)
            {
                JobEntryCopy jobEntry = result.getJobEntry();
                if (jobEntry!=null)
                {
                    if (jobEntry.equals(jobEntryCopy)) return tracker; 
                }
            }
        }
        return null;
    }
    /**
     * @return Returns the parentJobTracker.
     */
    public JobTracker getParentJobTracker()
    {
        return parentJobTracker;
    }
    /**
     * @param parentJobTracker The parentJobTracker to set.
     */
    public void setParentJobTracker(JobTracker parentJobTracker)
    {
        this.parentJobTracker = parentJobTracker;
    }
    
    public int getTotalNumberOfItems()
    {
        int total = 1; // 1 = this one
        
        for (int i=0;i<nrJobTrackers();i++)
        {
            total+=getJobTracker(i).getTotalNumberOfItems();
        }
        
        return total;
    }
}
