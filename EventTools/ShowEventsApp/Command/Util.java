package EventTools.ShowEventsApp.Command;

import javax.swing.SwingUtilities;
import java.io.*;

//import Command.execOneLine;
import DataSetTools.dataset.*;
import EventTools.ShowEventsApp.Command.Commands;
import MessageTools.*;

/**
 * 
 * @author Ruth
 *
 */
public class Util
{
   private static MessageCenter status_message_center = 
                                new MessageCenter("Fast Info Messages");
   static
   {
     new UpdateManager( status_message_center, null, 30 );
   }

   /**
    *  Get a reference to the message center contained in 
    *  this class that is used for sending short messages to
    *  the status pane.
    */
   public static MessageCenter getStatusMessageCenter()
   {
     return status_message_center;
   }

   /**
    * The message center will send messages to all listeners for the command
    * 
    * @param message_center   The message center
    * @param Command          The command 
    * @param value            The argument to the command
    * @param replace          If true, replaces all other messages from 
    *                         this command
    */
   public static void sendMessage( MessageCenter message_center,
                                   String Command,
                                   Object value,
                                   boolean replace)
   {
      if( Command == null )
         return;
      status_message_center.send( new Message(Command,value,replace) );
   }
   
   /**
    * The message center will send messages to all listeners for the command.
    * This request will be run in the AWT event dispatching thread via
    * the SwingUtilities.invokeLater method
    * 
    * @param message_center   The message center
    * @param Command          The command 
    * @param value            The argument to the command
    * @param replace          If true, replaces all other messages 
    *                         from this command
    */
   public static void sendMessageOnAWTThread( MessageCenter message_center,
                                              String  Command,
                                              Object  value,
                                              boolean replace)
   {
      if( message_center == null || Command == null )
         return;
      SwingUtilities.invokeLater(  
               new AWTThreadRun(new Message(Command,value,replace), 
                        message_center));
   }

   
   /**
    * Sends an error message to the status panel
    *
    * @param message  The error message
    */
   public static void sendError( String message )
   {
      sendMessage(status_message_center, Commands.DISPLAY_ERROR, message, false);
   }
   
   
   /**
    * Sends an informational message to the status panel
    *
    * @param message  The error message
    */
   public static void sendInfo( String message )
   {
      sendMessage(status_message_center, Commands.DISPLAY_INFO, message, false);
   } 


   /**
    * Sends a warning message to the status panel
    *
    * @param message  The error message
    */
   public static void sendWarning( String message )
   {
      sendMessage(status_message_center, Commands.DISPLAY_WARNING, message, false);
   } 
   /**
    * Sends a clear message to the status panel
    *
    * @param message  The error message
    */
   public static void sendClearStatusPanel( String message )
   {
      sendMessage(status_message_center, Commands.DISPLAY_CLEAR, message, false);
   }
   
   /**
    * Reads in trailing lines in a file with 2 or more numbers and interprets
    * these as a data block of a data set with one entry. Reads past empty lines
    * @param FileName  The name of the file with the data
    * @return    The data set or null if data is improper
    */
   public static DataSet ReadDSFile( String FileName)
   {
      float[] xvals,yvals,errs;
      int nx,ny;
      String line = null;
      FileInputStream finp = null;
     try
     {
        finp = new FileInputStream(FileName);
        boolean done = false;
        line=null;
        while( !done)
        {
           line = ReadOneLine( finp);
           if( line == null)
              return null;
           line = line.trim();
           if( getData(line) != null)
              done = true;
        }
     }catch( Exception s)
     {
        return null;
     }
     xvals = new float[200];
     yvals =new float[200];
     errs =new float[200];
     nx=ny=0;
     boolean done = false;
     while( !done)
     {
        float[] Res = getData(line);
        if( Res == null)
           done = true;
        else if( Res.length < 2)
        {
           xvals[nx++]= Res[0];
           done = true;
        }else
        {
           xvals[nx]=Res[0];
           yvals[ny]= Res[1];
           if( Res.length < 3 || errs == null)
              errs= null;
           else
              errs[nx]=Res[2];
           nx++; ny++;
           if( nx >=xvals.length || ny >= yvals.length)
           {
              xvals = Copy( xvals);
              yvals = Copy( yvals);
              errs = Copy( errs);
           }
           try{
              line = ReadOneLine( finp);
              if( line == null)
                done = true;
           }catch(Exception s3)
           {
              done = true;
              line = null;
           }
        }
        
     }
     Data D = null;
     float[] xx = new float[nx];
     float[] yy = new float[ny];
     float[] err = new float[ny];
     System.arraycopy(  xvals , 0 , xx , 0 , nx );
     System.arraycopy(  yvals , 0 , yy , 0 , ny );
     if( errs != null)
        System.arraycopy(  errs , 0 , err , 0 , ny );
     else
        err = null;
     if( nx == ny)
         D = new FunctionTable( new VariableXScale(xx), yy, err, 1 );
     else 
        D = new  HistogramTable( new VariableXScale(xx), yy, err, 1 );
     DataSet DS = new DataSet();
     DS.addData_entry( D );
     return DS;
       
   }
   
