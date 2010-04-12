
package EventTools.ShowEventsApp.Controls;

import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.ViewTools.Components.OneD.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.*;

import DataSetTools.operator.Generic.Special.ViewASCII;
import DataSetTools.operator.Generic.TOF_SCD.IPeak;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new;
import DataSetTools.operator.Generic.TOF_SCD.Peak_new_IO;
import EventTools.ShowEventsApp.Command.Commands;
import MessageTools.IReceiveMessage;
import MessageTools.Message;
import MessageTools.MessageCenter;


public class peaksStatPanel extends JPanel implements IReceiveMessage
{
   MessageCenter message_center;
   JPanel        Info;


   public peaksStatPanel( MessageCenter message_center )
   {

      super();
      setLayout( new GridLayout( 1 , 1 ) );
      this.message_center = message_center;
      message_center.addReceiver( this , Commands.SET_PEAK_NEW_LIST );
      add( new JTextArea( "No Indexed Peaks Available" ) );
   }


   @Override
   public boolean receive( Message message )
   {

      if( message == null
               || ! Commands.SET_PEAK_NEW_LIST.equals( message.getName() ) )
         return true;

      Vector< Peak_new > val = (Vector< Peak_new >) message.getValue();
      removeAll();
      Info = new JPanel();
      Info.setBorder( new TitledBorder(
               new LineBorder( Color.black) ,"Peak Statisitics" ));
      add( Info );
      SwingUtilities.invokeLater( new MyThread( val , Info ) );
      return true;
   }


   public static void main( String[] args )
   {

      String filename = "C:\\ISAW\\SampleRuns\\SNS\\Snap\\QuartzRunsFixed\\quartz.peaks";
      Vector< Peak_new > peaks = null;
      try
      {
         peaks = Peak_new_IO.ReadPeaks_new( filename );
      }
      catch( Exception s )
      {
         System.exit( 0 );
      }
      MessageCenter msgC = new MessageCenter( "Test" );
      peaksStatPanel pan = new peaksStatPanel( msgC );
      JFrame jf = new JFrame( "Test" );
      jf.setSize( 800 , 1500 );
      jf.getContentPane().add( pan );
      WindowShower.show( jf );
      messageThread mmm = new messageThread( msgC , peaks );
      SwingUtilities.invokeLater( mmm );
   }
}


class MyThread extends Thread implements ActionListener
{
   Vector< Peak_new > Peaks;

   JPanel             panel;

   FunctionViewComponent hGraph , kGraph , lGraph;

   JPanel                holder = null;


