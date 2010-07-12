package Wizard.TOF_SCD;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.LineBorder;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.lattice_calc;
import gov.anl.ipns.Util.SpecialStrings.ErrorString;
import gov.anl.ipns.Util.Sys.*;
import gov.anl.ipns.ViewTools.Panels.StringListChoiceViewer;
import DataSetTools.components.ui.Peaks.*;
import DataSetTools.operator.Generic.GenericOperator;
import DataSetTools.operator.Generic.Special.ViewASCII;
import DataSetTools.operator.Generic.TOF_SCD.*;
import DataSetTools.util.FilenameUtil;
import DataSetTools.util.SharedData;

public class Matrix2_App extends GenericOperator
{

   private static String LATKEY   = "LATKEY";

   private static String DELT1KEY = "DELT1KEY";

   private static String DELT2KEY = "DELT2KEY";

   private static String SEQ1KEY  = "SEQ1KEY";

   private static String SEQ2KEY  = "SEQ2KEY";

   private static String HKL1KEY  = "HKL1KEY";

   private static String HKL2KEY  = "HKL2KEY";

   private static String OTHKEY   = "OTHKEY";
   

   public Matrix2_App()
   {

      super( "Matrix2" );
      setDefaultParameters( );
   }

   
   @Override
   public void setDefaultParameters()
   {

      super.clearParametersVector( );
   }

   
   @Override
   public Object getResult()
   {

      Matrix2( null );
      return null;
   }

   /**
    * Updates all text fields in case a return was not entered.
    * Also, sets the hkl values selected in the bottom info part of GUI.
    * 
    * @author ruth
    *
    */
   static class updateManager
   {

      JTextField[] TextFields;

      JComboBox    jcmb;

      TextboxListener hkl1L, hkl2L;

      public updateManager(JTextField LatTextBox, 
                           JTextField Seq1,
                           JTextField Seq2,
                           JTextField HKL1, 
                           JTextField HKL2,
                           JTextField OthSeq,
                           JTextField Delta1, 
                           JTextField Delta2,
                           JComboBox jcmb
                          )
      {

         TextFields = new JTextField[ 8 ];
         TextFields[0] = LatTextBox;
         TextFields[1] = Seq1;
         TextFields[2] = Seq2;
         TextFields[3] = HKL1;
         TextFields[4] = HKL2;
         TextFields[5] = OthSeq;
         TextFields[6] = Delta1;
         TextFields[7] = Delta2;
         this.jcmb = jcmb;
         hkl1L = hkl2L = null;
      }

      public void fireActionEvents()
      {

         for( int i = 0 ; i < TextFields.length ; i++ )
         {
            ActionListener[] list = TextFields[i].getActionListeners( );
            fireListener( list , TextFields[i] );
         }

         ActionListener[] list = jcmb.getActionListeners( );
         fireListener( list , jcmb );

      }

      //fires the appropriate TextboxListener for each of the text boxes and
      // also the combo box
      private String fireListener(ActionListener[] list, Object source)
      {

         if ( list == null )
            return null;

         String key = null;
         
         for( int j = 0 ; j < list.length && key == null ; j++ )
            if ( list[j] instanceof TextboxListener )
            {
               key = ( ( TextboxListener ) list[j] ).getKey( );
               
               list[j].actionPerformed( new ActionEvent( source ,
                     ActionEvent.ACTION_PERFORMED , key ) );
            }
         
         return key;
      }

      
      public void setHKL1(float h, float k, float l)
      {

         if ( Float.isNaN( h ) )
            
            TextFields[3].setText( "" );
         
         else
            
            TextFields[3].setText( "" + h + "," + k + "," + l );

      }

      public void setHKL2(float h, float k, float l)
      {

         if ( Float.isNaN( h ) )
            
            TextFields[4].setText( "" );
         
         else
            
            TextFields[4].setText( "" + h + "," + k + "," + l );

      }

