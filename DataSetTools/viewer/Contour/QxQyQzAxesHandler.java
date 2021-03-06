/*
 * File: QxQyQzAxesHandler.java
 *
 * Copyright (C) 2002, Ruth Mikkelson
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
 * $Log$
 * Revision 1.19  2005/05/27 03:13:37  dennis
 * Changed to use get attribute method from AttrUtil, rather than
 * the old get attribute method from DataSet and Data
 *
 * Revision 1.18  2005/01/10 15:55:09  dennis
 * Removed empty statement.
 *
 * Revision 1.17  2004/08/24 18:49:41  rmikk
 * Fixed the calculation of QxQyQz so that it does not require an orientation matrix
 *
 * Revision 1.16  2004/03/15 03:29:00  dennis
 * Moved view components, math and utils to new source tree
 * gov.anl.ipns.*
 *
 * Revision 1.15  2004/02/20 16:44:33  rmikk
 * Added an import so it would compile
 *
 * Revision 1.14  2004/01/24 22:22:25  bouzekc
 * Removed unused imports and local variables.
 *
 * Revision 1.13  2003/08/26 14:04:31  rmikk
 * Used DiffractometerVecQ to get the Q vector so sand data
 *   can be viewed
 *
 * Revision 1.12  2003/05/08 15:34:39  rmikk
 * Used the SCDQxyz operqator to calculate the Qx, Qy, and Qz
 *    values.
 *
 * Revision 1.11  2003/02/18 20:24:29  dennis
 * Switched to use SampleOrientation attribute instead of separate
 * phi, chi and omega values.
 *
 * Revision 1.10  2002/11/27 23:24:29  pfpeterson
 * standardized header
 *
 */
package DataSetTools.viewer.Contour;
import gov.anl.ipns.MathTools.Geometry.*;
import DataSetTools.dataset.*;
import DataSetTools.math.*;
import DataSetTools.operator.DataSet.Information.XAxis.*;
import DataSetTools.instruments.*;

