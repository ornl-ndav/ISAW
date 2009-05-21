/* 
 * File: DisableCompListener.java
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


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JTextField;


/**
 * An ActionListener that can cause other components to be disabled or enabled
 * depending on the state of the object with this listener added
 */
public class DisableCompListener extends Object implements ActionListener
{



   boolean      IsLess;

   boolean      IsEqual;

   float        Val;

   String       SVal;

   JComponent[] Disable;

   boolean      Convert2Num;


   /**
    * Constructor for JTextField sources
    * 
    * @param isLess
    *           disable if evt source is Less than( if isLess true) val
    *           otherwise disable if more
    * @param isEqual
    *           disable if evt source is equal to val(if isEqual is true)
    * @param val
    *           The value to compare to
    * @param disable
    *           The list of components to disable if comparison suggests
    *           diabling
    */
   public DisableCompListener( boolean isLess, boolean isEqual, float val,
            JComponent[] disable )
   {

      IsLess = isLess;
      IsEqual = isEqual;
      Val = val;
      Disable = disable;
      Convert2Num = true;
      SVal = null;
   }


   /**
    * Constructor for JTextField sources
    * 
    * @param isLess
    *           disable if evt source is Less than( if isLess true) val
    *           otherwise disable if more
    * @param isEqual
    *           disable if evt source is equal to val(if isEqual is true)
    * @param val
    *           The value to compare to
    * @param disable
    *           The list of components to disable if comparison suggests
    *           diabling
    */
   public DisableCompListener( boolean isLess, boolean isEqual, String val,
            JComponent[] disable )
   {

      IsLess = isLess;
      IsEqual = isEqual;
      SVal = val;
      Disable = disable;
      Convert2Num = true;
      Val = Float.NaN;
   }


   /**
    * Constructor for AbstractButton sources
    * 
    * @param isSelected
    *           disable if evt source is equal to val(if isEqual is true)
    * @param disable
    *           The list of components to disable if comparison suggests
    *           diabling
    */
   public DisableCompListener( boolean isSelected, JComponent[] disable )
   {


      IsEqual = isSelected;
      SVal = null;
      Disable = disable;
      Convert2Num = false;
      Val = Float.NaN;
   }


   public void actionPerformed( ActionEvent e )
   {

      boolean disable = false;

      if( e.getSource() instanceof AbstractButton )
      {
         if( ( (AbstractButton) e.getSource() ).isSelected() == IsEqual )
            disable = true;

      }
      else if( e.getSource() instanceof JTextField )
      {
         String S = ( (JTextField) e.getSource() ).getText();

         int compareCode;
         if( Convert2Num )
            try
            {
               compareCode = getCode( Float.parseFloat( S.trim() ) , Val );
            }
            catch( Exception s )
            {
               compareCode = 3;
            }
         else
            compareCode = getCode( S , SVal );

         if( compareCode == 3 )
            return;

         if( compareCode == 0 && IsEqual )

            disable = true;

         else if( ( compareCode < 0 ) == IsLess )

            disable = true;


      }
      else
         return;// Nonsense

      for( int i = 0 ; i < Disable.length ; i++ )
         if( Disable[i] != null)
            Disable[ i ].setEnabled( ! disable );

   }


   private int getCode( float val1 , float val )
   {

      if( Float.isNaN( val1 ) || Float.isNaN( val ) )
         return 3;

      if( val1 < val )
         return - 1;

      else if( val1 > val )
         return 1;

      return 0;
   }


   private int getCode( String val1 , String Sval )
   {

      if( val1 == null || Sval == null )
         return 3;

      return val1.compareTo( Sval );
   }

}
