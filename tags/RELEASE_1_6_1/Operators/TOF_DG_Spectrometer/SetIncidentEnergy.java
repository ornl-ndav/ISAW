/*
 * File:  SetIncident.java 
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
 * Revision 1.4  2003/02/04 21:37:16  dennis
 * Added getDocumentation() method. (Chris Bouzek)
 *
 * Revision 1.3  2002/11/27 23:30:33  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/06/03 22:34:28  dennis
 * tof_calc.NewEnergyInData(,) is now used to adjust the spectra
 * for a TOF_DG_Spectrometer, to the incident energy calculated from the
 * beam monitors.
 *
 * Revision 1.1  2002/03/18 21:42:55  dennis
 * Operator to adjust the time bin boundaries for a new incident
 * energy value.
 *
 *
 */
package Operators.TOF_DG_Spectrometer;

import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;
import DataSetTools.math.*;
import DataSetTools.util.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.TOF_DG_Spectrometer.*;
import java.util.*;
import java.io.*;

/** 
 *  This operator sets a new incident energy value for a time-of-flight
 *  DataSet for a direct geometry spectrometer.  Each time bin boundary for
 *  each spectrum in the DataSet is adjusted based on the new incident energy
 *  specified.  The algorithm is as follows:
 *
 *  1. The current incident energy and initial flight path is used to 
 *     recalculate the source to sample time that was originally used, t_old.
 *
 *  2. The new incident energy and initial flight path is used to 
 *     calculate a new source to sample time, t_new.
 *
 *  3. The difference (t_new - t_old) is subtracted from each bin boundary
 *     of each spectrum.
 *
 *  4. The new incident energy is recorded in each Data block as the incident 
 *     energy.
 */
