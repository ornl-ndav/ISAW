/* 
 * File: DataSetDisplayable.java 
 *  
 * Copyright (C) 2007     Dennis Mikkelson
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
 * Contact :  Dennis Mikkelson<mikkelsond@uwstout.edu>
 *            MSCS Department
 *            HH237H
 *            Menomonie, WI. 54751
 *            (715)-232-2291
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0426797, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.7  2007/07/17 16:40:53  oakgrovej
 * deleted meaningless import
 *
 * Revision 1.6  2007/07/17 16:31:14  oakgrovej
 * *** empty log message ***
 *
 * Revision 1.5  2007/07/17 16:20:19  oakgrovej
 * setViewAttribute & setLineAttribute working along with a set of Hashtables to interpret choices.
 *
 * Revision 1.4  2007/07/16 14:53:39  dennis
 * The test method, main, now uses the new form of the display method
 * that includes the "with_controls" parameter.
 *
 * Revision 1.3  2007/07/13 14:23:03  dennis
 * Removed separate reference to the DataSet for this Displayable.
 * The viewer has a reference to the DataSet, so a separate
 * reference is not needed.
 *
 * Revision 1.2  2007/07/12 22:14:44  dennis
 * The getJComponent(with_controls) method now uses the with_controls
 * parameter to determine whether the full root pane with controls
 * is returned, or if the DataSetViewer getDisplayComponent() method
 * is used to just the the display component without controls.
 *
 * Revision 1.1  2007/07/12 19:28:03  dennis
 * Initial version of DataSetDisplayable.
 * Does not yet implement attribute setting, and does not
 * yet use the with_controls parameter to determine whether
 * or not to include the viewer controls.
 *
 */

package DataSetTools.viewer.DisplayDevices;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.retriever.*;
import gov.anl.ipns.DisplayDevices.*;
import gov.anl.ipns.ViewTools.Components.ObjectState;
import gov.anl.ipns.ViewTools.Components.TwoD.Contour.ContourViewComponent;
import gov.anl.ipns.ViewTools.Panels.Graph.GraphJPanel;

/**
 *  This class configures a view of a DataSet and produces a JComponent that
 *  can be printed, saved to a file or displayed on the screen by a specific 
 *  GraphicsDevice.
 */
public class DataSetDisplayable extends Displayable
{
  private ViewManager viewManager;
  private ObjectState Ostate;

 /**
  *  Construct an IDisplayable object to handle the specified DataSet
  *  and ViewType.
  *
  *  @param  ds        The DataSet to be displayed
  *  @param  view_type The type of DataSet viewer to use for the display
  */
  public DataSetDisplayable( DataSet ds, String view_type )
  {
    viewManager = new ViewManager( ds, view_type, false );
    Ostate = viewManager.getObjectState(true);
    System.out.println(Ostate);
  }


 /**
  *  This method returns a JComponent that can be displayed in a Frame,
  *  printed, or saved to a file.
  *
  *  @param  with_controls   If this is false, any interactive controls
  *                          associated with the view of the data will
  *                          NOT be visible on the JComponent
  *
  *  @return A reference to a JComponent containing the configured 
  *          display.
  */
  public JComponent getJComponent( boolean with_controls )
  {
    JComponent component = null;

    if ( with_controls )
      component = (JComponent)viewManager.getComponent(0); 
    else
    {
      DataSetViewer viewer = viewManager.getViewer();
      component = viewer.getDisplayComponent(); 
    }

    return component;
  }


 /**
  *  This method sets an attribute of the displayable that pertains
  *  to the overall display, such as a background color.
  *
  *  @param  name     The name of the attribute being set.
  *  @param  value    The value to use for the attribute.
  */
  public void setViewAttribute( String name , Object value) throws Exception
  {
    if( value instanceof String )
    {
      setViewAttribute( name, (String) value);
      return;
    }
    if(viewManager.getView().equals(ViewManager.SCROLLED_GRAPHS))
    {
      
    }
    else if(viewManager.getView().equals(ViewManager.SELECTED_GRAPHS))
    {
      
    }
    else if(viewManager.getView().equals(ViewManager.CONTOUR))
    {
      
    }
    else if(viewManager.getView().equals(ViewManager.IMAGE))
    {
      
    }
    else if(viewManager.getView().equals(ViewManager.TABLE))
    {
      
    }
  }

