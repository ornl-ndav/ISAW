/*
 * File:  PyScriptOperator.java
 *
 * Copyright (C) 2003, Chris M. Bouzek
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
 *           Chris M. Bouzek <coldfusion78@yahoo.com>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882.
 *
 * $Log$
 * Revision 1.13  2006/07/10 21:28:19  dennis
 * Removed unused imports, after refactoring the PG concept.
 *
 * Revision 1.12  2006/07/10 16:25:52  dennis
 * Change to new Parameter GUIs in gov.anl.ipns.Parameters
 *
 * Revision 1.11  2005/10/09 19:15:44  rmikk
 * Made the Python interpreter transient
 *
 * Revision 1.10  2005/01/07 17:44:35  rmikk
 * Added a method to determine if this operator is to be added to the
 *   operator list by Command.Script_Class_List_Handler
 *
 * Revision 1.9  2005/01/02 17:58:04  rmikk
 * Added a method to getFileName
 * Changed copy constructor to just use just the filename of the old 
 *    PyScriptOperator
 *
 * Revision 1.8  2004/05/12 02:00:30  bouzekc
 * Added code to reformat and display Python error messages.  Some errors
 * are still not caught-some class using this one is reformatting them on its
 * own.
 *
 * Revision 1.7  2004/03/15 03:28:22  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.6  2004/02/07 19:48:47  rmikk
 * Catches PyExceptions instead of the subclass PySyntaxError
 * Attempts to return a meaningful line number
 *
 * Revision 1.5  2004/01/08 14:48:54  bouzekc
 * Made call to generateError() static, as it is a static method.
 *
 * Revision 1.4  2003/11/20 01:45:56  bouzekc
 * Made all methods final.
 *
 * Revision 1.3  2003/10/20 22:08:58  bouzekc
 * Fixed javadoc errors.
 *
 * Revision 1.2  2003/10/10 02:02:05  bouzekc
 * Merged with pyScriptProcessor.
 *
 * Revision 1.1  2003/08/11 18:01:58  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.operator;

import Command.*;

import DataSetTools.dataset.*;

import DataSetTools.operator.Generic.GenericOperator;


import DataSetTools.util.*;

import gov.anl.ipns.Parameters.IParameter;
import gov.anl.ipns.Util.Messaging.*;
import gov.anl.ipns.Util.SpecialStrings.*;
import gov.anl.ipns.Util.Sys.*;

import org.python.core.*;

import org.python.util.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import javax.swing.text.Document;


/**
 * Operator for use with Jython/Python scripts.  This class can work with a
 * Jython Script that has been specifically designed as an Operator (See
 * find_multiple_peaks2.py in the ISAW scripts directory for an example of
 * this) or it can work with "generic" Jython code.  Although there is a
 * constructor and a few methods that can take javax.swing.text Documents,
 * this class does NOT work internally with Documents.  Note also that
 * although this will interpret all valid Jython code, if there is no class
 * definition given, the only "Operator" method that will return meaningful
 * results is getResult(), as the other methods rely on an existing class
 * definition.
 */
