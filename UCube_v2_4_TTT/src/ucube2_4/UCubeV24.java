package ucube2_4;

import java.io.IOException;
import java.util.ArrayList;

import newhull.Point3d;
import processing.core.PApplet;
import processing.core.PFont;
import processing.serial.Serial;
import toxi.geom.Vec3D;
import toxi.processing.ToxiclibsSupport;
import controlP5.CheckBox;
import controlP5.ControlP5;

//import java.awt.event.*;

public class UCubeV24 extends PApplet {

	/* A TicTacToe implementation on the UCube v2_4 */
	
	// Note: For every new project in eclipse using the serial library, you'll
	// have to add the -d32 argument under vm run settings
	// This version implements dynamic knot and mst builders, wireframe

	String version = "UCube v.024 Tic Tac Toe";
	ControlP5 controlP5;
	CheckBox checkBox;

	Nav3D nav = new Nav3D(this); // camera controller
	HullBuilder hb = new HullBuilder(this);

	ToxiclibsSupport gfx;

	Serial myPort; // the serial port
	boolean firstContact = false; // Whether we've heard from the
									// microcontroller

	int gridSize = 7; // size of grid (assumes all dimensions are equal)
	int spacing = 40; // distance between points
	int counter = 0; // wireframe toggle
	String inString; // string of coordinates from arduino
	String oldString;
	String message; // for UI hints

	PFont myFont; // init font for text

	Vec3D[] masterVectArray = new Vec3D[0];
	Point3d[] masterPointArray = new Point3d[0];

	int grey = color(100);
	int red = color(200, 0, 0);
	int green = color(0, 200, 0);
	int activeColor = red;

	// Player 1
	ArrayList<Vec3D> redVectors = new ArrayList<Vec3D>();
	// Player 2
	ArrayList<Vec3D> blueVectors = new ArrayList<Vec3D>();

	// whose turn is it?
	int turn = 0;
	// are we in game mode?
	boolean playing = false;
	Board theGame;
	boolean addedRed = false;
	boolean addedBlue = false;
	String gameStatus;

	@Override
	public void setup() {
		// List all the available serial ports
		// println(Serial.list());

		size(1400, 800, OPENGL);
		// size(1400, 800);
		frameRate(10);
		gfx = new ToxiclibsSupport(this); // initialize ToxiclibsSupport
		background(255);
		initControllers(); // init interface, see GUI tab
		// nav.mouseOverVectors = new Vec2D[0];

		myFont = createFont("FFScala", 32);
		textFont(myFont);
		initCoordArray();

		// If you've upgraded from snow leopard to mountain lion, the ports
		// changed at some point
		myPort = new Serial(this, Serial.list()[4], 115200);
		myPort.clear();

		theGame = new Board();

	}

