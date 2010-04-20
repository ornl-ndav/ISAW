/*
 * File:  LsqrsJ_base.java
  *
 * Copyright (C) 2004, Ruth Mikkelson,Peter F. Peterson
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
 
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 *
 * $Log$
 * Revision 1.15  2008/01/29 19:50:08  rmikk
 * Replaced Peak by IPeak
 *
 * Revision 1.14  2007/05/03 14:42:19  rmikk
 * Fixed JavaDoc errors
 *
 * Revision 1.13  2007/02/25 20:47:25  rmikk
 * Fixed to use the error matrix from BestFitMatrix for the constrained least
 *    squares.
 *
 * Extracted out a public static method that can easily be called from Java.
 *
 * Revision 1.12  2006/07/10 22:10:21  dennis
 * Removed unused imports after refactoring to use new Parameter GUIs
 * in gov.anl.ipns.Parameters.
 *
 * Revision 1.11  2006/07/10 16:26:12  dennis
 * Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.10  2006/06/16 18:17:20  rmikk
 * Now writes log information to the logging file at this time
 *
 * Revision 1.9  2006/01/17 22:42:35  rmikk
 * Set the UB matrix to null and hkl values to zero for peaks that are not
 *   indexed because they are not in selected runs or sequences
 *
 * Revision 1.8  2006/01/16 04:47:22  rmikk
 * Cloned the peak argument
 * Added the LeastSquare constraints
 *
 * Revision 1.7  2005/12/29 20:22:22  dennis
 * Replaced 'chisq == Double.isNaN'  with  'Double.isNaN(chisq)'
 * The original form does NOT properly check for the returned chisq
 * value being the error return flag, NaN.  This fix will not affect
 * the action of the operator, when everything is working correctly,
 * but it will now return a meaningful error string, if the data is
 * invalid.
 *
 * Revision 1.6  2005/08/06 21:58:00  rmikk
 * ReWrote so that no peaks objects are removed
 *
 * Revision 1.5  2005/08/05 20:21:36  rmikk
 * Fixed documentation to make parameters clearer and advertise that some
 * peaks may disappear.
 *
 * Revision 1.4  2005/06/20 00:44:56  rmikk
 * IMproved javadocs
 *
 * Revision 1.3  2005/01/04 17:02:11  rmikk
 * Implements HiddenOperator.  The result is not a String or a DataSet
 *
 * Revision 1.2  2004/07/31 23:08:24  rmikk
 * Removed unused imports
 *
 * Revision 1.1  2004/07/14 16:27:30  rmikk
 * Initial Checkin
 * LsqrsJ with peaks vector input and orientation matrix output
 *
 */
package Operators.TOF_SCD;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.Parameters.ChoiceListPG;
import gov.anl.ipns.Parameters.IParameter;
import gov.anl.ipns.Parameters.IntArrayPG;
import gov.anl.ipns.Parameters.IntegerPG;
import gov.anl.ipns.Parameters.PlaceHolderPG;
import gov.anl.ipns.Parameters.SaveFilePG;
import gov.anl.ipns.Parameters.StringPG;
import gov.anl.ipns.Util.Numeric.Format;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;


import java.util.*;
import java.io.*;

import DataSetTools.operator.Generic.TOF_SCD.GenericTOF_SCD;
import DataSetTools.operator.Generic.TOF_SCD.MatrixFilter;
import DataSetTools.operator.Generic.TOF_SCD.IPeak;
import DataSetTools.operator.Generic.TOF_SCD.Peak;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new_IO;
import DataSetTools.operator.Generic.TOF_SCD.Util;
import DataSetTools.util.FilenameUtil;
import DataSetTools.util.SharedData;
import DataSetTools.trial.*;


/**
 *  Finds the orientation matrix that is the best fit to the peak
 * positions and indexed h,k,l values for the peaks.
 * Input parameters:
 *     A Vector of Peaks objects
 *     String form(IntList) for the restricted run numbers 
 *     String form(IntList) for the restricted sequence numbers 
 *     String form for the Transformed Matrix
 *     FileName to save result to
 *     Integer value for the minimum peaks threshold 
 *     String(IntList) form for the Row's and Column's to keep
 */
