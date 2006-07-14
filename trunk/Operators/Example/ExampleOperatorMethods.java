/*
 * File:  ExampleOperatorMethods.java
 *
 * Copyright (C) 2006, Dennis Mikkelson 
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
 * Contact :  Dennis Mikkelson<mikkelsond@uwstout.edu>
 *            MSCS Department
 *            HH237H
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2006/07/14 13:56:06  dennis
 * Changed scale factor for normalization by solid angle, to 0.001f,
 * in basic test code.
 *
 * Revision 1.1  2006/07/12 14:33:17  dennis
 * Initial checkin of class with several examples of static methods
 * that can be used to generate an ISAW Operator.
 */
package Operators.Example;

import java.io.*;
import java.util.*;
import gov.anl.ipns.MathTools.Geometry.*;
import DataSetTools.dataset.*;
import DataSetTools.math.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;


/**
 *  This class contains the static methods providing the core calculations
 *  for three example Operators:
 * 
 *      SayHello   
 *      NormalizeBySolidAngle  
 *      NormalizeByDetectorEfficiency
 *
 *  The first example is the Operator equivalent of Hello World.  The last
 *  two are more typical of calculations encountered while reducing data and
 *  illustrated extracting data from a DataSet, using attributes and 
 *  putting modified data back into a DataSet.
 */

public class ExampleOperatorMethods
{

  /**
   *  This method just concatenates the word "Hello " with the specified name.
   *
   *  @param  name   The name of the person to say hello to.
   *
   *  @return the completed "Hello" String
   */
  public static String SayHello( String name )
  {
    return "Hello " + name ;
  }


  /**
   *  Normalize the counts in a spectrum by multiplying each count by 
   *  scale_factor/solid_angle.  The error estimates are also adjusted,
   *  assuming the error in the solid_angle value is zero.  
   *  At this level, the basic information for each Data block is unpacked 
   *  from a DataSet into arrays and passed on to a more basic routine to 
   *  do the actual calculation.
   *
   *  @param  ds            The DataSet to be normalized.  The Data blocks must
   *                        have properly set PixelInfoList attributes.
   *
   *  @param  scale_factor  Scale factor to keep reasonable range on the
   *                        normalized counts.
   *
   *  @return a new DataSet with counts normalized by solid angle.
   */
  public static DataSet NormalizeBySolidAngle( DataSet ds, float scale_factor )
  {
    ds = (DataSet)ds.clone();                       // clone the ds then 
                                                    // change the clone
    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      TabulatedData this_spectrum = (TabulatedData)ds.getData_entry(i);
      PixelInfoList pil = AttrUtil.getPixelInfoList( this_spectrum );
      float solid_angle = pil.SolidAngle();
      float[] sample = this_spectrum.getY_values(); // For TabulatedData, this
                                                    // is a REFERENCE to the 
                                                    // array of y-values.
      float[] errors = this_spectrum.getErrors();   // This may NOT be a 
                                                    // REFERENCE to the array
                                                    // of errors.
      NormBySolidAngle_calc( solid_angle, scale_factor, sample, errors );

      this_spectrum.setErrors( errors );           // May need to set the
                                                   // errors, since they may
                                                   // have just been calculated
    }                                              // as sqrt(counts)

