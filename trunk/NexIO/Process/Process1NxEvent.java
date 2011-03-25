package NexIO.Process;

import gov.anl.ipns.MathTools.Geometry.DetectorPosition;
import gov.anl.ipns.MathTools.Geometry.Vector3D;
import java.util.Arrays;

import DataSetTools.dataset.*;
import DataSetTools.operator.DataSet.Attribute.GetPixelInfo_op;
import NexIO.NxNode;
import NexIO.State.NxDataStateInfo;
import NexIO.State.NxDetectorStateInfo;
import NexIO.State.NxEntryStateInfo;
import NexIO.State.NxEventDataStateInfo;
import NexIO.State.NxfileStateInfo;
import NexIO.State.StateInfo;
import NexIO.Util.ConvertDataTypes;
import NexIO.Util.NexUtils;
/*
About the event NeXus field renaming. What they moved to:
event_index - unchanged
 event_pixel_id -> event_id
 event_time_of_flight -> event_time_offset
 pulse_time -> event_time_zero


*/

public class Process1NxEvent implements IProcessNxData
{

   String errorMessage ="";
   @Override
   public String getErrorMessage()
   {

      return errorMessage;
      
   }

   @Override
   public Object getNewInfo(String Name)
   {

      
      return null;
   }

   @Override
   public boolean processDS(NxNode NxEntryNode, NxNode NxEventNode,
         NxNode NxinstrumentNode, DataSet DS, NxfileStateInfo Params,
         int startGroupID)
   {
      int firstIndex = DS.getNum_entries();
      NexUtils nxut =(new NexUtils());
      int npush=0;
      if( Params == null || NxEntryNode== null || NxEventNode== null || 
            NxinstrumentNode == null || DS == null)
      { 
         errorMessage +="null inputs to process Event DS";
         return true;
      }
      
      NxEntryStateInfo EntryState = NexIO.Util.NexUtils
               .getEntryStateInfo( Params );
      if( EntryState == null ) {
         EntryState = new NxEntryStateInfo( NxEntryNode , Params, null, 
                  null, null, null );
         Params.Push( EntryState );
         npush++;
             
       }
      NxEventDataStateInfo EventState = NexIO.Util.NexUtils.getEventStateInfo( Params );
      
      if( EventState == null)
      {
         EventState = new  NxEventDataStateInfo( NxEventNode, NxinstrumentNode);
         Params.Push( EventState );
         npush++;
      }
      
      int[] pixIDs = EventState.pixelIDs;
      if( pixIDs == null)
         {
           errorMessage +="Cannot find set of pixel IDs for event Data";
           for( int pp=0; pp< npush; pp++)
              Params.Pop( );
           return true;
         }
     // long start = System.currentTimeMillis( );
      int[] times = NexUtils.getIntArrayFieldValue( NxEventNode, "event_time_offset" );
      int[] pixs = NexUtils.getIntArrayFieldValue( NxEventNode, "event_id" );
      //System.out.println("   Time(milliseconds)="+(System.currentTimeMillis( )-start));
      if( times == null || pixs == null || times.length != pixs.length)
         {
           errorMessage +="Cannot find times or pixels for events";
           for( int pp=0; pp< npush; pp++)
              Params.Pop( );
           return true;
         }
    
      int[] minMaxtimes= findMinMaxTimesChan( times);
      int[] minMaxPixIDs = findMinMaxTimesChan( pixIDs);
      if(minMaxPixIDs ==null)
      {
         errorMessage +="No times or pixels to use";
         for( int pp=0; pp< npush; pp++)
            Params.Pop( );
         return true;
      }
      double incrTimeUnit = 1;
      String units = NexUtils.getStringAttributeValue( 
                 NxEventNode.getChildNode(  "event_time_offset" ) ,"units" );
      if( units != null)
         incrTimeUnit = ConvertDataTypes.getUnitMultiplier( units , "us" );
      
      int[] buff = new int[2000];
      if(minMaxtimes == null)
      {
         minMaxtimes[0]=0;
         minMaxtimes[1] =1;
      }else if( minMaxtimes[0] == minMaxtimes[1])
         minMaxtimes[1]++;
      int nBins = pixs.length/pixIDs.length/4;
      nBins = Math.max( nBins,2000 );
      XScale xscl = new UniformXScale( minMaxtimes[0]*(float)incrTimeUnit,
                                       minMaxtimes[1]*(float)incrTimeUnit,
                                       Math.min( nBins, minMaxtimes[1]-minMaxtimes[0]+1)) ;
      int[] sizes = new int[minMaxPixIDs[1]-minMaxPixIDs[0]+1];
      Arrays.fill( sizes , 0 );
      int minPixID = minMaxPixIDs[0];
      for( int i=0; i< pixs.length; i++)
      {
         if( pixs[i] >= minPixID && pixs[i] <=minMaxPixIDs[1])
            sizes[pixs[i]-minPixID]++;
      }
      int[][] Data = new int[sizes.length][];
      for( int i=0; i< Data.length; i++)
         Data[i] = new int[sizes[i]];

      Arrays.fill( sizes , 0 );
      for( int i=0; i< pixs.length; i++)
      {
         int k = pixs[i]-minPixID;
         if ( k >= 0 && k < sizes.length )
         {
            Data[k][sizes[k]] = times[i];
            sizes[k]++ ;

         }
      }
      
      float TTcount =0;
      for( int i=0; i< pixIDs.length; i++)
      {
         int p = pixIDs[i];
         int k= p-minPixID;
         int N=sizes[k];
         
          
         int[] counts = new int[Data[k].length];
         Arrays.fill( counts , 1 );
         int[] Bres = Data[k];
         EventList EList = new EventList(0, 
                           (double)incrTimeUnit, Bres, counts);
         
         EventData data = new EventData(xscl, EList, p);
         data.setAttribute(  new FloatAttribute(Attribute.TOTAL_COUNT, N) );
         TTcount += N;
         DS.addData_entry( data );
         
      }
      
      float Tcount = AttrUtil.getTotalCount( DS );
      if( Tcount < 0 || Float.isNaN( Tcount ))
          Tcount =0;
      TTcount +=Tcount;
      DS.setAttribute( new FloatAttribute( Attribute.TOTAL_COUNT, TTcount) );

      NxNode NxDetectorNode = NexUtils.getCorrespondingNxDetector(
            EventState.linkName , NxinstrumentNode );

      NxDetectorStateInfo DetState = null;
      DetState = NexUtils.getDetectorStateInfo( Params );
      if ( NxDetectorNode == null )
      {
         DataSetTools.util.SharedData.addmsg( "no NxDetector Node for "
               + NxEventNode.getNodeName( ) );

      } else if ( DetState == null )
      {
         DetState = new NxDetectorStateInfo( NxDetectorNode , Params );
         Params.Push( DetState );
         npush++ ;
      }
      boolean res = false;
      if( ( NxDetectorNode != null ) )
      {
         res = setUpNXdetectorAttributes( DS , NxEventNode ,
                  NxDetectorNode , firstIndex , Params );
         if( res)
            errorMessage += nxut.getErrorMessage( );
      }
      for( int pp=0; pp< npush; pp++)
         Params.Pop( );
      return res;
   }

