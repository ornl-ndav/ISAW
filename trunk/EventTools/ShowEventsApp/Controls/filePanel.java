package EventTools.ShowEventsApp.Controls;

import java.awt.GridLayout;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Scanner;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

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
   private JTextField         numThreads;
   private JTextField         availableEvents;
   private JTextField         firstEvent;
   private JTextField         eventsToLoad;
   private JTextField         firstEventToShow;
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

   private void getDefaultData()
   {
      try
      {
         String defaultFile = "/home/fischerp/Desktop/IsawProps.dat";
         FileReader     f_in        = new FileReader( defaultFile );
         BufferedReader buff_reader = new BufferedReader( f_in );
         Scanner        sc          = new Scanner( buff_reader );
         
         detFileName.setText(sc.next());
         incFileName.setText(sc.next());
         detEffFileName.setText(sc.next());
         matFileName.setText(sc.next());
         
         if (sc.hasNextLine())
         {
            availableEvents.setText(sc.next());
            firstEvent.setText(sc.next());
            eventsToLoad.setText(sc.next());
            firstEventToShow.setText(sc.next());
            eventsToShow.setText(sc.next());
         }
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
      }   
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

      sub_panel.setLayout( new GridLayout(6,1) );
      sub_panel.add(buildDetPanel());
      sub_panel.add(buildIncPanel());
      sub_panel.add(buildDetEffPanel());
      sub_panel.add(buildMatPanel());
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

      evFileButton = new JButton("Event File...");
      evFileButton.addActionListener(new button());
      
      String default_evFileName =
                            "/usr2/SNAP_2/EVENTS/SNAP_240_neutron_event.dat";
      evFileName = new JTextField( default_evFileName );
      evFileName.addMouseListener(new mouse());
      evFileName.setEditable(false);
      
      String default_availableEvents = nf.getInstance().format(10000000);
      JLabel available = new JLabel("# of Events Available: ");
      availableEvents = new JTextField( default_availableEvents );
      availableEvents.setEditable(false);
      availableEvents.setHorizontalAlignment(JTextField.RIGHT);
      
      String default_firstEvent = "1";
      JLabel first = new JLabel("First Event: ");
      firstEvent = new JTextField( default_firstEvent );
      firstEvent.setHorizontalAlignment(JTextField.RIGHT);
      
      String default_numLoad = nf.getInstance().format(10000000);
      JLabel numLoad = new JLabel("Num. to Load: ");
      eventsToLoad = new JTextField( default_numLoad );
      eventsToLoad.setHorizontalAlignment(JTextField.RIGHT);
      
      String default_firstToShow = "1";
      JLabel firstEventToShowLbl = new JLabel("First Event to Show: ");
      firstEventToShow = new JTextField( default_firstToShow );
      firstEventToShow.setHorizontalAlignment(JTextField.RIGHT);
      
      String default_eventsToShow = nf.getInstance().format(5000000);
      JLabel maxEvents = new JLabel("# of Events to Show: ");
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
      eventPanel.add(firstEventToShowLbl);
      eventPanel.add(firstEventToShow);
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
      try
      {
         if (Integer.parseInt(size) > 5000000)
            eventsToShow.setText("5000000");
         else
            eventsToShow.setText(size);
      }
      catch (NumberFormatException nf)
      {
         System.out.println(nf.getStackTrace());
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
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }

      if (firstEvent.getText().equals(""))
      {
         String error = "You have not specified the first event to load!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }

      try
      {
         num = nf.parse(firstEvent.getText()).longValue();
      }
      catch (ParseException pe)
      {
         String error = "First event must be of type Integer!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      if (eventsToLoad.getText().equals(""))
      {
         String error = "You have not specified the number of events to load!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      try
      {
         num = nf.parse(eventsToLoad.getText()).longValue();
      }
      catch (ParseException pe)
      {
         String error = "Number of events to load must be of type Integer!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      if (firstEventToShow.getText().equals(""))
      {
         String error = "You have not specified the first event to display!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      try
      {
         num = nf.parse(firstEventToShow.getText()).longValue();
      }
      catch (ParseException pe)
      {
         String error = "First event to display must be of type Integer!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      if (eventsToShow.getText().equals(""))
      {
         String error = "You have not specified the number of events to display!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      try
      {
         num = nf.parse(eventsToShow.getText()).longValue();
      }
      catch (ParseException pe)
      {
         String error = "Number of events to show must be of type Integer!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      if (detFileName.getText().equals(""))
      {
         String error = "You have not specified a detector file!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      if (numThreads.getText().equals(""))
      {
         String error = "You have not specified the number of threads to use!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      try
      {
         Integer.parseInt(numThreads.getText());
      }
      catch (NumberFormatException nfe)
      {
         String error = "Number of threads to show must be of type Integer!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
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
                  NumberFormat nf = NumberFormat.getInstance();
                  
                  LoadEventsCmd fileInfo = 
                     new LoadEventsCmd(evFileName.getText(),
                                detFileName.getText(),
                                incFileName.getText(),
                                detEffFileName.getText(),
                                matFileName.getText(),
                                nf.parse(availableEvents.getText()).longValue(),
                                nf.parse(firstEvent.getText()).longValue(),
                                nf.parse(eventsToLoad.getText()).longValue(),
                                nf.parse(eventsToShow.getText()).longValue(),
                                Integer.parseInt(numThreads.getText()));
                  
                  sendMessage(Commands.LOAD_FILE, fileInfo);
               }
               catch (ParseException pe)
               {
                  System.out.println(pe.getStackTrace());
               }
            }
            //else
            //{
            //   String error = "There is file information not completely filled out or invalid!!";
            //   JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
            //}
         }
         else
         {
            final JFileChooser fc = new JFileChooser();
            File file = null;
            
            int returnVal = fc.showOpenDialog(null);
            
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
               file = fc.getSelectedFile();
               if (file.exists())
                  System.out.println("Selected: " + file.getPath());
               else
               {
                  String error = "File does not exist!";
                  JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
                  return;
               }
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
               //if (file.toString().indexOf("mat") < 0)
               //{
               //   String error = file.getName() + "is not a matrix file";
               //   JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.ERROR_MESSAGE);
               //}
               //else
               //{
                  matFileName.setText(file.getPath());
               //}
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
