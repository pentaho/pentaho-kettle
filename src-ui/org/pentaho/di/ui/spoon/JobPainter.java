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
package org.pentaho.di.ui.spoon;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.gui.AreaOwner;
import org.pentaho.di.core.gui.BasePainter;
import org.pentaho.di.core.gui.GCInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.Rectangle;
import org.pentaho.di.core.gui.ScrollBarInterface;
import org.pentaho.di.core.gui.AreaOwner.AreaType;
import org.pentaho.di.core.gui.GCInterface.EColor;
import org.pentaho.di.core.gui.GCInterface.EFont;
import org.pentaho.di.core.gui.GCInterface.EImage;
import org.pentaho.di.core.gui.GCInterface.ELineStyle;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;

public class JobPainter extends BasePainter {

	private JobMeta						jobMeta;
	private JobHopMeta					candidate;

	private List<JobEntryCopy>			mouseOverEntries;
	private Map<JobEntryCopy, String>	entryLogMap;
	private JobEntryCopy	startHopEntry;
	private Point	endHopLocation;
	private JobEntryCopy	endHopEntry;
	private JobEntryCopy	noInputEntry;
	private List<JobEntryCopy>	activeJobEntries;
	private List<JobEntryResult> jobEntryResults;

	public JobPainter(GCInterface gc, JobMeta jobMeta, Point area, ScrollBarInterface hori, ScrollBarInterface vert, JobHopMeta candidate, Point drop_candidate, Rectangle selrect, List<AreaOwner> areaOwners, List<JobEntryCopy> mouseOverEntries,
			int iconsize, int linewidth, int gridsize, int shadowSize, boolean antiAliasing, 
    		String noteFontName, int noteFontHeight) {
		super(gc, jobMeta, area, hori, vert, drop_candidate, selrect, areaOwners,
				iconsize, linewidth, gridsize, shadowSize, antiAliasing, 
        		noteFontName, noteFontHeight
			);
		this.jobMeta = jobMeta;

		this.candidate = candidate;

		this.mouseOverEntries = mouseOverEntries;

		entryLogMap = null;
	}

	public void drawJob() {

		Point max = jobMeta.getMaximum();
		Point thumb = getThumb(area, max);
		offset = getOffset(thumb, area);

		gc.setBackground(EColor.BACKGROUND);

		hori.setThumb(thumb.x);
		vert.setThumb(thumb.y);

		// If there is a shadow, we draw the transformation first with an alpha
		// setting
		//
		if (shadowSize > 0) {
			gc.setAlpha(20);
			gc.setTransform(translationX, translationY, shadowSize, magnification);
			shadow = true;
			drawJobElements();
		}

		// Draw the transformation onto the image
		//
		gc.setAlpha(255);
		gc.setTransform(translationX, translationY, 0, magnification);

		shadow = false;
		drawJobElements();

	}

	private void drawJobElements() {
		if (!shadow && gridSize > 1) {
			drawGrid();
		}

		// First draw the notes...
		gc.setFont(EFont.NOTE);

		for (int i = 0; i < jobMeta.nrNotes(); i++) {
			NotePadMeta ni = jobMeta.getNote(i);
			drawNote(ni);
		}

		gc.setFont(EFont.GRAPH);

		// ... and then the rest on top of it...
		for (int i = 0; i < jobMeta.nrJobHops(); i++) {
			JobHopMeta hi = jobMeta.getJobHop(i);
			drawJobHop(hi, false);
		}

		if (candidate != null) {
			drawJobHop(candidate, true);
		} else {
			if (startHopEntry != null && endHopLocation != null) {
				Point fr = startHopEntry.getLocation();
				Point to = endHopLocation;
				if (endHopEntry == null) {
					gc.setForeground(EColor.GRAY);
				} else {
					gc.setForeground(EColor.BLUE);
				}
	        	Point start = real2screen(fr.x+iconsize/2, fr.y+iconsize/2);
	        	Point end = real2screen(to.x, to.y);
				drawArrow(start.x, start.y, end.x, end.y, theta, calcArrowLength(), 1.2, null, startHopEntry, endHopEntry == null ? endHopLocation : endHopEntry);
			} else if (endHopEntry != null && endHopLocation != null) {
				Point fr = endHopLocation;
				Point to = endHopEntry.getLocation();
				if (startHopEntry == null) {
					gc.setForeground(EColor.GRAY);
				} else {
					gc.setForeground(EColor.BLUE);
				}
	        	Point start = real2screen(fr.x, fr.y);
	        	Point end = real2screen(to.x+iconsize/2, to.y+iconsize/2);
				drawArrow(start.x, start.y, end.x, end.y + iconsize / 2, theta, calcArrowLength(), 1.2, null, startHopEntry == null ? endHopLocation : startHopEntry, endHopEntry);
			}
		}		

		for (int j = 0; j < jobMeta.nrJobEntries(); j++) {
			JobEntryCopy je = jobMeta.getJobEntry(j);
			drawJobEntryCopy(je);
		}
		
        // Display an icon on the indicated location signaling to the user that the step in question does not accept input 
        //
        if (noInputEntry!=null) {
        	gc.setLineWidth(2);	
        	gc.setForeground(EColor.RED);
        	Point n = noInputEntry.getLocation();
        	gc.drawLine(offset.x + n.x-5, offset.y + n.y-5, offset.x + n.x+iconsize+5, offset.y + n.y+iconsize+5);
        	gc.drawLine(offset.x + n.x-5, offset.y + n.y+iconsize+5, offset.x + n.x+iconsize+5, offset.y + n.y-5);
        }


		if (drop_candidate != null) {
			gc.setLineStyle(ELineStyle.SOLID);
			gc.setForeground(EColor.BLACK);
			Point screen = real2screen(drop_candidate.x, drop_candidate.y);
			gc.drawRectangle(screen.x, screen.y, iconsize, iconsize);
		}

		if (!shadow) {
			drawRect(selrect);
		}
	}

