# **About**
This project contains code for a real-time simulator of an operating system running and managing multiple processes. How the processes are created and managed by the operating system is handled using an input file containing various parameters that the program automatically parses through. The program also includes a debugging menu with multiple options for how much information will display on the console.

# **Creating the input file**
Creating a valid input file for the program to parse successfully requires the input file to be within the same directory as the program, and the input file must also be a .txt file. Within the .txt file, the file must contain the following parameters **[Case sensitive]**:
- totalSimulationTime
- quantum
- contextSwitchTime
- averageProcessLength
- averageCreationTime
- IOBoundPct
- averageIOserviceTime

For each parameter, there must be a number entered to the left of the parameter. The program reads the numbers in milliseconds so to make one parameter last a second you will have to type 1000 before a parameter. The .txt file can also contain comments. To write a valid comment, the comment must start with a #. Multiple lined comments are supported as long as the first character of the line starts with a #. If there is any confusion on how to create a proper text file for the program to run, there are several text files created within this folder to look at and edit.
