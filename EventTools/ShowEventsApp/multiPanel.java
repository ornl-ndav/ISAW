package EventTools.ShowEventsApp;

import javax.swing.*;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import gov.anl.ipns.ViewTools.UI.SplitPaneWithState;

import EventTools.ShowEventsApp.Command.*;

import MessageTools.*;
import SSG_Tools.Viewers.Controls.MouseArcBall;

public class multiPanel implements IReceiveMessage
{
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
      mainView.setBounds(10, 10, 570, 480);
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
   }
   
   private JMenuBar getJMenuBar( JComponent C)
   {
      JMenuBar jmenBar= new JMenuBar();
      JMenu FileMen = new JMenu("File");
      JMenu helpMenu = new JMenu("Help");
      
      jmenBar.add(FileMen);
      JMenuItem closeMenItem= new JMenuItem( "Exit"); 
      FileMen.add( closeMenItem);
      closeMenItem.addActionListener(  
               new CloseAppActionListener( C, true) );
      
      jmenBar.add(helpMenu);
      JMenuItem isawHelp = new JMenuItem("Using IsawEV");
      isawHelp.addActionListener(new helpListener());
      
      JMenuItem aboutHelp = new JMenuItem("Help About");
      aboutHelp.addActionListener(new helpListener());
      
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

class helpListener implements ActionListener
{
   public void actionPerformed(ActionEvent e)
   {

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
