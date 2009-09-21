/* 
 * File: titleScreen.java
 *
 * Copyright (C) 2009, Paul Fischer
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
 * This work was supported by the National Science Foundation under grant
 * number DMR-0800276 and by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

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

/**
 * Creates a JFrame containing a SplitPaneWithState
 * which holds the controls panel on the left side and the
 * display panels on the right.
 */
public class multiPanel implements IReceiveMessage
{
   public static Rectangle    PANEL_BOUNDS =  new Rectangle(10, 10, 570, 510) ;

   private JFrame             mainView;
   private controlsPanel      controlpanel;
   private displayPanel       displayPanel;
   private SplitPaneWithState splitPane;
   private MessageCenter      messageCenter;
   
   /**
    * Creates the controlsPanel and the displayPanel
    * to be added to the SplitPane and then creates the JFrame.
    */
   public multiPanel( MessageCenter messageCenter,
                      MessageCenter viewMessageCenter )
   {
      this.messageCenter     = messageCenter;
      
      controlpanel = new controlsPanel( messageCenter, viewMessageCenter );
      displayPanel = new displayPanel(messageCenter);
      buildMainFrame();
   }

   /**
    * Builds the main window that holds the controls panel
    * on the left side of the JFrame and the display panel on 
    * the right that will show the corresponding panel according
    * to which button is pressed.
    */
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

      MessageCenter status_message_center = Util.getStatusMessageCenter();
      new StatusMessageHandler( status_message_center, 
                                StatusFrame.getContentPane());
      StatusFrame.validate();
      WindowShower.show( StatusFrame );
   }
   
   /**
    * Builds the menu bar for the app containing file and help
    * options.
    * 
    * @param C The parent component to be used for the 
    *          CloseAppActionListener
    * @return JMenuBar
    */
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
      isawHelpDir += "/IsawHelp/UsingIsawEV.html";
      isawHelp.addActionListener(
            new ShowHelpActionListener(isawHelpDir));
      
      JMenuItem aboutHelp = new JMenuItem("Help About");
      String aboutHelpDir = System.getProperty("ISAW_HOME");
      aboutHelpDir += "/IsawHelp/AboutIsawEV.html";
      aboutHelp.addActionListener(
            new ShowHelpActionListener(aboutHelpDir));
      
      helpMenu.add(isawHelp);
      helpMenu.add(aboutHelp);
            
      return jmenBar;
   }
   
   public boolean receive(Message message)
   {
    
      return false;
   }
}

/**
 * Catches the user when they select File->Exit
 * and double checks that that is what the user wants to do
 * so they don't lose information by closing on accident.
 */
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

/**
 * When Save Q or Save D is selected it pops up it pops up a
 * JFileChooser for the user to save a file and then sends out a message
 * of SAVE_Q/D_VALUES with the file name
 */
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
      message_center.send(  new Message( Command, 
               jf.getSelectedFile().toString(), false) );
   }
}