public class SetIncidentEnergy extends    GenericTOF_DG_Spectrometer
                               implements Serializable
{
  private static final String TITLE = "Set Spectrometer Incident Energy";

 /* ------------------------ Default constructor ------------------------- */ 
 /** 
  */  
  public SetIncidentEnergy()
  {
    super( TITLE );
  }

 /* ---------------------------- Constructor ----------------------------- */ 
 /** 
  *  @param  ds           DataSet to do the flight path correction for. 
  *  @param  new_e_in     New value to use for the incident energy.
  *  @param  make_new_ds  Flag that determines whether a new DataSet is
  *                       constructed, or the Data blocks of the original
  *                       DataSet are just altered.
  */
  public SetIncidentEnergy( DataSet ds, float new_e_in, boolean make_new_ds )
  {
    this(); 
    parameters = new Vector();
    addParameter( new Parameter("Spectrometer TOF DataSet", ds) );
    addParameter( new Parameter("New incident energy", new Float(new_e_in)) );
    addParameter( new Parameter( "Create new DataSet?", 
                                  new Boolean(make_new_ds) ) );
  }

 /* ---------------------------- getCommand ------------------------------- */ 
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "SetEin", the command used to invoke this 
  *           operator in Scripts
  */
  public String getCommand()
  {
    return "SetEin";
  }

 /* ------------------------ setDefaultParameters ------------------------- */ 
 /** 
  * Sets default values for the parameters.  This must match the data types 
  * of the parameters.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("Spectrometer TOF DataSet", 
                                 DataSet.EMPTY_DATA_SET)   );
    addParameter( new Parameter( "New incident energy", new Float(100)) );
    addParameter( new Parameter( "Create new DataSet?", new Boolean(false) ) );
  }

 /* ---------------------- getDocumentation --------------------------- */
 /**
 *  Returns the documentation for this method as a String.  The format
 *  follows standard JavaDoc conventions.
 */
 public String getDocumentation()
 {
   StringBuffer s = new StringBuffer("");
   s.append("@overview This operator sets a new incident energy value for ");
   s.append("a time-of-flight DataSet for a direct geometry spectrometer.  ");
   s.append("Each time bin boundary for each spectrum in the DataSet is ");
   s.append("adjusted based on the new incident energy specified. \n");
   s.append("@assumptions The input DataSet's X units must be time values.\n");
   s.append("The DataSet must not be empty or null.\n");
   s.append("The new incident energy must be valid.\n");
   s.append("@algorithm First the current incident energy and initial ");
   s.append("flight path is used to recalculate the source to sample time ");
   s.append("that was originally used (t_old).\n");
   s.append("Then the new incident energy and initial flight path is used ");
   s.append("to calculate a new source to sample time (t_new).\n");
   s.append("The difference (t_new - t_old) is then subtracted from each ");
   s.append("bin boundary of each spectrum.\n");
   s.append("Finally the new incident energy is recorded in each Data ");
   s.append("block as the incident energy, and an entry is appended to the ");
   s.append("DataSet's log indicating that the incident energy was set to ");
   s.append("the new incident energy value.\n");
   s.append("@param ds DataSet to do the flight path correction for.\n"); 
   s.append("@param new_e_in New value to use for the incident energy.\n");
   s.append("@param make_new_ds Flag that determines whether a new DataSet ");
   s.append("is constructed, or the Data blocks of the original DataSet ");
   s.append("are just altered.\n");
   s.append("@return If make_new_ds is true, this returns a new DataSet ");
   s.append("with the new ENERGY_IN and adjusted time channels, otherwise ");
   s.append("it just returns the same DataSet with new ENERGY_IN and ");
   s.append("adjusted time channels.\n");
   s.append("@error Returns an error if the input DataSet is null.\n");
   s.append("@error Returns an error if the input DataSet's X units are not ");
   s.append("in time values\n");
   s.append("@error Returns an error if the DataSet is empty.\n");
   s.append("@error Returns an error if the new incident energy is invalid.\n");
   return s.toString();
 }
  
 /* ----------------------------- getResult ------------------------------ */ 
 /** 
  * 
  *  Set the ENERGY_IN attribute for this DataSet and adjust the time channels 
  *  of each spectrum to correspond to the new ENERGY_IN value.  
  *
  *  @return  If make_new_ds is true, this returns a new DataSet with the new 
  *           ENERGY_IN and adjusted time channels, otherwise it just returns 
  *           the same DataSet with new ENERGY_IN and adjusted time channels.
  */
  public Object getResult()
  {
    DataSet ds          = (DataSet)(getParameter(0).getValue());
    float   new_e_in    = ((Float)getParameter(1).getValue()).floatValue();
    boolean make_new_ds = ((Boolean)getParameter(2).getValue()).booleanValue();
 
                                              // check for proper DataSet
    if ( ds == null )
      return new ErrorString("DataSet is null in SetIncidentEnergy");

    if ( !ds.getX_units().equalsIgnoreCase( "Time(us)" ) )
      return new ErrorString(
                        "DataSet X units not Time(us) in SetIncidentEnergy");

    if ( ds.getNum_entries() <= 0 )
      return new ErrorString("DataSet empty in SetIncidentEnergy");

    if ( new_e_in <= 0 )
      return new ErrorString(
               "New incident energy invalid in SetIncidentEnergy "+new_e_in );

    DataSet new_ds;
    if ( make_new_ds )
      new_ds = (DataSet)ds.clone();
    else
      new_ds = ds;
                                          // for each spectrum, modify each
                                          // time channel to compensate for the
                                          // new incident energy 
    for ( int i = 0; i < ds.getNum_entries(); i++ ) 
    {
      Data d = ds.getData_entry( i );
      d = tof_data_calc.NewEnergyInData( (TabulatedData)d, new_e_in );
      new_ds.replaceData_entry( d, i );   
    }

    new_ds.addLog_entry("Set Incident Energy to " + new_e_in );
    return new_ds; 
  }

 /* ------------------------------- clone -------------------------------- */ 
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    Operator op = new SetIncidentEnergy();
    op.CopyParametersFrom( this );
    return op;
  }

 /* ------------------------------- main --------------------------------- */ 
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {
     System.out.println( "Test of SetIncidentEnergy starting...");
                                                               // load a DataSet
     //String filename = "/usr/local/ARGONNE_DATA/hrcsdata/hrcs3005.run";
     String filename = "D:\\ISAW\\SampleRuns\\\\hrcs2955.run";
     RunfileRetriever rr = new RunfileRetriever( filename );
     DataSet ds = rr.getDataSet(1);
                                                               // make operator
                                                               // and call it
     SetIncidentEnergy op = new SetIncidentEnergy( ds, 100, true );
     Object obj = op.getResult();
     
     /*--- added by Chris Bouzek ----*/
     System.out.println("Documentation: " + op.getDocumentation());
     /*------------------------------*/
     
     if ( obj instanceof DataSet )                   // we got a DataSet back
     {                                               // so show it and original
       DataSet new_ds = (DataSet)obj;
       ViewManager vm1 = new ViewManager( ds,     IViewManager.IMAGE );
       ViewManager vm2 = new ViewManager( new_ds, IViewManager.IMAGE );
     }
     else 
       System.out.println( "Operator returned " + obj );

     System.out.println("Test of SetIncidentEnergy done.");
  }
}
