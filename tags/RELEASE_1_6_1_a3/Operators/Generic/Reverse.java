
/*
 * File:  Reverse.java 
 *             
 * Copyright (C) 2004, Ruth Mikkelson
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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.1  2004/01/09 20:05:31  rmikk
 * Initial Checkin
 *
 */
package Operators.Generic;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import java.util.*;
import java.lang.reflect.*;
import DataSetTools.util.*;

/**
 *   This operator reverses the order of an array or vector
 */
public class Reverse extends GenericOperator implements HiddenOperator{

   /**
     * Constructor
     */
   public Reverse(){
     super("Reverse");
     setDefaultParameters();
   }

   /**
     *  Constructor
     *  @param  A1  the array or vector whose elements are to be reversed
     *  If the object is neither an array or vector, no action is taken
     */
   public Reverse( Object A1){
      this();
      setParameter( new Parameter("", A1),0);

   }


   public void setDefaultParameters(){
     parameters = new Vector();
     
      addParameter( new Parameter("", null));

   }

   /**
     *  Reverses the elements of the array or vector.
     *  @return  the reversed array.  The original array or vector is ALSO REVERSED
     */
   public Object getResult(){

      Object O = getParameter(0).getValue();
      if( O == null)
         return null;
      int n= -1;
   
      boolean isVector = true;
      if( O.getClass().isArray()){
          n = Array.getLength(O);
          isVector = false;
      }else if( O instanceof Vector){
          n =((Vector) O).size();
      }else
          return O;

      Object sav;
      for( int i=0; i< n/2; i++){
         if( isVector){
            sav = ((Vector)O).elementAt(i);
            ((Vector)O).setElementAt(((Vector)O).elementAt(n-i-1),i);
            ((Vector)O).setElementAt(sav,n-i-1);
      

         }
         else{
            sav = Array.get(O,i);
            Array.set(O,i,Array.get(O,n-i-1));
            Array.set(O,n-i-1,sav);

         }
      }
     return O;    
   }

  public String getDocumentation(){
    
    StringBuffer s = new StringBuffer(  );
       s.append( "@overview This operator reverses the order of an array ");
       s.append( "or vector");
       s.append( "@param  A1  the array or vector whose elements are to be ");
       s.append( "reversed");
       s.append( "If the object is neither an array or vector, no action is");
       s.append( " taken");
       s.append( " @return  the reversed array.  The original array or vector");
       s.append(" is ALSO REVERSED " );
       return s.toString(  );
  }


  public static void main( String args[]){
    Reverse R = new Reverse(args);

    System.out.println("RevArray="+ StringUtil.toString( R.getResult()));

    Vector V = new Vector();
    for( int i = 0; i<args.length;i++)
      V.addElement( args[i]);
    R = new Reverse( V);
    System.out.println("RevVector="+ StringUtil.toString( R.getResult()));


  }

}//reverse
