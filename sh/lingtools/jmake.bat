@echo off
REM java make file for LING classes
CLS
echo java compilation started at %time%
echo Set Class Path
call SetClassPath.bat

echo compiling coordAmbiguityResults
call javac coordAmbiguityResults.java

echo compiling coordAmbiguityProc
call javac coordAmbiguityProc.java

echo compiling coordSent
call javac coordSent.java

echo compiling PatternType
call javac PatternType.java

echo compiling POSPatternSet
call javac POSPatternSet.java


echo compiling SketchEngineQueryObj
call javac SketchEngineQueryObj.java

echo compiling LingServer
call javac LingServer.java

REM echo compiling TreeMatcherDriver
REM call javac TreeMatcherDriver.java
echo make file complete at %time%...


