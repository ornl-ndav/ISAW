/*
 * File:  SetDetPos.java 
 *
 * Copyright (C) 2001, Dennis Mikkelson, Alok Chatterjee
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
 * Contact : Alok Chatterjee <achatterjee@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.2  2002/11/27 23:29:54  pfpeterson
 * standardized header
 *
 * Revision 1.1  2002/05/14 22:25:30  pfpeterson
 * Added to CVS on Alok's behalf.
 *
 * Revision 1.3  2002/02/22 20:45:05  pfpeterson
 * Operator reorganization.
 *
 * Revision 1.2  2001/11/27 18:20:13  dennis
 * Added operator title to constructor java docs.
 *
 */
package Operators;

import DataSetTools.operator.*;
import DataSetTools.operator.Generic.Special.*;
import DataSetTools.dataset.*;
import java.util.*;

/** 
 *  This operator provides a way to set the detector position attribute for
 *  a data block
 */
public class SetDetPos extends GenericSpecial
{
  private static final String TITLE = "SetDetPos";

 /* ------------------------ Default constructor ------------------------- */ 
 /**
  *  Creates operator with title "SetDetPos" and a  default list of
  *  parameters.
  */  
  public SetDetPos()
  {
    super( TITLE );
  }

 /* ---------------------------- Constructor ----------------------------- */ 
 /** 
  *  Creates operator with title "SetDetPos" and the specified list
  *  of parameters.  The getResult method must still be used to execute
  *  the operator.
  *
  *  @param  ds          DataSet to process
  *  @param  int_val     integer group ID
  *  @param  float_val1  float sph_radius parameter
  *  @param  float_val2  float  azimuth_angle parameter
  *  @param  float_val3  float polar_angle parameter
  */
  public SetDetPos( DataSet ds, 
                           int     int_val,
                           float   float_val1,
                           float   float_val2,
                           float   float_val3 )
  {
    this(); 
    parameters = new Vector();
    addParameter( new Parameter("DataSet parameter", ds) );
    addParameter( new Parameter("integer parameter", new Integer(int_val) ) );
    addParameter( new Parameter("Cylinder radius parameter", new Float(float_val1) ) );
    addParameter( new Parameter("Azimuth_angle parameter", new Float(float_val2) ) );
    addParameter( new Parameter(" Z parameter", new Float(float_val3) ) );
  }

 /* ---------------------------- getCommand ------------------------------- */ 
 /** 
  * Get the name of this operator to use in scripts
  * 
  * @return  "SetDetPos", the command used to invoke this 
  *           operator in Scripts
  */
  public String getCommand()
  {
    return "SetDetPos";
  }

 /* ------------------------ setDefaultParameters ------------------------- */ 
 /** 
  * Sets default values for the parameters.  This must match the data types 
  * of the parameters.
  */
  public void setDefaultParameters()
  {
    parameters = new Vector();
    addParameter( new Parameter("DataSet", DataSet.EMPTY_DATA_SET ) );
    addParameter( new Parameter("Group Index", new Integer(0) ) );
    addParameter( new Parameter("Radius r", new Float(0) ) );
    addParameter( new Parameter("Azimuth angle phi", new Float(0) ) );
    addParameter( new Parameter("z", new Float(0) ) );
  }

 /* ----------------------------- getResult ------------------------------ */ 
 /** 
  *  Executes this operator using the values of the current parameters.
  *
  *  @return  If successful, this template just returns a String indicating
  *           what the paramters were, and that the operator executed.  The
  *           code that does the work of the operator goes here. 
  */
  public Object getResult()
  {
    DataSet ds        =  (DataSet)(getParameter(0).getValue());
    int     group_index   = ((Integer)(getParameter(1).getValue())).intValue();
    float   float_val1 = ((Float)  (getParameter(2).getValue())).floatValue();
    float   float_val2 = ((Float)  (getParameter(3).getValue())).floatValue();
    float   float_val3 = ((Float)  (getParameter(4).getValue())).floatValue();


    DataSetTools.math.DetectorPosition detpos = new DataSetTools.math.DetectorPosition(  );
     detpos.setCylindricalCoords(float_val1, float_val2, float_val3);
    Attribute attr = new DetPosAttribute( Attribute.DETECTOR_POS, detpos);
    ds.getData_entry( group_index ).setAttribute(attr);
    ds.addLog_entry("Applied the SetDetPos");

    return ds;
  }

 /* ------------------------------- clone -------------------------------- */ 
 /** 
  *  Creates a clone of this operator.
  */
  public Object clone()
  { 
    Operator op = new SetDetPos();
    op.CopyParametersFrom( this );
    return op;
  }

 /* ------------------------------- main --------------------------------- */ 
 /** 
  * Test program to verify that this will complile and run ok.  
  *
  */
  public static void main( String args[] )
  {
     System.out.println("Test of SetDetPos starting...");

     // Test the template by constructing and running it, specifyinge
     // values for all of the parameters.
     SetDetPos op = new SetDetPos( DataSet.EMPTY_DATA_SET, 
                                             1, 3.14149f, 3.14159f, 3.14169f);
     Object obj = op.getResult();
     System.out.println("Using test parameters, the operator returned: ");
     System.out.println( (String)obj );

     // Test the template by constructing and running it, this time with the
     // default constructor.
     op = new SetDetPos();
     obj = op.getResult();
     System.out.println("Using default parameters, the operator returned: ");
     System.out.println( (String)obj );

     System.out.println("Test of SetDetPos done.");
  }
}
