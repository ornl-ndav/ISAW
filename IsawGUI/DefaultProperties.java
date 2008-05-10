/*
 * File:  DefaultProperties.java
 *
 * Copyright (C) 2002, Peter Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.36  2007/11/30 22:41:17  amoe
 *  Renamed IsawProps variable "ShowToolTip" to "ShowWCToolTip" .
 *
 *  Revision 1.35  2007/11/30 22:28:50  amoe
 *  Added variable "ShowToolTip=true".  (# Commented out)
 *
 *  Revision 1.34  2007/03/30 19:01:34  amoe
 *  Appended to defaultString()
 *       "#XRange_Time(us)_min"
 *       "#XRange_Time(us)_max"
 *       "#YRange_Counts_min"
 *       "#YRange_Counts_max"
 *
 *  Revision 1.33  2005/08/13 18:01:55  rmikk
 *  Added Default_Ext to the IsawProps.dat for documentation
 *  Removed newer DATA_FILE_EXTENSION(not used)
 *
 *  Revision 1.32  2005/08/05 19:21:25  dennis
 *  Updated some live data and file data server options in the
 *  default IsawProps.dat file that is generated by this class:
 *    -Removed NDS server at UW-Stout from list.
 *    -Added Live Data Server test at UW-Stout to list.
 *    -Reformatted name of File Data Server at UW-Stout.
 *
 *  Revision 1.31  2005/08/03 19:25:08  dennis
 *  Added lines for preferred browser and default file extension.
 *  The default file extension is not yet supported in ISAW.
 *
 *  Revision 1.30  2005/06/19 21:21:30  dennis
 *  Added PixelDepthScale=256 to default properties.  This is needed
 *  for the correct calculation of depth values with jogl when
 *  using the ATI proprietary drivers for FireGL T2 cards (and
 *  possibly other ATI cards).  This is commented out by default
 *  since it is not needed with Mesa or NVidia cards.
 *
 *  Revision 1.29  2005/06/14 21:07:19  dennis
 *  Removed Instrument_Macro_Path and Script_Macro_Path from
 *  the default properties file written by ISAW, since these
 *  properties are no longer used.
 *
 *  Revision 1.28  2005/01/10 16:15:39  rmikk
 *  Documented the new Property GROUPx_NAME in the IsawProps.dat file
 *
 *  Revision 1.27  2004/07/29 14:16:26  dennis
 *  Changed default color scale to Heat 2.
 *
 *  Revision 1.26  2004/06/18 20:32:54  rmikk
 *  Added eoln to the two new properties NSavedFiles and ShortSavedFilename
 *
 *  Revision 1.25  2004/05/25 12:22:44  rmikk
 *  Changed NsavedFiles to NSavedFiles
 *
 *  Revision 1.24  2004/05/24 18:35:27  rmikk
 *  Added properties ShortSavedFilename and NsavedFiles to the 
 *  default IsawProps.dat
 *
 *  Revision 1.23  2004/03/04 21:57:01  dennis
 *  Added two properties for controlling the size of a wizard.
 *
 *  Revision 1.22  2003/12/15 01:51:44  bouzekc
 *  Removed unused imports.
 *
 *  Revision 1.21  2003/11/25 19:04:06  rmikk
 *  Replaced %20 by a space after a call to Resource.
 *
 *  Revision 1.20  2003/06/03 21:27:51  pfpeterson
 *  Fixed circular logic by restoring old method.
 *
 *  Revision 1.19  2003/05/28 18:58:20  pfpeterson
 *  Changed System.getProperty to SharedData.getProperty
 *
 *  Revision 1.18  2003/04/21 16:13:38  pfpeterson
 *  Added SCD Live Data information, privatized load(String), and 
 *  removed unused code.
 *
 *  Revision 1.17  2003/03/05 20:21:14  pfpeterson
 *  Changed SharedData.status_pane.add(String) to SharedData.addmsg(String)
 *
 *  Revision 1.16  2003/02/13 21:45:13  pfpeterson
 *  Removed calls to deprecated function fixSeparator.
 *
 *  Revision 1.15  2002/11/27 23:27:07  pfpeterson
 *  standardized header
 *
 *  Revision 1.14  2002/10/24 15:15:28  pfpeterson
 *  Changed default IsawProps.dat to have "ViewDetetors=Filled" uncommented.
 *
 *  Revision 1.13  2002/08/15 18:46:48  pfpeterson
 *  Replaced hrmecs live data/file servers urls to be the new machines.
 *
 *  Revision 1.12  2002/08/12 20:25:40  pfpeterson
 *  Changed all calls to be FilenameUtil.fixSeparator rather than
 *  StringUtil.fixSeparator.
 *
 *  Revision 1.11  2002/06/12 19:08:59  pfpeterson
 *  Fixed a problem with the name of the jar file appearing in the
 *  properties. Also (small optimization) changed the default
 *  string to be built using a string buffer.
 *
 *  Revision 1.10  2002/05/29 21:39:17  pfpeterson
 *  Apply fixSeparator to ISAW_HOME directory and remove the
 *  leading '/' if it contains a ':'.
 *
 *  Revision 1.9  2002/05/29 21:16:07  pfpeterson
 *  Determines the location of ISAW_HOME through reflection.
 *
 *  Revision 1.8  2002/04/10 15:39:20  pfpeterson
 *  Added System properties to control MW width and height. Also improved
 *  changing the divider positions in the SplitPanes and removed SplitPanes
 *  that weren't used (get some screen space).
 *
 *  Revision 1.7  2002/04/08 18:22:14  pfpeterson
 *  Added properties to set JSplitPanel portions on startup.
 *
 *  Revision 1.6  2002/03/28 20:54:29  pfpeterson
 *  Resolved ISAW_HOME from ./ to an absolute directory.
 *
 *  Revision 1.5  2002/03/25 23:47:59  pfpeterson
 *  All file separators in the file are '/' not '' b/c it confuses
 *  java. Added more comments to deal with confusion of DEFAULT
 *  property.
 *
 *  Revision 1.4  2002/03/12 21:16:42  pfpeterson
 *  Removed log file line since it is no longer used by anyone.
 *
 *  Revision 1.3  2002/03/07 22:14:41  pfpeterson
 *  Hopefully will work better with windows
 *
 *  Revision 1.2  2002/03/04 20:31:02  pfpeterson
 *  Default properties file comments out more lines if ISAW is not found
 *  in the classpath.
 *
 *  Revision 1.1  2002/02/25 23:31:50  pfpeterson
 *  Extracted the writing of default properties file from Isaw.java and
 *  set new values to be more reasonable.
 *
 */

 
