package org.pentaho.di.trans.steps.autodoc;

import java.awt.Point;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.reporting.engine.classic.core.AttributeNames;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.GroupDataBody;
import org.pentaho.reporting.engine.classic.core.ItemBand;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.RelationalGroup;
import org.pentaho.reporting.engine.classic.core.ReportFooter;
import org.pentaho.reporting.engine.classic.core.ReportHeader;
import org.pentaho.reporting.engine.classic.core.SimplePageDefinition;
import org.pentaho.reporting.engine.classic.core.TableDataFactory;
import org.pentaho.reporting.engine.classic.core.elementfactory.ContentElementFactory;
import org.pentaho.reporting.engine.classic.core.elementfactory.LabelElementFactory;
import org.pentaho.reporting.engine.classic.core.elementfactory.TextFieldElementFactory;
import org.pentaho.reporting.engine.classic.core.modules.misc.beanshell.BSHExpression;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.csv.CSVReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.rtf.RTFReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xls.ExcelReportUtil;
import org.pentaho.reporting.engine.classic.core.style.TextStyleKeys;
import org.pentaho.reporting.engine.classic.core.style.TextWrap;

public class KettleReportBuilder {
	
	public enum OutputType {
		PDF, HTML, DOC, XLS, CSV,  
		METADATA
	}

	private List<ReportSubjectLocation> filenames;
	
	private MasterReport report;

	private String	targetFilename;

	private AutoDocOptionsInterface	options;

	private LoggingObjectInterface	parentObject;

	public KettleReportBuilder(LoggingObjectInterface parentObject, List<ReportSubjectLocation> locations, String targetFilename, AutoDocOptionsInterface options) {
		this.parentObject = parentObject;
		this.filenames = locations;
		this.targetFilename = targetFilename;
		this.options = options;
	}
	
