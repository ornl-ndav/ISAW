

/*
 * File:  NexUtilsmixDims.java
 *
 * Copyright (C) 2006, Ruth Mikkelson
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
 * Revision 1.3  2007/07/11 18:27:10  rmikk
 * Eliminated some debug printing
 * Added the Total Count Attribute
 * Deleted a bunch of unused routines that were copied from NexUtils
 *
 * Revision 1.2  2007/01/12 14:48:47  dennis
 * Removed unused imports.
 *
 * Revision 1.1  2006/11/14 16:23:38  rmikk
 * Initial Checkin.  This class deals with NeXus files whose tuple positions do
 *   not correspond to the natural order [det.row.col,time]
 *
 */

package NexIO.Util;


import NexIO.*;
import NexIO.State.*;
import DataSetTools.dataset.*;
import gov.anl.ipns.MathTools.Geometry.*;

import java.util.*;
//import javax.xml.parsers.*;
//import java.io.*;
import org.w3c.dom.*;


/**
 *  This class contains the Generic implementations of the methods in 
 *  the interface INexUtils. Also it has other utility routines related
 *  to NeXus files.
 *  @see INexUtils
 */
public class NexUtils_mixDims implements INexUtils {
 
    public String xUnits = "us";
    public String angleUnits = "radian";

    String errormessage = "";
    // The data in the file is stored float [a,b,c,d] where d is pos o, c is pos 1
    //   b is pos2, and a is pos3.  If no detectors use -1. LPSD's have i column and
    //    may have to be -1 if the dimension reported by nexus dimension does not
    //    have the number
    int timeDim = 0;
    int coldim  =1;
    int rowdim =2;
    int detDim =3;
    StateInfo states;
    private int[] mult; //multiply our det(3),row(2),col(1),and time(0) indexe by  to get their corresp "linear position"
    int nrows =0;
    int ncols=0;
    int ntimes=0;
    int ndetectors=0;
    int nMissingDims =0;
    int[] IPNS2NexDim, Nex2IPNSDim;
    int[] IdentDimAxis;
    int[] dimensions;
    NxDataStateInfo DataState;
    
    
    /**
     * Allows for reordering of the axes. The normal order is [detector, row, col, time]==[3,2,1,0]
     * and other state info can be taken care of.
     * @param timeDim  The position name([3,2,1,0] C conv) that represents time in the NeXus file
     * @param colDim   The position name([3,2,1,0] C conv) that represents the column in the NeXus file
     * @param rowDim   The position name([3,2,1,0] C conv) that represents the row in the NeXus file
     * @param detDim   The position name([3,2,1,0] C conv) that represents the first detector in the NeXus file
     * @param axis1Num the number given to the axis that should be axis 1 (tof)
     * @param axis2Num  the number given by the Nexus file for axis 2(col, or row if one col)
     * @param axis3Num  The number given by the NeXus file for axis 3( row or det if 1 col
     * @param axis4Num  The number given by the NeXus file for axis 4 if any or 01
     * @param states   A linked list of state information
     */
    public NexUtils_mixDims( int timeDim, int colDim, int rowDim, int detDim, 
             int axis1Num,int axis2Num, int axis3Num, int axis4Num, StateInfo states){
       this.timeDim = timeDim;
       this.coldim  = colDim;
       this.rowdim  = rowDim;
       this.detDim  = detDim;
       this.states = states;
      
       DataState=null;
       NxEntryStateInfo EntryState = null;
       NxfileStateInfo  FileState = null;
       StateInfo ss = states;
       while( ss != null){
          if( (ss instanceof NxDataStateInfo) &&( DataState == null))
             DataState = (NxDataStateInfo)ss;
          else if((ss instanceof NxfileStateInfo) &&( FileState== null))
             FileState = (NxfileStateInfo)ss;
          else if( (ss instanceof NxEntryStateInfo) && (EntryState == null))
             EntryState = (NxEntryStateInfo)ss;
          ss = ss.getNext();
       }
       if( DataState == null)
          return;
       dimensions = DataState.dimensions;
       

       int C=0;
       if( colDim < 0){colDim = -1; C++;}
       if( rowDim < 0){rowDim = -1; C++;}
       if( timeDim < 0){timeDim = -1;C++ ;}
       if( detDim < 0 ){detDim = -1;C++;}
       nMissingDims = C;
       if( dimensions == null){
          
          setErrorMessage(" no data dimensions");
       }
       IPNS2NexDim = new int[dimensions.length ];
       Arrays.fill( IPNS2NexDim, -2 );
       int m=1;
       IPNS2NexDim[0 ]= timeDim;
       if( colDim >=0)
          IPNS2NexDim[m++]  = colDim;
       if( rowDim >=0)
          IPNS2NexDim[m++]  = rowDim;
       if( detDim >=0)
                IPNS2NexDim[m++]  = detDim;
       
       
       if( !check(timeDim, 0, dimensions.length-1)){
           setErrorMessage( "time dimension incorrect");
           return;
       }if( (!check(rowDim, 0, dimensions.length-1)) &&(rowDim >=0)){
          setErrorMessage( " row dimensions out of bounds");
          return;
       }if( (!check(colDim, 0, dimensions.length-1)) && (colDim >=0 )){
          setErrorMessage( " column dimensions out of bounds");
          return;
       }if( (!check(detDim, 0, dimensions.length-1)) && (detDim >=0 )){
          setErrorMessage(" detector dimensions out of bounds");
          return;
       }
       
       Nex2IPNSDim = new int[ dimensions.length ];
       java.util.Arrays.fill( Nex2IPNSDim, -1);
       Nex2IPNSDim[timeDim ]= 0;
       if( colDim >=0)Nex2IPNSDim[colDim ]= 1;
       if( rowDim >=0)Nex2IPNSDim[rowDim ]= 2;
       if( detDim >=0)Nex2IPNSDim[detDim ]= 3;
  
      
       boolean[] check= new boolean[4];
       Arrays.fill( check, false);
       if( timeDim >=0){ 
        
          check[timeDim]=true;
       }
       if( colDim >=0){
         
          if( check[colDim])return;
          check[colDim]=true;
       }
       if( rowDim >=0 ){
       
          if( check[rowDim])return;
          check[rowDim]=true;
       }
       if( detDim >= 0) {
         
          if( check[detDim])return;
          check[detDim]=true;
       }
   
       

      
       int mm = m;
       for( int i =0; i< Nex2IPNSDim.length; i++)
          if( Nex2IPNSDim[i] < 0){
             Nex2IPNSDim[i] = mm;
             IPNS2NexDim[mm] = i;
             mm++;
          }
       
       ntimes = 1;
       nrows=1; 
       ncols=1; 
       ndetectors=1;
       int L = dimensions.length -1;
       if( timeDim >=0)
           ntimes = dimensions[L- timeDim ];
       else C++;
       if( rowDim >=0)
           nrows = dimensions[ L-rowDim ];
       else C++;
       if( colDim >=0)
           ncols = dimensions[ L-colDim ];
       else C++;
       if( detDim >=0 ){
          ndetectors = 1;
          for( int i=3-nMissingDims; i< dimensions.length-1; i++)
             ndetectors *= dimensions[ IPNS2NexDim[i]];
       }
       /*mult = new int[ dimensions.length -1 ] ; 
       
        m = 1;
       
       Arrays.fill( mult ,-1);
      
      for( int i= dimensions.length-1; i >= 0; i--){//i is Nex index
         if( Nex2IPNSDim[i] >= 0)
           if( i==1)
              mult[Nex2IPNSDim[i]]=1;
           else
              mult[Nex2IPNSDim[i]]= m;
           m*=dimensions[i];
         
       }
      */ 
       ndetectors = prod( dimensions,0,dimensions.length-1)/(nrows * ncols*ntimes);
         
    }
    
    
  