      //Removed so they will not fire events
      public void RemoveHKLlisteners()
      {

         if ( hkl1L == null )
         {
            ActionListener[] list = TextFields[3].getActionListeners( );
            
            for( int i = 0 ; i < list.length && hkl1L == null ; i++ )
               
               if ( list[i] instanceof TextboxListener )
                  
                  hkl1L = ( TextboxListener ) list[i];
         }

         if ( hkl2L == null )
         {

            ActionListener[] list = TextFields[4].getActionListeners( );
            
            for( int i = 0 ; i < list.length && hkl2L == null ; i++ )
               
               if ( list[i] instanceof TextboxListener )
                  
                  hkl2L = ( TextboxListener ) list[i];

         }

         TextFields[3].removeActionListener( hkl1L );
         TextFields[4].removeActionListener( hkl2L );

      }

      public void AddHKLListeners()
      {

         TextFields[3].addActionListener( hkl1L );
         
         TextFields[4].addActionListener( hkl2L );
         
      }
   }

   /**
    * The Matrix 2 application
    * 
    * @param fileName
    *           The name of the peaks file
    */
   public static void Matrix2(String fileName)
   {

      SharedData sd = new SharedData();
      JTextField LatTextBox = new JTextField( "3,3,3,90,90,90" );
      JTextField Seq1 = new JTextField( "    " );
      JTextField Seq2 = new JTextField( "    " );
      JTextField HKL1 = new JTextField( "    " );
      JTextField HKL2 = new JTextField( "    " );
      JTextField OthSeq = new JTextField( "    " );
      JTextField Delta1 = new JTextField( ".01    " );
      JTextField Delta2 = new JTextField( ".03   " );
      Vector< Peak_new > Peaks = new Vector< Peak_new >( );
      float[] Deltas = new float[ 2 ];
      Deltas[0] = .01f;
      Deltas[1] = .03f;
      char[] c = new char[ 1 ];
      c[0] = 'x';
      // -----------------

      // ---------------
      float[][] UB = new float[ 3 ][ 3 ];
      float[][] UBMat = new float[ 3 ][ 3 ];
      Arrays.fill( UB[0] , Float.NaN );
      Arrays.fill( UB[1] , Float.NaN );
      Arrays.fill( UB[2] , Float.NaN );
      Arrays.fill( UBMat[0] , Float.NaN );
      Arrays.fill( UBMat[1] , Float.NaN );
      Arrays.fill( UBMat[2] , Float.NaN );

      FinishJFrame MainFrame = new FinishJFrame( " Matrix 2" );
      Dimension D = MainFrame.getToolkit( ).getScreenSize( );
      MainFrame.setSize( D.width / 3 , D.height * 11 / 16 );

      Container MainPanel = MainFrame.getContentPane( );
      BoxLayout bl = new BoxLayout( MainPanel , BoxLayout.Y_AXIS );
      MainPanel.setLayout( bl );

      JPanel LatParams = new JPanel( new GridLayout( 4 , 2 ) );
      LatParams.add( new JLabel( "Lattice Parameters" ) );
      LatParams.add( LatTextBox );
      
      JComboBox jcmb = new JComboBox( new String[]
      { "P" , "A" , "B" , "C" , "F" , "I" , "R" } );
      LatParams.add( new JLabel( "   Centering" ) );
      LatParams.add( jcmb );
      jcmb.addActionListener( new MenuListener( c ) );
      
      LatParams.add( new JLabel( "Delta 1" ) );
      LatParams.add( Delta1 );
      
      LatParams.add( new JLabel( "Delta 2" ) );
      LatParams.add( Delta2 );
      
      MainPanel.add( LatParams );
      LatParams.setBorder( new LineBorder( Color.black ) );

      JPanel SeqHKL = new JPanel( new GridLayout( 4 , 3 ) );
      SeqHKL.add( new JLabel( "Point" ) );
      SeqHKL.add( new JLabel( "Seq Num" ) );
      SeqHKL.add( new JLabel( "h,k,l" ) );
      SeqHKL.add( new JLabel( "1" ) );
      SeqHKL.add( Seq1 );
      SeqHKL.add( HKL1 );
      SeqHKL.add( new JLabel( "2" ) );
      SeqHKL.add( Seq2 );
      SeqHKL.add( HKL2 );
      SeqHKL.setBorder( new LineBorder( Color.black ) );

      SeqHKL.add( new JLabel( "Other Seq Nums" ) );
      SeqHKL.add( OthSeq );

      MainPanel.add( SeqHKL );

      MainPanel.add( Box.createVerticalGlue( ) );

      SetPeaks setPeak = new SetPeaks( null , null );
      
      SetPeak1InfoHandler Peak1Choices = new SetPeak1InfoHandler( setPeak ,
                                                                  null );
      
      SetPeak2InfoHandler Peak2Choices = new SetPeak2InfoHandler( setPeak ,
                                                                  Peak1Choices ,
                                                                  null );
      
      updateManager updater = new updateManager( LatTextBox ,
                                                 Seq1 ,
                                                 Seq2 ,
                                                 HKL1 ,
                                                 HKL2 , 
                                                 OthSeq , 
                                                 Delta1 , 
                                                 Delta2 , 
                                                 jcmb );
      
      Info Inf = new Info( Peaks , 
                           UB , 
                           UBMat , 
                           Deltas , 
                           c , 
                           setPeak ,
                           Peak1Choices , 
                           Peak2Choices , 
                           updater );
      

      MainPanel.add( Inf );

      Peak1Choices.setActionListener( new Peak1Listener( Inf ) );

      SetListener( LATKEY , DELT1KEY , DELT2KEY , LatTextBox , Delta1 , Delta2 ,
            UB , UBMat , setPeak , Peak1Choices , Peak2Choices , Deltas , c ,
            Peaks );

      SetListener( SEQ1KEY , SEQ2KEY , HKL1KEY , Seq1 , Seq2 , HKL1 , UB ,
            UBMat , setPeak , Peak1Choices , Peak2Choices , Deltas , c , Peaks );

      SetListener( HKL2KEY , OTHKEY , null , HKL2 , OthSeq , null , UB , UBMat ,
            setPeak , Peak1Choices , Peak2Choices , Deltas , c , Peaks );

      JMenuBar jmbar = SetUpJMenuBar( fileName , 
                                      new MenuListener( setPeak ,
                                                        Peak1Choices , 
                                                        Peak2Choices , 
                                                        UB , 
                                                        Peaks , 
                                                        MainFrame ) );

      MainFrame.setJMenuBar( jmbar );

      if ( fileName != null )
      {
         Vector< Peak_new > Pk = ReadPeaks( fileName );
         if ( Pk == null )
         {
            JOptionPane.showMessageDialog( null ,
                  "Peak file did not read. Restart" );
            System.exit( 0 );
            
         } else
         {
            Peaks.clear( );
            Peaks.addAll( Pk );
            setPeak.setPeaks( Peaks );
         }

      }

      WindowShower.show( MainFrame );
   }

   
   //If a choice is made for Peak1 hkl , it must update Peak2 list and UB list.
   static class Peak1Listener implements ActionListener
   {

