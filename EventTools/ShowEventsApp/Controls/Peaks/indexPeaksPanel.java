package EventTools.ShowEventsApp.Controls.Peaks;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.Util.Sys.WindowShower;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;

import MessageTools.*;
import EventTools.ShowEventsApp.Command.*;
import DataSetTools.components.ui.Peaks.*;

public class indexPeaksPanel extends JPanel 
                                    implements IReceiveMessage
                                
{
   public static final long serialVersionUID = 1L;
   private MessageCenter    messageCenter;
   private JTextField       aTxt;
   private JTextField       bTxt;
   private JTextField       cTxt;
   private JTextField       alphaTxt;
   private JTextField       betaTxt;
   private JTextField       gammaTxt;
   private JTextField       toleranceTxt;
   private JTextField       fixedPeakTxt;
   private JTextField       requiredFractionTxt;
   private JButton          applyBtn;
   private JButton          MatFileBtn;  
   private JTextField       MatFileName;
   private JButton          ViewMatBtn;
   private JButton          WriteMatBtn;
   private float[][]        currentOrientationMatrix;
   private boolean          doShow;  //Show next incoming orientation matrix
   private boolean          doIndex;//Index peaks with next incoming 
                                    //orientation matrix
   
   private static String   NoOrientationText="<html><body> There is no "+
                  "Orientation matrix </body></html>";
   
   public indexPeaksPanel(MessageCenter messageCenter)
   {
      this.messageCenter = messageCenter;
      messageCenter.addReceiver( this , Commands.SET_ORIENTATION_MATRIX );
      currentOrientationMatrix = null;
      
      doShow = doIndex = false;
      
      this.setBorder(new TitledBorder("Index Peaks"));
      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
       
      this.add(buildPanel());
      
      //this.add( buildButtonsPanel());
   }
   
   /*private void getDefaultData()
   {
      try
      {
         String defaultFile = "/home/fischerp/Desktop/IsawProps.dat";
         FileReader     f_in        = new FileReader( defaultFile );
         BufferedReader buff_reader = new BufferedReader( f_in );
         Scanner        sc          = new Scanner( buff_reader );
         
         aTxt.setText(sc.next());
         bTxt.setText(sc.next());
         cTxt.setText(sc.next());
         alphaTxt.setText(sc.next());
         betaTxt.setText(sc.next());
         gammaTxt.setText(sc.next());
         toleranceTxt.setText(sc.next());
         fixedPeakTxt.setText(sc.next());
         requiredFractionTxt.setText(sc.next());
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
      }   
   }*/
   private JPanel buildPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(  new BorderLayout() );
      panel.add(  buildFromFilePanel(),BorderLayout.NORTH );
      panel.add( buildCalcMatPanel(), BorderLayout.CENTER);
      panel.add( buildButtonsPanel(), BorderLayout.SOUTH );
      return panel;
   }
   
   private JPanel buildFromFilePanel()
   {
      JPanel panel = new JPanel();
      panel.setBorder( new TitledBorder( new LineBorder(Color.black),
                 "Matrix from File") );
      panel.setLayout( new GridLayout(1,2));
      MatFileBtn = new JButton("Matrix filename");
      MatFileBtn.addActionListener( new buttonListener());
      panel.add( MatFileBtn );
      MatFileName = new JTextField(10);
      MatFileName.setText( "" );
      panel.add(MatFileName );
      return panel;
      
   }
   private JPanel buildCalcMatPanel()
   {
      JPanel panel = buildPanel1();
      panel.setBorder(  new TitledBorder( new LineBorder(Color.black),
               "Calculate Matrix") );
      return panel;
   }
   private JPanel buildButtonsPanel()
   {  
      JPanel panel = new JPanel();
      
      panel.setLayout( new GridLayout(1,3) );
      applyBtn = new JButton("Index Peaks");
      applyBtn.addActionListener(new buttonListener());
      applyBtn.setToolTipText( "<HTML><BODY>Will get Matrix and"+
               "<BR> apply it to the Peaks" );
      panel.add(applyBtn);
      
      
      ViewMatBtn = new JButton("Show Matrix");
      
      WriteMatBtn = new JButton("Write Matrix");
      WriteMatBtn.addActionListener( new buttonListener());
      ViewMatBtn.addActionListener( new buttonListener());
     
      panel.add( WriteMatBtn );
      panel.add( ViewMatBtn );
      return panel;
      
   }
   private JPanel buildPanel1()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(9, 2));
      
      JLabel aLbl = new JLabel("a:");
      String defaultA = "4.913";
      aTxt = new JTextField(defaultA);
      aTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel bLbl = new JLabel("b:");
      String defaultB = "4.913";
      bTxt = new JTextField(defaultB);
      bTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel cLbl = new JLabel("c:");
      String defaultC = "5.40";
      cTxt = new JTextField(defaultC);
      cTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel alphaLbl = new JLabel("alpha:");
      String defaultAlpha = "90";
      alphaTxt = new JTextField(defaultAlpha);
      alphaTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel betaLbl = new JLabel("beta:");
      String defaultBeta = "90";
      betaTxt = new JTextField(defaultBeta);
      betaTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel gammaLbl = new JLabel("gamma:");
      String defaultGamma = "120";
      gammaTxt = new JTextField(defaultGamma);
      gammaTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel toleranceLbl = new JLabel("Tolerance:");
      String defaultTolerance = ".12";
      toleranceTxt = new JTextField(defaultTolerance);
      toleranceTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel fixedPeakLbl = new JLabel("Fixed Peak Index:");
      String defaultFindPeaks = "1";
      fixedPeakTxt = new JTextField(defaultFindPeaks);
      fixedPeakTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      JLabel requiredFractionLbl = new JLabel("Required Fraction:");
      String defaultRequiredFraction = ".4";
      requiredFractionTxt = new JTextField(defaultRequiredFraction);
      requiredFractionTxt.setHorizontalAlignment(JTextField.RIGHT);
      
      panel.add(aLbl);
      panel.add(aTxt);
      panel.add(bLbl);
      panel.add(bTxt);
      panel.add(cLbl);
      panel.add(cTxt);
      panel.add(alphaLbl);
      panel.add(alphaTxt);
      panel.add(betaLbl);
      panel.add(betaTxt);
      panel.add(gammaLbl);
      panel.add(gammaTxt);
      panel.add(toleranceLbl);
      panel.add(toleranceTxt);
      panel.add(fixedPeakLbl);
      panel.add(fixedPeakTxt);
      panel.add(requiredFractionLbl);
      panel.add(requiredFractionTxt);
      
      return panel;
   }
   
   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command, value, true);
      
      messageCenter.receive(message);
   }

   private boolean valid()
   {
      try
      {
         Float.parseFloat(aTxt.getText());
      }
      catch (NumberFormatException e)
      {
         String error = "a must be of type Float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      } 
      
      try
      {
         Float.parseFloat(bTxt.getText());
      }
      catch (NumberFormatException e)
      {
         String error = "b must be of type Float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      } 
      
      try
      {
         Float.parseFloat(cTxt.getText());
      }
      catch (NumberFormatException e)
      {
         String error = "c must be of type Float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      try
      {
         Float.parseFloat(alphaTxt.getText());
      }
      catch (NumberFormatException e)
      {
         String error = "Alpha must be of type Float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      } 
      
      try
      {
         Float.parseFloat(betaTxt.getText());
      }
      catch (NumberFormatException e)
      {
         String error = "Beta must be of type Float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      try
      {
         Float.parseFloat(gammaTxt.getText());
      }
      catch (NumberFormatException e)
      {
         String error = "Gamma must be of type Float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      } 
      
      try
      {
         Float.parseFloat(toleranceTxt.getText());
      }
      catch (NumberFormatException e)
      {
         String error = "Tolerance must be of type Float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      try
      {
         Float.parseFloat(requiredFractionTxt.getText());
      }
      catch (NumberFormatException e)
      {
         String error = "Required Fraction must be of type Float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      try
      {
         Integer.parseInt(fixedPeakTxt.getText()); 
         
      }
      catch (NumberFormatException nfe)
      {
         String error = "Fixed Peaks must be of type Integer!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      

      return true;
   }
   
   
   /* (non-Javadoc)
    * @see MessageTools.IReceiveMessage#receive(MessageTools.Message)
    */
   @Override
   public boolean receive( Message message )
   {

      if( message.getName().equals( Commands.SET_ORIENTATION_MATRIX ))
      {
         currentOrientationMatrix = (float[][])message.getValue();
         
        
         if( doShow)
         {
            String ShowText =null;
            if( currentOrientationMatrix != null)
               ShowText = subs.ShowOrientationInfo( null , 
                          LinearAlgebra.getTranspose( currentOrientationMatrix) ,
                          null , null ,true );
            else
               ShowText = NoOrientationText;
         
            JFrame jf = new JFrame( "Orientation Matrix");
            jf.setSize( 400,200 );
            jf.getContentPane().add( new JEditorPane("text/html", ShowText) );
            jf.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
            
            WindowShower.show(jf);
            doShow = false;
         }
         if( doIndex)
         {
            sendMessage( Commands.INDEX_PEAKS_WITH_ORIENTATION_MATRIX,
                     currentOrientationMatrix);
            doIndex = false;
         }
         return true;
      }
      return false;
   }


   private class buttonListener implements ActionListener
   {
      String lastWriteFileName = System.getProperty("Data_Directory","");
      String lastInpMatFileName= lastWriteFileName;
      
      private String getText( JTextField text)
      {
         if( text == null || text.getText() == null)
            return "";
         return text.getText();
      }
      
      private String Directory(String filename)
      {
         if( filename == null)
            return "";
         String Filename = filename.replace( '\\' , '/' );
         int i= Filename.lastIndexOf( '/' );
         if( i < 0)
            return "";
         String res = Filename.substring( 0,i );
         return res.replace( '/' , java.io.File.separatorChar );
      }
      
      
      public void actionPerformed(ActionEvent e)
      {  String cmd = e.getActionCommand();
         if (cmd.startsWith( "Index" ))
         {
            if( getText(MatFileName).length() > 0)
         
            {
               sendMessage(  Commands.READ_ORIENTATION_MATRIX , getText(MatFileName) );
               doIndex = true;
               return;
            }
            else if (valid())
               {
            IndexPeaksCmd indexCmd 
               = new IndexPeaksCmd(Float.parseFloat(aTxt.getText()), 
                           Float.parseFloat(bTxt.getText()), 
                           Float.parseFloat(cTxt.getText()),
                           Float.parseFloat(alphaTxt.getText()), 
                           Float.parseFloat(betaTxt.getText()), 
                           Float.parseFloat(gammaTxt.getText()), 
                           Float.parseFloat(toleranceTxt.getText()), 
                           Integer.parseInt(fixedPeakTxt.getText()), 
                           Float.parseFloat(requiredFractionTxt.getText()));
           
            sendMessage(Commands.INDEX_PEAKS, indexCmd);
               }
         }else if(cmd.startsWith("Write"))
         {
            JFileChooser jfc = new JFileChooser( Directory(lastWriteFileName));
            if( jfc.showSaveDialog( null )== JFileChooser.APPROVE_OPTION)
            {
               lastWriteFileName = jfc.getSelectedFile().toString();
               messageCenter.receive( 
                         new Message( Commands.WRITE_ORIENTATION_MATRIX, lastWriteFileName, false) );
               
            }
         }else if( cmd.startsWith( "Show" ) )
         {

            sendMessage(  Commands.GET_ORIENTATION_MATRIX ,"" );
            doShow = true;
           
         }else if( cmd.startsWith("Matrix") )
         {

            JFileChooser jfc = new JFileChooser( Directory(lastInpMatFileName));
            if( jfc.showOpenDialog( null )== JFileChooser.APPROVE_OPTION)
            {
               lastInpMatFileName = jfc.getSelectedFile().toString();
               
               MatFileName.setText( lastInpMatFileName);
               
            }
         }
      }
   }
   
   public static void main(String[] args)
   {
      MessageCenter mc = new MessageCenter("Testing MessageCenter");
      TestReceiver tc = new TestReceiver("indexPeaks TestingMessages");
      mc.addReceiver(tc, Commands.INDEX_PEAKS);
      
      indexPeaksPanel ip = new indexPeaksPanel(mc);
      
      JFrame View = new JFrame( "Test Index Peaks Panel" );
      View.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
      View.setBounds(10,10, 350, 350);
      View.setVisible(true);
      
      View.add(ip);
      new UpdateManager(mc, null, 100);
   }
}
