/*
 * File:  Script_Class_List_Handler.java 
 *             
 * Copyright (C) 2001, Ruth Mikkelson
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
 * Revision 1.70  2005/10/27 21:13:17  rmikk
 * Incorporated the new opInfo structure correctly into the main program
 *
 * Revision 1.69  2005/08/24 15:19:42  rmikk
 * Sets up all the data in the operator info structure when a new operator
 *    is loaded during the fast load.
 *
 * Revision 1.68  2005/08/04 19:30:25  dennis
 * Fixed spelling error in javadoc.
 *
 * Revision 1.67  2005/08/04 19:27:39  dennis
 * Made new messages that are printed when the cached operator file
 * is not found, or when the time stamp doesn't match.  The messages
 * now indicate that this is normal and that Isaw will just take a
 * little longer to load.
 *
 * Revision 1.66  2005/08/04 13:56:35  rmikk
 * Fixed error in Filename of for linux jar files
 *
 * Revision 1.65  2005/07/19 18:33:04  rmikk
 * Added a SaveState and RestoreState system for faster loading if no
 * operators are changed.
 *
 * Revision 1.64  2005/06/19 20:31:19  rmikk
 * In LoadDebug mode, line numbers and class where error occurs is
 *   now indicated.
 *
 * Revision 1.63  2005/05/25 02:49:06  rmikk
 * Made a private static method public so that this method will not invoke
 *   the search for all the operators
 *
 * Revision 1.62  2005/05/13 18:03:21  rmikk
 * Add a catch statement so an improper operator does not bring the system
 *   down.
 *
 * Revision 1.61  2005/01/10 17:39:30  rmikk
 * ELiminated a null pointer error
 *
 * Revision 1.60  2005/01/07 17:48:15  rmikk
 * Added code to eliminate regular python scripts from being added as
 *   operators.  Python scripts must have a class
 *
 * Revision 1.59  2004/05/11 00:07:37  rmikk
 * The main program now displays the debug info
 *
 * Revision 1.58  2004/03/15 03:30:15  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.57  2004/01/21 17:56:43  bouzekc
 * main() now only creates a PropertiesLoader (to load IsawProps.dat) and
 * does not allocate storage for it.
 *
 * Revision 1.56  2004/01/21 17:55:08  bouzekc
 * processPaths() now handles the case of an empty String path.
 *
 * Revision 1.55  2004/01/08 22:26:05  bouzekc
 * Added code to handle Wrappable Objects.
 *
 * Revision 1.54  2003/12/15 00:48:27  rmikk
 * Uncommented out some code in main so it can again be used to debug classpath,etc.
 * problems
 *

 * Revision 1.53  2003/12/14 19:18:08  bouzekc
 * Removed unused import statements.
 *
 * Revision 1.52  2003/11/20 20:13:25  rmikk
 * Uses FilenameUtil.URLSpacetoSpace to eliminate the
 *   "%20"'s.
 * Uncommented out the show methods in the main progrqam
 *
 * Revision 1.51  2003/11/18 22:21:41  bouzekc
 * Now finds ISAW_HOME Operators when a non-System ClassLoader is used.
 *

 * Revision 1.50  2003/10/10 00:58:51  bouzekc
 * Removed references to PyOperatorFactory.
 *
 * Revision 1.49  2003/08/25 18:12:34  rmikk
 * Caught a Throwable from Jython scripts
 *
 * Revision 1.48  2003/08/11 18:04:01  bouzekc
 * Now uses PyScriptOperator.
 *
 * Revision 1.47  2003/07/08 15:49:49  rmikk
 * Caught a run-time error in creating Jython Scripts
 *
 * Revision 1.46  2003/06/26 22:31:43  rmikk
 * Added a try -catch structure around the new ScriptOperator
 *   statement to catch the newly thrown exceptionss
 *
 * Revision 1.45  2003/06/17 22:21:12  pfpeterson
 * Format changes to javadocs.
 *
 * Revision 1.44  2003/06/17 21:40:32  pfpeterson
 * Now is thread-safe. This was done by making init() static.
 *
 * Revision 1.43  2003/06/13 15:00:18  pfpeterson
 * Now adds jython operators to the GenericOperator or DataSetOperator
 * list, whichever is appropriate.
 *
 * Revision 1.42  2003/06/12 18:49:22  pfpeterson
 * Updated javadocs to reflect a idiosycracy of Script_Class_List_Handler.
 *
 * Revision 1.41  2003/06/06 21:31:17  pfpeterson
 * Switch order of checks in getInstance(classname) to properly
 * report Interfaces.
 *
 * Revision 1.40  2003/05/28 18:53:46  pfpeterson
 * Changed System.getProperty to SharedData.getProperty
 *
 * Revision 1.39  2003/04/25 22:15:38  pfpeterson
 * Made changes to improve readability, removed some redundant checks,
 * and 'synchronized inittt()' to make the class more thread-safe. When
 * a file is found to not be an operator (or loadable) the debug
 * information is now more informative as well.
 *
 * Revision 1.38  2003/03/06 22:50:53  pfpeterson
 * Added a boolean to decide whether or not a script is reloaded when
 * asked for. This shortens the initial loading of ISAW by ~25% since
 * the scripts are not reloaded at startup.
 *
 * Revision 1.37  2003/02/21 19:35:44  pfpeterson
 * Changed calls to fixSeparator appropriate (not deprecated) method.
 *
 * Revision 1.36  2003/01/29 22:47:57  pfpeterson
 * Added a check for InstantiationError when trying to make a new instance.
 *
 * Revision 1.35  2002/11/27 23:12:10  pfpeterson
 * standardized header
 *
 * Revision 1.34  2002/10/21 12:51:54  rmikk
 * Uncommented out code that created a list of data set operators
 * Sorted the list of data set operators
 * Introduced two methods ,public int getNumDataSetOperators()
 *   and public DataSetOperator getDataSetOperator( int index ),
 *   to access the list of data set operatoors alphabetically.
 * Fixed methods ScompareLess and getOperatorPosition. Now
 *   all operators with the same name( and different argument
 *   lists) can be found with the scripting system
 *
 * Revision 1.33  2002/10/14 15:59:48  pfpeterson
 * Fixed a couple of bugs with finding an operator given its
 * command name. Now finds the last version of the operator in
 * the SortonCommand vector so all versions can be checked
 * against the parameter list of the operator provided.
 *
 * Revision 1.32  2002/09/19 15:57:22  pfpeterson
 * Now uses IParameters rather than Parameters.
 *
 * Revision 1.31  2002/08/19 22:01:49  pfpeterson
 * Added code to create a vector of DataSetOperators. Two lines
 * of code to call the new methods have been commented out until
 * they are needed.
 *
 * Revision 1.30  2002/08/19 15:24:01  pfpeterson
 * Revived some dead code to make Scripts work.
 *
 * Revision 1.29  2002/08/15 18:53:41  pfpeterson
 * More code cleanup and added functionality for extracting
 * operators that are in a jar and removed all dependency on DataSetTools.operator.GenericOperatorList.
 *
 * Revision 1.28  2002/08/13 16:29:13  pfpeterson
 * Reformatted the file to make it easier to read.
 *
 * Revision 1.27  2002/02/22 20:33:46  pfpeterson
 * Operator Reorganization.
 *
 * Revision 1.26  2002/02/08 15:30:28  pfpeterson
 * Extracted method to add directories to list of possible ones into
 * separate method. Also added back the ability to parse GROUP_HOME
 * that is File.pathSeparator delimited.
 *
 * Revision 1.25  2002/01/24 23:14:05  pfpeterson
 * Adds ISAW_HOME to the classpath for loading operators if it is not already there.
 * This is done to make operators work easier for people that don't unpack ISAW.
 *
 * Revision 1.24  2002/01/08 19:48:38  rmikk
 * Eliminated a null pointer exception that occurred when
 * a command is not found.
 *
 * Revision 1.23  2001/12/17 16:38:45  pfpeterson
 * Made the algorithm to prevent double operator listings stonger.
 *
 * Revision 1.22  2001/12/13 18:57:43  dennis
 * -Fixed a documentation error
 * -Fixed and error that would find a command that did not exist
 *
 * Revision 1.21  2001/12/12 19:43:33  pfpeterson
 * Fixed the multiple listing of operators and scripts.
 *
 * Revision 1.20  2001/12/07 21:45:49  pfpeterson
 * Put the order of parsing directories back to correct order.
 *
 * Revision 1.19  2001/12/07 20:40:30  pfpeterson
 * Corrected directory parser to not double load operators and scripts if isaw
 * is installed in $HOME/ISAW. We still should account for multiply defined directories
 * within the GROUP#_HOME listings.
 *
 */
