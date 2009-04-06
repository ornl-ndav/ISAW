/* 
 * File: XtalLatticeControl.java
 *
 * Copyright (C) 2009, Ruth Mikkelson
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
 *  $Author$
 *  $Date$            
 *  $Rev$
 */

package DataSetTools.components.ui.Peaks;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import DataSetTools.operator.Generic.TOF_SCD.*;
import gov.anl.ipns.MathTools.*;
import gov.anl.ipns.MathTools.Geometry.Tran3D;
import gov.anl.ipns.Util.Sys.*;

/**
 * This class handles inputting the crystal lattice information and handling
 * the calculation of possible hkl values for given peaks. Uses SetPeaks for inputs 
 * and Info for displaying information
 * 
 * @author Ruth
 *
 */
public class XtalLatticeControl extends Object
{



   /**
    * This is the actionCommand of an ActionEvent for a listener.
    * @see getListener()
    */
   public static String CRYSTAL_LAT_INPUT_TEXT = "Crystal Lattice Input";

   // Default inputs for peaks of interest
   SetPeaks             PeakSetter;

   // Place to display information like possible hkl's
   Info                 OutInfo;

   //Basic GUI element. Can use listener below to use other event originators
   JButton              inpButton;

   //Listener for button events
   MyActionListener     listener;

   //Crystal parameters a,b,c,alpha,beta,gamma
   float[]              XtalParams;

   // Material matrix
   float[][]            BMat;

   //closeness of length q val for an hkl  to the length of a peak q 
   //  Delta 1.  Delta2 is closeness of corresponding dot products
   //  for two different peaks 
   float                Delta1 , Delta2;

   //Centering type character P,A,B,C,F,I, or R
   char                 Centering;

   //Handler to show info about possible hkl's for one  set peaks
   SetPeak1InfoHandler  Peak1Info;

   //Handler to show info about possible hkl's for 2nd set peaks
   //   Given first set peak
   SetPeak2InfoHandler  Peak2Info;


   /**
    * Constructor
    * @param peakSetter  A SetPeak object with set peak information
    * @param outInfo     An Info object that displays information on
    *                    possible hkl's for set peaks
    */
   public XtalLatticeControl( SetPeaks peakSetter, Info outInfo )
   {

      XtalParams = null;
      PeakSetter = peakSetter;
      OutInfo = outInfo;

      listener = new MyActionListener();
      inpButton = null;
      BMat = null;

      Peak1Info = null;
      Peak2Info = null;

      Delta1 = .01f;
      Delta2 = .03f;

      Centering = 'x';
   }


   /**
    * Sets the Delta1 and Delta2
    * 
    * @param Delta1  Type 1 Error for fitting
    * @param Delta2  Type 2 Error for fitting
    * 
    * NOTE: Negative Deltas will not change anything
    */
   public void setDeltas( float Delta1 , float Delta2 )
   {

      if( Delta1 > 0 )
         this.Delta1 = Delta1;

      if( Delta2 > 0 )
         this.Delta2 = Delta2;

      if( Peak1Info != null )
         Peak1Info.setErrData( this.Delta1 , this.Delta2 , Centering );

      if( Peak2Info != null )
         Peak2Info.setErrData( this.Delta1 , this.Delta2 , Centering );
   }


   /**
    * Sets the centering type. It should be P,A,B,C,F,I, or R
    * @param c  The centering code. Will be ignored if not P,A,B,C,F,I, or R
    */
   public void setCentering( char c )
   {

      Centering = c;

      if( Peak1Info != null )
         Peak1Info.setErrData( Delta1 , Delta2 , c );

      if( Peak2Info != null )
         Peak2Info.setErrData( Delta1 , Delta2 , c );
   }


