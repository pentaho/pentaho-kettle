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
package org.pentaho.di.job.entry.validator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ValidatorContext {

  private Map<String, Object> map = new HashMap<String, Object>();

  public Map<String, Object> getMap() {
    return map;
  }

  public ValidatorContext put(String key, Object value) {
    map.put(key, value);
    return this;
  }

  public ValidatorContext putAsList(String key, Object... value) {
    map.put(key, value);
    return this;
  }

  public void clear() {
    map.clear();
  }

  public boolean containsKey(String key) {
    return map.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  public Set<Map.Entry<String, Object>> entrySet() {
    return map.entrySet();
  }

  public Object get(String key) {
    return map.get(key);
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public Set<String> keySet() {
    return map.keySet();
  }

  public ValidatorContext putAll(Map<String, Object> t) {
    map.putAll(t);
    return this;
  }

  public Object remove(String key) {
    return map.remove(key);
  }

  public int size() {
    return map.size();
  }

  public Collection<Object> values() {
    return map.values();
  }

}