   private int[] findMinMaxTimesChan( int[] times)
   {
      if( times == null || times.length < 1)
         return null;
      int min =times[0];
      int max = min;
      for( int i=1; i< times.length; i++)
         if( times[i] < min)
            min = times[i];
         else if( times[i]> max)
            max = times[i];
      int[] Res = new int[2];
      Res[0]= min;
      Res[1]=max;
      return Res;
   }
   // Enough different from NXdata to rewrite
   private boolean setUpNXdetectorAttributes( DataSet DS, NxNode NxEventNode,
         NxNode NxDetectorNode, int firstIndex, NxfileStateInfo Params)
   {
      NxEntryStateInfo  EntryState = NexUtils.getEntryStateInfo( Params );
      NxDetectorStateInfo DetState = NexUtils.getDetectorStateInfo( Params );
      NxEventDataStateInfo EventState = NexUtils.getEventStateInfo( Params );
      if( EventState == null || DetState==null|| EntryState== null ||
            DS == null || NxEventNode == null || NxDetectorNode == null ||
            Params == null)
      {
         errorMessage +="Missing Info for Detector";
         return true;
      }
      
      int[] pixelDims = EventState.dimensions;
      int[] detDims  = getDetectorDims( NxDetectorNode);

      int Nrows = getNrows( pixelDims,detDims);
      int Ncols = getNcols( pixelDims,detDims);
      int NDetectors = getNDetectors( detDims, Nrows,Ncols);   
      
      float[] crate = getFieldVal( NxDetectorNode , "crate" ,detDims,-1);
      float[] slot = getFieldVal( NxDetectorNode , "slot" ,detDims,-1);
      float[] input = getFieldVal( NxDetectorNode , "input" ,detDims,-1);
      float[] solAng = getFieldVal( NxDetectorNode ,
               "solid_angle" ,detDims,-1);
      
      float[] width = getGeometry("width", detDims, NxDetectorNode, DetState);
      float[] length = getGeometry( "length",detDims, NxDetectorNode, DetState);
      float[] depth = getGeometry( "depth",detDims, NxDetectorNode, DetState);
      float[] orientationxx = getGeometry( "orientationxx",detDims, NxDetectorNode, DetState);
      float[] orientationxy = getGeometry( "orientationxy",detDims, NxDetectorNode, DetState);
      float[] orientationxz = getGeometry( "orientationxz",detDims, NxDetectorNode, DetState);
      float[] orientationyx = getGeometry( "orientationyx",detDims, NxDetectorNode, DetState);
      float[] orientationyy = getGeometry( "orientationyy",detDims, NxDetectorNode, DetState);
      float[] orientationyz = getGeometry( "orientationyz",detDims, NxDetectorNode, DetState);
      
      // get diameter, etc.
      
      float[] distance = getFieldVal( NxDetectorNode , "distance" ,detDims,-1);
      float[] azimuthal = getFieldVal( NxDetectorNode , "azimuthal_angle",detDims,-1 );
      float[] polar = getFieldVal( NxDetectorNode , "polar_angle" ,detDims,-1);
      
      int[] detIDs = NexUtils.getIntArrayFieldValue( NxDetectorNode , "detector_number" );
      if( detIDs != null && detIDs.length != NDetectors)
         detIDs =null;
      
      int det=0;
      int row =0;
      int col =0;
      IDataGrid grid = null;
      int detID =EventState.startDetectorID;
      if( detIDs != null)
         detID = detIDs[det];
      
      for( int d = firstIndex; d < DS.getNum_entries( ); d++)
      {
         Data db = DS.getData_entry( d );
         
         ConvertDataTypes.addAttribute(db,
            ConvertDataTypes.CreateFloatAttribute(  Attribute.CRATE ,
                                                    ArrVal(crate,det)  ));
         ConvertDataTypes.addAttribute(db,
               ConvertDataTypes.CreateFloatAttribute(  Attribute.INPUT , 
                                                       ArrVal(input,det)  ));
         ConvertDataTypes.addAttribute(db,
               ConvertDataTypes.CreateFloatAttribute(  Attribute.SLOT , 
                                                       ArrVal(crate,det)  ));
         ConvertDataTypes.addAttribute(db,
               ConvertDataTypes.CreateFloatAttribute(  Attribute.SOLID_ANGLE ,
                                                     ArrVal(solAng,det)  ));
       
       
         try
         {
        if( row ==0 && col==0)// get New Grid
            if ( distance == null || detDims == null )//No consistent distances
               grid = null;
            else
            {  

               
               
               if ( grid != null )
               {
                  grid.setData_entries( DS );

                 // Grid_util.setEffectivePositions( DS , grid.ID( ) );
               }

               if ( detDims.length == pixelDims.length && detDims.length > 1 )
               {// use RowColGrid
                  grid = new RowColGrid( Nrows , Ncols , detID );
               } else
               {// Use UniformGrid
                  Vector3D center = new Vector3D(
                        ConvertDataTypes.convertToIsaw( ArrVal( distance , det ,
                              1 ) , ArrVal( polar , det , 0 ) , ArrVal(
                              azimuthal , det , 0 ) ) );
                  Vector3D xvec = new Vector3D(
                        ArrVal( orientationxz , det , 1 ) , ArrVal(
                              orientationxx , det , 0 ) , ArrVal(
                              orientationxy , det , 0 ) );
                  Vector3D yvec = new Vector3D(
                        ArrVal( orientationyz , det , 0 ) , ArrVal(
                              orientationyx , det , 1 ) , ArrVal(
                              orientationyy , det , 0 ) );
                  grid = new UniformGrid( detID , "m" , center , xvec , yvec ,
                        ArrVal( width , det , .5f ) , ArrVal( length , det ,
                              .5f ) , ArrVal( depth , det , .02f ) ,
                        ( short ) Nrows , ( short ) Ncols );
               }
               det++;
               if( detIDs != null)
                 detID = detIDs[det];
               else
                 detID++;
            }
         }catch(Exception s)
         {
            grid = null;
            det++;
            if( detIDs != null)
              detID = detIDs[det];
            else
              detID++;
         }
         if ( grid != null )
         {
            PixelInfoList PixList = new PixelInfoList( new DetectorPixelInfo(
                  db.getGroup_ID( ) , ( short )( row +1) , ( short ) (col+1) , grid ) );

            PixelInfoListAttribute pix = new PixelInfoListAttribute(
                  Attribute.PIXEL_INFO_LIST , PixList );
            db.setAttribute( pix );
            if( grid instanceof RowColGrid)//Have to set detector position of Data element
            {
                 Vector3D center = new Vector3D(
                     ConvertDataTypes.convertToIsaw( ArrVal( distance , d ,
                           1 ) , ArrVal( polar , d , 0 ) , ArrVal(
                           azimuthal , d , 0 ) ) );
                  db.setAttribute( new DetPosAttribute(Attribute.DETECTOR_POS,
                        new DetectorPosition( center)));
            }
            
         }
         
         
         
         
      //Next datablock row,col, det values
         row++;
         if( row >=Nrows)
         {
            row=0;
            col++;
            if( col >= Ncols)
            {
               col =0;
              // det++;
              // if( detIDs != null)
              //    detID = detIDs[det];
             //  else
              //    detID++;
            }
         }
         
      }
      if( grid != null)
      {
        grid.setData_entries( DS );
     

       if( !(grid instanceof RowColGrid))
          Grid_util.setEffectivePositions( DS ,grid.ID() );
       
       DS.addOperator(  new GetPixelInfo_op() );
      }
   
      
      return false;
   }
   
