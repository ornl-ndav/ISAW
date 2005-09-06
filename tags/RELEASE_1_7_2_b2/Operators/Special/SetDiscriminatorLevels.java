/*
 * File:  SetDiscriminatorLevels.java 
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 * Revision 1.4  2005/08/25 14:51:38  dennis
 * Made/added to category DATA_SET_ANALYZE_MACROS.
 *
 * Revision 1.3  2004/06/02 15:44:16  dennis
 * Fixed error in assigning values to parameters in the constructor
 * that accepts parameters.
 *
 * Revision 1.2  2004/05/10 20:42:29  dennis
 * Test program now just instantiates a ViewManager to diplay
 * calculated DataSet, rather than keeping a reference to it.
 * This removes an Eclipse warning about a local variable that is
 * not read.
 *
 * Revision 1.1  2004/05/07 17:57:03  dennis
 * Moved operators that extend GenericSpecial from Operators
 * to Operators/Special
 *
 * Revision 1.11  2004/03/19 17:10:54  dennis
 * Removed unused variables
 *
 * Revision 1.10  2004/03/15 19:36:53  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.9  2004/03/15 03:36:59  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.8  2004/02/04 03:17:42  hammonds
 * Generalize by adding  max_chn.
 * Add IntAttributes for Upper & Lower Discriminator to allow 
 * extraction to file.
 * Adjust marker height to match each spectrum
 *
 * Revision 1.7  2003/02/10 18:51:23  dennis
 * Added getDocumentation() method. (Josh Olson)
 *
 * Revision 1.6  2002/11/27 23:29:54  pfpeterson
 * standardized header
 *
 * Revision 1.5  2002/02/22 20:45:06  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.4  2001/11/27 18:21:54  dennis
 * Added operator title to constructor java docs.
 *
 */
package Operators.Special;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.peak.*;

import java.util.*;

/** 
 */
public class SetDiscriminatorLevels extends GenericSpecial
{

  private static final String TITLE = "Set Discriminator Levels";


 /* ----------------------------- constructor ----------------------------- */
 /** 
  *  Creates operator with title "Set Discriminator Levels" and a 
  *  default list of parameters. 
  */  
  public SetDiscriminatorLevels()
  {
    super( TITLE );
  }


 /* ----------------------------- constructor ----------------------------- */
 /** 
  *  Creates operator with title "Set Discriminator Levels" and the  
  *  specified list of parameters.  The getResult method must still be 
  *  used to execute the operator.
  *
  *  @param  p_ds        The pulse height DataSet.
  *  @param  lower_frac  The fraction of the the pulse position to set for
  *                      the lower threshold. 
  *  @param  upper_frac  The fraction of the pulse amplitude to set for the
  *                      upper threshold.
  *  @param  width       Number of channels over which the peak will be fit.
  */
  public SetDiscriminatorLevels( DataSet p_ds, 
                                 float   lower_frac,
                                 float   upper_frac,
                                 float   max_chn,
                                 float   width )
  {
    super( TITLE );
    parameters = new Vector();
    addParameter( new Parameter( "Pulse Height Data Set", p_ds) );
    addParameter( new Parameter( "lower (fraction of peak position)", 
                                  new Float(lower_frac) ) );
    addParameter( new Parameter( "upper (fraction of peak amplitude)", 
                                  new Float(upper_frac) ) );
    addParameter( new Parameter( "maximum channel", 
                                  new Float(max_chn) ) );
    addParameter( new Parameter( "width of interval for fit", 
                                  new Float(width) ) );
  }


 /* ----------------------------- getCommand ----------------------------- */
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "SetDisc", the command used to invoke this operator in Scripts
  */
  public String getCommand()
  {
    return "SetDisc";
  }


 /* ---------------------------- getCategoryList -------------------------- */
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

 /* --------------------------- setDefaultParameters ---------------------- */
 /** 
  * Sets default values for the parameters.  This must match the data types 
  * of the parameters.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter( "Pulse Height Data Set", 
                                  new DataSet("","") ));
    addParameter( new Parameter( "lower (fraction of peak position)", 
                                  new Float(0.25f) ) );
    addParameter( new Parameter( "upper (fraction of peak amplitude)",
                                  new Float(1.0e-8) ) );
    addParameter( new Parameter( "maximum channel", 
                                  new Float(255.0) ) );
    addParameter( new Parameter( "width of interval for fit", 
                                  new Float(40) ) );
  }
                                                                              
 /* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */                                                                                    
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");                                                 
    s.append("@overview Creates operator with title 'Set Discriminator ");
    s.append("Levels' and the specified list of parameters. \n"); 
    s.append("NOTE: This is a \"work in progress\". ");
    s.append("@assumptions The parameters will accurately meet the ");  
    s.append("description given in the 'Parameters' section. ");

