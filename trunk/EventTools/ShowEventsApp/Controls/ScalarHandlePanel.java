/* 
 * File: ScalarHandlePanel.java
 *
 * Copyright (C) 2010, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Spallation Neutron Source Division
 * of Oak Ridge National Laboratory, Oak Ridge, TN, USA.
 *
 *  Last Modified:
 * 
 *  $Author$:
 *  $Date$:            
 *  $Rev$:
 */
package EventTools.ShowEventsApp.Controls;

import gov.anl.ipns.MathTools.LinearAlgebra;
import gov.anl.ipns.MathTools.lattice_calc;
import gov.anl.ipns.Util.File.TextSeparators;
import gov.anl.ipns.Util.Sys.WindowShower;
import gov.anl.ipns.ViewTools.Panels.StringListChoiceViewer;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import EventTools.ShowEventsApp.Command.Commands;
import MessageTools.*;
import Operators.TOF_SCD.ReducedCellInfo;
import Operators.TOF_SCD.ReducedFormList;

/**
 * This class becomes a panel in the application IsawEV. It handles
 * details needed by the new Scalar method.
 * 
 * @author ruth
 *
 */
public class ScalarHandlePanel implements IReceiveMessage
{

   private static String      SHOW_CENTERINGS   = "Update List";

   private static String      APPLY_CENTERINGS  = "Apply Displayed Cell";

   private static String      CLEAR_ALL         = "Clear ALL Boxes";

   private static String      SET_ALL           = "Set ALL Boxes";

   MessageCenter             OrientMatMessageCenter;

   float[][]                 UB;

   JPanel                    panel;

   JCheckBox[]               SymmetryChoices   = new JCheckBox[ 7 ];

   String[]                  ChoicesString     =
                                               { ReducedCellInfo.CUBIC ,
                                                 ReducedCellInfo.TETRAGONAL , 
                                                 ReducedCellInfo.ORTHORHOMBIC ,
                                                 ReducedCellInfo.RHOMBOHEDRAL , 
                                                 ReducedCellInfo.HEXAGONAL ,
                                                 ReducedCellInfo.MONOCLINIC , 
                                                 ReducedCellInfo.TRICLINIC 
                                                 };

   JCheckBox[]               Centerings        = new JCheckBox[ 8 ];

   String[]                  CentChoiceStrings =
                                               { "P" ,  
                                                 "F" , 
                                                 "I" , 
                                                 "C" ,
                                                 "a<>b" , 
                                                 "a=b" , 
                                                 "b<>c" , 
                                                 "b=c"      
                                                 };

   JTextField                Delta;

   JComboBox                 SortOn;

   String[]                  SortChoices       =
                                               { "Symmetry" , 
                                                 "Form Number" ,
                                                 "Error" ,                            
                                               };

   String[]                  Choices;

   StringListChoiceViewer    viewer;

   Vector< ReducedCellPlus > ScalarOpts;

   static String             Symm              =       ReducedCellInfo.CUBIC+ ";"                                                     
                                                     + ReducedCellInfo.TETRAGONAL + ";"                                                    
                                                     + ReducedCellInfo.ORTHORHOMBIC+ ";"
                                                     + ReducedCellInfo.RHOMBOHEDRAL+ ";"
                                                     + ReducedCellInfo.HEXAGONAL+ ";"
                                                     + ReducedCellInfo.MONOCLINIC+ ";"
                                                     + ReducedCellInfo.TRICLINIC+ ";";

   static String             Cent              = ReducedCellInfo.P_CENTERED+ ";"
                                                     + ReducedCellInfo.I_CENTERED+ ";"
                                                     + ReducedCellInfo.F_CENTERED+ ";"
                                                     + ReducedCellInfo.C_CENTERED+ ";"
                                                     + ReducedCellInfo.R_CENTERED+ ";";

