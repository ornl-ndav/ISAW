/*
 * File:  ScriptOperator.java 
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
 * Revision 1.16  2002/02/22 20:33:41  pfpeterson
 * Operator Reorganization.
 *
 * Revision 1.15  2001/11/12 21:27:55  dennis
 *   1. Supports  GROUP1_HOME, GROUP2_HOME, etc. These will be separated
 *      under IsawGUI's Macros menu into  Group Scripts, Group1 Scripts,
 *      Group2 Scripts, etc.
 *
 * Revision 1.14  2001/11/09 18:23:20  dennis
 *   1. Eliminated the System.exit(0) when the JParametersDialog is
 *      needed for parameters. Used a Window Listener to exit when
 *      the Dialog box is closed.
 *   2. Eliminated an error that occurred when a file had no directory
 *      in its name.  The directory was assumed to be the user.dir,
 *      the directory where the program was launched.
 *
 * Revision 1.13  2001/08/02 15:52:01  chatter
 * Changed message in the log entry for running this as a batch file
 *
 * Revision 1.12  2001/07/31 22:16:26  rmikk
 * Used Dennis' DataSet.util Shared Data to load the Isaw
 * Props.dat file
 *
 * Revision 1.11  2001/07/20 21:23:31  rmikk
 * Fixed Main program so a script and or class file(?) can
 * be executed as follows:
 *     "java Command.ScriptOperator  filename"
 * The dialog box will pop up if there are any script parameters
 *
 * Revision 1.10  2001/06/27 18:37:53  rmikk
 * Change USER_HOME to GROUP_HOME.
 * Changed Category List to contain Isaw Scripts if the leading
 *     directory part of the file matches ISAW_HOME
 * Changed Category List to contain Group Scripts and user
 *    Scripts if the leading part of the filename matches
 *    GROUP_HOME and user.home resp.
 *
 * Revision 1.9  2001/06/26 14:41:18  rmikk
 * -Used ISAW_HOME ,USER_HOME, and user.home
 * environmental variables to trim filename path.
 * -If no title given in script, the title will be the command name
 *
 * Revision 1.8  2001/06/05 16:50:35  rmikk
 * Changed props.dat to IsawProps.dat
 *
 * Revision 1.7  2001/06/04 20:14:20  rmikk
 * Updated Documentation
 *
 * Revision 1.6  2001/06/04 14:10:38  rmikk
 * *** empty log message ***
 *
 * Revision 1.5  2001/06/01 21:14:13  rmikk
 * Added Documentation for javadocs etc.
 *

 * 5-25-2001   Created 
 */
package Command;


import javax.swing.text.*;
//import Command.*;
import java.lang.*;
import java.awt.event.*;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.components.ParametersGUI.*;
import DataSetTools.util.*;
import java.beans.*;
import java.io.*;

/**  Adds features to a ScriptProcessor to be more of an "Operator"
 */
