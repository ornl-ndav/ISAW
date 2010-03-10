/* 
 * File: filePanel.java
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

package EventTools.ShowEventsApp.Controls;

import gov.anl.ipns.Parameters.*;

import java.awt.GridLayout;
import java.text.NumberFormat;
import java.text.ParseException;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import DataSetTools.util.SharedData;
import EventTools.EventList.SNS_Tof_to_Q_map;
import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;

/**
 * Creates a panel to load an event file.
 * Displays how many events are in the event file, first event
 * to load, how many events to load, and how many events to show.
 * Also has fields for a detector file, incident spectrum file,
 * max Q to load and the number of threads to use.
 */
public class filePanel //extends JPanel
{
   private static final long  serialVersionUID = 1L;
   private MessageCenter      message_center;
   private JPanel             panel;
   private JTabbedPane        tabPane;
   private JButton            evFileButton;
   private JButton            detFileButton;
   private JButton            incFileButton;
   private JButton            detEffFileButton;
   private JButton            bankFileButton;
   private JButton            IDmapFileButton;
   private JButton            matFileButton;
   private JButton            loadFiles;
   private JTextField         evFileName;
   private JTextField         detFileName;
   private JTextField         incFileName;
   private JTextField         detEffFileName;
   private JTextField         bankFileName;
   private JTextField         IDmapFileName;
   private JTextField         matFileName;
   private JTextField         absorptionRadius;
   private JTextField         totalAbsorption;
   private JTextField         absorptionTrue;
   private JTextField         maxQValue;
   private JTextField         numThreads;
   private JTextField         availableEvents;
   private JTextField         firstEvent;
   private JTextField         eventsToLoad;
   //private JTextField         firstEventToShow;
   private JTextField         eventsToShow;
   private JTextField         protonsOnTarget;

   private JTextField         eventsToShowUDP;
   private String             Datafilename;// Remember last file chosen
  
   private String             Detfilename;// Remember last file chosen
   private String             Incfilename;// Remember last file chosen
   private String             DetEfffilename;// Remember last file chosen
   private String             Bankfilename_l;// Remember last file chosen
   private String             IDMapfilename_l;// Remember last file chosen
   private String             Matfilename;// Remember last file chosen
   private float              AbsorptionRadius;
   private float              TotalAbsorption;
   private float              AbsorptionTrue;
   private float              MaxQValue;//Remember last MaxQValue
   private FilteredPG_TextField Port;
   private JComboBox         Instrument;
   private static String[]   InstrumentList = 
                                 SNS_Tof_to_Q_map.supported_instruments;
   /**
    * Creates file panel as well as sets up default
    * properties for the file load locations.
    * 
    * @param message_center
    */
   public filePanel(MessageCenter message_center)
   {
      this.message_center = message_center;
      //this.setSize(300, 265);
      new SharedData();//Will read in IsawProps.dat
      Datafilename = System.getProperty("Data_Directory");
      Detfilename =  System.getProperty( "InstrumentInfoDirectory");
      Incfilename = DetEfffilename = Bankfilename_l =IDMapfilename_l = Detfilename;
      Matfilename = Datafilename;
      AbsorptionRadius = Float.NaN;
      TotalAbsorption = Float.NaN;
      AbsorptionTrue = Float.NaN;
      MaxQValue = Float.NaN;
      buildPanel();
      //this.add(panel);
   }
   
   /**
    * Returns the panel containing all the load information
    * 
    * @return JPanel
    */
   public JPanel getPanel()
   {
      return panel;
   }
   
   /**
    * Sets the panel to be visible or not.
    * 
    * @param visible true makes it visible, false hides it.
    */
   //public void setVisible(boolean visible)
   //{
   //   panel.setVisible(visible);
   //}
   
   /**
    * Builds the panel to contain the EventPanel, DetPanel and MatPanel.
    */
   private void buildPanel()
   {
      panel = new JPanel();
      panel.setBorder(new TitledBorder("Load Data"));
      panel.setLayout(new GridLayout(2,1));
      tabPane = new JTabbedPane();
      loadFiles = new JButton("Load");
      loadFiles.addActionListener(new button());
      
      tabPane.addTab("From File", buildEventPanel());
      tabPane.addTab("From Live Data", buildUDPPanel());
      panel.add( tabPane);
      JPanel sub_panel = new JPanel();
      panel.add( sub_panel );

      sub_panel.setLayout( new GridLayout(10,1) );
      sub_panel.add(buildDetPanel());
      sub_panel.add( buildBankPanel() );
      sub_panel.add( buildIDMapPanel() );
      sub_panel.add(buildIncPanel());
      
      //sub_panel.add(buildDetEffPanel());
      //sub_panel.add(buildMatPanel());
      sub_panel.add(buildMaxQPanel());
      sub_panel.add(buildAbsorptionPanel1());
      sub_panel.add(buildAbsorptionPanel2());
      sub_panel.add(buildAbsorptionPanel3());
      sub_panel.add(buildThreadPanel());
      sub_panel.add(loadFiles);
   }
   
