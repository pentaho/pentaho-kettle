/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.core.gui;

import org.pentaho.di.core.gui.Point;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.trans.step.StepMeta;

public interface GCInterface {
	
	public enum EColor { BACKGROUND, BLACK, RED, YELLOW, ORANGE, GREEN, BLUE, MAGENTA, GRAY, LIGHTGRAY, DARKGRAY, }
	public enum EFont { NOTE, GRAPH, SMALL, }
	public enum ELineStyle { SOLID, DASHDOT, DOT, PARALLEL, }
	public enum EImage { LOCK, STEP_ERROR, EDIT, CONTEXT_MENU, TRUE, FALSE, ERROR, INFO, TARGET, INPUT, OUTPUT, ARROW, COPY_ROWS, 
		UNCONDITIONAL, PARALLEL, BUSY, }
	
	// TODO: Parallel and Unconditional!!!
	
	public void setLineWidth(int width);
	public void setFont(EFont font);
	
	public Point textExtent(String text);
	public Point getDeviceBounds();
	
	public void setBackground(EColor color);
	public void setForeground(EColor color);
	
	public void setBackground(int red, int green, int blue);
	public void setForeground(int red, int green, int blue);
	
	// public EColor getBackground();
	// public EColor getForeground();

	public void fillRectangle(int x, int y, int width, int height);
	public void drawImage(EImage image, int x, int y);
	public void drawLine(int x, int y, int x2, int y2);
	public void setLineStyle(ELineStyle lineStyle);
	public void drawRectangle(int x, int  y, int width, int height);
	public void drawPoint(int x, int y);
	public void drawText(String text, int x, int y);
	public void drawText(String text, int x, int y, boolean transparent);
	public void fillRoundRectangle(int x, int y, int width, int height, int circleWidth, int circleHeight);
	public void drawRoundRectangle(int x, int y, int width, int height, int circleWidth, int circleHeight);
	
	public void fillPolygon(int[] polygon);
	public void drawPolygon(int[] polygon);
	public void drawPolyline(int[] polyline);
	
	public void drawJobEntryIcon(int x, int y, JobEntryCopy jobEntryCopy);
	public void drawStepIcon(int x, int y, StepMeta stepMeta);
	
	public void setAntialias(boolean antiAlias);

	public void setTransform(float translationX, float translationY, int shadowsize, float magnification);
	
	public void setAlpha(int alpha);
	
	public void dispose();
	public int getAlpha();
	public void setFont(String fontName, int fontSize, boolean fontBold, boolean fontItalic);
	
	public Object getImage();
	public Point getImageBounds(EImage eImage);
	public void switchForegroundBackgroundColors();
	public Point getArea();
}
