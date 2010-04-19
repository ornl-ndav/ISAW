/* 
 * File: indexPeaksPanel.java
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

package EventTools.ShowEventsApp.Controls.Peaks;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.Parameters.FilteredPG_TextField;
import gov.anl.ipns.Parameters.FloatFilter;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Vector;

import MessageTools.*;
import EventTools.ShowEventsApp.Command.*;

/**
 * Panel that displays information about the index peaks
 * and orientation matrix which can be changed
 * to what the user specifically wants.  Also has the ability to load
 * an orientation matrix, index peaks, show orientation matrix,
 * and write an orientation matrix.
 */
public class indexPeaksPanel extends    JPanel  implements IReceiveMessage
                                
{
   
   public static final long     serialVersionUID  = 1L;

   private MessageCenter        messageCenter;

   private JTextField           aTxt;

   private JTextField           bTxt;

   private JTextField           cTxt;

   private JTextField           alphaTxt;

   private JTextField           betaTxt;

   private JTextField           gammaTxt;

   private JTextField           toleranceTxt;

   private JTextField           fixedPeakTxt;

   private JTextField           requiredFractionTxt;

   private JButton              applyBtn;

   private JButton              MatFileBtn;

   private JTextField           MatFileName;

   private FilteredPG_TextField Dmin;

   private FilteredPG_TextField Dmax;

   //private JButton              ViewMatBtn;

   //private JButton              WriteMatBtn;

   private JTabbedPane          middlePanel; 
   
   private JTextArea            OrientMatDispl;

   //Tab pane indices for orientation matrix "calculation"

   private static int           FROM_FILE         = 2;  

   private static int           AUTO_WPARAMS      = 0;

   private static int           AUTO_ROSS         = 1;
   
   private static int           FROM_UB            = 3;
   
   private float[][]            UBT = null;
   private static String   NoOrientationText="<html><body> There is no "+
                  "Orientation matrix </body></html>";
   
   /**
    * Builds the indexPanel and sets the message center.
    * 
    * @param messageCenter
    */
   public indexPeaksPanel(MessageCenter messageCenter)
   {
      this.messageCenter = messageCenter;
      
      this.setBorder(new TitledBorder("Index Peaks"));
      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
       
      this.add(buildPanel());
      
      messageCenter.addReceiver( this, Commands.SET_ORIENTATION_MATRIX);
      //this.add( buildButtonsPanel());
   }
   
   /**
    * Builds three panels, the from file panel, calcmatpanel, and
    * the buttons panel and adds them to a new panel to be displayed.
    *  
    * @return panel with the three panels.
    */
   private JPanel buildPanel()
   {
      JPanel panel = new JPanel();
      BoxLayout blayout = new BoxLayout( panel, BoxLayout.Y_AXIS );
      panel.setLayout(  blayout );
      middlePanel = new JTabbedPane();
      middlePanel.addTab( "AutoIndex(with Lattice Parameters)",
                           buildCalcMatPanel());
      middlePanel.addTab( "AutoIndex", buildCalcMat2Panel());
      middlePanel.addTab( "Read UB From File", buildFromFilePanel() );
      middlePanel.addTab( "Index Using Current UB" , buildCalcMat3Panel() );
      panel.add( middlePanel );
      panel.add( buildTolerancePanel());
      this.add(panel );
      panel.add( buildButtonsPanel(), BorderLayout.SOUTH );
      return panel;
   }
   
   /**
    * Builds the panel with the matrix filename and button to load
    * a file.
    * 
    * @return panel
    */
   private JPanel buildFromFilePanel()
   {
      JPanel MainPanel = new JPanel();
      JPanel panel = new JPanel();
      panel.setBorder( new TitledBorder( new LineBorder(Color.black),
                 "Orientation Matrix in File") );
      panel.setLayout( new GridLayout(1,2));
      MatFileBtn = new JButton("Matrix filename");
      MatFileBtn.addActionListener( new buttonListener());
      panel.add( MatFileBtn );
      MatFileName = new JTextField(10);
      MatFileName.setText( "" );
      panel.add(MatFileName );
      MainPanel.setLayout(new BorderLayout()) ;
      MainPanel.add( panel , BorderLayout.NORTH);
      return MainPanel;
      
   }
   
