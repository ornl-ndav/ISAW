/*
 * File:  findnexus.java
 *
 * Copyright (C) 2083, Ruth Mikkelson
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
 * Contact :  Ruth Mikkelson <mikkelsonr@uwstout.edu> *          
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the The Spallation Neutron Source at Oakridge
 * National Laboratory, Oakridge, Tennessee
 *
 * For further information, see  <http://ftp.sns.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 */
 package Operators.Generic.System;
 import DataSetTools.operator.*;
 import DataSetTools.operator.Generic.*;
 import gov.anl.ipns.Parameters.*;
 import java.util.*;
 import org.python.util.*;
 import org.python.core.PyObject;
 import DataSetTools.operator.Generic.Special.GenericSpecial;
 
 // This is a template for creating a Java operator that invokes the Jython 
 // interpreter to execute a Jython method( that needs no initialization)
 
 public class findnexus extends GenericSpecial{
    
    private static String filename="{ISAW_HOME}/Scripts/findnexus.py";
    private static String methodName = "findnexus";
    private static String[] prompts={"runNum", "instrument", "Facility", 
               "proposal", "collection", "verbose" ,"Other directories", 
               "extension", "recurselevel"};
               
   public findnexus(){
      super("findnexus");
   }
   
   
   public void setDefaultParameters(){
   
     parameters= new Vector();
     addParameter( new StringPG( prompts[0], ""));
     addParameter( new StringPG( prompts[1], ""));
     addParameter( new StringPG( prompts[2], ""));
     addParameter( new StringPG( prompts[3], ""));
     addParameter( new StringPG( prompts[4], ""));
     addParameter( new BooleanPG( prompts[5], false));
     addParameter( new DataDirPG( prompts[6], ""));
     addParameter( new StringPG( prompts[7], ".nxs"));
     addParameter( new IntegerPG( prompts[8], 6));    
   
   }
   
   public String getCommand(){
      return "findnexus";
   }
   
   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("Finds a nexus file satisfying all"+
               " the given properties in the arguments\n");
      S.append( "@param runNum       The run number\n");
      S.append( "@param instrument   The instrument name\n");
      S.append( "@param Facility     The Facility name\n");
      S.append( "@param proposal     The proposal identifier\n");
      S.append( "@param collection   The collection string\n");
      S.append( "@param verbose      True or false\n");
      S.append( "@param Dirs         "+      		
               "   The other directory(s) to look in. Separate multiple\n");
      S.append( " directories with semicolons(not implemented yet)\n");
                 
      S.append( "@param extension    The extension for the filenames\n");
      S.append( "@param recurselevel  "+
               "The number of level of subdirectories to search.\n");
      S.append( "@return   The full name of the file or null if it "+
               "cannot be found\n");
      return S.toString();
   }
   
   
   
   
   public static String errorString ="";
  /**
   * Finds a nexus file satisfying all the given properties in the arguments
   * @param runNum       The run number
   * @param instrument   The instrument name
   * @param Facility     The Facility name
   * @param proposal     The proposal identifier
   * @param collection   The collection string
   * @param verbose      True or false
   * @param Dirs         The other directory(s) to look in. Separate multiple
   *                     directories with semicolons(not implemented yet)
   *                     
   * @param extension    The extension for the filenames
   * @param recurselevel  The number of level of subdirectories to search.
   * @return   The full name of the file or null if it cannot be found
   * @error  Check the variable errorString if the result is null.
   */
   public static String findNeXus( String runNum, String instrument, 
                    String Facility, String proposal, String collection,
                    boolean verbose, String Dirs, String extension,
                    int recurselevel){
      errorString ="";
      try{
         PythonInterpreter interp = new PythonInterpreter();
         interp.execfile( Fixup( filename ) );
         interp.set( "arg0", runNum);
         interp.set( "arg1", instrument);
         interp.set( "arg2", Facility);
         interp.set( "arg3", proposal);
         interp.set( "arg4", collection);
         interp.set( "arg5", new Boolean( verbose ) );
         interp.set( "arg6", Dirs);
         interp.set( "arg7", extension);
         interp.set( "arg8", new Integer( recurselevel ));
         PyObject pyResult = interp.eval( methodName + 
               "(arg0,arg1,arg2,arg3,arg4,arg5,arg6,arg7,arg8)" );
         
         Object Res = pyResult.__tojava__( Object.class );
         if( Res == null)
            return null;
         if( Res instanceof String )
            return (String) Res;
         errorString = Res.toString();
         return null;
            
         
         
      }catch( Exception s){
         errorString = s.toString();
         return null;
      }
      
      
   }
   public Object getResult(){
      try{
        PythonInterpreter interp = new PythonInterpreter();
        interp.execfile( Fixup( filename ) );
        String S ="(";
        for( int i=0 ; i < prompts.length ; i++ ){
        
           interp.set( "arg"+i , getParameter( i ).getValue() );
           S += "arg" + i;
           if( i + 1 < prompts.length )
             S += ",";
           else
             S += ")";
        }
        PyObject pyResult = interp.eval( methodName + S );
        return pyResult.__tojava__( Object.class );
        
      }catch( Exception s){
      
        return new gov.anl.ipns.Util.SpecialStrings.ErrorString( s.toString());
     }
   
   }
   
   public String[] getCategoryList(){
     return DataSetTools.operator.Operator.UTILS_SYSTEM;
   }
   private static String Fixup( String S){
     if( S == null)
       return S;
    int i= S.indexOf("{");
    if( i < 0)
      return S;
    int j= S.indexOf("}", i+1);
    if( j < 0)
      return S;
   String R = System.getProperty( S.substring(i+1,j));
   if( R == null)
     return S.substring(0,j+1)+ Fixup(S.substring(j+1));
   return Fixup(S.substring(0,i)+R+S.substring(j+1));
   }
   
 }