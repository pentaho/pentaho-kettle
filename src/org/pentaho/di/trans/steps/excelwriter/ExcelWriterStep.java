//
// Excel Writer plugin for Pentaho PDI a.k.a. Kettle
// 
// Copyright (C) 2010 Slawomir Chodnicki
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

package org.pentaho.di.trans.steps.excelwriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.vfs.FileObject;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

public class ExcelWriterStep extends BaseStep implements StepInterface {

	private ExcelWriterStepData data;
	private ExcelWriterStepMeta meta;

	private static Class<?> PKG = ExcelWriterStepMeta.class; // for i18n

	public ExcelWriterStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		meta = (ExcelWriterStepMeta) smi;
		data = (ExcelWriterStepData) sdi;

		// get next row
		Object[] r = getRow();

		// first row initialization
		if (first && r != null) {

			first = false;
			data.outputRowMeta = getInputRowMeta().clone();
			data.inputRowMeta = getInputRowMeta().clone();

			// if we are supposed to init the file dalayed, here we go
			if (r != null && meta.isDoNotOpenNewFileInit()) {
				data.firstFileOpened = true;
				prepareNextOutputFile();
			}

			// remember where the output fields are in the input row
			data.fieldnrs = new int[meta.getOutputFields().length];
			for (int i = 0; i < meta.getOutputFields().length; i++) {
				data.fieldnrs[i] = data.inputRowMeta.indexOfValue(meta.getOutputFields()[i].getName());
				if (data.fieldnrs[i] < 0) {
					logError("Field [" + meta.getOutputFields()[i].getName() + "] couldn't be found in the input stream!");
					setErrors(1);
					stopAll();
					return false;
				}
			}

			// remember where the comment fields are in the input row
			data.commentfieldnrs = new int[meta.getOutputFields().length];
			for (int i = 0; i < meta.getOutputFields().length; i++) {
				data.commentfieldnrs[i] = data.inputRowMeta.indexOfValue(meta.getOutputFields()[i].getCommentField());
				if (data.commentfieldnrs[i] < 0 && !Const.isEmpty(meta.getOutputFields()[i].getCommentField())) {
					logError("Comment Field [" + meta.getOutputFields()[i].getCommentField() + "] couldn't be found in the input stream!");
					setErrors(1);
					stopAll();
					return false;
				}
			}

			// remember where the comment author fields are in the input row
			data.commentauthorfieldnrs = new int[meta.getOutputFields().length];
			for (int i = 0; i < meta.getOutputFields().length; i++) {
				data.commentauthorfieldnrs[i] = data.inputRowMeta.indexOfValue(meta.getOutputFields()[i].getCommentAuthorField());
				if (data.commentauthorfieldnrs[i] < 0 && !Const.isEmpty(meta.getOutputFields()[i].getCommentAuthorField())) {
					logError("Comment Author Field [" + meta.getOutputFields()[i].getCommentAuthorField() + "] couldn't be found in the input stream!");
					setErrors(1);
					stopAll();
					return false;
				}
			}

			// remember where the link fields are in the input row
			data.linkfieldnrs = new int[meta.getOutputFields().length];
			for (int i = 0; i < meta.getOutputFields().length; i++) {
				data.linkfieldnrs[i] = data.inputRowMeta.indexOfValue(meta.getOutputFields()[i].getHyperlinkField());
				if (data.linkfieldnrs[i] < 0 && !Const.isEmpty(meta.getOutputFields()[i].getHyperlinkField())) {
					logError("Link Field [" + meta.getOutputFields()[i].getHyperlinkField() + "] couldn't be found in the input stream!");
					setErrors(1);
					stopAll();
					return false;
				}
			}

		}

