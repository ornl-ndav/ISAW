 /*
 * File:  Isawinstaller.java
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
 * For further information, see http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 * 
 * $Log$
 * Revision 1.13  2002/09/27 20:04:18  pfpeterson
 * Small modification on exec script written for sun os.
 *
 * Revision 1.12  2002/08/16 15:18:56  pfpeterson
 * Fixed bug where you couldn't install from a directory with
 * spaces in the name.
 *
 * Revision 1.11  2002/08/15 18:40:41  pfpeterson
 * Fixed the windows and mac batch file creation.
 *
 * Revision 1.10  2002/05/29 21:14:57  pfpeterson
 * Now determines the name of the jar file through reflection. Also
 * added functionality for testing which uses the information as
 * well.
 *
 * Revision 1.9  2002/04/12 15:17:53  pfpeterson
 * Prints message about moving properties file only when sucessful.
 *
 * Revision 1.8  2002/04/04 20:48:50  pfpeterson
 * changed command line switch to '-mx128m'.
 *
 * Revision 1.7  2002/03/26 20:47:08  pfpeterson
 * More mac updates:
 * - Set default file extension to 'applescript' (uncompiled code).
 * - Extension for compiled code is 'scpt'.
 *
 * Revision 1.6  2002/03/26 16:42:12  pfpeterson
 * Changed batch file to be an apple script.
 *
 * Revision 1.5  2002/03/25 23:46:52  pfpeterson
 * Changed exiting information dialog. Location of java no longer
 * needed for mac clients.
 *
 * Revision 1.4  2002/03/04 20:29:54  pfpeterson
 * Updated mac support.
 *
 * Revision 1.3  2002/02/18 21:57:09  pfpeterson
 * Fixed nexus and windows problem.
 *
 * Revision 1.2  2002/02/18 16:33:27  pfpeterson
 * Changes the permission of Isaw_exec.sh to executable using the "chmod +x"
 * system call. New line character is now System.getProperty("line.separator").
 *
 * Revision 1.1  2002/02/13 20:42:19  pfpeterson
 * First version of unified installer in CVS.
 *
 *
 */

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.zip.*;
import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;

/* 
 * This installer is based on the ZipSelfExtractor utility by Z. Steve
 * Jin and John D. Mitchell as found at
 * http://www.javaworld.com/javaworld/javatips/jw-javatip120.html.
 *
 * The intent is to find information about where to place the new
 * version of ISAW then put it there.
 */
public class IsawInstaller extends JFrame
{
    private String myClassName;
    private javax.swing.Timer timer;
    static String MANIFEST = "META-INF/MANIFEST.MF";
    static JFrame mw;
    private Boolean injar;

    // global variables so text can be changed
    JTextField location;
    String     jarFileName;
    JTextField os;
    JTextField batch;
    JTextField java;
    String     operating_system;

    // buttons
    JButton installDir,
   	    batchFile,
	    javaLoc,
	    cancelBut,
	    installBut;

    // progress bar
    JProgressBar progress;

    // fields
    private static final String WIN_ID      = "windows";
    private static final String LIN_ID      = "linux";
    private static final String SUN_ID      = "sunos";
    private static final String MAC_ID      = "mac";
    private static final String UNKNOWN_ID  = "unknown";

    // button and label stuff
    private static final String UNPACK_ARCH = "Unpack Archive:";
    private static final String INSTALL_LOC = "Install Directory:";
    private static final String BATCH_FILE  = "Batch File:";
    private static final String JAVA_LOC    = "Java Location:";
    private static final String CANCEL_BUT  = "Cancel";
    private static final String CHANGE      = "Change";
    private static final String START_BUT   = "Install";
    private static final String NO_BATCH    = "not creating batch file";
    private static final String NA          = "n/a";

    /* =========================== main =========================== */
    /** 
     * This method is what is called when the jar is executed assuming
     * that the MANIFEST points at this class. The technique is to
     * draw the installer with meaningful default values (depending on
     * operating system) then extract the archive and create the batch
     * file once the install button is pressed.
     */
    public static void main(String[] args){
	IsawInstaller zse = new IsawInstaller();
	
	// get the operating system
	zse.getOS();
	
	// find the name of archive
        zse.jarFileName=zse.getJarFileName();
	
	// set up the GUI
	zse.init();
    } // end of main

    /* ======================== constructor ======================= */
    /**
     * The constructor is empty.
     */
    public IsawInstaller(){
    }

    /**
     * Method to determine if running from a jar file.
     */
    private boolean inJar(){
        if(injar==null){
            String className=this.getClass().getName().replace('.','/');
            String classJar=this.getClass().getResource("/"+className
                                                        +".class").toString();
            if(classJar.startsWith("jar:")){
                injar=Boolean.TRUE;
            }else{
                injar=Boolean.FALSE;
            }
        }

        return injar.booleanValue();
    }