    private int prod( int[] dim, int start, int end){
       if( dim ==null)
          return 1;
      if(start < 0) start = 0;
      if( end < 0) end =0;
      if( start > dim.length) start = dim.length-1;
      if( end > dim.length) end = dim.length-1;
      int P = 1;
      for( int i=start; i<=end;i++)
          P *= dim[i];
      return P;
    }
    
    
    public boolean check( int val1, int min, int max){
       if( val1 < min )
          return false;
       if( val1 > max)
          return false;
       return true;
    }
    
    
    
    /**
     *  Currently invoked by the standard Process1Nxdata. It adds all the 
     *  data from the data field of NXdetector to the DataSet DS 
     *  @param DS  the DataSet that is having information added to
     *  @param NxDataNode The NxNode containing information about an NeXus
     *                      NXdata class
     *  @param NxDetector The NxNode containing information about an NeXus
     *                      NXdetector class
     *  @param startDSindex  the first GroupIndex to process
     *  @param States   The linked list of state information
     *  This method sets up grids with default ID's of 1,2,3,...
     */
    public boolean setUpNXdetectorAttributes( DataSet DS , NxNode NxDataNode , 
        NxNode NxDetector , int startDSindex , NxfileStateInfo States ) {
    
        
        NxDataStateInfo dataState = NexUtils.getDataStateInfo( States );
        NxDetectorStateInfo detState = NexUtils.getDetectorStateInfo( States );
        NxEntryStateInfo  NxEntryState = NexUtils.getEntryStateInfo( States );
     
        //String version = null;

       // if ( NxEntryState != null )
       //     version = NxEntryState.version; 
        
        if ( dataState == null )
            return setErrorMessage( " No state for NXdata " +
                    NxDataNode.getNodeName() );
        
       /* if ( detState == null )
            return setErrorMessage( " No state for NXdetector " +
                    NxDetector.getNodeName() );
       */
        float[] azimuth,polar,distance,solAng, slot, crate,
             input, depth, width, height;
        Vector3D[]  x_dir, y_dir;
        int[] ids;
        dimensionHandler azimuthDims, polarDims, distanceDims, widthDims,
             heightDims, solAngDims, slotDims, crateDims, inputDims,
             x_dirDims, depthDims, idsDims;
           
        Object[] Dat =getLocationData(States.xmlDoc, NxDetector,  NxDataNode,  States, "crate");
           crate =(float[])(Dat[0]); crateDims =(dimensionHandler)(Dat[1]);   
        Dat =getLocationData(States.xmlDoc, NxDetector,  NxDataNode,  States, "slot");
          slot =(float[])(Dat[0]); slotDims =(dimensionHandler)(Dat[1]);   
        Dat =getLocationData(States.xmlDoc, NxDetector,  NxDataNode,  States, "input");
          input =(float[])(Dat[0]); inputDims =(dimensionHandler)(Dat[1]);   
        Dat =getLocationData(States.xmlDoc, NxDetector,  NxDataNode,  States, "solid_angle");
          solAng =(float[])(Dat[0]); solAngDims =(dimensionHandler)(Dat[1]);   
        Dat =getLocationData(States.xmlDoc, NxDetector,  NxDataNode,  States, "distance");
          distance =(float[])(Dat[0]);distanceDims =(dimensionHandler)(Dat[1]);   
        Dat =getLocationData(States.xmlDoc, NxDetector,  NxDataNode,  States, "polar_angle");
          polar =(float[])(Dat[0]); polarDims =(dimensionHandler)(Dat[1]);   
        Dat =getLocationData(States.xmlDoc, NxDetector,  NxDataNode,  States, "azimuthal_angle");
          azimuth =(float[])(Dat[0]); azimuthDims =(dimensionHandler)(Dat[1]);
        Dat =getLocationData(States.xmlDoc, NxDetector,  NxDataNode,  States, "id");
          ids =(int[])(Dat[0]); idsDims =(dimensionHandler)(Dat[1]);  
        
        Dat =getLocationData(States.xmlDoc, NxDetector,  NxDataNode,  States, "x_dir");
          x_dir =(Vector3D[])(Dat[0]); x_dirDims =(dimensionHandler)(Dat[1]);
        Dat =getLocationData(States.xmlDoc, NxDetector,  NxDataNode,  States, "y_dir");
          y_dir =(Vector3D[])(Dat[0]); 
        Dat =getLocationData(States.xmlDoc, NxDetector,  NxDataNode,  States, "width");
          width =(float[])(Dat[0]); widthDims =(dimensionHandler)(Dat[1]);
        Dat =getLocationData(States.xmlDoc, NxDetector,  NxDataNode,  States, "depth");
          depth=(float[])(Dat[0]); depthDims =(dimensionHandler)(Dat[1]);
        Dat =getLocationData(States.xmlDoc, NxDetector,  NxDataNode,  States, "height");
          height =(float[])(Dat[0]); heightDims =(dimensionHandler)(Dat[1]);
          
        int[] counter = new int[ dimensions.length];
        
        Arrays.fill(counter, 0);
        
        dimensionHandler detector= new dimensionHandler( dimensions, timeDim, coldim,rowdim);
        dimensionHandler row= new dimensionHandler( dimensions, timeDim, coldim,rowdim);
        dimensionHandler col= new dimensionHandler( dimensions, timeDim, coldim,rowdim);
        detector.resetIndex( counter);
        row.resetIndex( counter);
        col.resetIndex( counter);
        
           
        
        UniformGrid grid = null;
        int group =startDSindex;
        int m = Math.max(Math.max( rowdim , coldim), timeDim)+1;
        int startGridNumIndex = Grid_util.getAllDataGrids( DS ).size();
        for ( int i = 0; i< ndetectors; i++) {
           
           counter = detector.getCounter();
           
           setCounter( counter, row, col, crateDims, inputDims, slotDims,solAngDims);
           setCounter( counter, azimuthDims, polarDims, distanceDims, widthDims, heightDims, solAngDims);
           
           if( x_dirDims != null)
              x_dirDims.resetIndex( counter );
           if( idsDims != null)
               idsDims.resetIndex( counter );
           
           DetectorPosition center = getCenter( distance,azimuth,polar, distanceDims, 
                    azimuthDims, polarDims);
            
           if( (x_dir!=null) &&(y_dir != null)&&(center != null)&&
                    (distance != null))
                grid = new UniformGrid( startGridNumIndex +1+i, "m",
                    new Vector3D( center ),x_dir[x_dirDims.getIndex()],
                    y_dir[x_dirDims.getIndex()] ,Aval(width,widthDims),
                    Aval( height,heightDims), Aval( depth, depthDims),
                    nrows, ncols);
           else grid = null;
           for( int r=0; r< nrows; r++){
              counter = row.getCounter();
              setCounter( counter, col, crateDims, inputDims, slotDims,solAngDims, x_dirDims);
              setCounter( counter, azimuthDims, polarDims, distanceDims, widthDims, heightDims, solAngDims);

              if( idsDims != null)
                   idsDims.resetIndex( counter );
              for( int c =0; c< ncols; c++){              
       
            Data db = DS.getData_entry(group );
            group++;
            int TotPos = group - startDSindex;

            if ( States.Spectra != null )
                TotPos = States.Spectra[ group - startDSindex ];
            
           
            if( crateDims != null)
               setFloatAttr( db , Attribute.CRATE , crate , crateDims.getIndex() );
            if( inputDims != null)
               setFloatAttr( db , Attribute.INPUT , input , inputDims.getIndex() );
            if( slotDims != null)
               setFloatAttr( db , Attribute.SLOT , slot , slotDims.getIndex() );
            if( solAngDims != null)
               setFloatAttr( db , Attribute.SOLID_ANGLE , solAng , solAngDims.getIndex() );
        
            if ( ids != null )
              if( ids.length >= DS.getNum_entries()-startDSindex){
               
                 db.setGroup_ID( ids[idsDims.getIndex()]);
              }
           
           //---------------------------Set up Detectors----------------------
            if( (x_dir != null) && (y_dir != null)&& ( grid != null)){
               
               
               db.setAttribute( new PixelInfoListAttribute(  Attribute.PIXEL_INFO_LIST,
                        new PixelInfoList( new DetectorPixelInfo( group-1,(short)(r+1),(short)(c+1),
                                 grid))));
            }else{
               db.removeAttribute( Attribute.PIXEL_INFO_LIST);
               int dpos = 0,
                   ppos = 0,
                   apos = 0;
               if( distanceDims != null)
                  dpos = distanceDims.getIndex();
               if( polarDims != null)
                  ppos = polarDims.getIndex();
               if( azimuthDims != null)
                  apos = azimuthDims.getIndex();
               center = ConvertDataTypes.convertToIsaw( 
                        Aval(distance,dpos ) , Aval(polar,ppos ) ,
                        Aval(azimuth,apos ));
               db.setAttribute( new DetPosAttribute( Attribute.DETECTOR_POS, center));
            }

            Incrcol( col, crateDims, inputDims, slotDims,solAngDims,x_dirDims);
            Incrcol(  azimuthDims, polarDims, distanceDims, widthDims, heightDims, solAngDims);
            if( idsDims != null)
                idsDims.IncrCol();
           }//for col
              row.IncrRow();
           } //for row
           detector.IncrPlace( m );
           if( grid != null){
              grid.setData_entries( DS );
              Grid_util.setEffectivePositions( DS, grid.ID());
           }
        }//for detector

             
        return false;
    }
    private Object[] getxmlLocationData( Node N1, NxNode NxDetector, 
            NxNode DataNode, NxfileStateInfo state,   String field){
       Object[] Res = new Object[2];
       Res[0]= Res[1] = null;
       NxDataStateInfo DataState =  NexUtils.getDataStateInfo( state );
       NxEntryStateInfo EntryState = NexUtils.getEntryStateInfo( state );
       if( N1 == null)
          return Res;
       Node xmlDoc =Util.getNXInfo( N1, "NXinstrument", null, null, null);
       String XmlClasses ="NXdetector";
       
       String XmlClassNames = DataNode.getNodeName();
       if( xmlDoc != null )
         if( ( ";crate;slot;input;solid_angle;distance;"
                  + "azimuthal_angle;polar_angle;id;" ).indexOf( ";" + field
                  + ";" ) >= 0 ) {

            Node NN = Util.getNXInfo( xmlDoc , XmlClasses , XmlClassNames ,
                     field , null );
            if( NN != null ) {
               if( ! field.equals( "id" ) )
                  Res[ 0 ] = ConvertDataTypes.floatArrayValue( Util.getLeafNodeValues(NN) );
               else
                  Res[ 0 ] = ConvertDataTypes.intArrayValue( Util.getLeafNodeValues(NN) );
               int[] dims = ConvertDataTypes.intArrayValue( Util
                        .getXmlNodeAttributeValue( NN , "dimension" ) );
               if( dims != null)
                  Res[1] = new dimensionHandler( dims, timeDim, coldim, rowdim);
               else
                  Res[1] = null;


               return Res;
            }
         }
         else if( ";width;height;depth;".indexOf( field ) >= 0 ) {

            Node NN = Util.getNXInfo( xmlDoc , XmlClasses
                     + ".NXgeometry.NXshape.size" , XmlClassNames + ".geometry" ,
                     null , null );
            if( NN != null ) {

               int[] d = ConvertDataTypes.intArrayValue( Util
                        .getXmlNodeAttributeValue( NN , "dimension" ) );
               if( d != null ) {
                  int[] dd = new int[ d.length -1 ];
                  System.arraycopy( d , 0 , dd , 0 , dd.length );
                  Res[ 1 ] = new dimensionHandler( dd, timeDim, coldim, rowdim);
               }
               else
                  Res[ 1 ] = null;


               String units = ConvertDataTypes.StringValue( Util
                        .getXmlNodeAttributeValue( NN , "units" ) );
               float[] V = ConvertDataTypes.floatArrayValue( Util
                        .getLeafNodeValues( NN ) );
               Res[ 0 ] = null;
               if( ( V != null ) && ( Res[ 1 ] != null ) ) {
                  int offset = 1;
                  if( field.equals( "width" ) )
                     offset = 0;
                  else if( field.equals( "height" ) )
                     offset = 2;
                  float[] R = new float[ V.length / 3 ];
                  for( int i = 0 ; i < R.length ; i++ )
                     R[ i ] = V[ 3 * i + offset ];
                  ConvertDataTypes.UnitsAdjust( R , units , "m" , 1f , 0f );

                  Res[ 0 ] = R;
                 
                  return Res;
               }
            }
         } 
       
       return Res;
       
       
    }
    /**
     * 
     * @param N  A Nxentry node 
     * @param entryName  The name of the NXentry
     * @return  two nodes, one is a nonamed NXentry node and the second is the
     *          name NXentry node. The values are null if none
     */
    public static Node[] getNxEntryNodes( Node N, String entryName){
       Node[] Res = new Node[2];
       Res[0]=Res[1]=null;
       if( N== null)
          return Res;
       while( N != null){
          if( N.getNodeName().equals("NXentry")){
             String name =Util.getXmlNodeAttributeValue( N,"name");
             if( name == null)
                Res[0]= N;
          
             else if( (entryName != null) &&(name.equals(entryName)))
                Res[1] = N;
          }
          N= N.getNextSibling();
       }
       return Res;
    }
    