      Info inf;

      public Peak1Listener(Info inf)
      {

         this.inf = inf;
      }

      @Override
      public void actionPerformed(ActionEvent arg0)
      {

         inf.update( );

      }

   }

   
   private static Vector< Peak_new > ReadPeaks(String filename)
   {

      try
      {
         return Peak_new_IO.ReadPeaks_new( filename );

      } catch( Exception s )
      {
         s.printStackTrace( );
         return null;
      }
   }

   
   private static JMenuBar SetUpJMenuBar(String       filename,
                                         MenuListener menListener)
   {

      JMenuBar jmenBar = new JMenuBar( );
      JMenu file = new JMenu( "File" );
      JMenu view = new JMenu( "View" );
      JMenu help = new JMenu( "Help" );
      jmenBar.add( file );
      jmenBar.add( view );
      
      if ( filename == null )
      {
         JMenuItem Load = new JMenuItem( "Load Peaks File" );
         Load.addActionListener( menListener );
         file.add( Load );
         
      }
      
      
      JMenuItem Save = new JMenuItem( "Save Orientation Matrix" );
      Save.addActionListener( menListener );
      file.add( Save );
      
      JMenuItem Exit = new JMenuItem( "Exit" );
      Exit.addActionListener( menListener );
      file.add( Exit );

      JMenuItem VPeaks = new JMenuItem( "Peaks File" );
      VPeaks.addActionListener( menListener );
      view.add( VPeaks );

      JMenuItem about = new JMenuItem("about");
      String helpFile = System.getProperty( "Help_Directory" );
      if( helpFile == null)
         return jmenBar;
      

      jmenBar.add( help );
      
      helpFile = helpFile.replace( '\\','/').trim( );
      if( !helpFile.endsWith( "/" ))
         helpFile +='/';
      helpFile +="matrix2.html";
      
      helpFile= FilenameUtil.fixSeparator( helpFile );
      about.addActionListener( new ShowHelpActionListener( helpFile) );
      
      help.add( about );
      return jmenBar;

   }

   
   //Listener to all the JTextFields. Essentially it copies
   // values( after converting) to the associated variables
   static class TextboxListener implements ActionListener
   {

