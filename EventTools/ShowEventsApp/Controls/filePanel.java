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

import java.awt.GridLayout;
import java.text.NumberFormat;
import java.text.ParseException;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import DataSetTools.util.SharedData;
import EventTools.ShowEventsApp.Command.*;
import EventTools.EventList.SNS_Tof_to_Q_map;
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
   private JButton            evFileButton;
   private JButton            detFileButton;
   private JButton            incFileButton;
   private JButton            detEffFileButton;
   private JButton            matFileButton;
   private JButton            loadFiles;
   private JTextField         evFileName;
   private JTextField         detFileName;
   private JTextField         incFileName;
   private JTextField         detEffFileName;
   private JTextField         matFileName;
   private JTextField         maxQValue;
   private JTextField         numThreads;
   private JTextField         availableEvents;
   private JTextField         firstEvent;
   private JTextField         eventsToLoad;
   //private JTextField         firstEventToShow;
   private JTextField         eventsToShow;
   private String             Datafilename;// Remember last file chosen
  
   private String             Detfilename;// Remember last file chosen
   private String             Incfilename;// Remember last file chosen
   private String             DetEfffilename;// Remember last file chosen
   private String             Matfilename;// Remember last file chosen
   private float              MaxQValue;//Remember last MaxQValue
   
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
      SharedData sd = new SharedData();//Will read in IsawProps.dat
      Datafilename = System.getProperty("Data_Directory");
      Detfilename =  System.getProperty( "InstrumentInfoDirectory");
      Incfilename = DetEfffilename =Detfilename;
      Matfilename = Datafilename;
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
      panel.setBorder(new TitledBorder("File Selection Options"));
      panel.setLayout(new GridLayout(2,1));
    
      loadFiles = new JButton("Load");
      loadFiles.addActionListener(new button());
      
      panel.add(buildEventPanel());
      
      JPanel sub_panel = new JPanel();
      panel.add( sub_panel );

      sub_panel.setLayout( new GridLayout(5,1) );
      sub_panel.add(buildDetPanel());
      sub_panel.add(buildIncPanel());
      //sub_panel.add(buildDetEffPanel());
      //sub_panel.add(buildMatPanel());
      sub_panel.add(buildMaxQPanel());
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
      eventPanel.setLayout(new GridLayout(5,2));

      evFileButton = new JButton("Neutron Event File...");
      evFileButton.addActionListener(new button());
      
      
      String default_evFileName = "";//Datafilename;
                           // "/usr2/SNAP_2/EVENTS/SNAP_240_neutron_event.dat";
      
      evFileName = new JTextField( default_evFileName );
      evFileName.addMouseListener(new mouse());
      evFileName.setEditable(false);
      
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
      
      return eventPanel;
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
      
      detFileButton = new JButton("Detector Position File...");
      detFileButton.addActionListener(new button());
      
//      String default_detector_file =Detfilename;
         // /usr2/SNS_SCD_TEST_3/SNAP_1_Panel.DetCal";
      detFileName = new JTextField( "");
      detFileName.addMouseListener(new mouse());
      detFileName.setEditable(false);
      
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
      
      incFileButton = new JButton("Incident Spectrum File...");
      incFileButton.addActionListener(new button());
      
      incFileName = new JTextField();
      incFileName.addMouseListener(new mouse());
      incFileName.setEditable(false);
      
      incPanel.add(incFileButton);
      incPanel.add(incFileName);
      
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
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
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
      long num;
      
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
         num = nf.parse(firstEvent.getText()).longValue();
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
         num = nf.parse(eventsToLoad.getText()).longValue();
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
         num = nf.parse(firstEventToShow.getText()).longValue();
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
         num = nf.parse(eventsToShow.getText()).longValue();
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
            if(valid())
            {
               try
               {
                  String ev_file = evFileName.getText();
                  String instrument_name = "UNKNOWN";

                  if ( ev_file.indexOf("SNAP") >= 0 )
                    instrument_name = SNS_Tof_to_Q_map.SNAP;

                  else if ( ev_file.indexOf("ARCS") >= 0 )
                    instrument_name = SNS_Tof_to_Q_map.ARCS;

                  else if ( ev_file.indexOf("SEQ") >= 0 )
                    instrument_name = SNS_Tof_to_Q_map.SEQ;

                  else if ( ev_file.indexOf("TOPAZ") >= 0 )
                    instrument_name = SNS_Tof_to_Q_map.TOPAZ;

                  String det_file = detFileName.getText();
                  if ( det_file != null && det_file.trim().length() <= 0 )
                    det_file = null;

                  String inc_spec_file = incFileName.getText();
                  if ( inc_spec_file != null &&
                       inc_spec_file.trim().length() <= 0 )
                    inc_spec_file = null;

                  SetNewInstrumentCmd new_inst_cmd = 
                           new SetNewInstrumentCmd( instrument_name, 
                                                    det_file, 
                                                    inc_spec_file );
                  sendMessage( Commands.SET_NEW_INSTRUMENT, new_inst_cmd );
                  // The SET_NEW_INSTRUMENT command needs to be done before
                  // loading the event file, so that the histogram and
                  // mapping to Q are set up by the time we start sending
                  // in events.

                  NumberFormat nf = NumberFormat.getInstance();
                  long startEvent = nf.parse(firstEvent.getText()).longValue();
                  if (startEvent <= 0)
                  {
                     startEvent = 0;
                     firstEvent.setText(Long.toString(startEvent));
                  }
                  else
                     startEvent -= 1;
                  
                  LoadEventsCmd fileInfo = 
                     new LoadEventsCmd(
                                ev_file,
                                det_file,
                                inc_spec_file,
                                null,            //detEffFileName.getText(),
                                null,
                                MaxQValue,
                                nf.parse(availableEvents.getText()).longValue(),
                                startEvent, 
                                nf.parse(eventsToLoad.getText()).longValue(),
                                nf.parse(eventsToShow.getText()).longValue(),
                                Integer.parseInt(numThreads.getText()));
                  
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
                  JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
                  return;
               }
            }
            //else
            //{
            //   String error = "There is file information not completely filled out or invalid!!";
            //   JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
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
                  JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
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
               //   JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.ERROR_MESSAGE);
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
            else if (e.getSource() == matFileButton)
            {
               //if (file.toString().indexOf("mat") < 0)
               //{
               //   String error = file.getName() + "is not a matrix file";
               //   JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.ERROR_MESSAGE);
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