public class  QxQyQzAxesHandler implements IAxesHandler
  { DataSet ds;
    int GroupIndex = -1;
    float[] Q;
    String x_units,x_label;
    float scatteringAngle = Float.NaN;
    float pathLength = Float.NaN;
    float omega = Float.NaN, chi=Float.NaN, phi= Float.NaN,a21,a22,a31,a32;
    float[][] Trans=null;
    String[] AxisName=null;
    String[] AxisUnits=null;
    double co,so,cp,sp,ck,sk;
   /** Data Set should be converted to Q or else this should
   */
   public QxQyQzAxesHandler( DataSet DS)
     { ds = DS;
       x_units = ds.getX_units();
       x_label= ds.getX_label();
       SampleOrientation orientation =
        (SampleOrientation)ds.getAttributeValue(Attribute.SAMPLE_ORIENTATION);
     
      if( orientation != null )
        {omega = orientation.getOmega();
         phi   = orientation.getPhi();
         chi   = orientation.getChi();
         co= Math.cos(-omega);
         so=Math.sin(-omega);
         cp=Math.cos(-phi);
         sp=Math.sin(-phi);
         ck=Math.cos(-chi);
         sk=Math.sin(-chi);
        }
      
      }
   public QxQyQzAxesHandler( DataSet ds, float[][]Transf, String[]AxisName,
                              String[] AxisUnits)
     {this(ds);
       setTransformation( Transf,AxisName,AxisUnits);
     }

   public static float[] getQxQyQz( DataSet ds, int Group, float Time)
     {QxQyQzAxesHandler QH = new QxQyQzAxesHandler(ds);
      float[] q = QH.getQunitVect( Group);
      if( q == null) return null;
      
      float Q = QH.cnvrtToQ( Time);
      if( Float.isNaN(Q)) return null;
      q[0]=(float)(Q*q[0]);
      q[1]=(float)(Q*q[1]);
      q[2]=(float)(Q*q[2]);
      return q;
      }
   public void setTransformation( float [][] Transf, String[] AxisName, String[] AxisUnits)
     { this.Trans = checkTransform(Transf);
       this.AxisName = AxisName;
       this.AxisUnits = AxisUnits;
       if( AxisUnits != null)
        if( AxisUnits.length !=3)
          AxisUnits = null;
      }
   public IAxisHandler getAxis( int n)
     {if( n < 0) return null;
      if( n > 2) return null;
      String name;
      float[] cf= new float[3];
      cf[0]=0f;cf[1]=0f;cf[2]=0f;
      if( Trans == null)
        {cf[n]=1.0f;
        }
      else
        {cf[0]=Trans[n][0];
         cf[1]=Trans[n][1];
         cf[2] =Trans[n][2];
        }
      if( AxisName != null)
        name = AxisName[n];
      else
        {name="";
         if( cf[0] !=0)
            name += cf[0]+"Qx+";
         if(cf[1] !=0)
            name +=cf[1]+"Qy+";
         if(cf[2]!=0)
            name +=cf[2]+"Qz";
         }
      if( name.charAt(name.length()-1)=='+')
        name = name.substring(0,name.length()-1);
      String units ="";
      if( AxisUnits != null)
         units = AxisUnits[n];
      return new AxisHandler( cf, name, units);
     }
 
   public IAxisHandler getQxAxis()
     { return new QxAxisHandler();
     }
   public IAxisHandler getQyAxis()
    {return new QyAxisHandler();
    }
   public IAxisHandler getQzAxis()
    {return new QzAxisHandler();
     }
   public float[] getQunitVect( int GroupIndex)
     {if( this.GroupIndex == GroupIndex)
        return Q;
        Data D = ds.getData_entry( GroupIndex);
      //Will use Dennis' SCD operator
       float time =D.getX_scale().getStart_x();
       DetectorPosition pos = (DetectorPosition)
                            D.getAttributeValue( Attribute.DETECTOR_POS );

       float initial_path =
             ((Float)D.getAttributeValue(Attribute.INITIAL_PATH)).floatValue();
       SampleOrientation samporient = AttrUtil.getSampleOrientation(D);
       if( samporient == null){
          samporient = new IPNS_SCD_SampleOrientation(0f,0f,0f);
          ds.setAttribute( new SampleOrientationAttribute(
                     Attribute.SAMPLE_ORIENTATION, samporient));
          D.setAttribute( new SampleOrientationAttribute(
                Attribute.SAMPLE_ORIENTATION, samporient));
         
       }
       Object O = (new SCDQxyz(ds,GroupIndex,time)).getResult();
       Position3D q_pos = null;
       if( O instanceof Position3D)
          q_pos=(Position3D)O;
       else return null;

       Q = q_pos.getCartesianCoords();
       
       scatteringAngle = pos.getScatteringAngle();
       pathLength = initial_path +pos.getDistance();
      
       //Object Qw = op.getResult();
       if( Q == null)
          {System.out.println( "AQw is null for "+GroupIndex);
           return null;
           }
      /* if( Qw instanceof DataSetTools.util.ErrorString)
         {
           System.out.println("SCDQxyz err-"+Qw.toString());
           return null;
         }
       if( !(Qw instanceof  float[]))
         {System.out.println( "Qw !float[]");
          return null;
         }
       
       FloatAttribute Fat = (FloatAttribute)(D.getAttribute( Attribute.INITIAL_PATH ));
         DetPosAttribute DPa = (DetPosAttribute)D.getAttribute(Attribute.DETECTOR_POS);
      
      if( DPa == null) return null;
     
      DetectorPosition DP = DPa.getDetectorPosition();
       scatteringAngle = DP.getScatteringAngle();
      pathLength = Fat.getFloatValue() +DP.getDistance();
       if( Qw == null) 
         return null;
       System.out.println( "Qw class="+Qw.getClass()+","+
            (Qw instanceof Position3D));
       if( (Qw instanceof Position3D))
          Q = ((Position3D) Qw).getCartesianCoords();
       else
          return null;
      
       if( Q.length < 3)
         return Q;
       */
       float L = (float)java.lang.Math.sqrt( Q[0]*Q[0]+ Q[1]*Q[1]+Q[2]*Q[2]);
       
       if( L !=0)
         {
         Q[0]=Q[0]/L;
         Q[1]=Q[1]/L;
         Q[2]=Q[2]/L;
         }
      
       this.GroupIndex = GroupIndex;
     
     
      /*if( D == null)
        return null;
       Q = new float[3];

      DetPosAttribute DPa = (DetPosAttribute)D.getAttribute(Attribute.DETECTOR_POS);
 
      
      if( DPa == null) return null;

      DetectorPosition DP = DPa.getDetectorPosition();
      scatteringAngle = DP.getScatteringAngle();
      float[] xyz= DP.getCartesianCoords();
      float L = (float)java.lang.Math.sqrt( xyz[0]*xyz[0]+xyz[1]*xyz[1]+xyz[2]*xyz[2]);
      xyz[0] = xyz[0]/L -1; xyz[1] = xyz[1]/L; xyz[2] = xyz[2]/L; 
      L = (float)java.lang.Math.sqrt( xyz[0]*xyz[0]+xyz[1]*xyz[1]+xyz[2]*xyz[2]);
      float q1= xyz[0]/L;
      float q2 =xyz[1]/L;
      float q3 =xyz[2]/L;
    
 
      if( (Float.isNaN(omega)))
       { Q[0] = q1; Q[1]=q2;Q[2]=q3;
         
        }
      else
       { 
         
         Q[0]=(float)( q1*(cp*co+sp*so*ck)+
                       q2*(cp*so-sp*ck*co)+
                       q3*(sp*sk));
         Q[1] = (float)(q1*(sp*co-cp*so*ck)+
                        q2*(sp*so+cp*ck*co)+
                        q3*(-cp*sk));
         Q[2] = (float)(q1*(-so*sk)+
                        q2*(sk*co)+
                        q3*(ck));;
        
         
       } 
      */
       
      return Q;
      }

   public float[][] checkTransform( float[][] T)
     { if( T==null)
         return null;
       if( T.length != 3)
         return null;
       if( T[0].length !=3)
         return null;
       for(int i = 0; i<3;i++)
         for(int j=0;j<3;j++)
           if(Float.isNaN(T[i][j]))
              return null;

       int n0 = 0,n1=1,n2=2;
       if( T[0][0]==0)
         if( T[1][0]!=0)
           {n0=1;n1=1; }
         else if( T[2][0]==0)
           return null;
         else {n0=2;n2=1;}
       float[] Eq1= new float[2];
       float[] Eq2 = new float[2];
       for(int i=1;i<3;i++)
         {Eq1[i-1] = -T[n1][0]*T[n0][i]/T[n0][0]+ T[n1][i];
          Eq2[i-1] = -T[n2][0]*T[n0][i]/T[n0][0]+ T[n2][i];

          } 
       if( Eq1[0]==0)
        if( Eq2[0]==0)
          return null;
       if( Eq1[0]*Eq2[1]-Eq1[1]*Eq2[0] == 0)
          return null;
       return T;


       
      }
   public void setX_scale( int GroupIndex , XScale xscale)
     {  if( GroupIndex < 0)
           return;
        if( GroupIndex >= ds.getNum_entries())
           return;
        if( xscale == null)
          xscale = ds.getData_entry( 0).getX_scale();
        ds.getData_entry( GroupIndex).resample( xscale,0);
     }

    public XScale getX_scale(int GroupIndex)
     {  if( GroupIndex < 0)
           return null;
        if( GroupIndex >= ds.getNum_entries())
           return null;
        return ds.getData_entry( GroupIndex).getX_scale();
     }
   private boolean errorReported = false;
   public float cnvrtToQ( float v)
     {
      if(x_units.equals("Inverse Angstroms"))
        return v;
    
      if( x_units.equals("Time(us)"))
         {}
      else if(x_units.equals("meV"))
         v= tof_calc.TOFofEnergy( pathLength, v);
      else if( x_units.equals( "Angstroms" ))
         if( x_label.equals( "d-spacing" ))
           v = tof_calc.TOFofDSpacing( scatteringAngle, pathLength, v);
         else if( x_label.equals( "wavelength" ))
           v = tof_calc.TOFofWavelength(pathLength, v);
         else if( !errorReported )
           {DataSetTools.util.SharedData.addmsg("x-lablel unknown-"+x_label);
            errorReported = true;
           }
       else if( !errorReported)
          {DataSetTools.util.SharedData.addmsg("x-unit unknown-"+x_units);
            errorReported = true;
          }
 
        return tof_calc.DiffractometerQ(scatteringAngle, pathLength, v);
      
        
     }
   private float cnverFromQ( float q)
     {
      if( x_units.equals("Inverse Angstroms" ))
        return q;
      float waveLength = tof_calc.WavelengthofDiffractometerQ( scatteringAngle, q);
       if( x_units.equals("Time(us)"))
         return tof_calc.TOFofWavelength( pathLength, waveLength);

      if(x_units.equals("meV"))
        return tof_calc.EnergyFromWavelength( waveLength );

      if( x_units.equals( "Angstroms" ))
         if( x_label.equals( "d-spacing" ))
            return tof_calc.DSpacingofWavelength( scatteringAngle, waveLength );
  
         else if( x_label.equals( "wavelength" ))
           return waveLength;
         else if( !errorReported )
           {DataSetTools.util.SharedData.addmsg("x-lablel unknown-"+x_label);
            errorReported = true;
           }
       else if( !errorReported)
          {DataSetTools.util.SharedData.addmsg("x-unit unknown-"+x_units);
            errorReported = true;
          }
       return q;
      
     }
   class QxAxisHandler implements IAxisHandler
     {
       public QxAxisHandler(){}
       public String getAxisName()
        {return "Qx";
         }

      public String getAxisUnits()
       {return "/Angst";
        }

     /** Gets the axis value for this Group and xvalue<P>
      * NOTE: The y value can be gotten with getX(i)
      */
      public float getValue( int GroupIndex, int xIndex)
         { if( GroupIndex <0)
             return 0f;
           if( GroupIndex >= ds.getNum_entries())
              return 0;
           if( xIndex < 0)
              return 0.0f;
           float[] Q = getQunitVect( GroupIndex);

           if( Q== null)
               return 0f;
          
           Data D = ds.getData_entry( GroupIndex);
           if( D == null)
             return 0f;
           
           return cnvrtToQ(D.getX_scale().getX(xIndex))*Q[0];
          } 

     /** Returns the xIndex that has the given axis value = Value for  
     *  this Group
     */
      public float  getXindex( int GroupIndex, float Value)
        { float[] Q = getQunitVect( GroupIndex);
         if (Q == null) {return Float.NaN;}
          if( Q[0] == 0 )
            return -1;
          if( GroupIndex < 0)
             return -1;
          if( GroupIndex >= ds.getNum_entries())
             return -1;
          Data D = ds.getData_entry( GroupIndex);
          if( D == null)
              return -1;
           XScale xsc = D.getX_scale();
           float f = cnverFromQ( Value/Q[0]);
           int i = xsc.getI(f);//Value/Q[0]);
           /*if( i >= D.getX_scale().getNum_x())
             return -1;
           if( D.isHistogram()) i--;
           return i;
           */
           if( i >= xsc.getNum_x())
              return -1;
           if( i <= 0)
              return -1;
           
             
           float y1 = xsc.getX(i-1);
           float y2 = xsc.getX( i);
           if( y2 <= y1)
              return i;
           return i-1+(float)((f -y1)/(y2-y1));
          
         }
       public float getMaxAxisValue(int GroupIndex)
        {float[] Q = getQunitVect( GroupIndex);
         if( Q==null) {return Float.NaN;}
         Data D = ds.getData_entry( GroupIndex);
         if( D == null)
            {return Float.NaN;}
         XScale xscl =D.getX_scale();
         return java .lang.Math.max( cnvrtToQ(xscl.getStart_x()) *Q[0],
                                     cnvrtToQ(xscl.getEnd_x())*Q[0] );
         //if( Q[0] >=0)
         //   return xscl.getEnd_x() *Q[0];
         //else
         //   return xscl.getStart_x()*Q[0];
        }
      public float getMinAxisValue(int GroupIndex)
        {float[] Q = getQunitVect( GroupIndex);
         if( Q==null) {return Float.NaN;}
         Data D = ds.getData_entry( GroupIndex);
          if( D == null)
            {return Float.NaN;}
         XScale xscl =D.getX_scale();
         if( xscl == null)
           System.out.println("XSCALE IS NULL");
         if( Q == null)
           System.out.println( "Q is NULL "+GroupIndex);
         return java .lang.Math.min( cnvrtToQ(xscl.getStart_x()) *Q[0],
                                     cnvrtToQ(xscl.getEnd_x())*Q[0] );
         //if( Q[0] >=0)
         //    return xscl.getStart_x() *Q[0];
         //else
         //   return xscl.getEnd_x()*Q[0];   
        
        }

     /** Needs to be set to determine corresponding indecies.
     *
     */
     public void setXScale( int GroupIndex , XScale xscale)
      { setX_scale( GroupIndex, xscale);
      }

     public XScale getXScale(int GroupIndex)
      {  return getX_scale( GroupIndex); 
       }

      }
   class QyAxisHandler implements IAxisHandler
     { public QyAxisHandler(){}
       public String getAxisName()
        {return "Qy";
         }

      public String getAxisUnits()
       {return "/Angst";
        }

     /** Gets the axis value for this Group and xvalue<P>
      * NOTE: The y value can be gotten with getX(i)
      */
      public float  getValue( int GroupIndex, int xIndex)
         { 
           if( GroupIndex <0)
             return 0f;

           if( GroupIndex >= ds.getNum_entries())
              return 0;

          if( xIndex < 0)
              return 0.0f;
 
           float[] Q = getQunitVect( GroupIndex);

           if( Q== null)
               return 0f;
          
           Data D = ds.getData_entry( GroupIndex);

           if( D == null)
             return 0f;
           return cnvrtToQ(D.getX_scale().getX(xIndex))*Q[1];
          } 

     /** Returns the xIndex that has the given axis value = Value for  
     *  this Group
     */
      public float  getXindex( int GroupIndex, float Value)
        {  if( GroupIndex < 0)
             return -1;
           if( GroupIndex >= ds.getNum_entries())
             return -1;
          float[] Q = getQunitVect( GroupIndex);
          if( Q==null) {return Float.NaN;}
          if( (Value ==0) || (Q[1] == 0))
            return -1;
           
           Data D = ds.getData_entry( GroupIndex);
          if( D == null)
              return -1;
           
           XScale xsc = D.getX_scale();
           float f = cnverFromQ( Value/Q[1]);
           int i = xsc.getI(f);//Value/Q[0]);
           /*if( i >= D.getX_scale().getNum_x())
             return -1;
           if( D.isHistogram()) i--;
           return i;
           */
           if( i >= xsc.getNum_x())
              return -1;
           if( i <= 0)
              return -1;
           float y1 = xsc.getX(i-1);
           float y2 = xsc.getX( i);
           if( y2 <= y1)
              return i;
           return i-1+(float)((f -y1)/(y2-y1));
         }
       public float getMaxAxisValue(int GroupIndex)
        {float[] Q = getQunitVect( GroupIndex);
         if( Q==null) {return Float.NaN;}
         Data D = ds.getData_entry( GroupIndex);
          if( D == null)
            {return Float.NaN;}
         XScale xscl =D.getX_scale();
         
         return java .lang.Math.max( cnvrtToQ(xscl.getStart_x()) *Q[1],
                                     cnvrtToQ(xscl.getEnd_x())*Q[1] );
         //if( Q[1] >=0)
          //  return xscl.getEnd_x() *Q[1];
        // else
            //return xscl.getStart_x()*Q[1];
        }
      public float getMinAxisValue(int GroupIndex)
        {float[] Q = getQunitVect( GroupIndex);
         if( Q==null) {return Float.NaN;}
         Data D = ds.getData_entry( GroupIndex);
          if( D == null)
            {return Float.NaN;}
         XScale xscl =D.getX_scale();
         return java .lang.Math.min( cnvrtToQ(xscl.getStart_x()) *Q[1],
                                     cnvrtToQ(xscl.getEnd_x())*Q[1] );
         //if( Q[1] >=0)
         //    return xscl.getStart_x() *Q[1];
         //else
         //   return xscl.getEnd_x()*Q[1];   
        
        }
    public void setXScale( int GroupIndex , XScale xscale)
      { setX_scale( GroupIndex, xscale);
      }

     public XScale getXScale(int GroupIndex)
      {  return getX_scale( GroupIndex); 
       }


      }
   class QzAxisHandler implements IAxisHandler
     {public QzAxisHandler(){}
       public String getAxisName()
        {return "Qz";
         }

      public String getAxisUnits()
       {return "/Angst";
        }

     /** Gets the axis value for this Group and xvalue<P>
      * NOTE: The y value can be gotten with getX(i)
      */
      public float  getValue( int GroupIndex, int xIndex)
         { if( GroupIndex <0)
             return 0f;
           if( GroupIndex >= ds.getNum_entries())
              return 0;
          if( xIndex < 0)
              return 0.0f;
           float[] Q = getQunitVect( GroupIndex);
           if( Q== null)
               return 0f;
          
           Data D = ds.getData_entry( GroupIndex);
           if( D == null)
             return 0f;
           return cnvrtToQ(D.getX_scale().getX(xIndex))*Q[2];
          } 

     /** Returns the xIndex that has the given axis value = Value for  
     *  this Group
     */
      public float  getXindex( int GroupIndex, float Value)
        { float[] Q = getQunitVect( GroupIndex);
           if( Q == null) {
                       return Float.NaN;}
           if( Q[2] == 0)
             return -1;

           if( GroupIndex < 0)
             return -1;
           if( GroupIndex >= ds.getNum_entries())
             return -1;
           XScale xsc = ds.getData_entry(GroupIndex).getX_scale();
           float f = cnverFromQ( Value/Q[2]);
           int i = xsc.getI(f);//Value/Q[0]);
           /*if( i >= D.getX_scale().getNum_x())
             return -1;
           if( D.isHistogram()) i--;
           return i;
           */
           if( i >= xsc.getNum_x())
              return -1;
           if( i <= 0)
              return -1;
           float y1 = xsc.getX(i-1);
           float y2 = xsc.getX( i);
           if( y2 <= y1)
              return i;
          // System.out.println("i,y1,y2,V="+i+","+y1+","+y2+","+(Value/Q[2]));
           return i-1+(float)((f-y1)/(y2-y1));
         }
       public float getMaxAxisValue(int GroupIndex)
        {float[] Q = getQunitVect( GroupIndex);
         if( Q==null) {return Float.NaN;}
         Data D = ds.getData_entry( GroupIndex);
          if( D == null)
            {return Float.NaN;}
         XScale xscl =D.getX_scale();
         
         return java .lang.Math.max( cnvrtToQ(xscl.getStart_x()) *Q[2],
                                     cnvrtToQ(xscl.getEnd_x())*Q[2] );
        // if( Q[2] >=0)
         //   return xscl.getEnd_x() *Q[2];
        // else
        //    return xscl.getStart_x()*Q[2];
        }
      public float getMinAxisValue(int GroupIndex)
        {float[] Q = getQunitVect( GroupIndex);
         Data D = ds.getData_entry( GroupIndex);
         if( Q==null) {return Float.NaN;}
         if( D == null)
            {return Float.NaN;}
         XScale xscl =D.getX_scale();
         return java .lang.Math.min( cnvrtToQ(xscl.getStart_x()) *Q[2],
                                     cnvrtToQ(xscl.getEnd_x())*Q[2] );
         /*if( Q[2] >=0)
             return xscl.getStart_x() *Q[2];
         else
            return xscl.getEnd_x()*Q[2];   
          */
        
        }
      public void setXScale( int GroupIndex , XScale xscale)
        {setX_scale( GroupIndex, xscale);
        }

      public XScale getXScale(int GroupIndex)
        {return getX_scale( GroupIndex); 
        }


      }
  
  public static void main( String args[])
   {
    if( args == null)
       System.exit(0);
    if( args.length < 1)
       System.exit( 1);
    String filename = args[0].trim();
    DataSet[] DS = (new IsawGUI.Util()).loadRunfile( filename);
    if( DS == null)
      System.exit(0);
    int k= DS.length -1;
    if( args.length >1 )
      try{
         k =(new Integer( args[1].trim())).intValue();

         }
      catch( Exception u){}
     DataSet ds = DS[k];
    /* DataSetTools.operator.DataSet.DataSetOperator op1 = ds.getOperator( "Convert to Q");
     Object O = op1.getResult();
     if( O instanceof DataSet)
         ds = (DataSet)O;
     else
      {System.out.println( O);
       System.exit(0);
      }
    */
    QxQyQzAxesHandler Qax = new QxQyQzAxesHandler( ds );
    IAxisHandler Ax3 = Qax.getQzAxis();
    IAxisHandler Ax1 = Qax.getQxAxis();
    IAxisHandler Ax2 = Qax.getQyAxis();

    char c=0;
    int group=-1,indx=-1;
    while( c !='x')
     { try
         {System.out.print("Group  indx  ");
          if( c !='x')c=0;
          String S ="";

          while( c <=32)
            c= (char)System.in.read();
          while(( c > 32) &&( c!='x'))
            { S+=c;
              c= (char)System.in.read();
             }
          group= new Integer( S).intValue();
          if( c!='x')c=0;
          S ="";
          while( c <=32)
            c= (char)System.in.read();
          while(( c > 32) &&( c!='x'))
            { S+=c;
              c= (char)System.in.read();
             }
            indx = new Integer(S).intValue();
          }
        catch( Exception ss){
                 if( c=='x')System.exit(0);
                 else c=0;
                            }
          if(c !=0){
          float time = ds.getData_entry(group).getX_scale().getX(indx);
          SCDQxyz op = new SCDQxyz( ds , group, time);
          System.out.println("Dennis'="+
              op.PointInfo( ds.getData_entry(group).getX_scale().getX(indx),
                                       group));
          SCDQxyz_Dennis  op1= new SCDQxyz_Dennis(ds, group,time);
          op1.debug=true;
          System.out.println("Dennis new="+
                    op1.PointInfo( ds.getData_entry(group).getX_scale().getX(indx),
                                       group));
          System.out.println("Ruth"+Ax1.getValue(group,indx)+","+
                                   Ax2.getValue(group,indx)+","+
                                   Ax3.getValue(group,indx));
                  }

     }
   


    }
 class AxisHandler implements IAxisHandler
   {  float[] Trans;
      String name,units;
     public AxisHandler(float[]Transf, String name, String units)
       { this.Trans=Transf;
         this.name = name;
         this.units = units;
         if( units == null)
            units= "/Angst";
         
       }
      public String getAxisName()
        {return name;
         }

      public String getAxisUnits()
       {return units;
        }

     /** Gets the axis value for this Group and xvalue<P>
      * NOTE: The y value can be gotten with getX(i)
      */
      public float  getValue( int GroupIndex, int xIndex)
         { if( GroupIndex <0)
             return 0f;
           if( GroupIndex >= ds.getNum_entries())
              return 0;
          if( xIndex < 0)
              return 0.0f;
           float[] Q = getQunitVect( GroupIndex);
           if( Q== null)
               return 0f;
          
           Data D = ds.getData_entry( GroupIndex);
           if( D == null)
             return 0f;
           
           return cnvrtToQ(D.getX_scale().getX(xIndex))*
                      (Q[0]*Trans[0]+Q[1]*Trans[1]+Q[2]*Trans[2]);
          } 

     /** Returns the xIndex that has the given axis value = Value for  
     *  this Group
     */
      public float  getXindex( int GroupIndex, float Value)
        { float[] Q = getQunitVect( GroupIndex);
          if( Q==null) {return Float.NaN;}
           float V = Trans[0]*Q[0]+Trans[1]*Q[1]+Trans[2]*Q[2];
           if(V==0)
             return -1;

           if( GroupIndex < 0)
             return -1;
           if( GroupIndex >= ds.getNum_entries())
             return -1;
           XScale xsc = ds.getData_entry(GroupIndex).getX_scale();
           float f = cnverFromQ( Value/V);
      
           int i = xsc.getI(f);//Value/Q[0]);
          
           if( i >= xsc.getNum_x())
              return -1;
           if( i >= xsc.getNum_x())
              return -1;
           if( i <= 0)
              return -1;
           float y1 = xsc.getX(i-1);
           float y2 = xsc.getX( i);
           if( y2 <= y1)
              return i;
   
           return i-1+(float)((f-y1)/(y2-y1));
         }
       public float getMaxAxisValue(int GroupIndex)
        {float[] Q = getQunitVect( GroupIndex);
         if( Q==null) {return Float.NaN;}
         float V = Trans[0]*Q[0]+Trans[1]*Q[1]+Trans[2]*Q[2];
         Data D = ds.getData_entry( GroupIndex);
          if( D == null)
            {return Float.NaN;}
         XScale xscl =D.getX_scale();
         return java .lang.Math.max( cnvrtToQ(xscl.getStart_x()) *V,
                                    cnvrtToQ(xscl.getEnd_x())*V );
        }
      public float getMinAxisValue(int GroupIndex)
        {float[] Q = getQunitVect( GroupIndex);
         if( Q==null) {return Float.NaN;}
         float V = Trans[0]*Q[0]+Trans[1]*Q[1]+Trans[2]*Q[2];
         Data D = ds.getData_entry( GroupIndex);
         if( D == null)
            {return Float.NaN;}
         XScale xscl =D.getX_scale();
         return java .lang.Math.min( cnvrtToQ(xscl.getStart_x()) *V,
                                     cnvrtToQ(xscl.getEnd_x())*V );
    
        
        }
      public void setXScale( int GroupIndex , XScale xscale)
        {setX_scale( GroupIndex, xscale);
        }

      public XScale getXScale(int GroupIndex)
        {return getX_scale( GroupIndex); 
        }


    

   }//AxisHanlder
  }
