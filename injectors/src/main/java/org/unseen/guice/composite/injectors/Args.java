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
package org.unseen.guice.composite.injectors;

import java.lang.annotation.Annotation;

/**
 * The analog of the Names utility class of Guice but for factory parameter
 * annotations.
 * 
 * @author rinsvind@gmail.com (Todor Boev)
 */
public class Args {
  private Args() {
  }
  
  private static class ArgImpl implements Arg {
    private final String name;
    
    public ArgImpl(String name) {
      this.name = name;
    }
    
    public String value() {
      return "";
    }

    public Class<? extends Annotation> annotationType() {
      return Arg.class;
    }
    
    public String toString() {
      return "@" + Arg.class.getName() + "(value=" + name + ")";
    }
    
    public boolean equals(Object o) {
      return o instanceof Arg && name.equals(((Arg) o).value());
    }
    
    public int hashCode() {
      return (127 * "value".hashCode()) ^ name.hashCode();
    }
  }
  
  private static Arg DEFAULT = new ArgImpl("");
  
  public static Arg arg(String name) {
    return (name != null && name.length() > 0) ? new ArgImpl(name) : DEFAULT;
  }
}