      String              key;

      float[][]           UB;

      float[][]           UBmat;

      SetPeaks            setPeak;

      SetPeak1InfoHandler Peak1Choices;

      SetPeak2InfoHandler Peak2Choices;

      float[]             Deltas;

      char[]              c;

      Vector< Peak_new >  Peaks;

      public TextboxListener(String key, 
                             float[][]           UB, 
                             float[][]           UBmat,
                             SetPeaks            setPeak, 
                             SetPeak1InfoHandler Peak1Choices,
                             SetPeak2InfoHandler Peak2Choices, 
                             float[]             Deltas, 
                             char[]              c,
                             Vector< Peak_new >  Peaks)
      {

         this.key = key;
         this.UB = UB;
         this.UBmat = UBmat;

         this.setPeak = setPeak;

         this.Peak1Choices = Peak1Choices;

         this.Peak2Choices = Peak2Choices;

         this.Deltas = Deltas;
         this.c = c;
         this.Peaks = Peaks;
      }

      public String getKey()
      {

         return key;
      }

      @Override
      public void actionPerformed(ActionEvent arg0)
      {

         if ( !( arg0.getSource( ) instanceof JTextField ) )
            return;
         
         String ttt = ( ( JTextField ) arg0.getSource( ) ).getText( );

         if ( ttt == null || ttt.trim( ).length( ) < 1 )
            return;

         ttt = ttt.trim( );

         if ( key == LATKEY )
         {
            String[] ddd = ttt.split( "," );
            float[] latcon = new float[ ddd.length ];
            try
            {
               for( int i = 0 ; i < ddd.length ; i++ )
               {
                  latcon[i] = Float.parseFloat( ddd[i].trim( ) );

               }
            } catch( Exception ss )
            {
               JOptionPane.showMessageDialog( null ,
                     "Lattice parameters incorrect format" );
               return;
            }
            
            if ( latcon.length < 6 )
            {

               JOptionPane.showMessageDialog( null ,
                     "Not enough Lattice parameters" );
               return;
            }

            double[][] Matmat = lattice_calc.A_matrix( LinearAlgebra
                  .float2double( latcon ) );
            
            Matmat = LinearAlgebra.getInverse( Matmat );
            
            for( int i = 0 ; i < 3 ; i++ )
               for( int j = 0 ; j < 3 ; j++ )
                  UBmat[i][j] = ( float ) Matmat[i][j];
            
            Peak1Choices.setNewData( UBmat );
            
            Peak2Choices.setNewData( UBmat );

         } else if ( key == DELT1KEY )
         {
            try
            {
               float Delt1 = Float.parseFloat( ttt );
               Deltas[0] = Delt1;
               
               Peak1Choices.setErrData( Deltas[0] , Deltas[1] , c[0] );
               Peak2Choices.setErrData( Deltas[0] , Deltas[1] , c[0] );

            } catch( Exception s1 )
            {
               JOptionPane.showMessageDialog( null , "Delta1 Not a Number" );
               return;
            }
            
            
         } else if ( key == DELT2KEY )
         {
            try
            {
               float Delt2 = Float.parseFloat( ttt );
               Deltas[1] = Delt2;
               
               Peak1Choices.setErrData( Deltas[0] , Deltas[1] , c[0] );
               Peak2Choices.setErrData( Deltas[0] , Deltas[1] , c[0] );

            } catch( Exception s2 )
            {
               JOptionPane.showMessageDialog( null , "Delta2 Not a Number" );
               return;
            }
            
            
         } else if ( key == SEQ1KEY )
         {
            try
            {
               int seq1 = Integer.parseInt( ttt );
               if ( seq1 > 0 && seq1 <= Peaks.size( ) )
                  
                  setPeak.setPeakQ( 0 , Peaks.elementAt( seq1 - 1 ) );
               
               else
                  
                  JOptionPane.showMessageDialog( null ,
                        "Sequence1 Num not in range" );
               

            } catch( Exception s3 )
            {
               JOptionPane
                     .showMessageDialog( null , "Seq1 Number not a number" );
               
               return;
               
            }
            
            
         } else if ( key == SEQ2KEY )
         {
            try
            {
               int seq1 = Integer.parseInt( ttt );
               if ( seq1 > 0 && seq1 <= Peaks.size( ) )
                  
                  setPeak.setPeakQ( 1 , Peaks.elementAt( seq1 - 1 ) );
               
               
               else
                  JOptionPane.showMessageDialog( null ,
                                           "Sequence 2 Num not in range" );

            } catch( Exception s4 )
            {
               JOptionPane.showMessageDialog( null ,
                                       "Seq2 Number not a number" );
               
               return;
            }

            
         } else if ( key == HKL1KEY )
         {
            String message = "";
            try
            {
               String[] dd = ttt.split( "," );

               if ( dd.length != 3 )
               {
                  message = "Need 3 values separated by commas";
                  
               } else
               {
                  float[] hkl = new float[ 3 ];
                  message = "hkl values not numeric";
                  
                  for( int i = 0 ; i < 3 ; i++ )
                     hkl[i] = ( float ) Float.parseFloat( dd[i].trim( ) );
                  
                  setPeak.setPeakHKL( 0 , hkl[0] , hkl[1] , hkl[2] );

               }

            } catch( Exception s4 )
            {
               JOptionPane.showMessageDialog( null , message );
               
               return;
            }

            
            
         } else if ( key == HKL2KEY )
         {
            String message = "";
            try
            {
               String[] dd = ttt.split( "," );

               if ( dd.length != 3 )
               {
                  message = "Need 3 values separated by commas";
                  
               } else
               {
                  float[] hkl = new float[ 3 ];
                  
                  message = "hkl2 value not in correct format(3,5,-1)";
                  
                  for( int i = 0 ; i < 3 ; i++ )
                     hkl[i] = ( float ) Float.parseFloat( dd[i].trim( ) );
                  
                  setPeak.setPeakHKL( 1 , hkl[0] , hkl[1] , hkl[2] );

               }

            } catch( Exception s4 )
            {
               JOptionPane.showMessageDialog( null , message );
               return;
            }

            
         } else if ( key == OTHKEY )
         {
            String message = "";
            try
            {
               String[] dd = ttt.split( "," );

               if ( dd == null || dd.length < 1 )
               {

               } else
               {
                
                  for( int i = 0 ; i < dd.length ; i++ )
                  {
                    int seq = Integer.parseInt( dd[i].trim( ) );
                    IPeak peak = Peaks.elementAt( seq-1 );
                    setPeak.setPeakQ( i+2 , peak );
                  }

               }

            } catch( Exception s4 )
            {
               JOptionPane.showMessageDialog( null , message );
               return;
            }

         }

      }

   }

