package NexIO.Write;
import DataSetTools.dataset.*;
import java.lang.reflect.*;
import NexIO.*;
public class util {

   /** Returns Float.NaN if unable to return a float
   */
   public static float  getFloatVal( Data db, String AttributeName){
        
      Object O = db.getAttributeValue( AttributeName);
      if( O == null) return Float.NaN;
      if(O instanceof Number) return ((Number)O).floatValue();
      if( O.getClass().isArray())
         if( Array.getLength(O) > 0)
           try{
             return Array.getFloat( O,0);
           }catch( Exception ss){
              return Float.NaN;
           }

      return Float.NaN;

   }


   /** Returns Float.NaN if unable to return a float
   */
   public static int  getIntVal( Data db, String AttributeName){
        
      Object O = db.getAttributeValue( AttributeName);
      if( O == null) return Integer.MIN_VALUE;;
      if(O instanceof Number) return ((Number)O).intValue();
      if( O.getClass().isArray())
         if( Array.getLength(O) > 0)
           try{
             return Array.getInt( O,0);
           }catch( Exception ss){
              return Integer.MIN_VALUE;
           }

      return Integer.MIN_VALUE;

   }
  public static NxWriteNode writeFA_SDS(NxWriteNode parentnode, String childNodeName ,
                 float[] data, int[] ranks){
     if( parentnode == null) return null;
     if( childNodeName == null) return null;
     if( data == null) return null;
     if( ranks == null) return null;
     NxWriteNode child = parentnode.newChildNode(childNodeName, "SDS");
     child.setNodeValue( data, Types.Float, ranks);
     return child;
  }


  public static NxWriteNode writeIA_SDS(NxWriteNode parentnode, String childNodeName ,
                 int[] data, int[] ranks){
     if( parentnode == null) return null;
     if( childNodeName == null) return null;
     if( data == null) return null;
     if( ranks == null) return null;
     NxWriteNode child = parentnode.newChildNode(childNodeName, "SDS");
     child.setNodeValue( data, Types.Int, ranks);
     return child;
  }

 public static void writeStringAttr(NxWriteNode node,String AttrName, String AttrValue){
      if( node ==null) return;
      if( AttrValue ==  null) return;
      if( AttrName ==  null) return;
      int[] rank = new int[1];
      rank[0] = AttrValue.length()+1;
      node.addAttribute( AttrName, (AttrValue+(char)0).getBytes(), Types.Char,rank);

  }

 public static void writeIntAttr(NxWriteNode node,String AttrName, int AttrValue){
      if( node ==null) return;
      if( AttrName ==  null) return;
      int[] rank = new int[1];
      int [] val = new int[1];
      val[0] = AttrValue;
      rank[0] = 1;
      node.addAttribute( AttrName, val, Types.Int,rank);

  }
public static int[] setRankArray( Object multiArray){
  if(!(multiArray.getClass().isArray()))
    return new int[0];
  Object O = Array.get( multiArray,0);
  if( O.getClass().isPrimitive() || !(O.getClass().isArray())) {
     int[] Res = new int[1];
     Res[0] = Array.getLength( multiArray);
     return Res;
  }
  int n1 = Array.getLength(multiArray);
  int[] Res1 = setRankArray( O);
  int[] Res = new int[Res1.length+1];
  Res[0] = n1;
  System.arraycopy( Res1,0,Res,1,Res1.length);
  return Res;
}
}