   private static float[] Copy( float[] vals)
   {
      if( vals == null)
         return null;
      float[] Res = new float[vals.length+100];
      System.arraycopy( vals , 0 ,Res , 0 , vals.length );
      return Res;
   }
     private static float[] getData( String line)
     {
        if( line == null || line.length() <1)
           return null;
        line = line.trim();
        int i = FindMinPos( line, " ,");
        if( i < 0)
           return null;

        String S= line.substring(0,i);
        float f1,f2,f3;
        f1=f2=f3 = Float.NaN;
        try
        {
           f1 = Float.parseFloat( S );
           
           line = line.substring(i+1 );
           if( line == null || line.length()<1)
              return null;
           line = line.trim();
           if( line.startsWith(","))
              line = line.substring( 1 ).trim();
           i= FindMinPos( line," ,");
           if( i > 0)
           {  
              f2 = Float.parseFloat(  line.substring(0,i) );
           
              if( i+1 < line.length())
                 line = line.substring( i+1 );
              else
                 line ="";
             line = line.trim();
             if( line.startsWith(","))
                 line = line.substring( 1 ).trim();
             i= FindMinPos( line," ,");
             if( i >0)
                f3 = Float.parseFloat(  line.substring(0,i) );
           }
        }catch(Exception s)
        {
          if( Float.isNaN( f1 ))
             return null;
        }
        int n=1;

        if( !Float.isNaN( f2 ))
           n=2;
        if( !Float.isNaN( f3 ))
           n=3;
        float[] Res = new float[n];
        Res[0] = f1;
        if( n > 1)
           Res[1] = f2;
        if( n > 2)
           Res[2] = f3;
        return Res;
     }
   
   private static int FindMinPos( String S, String SrchChars)
   {
      if( S == null || S.length()<1 || SrchChars == null ||
               SrchChars.length()<1)
         return -1;
     
      S=S.trim();
      if( S == null || S.length()<1 || SrchChars == null ||
               SrchChars.length()<1)
         return -1;
      int minPos = S.length();
      for( int i=0; i < SrchChars.length(); i++)
      {
         int k= S.indexOf(  SrchChars.charAt( i ) );
         if( k >=0 && k < minPos)
            minPos = k;
      }
     
      return minPos;
   }
   public static String ReadOneLine( FileInputStream finp )throws IOException
   {
      String S="";
      int c=0;
      for(c=0; c <' ' && c >=0 ;c=finp.read())
      {}
      
      if( c < 0)
         return null;
      
      for( ;c >=' '; c= finp.read())
      {
         S +=(char)c;
      }
      return S;
   }
}


class AWTThreadRun extends Thread
{
   Message message;
   MessageCenter message_center;

   public AWTThreadRun( Message message, 
                        MessageCenter message_center)
   {
      this.message = message;
      this.message_center = message_center; 
   }

   public void run()
   {
      if( message_center != null)
         message_center.send( message );
   }
   
}