  public void setViewAttribute( String name , String value ) throws Exception
  {
    /*Object OSVal = null;
    name = name.toLowerCase();
    value = value.toLowerCase();
    Ostate = viewManager.getObjectState(true); 
    Hashtable<String, Object> values = getViewValueList();
    Hashtable<String,String> names = getViewAttributeList();
    
    String OSAttribute = (String)Style.TranslateKey(names,name);
    if( Ostate.get(OSAttribute) instanceof Dimension)
    {
      String checkedVal = "";
      Boolean hasComma = false;
      
      //--------Test value for proper format
      //--------Format will be forced if possible 
      //--------Else checkedVal will = null
      for( int i = 0; i < value.length(); i++)
      {
        
        if ( Character.isDigit(value.charAt(i)) )
        {
          checkedVal += value.charAt(i);
        }
        else if( value.charAt(i) == ',' )
        {
          if( !hasComma && checkedVal.length()>=1 )
          {
            checkedVal += value.charAt(i);
            hasComma = true;
          }
        }
      }
      if( !hasComma )
        checkedVal = null;
      else if( checkedVal.indexOf(",") == checkedVal.length()-1)
        checkedVal = null;
      
      //System.out.println(checkedVal);
      if( checkedVal != null )
      {
        int index = checkedVal.indexOf(",");
        String xStr = checkedVal.substring(0,index);
        String yStr = checkedVal.substring(index + 1);
        int xVal = Integer.parseInt(xStr);
        int yVal = Integer.parseInt(yStr);
        OSVal = new Dimension(xVal,yVal);
      }
    }
    else
      OSVal = Style.TranslateKey(values,value);
    
    try
    {
      Ostate.reset(OSAttribute, OSVal);
    }
    catch(Exception e)
    {
      throw e;
    }
    viewManager.setObjectState(Ostate);*/
  }


 /**
  *  This method sets an attribute of the displayable that pertains
  *  to a particular portion of the display, such as one particular
  *  line. 
  *
  *  @param  index    An index identifying the part of the display
  *                   that the attribute applies to, such as a 
  *                   specific line number.
  *  @param  name     The name of the attribute being set.
  *  @param  value    The value to use for the attribute.
  */
  public void setLineAttribute(int index, 
      String Attribute, 
      String val) throws Exception
  {
    Attribute = Attribute.toLowerCase();
    val = val.toLowerCase();
    Ostate = viewManager.getObjectState(true);
    Hashtable<String, Object> values = null;
    Hashtable<String,String> attributes = null;

    values = getGraphValuesTable();
    attributes = getGraphAttributeList();
    
    
    String OSAttribute = (String)Util.TranslateKey(attributes,Attribute);
    if(viewManager.getView().equals(ViewManager.SELECTED_GRAPHS))
      OSAttribute = attributes.get("selected graph data")+index+"."+OSAttribute;
    else if(viewManager.getView().equals(ViewManager.DIFFERENCE_GRAPH))
      OSAttribute = attributes.get("difference graph data")+index+"."+OSAttribute;
    else
      OSAttribute = null;
    
    Object OSVal = Util.TranslateKey(values,val);
    try
    {
      setLineAttribute(OSAttribute, OSVal);
    }
    catch(Exception e)
    {
      throw new Exception("Cannot put "+val+" into "+Attribute);
    }
  }

  private void setLineAttribute(String Attribute, 
      Object Val) throws Exception
  {
    try
    {
      Ostate.reset(Attribute, Val);
    }
    catch(Exception e)
    {
      throw e;
    }
    viewManager.setObjectState(Ostate);
  }
  
  public static Hashtable getGraphAttributeList()
  {    
    Hashtable<String,String> temp = new Hashtable<String,String>();
    temp.put("line type", "Line Type");
    temp.put("line color", "Line Color");
    temp.put("line width", "Line Width");
    temp.put("mark type", "Mark Type");
    temp.put("mark color", "Mark Color");
    temp.put("mark size", "Mark Size");
    temp.put("transparent", "Transparent");
    temp.put("selected graph data", 
             "Selected Graph View.View.Graph JPanel.Graph Data");
    temp.put("difference graph data", 
        "Difference Graph View.View.Graph JPanel.Graph Data");
    return temp;
  }
  
