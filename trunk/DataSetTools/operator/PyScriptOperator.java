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
 * Revision 1.1  2003/08/11 18:01:58  bouzekc
 * Added to CVS.
 *
 */
package DataSetTools.operator;

import Command.PyScript;
import Command.pyScriptProcessor;

import DataSetTools.dataset.*;

import DataSetTools.operator.Generic.GenericOperator;

import DataSetTools.parameter.*;

import DataSetTools.util.*;

import org.python.core.*;

import org.python.util.*;

import java.beans.*;

import java.io.*;

import java.util.*;


/**
 * Operator for use with Jython/Python scripts.
 */
public class PyScriptOperator extends GenericOperator implements IObserver,
  PropertyChanger {
  //~ Instance fields **********************************************************

  private String scriptFile;
  private PythonInterpreter interp;
  private PyScript script;
  private String classname;
  private IObserverList obss;
  private Vector Dsets;
  private PropertyChangeSupport PS;

  //~ Constructors *************************************************************

  /**
   * Copy constructor for use in clone and where clone is unnecessary.  This
   * makes a fairly deep copy of the parameters.
   *
   * @param oldPSO The PyScriptOperator to copy.
   */
  public PyScriptOperator( PyScriptOperator oldPSO ) {
    //set the filename and init the Operator
    this( oldPSO.toString(  ) );

    Object tempVal = null;

    //get the old parameters and set the new ones the same
    int numParams = oldPSO.getNum_parameters(  );

    for( int i = 0; i < numParams; i++ ) {
      tempVal = oldPSO.getParameter( i )
                      .getValue(  );
      this.getParameter( i )
          .setValue( tempVal );
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
    Dsets        = new Vector(  );
    scriptFile   = filename;
    obss         = new IObserverList(  );

    //create the interpreter
    reset(  );

    //link the parameters here with the ones in the Jython namespace
    setDefaultParameters(  );
  }

  //~ Methods ******************************************************************

  /**
   * Accessor method for the categoryList. Uses the internal interpreter to
   * call the superclass method.
   *
   * @return String array of the category list.
   */
  public String[] getCategoryList(  ) {
    PyObject pyCatList = interp.eval( "innerClass.getCategoryList(  )" );

    //convert the PyObject to a useful Java Object
    String[] catList = ( String[] )pyCatList.__tojava__( String[].class );

    return catList;
  }

  /**
   * Accessor method for obtaining the command name of this PyScriptOperator.
   * Unless otherwise specified in the Python/Jython script, this is the name
   * of the file without the .py extension.
   *
   * @return Command for using this Operator in Scripts.
   */
  public String getCommand(  ) {
    PyObject pyDocs = interp.eval( "innerClass.getCommand(  )" );

    //convert the PyObject to a useful Java Object
    String sDocs = ( String )pyDocs.__tojava__( String.class );

    return sDocs;
  }

  /**
   * Accessor method to get the internal DataSet array.
   *
   * @return A new DataSet array containing references to the DataSets in the
   *         internal  DataSet array.
   */
  public DataSet[] getDataSets(  ) {
    int numDsets = Dsets.size(  );
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
   * internal interpreter is null.  This method also calls reset(  ).
   */
  public void setDefaultParameters(  ) {
    if( interp == null ) {
      //i.e. The constructor has not yet been called.
      return;
    }

    try {
      reset(  );
      interp.exec( "innerClass.setDefaultParameters(  )" );
    } catch( InstantiationError ie ) {
      //setDefaultParameters can be called before it is at all useful, so we
      //will catch the exception and print a message.
      System.out.println( ie.getMessage(  ) );
    }
  }

  /**
   * Uses the interpreter to call the Java or Jython method (depending on which
   * is actually instantiated.
   *
   * @return Javadoc-style documentation for this operator.
   */
  public String getDocumentation(  ) {
    PyObject pyDocs = interp.eval( "innerClass.getDocumentation(  )" );

    //convert the PyObject to a useful Java Object
    String sDocs = ( String )pyDocs.__tojava__( String.class );

    return sDocs;
  }

  /**
   * Sets the whole IObserverList.   NOTE: Used when alternating between
   * different languages.
   *
   * @param IOlist The IObserver list to set.
   */
  public void setIObserverList( IObserverList IOlist ) {
    obss = IOlist;
  }

  /**
   * Accessor method for getting the number of parameters that this
   * PyScriptOperator has. Uses the internal interpreter to call the
   * superclass method.
   *
   * @return The number of parameters of this PyScriptOperator.
   */
  public int getNum_parameters(  ) {
    PyObject pyNumParams = interp.eval( "innerClass.getNum_parameters(  )" );

    //convert the PyObject to a useful Java Object
    Integer numParams = ( Integer )( pyNumParams.__tojava__( Integer.class ) );

    return numParams.intValue(  );
  }

  /**
   * Sets the parameter at the given index to the given parameter. Uses the
   * internal interpreter to call the superclass method.
   *
   * @param param The new IParameter.
   * @param index The index of the parameter to set.
   */
  public boolean setParameter( IParameter param, int index ) {
    //set the parameter in the Jython namespace
    interp.set( "tempParam", param );

    PyObject pyBool = interp.eval( 
        "innerClass.setParameter( tempParam," + index + ")" );

    //convert the PyObject to a useful Java Object
    Boolean bVal = ( Boolean )pyBool.__tojava__( Boolean.class );

    return bVal.booleanValue(  );
  }

  /**
   * Method to access the IParameters of this PyScriptOperator. Uses the
   * internal interpreter to call the superclass method.
   *
   * @param index The index of the IParameter to get.
   *
   * @return The given IParameter.
   */
  public IParameter getParameter( int index ) {
    PyObject pyParam = interp.eval( "innerClass.getParameter( " + index + " )" );

    //convert the PyObject to a useful Java Object
    IParameter iParam = ( IParameter )( pyParam.__tojava__( IParameter.class ) );

    return iParam;
  }

  /**
   * Sets the whole list of property change listeners.   NOTE: Used when
   * alternating between different languages.
   *
   * @param PcSupp The PropertyChangeSupport to set the list to.
   */
  public void setPropertyChangeList( PropertyChangeSupport PcSupp ) {
    PS = PcSupp;
  }

  /**
   * Calls the getResult defined in the Python script.
   *
   * @return Result of executing the Script.
   */
  public Object getResult(  ) {
    PyObject pyResult = interp.eval( "innerClass.getResult(  )" );

    //convert the PyObject to a useful Java Object
    Object oResult = pyResult.__tojava__( Object.class );

    return oResult;
  }

  /**
   * Testbed.
   *
   * @param args unused
   */
  public static void main( String[] args ) {
    /*PyScriptOperator pso = new PyScriptOperator(
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
       System.out.println( pso.toString(  ) );*/
  }

  /**
   * Gets the title (just the short script name) of this PyScriptOperator.
   * While toString() returns the fully qualified filename (due to a call
   * outside of Jython's namespace), this returns a name that is useful for
   * drop down menu in ISAW.
   *
   * @return The "short" name for the inner script.
   */
  public String getTitle(  ) {
    PyObject pyString = interp.eval( "innerClass.getTitle(  )" );

    //convert the PyObject to a useful Java Object
    String stringRep = ( String )pyString.__tojava__( String.class );

    return stringRep;
  }

  /**
   * Copy the parameter list from operator "op" to the current operator.  The
   * original list of parameters is cleared before copying the new parameter
   * list. Uses the internal interpreter to call the superclass method.
   *
   * @param op The operator object whose parameter list is to be  copied to the
   *        current operator.
   */
  public void CopyParametersFrom( Operator op ) {
    //set the operator in the Jython namespace
    interp.set( "tempOp", op );

    interp.exec( "innerClass.CopyParametersFrom( tempOp )" );
  }

  /**
   * Adds a DataSet to the Vector of DataSets.
   *
   * @param dss The DataSet to add.
   */
  public void addDataSet( DataSet dss ) {
    dss.addIObserver( this );
    Dsets.addElement( dss );

    interp.set( "ISAWDS" + dss.getTag(  ), dss );
  }

  /**
   * Adds an IObserver to the list of IObservers.
   *
   * @param iobs The IObserver to add.
   */
  public void addIObserver( IObserver iobs ) {
    obss.addIObserver( iobs );
  }

  /**
   * Adds the given property change listener.
   *
   * @param The PropertyChangeListener to add.
   */
  public void addPropertyChangeListener( PropertyChangeListener pcl ) {
    PS.addPropertyChangeListener( pcl );
  }

  /**
   * Adds the given property change listener.
   *
   * @param propName The name of the property.
   * @param P The PropertyChangeListener to add.
   */
  public void addPropertyChangeListener( 
    String propName, PropertyChangeListener pcl ) {
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
  public Object clone(  ) {
    DataSet[] newDS = this.getDataSets(  );

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
  public void deleteIObserver( IObserver iobs ) {
    obss.deleteIObserver( iobs );
  }

  /**
   * Removes all IObservers.
   */
  public void deleteIObservers(  ) {
    obss.deleteIObservers(  );
  }

  /**
   * Removes the given property change listener.
   *
   * @param The PropertyChangeListener to remove.
   */
  public void removePropertyChangeListener( PropertyChangeListener pcl ) {
    PS.removePropertyChangeListener( pcl );
  }

  /**
   * Initializes the Python Interpreter if it is null.  Adds DataSets and the
   * IOBS variable is also re-added.
   */
  public void reset(  ) throws InstantiationError {
    if( interp == null ) {
      //initialize the system state
      initInterpreter(  );
    }

    for( int i = 0; i < Dsets.size(  ); i++ ) {
      DataSet DS = ( DataSet )( Dsets.elementAt( i ) );

      interp.set( "ISAWDS" + DS.getTag(  ), DS );
    }

    interp.set( "IOBS", obss );

    //just reload the script if it already exists
    if( script == null ) {
      script = new PyScript( scriptFile );

      if( !script.isValid(  ) ) {
        throw new InstantiationError( "Invalid Script Format" );
      }
    } else {
      script.reload(  );
    }

    // execute the file (level 0) --> this throws the PyException
    try {  // one is faster, the other throws exceptions with the right filename
      this.interp.exec( script.toString(  ) );

      //this.interp.execfile(script.getFilename());
    } catch( PyException e ) {
      e.printStackTrace(  );
      throw script.generateError( e, scriptFile );
    }

    // get the name of the class within the file
    classname = script.getClassname(  );

    //create an instance of the class contained within the script so we can use
    //it within this class
    interp.exec( "innerClass = " + classname + "(  )" );
  }

  /**
   * TRIES to remove a deleted DataSet from the Jython/Python system.  Ignores
   * all other update calls. NOTE: It is difficult to get it out of the Jython
   * System
   *
   * @param observed_obj Ignored.
   * @param reason One of the DataSetTools.util.IObserver reasons.
   */
  public void update( Object observed_obj, Object reason ) {
    if( observed_obj instanceof DataSet ) {
      if( reason instanceof String ) {
        if( IObserver.DESTROY.equals( reason ) ) {
          ( ( DataSet )observed_obj ).deleteIObserver( this );
        }
      }
    }
  }

  /**
   * Adds the given parameter to the list of parameters in Jython's namespace.
   *
   * @param param The IParameter to add.
   */
  protected void addParameter( IParameter param ) {
    //set the parameter in the Jython namespace
    interp.set( "tempParam", param );
    interp.exec( "innerClass.addParameter( tempParam )" );
  }

  /**
   * Method to create a category list from this classes nearest abstract
   * parent's package name.   Uses the internal interpreter to call the
   * superclass method.
   */
  protected String[] createCategoryList(  ) {
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
  private void initInterpreter(  ) {
    // get preProperties, postProperties, and systemProperties
    Properties postProps = new Properties(  );
    Properties sysProps  = System.getProperties(  );

    // put systemProperties (those set with -D) in postProps
    Enumeration e = sysProps.propertyNames(  );

    while( e.hasMoreElements(  ) ) {
      String name = ( String )e.nextElement(  );

      if( name.startsWith( "python." ) ) {
        postProps.put( name, System.getProperty( name ) );
      }
    }

    // here's the initialization step
    PythonInterpreter.initialize( sysProps, postProps, null );

    // instantiate AFTER initialize
    interp = new PythonInterpreter(  );

    //execute a series of default import statements
    pyScriptProcessor.initImports( interp );
  }
}
