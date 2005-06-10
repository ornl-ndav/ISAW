
/*
 * File:  ViewArray.java 
 *             
 * Copyright (C) 2004, Ruth Mikkelson
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
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.7  2005/06/10 22:10:30  rmikk
 * Now uses the Display2D in place of the very deprecated ViewerSim.
 * Also the data can be any object that can be converted to a float[][].
 *
 * Revision 1.6  2005/06/02 22:34:23  dennis
 * Modified to just use IVirtualArray2D methods on a
 * VirtualArray2D object.
 *
 * Revision 1.5  2004/09/15 22:05:02  millermi
 * - Updated LINEAR, TRU_LOG, and PSEUDO_LOG setting for AxisInfo class.
 *   Adding a second log required the boolean parameter to be changed
 *   to an int. These changes may affect any ObjectState saved configurations
 *   made prior to this version.
 *
 * Revision 1.4  2004/03/15 19:36:54  dennis
 * Removed unused imports after factoring out view components,
 * math and utilities.
 *
 * Revision 1.3  2004/03/15 03:37:00  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.2  2004/01/30 02:24:37  bouzekc
 * Removed unused variables and imports.
 *
 * Revision 1.1  2004/01/08 23:51:37  rmikk
 * Initial Checkin
 *
 */

package Operators.Generic.Load;

import DataSetTools.operator.*;
import DataSetTools.parameter.*;
import DataSetTools.operator.Generic.Load.*;
import gov.anl.ipns.ViewTools.Components.*;
import gov.anl.ipns.ViewTools.Components.TwoD.*;

import java.util.*;
import gov.anl.ipns.ViewTools.Displays.*;
/**
  *  This class is an operator wrapper around DataSetTools.components.View.ViewerSim
  */
public class ViewArray extends GenericLoad implements HiddenOperator{
  //~ Instance fields **********************************************************

   public ViewArray(){

      super( "View a 2D Array");
      setDefaultParameters();
   }

   /**
    *   Constructor
    *   @param data   the 2-D to be shown.It can be vectorized or int[][]. It will be converted
    *   @param Title   the Title on the display
    *   @param minx    the minimum value for the columns
    *   @param maxx    the maximum value for the columns
    *   @param miny    the minimum value for the rows
    *   @param maxy    the maximum value for the rows
    *   @param Xlabel   the label for the columns
    *   @param Ylabel   the label for the rows
    *   @param Xunits   the units for the columns
    *   @param Yunits   the units for the rows
    */
   
   public ViewArray( Object data,
                     String Title ,
                     float minx, 
                     float         maxx,
                     float    miny ,
                     float  maxy ,
                     String Xlabel ,
                     String    Ylabel,
                     String  Xunits ,
                     String  Yunits){
        this();
        parameters = new Vector();
        addParameter( new PlaceHolderPG("",data));
        addParameter( new StringPG("Enter Title",Title));
        addParameter(new FloatPG("Min x",new Float(minx)));
        addParameter(new FloatPG("Max x",new Float(maxx)));
        addParameter(new FloatPG("Min y",new Float(miny)));
        addParameter(new FloatPG("Max y",new Float(maxy)));
        addParameter( new StringPG("X label",Xlabel));
        addParameter( new StringPG("Enter Y label",Ylabel));
        addParameter( new StringPG("Enter X units",Xunits));
        addParameter( new StringPG("Enter Y units",Yunits));

  }

  /**
  *   Sets the defaults for the parameters
  */
  public void setDefaultParameters(){
        parameters = new Vector();
        addParameter( new PlaceHolderPG("",new Object()));//So get Value works
        addParameter( new StringPG("Enter Title","Data"));
        addParameter(new FloatPG("Min x",new Float(0)));
        addParameter(new FloatPG("Max x",new Float(1)));
        addParameter(new FloatPG("Min y",new Float(0)));
        addParameter(new FloatPG("Max y",new Float(1)));
        addParameter( new StringPG("X label",""));
        addParameter( new StringPG("Enter Y label",""));
        addParameter( new StringPG("Enter X units",""));
        addParameter( new StringPG("Enter Y units",""));

  }

 
  /**
   *  The name, ViewArray, used in scripts to invoke this operator
   */
  public String getCommand(  ) {
    return "ViewArray";
  }

  
  public String getDocumentation(  ) {
    //uncomment the lines below and fill in what each asks for when you are
    //writing your documentation

    StringBuffer s = new StringBuffer(  );
       s.append( "@overview This class is an operator wrapper around ");
       s.append( "DataSetTools.components.View.ViewerSim" );
       s.append( "@param data   the 2-D to be shown. This operator will ");
       s.append("   attempt to convert any object to a 2-D float array");
       s.append( "@param Title   the Title on the display ");
       s.append( "@param minx    the minimum value for the columns ");
       s.append( "@param maxx    the maximum value for the columns ");
       s.append( "@param miny    the minimum value for the rows ");
       s.append( "@param maxy    the maximum value for the rows ");
       s.append( "@param Xlabel   the label for the columns ");
       s.append( "@param Ylabel   the label for the rows ");
       s.append( "@param Xunits   the units for the columns ");
       s.append( "@param Yunits   the units for the rows ");
       s.append( "@return Success or an Error " );
       return s.toString(  );

    //remove this line if you have added documentation
 
  }

  /**
   *  Creates the View of the 2D data
   */
  public Object getResult() {
    Object O = getParameter(0).getValue();
    
    float[][] data=null;
    try{
       data= (float[][])JavaWrapperOperator.cvrt( (new float[0][0]).getClass(), O);
    }catch(Exception s){
      return new gov.anl.ipns.Util.SpecialStrings.ErrorString(
                            "Could not convert to float[][]."+s.toString());
      
    }
    float minx= ((FloatPG)getParameter(2)).getfloatValue(), 
          maxx=((FloatPG)getParameter(3)).getfloatValue(),
          miny =((FloatPG)getParameter(4)).getfloatValue(),
          maxy =((FloatPG)getParameter(5)).getfloatValue();
    String Xlabel =getParameter(6).getValue().toString(),
           Ylabel =getParameter(7).getValue().toString(),
           Xunits =getParameter(8).getValue().toString(),
           Yunits=getParameter(9).getValue().toString();
    
    IVirtualArray2D Varray= new VirtualArray2D( data);
    Varray.setAxisInfo( AxisInfo.X_AXIS,minx,maxx,
                        Xlabel,Xunits,AxisInfo.LINEAR);
    Varray.setAxisInfo( AxisInfo.Y_AXIS,miny,maxy,
                        Ylabel,Yunits,AxisInfo.LINEAR);
   // (new ViewerSim(new ImageViewComponent(Varray))).show();
     Display2D display = new Display2D(Varray, Display2D.IMAGE,Display.CTRL_ALL);
    gov.anl.ipns.Util.Sys.WindowShower.show(display);
    return "Success";
  }

  /**
   *   Test program for this operator
   */
  public static void main( String args[]){
     int[][] dat={ {1,2,3,4},
                     {2,3,4,5},
                     {3,4,5,6},
                     {4,5,6,6}};
     ViewArray  V = new ViewArray(dat,"Test",0.0f,5.0f,0.0f,10.0f,"X","Y","cm","ft");
     System.out.println( V.getResult());


  }
}
