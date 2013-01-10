package org.pentaho.di.trans.steps.edi2xml;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.RecognitionException;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.di.trans.steps.edi2xml.grammar.FastSimpleGenericEdifactDirectXMLLexer;
import org.pentaho.di.trans.steps.edi2xml.grammar.FastSimpleGenericEdifactDirectXMLParser;

public class Edi2Xml extends BaseStep implements StepInterface {

	private static Class<?> PKG = Edi2XmlMeta.class; // for i18n purposes

	private Edi2XmlData data;
	private Edi2XmlMeta meta;
	private FastSimpleGenericEdifactDirectXMLLexer lexer;
	private CommonTokenStream tokens;
	private FastSimpleGenericEdifactDirectXMLParser parser;

	public Edi2Xml(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		meta = (Edi2XmlMeta) smi;
		data = (Edi2XmlData) sdi;

		Object[] r = getRow(); // get row, blocks when needed!
		if (r == null) // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		String inputValue = "";
		
		if (first) {
			first = false;

			data.inputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
			data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();

			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

			String realInputField = environmentSubstitute(meta.getInputField());
			String realOutputField = environmentSubstitute(meta.getOutputField());

			data.inputFieldIndex = getInputRowMeta().indexOfValue(realInputField);

			int numErrors = 0;

			if (data.inputFieldIndex < 0) {
				logError(BaseMessages.getString(PKG, "Edi2Xml.Log.CouldNotFindInputField", realInputField)); //$NON-NLS-1$
				numErrors++;
			}

			if (!data.inputRowMeta.getValueMeta(data.inputFieldIndex).isString()) {
				logError(BaseMessages.getString(PKG, "Edi2Xml.Log.InputFieldIsNotAString", realInputField)); //$NON-NLS-1$
				numErrors++;
			}

			if (numErrors > 0) {
				setErrors(numErrors);
				stopAll();
				return false;
			}

			data.inputMeta = data.inputRowMeta.getValueMeta(data.inputFieldIndex);

			if (Const.isEmpty(meta.getOutputField())) {
				// same field
				data.outputMeta = data.outputRowMeta.getValueMeta(data.inputFieldIndex);
				data.outputFieldIndex = data.inputFieldIndex;
			} else {
				// new field
				data.outputMeta = data.outputRowMeta.searchValueMeta(realOutputField);
				data.outputFieldIndex = data.outputRowMeta.size() - 1;
			}

			// create instances of lexer/tokenstream/parser
			// treat null values as empty strings for parsing purposes
			inputValue = Const.NVL(data.inputMeta.getString(r[data.inputFieldIndex]), "");

			lexer = new FastSimpleGenericEdifactDirectXMLLexer(new ANTLRStringStream(inputValue));
			tokens = new CommonTokenStream(lexer);
			parser = new FastSimpleGenericEdifactDirectXMLParser(tokens);

		} else {

			// treat null values as empty strings for parsing purposes
			inputValue = Const.NVL(data.inputMeta.getString(r[data.inputFieldIndex]), "");
			
			lexer.setCharStream(new ANTLRStringStream(inputValue));
			tokens.setTokenSource(lexer);
			parser.setTokenStream(tokens);

		}

		try {
			parser.edifact();
			// make sure the row is big enough
			r = RowDataUtil.resizeArray(r, data.outputRowMeta.size());
			// place parsing result into output field
			r[data.outputFieldIndex] = parser.buf.toString();
			putRow(data.outputRowMeta, r);

		} catch (MismatchedTokenException e) {

			StringBuilder errorMessage = new StringBuilder(180);
			errorMessage.append("error parsing edi on line "+e.line+" position "+e.charPositionInLine);
			errorMessage.append(": expecting "+((e.expecting > -1) ? parser.getTokenNames()[e.expecting] : "<UNKNOWN>")+" but found ");
			errorMessage.append((e.token.getType() >= 0) ? parser.getTokenNames()[e.token.getType()] : "<EOF>");

			if (getStepMeta().isDoingErrorHandling()) {
				putError(getInputRowMeta(), r, 1L, errorMessage.toString(), environmentSubstitute(meta.getInputField()), "MALFORMED_EDI");
			} else {
				logError(errorMessage.toString());

				// try to determine the error line
				String errorline = "<UNKNOWN>";
				try{
					errorline = inputValue.split("\\r?\\n")[e.line-1]; 
				}catch(Exception ee){}
				
				logError("Problem line: "+errorline);
				logError(StringUtils.leftPad("^", e.charPositionInLine+"Problem line: ".length()+1));
				throw new KettleException(e);
			}
		} catch (RecognitionException e) {
			StringBuilder errorMessage = new StringBuilder(180);
			errorMessage.append("error parsing edi on line ").append(e.line).append(" position ").append(e.charPositionInLine).append(". ").append(e.toString());

			if (getStepMeta().isDoingErrorHandling()) {
				putError(getInputRowMeta(), r, 1L, errorMessage.toString(), environmentSubstitute(meta.getInputField()), "MALFORMED_EDI");
			} else {
				logError(errorMessage.toString());

				// try to determine the error line
				String errorline = "<UNKNOWN>";
				try{
					errorline = inputValue.split("\\r?\\n")[e.line-1]; 
				}catch(Exception ee){}
				
				logError("Problem line: "+errorline);
				logError(StringUtils.leftPad("^", e.charPositionInLine+"Problem line: ".length()+1));

				throw new KettleException(e);
			}
		}

		if (checkFeedback(getLinesRead())) {
			logBasic("Linenr " + getLinesRead());
		}

		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (Edi2XmlMeta) smi;
		data = (Edi2XmlData) sdi;

		return super.init(smi, sdi);
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (Edi2XmlMeta) smi;
		data = (Edi2XmlData) sdi;

		data.inputMeta = null;
		data.inputRowMeta = null;
		data.outputMeta = null;
		data.outputRowMeta = null;

		super.dispose(smi, sdi);
	}

}
