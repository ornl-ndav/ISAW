 /*
  * File:  OperatorList.java
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
  * Modified:
  *
  *  $Log$
  *  Revision 1.5  2004/01/22 02:19:05  bouzekc
  *  Removed unused imports and local variables.
  *
  *  Revision 1.4  2003/10/15 23:50:12  dennis
  *  Fixed javadocs to build cleanly with jdk 1.4.2
  *
  *  Revision 1.3  2003/06/18 20:35:35  pfpeterson
  *  Changed calls for NxNodeUtils.Showw(Object) to
  *  DataSetTools.util.StringUtil.toString(Object)
  *
  *  Revision 1.2  2002/11/27 23:14:06  pfpeterson
  *  standardized header
  *
  *  Revision 1.1  2002/06/14 21:15:06  rmikk
  *  Implements IXmlIO interface. Used to handle the list
  *  Data Lists
  *
  */
  package DataSetTools.dataset;

import DataSetTools.util.StringUtil;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
* A utility class that deals with the list of Data in a DataSet
*/
public class DataSetList  implements IXmlIO
{ DataSet ds;

  /**
  * Constructor for the DataList
  *
  * @param ds  the data set with the list of Data
  */
  public DataSetList( DataSet ds)
  { this.ds = ds;
  }

  /**
  * Implements the IXmlIO interface so a list of Data can write itself
  *
  * @param  stream  the OutputStream to which the data is written
  * @param  mode   either IXmlIO.BASE64 or IXmlIO.NORMAL. This indicates how
  *                the spectra x, y and error values are written
  *
  * @return true if successful and false otherwise<P>
  *
  * NOTE: This routine writes the begin and end tags( DataList)
  */
  
  public boolean XMLwrite( OutputStream stream, int mode)
  { StringBuffer sb= new StringBuffer( 1500);
      
    sb.append("<DataList>\n");
    Vector V = new Vector();
    int entry = 0;
    boolean done = ds.getNum_entries()<1;
    try
    {
      while( !done)
      { Data db = ds.getData_entry( entry);
        
        entry++;
        XScale xscl= db.getX_scale();
        V.add(xscl);
        sb.append("<DataSubset>\n<VariableXScale>");
        float[] xs= xscl.getXs();
       
        byte[]b=xml_utils.convertToB64(xs);
        if( b == null)
          return xml_utils.setError( "could not convert float list");
        stream.write( sb.toString().getBytes());
        stream.write( b);
        stream.write("</VariableXScale>\n".getBytes());
        sb.delete(0, sb.length());
        if(!db.XMLwrite( stream, mode))
          return false;
        int nentry = entry;
        boolean xdone = !(nentry< ds.getNum_entries());
       
        while( !xdone)
        { boolean more=false;
            
          for( int i =nentry ; (i< ds.getNum_entries()) && (!more ); i++)
          { db = ds.getData_entry( i);
               
            XScale xscl2= db.getX_scale();
            if( xscl2 == xscl)
            { more = true;
              nentry = i;
              if( i== entry)
                entry++;
            }
               
          }
            
            
          xdone = !more;
          if(!xdone)
            if(!db.XMLwrite( stream, mode))
              return false;
            nentry++;
        }
        
            
        done = !(entry < ds.getNum_entries());
        if(!done)
        { boolean more = false;
          while(!more)
          { db = ds.getData_entry(entry);
            xscl= db.getX_scale();
            more = true;
            for( int i = 0; (i<V.size()) && !more;i++)
              if( V.elementAt(i).equals( xscl))
              { more = false;
                   
              }
            if(!more)
              entry++;
            if( entry >= ds.getNum_entries())
              more = false;

          }
      
             
           
          
        }

        done = !(entry < ds.getNum_entries());
        stream.write("</DataSubset>\n".getBytes());
      }//while !done
      stream.write("</DataList>\n".getBytes());
    }
    catch(Exception ss)
    { return xml_utils.setError( "xException="+ss.getClass()+"::"+
                  ss.getMessage());
    }
    return true;
  }


