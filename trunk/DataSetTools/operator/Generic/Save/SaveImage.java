
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
 * Revision 1.1  2003/09/11 17:42:26  rmikk
 * Initial Checkin.  Updata DataSetTools.viewer also
 * Saves view as jpg
 *
 */

package DataSetTools.operator.Generic.Save;

import java.awt.*;
import DataSetTools.parameter.*;
import java.util.*;
import java.io.*;
import java.awt.image.*;
import DataSetTools.viewer.*;
import DataSetTools.operator.*;
import DataSetTools.util.*;
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
  public SaveImage( DataSet DS, String ViewName, String filename,int width, int height){
    this();
    parameters = new Vector();
    addParameter( new DataSetPG("Enter Data Set", DS));
    addParameter( new StringPG("Name of View", ViewName));
    addParameter( new SaveFilePG("Name of file to save", filename));
    addParameter( new IntegerPG("Enter Width",new Integer(width)));
    addParameter( new IntegerPG("Enter Width",new Integer(height)));
    
  }

  public void setDefaultParameters(){
     parameters = new Vector();
    addParameter( new DataSetPG("Enter Data Set", null));
    addParameter( new StringPG("Name of View", "Image View"));
    addParameter( new SaveFilePG("Name of file to save", null));
    addParameter( new IntegerPG("Enter Width",new Integer(500)));
    addParameter( new IntegerPG("Enter Width",new Integer(500)));
  }


  /**
  *    Saves the indicated image to the indicated file in the format specified by the extension
  *    of the file
  */
  public Object getResult(){
    DataSet ds = ((DataSet)(getParameter(0).getValue()));
    String view = getParameter(1).getValue().toString();
    String SaveFileName = getParameter(2).getValue().toString();
    int width = ((IntegerPG)getParameter(3)).getintValue();


    int height = ((IntegerPG)getParameter(4)).getintValue();

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
    DataSetViewer DSV = ViewManager.getDataSetView( ds, view, null);
    JFrame jf1 = new JFrame();
    jf1.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    jf1.setSize( width, height);   
    jf1.getContentPane().setLayout( new GridLayout(1,1));
    jf1.getContentPane(). add( DSV);
    
    jf1.validate();
    
    jf1.show();
   
    Graphics2D gr = bimg.createGraphics();
  
    DSV.paint( gr);
    jf1.dispose();
 
    try{
      FileOutputStream fout =new FileOutputStream( SaveFileName);
      if( !javax.imageio.ImageIO.write( bimg, extension ,fout ))
           return new ErrorString( " no appropriate writer is found");;
      fout.close();
    }catch( Exception ss){
       return new ErrorString( ss.toString());
    }
    
    return "Success";
  }//getResult

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
    s.append("@param filename-  the name of the file for saving the image. The extension converted to");
    s.append(" lowercase determines the format of the save.  So far only jpg works");
    s.append("@param  width-  The width of the picture in pixels. If negative, 500 will be used");
    s.append("@param  height-  The height of the picture in pixels.If negative, 500 will be used");
    s.append("@return the string 'Success' or an Error Condition below");
    s.append( "@error  extension on save file name is improper");
    s.append( "@error   no appropriate writer is found for the extension");
    return s.toString();
  }

}//SaveImage
