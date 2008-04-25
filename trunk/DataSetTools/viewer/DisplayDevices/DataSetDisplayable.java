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
 *  Last Modified:
 *
 *  $Author$
 *  $Date$            
 *  $Revision$
 *
 * Modified:
 *
 * $Log: DataSetDisplayable.java,v $
 * Revision 1.15  2007/08/08 21:47:02  oakgrovej
 * Commenting and cleanup
 *
 * Revision 1.14  2007/08/07 16:41:26  oakgrovej
 * Changed name of the value "line" to "solid".
 *
 * Revision 1.13  2007/08/07 16:18:02  oakgrovej
 * Added "line" option to the Value list
 *
 * Revision 1.12  2007/08/06 15:13:15  oakgrovej
 *  - Combined the Values into one Hashtable
 *  - The Hashtables are fields that are set in the constructor
 *  - Added some more attributes and values
 *
 * Revision 1.11  2007/07/30 20:04:54  oakgrovej
 * In the getJComponent() method I made a copy of the ViewManager and returned the component from that instead of the original.  this will allow for the displayable to be passed into muliple devices at once.
 *
 * Revision 1.10  2007/07/27 03:48:12  dennis
 * Fixed name inconsistency between javadco and method.
 *
 * Revision 1.9  2007/07/25 22:27:13  oakgrovej
 * added test for PrinterDevice in main and it works!
 *
 * Revision 1.8  2007/07/18 16:03:02  oakgrovej
 * added setViewAttribute method and approprate getTable method.  
 * Hashtable is empty.
 *
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
import java.util.Vector;

import javax.swing.*;

