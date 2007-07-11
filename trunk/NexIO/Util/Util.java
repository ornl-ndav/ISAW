package NexIO.Util;


//import java.lang.reflect.Array;
import java.util.Arrays;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import NexIO.*;
//import NexIO.Process.Process1NxData;
import NexIO.State.*;

public class Util {

   public Util() {
      super();
      // TODO Auto-generated constructor stub
   }

   private static String standardize_dot_sep_list( String S ){
      
      if( S == null )
         return null;
      
      S = S.trim();
      if( S.length() < 1 )
         return null;
      
      if( !S.endsWith( "." ) )
         S  += ".";
      
      if( !S.startsWith( "." ) )
         S = "." + S;
      
      if( S.length() <= 2 )
         return null;
      
      //eliminate all dots
      boolean  nonDotFound = false;
      for( int i=0; i< S.length() && !nonDotFound; i++)
         if( S.charAt(i)!='.')
            if( S.charAt(i) !=' ')
               nonDotFound = true;
      if( !nonDotFound)
         return null;
      return S;
      
   }
   
   /**
    * Utility method to get starting( at NXentry) nodes of interest in an xmlDocument
    * @param xmlDoc  The top node of an xmldocument in the proper format
    * @param EntryName  The name of the NXentry node, for those NeXus files with
    *                   several NXentries
    * @param filename   The filename of the nexus file. It can include the path
    * @return  An array of 4 Nodes, some may be null, giving Start of NXentries of
    *          interest.  The Common without and with entry name and the Runs with
    *          given filename without and with the entry name
    */
   public static Node[] getxmlNXentryNodes( Node xmlDoc, String EntryName, String filename){
      Node[] Res = new Node[4];
      Res[0]=Res[1]=Res[2]=Res[3] = null;
      Node N = Util.getNXInfo( xmlDoc,"Common.NXentry",null,null,null);
      while( N != null ) {
         if( N.getNodeName().equals( "NXentry" ) ) {
            String name = ConvertDataTypes.StringValue( Util
                     .getXmlNodeAttributeValue( N , "name" ) );
            if( name == null )
               Res[ 0 ] = N;
            else if( ( EntryName != null ) && ( name.equals( EntryName ) ) )
               Res[ 1 ] = N;
         }
         N = N.getNextSibling();
      }
      if( filename == null)
         return Res;
      filename = filename.replace( '\\','/');
      
      int kk= filename.lastIndexOf('/');
      if( kk >=0)
         filename = filename.substring( kk+1);
      N= Util.getNXInfo( xmlDoc,"Runs", null, null, null);
      N= Util.getNXInfo( N,"Run.NXentry", null, null, filename);
      while( N != null ) {
         if( N.getNodeName().equals( "NXentry" ) ) {
            String name = ConvertDataTypes.StringValue( Util
                     .getXmlNodeAttributeValue( N , "name" ) );
            if( name == null )
               Res[ 2 ] = N;
            else if( ( EntryName != null ) && ( name.equals( EntryName ) ) )
               Res[ 3 ] = N;
         }
         N = N.getNextSibling();
      }
      return Res;
      
   }
   
