/*
 * File:  NXutil.java   
 *
 * Copyright (C) 2003, Peter F. Peterson
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
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 *
 * $Log$
 * Revision 1.2  2003/12/15 02:29:12  bouzekc
 * Removed unused imports.
 *
 * Revision 1.1  2003/05/06 21:58:42  pfpeterson
 * Added to CVS.
 *
 */
package NexIO;

import java.util.Enumeration;
import java.util.Hashtable;

import ncsa.hdf.hdflib.HDFConstants;
import neutron.nexus.AttributeEntry;
import neutron.nexus.NexusException;

/**
 * This is a class of utility functions for accessing information in a
 * nexus file.
 */
public class NXutil{
  private static final int CHAR  = HDFConstants.DFNT_CHAR;
  private static final int INT   = HDFConstants.DFNT_INT32;
  private static final int UINT  = HDFConstants.DFNT_UINT32;
  private static final int FLOAT = HDFConstants.DFNT_FLOAT32;

  /**
   * Like the name says, this is a utility class. Do not allow anyone
   * to instantiate this class.
   */
  private NXutil(){}

  /**
   * Get the list of attributes on the current level of the file
   * given.
   */
  public static Hashtable getAttrList(CNexusFile file) throws NexusException,
                                                          NullPointerException{
    // don't know what to do with a null file pointer
    if(file==null) throw new NullPointerException("null CNexusFile");

    // get the initial hashtable
    Hashtable table=file.attrdir();
    
    // collect the keys
    Enumeration keys=table.keys();

    // go through and replace the values with a string version
    String key=null;
    while(keys.hasMoreElements()){
      key=(String)keys.nextElement();
      table.put(key,getAttr(file,table,key));
    }

    return table;
  }

  /**
   * Method for converting the AttributeEntry objects into real
   * Atributes. The currently supported Attribute types are CHAR,
   * FLOAT, INT and UINT.
   */
  private static Object getAttr(CNexusFile file,Hashtable attrlist,String key)
                                   throws NexusException, NullPointerException{
    // null pointers are bad
    if(file==null) throw new NullPointerException("null CNexusFile");
    if(attrlist==null) throw new NullPointerException("null Hashtable");
    if(key==null) throw new NullPointerException("null key");

    // get the current value
    Object val=attrlist.get(key);

    // if it is a String the conversion is already done
    if(val instanceof String)  return val;
    if(val instanceof Float)   return val;
    if(val instanceof Integer) return val;

    // initialize some quick arrays to convert with
    int type=((AttributeEntry)val).type;
    int[] lentype={((AttributeEntry)val).length,type};
    if(type==CHAR) lentype[0]++;

    // create the value array
    Object data=null;
    if(type==CHAR)
      data=new byte[lentype[0]];
    else if(type==UINT || type==INT)
      data=getIntArray(lentype[0]);
    else if(type==FLOAT)
      data=getFloatArray(lentype[0]);
    else
      return "";

    // get the attribute value from the file
    file.getattr(key,data,lentype);

    // return a string version
    if(type==UINT || type==INT){
      if(((int[])data).length==1)
        return new Integer(((int[])data)[0]);
      else
        return data;
    }else if(type==FLOAT){
      if(((float[])data).length==1)
        return new Float(((float[])data)[0]);
      else
        return data;
    }else if(type==CHAR){
      return new String((byte[])data);
    }else{
      return new String("");
    }
  }

  public static Object getData(CNexusFile file, String key)
                                   throws NexusException, NullPointerException{
    // null pointers are bad
    if(file==null) throw new NullPointerException("null CNexusFile");
    if(key==null) throw new NullPointerException("null key");

    file.opendata(key);
    Object result=getData(file);
    file.closedata();

    return result;
  }

  /**
   * Get the data that is currently opened in the file. Can currently
   * deal with FLOAT, INT, and UINT.
   */
  public static Object getData(CNexusFile file) throws NexusException,
                                                          NullPointerException{
    // null pointers are bad
    if(file==null) throw new NullPointerException("null CNexusFile");

    // only allow data to up to three dimensions
    int[] dim=new int[4];
    int[] ranktype=new int[2];

    // determine the dimensions and type
    file.getinfo(dim,ranktype);
    int type=ranktype[1];

    // read the data
    Object data=null;
    if(type==UINT || type==INT){
      data=getIntArray(dim);
      file.getdata(data);
    }else if(type==FLOAT){
      data=getFloatArray(dim);
      file.getdata(data);
    }else if(type==CHAR){
      data=new byte[dim[0]];
      file.getdata(data);
      data=new String((byte[])data);
    }else{
      System.out.println("UNKOWN TYPE: "+type);
      data=null;
    }

    // cleanup
    dim=null;
    ranktype=null;

    // return the result
    return data;
  }

  /**
   * Basically does a malloc for an n-dimensional integer array.
   *
   * @param dim the length of the array in each dimension. Zero
   * elements are ignored.
   */
  public static Object getIntArray(int[] dim){
    // get the dimension of the array to allocate
    int size=dim.length;
    for(int i=size-1 ; i>0 ; i-- ){
      if(dim[i]<=0)
        size--;
      else
        break;
    }

    // allocate the array
    if(size==1)
      return new int[dim[0]];
    else if(size==2)
      return new int[dim[0]][dim[1]];
    else if(size==3)
      return new int[dim[0]][dim[1]][dim[2]];
    else if(size==4)
      return new int[dim[0]][dim[1]][dim[2]][dim[3]];
    else
      return null;
  }

