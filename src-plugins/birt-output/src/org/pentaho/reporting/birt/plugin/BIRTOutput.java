
package org.pentaho.reporting.birt.plugin;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IParameterDefn;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.reporting.birt.plugin.BIRTOutputMeta.ProcessorType;



/**
 * 
 * @author Bart Maertens
 * @since 24-aug-2011
 */

public class BIRTOutput extends BaseStep implements StepInterface {
	private static Class<?> PKG = BIRTOutput.class; // for i18n purposes, needed
													// by Translator2!!
													// $NON-NLS-1$

	private BIRTOutputMeta meta;
	private BIRTOutputData data;

	public static IReportEngine engine;
	
	private IScalarParameterDefn scalar;

	public BIRTOutput(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
			throws KettleException {
		meta = (BIRTOutputMeta) smi;
		data = (BIRTOutputData) sdi;

		boolean result = true;

		// For every row we read, we execute a report
		//
		Object[] r = getRow();

		// All done, signal this to the next steps...
		//
		if (r == null) {
			setOutputDone();
			return false;
		}

		if (first) {
			first = false;

			data.inputFieldIndex = getInputRowMeta().indexOfValue(
					meta.getInputFileField());
			if (data.inputFieldIndex < 0) {
				throw new KettleException(BaseMessages.getString(PKG,
						"PentahoReportingOutput.Exception.CanNotFindField",
						meta.getInputFileField()));
			}
			data.outputFieldIndex = getInputRowMeta().indexOfValue(
					meta.getOutputFileField());
			if (data.inputFieldIndex < 0) {
				throw new KettleException(BaseMessages.getString(PKG,
						"PentahoReportingOutput.Exception.CanNotFindField",
						meta.getOutputFileField()));
			}

			performPentahoReportingBoot(log, getClass());
		}

		String sourceFilename = getInputRowMeta().getString(r,
				data.inputFieldIndex);
		String targetFilename = getInputRowMeta().getString(r,
				data.outputFieldIndex);

		processReport(r, sourceFilename, targetFilename,
				meta.getOutputProcessorType());

		// in case we want the input data to go to more steps.
		//
		putRow(getInputRowMeta(), r);

		if (checkFeedback(getLinesOutput()))
			logBasic(BaseMessages.getString(PKG,
					"PentahoReportingOutput.Log.LineNumber") + getLinesOutput()); //$NON-NLS-1$

		return result;
	}

	public static void performPentahoReportingBoot(LogChannelInterface log, Class<?> referenceClass) {

		// Boot the BIRT reporting engine.
		try {
		  if (engine==null) {
  		  PluginRegistry registry = PluginRegistry.getInstance();
  	    PluginInterface plugin = registry.findPluginWithId(StepPluginType.class, "BIRTOutput");
  	    String birtHome;
  	    if (plugin.getPluginDirectory()==null) {
  	      birtHome="src-plugins/birt-output/lib/ReportEngine";
  	    } else {
  	      birtHome = new File(plugin.getPluginDirectory().toURI()).getPath()+"/lib/ReportEngine"; 
  	    }
  	    
        System.out.println("---- BIRT HOME SET TO: "+birtHome);
  	    for (String lib : plugin.getLibraries()) {
  	      System.out.println("-------- "+lib);
  	    }
  	    
  		  // String resourcePath = "";
  			EngineConfig config = new EngineConfig();
  			config.setLogConfig("/tmp/", java.util.logging.Level.FINEST);
  			config.setEngineHome(birtHome);
  			config.setResourcePath(birtHome);
  
  			Platform.startup(config);
  
  			IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
  			engine = factory.createReportEngine(config);
		  }
		} catch (Exception be) {
		  log.logError("Error booting the BIRT reporting engine...", be);
		}
	}

	public static IReportRunnable loadMasterReport(String sourceFilename)
			throws Exception {

		IReportRunnable report = engine.openReportDesign(sourceFilename);

		return report;
	}

	@SuppressWarnings("deprecation")
  private void processReport(Object[] r, String sourceFilename,
			String targetFilename, ProcessorType outputProcessorType)
			throws KettleException {
		try {

			// Load the master report from the .rptdesign.
			//
			IReportRunnable report = loadMasterReport(sourceFilename);
			IRunAndRenderTask task = engine.createRunAndRenderTask(report);

			// Set the parameters values that are present in the various
			// fields...
			IGetParameterDefinitionTask paramTask = engine.createGetParameterDefinitionTask(report);
			Collection<?> definition = paramTask.getParameterDefns(true);

			for (String parameterName : meta.getParameterFieldMap().keySet()) {
				String fieldName = meta.getParameterFieldMap().get(
						parameterName);
				if (fieldName != null) {
					int index = getInputRowMeta().indexOfValue(fieldName);
					if (index < 0) {
						throw new KettleException(
								BaseMessages
										.getString(
												PKG,
												"PentahoReportingOutput.Exception.CanNotFindField",
												fieldName));
					}

					int paramType = findParameterClass(definition,
							parameterName);
					Object value = null;
					if (paramType != -1) {
						System.out.println("######################################");
						System.out.println("param class: " + paramType);
						if (paramType == IParameterDefn.TYPE_ANY) {
							value = getInputRowMeta().getValueMeta(index)	.convertToNormalStorageType(r[index]);
						} else if (paramType == IParameterDefn.TYPE_STRING) {
							value = getInputRowMeta().getString(r, index);
						} else if (paramType == IParameterDefn.TYPE_FLOAT) {
							value = getInputRowMeta().getNumber(r, index);
						} else if (paramType == IParameterDefn.TYPE_DECIMAL) {
							value = getInputRowMeta().getBigNumber(r, index);
						} else if (paramType == IParameterDefn.TYPE_DATE_TIME) {
							value = getInputRowMeta().getDate(r, index);
						} else if (paramType == IParameterDefn.TYPE_BOOLEAN) {
							value = getInputRowMeta().getBoolean(r, index);
						} else if (paramType == IParameterDefn.TYPE_INTEGER) {
							value = getInputRowMeta().getInteger(r, index);
						} else if (paramType == IParameterDefn.TYPE_DATE) {
							value = getInputRowMeta().getDate(r, index);
						} else if (paramType == IParameterDefn.TYPE_TIME) {
							value = getInputRowMeta().getDate(r, index);
						}
						System.out.println("setting param " + parameterName + " to " + value );
				         task.setParameterValue(parameterName, value);
						System.out.println("######################################");
					} else {
						// This parameter was not found, log this as a
						// warning...
						//
						logBasic(BaseMessages.getString(PKG,
								"BIRTOutput.Log.ParameterNotFoundInReport",
								parameterName, sourceFilename));
					}

				}
			}

			IRenderOption options = new RenderOption();

			switch (outputProcessorType) {
			case PDF:
				options.setOutputFormat("pdf");
				PDFRenderOption pdfOptions = new PDFRenderOption(options);
				pdfOptions.setOption(IPDFRenderOption.FIT_TO_PAGE, true);
				pdfOptions.setOption(
						IPDFRenderOption.PAGEBREAK_PAGINATION_ONLY, true);
				break;
			case HTML:
				options.setOutputFormat("html");
				HTMLRenderOption htmlOptions = new HTMLRenderOption(options);
				htmlOptions.setImageDirectory("output/image");
				htmlOptions.setHtmlPagination(false);
				htmlOptions.setBaseImageURL("http://myhost/prependme?image=1");
				htmlOptions.setHtmlRtLFlag(false);
				htmlOptions.setEmbeddable(false);
				break;
			case XLS:
				options.setOutputFormat("xls");
				break;
			case POSTSCRIPT:
				options.setOutputFormat("postscript");
				break;
			case DOC:
				options.setOutputFormat("doc");
				break;
			case PPT:
				options.setOutputFormat("ppt");
				break;
			}

			options.setOutputFileName(targetFilename);
			task.setRenderOption(options);
			task.run();

			ResultFile resultFile = new ResultFile(
					ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(
							targetFilename, getTransMeta()), getTransMeta()
							.getName(), getStepname());
			resultFile
					.setComment("This file was created with the Pentaho BIRT Output step");
			addResultFile(resultFile);

		} catch (Exception e) {

			throw new KettleException(
					BaseMessages.getString(
							PKG,
							"PentahoReportingOutput.Exception.UnexpectedErrorRenderingReport",
							sourceFilename, targetFilename,
							outputProcessorType.getDescription()), e);
		}
	}

	@SuppressWarnings("unchecked")
  private int findParameterClass(Collection<?> definition, String parameterName) {
		Iterator<IParameterDefnBase> i = (Iterator<IParameterDefnBase>) definition.iterator();
		while (i.hasNext()) {
			IParameterDefnBase param = (IParameterDefnBase) i.next();
			if (param instanceof IParameterGroupDefn) {
				IParameterGroupDefn group = (IParameterGroupDefn) param;
				Iterator<?> i2 = group.getContents().iterator();
				while (i2.hasNext()) {
					scalar = (IScalarParameterDefn) i2.next();
					if (scalar.getName().equals(parameterName))
						return scalar.getDataType();
				}
			} else {
				scalar = (IScalarParameterDefn) param;
				if (scalar.getName().equals(parameterName))
					return scalar.getDataType();
			}
		}
		return -1;
	}
	
	@Override
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
	  
	  return super.init(smi, sdi);
	}
}