   /**
    * Finds information in an XML document
    * @param xmlDoc  The top node( or what is left) of a DOM document
    * @param NXclassPath   A dot separated list of Nexus Classes. These appear as <NXentry  name="...
    *                        in the XML file. Only those parts of the xml document will be searched.
    *                        This can be null for all NeXus classnames to be listed
    * @param NXclassNameList The name of the NeXus Class. The name is in the name= attribute. This also can
    *                        be null( all will be considered) , or a set
    * @param fieldName      The specific field name( tag or name attribute) to search for. No Dots
    * @param filename       The tag of the node must have a filename attribute corresponding to this filename 
    * @return     The top node or the value if only one simple child
    */
   public static Node getNXInfo( Node xmlDoc , String NXclassPath , String NXclassNameList , String fieldName ,
            String filename ){
      
        if( xmlDoc == null )
           return null;
        
        NXclassPath = standardize_dot_sep_list( NXclassPath );
        NXclassNameList = standardize_dot_sep_list( NXclassNameList );
        //fieldName = standardize_dot_sep_list( fieldName);
        if(filename != null && filename.length() < 1)
           filename = null;
        if( fieldName != null && fieldName.length() < 1)
           fieldName= null;
        NodeList children = xmlDoc.getChildNodes();
        if( children.getLength() ==1)if("data".equals(children.item(0).getNodeName())){
           xmlDoc = children.item(0);
           children = xmlDoc.getChildNodes();
        }
        
        
        if( ( NXclassPath == null ) &&( NXclassNameList == null )&&
                                  ( fieldName == null )&&( filename == null ) )
                return xmlDoc;
        
       
           
        for( int ik = 0 ; ik < children.getLength() ; ik++  ){
           
            Node NN = children.item( ik );
            int k = -1;
            if( NXclassPath != null )
               k = NXclassPath.indexOf( "." +  NN.getNodeName() + "." );
            
            String ThisNodeName = null;//Non-null only if matches the match nodeName
            String ThisNodeFileName = null;//Non-null only if matches the match match filename
            boolean ClassHasName = false;
            boolean ClassHasFile = false;
            NamedNodeMap atts = NN.getAttributes();
            if( NXclassNameList != null ) {


              String NodeName2Match = NXclassNameList.substring( 1 , NXclassNameList
                        .indexOf( "." , 1 ) );
              
              if( NodeName2Match != null)
                 NodeName2Match= NodeName2Match.trim();
              if( NodeName2Match.length()<1)
                 NodeName2Match= null;

               if( NodeName2Match != null )
                  if( NodeName2Match.trim().length() > 0 )
                     if( atts != null ) {

                        Node attNode = atts.getNamedItem( "name" );
                        if( attNode != null ) ClassHasName = true;
                        if( attNode != null )
                          if( NodeName2Match.equals( attNode.getNodeValue() ) )
                           ThisNodeName = NodeName2Match;
                     }
            }

         if( (filename != null) &&( atts != null) ) {

            Node attNode = atts.getNamedItem( "filename" );
            if( attNode != null )
               if( filename.equals( attNode.getNodeValue() ) )
                  ThisNodeFileName = "xxx";

            if( attNode != null )
               ClassHasFile = true;

         }
              
            
             
             boolean OkToEnter = false;// are there child nodes to search??
             if( ( NXclassPath !=null )||( NXclassNameList != null ))
                if( filename == null ){
                   
                   if( k == 0 )if( ThisNodeName != null ) 
                      OkToEnter = true;
                   if( k == 0 )if((ThisNodeName == null)) 
                      OkToEnter = true;
                   
                   if( k == 0 )if( !ClassHasName )  
                      OkToEnter = true;
                   
                   if( NXclassPath == null )if( ThisNodeName != null )  
                      OkToEnter = true;
                   
                   if( NXclassPath == null )if( !ClassHasName )  
                      OkToEnter = true;
                   
               }else{
                  
                  if( k == 0 )if( ThisNodeName != null )if( (ThisNodeFileName != null ) ||
                                                      !ClassHasFile ) 
                     OkToEnter = true;
                  
                  if( k == 0 )if(( NXclassNameList == null ) )
                        if( ( ThisNodeFileName != null ) ||!ClassHasFile ) 
                             OkToEnter = true;
                  
                  if( k == 0 )if( !ClassHasName )
                     if( (ThisNodeFileName != null ) ||!ClassHasFile ) 
                        OkToEnter = true;
                  
                  if( NXclassPath == null )if( ThisNodeName != null )
                        if( ( ThisNodeFileName != null ) ||!ClassHasFile )  
                          OkToEnter = true;
                  
                  if( NXclassPath == null )if( !ClassHasName )
                     if( ( ThisNodeFileName != null ) ||!ClassHasFile )  
                        OkToEnter = true;
                  
               }
                 
            if(  OkToEnter ){//Has child nodes to search
                             //REDO How about adding a variable changed. if any of the other stuff changes 
                             //     Woops go deeper cause child node and not done
               
              String Clist = null , 
                     CNameList = null;
              
              if( k >= 0 ){
                 k = NXclassPath.indexOf( '.' , k + 1 );
                 if( k >= 0 ) 
                    Clist = NXclassPath.substring( k );
              }
              
              if( ThisNodeFileName != null )
                 filename = null;
              
              if( ThisNodeName != null ){
                 k = NXclassNameList.indexOf( "." +  ThisNodeName + "." );
                 if( k >= 0  ){
                    k = k + 2 + ThisNodeName.length();
                    CNameList = NXclassNameList.substring( k );
                 }
              }else 
                 CNameList = NXclassNameList;
              
              
              Node X = getNXInfo( NN , Clist , CNameList , fieldName , filename );
              
              if( X != null )
                 return X;
              
            }else if( ( NXclassPath != null )|| ( NXclassNameList != null ) ){//Check for fieldName
               
               Node X =getNXInfo( NN , NXclassPath , NXclassNameList , fieldName , filename );
               if( X != null )
                  return X;
               
            }else{ //Do not check child nodes. Check if this fits
               
               if( fieldName != null )if( fieldName.equals( NN.getNodeName() ) )
                   return  NN;
               
               atts = NN.getAttributes();
               if( atts != null ){
                  
                  Node attNode = atts.getNamedItem( "name" );
                  if( fieldName != null )
                  if( attNode != null )if( fieldName.equals( attNode.getNodeValue() ) )
                     return   NN ;
                  
                  attNode = atts.getNamedItem( "filename" );
                  if( ( filename != null ) && ( attNode != null ) )
                     if( filename.equals( attNode.getNodeValue() ) )
                          return  NN ;
                  
               }
               if( ( fieldName == null ) && ( filename == null ) )
                  return NN;
               
               Node X =getNXInfo( NN , NXclassPath , NXclassNameList , fieldName , filename );
               if( X != null )
                  return X;
               
            }
         
        }//for each child
      
      
      
      return null;
      
   }
   