    private Object[] getxmlOrientationData(Node N1, NxNode NxDetector, 
             NxNode DataNode, NxfileStateInfo state,   String field){
       Object[] Res = new Object[2];  
       Res[0]=Res[1]= null;
       if( ";x_dir;y_dir;".indexOf(field)<0)
          return Res;
       N1 = Util.getNXInfo(N1,"NXinstrument", null,null,null);
       NxDataStateInfo DataState =  NexUtils.getDataStateInfo( state );
       NxEntryStateInfo EntryState = NexUtils.getEntryStateInfo( state );
       String XmlClasses ="NXdetector.NXgeometry.NXorientation.value";
       
       String XmlClassNames = DataNode.getNodeName()+"."+"geometry";
       
       Node NN = Util.getNXInfo( N1, XmlClasses, XmlClassNames, null, null);
       if( NN != null){
          int[] dim1 = ConvertDataTypes.intArrayValue( Util.getXmlNodeAttributeValue( 
                                                                NN, "dimension"));
          if( dim1 != null){
             int[] dim = new int[dim1.length-1];
             System.arraycopy( dim1,0,dim,0,dim.length);
             Res[1] = new dimensionHandler(dim, timeDim, coldim , rowdim);
          }else
             return Res;
          float[] V = ConvertDataTypes.floatArrayValue( Util.getLeafNodeValues( NN) );
          if( V == null){
             Res[0] = Res[1]= null;
             return Res;
          }else{
             Vector3D[] VV = new Vector3D[ V.length/6 ];
             int offset =0;
             if( field.equals("y_dir"))
                offset = 3;
             float xx,yy,zz;
             for( int i =0; i < VV.length; i++){
                xx= V[6*i+offset+2]; 
                yy= V[6*i+offset+0]; 
                zz= V[6*i+offset+1]; 
                VV[i] = new Vector3D( xx,yy,zz);
             }
            Res[0] = VV;
            return Res;
          }
          
       }
      return Res; 
    }
    private Object[] getLocationData(Node xmlDoc, NxNode NxDetector, 
            NxNode DataNode, NxfileStateInfo state,   String field){
       
       if(";orientation;x_dir;y_dir;".indexOf( ";"+field+";" ) >=0)
          return getOrientationData( xmlDoc , NxDetector , 
                                      DataNode , state , field );

       Object[] Res = new Object[2],            
                Res1 = new Object[2] ;
       Res[0]= Res[1] =Res1[0]= Res1[1] =  null;
       NxDataStateInfo DataState =  NexUtils.getDataStateInfo( state );
       NxEntryStateInfo EntryState = NexUtils.getEntryStateInfo( state );
       
       NxNode geom = null;
       if( NxDetector != null){
          geom = NxDetector.getChildNode( "geometry");
       } 
       Node NN = Util.getNXInfo( xmlDoc ,"Common.NXentry" , null ,
                null , null );
       Node[] R = getNxEntryNodes( NN,EntryState.Name);
       Res = getxmlLocationData( R[0], NxDetector,DataNode,state,field);
       Res1 =getxmlLocationData( R[1], NxDetector,DataNode,state,field);
       if( Res1[0] != null)
          Res = Res1;
     
       NN = Util.getNXInfo( xmlDoc ,"Runs" , null ,
               null , null );
       String filename= state.filename;
       if( (filename != null)&&(NN != null)){
          filename = filename.replace('\\','/');
          int kk= filename.lastIndexOf("/");
          if( kk >=0)
             filename = filename.substring(kk+1);
       }
         
      NN = Util.getNXInfo( NN, "Run", null, null,filename);
      R = getNxEntryNodes( NN, EntryState.Name);
      Res1 = getxmlLocationData( R[0], NxDetector,DataNode,state,field);
      if( Res1[0] != null)
         Res = Res1;

      Res1 = getxmlLocationData( R[1], NxDetector,DataNode,state,field);
      if( Res1[0] != null)
         Res = Res1;
      if( Res[0] != null)
         return Res;
      
       if( (geom == null) ||((";crate;slot;input;solid_angle;distance;"+
                   "azimuthal_angle;polar_angle;id;").indexOf(";"+field+";")>=0)){//orientation
         
           
            
           //Information is in NxGeometry. Assume rectangular detectors
           float[] crate = NexUtils.getFloatArrayFieldValue( NxDetector ,field );  
           dimensionHandler D = null;
           if( crate != null)
              Util.GetDimension( NxDetector.getChildNode( field), DataState ,
                             timeDim, coldim,rowdim,0);
           Res[0]= crate;
           Res[1] =D;
           return Res;
           
       }
       if( geom == null)
          return Res;
       NxDetectorStateInfo DetInfo = NexUtils.getDetectorStateInfo( state);
       Res[0] = NexUtils.getNxGeometryInfo( geom , field , DetInfo);
      
       
       int p = NexUtils.getNextChildNode(geom,"NXshape",0);
       if( p < 0){
          Res[0] = Res[1] = null;
          return Res;
       }
      
       NxNode shapeNode = geom.getChildNode( p );
      
       NxNode sizeNode =shapeNode.getChildNode("size");
       if( sizeNode == null){
          Res[0] = Res[1] = null;
       }
       ConvertDataTypes.UnitsAdjust((float[])Res[0], ConvertDataTypes.StringValue( 
                sizeNode.getAttrValue("units")),"m",1f,0f);
       
       Res[1] = Util.GetDimension( sizeNode ,DataState,timeDim,coldim,rowdim,1);
       return Res;
       
            
    }
    
    
    private Object[] getOrientationData(Node xmlDoc, NxNode NxDetector, 
             NxNode DataNode, NxfileStateInfo state,   String field){

       Object[] Res = new Object[2], 
                Res1 = new Object[2];   
       Res[0]= Res[1] = Res1[0]= Res1[1] =null; 
       
       if( ";x_dir;y_dir;".indexOf(field)<0)
          return Res;
       NxDataStateInfo DataState =  NexUtils.getDataStateInfo( state );
       NxEntryStateInfo EntryState = NexUtils.getEntryStateInfo( state );
      
       NxNode geom = null;
       if( NxDetector != null){
          geom = NxDetector.getChildNode( "geometry");
       }
       Node NN = Util.getNXInfo( xmlDoc ,"Common.NXentry" , null ,
                null , null );
       Node[] R = getNxEntryNodes( NN,EntryState.Name);
       Res = getxmlOrientationData( R[0], NxDetector,DataNode,state,field);
       Res1 =getxmlOrientationData( R[1], NxDetector,DataNode,state,field);
       if( Res1[0] != null)
          Res = Res1;
     
       NN = Util.getNXInfo( xmlDoc ,"Runs" , null ,
               null , null );
       String filename= state.filename;
       if( (filename != null)&&(NN != null)){
          filename = filename.replace('\\','/');
          int kk= filename.lastIndexOf("/");
          if( kk >=0)
             filename = filename.substring(kk+1);
       }
         
      NN = Util.getNXInfo( NN, "Run", null, null,filename);
      R = getNxEntryNodes( NN, EntryState.Name);
      Res1 = getxmlOrientationData( R[0], NxDetector,DataNode,state,field);
      if( Res1[0] != null)
         Res = Res1;

      Res1 = getxmlOrientationData( R[1], NxDetector,DataNode,state,field);
      if( Res1[0] != null)
         Res = Res1;
      if( Res[0] != null)
         return Res;
      
       if( NxDetector == null)
          return Res;
       if( geom == null){
           Vector3D[] x_dir = null,
               y_dir = null;   
           float[] orientation;
     
           NxNode orientNode = NxDetector.getChildNode( "orientation" );

           if ( orientNode != null ) {  
               
               orientation = ConvertDataTypes.floatArrayValue( orientNode.
                                                          getNodeValue() );
               ConvertDataTypes.UnitsAdjust( orientation , ConvertDataTypes.
                   StringValue( orientNode.getAttrValue( "units" ) ) , "radians" ,
                   1.0f , 0.0f ); 
                Res[1] = Util.GetDimension( orientNode, DataState, timeDim, coldim,rowdim,1);
             
                
           } else
               orientation = null;
          
           if ( orientation != null ) { 
                
               x_dir = new Vector3D[ orientation.length / 3 ];
               y_dir = new Vector3D[ orientation.length/3]
;          
               for ( int i = 0; i < x_dir.length; i++ ) {
                   x_dir[ i ] = Rotate( orientation , i , 0 , -1 , 0 );           
                   y_dir[ i ] = Rotate( orientation , i , 0 , 0 , 1 );
               }
               if( field.equals( "x_dir"))
                  Res[0] = x_dir;
               else
                  Res[0]= y_dir;
               return Res;
           }
       } else {//data in NXgeometry
                     
                int p   =   NexUtils.getNextChildNode(geom,"NXorientation",0);
               
                if( p >0 ){
                   NxNode orientNode = geom.getChildNode( p);
                   Res[1] = Util.GetDimension(  geom.getChildNode( p ),DataState,timeDim,coldim,rowdim,1);
                }
                Res[0]=null;
                NxDetectorStateInfo detState= NexUtils.getDetectorStateInfo( state );
               float[] xx = NexUtils.getNxGeometryInfo( geom , "x_dir" , detState);
               float[]yy = NexUtils.getNxGeometryInfo( geom , "y_dir" , detState);
         
               if ( ( xx != null ) && ( yy != null ) && ( xx.length == yy.length ) ) { 
          
                   Vector3D[] x_dir = new Vector3D[ xx.length / 3 ];
                   Vector3D[] y_dir = new Vector3D[ yy.length / 3 ];
           
                   for ( int i = 0; i < xx.length / 3; i++ ) {
             
                       x_dir[ i ] = new Vector3D( xx[ 3*i + 2 ] , xx[ 3*i ] ,
                                                            xx[ 3*i + 1 ] );
                       y_dir[ i ] = new Vector3D( yy[ 3*i + 2 ] , yy[ 3*i ] , 
                                                            yy[ 3*i + 1 ] );
             
                   }
                   if( field.equals("x_dir"))
                      Res[0] =x_dir;
                   else
                      Res[0] = y_dir;
                   
               }
               return Res;
           }
         return Res;
       }
    
   
     

  
     private void setCounter( int[] counter, dimensionHandler H1,
                            dimensionHandler H2,dimensionHandler H3,
                            dimensionHandler H4,dimensionHandler H5,
                            dimensionHandler H6 ){
         if( counter == null) return;
         if( H1 != null)
            H1.resetIndex( counter);
         if( H2 != null)
            H2.resetIndex( counter);
         if( H3 != null)
            H3.resetIndex( counter);
         if( H4 != null)
            H4.resetIndex( counter);
         if( H5 != null)
            H5.resetIndex( counter);
         if( H6 != null)
            H6.resetIndex( counter);
        
     }
     private void Incrcol( dimensionHandler H1 , dimensionHandler H2 ,
            dimensionHandler H3 , dimensionHandler H4 , dimensionHandler H5 ,
            dimensionHandler H6 ) {

 
      if( H1 != null )
         H1.IncrCol();
      if( H2 != null )
         H2.IncrCol();
      if( H3 != null )
         H3.IncrCol();
      if( H4 != null )
         H4.IncrCol();
      if( H5 != null )
         H5.IncrCol();
      if( H6 != null )
         H6.IncrCol();

   }
     