	protected void drawJobEntryCopy(JobEntryCopy jobEntryCopy) {
		if (!jobEntryCopy.isDrawn())
			return;

        int alpha = gc.getAlpha();

		Point pt = jobEntryCopy.getLocation();
		if (pt==null) {
			pt = new Point(50,50);
		}
		
		Point screen = real2screen(pt.x, pt.y);
		int x=screen.x;
		int y=screen.y;
		
		String name = jobEntryCopy.getName();
		if (jobEntryCopy.isSelected())
			gc.setLineWidth(3);
		else
			gc.setLineWidth(1);

		gc.drawJobEntryIcon(x, y, jobEntryCopy);
		gc.setBackground(EColor.BACKGROUND);
		
		if (activeJobEntries!=null && activeJobEntries.contains(jobEntryCopy)) {
			gc.setForeground(EColor.BLUE);
			int iconX = x + iconsize - 7;
			int iconY = y - 7;
			gc.drawImage(EImage.BUSY, iconX, iconY);
			areaOwners.add(new AreaOwner(AreaType.JOB_ENTRY_BUSY, iconX, iconY, iconsize, iconsize,  offset, subject, jobEntryCopy));
		} else {
			gc.setForeground(EColor.BLACK);
		}
		
		JobEntryResult jobEntryResult = findJobEntryResult(jobEntryCopy);
		if (jobEntryResult!=null) {
			Result result = jobEntryResult.getResult();
			int iconX = x + iconsize - 7;
			int iconY = y - 7;
			if (result.getResult()) {
				gc.drawImage(EImage.TRUE, iconX, iconY);
				areaOwners.add(new AreaOwner(AreaType.JOB_ENTRY_RESULT_SUCCESS, iconX, iconY, iconsize, iconsize, offset, jobEntryCopy, jobEntryResult));
			} else {
				gc.drawImage(EImage.FALSE, x + iconsize, y - 5 );
				areaOwners.add(new AreaOwner(AreaType.JOB_ENTRY_RESULT_FAILURE, iconX, iconY, iconsize, iconsize,  offset, jobEntryCopy, jobEntryResult));
			}
		}
		
		gc.drawRectangle(x - 1, y - 1, iconsize + 1, iconsize + 1);
		Point textsize = new Point(gc.textExtent("" + name).x, gc.textExtent("" + name).y);

		if (!shadow) {
			areaOwners.add(new AreaOwner(AreaType.JOB_ENTRY_ICON, x, y, iconsize, iconsize, offset, subject, jobEntryCopy));
		}
		
		gc.setBackground(EColor.BACKGROUND);
		gc.setLineWidth(1);

		int xpos = x + (iconsize / 2) - (textsize.x / 2);
		int ypos = y + iconsize + 5;

		gc.setForeground(EColor.BLACK);
		gc.drawText(name, xpos, ypos, true);
		
		
        // Optionally drawn the mouse-over information
        //
        if (mouseOverEntries.contains(jobEntryCopy)) {
        	EImage[] miniIcons = new EImage[] { EImage.INPUT, EImage.EDIT, EImage.CONTEXT_MENU, EImage.OUTPUT, };
        	
        	// First drawn the mini-icons balloon below the job entry
        	//
        	int totalHeight=0;
        	int totalIconsWidth=0;
        	int totalWidth=2*MINI_ICON_MARGIN;
        	for (EImage miniIcon : miniIcons) {
        		Point bounds = gc.getImageBounds(miniIcon);
        		totalWidth+=bounds.x+MINI_ICON_MARGIN;
        		totalIconsWidth+=bounds.x+MINI_ICON_MARGIN;
        		if (bounds.y>totalHeight) totalHeight=bounds.y;
        	}
        	totalHeight+=2*MINI_ICON_MARGIN;
        	        	
        	gc.setFont(EFont.SMALL);
        	String trimmedName = jobEntryCopy.getName().length()<30 ? jobEntryCopy.getName() : jobEntryCopy.getName().substring(0,30);
        	Point nameExtent = gc.textExtent(trimmedName);
        	nameExtent.y+=2*MINI_ICON_MARGIN;
        	nameExtent.x+=3*MINI_ICON_MARGIN;
        	totalHeight+=nameExtent.y;
        	if (nameExtent.x>totalWidth) totalWidth=nameExtent.x;

        	int areaX = x+iconsize/2-totalWidth/2+MINI_ICON_SKEW;
        	int areaY = y+iconsize+MINI_ICON_DISTANCE;

        	gc.setForeground(EColor.DARKGRAY);
        	gc.setBackground(EColor.LIGHTGRAY);
        	gc.setLineWidth(1);
        	gc.fillRoundRectangle(areaX, areaY, totalWidth, totalHeight, 7, 7);
        	gc.drawRoundRectangle(areaX, areaY, totalWidth, totalHeight, 7, 7);

        	gc.setBackground(EColor.BACKGROUND);
        	gc.fillRoundRectangle(areaX+2, areaY+2, totalWidth-MINI_ICON_MARGIN+1, nameExtent.y-MINI_ICON_MARGIN, 7, 7);
        	gc.setForeground(EColor.BLACK);
        	gc.drawText(trimmedName, areaX+(totalWidth-nameExtent.x)/2+MINI_ICON_MARGIN, areaY+MINI_ICON_MARGIN, true);
        	gc.setForeground(EColor.DARKGRAY);
        	gc.setBackground(EColor.LIGHTGRAY);

        	gc.setFont(EFont.GRAPH);
        	areaOwners.add(new AreaOwner(AreaType.MINI_ICONS_BALLOON, areaX, areaY, totalWidth, totalHeight, offset, jobMeta, jobEntryCopy));
        	
        	gc.fillPolygon(new int[] { areaX+totalWidth/2-MINI_ICON_TRIANGLE_BASE/2+1, areaY+2, areaX+totalWidth/2+MINI_ICON_TRIANGLE_BASE/2, areaY+2, areaX+totalWidth/2-MINI_ICON_SKEW, areaY-MINI_ICON_DISTANCE-5, });
        	gc.drawPolyline(new int[] { areaX+totalWidth/2-MINI_ICON_TRIANGLE_BASE/2+1, areaY, areaX+totalWidth/2-MINI_ICON_SKEW, areaY-MINI_ICON_DISTANCE-5, areaX+totalWidth/2+MINI_ICON_TRIANGLE_BASE/2, areaY, areaX+totalWidth/2-MINI_ICON_SKEW, areaY-MINI_ICON_DISTANCE-5, });
        	gc.setBackground(EColor.BACKGROUND);
        	
        	// Put on the icons...
        	//
        	int xIcon = areaX+(totalWidth-totalIconsWidth)/2+MINI_ICON_MARGIN;
        	int yIcon = areaY+5+nameExtent.y;
        	for (int i=0;i<miniIcons.length;i++) {
        		EImage miniIcon = miniIcons[i];
        		Point bounds = gc.getImageBounds(miniIcon);
        		boolean enabled=false;
        		switch(i) {
        		case 0: // INPUT
        			enabled=!jobEntryCopy.isStart();
                	areaOwners.add(new AreaOwner(AreaType.JOB_ENTRY_MINI_ICON_INPUT, xIcon, yIcon, bounds.x, bounds.y, offset, jobMeta, jobEntryCopy));
        			break;
        		case 1: // EDIT
        			enabled=true;
                	areaOwners.add(new AreaOwner(AreaType.JOB_ENTRY_MINI_ICON_EDIT, xIcon, yIcon, bounds.x, bounds.y, offset, jobMeta, jobEntryCopy));
        			break;
        		case 2: // Job entry context menu
        			enabled=true;
        			areaOwners.add(new AreaOwner(AreaType.JOB_ENTRY_MINI_ICON_CONTEXT, xIcon, yIcon, bounds.x, bounds.y, offset, jobMeta, jobEntryCopy));
                	break;
        		case 3: // OUTPUT
        			enabled=true;
                	areaOwners.add(new AreaOwner(AreaType.JOB_ENTRY_MINI_ICON_OUTPUT, xIcon, yIcon, bounds.x, bounds.y, offset, jobMeta, jobEntryCopy));
        			break;
        		}
        		if (enabled) {
        			gc.setAlpha(255);
        		} else {
        			gc.setAlpha(100);
        		}
        		gc.drawImage(miniIcon, xIcon, yIcon);
        		xIcon+=bounds.x+5;
        	}        	
        }        

        // Restore the previous alpha value
        //
        gc.setAlpha(alpha);
	}

