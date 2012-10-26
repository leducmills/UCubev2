package ucubev23;

import processing.core.PApplet;

class Edge {
	
	private Vertex fromVertex = null, toVertex = null;
	private float weight;
	PApplet p;

	public Edge(Vertex from, Vertex to, float weight) {
		this.fromVertex = from;
		this.toVertex = to;
		// this.weight = weight;
		this.weight = p.dist(fromVertex.getX(), fromVertex.getY(), fromVertex.getZ(),
				toVertex.getX(), toVertex.getY(), toVertex.getZ());
	}

	public Vertex getFrom() {
		return this.fromVertex;
	}

	public Vertex getTo() {
		return this.toVertex;
	}

	public float getWeight() {
		return this.weight;
	}

}
