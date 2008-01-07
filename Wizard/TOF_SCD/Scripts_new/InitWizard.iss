#                New Initial Peaks Wizard-Script Form

#@overview  This Script adds and links together the scripts for the 
#           InitialPeaks wizard. To get this in the menus it must be
#           moved somewhere pointed to by ISAW_HOME or GROUPx_HOME
#
#@algorithm  The appropriate forms are added to the wizard and the
#            links are set. The wizard save file is read and those 
#            values are set in for the initial values.

# @param  filename   The wizard save filename
#
#  


$Category = Macros, Examples, Scripts ( ISAW )

#------------------ Parameters-----------------
$filename     LoadFileString      Enter default file

#----------------- Code ------------------
#Set up the Script directory
isawHome=getSysProp("ISAW_HOME")
formHome=isawHome&"/Wizard/TOF_SCD/Scripts_new/"
#Assign the forms to use
form1=formHome&"find_multiple_peaks1.iss"
form2=formHome&"Blind.iss"
form3=formHome&"JIndex_Init1.iss"
form4=formHome&"Scalar.iss"
form5=formHome&"LsqrsInit.iss"
form6=formHome&"JIndxSave.iss"

#Create an instance of a Wizard object
a = createWizard("InitialPeaksWizardScriptVers",false)

#Add forms to the Wizard
addScriptForm(a, form1, "PlaceHolder", "Peaks", "", [])
addScriptForm(a, form2, "Array", "Orientation Matrix", "", [0,6])
addScriptForm(a, form3, "String", "Result", "", [0,1,6,7])
addScriptForm(a, form4, "String", "Transformation from Scalar", "", [0,3])
addScriptForm(a, form5, "Array", "Orientation Matrix", "", [0,5,10])
addScriptForm(a, form6, "String", "Result", "", [0,1,6,7])

#Create links between forms

#Link for peaks
links[0]=[13, 0, 0,-1, 0, 0]
       
#Link for init UB matrix
links[1]=[-1, 7, 1, 0,-1,-1]

#Link for Transformation
links[2]=[-1,-1,-1, 6, 5,-1]

#Link for lsqrs UB matrix
links[3]=[-1,-1,-1,-1,11, 1]

#Link for path
links[4]=[1 , 6 , 6, 3,10, 6]

#Link for expname
links[5]=[3 ,-1 ,7,-1, -1,7]


#Add links to this Wizard
wizardLinkParameters(a,links)


#Load the Wizard
wizardLoader(a)


return "success"
