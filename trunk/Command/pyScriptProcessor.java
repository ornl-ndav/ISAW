/*
 * File: pyScriptProcessor.java 

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
 * Revision 1.1  2003/01/02 20:46:43  rmikk
 * Initial Checkin for the interface to Jython's Scripting language
 *
 */

package Command;
import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import javax.swing.text.*;
import DataSetTools.util.*;
import java.util.*;
import org.python.util.*;
import java.beans.*;
import java.io.*;

/** This class interfaces the Scripting language to Jythons Interpreter class
*   The variable IOBS ( the list of observers ) and all the Isaw Data sets are
*   available to the interpreter.
*/
public class pyScriptProcessor extends ScriptProcessorOperator implements IObserver
  {Document doc;
   Vector Dsets;
   Document logdoc;
   IObserverList obss;
   PythonInterpreter Pinterpret;
   PropertyChangeSupport  PS;
   String errormessage;
   ByteArrayOutputStream eos;
   
  /** Constructor.  doc is the document containing the script
  */
  public pyScriptProcessor(Document doc)
    {this.doc = doc;
     Dsets= new Vector();
     logdoc = null;
     obss= new IObserverList();
     Pinterpret = new PythonInterpreter();
     Pinterpret.set("IOBS",obss);
     eos=new ByteArrayOutputStream();
     Pinterpret.setErr(eos);
     Pinterpret.setOut( new DisplayOStream ());
     PS = new PropertyChangeSupport( this);
     errormessage = null;
    }



    
  /** Sets the whole IObserverList.  
  *   NOTE: Used when alternating between different languages
  */
  public void setIObserverList( IObserverList IOlist)
    {
      obss = IOlist;
     } 
   


  /** Sets the whole list of property change listeners.  
  *   NOTE: Used when alternating between different languages
  */
  public void setPropertyChangeList( PropertyChangeSupport PcSupp)
    {
       PS = PcSupp;
     }


  
  /**  Executes one line of the document Doc
  */
  public void execute1(javax.swing.text.Document Doc, int line)
    {String S =ScriptProcessor.getLine(Doc,line);
     errormessage=null;
     if( S != null)
      {//eos= new ErrorOStream();
        eos.reset();
        try{
        Pinterpret.exec(S);
         }
        catch(Exception s)
          {errormessage = s.toString();
           DataSetTools.util.SharedData.addmsg( errormessage );
          }
        //try{eos.flush();eos.close();  }catch(IOException s){}; 
      }
 
    }

     
    /**
    *    Sets the document that contains the Python scripts
    */
    public  void setDocument( Document Doc)
     {doc=Doc;
     }
      


    /**
    *   Executes the script, returning the result.  Note that only the main
    *   program executes.  Methods are loaded into the system and can be used
    *   later in the immediate pane
    */
    public Object getResult()
     {
      if( doc ==null) 
         return new ErrorString( "No code to translate");
      try{errormessage=null;
      
       
       reset();
       Pinterpret.exec( doc.getText(0, doc.getLength()));
       if( eos.size() > 0)
          {
           errormessage = "Error:"+eos.toString();
           return new ErrorString( "Error:"+eos.toString());
           }
      
       return Pinterpret.get("Result",Object.class);
        }
      catch(org.python.core.PySyntaxError s){
                errormessage= "ERROR:"+s.toString();
              
                    return new ErrorString( errormessage);}
      catch(Exception s){
           errormessage= "ERROR:"+s.toString();
          return new ErrorString( "ERROR:"+s.toString());
      }
     }
     


   /** 
   *    Resets the PythonInterpreter.  The DataSets are added and the IOBS varible is also
   *    readded
   */
   public void  reset()
     {Pinterpret = new PythonInterpreter();
      for( int i=0; i<Dsets.size();i++)
        {DataSet DS =(DataSet)(Dsets.elementAt(i));
         Pinterpret.set("ISAWDS"+DS.getTag(),DS);
        }
      Pinterpret.set( "IOBS", obss);
      eos = new ByteArrayOutputStream();
      Pinterpret.setErr(eos);
      Pinterpret.setOut( new DisplayOStream ());
      errormessage = null;

     }
     

 
    public  void resetError()
      {  errormessage=null;
      }
      


   public int getErrorCharPos()
      {
       
       if(errormessage == null)
          return -1;
       if( errormessage.length()<1) 
          return -1;
       return 00;}
     
 

   public  int getErrorLine() 
     {
      if( errormessage == null)return -1;
      return 00;}
      

   public  String getErrorMessage()
     {

      //DataSetTools.util.SharedData.addmsg("getErrormessage="+errormessage);
       return errormessage;} 
      

   
   public String getVersion()
     {
      return "V1-PYth v"+org.python.core.PySystemState.version;
     }

   public  void setLogDoc(javax.swing.text.Document doc)
     {
      logdoc = doc;
     } 
       
   public  void  addDataSet(DataSet dss) 
     {
      dss.addIObserver( this);
      Dsets.addElement( dss);
      long tag = dss.getTag();
      Pinterpret.set( "ISAWDS"+tag, dss);
     }      
 
 
   public  void  addIObserver(IObserver iobs)
   {
    obss.addIObserver(iobs);
    }


   public   void  deleteIObserver(IObserver iobs) 
    {
     obss.deleteIObserver( iobs);
    }
   

   public  void deleteIObservers()
     {
      obss.deleteIObservers();
     }
   


  //Send all displays to console for a while  
  public  void addPropertyChangeListener(java.beans.PropertyChangeListener P)
    { 
     PS.addPropertyChangeListener( P );
    }

   
  public DataSet[] getDataSets()
    {int n= Dsets.size();
     DataSet DS[];
     DS = new DataSet[n];
     for(int i = 0; i < n ; i++)
       DS[i] = (DataSet)(Dsets.elementAt(i));
     return DS;
    }



  //if data set is deleted delete it here.
  //NOTE: It is difficult to get it out of the Jython System

   public void update(java.lang.Object observed_obj,
                      java.lang.Object reason)
    {if( observed_obj instanceof DataSet)
       if( reason instanceof String)
         if( IObserver.DESTROY.equals(reason))
           { ((DataSet) observed_obj).deleteIObserver( this );
              long tag = ((DataSet) observed_obj).getTag();
              while( Dsets.removeElement( observed_obj)){}
              
           }
     }



   /**
   *   set's Default parameters.  There are None here. The main program
   *   will have to bring up the JParametersDialog box itself( somehow)
   */
   public void setDefaultParameters()
     {
      parameters= new Vector();
     }
 


   /** Error Stream
   */
   class ErrorOStream extends OutputStream
    {
     public ErrorOStream()
      {super();
       errormessage = null;
      }


     public void write(int b) throws IOException
       { 
         errormessage +=(char)b;           
       }


     public void write(byte[] b)
           throws IOException
       {write(b, 0, b.length);
      
       }


     public void write(byte[] b,
                  int off,
                  int len)
           throws IOException
       {     
        if( off < 0) 
          off = 0;
        if( off+len >b.length) 
         len = b.length-off;
        for(int i=off;i<off+len;i++)
          { int c = (int)((char)b[i]);
            write(c);
          }
        
       
     }

   }//ErrorOStream



  /**
  *   Class to handle the Display information
  */
  class DisplayOStream extends OutputStream
  {String ouu;
   public DisplayOStream()
    {super();
     ouu= "";
    }

   /**
   *    "write"s one byte to a buffer
   */
   public void write(int b) throws IOException
   { 
     ouu +=(char)b; 
   }

   /**
   *   "write"s an array of bytes and dumps it out
   */
   public void write(byte[] b)
           throws IOException
   {
     write(b, 0, b.length);
   }


   public void write(byte[] b,
                  int off,
                  int len)
           throws IOException
     {
      if( off < 0) 
          off = 0;
      if( off+len >b.length) 
          len = b.length-off;
      for(int i=off;i<off+len;i++)
        { int c = (int)((char)b[i]);
          write(c);
        }
      ouu=ouu.trim();
      DataSetTools.util.SharedData.addmsg( ouu);
    
      //PS.firePropertyChange("Display", null, ouu);
      ouu="";
     }

   }//ErrorOStream




   }//ScriptProcessorOperator
