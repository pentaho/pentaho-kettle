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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.tags.SwtDeck;
import org.pentaho.ui.xul.swt.tags.SwtToolbarbutton;

/**
 * Singleton Object controlling SpoonPerspectives.
 * 
 * A Perspective is an optional Spoon mode that can be added by a SpoonPlugin.
 * Perspectives take over the look of the entire application by replacing the main UI area.
 * 
 * @author nbaker
 *
 */
public class SpoonPerspectiveManager {
  private static SpoonPerspectiveManager instance = new SpoonPerspectiveManager();
  private Map<Class<? extends SpoonPerspective>, SpoonPerspective> perspectives = new LinkedHashMap<Class<? extends SpoonPerspective>, SpoonPerspective>();
  private List<SpoonPerspective> orderedPerspectives = new ArrayList<SpoonPerspective>();
  private XulDeck deck;
  private SpoonPerspective activePerspective;
  private XulDomContainer domContainer;


  private static class SpoonPerspectiveComparator implements Comparator<SpoonPerspective> {
    public int compare(SpoonPerspective o1, SpoonPerspective o2) {
      return o1.getId().compareTo(o2.getId());
    }
  }
  
  private SpoonPerspectiveManager(){

  }
  
  /**
   * Returns the single instance of this class.
   * 
   * @return SpoonPerspectiveManager instance.
   */
  public static SpoonPerspectiveManager getInstance(){
    return instance;
  }
  
  /**
   * Sets the deck used by the Perspective Manager to display Perspectives in.
   * 
   * @param deck
   */
  public void setDeck(XulDeck deck){
    this.deck = deck;
  }
  
  /**
   * Receives the main XUL document comprising the menuing system and main layout of Spoon. 
   * Perspectives are able to modify these areas when activated. Any other areas need to be 
   * modified via a SpoonPlugin.
   * 
   * @param doc
   */
  public void setXulDoc(XulDomContainer doc){
    this.domContainer = doc;
  }
  
  /**
   * Adds a SpoonPerspective making it available to be activated later.
   * 
   * @param perspective
   */
  public void addPerspective(SpoonPerspective perspective){
    if(activePerspective == null){
      activePerspective = perspective;
    }
    perspectives.put(perspective.getClass(), perspective);
    orderedPerspectives.add(perspective);
    if(domContainer != null){
      initialize();
    }
  }
  
  /**
   * Returns an unmodifiable List of perspectives in no set order.
   * 
   * @return
   */
  public List<SpoonPerspective> getPerspectives(){
    return Collections.unmodifiableList(new ArrayList<SpoonPerspective>(orderedPerspectives));
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
    Spoon.getInstance().enableMenus();
  }
  
  /**
   * 
   * Activates the given instance of the class literal passed in. Activating a perspective first 
   * deactivates the current perspective removing any overlays its applied to the UI. It then switches
   * the main deck to display the perspective UI and applies the optional overlays to the main Spoon
   * XUL container.
   * 
   * @param clazz SpoonPerspective class literal  
   * @throws KettleException throws a KettleException if no perspective is found for the given parameter
   */
  public void activatePerspective(Class<? extends SpoonPerspective> clazz) throws KettleException{

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
          ResourceBundle res = null;
          if(overlay.getResourceBundleUri() != null){
            try{
              res = ResourceBundle.getBundle(overlay.getResourceBundleUri());
            } catch(MissingResourceException ignored){}
          } else {
            try{
              res = ResourceBundle.getBundle(overlay.getOverlayUri().replace(".xul", ".properties"));
            } catch(MissingResourceException ignored){}
          }
          if(res == null){
            res = new XulSpoonResourceBundle(sp.getClass());
          }
          domContainer.loadOverlay(overlay.getOverlayUri(), res);
        } catch (XulException e) {
          e.printStackTrace();
        }
      }
    }

    List<XulEventHandler> theXulEventHandlers = sp.getEventHandlers();
    if(theXulEventHandlers != null) {
      for (XulEventHandler handler : theXulEventHandlers) {
        domContainer.addEventHandler(handler);
      }
    }
    
    sp.setActive(true);
    deck.setSelectedIndex(deck.getChildNodes().indexOf(deck.getElementById("perspective-"+sp.getId())));
    Spoon.getInstance().enableMenus();
  }
  
  /**
   * Returns the current active perspective.
   * @return active SpoonPerspective
   */
  public SpoonPerspective getActivePerspective(){
    return activePerspective;
  }

  public void removePerspective(SpoonPerspective per) {
    perspectives.remove(per);
    orderedPerspectives.remove(per);
    Document document = domContainer.getDocumentRoot();

    XulComponent comp = document.getElementById("perspective-" + per.getId());
    comp.getParent().removeChild(comp);

    comp = document.getElementById("perspective-btn-" + per.getId());
    comp.getParent().removeChild(comp);
    XulToolbar mainToolbar = (XulToolbar) domContainer.getDocumentRoot().getElementById("main-toolbar");
    ((Composite) mainToolbar.getManagedObject()).layout(true, true);

    deck.setSelectedIndex(0);

  }

  private List<SpoonPerspective> installedPerspectives = new ArrayList<SpoonPerspective>();

  public void initialize(){
    XulToolbar mainToolbar = (XulToolbar) domContainer.getDocumentRoot().getElementById("main-toolbar");
    SwtDeck deck = (SwtDeck) domContainer.getDocumentRoot().getElementById("canvas-deck");


    boolean firstBtn = true;
    int y = 0;

    for (SpoonPerspective per : SpoonPerspectiveManager.getInstance().getPerspectives()) {
      if(installedPerspectives.contains(per)){
        y++;
        continue;
      }
      String name = per.getDisplayName(LanguageChoice.getInstance().getDefaultLocale());
      InputStream in = per.getPerspectiveIcon();

      SwtToolbarbutton btn = null;
      try {
        btn = (SwtToolbarbutton) domContainer.getDocumentRoot().createElement("toolbarbutton");
      } catch (XulException e) {
        e.printStackTrace();
      }
      btn.setType("toggle");
      btn.setLabel(name);
      btn.setTooltiptext(name);
      btn.setOnclick("spoon.loadPerspective(" + y + ")");
      btn.setId("perspective-btn-" + per.getId());
      mainToolbar.addChild(btn);
      if (firstBtn) {
        btn.setSelected(true);
        firstBtn = false;
      }
      if (in != null) {
        btn.setImageFromStream(in);
        try {
          in.close();
        } catch (IOException e1) {
        }
      }

      XulVbox box = deck.createVBoxCard();
      box.setId("perspective-" + per.getId());
      box.setFlex(1);
      deck.addChild(box);

      per.getUI().setParent((Composite) box.getManagedObject());
      per.getUI().layout();
      ((Composite) mainToolbar.getManagedObject()).layout(true, true);

      final SwtToolbarbutton finalBtn = btn;
      per.addPerspectiveListener(new SpoonPerspectiveListener() {
        public void onActivation() {
          finalBtn.setSelected(true);
        }

        public void onDeactication() {
          finalBtn.setSelected(false);
        }
      });
      y++;
      installedPerspectives.add(per);
    }
    deck.setSelectedIndex(0);

  }

  
}
