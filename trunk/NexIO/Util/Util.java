package NexIO.Util;

import javax.xml.parsers.*;

import org.w3c.dom.*;

public class Util {

   public Util() {
      super();
      // TODO Auto-generated constructor stub
   }

   private static String standardize_dot_sep_list( String S){
      if( S == null)
         return null;
      S = S.trim();
      if( S.length() <1)
         return null;
      if( !S.endsWith( "."))
         S +=".";
      if(!S.startsWith("."))
         S ="."+S;
      if( S.length() <=2)
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
    *                        be null( all will be considered), or a set
    * @param fieldName      The specific field name( tag or name attribute) to search for
    * @param filename       The tag of the node must have a filename attribute corresponding to this filename 
    * @return     String value of this node
    */
   public static Node getNXInfo( Node xmlDoc, String NXclassPath, String NXclassNameList, String fieldName,
            String filename){
      
        if( xmlDoc == null)
           return null;
        NXclassPath = standardize_dot_sep_list( NXclassPath);
        NXclassNameList = standardize_dot_sep_list( NXclassNameList);
        NodeList children = xmlDoc.getChildNodes();
           
        for( int ik=0; ik < children.getLength(); ik++  ){
            Node NN= children.item(ik);
            int k = -1;
            if( NXclassPath != null)
               k = NXclassPath.indexOf("."+ NN.getNodeName()+".");
            
            String namee=null;
            String fname=null;
            boolean ClassHasName = false;
            boolean ClassHasFile = false;
             if( NXclassNameList != null){
               NamedNodeMap atts =NN.getAttributes();
               String namess = NXclassNameList.substring( 1, NXclassNameList.indexOf(".",1));
              
               if( namess!=null) if( namess.trim().length() >0)if( atts != null){
                    Node attNode = atts.getNamedItem("name");
                    if( attNode != null) ClassHasName = true;
                   if( attNode != null)if( namess.equals(attNode.getNodeValue()))
                      namee = namess;
                  if( filename != null){
                      attNode = atts.getNamedItem("filename");
                      if(attNode !=null)if( filename.equals(attNode.getNodeValue()))
                         fname = namess;
                      if( attNode != null)
                         ClassHasFile =  true;
                      
                   }
               }
             }
             boolean OkToEnter= false;
             if( (NXclassPath !=null)||(NXclassNameList != null))
                if(filename == null){
                   if( k==0)if( namee != null) OkToEnter = true;
                   if( k==0)if(( NXclassNameList == null)) OkToEnter = true;
                   if( k==0)if( !ClassHasName )  OkToEnter = true;
                   if( NXclassPath == null)if( namee != null)  OkToEnter = true;
                   if( NXclassPath == null)if( !ClassHasName)  OkToEnter = true;
               }else{
                  if( k==0)if( namee != null)if((fname != null) ||!ClassHasFile) OkToEnter = true;
                  if( k==0)if(( NXclassNameList == null))if( (fname != null) ||!ClassHasFile) OkToEnter = true;
                  if( k==0)if( !ClassHasName)if( (fname != null) ||!ClassHasFile) OkToEnter = true;
                  if( NXclassPath == null)if( namee != null)if( (fname != null) ||!ClassHasFile)  OkToEnter = true;
                  if( NXclassPath == null)if( !ClassHasName)if( (fname != null) ||!ClassHasFile)  OkToEnter = true;
                  
               }
                 
            if(  OkToEnter){
              String Clist =null, CNameList = null;
              if( k >=0){
                 k = NXclassPath.indexOf('.',k+1);
                 if( k>=0) Clist = NXclassPath.substring(k);
              }
              if( namee != null){
                 k=NXclassNameList.indexOf("."+ namee+".");
                 if( k>= 0 ){
                    k = k+2+namee.length();
                    CNameList = NXclassNameList.substring( k);
                 }
              }else 
                 CNameList =NXclassNameList;
              Node X= getNXInfo(NN, Clist, CNameList, fieldName,filename);
              if( X != null)
                 return X;
            }else if((NXclassPath != null)|| (NXclassNameList != null)){//Check for fieldName
               
               Node X =getNXInfo(NN, NXclassPath, NXclassNameList, fieldName,filename);
               if( X != null)
                  return X;
               
            }else{
               if( fieldName != null)if( fieldName.equals( NN.getNodeName()))
                   return  NN;
               NamedNodeMap  atts = NN.getAttributes();
               if( atts != null){
                  Node attNode = atts.getNamedItem("name");
                  if( attNode != null)if( fieldName.equals( attNode.getNodeValue()))
                     return   NN ;
                  attNode = atts.getNamedItem("filename");
                  if( (filename != null) && (attNode != null))
                     return  NN ;
               }
               if( fieldName == null)
                  return NN;
               Node X =getNXInfo(NN, NXclassPath, NXclassNameList, fieldName,filename);
               if( X != null)
                  return X;
            }
         
        }//for each child
      
      
      
      return null;
      
   }
  
    //Must have one child that is a text node
    private static Object getLeafNodeValue( Node NN){
       if( NN == null)
          return null;
       NodeList children = NN.getChildNodes();
      String Res = "";
      for( int i = 0; i< children.getLength(); i++){
         Node tNode = children.item( i);
         if( !(tNode instanceof Text))
            return null;
         Res += tNode.getNodeValue();
      }
       return Res;
       
    }
   /**
    * @param args
    */
   public static void main( String[] args ) {
      
      String fileName = args[0]; 
      Node N1  = null;
      try{
        
         N1= DocumentBuilderFactory.newInstance().newDocumentBuilder().
                   parse( new java.io.FileInputStream(fileName));
         if( N1 == null)
            System.exit(0);
         
      }catch( Exception s){
         String S ="Error in "+ fileName+":"+s.getMessage();
         if( s instanceof org.xml.sax.SAXParseException)
            S +=" at line "+((org.xml.sax.SAXParseException)s).getLineNumber();
          javax.swing.JOptionPane.showMessageDialog(null,S);
        System.exit(0);
       }
      
      String list1= args[1];
      String list2 = args[2];
      String field = args[3];
      String filename = null;
      if( args.length > 4)
          filename = args[4];
      if( list1.trim().equals("null"))
         list1 = null;
      if( list2.trim().equals("null"))
         list2 = null;
      if( field.trim().equals("null"))
         field = null;
      Node Res =Util.getNXInfo( N1, list1, list2, field, filename);
      System.out.println("");
      System.out.println("-----------------");
      if( Res == null)
         System.out.println("Result is "+ null );
      else{
         System.out.println( "Res is "+ Res.getNodeName());
         NamedNodeMap attr = Res.getAttributes() ;
         System.out.println("   Attributes:");
         if( attr != null)
         for( int i =0; i< attr.getLength(); i++){
            Node att =attr.item(i);
            System.out.println( "      "+att.getNodeName()+"="+att.getNodeValue());
         }
         NodeList children = Res.getChildNodes();
         System.out.println("    Value(s):");
         if( Res instanceof Text)
            System.out.println("     "+ Res.getNodeValue());
         else
         for( int i = 0; i< children.getLength(); i++){
            System.out.println( "      "+children.item(i).getNodeValue());
         }
      }
         
      }
 }