 /**
  * Implements the IXmlIO interface so a list of Data can read itself
  *
  * @param  stream  the IntputStream to which the data is written
  *
  * @return true if successful and false otherwise<P>
  *
  * NOTE: This routine assumes the begin tag along with all attributes
  * have been read.  It also reads the end tag( DataList)
  */
  public boolean XMLread( InputStream stream )
  { float[] xvals= null;
    String Tag = xml_utils.getTag( stream );
    if(Tag == null)
      return xml_utils.setError( xml_utils.getErrorMessage());
    if(!xml_utils.skipAttributes( stream ))
      return xml_utils.setError( xml_utils.getErrorMessage());
      
    while( Tag.equals("DataSubset"))
    { Tag = xml_utils.getTag( stream );
        
      if(Tag == null)
        return xml_utils.setError( xml_utils.getErrorMessage());
      if(!xml_utils.skipAttributes( stream ))
        return xml_utils.setError( xml_utils.getErrorMessage());
      if(!Tag.equals("VariableXScale"))
        return xml_utils.setError("Improper tag name DataSubset");
      String v= xml_utils.getValue( stream);
      if( v == null)
        return xml_utils.setError( xml_utils.getErrorMessage());
        
      byte[]b = v.getBytes();
      xvals = xml_utils.convertB64Tofloat(b);
      if( xvals == null)
        return xml_utils.setError( "Improper xvals");
      XScale xscl = new VariableXScale( xvals); //fix
        
      Tag = xml_utils.getEndTag( stream );
      if(Tag == null)
        return xml_utils.setError( xml_utils.getErrorMessage());
      if( !Tag.equals("/VariableXScale"))
        return xml_utils.setError("Improper nesting of VariableXScale:"+
                Tag);
      if(!xml_utils.skipAttributes( stream ))
        return xml_utils.setError( xml_utils.getErrorMessage());       
 
        
      Tag = xml_utils.getTag( stream );//Tag for data set type
      if(Tag == null)
        return xml_utils.setError( xml_utils.getErrorMessage());
        
      while( ! Tag.equals("/DataSubset"))  
      { int Id =-1;
        int ysize=-1;
        for( int ii=0; (ii<2);ii++)
        { Vector V = xml_utils.getNextAttribute( stream );
          if( V == null)
            return xml_utils.setError( xml_utils.getErrorMessage());
          if( V.size()<2)
            return xml_utils.setError("Not enough attributes in "+Tag+
                                      StringUtil.toString(V));
          try
          {
            if( V.firstElement().equals("ID"))
              Id= (new Integer((String)( V.lastElement()))).intValue() ;
            else if( V.firstElement().equals("ysize"))
              ysize=(new Integer((String)( V.lastElement()))).intValue() ;
              
          }
          catch( Exception ss)
          {//System.out.println("CCC");
          }
        }
           
        if((Id<0) ||(ysize <0))
          return xml_utils.setError("Not enough attributes in "+Tag+
               ","+Id+","+ysize);

        if(!xml_utils.skipAttributes( stream))
          return xml_utils.setError( xml_utils.getErrorMessage());
        Data D = null;
        try
        {
          Class  DD = Class.forName( "DataSetTools.dataset."+Tag);
           
          Class[] params = new Class[1];
          params[0]= xscl.getClass().getSuperclass();
          Constructor C = DD.getConstructor( params);
           
          Object[] args = new Object[1];
          args[0] = xscl;
          //args[1] = new Integer( Id);

          D = (Data) (C.newInstance(args));
        }
        catch(ClassNotFoundException s3)
        { return xml_utils.setError( "Data Class not found");
        }
        catch( Exception s4)
        { return xml_utils.setError( "Data could not be initted- "
                   +s4.getClass()+"::"+s4.getMessage());
        }           
            
        if(!D.XMLread( stream))
          return false; 
            
        ds.addData_entry( D );
            
        try
        {
          Object id= D.getAttributeValue( Attribute.GROUP_ID );
              
          int gid= ((Integer)id).intValue();
          D.setGroup_ID( gid );
        }
        catch( Exception s3)
        {}

           

        Tag = xml_utils.getTag( stream);
        if(Tag == null)
          return xml_utils.setError( xml_utils.getErrorMessage());
      }//while Tag !=/DataSubset
      if(!xml_utils.skipAttributes( stream ))
        return xml_utils.setError( xml_utils.getErrorMessage());
      Tag = xml_utils.getTag( stream);
        
      if(Tag == null)
        return xml_utils.setError( xml_utils.getErrorMessage());
      if(!xml_utils.skipAttributes( stream ))
        return xml_utils.setError( xml_utils.getErrorMessage());
    }//while tag not /DataList
       
      

    if( !Tag.equals("/DataList"))
      return xml_utils.setError( "Improper nesting of tags in DataList"+
            Tag);
    return true;
  }
    
   
}
