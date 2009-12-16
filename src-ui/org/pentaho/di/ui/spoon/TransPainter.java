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

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.ScrollBar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.RepositoryLock;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.AreaOwner.AreaType;
import org.pentaho.di.ui.spoon.trans.TransGraph;




public class TransPainter
{
	
    public static final String STRING_PARTITIONING_CURRENT_STEP = "PartitioningCurrentStep"; // $NON-NLS-1$
    public static final String STRING_PARTITIONING_CURRENT_NEXT = "PartitioningNextStep";    // $NON-NLS-1$
	public static final String STRING_REMOTE_INPUT_STEPS        = "RemoteInputSteps";        // $NON-NLS-1$
	public static final String STRING_REMOTE_OUTPUT_STEPS       = "RemoteOutputSteps";       // $NON-NLS-1$
	public static final String STRING_STEP_ERROR_LOG            = "StepErrorLog";            // $NON-NLS-1$
	public static final String STRING_HOP_TYPE_COPY             = "HopTypeCopy";             // $NON-NLS-1$
	public static final String STRING_HOP_TYPE_INFO             = "HopTypeInfo";             // $NON-NLS-1$
	public static final String STRING_HOP_TYPE_ERROR            = "HopTypeError";            // $NON-NLS-1$
	public static final String STRING_INFO_STEP_COPIES          = "InfoStepMultipleCopies";  // $NON-NLS-1$
	public static final String STRING_INPUT_HOP_ICON            = "InputHopIcon";            // $NON-NLS-1$
	public static final String STRING_OUTPUT_HOP_ICON           = "OutputHopIcon";           // $NON-NLS-1$
	
	public static final String[] magnificationDescriptions = 
		new String[] { "  200% ", "  150% ", "  100% ", "  75% ", "  50% ", "  25% "};
	
	private static final int	MINI_ICON_MARGIN = 5;
	private static final int	MINI_ICON_TRIANGLE_BASE = 10;
	private static final int	MINI_ICON_DISTANCE = 7;
	private static final int	MINI_ICON_SKEW = 0;

	public final double theta = Math.toRadians(11); // arrowhead sharpness
	
/*	
	public static final float[] magnifications = 
		new float[] { 0.10f, 0.15f, 0.20f, 0.25f, 0.30f, 0.35f, 0.40f, 0.45f, 0.50f, 0.55f, 0.60f, 0.65f, 0.70f, 0.75f, 0.80f, 0.85f, 0.90f, 0.95f, 1.00f, 1.25f, 1.50f, 2.00f, 3.00f, 4.00f, 5.00f, 7.50f, 10.00f, };
	
	public static final int MAGNIFICATION_100_PERCENT_INDEX = 18;

	*/
	private PropsUI      props;
    private int          shadowsize;
    private Point        area;
    private TransMeta    transMeta;
    private ScrollBar    hori, vert;

    private Point        offset;

    private Color        background;
    private Color        black;
    private Color        red;
    //private Color        yellow;
    // private Color        orange;
    // private Color        green;
    private Color        blue;
    // private Color        magenta;
    private Color        gray;
    private Color        lightGray;
    private Color        darkGray;

    private Font         noteFont;
    private Font         graphFont;

    private TransHopMeta candidate;
    private Point        drop_candidate;
    private int          iconsize;
    private int          gridSize;
    private Rectangle    selrect;
    private int          linewidth;
    private Map<String, Image> images;
    
    private List<AreaOwner> areaOwners;
    
    private float           magnification;
    private float           translationX;
    private float           translationY;
	private boolean 		shadow;
	
	private Map<StepMeta, String> stepLogMap;
	private List<StepMeta>	mouseOverSteps;
	private StepMeta    startHopStep;
	private Point       endHopLocation;
	private StepMeta    endHopStep;
	private StepMeta       noInputStep;
	private StreamType	candidateHopType;
	private boolean 	startErrorHopStep;
	private StepMeta showTargetStreamsStep;

    public TransPainter(TransMeta transMeta, 
                        Point area, 
                        ScrollBar hori, ScrollBar vert, 
                        TransHopMeta candidate, Point drop_candidate, Rectangle selrect,
                        List<AreaOwner> areaOwners, 
                        List<StepMeta> mouseOverSteps
                        )
    {
        this.transMeta      = transMeta;
        
        this.background     = GUIResource.getInstance().getColorGraph();
        this.black          = GUIResource.getInstance().getColorBlack();
        this.red            = GUIResource.getInstance().getColorRed();
        //this.yellow         = GUIResource.getInstance().getColorYellow();
        // this.orange         = GUIResource.getInstance().getColorOrange();
        // this.green          = GUIResource.getInstance().getColorGreen();
        this.blue           = GUIResource.getInstance().getColorBlue();
        // this.magenta        = GUIResource.getInstance().getColorMagenta();
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
        
        this.mouseOverSteps  = mouseOverSteps;
        
        props = PropsUI.getInstance();
        iconsize = props.getIconSize(); 
        linewidth = props.getLineWidth();
        
        magnification = 1.0f;
        
        stepLogMap = null;
    }

    public Image getTransformationImage(Device device)
    {
        return getTransformationImage(device, false);
    }
    
