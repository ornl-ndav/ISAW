/*
 * File:  ParameterClassList.java 
 *             
 * Copyright (C) 2003, Peter F. Peterson
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
 * $Log$
 * Revision 1.7  2003/10/08 21:41:18  dennis
 * If a directory is not found, the exception message now
 * contains the name of the directory that was not found.
 *
 * Revision 1.6  2003/09/09 23:30:38  bouzekc
 * Fixed NullPointerException when type is not defined in ParameterGUIs.
 *
 * Revision 1.5  2003/07/17 19:32:57  bouzekc
 * Fixed a bug where if getInstance() returned null, the
 * construction of this class failed.
 *
 * Revision 1.4  2003/06/10 14:38:21  pfpeterson
 * Added an exit at the end of main.
 *
 * Revision 1.3  2003/06/09 21:10:40  bouzekc
 * Fixed bug in constructor where initialized was not set
 * to true.
 * Made DEBUG non-final so it can be changed for releases.
 *
 * Revision 1.2  2003/06/06 19:46:44  pfpeterson
 * Implemented processJar, factoring out some common code in the process.
 *
 * Revision 1.1  2003/06/06 18:58:46  pfpeterson
 * Added to CVS.
 *
 */

package Command;

import DataSetTools.parameter.IParameter;
import DataSetTools.parameter.ParameterGUI;
import DataSetTools.util.SharedData;
import DataSetTools.util.FilenameUtil;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Class that contains a list of all IParameters with a concrete
 * class. In practice this means only the ParameterGUIs. This class
 * implements a singleton design.
 *
 * NOTE: this will only look for parameters in the
 * DataSetTools.parameter package.
 */
public class ParameterClassList{
  static       Hashtable paramList   = null;
  static       boolean   DEBUG       = false;
  static       boolean   initialized = false;
  static final String    matchname   =
                       FilenameUtil.setForwardSlash("DataSetTools/parameter/");


  /**
   * Initializes the singleton if it is not already done.
   */
  public ParameterClassList(){
    if(!initialized){
      paramList=new Hashtable(35,.6f);
      String jarName=jarName();
      if(jarName!=null && jarName.length()>0)
        processJar(jarName);
      else
        processDir();
    }

    initialized = true;

    if(DEBUG) System.out.println("Found "+paramList.size()+" parameters");
  }

  /**
   * Converts the type into an IParameter instance. If this fails it
   * returns null.
   */
  public IParameter getInstance(String type){
    if(!initialized)
      throw new IllegalStateException("constructor never finished");
    Class klass=(Class)paramList.get(type);
    if(klass==null)
      return null;
    else
      return getInstance(klass,false);
  }

  /**
   * Method to get an instance from a class. This silently catches all
   * possible Throwables and returns null.
   */
  private IParameter getInstance(Class klass,boolean debug){
    // get the constructor
    Constructor construct=null;
    try{
      construct=klass.getConstructor(new Class[]{String.class,Object.class});
    }catch(NoSuchMethodException e){
      if(debug) System.out.println("(NoSuchMethodException) NO");
      return null;
    }

    // get the instance
    IParameter param=null;
    Object[] objArray={null,null};
    try{
      param=(IParameter)construct.newInstance(objArray);
    }catch(InvocationTargetException e){
      if(debug) System.out.println("(InvocationTargetException) NO");
      return null;
    }catch(ClassCastException e){
      if(debug) System.out.println("(ClassCastException) NO");
      return null;
    }catch(InstantiationError e){
      if(debug) System.out.println("(InstantiationError) NO");
      return null;
    }catch(InstantiationException e){
      if(debug) System.out.println("(InstantiationException) NO");
      return null;
    }catch(IllegalAccessException e){
      if(debug) System.out.println("(IllegalAccessException) NO");
      return null;
    }

    return param;
  }

  /**
   * Find the name of the jar file that holds this class
   * (ParameterClassList). If this class is not in a jar this returns
   * null.
   */
  private String jarName(){
    // determine the name of this class
    String className="/"+this.getClass().getName().replace('.','/')+".class";
    // find out where the class is on the filesystem
    String classFile=this.getClass().getResource(className).toString();

    if( (classFile==null) || !(classFile.startsWith("jar:")) ) return null;

    // trim out parts and set this to be forwardslash
    classFile=classFile.substring(5,classFile.indexOf(className));
    classFile=FilenameUtil.setForwardSlash(classFile);

    // chop off a little bit more
    classFile=classFile.substring(4,classFile.length()-1);

    return classFile;
  }

