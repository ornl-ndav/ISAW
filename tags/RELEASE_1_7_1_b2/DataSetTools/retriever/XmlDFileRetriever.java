/*
 * File:  XmlDFileRetriever.java
 *
 * Copyright (C) 2001, Dennis Mikkelson
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
 * Contact : Dennis Mikkelson <mikkelsond@uwstout.edu>
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
 *  $Log$
 *  Revision 1.7  2004/06/24 19:10:22  rmikk
 *  Fixed errors in calculating and reporting the number of data sets
 *
 *  Revision 1.6  2004/03/15 19:33:57  dennis
 *  Removed unused imports after factoring out view components,
 *  math and utilities.
 *
 *  Revision 1.5  2004/03/15 03:28:43  dennis
 *  Moved view components, math and utils to new source tree
 *  gov.anl.ipns.*
 *
 *  Revision 1.4  2003/03/03 16:54:29  pfpeterson
 *  Changed SharedData.status_pane.add(String) to SharedData.addmsg(String)
 *
 *  Revision 1.3  2002/12/10 22:13:53  pfpeterson
 *  Fixed javadoc
 *
 *  Revision 1.2  2002/11/27 23:23:16  pfpeterson
 *  standardized header
 *
 *  Revision 1.1  2002/06/18 19:45:23  rmikk
 *  Initial Checkin
 *
 */
package DataSetTools.retriever;

import java.io.*;
import DataSetTools.dataset.*;
import java.util.*;
import java.util.zip.*;

