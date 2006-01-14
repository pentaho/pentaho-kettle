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

package be.ibridge.kettle.trans;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.DBCache;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Rectangle;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SQLStatement;
import be.ibridge.kettle.core.TransAction;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLInterface;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;

/**
 * This class defines a transformation and offers methods to save and load it from XML or a Kettle database repository.
 * 
 * @since 20-jun-2003
 * @author Matt
 * 
 */
public class TransMeta implements XMLInterface
{
    private static LogWriter    log                = LogWriter.getInstance();

    private ArrayList           databases;

    private ArrayList           steps;

    private ArrayList           hops;

    private ArrayList           notes;

    private ArrayList           dependencies;

    /**  variables set for the transformation */
    private Hashtable           variables;

    private RepositoryDirectory directory;

    private RepositoryDirectory directoryTree;

    private String              name;

    private String              filename;

    private StepMeta            readStep;

    private StepMeta            writeStep;

    private StepMeta            inputStep;

    private StepMeta            outputStep;

    private StepMeta            updateStep;

    private String              logTable;

    private DatabaseMeta        logConnection;

    private int                 sizeRowset;

    private DatabaseMeta        maxDateConnection;

    private String              maxDateTable;

    private String              maxDateField;

    private double              maxDateOffset;

    private double              maxDateDifference;

    private String              arguments[];

    private Hashtable           counters;

    private ArrayList           sourceRows;

    private ArrayList           resultRows;

    private boolean             changed, changed_steps, changed_databases, changed_hops, changed_notes;

    // public Props props;
    private ArrayList           undo;

    private int                 max_undo;

    private int                 undo_position;

    private DBCache             dbCache;

    private long                id;

    private boolean             useBatchId;

    private long                batchId;

    private boolean             logfieldUsed;

    private String              createdUser, modifiedUser;

    private Value               createdDate, modifiedDate;

    private int                 sleepTimeEmpty;

    private int                 sleepTimeFull;

    // //////////////////////////////////////////////////////////////////////////

    public static final int     TYPE_UNDO_CHANGE   = 1;

    public static final int     TYPE_UNDO_NEW      = 2;

    public static final int     TYPE_UNDO_DELETE   = 3;

    public static final int     TYPE_UNDO_POSITION = 4;

    public static final String  desc_type_undo[]   = { "", "Undo change", "Undo new", "Undo delete", "Undo position" };

    /**
     * Builds a new empty transformation.
     */
    public TransMeta()
    {
        clear();
    }

    /**
     * Constructs a new transformation specifying the filename, name and arguments.
     * 
     * @param filename The filename of the transformation
     * @param name The name of the transformation
     * @param arguments The arguments as Strings
     */
    public TransMeta(String filename, String name, String arguments[])
    {
        clear();
        this.filename = filename;
        this.name = name;
        this.arguments = arguments;
    }

    /**
     * Get the database ID in the repository for this object.
     * 
     * @return the database ID in the repository for this object.
     */
    public long getID()
    {
        return id;
    }

    /**
     * Set the database ID for this object in the repository.
     * 
     * @param id the database ID for this object in the repository.
     */
    public void setID(long id)
    {
        this.id = id;
    }

    /**
     * Clears the transformation.
     */
    public void clear()
    {
        setID(-1L);
        databases = new ArrayList();
        steps = new ArrayList();
        hops = new ArrayList();
        notes = new ArrayList();
        dependencies = new ArrayList();
        variables = new Hashtable();
        name = null;
        filename = null;
        readStep = null;
        writeStep = null;
        inputStep = null;
        outputStep = null;
        updateStep = null;
        logTable = null;
        logConnection = null;
        
        sizeRowset     = Const.ROWS_IN_ROWSET;
        sleepTimeEmpty = Const.SLEEP_EMPTY_NANOS;
        sleepTimeFull  = Const.SLEEP_FULL_NANOS;

        maxDateConnection = null;
        maxDateTable = null;
        maxDateField = null;
        maxDateOffset = 0.0;

        maxDateDifference = 0.0;

        undo = new ArrayList();
        max_undo = Const.MAX_UNDO;
        undo_position = -1;

        counters = new Hashtable();
        resultRows = null;

        clearUndo();
        clearChanged();

        useBatchId = false;
        batchId = 0;

        modifiedUser = "-";
        modifiedDate = new Value("modified_date", new Date());

        // LOAD THE DATABASE CACHE!
        dbCache = DBCache.getInstance();

        directoryTree = new RepositoryDirectory();

        // Default directory: root
        directory = directoryTree;
    }

    public void clearUndo()
    {
        undo = new ArrayList();
        undo_position = -1;
    }

    /**
     * Get an ArrayList of defined DatabaseInfo objects.
     * 
     * @return an ArrayList of defined DatabaseInfo objects.
     */
    public ArrayList getDatabases()
    {
        return databases;
    }

    /**
     * @param databases The databases to set.
     */
    public void setDatabases(ArrayList databases)
    {
        this.databases = databases;
    }

    /**
     * Add a database connection to the transformation.
     * 
     * @param ci The database connection information.
     */
    public void addDatabase(DatabaseMeta ci)
    {
        databases.add(ci);
        changed_databases = true;
    }

    /**
     * Add a new step to the transformation
     * 
     * @param stepMeta The step to be added.
     */
    public void addStep(StepMeta stepMeta)
    {
        steps.add(stepMeta);
        changed_steps = true;
    }

    /**
     * Add a new hop to the transformation.
     * 
     * @param hi The hop to be added.
     */
    public void addTransHop(TransHopMeta hi)
    {
        hops.add(hi);
        changed_hops = true;
    }

    /**
     * Add a new note to the transformation.
     * 
     * @param ni The note to be added.
     */
    public void addNote(NotePadMeta ni)
    {
        notes.add(ni);
        changed_notes = true;
    }

    /**
     * Add a new dependency to the transformation.
     * 
     * @param td The transformation dependency to be added.
     */
    public void addDependency(TransDependency td)
    {
        dependencies.add(td);
    }

    /**
     * Add a database connection to the transformation on a certain location.
     * 
     * @param p The location
     * @param ci The database connection information.
     */
    public void addDatabase(int p, DatabaseMeta ci)
    {
        databases.add(p, ci);
        changed_databases = true;
    }

    /**
     * Add a new step to the transformation
     * 
     * @param p The location
     * @param stepMeta The step to be added.
     */
    public void addStep(int p, StepMeta stepMeta)
    {
        steps.add(p, stepMeta);
        changed_steps = true;
    }

    /**
     * Add a new hop to the transformation on a certain location.
     * 
     * @param p the location
     * @param hi The hop to be added.
     */
    public void addTransHop(int p, TransHopMeta hi)
    {
        hops.add(p, hi);
        changed_hops = true;
    }

    /**
     * Add a new note to the transformation on a certain location.
     * 
     * @param p The location
     * @param ni The note to be added.
     */
    public void addNote(int p, NotePadMeta ni)
    {
        notes.add(p, ni);
        changed_notes = true;
    }

    /**
     * Add a new dependency to the transformation on a certain location
     * 
     * @param p The location.
     * @param td The transformation dependency to be added.
     */
    public void addDependency(int p, TransDependency td)
    {
        dependencies.add(p, td);
    }

    /**
     * Retrieves a database connection information a a certain location.
     * 
     * @param i The database number.
     * @return The database connection information.
     */
    public DatabaseMeta getDatabase(int i)
    {
        return (DatabaseMeta) databases.get(i);
    }

    /**
     * Get an ArrayList of defined steps.
     * 
     * @return an ArrayList of defined steps.
     */
    public ArrayList getSteps()
    {
        return steps;
    }

    /**
     * Retrieves a step on a certain location.
     * 
     * @param i The location.
     * @return The step information.
     */
    public StepMeta getStep(int i)
    {
        return (StepMeta) steps.get(i);
    }

    /**
     * Retrieves a hop on a certain location.
     * 
     * @param i The location.
     * @return The hop information.
     */
    public TransHopMeta getTransHop(int i)
    {
        return (TransHopMeta) hops.get(i);
    }

    /**
     * Retrieves notepad information on a certain location.
     * 
     * @param i The location
     * @return The notepad information.
     */
    public NotePadMeta getNote(int i)
    {
        return (NotePadMeta) notes.get(i);
    }

    /**
     * Retrieves a dependency on a certain location.
     * 
     * @param i The location.
     * @return The dependency.
     */
    public TransDependency getDependency(int i)
    {
        return (TransDependency) dependencies.get(i);
    }

    /**
     * Removes a database from the transformation on a certain location.
     * 
     * @param i The location
     */
    public void removeDatabase(int i)
    {
        if (i < 0 || i >= databases.size()) return;
        databases.remove(i);
        changed_databases = true;
    }

    /**
     * Removes a step from the transformation on a certain location.
     * 
     * @param i The location
     */
    public void removeStep(int i)
    {
        if (i < 0 || i >= steps.size()) return;

        steps.remove(i);
        changed_steps = true;
    }

    /**
     * Removes a hop from the transformation on a certain location.
     * 
     * @param i The location
     */
    public void removeTransHop(int i)
    {
        if (i < 0 || i >= hops.size()) return;

        hops.remove(i);
        changed_hops = true;
    }

    /**
     * Removes a note from the transformation on a certain location.
     * 
     * @param i The location
     */
    public void removeNote(int i)
    {
        if (i < 0 || i >= notes.size()) return;
        notes.remove(i);
        changed_notes = true;
    }

    /**
     * Removes a dependency from the transformation on a certain location.
     * 
     * @param i The location
     */
    public void removeDependency(int i)
    {
        if (i < 0 || i >= dependencies.size()) return;
        dependencies.remove(i);
    }

    /**
     * Clears all the dependencies from the transformation.
     */
    public void removeAllDependencies()
    {
        dependencies.clear();
    }

    /**
     * Count the nr of databases in the transformation.
     * 
     * @return The nr of databases
     */
    public int nrDatabases()
    {
        return databases.size();
    }

    /**
     * Count the nr of steps in the transformation.
     * 
     * @return The nr of steps
     */
    public int nrSteps()
    {
        return steps.size();
    }

    /**
     * Count the nr of hops in the transformation.
     * 
     * @return The nr of hops
     */
    public int nrTransHops()
    {
        return hops.size();
    }

    /**
     * Count the nr of notes in the transformation.
     * 
     * @return The nr of notes
     */
    public int nrNotes()
    {
        return notes.size();
    }

    /**
     * Count the nr of dependencies in the transformation.
     * 
     * @return The nr of dependencies
     */
    public int nrDependencies()
    {
        return dependencies.size();
    }

    /**
     * Sets the variable with a certain name & content. This variable can be used in all the steps of the transformation
     * by using %%VARIABLE%% in file-names etc.
     * 
     * @param name The name of the variable
     * @param content The value
     */
    public void setVariable(String name, String content)
    {
        variables.put(name, content);
    }

    /**
     * Get the value of a transformation variable
     * 
     * @param name The name of the variable
     * @return The value or null if the variable wasn't set.
     */
    public String getVariable(String name)
    {
        return (String) variables.get(name);
    }

    /**
     * Changes the content of a step on a certain position
     * 
     * @param i The position
     * @param stepMeta The Step
     */
    public void setStep(int i, StepMeta stepMeta)
    {
        steps.set(i, stepMeta);
    }

    /**
     * Changes the content of a hop on a certain position
     * 
     * @param i The position
     * @param hi The hop
     */
    public void setTransHop(int i, TransHopMeta hi)
    {
        hops.set(i, hi);
    }

    /**
     * Counts the number of steps that are actually used in the transformation.
     * 
     * @return the number of used steps.
     */
    public int nrUsedSteps()
    {
        int nr = 0;
        for (int i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            if (isStepUsedInTransHops(stepMeta)) nr++;
        }
        return nr;
    }

    /**
     * Gets a used step on a certain location
     * 
     * @param lu The location
     * @return The used step.
     */
    public StepMeta getUsedStep(int lu)
    {
        int nr = 0;
        for (int i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            if (isStepUsedInTransHops(stepMeta))
            {
                if (lu == nr) return stepMeta;
                nr++;
            }
        }
        return null;
    }

    /**
     * Searches the list of databases for a database with a certain name
     * 
     * @param name The name of the database connection
     * @return The database connection information or null if nothing was found.
     */
    public DatabaseMeta findDatabase(String name)
    {
        int i;
        for (i = 0; i < nrDatabases(); i++)
        {
            DatabaseMeta ci = getDatabase(i);
            if (ci.getName().equalsIgnoreCase(name)) { return ci; }
        }
        return null;
    }

    /**
     * Searches the list of steps for a step with a certain name
     * 
     * @param name The name of the step to look for
     * @return The step information or null if no nothing was found.
     */
    public StepMeta findStep(String name)
    {
        return findStep(name, null);
    }

