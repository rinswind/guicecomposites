package org.unseen.guice.composite;

import java.lang.annotation.Annotation;

/**
 * The analog of the Names utility class of Guice but for factory parameter
 * annotations.
 * 
 * @author Todor Boev
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