    //gets the center of a Detector or returns null if no detectors(all 1 by 1) 
    private DetectorPosition getCenter( float[] distance, float[] azimuth, float[] polar, 
             dimensionHandler distanceDims, dimensionHandler azimuthDims,
             dimensionHandler polarDims){
        if( distance == null)
             return null;
        if(distanceDims == null)
          return null;
        int[] dims =  distanceDims.getLimits();
        int L = dims.length -1 ;
        if( dims [L-timeDim]>=0)
            return null;
        if((rowdim >0) &&( dims [rowdim]>=0))
           return null;
        if((coldim >0) && (dims [L-coldim]>=0))
           return null;
        float dist = 0;
       
        dist = distance[distanceDims.getIndex()];
        float azim=0;
        if( azimuth != null) if( azimuthDims != null){
           int[] azDims = azimuthDims.getLimits();
           if( azDims [ L-timeDim ]>=0)
              return null;
           //if((rowdim >0) &&(  azDims [L-rowdim ]>=0))
           //   return null;
           //if((coldim >0) && ( azDims [L-coldim]>=0))
           //   return null;
           azim =azimuth[ azimuthDims.getIndex()];
        }
        float pol=0;
        if( polar != null) if( polarDims != null){
           int[] polDim = polarDims.getLimits();
           if( polDim [L-timeDim]>=0)
              return null;
          // if((rowdim >0) &&(  polar [L-rowdim]>=0))
           //   return null;
           //if((coldim >0) && ( polar [L-coldim]>=0))
          //   return null;
           pol =polar[ polarDims.getIndex()];
        }
        return ConvertDataTypes.convertToIsaw( dist,pol,azim);
        
        
       
       
    }
  
    
    //Euler rotations , version null's orientation
    private Vector3D Rotate( float[] orientations , int grid , float x , float y ,
        float z ) {
     
        if ( orientations == null )
            return new Vector3D( x , y , z );
        if ( orientations.length < 3 )
            return new Vector3D( x , y , z );
 
        int p = 3 * grid;

        if ( p + 2 >= orientations.length ) {
            p = orientations.length / 3 - 1;
        }

        Tran3D Matrix = new Tran3D(  );

        Matrix.setRotation( (float ) ( orientations[ p ] * 180 /  
                            java.lang.Math.PI ) ,new Vector3D( 1 , 0 , 0 ) );
     
        Vector3D res = new Vector3D();
        Tran3D next = new Tran3D();

        next.setRotation( (float) ( orientations[ p + 1 ] * 
                           180 / java.lang.Math.PI ) ,new Vector3D( 0 , 1 , 0 ) );
        next.multiply_by( Matrix );
        Matrix = next;
     
        next = new Tran3D();
        next.setRotation( (float) ( orientations[ p + 2 ] *
                            180 / java.lang.Math.PI ) , new Vector3D( 1 , 0 , 0 ) );
        next.multiply_by( Matrix );
        Matrix = next;
    
        Matrix.apply_to( new Vector3D( x , y , z ) , res );
        return res;
     
    }

