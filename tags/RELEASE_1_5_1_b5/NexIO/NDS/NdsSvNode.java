/*
 * File: NdsSvNode.java
 *
 * Copyright (C) 2001, Ruth Mikkelson
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
 * $Log$
 * Revision 1.5  2002/11/27 23:28:56  pfpeterson
 * standardized header
 *
 */
package NexIO.NDS;

//nds server, 
import NexIO.*;
//import ndsSvAttr;
//import NDSClient;
import java.util.*;
import java.io.*;

public class NdsSvNode implements NxNode{
  public Vector Node;//1st filename,NXFile, 2nd node name comma class class
  NDSClient nds;
  String errormessage;
  int[] IDim=null;

  public NdsSvNode(Vector N, NDSClient nds){
    Node = N;
    this.nds=nds;
    errormessage="";
  }

  public NdsSvNode(String filename,NDSClient nds){
    Node=new Vector();
    Node.add(filename+",FILE");
    this.nds=nds;
    errormessage="";
  }

   public String getErrorMessage(){
     return errormessage;
   }

  public Vector getNode(){
    return Node;
  }

  public int getNChildNodes(){
    String S1;
    int i;
    int n;
    errormessage="";
    try{
      //System.out.println("getNchild Node2 args="+getFile()+","
      //+getDef()+"NXVGROUP");
      if(!nds.getVGroupDirectory(getFile(), getDef()+"NXVGROUP"))
        {errormessage=nds.getStringData();
        //System.out.println("errmessage="+errormessage);
        return -1;
        }
    }catch(IOException s){
      errormessage= s.getMessage();
      return -1;
    }

    n=0;
    S1=nds.getStringData().trim();
      
    if(S1.length()<=0)return 0;
    if(S1.charAt(S1.length()-1)!='\n')
      S1=S1+"\n";
    i=-1;
    
    for(i=S1.indexOf('\n',i+1);i>=0;i=S1.indexOf('\n',i+1)){
      n++;
    }
  
    return n;
  }
  
  public NxNode getChildNode(String NodeName){
    String S1;
    int i;
    errormessage="";
    //System.out.println("      getchildNode w name"+getDef());
    try{
      //System.out.println("get child args to ="+","+getFile()+","+getDef()
      //+"NXVGROUP");
      if(!nds.getVGroupDirectory(getFile(), getDef()+"NXVGROUP")){
        errormessage=nds.getStringData();
        //System.out.println("HERE");
        return null;
      }
    }catch(IOException s){
      errormessage=s.getMessage();
      return null;
    }

    S1=nds.getStringData();
    if(S1==null){
      errormessage="null string";
      return null;}
    int k=S1.indexOf(NodeName+'@');
    if(k>0) if(S1.charAt(k-1)!='\n'){
      k=S1.indexOf('\n'+NodeName+'@');
      if(k>=0)k++;
    }
    if(k<0){
      errormessage="No such Node";
      return null;
    }
    int j=S1.indexOf('@',k);
    if((j<0)||(j<k)){
      errormessage="Node error";
      return null;
    }
    int l = S1.indexOf('\n',k+1);
    if(l<0)
      l=S1.length();
    Vector V= (Vector)(Node.clone());
    String SS;
    SS=S1.substring(k,j)+","+S1.substring(j+1,l);
    V.addElement(SS);//S1.substring(j,k));
    return (NxNode)(new NdsSvNode(V,nds));
  }    

  public NxNode getChildNode(int index){
    String S1;
    int i;
    errormessage="";
    //  System.out.println("getNchild Node1 args="+getFile()+","+getDef()+
    //            "NXVGROUP");
    
    try{
      // System.out.println("get child args to i="+index+","+getFile()+","+
      //                    getDef()+"NXVGROUP");
      if(!nds.getVGroupDirectory(getFile(), getDef()+"NXVGROUP")){
        errormessage=nds.getStringData();
        //System.out.println("HERE");
        return null;
      }
    }catch(IOException s){
      errormessage=s.getMessage();
      return null;
    }

    int n=0;
    S1=nds.getStringData();
    
    Vector V = new Vector();
    if(S1.length()<=0)return null;
    i=-1;
    int u=0;
    
    S1=S1.trim();
    if(S1.charAt(S1.length()-1)!='\n')S1=S1+'\n';
    i=S1.indexOf('\n',i+1);
    for(;i>=0 && u<index;i=S1.indexOf('\n',i+1)){
      //System.out.print("i="+i+",");
      
      u++;
      //System.out.println(" u="+(u-1)+", indx="+index);
      
    }
    if(i<0)i=S1.length()-1;
    if(i<2)return null;
    if(u==index){
      int j=S1.lastIndexOf('\n',i-1)+1;
             
      int k = S1.indexOf('\n',i);
      //System.out.print(" j,k="+j+","+k);
      if(k<0)k=S1.length();
      V= (Vector)(Node.clone());
      String SS= S1.substring(j,k);
      SS=SS.replace('@',',');
      V.addElement(SS);//S1.substring(j,k));
      return (NxNode)(new NdsSvNode(V,nds));
    }
    return null;
    
  }