package Command;

import gov.anl.ipns.Util.Sys.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.lang.reflect.Modifier;
import DataSetTools.util.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.operator.DataSet.DataSetOperator;
//import DataSetTools.parameter.*;
import javax.swing.*;

/** 
 * Gets and Saves all scripts and java GenericOperators in the
 * Path(s) of the system property Script_Path
 *
 * <P><B>NOTE:</B> No class should directly extend Operator. Instead
 * they should extend either {@link
 * DataSetTools.operator.Generic.GenericOperator GenericOperator} or
 * {@link DataSetTools.operator.DataSet.DataSetOperator
 * DataSetOperator}. If it does not then they will not be categorized
 * by {@link Command.Script_Class_List_Handler
 * Script_Class_List_Handler}. The effect of this is that the operatr
 * will not be added to menus, will not be found by the help system,
 * and will not be available in scripts.</P>
 */
public class Script_Class_List_Handler  implements OperatorHandler{
    //Contains ordering for Command Names
    private static final Vector SortOnCommand = new Vector();  
    //Contains ordering for file names
    private static final Vector SortOnFileName       = new Vector();
    private static final Vector opList               = new Vector();
    private static final int    MIN_DIR_NAME__LENGTH = 3;

    private   static Vector            dsOpList        = new Vector();
    private   static DataSetOperator[] dsOpListI       = null;
    private   static int               Command_Compare = 257;
    private   static int               File_Compare    = 322;
    private   static boolean           first           = true;
    protected static boolean           reload_scripts  = true;
    public    static boolean           LoadDebug       = false;
    private   static String[]          pathlist        = null;

    private   static boolean           hasJython       = true;

    private   static String            jarFileName     = null;
    
    private   static Hashtable         RestoredFileNames = null;
    /**
     * The System property user.home,ISAW_HOME, GROUP_HOME,
     * GROUP1_HOME,..  are the paths for the operators that are to be
     * "installed"
     */
    public Script_Class_List_Handler(){
           
           Restore();
           init();
           restored = true;
           RestoredFileNames = null;
    }
    
  static public String[] getPathArray(String PathList){
    if(PathList==null || PathList.length()<0) return null;

    // prepare the PathList
    PathList=PathList.trim();
    return StringUtil.split(PathList,";");
  }

    /**
     * Shows the master list
     *
     * NOTE: if mode ==257 the output is sorted by command name.<br>
     * Otherwise it is sorted by the filename(and only includes script
     * operators)
     *
     * @param mode determines order presented 
     */
    public  void show( int mode  ){
        if( SortOnFileName.size() <= 0)
            return;
        int i;
        
        for( i=0; i< getNum_operators(); i++ ){
            int j = -1;
            if( mode == Command_Compare)
                j= ((Integer)SortOnCommand.elementAt(i)).intValue();
            else if( i < SortOnFileName.size() )
                j=((Integer)SortOnFileName.elementAt(i)).intValue();
            
            Operator X = null;
            if(j >= 0)
                X  =((OpnInfo) opList.elementAt(j)).getOperator();
            
            //System.out.print(i+":Command="+X.getCommand());
            if( X != null){
                if( X instanceof ScriptOperator){
                    ScriptOperator Y =(ScriptOperator) X;
                    System.out.println(X.getCommand()
                                       +"  File="+Y.getFileName() );
                }else{
                    System.out.println(X.getCommand()+"");
                }
            }
        }
    }
    
    /** 
     * Utility that tries to create an GenericOperator for a .class
     * filename
     * @param filename The name of the class file.
     * @return An instance of an Operator that subclasses the Generic
     * operator <P> or null if it cannot create this class.
     */
    public GenericOperator getClassInst( String filename ){
        return (GenericOperator)getClassInst(filename,true);
    }