    public Image getTransformationImage(Device device, boolean branded)
    {
        Image img = new Image(device, area.x, area.y);
        GC gc = new GC(img);
        
        if (props.isAntiAliasingEnabled()) gc.setAntialias(SWT.ON);
        
        areaOwners.clear(); // clear it before we start filling it up again.
        
        gridSize = props.getCanvasGridSize();
        shadowsize = props.getShadowSize();
        
        Point max   = transMeta.getMaximum();
        Point thumb = getThumb(area, max);
        offset = getOffset(thumb, area);

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

        
        // If there is a shadow, we draw the transformation first with an alpha setting
        //
        if (shadowsize>0) {
        	shadow = true;
        	Transform transform = new Transform(device);
        	transform.translate(translationX+shadowsize*magnification, translationY+shadowsize*magnification);
        	transform.scale(magnification, magnification);
        	gc.setTransform(transform);
            gc.setAlpha(20);
        	
        	drawTrans(gc, thumb);
        }
        
        // Draw the transformation onto the image
        //
        shadow = false;
    	Transform transform = new Transform(device);
    	transform.translate(translationX, translationY);
    	transform.scale(magnification, magnification);
    	gc.setTransform(transform);
        gc.setAlpha(255);
        drawTrans(gc, thumb);
        
        gc.dispose();
        
        return img;
    }

    private void drawTrans(GC gc, Point thumb)
    {
        if (!shadow && gridSize>1) {
        	drawGrid(gc);
        }
        
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

        for (int i = 0; i < transMeta.nrTransHops(); i++)
        {
            TransHopMeta hi = transMeta.getTransHop(i);
            drawHop(gc, hi);
        }

        if (candidate != null)
        {
            drawHop(gc, candidate, true);
        } else {
	        if (startHopStep!=null && endHopLocation!=null) {
	        	Point fr = startHopStep.getLocation();
	        	Point to = endHopLocation;
	        	if (endHopStep==null) {
	        		gc.setForeground(GUIResource.getInstance().getColorGray());
	        	} else {
	        		gc.setForeground(GUIResource.getInstance().getColorBlue());
	        	}
	        	drawArrow(gc, fr.x+iconsize/2, fr.y+iconsize/2, to.x, to.y, theta, calcArrowLength(), 1.2, startHopStep, endHopStep==null ? endHopLocation : endHopStep);
	        }  else if (endHopStep!=null && endHopLocation!=null) {
	        	Point fr = endHopLocation;
	        	Point to = endHopStep.getLocation();
	        	if (startHopStep==null) {
	        		gc.setForeground(GUIResource.getInstance().getColorGray());
	        	} else {
	        		gc.setForeground(GUIResource.getInstance().getColorBlue());
	        	}
	        	drawArrow(gc, fr.x, fr.y, to.x+iconsize/2, to.y+iconsize/2, theta, calcArrowLength(), 1.2, startHopStep==null ? endHopLocation : startHopStep, endHopStep);
	        }

        }
        
        for (int i = 0; i < transMeta.nrSteps(); i++)
        {
            StepMeta stepMeta = transMeta.getStep(i);
            if (stepMeta.isDrawn()) drawStep(gc, stepMeta);
        }

        // Display an icon on the indicated location signaling to the user that the step in question does not accept input 
        //
        if (noInputStep!=null) {
        	gc.setLineWidth(2);	
        	gc.setForeground(GUIResource.getInstance().getColorRed());
        	Point n = noInputStep.getLocation();
        	gc.drawLine(n.x-5, n.y-5, n.x+iconsize+10, n.y+iconsize+10);
        	gc.drawLine(n.x-5, n.y+iconsize+5, n.x+iconsize+5, n.y-5);
        }

        if (drop_candidate != null)
        {
            gc.setLineStyle(SWT.LINE_SOLID);
            gc.setForeground(black);
            Point screen = real2screen(drop_candidate.x, drop_candidate.y, offset);
            gc.drawRectangle(screen.x, screen.y,          iconsize, iconsize);
        }
        
        if (!shadow) {
        	drawRect(gc, selrect);
        }

        RepositoryLock lock = transMeta.getRepositoryLock();
        if (lock!=null) {
        	// This transformation is locked, draw some kind of logo in the right upper corner...
        	//
        	Image lockImage = GUIResource.getInstance().getImageLocked();
        	Rectangle lockBounds = lockImage.getBounds();
        	gc.drawImage(lockImage, area.x - lockBounds.width, 0);
        	areaOwners.add(new AreaOwner(AreaType.REPOSITORY_LOCK_IMAGE, area.x - lockBounds.width, 0, lockBounds.width, lockBounds.height, transMeta, lock));
        }
    }

    private void drawGrid(GC gc) {
    	Rectangle bounds = gc.getDevice().getBounds();
		for (int x=0;x<bounds.width;x+=gridSize) {
			for (int y=0;y<bounds.height;y+=gridSize) {
				gc.drawPoint(x+(offset.x%gridSize),y+(offset.y%gridSize));
			}
		}
	}

	private void drawHop(GC gc, TransHopMeta hi)
    {
        drawHop(gc, hi, false);
    }
	
	protected void drawNote(GC gc, NotePadMeta notePadMeta)
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
            int swt=SWT.NORMAL;
            if(notePadMeta.isFontBold()) swt=SWT.BOLD;
            if(notePadMeta.isFontItalic()) swt=swt | SWT.ITALIC;

