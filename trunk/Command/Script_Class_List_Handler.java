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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
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
 * Corrected directory parser to not double load operators and scripts if isaw is installed in $HOME/ISAW. We still should account for multiply defined directories within the GROUP#_HOME listings.
 *
 * Revision 1.18  2001/11/12 21:27:19  dennis
 *  1. Eliminated a developmental test program.  The main program
 *     of this class can now be used to see the directories and the
 *     files that are considered in installing scripts and operators.
 *
 *  2. The user's home directory is now searched first, then the Group
 *     directories(see 3), and finally the ISAW_HOME directory.
 *
 *  3. Added GROUP1_HOME, GROUP2_HOME, etc. as other search paths for
 *     scripts and operators.
 *
 * Revision 1.17  2001/08/15 14:17:58  rmikk
 * This now returns the SAME java operator whenever the
 *    getOperator method is called (no clone is returned).
 *    This will allow the parameters to retain their values more
 *     often.  Scripts, so far, return a NEW operator with
 *    default parameter values each time.
 *
 * Revision 1.16  2001/08/10 20:46:12  rmikk
 * The LoadDebug variable is now public and static so it
 * can be set to true by any application to get the debug
 * outputs.
 * Made the main program a test for the Script and Class
 * loading.  "java Command.Script_Class_List_Hanlder" will
 * now show the files being considered and possible errors.
 *
 * Revision 1.15  2001/08/06 14:09:28  rmikk
 * Fixed a bug that cause filenames to have "//".
 * Added a boolean variable LoadDebug that, when true,
 *    displays directories and files being considered.  Some
 *    Error messages are displayed in these cases.
 *
 * Revision 1.14  2001/07/20 16:35:21  rmikk
 * Fixed the show method to show more
 *
 * Revision 1.13  2001/07/20 15:48:21  chatter
 * Checked Properties directories for tailing '/' ir '\'
 *
 * Revision 1.12  2001/07/20 13:59:47  rmikk
 * Replace \ by / so Scripts can be installed from Unix
 *
 * Revision 1.11  2001/06/27 18:38:33  rmikk
 * Add tests to make sure a directory is only loaded once
 *
 * Revision 1.10  2001/06/26 14:44:27  rmikk
 * Install scripts and java operators are now relative to
 * ISAW_HOME(Operators and Scripts subdirectories),
 *  USER_HOME, and user.home Directories
 *
 * Revision 1.9  2001/06/25 19:59:58  chatter
 * Added Last Parameter to JParametersDialog Constructor
 * in main program
 *
 * Revision 1.8  2001/06/05 16:50:35  rmikk
 * Changed props.dat to IsawProps.dat
 *
 * Revision 1.7  2001/06/05 16:38:08  rmikk
 * Fixed potential error in getNextPath to catch only
 * repeated paths not repeated subpaths
 *
 * Revision 1.6  2001/06/04 20:15:21  rmikk
 * Fixed Documentation
 *
 * Revision 1.5  2001/06/04 14:10:27  rmikk
 * Did not add Scripts that had errors with their construction.
 *
 * Revision 1.4  2001/06/01 21:14:13  rmikk
 * Added Documentation for javadocs etc.
 *
 */
package Command;

import Command.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import DataSetTools.util.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.components.ParametersGUI.*;

/** 
 * Gets and Saves all scripts and java GenericOperators in the
 * Path(s) of the system property Script_Path
 */
public class Script_Class_List_Handler  implements OperatorHandler{
    //Contains ordering for Command Names
    private static final Vector SortOnCommand = new Vector();  
    //Contains ordering for file names
    private static final Vector SortOnFileName = new Vector();
    private static  final Vector opList  = new Vector();
    private String errorMessage= "";
    private static int Command_Compare =257;
    private static int File_Compare = 322;
    private static boolean first = true;
    public static boolean LoadDebug = false;
    private final int MIN_DIR_NAME__LENGTH=3;

