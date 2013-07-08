package popcad_v1;

import java.util.ArrayList;

import newhull.Point3d;

import processing.core.PApplet;
import processing.core.PConstants;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;

// --------------------------NAV3D----------------------------------//

// utility class for controlling 3D camera. supports rotating
// by dragging the mouse,panning with shift-click and zooming
// with the mouse wheel.

public class Nav3D {
	float rotX, rotY;
	float tx, ty, tz;
	Vec2D[] mouseOverVectors; // for keeping track of mouseover
	int vertexMouseOver = -1;
	boolean mouseOver = false;
	float x, y; // for hit detection screenX and screenY positions
	PApplet p;
	PopCAD_v1 u;
	
	Nav3D(PApplet parent) {
		p = parent;
		
	}

	void transform() {
		p.translate(p.width / 2, p.height / 2);
		p.translate(tx, ty, tz);
		p.rotateY(rotY);
		p.rotateX(rotX);
	}

	public void mouseReleased() {
		vertexMouseOver = -1;
	}
	
	
	// Function for detecting if mouse is over an active vertex
//		public void hitDetection() {
//
//			for (int i = 0; i < u.vectors.size(); i++) {
//
//				x = p.screenX(u.vectors.get(i).x, u.vectors.get(i).y,
//						u.vectors.get(i).z);
//				y = p.screenY(u.vectors.get(i).x, u.vectors.get(i).y,
//						u.vectors.get(i).z);
//				p.println(x + " " + y);
//				Vec2D v2d = new Vec2D(x, y);
//
//				mouseOverVectors = (Vec2D[]) PApplet.append(mouseOverVectors, v2d);
//
//				if (x > p.mouseX - 3 && x < p.mouseX + 3 && y > p.mouseY - 3
//						&& y < p.mouseY + 3) {
//					vertexMouseOver = i;
//				}
//			}
//		}
	
		
		public void hitDetection(ArrayList<Vec3D> vectors) {
			// TODO Auto-generated method stub
			for (int i = 0; i < vectors.size(); i++) {

				x = p.screenX(vectors.get(i).x, vectors.get(i).y,
						vectors.get(i).z);
				y = p.screenY(vectors.get(i).x, vectors.get(i).y,
						vectors.get(i).z);
				//p.println(x + " " + y);
				Vec2D v2d = new Vec2D(x, y);

				mouseOverVectors = (Vec2D[]) PApplet.append(mouseOverVectors, v2d);

				if (x > p.mouseX - 3 && x < p.mouseX + 3 && y > p.mouseY - 3
						&& y < p.mouseY + 3) {
					vertexMouseOver = i;
					
					PApplet.println("hit" + x + " " + y);
					//p.ellipse(p.screenX(vectors.get(vertexMouseOver).x, vectors.get(vertexMouseOver).y), p.screenY(vectors.get(vertexMouseOver).x, vectors.get(vertexMouseOver).y), 50, 50);

				}
			}
		
		}	

	public void mouseDragged() {

		// if edit mode is on, and the mouse is over a point, do stuff
		if (mouseOver == true && vertexMouseOver != -1) {

			PApplet.println("mouseOver: " + mouseOver);
			PApplet.println(mouseOverVectors[vertexMouseOver]);
			

			//u.vectors.get(vertexMouseOver).x = p.mouseX - p.width / 2;
			//u.vectors.get(vertexMouseOver).y = p.mouseY - p.height / 2;

			//u.reDraw = true;
			//u.drawHull();
		}

		else if (mouseOver == false) {
			// calculate rotX and rotY by the relative change
			// in mouse position
			if (p.keyEvent != null && p.keyEvent.isShiftDown()) {
				tx += PApplet.radians(p.mouseX - p.pmouseX) * 10;
				ty += PApplet.radians(p.mouseY - p.pmouseY) * 10;

			} else {
				rotY += PApplet.radians(p.mouseX - p.pmouseX);
				rotX -= PApplet.radians(p.mouseY - p.pmouseY);

			}
		}
	}
	
	public void mouseDragged(ArrayList<Vec3D> vectors, Point3d[] points, int activeColor) {
		// TODO Auto-generated method stub
		// if edit mode is on, and the mouse is over a point, do stuff
				if (mouseOver == true && vertexMouseOver != -1) {

					PApplet.println("mouseOver: " + mouseOver);
					PApplet.println(mouseOverVectors[vertexMouseOver]);

					//vectors.get(vertexMouseOver).x = p.mouseX - p.width / 2;
					//vectors.get(vertexMouseOver).y = p.mouseY - p.height / 2;
					
					
//					vectors.get(vertexMouseOver).x = (p.mouseX - (p.width/2))/2;
//					vectors.get(vertexMouseOver).y = (p.mouseY - (p.height/2))/2;
					
					//vectors.get(vertexMouseOver).x += p.mouseX;
					//vectors.get(vertexMouseOver).y += p.mouseY;
					
					vectors.get(vertexMouseOver).x += PApplet.radians(p.mouseX - p.pmouseX) * 40; 
					vectors.get(vertexMouseOver).y += PApplet.radians(p.mouseY - p.pmouseY) * 40;
					
					
					//u.reDraw = true;
					//u.drawHull(vectors, points, activeColor);
					//u.drawHull();
				}

				else if (mouseOver == false) {
					// calculate rotX and rotY by the relative change
					// in mouse position
					if (p.keyEvent != null && p.keyEvent.isShiftDown()) {
						tx += PApplet.radians(p.mouseX - p.pmouseX) * 10;
						ty += PApplet.radians(p.mouseY - p.pmouseY) * 10;

					} else {
						rotY += PApplet.radians(p.mouseX - p.pmouseX);
						rotX -= PApplet.radians(p.mouseY - p.pmouseY);

					}
				}
	}

	public void keyPressed() {
		if (p.key == PConstants.CODED) {
			// check to see if CTRL is pressed
			if (p.keyEvent.isControlDown()) {
				// do zoom in the Z axis
				if (p.keyCode == PConstants.UP)
					tz = tz + 2;
				if (p.keyCode == PConstants.DOWN)
					tz = tz - 2;
			}
			// check to see if SHIFT is pressed
			else if (p.keyEvent.isShiftDown()) {
				// do translations in X and Y axis
				if (p.keyCode == PConstants.UP)
					ty = ty - 2;
				if (p.keyCode == PConstants.DOWN)
					ty = ty + 2;
				if (p.keyCode == PConstants.RIGHT)
					tx = tx + 2;
				if (p.keyCode == PConstants.LEFT)
					tx = tx - 2;
			} else {
				// do rotations around X and Y axis
				if (p.keyCode == PConstants.UP)
					rotX = rotX + PApplet.radians(2);
				if (p.keyCode == PConstants.DOWN)
					rotX = rotX - PApplet.radians(2);
				if (p.keyCode == PConstants.RIGHT)
					rotY = rotY + PApplet.radians(2);
				if (p.keyCode == PConstants.LEFT)
					rotY = rotY - PApplet.radians(2);
			}
		} else {
			if (p.keyEvent.isControlDown()) {
				if (p.keyCode == 'R') {
					PApplet.println("Reset transformations.");
					tx = 0;
					ty = 0;
					tz = 0;
					rotX = 0;
					rotY = 0;
				}
			}
		}
	}





	

	// void mouseWheelMoved(float step) {
	// tz=tz+step*15;
	// }
}