   /**
    * Build the eventPanel.  Consists of button to load event file.
    * Once a file is loaded, information pertaining to the file such as number 
    * of events available from the file, first event to load, number of events
    * to load and number of events to show are loaded into appropriate 
    * textfields.
    * 
    * @return JPanel
    */
   private JPanel buildEventPanel()
   {
      NumberFormat nf = NumberFormat.getInstance();
      
      JPanel eventPanel = new JPanel();
      eventPanel.setLayout(new GridLayout(6,2));

      evFileButton = new JButton("Neutron Event File");
      evFileButton.addActionListener(new button());
      
      String default_evFileName = "";
      
      evFileName = new JTextField( default_evFileName );
      evFileName.addMouseListener(new mouse());
//    evFileName.setEditable(false);
      
      String default_availableEvents = nf.getInstance().format(10000000);
      JLabel available = new JLabel("Number of Events in File: ");
      availableEvents = new JTextField( default_availableEvents );
      availableEvents.setEditable(false);
      availableEvents.setHorizontalAlignment(JTextField.RIGHT);
      
      String default_firstEvent = "1";
      JLabel first = new JLabel("First Event to Load: ");
      firstEvent = new JTextField( default_firstEvent );
      firstEvent.setHorizontalAlignment(JTextField.RIGHT);
      
      String default_numLoad = nf.getInstance().format(10000000);
      JLabel numLoad = new JLabel("Number to Load: ");
      eventsToLoad = new JTextField( default_numLoad );
      eventsToLoad.setHorizontalAlignment(JTextField.RIGHT);
      
      /*String default_firstToShow = "1";
      JLabel firstEventToShowLbl = new JLabel("First Event to Show: ");
      firstEventToShow = new JTextField( default_firstToShow );
      firstEventToShow.setHorizontalAlignment(JTextField.RIGHT);*/
      
      String default_eventsToShow = nf.getInstance().format(5000000);
      JLabel maxEvents = new JLabel("Number to Show in 3D: ");
      eventsToShow = new JTextField( default_eventsToShow );
      eventsToShow.setHorizontalAlignment(JTextField.RIGHT);

      protonsOnTarget = new JTextField(15);
      
      eventPanel.add(evFileButton);
      eventPanel.add(evFileName);
      eventPanel.add(available);
      eventPanel.add(availableEvents);
      eventPanel.add(first);
      eventPanel.add(firstEvent);
      eventPanel.add(numLoad);
      eventPanel.add(eventsToLoad);
      //eventPanel.add(firstEventToShowLbl);
      //eventPanel.add(firstEventToShow);
      eventPanel.add(maxEvents);
      eventPanel.add(eventsToShow);
      eventPanel.add( new JLabel("Protons On Target"));
      eventPanel.add( protonsOnTarget);
      return eventPanel;
   }
   
