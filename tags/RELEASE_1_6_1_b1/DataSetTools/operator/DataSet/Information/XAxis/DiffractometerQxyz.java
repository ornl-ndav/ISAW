/*
 * File:  DiffractometerQxyz.java
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
 * Revision 1.5  2004/01/24 19:33:43  bouzekc
 * Removed unused variables in main().
 *
 * Revision 1.4  2003/01/13 20:24:13  dennis
 * Added getDocumentation() method, simple main test program and
 * javadocs on getResult(). (Chris Bouzek)
 *
 * Revision 1.3  2002/11/27 23:18:10  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/09/19 16:01:29  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.1  2002/07/31 16:28:00  dennis
 * Calculate vector Q in laboratory frame of reference for a
 * diffractometer ( eg. SAND )
 *
 *
 */

package DataSetTools.operator.DataSet.Information.XAxis;

import  java.io.*;
import  java.util.*;
import  java.text.*;
import  DataSetTools.dataset.*;
import  DataSetTools.math.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.parameter.*;
import  DataSetTools.viewer.*;
import  DataSetTools.retriever.*;

/**
 *  This operator produces a string giving the values of Qx, Qy, Qz
 *  for a specific bin in a histogram, in the laboratory frame of reference.
 */

public class DiffractometerQxyz extends    XAxisInformationOp
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
  public DiffractometerQxyz( )
  {
    super( "Find Qx, Qy, Qz" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds    The DataSet to which the operation is applied
   *  @param  i     index of the Data block to use
   *  @param  tof   the time-of-flight at which Qx,Qy,Qz is to be obtained
   */
  public DiffractometerQxyz( DataSet ds, int i, float tof )
  {
    this();

    IParameter parameter = getParameter(0);
    parameter.setValue( new Integer(i) );

    parameter = getParameter(1);
    parameter.setValue( new Float(tof) );

    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return the command name to be used with script processor:
   *         in this case, Qxyz
   */
   public String getCommand()
   {
     return "Qxyz";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter( "Data block index", new Integer(0) );
    addParameter( parameter );

    parameter = new Parameter( "TOF(us)" , new Float(0) );
    addParameter( parameter );
  }


  /* -------------------------- PointInfoLabel --------------------------- */
  /**
   * Get string label for the xaxis information.
   *
   *  @param  x    the x-value for which the axis label is to be obtained.
   *  @param  i    the index of the Data block that will be used for obtaining
   *               the label.
   *
   *  @return  String describing the information provided by X_Info(),
   *           "Qx,Qy,Qz".
   */
   public String PointInfoLabel( float x, int i )
   {
     return "Qx,Qy,Qz";
   }


  /* ------------------------------ PointInfo ----------------------------- */
  /**
   * Get Qx,Qy,Qz at the specified point.
   *
   *  @param  x    the x-value (tof) for which the axis information is to be
   *               obtained.
   *
   *  @param  i    the index of the Data block for which the axis information
   *               is to be obtained.
   *
   *  @return  information for the x axis at the specified x.
   */
   public String PointInfo( float x, int i )
   {
     DataSet ds  = this.getDataSet();
     Data    d   = ds.getData_entry(i);

     DetectorPosition pos = (DetectorPosition)
                             d.getAttributeValue( Attribute.DETECTOR_POS );

     float initial_path =
             ((Float)d.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();

     Position3D q_pos = tof_calc.DiffractometerVecQ(pos, initial_path, x);

     float xyz[] = q_pos.getCartesianCoords();

     NumberFormat f = new DecimalFormat( "0.####E0" );;

     return f.format(xyz[0]) + "," + f.format(xyz[1]) + "," + f.format(xyz[2]);
   }

  /* ---------------------- getDocumentation --------------------------- */
  /**
   *  Returns the documentation for this method as a String.  The format
   *  follows standard JavaDoc conventions.
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator produces a string giving the values ");
    s.append("of Qx, Qy, Qz for a specific bin in a histogram in a ");
    s.append("DataSet, in the laboratory frame of reference.\n");
    s.append("@assumptions The DataSet must contain spectra with ");
    s.append("attributes giving the detector position and initial path.\n");
    s.append("@algorithm This operator first uses the Data block index to ");
    s.append("retrieve the data entry in the Data block.\n");
    s.append("Then the operator uses the detector position and initial ");
    s.append("path to retrieve the 3D position coordinate for the specified ");
    s.append("time-of-flight value.\n");
    s.append("Finally, the 3D coordinate is transformed into Cartesian ");
    s.append("X, Y, and Z coordinates.\n");
    s.append("@param ds The DataSet to which the operation is applied.\n");
    s.append("@param i The index of the Data block to use.\n");
    s.append("@param tof The time-of-flight at which Qx,Qy,Qz is to be ");
    s.append("obtained.\n");
    s.append("@return String which consists of the Qx, Qy, and Qz values for ");
    s.append("a specific bin in a histogram in a DataSet.\n");
    return s.toString();
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
   *  Produces a string giving the values of Qx, Qy, Qz for a specific bin
   *  in a histogram, in the laboratory frame of reference.
   *
   *  @return String giving the values of Qx, Qy, Qz
   */

  public Object getResult()
  {
                                     // get the current data set
    DataSet ds = this.getDataSet();
    int   i    = ( (Integer)(getParameter(0).getValue()) ).intValue();
    float tof  = ( (Float)(getParameter(1).getValue()) ).floatValue();

    return PointInfo( tof, i );
  }


  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current DateTime Operator.  The list
   * of parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    DiffractometerQxyz new_op = new DiffractometerQxyz( );
                                                 // copy the data set associated
                                                 // with this operator
    new_op.setDataSet( this.getDataSet() );
    new_op.CopyParametersFrom( this );

    return new_op;
  }

  /* --------------------------- main ----------------------------------- */
  /*
   *  Main program for testing purposes
   */
  public static void main( String[] args )
  {
    int index;
    float TOF;

    StringBuffer p = new StringBuffer();

    index = 50;
    TOF = (float)7135.623;

    String file_name = "/home/groups/SCD_PROJECT/SampleRuns/GPPD12358.RUN";
                       //"D:\\ISAW\\SampleRuns\\GPPD12358.RUN";

    try
    {
      RunfileRetriever rr = new RunfileRetriever( file_name );
      DataSet ds1 = rr.getDataSet(1);
      new ViewManager(ds1, IViewManager.IMAGE);
      DiffractometerQxyz op =
      new DiffractometerQxyz(ds1, index, TOF);

      p.append("\nThe results of calling this operator are:\n");
      p.append(op.getResult());
      p.append("\n\nThe results of calling getDocumentation are:\n");
      p.append(op.getDocumentation());

      System.out.print(p.toString());
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

}