    /* ====================== operating system ==================== */
    /**
     * Determine the operating system. If the operating system is
     * something other than WIN_ID or LIN_ID then it defaults to
     * UNKNOWN_ID. This information is used to build the GUI with the
     * appropriate options available and produce the correct system
     * dependent batch file.
     */
    private String getOS(){
	String osS=System.getProperty("os.name");
        osS=osS.trim();
	if( osS != null ){
            int index=osS.indexOf(" ");
            if(index>0){
                osS=osS.substring(0,index);
            }
	    osS=osS.toLowerCase();
	    if(osS.startsWith(WIN_ID)){
		operating_system=WIN_ID;
	    }else if(osS.startsWith(LIN_ID)){
		operating_system=LIN_ID;
            }else if(osS.startsWith(SUN_ID)){
                operating_system=SUN_ID;
            }else if(osS.startsWith(MAC_ID)){
                operating_system=MAC_ID;
	    }else{
		System.err.println("OS ("+osS+") not known");
		operating_system=UNKNOWN_ID;
	    }
	}else{
	    operating_system=UNKNOWN_ID;
	}
	//operating_system=WIN_ID;
	//operating_system=MAC_ID;
	//operating_system=UNKNOWN_ID;

	return operating_system;
    }

    /* =================== installation directory ================= */
    /**
     * This method sets the current_working_directory/ISAW as the
     * default installation directory.
     */
    private String getDefaultDir(){
	File result=new File(".","ISAW");
	try{
	    result=result.getCanonicalFile();
	}catch(Exception e){
	    System.err.println("Exception in getDefaultDir:"+e);
	}

	return result.toString();
    }

    /**
     * Pops up a dialog to determine where the user would like ISAW
     * installed. This changes the value of the location JTextField.
     */
    private String getInstallDir(){
	JFileChooser fc = new JFileChooser();

	File result=new File(location.getText());
        fc.setCurrentDirectory(result);

	// set title
        fc.setDialogTitle("Select destination directory for installing ISAW");

	// turn off multiple selection
        fc.setMultiSelectionEnabled(false);
       
	// set selection mode to files and directories
	fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	//fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
	boolean keepgoing=true;
	while(keepgoing){
	    switch(fc.showDialog(IsawInstaller.this, "Select")){
	    case JFileChooser.CANCEL_OPTION:
		return null;
	    case JFileChooser.APPROVE_OPTION:
		// do nothing
	    }
	    result=fc.getSelectedFile();
	    if(result.exists()){
		if(result.isDirectory()){
		    keepgoing=false;
		}else{
		    System.out.println(result+" is a file");
		}
	    }else{ // should we create the directory?
		String msg="'"+result+"'"+" does not exist.\n"
		    +"Create?";
		switch(JOptionPane.showConfirmDialog(IsawInstaller.this,msg,
		          "Create New Directory?",JOptionPane.YES_NO_OPTION)){
		case JOptionPane.YES_OPTION:
		    if(result.mkdir()){
			keepgoing=false;
			break;
		    }
		case JOptionPane.NO_OPTION:
		    // don't do anything
		}
	    }
	}


	String filename=fc.getSelectedFile().toString();
	location.setText(filename);

	String batchfile=batch.getText();
        if(batchfile.equals(NO_BATCH)){
            // do nothing
        }else{
            batchfile=batchfile.substring(batchfile.lastIndexOf(File.separator));
            batch.setText(filename+batchfile);
        }

        return filename;
    }

    /**
     * Fix the separator in a director/file listing to contain only
     * forward slashes.
     */
    private static String fixSeparator(String filename){
        String separator = "/";
        String result    = null;

        result = replace(filename, "\\\\", separator);
        result = replace(result,   "\\",   separator);
        result = replace(result,   "//",   separator);

        return result;
    }

    private static String replace( String in_string, String old_chars,
                                   String new_chars ){

        if( in_string==null || old_chars==null || new_chars==null ) return null;

        if(old_chars.equals(new_chars)) return in_string;

        int start;
        String result=in_string;

        int from_index=0;
        while( result.indexOf(old_chars,from_index)>=0 ){
            start=result.indexOf( old_chars, from_index );
            result=result.substring(0,start)+new_chars
                +result.substring(start+old_chars.length());
            from_index=start+new_chars.length();
        }

        return result;
    }

    /* =================== make the batch file ==================== */
    /** 
     * Determine whether or not the user wants to make a batch
     * file. If cancel is selected then it does nothing and returns
     * false, otherwise will invoke either "noBatch" or "yesBatch".
     */
    private boolean makeBatch(){
	String msg = "Create a batch file?";
	switch(JOptionPane.showConfirmDialog(IsawInstaller.this,msg,
			     "Batch File",JOptionPane.YES_NO_CANCEL_OPTION)){
	case JOptionPane.CANCEL_OPTION:
	    if((batch.getText()).equals(NO_BATCH)){
		return false;
	    }else{
		return false;
	    }
	case JOptionPane.YES_OPTION:
	    this.yesBatch(null);
	    return true;
	case JOptionPane.NO_OPTION:
	    this.noBatch();
	    return false;
	}

	return true;
    }

    /**
     * Determine what the default name of the batch file should be
     * according to the operating system.
     */
    private String getDefaultBatch(){
	String filename=this.location.getText()+File.separator;

	String bt="";
	if(batch!=null){
	    bt=batch.getText();
	    if(bt==null) bt="";
	}

	if(operating_system.equals(WIN_ID)){
	    filename=filename+"Isaw_exec.bat";
	}else if(operating_system.equals(LIN_ID)){
	    filename=filename+"Isaw_exec.sh";
        }else if(operating_system.equals(SUN_ID)){
            filename=filename+"Isaw_exec.sh";
        }else if(operating_system.equals(MAC_ID)){
	    filename=filename+"Isaw_exec.applescript";
	}else{
	    return null;
	}
	return filename;
    }

