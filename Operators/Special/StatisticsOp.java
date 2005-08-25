/*
 * File:  StatisticsOp.java
 *
 * Copyright (C) 2005 Grant Adams
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
 *           Grant Adams <adamsg@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *  $Log$
 *  Revision 1.3  2005/08/25 14:51:38  dennis
 *  Made/added to category DATA_SET_ANALYZE_MACROS.
 *
 *  Revision 1.2  2005/05/12 16:02:40  dennis
 *  Inserted missing cvslog message and tag to record logging info.
 *
 *  Revision 1.1  2005/05/12 15:58:20  dennis
 *  Initial version of operator to calculate basic statistics of one Data
 *  block, specified by index. (Grant Adams)
 *  Added main test program (Dennis)
 */

package Operators.Special;

import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;


public class StatisticsOp implements Wrappable, IWrappableWithCategoryList
{
   public DataSet ds = null;
   public String  Op_Code = "Please give a command here.";
   public int     index = 0;

  /**
   *  Decided this would be better name but it is already taken hence why i did
   * not make it the name to begin with 
   */
  public String getCommand(  ) {
    return "Statistics";
  }


 /**
  *  Get the list of categories describing where this operator should appear
  *  in the menu system.
  *
  *  @return an array of strings listing the menu where the operator 
  *  should appear.
  */
  public String[] getCategoryList()
  {
    return Operator.DATA_SET_ANALYZE_MACROS;
  }


  /**
   *  documentation method as template requires
   */
  public String getDocumentation(  ) {
    
    StringBuffer s = new StringBuffer(  );
       s.append( "@overview This operator makes statistical calculations on" );
       s.append( "a Data Block at the index of the Data Set given by the ");
       s.append( "user.");
       
       s.append( "@algorithm The operator first finds the Data Block in the " );
       s.append( "Data Set and makes a check to see if the Data Block exist " );
       s.append( "in the Data Set. If not it returns an error message " );
       s.append( "indicating that the Data block doesn't exist. ");
       s.append( "If it exists, then the operator attempts to pull out the y" );
       s.append( " values for the Data Block. If it is a Datablock with no " );
       s.append( "values, we have a Empty DataBlock and therefore can not " );
       s.append( "make any calculations and the operator lets you know that." );
       s.append( "The operator will continue if there are values, by " );
       s.append( "checking the Op_code given by user until it finds a match." );
       s.append( "If a match is found, there is a call to a function in " );
       s.append( "gov.anl.ipns.MathTools.Statistics which is formated to a " );
       s.append( "Double for use of displaying result. If there is no match ");
       s.append( "the operator will let you know you mispelled it or the ");
       s.append( "command doesn't exist." ); 
       
       s.append( "@param ds is the Data Set you are working with" );
       
       s.append( "@param Op_Code is the statistical command you would like" );
       s.append( "performed. Spelling counts but case sensitivity doesn't." );
       s.append( "Please use the following commands for your particular " );
       s.append( "statistic.\n   Statistic = Op_code \n   mean = Mean \n   " );
       s.append( "maximum value = Max \n   minimum value = Min \n   " );
       s.append( "standard deviation = StdDev" );
       
       s.append( "@param index is the Data Block in the Data Set you would " );
       s.append( "like to find the average of." ); 
       
       s.append( "@return The operator returns an error message if the Data " );
       s.append( "Block doesn't exist in the Data Set due to index being " );
       s.append( "out of range or a bad Data Set. Another error message may " );
       s.append( "occur if there no information in the Data Block. Another " );
       s.append( "error message is returned if the Op_code is not match a " );
       s.append( "proper code name. Otherwise it returns one Float that " );
       s.append( "represents the statistic requested for the choosen Data " );
       s.append( "Block" );
       
       s.append( "@error Division by zero and null pointer exceptions " );
       s.append( "should have been prevented from occurring." );
       return s.toString(  );
  }

  /**
   *     This method does the calculation of the average y value
   *     for a Data block at the given index
   */
  public Object calculate(  ) {

    if(ds != null)
    {
       Data db = ds.getData_entry(index); // get my db if it is in the data set
       if(db != null) //check if i actually did get a data block
       {
          float[] arr = db.getY_values();   //get y values to work with

          if(arr.length <= 0)               //check to prevent errors
            return new ErrorString("The Data Block is EMPTY and" + 
                "therefore has no Y values to average."); //error message

          if(Op_Code.trim().equalsIgnoreCase("Mean")) // check Op_Code 
            return new Float(gov.anl.ipns.MathTools.Statistics.mean(arr));

          if(Op_Code.trim().equalsIgnoreCase("Max")) // check Op_Code
            return new Float(gov.anl.ipns.MathTools.Statistics.maximum(arr));

          if(Op_Code.trim().equalsIgnoreCase("Min")) // check Op_Code
            return new Float(gov.anl.ipns.MathTools.Statistics.minimum(arr));

          if(Op_Code.trim().equalsIgnoreCase("StdDev")) // check Op_Code
            return new 
                   Float(gov.anl.ipns.MathTools.Statistics.std_deviation(arr));

          return new ErrorString("That is not a valid operation code or" +
             "you made a spelling error."); //error message
       }  
       return new ErrorString("The data set doesn't have a" + 
          " Data Block at that index.");
    }

    //return message for the display letting them know they are trying to 
    //access something that doesn't exist.
    return new ErrorString("The data set is null");
  }


  /**
   *  Basic test program for StatisticsOp
   */
  public static void main( String args[] )
  {
    float x_vals[] = { 0, 1, 2, 3 };
    float y1_vals[] = { 1, 2, 3 };
    float y2_vals[] = { 10, 20, 30 };
    XScale x_scale = new VariableXScale( x_vals );

    HistogramTable d1 = new HistogramTable( x_scale, y1_vals, 1 );
    HistogramTable d2 = new HistogramTable( x_scale, y2_vals, 2 );

    DataSet ds = new DataSet( "Test DataSet", "Created Empty DataSet" );
    ds.addData_entry( d1 );
    ds.addData_entry( d2 );

    Operator stat_op = new JavaWrapperOperator( new StatisticsOp() );
    stat_op.getParameter(0).setValue( ds );
    stat_op.getParameter(1).setValue( "Mean" );
    stat_op.getParameter(2).setValue( new Integer(1) );
    System.out.println( stat_op.getResult() );

    stat_op.getParameter(0).setValue( ds );
    stat_op.getParameter(1).setValue( "Min" );
    stat_op.getParameter(2).setValue( new Integer(1) );
    System.out.println( stat_op.getResult() );

    stat_op.getParameter(0).setValue( ds );
    stat_op.getParameter(1).setValue( "Max" );
    stat_op.getParameter(2).setValue( new Integer(1) );
    System.out.println( stat_op.getResult() );

    stat_op.getParameter(0).setValue( ds );
    stat_op.getParameter(1).setValue( "StdDev" );
    stat_op.getParameter(2).setValue( new Integer(1) );
    System.out.println( stat_op.getResult() );
  }

}
