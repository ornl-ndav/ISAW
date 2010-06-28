/* 
 * File: SetPeak2InfoHandler.java
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
 *  $Author:$
 *  $Date:$            
 *  $Rev:$
 */
package DataSetTools.components.ui.Peaks;

import gov.anl.ipns.MathTools.Geometry.Tran3D;

import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import DataSetTools.operator.Generic.TOF_SCD.IPeak;

/**
 * Handler to display information on possible hkl values for 
 *      set peak number 2, Given the information about set Peak number 1
 *      
 * @author ruth
 *
 */
 
public class SetPeak2InfoHandler implements InfoHandler, 
                                            AncestorListener,
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
      public SetPeak2InfoHandler( SetPeaks            SelectedPeaks,
                                  SetPeak1InfoHandler Peak1, 
                                  float[][]           B_mat )
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

      public void kill()
      {
       selectedPeaks = null;
       Panel = null;
       peak1 = null;
       Choices =null;
       NcorrespTo = null;
       BMat = null;
       jt= null;
       
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
       * 
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

        /* if( !(e.getSource() == jt))
         {
            pickedChoice = -1;
            selectedPeaks.setPeakHKL( 1 , Float.NaN , Float.NaN , Float.NaN );
            
            if( Panel != null)//Update what is showing
               show( null,null,Panel);
            
            return;
         }
         */     
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
            if( pickedChoice >=0)
            for( int i = 0 ; i < NcorrespTo.length ; i++ )
            {
               S += NcorrespTo[ i ];

               if( row <= S )
               {
                  peak1.setSelectedChoiceIndex( i );

                  return;
               }
            }
            else
               peak1.setSelectedChoiceIndex( -1 );


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
       * @param   pk              Not used
       * @param  transformation  Not used
       * @param   panel           The panel to draw info calculated from values set
       *                          in previous methods.
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
            return;
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
                  && peak1.getSelectedChoiceIndex() >= 0 &&
                  peak1.getSelectedChoiceIndex( )<Choices1.length)
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
                        Choices1[ i ] , Delta1 , Delta2 , c );

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

   
   /**
    * @param args
    */
   public static void main(String[] args)
   {

      // TODO Auto-generated method stub

   }

}
