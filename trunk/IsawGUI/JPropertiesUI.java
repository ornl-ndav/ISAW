/*
 * $Id$
 *
 * $Log$
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
 * Revision 1.9  2001/08/02 19:49:24  neffk
 * now traps IObserver.DATA_DELETED message in update().  the message
 * displays an empty AttributeList object, which avoids the potential
 * problem of DataSet objects pointing at Data objects that don't exist.
 * instead of depending on the DataSet class, we just show nothing
 * until something else has been pointed to.
 *
 * Revision 1.8  2001/07/31 19:31:12  neffk
 * ignores new DataSet objects that are sent as 'reason' via the
 * update/IObservable menchanism.
 *
 * Revision 1.7  2001/07/25 17:30:58  neffk
 * added a debug println and cleaned up some poor indentation.
 *
 * Revision 1.6  2001/07/11 16:47:04  neffk
 * added keywords for substitution.  some other seemingly harmless
 * changes have been made, hopefully only in the formatting of the code.
 * i have no recollection of ever even looking at this file, so the
 * changes may have been inadvertent.
 *
 */
 
package IsawGUI;

import DataSetTools.dataset.*;
import DataSetTools.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import javax.swing.table.*;
import java.util.*;
import DataSetTools.operator.*;
import DataSetTools.instruments.*;
import java.util.zip.*; 
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
    DefaultTableModel dtm = new DefaultTableModel();
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
   * draws the table of Attribute objects to be shown.
   */
  public void showAttributes( AttributeList attr_list )
  {
    Vector heading = new Vector();
    heading.addElement("Attribute" ); 
    heading.addElement("Value");
    Vector data = new Vector();
    for (int i=0; i<attr_list.getNum_attributes(); i++)
    {
      Attribute attr = attr_list.getAttribute(i);
        
      Vector oo = new Vector();
      oo.addElement(attr.getName()); 
      oo.addElement(attr.getStringValue());
      data.addElement(oo);
    }
    DefaultTableModel dtm = new DefaultTableModel(data, heading);
    table.setModel(dtm);

                                 // the numbers used don't seem to
                                 // be important, but setting the 
                                 // size get's the table to fill out
    table.setSize( 30, 30 );     // the available space.

    ExcelAdapter myAd = new ExcelAdapter(table);
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
        data_shown = null;

      else if( reason_str.equals(SELECTION_CHANGED) )
      {
        int index = ds.getMostRecentlySelectedIndex();
        if(index>=0)
        {
          Data d = ds.getData_entry(index);
          if( d.isMostRecentlySelected() )
            showAttributes(d.getAttributeList());
          data_shown = d;
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
          data_shown = d;
        }
      }

      else if( reason_str.equals(DATA_DELETED) )
      {
        showAttributes(new AttributeList());
        data_shown = null;
      }

      else
      {
        showAttributes(ds.getAttributeList());
        data_shown = null;
      }
 
      return; 
    }     
  }
}
