package controller;


public class Score implements Comparable<Score> {
	private Player player;
	
	private int hits;
	private int sunken;
	private int damage;	
	private int death;
	
	private int score;
	
	
	
	public Score(Player player, int hits, int sunken, int damage,
			int death) {
		super();
		this.player = player;
		this.hits = hits;
		this.sunken = sunken;
		this.damage = damage;
		this.death = death;
		
		this.score = hits + (sunken * 5) - damage - (death * 10);
		
	}

	public String print() {
		return player.getDisplayName() + ": " + score + " (" + hits + ", " + sunken + ", " + damage + ", " + death + ")";
	}
	
	@Override
	public int compareTo(Score other) {
		
		if (score > other.score) {
			return 1;
		} else if (score < other.score){
			return -1;
		}
		
		if (death < other.death) {
			return 1;
		} else if (death > other.death) {
			return -1;
		}
		
		if (sunken > other.sunken) {
			return 1;
		} else if (sunken < other.sunken){
			return -1;
		}
		
		if (hits > other.hits) {
			return 1;
		} else if (hits < other.hits){
			return -1;
		}
		
		if (damage < other.damage) {
			return 1;
		} else if (damage > other.damage){
			return -1;
		}
		
		return 0;
	}

	public Player getPlayer() {
		return player;
	}

	public int getHits() {
		return hits;
	}

	public void setHits(int hits) {
		this.hits = hits;
	}

	public int getSunken() {
		return sunken;
	}

	public void setSunken(int sunken) {
		this.sunken = sunken;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public int getDeath() {
		return death;
	}

	public void setDeath(int death) {
		this.death = death;
	}	
	
	
	
}