    /**
     * Pop up a dialog to determine what the batch file should be
     * called. This changes the value of the batch JTextField through
     * the use of "noBatch" and "yesBatch".
     */
    private String getBatchName(){
	String filename=this.batch.getText();
	
	if(filename.indexOf(NO_BATCH)>=0){
	    filename=getDefaultBatch();
	}

	if(filename==null){
	    return null;
	}

	filename=ISAWgetFile("Select Name of Batch File",filename);

	if(filename==null) return null;

	/* if((new File(filename)).exists()){
	   switch(JOptionPane.showConfirmDialog(IsawInstaller.this,
	   "Overwrite Existing batch file?",
	   "Batch File",JOptionPane.YES_NO_CANCEL_OPTION)){
	   case JOptionPane.NO_OPTION:
	   this.noBatch();
	   case JOptionPane.CANCEL_OPTION:
	   return null;
	   case JOptionPane.YES_OPTION:
	   // do nothing
	   }
	   } */

	yesBatch(filename);
	return filename;
    }

    /**
     * Pop up a dialog to let the user specify the location of the
     * java executable. This modifies the JTextField java.
     */
    private String getJavaExec(){
	String filename=java.getText();
	if(operating_system.equals(WIN_ID)){
	    java.setText("n/a");
	    return null;
	}
	if(operating_system.equals(MAC_ID)){
	    java.setText("n/a");
	    return null;
	}
	
	filename=ISAWgetFile("Select Java Executable",this.findJavaExec());
	if(filename==null){
	    return null;
	}else{
	    java.setText(filename);
	    return filename;
	}
    }

    /**
     * Finds the java executable from the "java.home" system property.
     */
    private String findJavaExec(){
	String filename=System.getProperty("java.home");
	if(filename!=null){
	    filename=filename.substring(0,filename.lastIndexOf(System.getProperty("file.separator")))
		+System.getProperty("file.separator")+"bin"
		+System.getProperty("file.separator")+"java";
	}else{
	    filename=".";
	}

	return filename;
    }

    /**
     * Sets the text of batch to NO_BATCH and the text of java to NA.
     */
    private void noBatch(){
	batch.setText(NO_BATCH);
	java.setText(NA);

	return;
    }

    /**
     * Sets the text of batch to batchfile and resets the text of
     * java.
     *
     * @param batchfile New name of the batchfile.
     */
    private void yesBatch(String batchfile){
	if(batchfile!=null){
	    if((batch.getText()).equals(batchfile)){
		// do nothing
	    }else{
		batch.setText(batchfile);
	    }
	}
	if(operating_system.equals(WIN_ID)){
	    // do nothing
	}else{
	    if((java.getText()).equals(NA)){
		java.setText(null);
	    }
	}

	return;
    }

