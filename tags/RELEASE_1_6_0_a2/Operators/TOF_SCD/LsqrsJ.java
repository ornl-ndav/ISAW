/*
 * File:  LsqrsJ.java
 *
 * Copyright (C) 2003, Peter F. Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.24  2003/08/05 21:41:20  dennis
 * Set new hkl values into the Peak objects, after transforming the
 * hkl values by the transform specified by Scalar.  This fixes the
 * problem of the old hkl values being written to the lsqrs.log file.
 * Also, now limits the number of '*' used to flag peaks whose hkl
 * values differ from the theoretical values to one '*' per 0.1
 * difference in hkl, up to a max of ten.
 *
 * Revision 1.23  2003/07/28 20:21:39  dennis
 * The log file now includes the "fractional" hkl values corresponding
 * to a peak, as well as the integer hkl values.  Also, the difference
 * between the "fractional" and integer hkl values is included.
 *
 * Revision 1.22  2003/07/28 18:51:17  dennis
 * Fixed off by one error in extracting x,y,z values from the
 * result of Peak.toString().  The "x" value lost it's leading
 * digit, if it had two leading digits.  This made the recorded
 * value of x wrong in the least squares log file, in some cases.
 *
 * Revision 1.21  2003/07/09 21:02:45  bouzekc
 * Added comments and rearranged methods according to access
 * rights.
 *
 * Revision 1.20  2003/07/09 19:58:48  bouzekc
 * Added check in for null range parameter.  Sets range from
 * 0 to Integer.MAX_VALUE if null.
 *
 * Revision 1.19  2003/07/08 22:49:40  bouzekc
 * Now returns the fully qualified name of the lsqrs.log file.
 *
 * Revision 1.18  2003/06/27 18:05:34  bouzekc
 * Fixed indenting errors.
 *
 * Revision 1.17  2003/06/27 18:02:50  bouzekc
 * Now returns an ErrorString when the matrix file is not
 * specified.  Also returns an ErrorString rather than the
 * original error if Util.writeMatrix fails.
 *
 * Revision 1.16  2003/06/27 15:00:05  bouzekc
 * Now traps error which occurred if the matrix file was not
 * specified.  In that case, the lsqrs log file is returned
 * rather than "Success."
 *
 * Revision 1.15  2003/06/26 22:23:20  bouzekc
 * Added code to reject peaks based on peak intensity
 * threshold and detector geometry.
 *
 * Revision 1.14  2003/06/26 17:01:57  bouzekc
 * Changed "Channels to keep" to "Pixel Rows and Columns to
 * Keep".  Added a "NOT IMPLEMENTED" to parameter names for
 * the last two parameters.
 *
 * Revision 1.13  2003/06/25 21:41:01  bouzekc
 * Added hook parameters for minimum peak count threshold
 * and channels to keep.
 *
 * Revision 1.12  2003/06/25 21:33:18  bouzekc
 * Reformatted for indenting and spacing consistency.
 *
 * Revision 1.11  2003/06/25 16:42:38  bouzekc
 * Removed unused DEBUG variable, commented out unused
 * readans() private method.
 *
 * Revision 1.10  2003/06/25 16:34:32  bouzekc
 * Filled out getDocumentation(), removed extraneous
 * debug print messages.
 *
 * Revision 1.9  2003/06/09 22:00:30  bouzekc
 * getResult() now returns the name of the log file
 * and prints a message to SharedData.
 *
 * Revision 1.8  2003/06/02 14:31:06  pfpeterson
 * Fixed a spelling error.
 *
 * Revision 1.7  2003/05/21 18:48:10  pfpeterson
 * Added log file writing.
 *
 * Revision 1.6  2003/05/20 20:27:03  pfpeterson
 * Added parameter for transformation matrix.
 *
 * Revision 1.5  2003/05/06 18:18:25  pfpeterson
 * Removed dependence on experiment file.
 *
 * Revision 1.4  2003/04/30 19:52:04  pfpeterson
 * Moved lattice parameter calculation from orientation matrix into
 * DataSetTools.operator.Generic.TOF_SCD.Util.
 *
 * Revision 1.3  2003/04/28 16:39:24  pfpeterson
 * Changed calculation of chisq to agree with how the best fit matrix
 * is calculated.
 *
 * Revision 1.2  2003/04/23 15:52:33  pfpeterson
 * Fixed calculation of lattice parameters and implemented own method
 * for calculating chisq.
 *
 * Revision 1.1  2003/04/11 15:36:16  pfpeterson
 * Added to CVS.
 *
 */
package Operators.TOF_SCD;

import DataSetTools.math.LinearAlgebra;

import DataSetTools.operator.Generic.TOF_SCD.*;

import DataSetTools.parameter.*;

import DataSetTools.util.ErrorString;
import DataSetTools.util.FilenameUtil;
import DataSetTools.util.Format;
import DataSetTools.util.SharedData;
import DataSetTools.util.TextFileReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.lang.reflect.Array;