    /**
     *  Static form for getClassInst
     * @param filename   The filename that may contain an operator
     * @param isgeneric   True if the operator is generic
     * @return           returns the operator or null if the file does not
     *                   represent some form of an operator
     */
    static public Operator myGetClassInst(String filename, boolean isgeneric){
        if( filename == null ){
            System.out.println("No Name");
            return null;
        }
        
        if( filename.toUpperCase().endsWith(".ISS")){
          try{
             ScriptOperator X = new ScriptOperator( filename );
             if(X.getErrorMessage().length()<=0)
                return X;
          }catch(Throwable s){
               return null; 
          }
               
          return null;
        }
        if( filename.toUpperCase().endsWith(".PY")){
          if(!hasJython){
           return null;
          }
          // try to get the instance
          Operator op=null;
          try{
            op=new PyScriptOperator(filename);
          }catch(IllegalStateException e){
            op=null;
           
          }catch(ClassCastException e){
            op=null;
           
          }catch(MissingResourceException e){
            op=null;
            
          }catch(ParseError e){
            op=null;
            
          }catch(InstantiationError e){
            op=null;
           
          }catch(Throwable e){
            op = null;
           
          }
          if(op != null)
            if( !((PyScriptOperator)op).isInstallableOperator())
              return null;
          // add it to the proper lists
          
         return op;
         
        }
        // fix up the filename
        filename = FilenameUtil.setForwardSlash(filename);

        // get the pathlist
        if(pathlist==null){
          String mypathlist=null;
          mypathlist = SharedData.getProperty("java.class.path");
          mypathlist=FilenameUtil.setForwardSlash(mypathlist);
          mypathlist=mypathlist.replace(File.pathSeparatorChar,';');
          
          // get the location of ISAW
          String ScrPath=SharedData.getProperty("ISAW_HOME");
          if(ScrPath!=null){
            ScrPath=ScrPath+"/";
            ScrPath=FilenameUtil.setForwardSlash(ScrPath);

            //if the system is using a different classloader (as Tomcat does),
            //the System classpath may not be what we think it is.  If that is
            //the case, we are in danger of ignoring the Operator and Script
            //paths given in IsawProps.dat.  That is why the else clause is
            //added. 11/16/2003 CMB
            if(mypathlist.indexOf(ScrPath+"Isaw.jar;")>=0){
              if(mypathlist.indexOf(ScrPath+";")>=0){
                  // do nothing
              }else{
                mypathlist=ScrPath+";"+mypathlist;
              }
            } else {
              mypathlist=mypathlist+";"+ScrPath;
            }
          }
          // pass the string into something that packs up an array
          pathlist=getPathArray(mypathlist);
        }

        // find where the last '/' is in the filename
        int i = filename.lastIndexOf('/' );
        if( i < 0 ){
            if( LoadDebug){
                System.out.println( "No directory divider");
            }
            return null;
        }

        // the path of the file (everything except the classname)
        String CPath = filename.substring( 0 , i ).trim();
        // the name of the class is between CPath and '.class'
        String classname = filename.substring( i + 1 , filename.length()-6); 
        // CPathFix starts out as CPath
        String CPathFix=CPath;
        
        // if CPath is null or empty then give up
        if( (CPath == null) || (CPath.length()<=0) ){
            if( LoadDebug ) System.out.println("No Name");
            return null;
        }
        
        
        Operator XX=null;
        if( CPath.startsWith("/") || CPath.indexOf(":")>=0 ){
            // go through the classpath to shorten the classname appropriately
          for( int index=0 ; index<pathlist.length ; index++ ){
                String Path1=pathlist[index].trim();
                
                if( Path1 != null ){
                    if( Path1.length() > 0 ){
                        int lastindex=Path1.lastIndexOf('/');
                        if( lastindex == Path1.length()-1){
                            Path1=Path1.substring(0,lastindex).trim();
                        }
                    }
                }
                
                // if Path1 starts CPath then take it out of CPath
                if( CPath.indexOf(Path1 ) == 0 ){
                    CPathFix = CPath.substring( Path1.length()+1);
                    CPathFix =CPathFix.replace('/','.');
                    CPath=CPathFix+"."+classname;
                    
                    try{
                        if(isgeneric)
                            XX=getGenOperator(CPath);
                        else
                            XX=getDSOperator(CPath);
                    }catch(ClassNotFoundException e){
                        if(LoadDebug) System.out.print("(ClassNotFound:"
                                                       +e.getMessage()+") ");
                        return null;
                    }catch(InstantiationException e){
                        if(LoadDebug)System.out.print("(Cannot Instantiate) ");
                        return null;
                    }catch(InstantiationError e){
                        if(LoadDebug)System.out.print("(Cannot Instantiate) ");
                        return null;
                    }catch(IllegalAccessException e){
                        if(LoadDebug) System.out.print("(IllegalAccess:"
                                                       +e.getMessage()+") ");
                        return null;
                    }
                    if( XX == null){
                        return null;
                    }else{
                        if(isgeneric)
                            return (GenericOperator)XX;
                        else
                            return (DataSetOperator)XX;
                    }
                }
            }
            // must not be anywhere in the classpath
            if( LoadDebug ) System.out.print(" (not in classpath) ");
            return null;
        }else{
            // try getting an instance of the operator without shortening
            // the name...this picks up classes in a jar file
            try{
                if(isgeneric){
                    XX=getGenOperator(CPath.replace('/','.')+"."+classname);
                }else{
                    XX=getDSOperator(CPath.replace('/','.')+"."+classname);
                }
            }catch(ClassNotFoundException e){
                if(LoadDebug) System.out.print("(ClassNotFound:"
                                               +e.getMessage()+") ");
                return null;
            }catch(InstantiationException e){
                if(LoadDebug) System.out.print("(Instantiation:"
                                               +e.getMessage()+") ");
                return null;
            }catch(IllegalAccessException e){
                if(LoadDebug) System.out.print("(IllegalAccess:"
                                               +e.getMessage()+") ");
                return null;
            }
            return XX; // whatever it is return it
        }
    }

    /** 
     * Utility that tries to create an Operator for a .class
     * filename
     *
     * @param filename The name of the class file.
     * @param isgeneric Whether the class should be a GenericOperator
     * (true) or DataSetOperator false)
     *
     * @return An instance of an Operator that subclasses the Generic
     * operator <P> or null if it cannot create this class.
     */
    public  Operator getClassInst( String filename, boolean isgeneric ){
        return myGetClassInst(filename,isgeneric);
    }

    /**
     * Gets the Command of the index-th operator
     */
    public String getOperatorCommand( int index ){
        OpnInfo X = getOp1(CommandListIndex(index));
        if(X != null)  
            return X.CommandName;
        else 
            return null;
    }
  
    /**
     * Gets the number of operators in the master operator list
     */
    public int getNum_operators(){
        return opList.size();
    }

    /**
     * Gets the number of data set operators in its master list
    */
    public int getNumDataSetOperators(){
      return dsOpListI.length;
    }
    /**
     * Gets the number of parameters(arguments) of the index-th
     * operator
     */
    public int getNumParameters( int index ){
        OpnInfo X = getOp1(CommandListIndex(index));
        if( X == null )
            return 0;
        else
            return X.NArgs;
    }

    /**
     * Gets the par_index-th parameter of the index-th operator int
     * the master operator list
     */
    public Object getOperatorParameter( int index, int par_index){
        OpnInfo Y = getOp1(CommandListIndex(index));
        if( Y == null ) return null;
        Operator X = Y.getOperator();
        if( X == null)
           return null;
        int n = Y.NArgs;
        if( (par_index<0) || (par_index>=n) ) return null;

       return X.getParameter( par_index);
    }
    
    
    /**
     *  Retrieves the information about an operator. The operator field may be 
     *  null if there is no need for the actual operator.  This is for fast 
     *  loading
     * 
     * @param index  The index of the operator in the Generic operator list
     * @return  A OpnInfo class with CommandName,Title, etc. about the operator
     */
    public OpnInfo getOpInfo( int index){
        return getOp1( CommandListIndex(index));
    }

