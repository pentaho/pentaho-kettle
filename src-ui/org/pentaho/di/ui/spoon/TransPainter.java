/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ScrollBar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.ui.spoon.AreaOwner;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.ui.core.PropsUI;




public class TransPainter
{
    public static final String STRING_PARTITIONING_CURRENT_STEP = "PartitioningCurrentStep"; // $NON-NLS-1$
    public static final String STRING_PARTITIONING_CURRENT_NEXT = "PartitioningNextStep";    // $NON-NLS-1$
	public static final String STRING_REMOTE_INPUT_STEPS        = "RemoteInputSteps";        // $NON-NLS-1$
	public static final String STRING_REMOTE_OUTPUT_STEPS       = "RemoteOutputSteps";       // $NON-NLS-1$
    
	private PropsUI      props;
    private int          shadowsize;
    private Point        area;
    private TransMeta    transMeta;
    private ScrollBar    hori, vert;

    private Point        offset;

    private Color        background;
    private Color        black;
    private Color        red;
    private Color        yellow;
    private Color        orange;
    private Color        green;
    private Color        blue;
    private Color        magenta;
    private Color        gray;
    private Color        lightGray;
    private Color        darkGray;

    private Font         noteFont;
    private Font         graphFont;

    private TransHopMeta candidate;
    private Point        drop_candidate;
    private int          iconsize;
    private Rectangle    selrect;
    private int          linewidth;
    private Map<String, Image> images;
    
    private List<AreaOwner> areaOwners;

    public TransPainter(TransMeta transMeta)
    {
        this(transMeta, transMeta.getMaximum(), null, null, null, null, null, new ArrayList<AreaOwner>());
    }

    public TransPainter(TransMeta transMeta, Point area)
    {
        this(transMeta, area, null, null, null, null, null, new ArrayList<AreaOwner>());
    }

    public TransPainter(TransMeta transMeta, 
                        Point area, 
                        ScrollBar hori, ScrollBar vert, 
                        TransHopMeta candidate, Point drop_candidate, Rectangle selrect,
                        List<AreaOwner> areaOwners
                        )
    {
        this.transMeta      = transMeta;
        
        this.background     = GUIResource.getInstance().getColorGraph();
        this.black          = GUIResource.getInstance().getColorBlack();
        this.red            = GUIResource.getInstance().getColorRed();
        this.yellow         = GUIResource.getInstance().getColorYellow();
        this.orange         = GUIResource.getInstance().getColorOrange();
        this.green          = GUIResource.getInstance().getColorGreen();
        this.blue           = GUIResource.getInstance().getColorBlue();
        this.magenta        = GUIResource.getInstance().getColorMagenta();
        this.gray           = GUIResource.getInstance().getColorGray();
        this.lightGray      = GUIResource.getInstance().getColorLightGray();
        this.darkGray       = GUIResource.getInstance().getColorDarkGray();
        
        this.area           = area;
        this.hori           = hori;
        this.vert           = vert;
        this.noteFont       = GUIResource.getInstance().getFontNote();
        this.graphFont      = GUIResource.getInstance().getFontGraph();
        this.images         = GUIResource.getInstance().getImagesSteps();
        this.candidate      = candidate;
        this.selrect        = selrect;
        this.drop_candidate = drop_candidate;
        
        this.areaOwners     = areaOwners;
        
        props = PropsUI.getInstance();
        iconsize = props.getIconSize(); 
        linewidth = props.getLineWidth();
    }

    public Image getTransformationImage(Device device)
    {
        return getTransformationImage(device, false);
    }
    
    public Image getTransformationImage(Device device, boolean branded)
    {
        Image img = new Image(device, area.x, area.y);
        GC gc = new GC(img);
        
        // First clear the image in the background color
        gc.setBackground(background);
        gc.fillRectangle(0, 0, area.x, area.y);
        
        if (branded)
        {
            Image gradient= GUIResource.getInstance().getImageBanner();
            gc.drawImage(gradient, 0, 0);

            Image logo = GUIResource.getInstance().getImageKettleLogo();
            org.eclipse.swt.graphics.Rectangle logoBounds = logo.getBounds();
            gc.drawImage(logo, 20, area.y-logoBounds.height);
        }
        
        // Draw the transformation onto the image
        drawTrans(gc);
        
        gc.dispose();
        
        return img;
    }

