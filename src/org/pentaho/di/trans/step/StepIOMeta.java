package org.pentaho.di.trans.step;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.trans.step.errorhandling.StreamInterface;

public class StepIOMeta implements StepIOMetaInterface {
	private boolean inputAcceptor;
	private boolean outputProducer;
	private boolean	inputOptional;
	private List<StreamInterface> streams;
	private boolean	sortedDataRequired;
	
	/**
	 * @param inputAcceptor
	 * @param outputProducer
	 */
	public StepIOMeta(boolean inputAcceptor, boolean outputProducer, boolean inputOptional, boolean sortedDataRequired) {
		this.inputAcceptor = inputAcceptor;
		this.outputProducer = outputProducer;
		this.inputOptional = inputOptional;
		this.sortedDataRequired = sortedDataRequired;
		this.streams = new ArrayList<StreamInterface>();
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
	 * Get the info streams of this step...
	 */
	public StreamInterface[] getInfoStreams() {
		List<StreamInterface> list = new ArrayList<StreamInterface>();
		for (StreamInterface stream : streams) {
			if (stream.getStreamType().equals(StreamInterface.StreamType.INFO)) {
				list.add(stream);
			}
		}
		
		return list.toArray(new StreamInterface[list.size()]);
	}

	/**
	 * Get the target streams of this step...
	 */
	public StreamInterface[] getTargetStreams() {
		List<StreamInterface> list = new ArrayList<StreamInterface>();
		for (StreamInterface stream : streams) {
			if (stream.getStreamType().equals(StreamInterface.StreamType.TARGET)) {
				list.add(stream);
			}
		}
		
		return list.toArray(new StreamInterface[list.size()]);
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
		StreamInterface[] infoStreams = getInfoStreams();
		String[] names = new String[infoStreams.length];
		for (int i=0;i<names.length;i++) {
			names[i] = infoStreams[i].getStepname();
		}
		return names;
	}

	public String[] getTargetStepnames() {
		StreamInterface[] targetStreams = getTargetStreams();
		String[] names = new String[targetStreams.length];
		for (int i=0;i<names.length;i++) {
			names[i] = targetStreams[i].getStepname();
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
			if (stream.getStreamType().equals(StreamInterface.StreamType.INFO));
		}
		
		for (int i=0;i<infoSteps.length;i++) {
			if (i>=list.size()) {
				throw new RuntimeException("We expect all possible info streams to be pre-populated!");
			}
			streams.get(i).setStepMeta(infoSteps[i]);
		}
	}
}