    /**
     * Gets the index-th operator in the master list of operators
     */
    public Operator getOperator( int index ){
      OpnInfo Y = getOp1( CommandListIndex(index) ); 
      Operator X=null;
      if( Y == null)
         return null;
      if( Y.op != null )
          X = Y.op;
      else if( (Y.FileName != null)&&(Y.FileName.indexOf(".jar") < 0)){
          try{
            
            X=Script_Class_List_Handler.myGetClassInst( Y.FileName, true);
            Y.op = X;
          }catch(Exception s){
             X= null;
          }
      }else if( Y.ClassName != null){
          try{

          X = (Operator)(Class.forName( Y.ClassName)).newInstance();
          Y.op = X;
          }catch(Exception ss){
             X= null;
          }
      }
          
      if(reload_scripts){
        if(X==null)  
          return null;
        else if( X instanceof ScriptOperator ){ // Maybe will get these to
                                           // retain their parameter values too
          String filename = ((ScriptOperator)X).getFileName();
          return new ScriptOperator( filename );
        }else{ //Will try to have these operators retain their parameter values
          return X;
        }

      }else{
        return X;
      }
    }

    /**
     * Gets the index-th operator in the master list of DataSet operators
     */
    public DataSetOperator getDataSetOperator( int index ){
        if( index < 0)
           return null;
        if( index >= dsOpListI.length)
           return null;
        return dsOpListI[index];
    }

    /** Gets the position in the master list of the first operator
      *   whose command name is CommName.
    */
    public int getOperatorPosition( String CommName){
        int i = find(CommName , Command_Compare);
        if( i < 0 )
            return i;
        if( getOperatorCommand(i) == null) 
            return -1;
        if(! getOperatorCommand(i).equals( CommName))
            return -1;
        int j = i-1;
        while( j >= 0 ){
            if( getOperatorCommand( j ).equals(CommName)){
                i = j;
                j--;
            }
            else
                return i;
        }
        return i;
    }

    /**
     * Gets the position in the master list of the first operator
     * whose command name is CommName.
     */
    public int getOperatorPosition1( String CommName){
        int i = find(CommName , Command_Compare);
        
        if( i < 0 )
            return i;
        if( getOperatorCommand(i) == null) 
            return -1;

	for( int j=i ; j>=0 ; j-- ){
	    if( getOperatorCommand(j).equals( CommName))
		return j;
	}
	return -1;

    }

    /**
     * Gets an operator given its Command Name
     */
    public Operator getOperator( String CommName){
        int i = getOperatorPosition( CommName);
        if( i < 0)
            return null;
        return getOperator( i );
    }

    static private void toggleDebug(){
        try{
            if( System.in.available()>0){
                char c = (char)(System.in.read());
                if( c=='k')
                    LoadDebug = !LoadDebug;
                System.out.print("K="+c);
            }
        }catch( IOException s){
            // let it drop on the floor
        }
    }

    /** 
     * Method for finding operators and scripts.
     */
    static private synchronized void init(){  
        if( !first) return;

        jarFileName= null;
        processIsaw();
        /*for( int i = 0 ; i < GenericOperatorList.getNum_operators(); i++){
          Operator op = GenericOperatorList.getOperator( i );
          if( op instanceof GenericOperator) add( op );
          }*/
        first = false;  
        toggleDebug(); 
        
        Vector includeVec = new Vector();
        // add $HOME/ISAW to the path
        String ScrPaths2 = SharedData.getProperty( "user.home" );
        //String ScrPaths2 = "/IPNShome/hammonds";
        if( ScrPaths2 != null ){
            //System.out.print("**********\n"+"USER_HOME:");
            includeVec=addDir(ScrPaths2+File.separator+"ISAW",includeVec);
        }
        int g = 0;
        
        // add the different GROUP#_HOMEs to the path
        String ScrPaths1 = SharedData.getProperty( "GROUP_HOME" );
        while(ScrPaths1 != null){
            //System.out.print("**********\n"+"GROUP"+g+"_HOME:");
            includeVec=addDir(ScrPaths1,includeVec);
            g++;
            String suff=""+g;
            suff=suff.trim();
            ScrPaths1 = SharedData.getProperty( "GROUP"+suff+"_HOME" );
        }
        
        
        // add where ISAW lives to the path
        String ScrPaths = SharedData.getProperty( "ISAW_HOME" );
        if( ScrPaths != null ){
            //System.out.print("**********\n"+"ISAW_HOME:");
            includeVec=addDir(ScrPaths,includeVec);
        }
        
     
        
        // remove redundant listings from the path
        for( int i=0 ; i<includeVec.size() ; i++ ){
            for( int j=i ; j<includeVec.size() ; j++ ){
                if( i==j )continue; // do the next j loop
                // shorten some other lines in the inner for loop
                String ith=(String)includeVec.elementAt(i);
                String jth=(String)includeVec.elementAt(j);
                // check that i and j are unique
                if(ith.equals(jth)){
                    includeVec.remove(j);
                    j--;
                }
            }
        }
      
        for( int i=0 ; i<includeVec.size() ; i++ ){
            processPaths((String)includeVec.elementAt(i));
        }
       if( dsOpListI == null){
       
          dsOpListI = new DataSetOperator[ dsOpList.size()];
          for( int i=0 ; i < dsOpListI.length ; i++){
                dsOpListI[i]= (DataSetOperator)(dsOpList.elementAt(i));
          }
          dsOpList = null;
          java.util.Arrays.sort( dsOpListI, new CommandCompare( dsOpListI));
       }
        
        toggleDebug(); 
        //Set last modified on all Generic operators
        for( int i=0; i < opList.size(); i++){
          OpnInfo opnInf = (OpnInfo)(opList.elementAt(i));
          String filename = opnInf.FileName;
          if( filename == null)
             System.out.println("null filename for "+opnInf.CommandName);
          else{
            File F = new File( filename);
            if( !F.exists())
              System.out.println("File does not exist for "+opnInf.CommandName);
            else
               opnInf.lastmodified= F.lastModified();
          }
        }
        Save(true);
    }  // end of init()

    /**
     * This class is a comparator for two operators with respect of
     * their command name
     */
    static class CommandCompare implements Comparator{
       DataSetOperator[] dslist;
       public CommandCompare( DataSetOperator[] dslist){
          this.dslist= dslist;
       }
       
       /**
        * Compares two DataSetOperators according to their command name
        */  
       public int compare(Object o1, Object o2){
         if( o1 == null)
          if( o2 == null)
            return 0;
          else 
            return -1;
          if( o2 == null)
            return +1;
          if( !( o1 instanceof DataSetOperator))
             return 0;          
          if( !( o2 instanceof DataSetOperator))
             return 0;
           return ((DataSetOperator)o1).getCommand().compareTo(((DataSetOperator)o2).getCommand()); 
  
       }
       /** returns false. No two comparators are the same
       */
       public boolean equals(Object obj){
          return false;
       }

    }
      
    /**
     * Method to simplify code since all directories are parsed in the
     * same manner. This adds the directories "dir/Operators" and
     * "dir/Scripts" if they exist. The directory specified by the
     * String can be a list seperator by File.pathSeparator.
     */
    static private Vector addDir(String dir, Vector include){
        String path=new String(dir+File.pathSeparator);
        int last=path.indexOf(File.pathSeparator);
        
        //System.out.print("("+path+")");
        
        while(path.indexOf(File.pathSeparator)>0){
            dir=path.substring(0,last);
            path=path.substring(last+1,path.length());
            last=path.indexOf(File.pathSeparator);
            //System.out.println("PATH:"+path);
            if( dir!=null && dir.length()>0 ){
                dir=standardizeDir(dir);
                //System.out.println(dir);
                if(isJar(dir)){
                    include.add(dir.substring(0,dir.length()-1));
                }else if(existDir(dir)){
                    if(existDir(dir+"Operators")){
                        //System.out.println(dir+"Operators");
                        include.add(dir+"Operators");
                    }
                    if(existDir(dir+"Scripts")){
                        //System.out.println(dir+"Scripts");
                        include.add(dir+"Scripts");
                    }
                }
            }		
        }	 
        return include;
    }

