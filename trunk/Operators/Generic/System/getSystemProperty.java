package Operators.Generic.System;
import java.lang.*;

public class getSystemProperty{
  public getSystemProperty(){}

  public static String getSysProp(String propName) {
    return System.getProperty(propName);
  }
}
