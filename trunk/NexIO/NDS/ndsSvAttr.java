/*
 * File: ndsSvAttr.java
 *
 * Copyright (C) 2001, Ruth Mikkelson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.3  2002/11/27 23:28:56  pfpeterson
 * standardized header
 *
 */
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
