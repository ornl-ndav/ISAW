
/*
 * File:  TableViewMenuComponents.java
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.2  2002/07/17 19:11:39  rmikk
 *  Added GPL
 *  Fixed up the table views menu choices
 *
 */
package DataSetTools.viewer.Table;

import javax.swing.*;
import java.awt.event.*;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
/** Can be used to set Menu components and retrieve their corresponding
*   operations
*/
public class TableViewMenuComponents  
  { boolean error, index;
    JCheckBoxMenuItem jmErr=null; 
    JCheckBoxMenuItem jmInd=null;

   public TableViewMenuComponents( )
     {error = false;
      index = false;
  
     }
  private JCheckBoxMenuItem  getErrorItem()
     {jmErr = new JCheckBoxMenuItem("Show Errors");
      jmErr.addActionListener( new MyActionListener(0));
      return jmErr;
      }
 private JCheckBoxMenuItem  getIndexItem()
     {jmInd = new JCheckBoxMenuItem("Show Indicies");
      jmInd.addActionListener( new MyActionListener(1));
      return jmInd;
      }
  private boolean getErrors()
    {return error;}

 private boolean getIndex()
    {return index;}
  /** Returns the number of menu items
  */
  public static int getNMenuItems()
    { return 3;
    }

  /** Returns the menu Name associated with the menu items
  */
   public static String getNameMenuItem( int i)
    { if( i < 0)
         return null;
      if( i == 0)
         return "Gr,Time vs Field";
      if( i == 1)
         return "Time vs Gr,Field";
      if( i == 2 )
         return "Time,Row vs Col";
      else return null;
    }

 /** Adds associated Menu items to the JMenu Tables  with the given actionlistener
 */
 public void addMenuItems( JMenu Tables , ActionListener view_menu_handler)
   { JMenuItem button;
    for( int i = 0; i < getNMenuItems(); i++) 
     {button = new JMenuItem( getNameMenuItem(i));
      button.addActionListener( view_menu_handler );
      Tables.add( button);
      }
  /*  
    Tables.addSeparator();
    Tables.add( getErrorItem());
    Tables.add( getIndexItem());
    jmErr.setState( error);
    jmInd.setState( index);
  */

   }

 /** Returns the TableView associated with the view_type that corresponds to the
 * Name associated with the Menu items
 */
 public TableView getDataSetViewer( String view_type, DataSet DS, ViewerState state)
   {
    /*table_view tv= new table_view(0);
    String state1 = "X values;Y values;";
    if( error || (view_type.indexOf("err")>=0))
      state1 += "Error values;";
    if( index || (view_type.indexOf("indx") >=0))
      state1 += "XY index;";
    state.set_String( ViewerState.TABLE_DATA, state1);
    DataSet[] DSS = new DataSet[1];
    DSS[0] = DS;
    tv.setDataSets( DSS );
    tv.restoreState( state1);
    DefaultListModel LM = tv.getListModel();
    */
   // for( int j = 0; j<LM.size();j++)
   //   System.out.println("LM+"+LM.elementAt(j));
    if( DS.getSelectedIndices().length<1)
       {DataSetTools.util.SharedData.addmsg("No data sets selected");
        return null;
       }
    if( view_type.indexOf("Gr,Time vs Field")==0)
      return new TableView( DS, state,"HGT,F");//tv.getGenTableModel( DS,LM,"HGT,F",DS.getSelectedIndices() ));
    if( view_type.indexOf("Time vs Gr,Field")==0)
       return new TableView( DS, state,"HT,GF");//tv.getGenTableModel( DS,LM,"HT,FG",DS.getSelectedIndices() ));
    if(view_type.indexOf("Time,Row vs Col")==0)
       return new TableView( DS, state,"HTI,JF");//tv.getGenTableModel( DS,LM,"HTI,JF",DS.getSelectedIndices() ));
    
    return null;

   }
 private class MyActionListener implements ActionListener
    {int action;
     public MyActionListener( int action)
       { this.action = action;}

     public void actionPerformed( ActionEvent evt )
       { 
        if( action ==0 ) // error handler
          { error = jmErr.getState();
            
           }
        else if( action == 1) //
          {index = jmInd.getState();
           
          }
  
        

        }
    }

   }
