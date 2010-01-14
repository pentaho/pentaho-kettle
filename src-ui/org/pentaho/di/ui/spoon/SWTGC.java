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
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.pentaho.di.core.gui.GCInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;

public class SWTGC implements GCInterface {

    protected Color        background;
    
    protected Color        black;
    protected Color        red;
    protected Color        yellow;
    protected Color        orange;
    protected Color        green;
    protected Color        blue;
    protected Color        magenta;
    protected Color        gray;
    protected Color        lightGray;
    protected Color        darkGray;

	private GC gc;

	private int	iconsize;

	private Map<String, Image>	images;
	
	private List<Color> colors;
	private List<Font> fonts;

	private Image	image;

	private Point	area;
	
	public SWTGC(Device device, Point area, int iconsize) {
		this.image = new Image(device, area.x, area.y);
		this.gc = new GC(image);
		this.images = GUIResource.getInstance().getImagesSteps();
		this.iconsize = iconsize;
		this.area = area;
		
		this.colors = new ArrayList<Color>();
		this.fonts = new ArrayList<Font>();
		
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

	}
	
	public void dispose() {
		gc.dispose();
		for (Color color : colors) {
			color.dispose();
		}
		for (Font font : fonts) {
			font.dispose();
		}
	}

	public void drawLine(int x, int y, int x2, int y2) {
		gc.drawLine(x, y, x2, y2);
	}

	public void drawImage(EImage image, int x, int y) {
		
		Image img= getNativeImage(image);
		gc.drawImage(img, x, y);
	}
	
	public Point getImageBounds(EImage image) {
		Image img=getNativeImage(image);
		Rectangle r = img.getBounds();
		return new Point(r.width, r.height);
	}
	
	public static final Image getNativeImage(EImage image) {
		switch (image) {
		case LOCK: return GUIResource.getInstance().getImageLocked();
		case STEP_ERROR: return GUIResource.getInstance().getImageStepError();
		case EDIT: return GUIResource.getInstance().getImageEdit();
		case CONTEXT_MENU: return GUIResource.getInstance().getImageContextMenu();
		case TRUE: return GUIResource.getInstance().getImageTrue();
		case FALSE: return GUIResource.getInstance().getImageFalse();
		case ERROR: return GUIResource.getInstance().getImageErrorHop();
		case INFO: return GUIResource.getInstance().getImageInfoHop();
		case TARGET: return GUIResource.getInstance().getImageHopTarget();
		case INPUT: return GUIResource.getInstance().getImageHopInput();
		case OUTPUT: return GUIResource.getInstance().getImageHopOutput();
		case ARROW: return GUIResource.getInstance().getImageArrow();
		case COPY_ROWS: return GUIResource.getInstance().getImageCopyHop();
		case PARALLEL: return GUIResource.getInstance().getImageParallelHop();
		case UNCONDITIONAL: return GUIResource.getInstance().getImageUnconditionalHop();
		case BUSY: return GUIResource.getInstance().getImageBusy();
		}
		return null;
	}

	public void drawPoint(int x, int y) {
		gc.drawPoint(x, y);
	}

	public void drawPolygon(int[] polygon) {
		gc.drawPolygon(polygon);
	}

	public void drawPolyline(int[] polyline) {
		gc.drawPolyline(polyline);
	}

	public void drawRectangle(int x, int y, int width, int height) {
		gc.drawRectangle(x, y, width, height);
	}

	public void drawRoundRectangle(int x, int y, int width, int height, int circleWidth, int circleHeight) {
		gc.drawRoundRectangle(x, y, width, height, circleWidth, circleHeight);
	}

	public void drawText(String text, int x, int y) {
		gc.drawText(text, x, y);
	}

	public void drawText(String text, int x, int y, boolean transparent) {
		gc.drawText(text, x, y, SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT);
	}

	public void fillPolygon(int[] polygon) {
		gc.fillPolygon(polygon);
	}

	public void fillRectangle(int x, int y, int width, int height) {
		gc.fillRectangle(x, y, width, height);
	}

	public void fillRoundRectangle(int x, int y, int width, int height, int circleWidth, int circleHeight) {
		gc.fillRoundRectangle(x, y, width, height, circleWidth, circleHeight);
	}

	public Point getDeviceBounds() {
		org.eclipse.swt.graphics.Rectangle p = gc.getDevice().getBounds();
		return new Point(p.width, p.height);
	}

	public void setAlpha(int alpha) {
		gc.setAlpha(alpha);
	}
	
	public int getAlpha() {
		return gc.getAlpha();
	}

	public void setBackground(EColor color) {
		gc.setBackground(getColor(color));
	}

	private Color getColor(EColor color) {
		switch(color) {
		case BACKGROUND: return background;
		case BLACK: return black;
		case RED: return red;
		case YELLOW: return yellow;
		case ORANGE: return orange;
		case GREEN: return green;
		case BLUE: return blue;
		case MAGENTA: return magenta;
		case GRAY: return gray;
		case LIGHTGRAY: return lightGray;
		case DARKGRAY: return darkGray;
		}
		return null;
	}
	
