/*
 * File:  DSOpsImplementation.java
 *             
 * Copyright (C) 2000, Dennis Mikkelson
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
 * This file contains static methods that actually implement operations
 * on DataSets such as add, subtract, multiply and divide for the 
 * corresponding operators.
 *
 *  $Log$
 *  Revision 1.1  2002/02/22 20:59:42  pfpeterson
 *  Operator reorganization.
 *
 *  Revision 1.4  2001/08/16 19:14:23  dennis
 *  Added method AddDataBlocks to form a sum of Data blocks in a
 *  DataSet and to form an average DetectorPosition, weighted by
 *  the solid angles ( if present ).
 *
 *  Revision 1.3  2001/04/26 19:04:34  dennis
 *  Added copyright and GPL info at the start of the file.
 *
 *  Revision 1.2  2000/07/21 19:33:33  dennis
 *  changed String comparisons from == to .equals()
 *
 *  Revision 1.1  2000/07/10 22:35:48  dennis
 *  Now Using CVS 
 *
 *  Revision 1.2  2000/06/15 16:24:59  dennis
 *  Added method DoDSScalarOps() to implement +,-,*,/ by scalars
 *
 *  Revision 1.1  2000/06/14 21:41:45  dennis
 *  Initial revision
 *
 */

package DataSetTools.operator.DataSet;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;

/**
 * This class contains static methods that actually implement operations
 * on DataSets such as add, subtract, multiply and divide for the 
 * corresponding operators.
 */

public final class DSOpsImplementation implements Serializable
{
  /*
   *  Don't let anyone instantiate this class
   */
   private DSOpsImplementation(){}


  /* -------------------------- DoDSBinaryOp ------------------------------- */
  /**
   *  Carry out the specified binary operation on the given DataSet. This
   *  supports the binary operations of Add, Subtract, Multiply and Divide
   *  between two DataSets.
   *
   *  @param  op   The operation to carry out.  This must be one of
   *                  DataSetAdd,
   *                  DataSetSubtract,
   *                  DataSetMultiply,
   *                  DataSetDivide.
   *               There must be two parameters to the operator, the second
   *               DataSet operand and a boolean flag indicating wheter or not
   *               to create a new DataSet.  All parameters to the operation 
   *               must have been given values before calling this method.
   *
   *  @return An object containing the result of the operation.  This object
   *          may be a new DataSet, an ErrorString or a String.
   */

  public static Object DoDSBinaryOp( DataSetOperator op )
  {                        
                                                   // get the current data set
   DataSet ds_1        = op.getDataSet();
                                                   // get the parameters
   DataSet ds_2        = (DataSet)(op.getParameter(0).getValue());
   boolean make_new_ds =((Boolean)op.getParameter(1).getValue()).booleanValue();

   String op_name = op.getCommand();


    if ( !ds_1.SameUnits( ds_2 ) )    // DataSets are NOT COMPATIBLE TO COMBINE
      {
        ErrorString message = new ErrorString(
                                  "ERROR: DataSets have different units in"+
                                   op_name );
        System.out.println( message );
        return message;
      }
                                     // construct a new data set with the same
                                     // title, units, and operations as the
                                     // current DataSet, ds
    DataSet new_ds = ds_1.empty_clone(); 
                                            // now proceed to do the operation
    int num_data = ds_1.getNum_entries();
    Data data_1,
         data_2,
         new_data;

    for ( int i = 0; i < num_data; i++ )
    {
      data_1 = ds_1.getData_entry( i );      // get reference to the data entry

      data_2 = ds_2.getData_entry_with_id( data_1.getGroup_ID() );
 
      if ( data_2 != null )                  // there is a corresponding entry
      {                                      // to try to combine 
        if ( op_name.equals( "Add" )) 
          new_data = data_1.add( data_2 );  

        else if ( op_name.equals( "Sub" ) ) 
          new_data = data_1.subtract( data_2 );  

        else if ( op_name.equals( "Mult" )) 
          new_data = data_1.multiply( data_2 );  

        else if ( op_name.equals( "Div" )) 
          new_data = data_1.divide( data_2 );  

        else
        {
          System.out.println("ERROR: unsupported operation in DoDSBinaryOp");
          new_data = null;
        } 

        if ( new_data != null )            // they could be combined 
          new_ds.addData_entry( new_data );      
      }
    }

    if ( new_ds.getNum_entries() <= 0 )
    {
      return new ErrorString("Error: no compatible Data blocks to combine in "
                              + op_name );
    }
    else if ( make_new_ds ) 
    {
      new_ds.addLog_entry( "Operation " + op_name + " on " + ds_2 );
      new_ds.CombineAttributeList( ds_2 );
      return new_ds;
    } 
    else                         // copy Data blocks to the original DataSet
    {            
      ds_1.addLog_entry( "Operation " + op_name + " on " + ds_2 );
      ds_1.CombineAttributeList( ds_2 );  
      ds_1.removeAll_data_entries();        

      for ( int i = 0; i < new_ds.getNum_entries(); i++ )
        ds_1.addData_entry( new_ds.getData_entry(i) );  

      ds_1.notifyIObservers( IObserver.DATA_CHANGED );

      new_ds = null;
      return new String(op_name + " on " + ds_2 + " DONE");
    }
  }  


