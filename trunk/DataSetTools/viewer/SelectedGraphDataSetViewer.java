/*
* File:  SelectedGraphDataSetViewer.java
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
* Revision 1.1  2004/08/23 20:35:37  rmikk
* Initial Checkin
*
*/

 
package DataSetTools.viewer;

import gov.anl.ipns.ViewTools.Components.OneD.FunctionViewComponent;
import gov.anl.ipns.ViewTools.Components.OneD.VirtualArrayList1D;
import DataSetTools.components.View.DataSetData;
import DataSetTools.dataset.*;
import javax.swing.*;
import java.awt.*;
/**
 * @author rmikk
 *
 *  A DataSetViewer especially for the SelectedGraph Viewer
 */
public class SelectedGraphDataSetViewer extends DataSetViewer{
  
  
   DataSetViewerMaker viewer;
   /**
    * Constructor
    * @param DS    The DataSet to be viewed
    * @param state  The ViewerState
    */
   public SelectedGraphDataSetViewer( DataSet DS, ViewerState state){
     super( DS, state);
      VirtualArrayList1D varray = DataSetData.convertToVirtualArray(
            DS );
       FunctionViewComponent viewComp = new FunctionViewComponent(varray);
       viewer = new DataSetViewerMaker(DS, state, varray, viewComp);
       this.setLayout( new GridLayout(1,1));
       add( viewer);
   }
   
   public void setDataSet(DataSet ds){
      viewer.setDataSet(ds);
   }
   
   public DataSet getDataSet(){
       return viewer.getDataSet();
   }
   public boolean validDataSet(){
      return viewer.validDataSet();
   }
   public ViewerState getState(){
      return viewer.getState();
   }
   public void redraw(String reason){
      viewer.redraw(reason);
   }
   public XScale getXConversionScale(){
       return viewer.getXConversionScale();
   }
   public javax.swing.JMenuBar getMenuBar(){
      return viewer.getMenuBar();
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
      SelectedGraphDataSetViewer view = new SelectedGraphDataSetViewer( DS, null);
      jf.getContentPane().add( view);
      jf.setJMenuBar( view.getMenuBar());
      jf.setSize( 500, 400);
      jf.show();
     
      }
   
   

}
