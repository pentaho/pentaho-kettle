package org.pentaho.di.trans.step;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.trans.step.errorhandling.StreamInterface;

public class StepIOMeta implements StepIOMetaInterface {
	private boolean inputAcceptor;
	private boolean outputProducer;
	private boolean	inputOptional;
	private boolean outputDynamic;
	private boolean inputDynamic;
	
	private List<StreamInterface> streams;
	private boolean	sortedDataRequired;
	
	private String generalInfoDescription;
	private String generalTargetDescription;
	
	/**
	 * @param inputAcceptor
	 * @param outputProducer
	 */
	public StepIOMeta(boolean inputAcceptor, boolean outputProducer, boolean inputOptional, boolean sortedDataRequired, boolean inputDynamic, boolean outputDynamic) {
		this.inputAcceptor = inputAcceptor;
		this.outputProducer = outputProducer;
		this.inputOptional = inputOptional;
		this.sortedDataRequired = sortedDataRequired;
		this.streams = new ArrayList<StreamInterface>();
		this.inputDynamic = inputDynamic;
		this.outputDynamic = outputDynamic;
	}
	
	/**
	 * @return the inputAcceptor
	 */
	public boolean isInputAcceptor() {
		return inputAcceptor;
	}
	/**
	 * @param inputAcceptor the inputAcceptor to set
	 */
	public void setInputAcceptor(boolean inputAcceptor) {
		this.inputAcceptor = inputAcceptor;
	}
	/**
	 * @return the outputProducer
	 */
	public boolean isOutputProducer() {
		return outputProducer;
	}
	/**
	 * @param outputProducer the outputProducer to set
	 */
	public void setOutputProducer(boolean outputProducer) {
		this.outputProducer = outputProducer;
	}

	/**
	 * @return the inputOptional
	 */
	public boolean isInputOptional() {
		return inputOptional;
	}

	/**
	 * @param inputOptional the inputOptional to set
	 */
	public void setInputOptional(boolean inputOptional) {
		this.inputOptional = inputOptional;
	}
	
	/**
	 * @return the info streams of this step.
	 * Important: Modifying this list does not have any effect on the Steps IO metadata.
	 *  
	 */
	public List<StreamInterface> getInfoStreams() {
		List<StreamInterface> list = new ArrayList<StreamInterface>();
		for (StreamInterface stream : streams) {
			if (stream.getStreamType().equals(StreamInterface.StreamType.INFO)) {
				list.add(stream);
			}
		}
		
		return list;
	}

	/**
	 * @return the target streams of this step.
	 * Important: Modifying this list does not have any effect on the Steps IO metadata.
	 *  
	 */
	public List<StreamInterface> getTargetStreams() {
		List<StreamInterface> list = new ArrayList<StreamInterface>();
		for (StreamInterface stream : streams) {
			if (stream.getStreamType().equals(StreamInterface.StreamType.TARGET)) {
				list.add(stream);
			}
		}
		
		return list;
	}
	

	/**
	 * @return the sortedDataRequired
	 */
	public boolean isSortedDataRequired() {
		return sortedDataRequired;
	}

	/**
	 * @param sortedDataRequired the sortedDataRequired to set
	 */
	public void setSortedDataRequired(boolean sortedDataRequired) {
		this.sortedDataRequired = sortedDataRequired;
	}

	public void addStream(StreamInterface stream) {
		streams.add(stream);
	}

	public String[] getInfoStepnames() {
		List<StreamInterface> infoStreams = getInfoStreams();
		String[] names = new String[infoStreams.size()];
		for (int i=0;i<names.length;i++) {
			names[i] = infoStreams.get(i).getStepname();
		}
		return names;
	}

	public String[] getTargetStepnames() {
		List<StreamInterface> targetStreams = getTargetStreams();
		String[] names = new String[targetStreams.size()];
		for (int i=0;i<names.length;i++) {
			names[i] = targetStreams.get(i).getStepname();
		}
		return names;
	}
	
	/**
     * Replace the info steps with the supplied source steps.
     * 
     * @param infoSteps
     */
	public void setInfoSteps(StepMeta[] infoSteps) {
		// First get the info steps...
		//
		List<StreamInterface> list = new ArrayList<StreamInterface>();
		for (StreamInterface stream : streams) {
			if (stream.getStreamType().equals(StreamInterface.StreamType.INFO)) {
				list.add(stream);
			}
		}
		
		for (int i=0;i<infoSteps.length;i++) {
			if (i>=list.size()) {
				throw new RuntimeException("We expect all possible info streams to be pre-populated!");
			}
			streams.get(i).setStepMeta(infoSteps[i]);
		}
	}

	/**
	 * @return the generalInfoDescription
	 */
	public String getGeneralInfoDescription() {
		return generalInfoDescription;
	}

	/**
	 * @param generalInfoDescription the generalInfoDescription to set
	 */
	public void setGeneralInfoDescription(String generalInfoDescription) {
		this.generalInfoDescription = generalInfoDescription;
	}

	/**
	 * @return the generalTargetDescription
	 */
	public String getGeneralTargetDescription() {
		return generalTargetDescription;
	}

	/**
	 * @param generalTargetDescription the generalTargetDescription to set
	 */
	public void setGeneralTargetDescription(String generalTargetDescription) {
		this.generalTargetDescription = generalTargetDescription;
	}
	
	public void clearStreams() {
		streams.clear();
	}

	/**
	 * @return the outputDynamic
	 */
	public boolean isOutputDynamic() {
		return outputDynamic;
	}

	/**
	 * @param outputDynamic the outputDynamic to set
	 */
	public void setOutputDynamic(boolean outputDynamic) {
		this.outputDynamic = outputDynamic;
	}

	/**
	 * @return the inputDynamic
	 */
	public boolean isInputDynamic() {
		return inputDynamic;
	}

	/**
	 * @param inputDynamic the inputDynamic to set
	 */
	public void setInputDynamic(boolean inputDynamic) {
		this.inputDynamic = inputDynamic;
	}
	
	public StreamInterface findTargetStream(StepMeta targetStep) {
		for (StreamInterface stream : getTargetStreams()) {
			if (targetStep.equals(stream.getStepMeta())) return stream;
		}
		return null;
	}
	
	public StreamInterface findInfoStream(StepMeta infoStep) {
		for (StreamInterface stream : getInfoStreams()) {
			if (infoStep.equals(stream.getStepMeta())) return stream;
		}
		return null;
	}
}