   /**
    * Sets the Crystal lattice parameters
    * @param a
    * @param b
    * @param c
    * @param alpha
    * @param beta
    * @param gamma
    */
   public void setXtalParams( float a , float b , float c , float alpha ,
            float beta , float gamma )
   {

      if( a <= 0 || b <= 0 || c <= 0 )
      {
         XtalParams = null;
         BMat = null;
         SharedMessages.addmsg( "Improper Crystal Parameters" );
         return;
      }

      if( XtalParams == null )
         XtalParams = new float[ 6 ];

      XtalParams[ 0 ] = a;
      XtalParams[ 1 ] = b;
      XtalParams[ 2 ] = c;
      XtalParams[ 3 ] = alpha;
      XtalParams[ 4 ] = beta;
      XtalParams[ 5 ] = gamma;

      double[] scalars = Util
               .scalars( LinearAlgebra.float2double( XtalParams ) );

      double[][] RealTens = new double[ 3 ][ 3 ];
      for( int i = 0 ; i < 3 ; i++ )
         for( int j = i ; j < 3 ; j++ )
            if( i == j )

               RealTens[ i ][ j ] = scalars[ i ];

            else
            {
               RealTens[ i ][ j ] = (float) scalars[ 6 - i - j ];
               RealTens[ j ][ i ] = RealTens[ i ][ j ];
            }

      double[][] RecipTens = LinearAlgebra.copy( RealTens );

      if( ! LinearAlgebra.invert( ( RecipTens ) ) )
      {
         XtalParams = null;
         SharedMessages.addmsg( "Improper Crystal Parameters" );
         return;
      }

      double[] RecipXtalParams = XtalParamsFromGMatrix( RecipTens );

      BMat = LinearAlgebra.double2float( lattice_calc
               .A_matrix( RecipXtalParams ) );
      BMat = LinearAlgebra.getTranspose( BMat );


      //----------------------- Add or change info listeners ------------------------
      if( Peak1Info == null || Peak2Info == null )
      {

         Peak1Info = new SetPeak1InfoHandler( PeakSetter , BMat );
         Peak2Info = new SetPeak2InfoHandler( PeakSetter , Peak1Info , BMat );

         OutInfo.addInfoHandler( "Peak1(Crystal Params" , Peak1Info );
         OutInfo.addInfoHandler( "Peak2(Crystal Params" , Peak2Info );

      }
      else
      {
         Peak1Info.setNewData( BMat );
         Peak2Info.setNewData( BMat );
      }
   }


   /**
    * Calculates the (reciprocal)crystal parameters  from a tensor matrix. The inverse
    * operation is not applied to the Tensor matrix.
    * 
    * @param TensorMatrix   The Tensor matrix
    * 
    * @return  a float containing a,b,c,alpha,beta,gamma. a is the sqrt(TensorMatris[0][0])
    */
   public static double[] XtalParamsFromGMatrix( double[][] TensorMatrix )
   {

      double[] Res = new double[ 6 ];
      if( TensorMatrix == null )
         return null;

      try
      {
         Res[ 0 ] = Math.sqrt( TensorMatrix[ 0 ][ 0 ] );
         Res[ 1 ] = Math.sqrt( TensorMatrix[ 1 ][ 1 ] );
         Res[ 2 ] = Math.sqrt( TensorMatrix[ 2 ][ 2 ] );
         Res[ 3 ] = ( Math.acos( TensorMatrix[ 1 ][ 2 ] / Res[ 1 ] / Res[ 2 ] )
                  / Math.PI * 180f );
         Res[ 4 ] = ( Math.acos( TensorMatrix[ 0 ][ 2 ] / Res[ 0 ] / Res[ 2 ] )
                  / Math.PI * 180f );
         Res[ 5 ] = ( Math.acos( TensorMatrix[ 1 ][ 0 ] / Res[ 1 ] / Res[ 0 ] )
                  / Math.PI * 180f );

         return Res;

      }
      catch( Exception ss )
      {
         SharedMessages.addmsg( "Tensor Matrix is improper" );
         return null;
      }
   }


   /**
    * Sets the Crystal parameters
    * 
    * @param params  The new Crystal parameters
    */
   public void setXtalParams( float[] params )
   {

      if( params == null || params.length < 6 )
         return;

      setXtalParams( params[ 0 ] , params[ 1 ] , params[ 2 ] , params[ 3 ] ,
               params[ 4 ] , params[ 5 ] );

   }


