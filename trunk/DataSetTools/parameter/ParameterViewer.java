 /*
 * File:  ParameterViewer.java
 *
 * Copyright (C) 2003, Christopher M. Bouzek
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
 *           Chris Bouzek <coldfusion78@yahoo.com>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intendse Pulsed Neutron Source
 * Division of Argonne National Laboratory and the National Science
 * Foundation.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2003/06/02 22:13:23  bouzekc
 * Added code to view ASCII files.
 * Fixed inconsistent indenting.
 *
 * Revision 1.1  2003/03/19 14:59:38  pfpeterson
 * Added to CVS. (Chris Bouzek)
 *
 */
 
package DataSetTools.parameter; 
 
import java.util.*; 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import DataSetTools.parameter.*;
import DataSetTools.viewer.*;
import DataSetTools.dataset.DataSet;
import DataSetTools.operator.Generic.Special.ViewASCII;
 
/**
  *  Class for viewing IParameterGUIs.  Originally designed for the 
  *  Wizards, so that DataSets and other parameters could be viewed 
  *  after operations were performed.
  */
public class ParameterViewer implements ActionListener
{
  private IParameterGUI param;
  private JFrame holder;
  private JList selector;
  private Container c;
  private JButton view_b, cancel_b;
  private JScrollPane jsp;
  private Vector items;
  private JPanel button_p;
  private String[] item_names;
  
  private final String VIEW = "View";
  private final String CANCEL = "Cancel";
  
  public ParameterViewer(IParameterGUI ipg)
  {
    param = ipg;
  }
 
 
  /**
   *
   * Shows the parameters according to a preferred viewing state.
   * For example, a DataSet will be shown in a ViewManager, but a
   * number (such as a Float) is simply displayed in a message
   * box.  For parameters which can be selected from a list, 
   * such as an ArrayPG of DataSets, only one can be selected at a 
   * time.
   */
  public void showParameterViewer() 
  {
    Object obj;
    Vector v;
    //ViewManager vm;     

    if( param != null )
    {
      obj = param.getValue();

      //is it a DataSet?
      if( obj instanceof DataSet )
        new ViewManager((DataSet)obj,IViewManager.IMAGE);

      //is it a Vector?
      else if( obj instanceof Vector )
      {    
        v = (Vector)obj;
        if( v.size() == 1 )
        {
          obj = v.elementAt(0);
          if( obj instanceof DataSet )
            new ViewManager((DataSet)obj,IViewManager.IMAGE);
        }  
        else
        {
          //display a list so the user can choose the DataSet to view
          //basic setup for selection list
          int num_items = v.size();
          item_names = new String[num_items]; 
          items = new Vector(num_items);  

          for( int i = 0; i < num_items; i++ )
          {
            obj = v.elementAt(i);
            item_names[i] = obj.toString();
            items.add(obj);
          }
           
          this.makeSelectionList();
        }
        
      }
      else if(obj instanceof String)
      {
        String fileName = (String)obj, tempName;
        //try to determine if it is a viewable file

        //is this a file?
        if(fileName.indexOf('.') > 0)
        {
          //I think in lowercase...gives me a headache to shout
          tempName = fileName.toLowerCase();
          
          //ASCII files
          if((tempName.indexOf(".peaks") >= 0) || 
             (tempName.indexOf(".mat") >= 0 ))
          {
            new ViewASCII(fileName).getResult();  //look at the peaks file
          }

        }  
      }
      else
      {
        //handle some other IParameterGUI types
        JOptionPane.showMessageDialog(null, obj.toString());
      }

    }
  }
   
  /**
   *  Note that this relies mainly on what is in the String
   *  array item_names in order to create the list
   */
  private void makeSelectionList()
  {        
    if( item_names != null )
    {
      //create the selection list 
      holder = new JFrame();  
      holder.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      c = holder.getContentPane();       
      holder.setSize(640,480);
      selector = new JList(item_names); 
      selector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      selector.setPreferredSize(new Dimension(320,240));
      jsp = new JScrollPane(selector);
      jsp.setPreferredSize(new Dimension(320,240));
          
      view_b = new JButton(VIEW);
      cancel_b = new JButton(CANCEL);
      view_b.addActionListener(this);
      cancel_b.addActionListener(this);
      view_b.setPreferredSize(new Dimension(80,40));
      cancel_b.setPreferredSize(new Dimension(80,40));
         
      c.setLayout(new BorderLayout());
      c.add(jsp, BorderLayout.CENTER);
      button_p = new JPanel(new GridLayout());
      button_p.add(view_b);
      button_p.add(cancel_b);
      c.add(button_p, BorderLayout.SOUTH);
         
      holder.show();
    }
  }
   
  public void actionPerformed(ActionEvent e)
  {
    int index;
    Object obj;
   
    if( e.getActionCommand().equals(CANCEL) )
      holder.dispose();
    else if( e.getActionCommand().equals(VIEW) )
    {
      index = selector.getSelectedIndex();
              
      if( index >= 0 && items != null && items.size() > 0)
      {
        obj = items.elementAt(index);
        //DataSet?
        if( obj instanceof DataSet )
          new ViewManager((DataSet)obj,IViewManager.IMAGE);
      }
    }  
  }
}
