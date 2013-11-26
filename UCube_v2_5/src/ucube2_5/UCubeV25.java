package ucube2_5;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import newhull.*;
//import java.awt.event.*;
import toxi.geom.*;
import toxi.geom.mesh.*;
import toxi.processing.*;
import controlP5.*;
import processing.serial.*;
import processing.opengl.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

public class UCubeV25 extends PApplet {

	// Note: For every new project in eclipse using the serial library, you'll
	// have to add the -d32 argument under vm run settings
	// This version implements dynamic knot and mst builders, wireframe

	String version = "SnapCAD v.025";
	ControlP5 controlP5;
	CheckBox checkBox;

	// GUI gui = new GUI(this);
	Nav3D nav = new Nav3D(this); // camera controller
	HullBuilder hb = new HullBuilder(this);
	ToxiclibsSupport gfx;
	
	// regular hull (all points)
	QuickHull3D hull = new QuickHull3D(); // init quickhull for hull
	Point3d[] points; // init Point3d array
	Point3d[] savedPoints = new Point3d[0];
	Mesh3D mesh = new TriangleMesh(); // triangle mesh for convex hull
	// Vec3D[] vectors;
	ArrayList<Vec3D> vectors = new ArrayList<Vec3D>();
	ArrayList<Vec3D> savedVectors = new ArrayList<Vec3D>();

	// knots
	QuickHull3D knotHull = new QuickHull3D(); // init quickhull for knot
	Mesh3D knotMesh = new TriangleMesh(); // trianglemesh for knot
	ArrayList<Vec3D> knotVectors = new ArrayList<Vec3D>();
	Vec3D[] kVectors = new Vec3D[0]; // collection of all knot vectors
	Point3d[] knotPoints = new Point3d[0]; // knot points (cubes around each
											// point)
	//Point3d[] kSavedPoints = new Point3d[0];// saved points for knot

	// how thick your path is
	int offset = 10; 

	// minimal spanning tree
	QuickHull3D mstHull = new QuickHull3D(); // init quickhull for mst
	ArrayList<Vec3D> mstVectors = new ArrayList<Vec3D>();
	Mesh3D mstMesh = new TriangleMesh();
	Point3d[] mstPoints = new Point3d[0];
	Vec3D[] mVectors = new Vec3D[0];
	
	boolean readSerial = true;

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

	boolean doHull = false;
	boolean doKnot = false;
	boolean doMst = false;
	boolean reDrawMst = false;
	PFont myFont; // init font for text

	Vec3D[] masterVectArray = new Vec3D[0];
	Point3d[] masterPointArray = new Point3d[0];

	int grey = color(100);
	int red = color(200, 0, 0);
	int blue = color(0, 0, 200);
	int activeColor = grey;

	// spanning tree
	int maximumVertices;
	Graph g;
	List<Edge> mst;

