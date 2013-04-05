package ucube2_4;
import java.io.*;

public class Position {
	// Internal position in a 3D tic-tac-toe is defined to be a triple of  
	//    integers, (x, y, z), where 0 <= x, y, z <= 2. 
	// For the players, the position is xyz, where 1 <= x, y, z <= 3. 
	public int x, y, z;
	
	public Position(int a, int b, int c) throws IOException {
		x = a; y = b; z = c;
		if (badPosition()) throw new IOException("bad position");
	}
	
	public Position(int one) throws IOException {
		z = one%10 - 1;
		one /= 10;
		y = one%10 - 1;
		x = one/10 - 1;
		if (badPosition()) throw new IOException("bad position");
	}
	
	//changed 2's to 7's
	public boolean badPosition() {
	    if (x < 2 || x > 9) return true;
	    if (y < 2 || y > 9) return true;
	    if (z < 2 || z > 9) return true;
	    return false;
	}
	
	public String toString() {
		return " "+x+y+z;
	}
}