   /**
    * Finds information in an XML document
    * @param xmlDoc  The top node( or what is left) of a DOM document
    * @param NXclassPath   A dot separated list of Nexus Classes. These appear
    *                      as <NXentry  name="...in the XML file. Only those 
    *                      parts of the xml document will be searched.
    *                      This can be null for all NeXus classnames to be 
    *                      searched
    *                      
    * @param NXclassNameList The name of the NeXus Class. The name is in the  
    *                        name= attribute. This also can  be null( all will
    *                         be considered) , or a dot separated set of names
    *                         (two consecutive dots mean anything, - means no name
    *                         ).  NOTE: the names MUST correspond to the Classes in 
    *                          NXclassPath
    *                          
    * @param fieldName      NOT USED The specific field name( tag or name 
    *                       attribute) to search for. No Dots. Include this 
    *                       at the end of NXclassPath if a class name or at 
    *                       the end of NXclassNameList if a name of a class.
    *                       
    * @param filename       The tag of the node must have a filename attribute
    *                        corresponding to this filename 
    *                        
    * @return     The first node or [sub]node satisfying the parameters
    * 
    * NOTE: Use getLeafNodeValue to get the value of the node if it is a leaf.
    *    
    */
   public static Node getNXInfo1( Node xmlDoc , String NXclassPath , String NXclassNameList , String fieldName ,
            String filename ){
      
        if( xmlDoc == null )
           return null;
        
        NXclassPath = standardize_dot_sep_list( NXclassPath );
        NXclassNameList = standardize_dot_sep_list( NXclassNameList );
        
        if(filename != null && filename.length() < 1)
           filename = null;
        
        NodeList children = xmlDoc.getChildNodes();
        if( children.getLength() ==1)if("data".equals(children.item(0).getNodeName())){
           xmlDoc = children.item(0);
           children = xmlDoc.getChildNodes();
        }
        
        
        if( ( NXclassPath == null ) &&( NXclassNameList == null )&&
                                  ( fieldName == null )&&( filename == null ) )
                return xmlDoc;
        
       
           
        for( int ik = 0 ; ik < children.getLength() ; ik++  ){
           
            Node NN = children.item( ik );
            String NextNXclassPath = NXclassPath;
            String NextNXclassNameList = NXclassNameList;
            String Nextfilename = filename;
           
            if( NXclassPath != null )
               if( NXclassPath.startsWith(".."))
                  
                  NextNXclassPath = NXclassPath.substring(1);
            
               else if(NXclassPath.indexOf( "." +  NN.getNodeName() + "." )==0)
                  NextNXclassPath = NXclassPath.substring( NXclassPath.indexOf(".",1));
            
            
            NamedNodeMap atts = NN.getAttributes();                              
            if( NXclassNameList != null && atts != null) 
            if( NXclassNameList.startsWith(".."))
                     NextNXclassNameList = NXclassNameList.substring(1);
            else {

                   Node attNode = atts.getNamedItem( "name" );
                  if( attNode == null && NXclassNameList.startsWith(".-."))
                     NextNXclassNameList = NXclassNameList.substring(3); 
                  else if( attNode != null  && NXclassNameList.indexOf("."+
                                             attNode.getNodeValue()+".")==0 )
                         NextNXclassNameList  = NXclassNameList.substring(
                                  NXclassNameList.indexOf(".",1));
                   
            }

         if( (filename != null) &&( atts != null) ) {

            Node attNode = atts.getNamedItem( "filename" );
            if( attNode != null )
               if( filename.equals( attNode.getNodeValue() ) )
                  Nextfilename = null;
         }
              
            
           
              Node X = null;

              if( NXclassPath == null || NXclassNameList == null || (NXclassPath != NextNXclassPath &&
                          NXclassNameList != NextNXclassNameList    ))// only pop name list
                                                                        //if class popped       
                     X = getNXInfo1( NN , NextNXclassPath , NextNXclassNameList , fieldName , 
                            Nextfilename );
              else     
                 X = getNXInfo1( NN ,NXclassPath , NXclassNameList , fieldName , 
                          Nextfilename );
                 
              
              if( X != null )
                 return X;
         
        }//for each child
      
      
      
      return null;
      
   }
   
