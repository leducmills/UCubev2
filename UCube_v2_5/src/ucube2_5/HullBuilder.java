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

			for (int j = 0; j < numPoints; j++) {

				float x = (float) points[j].x;
				float y = (float) points[j].y;
				float z = (float) points[j].z;
				//p.vertex(x, y, z);
				Vec3D tempVect = new Vec3D(x,y,z);
				vectors.add(tempVect);
			}			
			
			
		}

		else if (hull.myCheck(points, numPoints) == true) {

			if (reDraw == true) {
				// print(reDraw);
				hull.build(points);
				hull.triangulate();
				// get an array of the vertices so we can get the faces
				Point3d[] vertices = hull.getVertices();
				
				vectors.clear();
				stlVectors.clear();

				int[][] faceIndices = hull.getFaces();
				for (int i = 0; i < faceIndices.length; i++) {
					for (int k = 0; k < faceIndices[i].length; k++) {

						// get points that correspond to each face
						Point3d pnt2 = vertices[faceIndices[i][k]];
						float x = (float) pnt2.x;
						float y = (float) pnt2.y;
						float z = (float) pnt2.z;
		
						Vec3D tempVect = new Vec3D(x, y, z);
						vectors.add(tempVect);
						stlVectors.add(tempVect);
					}
				}
				
				reDraw = false;
			}

			else if (reDraw == false) {
				
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
