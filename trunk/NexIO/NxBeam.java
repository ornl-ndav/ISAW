package NexIO;

import DataSetTools.dataset.*;


public class NxBeam
{
    String errormessage;
    public NxBeam()
    {errormessage ="";}
    public String getErrorMessage()
     { return errormessage;
     }
    public boolean processDS( NxNode node,  DataSet DS)
    {errormessage= "Improper inputs to NxBeam";
    if( node == null)
	return true;
    if( DS == null)
       return true;
    if( !node.getNodeClass().equals("NXbeam"))
       return true;
    errormessage ="";
     NxData_Gen ng = new NxData_Gen();
    NXData_util nu = new NXData_util();
    NxNode n1= node.getChildNode( "incident_energy");
    if( n1 != null)
      {Object O = n1.getNodeValue();
       float x[];
       float f;
       x = nu.Arrayfloatconvert( O );
       if( x!= null)
         if( x.length > 0)
	     f = x[0];
         else
            f= Float.NaN;
       else f = Float.NaN;
      if( f == Float.NaN)
       {Float F = ng.cnvertoFloat( O );
        if( F != null)
         f = F.floatValue();
       }
      if( f != Float.NaN)
       { FloatAttribute FA = new FloatAttribute( Attribute.NOMINAL_ENERGY_IN,
                             (f));
       
         DS.setAttribute( FA );
          FA = new FloatAttribute( Attribute.ENERGY_IN, (f));
        
         DS.setAttribute( FA );
       }
      }      

//  energy out
  n1= node.getChildNode( "final_energy");
    if( n1 != null)
      {Object O = n1.getNodeValue();
       float x[];
       float f;
       x = nu.Arrayfloatconvert( O );
       if( x!= null)
         if( x.length > 0)
	     f = x[0];
         else
            f= Float.NaN;
       else f = Float.NaN;
      if( f == Float.NaN)
       {Float F = ng.cnvertoFloat( O );
        if( F != null)
         f = F.floatValue();
       }
      if( f != Float.NaN)
       { FloatAttribute FA = new FloatAttribute( Attribute.ENERGY_OUT,
                 (f));
        
         DS.setAttribute( FA );
       }
      }    
   return false;  
    }//processDS



}
