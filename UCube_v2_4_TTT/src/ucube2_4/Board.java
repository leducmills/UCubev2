package ucube2_4;

import java.io.IOException;

public class Board {
	// The board in a 3D tic-tac-toe game is a 3D array, board[3][3][3].
	// board[i][j][k] = 0: empty position;
	// board[i][j][k] = 1: player 1's piece;
	// board[i][j][k] = 2: player 2's piece;
	private int[][][] board;

	// private static char[][] displayBoard;
	// private final int displayWidth = 29;
	// private final int displayHeight = 13;
	public final int boardSize = 11;

	public Board() {
		// initialize the board and displayBoard
		board = new int[boardSize][boardSize][boardSize];

	}

	public void reset() {
		for (int i = 0; i < 11; i++)
			for (int j = 0; j < 11; j++)
				for (int k = 0; k < 11; k++)
					board[i][j][k] = 0;
	}

	public void set(Position p, int v) throws IOException {
		if (board[p.x][p.y][p.z] != 0)
			throw new IOException("Position taken");
		board[p.x][p.y][p.z] = v;
	}

	public boolean threeInARow(Position p, int player) {

		// System.out.println("board: " + board[p.x][p.y][p.z]);
		// System.out.println("v: " + v);

		System.out.println("position: " + p.x + " " + p.y + " " + p.z);

		// return true if there is a three-in-a-row involving position p.
		if (board[p.x][p.y][p.z] != player)
			return false;

		// straight along any axis
		for (int i = 3; i < 10; i++) {

			if (board[i][p.y][p.z] == player
					&& board[i + 1][p.y][p.z] == player
					&& board[i + 2][p.y][p.z] == player) {
				System.out.println("true");
				return true;
			}
			if (board[p.x][i][p.z] == player
					&& board[p.x][i + 1][p.z] == player
					&& board[p.x][i + 2][p.z] == player) {
				System.out.println("true");
				return true;
			}
			if (board[p.x][p.y][i] == player
					&& board[p.x][p.y][i + 1] == player
					&& board[p.x][p.y][i + 2] == player) {
				System.out.println("true");
				return true;
			}

		}

		// // TODO: This is broken
		// if (p.x == 1 && p.y == 1 && p.z == 1) {
		// for (int i = 0; i < 3; i++)
		// for (int j = 0; j < 3; j++)
		// if (board[0][i][j] == player
		// && board[2][2 - i][2 - j] == player)
		// return true;
		// } else if (board[1][1][1] == player) {
		// if (board[2 - p.x][2 - p.y][2 - p.z] == player)
		// return true;
		// }

		for (int i = 3; i < 10; i++) {
			for (int j = 3; j < 10; j++) {
				for (int k = 3; k < 10; k++) {

					//down to the right on x
					if (board[i][j][k] == player
							&& board[i + 1][j - 1][k] == player
							&& board[i + 2][j - 2][k] == player) {
						System.out.println("true");
						return true;
					}

					// up to the right on x
					if (board[i][j][k] == player
							&& board[i + 1][j + 1][k] == player
							&& board[i + 2][j + 2][k] == player) {
						System.out.println("true");
						return true;
					}

					// back and up
					if (board[i][j][k] == player
							&& board[i][j + 1][k - 1] == player
							&& board[i][j + 2][k - 2] == player) {
						System.out.println("true");
						return true;
					}

					// back and down
					if (board[i][j][k] == player
							&& board[i][j - 1][k - 1] == player
							&& board[i][j - 2][k - 2] == player) {
						System.out.println("true");
						return true;
					}

					// diag back to right on y
					if (board[i][j][k] == player
							&& board[i + 1][j][k - 1] == player
							&& board[i + 2][j][k - 2] == player) {
						System.out.println("true");
						return true;
					}
					
					//diag back to left on y
					if (board[i][j][k] == player
							&& board[i - 1][j][k - 1] == player
							&& board[i - 2][j][k - 2] == player) {
						System.out.println("true");
						return true;
					}
					
					//down, back, and to the left
					if (board[i][j][k] == player
							&& board[i - 1][j - 1][k - 1] == player
							&& board[i - 2][j - 2][k - 2] == player) {
						System.out.println("true");
						return true;
					}
					
					//down, back, and to the right
					if (board[i][j][k] == player
							&& board[i + 1][j - 1][k - 1] == player
							&& board[i + 2][j - 2][k - 2] == player) {
						System.out.println("true");
						return true;
					}
					
					//up, back, and to the right
					if (board[i][j][k] == player
							&& board[i + 1][j + 1][k - 1] == player
							&& board[i + 2][j + 2][k - 2] == player) {
						System.out.println("true");
						return true;
					}
					
					//up, back, and to the left
					if (board[i][j][k] == player
							&& board[i - 1][j + 1][k - 1] == player
							&& board[i - 2][j + 2][k - 2] == player) {
						System.out.println("true");
						return true;
					}
					

				}
			}
		}

		return false;
	}

}