	private JobEntryResult findJobEntryResult(JobEntryCopy jobEntryCopy) {
		if (jobEntryResults==null) return null;
		for (JobEntryResult jobEntryResult : jobEntryResults) {
			if (jobEntryResult.getJobEntryName().equals(jobEntryCopy.getName()) && 
				jobEntryResult.getJobEntryNr() == jobEntryCopy.getNr()
			   )  {
				return jobEntryResult;
			}
		}
		
		return null;
	}

	protected void drawJobHop(JobHopMeta hop, boolean candidate) {
		if (hop == null || hop.getFromEntry() == null || hop.getToEntry() == null)
			return;
		if (!hop.getFromEntry().isDrawn() || !hop.getToEntry().isDrawn())
			return;

		drawLine(hop, candidate);
	}

	protected void drawLine(JobHopMeta jobHop, boolean is_candidate) {
		int line[] = getLine(jobHop.getFromEntry(), jobHop.getToEntry());

		gc.setLineWidth(linewidth);
		EColor col;

		if (jobHop.getFromEntry().isLaunchingInParallel()) {
			gc.setLineStyle(ELineStyle.PARALLEL);
		} else {
			gc.setLineStyle(ELineStyle.SOLID);
		}

		if (is_candidate) {
			col = EColor.BLUE;
		} else if (jobHop.isEnabled()) {
			if (jobHop.isUnconditional()) {
				col = EColor.BLACK;
			} else {
				if (jobHop.getEvaluation()) {
					col = EColor.GREEN;
				} else {
					col = EColor.RED;
				}
			}
		} else {
			col = EColor.GRAY;
		}

		gc.setForeground(col);

		if (jobHop.isSplit())
			gc.setLineWidth(linewidth + 2);
		drawArrow(line, jobHop);
		if (jobHop.isSplit())
			gc.setLineWidth(linewidth);

		gc.setForeground(EColor.BLACK);
		gc.setBackground(EColor.BACKGROUND);
		gc.setLineStyle(ELineStyle.SOLID);
	}

