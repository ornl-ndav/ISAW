/*
 * File:  NxInstrument.java 
 *             
 * Copyright (C) 2001, Ruth Mikkelson
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
 *           Menomonie, WI. 54751
 *           USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.3  2002/02/26 15:44:46  rmikk
 * This module can now be used to load in Monitor data that has been inadvertently labeled NXdata. It does not have two axes
 *
 * Revision 1.2  2001/07/24 20:09:34  rmikk
 * Added several other attributes.
 * Incorporated an equals to determine if two separate pieces
 * of data are really links of each other
 *
 * Revision 1.1  2001/07/05 21:45:10  rmikk
 * New Nexus datasource IO handlers
 *
 */
package NexIO;
import DataSetTools.dataset.*;
import NexIO.*;
import java.lang.reflect.*;
/** This class is used to process the NXinstrument information in a Nexus
*data source
*/
public class NxInstrument
{String errormessage;
 public NxInstrument()
   {errormessage = "";
   }

/**Returns error and/or warning messages or "" if none
*/
 public String getErrorMessage()
   {
     return errormessage;
   }

 public NxNode matchNode( NxNode instrNode, String ax1Link, String ax2Link)
  {NxData_Gen ng = new NxData_Gen();
  //System.out.print("in match");
   int ax1,ax2;
   NxNode nDef= null;
   ax1=ax2=0;  //undefined-0; true-1;false-(-1)
   errormessage =" Improper inputs to matchNode";
   
   if( instrNode == null)
    return null;
   if( (ax1Link == null))
    return null;
   if( !instrNode.getNodeClass().equals("NXinstrument" ))
     return null;
  
   errormessage = "";
   //System.out.print( "n instr children="+instrNode.getNChildNodes());
   for( int i = 0; i< instrNode.getNChildNodes(); i++)
    {NxNode nx = instrNode.getChildNode( i );
   // System.out.print("inst child"+i+nx.getNodeName()+"::");
     if( nx == null)
      errormessage +=";improper Instr Child"+i;
     else if( nx.getNodeClass().equals("NXdetector"))
       { ax1=ax2=0;
        for( int j = 0; (j < nx.getNChildNodes()) &&(ax1 >=0)
                            &&(ax2 >= 0); j++)
           {NxNode n1 = nx.getChildNode( j );
            //System.out.print("Det Child"+n1.getNodeName()+"::");
            if( n1 == null)
               errormessage +="improper Det Child"+j;
            else
              {Object X = n1.getAttrValue( "axis");
               if( X != null)
                 {int axnum =ng.cnvertoint(X);
                 // System.out.print("X"+i+","+axnum);
                  if( ng.getErrorMessage()!="")
                      System.out.println("ERROR ="+
                               ng.getErrorMessage());
                  if( ng.getErrorMessage() == "")
                   {if(axnum == 1)
                      if( n1.equals( ax1Link))
                        ax1 = 1;
                      else
                        ax1=-1;
                    else if( axnum == 2)
                       if(ax2Link==null)
                          ax2=-1;
                       else if( n1.equals( ax2Link))
                         ax2 = 1;
                       else
                         ax2 = -1;
                   }//if ng.error ==""
                  }//if X!=null
               }//else n1 ==null
            }//for j
           //System.out.print("Y"+ax1+","+ax2);
           if( (ax1 >0) &&(ax2) > 0)
                 return nx;
           if( (ax2Link==null)&&(ax1> 0))
                 return nx;
           if( nDef == null) nDef= nx;
           }//else if child a detector node
       }//for i
    return nDef;
    }//matchNode

   
 
 /** Fills out an existing DataSet with information from the NXinstrument
   * section of a Nexus datasource
  *@param node  the current node positioned to an NXinstrument part of a datasource
  *@param  DS  the existing DataSet that is to be filled out
  *@return  error status: true if there is an error otherwise false
  */
 public boolean processDS( NxNode node ,  DataSet DS )
  {
    errormessage = "Improper inputs NxInstrument";
  
   if( node ==  null ) 
        return true;
   if( DS == null ) 
        return true;
   if( !node.getNodeClass().equals( "NXinstrument" ) )
          return true;
   errormessage = "";
   NxNode X = node.getChildNode( "name" );
   if( X!= null )
     {Object r = X.getNodeValue();
      String S = new NxData_Gen().cnvertoString( r );
     if( S!= null ) 
        DS.setAttribute( new StringAttribute( Attribute.INST_NAME , S ) );
     }
  //NXdetector stuff done in NXdata
     for( int i = 0 ; i < node.getNChildNodes() ; i++ )
     {NxNode tnode = node.getChildNode( i );
      if( tnode.getNodeClass().equals( "NXsource" ) )
	{NxNode tnode1 = tnode.getChildNode("distance");
         if( tnode1 == null)
           return false;
         Object O = tnode1.getNodeValue();
         if( O != null)if( O instanceof float[])
           if( Array.getLength( O ) == 1)
          { float f = ((float[])O)[0];
           
              DS.setAttribute( new FloatAttribute( Attribute.INITIAL_PATH,
                                                     f));
          }
         
        }
     }
   
  
  if( errormessage.length() > 0 )
    return true;
  return false;
  }  
}