public class ScriptOperator extends GenericOperator
                            implements IObservable,
                                       Customizer  //for property change events
                                        
 {
   private String filename;
   private String command;
   
   private String categList[];
   private ScriptProcessor SP;
   private String errorMessage ="";
   public static String ER_FILE_ERROR             = "File error ";
   

  /**
 * Creates an operator wrapper around a script
 * The command Name and category list are derived from the filename
 * The title is determined by the $title = from the script
 *@param  filename  The file with a script
 */
   public ScriptOperator(  String filename )
     {  super("UNKNOWN");
        
        this.filename = filename;
        command = null;
        categList = null;
        SP = null;
        IsawGUI.Util ut = new IsawGUI.Util();
        Document D =ut.openDoc( filename ) ;
        errorMessage ="";
        if((D == null) ||( filename == null ))
          { errorMessage = ER_FILE_ERROR;
            return;
          }
        
        SP = new ScriptProcessor( D );
        if( SP.getErrorMessage().length( ) > 0 )
           {  errorMessage = SP.getErrorMessage();
              return;
            }
       
        int i, j;
        j = filename.lastIndexOf( '.');
        if( j < 0 ) 
          j = filename.length();
        
        String F = filename.replace('\\','/');
        i = F.lastIndexOf( '/', j);
        if( i < 0 )
           i = -1;
        command = F.substring( i + 1, j );
       
        if( i >= 0) F = F.substring( 0, i );
        else F = System.getProperty("user.dir");

        if( F.charAt(F.length()-1)!='/')
          F = F + "/";
    //adjust F;
       String MainCat=null;
    
   
       String X;   
       String F2 =F;
       X = System.getProperty( "ISAW_HOME");
            
          if(X!=null)
             {X = X.replace( '\\', '/');
              X.replace(java.io.File.pathSeparatorChar, ';');
              F2 = adjust ( F , X );
             }
    
       if( !(F2.equals(F)))
          {MainCat = "Isaw Scripts";
           F = F2;
          }
     
       String F3=F;
       int g=0;
       String grp ="";
       
       X = System.getProperty( "GROUP_HOME");
       while( (MainCat== null)&&(X !=null)) 
         {if( MainCat == null)  
           {  
            
              if( X!=null)
                 {X = X.replace( '\\', '/');
                  X.replace(java.io.File.pathSeparatorChar, ';');
                  F3 = adjust ( F , X );
                 }
           } 
         if( !(F3.equals(F)))
            { MainCat = "Group"+grp+" Scripts";
              F = F3;
            }
         else
            { g++;
              grp=""+g;
              grp=grp.trim();
              X = System.getProperty( "GROUP"+grp+"_HOME");
             }
        
        }
       if( MainCat == null)
         {  X = System.getProperty( "user.home");
            if( X != null)   
              {X = X.replace( '\\', '/');
               X.replace(java.io.File.pathSeparatorChar, ';');
               F3 = adjust ( F , X );
              }
         } 
      if( !(F3.equals(F)))
          {MainCat = "User Scripts";
            F = F3;
          }
      if( MainCat == null)
         {  X = System.getProperty( "java.class.path"); 
            if( X != null)           
              {X = X.replace( '\\', '/');
               X.replace(java.io.File.pathSeparatorChar, ';');
               F3 = adjust ( F , X );
               F = F3;
              }
         } 
	
       
        if(MainCat == null)
          { i =F.indexOf(':');
            if( i > 0 )
            F = F.substring( i + 1 );
            if( F.charAt( 0 ) == '/' )
                F = F.substring ( 1);
            }
        
        F.trim();
       
        if(F.length() >0)
          if(F.charAt(0) == '/')
             F =F.substring(1);
        if(F.length() > 0 )
          if( F.charAt(F.length()-1) =='/')
            F = F.substring( 0, F.length()-1);
        int c = 1;
                
        if( F.length()>1)
         for( i = F.indexOf( '/' ); i >= 0;  i = F.indexOf( '/' , i + 1 ) )
           c++;
       
        if( F.length()>1)
          categList = new String [ c + 1];
        else
          categList = new String [ c ];

        categList[ 0 ] = DataSetTools.operator.Operator.OPERATOR;
        j = 1 ;
    
        int i1 = 0;
        if( F.length()>1)
        for( i = F.indexOf( '/' ); i >= 0;  i = F.indexOf( '/' , i + 1 ) )
          {if((i1==0)&&(MainCat != null))
              categList[j]=MainCat;
            else
               categList[j] = F.substring(i1, i );
           j++;
           i1 = i+1;
          }
         

        if( F.length()>1)
          if((i1==0)&&(MainCat!=null))
              categList[ c] = MainCat;
          else
              categList[ c ] = F.substring( i1 ) ;               
        

      }

/**
*  Shows this operator. For debugging
*/
 public void show()
    {System.out.println( "Command ="+command );
     if(categList == null )
         System.out.println( "Cat list is null" );
     else
     {System.out.println("Cat leng="+categList.length);
     for(int i = 0; i < categList.length; i++ )
         {System.out.print( categList[i]+",");
         }
      System.out.println("");
       }
      }

 private String adjust( String F1, String X ) 
       {String F = F1;
         if( F == null ) 
           return null;
       
         F = F.trim();
         if( F.length()<1)
           return F;
         if( F.charAt(F.length()-1) !='/')
            F = F+'/';
          int i = 0;
        X = X.replace(java.io.File.pathSeparatorChar,';');
       // System.out.println("X ="+X);
        int j = X.indexOf( ';');
        boolean done = X.length()==0;
        if( j < 0) 
           j = X.length();
        
        while( !done)
          {//System.out.println(F+"-"+X.substring(i,j));
            if (F.toUpperCase().indexOf( X.substring(i,j).toUpperCase() ) >=0 )
             { done = true;
               F = F.substring( j - i ); 
               //System.out.println("adjusted");
               return F;
              }
           else if( j < X.length() )
             { i= j  + 1;
               j = X.indexOf( ';' , i );
               if( j < 0) 
                   j = X.length();
             }
           else 
              done = true;
          }
         return F;
        }

/** gets The title
* NOTE:  A line "Title= prompt or title" is needed for special titles
*/
public String getTitle()
  { if( SP != null )
       {String S =SP.getTitle();
        if( S.equals("UNKNOWN"))
           S = getCommand();
        return S;
       }
    else 
       return "UNKNOWN";
   }

/**
* @return category list
* NOTE: It is calculated from the filename
*/
public String[] getCategoryList()
  {return categList;
  }

/**
*  @return  the last category entry
*NOTE: This is calculated from the filename
*/
public String getCategory()
  {if(categList == null )
      return "UNKNOWN";
   if( categList.length <= 0)
      return "UNKNOWN";
   return categList[categList.length-1];
  }


public String getErrorMessage()
  { return errorMessage;
  }

/** Sets the default parameters for this operator
*
* The parameters determine the data types of the arguments.  This
* is an important part of a function
*/
public void setDefaultParameters()
  { if(SP != null)
       SP.setDefaultParameters();

     }

/** Returns the Command that can be used by the ScriptProcessor to execute this script
*
*NOTE: This is determined by the filename
*/
public String getCommand()
  {if(command == null)
      return "unknown";
    return command;
  }

public String getFileName()
  {return filename;
   }


public int getErrorCharPos()
  { return SP.getErrorCharPos();
   }


public int getErrorLine()
   {return SP.getErrorLine();
   }

/** Gives the number of arguments to this "Script" function
*/
public int getNum_parameters()
  {return SP.getNum_parameters();
  }


public boolean setParameter(Parameter parameter, int index)
  { return SP.setParameter( parameter,index);
  }

public Parameter getParameter( int index)
  { return SP.getParameter( index );
  }


public void addParameter( Parameter P)
  { return;
  }

public void CopyParametersFrom( Operator op)
  { SP.CopyParametersFrom( op );
  }


public void addPropertyChangeListener( PropertyChangeListener pl )
  {SP.addPropertyChangeListener( pl );
  }


public void removePropertyChangeListener(PropertyChangeListener listener)
  { //SP.removePropertyChangeListener( listener);
   }

public void setObject(Object bean)
  {
   }


public void addIObserver( IObserver iobs )
 {SP.addIObserver( iobs );
 }


public void deleteIObserver( IObserver iobs )
    {SP.deleteIObserver( iobs ) ; 
       
    }

   /**
     *deletes all the Iobserver 
    
     */

  public void deleteIObservers()
    {SP.deleteIObservers() ;   
    }

/** Sets the document to log information
*
*@param  doc the document that gets the log information
*NOTE: This document in the future will allow reexecuting a session
*/
public void setLogDoc( Document doc )
  {SP.setLogDoc(doc );
   }
 
/** Executes the script and returns the result
*/
public Object getResult()
  { if( SP != null)
      { Object Res = SP.getResult();
        errorMessage = SP.getErrorMessage();
        return Res;
      }
     else
         return null;
   }

/** Allows running of Scripts without Isaw and/or the CommandPane
*/
public static void main( String args [] )
     {   DataSetTools.util.SharedData sd = new DataSetTools.util.SharedData();
     
      /*java.util.Properties isawProp;
   isawProp = new java.util.Properties(System.getProperties());
   String path = System.getProperty("user.home")+"\\";
       path = StringUtil.fixSeparator(path);
       try {
	    FileInputStream input = new FileInputStream(path + "IsawProps.dat" );
          isawProp.load( input );
	  
          System.setProperties(isawProp);  
    //    System.getProperties().list(System.out);
          input.close();
       }
       catch (IOException ex) {
          System.out.println("Properties file could not be loaded due to error :" +ex);
       }

*/

  if( args == null)
    System.exit( 0 );
  if( args.length < 1)
     System.exit( 0 );
  ScriptOperator SO;
  SO = new ScriptOperator( args[ 0 ] );
  if( SO.getErrorMessage().length() > 0)
   { System.out.println("Error ="+args[0]+"--"+SO.getErrorMessage());
     System.exit( 0 );
   }
  boolean dialogbox=false;
  if( SO.getNum_parameters() > 0)
   {
    JParametersDialog JP = new JParametersDialog( SO , null, null, null  );
    myWindowListener  ml = new myWindowListener();
    JP.addWindowListener( ml );
    dialogbox=true;
   }
  else
   {Object XX = SO.getResult();
    System.out.print("Result =" +XX );        
   }

 if( SO != null)
     if( SO.getErrorMessage() != null )
  if( SO.getErrorMessage().length() > 0)
    System.out.println("An Error occurred "+SO.getErrorMessage());
  if(!dialogbox)System.exit( 0 );
   }
 }
class myWindowListener  extends WindowAdapter
  {
     public void windowClosed(WindowEvent e)
      {System.exit(0);
       }
   }

