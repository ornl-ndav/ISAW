/*
* File:  InstrumentTableDataSetViewer.java
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
* Revision 1.2  2005/05/25 18:01:19  dennis
* Replaced direct call to .show() method for window,
* since .show() is deprecated in java 1.5.
* Now calls WindowShower.show() to create a runnable
* that is run from the Swing thread and sets the
* visibility of the window true.
*
* Revision 1.1  2004/08/23 21:40:35  rmikk
* Initial Checkin
*
*
*/

 
package DataSetTools.viewer;

import DataSetTools.dataset.*;
import DataSetTools.viewer.Table.DataBlockSelector;
import DataSetTools.viewer.Table.LargeJTableViewComponent;

import gov.anl.ipns.Util.Sys.WindowShower;

import javax.swing.*;

/**
 * @author rmikk
 *
 *  A DataSetViewer especially for the SelectedGraph Viewer
 */
public class InstrumentTableDataSetViewer extends DataSetViewerMaker1{
  
  
  
   /**
    * Constructor
    * @param DS    The DataSet to be viewed
    * @param state  The ViewerState
    */
   public InstrumentTableDataSetViewer( DataSet DS, ViewerState state){
     super( DS,state, new DataBlockSelector( DS,null),
           new LargeJTableViewComponent(null,null));
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
      InstrumentTableDataSetViewer view = new InstrumentTableDataSetViewer( DS, null);
      jf.getContentPane().add( view);
      jf.setJMenuBar( view.getMenuBar());
      jf.setSize( 500, 400);
      WindowShower.show(jf);
     
      }
   
   

}
