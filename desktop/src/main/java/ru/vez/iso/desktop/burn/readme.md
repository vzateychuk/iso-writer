# Disk Burn 

Java does not have direct access to the hardware from pure Java code. You'd have to use native code loaded via IMAPI.
So, there are services/tools for disk burn. They uses Microsoft IMAPI2 (Com) as recorder.

Based on: 
[Reading and Writing to a DVD/CD - Java](https://stackoverflow.com/questions/8556291/reading-and-writing-to-a-dvd-cd-java)
[COM4j - Java tool that imports a COM type library and generates the Java definitions of the library](https://com4j.kohsuke.org/tutorial.html)
[COM4j - GitHub code](https://github.com/kohsuke/com4j)
[MSDN Using IMAPI](https://docs.microsoft.com/en-gb/windows/win32/imapi/using-imapi)
[Multi-Media Commands](http://www.13thmonkey.org/documentation/SCSI/mmc6r02g.pdf)
