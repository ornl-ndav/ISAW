/*
 * File:SaveImage.java 
 *             
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Contact : Ruth Mikkelson <mikkelsonr@uwstout.edu>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.8  2004/06/24 15:28:40  robertsonj
 * Implemented a windowlistener so the save image from scripts would get 
 * a nice clean picture of the stated viewer.
 *
 * Revision 1.7  2004/06/21 16:06:26  robertsonj
 * Changed the help file to relfect the changes in the viewer state class
 *
 * Revision 1.7  2004/06/21 10:57:31 robertsonj
 * Changed the help file to include the name value pairs for the viewer state.
 * fixed the border problem with the jpg saved pictures.
 * 
 * Revision 1.6  2004/06/15 19:24:53  robertsonj
 * Save image now allows you to input a state that you would like 
 * the viewer to be saved in.
 *
 * 
 * Revision 1.6 2004/06/14 robertson
 * Now has options to choose the viewer  state you would like to save
 * 
 * Revision 1.5  2004/05/04 19:03:50  dennis
 * Now clears DataSetPG after getting value, to avoid memory leak.
 *
 * Revision 1.4  2004/03/15 19:33:52  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.3  2004/03/15 03:28:34  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.2  2004/01/24 19:50:54  bouzekc
 * Removed unused imports.
 *
 * Revision 1.1  2003/09/11 17:42:26  rmikk
 * Initial Checkin.  Updata DataSetTools.viewer also
 * Saves view as jpg
 *
 */

package DataSetTools.operator.Generic.Save;

import gov.anl.ipns.Util.SpecialStrings.*;
import java.awt.event.*;
import java.awt.*;
import DataSetTools.parameter.*;
import java.util.*;
import java.io.*;
import java.awt.image.*;
import DataSetTools.viewer.*;
import DataSetTools.dataset.*;
import javax.swing.*;

/**
*    This class saves a DataSetViewer Image to a file. So far only saving as
*    jpg works.  ViewerState is always null so far
*    NOTE: needs java 1.4's javax.imagio
*/
public class SaveImage  extends GenericSave{

  BufferedImage bimg= null;
  Image Img = null;
  public SaveImage(){
    super("Save Image");
    setDefaultParameters();
  }

  /**
  *   Constructor for SaveImage
  *   @param  DS   The DataSet whose view is to be saved as jpg( see filename)
  *   @param ViewName the name of the view to use.  This MUST be a string that appears
  *                   as one of the menu items or submenu items under the View menu option
  *                   in the IsawGUI.Isaw main window
  *   @param filename  the name of the file for saving the image. The extension converted to
  *                lowercase determines the format of the save.  So far only jpg works
  *   @param  width   The width of the picture in pixels. If negative, 500 will be used
  *   @param  height  The height of the picture in pixels.If negative, 500 will be used
  */
  public SaveImage( DataSet DS, String ViewName, String filename, String state, int width, int height){
    this();
    parameters = new Vector();
    addParameter( new DataSetPG("Enter Data Set", DS));
    addParameter( new StringPG("Name of View", ViewName));
    addParameter( new SaveFilePG("Name of file to save", filename));
    addParameter( new StringPG("State Info", state));
    addParameter( new IntegerPG("Enter Width",new Integer(width)));
    addParameter( new IntegerPG("Enter Height",new Integer(height)));
    
  }

  public void setDefaultParameters(){
     parameters = new Vector();
    addParameter( new DataSetPG("Enter Data Set", null));
    addParameter( new StringPG("Name of View", "Image View"));
    addParameter( new SaveFilePG("Name of file to save", null));
    addParameter( new StringPG("State Info", null));
    addParameter( new IntegerPG("Enter Width",new Integer(500)));
    addParameter( new IntegerPG("Enter Height",new Integer(500)));
  }