  public String getNodeName(){
    int n=Node.size();
    if(n<=0) return null;
    String S1=(String)(Node.lastElement());
    //System.out.println("last node name="+S1);
    return S1.substring(0,S1.indexOf(','));
  }

  public String getNodeClass(){
    int n=Node.size();
    if(n<=0)
      return null;
    String S1=(String)(Node.lastElement());
    String res= S1.substring(S1.indexOf(',')+1);
    int i=res.indexOf(',');
    if(i>=0)
      res=res.substring(0,i);
    return res;
  }

  public int getNAttributes(){
    errormessage="";
    try{
      if(!nds.getAttrib( getFile(),getDef()+"/NXVGROUP")){
        errormessage= nds.getStringData();
        return -1;
      }
    }catch(IOException s){
      errormessage=s.getMessage();
      return -1;
    }
    if(nds.getType()!=NDSClient.STRING){
      errormessage="improper result";
      return -1;
    }
    String S1=nds.getStringData();
    int i,n;n=0;
    for(i=S1.indexOf('\n');(i+1<S1.length())&&(i>=0);i=S1.indexOf('\n',i+1))
      n++;
    if(S1.charAt(S1.length()-1)=='\n')n++;
    return n;
  }

  public int[] getDimension(){
    if( !(getNodeClass().indexOf("SDS")==0)) 
      return null;
    if( nds== null) 
      return null;
    return IDim;//nds.getDimension();
  }

  public Object getNodeValue(){
    errormessage="improper node for Value";
    String S1= getDef();
    if(S1==null){
      return null;
    }
    if(S1.length()<=3)
      return null;
    int k=S1.lastIndexOf('/',S1.length()-2);
    if(k<0)
      return null;
    if(!(S1.substring(k).indexOf(",SDS")>=0))
      return null;
      
    S1=S1.substring(0,S1.length()-2);
    
    k = S1.lastIndexOf('/');
    int j=S1.indexOf(',',k);
      
    errormessage="improper Node info";
    if((j<0)||(k<0)||(j<k))
      return null;
    errormessage="";
    S1=S1.substring(0,k+1)+"SDS "+S1.substring(k+1,j);
      
    try{
      if(!nds.getData(getFile(),S1)){
        errormessage=nds.getStringData();
        return null;
      }
      IDim = nds.getDimension();
    }catch(IOException s){
      errormessage=s.getMessage();
      return null;
    }
    
    k=nds.getType();
    //System.out.println("type ="+k);
    if(k==NDSClient.FLOAT)
      return nds.getFloatData();
    else if(k==NDSClient.INT)
      return nds.getIntData();
    else if(k==NDSClient.BYTE)
      return nds.getByteData(); 
    else if(k==NDSClient.STRING)
      return nds.getStringData();   
    else if(k==NDSClient.DOUBLE)
      return nds.getDoubleData(); 
    else{ //if(k==NDSClient.FILE) 
      errormessage="imporper return type";
      return null;
    }
  }

