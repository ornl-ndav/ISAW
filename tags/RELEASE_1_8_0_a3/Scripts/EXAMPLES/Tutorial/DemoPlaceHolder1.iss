#          Demo of PlaceHolder properties
#@overview This scripts demonstrates the property of the newly supported
#     "opaque data type" supported by the ISAW Scripting language.
#     Previously, this was implemented using elements of the Array(Vector)
#@param  A Data Set with a SampleOrientation attribute. Its value will be
#          stored in a variable with a PlaceHolder(opaque) data types
#@return  The SampleOrientation attribute for this DataSet
#@error   Algebraic type Operations were performed on the contents of a
#         PlaceHolder(opaque) variable when its contents were not one of
#         the other supported data types

$Category=Macros, Examples, Scripts ( ISAW )

$DS     DataSet         Enter Data Set

DetPos = GetAttr(DS,3,"Effective Position")
Display DetPos

# Following is an error. "+" is not supported for DetPos Objects
Display DetPos+1

#Can create then call operators to extract the desired information
Display DetPosGet(DetPos,"Cartesian")

#Scripts can have these variables as parameters too
Display ScriptWDetPosParam( DetPos, "Hi There")

#There is no type checking when assigning data to a variable with an opaque data type
#   This would result in an error if a variable was initially assigned one of the
#   other data types
DetPos = 5
DetPos ="abc"
Display DetPos&"xxx"

#Can return an opaque data type
return DetPos

