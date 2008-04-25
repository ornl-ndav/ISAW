/* File: Histogram3D.java
 *
 * Copyright (C) 2008, Joshua Oakgrove 
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
 *  Last Modified:
 *
 *  $Author$
 *  $Date$            
 *  $Revision$
 */

package DataSetTools.viewer.DisplayDevices;

import java.awt.Color;
import java.util.Vector;

import gov.anl.ipns.DisplayDevices.*;
import gov.anl.ipns.ViewTools.Components.TwoD.Contour.ContourViewComponent;
import DataSetTools.dataset.DataSet;
import DataSetTools.dataset.FunctionTable;
import DataSetTools.dataset.UniformXScale;
import DataSetTools.retriever.RunfileRetriever;
import DataSetTools.util.SharedData;

public class DSDisplayableTest
{
  public static void main(String[]args) throws Exception
  {
    //Create data set
    //------------------------------------------------------
    
  //DataSet[] DSArray = Command.ScriptUtil.load("filename")//sample runs/ gppd1238, scd8336 for image
    //use last element in array
	SharedData.reloadProperties();
    String directory = System.getProperty("ISAW_HOME")+"/SampleRuns";
    //System.out.println(directory);
    //String file_name = directory + "/GPPD12358.RUN";
    String file_name = directory + "/SCD06496.RUN";
    RunfileRetriever rr = new RunfileRetriever( file_name );
    DataSet ds = rr.getDataSet(1);
    
    for ( int i = 0; i < 5; i++ )
      ds.setSelectFlag(  i, true );
    
    /*DataSet ds = new DataSet();    
    float[][] testData = ContourViewComponent.getTestDataArr(41,51,3,4);
    for (int i=0; i<testData.length; i++)
       ds.addData_entry(
             new FunctionTable(new UniformXScale(0, 
                               testData[i].length-1, 
                               testData[i].length),
                               testData[i], 
                               i));
    ds.setSelectFlag(5, true);
    ds.setSelectFlag(6, true);//*/
    
    //Make displayables
    //------------------------------------------------------
    Displayable disp1 = new DataSetDisplayable(ds, "Selected Graph View");
    //Displayable disp2 = new DataSetDisplayable(ds, "Scrolled Graph View");
    Displayable disp2 = new DataSetDisplayable(ds, "Image View");
    Displayable disp3 = new DataSetDisplayable(ds, "2D Viewer");
    //Displayable disp3 = new DataSetDisplayable(ds, "3D View");
    //Displayable disp3 = new DataSetDisplayable(ds, "HKL Slice View");
    
    //Set Selected Graph attributes
    //-------------------------------------------------------
    //------line 1
    Displayable.setLineAttribute(disp1, 1, "line color", "red");
    Displayable.setLineAttribute(disp1, 1, "line tYpe", "doTtEd");
    Displayable.setLineAttribute(disp1, 1, "Mark Type", "plus");
    //Displayable.setLineAttribute(disp1, 1,"transparent", "true");
    Displayable.setLineAttribute(disp1, 1, "Mark color", "cyan");
    
    //------line 2
    Displayable.setLineAttribute(disp1, 2, "line color", "black");
    Displayable.setLineAttribute(disp1, 2, "line tYpe", "dashdot");
    Displayable.setLineAttribute(disp1, 2, "Mark Type", "plus");
    Displayable.setLineAttribute(disp1, 2, "Mark color", "purple");//defaults to red 
                                                      //because "purple" DNE
    //-----line 3
    Displayable.setLineAttribute(disp1, 3, "line color", "cyan");
    Displayable.setLineAttribute(disp1, 3, "line tYpe", "dashed");
    Displayable.setLineAttribute(disp1, 3, "Line width", 4f);
    Displayable.setLineAttribute(disp1, 3, "Mark type", "cross");
    Displayable.setLineAttribute(disp1, 3, "Mark color", "orange");
    
    //-----line 4
    Displayable.setLineAttribute(disp1, 4, "line color", "magenta");
    
    //-----line 5
    Displayable.setLineAttribute(disp1, 5, "line color", "light gray");
    Displayable.setLineAttribute(disp1, 5, "mark type", "bar");
    Displayable.setLineAttribute(disp1, 5, "mark Color", "red");
    
    //------View Attributes
    Displayable.setViewAttribute(disp1, "legend", "true");
    Displayable.setViewAttribute(disp1, "grid lines x", "on");
    Displayable.setViewAttribute(disp1, "grid lines y", "off");//already off as default
    Displayable.setViewAttribute(disp1, "grid color", "blue");
    
    
    //Set Image view attributes
    //------------------------------------------------------
    //none
    
  //Set 2D Viewer attributes
    //------------------------------------------------------
    //none
    
    
    //             DEVICE SELECTION
    //_______________________________________________________________________
    //choose your device by uncommenting, then comment out the others
    
    
    /*//        Create Scrn Device
    //____________________________________________________
    GraphicsDevice gd = new ScreenDevice();
    
    //Set Scrn Device attributes
    //-----------------------------------------  
    //none//*/
    
    /*//        Create File Device
    //____________________________________________________
    String filePath = 
    "C:/Documents and Settings/student/My Documents/My Pictures/test.jpg";
    GraphicsDevice gd = new FileDevice(filePath);
    
    //Set File Device attributes
    //-----------------------------------------
    Vector<Integer> vec = new Vector<Integer>();
    vec.add(500);
    vec.add(500);
    ((FileDevice)gd).setBounds(vec);
    //*/
    
    //        Create Printer Device
    //____________________________________________________
    GraphicsDevice gd = new PrinterDevice("Adobe PDF");
    
    //Set Printer Device Attributes
    //-----------------------------------------
    //GraphicsDevice.setDeviceAttribute(gd, "orientation", "portrait");
    GraphicsDevice.setDeviceAttribute(gd, "orientation", "Landscape");
    //GraphicsDevice.setDeviceAttribute(gd, "copies", 3);
    GraphicsDevice.setDeviceAttribute(gd, "mediasize", "posterc");
    //GraphicsDevice.setDeviceAttribute(gd, "mediasize", "postera");
    //GraphicsDevice.setDeviceAttribute(gd, "mediasize", "Letter");
    //GraphicsDevice.setDeviceAttribute(gd, "mediasize", "a");
    //GraphicsDevice.setDeviceAttribute(gd, "mediasize", "Legal");//*/
    
    /*//        Create Preview Device
    //____________________________________________________
    GraphicsDevice gd = new PreviewDevice();
    
    //Set Preview Device Attributes
    //-----------------------------------------
    //none*/
    
    //Print out
    //____________________________________________________
    System.out.println("Bounds[X,Y]\n"+gd.getBounds());
    GraphicsDevice.setRegion(gd, 0, 20, 800, 750);
    GraphicsDevice.display(gd, disp1, true );
    GraphicsDevice.setRegion(gd, 800, 20, 500, 500);
    GraphicsDevice.display(gd, disp2,true);
    GraphicsDevice.setRegion(gd, 0, 770, 800, 750);
    GraphicsDevice.display(gd, disp3,true);
    GraphicsDevice.print(gd);
    
    
  }
  
}
