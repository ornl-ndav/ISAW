package Command;
import DataSetTools.operator.*;
import DataSetTools.operator.Generic.*;
import DataSetTools.dataset.*;
import DataSetTools.util.*;
import DataSetTools.components.ParametersGUI.*;
public abstract class ScriptProcessorOperator extends GenericOperator
                                                       implements IScriptProcessor ,
                                                                  IDataSetListHandler
 {
    
    String Title,Command;
    
    public ScriptProcessorOperator( String Title)
       {super(Title);
        this.Title = Title;
        Command ="UNKNOWN";
       
       }
    public ScriptProcessorOperator()
       {super("");
        this.Title ="";
         Command ="";
       
       }
    public abstract void execute1(javax.swing.text.Document Doc, int line);
     
    public abstract void setDocument( javax.swing.text.Document Doc);
      
    public abstract Object getResult();
     
   public abstract void  reset();
     
 
    public abstract void resetError();
      
   public abstract int getErrorCharPos();
     
 
   public abstract int getErrorLine() ;
      

   public abstract String getErrorMessage(); 
      

    public void setTitle( String Title)
      {this.Title= Title;
      }
    public String getTitle()
      {return Title;
      }
    public void setCommand( String command)
      {Command = command;
      }
    public String getCommand()
      {return Command;
      }     
   public  abstract String getVersion();

   public abstract void setLogDoc(javax.swing.text.Document doc); 
       
   public abstract void  addDataSet(DataSet dss) ;
       
 
 
 public abstract void  addIObserver(IObserver iobs);
  
public  abstract void  deleteIObserver(IObserver iobs) ;
   
 public abstract void deleteIObservers(); 
   

  
 public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener P);
   
  public abstract DataSet[] getDataSets();

  }
