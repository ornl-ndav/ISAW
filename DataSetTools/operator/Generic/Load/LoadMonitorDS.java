/*
 * File:  LoadMonitorDS.java
 *
 * Copyright (C) 1999, Dennis Mikkelson
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
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 *  $Log$
 *  Revision 1.6  2004/01/24 19:48:52  bouzekc
 *  Removed unused imports.  Removed unused variables in main().
 *
 *  Revision 1.5  2002/12/20 17:50:43  dennis
 *  Added getDocumentation() method. (Chris Bouzek)
 *
 *  Revision 1.4  2002/11/27 23:21:16  pfpeterson
 *  standardized header
 *
 *  Revision 1.3  2002/09/19 16:05:32  pfpeterson
 *  Now uses IParameters rather than Parameters.
 *
 *  Revision 1.2  2002/03/05 19:27:47  pfpeterson
 *  Updated @see references in javadocs.
 *
 *  Revision 1.1  2002/02/22 20:57:54  pfpeterson
 *  Operator reorganization.
 *
 */

package DataSetTools.operator.Generic.Load;

import java.io.*;
import java.util.*;
import DataSetTools.dataset.*;
import DataSetTools.retriever.*;
import DataSetTools.viewer.*;
import DataSetTools.util.*;
import DataSetTools.operator.Parameter;
import DataSetTools.parameter.*;

/**
 * Operator to load the Monitor DataSet from one IPNS runfile
 *
 * @see DataSetTools.operator.Operator
 */

public class LoadMonitorDS extends    GenericLoad
                           implements Serializable
{
  /* ------------------------ DEFAULT CONSTRUCTOR -------------------------- */
  /**
   * Construct an operator with a default parameter list.  If this constructor
   * is used, meaningful values for the parameters should be set before
   * calling getResult().
   */
   public LoadMonitorDS( )
   {
     super( "Load Monitor DataSet" );
   }


  /* ---------------------- FULL CONSTRUCTOR ---------------------------- */
  /**
   *  Construct an operator for with the specified parameter values so
   *  that the operation can be invoked immediately by calling getResult().
   *
   *  @param  file_name   The fully qualified runfile name
   *
   */
   public LoadMonitorDS( String   file_name )
   {
      super( "Load Monitor DataSet" );

      IParameter parameter = getParameter(0);
      parameter.setValue( file_name );
   }

  /* -------------------------- setDefaultParameters ----------------------- */
  /**
   *  Set the parameters to default values.
   */
  public void setDefaultParameters()
  {
    parameters = new Vector();  // must do this to clear any old parameters

    Parameter parameter = new Parameter("Full File Name:", "" );
    addParameter( parameter );
  }


  /* ---------------------------- getCommand ------------------------------- */
  /**
   * @return  the command name to be used with script processor: 
   *          in this case, Mon
   *
   */
   public String getCommand()
   {
     return "Mon";
   }

  /* ---------------------- getDocumentation --------------------------- */
  /**
   *  Returns the documentation for this method as a String.  The format
   *  follows standard JavaDoc conventions.
   */
   public String getDocumentation()
   {
     StringBuffer s = new StringBuffer("");
     s.append("@overview This operator loads the Monitor DataSet from ");
     s.append("one IPNS runfile.");
     s.append("@assumptions The file exists and it contains valid ");
     s.append("runfile data.");
     s.append("@algorithm Uses the IPNS runfile data to create a new");
     s.append("DataSet.");
     s.append("@param file_name The fully qualified runfile name.");
     s.append("@return A new DataSet containing the monitor data for the ");
     s.append("specified run.");
     s.append("@error Returns an ErrorString if the file could not be ");
     s.append("opened.");
     return s.toString();
   }

  /* ----------------------------- getResult ---------------------------- */
  /**
   * Load the monitor DataSet from an IPNS runfile. 
   *
   * @return  A DataSet containing the monitor data for the specified run
   *          is returned as a java Object, or an error string is returned
   *          if the file could not be opened.
   */
   public Object getResult()
   {
     RunfileRetriever rr;
                                          // get the parameters specifying the
                                          // runs
     String    file_name   = (String)getParameter(0).getValue();

     rr = new RunfileRetriever( file_name );

     DataSet ds = rr.getFirstDataSet( RunfileRetriever.MONITOR_DATA_SET );
     if ( ds == null )
     {
       System.out.println("ERROR: no monitor DataSets in " + file_name );
       return new ErrorString("ERROR: no monitor DataSets in " + file_name);
     }

     return ds;
   }


   /* -------------------------------- main ------------------------------ */
   /*
    * main program for test purposes only
    */

   public static void main(String[] args)
   {
     LoadMonitorDS loader = new LoadMonitorDS(
                                "/IPNShome/dennis/ARGONNE_DATA/hrcs2444.run" );

      Object result = loader.getResult();
      if ( result instanceof DataSet )
      {
        DataSet ds = (DataSet)result;
        new ViewManager( ds, IViewManager.IMAGE );
      }
      else
        System.out.println( result.toString() );

      System.out.println("\nCalling getDocumentation():\n");
      System.out.println(loader.getDocumentation());
   }
}
