/*
 * File:  ViewQxQyQz.java 
 *
 * Copyright (C) 2002, Ruth Mikkelson
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
 * Modified:
 *
 * $Log$
 * Revision 1.8  2003/10/15 02:15:42  bouzekc
 * Fixed javadoc errors.
 *
 * Revision 1.7  2003/05/07 16:15:24  rmikk
 * Eliminated a run-time class cast exception
 *
 * Revision 1.6  2003/05/02 22:31:54  pfpeterson
 * Migrated to IParameterGUI.
 *
 * Revision 1.5  2003/02/24 18:54:52  dennis
 * Added getDocumentation() method. (Joshua Olson)
 *
 * Revision 1.4  2002/11/27 23:31:01  pfpeterson
 * standardized header
 *
 * Revision 1.3  2002/09/19 15:58:50  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.2  2002/08/30 19:45:49  rmikk
 * Eliminated reference to a operator used only for testing
 *
 * Revision 1.1  2002/08/30 15:36:13  rmikk
 * Initial Checkin
 *
 * Revision 1.1  2002/08/02 22:51:07  dennis
 * Very crude, marginally useful form of reciprocal lattice viewer in
 * the form of an operator.
 *
 *
 */
package Operators.TOF_SCD;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.parameter.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.viewer.Contour.*;
import DataSetTools.dataset.*;
import DataSetTools.components.image.*;
import DataSetTools.components.ThreeD.*;
import DataSetTools.math.*;
import DataSetTools.instruments.*;
import DataSetTools.util.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

/** 
 *    This operator allows views of QxQyQz spaces.  These Q values are NOT
 *    crystal aligned
 */

public class ViewQxQyQz extends GenericTOF_SCD
{
  private static final String TITLE = "View Qx,Qy,Qz";
  private static Vector choice_list=null;