   private int getNDetectors( int[] detDims, int Nrows, int Ncols)
   {
      if( detDims == null)
         return 0;
      
      int Prod = 1;
      for( int i=0; i< detDims.length;i++)
         Prod*=detDims[i];
      
      return Prod/Nrows/Ncols;
   }
   private int match(int[] pixelDims, int[] detDims) 
   {
      if( pixelDims == null || detDims == null)
         return -1;
      
      for( int i=0; i< Math.min( pixelDims.length , detDims.length );i++)
         if( pixelDims[i] != detDims[i])
            return i;
      
      return Math.min( pixelDims.length , detDims.length );
   }
   
   private int getNrows( int[] pixelDims, int[] detDims)
   {
     if( pixelDims == null || detDims == null||pixelDims.length==0 
           || detDims.length==0)
        return 1;
     if( pixelDims.length < detDims.length)
        return 1;
     
     int M = match(pixelDims,detDims);
     if( M < 0 || M != detDims.length|| M+2 < pixelDims.length)
        return 1;
     
     if( pixelDims.length != detDims.length)
     {
        int nrows=1;
        if( detDims.length+2 == pixelDims.length)
           nrows = pixelDims[pixelDims.length-1];
        return nrows;
        
     }else if( detDims.length ==1)
        
        return 1;
     
     else
        return detDims[detDims.length-1];
   }
   