   /**
    * Calculates the UB matrix given the info below and the material matrix
    * @param q1    First Q vector of a peak
    * @param hkl1  hkl value correpsonding to the first peak
    * @param q2    Second Q vector of a peak
    * @param hkl2  hkl value correpsonding to the second peak
    * 
    * @return     The corresponding UB matrix.
    */
   public float[][] CalcUB( float[] q1 , float[] hkl1 , float[] q2 ,
            float[] hkl2 )
   {

      return subs.getOrientationMatrix( q1 , hkl1 , q2 , hkl2 , BMat );
   }


   /**
    * Use this listener to handle actions corresponding to pressing a button.
    * The action command must be the String  CRYSTAL_LAT_INPUT_TEXT
    * @return
    */
   public ActionListener getListener()
   {

      return listener;
   }


   /**
    * Can be used without a GUI element
    * @return The Component that can be used to get Crystal Lattice Parameters 
    */
   public JButton getGUIinputElement()
   {

      if( inpButton == null )
      {
         inpButton = new JButton( CRYSTAL_LAT_INPUT_TEXT );
         inpButton.addActionListener( listener );

      }

      return inpButton;

   }

   /**
    * Listener to the menus
    * @author Ruth
    *
    */
   class MyActionListener implements ActionListener
   {



      /* (non-Javadoc)
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      @Override
      public void actionPerformed( ActionEvent e )
      {

         if( e.getActionCommand() != CRYSTAL_LAT_INPUT_TEXT )
            return;

         JPanel panel = new JPanel();
         panel.setLayout( new GridLayout( 4 , 1 ) );

         //-------
         JPanel pan1 = new JPanel();
         pan1.setLayout( new GridLayout( 1 , 2 ) );
         pan1.add( new JLabel( "Centering" ) );
         JComboBox Cent = new JComboBox( new String[]
         {
                  "P" , "A" , "B" , "C" , "F" , "I" , "R"
         } );
         Cent.setSelectedItem( "" + Centering );
         pan1.add( Cent );
         panel.add( pan1 );

         //-----
         pan1 = new JPanel();
         pan1.setLayout( new GridLayout( 1 , 2 ) );
         pan1.add( new JLabel( "Delta1" ) );
         JTextField Delt1 = new JTextField( "" + Delta1 );
         Delt1.setToolTipText( "Error in lengths in Fitting." );
         pan1.add( Delt1 );
         panel.add( pan1 );

         //----
         pan1 = new JPanel();
         pan1.setLayout( new GridLayout( 1 , 2 ) );
         pan1.add( new JLabel( "Delta2" ) );
         JTextField Delt2 = new JTextField( "" + Delta2 );

         Delt2.setToolTipText( "Error in angles between vectors when Fitting." );
         pan1.add( Delt2 );
         panel.add( pan1 );

         //----------
         panel.add( new JLabel( "Enter Crystal Parameters" ) );
         panel.validate();


         String Res = JOptionPane.showInputDialog( null , panel ,
                  "2,5,3,90,120,90" );
         if( Res == null )
            return;

         String[] S = Res.split( "," );
         float[] F = new float[ 6 ];
         for( int i = 0 ; i < S.length && i < 6 ; i++ )
            try
            {
               F[ i ] = Float.parseFloat( S[ i ].trim() );
            }
            catch( Exception ss )
            {
               JOptionPane.showMessageDialog( null , i + "element is improper" );
               return;
            }


         String Centr = (String) Cent.getSelectedItem();
         if( Centr != null && Centr.length() > 0
                  && Centr.charAt( 0 ) != Centering )
            setCentering( Centr.charAt( 0 ) );

         float Del1 = Delta1;
         float Del2 = Delta2;

         try
         {
            Del1 = Float.parseFloat( Delt1.getText().trim() );

         }
         catch( Exception s )
         {

         }

         try
         {
            Del2 = Float.parseFloat( Delt2.getText().trim() );

         }
         catch( Exception s )
         {

         }

         if( Del1 != Delta1 || Del2 != Delta2 )
            setDeltas( Del1 , Del2 );

         setXtalParams( F );

      }


   }

   //Handler to display information on possible hkl values for 
   // set peak number 1
   class SetPeak1InfoHandler implements InfoHandler , AncestorListener ,
            ListSelectionListener
   {



      SetPeaks  SelectedPeaks;

      float[][] B_mat;

      JPanel    Panel;

      int[][]   Choices;

      int       pickedChoice;

      JTable    jt = null;

      float     Delta1 = .01f , Delta2 = .03f;

      char      c      = 'x';


      /**
       * Constructor
       * @param selectePeaks  SetPeaks object containing the setPeak 
       *                     information
       * @param BMat           The Material matrix 
       */
      public SetPeak1InfoHandler( SetPeaks selectePeaks, float[][] BMat )
      {

         SelectedPeaks = selectePeaks;
         B_mat = BMat;
         Panel = null;
         Choices = null;
         pickedChoice = - 1;
      }


