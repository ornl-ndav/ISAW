
package EventTools.ShowEventsApp.DataHandlers;

import java.util.Vector;

import gov.anl.ipns.Operator.IOperator;
import gov.anl.ipns.Operator.Threads.ParallelExecutor;
import gov.anl.ipns.Operator.Threads.ExecFailException;
import gov.anl.ipns.MathTools.LinearAlgebra;

import DataSetTools.operator.Generic.TOF_SCD.Peak_new;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new_IO;
import DataSetTools.operator.Generic.TOF_SCD.PeakQ;

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
 *  columns instead of rows, and 
 */
public class OrientationMatrixHandler implements IReceiveMessage
{
  private MessageCenter message_center;
  private float[][] orientation_matrix = null;

  public OrientationMatrixHandler( MessageCenter message_center )
  {
    this.message_center = message_center;
    message_center.addReceiver( this, Commands.SET_ORIENTATION_MATRIX );
    message_center.addReceiver( this, Commands.GET_ORIENTATION_MATRIX );
  }


  public boolean receive( Message message )
  {
    System.out.println("***OrientationMatrixHandler in thread " 
                       + Thread.currentThread());

    if ( message.getName().equals(Commands.SET_ORIENTATION_MATRIX) )
    {
      Object obj = message.getValue();
      if ( obj == null )
      {
        Util.sendError( message_center,
                       "SET_ORIENTATION_MATRIX IS NULL" );
        return( false );
      }

      if ( message.getValue() == orientation_matrix )   // this is just
        return false;                                   // my message coming
                                                        // back to me.
      if ( obj instanceof float[][] ) 
      {
         float[][] new_mat = (float[][])obj;
         if ( new_mat.length != 3 || new_mat[0].length != 3 )
         {
           Util.sendError( message_center, 
                           "SET_ORIENTATION_MATRIX NOT SIZE 3X3" ); 
           return false;
         }
         if ( orientation_matrix == null )
           orientation_matrix = new float[3][3];

         double[][] d_arr = new double[3][3];
         for ( int row = 0; row < 3; row++ )
           for ( int col = 0; col < 3; col++ )
           {
             orientation_matrix[row][col] = new_mat[row][col];
             d_arr[row][col] = new_mat[row][col] * 2 * Math.PI;
           }

         System.out.println("Orientation Matrix : " );
         LinearAlgebra.print( orientation_matrix );
         LinearAlgebra.invert( d_arr );
         String lat_con = IndexPeaks_Calc.getLatticeParams( d_arr );

         Util.sendError( message_center, lat_con );
      } 
    }

    else if ( message.getName().equals(Commands.GET_ORIENTATION_MATRIX) )
    {
      if ( orientation_matrix == null )
        return false;

      Message mat_message = new Message( Commands.SET_ORIENTATION_MATRIX,
                                        orientation_matrix,
                                        true );
      message_center.receive( mat_message );
    } 
    return false;
  }

}