  /**
  *    Saves the indicated image to the indicated file in the format specified by the extension
  *    of the file
  */
  public Object getResult(){
    DataSet ds = ((DataSet)(getParameter(0).getValue()));
    ((DataSetPG)getParameter(0)).clear();    // needed to avoid memory leak
     
    String view = getParameter(1).getValue().toString();
    String SaveFileName = getParameter(2).getValue().toString();
    String stateInfo = getParameter(3).toString();
    System.out.println(stateInfo);
    int width = ((IntegerPG)getParameter(4)).getintValue();


    int height = ((IntegerPG)getParameter(5)).getintValue();

    int i = SaveFileName.lastIndexOf('.');
    if( i<= 0)
      return new ErrorString("the Filename must have an extension for type of save");

    String extension = SaveFileName.substring( i+1).toLowerCase();
    if( extension == null) 
      return new ErrorString("Save FileName must have an extension");
    if( extension.length() <2)
      return new ErrorString("Save FileName must have an extension with more than 1 character");
    if( width <=0) width = 500;
    if( height <= 0) height = 500;
    bimg= new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB );
    DataSetViewer DSV;
    if(stateInfo.equals(null)){
		DSV = ViewManager.getDataSetView( ds, view, null);
    }else{
    	ViewerState myState = new ViewerState();
    	System.out.println(stateInfo);
    	ViewerState newState = myState.setViewerState(stateInfo);
    	DSV = ViewManager.getDataSetView(ds, view, newState);
    }
    DSV.setSize(width, height);
    JFrame jf1 = new JFrame();
    jf1.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    jf1.setSize( width+7, height+25);   
    jf1.getContentPane().setLayout( new GridLayout(1,1));
    jf1.getContentPane(). add( DSV);

    
    
	jf1.validate();
	Graphics2D gr = bimg.createGraphics();
	jf1.addWindowListener(new MyWindowListener(SaveFileName, extension, bimg, DSV, gr, jf1));
	//DSV.paint( gr);
	jf1.show();
	
    
   
	//DSV.paint( gr);

    //jf1.dispose();
	return "Success";
 }
 
 class MyWindowListener extends WindowAdapter{
 	public String SaveFileName;
 	public String extension;
 	public BufferedImage bimg;
 	public DataSetViewer DSV;
 	public Graphics2D gr;
 	public JFrame jf1;
 
 	public MyWindowListener(String filename, String pin_extension, BufferedImage pin_bimg, 
 								DataSetViewer pin_DSV, Graphics2D pin_gr, JFrame pin_jf1)
 	{
 		SaveFileName = filename;
 		extension = pin_extension;
 		bimg = pin_bimg;
 		DSV = pin_DSV;
 		gr = pin_gr;
 		jf1 = pin_jf1;
 	//This should be in the window listener for this page.
 	}
    public void windowOpened(WindowEvent winevt){
    	DSV.paint(gr);
    	saveResult(SaveFileName, extension, bimg, DSV, jf1);
 	}//getResult
}
public void saveResult(String filename, String extension, BufferedImage bimg,
							 DataSetViewer DSV, JFrame jf1){
	try{
		  FileOutputStream fout =new FileOutputStream( filename);
		  if( !javax.imageio.ImageIO.write( bimg, extension ,fout ))
			   //return new ErrorString( " no appropriate writer is found");;
		  fout.close();
		}catch( Exception ss){
		   //return new ErrorString( ss.toString());
		}
		jf1.dispose();
	}	


