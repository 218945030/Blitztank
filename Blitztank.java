package stfx2020;
import robocode.*;
import java.awt.*;
import java.util.*;
/**
 * Blitztank robot for Robocode CPT 
 * @author Jonah Ottini
 * @course ICS4UC
 * @date 2020/11/10
 */
public class Blitztank extends AdvancedRobot {

	//Variables
	int numScanned = 0;
	double[] energy = null;
	double[] energyUnsorted = null;
	String[] names = null;
	boolean target = false;
	boolean trackTarget = false;
	int numRobots = 0;
	String trackName = null;
	double trackDistance = 0;
	int shot = 0;

	/**
	 * run: BlitzTank's default behavior
	 */
	public void run() {

		// Set colors
		this.setBodyColor(Color.black);
		this.setGunColor(Color.black);
		this.setRadarColor(Color.white);

		//Set array lengths to amount of other robots
		this.numRobots = getOthers()+1;
		this.names = new String [this.numRobots];
		this.energy = new double[this.numRobots];
		this.energyUnsorted = new double[this.numRobots];

		while (true){

			//Set shot strength based on energy level
			if (this.getEnergy()>=50) { 
				this.shot = 3;
			}
			else { 
				this.shot = 1;
			}

			//Resets amount of robots each run
			this.numRobots = getOthers()+1;

			//When all robots have been scanned
			if(this.numScanned>=this.numRobots-1) {

				//Calls subprogram to sort energy levels
				sortEnergy();

				//Compares collected data and gets name of lowest energy robot
				for (int i=0; i<=this.numRobots-1; i++) { 
					if(this.energyUnsorted[i]<=this.energy[0]) { 
						this.trackName = this.names[i];
						this.target = true;
					}
				}
			}

			//When a target has been found
			if(this.target == true) {

				//Reset number of scanned robots
				this.numScanned = 0;

				//Move in a circle and Scan battlefield
				this.setTurnRight(360);
				this.setAhead(10);
				this.scan();
			}

			//If Blitztank loses the target
			else if (this.trackTarget == true) { 

				//Reset all data
				this.energy = new double[this.numRobots];
				this.energyUnsorted = new double[this.numRobots];
				this.names = new String [this.numRobots];
				this.trackTarget=false;
				this.trackName= null;
				System.out.println("Target Lost");
			}

			//When Target has not been found
			else { 

				//Move in a circle and Scan battlefield 
				this.setTurnRight(360);
				this.setAhead(10);
				this.scan(); 
			}
			this.waitFor(new MoveCompleteCondition(this));
		}
	}

	/**
	 * onScannedRobot: Get Energy and Name
	 * if target is found get target name 
	 * if target is ready to be tracked, track and fire 
	 */
	public void onScannedRobot(ScannedRobotEvent e) {

		//If a target is found
		if(this.target == true) { 
			System.out.println("Checking Target");

			//If a lower energy is found, track it
			if(e.getEnergy()<=this.energy[0]) { 
				this.trackName = e.getName();
			}
			else if (this.numRobots==2) { 
				this.trackName = e.getName();
			}

			//Look for target name 
			if(e.getName().equals(this.trackName)) {
				System.out.println("Target Located");
				this.setTurnRight(e.getBearing());
				this.trackTarget = true;
				this.target=false;
			}

			else { 
				System.out.println("Not Target");
			}
		}

		//Track and fire at target
		else if(this.trackTarget== true) { 

			//If a strong opponent get between Blitztank and target
			if (e.getEnergy()>=this.energy[this.numRobots/2]) { 
				this.setAhead(-500); 

				//Reset all data
				this.energy = new double[this.numRobots];
				this.energyUnsorted = new double[this.numRobots];
				this.names = new String [this.numRobots];
				this.trackTarget=false;
				this.trackName= null;
				System.out.println("Strong robot in the way");
			}

			//Check distance from target and approach accordingly
			if(e.getDistance()<=90) {
				this.trackDistance = -10;
			}
			else if(e.getDistance()<=150) { 
				this.trackDistance = 0;
			}
			else if(e.getDistance()>=250) {  
				this.trackDistance = 100;
			}
			else { 
				this.trackDistance = 10;
			}

			this.turnRight(e.getBearing());
			this.setAhead(this.trackDistance);
			fire(this.shot);
			this.scan();

		}

		//If no target is found add there data to arrays 
		else {
			System.out.println("Scanning");
			this.numScanned++;
			getData(e);

		}
	}

	/**
	 * checkName: Gets name of robot scanned and adds it to array
	 * also adds energy levels to corresponding arrays
	 */
	public void getData(ScannedRobotEvent e) { 

		//Variables
		boolean nameCheck = false;	
		String name = e.getName();

		//Loops number of scanned robots
		for(int i = 0; i<this.numScanned; i++) {

			//Check if name is in array
			if(name.equals(this.names[i])) {
				this.numScanned--;
				System.out.println("Name already added");
				nameCheck = true;

				//Adds energy to arrays
				this.energy[i] = e.getEnergy();
				this.energyUnsorted[i] = e.getEnergy();
			}	
		}

		//Adds name if it isn't already added
		if(nameCheck == false) { 
			this.names[numScanned-1] = name;
			System.out.println("Add new Name");

			//Adds energy to arrays
			this.energy[this.numScanned] = e.getEnergy();
			this.energyUnsorted[this.numScanned] = e.getEnergy();
		}
	}

	/** 
	 * Sort energy levels from lowest to highest
	 */
	public void sortEnergy() { 
		System.out.println("Sorting");

		//Keeps track of how many times the lowest was found
		int numLowest = 0;

		//Loops until sorted
		while (numLowest<this.numScanned){ 

			//Variables for lowest and previous lowest
			double energyLowest = this.energy[numLowest];
			double energyLastLowest = this.energy[numLowest];

			// Loops for all energy levels scanned
			for (int i=0; i<this.numScanned-numLowest; i++){
				double energyA = this.energy[i+numLowest];

				// Compares current lowest to next energy level
				if(energyLowest - energyA< 0) { 

					// If it is lower set it as lowest and replace with previous lowest
					energyLastLowest = energyLowest;
					energyLowest = energyA;
					this.energy[i+numLowest] = energyLastLowest;
				}
			}

			// Set lowest energy to top of the array
			this.energy[numLowest] = energyLowest;
			numLowest++;
		}
		System.out.println("Sorted");
	}

	/**
	 * onHitWallRobot: Check bearing and respond accordingly
	 */
	public void onHitWall(HitWallEvent e) {
		System.out.println("Hit wall");

		//Variable for bearing
		double bearing = e.getBearing();

		//If collision in front move turn 
		if(bearing<90 && bearing>-90) { 
			setTurnRight(-bearing);
		}

		//Always moves forward
		setAhead(100);
	}

	/**
	 * onHitRobot: Check bearing and respond accordingly
	 */
	public void onHitRobot(HitRobotEvent e) {
		System.out.println("Hit robot");

		//Variable for bearing
		double bearing = e.getBearing();

		//If collision in front move backwards
		if(bearing<90 && bearing>-90) { 
			setAhead(-500);
		}

		//If collision behind move forwards
		else { 
			setAhead(500);
		}
	}
}