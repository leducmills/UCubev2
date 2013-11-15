package ucube2_5;

import java.util.ArrayList;

import newhull.Point3d;
import newhull.QuickHull3D;
import processing.core.PApplet;
import toxi.geom.Vec3D;
import toxi.geom.mesh.Mesh3D;
import toxi.geom.mesh.TriangleMesh;

public class HullBuilder {

	PApplet p;
	
	QuickHull3D hull = new QuickHull3D();
	//Point3d[] points;
	Point3d[] savedPoints;
	Mesh3D mesh = new TriangleMesh();
	ArrayList<Vec3D> vectors = new ArrayList<Vec3D>();
	ArrayList<Vec3D> stlVectors = new ArrayList<Vec3D>();
	boolean doHull = false;
	boolean reDraw = true;
	
	
	HullBuilder(PApplet parent) {
		p = parent;
	}
	
	public void makeHull(Point3d[] points) {

		int numPoints = points.length;
		// check that our hull is valid
		// println(numPoints);

		if (hull.myCheck(points, numPoints) == false) {

			// brute force inefficiency
//			p.beginShape(p.TRIANGLE_STRIP);
//			p.strokeWeight(1);
//			p.fill(200);
//
			for (int j = 0; j < numPoints; j++) {

				float x = (float) points[j].x;
				float y = (float) points[j].y;
				float z = (float) points[j].z;
				//p.vertex(x, y, z);
				Vec3D tempVect = new Vec3D(x,y,z);
				vectors.add(tempVect);
			}
//
//			p.endShape(p.CLOSE);
			
			
			
		}

		else if (hull.myCheck(points, numPoints) == true) {

			if (reDraw == true) {
				// print(reDraw);
				hull.build(points);
				hull.triangulate();
				// get an array of the vertices so we can get the faces
				Point3d[] vertices = hull.getVertices();
				//savedPoints = new Point3d[0];
				vectors.clear();
				stlVectors.clear();
				//PApplet.println("stl clear");

//				p.beginShape(p.TRIANGLE_STRIP);
//				p.strokeWeight(1);
//				p.fill(200);
//				if (doFill == false) {
//					noFill();
//				}
				int[][] faceIndices = hull.getFaces();
				for (int i = 0; i < faceIndices.length; i++) {
					for (int k = 0; k < faceIndices[i].length; k++) {

						// get points that correspond to each face
						Point3d pnt2 = vertices[faceIndices[i][k]];
						float x = (float) pnt2.x;
						float y = (float) pnt2.y;
						float z = (float) pnt2.z;
						//p.vertex(x, y, z);
						Vec3D tempVect = new Vec3D(x, y, z);
						// println(x + "," + y + "," + z + " " + k);
						//savedPoints = (Point3d[]) PApplet.append(savedPoints, pnt2);
						//vectors = (Vec3D[]) append(vectors, tempVect);
						
						//need an if does not contain?
						vectors.add(tempVect);
						stlVectors.add(tempVect);
						// println(x + "," + y + "," + z);
					}
				}
				//p.endShape(p.CLOSE);
				reDraw = false;
			}

			else if (reDraw == false) {
				//PApplet.print(reDraw);
//				beginShape(TRIANGLE_STRIP);
//				strokeWeight(1);
//				fill(200);
//				if (doFill == false) {
//					noFill();
//				}
//				for (int i = 0; i < savedPoints.length; i++) {
//
//					float x = (float) savedPoints[i].x;
//					float y = (float) savedPoints[i].y;
//					float z = (float) savedPoints[i].z;
//					p.vertex(x, y, z);
//				}
//				p.endShape(p.CLOSE);
				
				for (int j = 0; j < numPoints; j++) {

					float x = (float) points[j].x;
					float y = (float) points[j].y;
					float z = (float) points[j].z;
					//p.vertex(x, y, z);
					Vec3D tempVect = new Vec3D(x,y,z);
					vectors.add(tempVect);
				}
				
				
			}
		}
		
	}
	
}
