' ----------------------------------------------
'
' Merge the properties files to one subdir.
' For easy analyzing by Kettle Transformation "TranslationAnalyzer.ktr"
'
' Sorry for beeing in MS VBS... it was a quick shot 
' -> sometimes the cron wizard spends me time...
' In spite of everything this gives an example of an 
' alternative to .BAT or .CMD automatisation in MS environments:
' VBS-files use the MS windows Scripting host (WSH)
' ----------------------------------------------

' **********************************************************************
' **                                                                   **
' **               This code belongs to the KETTLE project.            **
' **                                                                   **
' ** Kettle, from version 2.2 on, is released into the public domain   **
' ** under the Lesser GNU Public License (LGPL).                       **
' **                                                                   **
' ** For more details, please read the document LICENSE.txt, included  **
' ** in this project                                                   **
' **                                                                   **
' ** http://www.kettle.be                                              **
' ** info@kettle.be                                                    **
' **                                                                   **
' **********************************************************************/
' *
' * Created on 09-may-2006
' *
' * @author Jens

Option Explicit


Dim fso
Dim WshShell
Dim sParentDir
Dim sTargetDir


Set fso = CreateObject("Scripting.FileSystemObject")
Set WshShell = CreateObject("WScript.Shell")


sParentDir = InputBox("Enter base directory:", _
                      "Enter base directory", "C:\Develop\Kettle\Kettle-src-2.3.0\src\be\ibridge\kettle")
If sParentDir = "" Then
   MsgBox "No directory entered."
   WScript.Quit
End If


sTargetDir = InputBox("Enter target directory:", _
                      "Enter target directory", "C:\Develop\Kettle\translations\analyze")
If sTargetDir = "" Then
   MsgBox "No directory entered."
   WScript.Quit
End If


if right(sTargetDir,1)<>"\" then
   sTargetDir=sTargetDir & "\"
end if

MsgBox "Ready to start... after pressing OK it will work and after some seconds gives you a message when ready."


Call mergeThem(sParentDir)


MsgBox "Properties files written to: " & sTargetDir


' ----------------------------------------------------
Function mergeThem(sDir)
' ----------------------------------------------------
Dim oParent,oFolders, oFiles
Dim fo, fi
Dim sOld, sNew
    
   'recursively - subdirs
   Set oParent = fso.GetFolder(sDir)
   Set oFolders = oParent.SubFolders
   For Each fo In oFolders
      Call mergeThem(fo)
   Next
   
   'our prop files
   Set oFiles = oParent.files
   For Each fi In oFiles
      sOld=fi.path 
      if right(lcase(sOld),10)="properties" and left(lcase(fi.name),8)="messages" and len(fi.Name)=len("messages_xx_XX.properties") then
         'replace base directory by target directory, replace all \ by .
         sNew=mid(sOld,len(sParentDir)+2)
         sNew=sTargetDir & replace(sNew,"\",".")
         'for easy handling later separate filename with '-'
         'sNew=left(sNew,len(sNew)-len(fi.Name)-1) & "-" & fi.Name
         call fso.CopyFile (sOld, sNew)
      end if
   Next
End Function