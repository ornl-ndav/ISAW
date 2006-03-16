$Category = Macros, Examples, Scripts ( ISAW )
$filename     LoadFileString      Enter default file

isawHome=getSysProp("ISAW_HOME")
formHome=isawHome&"/Wizard/TOF_SCD/Scripts_new/"
form1=formHome&"find_multiple_peaks1.iss"
form2=formHome&"JIndxSave1.iss"
form3=formHome&"LSqrs.iss"
form4=formHome&"JIndxSave2.iss"
form5=formHome&"integrate_multiple_runs.iss"


a = createWizard("DailyPeaksWizardScriptVers",false)


addScriptForm(a, form1, "PlaceHolder", "Peaks", "", [])
addScriptForm(a, form2, "Array", "Result1", "", [0])
addScriptForm(a, form3, "Array", "Result", "", [0,1,2,4])
addScriptForm(a, form4, "PlaceHolder", "Peaks", "", [0,3,4,8])
addScriptForm(a, form1, "String", "Result", "", [0,1,2,3,4,5,8,9])
links=[[0,-1,-1,-1,0],[1,-1,4,3,1],[2,-1,2,4,2],[3,-1,1,8,3],[12,0,0,0,-1],[10,-1,-1,-1,8],[11,-1,-1,-1,9],[8,-1,-1,-1,5],[-1,2,-1,5,-1],[-1,4,-1,-1,-1]]
wizardLinkParameters(a,links)


wizardLoader(a)