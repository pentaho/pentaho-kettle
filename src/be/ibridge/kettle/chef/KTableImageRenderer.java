
/*******************************************************************************
 * Copyright (C) 2004 by Friederich Kupzog Elektronik & Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Friederich Kupzog - initial API and implementation
 *    	fkmk@kupzog.de
 *		www.kupzog.de/fkmk
 *******************************************************************************/ 


package be.ibridge.kettle.chef;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import be.ibridge.kettle.core.Const;
import de.kupzog.ktable.KTableCellRenderer;

/**
 * @author Friederich Kupzog
 */
public class KTableImageRenderer 
extends KTableCellRenderer 
{
	protected Display m_Display;
	
	
	public KTableImageRenderer() 
	{
		m_Display = Display.getCurrent();
	}
	
	public int getOptimalWidth(
		GC gc, 
		int col, 
		int row, 
		Object content, 
		boolean fixed)
	{
		return Const.ICON_SIZE;
	}
	
	
	public void drawCell(GC gc, 
		Rectangle rect, 
		int col, 
		int row, 
		Object content, 
		boolean focus, 
		boolean fixed,
		boolean clicked)
	{
		if (content==null) return;
		if (row == 0) // header! 
		{
			gc.setForeground( m_Display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND) );
			gc.setBackground( m_Display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND) );
			rect.width++;
			rect.height++;
			gc.fillRectangle(rect);
			gc.setBackground( m_Display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND) );
			gc.setForeground( m_Display.getSystemColor(SWT.COLOR_BLACK ) );
			Point te = gc.textExtent((String)content);
			gc.drawText((String)content, rect.x+2, rect.y+(rect.height-te.y)/2);
		}
		else // Image
		{
			Image image = (Image)content;
			gc.drawImage(image,rect.x, rect.y);
		}
	}
	


}
