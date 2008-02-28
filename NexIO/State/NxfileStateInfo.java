
/*
 * File:  NxfileStateInfo.java
 *
 * Copyright (C) 2003, Ruth Mikkelson
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
 * Contact :  Ruth Mikkelson <mikkelsonr@uwstout.edu>
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
 * Revision 1.5  2007/08/26 23:56:34  rmikk
 * Added white space
 *
 * Revision 1.4  2006/11/14 16:40:38  rmikk
 * Tries the new Data parser for ISO dates
 *
 * Revision 1.3  2006/07/27 19:30:09  rmikk
 * Eliminated a javadoc warning
 *
 * Revision 1.2  2006/07/25 00:05:57  rmikk
 * Added code to update fields in a FixIt xml file in the same directory as
 * the NeXus file
 *
 * Revision 1.1  2003/11/16 21:44:26  rmikk
 * Initial Checkin
 *
 */

package NexIO.State;
import NexIO.*;
import NexIO.Util.*;
import java.util.*;
import javax.xml.parsers.*;
import java.io.*;
import org.w3c.dom.*;

/**
 *   This class is the root node for all the Params link list arguments.
 *   It contains information necessary to process a NeXus file
 */
public class NxfileStateInfo extends StateInfo{
   

   public String NexusVersion;

   public String HDFVersion;

   public Date Time;

   /**
    *  This contains the list of spectra ID's to be retrieved. If null all
    *  spectra( data blocks) will be retrieved.  The id's are the default ID's
    *  unless there is an int id field in the NXdetector.
    */
   public int[] Spectra;
   
   /**
    *  The Facility
    */
    public String facility;
    
    public String filename;
    public String InstrumentName;
    public NxNode  InstrumentNode;
  
    public Node xmlDoc;

   /**
    *     Constructor
    *     @param  NxfileNode  the NxNode containing information on the top node
    *                         of the NeXus file.
    */
   public NxfileStateInfo( NxNode NxfileNode , String filename, 
                                                        NxNode InstrSourceNode){
      
     NexusVersion =NexUtils.getStringAttributeValue( NxfileNode, "NeXus_version");
     HDFVersion = NexUtils.getStringAttributeValue( NxfileNode, "HDF_version");
     String time =NexUtils.getStringAttributeValue( NxfileNode, "file_time");
     
     long Tmill = ConvertDataTypes.parse_new( time );
     if( Tmill < 0 )
         Time = ConvertDataTypes.parse( time);
     else{
        GregorianCalendar GCal = new GregorianCalendar();
        GCal.setTimeInMillis( Tmill );
        Time = GCal.getTime();
     }
     
     Spectra = null;
     InstrumentNode = null;
        
     facility = GetFacility( NxfileNode, InstrSourceNode);
     this.filename = filename;
     //Get facility specific names.
     InstrumentName =null; 
     xmlDoc = GetFixItXML( filename);
     if( filename != null)
        if( filename.toUpperCase().indexOf("SCD")>=0)
           InstrumentName ="SCD";
     
   }