      /**
       * Used when input material matrix has changed. Will update displayed
       * information if it is showing.
       * 
       * @param newData  The new material matrix
       */
      public void setNewData( Object newData )
      {

         if( newData == null || ! ( newData instanceof float[][] ) )
            return;

         B_mat = (float[][]) newData;

         if( Panel != null ) // If panel is displaying, update
            show( null , null , Panel );
      }


      /**
       * If error and centering information change.  Will update displayed
       * information if it is showing.
       * @param Delta1  The delta 1 error in lengths of q vectors
       * @param Delta2  The delta2 error measuring the closeness of dot products
       * @param c      The centering type character P,A,B,C,F,I, or R 
       */
      public void setErrData( float Delta1 , float Delta2 , char c )
      {

         this.Delta1 = Delta1;
         this.Delta2 = Delta2;
         this.c = c;
         if( Panel != null )
            show( null , null , Panel );
      }


      /**
       * 
       * @return  The possible choices for hkl values
       */
      public int[][] getChoices()
      {

         return Choices;

      }


      /**
       * 
       * @return  The seleced choice in the list of choices
       */
      public int getSelectedChoiceIndex()
      {

         return pickedChoice;
      }


      /**
       * Sets the index of the selected choice
       * @param choiceIndex  The index of the selected choice
       */
      public void setSelectedChoiceIndex( int choiceIndex )
      {

         pickedChoice = choiceIndex;

         if( pickedChoice >= 0 && Choices != null
                  && pickedChoice < Choices.length )

            SelectedPeaks
                     .setPeakHKL( 0 , Choices[ pickedChoice ][ 0 ] ,
                              Choices[ pickedChoice ][ 1 ] ,
                              Choices[ pickedChoice ][ 2 ] );

         else

            SelectedPeaks.setPeakHKL( 0 , Float.NaN , Float.NaN , Float.NaN );
      }


      /* 
       *Displays a list of possible hkl values. A line can be selected from the table, which will
       *change the selecedChoiceIndex
       *
       * (non-Javadoc)
       * @see DataSetTools.components.ui.Peaks.InfoHandler#show(DataSetTools.operator.Generic.TOF_SCD.IPeak, gov.anl.ipns.MathTools.Geometry.Tran3D, javax.swing.JPanel)
       */
      @Override
      public void show( IPeak pk , Tran3D transformation , JPanel panel )
      {

         if( panel == null )
            return;

         panel.removeAll();

         if( SelectedPeaks == null || SelectedPeaks.getSetPeakQ( 0 ) == null )
         {
            JTextArea text = new JTextArea( 20 , 30 );
            text
                     .setText( "No Peaks have been set. Use Set Peak option to set \n the first peak" );
            panel.add( text );

            panel.invalidate();
            jt = null;

            Panel = panel;

            return;
         }

         if( B_mat == null )
         {
            JTextArea text = new JTextArea( 20 , 30 );
            text
                     .setText( "The Crystal Lattice constants have not been set correctly. Set these" );

            panel.add( text );
            panel.invalidate();

            jt = null;
            Panel = panel;
            return;

         }

         Choices = subs.FindPossibleHKLs( B_mat ,
                  SelectedPeaks.getSetPeakQ( 0 ) , Delta1 , Delta2 , Centering );

         if( Choices == null || Choices.length < 1 )
         {
            JTextArea text = new JTextArea( 20 , 30 );
            text
                     .setText( "There are NO possible indexing corresponding to the lattice parameters" );

            panel.add( text );
            panel.invalidate();

            jt = null;
            Panel = panel;

            return;
         }

         String[] colNames =
         {
                  "h" , "k" , "l"
         };
         Object[][] Data = ConvertToInteger( Choices );

         jt = new JTable( Data , colNames );

         panel.setLayout( new GridLayout( 1 , 1 ) );

         panel.add( new JScrollPane( jt ) );

         jt.getSelectionModel().addListSelectionListener( this );

         Panel = panel;

      }


