
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
 * Revision 1.1  2004/01/08 23:51:37  rmikk
 * Initial Checkin
 *
 */

package Operators.Generic.Load;

import DataSetTools.operator.Wrappable;
import DataSetTools.operator.*;
import DataSetTools.components.View.*;
import DataSetTools.components.View.TwoD.*;
import DataSetTools.parameter.*;
import DataSetTools.operator.Generic.Load.*;
import java.util.*;

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
    *   @param data   the 2-D to be shown
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
   
   public ViewArray( float[][] data,
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
        setParameter( new Parameter("",data),0);
        setParameter( new StringPG("Enter Title",Title),1);
        setParameter(new FloatPG("Min x",new Float(minx)),2);
        setParameter(new FloatPG("Max x",new Float(maxx)),3);
        setParameter(new FloatPG("Min y",new Float(miny)),4);
        setParameter(new FloatPG("Max y",new Float(maxy)),5);
        setParameter( new StringPG("X label",Xlabel),6);
        setParameter( new StringPG("Enter Y label",Ylabel),7);
        setParameter( new StringPG("Enter X units",Xunits),8);
        setParameter( new StringPG("Enter Y units",Yunits),9);

  }

  /**
  *   Sets the defaults for the parameters
  */
  public void setDefaultParameters(){
        parameters = new Vector();
        addParameter( new Parameter("",new float[0][0]));
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
       s.append( "@param data   the 2-D to be shown ");
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
   
    float[][] data= (float[][])(getParameter(0).getValue());
    String Title  = getParameter(1).getValue().toString();
    float minx= ((FloatPG)getParameter(2)).getfloatValue(), 
          maxx=((FloatPG)getParameter(3)).getfloatValue(),
          miny =((FloatPG)getParameter(4)).getfloatValue(),
          maxy =((FloatPG)getParameter(5)).getfloatValue();
    String Xlabel =getParameter(6).getValue().toString(),
           Ylabel =getParameter(7).getValue().toString(),
           Xunits =getParameter(8).getValue().toString(),
           Yunits=getParameter(9).getValue().toString();
    
    VirtualArray2D Varray= new VirtualArray2D( data);
    Varray.setAxisInfo( AxisInfo.X_AXIS,minx,maxx,Xlabel,Xunits,true);
    Varray.setAxisInfo( AxisInfo.Y_AXIS,miny,maxy,Ylabel,Yunits,true);
    (new ViewerSim(new ImageViewComponent(Varray))).show();
    
    return "Success";
  }

  /**
   *   Test program for this operator
   */
  public static void main( String args[]){
     float[][] dat={ {1.00f,2.0f,3.0f,4.0f},
                     {2.0f,3.0f,4.0f,5.0f},
                     {3.0f,4.0f,5.0f,6.0f},
                     {4.0f,5.0f,6.0f,6.0f}};
     ViewArray  V = new ViewArray(dat,"Test",0.0f,5.0f,0.0f,10.0f,"X","Y","cm","ft");
     System.out.println( V.getResult());


  }
}
