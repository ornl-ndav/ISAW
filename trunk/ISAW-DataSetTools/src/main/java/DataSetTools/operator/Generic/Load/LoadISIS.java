/*
 * File:  LoadISIS.java
 *
 * Copyright (C) 2003 Dominic Kramer
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
 *           Chris Bouzek <coldfusion78@yahoo.com>
 *           Department of Mathematics, Statistics and Computer Science
 *           University of Wisconsin-Stout
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the National Science Foundation under grant
 * number DMR-0218882, and by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 */
 
/*
 * If you place this file in, for example,
 * DataSetTools/operator/Generic/Special, change the line below to
 *
 * package.DataSetTools.operator.Generic.Special;
 *
 * This will allow you to have your Operator show up in the correct ISAW menu
 * (in this case, under Generic -> Special.
 */
package DataSetTools.operator.Generic.Load;

import gov.anl.ipns.Util.SpecialStrings.LoadFileString;
import DataSetTools.operator.HiddenOperator;
import DataSetTools.operator.Wrappable;
import ISIS.retriever.ISISRawfileRetriever;


/**
 * This class is a template for IPNS users to code their own Java routines.
 * This wrapper is used by the JavaWrapperOperator when it creates an
 * Operator.
 */

/*
 * You should change the name "WrapperTemplate" to the name you want (e.g.
 * Crunch, Integrate, etc.).  The HiddenOperator implementation is used so that
 * Operator will not show up in the menus.  You will generally not use it.
 *
 * To repeat:  You will generally not implement HiddenOperator unless you
 * do not want your Operator to show up in the menu.
 */
public class LoadISIS implements Wrappable, HiddenOperator {
  //~ Instance fields **********************************************************

  /*
   * Place the variables you are going to use here.
   * Each MUST HAVE the "public" before it.
   */
  public LoadFileString Filename;

  //~ Methods ******************************************************************

  /**
   * Uncomment the lines below if you want to use your own command name.
   * Normally the command name is the name of your class file (e.g.
   * ArtsIntegrate) in all capital letters.
   */
  public String getCommand(  ) {
	return "LoadISIS";
  }

  /**
   * Please document what your Operator does.  It can be beneficial to
   * everyone.
   */
  public String getDocumentation(  ) {
	//uncomment the lines below and fill in what each asks for when you are
	//writing your documentation

	   StringBuffer s = new StringBuffer(  );
	   s.append( "@overview Reads data from an ISIS RAW file and creates a DataSet " );
	   s.append("encapsulating the data.");
	   s.append( "@algorithm This operator just creates an ISISRawfileRetriever which is " );
	   s.append( "used to get DataSet zero from the ISIS RAW file." );
	   s.append( "@param Filename The full filename for the ISIS RAW file to be read." );
	   s.append( "@return DataSet zero created from the data enclosed in the ISIS RAW file." );
	   s.append( "@error For the file to be properly read, it must be an ISIS RAW file." );
	   return s.toString(  );
  }

  /**
   * This method/function is where you will do the majority of your
   * calculations.  Do not pass in any parameters.  Use the variables you
   * declared as "public" at the top of the file.
   */
  public Object calculate(  ) {
	/* This method returns a result.  At some point, you should assign the end
	 * result of your calculations to this variable (e.g. result =
	 * calculatedResult).  Do not remove this line.
	 */
	Object result = null;
	
	ISISRawfileRetriever rr = new ISISRawfileRetriever( Filename.toString() );
	result = rr.getDataSet( 0 );
	
	//do not remove this line
	return result;
  }
}
