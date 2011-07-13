/* 
 * File: OrientationMatrixHandler.java
 *
 * Copyright (C) 2009 Dennis Mikkelson
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
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package EventTools.ShowEventsApp.DataHandlers;

import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JFrame;

import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

import DataSetTools.components.ui.Peaks.subs;
import DataSetTools.instruments.SNS_SampleOrientation;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.ConfigLoadCmd;
import EventTools.ShowEventsApp.Command.SelectionInfoCmd;
import EventTools.ShowEventsApp.Command.UBwTolCmd;
import EventTools.ShowEventsApp.Command.Util;
import EventTools.ShowEventsApp.Command.IndexAndRefineUBCmd;

/**
 *  This class maintains a copy of the orientation matrix in
 *  the same format that is used for the IPNS style matrix file.
 *  THE ORIENTATION MATRIX PASSED BY ANY MESSAGE MUST BE OF THAT
 *  FORM.  The class DataHandlers.Util has methods to convert
 *  between this form and internal forms with a*, b* and c* as
 *  columns instead of rows.
 */
public class OrientationMatrixHandler implements IReceiveMessage
{
  private static final String OK_STRING = "OK";

  private MessageCenter message_center;
  private float[][]     orientation_matrix = null;

  private float[]       sig_abc            = null;

  private Vector3D      last_qxyz = null;          // last choice of qxyz
                                                   // "near" horizontal plane
                                                   // for ARCS

  int runNumber =0;
  int nbins =0;
  float phi=0;
  float chi=0;
  float omega =0;


  public OrientationMatrixHandler( MessageCenter message_center )
  {
    this.message_center = message_center;
    message_center.addReceiver( this, Commands.SET_ORIENTATION_MATRIX );
    message_center.addReceiver( this, Commands.GET_ORIENTATION_MATRIX );

    message_center.addReceiver( this, Commands.ADD_ORIENTATION_MATRIX_INFO );

    message_center.addReceiver( this, Commands.SHOW_ORIENTATION_MATRIX );
    message_center.addReceiver( this, Commands.WRITE_ORIENTATION_MATRIX );
    message_center.addReceiver( this, Commands.LOAD_CONFIG_INFO );
    message_center.addReceiver( this, Commands.READ_ORIENTATION_MATRIX );
    message_center.addReceiver( this, Commands.REFINE_ORIENTATION_MATRIX );
  }


