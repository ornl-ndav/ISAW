/*
 * File:  IssScript.java 
 *             
 * Copyright (C) 2003, Peter Peterson
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
 *           Argonne, IL 60439-4845, USA
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
 * Revision 1.2  2003/06/17 16:29:40  pfpeterson
 * Takes care of getCommand, getTitle, getDocumentation, and getCategoryList.
 * These are determined only once (Scripts are immutable) and only on demand.
 *
 * Revision 1.1  2003/06/13 20:05:40  pfpeterson
 * Added to CVS.
 *
 */

package Command;

import DataSetTools.util.FilenameUtil;
import DataSetTools.util.SharedData;
import DataSetTools.util.StringUtil;
import java.util.Hashtable;
import javax.swing.text.Document;

public class IssScript extends Script{
  private static final String    COMMAND = "COMMAND";
  private static final String    TITLE   = "TITLE";
  private static final String    SCRIPTS = "Scripts";
  private static       Hashtable HOMES   = null;

  private String   documentation    = null;
  private String   command          = null;
  private String   title            = null;
  private String[] categoryList     = null;
  private boolean  hasDocumentation = true;

  // ============================== CONSTRUCTORS
  public IssScript(String filename){
    super(filename);
  }

  public IssScript(StringBuffer script){
    super(script);
  }

  public IssScript(Document doc){
    super(doc);
  }

  // ============================== PUBLIC UTILITY METHODS
  public boolean reload(){
    this.documentation=null;
    return super.reload();
  }

  /**
   * Returns the title of the script. This is produced on demand, not
   * upon instantiation. If not defined within the text of the script
   * the command name is returned instead.
   */
  public String getTitle(){
    if(this.title!=null)
      return this.title;

    // try getting from the text
    String line=null;
    for( int i=0 ; i<this.numLines() ; i++ ){
      line=getLine(i).trim();
      if(! line.startsWith("$") ) continue;
      int index=line.toUpperCase().indexOf(TITLE);
      if( index<=0 ) continue;
      index=line.indexOf("=");
      this.title=line.substring(index+1).trim();
      return this.title;
    }

    // fall back on the command name
    this.title=this.getCommand();
    return this.title;
  }

  /**
   * Returns the category list for the script. This is produced on
   * demand, not upon instantiation.
   */
  public String[] getCategoryList(){
    // return list if initialized
    if(this.categoryList!=null)
      return this.categoryList;

    // initialize the hashtable if necessary
    if(HOMES==null) initHomes();

    // try getting value from filename
    if(this.filename.equals(UNKNOWN)){
      this.categoryList=new String[1];
      this.categoryList[0]=DataSetTools.operator.Operator.OPERATOR;
      return this.categoryList;
    }

    // chop of the filename
    int index=this.filename.lastIndexOf("/");
    if(index<=0) index=this.filename.length();
    String directory=this.filename.substring(0,index);

    // split into the major and minor categories
    index=directory.indexOf(SCRIPTS);
    String minor=null;
    String major=null;
    if(index>0){
      int end=index+SCRIPTS.length()+1;
      if(end>directory.length())
        minor=null;
      else
        minor=directory.substring(index+SCRIPTS.length()+1);
      major=directory.substring(0,index-1);
    }else{
      major=directory;
    }

    String[] minorArray=StringUtil.split(minor,"/");

    // fill in the category list
    int count=2;
    if(minorArray!=null) count=count+minorArray.length;
    this.categoryList=new String[count];
    this.categoryList[0]=DataSetTools.operator.Operator.OPERATOR;
    this.categoryList[1]=(String)HOMES.get(major);
    if(minorArray!=null){
      for( int i=0 ; i<minorArray.length ; i++ )
        this.categoryList[i+2]=minorArray[i];
    }

    return this.categoryList;
  }

  /**
   * Returns the command for the script. This is produced on demand,
   * not during instantiation. If not defined within the body of the
   * script this will return the filename (without extension).
   */
  public String getCommand(){
    // return command if initialized
    if(this.command!=null)
      return this.command;

    // try getting the command from the text
    String line=null;
    for( int i=0 ; i<this.numLines() ; i++ ){
      line=getLine(i).trim();
      if(! line.startsWith("$") ) continue;
      int index=line.toUpperCase().indexOf(COMMAND);
      if( index<=0 ) continue;
      index=line.indexOf("=");
      this.command=line.substring(index+1).trim();
      return this.command;
    }

    // don't have a filename to fall back on
    if( this.filename.equals(UNKNOWN) ){
      System.out.println("UNKNOWN COMMAND");
      return UNKNOWN;
    }
    
    // synthesize the command from the filename
    int start=this.filename.lastIndexOf("/");
    int end=this.filename.lastIndexOf(".");
    if(start<=0)
      start=0;
    else
      start=start+1;
    if(end<=0)   end=this.filename.length();
    this.command=this.filename.substring(start,end);
      return this.command;
  }

