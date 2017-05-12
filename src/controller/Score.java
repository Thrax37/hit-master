package controller;


public class Score implements Comparable<Score> {
	private Player player;

	public Score(Player player) {
		super();
		this.player = player;
	}
	
	public String print() {
		return player.getDisplayName() + " (" + ", " + ")";
	}
	
	@Override
	public int compareTo(Score other) {
			
		return 0;
	}

	public Player getPlayer() {
		return player;
	}	
	
}
