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
 * Contact : Ruth Mikkelson <mikkelsond@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
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
  private static String[] choice_list={"Qx,Qy vs Qz", "Qx,Qz vs Qy", "Qy,Qz vs Qx"};

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
   *  @param  path        The directory path to the data directory
   *  @param  run_numbers A list of run numbers to be loaded
   */
   public ViewQxQyQz(  DataSet ds,
                       String choice)
   {
      super( TITLE );

      Parameter parameter = getParameter(0);
      parameter.setValue( ds );
      StringChoiceList sl= new  StringChoiceList(choice_list);
      sl.setString( choice);
      parameter = getParameter(1);
      parameter.setValue( sl );
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
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "DataSet", DataSet.EMPTY_DATA_SET);

                        
    addParameter( parameter );
    StringChoiceList  sl = new  StringChoiceList(choice_list);
    
    parameter = new Parameter("Order",sl);
                             

    addParameter( parameter );
  }

/* ------------------------------ draw_axes ----------------------------- */



 /* ------------------------------ getResult ------------------------------- */
 /** 
  *  Executes this operator using the current values of the parameters.
  *
  */
  public Object getResult()
  { DataSet ds = (DataSet)(getParameter(0).getValue());
    String choice = ((StringChoiceList)(getParameter(1).getValue())).toString();
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
  *  Creates a clone of this operator.  ( Operators need a clone method, so 
  *  that Isaw can make copies of them when needed. )
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
      Operator op = new Operators.ChgOp( ds);
      op.getResult();
     
    ViewQxQyQz V = new ViewQxQyQz( ds, S );
    System.out.println( V.getResult());
    
  }
}