   public JPanel  buildUDPPanel()
   {
      NumberFormat nf = NumberFormat.getInstance();
      
      JPanel Res = new JPanel();
      BoxLayout BL = new BoxLayout( Res, BoxLayout.Y_AXIS);
      Res.setLayout( BL );
      JPanel subPanel = new JPanel();
      
      subPanel.setLayout(  new GridLayout(1,2) );
      subPanel.add( new JLabel("Port"));
      Port = new FilteredPG_TextField( new IntegerFilter());
      Port.setText("8002");
      subPanel.add( Port);
      Res.add( subPanel);
      
      subPanel = new JPanel();
      subPanel.setLayout(  new GridLayout(1,2) );
      subPanel.add( new JLabel("Instrument"));
      Instrument = new JComboBox( InstrumentList);
      Instrument.setSelectedIndex( 0 );
      
      subPanel.add( Instrument);
      Res.add(  subPanel );
      
      String default_eventsToShow = nf.getInstance().format(5000000);
      JLabel maxEvents = new JLabel("Number to Show in 3D: ");
      eventsToShowUDP = new JTextField( default_eventsToShow );
      eventsToShowUDP.setHorizontalAlignment(JTextField.RIGHT);
      subPanel = new JPanel();
      subPanel.setLayout( new GridLayout(1,2));
      subPanel.add(  maxEvents );
      subPanel.add(  eventsToShowUDP );
      Res.add(  subPanel );
      
      ActionListener list = new UDPActionListener();
      subPanel= new JPanel( new GridLayout(1,3));
      JButton Pause = new JButton( "Pause");
      subPanel.add( Pause );
      Pause.addActionListener( list );
      

      Pause = new JButton( "Continue");
      subPanel.add( Pause );
      Pause.addActionListener( list );
      
      Pause = new JButton( "Pause & Clear");
      subPanel.add( Pause );
      Pause.addActionListener( list );
      
      Res.add( subPanel);
      Res.add( Box.createVerticalGlue());
      return Res;
      
   }
   
   /**
    * Builds the DetPanel which consists of button to load the 
    * det file and a textfield to contain path.
    * 
    * @return JPanel
    */
   private JPanel buildDetPanel()
   {
      JPanel detPanel = new JPanel();
      detPanel.setLayout(new GridLayout(1,2));
      
      detFileButton = new JButton("Cal File(.DetCal)");
      detFileButton.addActionListener(new button());
      
//    String default_detector_file =Detfilename;
      detFileName = new JTextField( "");
      detFileName.addMouseListener(new mouse());
//    detFileName.setEditable(false);
      
      detPanel.add(detFileButton);
      detPanel.add(detFileName);
      
      return detPanel;
   }
   
   /**
    * Builds the Incident Spectrum panel which consists of a load button
    * to load the file as well as a textfield to contain the path.
    * 
    * @return JPanel
    */
   private JPanel buildIncPanel()
   {
      JPanel incPanel = new JPanel();
      incPanel.setLayout(new GridLayout(1,2));
      
      incFileButton = new JButton("Incident Spectrum File");
      incFileButton.addActionListener(new button());
      
      incFileName = new JTextField();
      incFileName.addMouseListener(new mouse());
//      incFileName.setEditable(false);
      
      incPanel.add(incFileButton);
      incPanel.add(incFileName);   

      return incPanel;
   }
   
   private JPanel buildBankPanel()
   {  
      JPanel incPanel = new JPanel();
      incPanel.setLayout(new GridLayout(1,2));
      
     bankFileButton = new JButton("Bank File (_bank_.xml)");
     bankFileButton.addActionListener(new button());
     
     bankFileName = new JTextField();
     bankFileName.addMouseListener(new mouse());
     
     incPanel.add(bankFileButton);
     incPanel.add(bankFileName);
     return incPanel;
   }
   
   
   private JPanel buildIDMapPanel()
   {  
      JPanel incPanel = new JPanel();
      incPanel.setLayout(new GridLayout(1,2));
     IDmapFileButton = new JButton("ID Map File (_TS_.dat) ");
     IDmapFileButton.addActionListener(new button());
     
     IDmapFileName = new JTextField();
     IDmapFileName.addMouseListener(new mouse());
     
     
     incPanel.add(IDmapFileButton);
     incPanel.add(IDmapFileName);
 
      
      return incPanel;
   }
   
