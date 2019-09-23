@echo off
set project="CleanupPath"

MkDir "C:\Temp\%project%\Documents\"
echo "Hello" > "C:\Temp\%project%\Documents\new.txt"
MkDir "C:\Temp\%project%\Documents\Assembly Procedures\"
echo "Hello" > "C:\Temp\%project%\Documents\Assembly Procedures\new.bak"
MkDir "C:\Temp\%project%\Documents\Design Requirements\"
echo "Hello" > "C:\Temp\%project%\Documents\Design Requirements\new.bak"
MkDir "C:\Temp\%project%\Documents\Test Procedures\"
echo "Hello" > "C:\Temp\%project%\Documents\Test Procedures\new.bak"

MkDir "C:\Temp\%project%\Drawings\"
echo "Hello" > "C:\Temp\%project%\Drawings\new.bak"
MkDir "C:\Temp\%project%\Drawings\Assembly Drawings\"
echo "Hello" > "C:\Temp\%project%\Drawings\Assembly Drawings\new.docx"
MkDir "C:\Temp\%project%\Drawings\Part Drawings\"
 
MkDir "C:\Temp\%project%\3D Models\"
MkDir "C:\Temp\%project%\3D Models\3D Printer\"