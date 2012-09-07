package ucubev23;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import processing.opengl.*;
import newhull.*;
//import java.awt.event.*;
import toxi.geom.*;
import toxi.geom.mesh.*;
import toxi.processing.*;
import controlP5.*;
import processing.serial.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class UCubeV23 extends PApplet {

	// Note: For every new project in eclipse using the serial library, you'll
	// have to add the -d32 argument under vm run settings

	String version = "UCube v.023";
	ControlP5 controlP5;
	CheckBox checkBox;

	// GUI gui = new GUI(this);
	Nav3D nav = new Nav3D(this); // camera controller
	HullBuilder hb = new HullBuilder(this);

	// regular hull (all points)
	QuickHull3D hull = new QuickHull3D(); // init quickhull for hull
	Point3d[] points; // init Point3d array
	Point3d[] savedPoints;
	Mesh3D mesh = new TriangleMesh(); // triangle mesh for convex hull
	// Vec3D[] vectors;
	ArrayList<Vec3D> vectors = new ArrayList<Vec3D>();

	// red hull
	QuickHull3D redHull = new QuickHull3D();
	Point3d[] redPoints;
	Point3d[] savedRedPoints;
	Mesh3D redMesh = new TriangleMesh();
	ArrayList<Vec3D> redSTLVectors = new ArrayList<Vec3D>();
	boolean doRedHull = false;
	ArrayList<Vec3D> redVectors = new ArrayList<Vec3D>();

	// blue / green hull
	QuickHull3D blueHull = new QuickHull3D();
	Point3d[] bluePoints;
	Point3d[] savedBluePoints;
	Mesh3D blueMesh = new TriangleMesh();
	ArrayList<Vec3D> blueSTLVectors = new ArrayList<Vec3D>();
	boolean doBlueHull = false;
	ArrayList<Vec3D> blueVectors = new ArrayList<Vec3D>();

	ToxiclibsSupport gfx;

	// knots
	QuickHull3D knotHull = new QuickHull3D(); // init quickhull for knot
	Mesh3D knotMesh = new TriangleMesh(); // trianglemesh for knot
	ArrayList<Vec3D> knotVectors = new ArrayList<Vec3D>();
	Vec3D[] kVectors = new Vec3D[0]; // collection of all knot vectors
	Point3d[] knotPoints = new Point3d[0]; // knot points (cubes around each
											// point)
	Point3d[] kSavedPoints = new Point3d[0];// saved points for knot

	int offset = 10; // how thick your knot is

	// minimal spanning tree
	QuickHull3D mstHull = new QuickHull3D(); // init quickhull for knot
	ArrayList<Vec3D> mstVectors = new ArrayList<Vec3D>();
	Mesh3D mstMesh = new TriangleMesh();
	Point3d[] mstPoints = new Point3d[0];
	Vec3D[] mVectors = new Vec3D[0];

	Vec3D[] sVectors; // spline vectors

	// Vec2D[] mouseOverVectors; // for keeping track of mouseover
	// int vertexMouseOver = -1;
	// boolean mouseOver = false;
	// float x, y; // for hit detection screenX and screenY positions
	// float rotX, rotY; // for manual rotation

	Serial myPort; // the serial port
	boolean firstContact = false; // Whether we've heard from the
									// microcontroller

	int gridSize = 7; // size of grid (assumes all dimensions are equal)
	int spacing = 40; // distance between points
	int counter = 0; // wireframe toggle
	String inString; // string of coordinates from arduino
	String oldString;
	String message; // for UI hints
	boolean reDraw = true;
	boolean reDrawKnot = true;
	boolean doFill = true;
	boolean doGrid = true;
	boolean doSpline = false;

	boolean doHull = false;
	boolean doKnot = false;
	boolean doMst = false;
	PFont myFont; // init font for text
	PrintWriter output; // for saving shape
	BufferedReader reader; // for loading shapes

	boolean readSerial = true;
	// String inString;

	Vec3D[] masterVectArray = new Vec3D[0];
	Point3d[] masterPointArray = new Point3d[0];

	int grey = color(200);
	int red = color(200, 0, 0);
	int green = color(0, 200, 0);
	int activeColor = grey;

	// spanning tree
	int maximumVertices;
	Graph g;
	List<Edge> mst;

	public void setup() {
		// List all the available serial ports
		// println(Serial.list());

		size(1400, 800, OPENGL);
		frameRate(12);
		gfx = new ToxiclibsSupport(this); // initialize ToxiclibsSupport
		background(255);
		initControllers(); // init interface, see GUI tab
		nav.mouseOverVectors = new Vec2D[0];

		myFont = createFont("FFScala", 32);
		textFont(myFont);
		initCoordArray();

		// If you've upgraded from snow leopard to mountain lion, the ports
		// changed at some point
		myPort = new Serial(this, Serial.list()[4], 115200);
		myPort.clear();

	}

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
		// drawGrid();

		if (doGrid == true) {
			drawGrid(); // draw grid
		}

		if (doSpline == true) {
			drawSpline(); // do splines
		}

		drawAxes(); // draw axes

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
					//stroke(200);

					// inString = inString +
					// "1000000010001000100010001000010010001000100110001001000010100000100001001000100010000000100010001000100010000100100010001001100010010000101000001000010010001000";

					// compare inString to oldString to see if coords changed
					if (inString.equals(oldString)) {
						// do nothing
					} else {
						// if we're not in edit mode, update the input
						// coordinates and redraw the hull
						if (readSerial == true) {
							oldString = inString;
							// println(inString);
							reDraw = true;
						}

						// I dont think this works right
						// if we are in edit mode, freeze the set of coordinates
						// so we can edit
						if (readSerial == false) {
							inString = oldString;
						}

						int counter = 0;
						// vectors = new Vec3D[0];
						vectors.clear();
						points = new Point3d[0];

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
									
									//TODO: Stop active color from taking over grey points
//									if(activeColor == grey) {
//										vectors.add(masterVectArray[counter]);
//										points = (Point3d[]) append(points, masterPointArray[counter]);
//									}

									if (activeColor == red
											&& !redVectors
													.contains(masterVectArray[counter])
											&& !blueVectors
													.contains(masterVectArray[counter])
											&& !vectors
													.contains(masterVectArray[counter])) {

										redVectors
												.add(masterVectArray[counter]);
										println("added red");
										initRedPoints();

									}

									if (activeColor == green
											&& !blueVectors
													.contains(masterVectArray[counter])
											&& !redVectors
													.contains(masterVectArray[counter])
											&& !vectors
													.contains(masterVectArray[counter])) {

										blueVectors
												.add(masterVectArray[counter]);
										println("added green");
										initBluePoints();
									}

									println(i + " " + bit + " " + counter + " "
											+ masterPointArray[counter]);
									//vectors = (Vec3D[]) append(vectors,masterVectArray[counter]);
									vectors.add(masterVectArray[counter]);
									points = (Point3d[]) append(points, masterPointArray[counter]);

									if (knotVectors
											.contains(masterVectArray[counter])) {

									} else {
										knotVectors
												.add(masterVectArray[counter]);
										println(masterVectArray[counter]
												+ " true");
										reDrawKnot = true;
									}

									if (mstVectors
											.contains(masterVectArray[counter])) {

									} else {
										mstVectors
												.add(masterVectArray[counter]);
									}

								}

								if (bit == '0') {

									redVectors.remove(masterVectArray[counter]);
									initRedPoints();
									// println("removed red");

									blueVectors
											.remove(masterVectArray[counter]);
									initBluePoints();
									// println("removed green");

									if (knotVectors
											.contains(masterVectArray[counter])) {
										knotVectors
												.remove(masterVectArray[counter]);
									}

									if (mstVectors
											.contains(masterVectArray[counter])) {
										mstVectors
												.remove(masterVectArray[counter]);
									}
								}

								counter++;
							}
						}
					}
					// if (vectors.length > 0) {
					// stroke(grey);
					// // put points on the canvas
					// for (int j = 0; j < vectors.length; j++) {
					// // println(vectors[j]);
					// float x = vectors[j].x;
					// float y = vectors[j].y;
					// float z = vectors[j].z;
					// point(x, y, z);
					// }
					// }
					if (vectors.size() > 0) {
						stroke(grey);
						for (int j = 0; j < vectors.size(); j++) {
							// println(vectors[j]);
							float x = vectors.get(j).x;
							float y = vectors.get(j).y;
							float z = vectors.get(j).z;
							point(x, y, z);
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

					// call drawHull function if Hull mode is active
					if (doHull == true) {

						drawHull(vectors, points, grey);

					}

					if (doRedHull == true) {

						drawHull(redVectors, redPoints, red);

					}

					if (doBlueHull == true) {

						drawHull(blueVectors, bluePoints, green);

					}

					// draw knot if doKnot boolean == true
					if (doKnot == true) {
						// drawKnot();
						strokeWeight(1);
						fill(200);
						beginShape(TRIANGLES);

						for (int i1 = 0; i1 < kVectors.length; i1 += 3) {

							vertex(kVectors[i1]);
							vertex(kVectors[i1 + 1]);
							vertex(kVectors[i1 + 2]);
						}
						endShape();
					}

					// draw MST
					if (doMst == true) {
						// drawKnot();
						strokeWeight(1);
						fill(200);
						beginShape(TRIANGLES);

						for (int i1 = 0; i1 < mVectors.length; i1 += 3) {

							vertex(mVectors[i1]);
							vertex(mVectors[i1 + 1]);
							vertex(mVectors[i1 + 2]);
						}
						endShape();
					}

					// if we're in edit mode, enable rollover detection for
					// vertices
					if (readSerial == false) {
						nav.hitDetection();
						// hitDectection();
					}
				}
			}
		}

		popMatrix();

		// turn off depth test so the controlP5 GUI draws correctly
		hint(DISABLE_DEPTH_TEST);

		// if in edit mode, show pop-up
		if (readSerial == false) {
			// myPort.stop();
			textSize(14);
			text("Edit Mode On", 100, 375, 0);
		}
		// do text cues after popMatrix so it doesn't rotate
		doMouseOvers(); // text cues on button rollover

		// ask for another reading
		myPort.write("A"); // check if this is the right place for this line
	}

	public void initCoordArray() {

		for (int z = 6; z > -1; z--) {

			for (int x = 6; x > -1; x--) {

				for (int y = 6; y > -1; y--) {

					Vec3D tempVect = new Vec3D(x * spacing, y * -spacing, z
							* spacing); // maybe should be i,k,j so moves in Y
					Point3d tempPoint = new Point3d(x * spacing, y * -spacing,
							z * spacing);
					masterVectArray = (Vec3D[]) append(masterVectArray,
							tempVect);
					masterPointArray = (Point3d[]) append(masterPointArray,
							tempPoint);
					// println("x: " + x + "  y: " + y + "  z: " + z);
					// println("points: " + masterPointArray.length);
				}
			}
		}

		// println(vectors.length);
	}

	// --------------------------GUI----------------------------------//

	// Initialize buttons and NAV3D class
	public void initControllers() {
		// nav = new Nav3D();

		controlP5 = new ControlP5(this);
		controlP5.setColorBackground(50);

		controlP5.addButton("Hull", 0, 100, 100, 80, 19);
		controlP5.addButton("WireFrame", 0, 100, 120, 80, 19);
		controlP5.addButton("Grid", 0, 100, 140, 80, 19);
		controlP5.addButton("Export", 0, 100, 160, 80, 19);
		controlP5.addButton("Spline", 0, 100, 180, 80, 19);
		controlP5.addButton("Edit", 0, 100, 200, 80, 19);
		controlP5.addButton("Save", 0, 100, 220, 80, 19);
		controlP5.addButton("Load", 0, 100, 240, 80, 19);
		controlP5.addButton("Knot", 0, 100, 260, 80, 19);
		controlP5.addButton("ExportKnot", 0, 100, 280, 80, 19);
		controlP5.addButton("ClearKnot", 0, 100, 300, 80, 19);
		controlP5.addButton("CloseKnot", 0, 100, 320, 80, 19);
		controlP5.addButton("Tree", 0, 100, 360, 80, 19);
		controlP5.addButton("ExportTree", 0, 100, 380, 80, 19);
		controlP5.addButton("ClearTree", 0, 100, 400, 80, 19);

		controlP5.addButton("GreenHull", 0, 200, 120, 80, 19);
		controlP5.addButton("RedHull", 0, 200, 140, 80, 19);

		controlP5.addButton("ExportGreen", 0, 200, 180, 80, 19);
		controlP5.addButton("ExportRed", 0, 200, 200, 80, 19);

		Slider s = controlP5.addSlider("offset", 5, 35, offset, 100, 340, 80,
				19);

		controlP5.Label label = s.captionLabel();
		label.style().marginLeft = -140;
		s.setColorLabel(50);
		s.setLabel("knot width");

		checkBox = controlP5.addCheckBox("checkBox", 100, 420);
		checkBox.setSize(20, 20);
		checkBox.setItemsPerRow(1);
		checkBox.setSpacingRow(2);
		checkBox.addItem("red", 1);
		checkBox.addItem("green", 2);
		checkBox.deactivateAll();

	}

	public void controlEvent(ControlEvent theEvent) {

		if (theEvent.isGroup()) {
			// print("got an event from "+theEvent.group().name()+"\t");
			// checkbox uses arrayValue to store the state of
			// individual checkbox-items. usage:
			for (int i = 0; i < theEvent.group().arrayValue().length; i++) {
				int n = (int) theEvent.group().arrayValue()[i];

				if (theEvent.group().arrayValue()[0] == 0
						&& theEvent.group().arrayValue()[1] == 0) {
					activeColor = grey;
					println("grey");
				}

				if (n == 1) {
					// ((RadioButton)theEvent.group()).getItem(i).internalValue();
					println(((RadioButton) theEvent.group()).getItem(i)
							.internalValue());
					int selected = (int) ((RadioButton) theEvent.group())
							.getItem(i).internalValue();
					// red
					if (selected == 1) {
						activeColor = red;
						println("red");
					}

					// green
					if (selected == 2) {
						activeColor = green;
						println("green");

					}
				}
			}
		}
	}

	// help text when mouse is over buttons
	public void doMouseOvers() {

		textSize(18);
		fill(50);
		text(version, 100, 90, 0);

		if (controlP5.controller("Hull").isInside()) {
			message = "Toggles the convex hull (fill).";
		}

		if (controlP5.controller("WireFrame").isInside()) {
			message = "Toggles wireframe - works with the Hull button on.";
		}

		if (controlP5.controller("Grid").isInside()) {
			message = "Turns the background grid on or off.";
		}

		if (controlP5.controller("Export").isInside()) {
			message = "Exports your shape as an .stl file for 3D printing.";
		}

		if (controlP5.controller("Spline").isInside()) {
			message = "Connects the points with spines (curves).";
		}

		if (controlP5.controller("Edit").isInside()) {
			message = "Turns on edit mode, where you can click and drag points to alter the shape.";
		}

		if (controlP5.controller("Save").isInside()) {
			message = "Save your shape to a text file that you can load later.";
		}

		if (controlP5.controller("Load").isInside()) {
			message = "Load a shape that you have previously saved.";
		}

		if (controlP5.controller("Knot").isInside()) {
			message = "Make a knot.";
		}

		if (controlP5.controller("ExportKnot").isInside()) {
			message = "Export knot as an STL file.";
		}

		if (controlP5.controller("ClearKnot").isInside()) {
			message = "Clears the knot so you can start a new path.";
		}

		if (controlP5.controller("CloseKnot").isInside()) {
			message = "Closes your knot.";
		}

		if (controlP5.controller("offset").isInside()) {
			message = "Changes the thickness of the knot.";
		}

		if (controlP5.controller("Tree").isInside()) {
			message = "Makes a minimal spaninng tree.";
		}

		if (controlP5.controller("ExportTree").isInside()) {
			message = "Exports the STL of your tree.";
		}

		if (controlP5.controller("ClearTree").isInside()) {
			message = "Clears the points of the Tree.";
		}

		if (controlP5.controller("RedHull").isInside()) {
			message = "Toggles the red convex hull (fill).";
		}

		if (controlP5.controller("GreenHull").isInside()) {
			message = "Toggles the green convex hull (fill).";
		}

		if (controlP5.controller("ExportRed").isInside()) {
			message = "Exports the STL of the Red Hull.";
		}

		if (controlP5.controller("ExportGreen").isInside()) {
			message = "Exports the STL of the Green Hull";
		}

		if (message != null && controlP5.window(this).isMouseOver()) {
			textSize(14);
			text(message, 100, 550, 0);
		}
	}

	// pass mouse and key events to our Nav3D instance
	public void mouseDragged() {
		// ignore mouse event if cursor is over controlP5 GUI elements
		if (controlP5.window(this).isMouseOver())
			return;

		nav.mouseDragged();
	}

	public void mouseReleased() {
		nav.mouseReleased();
	}

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

	// toggle convex hull
	public void Hull(int theValue) {
		if (doHull == true) {
			doHull = false;
		} else if (doHull == false) {
			doHull = true;
		}
	}

	public void GreenHull(int theValue) {
		if (doBlueHull == true) {
			doBlueHull = false;
		} else if (doBlueHull == false) {
			doBlueHull = true;
		}
	}

	public void RedHull(int theValue) {
		if (doRedHull == true) {
			doRedHull = false;
		} else if (doRedHull == false) {
			doRedHull = true;
		}
	}

	// toggle wireframe
	public void WireFrame(int theValue) {

		if (doFill == true) {
			doHull = true;
			doFill = false;
		} else if (doFill == false) {
			doHull = true;
			doFill = true;
		}
	}

	// toggle grid
	public void Grid(int theValue) {

		if (doGrid == true) {
			doGrid = false;
		} else if (doGrid == false) {
			doGrid = true;
		}
	}

	// toggle knot
	public void Knot(int theValue) {

		if (doKnot == true) {
			doKnot = false;
		} else if (doKnot == false) {
			doKnot = true;
			drawKnot();
		}
	}

	// clear the knot arrays
	public void ClearKnot(int theValue) {
		clearKnot();
	}

	// close last segment of Knot
	public void CloseKnot(int theValue) {
		closeKnot();
	}

	// toggle MST
	public void Tree(int theValue) {

		if (doMst == true) {
			doMst = false;
		} else if (doMst == false) {
			doMst = true;
			drawMst();
		}
	}

	// clear MST
	public void ClearTree(int theValue) {

		mVectors = new Vec3D[0];
		mstVectors.clear();
		mstPoints = new Point3d[0];

	}

	// enter edit mode
	public void Edit(int theValue) {

		if (readSerial == true) {
			readSerial = false;
			nav.mouseOver = true;
		} else if (readSerial == false) {
			readSerial = true;
			nav.mouseOver = false;
		}
	}

	// toggle spline
	public void Spline(int theValue) {

		if (doSpline == true) {
			doSpline = false;
		}

		else if (doSpline == false) {
			doSpline = true;
		}
	}

	// draw a spline through the points
	public void drawSpline() {

		if (points.length > 2) {

			sVectors = new Vec3D[0];

			for (int i = 0; i < points.length; i++) {

				float x = (float) points[i].x;
				float y = (float) points[i].y;
				float z = (float) points[i].z;

				Vec3D tempVect = new Vec3D(x, y, z);
				sVectors = (Vec3D[]) append(sVectors, tempVect);
			}

			Spline3D spline = new Spline3D(knotVectors);
			java.util.List vertices = spline.computeVertices(8);

			noFill();
			beginShape();
			for (Iterator i = vertices.iterator(); i.hasNext();) {
				Vec3D v = (Vec3D) i.next();
				vertex(v.x, v.y, v.z);
			}
			endShape();
		}
	}

	// export stl of convex hull
	public void Export(int theValue) {
		drawHull(vectors, points, grey);
		outputSTL();
	}

	// export stl of knot
	public void ExportKnot(int theValue) {
		outputKnot();
	}

	// export STL of MST
	public void ExportTree(int theValue) {
		outputMst();
	}

	// export Green Hull
	public void ExportGreen(int theValue) {
		// TODO: get STL vectors from HullBuilder class
		// blueHull();
		blueSTL();
	}

	// export Red Hull
	public void ExportRed(int theValue) {

		// redHull();
		redSTL();
	}

	// use this for convex hulls
	public void outputSTL(Vec3D[] vectors, TriangleMesh mesh) {
		TriangleMesh mySTL = new TriangleMesh();

		for (int i = 0; i < vectors.length; i += 3) {

			mesh.addFace(vectors[i], vectors[i + 1], vectors[i + 2]);
			// println(vectors[i] + " " + vectors[i+1] + " " + vectors[i+2]);
		}

		mySTL.addMesh(mesh);
		mySTL.saveAsSTL(selectOutput());
	}

	// Use this for knot and mst
	public void outputPath(Vec3D[] vectors, TriangleMesh mesh) {
		TriangleMesh mySTL = new TriangleMesh();

		for (int i = 0; i < vectors.length; i += 3) {

			mesh.addFace(vectors[i], vectors[i + 1], vectors[i + 2]);
			// println(vectors[i] + " " + vectors[i+1] + " " + vectors[i+2]);
		}

		mesh.flipVertexOrder();
		mySTL.addMesh(mesh);
		mySTL.saveAsSTL(selectOutput());
	}

	// stl writer for convex hull
	public void outputSTL() {

		TriangleMesh mySTL = new TriangleMesh();

		// for (int i = 0; i < vectors.length; i += 3) {
		//
		// // scale(.05);
		// mesh.addFace(vectors[i], vectors[i + 1], vectors[i + 2]);
		// // println(vectors[i] + " " + vectors[i+1] + " " + vectors[i+2]);
		// }

		for (int i = 0; i < vectors.size(); i += 3) {

			mesh.addFace(vectors.get(i), vectors.get(i + 1), vectors.get(i + 2));

		}

		mySTL.addMesh(mesh);
		mySTL.saveAsSTL(selectOutput());
	}

	// stl writer for green hull
	public void blueSTL() {

		TriangleMesh myBlueSTL = new TriangleMesh();

		for (int i = 0; i < blueSTLVectors.size(); i += 3) {

			blueMesh.addFace(blueSTLVectors.get(i), blueSTLVectors.get(i + 1),
					blueSTLVectors.get(i + 2));

		}

		myBlueSTL.addMesh(blueMesh);
		myBlueSTL.saveAsSTL(selectOutput());
	}

	public void redSTL() {

		TriangleMesh myRedSTL = new TriangleMesh();

		for (int i = 0; i < redSTLVectors.size(); i += 3) {
			redMesh.addFace(redSTLVectors.get(i), redSTLVectors.get(i + 1),
					redSTLVectors.get(i + 2));
		}

		myRedSTL.addMesh(redMesh);
		myRedSTL.saveAsSTL(selectOutput());
	}

	// stl writer for knot
	public void outputKnot() {

		TriangleMesh mySTL = new TriangleMesh();

		for (int i = 0; i < kVectors.length; i += 3) {

			knotMesh.addFace(kVectors[i], kVectors[i + 1], kVectors[i + 2]);
			println(kVectors[i] + " " + kVectors[i + 1] + " " + kVectors[i + 2]);
		}

		knotMesh.flipVertexOrder();
		mySTL.addMesh(knotMesh);
		mySTL.saveAsSTL(selectOutput());
	}

	public void outputMst() {

		TriangleMesh mySTL = new TriangleMesh();

		for (int i = 0; i < mVectors.length; i += 3) {

			mstMesh.addFace(mVectors[i], mVectors[i + 1], mVectors[i + 2]);
			println(mVectors[i] + " " + mVectors[i + 1] + " " + mVectors[i + 2]);
		}

		mstMesh.flipVertexOrder();
		mySTL.addMesh(mstMesh);
		mySTL.saveAsSTL(selectOutput());

	}

	// save a text file of active points
	public void Save(int theValue) {

		// Create a new file in the sketch directory
		output = createWriter(selectOutput());
		// write the coordinates to the file
		// for (int i = 0; i < vectors.length; i++) {
		// output.print(vectors[i].x / spacing + "," + vectors[i].y / spacing
		// * -1 + "," + vectors[i].z / spacing + ";");
		// }

		for (int i = 0; i < vectors.size(); i++) {
			output.print(vectors.get(i).x / spacing + "," + vectors.get(i).y
					/ spacing * -1 + "," + vectors.get(i).z / spacing + ";");
		}

		output.flush();
		output.close();
	}

	// load in a text file of active points
	public void Load(int theValue) {

		// read in a text file
		reader = createReader(selectInput());

		try {
			inString = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// --------------------------KNOT
	// FUNCTIONS----------------------------------//

	public void drawKnot() {

		strokeWeight(1);
		fill(200);

		for (int i = 0; i < knotVectors.size() - 1; i++) {

			Vec3D vec = knotVectors.get(i);
			float x = (float) vec.x;
			float y = (float) vec.y;
			float z = (float) vec.z;

			Vec3D vec2 = knotVectors.get(i + 1);
			float x2 = (float) vec2.x;
			float y2 = (float) vec2.y;
			float z2 = (float) vec2.z;

			line(x, y, z, x2, y2, z2);

		}

		for (int i = 0; i < knotVectors.size() - 1; i++) {

			Vec3D vec = knotVectors.get(i);
			float x = (float) vec.x;
			float y = (float) vec.y;
			float z = (float) vec.z;

			Vec3D vec2 = knotVectors.get(i + 1);
			float x2 = (float) vec2.x;
			float y2 = (float) vec2.y;
			float z2 = (float) vec2.z;

			knotPoints = new Point3d[0];

			Point3d p1 = new Point3d(x + offset, y + offset, z + offset);
			Point3d p2 = new Point3d(x + offset, y + offset, z - offset);
			Point3d p3 = new Point3d(x + offset, y - offset, z + offset);
			Point3d p4 = new Point3d(x - offset, y + offset, z + offset);

			Point3d p5 = new Point3d(x - offset, y - offset, z + offset);
			Point3d p6 = new Point3d(x - offset, y + offset, z - offset);
			Point3d p7 = new Point3d(x + offset, y - offset, z - offset);
			Point3d p8 = new Point3d(x - offset, y - offset, z - offset);

			Point3d p9 = new Point3d(x2 + offset, y2 + offset, z2 + offset);
			Point3d p10 = new Point3d(x2 + offset, y2 + offset, z2 - offset);
			Point3d p11 = new Point3d(x2 + offset, y2 - offset, z2 + offset);
			Point3d p12 = new Point3d(x2 - offset, y2 + offset, z2 + offset);

			Point3d p13 = new Point3d(x2 - offset, y2 - offset, z2 + offset);
			Point3d p14 = new Point3d(x2 - offset, y2 + offset, z2 - offset);
			Point3d p15 = new Point3d(x2 + offset, y2 - offset, z2 - offset);
			Point3d p16 = new Point3d(x2 - offset, y2 - offset, z2 - offset);

			knotPoints = (Point3d[]) append(knotPoints, p1);
			knotPoints = (Point3d[]) append(knotPoints, p2);
			knotPoints = (Point3d[]) append(knotPoints, p3);
			knotPoints = (Point3d[]) append(knotPoints, p4);

			knotPoints = (Point3d[]) append(knotPoints, p5);
			knotPoints = (Point3d[]) append(knotPoints, p6);
			knotPoints = (Point3d[]) append(knotPoints, p7);
			knotPoints = (Point3d[]) append(knotPoints, p8);

			knotPoints = (Point3d[]) append(knotPoints, p9);
			knotPoints = (Point3d[]) append(knotPoints, p10);
			knotPoints = (Point3d[]) append(knotPoints, p11);
			knotPoints = (Point3d[]) append(knotPoints, p12);

			knotPoints = (Point3d[]) append(knotPoints, p13);
			knotPoints = (Point3d[]) append(knotPoints, p14);
			knotPoints = (Point3d[]) append(knotPoints, p15);
			knotPoints = (Point3d[]) append(knotPoints, p16);

			doKnotHull(knotPoints);
		}
	}

	// close knot by taking last point and first point in array and adding that
	// hull to the shape
	public void closeKnot() {

		int i = knotVectors.size();
		println(i);

		Vec3D vec = knotVectors.get(0);
		float x = (float) vec.x;
		float y = (float) vec.y;
		float z = (float) vec.z;

		Vec3D vec2 = knotVectors.get(i - 1);
		float x2 = (float) vec2.x;
		float y2 = (float) vec2.y;
		float z2 = (float) vec2.z;

		knotPoints = new Point3d[0];

		Point3d p1 = new Point3d(x + offset, y + offset, z + offset);
		Point3d p2 = new Point3d(x + offset, y + offset, z - offset);
		Point3d p3 = new Point3d(x + offset, y - offset, z + offset);
		Point3d p4 = new Point3d(x - offset, y + offset, z + offset);

		Point3d p5 = new Point3d(x - offset, y - offset, z + offset);
		Point3d p6 = new Point3d(x - offset, y + offset, z - offset);
		Point3d p7 = new Point3d(x + offset, y - offset, z - offset);
		Point3d p8 = new Point3d(x - offset, y - offset, z - offset);

		Point3d p9 = new Point3d(x2 + offset, y2 + offset, z2 + offset);
		Point3d p10 = new Point3d(x2 + offset, y2 + offset, z2 - offset);
		Point3d p11 = new Point3d(x2 + offset, y2 - offset, z2 + offset);
		Point3d p12 = new Point3d(x2 - offset, y2 + offset, z2 + offset);

		Point3d p13 = new Point3d(x2 - offset, y2 - offset, z2 + offset);
		Point3d p14 = new Point3d(x2 - offset, y2 + offset, z2 - offset);
		Point3d p15 = new Point3d(x2 + offset, y2 - offset, z2 - offset);
		Point3d p16 = new Point3d(x2 - offset, y2 - offset, z2 - offset);

		knotPoints = (Point3d[]) append(knotPoints, p1);
		knotPoints = (Point3d[]) append(knotPoints, p2);
		knotPoints = (Point3d[]) append(knotPoints, p3);
		knotPoints = (Point3d[]) append(knotPoints, p4);

		knotPoints = (Point3d[]) append(knotPoints, p5);
		knotPoints = (Point3d[]) append(knotPoints, p6);
		knotPoints = (Point3d[]) append(knotPoints, p7);
		knotPoints = (Point3d[]) append(knotPoints, p8);

		knotPoints = (Point3d[]) append(knotPoints, p9);
		knotPoints = (Point3d[]) append(knotPoints, p10);
		knotPoints = (Point3d[]) append(knotPoints, p11);
		knotPoints = (Point3d[]) append(knotPoints, p12);

		knotPoints = (Point3d[]) append(knotPoints, p13);
		knotPoints = (Point3d[]) append(knotPoints, p14);
		knotPoints = (Point3d[]) append(knotPoints, p15);
		knotPoints = (Point3d[]) append(knotPoints, p16);

		doKnotHull(knotPoints);
	}

	public void doKnotHull(Point3d[] knotPoints) {

		int numPoints = knotPoints.length;
		// kVectors = new Vec3D[0];

		if (knotHull.myCheck(knotPoints, numPoints) == false) {
		} else if (knotHull.myCheck(knotPoints, numPoints) == true) {

			knotHull.build(knotPoints);
			knotHull.triangulate();
			// get an array of the vertices so we can get the faces
			Point3d[] vertices = knotHull.getVertices();

			fill(200);
			if (doFill == false) {
				noFill();
			}
			int[][] faceIndices = knotHull.getFaces();
			for (int i = 0; i < faceIndices.length; i++) {

				for (int k = 0; k < faceIndices[i].length; k++) {

					// get points that correspond to each face
					Point3d pnt2 = vertices[faceIndices[i][k]];
					float x = (float) pnt2.x;
					float y = (float) pnt2.y;
					float z = (float) pnt2.z;
					// vertex(x, y, z);
					Vec3D tempVect = new Vec3D(x, y, z);
					// println(x + "," + y + "," + z + " " + k);
					kSavedPoints = (Point3d[]) append(kSavedPoints, pnt2);
					kVectors = (Vec3D[]) append(kVectors, tempVect);

					// println(x + "," + y + "," + z);
				}
			}
			// endShape(CLOSE);
			reDrawKnot = false;
			println("false");
		}
	}

	void vertex(Vec3D v) {
		vertex(v.x, v.y, v.z);
	}

	public void clearKnot() {

		knotVectors.clear();
		kVectors = null;
		kVectors = new Vec3D[0];

		knotPoints = null;
		knotPoints = new Point3d[0];
		knotMesh.clear();
	}

	// --------------------------MOUSEOVER FUNCTIONS / EDIT
	// MODE----------------------------------//

	// // Function for detecting if mouse is over an active vertex
	// public void hitDetection() {
	//
	// for (int i = 0; i < vectors.length; i++) {
	//
	// x = screenX((float) vectors[i].x, (float) vectors[i].y,
	// (float) vectors[i].z);
	// y = screenY((float) vectors[i].x, (float) vectors[i].y,
	// (float) vectors[i].z);
	// // println(x + " " + y);
	// Vec2D v2d = new Vec2D(x, y);
	//
	// mouseOverVectors = (Vec2D[]) append(mouseOverVectors, v2d);
	//
	// if (x > mouseX - 3 && x < mouseX + 3 && y > mouseY - 3
	// && y < mouseY + 3) {
	// vertexMouseOver = i;
	// }
	// }
	// }

	// // --------------------------NAV3D----------------------------------//
	//
	// // utility class for controlling 3D camera. supports rotating
	// // by dragging the mouse,panning with shift-click and zooming
	// // with the mouse wheel.
	//
	// public class Nav3D {
	// float rotX, rotY;
	// float tx, ty, tz;
	//
	// void transform() {
	// translate(width / 2, height / 2);
	// translate(tx, ty, tz);
	// rotateY(rotY);
	// rotateX(rotX);
	// }
	//
	// public void mouseReleased() {
	// vertexMouseOver = -1;
	// }
	//
	// public void mouseDragged() {
	//
	// // if edit mode is on, and the mouse is over a point, do stuff
	// if (mouseOver == true && vertexMouseOver != -1) {
	//
	// println("mouseOver: " + mouseOver);
	// println(mouseOverVectors[vertexMouseOver]);
	//
	// vectors[vertexMouseOver].x = mouseX - width / 2;
	// vectors[vertexMouseOver].y = mouseY - height / 2;
	//
	// reDraw = true;
	// drawHull();
	// }
	//
	// else if (mouseOver == false) {
	// // calculate rotX and rotY by the relative change
	// // in mouse position
	// if (keyEvent != null && keyEvent.isShiftDown()) {
	// tx += radians(mouseX - pmouseX) * 10;
	// ty += radians(mouseY - pmouseY) * 10;
	//
	// } else {
	// rotY += radians(mouseX - pmouseX);
	// rotX -= radians(mouseY - pmouseY);
	//
	// }
	// }
	// }
	//
	// public void keyPressed() {
	// if (key == CODED) {
	// // check to see if CTRL is pressed
	// if (keyEvent.isControlDown()) {
	// // do zoom in the Z axis
	// if (keyCode == UP)
	// tz = tz + 2;
	// if (keyCode == DOWN)
	// tz = tz - 2;
	// }
	// // check to see if SHIFT is pressed
	// else if (keyEvent.isShiftDown()) {
	// // do translations in X and Y axis
	// if (keyCode == UP)
	// ty = ty - 2;
	// if (keyCode == DOWN)
	// ty = ty + 2;
	// if (keyCode == RIGHT)
	// tx = tx + 2;
	// if (keyCode == LEFT)
	// tx = tx - 2;
	// } else {
	// // do rotations around X and Y axis
	// if (keyCode == UP)
	// rotX = rotX + radians(2);
	// if (keyCode == DOWN)
	// rotX = rotX - radians(2);
	// if (keyCode == RIGHT)
	// rotY = rotY + radians(2);
	// if (keyCode == LEFT)
	// rotY = rotY - radians(2);
	// }
	// } else {
	// if (keyEvent.isControlDown()) {
	// if (keyCode == 'R') {
	// println("Reset transformations.");
	// tx = 0;
	// ty = 0;
	// tz = 0;
	// rotX = 0;
	// rotY = 0;
	// }
	// }
	// }
	// }
	//
	// // void mouseWheelMoved(float step) {
	// // tz=tz+step*15;
	// // }
	// }

	// --------------------------NEWHULL: Convex Hull
	// Functions----------------------------------//

	// colorHulls

	public void initRedPoints() {

		// redPoints = new Point3d[redVectors.size()];
		redPoints = new Point3d[0];

		for (int i = 0; i < redVectors.size(); i++) {

			float x = redVectors.get(i).x;
			float y = redVectors.get(i).y;
			float z = redVectors.get(i).z;

			Point3d tempPnt = new Point3d(x, y, z);

			// redPoints[i] = new Point3d(x,y,z);
			redPoints = (Point3d[]) append(redPoints, tempPnt);
		}

	}

	public void initBluePoints() {

		// redPoints = new Point3d[redVectors.size()];
		bluePoints = new Point3d[0];

		for (int i = 0; i < blueVectors.size(); i++) {

			float x = blueVectors.get(i).x;
			float y = blueVectors.get(i).y;
			float z = blueVectors.get(i).z;

			Point3d tempPnt = new Point3d(x, y, z);

			// redPoints[i] = new Point3d(x,y,z);
			bluePoints = (Point3d[]) append(bluePoints, tempPnt);
		}

	}

	public void initPoints(ArrayList<Vec3D> vectors, Point3d[] points) {

		points = new Point3d[0];

		if (vectors.size() > 0) {

			for (int i = 0; i < vectors.size(); i++) {

				float x = vectors.get(i).x;
				float y = vectors.get(i).y;
				float z = vectors.get(i).z;

				Point3d tempPnt = new Point3d(x, y, z);

				// redPoints[i] = new Point3d(x,y,z);
				points = (Point3d[]) append(points, tempPnt);
			}
		}
	}

	public void drawHull(ArrayList<Vec3D> vectors, Point3d[] points, int color) {

		initPoints(vectors, points);

		hb.reDraw = true;
		// TODO: draw from hullBuilder
		hb.makeHull(points);

		if (hb.hull.myCheck(points, points.length) == false) {

			beginShape(TRIANGLE_STRIP);

			strokeWeight(1);
			stroke(color);
			fill(color);

			for (int j = 0; j < points.length; j++) {

				float x = (float) points[j].x;
				float y = (float) points[j].y;
				float z = (float) points[j].z;
				vertex(x, y, z);
			}

			endShape(CLOSE);

		} else {

			strokeWeight(1);
			stroke(color);
			fill(color);
			beginShape(TRIANGLES);

			for (int i1 = 0; i1 < hb.vectors.size(); i1 += 3) {

				vertex(hb.vectors.get(i1));
				vertex(hb.vectors.get(i1 + 1));
				vertex(hb.vectors.get(i1 + 2));
			}
			endShape();
		}

	}

	// public void redHull() {
	//
	// // println("redhull");
	//
	// int numPoints = redPoints.length;
	// println("redhull: " + redPoints.length);
	// // check that our hull is valid
	//
	// if (redHull.myCheck(redPoints, numPoints) == false) {
	//
	// // brute force inefficiency
	// beginShape(TRIANGLE_STRIP);
	// strokeWeight(1);
	// fill(red);
	// stroke(red);
	//
	// for (int j = 0; j < numPoints; j++) {
	//
	// float x = (float) redPoints[j].x;
	// float y = (float) redPoints[j].y;
	// float z = (float) redPoints[j].z;
	// vertex(x, y, z);
	// }
	//
	// endShape(CLOSE);
	// }
	//
	// else if (redHull.myCheck(redPoints, numPoints) == true) {
	//
	// if (reDraw == true) {
	// // println(reDraw);
	// redHull.build(redPoints);
	// redHull.triangulate();
	// // get an array of the vertices so we can get the faces
	// Point3d[] vertices = redHull.getVertices();
	// savedRedPoints = new Point3d[0];
	// // vectors = new Vec3D[0];
	// // redVectors = new Vec3D[0];
	// // redVectors.clear();
	//
	// beginShape(TRIANGLE_STRIP);
	// strokeWeight(1);
	// fill(red);
	// stroke(red);
	// if (doFill == false) {
	// noFill();
	// }
	// int[][] faceIndices = redHull.getFaces();
	// for (int i = 0; i < faceIndices.length; i++) {
	// for (int k = 0; k < faceIndices[i].length; k++) {
	//
	// // get points that correspond to each face
	// Point3d pnt2 = vertices[faceIndices[i][k]];
	// float x = (float) pnt2.x;
	// float y = (float) pnt2.y;
	// float z = (float) pnt2.z;
	// vertex(x, y, z);
	// Vec3D tempVect = new Vec3D(x, y, z);
	// savedRedPoints = (Point3d[]) append(savedRedPoints,
	// pnt2);
	// // redSTLVectors = (Vec3D[]) append(redVectors,
	// // tempVect);
	// // vectors = (Vec3D[])append(vectors, tempVect);
	// redSTLVectors.add(tempVect);
	// }
	// }
	// endShape(CLOSE);
	// // reDraw = false;
	// }
	//
	// // else if (reDraw == false) {
	// // // println(reDraw);
	// // beginShape(TRIANGLE_STRIP);
	// // strokeWeight(1);
	// // fill(red);
	// // stroke(red);
	// // if (doFill == false) {
	// // noFill();
	// // }
	// // for (int i = 0; i < savedRedPoints.length; i++) {
	// //
	// // float x = (float) savedRedPoints[i].x;
	// // float y = (float) savedRedPoints[i].y;
	// // float z = (float) savedRedPoints[i].z;
	// // vertex(x, y, z);
	// // }
	// // endShape(CLOSE);
	// // }
	// }
	// }
	//
	// public void blueHull() {
	//
	// bluePoints = new Point3d[blueVectors.size()];
	//
	// for (int i = 0; i < blueVectors.size(); i++) {
	//
	// float x = blueVectors.get(i).x;
	// float y = blueVectors.get(i).y;
	// float z = blueVectors.get(i).z;
	//
	// bluePoints[i] = new Point3d(x, y, z);
	//
	// }
	//
	// int numPoints = bluePoints.length;
	// // println("bluehull: " + bluePoints.length);
	// // check that our hull is valid
	//
	// if (blueHull.myCheck(bluePoints, numPoints) == false) {
	//
	// // brute force inefficiency
	// beginShape(TRIANGLE_STRIP);
	// strokeWeight(1);
	// fill(green);
	// stroke(green);
	//
	// for (int j = 0; j < numPoints; j++) {
	//
	// float x = (float) bluePoints[j].x;
	// float y = (float) bluePoints[j].y;
	// float z = (float) bluePoints[j].z;
	// vertex(x, y, z);
	// }
	//
	// endShape(CLOSE);
	// }
	//
	// else if (blueHull.myCheck(bluePoints, numPoints) == true) {
	//
	// if (reDraw == true) {
	// // println(reDraw);
	// blueHull.build(bluePoints);
	// blueHull.triangulate();
	// // get an array of the vertices so we can get the faces
	// Point3d[] vertices = blueHull.getVertices();
	// savedBluePoints = new Point3d[0];
	// // vectors = new Vec3D[0];
	// // blueVectors = new Vec3D[0];
	// // blueVectors.clear();
	//
	// beginShape(TRIANGLE_STRIP);
	// strokeWeight(1);
	// fill(green);
	// stroke(green);
	// if (doFill == false) {
	// noFill();
	// }
	// int[][] faceIndices = blueHull.getFaces();
	// for (int i = 0; i < faceIndices.length; i++) {
	// for (int k = 0; k < faceIndices[i].length; k++) {
	//
	// // get points that correspond to each face
	// Point3d pnt2 = vertices[faceIndices[i][k]];
	// float x = (float) pnt2.x;
	// float y = (float) pnt2.y;
	// float z = (float) pnt2.z;
	// vertex(x, y, z);
	// Vec3D tempVect = new Vec3D(x, y, z);
	// savedPoints = (Point3d[]) append(savedBluePoints, pnt2);
	// // vectors = (Vec3D[])append(vectors, tempVect);
	// // blueVectors = (Vec3D[]) append(blueVectors,
	// // tempVect);
	// blueSTLVectors.add(tempVect);
	// }
	// }
	// endShape(CLOSE);
	// // reDraw = false;
	// }
	//
	// else if (reDraw == false) {
	// // println(reDraw);
	// beginShape(TRIANGLE_STRIP);
	// strokeWeight(1);
	// fill(green);
	// stroke(green);
	// if (doFill == false) {
	// noFill();
	// }
	// for (int i = 0; i < savedPoints.length; i++) {
	//
	// float x = (float) savedPoints[i].x;
	// float y = (float) savedPoints[i].y;
	// float z = (float) savedPoints[i].z;
	// vertex(x, y, z);
	// }
	// endShape(CLOSE);
	// }
	// }
	// }

	// public void drawHull() {
	//
	// int numPoints = points.length;
	// // check that our hull is valid
	// // println(numPoints);
	//
	// if (hull.myCheck(points, numPoints) == false) {
	//
	// // brute force inefficiency
	// beginShape(TRIANGLE_STRIP);
	// strokeWeight(1);
	// fill(200);
	//
	// for (int j = 0; j < numPoints; j++) {
	//
	// float x = (float) points[j].x;
	// float y = (float) points[j].y;
	// float z = (float) points[j].z;
	// vertex(x, y, z);
	// }
	//
	// endShape(CLOSE);
	// }
	//
	// else if (hull.myCheck(points, numPoints) == true) {
	//
	// if (reDraw == true) {
	// // print(reDraw);
	// hull.build(points);
	// hull.triangulate();
	// // get an array of the vertices so we can get the faces
	// Point3d[] vertices = hull.getVertices();
	// savedPoints = new Point3d[0];
	// vectors = new Vec3D[0];
	//
	// beginShape(TRIANGLE_STRIP);
	// strokeWeight(1);
	// fill(200);
	// if (doFill == false) {
	// noFill();
	// }
	// int[][] faceIndices = hull.getFaces();
	// for (int i = 0; i < faceIndices.length; i++) {
	// for (int k = 0; k < faceIndices[i].length; k++) {
	//
	// // get points that correspond to each face
	// Point3d pnt2 = vertices[faceIndices[i][k]];
	// float x = (float) pnt2.x;
	// float y = (float) pnt2.y;
	// float z = (float) pnt2.z;
	// vertex(x, y, z);
	// Vec3D tempVect = new Vec3D(x, y, z);
	// // println(x + "," + y + "," + z + " " + k);
	// savedPoints = (Point3d[]) append(savedPoints, pnt2);
	// vectors = (Vec3D[]) append(vectors, tempVect);
	//
	// // println(x + "," + y + "," + z);
	// }
	// }
	// endShape(CLOSE);
	// reDraw = false;
	// }
	//
	// else if (reDraw == false) {
	// // print(reDraw);
	// beginShape(TRIANGLE_STRIP);
	// strokeWeight(1);
	// fill(200);
	// if (doFill == false) {
	// noFill();
	// }
	// for (int i = 0; i < savedPoints.length; i++) {
	//
	// float x = (float) savedPoints[i].x;
	// float y = (float) savedPoints[i].y;
	// float z = (float) savedPoints[i].z;
	// vertex(x, y, z);
	// }
	// endShape(CLOSE);
	// }
	// }
	// }

	// MINIMAL SPANNING TREE

	public void drawMst() {

		// int offset = 10;
		// int offset = spacing/2;
		// lerpPoints();
		// doCubes();

		strokeWeight(1);
		fill(200);
		maximumVertices = mstVectors.size();
		g = new Graph(maximumVertices);

		for (int i = 0; i < mstVectors.size(); i++) {

			g.addVertex(i, mstVectors.get(i).x, mstVectors.get(i).y,
					mstVectors.get(i).z);

		}

		for (int j = 0; j < maximumVertices; j++) {
			for (int k = 0; k < maximumVertices; k++) {
				if (k != j) {
					g.addEdge(j, k);
				}
				println(j + " " + k);
			}
		}

		Kruskal k = new Kruskal(g);
		mst = k.getMSTEdges();

		// System.out.println ("Minimum Spanning Tree Edges are:");
		// java.util.ListIterator it = mst.listIterator();
		// while (it.hasNext ()) {
		// Edge e = (Edge)it.next();
		// System.out.println ("v" + e.getFrom().getVertexNo() + " --- v" +
		// e.getTo().getVertexNo());
		// }

		for (int i = 0; i < mst.size(); i++) {

			println(i);

			Edge e = (Edge) mst.get(i);

			float x = (float) e.getFrom().getX();
			float y = (float) e.getFrom().getY();
			float z = (float) e.getFrom().getZ();

			float x2 = (float) e.getTo().getX();
			float y2 = (float) e.getTo().getY();
			float z2 = (float) e.getTo().getZ();

			mstPoints = new Point3d[0];

			Point3d p1 = new Point3d(x + offset, y + offset, z + offset);
			Point3d p2 = new Point3d(x + offset, y + offset, z - offset);
			Point3d p3 = new Point3d(x + offset, y - offset, z + offset);
			Point3d p4 = new Point3d(x - offset, y + offset, z + offset);

			Point3d p5 = new Point3d(x - offset, y - offset, z + offset);
			Point3d p6 = new Point3d(x - offset, y + offset, z - offset);
			Point3d p7 = new Point3d(x + offset, y - offset, z - offset);
			Point3d p8 = new Point3d(x - offset, y - offset, z - offset);

			Point3d p9 = new Point3d(x2 + offset, y2 + offset, z2 + offset);
			Point3d p10 = new Point3d(x2 + offset, y2 + offset, z2 - offset);
			Point3d p11 = new Point3d(x2 + offset, y2 - offset, z2 + offset);
			Point3d p12 = new Point3d(x2 - offset, y2 + offset, z2 + offset);

			Point3d p13 = new Point3d(x2 - offset, y2 - offset, z2 + offset);
			Point3d p14 = new Point3d(x2 - offset, y2 + offset, z2 - offset);
			Point3d p15 = new Point3d(x2 + offset, y2 - offset, z2 - offset);
			Point3d p16 = new Point3d(x2 - offset, y2 - offset, z2 - offset);

			mstPoints = (Point3d[]) append(mstPoints, p1);
			mstPoints = (Point3d[]) append(mstPoints, p2);
			mstPoints = (Point3d[]) append(mstPoints, p3);
			mstPoints = (Point3d[]) append(mstPoints, p4);

			mstPoints = (Point3d[]) append(mstPoints, p5);
			mstPoints = (Point3d[]) append(mstPoints, p6);
			mstPoints = (Point3d[]) append(mstPoints, p7);
			mstPoints = (Point3d[]) append(mstPoints, p8);

			mstPoints = (Point3d[]) append(mstPoints, p9);
			mstPoints = (Point3d[]) append(mstPoints, p10);
			mstPoints = (Point3d[]) append(mstPoints, p11);
			mstPoints = (Point3d[]) append(mstPoints, p12);

			mstPoints = (Point3d[]) append(mstPoints, p13);
			mstPoints = (Point3d[]) append(mstPoints, p14);
			mstPoints = (Point3d[]) append(mstPoints, p15);
			mstPoints = (Point3d[]) append(mstPoints, p16);

			doMstHull(mstPoints);

		}
	}

	public void doMstHull(Point3d[] mstPoints) {

		int numPoints = mstPoints.length;
		// kVectors = new Vec3D[0];

		if (mstHull.myCheck(mstPoints, numPoints) == false) {
		} else if (mstHull.myCheck(mstPoints, numPoints) == true) {

			mstHull.build(mstPoints);
			mstHull.triangulate();
			// get an array of the vertices so we can get the faces
			Point3d[] vertices = mstHull.getVertices();

			fill(200);
			if (doFill == false) {
				noFill();
			}
			int[][] faceIndices = mstHull.getFaces();
			for (int i = 0; i < faceIndices.length; i++) {

				for (int k = 0; k < faceIndices[i].length; k++) {

					// get points that correspond to each face
					Point3d pnt2 = vertices[faceIndices[i][k]];
					float x = (float) pnt2.x;
					float y = (float) pnt2.y;
					float z = (float) pnt2.z;
					// vertex(x, y, z);
					Vec3D tempVect = new Vec3D(x, y, z);
					// println(x + "," + y + "," + z + " " + k);
					// kSavedPoints = (Point3d[]) append(kSavedPoints, pnt2);
					mVectors = (Vec3D[]) append(mVectors, tempVect);

					// println(x + "," + y + "," + z);
				}
			}
			// endShape(CLOSE);
			// reDrawKnot = false;
			// println("false");
		}
	}

	// public void clearMst() {
	// mVectors = new Vec3D[0];
	// mstVectors.clear();
	// mstPoints = new Point3d[0];
	// }

	// public class Kruskal {
	//
	// private Graph graph;
	// private int[] sets; //represent set for vertices
	//
	// public Kruskal(Graph g) {
	// this.graph = g;
	// this.sets = new int[g.getTotalNumberOfVertices()];
	// }
	//
	// private void makeSet(Vertex v) {
	// this.sets[v.getVertexNo()] = v.getVertexNo(); //simply set the set name
	// to each vertex no
	// }
	//
	// private int findSet(Vertex v) {
	// return this.sets[v.getVertexNo()]; //gets the set name/number of a vertex
	// }
	//
	// private void union(Vertex u, Vertex v) {
	// int findWhat, replaceWith;
	//
	// if (u.getVertexNo() < v.getVertexNo()) {
	// findWhat = this.sets[v.getVertexNo()];
	// replaceWith = this.sets[u.getVertexNo()];
	// }
	// else {
	// findWhat = this.sets[u.getVertexNo()];
	// replaceWith = this.sets[v.getVertexNo()];
	// }
	//
	// //make both sets same
	// for (int i=0; i<this.sets.length; i++) {
	// if (this.sets[i] == findWhat) {
	// this.sets[i] = replaceWith;
	// }
	// }
	// }
	//
	// private void sortEdges(Edge[] edges) {
	// for (int i=0; i<edges.length-1; i++) {
	// for (int j=i+1; j<edges.length; j++) {
	// if (edges[i].getWeight() > edges[j].getWeight()) {
	// Edge tmp = edges[i];
	// edges[i] = edges[j];
	// edges[j] = tmp;
	// }
	// }
	// }
	// }
	//
	// //runs the main kruskal algorithm
	// public List<Edge> getMSTEdges() {
	// //holds the MST edges
	// List<Edge> mstEdges = new ArrayList<Edge>();
	//
	// Vertex[] vertices = this.graph.getVertices();
	// for (int i=0; i<vertices.length; i++) {
	// this.makeSet(vertices[i]);
	// }
	//
	// //get all bi-directional edges
	// Edge[] edges = this.graph.getAllBidirectionalEdges();
	// //sort the edges w.r.t their weights in non-decreasing order
	// this.sortEdges(edges);
	//
	// for (int i=0; i<edges.length; i++) {
	// //for each each, in sorted order
	// Edge e = edges[i];
	// if (this.findSet(e.getFrom()) != this.findSet(e.getTo())) {
	// //if the vertices it connects are not in the same set
	// //this edge is an MST edge
	// mstEdges.add(e);
	// //now, both vertices should have same set
	// this.union(e.getFrom(), e.getTo());
	// }
	// }
	//
	// return mstEdges;
	// }
	// }

	// //edge class is needed for Kruskal's algorithm
	// class Edge {
	// private Vertex fromVertex=null, toVertex=null;
	// private float weight;
	//
	//
	// public Edge(Vertex from, Vertex to, float weight) {
	// this.fromVertex = from;
	// this.toVertex = to;
	// //this.weight = weight;
	// this.weight =
	// dist(fromVertex.x,fromVertex.y,fromVertex.z,toVertex.x,toVertex.y,toVertex.z);
	// }
	//
	// public Vertex getFrom() {
	// return this.fromVertex;
	// }
	// public Vertex getTo() {
	// return this.toVertex;
	// }
	// public float getWeight() {
	// return this.weight;
	// }
	//
	//
	//
	// }

	// class Graph {
	// private final int DEFAULT_EDGE_COST = 1;
	// private Vertex[] vertices = null; //list of all vertices in the graph
	// private int totalVertices = 0; //keeps count of vertices
	// private int[][] adjMatrix = null; //keeps the edges of the graph using
	// adjacency matrix
	// private int[] adjacentVertCount = null; //keeps count of adjacent
	// vertices for each vertex
	//
	// public Graph(int maxVertices) {
	// this.vertices = new Vertex[maxVertices]; //initialize vertices array
	// this.adjMatrix = new int[maxVertices][maxVertices]; //initialize
	// adjacency matrix
	// this.adjacentVertCount = new int[maxVertices]; //initialize adjacent
	// vertices count
	// for (int i=0; i<maxVertices; i++) {
	// this.adjacentVertCount[i] = 0; //set adjacent vertex count to 0 initially
	// for (int j=0; j<maxVertices; j++) {
	// this.adjMatrix[i][j] = -1; //set adjacency list to -1 initially
	// }
	// }
	// }
	//
	// public Graph() {
	// //default Max amount of vertices: 100 [0-99]
	// this(100);
	// }
	//
	// // //add a new vertex with vertexNo and data
	// // public void addVertex(int vertexNo, Object data) {
	// // this.vertices[vertexNo] = new Vertex(vertexNo, data);
	// // this.totalVertices++;
	// // }
	//
	// // //add a new vertex with vertexNo only
	// // public void addVertex(int vertexNo) {
	// // this.addVertex(vertexNo, null);
	// // }
	//
	// //MINE
	// public void addVertex(int vertexNo, float x, float y, float z) {
	// this.vertices[vertexNo] = new Vertex(vertexNo,x,y,z);
	// this.totalVertices++;
	// }
	//
	// //add a uni-directional edge with cost
	// public void addEdge(int fromVertexNo, int toVertexNo, int cost) {
	// this.adjMatrix[fromVertexNo][toVertexNo] = cost;
	// this.adjacentVertCount[fromVertexNo]++;
	// }
	//
	// //add a uni-directional edge with cost
	// public void addEdge(Vertex fromVertex, Vertex toVertex, int cost) {
	// this.addEdge(fromVertex.getVertexNo(), toVertex.getVertexNo(), cost);
	// }
	//
	// //add a uni-directional edge
	// public void addEdge(int fromVertexNo, int toVertexNo) {
	// this.addEdge(fromVertexNo, toVertexNo, this.DEFAULT_EDGE_COST);
	// }
	//
	// //add a uni-directional edge
	// public void addEdge(Vertex fromVertex, Vertex toVertex) {
	// this.addEdge(fromVertex.getVertexNo(), toVertex.getVertexNo());
	// }
	//
	// //add a bi-directional edge with cost
	// public void addBidirectionalEdge(int vertex1, int vertex2, int cost) {
	// this.addEdge(vertex1, vertex2, cost);
	// this.addEdge(vertex2, vertex1, cost);
	// }
	//
	// //add a bi-directional edge with cost
	// public void addBidirectionalEdge(Vertex v1, Vertex v2, int cost) {
	// this.addBidirectionalEdge(v1.getVertexNo(), v2.getVertexNo(), cost);
	// }
	//
	// //add a bi-directional edge
	// public void addBidirectionalEdge(int vertex1, int vertex2) {
	// this.addBidirectionalEdge(vertex1, vertex2, this.DEFAULT_EDGE_COST);
	// }
	//
	// //add a bi-directional edge
	// public void addBidirectionalEdge(Vertex v1, Vertex v2) {
	// this.addBidirectionalEdge(v1.getVertexNo(), v2.getVertexNo());
	// }
	//
	// //get the total vertices count in the graph
	// public int getTotalNumberOfVertices() {
	// return this.totalVertices;
	// }
	//
	// ///mark a vertex as visited
	// public void visitVertex(int vertexNo) {
	// this.vertices[vertexNo].visit();
	// }
	//
	// //gets if a vertex is visited
	// public boolean isVisited(int vertexNo) {
	// return this.vertices[vertexNo].isVisited();
	// }
	//
	// //get a vertex from a vertexNo
	// public Vertex getVertex(int vertexNo) {
	// return this.vertices[vertexNo];
	// }
	//
	// //gets a vertex data from vertexNo
	// public Object getVertexData(int vertexNo) {
	// return this.vertices[vertexNo].getData();
	// }
	//
	// //get adjacent vertex numbers for a given vertexNo
	// public int[] getAdjacentVertexNumbers(int vertexNo) {
	// int[] ret = new int[this.adjacentVertCount[vertexNo]];
	// int index = 0;
	//
	// for (int i=0; i<this.adjMatrix[vertexNo].length; i++) {
	// if (this.adjMatrix[vertexNo][i] >= 0) {
	// ret[index++] = i;
	// }
	// }
	//
	// return ret;
	// }
	//
	// //get adjacent vertex numbers for a given vertex
	// public int[] getAdjacentVertexNumbers(Vertex vert) {
	// return this.getAdjacentVertexNumbers(vert.getVertexNo());
	// }
	//
	// //get adjacent vertices for a given vertexNo
	// public Vertex[] getAdjacentVertices(int vertexNo) {
	// Vertex[] ret = new Vertex[this.adjacentVertCount[vertexNo]];
	// int index = 0;
	//
	// for (int i=0; i<this.adjMatrix[vertexNo].length; i++) {
	// if (this.adjMatrix[vertexNo][i] >= 0) {
	// ret[index++] = this.vertices[i];
	// }
	// }
	//
	// return ret;
	// }
	//
	// //get adjacent vertices for a given vertex
	// public Vertex[] getAdjacentVertices(Vertex vert) {
	// return this.getAdjacentVertices(vert.getVertexNo());
	// }
	//
	// //gets the edge/path cost from adjacency list for two given vertexNo
	// public int getEdgeCost(int fromVertNo, int toVertNo) {
	// return this.adjMatrix[fromVertNo][toVertNo];
	// }
	//
	// //gets the edge/path cost from adjacency list for two given vertices
	// public int getEdgeCost(Vertex fromVert, Vertex toVert) {
	// return this.getEdgeCost(fromVert.getVertexNo(), toVert.getVertexNo());
	// }
	//
	// //gets all vertices
	// public Vertex[] getVertices() {
	// return this.vertices;
	// }
	//
	// //returns all the edges of the graph
	// //needed for edge traversing algorithms
	// public Edge[] getAllEdges() {
	// int totalEdges = 0;
	// for (int i=0; i<this.adjacentVertCount.length; i++) {
	// totalEdges += this.adjacentVertCount[i];
	//
	// }
	//
	// Edge[] edges = new Edge[totalEdges];
	// int index = 0;
	// for (int i=0; i<this.vertices.length; i++) {
	// for (int j=0; j<this.vertices.length; j++) {
	// if (this.adjMatrix[i][j] >= 0) {
	// edges[index++] = new Edge(this.vertices[i], this.vertices[j],
	// this.adjMatrix[i][j]);
	//
	// }
	// }
	// }
	//
	// return edges;
	// }
	//
	// public Edge[] getAllBidirectionalEdges() {
	// int totalEdges = 0;
	// for (int i=0; i<this.adjacentVertCount.length; i++) {
	// totalEdges += this.adjacentVertCount[i];
	// }
	// totalEdges /= 2;
	//
	// Edge[] edges = new Edge[totalEdges];
	// int index = 0;
	// for (int i=0; i<this.vertices.length; i++) {
	// for (int j=i+1; j<this.vertices.length; j++) {
	// if (this.adjMatrix[i][j] >= 0) {
	// edges[index++] = new Edge(this.vertices[i], this.vertices[j],
	// this.adjMatrix[i][j]);
	// }
	// }
	// }
	//
	// return edges;
	// }
	// }

	// class Vertex {
	// private int vertexNo; //vertex number, starting from 0
	// private boolean visited;
	// private Object data = null; //can store any data associate with this
	// vertes
	// private float x,y,z;
	//
	// //public Vertex(int vertexNo, Object data) {
	// public Vertex(int vertexNo, float x, float y, float z) {
	// this.vertexNo = vertexNo;
	// this.visited = false;
	// this.data = data;
	//
	// this.x = x;
	// this.y = y;
	// this.z = z;
	// }
	// // public Vertex(int vertexNo) {
	// // this(vertexNo, null);
	// // }
	//
	// public void visit() {
	// this.visited = true;
	// }
	// public boolean isVisited() {
	// return this.visited;
	// }
	// public int getVertexNo() {
	// return this.vertexNo;
	// }
	// public Object getData() {
	// return this.data;
	// }
	//
	// public float getX() {
	// return this.x;
	// }
	//
	// public float getY() {
	// return this.y;
	// }
	//
	// public float getZ() {
	// return this.z;
	// }
	//
	//
	// }

} // end class