/**
*  Contains methods to retrieve files written in either the ISAW xml format
*  or its zipped form( extension should be "zip")
*/
public class XmlDFileRetriever extends Retriever
 {boolean zip;
  ZipFile zf = null;
  //DSPositioner posn;
  InputStream fi=null;
  int nDataSets = 0;
  Vector V;// Contains the entry byte offsets to the start of the data sets
  boolean errFile = false;
  public XmlDFileRetriever( String data_source_name )
   {super( data_source_name);
    zip = false;
    if(data_source_name == null)
      {errFile = true;
       DataSetTools.util.SharedData.addmsg("No data source");
       return;
      }
    if(data_source_name.length() <4)
      {errFile = true;
       DataSetTools.util.SharedData.addmsg("Improper filename extension");
       return;
      }
    if( data_source_name.substring( data_source_name.length()-3)
         .toUpperCase().equals("ZIP"))
      {zip = true;

       try{
         //FileInputStream f= new FileInputStream( data_source_name);
         zf = new ZipFile( data_source_name);
        
           }
       catch( Exception ss)
         { DataSetTools.util.SharedData.addmsg(
             "Exception="+ss.getClass()+":"+ss.getMessage());
           errFile= true;
          }
       return;
      }
    
    V= new Vector();
  // if extension xml
    FileInputStream f =null;
    
    try{
      f= new FileInputStream( data_source_name);
      fi = new BufferedInputStream( f );
       }
    catch( Exception xs)
      {errFile = true;
       return;
       }
    
    
    int nbytes =0;
    int nbytesStart=-1;
    int mode = 0;//outside ds block, 1 inside DS block
    boolean done = false;
    StringBuffer sb = new StringBuffer(10);
  
    try{
    while (!done)
     { int  cn = fi.read();
       if( cn < 0)
         { 
           return;
          }
       char c =(char)(cn);
       if( cn >= 0)
          nbytes++;
       if( sb.length() > 0)
        { sb.append( c);
         
          if(sb.toString().equals("<DataSet "))
            if( mode == 0)
             { mode =1;
               V.add( new Integer( nbytesStart-1));
               nDataSets++;  
               nbytesStart = -1;
               sb.setLength(0);
               
             }
            else
              { DataSetTools.util.SharedData.addmsg( 
                    "improper nesting of DataSet tags" );
                return;
              }
           else if( sb.toString().equals("</DataSet>"))
             {mode = 0;
               
               sb.setLength(0);
              nbytesStart= -1;
             }
           else if( c=='>')
             sb.setLength(0);
           else if( sb.length() >10)
             sb.setLength(0);
         
          // if( nbytes >100) done = true;
        }
       else if( c=='<')
         { sb.setLength(0);
           sb.append( c);
          nbytesStart = nbytes;
          }
     

      
     }
        }
      catch( Exception s)
        {  
           try{fi.close();}catch(Exception ss){}
        }
  
    }
 private String getTagAdv( InputStream stream, boolean endTag)
   {String Tag = xml_utils.getTag( stream );
    if( Tag == null)
      {xml_utils.setError( xml_utils.getErrorMessage());
       return null;
      }
    if( endTag)
      if( !xml_utils.skipAttributes( stream ))
        {xml_utils.setError( xml_utils.getErrorMessage());
         return null;
         }
    return Tag;
    }
 /**
 *  Returns the number of data sets in this file
 */
 public int numDataSets(){
    return V.size();
 }
 private int numDataSets1()
  { if( nDataSets >=0)
       return nDataSets;
    else
       return -1;// Found in constructor
   /*if(errFile)
     { 
       return 0;
     }
   if( zip)
     { nDataSets= zf.size()-1;//header stuff was an entry
       return nDataSets;
      }
   errFile = true;
   FileInputStream f=null;
   try{
     f = new FileInputStream( data_source_name);
       }
   catch( Exception s)
     {xml_utils.setError("Exceptions="+ s.getClass()+":"+s.getMessage());
      return 0;
     }
   
   String Tag = getTagAdv( f , false);
   if( Tag == null)
      {
       return 0;
       }

   if( Tag.equals("?xml"))
    Tag = getTagAdv( f, false);
   if( Tag == null)
     {
      return 0;
      }
   if( !Tag.equals("DataSetList"))
     {
       return 0;
      }  
   Vector VV= xml_utils.getNextAttribute( f);
   if( VV == null)
     {xml_utils.setError("E"+ xml_utils.getErrorMessage());
      return 0;
     }
   if( VV.size() < 2)
     {xml_utils.setError( "F"+xml_utils.getErrorMessage());
      return 0;
     }
   if( !VV.firstElement().equals("size"))
     {xml_utils.setError("G No Size Attribute on DataSetList tag");
      return 0;
     }      
   try{
     nDataSets = new Integer( (String)(VV.lastElement())).intValue();
     if( nDataSets != V.size())
       {xml_utils.setError("improper size to DataSetList tag");
        return 0;
       }
     errFile = false;
     return nDataSets;
      }
    catch( Exception s)
     { xml_utils.setError("value of DataSetList size attribute improper");
       return 0;
     }
  */
  }
 private boolean locate( InputStream stream, int data_set_num)
  {if( errFile) 
    return false;
   if( data_set_num < 0)
    return false;
   if( data_set_num >= nDataSets)
     return false;
   int n = ((Integer)(V.elementAt(data_set_num))).intValue();
   try{
   if( stream.skip(n) != (n))
    { xml_utils.setError( "Could not locate data set");
      errFile = true;
      stream.close();
      return false;
    }
      }
   catch( Exception s)
     {return xml_utils.setError( "Exception="+s.getClass()+":"+s.getMessage());
      }
   return true;
  }

 /**
 *  Returns the indicated data set in this file
 *@param data_set_num  the number of the data set that is returned <P>
 * 
 *@return  the indicated data set or null if not possible
 */
 public DataSet getDataSet(int data_set_num)
  { DataSet DD =null;
   
   if(!zip)
    {try{
      FileInputStream f = new FileInputStream( data_source_name);
      fi = new BufferedInputStream( f );
       }
     catch(Exception s)
       {xml_utils.setError("Exception="+s.getClass()+":"+
                 s.getMessage());
        return null;
        }
     if(!locate( fi, data_set_num ))
       return null;
     }
   else
     {ZipEntry ze=zf.getEntry("Entry"+data_set_num);
      if( ze == null)
       { DataSetTools.util.SharedData.addmsg("No zip entry "+data_set_num);
         return null;
       }
      try{
       fi= zf.getInputStream( ze);
         }
      catch( Exception s2)
         {DataSetTools.util.SharedData.addmsg("No zip entry "+data_set_num);
         return null;
         }
      }
   DD= new DataSet(true);
   DD.XMLread( fi);
   if(!zip)
   try{
     fi.close();
       }
   catch( Exception xx){}
   return DD;
   

 
  }
 
  /** Returns the type of the indicated data set
  * @param  data_set_num  the indicated data set
  * @return  The type of data set
  *
  * @see DataSetTools.retriever.Retriever#HISTOGRAM_DATA_SET  
  */
  public int getType(int data_set_num)
  {  
    if(!zip)
     {try{
       FileInputStream f = new FileInputStream( data_source_name);
       fi = new BufferedInputStream( f );
         }
      catch( Exception s)
       { return Retriever.INVALID_DATA_SET;
        }
      if(!locate( fi, data_set_num))
        return Retriever.INVALID_DATA_SET;
      }
    else
      {ZipEntry ze=zf.getEntry("Entry"+data_set_num);
      if( ze == null)
       { DataSetTools.util.SharedData.addmsg("No zip entry "+data_set_num);
         return Retriever.INVALID_DATA_SET;
       }
      try{
        fi= zf.getInputStream( ze);
         }
      catch( Exception s2)
         {DataSetTools.util.SharedData.addmsg("No zip entry "+data_set_num);
         return Retriever.INVALID_DATA_SET;
         }
      }
    String Tag= getTagAdv( fi, true);
    if( Tag == null)
      return Retriever.INVALID_DATA_SET;
    if(!Tag.equals("DataSet"))
      {xml_utils.setError("Improper tag.Should be DataSet");
       return Retriever.INVALID_DATA_SET;
      }
     Tag= getTagAdv( fi, true);
    if( Tag == null)
      return Retriever.INVALID_DATA_SET;
    if(!Tag.equals("AttributeList"))
      {xml_utils.setError("Improper tag.Should be DataSet");
       return Retriever.INVALID_DATA_SET;
      }
     AttributeList al = new AttributeList();
     if(!al.XMLread( fi))
       return Retriever.INVALID_DATA_SET;
     Object O =al.getAttributeValue( Attribute.INST_TYPE);
     if(!(O instanceof Integer))
       return Retriever.INVALID_DATA_SET;
     return ((Integer)O).intValue();


      
  

   }


/*
 abstract class DSPositioner
  {
    public abstract boolean PositionStream( InputStream is );
   }
 class zipFilePositioner extends DSPositioner
   {String zipEntryName;
    public zipFilePositioner(String zipEntryName)
     {this.zipEntryName = zipEntryName;
      }
    public  boolean PositionStream( InputStream is, int i )
     { 
      }
   }
  
  class xmlFilePositioner extends DSPositioner
    { int nbytes;
      public xmlFilePositioner ( int nbytes)
        {this.nbytes = nbytes;
         }
      public  boolean PositionStream( InputStream is, int i )
       {try
        {
         nbytes =((Integer) V.elementAt(i)).intValue();
         long n =is.skip(nbytes);
         if( n!= nbytes)
          {DataSetTools.util.SharedData.status_pane.add( "could not locate DataSet");
           return false;
           }
      
          is.close();
          return true;
        }
        catch( Exception s)
        {return xml_utils.setError("Exception="+s.getClass()+":"+s.getMessage());
        }
       }
     }
*/
 /** Test program for this module. It currently reads the file xx.zip.
 */
 public static void main( String args[])
   { XmlDFileRetriever xmlr = new XmlDFileRetriever("xx.zip");
     
     System.out.println("#data sets="+xmlr.numDataSets());
     for( int i=0; i< xmlr.numDataSets(); i++)
       {
        DataSet D = xmlr.getDataSet( i);
        DataSetTools.dataset.DataSet_IO.SaveDataSet( D, "abc"+i+".isd");
        }


   }

 }