   /**
    * Returns the String value of the given attribue of an xml node
    * 
    * @param NN   The xml node in question
    * @param AttrName  The name of the attribute
    * @return   The string representation of the value or null if 
    *       it is not possible to find this value
    */
   public static String getXmlNodeAttributeValue( Node NN, String AttrName){
      if( NN == null )
         return null;
      if( AttrName == null)
         return null;
      if( AttrName.length() <1)
         return null;
      NamedNodeMap  atts = NN.getAttributes();
      if( atts != null ){
         
         Node attNode = atts.getNamedItem( AttrName );
         if(  attNode != null)
           return attNode.getNodeValue();
         return null;
        
      }else
         return null;
      
   }
   
   
  /**
   * Gets the value of an xml node. This node must only have
   * text child nodes.  The concatenation of these text nodes
   * is returned
   * 
   * @param NN  The xml node in question
   * 
   * @return  The concatenation of all the text child nodes or
   *          null if a child node is not a text node, or NN
   *          is null.
   *
   */
    //Must have one child that is a text node
   // unions the values of all text nodes
   public static Object getLeafNodeValues( Node NN ){
       
       if( NN == null )
          return null;
       
      NodeList children = NN.getChildNodes();
      String Res = "";
      for( int i = 0 ; i< children.getLength() ; i++ ){
         
         Node tNode = children.item( i );
         
         if( !( tNode instanceof Text ) )
            return null;
         Res += tNode.getNodeValue();
         
      }
      
       return Res;
       
   }
 
    
    
    
    
    /**
     * Returns an adjusted dimension node where * fields are replaced by -1,
     * the time dim is replaced by -2 if not in the  trail.  The axis numbers
     * (for *) are fixed uing NxDataStateInfo.XlateAxes. axes attribute is
     * NOT used.
     * 
     * @param  node  The node in which to get the fixed up dimension
     * @param  dataState  the stateInfo for the NXdata. Gives dimensions and 
     *                    translations
     * @param timeDim the position in dimension from the NeXus getDimension 
     *                that represents time.  Position 0 is the rightmost
     * @param colDim the position in dimension from the NeXus getDimension 
     *                that represents column.  Position 0 is the rightmost
     * @param rowDim the position in dimension from the NeXus getDimension 
     *                that represents row.  Position 0 is the rightmost
     *    
     *                    
     * @param  axisOffset  the number of rightmost dimensions to ignore from 
     *           the node.getDimension
     * 
     *        
     * @return  The fixed up dimension that takes into account axes .
     *           -1 at a position means *.
     * NOTE: rowDims and colDim are not used yet(?). The position corresponding
     *      to the timeDim will have a -2 in it.
     */
    public static dimensionHandler GetDimension( NxNode node , NxDataStateInfo dataState , 
                                                   int timeDim , int colDim, int rowDim, int axisOffset){
        
         if( node == null )
            return null;
         
         int[] dim1 = node.getDimension();
         if( dim1 == null )
            return null;
         
         if( dim1.length - axisOffset < 0 )
            return null;
        
         int[] dim = new int[ dim1.length - axisOffset ];
        
         System.arraycopy( dim1 , 0 , dim , 0 , dim.length );
         
         int[] axes = null;
         int ax = ConvertDataTypes.intValue( node.getAttrValue( "axis" )  );
         
         if( ax != Integer.MIN_VALUE ){
            
            axes = new int[ 1 ];
            if( dataState.XlateAxes != null )
               ax = dataState.XlateAxes[ ax - 1 ];
            axes[ 0 ] = ax;
            
         }else{// check fro axes attributes
            
            String S =( String )node.getAttrValue( "axes" );
            if( S != null )if( S.length() > 0 ){
               
               S = S.trim();
               if( S.startsWith( "[" ) )
                  S = S.substring( 1 );
               
               if( S.endsWith( "]" ) )
                  S = S.substring( 0 , S.length() - 1 );
               
               String[] SS = S.split( "[,:]" );
               
               axes = new int[ SS.length ];
               for( int i = 0 ; ( i < axes.length ) &&( axes != null ) ; i++ ){
                  
                  int indx = -1;
                  for( int k = 0 ; ( k < dataState.axisName.length )&&( indx < 0 ) ; k++ )
                     if( SS[ i ].equals( dataState.axisName[ k ] ) )
                        indx = k;
                  
                  if( indx < 0 )
                     axes = null;
                  
                  else if( dataState.XlateAxes != null )
                     if( indx < dataState.XlateAxes.length )
                        indx = dataState.XlateAxes[ indx - 1 ];
                     else{
                        indx = -1;
                        axes = null;
                     }
                  
                  if( axes != null ) axes[ i ] = indx + 1;                 
               }
            }
         }
         
         //Find min axis
         int  minAx = dataState.dimensions.length;
         if( axes == null )
            
            minAx = minAx - dim.length + 1;
         
         else{
            
            for( int i = 0 ; i < axes.length ; i++ )
               if( axes[ i ] < minAx )
                  minAx = axes[ i ];
            
         }
         
         
         if( axes == null )
            return new dimensionHandler( dim, timeDim, colDim, rowDim);
         
         if( timeDim + 1 == minAx )
            minAx++;
         
         int[] ResDim = new int[ dataState.dimensions.length];//new int[ dataState.dimensions.length - minAx + 1 ];
         java.util.Arrays.fill( ResDim , -1 );
         
         int L = ResDim.length-1 ;
         
         for( int i = 0 ; i < dim.length ;  i++ )
            ResDim[ L - axes[ i ] +1 ] = dim[ i ];
         
         if( timeDim + 1 >= minAx )
            ResDim[ L - timeDim ] = -2;
       
       return new dimensionHandler( ResDim, timeDim, colDim,rowDim);
    }
    
