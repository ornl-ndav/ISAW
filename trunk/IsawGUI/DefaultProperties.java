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
 * Contact : Peter Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 S. Cass Avenue, Bldg 360
 *           Argonne, IL 60440
 *           USA
 *
 * 
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
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

    private static String separator        = "/";
    private static String newline          = "\n";
    private static final SharedData shared = new SharedData();

    /** **************************************************************
     * Constructor that does all of the work for you. Will find a
     * working version of ISAW to base the directory information on.
     */
    public DefaultProperties(){
        String temp=System.getProperty("user.home");
        if(temp!=null){
            UserHome=StringUtil.fixSeparator(temp);
        }
        temp=getIsawHome();
        if(temp!=null){
            IsawHome=StringUtil.fixSeparator(temp);
        }else{
            IsawHome=UserHome+separator+"ISAW";
        }
        //System.out.println(UserHome+","+IsawHome);
        IsawProps=defaultString();
    }
    
    /**
     * Constructor that takes the Properties object to setup.
     */
    public DefaultProperties( Properties ISAWPROP ){
        this();
        isawProp=ISAWPROP;
    }

    /**
     * Constructor which assumes that ISAW is installed in the users
     * home directory.
     */
    public DefaultProperties( String USERHOME ){
        this();
        UserHome=StringUtil.fixSeparator(USERHOME);
        IsawHome=USERHOME+separator+"ISAW";
        System.out.println(UserHome+","+IsawHome);
        IsawProps=defaultString();
    }

    /**
     * Constructor that allows all information to be specified by the
     * calling program.
     */
    public DefaultProperties( String USERHOME, String ISAWHOME){
        this(USERHOME);
        IsawHome=StringUtil.fixSeparator(ISAWHOME);
        System.out.println(UserHome+","+IsawHome);
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
            shared.status_pane.add("Could not write file:"+e);
            return false;
        }

        if(isawProp==null){
            //System.out.println("isawProp was null");
            isawProp=new Properties(System.getProperties());
        }
        return load();
    }

    /**
     * Public method for loading a Properties file. Filename is
     * constructed using UserHome.
     */
    public boolean load(){
        String propsFile=UserHome+"/IsawProps.dat";
        return load(propsFile);
    }

    /**
     * Public method for loading a Properties file. With caller
     * defined filename.
     */
    public boolean load(String propsFile){
        try{
            FileInputStream fis = new FileInputStream(propsFile);
            isawProp.load(fis);
            System.setProperties(isawProp);
            fis.close();
        }catch(Exception e){
            shared.status_pane.add("Could not read in file:"+e);
            return false;
        }
        return true;
    }


    /** **************************************************************
     * Find the location of ISAW.
     */
    private String getIsawHome(){
        //System.out.println("in getIsawHome()");
        String pathsep=System.getProperty("path.separator");
        String classpath=System.getProperty("java.class.path");
        String dir;
        int index=classpath.indexOf(pathsep);
        while( index>=0 ){
            dir=classpath.substring(0,index);
            dir=FilenameUtil.fixSeparator(dir);
            //System.out.println("dir:"+dir);
            classpath=classpath.substring(index+1,classpath.length());
            if(dir.endsWith("Isaw.jar")){
                System.out.println("Isaw found: "+dir);
                index=dir.indexOf("Isaw.jar")-1;
                if(index>0){
                    return dir.substring(0,dir.indexOf("Isaw.jar")-1);
                }
            }else{
                String isawExec
                    =separator+"IsawGUI"+separator+"Isaw.class";
                File isIsaw=new File(dir+isawExec);
                if(isIsaw.exists()){
                    System.out.println("Isaw found: "+isIsaw);
                    return dir;
                }
            }
            index=classpath.indexOf(pathsep);
            if(index<0){
                System.err.println("WARNING: Could not find ISAW "
                                   +"- Edit Properties File");
                return "DEFAULT";
            }
        }
        
        System.err.println("WARNING: Could not find ISAW "
                           +"- Edit Properties File");
        return "DEFAULT";
    }

    /**
     * Build the default string for the properties file
     */
    private String defaultString(){
        String rs;

        rs=  "#"+newline
            +"# This is your ISAW properties file ... "+newline
            +"# THE DIRECTORIES ON YOUR SYSTEM MUST MATCH THOSE"
                                +" LISTED IN THIS FILE"+newline
            +"#"+newline
            +"# The '#' symbol denotes a commented line"+newline
            +"#"+newline
            +newline
            +"#"+newline
            +"# Directory Options"+newline
            +"#"+newline
            +"ISAW_HOME="+IsawHome+newline;
        if(IsawHome.equals("DEFAULT")){
            rs=rs+"#GROUP_HOME="+UserHome+separator+"ipns"+newline
                +"#Help_Directory="+IsawHome+separator+"IsawHelp"+newline
                +"#Script_Path="+IsawHome+separator+"Scripts"+newline
                +"#Docs_Directory="+IsawHome+separator+"docs"+separator
                +"html"+newline
                +"#Data_Directory="+IsawHome+separator+"SampleRuns"+newline
                +"#Log_Directory="+IsawHome+separator+"SampleRuns"+newline
                +"#Instrument_Macro_Path="+IsawHome+newline
                +"#User_Macro_Path="+IsawHome+newline
                +newline;
        }else{
            rs=rs+"#GROUP_HOME="+UserHome+separator+"ipns"+newline
                +"Help_Directory="+IsawHome+separator+"IsawHelp"+newline
                +"Script_Path="+IsawHome+separator+"Scripts"+newline
                +"Docs_Directory="+IsawHome+separator+"docs"+separator
                +"html"+newline
                +"Data_Directory="+IsawHome+separator+"SampleRuns"+newline
                +"Log_Directory="+IsawHome+separator+"SampleRuns"+newline
                +"Instrument_Macro_Path="+IsawHome+newline
                +"User_Macro_Path="+IsawHome+newline
                +newline;
        }
        rs=rs+"#"+newline
            +"# Live Data Server Options"+newline
            +"#"+newline
            +"Inst1_Name=HRMECS"+newline
            +"Inst1_Path=zeus.pns.anl.gov;6088"+newline
            +newline
            +"#"+newline
            +"# Remote Data Server Options"+newline
            +"#"+newline
            +"IsawFileServer1_Name=IPNS(zeus)"+newline
            +"IsawFileServer1_Path=zeus.pns.anl.gov;6089"+newline
            +"IsawFileServer2_Name=Test(dmikk-Isaw)"+newline
            +"IsawFileServer2_Path=dmikk.mscs.uwstout.edu;6089"+newline
            +"NDSFileServer1_Name=Test(dmikk-NDS)"+newline
            +"NDSFileServer1_Path=dmikk.mscs.uwstout.edu;6008"+newline
            +newline
            +"#"+newline
            +"# Viewer Options"+newline
            +"#"+newline
            +"Default_Instrument=HRCS"+newline
            +"ColorScale=Optimal"+newline
            +"#RebinFlag=false"+newline
            +"#HScrollFlag=false"+newline
            +"#ViewAltitudeAngle=20.0"+newline
            +"#ViewAzimuthAngle=45.0"+newline
            +"#ViewDistance=4.5"+newline
            +"#ViewGroups=Medium"+newline
            +"#ViewDetectors=SOLID"+newline
            +"#Brightness=40"+newline
            +"#Auto-Scale=0.0"+newline
            ;

      /* This causes more problems with nexus than it fixes. These
         lines are taken from the original code that was inside Isaw.java.
	 if ( windows ){
	 opw.write("neutron.nexus.JNEXUSLIB="+ipath+"lib/jnexus.dll");
	 System.setProperty("neutron.nexus.JNEXUSLIB",ipath+"lib/jnexus.dll");
	 opw.write("\n");   
	 } */

        return rs;
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
    

   /**
    * entry point for the ISAW application.
    */
    public static void oldmain( String[] args ){
        Properties isawProp = new Properties(System.getProperties());
        String path = System.getProperty("user.home")+"\\";
        path = StringUtil.fixSeparator(path);
        //boolean windows = isWindowsPlatform();
        
        try{
            FileInputStream input = new FileInputStream(path + "IsawProps.dat" );
            isawProp.load( input );
            System.setProperties(isawProp);  
            input.close();
        }catch( IOException ex ){
            System.out.println(
                               "Properties file could not be loaded due to error :" +ex );
            
            System.out.println(
                               "Creating a new Properties file called IsawProps in the directory " +
                               System.getProperty("user.home") );
            
            String npath = System.getProperty("user.home")+"\\";
            String ipath = System.getProperty("user.dir")+"\\";
            npath = StringUtil.fixSeparator(npath);
            npath = npath.replace('\\','/');
            
            ipath = StringUtil.fixSeparator(ipath);
            ipath = ipath.replace('\\','/');
            
            File f= new File( npath + "IsawProps.dat" );
            
            try{
                FileOutputStream op= new FileOutputStream(f);
                OutputStreamWriter opw = new OutputStreamWriter(op);
                opw.write("#This is a properties file");
                opw.write("\n");
                opw.write("Help_Directory="+ipath+"IsawHelp/");
                System.setProperty("Help_Directory",  ipath+"IsawHelp/");
                opw.write("\n");
                opw.write("Script_Path="+ipath+"Scripts/");
                System.setProperty("Script_Path",ipath+"Scripts/");
                opw.write("\n");
                opw.write("Data_Directory="+ipath+"SampleRuns/");
                System.setProperty("Data_Directory",ipath+"SampleRuns/");
                opw.write("\n");
                opw.write("Default_Instrument=HRCS");
                System.setProperty("Default_Instrument","HRCS");
                opw.write("\n");
                opw.write("Instrument_Macro_Path="+ipath);
                System.setProperty("Instrument_Macro_Path",ipath);
                opw.write("\n");
                opw.write("User_Macro_Path="+ipath);
                System.setProperty("User_Macro_Path",ipath);
                opw.write("\n");
                
                opw.write("ISAW_HOME="+ipath);
                System.setProperty("ISAW_HOME",ipath);
                
                opw.write("\n");
                opw.write("Inst1_Name=HRMECS");
                opw.write("\n"); 
                opw.write("Inst1_Path=zeus.pns.anl.gov;6088");
                System.setProperty("Inst1_Name", "HRMECS");
                System.setProperty("Inst1_Path", "zeus.pns.anl.gov;6088");
                opw.write("\n");  
                
                opw.write("Inst2_Name=GPPD");
                opw.write("\n"); 
                opw.write("Inst2_Path=gppd-pc.pns.anl.gov;6088");
                System.setProperty("Inst2_Name", "GPPD");
                System.setProperty("Inst2_Path", "gppd-pc.pns.anl.gov;6088");
                opw.write("\n");  
                
                opw.write("IsawFileServer1_Name=IPNS(zeus)");
                opw.write("\n"); 
                opw.write("IsawFileServer1_Path=zeus.pns.anl.gov;6089");
                System.setProperty("IsawFileServer1_Name", "IPNS");
                System.setProperty("IsawFileServer1_Path", "zeus.pns.anl.gov;6089");
                opw.write("\n");
                
                opw.write("IsawFileServer2_Name=Test(dmikk-Isaw)");
                opw.write("\n"); 
                opw.write("IsawFileServer2_Path=dmikk.mscs.uwstout.edu;6091");
                System.setProperty("IsawFileServer2_Name", "Test");
                System.setProperty("IsawFileServer2_Path", "dmikk.mscs.uwstout.edu;6091");
                opw.write("\n");
                
                opw.write("NDSFileServer1_Name=Test(dmikk-NDS)");
                opw.write("\n"); 
                opw.write("NDSFileServer1_Path=dmikk.mscs.uwstout.edu;6008");
                System.setProperty("NDSFileServer1_Name", "Test");
                System.setProperty("NDSFileServer1_Path", "dmikk.mscs.uwstout.edu;6008");
                opw.write("\n");
                
                opw.write("ColorScale=Heat 2");
                System.setProperty("ColorScale", "Heat 2");
                opw.write("\n");  
                
                opw.write("RebinFlag=false");
                System.setProperty("RebinFlag", "false");
                opw.write("\n"); 
                
                opw.write("HScrollFlag=false");
                System.setProperty("HScrollFlag", "false");
                opw.write("\n"); 
                
                opw.write("ViewAltitudeAngle=1.0");
                System.setProperty("ViewAltitudeAngle", "1.0");
                opw.write("\n"); 
                
                opw.write("ViewAzimuthAngle=180");
                System.setProperty("ViewAzimuthAngle", "180");
                opw.write("\n"); 
                
                opw.write("ViewDistance=0.9");
                System.setProperty("ViewDistance", "0.9");
                opw.write("\n"); 
                
                opw.write("ViewGroups=NOT DRAWN");
                System.setProperty("ViewGroups", "NOT DRAWN");
                opw.write("\n"); 
                
                opw.write("ViewDetectors=SOLID");
                System.setProperty("ViewDetectors", "SOLID");
                opw.write("\n"); 
                
                opw.write("Brightness=40");
                System.setProperty("Brightness", "40");
                opw.write("\n"); 
                
                opw.write("Auto-Scale=0.0");
                System.setProperty("Auto-Scale", "0.0");
                opw.write("\n"); 
                
      /* This causes more problems with nexus than it fixes
	 if ( windows ){
	 opw.write("neutron.nexus.JNEXUSLIB="+ipath+"lib/jnexus.dll");
	 System.setProperty("neutron.nexus.JNEXUSLIB",ipath+"lib/jnexus.dll");
	 opw.write("\n");   
	 } */


/* 
        opw.write("Inst2_Name=LRMECS");
        opw.write("\n");  
        opw.write("Inst2_Path=webproject-4.pns.anl.gov");
        System.setProperty("Inst2_Name", "LRMECS");
p        System.setProperty("Inst2_Path", "webproject-4.pns.anl.gov");
        opw.write("\n");  
 
        opw.write("Inst3_Name=GPPD");
        opw.write("\n");  
        opw.write("Inst3_Path=gppd-pc.pns.anl.gov");
        System.setProperty("Inst3_Name", "GPPD");
        System.setProperty("Inst3_Path", "gppd-pc.pns.anl.gov");
        opw.write("\n");  
 
        opw.write("Inst4_Name=SEPD");
        opw.write("\n");  
        opw.write("Inst4_Path=dmikk.mscs.uwstout.edu");
        System.setProperty("Inst4_Name", "SEPD");
        System.setProperty("Inst4_Path", "dmikk.mscs.uwstout.edu");
        opw.write("\n");
 
        opw.write("Inst5_Name=SAD");
        opw.write("\n");
        opw.write("Inst5_Path=webproject-4.pns.anl.gov");
        System.setProperty("Inst5_Name", "SAD");
        System.setProperty("Inst5_Path", "webproject-4.pns.anl.gov");
        opw.write("\n");    
   
        opw.write("Inst6_Name=SAND");
        opw.write("\n");      
        opw.write("Inst6_Path=webproject-4.pns.anl.gov");
        System.setProperty("Inst6_Name", "SAND");
        System.setProperty("Inst6_Path", "webproject-4.pns.anl.gov");
        opw.write("\n");
       
        opw.write("Inst7_Name=SCD");
        opw.write("\n");      
        opw.write("Inst7_Path=webproject-4.pns.anl.gov");
        System.setProperty("Inst7_Name", "SCD");
        System.setProperty("Inst7_Path", "webproject-4.pns.anl.gov");
        opw.write("\n");  
     
        opw.write("Inst8_Name=GLAD");
        opw.write("\n");      
        opw.write("Inst8_Path=webproject-4.pns.anl.gov");
        System.setProperty("Inst8_Name", "GLAD");
        System.setProperty("Inst8_Path", "webproject-4.pns.anl.gov");
        opw.write("\n"); 
      
        opw.write("Inst9_Name=HIPD");
        opw.write("\n"); 
        opw.write("Inst9_Path=webproject-4.pns.anl.gov");
        System.setProperty("Inst9_Name", "HIPD");
        System.setProperty("Inst9_Path", "webproject-4.pns.anl.gov");
        opw.write("\n");
 
        opw.write("Inst10_Name=POSY1");
        opw.write("\n");      
        opw.write("Inst10_Path=webproject-4.pns.anl.gov");
        System.setProperty("Inst10_Name", "POSY1");
        System.setProperty("Inst10_Path", "webproject-4.pns.anl.gov");
        opw.write("\n");  
     
        opw.write("Inst11_Name=POSY2");
        opw.write("\n");  
        opw.write("Inst11_Path=webproject-4.pns.anl.gov");
        System.setProperty("Inst11_Name", "POSY2");
        System.setProperty("Inst11_Path", "webproject-4.pns.anl.gov");
        opw.write("\n");  
                
        opw.write("Inst12_Name=QENS");
        opw.write("\n");                 
        opw.write("Inst12_Path=webproject-4.pns.anl.gov");
        System.setProperty("Inst12_Name", "QENS");
        System.setProperty("Inst12_Path", "webproject-4.pns.anl.gov");
        opw.write("\n");  
                
        opw.write("Inst13_Name=CHEXS");
        opw.write("\n");                 
        opw.write("Inst13_Path=webproject-4.pns.anl.gov");
        System.setProperty("Inst13_Name", "CHEXS");
        System.setProperty("Inst13_Path", "webproject-4.pns.anl.gov");
        opw.write("\n"); 
*/                    
                opw.flush();
                opw.close(); 
            }catch( Exception d ){
            }
        }
    }
}
