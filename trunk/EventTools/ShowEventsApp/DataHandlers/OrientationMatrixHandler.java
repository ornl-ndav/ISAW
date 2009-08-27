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

import javax.swing.JOptionPane;

import gov.anl.ipns.Operator.IOperator;
import gov.anl.ipns.Operator.Threads.ParallelExecutor;
import gov.anl.ipns.Operator.Threads.ExecFailException;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import gov.anl.ipns.MathTools.LinearAlgebra;

import DataSetTools.operator.Generic.TOF_SCD.Peak_new;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new_IO;
import DataSetTools.operator.Generic.TOF_SCD.PeakQ;

import Operators.TOF_SCD.IndexJ;
import Operators.TOF_SCD.IndexPeaks_Calc;

import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;

import EventTools.EventList.IEventList3D;
import EventTools.EventList.SNS_Tof_to_Q_map;
import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.IndexPeaksCmd;
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
  private boolean       changed = false;  // Flag to determine if an 
                                          // orientation matrix was acceppted

  public OrientationMatrixHandler( MessageCenter message_center )
  {
    this.message_center = message_center;
    message_center.addReceiver( this, Commands.SET_ORIENTATION_MATRIX );
    message_center.addReceiver( this, Commands.GET_ORIENTATION_MATRIX );
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

      String matrix_status = isValidMatrix( obj );
      if ( !matrix_status.equals( OK_STRING ) )
      {
        Util.sendError( message_center, matrix_status );
        return false;
      }
 
      float[][] new_mat = (float[][])obj;

      if ( orientation_matrix == null )
        orientation_matrix = new float[3][3];

      for ( int row = 0; row < 3; row++ )
        for ( int col = 0; col < 3; col++ )
          orientation_matrix[row][col] = new_mat[row][col];

       ShowLatticeParams( orientation_matrix );
         changed = true;
    }

    else if ( message.getName().equals(Commands.GET_ORIENTATION_MATRIX) )
    {
      Message mat_message = new Message( Commands.SET_ORIENTATION_MATRIX,
                                        orientation_matrix,
                                        true );
      message_center.receive( mat_message );
    } 

    else if ( message.getName().equals(Commands.WRITE_ORIENTATION_MATRIX))
    {
       String filename = (String)message.getValue();
       if( orientation_matrix == null)
       {
          Util.sendError( message_center, 
                          "There is no Orientation matrix to save" );
          return false;
       }
       float[][] UB = LinearAlgebra.getTranspose(orientation_matrix);

       ErrorString Res =
        DataSetTools.operator.Generic.TOF_SCD.Util.WriteMatrix( filename, UB );

       if( Res == null)
         Util.sendInfo( message_center, 
                       "Wrote Orientation matrix to " + filename );
       else
         Util.sendError( message_center, 
                       "Write Orientation Error:"+Res.toString() );
    } 

    else if ( message.getName().equals(Commands.READ_ORIENTATION_MATRIX))
    {
       String filename = (String)message.getValue();
       Object Res = Operators.TOF_SCD.IndexJ.readOrient( filename );
       if( Res == null || !(Res instanceof float[][]) )
       {
          if( Res == null )
             Res ="";
          Util.sendError( message_center, 
                          "Read Orientation Matrix Error:"+Res.toString() );
          return false;
       }
       float[][] orientSav = orientation_matrix;
       float[][] orMat = LinearAlgebra.getTranspose( (float[][]) Res );
       changed = false;
       receive( new Message(Commands.SET_ORIENTATION_MATRIX, orMat, false) );
          
       if( changed )
       {
         Message get_mess = new Message( Commands.GET_ORIENTATION_MATRIX,
                                         null,
                                         false );
         return receive( get_mess );
       }
    }
    return false;
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
      
     System.out.println("Orientation Matrix : " );
     LinearAlgebra.print( orientation_matrix );

     LinearAlgebra.invert( d_arr );
     String lat_con = IndexPeaks_Calc.getLatticeParams( d_arr );

     Util.sendInfo( message_center, lat_con );
  }

}