    /**
     * Returns an adjusted dimension node where * fields are replaced by -1,
     * the time dim is replaced by -2 if not in the  trail.  The axis numbers
     * (for *) are fixed uing NxDataStateInfo.XlateAxes. axes attribute is
     * NOT used.
     * 
     * @param  node  The node in which to get the fixed up dimension
     * @param  dataState  the stateInfo for the NXdata. Gives dimensions and 
     *                    translations
     *                    
     * @param  timeDim The position in the multi dimension array corresponding to 
     *        NXdata.data that represents time. The fastest changing dimension 
     *       has position 0.
     * 
     *  @param axisOffset  If trailing dimensions are to be ignored.
     *        
     * @return  The fixed up dimension that takes into account axes and reorderings.
     *           -1 at a position means *.
     */
    public static int[] GetDimension( NxNode node , NxDataStateInfo dataState , 
                                                   int timeDim , int axisOffset){
          
         if( node == null )
            return null;
         
         int[] dim1 = node.getDimension();
         if( dim1 == null )
            return null;
         
         if( dim1.length - axisOffset < 0 )
            return null;
        
         int[] dim = new int[ dim1.length - axisOffset ];
        
         System.arraycopy( dim1 , 0 , dim , 0 , dim.length );
         
         int[] axes = null;
         int ax = ConvertDataTypes.intValue( node.getAttrValue( "axis" )  );
         
         if( ax != Integer.MIN_VALUE ){
            
            axes = new int[ 1 ];
            if( dataState.XlateAxes != null )
               ax = dataState.XlateAxes[ ax - 1 ];
            axes[ 0 ] = ax;
            
         }else{// check fro axes attributes
            
            String S =( String )node.getAttrValue( "axes" );
            if( S != null )if( S.length() > 0 ){
               
               S = S.trim();
               if( S.startsWith( "[" ) )
                  S = S.substring( 1 );
               
               if( S.endsWith( "]" ) )
                  S = S.substring( 0 , S.length() - 1 );
               
               String[] SS = S.split( "[,:]" );
               
               axes = new int[ SS.length ];
               for( int i = 0 ; ( i < axes.length ) &&( axes != null ) ; i++ ){
                  
                  int indx = -1;
                  for( int k = 0 ; ( k < dataState.axisName.length )&&( indx < 0 ) ; k++ )
                     if( SS[ i ].equals( dataState.axisName[ k ] ) )
                        indx = k;
                  
                  if( indx < 0 )
                     axes = null;
                  
                  else if( dataState.XlateAxes != null )
                     if( indx < dataState.XlateAxes.length )
                        indx = dataState.XlateAxes[ indx - 1 ];
                     else{
                        indx = -1;
                        axes = null;
                     }
                  
                  if( axes != null ) axes[ i ] = indx + 1;                 
               }
            }
         }
         
         //Find min axis
         int  minAx = dataState.dimensions.length;
         if( axes == null )
            
            minAx = minAx - dim.length + 1;
         
         else{
            
            for( int i = 0 ; i < axes.length ; i++ )
               if( axes[ i ] < minAx )
                  minAx = axes[ i ];
            
         }
         
         
         if( axes == null )
            return  dim;
         
         if( timeDim + 1 == minAx )
            minAx++;
         
         int[] ResDim = new int[ dataState.dimensions.length - minAx + 1 ];
         java.util.Arrays.fill( ResDim , -1 );
         
         int L = ResDim.length - 1;
         
         for( int i = 0 ; i < dim.length ;  i++ )
            ResDim[ L - axes[ i ] + minAx ] = dim[ i ];
         
         if( timeDim + 1 >= minAx )
            ResDim[ timeDim + 1 - minAx ] = -2;
       
       return ResDim;
    }
    
    
    
    
    //Not used yet.
    
