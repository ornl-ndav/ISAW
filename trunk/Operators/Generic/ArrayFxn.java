/*
 * File:  ArrayFxn.java
 *
 * Copyright (C) 2004 Ruth Mikkelson
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.4  2004/03/15 19:36:53  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.3  2004/03/15 03:36:59  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.2  2004/03/11 16:43:59  rmikk
 * Fixed Javadoc error
 *
 * Revision 1.1  2004/03/09 17:04:29  rmikk
 * Initial Checkin
 * This operator applies an algebraic expression(in x0) to all elements
 * of an array or vector.
 *
 */
package Operators.Generic;

import DataSetTools.operator.*;
import gov.anl.ipns.MathTools.Functions.FunctionTools.*;
import gov.anl.ipns.Util.SpecialStrings.*;

import java.util.*;
import java.lang.reflect.*;
import Command.*;

/**
 * This class applies an expression of one variable to all elements of a vector
 *  or an array.  If any elements of the vector or array are arrays or vectors
 *  this class will apply the expression on all subcomponents( or subcomponents
 *  of subcomponents, etc.) of these structures.
 */

public class ArrayFxn implements Wrappable {
  //~ Instance fields **********************************************************

  public Vector ArrayHolder=new Vector();
  public String formula="2*x0^2+3";

  //~ Methods ******************************************************************

  /**
    *  Returns "ArrayFxn" the name used by the scripting system to refer to
    *  the operator derived from this class
    */
  public String getCommand(  ) {
    return "ArrayFxn";
  }

  /**
    *  Returns a documentation string that can be used by ISAW's help system
    */
  public String getDocumentation(  ) {

     StringBuffer s = new StringBuffer(  );
       s.append( "@overview  This class applies an expression of one ");
       s.append( "variable to all elements of a vector or an array.  If");
       s.append( " any elements of the vector or array are arrays or vectors");
       s.append( " this class will apply the expression on all subcomponents");
       s.append( "( or subcomponents of subcomponents, etc.) of these");
       s.append( "structures.");
       s.append( "@param ArrayHolder Is or contains the possibly multidimensional");
       s.append( "    array or vector" );
       s.append( "@param formula An expression in one variable x0 that is ");
       s.append( "  applied to the elements of the array. The expression can");
       s.append( " contain the operations +,-,*,/,^,sin,asin,cos,acos,tan,");
       s.append( "atan,exp,and log(base e),max,min,int, and round" );
     
       s.append( "@return A new object that corresponds to the ArrayHolder " );
       s.append( "after the rule was applied" );
       s.append( "@error Improper formula" );
       s.append(" @error Entry null or improper data type(NaN)");
       s.append( "occur. For example: Error occurs if number of bins is zero." );
       return s.toString(  );

  }

  /**
    *  The entry point to this class that can be used by Java and Jython.
    *   This method applies the Rule to every element(subelement, subelement of 
    *   subelement, etc) of ArrayHolder.
    *   @param ArrayHolder  Contains a possibly multi dimensioned array or Vector.
    *   @param Rule  The rule that is to be applied to elements of ArrayHolder. 
    *     This rule must contain only one variable, x0. It can use the operations 
    *     +,-,*,/,^,sin,asin,cos,acos,tan,atan,exp,and log(base e),max,min,int,
    *     and round
    *   @return  A new Object of the same data type as ArrayHolder. Its values all 
    *     have the Rule applied to them
    *   @see DataSetTools.functions.FunctionTools
    */
  public Object calculate( Vector ArrayHolder, String Rule){

     this.ArrayHolder= ArrayHolder;
     this.formula = Rule;
     return calculate();

  }

  Fxn F = null;
  double[] x = new double[1];

  /**
    *  This method is called by the getResult method in the operator form
    *  of this class.
    */ 
  public Object calculate(  ) {
      String2Instance1 S= new String2Instance1(formula, "rule");
      if( S == null)
         return new ErrorString("Improper formula");
      
      F=S.parse();
      if( F == null)
         return new ErrorString("Improper formula:"+S.geterrormessage()
                    +" at pos "+S.geterrorposition()+" in "+formula);
      return Calc( ArrayHolder, F);        
  }