   //Sets 3 listeners at once
   private static void SetListener(String              LATKEY, 
                                   String              DELT1KEY,
                                   String              DELT2KEY, 
                                   JTextField          LatTextBox, 
                                   JTextField          Delta1,
                                   JTextField          Delta2, 
                                   float[][]           UB, 
                                   float[][]           UBmat, 
                                   SetPeaks            setPeak,
                                   SetPeak1InfoHandler Peak1Choices, 
                                   SetPeak2InfoHandler Peak2Choices,
                                   float[]             Deltas, 
                                   char[]              c, 
                                   Vector< Peak_new >  Peaks)
   {

      if ( LatTextBox != null )
         LatTextBox.addActionListener( new TextboxListener( LATKEY , UB ,
               UBmat , setPeak , Peak1Choices , Peak2Choices , Deltas , c ,
               Peaks ) );

      if ( Delta1 != null )
         Delta1.addActionListener( new TextboxListener( DELT1KEY , UB , UBmat ,
               setPeak , Peak1Choices , Peak2Choices , Deltas , c , Peaks ) );

      if ( Delta2 != null )
         Delta2.addActionListener( new TextboxListener( DELT2KEY , UB , UBmat ,
               setPeak , Peak1Choices , Peak2Choices , Deltas , c , Peaks ) );

   }

