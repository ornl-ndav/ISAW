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
 * Revision 1.7  2004/05/27 19:18:22  robertsonj
 * *** empty log message ***
 *
 * Rivision 1.7 2004/05/27 robertson
 * added functionality: Print in landscape, pick printer from list, print x copies, choose print quality.
 * 
 * Revision 1.6  2004/05/04 19:03:50  dennis
 * Now clears DataSetPG after getting value, to avoid memory leak.
 *
 * Revision 1.5  2004/03/15 06:10:50  dennis
 * Removed unused import statements.
 *
 * Revision 1.4  2004/03/15 03:28:34  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.3  2003/10/20 16:38:11  rmikk
 * Fixed javadoc error
 *
 * Revision 1.2  2003/10/09 20:23:08  dennis
 * Changed catch(Exception u) to catch(Throwable u) to fix
 * warning when compiling with jdk 1.4.1_05.
 *
 * Revision 1.1  2003/09/16 15:43:31  rmikk
 * Initial Checkin
 *
 */

package DataSetTools.operator.Generic.Save;
import DataSetTools.dataset.*;
import DataSetTools.viewer.*;
import DataSetTools.parameter.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Sys.*;

import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
   *   @param DS  The DataSet whose view is to be printed
   *   @param view_type The name of the view used by the ViewManager. This 
   *                    is the String that appears in Isaw's View Menu
   *   @param State A Vector containing entries that are Vectors with two 
   *                elements: of State's Name and its value(not implemented yet)
   *                See getDocumentation method for some State names
   *   @param width The width of the image(will be scaled to fit the paper)
   *   @param height The height of the image in pixels(Also scaled)
   *   @param PrintName the name of the printer. If blank, any printer 
   *                    will be considered
   *   @param PrintLocation The location of the printer. If blank, any
   *                        location will be considered
   *   @param PrintOptions (not implemented yet).For options like Portrait, etc.
   */
   public PrintImage( DataSet DS, String view_type, Vector State,
   int width, int height, String PrintName, 
	  String PrintLocation, Vector PrintOptions, String orientation, int copies){
   this();
   
   
  
  	
	  
      parameters = new Vector();
      addParameter( new DataSetPG( "Select DataSet",DS));
	  addParameter( new PrinterNamePG("select printer", null));
	  addParameter( new StringPG( "View Name", view_type));
	  addParameter(new BooleanPG("Landscape" , orientation));
      addParameter( new IntegerPG("copies", copies));
      addParameter( new ArrayPG("State info", null));
	  addParameter( new IntegerPG("quality", 1));
	  addParameter( new IntegerPG("height",height));
      addParameter( new IntegerPG("width", width));
      
	 
      
	  
	  
	  
   }

  public void setDefaultParameters(){ 
	
	

      parameters = new Vector();
      addParameter( new DataSetPG( "Select DataSet",null));//0
	  addParameter( new PrinterNamePG("select printer", null));//5
	  addParameter( new StringPG( "View Name", "Image View"));//1
	  addParameter( new BooleanPG("LandScape",""));//7
      addParameter( new IntegerPG("Copies", 1));
	  addParameter( new IntegerPG("quality",0));//8
      addParameter( new ArrayPG("State info", null));//2
	  addParameter( new IntegerPG("height",500));//4
      addParameter( new IntegerPG("width", 500));//3
      
	  
      
	  
	  
	  
  }

  /**
  *    Creates the desired image and puts it into a JFrame. Then
  *    it attempts to find a printer to print this image
  */
  public Object getResult(){
  	
     DataSet DS = ((DataSetPG)(getParameter(0))).getDataSetValue();
     ((DataSetPG)(getParameter(0))).clear();  //needed to avoid memory leak

     String ViewName = getParameter(2).getValue().toString();
     Vector State = ((ArrayPG)(getParameter(6))).getVectorValue();
     int  width = ((IntegerPG)(getParameter(8))).getintValue();
     int height = ((IntegerPG)(getParameter(7))).getintValue();
     String PrintName = getParameter(1).getValue().toString();
     boolean orientation =((BooleanPG)(getParameter(3))).getbooleanValue();
     int quality = ((IntegerPG)(getParameter(5))).getintValue();
     int copies = ((IntegerPG)(getParameter(4))).getintValue();
     
   
     // Set up the Viewer State here
     DataSetViewer DSV = ViewManager.getDataSetView(DS, ViewName, null);
     DSV.validate();
     DSV.setSize(width-5, height-5);
     JFrame jf = new JFrame();
     jf.setSize(width, height);
     jf.getContentPane().setLayout( new GridLayout(1,1));
     jf.getContentPane().add(DSV);
     jf.addWindowListener(new MyWindowListener(DSV, PrintName, orientation, quality, copies, jf));
	 jf.show();
	
     return "Success";
  }
  class MyWindowListener extends WindowAdapter{
	DataSetViewer DSV;
 	String PrintLocation;
 	String PrintName;
 	JFrame jf;
 	int quality;
 	boolean orientation;
 	int copies;
  	public MyWindowListener(DataSetViewer DSV, String PrintName, 
  														boolean orientation, int quality, int copies, JFrame jf)
  	{
			this.DSV = DSV;
		
			this.PrintName = PrintName;
			this.jf = jf;
			this.orientation = orientation;
			this.quality = quality;
			this.copies = copies;
  	}
	
  public void windowOpened(WindowEvent winevt){
  	printResult(DSV,PrintLocation, PrintName, orientation, quality, copies, jf);
  }
 
  	public void printResult(DataSetViewer DSV, String PrintLocation, String PrintName, 
  										boolean orientation, int quality, int copies, JFrame jf){
	
	
	
	 DocFlavor myFormat = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
	 Doc myDoc = null;
     PrintUtilities pr_utils = new PrintUtilities(DSV);
     // Set the document type
    
     // Create a Doc
     
     try{
       myDoc = new SimpleDoc(pr_utils, myFormat, null); 
     }catch(Throwable u){jf.dispose();
       /*return*/ new ErrorString( u.toString());
     }
    // Build a set of attributes
    HashPrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
    Copies numberOfCopies = new Copies(copies);
    aset.add(numberOfCopies);
  	if (orientation == true){
  		boolean fake = aset.add(OrientationRequested.LANDSCAPE);
  	}
  	else
  	{
  		boolean fake2 = aset.add(OrientationRequested.PORTRAIT);
  	}

  	// To determine the level of quality. (may not work with laser printers
	if (quality != 0)
  	System.out.println("Past quality check");
      PrintService[] services =
	PrintServiceLookup.lookupPrintServices(myFormat, aset);
	

     // Create a print job from one of the print services
   
     PrintService service = getPrintService( services, PrintName,
                PrintLocation);
     if (quality != 0)
     {
     	
   		if (service.isAttributeCategorySupported(PrintQuality.class) )
   		{
   			if (quality == 1){aset.add(PrintQuality.DRAFT);
   			}
   			if (quality == 2){aset.add(PrintQuality.NORMAL);
   			}
   			if (quality == 3){aset.add(PrintQuality.HIGH);
   			}
   		}
     }
    if (service !=null) { 
        
	DocPrintJob job = service.createPrintJob(); 
	
	
	
	try { 
		job.print(myDoc, aset); 
	} catch (PrintException pe) {jf.dispose();
         /*return*/ new ErrorString( "Print Exception:"+ pe.toString());
        } 
   }else{;//jf.dispose();
       /*return*/ new ErrorString( " No Printers Found");
   }
   jf.dispose();
  
   }
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