import java.util.Enumeration;
import java.util.Vector;


/**
 * Class designed to analyze cell scalars from BLIND to determine Laue
 * symmetry. Originally written by R. Goyette.
 */
public class LsqrsJ extends GenericTOF_SCD {
  //~ Static fields/initializers ***********************************************

  private static final double SMALL    = 1.525878906E-5;
  private static final String identmat = "[[1,0,0][0,1,0][0,0,1]]";

  //~ Constructors *************************************************************

  /**
   * Construct an operator with a default parameter list.
   */
  public LsqrsJ(  ) {
    super( "JLsqrs" );
  }

  //~ Methods ******************************************************************

  /**
   * Returns the command name to be used with script processor, in this case
   * JLsqrs.
   */
  public String getCommand(  ) {
    return "JLsqrs";
  }

  /**
   * Set the parameters to default values.
   */
  public void setDefaultParameters(  ) {
    parameters = new Vector(  );

    LoadFilePG lfpg = new LoadFilePG( "Peaks file", null );

    lfpg.setFilter( new PeaksFilter(  ) );

    //0
    addParameter( lfpg );

    //1
    addParameter( 
      new IntArrayPG( "Restrict Run Numbers (blank for all)", null ) );

    //2
    addParameter( 
      new IntArrayPG( "Restrict Sequence Numbers (blank for all)", null ) );

    //3
    addParameter( new StringPG( "Transform Matrix", identmat ) );

    SaveFilePG sfpg = new SaveFilePG( "Matrix file to write to", null );

    sfpg.setFilter( new MatrixFilter(  ) );

    //4
    addParameter( sfpg );

    //5
    addParameter( 
      new IntegerPG( "Minimum Peak Intensity Threshold", 0, false ) );

    //6
    addParameter( 
      new IntArrayPG( "Pixel Rows and Columns to Keep", "0:100", false ) );
  }

  /**
   * This returns the help documentation
   */
  public String getDocumentation(  ) {
    StringBuffer sb = new StringBuffer( 80 * 5 );

    // overview
    sb.append( "@overview This is a java version of \"LSQRS\" maintained by " );
    sb.append( "A.J.Schultz and originally written by J.Marc Overhage in " );
    sb.append( "1979. This version is updated to use different methods for " );
    sb.append( "finding determining the best UB matrix." );

    //assumptions
    sb.append( "@assumptions The peaks file exists and the transformation " );
    sb.append( "matrix is valid." );

    //algorithm
    sb.append( "@algorithm The reflections are read in from the peaks file.  " );
    sb.append( 
      "Then peaks that are not within the selected sequence numbers, " );
    sb.append( "not within the h, k, and l delta values, and not within the " );
    sb.append( "selected histogram are trimmed out.  Next the hkl and q " );
    sb.append( "matrix are created, and the transformation matrix is " );
    sb.append( "applied.  The UB matrix is calculated, and lattice " );
    sb.append( "parameters and cell volume are calculated.  Next the " );
    sb.append( "uncertainties and derivatives are calculated, and the sigmas " );
    sb.append( "are accumulated and turned into \"actual\" sigmas.  At this " );
    sb.append( "point, the log file and matrix file are written and/or " );
    sb.append( "updated." );

    // parameters
    sb.append( "@param peaksFile The peaks file to load. " );
    sb.append( "@param restrictRuns The run numbers to restrict. " );
    sb.append( "@param restrictSeq The sequence numbers to restrict. " );
    sb.append( "@param xFormMat The transformation matrix to use. " );
    sb.append( "@param matFile The matrix to write to. " );
    sb.append( "@param minThresh The minimum peak intensity threshold to " );
    sb.append( "use." );
    sb.append( "@param keepPixels The detector pixel range to keep." );

    // return
    sb.append( "@return If a matrix file name was given and the file was " );
    sb.append( "successfully written, it returns the " );
    sb.append( "name of the matrix file.  If no matrix file was written, it " );
    sb.append( "returns the name of the lsqrsJ log file." );

    // error
    sb.append( "@error If the peaks file is not found or cannot be read from." );
    sb.append( 
      "@error If the matrix file is not found or cannot be written to." );

    return sb.toString(  );
  }