   /**
    * Constructor where the orientation matrix comes from a message center
    * 
    * @param OrientMatMessageCenter The message center that sends and gets orientation
    *                            matrices using commands in 
    *                            EventTools.ShowEventsApp.Command.Commands
    */
   public ScalarHandlePanel(MessageCenter OrientMatMessageCenter)
   {

      this.OrientMatMessageCenter = OrientMatMessageCenter;
      UB = null;
      ScalarOpts = null;
      Choices = new String[ 1 ];
      Choices[0] = " No Cell Types Available ";
      viewer = new StringListChoiceViewer( Choices , 4 , 25 , false );
      
      BuildJPanel( );
      
      OrientMatMessageCenter.addReceiver( this ,
            Commands.SET_ORIENTATION_MATRIX );

   }
   
   
/**
 * Constructor where the orientation matrix is supplied in the constructor
 *  
 * @param UB  The untransposed orientation matrix
 */
   public ScalarHandlePanel(float[][] UB)
   {

      this.UB = UB;
      OrientMatMessageCenter = null;
      Choices = new String[ 1 ];
      Choices[0] = " No Cell Types Available ";
      viewer = new StringListChoiceViewer( Choices , 4 , 25 , false );
      BuildJPanel( );
   }

   /**
    * Returns the JPanel with GUI elements to direct the process
    * @return the JPanel with GUI elements to direct the process
    */
   public JPanel getPanel()
   {

      return panel;
   }
   
   

   private void BuildJPanel()
   {

      panel = new JPanel( );
      BoxLayout layout = new BoxLayout( panel , BoxLayout.Y_AXIS );
      panel.setLayout( layout );

      panel.add( BuildTopPanel( ) );
      panel.add( BuildClearSetPanel( ) );
      panel.add( BuildSortUpdatePanel( ) );
      viewer.setBorder( new TitledBorder( new LineBorder( Color.black , 2 ) ,
            "List of Transformed Cells" ) );
      panel.add( viewer );
      panel.add( BuildBottomPanel( ) );

   }

   private JPanel BuildTopPanel()
   {

      JPanel panel = new JPanel( new GridLayout( 1 , 2 ) );
      panel.add( BuildLeftTopPanel( ) );
      panel.add( BuildRightTopPanel( ) );

      return panel;
   }

   private JPanel BuildLeftTopPanel()
   {

      JPanel panel = new JPanel( new BorderLayout( ) );
      panel.add( new JLabel( "Restrict Shown to" ) , BorderLayout.NORTH );
      JPanel choices = new JPanel( new GridLayout( 7 , 1 ) );
      for( int i = 0 ; i < 7 ; i++ )
      {
         SymmetryChoices[i] = new JCheckBox( ChoicesString[i] , true );
         choices.add( SymmetryChoices[i] );
      }
      panel.add( choices , BorderLayout.CENTER );
      return panel;
   }

   private JPanel BuildRightTopPanel()
   {

      JPanel panel = new JPanel( );
      BoxLayout bl = new BoxLayout( panel , BoxLayout.Y_AXIS );
      panel.setLayout( bl );

      JPanel deltaPanel = new JPanel( new GridLayout( 1 , 2 ) );
      deltaPanel.add( new JLabel( "Max error" ) );
      Delta = new JTextField( ".2" );
      deltaPanel.add( Delta );
      panel.add( deltaPanel );

      JPanel CenteringPanel = new JPanel( new GridLayout( 2 , 2 ) );
      CenteringPanel.setBorder( new TitledBorder( new LineBorder( Color.black ,
            1 ) , "Centering" ) );
      for( int i = 0 ; i < 4 ; i++ )
      {
         Centerings[i] = new JCheckBox( CentChoiceStrings[i] , true );
         CenteringPanel.add( Centerings[i] );
      }

      panel.add( CenteringPanel );

      JPanel SidesPanel = new JPanel( new GridLayout( 2 , 2 ) );
      SidesPanel.setBorder( new TitledBorder(
            new LineBorder( Color.black , 1 ) , "Sides" ) );
      for( int i = 4 ; i < 8 ; i++ )
      {
         Centerings[i] = new JCheckBox( CentChoiceStrings[i] , true );
         SidesPanel.add( Centerings[i] );
      }

      panel.add( SidesPanel );

      return panel;
   }

   private JPanel BuildClearSetPanel()
   {

      JPanel Res = new JPanel( );
      BoxLayout bl = new BoxLayout( Res , BoxLayout.X_AXIS );
      Res.setLayout( bl );

      JButton Clear = new JButton( CLEAR_ALL );
      Res.add( Box.createHorizontalGlue( ) );
      Res.add( Clear );

      JButton Set = new JButton( SET_ALL );
      Res.add( Box.createHorizontalGlue( ) );
      Res.add( Set );

      Res.add( Box.createHorizontalGlue( ) );

      Clear.addActionListener( new ThisActionListener( 0 ) );
      Set.addActionListener( new ThisActionListener( 0 ) );
      return Res;

   }