  public Object getAttrValue(String AttrName){
    Object X;
    String def;
    errormessage="";
    try{
      if(getNodeClass().equals("SDS")){
        def=getDef().trim();
        if(def==null){
          errormessage="Improper node152";
          return null;
        }
        int ii=def.lastIndexOf('/',def.length()-2);
        if(ii<0){
          errormessage="Improper node153";
          return null;
        }
        def=def.substring(0,ii+1)+"SDS "+getNodeName();
        
      }else
        def=getDef()+"NXVGROUP";

      //System.out.println("getAttr argsto="+getFile()+"::"+def);
      if(!nds.getAttrib( getFile(),def)){
        errormessage= nds.getStringData();
        return null;
      }
    }catch(IOException s){
      errormessage=s.getMessage();
      return null;
    }
    if(nds.getType()!=NDSClient.STRING){
      errormessage="improper result";
      return null;
    }
    String S1=nds.getStringData();
    if(S1==null) return null;
    if(S1.length()<=0) return null;
    int n=0;int i;
    int k=S1.indexOf(AttrName+'@');
    errormessage="No such Attribute";
    
    if(k>0) if(S1.charAt(k-1)!='\n'){
      k=S1.indexOf('\n'+AttrName+'@',k+1);
      if(k>=0) k++;
    }
    if(k<0) return null;
    i=S1.indexOf('\n',k+1);
    if(i<0)i=S1.length();
           
    errormessage="";
    int j=S1.indexOf('@',k);
    if((j<0)||(j<=k)||(j+1>=i)){
      errormessage="nds format error";
      return null;
    }
    String Adef=S1.substring(j+1,i);
    
    if(Adef.indexOf("SDS")!=0)
      return (Object)Adef;
    try{
      if(!nds.getData(getFile(),getDef()+"SDS "+AttrName)){
        errormessage=nds.getStringData();
        return null;
      }
      k=nds.getType();
      // System.out.println("type ="+k);
      if(k==NDSClient.FLOAT)
        return nds.getFloatData();
      else if(k==NDSClient.INT) return nds.getIntData();
      else if(k==NDSClient.BYTE) return nds.getByteData(); 
      else if(k==NDSClient.STRING) return nds.getStringData();   
      else if(k==NDSClient.DOUBLE)return nds.getDoubleData(); 
      else{ //if(k==NDSClient.FILE) 
        errormessage="imporper return type";
        return null;
      }
    }catch(IOException s){ 
      errormessage=s.getMessage();
      return null;
    }
  }

  public Attr getAttribute(int index){
    Object X;
    String def;
    errormessage="";
    try{
      if(getNodeClass().equals("SDS")){
        def=getDef().trim();
        if(def==null){
          errormessage="Improper node152";
          return null;
        }
        int ii=def.lastIndexOf('/',def.length()-2);
        if(ii<0){
          errormessage="Improper node153";
          return null;
        }
        def=def.substring(0,ii+1)+"SDS "+getNodeName();
      }else
        def=getDef()+"NXVGROUP";

      //System.out.println("getAttr argsto="+getFile()+"::"+def);
      if(!nds.getAttrib( getFile(),def)){
        errormessage= nds.getStringData();
        return null;
      }
    }catch(IOException s){
      errormessage=s.getMessage();
      return null;
    }
    if(nds.getType()!=NDSClient.STRING){
      errormessage="improper result";
      return null;
    }
    String S1=nds.getStringData();
    if(S1==null) return null;
    if(S1.length()<=0) return null;
    int n=0;int i;
    for(i=S1.indexOf('\n');(i+1<S1.length())&&(i>=0)&&(n<=index);i=S1.indexOf('\n',i+1)){
      n++;
    }
       
    if(i<0)
      if(S1.indexOf(S1.length()-1)!='\n'){
        n++;
        i = S1.length();
      }
    if(n<index){
      errormessage="No more Attributes";
      return null;
    }   
    int j=S1.lastIndexOf('\n',i-1);
    if(j<0)
      j=0;
    else
      j++;
    errormessage="improper attribute list";
    String Aname=S1.substring(j,i);
    if(Aname==null) return null;
    int k=Aname.indexOf('@');
    if(k<0)return null;
    if(Aname.length()>=k+3)if(Aname.indexOf("SDS",k+1)==k+1){
      String key=Aname.substring(0,k);
      String SS= getDef();
      if(SS== null) return null;
      
      i=SS.length();
      if(i>1) i=SS.lastIndexOf('/',i-1);
      if(i<0)i=0;
      if(SS.charAt(i)=='/')
        SS=SS.substring(0,i+1);
      else
        SS="/";
      SS=SS+"SDS "+key;
      //System.out.println("getAttr args="+getFile()+"::"+SS);
      try{
        if(!nds.getData(getFile(),SS)){
          errormessage=nds.getStringData();
          return null;
        }
      }catch(IOException s){
        errormessage=s.getMessage();
        return null;
      }
              
      if(k==NDSClient.FLOAT)
        X=(Object)nds.getFloatData();
      else if(k==NDSClient.INT)
        X=(Object)nds.getIntData();
      else if(k==NDSClient.BYTE)
        X=(Object)nds.getByteData(); 
      else if(k==NDSClient.STRING)
        X=(Object)nds.getStringData();   
      else if(k==NDSClient.DOUBLE)
        X=(Object)nds.getDoubleData(); 
      else{ //if(k==NDSClient.FILE) 
        errormessage="imporper return type";
        return null;
      }

      return (Attr)(new ndsSvAttr(key,X));
    }
    errormessage="";      
    return (Attr)(new ndsSvAttr(Aname));
  }