	protected int[] getLine(JobEntryCopy fs, JobEntryCopy ts) {
		if (fs == null || ts == null)
			return null;

		Point from = fs.getLocation();
		Point to = ts.getLocation();

		int x1 = from.x + iconsize / 2;
		int y1 = from.y + iconsize / 2;

		int x2 = to.x + iconsize / 2;
		int y2 = to.y + iconsize / 2;

		return new int[] { x1, y1, x2, y2 };
	}
	
	private void drawArrow(int line[], JobHopMeta jobHop) {
		drawArrow(line, jobHop, jobHop.getFromEntry(), jobHop.getToEntry());
	}
	
    private void drawArrow(int line[], JobHopMeta jobHop, Object startObject, Object endObject)
    {
    	Point screen_from = real2screen(line[0], line[1]);
        Point screen_to = real2screen(line[2], line[3]);
        
        drawArrow(screen_from.x, screen_from.y, screen_to.x, screen_to.y, theta, calcArrowLength(), -1, jobHop, startObject, endObject);
    }


    private void drawArrow(int x1, int y1, int x2, int y2, double theta, int size, double factor, JobHopMeta jobHop, Object startObject, Object endObject) 
    {
 		int mx, my;
		int x3;
		int y3;
		int x4;
		int y4;
		int a, b, dist;
		double angle;

		// gc.setLineWidth(1);
		// WuLine(gc, black, x1, y1, x2, y2);

		gc.drawLine(x1, y1, x2, y2);

		// What's the distance between the 2 points?
		a = Math.abs(x2 - x1);
		b = Math.abs(y2 - y1);
		dist = (int) Math.sqrt(a * a + b * b);

        // determine factor (position of arrow to left side or right side
        // 0-->100%)
        if (factor<0)
        {
	        if (dist >= 2 * iconsize)
	             factor = 1.3;
	        else
	             factor = 1.2;
        }

		// in between 2 points
		mx = (int) (x1 + factor * (x2 - x1) / 2);
		my = (int) (y1 + factor * (y2 - y1) / 2);

		// calculate points for arrowhead
		angle = Math.atan2(y2 - y1, x2 - x1) + Math.PI;

		x3 = (int) (mx + Math.cos(angle - theta) * size);
		y3 = (int) (my + Math.sin(angle - theta) * size);

		x4 = (int) (mx + Math.cos(angle + theta) * size);
		y4 = (int) (my + Math.sin(angle + theta) * size);

		gc.switchForegroundBackgroundColors();
		gc.fillPolygon(new int[] { mx, my, x3, y3, x4, y4 });
		gc.switchForegroundBackgroundColors();
		
		// Display an icon above the hop...
		//
		factor = 0.8;

		// in between 2 points
		mx = (int) (x1 + factor * (x2 - x1) / 2) - 8;
		my = (int) (y1 + factor * (y2 - y1) / 2) - 8;

		if (jobHop!=null) {
			EImage hopsIcon;
			if (jobHop.isUnconditional()) {
				hopsIcon = EImage.UNCONDITIONAL;
			} else {
				if (jobHop.getEvaluation()) {
					hopsIcon = EImage.TRUE;
				} else {
					hopsIcon = EImage.FALSE;
				}
			}
	
			Point bounds = gc.getImageBounds(hopsIcon);
			gc.drawImage(hopsIcon, mx, my);
			if (!shadow) {
				areaOwners.add(new AreaOwner(AreaType.JOB_HOP_ICON, mx, my, bounds.x, bounds.y, offset, subject, jobHop));
			}
			
			if (jobHop.getFromEntry().isLaunchingInParallel()) {
	
				factor = 1;
	
				// in between 2 points
				mx = (int) (x1 + factor * (x2 - x1) / 2) - 8;
				my = (int) (y1 + factor * (y2 - y1) / 2) - 8;
	
				hopsIcon = EImage.PARALLEL;
				gc.drawImage(hopsIcon, mx, my);
				if (!shadow) {
					areaOwners.add(new AreaOwner(AreaType.JOB_HOP_PARALLEL_ICON, mx, my, bounds.x, bounds.y, offset, subject, jobHop));
				}
			}	
		}
	}