    /**
     * Writes out the system dependent batch file.
     */
    private void writeBatch(){
	String filename=batch.getText();
        if((filename==null)||(filename.equals(NO_BATCH)))return;

	File batchF=new File(filename);
	if(batchF.exists()){
	    int last=filename.lastIndexOf(".");
	    File newName=new File(filename.substring(0,last)+".old");
	    batchF.renameTo(newName);

	    String msg="Renaming existing batch file from\n"
		+filename+"\n"
		+"to\n"
		+newName;

	    JOptionPane.showMessageDialog(IsawInstaller.this,msg,
					  "Renaming Batchfile",
					  JOptionPane.INFORMATION_MESSAGE);
	}

	String isaw_home=location.getText();
	if( (new File(isaw_home)).exists() ){
	    //System.out.println("isaw_home exists");
	}else{
	    (new File(isaw_home)).mkdir();

	    String msg="Creating new directory:\n"
		+isaw_home;
	    
	    JOptionPane.showMessageDialog(IsawInstaller.this,msg,
					  "Creating Directory",
					  JOptionPane.INFORMATION_MESSAGE);
	}

	String lib_home=isaw_home+File.separator+"lib";
	String java_home=java.getText();
	if(java_home==null || java_home.equals(NA) || java_home.length()<=0 
	   ) java_home="java";

	String content="";
	String newline=System.getProperty("line.separator"); // "\n";

	if(operating_system.equals(WIN_ID)){
	    content="rem The '-mx' option specifies initial memory"
		+" allocation."+newline
		+"rem If you have less than 256 MB of memory, you might"
		+" need to ask for less."+newline
		+"rem The '-cp' option specifies the class path\n"
		+"rem --"+newline
		+"rem The following command is used to run from jar files\n"
		+"rem --"+newline
		+"cd "+isaw_home+newline
		+"path %PATH%;./lib"+newline
		+"java -mx128m -cp "+fixSeparator(isaw_home)
                +";Isaw.jar;sgt_v2.jar;IPNS.jar;jnexus.jar;sdds.jar;.  "
                +"IsawGUI.Isaw"+newline
		+"rem --"+newline
 		+"rem The following command is used to run from Isaw folder"
		+newline
		+"rem --"+newline
		+"rem java -cp sgt_v2.jar;IPNS.jar;jnexus.jar;sdds.jar;."
		+" IsawGUI.Isaw"+newline;
	}else if(operating_system.equals(LIN_ID)){
	    content="#!/bin/sh"+newline
		+"ISAW="+isaw_home+newline
		+"JAVA="+java_home+newline
		+"export LD_LIBRARY_PATH="+lib_home+newline
		+"cd $ISAW"+newline
		+"$JAVA -mx128m -cp $ISAW:$ISAW/Isaw.jar:$ISAW/IPNS.jar:"+
		"$ISAW/jnexus.jar:$ISAW/sgt_v2.jar:$ISAW/sdds.jar"
		+" IsawGUI.Isaw"+newline;
	}else if(operating_system.equals(SUN_ID)){
	    content="#!/bin/sh"+newline
		+"ISAW="+isaw_home+newline
		+"JAVA="+java_home+newline
		+"LD_LIBRARY_PATH="+lib_home+newline
		+"cd $ISAW"+newline
		+"$JAVA -mx128m -cp $ISAW:$ISAW/Isaw.jar:$ISAW/IPNS.jar:"+
		"$ISAW/jnexus.jar:$ISAW/sgt_v2.jar:$ISAW/sdds.jar"
		+" IsawGUI.Isaw"+newline;
        }else if(operating_system.equals(MAC_ID)){
            content="tell application \"Terminal\""+newline
                +"      do script with command \"java -mx128m -cp "
                +isaw_home+":"
                +isaw_home+"/Isaw.jar:"
                +isaw_home+"/sgt_v2.jar:"
                +isaw_home+"/IPNS.jar:"
                +isaw_home+"/jnexus.jar:"
                +isaw_home+"/sdds.jar:. IsawGUI.Isaw\""+newline
                +"end tell"+newline;
	}else{
	    System.err.println("Unknown operating system: "+operating_system);
	    return;
	}

	//System.out.print(content);
	try{
	    FileWriter outfile=new FileWriter(filename);
	    outfile.write(content);
	    outfile.flush();
	    outfile.close();
	}catch(Exception e){
	    System.err.println("Exception in writeBatch: "+e);
	}

	if(operating_system.equals(LIN_ID) || operating_system.equals(SUN_ID)){
	    Process proc = null;
	    try{
		proc=Runtime.getRuntime().exec("chmod +x "+filename);
		proc.waitFor();
	    }catch(InterruptedException e){
		System.err.println("Could not change access of batch file: "+e);
	    }catch(IOException e){
		System.err.println("Could not change access of batch file: "+e);
	    }finally{
		if(proc!=null)proc.destroy();
	    }
	}

	return;
    }

    /* ======================== extraction ======================== */
    /**
     * Determine the name of the jar file.
     */
    private String getJarFileName(){
        String urlStr=null;
        if(inJar()){
            myClassName = this.getClass().getName()+".class";
            urlStr = this.getClass().getResource(myClassName).toString();
            if(urlStr!=null){
                urlStr=fixSeparator(urlStr);
                urlStr=URLDecoder.decode(urlStr);
                int from = "jar:file:".length();
                int to = urlStr.indexOf("!");
                if( from<to && from!=-1 ){
                    if(operating_system.equals(WIN_ID)) from++;
                    return urlStr.substring(from, to);
                }else{
                    System.err.println("'"+urlStr+"' not an archive("
                                       +from+","+to+")");
                }
            }else{
                System.err.println("Name of archive not found");
            }
        }else{
            File dir=new File("/IPNShome/pfpeterson/packup/");
            if(dir.isDirectory() && dir.exists() ){
                File F[];
                F = dir.listFiles();
                for( int i=0 ; i<F.length ; i++){
                    if(F[i].isFile()){
                        if(F[i].getName().endsWith(".jar")){
                            if(F[i].getName().startsWith("Isaw-")){
                                return F[i].getAbsolutePath();
                            }
                        }
                    }
                }
                System.err.println("Name of archive not found");
            }else{
                System.err.println(dir.getAbsolutePath()+" does not exist");
            }
        }
        System.exit(-1);
	return "";
    }

    /**
     * Move the existing IsawProps.dat if it exists.
     */
    private void moveIsawProps(){
	String filename=System.getProperty("user.home");
	if(filename.endsWith(File.separator)){
	    // do nothing
	}else{
	    filename=filename+File.separator;
	}
	filename=filename+"IsawProps.dat";
	//System.out.println(filename);

	File props=new File(filename);
	if(props.exists()){
	    int last=filename.lastIndexOf(".");
	    File newName=new File(filename.substring(0,last)+".old");
	    props.renameTo(newName);
	    
            if(newName.exists()&&!props.exists()){
                String msg="Renaming existing IsawProps.dat to\n"
                    +newName;

                JOptionPane.showMessageDialog(IsawInstaller.this,msg,
                                              "Renaming IsawProps.dat",
                                              JOptionPane.INFORMATION_MESSAGE);
            }
	}
    }

    /**
     * Access point for extracting files from the archive. The actual
     * extraction is done from a separate thread. This allows for the
     * progress bar to be updated.
     */
    public void extract(){
	final Extractor worker;
	worker=new Extractor(IsawInstaller.this);
	worker.init(location.getText(),jarFileName);
	worker.start();
    }