	public void setFont(EFont font) {
		switch(font) {
		case GRAPH : gc.setFont(GUIResource.getInstance().getFontGraph()); break;
		case NOTE : gc.setFont(GUIResource.getInstance().getFontNote()); break;
		case SMALL: gc.setFont(GUIResource.getInstance().getFontSmall()); break;
		}
	}

	public void setForeground(EColor color) {
		gc.setForeground(getColor(color));
	}

	public void setLineStyle(ELineStyle lineStyle) {
		switch(lineStyle) {
		case DASHDOT : gc.setLineStyle(SWT.LINE_DASHDOT); break;
		case SOLID : gc.setLineStyle(SWT.LINE_SOLID); break;
		case DOT : gc.setLineStyle(SWT.LINE_DOT); break;
		case PARALLEL: 
			gc.setLineAttributes(new LineAttributes((float) gc.getLineWidth(), SWT.CAP_FLAT, SWT.JOIN_MITER, SWT.LINE_CUSTOM, new float[] { 5, 3, }, 0, 10));
			break;
		}
	}

	public void setLineWidth(int width) {
		gc.setLineWidth(width);
	}

	public void setTransform(float translationX, float translationY, int shadowsize, float magnification) {
    	Transform transform = new Transform(gc.getDevice());
    	transform.translate(translationX+shadowsize*magnification, translationY+shadowsize*magnification);
    	transform.scale(magnification, magnification);
    	gc.setTransform(transform);
	}
	
	public Point textExtent(String text) {
		org.eclipse.swt.graphics.Point p = gc.textExtent(text);
		return new Point(p.x, p.y);
	}

	public void drawStepIcon(int x, int y, StepMeta stepMeta) {
        // Draw a blank rectangle to prevent alpha channel problems...
        //
		gc.fillRectangle(x, y, iconsize, iconsize);
        String steptype = stepMeta.getStepID();
        Image im = (Image) images.get(steptype);
        if (im != null) // Draw the icon!
        {
            org.eclipse.swt.graphics.Rectangle bounds = im.getBounds();
            gc.drawImage(im, 0, 0, bounds.width, bounds.height, x, y, iconsize, iconsize);
        }
	}
	
	public void drawJobEntryIcon(int x, int y, JobEntryCopy jobEntryCopy) {
			    if (jobEntryCopy == null)
			      return; // Don't draw anything

			    Image image = null;

			    if (jobEntryCopy.isSpecial()) {
			        if (jobEntryCopy.isStart()) {
			          image = GUIResource.getInstance().getImageStart();
			        }
			        if (jobEntryCopy.isDummy()) {
			          image = GUIResource.getInstance().getImageDummy();
			        }
			    } else {
			        String configId = jobEntryCopy.getEntry().getConfigId();
			        if (configId != null) {
			          image = GUIResource.getInstance().getImagesJobentries().get(configId);
			        }
			    }
			    if (image==null) {
			    	return;
			    }
			    
	            org.eclipse.swt.graphics.Rectangle bounds = image.getBounds();
	            gc.drawImage(image, 0, 0, bounds.width, bounds.height, x, y, iconsize, iconsize);
	}

	public void setAntialias(boolean antiAlias) {
		if (antiAlias) {
			gc.setAntialias(SWT.ON);
		} else {
			gc.setAntialias(SWT.OFF);
		}
	}
	
	public void setBackground(int r, int g, int b) {
		Color color = getColor(r, g, b);
		gc.setBackground(color);
	}

	public void setForeground(int r, int g, int b) {
		Color color = getColor(r, g, b);
		gc.setForeground(color);
	}

	private Color getColor(int r, int g, int b) {
		Color color = new Color(PropsUI.getDisplay(), new RGB(r,g,b));
		int index = colors.indexOf(color);
		if (index<0) {
			colors.add(color);
		} else {
			color.dispose();
			color = colors.get(index);
		}
		return color;
	}

	public void setFont(String fontName, int fontSize, boolean fontBold, boolean fontItalic) {
		int swt=SWT.NORMAL;
        if(fontBold) swt=SWT.BOLD;
        if(fontItalic) swt=swt | SWT.ITALIC;
        
		Font font = new Font(PropsUI.getDisplay(), fontName, fontSize, swt);
		int index = fonts.indexOf(font);
		if (index<0) {
			fonts.add(font);
		} else {
			font.dispose();
			font = fonts.get(index);
		}
		gc.setFont(font);
	}

	public Object getImage() {
		return image;
	}
	
	public void switchForegroundBackgroundColors() {
		Color fg = gc.getForeground();
		Color bg = gc.getBackground();
		
		gc.setForeground(bg);
		gc.setBackground(fg);
	}
	
	public Point getArea() {
		return area;
	}
}
