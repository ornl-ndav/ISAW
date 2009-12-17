package EventTools.ShowEventsApp.Command;

public class IndexedPeaksFilterCmd
{
    private String IndexedPeakFile;
    private boolean FilterNonIndexedPeaks;
    
    public IndexedPeaksFilterCmd(String IndexedPeakFile,
                                 boolean FilterNonIndexedPeaks)
    {
        this.IndexedPeakFile = IndexedPeakFile;
        this.FilterNonIndexedPeaks = FilterNonIndexedPeaks;
    }

    public String getIndexedPeakFile()
    {
        return IndexedPeakFile;
    }

    public boolean isFilterNonIndexedPeaks()
    {
        return FilterNonIndexedPeaks;
    }

    public String toString()
    {
       return "\nIndex Peak File : " + getIndexedPeakFile() +
              "\nFilter Non Indexed Peaks : " + isFilterNonIndexedPeaks();
    }
}