package IsawGUI;

import java.io.*;
import java.util.Properties;
import DataSetTools.util.*;

 /**
  * The main class for ISAW. It is the GUI that ties together the 
  * DataSetTools, IPNS, ChopTools and graph packages.
  */
public class DefaultProperties{
    private String     IsawHome;
    private String     UserHome;
    private Properties current_props;

    private static final String separator = "/";
    private static final String newline   = "\n";

    /** **************************************************************
     *  Construct a new DefaultProperties object that will set 
     *  default properties, that have NOT already been set by the
     *  specified properties object.
     *
     *  @param current_props  The properties that have been previously
     *                        set and should not be overridden by 
     *                        default.
     */
    public DefaultProperties( Properties current_props ){
    
        if ( current_props != null )
          this.current_props = current_props;
        else
          this.current_props = new Properties();

        String temp = System.getProperty("user.home");
        if( temp != null )
          UserHome = FilenameUtil.setForwardSlash(temp);

        temp=getIsawHome();
        if( temp != null )
          IsawHome = FilenameUtil.setForwardSlash(temp);
        else
          IsawHome = UserHome+separator+"ISAW";

        //System.out.println(UserHome+","+IsawHome);
    }
 
    /*****************************************************************
     * Write out the default properties to the specified file.
     */
    public boolean write( String propsFile )
    {
        System.out.println("Creating a new Properties file: "
                           +propsFile);
        try
        {
            FileWriter fos = new FileWriter(propsFile);
            fos.write( defaultString(current_props) );
            fos.flush();
            fos.close();
        }
        catch(Exception e)
        {
            SharedData.addmsg("Could not write file:"+e);
            return false;
        }

        return load(propsFile);
    }

