package DataSetTools.viewer.Table;
import DataSetTools.operator.*;
import DataSetTools.operator.DataSet.*;
import DataSetTools.dataset.*;
import java.util.*;
public class MOperator extends DataSetOperator
     { int paramPos = -1;
       Object DefValue = null;
       String Title = "xxx";
       DataSetOperator op = null;
       public  MOperator( DataSetOperator op , int paramPos , Object DefValue )
          {super( op.getTitle());
     
           Title = op.getTitle();
           this.paramPos = paramPos;
           this.op = op;
           this.DefValue = DefValue;
     
           setDefaultParameters();
          }
       public MOperator()
         {super( "unknown" );          
          setDefaultParameters();
         }
       public String getCommand()
         { return op.getCommand();
         }
       public void setDefaultParameters()
         {
           if( op == null )
             return;
           parameters = new Vector();        
           CopyParametersFrom( op );   
           parameters.remove( paramPos );      
        }

       public DataSet getDataSet()
          {return op.getDataSet();
          }
       public Object getResult()
         {
           for( int i = 0 ; i < paramPos ; i++ )       
              op.setParameter(  getParameter( i ) , i );
      
    
           op.setParameter( new Parameter( "ttt" , DefValue ) , paramPos );
     
           for( int i = paramPos + 1 ; i < op.getNum_parameters() ; i++ )
              op.setParameter( getParameter( i - 1 ) , i );        
     
           return  op.getResult();
         }
       public Object clone()
         {MOperator Res = new MOperator( op , paramPos , DefValue );
           return Res;
         }
   }//MOperator