  /**
   * Uses the current values of the parameters to generate a result.
   */
  public Object getResult(  ) {
    // get the parameters
    String peaksfile = getParameter( 0 )
                         .getValue(  )
                         .toString(  );
    int[] run_nums   = ( ( IntArrayPG )getParameter( 1 ) ).getArrayValue(  );
    int[] seq_nums   = ( ( IntArrayPG )getParameter( 2 ) ).getArrayValue(  );
    int threshold    = ( ( IntegerPG )getParameter( 5 ) ).getintValue(  );
    int[] keepRange  = ( ( IntArrayPG )getParameter( 6 ) ).getArrayValue(  );
    float[][] matrix = null;
    int lowerLimit;
    int upperLimit;

    if( keepRange != null ) {
      lowerLimit   = keepRange[0];  //lower limit of range

      //upper limit of range
      upperLimit = keepRange[keepRange.length - 1];
    } else {  //shouldn't happen, but default to 0:MAX_VALUE
      lowerLimit   = 0;
      upperLimit   = Integer.MAX_VALUE;
    }

    String logfile;

    {
      IParameter iparm = getParameter( 3 );

      if( iparm.getValue(  ) == null ) {
        matrix = null;
      } else {
        try {
          matrix = stringTo2dArray( iparm.getValue(  ).toString(  ) );
        } catch( NumberFormatException e ) {
          return new ErrorString( "Improper format in matrix" );
        }
      }
    }

    String matfile = getParameter( 4 )
                       .getValue(  )
                       .toString(  );

    matfile = FilenameUtil.setForwardSlash( matfile );

    // confirm the parameters
    {
      File file = new File( peaksfile );

      if( !file.canRead(  ) ) {
        return new ErrorString( peaksfile + " is not user readable" );
      }

      if( ( matfile == null ) || ( matfile.length(  ) <= 0 ) ) {
        matfile = null;
      } else {
        file = new File( matfile );

        if( file.isDirectory(  ) ) {
          matfile = null;
        }
      }

      if( matfile == null ) {
        return new ErrorString( "Matrix file has not been specified" );
      }

      file = null;
    }

    // create a buffer for a log file
    StringBuffer logBuffer = new StringBuffer(  );

    logBuffer.append( "Peaks file  = " + peaksfile + "\n" );
    logBuffer.append( "Matrix file = " + matfile + "\n" );
    logBuffer.append( "run#        = " + arrayToString( run_nums ) + "\n" );
    logBuffer.append( "seq#        = " + arrayToString( seq_nums ) + "\n" );

    if( matrix != null ) {
      logBuffer.append( "     " );

      for( int i = 0; i < 3; i++ ) {
        logBuffer.append( Format.real( matrix[0][i], 3 ) + " " );
      }

      logBuffer.append( "\n" + "UB = " );

      for( int i = 0; i < 3; i++ ) {
        logBuffer.append( Format.real( matrix[1][i], 3 ) + " " );
      }

      logBuffer.append( "\n" + "     " );

      for( int i = 0; i < 3; i++ ) {
        logBuffer.append( Format.real( matrix[2][i], 3 ) + " " );
      }

      logBuffer.append( "\n" );
    } else {
      logBuffer.append( "UB = identity\n" );
    }

    logBuffer.append( "------------------------------\n" );

    // read in the reflections from the peaks file
    Vector peaks = null;

    {
      DataSetTools.operator.Operator readpeaks = new ReadPeaks( peaksfile );
      Object res                               = readpeaks.getResult(  );

      if( res instanceof ErrorString ) {
        return res;
      } else if( res instanceof Vector ) {
        peaks = ( Vector )res;
      } else {
        return new ErrorString( 
          "Something went wrong reading peaks file: " + peaksfile );
      }

      readpeaks   = null;
      res         = null;
    }

    Peak peak = null;

    // trim out the peaks that are not in the list of selected sequence numbers
    if( seq_nums != null ) {
      for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
        peak = ( Peak )peaks.elementAt( i );

        if( binsearch( seq_nums, peak.seqnum(  ) ) == -1 ) {
          peaks.remove( i );
        }
      }
    }

    // trim out the peaks with out hkl listed
    for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
      peak = ( Peak )peaks.elementAt( i );

