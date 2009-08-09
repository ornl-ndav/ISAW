package EventTools.ShowEventsApp.Controls;

import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import EventTools.ShowEventsApp.Command.*;
import MessageTools.*;

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
   private JTextField         availableEvents;
   private JTextField         firstEvent;
   private JTextField         eventsToLoad;
   private JTextField         eventsToShow;
   
   public filePanel(MessageCenter message_center)
   {
      this.message_center = message_center;
      //this.setSize(300, 265);
      buildPanel();
      //this.add(panel);
   }
   
   public JPanel getPanel()
   {
      return panel;
   }
   
   public void setVisible(boolean visible)
   {
      panel.setVisible(visible);
   }
   
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
      sub_panel.add(buildDetEffPanel());
      sub_panel.add(buildMatPanel());
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
      eventPanel.setLayout(new GridLayout(5,2));

      evFileButton = new JButton("Event File...");
      evFileButton.addActionListener(new button());
      
      String default_evFileName =
                            "/usr2/SNAP_2/EVENTS/SNAP_240_neutron_event.dat";
      evFileName = new JTextField( default_evFileName );
      evFileName.addMouseListener(new mouse());
      evFileName.setEditable(false);
      
      String default_availableEvents = "100000000";
      JLabel available = new JLabel("# of Events Available: ");
      availableEvents = new JTextField( default_availableEvents );
      
      String default_firstEvent = "1";
      JLabel first = new JLabel("First Event: ");
      firstEvent = new JTextField( default_firstEvent );
      
      String default_numLoad = "100000000";
      JLabel numLoad = new JLabel("Num. to Load: ");
      eventsToLoad = new JTextField( default_numLoad );
      
      String default_eventsToShow = "5000000";
      JLabel maxEvents = new JLabel("# of Events to Show: ");
      eventsToShow = new JTextField( default_eventsToShow );

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
      
      eventPanel.validate();
      
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
      
      detFileButton = new JButton("Det. File...");
      detFileButton.addActionListener(new button());
      
      String default_detector_file ="/usr2/SNS_SCD_TEST_3/SNAP_1_Panel.DetCal";
      detFileName = new JTextField( default_detector_file);
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
      
      incFileButton = new JButton("Inc. Spect. File...");
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
    * @return
    */
   private JPanel buildDetEffPanel()
   {
      JPanel detEffPanel = new JPanel();
      detEffPanel.setLayout(new GridLayout(1,2));
      
      detEffFileButton = new JButton("Det. Effic. File...");
      detEffFileButton.addActionListener(new button());
      
      detEffFileName = new JTextField();
      detEffFileName.addMouseListener(new mouse());
      detEffFileName.setEditable(false);
      
      detEffPanel.add(detEffFileButton);
      detEffPanel.add(detEffFileName);
      
      return detEffPanel;
   }
   
   /**
    * Builds the MatPanel which consists of button to load the 
    * det file and a textfield to contain path.
    * 
    * @return JPanel
    */
   private JPanel buildMatPanel()
   {
      JPanel matPanel = new JPanel();
      matPanel.setLayout(new GridLayout(1,2));
      
      matFileButton = new JButton("Mat. File...");
      matFileButton.addActionListener(new button());
      
      matFileName = new JTextField();
      matFileName.addMouseListener(new mouse());
      matFileName.setEditable(false);
      
      matPanel.add(matFileButton);
      matPanel.add(matFileName);
      
      return matPanel;
   }
   
   public void setEventFile(String inFile)
   {
      File file = new File(inFile);
      setEventData(file);
   }
   
   public void setDetectorFile(String inFile)
   {
      detFileName.setText(inFile);
   }
   
   public void setIncSpecFile(String inFile)
   {
      incFileName.setText(inFile);
   }
   
   public void setDetEffFile(String inFile)
   {
      detEffFileName.setText(inFile);
   }
   
   public void setMatrixFile(String inFile)
   {
      matFileName.setText(inFile);
   }
   
   private void setEventData(File inFile)
   {
      long file_size = inFile.length();
      String size = String.valueOf((file_size / 8));
      
      evFileName.setText(inFile.getPath());
      availableEvents.setText(size);
      firstEvent.setText("1");
      eventsToLoad.setText(size);
      if (Integer.parseInt(size) > 5000000)
         eventsToShow.setText("5000000");
      else
         eventsToShow.setText(size);
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
      if (evFileName.getText().length() == 0)
         return false;         
      
      if (availableEvents.getText().length() == 0)
         return false;
      
      if (firstEvent.getText().length() == 0)
         return false;
      
      if (eventsToLoad.getText().length() == 0)
         return false;
      
      if (eventsToShow.getText().length() == 0)
         return false;
      
      if (detFileName.getText().length() == 0)
         return false;
      
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
               LoadEventsCmd fileInfo = 
                  new LoadEventsCmd(evFileName.getText(),
                                detFileName.getText(),
                                incFileName.getText(),
                                detEffFileName.getText(),
                                matFileName.getText(),
                                Integer.parseInt(availableEvents.getText()),
                                Integer.parseInt(firstEvent.getText()),
                                Integer.parseInt(eventsToLoad.getText()),
                                Integer.parseInt(eventsToShow.getText()));
            
               sendMessage(Commands.LOAD_FILE, fileInfo);
            }
            else
            {
               String error = "There is file information not completely filled out or invalid!!";
               JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
         }
         else
         {
            final JFileChooser fc = new JFileChooser();
            File file = null;
            
            int returnVal = fc.showOpenDialog(null);
            
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
               file = fc.getSelectedFile();
               System.out.println("Selected: " + file.getPath());
            } 
            else if (returnVal == JFileChooser.CANCEL_OPTION)
            {
               System.out.println("Open command cancelled by user.");
               return;
            }
            else if (returnVal == JFileChooser.ERROR_OPTION)
            {
               System.out.println("Error with file chooser.");
               JOptionPane.showMessageDialog( null, 
                                             "Error opening file", 
                                             "Error Opening File!", 
                                              JOptionPane.ERROR_MESSAGE);
               return;
            }
            
            if (e.getSource() == evFileButton)
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
                  
                  /*String size = String.valueOf((file_size / 8));
                  evFileName.setText(file.getPath());
                  availableEvents.setText(size);
                  firstEvent.setText("0");
                  eventsToLoad.setText(size);
                  eventsToShow.setText(size);*/
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
               //}
            }
            else if (e.getSource() == incFileButton)
            {
               incFileName.setText(file.getPath());
            }
            else if (e.getSource() == detEffFileButton)
            {
               detEffFileName.setText(file.getPath());
            }
            else if (e.getSource() == matFileButton)
            {
               if (file.toString().indexOf("mat") < 0)
               {
                  String error = file.getName() + "is not an event file";
                  JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.ERROR_MESSAGE);
               }
               else
               {
                  matFileName.setText(file.getPath());
               }
            }
         }
      }
   }
   
   private void sendMessage(String command, Object value)
   {
      Message message = new Message( command,
                                     value,
                                     true );
      
      message_center.receive( message );
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