    /* =========================== GUI ============================ */
    /**
     * Draw the GUI using reasonable start values for all of the
     * install information.
     */
    private void init(){
	mw = new JFrame("Isaw Installer");
	mw.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	// ==================== initialize the grid bag constraints ==
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill    = GridBagConstraints.BOTH;
	gbc.weightx = 1.0;
	gbc.anchor  = GridBagConstraints.WEST;

	// ==================== put text-box on top and fill with readme
	JEditorPane leftEdP = new JEditorPane();
	leftEdP.setEditable(false);
	leftEdP.setText(this.readmetext());
	JScrollPane instruct=new JScrollPane(leftEdP);
	instruct.setVerticalScrollBarPolicy(
			   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	instruct.setPreferredSize(new Dimension(700,400));
	mw.getContentPane().add(instruct, BorderLayout.NORTH);

	// ==================== put a generic Panel on the bottom ====
	JPanel right=new JPanel();
	right.setLayout(new GridBagLayout());
	mw.getContentPane().add(right, BorderLayout.SOUTH);


	// ==================== create objects to fill bottom panel ==
	// ========== stuff for archive name
	JLabel unpackL = new JLabel(UNPACK_ARCH);
	JTextField unpack=new JTextField();
	unpack.setEditable(false);
	unpack.setColumns(30);
	unpack.setText(jarFileName);
	// ========== stuff for install directory
	JLabel locationL = new JLabel(INSTALL_LOC);
	location=new JTextField(this.getDefaultDir());
	location.setEditable(false);
	location.setColumns(30);
	installDir=new JButton(CHANGE);
	// ========== stuff for batch file frame
	JPanel batchP = new JPanel(new GridBagLayout());
	batchP.setBorder(BorderFactory.createTitledBorder("Create Batch File"));
	// ========== cancel button
	cancelBut=new JButton(CANCEL_BUT);
	cancelBut.setForeground(Color.red);
	// ========== progress meter
	progress=new JProgressBar();
	//progress.setIndeterminate(true);
	// ========== install button
	installBut=new JButton(START_BUT);
	installBut.setForeground(Color.green.darker());

	// ==================== things for batch file frame ==========
	GridBagLayout batchGB = new GridBagLayout();
	// ========== stuff for batch file name
	JLabel batchL=new JLabel(BATCH_FILE);
	batch=new JTextField(getDefaultBatch());
	batch.setEditable(false);
	batch.setColumns(30);
	batchFile=new JButton(CHANGE);
	// ========== stuff for java executable location
	JLabel javaL=new JLabel(JAVA_LOC);
	if(operating_system.equals(WIN_ID)){
	    java=new JTextField(NA);
	}else{
	    java=new JTextField(this.findJavaExec());
	}
	java.setEditable(false);
	java.setColumns(30);
	javaLoc=new JButton(CHANGE);

	// ==================== set size of buttons to be same as installBut
	Dimension d=installBut.getSize();
	installDir.setSize(d);
	batchFile.setSize(d);
	javaLoc.setSize(d);
	cancelBut.setSize(d);

	// ==================== add the listeners ====================
	installDir.addActionListener(new MyMouseListener(this));
	batchFile.addActionListener(new MyMouseListener(this));
	javaLoc.addActionListener( new MyMouseListener(this));
	cancelBut.addActionListener( new MyMouseListener(this));
	installBut.addActionListener( new MyMouseListener(this));

	// ==================== pack the lower panel =================
	// ========== add vertical space at the top
	gbc.weightx=1.0;   gbc.gridwidth=GridBagConstraints.REMAINDER;
	right.add(Box.createVerticalStrut(10),gbc);
	// ========== stuff for unpack step
	gbc.weightx=0.0;   gbc.gridwidth=1;
	right.add(Box.createHorizontalStrut(2),gbc);
	gbc.weightx=0.0;   gbc.gridwidth=1;
	right.add(unpackL,gbc);
	gbc.weightx=1.0;   gbc.gridwidth=1;
	right.add(Box.createHorizontalGlue(),gbc);
	gbc.weightx=2.0;   gbc.gridwidth=1;
	right.add(unpack,gbc);
	gbc.weightx=1.0;   gbc.gridwidth=1;
	right.add(Box.createHorizontalGlue(),gbc);
	gbc.weightx=0.0;   gbc.gridwidth=1;
	right.add(Box.createRigidArea(d),gbc);
	gbc.weightx=0.0;   gbc.gridwidth=GridBagConstraints.REMAINDER;
	right.add(Box.createHorizontalGlue(),gbc);
	// ========== add vertical space
	gbc.weightx=1.0;   gbc.gridwidth=GridBagConstraints.REMAINDER;
	right.add(Box.createVerticalStrut(10),gbc);
	// ========== stuff for install directory step
	gbc.weightx=0.0;   gbc.gridwidth=1;
	right.add(Box.createHorizontalStrut(2),gbc);
	gbc.weightx=0.0;   gbc.gridwidth=1;
	right.add(locationL,gbc);
	gbc.weightx=1.0;   gbc.gridwidth=1;
	right.add(Box.createHorizontalGlue(),gbc);
	gbc.weightx=2.0;   gbc.gridwidth=1;
	right.add(location,gbc);
	gbc.weightx=1.0;   gbc.gridwidth=1;
	right.add(Box.createHorizontalGlue(),gbc);
	gbc.weightx=0.0;   gbc.gridwidth=1;
	right.add(installDir,gbc);
	gbc.weightx=0.0;   gbc.gridwidth=GridBagConstraints.REMAINDER;
	right.add(Box.createHorizontalStrut(2),gbc);
	if(operating_system.equals(UNKNOWN_ID)){
	    // do nothing
	}else{
	    // ========== add vertical space
	    gbc.weightx=1.0;   gbc.gridwidth=GridBagConstraints.REMAINDER;
	    right.add(Box.createVerticalStrut(5),gbc);
	    
	    // ========== batch frame
	    gbc.weightx=0.0;   gbc.gridwidth=1;
	    right.add(Box.createHorizontalStrut(5),gbc);
	    gbc.weightx=1.0;   gbc.gridwidth=GridBagConstraints.RELATIVE;
	    right.add(batchP,gbc);
	    gbc.weightx=0.0;   gbc.gridwidth=GridBagConstraints.REMAINDER;
	    right.add(Box.createHorizontalStrut(5),gbc);
	}
	// ========== add vertical space
	gbc.weightx=1.0;   gbc.gridwidth=GridBagConstraints.REMAINDER;
	right.add(Box.createVerticalStrut(5),gbc);
	// ========== Cancel Button
	gbc.weightx=0.0;   gbc.gridwidth=1;
	right.add(Box.createHorizontalStrut(5),gbc);
	gbc.weightx=0.0;   gbc.gridwidth=1;
	right.add(cancelBut,gbc);
	// ========== add horizontal space
	gbc.weightx=1.0;   gbc.gridwidth=1;
	right.add(Box.createHorizontalGlue(),gbc);
	// ========== add horizontal space
	gbc.weightx=2.0;   gbc.gridwidth=1;
	right.add(progress,gbc);
	// ========== add horizontal space
	gbc.weightx=1.0;   gbc.gridwidth=1;
	right.add(Box.createHorizontalGlue(),gbc);
	// ========== Install Button
	gbc.weightx=0.0;   gbc.gridwidth=1;
	right.add(installBut,gbc);
	gbc.weightx=0.0;   gbc.gridwidth=GridBagConstraints.REMAINDER;
	right.add(Box.createHorizontalStrut(5),gbc);
	// ========== add vertical space
	gbc.weightx=1.0;   gbc.gridwidth=GridBagConstraints.REMAINDER;
	right.add(Box.createVerticalStrut(5),gbc);

	// ==================== pack the batch file fram =============
	//========== put in a vertical space
	//gbc.weightx=1.0;   gbc.gridwidth=GridBagConstraints.REMAINDER;
	//batchP.add(Box.createVerticalStrut(5),gbc);
	// ========== batch file name location
	gbc.weightx=0.0;   gbc.gridwidth=1;
	batchP.add(batchL,gbc);
	gbc.weightx=1.0;   gbc.gridwidth=1;
	batchP.add(Box.createHorizontalGlue(),gbc);
	gbc.weightx=1.0;   gbc.gridwidth=1;
	batchP.add(batch,gbc);
	gbc.weightx=1.0;   gbc.gridwidth=1;
	batchP.add(Box.createHorizontalGlue(),gbc);
	gbc.weightx=0.0;   gbc.gridwidth=GridBagConstraints.REMAINDER;
	batchP.add(batchFile,gbc);
	if(operating_system.equals(WIN_ID) || operating_system.equals(MAC_ID)){
	    // do nothing
	}else{
	    // ========== put in a vertical space
	    gbc.weightx=1.0;   gbc.gridwidth=GridBagConstraints.REMAINDER;
	    batchP.add(Box.createVerticalStrut(10),gbc);
	    // ========== java executable location
	    gbc.weightx=0.0;   gbc.gridwidth=1;
	    batchP.add(javaL,gbc);
	    gbc.weightx=1.0;   gbc.gridwidth=1;
	    batchP.add(Box.createHorizontalGlue(),gbc);
	    gbc.weightx=1.0;   gbc.gridwidth=1;
	    batchP.add(java,gbc);
	    gbc.weightx=1.0;   gbc.gridwidth=1;
	    batchP.add(Box.createHorizontalGlue(),gbc);
	    gbc.weightx=0.0;   gbc.gridwidth=GridBagConstraints.REMAINDER;
	    batchP.add(javaLoc,gbc);
	}
	// ========== put in a vertical space
	gbc.weightx=1.0;   gbc.gridwidth=GridBagConstraints.REMAINDER;
	batchP.add(Box.createVerticalStrut(5),gbc);

	// ==================== put up the GUI =======================
	mw.pack();
	mw.show();
    }

    /* ===================== random methods ======================= */
    /**
     * Standardized method for selecting files.
     */
    private String ISAWgetFile( String title, String filename){
	JFileChooser fc=new JFileChooser();
	fc.setSelectedFile(new File(filename));
	fc.setDialogType(JFileChooser.OPEN_DIALOG);
	fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fc.setDialogTitle(title);
	fc.setMultiSelectionEnabled(false);
	
	switch(fc.showDialog(IsawInstaller.this,"Select")){
	case JFileChooser.CANCEL_OPTION:
	    return null;
	case JFileChooser.APPROVE_OPTION:
	    return fc.getSelectedFile().toString();
	}

	return null;
    }

    /**
     * Initialize the progress bar.
     */
    void initProgress(){
	progress.setStringPainted(true);
	progress.setString("");
	progress.setValue(0);
    }


    /**
     * Set the progress bar to the new values. This is meant to be
     * called from a separate thread.
     */
    void setProgress(int value, int max, String text){
	progress.setValue(value);
	progress.setMaximum(max);

	if(text.indexOf("docs/html/")>=0){
	    int index=(new String("docs/html/")).length();
	    int index2=text.indexOf("/",index+1);
	    if(index2>index){
		text=text.substring(0,index2+1)+"...";
	    }
	}
	while(text.length()<20){
	    text=text+" ";
	}


	progress.setString(text);
    }

    /**
     * Method to allow other threads to close the main GUI window.
     */
    void closeMain(int extracted){
        String msg;


        msg="Start ISAW using   : ";
        if(batch.getText().equals(NO_BATCH)){
            msg=msg+"java IsawGUI.Isaw";
        }else{
            msg=msg+batch.getText();
        }
        msg=msg+"\n\n";

	msg=msg+"Extracted "+extracted+" file"
	    +((extracted > 1) ? "s":"")+"\n"
	    +"from the archive   : "+jarFileName+"\n"
	    +"into the directory : "+location.getText()+"\n";
	    

	JOptionPane.showMessageDialog(IsawInstaller.this,msg,
				      "Zip Self Extractor",
				      JOptionPane.INFORMATION_MESSAGE);

	System.exit(0);
    }

    /**
     * Get the readme file out of the jar and pass it back to the
     * caller. This is intended for displaying the readme in the
     * installer main window.
     */
    private String readmetext(){
	String rs="";

	try{
	    ZipFile zf=new ZipFile(this.jarFileName);
	    Enumeration e=zf.entries();
	    ZipEntry ze=(ZipEntry)e.nextElement();
	    while(e.hasMoreElements()){
		ze=(ZipEntry)e.nextElement();
		if((ze.getName()).indexOf("Readme")>=0){
		    break;
		}
	    }
	    InputStream zis=zf.getInputStream(ze);
	    int size=(int)ze.getSize();
	    byte[] b=new byte[size];
	    int rb=0;
	    int chunk=0;
	    while( (size-rb)>0 ){
		chunk=zis.read(b,rb,size-rb);
		if( chunk==-1){
		    break;
		}
		rb+=chunk;
	    }
	    zf.close();
	    rs=new String(b);
	}catch( Exception e){
	    System.err.println("Exception in readmetext: "+e);
	}

	return rs;
    }

/*===================== inner classes ==============================*/
    /**
     * Inner class to handle actions on the buttons
     */
    class MyMouseListener extends MouseAdapter implements ActionListener{
	IsawInstaller II;

	/**
	 * Constructor registers the main application
	 */
	public MyMouseListener( IsawInstaller ii ){
	    II=ii;
	}

	/**
	 * Handle the events accordingly.
	 */
	public void actionPerformed( ActionEvent e ){
	    if( e.getSource().equals(II.installDir) ){
		II.getInstallDir();
	    }else if( e.getSource().equals(II.batchFile) ){
		if(II.makeBatch()) getBatchName();
	    }else if( e.getSource().equals(II.javaLoc) ){
		getJavaExec();
	    }else if( e.getSource().equals(II.cancelBut) ){
		System.exit(0);
	    }else if( e.getSource().equals(II.installBut) ){
		II.installBut.setEnabled(false);
		II.writeBatch();
		II.moveIsawProps();
		II.extract();
	    }
	}
    } // end of MyMouseListener inner class

    /**
     * Inner class to allow for multithreading the extraction process.
     */
    class Extractor extends Thread{
	private File          outputDir;
	private ZipFile       zf;
	        Runnable      runnable;
	        int           current=0;
	        String        text;
	        int           max=10;
  	        IsawInstaller app;

	/**
	 * Constructor takes a pointer to the main thread so
	 * interprocess communication can occur.
	 */
	public Extractor(IsawInstaller APP){
	    app=APP;
	    app.initProgress();
	    runnable = new Runnable(){
		    public void run(){
			app.setProgress(current,max,text);
		    }
		};
	}

	/**
	 * Names of files and the total number of files to extract are
	 * given in this separate initialization method. This allows
	 * greater clarity of how the multithreading works. 
	 *
	 * The total number of files to extract (for progress bar) is
	 * set to be 10 less than the number of files which are
	 * extracted. I don't know why this is needed but otherwise
	 * the progress bar isn't full when the job is done.
	 */
	public void init(String OUTPUTDIR, String JARFILENAME){
	    outputDir=new File(OUTPUTDIR);
	    try{
		zf=new ZipFile(JARFILENAME);
		Enumeration entries=zf.entries();
		ZipEntry entry=null;
		String name=null;
		while(entries.hasMoreElements()){
		    entry=(ZipEntry)entries.nextElement();
		    name=entry.getName();
		    if(name.equals(null)){
			break;
		    }else if(this.skip(name)){
			continue;
		    }else{
			max++;
		    }
		}
		max=max-10;
		//System.out.println("NUMENTRIES:"+zf.size()+" "+max);
	    }catch(Exception e){
		System.err.println("Exception in Extractor.init: "+e);
	    }
	}

	/**
	 * This method does the actual extraction. Everytime that a
	 * file is about to be extracted the progress bar is updated.
	 */	
	public void run(){
	    byte[] buf = new byte[1024];
	    SimpleDateFormat formatter = 
		new SimpleDateFormat ("MM/dd/yyyy hh:mma",Locale.getDefault());
	    
	    boolean overwrite = false;
	    
	    FileOutputStream out = null;
	    InputStream in = null;
	    
	    int size = zf.size();
	    int extracted = 0;

	    try{
		Enumeration entries = zf.entries();
		
		for (int i=0; i<size; i++){
		    ZipEntry entry = (ZipEntry) entries.nextElement();
		    if(entry.isDirectory()){
			continue;
		    }
		    
		    String pathname = entry.getName();
		    
		    // check whether it should not be installed
		    if(this.skip(pathname)) continue;
		    
		    current=i;
		    text=new String(pathname);
		    SwingUtilities.invokeLater(runnable);

		    extracted ++;
		    in = zf.getInputStream(entry);
		    
		    File outFile = new File(outputDir, pathname);
		    Date archiveTime = new Date(entry.getTime());
		    
		    if(overwrite==false){
			if(outFile.exists()){
			    Object[] options = {"Yes", "Yes To All", "No"};
			    Date existTime = new Date(outFile.lastModified());
			    Long archiveLen = new Long(entry.getSize());
			    
			    String msg = "File name conflict: "
				+ "There is already a file with "
				+ "that name on the disk!\n"
				+ "\nFile name: " + outFile.getName()
				+ "\nExisting file: "
				+ formatter.format(existTime) + ",  "
				+ outFile.length() + "Bytes"
				+ "\nFile in archive:"
				+ formatter.format(archiveTime) + ",  " 
				+ archiveLen + "Bytes"
				+"\n\nWould you like to overwrite the file?";
			    
			    int result = 
				JOptionPane.showOptionDialog
				(IsawInstaller.this,
				 msg, "Warning", 
				 JOptionPane.DEFAULT_OPTION,
				 JOptionPane.WARNING_MESSAGE, 
				 null, options,options[0]); 
			    
			    if(result == 2){ // No
				continue;
			    }else if( result == 1){ //YesToAll
				overwrite = true;
			    }
			}
		    }
		    
		    File parent = new File(outFile.getParent());
		    if (parent != null && !parent.exists()){
			parent.mkdirs();
		    }
		    
		    out = new FileOutputStream(outFile);                
		    
		    while (true){
			int nRead = in.read(buf, 0, buf.length);
			if (nRead <= 0)
			    break;
			out.write(buf, 0, nRead);
		    }
		    
		    out.close();
		    outFile.setLastModified(archiveTime.getTime());
		}
		
		zf.close();
		getToolkit().beep();
		
	    }catch (Exception e){
		System.out.println("In IsawInstaller.extract(): "+e);
		if(zf!=null){ 
		    try { zf.close(); } catch(IOException ioe) {;} 
		}
		if(out!=null){ 
		    try{out.close();} catch(IOException ioe) {;} 
		}
		if(in!=null){ 
		    try { in.close(); } catch(IOException ioe) {;} 
		}
	    }
	    app.closeMain(extracted);
	}

	/**
	 * This is a filter as to whether or not a file should be
	 * extracted. This method allows for a central location of bad
	 * filename. If new files are to be skipped then they should
	 * be added here.
	 *
	 * The MANIFEST, any cvs, IsawInstaller* and SwingWorker*
	 * files are all skipped in general. ".dll" files are skipped
	 * in linux while ".so" files are skipped in windows.
	 */
	private boolean skip( String filename ){
	    // check if it is a shared library
	    if( filename.endsWith("dll") ){
		if( operating_system.equals(WIN_ID) ){
		    // install file
		}else if( operating_system.equals(LIN_ID) ){
		    return true; // don't install file
                }else if( operating_system.equals(SUN_ID) ){
                    return true; // don't install file
                }else if( operating_system.equals(MAC_ID) ){
                    return true; // don't install file
		}else if( operating_system.equals(UNKNOWN_ID) ){
		    // install file
		}
	    }else if( filename.endsWith("so") ){
		if( operating_system.equals(WIN_ID) ){
		    return true; // don't install file
		}else if( operating_system.equals(LIN_ID) ){
		    // install file
                }else if( operating_system.equals(SUN_ID) ){
                    return true; // don't install file
                }else if( operating_system.equals(MAC_ID) ){
                    return true; // don't install file
		}else if( operating_system.equals(UNKNOWN_ID) ){
		    // install file
		}
	    }else if( (filename.toUpperCase()).indexOf("CVS") >=0 ){
		return true;
	    }else if( (filename.toUpperCase()).equals(MANIFEST) ){
		return true;
	    }else if( filename.indexOf("IsawInstaller")==0 ){
		return true;
	    }else if( filename.indexOf("SwingWorker")==0 ){
		return true;
	    }
	    
	    return false;
	}
    } // end of Extractor inner class
}
