/*
 * File:  LowPassFilterDS0.java 
 *
 * Copyright (C) 2004, Dennis Mikkelson
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
 * Revision 1.2  2004/09/29 22:45:36  kramer
 * Changed the title to "Butterworth Low Pass Filter 0" to distinguish this
 * operator from the regular "Butterworth Low Pass Filter" in the GUI.
 *
 * Revision 1.1  2004/07/26 22:12:09  taoj
 * test version.
 *
 * 
 * Revision 1.2 2004/06/16 10:39:00 julian
 * Added a few lines so it handles datablocks with leading and trailing zeroes in the y value array;
 * 
 * Revision 1.1  2004/05/07 17:57:01  dennis
 * Moved operators that extend GenericSpecial from Operators
 * to Operators/Special
 *
 * Revision 1.1  2004/03/29 17:58:00  dennis
 * Initial version of operator to perform Low-Pass Fourier Filtering
 * using a Butterworth filter.  The version does the filtering
 * "in place" by altering the current DataSet.  Future versions
 * may include an option to create a new DataSet and my be placed
 * in a different package.
 *
 */
package Operators.Special;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.retriever.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.util.*;
import java.util.*;
import jnt.FFT.*;

/**
 * This operator smoothes all Data blocks in a DataSet using Fourier
 * Filtering.  Specifically, a Butterworth Low Pass filter is used.
 * After the Fourier transform is calculated, the higher frequency 
 * components in each Data block are attenuated by multiplying by
 * the factor:
 *   
 *    H(u) = 1 / (1 + (u^2/cutoff^2)^n)
 * 
 * where u, and cutoff are the normalized frequency of the component
 * and cutoff is the normalized cutoff frequency specified by the user.
 * Frequecies are normalized to [0,1], with 0 representing the "DC term"
 * and 1 representing the highest frequency possible. The order of 
 * the filter is "n". 
 *   
 *  NOTE: This only works for TabulatedData objects such as "
 *        "HistogramTable or FunctionTable Data objects."
 * 
 *  Butterworth low pass filter 
 */

public class LowPassFilterDS0 extends GenericSpecial
{
  private static final String TITLE = "Butterworth Low Pass Filter 0";

 /* ------------------------- DefaultConstructor -------------------------- */
 /** 
  *  Creates operator with title "Butterworth Low Pass Filter" and
  *  a  default list of parameters.
  */  
  public LowPassFilterDS0()
  {
    super( TITLE );
  }


 /* ----------------------------- Constructor ----------------------------- */
 /** 
  *  Creates operator with title "Butterworth Low Pass Filter" and the 
  *  specified list of parameters.  The getResult method must still 
  *  be used to execute the operator.
  *  
  *  @param  p_ds    The DataSet to process.
  *  @param  cutoff  The "normalized" cutoff frequency, between 0 and 1.
  *  @param  order   The order of the Butterworth low pass filter, 1, 2, 3
  *                  etc.  As the order increases the cutoff gets sharper.
  *                  
  */
  public LowPassFilterDS0( DataSet p_ds, float cutoff, int order )
  {
    this();
    parameters = new Vector();
    addParameter( new Parameter("Data Set to Process", p_ds) );
    addParameter( new Parameter("Normalized cutoff [0,1]", new Float(cutoff)) );
    addParameter( new Parameter("Filter order,1,2...", new Integer(order)));
  }


 /* ------------------------------ getCommand ----------------------------- */
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "LowPassFilter", the command used to invoke this operator 
  * in Scripts
  */
  public String getCommand()
  {
    return "LowPassFilter0";
  }


 /* ------------------------ setDefaultParameters ------------------------- */
 /** 
  * Sets default values for the parameters.  The parameters set must match the 
  * data types of the parameters used in the constructor.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("Data Set to Process", DataSet.EMPTY_DATA_SET));
    addParameter( new Parameter("Normalized cutoff [0,1]", new Float(0.5)) );
    addParameter( new Parameter("Filter order,1,2,...", new Integer(1)) );
  }


 /* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */                                                                                 
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer(""); 