    public static float GetArrayValue( float[] list , int[] listDims , 
                                     int[] AllDims , int grid , int ngrids ,
                                      int RowDim , int ColDim , int TimeDim ){
       
       int D = 1;
       int G = 1;
       if( AllDims == null )
          return Float.NaN;
       
       if( AllDims.length - 1  < listDims.length )
          return Float.NaN;
       
       int GG = 0;
       
       int[] coord = new int[listDims.length - GG ];
       int GRid = grid;
       int NGrids = ngrids;
       int NListGrids = 1;
       
       for ( int i = 0 ; i < listDims.length - GG ; i++ )
          
          if( listDims[ i ] >= 0 )
             NListGrids *= listDims[ i ];
       
       java.util.Arrays.fill( coord , -1 );
       for( int i = 0 ; ( i < listDims.length - GG )&&( G < ngrids ) ; i++ ){
          
          if( i != AllDims.length - RowDim - 1 )
             if( i != AllDims.length - ColDim - 1 )
                if( i != AllDims.length - TimeDim - 1 ){
                   
                   G *= AllDims[ i ];
                   int DD = (int )( NGrids/AllDims[ i ] );
                   if( listDims[ i ] <  0 )
                       coord[ i ] = -1;
                   else
                      coord[ i ] = (int)( GRid/DD );
                  GRid = GRid %DD;
                  NGrids = NGrids/AllDims[ i ];                 
                }
       }
       
       
      grid = 0;
      int mult = 1;
      for ( int i = listDims.length - 1 ; i >= GG ; i-- ){
         
         if( coord[ i ] >= 0 ){
            grid += coord[ i ]*mult;
            mult *= listDims[ i ];
         }
      }
   
      
      if( grid >= 0 ) if( grid < list.length )
          return list[ grid ];
      return Float.NaN;
    }
      
      
      
      public static void main( String[] args ){
         
         int[] AllDims = {2 , 3 , 10 , 12 , 20 , 25};
         int[] listDim = {2 , -1 , 10 , 3};
         float[] list = new float[ 20 ];
         int k = 0;
         
         for( int i = 0 ; i < 2 ; i++ )
            for( int j = 0 ; j < 10 ; j++ )
               for(  k = 0 ; k < 3 ; k++ )
                list[ k++ ] = 100*i + 10*j + k;
         
         
         System.out.println(  "list=" + Util.GetArrayValue( list , listDim , 
             AllDims , new Integer( args[ 0 ] ).intValue() ,
             new Integer( args[ 1 ] ).intValue() ,
            3 , 1 , 3 ) );         
      }
      
      
      
      
      
      
      public static int[] GetDimension( NxNode node , NxDataStateInfo dataState , int timeDim ){
         
         return GetDimension( node , dataState , timeDim ,  0 );
         
      }
   
      
      
      
      
