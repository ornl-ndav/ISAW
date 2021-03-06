$Category = Macros, Examples, Scripts ( ISAW )
$filename     LoadFileString      Enter default file

#Set up the Script directory
isawHome=getSysProp("ISAW_HOME")
formHome=isawHome&"/Wizard/TOF_SCD/Scripts_new/"
#Assign the forms to use
form1=formHome&"find_multiple_peaks1.iss"
form2=formHome&"JIndxSave1.iss"
form3=formHome&"LSqrs.iss"
form4=formHome&"JIndxSave2.iss"
form5=formHome&"integrate_multiple_runs.iss"

#Create an instance of a Wizard object
a = createWizard("DailyPeaksWizardScriptVers",false)

#Add forms to the Wizard
addScriptForm(a, form1, "PlaceHolder", "Peaks", "", [])
addScriptForm(a, form2, "Array", "Result1", "", [0,6,7])
addScriptForm(a, form3, "Array", "Result", "", [0,1,2,4])
addScriptForm(a, form4, "PlaceHolder", "Peaks", "", [0,4,5,6,8])
addScriptForm(a, form5, "String", "Result", "", [0,1,2,3,5,6,9,10])

#Create links between forms
#Link for PATH data
links[0]=[0,-1,-1,-1,0]
#Link for output path
links[1]=[1,6,4,4,1]
#Link for Runnums
links[2]=[2,-1,2,5,2]
#Link for exp name
links[3]=[3,  7, 1, 8, 3]
#Link for peaks
links[4]=[13, 0, 0, 0,-1]
#Link for instName
links[5]=[11,-1,-1,-1, 9]
#Link for FileExt
links[6]=[12,-1,-1,-1,10]
#link for useCalib filename
links[7] =[8, -1,-1,-1, 5]
#Link for calibFilename
links[8]=[9, -1,-1,-1, 6]
#Link for RestrRuns ????
links[9]=[-1, 2,-1, 6,-1]
#Link for Filename to save peaks to ??
links[10]=[-1, 4,-1,-1,-1]

#Add links to this Wizard
wizardLinkParameters(a,links)

#Load the Wizard
wizardLoader(a)