   static class MenuListener implements ActionListener, IhasWindowClosed
   {

      SetPeaks            setPeak;

      SetPeak1InfoHandler Peak1Choices;

      SetPeak2InfoHandler Peak2Choices;

      float[][]           UB;           // Change values

      Vector< Peak_new >  Peaks;

      JFrame              parent;

      FinishJFrame        jf = null;

      char[]              centering;

      public MenuListener(SetPeaks setPeak, 
                          SetPeak1InfoHandler Peak1Choices,
                          SetPeak2InfoHandler Peak2Choices,
                          float[][] UB,
                          Vector< Peak_new > Peaks, JFrame parent)
      {

         this.setPeak = setPeak;
         this.Peak1Choices = Peak1Choices;
         this.Peak2Choices = Peak2Choices;
         this.UB = UB;
         this.Peaks = Peaks;
         this.parent = parent;
         centering = null;
      }

      public MenuListener(char[] centering)
      {

         this.setPeak = null;
         this.Peak1Choices = null;
         this.Peak2Choices = null;
         this.UB = null;
         this.Peaks = null;
         this.parent = null;
         this.centering = centering;
      }

      @Override
      public void actionPerformed(ActionEvent arg0)
      {

         String command = arg0.getActionCommand( );

         if ( command.equals( "Load Peaks File" ) )
         {
            JFileChooser jfc = new JFileChooser( System
                  .getProperty( "Data_Directory" ) );
            
            if ( jfc.showOpenDialog( null ) == JFileChooser.APPROVE_OPTION )
            {
               String filename = jfc.getSelectedFile( ).getPath( );
               System.out.println( "filename =" + filename );
               Vector< Peak_new > pk = ReadPeaks( filename );
               Peaks.clear( );
               Peaks.addAll( pk );
               setPeak.setPeaks( Peaks );
            }

            
         } else if ( command.equals( "Save Orientation Matrix" ) )
         {
            JFileChooser jfc = new JFileChooser( System
                  .getProperty( "Data_Directory" ) );
            
            if ( jfc.showOpenDialog( null ) == JFileChooser.APPROVE_OPTION )
            {
               String filename = jfc.getSelectedFile( ).getPath( );
               Object X = DataSetTools.operator.Generic.TOF_SCD.Util
                                                .WriteMatrix( filename , UB );
               
               if ( X instanceof ErrorString )
                  JOptionPane.showMessageDialog( null ,
                                        "Could Not write Matrix file:" + X );

            }

         } else if ( command.equals( "Exit" ) )
         {
            System.exit( 0 );

         } else if ( command.equals( "Peaks File" ) )
         {

            String filename = System.getProperty( "user.home" );
            filename = gov.anl.ipns.Util.File.FileIO.appendPath( filename ,
                  "ISAW/tmp" ) + "peaks.peaks";
            
            try
            {
               Peak_new_IO.WritePeaks_new( filename , Peaks , false );
               ( new ViewASCII( filename ) ).getResult( );

            } catch( Exception s )
            {
              SharedData.addmsg( "Could not write out Peaks:"+s );
            }

            
         } else if ( arg0.getSource( ) instanceof JComboBox )
         {
            int k = ( ( JComboBox ) arg0.getSource( ) ).getSelectedIndex( );
            
            if ( k < 0 || k > 6 )
               return;
            
            centering[0] = "PABCFIR".charAt( k );
            return;
         }

      }

      @Override
      public void WindowClose(String ID)
      {

         if ( jf == null )
            return;
         jf.removeAll( );
         jf.dispose( );
         jf = null;

      }

   }

   /**
    * The information part of the display
    * @author ruth
    *
    */
   static class Info extends JPanel implements ActionListener
   {

      Vector< Peak_new >     Peaks;

      Vector< IPeak >        PeaksI;

      float[][]              UB;

