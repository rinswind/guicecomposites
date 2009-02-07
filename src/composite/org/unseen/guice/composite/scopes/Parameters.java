package org.unseen.guice.composite.scopes;

import java.lang.annotation.Annotation;

/**
 * The analog of the Names utility class of Guice but for factory parameter
 * annotations.
 * 
 * @author Todor Boev
 */
public class Parameters {
  private Parameters() {
  }
  
  private static class ParameterImpl implements Parameter {
    private final String name;
    
    public ParameterImpl(String name) {
      this.name = name;
    }
    
    public String value() {
      return "";
    }

    public Class<? extends Annotation> annotationType() {
      return Parameter.class;
    }
    
    public String toString() {
      return "@" + Parameter.class.getName() + "(value=" + name + ")";
    }
    
    public boolean equals(Object o) {
      return o instanceof Parameter && name.equals(((Parameter) o).value());
    }
    
    public int hashCode() {
      return (127 * "value".hashCode()) ^ name.hashCode();
    }
  }
  
  private static Parameter DEFAULT = new ParameterImpl("");
  
  public static Parameter parameter(String name) {
    return (name != null && name.length() > 0) ? new ParameterImpl(name) : DEFAULT;
  }
}