  public boolean receive( Message message )
  {

    if ( message.getName().equals(Commands.SET_ORIENTATION_MATRIX) )
    {
      Object obj = message.getValue();
      if ( obj == orientation_matrix )         // this is just my message
        return false;                          // coming back to me.
     if( obj instanceof Vector && ((Vector)obj).size()==2 &&
       ((Vector)obj).lastElement() == orientation_matrix)
       return false;
       
      SetNewOrientationMatrix( obj );
    }

    else if( message.getName( ).equals( Commands.LOAD_CONFIG_INFO ))
    {
       Object val = message.getValue();
       if( val instanceof ConfigLoadCmd)
       {
          ConfigLoadCmd conf = (ConfigLoadCmd)val;
          runNumber = conf.getRunNumber( );
          nbins = conf.getNbins( );
          phi = conf.getPhi( );
          chi = conf.getChi( );
          omega = conf.getOmega();
       }
    }
    else if ( message.getName().equals(Commands.GET_ORIENTATION_MATRIX) )
    {
     /* Object message_value = orientation_matrix;
      if( sig_abc != null)
      {
         Vector V = new Vector(2);
         V.add(  orientation_matrix );
         V.add( sig_abc);
         message_value = V;
      }*/
      Vector message_value= Commands.MakeSET_ORIENTATION_MATRIX_arg( 
                                       orientation_matrix  , sig_abc );
      Message mat_message = new Message( Commands.SET_ORIENTATION_MATRIX,
                                        message_value,
                                        true );
      message_center.send( mat_message );
    } 

    else if ( message.getName().equals(Commands.SHOW_ORIENTATION_MATRIX))
    {
       if ( orientation_matrix != null )
          
         ShowOrientationMatrix( orientation_matrix );
       
       else
          
         Util.sendError( "There is no Orientation matrix to Show" );
    }

    else if ( message.getName().equals(Commands.WRITE_ORIENTATION_MATRIX))
    {
       String filename = (String)message.getValue();
       if( orientation_matrix == null)
       {
          Util.sendError( "There is no Orientation matrix to save" );
          return false;
       }
       float[][] UBo = ApplyGoniometerRotationToUB(orientation_matrix,true);
       float[][] UB = LinearAlgebra.getTranspose(UBo);
       double[] abc = DataSetTools.operator.Generic.TOF_SCD.Util.abc(
               LinearAlgebra.float2double( UB ));
       
       ErrorString Res =
        DataSetTools.operator.Generic.TOF_SCD.Util.writeMatrix( filename, 
              UB,
              LinearAlgebra.double2float( abc ), sig_abc);

       if( Res == null)
       {  
         Util.sendInfo( "Wrote Orientation matrix to " + filename );
         Util.sendInfo( "The Sample phi,chi and omega have been applied" );
       }
       else
          
         Util.sendError( "Write Orientation Error:"+Res.toString() );
    } 

    else if ( message.getName().equals(Commands.READ_ORIENTATION_MATRIX))
    {
       Vector V =(Vector)message.getValue();
       String filename = (String)V.firstElement();
       float OffInt =((Float)V.lastElement()).floatValue();
       Object Res = Operators.TOF_SCD.IndexJ.readOrient( filename );
       
       if( Res == null || !(Res instanceof float[][]) )
       {
          if( Res == null )
             Res ="";
          
          Util.sendError( "Read Orientation Matrix Error:"+Res.toString() );
          return false;
       }
       float[][] orMat = LinearAlgebra.getTranspose( (float[][]) Res );
       orMat = ApplyGoniometerRotationToUB(orMat,false);
       
       message_center.send( new Message( Commands.SET_ORIENTATION_MATRIX,
             Commands.MakeSET_ORIENTATION_MATRIX_arg( orMat,null),
             true ));//Must broadcast this orientation matrix
       
       Util.sendInfo( "The orientation matrix has been read in " );
       Util.sendInfo( "The Sample phi,chi and omega have been " +
                      "applied to this matrix" );
       IndexPeakWithOrientationMat( orMat, OffInt);
    }

    else if ( message.getName().equals(Commands.REFINE_ORIENTATION_MATRIX))
    {
       if( orientation_matrix == null )
       {
          Util.sendError( "NO Orientation Matrix so Can't Refine!" );
          return false;
       }
       
       Object obj = message.getValue();
       if ( !(obj instanceof IndexAndRefineUBCmd) ) 
       {
         Util.sendError( "Wrong command type for " + message.getName() );
         return false;
       }

       IndexAndRefineUBCmd cmd = (IndexAndRefineUBCmd)obj;
       cmd.setUB( orientation_matrix );

       Message mess = new Message( Commands.LSQRS_REFINE_ORIENTATION_MATRIX,
                                   cmd, true );
       message_center.send( mess );
    }

    else if ( message.getName().equals(Commands.ADD_ORIENTATION_MATRIX_INFO) )
    {
      Object val = message.getValue();
      if ( val instanceof SelectionInfoCmd )         // fill in counts field
      {
        SelectionInfoCmd select_info_cmd = (SelectionInfoCmd)val;
        if ( orientation_matrix != null )
        {
          Vector3D qxyz = select_info_cmd.getQxyz();
          Vector3D hkl  = Calc_hkl( qxyz.get(), orientation_matrix );
          select_info_cmd.setHKL( hkl );

          set_projected_HKL_info( select_info_cmd );
        }
        Message new_mess = new Message( Commands.ADD_PEAK_LIST_INFO,
                                        select_info_cmd,
                                        true );
        message_center.send( new_mess );
        return false;
      }
      else
        Util.sendError( "WRONG TYPE VALUE IN " +
                                        Commands.ADD_ORIENTATION_MATRIX_INFO );
    }

    return false;
  }


