/*
 * File:  WriteNexus.java 
 *
 * Copyright (C) 2001,/ruth Mikkelson
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
 * Modified:
 *
 * $Log$
 * Revision 1.2  2002/11/27 23:21:28  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/02/22 20:58:16  pfpeterson
 * Operator reorganization.
 *
 */
package DataSetTools.operator.Generic.Save;

import DataSetTools.operator.*;
import DataSetTools.dataset.*;
import DataSetTools.writer.*;
import java.util.*;

/** This operator writes a Histogram DataSet with its associated
 *  Monitor to a Nexus file or xml file<P>
 * The title is : <Ul><B>Write Data Set to Nexus File</b>. This appears in
 *   menu bars </ul>
 * The Command Name is :<UL><B>SaveNX</b>. This is the name that is used in
 *   Scripts to invoke this operator </ul>
 */
public class WriteNexus extends GenericSave
{
/** Default constructor that is used when the parameters will be
*   set later
*/  
    public WriteNexus()
    {super( "Save as Nexus(or xml) File");
     setDefaultParameters();
    }

/** This form can be used from java.  Here the parameters are known and set.
*  The getResult method must still be used to get the operator code to run.<P>
*@param Monitor   The Monitor dataset( Could be null).<P>
*@param  Histogram The Histogram or nonMonitor data set<P>
*@param  filename  the name where the file is to be written.  If the extension
*    is xml(case insensitive) the output will be in xml format, otherwise
*    the output will be to a nexus file
*/
    public WriteNexus( DataSet Monitor, DataSet Histogram, String filename )
    {super( "Save as Nexus(or xml) File");
     parameters = new Vector();
     addParameter( new Parameter( "Monitor Data Set", Monitor));
     addParameter( new Parameter( "Histogram Data Set", Histogram));
     addParameter( new Parameter("filename", filename ));
    }

/** Returns SaveNX, the command used to invoke this operator in Scripts
*/
 public String getCommand()
   {return "SaveNX";
   }

/** Sets default parameters.  This sets the data types of the parameters.
*/
public void setDefaultParameters()
  {
     parameters = new Vector();
     addParameter( new Parameter( "Monitor Data Set", new DataSet("","")));
     addParameter( new Parameter( "Histogram Data Set", new DataSet("","")));
     addParameter( new Parameter("filename(.nxs or .xml)", "Test.nxs" ));
  }

/** Executes or runs this operator using the values of the current parameters
*@return  "Success"  no matter what
*/
public Object getResult()
  {DataSet M,D;
   M = (DataSet)(getParameter( 0 ).getValue());
   D= (DataSet)(getParameter( 1 ).getValue());
   String filename = ((String) getParameter( 2).getValue());
   int k = filename.lastIndexOf('.');
   String extension ="nxs";
   if( k>= 0)
     extension = filename.substring( k+1);
   Writer WW;
   if( extension.toUpperCase().equals("XML"))
       WW =(Writer) (new XmlWriter( filename ));
   else 
       WW =(Writer) (new NexWriter( filename ));
  
   DataSet DS[];
   DS = new DataSet [2];
   DS[0] = M;
   DS[1] = D;
   WW.writeDataSets( DS );
   
   return "Success";

  }

/** Creates a clone of this operator.
*/
public Object clone()
  { WriteNexus W = new WriteNexus();
    W.CopyParametersFrom( this );
    return W;
  }
/** Test program for this module.  
* It just test to see if classpaths are correct, etc.
*@returns  "OKKK" to System.out if successful
*/
public static void main( String args[] )
 {
  System.out.println("OKKK");
 }
}