   /**
    * @param args
    */
   public static void main1( String[] args  ) {
      
      String fileName = args[ 0 ]; 
      Node N1  = null;
      try{
        
         N1 = DocumentBuilderFactory.newInstance().newDocumentBuilder().
                   parse( new java.io.FileInputStream( fileName ) );
         if( N1 == null )
            System.exit( 0 );
         
      }catch( Exception s ){
         String S = "Error in " + fileName + ":" + s.getMessage();
         if( s instanceof org.xml.sax.SAXParseException )
            S += " at line " + (( org.xml.sax.SAXParseException )s ).getLineNumber();
          javax.swing.JOptionPane.showMessageDialog( null , S );
        System.exit( 0 );
       }
      
      
      String list1 = args[ 1 ];
      String list2 = args[ 2 ];
      String field = args[ 3 ];
      String filename = null;
      if( args.length > 4 )
          filename = args[ 4 ];
      
      if( list1.trim( ).equals( "null" ) )
         list1 = null;
      
      if( list2.trim().equals( "null" ) )
         list2 = null;
      
      if( field.trim().equals( "null" ) )
         field = null;
      
      Node Res = Util.getNXInfo( N1 , list1 , list2 , field , filename );
      
      System.out.println( "" );
      System.out.println( "-----------------" );
      if( Res == null )
         
         System.out.println( "Result is " + null  );
      
      else{
         
         System.out.println( "Res is " + Res.getNodeName() );
         NamedNodeMap attr = Res.getAttributes() ;
         System.out.println( "   Attributes:" );
         if( attr != null )
            for( int i = 0 ; i < attr.getLength() ; i++ ){
             
               Node att = attr.item( i );
               System.out.println( "      " + att.getNodeName() + "= " + att.getNodeValue());
            
            }
         
         NodeList children = Res.getChildNodes();
         
         System.out.println( "    Value(s):" );
         if( Res instanceof Text )
            
            System.out.println( "     " + Res.getNodeValue() );
         
         else
            for( int i = 0 ; i < children.getLength() ; i++ ){
               System.out.println( "      " + children.item( i ).getNodeValue() );
         }
      }
         
      }
   /**
    *  Same as getNXInfo except that It checks general first( NXclassNameList 
    *  is "") then repeatedly add elements of the NXclassNameList and/or field 
    *  name , then does the same with the runs node
    * @param xmlDoc      The node in the xml document
    * @param NXclassPath  The dot separated string of class names(<....<--class naem) to find
    * @param NXclassNameList The dot separated string of name attributes for the above 
    * @param fieldName     The dot separated string name of attribute names or field names
    * @param filename     The filename attribute for the Runs section of the xmldoc
    * @return
    */
   public static Node getNXInfoDefault( Node xmlDoc , String NXclassPath , 
                                    String NXclassNameList , String fieldName ,
            String filename ){
      
      if( NXclassPath == null ) return null;
      if( xmlDoc == null ) return null;

      NXclassNameList = standardize_dot_sep_list( NXclassNameList );

      fieldName = standardize_dot_sep_list( fieldName );

      Node N = getNXInfo( xmlDoc , NXclassPath , "" , fieldName , "" );
    
      int i=1;
      String S= "";
      
      int NClassNames =0;
      if( NXclassNameList != null)
      for( int j= NXclassNameList.indexOf(".",i); j>=i; 
                      ){
         
         NClassNames ++;
        
         i=j+1;
         j= NXclassNameList.indexOf(".",i);
      }
      int NFieldNames =0;
      i=1;
      if( fieldName != null)
         for( int j= fieldName.indexOf(".",i); j>=i; ){
            NFieldNames ++;
            i=j+1;
            j=fieldName.indexOf(".",i);
         }
      Node N1=null;
      boolean[] classes = new boolean[NClassNames];
      boolean[] fields  = new boolean[NFieldNames];
      for(  i=0;i<= NClassNames; i++){
         Arrays.fill( classes,false);
         if( i < NClassNames)
             for( int k=0; k<i;k++)
                classes[k]=true;
         if( (i!= NClassNames)|| NClassNames ==0)
         for( boolean done =false;!done; done = incr(classes))
         for(int j=0; j<=NFieldNames;j++){
            Arrays.fill( fields,false);
            if( j < NFieldNames)
               for( int k=0; k<NFieldNames;k++)
                  fields[k]=true;
            if( i!= NFieldNames || NFieldNames ==0)
            for( boolean done1=false; !done1; done1 =incr(fields)){
                String ClassNames =getDotString( classes, NXclassNameList);
                String FieldNames = getDotString( fields, fieldName);
                N1=getNXInfo( xmlDoc, NXclassPath,ClassNames,FieldNames,"");
                if( N1 != null)
                   N = N1;
            }
            
         }
         
      }//
      
      Node Runs = getNXInfo( xmlDoc,"Runs", null,null, filename);
      
      N1= getNXInfoDefault( Runs, NXclassPath , NXclassNameList ,  fieldName, null);
         
      if( N1 != null)
         N=N1;
      return N;
         
      
       
   }
   private static String getDotString( boolean[] dots,  String NamesList){
      String Res="";
      int i=1;
      if( dots == null || dots.length < 1)
         return Res;
      if( NamesList == null)
         return Res;
      if( NamesList.length() <=2 )
         return Res;
      Res =".";
      int dotIndex =0;
      for( int j= NamesList.indexOf(".",i); j>0; j=NamesList.indexOf(".",i)){
        if( dots[dotIndex])
                 Res+= NamesList.indexOf(i,j-1)+".";
        else
           Res +=".";
         i=j+1;
      }
      while( Res != null && Res.startsWith("."))
         Res = Res.substring(1);

      while( Res != null && Res.endsWith("."))
         Res = Res.substring(0,Res.length()-1);
      if( Res.trim().length() < 1)
         Res = null;
      return Res;
   }
   