import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
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
  private Hashtable<String,Object> valueList;
  private Hashtable<String,String> graphAttributes;
  private Hashtable<String,String> viewAttributes;
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
    valueList = getValueList();
    graphAttributes = getGraphAttributeList();
    viewAttributes = getViewAttributeList();
    //System.out.println(Ostate);
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
    ViewManager temp = new ViewManager(viewManager.getDataSet(),viewManager.getView(),false);
    temp.setObjectState(viewManager.getObjectState(false));

    if ( with_controls )
      component = (JComponent)temp.getComponent(0);
    else
    {
      DataSetViewer viewer = temp.getViewer();
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

  /**
   * This method sets a particular View Attribute to the Value
   * 
   * @param name The name of the View Attribute to be set
   * @param value The name of the Value
   */
  public void setViewAttribute( String name , String value ) throws Exception
  {
    Object OSVal = null;
    name = name.toLowerCase();
    value = value.toLowerCase();
    Ostate = viewManager.getObjectState(true); 
    
    String OSAttribute = (String)Util.TranslateKey(viewAttributes,name);
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
    else if(name.contains("color"))
      OSVal = Util.Convert2Color(value);
    else
      OSVal = Util.TranslateKey(valueList,value);
    
    try
    {
      Ostate.reset(OSAttribute, OSVal);
    }
    catch(Exception e)
    {
      throw e;
    }
    viewManager.setObjectState(Ostate);
  }
  
  @Override
  public void setLineAttribute(int index, String name, Object value)
  		throws Exception {
  	if(value instanceof String)
  		setLineAttribute(index,name,(String)value);
  	else
  	{
  		String OSAttribute = (String)Util.TranslateKey(graphAttributes,name.toLowerCase());
  	    if(viewManager.getView().equals(ViewManager.SELECTED_GRAPHS))
  	      OSAttribute = graphAttributes.get("selected graph data")+index+"."+OSAttribute;
  	    else if(viewManager.getView().equals(ViewManager.DIFFERENCE_GRAPH))
  	      OSAttribute = graphAttributes.get("difference graph data")+index+"."+OSAttribute;
  	    else
  	      OSAttribute = null;
  		
  		try
  	    {
  	      setLineAttribute(OSAttribute, value);
  	    }
  	    catch(Exception e)
  	    {
  	      throw new Exception ("Cannot put "+value+" into "+name);
  	    }
  	}
  	
  }  
  
 /**
  *  This method sets an attribute of the displayable that pertains
  *  to a particular portion of the display, such as one particular
  *  line. 
  *
  *  @param  index      An index identifying the part of the display
  *                     that the attribute applies to, such as a 
  *                     specific line number.
  *  @param  Attribute  The name of the attribute being set.
  *  @param  val        The value to use for the attribute.
  */
  private void setLineAttribute(int    index, 
                               String Attribute, 
                               String val        ) throws Exception
  {
    Attribute = Attribute.toLowerCase();
    val = val.toLowerCase();
    Ostate = viewManager.getObjectState(true);    
    
    String OSAttribute = (String)Util.TranslateKey(graphAttributes,Attribute);
    if(viewManager.getView().equals(ViewManager.SELECTED_GRAPHS))
      OSAttribute = graphAttributes.get("selected graph data")+index+"."+OSAttribute;
    else if(viewManager.getView().equals(ViewManager.DIFFERENCE_GRAPH))
      OSAttribute = graphAttributes.get("difference graph data")+index+"."+OSAttribute;
    else
      OSAttribute = null;
    
    Object OSVal;
    if(Attribute.contains("color"))
      OSVal = Util.Convert2Color(val);
    else
      OSVal = Util.TranslateKey(valueList,val);
    try
    {
      setLineAttribute(OSAttribute, OSVal);
    }
    catch(Exception e)
    {
      throw new Exception ("Cannot put "+val+" into "+Attribute);
    }
  }

  /**
   * This method is used by the other setLineAttribute methods after
   * they have altered the Attribute name to be ObjectState specific.
   * 
   * @param name The ObjectState specific Attribute name
   * @param val The Value to set the Attribute to
   */
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
  
  /**
   * This methods creates and returns a Hashtable containing the Graph Line
   * Attribute names along with their ObjectState specific name
   *  
   * @return The Hashtable
   */
  public static Hashtable<String,String> getGraphAttributeList()
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
  
  /**
   * This methods creates and returns a Hashtable containing the View Attribute
   * names along with their ObjectState specific path
   *  
   * @return The Hashtable
   */
  public static Hashtable<String,String> getViewAttributeList()
  {
    Hashtable<String,String> temp = new Hashtable<String,String>();
    temp.put("legend", "Selected Graph View.View.FunctionControls.Legend Control.Selected");
    temp.put("grid lines x", "Selected Graph View.View.AxisOverlay2D.Grid Display X");
    temp.put("grid lines y", "Selected Graph View.View.AxisOverlay2D.Grid Display Y");
    temp.put("grid color", "Selected Graph View.View.AxisOverlay2D.Grid Color");
    return temp;
  }
  
  /**
   * This methods creates and returns a Hashtable containing the Value
   * names along with the Object they represent
   *  
   * @return The Hashtable
   */
  public static Hashtable<String,Object> getValueList()
  {
    Hashtable<String,Object> temp = new Hashtable<String,Object>();
    temp.put("black", Color.black);
    temp.put("white", Color.white);
    temp.put("blue", Color.blue);
    temp.put("cyan", Color.cyan);
    temp.put("green", Color.green);
    temp.put("orange", Color.orange);
    temp.put("red", Color.red);
    temp.put("yellow", Color.yellow);
    temp.put("gray", Color.gray);
    temp.put("magenta", Color.magenta);
    temp.put("light gray", Color.lightGray);
    temp.put("dotted", GraphJPanel.DOTTED);
    temp.put("dashed", GraphJPanel.DASHED);
    temp.put("dashdot", GraphJPanel.DASHDOT);
    temp.put("none", 0);
    temp.put("off", 0);
    temp.put("on", 1);
    temp.put("true", true);
    temp.put("false", false);
    temp.put("solid", GraphJPanel.LINE);
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
//    String directory = "/home/dennis/WORK/ISAW/SampleRuns";
//    String file_name = directory + "/GPPD12358.RUN";
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
  
//  Displayable disp = new DataSetDisplayable(ds, "Contour View");//note dnw
//  Displayable disp = new DataSetDisplayable(ds, "HKL Slice View");
  Displayable disp2 = new DataSetDisplayable(ds, "Scrolled Graph View");
  Displayable disp = new DataSetDisplayable(ds, "Selected Graph View");
//  Displayable disp = new DataSetDisplayable(ds, "Difference Graph View");
//  Displayable disp = new DataSetDisplayable(ds, "GRX_Y");
//  Displayable disp = new DataSetDisplayable(ds, "Parallel y(x)");
//  Displayable disp = new DataSetDisplayable(ds, "Instrument Table");
//  Displayable disp = new DataSetDisplayable(ds, "Table Generator");
    
  //---------not in ViewManager
//  Displayable disp = new DataSetDisplayable(ds, "Counts(x,y)");
//  Displayable disp = new DataSetDisplayable(ds, "2D Viewer");
//  Displayable disp = new DataSetDisplayable(ds, "Slice Viewer");
//*****  Displayable disp = new DataSetDisplayable(ds, "Contour:Qy,Qz vs Qx");
//*****  Displayable disp = new DataSetDisplayable(ds, "Contour:Qx,Qy vs Qz");
//*****  Displayable disp = new DataSetDisplayable(ds, "Contour:Qxyz slices");
    
    disp.setLineAttribute(1, "line color", "red");
    disp.setLineAttribute(2, "line color", "red");
    disp.setLineAttribute(2, "line tYpe", "dashdot");
    disp.setLineAttribute(2, "line tYpe", "solid");
    //disp.setLineAttribute(1,"transparent", "true");
    disp.setLineAttribute(1, "line tYpe", "doTtEd");
    disp.setLineAttribute(1, "Mark Type", "plus");
    //disp.setLineAttribute(1, "Mark color", "cyan");
    
    disp.setViewAttribute("legend", "true");
    disp.setViewAttribute("grid lines x", 1);
    disp.setViewAttribute("grid lines y", true);
    disp.setViewAttribute("grid color", Color.red);
    
//  GraphicsDevice gd = new ScreenDevice();
//  GraphicsDevice gd = new FileDevice("C:/Documents and Settings/student/My Documents/My Pictures/test.jpg");
//  GraphicsDevice gd = new PreviewDevice();
  GraphicsDevice gd = new PrinterDevice("Adobe PDF");//HP LaserJet 4000 Series PCL Adobe PDF
    
  System.out.println(((DataSetDisplayable) disp).Ostate);
    // -------------For PrinterDevice
    //gd.setDeviceAttribute("orientation", "landscape");
    //gd.setDeviceAttribute("copies", 1);
  
   
    gd.setRegion(10, 0, 200, 600);
    //gd2.setRegion( 600,0, 600, 900 );
   // Vector bound = new Vector<Integer>();
    //bound.add(700);
    //bound.add(100);
    //((FileDevice)gd).setBounds(bound);
    gd.display( disp, true );
    gd.setRegion(200, 10, 250, 250 );
    System.out.println(gd.getBounds());
    gd.display( disp2, true );
    gd.setDeviceAttribute("orientation", "portrait");
    //gd.setDeviceAttribute("file resolution", value)
    //gd.setDeviceAttribute("printableareax", .5f);
    //gd.setDeviceAttribute("printableareay", .5f);
    //gd2.display( disp2,true);
    gd.print();
    //gd2.print();
//    gd.close();
  }

}