   /**
    *     Copy Constructor
    *     @param state the state to copy
    */
  public NxfileStateInfo( NxfileStateInfo state){
     this.NexusVersion = state.NexusVersion;
     this.HDFVersion =state.HDFVersion;
     this.Time = state.Time;
     this.Spectra = state.Spectra;
  }
   
  
 static  String[][] Names ={  {"LANL","LANSCE","ALAMOS"},
                       {"ANL","ARGONNE","CHICAGO"},
                       {"SNS","OAKRIDGE","SPALLATION NEUTRON SOURCE"},
                       {"ISIS","RUTHERFORD","APPLETON"},
                       {"NIST","NCNR","GAITHERSBURG"}
                   };
                       
 
  /**
   *  Attempts to find the name of the facility from several sources. The return
   *  will be standardized to LANL, ANL, SNS, NIST, ISIS, 
   * @param topNode
   * @param InstrSourceNode
   * @return  The standardized name of the facility,LANL,ANL SNS, ISIS,
   *             or  NIST, if possible, otherwize a null is returned.
   */
  public static String GetFacility( NxNode  topNode, NxNode InstrSourceNode){
     
    int nattr = topNode.getNAttributes();
    for( int i=0; i < nattr; i++){
      NexIO.Attr attr =topNode.getAttribute( i );
     
      if(attr != null )if( attr.getItemValue() != null){
         for( int k =0; k< Names.length; k++){
            for(int m=0;  m < Names[k].length; m++){
              int kk ;
              if( attr.getItemValue() instanceof String){
                 kk = ((String)attr.getItemValue()).indexOf( Names[k][m]);
                 if( kk==0)
                    return Names[k][0];
                 if( kk >0) 
                   if( kk + Names[k][m].length() + 1 < ((String)attr.getItemValue()).length())
                      if( ", +-[{(/t/n/r%&$".indexOf( ((String)attr.getItemValue()).charAt(kk+Names[k][m].length()+1))>=0)
                         return Names[k][0];
                 kk = ((String)attr.getItemName()).indexOf( Names[k][m]);
                 if( kk >0) 
                   if( kk + Names[k][m].length() + 1 < ((String)attr.getItemName()).length())
                      if( ", +-[{(/t/n/r%&$".indexOf( ((String)attr.getItemName()).charAt(kk+Names[k][m].length()+1))>=0)
                         return Names[k][0];
              }
              
            }//for mm a possible facility other name
           
           
                       
          }//For one of the facilityes
         }//attr non null
            
      }// look at global attribute i
    //-----------------Now look atSource for the name---------------------
    NxNode NODE = null;
    if( InstrSourceNode != null){
   // for( int i=0;( i< InstrNode.getNChildNodes()) &&(NODE == null); i++){
       NxNode node= InstrSourceNode;
       if( node.getClass().equals("NXsource")){
          NODE=node;
          for( int k=0; k< Names.length; k++)
             for( int m=0; m< Names[k].length; m++){
                int kk = node.getNodeName().indexOf( Names[k][m]);
                if( kk==0)
                   return Names[k][0];
                if( kk > 0)if( kk +Names[k][m].length()+1 < node.getNodeName().length())
                   if( " ;,.[+-(])&%$\t\n".indexOf( node.getNodeName().charAt( kk + Names[k][m].length()+1))>=0)
                      return Names[k][0];
              }
         
        }
    
         
    }
       
    if( NODE != null)
       for( int i=0; i < NODE.getNChildNodes(); i++)
          if( NODE.getChildNode(i).getNodeName().equals("name")){
             String nm = NODE.getChildNode(i).getNodeName();
             String nm1 = NODE.getChildNode(i).getNodeValue().toString();
             for( int k=0; k< Names.length; k++)
                for( int m=0; m< Names[k].length; m++){
                   int kk = nm.indexOf( Names[k][m]);
                   if( kk==0)
                      return Names[k][0];
                   if( kk > 0)if( kk +Names[k][m].length()+1 < nm.length())
                      if( " ;,.[+-(])&%$\t\n".indexOf(nm.charAt( kk + Names[k][m].length()+1))>=0)
                         return Names[k][0];

                   kk = nm1.indexOf( Names[k][m]);
                   if( kk==0)
                      return Names[k][0];
                   if( kk > 0)if( kk +Names[k][m].length()+1 < nm1.length())
                      if( " ;,.[+-(])&%$\t\n".indexOf(nm1.charAt( kk + Names[k][m].length()+1))>=0)
                         return Names[k][0];
                 }  
             return null;
           }  
              
     return null;     
  }
  
  /**
   * Finds a file with a FixIt.  in its name in the same directory, reads the 
   *  first on in as an XML file and returns the XML Document Node structure
   *  
   * @param filename  A filename for NeXus data that may have a FixIt file int
   *                  the same directory to use
   * @return      The XML parsed structure or null if there was no FixIt file
   *                or the FixIt file was in the improper format.
   */
  public static Node GetFixItXML( String filename){
    if( filename == null )
       return null;


    Node N1 = null;
    String dir = (new File( filename )).getParent();
    
    File dirFile = new File( dir);
    File[] dirList = dirFile.listFiles();
    
    if( dirList != null)
    for( int i =0; i< dirList.length ; i++){
       
       File d = dirList[i];
       if( d.isFile()) 
          if( d.getAbsoluteFile().toString().indexOf( "FixIt." ) >=0 ){
          
            String fileName =null;
            try{
             
               fileName = d.getCanonicalPath();
               N1= DocumentBuilderFactory.newInstance().newDocumentBuilder().
                     parse( new java.io.FileInputStream(fileName)); 
              
            }catch(Exception s){
              
               String S ="Error in "+ fileName+":"+s.getMessage();
               if( s instanceof org.xml.sax.SAXParseException)
                  S +=" at line "+
                      ((org.xml.sax.SAXParseException)s).getLineNumber();
               (new javax.swing.JOptionPane()).showMessageDialog(null,S);
               return null;
            
         }
       }
    }
    return N1;   
 }

}
