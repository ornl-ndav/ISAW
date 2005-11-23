/*
 * File:  LPSDSensitivity.java 
 *
 * Copyright (C) 2003, Alok Chatterjee, Dennis Mikkelson
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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.4  2005/11/23 19:19:44  hammonds
 * Small edit changes to reduce the difference between LPSDSensitivity and DetectorSensitivity.
 *
 * Revision 1.3  2004/05/10 20:42:21  dennis
 * Test program now just instantiates a ViewManager to diplay
 * calculated DataSet, rather than keeping a reference to it.
 * This removes an Eclipse warning about a local variable that is
 * not read.
 *
 * Revision 1.2  2004/04/22 18:48:33  dennis
 * Replaced num_row, num_cols parameters with min and max group id.
 * This can now be used for either the LPSDs or area detector, by
 * specifying the correct range of group ids.
 * Added main program for testing.
 *
 * Revision 1.1  2004/04/22 17:32:42  dennis
 * Initial Version of sensitivity calculation for SAND LPSDs. (Alok)
 *
 *
 */
package DataSetTools.operator.Generic.TOF_SAD;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.Attribute.*;

import DataSetTools.viewer.*;
import DataSetTools.retriever.*;

import java.util.*;

/** 
 * This operator calculates the sensitivity (with errors) and corresponding
 * mask for the pixels of a collection of detectors.  While it could be used
 * for any type of detector, it should only be applied to DataSets with one
 * type of detector, or the sensitivity data may not be comparable.
 */
public class LPSDSensitivity extends GenericTOF_SAD
{
  private static final String  TITLE = "LPSD Detector Sensitivity";

  /* ------------------------ Default constructor ------------------------- */ 
  /**
   *  Creates operator with title "LPSD Detector Sensitivity" and a default 
   *  list of parameters.
   */  
  public LPSDSensitivity()
  {
    super( TITLE );
  }
  
  /* ---------------------------- constructor ---------------------------- */ 
  /** 
   *  Creates operator with title "LPSD Detector Sensitivity" and the 
   *  specified list of parameters. The getResult method must still be 
   *  used to execute the operator.
   *
   *  @param  ds    DataSet containing flood pattern data from a single area
   *                detector or LPSD.  This is used to find
   *                the sensitivity of individual pixels. 
   *
   *  @param  dead_level  Relative sensitivity, below which a pixel will be 
   *                      discarded as "dead". 
   *
   *  @param  hot_level   Relative sensitivity, above which a pixel will be 
   *                      discarded as "hot", i.e. noisy. 
   *
   *  @param  first_id    The first group id to use.
   *  @param  last_id     The last group id to use.
   */
  public LPSDSensitivity( DataSet  ds, 
                          float    dead_level, 
                          float    hot_level, 
                          int      first_id, 
                          int      last_id )
  {
    this(); 
    parameters = new Vector();
    addParameter( new Parameter("Flood Pattern Histogram", ds) );
    addParameter( new Parameter("Dead pixel threshold", new Float(dead_level)));
    addParameter( new Parameter("Hot pixel threshold", new Float(hot_level)) );
    addParameter( new Parameter("First ID", new Integer(first_id)) );
    addParameter( new Parameter("Last ID", new Integer(last_id)) );
  }

  /* --------------------------- getCommand ------------------------------- */ 
  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return  "LPSDSens", the command used to invoke this 
   *           operator in Scripts
   */
  public String getCommand()
  {
    return "LPSDSens";
  }
  