    s.append("@algorithm The title of 'p_ds' is stored.  \n\n ");
    s.append("An array of two DataSet objects is declared.  The first ");
    s.append("DataSet in this array is set to be a clone of 'p_ds'.  The ");
    s.append("second is set to be the result of fitting the first one's ");
    s.append("Gaussian distribution to the major peaks in the data blocks & ");
    s.append("marking recommended upper and lower discriminator levels.  (The");
    s.append(" viewer corresponding to the second DataSet should have a ");
    s.append("column of assorted white dots on the left and right side of ");
    s.append("the image.  These are the lower and upper discriminator ");
    s.append("levels.) \n\n ");                                                                  	       
    s.append("The title of the first DataSet is set to be four separate ");
    s.append("things combined into one string.  These four things are: \n ");
    s.append("a) the title of 'p_ds' \n");
    s.append("b) the string  \"_Levels( \" \n");  
    s.append("c) 'lower_frac' \n");
    s.append("d) 'width' \n\n");
    s.append("The title of the second DataSet is set to be the same four  ");
    s.append("things, except \"_Gaussians( \" replaces \"_Levels( \". \n\n");       
    s.append("The array containing the two DataSets is returned. ");  
    s.append("@param p_ds The pulse height DataSet");
    s.append("@param lower_frac The fraction of the the pulse position to ");
    s.append("set for the lower threshold");
    s.append("@param upper_frac The fraction of the pulse amplitude to set ");
    s.append("for the upper threshold");
    s.append("@param max_chn maximum channel to look for the peak");
    s.append("@param width Number of channels over which the peak will be ");
    s.append("fit.");
    s.append("@return If successful, this returns an array of two DataSets.");
    s.append("\nThe first is a clone of the original DataSet 'p_ds'. The ");
    s.append("second is the result of fitting the first one's Gaussian ");
    s.append("distribution to the major peaks in the data blocks & marking ");
    s.append("recommended upper and lower discriminator levels.");
    return s.toString();
  } 									        


 /* ----------------------------- getResult ------------------------------- */
 /** 
  *  Executes this operator using the values of the current parameters.
  *
  *  @return  If successful, this returns an array of two DataSets.  The first 
  *           is a clone of the original DataSet 'p_ds'. The second is the 
  *	      result of fitting the first one's Gaussian distribution to the 
  *	      major peaks in the data blocks & marking recommended upper and 
  *	      lower discriminator levels.  
  */
  public Object getResult()
  {
    DataSet p_ds = (DataSet)(getParameter(0).getValue()); 
    float lower_frac = ( (Float)(getParameter(1).getValue()) ).floatValue(); 
    float upper_frac = ( (Float)(getParameter(2).getValue()) ).floatValue(); 
    float max_chn      = ( (Float)(getParameter(3).getValue()) ).floatValue(); 
    float width      = ( (Float)(getParameter(4).getValue()) ).floatValue(); 

    String title = p_ds.getTitle();

    DataSet result_ds[] = new DataSet[2];
    result_ds[0] = (DataSet)p_ds.clone();
    result_ds[1] = FitPeaks( result_ds[0], lower_frac, upper_frac, max_chn, 
			     width );
                      
    result_ds[0].setTitle( title + "_Levels( "+lower_frac+", "+width+")" );
    result_ds[1].setTitle( title + "_Gaussians( "+lower_frac+", "+width+")" );
    return result_ds;
  }


 /* ------------------------------ clone ------------------------------- */
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    SetDiscriminatorLevels sl = new SetDiscriminatorLevels();
    sl.CopyParametersFrom( this );
    return sl;
  }

 /* ------------------------------------------------------------------------
  *
  *  PRIVATE METHODS
  *
  */

  private DataSet FitPeaks( DataSet p_ds, 
                            float   lower_frac, 
                            float   upper_frac, 
                            float   max_chn, 
                            float   peak_width )  
  {
    Data         data,
                 peak_data;
    XScale       x_scale;

    GaussianPeak   peak   = new GaussianPeak( 0, 1, 1, 0, 0 );
    DataSet        new_ds = p_ds.empty_clone();
    int            n_data = p_ds.getNum_entries();
    int            lower,
                   upper;
    int            lower_list[],        // record the discriminator levels in
                   upper_list[];        // arrays that could be written out
    float          delta  = 
             (float) Math.sqrt( -2 * Math.log(Math.sqrt(2*Math.PI)*upper_frac));

    lower_list = new int[ n_data ];
    upper_list = new int[ n_data ];
    for ( int i = 0; i < n_data; i++ )
    {
      float x1 = 50;
      float x2 = max_chn;
      float width = peak_width/2;

      data = p_ds.getData_entry(i);

      int count = 0;
      boolean done   = false;
      boolean fit_ok = false;
      float last_position = -1000;
      float position = 0;
      while ( count < peak_width && !done && !fit_ok )
      {
        if ( !peak.FitPeakToData( data, x1, x2 ) )
          done = true;
        else
        {
          position = peak.getPosition();
          if ( Math.abs( position - last_position ) < 0.1 )
            fit_ok = true;
          else
          {
            width = peak.getFWHM(); 
            if ( width > peak_width/2 )
              width = peak_width/2;
            x1 = peak.getPosition() - 1.1f * width;
            x2 = peak.getPosition() + 1.1f * width;
            last_position = position;
            count++;
          }
        }
      }
      x_scale = data.getX_scale();
      peak.setEvaluationMode( IPeak.PEAK_ONLY );
      peak_data = peak.PeakData( x_scale );
      peak_data.setGroup_ID( data.getGroup_ID() );
      new_ds.addData_entry( peak_data );
      if ( !fit_ok )
      {
        float y_vals[] = peak_data.getY_values();
        for ( int j = 0; j < y_vals.length; j++ )
          y_vals[j] = 0;

        lower_list[i] = 0;
        upper_list[i] = y_vals.length-1;
      }
      else
      {
        lower = (int)( lower_frac * peak.getPosition() );
        upper = (int)( peak.getPosition() + delta * peak.getSigma() );
        
        lower_list[i] = lower;
        upper_list[i] = upper;

        float y_vals[] = data.getY_values();
        if ( lower < 0 ) 
          lower = 0;
        if ( lower >= y_vals.length )
          lower = y_vals.length - 1;
        y_vals[lower] = y_vals[(int)peak.getPosition()] * 2;

        if ( upper < 0 )
          upper = 0;
        if ( upper >= y_vals.length )
          upper = y_vals.length - 1;
        y_vals[upper] = y_vals[(int)peak.getPosition()] * 2;
	/*        y_vals[upper] = range.getEnd_x();*/
      }
    }

    for ( int i = 0; i < lower_list.length; i++ ) {
      System.out.println("i, lower, upper = " + i + 
                         ", " + lower_list[i] +
                         ", " + upper_list[i] );
      p_ds.getData_entry(i).getAttributeList().addAttribute(
                 new IntAttribute("Lower Level Discriminator", lower_list[i]));
      p_ds.getData_entry(i).getAttributeList().addAttribute(
                 new IntAttribute("Upper Level Discriminator", upper_list[i]));
    }
    return new_ds;
  }


 /* -------------------------------- main -------------------------------- */ 
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {
    System.out.println("SetDsicriminatorLevels compiled OK ");
/*
    String    run_A = "/home/dennis/ARGONNE_DATA/hrcs2937.run";
    Retriever rr    = new RunfileRetriever( run_A );
*/
    String    run_B = "/home/dennis/ARGONNE_DATA/pulseheight.nxs";
    Retriever rr    = new NexusRetriever( run_B );

    DataSet   p_ds  = rr.getDataSet(1);
    if ( p_ds == null )
    {
      System.out.println("ERROR: file not found");
      System.exit(1);
    }

    Operator op = new SetDiscriminatorLevels( p_ds, 0.25f, 1e-8f, 255, 40 );
    Object obj = op.getResult();

    if ( obj instanceof DataSet[] )
    {
      DataSet ds_a[] = (DataSet[])obj;

      for ( int i = 0; i < ds_a.length; i++ )
        new ViewManager( ds_a[i], IViewManager.IMAGE );
    }
    else
      System.out.println("Operator returned " + obj );
  }

}
