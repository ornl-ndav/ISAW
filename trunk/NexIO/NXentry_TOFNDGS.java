/*
 * File:  NXentry_TOFNDGS.java 
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
 * Revision 1.2  2001/07/17 14:58:20  rmikk
 * Added More attributes
 *
 * Revision 1.1  2001/07/05 21:45:10  rmikk
 * New Nexus datasource IO handlers
 *
*/
package NexIO ; 

import DataSetTools.dataset.* ;
import DataSetTools.operator.* ;
import java.util.* ;
import java.text.* ;

/** This Class process the NXentry class nodes in a NXNode datasource<P>
 *NOTE: The datasource should follow the Nexus standard
 */
public class NXentry_TOFNDGS implements NXentry
 { String errormessage ;
   NxNode node ; DataSet DS ;
   NxData nd ;
   NxMonitor nm ;

     /**
*@param  node  the datasource node used to retrieve information
*@param  DS    the DataSet( already existing ) to be built up
*/
 public NXentry_TOFNDGS( NxNode node, DataSet DS )
    {
     this.node = node ;
     this.DS = DS ;
     nd = new NXdata_Fields( "time_of_flight","phi","data" ) ;
     nm =  new NxMonitor() ;
     nm.setMonitorNum( 0 ) ;
     }
/** Returns any error or warning message or "" if none
*/
  public String getErrorMessage()
    {
     return errormessage ;
    }

/**  Sets the NXdata Handler for the NXentry handler
*@param  nd  a NXdata handler
*/
  public void setNxData( NxData nd )
    {
      this.nd = nd ;
    }
    
/** adds fields to the data set DS
* @param DS  the Data Set( already existing ) that is to be built
*@param index  A given NXentry has several data sets. The index parameter
* tells which one is to be retrieved
*<P>NOTE: The Monitors are retrieved first, then the Others next
*/  
public boolean processDS( DataSet DS, int index )
    {
    NxNode datanode ;
    boolean monitorDS, HistDS ;
    monitorDS = false ;
    HistDS = false ;
   NxNodeUtils nu = new NxNodeUtils();
    errormessage = "improper index" ;
    if( index < 0 ) 
      return false ;
    errormessage = "" ;
    
     boolean done = false ;
     int nchildren =  node.getNChildNodes() ;
     int ndatasets = 0 ;
     
     
     for( int i = 0 ; ( i<nchildren )&&( !done ) ; i++  )
      {datanode =  node.getChildNode( i ) ;
     
       if( datanode == null )
           {
            return false ;
           }
      

       if( datanode.getNodeClass().equals( "NXmonitor" ) )
        { if( ndatasets !=  index )
	    {
             ndatasets++  ; 
             done =  true ; 
            }
          else
           if( !nm.processDS( datanode , DS) )
	       {
               monitorDS = true ;
	       int nn = nm.getMonitorNum() + 1 ;
               nm.setMonitorNum( nn ) ;
              
               }
            else 
                {
                errormessage += ";" + nm.getErrorMessage() ;                 
                return true ;
                }           
         
         }//if( NXmonitor node
      }//for each node
   
     done =  false ;
     if( !monitorDS )
     for( int i = 0 ; ( i < nchildren )&&( !done ) ; i++ )
      {
       datanode =  node.getChildNode( i ) ;      
       if( datanode == null )
           {
            return false ;
           }
    
       if( datanode.getNodeClass().equals( "NXdata" ) )
        { if( ndatasets!= index )
            {
              ndatasets++ ;
             }
           else
           if( !nd.processDS( datanode,DS ) )
              {ndatasets++ ;
               HistDS = true ;
               done = true ;
               }
            else
             {errormessage = nd.getErrorMessage() ;            
              return true ;
             }           
          
         }//if( NXmonitor node
      }//for each node
     if( !( monitorDS || HistDS ) )
         {errormessage = "No more DataSets" ;
         return true ;
         }
    
    NxNode X =  node.getChildNode( "run_number" ) ;
    
    X =  node.getChildNode( "run_number" ) ;
    if( X!= null )
      {  Object val =  X.getNodeValue() ;
         int rn =  new NxData_Gen().cnvertoint( val ) ;
       
         DS.setAttribute( new IntAttribute( Attribute.RUN_NUM,  rn ) ); 
       
      }
   X =  node.getChildNode( "title" ) ;
    if( X!= null )
      {  Object val =  X.getNodeValue() ;
         String rn1 =  new NxData_Gen().cnvertoString( val ) ;
       
         DS.setAttribute( new StringAttribute( Attribute.RUN_TITLE,  rn1 ) ); 
       
      }

      X =  node.getChildNode( "duration" ) ;
    if( X!= null )
      {  Object val =  X.getNodeValue() ;
         Float ff =  new NxData_Gen().cnvertoFloat( val ) ;
         if( ff != null)
           {
            DS.setAttribute( new FloatAttribute( Attribute.NUMBER_OF_PULSES,  
                      ff.floatValue()*30.0f   ));
           } 
       
      }
    X = node.getChildNode( "end_time" ) ;
    if( X!= null )
      {  Object val = X.getNodeValue() ;
          String rn1,
                 rn = new NxData_Gen().cnvertoString( val ) ;
          if( rn!= null )
	      { Date D = nu.parse( rn);
                if( D == null)
                  {rn1 =rn;
                   rn = null;
                  }
                else
                  {GregorianCalendar C = new GregorianCalendar();
                   C.setTime( D);
                   int year= C.get(Calendar.YEAR);
                   if( year <500) C.set(Calendar.YEAR, year+1900);
                   rn1 = ""+C.get(Calendar.YEAR)+"-"+(1+C.get(Calendar.MONTH))+
                         "-"+C.get(Calendar.DAY_OF_MONTH);
                   rn = ""+C.get(Calendar.HOUR_OF_DAY)+":"+C.get(Calendar.MINUTE)+
                          ":"+C.get(Calendar.SECOND);
                  }
		  /*try{
                    Date DD = new SimpleDateFormat().parse( rn ) ;
                    rn1 = ""+ DD.getMonth() + "/" + DD.getDay() + 
                           "/" + DD.getYear() ;
                    rn = "" + DD.getHours() + ":" + DD.getMinutes() + 
                           ":" + DD.getSeconds() ;
	           }
                catch( ParseException s )
                   {rn1 = rn ; rn = null ;
                  }
                 */
                DS.setAttribute( new StringAttribute( Attribute.END_DATE, 
                                                       rn1  ) ) ;
              if( rn!= null )
                   DS.setAttribute( new StringAttribute( Attribute.END_TIME, 
                                                         rn  ) ) ;
               }
         
      }

    for( int i = 0 ; i< node.getNChildNodes() ; i++  )
     {datanode = node.getChildNode( i ) ;
      String C = datanode.getNodeClass() ;
      if( C.equals( "NXinstrument" ) )    
         {NxInstrument nx = new NxInstrument() ;
          if( !nx.processDS( datanode , DS ) )
             {}
           else
            errormessage += ":" + nx.getErrorMessage() ;
          }
      else  if( C.equals( "NXsample" ) )
        { NxSample ns =  new NxSample() ;
          if( !ns.processDS( datanode,DS ) )
            {
            }
          else
            errormessage += ";" + ns.getErrorMessage() ;
        }
      else if( C.equals( "NXbeam"))
        {NxBeam nb = new NxBeam();
        if( nb.processDS( datanode, DS))
          errormessage +=";"+nb.getErrorMessage();
	}
    
     }
    return false ;
    }//process DS
  
 }











