/*
 * File: ArrayArithm.java
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
 * Contact : Ruth Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.5  2004/03/15 19:36:53  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.4  2004/03/15 03:36:59  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.3  2004/03/10 17:59:46  rmikk
 * Fixed an error when dealing with multidimensioned arrays
 * Removed a StringChoiceList parameter and replaced it by a String parameter
 *
 * Revision 1.2  2004/03/09 17:57:17  rmikk
 * Fixed Javadoc errors
 *
 * Revision 1.1  2004/02/29 17:56:02  rmikk
 * Inital Checkin.  This operator adds,subtracts,multiplies,or divides two
 * (possible multidimensional) arrays or vectors.
 *
 */

package Operators.Generic;

import DataSetTools.operator.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Sys.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * This class does addition("Add"),Subtraction("Subtract"), 
 * multiplication("Mult"), on two arrays or Vectors that are compatible in 
 * DataTypes and sizes.
 */


public class ArrayArithm implements Wrappable, HiddenOperator {
   //~ Instance fields *******************************************************
   private String[] Choices = {"Add", "Subtract", "Mult", "Divide"};

   public Vector Argument1 = new Vector();
   public Vector Argument2 = new Vector();
   public String Operation = "Add";//new StringChoiceList(Choices);

   private OpType  op = null; //
  
   /**
    * Returns "ArrayArithm" which can be used to invoke the calc method in 
    * ISAW Scripts
    */
   public String getCommand() {
      return "ArrayArithm";
   }

   /**
    * Returns a string that  can be used for on line documentation
    */
   public String getDocumentation() {
      StringBuffer s = new StringBuffer();

      s.append("@overview This class does addition(\"Add\"),Subtraction");
      s.append("(\"Subtract\"), multiplication(\"Mult\"), on two arrays or ");
      s.append("Vectors that are compatible in DataTypes and sizes.");
      s.append("@algorithm Recursively traverses each array to a primitive ");
      s.append("data type. The desired operation is then done. This result ");
      s.append("is then reassembled into the common data type and returned ");
      s.append("@param Array1  The first array(Argument1 in division)");
      s.append("@param Array2  The second array");
      s.append("@param Operation  Either \"Add\",\"Subtract\",\"Mult\",or ");
      s.append(" \"Divide\"");
      s.append("@return An ErrorString or the result of the operation");
      s.append("@error  Undefined Argument1(Argument2) in ArrayArithm");
      s.append("@error  No data in Argument1(Argument2) in ArrayArithm");
      s.append("@error  No(Improper) operation is specified in ArrayArithm");
      s.append("@error  Incompatible Data Types in ArrayArithm");
      s.append("@error  Incompatible sizes in ArrayArithm");
 
      return s.toString();

   }
  
   /**
    * This method does addition("Add"),Subtraction("Subtract"), multiplication
    * ("Mult"), on two arrays or Vectors that are compatible in DataTypes and 
    * sizes. It recursively traverses each array to a primitive data type. The 
    * desired operation is then done. This result is then reassembled into the
    * common data type
    * @param Argument1  The first array(Argument1 in division)
    * @param Argument2  The second array
    * @param op  Either Add,Subtract,Mult,or Divide
    * @return the DataSet or an ErrorString
    */

   public Object calculate(Vector Argument1, Vector Argument2, 
                                                          String op) {

      this.Argument1 = Argument1;
      this.Argument2 = Argument2;
      this.Operation = op;
      return calculate();

   }

