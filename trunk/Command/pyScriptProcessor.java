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
 * Revision 1.3  2003/07/08 16:42:04  bouzekc
 * Added all missing javadocs, reformatted for consistency.
 *
 * Revision 1.2  2003/01/09 15:08:53  rmikk
 * Fixed an error in the unix version that causes an exception to occur if the
 * result of a variable in the Jython system is null.
 *
 * Reinitialized the variable errormessage so that when an error occurs, the next run is not affected by it.
 *
 * Revision 1.1  2003/01/02 20:46:43  rmikk
 * Initial Checkin for the interface to Jython's Scripting language
 *
 */
package Command;

import DataSetTools.dataset.*;

import DataSetTools.operator.*;

import DataSetTools.util.*;

import org.python.util.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import javax.swing.text.*;


/**
 * This class interfaces the Scripting language to Jythons Interpreter class
 * The variable IOBS ( the list of observers ) and all the Isaw Data sets are
 * available to the interpreter.
 */
public class pyScriptProcessor extends ScriptProcessorOperator
  implements IObserver
  {
  //~ Instance fields **********************************************************

  Document doc;
  Vector Dsets;
  Document logdoc;
  IObserverList obss;
  PythonInterpreter Pinterpret;
  PropertyChangeSupport PS;
  String errormessage;
  ByteArrayOutputStream eos;

  //~ Constructors *************************************************************

  /**
   * Constructor
   *
   * @param doc The document containing the script
   */
  public pyScriptProcessor( Document doc )
    {
    this.doc     = doc;
    Dsets        = new Vector(  );
    logdoc       = null;
    obss         = new IObserverList(  );
    Pinterpret   = new PythonInterpreter(  );
    Pinterpret.set( "IOBS", obss );
    eos = new ByteArrayOutputStream(  );
    Pinterpret.setErr( eos );
    Pinterpret.setOut( new DisplayOStream(  ) );
    PS             = new PropertyChangeSupport( this );
    errormessage   = null;
    }

  //~ Methods ******************************************************************

  /**
   * Accessor method to get the internal DataSet array.
   *
   * @return The internal DataSet array.
   */
  public DataSet[] getDataSets(  )
    {
    int n        = Dsets.size(  );
    DataSet[] DS;

    DS = new DataSet[n];

    for( int i = 0; i < n; i++ )
      {
      DS[i] = ( DataSet )( Dsets.elementAt( i ) );
      }

    return DS;
    }

  /**
   * Sets the default parameters.  There are none here. The main program will
   * have to bring up the JParametersDialog box itself (somehow).
   */
  public void setDefaultParameters(  )
    {
    errormessage   = null;
    parameters     = new Vector(  );
    }

  /**
   * Sets the document that contains the Python scripts.
   *
   * @param Doc The document to set.
   */
  public void setDocument( Document Doc )
    {
    doc            = Doc;
    errormessage   = null;
    }

  /**
   * Gets the position of the character that generated an error.
   *
   * @return The position of the character in the script that generated an
   *         error.
   */
  public int getErrorCharPos(  )
    {
    if( errormessage == null )
      {
      return -1;
      }

    if( errormessage.length(  ) < 1 )
      {
      return -1;
      }

    return 00;
    }

  /**
   * Gets the line that generated an error.
   *
   * @return The line number in the script that generated an error.
   */
  public int getErrorLine(  )
    {
    if( errormessage == null )
      {
      return -1;
      }

    return 00;
    }

  /**
   * Gets the generated error message.
   *
   * @return The error message created from the script error.
   */
  public String getErrorMessage(  )
    {
    //DataSetTools.util.SharedData.addmsg("getErrormessage="+errormessage);
    return errormessage;
    }

  /**
   * Sets the whole IObserverList.   NOTE: Used when alternating between
   * different languages.
   *
   * @param IOlist The IObserver list to set.
   */
  public void setIObserverList( IObserverList IOlist )
    {
    obss = IOlist;
    }

  /**
   * Saves the internal log.  As of ISAW 1.5 alpha6, the internal log is null,
   * so it does nothing.
   *
   * @param doc The document to save the log to.
   */
  public void setLogDoc( javax.swing.text.Document doc )
    {
    logdoc = doc;
    }

  /**
   * Sets the whole list of property change listeners.   NOTE: Used when
   * alternating between different languages.
   *
   * @param PcSupp The PropertyChangeSupport to set the list to.
   */
  public void setPropertyChangeList( PropertyChangeSupport PcSupp )
    {
    PS = PcSupp;
    }

  /**
   * Executes the script, returning the result.  Note that only the main
   * program executes.  Methods are loaded into the system and can be used
   * later in the immediate pane.
   *
   * @return The result of running the script.
   */
  public Object getResult(  )
    {
    if( doc == null )
      {
      return new ErrorString( "No code to translate" );
      }

    try
      {
      errormessage = null;

      reset(  );
      Pinterpret.exec( doc.getText( 0, doc.getLength(  ) ) );

      if( eos.size(  ) > 0 )
        {
        errormessage = "Error:" + eos.toString(  );

        return new ErrorString( "Error:" + eos.toString(  ) );
        }

      Object O = Pinterpret.get( "Result", Object.class );

      return O;
      }
    catch( org.python.core.PySyntaxError s )
      {
      errormessage = "ERROR1:" + s.toString(  );

      return new ErrorString( errormessage );
      }
    catch( Exception s )
      {
      errormessage = "ERROR2:" + s.toString(  );

      return new ErrorString( "ERROR:" + s.toString(  ) );
      }
    }

  /**
   * Gets the version of Python on the system.
   *
   * @return Formatted Python version number.
   */
  public String getVersion(  )
    {
    return "V1-PYth v" + org.python.core.PySystemState.version;
    }

  /**
   * Adds a DataSet to the Vector of DataSets.
   *
   * @param dss The DataSet to add.
   */
  public void addDataSet( DataSet dss )
    {
    dss.addIObserver( this );
    Dsets.addElement( dss );

    long tag = dss.getTag(  );

    Pinterpret.set( "ISAWDS" + tag, dss );
    }

  /**
   * Adds an IObserver to the list of IObservers.
   *
   * @param iobs The IObserver to add.
   */
  public void addIObserver( IObserver iobs )
    {
    obss.addIObserver( iobs );
    }

  /**
   * Adds the given property change listener.
   *
   * @param The PropertyChangeListener to add.
   */
  public void addPropertyChangeListener( java.beans.PropertyChangeListener P )
    {
    PS.addPropertyChangeListener( P );
    }

  /**
   * Removes an IObserver from the list.
   *
   * @param iobs The IObserver to remove.
   */
  public void deleteIObserver( IObserver iobs )
    {
    obss.deleteIObserver( iobs );
    }

  /**
   * Removes all IObservers.
   */
  public void deleteIObservers(  )
    {
    obss.deleteIObservers(  );
    }

  /**
   * Executes one line of the document.
   *
   * @param Doc The document to execute one line of.
   * @param line The line to execute.
   */
  public void execute1( javax.swing.text.Document Doc, int line )
    {
    String S = ScriptProcessor.getLine( Doc, line );

    errormessage = null;

    if( S != null )
      {  //eos= new ErrorOStream();
      eos.reset(  );

      try
        {
        Pinterpret.exec( S );
        }
      catch( Exception s )
        {
        errormessage = s.toString(  );
        DataSetTools.util.SharedData.addmsg( errormessage );
        }

      //try{eos.flush();eos.close();  }catch(IOException s){}; 
      }
    }

  /**
   * Resets the PythonInterpreter.  The DataSets are added and the IOBS varible
   * is also re-added.
   */
  public void reset(  )
    {
    Pinterpret = new PythonInterpreter(  );

    for( int i = 0; i < Dsets.size(  ); i++ )
      {
      DataSet DS = ( DataSet )( Dsets.elementAt( i ) );

      Pinterpret.set( "ISAWDS" + DS.getTag(  ), DS );
      }

    Pinterpret.set( "IOBS", obss );
    Pinterpret.set( "Result", null );
    eos = new ByteArrayOutputStream(  );
    Pinterpret.setErr( eos );
    Pinterpret.setOut( new DisplayOStream(  ) );
    errormessage = null;
    }

  /**
   * Resets the internal errormessage.
   */
  public void resetError(  )
    {
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
  public void update( java.lang.Object observed_obj, java.lang.Object reason )
    {
    if( observed_obj instanceof DataSet )
      {
      if( reason instanceof String )
        {
        if( IObserver.DESTROY.equals( reason ) )
          {
          ( ( DataSet )observed_obj ).deleteIObserver( this );

          long tag = ( ( DataSet )observed_obj ).getTag(  );

          while( Dsets.removeElement( observed_obj ) ) {}
          }
        }
      }
    }

  //~ Inner Classes ************************************************************

  /**
   * Class to handle the Display information
   */
  class DisplayOStream extends OutputStream
    {
    //~ Instance fields ********************************************************

    String ouu;

    //~ Constructors ***********************************************************

    /**
     * Creates a new DisplayOStream object.
     */
    public DisplayOStream(  )
      {
      super(  );
      ouu = "";
      }

    //~ Methods ****************************************************************

    /**
     * "Writes" one byte to a buffer.
     *
     * @param b The byte to write to the buffer.
     */
    public void write( int b ) throws IOException
      {
      ouu += ( char )b;
      }

    /**
     * "Writes" an array of bytes and dumps it out.
     *
     * @param b The array of bytes to write.
     */
    public void write( byte[] b ) throws IOException
      {
      write( b, 0, b.length );
      }

    /**
     * "Writes" a subarray of bytes to a buffer, using an offset  and length.
     *
     * @param b Array of bytes to write.
     * @param off Offset to use.
     * @param len Length of subarray.
     */
    public void write( byte[] b, int off, int len ) throws IOException
      {
      if( off < 0 )
        {
        off = 0;
        }

      if( ( off + len ) > b.length )
        {
        len = b.length - off;
        }

      for( int i = off; i < ( off + len ); i++ )
        {
        int c = ( int )( ( char )b[i] );

        write( c );
        }

      ouu = ouu.trim(  );
      DataSetTools.util.SharedData.addmsg( ouu );

      //PS.firePropertyChange("Display", null, ouu);
      ouu = "";
      }
    }

  //ErrorOStream

  /**
   * Error Stream.  Internal class used for writing error messages to a buffer.
   */
  class ErrorOStream extends OutputStream
    {
    //~ Constructors ***********************************************************

    /**
     * Creates a new ErrorOStream object.
     */
    public ErrorOStream(  )
      {
      super(  );
      errormessage = null;
      }

    //~ Methods ****************************************************************

    /**
     * "Writes" one byte to a buffer.
     *
     * @param b The byte to write to the buffer.
     */
    public void write( int b ) throws IOException
      {
      errormessage += ( char )b;
      }

    /**
     * "Writes" an array of bytes and dumps it out.
     *
     * @param b The array of bytes to write.
     */
    public void write( byte[] b ) throws IOException
      {
      write( b, 0, b.length );
      }

    /**
     * "Writes" a subarray of bytes to a buffer, using an offset  and length.
     *
     * @param b Array of bytes to write.
     * @param off Offset to use.
     * @param len Length of subarray.
     */
    public void write( byte[] b, int off, int len ) throws IOException
      {
      if( off < 0 )
        {
        off = 0;
        }

      if( ( off + len ) > b.length )
        {
        len = b.length - off;
        }

      for( int i = off; i < ( off + len ); i++ )
        {
        int c = ( int )( ( char )b[i] );

        write( c );
        }
      }
    }

  //ErrorOStream
  }


//ScriptProcessorOperator