  private boolean SetNewOrientationMatrix( Object obj )
  {
     sig_abc = null;
     Object obj1 = obj;
     if( obj1 != null && obj instanceof Vector && ((Vector)obj1).size() == 2)
     {
        obj = ((Vector)obj1).firstElement( );
        sig_abc = (float[])((Vector)obj1).lastElement( );
     }
     String matrix_status = isValidMatrix( obj );
     if ( !matrix_status.equals( OK_STRING ) )
     {
       Util.sendError( matrix_status );
       sig_abc = null;
       return false;
     }

     float[][] new_mat = (float[][])obj;

     if ( orientation_matrix == null )
       orientation_matrix = new float[3][3];

     for ( int row = 0; row < 3; row++ )
       for ( int col = 0; col < 3; col++ )
         orientation_matrix[row][col] = new_mat[row][col];

     ShowLatticeParams( orientation_matrix );
     return true;
  }
  

  private boolean IndexPeakWithOrientationMat( float[][]UB, float offInt)
  { 
     // NOTE: we don't use a separate thread for setting the orientation matrix
     //       since we want it done BEFORE indexing.
   
     
     message_center.send( new Message( 
                           Commands.INDEX_PEAKS_WITH_ORIENTATION_MATRIX, 
                           new UBwTolCmd(UB, offInt),
                           false,
                           true ) );

     return true;
  }


  /**
   *  Check if the specified object seems to be a valid 3X3 orientation
   *  matrix.
   *
   *  @param obj  The object to check.
   *
   *  @return A String indicating what the problem with the matrix was,
   *          if any, or the OK_STRING if the matrix seems to be OK.
   */
  private String isValidMatrix( Object obj )
  {
    if ( obj == null )
      return "ORIENTATION MATRIX IS NULL";

    if ( ! (obj instanceof float[][]) )
      return "ORIENTATION MATRIX IS NOT A 2D ARRAY";

     float[][] new_mat = (float[][])obj;
     if ( new_mat.length    != 3 || 
          new_mat[0].length != 3 || 
          new_mat[1].length != 3 || 
          new_mat[2].length != 3  ) 
       return "ORIENTATION MATRIX IS NOT A 3X3 MATRIX";

     float[][] result = LinearAlgebra.getInverse( new_mat );
     if ( result == null )
       return "ORIENTATION MATRIX DOES NOT HAVE AN INVERSE"; 

     return OK_STRING;    // Seems to be OK.
  }


  private void ShowOrientationMatrix( float[][] matrix1 )
  {
    float[][] matrix = matrix1;
    matrix = ApplyGoniometerRotationToUB(matrix1,true);
    String ShowText = subs.ShowOrientationInfo( 
                      null, 
                      LinearAlgebra.getTranspose( matrix ),
                      null, sig_abc, true );
   
    JFrame jf = new JFrame( "Orientation Matrix");
    jf.setSize( 500,200 );
    jf.getContentPane().add( new JEditorPane("text/html", ShowText) );
    jf.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
            
    WindowShower.show(jf);
  }


  /**
   *  Calculate the lattice paramaters from the specified matrix and
   *  send a message with the lattice parameters to the status pane.
   *
   *  @param matrix UB matrix from which the lattice parameters are
   *                calculated.
   */
  private void ShowLatticeParams( float[][] matrix )
  {
     double[][] d_arr = LinearAlgebra.float2double( matrix );
     d_arr = LinearAlgebra.getTranspose( d_arr );
     double[] abc = DataSetTools.operator.Generic.TOF_SCD.Util.abc( d_arr );
     
     if ( abc != null )
     {
       String lat_con = String.format(
           "%5.3f  %5.3f  %5.3f    %5.3f  %5.3f  %5.3f    %6.2f",
            abc[0], abc[1], abc[2], abc[3], abc[4], abc[5], abc[6] );
        
       Util.sendInfo( lat_con );
     }
     else
       Util.sendInfo( "NO LATTICE PARAMETERS CALCULATED" );
  }