	@Override
	public void draw() {

		background(255);
		smooth();
		// because we want controlP5 to be drawn on top of everything
		// else we need to disable OpenGL's depth testing at the end
		// of draw(). that means we need to turn it on again here.
		hint(ENABLE_DEPTH_TEST);
		pushMatrix();
		lights();
		nav.transform(); // do transformations using Nav3D controller
		drawGrid();

		while (myPort.available() > 0) {

			String inString = myPort.readStringUntil('\n');

			if (inString != null) {

				inString = trim(inString);
				// println(inString);

				if (firstContact == false) {
					// if we get a hello, clear the port, set firstcontact to
					// false, send back an 'A', and print 'contact'
					if (inString.equals("hello")) {
						myPort.clear();
						firstContact = true;
						myPort.write('A');
						println("contact");
					}
				} else {
					// make active points more visible

					strokeWeight(8);

					// compare inString to oldString to see if coords changed
					if (inString.equals(oldString)) {
						// do nothing
						// println(inString);
					} else {
						// if we're not in edit mode, update the input
						// coordinates and redraw the hull
						// if (readSerial == true) {
						oldString = inString;
						// println(inString);

						// }

						int counter = 0;
						// vectors = new Vec3D[0];
						// vectors.clear();

						for (int i = 0; i < inString.length(); i++) {
							// println("1: " + inString.length());
							char bit = inString.charAt(i);

							// throw away leading bit from each shift register
							// (we're only using 7 of the 8 bits)
							if (i == 0 || i % 8 == 0) {
								inString.replace(bit, ' ');
							} else {
								// if the bit == 1, an led is plugged into that
								// space, so look up it's coordinate
								if (bit == '1') {

									if (activeColor == red
											&& !redVectors
													.contains(masterVectArray[counter])
											&& !blueVectors
													.contains(masterVectArray[counter])) {

										redVectors
												.add(masterVectArray[counter]);
										println("added red");
										
										addedRed = true;

									}

									if (activeColor == green
											&& !blueVectors
													.contains(masterVectArray[counter])
											&& !redVectors
													.contains(masterVectArray[counter])) {

										blueVectors
												.add(masterVectArray[counter]);
										println("added green");
										addedBlue = true;

									}

								}

								if (bit == '0') {

									// redVectors.remove(masterVectArray[counter]);
									// blueVectors
									// .remove(masterVectArray[counter]);

								}

								counter++;
							}
						}
					}

					if (redVectors.size() > 0) {
						stroke(red);

						for (int j = 0; j < redVectors.size(); j++) {
							// println(vectors[j]);
							float x = redVectors.get(j).x;
							float y = redVectors.get(j).y;
							float z = redVectors.get(j).z;
							point(x, y, z);
						}
					}

					if (blueVectors.size() > 0) {
						stroke(green);

						for (int j = 0; j < blueVectors.size(); j++) {
							// println(vectors[j]);
							float x = blueVectors.get(j).x;
							float y = blueVectors.get(j).y;
							float z = blueVectors.get(j).z;
							point(x, y, z);
						}
					}
				}
			}
		}

		if (playing == true) {

			try {
				ticTacToe();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		drawAxes();
		popMatrix();

		// turn off depth test so the controlP5 GUI draws correctly
		hint(DISABLE_DEPTH_TEST);

		// do text cues after popMatrix so it doesn't rotate
		doMouseOvers(); // text cues on button rollover

		// ask for another reading
		myPort.write("A"); // check if this is the right place for this line
	}// end draw

	public void initCoordArray() {

		for (int z = 9; z > 2; z--) {

			for (int x = 9; x > 2; x--) {

				for (int y = 9; y > 2; y--) {

					Vec3D tempVect = new Vec3D((x-3) * spacing, (y-3) * -spacing, (z-3)
							* spacing); // maybe should be i,k,j so moves in Y
					Point3d tempPoint = new Point3d((x-3) * spacing, (y-3) * -spacing,
							(z-3) * spacing);
					masterVectArray = (Vec3D[]) append(masterVectArray,
							tempVect);
					masterPointArray = (Point3d[]) append(masterPointArray,
							tempPoint);
					//println("x: " + x + "  y: " + y + "  z: " + z);
					// println("points: " + masterPointArray.length);
				}
			}
		}
	}

	// --------------------------GUI----------------------------------//

	// Initialize buttons and NAV3D class
	public void initControllers() {
		// nav = new Nav3D();

		controlP5 = new ControlP5(this);
		controlP5.setColorBackground(50);

		controlP5.addButton("Play", 0, 100, 100, 80, 19);
		controlP5.addButton("Reset", 0, 100, 120, 80, 19);

//		checkBox = controlP5.addCheckBox("checkBox", 200, 100);
//		checkBox.setSize(19, 19);
//		checkBox.setItemsPerRow(1);
//		checkBox.setSpacingRow(1);
//		checkBox.addItem("red", 1);
//		checkBox.addItem("green", 2);
//		checkBox.deactivateAll();

	}

	// help text when mouse is over buttons
	public void doMouseOvers() {

		textSize(18);
		fill(50);
		text(version, 100, 90, 0);

		if (controlP5.controller("Play").isInside()) {
			message = "Starts a new game of Tic Tac Toe.";
		}
		
		if (controlP5.controller("Reset").isInside()) {
			message = "Resets the board before a new game of Tic Tac Toe.";
		}

		if (message != null && controlP5.window(this).isMouseOver()) {
			//textSize(14);
			text(message, 100, 600, 0);
		}

		if (gameStatus != null) {

			text(gameStatus, 100, 300, 0);
		}

	}

	public void mouseDragged() {
		nav.mouseDragged();
	}

	@Override
	public void mouseReleased() {
		nav.mouseReleased();
	}

	@Override
	public void keyPressed() {
		nav.keyPressed();
	}

	// draw the little grey grid
	public void drawGrid() {

		// draw rest of grid
		// (spacing * (gridSize -1) * -1) /2 = center around 0
		int xpos = 0;
		int ypos = 0;
		int zpos = 0;

		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				for (int k = 0; k < gridSize; k++) {
					stroke(100);
					strokeWeight(2);
					point(xpos, ypos, zpos);
					xpos += spacing;
				}
				xpos = 0;
				ypos -= spacing;
			}
			xpos = 0;
			ypos = 0;
			zpos += spacing;
		}
	}