    private void drawTrans(GC gc)
    {
        if (props.isAntiAliasingEnabled()) gc.setAntialias(SWT.ON);
        
        areaOwners.clear(); // clear it before we start filling it up again.
        
        shadowsize = props.getShadowSize();

        Point max   = transMeta.getMaximum();
        Point thumb = getThumb(area, max);
        offset = getOffset(thumb, area);

        if (hori!=null && vert!=null)
        {
            hori.setThumb(thumb.x);
            vert.setThumb(thumb.y);
        }
        
        gc.setFont(noteFont);
        
        // First the notes
        for (int i = 0; i < transMeta.nrNotes(); i++)
        {
            NotePadMeta ni = transMeta.getNote(i);
            drawNote(gc, ni);
        }

        gc.setFont(graphFont);
        gc.setBackground(background);

        if (shadowsize > 0)
        {
            for (int i = 0; i < transMeta.nrSteps(); i++)
            {
                StepMeta stepMeta = transMeta.getStep(i);
                if (stepMeta.isDrawn()) drawStepShadow(gc, stepMeta);
            }
        }

        for (int i = 0; i < transMeta.nrTransHops(); i++)
        {
            TransHopMeta hi = transMeta.getTransHop(i);
            drawHop(gc, hi);
        }

        if (candidate != null)
        {
            drawHop(gc, candidate, true);
        }

        for (int i = 0; i < transMeta.nrSteps(); i++)
        {
            StepMeta stepMeta = transMeta.getStep(i);
            if (stepMeta.isDrawn()) drawStep(gc, stepMeta);
        }

        if (drop_candidate != null)
        {
            gc.setLineStyle(SWT.LINE_SOLID);
            gc.setForeground(black);
            Point screen = real2screen(drop_candidate.x, drop_candidate.y, offset);
            gc.drawRectangle(screen.x, screen.y,          iconsize, iconsize);
        }

        drawRect(gc, selrect);
    }

    private void drawHop(GC gc, TransHopMeta hi)
    {
        drawHop(gc, hi, false);
    }

    private void drawNote(GC gc, NotePadMeta notePadMeta)
    {
        int flags = SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT;

        if (notePadMeta.isSelected()) gc.setLineWidth(2); else gc.setLineWidth(1);
        
        org.eclipse.swt.graphics.Point ext;
        if (Const.isEmpty(notePadMeta.getNote()))
        {
            ext = new org.eclipse.swt.graphics.Point(10,10); // Empty note
        }
        else
        {
            ext = gc.textExtent(notePadMeta.getNote(), flags);
        }
        Point p = new Point(ext.x, ext.y);
        Point loc = notePadMeta.getLocation();
        Point note = real2screen(loc.x, loc.y, offset);
        int margin = Const.NOTE_MARGIN;
        p.x += 2 * margin;
        p.y += 2 * margin;
        int width = notePadMeta.width;
        int height = notePadMeta.height;
        if (p.x > width) width = p.x;
        if (p.y > height) height = p.y;

        int noteshape[] = new int[] { note.x, note.y, // Top left
                note.x + width + 2 * margin, note.y, // Top right
                note.x + width + 2 * margin, note.y + height, // bottom right 1
                note.x + width, note.y + height + 2 * margin, // bottom right 2
                note.x + width, note.y + height, // bottom right 3
                note.x + width + 2 * margin, note.y + height, // bottom right 1
                note.x + width, note.y + height + 2 * margin, // bottom right 2
                note.x, note.y + height + 2 * margin // bottom left
        };
        int s = props.getShadowSize();
        int shadow[] = new int[] { note.x + s, note.y + s, // Top left
                note.x + width + 2 * margin + s, note.y + s, // Top right
                note.x + width + 2 * margin + s, note.y + height + s, // bottom
                // right 1
                note.x + width + s, note.y + height + 2 * margin + s, // bottom
                // right 2
                note.x + s, note.y + height + 2 * margin + s // bottom left
        };

        gc.setForeground(lightGray);
        gc.setBackground(lightGray);
        gc.fillPolygon(shadow);

        gc.setForeground(darkGray);
        gc.setBackground(yellow);

        gc.fillPolygon(noteshape);
        gc.drawPolygon(noteshape);
        //gc.fillRectangle(ni.xloc, ni.yloc, width+2*margin, heigth+2*margin);
        //gc.drawRectangle(ni.xloc, ni.yloc, width+2*margin, heigth+2*margin);
        gc.setForeground(black);
        if ( !Const.isEmpty(notePadMeta.getNote()) )
        {
            gc.drawText(notePadMeta.getNote(), note.x + margin, note.y + margin, flags);
        }

        notePadMeta.width = width; // Save for the "mouse" later on...
        notePadMeta.height = height;

        if (notePadMeta.isSelected()) gc.setLineWidth(1); else gc.setLineWidth(2);
        
        // Add to the list of areas...
        //
        areaOwners.add(new AreaOwner(note.x, note.y, width, height, transMeta, notePadMeta));
    }

