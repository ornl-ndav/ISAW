/* 
 * File: getPeakInfo.java
 *  
 * Copyright (C) 2007     Ruth Mikkelson
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
 * Contact :  Ruth Mikkelson<Mikkelsonr@uwstout.edu>
 *            Department of Mathematics, Statistics and Computer Science
 *            Menomonie, WI 54751
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 * This work was supported by the National Science Foundation under
 * grant number DMR-0218882
 *
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2008/01/29 19:15:58  rmikk
 * Repllaced Peak by IPeak
 *
 * Revision 1.2  2007/08/23 21:05:03  dennis
 * Removed unused imports.
 *
 * Revision 1.1  2007/02/22 16:22:33  rmikk
 * Extracts various information from a peaks object
 *
 *
 */

package DataSetTools.operator.Generic.TOF_SCD;
import DataSetTools.operator.Generic.*;
import gov.anl.ipns.Parameters.*;

import gov.anl.ipns.Util.SpecialStrings.*;


/**
 * This class has been dynamically created using the Method2OperatorWizard
 * and usually should not be edited.
 */
public class getPeakInfo extends GenericOperator{


   /**
	* Constructor for the operator.  Calls the super class constructor.
    */
   public getPeakInfo(){
     super("getPeakInfo");
     }


   /**
    * Gives the user the command for the operator.
    *
	 * @return  The command for the operator, a String.
    */
   public String getCommand(){
      return "getPeakInfo";
   }


   /**
    * Sets the default parameters for the operator.
    */
   public void setDefaultParameters(){
      clearParametersVector();
      addParameter( new PlaceHolderPG("Peak",null));
      addParameter( new StringPG("Field","x"));
   }


   /**
    * Writes a string for the documentation of the operator provided by
    * the user.
    *
	 * @return  The documentation for the operator.
    */
   public String getDocumentation(){
      StringBuffer S = new StringBuffer();
      S.append("@overview    "); 
      S.append("Returns float information about a peak. Needed for scripts");
      S.append("@algorithm    "); 
      S.append("");
      S.append("@assumptions    "); 
      S.append("The field is either  \"x\",\"y\",\"z\"(time chan),\"h\",\"k\",\"l\",\"xcm\",\"ycm\",\"wl\",");
      S.append(" \"ipkobs\",\"inti\",\"sigi\",\"monct\",\"detA\",\"detD\",\"detA2\",");
      S.append(" \"L1\",\"chi\",\"Phi\",\"omega\",\"detnum\"");
      S.append("@param   ");
      S.append("The peak object");
      S.append("@param   ");
      S.append("The field");
      S.append("@return   ");
      S.append("The float value of the corresponding field or an error message");
      S.append("@error ");
      S.append("Null Peak");
      S.append(" Improper Field name");
      return S.toString();
   }


   /**
    * Returns a string array with the category the operator is in.
    *
    * @return  An array containing the category the operator is in.
    */
   public String[] getCategoryList(){
            return new String[]{
                     "HIDDENOPERATOR"
                     };
   }


   /**
    * Returns the result of the operator, otherwise and ErrorString.
    *
    * @return  The result of the operator, or an ErrorString.
    */
   public Object getResult(){
     

         IPeak pk = (IPeak)(getParameter(0).getValue());
         java.lang.String descriptor = getParameter(1).getValue().toString();
         

            if( pk == null )
               return new ErrorString( " the peak is null");
            if( descriptor == null )
               return new Float(Float.NaN );
            if( descriptor.compareTo( "detnum" )==0)
               return new Float(pk.detnum( ));
            if( descriptor.compareTo( "l" ) <= 0 )
               if( descriptor.compareTo( "h" ) <= 0 )
                  if( descriptor.compareTo( "detA2" ) <= 0 )
                     if( descriptor.equals( "chi" ) )
                        return new Float(pk.chi() );
                     else if( descriptor.equals( "detA" ) )
                        if( pk instanceof IPeak_IPNS_out)
                          return new Float(((IPeak_IPNS_out)pk).detA() );
                        else
                           return Float.NaN;
                     else if( descriptor.equals( "detA2" ) )
                        if( pk instanceof IPeak_IPNS_out)
                           return new Float(((IPeak_IPNS_out)pk).detA2() );
                         else
                            return Float.NaN;
                     else
                        return Float.NaN ;
                  else if( descriptor.equals( "detD" )) 
                           if( pk instanceof IPeak_IPNS_out)
                              return new Float(((IPeak_IPNS_out)pk).detD() );
                            else
                               return Float.NaN;
                  else if( descriptor.equals( "h" ) )
                     return new Float(pk.h() );
                  else
                     return Float.NaN ;

               else if( descriptor.compareTo( "ipkobs" ) <= 0 )
                  if( descriptor.equals( "inti" ) )
                     return new Float(pk.inti() );
                  else if( descriptor.equals( "ipkobs" ) )
                     return new Float(pk.ipkobs() );

                  else
                     return new Float(Float.NaN );
               else if( descriptor.equals( "k" ) )
                  return new Float(pk.k() );
               else if( descriptor.equals( "l" ) )
                  return new Float(pk.l() );
               else
                  return new Float(Float.NaN );
            else if( descriptor.compareTo( "wl" ) <= 0 )
               if( descriptor.compareTo( "phi" ) <= 0 )
                  if( descriptor.equals( "monct" ) )
                     return new Float(pk.monct() );
                  else if( descriptor.equals( "omega" ) )
                     return new Float(pk.omega() );
                  else if( descriptor.equals( "phi" ) )
                     return new Float(pk.phi() );
                  else
                     return new Float(Float.NaN );
               else if( descriptor.equals( "sigi" ) )
                  return new Float(pk.sigi() );
               else if( descriptor.equals( "wl" ) )
                  return new Float(pk.wl() );
               else
                  return new Float(Float.NaN );
            else if( descriptor.compareTo( "y" ) <= 0 )
               if( descriptor.compareTo( "xcm" ) <= 0 )
                  if( descriptor.equals( "x" ) )
                     return new Float(pk.x() );
                  else if( descriptor.equals( "xcm" ) )
                     return new Float(pk.xcm() );
                  else
                     return new Float(Float.NaN );
               else if( descriptor.equals( "y" ) )
                  return new Float(pk.y() );
               else
                  return new Float(Float.NaN );
            else if( descriptor.equals( "ycm" ) )
               return new Float(pk.ycm() );
            else if( descriptor.equals( "z" ) )
               return new Float(pk.z() );
            else if( descriptor.equals( "L1" ) )
               return new Float(pk.L1());
            return new ErrorString("Improper field name");


   }
}



