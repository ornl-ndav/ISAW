package NexIO.State;

import java.util.Arrays;

import NexIO.NxNode;
import NexIO.Util.ConvertDataTypes;
import NexIO.Util.NexUtils;


public class NxEventDataStateInfo extends StateInfo
{
   
   /**
    *   The name of the NXevent node
    */
   public String Name;

   public int[] dimensions; 
   
   /**
    *   The starting Default GroupID if there is no id or detector_number field.
    */
   public int startGroupID;
   public int nGroups;
   public int startDetectorID;
   
   public int[] pixelIDs;
   public int endDetectorID;//not used unless data.label attribute in use
   /**
    *   The name of the corresponding NXdetector for this NXdata node. This
    *   comes from a link attribute on one of the axes.
    */
   public String linkName;

   /** 
    *   Constructor for the NxDataStatInfo.
    *   NOTE: Fields can be added to this structure, Other methods and/or
    *         constructors can be introduced, or subclassing can be used to
    *         account for variabilities
    *   @param  NxEventNode  An NxNode containing information on a NeXus NXevent 
    *                       class
    *   @param  NxInstrumentNode An NxNode containing information on a NeXus  
    *                       NXinstrument class
    *   @param  Params   a linked list of State information
    *   @param startGroupID the default starting ID for the data blocks. This is
    *                       used if there is NO int id field in the corresponding
    *                       NXdetector 
    */
   public NxEventDataStateInfo(NxNode NxEventNode, NxNode NxInstrumentNode)
   {

      Name = NxEventNode.getNodeName( );

      startGroupID = -1;
      startDetectorID = nGroups = -1;
      linkName = null;
      pixelIDs =null;
      //Find linkName
      for( int i = 0 ; i < NxEventNode.getNChildNodes() ; i++ ){
         
         NxNode child = NxEventNode.getChildNode( i );
         if( child.getNodeClass( ).equals( "SDS" ))
         {
            String L = ConvertDataTypes.StringValue( child.getAttrValue( "link" ) ); 
            if( L == null )
               L = ConvertDataTypes.StringValue( child.getAttrValue( "target" ) );
             
            if( L != null ){
                linkName = NxDataStateInfo.FixUp( L , child );
            }
         }
         
         
      }
      if( linkName == null)
         linkName=Name;
      if( NxInstrumentNode == null)
         return;
      NxNode NxDetector = NxInstrumentNode.getChildNode(  linkName );
      if( NxDetector ==null || !NxDetector.getNodeClass().equals( "NXdetector" ))
         return;
      
      NxNode pixIDNode = NxDetector.getChildNode( "pixel_id" );
      if( pixIDNode != null)
      
         { 
            dimensions = pixIDNode.getDimension( );
            pixelIDs =ConvertDataTypes.intArrayValue( pixIDNode.getNodeValue( ) );
         }
      if( pixelIDs != null && pixelIDs.length > 0)
      {
         int[] Res = new int[pixelIDs.length];
         System.arraycopy( pixelIDs,0,Res,0, Res.length);
         Arrays.sort( Res );
         startGroupID = Res[0];
         nGroups = Res[Res.length-1]- startGroupID+1;
      }
      
      

   }

}