  public static Hashtable getGraphValuesTable()
  {
    Hashtable<String,Object> temp = new Hashtable();
    temp.put("black", Color.black);
    temp.put("white", Color.white);
    temp.put("blue", Color.blue);
    temp.put("cyan", Color.cyan);
    temp.put("green", Color.green);
    temp.put("orange", Color.orange);
    temp.put("red", Color.red);
    temp.put("yellow", Color.yellow);
    temp.put("dotted", GraphJPanel.DOTTED);
    temp.put("dashed", GraphJPanel.DASHED);
    temp.put("dashdot", GraphJPanel.DASHDOT);
    temp.put("none", 0);
    temp.put("true", true);
    temp.put("false", false);
    temp.put("dot", GraphJPanel.DOT);
    temp.put("plus", GraphJPanel.PLUS);
    temp.put("star", GraphJPanel.STAR);
    temp.put("bar", GraphJPanel.BAR);
    temp.put("cross", GraphJPanel.CROSS);
    return temp;
  }


 /**
  *  Main program that contains a crude test of the functionality
  *  of this class.
  */
  public static void main( String args[] ) throws Exception
  {
    String directory = "/home/dennis/WORK/ISAW/SampleRuns";
    String file_name = directory + "/GPPD12358.RUN";
//  String file_name = directory + "/SCD06496.RUN";
    //RunfileRetriever rr = new RunfileRetriever( file_name );
    //DataSet ds = rr.getDataSet(1);

    //for ( int i = 0; i < 100; i++ )
      //ds.setSelectFlag(  i, true );
    
    DataSet ds = new DataSet();    
    float[][] testData = ContourViewComponent.getTestDataArr(41,51,3,4);
    for (int i=0; i<testData.length; i++)
       ds.addData_entry(
             new FunctionTable(new UniformXScale(0, 
                               testData[i].length-1, 
                               testData[i].length),
                               testData[i], 
                               i));
    ds.setSelectFlag(5, true);
    ds.setSelectFlag(6, true);

//  Displayable disp = new DataSetDisplayable(ds, "Image View");
//  Displayable disp = new DataSetDisplayable(ds, "3D View");
  Displayable disp = new DataSetDisplayable(ds, "Contour View");
//  Displayable disp = new DataSetDisplayable(ds, "HKL Slice View");
//  Displayable disp = new DataSetDisplayable(ds, "Scrolled Graph View");
//  Displayable disp = new DataSetDisplayable(ds, "Selected Graph View");
//  Displayable disp = new DataSetDisplayable(ds, "Difference Graph View");
//  Displayable disp = new DataSetDisplayable(ds, "GRX_Y");
//  Displayable disp = new DataSetDisplayable(ds, "Parallel y(x)");
//  Displayable disp = new DataSetDisplayable(ds, "Counts(x,y)");
//  Displayable disp = new DataSetDisplayable(ds, "2D Viewer");
//  Displayable disp = new DataSetDisplayable(ds, "Slice Viewer");
//  Displayable disp = new DataSetDisplayable(ds, "Instrument Table");
//*****  Displayable disp = new DataSetDisplayable(ds, "Contour:Qy,Qz vs Qx");
//*****  Displayable disp = new DataSetDisplayable(ds, "Contour:Qx,Qy vs Qz");
//*****  Displayable disp = new DataSetDisplayable(ds, "Contour:Qxyz slices");
//  Displayable disp = new DataSetDisplayable(ds, "Table Generator");
    
//    disp.setLineAttribute(1, "line color", "red");
//    disp.setLineAttribute(2, "line color", "green");
//    disp.setLineAttribute(1, "line tYpe", "doTtEd");
//    disp.setLineAttribute(1, "Mark Type", "plus");
//    disp.setLineAttribute(1, "Mark color", "cyan");
    
//  GraphicsDevice gd = new ScreenDevice();
//  GraphicsDevice gd = new FileDevice("/home/dennis/test.jpg");
    GraphicsDevice gd = new PreviewDevice();
    gd.setRegion( 400, 500, 600, 400 );
    gd.display( disp, true );
    gd.print();
//    gd.close();
  }
}