  static private String standardizeDir( String dir ){
        // remove whitespace from the name
        dir.trim();
        // add a '/' at the end (setForwardSlash will remove redundancies)
        dir=dir+'/';
        // switch '\' to '/' and remove any extra of either
        dir=FilenameUtil.setForwardSlash(dir);

        return dir;
    }

    /**
     * This checks that the given file exists and is a jar file. If
     * any of these criteria are not fulfilled then false is returned.
     */
    static private boolean isJar( String file ){
        // the filename must be at least a certain length long
        if(file.length()<=MIN_DIR_NAME__LENGTH) return false;
        if(file.endsWith("/")){      // chop off the trailing '/'
            file=file.substring(0,file.length()-1);
        }
        // it better end with .jar to be a jar file
        if(! file.endsWith(".jar")) return false;

        // now two tests that depend on it really being a file
        File f=new File(file);
        if(! f.exists() ) return false;
        return f.isFile();
    }

    static private boolean existDir( String dir ){
        // the directory must be at least a certain length long
        if(dir.length()<=MIN_DIR_NAME__LENGTH)return false;
        // check if the directory exists
        File Dir = new File(dir);
        return Dir.isDirectory();
    }

    static private void processIsaw(){
        String  className  = null;
        String  classFile  = null;
        boolean injar      = false;

        className='/'+Script_Class_List_Handler.class.getName()
                                                    .replace('.','/')+".class";
        classFile=Script_Class_List_Handler.class.getResource(className)
                                                                   .toString();
        if( (classFile!=null) && (classFile.startsWith("jar:")) ) injar=true;
        // the start is to remove the jar or file
        // the end is to remove the classname
        int n=5;
        if( injar) n=4;
        classFile=classFile.substring(n,classFile.indexOf(className));
        // then change the separator to forward slash (should be already)
        classFile=FilenameUtil.setForwardSlash(classFile);
        classFile=FilenameUtil.URLSpacetoSpace( classFile );
        if(injar){ // we are working from a jar file
            // remove a little bit more from classFile
            classFile=classFile.substring(5);
            classFile= classFile.substring(0,classFile.length()-1);
           
            processDataSetOperators(classFile,true);
           
            if( LoadDebug) System.out.println("----PATH="+classFile);
            jarFileName = classFile;
            ProcessJar(classFile,opList);
            jarFileName = null;
        }else{     // isaw is unpacked
            
            processDataSetOperators(classFile+"/DataSetTools/operator/DataSet",
                                                                        false);
           
            File opDir=new File(classFile+"/DataSetTools/operator/Generic");
            if(opDir.exists() && opDir.isDirectory()){
                if( LoadDebug) System.out.println("----PATH="+classFile+"/DataSetTools/operator/Generic");
                ProcessDirectory(opDir,opList);
            }
        }
    }

    /**
     * Method to populate the vector of DataSetOperators.
     */
    static private void processDataSetOperators(String dir, boolean inJar){
        if(LoadDebug) System.out.println("-----DSPATH="+dir);
        if( restored)
           return;
        if(inJar){
            ZipFile zf=null;
            try{
                zf=new ZipFile(dir);
            }catch(IOException e){
                return; // something wrong, just exit out
            }
            Enumeration entries=zf.entries();
            ZipEntry entry=null;
            String name=null;
            String matchname=
                FilenameUtil.setForwardSlash("DataSetTools/operator/DataSet");
            if(!dir.endsWith("Isaw.jar"))return; // only works with Isaw
            
            // go through the entries
            while(entries.hasMoreElements()){
                entry=(ZipEntry)entries.nextElement();
                name = FilenameUtil.setForwardSlash(entry.getName());
                if( (name.endsWith(".class")) && (name.indexOf(matchname)>=0) ){
                    add(name,dsOpList);
                }
            }
        }else{
            File opDir=new File(dir);
            if( opDir.exists() && opDir.isDirectory() ){
                    ProcessDirectory(opDir,dsOpList);
            }
        }
        if(LoadDebug) System.out.println("=====Number of DataSet operators: "
                                         +dsOpList.size());
    }

    static private void processPaths( String ScrPaths){
        ScrPaths.trim();
        if( LoadDebug) System.out.println("----PATH="+ScrPaths);

        ScrPaths=ScrPaths.replace(File.pathSeparatorChar,';'); 
        if( ScrPaths.lastIndexOf(';') != ';')
            ScrPaths = ScrPaths+";";
        
        String[] paths=StringUtil.split(ScrPaths,";");
        for(int i=0 ; i<paths.length ; i++ ){
          //ignore empty elements
          if( !paths[i].trim( ).equals( "" )  ) {
            if(isJar(paths[i])){
                jarFileName= paths[i];
                ProcessJar(paths[i],opList);
                jarFileName = null;
            }else{
                File Dir=new File(paths[i]);
                if( Dir.isDirectory() ){
                    ProcessDirectory( Dir ,opList);
                }else{
                    // do something
                }
            }
          }
        }
        
        return;
    }

    static private void ProcessJar( String jarname, Vector opList ){
        ZipFile zf=null;
        try{
            zf=new ZipFile(jarname);
        }catch(IOException e){
            return; // something wrong, just exit out
        }
        Enumeration entries=zf.entries();
        ZipEntry entry=null;
        String name=null;
        String matchname="Operators";
        if(jarname.endsWith("Isaw.jar")){
            matchname=
                FilenameUtil.setForwardSlash("DataSetTools/operator/Generic/");
        }

        // go through the entries
        while(entries.hasMoreElements()){
            entry=(ZipEntry)entries.nextElement();
            name=FilenameUtil.setForwardSlash(entry.getName());
            if( (name.indexOf(matchname)>=0) && (name.endsWith(".class")) ){
                //System.out.println("ENTRY:"+name);
                add(name,opList);
            }
        }
    }

    static private  void ProcessDirectory( File Dir, Vector opList ){
        File F[];
        F = new File[0];
        F = Dir.listFiles();
        
        for( int i = 0; i < F.length; i++){
            if( F[i].isDirectory()){
                ProcessDirectory( F[i], opList );
            }else if( F[i].isFile() ){
                if( isFileExtension(F[i]))
                    ProcessFile( F[i], opList );
            }
        }
    }

    static private  void ProcessFile( File file , Vector opList){
        add( file.toString() , opList);
    }

