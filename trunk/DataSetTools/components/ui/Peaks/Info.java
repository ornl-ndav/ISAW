/* 
 * File: Info.java
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
 *  $Author$:
 *  $Date$:           
 *  $Rev$:
 */

package DataSetTools.components.ui.Peaks;

import gov.anl.ipns.MathTools.Geometry.Tran3D;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;

import javax.swing.border.*;

import DataSetTools.operator.Generic.TOF_SCD.IPeak;


/**
 * Handles information that occurs in the Info Pane with a combo box to
 * determine which information is visible.
 * 
 * @author Ruth
 * 
 */
public class Info extends JPanel implements ActionListener ,
         IRotatePeaksHandler , ISelectPeakHandler
{



   JComboBox                         Choices;

   Vector< String >                  ChoiceItems;

   Hashtable< String , InfoHandler > table;

   IPeak                             currentPeak           = null;

   Tran3D                            currentTransformation = null;

   InfoHandler                       CurrentHandler        = null;

   JPanel                            viewPanel;


   /**
    * Constructor
    * 
    * @param V3DControl
    *           The input control on top of View 3D
    */
   public Info( View3DControl V3DControl )
   {

      super();

      if( V3DControl != null  && V3DControl.Peaks.size() > 0 )
      {
         V3DControl.addSelectPeakHandler( this );
         V3DControl.addRotatePeaksHandler( this );
         currentPeak = V3DControl.Peaks.elementAt( 0 );
      }else
         currentPeak = null;

      ChoiceItems = new Vector< String >();

      Choices = new JComboBox();
      Choices.addActionListener( this );
      table = new Hashtable< String , InfoHandler >();

      addInfoHandler( "Selected Peak Information" , new SelPeakInfoHandler() );
      addInfoHandler( "Rotation Information" , new RotatePeaksInfoHandler() );

      MakeChoiceBox();

      setLayout( new BorderLayout() );

      add( Choices , BorderLayout.NORTH );

      viewPanel = new JPanel();
      add( viewPanel , BorderLayout.CENTER );

      currentTransformation = V3DControl.view.getCurrentTransformation();
     
      CurrentHandler = table.get( "Rotation Information" );
      Choices.setSelectedItem( "Rotation Information" );


      this.setBorder( new TitledBorder( new LineBorder( Color.black ) ,
               "Information" ) );

   }


   // Makes the choice combo box
   private void MakeChoiceBox()
   {

      Choices.removeAllItems();
      for( int i = 0 ; i < ChoiceItems.size() ; i++ )
         Choices.addItem( ChoiceItems.elementAt( i ) );

   }


   /**
    * Adds an InfoHandler to the list of InfoHandlers
    * 
    * @param MenuString
    *           The String that will appear in the combo box
    * @param infHandle
    *           The handler associated with this String
    */
   public void addInfoHandler( String MenuString , InfoHandler infHandle )
   {

      if( MenuString == null | infHandle == null )
         return;

      if( ! ChoiceItems.contains( MenuString ) )
      {
         ChoiceItems.add( MenuString );
         Choices.addItem( MenuString );
         Choices.validate();
      }

      table.put( MenuString , infHandle );

   }


   /**
    * Removes the MenuString from the Combo box and the associated handler .
    * 
    * @param MenuString
    *           The string in the combobos
    */
   public void removeInfoHandler( String MenuString )
   {

      if( MenuString == null || ! ChoiceItems.contains( MenuString ) )
         return;

      ChoiceItems.remove( MenuString );
      table.remove( MenuString );
      Choices.removeItem( MenuString );

   }


   @Override
   public void actionPerformed( ActionEvent e )
   {

      if( e.getSource() != Choices )
         return;


      String viewType = (String) Choices.getSelectedItem();
      if( viewType == null )
         return;


      CurrentHandler = table.get( viewType );

      if( CurrentHandler != null )
      {
         CurrentHandler.show( currentPeak , currentTransformation , viewPanel );
         validate();
         invalidate();
      }


   }


   /**
    * Invoked when the 3D view of reciprocal space is rotated. This information
    * will be updated in the current viewed
    * 
    * @param transformation
    *           The new transformation
    */
   @Override
   public void RotatePeaks( Tran3D transformation )
   {

      currentTransformation = transformation;
      if( CurrentHandler != null )
      {
         CurrentHandler.show( currentPeak , currentTransformation , viewPanel );
         validate();
         invalidate();
      }
   }


   /**
    * Invoked when the 3D view of reciprocal space has a peak selected. This
    * information will be updated in the current view
    * 
    * @param Peak
    *           The new peak
    */
   @Override
   public void SelectPeak( IPeak Peak )
   {

      currentPeak = Peak;

      if( CurrentHandler != null )
      {
         CurrentHandler.show( currentPeak , currentTransformation , viewPanel );
         validate();
         invalidate();
      }
   }


}
