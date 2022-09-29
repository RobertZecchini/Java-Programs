import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

/*
Program Name: Simulation Driver
Programmer Name: Robert S. Zecchini
Version: 1.0
Purpose: This driver runs a simulation that will go through all of the processes
		that are in an input file using the input file's parameters until all of
		the processes are complete.
*/

public class SimDriver {
	public static void main(String[] args) {
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////     Variables     //////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//Declaring variables.
		boolean isRunning = false;
		boolean isRunningIO = false;
		int ctr = 0;
		int cpuctr = 0;
		int base = 0;
		int IOServiceCount = 0;
		long startTime = System.nanoTime();
		long endTime = System.nanoTime();
		long setTime = System.nanoTime();
		long contextTime = System.nanoTime();
		long CPUTime = 0;
		long totalTime = (endTime - startTime) / 1000;
		long totalSimTime = 0;
		long quantum = 0;
		long contextSwitchTime = 0;
		long averageProcessLength = 0;
		long averageCreationTime = 0;
		long IOBoundPct = 0;
		long averageIOserviceTime = 0;
		ArrayList<PCB> PCBs = new ArrayList<PCB>();
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////       Input       //////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//Asking the user to input the file they want to use for the schedule simulator.
		System.out.println("Please type the file you want to use for the schedule simulator.");
		
		//Getting input from user.
		Scanner uInpt = new Scanner(System.in);
		String fname = uInpt.nextLine();
		
		
		//Asking the user what mode they want to run the simulator.
		System.out.println("Please type the Debug mode you want to run in for the schedule simulator.");
		System.out.println("0 None");
		System.out.println("1 File Input/Basic Process Info");
		System.out.println("2 File Input/Basic Process Info and Process Creation");
		System.out.println("3 File Input/Basic Process Info, Process Creation, and Process Activity");
		System.out.println("4 File Input/Basic Process Info, Process Creation, Process Activity, and Process Lifetime");
		System.out.println("5 File Input/Basic Process Info, Process Creation, Process Activity, Process Lifetime, and Runtime");
		
		//Getting input from user.
		String dname = uInpt.nextLine();
		
		//Closing keyboard listener.
		uInpt.close();
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////    File Reader    ////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//Opening file.
		try {
			//Getting file based off of user input.
			File mText = new File("src/" + fname + ".txt");
			
			//Did the user input a .txt extension?
			if(fname.contains(".txt"))
				mText = new File("src/" + fname);
			
			//Creating scanner to read each line of the file.
			Scanner mReader = new Scanner(mText);
			
			//Reading file.
			while(mReader.hasNextLine()) {
				//Getting data from the current line the scanner is on.
				String data = mReader.nextLine();
				
				//Are we currently viewing a comment?
				if(data.substring(0, 1).equals("#")) {
					++ctr;
					base = ctr;
					continue;
				}
				
				//Are we no longer viewing a comment?
				else {
					
					//Splitting the string if there's a space.
					String[] dataset = data.split(" ");
					
					//Splitting the string if tab was used instead of space.
					if(dataset.length <= 1)
						dataset = data.split("\t");
					
					//Getting the totalSimulationTime.
					if(ctr == base) {
						//Do we have debug settings enabled?
						if(Integer.parseInt(dname) > 0)
							System.out.println("END OF COMMENTS AT LINE: " + ctr);
						totalSimTime = Long.parseLong(dataset[0]);
						++ctr;
						continue;
					}
					
					//Getting the quantum.
					if(ctr == base + 1) {
						quantum = Long.parseLong(dataset[0]);
						++ctr;
						continue;
					}
					
					//Getting the contextSwitchTime.
					if(ctr == base + 2) {
						contextSwitchTime = Long.parseLong(dataset[0]);
						++ctr;
						continue;
					}
					
					//Getting the averageProcessLength.
					if(ctr == base + 3) {
						averageProcessLength = Long.parseLong(dataset[0]);
						++ctr;
						continue;
					}
					
					//Getting the averageCreationTime.
					if(ctr == base + 4) {
						averageCreationTime = Long.parseLong(dataset[0]);
						++ctr;
						continue;
					}
					
					//Getting the IOBoundPct.
					if(ctr == base + 5) {
						IOBoundPct = Long.parseLong(dataset[0]);
						++ctr;
						continue;
					}
					
					//Getting the averageIOserviceTime.
					if(ctr == base + 6) {
						averageIOserviceTime = Long.parseLong(dataset[0]);
						++ctr;
						continue;
					}
				}
			}
			mReader.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////// Process  Creation //////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//Creating a random number of processes from 2 to 100.
		int pnum = (int)(Math.random() * (10 - 2 + 1) + 2);
		
		//Increasing our pnum range depending on our totalSimTime.
		if(totalSimTime >= 10)
			pnum = (int)(Math.random() * (100 - 2 + 1) + 2);
		
		if(totalSimTime >= 100)
			pnum = (int)(Math.random() * (1000 - 2 + 1) + 2);
		
		if(totalSimTime >= 1000)
			pnum = (int)(Math.random() * (10000 - 2 + 1) + 2);
		
		if(totalSimTime >= 10000)
			pnum = (int)(Math.random() * (100000 - 2 + 1) + 2);
		
		if(totalSimTime >= 100000)
			pnum = (int)(Math.random() * (1000000 - 2 + 1) + 2);
		
		//Declaring variables for the exponential distribution generator.
		long sum = 0;
		double lambda = (1.0/averageProcessLength);
		
		//Generating random numbers for each process that adds up to the averageProcessLength.
		for(int i = 0; i < pnum; ++i) {
			//I had to create my own exponential distribution from scratch since Java doesn't have an 
			//exponential distribution class! This was done by using the exponential distribution equation. 
			//It should be noted that the average doesn't end up being the exact same as the 
			//averageProcessLength; however, it gets really close to the average.
			Random rand = new Random();
							
			//Exponential distribution equation.
			long x = (long)(Math.log(1-rand.nextDouble()) / (-(lambda)));
			
			//Creating new PCBs with i as index and x as duration.
			PCBs.add(new PCB(i, x));
			
			//Are we at debug level 2?
			if(Integer.parseInt(dname) > 1) {
				System.out.println("RANDOM: " + Math.log(1 - rand.nextDouble()) / -(lambda));
				System.out.println("PROCESS " + i + " LENGTH: " + x);
				System.out.println();
			}
			
			sum += x;
		}
		
		//Outputting sum and average for averageProcessLength.
		long average = sum/pnum;
		
		//Are we at debug level 1?
		if(Integer.parseInt(dname) > 0) {
			System.out.println("SUM: " + sum);
			System.out.println("AVERAGE: " + average);
			System.out.println("averageProcessLength: " + averageProcessLength);
			System.out.println();
		}
		
		//Redeclaring variables for the exponential distribution generator.
		sum = 0;
		lambda = (1.0/averageCreationTime);
		
		//Generating random numbers for each process that adds up to the averageCreationTime.
		for(int i = 0; i < pnum; ++i) {
			//This also uses the custom exponential distribution.
			Random rand = new Random();
				
			//Exponential distribution equation.
			long x = (long)(Math.log(1-rand.nextDouble()) / (-(lambda)));
			
			//Assigning x to process in PCB ArrayList.
			PCBs.get(i).setStartTime(x);
			
			//Are we at debug level 2?
			if(Integer.parseInt(dname) > 1) {
				System.out.println("RANDOM: " + Math.log(1 - rand.nextDouble()) / -(lambda));
				System.out.println("PROCESS " + i + " CREATION TIME: " + x);
				System.out.println();
			}
			
			sum += x;
		}
		
		//Outputting sum and average for averageCreationTime.
		average = sum/pnum;
		
		//Are we at debug level 1?
		if(Integer.parseInt(dname) > 0) {
			System.out.println("SUM: " + sum);
			System.out.println("AVERAGE: " + average);
			System.out.println("averageCreationTime: " + averageCreationTime);
			System.out.println();
		}
		
		//Determining the amount of processes that need to be IOBound based on IOBoundPct.
		int IOb = (int)(Math.ceil(pnum * (IOBoundPct / 100.0)));
		int CPUb = pnum - IOb;
		
		//Are we at debug level 1?
		if(Integer.parseInt(dname) > 0) {
			System.out.println("IOb: " + IOb);
			System.out.println("CPUb: " + CPUb);
			System.out.println("TOTAL: " + (IOb + CPUb));
			System.out.println();
		}
		
		//Reusing counter variable.
		ctr = 0;
					
		//Randomly assigning which processes will be IOBound or CPUBound.
		while(ctr < pnum) {
			//Creating an RNG in range of 1 - 2.
			int boundDtr = (int)(Math.random() * (2 - 1 + 1) + 1);
			
			//Assign the rest of the processes as CPUBound if IOBound is no longer available.
			if(IOb <= 0) {
				//Are we at debug level 2?
				if(Integer.parseInt(dname) > 1)
					System.out.println("Created CPUBound Process");
				
				//Assigning boundDtr to process in PCB ArrayList.
				PCBs.get(ctr).setBound(boundDtr - 1);
				
				//Updating variables.
				++ctr;
				--CPUb;
			}
			
			//Assign the rest of the processes as IOBound if CPUBound is no longer available.
			else if(CPUb <= 0) {
				//Are we at debug level 2?
				if(Integer.parseInt(dname) > 1)
					System.out.println("Created IOBound Process");
				
				//Assigning boundDtr to process in PCB ArrayList.
				PCBs.get(ctr).setBound(boundDtr - 1);
				++ctr;
				--IOb;
			}
			
			//If boundDtr equals 1 and we still need IOBound processes, set this process as IOBound.
			else if(boundDtr == 1 && IOb != 0) {
				//Are we at debug level 2?
				if(Integer.parseInt(dname) > 1)
					System.out.println("Created IOBound Process");
				
				//Assigning boundDtr to process in PCB ArrayList.
				PCBs.get(ctr).setBound(boundDtr - 1);
				++ctr;
				--IOb;
			}
			
			//If boundDtr equals 2 and we still need CPUBound processes, set this process as CPUBound.
			else if(boundDtr == 2 && CPUb != 0) {
				//Are we at debug level 2?
				if(Integer.parseInt(dname) > 1)
					System.out.println("Created CPUBound Process");
				
				//Assigning boundDtr to process in PCB ArrayList.
				PCBs.get(ctr).setBound(boundDtr - 1);
				
				//Updating variables.
				++ctr;
				--CPUb;
			}
			
			//Are we at debug level 2?
			if(Integer.parseInt(dname) > 1) {
				System.out.println("boundDtr: " + boundDtr);
				System.out.println();
			}
		}
		
		//Creating CPU variable.
		PCB cpu = null;
		
		//Creating and declaring Ready, IOService, and Completion Queue.
		Queue<PCB> readyQ = new LinkedList<>();
		Queue<PCB> IOQ = new LinkedList<>();
		Queue<PCB> completeQ = new LinkedList<>();
		
		//Do we have debug settings enabled?
		if(Integer.parseInt(dname) > 0) {
			//Debug stuff.
			System.out.println();
			System.out.println("totalSimulationTime: " + totalSimTime);
			System.out.println("quantum: " + quantum);
			System.out.println("contextSwitchTime: " + contextSwitchTime);
			System.out.println("averageProcessLength: " + averageProcessLength);
			System.out.println("averageCreationTime: " + averageCreationTime);
			System.out.println("IOBoundPct: " + IOBoundPct);
			System.out.println("averageIOserviceTime: " + averageIOserviceTime);
			System.out.println("NUMBER OF PROCESSES CREATED: " + pnum);
			System.out.println();
		}

		//Is our debug settings greater than 2?
		if(Integer.parseInt(dname) > 2) {
			for(int i = 0; i < pnum; ++i)
				System.out.println(PCBs.get(i).toString());
			System.out.println();
		}
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////  Simulation Code  //////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//Redeclaring variables for the exponential distribution generator for the averageIOserviceTime.
		sum = 0;
		ctr = 0;
		lambda = (1.0/averageIOserviceTime);
		
		//Setting up initial setTime and endTime for starting the simulation loop.
		setTime = System.nanoTime();
		endTime = System.nanoTime();
		
		//Getting the time that we started our simulation.
		totalTime = (endTime - startTime) / 1000;
		
		//This variable tracks how long the user took to enter a file and debug settings.
		double timeOffset = totalTime / 1000000.0;
		
		System.out.println("Starting simulation at " + totalTime / 1000000.0 + " seconds.");
		
		//Run the simulation until we reach the totalSimTime starting from the timeOffset.
		while(totalTime / 1000000.0 < (totalSimTime + timeOffset)) {
			//Setting totalTime to the difference between our current time and the last time a process was created.
			totalTime = (endTime - setTime) / 1000;
			
			//Force the very first process to be created upon the start of the simulation.
			if(ctr == 0)
				PCBs.get(0).setStartTime(0);
			
			//Do we still have any processes in the PCB ArrayList?
			if(PCBs.size() > 0) {
				//Checking to see if the head of our PCB ArrayList can be created.
				if(totalTime >= PCBs.get(0).getStartTime()) {
					//Setting totalTime to the overall duration of the program.
					totalTime = (endTime - startTime) / 1000;
					
					//Updating setTime to the current time.
					setTime = endTime;
					
					//Getting process startTime before updating it.
					long pstarttime = PCBs.get(0).getStartTime();
							
					//Setting the time in which the process was created.
					PCBs.get(0).setStartTime(setTime);
					PCBs.get(0).setStateTime(setTime);
					
					//Change head to ready and move it to the ready queue if our process can be created.
					PCBs.get(0).updateState('R');
					readyQ.add(PCBs.get(0));
					
					//Is our debug settings greater than 1?
					if(Integer.parseInt(dname) > 1) {
						System.out.println("PROCESS " + PCBs.get(0).getPID() + " has been created at " + totalTime / 1000000.0 + " seconds!");
						System.out.println("PROCESS " + PCBs.get(0).getPID() + " CREATION TIME: " + pstarttime / 1000000.0 + " seconds.");
					}
					
					//Is our debug settings greater than 2?
					if(Integer.parseInt(dname) > 2) {
						System.out.println("PROCESS " + PCBs.get(0).getPID() + " has been moved to the ready queue at " + totalTime / 1000000.0 + " seconds!");
						System.out.println();
						System.out.println(PCBs.get(0).toString());
					}
						
					//Is there an element in our PCB ArrayList?
					if(PCBs.size() > 0) {
						PCBs.remove(0);
						++ctr;
					}
				}
			}
			
			//Setting totalTime to the overall duration of the program.
			endTime = System.nanoTime();
			totalTime = (endTime - startTime) / 1000;
			
			//Do we currently have any processes running on the CPU?
			if(cpu == null) {
				//If we have any processes in the ready queue assign the head process to the CPU.
				if(readyQ.size() > 0) {
					//Set the contextTime to our current time if the contextTime isn't currently running.
					if(!isRunning) {
						contextTime = System.nanoTime();
						isRunning = true;
						
						//Adding current time spent waiting in the ready queue to process ready time.
						readyQ.peek().setReadyTime(readyQ.peek().getTotalReadyTime() + ((endTime - readyQ.peek().getStateTime()) / 1000));
					}
					long curcontextTime = (endTime - contextTime) / 1000;
					
					//Is our debug settings greater than 4?
					if(Integer.parseInt(dname) > 4) {
						System.out.println("curcontextTime: " + curcontextTime);
						System.out.println("contextSwitchTime: " + contextSwitchTime);
					}
					
					//We must wait for the context switch to complete before we can completely move our process to the CPU!
					if(contextSwitchTime - curcontextTime <= 0) {
						isRunning = false;
						
						//Adding contextSwitchTime to process total time.
						readyQ.peek().setTotalTime((endTime - readyQ.peek().getStartTime()) / 1000);
						
						//Setting burst time for process.
						//Does our process currently have a burst time set?
						if(readyQ.peek().getBurstTime() <= 0) {
							//Is the process IO bound?
							if(readyQ.peek().getBound() == 0) {
								//Using normal distribution for burstTime.
								long burstTime = (long)(Math.random() * (2000 - 1000 + 1) + 1000);
								readyQ.peek().setBurstTime(burstTime);
								//Are we at debug level 3?
								if(Integer.parseInt(dname) > 2) {
									System.out.println("PROCESS " + readyQ.peek().getPID() + " BURST TIME: " + readyQ.peek().getBurstTime());
								}
							}
							
							//Is the process CPU bound?
							if(readyQ.peek().getBound() == 1) {
								//Using normal distribution for burstTime.
								long burstTime = (long)(Math.random() * (20000 - 10000 + 1) + 10000);
								readyQ.peek().setBurstTime(burstTime);
							}
							
							//Using exponential distribution for averageIOserviceTime.
							Random rand = new Random();
							
							//Exponential distribution equation.
							long x = (long)(Math.log(1-rand.nextDouble()) / (-(lambda)));
							//Are we at debug level 4?
							if(Integer.parseInt(dname) > 3) {
								System.out.println("RANDOM: " + Math.log(1 - rand.nextDouble()) / -(lambda));
								System.out.println("I/O SERVICE TIME: " + x);
							}
							
							sum += x;
							++IOServiceCount;
							
							//Setting the IOserviceTime for the process.
							readyQ.peek().setIOServiceTime(x);
						}
						
						//Moving process to CPU.
						cpu = readyQ.poll();
						cpu.setStateTime(endTime);
						cpu.updateState('!');
						
						//Is our debug settings greater than 2?
						if(Integer.parseInt(dname) > 2) {
							//Is our debug settings greater than 4?
							if(Integer.parseInt(dname) > 4)
								System.out.println();
							
							System.out.println("PROCESS " + cpu.getPID() + " has been moved to the CPU at " + totalTime / 1000000.0 + " seconds!");
							//Is our debug settings greater than 3?
							if(Integer.parseInt(dname) > 3)
								System.out.println("PROCESS LIFETIME: " + cpu.getTotalTime() / 1000000.0 + " seconds!");
							
							System.out.println();
							System.out.println(cpu.toString());
						}
					}
				}
			}
			
			//Decrease the total process time required for the process that's currently on the CPU.
			if(cpu != null) {
				//Decreasing CPU process remaining time.
				cpu.getLifeCycle();
				
				//Is our debug settings greater than 3?
				if(Integer.parseInt(dname) > 3) {
					System.out.println("PROCESS " + cpu.getPID() + " REMAINING CPU TIME: " + cpu.getLifeCycle() / 1000000.0 + " seconds");
					System.out.println("PROCESS " + cpu.getPID() + " REMAINING CPU BURST TIME: " + (cpu.getBurstTime() - cpu.getTime()) / 1000000.0 + " seconds");
				}
				
				//Is this process done running?
				if(cpu.getLifeCycle() <= 0) {
					//Adding current time spent on CPU to process total time and CPU time.
					cpu.setTotalTime((endTime - cpu.getStartTime()) / 1000);
					cpu.setTotalCPUTime((endTime - cpu.getStateTime()) / 1000);
					
					//Adding the amount of time the CPU spent executing this process to CPUTime.
					CPUTime += (endTime - cpu.getStateTime()) / 1000;
					
					//Incrementing how many times a process has entered the CPU.
					++cpuctr;
					
					//Terminating process.
					cpu.terminateProcess();
					
					//Is our debug settings greater than 1?
					if(Integer.parseInt(dname) > 1) {
						//Print out terminated process status.
						System.out.println("Terminiating " + cpu.toString());
						//Is our debug settings greater than 2?
						if(Integer.parseInt(dname) > 2) {
							System.out.println("PROCESS " + cpu.getPID() + " has completed it's process at " + totalTime / 1000000.0 + " seconds!");
							System.out.println("PROCESS " + cpu.getPID() + " COMPLETION TIME SINCE START OF SIMULATION: " + (totalTime - timeOffset) / 1000000.0 + " seconds!");
							System.out.println("PROCESS " + cpu.getPID() + " TURNOVER TIME: " + cpu.getTotalTime() / 1000000.0 + " seconds!");
							System.out.println("PROCESS " + cpu.getPID() + " has been terminated.\n");
							System.out.println(cpu.toString());
						}
					}
					
					//Adding completed process to completed queue.
					completeQ.add(cpu);
					cpu = null;
				}
				
				//Does the CPU currently have a process running and has that process hit it's burst time?
				if(cpu != null && cpu.getBurstTime() - cpu.getTime() <= 0) {
					//Adding current time spent on CPU to process total time and CPU time.
					cpu.setTotalTime((endTime - cpu.getStartTime()) / 1000);
					cpu.setTotalCPUTime((endTime - cpu.getStateTime()) / 1000);
					
					//Adding the amount of time the CPU spent executing this process to CPUTime.
					CPUTime += (endTime - cpu.getStateTime()) / 1000;
					
					//Incrementing how many times a process has entered the CPU.
					++cpuctr;
					
					//Setting the remaining CPU time and burst time for the process.
					cpu.setDuration(cpu.getDifference());
					cpu.setBurstTime(0);
					
					//Setting the state time and state of the process. Also incrementing I/O count of process.
					cpu.setStateTime(endTime);
					cpu.updateState('W');
					cpu.updateIOCalls(1);
					
					//Moving the process to IO service Queue.
					IOQ.add(cpu);
					
					//Is our debug settings greater than 2?
					if(Integer.parseInt(dname) > 2) {
						System.out.println("PROCESS " + cpu.getPID() + " has been moved to I/O Service at " + totalTime / 1000000.0 + " seconds!");
						System.out.println("PROCESS " + cpu.getPID() + " REMAINING CPU TIME: " + cpu.getDuration());
						System.out.println("PROCESS " + cpu.getPID() + " I/O SERVICE TIME: " + cpu.getServiceTime());
						//Is our debug settings greater than 3?
						if(Integer.parseInt(dname) > 3)
							System.out.println("PROCESS LIFETIME: " + cpu.getTotalTime() / 1000000.0 + " seconds!");
						System.out.println();
						System.out.println(cpu.toString());
					}
					
					//Removing process from CPU.
					cpu = null;
				}
				
				//Does the CPU currently have a process running and has that process hit the quantum limit?
				if(cpu != null && quantum - cpu.getTime() <= 0) {
					//Adding current time spent on CPU to process total time and CPU time.
					cpu.setTotalTime((endTime - cpu.getStartTime()) / 1000);
					cpu.setTotalCPUTime((endTime - cpu.getStateTime()) / 1000);
					
					//Adding the amount of time the CPU spent executing this process to CPUTime.
					CPUTime += (endTime - cpu.getStateTime()) / 1000;
					
					//Incrementing how many times a process has entered the CPU.
					++cpuctr;
					
					//Setting the remaining CPU time and burst time for the process.
					cpu.setDuration(cpu.getDifference());
					cpu.setBurstTime(cpu.getBurstTime() - (endTime - cpu.getStateTime()) / 1000);
					
					//Setting the state time and state of the process.
					cpu.setStateTime(endTime);
					cpu.updateState('R');
					
					//Moving the process to Ready Queue.
					readyQ.add(cpu);
					
					//Is our debug settings greater than 2?
					if(Integer.parseInt(dname) > 2) {
						System.out.println("PROCESS " + cpu.getPID() + " has been moved to Ready Queue at " + totalTime / 1000000.0 + " seconds!");
						System.out.println("PROCESS " + cpu.getPID() + " REMAINING CPU TIME: " + cpu.getDuration());
						//Is our debug settings greater than 3?
						if(Integer.parseInt(dname) > 3)
							System.out.println("PROCESS LIFETIME: " + cpu.getTotalTime() / 1000000.0 + " seconds!");
						System.out.println();
						System.out.println(cpu.toString());
					}
					
					//Removing process from CPU.
					cpu = null;
				}
			}
			
			//Do we currently have anything in the I/O Queue.
			if(IOQ.size() > 0) {
				//Start running an IO service request if we currently are not running one.
				if(!isRunningIO) {
					//Setting the start time in which a process has started running an IO service request.
					IOQ.peek().startIO(endTime);
					isRunningIO = true;
				}
				
				//Running the current head of the IO queue's service time.
				if(isRunningIO) {
					//Is our debug settings greater than 3?
					if(Integer.parseInt(dname) > 3)
						System.out.println("PROCESS " + IOQ.peek().getPID() + " REMAINING I/O SERVICE TIME: " + IOQ.peek().getServiceTime() / 1000000.0 + " seconds");
					
					//Is the head of the IO queue done running it's service time?
					if(IOQ.peek().getServiceTime() <= 0) {
						//Adding current time spent in IO to process total time and IO service time.
						IOQ.peek().setTotalTime((endTime - IOQ.peek().getStartTime()) / 1000);
						IOQ.peek().setTotalIOTime((endTime - IOQ.peek().getStateTime()) / 1000);
						
						//Setting the state time and state of the process.
						IOQ.peek().setStateTime(endTime);
						IOQ.peek().updateState('R');
						
						//Is our debug settings greater than 2?
						if(Integer.parseInt(dname) > 2) {
							System.out.println("PROCESS " + IOQ.peek().getPID() + " has been moved to Ready Queue at " + totalTime / 1000000.0 + " seconds!");
							System.out.println("PROCESS " + IOQ.peek().getPID() + " REMAINING CPU TIME: " + IOQ.peek().getDuration());
							//Is our debug settings greater than 3?
							if(Integer.parseInt(dname) > 3)
								System.out.println("PROCESS LIFETIME: " + IOQ.peek().getTotalTime() / 1000000.0 + " seconds!");
							System.out.println();
							System.out.println(IOQ.peek().toString());
						}
						
						//Moving the head of the IO queue back to the ready queue.
						readyQ.add(IOQ.poll());
						
						//Stop running isRunningIO.
						isRunningIO = false;
					}
				}
			}
			
			//Is our debug settings greater than 4?
			if(Integer.parseInt(dname) > 4)
				System.out.println("Current time is " + totalTime / 1000000.0 + " seconds");
		}
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////// Ending  Simulation  Code  //////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//Getting the remaining time for each process that's still running in either the CPU, Ready, or I/O. This needs to be done
		//since the execution time for each process within these queues are not accounted for after the simulation closes.
		//Getting CPU time.
		if(cpu != null ) {
			//Adding current time spent on CPU to process total time and CPU time.
			cpu.setTotalTime((endTime - cpu.getStartTime()) / 1000);
			cpu.setTotalCPUTime((endTime - cpu.getStateTime()) / 1000);
			
			//Adding the amount of time the CPU spent executing an active process to CPUTime.
			CPUTime += (endTime - cpu.getStateTime()) / 1000;
		
			//Adding the current active process that's in the CPU to our CPU counter.
			++cpuctr;
		}
		
		//Getting ready queue time.
		if(readyQ.size() > 0) {
			for(int i = 0; i < readyQ.size(); ++i) {
				//Adding current time spent waiting in the ready queue to process total time and ready time.
				readyQ.peek().setTotalTime((endTime - readyQ.peek().getStartTime()) / 1000);
				readyQ.peek().setReadyTime(readyQ.peek().getTotalReadyTime() + ((endTime - readyQ.peek().getStateTime()) / 1000));
				readyQ.add(readyQ.poll());
			}
		}
		
		//Getting IO service queue time.
		if(IOQ.size() > 0) {
			for(int i = 0; i < IOQ.size(); ++i) {
				//Adding current time spent in IO to process total time and IO service time.
				IOQ.peek().setTotalTime((endTime - IOQ.peek().getStartTime()) / 1000);
				IOQ.peek().setTotalIOTime((endTime - IOQ.peek().getStateTime()) / 1000);
				IOQ.add(IOQ.poll());
			}
		}
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////      Results      //////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//Output how much time it took to run the simulation.
		System.out.println("Execution time is " + totalTime / 1000000.0 + " seconds");
		System.out.println();
		
		//Outputting results.
		System.out.print("PROCESSES ACTIVE: " + (pnum - PCBs.size()) + "/" + pnum);
		
		//Is our debug settings greater than 0?
		if(Integer.parseInt(dname) > 1) {
			//Outputting all active processes.
			//Checking CPU.
			if(cpu != null)
				System.out.print(" (PID: " + cpu.getPID() + " [CPU])");
			
			//Checking Ready Queue.
			for(int i = 0; i < readyQ.size(); ++i) { 
				//Setting head to the back of Ready Queue.
				readyQ.add(readyQ.peek());
				
				//Printing and removing head from Ready Queue.
				System.out.print(" (PID: " + readyQ.poll().getPID() + " [READY])");
			}
				
			//Checking IO Queue.
			for(int i = 0; i < IOQ.size(); ++i) {
				//Setting head to the back of IO Queue.
				IOQ.add(IOQ.peek());
				
				//Printing and removing head from IO Queue.
				System.out.print(" (PID: " + IOQ.poll().getPID() + " [I/O])");
			}
			
			//Checking Completed Queue.
			for(int i = 0; i < completeQ.size(); ++i) {
				//Setting head to the back of Completed Queue.
				completeQ.add(completeQ.peek());
				
				//Printing and removing head from IO Queue.
				System.out.print(" (PID: " + completeQ.poll().getPID() + " [COMPLETED])");
			}
		}
		System.out.println();
		System.out.print("PROCESSES INACTIVE: " + PCBs.size() + "/" + pnum);
		
		//Is our debug settings greater than 0?
		if(Integer.parseInt(dname) > 1) {
			//Outputting all inactive processes.
			for(int i = 0; i < PCBs.size(); ++i) { 
				//Printing from PCB ArrayList.
				System.out.print(" (PID: " + PCBs.get(i).getPID() + ")");
			}	
		}
		System.out.println();
		System.out.print("PROCESSES COMPLETED: " + completeQ.size() + "/" + (pnum - PCBs.size()));
		
		//Is our debug settings greater than 0?
		if(Integer.parseInt(dname) > 1) {
			//Outputting all completed processes.
			for(int i = 0; i < completeQ.size(); ++i) {
				//Setting head to the back of Completed Queue.
				completeQ.add(completeQ.peek());
				
				//Printing and removing head from IO Queue.
				System.out.print(" (PID: " + completeQ.poll().getPID() + ")");
			}
		}
		System.out.println();
		if(cpu != null) {
			System.out.print("PROCESSES IN CPU: 1/1");
			
			//Is our debug settings greater than 0?
			if(Integer.parseInt(dname) > 1)
				System.out.print(" (PID: " + cpu.getPID() + ")");
		}
		
		if(cpu == null)
			System.out.print("PROCESSES IN CPU: 0/1");
		
		System.out.println();
		System.out.print("PROCESSES IN READY QUEUE: " + readyQ.size() + "/" + (pnum - PCBs.size()));
		
		//Is our debug settings greater than 0?
		if(Integer.parseInt(dname) > 1) {
			//Outputting all processes in the Ready Queue.
			for(int i = 0; i < readyQ.size(); ++i) { 
				//Setting head to the back of Ready Queue.
				readyQ.add(readyQ.peek());
				
				//Printing and removing head from Ready Queue.
				System.out.print(" (PID: " + readyQ.poll().getPID() + ")");
			}
		}
		System.out.println();
		System.out.print("PROCESSES IN I/O SERVICE: " + IOQ.size() + "/" + (pnum - PCBs.size()));
		
		//Is our debug settings greater than 0?
		if(Integer.parseInt(dname) > 1) {
			//Outputting all processes in the IO Queue.
			for(int i = 0; i < IOQ.size(); ++i) {
				//Setting head to the back of IO Queue.
				IOQ.add(IOQ.peek());
				
				//Printing and removing head from IO Queue.
				System.out.print(" (PID: " + IOQ.poll().getPID() + ")");
			}
			System.out.println();
			System.out.println();
			
			//Outputting sum and average for our I/O service time and comparing it to the actual averageIOserviceTime.
			System.out.println("Comparing our average I/O service time to the base file average I/O service time...");
			System.out.println("SUM: " + sum);
			average = sum / IOServiceCount;
			System.out.println("AVERAGE: " + average);
			System.out.println("averageIOserviceTime: " + averageIOserviceTime);
		}
		System.out.println();
		System.out.println();
		
		//Creating decimal format.
		DecimalFormat p = new DecimalFormat("##.00");
		
		//Average CPU utilization results.
		System.out.println("CPU UTILIZATION: " + p.format((double)((CPUTime / (totalTime - timeOffset)) * 100)) + "%");
		System.out.println("AVERAGE CPU TIME: " + (CPUTime / cpuctr) / 1000000.0 + " seconds");
		System.out.println("CPU ACTIVE TIME: " + CPUTime / 1000000.0 + " seconds");
		System.out.println("CPU INACTIVE TIME: " + ((totalTime - timeOffset) - CPUTime) / 1000000.0 + " seconds");
		System.out.println();
		
		//Declaring sum variables.
		long IObCPUSum = 0;
		long IObIOSum = 0;
		long IObrSum = 0;
		long IObtaSum = 0;
		long CPUbCPUSum = 0;
		long CPUbIOSum = 0;
		long CPUbrSum = 0;
		long CPUbtaSum = 0;
		long CPUSum = 0;
		long IOSum = 0;
		long rSum = 0;
		long taSum = 0;
		
		//Getting all the averages for each process.
		//Checking PCB
		for(int i = 0; i < PCBs.size(); ++i) {
			//Getting sum of IO bound processes only.
			if(PCBs.get(i).getBound() == 0) {
				IObCPUSum += PCBs.get(i).getTotalCPUTime();
				IObIOSum += PCBs.get(i).getTotalIOTime();
				IObrSum += PCBs.get(i).getTotalReadyTime();
			}
			
			//Getting sum of CPU bound processes only.
			if(PCBs.get(i).getBound() == 1) {
				CPUbCPUSum += PCBs.get(i).getTotalCPUTime();
				CPUbIOSum += PCBs.get(i).getTotalIOTime();
				CPUbrSum += PCBs.get(i).getTotalReadyTime();
			}
			
			//Getting sum of all processes.
			CPUSum += PCBs.get(i).getTotalCPUTime();
			IOSum += PCBs.get(i).getTotalIOTime();
			rSum += PCBs.get(i).getTotalReadyTime();
		}	
		
		//Checking CPU.
		if(cpu != null) {
			//Getting sum of all processes.
			CPUSum += cpu.getTotalCPUTime();
			IOSum += cpu.getTotalIOTime();
			rSum += cpu.getTotalReadyTime();
		}
		
		//Checking Ready Queue.
		for(int i = 0; i < readyQ.size(); ++i) { 
			//Setting head to the back of Ready Queue.
			readyQ.add(readyQ.peek());
			
			//Getting sum of IO bound processes only.
			if(readyQ.peek().getBound() == 0) {
				IObCPUSum += readyQ.peek().getTotalCPUTime();
				IObIOSum += readyQ.peek().getTotalIOTime();
				IObrSum += readyQ.peek().getTotalReadyTime();
			}
			
			//Getting sum of CPU bound processes only.
			if(readyQ.peek().getBound() == 1) {
				CPUbCPUSum += readyQ.peek().getTotalCPUTime();
				CPUbIOSum += readyQ.peek().getTotalIOTime();
				CPUbrSum += readyQ.peek().getTotalReadyTime();
			}
			
			//Getting sum of all processes.
			CPUSum += readyQ.peek().getTotalCPUTime();
			IOSum += readyQ.peek().getTotalIOTime();
			rSum += readyQ.poll().getTotalReadyTime();
		}
			
		//Checking IO Queue.
		for(int i = 0; i < IOQ.size(); ++i) {
			//Setting head to the back of IO Queue.
			IOQ.add(IOQ.peek());
			
			//Getting sum of IO bound processes only.
			if(IOQ.peek().getBound() == 0) {
				IObCPUSum += IOQ.peek().getTotalCPUTime();
				IObIOSum += IOQ.peek().getTotalIOTime();
				IObrSum += IOQ.peek().getTotalReadyTime();
			}
			
			//Getting sum of CPU bound processes only.
			if(IOQ.peek().getBound() == 1) {
				CPUbCPUSum += IOQ.peek().getTotalCPUTime();
				CPUbIOSum += IOQ.peek().getTotalIOTime();
				CPUbrSum += IOQ.peek().getTotalReadyTime();
			}
			
			//Getting sum of all processes.
			CPUSum += IOQ.peek().getTotalCPUTime();
			IOSum += IOQ.peek().getTotalIOTime();
			rSum += IOQ.poll().getTotalReadyTime();
		}
		
		//Checking Completed Queue.
		for(int i = 0; i < completeQ.size(); ++i) {
			//Setting head to the back of Completed Queue.
			completeQ.add(completeQ.peek());
			
			//Getting sum of IO bound processes only.
			if(completeQ.peek().getBound() == 0) {
				IObCPUSum += completeQ.peek().getTotalCPUTime();
				IObIOSum += completeQ.peek().getTotalIOTime();
				IObrSum += completeQ.peek().getTotalReadyTime();
				IObtaSum += completeQ.peek().getTotalTime();
			}
			
			//Getting sum of CPU bound processes only.
			if(completeQ.peek().getBound() == 1) {
				CPUbCPUSum += completeQ.peek().getTotalCPUTime();
				CPUbIOSum += completeQ.peek().getTotalIOTime();
				CPUbrSum += completeQ.peek().getTotalReadyTime();
				CPUbtaSum += completeQ.peek().getTotalTime();
			}
			
			//Getting sum of all processes.
			CPUSum += completeQ.peek().getTotalCPUTime();
			IOSum += completeQ.peek().getTotalIOTime();
			rSum += completeQ.peek().getTotalReadyTime();
			taSum += completeQ.poll().getTotalTime();
		}
		
		//Outputting CPU, Ready, IO, and turnaround averages for IO bound processes.
		System.out.println("AVERAGE CPU TIME PER PROCESS [I/O BOUND]: " + (IObCPUSum / pnum) / 1000000.0 + " seconds");
		System.out.println("AVERAGE READY TIME PER PROCESS [I/O BOUND]: " + (IObrSum / pnum) / 1000000.0 + " seconds");
		System.out.println("AVERAGE I/O SERVICE TIME PER PROCESS [I/O BOUND]: " + (IObIOSum / pnum) / 1000000.0 + " seconds");
		System.out.println("AVERAGE TURNAROUND TIME PER PROCESS [I/O BOUND]: " + (IObtaSum / pnum) / 1000000.0 + " seconds");
		System.out.println();
		
		//Outputting CPU, Ready, IO, and turnaround averages for CPU bound processes.
		System.out.println("AVERAGE CPU TIME PER PROCESS [CPU BOUND]: " + (CPUbCPUSum / pnum) / 1000000.0 + " seconds");
		System.out.println("AVERAGE READY TIME PER PROCESS [CPU BOUND]: " + (CPUbrSum / pnum) / 1000000.0 + " seconds");
		System.out.println("AVERAGE I/O SERVICE TIME PER PROCESS [CPU BOUND]: " + (CPUbIOSum / pnum) / 1000000.0 + " seconds");
		System.out.println("AVERAGE TURNAROUND TIME PER PROCESS [CPU BOUND]: " + (CPUbtaSum / pnum) / 1000000.0 + " seconds");
		System.out.println();
		
		//Outputting CPU, Ready, IO, and turnaround averages for all processes.
		System.out.println("AVERAGE CPU TIME PER PROCESS [ALL]: " + (CPUSum / pnum) / 1000000.0 + " seconds");
		System.out.println("AVERAGE READY TIME PER PROCESS [ALL]: " + (rSum / pnum) / 1000000.0 + " seconds");
		System.out.println("AVERAGE I/O SERVICE TIME PER PROCESS [ALL]: " + (IOSum / pnum) / 1000000.0 + " seconds");
		System.out.println("AVERAGE TURNAROUND TIME PER PROCESS [ALL]: " + (taSum / pnum) / 1000000.0 + " seconds");
		System.out.println();
	}
}