package ucubev23;

import processing.core.PApplet;
import toxi.geom.Vec2D;

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
	UCubeV23 u;
	
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
		public void hitDetection() {

			for (int i = 0; i < u.vectors.size(); i++) {

				x = p.screenX((float) u.vectors.get(i).x, (float) u.vectors.get(i).y,
						(float) u.vectors.get(i).z);
				y = p.screenY((float) u.vectors.get(i).x, (float) u.vectors.get(i).y,
						(float) u.vectors.get(i).z);
				// println(x + " " + y);
				Vec2D v2d = new Vec2D(x, y);

				mouseOverVectors = (Vec2D[]) u.append(mouseOverVectors, v2d);

				if (x > p.mouseX - 3 && x < p.mouseX + 3 && y > p.mouseY - 3
						&& y < p.mouseY + 3) {
					vertexMouseOver = i;
				}
			}
		}
	

	public void mouseDragged() {

		// if edit mode is on, and the mouse is over a point, do stuff
		if (mouseOver == true && vertexMouseOver != -1) {

			p.println("mouseOver: " + mouseOver);
			p.println(mouseOverVectors[vertexMouseOver]);

			u.vectors.get(vertexMouseOver).x = p.mouseX - p.width / 2;
			u.vectors.get(vertexMouseOver).y = p.mouseY - p.height / 2;

			//u.reDraw = true;
			//u.drawHull();
		}

		else if (mouseOver == false) {
			// calculate rotX and rotY by the relative change
			// in mouse position
			if (p.keyEvent != null && p.keyEvent.isShiftDown()) {
				tx += p.radians(p.mouseX - p.pmouseX) * 10;
				ty += p.radians(p.mouseY - p.pmouseY) * 10;

			} else {
				rotY += p.radians(p.mouseX - p.pmouseX);
				rotX -= p.radians(p.mouseY - p.pmouseY);

			}
		}
	}

	public void keyPressed() {
		if (p.key == p.CODED) {
			// check to see if CTRL is pressed
			if (p.keyEvent.isControlDown()) {
				// do zoom in the Z axis
				if (p.keyCode == p.UP)
					tz = tz + 2;
				if (p.keyCode == p.DOWN)
					tz = tz - 2;
			}
			// check to see if SHIFT is pressed
			else if (p.keyEvent.isShiftDown()) {
				// do translations in X and Y axis
				if (p.keyCode == p.UP)
					ty = ty - 2;
				if (p.keyCode == p.DOWN)
					ty = ty + 2;
				if (p.keyCode == p.RIGHT)
					tx = tx + 2;
				if (p.keyCode == p.LEFT)
					tx = tx - 2;
			} else {
				// do rotations around X and Y axis
				if (p.keyCode == p.UP)
					rotX = rotX + p.radians(2);
				if (p.keyCode == p.DOWN)
					rotX = rotX - p.radians(2);
				if (p.keyCode == p.RIGHT)
					rotY = rotY + p.radians(2);
				if (p.keyCode == p.LEFT)
					rotY = rotY - p.radians(2);
			}
		} else {
			if (p.keyEvent.isControlDown()) {
				if (p.keyCode == 'R') {
					p.println("Reset transformations.");
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