    s.append("@overview This operator smoothes all Data blocks in a ");
    s.append("          DataSet using Fourier Filtering. ");

    s.append("@assumptions Since the Data blocks in the DataSet are ");
    s.append("             in-place, the Data blocks must be ");
    s.append("             TablulatedData objects.");

    s.append("@algorithm A Butterworth Low Pass filter is used.  The " );
    s.append("           sequence of y values is first extended to ");
    s.append("           twice it's original length, by repeating the ");
    s.append("           original sequence of y values in reverse order. ");
    s.append("           This forms an even, periodic function.  Next ");
    s.append("           the discrete Complex Fourier transform is ");
    s.append("           calculated using the FFT codes in the Java ");
    s.append("           Numerical Toolkit (jnt) from NIST. The ");
    s.append("           higher frequency components in each Data ");
    s.append("           block are attenuated by multiplying by ");
    s.append("           the factor: ");
    s.append("           H(u) = 1 / (1 + (u^2/cutoff^2)^n) ");
    s.append("           where u, is the normalized frequency ");
    s.append("           of the component and cutoff is the normalized ");
    s.append("           cutoff frequency specified by the user. ");
    s.append("           Frequecies are normalized to [0,1], with 0 ");
    s.append("           representing the DC term and 1 representing ");
    s.append("           the highest frequency possible. The order of ");
    s.append("           the filter is 'n'. ");
    s.append("           After attenuating the high frequency components");
    s.append("           the inverse Fourier Transform is applied to ");
    s.append("           construct the low-pass filtered result.");

    s.append("@param  p_ds    The DataSet to process ");

    s.append("@param  cutoff  The normalized cutoff frequency, ");
    s.append("                between 0 and 1. Values close to 0 will ");
    s.append("                reconstruct the original data using only ");
    s.append("                a small number of low-frequency components.");
    s.append("                A value of 1 will do very little filtering ");
    s.append("                since even the highest possible frequency will ");
    s.append("                only be reduced by a a factor of 1/2. ");

    s.append("@param  order   The order of the filter 1, 2, 3, etc.  As the ");
    s.append("                order of the filter is increased, the cutoff ");
    s.append("                becomes more abrupt.");
    s.append("@return A  String indicating that the operation was completed.");