   private static boolean incr( boolean[] list){
      if( list == null)
         return true;
      if( list.length <=1)
         return true;
      boolean done = false;
      int C=0;
      while(!done){
         int x = -1;
         for( int i=list.length-1; i>=0 && x<0; i--)
            if( list[i]){
               x=i;
               list[i]=false;
               C++;
            }
         if( x< 0)
            return true;
         if( x < list.length-1-C){
            done = true;
            for( int k=0; k<C; k++)
               list[x+k]=true;
         }
         
      }
      return false;
      
   }
   /**
    * Will Search the xml document( or part) for the Node 
    * 
    * @param xmlDoc  The node that is being searched
    * @param NXentryName  the Name of the NXentry xml node, could be null for
    *                      any NXentry
    * @param  FieldClass The classes(<___) of the field in the NXentry Node
    *                     later entries are subclasses of previous FieldClass
    *                     
    * @param  FieldClassName  the corresponding name attributes( could be "" or
    *                   null)
    *                   
    * @param filename   The filename attribute or null. Additional searches will
    *                be done with the filename == null.
    * 
    * @param SearchNoNameNXentry  Does an additional search on NXentries without
    *                             a name.
    * @param SearchNoNamesubFields  Does additional searches on NXdata's without names
    * 
    * @return The node in question.  use getLeafNodeValue to get its value.
    *            
    * ALGORITHM: The Run fields will be searched first with the filename attribute.
    *            then the Common fields
    *            In each The full path with names will be searched, then entries with
    *            nonames NXentries if searchNoNameNXentry, then NXentries with names
    *            will be searched for fields without names if specified in 
    *            SearchNoNamesubFields. Finally all the nonamed fields will be searched
    */
   public static Node getXMLNodeVal( Node xmlDoc, String NXentryName, 
                                  String[] FieldClass, String[] FieldClassName,               
                                  String filename, boolean SearchNoNameNXentry,
                                  boolean[]SearchNoNamesubFields )
                                  {
     
     
      if( xmlDoc == null)
         return null;
      
      if( NXentryName == null)
         SearchNoNameNXentry = false;
      
      if( FieldClassName == null)
         SearchNoNamesubFields = null;
      
      if( FieldClass == null)
         FieldClassName = null;
      
      if( FieldClass != null && FieldClassName != null)
         if( FieldClass.length != FieldClassName.length)
            return null;
         else if( SearchNoNamesubFields != null && 
                     SearchNoNamesubFields.length !=FieldClassName.length)
            return null;
         else if( SearchNoNamesubFields != null){
            boolean foundTrue = false;
            for( int i=0; i< SearchNoNamesubFields.length  && !foundTrue; i++)
               if( SearchNoNamesubFields[i])
                  foundTrue = true;
            if( !foundTrue)
               SearchNoNamesubFields = null;
         }
      
      String FileName = filename;
      if( filename != null){
         FileName = filename.trim();
         FileName = FileName.replace('\\','/');
         int k = FileName.lastIndexOf('/');
         if( k >=0)
            FileName = FileName.substring( k+1);
         if( FileName.trim().length() < 1)
            FileName = null;
      }

      String NxEntryString ="";
      if( NXentryName != null)
         NxEntryString = NXentryName;
      Node Res = null;
      if( FileName != null){
         
         Res = Util.getNXInfo1( xmlDoc,"Runs.Run.NXentry"+GetdottedStringFor( FieldClass,null), 
                              "..."+NxEntryString+GetdottedStringFor(FieldClassName,null ), 
                              null, FileName);
         if( Res == null)
            Res =Util.getNXInfo1( xmlDoc,"Runs.Run.NXentry"+GetdottedStringFor( FieldClass,null), 
                     "...-"+GetdottedStringFor(FieldClassName,null ), 
                     null, FileName);
         if( Res == null && SearchNoNamesubFields != null)
            Res =Util.getNXInfo1( xmlDoc,"Runs.Run.NXentry"+GetdottedStringFor( FieldClass,null), 
                     "..."+NxEntryString+GetdottedStringFor(FieldClassName,SearchNoNamesubFields ), 
                     null, FileName);
         

         if( Res == null && SearchNoNamesubFields != null)
            Res =Util.getNXInfo1( xmlDoc,"Runs.Run.NXentry"+GetdottedStringFor( FieldClass,null), 
                     "...-"+GetdottedStringFor(FieldClassName,SearchNoNamesubFields ), 
                     null, FileName);
         
      }
      if( Res == null){
         Res = Util.getNXInfo1( xmlDoc,"Common.NXentry"+GetdottedStringFor( FieldClass,null), 
                  ".."+NxEntryString+GetdottedStringFor(FieldClassName,null ), 
                  null, null);
         if( Res == null)
            Res = Util.getNXInfo1( xmlDoc,"Common.NXentry"+GetdottedStringFor( FieldClass,null), 
                     "..-"+GetdottedStringFor(FieldClassName,null ), 
                     null, null);
         if( Res == null)
            Res = Util.getNXInfo1( xmlDoc,"Common.NXentry"+GetdottedStringFor( FieldClass,null), 
                  ".."+NxEntryString+GetdottedStringFor(FieldClassName,SearchNoNamesubFields ), 
                  null, null);

         if( Res == null)
            Res = Util.getNXInfo1( xmlDoc,"Common.NXentry"+GetdottedStringFor( FieldClass,null), 
                  "..-"+GetdottedStringFor(FieldClassName,SearchNoNamesubFields ), 
                  null, null);
      
         
      }
      return Res;
      
         
   }
   
   
   private static String GetdottedStringFor( String[] FieldNames, boolean[] useName){
      String Fclass ="";
      if( FieldNames != null)
         for( int i=0; i < FieldNames.length; i++)
            if( FieldNames[i] == null )
               Fclass +=".";
            else if( useName != null && useName[i])
               Fclass +=".-";
            else
               Fclass +="."+FieldNames[i];
      while(Fclass.length() >1 && Fclass.endsWith(".."))
         Fclass = Fclass.substring( 0, Fclass.length() -1);
      return Fclass;
   }
   

 }