  /* -------------------------- DoDSScalarOp ------------------------------- */
  /**
   *  Carry out the specified scalar operation on the given DataSet. This
   *  supports the scalar operations of Add, Subtract, Multiply and Divide
   *  by scalars.
   *
   *  @param  op   The operation to carry out.  This must be one of
   *                  DataSetScalarAdd,
   *                  DataSetScalarSubtract,
   *                  DataSetScalarMultiply,
   *                  DataSetScalarDivide.
   *               There must be two parameters to the operator, the scalar  
   *               operand and a boolean flag indicating wheter or not
   *               to create a new DataSet.  All parameters to the operation 
   *               must have been given values before calling this method.
   *
   *  @return An object containing the result of the operation.  This object
   *          may be a new DataSet and ErrorString, or a String.
   */

  public static Object DoDSScalarOp( DataSetOperator op )
  {
                                                   // get the current data set
   DataSet ds  = op.getDataSet();
                                                   // get the parameters
   float   value       = ((Float)(op.getParameter(0).getValue()) ).floatValue();
   boolean make_new_ds =((Boolean)op.getParameter(1).getValue()).booleanValue();

   String op_name = op.getCommand();

   
   if ( op_name.equals( "Div" ) && value == 0 )
     return new ErrorString( "ERROR: Division by zero in scalar divide" );


   DataSet new_ds = null;
   if ( make_new_ds )
   {
     new_ds = ds.empty_clone();
     new_ds.addLog_entry( "Operation " + op_name + " with value " + value );
   }
   else
     ds.addLog_entry( "Operation " + op_name + " with value " + value ); 


   int num_data = ds.getNum_entries();
   Data data,
        new_data;
   for ( int i = 0; i < num_data; i++ )
   {
     data = ds.getData_entry( i );    
                                       // carry out the operation, assuming
                                       // zero error in the the numerical value.
     if ( op_name.equals("Add") )
        new_data = data.add( value );

     else if ( op_name.equals("Sub") )
        new_data = data.subtract( value );

     else if ( op_name.equals( "Mult" ) )
        new_data = data.multiply( value );

     else if ( op_name.equals( "Div" ) )
        new_data = data.divide( value );

     else
       {
         System.out.println("ERROR: unsupported operation in DoDSScalarOp");
         new_data = null;
       }

      if ( make_new_ds )
        new_ds.addData_entry( new_data );
      else 
        ds.replaceData_entry( new_data, i );
    }

    if ( make_new_ds ) 
      return new_ds;
    else
    {
      ds.notifyIObservers( IObserver.DATA_CHANGED );
      return new String( op_name + " with value " + value + " DONE" );
    } 
   }


  /* -------------------------- AddDataBlocks ---------------------------- */
  /**
   *  Remove all of the Data blocks in a DataSet and put the sum of those
   *  Data blocks back into the DataSet.  The Group ID's should either all
   *  be the same, or they should all be different.  If they are all the
   *  same, the DetectorPosition attribute will NOT be adjusted from the
   *  default behavior when adding Data blocks.  Otherwise, the summed 
   *  Data block will have it's DetectorPosition set to the weighted
   *  average of the DetectorPositions.  The weights used are the solid 
   *  angle values, if all Data blocks have a solid angle attribute.
   *
   */
   public static SpecialString AddDataBlocks( DataSet ds )
   {
      if ( ds == null )
        return new ErrorString("null DataSet in AddDataBlocks");

      if ( ds.getNum_entries() < 1 )
        return new ErrorString("no Data blocks to add in AddDataBlocks");

      Attribute attr;
      boolean   same_group_id     = true;
      boolean   have_solid_angles = true;
      boolean   have_det_pos      = true;
      int       group_id          = ds.getData_entry(0).getGroup_ID();
      Data      sum               = ds.getData_entry( 0 ); // get the first
                                                           // and add all the
                                                           // later ones to it
      for ( int i = 1; i < ds.getNum_entries(); i++ )
      {                                                   
        Data d = ds.getData_entry(i);

        if ( d.getGroup_ID() != group_id )
          same_group_id = false;

        attr = d.getAttribute( Attribute.SOLID_ANGLE );
        if ( attr == null )
          have_solid_angles = false;

        attr = d.getAttribute( Attribute.DETECTOR_POS );
        if ( attr == null )
          have_det_pos = false;

        sum = sum.add( ds.getData_entry(i) );
        if ( sum == null )
          return  new ErrorString("ERROR: Data block not compatible to add" );
      }
                                                     // adjust the detector
                                                     // position to the new
                                                     // "effective" position
      if ( have_det_pos && !same_group_id )
      {
        float            weights[] = new float[ ds.getNum_entries() ];
        DetectorPosition points[]  = new DetectorPosition[ds.getNum_entries()];

        for ( int i = 1; i < ds.getNum_entries(); i++ )
        {
          Data d =  ds.getData_entry(i);
          points[i] =
               (DetectorPosition)(d.getAttributeValue(Attribute.DETECTOR_POS));
          if ( have_solid_angles )
          {
            Float sa = (Float)(d.getAttributeValue( Attribute.SOLID_ANGLE ));
            weights[i] = sa.floatValue();
          }
          else
            weights[i] = 1;
        }

        DetectorPosition ave_pos =
                        DetectorPosition.getAveragePosition( points, weights );
        DetPosAttribute ave_pos_attr =
                        new DetPosAttribute( Attribute.DETECTOR_POS, ave_pos );

        sum.setAttribute( ave_pos_attr );
      }

      for ( int i = ds.getNum_entries()-1; i >= 0 ; i-- )
        ds.removeData_entry(i);                    // throw out used entries

      ds.addData_entry( sum );

      return null;
   } 

}