   public MyThread( Vector< Peak_new > Peaks, JPanel panel )
   {
      this.Peaks = Peaks;
      this.panel = panel;
      hGraph = kGraph = lGraph = null;
   }


   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Thread#run()
    */
   @Override
   public void run()
   {
      int NIndexedPeaks = 0;

      float[] h_offset = new float[ 49 ];
      float[] k_offset = new float[ 49 ];
      float[] l_offset = new float[ 49 ];

      Arrays.fill( h_offset , 0 );
      Arrays.fill( k_offset , 0 );
      Arrays.fill( l_offset , 0 );

      int NIntegrated = 0;

      float[] xaxis = new float[ 50 ];
      
      for( int i = 0 ; i < 50 ; i++ )
         xaxis[ i ] = - .5f + i / 49f;
      
      for( int i = 0 ; i < Peaks.size() ; i++ )
      {
         IPeak peak = Peaks.elementAt( i );
         float h = peak.h();
         float k = peak.k();
         float l = peak.l();
         if( h != 0 || k != 0 || l != 0 )
         {
            h_offset[ Index( h ) ] += 1;
            k_offset[ Index( k ) ] += 1;
            l_offset[ Index( l ) ] += 1;

            NIndexedPeaks++ ;
         }
         if( peak.inti() > 0 && peak.sigi() > 0
                  && peak.inti() / peak.sigi() >= 3 )
            NIntegrated++ ;
      }
      
      // Now display in the JPanel
      panel.setLayout( new BorderLayout() );
      panel.add( new JLabel( "Indexed " + NIndexedPeaks + " out of "
               + Peaks.size() + " peaks" ),BorderLayout.NORTH );
      // ----------------------
      VirtualArrayList1D hData = new VirtualArrayList1D( new DataArray1D(
               xaxis , h_offset ) );

      hGraph = new FunctionViewComponent( hData );
      hData.setTitle( " h offsets from integer" );
      VirtualArrayList1D kData = new VirtualArrayList1D( new DataArray1D(
               xaxis , k_offset ) );
      kGraph = new FunctionViewComponent( kData );
      kData.setTitle( " k offsets from integer" );
      VirtualArrayList1D lData = new VirtualArrayList1D( new DataArray1D(
               xaxis , l_offset ) );
      lGraph = new FunctionViewComponent( lData );
      lData.setTitle( " l offsets from integer" );

      holder = new JPanel( new GridLayout( 1 , 1 ) );
      // -------------------------
     
      JPanel pp = new JPanel( new GridLayout( 3 , 1 ) );
      
      JCheckBox box = new JCheckBox( "h" , true );
      ButtonGroup grp = new ButtonGroup();
      grp.add( box );
      box.addActionListener( this );
      pp.add( box );
      box = new JCheckBox( "k" , false );
      grp.add( box );
      box.addActionListener( this );
      pp.add( box );
      box = new JCheckBox( "l" , false );
      grp.add( box );
      box.addActionListener( this );
      pp.add( box );
      panel.add( pp ,BorderLayout.EAST);


      holder.add( hGraph.getDisplayPanel() );
      holder.setBorder( new  LineBorder( Color.black));
      panel.add( holder, BorderLayout.CENTER );

      // panel.add( kGraph.getDisplayPanel() );

      // panel.add( lGraph.getDisplayPanel() );

      JPanel BottomPanel = new JPanel();
      BottomPanel.setLayout( new GridLayout( 1 , 2 ) );
      JLabel IntegInfo =new JLabel( "Measured " + NIntegrated  );
      IntegInfo.setToolTipText( "<html><body>Number of peaks where <BR> integrated"+
                     " intensity / error >= 3</body></html>" );
      BottomPanel.add( IntegInfo );
      //JButton ShowPeaks = new JButton( "Show Peaks" );
     // BottomPanel.add( ShowPeaks );
      //ShowPeaks.addActionListener( this );
      BottomPanel.add(  new JLabel("               ") );
      panel.add( BottomPanel, BorderLayout.SOUTH );
      panel.invalidate();
      panel.validate();
   }


   /*
    * (non-Javadoc)
    * 
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   @Override
   public void actionPerformed( ActionEvent arg0 )
   {
      if( arg0.getSource() instanceof JButton )
      {   //JOptionPane.showMessageDialog( null , "Not implemented yet" );
         String filename = gov.anl.ipns.Util.File.FileIO.appendPath( 
                  System.getProperty( "user.home" ), "ISAW/tmp/ppp.peaks" );
         try{
            
         Peak_new_IO.WritePeaks_new(filename, Peaks, false);

         JOptionPane.showMessageDialog( null , 
               "This option will disappear. Use Menu bar" );
         
         }catch(Exception ss)
         {
            JOptionPane.showMessageDialog( null , "Error in Peaks "+ss );
            return;
         }
         
         (new ViewASCII(filename)).getResult();
         
      }else   if( arg0.getActionCommand().startsWith( "h" ) )
      {
         holder.removeAll();
         holder.add( hGraph.getDisplayPanel() );
         holder.validate();
         holder.repaint();
      }
      else if( arg0.getActionCommand().startsWith( "k" ) )
      {  
         holder.removeAll();
         holder.add( kGraph.getDisplayPanel() );
         holder.validate();
         holder.repaint();
      }
      else if( arg0.getActionCommand().startsWith( "l" ) )
      {
         holder.removeAll();
         holder.add( lGraph.getDisplayPanel() );
         holder.validate();
         holder.repaint();
      }
   }


   private int Index( float h )
   {
      float offset = h - (int) Math.floor( h );

      if( offset > .5 )
         offset = offset - 1;

      int index = (int) ( offset * 48 + 24 );
      if( index < 0 )
         index = 0;
      if( index > 48 )
         index = 48;
      return index;
   }
}


//for testing puroposes only

class messageThread extends Thread
{
   MessageCenter      msg;

   Vector< Peak_new > Peaks;

   public messageThread( MessageCenter msg, Vector< Peak_new > Peaks )
   {
      this.msg = msg;
      this.Peaks = Peaks;
   }


   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Thread#run()
    */
   @Override
   public void run()
   {
      Message mmm = new Message( Commands.SET_PEAK_NEW_LIST , Peaks , false );
      msg.send( mmm );
      msg.dispatchMessages();
   }

}