   /**
    * This class does addition("Add"),Subtraction("Subtract"), multiplication
    * ("Mult"), on two arrays or Vectors that are compatible in DataTypes and
    *  sizes. It recursively traverses each array to a primitive data type. 
    * The desired operation is then done. This result is then reassembled into
    *  the common data type and returned
    */
   public Object calculate() {
   
      Object result = null;
  
      if (Argument1 == null) 
         return new ErrorString("Undefined Argument1 in ArrayArithm");

      if (Argument2 == null) 
         return new ErrorString("Undefined Argument2 in ArrayArithm");

      if (Argument1.size() < 1) 
         return new ErrorString("No data in Argument1 in ArrayArithm");

      if (Argument2.size() < 1) 
         return new ErrorString("No data in Argument2 in ArrayArithm");

      if (Argument1.size() != Argument2.size())
         return new ErrorString("Argument1 and Argument2 have different"+
                                                      " sizes in ArrayArithm");

      if (Operation == null)
         return new ErrorString(" No operation is specified in ArrayArithm");

      String S = Operation.toString();

      if (S.equals("Add"))
         op = new AddOperation();

      else if (S.equals("Subtract"))
         op = new SubOperation();

      else if (S.equals("Mult"))
         op = new MultOperation();

      else if (S.equals("Divide"))
         op = new DivideOperation();

      else
         return new ErrorString("Improper operation, " + S + 
                                                       ", in ArrayArithm");

      Vector Num = Argument1;
      Vector Den = Argument2;

      if (Num.size() > 1) {

         for (int i = 0; i < Num.size(); i++) {

            Argument1 = setVect(Num.elementAt(i));
            Argument2 = setVect(Den.elementAt(i));
            Object O = calculate();

            if (O instanceof ErrorString) 
               return O;
            else
               Num.setElementAt(((Vector) O).firstElement(), i);
          
         }
         return Num;
      }
      else if (Num.elementAt(0) instanceof Vector) {

         if (!(Den.elementAt(0) instanceof Vector))
            return new ErrorString("Incompatible Data Types in ArrayArithm");

         if (((Vector) (Den.elementAt(0))).size() !=
            ((Vector) (Num.elementAt(0))).size())
            return new ErrorString("Incompatible sizes in ArrayArithm");

         for (int i = 0; i < ((Vector) (Num.firstElement())).size(); i++) {

            Argument1 = (Vector) (Num.elementAt(i));
            Argument2 = (Vector) (Den.elementAt(i));
            Object O = calculate();

            if (O instanceof ErrorString)
               return O;
            Num.setElementAt(O, i);

         }
         return Num;

      }
      else if (Argument1.elementAt(0).getClass().isArray()) {

         Object Xnum = Argument1.elementAt(0);

         if (!Argument2.elementAt(0).getClass().isArray())
            return new ErrorString("Elements incompatible in ArrayArithm");

         Object Xden = Argument2.elementAt(0);

         if (Array.getLength(Xnum) != Array.getLength(Xden))
            return new ErrorString("Elements incompatible lengths in"+
                                                              " ArrayArithm");

         if (Array.getLength(Xnum) < 1)
            return setVect(Xnum);

         if (Xnum instanceof int[]) {

            int[] r = new int[Array.getLength(Xnum)];

            for (int i = 0; i < Array.getLength(Xnum); i++)
               r[i] = op.calc(((int[]) Xnum)[i], ((int[]) Xden)[i]);

            return setVect(r);
         }
         else if (Xnum instanceof float[]) {

            float[] r = new float[Array.getLength(Xnum)];

            for (int i = 0; i < Array.getLength(Xnum); i++)
               r[i] = op.calc(((float[]) Xnum)[i], ((float[]) Xden)[i]);

            return setVect(r);
         }
         else if (Xnum instanceof double[]) {

            double[] r = new double[Array.getLength(Xnum)];

            for (int i = 0; i < Array.getLength(Xnum); i++)
               r[i] = op.calc(((double[]) Xnum)[i], ((double[]) Xden)[i]);

            return setVect(r);
         }
         else if (Xnum instanceof long[]) {

            long[] r = new long[Array.getLength(Xnum)];

            for (int i = 0; i < Array.getLength(Xnum); i++)
               r[i] = op.calc(((long[]) Xnum)[i], ((long[]) Xden)[i]);

            return setVect(r);
    
         }
         else if (Xnum instanceof short[]) {

            short[] r = new short[Array.getLength(Xnum)];

            for (int i = 0; i < Array.getLength(Xnum); i++)
               r[i] = op.calc(((short[]) Xnum)[i], ((short[]) Xden)[i]);

            return setVect(r);
         }
         else if (Xnum instanceof byte[]) {

            byte[] r = new byte[Array.getLength(Xnum)];

            for (int i = 0; i < Array.getLength(Xnum); i++)
               r[i] = op.calc(((byte[]) Xnum)[i], ((byte[]) Xden)[i]);

            return setVect(r);
         }
         else if (Array.get(Xnum, 0).getClass().isPrimitive()) {

            return new ErrorString("Cannot divide this data type in"+
                                                        " ArrayArithm");

         }
         else if (Array.get(Xnum, 0).getClass().isArray()) { 
            Object Res=Array.newInstance( Xnum.getClass().getComponentType(), 
                        Array.getLength(Xnum));
            for (int i = 0; i < Array.getLength(Xnum); i++) {
       
               if (!Array.get(Xden, i).getClass().isArray())
                  return new ErrorString("Elements incompatible in"+
                                                          " ArrayArithm");

               Argument1 = setVect(Array.get(Xnum, i));
               Argument2 = setVect(Array.get(Xden, i));
               Object O = calculate();

               if (O instanceof ErrorString)
                  return O;

               Array.set(Res,i,((Vector)O).firstElement());
            }

            return setVect(Res);
         }
      }
      else if (Argument1.firstElement() instanceof Integer) { //Double

         if (!(Argument2.firstElement() instanceof Integer))
            return new ErrorString("incompatible DataTypes in ArrayArithm");

         Integer Xnum = (Integer) Argument1.firstElement();
         Integer Xden = (Integer) Argument2.firstElement();

         return setVect(new Integer(op.calc(Xnum.intValue(), 
                                                          Xden.intValue())));
      }
      else if (Argument1.firstElement() instanceof Float) { //Double

         if (!(Argument2.firstElement() instanceof Float))
            return new ErrorString("incompatible DataTypes in ArrayArithm");

         Float Xnum = (Float) Argument1.firstElement();
         Float Xden = (Float) Argument2.firstElement();

         return setVect(new Float(op.calc(Xnum.floatValue(), 
                                                       Xden.floatValue())));
      
      }
      else if (Argument1.firstElement() instanceof Double) { //Double

         if (!(Argument2.firstElement() instanceof Double))
            return new ErrorString("incompatible DataTypes in ArrayArithm");

         Double Xnum = (Double) Argument1.firstElement();
         Double Xden = (Double) Argument2.firstElement();

         return setVect(new Double(op.calc(Xnum.doubleValue(), 
                                                        Xden.doubleValue())));
      
      }
      else if (Argument1.firstElement() instanceof Long) { //Double

         if (!(Argument2.firstElement() instanceof Long))
            return new ErrorString("incompatible DataTypes in ArrayArithm");

         Long Xnum = (Long) Argument1.firstElement();
         Long Xden = (Long) Argument2.firstElement();

         return setVect(new Long(op.calc(Xnum.longValue(), Xden.longValue())));
      
      }
      else if (Argument1.firstElement() instanceof Short) { //Double

         if (!(Argument2.firstElement() instanceof Short))
            return new ErrorString("incompatible DataTypes in ArrayArithm");

         Short Xnum = (Short) Argument1.firstElement();
         Short Xden = (Short) Argument2.firstElement();

         return setVect(new Short(op.calc(Xnum.shortValue(), 
                                                        Xden.shortValue())));
      }
      else // Not an array

         return new ErrorString("Cannot divide this data type in ArrayArithm");

         //do not remove this line
      return result;
   }


