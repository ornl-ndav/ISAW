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
 *  Revision 1.18  2003/04/21 16:13:38  pfpeterson
 *  Added SCD Live Data information, privatized load(String), and removed unused code.
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
import java.io.IOException; 
import java.util.Properties;
import DataSetTools.util.*;

 /**
  * The main class for ISAW. It is the GUI that ties together the 
  * DataSetTools, IPNS, ChopTools and graph packages.
  */
public class DefaultProperties{
    private String IsawHome;
    private String UserHome;
    private String IsawProps;
    private Properties isawProp;

    private static final String separator        = "/";
    private static final String newline          = "\n";

    /** **************************************************************
     * Constructor that does all of the work for you. Will find a
     * working version of ISAW to base the directory information on.
     */
    public DefaultProperties(){
        String temp=System.getProperty("user.home");
        if(temp!=null){
            UserHome=FilenameUtil.setForwardSlash(temp);
        }
        temp=getIsawHome();
        if(temp!=null){
            IsawHome=FilenameUtil.setForwardSlash(temp);
        }else{
            IsawHome=UserHome+separator+"ISAW";
        }
        //System.out.println(UserHome+","+IsawHome);
        IsawProps=defaultString();
    }
 
    /** **************************************************************
     * Write out the properties file in the user's home directory then
     * read it in to get all of the properties into memory.
     */
    public boolean write(){
        String propsFile=UserHome+"/IsawProps.dat";
        System.out.println("Creating a new Properties file: "
                           +propsFile);
        try{
            FileWriter fos = new FileWriter(propsFile);
            fos.write(IsawProps);
            fos.flush();
            fos.close();
        }catch(Exception e){
            SharedData.addmsg("Could not write file:"+e);
            return false;
        }

        if(isawProp==null){
            //System.out.println("isawProp was null");
            isawProp=new Properties(System.getProperties());
        }
        return load(propsFile);
    }

    /**
     * Public method for loading a Properties file. With caller
     * defined filename.
     */
    private boolean load(String propsFile){
        try{
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
     * Resolve the full path for a directory (get rid of ~ and .)
     */
    private String resolveDir(String origDir){
        String dir=(new File(origDir)).getAbsolutePath();
        dir=FilenameUtil.setForwardSlash(dir);
        int index=dir.indexOf("/.");
        if(index>0){
            dir=dir.substring(0,index);
        }

        return dir;
    }

    /**
     * Build the default string for the properties file
     */
    private String defaultString(){
        StringBuffer rs=new StringBuffer();
        String defstr="";
        String eol=newline;

        rs.append("#").append(eol)
            .append("# This is your ISAW properties file ... ").append(eol)
            .append("# THE DIRECTORIES ON YOUR SYSTEM MUST").append(eol)
            .append("#   MATCH THOSE LISTED IN THIS FILE").append(eol)
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
        rs.append(eol).append("#").append(eol)
            .append("ISAW_HOME=").append(IsawHome).append(eol)
            .append("#GROUP_HOME=").append(UserHome).append(separator)
            .append("ipns").append(eol);
        if(IsawHome.equals("DEFAULT")){
            defstr="#";
        }
        rs.append(defstr).append("Help_Directory=").append(IsawHome)
            .append(separator).append("IsawHelp").append(eol)
            .append(defstr).append("Script_Path=").append(IsawHome)
            .append(separator).append("Scripts").append(eol)
            .append(defstr).append("Docs_Directory=").append(IsawHome)
            .append(separator).append("docs").append(separator)
            .append("html").append(eol)
            .append(defstr).append("Data_Directory=").append(IsawHome)
            .append(separator).append("SampleRuns").append(eol)
            .append(defstr).append("Instrument_Macro_Path=").append(IsawHome)
            .append(eol)
            .append(defstr).append("User_Macro_Path=").append(IsawHome)
            .append(eol)
            .append(eol)
            .append("#").append(eol)
            .append("# Live Data Server Options").append(eol)
            .append("#").append(eol)
            .append("Inst1_Name=HRMECS").append(eol)
            .append("Inst1_Path=hrmecs.pns.anl.gov;6088").append(eol)
            .append("Inst2_Name=SCD").append(eol)
            .append("Inst2_Path=scd2.pns.anl.gov;6088").append(eol)
            .append("Inst3_Name=QUIP").append(eol)
            .append("Inst3_Path=vulcan.pns.anl.gov;6088").append(eol)
            .append(eol)
            .append("#").append(eol)
            .append("# Remote Data Server Options").append(eol)
            .append("#").append(eol)
            .append("IsawFileServer1_Name=IPNS(hrmecs)").append(eol)
            .append("IsawFileServer1_Path=hrmecs.pns.anl.gov;6089").append(eol)
            .append("IsawFileServer2_Name=Test(dmikk-Isaw)").append(eol)
            .append("IsawFileServer2_Path=dmikk.mscs.uwstout.edu;6089")
            .append(eol)
            .append("NDSFileServer1_Name=Test(dmikk-NDS)").append(eol)
            .append("NDSFileServer1_Path=dmikk.mscs.uwstout.edu;6008")
            .append(eol)
            .append(eol)
            .append("#").append(eol)
            .append("# Screen Size in percentage (<=1) or pixels (>1)")
            .append(eol)
            .append("#").append(eol)
            .append("Isaw_Width=0.8").append(eol)
            .append("Isaw_Height=0.4").append(eol)
            .append("Tree_Width=0.2").append(eol)
            .append("Status_Height=0.2").append(eol)
            .append(eol)
            .append("#").append(eol)
            .append("# Viewer Options").append(eol)
            .append("#").append(eol)
            .append("Default_Instrument=HRCS").append(eol)
            .append("ColorScale=Optimal").append(eol)
            .append("ViewDetectors=Filled").append(eol)
            .append("#ViewGroups=Medium").append(eol)
            .append("#RebinFlag=false").append(eol)
            .append("#HScrollFlag=false").append(eol)
            .append("#ViewAltitudeAngle=20.0").append(eol)
            .append("#ViewAzimuthAngle=45.0").append(eol)
            .append("#ViewDistance=4.5").append(eol)
            .append("#Brightness=40").append(eol)
            .append("#Auto-Scale=0.0").append(eol);

      /* This causes more problems with nexus than it fixes. These
         lines are taken from the original code that was inside Isaw.java.
	 if ( windows ){
	 opw.write("neutron.nexus.JNEXUSLIB="+ipath+"lib/jnexus.dll");
	 System.setProperty("neutron.nexus.JNEXUSLIB",ipath+"lib/jnexus.dll");
	 opw.write("\n");   
	 } */

        return rs.toString();
    }

    /** **************************************************************
     * Testing routine for the class.
     */
    public static void main( String[] args ){
        DefaultProperties newguy 
            = new DefaultProperties();
        //System.out.println(newguy.defaultString());
        newguy.write();
    }
}