            gc.setFont(new Font(PropsUI.getDisplay(),Const.NVL(notePadMeta.getFontName(),props.getNoteFont().getName()), 
            		notePadMeta.getFontSize()==-1?props.getNoteFont().getHeight():notePadMeta.getFontSize(), swt));

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
		
		// Draw shadow around note?
		if(notePadMeta.isDrawShadow())
		{
			int s = this.props.getShadowSize();
			int shadowa[] = new int[] { note.x+s, note.y+s, // Top left
				note.x + width + 2 * margin+s, note.y+s, // Top right
				note.x + width + 2 * margin+s, note.y + height+s, // bottom right 1
				note.x + width+s, note.y + height + 2 * margin+s, // bottom right 2
				note.x+s, note.y + height + 2 * margin+s // bottom left
			};
			gc.setBackground(lightGray);
			gc.fillPolygon(shadowa);
		}
        gc.setBackground(new Color(PropsUI.getDisplay(),new RGB(notePadMeta.getBackGroundColorRed(),notePadMeta.getBackGroundColorGreen(),notePadMeta.getBackGroundColorBlue())));
        gc.setForeground(new Color(PropsUI.getDisplay(),new RGB(notePadMeta.getBorderColorRed(),notePadMeta.getBorderColorGreen(),notePadMeta.getBorderColorBlue())));

        gc.fillPolygon(noteshape);
        gc.drawPolygon(noteshape);
      
        if (!Const.isEmpty(notePadMeta.getNote()))
        {
            gc.setForeground(new Color(PropsUI.getDisplay(),new RGB(notePadMeta.getFontColorRed(),notePadMeta.getFontColorGreen(),notePadMeta.getFontColorBlue())));
        	gc.drawText(notePadMeta.getNote(), note.x + margin, note.y + margin, flags);
        }
   
        notePadMeta.width = width; // Save for the "mouse" later on...
        notePadMeta.height = height;

        if (notePadMeta.isSelected()) gc.setLineWidth(1); else gc.setLineWidth(2);
        