    /**
     * The System property user.home,ISAW_HOME, GROUP_HOME,
     * GROUP1_HOME,..  are the paths for the operators that are to be
     * "installed"
     */
    public Script_Class_List_Handler(){
        inittt();
    }
    
    /**
     * Extracts the Next Path in a semicolon delimited list of paths
     * This method can be used to extract next Entry from any
     * delimited list.
     *
     * Repeats will NOT be extracted twice. Empty Paths will be
     * ignored
     *
     * @param  PathList A semicolon delimited list of String entities
     * @param  PrevPath Must be null or one entity in the list
     *         (w.o. semicolons )
     * @return The next entity in the PathList or null if therre is none.
     */
    public  String getNextPath( String PathList , String PrevPath ){
        if(PathList == null ) return null;
        
        int i ;
        int j;
        if( PrevPath != null )
            if( PrevPath.length() <= 0)
                PrevPath = null;
        
        PathList.trim();
        if( PathList.lastIndexOf( ';') != PathList.length()-1)
            PathList = PathList+ ";";
        i = 0;
        if( PrevPath != null ){
            if( PrevPath.length() > 0 ){
                if( PathList.indexOf( PrevPath ) >= 0 ){
                    i = PathList.indexOf( PrevPath );            
                    if( i >= 0 ){
                        while(PathList.indexOf(PrevPath+";",
                                               i+PrevPath.length())>=0){
                            i=PathList.indexOf( PrevPath, i+PrevPath.length() );
                        }        
                    }      
                    if( i < 0 ){
                        return null;
                    }

                    i = i + PrevPath.length() +1;
                    if( i >= PathList.length()) return null;
                }
            }
        }
        
        j = PathList.indexOf(';', i );
        
        if( j < 0 )
            return null;
       
        String Res = PathList.substring( i , j );
        // Prepare for several occurrences of the same path
        // Only the last occurrence of a path comes through
        String X=";";
        if( i == 0)
            X ="";
        while( PathList.indexOf( X+Res+ ";" , j ) > 0 ){
            i = j + 1 ;
            j = PathList.indexOf( ';' , i  );
            if( j < 0 )
                return null;
            Res = PathList.substring( i , j );
            X = ";";
        }
        return Res;
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
                X  =(Operator) opList.elementAt(j);
            
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
    public  Operator getClassInst( String filename ){
        String path;
        if( filename == null ){
            System.out.println("No Name");
            return null;
        }
    
        filename = filename.replace('\\' , '/' );
        String pathlist = System.getProperty("java.class.path");
        pathlist=pathlist.replace('\\','/');
        pathlist=pathlist.replace(java.io.File.pathSeparatorChar,';');
        String ScrPath=System.getProperty("ISAW_HOME");
        if(ScrPath!=null){
            ScrPath=ScrPath.replace('\\','/');
            if(ScrPath.lastIndexOf('/')<ScrPath.length()){
                ScrPath=ScrPath+"/";
            }
            if(pathlist.indexOf(ScrPath+"Isaw.jar;")>=0){
                if(pathlist.indexOf(ScrPath+";")>=0){
                    // do nothing
                }else{
                    pathlist=ScrPath+";"+pathlist;
                }
            }
        }
        int i = filename.lastIndexOf('/' );
        if( i < 0 ){
            if( LoadDebug){
                System.out.println( "No directory divider");
            }
            return null;
        }
        String CPath = filename.substring( 0 , i ).trim();
        String classname = filename.substring( i + 1 , filename.length()-6); 
        
        String CPathFix=CPath;
        
        if( CPath == null ){
            if( LoadDebug ){
                System.out.println("No Name");
            }
            return null;
        }
        if( CPath.length() <= 0 ){
            if( LoadDebug ){
                System.out.println("No Name");
            }
            return null;
        }
        
        for( path = getNextPath( pathlist, null ) ; path != null;
             path = getNextPath( pathlist, path )){
            String Path1=path.trim();
            
            if( Path1 != null ){
                if( Path1.length() > 0 ){
                    if( Path1.lastIndexOf('/') == Path1.length()-1)
                        Path1=Path1.substring(0,Path1.lastIndexOf('/' )).trim();
                    if( Path1 !=null ){
                        if( Path1.length() > 0 ){
                            // do nothing
			}
                    }
                }
            }
            
            if( CPath.indexOf(Path1 ) == 0 ){
                CPathFix = CPath.substring( Path1.length()+1);
                CPathFix =CPathFix.replace('/','.');
                CPath=CPathFix+"."+classname;
                
                try{
                    Class C = Class.forName( CPath );
                    Object XX = C.newInstance();
                    if( XX instanceof GenericOperator){
                        return (Operator)XX;
                    }            
                    if( LoadDebug){
                        System.out.println("NO: Not Generic OP");
                    }
                }catch(Exception s){
                    if( LoadDebug ){
                        System.out.println("NO-"+s);
                    }
                }
                
            }
            
        }//Check each classpath for match
        if( LoadDebug ) System.out.print(" (not in classpath) ");
        return null;
    }

    /**
     * Gets the Command of the index-th operator
     */
    public String getOperatorCommand( int index ){
        Operator X = getOp1(CommandListIndex(index));
        if(X != null)  
            return X.getCommand();
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
     * Gets the number of parameters(arguments) of the index-th
     * operator
     */
    public int getNumParameters( int index ){
        Operator X = getOp1(CommandListIndex(index));
        if( X == null )
            return 0;
        else
            return X.getNum_parameters();
    }

    /**
     * Gets the par_index-th parameter of the index-th operator int
     * the master operator list
     */
    public Object getOperatorParameter( int index, int par_index){
        Operator X = getOp1(CommandListIndex(index));
        if( X == null ) return null;

        int n = X.getNum_parameters();
        if( (par_index<0) || (par_index>=n) ) return null;

        Parameter P = X.getParameter( par_index );
        if( P == null ) return null;

        return P.getValue();
    }

    /**
     * Gets the index-th operator in the master list of operators
     */
    public Operator getOperator( int index ){
        Operator X = getOp1( CommandListIndex(index) );
        if( X == null )
            return null;
        if( X instanceof ScriptOperator ){ // Maybe will get these to retain
                                           // their parameter values too
            String filename = ((ScriptOperator)X).getFileName();
            return new ScriptOperator( filename );
        }else{ //Will try to have these operators retain their parameter values
            /*Class C = X.getClass();
              try{
              return (Operator)(C.newInstance());
              }
              catch(Exception s)
              { return null;}*/
            return X;
        }
    }

    /**
     * Gets the position in the master list of the first operator
     * whose command name is CommName.
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
            }else{
                return i;
            }
        }
        return i;
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

    private void toggleDebug(){
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
    private void inittt(){  
        if( !first) return;
        for( int i = 0 ; i < GenericOperatorList.getNum_operators(); i++){
            Operator op = GenericOperatorList.getOperator( i );
            if( op instanceof GenericOperator) add( op );
        }
        first = false;  
        toggleDebug(); 
        
        Vector includeVec = new Vector();
        // add $HOME/ISAW to the path
        String ScrPaths2 = System.getProperty( "user.home" );
        //String ScrPaths2 = "/IPNShome/hammonds";
        if( ScrPaths2 != null ){
            //System.out.print("**********\n"+"USER_HOME:");
            includeVec=addDir(ScrPaths2+File.separator+"ISAW",includeVec);
        }
        int g = 0;
        
        // add the different GROUP#_HOMEs to the path
        String ScrPaths1 = System.getProperty( "GROUP_HOME" );
        while(ScrPaths1 != null){
            //System.out.print("**********\n"+"GROUP"+g+"_HOME:");
            includeVec=addDir(ScrPaths1,includeVec);
            g++;
            String suff=""+g;
            suff=suff.trim();
            ScrPaths1 = System.getProperty( "GROUP"+suff+"_HOME" );
        }
        
        
        // add where ISAW lives to the path
        String ScrPaths = System.getProperty( "ISAW_HOME" );
        if( ScrPaths != null ){
            //System.out.print("**********\n"+"ISAW_HOME:");
            includeVec=addDir(ScrPaths,includeVec);
        }
        
        /* System.out.println("********************");
           for( int i=0 ; i<includeVec.size() ; i++ ){
           System.out.println("* "+i+":"+includeVec.elementAt(i));
           }
           System.out.println("********************"); */
        
        // remove redundant listings from the path
        for( int i=0 ; i<includeVec.size() ; i++ ){
            for( int j=i ; j<includeVec.size() ; j++ ){
                if( i==j )continue; // do the next j loop
                // shorten some other lines in the inner for loop
                String ith=(String)includeVec.elementAt(i);
                String jth=(String)includeVec.elementAt(j);
                // check that j does not start with i
                if( jth.indexOf(ith)==0 ){
                    //System.out.println("REMOVING("+i+","+j+")"+jth);
                    includeVec.remove(j);
                    j--;
                }
            }
        }
        
        /* System.out.println("********************");
           for( int i=0 ; i<includeVec.size() ; i++ ){
           System.out.println("* "+i+":"+includeVec.elementAt(i));
           }
           System.out.println("********************"); */
        
        for( int i=0 ; i<includeVec.size() ; i++ ){
            processPaths((String)includeVec.elementAt(i));
        }
        
        
        toggleDebug(); 
    }  // end of inittt()
    
    /**
     * Method to simplify code since all directories are parsed in the
     * same manner. This adds the directories "dir/Operators" and
     * "dir/Scripts" if they exist. The directory specified by the
     * String can be a list seperator by File.pathSeparator.
     */
    private Vector addDir(String dir, Vector include){
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
                if(existDir(dir)){
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

    private String standardizeDir( String dir ){
        // remove whitespace from the name
        dir.trim();
        // switch \ to /
        dir=dir.replace('\\','/');
        // put a / at the end if it is not there already
        if( !(dir.charAt(dir.length()-1) == '/') ){
            dir=dir+'/';
        }
        return dir;
    }

    private boolean existDir( String dir ){
        // the directory must be at least a certain length long
        if(dir.length()<=MIN_DIR_NAME__LENGTH)return false;
        // check if the directory exists
        File Dir = new File(dir);
        return Dir.isDirectory();
    }

    private void processPaths( String ScrPaths){
        ScrPaths.trim();
        if( LoadDebug) System.out.println("----PATH="+ScrPaths);

        ScrPaths=ScrPaths.replace(java.io.File.pathSeparatorChar,';'); 
        if( ScrPaths.lastIndexOf(';') != ';')
            ScrPaths = ScrPaths+";";
        
        
        for( String Path = getNextPath( ScrPaths , null );
                          Path != null; Path = getNextPath( ScrPaths , Path ) ){
   
            File Dir = new File( Path) ;
            if( Dir.isDirectory() )ProcessDirectory( Dir ,opList);
        }
        
        return;
    }

    private  void ProcessDirectory( File Dir, Vector opList ){
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

    private  void ProcessFile( File file , Vector opList){
        add( file.toString() , opList);
    }

    private  boolean isFileExtension( File F){
        String nm = F.getName();
        if(nm == null )
            return false;
        int i = nm.lastIndexOf( '.');
        if( i < 0 )
            return false;
        String ext = nm.substring( i + 1).toUpperCase();

        if( ext.equals("ISS") || ext.equals("CLASS") ){
            return true;
        }else{
            return false;
        }
    }

    private  void add( String filename , Vector opList){
        int i;
        
        if(filename == null )
            return;
        if( LoadDebug)
            System.out.print( "Processing "+filename+":");

        i = filename.lastIndexOf('.');
        if( i< 0 )
            return;

        String Extension = filename.substring( i + 1 );
        if( Extension.equalsIgnoreCase("iss")){
            ScriptOperator X = new ScriptOperator( filename );
            if(X.getErrorMessage().length()<=0){
                add( X );
                if( LoadDebug )
                    System.out.println( "OK" );
            }else if( LoadDebug ){
                System.out.println( "NO "+X.getErrorMessage() );
            }
        }else{
            Operator X = getClassInst( filename );
            if( X != null ){
                if(LoadDebug)
                    System.out.println( "OK" );
                add( X );
            }else{
                if(LoadDebug)
                    System.out.println( "NO ");
            }
        }
    }

    private  void add( Operator op){
        opList.addElement( op );
        
        insert( opList , SortOnCommand );
        insert(opList , SortOnFileName );
    }

    /**
     * insert before
     */
    private  void insert( int n_toInsert, int pos_insert , Vector rankList  ){
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

    private  void insert( Vector opList, Vector rankList ){
        if( opList.size()<= 0)
            return;
        
        
        Operator op = (Operator)( opList.elementAt( opList.size() - 1));
        int n = opList.size() -1;
        int indx = -1;
        if( rankList.equals( SortOnCommand ) )
            indx = find( op.getCommand(), Command_Compare );
        else if(op instanceof ScriptOperator )
            indx = find( ((ScriptOperator)op).getFileName(), File_Compare );
        else 
            return;
        
        insert( n, indx, rankList);
    }

    /**
     * binary search for inserting only
     * @return the first index in the appropriate rank list that is
     * more than or = to op
     */
    private  int find( String textt, int mode ){
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
                Operator opp = (Operator)(opList.elementAt(pos));
                less = false;
  
                if( mode == Command_Compare){
                    if( ScompareLess(opp.getCommand() , textt))
                        less = true;
                    else if( opp.getCommand().equals( textt ))  
                        return mid;  
                }else{
                    if( !(V.equals( SortOnFileName )  ) )
                        return -12;
                    String  sopp;
                    
                    sopp = ((ScriptOperator)opp).getFileName();
                    if( ScompareLess(sopp , textt) )
                        less = true;
                    else if( sopp.equals( textt ) )
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

    private Operator getOp1( int index ){
        if( (index<0) || (index>=opList.size()) ) return null;

        Operator X = (Operator)(opList.elementAt( index));
        return X;
   }

    private int CommandListIndex(int index){
        if( (index<0) || (index>=SortOnCommand.size()) ) return -1;

        Integer X = (Integer)SortOnCommand.elementAt(index);
        int j = X.intValue();
        return j;
    }

    private String getString(){
        char c=0;
        String S ="";
        try{
            while( c<32)
                c = (char)System.in.read();
            
            while( c>=32){
                S = S + c;
                c = (char)System.in.read();
            }
        }catch(Exception s){
            // let it drop on the floor
        }
        //System.out.println("getString="+S);
        return S;
    }

    private  boolean ScompareLess( String s1, String s2){
        if( s1 == null ) return ( s2!=null );

        for( int i=0 ; i< s1.length(); i++){
            if( (i >= s2.length()) || (s1.charAt(i) >s2.charAt( i )) )
                return false;
            else if( s1.charAt(i) < s2.charAt( i ))
                return true;
        }
        
        return ( s1.length()==s2.length() );
    }

    /**
     * Test program for this module.  No arguments are used 
     */
    public static void main( String args[] ){
        PropertiesLoader PL= new PropertiesLoader("IsawProps.dat"  );
        
        Script_Class_List_Handler.LoadDebug = true;
        Script_Class_List_Handler BB = new Script_Class_List_Handler();
        System.out.println("-------------------------------------------"
                           +"-------------------------------");
        System.out.println("#operators ="+BB.getNum_operators());
        BB.show(257);
        
        System.exit( 0 );
    }

}
