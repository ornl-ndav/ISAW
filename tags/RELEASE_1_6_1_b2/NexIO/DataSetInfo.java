package NexIO;


  public class DataSetInfo{
    NxNode NxentryNode, NxdataNode;
    int startGroupID;
    int endGroupID;
    String label;   //or field name other than time for NXlog
    /**
    *   NxdataNode is null for monitors.  All monitors are merged
    *   NxdataNode may be a NXlog node
    */
    public DataSetInfo( NxNode NxentryNode,NxNode NxdataNode,
                   int startGroupID,int EndGroupID, String label){
       this.NxentryNode = NxentryNode;
       this.NxdataNode = NxdataNode ;
       this.startGroupID = startGroupID;
       this.endGroupID = EndGroupID ;
       this.label = label;
    }

  }//DataSetInfo
