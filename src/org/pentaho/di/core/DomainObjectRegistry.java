package org.pentaho.di.core;

import java.lang.reflect.Constructor;

import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectCreationException;

public class DomainObjectRegistry {

    public static final Class<?> DEFAULT_TRANS_META_CLASS = TransMeta.class;
    public static final Class<?> DEFAULT_JOB_META_CLASS = JobMeta.class;
    public static final Class<?> DEFAULT_REP_OBJ_CLASS = RepositoryObject.class;
    
    private static DomainObjectRegistry instance;
    
    private Class<?> transMetaClass;
    private Class<?> jobMetaClass;
    private Class<?> repObjClass;


    private DomainObjectRegistry() {

    }

    public static DomainObjectRegistry getInstance() {
      if (instance == null) {
        instance = new DomainObjectRegistry();
      }
      return instance;
    }

    public void registerTransMetaClass(Class<?> transMetaClass) {
      this.transMetaClass = transMetaClass;
    }

    public Class<?> getRegisteredTransMetaClass() {
      return this.transMetaClass;
    }

    public void registerJobMetaClass(Class<?> jobMetaClass) {
      this.jobMetaClass = jobMetaClass;
    }

    public Class<?> getRegisteredJobMetaClass() {
      return this.jobMetaClass;
    }

    public void registerRepositoryObjectClass(Class<?> repObjClass) {
      this.repObjClass = repObjClass;
    }

    public Class<?> getRegisteredRepositoryObjectClass() {
      return this.repObjClass;
    }

    public TransMeta constructTransMeta(Class<?>[] parameterTypes, Object[] initArgs) throws DomainObjectCreationException {
      try {
        if(transMetaClass == null) {
          transMetaClass = DEFAULT_TRANS_META_CLASS;
        }
        Constructor<?> constructor = transMetaClass.getConstructor(parameterTypes);
        if (constructor != null) {
          return (TransMeta) constructor.newInstance(initArgs);
        } else {
          throw new DomainObjectCreationException("Unable to get the constructor for " + transMetaClass);
        }
      } catch (Exception e) {
        throw new DomainObjectCreationException("Unable to instantiate object for " + transMetaClass);
      }
    }
    
    public JobMeta constructJobMeta(Class<?>[] parameterTypes, Object[] initArgs) throws DomainObjectCreationException {
      try {
        if(jobMetaClass == null) {
          Constructor<?> constructor = DEFAULT_JOB_META_CLASS.getConstructor(parameterTypes);
          if (constructor != null) {
            return (JobMeta) constructor.newInstance(initArgs);
          } else {
            throw new UIObjectCreationException("Unable to get the constructor for " + jobMetaClass);
          }
        
        } else {
          Constructor<?> constructor = jobMetaClass.getConstructor(parameterTypes);
          if (constructor != null) {
            return (JobMeta) constructor.newInstance(initArgs);
          } else {
            throw new DomainObjectCreationException("Unable to get the constructor for " + jobMetaClass);
          }
        }
      } catch (Exception e) {
        throw new DomainObjectCreationException("Unable to instantiate object for " + jobMetaClass);
      }
    }
    
    public RepositoryObject constructRepositoryObject(Class<?>[] parameterTypes, Object[] initArgs) throws DomainObjectCreationException {
      try {
        if(repObjClass == null) {
          Constructor<?> constructor = DEFAULT_REP_OBJ_CLASS.getConstructor(parameterTypes);
          if (constructor != null) {
            return (RepositoryObject) constructor.newInstance(initArgs);
          } else {
            throw new DomainObjectCreationException("Unable to get the constructor for " + repObjClass);
          }
        
        } else {
          Constructor<?> constructor = repObjClass.getConstructor(parameterTypes);
          if (constructor != null) {
            return (RepositoryObject) constructor.newInstance(initArgs);
          } else {
            throw new DomainObjectCreationException("Unable to get the constructor for " + repObjClass);
          }
        }
      } catch (Exception e) {
        throw new DomainObjectCreationException("Unable to instantiate object for " + repObjClass);
      }
    }
 }