 /* ------------------------- DefaultConstructor -------------------------- */
 /** 
  */  
  public ViewQxQyQz()
  {
    super( TITLE );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for with the specified parameter values so
   *  that the operation can be invoked immediately by calling getResult().
   *
   *  @param  ds        The DataSet to view
   *  @param  choice    The viewing choice.
   */
   public ViewQxQyQz(  DataSet ds,
                       String choice)
   {
      super( TITLE );

      getParameter(0).setValue( ds );
      getParameter(1).setValue( choice );
   }


 /* ------------------------------ getCommand ----------------------------- */
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "RecipLattice", the command used to invoke this operator in Scripts
  */
  public String getCommand()
  {
    return "QxQyQzView";
  }

 /* ------------------------ setDefaultParameters ------------------------- */
 /** 
  * Sets default values for the parameters.  The parameters set must match the 
  * data types of the parameters used in the constructor.
  */
  public void setDefaultParameters()
  { 
    if(choice_list==null || choice_list.size()==0 ){
      choice_list=new Vector();
      choice_list.add("Qx,Qy vs Qz");
      choice_list.add("Qx,Qz vs Qy");
      choice_list.add("Qy,Qz vs Qx");
    }

    parameters = new Vector();  // must do this to clear any old parameters

    addParameter(new SampleDataSetPG("Data Set",null));
    ChoiceListPG clpg=new ChoiceListPG("Order",choice_list.elementAt(0));
    addParameter(clpg);
  }


 /* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator allows views of QxQyQz spaces.  These ");
    s.append("Q values are NOT crystal aligned. ");
    s.append("@assumptions The DataSet 'ds' is non-empty, and has a QxQyQz ");
    s.append("space present in it. \n\n");
    s.append("The string 'choice' can be used to determine which of Axis1, ");
    s.append("Axis2, and Axis3 should be made equal to Qx, Qy, or Qz. ");
    s.append("(This is further discussed in the 'Algorithm' section.) ");
    s.append("@algorithm There are 3 axis present in the DataSet indicated ");
    s.append("by 'path'.  They are: Qx, Qy, and Qz. \n\n ");
    s.append("The operator creates 3 axis, simply called Axis1, Axis2, and ");
    s.append("Axis3. 'choice' is a string that determines which of the axis ");
    s.append("Axis1, Axis2 or Axis3 should be made equal to Qx, Qy, or Qz. ");
    s.append("(For example, maybe Axis1 should be made equal to Qx, Axis2 ");
    s.append("should be made equal to Qz, and Axis3 should be made equal to ");
    s.append("Qy.)  \n\n ");
    s.append("The operator then makes sure that Axis1, Axis2, and Axis3 ");
    s.append("have been defined.  If any one has not, then an error string ");
    s.append("is returned and execution of the operator terminates.  ");
    s.append("Otherwise the operator continues. \n\n ");
    s.append("A ContourView is now created, consisting of 'ds' and Axis1, ");
    s.append("Axis2, and Axis3.  The ContourView is added to a JFrame, and ");
    s.append("the JFrame is shown.  The string 'Success' is returned. ");
    s.append("@param ds The directory path that indicates where the DataSet ");
    s.append("is");
    s.append("@param choice Determines which of Axis1, Axis2, and Axis3 ");
    s.append("should be made equal to Qx, Qy, or Qz");
    s.append("@return If successful, returns the string 'Success'. ");
    s.append("@error Returns an error string if Axis1, Axis2, or Axis3 are ");
    s.append("undefined. ");
    return s.toString();
  }

 /* ------------------------------ getResult ------------------------------- */
 /** 
  *  Executes this operator using the current values of the parameters.
  *
  */
  public Object getResult()
  { DataSet ds = (DataSet)(getParameter(0).getValue());
    String choice = (getParameter(1).getValue()).toString();
     JFrame jf = new JFrame( "Contour View:"+ ds.toString());

     jf.setSize( 400,600);
      QxQyQzAxesHandler Qax = new QxQyQzAxesHandler(ds);
      IAxisHandler Axis1 = null, Axis2 = null , Axis3 = null;
     
      String S =choice.substring(0,2);
     
      if( S.equals("Qx"))
        Axis1 = Qax.getQxAxis();
      else if( S.equals("Qy"))
        Axis1 = Qax.getQyAxis();
      S=choice.substring(3,5);
     
      if( S.equals("Qx"))
        Axis2 = Qax.getQxAxis();
      else if( S.equals("Qy"))
        Axis2 = Qax.getQyAxis();
      else if(S.equals("Qz"))
        Axis2 = Qax.getQzAxis();

      S= choice.substring(9);
      
      if( S.equals("Qx"))
        Axis3 = Qax.getQxAxis();
      else if(S.equals("Qy"))
        Axis3 = Qax.getQyAxis();
      else if(S.equals("Qz"))
        Axis3 = Qax.getQzAxis();
    if( Axis1 == null)
       return new ErrorString("Axis 1 undefined");
    if( Axis2 == null)
       return new ErrorString( "Axis2 undefined");
    if( Axis3 == null)
       return new ErrorString("Axis3 undefined"); 
    ContourView cv = new ContourView( ds, null ,Axis1,Axis2,Axis3);
    jf.getContentPane().setLayout( new GridLayout(1,1));
    jf.getContentPane().add(cv);
    jf.validate();
    jf.show();
    return "Success";
  }

 /* --------------------------------- clone -------------------------------- */
 /** 
  *  Creates a clone of this operator.  Operators need a clone method, so 
  *  that Isaw can make copies of them when needed.
  */
  public Object clone()
  { 
    ViewQxQyQz op = new ViewQxQyQz();
    op.CopyParametersFrom( this );
    return op;
  }

 /* ------------------------------- main ----------------------------------- */
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {if( args == null)
       {System.out.println( " Please specify the filename with the data");
        System.exit(0);
        }
      if( args.length <1)
       {System.out.println( " Please specify the filename with the data");
        System.exit(0);
        }
      String filename = args[0];
      DataSet[] data_set;

      data_set = new IsawGUI.Util().loadRunfile( filename );
      int spectra = data_set.length -1;
      if( args.length > 1)
        try
          {
            spectra = ( new Integer( args[1].trim())).intValue();
          }
        catch( Exception ss){}
     DataSet ds = data_set[spectra];
        // int Choice1 = 0, Choice2 = 1,Choice3 =2;
    
    
     System.out.println("Enter option desired");
     System.out.println("  a) Qx,Qy vs Qz");
     System.out.println("  b) Qx,Qz vs Qy");
     System.out.println("  c) Qy,Qz vs Qx");
     char c=0;
     try{
       while( (c <=32)&&(c!='a') &&(c!='b')&& (c!='c'))
          c = (char)System.in.read();
         }
      catch( Exception u){}
      String S = "Qx,Qy vs Qz";
      if( c =='b')
        {S = "Qx,Qz vs Qy";}
      else if( c=='c')
        {S = "Qy,Qz vs Qz";}
      /*DataSetOperator op = ds.getOperator( "Convert to Q");
      op.setParameter(new Parameter("nbins", new Integer(0)),2);
      Object O = op.getResult();
      if( O instanceof DataSet)
         ds = (DataSet)O;
      else
        {System.out.println( O);
         System.exit(0);
        }
       */
     
     
    ViewQxQyQz V = new ViewQxQyQz( ds, S );
    System.out.println( V.getResult());
    
  }
}
