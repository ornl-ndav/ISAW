
/*
 * File:  PrintImage.java
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
 * Revision 1.1  2003/09/16 15:43:31  rmikk
 * Initial Checkin
 *
 */



package DataSetTools.operator.Generic.Save;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.parameter.*;
import IsawGUI.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import java.util.*;
import DataSetTools.util.*;
import javax.swing.*;
import java.awt.*;

/**
*  This operator will Print a View of the DataSet corresponding to one of the
*  DataSetViewers to a Printer.
*/
public class PrintImage extends GenericSave{

   /**
   *    Default Constructor
   */
   public PrintImage(){
     super("Print Image");
     setDefaultParameters();
   }
  

   /**
   *      Constructor for Java code
   *   @param DS - The DataSet whose view is to be printed
   *   @param view_type- The name of the view used by the ViewManager. This is the
   *                     String that appears in Isaw's View Menu
   *   @param State -A Vector containing entries that are Vectors with two 
   *                elements: of State's Name and its value(not implemented yet)
   *                See getDocumentation method for some State names
   *   @param width- The width of the image(will be scaled to fit the paper)
   *   @param height- The height of the image in pixels(Also scaled)
   *   @param PrintName- the name of the printer. If blank, any printer will be considered
   *   @param PrintLocation- The location of the printer. If blank, any location will
   *                        be considered
   *   @param PrintOptions- (not implemented yet).For options like Portrait, etc.
   */
   public PrintImage( DataSet DS, String view_type, Vector State,
         int width, int height, String PrintName, 
         String PrintLocation, Vector PrintOptions){
      this();
      parameters = new Vector();
      addParameter( new DataSetPG( "Select DataSet",DS));
      addParameter( new StringPG( "View Name", view_type));
      addParameter( new ArrayPG("State info", null));
      addParameter( new IntegerPG("width", width));
      addParameter( new IntegerPG("height",height));
      addParameter( new StringPG( "Printer Name", PrintName));
      addParameter( new StringPG( "Printer Location", PrintLocation));
      addParameter( new ArrayPG("Printer Options", PrintOptions));

   }

  public void setDefaultParameters(){ 
      parameters = new Vector();
      addParameter( new DataSetPG( "Select DataSet",null));
      addParameter( new StringPG( "View Name", "Image View"));
      addParameter( new ArrayPG("State info", null));
      addParameter( new IntegerPG("width", 500));
      addParameter( new IntegerPG("height",500));
      addParameter( new StringPG( "Printer Name",""));
      addParameter( new StringPG( "Printer Location", ""));
      addParameter( new ArrayPG("Printer Options", new Vector()));
   
  }

  /**
  *    Creates the desired image and puts it into a JFrame. Then
  *    it attempts to find a printer to print this image
  */
  public Object getResult(){
     DataSet DS = ((DataSetPG)(getParameter(0))).getDataSetValue();
     String ViewName = getParameter(1).getValue().toString();
     Vector State = ((ArrayPG)(getParameter(2))).getVectorValue();
     int  width = ((IntegerPG)(getParameter(3))).getintValue();
     int height = ((IntegerPG)(getParameter(4))).getintValue();
     String PrintName = getParameter(5).getValue().toString();
     String PrintLocation = getParameter(6).getValue().toString();
     Vector PrintOptions = ((ArrayPG)(getParameter(7))).getVectorValue();

     // Set up the Viewer State here
     DataSetViewer DSV = ViewManager.getDataSetView(DS, ViewName, null);
     DSV.setSize( width,height);
     JFrame jf = new JFrame();
     jf.getContentPane().setLayout( new GridLayout(1,1));
     jf.setSize( width,height);
     jf.getContentPane().add( DSV);
     jf.show();
     PrintUtilities pr_utils = new PrintUtilities( DSV);
     // Set the document type
     DocFlavor myFormat = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
     // Create a Doc
     Doc myDoc = null;
     try{
       myDoc = new SimpleDoc(pr_utils, myFormat, null); 
     }catch(Exception u){jf.dispose();
       return new ErrorString( u.toString());
     }
    // Build a set of attributes
     HashPrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
      
     
     PrintService[] services =
	PrintServiceLookup.lookupPrintServices(myFormat, aset);
     // Create a print job from one of the print services
   
     PrintService service = getPrintService( services, PrintName,
                PrintLocation);
    if (service !=null) { 
        
	DocPrintJob job = service.createPrintJob(); 
	try { 
		job.print(myDoc, aset); 
	} catch (PrintException pe) {jf.dispose();
         return new ErrorString( "Print Exception:"+ pe.toString());
        } 
   }else{jf.dispose();
       return new ErrorString( " No Printers Found");
   }
   jf.dispose();
   return "Success";
   }
  private PrintService getPrintService( PrintService[] services, 
          String PrintName,String  PrintLocation){
    if( services == null)
       return null;
    for( int i = 0; i < services.length; i++){
       PrintServiceAttribute P1 = services[i].getAttribute(
              (new PrinterName("",Locale.US).getClass()));
       if( PrintName != null) if( PrintName.length() > 1)
         if( P1 == null)
           services[i] = null;
         else if( P1.toString().toUpperCase().indexOf(PrintName.toUpperCase())<0)
             services[i] = null;
       
       if( services[i] != null)if(PrintLocation !=null) 
          if(PrintLocation.length()>1){
            P1 = services[i].getAttribute(
                 (new PrinterLocation("",Locale.US)).getClass());
          
          if( P1 == null)
             services[i] = null;
          else if( P1.toString().toUpperCase().indexOf(PrintLocation.toUpperCase())
              >=0) services[i] = null;
       }
                
     }
     for(int  i = 0; i< services.length; i++)
        if( services[i] != null)
          return services[i];
     return null;
 
    
   }//getPrintService