  /* ------------------------ getDocumentation ---------------------------- */
  /**
   *  Get the documentation to be displayed by the help system.
   */ 
  public String getDocumentation()
  {
    StringBuffer Res = new StringBuffer();
    
    Res.append("@overview This program calculates the sensitivity and " );
    Res.append("mask of pixels to be used, based on a flood fill data set." );
 
    Res.append("@algorithm This program first calculates the average of ");
    Res.append("the total intensities of all pixels.  Any pixels with ");
    Res.append("a total intensity that is too small or too large ");
    Res.append("based on the parameters 'dead_level' and 'hot_level' " );
    Res.append("are omitted from ");
    Res.append("the later calculations.  The average of the total counts ");
    Res.append("for the remaining pixels are calculated.  The relative ");
    Res.append("sensitivity of a pixel is then calculated as the counts in");
    Res.append("the pixel divided by the average counts of the good pixels.");
        
    Res.append("@param ds - DataSet with the flood pattern Data.");
    Res.append("@param dead_level - Relative sensitivity, below which ");
    Res.append(" a pixelwill be discarded as 'dead'.");
    Res.append("@param hot_level - Relative sensitivity, above which a pixel ");
    Res.append(" will be discarded as 'hot', i.e. noisy.");

    Res.append("@param first_id - The first group ID to use ");    
    Res.append("@param last_id - The last group ID to use ");    
    
    Res.append("@return Returns a vector of two DataSets.  The first ");
    Res.append("DataSet contains the pixel sensitivity values, with ");
    Res.append("their errors.  The second DataSet contains the mask values. ");
    
    return Res.toString();
  }