  private Vector3D Calc_hkl(float[] Q, float[][] orientation_mat )
  {
     float[][] real_orientation_mat = 
                             LinearAlgebra.getTranspose( orientation_mat );
     float[][] OrientationMatrixInv = 
                             LinearAlgebra.getInverse( real_orientation_mat );
     if( OrientationMatrixInv == null)
        return new Vector3D(0f,0f,0f);
     System.out.println("++++++++++++++Q+++++++++++++++++++");
     LinearAlgebra.print( Q );
     System.out.println("     - -- - - UBnv- - - - -- -");
     LinearAlgebra.print( OrientationMatrixInv );
     System.out.println("++++++++++++++++++++++++++++");
     float[]hkl = new float[3];
     java.util.Arrays.fill( hkl , 0f );
     for( int i=0;i<3;i++)
        for( int j=0; j< 3; j++)
            hkl[i] +=OrientationMatrixInv[i][j]*Q[j];

     return new Vector3D(hkl);
  }


  /** 
   *  Add information for orienting single crystals on ARCS, to the  
   *  SelectionInfoCmd. 
   */ 
  private void set_projected_HKL_info( SelectionInfoCmd select_info_cmd )
  {
    Vector3D hkl  = select_info_cmd.getHKL();    

    if ( hkl.getX() == 0 && hkl.getY() == 0 && hkl.getZ() == 0 ) 
    { 
      System.out.println("NO HKL CHOOSEN"); 
      return; 
    } 

    float[] exact_hkl = new float[3];

    exact_hkl[0] = Math.round(hkl.getX()); 
    exact_hkl[1] = Math.round(hkl.getY()); 
    exact_hkl[2] = Math.round(hkl.getZ()); 

    System.out.printf( "Exact HKL  = %6.1f  %6.1f  %6.1f\n",
                        exact_hkl[0], exact_hkl[1], exact_hkl[2]);
  
    float[][] or_mat     = LinearAlgebra.getTranspose( orientation_matrix );
    float[][] or_mat_inv = LinearAlgebra.getInverse( or_mat );
    
    float[] exact_qxyz = LinearAlgebra.mult( or_mat, exact_hkl );

    System.out.printf( "Exact Qxyz = %6.3f  %6.3f  %6.3f\n",
                        exact_qxyz[0], exact_qxyz[1], exact_qxyz[2]);

    float[] projected_qxyz = { exact_qxyz[0], exact_qxyz[1], 0 };
    float[] projected_hkl  = LinearAlgebra.mult( or_mat_inv, projected_qxyz );

    System.out.printf( "Projected Qxyz = %6.3f  %6.3f  %6.3f\n",
                      projected_qxyz[0], projected_qxyz[1], projected_qxyz[2]);

    System.out.printf( "Projected HKL  = %6.3f  %6.3f  %6.3f\n",
                        projected_hkl[0], projected_hkl[1], projected_hkl[2]);

    select_info_cmd.setProjectedHKL( new Vector3D( projected_hkl ) ); 

    double psi = Math.atan2( projected_qxyz[1], projected_qxyz[0] );
    double psi_deg = psi * 180 / Math.PI;

    System.out.printf( "PSI(deg) = %9.3f\n", psi_deg );

    select_info_cmd.setPSI( (float)psi_deg );

    Vector3D new_qxyz = new Vector3D( exact_qxyz );
    if ( last_qxyz != null )
    {
      Vector3D cross_prod = new Vector3D();
      cross_prod.cross( new_qxyz, last_qxyz );
      if ( cross_prod.length() > 0 )
      {
        cross_prod.normalize();
        if ( cross_prod.getZ() < 0 )
          cross_prod.multiply( -1 );
        double tilt     = Math.acos( cross_prod.getZ() );
        double tilt_deg = tilt * 180 / Math.PI;
        System.out.printf("Tilt(deg) = %8.4f\n", tilt_deg );
        select_info_cmd.setTilt( (float)tilt_deg ); 
      }
    }
    last_qxyz = new_qxyz;
  }
  
  private float[][] ApplyGoniometerRotationToUB( float[][] UBT, boolean to_out)
  {
     SNS_SampleOrientation Sorient = new SNS_SampleOrientation( phi,chi,omega);
     float[][] mat ;
     if( to_out)
        mat = Sorient.getGoniometerRotationInverse( ).get( );
     else
        mat = Sorient.getGoniometerRotation( ).get( );
     float[][] MatT = new float[3][3];
     for( int r=0; r< 3;r++)
        for( int c=0; c<3;c++)
           MatT[r][c]= mat[c][r];
     
     return LinearAlgebra.mult( UBT, MatT) ;
   
  }
}
