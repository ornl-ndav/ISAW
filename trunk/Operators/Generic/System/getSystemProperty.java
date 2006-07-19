package Operators.Generic.System;

public class getSystemProperty{
  public getSystemProperty(){}

  public static String getSysProp(String propName) {
    return System.getProperty(propName);
  }
}
