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
 *           Menomonie, WI 54751, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * Modified:
 *
 * $Log$
 * Revision 1.10  2003/10/15 03:05:46  bouzekc
 * Fixed javadoc errors.
 *
 * Revision 1.9  2002/11/27 23:28:17  pfpeterson
 * standardized header
 *
 * Revision 1.8  2002/11/20 16:14:41  pfpeterson
 * reformating
 *
 * Revision 1.7  2002/07/29 18:51:32  rmikk
 * Initial path now appears in all data blocks if it is in the
 *   data set.
 *
 * Revision 1.6  2002/06/19 15:05:52  rmikk
 * Trimmed a string so that the run title no longer has a null
 * character at the end.
 * Fixed code spacing and alignment
 *
 * Revision 1.5  2002/04/01 20:22:44  rmikk
 * Added the NxNode of the first NXdata of a histogram or monitor as an argument to the constructor.  This NxNode is used as an argument in a NxData.processDS.
 *
 * Revision 1.4  2002/02/26 15:40:42  rmikk
 * Added a debug field
 * Added a timeField field to put into the TimeField attribute.  All NXdata are
 *    merged.  To unmerge, extract with the TimefieldType  attribute
 * The code now allows for fixed monitor names and arbitrary monitor names
 * Some error messages now appear on the status pane
 *
 */
package NexIO;


import DataSetTools.dataset.*;
import DataSetTools.operator.*;
import java.util.*;
import java.text.*;


/** 
 * This Class process the NXentry class nodes in a NXNode datasource.
 * NOTE: The datasource should follow the Nexus standard
 */
public class NXentry_TOFNDGS implements NXentry{
  String errormessage;
  NxNode node, NxData;
  DataSet DS;
  NxData nd;
  NxMonitor nm;
  String monitorNames[];
  boolean debug = false;
  boolean unknown = true;

  /**
   * @param node the datasource node used to retrieve information
   * @param DS the DataSet( already existing ) to be built up
   * @param NxData the node that is a NXmonitor or NXdata node
   */
  public NXentry_TOFNDGS( NxNode node, DataSet DS, NxNode NxData ){
    this.node = node;
    this.DS = DS;
    this.NxData = NxData;
    nd = new NXdata_Fields( "time_of_flight", "phi", "data" );
    nm = new NxMonitor();
    nm.setMonitorNum( 0 );
    monitorNames = new String[2];
    monitorNames[0] = null;
    monitorNames[1] = null;
  }


  /**
   * Returns any error or warning message or "" if none
   */
  public String getErrorMessage(){
    return errormessage;
  }

  /**
   * Sets the NXdata Handler for the NXentry handler
   * @param nd a NXdata handler
   */
  public void setNxData( NxData nd ){
    if( nd instanceof NxData_Gen )
      unknown = true;
    else
      unknown = false;
    this.nd = nd;
  }