  /**
   * Determine the possible class names from code residing in the jar.
   */
  private void processJar(String jarname){
    ZipFile zf=null;
    Enumeration entries=null;

    // open the zip file and get the list of entries
    try{
      zf=new ZipFile(jarname);
      entries=zf.entries();
    }catch(IOException e){
      throw new InstantiationError("IOException:"+e.getMessage());
    }

    // prepare some variables for looping through
    ZipEntry entry=null;
    String name=null;

    // go through the entries
    while(entries.hasMoreElements()){
      entry=(ZipEntry)entries.nextElement();
      name=checkName(entry.getName(),0);
      if(name!=null) addParameter(name);
    }

    // clean up
    try{
      zf.close();
    }catch(IOException e){
      // doesn't matter now
    }
  }

  /**
   * Determine the possible class names from code residing in a
   * unpacked directory.
   */
  private void processDir(){
    // get the location of ISAW code base
    String isaw_home=SharedData.getProperty("ISAW_HOME");
    if(isaw_home==null)
      throw new InstantiationError("Could not find directory");
    isaw_home=FilenameUtil.setForwardSlash(isaw_home+"/");

    // get the name of the directory to check
    String dir=FilenameUtil.setForwardSlash(isaw_home
                                            +"DataSetTools/parameter/");

    // check that the directory is okay to work with
    if(DEBUG) System.out.println("Looking in "+dir);
    File paramDir=new File(dir);
    if( !(paramDir.exists()) || !(paramDir.isDirectory()) )
      throw new InstantiationError("Could not find directory " + dir);

    // get the list of all possible classes
    File[] files=paramDir.listFiles();
    String filename=null;
    for( int i=0 ; i<files.length ; i++ ){
      filename=checkName(files[i].toString(),isaw_home.length());
      if(filename!=null) addParameter(filename);
    }
  }

  /**
   * Checks if the specified filename is worth trying to get a class
   * from. If sucessful this will return a package qualified
   * classname, otherwise it will return null.
   */
  private String checkName(String filename,int start_off){
    String name=FilenameUtil.setForwardSlash(filename);

    // check that it is a possibility
    if(name==null || name.length()<=0) return null;
    if(name.indexOf(matchname)<0) return null;
    if(! name.endsWith(".class") ) return null;
    if(name.indexOf("$")>name.lastIndexOf("/")) return null;
    
    // chop off the class part
    name=name.substring(start_off,name.length()-6);
    name=name.replace('/','.');
    
    return name;
  }

  /**
   * Converts a classname into a class, does several checks, and adds
   * it to the list, if appropriate.
   */
  private void addParameter( String classname ){
    if(DEBUG) System.out.print(classname+" ");

    // get the class
    Class klass=null;
    try{
      klass=Class.forName(classname);
    }catch(NoClassDefFoundError e){
      if(DEBUG) System.out.println("(NoClassDefError) NO");
      return;
    }catch(ClassNotFoundException e){
      if(DEBUG) System.out.println("(ClassNotFoundException) NO");
      return;
    }catch(ClassFormatError e){
      if(DEBUG) System.out.println("(ClassFormatError) NO");
      return;
    }

    // confirm this isn't null
    if(klass==null){
      if(DEBUG) System.out.println("(Null Class) NO");
      return;
    }

    // check that this is not an interface or abstract
    int modifier=klass.getModifiers();
    if(Modifier.isInterface(modifier)){
      if(DEBUG) System.out.println("(Interface) NO");
      return;
    }
    if(Modifier.isAbstract(modifier)){
      if(DEBUG) System.out.println("(Abstract) NO");
      return;
    }

    // check that this is a parameter
    if(! (IParameter.class.isAssignableFrom(klass)) ){
      if(DEBUG) System.out.println("(Not a IParameter) NO");
      return;
    }

    // get the instance
    IParameter param=getInstance(klass,DEBUG);

    if( param == null ){
      return;
    }

    // get the type which will be the key in the hashtable
    String type=param.getType();

    if(type == null) {
      System.err.println("Type not defined for " + param.getClass());
      return;
    }
    if(type.equals("UNKNOWN")){
      if(DEBUG) System.out.println("(Type Unknown) NO");
      return;
    }else{
      if(DEBUG) System.out.print("[type="+type+"] ");
    }

    // add it to the hashtable
    paramList.put(type,klass);
    
    // final debug print
    if(DEBUG) System.out.println("OK");
  }

  /**
   * For testing purposes only. This finds the possible parameters and
   * prints the result to the console.
   */
  public static void main(String[] args){
    DEBUG = true;
    ParameterClassList PL=new ParameterClassList();
    System.exit(0);
  }
}
