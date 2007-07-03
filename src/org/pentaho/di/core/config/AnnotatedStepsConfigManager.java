package org.pentaho.di.core.config;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.pentaho.di.core.annotations.Inject;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.exception.KettleConfigException;
import org.pentaho.di.core.util.ResolverUtil;
import org.pentaho.di.trans.StepPluginMeta;
import org.pentaho.di.trans.step.Messages;

/**
 * Registers classes annotated with @Step as Kettle/PDI steps, without the need for XML configurations.
 * 
 * Note: XML configurations will superseed and overwrite annotated definitions.
 * 
 * @author Alex Silva
 *
 * @param <T>
 */public class AnnotatedStepsConfigManager<T extends StepPluginMeta> extends BasicConfigManager<T> 
{
	@Inject
	String packages;
	
	@SuppressWarnings("unchecked") //this is ok here because we defined T above.
	public Collection<T> load() throws KettleConfigException
	{
		ResolverUtil<StepPluginMeta> resolver = new ResolverUtil<StepPluginMeta>();
		resolver.find(new ResolverUtil.AnnotatedWith(Step.class), packages != null ? packages.split(",")
				: new String[] {});
		Collection<StepPluginMeta> steps = new LinkedHashSet<StepPluginMeta>(resolver.size());
		for (Class<?> clazz : resolver.getClasses())
		{
			Step step = clazz.getAnnotation(Step.class);
			String[] stepName = step.name();
			if (stepName.length == 1 && stepName[0].equals("")) // default
				stepName = new String[] { clazz.getName() };

			steps.add(new StepPluginMeta(clazz, stepName, Messages.getString(step.description()), Messages
					.getString(step.tooltip()), step.image(), step.category().getName()));
		}
		
		
		return (Collection<T>)steps;
	}

}