    /**
     * Public method for loading a Properties file. With caller
     * defined filename.
     */
    private boolean load(String propsFile){
        try{
            Properties isawProp = new Properties(System.getProperties());
            FileInputStream fis = new FileInputStream(propsFile);
            isawProp.load(fis);
            System.setProperties(isawProp);
            fis.close();
        }catch(Exception e){
            SharedData.addmsg("Could not read in file:"+e);
            return false;
        }
        return true;
    }

    /** **************************************************************
     * Find the location of ISAW.
     */
    private String getIsawHome(){
        String className=null;
        String classFile=null;
        boolean injar=false;
        int index=0;

        className='/'+this.getClass().getName().replace('.','/')+".class";
        classFile=this.getClass().getResource(className).toString();
        classFile=FilenameUtil.URLSpacetoSpace( classFile);
        if(classFile==null) return "DEFAULT";
        injar=classFile.startsWith("jar:");
        index=classFile.indexOf("file:");
        classFile=classFile.substring(index+5,classFile.length());
        index=classFile.indexOf(className);
        classFile=classFile.substring(0,index);
        classFile=FilenameUtil.setForwardSlash(classFile);
        if(injar){
            index=classFile.lastIndexOf("/");
            if(index>=0) classFile=classFile.substring(0,index);
        }
        if(classFile.indexOf(":")>0 && classFile.startsWith("/")){
            classFile=classFile.substring(1,classFile.length());
        }
        return classFile;
    }


