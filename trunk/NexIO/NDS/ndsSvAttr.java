package NexIO.NDS;

import NexIO.*;
import java.lang.*;

// for attributes
public class ndsSvAttr implements Attr{
  String name;
  Object Value;

  public ndsSvAttr(String name, Object Value){
    this.name=name;
    this.Value=Value;
  }

  public ndsSvAttr(String nameampval){ //name@value format
    int k=nameampval.indexOf('@');
    if(k<0){
      name="";Value="";
    }else{
      name=nameampval.substring(0,k);
      Value = nameampval.substring(k+1);  
    }
  }

   public String getItemName(){
     return name;
   }

   public Object getItemValue(){
     return Value;
   }

   public String DisplayValue(){
     return Display(Value);
   }

  public static int npts(Object Value){
    if(Value == null)
      return -1;
    else if((Value instanceof byte[])){
      byte b[];
      b=(byte[])Value;
      return b.length;
    }else if( (Value instanceof int[])){
      int b[]; 
      b=(int[])Value;
      return b.length;
    }else if((Value instanceof float[])){
      float b[];
      b=(float[])Value;
      return b.length;
    }else if((Value instanceof double[])){
      double b[];
      b=(double[])Value;
      return b.length;
    }else 
      return -1;

  }

  public static String Vall(Object Value, int i){
    if(Value == null)
      return "null";
    else if((Value instanceof byte[])){
      byte b[];
      b=(byte[])Value; 
      if(i>=b.length)
        return "";
      else
        return new Byte(b[i]).toString();
    }else if( (Value instanceof int[])){
      int b[];
      b=(int[])Value; 
      if( i>=b.length)
        return "";
      else
        return new Integer(b[i]).toString();
    }else if((Value instanceof float[])){
      float b[];
      b=(float[])Value; 
      if( i>=b.length)
        return "";
      else
        return new Float(b[i]).toString();
    }else if((Value instanceof double[])){
      double b[];
      b=(double[])Value; 
      if(i>=b.length)
        return "";
      else
        return new Double(b[i]).toString();
    }else
      return "";
  }

  public static String Display(Object Value){
    if(Value instanceof String) 
      return Value.toString();
    if(!(Value instanceof byte[])){
      // do nothing
    }else if( !(Value instanceof int[])){
      // do nothing
    }else if(!(Value instanceof float[])){
      // do nothing
    }else if(!(Value instanceof double[])){
      // do nothing
    }else{
      return Value.toString();
    }

    String S ="[";
    for(int i=0; i<npts(Value);i++){
      S = S + Vall(Value,i);
      if(i<npts(Value)-1)S = S+",";
    }
    S = S+"]";
    return S;
  } 
}