      float[][]              UBMat;

      float[]                Deltas;

      char[]                 c;

      SetPeaks               setPeak;

      SetPeak1InfoHandler    Peak1Choices;

      SetPeak2InfoHandler    Peak2Choices;

      updateManager          updater;

      JTabbedPane            tabs;

      JPanel                 Peak1hkls;

      JPanel                 Peak2hkls;

      StringListChoiceViewer OrientMatrices;

      JButton                Update;

      Vector< float[][] >    UBs = null;

      Info(Vector< Peak_new >  Peaks, 
           float[][]           UB, 
           float[][]           UBMat,
           float[]             Deltas, 
           char[]              c, 
           SetPeaks            setPeak,
           SetPeak1InfoHandler Peak1Choices, 
           SetPeak2InfoHandler Peak2Choices,
           updateManager       updater)
      {

         super( new BorderLayout( ) );
         this.Peaks = Peaks;
         cvrtPeakNew2IPeak( );
         this.UB = UB;
         this.UBMat = UBMat;
         this.Deltas = Deltas;
         this.c = c;
         this.setPeak = setPeak;
         this.Peak1Choices = Peak1Choices;
         this.Peak2Choices = Peak2Choices;
         this.updater = updater;
         
         tabs = new JTabbedPane( );
         Update = new JButton( "Update" );
         Update.addActionListener( this );
         
         Peak1hkls = new JPanel( );
         Peak2hkls = new JPanel( );
         OrientMatrices = new StringListChoiceViewer( new String[]
         { "No Matrices yet" } , 20 , 40 , true );

         Peak1hkls.setLayout( new GridLayout( 1 , 1 ) );
         Peak2hkls.setLayout( new GridLayout( 1 , 1 ) );

         Peak1Choices.show( null , null , Peak1hkls );
         Peak2Choices.show( null , null , Peak2hkls );
         
         SetUpOrientationMatrices( );
         
         OrientMatrices.repaint( );
         
         OrientMatrices.addActionListener( this );

         tabs.add( "Peak 1 HKL's" , Peak1hkls );
         tabs.add( "Peak 2 HKL's" , Peak2hkls );
         tabs.add( "UB mats" , OrientMatrices );
         this.add( tabs , BorderLayout.CENTER );
         this.add( Update , BorderLayout.SOUTH );
         
         setPeak.addSetPeakListeners( this );

      }

      private void cvrtPeakNew2IPeak()
      {

         PeaksI = new Vector< IPeak >( Peaks.size( ) );
         
         for( int i = 0 ; i < Peaks.size( ) ; i++ )
            PeaksI.add( ( IPeak ) Peaks.elementAt( i ) );

      }

      private boolean TwoPeaksPicked( SetPeaks setPeak)
      {
         if( setPeak == null )
            return false;
         if( setPeak.getSetPeakQ( 0 ) == null)
            return false;
         
         if( setPeak.getSetPeakQ(1) == null)
            return false;
         
         return true;
      }
      