    /**
     *     Grid_util.getAreaGridIDs does NOT WORK if each detector has
     *     only one row and one column
     */
    private int Maxx( DataSet DS , int maxIndex ) {
        int res = 0;

        for ( int i = 0; i < maxIndex; i++ ) {
            Data db = DS.getData_entry( i );
            Object v = db.getAttributeValue( Attribute.PIXEL_INFO_LIST );

            if ( v != null ) {
                IPixelInfo ipInf = ( (PixelInfoList) v ).pixel( 0 );

                if ( ipInf != null )
                    if ( ipInf.gridID() > res )
                        res = ipInf.gridID();

            }
        }
        return res;

    }

    // returns the maximum number in an array
    private int Maxx( int[] array ) {
        if ( array == null )
            return 0;
        int res = 0;

        for ( int i = 0; i < array.length; i++ )
            if ( array[ i ] > res )
                res = array[ i ];

        return res;

    }

    
    //returns the index-th value in the array ir .01f if index is improper
    private float Aval( float[] array , dimensionHandler indx ) {
    
        if ( array == null )
     
            return 0.01f;
        
        if ( array.length < 1 )
     
            return 0.01f;
        
        if ( indx == null )
     
            return 0.01f;
        if( indx.getIndex() <0)
           return 0.01f;
        if( indx.getIndex() >= array.length)
           
            return array[ array.length - 1 ];
        
        return array[ indx.getIndex() ];
        
    }
    //returns the index-th value in the array ir .01f if index is improper
    private float Aval( float[] array , int index ) {
    
        if ( array == null )
     
            return 0.01f;
        
        if ( array.length < 1 )
     
            return 0.01f;
        
        if ( index < 0 )
     
            return 0.01f;
        
        if ( index > array.length )
     
            return array[ array.length - 1 ];
        
        return array[ index ];
        
    }

