package be.ibridge.kettle.spoon;

import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.NotePadMeta;
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Rectangle;
import be.ibridge.kettle.trans.TransHopMeta;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;

public class TransPainter
{
    private Props        props;
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
    private Hashtable    images;

    public TransPainter(TransMeta transMeta, Point area)
    {
        this(transMeta, area, null, null, null, null, null);
    }

    public TransPainter(TransMeta transMeta, 
                        Point area, 
                        ScrollBar hori, ScrollBar vert, 
                        TransHopMeta candidate, Point drop_candidate, Rectangle selrect
                        )
    {
        this.transMeta     = transMeta;
        
        this.background    = GUIResource.getInstance().getColorBackground();
        this.black         = GUIResource.getInstance().getColorBlack();
        this.red           = GUIResource.getInstance().getColorRed();
        this.yellow        = GUIResource.getInstance().getColorYellow();
        this.orange        = GUIResource.getInstance().getColorOrange();
        this.green         = GUIResource.getInstance().getColorGreen();
        this.blue          = GUIResource.getInstance().getColorBlue();
        this.magenta       = GUIResource.getInstance().getColorMagenta();
        this.gray          = GUIResource.getInstance().getColorGray();
        this.lightGray     = GUIResource.getInstance().getColorLightGray();
        this.darkGray      = GUIResource.getInstance().getColorDarkGray();
        
        this.area          = area;
        this.hori          = hori;
        this.vert          = vert;
        this.noteFont      = GUIResource.getInstance().getFontNote();
        this.graphFont     = GUIResource.getInstance().getFontGraph();
        this.images        = GUIResource.getInstance().getImagesSteps();
        this.candidate     = candidate;
        this.selrect       = selrect;
        this.drop_candidate= drop_candidate;
        
        props = Props.getInstance();
        iconsize = props.getIconSize(); 
        linewidth = props.getLineWidth();
    }
    
    public Image getTransformationImage()
    {
        Display disp = GUIResource.getInstance().getDisplay();
        Image img = new Image(disp, area.x, area.y);
        GC gc = new GC(img);
        // First clear the image in the background color
        gc.setBackground(background);
        gc.fillRectangle(0, 0, area.x, area.y);
        
        // Draw the transformation onto the image
        drawTrans(gc);
        
        gc.dispose();
        
        return img;
    }

    private void drawTrans(GC gc)
    {
        if (props.isAntiAliasingEnabled() && Const.getOS().startsWith("Windows")) gc.setAntialias(SWT.ON);
        
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
            Point screen = real2screen(drop_candidate.x, drop_candidate.y);
            gc.drawRectangle(screen.x, screen.y,          iconsize, iconsize);
        }