    private void drawHop(GC gc, TransHopMeta hi, boolean is_candidate)
    {
        StepMeta fs = hi.getFromStep();
        StepMeta ts = hi.getToStep();

        if (fs != null && ts != null)
        {
            if (shadowsize > 0) drawLineShadow(gc, fs, ts, hi, false);
            drawLine(gc, fs, ts, hi, is_candidate);
        }
    }

    private void drawStepShadow(GC gc, StepMeta stepMeta)
    {
        if (stepMeta == null) return;

        Point pt = stepMeta.getLocation();

        int x, y;
        if (pt != null)
        {
            x = pt.x;
            y = pt.y;
        } else
        {
            x = 50;
            y = 50;
        }

        Point screen = real2screen(x, y, offset);

        // First draw the shadow...
        gc.setBackground(lightGray);
        gc.setForeground(lightGray);
        int s = shadowsize;
        gc.fillRectangle(screen.x + s, screen.y + s, iconsize, iconsize);
    }

    private void drawStep(GC gc, StepMeta stepMeta)
    {
        if (stepMeta == null) return;

        Point pt = stepMeta.getLocation();

        int x, y;
        if (pt != null)
        {
            x = pt.x;
            y = pt.y;
        } else
        {
            x = 50;
            y = 50;
        }

        Point screen = real2screen(x, y, offset);

        // REMOTE STEPS
        
        // First draw an extra indicator for remote input steps...
        //
        if (!stepMeta.getRemoteInputSteps().isEmpty()) {
        	gc.setForeground(GUIResource.getInstance().getColorGray());
        	gc.setBackground(GUIResource.getInstance().getColorBackground());
            gc.setFont(GUIResource.getInstance().getFontGraph());
        	String nrInput = Integer.toString(stepMeta.getRemoteInputSteps().size());
        	org.eclipse.swt.graphics.Point textExtent = gc.textExtent(nrInput);
        	textExtent.x+=2; // add a tiny little bit of a margin
        	textExtent.y+=2;
        	
        	// Draw it an icon above the step icon.
        	// Draw it an icon and a half to the left
        	//
        	Point point = new Point(screen.x-iconsize-iconsize/2, screen.y-iconsize);
        	gc.drawRectangle(point.x, point.y, textExtent.x, textExtent.y);
        	gc.drawText(nrInput, point.x+1, point.y+1);
        	
        	// Now we draw an arrow from the cube to the step...
        	// 
        	gc.drawLine(point.x+textExtent.x, point.y+textExtent.y/2, screen.x-iconsize/2, point.y+textExtent.y/2);
         	drawArrow(gc, screen.x-iconsize/2, point.y+textExtent.y/2, screen.x+iconsize/3, screen.y, Math.toRadians(15), 15, 1.8, null, null );
         	
            // Add to the list of areas...
            areaOwners.add(new AreaOwner(point.x, point.y, textExtent.x, textExtent.y, stepMeta, STRING_REMOTE_INPUT_STEPS));
        }

        // Then draw an extra indicator for remote output steps...
        //
        if (!stepMeta.getRemoteOutputSteps().isEmpty()) {
        	gc.setForeground(GUIResource.getInstance().getColorGray());
        	gc.setBackground(GUIResource.getInstance().getColorBackground());
            gc.setFont(GUIResource.getInstance().getFontGraph());
        	String nrOutput = Integer.toString(stepMeta.getRemoteOutputSteps().size());
        	org.eclipse.swt.graphics.Point textExtent = gc.textExtent(nrOutput);
        	textExtent.x+=2; // add a tiny little bit of a margin
        	textExtent.y+=2;
        	
        	// Draw it an icon above the step icon.
        	// Draw it an icon and a half to the right
        	//
        	Point point = new Point(screen.x+2*iconsize+iconsize/2-textExtent.x, screen.y-iconsize);
        	gc.drawRectangle(point.x, point.y, textExtent.x, textExtent.y);
        	gc.drawText(nrOutput, point.x+1, point.y+1);
        	
        	// Now we draw an arrow from the cube to the step...
        	// This time, we start at the left side...
        	// 
        	gc.drawLine(point.x, point.y+textExtent.y/2, screen.x+iconsize+iconsize/2, point.y+textExtent.y/2);
         	drawArrow(gc, screen.x+2*iconsize/3, screen.y, screen.x+iconsize+iconsize/2, point.y+textExtent.y/2, Math.toRadians(15), 15, 1.8, null, null );

         	// Add to the list of areas...
            areaOwners.add(new AreaOwner(point.x, point.y, textExtent.x, textExtent.y, stepMeta, STRING_REMOTE_OUTPUT_STEPS));
        }
        
        // PARTITIONING

        // If this step is partitioned, we're drawing a small symbol indicating this...
        //
        if (stepMeta.isPartitioned()) {
        	gc.setForeground(GUIResource.getInstance().getColorRed());
        	gc.setBackground(GUIResource.getInstance().getColorBackground());
            gc.setFont(GUIResource.getInstance().getFontGraph());
            
            PartitionSchema partitionSchema = stepMeta.getStepPartitioningMeta().getPartitionSchema();
            if (partitionSchema!=null) {
	            
            	String nrInput;
            	
            	if (partitionSchema.isDynamicallyDefined()) {
            		nrInput = "Dx"+partitionSchema.getNumberOfPartitionsPerSlave();
            	}
            	else {
            		nrInput = "Px"+Integer.toString(partitionSchema.getPartitionIDs().size());
            	}
	        	
	        	org.eclipse.swt.graphics.Point textExtent = gc.textExtent(nrInput);
	        	textExtent.x+=2; // add a tiny little bit of a margin
	        	textExtent.y+=2;
	        	
	        	// Draw it a 2 icons above the step icon.
	        	// Draw it an icon and a half to the left
	        	//
	        	Point point = new Point(screen.x-iconsize-iconsize/2, screen.y-iconsize-iconsize);
	        	gc.drawRectangle(point.x, point.y, textExtent.x, textExtent.y);
	        	gc.drawText(nrInput, point.x+1, point.y+1);
	        	
	        	// Now we draw an arrow from the cube to the step...
	        	// 
	        	gc.drawLine(point.x+textExtent.x, point.y+textExtent.y/2, screen.x-iconsize/2, point.y+textExtent.y/2);
	         	gc.drawLine(screen.x-iconsize/2, point.y+textExtent.y/2, screen.x+iconsize/3, screen.y);
	         	
	            // Add to the list of areas...
	         	//
	            areaOwners.add(new AreaOwner(point.x, point.y, textExtent.x, textExtent.y, stepMeta, STRING_PARTITIONING_CURRENT_STEP));
            }
        }
        
        String name = stepMeta.getName();

        if (stepMeta.isSelected())
            gc.setLineWidth(linewidth + 2);
        else
            gc.setLineWidth(linewidth);
        gc.setBackground(red);
        gc.setForeground(black);
        gc.fillRectangle(screen.x, screen.y, iconsize, iconsize);
        
        // Add to the list of areas...
        areaOwners.add(new AreaOwner(screen.x, screen.y, iconsize, iconsize, transMeta, stepMeta));
        
        String steptype = stepMeta.getStepID();
        Image im = (Image) images.get(steptype);
        if (im != null) // Draw the icon!
        {
            org.eclipse.swt.graphics.Rectangle bounds = im.getBounds();
            gc.drawImage(im, 0, 0, bounds.width, bounds.height, screen.x, screen.y, iconsize, iconsize);
        }
        gc.setBackground(background);
        gc.drawRectangle(screen.x - 1, screen.y - 1, iconsize + 1, iconsize + 1);

        Point namePosition = getNamePosition(gc, name, screen, iconsize );
        
        if (shadowsize > 0)
        {
            gc.setForeground(lightGray);
            gc.setFont(GUIResource.getInstance().getFontGraph());
            gc.drawText(name, namePosition.x + shadowsize, namePosition.y + shadowsize, SWT.DRAW_TRANSPARENT);
        }

        gc.setForeground(black);
        gc.setFont(GUIResource.getInstance().getFontGraph());
        gc.drawText(name, namePosition.x, namePosition.y, SWT.DRAW_TRANSPARENT);

        boolean partitioned=false;
        
        StepPartitioningMeta meta = stepMeta.getStepPartitioningMeta();
        if (stepMeta.isPartitioned() && meta!=null)
        {
            partitioned=true;
        }
        if (stepMeta.getClusterSchema()!=null)
        {
            String message = "C";
            message+="x"+stepMeta.getClusterSchema().findNrSlaves();
            
            gc.setBackground(background);
            gc.setForeground(black);
            gc.drawText(message, screen.x + 3 + iconsize, screen.y - 8);
        }
        if (stepMeta.getCopies() > 1  && !partitioned)
        {
            gc.setBackground(background);
            gc.setForeground(black);
            gc.drawText("x" + stepMeta.getCopies(), screen.x - 5, screen.y - 5);
        }
        
    }