  //Trailing slash not incorporated. Leading yes
  public  String getDef(){
    String S ;int i;
    S ="";  
    //System.out.println("Node size="+Node.size());  
    if(1>=Node.size())return "";  
    for(i=1;i< Node.size();i++)
      S = S+"/"+(String)(Node.elementAt(i));
    return S+"/";
  }

  public String getFile(){
    String S = (String)(Node.elementAt(0));
    return S.substring(0,S.indexOf(','));
  }

  public String show(){
    String Res="";
    Res = "file="+getFile()+", class="+getNodeClass()+", name="+getNodeName();
    return Res;
  }

  public void close(){
    nds.disconnect();
  } 
  
  //LINKING ROUTINES
  public String getLinkName(){
    return getDef();
  }
   
  public boolean equals( String linkName){
    Vector V = new Vector();
    int j=0;
    for(int i = linkName.indexOf(','); i>=0; i =linkName.indexOf(',',i+1)){
      V.addElement( linkName.substring( j, i));
      j=i+1;
    }
    V.addElement( linkName.substring(j));
    if( V.size()<1) return false;
    if( Node.size()<1) return false;
    if( (V.lastElement()).equals(Node.lastElement())){
      // do nothing
    }else
      return false;

    //Can get values and children and attributes and compare
    // to guess?? same node
    return false;  
  }
 
  public static void main(String args[]){
    NdsSvNode node; char c;
    NDSClient ndsclient= new NDSClient("dmikk.mscs.uwstout.edu",6008,6081998);
    String filename= "lrcs3000.hdf";
    if(!ndsclient.connect()){
      System.out.println("Did not connect");
      System.exit(0);
    }
    c=0;
    int i=0;
    int a=0;
    Vector V = new Vector();
    
    V.addElement(filename+","+"File");
    NdsSvNode parent=new NdsSvNode(V,ndsclient);
    node=parent;
    while (c!='x'){
      System.out.println("Enter option");
      System.out.println("  n:getNode");
      System.out.println("  s:show node");
      System.out.println("  c:GetChild");
      System.out.println("  n:next child");
      System.out.println("  a:get attribute");
      System.out.println("  v:get node value");
      System.out.println("  C:get node with name title");
      System.out.println("  A:get attribute with name file_name");
      try{
        c=0;
        while(c<' ')c=(char)System.in.read();
      }catch(IOException s){
        c=0;
      }
             
      if(c=='s'){
        System.out.println(parent.getNodeName()+"::"+parent.getNodeClass()+"::"
             +parent.getFile()+"::"+parent.getDef());
      }else if(c=='c'){
        NdsSvNode node1=(NdsSvNode)(parent.getChildNode(i));
        if(node1==null){
          System.out.println("No Children or"+node.getErrorMessage());
          parent=new NdsSvNode(V,ndsclient);
          i=0;a=0;
        }else
          parent=node1;
        i=0;a=0;
      }else if(c=='n'){
        a=0; 
        NdsSvNode node1=(NdsSvNode)(parent.getChildNode(i));i++;
        if(node1==null){
          System.out.println("No Children or"+node.getErrorMessage());
          parent=new NdsSvNode(V,ndsclient);
          i=0;
        }else{
          System.out.println(node1.getNodeName()+"::"+node1.getNodeClass()+"::"
             +node1.getFile()+"::"+node1.getDef());
          node=node1;
        }
      }else if(c=='a'){
        ndsSvAttr A = (ndsSvAttr)(node.getAttribute(a));
        a++;
        if(A == null){
          System.out.println("no Attribute or"+node.getErrorMessage());
          a=0;
        }else
          System.out.println(A.getItemName()+","+A.DisplayValue());
      }else if(c=='v'){
        Object X =node.getNodeValue();
        if((!node.getErrorMessage().equals(""))||(X==null)){
          System.out.println("no value for node. err="+node.getErrorMessage());
        }else
          System.out.println( "value="+ ndsSvAttr.Display(X));
      }else if(c=='C'){
        NdsSvNode node1=(NdsSvNode)(parent.getChildNode("title"));
        if(node1==null){
          System.out.println("No Children or"+node.getErrorMessage());
          parent=new NdsSvNode(V,ndsclient);
          i=0;a=0;
        }else
          parent=node1;
        i=0;
        a=0;
      }else if(c=='A'){
        Object A = (node.getAttrValue("file_name"));
        a++;
        if(A == null){
          System.out.println("no Attribute or"+node.getErrorMessage());
          a=0;
        }else
          System.out.println("file_name"+","+ndsSvAttr.Display(A));
      }else if(c=='x'){
        ndsclient.disconnect();
      }
    }
  }
}