  public static Object getFloatArray(int[] dim){
    // get the dimension of the array to allocate
    int size=dim.length;
    for(int i=size-1 ; i>0 ; i-- ){
      if(dim[i]<=0)
        size--;
      else
        break;
    }

    // allocate the array
    if(size==1)
      return new float[dim[0]];
    else if(size==2)
      return new float[dim[0]][dim[1]];
    else if(size==3)
      return new float[dim[0]][dim[1]][dim[2]];
    else if(size==4)
      return new float[dim[0]][dim[1]][dim[2]][dim[3]];
    else
      return null;
  }

  public static int[] getIntArray(int length){
    int[] dim={length};
    return (int[])getIntArray(dim);
  }

  public static float[] getFloatArray(int length){
    int[] dim={length};
    return (float[])getFloatArray(dim);
  }

  public static void printArray(Object array){
    int maxmax=10;
    if(array instanceof float[]){
      int maxI=((float[])array).length;
      if(maxI>maxmax) maxI=maxmax;
      System.out.print("[");
      for( int i=0 ; i<maxI ; i++ ){
        System.out.print(((float[])array)[i]);
        if(i<maxI-1)
          System.out.print(", ");
        else if(i==maxI-1)
          System.out.println(", ... ]");
      }
    }else if(array instanceof int[]){
      int maxI=((int[])array).length;
      if(maxI>maxmax) maxI=maxmax;
      System.out.print("[");
      for( int i=0 ; i<maxI ; i++ ){
        System.out.print(((int[])array)[i]);
        if(i<maxI-1)
          System.out.print(", ");
        else if(i==maxI-1)
          System.out.println(", ... ]");
      }
    }else if(array instanceof float[][]){
      int maxI=((float[][])array).length;
      if(maxI>maxmax) maxI=maxmax;
      System.out.print("[");
      for( int i=0 ; i<maxI ; i++ ){
        printArray(((float[][])array)[i]);
        if(i<maxI-1)
          System.out.print(", ");
        else if(i==maxI-1)
          System.out.println(", ... ]");
      }
    }else if(array instanceof int[][]){
      int maxI=((int[][])array).length;
      if(maxI>maxmax) maxI=maxmax;
      System.out.print("[");
      for( int i=0 ; i<maxI ; i++ ){
        printArray(((int[][])array)[i]);
        if(i<maxI-1)
          System.out.print(", ");
        else if(i==maxI-1)
          System.out.println(", ... ]");
      }
    }else{
      System.out.println("NOT PRINTING");
    }
  }

  public static String getNXfileSummary(String filename){
    filename=DataSetTools.util.FilenameUtil.setForwardSlash(filename);
    String result=null;
    CNexusFile file=null;

    String fname = null;
    String date  = null;
    String name  = null;
    String title = null;

    int index=0;

    try{
      Hashtable attrlist=null;
      Hashtable direc=null;
      Hashtable inner_direc=null;
      Enumeration keys=null;
      Enumeration inner_keys=null;


      file=new CNexusFile(filename,CNexusFile.NXACC_READ);
      attrlist=getAttrList(file);

      // create some defaults
      String defFile=filename;
      String defDate=(String)attrlist.get("file_time");
      String defName=(String)attrlist.get("user");
      String defTitl="";

      // useful local variables
      String key=null;
      String val=null;

      // find the right group to be in
      direc=file.groupdir();
      keys=direc.keys();
      while(keys.hasMoreElements()){
        key=(String)keys.nextElement();
        if( "NXentry".equals((String)direc.get(key)) ){
          file.opengroup(key,"NXentry");
          inner_direc=file.groupdir();
          if( inner_direc.get("title")!=null )
            break;
          else
            file.closegroup();
        }else{
          key=null;
        }
      }

      // now start looking for the good stuff
      attrlist=file.groupdir();
      // first the date;
      val=(String)attrlist.get("start_time");
      if(val!=null && val.equals("SDS"))
        date=(String)getData(file,"start_time");
      // next the user name
      keys=attrlist.keys();
      while(keys.hasMoreElements()){
        key=(String)keys.nextElement();
        if("NXuser".equals(attrlist.get(key))){
          file.opengroup(key,"NXuser");
          name=(String)getData(file,"name");
          file.closegroup();
          break;
        }
      }
      // finally the title
      val=(String)attrlist.get("title");
      if(val!=null && val.equals("SDS"))
        title=(String)getData(file,"title");

      // use the defaults if there isn't another choice
      if(fname==null || fname.length()==0) fname=defFile;
      if(date==null  || date.length()==0)  date=defDate;
      if(name==null  || name.length()==0)  name=defName;
      if(title==null || title.length()==0) title=defTitl;

      // chop down the filename
      fname=DataSetTools.util.FilenameUtil.setForwardSlash(fname);
      index=fname.lastIndexOf("/");
      if(index>=0) fname=fname.substring(index+1);
      // chop down the date
      index=date.indexOf(" ");
      if(index>=0) date=date.substring(0,index);

      // format this stuff
      StringBuffer sb=new StringBuffer(80);
      sb.append(fname+"                              ");
      sb.delete(30,sb.length());
      sb.append(date+" ");
      sb.append(name+"                    ");
      sb.delete(55,sb.length());
      sb.append(title+"                                   ");
      result=sb.substring(0,80);
    }catch(NexusException e){
      System.out.println("ENCOUNTERED EXCEPTION"+e.getMessage());
      // let it drop on the floor
    }

    try{
      if(file!=null) file.finalize(); // close the file
    }catch(Throwable e){
      // let it drop on the floor
    }

    return result;
  }
}
