/*
 * File:  GetField.java 
 *             
 * Copyright (C) 2000, Ruth Mikkelson,
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
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
 *  $Log$
 *  Revision 1.5  2004/03/15 03:28:23  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.4  2003/02/03 19:05:50  dennis
 *  Added getDocumentation() method and java docs on getResult().
 *  (Mike Miller)
 *
 *  Revision 1.3  2002/11/27 23:16:41  pfpeterson
 *  standardized header
 *
 *  Revision 1.2  2002/09/19 16:00:05  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.1  2002/02/22 21:00:07  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Attribute;

import gov.anl.ipns.Util.Numeric.*;
import gov.anl.ipns.Util.SpecialStrings.*;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;

/**
  * This operator gets a DataSet Field value 
  *
  *  @see  DS_Attribute
  */

public class GetField extends    DS_Attribute 
                                 implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this
   * constructor is used, the operator must be subsequently added to the
   * list of operators of a particular DataSet.  Also, meaningful values for
   * the parameters should be set ( using a GUI ) before calling getResult()
   * to apply the operator to the DataSet this operator was added to.
   */

  public GetField( )
  {
    super( "Get DataSet Field" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately 
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *  @param  Field       The Field to be gotten.
   *
   */

  public GetField  ( DataSet ds, DSFieldString  Field )
  {
    this();                         // do the default constructor, then set
                                    // the parameter value(s) by altering a
                                    // reference to each of the parameters

    IParameter parameter = getParameter( 0 );
    parameter.setValue( Field );

    

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }

/* ---------------------------getDocumentation--------------------------- */
 /**
  *  Returns a string of the description/attributes of GetField
  *   for a user activating the Help System
  */
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    Res.append("@overview This operator returns the value ");
    Res.append("of the specified field.\n");
    Res.append("@algorithm Given a data set and the field, ");
    Res.append("the field value is found.\n");
    Res.append("@param ds\n");
    Res.append("@param field\n");
    Res.append("@return an Object containing the field value\n"); 
    Res.append("@error No such field\n");  
    
    return Res.toString();
    
  }

  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return	the command name to be used with script processor: in this case,
   * GetField
   */
   public String getCommand()
   {
     return "GetField";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "Field?", new DSFieldString("Title") );
    addParameter( parameter );
  }


  /* ---------------------------- getResult ------------------------------- */
  /**
   * @return The return type is dependent on the desired field.
   * Field               Return Type
   * -------------       -----------
   * Title               String
   * X_Label             String
   * X_Unit              String
   * Y_Label             String
   * Y_Unit              String
   * Max_Samples         int
   * X_Range             UniformXScale
   * Y_Range             ClosedInterval
   * Pointed_at_index    int
   * Pointed_at_ID       Integer
   * Num_Groups          Integer
   * Group_IDs           String
   * Num_Selected        Integer
   * Selected_Groups     String
   *
   * Error String returned if field not found  
   */

  public Object getResult()
    { 
     DataSet ds = getDataSet();
     String S = ((DSFieldString)(getParameter(0).getValue())).toString();
    
    if( S.equals( DSFieldString.TITLE ))
        return ds.getTitle();
       
     else if( S.equals( DSFieldString.X_LABEL ))
        return ds.getX_label();
       
     else if( S.equals( DSFieldString.X_UNITS )) 
        return ds.getX_units();     

     else if( S.equals( DSFieldString.Y_LABEL ))
       return  ds.getY_label();

     else if( S.equals( DSFieldString.Y_UNITS ))
        return ds.getY_units();

     else if ( S.equals(DSFieldString.MAX_SAMPLES) )
        return new Integer( ds.getMaxXSteps() );

     else if ( S.equals( DSFieldString.X_RANGE ) )
        return ds.getXRange();

     else if ( S.equals( DSFieldString.Y_RANGE ) )
        return ds.getYRange();

     else if( S.equals( DSFieldString.POINTED_AT_INDEX ))
        return new Integer( ds.getPointedAtIndex());

     else if( S.equals( DSFieldString.POINTED_AT_ID ))
     {
        int index = ds.getPointedAtIndex();
        if ( index < 0 )
          return new Integer( index );
        else
        {
          int id = ds.getData_entry(index).getGroup_ID();
          return new Integer( id );
        }
     }

     else if ( S.equals( DSFieldString.NUM_GROUPS ))
        return new Integer ( ds.getNum_entries() );

     else if ( S.equals( DSFieldString.GROUP_IDS  ) )
     {
       int num_data     = ds.getNum_entries();

       int ids[] = new int[ num_data ];
       for ( int i = 0; i < num_data; i++ )
         ids[i] = ds.getData_entry(i).getGroup_ID();

       String result = IntList.ToString( ids );
       return result;
     }

     else if ( S.equals( DSFieldString.NUM_SELECTED ))
       return new Integer( ds.getNumSelected() );
     
     else if ( S.equals( DSFieldString.SELECTED_GROUPS  ) )
     {
       int num_selected = ds.getNumSelected();
       int num_data     = ds.getNum_entries();

       int index = 0;
       int ids[] = new int[ num_selected ];
       for ( int i = 0; i < num_data; i++ )
         if ( ds.isSelected(i) )
         {
           ids[index] = i;
           index++;
         }
       String result = IntList.ToString( ids );
       return result;
     }

     else 
        return new ErrorString( "No such Field");   
     
    
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current GetField Operator.  The list of parameters 
   * and the reference to the DataSet to which it applies is copied.
   */
  public Object clone()
  {
    
   GetField new_op = new GetField( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

  /* --------------------------- main ----------------------------------- */
  /**
   *  Main program for testing purposes
   */
  public static void main( String[] args )
  {
    System.out.println("Test of GetField starting...");
    
    DataSet ds = DataSetFactory.getTestDataSet();
    
    GetField op = new GetField( ds, new DSFieldString(DSFieldString.TITLE) );
    System.out.println("Results: " + op.getResult().toString() );
 
    System.out.println();
    System.out.println( op.getDocumentation() );
    
    System.out.println("Test of GetField done.");
  }

}
