/*
 * File:  EnergyFromMonitorDS.java
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
 * Revision 1.3  2003/01/13 17:13:49  dennis
 * Added getDocumentation(), main test program and javadocs for getResult.
 * (Chris Bouzek)
 *
 * Revision 1.2  2002/11/27 23:19:19  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/02/22 21:03:41  pfpeterson
 * Operator reorganization.
 *
 */

package DataSetTools.operator.DataSet.Special;

import  java.io.*;
import  java.util.Vector;
import  DataSetTools.dataset.*;
import  DataSetTools.util.*;
import  DataSetTools.math.*;
import  DataSetTools.operator.Parameter;
import  DataSetTools.retriever.*;

/**
  *  This operator calculates the incident energy of a neutron beam for a
  *  chopper spectrometer given a DataSet containing the Data blocks from
  *  two beam monitors.
  */

public class  EnergyFromMonitorDS  extends    DS_Special
                                   implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.
   */

  public EnergyFromMonitorDS( )
  {
    super( "Calculate Energy in using two monitors" );
  }

  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for a specified DataSet and with the specified
   *  parameter values so that the operation can be invoked immediately
   *  by calling getResult().
   *
   *  @param  ds    The monitor DataSet used for the energy calculation.
   */

  public EnergyFromMonitorDS( DataSet ds )
  {
    this();
    setDataSet( ds );               // record reference to the DataSet that
                                    // this operator should operate on
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return  the command name to be used with script processor:
   *          in this case, Emon
   */
   public String getCommand()
   {
     return "Emon";
   }


 /* -------------------------- setDefaultParameters ------------------------- */
 /**
  *  Set the parameters to default values.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters
  }

  /* ---------------------- getDocumentation --------------------------- */
  /**
   *  Returns the documentation for this method as a String.  The format
   *  follows standard JavaDoc conventions.
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview This operator calculates the incident energy of a ");
    s.append("neutron beam for a chopper spectrometer given a DataSet ");
    s.append("containing the Data blocks from two beam monitors.\n");
    s.append("@assumptions It is assumed that the input DataSet has pulse ");
    s.append("data from two beam monitors.\n");
    s.append("@algorithm Uses the EnergyFromMonitorData method from the ");
    s.append("DataSetTools math library to calculate the incident energy ");
    s.append("of a neutron beam based on the pulse data from the two beam");
    s.append("monitors contained in the input DataSet.\n");
    s.append("@param ds The monitor DataSet used for the energy calculation.\n");
    s.append("@return Float object which represents the incident energy.\n");
    s.append("@error Returns an error if the required two monitor Data blocks ");
    s.append("are not available.\n");
    return s.toString();
  }

  /* ---------------------------- getResult ------------------------------- */
  /**
   *  Calculates the incident energy of a neutron beam based on the pulse
   *  data from the two beam monitors contained in the input DataSet.
   *
   *  @return Float object which represents the incident energy.
   */
  public Object getResult()
  {
                                     // get the current data set and do the
                                     // operation
    DataSet ds = this.getDataSet();

    Data mon_1 = ds.getData_entry(0);
    Data mon_2 = ds.getData_entry(1);

    if ( mon_1 == null || mon_2 == null )
    {
      ErrorString message = new ErrorString(
                           "ERROR: Two monitor Data block are needed" );
      System.out.println( message );
      return message;
    }
    else
    {
      float result = tof_data_calc.EnergyFromMonitorData( mon_1, mon_2 );
      return new Float( result );
    }
  }

  /* ------------------------------ clone ------------------------------- */
  /**
   * Get a copy of the current EnergyFromMonitorDS Operator.  The list of
   * parameters and the reference to the DataSet to which it applies are
   * also copied.
   */
  public Object clone()
  {
    EnergyFromMonitorDS new_op = new EnergyFromMonitorDS( );
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
  StringBuffer m = new StringBuffer();
  String file_name = "/home/groups/SCD_PROJECT/SampleRuns/hrcs2955.run";
                      //"D:\\ISAW\\SampleRuns\\hrcs2955.run";
  try
  {
    RunfileRetriever rr = new RunfileRetriever( file_name );
    DataSet ds1 = rr.getDataSet(1);
      EnergyFromMonitorDS op = new EnergyFromMonitorDS(ds1);

    m.append("\nThe results of calling getResult() for ");
    m.append("EnergyFromMonitorDS are:\n\n");
    m.append(op.getResult().toString());
    m.append("\n\n");

    m.append("\nThe results of calling getDocumentation() for ");
    m.append("EnergyFromMonitorDS are:\n\n");
    m.append(op.getDocumentation());
    System.out.print(m.toString());
  }
  catch(Exception e)
  {
   e.printStackTrace();
  }
 }

}
