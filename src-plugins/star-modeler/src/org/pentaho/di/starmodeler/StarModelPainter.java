package org.pentaho.di.starmodeler;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.GCInterface;
import org.pentaho.di.core.gui.GCInterface.EColor;
import org.pentaho.di.core.gui.Point;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.TableType;
import org.pentaho.pms.schema.concept.DefaultPropertyID;

public class StarModelPainter {
  private LogicalModel logicalModel;
  private GCInterface gc;
  private Point area;
  private String locale;
  private List<LogicalRelationship> logicalRelationships;
  
  /**
   * @param gc The graphical context to draw the logical model on 
   * @param logicalModel The model to depict
   * @param logicalRelationships 
   * @param relationships the relationships (dynamically derived from a UI)
   * @param locale the locale to use to draw names of tables.
   * 
   */
  public StarModelPainter(GCInterface gc, LogicalModel logicalModel, List<LogicalRelationship> logicalRelationships, String locale) {
    this.gc = gc;
    this.area = gc.getArea();
    this.logicalModel = logicalModel;
    this.logicalRelationships = logicalRelationships;
    this.locale = locale;
  }
  
  public void draw() {
    gc.setAntialias(true);
    
    Point center = new Point(area.x/2, area.y/2);
    gc.setBackground(EColor.BACKGROUND);
    gc.setForeground(EColor.BLACK);
    gc.fillRectangle(0,0,area.x, area.y);
    
    // gc.drawText("bounds: x="+rect.x+", y="+rect.y+", height="+rect.height+", width="+rect.width, 10, 10);
    
    List<LogicalTable> tableList = new ArrayList<LogicalTable>();
    tableList.addAll(logicalModel.getLogicalTables());
    
    // Find the fact...
    //
    LogicalTable fact = null;
    for (LogicalTable logicalTable : tableList) {
      if (TableType.FACT == ConceptUtil.getTableType(logicalTable)) {
        fact=logicalTable;
      }
    }
    if (fact!=null) {
      tableList.remove(fact);
    }
    
    int maxWidth = Integer.MIN_VALUE;
    for (LogicalTable table : tableList) {
      String name = table.getName(locale);
      if (!Const.isEmpty(name)) {
        Point p = gc.textExtent(name);
        if (p.x>maxWidth) maxWidth=p.x;
      }
    }
    
    List<TablePoint> points = new ArrayList<TablePoint>();
    if (fact!=null) {
      points.add(new TablePoint(fact, center));
    }
    
    // Draw the other dimensions around the fact...
    //
    if (!tableList.isEmpty()) {
      List<TablePoint> dimPoints = getCirclePoints(center, center.x-maxWidth/2-20, center.y-20, tableList);
      points.addAll(dimPoints);
    }

    // Draw relationships first...
    //
    for (LogicalRelationship rel : logicalRelationships) {
      LogicalTable fromTable = rel.getFromTable();
      LogicalTable toTable = rel.getToTable();
      Point from = findPointOfTable(points, fromTable);
      Point to = findPointOfTable(points, toTable);
      if (from!=null && to!=null) {
        gc.drawLine(from.x, from.y, to.x, to.y);
      }
    }
    
    // Then fill and draw all the ovals.
    //
    for (TablePoint tablePoint : points) {
      LogicalTable table = tablePoint.logicalTable;
      Point point = tablePoint.point;
      drawCircleName(point, table);
    }

  }
  
  private class TablePoint {
    public LogicalTable logicalTable;
    public Point point;
    private TablePoint(LogicalTable logicalTable, Point point) {
      this.logicalTable = logicalTable;
      this.point = point;
    }
  }
  
  private List<TablePoint> getCirclePoints(Point center, int width, int heigth, List<LogicalTable> tableList) {
    List<TablePoint> points = new ArrayList<TablePoint>();
    int nrPoints = tableList.size();
    double alpha = Math.PI * 2 / nrPoints;
    for (int i=0;i<nrPoints;i++) {
      double tetha = alpha*i;
      Point point = new Point(center.x, center.y);
      point.x += (int)Math.round(Math.cos(tetha)*width);
      point.y += (int)Math.round(Math.sin(tetha)*(heigth-5));
      
      points.add(new TablePoint(tableList.get(i), point));
    }
    
    return points;
  }

  private Point findPointOfTable(List<TablePoint> points, LogicalTable fromTable) {
    for (TablePoint tablePoint : points) {
      if (tablePoint.logicalTable.equals(fromTable)) {
        return tablePoint.point;
      }
    }
    return null;
  }
  
  private void drawCircleName(Point center, LogicalTable logicalTable) {
    String name = ConceptUtil.getName(logicalTable, locale);
    if (!Const.isEmpty(name)) {
      Point nameSize = gc.textExtent(name);
      int margin=20;
      nameSize.x+=margin;
      nameSize.y+=margin;
      EColor bg = EColor.BACKGROUND;
      TableType tableType = (TableType) logicalTable.getProperty(DefaultPropertyID.TABLE_TYPE.getId());
      if (tableType!=null && TableType.FACT==tableType) {
        bg = EColor.LIGHTGRAY;
      }
      gc.setBackground(bg);
      
      gc.fillRoundRectangle(center.x-nameSize.x/2, center.y-nameSize.y/2, nameSize.x, nameSize.y, 20, 20);
      gc.drawRoundRectangle(center.x-nameSize.x/2, center.y-nameSize.y/2, nameSize.x, nameSize.y, 20, 20);
      gc.drawText(name, center.x-nameSize.x/2+margin/2, center.y-nameSize.y/2+margin/2, true);
      gc.setBackground(EColor.BACKGROUND);
    }
  }
    


  
  
  
  /**
   * @return the logicalModel
   */
  public LogicalModel getLogicalModel() {
    return logicalModel;
  }
  /**
   * @param logicalModel the logicalModel to set
   */
  public void setLogicalModel(LogicalModel logicalModel) {
    this.logicalModel = logicalModel;
  }
  /**
   * @return the gc
   */
  public GCInterface getGc() {
    return gc;
  }
  /**
   * @param gc the gc to set
   */
  public void setGc(GCInterface gc) {
    this.gc = gc;
  }
}
