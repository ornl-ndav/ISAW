/*
 * File: JPropertiesUI.java
 *
 * Copyright (C) 1999, Alok Chatterjee
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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.16  2004/03/15 03:31:25  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.15  2004/01/24 23:09:38  bouzekc
 * Removed unused imports.
 *
 * Revision 1.14  2003/12/09 23:19:15  dennis
 * The publicly accessible showAttributes() method now resets the
 * reference to the last Data whose attributes were shown.  This
 * fixes the problem that pointing to a Data block, then a DataSet
 * then the same Data block in the tree, did not redisplay the
 * attributes of the Data block.
 *
 * Revision 1.13  2003/08/06 19:35:07  dennis
 * When new attributes are to be displayed, this now empties
 * the current table, then adds the new rows of info, instead
 * of creating a new table.  The column divider can now be
 * adjusted to allow more room for the attribute values,
 * and it does not instantly reset to the middle when new
 * attributes are displayed.
 *
 * Revision 1.12  2002/11/27 23:27:07  pfpeterson
 * standardized header
 *
 * Revision 1.11  2002/07/24 15:02:46  dennis
 * Now keeps track of the last Data block whose attributes were shown
 * and does not regenerate the properties display if it is not changed.
 * This was necessary to prevent excessive updates when POINTED_AT_CHANGED
 * messages are generated in a viewer using an AnimationController.
 *
 * Revision 1.10  2002/04/05 15:27:17  dennis
 * Removed printing of error message when an update() message is received
 * that the JPropertiesUI does not respond to.  It is not an error to
 * not handle some message.
 *
 */
 
package IsawGUI;

import DataSetTools.dataset.*;
import DataSetTools.util.*;
import gov.anl.ipns.Util.Messaging.*;

import java.awt.*;
import javax.swing.*;

import javax.swing.table.*;
import java.util.*;
import java.io.Serializable;
import javax.swing.border.*;

/**
 *  PropertiesUI implements the IObserver interface
 *  @version 1.0
 */

public class JPropertiesUI extends  JPanel implements IObserver, Serializable
{
  private JTable  table;
  private Data    data_shown = null;

  /**
   *
   */ 
  public JPropertiesUI()
  {
    Vector heading = new Vector();
    heading.addElement("Attribute" ); 
    heading.addElement("Value");

    Vector data = new Vector();

    DefaultTableModel dtm = new DefaultTableModel( data, heading );

    table = new JTable(dtm);

    setLayout(new GridLayout(1,1) );
    JScrollPane scrollPane = new JScrollPane(table);
    add( scrollPane );
    setBorder(  
      new CompoundBorder( 
        new EmptyBorder( 4, 4, 4, 4 ), 
        new EtchedBorder( EtchedBorder.RAISED ) 
      )
    );

    showAttributes(  new AttributeList() );
  }  
 

  /**
   *
   */ 
  public JTable getPropsTable()
  {
    return table;
  }


  /**
   *  Displays the specified list of Attributes in the properties panel.
   *  
   *  @param attr_list  The list of attributes to be displayed.
   *  
   */
  public void showAttributes( AttributeList attr_list )
  {
    DefaultTableModel dtm = (DefaultTableModel)table.getModel();

    int n_rows = dtm.getRowCount();          // empty the table
                                             // headings counts as 1 row
    for ( int i = n_rows-1; i >= 0; i-- )
      dtm.removeRow(i);

    for (int i=0; i<attr_list.getNum_attributes(); i++)
    {
      Attribute attr = attr_list.getAttribute(i);
      Vector row_data = new Vector();
      row_data.addElement(attr.getName()); 
      row_data.addElement(attr.getStringValue());
      dtm.addRow( row_data );
    }

    ExcelAdapter myAd = new ExcelAdapter(table);

    data_shown = null;  // Reset the data_shown variable.  If this is called
                        // from outside this class, to display some arbitrary
                        // attribute list, we won't have a valid reference to
                        // the data block displayed, so reset it.  If called
                        // from inside this class, to display a Data block,
                        // the caller should record a reference to the Data
                        // block, in data_shown, so that redundant calls to
                        // show the same Data attributes don't waste time.
  }


  /**
   *  Update the JPropertiesUI due to a change in the DataSet.  This method
   *  should be called by the DataSet's notification method, when the DataSet
   *  is changed. 
   *
   *  @param  observed  If all is well, this will be a reference to the 
   *                    DataSet that is being managed.
   *  @param  reason    Object telling the nature of the change and/or a
   *                    command.  The valid reasons are listed in the interface
   *                    IObserver
   *
   *  @see IObserver                     
   */
  public void update( Object observed, Object reason )
  {
                                       //if a new DataSet is generated,
                                       //this object can just ignore it
    if(  reason instanceof DataSet )
      return;

    if( observed instanceof DataSet  &&  reason instanceof String )
    {
      String reason_str = (String)reason;
      DataSet ds = (DataSet)observed;

      if( reason_str.equals(DESTROY) )
      {
        showAttributes(new AttributeList());
      }

      else if( reason_str.equals(SELECTION_CHANGED) )
      {
        int index = ds.getMostRecentlySelectedIndex();
        if(index>=0)
        {
          Data d = ds.getData_entry(index);
          if( d.isMostRecentlySelected() )
            showAttributes(d.getAttributeList());
          data_shown = d;                         // keep track of what was
                                                  // shown, so we don't show
                                                  // it twice
        }                             
      }

      else if( reason_str.equals(POINTED_AT_CHANGED) )
      {
        int index = ds.getPointedAtIndex() ;
        if(index>=0)
        {
          Data d = ds.getData_entry( index );
          if ( d != data_shown )
            showAttributes(  d.getAttributeList()  );
          data_shown = d;                        // keep track of what was
                                                 // shown, so we don't show
                                                 // it twice
        }
      }

      else if( reason_str.equals(DATA_DELETED) )
      {
        showAttributes(new AttributeList());
      }

      else
      {
        showAttributes(ds.getAttributeList());
      }
 
      return; 
    }     
  }
}
