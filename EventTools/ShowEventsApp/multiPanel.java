package EventTools.ShowEventsApp;

import javax.swing.*;

import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import gov.anl.ipns.Util.Sys.ShowHelpActionListener;
import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.ViewTools.UI.SplitPaneWithState;

import EventTools.ShowEventsApp.Command.*;
import EventTools.ShowEventsApp.ViewHandlers.StatusMessageHandler;

import MessageTools.*;
import SSG_Tools.Viewers.Controls.MouseArcBall;

public class multiPanel implements IReceiveMessage
{
   public static Rectangle    PANEL_BOUNDS =  new Rectangle(10, 10, 570, 510) ;
   private final int          INTERVAL       = 30;
   private JFrame             mainView;
   private controlsPanel      controlpanel;
   private displayPanel       displayPanel;
   private SplitPaneWithState splitPane;
   private MessageCenter      messageCenter;
   
   /**
    * Creates three frames.
    * One frame is a splitpane with control buttons that bring up the
    * corresponding panel in the second area of the split.
    * The other two are a 3D display of the data and a 2D image of
    * the corresponding slice.
    */
   public multiPanel( MessageCenter messageCenter )
   {
      this.messageCenter = messageCenter;
      
      controlpanel = new controlsPanel(messageCenter);
      displayPanel = new displayPanel(messageCenter);
      buildMainFrame();
   }

   private void buildMainFrame()
   {
      mainView = new JFrame("Reciprocal Space Event Viewer");
      mainView.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      mainView.setBounds(PANEL_BOUNDS);
      mainView.setVisible(true);
     
      
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,1));
      panel.add(displayPanel);
     
      splitPane = new SplitPaneWithState(JSplitPane.HORIZONTAL_SPLIT,
                                         controlpanel, panel, .3f);
      splitPane.setOneTouchExpandable(false);
      splitPane.setDividerSize(3);
      
      mainView.add(splitPane);
      mainView.setJMenuBar( getJMenuBar( controlpanel) );
      mainView.validate();
      
      JFrame StatusFrame = new JFrame( " Messages" );
      StatusFrame.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
      StatusFrame.setBounds( PANEL_BOUNDS.x,
                            PANEL_BOUNDS.y+PANEL_BOUNDS.height,
                            PANEL_BOUNDS.width,
                            PANEL_BOUNDS.height/2 );
      new StatusMessageHandler( messageCenter, StatusFrame.getContentPane());
      StatusFrame.validate();
      WindowShower.show(  StatusFrame );
   }
   
   private JMenuBar getJMenuBar( JComponent C)
   {
      JMenuBar jmenBar= new JMenuBar();
      JMenu FileMen = new JMenu("File");
      JMenu helpMenu = new JMenu("Help");
      
      jmenBar.add(FileMen);
      JMenuItem SaveQGraph = new JMenuItem("Save Q Graph");
      JMenuItem SaveDGraph = new JMenuItem("Save D Graph");
      SaveQGraph.addActionListener( 
                new SaveActionListener( messageCenter , "Q"));
      SaveDGraph.addActionListener(  
               new SaveActionListener( messageCenter ,"D") );

      FileMen.add( SaveQGraph);
      FileMen.add(SaveDGraph);
      JMenuItem closeMenItem= new JMenuItem( "Exit"); 
      FileMen.add( closeMenItem);
      closeMenItem.addActionListener(  
               new CloseAppActionListener( C, true) );
      
      jmenBar.add(helpMenu);
      JMenuItem isawHelp = new JMenuItem("Using IsawEV");
      String isawHelpDir = System.getProperty("ISAW_HOME");
      isawHelpDir += "/IsawHelp/About.html";
      isawHelp.addActionListener(
            new ShowHelpActionListener(isawHelpDir));
      
      JMenuItem aboutHelp = new JMenuItem("Help About");
      String aboutHelpDir = System.getProperty("ISAW_HOME");
      aboutHelpDir += "/IsawHelp/About.html";
      aboutHelp.addActionListener(
            new ShowHelpActionListener(aboutHelpDir));
      
      helpMenu.add(isawHelp);
      helpMenu.add(aboutHelp);
            
      return jmenBar;
   }
   
   public boolean receive(Message message)
   {
      // System.out.println(message.getName() + " " + message.getValue());
      
      /*if (message.getName().equals(Commands.LOAD_FILE))
      {
         LoadEventsCmd loadEvents = ((LoadEventsCmd)message.getValue());
         
         oModel.setFile(loadEvents.getEventFile(), loadEvents.getDetFile(),
                   loadEvents.getFirstEvent(), loadEvents.getEventsToLoad());
         oVisual.buildDisplay(oModel, controlpanel.getColorScale(), true);
      }*/
      
      return false;
   }
}

class CloseAppActionListener implements java.awt.event.ActionListener
{
   JComponent comp;
   boolean check;
   public CloseAppActionListener(JComponent C,  boolean check)
   {
      comp = C;
      this.check = check;
   }
   /* (non-Javadoc)
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   @Override
   public void actionPerformed( ActionEvent e )
   {
     if( check)
     {
        int res = JOptionPane.showConfirmDialog( comp ,
                 "Do you Really want to Exit" , "Exit" , 
                 JOptionPane.YES_NO_OPTION);
        if( res == JOptionPane.NO_OPTION)
           return;
     }
      
     System.exit( 0); 
   }
   
}
class SaveActionListener implements ActionListener
{
   String Sv;
   String Command;
   MessageCenter message_center;
   
   public SaveActionListener( MessageCenter message_center, String Sv)
   {
      this.Sv = Sv;
      if( Sv =="Q")
         Command = Commands.SAVE_Q_VALUES;
      else
         Command = Commands.SAVE_D_VALUES;
      
      this.message_center = message_center;
   }
   
   public void actionPerformed( ActionEvent evt)
   {
      JFileChooser jf = new JFileChooser();
      if( jf.showSaveDialog( null ) != JFileChooser.APPROVE_OPTION)
         return;
      message_center.receive(  new Message( Command, 
               jf.getSelectedFile().toString(), false) );
   }
}