   /**
    * Builds the panel to display information for auto indexing with Crystal
    * parameters. These parameters and tolerances can be changed by the user.
    * 
    * @return panel
    */
   private JPanel buildCalcMatPanel()
   {
      JPanel panel = buildPanel1();
      panel.setBorder(  new TitledBorder( new LineBorder(Color.black),
               "Find Matrix Using Lattice Parameters") );
      return panel;
   }
   
   /**
    * Builds the panel to display information for auto indexing without Crystal
    * parameters. Max and Min d-spacing can be specified.
    * 
    * @return panel
    */
   private JPanel buildCalcMat2Panel()
   {
      JPanel MainPanel = new JPanel();
      JPanel panel = new JPanel();
      panel.setBorder( new TitledBorder( new LineBorder(Color.black),
                 "AutoIndex, Bounds on Lattice Parameters") );
      
      panel.setLayout( new GridLayout(2,2));
      JLabel  DMinLabel= new JLabel("Min Unit Cell Edge");
      Dmin = new FilteredPG_TextField(new FloatFilter());
      Dmin.setText( "2.0" );
      panel.add( DMinLabel );
      panel.add(  Dmin );
      JLabel  DMaxLabel= new JLabel("Max Unit Cell Edge");
      Dmax = new FilteredPG_TextField(new FloatFilter());
      Dmax.setText( "12.0" );
      panel.add( DMaxLabel );
      panel.add( Dmax );
      MainPanel.setLayout(new BorderLayout()) ;
      MainPanel.add( panel , BorderLayout.NORTH);
      
      return MainPanel;
      
   }
   
   private JTextArea buildCalcMat3Panel()
   {
      OrientMatDispl = new JTextArea(20, 8);
      OrientMatDispl.setText( "No Matrix available" );
      return OrientMatDispl;
   }
   /**
    * Builds the panel with the index peaks,
    * show matrix and write matrix buttons.
    * 
    * @return panel
    */
   private JPanel buildButtonsPanel()
   {  
      JPanel panel = new JPanel();
      
      panel.setLayout( new GridLayout(1,3) );
      applyBtn = new JButton("Index Peaks");
      applyBtn.addActionListener(new buttonListener());
      applyBtn.setToolTipText( "<HTML><BODY>Will get Matrix and"+
               "<BR> apply it to the Peaks" );
      panel.add(applyBtn);
      
     /* 
      ViewMatBtn = new JButton("Show Matrix");
      
      WriteMatBtn = new JButton("Write Matrix");
      WriteMatBtn.addActionListener( new buttonListener());
      ViewMatBtn.addActionListener( new buttonListener());
     
      panel.add( WriteMatBtn );
      panel.add( ViewMatBtn );
      */
      return panel;
   }

   
   /**
    * Builds a panel with the information.
    * 
    * @return panel
    */
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
     
      
      JLabel fixedPeakLbl = new JLabel("Fixed Peak Index:");
      String defaultFindPeaks = "1";
      fixedPeakTxt = new JTextField(defaultFindPeaks);
      fixedPeakTxt.setHorizontalAlignment(JTextField.RIGHT);
  
      JLabel requiredFractionLbl = new JLabel("Pass 1 Required Fraction:");
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
      panel.add(requiredFractionLbl);
      panel.add(requiredFractionTxt);
      panel.add(fixedPeakLbl);
      panel.add(fixedPeakTxt);
      
