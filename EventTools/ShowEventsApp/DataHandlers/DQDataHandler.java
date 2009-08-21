package EventTools.ShowEventsApp.DataHandlers;

import EventTools.ShowEventsApp.Command.Commands;
import EventTools.ShowEventsApp.Command.Util;
import EventTools.EventList.IEventList3D;
import MessageTools.*;

public class DQDataHandler implements IReceiveMessage
{
   public static final int   NUM_BINS = 2000;
   public static final float MAX_Q = 20;
   public static final float MAX_D = 10;
 
   private MessageCenter messageCenter;
   private float[][] d_values = new float[2][NUM_BINS+1];
   private float[][] q_values = new float[2][NUM_BINS+1]; 

   public DQDataHandler(MessageCenter messageCenter)
   {
      this.messageCenter = messageCenter;
      this.messageCenter.addReceiver(this, Commands.ADD_EVENTS);
      this.messageCenter.addReceiver(this, Commands.CLEAR_DQ);
      this.messageCenter.addReceiver(this, Commands.GET_D_VALUES);
      this.messageCenter.addReceiver(this, Commands.GET_Q_VALUES);

      setXs();
      clearYs();
   }
   
   private void clearYs()
   {
     for ( int i = 0; i <= NUM_BINS; i++ )
       d_values[1][i] = 0;

     for ( int i = 0; i <= NUM_BINS; i++ )
       q_values[1][i] = 0;
   }

   private void setXs()
   {
     for ( int i = 0; i <= NUM_BINS; i++ )
       d_values[0][i] = i * MAX_D / NUM_BINS;

     for ( int i = 0; i <= NUM_BINS; i++ )
       q_values[0][i] = i * MAX_Q / NUM_BINS;
   }

   private void AddEvents( IEventList3D events )
   {
     float xyz[] = events.eventVals();
     float weights[] = events.eventWeights();
     int   n_events = events.numEntries();
     int   index = 0;
     int   bin_num;
     float mag_q,
           d_val;
     float x, y, z;
     float[] q_arr = q_values[1];
     float[] d_arr = d_values[1];

     for ( int i = 0; i < n_events; i++ )
     {
       x = xyz[index++];
       y = xyz[index++];
       z = xyz[index++];
       mag_q = (float)Math.sqrt( x*x + y*y + z*z );

       bin_num = (int)(NUM_BINS * mag_q/MAX_Q); 
       if ( bin_num <= NUM_BINS )
         q_arr[bin_num] += 1;  // weights[i];

       if ( mag_q > 0 )
       {
         d_val = (float)(2 * Math.PI / mag_q);
         bin_num = (int)(NUM_BINS * d_val/MAX_D);
         if ( bin_num <= NUM_BINS )
           d_arr[bin_num] += 1;  // weights[i];
       }
     }
/*
     System.out.println("*********** Processed events " + n_events );
     System.out.println("*********** Q VALUES ARE : " );
     for ( int i = 0; i <= NUM_BINS; i++ )
       System.out.printf("x = %5.2f  y = %5.2f \n", q_values[0][i], q_values[1][i] );
*/
   }

   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command, value, true);
      
      messageCenter.receive(message);
   }
   
   private float[][] buildGraphData()
   {
      // Sample data for now. replace!
      float[][] xy = new float[2][500];
      
      for(int i = 0; i < 2; i++)
         for(int j = 0; j < 500; j++)
            xy[i][j] = j+100*i;
            
      return xy;
   }
   
   public boolean receive(Message message)
   {
      if (message.getName().equals(Commands.ADD_EVENTS))
      {
        Object obj = message.getValue();

        if ( obj == null || !(obj instanceof IEventList3D) )
          Util.sendError( messageCenter, 
                         "NULL or Empty EventList in DQHandler");
        AddEvents( (IEventList3D)obj );
      }
      
      if (message.getName().equals(Commands.CLEAR_DQ))
      {
        clearYs();
      }
      
      if (message.getName().equals(Commands.GET_D_VALUES))
      {
        sendMessage(Commands.SET_D_VALUES, d_values );
        return true;
      }
      
      if (message.getName().equals(Commands.GET_Q_VALUES))
      {
         sendMessage(Commands.SET_Q_VALUES, q_values );
         return true;
      }
      
      return false;
   }
}