    public static final Point getNamePosition(GC gc, String string, Point screen, int iconsize)
    {
        org.eclipse.swt.graphics.Point textsize = gc.textExtent(string);
        
        int xpos = screen.x + (iconsize / 2) - (textsize.x / 2);
        int ypos = screen.y + iconsize + 5;

        return new Point(xpos, ypos);
    }

    private void drawLineShadow(GC gc, StepMeta fs, StepMeta ts, TransHopMeta hi, boolean is_candidate)
    {
        int line[] = getLine(fs, ts);
        int s = shadowsize;
        for (int i = 0; i < line.length; i++)
            line[i] += s;

        gc.setLineWidth(linewidth);
        
        gc.setForeground(lightGray);

        drawArrow(gc, line, null, null);
    }

    private void drawLine(GC gc, StepMeta fs, StepMeta ts, TransHopMeta hi, boolean is_candidate)
    {
        StepMetaInterface fsii = fs.getStepMetaInterface();
        StepMetaInterface tsii = ts.getStepMetaInterface();

        int line[] = getLine(fs, ts);

        Color col;
        int linestyle=SWT.LINE_SOLID;
        int activeLinewidth = linewidth; 

        if (is_candidate)
        {
            col = blue;
        }
        else
        {
            if (hi.isEnabled())
            {
                String[] targetSteps = fsii.getTargetSteps();
                String[] infoSteps = tsii.getInfoSteps();

                // System.out.println("Normal step: "+fs+" --> "+ts+",
                // "+(infoSteps!=null)+", "+(targetSteps!=null));

                if (fs.isSendingErrorRowsToStep(ts))
                {
                    col = red;
                    linestyle = SWT.LINE_DOT;
                    activeLinewidth = linewidth+2;
                }
                else
                {
                    if (targetSteps == null) // Normal link: distribute or copy data...
                    {
                        boolean distributes = fs.isDistributes();
                        if (ts.getStepPartitioningMeta().isMethodMirror()) distributes=false;
                        
                        // Or perhaps it's an informational link: draw different
                        // color...
                        if (Const.indexOfString(fs.getName(), infoSteps) >= 0)
                        {
                            if (distributes)
                                col = yellow;
                            else
                                col = magenta;
                        }
                        else
                        {
                            if (distributes)
                                col = green;
                            else
                                col = red;
                        }
                    }
                    else
                    {
                        // Visual check to see if the target step is specified...
                        if (Const.indexOfString(ts.getName(), fsii.getTargetSteps()) >= 0)
                        {
                            col = black;
                        }
                        else
                        {
                            linestyle = SWT.LINE_DOT;
                            col = orange;
                        }
                    }
                }
            }
            else
            {
                col = gray;
            }
        }
        if (hi.split) activeLinewidth = linewidth+2;

        gc.setForeground(col);
        gc.setLineStyle(linestyle);
        gc.setLineWidth(activeLinewidth);
        
        drawArrow(gc, line, null, ts);
        
        if (hi.split) gc.setLineWidth(linewidth);

        gc.setForeground(black);
        gc.setBackground(background);
        gc.setLineStyle(SWT.LINE_SOLID);
    }

