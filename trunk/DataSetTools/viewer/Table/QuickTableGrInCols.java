/*
 * File:  QuickTableGrInCols.java 
 *
 * Copyright (C) 2002, Ruth Mikkelson
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
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * 
 * $Log$
 * Revision 1.3  2002/11/27 23:25:37  pfpeterson
 * standardized header
 *
 * Revision 1.2  2002/11/18 17:31:20  rmikk
 * Added documentation and GPL
 * Added a checkbox to show all indices.
 *
 * Revision 1.1  2002/07/24 20:03:52  rmikk
 * Initial Checkin
 *
 */
package DataSetTools.viewer.Table;

import DataSetTools.dataset.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import DataSetTools.viewer.*;
import IsawGUI.*;
import java.io.*;
import javax.swing.table.*;
import DataSetTools.components.ui.*;
import DataSetTools.components.containers.*;
import DataSetTools.util.*;


/** This Class Creates a "quick interactive" table view that displays
*   channel numbers versus groups in columns. The columns can show counts,
*   with options to show errors and channel indices.
*/
public class QuickTableGrInCols  extends STableView
 {boolean showerrs, 
          showind;
  JCheckBox ShowAll;
  int[] SelectedIndices , AllIndices;

  /** Constructor for the QuickTableModel
  */
  public QuickTableGrInCols( DataSet ds, ViewerState st)
    {super( ds, st, new DS_XY_TableModel( ds,
                           ds.getSelectedIndices(), false, false));
     showerrs = false;
     showind =  false;
     SelectedIndices = ds.getSelectedIndices();
     AllIndices = new int[ ds.getNum_entries() ];
     for( int i = 0; i < ds.getNum_entries() ; i++ )
        AllIndices[i]=i;
     }


  /** Called by STableView when there is a possibility that the data is changed
  */
  public TableViewModel fixTableModel( ViewerState state , TableViewModel table_model, 
                          boolean showerrors, boolean showIndices)
    {showerrs= showerrors;
     showind = showIndices;
     SelectedIndices = ds.getSelectedIndices();
     if( ShowAll != null )
       if( ShowAll.isSelected() )
         SelectedIndices = AllIndices;
     return new DS_XY_TableModel( ds, SelectedIndices , showerrors, showIndices);
    }


  /** Changes the table model if the data is changed
  */
  public void redraw( String reason)
    {
     if( reason.equals(IObserver.POINTED_AT_CHANGED))
        super.redraw( reason);
     else if (reason.equals( IObserver.SELECTION_CHANGED) ||
             reason.equals( IObserver.DATA_REORDERED)||
             reason.equals( IObserver.DATA_DELETED) || 
             reason.equals( IObserver.DATA_CHANGED)||
             reason.equals( IObserver.GROUPS_CHANGED))
       {     
        SelectedIndices = ds.getSelectedIndices();
        if( ShowAll != null )
           if( ShowAll.isSelected() )
              SelectedIndices = AllIndices;
        table_model = new  DS_XY_TableModel( ds, SelectedIndices , showerrs, showind);
        jtb.setModel( table_model);
        jtb.repaint();
       }

    }


  /** Adds the "Show All indices Check box to the Control Panel
  */
  public void AddComponentsAboveInfo(javax.swing.JPanel EastPanel)
    {
     ShowAll = new JCheckBox( "Show All Groups" );
     ShowAll.addActionListener( new MActionListener( ) );
     EastPanel.add( ShowAll );
    }


  /** Listens for selection/deselection of the Show All indices checkbox
  */
  class MActionListener implements ActionListener
    {
     
     public void actionPerformed( ActionEvent evt )
       {
        redraw( IObserver.DATA_CHANGED );
         
       }
    }

  }
