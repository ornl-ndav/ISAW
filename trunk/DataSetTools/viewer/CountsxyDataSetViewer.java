/*
* File:  CountsxyDataSetViewer.java
*
* Copyright (C) 2004, Ruth Mikkelson
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
* Contact :  Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
* Revision 1.1  2004/08/23 20:49:03  rmikk
* Initial Checkin
*
*/

 
package DataSetTools.viewer;




import DataSetTools.dataset.*;
import DataSetTools.viewer.Table.LargeJTableViewComponent;
import DataSetTools.viewer.Table.RowColTimeVirtualArray;

import javax.swing.*;

/**
 * @author rmikk
 *
 *  A DataSetViewer especially for the SelectedGraph Viewer
 */
public class CountsxyDataSetViewer extends DataSetViewerMaker1{
  
  
  
   /**
    * Constructor
    * @param DS    The DataSet to be viewed
    * @param state  The ViewerState
    */
   public CountsxyDataSetViewer( DataSet DS, ViewerState state){
     super( DS, state, new RowColTimeVirtualArray( DS, 
           DS.getData_entry(0).getX_scale().getStart_x(),
           false, false, state),
           new LargeJTableViewComponent(state, new dummyIVirtualArray2D()));
   }
         public static void main( String[] args){
       DataSet[] DSS = null;
       try{
         DSS = Command.ScriptUtil.load( args[0]);
       }catch( Exception ss){
          System.exit(0);
       }
      DataSet DS = DSS[DSS.length-1];
      JFrame jf = new JFrame( DS.getTitle());
      CountsxyDataSetViewer view = new CountsxyDataSetViewer( DS, null);
      jf.getContentPane().add( view);
      jf.setJMenuBar( view.getMenuBar());
      jf.setSize( 500, 400);
      jf.show();
     
      }
   
   

}
