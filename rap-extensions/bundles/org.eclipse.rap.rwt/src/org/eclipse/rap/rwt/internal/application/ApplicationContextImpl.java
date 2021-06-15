/*******************************************************************************
 * Copyright (c) 2011, 2015 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.application;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletContext;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.ExceptionHandler;
import org.eclipse.rap.rwt.internal.client.ClientSelector;
import org.eclipse.rap.rwt.internal.lifecycle.EntryPointManager;
import org.eclipse.rap.rwt.internal.lifecycle.LifeCycleFactory;
import org.eclipse.rap.rwt.internal.lifecycle.PhaseListenerManager;
import org.eclipse.rap.rwt.internal.remote.MessageChainElement;
import org.eclipse.rap.rwt.internal.remote.MessageChainReference;
import org.eclipse.rap.rwt.internal.remote.MessageFilter;
import org.eclipse.rap.rwt.internal.remote.MessageFilterChain;
import org.eclipse.rap.rwt.internal.resources.ClientResources;
import org.eclipse.rap.rwt.internal.resources.ResourceDirectory;
import org.eclipse.rap.rwt.internal.resources.ResourceManagerImpl;
import org.eclipse.rap.rwt.internal.resources.ResourceRegistry;
import org.eclipse.rap.rwt.internal.serverpush.ServerPushServiceHandler;
import org.eclipse.rap.rwt.internal.service.ApplicationStoreImpl;
import org.eclipse.rap.rwt.internal.service.LifeCycleServiceHandler;
import org.eclipse.rap.rwt.internal.service.RWTMessageHandler;
import org.eclipse.rap.rwt.internal.service.ServiceManagerImpl;
import org.eclipse.rap.rwt.internal.service.SettingStoreManager;
import org.eclipse.rap.rwt.internal.service.StartupPage;
import org.eclipse.rap.rwt.internal.textsize.ProbeStore;
import org.eclipse.rap.rwt.internal.textsize.TextSizeStorage;
import org.eclipse.rap.rwt.internal.theme.ThemeManager;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.internal.util.SerializableLock;
import org.eclipse.rap.rwt.service.ApplicationContext;
import org.eclipse.rap.rwt.service.ApplicationContextEvent;
import org.eclipse.rap.rwt.service.ApplicationContextListener;
import org.eclipse.rap.rwt.service.FileSettingStoreFactory;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.rap.rwt.service.UISessionEvent;
import org.eclipse.rap.rwt.service.UIThreadListener;
import org.eclipse.swt.internal.graphics.FontDataFactory;
import org.eclipse.swt.internal.graphics.ImageDataFactory;
import org.eclipse.swt.internal.graphics.ImageFactory;
import org.eclipse.swt.internal.graphics.InternalImageFactory;
import org.eclipse.swt.internal.graphics.ResourceFactory;
import org.eclipse.swt.internal.widgets.DisplaysHolder;


public class ApplicationContextImpl implements ApplicationContext {

  private static enum State { INACTIVE, ACTIVATING, ACTIVE, ABOUT_TO_DEACTIVATE, DEACTIVATING }

  private final static String ATTR_APPLICATION_CONTEXT
    = ApplicationContextImpl.class.getName() + "#instance";

  // TODO [fappel]: this flag is used to skip resource registration. Think about
  //                a less intrusive solution.
  // [rst] made public to allow access from testfixture in OSGi (bug 391510)
  public static boolean skipResoureRegistration;

  // TODO [fappel]: this flag is used to skip resource deletion. Think about
  //                a less intrusive solution.
  // [rst] made public to allow access from testfixture in OSGi (bug 391510)
  public static boolean skipResoureDeletion;

  private final ThemeManager themeManager;
  private final ApplicationConfiguration applicationConfiguration;
  private final ResourceDirectory resourceDirectory;
  private final ResourceManager resourceManager;
  private final PhaseListenerManager phaseListenerManager;
  private final LifeCycleFactory lifeCycleFactory;
  private final MessageChainReference messageChainReference;
  private final EntryPointManager entryPointManager;
  private final SettingStoreManager settingStoreManager;
  private final ServiceManagerImpl serviceManager;
  private final ResourceRegistry resourceRegistry;
  private final ApplicationStoreImpl applicationStore;
  private final ResourceFactory resourceFactory;
  private final ImageFactory imageFactory;
  private final InternalImageFactory internalImageFactory;
  private final ImageDataFactory imageDataFactory;
  private final FontDataFactory fontDataFactory;
  private final StartupPage startupPage;
  private final DisplaysHolder displaysHolder;
  private final TextSizeStorage textSizeStorage;
  private final ProbeStore probeStore;
  private final ServletContext servletContext;
  private final ClientSelector clientSelector;
  private final Set<ApplicationContextListener> appContextListeners;
  private final Set<UIThreadListener> uiThreadListeners;
  private final SerializableLock listenersLock;
  private final AtomicReference<State> state;
  private ExceptionHandler exceptionHandler;

  public ApplicationContextImpl( ApplicationConfiguration applicationConfiguration,
                                 ServletContext servletContext )
  {
    this.applicationConfiguration = applicationConfiguration;
    this.servletContext = servletContext;
    applicationStore = new ApplicationStoreImpl();
    resourceDirectory = new ResourceDirectory();
    resourceManager = createResourceManager();
    phaseListenerManager = new PhaseListenerManager();
    entryPointManager = new EntryPointManager();
    lifeCycleFactory = new LifeCycleFactory( this );
    RWTMessageHandler rwtHandler = new RWTMessageHandler( lifeCycleFactory );
    messageChainReference = new MessageChainReference( new MessageChainElement( rwtHandler, null ) );
    themeManager = createThemeManager();
    resourceFactory = new ResourceFactory();
    imageFactory = new ImageFactory();
    internalImageFactory = new InternalImageFactory();
    imageDataFactory = new ImageDataFactory( resourceManager );
    fontDataFactory = new FontDataFactory();
    settingStoreManager = new SettingStoreManager();
    resourceRegistry = new ResourceRegistry( getResourceManager() );
    startupPage = new StartupPage( this );
    serviceManager = createServiceManager();
    displaysHolder = new DisplaysHolder();
    textSizeStorage = new TextSizeStorage();
    probeStore = new ProbeStore( textSizeStorage );
    clientSelector = new ClientSelector();
    appContextListeners = new HashSet<>();
    listenersLock = new SerializableLock();
    state = new AtomicReference<>( State.INACTIVE );
    uiThreadListeners = new CopyOnWriteArraySet<>();
  }

  protected ThemeManager createThemeManager() {
    return new ThemeManager();
  }

  protected ResourceManager createResourceManager() {
    return new ResourceManagerImpl( resourceDirectory );
  }

  public static ApplicationContextImpl getFrom( ServletContext servletContext ) {
    return ( ApplicationContextImpl )servletContext.getAttribute( ATTR_APPLICATION_CONTEXT );
  }

  public void attachToServletContext() {
    servletContext.setAttribute( ATTR_APPLICATION_CONTEXT, this );
  }

  public void removeFromServletContext() {
    servletContext.removeAttribute( ATTR_APPLICATION_CONTEXT );
  }

  @Override
  public void setAttribute( String name, Object value ) {
    applicationStore.setAttribute( name, value );
  }

  @Override
  public Object getAttribute( String name ) {
    return applicationStore.getAttribute( name );
  }

  @Override
  public void removeAttribute( String name ) {
    applicationStore.removeAttribute( name );
  }

  @Override
  public void addUIThreadListener( UIThreadListener listener ) {
    ParamCheck.notNull( listener, "listener" );
    uiThreadListeners.add( listener );
  }

  @Override
  public void removeUIThreadListener( UIThreadListener listener ) {
    ParamCheck.notNull( listener, "listener" );
    uiThreadListeners.remove( listener );
  }

  public void notifyEnterUIThread( UISession uiSession ) {
    UISessionEvent event = new UISessionEvent( uiSession );
    for( UIThreadListener listener : uiThreadListeners ) {
      listener.enterUIThread( event );
    }
  }

  public void notifyLeaveUIThread( UISession uiSession ) {
    UISessionEvent event = new UISessionEvent( uiSession );
    for( UIThreadListener listener : uiThreadListeners ) {
      listener.leaveUIThread( event );
    }
  }

  @Override
  public boolean addApplicationContextListener( ApplicationContextListener listener ) {
    ParamCheck.notNull( listener, "listener" );
    boolean result = false;
    synchronized( listenersLock ) {
      if( state.get().equals( State.ACTIVE ) ) {
        result = true;
        appContextListeners.add( listener );
      }
    }
    return result;
  }

  @Override
  public boolean removeApplicationContextListener( ApplicationContextListener listener ) {
    ParamCheck.notNull( listener, "listener" );
    boolean result = false;
    synchronized( listenersLock ) {
      if( state.get().equals( State.ACTIVE ) ) {
        result = true;
        appContextListeners.remove( listener );
      }
    }
    return result;
  }

  public boolean isActive() {
    State currentState = state.get();
    return currentState.equals( State.ACTIVE ) || currentState.equals( State.ABOUT_TO_DEACTIVATE );
  }

  public boolean allowsRequests() {
    return state.get().equals( State.ACTIVE );
  }

  public void activate() {
    if( state.compareAndSet( State.INACTIVE, State.ACTIVATING ) ) {
      try {
        doActivate();
        state.set( State.ACTIVE );
      } catch( RuntimeException rte ) {
        state.set( State.INACTIVE );
        throw rte;
      }
    }
  }

  public void deactivate() {
    if( state.compareAndSet( State.ACTIVE, State.ABOUT_TO_DEACTIVATE ) ) {
      try {
        fireBeforeDestroy();
        state.set( State.DEACTIVATING );
        doDeactivate();
      } finally {
        state.set( State.INACTIVE );
      }
    }
  }

  public ServletContext getServletContext() {
    return servletContext;
  }

  public ResourceDirectory getResourceDirectory() {
    return resourceDirectory;
  }

  @Override
  public ResourceManager getResourceManager() {
    return resourceManager;
  }

  public EntryPointManager getEntryPointManager() {
    return entryPointManager;
  }

  public SettingStoreManager getSettingStoreManager() {
    return settingStoreManager;
  }

  public PhaseListenerManager getPhaseListenerManager() {
    return phaseListenerManager;
  }

  public ResourceRegistry getResourceRegistry() {
    return resourceRegistry;
  }

  @Override
  public ServiceManagerImpl getServiceManager() {
    return serviceManager;
  }

  public ThemeManager getThemeManager() {
    return themeManager;
  }

  public LifeCycleFactory getLifeCycleFactory() {
    return lifeCycleFactory;
  }

  public ResourceFactory getResourceFactory() {
    return resourceFactory;
  }

  public ImageFactory getImageFactory() {
    return imageFactory;
  }

  public InternalImageFactory getInternalImageFactory() {
    return internalImageFactory;
  }

  public ImageDataFactory getImageDataFactory() {
    return imageDataFactory;
  }

  public FontDataFactory getFontDataFactory() {
    return fontDataFactory;
  }

  public StartupPage getStartupPage() {
    return startupPage;
  }

  public DisplaysHolder getDisplaysHolder() {
    return displaysHolder;
  }

  public TextSizeStorage getTextSizeStorage() {
    return textSizeStorage;
  }

  public ProbeStore getProbeStore() {
    return probeStore;
  }

  public ClientSelector getClientSelector() {
    return clientSelector;
  }

  public ExceptionHandler getExceptionHandler() {
    return exceptionHandler;
  }

  public void setExceptionHandler( ExceptionHandler exceptionHandler ) {
    this.exceptionHandler = exceptionHandler;
  }

  public MessageFilterChain getHandlerChain() {
    return messageChainReference.get();
  }

  public void addMessageFilter( MessageFilter filter ) {
    ParamCheck.notNull( filter, "filter" );
    messageChainReference.add( filter );
  }

  public void removeMessageFilter( MessageFilter filter ) {
    ParamCheck.notNull( filter, "filter" );
    messageChainReference.remove( filter );
  }

  void doActivate() {
    themeManager.initialize();
    applicationConfiguration.configure( new ApplicationImpl( this, applicationConfiguration ) );
    resourceDirectory.configure( getContextDirectory() );
    addInternalServiceHandlers();
    setInternalSettingStoreFactory();
    startupPage.activate();
    lifeCycleFactory.activate();
    // Note: order is crucial here
    themeManager.activate();
    if( !skipResoureRegistration ) {
      ClientResources clientResources = new ClientResources( this );
      clientResources.registerResources();
    }
    resourceRegistry.registerResources();
    clientSelector.activate();
  }

  void doDeactivate() {
    startupPage.deactivate();
    lifeCycleFactory.deactivate();
    serviceManager.clear();
    themeManager.deactivate();
    if( !skipResoureDeletion ) {
      resourceDirectory.deleteDirectory();
    }
    entryPointManager.deregisterAll();
    phaseListenerManager.clear();
    resourceRegistry.clear();
    settingStoreManager.deregisterFactory();
    resourceDirectory.reset();
    applicationStore.reset();
  }

  private ServiceManagerImpl createServiceManager() {
    return new ServiceManagerImpl( new LifeCycleServiceHandler( messageChainReference ) );
  }

  private String getContextDirectory() {
    String location
      = ( String )servletContext.getAttribute( ApplicationConfiguration.RESOURCE_ROOT_LOCATION );
    if( location == null ) {
      location = servletContext.getRealPath( "/" );
    }
    return location;
  }

  private void addInternalServiceHandlers() {
    serviceManager.registerServiceHandler( ServerPushServiceHandler.HANDLER_ID,
                                           new ServerPushServiceHandler() );
  }

  private void setInternalSettingStoreFactory() {
    if( !settingStoreManager.hasFactory() ) {
      settingStoreManager.register( new FileSettingStoreFactory() );
    }
  }

  private void fireBeforeDestroy() {
    ApplicationContextEvent event = new ApplicationContextEvent( this );
    for( ApplicationContextListener listener : copyListeners() ) {
      try {
        listener.beforeDestroy( event );
      } catch( RuntimeException exception ) {
        handleBeforeDestroyException( listener, exception );
      }
    }
  }

  private List<ApplicationContextListener> copyListeners() {
    synchronized( listenersLock ) {
      return new ArrayList<>( appContextListeners );
    }
  }

  private void handleBeforeDestroyException( ApplicationContextListener listener,
                                             RuntimeException exception )
  {
    String txt = "Could not execute {0}.beforeDestroy(ApplicationContextEvent).";
    Object[] param = { listener.getClass().getName() };
    String msg = MessageFormat.format( txt, param );
    servletContext.log( msg, exception );
  }

}
