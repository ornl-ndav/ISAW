package DataSetTools.viewer.Contour;
import DataSetTools.dataset.*;
import DataSetTools.math.*;

public class  QxQyQzAxesHandler
  { DataSet ds;
    int GroupIndex = -1;
    float[] Q;
   /** Data Set should be converted to Q or else this should
   */
   public QxQyQzAxesHandler( DataSet DS)
     { ds = DS;
       
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
   private float[] getQunitVect( int GroupIndex)
     {if( this.GroupIndex == GroupIndex)
        return Q;
      Data D = ds.getData_entry( GroupIndex);
      if( D == null)
        return null;
       Q = new float[3];

      DetPosAttribute DPa = (DetPosAttribute)D.getAttribute(Attribute.DETECTOR_POS);
      if( DPa == null) return null;
      DetectorPosition DP = DPa.getDetectorPosition();
      float[] xyz= DP.getCartesianCoords();
      float L = (float)java.lang.Math.sqrt( xyz[0]*xyz[0]+xyz[1]*xyz[1]+xyz[2]*xyz[2]);
      xyz[0] = xyz[0]/L -1; xyz[1] = xyz[1]/L; xyz[2] = xyz[2]/L; 
      L = (float)java.lang.Math.sqrt( xyz[0]*xyz[0]+xyz[1]*xyz[1]+xyz[2]*xyz[2]);
      Q[0]= xyz[0]/L;Q[1]= xyz[1]/L;Q[2]= xyz[2]/L;
      return Q;
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
           return D.getX_scale().getX(xIndex)*Q[0];
          } 

     /** Returns the xIndex that has the given axis value = Value for  
     *  this Group
     */
      public int  getXindex( int GroupIndex, float Value)
        { float[] Q = getQunitVect( GroupIndex);
          if( (Value ==0) ||(Q[0]==0))
            return -1;
           Data D = ds.getData_entry( GroupIndex);
          if( D == null)
              return -1;
           int i = D.getX_scale().getI(Value/Q[0]);
           if( i >= D.getX_scale().getNum_x())
             return -1;
           if( D.isHistogram()) i--;
           return i;
         }
       public float getMaxAxisValue(int GroupIndex)
        {float[] Q = getQunitVect( GroupIndex);
         Data D = ds.getData_entry( GroupIndex);
         XScale xscl =D.getX_scale();
         if( Q[0] >=0)
            return xscl.getEnd_x() *Q[0];
         else
            return xscl.getStart_x()*Q[0];
        }
      public float getMinAxisValue(int GroupIndex)
        {float[] Q = getQunitVect( GroupIndex);
         Data D = ds.getData_entry( GroupIndex);
         XScale xscl =D.getX_scale();
         if( Q[0] >=0)
             return xscl.getStart_x() *Q[0];
         else
            return xscl.getEnd_x()*Q[0];   
        
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
           return D.getX_scale().getX(xIndex)*Q[1];
          } 

     /** Returns the xIndex that has the given axis value = Value for  
     *  this Group
     */
      public int  getXindex( int GroupIndex, float Value)
        { float[] Q = getQunitVect( GroupIndex);
          if( (Value ==0) || (Q[1] == 0))
            return -1;
           Data D = ds.getData_entry( GroupIndex);
          if( D == null)
              return -1;
           int i = D.getX_scale().getI(Value/Q[1]);
           if( i >= D.getX_scale().getNum_x())
             return -1;
           if( D.isHistogram()) i--;
           return i;
         }
       public float getMaxAxisValue(int GroupIndex)
        {float[] Q = getQunitVect( GroupIndex);
         Data D = ds.getData_entry( GroupIndex);
         XScale xscl =D.getX_scale();
         if( Q[1] >=0)
            return xscl.getEnd_x() *Q[1];
         else
            return xscl.getStart_x()*Q[1];
        }
      public float getMinAxisValue(int GroupIndex)
        {float[] Q = getQunitVect( GroupIndex);
         Data D = ds.getData_entry( GroupIndex);
         XScale xscl =D.getX_scale();
         if( Q[1] >=0)
             return xscl.getStart_x() *Q[1];
         else
            return xscl.getEnd_x()*Q[1];   
        
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
           return D.getX_scale().getX(xIndex)*Q[2];
          } 

     /** Returns the xIndex that has the given axis value = Value for  
     *  this Group
     */
      public int  getXindex( int GroupIndex, float Value)
        { float[] Q = getQunitVect( GroupIndex);
          if( Value ==0)
            return -1;
           Data D = ds.getData_entry( GroupIndex);
           if( D == null)
              return -1;
           int i = D.getX_scale().getI(Value/Q[2]);
           if( i >= D.getX_scale().getNum_x())
             return -1;
           if( D.isHistogram() &&( i>0)) i--;
           return i;
         }
       public float getMaxAxisValue(int GroupIndex)
        {float[] Q = getQunitVect( GroupIndex);
         Data D = ds.getData_entry( GroupIndex);
         XScale xscl =D.getX_scale();
         if( Q[2] >=0)
            return xscl.getEnd_x() *Q[2];
         else
            return xscl.getStart_x()*Q[2];
        }
      public float getMinAxisValue(int GroupIndex)
        {float[] Q = getQunitVect( GroupIndex);
         Data D = ds.getData_entry( GroupIndex);
         XScale xscl =D.getX_scale();
         if( Q[2] >=0)
             return xscl.getStart_x() *Q[2];
         else
            return xscl.getEnd_x()*Q[2];   
        
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
     DataSetTools.operator.DataSet.DataSetOperator op = ds.getOperator( "Convert to Q");
     Object O = op.getResult();
     if( O instanceof DataSet)
         ds = (DataSet)O;
     else
      {System.out.println( O);
       System.exit(0);
      }
    QxQyQzAxesHandler Qax = new QxQyQzAxesHandler( ds );
    IAxisHandler Ax3 = Qax.getQzAxis();
    for( int i=0;i<ds.getNum_entries(); i++)
      System.out.println( "Min,Max="+Ax3.getMinAxisValue( i)+","+
          Ax3.getMaxAxisValue( i));
   


    }
  }
