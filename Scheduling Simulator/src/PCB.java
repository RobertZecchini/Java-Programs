/*
Program Name: Process Control Block
Programmer Name: Robert S. Zecchini
Version: 1.0
Purpose: This class creates a Process Control Block Data Structure that assigns a
		process ID that's an integer. This data structure also includes the process
		lifetime and how long it has been within the CPU. This data structure also
		keeps track of how long, and how often a process was in the IO service
		queue, ready queue, or CPU.
			
*/

public class PCB {
	//Declaring Variables.
	int pid;
	int pstat;
	int pio;
	int bound;
	long dif;
	long time;
	long total;
	long durat;
	long totdurat;
	long strtime;
	long stattime;
	long etime;
	long ptime;
	long cputime;
	long piotime;
	long iotime;
	long btime;
	long rtime;
	long iosrv;
	long sIO;
	
	//Creating constructors.
	public PCB(int pid, long dur) {
		//Assigning variables.
		this.pid = pid;
		this.durat = dur;
		this.totdurat = dur;
		this.bound = 0;
		this.dif = 0;
		this.time = 0;
		this.total = 0;
		this.strtime = 0;
		this.stattime = 0;
		this.etime = 0;
		this.pstat = -1;
		this.pio = 0;
		this.ptime = 0;
		this.cputime = 0;
		this.piotime = 0;
		this.iotime = 0;
		this.btime = 0;
		this.rtime = 0;
		this.iosrv = 0;
		this.sIO = 0;
	}
	
	//This function updates the process CPU time.
	public void setCPUtime(long time) { this.time = time; }
	
	//This function sets the total CPU time of a process.
	public void setTotalCPUTime(long time) { this.cputime += time; }
	
	//This function sets the start time for a process since it was created.
	public void setStartTime(long time) { this.strtime = time; }
	
	//THis function sets the start time of a process since it changed states.
	public void setStateTime(long time) { this.stattime = time; }
	
	//This function sets the burst time of a process.
	public void setBurstTime(long time) { this.btime = time; }
	
	//This function sets the overall time a process took during IO services.
	public void setTotalIOTime(long time) { this.iotime += time; }
	
	//This function sets the time for which a process had started an IO service.
	public void startIO(long time) { this.sIO = time; }
	
	//This function sets the overall time a process took within the ready queue.
	public void setReadyTime(long time) { this.rtime = time; }
	
	//This function sets the overall time a process was active.
	public void setTotalTime(long time) { this.total = time; }
	
	//This function determines what a process is bounded to. This function also sets the IOservice time of a process.
	public void setBound(int bound) { this.bound = bound; }
	
	//This function sets how long a process takes to perform an IO service assuming that the process is IO bound.
	public void setIOServiceTime(long iosrv) { this.iosrv = iosrv; }
	
	//This sets the CPU duration of a process.
	public void setDuration(long time) { this.durat = time; }
	
	//This function gets the ID of a process.
	public int getPID() { return this.pid; }
	
	//This function gets the start time for a process since creation.
	public long getStartTime() { return this.strtime; }
	
	//This function gets the start time for a process since state change.
	public long getStateTime() { return this.stattime; }
	
	//This function gets the burst time of a process.
	public long getBurstTime() { return this.btime; }
	
	//This function gets the current time a process has been in the CPU.
	public long getTime() { return this.time; }

	//This gets the current CPU duration time of a process.
	public long getDuration() { return this.durat; }
	
	//This function sets the remaining duration of either the CPU time or the IO service time.
	public long getDifference() { return this.dif; }
	
	//This function gets the total CPU time of a process.
	public long getTotalCPUTime() { return this.cputime; }
	
	//This function gets the time for which a process had started an IO service.
	public long getIOstartTime() { return this.sIO; }
	
	//This function gets the overall time a process spent in IO services.
	public long getTotalIOTime() { return this.iotime; }
	
	//This function gets the overall time a process spent in the ready queue.
	public long getTotalReadyTime() { return this.rtime; }
	
	//This function gets the overall life cycle of a process.
	public long getTotalTime() { return this.total; }
	
	//This gets the overall CPU duration of a process.
	public long getTotalDuration() { return this.totdurat; }
	
	//This function gets the bound of a process.
	public int getBound() { return this.bound; }
	
 	//This function will get the current CPU processing time since the process has move onto the CPU.
	public long getCPUTime() {
		//Do we still have any CPU time remaining?
		if(this.durat > 0)
			this.etime = System.nanoTime();
		this.ptime = (this.etime - this.stattime) / 1000;
		return this.ptime;
	}
	
	//This function will get the current IO service time since the process has moved to IO services.
	public long getIOTime() {
		this.etime = System.nanoTime();
		this.piotime = (this.etime - this.sIO) / 1000;
		return this.piotime;
	}
	
	//This function will check the remaining service time of a process.
	public long getServiceTime() {
		this.piotime = getIOTime();
		this.dif = this.iosrv - this.piotime;
		return this.dif;
	}
	
	//This function will check the remaining life cycle of a process.
	public long getLifeCycle() {
		this.ptime = getCPUTime();
		this.setCPUtime(ptime);
		this.dif = this.durat - this.ptime;
		return this.dif;
	}
	

	//This function updates a process state
	public void updateState(char pstatc) {
		//Update the state to "ready."
		if(pstatc == 'R')
			this.pstat = 0;
		
		//Update the state to "running."
		if(pstatc == '!')
			this.pstat = 1;
		
		//Update the state to "waiting."
		if(pstatc == 'W')
			this.pstat = 2;
		
		//Update the state to "terminating."
		if(pstatc == 'T')
			this.pstat = 3;
	}
	
	//This functions updates the number IO calls a process has.
	public void updateIOCalls(int numIO) {
		this.pio += numIO;
	}
	
	//Overriding toString() function.
	@Override
	public String toString() {
		//Declaring Variables.
		String pstats = "";
		String pbound = "";
		
		//Converting pstat into string.
		if(this.pstat == -1)
			pstats = "New";
		if(this.pstat == 0)
			pstats = "Ready";
		if(this.pstat == 1)
			pstats = "Running";
		if(this.pstat == 2)
			pstats = "Waiting";
		if(this.pstat == 3)
			pstats = "Terminated";
		
		//Setting bound name as a string.
		if(this.bound == 0)
			pbound = "I/O";
		if(this.bound == 1)
			pbound = "CPU";
		
		return "Process " + this.pid + "\n  State: " + pstats + "\n  Bound: " + pbound + "\n  I/O CALLS: " + this.pio 
				+ "\n  BURST TIME: " + this.btime + "\n  CPU TIME: " + this.cputime + "\n  REAMINING CPU TIME: " + this.durat 
				+ "\n  READY TIME: " + this.rtime + "\n  I/O WAITING TIME: " + this.iotime + "\n  TOTAL TIME: " + this.total + "\n";
	}
	
	//This function prints out the PCB data before it terminates.
	public void terminateProcess() {
		this.setDuration(this.dif);
		this.updateState('T');
	}
}
