package NexIO.Write;


import DataSetTools.dataset.*;
import java.lang.reflect.*;
import NexIO.*;


public class util {

    /** Returns a float value corresponding to the given attribute for the 
     *   given data block
     * 
     * @param db  the data block for which attribute is sought
     * @param  AttributeName  the name of the attribute
     * @return  the value converted to a float, or Float.NaN if not possible
     */
    public static float  getFloatVal(Data db, String AttributeName) {
        
        Object O = db.getAttributeValue(AttributeName);

        if (O == null) 
            return Float.NaN;
         
        if (O instanceof Number) 
            return ((Number) O).floatValue();
         
        if (O.getClass().isArray())
      
            if (Array.getLength(O) > 0)
                try {
             
                    return Array.getFloat(O, 0);
             
                } catch (Exception ss) {
             
                    return Float.NaN;
              
                }

        return Float.NaN;

    }

    /** 
     * Returns a int value corresponding to the given attribute for the 
     *   given data block
     * 
     * @param db  the data block for which attribute is sought
     * @param  AttributeName  the name of the attribute
     * 
     * @return Integer.MIN_VALUE if unable to return an int value
     */
    public static int  getIntVal(Data db, String AttributeName) {
        
        Object O = db.getAttributeValue(AttributeName);

        if (O == null) 
            return Integer.MIN_VALUE;
         
        if (O instanceof Number) 
            return ((Number) O).intValue();
         
        if (O.getClass().isArray())
      
            if (Array.getLength(O) > 0)
                try {
             
                    return Array.getInt(O, 0);
             
                } catch (Exception ss) {
             
                    return Integer.MIN_VALUE;
              
                }

        return Integer.MIN_VALUE;

    }
   
    /**
     *  Creates a new SDS child node and fills it up with the float data
     * @param parentnode   The parent node
     * @param childNodeName   The name of the child node
     * @param data   The float data stored in this leaf
     * @param ranks  The rank of the data
     * @return
     */
    public static NxWriteNode writeFA_SDS(NxWriteNode parentnode, String childNodeName,
        float[] data, int[] ranks) {
                   
        if (parentnode == null) 
            return null;
        
        if (childNodeName == null) 
            return null;
        
        if (data == null) 
            return null;
        
        if (ranks == null) 
            return null;
        
        NxWriteNode child = parentnode.newChildNode(childNodeName, "SDS");

        child.setNodeValue(data, Types.Float, ranks);
     
        return child;
    }


    /**
     * Utility routine that creates and "writes" the int array data to the 
     * childNode of the parentnode
     * 
     * @param parentnode  The parent of the node to be created
     * @param childNodeName  The name to be given to the child. THe clas is SDS
     * @param data   The int array data to be placed into the child SDS node
     * @param ranks  The dimensions of the data array.
     * @return  The newly created node to incororate attributes, or null if not
     *          possible
     */
    public static NxWriteNode writeIA_SDS(NxWriteNode parentnode, String childNodeName,
        int[] data, int[] ranks) {
          
        if (parentnode == null) 
           return null;
           
        if (childNodeName == null)
           return null;
           
        if (data == null) 
           return null;
           
        if (ranks == null) 
           return null;
           
        NxWriteNode child = parentnode.newChildNode(childNodeName, "SDS");

        child.setNodeValue(data, Types.Int, ranks);
        return child;
    }


    /**
     * A utility that writes a string attribute to the node
     * @param node  The node to which the string attribute is to be added
     * @param AttrName  The name of the attribute
     * @param AttrValue  The String value of this attribute
     */
    public static void writeStringAttr(NxWriteNode node, String AttrName, 
                       String AttrValue) {
                         
        if (node == null) 
           return;
           
        if (AttrValue == null) 
           return;
           
        if (AttrName == null)  
           return;
           
        int[] rank = new int[1];

        rank[0] = AttrValue.length() + 1;
        node.addAttribute(AttrName, (AttrValue + (char) 0).getBytes(),
                         Types.Char, rank);

    }




     /**
      * A utility that writes an integer attribute to the node
      * @param node  The node to which the integer attribute is to be added
      * @param AttrName  The name of the attribute
      * @param AttrValue  The integer value of this attribute
      */
    public static void writeIntAttr(NxWriteNode node, String AttrName, 
                                                     int AttrValue) {
                                                       
        if (node == null) 
           return;
           
        if (AttrName == null) 
           return;
           
        int[] rank = new int[1];
        int[] val = new int[1];

        val[0] = AttrValue;
        rank[0] = 1;
        node.addAttribute(AttrName, val, Types.Int, rank);
    }



    /**
     * Creates a "rank array" for a multidimensional array
     * @param multiArray  The multidimensional array
     * @return   the array of sizes in each dimension or an int array with
     *            size 0.
     */
    public static int[] setRankArray(Object multiArray, boolean elim1dim) {
      
        if (!(multiArray.getClass().isArray()))
            return new int[0];
            
        Object O = Array.get(multiArray, 0);

        if (O.getClass().isPrimitive() || !(O.getClass().isArray())) {
            int[] Res = new int[1];

            Res[0] = Array.getLength(multiArray);
            return Res;
        }
        int n1 = Array.getLength(multiArray);
        int nNew = 1;//try to eliminate dim size = 1
        if( (n1 <=1) && elim1dim)
           nNew =0;
        int[] Res1 = setRankArray(O, elim1dim);
        int[] Res = new int[Res1.length + nNew];

        Res[0] = n1;
        System.arraycopy(Res1, 0, Res, nNew, Res1.length);
   
        return Res;
    }
}