      return panel;
   }
   
   private JPanel buildTolerancePanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(  new GridLayout( 1,2) );
      
      JLabel toleranceLbl = new JLabel("hkl Tolerance:");
      String defaultTolerance = ".12";
      toleranceTxt = new JTextField(defaultTolerance);
      toleranceTxt.setHorizontalAlignment(JTextField.RIGHT);

      panel.add(toleranceLbl);
      panel.add(toleranceTxt);
    
      return panel;
   }
   
   /**
    * Sends a message to the message center
    * 
    * @param command
    * @param value
    */
   private void sendMessage(String command, Object value)
   {
      Message message = new Message(command, value, true);
      
      messageCenter.send(message);
   }

   /**
    * Checks that all the information has been
    * inputed is in the correct form.
    * 
    * @return false if the information is missing
    *       pieces or is in the incorrect format.
    *       true otherwise.
    */
   private boolean valid()
   {
      try
      {
         Float.parseFloat(aTxt.getText());
      }
      catch (NumberFormatException e)
      {
         String error = "a must be of type Float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", 
                                       JOptionPane.ERROR_MESSAGE);
         return false;
      } 
      
      try
      {
         Float.parseFloat(bTxt.getText());
      }
      catch (NumberFormatException e)
      {
         String error = "b must be of type Float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", 
                                       JOptionPane.ERROR_MESSAGE);
         return false;
      } 
      
      try
      {
         Float.parseFloat(cTxt.getText());
      }
      catch (NumberFormatException e)
      {
         String error = "c must be of type Float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", 
                                       JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      try
      {
         Float.parseFloat(alphaTxt.getText());
      }
      catch (NumberFormatException e)
      {
         String error = "Alpha must be of type Float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", 
                                       JOptionPane.ERROR_MESSAGE);
         return false;
      } 
      
      try
      {
         Float.parseFloat(betaTxt.getText());
      }
      catch (NumberFormatException e)
      {
         String error = "Beta must be of type Float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", 
                                       JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      try
      {
         Float.parseFloat(gammaTxt.getText());
      }
      catch (NumberFormatException e)
      {
         String error = "Gamma must be of type Float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", 
                                       JOptionPane.ERROR_MESSAGE);
         return false;
      } 
      
      try
      {
         Float.parseFloat(toleranceTxt.getText());
      }
      catch (NumberFormatException e)
      {
         String error = "Tolerance must be of type Float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", 
                                       JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      try
      {
         Float.parseFloat(requiredFractionTxt.getText());
      }
      catch (NumberFormatException e)
      {
         String error = "Required Fraction must be of type Float!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", 
                                       JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      try
      {
         Integer.parseInt(fixedPeakTxt.getText()); 
         
      }
      catch (NumberFormatException nfe)
      {
         String error = "Fixed Peaks must be of type Integer!";
         JOptionPane.showMessageDialog(null, error, "Invalid Input", 
                                       JOptionPane.ERROR_MESSAGE);
         return false;
      }
      

      return true;
   }
   

   @Override
   public boolean receive(Message message)
   {

      if( message.getValue() instanceof Vector)
         
         UBT = (float[][])((Vector)message.getValue()).firstElement();
      
      else
         
         UBT = (float[][])message.getValue( );
      
      if( UBT == null)
         
         OrientMatDispl.setText( "null UB" );
      
      else
      {
         String S = DataSetTools.components.ui.Peaks.subs.ShowOrientationInfo(
               null , LinearAlgebra.getTranspose( UBT) , null , null , false );
         OrientMatDispl.setText( S );
      }
      
      return false;
   }


   /**
    * Button listener for the buttons that sends message of 
    * READ_ORIENTATION_MATRIX or INDEX_PEAKS of type IndexPeaksCmd if Index
    * Peaks button is pressed.  WRITE_ORIENTATION_MATRIX if the write
    * matrix button is press. GET_ORIENTATION_MATRIX if show matrix is 
    * pressed. 
    */
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
      
      
      public void actionPerformed( ActionEvent e )
      {

         String cmd = e.getActionCommand();
         if( cmd.startsWith( "Index" ) )
         {
            if( middlePanel == null )
               return;
            if( middlePanel.getSelectedIndex() == FROM_FILE )
            {
               if( getText( MatFileName ).length() > 0 )

               {
                  java.util.Vector Messge = new java.util.Vector(2);
                  Messge.add( getText(MatFileName));
                  try
                  {
                  Messge.add( Float.parseFloat(
                           toleranceTxt.getText().trim())  );
                  }catch(Exception s3)
                  {
                     Messge.add(.12f);
                  }
                  sendMessage( Commands.READ_ORIENTATION_MATRIX ,
                           Messge );
                  return;
               }
            }
            else if( middlePanel.getSelectedIndex() == AUTO_WPARAMS )
            {
               if( valid() )
               {  
                  IndexPeaksCmd indexCmd = new IndexPeaksCmd( Float
                           .parseFloat( aTxt.getText() ) , Float
                           .parseFloat( bTxt.getText() ) , Float
                           .parseFloat( cTxt.getText() ) , Float
                           .parseFloat( alphaTxt.getText() ) , Float
                           .parseFloat( betaTxt.getText() ) , Float
                           .parseFloat( gammaTxt.getText() ) , Float
                           .parseFloat( toleranceTxt.getText() ) , Integer
                           .parseInt( fixedPeakTxt.getText() ) , Float
                           .parseFloat( requiredFractionTxt.getText() ) );

                  sendMessage( Commands.INDEX_PEAKS , indexCmd );
               }
            }
            else if( middlePanel.getSelectedIndex() == AUTO_ROSS )
            {
               ProcessAutoRoss( Dmin.getText(), Dmax.getText(), toleranceTxt.getText());
               
            }else if( middlePanel.getSelectedIndex() == FROM_UB)
            {
               if( UBT == null)
                  JOptionPane.showMessageDialog( null ,
                        "There is NO Orientation Matrix" );
               else
               {
                  messageCenter.send( new Message( 
                        Commands.INDEX_PEAKS_WITH_ORIENTATION_MATRIX, 
                        new UBwTolCmd(UBT, .12f),
                        false,
                        true ) );
               }
            }
         }
         else if( cmd.startsWith( "Write" ) )
         {
            JFileChooser jfc = new JFileChooser( Directory( lastWriteFileName ) );
            if( jfc.showSaveDialog( null ) == JFileChooser.APPROVE_OPTION )
            {
               lastWriteFileName = jfc.getSelectedFile().toString();
               messageCenter.send( new Message(
                        Commands.WRITE_ORIENTATION_MATRIX , lastWriteFileName ,
                        false ) );
            }

            JOptionPane.showMessageDialog( null , 
                  "This option will disappear. Use Menu bar" );
         }
         else if( cmd.startsWith( "Show" ) )
         {
            sendMessage( Commands.SHOW_ORIENTATION_MATRIX , "" );

            JOptionPane.showMessageDialog( null , 
                  "This option will disappear. Use Menu bar" );
         }
         else if( cmd.startsWith( "Matrix" ) )
         {
            JFileChooser jfc = new JFileChooser( Directory( lastInpMatFileName ) );
            if( jfc.showOpenDialog( null ) == JFileChooser.APPROVE_OPTION )
            {
               lastInpMatFileName = jfc.getSelectedFile().toString();

               MatFileName.setText( lastInpMatFileName );
            }
         }
      }
   }
   
   // Sends messages for autoIndexing
   private void  ProcessAutoRoss( String Dmin , String Dmax, String Fraction)
   {
      float dmin = -1;
      float dmax = -1;
      float fract = -1;
      try
      {
         dmin = Float.parseFloat( Dmin );
         dmax = Float.parseFloat( Dmax );
         fract = Float.parseFloat( Fraction );
      }catch( Exception ss)
      {
         messageCenter.send(  new Message( Commands.DISPLAY_ERROR,
                  "Min,Max d-spacing or Max distance from integer "+
                  "are not set for autoindexing", false) );
         return;
      }
      float[] data = new float[3];
      data[0] = dmin;
      data[1] = dmax;
      data[2] = fract;
      
      messageCenter.send(  new Message( Commands.INDEX_PEAKS_ROSS,
               data, false) );
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