   private JPanel BuildSortUpdatePanel()
   {

      JPanel Res = new JPanel( );
      BoxLayout bl = new BoxLayout( Res , BoxLayout.X_AXIS );
      Res.setLayout( bl );
      SortOn = new JComboBox( SortChoices );

      Res.add( new JLabel( " Sort On  " ) , BorderLayout.WEST );
      Res.add( SortOn , BorderLayout.CENTER );
      Res.add( Box.createHorizontalGlue( ) );

      JButton Show = new JButton( SHOW_CENTERINGS );
      Show.addActionListener( new ThisActionListener( 0 ) );
      Res.add( Show );
      Res.setBorder( new LineBorder( Color.black ) );
      return Res;

   }

   private JPanel BuildBottomPanel()
   {

      JButton Apply = new JButton( APPLY_CENTERINGS );
      Apply.addActionListener( new ThisActionListener( 0 ) );

      JPanel panel = new JPanel( );
      BoxLayout layout = new BoxLayout( panel , BoxLayout.X_AXIS );
      panel.setLayout( layout );
      panel.add( Box.createHorizontalGlue( ) );

      panel.add( Apply );
      panel.add( Box.createHorizontalGlue( ) );
      return panel;
   }

   /**
    * The angle closest to 90 degrees stays the same and the other two angles
    * are replaced by their supplementary angles. This is necessary because angles
    * close to 90 degrees may be miscategorized as acute or obtuse
    * 
    * @param latParams
    *           The lattice parameters. These are changed
    * 
    * @return The angle(0,1,2 for alpha, beta and gamma) that stays the same or
    *         a negative number if not possible. Assume no two angles are equal.
    */
   private int flipLatticeAngle(double[] latParams)
   {
      if ( latParams == null || latParams.length < 6 )
         return -1;
      
      int sgn = 1;
      
      if ( latParams[3] >= 90 )
         sgn = -1;
      
      int res = 0;
      double min = sgn * ( 90 - latParams[3] );
      
      if ( sgn * ( 90 - latParams[4] ) < min )
      {
         res = 1;
         min = sgn * ( 90 - latParams[4] );
      }
      
      if ( sgn * ( 90 - latParams[5] ) < min )
      {
         res = 2;
         min = sgn * ( 90 - latParams[2] );
      }

      for( int i = 1 ; i < 3 ; i++ )
         latParams[3 + ( res + i ) % 3] = 180 - latParams[3 + ( res + i ) % 3];
      
      return res;
   }

   //Calculates the new UB matrix from the RedCell and UB
   private static double[][] NewUB(ReducedCellPlus RedCell, float[][] UB)
   {
      double[][] transf = RedCell.redCell.getTransformation( );
      
      if ( RedCell.flipUBRow >= 0 )//redo mult by self. Just chang signs in cols/rows in transf
      {
         double[][] ident = new double[ 3 ][ 3 ];
         ident[0][0] = 1;
         ident[0][1] = 0;
         ident[0][2] = 0;
         ident[1][0] = 0;
         ident[1][1] = 1;
         ident[1][2] = 0;
         ident[2][0] = 0;
         ident[2][1] = 0;
         ident[2][2] = 1;

         int k = RedCell.flipUBRow;
         for( int i = 1 ; i <= 2 ; i++ )
            ident[( k + i ) % 3][( k + i ) % 3] = -1;
         transf = LinearAlgebra.mult( transf , ident );
      }

      return LinearAlgebra.mult( LinearAlgebra.float2double( UB ) ,
            LinearAlgebra.getInverse( transf ) );
   }

