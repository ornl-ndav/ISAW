/* 
 * File: SetPeak1InfoHandler.java
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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import gov.anl.ipns.MathTools.Geometry.Tran3D;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import DataSetTools.operator.Generic.TOF_SCD.IPeak;

/**
 * Handler to display information on possible hkl values for a UB matrix
 * with specified hkl values at several "peaks"
 * @author ruth
 *
 */

public class SetPeak1InfoHandler implements InfoHandler, AncestorListener,
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

      ActionListener listener = null;
      public void setActionListener( ActionListener listener)
      {
         this.listener = listener;
      }
      
      public void kill()
      {
         SelectedPeaks = null;
         B_mat = null;
         Panel = null;
         Choices = null;
         jt = null;
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
         

         if(listener != null)
            listener.actionPerformed( new ActionEvent(this,
                                             ActionEvent.ACTION_PERFORMED,
                                             "List Element Selected") );
      }

    
      /* 
       *Displays a list of possible hkl values. A line can be selected from the table, which will
       *change the selecedChoiceIndex
       *
       * @param   pk              Not used
       * @param  transformation  Not used
       * @param   panel           The panel to draw info calculated from values set
       *                          in previous methods.
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
                  SelectedPeaks.getSetPeakQ( 0 ) , Delta1 , Delta2 , c );

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
         
         if(listener != null)
            listener.actionPerformed( new ActionEvent(this,
                                             ActionEvent.ACTION_PERFORMED,
                                             "List Element Selected") );

      }


  

   /**
    * @param args
    */
   public static void main(String[] args)
   {

      // TODO Auto-generated method stub

   }

}