  /* ----------------------- setDefaultParameters ------------------------- */ 
  /** 
   * Sets default values for the parameters.  This must match the data types 
   * of the parameters.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("Flood Pattern Histogram", 
                                 DataSet.EMPTY_DATA_SET) );
    addParameter( new Parameter("Dead pixel threshold", new Float(0.6f)) );
    addParameter( new Parameter("Hot pixel threshold", new Float(1.4f)) );
    addParameter( new Parameter("First Group ID", new Integer(16389)) );
    addParameter( new Parameter("Last Group ID", new Integer(18948)) );
  }
  
  /* ----------------------------- getResult ------------------------------ */ 
  /** 
   *  Calculate the detector pixels' sensitivity using the current parameters.
   *
   *  @return If successful, this operator returns a vector with two DataSets.
   *  The first DataSet contains the pixel sensitivity values, with
   *  their errors.  The second DataSet contains the mask values.
   */
  public Object getResult()
  {
    DataSet ds         = (DataSet)(getParameter(0).getValue());
    float   dead_level = ((Float)(getParameter(1).getValue())).floatValue();
    float   hot_level  = ((Float)(getParameter(2).getValue())).floatValue();
    int     first_id   = ((Integer)(getParameter(3).getValue())).intValue();
    int     last_id    = ((Integer)(getParameter(4).getValue())).intValue();

    //
    // Clone the DataSet, so we don't lose the original data,
    // but only keep the data blocks in the specified range of group IDs. 
    // Also, simplify the attribute lists and operator list.
    //
    DataSet sens_ds = (DataSet)ds.empty_clone();
    sens_ds.removeAllOperators();
    DataSetFactory.addOperators( sens_ds );

    Data  d;
    int   id;
    for ( int i = 0; i < ds.getNum_entries(); i++ )     // copy over the data
    {                                                   // blocks with the 
       d = ds.getData_entry(i);                         // right ids
       id = d.getGroup_ID();
       if ( id >= first_id && id <= last_id )
       { 
         d = (Data)d.clone();
         AttributeList list = new AttributeList();
         list.addAttribute( d.getAttribute( Attribute.DETECTOR_POS ));
         list.addAttribute( d.getAttribute( Attribute.PIXEL_INFO_LIST ));
         d.setAttributeList( list );
         sens_ds.addData_entry( d ); 
       }
    }

    sens_ds.addLog_entry("Set Pixel values to relative sensitivity of pixel");
    sens_ds.setTitle( sens_ds.getTitle() + "Pixel Sensitivity" );
    sens_ds.setY_units("Sensitivity");
    sens_ds.setY_label("Pixel Relative Sensitivity");
  
    //
    // Now rebin the Data down to one bin.  We take a wide enough range to
    // to cover all possible times-of-flight.
    //
    XScale new_scale = new UniformXScale( 0, 33333, 2 );
    for ( int i = 0; i < sens_ds.getNum_entries(); i++ )
      sens_ds.getData_entry(i).resample( new_scale, IData.SMOOTH_NONE );

    // 
    // Next, calculate the total counts and average counts for all pixels 
    //
    double sum = 0;
    for ( int cc = 0; cc < sens_ds.getNum_entries(); cc++ )
      sum += sens_ds.getData_entry(cc).getY_values()[0]; 
    double average = sum / sens_ds.getNum_entries();

    System.out.println();
    System.out.println("LIVE CELLS = " + sens_ds.getNum_entries() );
    System.out.println("NET COUNTS = " + sum ); 
    System.out.println("AVERAGE COUNTS PER CELL = " + average );

    //
    // Calculate the total counts and average counts only using pixels 
    // whose counts are within limits set from the dead_level and hot_level.
    // Mark as selected those Data blocks that were used.  Keep track of the
    // number of dead and hot pixels. 
    //
    double good_counts = 0;
    double counts;  
    int n_dead = 0;
    int n_hot  = 0;
    int n_good = 0;
    for ( int cc = 0; cc < sens_ds.getNum_entries(); cc++ )
       {
        counts = sens_ds.getData_entry(cc).getY_values()[0];
        if ( counts/average <= dead_level )
        {
          n_dead++;
          sens_ds.getData_entry(cc).setSelected(false);
        }  
        else if ( counts/average >= hot_level )
        {
          n_hot++;
          sens_ds.getData_entry(cc).setSelected(false);
        }
        else
        {
          n_good++;
          good_counts += counts;
          sens_ds.getData_entry(cc).setSelected(true);
        }
      }

    double good_average = good_counts/n_good;

    System.out.println();
    System.out.println("DEAD CELLS = " + n_dead );
    System.out.println("HOT CELLS  = " + n_hot );
    System.out.println("LIVE CELLS = " + n_good );
    System.out.println("NET COUNTS = " + good_counts );
    System.out.println("AVERAGE COUNTS PER CELL = " + good_average );
    System.out.println("(AFTER ACCOUNTING FOR DEAD AND HOT CELLS)" );

    //
    // Calculate the sensitivities and errors for the selected cells 
    // and set the others to zero.  Keep track of the min and max 
    // sensitivities.
    //
    float sens[];
    float errors[];
    float min_sens = 1;
    float max_sens = 1;
    for ( int cc = 0; cc < sens_ds.getNum_entries(); cc++ )
      {
        sens   = sens_ds.getData_entry(cc).getY_values();
        errors = new float[1];
        if ( sens_ds.getData_entry(cc).isSelected() )
        {
          errors[0] = (float)(Math.sqrt( sens[0] ) / good_average);
          sens[0]   /= good_average;
          if ( sens[0] > max_sens )
            max_sens = sens[0];
          if ( sens[0] < min_sens )
            min_sens = sens[0];
        }
        else
        {
          errors[0] = 0;
          sens[0]   = 0;
        }
        ((TabulatedData)sens_ds.getData_entry(cc)).setErrors( errors );
      }

    System.out.println();
    System.out.print  ("THE MINIMUM AND MAXIMUM VALUES IN THE DETECTOR " );
    System.out.println("SENSITIVITY ARRAY ARE, RESPECTIVELY : ");
    System.out.println("MIN = " + min_sens ); 
    System.out.println("MAX = " + max_sens ); 

    sens_ds.addOperator( new GetPixelInfo_op() );

    sens_ds.clearSelections();
    Vector result = new Vector(1);
    result.addElement( sens_ds );

    return result;
  }

  
  /* ------------------------------- main --------------------------------- */ 
  /** 
   * Test program to verify that this will complile and run ok.  
   *
   */
  public static void main( String args[] )
  {
     String file_name;
     if ( args.length > 0 )
       file_name = args[0];
     else
       file_name = "/usr2/ARGONNE_DATA/SAND_LPSD_RUNS/sand22403.run";

     RunfileRetriever rr = new RunfileRetriever( file_name );
    
     DataSet ds = rr.getDataSet(1);
     
     Operator op = new LPSDSensitivity( ds, 0.6f, 1.4f, 0, 16388 );
     Vector result = (Vector)op.getResult();
     DataSet area_sens = (DataSet)result.elementAt(0);
     new ViewManager( area_sens, IViewManager.THREE_D );
     
     op = new LPSDSensitivity( ds, 0.6f, 1.4f, 16389, 18948 );
     result = (Vector)op.getResult();
     DataSet lpsd_sens = (DataSet)result.elementAt(0);
     new ViewManager( lpsd_sens, IViewManager.THREE_D );
  }
}