	@Override
	public void setup() {
		// List all the available serial ports
		// println(Serial.list());

		size(1440, 835, OPENGL);
		frameRate(10);
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
		
		if (doGrid == true) {
			drawGrid(); // draw grid
		}


		while (myPort.available() > 0) {

			inString = myPort.readStringUntil('\n');

			if (inString != null) {

				inString = trim(inString);
				//println(inString);

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
					// stroke(100);

					//inString = inString +
					//"1000000010001000100010001000010010001000100110001001000010100000100001001000100010000000100010001000100010000100100010001001100010010000101000001000010010001000";

					// compare inString to oldString to see if coords changed
					if (inString.equals(oldString)) {
						// do nothing
						// println(inString);
					} else {
						// if we're not in edit mode, update the input
						// coordinates and redraw the hull
						if (readSerial == true) {
							oldString = inString;
							
							//println(inString);
							// reDraw = true;
						}
						
						sortString();


//						int counter = 0;
//						// vectors = new Vec3D[0];
//						vectors.clear();
//						points = new Point3d[0];
//						
//						savedVectors.clear();
//						savedPoints = new Point3d[0];
//						//println("cleared");
//
//						kVectors = null;
//						kVectors = new Vec3D[0];
//
//						mVectors = null;
//						mVectors = new Vec3D[0];
//
//						for (int i = 0; i < inString.length(); i++) {
//							//println("1: " + inString.length());
//							char bit = inString.charAt(i);
//
//							// throw away leading bit from each shift register
//							// (we're only using 7 of the 8 bits)
//							if (i == 0 || i % 8 == 0) {
//								inString.replace(bit, ' ');
//							} else {
//								// if the bit == 1, an led is plugged into that
//								// space, so look up it's coordinate
//								if (bit == '1') {
//
//									vectors.add(masterVectArray[counter]);
//									points = (Point3d[]) append(points,
//											masterPointArray[counter]);
//									
//									savedVectors.add(masterVectArray[counter]);
//									savedPoints = (Point3d[]) append(savedPoints,
//											masterPointArray[counter]);
//									println("bit = 1 added");
//
//									if (knotVectors
//											.contains(masterVectArray[counter])) {
//
//									} else {
//										knotVectors
//												.add(masterVectArray[counter]);
//										// println(masterVectArray[counter]
//										// + " true");
//										reDrawKnot = true;
//										knotMesh.clear();
//									}
//
//									if (mstVectors
//											.contains(masterVectArray[counter])) {
//
//									} else {
//										mstVectors
//												.add(masterVectArray[counter]);
//										reDrawMst = true;
//										mstMesh.clear();
//									}
//
//								}
//
//								if (bit == '0') {
//
//									if (knotVectors
//											.contains(masterVectArray[counter])) {
//										knotVectors
//												.remove(masterVectArray[counter]);
//										reDrawKnot = true;
//										knotMesh.clear();
//										// println("removed");
//									}
//
//									if (mstVectors
//											.contains(masterVectArray[counter])) {
//										mstVectors
//												.remove(masterVectArray[counter]);
//										reDrawMst = true;
//										mstMesh.clear();
//									}
//								}
//
//								
//								counter++;
//							}
//							
//						}
						
						
						//savedVectors = (ArrayList<Vec3D>) vectors.clone();
						//println("cloned");
						
					}
					

					if (vectors.size() > 0) {
						stroke(grey);
						if (doFill) {
							fill(200);
						} else {
							noFill();
						}
						for (int j = 0; j < vectors.size(); j++) {
							// println(vectors[j]);
							float x = vectors.get(j).x;
							float y = vectors.get(j).y;
							float z = vectors.get(j).z;
							point(x, y, z);
						}
						
						
						
					}
					// call drawHull function if Hull mode is active
					// need to change point array for edit mode to work?
					if (doHull == true) {
						drawHull(vectors, points, grey);
						

					}

					if (doKnot == true) {

						if (reDrawKnot == true) {
							println("redrawknot");

							drawKnot();
						}
						// stroke(grey);
						strokeWeight(1);
						if (doFill) {
							fill(200);
						} else {
							noFill();
						}

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
						if (reDrawMst == true) {
							drawMst();
						}
						strokeWeight(1);
						if (doFill) {
							fill(200);
						} else {
							noFill();
						}
						beginShape(TRIANGLES);

						for (int i1 = 0; i1 < mVectors.length; i1 += 3) {

							vertex(mVectors[i1]);
							vertex(mVectors[i1 + 1]);
							vertex(mVectors[i1 + 2]);
						}
						endShape();
					}

					// TODO: Edit mode
					// if we're in edit mode, enable rollover detection for
					// vertices
					if (readSerial == false) {
						nav.hitDetection(vectors);
					}
				}
			}
		}

		drawAxes();
		popMatrix();

		// turn off depth test so the controlP5 GUI draws correctly
		hint(DISABLE_DEPTH_TEST);

		// if in edit mode, show pop-up
		if (readSerial == false) {
			// myPort.stop();
			textSize(14);
			text("Edit Mode On", 100, 575, 0);
		}
		// do text cues after popMatrix so it doesn't rotate
		doMouseOvers(); // text cues on button rollover