 private Object Calc( Object Dat, Fxn F){
    if( Dat == null)
      return new ErrorString("Data contains a null entry");
    if( Dat instanceof Vector){
       Vector V = new Vector();
       for(int i=0;i<((Vector)Dat).size(); i++){
         Object O = Calc( ((Vector)Dat).elementAt(i),F);
         if( O instanceof ErrorString)
           return O;
         V.addElement(O);
       }
       return V;
    }else if( Dat instanceof Float){
       x[0]= (double)(((Float)Dat).floatValue());
       double xx= F.vall(x,1);
       return new Float( (float)xx);
    }else if( Dat instanceof Integer){
       x[0]= (double)(((Integer)Dat).intValue());
       double xx= F.vall(x,1);
       return new Integer( (int)xx);

    }else if( Dat instanceof Double){
       x[0]= (((Double)Dat).doubleValue());
       double xx= F.vall(x,1);
       return new Double( xx);

    }else if( Dat instanceof Long){
       x[0]= (double)(((Long)Dat).longValue());
       double xx= F.vall(x,1);
       return new Long( (long)xx);

    }else if( Dat instanceof Short){
       x[0]= ((Short)Dat).doubleValue();
       double xx= F.vall(x,1);
       return new Short( (short)xx);

    }else if( Dat instanceof Byte){
       x[0]= (((Byte)Dat).doubleValue());
       double xx= F.vall(x,1);
       return new Byte( (byte)xx);

    }else if( Dat instanceof float[]){
      float[] f = new float[ ((float[])Dat).length];
      for( int i = 0;i < f.length; i++){
          x[0]= (double)((float[])Dat)[i];
          double xx= F.vall(x,1);
          f[i]=(float)xx;

      }
     return f;
    }else if( Dat instanceof int[]){
      int[] f = new int[ ((int[])Dat).length];
      for( int i = 0;i < f.length; i++){
          x[0]= (double)((int[])Dat)[i];
          double xx= F.vall(x,1);
          f[i]=(int)xx;

      }
      
      return f;
    }else if( Dat instanceof double[]){
      double[] f = new double[ ((double[])Dat).length];
      for( int i = 0;i < f.length; i++){
          x[0]= (double)((double[])Dat)[i];
          double xx= F.vall(x,1);
          f[i]=(double)xx;

      }
      
     return f;
    }else if( Dat instanceof short[]){
      short[] f = new short[ ((short[])Dat).length];
      for( int i = 0;i < f.length; i++){
          x[0]= (double)((short[])Dat)[i];
          double xx= F.vall(x,1);
          f[i]=(short)xx;

      }
      
     return f;
    }else if( Dat instanceof long[]){
      long[] f = new long[ ((long[])Dat).length];
      for( int i = 0;i < f.length; i++){
          x[0]= (double)((long[])Dat)[i];
          double xx= F.vall(x,1);
          f[i]=(long)xx;

      }
      
      return f;
    }else if( Dat instanceof byte[]){
      byte[] f = new byte[ ((byte[])Dat).length];
      for( int i = 0;i < f.length; i++){
          x[0]= (double)((byte[])Dat)[i];
          double xx= F.vall(x,1);
          f[i]=(byte)xx;

      }
      
     return f;
    }else if( Dat.getClass().isArray()){
       
       Object Res = Array.newInstance( Dat.getClass().getComponentType(), 
                               Array.getLength(Dat));
       for( int i=0;i < Array.getLength(Dat);i++){
         Object O = Array.get(Dat,i);
         if( O == null)
           return new ErrorString("An Entry is null");
         if( O.getClass().isPrimitive())
            return new ErrorString("Primitive data type "+O.getClass()+
                  " is not supported");
         O = Calc(O, F);
         Array.set(Res,i,O);
        
          
       }
      return Res;

    }else 
       return new ErrorString( "Data Type "+Dat.getClass()+" is not supported");
   
 } 

  /**
    *  Test program for this module
    *  @param  args  Enter the rule as the first argument
    *  @return  returns the value when the rule is applied to
    *  {  {1.0f,2.0f,3.0f},{2.3f,3.5f,7.2f},{1f,3f,5f,7f}};
    */
  public static void main( String args[]){

     float[][] ff={  {1.0f,2.0f,3.0f},
                      {2.3f,3.5f,7.2f},
                     {1f,3f,5f,7f}
                  };
     Vector V = new Vector();
     V.addElement(ff);
     ArrayFxn dd = new ArrayFxn();
   
     ScriptUtil.display( dd.calculate( V, args[0].trim()));

  }   
}