public class PyScriptOperator extends GenericOperator
  implements IScriptProcessor {
  //~ Instance fields ----------------------------------------------------------

  transient private PythonInterpreter     interp;
  private PyScript              script;
  private String                scriptFile;
  private IObserverList         obss;
  private Vector                Dsets;
  private PropertyChangeSupport PS;
  private String                errormessage;
  transient private ByteArrayOutputStream eos;
  private boolean               IAmOperator = false;
  private boolean               scriptLoaded = false;
  private boolean               ParamsSet = false;
  private int                   errLineNum  = -1;
  private int                   errPos  = -1;
  private static boolean        PySystemInitted = false;
  private static Object        syncObj = new Object();  

  //~ Constructors -------------------------------------------------------------

  /**
   * Constructor for creating a PyScriptOperator out of a Document.
   *
   * @param inDoc The document containing the script
   */
  public PyScriptOperator( Document inDoc ) {
    super( "UNKNOWN" );
    scriptFile   = "UNKNOWN";
    script       = new PyScript( inDoc );
    initOperator(  );
  }

  /**
   * Copy constructor for use in clone and where clone is unnecessary.  This
   * makes a fairly deep copy of the parameters.
   *
   * @param oldPSO The PyScriptOperator to copy.
   */
  public PyScriptOperator( PyScriptOperator oldPSO ) {
    //set the filename and init the Operator
    this( oldPSO.getFileName() );
    Object tempVal = null;

    //get the old parameters and set the new ones the same
    int numParams = oldPSO.getNum_parameters(  );

    for( int i = 0; i < numParams; i++ ) {
      tempVal = oldPSO.getParameter( i ).getValue(  );
      this.getParameter( i ).setValue( tempVal );
    }
  }

  /**
   * Creates a new PyScriptOperator object.
   *
   * @param filename The filename of the Jython Script to load.
   *
   * @throws InstantiationError If the script is invalid.
   */
  public PyScriptOperator( String filename ) throws InstantiationError {
    super( filename );
    scriptFile   = filename;
    script       = new PyScript( scriptFile );
    initOperator(  );
  }

  //~ Methods ------------------------------------------------------------------

  /**
   * Accessor method for the categoryList. Uses the internal interpreter to
   * call the superclass method.
   *
   * @return String array of the category list. If this script does not truly
   *         define an Operator, this returns super.getCategoryList().
   */
  public final String[] getCategoryList(  ) {
    if( !IAmOperator ) {
      return super.getCategoryList(  );
    }
    
    PyObject pyCatList = null;
    String[] catList = null;
    synchronized(  syncObj){
    pyCatList = interp.eval( "innerClass.getCategoryList(  )" );

    //convert the PyObject to a useful Java Object
    catList = ( String[] )pyCatList.__tojava__( String[].class );
    }
    return catList;
    
  }

  /**
   * Accessor method for obtaining the command name of this PyScriptOperator.
   * Unless otherwise specified in the Python/Jython script, this is the name
   * of the file without the .py extension.
   *
   * @return Command for using this Operator in Scripts. If this script does
   *         not truly define an Operator, this returns super.getCommand(  ).
   */
  public final String getCommand(  ) {
    if( !IAmOperator ) {
      return super.getCommand(  );
    }
    synchronized(  syncObj){
    PyObject pyDocs = interp.eval( "innerClass.getCommand(  )" );

    //convert the PyObject to a useful Java Object
    String sDocs = ( String )pyDocs.__tojava__( String.class );

    return sDocs;
    }
  }

  /**
   * Returns the filename for this operator
   * @return  the filename for this script
   */
  public String getFileName(){
     return scriptFile;
  }
  
  
  
  /**
   *   Returns true if this script is written in operator form
   *   otherwise it returns false
   *   @return  True if this operator can be installed.
   */
  public boolean isInstallableOperator(){
    return IAmOperator;
  }
   
  /**
   * Accessor method to get the internal DataSet array.
   *
   * @return A new DataSet array containing references to the DataSets in the
   *         internal  DataSet array.
   */
  public final DataSet[] getDataSets(  ) {
    int       numDsets = Dsets.size(  );
    DataSet[] DS;
    DS = new DataSet[numDsets];

    for( int i = 0; i < numDsets; i++ ) {
      DS[i] = ( DataSet )( Dsets.elementAt( i ) );
    }

    return DS;
  }

  /**
   * Sets default parameters for this PyScriptOperator, using the
   * setDefaultParameters defined in the Python script.  Does nothing if the
   * internal interpreter is null.  This method also calls reset(  ). If this
   * script does not truly define an Operator, this does nothing.
   */
  public final void setDefaultParameters(  ) {
    if( !IAmOperator ) {
      //not an Operator, so not much to set here
      return;
    }

    if( ( interp == null ) || ( script == null ) ) {
      //i.e. The constructor has not yet been called.
      return;
    }
  
    try {
      reset(  );
      synchronized(  syncObj){
      interp.exec( "innerClass.setDefaultParameters(  )" );
      }
      ParamsSet = true;
    } catch( InstantiationError ie ) {
      //setDefaultParameters can be called before it is at all useful, so we
      //will catch the exception and print a message.
      System.out.println( "Error in setDefaultParameters in PyScriptOperator " +
        ie.getMessage(  ) );
    }catch( PyException s ) {
       //cannot get line number. May have to go in an use
       // interp on s.type to get line number.Check Python dealing with
       //exceptions
       
       append2error( "ERROR1:" + s.toString(  ));
       errLineNum     = s.traceback.tb_lineno - 1;
    
       SharedData.addmsg( reformatPythonError( s.value.toString(  ) ) );
    }catch( Throwable s)
    {
       Throwable s1 = s.fillInStackTrace( );
       StackTraceElement[] SS = s1.getStackTrace( );
       if( SS.length < 1)
       {
          append2error( s.toString( ));
          errLineNum =-1;
          return;
       }
       append2error(SS[0].toString( ));
       errLineNum = SS[0].getLineNumber( );
       return;
    }
   
  }

  /**
   * Takes a Document that contains valid Jython code so that this
   * PyScriptOperator can operate on it.  Also resets the error message to
   * null.  What this actually does is reset the internal Python script,
   * determining whether or not the new script (Document) is a fully define
   * Jython Operator and set things up accordingly.  One of the last things
   * that gets done is a call to reset(  ).
   *
   * @param inDoc The Document to interpret.
   */
  public final void setDocument( Document inDoc ) {
    script = new PyScript( inDoc );
    initOperator(  );
  }

  /**
   * Uses the interpreter to call the Java or Jython method (depending on which
   * is actually instantiated.
   *
   * @return Javadoc-style documentation for this operator. If this script does
   *         not truly define an Operator, this returns the default docs.
   */
  public final String getDocumentation(  ) {
    if( !IAmOperator ) {
      return super.getDocumentation(  );
    }
    synchronized(  syncObj){
    PyObject pyDocs = interp.eval( "innerClass.getDocumentation(  )" );

    //convert the PyObject to a useful Java Object
    String sDocs = ( String )pyDocs.__tojava__( String.class );

    return sDocs;
    }
  }

  /**
   * CURRENTLY NOT FULLY IMPLEMENTED.
   *
   * @return The position of the character in the script that generated an
   *         error.
   */
  public final int getErrorCharPos(  ) {
    if( errormessage == null ) {
      return -1;
    }

    if( errormessage.length(  ) < 1 ) {
      return -1;
    }

    return 00;
  }

  /**
   * CURRENTLY NOT FULLY IMPLEMENTED.
   *
   * @return The line number in the script that generated an error.
   */
  public final int getErrorLine(  ) {
    if( errormessage == null ) {
      return -1;
    }

    if( errLineNum >= 0 ) {
      return errLineNum;
    }

    return 0;
  }

  /**
   * Gets the generated error message.
   *
   * @return The error message created from the script error.
   */
  public final String getErrorMessage(  ) {
    return errormessage;
  }

  /**
   * Sets the whole IObserverList.   NOTE: Used when alternating between
   * different languages.
   *
   * @param IOlist The IObserver list to set.
   */
  public final void setIObserverList( IObserverList IOlist ) {
    obss = IOlist;
  }

  /**
   * Not implemented yet.
   */
  public final void setLogDoc( Document doc ) {}

  /**
   * Accessor method for getting the number of parameters that this
   * PyScriptOperator has. Uses the internal interpreter to call the
   * superclass method.
   *
   * @return The number of parameters of this PyScriptOperator. If this script
   *         does not truly define an Operator, this returns 0.
   */
  public final int getNum_parameters(  ) {
    if( !IAmOperator ) {
      return 0;
    }
    synchronized(  syncObj){
    PyObject pyNumParams = interp.eval( "innerClass.getNum_parameters(  )" );

    //convert the PyObject to a useful Java Object
    Integer numParams = ( Integer )( pyNumParams.__tojava__( Integer.class ) );

    return numParams.intValue(  );
    }
  }

  /**
   * Sets the parameter at the given index to the given parameter. Uses the
   * internal interpreter to call the superclass method.
   *
   * @param param The new IParameter.
   * @param index The index of the parameter to set.
   *
   * @return Whether or not the parameter was successfully set. If this script
   *         does not truly define an Operator, this returns false.
   */
  public final boolean setParameter( IParameter param, int index ) {
    if( !IAmOperator ) {
      return false;
    }
    synchronized(  syncObj){
    //set the parameter in the Jython namespace
    interp.set( "tempParam", param );

    PyObject pyBool = interp.eval( "innerClass.setParameter( tempParam," +
        index + ")" );

    //convert the PyObject to a useful Java Object
    Boolean bVal = ( Boolean )pyBool.__tojava__( Boolean.class );

    return bVal.booleanValue(  );
    }
  }

  /**
   * Method to access the IParameters of this PyScriptOperator. Uses the
   * internal interpreter to call the superclass method.
   *
   * @param index The index of the IParameter to get.
   *
   * @return The given IParameter. If this script does not truly define an
   *         Operator, this returns null.
   */
  public final IParameter getParameter( int index ) {
    if( !IAmOperator ) {
      return null;
    }

    synchronized(  syncObj){
    PyObject pyParam = interp.eval( "innerClass.getParameter( " + index + " )" );

    //convert the PyObject to a useful Java Object
    IParameter iParam = ( IParameter )( pyParam.__tojava__( IParameter.class ) );

    return iParam;
    }
  }

  /**
   * Sets the whole list of property change listeners.   NOTE: Used when
   * alternating between different languages.
   *
   * @param PcSupp The PropertyChangeSupport to set the list to.
   */
  public final void setPropertyChangeList( PropertyChangeSupport PcSupp ) {
    PS = PcSupp;
  }

  /**
   * Calls the getResult defined in the Python script. If this script does not
   * truly define an Operator, this returns the result of attempting to
   * execute the Jython script code.  <br>
   * <br>
   * If the Jython code does not define an Operator:<br>
   * 
   * <ul>
   * <li>
   * The variable "Result" must be assigned a value if you want this method to
   * return something other than null.
   * </li>
   * <li>
   * This method will only execute the leftmost statements.  That is, if  you
   * define Jython functions, it will not execute code within them by itself.
   * You must explicitly call those functions to execute them.
   * </li>
   * </ul>
   * 
   *
   * @return Result of executing the Script.
   */
  public final Object getResult(  ) {
    errLineNum = -1;
  
    if( !IAmOperator ) {
      //we must be working with a document that does not have a class
      //definition, so we'll try to just execute the document text
      if( script == null ) {
        return new ErrorString( "No code to translate" );
      }

      try {
        reset(  );
        synchronized(  syncObj){
        //execute level 0 stuff
        interp.exec( script.toString(  ) );
        }
        scriptLoaded = true;
        //interpreter hit an error when processing the document, so return it
        if( eos.size(  ) > 0 ) {
           append2error( "Error " + eos.toString(  ));

          return new ErrorString( errormessage );
        }

        //we don't really have an inner class to work with here, so we will
        //try to get the "Result" from the Jython code.  If we can't get it,
        //then we have to assume that no one set it, and continue on.
        synchronized(  syncObj){
        PyObject pyResult = interp.get( "Result" );
        Object   result = null;

        if( pyResult != null ) {
          result = pyResult.__tojava__( Object.class );;
        }
        pyResult = null;
        return result;
        }
      } catch( PyException s ) {
        //cannot get line number. May have to go in an use
        // interp on s.type to get line number.Check Python dealing with
        //exceptions
        System.out.println("In PyException "+ s.getClass());
        append2error("ERROR1:" + s.toString(  ));
        s.fillInStackTrace( );
        if( s.traceback != null)
            errLineNum     = s.traceback.tb_lineno - 1;
        else
           errLineNum = -1;
        SharedData.addmsg( reformatPythonError( s.value.toString(  ) ) );
       

        return new ErrorString( errormessage );
      } catch( Throwable s ) {
            Throwable s1 = s.fillInStackTrace( );
            StackTraceElement[] SS = s1.getStackTrace( );
            if( SS.length < 1)
            {
               append2error( s.toString( ));
               errLineNum =-1;
               return new ErrorString( errormessage );
            }
            append2error( SS[0].toString( ));
            errLineNum = SS[0].getLineNumber( );
        
            return new ErrorString( errormessage );
      }
    } else {
       synchronized(  syncObj){
      PyObject pyResult = interp.eval( "innerClass.getResult(  )" );

      //convert the PyObject to a useful Java Object
      Object oResult = pyResult.__tojava__( Object.class );
   
      pyResult = null;
      
      return oResult;
       }
    }
  }

  /**
   * Does nothing here.  The title must be returned from the Jython script, and
   * so until an Operator exists which can handle a title, this cannot be
   * used.
   */
  public final void setTitle( String title ) {}

  /**
   * Testbed.
   */

  /*public static void main( String[] args ) {
     StringBuffer s       = new StringBuffer(  );
        PyScriptOperator pso = new PyScriptOperator(
            "/home/coldfire/ISAW/Scripts/TestArrayPG.py" );
        System.out.println( "Testing getTitle():" );
        System.out.println( pso.getTitle(  ) );
        System.out.println(  );
        System.out.println( "Testing getCategoryList():" );
        String[] categories = pso.getCategoryList(  );
        for( int i = 0; i < categories.length; i++ ) {
          System.out.println( categories[i] );
        }
        System.out.println(  );
        System.out.println( "Testing getCommand():" );
        System.out.println( pso.getCommand(  ) );
        System.out.println(  );
        System.out.println( "Testing getNum_parameters():" );
        System.out.println( pso.getNum_parameters(  ) );
        System.out.println(  );
        System.out.println( "Testing getDocumentation():" );
        System.out.println( pso.getDocumentation(  ) );
        System.out.println(  );
        System.out.println( "Testing change of parameters:" );
        System.out.println( "Old parameter" + pso.getParameter( 0 ) );
        pso.setParameter( new ArrayPG( "Enter Numbers", "20:30" ), 0 );
        System.out.println( "New parameter" + pso.getParameter( 0 ) );
        System.out.println(  );
        System.out.println( "Testing getResult() with the default parameters:" );
        System.out.println( pso.getResult(  ) );
        System.out.println(  );
        System.out.println( "Testing clone:" );
        PyScriptOperator pso2 = ( PyScriptOperator )pso.clone(  );
        System.out.println( "Setting Operator #1's parameter to \"99:123\"" );
        pso.getParameter( 0 )
           .setValue( "99:123" );
        System.out.println( "Operator #1's parameter value:" );
        System.out.println( pso.getParameter( 0 ).getValue(  ) );
        System.out.println( "Operator #2's parameter value (original):" );
        System.out.println( pso2.getParameter( 0 ).getValue(  ) );
        System.out.println(  );*/
  /*System.out.println( "Testing addParameter(...)" );
     pso.testAddParameter(
       new IntegerPG( "testParameter", new Integer( "5" ), false ) );
     System.out.print( "There are now " + pso.getNum_parameters(  ) );
     System.out.println( " parameters." );*/
  /*System.out.println( "Testing clearParametersVector():" );
     pso.clearParametersVector(  );
     System.out.println( "After clearing, the number of parameters is: " );
     System.out.println( pso.getNum_parameters(  ) );
     System.out.println( "Testing createCategoryList():" );
     categories = pso.testCCL(  );
     for( int i = 0; i < categories.length; i++ ) {
       System.out.println( categories[i] );
     }*/
  /*System.out.println(  );
     System.out.println( "Op #1's parameters before copy: " );
     System.out.println( pso.getParameter( 0 ).getValue(  ) );
     System.out.println( "Op #2's parameters before copy: " );
     System.out.println( pso2.getParameter( 0 ).getValue(  ) );
     System.out.println( "Copying parameter from Op #1 to Op #2:" );
     pso.CopyParametersFrom( pso2 );
     System.out.println( "Op #1's parameters after copy: " );
     System.out.println( pso.getParameter( 0 ).getValue(  ) );
     System.out.println( "Op #2's parameters after copy: " );
     System.out.println( pso2.getParameter( 0 ).getValue(  ) );
     System.out.println( "Testing toString(  )" );
     System.out.println( pso.toString(  ) );
     }*/

  /**
   * Gets the title (just the short script name) of this PyScriptOperator.
   * While toString() returns the fully qualified filename (due to a call
   * outside of Jython's namespace), this returns a name that is useful for
   * drop down menu in ISAW.
   *
   * @return The "short" name for the inner script. If this script does not
   *         truly define an Operator, this returns super.getTitle().
   */
  public final String getTitle(  ) {
    if( !IAmOperator ) {
      return super.getTitle(  );
    }
    synchronized(  syncObj){
    PyObject pyString = interp.eval( "innerClass.getTitle(  )" );

    //convert the PyObject to a useful Java Object
    String stringRep = ( String )pyString.__tojava__( String.class );

    return stringRep;
    }
  }

  /**
   * Gets the version of Python on the system.
   *
   * @return Formatted Python version number.
   */
  public final String getVersion(  ) {
     synchronized(  syncObj){
    return "V1-PYth v" + PySystemState.version;
     }
  }

  /**
   * Copy the parameter list from operator "op" to the current operator.  The
   * original list of parameters is cleared before copying the new parameter
   * list. Uses the internal interpreter to call the superclass method. If
   * this script does not truly define an Operator, this does nothing.
   *
   * @param op The operator object whose parameter list is to be  copied to the
   *        current operator.
   */
  public final void CopyParametersFrom( Operator op ) {
    if( !IAmOperator ) {
      return;
    }
    synchronized(  syncObj){
    //set the operator in the Jython namespace
    interp.set( "tempOp", op );
    interp.exec( "innerClass.CopyParametersFrom( tempOp )" );
    }
  }

  /**
   * Adds a DataSet to the Vector of DataSets.
   *
   * @param dss The DataSet to add.
   */
  public final void addDataSet( DataSet dss ) {
    dss.addIObserver( this );
    Dsets.addElement( dss );
    synchronized(  syncObj){
    interp.set( "ISAWDS" + dss.getTag(  ), dss );
    }
   
  }

  /**
   * Adds an IObserver to the list of IObservers.
   *
   * @param iobs The IObserver to add.
   */
  public final void addIObserver( IObserver iobs ) {
    obss.addIObserver( iobs );
  }

  /**
   * Adds the given property change listener.
   *
   * @param pcl The PropertyChangeListener to add.
   */
  public final void addPropertyChangeListener( PropertyChangeListener pcl ) {
    PS.addPropertyChangeListener( pcl );
  }

  /**
   * Adds the given property change listener.
   *
   * @param propName The name of the property.
   * @param pcl The PropertyChangeListener to add.
   */
  public final void addPropertyChangeListener( String propName,
    PropertyChangeListener pcl ) {
    PS.addPropertyChangeListener( propName, pcl );
  }

  /**
   * Clones this PyScriptOperator.  Works by creating a new PyScriptOperator
   * using the copy constructor, then adding the DataSets and IObservers from
   * the old PyScriptOperator to this one (i.e. it produces a deeper copy of
   * this PyScriptOperator than the copy constructor).
   *
   * @return A clone of this Operator.
   */
  public final Object clone(  ) {
    DataSet[]        newDS  = this.getDataSets(  );
    PyScriptOperator newPSO = new PyScriptOperator( this );

    for( int i = 0; i < newDS.length; i++ ) {
      newPSO.addDataSet( newDS[i] );
    }

    newPSO.setIObserverList( obss );

    return newPSO;
  }

  /**
   * Removes an IObserver from the list.
   *
   * @param iobs The IObserver to remove.
   */
  public final void deleteIObserver( IObserver iobs ) {
    obss.deleteIObserver( iobs );
  }

  /**
   * Removes all IObservers.
   */
  public final void deleteIObservers(  ) {
    obss.deleteIObservers(  );
  }

  /**
   * Executes one line of the document.
   *
   * @param Doc The document to execute one line of.
   * @param line The line to execute.
   */
  public final void execute1( Document Doc, int line ) {
    String codeLine = ScriptOperator.getLine( Doc, line );
    resetError(  );

    if( codeLine != null ) {
      eos.reset(  );
      synchronized(  syncObj){
      try {
        interp.exec( codeLine );
      } catch( Exception s ) {
         append2error(s.getMessage(  ));
        SharedData.addmsg( errormessage );
      }
      }
    }
  }

  /**
   * Executes a series of import statements.  It seems to not matter to the
   * Python interpreter whether or not multiple copies of the same import
   * statement are executed.
   *
   * @param pyInterp The PythonInterpreter to use to execute the statements.
   */
  public static final void initImports( PythonInterpreter pyInterp ) {
    String scriptsDir = SharedData.getProperty( "ISAW_HOME" );

    if( scriptsDir == null ) {
      return;
    }

    scriptsDir = StringUtil.setFileSeparator( scriptsDir + "/Scripts/" );
    synchronized(  syncObj){
    pyInterp.execfile( scriptsDir + "default_imports.py" );
    }
  }

  /**
   * Executes when a PropertyChangeEvent Occurs
   *
   * @param e The property change Event.  NOTE: The only PropertyChangeEvent
   *        processed has a name "Display"
   */
  public final void propertyChange( PropertyChangeEvent e ) {
    if( PS == null ) {
      return; // no one to notify
    }

    PS.firePropertyChange( e );
  }

  /**
   * Removes the given property change listener.
   *
   * @param pcl The PropertyChangeListener to remove.
   */
  public final void removePropertyChangeListener( PropertyChangeListener pcl ) {
    PS.removePropertyChangeListener( pcl );
  }

  /**
   * Creates a new Python Interpreter.  Adds DataSets and the IOBS variable is
   * also re-added.  This also calls the interpreter's exec method on the
   * script if the script exists.
   */
  public final void reset(  ) throws InstantiationError {
     String problem ="initing interpreter";
     try
     {
    if( interp == null ) {
      //initialize the system state
      initInterpreter(  );
    } else {
      interp.cleanup();
      resetInterpreter(  );
    }
    problem = "resetVariables";
    resetVariables(  );

    problem = "reloading scripts";
    //just reload the script if it already exists
    if( script != null ) {
      script.reload(  );
    }

    if( IAmOperator ) {
      // execute the file (level 0) --> this throws the PyException
      try { // one is faster, the other throws exceptions with the right filename

         problem = "executing script";
        interp.exec( script.toString(  ) );
        scriptLoaded = true;

        //interp.execfile(script.getFilename());
      } catch( PyException e ) {
        SharedData.addmsg( reformatPythonError( e.value.toString(  ) ) );
        throw PyScript.generateError( e, scriptFile );
      }

      problem = "defining innerclass to this class";
      interp.exec( "innerClass = " + script.getClassname(  ) + "(  )" );
    }}catch(Throwable ss)
    {
       append2error("Error at" +problem+":"+ss.toString());
       errLineNum =0;
    }
  }

 
  /**
   * Resets the internal errormessage.
   */
  public final void resetError(  ) {
    errormessage = null;
  }

  /**
   * TRIES to remove a deleted DataSet from the Jython/Python system.  Ignores
   * all other update calls. NOTE: It is difficult to get it out of the Jython
   * System
   *
   * @param observed_obj Ignored.
   * @param reason One of the DataSetTools.util.IObserver reasons.
   */
  public final void update( Object observed_obj, Object reason ) {
     
    if( observed_obj instanceof DataSet ) {
      if( reason instanceof String ) {
        if( IObserver.DESTROY.equals( reason ) ) {
          ( ( DataSet )observed_obj ).deleteIObserver( this );
           Dsets.remove( observed_obj );
           interp.set( "ISAWDS" + ((DataSet)observed_obj).getTag(  ), null );
        }
      }
    }
  }

  /**
   * Adds the given parameter to the list of parameters in Jython's namespace.
   * If this script does not truly define an Operator, this does nothing.
   *
   * @param param The IParameter to add.
   */
  protected final void addParameter( IParameter param ) {
    if( !IAmOperator ) {
      return;
    }

    //set the parameter in the Jython namespace
    interp.set( "tempParam", param );
    interp.exec( "innerClass.addParameter( tempParam )" );
  }

  /**
   * Method to create a category list from this classes nearest abstract
   * parent's package name.  Uses the internal interpreter to call the
   * superclass method.
   *
   * @return String array of the category list. If this script does not truly
   *         define an Operator, this returns super.createCategoryList().
   */
  protected final String[] createCategoryList(  ) {
    if( !IAmOperator ) {
      return super.createCategoryList(  );
    }

    PyObject pyCatList = interp.eval( "innerClass.createCategoryList(  )" );

    //convert the PyObject to a useful Java Object
    String[] catList = ( String[] )pyCatList.__tojava__( String[].class );

    return catList;
  }

  /**
   * Initializes the internal PythonInterpreter.  Don't execute this method
   * more than once unless absolutely necessary, as it calls
   * PythonInterpreter.initialize().
   */
  private final void initInterpreter(  ) {
    // get preProperties, postProperties, and systemProperties
    if( PySystemInitted ){
       resetInterpreter(  );
       return;
    }
    initPySystem();
    resetInterpreter(  );
  }
  
  private void append2error( String newError)
  {
     if( errormessage !=null && errormessage.length() >0)
        errormessage +="\n";
     else
        errormessage ="";
     
     errormessage +=newError;
  }
  private final void initPySystem()
  {
    if( PySystemInitted)
       return;
    
    PySystemInitted = true;
    Properties postProps = new Properties(  );
    Properties sysProps = System.getProperties(  );

    // put systemProperties (those set with -D) in postProps
    Enumeration e = sysProps.propertyNames(  );

    boolean homeSet= false;
    String pyPath="";
    while( e.hasMoreElements(  ) ) 
    {
      String name = ( String )e.nextElement(  );
      name = name.trim();
      if( name.startsWith( "python." ) )
       
      { 
        if( name.equals( "python.home" )) 
           homeSet=true;
        
        else if( name.equals( "python.path" ))
        {
           pyPath = System.getProperty(name);
           if( pyPath == null || pyPath.trim( ).length() <=0)
              pyPath ="";
       
           
        }else 
           postProps.put( name, System.getProperty( name ) );
      }else
         postProps.put( name, System.getProperty( name ) );
    }
    String pyPath1 = gov.anl.ipns.Util.File.FileIO.appendPath( 
          System.getProperty( "ISAW_HOME"),"PythonSources/Lib/" );
    if( pyPath.length() > 0)
       pyPath1 =File.pathSeparatorChar+pyPath1;
    pyPath = gov.anl.ipns.Util.File.FileIO.appendPath( pyPath , 
              pyPath1 ) ;
    System.setProperty( "python.path" , FixFileName(pyPath) );
    
    String home = 
          System.getProperty("ISAW_HOME")+"/jython";
   
   // if( !homeSet)
       postProps.put( "python.home" , FixFileName(home) );
    
    postProps.put( "internalTablesImpl" , "weak" );
    
    // here's the initialization step
    PythonInterpreter.initialize( sysProps, postProps, null );
    
  }
  
 //Fixes file names and lists of filenames so they work when set as jython
  // properties
  private String FixFileName( String fileName)
  {
   
    fileName = fileName.replace( '\\' , '/' );
    
    StringUtil.replace( fileName , "//" , "/" );
    StringUtil.replace( fileName , "//" , "/" );
    fileName.replace( '/' , '\\');
    fileName = StringUtil.setFileSeparator( fileName );
    fileName = fileName.replace( ';' , File.pathSeparatorChar );
    return StringUtil.replace( fileName , "\\" , "\\\\" );
    
  }
  /**
   * Sets the internal IAmOperator variable, as well as calling reset(  ) to
   * reset the interpreter variables and call setDefaultParameters if the
   * script defines an operator.
   */
  private final void initOperator(  ) 
  {
     try
     {
    synchronized(  syncObj){
    initPySystem();
    if( script.isValid(  ) ) {
      IAmOperator = true;
    } else {
      IAmOperator = false;
    }
    }
    reset(  );

    if( IAmOperator ) {
      setDefaultParameters(  );
    }
     }catch( Throwable ss)
     {
        String[] SS = ScriptUtil.GetExceptionStackInfo( ss , true , 3 );
        String message = ss.toString( );
        if( SS != null && SS.length > 0)
           message +="\n"+SS[0];
        append2error( message);
        errLineNum=0;
     }
     }
   
  

  /**
   * Utility method to reformat a PyException string to a friendlier value.
   *
   * @param err The error (PyException.value.toString(  )) to reformat.
   */
  private String reformatPythonError( String err ) {
    StringBuffer myErr = new StringBuffer(  );

    try {
      //retrieve the message that was generated (e.g. invalid syntax).
      String[] tokens = err.split( "," );

      //error message/code
      myErr.append( tokens[0].trim(  ).substring( 2, tokens[0].length(  ) - 1 ) );
      myErr.append( " at line number " );

      //line number
      myErr.append( tokens[2].trim(  ) );
      myErr.append( ", character " );

      //character
      myErr.append( tokens[3].trim(  ) );
      myErr.append( " near code " );

      //function name
      myErr.append( tokens[4].trim(  ).substring( 1, tokens[4].length(  ) - 4 )
        .trim(  ) );
      myErr.append( "." );

      return myErr.toString(  );
    } catch( Exception e ) {
      return "Could not generate a proper error message";
    }
  }

  /**
   * Creates a new Python interpreter and executes default_imports.py.
   */
  private final void resetInterpreter(  ) {
    // instantiate AFTER initialize
    if( interp != null)
       CleanInterpreter();
    interp = new PythonInterpreter(  );
    scriptLoaded = false;

    //execute a series of default import statements
    PyScriptOperator.initImports( interp );
  }

  /**
   * Sets the variables (DataSets, IObservers, etc.) that we want the internal
   * interpreter to hold on to.  The variables will be initialized if they do
   * not exist.
   */
  private final void resetVariables(  ) {
    if( Dsets == null ) {
      Dsets = new Vector(  );
    }

    if( obss == null ) {
      obss = new IObserverList(  );
    }

    if( PS == null ) {
      PS = new PropertyChangeSupport( this );
    }

    synchronized( syncObj){
    for( int i = 0; i < Dsets.size(  ); i++ ) {
      DataSet DS = ( DataSet )( Dsets.elementAt( i ) );
      //reset the internal DataSet variables
      interp.set( "ISAWDS" + DS.getTag(  ), DS );
      
    }

    //reset the IObserver internal variable
    interp.set( "IOBS", obss );

    //reset the error stream
    eos = new ByteArrayOutputStream(  );

    //set the interpreter's error stream
    interp.setErr( eos );

    //set the interpreter's output stream
    interp.setOut( new DisplayOStream(  ) );
    }
    //reset the error message
    resetError(  );
  }
  
 public void finalize()throws Throwable{
     
     if( Dsets !=null){
        for( int i =0; i< Dsets.size();i++){
           DataSet DD = (DataSet)Dsets.elementAt(i);
           DD.deleteIObserver( this );
          // interp.exec( "del ISAWDS"+DD.getTag());
        }
        Dsets.clear();
     }

     synchronized( syncObj){
    if( interp != null)
       CleanInterpreter();
    
    if( interp != null)
       interp.cleanup();
    
    if( obss != null)
       obss.deleteIObservers();
   
    PS = null;
    try{
      if( eos != null)
         eos.close();
    }catch( Exception s){
       
    }
     }
    super.finalize(); 
  }
  
  /**
   *  If the interpret variable is being wiped out, this will attempt to
   *  delete as much of the set variables as possible
   */
  private void CleanInterpreter(){
    synchronized( syncObj){
     try{
     if( Dsets != null)
        for( int i =0; i< Dsets.size();i++){
           DataSet DD = (DataSet)Dsets.elementAt(i);
           interp.exec( "del ISAWDS"+DD.getTag());
        }
     
     if(obss != null)
       interp.exec( "del IOBS" );

     if( IAmOperator && scriptLoaded){
   
        interp.exec( "del innerClass" );
     }
      
     if( eos != null)
        try{
          eos.close();
        }catch(Exception s){
           
        }
     }catch( Exception ss){
        ss.printStackTrace();
     }
    }
      //interp.setErr( (OutputStream)null );
     // interp.setOut( (OutputStream)null ); 
 
  }

}
/**
 * Used for testing instance handliing
 * @author Ruth
 *
 */