   private Vector setVect(Object val) {
      Vector Res = new Vector();

      Res.addElement(val);
      return Res;
   }

  /**
    *  Test program for this module
    */
   public static void main(String args[]) {
      float[][] a = {{1,2,3,4},{3, 5, 7, 9}};
      float[][] b = {{6,8,9,1},{1, 2, 3, 4}};
      ArrayArithm D = new ArrayArithm();
      String[] S = new String[1];

      S[0] = args[0];
      D.Operation = args[0].trim();
   
      D.Argument1 = new Vector();
      D.Argument2 = new Vector();

      D.Argument1.addElement(a);
      D.Argument2.addElement(b);

      D.Argument1.addElement(b);
      D.Argument2.addElement(a);
      Object O = D.calculate();

      System.out.println("result=" + StringUtil.toString(O));

   }

   
   interface OpType {

      public int calc(int x, int y);
    
      public float calc(float x, float y);

      public double calc(double x, double y);

      public long calc(long x, long y);

      public short calc(short x, short y);

      public byte calc(byte x, byte y);

   }


   class AddOperation implements OpType {

      public int calc(int x, int y) {
         return x + y;
      }

      public float calc(float x, float y) {
         return x + y;
      
      }

      public double calc(double x, double y) {

         return x + y;
      }

      public long calc(long x, long y) {

         return x + y;
      }

      public short calc(short x, short y) {

         return (short) (x + y);
      }

      public byte calc(byte x, byte y) {

         return (byte) (x + y);
      }

   }


   class SubOperation implements OpType {

      public int calc(int x, int y) {

         return x - y;
      }
     
      public float calc(float x, float y) {

         return x - y;
      }

      public double calc(double x, double y) {

         return x - y;
      }

      public long calc(long x, long y) {

         return x - y;
      }

      public short calc(short x, short y) {

         return (short) (x - y);
      }

      public byte calc(byte x, byte y) {

         return (byte) (x - y);
      }

   }


   class MultOperation implements OpType {

      public int calc(int x, int y) {

         return (x * y);
      }

      public float calc(float x, float y) {

         return (x * y);
      }

      public double calc(double x, double y) {

         return (x * y);
      }

      public long calc(long x, long y) {

         return (long) (x * y);
      }

      public short calc(short x, short y) {

         return (short) (x * y);
      }

      public byte calc(byte x, byte y) {

         return (byte) (x * y);
      }

   }


   class DivideOperation implements OpType {

      public int calc(int x, int y) {

         if (y == 0)
            return 0;
         else 
            return x / y;
      }

      public float calc(float x, float y) {

         if (y == 0)
            return 0.0f;
         else 
            return x / y;
      }

      public double calc(double x, double y) {

         if (y == 0)
            return 0.0;
         else 
            return x / y;
      }

      public long calc(long x, long y) {

         if (y == 0)
            return (long) 0;
         else 
            return x / y;
      }

      public short calc(short x, short y) {

         if (y == 0)
            return (short) 0;
         else 
            return (short) (x / y);
      }

      public byte calc(byte x, byte y) {

         if (y == 0)
            return (byte) 0;
         else 
            return (byte) (x / y);
      }

   }
}
