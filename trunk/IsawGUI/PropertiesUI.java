/*
 * @(#)PropertiesUI.java   1.0  2000/03/14  Alok Chatterjee
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

public class PropertiesUI extends  JPanel implements IObserver, Serializable
{
	private   JTable table;
	private DataSet ds;

	 public PropertiesUI()
	 {

           	DefaultTableModel dtm = new DefaultTableModel();
           	table = new JTable(dtm);
          // table.setPreferredSize(new Dimension(50,50));
          //  table.setMinimumSize(new Dimension(50,50));
           	setLayout(new GridLayout(1,1) );
           	JScrollPane scrollPane = new JScrollPane(table);
           	add( scrollPane );
	//	setBorder(new BevelBorder (BevelBorder.LOWERED));   
						setBorder(new CompoundBorder(new EmptyBorder(4,4,4,4), new EtchedBorder (EtchedBorder.RAISED)));

           	showAttributes(  new AttributeList() );
	 }  
	 
	 
	 public void showAttributes( AttributeList attr_list)
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
             table.setSize( 30, 30 );     // the numbers used don't seem to
                                          // be important, but setting the 
                                          // size get's the table to fill out
                                          // the available space.
	 }

 /**
    *  Update the PropertiesUI due to a change in the DataSet.  This method
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
     if ( !( reason instanceof String) )   // currently we only allow Strings
     {
      // System.out.println("Error: ViewManager update called with wrong reason");
       return;
     }
 
     if ( observed instanceof DataSet )             
     {

		// System.out.println("Inside update in PropertiesUI");
		DataSet ds = (DataSet)observed;
		showAttributes(ds.getAttributeList());
      	// System.out.println("Error: ViewManager update called with wrong DataSet");
      	if ( (String)reason == DESTROY )
       			System.out.println("ERROR: Destroy in PropertiesUI:" + reason );
    
		else if ( (String)reason == SELECTION_CHANGED )
			{
				int index = ds.getMostRecentlySelectedIndex();
					if(index>=0)
					{
						Data d = ds.getData_entry(index);
						if(d.isMostRecentlySelected())					
							showAttributes(d.getAttributeList());
					}

			}
		else if ( (String)reason == POINTED_AT_CHANGED )
			{
				//	int index = ds.getMostRecentlySelectedIndex();
					int index = ds.getPointedAtIndex() ;
					if(index>=0)
					{
						Data d = ds.getData_entry(index);
					//	if(d.isMostRecentlySelected())	
						  showAttributes(d.getAttributeList());
					}
			}
		
     			else
     			{
       			// System.out.println("ERROR: Unsupported Tree Update:" + reason );
       		}

      		return; 
     }     

     
   }


}