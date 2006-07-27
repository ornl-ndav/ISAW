package NexIO.Util;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import NexIO.*;
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
      
      return S;
      
   }
   
   
   
   
   /**
    * Finds information in an XML document
    * @param xmlDoc  The top node( or what is left) of a DOM document
    * @param NXclassPath   A dot separated list of Nexus Classes. These appear as <NXentry  name="...
    *                        in the XML file. Only those parts of the xml document will be searched.
    *                        This can be null for all NeXus classnames to be listed
    * @param NXclassNameList The name of the NeXus Class. The name is in the name= attribute. This also can
    *                        be null( all will be considered) , or a set
    * @param fieldName      The specific field name( tag or name attribute) to search for
    * @param filename       The tag of the node must have a filename attribute corresponding to this filename 
    * @return     String value of this node
    */
   public static Node getNXInfo( Node xmlDoc , String NXclassPath , String NXclassNameList , String fieldName ,
            String filename ){
      
        if( xmlDoc == null )
           return null;
        
        NXclassPath = standardize_dot_sep_list( NXclassPath );
        NXclassNameList = standardize_dot_sep_list( NXclassNameList );
        NodeList children = xmlDoc.getChildNodes();
        if( ( NXclassPath == null ) &&( NXclassNameList == null )&&
                                  ( fieldName == null )&&( filename == null ) )
           if( children.getLength() != 1 )
                return xmlDoc;
        
           else 
                return xmlDoc.getFirstChild();
        
           
        for( int ik = 0 ; ik < children.getLength() ; ik++  ){
           
            Node NN = children.item( ik );
            int k = -1;
            if( NXclassPath != null )
               k = NXclassPath.indexOf( "." +  NN.getNodeName() + "." );
            
            String namee = null;
            String fname = null;
            boolean ClassHasName = false;
            boolean ClassHasFile = false;
             if( NXclassNameList != null ){
                
               NamedNodeMap atts = NN.getAttributes();
               String namess = NXclassNameList.substring( 1 , 
                                            NXclassNameList.indexOf( "." , 1 ) );
              
               if( namess!= null ) if( namess.trim().length() > 0 )if( atts != null ){
                  
                    Node attNode = atts.getNamedItem( "name" );
                    if( attNode != null ) ClassHasName = true;
                   if( attNode != null )if( namess.equals( attNode.getNodeValue() ) )
                      namee = namess;
                   
                  if( filename != null ){
                     
                      attNode = atts.getNamedItem( "filename" );
                      if( attNode != null )if( filename.equals( attNode.getNodeValue() ) )
                         fname = namess;
                      
                      if( attNode != null )
                         ClassHasFile =  true;
                      
                   }
               }
             }
             
             boolean OkToEnter = false;
             if( ( NXclassPath !=null )||( NXclassNameList != null ))
                if( filename == null ){
                   
                   if( k == 0 )if( namee != null ) 
                      OkToEnter = true;
                   if( k == 0 )if(( NXclassNameList == null ) ) 
                      OkToEnter = true;
                   if( k == 0 )if( !ClassHasName )  
                      OkToEnter = true;
                   if( NXclassPath == null )if( namee != null )  
                      OkToEnter = true;
                   if( NXclassPath == null )if( !ClassHasName )  
                      OkToEnter = true;
                   
               }else{
                  
                  if( k == 0 )if( namee != null )if( (fname != null ) ||!ClassHasFile ) 
                     OkToEnter = true;
                  if( k == 0 )if(( NXclassNameList == null ) )if( ( fname != null ) ||!ClassHasFile ) 
                     OkToEnter = true;
                  if( k == 0 )if( !ClassHasName )if( (fname != null ) ||!ClassHasFile ) 
                     OkToEnter = true;
                  if( NXclassPath == null )if( namee != null )if( ( fname != null ) ||!ClassHasFile )  
                     OkToEnter = true;
                  if( NXclassPath == null )if( !ClassHasName )if( ( fname != null ) ||!ClassHasFile )  
                     OkToEnter = true;
                  
               }
                 
            if(  OkToEnter ){//Has child nodes to search
               
              String Clist = null , 
                     CNameList = null;
              
              if( k >= 0 ){
                 k = NXclassPath.indexOf( '.' , k + 1 );
                 if( k >= 0 ) Clist = NXclassPath.substring( k );
              }
              
              if( namee != null ){
                 k = NXclassNameList.indexOf( "." +  namee + "." );
                 if( k >= 0  ){
                    k = k + 2 + namee.length();
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
               
            }else{
               
               if( fieldName != null )if( fieldName.equals( NN.getNodeName() ) )
                   return  NN;
               
               NamedNodeMap  atts = NN.getAttributes();
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
   
   
   
  
    //Must have one child that is a text node
   private static Object getLeafNodeValue( Node NN ){
       
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
     *                    
     * @param  The position in the multi dimension array corresponding to 
     *        NXdata.data that represents time. The fastest changing dimension 
     *       has position 0.
     *        
     * @return  The fixed up dimension that takes into account axes and reorderings.
     *           -1 at a position means *.
     */
    public static int[] GetDimension( NxNode node , NxDataStateInfo dataState , 
                                                   int timeDim , int axisOffset ){
       
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
            return dim;
         
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
 }