  public String getDocumentation(){
    StringBuffer s = new StringBuffer("");                                                 
    s.append("@overview-This operator will Print a View of the DataSet ");
    s.append("corresponding to one of the DataSetViewers to a Printer.");                                            
    s.append("@algorithm- Creates the desired image and puts it into a ");                                           
    s.append("JFrame. Then it attempts to find a printer to print this image to");
    s.append("@param DS - The DataSet whose view is to be printed");
    s.append("@param view_type- The name of the view used by the ViewManager. ");
    s.append("This is the String that appears in Isaw's View Menu");
    s.append("@param State -A Vector containing entries that are Vectors with two "); 
    s.append("elements: of State's Name and its value(not implemented yet)");
    s.append("@param width- The width of the image(will be scaled to fit the paper)");
    s.append("@param height- The height of the image in pixels(Also scaled)");
    s.append("@param PrintName- the name of the printer. If blank, any printer ");
    s.append("will be considered");
    s.append("@param PrintLocation- The location of the printer. If blank, any ");
    s.append("location will be considered");
    s.append("@param PrintOptions- (not implemented yet).For options like ");
    s.append("Portrait, etc.");
  
    s.append("@return Always returns the string 'Success' or an ErrorString ");
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
    s.append("</tr><tr><td>Auto-Scale</td><td>Image</td><td>float 0 to 100</td>");
    s.append("</tr><tr><td>table_view Data</td> <td>TableView(time slice</td>");
    s.append(" <td>String OK if set</td>");
    s.append("<td>String:Contains \"Err\"or \"index\"if they are to be shown</td>");
    s.append("</tr><tr><td>Contour.Style</td> </td>Contour View<td></td>");
    s.append("<td> 1 for AREA_FILL, 4 for AREA_FILL_CONTOUR ,2 for CONTOUR ,");
    s.append("0 for RASTER,  3 for RASTER_CONTOUR </td>");
    s.append("</tr><tr><td>ContourTimeMin</td> <td>Contour</td> <td>float min time</td>");
    s.append("</tr><tr><td>Time Slice Table</td> </td><td></td> <td></td>");
    s.append("</tr><tr><td>ContourTimeMax</td> <td>Contour</td> <td>float max time</td>");
    s.append("</tr><tr><td>ContourTimeStep</td> <td>Contour</td> <td>");
    s.append("int for Xscale chooser number of time steps</td>");
    s.append("</tr><tr><td>Contour.Intensity</td><td>Controu</td>");
    s.append("<td> int 0 to 100, intensity</td>");
    s.append("</tr><tr><td>TableTS_TimeInd</td><td>TimeSlice Table View</td>");
    s.append(" <td>int:Pointed at time channel or slice channel</td>");
    s.append("</tr><tr><td>TableTS_MinRow</td><td>TimeSlice Table View</td>");
    s.append("<td> int:Min row to include(1 to #rows)</td>");
    s.append("</tr><tr><td>TableTS_MaxRow</td><td>TimeSlice Table View</td>");
    s.append(" <td> int:Max row to include(1 to #rows)</td>");
    s.append("</tr><tr><td>TableTS_MinCol</td><td>TimeSlice Table View</td>");
    s.append(" <td> int:Min col to include(1 to #rows)</td>");
    s.append("</tr><tr><td>TableTS_MaxCol</td><td>TimeSlice Table View</td>");
    s.append("<td> int:Max col to include(1 to #rows)</td>");
    s.append("</tr><tr><td>TABLE_TS_MIN_TIME</td><td>TimeSlice Table View</td>");
    s.append("<td>float: min time to include</td>");
    s.append("</tr><tr><td>TABLE_TS_MAX_TIME</td><td>TimeSlice Table View</td>");
    s.append(" <td>float: max time to include</td>");
    s.append("</tr><tr><td>TABLE_TS_NXSTEPS</td><td>TimeSlice Table View</td>");
    s.append("<td>float: # of time steps for Xsclae</td>");
    s.append(" </tr></table>");
    s.append("@error  Errors are returned from the underlying print service");

    return s.toString();
  }

}//PrintImage