      if( ( peak.h(  ) == 0 ) && ( peak.k(  ) == 0 ) && ( peak.l(  ) == 0 ) ) {
        peaks.remove( i );
      }
    }

    // trim out the ones that are not in the selected histograms
    if( run_nums != null ) {
      for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
        peak = ( Peak )peaks.elementAt( i );

        if( binsearch( run_nums, peak.nrun(  ) ) == -1 ) {
          peaks.remove( i );
        }
      }
    }

    // trim out small peaks (defined by the threshold parameter)
    if( threshold >= 0 ) {
      for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
        peak = ( Peak )peaks.elementAt( i );

        if( peak.ipkobs(  ) < threshold ) {
          peaks.remove( i );
        }
      }
    }

    // trim out edge peaks (defined by the "pixels to keep" parameter)
    for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
      peak = ( Peak )peaks.elementAt( i );

      //see if the peak pixels are within the user defined array.  We are
      //assuming a SQUARE detector, so we'll reject it if the x or y position
      //is not within our range
      if( 
        ( peak.x(  ) > upperLimit ) || ( peak.x(  ) < lowerLimit ) ||
          ( peak.y(  ) > upperLimit ) || ( peak.y(  ) < lowerLimit ) ) {
        peaks.remove( i );
      }
    }

    // can't refine nothing
    if( peaks.size(  ) == 0 ) {
      return new ErrorString( "No peaks to refine" );
    }

    // create the hkl-matrix and q-matrix (q=1/d)
    double[][] q   = new double[peaks.size(  )][3];
    double[][] hkl = new double[peaks.size(  )][3];

    for( int i = 0; i < hkl.length; i++ ) {
      peak        = ( Peak )peaks.elementAt( i );
      hkl[i][0]   = Math.round( peak.h(  ) );
      hkl[i][1]   = Math.round( peak.k(  ) );
      hkl[i][2]   = Math.round( peak.l(  ) );
      q[i]        = peak.getUnrotQ(  );
    }

    // apply the transformation matrix
    if( matrix != null ) {
      float[] myhkl = new float[3];

      for( int i = 0; i < hkl.length; i++ ) {
        // zero out the temp values
        for( int j = 0; j < 3; j++ ) {
          myhkl[j] = 0f;
        }

        // multiply by the transformation matrix
        for( int j = 0; j < 3; j++ ) {
          for( int k = 0; k < 3; k++ ) {
            myhkl[k] = myhkl[k] + ( matrix[k][j] * ( float )hkl[i][j] );
          }
        }

        // copy back the temp values
        for( int j = 0; j < 3; j++ ) {
          hkl[i][j] = Math.round( myhkl[j] );
        }
      }
    }

    // set the new transformed hkl values back into the peaks objects. D.M.
    for( int i = 0; i < hkl.length; i++ ) {
      peak = ( Peak )peaks.elementAt( i );
      peak.sethkl((float)hkl[i][0], (float)hkl[i][1], (float)hkl[i][2], false);
    }


    // calculate ub
    double[][] UB = new double[3][3];
    double chisq  = 0.;

    {
      double[][] Thkl = new double[peaks.size(  )][3];
      double[][] Tq   = new double[peaks.size(  )][3];

      for( int i = 0; i < hkl.length; i++ ) {
        for( int j = 0; j < 3; j++ ) {
          Thkl[i][j]   = hkl[i][j];
          Tq[i][j]     = q[i][j];
        }
      }

      chisq   = LinearAlgebra.BestFitMatrix( UB, Thkl, Tq );
      chisq   = 0.;  // reset chisq
      Thkl    = new double[3][peaks.size(  )];

      for( int i = 0; i < hkl.length; i++ ) {
        for( int j = 0; j < 3; j++ ) {
          Thkl[j][i] = hkl[i][j];
        }
      }

      Tq     = LinearAlgebra.mult( UB, Thkl );
      Thkl   = LinearAlgebra.mult( LinearAlgebra.getInverse( UB ), Tq );

                                // the "observed" hkl corresponding to measured
                                // q values.
      double obs_q[][] = LinearAlgebra.getTranspose( q );
      double obs_hkl[][];          
      obs_hkl = LinearAlgebra.mult( LinearAlgebra.getInverse( UB ), obs_q );

      // write information to the log file
      logBuffer.append( 
        " seq#   h     k     l      x      y      z      " +
        "xcm    ycm      wl  Iobs    Qx     Qy     Qz\n" );

      for( int i = 0; i < peaks.size(  ); i++ ) {
        peak = (Peak)peaks.elementAt(i);
  
        logBuffer.append( Format.integer( peak.seqnum(), 5 ) +
                          Format.integer( peak.h(), 4 ) +
                          Format.integer( peak.k(), 6 ) +
                          Format.integer( peak.l(), 6 ) +
                          Format.real( peak.x(),   9, 2 ) +
                          Format.real( peak.y(),   7, 2 ) +
                          Format.real( peak.z(),   7, 2 ) +
                          Format.real( peak.xcm(), 7, 2 ) +
                          Format.real( peak.ycm(), 7, 2 ) +
                          Format.real( peak.wl(),  8, 4 ) +
                          Format.integer(  peak.ipkobs(), 6 ) +
                          Format.real( peak.getUnrotQ()[0], 8, 3 ) +
                          Format.real( peak.getUnrotQ()[1], 7, 3 ) + 
                          Format.real( peak.getUnrotQ()[2], 7, 3 ) +"\n" );

/* This "should" eventually work, for writing out the values that correspond
   to the integerized hkl, but the peak requires the calibration information
   which is not currently available.  

        Peak exact_peak = (Peak)peak.clone();
                                 // now trigger the peak to recalculate it's
                                 // values based on the integerized h,k,l.
        exact_peak.sethkl( peak.h(), peak.k(), peak.l(), true );  

        logBuffer.append( Format.string("", 21) + 
                          Format.real( exact_peak.x(),   9, 2 ) +
                          Format.real( exact_peak.y(),   7, 2 ) +
                          Format.real( exact_peak.z(),   7, 2 ) +
                          Format.real( exact_peak.xcm(), 7, 2 ) +
                          Format.real( exact_peak.ycm(), 7, 2 ) +
                          Format.real( exact_peak.wl(),  8, 4 ) +
                          Format.integer( exact_peak.ipkobs(), 6 ) +
                          Format.real( exact_peak.getUnrotQ()[0], 8, 3 ) +
                          Format.real( exact_peak.getUnrotQ()[1], 7, 3 ) +
                          Format.real( exact_peak.getUnrotQ()[2], 7, 3 ) +"\n");
*/
        logBuffer.append( Format.string("",73) +
                          Format.real( Tq[0][i], 7, 3 ) + 
                          Format.real( Tq[1][i], 7, 3 ) +
                          Format.real( Tq[2][i], 7, 3 ) + "\n" );

                                              // print out third line, with 
                                              // fractional hkl values and
                                              // display difference between
                                              // observed and integer hkl
        logBuffer.append( "      " + 
                          Format.real( obs_hkl[0][i], 6, 2 ) +
                          Format.real( obs_hkl[1][i], 6, 2 ) + 
                          Format.real( obs_hkl[2][i], 6, 2 )  );

        double error = Math.abs( obs_hkl[0][i] - peak.h() ) +
                       Math.abs( obs_hkl[1][i] - peak.k() ) +
                       Math.abs( obs_hkl[2][i] - peak.l() );

        logBuffer.append( "   Del =" + Format.real( error, 6, 3 ) + " " ); 

                                              // show one "*" for each .1 error
        int n_stars = (int)( error / 0.1 );   // in hkl, up to 10.
        if ( n_stars > 10 )
          n_stars = 10;
        while ( n_stars > 0 )     
        {
          logBuffer.append( "*" ); 
          n_stars--;
        } 
        logBuffer.append( "\n" );

/* keep this around for debugging, until values corresponding to integer hkl
   have been calculated and written to the log file.
 
        logBuffer.append( "obs_hkl " + 
                          Format.real( obs_hkl[0][i], 6, 2 ) +
                          Format.real( obs_hkl[1][i], 6, 2 ) + 
                          Format.real( obs_hkl[2][i], 6, 2 ) +"\n" );
        logBuffer.append( "obs_q   " + 
                          Format.real( obs_q[0][i], 6, 3 ) +
                          Format.real( obs_q[1][i], 6, 3 ) + 
                          Format.real( obs_q[2][i], 6, 3 ) +"\n" );
        logBuffer.append( "pk_hkl  " + 
                          Format.real( peak.h(), 6, 2 ) +
                          Format.real( peak.k(), 6, 2 ) + 
                          Format.real( peak.l(), 6, 2 ) +"\n" );
        logBuffer.append( "pk_q    " + 
                          Format.real( peak.getUnrotQ()[0], 6, 3 ) +
                          Format.real( peak.getUnrotQ()[1], 6, 3 ) + 
                          Format.real( peak.getUnrotQ()[2], 6, 3 ) +"\n" );
*/
      }

      // calculate chisq
      for( int i = 0; i < peaks.size(  ); i++ ) {
        for( int j = 0; j < 3; j++ ) {
          chisq = chisq + ( ( q[i][j] - Tq[j][i] ) * ( q[i][j] - Tq[j][i] ) );
        }
      }
    }

    // add chisq to the logBuffer
    logBuffer.append( 
      "\nchisq[Qobs-Qexp]: " + Format.real( chisq, 8, 5 ) + "\n" );

    // calculate lattice parameters and cell volume
    double[] abc = Util.abc( UB );

    // determine uncertainties
    double[] sig_abc = new double[7];

    {
      double numFreedom      = 3. * ( peaks.size(  ) - 3. );
      double[] temp_abc      = null;
      double[][] derivatives = new double[3][7];
      double[][] VC          = generateVC( peaks );

      for( int i = 0; i < 3; i++ ) {
        // determine derivatives
        for( int j = 0; j < 3; j++ ) {
          UB[i][j]   = UB[i][j] + SMALL;
          temp_abc   = Util.abc( UB );
          UB[i][j]   = UB[i][j] - SMALL;

          for( int k = 0; k < 7; k++ ) {
            derivatives[j][k] = ( temp_abc[k] - abc[k] ) / SMALL;
          }
        }

        // accumulate sigmas
        for( int l = 0; l < 7; l++ ) {
          for( int m = 0; m < 3; m++ ) {
            for( int n = 0; n < 3; n++ ) {
              sig_abc[l] += (derivatives[m][l] * VC[m][n] * derivatives[n][l]);
            }
          }
        }
      }

      // turn the 'sigmas' into actual sigmas
      double delta = chisq / numFreedom;

      for( int i = 0; i < sig_abc.length; i++ ) {
        sig_abc[i] = Math.sqrt( delta * sig_abc[i] );
      }
    }

    // finish up the log buffer
    logBuffer.append( "\nOrientation matrix:\n" );

    for( int i = 0; i < 3; i++ ) {
      for( int j = 0; j < 3; j++ ) {
        logBuffer.append( Format.real( UB[j][i], 10, 6 ) );
      }

      logBuffer.append( "\n" );
    }

    logBuffer.append( "\n" );
    logBuffer.append( "Lattice parameters:\n" );

    for( int i = 0; i < 7; i++ ) {
      logBuffer.append( Format.real( abc[i], 10, 3 ) );
    }

    logBuffer.append( "\n" );

    for( int i = 0; i < 7; i++ ) {
      logBuffer.append( Format.real( sig_abc[i], 10, 3 ) );
    }

    logBuffer.append( "\n" );

    // print out the results
    toConsole( UB, abc, sig_abc );

    // write the log file
    {
      logfile = "lsqrs.log";

      int index = matfile.lastIndexOf( "/" );

      if( index >= 0 ) {
        logfile = matfile.substring( 0, index + 1 ) + logfile;
      }

      String warn = writeLog( logfile, logBuffer.toString(  ) );

      if( ( warn != null ) && ( warn.length(  ) > 0 ) ) {
        SharedData.addmsg( "JLsqrs(WARN) while writting lsqrs.log: " + warn );
      } else {
        SharedData.addmsg( "Wrote log file: " + logfile + "." );
      }
    }

    // update the matrix file
    ErrorString error = Util.writeMatrix( 
        matfile, double2float( UB ), double2float( abc ),
        double2float( sig_abc ) );

    if( error != null ) {
      return new ErrorString( "LsqrsJ failed to update matrix file: " + error );
    } else {
      SharedData.addmsg( "Wrote file: " + matfile );

      return logfile;
    }
  }

  /**
   * Main method for testing purposes and running outside of ISAW.
   */
  public static void main( String[] args ) {
    LsqrsJ lsqrs = new LsqrsJ(  );

    /* TEST VERSION
       lsqrs.getParameter(0).setValue(System.getProperty("user.dir"));
       lsqrs.getParameter(0).setValue("/IPNShome/pfpeterson/data/SCD");
       lsqrs.getParameter(1).setValue("quartz");
       lsqrs.getParameter(2).setValue("1:4"); // set histogram numbers
       lsqrs.getParameter(3).setValue("1,3:5,15:20"); // set sequence numbers
       lsqrs.getParameter(4).setValue("/IPNShome/pfpeterson/data/SCD/lookatme.mat");
     */
    /*
       if(!lsqrs.readUser())
         System.exit(-1);
     */
    Object obj = lsqrs.getResult(  );

    if( obj instanceof ErrorString ) {
      System.out.println( obj );
      System.exit( -1 );
    } else {
      System.out.println( "RESULT:" + obj );
      System.exit( 0 );
    }
  }

  /**
   * Read in input from the user when not running as an operator. This is
   * currently commented out code until the real implementation is done.
   */

  /*  private boolean readUser(){
     String ans=null;
     double delta = 0.01;
     System.out.println(" ENTER ERROR PARAMETER DELTA (DEFAULT=0.01): " );
     ans=readans();
     try{
       if( ans==null || ans.length()<=0)
         delta=0.01;
       else
         delta=Double.parseDouble(ans);
     }catch(NumberFormatException e){
       System.err.println(e);
       return false;
     }
     this.getParameter(1).setValue(new Float(delta));
     nequal = 0;
     nchoice = 0;
     ncell = 0;
     System.out.println(" ");
     System.out.println("HOW WOULD YOU LIKE TO RESTRICT THE SEARCH?");
     System.out.println("    1=LOOK FOR HIGHEST SYMMETRY");
     System.out.println("    2=INPUT A KNOWN CELL TYPE ");
     System.out.println("    3=INPUT SYMMETRIC SCALAR EQUALITIES ");
     System.out.println("<RET>=NO RESTRICTION");
     System.out.println(" ENTER METHOD OF SEARCH: " );
     ans=readans();
     try{
       if(ans==null || ans.length()<=0)
         nchoice=0;
       else
         nchoice=Integer.parseInt(ans);
     }catch(NumberFormatException e){
       System.err.println(e);
       return false;
     }
     System.out.println(" ");
     if (nchoice == 0){
       System.out.println("NO RESTRICTIONS");
       this.getParameter(2).setValue(choices.elementAt(0));
       return true;
     }else if (nchoice == 1){
       System.out.println("SEARCHING FOR HIGHEST SYMMETRY MATCH");
       this.getParameter(2).setValue(choices.elementAt(1));
       return true;
     }else if (nchoice == 2){
       System.out.println("POSSIBLE CELL TYPES ARE:");
       System.out.println("    1=P, CUBIC");
       System.out.println("    2=F, CUBIC");
       System.out.println("    3=R, HEXAGONAL");
       System.out.println("    4=I, CUBIC");
       System.out.println("    5=I, TETRAGONAL");
       System.out.println("    6=I, ORTHOROMBIC");
       System.out.println("    7=P, TETRAGONAL");
       System.out.println("    8=P, HEXAGONAL");
       System.out.println("    9=C, ORTHOROMBIC");
       System.out.println("   10=C, MONOCLINIC");
       System.out.println("   11=F, ORTHOROMBIC");
       System.out.println("   12=P, ORTHOROMBIC");
       System.out.println("   13=P, MONOCLINIC");
       System.out.println("   14=P, TRICLINIC");
       System.out.println("<RET>=EXIT");
       System.out.println(" ENTER CELL TYPE: " );
       //
       ans=readans();
       try{
         if(ans==null || ans.length()<=0)
           ncell=0;
         else
           ncell=Integer.parseInt(ans);
       }catch(NumberFormatException e){
         System.err.println(e);
         return false;
       }
       if( ncell==0 || ncell>14)
         return false;
       System.out.println(" ");
       System.out.println(" SEARCHING FOR CELL TYPE = "+cell[ncell-1]+" ");
       System.out.println(" ");
       this.getParameter(2).setValue(choices.elementAt(ncell+1));
       return true;
     }else if (nchoice == 3){
       System.out.println("THE POSSIBLE SYMMETRIC SCALAR EQUALITIES ARE:");
       System.out.println("    1=(R11 EQ R22 EQ R33)");
       System.out.println("    2=(R11 EQ R22 NE R33)");
       System.out.println("    3=(R11 EQ R33 NE R22)");
       System.out.println("    4=(R11 NE R22 NE R33)");
       System.out.println("<RET>=EXIT");
       //
       System.out.println(" ENTER EQUALITY: ");
       ans=readans();
       try{
         if(ans==null || ans.length()<=0)
           nequal=0;
         else
           nequal=Integer.parseInt(ans);
       }catch(NumberFormatException e){
         System.err.println(e);
         return false;
       }
       if( nequal==0 || nequal>4)
         return false;
       this.getParameter(2).setValue(choices.elementAt(nequal+15));
       return true;
     }else{
       return false;
     }
     return true;
     }*/

  /**
   * Read in result from STDIN.
   */

  /*private static String readans(){
     char c=0;
     String Res="";
     try{
       c =(char) System.in.read();
       while( c >=32){
         Res+=c;
         c =(char) System.in.read();
       }
     }catch(Exception ss){
       return Res;
     }
     return Res;
     }*/

  /**
   * A method to make printing arrays easier
   */
  private static String arrayToString( Object array ) {
    if( array == null ) {
      return null;
    }

    StringBuffer result = new StringBuffer( "[" );

    if( array instanceof int[] ) {
      for( int i = 0; i < ( ( int[] )array ).length; i++ ) {
        result.append( ( ( int[] )array )[i] + ", " );
      }
    } else if( array instanceof int[][] ) {
      for( int i = 0; i < ( ( int[][] )array ).length; i++ ) {
        result.append( arrayToString( ( ( int[][] )array )[i] ) + ", " );
      }
    } else if( array instanceof float[] ) {
      for( int i = 0; i < ( ( float[] )array ).length; i++ ) {
        result.append( ( ( float[] )array )[i] + ", " );
      }
    } else if( array instanceof double[] ) {
      for( int i = 0; i < ( ( double[] )array ).length; i++ ) {
        result.append( ( ( double[] )array )[i] + ", " );
      }
    } else if( array instanceof double[][] ) {
      for( int i = 0; i < ( ( double[][] )array ).length; i++ ) {
        result.append( arrayToString( ( ( double[][] )array )[i] ) + ", " );
      }
    } else {
      return null;
    }

    result.delete( result.length(  ) - 2, result.length(  ) );
    result.append( "]" );

    return result.toString(  );
  }

  /**
   * This search tries to find the value in the provided <U>ORDERED</U> array.
   * If the value does not appear in an index it returns -1.
   */
  private static int binsearch( int[] array, int value ) {
    // check for impossibles
    if( ( array == null ) || ( array.length <= 0 ) ) {
      return -1;
    }

    if( value < array[0] ) {
      return -1;
    }

    if( value > array[array.length - 1] ) {
      return -1;
    }

    // set up indices
    int first = 0;
    int last  = array.length - 1;
    int index = ( int )( ( last + first ) / 2 );

    // do the search
    while( first < last ) {
      if( array[index] == value ) {
        return index;
      } else if( array[index] < value ) {
        first = index + 1;
      } else if( array[index] > value ) {
        last = index - 1;
      }

      index = ( int )( ( last + first ) / 2 );
    }

    if( array[index] == value ) {  // check where it ended

      return index;
    } else {  // or return not found

      return -1;
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param array DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  private static float[][] double2float( double[][] array ) {
    return LinearAlgebra.double2float( array );
  }

  /**
   * DOCUMENT ME!
   *
   * @param array DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  private static float[] double2float( double[] array ) {
    return LinearAlgebra.double2float( ( double[] )array );
  }

  /**
   * Method to generate the hkl sums matrix
   */
  private static double[][] generateVC( Vector peaks ) {
    if( ( peaks == null ) || ( peaks.size(  ) <= 0 ) ) {
      return null;
    }

    double[][] VC = new double[3][3];
    Peak peak     = null;

    double[] hkl = new double[3];

    // find the sum
    for( int i = 0; i < peaks.size(  ); i++ ) {
      peak     = ( Peak )peaks.elementAt( i );
      hkl[0]   = Math.round( peak.h(  ) );
      hkl[1]   = Math.round( peak.k(  ) );
      hkl[2]   = Math.round( peak.l(  ) );

      for( int j = 0; j < 3; j++ ) {
        for( int k = 0; k < 3; k++ ) {
          VC[j][k] = VC[j][k] + ( hkl[j] * hkl[k] );
        }
      }
    }

    // find the inverse of VC
    if( LinearAlgebra.invert( VC ) ) {
      return VC;
    } else {
      return null;
    }
  }

  /**
   * This takes a String representing a 3x3 matrix and turns it into a
   * float[3][3].
   *
   * @param text A String to be converted
   *
   * @return The matrix as a float[3][3]
   */
  private static float[][] stringTo2dArray( String text )
    throws NumberFormatException {
    // check that there is something to parse
    if( ( text == null ) || ( text.length(  ) == 0 ) ) {
      return null;
    }

    // now take up some memory
    float[][] matrix = new float[3][3];
    int index;
    float temp;

    // start with a StringBuffer b/c they are nicer to parse
    StringBuffer sb = new StringBuffer( text );

    sb.delete( 0, 2 );

    try {
      // repeat for each row
      for( int i = 0; i < 3; i++ ) {
        // parse the first two columns which are ended by ','
        for( int j = 0; j < 2; j++ ) {
          index = sb.toString(  )
                    .indexOf( "," );

          if( index > 0 ) {
            temp = Float.parseFloat( sb.substring( 0, index ) );
            sb.delete( 0, index + 1 );
            matrix[i][j] = temp;
          } else {
            return null;
          }
        }

        // the third column is ended by ']'
        index = sb.toString(  )
                  .indexOf( "]" );

        if( index > 0 ) {
          temp = Float.parseFloat( sb.substring( 0, index ) );
          sb.delete( 0, index + 2 );
          matrix[i][2] = temp;
        } else {
          return null;
        }
      }
    } catch( NumberFormatException e ) {
      // something went wrong so exit out
      throw e;
    }

    // if it is the identity matrix then we should just return null
    boolean isident = true;

    for( int i = 0; i < 3; i++ ) {
      if( isident ) {  // breakout if it is not the identity matrix

        for( int j = 0; j < 3; j++ ) {
          if( i == j ) {  // should be one

            if( matrix[i][j] != 1f ) {
              isident = false;

              break;
            }
          } else {  // should be zero

            if( matrix[i][j] != 0f ) {
              isident = false;

              break;
            }
          }
        }
      }
    }

    if( isident ) {
      return null;
    }

    // return the result
    return matrix;
  }

  /**
   * Print the orientation matrix and lattice parameters to the console.
   */
  private static void toConsole( double[][] UB, double[] abc, double[] sig_abc ) {
    // print the UB matrix
    if( UB == null ) {
      return;
    }

    StringBuffer sb = new StringBuffer( ( 31 * 3 ) + ( 71 * 2 ) );

    for( int i = 0; i < 3; i++ ) {
      for( int j = 0; j < 3; j++ ) {
        sb.append( Format.real( UB[j][i], 10, 6 ) );
      }

      sb.append( "\n" );
    }

    // print the lattice parameters
    if( abc == null ) {
      SharedData.addmsg( sb.toString(  ) );

      return;
    }

    for( int i = 0; i < 7; i++ ) {
      sb.append( Format.real( abc[i], 10, 3 ) );
    }

    sb.append( "\n" );

    // print the uncertainties for lattice parameters
    if( sig_abc == null ) {
      SharedData.addmsg( sb.toString(  ) );

      return;
    }

    for( int i = 0; i < 7; i++ ) {
      sb.append( Format.real( sig_abc[i], 10, 3 ) );
    }

    sb.append( "\n" );

    SharedData.addmsg( sb.toString(  ) );
  }

  /**
   *
   */
  private static String writeLog( String logfile, String log ) {
    FileOutputStream fout = null;

    try {
      fout = new FileOutputStream( logfile );
      fout.write( log.getBytes(  ) );
      fout.flush(  );
    } catch( IOException e ) {
      return e.toString(  );
    } finally {
      if( fout != null ) {
        try {
          fout.close(  );
        } catch( IOException e ) {
          // let it drop on the floor
        }
      }
    }

    return null;
  }
}
