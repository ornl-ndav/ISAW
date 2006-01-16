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
 * $Log$
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
import gov.anl.ipns.Util.Numeric.Format;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;

//import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import DataSetTools.operator.Generic.TOF_SCD.GenericTOF_SCD;
import DataSetTools.operator.Generic.TOF_SCD.MatrixFilter;
import DataSetTools.operator.Generic.TOF_SCD.Peak;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new;
//import DataSetTools.operator.Generic.TOF_SCD.ReadPeaks;
import DataSetTools.operator.Generic.TOF_SCD.Util;
import DataSetTools.parameter.*;
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

  private static final double SMALL    = 1.525878906E-5;
  private static final String identmat = "[[1,0,0][0,1,0][0,0,1]]";

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
      new IntegerPG( "Minimum Peak Intensity Threshold", 0, false ) );

    //6
    addParameter( 
      new IntArrayPG( "Pixel Rows and Columns to Keep", "0:100", false ) );

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
    sb.append( "@param peaks  Vector of peaks" );
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
     Vector peaks =new Vector();
     for( int i=0; i< peaksPar.size(); i++){
         peaks.addElement(((Peak_new)peaksPar.elementAt(i)).clone());
     }                   
     int[] run_nums   = ( ( IntArrayPG )getParameter( 1 ) ).getArrayValue(  );
     int[] seq_nums   = ( ( IntArrayPG )getParameter( 2 ) ).getArrayValue(  );
     int threshold    = ( ( IntegerPG )getParameter( 5 ) ).getintValue(  );
     int[] keepRange  = ( ( IntArrayPG )getParameter( 6 ) ).getArrayValue(  );
     String cellType  = ((ChoiceListPG)getParameter( 7 )).getValue().toString();

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
     

     String matfile = getParameter( 4 )
                        .getValue(  )
                        .toString(  );

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
    
     Peak peak = null;
    int[] keep = new int[peaks.size()];
    java.util.Arrays.fill(keep ,0);
    int nargs= peaks.size();
    // trim out the peaks that are not in the list of selected sequence numbers
    if( seq_nums != null ) {
      for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
        peak = ( Peak )peaks.elementAt( i );

        if( binsearch( seq_nums, peak.seqnum(  ) ) == -1 ) {
           
          keep[i] = -1;
          nargs--;           
        }
      }
    }

    // trim out the peaks with out hkl listed
    for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
      peak = ( Peak )peaks.elementAt( i );
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
        peak = ( Peak )peaks.elementAt( i );
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
        peak = ( Peak )peaks.elementAt( i );
        if(keep[i] ==0)
        if( peak.ipkobs(  ) < threshold ) {
        
            keep[i] = -1;
            nargs--;
        }
      }
    }

    // trim out edge peaks (defined by the "pixels to keep" parameter)
    for( int i = peaks.size(  ) - 1; i >= 0; i-- ) {
      peak = ( Peak )peaks.elementAt( i );

      //see if the peak pixels are within the user defined array.  We are
      //assuming a SQUARE detector, so we'll reject it if the x or y position
      //is not within our range
      if(keep[i] ==0)
      if( 
        ( peak.x(  ) > upperLimit ) || ( peak.x(  ) < lowerLimit ) ||
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
    double[][] q   = new double[ nargs ][ 3 ];
    double[][] hkl = new double[ nargs ][ 3 ];
    int k=0;
    for( int i = 0; i < peaks.size(); i++ ) 
    if( keep[i]==0){
      peak        = ( Peak )peaks.elementAt( i );
      hkl[k][0]   = Math.round( peak.h(  ) );
      hkl[k][1]   = Math.round( peak.k(  ) );
      hkl[k][2]   = Math.round( peak.l(  ) );
      q[k]        = peak.getUnrotQ(  );
      k++;
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
      peak = ( Peak )peaks.elementAt( i );
     
      peak.UB(null);
      peak.sethkl((float)hkl[k][0], (float)hkl[k][1], (float)hkl[k][2], false);
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
      else
        chisq = SCD_util.BestFitMatrix( cellType, UB, Thkl, Tq );



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
        " seq#   h     k     l      x      y      z      " +
        "xcm    ycm      wl  Iobs    Qx     Qy     Qz\n" );
      k=0;
      for( int i = 0; i < peaks.size(  ); i++ ) 
      if(keep[i]==0){
        peak = (Peak)peaks.elementAt(i);
  
                        // The first line logged for a peak has the observered
                        // values for the peak, 'indexed' by integer hkl values.
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

                  logBuffer.append( Format.string("",73) +
                          Format.real( Tq[0][k], 7, 3 ) + 
                          Format.real( Tq[1][k], 7, 3 ) +
                          Format.real( Tq[2][k], 7, 3 ) + "\n" );

                          // The third line logged has the fractional hkl
                          // values observed for a peak, together with the
                          // difference in theoretical and observed hkl
        logBuffer.append( "      " + 
                          Format.real( obs_hkl[0][k], 6, 2 ) +
                          Format.real( obs_hkl[1][k], 6, 2 ) + 
                          Format.real( obs_hkl[2][k], 6, 2 )  );

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

      // calculate chisq
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
    double[] sig_abc = new double[7];

    
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
    }catch(Exception xx){
       xx.printStackTrace();
       return new ErrorString(xx);
    }
    return null;
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
  public static void main( String[] args ) {
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
   * Method to generate the hkl sums matrix
   */
  private static double[][] generateVC( Vector peaks,int[] keep ) {
    if( ( peaks == null ) || ( peaks.size(  ) <= 0 ) ) {
      return null;
    }

    double[][] VC = new double[3][3];
    Peak peak     = null;

    double[] hkl = new double[3];

    // find the sum
   
    for( int i = 0; i < peaks.size(  ); i++ )
    if(keep[i]==0) {
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
  }
  public Object clone(){
    LsqrsJ_base Res = new LsqrsJ_base();
    Res.CopyParametersFrom(this);
    return Res;
  }
}