      private void SetUpOrientationMatrices()
      {
         if( !TwoPeaksPicked( setPeak))
            return;
         
         Vector< String > Choices = new Vector< String >( );
         UBs = new Vector< float[][] >( );
         
         int[][] hkl1 = new int[ 1 ][ 3 ];
         
         if ( setPeak.getSetPeak_hkl( 0 ) != null )
         {
            float[] Hkl1 = setPeak.getSetPeak_hkl( 0 );
            hkl1[0][0] = ( int ) Hkl1[0];
            hkl1[0][1] = ( int ) Hkl1[1];
            hkl1[0][2] = ( int ) Hkl1[2];
            
         } else
            
            hkl1 = subs.FindPossibleHKLs( UBMat , setPeak.getSetPeakQ( 0 ) ,
                  Deltas[0] , Deltas[1] , c[0] );
         
         if ( hkl1 != null )
            for( int i = 0 ; i < hkl1.length ; i++ )
            {
               float[] HKL1 = new float[ 3 ];
               HKL1[0] = hkl1[i][0];
               HKL1[1] = hkl1[i][1];
               HKL1[2] = hkl1[i][2];
               int[][] hkl2 = new int[ 1 ][ 3 ];
               
               if ( setPeak.getSetPeak_hkl( 1 ) != null )
               {
                  float[] Hkl2 = setPeak.getSetPeak_hkl( 1 );
                  hkl2[0][0] = ( int ) Hkl2[0];
                  hkl2[0][1] = ( int ) Hkl2[1];
                  hkl2[0][2] = ( int ) Hkl2[2];
                  
               } else
                  // hkl2 =subs.FindPossibleHKLs( UBMat , setPeak.getSetPeakQ( 1
                  // ) ,
                  // Deltas[0] ,Deltas[1] , c[0] );

                  hkl2 = subs.FindPossibleHKLs( UBMat ,
                                                setPeak.getSetPeakQ( 0 ) , 
                                                setPeak.getSetPeakQ( 1 ) ,
                                                hkl1[i] , 
                                                Deltas[0] , 
                                                Deltas[1] , 
                                                c[0] );
               
               if ( hkl2 != null )
                  for( int j = 0 ; j < hkl2.length ; j++ )
                  {
                     float[] HKL2 = new float[ 3 ];
                     HKL2[0] = hkl2[j][0];
                     HKL2[1] = hkl2[j][1];
                     HKL2[2] = hkl2[j][2];

                     float[][] UB = subs.getOrientationMatrix( 
                                                 setPeak.getSetPeakQ( 0 )   , 
                                                 HKL1 , 
                                                 setPeak.getSetPeakQ( 1 ) ,
                                                 HKL2 , 
                                                 UBMat );
                     
                     if ( UB != null )
                     {
                        UBs.add( UB );
                        String S = OrientMatrixControl.ShowMatString( UB ,
                                                                      PeaksI , 
                                                                      null ,
                                                                      setPeak );
                        Choices.add( S );
                     }
                  }

            }
         
         
         String[] StringList = new String[]{ "No Matrices yet" };
         
         if ( Choices.size( ) > 0 )
            StringList = Choices.toArray( new String[ 0 ] );

         OrientMatrices.setNewStringList( StringList );

      }

      
      public boolean isOptimizedDrawingEnabled()
      {

         return false;
      }

      
      public void update()
      {

         updater.fireActionEvents( );
         cvrtPeakNew2IPeak( );

         Peak1Choices.show( null , null , Peak1hkls );
         Peak2Choices.show( null , null , Peak2hkls );
         SetUpOrientationMatrices( );
         OrientMatrices.repaint( );
      }

      @Override
      public void actionPerformed(ActionEvent arg0)
      {

         if ( arg0.getSource( ) == OrientMatrices )
         {
            int k = OrientMatrices.getSelectedChoice( );
            
            if ( k < 0 )
               k = OrientMatrices.getLastViewedChoice( );
            
            if ( k < 0 || k >= UBs.size( ) )
               return;

            float[][] UBB = UBs.elementAt( k );
            
            LinearAlgebra.copy( UBB , UB );

         } else if ( arg0.getActionCommand( ) == SetPeaks.SET_PEAK_INFO_CHANGED )
         {
            float[] hkl1 = setPeak.getSetPeak_hkl( 0 );
            float[] hkl2 = setPeak.getSetPeak_hkl( 1 );
            if ( hkl1 != null && hkl1.length != 3 )
               hkl1 = null;
            
            if ( hkl2 != null && hkl2.length != 3 )
               hkl2 = null;
            
            updater.RemoveHKLlisteners( );
            
            if ( hkl1 == null )
               updater.setHKL1( Float.NaN , Float.NaN , Float.NaN );
            
            else
               updater.setHKL1( hkl1[0] , hkl1[1] , hkl1[2] );
            

            if ( hkl2 == null )
               updater.setHKL2( Float.NaN , Float.NaN , Float.NaN );
            
            else
               updater.setHKL2( hkl2[0] , hkl2[1] , hkl2[2] );

            updater.AddHKLListeners( );

            SetUpOrientationMatrices( );

         } else if ( arg0.getSource( ) instanceof JButton
               && arg0.getActionCommand( ).equals( "Update" ) )
         {
            update( );
         }

      }

   }

   public static void main(String[] args)
   {

      Matrix2_App.Matrix2( null );

   }

   /**
    * @param args
    */

}