   // make sure current UB is saved
   private void showChoices()
   {

      double[] latParams1 = lattice_calc.LatticeParamsOfUB( LinearAlgebra
            .float2double( UB ) );

      double delta;
      try
      {
         delta = Double.parseDouble( Delta.getText( ).trim( ) );
      } catch( Exception s )
      {
         delta = .2;
      }
      
      double[] latParams2 = new double[ 6 ];
      System.arraycopy( latParams1 , 0 , latParams2 , 0 , 6 );

      int side = flipLatticeAngle( latParams2 );

      ScalarOpts = new Vector< ReducedCellPlus >( );
      
      ReducedCellInfo SrcRedCell1 = new ReducedCellInfo( 0 , latParams1[0] ,
            latParams1[1] , latParams1[2] , latParams1[3] , latParams1[4] ,
            latParams1[5] );
      
      ReducedCellInfo SrcRedCell2 = new ReducedCellInfo( 0 , latParams2[0] ,
            latParams2[1] , latParams2[2] , latParams2[3] , latParams2[4] ,
            latParams2[5] );
      
      for( int i = 1 ; i < 45 ; i++ )
      {
         ReducedCellInfo redCell = new ReducedCellInfo( i , latParams1[0] ,
               latParams1[1] , latParams1[2] , latParams1[3] , latParams1[4] ,
               latParams1[5] );
         
         double dist = redCell.weighted_distance( SrcRedCell1 );
         
         if ( dist < delta )
            ScalarOpts.add( new ReducedCellPlus( redCell , -1 , dist ) );

         redCell = new ReducedCellInfo( i , latParams2[0] , latParams2[1] ,
               latParams2[2] , latParams2[3] , latParams2[4] , latParams2[5] );
         dist = redCell.weighted_distance(  SrcRedCell2 );
         
         if ( dist < delta )
            ScalarOpts.add( new ReducedCellPlus( redCell , side , dist ) );

      }

      for( int i = 0 ; i < SymmetryChoices.length ; i++ )
         if ( !SymmetryChoices[i].isSelected( ) )
            FilterOutSymmetry( ScalarOpts , ChoicesString[i] );

      for( int i = 0 ; i < Centerings.length ; i++ )
         if ( !Centerings[i].isSelected( ) )
            FilterOutCenterings( ScalarOpts , i );

      if ( ScalarOpts == null || ScalarOpts.size( ) < 1 )
      {
         Choices = new String[ 1 ];
         Choices[0] = "No Cell Types Available";
         viewer.setNewStringList( Choices );
         return;
      }

      ReducedCellPlus[] RCells = ScalarOpts.toArray( new ReducedCellPlus[ 0 ] );
      
      Comparator Comp = null;
      int sortChoice = SortOn.getSelectedIndex( );
      
      if ( sortChoice == 0 )
         
         Comp = new SymmetrySort( );
      
      else if ( sortChoice == 1 )
         
         Comp = new FormSort( );
      
      else if ( sortChoice == 2 )
         
         Comp = new distSort( );
      
      if ( Comp != null )
         Arrays.sort( RCells , Comp );

      ScalarOpts = new Vector< ReducedCellPlus >( RCells.length );
      for( int i = 0 ; i < RCells.length ; i++ )
         ScalarOpts.add( RCells[i] );

      String[] ScalarOptsStrings = new String[ ScalarOpts.size( ) ];
      for( int i = 0 ; i < ScalarOpts.size( ) ; i++ )
         ScalarOptsStrings[i] = MakeString( RCells[i] );

      viewer.setNewStringList( ScalarOptsStrings );

   }