    /**
     *  Currently invoked by the standard ProcessNXentry. It adds all the 
     *  data the NXmonitor class to the DataSet DS 
     *  @param DS  the DataSet that is having information added to
     *  @param NxMonitorNode The NxNode containing information about an NeXus
     *                      NXmonitor class
     *  @param startDSindex  the first GroupIndex to process
     *  @param States   The linked list of state information
     */
    public boolean setUpNXmonitorAttributes( DataSet DS , NxNode NxMonitorNode , 
        int startDSindex , NxfileStateInfo States ) {
        
        Data DB = DS.getData_entry( startDSindex );

        if ( DB == null )
            return setErrorMessage( "No Data block " + startDSindex );
        
        float x = NexUtils.getFloatFieldValue( NxMonitorNode , "distance" )
                                                            .floatValue();

        if ( !Float.isNaN( x ) )
            ConvertDataTypes.addAttribute( DB , new DetPosAttribute(
                    Attribute.DETECTOR_POS , new DetectorPosition(
                        new Position3D( new Vector3D( x , 0f , 0f ) ) ) ) );
       
        return false;
    }

    /**
     *  Currently invoked by the standard Process1Nxdata. It adds all the 
     *  data from the data field of NXdata to the DataSet DS using default
     *  GroupID's.  Units are changed to ISaw units
     *  @param DS  the DataSet that is having information added to
     *  @param NxDataNode The NxNode containing information about an NeXus
     *                      NXdata class
     *  @param startGroupID  the default GroupID for the first new DataBlock 
     *                       added by this method
     *  @param States   The linked list of state information
     */
    public boolean setUpNxData( DataSet DS , NxNode NxDataNode , 
        int startGroupID , NxfileStateInfo States ) {

        //int startDSindex = DS.getNum_entries();
        NxDataStateInfo DataInf = NexUtils.getDataStateInfo( States );

        if ( DataInf == null )
            return setErrorMessage( "No State info for NXdata " + NxDataNode.
                                                             getNodeName() );
        
        NxNode dataNode = NxDataNode.getChildNode( "data" );
     
        //int[] dimensions = DataInf.dimensions;//dataNode.getDimension();
            
        int NGroups = getNGroups( dimensions, timeDim );
     
        if ( (NGroups < 0) ||(dimensions == null)||(dimensions.length <1) )
            return setErrorMessage( " Improper Dimensions for NXdata " + 
                    NxDataNode.getNodeName() );
                             
        int length = dimensions[ dimensions.length-1-timeDim ];
        float[] data = ConvertDataTypes.floatArrayValue( dataNode.
                                                          getNodeValue() );
   
        if ( data == null ) 
            return setErrorMessage( "No data in NxData" );
        
        if ( ( DataInf.axisName == null ) || ( DataInf.axisName.length < 1 ) ) 
            return setErrorMessage( "No axis 1 information in NXnode " + 
                    NxDataNode.getNodeName() );
                      
        NxNode tofNode = NxDataNode.getChildNode( DataInf.axisName[ DataInf.XlateAxes[timeDim ]-1] );

        if ( tofNode == null )
            return setErrorMessage( "No tof axis named " + 
                 DataInf.axisName[ 0 ] + "in SetupNxData" );
                 
        float[] xvals = ConvertDataTypes.floatArrayValue( tofNode.
                                                     getNodeValue() );
        
        xvals = NexUtils.MakeHistogram( xvals , tofNode );
        String Xunits = ConvertDataTypes.StringValue(
                tofNode.getAttrValue( "units" ) ); 

        ConvertDataTypes.UnitsAdjust( xvals , Xunits , xUnits , 1.0f , 0.0f );

        NxNode errNode = NxDataNode.getChildNode( "errors" );
        float[] evals = null;
     
        if ( errNode != null ) {
            evals = ConvertDataTypes.floatArrayValue( errNode.getNodeValue() );
            //evals = MakeHistogram( evals , errNode );
            Xunits = ConvertDataTypes.StringValue(
                        errNode.getAttrValue( "units" ) ); 

            ConvertDataTypes.UnitsAdjust( evals , Xunits , xUnits , 1.0f , 0.0f );
        }
     
        if ( xvals == null )
            return setErrorMessage( "no x values" );
     
        if ( xvals.length != length )
            if ( xvals.length != length + 1 )
                return setErrorMessage( "Improper length for axis 1" );
    
        XScale xsc = new VariableXScale( xvals );
        
         int m = 0;
         for( int i=0; i< 3; i++)
            if( timeDim == m) m++;
            else if( coldim == m) m++;
            else if( rowdim ==m) m++;
        
        int[] detDigits;
        int minDetDim = dimensions.length;
        for( int i =m; i < IPNS2NexDim.length; i++){
           if( IPNS2NexDim[i] < minDetDim)
              minDetDim = IPNS2NexDim[i];
        }
        detDigits = new int[ dimensions.length ];
        java.util.Arrays.fill( detDigits,0);
        dimensionHandler hand = new dimensionHandler( dimensions, timeDim, coldim, rowdim);
        hand.resetIndex( detDigits );
        for ( int i=0; i< NGroups; i++)
        {
            int id = startGroupID ;  //will have NXdetector change groupID
            
            float TotCount =0;  
            float[] yvals = new float[ ntimes ];
            float[] errs = new float[ntimes];
            for( int j=0;  j< ntimes; j++){
               int indx =hand.getIndex();
              
               yvals[j] = data[indx];
               TotCount += yvals[j];
               if( evals != null)
                  errs[j]  = evals[indx];
               hand.IncrTime();
            }
                
            HistogramTable DB = new HistogramTable( xsc , yvals , errs , id );
            DB.setAttribute( new FloatAttribute( Attribute.TOTAL_COUNT, TotCount));
            DS.addData_entry( DB );
            
            startGroupID++;
        }  
        return false;
    }

