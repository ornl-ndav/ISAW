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
 *  Revision 1.24  2004/08/24 18:51:32  rmikk
 *  Reorder menu options
 *  Caught errors on viewer initialization and returned null
 *
 *  Revision 1.23  2004/07/29 14:33:13  rmikk
 *  Changed Proffen View to Slice Viewer
 *
 *  Revision 1.22  2004/07/28 18:35:38  rmikk
 *  Replaced one of the experimental Q views by the Proffen View
 *
 *  Revision 1.21  2004/07/28 18:29:33  rmikk
 *  Prints a Stack trace if the initialization of a viewer causes an exception
 *
 *  Revision 1.20  2004/01/24 22:41:15  bouzekc
 *  Removed/commented out unused imports/variables.
 *
 *  Revision 1.19  2003/12/30 13:42:49  rmikk
 *  Changed the names for the three interactive table views.
 *
 *  Revision 1.18  2003/12/11 19:29:22  rmikk
 *  Change Tom's view to Instrument Table and decreased the
 *    portion of the view assigned to the controls
 *
 *  Revision 1.17  2003/12/04 20:48:44  rmikk
 *  Added "Tom's View" to the Selected Table View Submenu
 *
 *  Revision 1.16  2003/11/06 20:03:19  rmikk
 *  Catches and handles the exception thrown by
 *    row-col time Array  maker
 *
 *  Revision 1.15  2003/10/27 15:13:49  rmikk
 *  Now uses the new Table views- with format specifiers and cursor keys
 *
 *  Revision 1.14  2003/08/13 23:48:44  dennis
 *  Changed number of menu items from 8 to 7.  This fixes a bug I
 *  introduced when I moved the "Brent" view from the table submenu
 *  to the main view menu.
 *
 *  Revision 1.13  2003/08/08 17:56:55  dennis
 *  Removed "Brent" view, which had been added for testing purposes.
 *
 *  Revision 1.12  2003/06/06 19:07:23  rmikk
 *  Added Brent's viewer to the TableMenu list
 *
 *  Revision 1.11  2003/05/19 15:22:15  rmikk
 *  -Added ContourView:Qxyz slices option to the Table
 *      view menu
 *
 *  Revision 1.10  2003/05/12 16:05:00  rmikk
 *  Included the Contour Qx,Qy,Qz, Contour Qx,Qz,Qy, etc
 *     as interactive views( Unfortunately only as submenus
 *    of the Selected Table View).  These are experimental.
 *
 *  Revision 1.9  2002/11/27 23:25:37  pfpeterson
 *  standardized header
 *
 *  Revision 1.8  2002/10/07 14:47:11  rmikk
 *  Uses the specialized DataSet viewer,QuickGrIncols,
 *     that uses the  DS_XY_TableModel.
 *
 *  Revision 1.7  2002/08/21 15:46:55  rmikk
 *   If there are no selected Groups the pointed At group or if none,
 *   group 0 is selected
 *
 *  Revision 1.6  2002/07/26 22:01:51  rmikk
 *  Replaced the null for the state in the TimeSliceView with
 *    the state variable.
 *
 *  Revision 1.5  2002/07/25 21:00:06  rmikk
 *  The Specialized TimeSlice Table view no longer needs
 *     selected Groups
 *
 *  Revision 1.4  2002/07/24 19:59:25  rmikk
 *  Changed the choice time,row, vs col to call the specialized
 *     viewer-TimeSliceView
 *
 *  Revision 1.3  2002/07/17 19:58:25  rmikk
 *  Change the Menu Item names for the different types of
 *     tables
 *
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
import DataSetTools.viewer.Contour.*;

