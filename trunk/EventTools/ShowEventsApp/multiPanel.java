package EventTools.ShowEventsApp;

import javax.swing.*;
import java.awt.GridLayout;

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
      messageCenter.addReceiver(this, Commands.VALIDATE);
      
      controlpanel = new controlsPanel(messageCenter);
      displayPanel = new displayPanel(messageCenter);
      
      buildMainFrame();
   }

   private void buildMainFrame()
   {
      mainView = new JFrame("Multi Panel Viewer");
      mainView.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
      mainView.validate();
   }
   
   
   public boolean receive(Message message)
   {
      // System.out.println(message.getName() + " " + message.getValue());
      
      if (message.getName().equals(Commands.VALIDATE))
      {
         if (Boolean.parseBoolean((message.getValue().toString())))
         {
            //System.out.println("Start validate");
            //splitPane.validate();
            //mainView.validate();
            //splitPane.resetToPreferredSizes();
            //System.out.println("Finish validate");
         }
         
         return true;
      }
      
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
