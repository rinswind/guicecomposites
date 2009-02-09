package org.unseen.guice.composite.scopes;

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
      return (127 * "name".hashCode()) ^ name.hashCode() + 
        (127 * "value".hashCode()) ^ value.hashCode();
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
