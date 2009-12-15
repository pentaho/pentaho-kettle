package org.pentaho.di.ui.spoon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.containers.XulDeck;

/**
 * Singleton Object controlling SpoonPerspectives.
 * 
 * @author nbaker
 *
 */
public class SpoonPerspectiveManager {
  private static SpoonPerspectiveManager instance = new SpoonPerspectiveManager();
  private Map<Class<? extends SpoonPerspective>, SpoonPerspective> perspectives = new LinkedHashMap<Class<? extends SpoonPerspective>, SpoonPerspective>();
  private XulDeck deck;
  private SpoonPerspective activePerspective;
  private XulDomContainer domContainer;
  
  private SpoonPerspectiveManager(){
    
  }
  
  public static SpoonPerspectiveManager getInstance(){
    return instance;
  }
  
  public void setDeck(XulDeck deck){
    this.deck = deck;
  }
  
  public void setXulDoc(XulDomContainer doc){
    this.domContainer = doc;
  }
  
  public void addPerspective(SpoonPerspective perspective){
    if(activePerspective == null){
      activePerspective = perspective;
    }
    perspectives.put(perspective.getClass(), perspective);
  }
  
  public List<SpoonPerspective> getPerspectives(){
    return Collections.unmodifiableList(new ArrayList<SpoonPerspective>(perspectives.values()));
  }
  
  private void unloadPerspective(SpoonPerspective per){
    per.setActive(false);
    List<XulOverlay> overlays = per.getOverlays();
    if(overlays != null){
      for(XulOverlay overlay : overlays){
        try {
          domContainer.removeOverlay(overlay.getOverlayUri());
        } catch (XulException e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  public boolean activatePerspective(Class<? extends SpoonPerspective> clazz) throws KettleException{

    SpoonPerspective sp = perspectives.get(clazz);
    if(sp == null){
      throw new KettleException("Could not locate perspective by class: "+clazz);
    }
    unloadPerspective(activePerspective);
    activePerspective = sp;

    List<XulOverlay> overlays = sp.getOverlays();
    if(overlays != null){
      for(XulOverlay overlay : overlays){
        try {
          domContainer.loadOverlay(overlay.getOverlayUri());
        } catch (XulException e) {
          e.printStackTrace();
        }
      }
    }
    
    sp.setActive(true);
    deck.setSelectedIndex(deck.getChildNodes().indexOf(deck.getElementById("perspective-"+sp.getId())));
    
    return true;
  }
  
  public SpoonPerspective getActivePerspective(){
    return activePerspective;
  }
  
}