		if (r != null) {

			// File Splitting Feature, is it time to create a new file?
			if (!meta.isAppendLines() && meta.getSplitEvery() > 0 && data.datalines > 0 && data.datalines % meta.getSplitEvery() == 0) {
				closeOutputFile();
				prepareNextOutputFile();
			}

			writeNextLine(r);
			incrementLinesOutput();

			data.datalines++;

			// pass on the row unchanged
			putRow(data.outputRowMeta, r);

			// Some basic logging
			if (checkFeedback(getLinesOutput())) {
				if (log.isBasic())
					logBasic("Linenr " + getLinesOutput());
			}
			return true;

		} else {

			// after the last row, the (last) file is closed
			if (data.wb != null) {
				closeOutputFile();
			}
			setOutputDone();
			return false;
		}

	}

	private void closeOutputFile() throws KettleException {
		try {

			// may have to write a footer here
			if (meta.isFooterEnabled()) {
				writeHeader();
			}

			// handle auto size for columns
			if (meta.isAutoSizeColums()) {

				if (meta.getOutputFields() == null || meta.getOutputFields().length == 0) {
					for (int i = 0; i < data.inputRowMeta.size(); i++) {
						data.sheet.autoSizeColumn(i + data.startingCol);
					}
				} else {
					for (int i = 0; i < meta.getOutputFields().length; i++) {
						data.sheet.autoSizeColumn(i + data.startingCol);
					}
				}
			}

			BufferedOutputStream out = new BufferedOutputStream(KettleVFS.getOutputStream(data.file, false));
			data.wb.write(out);
			out.close();
		} catch (IOException e) {
			throw new KettleException(e);
		}
	}

	public void writeNextLine(Object[] r) throws KettleException {

		try {

			openLine();

			Row xlsRow = data.sheet.getRow(data.posY);
			if (xlsRow == null) {
				xlsRow = data.sheet.createRow(data.posY);
			}

			Object v = null;

			if (meta.getOutputFields() == null || meta.getOutputFields().length == 0) {
				/*
				 * Write all values in stream to text file.
				 */
				for (int i = 0; i < data.inputRowMeta.size(); i++) {
					v = r[i];
					writeField(v, data.inputRowMeta.getValueMeta(i), null, xlsRow, data.posX++, r, i, false);
				}
				// go to the next line
				data.posX = data.startingCol;
				data.posY++;

			} else {
				/*
				 * Only write the fields specified!
				 */
				for (int i = 0; i < meta.getOutputFields().length; i++) {
					v = r[data.fieldnrs[i]];
					writeField(v, data.inputRowMeta.getValueMeta(data.fieldnrs[i]), meta.getOutputFields()[i], xlsRow, data.posX++, r, i, false);
				}

				// go to the next line
				data.posX = data.startingCol;
				data.posY++;
			}
		} catch (Exception e) {
			logError("Error writing line :" + e.toString());
			throw new KettleException(e);
		}

	}

	private Comment createCellComment(String author, String comment) {

		// comments only supported for XLSX
		if (data.sheet instanceof XSSFSheet) {
			CreationHelper factory = data.wb.getCreationHelper();
			Drawing drawing = data.sheet.createDrawingPatriarch();

			ClientAnchor anchor = factory.createClientAnchor();
			Comment cmt = drawing.createCellComment(anchor);
			RichTextString str = factory.createRichTextString(comment);
			cmt.setString(str);
			cmt.setAuthor(author);
			return cmt;

		}
		return null;

	}

	/**
	 * @param reference
	 * @return the cell the refernce points to
	 */
	private Cell getCellFromReference(String reference) {

		CellReference cellRef = new CellReference(reference);

		String sheetName = cellRef.getSheetName();

		Sheet sheet = data.sheet;
		if (!Const.isEmpty(sheetName)) {
			sheet = data.wb.getSheet(sheetName);
		}

		if (sheet == null) {
			return null;
		}

		// reference is assumed to be absolute
		Row xlsRow = sheet.getRow(cellRef.getRow());
		if (xlsRow == null) {
			return null;
		}
		Cell styleCell = xlsRow.getCell(cellRef.getCol());

		return styleCell;

	}

	private void writeField(Object v, ValueMetaInterface vMeta, ExcelWriterStepField excelField, Row xlsRow, int posX, Object[] row, int fieldNr, boolean isTitle) throws KettleException {

		try {

			// get the cell
			Cell cell = xlsRow.getCell(posX);
			if (cell == null) {
				cell = xlsRow.createCell(posX);
			}

			// if the style of this field is cached, reuse it
			if (!isTitle && data.getCachedStyle(fieldNr) != null) {
				cell.setCellStyle(data.getCachedStyle(fieldNr));
			} else {
				// apply style if requested
				if (excelField != null) {

					// determine correct cell for title or data rows
					String styleRef = null;
					if (!isTitle && !Const.isEmpty(excelField.getStyleCell())) {

						styleRef = excelField.getStyleCell();
					} else if (isTitle && !Const.isEmpty(excelField.getTitleStyleCell())) {
						styleRef = excelField.getTitleStyleCell();
					}

					if (styleRef != null) {
						Cell styleCell = getCellFromReference(styleRef);
						if (styleCell != null && cell != styleCell) {
							cell.setCellStyle(styleCell.getCellStyle());
						}
					}
				}

				// set cell format as specified, specific format overrides cell specification
				if (!isTitle && excelField != null && !Const.isEmpty(excelField.getFormat()) && !excelField.getFormat().startsWith("Image")) {
					DataFormat format = data.wb.createDataFormat();
					short formatIndex = format.getFormat(excelField.getFormat());
					CellUtil.setCellStyleProperty(cell, data.wb, CellUtil.DATA_FORMAT, formatIndex);
				}

				// cache it for later runs
				if (!isTitle) {
					data.cacheStyle(fieldNr, cell.getCellStyle());
				}
			}

			// create link on cell if requested
			if (!isTitle && excelField != null && data.linkfieldnrs[fieldNr] >= 0) {

				String link = data.inputRowMeta.getValueMeta(data.linkfieldnrs[fieldNr]).getString(row[data.linkfieldnrs[fieldNr]]);
				if (!Const.isEmpty(link)) {

					CreationHelper ch = data.wb.getCreationHelper();
					// set the link on the cell depending on link type
					Hyperlink hyperLink = null;
					if (link.startsWith("http:") || link.startsWith("https:") || link.startsWith("ftp:")) {
						hyperLink = ch.createHyperlink(Hyperlink.LINK_URL);
						hyperLink.setLabel("URL Link");
					} else if (link.startsWith("mailto:")) {
						hyperLink = ch.createHyperlink(Hyperlink.LINK_EMAIL);
						hyperLink.setLabel("Email Link");
					} else if (link.startsWith("'")) {
						hyperLink = ch.createHyperlink(Hyperlink.LINK_DOCUMENT);
						hyperLink.setLabel("Link within this document");
					} else {
						hyperLink = ch.createHyperlink(Hyperlink.LINK_FILE);
						hyperLink.setLabel("Link to a file");
					}

					hyperLink.setAddress(link);
					cell.setHyperlink(hyperLink);

					if (data.getCachedLinkStyle(fieldNr) != null) {
						cell.setCellStyle(data.getCachedLinkStyle(fieldNr));
					} else {
						//CellStyle style = cell.getCellStyle();
						Font origFont = data.wb.getFontAt(cell.getCellStyle().getFontIndex());
						Font hlink_font = data.wb.createFont();
						// reporduce original font characteristics

						hlink_font.setBoldweight(origFont.getBoldweight());
						hlink_font.setCharSet(origFont.getCharSet());
						hlink_font.setFontHeight(origFont.getFontHeight());
						hlink_font.setFontName(origFont.getFontName());
						hlink_font.setItalic(origFont.getItalic());
						hlink_font.setStrikeout(origFont.getStrikeout());
						hlink_font.setTypeOffset(origFont.getTypeOffset());
						// make it blue and underlined
						hlink_font.setUnderline(Font.U_SINGLE);
						hlink_font.setColor(IndexedColors.BLUE.getIndex());
						CellUtil.setCellStyleProperty(cell, data.wb, CellUtil.FONT, hlink_font.getIndex());
						data.cacheLinkStyle(fieldNr, cell.getCellStyle());
					}
				}
			}

			// create comment on cell if requrested
			if (!isTitle && excelField != null && data.commentfieldnrs[fieldNr] >= 0 && data.wb instanceof XSSFWorkbook) {

				String comment = data.inputRowMeta.getValueMeta(data.commentfieldnrs[fieldNr]).getString(row[data.commentfieldnrs[fieldNr]]);
				if (!Const.isEmpty(comment)) {
					String author = data.commentauthorfieldnrs[fieldNr] >= 0 ? data.inputRowMeta.getValueMeta(data.commentauthorfieldnrs[fieldNr]).getString(row[data.commentauthorfieldnrs[fieldNr]])
							: "Kettle PDI";
					cell.setCellComment(createCellComment(author, comment));
				}
			}

			// cell is getting a formula value or static content
			if (!isTitle && excelField != null && excelField.isFormula()) {
				// formula case
				cell.setCellFormula(vMeta.getString(v));
			} else {
				// static content case
				switch (vMeta.getType()) {
				case ValueMetaInterface.TYPE_DATE: {
					if (v != null && vMeta.getDate(v) != null) {
						cell.setCellValue(vMeta.getDate(v));
					}
				}
					break;

				case ValueMetaInterface.TYPE_BOOLEAN: {
					if (v != null) {
						cell.setCellValue(vMeta.getBoolean(v));
					}

				}
					break;

				case ValueMetaInterface.TYPE_STRING:
				case ValueMetaInterface.TYPE_BINARY:

				{
					if (v != null) {
						cell.setCellValue(vMeta.getString(v));
					}
				}
					break;

				case ValueMetaInterface.TYPE_BIGNUMBER:
				case ValueMetaInterface.TYPE_NUMBER:
				case ValueMetaInterface.TYPE_INTEGER: {

					if (v != null) {
						cell.setCellValue(vMeta.getNumber(v));
					}

				}
					break;
				default:
					break;
				}

			}

		} catch (Exception e) {
			logError("Error writing field (" + data.posX + "," + data.posY + ") : " + e.toString());
			logError(Const.getStackTracker(e));
			throw new KettleException(e);
		}

	}

	/**
	 * Returns the output filename that belongs to this step observing the file split feature
	 * 
	 * @return current output filename to write to
	 */
	public String buildFilename(int splitNr) {
		return meta.buildFilename(this, getCopy(), splitNr);
	}

	/**
	 * Copies a VFS File
	 * 
	 * @param in
	 *            the source file object
	 * @param out
	 *            the destination file object
	 * @throws KettleException
	 */
	public static void copyFile(FileObject in, FileObject out) throws KettleException {

		BufferedInputStream fis = null;
		BufferedOutputStream fos = null;

		try {

			fis = new BufferedInputStream(KettleVFS.getInputStream(in));
			fos = new BufferedOutputStream(KettleVFS.getOutputStream(out, false));

			byte[] buf = new byte[1024 * 1024]; // copy in chunks of 1 MB
			int i = 0;
			while ((i = fis.read(buf)) != -1) {
				fos.write(buf, 0, i);
			}
			fos.flush();
			fos.close();
			fis.close();
		} catch (Exception e) {
			throw new KettleException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void prepareNextOutputFile() throws KettleException {

		try {

			// clear style cache
			int numOfFields = meta.getOutputFields() != null && meta.getOutputFields().length > 0 ? meta.getOutputFields().length : 0;
			if (numOfFields == 0) {
				numOfFields = data.inputRowMeta != null ? data.inputRowMeta.size() : 0;
			}
			data.clearStyleCache(numOfFields);

			// build new filename
			String buildFilename = buildFilename(data.splitnr);

			data.file = KettleVFS.getFileObject(buildFilename, getTransMeta());

			if (log.isDebug())
				logDebug(BaseMessages.getString(PKG, "ExcelWriterStep.Log.OpeningFile", buildFilename));

			// determine whether existing file must be deleted
			if (data.file.exists() && data.createNewFile) {
				if (!data.file.delete()) {
					if (log.isBasic()) {
						logBasic(BaseMessages.getString(PKG, "ExcelWriterStep.Log.CouldNotDeleteStaleFile", buildFilename));
					}
					setErrors(1);
					throw new KettleException("Could not delete stale file " + buildFilename);
				}
			}

			// adding filename to result
			if (meta.isAddToResultFiles()) {
				// Add this to the result file names...
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname());
				resultFile.setComment("This file was created with an Excel writer step by Pentaho Data Integration");
				addResultFile(resultFile);
			}

			boolean appendingToSheet = true;

			// if now no file exists we must create it as indicated by user
			if (!data.file.exists()) {

				// if template file is enabled
				if (meta.isTemplateEnabled()) {

					// handle template case (must have same format)
					// ensure extensions match
					String templateExt = KettleVFS.getFileObject(data.realTemplateFileName).getName().getExtension();
					if (!meta.getExtension().equalsIgnoreCase(templateExt)) {
						throw new KettleException("Template Format Mismatch: Template has extension: " + templateExt + ", but output file has extension: " + meta.getExtension()
								+ ". Template and output file must share the same format!");
					}

					if (KettleVFS.getFileObject(data.realTemplateFileName).exists()) {
						// if the template exists just copy the template in place
						copyFile(KettleVFS.getFileObject(data.realTemplateFileName, getTransMeta()), data.file);
					} else {
						// template is missing, log it and get out
						if (log.isBasic())
							logBasic(BaseMessages.getString(PKG, "ExcelWriterStep.Log.TemplateMissing", data.realTemplateFileName));
						setErrors(1);
						throw new KettleException("Template file missing: " + data.realTemplateFileName);
					}

				} else {

					// handle fresh file case, just create a fresh workbook
					
					Workbook wb = meta.getExtension().equalsIgnoreCase("xlsx") ? new XSSFWorkbook() : new HSSFWorkbook();
					OutputStream out = KettleVFS.getOutputStream(data.file, false);
					wb.createSheet(data.realSheetname);
					wb.write(out);
					out.close();
				}
				
				appendingToSheet = false;

			}

			// file is guaranteed to be in place now
			if (meta.getExtension().equalsIgnoreCase("xlsx")) {
				data.wb = new XSSFWorkbook(KettleVFS.getInputStream(data.file));
			} else {
				data.wb = new HSSFWorkbook(KettleVFS.getInputStream(data.file));
			}

			int existingActiveSheetIndex = data.wb.getActiveSheetIndex();
			int replacingSheetAt = -1;

			if (data.wb.getSheet(data.realSheetname) != null) {
				// sheet exists, replace or reuse as indicated by user
				if (data.createNewSheet) {
					replacingSheetAt = data.wb.getSheetIndex(data.wb.getSheet(data.realSheetname));
					data.wb.removeSheetAt(replacingSheetAt);
				}
			}

			// if sheet is now missing, we need to create a new one
			if (data.wb.getSheet(data.realSheetname) == null) {

				if (meta.isTemplateSheetEnabled()) {

					Sheet ts = data.wb.getSheet(data.realTemplateSheetName);
					// if template sheet is missing, break
					if (ts == null) {
						throw new KettleException("Tempate Sheet: " + data.realTemplateSheetName + " not found, aborting");
					}
					data.sheet = data.wb.cloneSheet(data.wb.getSheetIndex(ts));
					data.wb.setSheetName(data.wb.getSheetIndex(data.sheet), data.realSheetname);
					// unhide sheet in case it was hidden
					data.wb.setSheetHidden(data.wb.getSheetIndex(data.sheet), false);
				}
				// no template to use, simply create a new sheet
				else {
					data.sheet = data.wb.createSheet(data.realSheetname);
				}

				if (replacingSheetAt > -1) {
					data.wb.setSheetOrder(data.sheet.getSheetName(), replacingSheetAt);
				}

				// preserves active sheet selection in workbook 
				data.wb.setActiveSheet(existingActiveSheetIndex);
				data.wb.setSelectedTab(existingActiveSheetIndex);

				appendingToSheet = false;
			} else {
				// sheet is there and should be reused
				data.sheet = data.wb.getSheet(data.realSheetname);
			}

			// if use chose to make the current sheet active, do so
			if (meta.isMakeSheetActive()) {
				int sheetIndex = data.wb.getSheetIndex(data.sheet);
				data.wb.setActiveSheet(sheetIndex);
				data.wb.setSelectedTab(sheetIndex);
			}

			// handle write protection
			if (meta.isSheetProtected()) {
				// Write protect Sheet by setting password
				// works only for xls output at the moment
				if (data.wb instanceof HSSFWorkbook) {
					((HSSFWorkbook) data.wb).writeProtectWorkbook(data.realPassword, Const.isEmpty(meta.getProtectedBy()) ? "Kettle PDI" : data.realProtectedBy);
				}
			}

			// starting cell support
			data.startingRow = 0;
			data.startingCol = 0;

			if (!Const.isEmpty(data.realStartingCell)) {
				CellReference cellRef = new CellReference(data.realStartingCell);
				data.startingRow = cellRef.getRow();
				data.startingCol = cellRef.getCol();
			}

			data.posX = data.startingCol;
			data.posY = data.startingRow;

			// Find last row and append accordingly
			if (!data.createNewSheet && meta.isAppendLines() && appendingToSheet) {
				data.posY = data.sheet.getLastRowNum();
				if (data.posY > 0) {
					data.posY++;
				}
			}

			// offset by configured value
			// Find last row and append accordingly
			if (!data.createNewSheet && meta.getAppendOffset() != 0 && appendingToSheet) {
				data.posY += meta.getAppendOffset();
			}

			// may have to write a few empty lines 
			if (!data.createNewSheet && meta.getAppendEmpty() > 0 && appendingToSheet) {
				for (int i = 0; i < meta.getAppendEmpty(); i++) {
					openLine();
					if (!data.shiftExistingCells || meta.isAppendLines()) {
						data.posY++;
					}
				}
			}

			// may have to write a header here
			if (meta.isHeaderEnabled() && !(!data.createNewSheet && meta.isAppendOmitHeader() && appendingToSheet)) {
				writeHeader();
			}

			if (log.isDebug())
				logDebug(BaseMessages.getString(PKG, "ExcelWriterStep.Log.FileOpened", buildFilename));

			// this is the number of the new output file
			data.splitnr++;

		} catch (Exception e) {
			logError("Error opening new file", e);
			setErrors(1);
			throw new KettleException(e);
		}

	}

	private void openLine() {
		if (data.shiftExistingCells) {
			data.sheet.shiftRows(data.posY, Math.max(data.posY, data.sheet.getLastRowNum()), 1);
		}
	}

	private void writeHeader() throws KettleException {

		try {

			openLine();

			Row xlsRow = data.sheet.getRow(data.posY);
			if (xlsRow == null) {
				xlsRow = data.sheet.createRow(data.posY);
			}

			int posX = data.posX;

			// If we have fields specified: list them in this order!
			if (meta.getOutputFields() != null && meta.getOutputFields().length > 0) {
				for (int i = 0; i < meta.getOutputFields().length; i++) {
					String fieldName = !Const.isEmpty(meta.getOutputFields()[i].getTitle()) ? meta.getOutputFields()[i].getTitle() : meta.getOutputFields()[i].getName();

					ValueMetaInterface vMeta = new ValueMeta(fieldName, ValueMetaInterface.TYPE_STRING);

					writeField(fieldName, vMeta, meta.getOutputFields()[i], xlsRow, posX++, null, -1, true);
				}
				// Just put all field names in	
			} else if (data.inputRowMeta != null) {
				for (int i = 0; i < data.inputRowMeta.size(); i++) {
					String fieldName = data.inputRowMeta.getFieldNames()[i];
					ValueMetaInterface vMeta = new ValueMeta(fieldName, ValueMetaInterface.TYPE_STRING);
					writeField(fieldName, vMeta, null, xlsRow, posX++, null, -1, true);
				}
			}

			data.posY++;
			incrementLinesOutput();

		} catch (Exception e) {
			throw new KettleException(e);
		}

	}

	/**
	 * transformation run initialize, may create the output file if specified by user options
	 * 
	 * @see org.pentaho.di.trans.step.BaseStep#init(org.pentaho.di.trans.step.StepMetaInterface, org.pentaho.di.trans.step.StepDataInterface)
	 * 
	 */
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (ExcelWriterStepMeta) smi;
		data = (ExcelWriterStepData) sdi;

		if (super.init(smi, sdi)) {

			data.splitnr = 0;
			data.datalines = 0;
			data.realSheetname = environmentSubstitute(meta.getSheetname());
			data.realTemplateSheetName = environmentSubstitute(meta.getTemplateSheetName());
			data.realTemplateFileName = environmentSubstitute(meta.getTemplateFileName());
			data.realStartingCell = environmentSubstitute(meta.getStartingCell());
			data.realPassword = environmentSubstitute(meta.getPassword());
			data.realProtectedBy = environmentSubstitute(meta.getProtectedBy());

			data.shiftExistingCells = ExcelWriterStepMeta.ROW_WRITE_PUSH_DOWN.equals(meta.getRowWritingMethod());
			data.createNewSheet = ExcelWriterStepMeta.IF_SHEET_EXISTS_CREATE_NEW.equals(meta.getIfSheetExists());
			data.createNewFile = ExcelWriterStepMeta.IF_FILE_EXISTS_CREATE_NEW.equals(meta.getIfFileExists());

			// if we are supposed to init the file up front, here we go
			if (!meta.isDoNotOpenNewFileInit()) {
				data.firstFileOpened = true;

				try {
					prepareNextOutputFile();
				} catch (KettleException e) {
					e.printStackTrace();
					logError("Couldn't prepare output file " + meta.getFileName());
					setErrors(1L);
					stopAll();

				}
			}
			return true;
		}

		return false;

	}

	/**
	 * transformation run end
	 * 
	 * @see org.pentaho.di.trans.step.BaseStep#dispose(org.pentaho.di.trans.step.StepMetaInterface, org.pentaho.di.trans.step.StepDataInterface)
	 * 
	 */
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (ExcelWriterStepMeta) smi;
		data = (ExcelWriterStepData) sdi;

		super.dispose(smi, sdi);
	}

}