  /**
   * Adds fields to the data set DS. NOTE: The Monitors are retrieved
   * and merged, the other NXdata are merged.
   *
   * @param DS the Data Set( already existing ) that is to be built.
   * @param NxData DOCUMENT ME!
   */
  public boolean processDS( DataSet DS, NxNode NxData ){
    NxNode datanode, instrNode;
    NxData_Gen util = new NxData_Gen();
    boolean monitorDS, HistDS;
    
    monitorDS = false;
    HistDS = false;
    NxNodeUtils nu = new NxNodeUtils();
    
    if( monitorNames == null )
      unknown = false;
    else if( monitorNames.length < 1 )
      unknown = false;
    else if( monitorNames[0] == null )
      unknown = false;
    else if( !( monitorNames[0].equals( "monitor1" ) ) )
      unknown = false;
    
    errormessage = "improper index";
    if( NxData == null )
      return false;
    errormessage = "";
    
    boolean monitor = false;
    
    if( NxData.getNodeClass().equals( "NXmonitor" ) )
      monitor = true;
    String label = null;
    
    if( !monitor ){
      NxNode dat = NxData.getChildNode( "data" );
      
      errormessage += ";No data in an NXdata";
      if( dat == null )
        return false;
      errormessage = "";
      label = util.cnvertoString( dat.getAttrValue( "label" ) );
      
    }

    int nchildren = node.getNChildNodes();
    
    //Find Instrument  node
    instrNode = null;
    boolean done = false;
    
    for( int i = 0; ( i < nchildren ) && ( !done ); i++ ){
      datanode = node.getChildNode( i );
      if( datanode == null ){
        return false;
      }

      if( datanode.getNodeClass().equals( "NXinstrument" ) ){
        instrNode = datanode;
        done = true;
      }
    }
    int ndatasets = 0;
    
    done = false;
    int timeFieldNum = 0;
    
    for( int i = 0; ( i < nchildren ) & ( !done ); i++ ){
      datanode = node.getChildNode( i );

      if( datanode == null ){
        return false;
      }

      if( ( datanode.getNodeClass().equals( "NXmonitor" ) ) ){
        if( monitor ){
          if( !nm.processDS( datanode, DS ) ){
            int nn = nm.getMonitorNum() + 1;
            
            nm.setMonitorNum( nn );
            
          }
          
        }

      }else if( ( datanode.getNodeClass().equals( "NXdata" ) ) && !monitor ){
        NxNode dat = datanode.getChildNode( "data" );
        
        errormessage += ";No data in an NXdata";
        
        if( dat == null )
          return false;
        errormessage = "";
        String S = util.cnvertoString( dat.getAttrValue( "label" ) );
        boolean process = false;
        
        if( ( S == null ) && ( label == null ) )
          if( datanode.getNodeName().equals( NxData.getNodeName() ) )
            process = true;
        if( S != null ) if( label != null ) if( S.equals( label ) )
          process = true;
        
        if( process ){
          if( S == null )
            done = true;
          (nd).setTimeFieldType( timeFieldNum );
          timeFieldNum++;
          if( !nd.processDS( datanode, instrNode, DS ) ){
            ndatasets++;
            if( debug )
              System.out.println( "NXentry:thru process data set at node " + i );
            
          }else{
            errormessage += ";" + nd.getErrorMessage();
            DataSetTools.util.SharedData.addmsg( "ERROR=" + errormessage );
            //return true ;
          }
          
        }//if process
        
      }
    }//for each node
    // only one Monitor so break up

    NxNode X = node.getChildNode( "run_number" );
    
    if( X != null ){
      Object val = X.getNodeValue();
      
      int rn = new NxData_Gen().cnvertoint( val );
      
      DS.setAttribute( new IntAttribute( Attribute.RUN_NUM, rn ) );
      
    }
    X = node.getChildNode( "title" );
    if( X != null ){
      Object val = X.getNodeValue();
      String rn1 = util.cnvertoString( val );
      
      rn1 = rn1.trim();
      DS.setAttribute( new StringAttribute( Attribute.RUN_TITLE, rn1 ) );
      
    }

    X = node.getChildNode( "duration" );
    if( X != null ){
      Object val = X.getNodeValue();
      Float ff = util.cnvertoFloat( val );
      
      if( ff != null ){
        DS.setAttribute( new FloatAttribute( Attribute.NUMBER_OF_PULSES,
                                             ff.floatValue() * 30.0f ) );
      }
      
    }
    X = node.getChildNode( "end_time" );
    if( X != null ){
      Object val = X.getNodeValue();
      String rn1,
        rn = util.cnvertoString( val );
      
      if( rn != null ){
        Date D = nu.parse( rn );
        
        if( D == null ){
          rn1 = rn;
          rn = null;
        }else{
          GregorianCalendar C = new GregorianCalendar();
          
          C.setTime( D );
          int year = C.get( Calendar.YEAR );
          
          if( year < 500 )
            C.set( Calendar.YEAR, year + 1900 );
          rn1 = ""+C.get(Calendar.YEAR)+"-"+(1+C.get(Calendar.MONTH))+
            "-" + C.get(Calendar.DAY_OF_MONTH);
          rn = ""+C.get(Calendar.HOUR_OF_DAY) + ":" + C.get(Calendar.MINUTE) +
            ":" + C.get(Calendar.SECOND);
        }
        
        DS.setAttribute( new StringAttribute( Attribute.END_DATE, rn1 ) );
        if( rn != null )
          DS.setAttribute( new StringAttribute( Attribute.END_TIME, rn ) );
      }

    }

    for( int i = 0; i < node.getNChildNodes(); i++ ){
      datanode = node.getChildNode( i );
      String C = datanode.getNodeClass();
      
      if( C.equals( "NXinstrument" ) ){
        NxInstrument nx = new NxInstrument();
        
        if( !monitorDS )
          if( !nx.processDS( datanode, DS ) ){
            // do nothing
          }else
            errormessage += ":" + nx.getErrorMessage();
      }else  if( C.equals( "NXsample" ) ){
        NxSample ns = new NxSample();
        
        if( !ns.processDS( datanode, DS ) ){
          // do nothing
        }else
          errormessage += ";" + ns.getErrorMessage();
      }else if( C.equals( "NXbeam" ) ){
        NxBeam nb = new NxBeam();
        
        if( !monitorDS )
          if( nb.processDS( datanode, DS ) )
            errormessage += ";" + nb.getErrorMessage();
      }

    }
    Object X1 = DS.getAttributeValue( Attribute.RUN_NUM );
    int run_num = -1;
    
    if( X1 != null )
      if( X1 instanceof Integer )
        run_num = ( ( Integer )X1 ).intValue();
    int npulses = -1;

    X1 = DS.getAttributeValue( Attribute.NUMBER_OF_PULSES );
    if( X1 instanceof Number )
      npulses = ( ( Number )X1 ).intValue();
    
    float initial_path = -1;
    boolean  initialPathSet = false;
    X1 = DS.getAttributeValue( Attribute.INITIAL_PATH );
    if( X1 != null ) if( X1 instanceof Number ){
      initial_path = ( ( Number )X1 ).floatValue();
      initialPathSet = true  ;
    }

    for( int i = 0; i < DS.getNum_entries(); i++ ){
      Data DB = DS.getData_entry( i );

      if( run_num >= 0 )
        DB.setAttribute( new IntAttribute( Attribute.RUN_NUM, run_num ) );
      if( npulses >= 0 )
        DB.setAttribute(new IntAttribute(Attribute.NUMBER_OF_PULSES, npulses));
      if( initialPathSet)
        DB.setAttribute(new FloatAttribute(
                                         Attribute.INITIAL_PATH,initial_path));
    }
    return false;
  }//process DS
}