		// ask for another reading
		myPort.write("A"); 
	}// end draw

	public void initCoordArray() {

		masterVectArray = new Vec3D[0];
		masterPointArray = new Point3d[0];
		
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
				}
			}
		}
	}

	// --------------------------GUI----------------------------------//

	// Initialize buttons and NAV3D class
	public void initControllers() {
		
		int buttonWidth = 130;
		int buttonHeight = 29;
		int bSpacing = 30;
		int start = 100;

		controlP5 = new ControlP5(this);
		controlP5.setColorBackground(50);

		controlP5.addButton("Hull", 0, 100, start, buttonWidth, buttonHeight);
		controlP5.addButton("Path", 0, 100, start + bSpacing, buttonWidth, buttonHeight);
		controlP5.addButton("Tree", 0, 100, start + bSpacing *2, buttonWidth, buttonHeight);
		
		controlP5.addButton("Export", 0, 100, start + bSpacing *4, buttonWidth, buttonHeight);
		controlP5.addButton("ClosePath", 0, 100, start + bSpacing *5, buttonWidth, buttonHeight);
		
		controlP5.addButton("Edit", 0, 100, start + bSpacing *7, buttonWidth, buttonHeight);
		controlP5.addButton("Reset", 0, 100, start + bSpacing *8, buttonWidth, buttonHeight);

		controlP5.addButton("WireFrame", 0, 100, start + bSpacing *10, buttonWidth, buttonHeight);
		controlP5.addButton("Grid", 0, 100, start + bSpacing *11, buttonWidth, buttonHeight);

//		Slider s = controlP5.addSlider("slider", 5, spacing/2, 10, 100, start + spacing *12, buttonWidth,
//				buttonHeight);
		
		Slider s = controlP5.addSlider("slider", 3, spacing, 10, 150, start + bSpacing *12, buttonWidth-50,
				buttonHeight);
		
		controlP5.Label label = s.captionLabel();
		label.style().marginLeft = -133;
		s.setColorLabel(50);
		s.setLabel("path width");
	}

	public void slider(int theWidth) {
		offset = theWidth;
		println(offset);
		if(doKnot==true) {
			drawKnot();
		}
		if(doMst==true) {
			drawMst();
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
			message = "Toggles wireframe.";
		}

		if (controlP5.controller("Grid").isInside()) {
			message = "Turns the background grid on or off.";
		}

		if (controlP5.controller("Export").isInside()) {
			message = "Exports your shape as an .stl file for 3D printing.";
		}


		if (controlP5.controller("Edit").isInside()) {
			message = "Turns on edit mode, where you can click and drag points to alter the shape.";
		}
		
		if (controlP5.controller("Reset").isInside()) {
			message = "Resets edited points back to the default grid.";
		}

		if (controlP5.controller("Path").isInside()) {
			message = "Create a sequential path.";
		}

		if (controlP5.controller("ClosePath").isInside()) {
			message = "Closes your path.";
		}

		if (controlP5.controller("slider").isInside()) {
			message = "Changes the thickness of your path or tree";
		}

		if (controlP5.controller("Tree").isInside()) {
			message = "Makes a minimal spaninng tree.";
		}


		if (message != null && controlP5.window(this).isMouseOver()) {
			textSize(14);
			text(message, 100, 600, 0);
		}
	}

	// pass mouse and key events to our Nav3D instance
	@Override
	public void mouseDragged() {
		// ignore mouse event if cursor is over controlP5 GUI elements
		if (controlP5.window(this).isMouseOver())
			return;

		if (nav.mouseOver == true && nav.vertexMouseOver != -1) {
			if (activeColor == grey) {
				//ellipse(screenX(vectors.get(nav.vertexMouseOver).x/4, vectors.get(nav.vertexMouseOver).y), screenY(vectors.get(nav.vertexMouseOver).x, vectors.get(nav.vertexMouseOver).y), 50, 50);
				//editShape(vectors, points);
				editShape(vectors, points);
			}
	
		} else {
			nav.mouseDragged();
		}
	}
 
	//TODO:make editing and reset work
	public void editShape(ArrayList<Vec3D> vectors, Point3d[] points) {
	//public void editShape() {
		
		
		
		println(vectors.subList(0, 5));
	    println(savedVectors.subList(0,5));
	    
	    //println(points[2].toString());
	   // println(savedPoints[2].toString());
	    
	    println(nav.vertexMouseOver);

		vectors.get(nav.vertexMouseOver).x += PApplet.radians(mouseX - pmouseX) * 40;
		vectors.get(nav.vertexMouseOver).y += PApplet.radians(mouseY - pmouseY) * 40;

		//println(vectors.get(nav.vertexMouseOver).x + " " + savedVectors.get(nav.vertexMouseOver).x);

		points[nav.vertexMouseOver].x += PApplet.radians(mouseX - pmouseX) * 40;
		points[nav.vertexMouseOver].y += PApplet.radians(mouseY - pmouseY) * 40;	
		
		reDrawMst = true;
		reDrawKnot = true;
		
		for(int i = 0; i < masterVectArray.length; i++) {
			//println(masterVectArray[i]);
		}

		
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
		stroke(150, 0, 20);
		line(-20, 20, 0, 800, 20, 0);
		fill(150, 0, 20);
		text("X", 260, 20);
		stroke(0, 150, 0);
		line(-20, 20, 0, -20, -500, 0);
		fill(0, 150, 0);
		text("Y", -20, -260);
		stroke(0, 0, 150);
		line(-20, 20, 0, -20, 20, 800);
		fill(0, 0, 150);
		text("Z", -50, 20, 260);
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

	// toggle wireframe
	public void WireFrame(int theValue) {
		if (doFill == true) {
			// doHull = true;
			doFill = false;
		} else if (doFill == false) {
			// doHull = true;
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
	//TODO: is this the best way?
	public void Reset(int theValue) {
		
//		println(vectors.subList(0, 5));
//	    println(savedVectors.subList(0,5));
		
		initCoordArray();
//		for(int i = 0; i < masterVectArray.length; i++) {
//			println(masterVectArray[i]);
//		}
		
		sortString();
		//println(vectors.size());
		
	}
	
	
	public void sortString() {
		
		int counter = 0;
		// vectors = new Vec3D[0];
		vectors.clear();
		points = new Point3d[0];
		
		savedVectors.clear();
		savedPoints = new Point3d[0];
		//println("cleared");

		kVectors = null;
		kVectors = new Vec3D[0];
		knotPoints = new Point3d[0];
		knotVectors.clear();

		mVectors = null;
		mVectors = new Vec3D[0];
		mstVectors.clear();
		mstPoints = new Point3d[0];
		
		
		for (int i = 0; i < inString.length(); i++) {
			//println("1: " + inString.length());
			char bit = inString.charAt(i);

			// throw away leading bit from each shift register
			// (we're only using 7 of the 8 bits)
			if (i == 0 || i % 8 == 0) {
				inString.replace(bit, ' ');
			} else {
				// if the bit == 1, an led is plugged into that
				// space, so look up it's coordinate
				if (bit == '1') {

					vectors.add(masterVectArray[counter]);
					points = (Point3d[]) append(points,
							masterPointArray[counter]);
					
					savedVectors.add(masterVectArray[counter]);
					savedPoints = (Point3d[]) append(savedPoints,
							masterPointArray[counter]);
					println("bit = 1 added");

					if (knotVectors
							.contains(masterVectArray[counter])) {

					} else {
						knotVectors
								.add(masterVectArray[counter]);
						// println(masterVectArray[counter]
						// + " true");
						reDrawKnot = true;
						knotMesh.clear();
					}

					if (mstVectors
							.contains(masterVectArray[counter])) {

					} else {
						mstVectors
								.add(masterVectArray[counter]);
						reDrawMst = true;
						mstMesh.clear();
					}

				}

				if (bit == '0') {

					if (knotVectors
							.contains(masterVectArray[counter])) {
						knotVectors
								.remove(masterVectArray[counter]);
						reDrawKnot = true;
						knotMesh.clear();
						// println("removed");
					}

					if (mstVectors
							.contains(masterVectArray[counter])) {
						mstVectors
								.remove(masterVectArray[counter]);
						reDrawMst = true;
						mstMesh.clear();
					}
				}

				
				counter++;
			}
			
		}
		
		reDrawKnot = true;
		reDrawMst = true;
		
		
	}
	

	// toggle knot
	public void Path(int theValue) {
		if (doKnot == true) {
			doKnot = false;
		} else if (doKnot == false) {
			doKnot = true;
			kVectors = new Vec3D[0];
			drawKnot();
		}
	}

	// close last segment of Knot
	public void ClosePath(int theValue) {
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
	
	public void clearTree() {
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

	// EXPORT ALL THE THINGS
	public void Export(int theValue) {
		if(doHull==true) {
		drawHull(vectors, points, grey);
		outputSTL(hb.stlVectors, mesh);
		}
		
		if(doMst==true) {
			outputSTL(mVectors);
		}
		if(doKnot==true) {
			outputSTL(kVectors);
		}
	}
	
	public String getDate() {
		
		String timeStamp;
		
		Integer d = day();
		Integer m = month();
		Integer min = minute();
		
		String desktop = "/users/Ben/Desktop/";
		
		timeStamp = desktop + "myShape_" + m.toString() + d.toString() + min.toString() + ".stl";
		
		message = "Export Success!";
		
		return timeStamp;
	}
	
	//ONE EXPORT TO RULE THEM ALL!
	public void outputSTL(Vec3D[] vectors) {
		
		mesh.clear();
		TriangleMesh mySTL = new TriangleMesh();

		for (int i = 0; i < vectors.length; i += 3) {
			mesh.addFace(vectors[i], vectors[i + 1], vectors[i + 2]);
		}

		mesh.flipYAxis();
		mesh.flipVertexOrder();
		mySTL.addMesh(mesh);
		//mySTL.saveAsSTL(selectOutput());
		mySTL.saveAsSTL(getDate());
		
	}
	
	public void outputSTL(ArrayList<Vec3D> vectors, Mesh3D mesh) {

		mesh.clear();
		TriangleMesh mySTL = new TriangleMesh();

		for (int i = 0; i < vectors.size(); i += 3) {
			mesh.addFace(vectors.get(i), vectors.get(i + 1), vectors.get(i + 2));
		}

		mesh.flipYAxis();
		mySTL.addMesh(mesh);
		mySTL.saveAsSTL(getDate());
	}


	// --------------------------KNOT
	// FUNCTIONS----------------------------------//

	// TODO: Make this dynamically update when in knot mode and coord array
	// changes
	public void drawKnot() {

		kVectors = new Vec3D[0];
		strokeWeight(1);
		fill(200);
		println(offset);

		for (int i = 0; i < knotVectors.size() - 1; i++) {

			Vec3D vec = knotVectors.get(i);
			float x = vec.x;
			float y = vec.y;
			float z = vec.z;

			Vec3D vec2 = knotVectors.get(i + 1);
			float x2 = vec2.x;
			float y2 = vec2.y;
			float z2 = vec2.z;

			line(x, y, z, x2, y2, z2);

		}

		for (int i = 0; i < knotVectors.size() - 1; i++) {

			Vec3D vec = knotVectors.get(i);
			float x = vec.x;
			float y = vec.y;
			float z = vec.z;

			Vec3D vec2 = knotVectors.get(i + 1);
			float x2 = vec2.x;
			float y2 = vec2.y;
			float z2 = vec2.z;

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

		Vec3D vec = knotVectors.get(0);
		float x = vec.x;
		float y = vec.y;
		float z = vec.z;

		Vec3D vec2 = knotVectors.get(i - 1);
		float x2 = vec2.x;
		float y2 = vec2.y;
		float z2 = vec2.z;

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
					Vec3D tempVect = new Vec3D(x, y, z);
					//kSavedPoints = (Point3d[]) append(kSavedPoints, pnt2);
					kVectors = (Vec3D[]) append(kVectors, tempVect);
				}
			}
			reDrawKnot = false;
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

	// --------------------------NEWHULL: Convex Hull
	// Functions----------------------------------//

	public void initPoints(ArrayList<Vec3D> vectors, Point3d[] points) {

		points = new Point3d[0];

		if (vectors.size() > 0) {

			for (int i = 0; i < vectors.size(); i++) {

				float x = vectors.get(i).x;
				float y = vectors.get(i).y;
				float z = vectors.get(i).z;

				Point3d tempPnt = new Point3d(x, y, z);
				points = (Point3d[]) append(points, tempPnt);
			}
		}
	}

	public void drawHull(ArrayList<Vec3D> vectors, Point3d[] points, int color) {

		initPoints(vectors, points);

		hb.reDraw = true;
		hb.makeHull(points);

		if (hb.hull.myCheck(points, points.length) == false) {

			beginShape(TRIANGLE_STRIP);
			strokeWeight(1);
			stroke(color);
			
			if (doFill) {
				fill(color);
			} else {
				noFill();
			}

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
			if (doFill) {
				fill(color);
			} else {
				noFill();
			}
			beginShape(TRIANGLES);

			for (int i1 = 0; i1 < hb.vectors.size(); i1 += 3) {
				
				vertex(hb.vectors.get(i1));
				vertex(hb.vectors.get(i1 + 1));
				vertex(hb.vectors.get(i1 + 2));
			}
			endShape();
		}
	}

	// MINIMAL SPANNING TREE
	public void drawMst() {

		mVectors = new Vec3D[0];
		
		strokeWeight(1);
		if (doFill) {
			fill(200);
		} else {
			noFill();
		}
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
			}
		}

		Kruskal k = new Kruskal(g);
		mst = k.getMSTEdges();

		for (int i = 0; i < mst.size(); i++) {

			Edge e = mst.get(i);

			float x = e.getFrom().getX();
			float y = e.getFrom().getY();
			float z = e.getFrom().getZ();

			float x2 = e.getTo().getX();
			float y2 = e.getTo().getY();
			float z2 = e.getTo().getZ();

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
					Vec3D tempVect = new Vec3D(x, y, z);
					mVectors = (Vec3D[]) append(mVectors, tempVect);
				}
			}
			// endShape(CLOSE);
			reDrawMst = false;
			// println("false");
		}
	}

} // end class