        drawRect(gc, selrect);
    }

    private void drawHop(GC gc, TransHopMeta hi)
    {
        drawHop(gc, hi, false);
    }

    private void drawNote(GC gc, NotePadMeta ni)
    {
        int flags = SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT;

        if (ni.isSelected()) gc.setLineWidth(2); else gc.setLineWidth(1);
        
        org.eclipse.swt.graphics.Point ext = gc.textExtent(ni.getNote(), flags); 
        Point p = new Point(ext.x, ext.y);
        Point loc = ni.getLocation();
        Point note = real2screen(loc.x, loc.y);
        int margin = Const.NOTE_MARGIN;
        p.x += 2 * margin;
        p.y += 2 * margin;
        int width = ni.width;
        int height = ni.height;
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
        gc.drawText(ni.getNote(), note.x + margin, note.y + margin, flags);

        ni.width = width; // Save for the "mouse" later on...
        ni.height = height;

        if (ni.isSelected()) gc.setLineWidth(1); else gc.setLineWidth(2);
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

        Point screen = real2screen(x, y);

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

        Point screen = real2screen(x, y);

        String name = stepMeta.getName();

        if (stepMeta.isSelected())
            gc.setLineWidth(linewidth + 2);
        else
            gc.setLineWidth(linewidth);
        gc.setBackground(red);
        gc.setForeground(black);
        gc.fillRectangle(screen.x, screen.y, iconsize, iconsize);
        String steptype = stepMeta.getStepID();
        Image im = (Image) images.get(steptype);
        if (im != null) // Draw the icon!
        {
            org.eclipse.swt.graphics.Rectangle bounds = im.getBounds();
            gc.drawImage(im, 0, 0, bounds.width, bounds.height, screen.x, screen.y, iconsize, iconsize);
        }
        gc.setBackground(background);
        gc.drawRectangle(screen.x - 1, screen.y - 1, iconsize + 1, iconsize + 1);
        //gc.setXORMode(true);
        org.eclipse.swt.graphics.Point textsize = gc.textExtent(name);

        int xpos = screen.x + (iconsize / 2) - (textsize.x / 2);
        int ypos = screen.y + iconsize + 5;

        if (shadowsize > 0)
        {
            gc.setForeground(lightGray);
            gc.drawText(name, xpos + shadowsize, ypos + shadowsize, SWT.DRAW_TRANSPARENT);
        }

        gc.setForeground(black);
        gc.drawText(name, xpos, ypos, SWT.DRAW_TRANSPARENT);

        if (stepMeta.getCopies() > 1)
        {
            gc.setBackground(background);
            gc.setForeground(black);
            gc.drawText("x" + stepMeta.getCopies(), screen.x - 5, screen.y - 5);
        }
    }

    private void drawLineShadow(GC gc, StepMeta fs, StepMeta ts, TransHopMeta hi, boolean is_candidate)
    {
        int line[] = getLine(fs, ts);
        int s = shadowsize;
        for (int i = 0; i < line.length; i++)
            line[i] += s;

        gc.setLineWidth(linewidth);
        
        gc.setForeground(lightGray);

        drawArrow(gc, line);
    }

    private void drawLine(GC gc, StepMeta fs, StepMeta ts, TransHopMeta hi, boolean is_candidate)
    {
        StepMetaInterface fsii = fs.getStepMetaInterface();
        StepMetaInterface tsii = ts.getStepMetaInterface();

        int line[] = getLine(fs, ts);

        gc.setLineWidth(linewidth);
        Color col;

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

                if (targetSteps == null) // Normal link: distribute or copy data...
                {
                    // Or perhaps it's an informational link: draw different
                    // color...
                    if (Const.indexOfString(fs.getName(), infoSteps) >= 0)
                    {
                        if (fs.distributes)
                            col = yellow;
                        else
                            col = magenta;
                    }
                    else
                    {
                        if (fs.distributes)
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
                        gc.setLineStyle(SWT.LINE_DOT);
                        col = orange;
                    }
                }
            }
            else
            {
                col = gray;
            }
        }

        gc.setForeground(col);

        if (hi.split) gc.setLineWidth(linewidth + 2);

        drawArrow(gc, line);

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
    
    public Point real2screen(int x, int y)
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
    
    private void drawArrow(GC gc, int line[])
    {
        double theta = Math.toRadians(10); // arrowhead sharpness
        int size = 30 + (linewidth - 1) * 5; // arrowhead length

        Point screen_from = real2screen(line[0], line[1]);
        Point screen_to = real2screen(line[2], line[3]);

        int mx, my;
        int x1 = screen_from.x;
        int y1 = screen_from.y;
        int x2 = screen_to.x;
        int y2 = screen_to.y;
        int x3;
        int y3;
        int x4;
        int y4;
        int a, b, dist;
        double factor, angle;

        gc.drawLine(x1, y1, x2, y2);

        // in between 2 points
        mx = x1 + (x2 - x1) / 2;
        my = y1 + (y2 - y1) / 2;

        a = Math.abs(x2 - x1);
        b = Math.abs(y2 - y1);
        dist = (int) Math.sqrt(a * a + b * b);

        // determine factor (position of arrow to left side or right side
        // 0-->100%)
        if (dist >= 2 * iconsize)
            factor = 1.5;
        else
            factor = 1.2;

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
    }

}
