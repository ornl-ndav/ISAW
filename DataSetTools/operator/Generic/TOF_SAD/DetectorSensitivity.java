/*
 * File:  DetectorSensitivity.java 
 *
 * Copyright (C) 2003, Dennis Mikkelson 
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.2  2003/07/07 20:42:05  dennis
 * Now returns an array with two DataSets.  The first DataSet stores
 * the detector efficiencies and the second DataSet stores the mask
 * indicating which pixels were used.  Following the conventions of
 * the existing SAND data reduction, the efficiencies of pixels that
 * are either too "hot" or "dead" are set to zero.  Consequently,
 * the mask DataSet is redundant and may be removed from later versions.
 * Also, this operator will be altered to return a Vector of DataSets,
 * when the main Isaw program is modified to place DataSets from a
 * vector of Objects in the tree.
 *
 * Revision 1.1  2003/07/05 22:11:23  dennis
 * Calculate the efficiences of pixels in a detector, for SAD data
 * reduction.
 *
 */
package DataSetTools.operator.Generic.TOF_SAD;

import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import DataSetTools.instruments.*;
import DataSetTools.util.*;
import DataSetTools.retriever.*;
import java.util.*;
import java.util.*;

/** 
 * This operator calculates the sensitivity (with errors) and corresponding
 * mask for a single area detector or LPSD.  It implements the concepts from
 * the FORTRAN program:
 *   
 *   areadetsens_v3
 *
 * developed by the small angle group at the Intense Pulsed Neutron Source
 * division at ArgonneNationalLaboratory.
 */
public class DetectorSensitivity extends GenericTOF_SAD
{
  private static final String  TITLE = "Area Detector Sensitivity";

  /* ------------------------ Default constructor ------------------------- */ 
  /**
   *  Creates operator with title "Area Detector Sensitivity" and a default 
   *  list of parameters.
   */  
  public DetectorSensitivity()
  {
    super( TITLE );
  }
  
  /* ---------------------------- constructor ---------------------------- */ 
  /** 
   *  Creates operator with title "Area Detector Sensitivity" and the 
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
   */
  public DetectorSensitivity( DataSet ds, float dead_level, float hot_level )
  {
    this(); 
    parameters = new Vector();
    addParameter( new Parameter("Flood Pattern Histogram", ds) );
    addParameter( new Parameter("Dead pixel threshold", new Float(dead_level)));
    addParameter( new Parameter("Hot pixel threshold", new Float(hot_level)) );
  }
  
  /* --------------------------- getCommand ------------------------------- */ 
  /** 
   * Get the name of this operator to use in scripts
   * 
   * @return  "DetSens", the command used to invoke this 
   *           operator in Scripts
   */
  public String getCommand()
  {
    return "DetSens";
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
    Res.append("efficiency of a pixel is then calculated as the counts in");
    Res.append("the pixel divided by the average counts of the good pixels.");
        
    Res.append("@param ds - DataSet with the flood pattern Data.");
    Res.append("@param dead_level - Relative sensitivity, below which ");
    Res.append(" a pixelwill be discarded as 'dead'.");
    Res.append("@param hot_level - Relative sensitivity, above which a pixel ");
    Res.append(" will be discarded as 'hot', i.e. noisy.");
    
    Res.append("@return Returns a vector of two DataSets.  The first ");
    Res.append("DataSet contains the detector efficiency values, with ");
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
  }
  
