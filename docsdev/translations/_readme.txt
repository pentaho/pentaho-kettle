This code snippets should help:
- to give an overview of translations (e.g. compare two languages)
- to find not translated externalizations (TODO)
- translations that violate any proposals (TODO)

Before running, all properties files must be merged in one directory with
'TranslationMergeProps.vbs'

It was a quick shot, next time there is more flexibility ;-)

1) create a directory... 
   C:\Develop\Kettle\translations\analyze

2) run 'TranslationMergeProps.vbs' in Windows
   (Merge the properties files to one subdir)

3) look at transformation 'TranslationAnalyzer.ktr'