   @Override
   public boolean receive(Message message)
   {

      UB = ( float[][] ) message.getValue( );
      UB = LinearAlgebra.getTranspose( UB );
      if ( UB == null )
      {

         Choices = new String[ 1 ];
         Choices[0] = "No Orientation Matrix ";
         viewer.setNewStringList( Choices );
         return false;
      }
      showChoices( );
      return false;
   }

   
   private void FilterOutSymmetry(Vector< ReducedCellPlus > ScalarOpts,
         String symm)
   {

      for( int i = ScalarOpts.size( ) - 1 ; i >= 0 ; i-- )
      {
         ReducedCellPlus red = ScalarOpts.elementAt( i );
         if ( red.redCell.getCellType( ).equals( symm ) )
            ScalarOpts.remove( i );
      }
   }

   
   private void FilterOutCenterings(Vector< ReducedCellPlus > ScalarOpts,
         int cent)
   {

      for( int i = ScalarOpts.size( ) - 1 ; i >= 0 ; i-- )
      {
         ReducedCellPlus red = ScalarOpts.elementAt( i );
         if ( cent < 4 )
         {
            if ( red.redCell.getCentering( ).startsWith(
                  CentChoiceStrings[cent] ) )
               ScalarOpts.remove( i );
         } else
         {
            int lineNum = red.redCell.getFormNum( );
            boolean remove = false;
            
            if ( cent == 4 && lineNum > 17 )
               
               remove = true;
            
            else if ( cent == 5 && lineNum <= 17 )
               
               remove = true;
            
            else if ( cent == 6 && lineNum > 8 && lineNum <= 17 )
               
               remove = true;
            
            else if ( cent == 6 && lineNum > 25 )
               
               remove = true;
            
            else if ( cent == 7 && lineNum <= 8 )
               
               remove = true;
            
            else if ( cent == 7 && lineNum > 17 && lineNum <= 25 )
               
               remove = true;
            
            if ( remove )
               ScalarOpts.remove( i );
         }

      }
   }

   
   private String MakeString(ReducedCellPlus RedCell)
   {

      TextSeparators ut = new TextSeparators( "plain" );
      String Res = ut.start( );
      
      Res += "Form Num:" + RedCell.redCell.getFormNum( );
      Res += "     Error:" + String.format( "%6.4f" , RedCell.distance )
            + ut.eol( );
      ;
      double[][] UB1 = NewUB( RedCell , UB );
      
      Res += String.format( "%-14s" , RedCell.redCell.getCellType( ) ) + "  "
            + RedCell.redCell.getCentering( ) + "  ";
      
      int lineNum = RedCell.redCell.getFormNum( );
      
      if ( lineNum <= 25 )
         
         Res += "a=b";
      
      else
         
         Res += "a <>b";
      
      if ( lineNum < 9 || ( lineNum > 17 && lineNum <= 25 ) )
         
         Res += "=c";
      
      else
         
         Res += "<>c";
      

      Res += ut.eol( );

      double[] LatticeParams = lattice_calc.LatticeParamsOfUB( UB1 );
      
      // plain
      // Res +=ut.table()+ut.row();
      for( int i = 0 ; i < 3 ; i++ )
      {
         Res += String.format( "%5.3f " , LatticeParams[i] );
         
         if ( i + 1 < 6 )
            
            Res += ut.col( );
         
         else
            
            Res += ut.rowEnd( );
         
      }

      for( int i = 3 ; i < 6 ; i++ )
      {
         Res += String.format( "%5.2f " , LatticeParams[i] );
         
         if ( i + 1 < 6 )
            
            Res += ut.col( );
         
         else
            
            Res += ut.rowEnd( );
      }
      
      Res += "Vol:"+ String.format("%8.3f",LatticeParams[6])+ut.eol();
    
      return Res;

   }

   /**
    * @param args  args[0] contains the filename with an orientation 
    *                     matrix.
    */
   public static void main(String[] args)
   {

      String filename = args[0];//"C:/ISAW/SampleRuns/SNS/SNAP/WSF/235_46/quartzTest.mat";
      
      float[][] UB = ( float[][] ) Operators.TOF_SCD.IndexJ
            .readOrient( filename );
      
      ScalarHandlePanel panel = new ScalarHandlePanel( UB );
      
      JFrame jf = new JFrame( "Test" );
      
      jf.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      
      jf.getContentPane( ).setLayout( new GridLayout( 1 , 1 ) );
      
      jf.getContentPane( ).add( panel.getPanel( ) );
      
      jf.setSize( 400 , 600 );
      
      WindowShower.show( jf );
      

   }

   /**
    * This class handles the action listening for the class ScalarHandlePanel
    * @author ruth
    *
    */
   class ThisActionListener implements ActionListener
   {

      int category;

      public ThisActionListener(int category)
      {

         this.category = category;
      }

      @Override
      public void actionPerformed(ActionEvent e)
      {

         String command = e.getActionCommand( );
         
         if ( command == SHOW_CENTERINGS )
         {
            if ( OrientMatMessageCenter != null )
               
               OrientMatMessageCenter.send( new Message(
                     Commands.GET_ORIENTATION_MATRIX , null , false ) );
            
            else if ( UB == null )
               
               return;
            
            else
           
               showChoices( );
           
         } else if ( command == APPLY_CENTERINGS )
         {
            int k = viewer.getSelectedChoice( );
            
            if ( k < 0 )
               
               k = viewer.getLastViewedChoice( );
            
            if ( k < 0 || k >= ScalarOpts.size( ) )
            {
               JOptionPane.showMessageDialog( null ,
                     "Cannot Determine desired Choice" );
               return;
               
            }
            
            double[][] UB1 = NewUB( ScalarOpts.elementAt( k ) , UB );
            
            UB = LinearAlgebra.double2float( UB1 );
            
            if ( OrientMatMessageCenter != null )
               
               OrientMatMessageCenter.send( new Message(
                                                 Commands.SET_ORIENTATION_MATRIX ,
                                                 LinearAlgebra.getTranspose( UB ) ,
                                                 true ) );

         } else if ( command.toUpperCase( ).startsWith( "SET" )
               || command.toUpperCase( ).startsWith( "CLEAR" ) )
         {
            
            boolean state = true;
            
            if ( command.toUpperCase( ).startsWith( "CLEAR" ) )
               
               state = false;

            for( int i = 0 ; i < SymmetryChoices.length ; i++ )
               
               SymmetryChoices[i].setSelected( state );

            
            for( int i = 0 ; i < Centerings.length ; i++ )
               
               Centerings[i].setSelected( state );

         }

      }

   }