public class LsqrsJ_base extends GenericTOF_SCD implements 
                           DataSetTools.operator.HiddenOperator{
  //~ Static fields/initializers ***********************************************

  /**
    * 
    */
   private static final long serialVersionUID = 1L;
private static final double SMALL    = 1.525878906E-5;
  private static final String identmat = "[[1,0,0][0,1,0][0,0,1]]";
  private static boolean first = false;

  //~ Constructors *************************************************************

  /**
   * Construct an operator with a default parameter list.
   */
  public LsqrsJ_base(  ) {
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

    
    //0
    addParameter( new PlaceHolderPG("Peaks Vector", new Vector()));

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
      new IntegerPG( "Minimum Peak Intensity Threshold", new Integer(0) ) );

    //6
    addParameter( 
      new IntArrayPG( "Pixel Rows and Columns to Keep", "0:100" ) );

    ChoiceListPG choices = new ChoiceListPG("Cell Type Constraint","Triclinic");

    choices.addItem("Monoclinic ( b unique )");
    choices.addItem("Monoclinic ( a unique )");
    choices.addItem("Monoclinic ( c unique )");
    choices.addItem("Orthorhombic");
    choices.addItem("Tetragonal");
    choices.addItem("Rhombohedral");
    choices.addItem("Hexagonal");
    choices.addItem("Cubic");
       
    //7   
    addParameter( choices) ;   

  }

  /**
   * This returns the help documentation
   */
  public String getDocumentation(  ) {
    StringBuffer sb = new StringBuffer( 80 * 5 );

    // overview
    sb.append( "@overview This operator uses a standard Least Squares method" );
    sb.append( "to find the best fit.  Some of the errors are for code by ");
    sb.append( "A.J.Schultz and originally written by J.Marc Overhage in 1979."); 

    //assumptions
    sb.append( "@assumptions The peaks vector exists and the transformation " );
    sb.append( "matrix is valid." );

    //algorithm
    sb.append( "@algorithm The peaks that are not within the selected sequence numbers, " );
    sb.append( "not within the h, k, and l delta values, and not within the " );
    sb.append( "selected histogram are trimmed out.  Next the hkl and q " );
    sb.append( "matrix are created, and the transformation matrix is " );
    sb.append( "applied.  The UB matrix is calculated, and lattice " );
    sb.append( "parameters and cell volume are calculated.  Next the " );
    sb.append( "uncertainties and derivatives are calculated, and the sigmas ");
    sb.append( "are accumulated and turned into \"actual\" sigmas.  At this " );
    sb.append( "point, the log file and matrix file are written and/or " );
    sb.append( "updated." );

    // parameters
    sb.append( "@param peaks  Vector of peaks. A copy is made and used. ");
    sb.append(  "This input parameter is NOT changed" );
    sb.append( "@param  Runs The run numbers to use " );
    sb.append( "@param restrictSeq The sequence numbers to use. " );
    sb.append( "@param xFormMat The transformation matrix to use. " );
    sb.append( "@param matFile The matrix to write to. " );
    sb.append( "@param minThresh The minimum peak intensity threshold to " );
    sb.append( "use." );
    sb.append( "@param keepPixels The detector pixel range to keep." );
    sb.append( "@param cellType  The type of cell to be used if the ");
    sb.append( "@param least squares optimization is to be constrained ");
    sb.append(  "to a particular unit cell type");
    // return
    sb.append( "@return the resultant matrix file or an errormessage.");
    sb.append(" The peaks object is also indexed and some peaks may be deleted");

    // error
   
    sb.append( 
      "@error If the matrix file  cannot be written to." );

    return sb.toString(  );
  }

  /**
   * Uses the current values of the parameters to generate a result.
   */
  public Object getResult(  ) {
    // get the parameters
    try{
    
     Vector peaksPar = (Vector)(getParameter( 0 ).getValue(  ));
     int[] run_nums   = ( ( IntArrayPG )getParameter( 1 ) ).getArrayValue(  );
     int[] seq_nums   = ( ( IntArrayPG )getParameter( 2 ) ).getArrayValue(  );
     
       
     int threshold    = ( ( IntegerPG )getParameter( 5 ) ).getintValue(  );
     int[] keepRange  = ( ( IntArrayPG )getParameter( 6 ) ).getArrayValue(  );
     String cellType  = ((ChoiceListPG)getParameter( 7 )).getValue().toString();

     float[][] matrix = null;
     IParameter iparm = getParameter( 3 );
     if( iparm.getValue(  ) == null ) {
        matrix = null;
      } else {
        try {
          matrix = stringTo2dArray( iparm.getValue(  ).toString(  ) );
        } catch( NumberFormatException e ) {
          return new ErrorString( "Improper format in matrix" );
        }
        if( LinearAlgebra.determinant( matrix ) <= 0)
           return new ErrorString(
                 "Transformation Matrix has Negative Determinant");
      }
     String matfile = getParameter( 4 )
                               .getValue(  )
                              .toString(  );
     Object Res = LsqrsJ1( peaksPar, run_nums,seq_nums,matrix,matfile, threshold, 
                 keepRange, cellType);
     return Res;
    }catch( Exception xx){
       xx.printStackTrace();
       return new ErrorString(xx);
    }
  }

  /**
   * Public static method for the base Least Squares operators and forms.
   * Finds the least square matrix mapping the hkl values to the qx,qy,qz
   * values. Returns this matrix or an error message
   * 
   * @param peaksPar  The Vector of Peaks( will not be changed)
   * @param  run_nums The run numbers to use 
   * @param seq_nums The sequence numbers to use. 
   * @param matrix The transformation matrix to use. 
   * @param threshold The minimum peak intensity threshold to 
   *          use.
   * @param keepRange The detector pixel range to keep.
   * @param cellType  The type of cell to be used if the 
   *              least squares optimization is to be constrained
   *             to a particular unit cel1 l type
  
   * @return  the optimized orientation matrix or an errormessage.
   */
  public static Object LsqrsJ( Vector peaksPar, int[] run_nums, int[] seq_nums,
                 float[][] matrix ,String logfile,int threshold, int[] keepRange,
                 String cellType)
  {
     Vector peaks =new Vector();
     for( int i=0; i< peaksPar.size(); i++)
     {
       IPeak old_peak = (IPeak)peaksPar.elementAt(i);
       IPeak new_peak = old_peak.createNewPeakxyz( old_peak.x(), 
                                                   old_peak.y(),
                                                   old_peak.z(), 
                                                   old_peak.time() );
       peaks.addElement( new_peak );
     }  
     
     if ( run_nums != null && run_nums.length < 1 )
        run_nums = null;
      if ( seq_nums != null && seq_nums.length < 1 )
        seq_nums = null;
      
     int lowerLimit;
     int upperLimit;
     double[] sig_abc = null;
     if( keepRange != null ) {
       lowerLimit   = keepRange[0];  //lower limit of range

       //upper limit of range
       upperLimit = keepRange[keepRange.length - 1];
     } else {  //shouldn't happen, but default to 0:MAX_VALUE
       lowerLimit   = 0;
       upperLimit   = Integer.MAX_VALUE;
     }

     
     //-------------------cut
     StringBuffer logBuffer = new StringBuffer(  );

     //logBuffer.append( "Peaks file  = " + peaksfile + "\n" );
    
     logBuffer.append( "run#        = " + arrayToString( run_nums ) + "\n" );
     logBuffer.append( "seq#        = " + arrayToString( seq_nums ) + "\n" );

     if( matrix != null ) {
       logBuffer.append( "     " );

       for( int i = 0; i < 3; i++ ) {
         logBuffer.append( Format.real( matrix[0][i], 3 ) + " " );
       }

       logBuffer.append( "\n" + "Tr = " );

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
     
      IPeak peak = null;
     int[] keep = new int[peaks.size()];
     java.util.Arrays.fill(keep ,0);
     int nargs= peaks.size();
     // trim out the peaks that are not in the list of selected sequence numbers
     if( seq_nums != null ) {
       for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
         peak = ( IPeak )peaks.elementAt( i );

         if( binsearch( seq_nums, peak.seqnum(  ) ) == -1 ) {
            
           keep[i] = -1;
           nargs--;           
         }
       }
     }

     // trim out the peaks without hkl listed
     for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
       peak = ( IPeak )peaks.elementAt( i );
       //peak.UB(matrix);
       if(keep[i] ==0)
       if( ( peak.h(  ) == 0 ) && ( peak.k(  ) == 0 ) && ( peak.l(  ) == 0 ) ) {
        
           keep[i] = -1;
           nargs--;
       }
     }

     // trim out the ones that are not in the selected histograms
     if( run_nums != null ) {
       for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
         peak = ( IPeak )peaks.elementAt( i );
         if(keep[i] ==0)
         if( binsearch( run_nums, peak.nrun(  ) ) == -1 ) {          
           
             keep[i] = -1;
             nargs--;         
         }
       }
     }

     // trim out small peaks (defined by the threshold parameter)
     if( threshold >= 0 ) {
       for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
         peak = ( IPeak )peaks.elementAt( i );
         if(keep[i] ==0)
         if( peak.ipkobs(  ) < threshold ) {
         
             keep[i] = -1;
             nargs--;
         }
       }
     }

     // trim out edge peaks (defined by the "pixels to keep" parameter)
     for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
       peak = ( IPeak )peaks.elementAt( i );

       //see if the peak pixels are within the user defined array.  We are
       //assuming a SQUARE detector, so we'll reject it if the x or y position
       //is not within our range
       if(keep[i] ==0)
       if( ( peak.x(  ) > upperLimit ) || ( peak.x(  ) < lowerLimit ) ||
           ( peak.y(  ) > upperLimit ) || ( peak.y(  ) < lowerLimit ) ) {
         
               keep[i] = -1;
               nargs--;
       }
     }

     // can't refine nothing
     if( nargs<= 0 ) {
       return new ErrorString( "No peaks to refine" );
     }

     // can't do a least squares fit without at least 3 points
     if( nargs < 3 ) {
       return new ErrorString( "Only " + peaks.size() + " peaks to fit in " +
                               "LsqrsJ, need at least 3" );
     }


     // create the hkl-matrix and q-matrix (q=1/d)
     double[][] q      = new double[ nargs ][ 3 ];
     double[][] hkl    = new double[ nargs ][ 3 ];
     float[]    unrotQ = new float[3];
     int k=0;
     for( int i = 0; i < peaks.size(); i++ ) 
     if( keep[i]==0){
       peak        = ( IPeak )peaks.elementAt( i );
       hkl[k][0]   = Math.round( peak.h(  ) );
       hkl[k][1]   = Math.round( peak.k(  ) );
       hkl[k][2]   = Math.round( peak.l(  ) );
       unrotQ      = peak.getUnrotQ();
       for ( int component = 0; component < 3; component++ )
         q[k][component] = unrotQ[component];
       k++;
     }else{
        peak =( IPeak )peaks.elementAt( i );
        peak.UB(null);
        if( peak instanceof Peak)
          ((Peak)peak).sethkl(0f,0f,0f,false);
        else
           peak.sethkl(0f,0f,0f);
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
           for(  k = 0; k < 3; k++ ) {
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
     k=0;
     for( int i = 0; i < peaks.size(); i++ )
     if(keep[i]==0) {
       peak = ( IPeak )peaks.elementAt( i );
      
       peak.UB(null);
       if( peak instanceof Peak)
        ((Peak) peak).sethkl((float)hkl[k][0], (float)hkl[k][1], (float)hkl[k][2]
                  );
       else
          peak.sethkl((float)hkl[k][0], (float)hkl[k][1], (float)hkl[k][2]);
       k++;
     }

     // calculate ub
     double[][] UB = new double[3][3];
     double chisq  = 0.;
     
       double[][] Thkl = new double[peaks.size(  )][3];
       double[][] Tq   = new double[peaks.size(  )][3];

       for( int i = 0; i < hkl.length; i++ ) {
         for( int j = 0; j < 3; j++ ) {
           Thkl[i][j]   = hkl[i][j];
           Tq[i][j]     = q[i][j];
         }
       }

       System.out.println("CellType is "+cellType);
       if ( cellType.startsWith( "Tri" ) )
         chisq = LinearAlgebra.BestFitMatrix( UB, Thkl, Tq );
       else{
          sig_abc= new double[7];
         chisq = SCD_util.BestFitMatrix( cellType, UB, Thkl, Tq ,sig_abc);
       }


       if ( Double.isNaN( chisq ) )
         return new ErrorString( "ERROR in LsqrsJ: " + 
                                 " BestFitMatrix calculation failed" );
       chisq   = 0.;  // reset chisq
       Thkl    = new double[3][nargs];

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
         " seq#   h     k     l      x      y       z      " +
         "xcm    ycm      wl  Iobs    Qx     Qy     Qz\n" );
       k=0;
       for( int i = 0; i < peaks.size(  ); i++ ) 
       if(keep[i]==0){
         peak = (IPeak)peaks.elementAt(i);
   
                         // The first line logged for a peak has the observered
                         // values for the peak, 'indexed' by integer hkl values.
         logBuffer.append( Format.integer( peak.seqnum(), 5)+" " +
                           Format.integer( peak.h(), 3 )+" " +
                           Format.integer( peak.k(), 5 )+" " +
                           Format.integer( peak.l(), 5 )+" " +
                           Format.real( peak.x(),   8, 2 )+" " +
                           Format.real( peak.y(),   6, 2 )+" " +
                           Format.real( peak.z(),   7, 2 )+" " +
                           Format.real( peak.xcm(), 6, 2 )+" " +
                           Format.real( peak.ycm(), 6, 2 )+" " +
                           Format.real( peak.wl(),  7, 4 )+" " +
                           Format.integer(  peak.ipkobs(), 5 )+" " +
                           Format.real( peak.getUnrotQ()[0], 7, 3 )+" " +
                           Format.real( peak.getUnrotQ()[1], 6, 3 )+" " + 
                           Format.real( peak.getUnrotQ()[2], 6, 3 )+"\n" );

                   logBuffer.append( Format.string("",73) +
                           Format.real( Tq[0][k], 6, 3 )+" " + 
                           Format.real( Tq[1][k], 6, 3 )+" " +
                           Format.real( Tq[2][k], 6, 3 ) + "\n" );

                           // The third line logged has the fractional hkl
                           // values observed for a peak, together with the
                           // difference in theoretical and observed hkl
         logBuffer.append( "      " + 
                           Format.real( obs_hkl[0][k], 6, 2 )+" " +
                           Format.real( obs_hkl[1][k], 5, 2 )+" " + 
                           Format.real( obs_hkl[2][k], 5, 2 )+" "  );

         double error = Math.abs( obs_hkl[0][k] - peak.h() ) +
                        Math.abs( obs_hkl[1][k] - peak.k() ) +
                        Math.abs( obs_hkl[2][k] - peak.l() );

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
         k++;
       }

       // calculate 
       for( int i = 0; i < nargs; i++ ) {
         for( int j = 0; j < 3; j++ ) {
           chisq = chisq + ( ( q[i][j] - Tq[j][i] ) * ( q[i][j] - Tq[j][i] ) );
         }
       }
     

     // add chisq to the logBuffer
     logBuffer.append( 
       "\nchisq[Qobs-Qexp]: " + Format.real( chisq, 8, 5 ) + "\n" );

     // calculate lattice parameters and cell volume
     double[] abc = Util.abc( UB );

     // determine uncertainties
    
     if(sig_abc == null){
        sig_abc = new double[7];
     
       double numFreedom      = 3. * ( nargs - 3. );
       double[] temp_abc      = null;
       double[][] derivatives = new double[3][7];
       double[][] VC          = generateVC( peaks ,keep);

       for( int i = 0; i < 3; i++ ) {
         // determine derivatives
         for( int j = 0; j < 3; j++ ) {
           UB[i][j]   = UB[i][j] + SMALL;
           temp_abc   = Util.abc( UB );
           UB[i][j]   = UB[i][j] - SMALL;

           for(  k = 0; k < 7; k++ ) {
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
         logBuffer.append( Format.real( UB[j][i], 9, 6 ) +" ");
       }

       logBuffer.append( "\n" );
     }

     logBuffer.append( "\n" );
     logBuffer.append( "Lattice parameters:\n" );

     for( int i = 0; i < 7; i++ ) {
       logBuffer.append( Format.real( abc[i], 9, 3 )+" " );
     }

     logBuffer.append( "\n" );

     for( int i = 0; i < 7; i++ ) {
       logBuffer.append( Format.real( sig_abc[i], 9, 3 )+" " );
     }

     logBuffer.append( "\n" );

     // print out the results
     toConsole( UB, abc, sig_abc );

     // write the log file
     if( logfile == null)
       logfile = null;
     
     
       String warn = writeLog( logfile, logBuffer.toString(  ) );

       if( ( warn != null ) && ( warn.length(  ) > 0 ) ) {
         SharedData.addmsg( "JLsqrs(WARN) while writting lsqrs.log: " + warn );
       } else {
         SharedData.addmsg( "Wrote log file: " + logfile + "." );
       }
     

       float[][] F_UB=LinearAlgebra.double2float( UB );
       return F_UB;
   //-----cut
  }
  
    /**
     * Public static method for the base Least Squares operators and forms.
     * Finds the least square matrix mapping the hkl values to the qx,qy,qz
     * values. Writes the optimized matrix to a file.
     * 
     * @param peaksPar  The Vector of Peaks( will not be changed)
     * @param  run_nums The run numbers to use 
     * @param seq_nums The sequence numbers to use. 
     * @param matrix The transformation matrix to use. 
     * @param matfile The matrix to write to. 
     * @param threshold The minimum peak intensity threshold to 
     *          use.
     * @param keepRange The detector pixel range to keep.
     * @param cellType  The type of cell to be used if the 
     *              least squares optimization is to be constrained
     *             to a particular unit cel l type
    
     * @return  the  matrix file has the orientation matrix or an
     *              errormessage.
     */
  public static Object LsqrsJ1( Vector peaksPar, int[] run_nums, int[] seq_nums,
        float[][] matrix, String matfile, int threshold, int[] keepRange,
        String cellType)
  
   {
     return LsqrsJ1(peaksPar, run_nums, seq_nums,
        matrix, matfile, threshold,  keepRange,
        cellType, null);
   }
  
     public static Object LsqrsJ1( Vector peaksPar, int[] run_nums, int[] seq_nums,
                   float[][] matrix, String matfile, int threshold, int[] keepRange,
                   String cellType, double[] sig_abc){

     Vector peaks =new Vector();
     if( sig_abc == null || sig_abc.length < 1)
        sig_abc = null;
     else
        sig_abc[0] = -1;
     for( int i=0; i< peaksPar.size(); i++)
     {
       IPeak old_peak = (IPeak)peaksPar.elementAt(i);
       IPeak new_peak = old_peak.createNewPeakxyz( old_peak.x(), 
                                                   old_peak.y(),
                                                   old_peak.z(), 
                                                   old_peak.time() );
       peaks.addElement( new_peak );
     }  
     
     if ( run_nums != null && run_nums.length < 1 )
        run_nums = null;
      if ( seq_nums != null && seq_nums.length < 1 )
        seq_nums = null;
      
     int lowerLimit;
     int upperLimit;
     if( sig_abc == null)
         sig_abc = new double[7];
     if( keepRange != null ) {
       lowerLimit   = keepRange[0];  //lower limit of range

       //upper limit of range
       upperLimit = keepRange[keepRange.length - 1];
     } else {  //shouldn't happen, but default to 0:MAX_VALUE
       lowerLimit   = 0;
       upperLimit   = Integer.MAX_VALUE;
     }

     String logfile;
   
    matfile = FilenameUtil.setForwardSlash( matfile );

    // confirm the parameters
      if( ( matfile == null ) || ( matfile.length(  ) <= 0 ) ) {
        matfile = null;
      } 

      if( matfile == null ) {
       // return new ErrorString( "Matrix file has not been specified" );
      }

    // create a buffer for a log file
    StringBuffer logBuffer = new StringBuffer(  );

    //logBuffer.append( "Peaks file  = " + peaksfile + "\n" );
    if( matfile != null)
       logBuffer.append( "Matrix file = " + matfile + "\n" );
    logBuffer.append( "run#        = " + arrayToString( run_nums ) + "\n" );
    logBuffer.append( "seq#        = " + arrayToString( seq_nums ) + "\n" );

    if( matrix != null ) {
      logBuffer.append( "     " );

      for( int i = 0; i < 3; i++ ) {
        logBuffer.append( Format.real( matrix[0][i], 3 ) + " " );
      }

      logBuffer.append( "\n" + "Tr = " );

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
    
     IPeak peak = null;
    int[] keep = new int[peaks.size()];
    java.util.Arrays.fill(keep ,0);
    int nargs= peaks.size();
    // trim out the peaks that are not in the list of selected sequence numbers
    if( seq_nums != null ) {
      for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
        peak = ( IPeak )peaks.elementAt( i );

        if( binsearch( seq_nums, peak.seqnum(  ) ) == -1 ) {
           
          keep[i] = -1;
          nargs--;           
        }
      }
    }

    // trim out the peaks without hkl listed
    for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
      peak = ( IPeak )peaks.elementAt( i );
      //peak.UB(matrix);
      if(keep[i] ==0)
      if( ( peak.h(  ) == 0 ) && ( peak.k(  ) == 0 ) && ( peak.l(  ) == 0 ) ) {
       
          keep[i] = -1;
          nargs--;
      }
    }

    // trim out the ones that are not in the selected histograms
    if( run_nums != null ) {
      for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
        peak = ( IPeak )peaks.elementAt( i );
        if(keep[i] ==0)
        if( binsearch( run_nums, peak.nrun(  ) ) == -1 ) {          
          
            keep[i] = -1;
            nargs--;         
        }
      }
    }

    // trim out small peaks (defined by the threshold parameter)
    if( threshold >= 0 ) {
      for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
        peak = ( IPeak )peaks.elementAt( i );
        if(keep[i] ==0)
        if( peak.ipkobs(  ) < threshold ) {
        
            keep[i] = -1;
            nargs--;
        }
      }
    }

    // trim out edge peaks (defined by the "pixels to keep" parameter)
    for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
      peak = ( IPeak )peaks.elementAt( i );

      //see if the peak pixels are within the user defined array.  We are
      //assuming a SQUARE detector, so we'll reject it if the x or y position
      //is not within our range
      if(keep[i] ==0)
      if( ( peak.x(  ) > upperLimit ) || ( peak.x(  ) < lowerLimit ) ||
          ( peak.y(  ) > upperLimit ) || ( peak.y(  ) < lowerLimit ) ) {
        
              keep[i] = -1;
              nargs--;
      }
    }

    // can't refine nothing
    if( nargs<= 0 ) {
      return new ErrorString( "No peaks to refine" );
    }

    // can't do a least squares fit without at least 3 points
    if( nargs < 3 ) {
      return new ErrorString( "Only " + peaks.size() + " peaks to fit in " +
                              "LsqrsJ, need at least 3" );
    }


    // create the hkl-matrix and q-matrix (q=1/d)
    double[][] q      = new double[ nargs ][ 3 ];
    double[][] hkl    = new double[ nargs ][ 3 ];
    float[]    unrotQ = new float[3];
    int k=0;
    for( int i = 0; i < peaks.size(); i++ ) 
    if( keep[i]==0){
      peak        = ( IPeak )peaks.elementAt( i );
      hkl[k][0]   = Math.round( peak.h(  ) );
      hkl[k][1]   = Math.round( peak.k(  ) );
      hkl[k][2]   = Math.round( peak.l(  ) );
      unrotQ      = peak.getUnrotQ();
      for ( int component = 0; component < 3; component++ )
        q[k][component] = unrotQ[component];
      k++;
    }else{
       peak =( IPeak )peaks.elementAt( i );
       peak.UB(null);
       if( peak instanceof Peak)
         ((Peak)peak).sethkl(0f,0f,0f,false);
       else
          peak.sethkl(0f,0f,0f);
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
          for(  k = 0; k < 3; k++ ) {
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
    k=0;
    for( int i = 0; i < peaks.size(); i++ )
    if(keep[i]==0) {
      peak = ( IPeak )peaks.elementAt( i );
     
      peak.UB(null);
      if( peak instanceof Peak)
       ((Peak) peak).sethkl((float)hkl[k][0], (float)hkl[k][1], (float)hkl[k][2]
                 );
      else
         peak.sethkl((float)hkl[k][0], (float)hkl[k][1], (float)hkl[k][2]);
      k++;
    }

    // calculate ub
    double[][] UB = new double[3][3];
    double chisq  = 0.;
    
      double[][] Thkl = new double[peaks.size(  )][3];
      double[][] Tq   = new double[peaks.size(  )][3];

      for( int i = 0; i < hkl.length; i++ ) {
        for( int j = 0; j < 3; j++ ) {
          Thkl[i][j]   = hkl[i][j];
          Tq[i][j]     = q[i][j];
        }
      }

      System.out.println("CellType is "+cellType);
      if ( cellType.startsWith( "Tri" ) )
        chisq = LinearAlgebra.BestFitMatrix( UB, Thkl, Tq );
      else{
         sig_abc= new double[7];
        chisq = SCD_util.BestFitMatrix( cellType, UB, Thkl, Tq ,sig_abc);
      }


      if ( Double.isNaN( chisq ) )
        return new ErrorString( "ERROR in LsqrsJ: " + 
                                " BestFitMatrix calculation failed" );
      chisq   = 0.;  // reset chisq
      Thkl    = new double[3][nargs];

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
        " seq#   h     k     l      x      y       z      " +
        "xcm    ycm      wl  Iobs    Qx     Qy     Qz\n" );
      k=0;
      for( int i = 0; i < peaks.size(  ); i++ ) 
      if(keep[i]==0){
        peak = (IPeak)peaks.elementAt(i);
  
                        // The first line logged for a peak has the observered
                        // values for the peak, 'indexed' by integer hkl values.
        logBuffer.append( Format.integer( peak.seqnum(), 5)+" " +
                          Format.integer( peak.h(), 3 )+" " +
                          Format.integer( peak.k(), 5 )+" " +
                          Format.integer( peak.l(), 5 )+" " +
                          Format.real( peak.x(),   8, 2 )+" " +
                          Format.real( peak.y(),   6, 2 )+" " +
                          Format.real( peak.z(),   7, 2 )+" " +
                          Format.real( peak.xcm(), 6, 2 )+" " +
                          Format.real( peak.ycm(), 6, 2 )+" " +
                          Format.real( peak.wl(),  7, 4 )+" " +
                          Format.integer(  peak.ipkobs(), 5 )+" " +
                          Format.real( peak.getUnrotQ()[0], 7, 3 )+" " +
                          Format.real( peak.getUnrotQ()[1], 6, 3 )+" " + 
                          Format.real( peak.getUnrotQ()[2], 6, 3 )+"\n" );

                  logBuffer.append( Format.string("",73) +
                          Format.real( Tq[0][k], 6, 3 )+" " + 
                          Format.real( Tq[1][k], 6, 3 )+" " +
                          Format.real( Tq[2][k], 6, 3 ) + "\n" );

                          // The third line logged has the fractional hkl
                          // values observed for a peak, together with the
                          // difference in theoretical and observed hkl
        logBuffer.append( "      " + 
                          Format.real( obs_hkl[0][k], 6, 2 )+" " +
                          Format.real( obs_hkl[1][k], 5, 2 )+" " + 
                          Format.real( obs_hkl[2][k], 5, 2 )+" "  );

        double error = Math.abs( obs_hkl[0][k] - peak.h() ) +
                       Math.abs( obs_hkl[1][k] - peak.k() ) +
                       Math.abs( obs_hkl[2][k] - peak.l() );

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
        k++;
      }

      // calculate 
      for( int i = 0; i < nargs; i++ ) {
        for( int j = 0; j < 3; j++ ) {
          chisq = chisq + ( ( q[i][j] - Tq[j][i] ) * ( q[i][j] - Tq[j][i] ) );
        }
      }
    

    // add chisq to the logBuffer
    logBuffer.append( 
      "\nchisq[Qobs-Qexp]: " + Format.real( chisq, 8, 5 ) + "\n" );

    // calculate lattice parameters and cell volume
    double[] abc = Util.abc( UB );

    // determine uncertainties
   
    if(sig_abc == null || sig_abc[0] < 0){
       sig_abc = new double[7];
    
      double numFreedom      = 3. * ( nargs - 3. );
      double[] temp_abc      = null;
      double[][] derivatives = new double[3][7];
      double[][] VC          = generateVC( peaks ,keep);

      for( int i = 0; i < 3; i++ ) {
        // determine derivatives
        for( int j = 0; j < 3; j++ ) {
          UB[i][j]   = UB[i][j] + SMALL;
          temp_abc   = Util.abc( UB );
          UB[i][j]   = UB[i][j] - SMALL;

          for(  k = 0; k < 7; k++ ) {
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
        logBuffer.append( Format.real( UB[j][i], 9, 6 ) +" ");
      }

      logBuffer.append( "\n" );
    }

    logBuffer.append( "\n" );
    logBuffer.append( "Lattice parameters:\n" );

    for( int i = 0; i < 7; i++ ) {
      logBuffer.append( Format.real( abc[i], 9, 3 )+" " );
    }

    logBuffer.append( "\n" );

    for( int i = 0; i < 7; i++ ) {
      logBuffer.append( Format.real( sig_abc[i], 9, 3 )+" " );
    }

    logBuffer.append( "\n" );

    // print out the results
    toConsole( UB, abc, sig_abc );

    // write the log file
    if( matfile == null)
      logfile = null;
    else
    {
      logfile = "lsqrs.log";

      int index = matfile.lastIndexOf( "/" );

      if( index >= 0 ) {
        logfile = matfile.substring( 0, index + 1 ) + logfile;
      }
    }
    
      String warn = writeLog( logfile, logBuffer.toString(  ) );

      if( ( warn != null ) && ( warn.length(  ) > 0 ) ) {
        SharedData.addmsg( "JLsqrs(WARN) while writting lsqrs.log: " + warn );
      } else {
        SharedData.addmsg( "Wrote log file: " + logfile + "." );
      }
    

    // update the matrix file
    ErrorString error=null;
    if( matfile !=null)
              error = Util.writeMatrix( matfile, 
                                     LinearAlgebra.double2float( UB ), 
                                     LinearAlgebra.double2float( abc ),
                                     LinearAlgebra.double2float( sig_abc ) );
    else 
       SharedData.addmsg("Orientation Matrix\n"+Show(UB)+"\n"+Show(abc)+
              "\n"+Show(sig_abc));

    if( error != null ) {
      return new ErrorString( "LsqrsJ failed to update matrix file: " + error );
    } else if (matfile != null) {
      SharedData.addmsg( "Wrote file: " + matfile );

      float[][] F_UB=LinearAlgebra.double2float( UB );
      return F_UB;
    }
   
    return null;
  }

     
    

     private static double[] ERRmult( double[]M1, double[]errM1,
           double[][]M2, double[][]errM2)
     {
         double[] Res = new double[M2[0].length];
         for( int i=0; i< Res.length; i++)
         {
            for(int k=0; k<M1.length; k++ )
            {
               Res[i] += M1[k]*errM2[k][i]*M1[k]*errM2[k][i]+
                    errM1[k]*M2[k][i]*errM1[k]*M2[k][i];
            }
            Res[i] = Math.sqrt( Res[i] );
         }
         return Res;
         
     }
     private static double[][] ERRmult( double[][]M1, double[][]errM1,
                                        double[][]M2, double[][]errM2)
     {
        double[][] Res = new double[ M1.length][];
        for( int i=0; i< M1.length;i++)
           Res[i] = ERRmult( M1[i],errM1[i], M2, errM2);
        
        return Res;
        
     }
     public static double[][] errorAdd( double[][]M1,double[][]M2, boolean square)
     {
        double[][] Res = new double[M1.length][];
        for( int r =0; r< M1.length; r++)
        {
           Res[r] = new double[M1[r].length];
           for( int c =0; c< M1[r].length; c++)
           {
              double v;
              if( square)

                 v= Math.sqrt( M1[r][c]*M1[r][c] + M2[r][c]* M2[r][c]);
              else
                 v= Math.abs( M1[r][c] + M2[r][c]);
              Res[r][c] =v;
           }
        
        }
        return Res;
     }
     public static double[][] errorMult( double[][]M1,double[][]M2, boolean square)
     {
        double[][] Res = new double[M1.length][];
        for( int r =0; r< M1.length; r++)
        {
           Res[r] = new double[M2[0].length];
           for( int c=0; c<Res.length; c++)
           {
              
           for( int k=0; k< Res.length;k++)
           {
              double V = M1[r][k]*M2[k][c];
              if( square)
                 V = V*V;
              Res[r][c] += Math.abs(V);
           }
           Res[r][c] = Math.sqrt( Res[r][c]);
        }
        }
        return Res;
     }
/*
     private static double[] errorMult( double[][]M1,double[]M2)
     {
        double[] Res = new double[M1.length];
        for( int i=0; i< Res.length; i++)
        {
           for( int k=0; k < M2.length ; k++)
              Res[i] += M1[i][k]*M2[k]*M1[i][k]*M2[k];
           Res[i] = Math.sqrt( Res[i] );
        }
        return Res;
     }

     private static double[] errorMult( double[]M1,double[][]M2)
     {
        double[] Res = new double[M2[0].length];
        for( int i=0; i< Res.length; i++)
        {
           for( int k=0; k < M2.length ; k++)
              Res[i] += M1[k]*M2[k][i]*M1[k]*M2[k][i];
           
           Res[i] = Math.sqrt( Res[i] );
        }
        return Res;
     }

     private static double errorMult( double[]M1,double[]M2)
     {
        double Res=0;
        for( int k=0; k < M2.length ; k++)
           Res += M1[k]*M2[k]*M1[k]*M2[k];
        Res = Math.sqrt( Res );
        return Res;
     }
  */   
     /**
      * Returns a 3*peaksSize array of hkl values
      * @param peaks The list of indexed peaks.
      * @Transform  The matrix to transform the hkl values by.
      * 
      * @return
      */
     public static double[][] getHKLArrays( Vector<IPeak> peaks,float[][]Transform,
           float MinIpkObs,
           int[] OmitSeqNums, int[] OmitRunNums, int n2bEdge)
     {
        
        double[][]hkl = new double[3][peaks.size()];
        int N=0;
        for( int i=0; i< peaks.size( );i++)
        {
           IPeak peak = peaks.get( i );
           boolean omit =omitPeak(peak, MinIpkObs,
                    OmitSeqNums, OmitRunNums,  n2bEdge);
           if( !omit)
           {
           hkl[0][N]= Math.floor( .5+peak.h( ));
           hkl[1][N]= Math.floor( .5+peak.k( ));
           hkl[2][N]= Math.floor( .5+peak.l( ));
           N++;
           }
        }

        double[][] HKL = new double[3][N];
        System.arraycopy( hkl[0],0, HKL[0] , 0 , N );
        System.arraycopy( hkl[1],0, HKL[1] , 0 , N );
        System.arraycopy( hkl[2],0, HKL[2] , 0 , N );
        if( Transform == null)
           return HKL;
        
        
        
        float[] myhkl = new float[3];

        for( int i = 0; i < N; i++ ) {
          // zero out the temp values
          for( int j = 0; j < 3; j++ ) {
            myhkl[j] = 0f;
          }

          // multiply by the transformation matrix
          for( int j = 0; j < 3; j++ ) {
            for( int  k = 0; k < 3; k++ ) {
              myhkl[k] = myhkl[k] + ( Transform[k][j] * ( float )hkl[i][j] );
            }
          }
        
          // copy back the temp values
          for( int j = 0; j < 3; j++ ) {
            HKL[i][j] = Math.round( myhkl[j] );
          }
        }
        
        return      HKL;
     }
     
    
     /**
      * Returns a 3*peaksSize array of q values that have been indexed
      * and have a max
      * @param peaks
      * @return
      */
     public static double[][] getQArray( Vector<IPeak> peaks, float MinIpkObs,
                    int[] OmitSeqNums, int[] OmitRunNums, int n2bEdge)
     {
       
        if( OmitSeqNums != null)
           Arrays.sort( OmitSeqNums );
        else
           OmitSeqNums = new int[0];
        
        if( OmitRunNums != null)
           Arrays.sort(  OmitRunNums );
        else
           OmitRunNums = new int[0];
        
        
        int N=0;
        double[][]q = new double[3][peaks.size()];
        for( int i=0; i< peaks.size( );i++)
        {
           IPeak peak = peaks.get( i );
           boolean omit = omitPeak( peak,MinIpkObs,
                     OmitSeqNums,  OmitRunNums,  n2bEdge);
         if ( !omit )
         {
            float[] qi = peak.getUnrotQ( );
            q[0][N] = qi[0];
            q[1][N] = qi[1];
            q[2][N] = qi[2];
            N++ ;
           }
        }
        double[][]Resq = new double[3][N];
        System.arraycopy( q[0] , 0 , Resq[0] , 0 , N );
        System.arraycopy( q[1] , 0 , Resq [1], 0 , N );
        System.arraycopy( q[2] , 0 , Resq[2] , 0 , N );
        return Resq;
     }
     
     private static boolean omitPeak( IPeak peak, float MinIpkObs,
                    int[] OmitSeqNums, int[] OmitRunNums, int n2bEdge)
     {
        if( peak == null)
           return true;
        
        int run = peak.nrun( );
        int seq = peak.seqnum( );
        
        boolean omit = peak.ipkobs( )<MinIpkObs;
        
        if( !omit && peak.nearedge( )<n2bEdge)
           omit= true;
        
        if( peak.h() ==0 && peak.k() == 0 && peak.l() == 0)
           omit = true;
        
        if( !omit && OmitSeqNums != null)
       
           if( Arrays.binarySearch( OmitSeqNums , seq )>=0)
              omit = true;
        

        if( !omit && OmitRunNums != null)
       
           if( Arrays.binarySearch( OmitRunNums , run )>=0)
              omit = true;
        
        
        
        return omit;
     }
     /**
      * Untested
      * @param UB
      * @param peaks
      * @param keep
      * @param logBuffer
      * @param chisq
      * @param abc
      * @param sig_abc
      * @param cellType
      * @return
      */
     public static Object ShowLogInfo( double[][]UB,  
                                     Vector<Peak_new>peaks,
                                     int[]keep, 
                                     StringBuffer logBuffer,
                                     double chisq, 
                                     double[] abc, 
                                     double[] sig_abc,
                                     String cellType)
     {
        System.out.println("CellType is "+cellType);
       

        boolean hasLog = true;
        if( logBuffer == null)
        {
           logBuffer = new StringBuffer();
           hasLog = false;
        }
        
        if ( Double.isNaN( chisq ) )
          return new ErrorString( "ERROR in LsqrsJ: " + 
                                  " BestFitMatrix calculation failed" );
       
        double[][]hkl = new double[3][peaks.size()];
        double[][]q = new double[3][peaks.size()];
        for( int i=0; i< peaks.size( );i++)
        {
           Peak_new peak = peaks.get( i );
           hkl[0][i]= Math.floor( .5+peak.h( ));
           hkl[1][i]= Math.floor( .5+peak.k( ));
           hkl[2][i]= Math.floor( .5+peak.l( ));
           float[] qi= peak.getUnrotQ( );
           q[0][i]= qi[0];
           q[1][i]= qi[1];
           q[2][i]= qi[2];
           
        }
        
        double[][] Tq     = LinearAlgebra.mult( UB, hkl );
       // double[][] Thkl   = LinearAlgebra.mult( LinearAlgebra.getInverse( UB ), Tq );

                                  // the "observed" hkl corresponding to measured
                                  // q values.
        double obs_q[][] = LinearAlgebra.getTranspose( q );
        double obs_hkl[][];          
        obs_hkl = LinearAlgebra.mult( LinearAlgebra.getInverse( UB ), obs_q );

        // write information to the log file
        logBuffer.append( 
          " seq#   h     k     l      x      y       z      " +
          "xcm    ycm      wl  Iobs    Qx     Qy     Qz\n" );
        int k=0;
        IPeak peak;
        for( int i = 0; i < peaks.size(  ); i++ ) 
        if(keep[i]==0){
          peak = (IPeak)peaks.elementAt(i);
    
                          // The first line logged for a peak has the observered
                          // values for the peak, 'indexed' by integer hkl values.
          logBuffer.append( Format.integer( peak.seqnum(), 5)+" " +
                            Format.integer( peak.h(), 3 )+" " +
                            Format.integer( peak.k(), 5 )+" " +
                            Format.integer( peak.l(), 5 )+" " +
                            Format.real( peak.x(),   8, 2 )+" " +
                            Format.real( peak.y(),   6, 2 )+" " +
                            Format.real( peak.z(),   7, 2 )+" " +
                            Format.real( peak.xcm(), 6, 2 )+" " +
                            Format.real( peak.ycm(), 6, 2 )+" " +
                            Format.real( peak.wl(),  7, 4 )+" " +
                            Format.integer(  peak.ipkobs(), 5 )+" " +
                            Format.real( peak.getUnrotQ()[0], 7, 3 )+" " +
                            Format.real( peak.getUnrotQ()[1], 6, 3 )+" " + 
                            Format.real( peak.getUnrotQ()[2], 6, 3 )+"\n" );

                    logBuffer.append( Format.string("",73) +
                            Format.real( Tq[0][k], 6, 3 )+" " + 
                            Format.real( Tq[1][k], 6, 3 )+" " +
                            Format.real( Tq[2][k], 6, 3 ) + "\n" );

                            // The third line logged has the fractional hkl
                            // values observed for a peak, together with the
                            // difference in theoretical and observed hkl
          logBuffer.append( "      " + 
                            Format.real( obs_hkl[0][k], 6, 2 )+" " +
                            Format.real( obs_hkl[1][k], 5, 2 )+" " + 
                            Format.real( obs_hkl[2][k], 5, 2 )+" "  );

          double error = Math.abs( obs_hkl[0][k] - peak.h() ) +
                         Math.abs( obs_hkl[1][k] - peak.k() ) +
                         Math.abs( obs_hkl[2][k] - peak.l() );

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
          k++;
        }

        // calculate 
        for( int i = 0; i <peaks.size(); i++ ) {
          for( int j = 0; j < 3; j++ ) {
            chisq = chisq + ( ( q[i][j] - Tq[j][i] ) * ( q[i][j] - Tq[j][i] ) );
          }
        }
      

      // add chisq to the logBuffer
      logBuffer.append( 
        "\nchisq[Qobs-Qexp]: " + Format.real( chisq, 8, 5 ) + "\n" );

      // calculate lattice parameters and cell volume
     

      // determine uncertainties
     
      logBuffer.append( "\nOrientation matrix:\n" );

      for( int i = 0; i < 3; i++ ) {
        for( int j = 0; j < 3; j++ ) {
          logBuffer.append( Format.real( UB[j][i], 9, 6 ) +" ");
        }

        logBuffer.append( "\n" );
      }

      logBuffer.append( "\n" );
      logBuffer.append( "Lattice parameters:\n" );

      for( int i = 0; i < 7; i++ ) {
        logBuffer.append( Format.real( abc[i], 9, 3 )+" " );
      }

      logBuffer.append( "\n" );

      for( int i = 0; i < 7; i++ ) {
        logBuffer.append( Format.real( sig_abc[i], 9, 3 )+" " );
      }

      logBuffer.append( "\n" );

      // print out the results
      if( !hasLog)
         toConsole( UB, abc, sig_abc );


      return null;
     }
     /**
      * Calculates the least squares matrix that best maps all the hkl vectors to
      * all the q vectors. 
      * 
      * NOTE: This finds UB*hkl_col = Q_col.
      * 
      * Assumes errors in hkl values is 0. See CalcSig1 and CalcSigs2 for test
      *        cases
      *        
      * NOTE: The theoretical Error results conform well with experimental results
      *       but not well with results from the previous SCD least squares
      *       estimates.
      *       
      * NOTE: hkl matrix must be preTransformed to use this method and all the q 
      *        vectors must be indexed successfully. See other Utilities to create
      *        proper arrays.(To be done)
      * 
      * @param UB      a 3x3 matrix. Must be allocated by user
      * 
      * @param hkl    a 3 by k matrix of hkl values 
      * 
      * @param q      a 3 by k matrix of q values
      * 
      * @param abc     The scalars. Must allocated at least 7 doubles
      *                before calling it or no data will be returned
      *                
      *                
      * @param sig_abc The errors in abc. Must allocated at least 7 doubles
      *                before calling it or no data will be returned
      *                 
      * @return the chi square value( sum of squares of errors in q values)
      */
     public static  double LeastSquaresSCD(double[][] UB, 
                                            double[][] hkl,
                                            double [][] q, 
                                            double[] abc, 
                                            double[] sig_abc)
     {
        if( UB == null ||hkl == null || q==null || UB.length < 3 || 
              hkl.length<3  ||   q.length < 3 ||UB.length < 3 )
           
           return Double.NaN;
        
        for( int i=0; i< 3; i++)
           if( q[i].length != hkl[i].length  || UB[i].length < 3)
              return Double.NaN;
        
        if( abc == null || abc.length < 7 ) abc = new double[7];
        
        if( sig_abc==null || sig_abc.length < 7) sig_abc = new double[7];
        
        double[][] UBS = new double[3][3];
        double chiSq = LinearAlgebra.BestFitMatrix( UBS ,
              LinearAlgebra.getTranspose(hkl), LinearAlgebra.getTranspose(q) );
       
        if( Double.isNaN( chiSq) )
           return Double.NaN;
        
        LinearAlgebra.copy( UBS,UB);
        
        double[][] HHT = LinearAlgebra.mult( hkl, LinearAlgebra.getTranspose( hkl ) );
        double[][]HHTinv = LinearAlgebra.getInverse( HHT );
       

        double s2_q = chiSq/(3*q[0].length -6);
        
        
        double[][]Tensor = LinearAlgebra.getInverse( LinearAlgebra.mult(  
                                LinearAlgebra.getTranspose( UBS ),UBS));
        
        abc[0]= Math.sqrt( Tensor[0][0] );
        abc[1]= Math.sqrt( Tensor[1][1] );
        abc[2]= Math.sqrt( Tensor[2][2] );
        abc[3] =(  Tensor[1][2]/abc[1]/abc[2] );
        abc[4] =(  Tensor[0][2]/abc[0]/abc[2] );
        abc[5] =(  Tensor[0][1]/abc[0]/abc[1] );
        
        if( abc.length > 6)
           abc[6] = abc[0]*abc[1]*abc[2]*
                       Math.sqrt(1-abc[3]*abc[3]-abc[4]*abc[4]-abc[5]*abc[5]+
                             2*abc[3]*abc[4]*abc[5]);
       for( int i=3; i< 6;i++)
          abc[i]= Math.acos( abc[i])*180/Math.PI;
       
       
       
       double[][] errSqij = new double[3][3];
       Arrays.fill( errSqij[0], 0.);  
       Arrays.fill( errSqij[1], 0.); 
       Arrays.fill( errSqij[2], 0.);         
       
       
      
       double v;
        for( int i=0; i< q[0].length; i++)
        {
             
        //partial wrt  qx_i
        for( int r=0; r<3;r++)
           for(int c=0;c<3;c++)
           {  
              v  = HHTinv[c][0]*hkl[0][i];//*HHTinv[c][0]*hkl[0][i];
              v += HHTinv[c][1]*hkl[1][i];//*HHTinv[c][1]*hkl[1][i];
              v += HHTinv[c][2]*hkl[2][i];//*HHTinv[c][2]*hkl[2][i];
            
              errSqij[r][c] += v*v;
           }
        }
   
        
        errSqij = LinearAlgebra.mult(  errSqij ,s2_q );
       
        for(int i=0; i<3;i++)
           for(int j=0; j<3;j++)
              errSqij[i][j] =Math.sqrt( errSqij[i][j] );
        
     
       //missed tr(errUBTUB)*errUBTUB
        double[][] errUBTUB = LsqrsJ_base.errorAdd( 
              LsqrsJ_base.errorMult( LinearAlgebra.getTranspose( UBS ) ,errSqij, true), 
              LsqrsJ_base.errorMult( LinearAlgebra.getTranspose(errSqij  ) ,UBS, true)      , 
              false );           
         
        
        double[][] errTens =LsqrsJ_base.errorMult( Tensor ,
              LsqrsJ_base.errorMult(errUBTUB, Tensor, true) ,true);
        
/*        if( first && false)
        {
        System.out.println( "Tensor Errors in lsqrs");
        LinearAlgebra.print( errTens );
        
        System.out.println( "errUB");
        LinearAlgebra.print( errSqij );

        System.out.println("errUBTUB");
        LinearAlgebra.print(  errUBTUB );
        first = false;
        }      
 */       
        double[] sigs =LatticeErrors(abc, errTens);
        
        System.arraycopy( sigs,0, sig_abc , 0 , Math.min( sigs.length,sig_abc.length) );
        
        if( sig_abc.length >6)
            sig_abc[6]=SCD_ConstrainedLsqrsError.calcVolumeError( abc , sig_abc );
               
            
        return chiSq;
     }
     
     private static double sqr( double x)
     {
        return x*x;
     }
     
     /**
      * Calculates the errors in the lattice parameters given the errors in
      * the Tensor matrix in real space
      * 
      * @param abc    
      * @param errTens
      * @return
      */
     public static double[] LatticeErrors( double[] abc, double[][] errTens)
     {
        
        
        
        double[] sig_abc = new double[7];
        for( int i=0; i<3;i++)
          sig_abc[i] = errTens[i][i]/(abc[i]*2); 
              
        for( int i=0; i <3;i++)//doing 3+i
        {
           double a = abc[(i+1)%3];
           double b = abc[(i+2)%3];
           double da = sig_abc[(i+1)%3];
           double db = sig_abc[(i+2)%3];
           double gamma = abc[3+(i+3)%3];

           gamma = gamma*Math.PI/180;
           double a_bErr = errTens[(i+1)%3][(i+2)%3];
           double a_b =a*b*Math.cos( gamma );
           
           
           
           double Numsq= sqr(a_bErr*a*b)-sqr(a_b)*(sqr(a*db)+sqr(b*da));
           if( Numsq <0)
              Numsq = - Numsq;
          // Numsq=x1+x3;
           sig_abc[i+3]= Math.sqrt(Numsq)/sqr(a)/sqr(b)/Math.abs( Math.sin(gamma) );
           sig_abc[i+3] *=180/Math.PI;
           //formula for alpha where cos(gamma)= a.b/a/b
           //  taking differentials  d gamme_r = d(a.b)*a*b-(a.b)*(adb+bda) divided by
           //                     a^2c^2sin(gamma_r)
           
          
           
        }
        sig_abc[6]=SCD_ConstrainedLsqrsError.calcVolumeError( abc , sig_abc );
        return sig_abc;
     }
          
  /**
   * Calculates the least squares matrix that best maps the hkl vectors to
   * the q vectors. This is test code that is NOT used.
   * The test code remains so that it can be reused by others for  finding
   * least squares errors.
   * NOTE: This finds UB*hkl = Q.
   * Assumes errors in hkl values is 0(code for error in hkl != 0 has not been tested)
   * 
   * @param UB      a 3x3 matrix. Must be allocated by user
   * @param hkl    a 3 by k matrix of hkl values 
   * @param q      a 3 by k matrix of q values
   * @param abc     The scalars. Must be allocated before called or it
   *                will not be returned
   * @param sig_abc The errors in the scalars. Must be allocated before 
   *                 calling this routing.
   * @return
   */
  private static  double CalcSigs2(double[][] UB, double[][] hkl,
                            double [][] q, double[] abc, double[] sig_abc)
  {
     if( UB == null ||hkl == null || q==null || UB.length < 3 || 
           hkl.length<3  ||   q.length < 3 ||UB.length < 3 )
        
        return Double.NaN;
     
     for( int i=0; i< 3; i++)
        if( q[i].length != hkl[i].length  || UB[i].length < 3)
           return Double.NaN;
     
     if( abc == null || abc.length < 6 ) abc = new double[7];
     if( sig_abc==null || sig_abc.length < 6) sig_abc = new double[7];
     
     double[][] UBS = new double[3][3];
     double chiSq = LinearAlgebra.BestFitMatrix( UBS ,
           LinearAlgebra.getTranspose(hkl), LinearAlgebra.getTranspose(q) );
     if( first)
        System.out.println("ChiSq,nelts in CalcSigs="+chiSq+","+q[0].length);
     if( Double.isNaN( chiSq) )
        return Double.NaN;
     
     LinearAlgebra.copy( UBS,UB);
     double[][]Tensor = LinearAlgebra.getInverse( LinearAlgebra.mult(  
                             LinearAlgebra.getTranspose( UBS ),UBS));
     
     abc[0]= Math.sqrt( Tensor[0][0] );
     abc[1]= Math.sqrt( Tensor[1][1] );
     abc[2]= Math.sqrt( Tensor[2][2] );
     abc[3] =(  Tensor[1][2]/abc[1]/abc[2] );
     abc[4] =(  Tensor[0][2]/abc[0]/abc[2] );
     abc[5] =(  Tensor[0][1]/abc[0]/abc[1] );
     if( abc.length > 6)
        abc[6] = abc[0]*abc[1]*abc[2]*
                    Math.sqrt(1-abc[3]*abc[3]-abc[4]*abc[4]-abc[5]*abc[5]+
                          2*abc[3]*abc[4]*abc[5]);
    for( int i=3; i< 6;i++)
       abc[i]= Math.acos( abc[i])*180/Math.PI;
    
    double s2_q = chiSq/(3*q[0].length -6);
    double s2_hkl =0;
    double[][] Thkl =LinearAlgebra.mult( LinearAlgebra.getInverse( UBS ),q);
    for( int i=0; i < 3; i++)
       for( int j=0; j< Thkl[i].length; j++)
           s2_hkl+=(Thkl[i][j] - hkl[i][j])*(Thkl[i][j] - hkl[i][j]);
    s2_hkl = s2_hkl/(3*q[0].length -6);
    
    double[][] errSqij = new double[3][3];
    Arrays.fill( errSqij[0], 0.);  
    Arrays.fill( errSqij[1], 0.); 
    Arrays.fill( errSqij[2], 0.);         
    
    
    double[][] HHT = LinearAlgebra.mult( hkl, LinearAlgebra.getTranspose( hkl ) );
    double[][]HHTinv = LinearAlgebra.getInverse( HHT );
    
    double [][] Qmid= LinearAlgebra.mult( hkl , LinearAlgebra.getTranspose(q) );
    Qmid =LinearAlgebra.mult( HHTinv, Qmid );
    double[][]scratch = new double[3][3];
    double qi,v;
    double[][] res;
    double[] savqxi= new double[q[0].length];
     for( int i=0; i< q[0].length; i++)
     {
        //-----partial wrt hi
        Arrays.fill( scratch[0] , 0. );
        Arrays.fill( scratch[1] , 0. );
        Arrays.fill( scratch[2] , 0. );
        scratch[0][0]=2*hkl[0][i];
        scratch[0][1] = scratch[1][0]= hkl[1][i];
        scratch[0][2] =scratch[2][0]=hkl[2][i];
        res = LinearAlgebra.mult( 
                           LinearAlgebra.mult( HHTinv,scratch),Qmid);
       for( int r=0; r<3;r++)
          for(int c=0;c<3;c++)
            
          {  
             qi= q[r][i];
          
             v= res[c][r] -qi*HHTinv[c][0] ;
             errSqij[r][c] += 0;//v*v;
          }
       // partial wrt ki's
       Arrays.fill( scratch[0] , 0. );
       Arrays.fill( scratch[1] , 0. );
       Arrays.fill( scratch[2] , 0. );
       scratch[1][1]=2*hkl[1][i];
       scratch[0][1] = scratch[1][0]= hkl[0][i];
       scratch[1][2] =scratch[2][1]=hkl[2][i];
       res = LinearAlgebra.mult( 
                          LinearAlgebra.mult( HHTinv,scratch),Qmid);
      for( int r=0; r<3;r++)
         for(int c=0;c<3;c++)
         {  
            qi= q[r][i];
            int k=1;
            v= res[c][r] -qi*HHTinv[c][k] ;
            errSqij[r][c] += 0;//v*v;
         }
  
      
      // partial wrt q_li's
      Arrays.fill( scratch[0] , 0. );
      Arrays.fill( scratch[1] , 0. );
      Arrays.fill( scratch[2] , 0. );
      scratch[2][2]=2*hkl[2][i];
      scratch[0][2] = scratch[2][0]= hkl[0][i];
      scratch[1][2] =scratch[2][1]=hkl[1][i];
      res = LinearAlgebra.mult( 
                         LinearAlgebra.mult( HHTinv,scratch),Qmid);
     for( int r=0; r<3;r++)
        for(int c=0;c<3;c++)
        {  
           qi= q[r][i];
           int k=1;
           v= res[c][r] -qi*HHTinv[c][k] ;
           errSqij[r][c] +=0;// v*v;
        }
    
     
     //partial wrt  qx_i
     for( int r=0; r<3;r++)
        for(int c=0;c<3;c++)
        {  
           v  = HHTinv[c][0]*hkl[0][i];//*HHTinv[c][0]*hkl[0][i];
           v += HHTinv[c][1]*hkl[1][i];//*HHTinv[c][1]*hkl[1][i];
           v += HHTinv[c][2]*hkl[2][i];//*HHTinv[c][2]*hkl[2][i];
           if( c==0)
              savqxi[i] = v;
           errSqij[r][c] += v*v;
        }
     }
     if( first )
     {
        System.out.println("Should match bottom matrix,"+s2_q);
        LinearAlgebra.print(  errSqij);       
           
     }
     errSqij = LinearAlgebra.mult(  errSqij ,s2_q );
    
     for(int i=0; i<3;i++)
        for(int j=0; j<3;j++)
           errSqij[i][j] =Math.sqrt( errSqij[i][j] );
     
  
    
     double[][] errUBTUB = ERRmult( UBS ,errSqij, 
                             LinearAlgebra.getTranspose( UBS ),
                             LinearAlgebra.getTranspose( errSqij ));
    /* for(int i=0; i<3;i++)
        for( int j=0; i<i;j++)
           errUBTUB[i][j]= errUBTUB[j][i] =
              Math.sqrt( errUBTUB[i][j]*errUBTUB[i][j]+
                    errUBTUB[j][i]*errUBTUB[j][i]);
     */
     double[][] errTens = errorMult( Tensor , errUBTUB,true );
     errTens = errorMult( errTens , Tensor ,true);
     for(int i=0; i<3;i++)
        for( int j=0; j<3;j++)
           errTens[i][j]= Math.abs( errTens[j][i]);
     
     if( first)
     {
        first = false;
        System.out.println("UB error in CalcSigs");
        LinearAlgebra.print(  errSqij );
        System.out.println("UBTUB errors=");
        LinearAlgebra.print( errUBTUB );
        System.out.println("Tensor errors=");
        LinearAlgebra.print(  errTens );
        
     }
     
     for( int i=0; i<3;i++)
       sig_abc[i] = errTens[i][i]/(abc[i]*2); 
           
     for( int i=0; i <3;i++)
     {
        
        sig_abc[3+i] = errTens[(i+1)%3][(i+2)%3];
        sig_abc[3+i]=sig_abc[3+i]*abc[(i+1)%3]*abc[(i+2)%3]+
                      Math.abs(Tensor[(i+1)%3][(i+2)%3])*
                        (abc[(i+1)%3]*sig_abc[(i+2)%3]+
                         abc[(i+2)%3]*sig_abc[(i+1)%3]
                        );
        sig_abc[3+i] /=abc[(i+1)%3]*abc[(i+1)%3]*abc[(i+2)%3]*abc[(i+2)%3]*
                       Math.sin( abc[3+i]*Math.PI/180 );
        sig_abc[3+i] *=180/Math.PI;
        
     }
     
     if( sig_abc.length >6)
         sig_abc[6]=SCD_ConstrainedLsqrsError.calcVolumeError( abc , sig_abc );
            
            
     return chiSq;
  }
  
  
  /**
   * Calculates the least squares matrix that best maps the hkl vectors to
   * the q vectors. This is test code that is NOT used.
   * The test code remains so that it can be reused by others for  finding
   * least squares errors.
   * NOTE: This finds UB*Q = hkl.
   * Assumes code has not been tested much)
   */
   private static double CalcSigs1(double[][] UB, double[][] hkl, double[][] q,
         double[] abc, double[] sig_abc)
   {

      if ( UB == null || hkl == null || q == null || UB.length < 3
            || hkl.length < 3 || q.length < 3 || UB.length < 3 )

         return Double.NaN;

      for( int i = 0 ; i < 3 ; i++ )
         if ( q[i].length != hkl[i].length || UB[i].length < 3 )
            return Double.NaN;

      if ( abc == null || abc.length < 6 )
         abc = new double[ 7 ];
      if ( sig_abc == null || sig_abc.length < 6 )
         sig_abc = new double[ 7 ];

      double[][] UBI = new double[ 3 ][ 3 ];
      double chiSq = LinearAlgebra.BestFitMatrix( UBI , LinearAlgebra
            .getTranspose( q ) , LinearAlgebra.getTranspose( hkl ) );

      if ( Double.isNaN( chiSq ) )
         return Double.NaN;

      LinearAlgebra.copy( LinearAlgebra.getInverse( UBI ) , UB );
      double[][] Tensor = LinearAlgebra.mult( UBI , LinearAlgebra
            .getTranspose( UBI ) );

      abc[0] = Math.sqrt( Tensor[0][0] );
      abc[1] = Math.sqrt( Tensor[1][1] );
      abc[2] = Math.sqrt( Tensor[2][2] );
      abc[3] = ( Tensor[1][2] / abc[1] / abc[2] );
      abc[4] = ( Tensor[0][2] / abc[0] / abc[2] );
      abc[5] = ( Tensor[0][1] / abc[0] / abc[1] );
      if ( abc.length > 6 )
         abc[6] = abc[0]
               * abc[1]
               * abc[2]
               * Math.sqrt( 1 - abc[3] * abc[3] - abc[4] * abc[4] - abc[5]
                     * abc[5] + 2 * abc[3] * abc[4] * abc[5] );
      ;
      for( int i = 3 ; i < 6 ; i++ )
         abc[i] = Math.acos( abc[i] ) * 180 / Math.PI;

      double s2_hkl = chiSq / ( 3 * q[0].length - 6 );
      double s2_q = 0;
      double[][] Tqhkl = LinearAlgebra.mult( LinearAlgebra.getInverse( UBI ) ,
            hkl );
      for( int i = 0 ; i < 3 ; i++ )
         for( int j = 0 ; j < Tqhkl[i].length ; j++ )
            s2_q += ( Tqhkl[i][j] - q[i][j] ) * ( Tqhkl[i][j] - q[i][j] );
      s2_q = s2_q / ( 3 * q[0].length - 6 );

      double[][] errSqij = new double[ 3 ][ 3 ];
      Arrays.fill( errSqij[0] , 0 );
      Arrays.fill( errSqij[1] , 0 );
      Arrays.fill( errSqij[2] , 0 );

      double[][] QQT = LinearAlgebra.mult( q , LinearAlgebra.getTranspose( q ) );
      double[][] QQTinv = LinearAlgebra.getInverse( QQT );

      double[][] Qmid = LinearAlgebra.mult( q , LinearAlgebra
            .getTranspose( hkl ) );
      Qmid = LinearAlgebra.mult( QQTinv , Qmid );
      double[][] scratch = new double[ 3 ][ 3 ];
      double hkli, v;
      double[][] res;
      for( int i = 0 ; i < q[0].length ; i++ )
      {
         // -----partial wrt q_xi
         Arrays.fill( scratch[0] ,0);
         Arrays.fill( scratch[1] ,0);
         Arrays.fill( scratch[2] ,0);
         
         scratch[0][0] = 2 * q[0][i];
         scratch[0][1] = scratch[1][0] = q[1][i];
         scratch[0][2] = scratch[2][0] = q[2][i];
         res = LinearAlgebra.mult( LinearAlgebra.mult( QQTinv , scratch ) ,
               Qmid );
         for( int r = 0 ; r < 3 ; r++ )
            for( int c = 0 ; c < 3 ; c++ )

            {
               hkli = hkl[r][i];

               v = res[c][r] - hkli * QQTinv[c][0];
               errSqij[r][c] += v * v;
            }
         // partial wrt q_yi's
         Arrays.fill( scratch[0] ,0);
         Arrays.fill( scratch[1] ,0);
         Arrays.fill( scratch[2] ,0);
         scratch[1][1] = 2 * q[1][i];
         scratch[0][1] = scratch[1][0] = q[0][i];
         scratch[1][2] = scratch[2][1] = q[2][i];
         res = LinearAlgebra.mult( LinearAlgebra.mult( QQTinv , scratch ) ,
               Qmid );
         for( int r = 0 ; r < 3 ; r++ )
            for( int c = 0 ; c < 3 ; c++ )
            {
               hkli = hkl[r][i];
               int k = 1;
               v = res[c][r] - hkli * QQTinv[c][k];
               errSqij[r][c] += v * v;
            }

         // partial wrt q_zi's
         Arrays.fill( scratch[0] ,0);
         Arrays.fill( scratch[1] ,0);
         Arrays.fill( scratch[2] ,0);
         scratch[2][2] = 2 * q[2][i];
         scratch[0][2] = scratch[2][0] = q[0][i];
         scratch[1][2] = scratch[2][1] = q[1][i];
         res = LinearAlgebra.mult( LinearAlgebra.mult( QQTinv , scratch ) ,
               Qmid );
         for( int r = 0 ; r < 3 ; r++ )
            for( int c = 0 ; c < 3 ; c++ )
            {
               hkli = hkl[r][i];
               int k = 1;
               v = res[c][r] - hkli * QQTinv[c][k];
               errSqij[r][c] += v * v;
            }

         // partial wrt hkl_i
         for( int r = 0 ; r < 3 ; r++ )
            for( int c = 0 ; c < 3 ; c++ )
            {
               v = QQTinv[c][0] * q[0][i] + QQTinv[c][1] * q[1][i]
                     + QQTinv[c][2] * q[2][i];
               errSqij[r][c] += v * v / s2_q * s2_hkl;
            }
      }
      errSqij = LinearAlgebra.mult( errSqij , s2_q );

      for( int i = 0 ; i < 3 ; i++ )
         for( int j = 0 ; j < 3 ; j++ )
            errSqij[i][j] = Math.sqrt( errSqij[i][j] );

      // TODO: mult out use sq(a1*b1)+sq(a2*b2) +sq(a3*b3) as dot product
      double[][] plMin = LinearAlgebra.mult( UB , LinearAlgebra
            .getTranspose( errSqij ) );
      for( int i = 0 ; i < 3 ; i++ )
         sig_abc[i] = 2 * plMin[i][i];

      for( int i = 0 ; i < 3 ; i++ )
      {

         sig_abc[3 + i] = plMin[( i + 1 ) % 3][( i + 2 ) % 3]
               + plMin[( i + 2 ) % 3][( i + 1 ) % 3];// dot product error
         sig_abc[3 + i] = ( sig_abc[3 + i] * abc[( i + 1 ) % 3]
               * abc[( i + 2 ) % 3] + Math
               .abs( Tensor[( i + 1 ) % 3][( i + 2 ) % 3] )
               * sig_abc[3 + i] )
               / ( abc[( i + 1 ) % 3] * abc[( i + 1 ) % 3] * abc[( i + 2 ) % 3]
                     * abc[( i + 2 ) % 3] * Math.sin( abc[i] * Math.PI / 180 ) );// dot
                                                                                 // prod
                                                                                 // error
                                                                                 // to
                                                                                 // angle
                                                                                 // error
      }

      if ( sig_abc.length > 6 )
         sig_abc[6] = SCD_ConstrainedLsqrsError.calcVolumeError( abc , sig_abc );

      return chiSq;
   }
   
  private static String Show( Object A){
    String S ="";
    S= gov.anl.ipns.Util.Sys.StringUtil.toString(A);
    //multdimensional arrays on separate lines
    if( S != null)
       {
        String Res ="";
        int level =0;
        for( int i=0; i< S.length(); i++)
          if(S.charAt(i)=='['){
             
             level++;
          }else if(S.charAt(i)==']'){
            level--;
            if( level ==1)
              Res +='\n';
          }else
             Res +=S.charAt(i);
        S = Res;
      }
    return S;
  }

  /**
   * Main method for testing purposes and running outside of ISAW.
   */
  public static void main1( String[] args ) {
    LsqrsJ_base lsqrs = new LsqrsJ_base(  );

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
  public static void main( String[] args )
  {
     try
     {
       Vector<Peak_new> V = Peak_new_IO.ReadPeaks_new( "c:/ISAW/SampleRuns/SNS/SNAP/WSF/235_46/quartzFx.peaks");

       double[] sig_abc = new double[7];
       System.out.println("orig="+LsqrsJ1(V,null,null,
                   null, "C:/Users/ruth/x.mat", 8, null,
                   "Tri", sig_abc));
       System.out.println("original errors");
       LinearAlgebra.print( sig_abc );
     
       double[][] UB = new double[3][3];
       double[][] q = new double[3][V.size()];
       double[][] hkl = new double[3][V.size()];
       double[] abc = new double[7];
       float[] qq;
       for( int i=0; i< V.size( ); i++)
       {
          Peak_new pk = V.elementAt( i );
          qq = pk.getUnrotQ( );
          q[0][i] = qq[0];
          q[1][i] = qq[1];
          q[2][i] = qq[2];
          hkl[0][i]= Math.floor(.5+pk.h( ));
          hkl[1][i]= Math.floor(.5+pk.k( ));
          hkl[2][i]= Math.floor(.5+pk.l( ));
       }
       
       double chiSq = LeastSquaresSCD(UB,  hkl,
             q,  abc,  sig_abc);
       System.out.println( "new="+chiSq);
       
       System.out.println("new UB =");
       LinearAlgebra.print( LinearAlgebra.getTranspose( UB) );
       System.out.println("new abc");
       LinearAlgebra.print(  abc );
       System.out.println("new sigabc");
       LinearAlgebra.print(  sig_abc );
       //===================Experimental works
       Scanner fin = new Scanner(new
             File("C:/ISAW/Operators/TOF_SCD/NormZ.vals"));
       
       double[] zVals = new double[1534];
       for(int i=0;i<1533;i++)
          zVals[i] = fin.nextDouble( );

       //LinearAlgebra.print(zVals);
       double[][] UBsq = new double[3][3];
       double[][] UBs = new double[3][3];
       double[][] scratch1 = new double[3][3];
       double[][] scratch2 = new double[3][3];
       double[][] UBTUB1 = new double[3][3];
       double[][] Tensor1 = new double[3][3];;
       double[][] UBTUB2 = new double[3][3];
       double[][] Tensor2 = new double[3][3];
       double[][] UB1 = new double[3][3];
       double[] scalarSQ = new double[7];
       double[] scalar1 = new double[7];
       double[] scalar  = new double[7];
       int N=1534;
       double sq = Math.sqrt( chiSq/(q[0].length-1)/3);
       
       int z=0150;
       
       for( int i=0; i< N; i++)
       {
          q = LinearAlgebra.mult( UB,hkl );
          for( int s=0;s<q.length;s++)
             for( int k=0; k < q[s].length; k++)
                {
                 q[s][k] += zVals[z]*sq;
                 z++;
                 if( z >= 1534)
                    z=0;
                }
               
       
          if( z >=1534) z=0;
          
          LeastSquaresSCD( UBs, hkl, q, scalar,null);
          scratch1= LinearAlgebra.mult( LinearAlgebra.getTranspose(UBs) ,UBs  );
          scratch2= LinearAlgebra.getInverse( scratch1 ); 
          for( int r=0; r< 3; r++)
          {
             
             for( int c=0; c< 3;c++)
             {
                UBsq[r][c] += UBs[r][c]*UBs[r][c];
                UB1[r][c] += UBs[r][c];
                UBTUB1[r][c] +=scratch1[r][c];

                UBTUB2[r][c] +=scratch1[r][c]*scratch1[r][c];

                Tensor1[r][c] +=scratch2[r][c];

                Tensor2[r][c] +=scratch2[r][c]*scratch2[r][c];

             }
          }
          
          for( int s=0; s<7;s++)
          {
             scalarSQ[s] += scalar[s]*scalar[s];
             scalar1[s] +=scalar[s];
          }
          
       }
       
      
       System.out.println("Experimental results");
       for( int r=0;r<3;r++)
          for(int c=0; c<3; c++)
          {
             UBsq[r][c]= Math.sqrt((UBsq[r][c]-UB1[r][c]*UB1[r][c]/N)/(N-1));
             UBTUB2[r][c]= Math.sqrt((UBTUB2[r][c]-UBTUB1[r][c]*UBTUB1[r][c]/N)/(N-1));
             Tensor2[r][c]= Math.sqrt((Tensor2[r][c]-Tensor1[r][c]*Tensor1[r][c]/N)/(N-1));
          }
       
       for( int s =0; s< 7; s++)
          scalar1[s] =Math.sqrt((scalarSQ[s]-scalar1[s]*scalar1[s]/N)/(N-1));
      
       
       System.out.println("Errors in abc");
       LinearAlgebra.print( scalar1 );
       
       //Testing again. Using error multiply routines instead of
       //  LinearAlgebra mult routines.
       System.out.println("------------------\n    Experimental UB");
       LinearAlgebra.print(  LinearAlgebra.mult( UB1 ,1.0/N ) );
       System.out.println("Real UB");
       LinearAlgebra.print(   UB );
          //--------------------------
      System.out.println("-----------------------\n       Experimental errUB");
       LinearAlgebra.print(  UBsq );
       double [][] errUB= LinearAlgebra.mult( hkl,
                               LinearAlgebra.getTranspose(hkl));
       errUB = LinearAlgebra.mult(LinearAlgebra.getInverse(errUB),sq*sq);
       for( int c=0; c<3;c++)errUB[0][c] = Math.sqrt(errUB[c][c]);
       for( int r=1; r<3;r++)
          for( int c=0; c<3;c++)
             errUB[r][c]=errUB[0][c];

       System.out.println("Real errUB");
       LinearAlgebra.print(   errUB );
       //-------UBTUB
       System.out.println("------------------------------\n UBTUB");
       LinearAlgebra.print( LinearAlgebra.mult( UBTUB1,1.0/N ));
       System.out.println("Real UBTUB");
       double[][]UBTUB = LinearAlgebra.mult( LinearAlgebra.getTranspose(UB) ,UB );
       LinearAlgebra.print( UBTUB );
       System.out.println("----------------------------\n errUBTUB");
       LinearAlgebra.print( UBTUB2 );
       double[][] errUBTUB = LsqrsJ_base.errorAdd( 
                              LsqrsJ_base.errorMult( LinearAlgebra.getTranspose( UB ) ,errUB, true), 
                              LsqrsJ_base.errorMult( LinearAlgebra.getTranspose(errUB  ) ,UB, true)      , 
                              false );
      System.out.println(" Theoretical");
      LinearAlgebra.print(  errUBTUB );
      System.out.println("-----------------------\n Tensor");
      LinearAlgebra.print(  LinearAlgebra.mult( Tensor1 , 1.0/N ) );
      System.out.println("   Real");
      double[][]Tensor =LinearAlgebra.getInverse(UBTUB);
      LinearAlgebra.print( Tensor  );
    
      System.out.println("-----------------------\n errTensor");
      LinearAlgebra.print(  Tensor2 );
      System.out.println("   theoretical");
      double[][] errTensor =LsqrsJ_base.errorMult( Tensor ,
         LsqrsJ_base.errorMult(errUBTUB, Tensor, true) ,true);
      LinearAlgebra.print(  errTensor );
       //NOTE: If used Tensor and Tensor+/- errors to calc alpha, beta and
       // gamma got closer to the theoretical results than experimental
       // results
     
       
      
     }catch(Exception ss)
     {
        ss.printStackTrace( );
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

    if ( result.length() >= 2 )
    {
      result.delete( result.length(  ) - 2, result.length(  ) );
      result.append( "]" );
    }
    else
      return null;

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
   * Method to generate the hkl sums matrix
   */
  private static double[][] generateVC( Vector peaks,int[] keep ) {
    if( ( peaks == null ) || ( peaks.size(  ) <= 0 ) ) {
      return null;
    }

    double[][] VC = new double[3][3];
    IPeak peak     = null;

    double[] hkl = new double[3];

    // find the sum
   
    for( int i = 0; i < peaks.size(  ); i++ )
    if(keep[i]==0) {
      peak     = ( IPeak )peaks.elementAt( i );
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

    text = text.trim();
    if( ( text == null ) || ( text.length(  ) == 0 ) ) {
      return null;
    }

    // now take up some memory
    float[][] matrix = new float[3][3];
    int index;
    float temp;
    if( text.startsWith("[["))
      if( text.endsWith("]]"))
        text = text.substring(2,text.length()-1);
   
      
    // start with a StringBuffer b/c they are nicer to parse
    StringBuffer sb = new StringBuffer( text );

    //sb.delete( 0, 2 );

    try {
      // repeat for each row
      for( int i = 0; i < 3; i++ ) {
        // parse the first two columns which are ended by ','
        for( int j = 0; j < 2; j++ ) {
          index = sb.toString(  )
                    .indexOf( "," );

          if( index > 0 ) {
            temp = Float.parseFloat( sb.substring( 0, index ).trim() );
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
          temp = Float.parseFloat( sb.substring( 0, index ).trim() );
          sb.delete( 0, index + 1 );
          index = sb.toString().indexOf('[');
          if( index >0)
            sb.delete(0,index+1);
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
        sb.append( Format.real( UB[j][i], 9, 6 ) +" ");
      }

      sb.append( "\n" );
    }

    // print the lattice parameters
    if( abc == null ) {
      SharedData.addmsg( sb.toString(  ) );

      return;
    }

    for( int i = 0; i < 7; i++ ) {
      sb.append( Format.real( abc[i], 90, 3 )+" " );
    }

    sb.append( "\n" );

    // print the uncertainties for lattice parameters
    if( sig_abc == null ) {
      SharedData.addmsg( sb.toString(  ) );

      return;
    }

    for( int i = 0; i < 7; i++ ) {
      sb.append( Format.real( sig_abc[i], 9, 3 ) +" ");
    }

    sb.append( "\n" );

    SharedData.addmsg( sb.toString(  ) );
  }

  /**
   *
   */
  private static String writeLog( String logfile, String log ) {
   /* FileOutputStream fout = null;
    if( logfile == null){
   
       SharedData.addmsg(log+"\n");
       return null;
    }
       
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
    */
	gov.anl.ipns.Util.Sys.SharedMessages.LOGaddmsg( log );
	
	return null;
  }
  public Object clone(){
    LsqrsJ_base Res = new LsqrsJ_base();
    Res.CopyParametersFrom(this);
    return Res;
  }
}