	public void createReport() throws Exception {
		// Create a new report
		//
		report = new MasterReport();
		
		// Define where which transformation and step to read from, explain it to the reporting engine
		//
		KettleFileTableModel transMetaTableModel = new KettleFileTableModel(parentObject, filenames);
		
		TableDataFactory dataFactory = new TableDataFactory("default", transMetaTableModel);
		
		// Give the data to the report at runtime!
		//
		report.setDataFactory(dataFactory);
		
		// Add a report header and footer
		//
		ReportHeader reportHeader = new ReportHeader();
		report.setReportHeader(reportHeader);
		ReportFooter reportFooter = new ReportFooter();
		report.setReportFooter(reportFooter);
		
		// Now we need to define an area on which we can draw report elements, called groups and bands...
		//
		RelationalGroup group = new RelationalGroup();
		group.addField("filename");
		GroupDataBody groupData = new GroupDataBody();
		ItemBand itemBand = new ItemBand();
		itemBand.setVisible(true);
		groupData.setItemBand(itemBand);
		group.setBody(groupData);
		report.setRootGroup(group);
		
		// Put a title at the top of the report
		//
		/*
		LabelElementFactory labelElementFactory = new LabelElementFactory();
		labelElementFactory.setText("Kettle documentation");
		labelElementFactory.setMinimumWidth(500f);
		labelElementFactory.setMinimumHeight(20f);
		labelElementFactory.setUnderline(true);
		labelElementFactory.setBold(true);
		Element label = labelElementFactory.createElement();
		
		// Add the label to the header...
		//
		reportHeader.addElement(label);
		*/
		Float fontHeight = 12f;
		int pagePosition = 0;
		int labelWidth = 120;
		int textWidth = 630;
		
		boolean labelUnderline=false;
		boolean labelBold=false;
		boolean labelItalic=true;
		
		// Set the header to bold...
		//
		reportHeader.getStyle().setStyleProperty(TextStyleKeys.BOLD, true);
		
		LabelElementFactory labelElementFactory = new LabelElementFactory();
		labelElementFactory.setText("Filename: ");
		labelElementFactory.setMinimumWidth((float)labelWidth);
		labelElementFactory.setMinimumHeight(fontHeight);
		labelElementFactory.setUnderline(labelUnderline);
		labelElementFactory.setBold(labelBold);
		labelElementFactory.setItalic(labelItalic);
		Element label = labelElementFactory.createElement();
		itemBand.addElement(label);
		
		// Now add the filename to the report
		//
		TextFieldElementFactory textFactory = new TextFieldElementFactory();
		textFactory.setFieldname("filename");
		textFactory.setAbsolutePosition(new Point(labelWidth, pagePosition));
		textFactory.setMinimumWidth((float)textWidth);
		textFactory.setMinimumHeight(fontHeight);
		Element filenameElement = textFactory.createElement();
		itemBand.addElement(filenameElement);

		pagePosition+=fontHeight;

		// The name of the transformation
		//
		if (options.isIncludingName()) {
			labelElementFactory = new LabelElementFactory();
			labelElementFactory.setText("Name: ");
			labelElementFactory.setAbsolutePosition(new Point(0, pagePosition));
			labelElementFactory.setMinimumWidth((float)labelWidth);
			labelElementFactory.setMinimumHeight(fontHeight);
			labelElementFactory.setUnderline(labelUnderline);
			labelElementFactory.setBold(labelBold);
			labelElementFactory.setItalic(labelItalic);
			label = labelElementFactory.createElement();
			itemBand.addElement(label);
	
			textFactory = new TextFieldElementFactory();
			textFactory.setFieldname("name");
			textFactory.setAbsolutePosition(new Point(labelWidth, pagePosition));
			textFactory.setMinimumWidth((float)textWidth);
			textFactory.setMinimumHeight(fontHeight);
			Element nameElement = textFactory.createElement();
			itemBand.addElement(nameElement);
			pagePosition+=fontHeight;
		}

		// The description of the transformation...
		//
		if (options.isIncludingDescription()) {
			labelElementFactory = new LabelElementFactory();
			labelElementFactory.setText("Description: ");
			labelElementFactory.setAbsolutePosition(new Point(0, pagePosition));
			labelElementFactory.setMinimumWidth((float)labelWidth);
			labelElementFactory.setMinimumHeight(fontHeight);
			labelElementFactory.setUnderline(labelUnderline);
			labelElementFactory.setBold(labelBold);
			labelElementFactory.setItalic(labelItalic);
			label = labelElementFactory.createElement();
			itemBand.addElement(label);
	
			textFactory = new TextFieldElementFactory();
			textFactory.setFieldname("description");
			textFactory.setAbsolutePosition(new Point(labelWidth, pagePosition));
			textFactory.setMinimumWidth((float)textWidth);
			textFactory.setMinimumHeight(fontHeight);
			Element descriptionElement = textFactory.createElement();
			itemBand.addElement(descriptionElement);
			descriptionElement.setDynamicContent(true);
      
			pagePosition+=fontHeight;
		}

		// The description of the transformation...
		//
		if (options.isIncludingExtendedDescription()) {
			labelElementFactory = new LabelElementFactory();
			labelElementFactory.setText("Long description: ");
			labelElementFactory.setAbsolutePosition(new Point(0, pagePosition));
			labelElementFactory.setMinimumWidth((float)labelWidth);
			labelElementFactory.setMinimumHeight(fontHeight);
			labelElementFactory.setUnderline(labelUnderline);
			labelElementFactory.setBold(labelBold);
			labelElementFactory.setItalic(labelItalic);
			label = labelElementFactory.createElement();
			itemBand.addElement(label);
	
			textFactory = new TextFieldElementFactory();
			textFactory.setFieldname("extended_description");
			textFactory.setAbsolutePosition(new Point(labelWidth, pagePosition));
			textFactory.setMinimumWidth((float)textWidth);
			textFactory.setMinimumHeight(fontHeight);
	    textFactory.setDynamicHeight(true);
	    textFactory.setWrap(TextWrap.WRAP);
			Element descriptionElement = textFactory.createElement();
			itemBand.addElement(descriptionElement);
			pagePosition+=fontHeight;
		}

		// Include a line with logging information
		//
		if (options.isIncludingLoggingConfiguration()) {
			labelElementFactory = new LabelElementFactory();
			labelElementFactory.setText("Logging: ");
			labelElementFactory.setAbsolutePosition(new Point(0, pagePosition));
			labelElementFactory.setMinimumWidth((float)labelWidth);
			labelElementFactory.setMinimumHeight(fontHeight);
			labelElementFactory.setUnderline(labelUnderline);
			labelElementFactory.setBold(labelBold);
			labelElementFactory.setItalic(labelItalic);
			label = labelElementFactory.createElement();
			itemBand.addElement(label);
	
			textFactory = new TextFieldElementFactory();
			textFactory.setFieldname("logging");
			textFactory.setAbsolutePosition(new Point(labelWidth, pagePosition));
			textFactory.setMinimumWidth((float)textWidth);
			textFactory.setMinimumHeight(fontHeight);
			Element loggingElement = textFactory.createElement();
			itemBand.addElement(loggingElement);
			pagePosition+=fontHeight;
		}

		// Include a line with the creation date and user
		//
		if (options.isIncludingCreated()) {
			labelElementFactory = new LabelElementFactory();
			labelElementFactory.setText("Creation: ");
			labelElementFactory.setAbsolutePosition(new Point(0, pagePosition));
			labelElementFactory.setMinimumWidth((float)labelWidth);
			labelElementFactory.setMinimumHeight(fontHeight);
			labelElementFactory.setUnderline(labelUnderline);
			labelElementFactory.setBold(labelBold);
			labelElementFactory.setItalic(labelItalic);
			label = labelElementFactory.createElement();
			itemBand.addElement(label);
	
			textFactory = new TextFieldElementFactory();
			textFactory.setFieldname("creation");
			textFactory.setAbsolutePosition(new Point(labelWidth, pagePosition));
			textFactory.setMinimumWidth((float)textWidth);
			textFactory.setMinimumHeight(fontHeight);
			Element loggingElement = textFactory.createElement();
			itemBand.addElement(loggingElement);
			pagePosition+=fontHeight;
		}

		// Include a line with the modification date and user
		//
		if (options.isIncludingModified()) {
			labelElementFactory = new LabelElementFactory();
			labelElementFactory.setText("Modification: ");
			labelElementFactory.setAbsolutePosition(new Point(0, pagePosition));
			labelElementFactory.setMinimumWidth((float)labelWidth);
			labelElementFactory.setMinimumHeight(fontHeight);
			labelElementFactory.setUnderline(labelUnderline);
			labelElementFactory.setBold(labelBold);
			labelElementFactory.setItalic(labelItalic);
			label = labelElementFactory.createElement();
			itemBand.addElement(label);
	
			textFactory = new TextFieldElementFactory();
			textFactory.setFieldname("modification");
			textFactory.setAbsolutePosition(new Point(labelWidth, pagePosition));
			textFactory.setMinimumWidth((float)textWidth);
			textFactory.setMinimumHeight(fontHeight);
			Element loggingElement = textFactory.createElement();
			itemBand.addElement(loggingElement);
			pagePosition+=fontHeight;
		}

		// The last execution result
		//
		if (options.isIncludingLastExecutionResult()) {
			labelElementFactory = new LabelElementFactory();
			labelElementFactory.setText("Last execution result: ");
			labelElementFactory.setAbsolutePosition(new Point(0, pagePosition));
			labelElementFactory.setMinimumWidth((float)labelWidth);
			labelElementFactory.setMinimumHeight(fontHeight);
			labelElementFactory.setUnderline(labelUnderline);
			labelElementFactory.setBold(labelBold);
			labelElementFactory.setItalic(labelItalic);
			label = labelElementFactory.createElement();
			itemBand.addElement(label);
	
			textFactory = new TextFieldElementFactory();
			textFactory.setFieldname("last_exec_result");
			textFactory.setAbsolutePosition(new Point(labelWidth, pagePosition));
			textFactory.setMinimumWidth((float)textWidth);
			textFactory.setMinimumHeight(fontHeight);
			Element loggingElement = textFactory.createElement();
			itemBand.addElement(loggingElement);
			pagePosition+=fontHeight;
		}
		
		// Optionally include an image of the transformation...
		//
		String packName = KettleReportBuilder.class.getPackage().getName();
		if (options.isIncludingImage()) {
			String bshCode = "Object getValue() { "+Const.CR+
        "  return new "+packName+".TransJobDrawable(dataRow, "+(options.getOutputType()==OutputType.PDF ? "true" : "false")+");" + Const.CR +
				"}";
			BSHExpression bshExpression = new BSHExpression();
			bshExpression.setExpression(bshCode);
			bshExpression.setName("getImage");
			report.addExpression(bshExpression);
			
			ContentElementFactory contentElementFactory = new ContentElementFactory();
			contentElementFactory.setName("image");
			contentElementFactory.setAbsolutePosition(new Point(0, pagePosition));
			contentElementFactory.setMinimumWidth(750f);
			contentElementFactory.setMaximumWidth(750f);
			contentElementFactory.setMinimumHeight(400f);
			contentElementFactory.setMaximumHeight(750f);
		  contentElementFactory.setScale(true);
      contentElementFactory.setDynamicHeight(false);
			// contentElementFactory.setAvoidPagebreaks(true);
			Element imageElement = contentElementFactory.createElement();
			
			imageElement.setAttributeExpression(AttributeNames.Core.NAMESPACE, AttributeNames.Core.VALUE, bshExpression);
			imageElement.setAttribute(AttributeNames.Core.NAMESPACE, AttributeNames.Core.IMAGE_ENCODING_QUALITY, "9");
			imageElement.setAttribute(AttributeNames.Core.NAMESPACE, AttributeNames.Core.IMAGE_ENCODING_TYPE, "PNG");		
			itemBand.addElement(imageElement);
		}
		
		Paper a4Paper = new Paper ();
		
		double paperWidth = 8.26;
		double paperHeight = 11.69;
		a4Paper.setSize(paperWidth * 72.0, paperHeight * 72.0);

		/*
		* set the margins respectively the imageable area
		*/
		double leftMargin = 0.78; /* should be about 2cm */
		double rightMargin = 0.78;
		double topMargin = 0.08; // this is a very small topMargin
		double bottomMargin = 0.78;

		a4Paper.setImageableArea(leftMargin * 72.0, topMargin * 72.0,
		(paperWidth - leftMargin - rightMargin)*72.0,
		(paperHeight - topMargin - bottomMargin)*72.0);

		/*
		* create a PageFormat and associate the Paper with it.
		*/
    PageFormat pageFormat = new PageFormat();
    pageFormat.setOrientation (PageFormat.LANDSCAPE);
		pageFormat.setPaper(a4Paper);
		
		SimplePageDefinition pageDefinition = new SimplePageDefinition(pageFormat);
		report.setPageDefinition(pageDefinition);
	}
	
	public void render() throws Exception {
		
		createReport();

		switch(options.getOutputType()) {
		case PDF: 
			PdfReportUtil.createPDF(report, targetFilename);
			break;
		case DOC: 
			RTFReportUtil.createRTF(report, targetFilename);
			break;
		case XLS: 
			ExcelReportUtil.createXLS(report, targetFilename);
			break;
		case HTML:
			HtmlReportUtil.createDirectoryHTML(report, targetFilename);
			break;
		case CSV: 
			CSVReportUtil.createCSV(report, targetFilename);
			break;
		}
	}
}

