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

import javax.swing.JEditorPane;
import javax.swing.JFrame;

import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.Geometry.Vector3D;

import DataSetTools.components.ui.Peaks.subs;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.SelectionInfoCmd;
import EventTools.ShowEventsApp.Command.Util;

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

  public OrientationMatrixHandler( MessageCenter message_center )
  {
    this.message_center = message_center;
    message_center.addReceiver( this, Commands.SET_ORIENTATION_MATRIX );
    message_center.addReceiver( this, Commands.GET_ORIENTATION_MATRIX );

    message_center.addReceiver( this, Commands.ADD_ORIENTATION_MATRIX_INFO );

    message_center.addReceiver( this, Commands.SHOW_ORIENTATION_MATRIX );
    message_center.addReceiver( this, Commands.WRITE_ORIENTATION_MATRIX );
    message_center.addReceiver( this, Commands.READ_ORIENTATION_MATRIX );
  }


  public boolean receive( Message message )
  {
    System.out.println("***OrientationMatrixHandler in thread " 
                       + Thread.currentThread());

    if ( message.getName().equals(Commands.SET_ORIENTATION_MATRIX) )
    {
      Object obj = message.getValue();
      if ( obj == orientation_matrix )         // this is just my message
        return false;                          // coming back to me.

      SetNewOrientationMatrix( obj );
    }

    else if ( message.getName().equals(Commands.GET_ORIENTATION_MATRIX) )
    {
      Message mat_message = new Message( Commands.SET_ORIENTATION_MATRIX,
                                        orientation_matrix,
                                        true );
      message_center.receive( mat_message );
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
       float[][] UB = LinearAlgebra.getTranspose(orientation_matrix);

       ErrorString Res =
        DataSetTools.operator.Generic.TOF_SCD.Util.WriteMatrix( filename, UB );

       if( Res == null)
         Util.sendInfo( "Wrote Orientation matrix to " + filename );
       else
         Util.sendError( "Write Orientation Error:"+Res.toString() );
    } 

    else if ( message.getName().equals(Commands.READ_ORIENTATION_MATRIX))
    {
       String filename = (String)message.getValue();
       Object Res = Operators.TOF_SCD.IndexJ.readOrient( filename );
       if( Res == null || !(Res instanceof float[][]) )
       {
          if( Res == null )
             Res ="";
          Util.sendError( "Read Orientation Matrix Error:"+Res.toString() );
          return false;
       }
       float[][] orientSav = orientation_matrix;
       float[][] orMat = LinearAlgebra.getTranspose( (float[][]) Res );
       SetNewOrientationMatrix( orMat );
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
        }
        Message new_mess = new Message( Commands.SHOW_SELECTED_POINT_INFO,
                                        select_info_cmd,
                                        true );
        message_center.receive( new_mess );
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
     String matrix_status = isValidMatrix( obj );
     if ( !matrix_status.equals( OK_STRING ) )
     {
       Util.sendError( matrix_status );
       return false;
     }

     float[][] new_mat = (float[][])obj;

     if ( orientation_matrix == null )
       orientation_matrix = new float[3][3];

     for ( int row = 0; row < 3; row++ )
       for ( int col = 0; col < 3; col++ )
         orientation_matrix[row][col] = new_mat[row][col];

     ShowLatticeParams( orientation_matrix );
     
     Message new_message = new Message( Commands.SET_ORIENTATION_MATRIX, 
                                        orientation_matrix, 
                                        false );
     
     message_center.receive( new_message );
     

     message_center.receive( new Message( 
                           Commands.INDEX_PEAKS_WITH_ORIENTATION_MATRIX, 
                           orientation_matrix, 
                           false) );

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


  private void ShowOrientationMatrix( float[][] matrix )
  {
    String ShowText = subs.ShowOrientationInfo( 
                      null, 
                      LinearAlgebra.getTranspose( matrix ),
                      null, null, true );
         
    JFrame jf = new JFrame( "Orientation Matrix");
    jf.setSize( 400,200 );
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


}