	// draw the axes
	public void drawAxes() {

		strokeWeight(1);
		textSize(32);
		stroke(150, 0, 150);
		line(0, 0, 0, 100, 0, 0);
		fill(150, 0, 150);
		text("X", 220, 0);
		stroke(0, 150, 0);
		line(0, 0, 0, 0, -100, 0);
		fill(0, 150, 0);
		text("Y", 0, -220);
		stroke(0, 0, 150);
		line(0, 0, 0, 0, 0, 100);
		fill(0, 0, 150);
		text("Z", 0, 0, 220);
		fill(0, 0, 0);
	}

	public void Play(int theValue) throws IOException {

		if (playing == true) {
			// doHull = true;
			playing = false;
			//reset();
			
		} else if (playing == false) {
			// doHull = true;
			playing = true;
			gameStatus = "Red's Turn";
			ticTacToe();
		}
	}
	
	public void Reset(int theValue) {
		//reset();
		redVectors.clear();
		blueVectors.clear();
		theGame.reset();
		activeColor = red;
		gameStatus = "New Game! Red's Turn";
		turn = 0;
	}
	
//	public void reset() {
//		redVectors.clear();
//		blueVectors.clear();
//		theGame.reset();
//		activeColor = red;
//		gameStatus = "New Game! Red's Turn";
//		turn = 0;
//	}
	

	public void ticTacToe() throws IOException {

		// System.out.println("play");
		int player = turn % 2 + 1;
		if (player == 1) {
			activeColor = red;
		} else {
			activeColor = green;
		}
		
		

		if (addedRed == true) {

			int j = redVectors.size() - 1;

			int x = (int) redVectors.get(j).x / spacing;
			int y = (int) redVectors.get(j).y / spacing * -1;
			int z = (int) redVectors.get(j).z / spacing;
			Position p = new Position(x+3, y+3, z+3);
			theGame.set(p, player);
			System.out.println(p);			
			
			addedRed = false;
			turn++;
			gameStatus = "Green's Turn";
			
			if (theGame.threeInARow(p, player)) {
				gameStatus = "After "+turn+" steps, red player wins!" + "\n" + "Clear game pieces and press reset to start another game!"; 
				//TODO: Draw line through winning set
			}
		}

		if (addedBlue == true) {

			int j = blueVectors.size() - 1;

			int x = (int) blueVectors.get(j).x / spacing;
			int y = (int) blueVectors.get(j).y / spacing * -1;
			int z = (int) blueVectors.get(j).z / spacing;
			Position p = new Position(x+3, y+3, z+3);
			theGame.set(p, player);
			System.out.println(p);

			addedBlue = false;
			turn++;
			gameStatus = "Red's Turn";
			
			if (theGame.threeInARow(p, player)) {
				gameStatus = "After "+turn+" steps, green player wins!" + "\n" + "Clear game pieces and press reset to start another game!"; 
			}
		}

	}

} // end class