    private static final Point getThumb(Point area, Point max)
    {
        Point thumb = new Point(0, 0);
        if (max.x <= area.x)
            thumb.x = 100;
        else
            thumb.x = 100 * area.x / max.x;

        if (max.y <= area.y)
            thumb.y = 100;
        else
            thumb.y = 100 * area.y / max.y;

        return thumb;
    }
    
    private Point getOffset(Point thumb, Point area)
    {
        Point p = new Point(0, 0);

        if (hori==null || vert==null) return p;

        Point sel = new Point(hori.getSelection(), vert.getSelection());

        if (thumb.x == 0 || thumb.y == 0) return p;

        p.x = -sel.x * area.x / thumb.x;
        p.y = -sel.y * area.y / thumb.y;

        return p;
    }
    
    public static final Point real2screen(int x, int y, Point offset)
    {
        Point screen = new Point(x + offset.x, y + offset.y);

        return screen;
    }
    
    private void drawRect(GC gc, Rectangle rect)
    {
        if (rect == null) return;
        gc.setLineStyle(SWT.LINE_DASHDOT);
        gc.setLineWidth(linewidth);
        gc.setForeground(gray);
        gc.drawRectangle(rect.x + offset.x, rect.y + offset.y, rect.width, rect.height);
        gc.setLineStyle(SWT.LINE_SOLID);
    }

