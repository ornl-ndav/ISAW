/*
 * File:  InputBox.java
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
 *
 * Modified:
 *
 * $Log$
 * Revision 1.4  2003/10/14 21:05:15  dennis
 * Fixed javadocs on constructor to build cleanly with jdk 1.4.2.
 * and made a few other fixes to javadocs.
 *
 * Revision 1.3  2003/09/27 13:19:07  rmikk
 * Made the dialog box non-modal with a sleep until new values are set
 * at the end.
 *
 * Revision 1.2  2003/01/07 16:04:48  rmikk
 * Input Box now disappears after pressing the Apply button
 *
 * Revision 1.1  2002/12/23 17:12:58  rmikk
 * Initial Commit
 *
 
*/
package Operators.Generic;

import DataSetTools.operator.*;
import DataSetTools.dataset.*;
import DataSetTools.components.ParametersGUI.*;
import java.util.*;
import DataSetTools.operator.Generic.Batch.*;
import DataSetTools.util.*;


/** This class behaves like Visual Basic's Input box to allow quick entry of 
 *  data by the user at run time.  
 */
public class InputBox  extends GenericBatch
 {
  Vector Prompts,
         InitValues,
         DataSetList;

  boolean Done;
 
 /**
  *   Construct an input box that modifies the values in the InitValues 
  *   vector to those input by the user, after getResult() is called. 
  *
  *   @param Title        The title of the ParametersDialog box
  *   @param Prompts      The Vector of Prompts.  They should be strings
  *   @param InitValues   The Vector of initial values
  *   @param DataSetList  The Vector of DataSets that could be choices for 
  *                       DataSet Parameters
  */
  public InputBox( String Title, 
                   Vector Prompts,  
                   Vector InitValues, 
                   Vector DataSetList)
    {
     super( "InputBox" );
     setParameter( new Parameter( "Title", Title) ,  0);
     setParameter( new Parameter( "Prompts", Prompts) ,  1);
     setParameter( new Parameter( "InitValues", InitValues) ,  2);
     setParameter( new Parameter( "DSList", DataSetList) ,  3);
     this.Prompts = Prompts;
     this.InitValues = InitValues;
     this.DataSetList = DataSetList;
    }

  public InputBox()
    {
     super( "InputBox" );
     setDefaultParameters();
    }
   
  /** Returns "InputBox", the command used by ISAW's scripting language
  */
  public String getCommand()
    {
     return "InputBox";
     }

   public String toString()
     {
       return "InputBox";
     }

   /**
   *   Sets the default parameters
   */
   public void setDefaultParameters()
    {parameters = new Vector();
     addParameter( new Parameter( "Title" , getTitle()));
     addParameter( new Parameter( "Prompts" ,new Vector())   );
     addParameter( new Parameter( "InitValues" , new Vector())  );
     addParameter( new Parameter( "DSList" , new Vector()));     
     }


   public String getDocumentation()
     {
      StringBuffer S = new StringBuffer(3000);
      
      S.append( "@overview The InputBox operator will pop up a ParametersDialog box ");
      S.append( "that will allow the user to change some values in this box.  These ");
      S.append( "changed values will be copied into the appropriate position in one ");
      S.append( "of the input java Vectors( ISAW Script Arrays.");
   
      S.append( "@algorithm  The InputBox copies the Prompt Vector and InitValues Vector ");
      S.append( "to another internal operator (ArgOperator).  This operator uses these ");
      S.append( "Prompt and InitValues vectors to setDefaultParameters.  A Parameter  ");
      S.append( "dialog box using this ArgOperator pops up.  The values entered by the user ");
      S.append( "are copied to the InitValues vector.  Vectors are Input-Output variables so ");
      S.append( "the initial InitValues Vector will then have the new values.");

      S.append( "@param Title- The Title on the Parameter Dialog box.");
      S.append( "@param Prompts- The Vector(ISAW Array) of prompts for the new values");
      S.append( "@param InitValues- The initial values of the corresponding variables. ");
      S.append( "            The return values are in this Vector.");
      S.append( "@param DataSetList-The list of DataSets that can be used to select a");
      S.append( " DataSet for initial values that are DataSets. ");

      S.append( "@return Returns 'Success'.  The real results are returned in the Vector ");
      S.append( " or ISAW Array InitValues ");
      
      S.append( "@error null prompts or values ");
      S.append( "@error Prompt size differs from InitValues size \n");

      S.append( "@assumptions new operators should be created to convert Strings to SpecialStrings ");
      S.append( "to get File Dialog Boxes to appear or to initialize Directory paths ");
      S.append( "to those in IsawProps.dat");

      return S.toString();

     } 
 
  
    /**
    *    Pops up the Parameters Dialog box to get the new values. These new
    *    values are then placed in the InitValues Vector and returned.
    *    ERROR Conditions: If the Prompt Vector and/or InitValue are null 
    *    or disagree in size.
    */
    public Object getResult()
     {
      String Title = getParameter(0).getValue().toString();
      Prompts = (Vector)( getParameter( 1 ).getValue() );
      InitValues = (Vector)( getParameter( 2 ).getValue() );
      DataSetList = (Vector)( getParameter( 3 ).getValue() );
      Done = false;
      if( ( Prompts == null ) || ( InitValues == null ))
        return new ErrorString( "null prompts or values");

      if( Prompts.size() != InitValues.size() )
         return new ErrorString(" Prompt size differs from InitValues size" );

      ArgOperator A = new ArgOperator( Title, Prompts , InitValues, this );
      JParametersDialog JP = new JParametersDialog(
                       A,
                       new VectDataSetHandler( DataSetList ),
                       null, null, false);
      while( !Done)
           try{
            Thread.sleep(250);
           }
           catch(InterruptedException e)
             { 
                Done = true;
              }
      return new DataSetTools.operator.Generic.Batch.ExitClass(); 
     }


  /** 
   *   This class implements the IDataSetListHandler interface for DataSet Lists
   *   that are stored in a Vector
   */
  class  VectDataSetHandler  implements IDataSetListHandler
    {
     DataSet[] ds;
     public VectDataSetHandler( Vector V)
       {if( V == null)
          ds = new DataSet[0];
        else
          {
           ds = new DataSet[ V.size() ];
           for( int i = 0; i < V.size() ; i++ )
             ds[i] = ((DataSet) V.elementAt(i));
           }

        }

     public DataSet[] getDataSets()
       {
         return ds;
       }

    }//VectDataSetHandler

  /** 
  *   Test program for InputBox
  */
  public static void main( String args[] )
    {Vector Prompts, InitValues;
     Prompts = new Vector();
     InitValues = new Vector();
     Prompts.addElement("Enter Int");
     Prompts.addElement("Enter String");
     InitValues.addElement( new Integer( 12));
     InitValues.addElement( "abcdefg");

     InputBox IB = new InputBox("Test", Prompts,InitValues, new Vector() );

     System.out.println( IB.getResult() );
     for( int i=0; i<2;i++)
      System.out.println( "new val="+ InitValues.elementAt(i));
    } 
 
  }

/**
*  This operator is the operator argument for the JParametersDialog box. Its job is
*  to set up the Parameters so the dialog box allows the user to change the values, then
*  it copies the changed values into the InitValues Vector
*/ 
class ArgOperator extends GenericBatch
 {
  String Title;
  Vector Prompts,InitValues;
  InputBox mainn;
  public ArgOperator( String   Title, 
                      Vector   Prompts, 
                      Vector   InitValues, 
                      InputBox mainn)
    {super( Title );
     this.Prompts = Prompts;
     this.InitValues = InitValues;
     setDefaultParameters();
     this.mainn = mainn;
     }

   public void setDefaultParameters()
    {
     super.parameters = new Vector();
     if( InitValues == null)
       return;
     for( int i = 0; i<InitValues.size(); i++)
       super.parameters.addElement( new Parameter( (String)Prompts.elementAt(i),
                                              InitValues.elementAt( i ) ) );
    }

  public String getCommand()
    {return "Args";
    }

  public Object getResult()
    {
     for( int i=0; i < super.parameters.size(); i++)
       InitValues.setElementAt( super.getParameter(i).getValue(), i);
     mainn.Done = true;
     return new DataSetTools.operator.Generic.Batch.ExitClass() ;
    }

 }//ArgOperator Class