  /**
   * Returns the documentation string for the script. This is produced
   * on demand. If there is not documentation this returns null.
   */
  public String getDocumentation(){
    // return null b/c there is no documentation
    if(!this.hasDocumentation) return null;

    // return the docs if they already exist
    if(this.documentation!=null) return this.documentation;

    // determine if there is docs in the script
    int lastdocline=this.findEndOfDoc();
    if(lastdocline<0){ // there is no documentation
      this.hasDocumentation=false;
      return null;
    }

    StringBuffer docbuffer=new StringBuffer(80*lastdocline);
    boolean pastfirst_nonempty=false;
    String line;

    // copy the docs into the buffer
    for( int i=0 ; i<lastdocline ; i++ ){
      line=this.getLine(i).substring(1).trim();
      if(line==null || line.length()<=0){
        if(pastfirst_nonempty)
          docbuffer.append("\n");
      }else{
        pastfirst_nonempty=true;
        docbuffer.append(line+"\n");
      }
    }

    // set the doc string and return the result
    this.documentation=docbuffer.toString();
    return this.documentation;
  }

  // ============================== PRIVATE UTILITY METHODS
  /**
   * Initialize the hashtable that maps directory to category.
   */
  private static void initHomes(){
    HOMES=new Hashtable();
    // ISAW home directory
    addDir(SharedData.getProperty("ISAW_HOME"),"Isaw Scripts");

    // user home directory
    addDir(SharedData.getProperty("user.home"),"User Scripts");

    // group home directories
    addDirs(SharedData.getProperty("GROUP_HOME"),"Group Home");
    int group=1;
    String group_home=SharedData.getProperty("GROUP"+group+"_HOME");
    while(group_home!=null && group_home.length()>0){
      addDirs(group_home,"Group"+group+" Scripts");
      group=group+1;
      group_home=SharedData.getProperty("GROUP"+group+"_HOME");
    }
  }

  /**
   * Add a set of directories to the hashtable of directories. Names
   * in the hashtable take precedence over new ones.
   */
  private static void addDirs(String directories, String name){
    if(directories==null || directories.length()<=0) return;

    directories=FilenameUtil.setForwardSlash(directories);
    directories=directories.replace(java.io.File.pathSeparatorChar,';');
    
    String[] array=StringUtil.split(directories,";");
    if(array==null) return;
    for( int i=0 ; i<array.length ; i++ )
      addDir(array[i],name);
  }

  /**
   * Add a directory to the hashtable of directories. Names in the
   * hashtable take precedence over new ones.
   */
  private static void addDir(String directory, String name){
    directory=FilenameUtil.setForwardSlash(directory);
    if(HOMES.get(directory)==null)
      HOMES.put(directory,name);
  }

  /**
   * This determines the number of the last line of the
   * documentation. This returns -1 if the first line does not contain
   * a commend and is non-empty.
   */
  private int findEndOfDoc(){
    String line=null;
    int endofdoc=0;

    for( int i=0 ; i<this.numLines() ; i++ ){
      line=this.getLine(i);
      if( line.startsWith("#") || line.startsWith("\n"))
        endofdoc=i;
      else if(line.trim().length()<=0)
        endofdoc=i;
      else if(i==0)
        return -1;
      else
        break;
    }        
    return endofdoc;
  }

  // ============================== MAIN METHOD FOR TESTING ONLY
  public static void main(String[] args){
    if(args.length<1){
      System.out.println("USAGE: Script <filename>");
      System.exit(-1);
    }

    System.out.println("Loading "+args[0]);
    IssScript script=new IssScript(args[0]);
    System.out.println("COMMAND="+script.getCommand());
    String[] list=script.getCategoryList();
    System.out.print("CAT LIST=[");
    for( int i=0 ;  i<list.length ; i++ ){
      System.out.print(list[i]);
      if(i<list.length-1) System.out.print(",");
    }
    System.out.println("]");
    System.out.println(">>"+script.getDocumentation()+"<<");
  }
}