    private int[] getLine(StepMeta fs, StepMeta ts)
    {
        Point from = fs.getLocation();
        Point to = ts.getLocation();
        
        int x1 = from.x + iconsize / 2;
        int y1 = from.y + iconsize / 2;

        int x2 = to.x + iconsize / 2;
        int y2 = to.y + iconsize / 2;

        return new int[] { x1, y1, x2, y2 };
    }

    private void drawArrow(GC gc, int line[], Object startObject, Object endObject)
    {
    	double theta = Math.toRadians(10); // arrowhead sharpness
        int size = 30 + (linewidth - 1) * 5; // arrowhead length

        Point screen_from = real2screen(line[0], line[1], offset);
        Point screen_to = real2screen(line[2], line[3], offset);
        
        drawArrow(gc, screen_from.x, screen_from.y, screen_to.x, screen_to.y, theta, size, -1, startObject, endObject);
    }

    private void drawArrow(GC gc, int x1, int y1, int x2, int y2, double theta, int size, double factor, Object startObject, Object endObject)
    {
        
        int mx, my;
        int x3;
        int y3;
        int x4;
        int y4;
        int a, b, dist;
        double angle;

        gc.drawLine(x1, y1, x2, y2);

        // in between 2 points
        mx = x1 + (x2 - x1) / 2;
        my = y1 + (y2 - y1) / 2;

        a = Math.abs(x2 - x1);
        b = Math.abs(y2 - y1);
        dist = (int) Math.sqrt(a * a + b * b);

        // determine factor (position of arrow to left side or right side
        // 0-->100%)
        if (factor<0)
        {
	        if (dist >= 2 * iconsize)
	             factor = 1.5;
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

        // draw arrowhead
        //gc.drawLine( mx, my, x3, y3 );
        //gc.drawLine( mx, my, x4, y4 );
        //gc.drawLine( x3, y3, x4, y4 );

        Color fore = gc.getForeground();
        Color back = gc.getBackground();
        gc.setBackground(fore);
        gc.fillPolygon(new int[] { mx, my, x3, y3, x4, y4 });
        gc.setBackground(back);

        // Show message at the start/end of the arrow
        // Only show this message when the length is sufficiently long
        //
        /*
        if (startObject!=null && startObject instanceof StepMeta) {
        	String startMessage = null;
        	StepMeta fs = (StepMeta) startObject;
            if ( fs.isPartitioned()) {
            	startMessage = "x"+fs.getStepPartitioningMeta().getPartitionSchema().getPartitionIDs().size()+Const.CR+fs.getStepPartitioningMeta().getPartitionSchema().getName();
            }
	        if (startMessage!=null && dist >= 2 * iconsize) {
	        	gc.setFont(GUIResource.getInstance().getFontTiny());
	        	gc.setForeground(GUIResource.getInstance().getColorRed());
	        	factor = 0.50;
	        	mx = (int) (x1 + factor * (x2 - x1) / 2);
	            my = (int) (y1 + factor * (y2 - y1) / 2);
	            org.eclipse.swt.graphics.Point textExtent = gc.textExtent(startMessage);
	            gc.drawText(startMessage, mx-textExtent.x/3, my-textExtent.y/2, true);
	            gc.setForeground(GUIResource.getInstance().getColorLightGray());
	            gc.drawRectangle(mx-textExtent.x/3-2, my-textExtent.y/2-2, textExtent.x+4, textExtent.y+4);
	            
	            areaOwners.add(new AreaOwner(mx-textExtent.x/3, my-textExtent.y/2, textExtent.x+4, textExtent.y+4, fs, STRING_PARTITIONING_CURRENT_STEP));
	        }
        }
        if (endObject!=null && endObject instanceof StepMeta) {
            String endMessage = null;
            StepMeta ts = (StepMeta) endObject;
            if (ts.isPartitioned()) {
            	int x = 0;
            	String name = "unknown";
            	StepPartitioningMeta stepPartitioningMeta = ts.getStepPartitioningMeta();
            		PartitionSchema partitionSchema = stepPartitioningMeta.getPartitionSchema();
            		if( stepPartitioningMeta != null ) {
            		if( partitionSchema != null ) {
            			List<String> ids = partitionSchema.getPartitionIDs();
            			name = partitionSchema.getName();
            			if( ids != null ) {
            				x = ids.size();
            			}
            		}
            	}
            	endMessage = "x"+x+Const.CR+name;
            }
            if (endMessage!=null && dist >= 2 * iconsize) {
            	gc.setFont(GUIResource.getInstance().getFontTiny());
            	gc.setForeground(GUIResource.getInstance().getColorRed());
            	factor = 1.55;
            	mx = (int) (x1 + factor * (x2 - x1) / 2);
                my = (int) (y1 + factor * (y2 - y1) / 2);
                org.eclipse.swt.graphics.Point textExtent = gc.textExtent(endMessage);
                gc.drawText(endMessage, mx-textExtent.x/3, my-textExtent.y/2, true);
                gc.setForeground(GUIResource.getInstance().getColorLightGray());
                gc.drawRectangle(mx-textExtent.x/3-2, my-textExtent.y/2-2, textExtent.x+4, textExtent.y+4);
	            areaOwners.add(new AreaOwner(mx-textExtent.x/3, my-textExtent.y/2, textExtent.x+4, textExtent.y+4, ts, STRING_PARTITIONING_CURRENT_NEXT));
            }
        }
        */
    }

}
