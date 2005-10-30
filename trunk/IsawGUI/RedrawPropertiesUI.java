/*
 * File:  RedrawPropertiesUI.java
 *
 * Copyright (C) 2005, Dennis Mikkelson
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
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * $Log$
 * Revision 1.1  2005/10/30 22:32:59  dennis
 *   This class is a Runnable that rebuilds ISAW's properties table, when
 * a new DataSet or Data block is designated.  Classes that request the table
 * be redrawn from a non-Swing thread  MUST create an instance of this class
 * and use SwingUtilities.invokeLater() to place that instance of this class on
 * the Swing event queue.  In all cases, this will already be taken care of
 * by calling the JPropertiesUI.showAttributes() method, which now uses this
 * Runnable to do the actual drawing.
 *   This fixes an intermittent bug that was recently discovered on mult-processor
 * systems.  Occasionally, an invalid index would be passed to Vector.elementAt()
 * inside of the DefaultTableModel code, if a table update was triggered
 * from a non-Swing thread.  In particular, this could happen when an operator
 * was run in a non-Swing thread, using the SwingWorker class.  The operator
 * could change a DataSet and then send a message to the observers of the
 * DataSet.  Since the JPropertiesUI was an observer of the DataSet, it could
 * proceed to alter the table in the non-Swing thread, and the Swing thread
 * could be trying to draw the table at the same time (on a dual processor
 * system.)
 *
 */

package IsawGUI; 

import javax.swing.*;

import javax.swing.table.*;
import java.util.*;

import DataSetTools.dataset.*;


/**
 *  This class is a Runnable that draws updated values in the PropertiesUI
 *  table, from within the Swing thread.  This became necessary when the
 *  SwingWorker class was added to run the operators' getResult() method
 *  a different thread, than the Swing thread.  Since operators are no
 *  longer just run in the Swing thread, an operator that changes a DataSet
 *  will notify the observers of that DataSet, in a non-Swing thread.  This
 *  caused the PropertiesUI.showAttributes() method to remove rows and add
 *  new rows to the table, in a non-Swing thread.  However, on dual processor
 *  systems, the Swing thread might be drawing the table at the same time.
 *  The result was an occasional Vector.elementAt() call with an invalid
 *  index, in the DefaultTableModel code. 
 */

public class RedrawPropertiesUI implements Runnable 
{
  JTable          table;      
  IAttributeList  attr_list;

  /* ---------------------------- constructor ---------------------------- */
  /**
   *  Construct a new Runnable to update the specified table with the
   *  information from the specified AttrbiteList.
   *
   *  @param  table      The table to update
   *  @param  attr_list  The AttributeList from which we get the information
   */
  public RedrawPropertiesUI( JTable table, IAttributeList attr_list )
  {
    this.table     = table;
    this.attr_list = attr_list; 
  }


  /* --------------------------------- run -------------------------------- */
  /**
   *  Method to be invoked by the Swing thread, to actually draw the table.
   */
  public void run()
  {
    DefaultTableModel dtm = (DefaultTableModel)table.getModel();

    int n_rows = dtm.getRowCount();          // empty the table
                                             // headings counts as 1 row
    for ( int i = n_rows-1; i >= 0; i-- )
      dtm.removeRow(i);

    if ( attr_list != null )
    {
      if ( attr_list instanceof DataSet )    // show the DS title
      {
        Vector ds_name_row = new Vector(2);
        ds_name_row.addElement( "DataSet Tag:Title" );
        ds_name_row.addElement( ((DataSet)attr_list).toString() );
        dtm.addRow( ds_name_row );
      }
                                             // show the attributes
      for ( int i=0; i < attr_list.getNum_attributes(); i++ )
      {
        Attribute attr = attr_list.getAttribute(i);
        Vector row_data = new Vector(2);
        row_data.addElement(attr.getName());
        row_data.addElement(attr.getStringValue());
        dtm.addRow( row_data );
      }
    }
  }

}
