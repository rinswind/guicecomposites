/**
 * Copyright (C) 2009 Todor Boev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unseen.guice.composite.scopes;

import java.lang.annotation.Annotation;

/**
 * The analog of the Names utility class of Guice but for factory parameter
 * annotations.
 * 
 * @author rinsvind@gmail.com (Todor Boev)
 *
 */
public class Args {
  private Args() {
  }
  
  private static class ArgImpl implements Arg {
    private final Class<? extends Annotation> value;
    private final String name;
    
    public ArgImpl(Class<? extends Annotation> value, String name) {
      this.value = value;
      this.name = name;
    }
    
    public Class<? extends Annotation> value() {
      return value;
    }

    public String name() {
      return name;
    }
    
    public Class<? extends Annotation> annotationType() {
      return Arg.class;
    }
    
    public String toString() {
      return "@" + Arg.class.getName() + "(name=" + name + ", value=" + value + ")";
    }
    
    public boolean equals(Object o) {
      if (!(o instanceof Arg)) {
        return false;
      }
      
      Arg other = (Arg) o;
      return value.equals(other.value()) && name.equals(other.name());
    }
    
    public int hashCode() {
      return ((127 * "name".hashCode()) ^ name.hashCode()) + 
        ((127 * "value".hashCode()) ^ value.hashCode());
    }
  }
  
  public static Arg arg(Class<? extends Annotation> value) {
    return arg("", value);
  }
  
  public static Arg arg(String name, Class<? extends Annotation> value) {
    if (value == null) {
      value = AnonymousScope.class;
    }
      
    if (name == null) {
      name = "";
    }
    
    return new ArgImpl(value, name);
  }
}