class MyPyInterpreter extends PythonInterpreter{
   
   private static int nInterp =0;
   private int thisInterp;
   private static java.util.logging.Logger Log = null;
   public MyPyInterpreter(){
      super();
      thisInterp= nInterp++;
      if( Log == null){
         Log = java.util.logging.Logger.getLogger( "PythonInterpreter" );
         try{
         java.util.logging.FileHandler FH = new java.util.logging.FileHandler("C:\\PythonInterpreter.log");
         Log.addHandler( FH );
         FH.setFormatter( new java.util.logging.SimpleFormatter() );
         }catch(Exception sss){}
      }
      Log.setLevel( java.util.logging.Level.OFF );
      Log.info("PythonIntereter "+thisInterp+" add");
   }
   
   public void finalize() throws Throwable{

     Log.info("PythonIntereter "+thisInterp+" sub");
      super.finalize();
   }
   
}
/*=======================================
   ... Set up the initial state of myInterpreter, as before ..
   // Save the initial state of myInterpreter.
   PyObject localsMap = myInterpreter.getLocals();
   PyObject initialState = localsMap.invoke("copy");

   ... Change the state of myInterpreter, as before ...

   // Restore the initial state of myInterpreter.  
   myInterpreter.setLocals(initialState.invoke("copy"));

======================================

*/