    return s.toString();
  }


 /* ------------------------------ getResult ------------------------------- */
 /** 
  *  Replaces all y values of all Data blocks by lowpass filtered values.
  *
  *  @return  This returns a string indicating that the DataSet was altered.
  */
  public Object getResult()
  {
    DataSet ds     = (DataSet)(getParameter(0).getValue());
    float   cutoff = ((Float)(getParameter(1).getValue())).floatValue();
    int     order  = ((Integer)(getParameter(2).getValue())).intValue();

    if ( order < 1 )
    {
      SharedData.addmsg("Warning: order < 1 in LowPassFilterDS, using 1");
      SharedData.addmsg("         specified order was = " + order);
      order = 1;
    }
    
    if ( cutoff <= 0 )
    {
      SharedData.addmsg("Warning: cutoff <= 0 in LowPassFilterDS, using 0.1");
      SharedData.addmsg("         specified cutoff was = " + cutoff);
      cutoff = 0.1f;
    }

    Data   spectrum;                    // variables to hold one data
    float  y[] = null;                  // block and its y values 
    float  complex[] = null;            // the complex Fourier transform
                                        // of y
    int    N = -1;
    float  scale = 0;
    int    index = 0;
    float  attenuation = 1;
    float  u = 0;
    
    float[] yvals;
    int istart, iend;
                                               // for each spectrum.....
    ComplexFloatFFT fft_calculator = null;
    for ( int i = 0; i < ds.getNum_entries(); i++ )
    {
      spectrum = ds.getData_entry( i );        // get REFERENCE to a spectrum
      yvals    = spectrum.getY_values();           // then get REFERENCE to array of y_values
      istart = 0;
      iend = yvals.length-1;

      while (yvals[istart] == 0.0f) {
        istart++;
      }                                                                //find the first none zero y value;
      while (yvals[iend] == 0.0f) {
        iend--;
      }                                                               //find the last none zero y value;
      y = new float[iend-istart+1];                
      
      System.arraycopy(yvals, istart, y, 0, iend-istart+1);  //y is the none zero subarray of yvals;

      if ( y.length != N )                     // we need a new arrays 
      {                                        // and fft calculator
        N = y.length;
        complex = new float[ 4*N ];
        fft_calculator = new ComplexFloatFFT_Mixed( 2 * N );
        scale = 1.0f/(2 * N); 
      }
                                               // copy the data (forwards and 
                                               // backwards into the array as
                                               // complex values, and do FFT
      for ( int j = 0; j < N; j++ )
      {
        complex[ 2*j   ] = y[j];               // data values, increasing,
        complex[ 2*j+1 ] = 0;                  // imaginary part is zero
        complex[ 4*N - 2 - 2*j   ] = y[j];     // data values, decreasing,
        complex[ 4*N - 2 - 2*j+1 ] = 0;        // imaginary part is zero
      }

      fft_calculator.transform( complex );
                                               // now filter the transform by 
                                               // multiplying terms by the
                                               // Butterworth attenuation factor
      for ( int j = 1; j < N; j++ )
      {
         u = j / (float)N;
         if ( order > 1 )
           attenuation = (float)(1.0/(1+Math.pow((u*u)/(cutoff*cutoff),order)));
         else
           attenuation = (float)(1.0/(1 + (u*u)/(cutoff*cutoff) ));
         index = 2 * j;
         complex[ index ]     *= attenuation;
         complex[ index + 1 ] *= attenuation;
         index = 2 * ( 2*N - j );
         complex[ index ]     *= attenuation;
         complex[ index + 1 ] *= attenuation; 
      }
                                             // Now do the inverse FFT and copy
                                             // the filtered data back into y[]
      fft_calculator.backtransform( complex );

      for ( int j = 0; j < N; j++ )          // for each channel.....
        y[j] = complex[2*j] * scale;         // take the real part of the
                                             // inverse transform 
      
      System.arraycopy(y, 0, yvals, istart, iend-istart+1);                                       
                                             
    }
                                             // record the operation in the
                                             // DataSet's log and return
        
    ds.addLog_entry("Low Pass Filtered");
    ds.addLog_entry("  normalized cutoff frequency " + cutoff );
    ds.addLog_entry("  Butterworth filter order " + order );
    return new String("Operator completed successfully");
  }


 /* --------------------------------- clone -------------------------------- */
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    Operator op = new LowPassFilterDS0();
    op.CopyParametersFrom( this );
    return op;
  }

 
 /* ------------------------------- main ----------------------------------- */
 /** 
  * Test program to verify that this will compile and run ok.  
  *
  */
  public static void main( String args[] )
  {
    System.out.println("Test of LowPasFilterDS starting...");

                                                 // make a new RunfileRetriever
                                                 // and load the first DataSet
                                                 // YOU MUST MODIFY THE RUN 
                                                 // NAME TO LOAD A FILE ON YOUR
                                                 // SYSTEM
    String    run_name = "/usr2/ARGONNE_DATA/gppd12358" + ".run";
    Retriever rr       = new RunfileRetriever( run_name );
    DataSet   ds       = rr.getDataSet(1);

    if ( ds == null )                            // check for successful load
    {
      System.out.println("ERROR: file not found");
      System.exit(1);
    }
                                                 // make a clone and pop up a
                                                 // view of the clone so that
                                                 // we can compare them after
                                                 // the operator changes the
                                                 // original.
    DataSet     new_ds = (DataSet)ds.clone();
    new ViewManager( new_ds, IViewManager.IMAGE );

                                                 // make and run the operator
                                                 // to filter the original ds
                                                 // and display it after it's 
    Operator op  = new LowPassFilterDS0( ds, 0.1f, 1 );          // altered
    Object   obj = op.getResult();
    new ViewManager( ds, IViewManager.IMAGE );
                                                 // display any message string
                                                 // that might be returned
    System.out.println("Operator returned: " + obj );
    System.out.println("Test of LowPassFilterDS done.");
  }
}