    ds.addLog_entry("Normalized by solid angle");  // add this operation to the
                                                   // log of operations and
    return ds;                                     // return
  }


  /**
   *  This method does the actual calculation to normalize one Data block 
   *  by the solid_angle of the corresponding detector.
   *
   *  @param  solid_angle   The solid angle of the detector element(s) for
   *                        this Data block.
   *  @param  scale_factor  Scale factor to keep reasonable range on the
   *                        normalized counts.
   *  @param  sample        Array of counts for one detector.  This is passed
   *                        by reference, and its contents are altered by
   *                        this method. 
   *  @param  sample_errs   Array of error estimates for the counts.  This 
   *                        is passed by reference, and its contents are 
   *                        altered by this method. 
   */
  public static void NormBySolidAngle_calc( float   solid_angle,
                                            float   scale_factor,
                                            float[] sample,
                                            float[] sample_errs  )
  {
     float scale = scale_factor / solid_angle;

     for ( int i = 0; i < sample.length; i++ )
     {
        sample[i]      *= scale;
        sample_errs[i] *= scale;      // assuming there is zero error in the
                                      // solid angle
     }
  }

 
  /**
   *  Normalize the counts in a spectrum by multiplying each count by a
   *  wavelength dependent efficiency correction.  The error estimates are
   *  also adjusted.  At this level, the detector efficiency information is
   *  is read in from a file and the basic information for each Data block
   *  is unpacked from a DataSet into arrays.  The actual work is done in
   *  a lower level routine.  NOTE: A time-of-flight DataSet, with 
   *  histograms and detector position attributes is required. 
   *
   *  @param  ds            The DataSet to be normalized.  The Data blocks must
   *                        have properly set PixelInfoList attributes.
   *
   *  @param  eff_file      The file name for the ASCII file listing the 
   *                        detector efficiency as a function of wavelength.
   *                        The form of this file is assumed to be three
   *                        column ASCII, with each line containing the
   *                        wavelength, efficiency ratio and error estimate.
   *                        The first line of the file contains the number of
   *                        wavelengths recorded. 
   *
   *  @return a new DataSet with counts normalized by the wavelength dependent
   *          detector efficiency.
   */
  public static DataSet NormalizeByDetectorEfficiency( DataSet  ds, 
                                                       String   eff_file )
                       throws IOException
  {
                                        // load in the efficiency information
                                        // as a function of wavelength

     FileReader     in_file = new FileReader( eff_file );     
     BufferedReader buff    = new BufferedReader( in_file );
     Scanner        file_sc = new Scanner( buff );
     
     int num_wavelengths = file_sc.nextInt();
     float[] lamda_eff = new float[num_wavelengths];
     float[] eff       = new float[num_wavelengths];
     float[] eff_errs  = new float[num_wavelengths];
     for ( int i = 0; i < num_wavelengths; i++ )
     {
       lamda_eff[i] = file_sc.nextFloat();
       eff[i]       = file_sc.nextFloat();
       eff_errs[i]  = file_sc.nextFloat();
     }

                                     // NOTE: We should check the DataSet
                                     // units and throw an exception if not TOF
     float t_ave;
     float initial_path;
     float final_path;
     float path_length;
     DetectorPosition position;
                                     // Start with an empty copy of the current
                                     // DataSet.

     DataSet wl_ds = (DataSet)ds.empty_clone();

                                     // For each Data block, get the counts,
                                     // errors, and times-of-flight.  Also,
                                     // construct an array giving the
                                     // wavelengths for each histogram bin.

     for ( int i = 0; i < ds.getNum_entries(); i++ )
     {
       Data this_spectrum  = ds.getData_entry( i );
       if ( !this_spectrum.isHistogram() )
                throw new IllegalArgumentException( 
                          "Need a histogram in NormalizeByDetectorEfficiency");
       float[] sample      = this_spectrum.getCopyOfY_values();
       float[] sample_errs = this_spectrum.getCopyOfErrors();
       float[] t_vals      = this_spectrum.getX_scale().getXs();
       float[] lamda_sample = new float[sample.length];

       initial_path = AttrUtil.getInitialPath( this_spectrum );
       position     = AttrUtil.getDetectorPosition( this_spectrum );
       final_path   = position.getDistance();
       for ( int k = 0; k < lamda_sample.length; k++ )
       {
         t_ave = ( t_vals[k] + t_vals[k+1] )/2;
         path_length = initial_path + final_path; 
         lamda_sample[k] = tof_calc.Wavelength( path_length, t_ave );
       }
                                      // call a lower level routine to do the
                                      // actual calculation 
       NormByDetEff_calc( lamda_eff,    eff,    eff_errs, 
                          lamda_sample, sample, sample_errs );

                                      // put the normalized data into a 
                                      // new Data block, and add it to the
                                      // new DataSet
       Data normalized_spectrum = new HistogramTable( 
                                         this_spectrum.getX_scale(),
                                         sample,
                                         sample_errs,
                                         this_spectrum.getGroup_ID() );
       normalized_spectrum.setAttributeList( this_spectrum.getAttributeList());
       wl_ds.addData_entry( normalized_spectrum );
     }
                                       // Add this operation to the Log of
                                       // operations carried out on the
                                       // DataSet and return.
     wl_ds.addLog_entry("Normalized by detector efficiency");
     return wl_ds;
  }


  /**
   *  Normalize the counts in a spectrum by multiplying each count by a
   *  wavelength dependent efficiency correction.  The error estimates are
   *  also adjusted.  All information is passed in simple arrays.  
   *  NOTE: Since the sample and sample_errs arrays are passed by reference,
   *  there entries are changed by this method, to pass information back to
   *  the calling routine.
   *
   *  @param  lamda_eff     The list of wavelengths at which the efficiency
   *                        has been measured.
   *  @param  eff           The list of measured efficiencies.
   *  @param  eff_errs      The estimated errors in the measured efficiencies.
   *
   *  @param  lamda_sample  The list of wavelengths at which the sample 
   *                        scattering has been measured.
   *  @param  sample        The list of counts for the sample.
   *  @param  sample_errs   The list estimated errors sample counts.
   *
   */
  public static void NormByDetEff_calc( float[] lamda_eff,
                                        float[] eff,
                                        float[] eff_errs,
                                        float[] lamda_sample,
                                        float[] sample,
                                        float[] sample_errs  )
  {
     float  key;
     int    index;
     int    insert_point;
     float  eff_ratio,
            eff_error;
     float  samp,
            samp_error;

     for ( int i = 0; i < sample.length; i++ )         // for each sample point
     {
        key = lamda_sample[i];                         // use binary search
        index = Arrays.binarySearch( lamda_eff, key ); // to find wavelength

                                                       // if index > 0 we found
                                                       // the key in the list
        if (index < 0)                                 // exactly.  Otherwise 
        {                                              // we may be off the end
          insert_point = -index - 1;                   // of the list.   

          if ( insert_point <= 0 )                     // if off either end
            index = 0;                                 // of the list, use
                                                       // the eff value from
          else if ( insert_point >= eff.length )       // that end of the list
            index = eff.length-1;

          else                                         // use value before the 
            index = insert_point - 1;                  // insertion point.
        }                                              // NOTE: We could 
                                                       // interpolate, but this
                                                       // is meant as an example
        eff_ratio  = eff[ index ];
        eff_error  = eff_errs[ index ];
        samp       = sample[i];
        samp_error = sample_errs[i];
                                                       // adjust the error
                                                       // estimate
        if ( samp == 0 || eff_ratio == 0 )
          sample_errs[i] = 0;
        else
          sample_errs[i] = Math.abs( samp/eff_ratio ) * 
             (float)Math.sqrt( samp_error /samp    * samp_error/samp +
                               eff_error/eff_ratio * eff_error /eff_ratio );

        sample[i] = samp / eff_ratio;                  // adjust the sample
                                                       // value
     }
  }

  
  /**
   *  Simple main program to independently check the operation of the
   *  normalization methods.  This is only for basic functionality testing.
   */
  public static void main( String args[] ) throws IOException
  {
                             // check the NormalizeByDetectorEfficiency 
                             // calculation by loading a SAND DataSet and
                             // calling the method.

     String  data_dir  = "/home/dennis/WORK/ISAW/SampleRuns/";
     String  sand_file = "sand22366.run";
     String  efr_file  = "EFR22227.dat";

     RunfileRetriever rr = new RunfileRetriever( data_dir + sand_file );
     DataSet sand_ds = rr.getDataSet(1);
     new ViewManager( sand_ds, IViewManager.IMAGE );

     DataSet new_ds = NormalizeByDetectorEfficiency(sand_ds, data_dir+efr_file);
     new ViewManager( new_ds, IViewManager.IMAGE );

                             // check the NormalizeBySolidAngle 
                             // calculation by loading a HRMECS DataSet and
                             // calling the method.
     String  hrcs_file = "hrcs3817.run";
     rr = new RunfileRetriever( data_dir + hrcs_file );
     DataSet hrcs_ds = rr.getDataSet(1);
     new ViewManager( hrcs_ds, IViewManager.IMAGE );

     float scale_factor = 0.001f;
     DataSet norm_ds = NormalizeBySolidAngle( hrcs_ds, scale_factor );
     new ViewManager( norm_ds, IViewManager.IMAGE );
  }

}