    /**
     * Searches the list of steps for a step with a certain name while excluding one step.
     * 
     * @param name The name of the step to look for
     * @param exclude The step information to exclude.
     * @return The step information or null if nothing was found.
     */
    public StepMeta findStep(String name, StepMeta exclude)
    {
        if (name==null) return null;
        
        int excl = -1;
        if (exclude != null) excl = indexOfStep(exclude);

        for (int i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            if (i != excl && stepMeta.getName().equalsIgnoreCase(name)) { return stepMeta; }
        }
        return null;
    }

    /**
     * Searches the list of hops for a hop with a certain name
     * 
     * @param name The name of the hop to look for
     * @return The hop information or null if nothing was found.
     */
    public TransHopMeta findTransHop(String name)
    {
        int i;

        for (i = 0; i < nrTransHops(); i++)
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.toString().equalsIgnoreCase(name)) { return hi; }
        }
        return null;
    }

    /**
     * Search all hops for a hop where a certain step is at the start.
     * 
     * @param fromstep The step at the start of the hop.
     * @return The hop or null if no hop was found.
     */
    public TransHopMeta findTransHopFrom(StepMeta fromstep)
    {
        int i;
        for (i = 0; i < nrTransHops(); i++)
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.getFromStep() != null && hi.getFromStep().equals(fromstep)) // return the first
            { return hi; }
        }
        return null;
    }

    /**
     * Find a certain hop in the transformation..
     * 
     * @param hi The hop information to look for.
     * @return The hop or null if no hop was found.
     */
    public TransHopMeta findTransHop(TransHopMeta hi)
    {
        return findTransHop(hi.getFromStep(), hi.getToStep());
    }

    /**
     * Search all hops for a hop where a certain step is at the start and another is at the end.
     * 
     * @param from The step at the start of the hop.
     * @param to The step at the end of the hop.
     * @return The hop or null if no hop was found.
     */
    public TransHopMeta findTransHop(StepMeta from, StepMeta to)
    {

        int i;
        for (i = 0; i < nrTransHops(); i++)
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.isEnabled())
            {
                if (hi.getFromStep() != null && hi.getToStep() != null && hi.getFromStep().equals(from) && hi.getToStep().equals(to)) { return hi; }
            }
        }
        return null;
    }

    /**
     * Search all hops for a hop where a certain step is at the end.
     * 
     * @param tostep The step at the end of the hop.
     * @return The hop or null if no hop was found.
     */
    public TransHopMeta findTransHopTo(StepMeta tostep)
    {
        int i;
        for (i = 0; i < nrTransHops(); i++)
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.getToStep() != null && hi.getToStep().equals(tostep)) // Return the first!
            { return hi; }
        }
        return null;
    }

    /**
     * Determines whether or not a certain step is informative. This means that the previous step is sending information
     * to this step, but only informative. This means that this step is using the information to process the actual
     * stream of data. We use this in StreamLookup, TableInput and other types of steps.
     * 
     * @param this_step The step that is receiving information.
     * @param prev_step The step that is sending information
     * @return true if prev_step if informative for this_step.
     */
    public boolean isStepInformative(StepMeta this_step, StepMeta prev_step)
    {
        String[] infoSteps = this_step.getStepMetaInterface().getInfoSteps();
        if (infoSteps == null) return false;
        for (int i = 0; i < infoSteps.length; i++)
        {
            if (prev_step.getName().equalsIgnoreCase(infoSteps[i])) return true;
        }

        return false;
    }

    /**
     * Counts the number of previous steps for a step name.
     * 
     * @param stepname The name of the step to start from
     * @return The number of preceding steps.
     */
    public int findNrPrevSteps(String stepname)
    {
        return findNrPrevSteps(findStep(stepname), false);
    }

    /**
     * Counts the number of previous steps for a step name taking into account whether or not they are informational.
     * 
     * @param stepname The name of the step to start from
     * @return The number of preceding steps.
     */
    public int findNrPrevSteps(String stepname, boolean info)
    {
        return findNrPrevSteps(findStep(stepname), info);
    }

    /**
     * Find the number of steps that precede the indicated step.
     * 
     * @param stepMeta The source step
     * 
     * @return The number of preceding steps found.
     */
    public int findNrPrevSteps(StepMeta stepMeta)
    {
        return findNrPrevSteps(stepMeta, false);
    }

    /**
     * Find the previous step on a certain location.
     * 
     * @param stepname The source step name
     * @param nr the location
     * 
     * @return The preceding step found.
     */
    public StepMeta findPrevStep(String stepname, int nr)
    {
        return findPrevStep(findStep(stepname), nr);
    }

    /**
     * Find the previous step on a certain location taking into account the steps being informational or not.
     * 
     * @param stepname The name of the step
     * @param nr The location
     * @param info true if we only want the informational steps.
     * @return The step information
     */
    public StepMeta findPrevStep(String stepname, int nr, boolean info)
    {
        return findPrevStep(findStep(stepname), nr, info);
    }

    /**
     * Find the previous step on a certain location.
     * 
     * @param stepMeta The source step information
     * @param nr the location
     * 
     * @return The preceding step found.
     */
    public StepMeta findPrevStep(StepMeta stepMeta, int nr)
    {
        return findPrevStep(stepMeta, nr, false);
    }

    /**
     * Count the number of previous steps on a certain location taking into account the steps being informational or
     * not.
     * 
     * @param stepMeta The name of the step
     * @param info true if we only want the informational steps.
     * @return The number of preceding steps
     */
    public int findNrPrevSteps(StepMeta stepMeta, boolean info)
    {
        int count = 0;
        int i;

        for (i = 0; i < nrTransHops(); i++) // Look at all the hops;
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.getToStep() != null && hi.isEnabled() && hi.getToStep().equals(stepMeta))
            {
                // Check if this previous step isn't informative (StreamValueLookup)
                // We don't want fields from this stream to show up!
                if (info || !isStepInformative(stepMeta, hi.getFromStep()))
                {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Find the previous step on a certain location taking into account the steps being informational or not.
     * 
     * @param stepMeta The step
     * @param nr The location
     * @param info true if we only want the informational steps.
     * @return The preceding step information
     */
    public StepMeta findPrevStep(StepMeta stepMeta, int nr, boolean info)
    {
        int count = 0;
        int i;

        for (i = 0; i < nrTransHops(); i++) // Look at all the hops;
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.getToStep() != null && hi.isEnabled() && hi.getToStep().equals(stepMeta))
            {
                if (info || !isStepInformative(stepMeta, hi.getFromStep()))
                {
                    if (count == nr) { return hi.getFromStep(); }
                    count++;
                }
            }
        }
        return null;
    }

    /**
     * Get the informational steps for a certain step. An informational step is a step that provides information for
     * lookups etc.
     * 
     * @param stepMeta The name of the step
     * @return The informational steps found
     */
    public StepMeta[] getInfoStep(StepMeta stepMeta)
    {
        String[] infoStepName = stepMeta.getStepMetaInterface().getInfoSteps();
        if (infoStepName == null) return null;

        StepMeta[] infoStep = new StepMeta[infoStepName.length];
        for (int i = 0; i < infoStep.length; i++)
        {
            infoStep[i] = findStep(infoStepName[i]);
        }

        return infoStep;
    }

    /**
     * Find the the number of informational steps for a certains step.
     * 
     * @param stepMeta The step
     * @return The number of informational steps found.
     */
    public int findNrInfoSteps(StepMeta stepMeta)
    {
        if (stepMeta == null) return 0;

        int count = 0;

        for (int i = 0; i < nrTransHops(); i++) // Look at all the hops;
        {
            TransHopMeta hi = getTransHop(i);
            if (hi == null || hi.getToStep() == null)
            {
                log.logError(toString(), "Internal error detected, a hop's destination can't be null!");
            }
            if (hi != null && hi.getToStep() != null && hi.isEnabled() && hi.getToStep().equals(stepMeta))
            {
                // Check if this previous step isn't informative (StreamValueLookup)
                // We don't want fields from this stream to show up!
                if (isStepInformative(stepMeta, hi.getFromStep()))
                {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Find the informational fields coming from an informational step into the step specified.
     * 
     * @param stepname The name of the step
     * @return A row containing fields with origin.
     */
    public Row getPrevInfoFields(String stepname) throws KettleStepException
    {
        return getPrevInfoFields(findStep(stepname));
    }

    /**
     * Find the informational fields coming from an informational step into the step specified.
     * 
     * @param stepMeta The receiving step
     * @return A row containing fields with origin.
     */
    public Row getPrevInfoFields(StepMeta stepMeta) throws KettleStepException
    {
        Row row = new Row();

        for (int i = 0; i < nrTransHops(); i++) // Look at all the hops;
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.isEnabled() && hi.getToStep().equals(stepMeta))
            {
                if (isStepInformative(stepMeta, hi.getFromStep()))
                {
                    getThisStepFields(stepMeta, row);
                    return row;
                }
            }
        }
        return row;
    }

    /**
     * Find the number of succeeding steps for a certain originating step.
     * 
     * @param stepMeta The originating step
     * @return The number of succeeding steps.
     */
    public int findNrNextSteps(StepMeta stepMeta)
    {
        int count = 0;
        int i;
        for (i = 0; i < nrTransHops(); i++) // Look at all the hops;
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.isEnabled() && hi.getFromStep().equals(stepMeta)) count++;
        }
        return count;
    }

    /**
     * Find the succeeding step at a location for an originating step.
     * 
     * @param stepMeta The originating step
     * @param nr The location
     * @return The step found.
     */
    public StepMeta findNextStep(StepMeta stepMeta, int nr)
    {
        int count = 0;
        int i;

        for (i = 0; i < nrTransHops(); i++) // Look at all the hops;
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.isEnabled() && hi.getFromStep().equals(stepMeta))
            {
                if (count == nr) { return hi.getToStep(); }
                count++;
            }
        }
        return null;
    }

    /**
     * Retrieve an array of preceding steps for a certain destination step.
     * 
     * @param stepMeta The destination step
     * @return An array containing the preceding steps.
     */
    public StepMeta[] getPrevSteps(StepMeta stepMeta)
    {
        int nr = findNrPrevSteps(stepMeta, true);
        StepMeta retval[] = new StepMeta[nr];

        for (int i = 0; i < nr; i++)
        {
            retval[i] = findPrevStep(stepMeta, i, true);
        }
        return retval;
    }

    /**
     * Retrieve an array of succeeding step names for a certain originating step name.
     * 
     * @param stepname The originating step name
     * @return An array of succeeding step names
     */
    public String[] getPrevStepNames(String stepname)
    {
        return getPrevStepNames(findStep(stepname));
    }

    /**
     * Retrieve an array of preceding steps for a certain destination step.
     * 
     * @param stepMeta The destination step
     * @return an array of preceding step names.
     */
    public String[] getPrevStepNames(StepMeta stepMeta)
    {
        StepMeta prevStepMetas[] = getPrevSteps(stepMeta);
        String retval[] = new String[prevStepMetas.length];
        for (int x = 0; x < prevStepMetas.length; x++)
            retval[x] = prevStepMetas[x].getName();

        return retval;
    }

    /**
     * Retrieve an array of succeeding steps for a certain originating step.
     * 
     * @param stepMeta The originating step
     * @return an array of succeeding steps.
     */
    public StepMeta[] getNextSteps(StepMeta stepMeta)
    {
        int nr = findNrNextSteps(stepMeta);
        StepMeta retval[] = new StepMeta[nr];

        for (int i = 0; i < nr; i++)
        {
            retval[i] = findNextStep(stepMeta, i);
        }
        return retval;
    }

    /**
     * Retrieve an array of succeeding step names for a certain originating step.
     * 
     * @param stepMeta The originating step
     * @return an array of succeeding step names.
     */
    public String[] getNextStepNames(StepMeta stepMeta)
    {
        StepMeta nextStepMeta[] = getNextSteps(stepMeta);
        String retval[] = new String[nextStepMeta.length];
        for (int x = 0; x < nextStepMeta.length; x++)
            retval[x] = nextStepMeta[x].getName();

        return retval;
    }

    /**
     * Find the step that is located on a certain point on the canvas, taking into account the icon size.
     * 
     * @param x the x-coordinate of the point queried
     * @param y the y-coordinate of the point queried
     * @return The step information if a step is located at the point. Otherwise, if no step was found: null.
     */
    public StepMeta getStep(int x, int y, int iconsize)
    {
        int i, s;
        s = steps.size();
        for (i = s - 1; i >= 0; i--) // Back to front because drawing goes from start to end
        {
            StepMeta stepMeta = (StepMeta) steps.get(i);
            if (partOfTransHop(stepMeta) || stepMeta.isDrawn()) // Only consider steps from active or inactive hops!
            {
                Point p = stepMeta.getLocation();
                if (p != null)
                {
                    if (x >= p.x && x <= p.x + iconsize && y >= p.y && y <= p.y + iconsize + 20) { return stepMeta; }
                }
            }
        }
        return null;
    }

    /**
     * Find the note that is located on a certain point on the canvas.
     * 
     * @param x the x-coordinate of the point queried
     * @param y the y-coordinate of the point queried
     * @return The note information if a note is located at the point. Otherwise, if nothing was found: null.
     */
    public NotePadMeta getNote(int x, int y)
    {
        int i, s;
        s = notes.size();
        for (i = s - 1; i >= 0; i--) // Back to front because drawing goes from start to end
        {
            NotePadMeta ni = (NotePadMeta) notes.get(i);
            Point loc = ni.getLocation();
            Point p = new Point(loc.x, loc.y);
            if (x >= p.x && x <= p.x + ni.width + 2 * Const.NOTE_MARGIN && y >= p.y && y <= p.y + ni.height + 2 * Const.NOTE_MARGIN) { return ni; }
        }
        return null;
    }

    /**
     * Determines whether or not a certain step is part of a hop.
     * 
     * @param stepMeta The step queried
     * @return true if the step is part of a hop.
     */
    public boolean partOfTransHop(StepMeta stepMeta)
    {
        int i;
        for (i = 0; i < nrTransHops(); i++)
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.getFromStep() == null || hi.getToStep() == null) return false;
            if (hi.getFromStep().equals(stepMeta) || hi.getToStep().equals(stepMeta)) return true;
        }
        return false;
    }

    /**
     * Returns the fields that are emitted by a certain step name
     * 
     * @param stepname The stepname of the step to be queried.
     * @return A row containing the fields emitted.
     */
    public Row getStepFields(String stepname) throws KettleStepException
    {
        StepMeta stepMeta = findStep(stepname);
        if (stepMeta != null)
            return getStepFields(stepMeta);
        else
            return null;
    }

    /**
     * Returns the fields that are emitted by a certain step
     * 
     * @param stepMeta The step to be queried.
     * @return A row containing the fields emitted.
     */
    public Row getStepFields(StepMeta stepMeta) throws KettleStepException
    {
        return getStepFields(stepMeta, null);
    }

    public Row getStepFields(StepMeta[] stepMeta) throws KettleStepException
    {
        Row fields = new Row();

        for (int i = 0; i < stepMeta.length; i++)
        {
            Row flds = getStepFields(stepMeta[i]);
            if (flds != null) fields.mergeRow(flds);
        }
        return fields;
    }

    /**
     * Returns the fields that are emitted by a certain step
     * 
     * @param stepMeta The step to be queried.
     * @param monitor The progress monitor for progress dialog. (null if not used!)
     * @return A row containing the fields emitted.
     */
    public Row getStepFields(StepMeta stepMeta, IProgressMonitor monitor) throws KettleStepException
    {
        Row row = new Row();

        if (stepMeta == null) return row;

        log.logDebug(toString(), "From step: " + stepMeta.getName() + ", looking at " + findNrPrevSteps(stepMeta) + " prev. steps.");
        for (int i = 0; i < findNrPrevSteps(stepMeta); i++)
        {
            StepMeta prevStepMeta = findPrevStep(stepMeta, i);

            if (monitor != null)
            {
                monitor.subTask("Checking step [" + prevStepMeta.getName() + "]");
            }

            Row add = getStepFields(prevStepMeta, monitor);
            if (add == null) add = new Row();
            log.logDebug(toString(), "Found fields to add: " + add.toString());
            if (i == 0)
            {
                row.addRow(add);
            }
            else
            {
                // See if the add fields are not already in the row
                for (int x = 0; x < add.size(); x++)
                {
                    Value v = add.getValue(x);
                    Value s = row.searchValue(v.getName());
                    if (s == null)
                    {
                        row.addValue(v);
                    }
                }
            }
        }
        // Finally, see if we need to add/modify/delete fields with this step "name"
        return getThisStepFields(stepMeta, row, monitor);
    }

    /**
     * Find the fields that are entering a step with a certain name.
     * 
     * @param stepname The name of the step queried
     * @return A row containing the fields (w/ origin) entering the step
     */
    public Row getPrevStepFields(String stepname) throws KettleStepException
    {
        return getPrevStepFields(findStep(stepname));
    }

    /**
     * Find the fields that are entering a certain step.
     * 
     * @param stepMeta The step queried
     * @return A row containing the fields (w/ origin) entering the step
     */
    public Row getPrevStepFields(StepMeta stepMeta) throws KettleStepException
    {
        return getPrevStepFields(stepMeta, null);
    }

    /**
     * Find the fields that are entering a certain step.
     * 
     * @param stepMeta The step queried
     * @param monitor The progress monitor for progress dialog. (null if not used!)
     * @return A row containing the fields (w/ origin) entering the step
     */
    public Row getPrevStepFields(StepMeta stepMeta, IProgressMonitor monitor) throws KettleStepException
    {
        Row row = new Row();

        if (stepMeta == null) { return null; }

        log.logDebug(toString(), "From step: " + stepMeta.getName() + ", looking at " + findNrPrevSteps(stepMeta) + " prev. steps.");
        for (int i = 0; i < findNrPrevSteps(stepMeta); i++)
        {
            StepMeta prevStepMeta = findPrevStep(stepMeta, i);

            if (monitor != null)
            {
                monitor.subTask("Checking step [" + prevStepMeta.getName() + "]");
            }

            Row add = getStepFields(prevStepMeta, monitor);
            log.logDebug(toString(), "Found fields to add: " + add.toString());
            if (i == 0) // we expect all input streams to be of the same layout!
            {
                row.addRow(add); // recursive!
            }
            else
            {
                // See if the add fields are not already in the row
                for (int x = 0; x < add.size(); x++)
                {
                    Value v = add.getValue(x);
                    Value s = row.searchValue(v.getName());
                    if (s == null)
                    {
                        row.addValue(v);
                    }
                }
            }
        }
        return row;
    }

    /**
     * Return the fields that are emitted by a step with a certain name
     * 
     * @param stepname The name of the step that's being queried.
     * @param row A row containing the input fields or an empty row if no input is required.
     * @return A Row containing the output fields.
     */
    public Row getThisStepFields(String stepname, Row row) throws KettleStepException
    {
        return getThisStepFields(findStep(stepname), row);
    }

    /**
     * Returns the fields that are emitted by a step
     * 
     * @param stepMeta : The StepMeta object that's being queried
     * @param row : A row containing the input fields or an empty row if no input is required.
     * 
     * @return A Row containing the output fields.
     */
    public Row getThisStepFields(StepMeta stepMeta, Row row) throws KettleStepException
    {
        return getThisStepFields(stepMeta, row, null);
    }

    /**
     * Returns the fields that are emitted by a step
     * 
     * @param stepMeta : The StepMeta object that's being queried
     * @param row : A row containing the input fields or an empty row if no input is required.
     * 
     * @return A Row containing the output fields.
     */
    public Row getThisStepFields(StepMeta stepMeta, Row row, IProgressMonitor monitor) throws KettleStepException
    {
        // Then this one.
        log.logDebug(toString(), "Getting fields from step: " + stepMeta.getName() + ", type=" + stepMeta.getStepID());
        String name = stepMeta.getName();

        if (monitor != null)
        {
            monitor.subTask("Getting fields from step [" + name + "]");
        }

        StepMetaInterface stepint = stepMeta.getStepMetaInterface();
        Row inform = null;
        StepMeta[] lu = getInfoStep(stepMeta);
        if (lu != null)
        {
            inform = getStepFields(lu);
        }
        else
        {
            inform = stepint.getTableFields();
        }

        stepint.getFields(row, name, inform);

        return row;
    }

    /**
     * Returns a string containing an XML representation of the transformation object.
     * 
     * @return String containing an XML representation of the complete transformation.
     */
    /**
     * Saves the transformation information to a repository.
     * 
     * @param rep The repository to save to.
     * @return True if everything went OK, false if an error occured.
     */
    private void saveRepTrans(Repository rep) throws KettleDatabaseException
    {
        // The ID has to be assigned, even when it's a new item...
        rep.insertTransformation(getID(), getName(), readStep == null ? -1 : readStep.getID(), writeStep == null ? -1 : writeStep.getID(),
                inputStep == null ? -1 : inputStep.getID(), outputStep == null ? -1 : outputStep.getID(), updateStep == null ? -1 : updateStep
                        .getID(), logConnection == null ? -1 : logConnection.getID(), logTable, useBatchId, logfieldUsed,
                maxDateConnection == null ? -1 : maxDateConnection.getID(), maxDateTable, maxDateField, maxDateOffset, maxDateDifference,
                modifiedUser, modifiedDate, sizeRowset, directory.getID());
    }

    /**
     * Determine if we should put a replace warning or not for the transformation in a certain repository.
     * 
     * @param rep The repository.
     * @return True if we should show a replace warning, false if not.
     */
    public boolean showReplaceWarning(Repository rep)
    {
        if (getID() < 0)
        {
            try
            {
                if (rep.getTransformationID(getName(), directory.getID()) > 0) return true;
            }
            catch (KettleDatabaseException dbe)
            {
                log.logError(toString(), "Kettle Database error: " + dbe.getMessage());
                return true;
            }
        }
        return false;
    }

    /**
     * Saves the transformation to a repository.
     * 
     * @param rep The repository.
     * @throws KettleException if an error occurrs.
     */
    public void saveRep(Repository rep) throws KettleException
    {
        saveRep(rep, null);
    }

    /**
     * Saves the transformation to a repository.
     * 
     * @param rep The repository.
     * @throws KettleException if an error occurrs.
     */
    public void saveRep(Repository rep, IProgressMonitor monitor) throws KettleException
    {
        try
        {
            rep.lockRepository(); // make sure we're they only one using the repository at the moment

            // Clear attribute id cache
            rep.clearNextIDCounters(); // force repository lookup.

            // Do we have a valid directory?
            if (directory.getID() < 0) { throw new KettleException("Please select a valid directory before saving the transformation!"); }

            int nrWorks = 2 + nrDatabases() + nrNotes() + nrSteps() + nrTransHops();
            if (monitor != null) monitor.beginTask("Saving transformation " + getPathAndName(), nrWorks);
            log.logDebug(toString(), "Saving of transofmation started.");

            // Before we start, make sure we have a valid transformation ID!
            // Two possibilities:
            // 1) We have a ID: keep it
            // 2) We don't have an ID: look it up.
            // If we find a transformation with the same name: ask!
            //
            if (monitor != null) monitor.subTask("Handling old version of transformation (if any)...");
            setID(rep.getTransformationID(getName(), directory.getID()));

            // If no valid id is available in the database, assign one...
            if (getID() <= 0)
            {
                setID(rep.getNextTransformationID());
            }
            else
            {
                // If we have a valid ID, we need to make sure everything is cleared out
                // of the database for this id_transformation, before we put it back in...
                if (monitor != null) monitor.subTask("deleting old version of transformation...");
                log.logDebug(toString(), "deleting old version of transformation...");
                rep.delAllFromTrans(getID());
                log.logDebug(toString(), "Old version of transformation removed.");
            }
            if (monitor != null) monitor.worked(1);

            log.logDebug(toString(), "Saving notes...");
            for (int i = 0; i < nrNotes(); i++)
            {
                if (monitor != null) monitor.subTask("Saving note #" + (i + 1) + "/" + nrNotes());
                NotePadMeta ni = getNote(i);
                ni.saveRep(rep, getID());
                if (ni.getID() > 0) rep.insertTransNote(getID(), ni.getID());
                if (monitor != null) monitor.worked(1);
            }

            log.logDebug(toString(), "Saving database connections...");
            for (int i = 0; i < nrDatabases(); i++)
            {
                if (monitor != null) monitor.subTask("Saving database #" + (i + 1) + "/" + nrDatabases());
                DatabaseMeta ci = getDatabase(i);
                ci.saveRep(rep);
                if (monitor != null) monitor.worked(1);
            }

            // Before saving the steps, make sure we have all the step-types.
            // It is possible that we received another step through a plugin.
            log.logDebug(toString(), "Checking step types...");
            rep.updateStepTypes();

            log.logDebug(toString(), "Saving steps...");
            for (int i = 0; i < nrSteps(); i++)
            {
                if (monitor != null) monitor.subTask("Saving step #" + (i + 1) + "/" + nrSteps());
                StepMeta stepMeta = getStep(i);
                stepMeta.saveRep(rep, getID());

                if (monitor != null) monitor.worked(1);
            }
            rep.closeStepAttributeInsertPreparedStatement();

            log.logDebug(toString(), "Saving hops...");
            for (int i = 0; i < nrTransHops(); i++)
            {
                if (monitor != null) monitor.subTask("Saving hop #" + (i + 1) + "/" + nrTransHops());
                TransHopMeta hi = getTransHop(i);
                hi.saveRep(rep, getID());
                if (monitor != null) monitor.worked(1);
            }

            if (monitor != null) monitor.subTask("finishing...");
            log.logDebug(toString(), "Saving transformation info...");
            saveRepTrans(rep);

            log.logDebug(toString(), "Saving dependencies...");
            for (int i = 0; i < nrDependencies(); i++)
            {
                TransDependency td = getDependency(i);
                td.saveRep(rep, getID());
            }

            log.logDebug(toString(), "Saving finished...");

            // Perform a commit!
            rep.commit();

            clearChanged();
            if (monitor != null) monitor.worked(1);
            if (monitor != null) monitor.done();
        }
        catch (KettleDatabaseException dbe)
        {
            // Oops, rollback!
            rep.rollback();

            log.logError(toString(), "Error saving transformation to repository!" + Const.CR + dbe.getMessage());
            throw new KettleException("Error saving transformation to repository!", dbe);
        }
        finally
        {
            // don't forget to unlock the repository.
            // Normally this is done by the commit / rollback statement, but hey there are some freaky database out
            // there...
            rep.unlockRepository();
        }
    }

    /**
     * Read the database connections in the repository and add them to this transformation if they are not yet present.
     * 
     * @param rep The repository to load the database connections from.
     */
    public void readDatabases(Repository rep)
    {
        try
        {
            long dbids[] = rep.getDatabaseIDs();
            for (int i = 0; i < dbids.length; i++)
            {
                DatabaseMeta ci = new DatabaseMeta(rep, dbids[i]);
                if (ci.getName() != null && indexOfDatabase(ci) < 0)
                {
                    addDatabase(ci);
                    ci.setChanged(false);
                }
            }
            changed_databases = false;
        }
        catch (KettleDatabaseException dbe)
        {
            log.logError(toString(), "Unable to read database IDs from repository: " + dbe.getMessage());
        }
        catch (KettleException ke)
        {
            log.logError(toString(), "Unable to read databases from repository: " + ke.getMessage());
        }
    }

    /**
     * Load the transformation name & other details from a repository.
     * 
     * @param rep The repository to load the details from.
     */
    public void loadRepTrans(Repository rep) throws KettleException
    {
        try
        {
            Row r = rep.getTransformation(getID());

            if (r != null)
            {
                name = r.searchValue("NAME").getString();
                readStep = findStep(steps, r.getInteger("ID_STEP_READ", -1L));
                writeStep = findStep(steps, r.getInteger("ID_STEP_WRITE", -1L));
                inputStep = findStep(steps, r.getInteger("ID_STEP_INPUT", -1L));
                outputStep = findStep(steps, r.getInteger("ID_STEP_OUTPUT", -1L));
                updateStep = findStep(steps, r.getInteger("ID_STEP_UPDATE", -1L));

                logConnection = Const.findDatabase(databases, r.getInteger("ID_DATABASE_LOG", -1L));
                logTable = r.getString("TABLE_NAME_LOG", null);
                useBatchId = r.getBoolean("USE_BATCHID", false);
                logfieldUsed = r.getBoolean("USE_LOGFIELD", false);

                maxDateConnection = Const.findDatabase(databases, r.getInteger("ID_DATABASE_MAXDATE", -1L));
                maxDateTable = r.getString("TABLE_NAME_MAXDATE", null);
                maxDateField = r.getString("FIELD_NAME_MAXDATE", null);
                maxDateOffset = r.getNumber("OFFSET_MAXDATE", 0.0);
                maxDateDifference = r.getNumber("DIFF_MAXDATE", 0.0);

                modifiedUser = r.getString("MODIFIED_USER", null);
                modifiedDate = r.searchValue("MODIFIED_DATE");

                // Optional:
                sizeRowset = Const.ROWS_IN_ROWSET;
                Value val_size_rowset = r.searchValue("SIZE_ROWSET");
                if (val_size_rowset != null && !val_size_rowset.isNull())
                {
                    sizeRowset = (int) val_size_rowset.getInteger();
                }

                long id_directory = r.getInteger("ID_DIRECTORY", -1L);
                if (id_directory >= 0)
                {
                    log.logDetailed(toString(), "ID_DIRECTORY=" + id_directory);
                    // Set right directory...
                    directory = directoryTree.findDirectory(id_directory);
                }
            }
        }
        catch (KettleDatabaseException dbe)
        {
            throw new KettleException("Unable to load transformation information from the repository", dbe);
        }
    }

    /**
     * Read a transformation with a certain name from a repository
     * 
     * @param rep The repository to read from.
     * @param transname The name of the transformation.
     * @param repdir the path to the repository directory
     */
    public TransMeta(Repository rep, String transname, RepositoryDirectory repdir) throws KettleException
    {
        this(rep, transname, repdir, null);
    }

    /**
     * Read a transformation with a certain name from a repository
     * 
     * @param rep The repository to read from.
     * @param transname The name of the transformation.
     * @param repdir the path to the repository directory
     * @param monitor The progress monitor to display the progress of the file-open operation in a dialog
     */
    public TransMeta(Repository rep, String transname, RepositoryDirectory repdir, IProgressMonitor monitor) throws KettleException
    {
        try
        {
            String pathAndName = repdir.isRoot() ? repdir + transname : repdir + RepositoryDirectory.DIRECTORY_SEPARATOR + transname;

            // Clear everything...
            clear();

            setName(transname);
            directory = repdir;
            directoryTree = directory.findRoot();

            // Get the transformation id
            log.logDetailed(toString(), "Looking for the transformation [" + transname + "] in directory [" + directory.getPath() + "]");

            if (monitor != null) monitor.subTask("Reading transformation information");
            setID(rep.getTransformationID(transname, directory.getID()));
            if (monitor != null) monitor.worked(1);

            // If no valid id is available in the database, then give error...
            if (getID() > 0)
            {
                long noteids[] = rep.getTransNoteIDs(getID());
                long stepids[] = rep.getStepIDs(getID());
                long hopids[] = rep.getTransHopIDs(getID());

                int nrWork = 3 + noteids.length + stepids.length + hopids.length;

                if (monitor != null) monitor.beginTask("Loading transformation " + pathAndName, nrWork);

                log.logDetailed(toString(), "Loading transformation [" + getName() + "] from repository...");

                // Load the common database connections

                if (monitor != null) monitor.subTask("Reading the available database from the repository");
                readDatabases(rep);
                if (monitor != null) monitor.worked(1);

                // Load the notes...
                if (monitor != null) monitor.subTask("Reading notes...");
                for (int i = 0; i < noteids.length; i++)
                {
                    NotePadMeta ni = new NotePadMeta(log, rep, noteids[i]);
                    if (indexOfNote(ni) < 0) addNote(ni);
                    if (monitor != null) monitor.worked(1);
                }

                if (monitor != null) monitor.subTask("Reading steps...");
                rep.fillStepAttributesBuffer(getID()); // read all the attributes on one go!
                for (int i = 0; i < stepids.length; i++)
                {
                    log.logDetailed(toString(), "Loading step with ID: " + stepids[i]);
                    if (monitor != null) monitor.subTask("Reading step #" + (i + 1) + "/" + (stepids.length));
                    StepMeta stepMeta = new StepMeta(log, rep, stepids[i], databases, counters);
                    addStep(stepMeta);
                    if (monitor != null) monitor.worked(1);
                }
                if (monitor != null) monitor.worked(1);
                rep.setStepAttributesBuffer(null); // clear the buffer (should be empty anyway)

                // Have all StreamValueLookups, etc. reference the correct source steps...
                for (int i = 0; i < nrSteps(); i++)
                {
                    StepMetaInterface sii = getStep(i).getStepMetaInterface();
                    sii.searchInfoAndTargetSteps(steps);
                }

                if (monitor != null) monitor.subTask("Reading the hops");
                for (int i = 0; i < hopids.length; i++)
                {
                    TransHopMeta hi = new TransHopMeta(rep, hopids[i], steps);
                    addTransHop(hi);
                    if (monitor != null) monitor.worked(1);
                }

                if (monitor != null) monitor.subTask("Loading the transformation details");
                loadRepTrans(rep);
                if (monitor != null) monitor.worked(1);

                if (monitor != null) monitor.subTask("Reading the dependencies");
                long depids[] = rep.getTransDependencyIDs(getID());
                for (int i = 0; i < depids.length; i++)
                {
                    TransDependency td = new TransDependency(rep, depids[i], databases);
                    addDependency(td);
                }
                if (monitor != null) monitor.worked(1);

                if (monitor != null) monitor.subTask("Sorting steps");
                sortSteps();
                if (monitor != null) monitor.worked(1);
                if (monitor != null) monitor.done();
            }
            else
            {
                throw new KettleException("This transformation doesn't exist : " + name);
            }

            log.logDetailed(toString(), "Loaded the transformation [" + transname + "] , directory == null : " + (directory == null));

            log.logDetailed(toString(), "Loaded the transformation [" + transname + "] from the directory [" + directory.getPath() + "]");

        }
        catch (KettleDatabaseException e)
        {
            log.logError(toString(), "A database error occured reading a transformation from the repository" + Const.CR + e);
            throw new KettleException("A database error occured reading a transformation from the repository", e);
        }
        catch (Exception e)
        {
            log.logError(toString(), "A database error occured reading a transformation from the repository" + Const.CR + e);
            throw new KettleException("An error occured reading a transformation from the repository", e);
        }
    }

    /**
     * Find the location of hop
     * 
     * @param hi The hop queried
     * @return The location of the hop, -1 if nothing was found.
     */
    public int indexOfTransHop(TransHopMeta hi)
    {
        return hops.indexOf(hi);
    }

    /**
     * Find the location of step
     * 
     * @param stepMeta The step queried
     * @return The location of the step, -1 if nothing was found.
     */
    public int indexOfStep(StepMeta stepMeta)
    {
        return steps.indexOf(stepMeta);
    }

    /**
     * Find the location of database
     * 
     * @param ci The database queried
     * @return The location of the database, -1 if nothing was found.
     */
    public int indexOfDatabase(DatabaseMeta ci)
    {
        return databases.indexOf(ci);
    }

    /**
     * Find the location of a note
     * 
     * @param ni The note queried
     * @return The location of the note, -1 if nothing was found.
     */
    public int indexOfNote(NotePadMeta ni)
    {
        return notes.indexOf(ni);
    }

    public String getXML()
    {
        String xml = "";

        xml += "<transformation>" + Const.CR;

        xml += "  <info>" + Const.CR;

        xml += "    " + XMLHandler.addTagValue("name", name);
        xml += "    " + XMLHandler.addTagValue("directory", directory != null ? directory.getPath() : RepositoryDirectory.DIRECTORY_SEPARATOR);
        xml += "    <log>" + Const.CR;
        xml += "      " + XMLHandler.addTagValue("read", readStep == null ? "" : readStep.getName());
        xml += "      " + XMLHandler.addTagValue("write", writeStep == null ? "" : writeStep.getName());
        xml += "      " + XMLHandler.addTagValue("input", inputStep == null ? "" : inputStep.getName());
        xml += "      " + XMLHandler.addTagValue("output", outputStep == null ? "" : outputStep.getName());
        xml += "      " + XMLHandler.addTagValue("update", updateStep == null ? "" : updateStep.getName());
        xml += "      " + XMLHandler.addTagValue("connection", logConnection == null ? "" : logConnection.getName());
        xml += "      " + XMLHandler.addTagValue("table", logTable);
        xml += "      " + XMLHandler.addTagValue("use_batchid", useBatchId);
        xml += "      " + XMLHandler.addTagValue("use_logfield", logfieldUsed);
        xml += "      </log>" + Const.CR;
        xml += "    <maxdate>" + Const.CR;
        xml += "      " + XMLHandler.addTagValue("connection", maxDateConnection == null ? "" : maxDateConnection.getName());
        xml += "      " + XMLHandler.addTagValue("table", maxDateTable);
        xml += "      " + XMLHandler.addTagValue("field", maxDateField);
        xml += "      " + XMLHandler.addTagValue("offset", maxDateOffset);
        xml += "      " + XMLHandler.addTagValue("maxdiff", maxDateDifference);
        xml += "      </maxdate>" + Const.CR;
        xml += "    " + XMLHandler.addTagValue("size_rowset", sizeRowset);
        xml += "    " + XMLHandler.addTagValue("sleep_time_empty", sleepTimeEmpty);
        xml += "    " + XMLHandler.addTagValue("sleep_time_full", sleepTimeFull);

        xml += "    <dependencies>" + Const.CR;
        for (int i = 0; i < nrDependencies(); i++)
        {
            TransDependency td = getDependency(i);
            xml += td.getXML();
        }
        xml += "      </dependencies>" + Const.CR;

        xml += "    </info>" + Const.CR;

        xml += "  <notepads>" + Const.CR;
        if (notes != null) for (int i = 0; i < nrNotes(); i++)
        {
            NotePadMeta ni = getNote(i);
            xml += ni.getXML();
        }
        xml += "    </notepads>" + Const.CR;

        for (int i = 0; i < nrDatabases(); i++)
        {
            DatabaseMeta dbMeta = getDatabase(i);
            xml += dbMeta.getXML();
        }

        xml += "  <order>" + Const.CR;
        for (int i = 0; i < nrTransHops(); i++)
        {
            TransHopMeta transHopMeta = getTransHop(i);
            xml += transHopMeta.getXML();
        }
        xml += "  </order>" + Const.CR + Const.CR;

        for (int i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            xml += stepMeta.getXML();
        }

        xml += "</transformation>" + Const.CR;

        return xml;
    }

    /**
     * Parse a file containing the XML that describes the transformation.
     * 
     * @param fname The filename
     */
    public TransMeta(String fname) throws KettleXMLException
    {
        Document doc = XMLHandler.loadXMLFile(fname);
        if (doc != null)
        {
            // Clear the transformation
            clearUndo();
            clear();

            // Root node:
            Node transnode = XMLHandler.getSubNode(doc, "transformation");

            // Load from this node...
            loadXML(transnode);

            setFilename(fname);
        }
        else
        {
            throw new KettleXMLException("Error opening/validating the XML file!");
        }
    }

    /**
     * Load the transformation from an XML node
     * 
     * @param transnode The XML node to parse
     * @throws KettleXMLException
     */
    public TransMeta(Node transnode) throws KettleXMLException
    {
        loadXML(transnode);
    }

    /**
     * Parse a file containing the XML that describes the transformation.
     * 
     * @param transnode The XML node to load from
     * @throws KettleXMLException
     */
    private void loadXML(Node transnode) throws KettleXMLException
    {
        try
        {
            // Clear the transformation
            clearUndo();
            clear();

            // Handle connections
            int n = XMLHandler.countNodes(transnode, "connection");
            log.logDebug(toString(), "We have " + n + " connections...");
            for (int i = 0; i < n; i++)
            {
                log.logDebug(toString(), "Looking at connection #" + i);
                Node nodecon = XMLHandler.getSubNodeByNr(transnode, "connection", i);

                DatabaseMeta dbcon = new DatabaseMeta(nodecon);

                DatabaseMeta exist = findDatabase(dbcon.getName());
                if (exist == null)
                {
                    addDatabase(dbcon);
                }
                else
                {
                    int idx = indexOfDatabase(exist);
                    removeDatabase(idx);
                    addDatabase(idx, dbcon);
                }
            }

            // Read the notes...
            Node notepadsnode = XMLHandler.getSubNode(transnode, "notepads");
            int nrnotes = XMLHandler.countNodes(notepadsnode, "notepad");
            for (int i = 0; i < nrnotes; i++)
            {
                Node notepadnode = XMLHandler.getSubNodeByNr(notepadsnode, "notepad", i);
                NotePadMeta ni = new NotePadMeta(notepadnode);
                notes.add(ni);
            }

            // Handle Steps
            int s = XMLHandler.countNodes(transnode, "step");

            log.logDebug(toString(), "Reading " + s + " steps...");
            for (int i = 0; i < s; i++)
            {
                Node stepnode = XMLHandler.getSubNodeByNr(transnode, "step", i);

                log.logDebug(toString(), "Looking at step #" + i);
                StepMeta inf = new StepMeta(log, stepnode, databases, counters);
                addStep(inf);
            }

            // Have all StreamValueLookups, etc. reference the correct source steps...
            for (int i = 0; i < nrSteps(); i++)
            {
                StepMeta stepMeta = getStep(i);
                StepMetaInterface sii = stepMeta.getStepMetaInterface();
                if (sii != null) sii.searchInfoAndTargetSteps(steps);
            }

            // Handle Hops
            Node ordernode = XMLHandler.getSubNode(transnode, "order");
            n = XMLHandler.countNodes(ordernode, "hop");

            log.logDebug(toString(), "We have " + n + " hops...");
            for (int i = 0; i < n; i++)
            {
                log.logDebug(toString(), "Looking at hop #" + i);
                Node hopnode = XMLHandler.getSubNodeByNr(ordernode, "hop", i);

                TransHopMeta hopinf = new TransHopMeta(hopnode, steps);
                addTransHop(hopinf);
            }

            //
            // get transformation info:
            //
            Node infonode = XMLHandler.getSubNode(transnode, "info");

            // Name
            name = XMLHandler.getTagValue(infonode, "name");

            /*
             * Directory String directoryPath = XMLHandler.getTagValue(infonode, "directory");
             */

            // Logging method...
            readStep = findStep(XMLHandler.getTagValue(infonode, "log", "read"));
            writeStep = findStep(XMLHandler.getTagValue(infonode, "log", "write"));
            inputStep = findStep(XMLHandler.getTagValue(infonode, "log", "input"));
            outputStep = findStep(XMLHandler.getTagValue(infonode, "log", "output"));
            updateStep = findStep(XMLHandler.getTagValue(infonode, "log", "update"));
            String logcon = XMLHandler.getTagValue(infonode, "log", "connection");
            logConnection = findDatabase(logcon);
            logTable = XMLHandler.getTagValue(infonode, "log", "table");
            useBatchId = "Y".equalsIgnoreCase(XMLHandler.getTagValue(infonode, "log", "use_batchid"));

            // Maxdate range options...
            String maxdatcon = XMLHandler.getTagValue(infonode, "maxdate", "connection");
            maxDateConnection = findDatabase(maxdatcon);
            maxDateTable = XMLHandler.getTagValue(infonode, "maxdate", "table");
            maxDateField = XMLHandler.getTagValue(infonode, "maxdate", "field");
            String offset = XMLHandler.getTagValue(infonode, "maxdate", "offset");
            maxDateOffset = Const.toDouble(offset, 0.0);
            String mdiff = XMLHandler.getTagValue(infonode, "maxdate", "maxdiff");
            maxDateDifference = Const.toDouble(mdiff, 0.0);

            // Check the dependencies as far as dates are concerned...
            // We calculate BEFORE we run the MAX of these dates
            // If the date is larger then enddate, startdate is set to MIN_DATE
            // 
            Node depsnode = XMLHandler.getSubNode(infonode, "dependencies");
            int deps = XMLHandler.countNodes(depsnode, "dependency");

            for (int i = 0; i < deps; i++)
            {
                Node depnode = XMLHandler.getSubNodeByNr(depsnode, "dependency", i);

                TransDependency td = new TransDependency(depnode, databases);
                if (td.getDatabase() != null && td.getFieldname() != null)
                {
                    addDependency(td);
                }
            }

            String srowset = XMLHandler.getTagValue(infonode, "size_rowset");
            sizeRowset = Const.toInt(srowset, Const.ROWS_IN_ROWSET);
            sleepTimeEmpty = Const.toInt(XMLHandler.getTagValue(infonode, "sleep_time_empty"), Const.SLEEP_EMPTY_NANOS);
            sleepTimeFull  = Const.toInt(XMLHandler.getTagValue(infonode, "sleep_time_full"), Const.SLEEP_FULL_NANOS);

            log.logDebug(toString(), "nr of steps read : " + nrSteps());
            log.logDebug(toString(), "nr of hops  read : " + nrTransHops());

            sortSteps();
        }
        catch (KettleXMLException xe)
        {
            throw new KettleXMLException("Error reading transformation from XML file", xe);
        }

    }

    /**
     * Gives you an ArrayList of all the steps that are at least used in one active hop. These steps will be used to
     * execute the transformation. The others will not be executed.
     * 
     * @param all Set to true if you want to get ALL the steps from the transformation.
     * @return A ArrayList of steps
     */
    public ArrayList getTransHopSteps(boolean all)
    {
        ArrayList st = new ArrayList();
        int idx;

        for (int x = 0; x < nrTransHops(); x++)
        {
            TransHopMeta hi = getTransHop(x);
            if (hi.isEnabled() || all)
            {
                idx = st.indexOf(hi.getFromStep()); // FROM
                if (idx < 0) st.add(hi.getFromStep());

                idx = st.indexOf(hi.getToStep()); // TO
                if (idx < 0) st.add(hi.getToStep());
            }
        }

        // Also, add the steps that need to be painted, but are not part of a hop
        for (int x = 0; x < nrSteps(); x++)
        {
            StepMeta stepMeta = getStep(x);
            if (stepMeta.isDrawn() && !isStepUsedInTransHops(stepMeta))
            {
                st.add(stepMeta);
            }
        }

        return st;
    }

    /**
     * Get the name of the transformation
     * 
     * @return The name of the transformation
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the name of the transformation.
     * 
     * @param n The new name of the transformation
     */
    public void setName(String n)
    {
        name = n;
    }

    /**
     * Get the filename (if any) of the transformation
     * 
     * @return The filename of the transformation.
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * Set the filename of the transformation
     * 
     * @param fname The new filename of the transformation.
     */
    public void setFilename(String fname)
    {
        filename = fname;
    }

    /**
     * Determines if a step has been used in a hop or not.
     * 
     * @param stepMeta The step queried.
     * @return True if a step is used in a hop (active or not), false if this is not the case.
     */
    public boolean isStepUsedInTransHops(StepMeta stepMeta)
    {
        TransHopMeta fr = findTransHopFrom(stepMeta);
        TransHopMeta to = findTransHopTo(stepMeta);
        if (fr != null || to != null) return true;
        return false;
    }

    /**
     * Mark the transformation as being changed.
     * 
     */
    public void setChanged()
    {
        setChanged(true);
    }

    /**
     * Sets the changed parameter of the transformation.
     * 
     * @param ch True if you want to mark the transformation as changed, false if not.
     */
    public void setChanged(boolean ch)
    {
        changed = ch;
    }

    /**
     * Clears the different changed flags of the transformation.
     * 
     */
    public void clearChanged()
    {
        changed = false;
        changed_steps = false;
        changed_databases = false;
        changed_hops = false;
        changed_notes = false;

        for (int i = 0; i < nrSteps(); i++)
        {
            getStep(i).setChanged(false);
        }
        for (int i = 0; i < nrDatabases(); i++)
        {
            getDatabase(i).setChanged(false);
        }
        for (int i = 0; i < nrTransHops(); i++)
        {
            getTransHop(i).setChanged(false);
        }
        for (int i = 0; i < nrNotes(); i++)
        {
            getNote(i).setChanged(false);
        }
    }

    /**
     * Checks whether or not the connections have changed.
     * 
     * @return True if the connections have been changed.
     */
    public boolean haveConnectionsChanged()
    {
        if (changed_databases) return true;

        for (int i = 0; i < nrDatabases(); i++)
        {
            DatabaseMeta ci = getDatabase(i);
            if (ci.hasChanged()) return true;
        }
        return false;
    }

    /**
     * Checks whether or not the steps have changed.
     * 
     * @return True if the connections have been changed.
     */
    public boolean haveStepsChanged()
    {
        if (changed_steps) return true;

        for (int i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            if (stepMeta.hasChanged()) return true;
        }
        return false;
    }

    /**
     * Checks whether or not any of the hops have been changed.
     * 
     * @return True if a hop has been changed.
     */
    public boolean haveHopsChanged()
    {
        if (changed_hops) return true;

        for (int i = 0; i < nrTransHops(); i++)
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.hasChanged()) return true;
        }
        return false;
    }

    /**
     * Checks whether or not any of the notes have been changed.
     * 
     * @return True if the notes have been changed.
     */
    public boolean haveNotesChanged()
    {
        if (changed_notes) return true;

        for (int i = 0; i < nrNotes(); i++)
        {
            NotePadMeta ni = getNote(i);
            if (ni.hasChanged()) return true;
        }

        return false;
    }

    /**
     * Checks whether or not the transformation has changed.
     * 
     * @return True if the transformation has changed.
     */
    public boolean hasChanged()
    {
        if (changed) return true;

        if (haveConnectionsChanged()) return true;
        if (haveStepsChanged()) return true;
        if (haveHopsChanged()) return true;
        if (haveNotesChanged()) return true;

        return false;
    }

    /**
     * See if there are any loops in the transformation, starting at the indicated step. This works by looking at all
     * the previous steps. If you keep going backward and find the step, there is a loop. Both the informational and the
     * normal steps need to be checked for loops!
     * 
     * @param stepMeta The step position to start looking
     * 
     * @return True if a loop has been found, false if no loop is found.
     */
    public boolean hasLoop(StepMeta stepMeta)
    {
        return hasLoop(stepMeta, null, true) || hasLoop(stepMeta, null, false);
    }

    /**
     * See if there are any loops in the transformation, starting at the indicated step. This works by looking at all
     * the previous steps. If you keep going backward and find the orginal step again, there is a loop.
     * 
     * @param stepMeta The step position to start looking
     * @param lookup The original step when wandering around the transformation.
     * @param info Check the informational steps or not.
     * 
     * @return True if a loop has been found, false if no loop is found.
     */
    public boolean hasLoop(StepMeta stepMeta, StepMeta lookup, boolean info)
    {
        int nr = findNrPrevSteps(stepMeta, info);
        for (int i = 0; i < nr; i++)
        {
            StepMeta prevStepMeta = findPrevStep(stepMeta, i, info);
            if (prevStepMeta != null)
            {
                if (prevStepMeta.equals(stepMeta)) return true;
                if (prevStepMeta.equals(lookup)) return true;
                if (hasLoop(prevStepMeta, lookup == null ? stepMeta : lookup, info)) return true;
            }
        }
        return false;
    }

    /**
     * Mark all steps in the transformation as selected.
     * 
     */
    public void selectAll()
    {
        int i;
        for (i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            stepMeta.setSelected(true);
        }
        for (i = 0; i < nrNotes(); i++)
        {
            NotePadMeta ni = getNote(i);
            ni.setSelected(true);
        }
    }

    /**
     * Clear the selection of all steps.
     * 
     */
    public void unselectAll()
    {
        int i;
        for (i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            stepMeta.setSelected(false);
        }
        for (i = 0; i < nrNotes(); i++)
        {
            NotePadMeta ni = getNote(i);
            ni.setSelected(false);
        }
    }

    /**
     * Count the number of selected steps in this transformation
     * 
     * @return The number of selected steps.
     */
    public int nrSelectedSteps()
    {
        int i, count;
        count = 0;
        for (i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            if (stepMeta.isSelected()) count++;
        }
        return count;
    }

    /**
     * Get the selected step at a certain location
     * 
     * @param nr The location
     * @return The selected step
     */
    public StepMeta getSelectedStep(int nr)
    {
        int i, count;
        count = 0;
        for (i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            if (stepMeta.isSelected())
            {
                if (nr == count) return stepMeta;
                count++;
            }
        }
        return null;
    }

    /**
     * Count the number of selected notes in this transformation
     * 
     * @return The number of selected notes.
     */
    public int nrSelectedNotes()
    {
        int i, count;
        count = 0;
        for (i = 0; i < nrNotes(); i++)
        {
            NotePadMeta ni = getNote(i);
            if (ni.isSelected()) count++;
        }
        return count;
    }

    /**
     * Get the selected note at a certain index
     * 
     * @param nr The index
     * @return The selected note
     */
    public NotePadMeta getSelectedNote(int nr)
    {
        int i, count;
        count = 0;
        for (i = 0; i < nrNotes(); i++)
        {
            NotePadMeta ni = getNote(i);
            if (ni.isSelected())
            {
                if (nr == count) return ni;
                count++;
            }
        }
        return null;
    }

    /**
     * Select all the steps in a certain (screen) rectangle
     * 
     * @param rect The selection area as a rectangle
     */
    public void selectInRect(Rectangle rect)
    {
        for (int i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            Point a = stepMeta.getLocation();
            if (rect.contains(a)) stepMeta.setSelected(true);
        }

        for (int i = 0; i < nrNotes(); i++)
        {
            NotePadMeta ni = getNote(i);
            Point a = ni.getLocation();
            Point b = new Point(a.x + ni.width, a.y + ni.height);
            if (rect.contains(a) && rect.contains(b)) ni.setSelected(true);
        }
    }

    /**
     * Get an array of all the selected step and note locations
     * 
     * @return The selected step and notes locations.
     */
    public Point[] getSelectedStepLocations()
    {
        ArrayList points = new ArrayList();

        for (int i = 0; i < nrSelectedSteps(); i++)
        {
            StepMeta stepMeta = getSelectedStep(i);
            Point p = stepMeta.getLocation();
            points.add(new Point(p.x, p.y)); // explicit copy of location
        }

        return (Point[]) points.toArray(new Point[points.size()]);
    }

    /**
     * Get an array of all the selected step and note locations
     * 
     * @return The selected step and notes locations.
     */
    public Point[] getSelectedNoteLocations()
    {
        ArrayList points = new ArrayList();

        for (int i = 0; i < nrSelectedNotes(); i++)
        {
            NotePadMeta ni = getSelectedNote(i);
            Point p = ni.getLocation();
            points.add(new Point(p.x, p.y)); // explicit copy of location
        }

        return (Point[]) points.toArray(new Point[points.size()]);
    }

    /**
     * Get an array of all the selected steps
     * 
     * @return An array of all the selected steps.
     */
    public StepMeta[] getSelectedSteps()
    {
        int sels = nrSelectedSteps();
        if (sels == 0) return null;

        StepMeta retval[] = new StepMeta[sels];
        for (int i = 0; i < sels; i++)
        {
            StepMeta stepMeta = getSelectedStep(i);
            retval[i] = stepMeta;

        }
        return retval;
    }

    /**
     * Get an array of all the selected notes
     * 
     * @return An array of all the selected notes.
     */
    public NotePadMeta[] getSelectedNotes()
    {
        int sels = nrSelectedNotes();
        if (sels == 0) return null;

        NotePadMeta retval[] = new NotePadMeta[sels];
        for (int i = 0; i < sels; i++)
        {
            NotePadMeta si = getSelectedNote(i);
            retval[i] = si;

        }
        return retval;
    }

    /**
     * Get an array of all the selected step names
     * 
     * @return An array of all the selected step names.
     */
    public String[] getSelectedStepNames()
    {
        int sels = nrSelectedSteps();
        if (sels == 0) return null;

        String retval[] = new String[sels];
        for (int i = 0; i < sels; i++)
        {
            StepMeta stepMeta = getSelectedStep(i);
            retval[i] = stepMeta.getName();
        }
        return retval;
    }

    /**
     * Get an array of the locations of an array of steps
     * 
     * @param steps An array of steps
     * @return an array of the locations of an array of steps
     */
    public int[] getStepIndexes(StepMeta steps[])
    {
        int retval[] = new int[steps.length];

        for (int i = 0; i < steps.length; i++)
            retval[i] = indexOfStep(steps[i]);

        return retval;
    }

    /**
     * Get an array of the locations of an array of notes
     * 
     * @param notes An array of notes
     * @return an array of the locations of an array of notes
     */
    public int[] getNoteIndexes(NotePadMeta notes[])
    {
        int retval[] = new int[notes.length];

        for (int i = 0; i < notes.length; i++)
            retval[i] = indexOfNote(notes[i]);

        return retval;
    }

    /**
     * Get the maximum number of undo operations possible
     * 
     * @return The maximum number of undo operations that are allowed.
     */
    public int getMaxUndo()
    {
        return max_undo;
    }

    /**
     * Sets the maximum number of undo operations that are allowed.
     * 
     * @param mu The maximum number of undo operations that are allowed.
     */
    public void setMaxUndo(int mu)
    {
        max_undo = mu;
        while (undo.size() > mu && undo.size() > 0)
            undo.remove(0);
    }

    /**
     * Add an undo operation to the undo list
     * 
     * @param from array of objects representing the old state
     * @param to array of objectes representing the new state
     * @param pos An array of object locations
     * @param prev An array of points representing the old positions
     * @param curr An array of points representing the new positions
     * @param type_of_change The type of change that's being done to the transformation.
     * @param nextAlso indicates that the next undo operation needs to follow this one.
     */
    public void addUndo(Object from[], Object to[], int pos[], Point prev[], Point curr[], int type_of_change, boolean nextAlso)
    {
        // First clean up after the current position.
        // Example: position at 3, size=5
        // 012345
        // ^
        // remove 34
        // Add 4
        // 01234

        while (undo.size() > undo_position + 1 && undo.size() > 0)
        {
            int last = undo.size() - 1;
            undo.remove(last);
        }

        TransAction ta = new TransAction();
        switch (type_of_change)
        {
        case TYPE_UNDO_CHANGE:
            ta.setChanged(from, to, pos);
            break;
        case TYPE_UNDO_DELETE:
            ta.setDelete(from, pos);
            break;
        case TYPE_UNDO_NEW:
            ta.setNew(from, pos);
            break;
        case TYPE_UNDO_POSITION:
            ta.setPosition(from, pos, prev, curr);
            break;
        }
        ta.setNextAlso(nextAlso);
        undo.add(ta);
        undo_position++;

        if (undo.size() > max_undo)
        {
            undo.remove(0);
            undo_position--;
        }
    }

    /**
     * Get the previous undo operation and change the undo pointer
     * 
     * @return The undo transaction to be performed.
     */
    public TransAction previousUndo()
    {
        if (undo.size() == 0 || undo_position < 0) return null; // No undo left!

        TransAction retval = (TransAction) undo.get(undo_position);

        undo_position--;

        return retval;
    }

    /**
     * View current undo, don't change undo position
     * 
     * @return The current undo transaction
     */
    public TransAction viewThisUndo()
    {
        if (undo.size() == 0 || undo_position < 0) return null; // No undo left!

        TransAction retval = (TransAction) undo.get(undo_position);

        return retval;
    }

    /**
     * View previous undo, don't change undo position
     * 
     * @return The previous undo transaction
     */
    public TransAction viewPreviousUndo()
    {
        if (undo.size() == 0 || undo_position - 1 < 0) return null; // No undo left!

        TransAction retval = (TransAction) undo.get(undo_position - 1);

        return retval;
    }

    /**
     * Get the next undo transaction on the list. Change the undo pointer.
     * 
     * @return The next undo transaction (for redo)
     */
    public TransAction nextUndo()
    {
        int size = undo.size();
        if (size == 0 || undo_position >= size - 1) return null; // no redo left...

        undo_position++;

        TransAction retval = (TransAction) undo.get(undo_position);

        return retval;
    }

    /**
     * Get the next undo transaction on the list.
     * 
     * @return The next undo transaction (for redo)
     */
    public TransAction viewNextUndo()
    {
        int size = undo.size();
        if (size == 0 || undo_position >= size - 1) return null; // no redo left...

        TransAction retval = (TransAction) undo.get(undo_position + 1);

        return retval;
    }

    /**
     * Get the maximum size of the canvas by calculating the maximum location of a step
     * 
     * @return Maximum coordinate of a step in the transformation + (100,100) for safety.
     */
    public Point getMaximum()
    {
        int maxx = 0, maxy = 0;
        for (int i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            Point loc = stepMeta.getLocation();
            if (loc.x > maxx) maxx = loc.x;
            if (loc.y > maxy) maxy = loc.y;
        }
        for (int i = 0; i < nrNotes(); i++)
        {
            NotePadMeta notePadMeta = getNote(i);
            Point loc = notePadMeta.getLocation();
            if (loc.x + notePadMeta.width > maxx) maxx = loc.x + notePadMeta.width;
            if (loc.y + notePadMeta.height > maxy) maxy = loc.y + notePadMeta.height;
        }

        return new Point(maxx + 100, maxy + 100);
    }

    /**
     * Get the names of all the steps.
     * 
     * @return An array of step names.
     */
    public String[] getStepNames()
    {
        String retval[] = new String[nrSteps()];

        for (int i = 0; i < nrSteps(); i++)
            retval[i] = getStep(i).getName();

        return retval;
    }

    /**
     * Get all the steps in an array.
     * 
     * @return An array of all the steps in the transformation.
     */
    public StepMeta[] getStepsArray()
    {
        StepMeta retval[] = new StepMeta[nrSteps()];

        for (int i = 0; i < nrSteps(); i++)
            retval[i] = getStep(i);

        return retval;
    }

    /**
     * Find a step with the ID in a given ArrayList of steps
     * 
     * @param steps The ArrayList of steps
     * @param id The ID of the step
     * @return The step if it was found, null if nothing was found
     */
    public static final StepMeta findStep(ArrayList steps, long id)
    {
        if (steps == null) return null;

        for (int i = 0; i < steps.size(); i++)
        {
            StepMeta stepMeta = (StepMeta) steps.get(i);
            if (stepMeta.getID() == id) return stepMeta;
        }
        return null;
    }

    /**
     * Find a step with its name in a given ArrayList of steps
     * 
     * @param steps The ArrayList of steps
     * @param stepname The name of the step
     * @return The step if it was found, null if nothing was found
     */
    public static final StepMeta findStep(ArrayList steps, String stepname)
    {
        if (steps == null) return null;

        for (int i = 0; i < steps.size(); i++)
        {
            StepMeta stepMeta = (StepMeta) steps.get(i);
            if (stepMeta.getName().equalsIgnoreCase(stepname)) return stepMeta;
        }
        return null;
    }

    /**
     * Look in the transformation and see if we can find a step in a previous location starting somewhere.
     * 
     * @param startStep The starting step
     * @param stepToFind The step to look for backward in the transformation
     * @return true if we can find the step in an earlier location in the transformation.
     */
    public boolean findPrevious(StepMeta startStep, StepMeta stepToFind)
    {
        // Normal steps
        int nrPrevious = findNrPrevSteps(startStep, false);
        for (int i = 0; i < nrPrevious; i++)
        {
            StepMeta stepMeta = findPrevStep(startStep, i, false);
            if (stepMeta.equals(stepToFind)) return true;

            boolean found = findPrevious(stepMeta, stepToFind); // Look further back in the tree.
            if (found) return true;
        }

        // Info steps
        nrPrevious = findNrPrevSteps(startStep, true);
        for (int i = 0; i < nrPrevious; i++)
        {
            StepMeta stepMeta = findPrevStep(startStep, i, true);
            if (stepMeta.equals(stepToFind)) return true;

            boolean found = findPrevious(stepMeta, stepToFind); // Look further back in the tree.
            if (found) return true;
        }

        return false;
    }

    /**
     * Put the steps in alfabetical order.
     */
    public void sortSteps()
    {
        try
        {
            Const.quickSort(steps);
        }
        catch (Exception e)
        {
            System.out.println("Exception sorting steps: " + e);
            e.printStackTrace();
        }
    }

    public void sortHops()
    {
        Const.quickSort(hops);
    }

    /**
     * Put the steps in a more natural order: from start to finish. For the moment, we ignore splits and joins. Splits
     * and joins can't be listed sequentially in any case!
     * 
     */
    public void sortStepsNatural()
    {
        // Loop over the steps...
        for (int j = 0; j < nrSteps(); j++)
        {
            // Buble sort: we need to do this several times...
            for (int i = 0; i < nrSteps() - 1; i++)
            {
                StepMeta one = getStep(i);
                StepMeta two = getStep(i + 1);

                if (!findPrevious(two, one))
                {
                    setStep(i + 1, one);
                    setStep(i, two);
                }
            }
        }
    }

    /**
     * Sort the hops in a natural way: from beginning to end
     */
    public void sortHopsNatural()
    {
        // Loop over the hops...
        for (int j = 0; j < nrTransHops(); j++)
        {
            // Buble sort: we need to do this several times...
            for (int i = 0; i < nrTransHops() - 1; i++)
            {
                TransHopMeta one = getTransHop(i);
                TransHopMeta two = getTransHop(i + 1);

                StepMeta a = two.getFromStep();
                StepMeta b = one.getToStep();

                if (!findPrevious(a, b) && !a.equals(b))
                {
                    setTransHop(i + 1, one);
                    setTransHop(i, two);
                }
            }
        }
    }

    /**
     * This procedure determines the impact of the different steps in a transformation on databases, tables and field.
     * 
     * @param impact An ArrayList of DatabaseImpact objects.
     * 
     */
    public void analyseImpact(ArrayList impact, IProgressMonitor monitor) throws KettleStepException
    {
        if (monitor != null)
        {
            monitor.beginTask("Determining impact...", nrSteps());
        }
        boolean stop = false;
        for (int i = 0; i < nrSteps() && !stop; i++)
        {
            if (monitor != null) monitor.subTask("Looking at step #" + (i + 1) + "/" + nrSteps());
            StepMeta stepMeta = getStep(i);

            Row prev = getPrevStepFields(stepMeta);
            StepMetaInterface stepint = stepMeta.getStepMetaInterface();
            Row inform = null;
            StepMeta[] lu = getInfoStep(stepMeta);
            if (lu != null)
            {
                inform = getStepFields(lu);
            }
            else
            {
                inform = stepint.getTableFields();
            }

            stepint.analyseImpact(impact, this, stepMeta, prev, null, null, inform);

            if (monitor != null)
            {
                monitor.worked(1);
                stop = monitor.isCanceled();
            }
        }

        if (monitor != null) monitor.done();
    }

    /**
     * Proposes an alternative stepname when the original already exists...
     * 
     * @param stepname The stepname to find an alternative for..
     * @return The alternative stepname.
     */
    public String getAlternativeStepname(String stepname)
    {
        String newname = stepname;
        StepMeta stepMeta = findStep(newname);
        int nr = 1;
        while (stepMeta != null)
        {
            nr++;
            newname = stepname + " " + nr;
            stepMeta = findStep(newname);
        }

        return newname;
    }

    /**
     * Builds a list of all the SQL statements that this transformation needs in order to work properly.
     * 
     * @return An ArrayList of SQLStatement objects.
     */
    public ArrayList getSQLStatements() throws KettleStepException
    {
        return getSQLStatements(null);
    }

    /**
     * Builds a list of all the SQL statements that this transformation needs in order to work properly.
     * 
     * @return An ArrayList of SQLStatement objects.
     */
    public ArrayList getSQLStatements(IProgressMonitor monitor) throws KettleStepException
    {
        if (monitor != null) monitor.beginTask("Getting the SQL needed for this transformation...", nrSteps() + 1);
        ArrayList stats = new ArrayList();

        for (int i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            if (monitor != null) monitor.subTask("Getting SQL statements for step [" + stepMeta + "]");
            Row prev = getPrevStepFields(stepMeta);
            SQLStatement sql = stepMeta.getStepMetaInterface().getSQLStatements(this, stepMeta, prev);
            if (sql.getSQL() != null || sql.hasError())
            {
                stats.add(sql);
            }
            if (monitor != null) monitor.worked(1);
        }

        // Also check the sql for the logtable...
        if (monitor != null) monitor.subTask("Getting SQL statements for the transformation (logtable, etc.)");
        if (logConnection != null && logTable != null && logTable.length() > 0)
        {
            Database db = new Database(logConnection);
            try
            {
                db.connect();
                Row fields = Database.getTransLogrecordFields(useBatchId, logfieldUsed);
                String sql = db.getDDL(logTable, fields);
                if (sql != null && sql.length() > 0)
                {
                    SQLStatement stat = new SQLStatement("<this transformation>", logConnection, sql);
                    stats.add(stat);
                }
            }
            catch (KettleDatabaseException dbe)
            {
                SQLStatement stat = new SQLStatement("<this transformation>", logConnection, null);
                stat.setError("Error obtaining transformation log table info: " + dbe.getMessage());
                stats.add(stat);
            }
            finally
            {
                db.disconnect();
            }
        }
        if (monitor != null) monitor.worked(1);
        if (monitor != null) monitor.done();

        return stats;
    }

    /**
     * Get the SQL statements, needed to run this transformation, as one String.
     * 
     * @return the SQL statements needed to run this transformation.
     */
    public String getSQLStatementsString() throws KettleStepException
    {
        String sql = "";
        ArrayList stats = getSQLStatements();
        for (int i = 0; i < stats.size(); i++)
        {
            SQLStatement stat = (SQLStatement) stats.get(i);
            if (!stat.hasError() && stat.hasSQL())
            {
                sql += stat.getSQL();
            }
        }

        return sql;
    }

    /**
     * Checks all the steps and fills a List of (CheckResult) remarks.
     * 
     * @param remarks The remarks list to add to.
     * @param only_selected Check only the selected steps.
     * @param monitor The progress monitor to use, null if not used
     */
    public void checkSteps(ArrayList remarks, boolean only_selected, IProgressMonitor monitor)
    {
        try
        {
            remarks.clear(); // Start with a clean slate...

            Hashtable values = new Hashtable();
            String stepnames[];
            StepMeta steps[];
            if (!only_selected || nrSelectedSteps() == 0)
            {
                stepnames = getStepNames();
                steps = getStepsArray();
            }
            else
            {
                stepnames = getSelectedStepNames();
                steps = getSelectedSteps();
            }

            boolean stop_checking = false;

            if (monitor != null) monitor.beginTask("Verifying this transformation...", steps.length + 2);

            for (int i = 0; i < steps.length && !stop_checking; i++)
            {
                if (monitor != null) monitor.subTask("Verifying step [" + stepnames[i] + "]");

                StepMeta stepMeta = steps[i];

                int nrinfo = findNrInfoSteps(stepMeta);
                StepMeta[] infostep = null;
                if (nrinfo > 0)
                {
                    infostep = getInfoStep(stepMeta);
                }

                Row info = null;
                if (infostep != null)
                {
                    try
                    {
                        info = getStepFields(infostep);
                    }
                    catch (KettleStepException kse)
                    {
                        info = null;
                        CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "An error occurred getting step info fields for step ["
                                + stepMeta + "] :" + Const.CR + kse.getMessage(), stepMeta);
                        remarks.add(cr);
                    }
                }

                // The previous fields from non-informative steps:
                Row prev = null;
                try
                {
                    prev = getPrevStepFields(stepMeta);
                }
                catch (KettleStepException kse)
                {
                    CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "An error occurred getting input fields for step [" + stepMeta
                            + "] :" + Const.CR + kse.getMessage(), stepMeta);
                    remarks.add(cr);
                    // This is a severe error: stop checking...
                    // Otherwise we wind up checking time & time again because nothing gets put in the database
                    // cache, the timeout of certain databases is very long... (Oracle)
                    stop_checking = true;
                }

                if (isStepUsedInTransHops(stepMeta))
                {
                    // Get the input & output steps!
                    // Copy to arrays:
                    String input[] = getPrevStepNames(stepMeta);
                    String output[] = getPrevStepNames(stepMeta);

                    // Check step specific info...
                    stepMeta.check(remarks, prev, input, output, info);

                    // See if illegal characters etc. were used in field-names...
                    if (prev != null)
                    {
                        for (int x = 0; x < prev.size(); x++)
                        {
                            Value v = prev.getValue(x);
                            String name = v.getName();
                            if (name == null)
                                values.put(v, "Field name is empty.");
                            else
                                if (name.indexOf(' ') >= 0)
                                    values.put(v, "Field name contains one or more spaces.  (database unfriendly!)");
                                else
                                {
                                    char list[] = new char[] { '.', ',', '-', '/', '+', '*', '\'', '\t', '"', '|', '@', '(', ')', '{', '}', '!', '^' };
                                    for (int c = 0; c < list.length; c++)
                                    {
                                        if (name.indexOf(list[c]) >= 0)
                                            values.put(v, "Field name contains one or more " + list[c] + "  (database unfriendly!)");
                                    }
                                }
                        }

                        // Check if 2 steps with the same name are entering the step...
                        if (prev.size() > 1)
                        {
                            String fieldNames[] = prev.getFieldNames();
                            String sortedNames[] = Const.sortStrings(fieldNames);

                            String prevName = sortedNames[0];
                            for (int x = 1; x < sortedNames.length; x++)
                            {
                                // Checking for doubles
                                if (prevName.equalsIgnoreCase(sortedNames[x]))
                                {
                                    // Give a warning!!
                                    CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING,
                                            "I found input fields that have the same name [" + prevName + "]", stepMeta);
                                    remarks.add(cr);
                                }
                                else
                                {
                                    prevName = sortedNames[x];
                                }
                            }
                        }
                    }
                    else
                    {
                        CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Can't find previous fields for step: " + stepMeta.getName(),
                                stepMeta);
                        remarks.add(cr);
                    }
                }
                else
                {
                    CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, "This step is not used in the transformation.", stepMeta);
                    remarks.add(cr);
                }

                if (monitor != null)
                {
                    monitor.worked(1); // progress bar...
                    if (monitor.isCanceled()) stop_checking = true;
                }
            }

            // Also, check the logging table of the transformation...
            if (monitor == null || !monitor.isCanceled())
            {
                if (monitor != null) monitor.subTask("Checking the logging table...");
                if (getLogConnection() != null)
                {
                    Database logdb = new Database(getLogConnection());
                    try
                    {
                        logdb.connect();
                        CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Transformation logging connection supplied: connecting works",
                                null);
                        remarks.add(cr);

                        if (getLogTable() != null)
                        {
                            if (logdb.checkTableExists(getLogTable()))
                            {
                                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "The logging table [" + getLogTable() + "] exists.", null);
                                remarks.add(cr);

                                Row fields = Database.getTransLogrecordFields(isBatchIdUsed(), isLogfieldUsed());
                                String sql = logdb.getDDL(getLogTable(), fields);
                                if (sql == null || sql.length() == 0)
                                {
                                    cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "The logging table has the correct layout.", null);
                                    remarks.add(cr);
                                }
                                else
                                {
                                    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "The logging table needs some adjustments:" + Const.CR + sql,
                                            null);
                                    remarks.add(cr);
                                }

                            }
                            else
                            {
                                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "The logging table doesn't exist on the logging connection", null);
                                remarks.add(cr);
                            }
                        }
                        else
                        {
                            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "The log table is not specified, the logging connection is", null);
                            remarks.add(cr);
                        }
                    }
                    catch (KettleDatabaseException dbe)
                    {

                    }
                    finally
                    {
                        logdb.disconnect();
                    }
                }
                if (monitor != null) monitor.worked(1);

            }

            if (monitor != null) monitor.subTask("Checking for database unfriendly characters in field names...");
            if (values.size() > 0)
            {
                Enumeration keys = values.keys();
                while (keys.hasMoreElements())
                {
                    Value v = (Value) keys.nextElement();
                    String message = (String) values.get(v);
                    CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, "Field [" + v.getName() + "] : " + message + " in step ["
                            + v.getOrigin() + "]", findStep(v.getOrigin()));
                    remarks.add(cr);
                }
            }
            else
            {
                CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
                        "None of the field names seem to contain spaces or other database unfriendly characters(OK)", null);
                remarks.add(cr);
            }
            if (monitor != null) monitor.worked(1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * @return Returns the resultRows.
     */
    public ArrayList getResultRows()
    {
        return resultRows;
    }

    /**
     * @param resultRows The resultRows to set.
     */
    public void setResultRows(ArrayList resultRows)
    {
        this.resultRows = resultRows;
    }

    /**
     * @return Returns the sourceRows.
     */
    public ArrayList getSourceRows()
    {
        return sourceRows;
    }

    /**
     * @param sourceRows The sourceRows to set.
     */
    public void setSourceRows(ArrayList sourceRows)
    {
        this.sourceRows = sourceRows;
    }

    /**
     * @return Returns the directory.
     */
    public RepositoryDirectory getDirectory()
    {
        return directory;
    }

    /**
     * @param directory The directory to set.
     */
    public void setDirectory(RepositoryDirectory directory)
    {
        this.directory = directory;
    }

    /**
     * @return Returns the directoryTree.
     * @deprecated
     */
    public RepositoryDirectory getDirectoryTree()
    {
        return directoryTree;
    }

    /**
     * @param directoryTree The directoryTree to set.
     * @deprecated
     */
    public void setDirectoryTree(RepositoryDirectory directoryTree)
    {
        this.directoryTree = directoryTree;
    }

    /**
     * @return The directory path plus the name of the transformation
     */
    public String getPathAndName()
    {
        if (getDirectory().isRoot())
            return getDirectory().getPath() + getName();
        else
            return getDirectory().getPath() + RepositoryDirectory.DIRECTORY_SEPARATOR + getName();
    }

    /**
     * @return Returns the arguments.
     */
    public String[] getArguments()
    {
        return arguments;
    }

    /**
     * @param arguments The arguments to set.
     */
    public void setArguments(String[] arguments)
    {
        this.arguments = arguments;
    }

    /**
     * @return Returns the counters.
     */
    public Hashtable getCounters()
    {
        return counters;
    }

    /**
     * @param counters The counters to set.
     */
    public void setCounters(Hashtable counters)
    {
        this.counters = counters;
    }

    /**
     * @return Returns the dependencies.
     */
    public ArrayList getDependencies()
    {
        return dependencies;
    }

    /**
     * @param dependencies The dependencies to set.
     */
    public void setDependencies(ArrayList dependencies)
    {
        this.dependencies = dependencies;
    }

    /**
     * @return Returns the id.
     */
    public long getId()
    {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(long id)
    {
        this.id = id;
    }

    /**
     * @return Returns the inputStep.
     */
    public StepMeta getInputStep()
    {
        return inputStep;
    }

    /**
     * @param inputStep The inputStep to set.
     */
    public void setInputStep(StepMeta inputStep)
    {
        this.inputStep = inputStep;
    }

    /**
     * @return Returns the logConnection.
     */
    public DatabaseMeta getLogConnection()
    {
        return logConnection;
    }

    /**
     * @param logConnection The logConnection to set.
     */
    public void setLogConnection(DatabaseMeta logConnection)
    {
        this.logConnection = logConnection;
    }

    /**
     * @return Returns the logTable.
     */
    public String getLogTable()
    {
        return logTable;
    }

    /**
     * @param logTable The logTable to set.
     */
    public void setLogTable(String logTable)
    {
        this.logTable = logTable;
    }

    /**
     * @return Returns the maxDateConnection.
     */
    public DatabaseMeta getMaxDateConnection()
    {
        return maxDateConnection;
    }

    /**
     * @param maxDateConnection The maxDateConnection to set.
     */
    public void setMaxDateConnection(DatabaseMeta maxDateConnection)
    {
        this.maxDateConnection = maxDateConnection;
    }

    /**
     * @return Returns the maxDateDifference.
     */
    public double getMaxDateDifference()
    {
        return maxDateDifference;
    }

    /**
     * @param maxDateDifference The maxDateDifference to set.
     */
    public void setMaxDateDifference(double maxDateDifference)
    {
        this.maxDateDifference = maxDateDifference;
    }

    /**
     * @return Returns the maxDateField.
     */
    public String getMaxDateField()
    {
        return maxDateField;
    }

    /**
     * @param maxDateField The maxDateField to set.
     */
    public void setMaxDateField(String maxDateField)
    {
        this.maxDateField = maxDateField;
    }

    /**
     * @return Returns the maxDateOffset.
     */
    public double getMaxDateOffset()
    {
        return maxDateOffset;
    }

    /**
     * @param maxDateOffset The maxDateOffset to set.
     */
    public void setMaxDateOffset(double maxDateOffset)
    {
        this.maxDateOffset = maxDateOffset;
    }

    /**
     * @return Returns the maxDateTable.
     */
    public String getMaxDateTable()
    {
        return maxDateTable;
    }

    /**
     * @param maxDateTable The maxDateTable to set.
     */
    public void setMaxDateTable(String maxDateTable)
    {
        this.maxDateTable = maxDateTable;
    }

    /**
     * @return Returns the outputStep.
     */
    public StepMeta getOutputStep()
    {
        return outputStep;
    }

    /**
     * @param outputStep The outputStep to set.
     */
    public void setOutputStep(StepMeta outputStep)
    {
        this.outputStep = outputStep;
    }

    /**
     * @return Returns the readStep.
     */
    public StepMeta getReadStep()
    {
        return readStep;
    }

    /**
     * @param readStep The readStep to set.
     */
    public void setReadStep(StepMeta readStep)
    {
        this.readStep = readStep;
    }

    /**
     * @return Returns the updateStep.
     */
    public StepMeta getUpdateStep()
    {
        return updateStep;
    }

    /**
     * @param updateStep The updateStep to set.
     */
    public void setUpdateStep(StepMeta updateStep)
    {
        this.updateStep = updateStep;
    }

    /**
     * @return Returns the variables.
     */
    public Hashtable getVariables()
    {
        return variables;
    }

    /**
     * @param variables The variables to set.
     */
    public void setVariables(Hashtable variables)
    {
        this.variables = variables;
    }

    /**
     * @return Returns the writeStep.
     */
    public StepMeta getWriteStep()
    {
        return writeStep;
    }

    /**
     * @param writeStep The writeStep to set.
     */
    public void setWriteStep(StepMeta writeStep)
    {
        this.writeStep = writeStep;
    }

    /**
     * @return Returns the sizeRowset.
     */
    public int getSizeRowset()
    {
        return sizeRowset;
    }

    /**
     * @param sizeRowset The sizeRowset to set.
     */
    public void setSizeRowset(int sizeRowset)
    {
        this.sizeRowset = sizeRowset;
    }

    /**
     * @return Returns the dbCache.
     */
    public DBCache getDbCache()
    {
        return dbCache;
    }

    /**
     * @param dbCache The dbCache to set.
     */
    public void setDbCache(DBCache dbCache)
    {
        this.dbCache = dbCache;
    }

    /**
     * @return Returns the batchId.
     */
    public long getBatchId()
    {
        return batchId;
    }

    /**
     * @param batchId The batchId to set.
     */
    public void setBatchId(long batchId)
    {
        this.batchId = batchId;
    }

    /**
     * @return Returns the useBatchId.
     */
    public boolean isBatchIdUsed()
    {
        return useBatchId;
    }

    /**
     * @param useBatchId The useBatchId to set.
     */
    public void setBatchIdUsed(boolean useBatchId)
    {
        this.useBatchId = useBatchId;
    }

    /**
     * @return Returns the logfieldUsed.
     */
    public boolean isLogfieldUsed()
    {
        return logfieldUsed;
    }

    /**
     * @param logfieldUsed The logfieldUsed to set.
     */
    public void setLogfieldUsed(boolean logfieldUsed)
    {
        this.logfieldUsed = logfieldUsed;
    }

    /**
     * @return Returns the createdDate.
     */
    public Value getCreatedDate()
    {
        return createdDate;
    }

    /**
     * @param createdDate The createdDate to set.
     */
    public void setCreatedDate(Value createdDate)
    {
        this.createdDate = createdDate;
    }

    /**
     * @param createdUser The createdUser to set.
     */
    public void setCreatedUser(String createdUser)
    {
        this.createdUser = createdUser;
    }

    /**
     * @return Returns the createdUser.
     */
    public String getCreatedUser()
    {
        return createdUser;
    }

    /**
     * @param modifiedDate The modifiedDate to set.
     */
    public void setModifiedDate(Value modifiedDate)
    {
        this.modifiedDate = modifiedDate;
    }

    /**
     * @return Returns the modifiedDate.
     */
    public Value getModifiedDate()
    {
        return modifiedDate;
    }

    /**
     * @param modifiedUser The modifiedUser to set.
     */
    public void setModifiedUser(String modifiedUser)
    {
        this.modifiedUser = modifiedUser;
    }

    /**
     * @return Returns the modifiedUser.
     */
    public String getModifiedUser()
    {
        return modifiedUser;
    }

    /**
     * @return the textual representation of the transformation: it's name. If the name has not been set, the classname
     * is returned.
     */
    public String toString()
    {
        if (name != null) return name;
        return getClass().getName();
    }

    /**
     * Cancel queries opened for checking & fieldprediction
     */
    public void cancelQueries() throws KettleDatabaseException
    {
        for (int i = 0; i < nrSteps(); i++)
        {
            getStep(i).getStepMetaInterface().cancelQueries();
        }
    }

    /**
     * Get the arguments used by this transformation.
     * 
     * @param arguments
     * @return A row with the used arguments in it.
     */
    public Row getUsedArguments(String[] arguments)
    {
        Row args = new Row(); // Always at least return an empty row, not null!
        for (int i = 0; i < nrSteps(); i++)
        {
            StepMetaInterface smi = getStep(i).getStepMetaInterface();
            Row row = smi.getUsedArguments(); // Get the command line arguments that this step uses.
            if (row != null)
            {
                for (int x = 0; x < row.size(); x++)
                {
                    Value value = row.getValue(x);
                    String argname = value.getName();
                    if (args.searchValueIndex(argname) < 0) args.addValue(value);
                }
            }
        }

        // OK, so perhaps, we can use the arguments from a previous execution?
        String[] saved = Props.getInstance().getLastArguments();

        // Set the default values on it...
        // Also change the name to "Argument 1" .. "Argument 10"
        if (arguments != null)
        {
            for (int i = 0; i < args.size(); i++)
            {
                Value arg = args.getValue(i);
                int argNr = Const.toInt(arg.getName(), -1);
                if (argNr >= 0 && argNr < arguments.length)
                {
                    arg.setValue(arguments[argNr]);
                }
                if (arg.isNull() || arg.getString() == null) // try the saved option...
                {
                    if (argNr >= 0 && argNr < saved.length && saved[argNr] != null)
                    {
                        arg.setValue(saved[argNr]);
                    }
                }
                arg.setName("Argument " + arg.getName());
            }
        }

        return args;
    }

    public StepMeta getMappingInputStep()
    {
        for (int i = 0; i < nrSteps(); i++)
        {
            if (getStep(i).getStepID().equalsIgnoreCase("MappingInput")) { return getStep(i); }
        }
        return null;
    }

    public StepMeta getMappingOutputStep()
    {
        for (int i = 0; i < nrSteps(); i++)
        {
            if (getStep(i).getStepID().equalsIgnoreCase("MappingOutput")) { return getStep(i); }
        }
        return null;
    }

    /**
     * @return Sleep time waiting when buffer is empty, in nano-seconds
     */
    public int getSleepTimeEmpty()
    {
        return Const.SLEEP_EMPTY_NANOS;
    }
    
    /**
     * @return Sleep time waiting when buffer is full, in nano-seconds
     */
    public int getSleepTimeFull()
    {
        return Const.SLEEP_FULL_NANOS;
    }

    /**
     * @param sleepTimeEmpty The sleepTimeEmpty to set.
     */
    public void setSleepTimeEmpty(int sleepTimeEmpty)
    {
        this.sleepTimeEmpty = sleepTimeEmpty;
    }

    /**
     * @param sleepTimeFull The sleepTimeFull to set.
     */
    public void setSleepTimeFull(int sleepTimeFull)
    {
        this.sleepTimeFull = sleepTimeFull;
    }
}
