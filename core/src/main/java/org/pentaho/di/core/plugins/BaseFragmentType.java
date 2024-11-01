/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.plugins;

import org.pentaho.di.core.exception.KettlePluginException;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

public abstract class BaseFragmentType extends BasePluginType {

  BaseFragmentType( Class<? extends Annotation> pluginType, String id, String name, Class<? extends PluginTypeInterface> typeToTrack ) {
    super( pluginType, id, name );
    populateFolders( null );
    initListeners( this.getClass(), typeToTrack );
  }

  protected void initListeners( Class<? extends PluginTypeInterface> aClass, Class<? extends PluginTypeInterface> typeToTrack ) {
    // keep track of new fragments
    registry.addPluginListener( aClass, new FragmentTypeListener( registry, typeToTrack ) {
      /**
       * Keep track of new Fragments, keep note of the method signature's order
       * @param fragment The plugin fragment to merge
       * @param plugin The plugin to be merged
       */
      @Override
      void mergePlugin( PluginInterface fragment, PluginInterface plugin ) {
        if ( plugin != null ) {
          plugin.merge( fragment );
        }
      }
    } );

    // start listening to interested parties
    registry.addPluginListener( typeToTrack, new FragmentTypeListener( registry, aClass ) {
      /**
       * Keep track of new Fragments, keep note of the method signature's order
       * @param plugin The plugin to be merged
       * @param fragment The plugin fragment to merge
       */
      @Override
      void mergePlugin( PluginInterface plugin, PluginInterface fragment ) {
        if ( plugin != null ) {
          plugin.merge( fragment );
        }
      }
    } );
  }

  @Override
  public boolean isFragment() {
    return true;
  }

  @Override protected URLClassLoader createUrlClassLoader( String pluginClassName, URL jarFileUrl, ClassLoader classLoader ) {
    return new KettleURLClassLoader( new URL[]{ jarFileUrl }, classLoader );
  }

  @Override protected void registerNatives() throws KettlePluginException { }

  @Override protected void registerXmlPlugins() throws KettlePluginException { }

  @Override
  protected String extractName( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractDesc( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractCategory( Annotation annotation ) {
    return null;
  }

  @Override
  protected boolean extractSeparateClassLoader( Annotation annotation ) {
    return false;
  }

  @Override
  protected String extractI18nPackageName( Annotation annotation ) {
    return null;
  }

  @Override
  protected void addExtraClasses( Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation ) { }

  protected abstract class FragmentTypeListener implements PluginTypeListener {
    private final PluginRegistry registry;
    private final Class<? extends PluginTypeInterface> typeToTrack;

    FragmentTypeListener( PluginRegistry registry, Class<? extends PluginTypeInterface> typeToTrack ) {
      this.registry = registry;
      this.typeToTrack = typeToTrack;
    }

    abstract void mergePlugin( PluginInterface left, PluginInterface right );

    @Override
    public void pluginAdded( Object serviceObject ) {
      PluginInterface left = (PluginInterface) serviceObject;
      PluginInterface right = registry.findPluginWithId( typeToTrack, left.getIds()[0] );
      mergePlugin( left, right );
    }

    @Override
    public void pluginRemoved( Object serviceObject ) { }

    @Override
    public void pluginChanged( Object serviceObject ) {
      pluginAdded( serviceObject );
    }
  }
}