        // Add to the list of areas...
        //
        if (!shadow) {
        	areaOwners.add(new AreaOwner(AreaType.NOTE, note.x, note.y, width, height, transMeta, notePadMeta));
        }
    }

    private void drawHop(GC gc, TransHopMeta hi, boolean is_candidate)
    {
        StepMeta fs = hi.getFromStep();
        StepMeta ts = hi.getToStep();

        if (fs != null && ts != null)
        {
            drawLine(gc, fs, ts, hi, is_candidate);
        }
    }

    private void drawStep(GC gc, StepMeta stepMeta)
    {
        if (stepMeta == null) return;
        int alpha = gc.getAlpha();
        
        StepIOMetaInterface ioMeta = stepMeta.getStepMetaInterface().getStepIOMeta();

        boolean fade =  startHopStep!=null && (!ioMeta.isInputAcceptor() || startHopStep.equals(stepMeta));
        fade=fade || mouseOverSteps.contains(stepMeta);
        if (fade) {
        	gc.setAlpha(150);
        }

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
        
        boolean stepError = false;
        if (stepLogMap!=null && !stepLogMap.isEmpty()) {
        	String log = stepLogMap.get(stepMeta);
	        	if (!Const.isEmpty(log)) {
	        		stepError=true;
	        	}
        	}

        // REMOTE STEPS
        
        // First draw an extra indicator for remote input steps...
        //
        if (!stepMeta.getRemoteInputSteps().isEmpty()) {
        	gc.setLineWidth(1);
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
            if (!shadow) {
            	areaOwners.add(new AreaOwner(AreaType.REMOTE_INPUT_STEP, point.x, point.y, textExtent.x, textExtent.y, stepMeta, STRING_REMOTE_INPUT_STEPS));
            }
        }

        // Then draw an extra indicator for remote output steps...
        //
        if (!stepMeta.getRemoteOutputSteps().isEmpty()) {
        	gc.setLineWidth(1);
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
            if (!shadow) {
            	areaOwners.add(new AreaOwner(AreaType.REMOTE_OUTPUT_STEP, point.x, point.y, textExtent.x, textExtent.y, stepMeta, STRING_REMOTE_OUTPUT_STEPS));
            }
        }
        
        // PARTITIONING

        // If this step is partitioned, we're drawing a small symbol indicating this...
        //
        if (stepMeta.isPartitioned()) {
        	gc.setLineWidth(1);
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
	         	
	         	// Also draw the name of the partition schema below the box
	         	//
	         	gc.setForeground(gray);
	         	gc.drawText(Const.NVL(partitionSchema.getName(), "<no partition name>"), point.x, point.y+textExtent.y+3, true);
	         	
	            // Add to the list of areas...
	         	//
	            if (!shadow) {
	            	areaOwners.add(new AreaOwner(AreaType.STEP_PARTITIONING, point.x, point.y, textExtent.x, textExtent.y, stepMeta, STRING_PARTITIONING_CURRENT_STEP));
	            }
            }
        }
                        
        String name = stepMeta.getName();

        if (stepMeta.isSelected())
            gc.setLineWidth(linewidth + 2);
        else
            gc.setLineWidth(linewidth);
        
        // Add to the list of areas...
        if (!shadow) {
        	areaOwners.add(new AreaOwner(AreaType.STEP_ICON, screen.x, screen.y, iconsize, iconsize, transMeta, stepMeta));
        }
        
        // Draw a blank rectangle to prevent alpha channel problems...
        //
        gc.fillRectangle(screen.x, screen.y, iconsize, iconsize);
        String steptype = stepMeta.getStepID();
        Image im = (Image) images.get(steptype);
        if (im != null) // Draw the icon!
        {
            org.eclipse.swt.graphics.Rectangle bounds = im.getBounds();
            gc.drawImage(im, 0, 0, bounds.width, bounds.height, screen.x, screen.y, iconsize, iconsize);
        }
        gc.setBackground(background);
        if (stepError) {
        	gc.setForeground(red);
        } else {
        	gc.setForeground(black);
        }
        gc.drawRectangle(screen.x - 1, screen.y - 1, iconsize + 1, iconsize + 1);

        Point namePosition = getNamePosition(gc, name, screen, iconsize );
        
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
        
        // If there was an error during the run, the map "stepLogMap" is not empty and not null.  
        //
        if (stepError) {
        	String log = stepLogMap.get(stepMeta);
    		// Show an error lines icon in the lower right corner of the step...
    		//
    		int xError = screen.x + iconsize - 5;
    		int yError = screen.y + iconsize - 5;
    		Image image = GUIResource.getInstance().getImageStepError();
    		gc.drawImage(image, xError, yError);
    		if (!shadow) {
    			areaOwners.add(new AreaOwner(AreaType.STEP_ERROR_ICON, pt.x + iconsize-5, pt.y + iconsize-5, image.getBounds().width, image.getBounds().height, log, STRING_STEP_ERROR_LOG));
    		}
        }
        
        // Restore the previous alpha value
        //
        if (fade) {
        	gc.setAlpha(alpha);
        }

        // Optionally drawn the mouse-over information
        //
        if (mouseOverSteps.contains(stepMeta)) {
        	Image[] miniIcons = new Image[] {
                GUIResource.getInstance().getImageHopInput(),
                GUIResource.getInstance().getImageEdit(),
                GUIResource.getInstance().getImageContextMenu(),
                GUIResource.getInstance().getImageHopOutput(),
        	};
        	
        	int totalHeight=0;
        	int totalIconsWidth=0;
        	int totalWidth=2*MINI_ICON_MARGIN;
        	for (Image miniIcon : miniIcons) {
        		Rectangle bounds = miniIcon.getBounds();
        		totalWidth+=bounds.width+MINI_ICON_MARGIN;
        		totalIconsWidth+=bounds.width+MINI_ICON_MARGIN;
        		if (bounds.height>totalHeight) totalHeight=bounds.height;
        	}
        	totalHeight+=2*MINI_ICON_MARGIN;
        	        	
        	gc.setFont(GUIResource.getInstance().getFontSmall());
        	String trimmedName = stepMeta.getName().length()<30 ? stepMeta.getName() : stepMeta.getName().substring(0,30);
        	org.eclipse.swt.graphics.Point nameExtent = gc.textExtent(trimmedName);
        	nameExtent.y+=2*MINI_ICON_MARGIN;
        	nameExtent.x+=3*MINI_ICON_MARGIN;
        	totalHeight+=nameExtent.y;
        	if (nameExtent.x>totalWidth) totalWidth=nameExtent.x;

        	int areaX = screen.x+iconsize/2-totalWidth/2+MINI_ICON_SKEW;
        	int areaY = screen.y+iconsize+MINI_ICON_DISTANCE;

        	gc.setForeground(darkGray);
        	gc.setBackground(lightGray);
        	gc.setLineWidth(1);
        	gc.fillRoundRectangle(areaX, areaY, totalWidth, totalHeight, 7, 7);
        	gc.drawRoundRectangle(areaX, areaY, totalWidth, totalHeight, 7, 7);

        	gc.setBackground(background);
        	gc.fillRoundRectangle(areaX+2, areaY+2, totalWidth-MINI_ICON_MARGIN+1, nameExtent.y-MINI_ICON_MARGIN, 7, 7);
        	gc.setForeground(black);
        	gc.drawText(trimmedName, areaX+(totalWidth-nameExtent.x)/2+MINI_ICON_MARGIN, areaY+MINI_ICON_MARGIN, true);
        	gc.setForeground(darkGray);
        	gc.setBackground(lightGray);

        	gc.setFont(GUIResource.getInstance().getFontGraph());
        	areaOwners.add(new AreaOwner(AreaType.STEP_MINI_ICONS_BALLOON, areaX, areaY, totalWidth, totalHeight, stepMeta, ioMeta));

        	
        	gc.fillPolygon(new int[] { areaX+totalWidth/2-MINI_ICON_TRIANGLE_BASE/2+1, areaY+2, areaX+totalWidth/2+MINI_ICON_TRIANGLE_BASE/2, areaY+2, areaX+totalWidth/2-MINI_ICON_SKEW, areaY-MINI_ICON_DISTANCE-5, });
        	
        	gc.drawPolyline(new int[] { areaX+totalWidth/2-MINI_ICON_TRIANGLE_BASE/2+1, areaY, areaX+totalWidth/2-MINI_ICON_SKEW, areaY-MINI_ICON_DISTANCE-5, areaX+totalWidth/2+MINI_ICON_TRIANGLE_BASE/2, areaY, areaX+totalWidth/2-MINI_ICON_SKEW, areaY-MINI_ICON_DISTANCE-5, });

        	gc.setBackground(background);
        	
        	// Put on the icons...
        	//
        	int xIcon = areaX+(totalWidth-totalIconsWidth)/2+MINI_ICON_MARGIN;
        	int yIcon = areaY+5+nameExtent.y;
        	for (int i=0;i<miniIcons.length;i++) {
        		Image miniIcon = miniIcons[i];
        		Rectangle bounds = miniIcon.getBounds();
        		boolean enabled=false;
        		switch(i) {
        		case 0: // INPUT
        			enabled=ioMeta.isInputAcceptor() || ioMeta.isInputDynamic();
                	areaOwners.add(new AreaOwner(AreaType.STEP_INPUT_HOP_ICON, xIcon, yIcon, bounds.width, bounds.height, stepMeta, ioMeta));
        			break;
        		case 1: // EDIT
        			enabled=true;
                	areaOwners.add(new AreaOwner(AreaType.STEP_EDIT_ICON, xIcon, yIcon, bounds.width, bounds.height, stepMeta, ioMeta));
        			break;
        		case 2: // STEP_MENU
        			enabled=true;
        			areaOwners.add(new AreaOwner(AreaType.STEP_MENU_ICON, xIcon, yIcon, bounds.width, bounds.height, stepMeta, ioMeta));
                	break;
        		case 3: // OUTPUT
        			enabled=ioMeta.isOutputProducer() || ioMeta.isOutputDynamic();
                	areaOwners.add(new AreaOwner(AreaType.STEP_OUTPUT_HOP_ICON, xIcon, yIcon, bounds.width, bounds.height, stepMeta, ioMeta));
        			break;
        		}
        		if (enabled) {
        			gc.setAlpha(255);
        		} else {
        			gc.setAlpha(100);
        		}
        		gc.drawImage(miniIcon, xIcon, yIcon);
        		xIcon+=miniIcon.getBounds().width+5;
        	}
        	
        	// OK, see if we need to show a slide-out for target streams...
        	//
        	if (showTargetStreamsStep!=null) {
        		ioMeta = showTargetStreamsStep.getStepMetaInterface().getStepIOMeta();
        		List<StreamInterface> targetStreams = ioMeta.getTargetStreams();
        		int targetsWidth=0;
        		int targetsHeight=0;;
        		for (int i=0;i<targetStreams.size();i++) {
        			String description = targetStreams.get(i).getDescription(); 
        			org.eclipse.swt.graphics.Point extent = gc.textExtent(description);
        			if (extent.x>targetsWidth) targetsWidth=extent.x;
        			targetsHeight+=extent.y+MINI_ICON_MARGIN;
        		}
        		targetsWidth+=MINI_ICON_MARGIN;
        		
            	gc.setBackground(lightGray);
        		gc.fillRoundRectangle(areaX, areaY+totalHeight+2, targetsWidth, targetsHeight, 7, 7);
        		gc.drawRoundRectangle(areaX, areaY+totalHeight+2, targetsWidth, targetsHeight, 7, 7);

        		int targetY=areaY+totalHeight+MINI_ICON_MARGIN;
        		for (int i=0;i<targetStreams.size();i++) {
        			String description = targetStreams.get(i).getDescription(); 
        			org.eclipse.swt.graphics.Point extent = gc.textExtent(description);
        			gc.drawText(description, areaX+MINI_ICON_MARGIN, targetY, true);
        			if (i<targetStreams.size()-1) {
        				gc.drawLine(areaX+MINI_ICON_MARGIN/2, targetY+extent.y+3, areaX+targetsWidth-MINI_ICON_MARGIN/2, targetY+extent.y+2);
        			}
        			
                	areaOwners.add(new AreaOwner(AreaType.STEP_TARGET_HOP_ICON_OPTION, areaX, targetY, targetsWidth, extent.y+MINI_ICON_MARGIN, stepMeta, targetStreams.get(i)));

        			targetY+=extent.y+MINI_ICON_MARGIN;
        		}
        		
            	gc.setBackground(background);
        	}
        }
        
        /*
        if (streamOptions!=null) {
        	int xOptions = streamOptions.getLocation().x;
        	int yOptions = streamOptions.getLocation().y;
        	int widthOptions = 0;
        	int heightOptions = 0;
        	
        	for (int i=0;i<streamOptions.getOptions().size();i++) {
        		StreamInterface stream = streamOptions.getOptions().get(i);
        		int width = MINI_ICON_MARGIN;
        		int height = 0;
        		Image miniIcon = null;
        		switch(stream.getStreamIcon()) {
        		case TRUE   : miniIcon = GUIResource.getInstance().getImageTrue(); break;
        		case FALSE  : miniIcon = GUIResource.getInstance().getImageTrue(); break;
        		case ERROR  : miniIcon = GUIResource.getInstance().getImageErrorHop(); break;
        		case INFO   : miniIcon = GUIResource.getInstance().getImageInfoHop(); break;
        		case TARGET : miniIcon = GUIResource.getInstance().getImageHopTarget(); break;
        		case INPUT  : miniIcon = GUIResource.getInstance().getImageHopInput(); break;
        		case OUTPUT : miniIcon = GUIResource.getInstance().getImageHopOutput(); break;
        		default: miniIcon = GUIResource.getInstance().getImageArrow(); break;
        		}
        		Rectangle iconBounds = miniIcon.getBounds();
        		width+=iconBounds.width;
        		height+=iconBounds.height;
        		
        		width+=MINI_ICON_MARGIN;
        		org.eclipse.swt.graphics.Point textExtent = gc.textExtent(stream.getDescription());
        		width+=textExtent.x+MINI_ICON_MARGIN;
        		height+=textExtent.y+MINI_ICON_MARGIN;
        		
        		if (width>widthOptions) widthOptions=width;
        		heightOptions+=height;
        	}
        	
        	gc.setBackground(lightGray);
        	gc.setForeground(darkGray);
        	gc.fillRoundRectangle(xOptions, yOptions, widthOptions, heightOptions, 7, 7);
        	gc.drawRoundRectangle(xOptions, yOptions, widthOptions, heightOptions, 7, 7);
        	
        	for (int i=0;i<streamOptions.getOptions().size();i++) {
        		StreamInterface stream = streamOptions.getOptions().get(i);
        	
        	
        }
        */
        

        /*
        if (ioMeta.isInputAcceptor() && !stepMeta.equals(startHopStep) && ((mouseOverSteps.contains(stepMeta)) || showingHopInputIcons) ) {
        	// Draw the input hop icon next to the step...
        	//
        	Image hopInput = GUIResource.getInstance().getImageHopInput();
        	Rectangle inputBounds = hopInput.getBounds();
        	int xIcon = x-inputBounds.width-3;
        	int yIcon = y;
        	gc.drawImage(hopInput, xIcon, yIcon);
        	areaOwners.add(new AreaOwner(AreaType.STEP_INPUT_HOP_ICON, xIcon, yIcon, inputBounds.width, inputBounds.height, stepMeta, ioMeta));
         }
        
        int rightIconIndex=0;
         if (ioMeta.isOutputProducer() && candidate==null && mouseOverSteps.contains(stepMeta) && !showingHopInputIcons) {
        	Image hopOutput= GUIResource.getInstance().getImageHopOutput(); 
        	Rectangle outputBounds = hopOutput.getBounds();
        	int xIcon = x+iconsize+3;
        	int yIcon = y;
        	gc.drawImage(hopOutput, xIcon, yIcon);
        	areaOwners.add(new AreaOwner(AreaType.STEP_OUTPUT_HOP_ICON, xIcon, yIcon, outputBounds.width, outputBounds.height, stepMeta, ioMeta));
        	rightIconIndex++;
         }

         // In case of a step with info streams coming in, we should display all info icons at the top of the step 
         //
         int topIconIndex = 0;
         if ((mouseOverSteps.contains(stepMeta) || showingHopInputIcons) && !stepMeta.equals(startHopStep)) {
	         StreamInterface[] infoStreams = ioMeta.getInfoStreams();
        	 for (int i=0;i<infoStreams.length;i++) {
        		 StreamInterface stream = infoStreams[i];
        		 Image infoImage = GUIResource.getInstance().getImageInfoHop();
        		 Rectangle bounds = infoImage.getBounds();
        		 int xIcon = x+i*(bounds.width+5);
        		 int yIcon = y-bounds.height-3;
        		 gc.drawImage(infoImage, xIcon, yIcon);
             	 areaOwners.add(new AreaOwner(AreaType.STEP_INFO_HOP_ICON, xIcon, yIcon, bounds.width, bounds.height, stepMeta, stream));
             	 topIconIndex++;
        	 }

	         StreamInterface[] targetStreams = ioMeta.getTargetStreams();
        	 for (int i=0;i<targetStreams.length;i++) {
        		 StreamInterface stream = targetStreams[i];
        		 Image targetImage = GUIResource.getInstance().getImageHopTarget();
        		 Rectangle bounds = targetImage.getBounds();
        		 int xIcon = x+iconsize+3;
        		 int yIcon = y+(i+rightIconIndex)*(bounds.height+3);
        		 gc.drawImage(targetImage, xIcon, yIcon);
        		 Font oldFont = gc.getFont();
        		 gc.setFont(GUIResource.getInstance().getFontSmall());
        		 gc.drawText(stream.getDescription(), xIcon+bounds.width+5, yIcon, true);
        		 gc.setFont(oldFont);
        		 areaOwners.add(new AreaOwner(AreaType.STEP_TARGET_HOP_ICON, xIcon, yIcon, bounds.width, bounds.height, stepMeta, stream));
        	 }
         }

         if (stepMeta.supportsErrorHandling() && candidate==null && mouseOverSteps.contains(stepMeta) && !showingHopInputIcons) {
           	Image hopError = GUIResource.getInstance().getImageErrorHop(); 
          	Rectangle bounds = hopError.getBounds();
          	int xIcon = x+topIconIndex*(bounds.width+5);
          	int yIcon = y-bounds.height-3;
          	gc.drawImage(hopError, xIcon, yIcon);
          	areaOwners.add(new AreaOwner(AreaType.STEP_ERROR_HOP_ICON, xIcon, yIcon, bounds.width, bounds.height, stepMeta, ioMeta));
          }
          */
    }

    public static final Point getNamePosition(GC gc, String string, Point screen, int iconsize)
    {
        org.eclipse.swt.graphics.Point textsize = gc.textExtent(string);
        
        int xpos = screen.x + (iconsize / 2) - (textsize.x / 2);
        int ypos = screen.y + iconsize + 5;

        return new Point(xpos, ypos);
    }

    private void drawLine(GC gc, StepMeta fs, StepMeta ts, TransHopMeta hi, boolean is_candidate)
    {
        // StepMetaInterface fsii = fs.getStepMetaInterface();
        // StepMetaInterface tsii = ts.getStepMetaInterface();

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
                if (fs.isSendingErrorRowsToStep(ts))
                {
                    col = red;
                    linestyle = SWT.LINE_DOT;
                    activeLinewidth = linewidth+1;
                }
                else
                {
                	col = black;
                }
            }
            else
            {
                col = gray;
            }
        }
        if (hi.split) activeLinewidth = linewidth+2;

        // Check to see if the source step is an info step for the target step.
        //
        StepIOMetaInterface ioMeta = ts.getStepMetaInterface().getStepIOMeta();
        List<StreamInterface> infoStreams = ioMeta.getInfoStreams();
        if (!infoStreams.isEmpty()) {
        	// Check this situation, the source step can't run in multiple copies!
        	//
        	for (StreamInterface stream : infoStreams) {
        		if (fs.getName().equalsIgnoreCase(stream.getStepname())) {
        			// This is the info step over this hop!
        			//
        			if (fs.getCopies()>1) {
        				// This is not a desirable situation, it will always end in error.
        				// As such, it's better not to give feedback on it.
        				// We do this by drawing an error icon over the hop...
        				//
        				col=red;
        			}
        		}
        	}
        }
        
        gc.setForeground(col);
        gc.setLineStyle(linestyle);
        gc.setLineWidth(activeLinewidth);
        
        drawArrow(gc, line, fs, ts);
        
        if (hi.split) gc.setLineWidth(linewidth);

        gc.setForeground(black);
        gc.setBackground(background);
        gc.setLineStyle(SWT.LINE_SOLID);
    }

    private Point getThumb(Point area, Point transMax)
    {
    	Point resizedMax = magnifyPoint(transMax);
    	
        Point thumb = new Point(0, 0);
        if (resizedMax.x <= area.x)
            thumb.x = 100;
        else
            thumb.x = 100 * area.x / resizedMax.x;

        if (resizedMax.y <= area.y)
            thumb.y = 100;
        else
            thumb.y = 100 * area.y / resizedMax.y;

        return thumb;
    }
    
    private Point magnifyPoint(Point p) {
    	return new Point(Math.round(p.x * magnification), Math.round(p.y*magnification));
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
        // PDI-2619: SWT on Windows doesn't cater for negative rect.width/height so handle here. 
        Point s = real2screen(rect.x, rect.y, offset);
        if (rect.width < 0) {
        	s.x = s.x + rect.width;
        }
        if (rect.height < 0) {
        	s.y = s.y + rect.height;
        }
        gc.drawRectangle(s.x, s.y, Math.abs(rect.width), Math.abs(rect.height));
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
    	Point screen_from = real2screen(line[0], line[1], offset);
        Point screen_to = real2screen(line[2], line[3], offset);
        
        drawArrow(gc, screen_from.x, screen_from.y, screen_to.x, screen_to.y, theta, calcArrowLength(), -1, startObject, endObject);
    }
    
    private int calcArrowLength() {
    	return 19 + (linewidth - 1) * 5; // arrowhead length;
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

        Color fore = gc.getForeground();
        Color back = gc.getBackground();
        gc.setBackground(fore);
        gc.fillPolygon(new int[] { mx, my, x3, y3, x4, y4 });
        gc.setBackground(back);
        
        if ( startObject instanceof StepMeta && endObject instanceof StepMeta) {
        	factor = 0.8;

        	StepMeta fs = (StepMeta)startObject;
        	StepMeta ts = (StepMeta)endObject;
        	
	        // in between 2 points
	        mx = (int) (x1 + factor * (x2 - x1) / 2) - 8;
	        my = (int) (y1 + factor * (y2 - y1) / 2) - 8;
	        
	        boolean errorHop = fs.isSendingErrorRowsToStep(ts) || (startErrorHopStep && fs.equals(startHopStep));
	        boolean targetHop = Const.indexOfString(ts.getName(), fs.getStepMetaInterface().getStepIOMeta().getTargetStepnames())>=0;

	        if (targetHop) {
	        	StepIOMetaInterface ioMeta = fs.getStepMetaInterface().getStepIOMeta();
	        	StreamInterface targetStream = ioMeta.findTargetStream(ts);
	        	if (targetStream!=null) {
		        	Image hopsIcon = TransGraph.getStreamIconImage(targetStream.getStreamIcon());
		        	gc.drawImage(hopsIcon, mx, my);
		        	if (!shadow) {
		    			areaOwners.add(new AreaOwner(AreaType.STEP_TARGET_HOP_ICON, mx, my, hopsIcon.getBounds().width, hopsIcon.getBounds().height, fs, targetStream));
		    		}
	        	}
	        } else  if (!fs.isDistributes() && !ts.getStepPartitioningMeta().isMethodMirror() && !errorHop) {
		        
	        	Image copyHopsIcon = GUIResource.getInstance().getImageCopyHop();
	        	gc.drawImage(copyHopsIcon, mx, my);
	        	
	        	if (!shadow) {
	    			areaOwners.add(new AreaOwner(AreaType.HOP_COPY_ICON, mx, my, copyHopsIcon.getBounds().width, copyHopsIcon.getBounds().height, fs, STRING_HOP_TYPE_COPY));
	    		}
		        mx+=16;
	        } 
	        
	        if (errorHop) {
	        	Image copyHopsIcon = GUIResource.getInstance().getImageErrorHop();
		        gc.drawImage(copyHopsIcon, mx, my);
	        	if (!shadow) {
	    			areaOwners.add(new AreaOwner(AreaType.HOP_ERROR_ICON, mx, my, copyHopsIcon.getBounds().width, copyHopsIcon.getBounds().height, fs, ts));
	    		}
		        mx+=16;
            }
	        
	        StepIOMetaInterface ioMeta = ts.getStepMetaInterface().getStepIOMeta();
	        String[] infoStepnames = ioMeta.getInfoStepnames();
	        
	        if ( (candidateHopType==StreamType.INFO && ts.equals(endHopStep) && fs.equals(startHopStep)) || Const.indexOfString(fs.getName(), infoStepnames) >= 0) {
	        	Image hopIcon = GUIResource.getInstance().getImageInfoHop();
        		gc.drawImage(hopIcon, mx, my);
	        	if (!shadow) {
	    			areaOwners.add(new AreaOwner(AreaType.HOP_INFO_ICON, mx, my, hopIcon.getBounds().width, hopIcon.getBounds().height, fs, ts));
	    		}
		        mx+=16;
	        }
	        
	        // Check to see if the source step is an info step for the target step.
	        //
	        if (!Const.isEmpty(infoStepnames)) {
	        	// Check this situation, the source step can't run in multiple copies!
	        	//
	        	for (String infoStep : infoStepnames) {
	        		if (fs.getName().equalsIgnoreCase(infoStep)) {
	        			// This is the info step over this hop!
	        			//
	        			if (fs.getCopies()>1) {
	        				// This is not a desirable situation, it will always end in error.
	        				// As such, it's better not to give feedback on it.
	        				// We do this by drawing an error icon over the hop...
	        				//
	        	        	Image errorHopsIcon = GUIResource.getInstance().getImageErrorHop();
	        	        	gc.drawImage(errorHopsIcon, mx, my);
	        	        	if (!shadow) {
	        	    			areaOwners.add(new AreaOwner(AreaType.HOP_INFO_STEP_COPIES_ERROR, mx, my, errorHopsIcon.getBounds().width, errorHopsIcon.getBounds().height, fs, ts));
	        	    		}
	        		        mx+=16;
	        				
	        			}
	        		}
	        	}
	        }


        }

    }

	/**
	 * @return the magnification
	 */
	public float getMagnification() {
		return magnification;
	}

	/**
	 * @param magnification the magnification to set
	 */
	public void setMagnification(float magnification) {
		this.magnification = magnification;
	}

	/**
	 * @return the translationX
	 */
	public float getTranslationX() {
		return translationX;
	}

	/**
	 * @param translationX the translationX to set
	 */
	public void setTranslationX(float translationX) {
		this.translationX = translationX;
	}

	/**
	 * @return the translationY
	 */
	public float getTranslationY() {
		return translationY;
	}

	/**
	 * @param translationY the translationY to set
	 */
	public void setTranslationY(float translationY) {
		this.translationY = translationY;
	}

	/**
	 * @return the stepLogMap
	 */
	public Map<StepMeta, String> getStepLogMap() {
		return stepLogMap;
	}

	/**
	 * @param stepLogMap the stepLogMap to set
	 */
	public void setStepLogMap(Map<StepMeta, String> stepLogMap) {
		this.stepLogMap = stepLogMap;
	}
	
	/**
	 * @param startHopStep the startHopStep to set
	 */
	public void setStartHopStep(StepMeta startHopStep) {
		this.startHopStep = startHopStep;
	}

	/**
	 * @param endHopLocation the endHopLocation to set
	 */
	public void setEndHopLocation(Point endHopLocation) {
		this.endHopLocation = endHopLocation;
	}

	/**
	 * @param noInputStep the noInputStep to set
	 */
	public void setNoInputStep(StepMeta noInputStep) {
		this.noInputStep = noInputStep;
	}

	/**
	 * @param endHopStep the endHopStep to set
	 */
	public void setEndHopStep(StepMeta endHopStep) {
		this.endHopStep = endHopStep;
	}

	public void setCandidateHopType(StreamType candidateHopType) {
		this.candidateHopType = candidateHopType;
	}

	public void setStartErrorHopStep(boolean startErrorHopStep) {
		this.startErrorHopStep = startErrorHopStep;
	}

	/**
	 * @return the showTargetStreamsStep
	 */
	public StepMeta getShowTargetStreamsStep() {
		return showTargetStreamsStep;
	}

	/**
	 * @param showTargetStreamsStep the showTargetStreamsStep to set
	 */
	public void setShowTargetStreamsStep(StepMeta showTargetStreamsStep) {
		this.showTargetStreamsStep = showTargetStreamsStep;
	}
	

}
