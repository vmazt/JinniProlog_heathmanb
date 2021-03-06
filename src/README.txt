The Prolog compiler and runtime system are in directory
MainProlog.

You can start running Prolog there, right away, by typing 

jinni

or

jinni.bat

on OS X + Linux/Unix or Windows.

The script "makejinni" in directory "build" regenerates 
the system by compiling various Java and Prolog components.

Here is the directory for MainProlog structure.

+---bin <=========== START HERE TO WORK WITH JINNI
|   +---agentlib => agents
¦   +---classlib => Object Oriented Prolog classes
¦   +---progs    => classic Prolog programs
¦   +---vprogs   => visual programs
|   + user guide and demos
+---build  scripts to generate various components
+---prolog  JAVA SOURCES
¦   +---kernel 
|   +---core 
+---psrc  PROLOG SOURCES
+---tests 

The distribution contains a few related projects, untested
recently but most likely easy to get running.

Scripts (mostly .bat files) in directories other than
MainProlog have not been tested recently but might
be useful, especially on Windows platforms.

CallProlog - an interface to call Jinni from Java
GP - a simple Genetic Programming framework
PROLOG3D - a Java3D interface to Jinni.

Please read the JinniUserGuide.html file in directory "bin"
for more information.
 