/** Can be used to set Menu components and retrieve their corresponding
*   operations
*/
public class TableViewMenuComponents  
  { boolean error, index;
    JCheckBoxMenuItem jmErr=null; 
    JCheckBoxMenuItem jmInd=null;
    public static final String TOMS_VIEW = "Instrument Table";
    public static final String GRX_Y="Serial y(x)";//"Group x vs y"old
    public static final String X_GrY ="Parallel y(x)"; // old: x vs Group y
    public static final String xR_Cy="Counts(x,y)"; //old:x,Row vs Col y
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
    {
      return 8;
    }

  /** Returns the menu Name associated with the menu items
  */
   public static String getNameMenuItem( int i)
    { if( i < 0)
         return null;
      if( i == 0)
         return "GRX_Y";
      if( i == 1)
         return "Parallel y(x)";
      if( i == 2 )
         return "Counts(x,y)";
      if( i == 3)
         return "Slice Viewer";
      if( i==4)
         return TOMS_VIEW;
      if( i == 6)
         return "Contour:Qx,Qy vs Qz";
      if( i == 5)
         return "Contour:Qy,Qz vs Qx";
      if( i == 7 )
         return "Contour:Qxyz slices";
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
 public DataSetViewer getDataSetViewer( String view_type, DataSet DS, ViewerState state)
   { try{                    
     if(view_type.indexOf("Counts(x,y)")==0)
       try{
          
           return new DataSetViewerMaker1(DS, state,
                   new RowColTimeVirtualArray( DS, 
                                DS.getData_entry(0).getX_scale().getStart_x(),
                               false, false, state),
                   new LargeJTableViewComponent(state, new dummyIVirtualArray2D()));
                   
       }catch( Exception ss){
          DataSetTools.util.SharedData.addmsg( "Cannot create Counts(x,y) :"+ss);
          ss.printStackTrace();
          return null;
       }

    if( view_type == TOMS_VIEW){
        DataSetViewerMaker1 dsv=new DataSetViewerMaker1(DS,state, new DataBlockSelector( DS,null),
                     new LargeJTableViewComponent(null,
                null));
        
         
         dsv.ImagePortion = .90f;
         return dsv;
    }
       //return (DataSetViewer)(new TimeSliceView( DS, state));
    if( DS.getSelectedIndices().length<1)
       {//DataSetTools.util.SharedData.addmsg("No data sets selected");
        if( DS == null)
           return null;
        if( DS.getNum_entries() <=0)
           return null;
        int indx = DS.getPointedAtIndex();
        if( indx == DataSet.INVALID_INDEX)
          indx = 0;
        DS.setSelectFlag(indx, true);
       }
    if( view_type.indexOf("GRX_Y")==0)
      return new TableView( DS, state,"HGT,F");//tv.getGenTableModel( DS,LM,"HGT,F",DS.getSelectedIndices() ));
    if( view_type.indexOf("Slice Viewer")==0)
       return new ProffenViewController( DS, state);
    if( view_type.indexOf("Parallel y(x)")==0){
       TimeVersusGroupValues ArrayMaker = new TimeVersusGroupValues( DS,  
                              DS.getSelectedIndices(), false, false, state);
       LargeJTableViewComponent ViewComp =new LargeJTableViewComponent(state, new dummyIVirtualArray2D());
       return new DataSetViewerMaker1(DS, state, ArrayMaker,ViewComp); 
     }  
      //return new QuickTableGrInCols( DS, state );
       //return new TableView( DS, state,"HT,GF");//tv.getGenTableModel( DS,LM,"HT,FG",DS.getSelectedIndices() ));
   
       //return new TableView( DS, state,"HTI,JF");//tv.getGenTableModel( DS,LM,"HTI,JF",DS.getSelectedIndices() ));
     QxQyQzAxesHandler Qax = new QxQyQzAxesHandler(DS);

       if( view_type.indexOf("Contour:Qx,Qy vs Qz")==0)
         return new ContourView( DS, state, Qax.getQxAxis(), Qax.getQyAxis(), Qax.getQzAxis());
      if( view_type.indexOf("Contour:Qx,Qz vs Qy")==0)
	return new ContourView( DS, state, Qax.getQxAxis(), Qax.getQzAxis(), Qax.getQyAxis());
      if( view_type.indexOf("Contour:Qy,Qz vs Qx")==0)
	   return new ContourView( DS, state, Qax.getQyAxis(), Qax.getQzAxis(), Qax.getQxAxis());

      if( view_type.indexOf("Contour:Qxyz slices") == 0)
         return new TQxQyQz( DS, state);
   }catch(Throwable ss){
      DataSetTools.util.SharedData.addmsg( "Cannot create Viewer");
   }
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