/* ---------------------- getDocumentation --------------------------- */
  /** 
   *  Returns the documentation for this method as a String.  The format 
   *  follows standard JavaDoc conventions.  
   */
  public String getDocumentation()
  {
    StringBuffer s = new StringBuffer("");
    s.append("@overview  This operator writes a View of a DataSet ");
    s.append("to a file. So far jpg format is the only format tested");
    s.append("@assumptions The given data set is not empty.\n");                                                            
    s.append("@algorithm This operator uses javax.imageio.ImageIO.write ");
    s.append("to write the view. It first gets a View and places it in a ");
    s.append("JFrame, shows the frame, prints the file, then disposes of the frame.");
    s.append("@param  DS-  The DataSet whose view is to be saved as jpg( see filename)");
    s.append("@param ViewName- the name of the view to use.  This MUST be a string that appears");
    s.append(" as one of the menu items or submenu items under the View menu option");
    s.append(" in the IsawGUI.Isaw main window");
    s.append("@param stateInfo- uses the ViewerState class.  This string must be of the form");
    s.append(" Name Value,Name Value,.....,Name Value.  Where the name and values must be part of the ");
    s.append(" ViewState class.  The values are below");
    s.append("@param filename-  the name of the file for saving the image. The extension converted to");
    s.append(" lowercase determines the format of the save.  So far only jpg works");
    s.append("@param  width-  The width of the picture in pixels. If negative, 500 will be used");
    s.append("@param  height-  The height of the picture in pixels.If negative, 500 will be used");
	s.append("<P><P> Some DataSetViewer States are<table bofder=1>");
s.append("<tr><td>ColorScale</td> <td>Most Views</td><td> String ");
s.append("like Heat1,Rainbow</td>");
s.append("</tr><tr><td>RebinFlag</td><td>Image View</td><td> Boolean</td>");
s.append("</tr><tr><td>HScrollFlag</td> <td>Image View</td><td>??</td>");
s.append("</tr><tr><td>HScrollPosition</td><td>Image View </td><td>float 0 to 1</td>");
s.append(" </tr><tr><td>VScrollPosition</td> <td>Image View </td><td>???</td>");
s.append("</tr><tr><td>PointedAtIndex</td> <td> Most Views</td>" );
s.append("  <td> Positive Integer<#of spectra</td>");
s.append("</tr><tr><td>PointedAtX</td>td> Most Views</td> ");
s.append("<td> float corresponding to x values</td>" );
s.append("</tr><tr><td>Brightness</td> <td>Image and 3D views</td><td>int from 0 ");
s.append("to 1000</td>");
s.append("</tr><tr><td>ViewAzimuthAngle</td> <td>ThreeD</td><td>angle in degrees</td>");
s.append("</tr><tr><td>ViewAltitudeAngle</td> <td>ThreeD</td><td>angle in degrees</td>");
s.append(" </tr><tr><td>ViewDistance</td><td>ThreeD</td><td>dist in meters</td>");
s.append("</tr><tr><td>ViewGroups</td> <td>ThreeD</td><td>String(see 3Dmenu) </td>");
s.append(" </tr><tr><td>ViewDetectors</td> <td>ThreeD</td><td>String(see menu ");
s.append(" in 3D for choices)</td> ");
s.append("</tr><tr><td>AutoScale</td><td>Image</td><td>float 0 to 100</td>");
s.append("</tr><tr><td>tableview Data</td> <td>TableView(time slice</td>");
s.append(" <td>String OK if set</td>");
s.append("<td>String:Contains \"Err\"or \"index\"if they are to be shown</td>");
s.append("</tr><tr><td>ContourStyle</td> </td>Contour View<td></td>");
s.append("<td> 1 for AREA_FILL, 4 for AREA_FILL_CONTOUR ,2 for CONTOUR ,");
s.append("0 for RASTER,  3 for RASTER_CONTOUR </td>");
s.append("</tr><tr><td>ContourTimeMin</td> <td>Contour</td> <td>float min time</td>");
s.append("</tr><tr><td>TimeSliceTable</td> </td><td></td> <td></td>");
s.append("</tr><tr><td>ContourTimeMax</td> <td>Contour</td> <td>float max time</td>");
s.append("</tr><tr><td>ContourTimeStep</td> <td>Contour</td> <td>");
s.append("int for Xscale chooser number of time steps</td>");
s.append("</tr><tr><td>ContourIntensity</td><td>Controu</td>");
s.append("<td> int 0 to 100, intensity</td>");
s.append("</tr><tr><td>TableTimeSliceTimeInd</td><td>TimeSlice Table View</td>");
s.append(" <td>int:Pointed at time channel or slice channel</td>");
s.append("</tr><tr><td>TableTimeSliceMinRow</td><td>TimeSlice Table View</td>");
s.append("<td> int:Min row to include(1 to #rows)</td>");
s.append("</tr><tr><td>TableTimeSliceMaxRow</td><td>TimeSlice Table View</td>");
s.append(" <td> int:Max row to include(1 to #rows)</td>");
s.append("</tr><tr><td>TableTimeSliceMinCol</td><td>TimeSlice Table View</td>");
s.append(" <td> int:Min col to include(1 to #rows)</td>");
s.append("</tr><tr><td>TableTimeSliceMaxCol</td><td>TimeSlice Table View</td>");
s.append("<td> int:Max col to include(1 to #rows)</td>");
s.append("</tr><tr><td>TableTimeSliceMinTime</td><td>TimeSlice Table View</td>");
s.append("<td>float: min time to include</td>");
s.append("</tr><tr><td>TableTimeSliceMaxTime</td><td>TimeSlice Table View</td>");
s.append(" <td>float: max time to include</td>");
s.append("</tr><tr><td>TableTimeSliceNxSteps</td><td>TimeSlice Table View</td>");
s.append("<td>float: # of time steps for Xsclae</td>");
s.append(" </tr></table>");
    s.append("@return the string 'Success' or an Error Condition below");
    s.append( "@error  extension on save file name is improper");
    s.append( "@error   no appropriate writer is found for the extension");
    return s.toString();
  }

}//SaveImage