   /**
    * Builds the Detector Efficiency panel which consists of a load button
    * to load the file as well as a textfield to contain the path.
    * 
    * @return JPanel
    */
/*
   private JPanel buildDetEffPanel()
   {
      JPanel detEffPanel = new JPanel();
      detEffPanel.setLayout(new GridLayout(1,2));
      
      detEffFileButton = new JButton("Detector Efficiency File...");
      detEffFileButton.addActionListener(new button());
      
      detEffFileName = new JTextField();
      detEffFileName.addMouseListener(new mouse());
      detEffFileName.setEditable(false);
      
      detEffPanel.add(detEffFileButton);
      detEffPanel.add(detEffFileName);
      
      return detEffPanel;
   }
*/   
   /**
    * Builds the MatPanel which consists of button to load the 
    * det file and a textfield to contain path.
    * 
    * @return JPanel
    */
/*
   private JPanel buildMatPanel()
   {
      JPanel matPanel = new JPanel();
      matPanel.setLayout(new GridLayout(1,2));
      
      matFileButton = new JButton("Matrix File...");
      matFileButton.addActionListener(new button());
      
      matFileName = new JTextField();
      matFileName.addMouseListener(new mouse());
      matFileName.setEditable(false);
      
      matPanel.add(matFileButton);
      matPanel.add(matFileName);
      
      return matPanel;
   }
*/   
   /**
    * Builds the maxQPanel which consists of label and a 
    * textfield to contain the Value.
    * 
    * @return JPanel
    */
   private JPanel buildMaxQPanel()
   {
      JPanel maxQPanel = new JPanel();
      maxQPanel.setLayout(new GridLayout(1,2));
      
      JLabel maxQButton = new JLabel("Max Q to load");
      
      maxQValue = new JTextField();
      maxQValue.addActionListener( new button());
      maxQPanel.add(maxQButton);
      maxQPanel.add(maxQValue);
      //maxQPanel.add(matFileName);
      
      return maxQPanel;
   }
   /**
    * Build the absorptionPanel. 
    * Radius of the sample for the absorption correction is loaded into appropriate 
    * textfields.
    * 
    * @return JPanel
    */
   private JPanel buildAbsorptionPanel1()
   {
      JPanel absorptionPanel = new JPanel();
      absorptionPanel.setLayout(new GridLayout(1,2));

      String default_absorptionRadius  = "0.0";
      JLabel radAbsorption = new JLabel("Absorption Corr. Radius(cm): ");
      absorptionRadius = new JTextField( default_absorptionRadius );
      absorptionRadius.setHorizontalAlignment(JTextField.RIGHT);

      absorptionPanel.add(radAbsorption);
      absorptionPanel.add(absorptionRadius);

      return absorptionPanel;
   }
   /**
    * Build the absorptionPanel. 
    * Total absorption for the absorption correction is loaded into appropriate 
    * textfields.
    * 
    * @return JPanel
    */
   private JPanel buildAbsorptionPanel2()
   {
      JPanel absorptionPanel = new JPanel();
      absorptionPanel.setLayout(new GridLayout(1,2));

      String default_totalAbsorption = "0.0";
      JLabel total = new JLabel("Mu scattering(cm): ");
      totalAbsorption = new JTextField( default_totalAbsorption );
      totalAbsorption.setHorizontalAlignment(JTextField.RIGHT);
      
      absorptionPanel.add(total);
      absorptionPanel.add(totalAbsorption);

      return absorptionPanel;
   }
   /**
    * Build the absorptionPanel. 
    * True absorption for the absorption correction is loaded into appropriate 
    * textfields.
    * 
    * @return JPanel
    */
   private JPanel buildAbsorptionPanel3()
   {
      JPanel absorptionPanel = new JPanel();
      absorptionPanel.setLayout(new GridLayout(1,2));

      String default_trueAbs = "0.0";
      JLabel trueAbs = new JLabel("Mu absorption(cm): ");
      absorptionTrue = new JTextField( default_trueAbs );
      absorptionTrue.setHorizontalAlignment(JTextField.RIGHT);
      
      absorptionPanel.add(trueAbs);
      absorptionPanel.add(absorptionTrue);

      return absorptionPanel;
   }
   
