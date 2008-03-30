from DataSetTools.operator.Generic.Load import GenericLoad
from DataSetTools.operator.DataSet.EditList import DataSetFastMerge
from DataSetTools.retriever import *
from DataSetTools.viewer import *

def getConcurrentIndices(banks, startIndex=0):
    start = startIndex
    stop = start
    for i in range(startIndex,len(banks)-1):
        if banks[i]+1 == banks[i+1]:
            stop = i+1
        else:
            return (start, stop)
    return (start,stop)

def condenseList(banks, **kwargs):
    if banks is None:
        raise ValueError, "Cannot condense empty list of banks"
    for i in range(len(banks)):
        banks[i] = int(banks[i])
    banks.sort()

    result = []
    (start, stop) = (0,0)
    length = len(banks)
    while stop < length:
        (start, stop) = getConcurrentIndices(banks, start)
        if stop >= length:
            break
        if start < stop:
            result.append("%s-%s" % (banks[start], banks[stop]))
        else:
            result.append(banks[start])
        start = stop+1

    # convert the remaining banks into strings
    result = map(lambda x: str(x), result)

    return ",".join(result)

def runPrefix(dataset):
    filename = dataset.getAttributeValue("File")
    stuff = filename.split("/")
    filename = stuff[-1]
    stuff = filename.split("\\")
    filename = stuff[-1]
    stuff = filename.split(".")
    return stuff[0]

def fixTitle(dataset, titles):
#    print "fixTitle(%s, %s)" % (dataset, titles) # REMOVE
    filename = runPrefix(dataset)
    if len(titles) == 1:
        title = titles[0]
    else:
        numBank = 0
        titles.sort()
        for item in titles:
            if item.startswith("bank"):
                numBank += 1
        if numBank == len(titles):
            bankLen = len("bank")
            for i in range(len(titles)):
                titles[i] = titles[i][bankLen]
            title = "banks " + condenseList(titles)
        else:
            title = ",".join(titles)
    dataset.setTitle("%s[%s]" % (filename, title))
    return dataset

class LoadMergeNXS(GenericLoad):
    def __init__(self):
        Operator.__init__(self, "LoadMergeNXS")

    def setDefaultParameters(self):
        self.super__clearParametersVector()
        self.addParameter(LoadFilePG("NeXus file", ""))
        self.addParameter(BooleanPG("Load Monitors", "False"))
        choicelist = ChoiceListPG("Plot data", "none")
        choicelist.addItem("3D View")
        choicelist.addItem("Image View")
        self.addParameter(choicelist)

    def getResult(self):
        # get the value of the parameters
        filename = self.getParameter(0).value
        loadMon = self.getParameter(1).value
        viewtype = self.getParameter(2).value
        if viewtype == "none":
            viewtype = None

        # load in the data
        retriever = NexusRetriever(filename)
        num = retriever.numDataSets()
        result = Vector()
        merged = None
        titles =[]
        for i in range(num):
            dataType = retriever.getType(i)
            if (dataType == Retriever.HISTOGRAM_DATA_SET) or loadMon:
                ds = retriever.getDataSet(i)
            else:
                ds = None
            if retriever.getType(i) == Retriever.HISTOGRAM_DATA_SET:
                titles.append(ds.getTitle())
                if merged is None:
                    merged = retriever.getDataSet(i)
                else:
                    merger = DataSetFastMerge(merged, retriever.getDataSet(i))
                    merged = merger.getResult()
            else:
                if loadMon:
                    ds.setTitle("%s[%s]" % (runPrefix(ds), ds.getTitle()))
                    result.add(ds)

        # fix the title
        # merged = fixTitle(merged, titles)

        # plot the data if requested
        if viewtype is not None:
            view = ViewManager(merged, viewtype)

        # return the data
        if loadMon:
            result.add(merged)
            return result
        else:
            return merged
