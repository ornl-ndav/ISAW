/*
 * Created on Jul 11, 2005
 *
 */
package Command;

import DataSetTools.operator.*;
/**
 * @author MikkelsonR
 *
 *
 */
public class OpnInfo implements java.io.Serializable{
     public String FileName, ClassName;
     long lastmodified;
     transient Operator op = null;
     public String CommandName, Title;
     boolean isHidden;
     public int NArgs;
     public String[] CatList;
     public String ToolTip;
     
     public OpnInfo( Operator op){
         this.op = op;
         CommandName = op.getCommand();
         ClassName= op.getClass().toString();
         if( (ClassName != null) &&(ClassName.trim().startsWith("class")))
            ClassName = ClassName.substring(6).trim();
         if( op instanceof ScriptOperator)
           FileName = op.getSource();
         else if( op instanceof PyScriptOperator)
           FileName = op.getSource();
         else
           FileName = null;
         Title = op.getTitle();
         NArgs = op.getNum_parameters();
         ToolTip = op.getSource();
          isHidden = false;
          if( op instanceof HiddenOperator )
             isHidden = true;
          CatList= op.getCategoryList();        
      }
      public OpnInfo( String FileName, String ClassName, long lastmodified, boolean isHidden,
                      String  CommandName,String  Title,int NArgs, String[] CatList, 
                      String ToolTip ) 
                                             throws IllegalArgumentException{
          this.FileName = FileName;
          this.ClassName = ClassName;
          this.lastmodified= lastmodified;
          this.CommandName = CommandName;
          this.Title = Title;
          this.NArgs =NArgs;
          this.CatList= CatList;
          this.isHidden= isHidden;
          this.ToolTip = ToolTip;
          
          
                                   
     }
  
    public Operator getOperator(){
      try{
        if(op != null)
           return op;
        Operator op=null;
        if( FileName != null)
           op= Script_Class_List_Handler.myGetClassInst( FileName, true);
        else if( ClassName != null)
    
           op= (Operator)(Class.forName( ClassName)).newInstance();
        this.op = op;
        return op;
      }catch(Exception s){
        return null;
    }
   }

}