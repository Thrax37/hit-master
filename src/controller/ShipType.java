package controller;
public enum ShipType{
	
	CARRIER("Carrier", 5, 1, 1, 7, 3),
	BATTLESHIP("Battleship", 4, 1, 3, 7, 1),
	CRUISER("Cruiser", 3, 1, 2, 9, 2),
	SUBMARINE("Submarine", 3, 2, 1, 5, 4),
	DESTROYER("Destroyer", 2, 3, 1, 9, 4);
	
	private ShipType(String name, int length, int moves, int shots, int range, int mines) {
		this.name = name;
		this.length = length;
		this.moves = moves;
		this.shots = shots;
		this.range = range;
		this.mines = mines;
	}
	private final String name;
	private final int length;
	private final int moves;
	private final int shots;
	private final int range;
	private final int mines;
	
	public String getName() {
		return name;
	}

	public int getLength() {
		return length;
	}

	public int getMoves() {
		return moves;
	}

	public int getShots() {
		return shots;
	}

	public int getRange() {
		return range;
	}

	public int getMines() {
		return mines;
	}
	
	
}