   private int getNcols( int[] pixelDims, int[] detDims)
   {

      if( pixelDims == null || detDims == null||pixelDims.length==0 
               || detDims.length==0)
         return 1;
      
      if( pixelDims.length < detDims.length)
         return 1;
      
      int M = match(pixelDims,detDims);
      if( M < 0 || M != detDims.length || M+2<pixelDims.length)
         return 1;
      
      if( pixelDims.length != detDims.length)
      {
         int ncols=1;
         if( M < pixelDims.length)
            ncols = pixelDims[M];
         
         return ncols;
         
      }else if( detDims.length ==1)
         
         return 1;
      
      else
         
         return detDims[detDims.length-2];
   }
   
   private int[] getDetectorDims( NxNode NxDetectorNode)
   {
      int[] [] Dims = new int [8][];
      String[] fields = {"crate","slot","input","solid_angle","distance",
                          "polar_angle","azimuthal_angle"};
     NxNode NN= null;
     for( int i=0;i<7;i++)
     {
        NN = NxDetectorNode.getChildNode(fields[i]);
        if( NN== null)
           Dims[i]= null;
        else
           Dims[i] = NN.getDimension( );
     }
     NN= NxDetectorNode.getChildNode( "geometry" );
     Dims[7]=null;
     if( NN== null || !NN.getNodeClass().equals( "NXgeometry" ))
        Dims[7]= null;
     else
     {
       NxNode Shape = null;
       for( int i=0; i< NN.getNChildNodes( )&& Shape== null;i++)
       {
          NxNode child = NN.getChildNode( i );
          if( child.getNodeClass( ).equals( "NXshape" ))
             Shape = child;
       }
       if(Shape != null)
       {
          Shape = Shape.getChildNode( "size" );
          int[] D = Shape.getDimension( );
          Dims[7]= new int[D.length-1];
          System.arraycopy( D,0,Dims[7],0,Dims[7].length);
       }
     }
          
       //All non-null Dims should be the same. Otherwise pick longest
       // or return null if leading dims do not match.
       int[] Res = new int[20];
       int N=0;
       for( int i=0; i< Dims.length; i++)
          if( Dims[i] != null)
             if( N==0)
             {
                System.arraycopy( Dims[i],0,Res,0, Dims[i].length);
                N= Dims[i].length;
             }else
             {
                //check if leading digits match
                boolean match = true;
                for( int j=0; j< Math.min(N,Dims[i].length);j++)
                   if( Dims[i][j] != Res[j])
                      match = false;
                if( !match)
                   return null;
                if( Dims[i].length > N)
                {
                   System.arraycopy( Dims[i],0,Res,0, Dims[i].length);
                   N= Dims[i].length;
                }
             }
       int[] RR = new int[N];
       System.arraycopy( Res,0,RR,0,N);
       return RR;
       
    
   }
   private Float ArrVal( float[] arr, int indx)
   {
      if( arr == null || indx <0 || indx >= arr.length)
         return null;
      return new Float(arr[indx]);
   }
   
