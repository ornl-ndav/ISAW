/*
 * File:  KeySelect
 *
 * Copyright (C) 2000, Dennis Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.3  2002/11/27 23:26:11  pfpeterson
 *  standardized header
 *
 */

package DataSetTools.viewer.util;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;

/**
 *  The KeySelect class implements the keyboard based spectrum selection
 *  policy for the ImageView and GraphView viewers.
 */   

public final class KeySelect implements Serializable
{

  /**
   *  Don't let anyone instantiate this class
   */
   private KeySelect(){}

  /**
   *  Mark Data blocks in ds as "Selected" based on the specified KeyEvent
   *  and on the Data block that is under the cursor.
   *
   *  @param  ds         The DataSet in which Data blocks are being selected.
   *  @param  new_index  The index of the Data block under the cursor.
   *  @param  e          The KeyEvent to be interpreted.
   */
  public static void ProcessKeySelection( DataSet  ds,  
                                          int      new_index, 
                                          KeyEvent e )
  {
    char  key   = e.getKeyChar();
    int   code  = e.getKeyCode();
    int   last  = ds.getMostRecentlySelectedIndex();

    int   row     = new_index;

    if ( code != KeyEvent.VK_S &&
         code != KeyEvent.VK_T &&
         code != KeyEvent.VK_D    )       // no selection operation
      return;

    if ( !e.isControlDown() && code == KeyEvent.VK_S )  // control key is used
    {                                                   // to append selections
      ds.clearSelections();
    }

    if ( !e.isShiftDown() )                         // lower case, so change
    {                                               // selection on one block
      if (code == KeyEvent.VK_S )
        ds.setSelectFlag( row, true );
      else if ( code == KeyEvent.VK_T )
        ds.toggleSelectFlag( row );
      else
        ds.setSelectFlag( row, false );
    }

    else                                           // change select on range
      {                                            // of Data blocks
        if ( last == DataSet.INVALID_INDEX )       // just select one Data block
        {
          if ( code == KeyEvent.VK_S )
            ds.setSelectFlag( row, true );
          else if ( code == KeyEvent.VK_T )
            ds.toggleSelectFlag( row );
          else
            ds.setSelectFlag( row, false );
        }
        else
        {
          if ( last <= row )                           // this "if" is needed to
            for ( int i = last; i <= row; i++ )        // avoid doing i=last
              if ( code == KeyEvent.VK_S )             // twice
                ds.setSelectFlag( i, true );
              else if ( code == KeyEvent.VK_T )
                ds.toggleSelectFlag( i );
              else
                ds.setSelectFlag( i, false );

          else
            for ( int i = row; i <= last; i++ )
              if ( code == KeyEvent.VK_S )
                ds.setSelectFlag( i, true );
              else if ( code == KeyEvent.VK_T )
                ds.toggleSelectFlag( i );
              else
                ds.setSelectFlag( i, false );

           ds.toggleSelectFlag( row );      // make current row the
           ds.toggleSelectFlag( row );      // most recently selected
                                                      // if it was selected
        }
      }
      ds.notifyIObservers( IObserver.SELECTION_CHANGED );
  }
} 