    static private  boolean isFileExtension( File F){
        String nm = F.getName();
        if(nm == null )
            return false;
        int i = nm.lastIndexOf( '.');
        if( i < 0 )
            return false;
        String ext = nm.substring( i + 1).toUpperCase();

        //System.out.println(ext);
        if( ext.equals("ISS") || ext.equals("CLASS") || ext.equals("JAR") || ext.equals("PY")){
            return true;
        }else{
            return false;
        }
    }
    
    static private  void add( String filename , Vector opList){
        int i;
        
        // only deal with real filenames
        if(filename == null ) 
           return;
        if( LoadDebug) 
           System.out.print( "Processing "+filename+":");
        if( RestoredFileNames != null)
           if( RestoredFileNames.containsKey( filename))
              return;
        if( RestoredFileNames != null)                  //Restored correctly
           if( opList != Script_Class_List_Handler.opList)//DS operator
              return;
        if(RestoredFileNames !=null)//have already checked that the jar file is OK
           if( jarFileName != null)
              return;
        i = filename.lastIndexOf('.');
        // give up if there is not a '.' near the end of the name (for class)
        if( i< 0 ) return;

        String Extension = filename.substring( i + 1 );
        if( Extension.equalsIgnoreCase("iss")){ // it is a script
            try{ScriptOperator X = new ScriptOperator( filename );
            if(X.getErrorMessage().length()<=0){
                OpnInfo Xop= new OpnInfo(X);
                Xop.FileName= filename;
                if( jarFileName != null)
                   Xop.FileName = jarFileName;
                add( Xop );
                if( LoadDebug )
                    System.out.println( "OK" );
            }else if( LoadDebug ){
                System.out.println( "NO "+X.getErrorMessage() );
            }
            }
           catch( Throwable uu)
            {
               if( LoadDebug )
                System.out.println( "NO "+uu.getMessage() );
            }
        }else if(Extension.equalsIgnoreCase("py")){
          // error out if there is no jython
          if(!hasJython){
            if(LoadDebug) System.out.println("(No Jython) NO");
            return;
          }
          // try to get the instance
          Operator op=null;
          try{
            op=new PyScriptOperator(filename);
          }catch(IllegalStateException e){
            op=null;
            if(LoadDebug) System.out.print("(IllegalStateException) ");
          }catch(ClassCastException e){
            op=null;
            if(LoadDebug) System.out.print("(ClassCastException) ");
          }catch(MissingResourceException e){
            op=null;
            if(LoadDebug) System.out.print("(MissingResourceException) ");
          }catch(ParseError e){
            op=null;
            if(LoadDebug) System.out.print("(ParseError on line "+e.linenum()+")");
          }catch(InstantiationError e){
            op=null;
            if(LoadDebug) System.out.print("(InstantiationError) ");
          }catch(Throwable e){
            op = null;
            if(LoadDebug) System.out.println( "Err="+e.toString());
          }
          if(op != null)
            if( !((PyScriptOperator)op).isInstallableOperator())
               op = null;
          // add it to the proper lists
          if(op!=null){
            if(op instanceof GenericOperator){
              OpnInfo opn= new OpnInfo( op);
              opn.FileName= filename;
              add(opn);
              
            }else if(op instanceof DataSetOperator){
              dsOpList.addElement(op);
            }else{
                if(LoadDebug)
                  System.out.println("(Not Generic or DataSet) NO");
                return;
            }
            if(LoadDebug) System.out.println( "OK" );
          }else{
            if(LoadDebug) System.out.println("NO");
          }
        }else if(Extension.equalsIgnoreCase("class")){ // it is a class
            boolean isgeneric=!(opList==dsOpList);
            Operator X = myGetClassInst( filename, isgeneric);
            if( X != null ){
                if(LoadDebug) System.out.println( "OK" );
                if(isgeneric){  // add in the normal way
                    OpnInfo Xopn= new OpnInfo(X);
                    Xopn.FileName = filename;
                    if( jarFileName != null)
                       Xopn.FileName = jarFileName;
                    add( Xopn );
                }else{ // add it to the DSList
                    opList.addElement(X);
                }
            }else{
                if(LoadDebug) System.out.println( "NO ");
            }
        }else{
            return;
        }
    }

    static private  void add( Operator op){
        System.out.println("in add opn only should not be here***************");
        opList.addElement( new OpnInfo(op) );
        
        insert( opList , SortOnCommand );
        insert(opList , SortOnFileName );
    }

    static private void add( OpnInfo opInf){
      opList.addElement( opInf);
     
      insert( opList , SortOnCommand );
      insert(opList , SortOnFileName );
      
    }
    /**
     * insert before 
     */
    static private  void insert( int n_toInsert, int pos_insert ,
                                                            Vector rankList  ){
        if((pos_insert < 0) ||(pos_insert >=rankList.size()))
            pos_insert = rankList.size();
        rankList.add( pos_insert, new Integer( n_toInsert ) );
        /*  System.out.print( pos_insert+" ");
            for ( int i = 0; i < rankList.size();i++)
            {int j= ((Integer)(rankList.elementAt(i))).intValue();
            Operator X = (Operator)(opList.elementAt( j ) );
            System.out.print( X.getCommand()+" " );
            }
            System.out.println("");
        */
    }

    static private  void insert( Vector opList, Vector rankList ){
        if( opList.size()<= 0)
            return;
        
        int n = opList.size() -1;
        String CommandName = ((OpnInfo)opList.elementAt(n)).CommandName;
        String FileName =((OpnInfo)opList.elementAt(n)).FileName;
        String ClassName =((OpnInfo)opList.elementAt(n)).ClassName;
        int indx = -1;
        if( rankList.equals( SortOnCommand ) )
            indx = find( CommandName, Command_Compare );
        else if(  ClassName.trim().equals("Command.ScriptOperator"))
            indx = find( FileName, File_Compare );
        else 
            return;
        
        insert( n, indx, rankList);
    }

    /**
     * binary search for inserting only
     * @return the first index in the appropriate rank list that is
     * more than or = to op
     */
    static private  int find( String textt, int mode ){
        int mid, first, last;
        boolean done = false;
        boolean less;
        Vector V;
        
        if( textt == null )
            return -5;  
        if( mode == Command_Compare )
            V = SortOnCommand;
        else
            V = SortOnFileName;
        first = 0;
        last = V.size() -1;
        done = first > last; 

        less = true;
        if( done){
            mid = 0;
            less = false;
        }else{
            for( mid =(first + last ) / 2; !done ; ){
                int pos = ((Integer)(V.elementAt(mid))).intValue();
                String CommandName= ((OpnInfo)opList.elementAt(pos)).CommandName;
                String FileName =((OpnInfo)opList.elementAt(pos)).FileName;
               
                less = false;
                 
                if( mode == Command_Compare){
                    if( ScompareLess(CommandName , textt))
                        less = true;
                }else{
                    if( !(V.equals( SortOnFileName )  ) )
                        return -12;
                   
                    if( ScompareLess(FileName , textt) )
                        less = true;
                    else if( FileName.equals( textt ) )
                        return mid;
                }
                if( less){
                    first = mid +1;
                }else{
                    last = mid -1;
                }
                if( first > last)
                    done = true;
                else
                    mid =(first + last )/2;
                
            }
        }
        if( less )
            return mid +1;
        else
            return mid;
    }