      private Object[][] ConvertToInteger( int[][] Choicesx )
      {

         if( Choicesx == null )
            return null;

         Object[][] Res = new Object[ Choicesx.length + 1 ][];
         Res[ 0 ] = new String[]
         {
                  "Try All Choices" , "" , ""
         };

         for( int i = 0 ; i < Choicesx.length ; i++ )
         {
            Res[ i + 1 ] = new Integer[ Choicesx[ i ].length ];

            for( int j = 0 ; j < Choicesx[ i ].length ; j++ )
               Res[ i + 1 ][ j ] = Choicesx[ i ][ j ];

         }

         return Res;

      }


      /* (non-Javadoc)
       * @see javax.swing.event.AncestorListener#ancestorAdded(javax.swing.event.AncestorEvent)
       */
      @Override
      public void ancestorAdded( AncestorEvent event )
      {


      }


      /* (non-Javadoc)
       * @see javax.swing.event.AncestorListener#ancestorMoved(javax.swing.event.AncestorEvent)
       */
      @Override
      public void ancestorMoved( AncestorEvent event )
      {


      }


      /* (non-Javadoc)
       * @see javax.swing.event.AncestorListener#ancestorRemoved(javax.swing.event.AncestorEvent)
       */
      @Override
      public void ancestorRemoved( AncestorEvent event )
      {

         // Panel has been removed so another view is available
         Panel = null;
         jt = null;

      }


      /* 
       * If a row has been selected, changes the index of the  hkl choices
       * (non-Javadoc)
       * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
       */
      @Override
      public void valueChanged( ListSelectionEvent e )
      {

         if( jt == null )
            pickedChoice = - 1;

         int row = jt.getSelectedRow();

         if( row <= 0 )

            pickedChoice = - 1;

         else

            pickedChoice = row - 1;


         if( pickedChoice >= 0 )

            SelectedPeaks
                     .setPeakHKL( 0 , Choices[ pickedChoice ][ 0 ] ,
                              Choices[ pickedChoice ][ 1 ] ,
                              Choices[ pickedChoice ][ 2 ] );
         else

            SelectedPeaks.setPeakHKL( 0 , Float.NaN , Float.NaN , Float.NaN );

      }


   }


   //Handler to display information on possible hkl values for 
   // set peak number 2, Given the information about set Peak number 1

