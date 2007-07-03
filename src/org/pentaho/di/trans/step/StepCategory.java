package org.pentaho.di.trans.step;

/**
 * Different types of steps; right now used by the Step annotation
 * @author Alex Silva
 *
 */
public enum StepCategory
{
	INPUT(Messages.getString("BaseStep.Category.Input")),
    OUTPUT(Messages.getString("BaseStep.Category.Output")),
    TRANSFORM (Messages.getString("BaseStep.Category.Transform")),
    SCRIPTING (Messages.getString("BaseStep.Category.Scripting")),
    LOOKUP(Messages.getString("BaseStep.Category.Lookup")),
    JOINS (Messages.getString("BaseStep.Category.Joins")),
    DATA_WAREHOUSE(Messages.getString("BaseStep.Category.DataWarehouse")),
    JOB   (Messages.getString("BaseStep.Category.Job")),
    MAPPING   (Messages.getString("BaseStep.Category.Mapping")),
    INLINE(Messages.getString("BaseStep.Category.Inline")),
    EXPERIMENTAL(Messages.getString("BaseStep.Category.Experimental")),
    DEPRECATED(Messages.getString("BaseStep.Category.Deprecated"));


	private String name;
	
	private StepCategory(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
}