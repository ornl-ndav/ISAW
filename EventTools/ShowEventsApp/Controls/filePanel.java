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

import java.awt.event.*;
import java.io.*;
import java.net.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import DataSetTools.util.SharedData;
import EventTools.EventList.SNS_Tof_to_Q_map;
import EventTools.EventList.FileUtil;
import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;

/**
 * Creates a panel to load an event file.
 * Displays how many events are in the event file, first event
 * to load, how many events to load, and how many events to show.
 * Also has fields for a detector file, incident spectrum file,
 * max Q to load and the number of threads to use.
 */
public class filePanel implements IReceiveMessage 
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
   private JTextField         smuAbsorption;
   private JTextField         amuAbsorption;
   private JTextField         maxQValue;
   private JTextField         numThreads;
   private JTextField         availableEvents;
   private JTextField         firstEvent;
   private JTextField         eventsToLoad;
   private JTextField         eventsToShow;
   private JTextField         protonsOnTarget;
   private JTextField         eventsToShowUDP;

   private JCheckBox            UseManualPort;
   private FilteredPG_TextField Port;
   private JComboBox            Instrument;

   private String             Datafilename;

   private long               numAvailable;
   private long               firstToLoad; 
   private long               num_to_load;
   private long               num_to_show;
   private long               num_to_show_UDP;
   private int                num_threads;

   private boolean            use_manual_port;
   private int                udp_port;
   private int                auto_connect_port; // TCP port to request UDP
                                                 // port for live events 
   private InputStream        tcp_input_stream;
   private OutputStream       tcp_output_stream;
   private Socket             tcp_socket;
   private String[][]         inst_computer;     // 2D array listing instrument
                                                 // name in col 1 and instrument
                                                 // computer in col 2.
   private String             detcal_filename;
   private String             inc_spec_filename;
   private String             DetEfffilename;
   private String             bank_filename; 
   private String             idmap_filename;
   private String             Matfilename; 

   private float              scale_factor;    // 1/protons_on_target 0 to skip
   private float              MaxQValue;
   private float              absorption_radius;
   private float              absorption_smu;
   private float              absorption_amu;

   /**
    * Creates file panel as well as sets up default
    * properties for the file load locations.
    * 
    * @param message_center
    */
   public filePanel(MessageCenter message_center)
   {
      this.message_center = message_center;
      message_center.addReceiver(this, Commands.EXIT_APPLICATION);

      new SharedData();               // Read in IsawProps.dat

      Datafilename = System.getProperty("Data_Directory");
      Matfilename  = Datafilename;

      use_manual_port   = false;
      auto_connect_port = 9000;
      inst_computer     = FileUtil.LoadSupportedSNS_InstrumentInfo();

      numAvailable    = 10000000;
      firstToLoad     = 1; 
      num_to_load     = 25000000;
      num_to_show     =  5000000;

      udp_port        = 8002;
      num_to_show_UDP = 5000000;

      scale_factor = -1;

      detcal_filename   = "";
      inc_spec_filename = "";
      DetEfffilename    = "";
      bank_filename     = "";
      idmap_filename    = "";

      MaxQValue = 1000000;            // use all Q values by default
      absorption_radius = 0;          // 0 means don't do absorption correction
      absorption_smu  = 1;
      absorption_amu   = 1;

      buildPanel();
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
    * Builds the panel to contain the EventPanel, DetPanel and MatPanel.
    */
   private void buildPanel()
   {
      panel = new JPanel();
      panel.setBorder(new TitledBorder("Load Data"));
      panel.setLayout(new GridLayout(2,1));
      tabPane = new JTabbedPane();
      loadFiles = new JButton("Load");
      loadFiles.addActionListener(new LoadListener());
      
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
      JPanel eventPanel = new JPanel();
      eventPanel.setLayout(new GridLayout(6,2));

      evFileButton = new JButton("Neutron Event File");
      evFileButton.addActionListener(new button());
      
      String default_evFileName = "";
      
      evFileName = new JTextField( default_evFileName );
      evFileName.addActionListener( new EventFileTextListener() );
      evFileName.addMouseListener(new mouse());
      
      JLabel available = new JLabel("Number of Events in File: ");
      availableEvents = new JTextField( ""+numAvailable );
      availableEvents.setEditable(false);
      availableEvents.setHorizontalAlignment(JTextField.RIGHT);
      
      String default_firstEvent = "1";
      JLabel first = new JLabel("First Event to Load: ");
      firstEvent = new JTextField( default_firstEvent );
      firstEvent.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel numLoad = new JLabel("Number to Load: ");
      eventsToLoad = new JTextField( ""+num_to_load );
      eventsToLoad.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel maxEvents = new JLabel("Number to Show in 3D: ");
      eventsToShow = new JTextField( ""+num_to_show );
      eventsToShow.setHorizontalAlignment(JTextField.RIGHT);

      protonsOnTarget = new JTextField( "0" );
      protonsOnTarget.setHorizontalAlignment(JTextField.RIGHT);
      
      eventPanel.add(evFileButton);
      eventPanel.add(evFileName);
      eventPanel.add(available);
      eventPanel.add(availableEvents);
      eventPanel.add(first);
      eventPanel.add(firstEvent);
      eventPanel.add(numLoad);
      eventPanel.add(eventsToLoad);
      eventPanel.add(maxEvents);
      eventPanel.add(eventsToShow);
      eventPanel.add( new JLabel("Protons On Target"));
      eventPanel.add( protonsOnTarget );
      return eventPanel;
   }
   
   public JPanel buildUDPPanel()
   {
      JPanel Res = new JPanel();
      BoxLayout BL = new BoxLayout( Res, BoxLayout.Y_AXIS);
      Res.setLayout( BL );

      JPanel subPanel = new JPanel();
      subPanel.setLayout(  new GridLayout(1,2) );
      subPanel.add( new JLabel("Instrument"));
      Instrument = new JComboBox( FileUtil.SupportedSNS_Instruments() );
      Instrument.setSelectedIndex( 0 );
      subPanel.add( Instrument);
      Res.add(  subPanel );
      
      subPanel = new JPanel();
      subPanel.setLayout( new GridLayout(1,2) );
      UseManualPort = new JCheckBox("Only Connect to Port ->",use_manual_port);
      UseManualPort.setToolTipText( 
            "<html><body> Check box to NOT use TCP auto-connect port.<Br>"+
               "NOTE: If LDP is off, use 8002 to listen to DAS directly,<Br>"+
               "otherwise use the port on which LDP is forwarding events.<P>"+
               "Also \"enable\" Pass-Thru Data Port<BR> on the LDP monitor"+
               "</body></html>");
      subPanel.add( UseManualPort );
      Port = new FilteredPG_TextField( new IntegerFilter() );
      Port.setText( ""+udp_port );
      Port.setHorizontalAlignment(JTextField.RIGHT);
      subPanel.add( Port );

      subPanel.setBorder(  new LineBorder( java.awt.Color.black) );
      Res.add( subPanel );
      
      JLabel maxEvents = new JLabel("Number to Show in 3D: ");
      eventsToShowUDP = new JTextField( ""+num_to_show_UDP );
      eventsToShowUDP.setHorizontalAlignment(JTextField.RIGHT);
      subPanel = new JPanel();
      subPanel.setLayout( new GridLayout(1,2));
      subPanel.add( maxEvents );
      subPanel.add( eventsToShowUDP );
      Res.add( subPanel );
      
      ActionListener list = new UDPActionListener();
      subPanel= new JPanel( new GridLayout(1,3) );
      JButton Pause = new JButton( "Pause" );
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
      
      detFileName = new JTextField( detcal_filename );
      detFileName.addMouseListener(new mouse());
      
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
      
      MaxQValue = 20;
      maxQValue = new JTextField( "" + MaxQValue );
      maxQValue.setHorizontalAlignment(JTextField.RIGHT);
      maxQValue.addActionListener( new button());
      maxQPanel.add(maxQButton);
      maxQPanel.add(maxQValue);
      //maxQPanel.add(matFileName);
      
      return maxQPanel;
   }

   /**
    * Build the absorptionPanel. 
    * Radius of the sample for the absorption correction is loaded into 
    * appropriate textfields.
    * 
    * @return JPanel
    */
   private JPanel buildAbsorptionPanel1()
   {
      JPanel absorptionPanel = new JPanel();
      absorptionPanel.setLayout(new GridLayout(1,2));

      absorption_radius = 0.0f; 
      JLabel radAbsorption = new JLabel("Absorption Corr. Radius(cm): ");
      absorptionRadius = new JTextField( ""+absorption_radius );
      absorptionRadius.setHorizontalAlignment(JTextField.RIGHT);

      absorptionPanel.add(radAbsorption);
      absorptionPanel.add(absorptionRadius);

      return absorptionPanel;
   }

   /**
    * Build the absorptionPanel. 
    * Scattering absorption mu for the absorption correction is loaded 
    * into appropriate textfields.
    * 
    * @return JPanel
    */
   private JPanel buildAbsorptionPanel2()
   {
      JPanel absorptionPanel = new JPanel();
      absorptionPanel.setLayout(new GridLayout(1,2));

      absorption_smu = 1.0f;
      JLabel total = new JLabel("smu scattering(1/cm): ");
      smuAbsorption = new JTextField( ""+absorption_smu );
      smuAbsorption.setHorizontalAlignment(JTextField.RIGHT);
      
      absorptionPanel.add(total);
      absorptionPanel.add(smuAbsorption);

      return absorptionPanel;
   }

   /**
    * Build the absorptionPanel. 
    * Linear absorption coefficient for the absorption correction is 
    * loaded into appropriate textfields.
    * 
    * @return JPanel
    */
   private JPanel buildAbsorptionPanel3()
   {
      JPanel absorptionPanel = new JPanel();
      absorptionPanel.setLayout(new GridLayout(1,2));

      absorption_amu = 1.0f;
      JLabel trueAbs = new JLabel("amu absorption(1/cm): ");
      amuAbsorption = new JTextField( ""+absorption_amu );
      amuAbsorption.setHorizontalAlignment(JTextField.RIGHT);
      
      absorptionPanel.add(trueAbs);
      absorptionPanel.add(amuAbsorption);

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
      
      num_threads = 4;
      JLabel numThreadsLbl = new JLabel("Number of Threads");
      numThreads = new JTextField(""+num_threads);
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
   public boolean setEventFile(String inFile)
   {
      File file = new File(inFile);
      return setEventData(file);
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
   private boolean setEventData(File inFile)
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

         return true;
      }
      catch (NumberFormatException nfe)
      {
         //System.out.println(nfe.getStackTrace());
         ShowError( "Error parsing number of events to load or show" );
         return false;
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
            evFileName.setToolTipText(evFileName.getText().trim());
         
         if (e.getSource() == detFileName)
            detFileName.setToolTipText(detFileName.getText().trim());
         
         if (e.getSource() == incFileName)
            incFileName.setToolTipText(incFileName.getText().trim());
         
         if (e.getSource() == detEffFileName)
            detEffFileName.setToolTipText(detEffFileName.getText().trim());
         
         if (e.getSource() == matFileName)
            matFileName.setToolTipText(matFileName.getText().trim());
      }

      public void mouseExited(MouseEvent e){}
      public void mousePressed(MouseEvent e){}
      public void mouseReleased(MouseEvent e){}
   }


   /**
    * Check whether each field needed by both the load file and UDP
    * load operations are usable, and have values set in the 
    * file level variables.
    * 
    * @return True if every field is valid, false if not.
    */
   private boolean common_info_valid()
   {
     boolean exception = false;
                                     // if we specify .DetCal, it must exist
     detcal_filename = detFileName.getText().trim();
     if ( detcal_filename != null && detcal_filename.trim().length() > 0 )
     {
       if ( !CheckFileExists(detcal_filename, ".DetCal file doesn't exist ") )
         return false;
     }

     bank_filename = bankFileName.getText().trim();
     if ( bank_filename != null && bank_filename.trim().length() > 0 )
     {
       if ( !CheckFileExists(bank_filename, "Banking file doesn't exist ") )
         return false;
     }

     idmap_filename = IDmapFileName.getText().trim();
     if ( idmap_filename != null && idmap_filename.trim().length() > 0 )
     {
       if ( !CheckFileExists(idmap_filename, "ID Map file doesn't exist ") )
         return false;
     }

     inc_spec_filename = incFileName.getText().trim();
     if ( inc_spec_filename != null && inc_spec_filename.trim().length() > 0 )
     {
       if ( !CheckFileExists(inc_spec_filename, 
                             "Incident Spectrum file doesn't exits" ) )
       return false;
     }

     try
     {
       MaxQValue = Float.parseFloat( maxQValue.getText().trim() );
     }
     catch( Exception s)
     {
       exception = true;
     }
     if ( exception || MaxQValue <= 0 )
     {
       ShowError(" maxQValue must be a positive number " + 
                   maxQValue.getText() );
       return false;
     }

     try
     {
       absorption_radius =
                         Float.parseFloat( absorptionRadius.getText().trim() );
     }
     catch( Exception s)
     {
       exception = true;
     }
     if ( exception || absorption_radius < 0 )
     {
       ShowError(" Sample radius must be at least 0 (0 to skip) " + 
                   absorptionRadius.getText() );
       return false;
     }

     try
     {
       absorption_smu = Float.parseFloat( smuAbsorption.getText().trim() );
     }
     catch( Exception s)
     {
       exception = true;
     }
     if ( exception || absorption_smu <= 0 )
     {
       ShowError(" Sample scattering mu must be a positive number " + 
                   smuAbsorption.getText() );
       return false;
     }

     try
     {
       absorption_amu = Float.parseFloat( amuAbsorption.getText().trim() );
     }
     catch( Exception s)
     {
       exception = true;
     }
     if ( exception || absorption_amu <= 0 )
     {
       ShowError(" Sample absorption mu must be a positive number " + 
                   amuAbsorption.getText() );
       return false;
     }

     try
     {
       num_threads = Integer.parseInt(numThreads.getText().trim());
     }
     catch ( Exception s )
     {
       exception = true;
     }
     if ( exception || num_threads <= 0 )
     {
        ShowError( "Number of threads to show must be an Integer >= 0!" );
        num_threads = 4;
        return false;
     }

     return true;
   }


   /**
    * Check whether each value needed to get live UDP events 
    * seems valid, and set fields from the GUI components.
    *
    * @return True if every field is valid, false if not.
    */
   private boolean udp_load_info_valid()
   {
      use_manual_port = UseManualPort.isSelected();

      try
      {
        udp_port = Integer.parseInt( Port.getText().trim() );
      }
      catch ( Exception ex )
      {
        ShowError("UDP port must be valid Integer!" );
        return false;
      }

      try
      {
        num_to_show_UDP = Long.parseLong(eventsToShowUDP.getText().trim());
      }
      catch ( Exception ex )
      {
         ShowError("Number of UDP events to show must be of type Integer!" );
         return false;
      }

      return true;
   }
   
   /**
    * Check whether each value needed to load the files
    * seems valid, and set fields from the GUI components.
    * 
    * @return True if every field is valid, false if not.
    */
   private boolean file_info_valid()
   {
      try
      {
        String name = evFileName.getText().trim();
        if ( CheckFileExists( name, "Event file can't be read: ") )
        {
          if ( !name.equals(Datafilename) )    // update file size info
          {
            if ( setEventFile(name) )
              Datafilename = name;
            else
              return false;
          }
        }
        else
          return false;
      }
      catch ( Exception ex )
      {
         ShowError( "First event must be of type Integer!" );
         return false;
      }

      NumberFormat nf = NumberFormat.getInstance();
      try
      {
        numAvailable = nf.parse(availableEvents.getText().trim()).longValue();
      }
      catch( Exception sx )
      {
        ShowError( "Number of Events available must be >= 0" );
        numAvailable = 0;
        return false;
      }

      try
      {
         firstToLoad = Long.parseLong(firstEvent.getText().trim());
      }
      catch (Exception ex)
      {
         ShowError( "First event must be of type Integer!" );
         return false;
      }
      
      try
      {
         num_to_load = nf.parse(eventsToLoad.getText().trim()).longValue();
      }
      catch (Exception ex)
      {
         ShowError( "Number of events to load must be of type Integer!" );
         return false;
      }
      
      try
      {
         num_to_show = nf.parse(eventsToShow.getText().trim()).longValue();
      }
      catch (Exception ex)
      {
         ShowError( "Number of events to show must be of type Integer!" );
         return false;
      }

      try
      {
        float protons_on_target =
                          Float.parseFloat( protonsOnTarget.getText().trim() );
        if ( protons_on_target <= 0 )
          scale_factor = -1;
        scale_factor = 1/protons_on_target;
      }
      catch( Exception sx )
      {
        ShowError( "Protons On Target must be > 0 (or 0 to skip)" );
        scale_factor = -1;
        return false;
      }

      return true;
   }

   
   /**
    *  Try to get the UDP port to connect with, by closing and re-opening
    *  a TCP port to Jim Kohl's event catcher.
    *
    *  @param instrument  The name of the instrument selected by the user
    *
    *  @return The UDP port to use, or -1 if the connection to the TCP port 
    *          failed.
    */
   private int AutoGetUDP_Port( String instrument )
   {
      inst_computer     = FileUtil.LoadSupportedSNS_InstrumentInfo();

                               // first get the name of the instrument computer
      String node_name = "localhost";  
      for ( int i = 0; i < inst_computer.length; i++ )
        if ( instrument.equalsIgnoreCase( inst_computer[i][0] ) )
          node_name = inst_computer[i][1];

      if ( node_name == null )
      {
        System.out.println("WARNING: Failed to find " + instrument +
                           " in list of supported instruments ");
        System.out.println("NOT trying to auto-connect with event server...");
        return -1;
      }

      String lp_property = System.getProperty( "ISAWEV_USE_LOCAL_PORT" );
      if ( lp_property != null && lp_property.equalsIgnoreCase("true") )
        node_name = "localhost";

      CloseTCP_Connection();              // first close TCP connection 
                                          // then reopen it to get the port
      try
      {
        Thread.sleep( 1000 );             // give the OS some time to close it
      }
      catch ( Exception ex )
      {
      }
                                          // now try to reopen the port
      int new_port = -1;
      tcp_socket  = null;
      try
      {
        Util.sendInfo("Trying TCP connect to " + node_name + 
                      " on port " + auto_connect_port );
        tcp_socket = new Socket( node_name, auto_connect_port );
        tcp_socket.setKeepAlive( false );
        tcp_socket.setSoLinger( false, 0 );
        tcp_input_stream  = tcp_socket.getInputStream();
        tcp_output_stream = tcp_socket.getOutputStream();
        int low_byte  = tcp_input_stream.read();
        int high_byte = tcp_input_stream.read();
        new_port  = ((high_byte & 0xFF) << 8) + (low_byte & 0xFF);
      }
      catch ( IOException ex )
      {
        Util.sendInfo( "TCP connect FAILED: " + ex + "\n" +
                       "could not connect to " + node_name );
        return -1;
      }

      System.out.println("NEW UDP PORT = " + new_port );

      return new_port;
   }


   public boolean receive( Message message )
   {
     if ( message.getName().equals(Commands.EXIT_APPLICATION) ) 
     {
       CloseTCP_Connection();
       try
       {
         System.out.println( "Closed TCP Connection" );
         Thread.sleep( 1000 );
       }
       catch ( Exception ex ) 
       {}
     }
     System.exit(0);
     return true;
   }


   /**
    *  Close the TCP port connection, if possible
    */
   private void CloseTCP_Connection()
   {
     try
     {
       if ( tcp_socket != null )
       {
         tcp_socket.shutdownInput();
         tcp_socket.shutdownOutput();
         tcp_input_stream.close();
         tcp_output_stream.close();
         tcp_socket.close();
         System.out.println("Closed TCP connection");
         Thread.sleep(500);
       }
     }
     catch ( Exception ex )
     {
     }
   }

   
   private class UDPActionListener implements ActionListener
   {
      public void actionPerformed( ActionEvent evt)
      {
         String command = evt.getActionCommand();
         if( command.equals( "Pause" ))
         {
            message_center.send( 
                          new Message( Commands.PAUSE_UDP, null,true, true) );
         }else if( command.equals( "Continue" ))
         {

            message_center.send( 
                        new Message( Commands.CONTINUE_UDP, null,true, true) );
         }else if( command.equals("Pause & Clear"))
         {
            message_center.send( 
                          new Message( Commands.CLEAR_UDP, null,true, true) );
         }
      }
   }
   
   /**
    * Used for each button to load the files if Event File, Det file,
    * or Mat File button is pressed.  Also to send a message if Load
    * is pressed containing all the file names as well as the first event,
    * number of events to load, and number of events to load.
    */
   private class LoadListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         boolean collapse_messages   = true;
         boolean use_separate_thread = true;

         if( tabPane.getSelectedIndex() == 1 )
         {
           if ( common_info_valid() && udp_load_info_valid() )
           {
              String instrument_name = Instrument.getSelectedItem().toString();

              if ( !use_manual_port )
              {
                int try_port = AutoGetUDP_Port( instrument_name );
                if ( try_port > 0 )
                  udp_port = try_port;
              }

              Util.sendInfo( "Trying to get live events on port " + udp_port );
              System.out.println("Listening for UDP messages on port: " 
                                  + udp_port);

              LoadUDPEventsCmd cmd =
                new LoadUDPEventsCmd( 
                           instrument_name,
                           udp_port,
                           detcal_filename,
                           inc_spec_filename,
                           null,                    // detector Eff filename 
                           bank_filename,
                           IDmapFileName.getText().trim(),
                           null,
                           absorption_radius,
                           absorption_smu,
                           absorption_amu,
                           MaxQValue,
                           num_to_show_UDP );
              
              Message mess = new Message( Commands.LOAD_UDP_EVENTS,
                                          cmd,
                                          collapse_messages, 
                                          use_separate_thread );
              message_center.send(mess);
            }
          }

          else if( file_info_valid() && common_info_valid() )
          {
            LoadEventsCmd fileInfo = 
              new LoadEventsCmd(  Datafilename,
                                  detcal_filename,
                                  inc_spec_filename,
                                  null,               // detector Eff filename
                                  bank_filename,
                                  IDmapFileName.getText().trim(),         
                                  null,
                                  absorption_radius,
                                  absorption_smu,
                                  absorption_amu,
                                  MaxQValue,
                                  numAvailable,
                                  firstToLoad, 
                                  num_to_load,
                                  num_to_show,
                                  num_threads,
                                  scale_factor );
                 
            Message load_message = new Message( Commands.LOAD_FILE,
                                                fileInfo,
                                                collapse_messages,
                                                use_separate_thread );
            message_center.send( load_message );
          }
      }
   }


  /**
   *  Listens for changes in text field specifying the event data file
   */
   private class EventFileTextListener implements ActionListener
   {
     public void actionPerformed(ActionEvent e)
     {
       if (e.getSource() == evFileName )
       {
         String name = evFileName.getText().trim();
         if ( CheckFileExists( name, "Can't Read Specified Event File : " ) )
           if ( setEventFile( name ) )
             Datafilename = name;
       }
     }
   }


   /**
    * Used for most buttons, except the Load and event file name.
    * In particular it is called if Event File, Det file,
    * or Mat File button is pressed.  Also to send a message if Load
    * is pressed containing all the file names as well as the first event,
    * number of events to load, and number of events to load.
    */
   private class button implements ActionListener
   {
     public void actionPerformed(ActionEvent e)
     {
         if( e.getSource() == maxQValue)
         {
            String Sval = maxQValue.getText().trim();
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
               filename = detcal_filename;
           else if( e.getSource() == incFileButton)
              filename = inc_spec_filename;
           else if( e.getSource() == detEffFileButton)
              filename = DetEfffilename;
           else if( e.getSource() == bankFileButton)
              filename = bank_filename;
           else if( e.getSource() == IDmapFileButton)
              filename = idmap_filename;
           else if( e.getSource() == matFileButton)
              filename = Matfilename;
           
            final JFileChooser fc = new JFileChooser( filename );
            File file = null;
            int returnVal = fc.showOpenDialog(null);
            
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
               file = fc.getSelectedFile();
               if (!file.exists())
               {
                  ShowError( "File does not exist! : " + filename );
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
               ShowError( "Error opening file: " + filename );
               return;
            }
            
            if (e.getSource() == evFileButton && file != null)
            {
               long file_size = file.length();
               if (file_size % 8 != 0 ) //|| file.toString().indexOf(".dat")<0)
               {
                  evFileName.setText("");
                  availableEvents.setText("");
                  firstEvent.setText("");
                  eventsToLoad.setText("");
                  eventsToShow.setText("");
                  ShowError( file.getName() + "is not an event file" );
               }
               else
               {
                  setEventData(file);
                  Datafilename = file.getPath();
               }
            }
            else if (e.getSource() == detFileButton)
            {
               detFileName.setText(file.getPath());
               detcal_filename = detFileName.getText().trim();
            }
            else if (e.getSource() == incFileButton)
            {
               incFileName.setText(file.getPath());
               inc_spec_filename = incFileName.getText().trim();
            }
            else if (e.getSource() == detEffFileButton)
            {
               detEffFileName.setText(file.getPath());
               DetEfffilename = detEffFileName.getText().trim();
            }
            else if (e.getSource() == bankFileButton)
            {
               bankFileName.setText(file.getPath());
               bank_filename = bankFileName.getText().trim();
            }
            else if (e.getSource() == IDmapFileButton)
            {
               IDmapFileName.setText(file.getPath());
               idmap_filename = IDmapFileName.getText().trim();
            }
            else if (e.getSource() == matFileButton)
            {
               matFileName.setText(file.getPath());
               Matfilename = matFileName.getText().trim();
            }
         }
      }
   }


   /**
    *  Check that the specified file exists and can be read.  If NOT, 
    *  popup an error dialog with the specified message.
    *
    *  @param filename The name of the file to check
    *  @param messge   The error message to display if the file
    *                  can't be read.
    *  @return true if the file exists and is readable, false otherwise
    */
   private boolean CheckFileExists( String filename, String message )
   {
     try
     {
       FileUtil.CheckFile( filename );
     }
     catch (Exception ex)
     {
       ShowError( message + filename );
       return false;
     }
     return true;
   }


   /**
    *  Pop up an error dialog box with the specified message
    */
   private void ShowError( String message )
   {
     JOptionPane.showMessageDialog( null,
                                    message,
                                   "Error",
                                    JOptionPane.ERROR_MESSAGE);
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