   /**
    * Builds the panel holding the field to enter
    * the number of threads to use.
    * 
    * @return JPanel
    */
   private JPanel buildThreadPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));
      
      String default_numThreads = "4";
      JLabel numThreadsLbl = new JLabel("Number of Threads");
      numThreads = new JTextField(default_numThreads);
      numThreads.setHorizontalAlignment(JTextField.RIGHT);
      
      panel.add(numThreadsLbl);
      panel.add(numThreads);
      
      return panel;
   }
   
   /**
    * Takes the pathname of a file and 
    * calls setEventData(file) to set the
    * information on the panel according to 
    * the file.
    * 
    * @param inFile Path name for the event file to load.
    */
   public void setEventFile(String inFile)
   {
      File file = new File(inFile);
      setEventData(file);
   }
   
   /**
    * Takes the pathname of a file for the 
    * detector file and sets the textbox with the path.
    * 
    * @param inFile Path name for the detector file.
    */
   public void setDetectorFile(String inFile)
   {
      detFileName.setText(inFile);
   }
   
   /**
    * Takes the pathname of a file for the 
    * incident spectrum file and sets the textbox 
    * with the path.
    * 
    * @param inFile Path name for the incident spectrum file.
    */
   public void setIncSpecFile(String inFile)
   {
      incFileName.setText(inFile);
   }
   
   /**
    * Takes the pathname of a file for the 
    * detector efficiency file and sets the textbox 
    * with the path.
    * 
    * @param inFile Path name for the detector efficiency file.
    */
   public void setDetEffFile(String inFile)
   {
      detEffFileName.setText(inFile);
   }
   
   /**
    * Takes the pathname of a file for the 
    * matrix file and sets the textbox 
    * with the path.
    * 
    * @param inFile Path name for the matrix file.
    */
   public void setMatrixFile(String inFile)
   {
      matFileName.setText(inFile);
   }
   
   /**
    * Takes a file and populates the information pertaining to it
    * such as how many events in the file, and how many events to
    * load as well a show unless the number to show is more than 
    * 5,000,000 it defaults to that.
    * 
    * @param inFile File of the event file to get information from.
    */
   private void setEventData(File inFile)
   {
      NumberFormat nf = NumberFormat.getInstance();
      
      long file_size = inFile.length();
      long size = file_size / 8;
      
      try
      {
         evFileName.setText(inFile.getPath());
         availableEvents.setText(nf.getInstance().format(size));
         firstEvent.setText("1");
         
         if (size > 25000000)
            eventsToLoad.setText("25,000,000");
         else
            eventsToLoad.setText(nf.getInstance().format(size));
      
         if (size > 5000000)
            eventsToShow.setText("5,000,000");
         else
            eventsToShow.setText(nf.getInstance().format(size));
      }
      catch (NumberFormatException nfe)
      {
         //System.out.println(nfe.getStackTrace());
         String error = "Error formatting data!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", 
                                       JOptionPane.ERROR_MESSAGE);
         return;
      }
   }
   
   /**
    * This MouseListener is used to set the tooltip for the three
    * file textfields.  When the mouse enters the textfield, it display
    * the entire text so as to save space from displaying the entire
    * textfield.
    */
   private class mouse implements MouseListener
   {
      public void mouseClicked(MouseEvent e){}
      public void mouseEntered(MouseEvent e)
      {
         if (e.getSource() == evFileName)
            evFileName.setToolTipText(evFileName.getText());
         
         if (e.getSource() == detFileName)
            detFileName.setToolTipText(detFileName.getText());
         
         if (e.getSource() == incFileName)
            incFileName.setToolTipText(incFileName.getText());
         
         if (e.getSource() == detEffFileName)
            detEffFileName.setToolTipText(detEffFileName.getText());
         
         if (e.getSource() == matFileName)
            matFileName.setToolTipText(matFileName.getText());
      }

      public void mouseExited(MouseEvent e){}
      public void mousePressed(MouseEvent e){}
      public void mouseReleased(MouseEvent e){}
   }
   
   /**
    * Check whether each field needed to load the files
    * is valid information and the files can then be loaded.
    * 
    * @return True if every field is valid or false if not.
    */
   private boolean valid()
   {
      NumberFormat nf = NumberFormat.getInstance();
       
      if (evFileName.getText().equals(""))
      {
         String error = "You have not specified an event file!";
         JOptionPane.showMessageDialog( null, error, "Invalid Input", 
                                        JOptionPane.ERROR_MESSAGE );
         return false;
      }

      if (firstEvent.getText().equals(""))
      {
         String error = "You have not specified the first event to load!";
         JOptionPane.showMessageDialog( null, error, "Invalid Input", 
                                        JOptionPane.ERROR_MESSAGE );
         return false;
      }

      try
      {
         nf.parse(firstEvent.getText()).longValue();
      }
      catch (ParseException pe)
      {
         String error = "First event must be of type Integer!";
         JOptionPane.showMessageDialog( null, error, "Invalid Input", 
                                        JOptionPane.ERROR_MESSAGE );
         return false;
      }
      
      if (eventsToLoad.getText().equals(""))
      {
         String error = "You have not specified the number of events to load!";
         JOptionPane.showMessageDialog( null, error, "Invalid Input", 
                                        JOptionPane.ERROR_MESSAGE );
         return false;
      }
      
      try
      {
         nf.parse(eventsToLoad.getText()).longValue();
      }
      catch (ParseException pe)
      {
         String error = "Number of events to load must be of type Integer!";
         JOptionPane.showMessageDialog( null, error, "Invalid Input", 
                                        JOptionPane.ERROR_MESSAGE );
         return false;
      }
      
      /*if (firstEventToShow.getText().equals(""))
      {
         String error = "You have not specified the first event to display!";
         JOptionPane.showMessageDialog( null, error, "Invalid Input", 
                                        JOptionPane.ERROR_MESSAGE );
         return false;
      }
      
      try
      {
         nf.parse(firstEventToShow.getText()).longValue();
      }
      catch (ParseException pe)
      {
         String error = "First event to display must be of type Integer!";
         JOptionPane.showMessageDialog( null, error, "Invalid Input", 
                                        JOptionPane.ERROR_MESSAGE );
         return false;
      }*/
      
      if (eventsToShow.getText().equals(""))
      {
         String error = 
                   "You have not specified the number of events to display!";
         JOptionPane.showMessageDialog( null, error, "Invalid Input", 
                                        JOptionPane.ERROR_MESSAGE );
         return false;
      }
      
      try
      {
         nf.parse(eventsToShow.getText()).longValue();
      }
      catch (ParseException pe)
      {
         String error = "Number of events to show must be of type Integer!";
         JOptionPane.showMessageDialog( null, error, "Invalid Input", 
                                        JOptionPane.ERROR_MESSAGE );
         return false;
      }
/*
      if (detFileName.getText().equals(""))
      {
         String error = "You have not specified a detector file!";
         JOptionPane.showMessageDialog( null, error, "Invalid Input", 
                                        JOptionPane.ERROR_MESSAGE );
         return false;
      }
*/      
      if (numThreads.getText().equals(""))
      {
         String error = "You have not specified the number of threads to use!";
         JOptionPane.showMessageDialog( null, error, "Invalid Input", 
                                        JOptionPane.ERROR_MESSAGE );
         return false;
      }
      
      try
      {
         Integer.parseInt(numThreads.getText());
      }
      catch (NumberFormatException nfe)
      {
         String error = "Number of threads to show must be of type Integer!";
         JOptionPane.showMessageDialog( null, error, "Invalid Input", 
                                        JOptionPane.ERROR_MESSAGE );
         return false;
      }
      
      return true;
   }
   
   private class UDPActionListener implements ActionListener
   {
      public void actionPerformed( ActionEvent evt)
      {
         String command = evt.getActionCommand();
         if( command.equals( "Pause" ))
         {
            message_center.send(  new Message( Commands.PAUSE_UDP, null,true, true) );
         }else if( command.equals( "Continue" ))
         {

            message_center.send(  new Message( Commands.CONTINUE_UDP, null,true, true) );
         }else if( command.equals("Pause & Clear"))
         {

            message_center.send(  new Message( Commands.CLEAR_UDP, null,true, true) );
         }
      }
   }
   
   /**
    * Used for each button to load the files if Event File, Det file,
    * or Mat File button is pressed.  Also to send a message if Load
    * is pressed containing all the file names as well as the first event,
    * number of events to load, and number of events to load.
    */
   private class button implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         if (e.getSource() == loadFiles)
         {  
            String det_file = detFileName.getText();
            if ( det_file != null && det_file.trim().length() <= 0 )
              det_file = null;

            String inc_spec_file = incFileName.getText();
            if ( inc_spec_file != null &&
                 inc_spec_file.trim().length() <= 0 )
              inc_spec_file = null;
            
            NumberFormat nf = NumberFormat.getInstance();
            
            if(tabPane.getSelectedIndex() ==1)
            {
               try
               {
                  float AbsorptionRadius = 0.0f;
                  try
                  {
                     AbsorptionRadius = Float.parseFloat(  absorptionRadius.getText().trim() );
                  }catch( Exception s)
                  {
                     AbsorptionRadius = 0.0f;
                  }
                  float TotalAbsorption = 0.0f;
                  try
                  {
                     TotalAbsorption = Float.parseFloat(  totalAbsorption.getText().trim() );
                  }catch( Exception s)
                  {
                     TotalAbsorption = 0.0f;
                  }
                  AbsorptionTrue = 0.0f;
                  try
                  {
                     AbsorptionTrue = Float.parseFloat(  absorptionTrue.getText().trim() );
                  }catch( Exception s)
                  {
                     AbsorptionTrue = 0.0f;
                  }
                  LoadUDPEventsCmd cmd =new LoadUDPEventsCmd( 
                           Instrument.getSelectedItem().toString(),
                           Integer.parseInt( Port.getText()), 
                           det_file,
                           inc_spec_file,
                           null,            //detEffFileName.getText(),
                           bankFileName.getText(),
                           IDmapFileName.getText(),
                           null,
                           AbsorptionRadius,
                           TotalAbsorption,
                           AbsorptionTrue,
                           MaxQValue,
                           nf.parse( eventsToShowUDP.getText()).longValue()
                                                            );
                  
                  Message mess = new Message( Commands.LOAD_UDP_EVENTS,
                                              cmd,
                                              true, 
                                              true
                                              );
                  message_center.send(mess);
                  return;
               }catch(Exception ss)
               {
                  
               }
               
               return;
            }
            if(valid())
            {
               
               try
               {
                  String ev_file = evFileName.getText();
                 
                  // sendMessage( Commands.SET_NEW_INSTRUMENT, new_inst_cmd );
                  // The SET_NEW_INSTRUMENT command needs to be done before
                  // loading the event file, so that the histogram and
                  // mapping to Q are set up by the time we start sending
                  // in events.

                  
                  long startEvent = nf.parse(firstEvent.getText()).longValue();
                  if (startEvent <= 0)
                  {
                     startEvent = 0;
                     firstEvent.setText(Long.toString(startEvent));
                  }
                  else
                     startEvent -= 1;
                  float scale_factor = -1;
                  try
                  {
                     scale_factor = Float.parseFloat( protonsOnTarget.getText().trim() );
                     scale_factor = 1/scale_factor;
                     if( scale_factor < 0)
                        scale_factor = -1;
                  }catch( Exception sx)
                  {
                     scale_factor = -1;
                  }
                  float AbsorptionRadius = 0.0f;
                  try
                  {
                     AbsorptionRadius = Float.parseFloat(  absorptionRadius.getText().trim() );
                  }catch( Exception s)
                  {
                     AbsorptionRadius = 0.0f;
                  }
                  float TotalAbsorption = 0.0f;
                  try
                  {
                     TotalAbsorption = Float.parseFloat(  totalAbsorption.getText().trim() );
                  }catch( Exception s)
                  {
                     TotalAbsorption = 0.0f;
                  }
                  AbsorptionTrue = 0.0f;
                  try
                  {
                     AbsorptionTrue = Float.parseFloat(  absorptionTrue.getText().trim() );
                  }catch( Exception s)
                  {
                     AbsorptionTrue = 0.0f;
                  }
                  LoadEventsCmd fileInfo = 
                     new LoadEventsCmd(
                                ev_file,
                                det_file,
                                inc_spec_file,
                                null,               //detEffFileName.getText(),
                                bankFileName.getText(),
                                IDmapFileName.getText(),         
                                null,
                                AbsorptionRadius,
                                TotalAbsorption,
                                AbsorptionTrue,
                                MaxQValue,
                                nf.parse(availableEvents.getText()).longValue(),
                                startEvent, 
                                nf.parse(eventsToLoad.getText()).longValue(),
                                nf.parse(eventsToShow.getText()).longValue(),
                                Integer.parseInt(numThreads.getText()),
                                scale_factor);
                  
                  boolean collapse_messages   = true;
                  boolean use_separate_thread = true;
                  Message load_message = new Message( Commands.LOAD_FILE,
                                                      fileInfo,
                                                      collapse_messages,
                                                      use_separate_thread );
                  message_center.send( load_message );
               }
               catch (ParseException pe)
               {
                  //System.out.println(pe.getStackTrace());
                  String error = "Error parsing data to correct data types.";
                  JOptionPane.showMessageDialog(null, error, "Invalid Input",
                                                JOptionPane.ERROR_MESSAGE);
                  return;
               }
            }
            //else
            //{
            //   String error = "There is file information not completely 
            //   filled out or invalid!!";
            //   JOptionPane.showMessageDialog(null, error, "Invalid Input", 
            //                                 JOptionPane.ERROR_MESSAGE);
            //}
         }
         else if( e.getSource() == maxQValue)
         {
            String Sval = maxQValue.getText();
            if( Sval == null || Sval.trim().length() < 1)
               return;
            try
            {
               MaxQValue = Float.parseFloat(  maxQValue.getText().trim() );
            }catch( Exception s)
            {
               maxQValue.setText( "" );
            }
            return;
         }
         else
         {
           String filename = null;
           if( e.getSource() == evFileButton)
              filename = Datafilename;
           else if( e.getSource() == detFileButton)
               filename = Detfilename;
           else if( e.getSource() == incFileButton)
              filename = Incfilename;
           else if( e.getSource() == detEffFileButton)
              filename = DetEfffilename;
           else if( e.getSource() == bankFileButton)
              filename = Bankfilename_l;
           else if( e.getSource() == IDmapFileButton)
              filename = IDMapfilename_l;
           else if( e.getSource() == matFileButton)
              filename = Matfilename;
           
           
            final JFileChooser fc = new JFileChooser(  filename);
            File file = null;
            int returnVal = fc.showOpenDialog(null);
            
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
               file = fc.getSelectedFile();
               if (!file.exists())
               {
                  String error = "File does not exist!";
                  JOptionPane.showMessageDialog(null, error, "Invalid Input",
                                                JOptionPane.ERROR_MESSAGE);
                  return;
               }
               //else
               //   System.out.println("Selected: " + file.getPath());
            } 
            else if (returnVal == JFileChooser.CANCEL_OPTION)
            {
               //System.out.println("Open command cancelled by user.");
               return;
            }
            else if (returnVal == JFileChooser.ERROR_OPTION)
            {
               //System.out.println("Error with file chooser.");
               JOptionPane.showMessageDialog( null, 
                                             "Error opening file", 
                                             "Error Opening File!", 
                                              JOptionPane.ERROR_MESSAGE);
               return;
            }
            
            if (e.getSource() == evFileButton && file != null)
            {
               long file_size = file.length();
               if ( file_size % 8 != 0 )//|| file.toString().indexOf(".dat") < 0)
               {
                  evFileName.setText("");
                  availableEvents.setText("");
                  firstEvent.setText("");
                  eventsToLoad.setText("");
                  eventsToShow.setText("");
                  
                  String error = file.getName() + "is not an event file";
                  JOptionPane.showMessageDialog( null, 
                                                 error, 
                                                "Error", 
                                                 JOptionPane.ERROR_MESSAGE);
               }
               else
               {
                  setEventData(file);
                 
                  Datafilename =file.getPath();
                
               }
            }
            else if (e.getSource() == detFileButton)
            {
               //if ( file.toString().indexOf("grid") < 0)
               //{
               //   String error = file.getName() + "is not an event file";
               //   JOptionPane.showMessageDialog(null, error, "Error", 
               //                                 JOptionPane.ERROR_MESSAGE);
               //}
               //else
               //{
                  detFileName.setText(file.getPath());
                  Detfilename = detFileName.getText();
               //}
            }
            else if (e.getSource() == incFileButton)
            {
               incFileName.setText(file.getPath());
               Incfilename = incFileName.getText();
            }
            else if (e.getSource() == detEffFileButton)
            {
               detEffFileName.setText(file.getPath());
               DetEfffilename = detEffFileName.getText();
            }
            else if (e.getSource() == bankFileButton)
            {
               bankFileName.setText(file.getPath());
               Bankfilename_l = bankFileName.getText();
            }
            else if (e.getSource() == IDmapFileButton)
            {
               IDmapFileName.setText(file.getPath());
               IDMapfilename_l = IDmapFileName.getText();
            }
            else if (e.getSource() == matFileButton)
            {
               //if (file.toString().indexOf("mat") < 0)
               //{
               //   String error = file.getName() + "is not a matrix file";
               //   JOptionPane.showMessageDialog(null, error, "Error", 
               //                                 JOptionPane.ERROR_MESSAGE);
               //}
               //else
               //{
                  matFileName.setText(file.getPath());
                  Matfilename = matFileName.getText();
               //}
            }
         }
      }
   }
   
   /**
    * Sends a message to the message center
    * 
    * @param command Command Name for others to listen to.
    * @param value Object to send to the listener.
    */
   private void sendMessage(String command, Object value)
   {
      Message message = new Message( command,
                                     value,
                                     true,
                                     true );
      
      message_center.send( message );
   }
   
   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("Testing MessageCenter");
      TestReceiver tc = new TestReceiver("FilePanel TestingMessages");
      mc.addReceiver(tc, Commands.LOAD_FILE);
      
      filePanel fp = new filePanel(mc);
      
      JFrame View = new JFrame( "Test File Panel" );
      View.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      View.setBounds(10,10, 300, 275);
      View.setVisible(true);
      
      View.add(fp.getPanel());
      new UpdateManager(mc, null, 100);
   }
}