    private OpnInfo getOp1( int index ){
        if( (index<0) || (index>=opList.size()) ) return null;

        OpnInfo X = (OpnInfo)(opList.elementAt( index));
        return X;
   }

    /**
     * Method to get an instance of the operator if possible. The
     * method checks for NoClassDefFoundError (see java docs) and
     * removes parts of the packagename (from the beginning) until
     * either nothing is left or the class is found.
     *
     * @param classname the package qualified name of the class.
     *
     * @return A generic operator or null.
     *
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    static private Operator getOperatorInst( String classname ) throws
        ClassNotFoundException, InstantiationException, IllegalAccessException{
        // make sure we have something to get
        if(classname==null) return null;

        while(classname.length()>0){
            try{
                Class C = Class.forName( classname );
                int modifier=C.getModifiers();
                if(Modifier.isInterface(modifier)){
                  if(LoadDebug) System.out.print("(Interface)");
                  return null;
                }
                if(Modifier.isAbstract(modifier)){
                  if(LoadDebug) System.out.print("(Abstract)");
                  return null;
                }
                if(Operator.class.isAssignableFrom(C)){
                  return (Operator)C.newInstance();
                } else if( Wrappable.class.isAssignableFrom(C) ) {
                  //we need to create a JavaWrapperOperator
                  return new JavaWrapperOperator( ( Wrappable )C.newInstance() );
                } else{
                  if(LoadDebug) System.out.print("(Not Operator) ");
                  return null;
                }
            }catch(NoClassDefFoundError e){
                // the package name and the classname do not agree,
                // trim off the first part of the package and try
                // again
                int index=classname.indexOf(".");
                if(index>0)
                    classname=classname.substring(index+1,classname.length());
                else{
                    if( LoadDebug)System.out.print("Cannot Create Class Def");
                    return null;
                }
            }catch( ClassNotFoundException e3){
                 if(LoadDebug)
                    System.out.print(" Class not found ");
                 return null;     
            }catch( Throwable e1){
              String[] SS=ScriptUtil.GetExceptionStackInfo(e1,true,1);
              String S="";
              if( SS!=null) if(SS.length>=1)
                 S=SS[0];
              if(LoadDebug)if(S.length()>1) System.out.print("err="+S);
              return null;
            }
        }
        // nothing works, just return null
        return null;
    }

    /**
     * Method to get an instance of the operator if possible. The
     * method calls getOperatorInst(classname) and checks if the
     * Operator is a GenericOperator.
     *
     * @param classname the package qualified name of the class.
     *
     * @return A generic operator or null.
     *
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    static private GenericOperator getGenOperator( String classname ) throws
        ClassNotFoundException, InstantiationException, IllegalAccessException{
        Operator op=getOperatorInst(classname);
        if(op instanceof GenericOperator)
            return (GenericOperator)op;
        else
            return null;
    }

    /**
     * Method to get an instance of the operator if possible. The
     * method calls getOperatorInst(classname) and checks if the
     * Operator is a DataSetOperator.
     *
     * @param classname the package qualified name of the class.
     *
     * @return A generic operator or null.
     *
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    static private DataSetOperator getDSOperator( String classname ) throws
        ClassNotFoundException, InstantiationException, IllegalAccessException{
        Operator op=getOperatorInst(classname);
        if(op instanceof DataSetOperator)
            return (DataSetOperator)op;
        else
            return null;
    }

    private int CommandListIndex(int index){
        if( (index<0) || (index>=SortOnCommand.size()) ) return -1;

        Integer X = (Integer)SortOnCommand.elementAt(index);
        int j = X.intValue();
        return j;
    }

    /**
     * Utility to read a space separated String from the console.
     * 
     * @return The String that was read.
     */
    public static String getString(){
        char c=0;
        String S ="";
        try{
            while( c<32)
                c = (char)System.in.read();
            
            while( c>=32){
                S = S + c;
                c = (char)System.in.read();
            }
        }catch(IOException e){
            // let it drop on the floor
        }
        //System.out.println("getString="+S);
        return S;
    }

    static private  boolean ScompareLess( String s1, String s2){
        if( s1 == null ) return ( s2!=null );

	if(s1.compareTo(s2)<0)  //$$$ had <=
	    return true;
	else
	    return false;
    }
    
    
    
    /**
     * This method will save the static information from this class to the file
     * user.home\ISAW\SCLH.iii if create is true. If create is false, it will delete
     * the file.
     * @param create   if true the file will be created otherwise the file will be deleted
     * 
     */
    public static void Save( boolean create){
      String S = System.getProperty("user.home");
      if(S == null){ 
        JOptionPane.showMessageDialog( null, "There is no User Home directory");
        return;
      }
      if( !S.endsWith(File.separator))
        S+=File.separator;
      if( !(new File(S)).exists()){
         JOptionPane.showMessageDialog(null,"The User HOME directory does not exist");
         return;
      }
      
      String filename= S+"ISAW"+File.separator;
      if( !(new File(filename)).exists()){
        
         try{ 
           if(!(new File(filename)).mkdir()){
             JOptionPane.showMessageDialog( null , "Cannot Create ISAW directory in your home directory");
             return;
            }
         }catch(Exception ss){
            JOptionPane.showMessageDialog( null, "Cannot create ISAW directory in your home directory:"+
                     ss.toString());
            return;
         }
      }
      filename = filename+"SCLH.iii";
      File F = new File( filename); 
      if(!create){
        
         if(!F.exists())
           return;
           F.delete();
           return;
      }
      if(first)
        return;
      try{
        FileOutputStream fout= new FileOutputStream( F);
        ObjectOutputStream Fout = new ObjectOutputStream(fout);
        Fout.writeObject( System.getProperty("ISAW_HOME",""));
        if(System.getProperty("GROUP_HOME")!= null)
           Fout.writeObject( System.getProperty("GROUP_HOME"));
        for( int i=1; System.getProperty("GROUP"+i+"_HOME") != null;i++)
           Fout.writeObject( System.getProperty("GROUP"+i+"_HOME"));
        Fout.writeObject("XXXX");
        String className='/'+Script_Class_List_Handler.class.getName()
                                                            .replace('.','/')+".class";
        
        String classFile=Script_Class_List_Handler.class.getResource(className)
                                                                          .toString();
        int n=5;
        if( classFile.indexOf(".jar")>=0) n=4;
          classFile=classFile.substring(n);
               // then change the separator to forward slash (should be already)
        classFile=FilenameUtil.setForwardSlash(classFile);
        classFile=FilenameUtil.URLSpacetoSpace( classFile );
        long TimeStamp = (new File(classFile)).lastModified();
        Fout.writeLong( TimeStamp);
        Fout.writeObject( System.getProperty("java.class.path"));
        Fout.writeObject( dsOpListI);
        Fout.writeObject( opList);

        Fout.writeObject( SortOnFileName);
        Fout.writeObject( SortOnCommand);
        Fout.close();
     }catch(Exception s){
        JOptionPane.showMessageDialog(null,"Cannot save:"+s.toString());
        F.delete();
        s.printStackTrace();
        Save( false );
        return;
     }         
     
    }
    
    
    
