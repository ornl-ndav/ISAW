/*
 * File:  XDateTime.java 
 *             
 * Copyright (C) 2002, Dennis Mikkelson
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
 * $Log$
 * Revision 1.2  2002/05/29 22:43:12  dennis
 * Now returns day and date as the label, and the time as the value of the
 * information.
 *
 * Revision 1.1  2002/04/08 15:40:32  dennis
 * Initial version of operator to convert elapsed time in
 * seconds to a Date and Time form, for use with SDDS DataSets.
 * NOT COMPLETE... for now it just shows the current time.
 *
 */

package DataSetTools.operator.DataSet.Information.XAxis;

import  java.io.*;
import  java.util.*;
import  java.text.*; 
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.util.*;
import  DataSetTools.operator.Parameter;

/**
 *  This operator uses the StartTime attribute to produce a string giving 
 *  the Date and Time for a specific x value.
 */

public class XDateTime extends  XAxisInformationOp 
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

  public XDateTime( ) 
  {
    super( "Find Date and Time" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds          The DataSet to which the operation is applied
   *                      the specified min and max channels will be kept.
   *  @param  i           index of the Data block to use for the x-scale(time) 
   *  @param  time        the time(x-value) at which the Date is to be obtained
   */
  public XDateTime( DataSet ds, int i, float time )
  {
    this();                        

    Parameter parameter = getParameter(0); 
    parameter.setValue( new Integer(i) );
    
    parameter = getParameter(1); 
    parameter.setValue( new Float(time) );
    
    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor: 
   *         in this case, Date 
   */
   public String getCommand()
   {
     return "Date";
   }


 /* -------------------------- setDefaultParmeters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "Data block index", new Integer(0) );
    addParameter( parameter );

    parameter = new Parameter( "Time(sec)" , new Float(0) );
    addParameter( parameter );
  }

  /* -------------------------- XInfo_label ---------------------------- */
  /**
   * Get string label for the xaxis information, in this case the month,
   * day and year are returned.
   *
   *  @param  x    the x-value for which the axis label is to be obtained.
   *  @param  i    the index of the Data block that will be used for obtaining
   *               the label.
   *
   *  @return  String describing the information provided by X_Info().
   */
   public String XInfo_label( float x, int i )
   {
     Date date = getDate( x, i ); 

     DateFormat df = DateFormat.getDateInstance( DateFormat.SHORT );
     return df.format(date);
   }


  /* ------------------------------ X_Info ----------------------------- */
  /**
   * Get the axis information at one point only, in this case the time of
   * day is returned.
   *
   *  @param  x    the x-value for which the axis information is to be obtained.
   *
   *  @param  i    the index of the Data block for which the axis information
   *               is to be obtained.
   *
   *  @return  information for the x axis at the specified x.
   */
   public String X_Info( float x, int i )
   {
     Date date = getDate( x, i ); 

     DateFormat tf = DateFormat.getTimeInstance( DateFormat.MEDIUM );
     return tf.format(date);
   }


   private Date getDate( float x, int i )
   {
     DataSet ds = this.getDataSet();
     Data d = ds.getData_entry( i );
     if ( d != null )
     {
       StringAttribute start_attr = 
                            (StringAttribute)d.getAttribute( "StartTime" );
       if ( start_attr != null )
       { 
         long time = 0;

         String start_str = start_attr.getStringValue();
         try
         {
           double start = Double.valueOf( start_str ).doubleValue(); 
           time = (long)start;
         }
         catch ( NumberFormatException e )
         {
           System.out.println("WARNING: StartTime attribute NOT a number" +
                                        " in XDateTime" ); 
         }
         time = (long)x + time;
         return new Date( time * 1000 );
       } 
       else
         System.out.println("StartTime attribute == null in XDateTime " );
     }
     return new Date( 0 );
   }


  /* ---------------------------- getResult ------------------------------- */

  public Object getResult()
  {
                                     // get the current data set
    DataSet ds = this.getDataSet();
    int   i    = ( (Integer)(getParameter(0).getValue()) ).intValue();
    float time = ( (Float)(getParameter(1).getValue()) ).floatValue();

    return X_Info( time, i );
  }  

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DateTime Operator.  The list 
   * of parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    XDateTime new_op = new XDateTime( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

}
