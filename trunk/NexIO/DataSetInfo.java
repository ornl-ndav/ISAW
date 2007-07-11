package NexIO;


  /**
   * This class is used to store information on the data sets in the NeXus file
   * so that the number of data sets and default group and detector ID's are
   * accurate 
   * 
   * @author Ruth
   *
   */
  public class DataSetInfo{
     
    public NxNode NxentryNode, 
                  NxdataNode;
    
    public int startGroupID;
    public int endGroupID;
    
    public int startDetectorID;
    public int endDetectorID;
    public int nelts;
    public int ndetectors;
    
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
       
       this.startDetectorID =-1;
       this.endDetectorID = -1;
       this.label = label;
       nelts = -1;
       ndetectors = -1;
    }
}//DataSetInfo