  /* ----------------------------- getResult ------------------------------ */ 
  /** 
   *  Calculate the detector sensitivity using the current parameters.
   *
   *  @return If successful, this operator returns a vector with two DataSets.
   *  The first DataSet contains the detector efficiency values, with
   *  their errors.  The second DataSet contains the mask values.
   */
  public Object getResult()
  {
    System.out.println("Starting getResult()..." );

    DataSet ds         = (DataSet)(getParameter(0).getValue());
    float   dead_level = ((Float)(getParameter(1).getValue())).floatValue();
    float   hot_level  = ((Float)(getParameter(2).getValue())).floatValue();

    //
    // Find the DataGrid for this detector and make sure that we have a 
    // segmented detector. 
    //
    DataSet eff_ds = (DataSet)ds.clone();
    UniformGrid grid = null;
    Data d = null;
    PixelInfoList pil;
    Attribute attr;
    boolean segmented_detector_found  = false;
    int data_index = 0;
    int n_data     = eff_ds.getNum_entries();

    while ( !segmented_detector_found && data_index < n_data )
    {
     d = eff_ds.getData_entry( data_index );
     attr = d.getAttribute( Attribute.PIXEL_INFO_LIST );
     if ( attr != null && attr instanceof PixelInfoListAttribute )
     {
        pil  = (PixelInfoList)attr.getValue();
        grid = (UniformGrid)pil.pixel(0).DataGrid();
        if ( grid.num_rows() > 1 || grid.num_cols() > 1 )
          segmented_detector_found = true;
      }
      else
        return new ErrorString("Need PixelInfoList attribute.");

      data_index++;
    }

    if ( !segmented_detector_found )
      return new ErrorString("Need and Area Detector or LPSD");

    //
    // Throw out any Data blocks that don't belong with this detector
    //
    if ( !grid.setData_entries( eff_ds ) )
      return new ErrorString("Can't set Data grid entries"); 

    for ( int i = 0; i < eff_ds.getNum_entries(); i++ )
      eff_ds.getData_entry(i).setSelected( true );

    for ( int row = 1; row <= grid.num_rows(); row++ )
      for ( int col = 1; col <= grid.num_cols(); col++ )
      {
        if ( grid.getData_entry( row, col ) == null )
          System.out.println("NULL at : " + row + ", " + col );
        else
          grid.getData_entry( row, col ).setSelected( false );
      }

    eff_ds.removeSelected( true );

    //
    // Remove specialty operators and un-needed attributes.  Fix up the
    // title, labels and units.
    //
    eff_ds.removeAllOperators();
    DataSetFactory.addOperators( eff_ds );
    for ( int row = 1; row <= grid.num_rows(); row++ )
      for ( int col = 1; col <= grid.num_cols(); col++ )
      {
        d = grid.getData_entry( row, col );
        AttributeList list = new AttributeList();
        list.addAttribute( d.getAttribute( Attribute.DETECTOR_POS )); 
        list.addAttribute( d.getAttribute( Attribute.PIXEL_INFO_LIST )); 
        d.setAttributeList( list );
      }
    eff_ds.setTitle( eff_ds.getTitle() + " Detector Efficiency" );
    eff_ds.setX_units("number");
    eff_ds.setX_label("Detector");
    eff_ds.setY_units("number");
    eff_ds.setY_label("Relative Efficiency");
  
    //
    // Now rebin the Data down to one bin.  We assume there is only one 
    // x_scale for the Data blocks from this detector.
    //
    XScale old_scale = eff_ds.getData_entry(0).getX_scale();
    XScale new_scale = new UniformXScale( old_scale.getStart_x(),
                                          old_scale.getEnd_x(),
                                          2 );
    for ( int i = 0; i < eff_ds.getNum_entries(); i++ )
      eff_ds.getData_entry(i).resample( new_scale, IData.SMOOTH_NONE );

    System.out.println("Resampled entries..." );
    
    // 
    // Next, calculate the total counts and average counts for all pixels 
    //
    double sum = 0;
    for ( int row = 1; row <= grid.num_rows(); row++ )
      for ( int col = 1; col <= grid.num_cols(); col++ )
        sum += grid.getData_entry(row,col).getY_values()[0];

    double average = sum / (grid.num_rows()*grid.num_cols());

    System.out.println();
    System.out.println("LIVE CELLS = " + grid.num_rows()*grid.num_cols() );
    System.out.println("NET COUNTS = " + sum ); 
    System.out.println("AVERAGE COUNTS PER CELL = " + average );

    //
    // Calculate the total counts and average counts only using pixels 
    // whose counts are within limits set from the dead_level and hot_level.
    // Mark a selected those Data blocks that were used.  Keep track of the
    // number of dead and hot pixels. 
    //
    double good_counts = 0;
    double counts;  
    int n_dead = 0;
    int n_hot  = 0;
    int n_good = 0;
    for ( int row = 1; row <= grid.num_rows(); row++ )
      for ( int col = 1; col <= grid.num_cols(); col++ )
      {
        counts = grid.getData_entry(row,col).getY_values()[0];
        if ( counts/average <= dead_level )
        {
          n_dead++;
          grid.getData_entry(row,col).setSelected(false);
        }  
        else if ( counts/average >= hot_level )
        {
          n_hot++;
          grid.getData_entry(row,col).setSelected(false);
        }
        else
        {
          n_good++;
          good_counts += counts;
          grid.getData_entry(row,col).setSelected(true);
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
    // Calculate the efficiencies and errors for the selected cells 
    // and set the others to zero.  Keep track of the min and max 
    // sensitivities.
    //
    float eff[];
    float errors[];
    float min_sens = 1;
    float max_sens = 1;
    for ( int row = 1; row <= grid.num_rows(); row++ )
      for ( int col = 1; col <= grid.num_cols(); col++ )
      {
        eff    = grid.getData_entry(row,col).getY_values();
        errors = new float[1];
        if ( grid.getData_entry(row,col).isSelected() )
        {
          errors[0] = (float)(Math.sqrt( eff[0] ) / good_average);
          eff[0]   /= good_average;
          if ( eff[0] > max_sens )
            max_sens = eff[0];
          if ( eff[0] < min_sens )
            min_sens = eff[0];
        }
        else
        {
          errors[0] = 0;
          eff[0]    = 0;
        }
        ((TabulatedData)grid.getData_entry(row,col)).setErrors( errors );
      }

    System.out.println();
    System.out.print  ("THE MINIMUM AND MAXIMUM VALUES IN THE DETECTOR " );
    System.out.println("SENSITIVITY ARRAY ARE, RESPECTIVELY : ");
    System.out.println("MIN = " + min_sens ); 
    System.out.println("MAX = " + max_sens ); 
 
    // 
    // Now make the "Mask" DataSet
    //
    DataSetFactory ds_f = new DataSetFactory( "Pixel Mask",
                                              "number",
                                              "Mask",
                                              "number",
                                              "Mask Value"      );
    DataSet    mask_ds = ds_f.getDataSet();
    XScale     scale = new UniformXScale(0,0,1);
    float      y[];
    int        id;
    Data       new_d;
    IPixelInfo list[];
    UniformGrid mask_grid = new UniformGrid( grid, false );
    for ( short row = 1; row <= mask_grid.num_rows(); row++ )
      for ( short col = 1; col <= mask_grid.num_cols(); col++ )
      {
        d = grid.getData_entry(row,col);               // efficiency Data

        y = new float[1];
        if ( d.isSelected() )
          y[0] = 1;
        else
          y[0] = 0;
  
        id = d.getGroup_ID();
        new_d = new FunctionTable( scale, y, id );

        new_d.setAttribute( d.getAttribute( Attribute.DETECTOR_POS ));

        list = new IPixelInfo[1];
        list[0] = new DetectorPixelInfo( id, row, col, mask_grid );
        pil = new PixelInfoList( list );
        attr = new PixelInfoListAttribute( Attribute.PIXEL_INFO_LIST, pil );
        new_d.setAttribute( attr );

        mask_ds.addData_entry( new_d );
      }

    System.out.println("Grid = " + mask_grid );
    System.out.println("Mask Grid = " + mask_grid );

    if ( !mask_grid.setData_entries( mask_ds ) )
      return new ErrorString("Can't set the Mask grid entries");
/*    
    Vector result = new Vector(2);
    result.addElement( eff_ds );
*/ 
    DataSet result[] = new DataSet[2];
    result[0] = eff_ds;
    result[1] = mask_ds;
  
    return result;
  }

  /* ------------------------------- clone -------------------------------- */ 
  /** 
   *  Creates a clone of this operator.
   */
  public Object clone()
  {
    Operator op = new DetectorSensitivity();
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
    
  }
}