    /**
     * Build the default string for the properties file
     */
    private String defaultString( Properties current_props )
    {
        StringBuffer rs=new StringBuffer();
        String defstr="";
        String eol=newline;

        rs.append("#").append(eol)
          .append("# This is your ISAW properties file ... ").append(eol)
          .append("# THE DIRECTORIES ON YOUR SYSTEM MUST").append(eol)
          .append("# MATCH THOSE LISTED IN THIS FILE").append(eol)
          .append("#").append(eol)
          .append("# The '#' symbol denotes a commented line").append(eol)
          .append("#").append(eol)
          .append(eol)
          .append("#").append(eol)
          .append("# Directory Options");
        
        if(IsawHome.equals("DEFAULT")){
            rs.append(" - replace the word DEFAULT with").append(eol)
              .append("# the location of the ISAW home directory");
        }
        
        rs.append(eol).append("#").append(eol);

        if ( current_props.getProperty("ISAW_HOME") != null )
          rs.append("#");
        rs.append("ISAW_HOME=").append(IsawHome).append(eol);
        
        rs.append("#GROUP_HOME=").append(UserHome).append(separator)
          .append("ipns").append(eol);
        
        rs.append("#GROUP_NAME=").append("My Scripts").append(eol);
        
        if(IsawHome.equals("DEFAULT"))
        {
            defstr="#";
        }
        
        if ( current_props.getProperty("Help_Directory") != null )
          rs.append("#");
        rs.append(defstr).append("Help_Directory=").append(IsawHome)
          .append(separator).append("IsawHelp").append(eol);
        
        if ( current_props.getProperty("Script_Path") != null )
          rs.append("#");
        rs.append(defstr).append("Script_Path=").append(IsawHome)
          .append(separator).append("Scripts").append(eol);
        
        if ( current_props.getProperty("Docs_Directory") != null )
          rs.append("#");
        rs.append(defstr).append("Docs_Directory=").append(IsawHome)
          .append(separator).append("docs").append(separator)
          .append("html").append(eol);
        
        if ( current_props.getProperty("Data_Directory") != null )
          rs.append("#");
        rs.append(defstr).append("Data_Directory=").append(IsawHome)
          .append(separator).append("SampleRuns").append(eol);
        rs.append(eol);
        
        rs.append("#").append(eol);
        rs.append("# Command to start browser for Help system").append(eol);
        rs.append("#").append(eol);
        
        rs.append("#PREFERRED_BROWSER=/usr/bin/mozilla").append(eol);
        rs.append(eol);
        rs.append("#").append(eol);
        rs.append("# Default data file extension, hdf or ipns").append(eol);
        rs.append("#").append(eol);
        rs.append("#Default_Ext=hdf").append(eol);
        rs.append(eol);
        rs.append("# Slab size for reading NeXus files").append(eol);
        rs.append("# NexusSlabSize = 8NexusSlabSized(eol).append(eol);
        rs.append("#").append(eol);
        rs.append("# Live Data Server Options").append(eol);
        rs.append("#").append(eol);
        rs.append("Inst1_Name=Test:( UW-Stout Live Data Simulator )")
          .append(eol);
        rs.append("Inst1_Path=isaw.mscs.uwstout.edu;6088").append(eol);
        rs.append(eol);
        rs.append("#").append(eol);
        rs.append("# Remote Data Server Options").append(eol);
        rs.append("#").append(eol);
        rs.append("IsawFileServer1_Name=Test:( UW-Stout ISAW File Server )")
          .append(eol);
        rs.append("IsawFileServer1_Path=isaw.mscs.uwstout.edu;6089")
          .append(eol);
        rs.append("#").append(eol);
        rs.append("# Screen Size in percentage (<=1) or pixels (>1)")
          .append(eol);
        rs.append("#").append(eol);
        rs.append("Isaw_Width=0.8").append(eol);
        rs.append("Isaw_Height=0.4").append(eol);
        rs.append("Tree_Width=0.2").append(eol);
        rs.append("Status_Height=0.2").append(eol);
        rs.append(eol);
        
        rs.append("#").append(eol);
        rs.append("# PixelDepthScale=256 for ATI FireGL T2 drivers on nw8000")
          .append(eol);
        rs.append("#").append(eol);
        rs.append("#PixelDepthScale=256").append(eol);
        rs.append(eol);
        rs.append("#").append(eol);
        rs.append("# Viewer Options").append(eol);
        rs.append("#").append(eol);
        rs.append("Default_Instrument=HRCS").append(eol);
        rs.append("ColorScale=Heat 2").append(eol);
        rs.append("ViewDetectors=Filled").append(eol);
        rs.append("#ViewGroups=Medium").append(eol);
        rs.append("#RebinFlag=false").append(eol);
        rs.append("#HScrollFlag=false").append(eol);
        rs.append("#ViewAltitudeAngle=20.0").append(eol);
        rs.append("#ViewAzimuthAngle=45.0").append(eol);
        rs.append("#ViewDistance=4.5").append(eol);
        rs.append("#Brightness=40").append(eol);
        rs.append("#Auto-Scale=0.0").append(eol);
        rs.append("#ShowWCToolTip=true").append(eol);
        rs.append("#XRange_Time(us)_min=0.0").append(eol);
        rs.append("#XRange_Time(us)_max=0.0").append(eol);
        rs.append("#YRange_Counts_min=0.0").append(eol);
        rs.append("#YRange_Counts_max=0.0").append(eol);
        rs.append(eol);
        rs.append("#").append(eol);
        rs.append("# Wizard Options").append(eol);
        rs.append("#").append(eol);
        rs.append("WIZARD_WIDTH=700").append(eol);
        rs.append("WIZARD_HEIGHT=600").append(eol);
        rs.append("#NSavedFiles=0").append(eol);
        rs.append("#ShortSavedFilename=false").append(eol);
            
        return rs.toString();
    }

    /** **************************************************************
     * Testing routine for the class.
     */
    public static void main( String[] args ){
        DefaultProperties newguy = new DefaultProperties( null );
        //System.out.println(newguy.defaultString());
        String propsFile = System.getProperty("user.home")+"/IsawProps.dat";
        newguy.write( propsFile );
    }
}