	/**
	 * @return the mouseOverEntries
	 */
	public List<JobEntryCopy> getMouseOverEntries() {
		return mouseOverEntries;
	}

	/**
	 * @param mouseOverEntries
	 *            the mouseOverEntries to set
	 */
	public void setMouseOverEntries(List<JobEntryCopy> mouseOverEntries) {
		this.mouseOverEntries = mouseOverEntries;
	}

	/**
	 * @return the entryLogMap
	 */
	public Map<JobEntryCopy, String> getEntryLogMap() {
		return entryLogMap;
	}

	/**
	 * @param entryLogMap
	 *            the entryLogMap to set
	 */
	public void setEntryLogMap(Map<JobEntryCopy, String> entryLogMap) {
		this.entryLogMap = entryLogMap;
	}

	public void setStartHopEntry(JobEntryCopy startHopEntry) {
		this.startHopEntry = startHopEntry;
	}

	public void setEndHopLocation(Point endHopLocation) {
		this.endHopLocation = endHopLocation;
	}

	public void setEndHopEntry(JobEntryCopy endHopEntry) {
		this.endHopEntry = endHopEntry;
	}

	public void setNoInputEntry(JobEntryCopy noInputEntry) {
		this.noInputEntry = noInputEntry;
	}

	public void setActiveJobEntries(List<JobEntryCopy> activeJobEntries) {
		this.activeJobEntries = activeJobEntries;
	}

	/**
	 * @return the jobEntryResults
	 */
	public List<JobEntryResult> getJobEntryResults() {
		return jobEntryResults;
	}

	/**
	 * @param jobEntryResults Sets AND sorts the job entry results by name and number
	 */
	public void setJobEntryResults(List<JobEntryResult> jobEntryResults) {
		this.jobEntryResults = jobEntryResults;
		Collections.sort(this.jobEntryResults);
	}

}