   private float ArrVal( float[] arr, int indx, float def)
   {
      if( arr ==null || indx<0|| indx >= arr.length)
         return def;
      return arr[indx];
   }
   private float[] getFieldVal( NxNode node, String field, int[] targ_dimensions, int index)
   {
      if( node == null || field == null || targ_dimensions == null)
         return null;
      
      NxNode child = node.getChildNode(field);
      if( child == null)
         return null;
      int[] Fdims = child.getDimension( );
      return rePack( ConvertDataTypes.floatArrayValue( child.getNodeValue( ) ),
                 Fdims, targ_dimensions, index);
   }
   /**
    * Gets Detector geometry 
    * @param field       width, length,depth, orientationx, orientationy,orientationz
    * @param dimensions   dimensions to fill resultant arrays to
    * @param DetectorNode The detector node
    * @param DetState      The detector state
    * @return     a linear array with the correct data to the given dimensions or null
    */
   private float[] getGeometry( String field,int[] dimensions, NxNode DetectorNode, NxDetectorStateInfo DetState)
   {
    
      if( DetState.NxGeometryName == null)
      {
         return null;
      }else
      {
         
         NxNode Geom = DetectorNode.getChildNode( DetState.NxGeometryName );
         NxNode Shape=null,
                orientation = null;
         for( int i=0; i< Geom.getNChildNodes( ) && (orientation == null || Shape==null); i++)
         {
            NxNode NN = Geom.getChildNode( i );
            if(Shape == null && NN.getNodeClass().equals( "NXshape" ) )
               Shape = NN;
            else if( orientation == null && NN.getNodeClass( ).equals("Nxorientation"))
               orientation = NN;
         }
         
         if( field.startsWith("orientation"))
            return getOrientationGeom( field, orientation, dimensions);
         
         if( !Shape.getNodeName().equals("nxbox"))
            return null;
         
         NxNode sizeNode = Shape.getChildNode( "size" );
         int[] dimSizeNode =sizeNode.getDimension( );
         if( sizeNode == null)
            return null;
         float[] size = ConvertDataTypes.floatArrayValue( sizeNode.getNodeValue() );
         
         if( size == null)
            return null;
         int indx =0;
         if( field.equals("width"))
            indx =1;
         else if( field.equals( "length" ))
            indx = 2;
         
         return rePack( size, dimSizeNode, dimensions, indx);
         
         
         
         
         
      }
     
   }
   
   
   private float[] getOrientationGeom( String field, NxNode orientation, int[] dimensions)
   {
      int indx =0;
      if( field.endsWith("xy"))
         indx =1;
      else if( field.endsWith("xx"))
         indx =2;
      else if( field.endsWith("yx"))
         indx =3;
      else if( field.endsWith("yy"))
         indx =4;
      else if( field.endsWith("yz"))
         indx =5;
      
      NxNode V = orientation.getChildNode( "value" );
      if( V == null)
         return null;
      int[] Adims = V.getDimension();
      
      float[] array = ConvertDataTypes.floatArrayValue( V.getNodeValue() );
      if( array == null)
         return null;
      
      
      return rePack(array,Adims, dimensions, indx);
   }
   /**
    * 
    * @param array       The array to be rePacked, filled with 0's with undef data
    * @param arrayDims   The dimension of the array
    * @param targeDimensions  The target dimensions
    * @param indx             if non negative, the last tuple will only extract indx element
    * @return      A linear array filled with 0's or repeats so match target Dimension
    */
   private float[] rePack( float[] array, int[] arrayDims, int[] targetDimensions, int indx)
   {
      if( array == null || arrayDims == null || targetDimensions == null )
         return array;
      
      boolean same =  arrayDims.length == targetDimensions.length;
      
      if( same)
         for( int i=0; i< arrayDims.length; i++)
            if( arrayDims[i]!= targetDimensions[i])
               same = false;
      
      if( same )
         return array;
      
      int DD = arrayDims.length -targetDimensions.length;
      if( DD >1 || (DD==1 && indx <0) || (DD==0 && indx>=0))
         return array;
      
      int newLength =1;
      for( int i=0; i< targetDimensions.length; i++)
         newLength *=targetDimensions[i];
      
      float[] Res = new float[newLength];
      
      int[] tuple = new int[targetDimensions.length];
      Arrays.fill( tuple , 0 );
      int ka=0;
      int kt =0;
      
      int Indx =0;
      int Step =1;
      if( indx >=0)
      {
         Indx = indx;
         Step = arrayDims[arrayDims.length-1];
         
      }
      while( tuple[0]< targetDimensions[0])
      {
         boolean zero= false;
         for( int kk=0; kk< tuple.length && !zero; kk++)
            if(kk>=arrayDims.length || tuple[kk]>=arrayDims[kk])
               zero=true;
         if( zero)
         {
            Res[kt]=0;
         }else
         {   Res[kt] = array[ka+Indx];
             ka+=Step;
         }
      
         kt++;
         
         //Next element
         boolean done = false;
         for(int kk=tuple.length-1; kk >=0&& !done; kk--)
         {
            tuple[kk]++;
            if( tuple[kk]>=targetDimensions[kk])
               tuple[kk] =0;
            else
               done = true;
         }
      }
      return Res;
   }
   @Override
   public void setNewInfo(String Name, Object value)
   {

   }
   
   

}