   /**
    * This class is a wrapper around ReducedCellInfo with other information
    * necessary for ScalarHandlePanel to do its job
    * @author ruth
    *
    */
   class ReducedCellPlus
   {

      int             flipUBRow;

      double          distance;

      ReducedCellInfo redCell;

      public ReducedCellPlus(ReducedCellInfo redCell, int flipUBRow, double dist)
      {

         this.redCell = redCell;
         this.flipUBRow = flipUBRow;
         this.distance = dist;
      }

   }

   /**
    * This Comparator is used for sorting lists by Symmetry constraints
    * 
    * @author ruth
    *
    */
   class SymmetrySort implements Comparator< ReducedCellPlus >
   {

      @Override
      public int compare(ReducedCellPlus arg0, ReducedCellPlus arg1)
      {

         if ( arg0 == null )
            
            if ( arg1 == null )
               
               return 0;
         
            else
               
               return -1;
         
         else if ( arg1 == null )
            
            return 1;

         int i1 = Symm.indexOf( arg0.redCell.getCellType( ) );
         int i2 = Symm.indexOf( arg1.redCell.getCellType( ) );
         
         if ( i1 < 0 )
            
            if ( i2 < 0 )
               
               return 0;
         
            else
               
               return -1;
         
         else if ( i2 < 0 )
            
            return 1;
         
         if ( i1 < i2 )
            
            return -1;
         
         if ( i1 > i2 )
            
            return 1;
         
         
         i1 = Cent.indexOf( arg0.redCell.getCentering( ) );
         i2 = Cent.indexOf( arg1.redCell.getCentering( ) );
         if ( i1 < 0 )
            
            if ( i2 < 0 )
               
               return 0;
         
            else
               
               return -1;
         
         else if ( i2 < 0 )
            
            return 1;
    
         if ( i1 < i2 )
            
            return -1;
         
         if ( i1 > i2 )
            
            return 1;
         
         return 0;
      }

   }

   /**
    * This comparator is used in sorting a list of ReducedCellPlus according
    * to the Form 
    *  See Form numbers and referenced paper in ReducedCellInfo
    * 
    * @author ruth
    *
    */
   class FormSort implements Comparator< ReducedCellPlus >
   {

      @Override
      public int compare(ReducedCellPlus arg0, ReducedCellPlus arg1)
      {

         if ( arg0 == null )
            
            if ( arg1 == null )
               
               return 0;
         
            else
               
               return -1;
         
         else if ( arg1 == null )
            
            return 1;
         

         int i1 = arg0.redCell.getFormNum( );
         int i2 = arg1.redCell.getFormNum( );
         
         if ( i1 < 0 )
            
            if ( i2 < 0 )
               
               return 0;
         
            else
               
               return -1;
         
         else if ( i2 < 0 )
            
            return 1;
         
         if ( i1 < i2 )
            
            return -1;
         
         if ( i1 > i2 )
            
            return 1;
         
         return 0;
      }

   }


   /*
    *   This Comparator is used to sort the list according to errors from
    *   theoretical scalars
    *   
    *     @author ruth
    *
    */
   class distSort implements Comparator< ReducedCellPlus >
   {

      @Override
      public int compare(ReducedCellPlus arg0, ReducedCellPlus arg1)
      {

         if ( arg0 == null )
            
            if ( arg1 == null )
               
               return 0;
         
            else
               
               
               return -1;
         else if ( arg1 == null )
            
            return 1;

         double i1 = arg0.distance;
         double i2 = arg1.distance;
         
         if ( i1 < 0 )
            
            if ( i2 < 0 )
               
               return 0;
         
            else
               
               return -1;
         
         else if ( i2 < 0 )
            
            return 1;
         
         if ( i1 < i2 )
            
            return -1;
         
         if ( i1 > i2 )
            
            return 1;
         
         return 0;
      }

   }
}