    static boolean restored=false;
    
    private static boolean Message(String S, ObjectInputStream oinp){
        System.out.println(S);
        try{
            oinp.close();
        }catch(Exception s){
        }
        return false;
    }  
    
    /**
     * This methods restores the Script_Class_List_Handler system to the system via
     * the previous Save.  Also the System Property, Restored, will be set to "true".
     * @return true if System is restored. If there is no file or an error occurs,
     *                   false is returned.
     */
    public static synchronized boolean Restore(){
      if(!first)
          return true;
      if( restored)
        return true; 
      DataSetTools.util.SharedData sd = new DataSetTools.util.SharedData();
      
      RestoredFileNames = null;
      String S = System.getProperty("user.home");
      
      if(S == null){ 
        JOptionPane.showMessageDialog( null, "There is no User Home directory");
        return false;
      }
      if(!S.endsWith( File.separator))
        S =S+File.separator;
      String fileName= S+"ISAW"+File.separator+"SCLH.iii";
      if(!( new File(fileName)).exists()){
         System.out.println("Didn't find cached operator file: " + fileName );
         System.out.println("searching for all available operators....");
         System.out.println("This is normal for the first time a version of ISAW is run;");
         System.out.println("it will just take somewhat longer to start.");
         return false;
      }
      try{
        FileInputStream fin = new FileInputStream( fileName);
        ObjectInputStream Fin = new ObjectInputStream( fin);
        S = (String)Fin.readObject();
        if( !S.equals(System.getProperty("ISAW_HOME","")))
           return Message("ISAW HOME did not match",Fin);
        
        S= (String)Fin.readObject();
        if( !S.equals("XXXX"))
        if(!S.equals( System.getProperty("GROUP_HOME")))
           return Message("Group HOme did not match", Fin);
        boolean done = S.equals("XXXX");
        for( int i=1; !done; i++){
          S= (String)Fin.readObject();
          if( S.equals("XXXX"))
            done = true;
          else if( !S.equals( System.getProperty("GROUP"+i+"_HOME" )))
             return Message("GROUP"+i+"_HOME did not match", Fin);
        }
           
        String className='/'+Script_Class_List_Handler.class.getName()
                                                    .replace('.','/')+".class";
        String classFile=Script_Class_List_Handler.class.getResource(className)
                                                                      .toString(); 
        int n=5;
        if( classFile.indexOf(".jar")>=0) n=4;
        classFile=classFile.substring(n);
               // then change the separator to forward slash (should be already)
        classFile=FilenameUtil.setForwardSlash(classFile);
        classFile=FilenameUtil.URLSpacetoSpace( classFile );
        long TimeStamp = (new File(classFile)).lastModified();
        
        if(Fin.readLong() != TimeStamp)
        {
           String message = "Time stamp on cached operator file didn't match. \n" +
                            "Searching for all available operators.... \n" +
                            "This is normal for the first time a version of ISAW is run \n" + 
                            "after it's modified; it will just take somewhat longer to start.";

           return Message(message, Fin);
        }

        S=(String)Fin.readObject();
        if(!S.equals( System.getProperty("java.class.path")))
           return Message("ClassPath did not match", Fin);
        
        dsOpListI=(DataSetOperator[])Fin.readObject( );
         
        Vector opListCopy=(Vector)(Fin.readObject());
        assign(opList, opListCopy);
        
        if(!checkDates(opList)){
          System.out.println("Dates did not check on Restore");
          opList.clear();
          SortOnFileName.clear();
          SortOnCommand.clear();
          dsOpList.clear();
          first= true;
          RestoredFileNames = null;
          Save( false );
          return false;
          
        }
        assign(SortOnFileName,(Vector)(Fin.readObject()));
        assign(SortOnCommand,(Vector)(Fin.readObject( )));
        Fin.close();    
               
      }catch(Throwable s){
         JOptionPane.showMessageDialog(null,"Could not retrieve:"+s.toString());
         Save( false );
         s.printStackTrace();
         opList.clear();
         SortOnFileName.clear();
         SortOnCommand.clear();
         dsOpList.clear();
         first= true;
         RestoredFileNames = null;
         restored=false;
         return false;
      }      
      restored=true;
      return true;
     
    }
    
    private static void assign( Vector V, Vector V1){
      if( V== null)
         return;
      V.clear();
      V.addAll( V1);
      
    }
    
    private static boolean checkDates( Vector V){
      if( V== null)
        return false;
      RestoredFileNames = new Hashtable();
      for( int i=0; i< V.size(); i++){
         if(!(V.elementAt(i) instanceof OpnInfo))
           return false;
         OpnInfo opn= (OpnInfo)(V.elementAt(i));
         String fileName= opn.FileName;
         RestoredFileNames.put( fileName,"");
         if( fileName == null)
           return false;
         File F= new File( fileName);
         if(!F.exists())
           return false;
         opn.op = null;
         if( F.lastModified() != opn.lastmodified){
           if( fileName.indexOf(".jar")>0){
             try{
               Operator op= (Operator)Class.forName( opn.ClassName).newInstance();
               if( op.getCommand().equals(opn.CommandName)){
                 opn.op=op;
                 opn.CatList= op.getCategoryList();
                 opn.NArgs=op.getNum_parameters();
                 opn.Title=op.getTitle();
                 opn.isHidden= op instanceof HiddenOperator;
                 
               }else
                 return false;
               
             }catch(Throwable s){
                return false;
             }
           }else{
             Operator op = myGetClassInst(fileName, true);
             if(op == null)
               return false;
             if( !op.getCommand().equals(opn.CommandName))
               return false;
             opn.op =op;
             opn.CatList= op.getCategoryList();
             opn.NArgs=op.getNum_parameters();
             opn.Title=op.getTitle();
             opn.isHidden= op instanceof HiddenOperator;
           }
              
          
         }
         
      }
      return true;
    }
    
    /**
     * 
     *
     */
    public static void ReHash(){
       
       SortOnCommand.clear();  
          //Contains ordering for file names
       SortOnFileName.clear();
       opList.clear();
       

       dsOpList.clear();
       dsOpListI       = null;
       
       first           = true;
       reload_scripts  = true;
       LoadDebug       = false;
       pathlist        = null;

       hasJython       = true;
       init();

       
    }
    /**
     * Test program for this module.  No arguments are used 
     */
    public static void main( String args[] ){
      //load up IsawProps.dat
        new PropertiesLoader("IsawProps.dat"  );
        
        Script_Class_List_Handler.LoadDebug = true;
        Script_Class_List_Handler BB = new Script_Class_List_Handler();
        System.out.println("-------------------------------------------"
                           +"-------------------------------");
        System.out.println("=====Number of Generic operators: "
                           +BB.getNum_operators());
        BB.show(257);
       
        
        System.exit( 0 );
    }
    
 
}