   class SetPeak2InfoHandler implements InfoHandler , AncestorListener ,
            ListSelectionListener
   {



      JPanel              Panel = null;

      SetPeaks            selectedPeaks;

      SetPeak1InfoHandler peak1;

      int[][]             Choices;

      int[]               NcorrespTo;

      int                 pickedChoice;

      float[][]           BMat;

      float               Delta1 = .01f , Delta2 = .03f;

      char                c      = 'x';

      JTable              jt;

      boolean             AllChoices_SelPeak1;


      /**
       * Constructor
       * @param SelectedPeaks  The SetPeak Object containing the set peaks
       * @param Peak1          The SetPeak1 info handler
       * @param B_mat          The material orientation matrix
       */
      public SetPeak2InfoHandler( SetPeaks SelectedPeaks,
               SetPeak1InfoHandler Peak1, float[][] B_mat )
      {

         selectedPeaks = SelectedPeaks;
         peak1 = Peak1;
         BMat = B_mat;
         Choices = null;
         AllChoices_SelPeak1 = true;

         pickedChoice = - 1;
         jt = null;
         NcorrespTo = null;
      }


      /**
       * Used when input material matrix has changed. Will update displayed
       * information if it is showing.
       * 
       * @param newData  The new material matrix
       */
      public void setNewData( Object newData )
      {

         if( newData == null || ! ( newData instanceof float[][] ) )
            return;

         pickedChoice = - 1;
         Choices = null;

         AllChoices_SelPeak1 = true;
         BMat = (float[][]) newData;

         if( Panel != null )
            show( null , null , Panel );
      }


      /**
       * If error and centering information change.  Will update displayed
       * information if it is showing.
       * @param Delta1  The delta 1 error in lengths of q vectors
       * @param Delta2  The delta2 error measuring the closeness of dot products
       * @param c      The centering type character P,A,B,C,F,I, or R 
       */
      public void setErrData( float Delta1 , float Delta2 , char c )
      {

         this.Delta1 = Delta1;
         this.Delta2 = Delta2;
         this.c = c;

         pickedChoice = - 1;
         Choices = null;

         AllChoices_SelPeak1 = true;

         if( Panel != null )
            show( null , null , Panel );
      }


      /**
       * 
       * @return  The possible choices for hkl values
       */
      public int[][] getChoices()
      {

         return Choices;

      }


      /**
       * 
       * @return  The seleced choice in the list of choices
       */
      public int getSelectedChoiceIndex()
      {

         return pickedChoice;
      }


      /* 
       * If a row has been selected, changes the index of the  hkl choices
       * (non-Javadoc)
       * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
       */
      @Override
      public void valueChanged( ListSelectionEvent e )
      {


         if( jt == null )
         {
            pickedChoice = - 1;
            return;
         }

         int row = jt.getSelectedRow();

         if( row <= 0 )

            pickedChoice = - 1;

         else

            pickedChoice = row - 1;

         if( pickedChoice >= 0 )

            selectedPeaks
                     .setPeakHKL( 1 , Choices[ pickedChoice ][ 0 ] ,
                              Choices[ pickedChoice ][ 1 ] ,
                              Choices[ pickedChoice ][ 2 ] );
         else

            selectedPeaks.setPeakHKL( 1 , Float.NaN , Float.NaN , Float.NaN );


         int S = 0;

         if( NcorrespTo != null && AllChoices_SelPeak1 )
            for( int i = 0 ; i < NcorrespTo.length ; i++ )
            {
               S += NcorrespTo[ i ];

               if( row <= S )
               {
                  peak1.setSelectedChoiceIndex( i );

                  return;
               }
            }


      }


      /* (non-Javadoc)
       * @see javax.swing.event.AncestorListener#ancestorAdded(javax.swing.event.AncestorEvent)
       */
      @Override
      public void ancestorAdded( AncestorEvent event )
      {


      }


      /* (non-Javadoc)
       * @see javax.swing.event.AncestorListener#ancestorMoved(javax.swing.event.AncestorEvent)
       */
      @Override
      public void ancestorMoved( AncestorEvent event )
      {


      }


      /* (non-Javadoc)
       * @see javax.swing.event.AncestorListener#ancestorRemoved(javax.swing.event.AncestorEvent)
       */
      @Override
      public void ancestorRemoved( AncestorEvent event )
      {

         Panel = null;
         jt = null;
      }


      /*  
       * Displays a list of possible hkl values. A line can be selected from the table, which will
       * change the selecedChoiceIndex
       * 
       *(non-Javadoc)
       * @see DataSetTools.components.ui.Peaks.InfoHandler#show(DataSetTools.operator.Generic.TOF_SCD.IPeak, gov.anl.ipns.MathTools.Geometry.Tran3D, javax.swing.JPanel)
       */
      @Override
      public void show( IPeak pk , Tran3D transformation , JPanel panel )
      {

         Panel = panel;

         if( panel == null )
            return;

         panel.removeAll();

         panel.setLayout( new GridLayout( 1 , 1 ) );

         if( selectedPeaks == null || selectedPeaks.getSetPeakQ( 1 ) == null )
         {
            JTextArea text = new JTextArea( 20 , 30 );
            text.setText( " Peak 2 has not been set. Use Set Peak to set this" );

            panel.add( text );
            panel.validate();
            panel.repaint();
         }

         if( BMat == null )
         {
            JTextArea text = new JTextArea( 20 , 30 );
            text
                     .setText( "The Crystal Lattice constants have not been set correctly. Set these" );

            panel.add( text );
            panel.invalidate();
            jt = null;
            return;

         }

         int[][] Choices1 = null;

         AllChoices_SelPeak1 = true;

         if( peak1 != null )
            Choices1 = peak1.getChoices();

         if( peak1 != null && Choices1 != null
                  && peak1.getSelectedChoiceIndex() >= 0 )
         {
            int[] C = Choices1[ peak1.getSelectedChoiceIndex() ];
            Choices1 = new int[ 1 ][ 3 ];

            Choices1[ 0 ] = C;
            AllChoices_SelPeak1 = false;

         }
         Vector< Object[] > ChoicesV = new Vector< Object[] >();

         // ChoicesV.add( new String[]{"Try all Choices","",""});
         if( Choices1 != null && AllChoices_SelPeak1 )

            NcorrespTo = new int[ Choices1.length ];

         else

            NcorrespTo = new int[ 1 ];

         if( Choices1 == null )
         {
            JTextArea text = new JTextArea( 20 , 20 );
            text
                     .setText( "No Choices found for Peak1\n Try viewing Info for SelectPeak1 First" );

            panel.add( text );
            panel.validate();
            return;

         }
         else
         {
            for( int i = 0 ; i < Choices1.length ; i++ )
            {

               int[][] Choices2 = subs.FindPossibleHKLs( BMat , selectedPeaks
                        .getSetPeakQ( 0 ) , selectedPeaks.getSetPeakQ( 1 ) ,
                        Choices1[ i ] , Delta1 , Delta2 , Centering );

               if( Choices2 == null )

                  NcorrespTo[ i ] = 0;

               else
               {
                  NcorrespTo[ i ] = Choices2.length;

                  for( int j = 0 ; j < Choices2.length ; j++ )
                  {
                     Integer[] Ix = new Integer[ 6 ];
                     Ix[ 0 ] = Choices2[ j ][ 0 ];
                     Ix[ 1 ] = Choices2[ j ][ 1 ];
                     Ix[ 2 ] = Choices2[ j ][ 2 ];
                     Ix[ 3 ] = Choices1[ i ][ 0 ];
                     Ix[ 4 ] = Choices1[ i ][ 1 ];
                     Ix[ 5 ] = Choices1[ i ][ 2 ];

                     ChoicesV.add( Ix );
                  }
               }

            }

         }

         Choices = new int[ ChoicesV.size() ][ 6 ];

         for( int i = 0 ; i < ChoicesV.size() ; i++ )
         {
            Integer[] elt = (Integer[]) ChoicesV.elementAt( i );

            if( elt != null && elt.length >= 6 )

               for( int j = 0 ; j < 6 ; j++ )
                  Choices[ i ][ j ] = elt[ j ].intValue();

            else

               java.util.Arrays.fill( Choices[ i ] , Integer.MIN_VALUE );
         }

         ChoicesV.insertElementAt( new String[]
         {
                  "Try all Choices" , "" , "" , "" , "" , ""
         } , 0 );

         jt = new JTable( ChoicesV.toArray( new Object[ 0 ][ 0 ] ) ,
                  new String[]
                  {
                           "h" , "k" , "l" , "h0" , "k0" , "l0"
                  } );

         panel.setLayout( new GridLayout( 1 , 1 ) );
         panel.add( new JScrollPane( jt ) );

         jt.getSelectionModel().addListSelectionListener( this );


      }

   }
}
