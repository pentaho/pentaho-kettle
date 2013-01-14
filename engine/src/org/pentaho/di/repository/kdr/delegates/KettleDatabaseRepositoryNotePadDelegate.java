/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.repository.kdr.delegates;

import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

public class KettleDatabaseRepositoryNotePadDelegate extends KettleDatabaseRepositoryBaseDelegate {
	// private static Class<?> PKG = NotePadMeta.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

	public KettleDatabaseRepositoryNotePadDelegate(KettleDatabaseRepository repository) {
		super(repository);
	}
	
	public synchronized ObjectId getNoteID(String note) throws KettleException
	{
		return repository.connectionDelegate.getIDWithValue(quoteTable(KettleDatabaseRepository.TABLE_R_NOTE), quote(KettleDatabaseRepository.FIELD_NOTE_ID_NOTE), quote(KettleDatabaseRepository.FIELD_NOTE_VALUE_STR), note);
	}
	
	public RowMetaAndData getNote(ObjectId id_note) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_NOTE), quote(KettleDatabaseRepository.FIELD_NOTE_ID_NOTE), id_note);
	}

	public NotePadMeta loadNotePadMeta(ObjectId id_note) throws KettleException {
		NotePadMeta note = new NotePadMeta();
		try {
			note.setObjectId(id_note);

			RowMetaAndData r = getNote(id_note);
			if (r != null) {
				note.setNote(r.getString("VALUE_STR", ""));
				int x = (int) r.getInteger("GUI_LOCATION_X", 0L);
				int y = (int) r.getInteger("GUI_LOCATION_Y", 0L);
				note.setLocation(new Point(x, y));
				note.setWidth((int) r.getInteger("GUI_LOCATION_WIDTH", 0L));
				note.setHeight((int) r.getInteger("GUI_LOCATION_HEIGHT", 0L));
				note.setSelected(false);

				// Font
				note.setFontName(r.getString("FONT_NAME", null));
				note.setFontSize((int) r.getInteger("FONT_SIZE", -1));
				note.setFontBold(r.getBoolean("FONT_BOLD", false));
				note.setFontItalic(r.getBoolean("FONT_ITALIC", false));

				// Font color
				note.setFontColorRed((int) r.getInteger("FONT_COLOR_RED", NotePadMeta.COLOR_RGB_BLACK_BLUE));
				note.setFontColorGreen((int) r.getInteger("FONT_COLOR_GREEN", NotePadMeta.COLOR_RGB_BLACK_GREEN));
				note.setFontColorBlue((int) r.getInteger("FONT_COLOR_BLUE", NotePadMeta.COLOR_RGB_BLACK_BLUE));

				// Background color
				note.setBackGroundColorRed((int) r.getInteger("FONT_BACK_GROUND_COLOR_RED", NotePadMeta.COLOR_RGB_DEFAULT_BG_RED));
				note.setBackGroundColorGreen((int) r.getInteger("FONT_BACK_GROUND_COLOR_GREEN", NotePadMeta.COLOR_RGB_DEFAULT_BG_GREEN));
				note.setBackGroundColorBlue((int) r.getInteger("FONT_BACK_GROUND_COLOR_BLUE", NotePadMeta.COLOR_RGB_DEFAULT_BG_BLUE));

				// Border color
				note.setBorderColorRed((int) r.getInteger("FONT_BORDER_COLOR_RED", NotePadMeta.COLOR_RGB_DEFAULT_BORDER_RED));
				note.setBorderColorGreen((int) r.getInteger("FONT_BORDER_COLOR_GREEN", NotePadMeta.COLOR_RGB_DEFAULT_BORDER_GREEN));
				note.setBorderColorBlue((int) r.getInteger("FONT_BORDER_COLOR_BLUE", NotePadMeta.COLOR_RGB_DEFAULT_BORDER_BLUE));
				note.setDrawShadow(r.getBoolean("DRAW_SHADOW", true));

				// Done!
				return note;
			} else {
				note.setObjectId(null);
				throw new KettleException("I couldn't find Notepad with id_note=" + id_note + " in the repository.");
			}
		} catch (KettleDatabaseException dbe) {
			note.setObjectId(null);
			throw new KettleException("Unable to load Notepad from repository (id_note=" + id_note + ")", dbe);
		}
	}

	/**
	 * 
	 * @param rep
	 * @param id_transformation
	 * @throws KettleException
	 */
	public void saveNotePadMeta(NotePadMeta note, ObjectId id_transformation) throws KettleException {
		try {
			Point location = note.getLocation();
			int x = location == null ? -1 : location.x;
			int y = location == null ? -1 : location.y;

			// Insert new Note in repository
			note.setObjectId(insertNote(note.getNote(), x, y, note.getWidth(), note.getHeight(), note.getFontName(), note.getFontSize(), note.isFontBold(), note.isFontItalic(), note.getFontColorRed(), note.getFontColorGreen(), note
					.getFontColorBlue(), note.getBackGroundColorRed(), note.getBackGroundColorGreen(), note.getBackGroundColorBlue(), note.getBorderColorRed(), note.getBorderColorGreen(), note.getBorderColorBlue(), note.isDrawShadow()));
		} catch (KettleDatabaseException dbe) {
			throw new KettleException("Unable to save notepad in repository (id_transformation=" + id_transformation + ")", dbe);
		}
	}

	public synchronized ObjectId insertNote(String note, long gui_location_x, long gui_location_y, long gui_location_width, long gui_location_height
			,String fontname, long fontsize,boolean fontbold, boolean fontitalic,
			long fontcolorred, long fontcolorgreen, long fontcolorblue,
			long fontbackcolorred, long fontbackcolorgreen, long fontbackcolorblue,
			long fontbordercolorred, long fontbordercolorgreen, long fontbordercolorblue,boolean drawshadow) throws KettleException
	{
		ObjectId id = repository.connectionDelegate.getNextNoteID();

		RowMetaAndData table = new RowMetaAndData();

		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_ID_NOTE, ValueMetaInterface.TYPE_INTEGER), id);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_VALUE_STR, ValueMetaInterface.TYPE_STRING), note);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_GUI_LOCATION_X, ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_x));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_GUI_LOCATION_Y, ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_y));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_GUI_LOCATION_WIDTH, ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_width));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_GUI_LOCATION_HEIGHT, ValueMetaInterface.TYPE_INTEGER), new Long(gui_location_height));
		// Font
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_FONT_NAME, ValueMetaInterface.TYPE_STRING), fontname);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_FONT_SIZE, ValueMetaInterface.TYPE_INTEGER), fontsize);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_FONT_BOLD, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(fontbold));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_FONT_ITALIC, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(fontitalic));
		// Font color
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_COLOR_RED, ValueMetaInterface.TYPE_INTEGER), new Long(fontcolorred));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_COLOR_GREEN, ValueMetaInterface.TYPE_INTEGER), new Long(fontcolorgreen));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_COLOR_BLUE, ValueMetaInterface.TYPE_INTEGER), new Long(fontcolorblue));
		// Font background color
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_BACK_GROUND_COLOR_RED, ValueMetaInterface.TYPE_INTEGER), new Long(fontbackcolorred));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_BACK_GROUND_COLOR_GREEN, ValueMetaInterface.TYPE_INTEGER), new Long(fontbackcolorgreen));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_BACK_GROUND_COLOR_BLUE, ValueMetaInterface.TYPE_INTEGER), new Long(fontbackcolorblue));
		// Font border color
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_BORDER_COLOR_RED, ValueMetaInterface.TYPE_INTEGER), new Long(fontbordercolorred));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_BORDER_COLOR_GREEN, ValueMetaInterface.TYPE_INTEGER), new Long(fontbordercolorgreen));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_BORDER_COLOR_BLUE, ValueMetaInterface.TYPE_INTEGER),new Long(fontbordercolorblue));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_NOTE_DRAW_SHADOW, ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(drawshadow));
		
		repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), KettleDatabaseRepository.TABLE_R_NOTE);
		repository.connectionDelegate.getDatabase().setValuesInsert(table);
		repository.connectionDelegate.getDatabase().insertRow();
		repository.connectionDelegate.getDatabase().closeInsert();

		return id;
	}
    
}