    // returns the startGroupID + offfset entry in IDS 
    private int getGroupsID( int[] IDS , int startGroupID , int offset ) {
        if ( IDS == null )
            return startGroupID + offset;
        if ( IDS.length <= offset )
            return startGroupID + offset;
        if ( offset < 0 )
            return startGroupID + offset;
        return IDS[ offset ];
    }

   
    private boolean setErrorMessage( String err ) {
        errormessage = err;
        return true;
    }

    /**
     *  returns an errormessage or an empty string if there is no error 
     */
    public String getErrorMessage() {
        return errormessage;
    }

  

    private void setFloatAttr( Data db , String AttributeName , float[] crate ,
        int TotPos ) {
            
        if ( crate == null )
            return;
        
        if ( db == null )
            return;
        
        if ( AttributeName == null )
            return;
        
        if ( TotPos < 0 )
            return;
        
        if ( crate.length <= TotPos )
            return;
        
        db.setAttribute( new FloatAttribute( AttributeName , crate[ TotPos ] ) );

    }
    
    

     
    private int getNGroups( int[] dimension, int TimeDim ) {
    
        if ( dimension == null )
            return -1;
        
        if ( dimension.length < 1 ) 
            return -1;
        
        int NGroups = 1;

        for ( int i = 0; i < dimension.length ; i++ )
           if( i != dimension.length - TimeDim-1)
            NGroups *= dimension[ i ];
        
        return NGroups;

    }


   
  }
