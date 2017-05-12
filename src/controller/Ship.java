package controller;
import java.util.Scanner;

public class Ship {
	private final int id;
	private Player owner;
	private ShipType shipType;
	
	
	private String[][] map;
	
	private Boolean[] hull;
	
	private Integer direction;
	private Position pos;
	
	private Integer actionsLeft = 0;
	private Integer movesLeft = 0;
	private Integer shotsLeft = 0;
	private Integer underwaterLeft = 0;
	private Integer cooldownLeft = 0;
	private Integer minesLeft = 0;
	
	private Boolean scan = false;
	private Boolean underwater = false;
	private Boolean shield = false;
	private Boolean shooting = false;
	
	public Ship(int id, Player owner, ShipType shipType, int x, int y, int direction) {
		super();
		this.id = id;
		this.owner = owner;
		this.shipType = shipType;
		this.pos = new Position(x, y);
		this.direction = direction;
		this.minesLeft = shipType.getMines();
	
		hull = new Boolean[shipType.getLength()];
		for (int i = 0; i < hull.length; i++) {
			hull[i] = true;
		}
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}

	public int getId() {
		return id;
	}
	
	public boolean isAlive() {
		Boolean alive = false;
		for (int i = 0; i < hull.length; i++) {
			alive = alive || hull[i];
		}
		return alive;
	}

	public String getCommand(String args) throws Exception {
		
		Process proc = null;
		Scanner stdin = null;
		try {
			proc = Runtime.getRuntime().exec(owner.getCmd() + " " + args);
			stdin = new Scanner(proc.getInputStream());
			StringBuilder response = new StringBuilder();
			while (stdin.hasNext()) {
				response.append(stdin.next()).append(' ');
			}
			return response.toString();	
		} finally {
			if (stdin != null) stdin.close();
			if (proc != null) proc.destroy();
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other != null && other instanceof Ship) {
			return getId() == ((Ship) other).getId();
		}
		return false;
	}
	
	@Override
	public String toString() {
		return owner.getDisplayName();
	}

	public ShipType getShipType() {
		return shipType;
	}

	public Position getPos() {
		return pos;
	}

	public String[][] getMap() {
		return map;
	}

	public Integer getDirection() {
		return direction;
	}

	public void setDirection(Integer direction) {
		this.direction = direction;
	}

	public Integer getActionsLeft() {
		return actionsLeft;
	}

	public void setActionsLeft(Integer actionsLeft) {
		this.actionsLeft = actionsLeft;
	}

	public Integer getMovesLeft() {
		return movesLeft;
	}

	public void setMovesLeft(Integer movesLeft) {
		this.movesLeft = movesLeft;
	}

	public Integer getShotsLeft() {
		return shotsLeft;
	}

	public void setShotsLeft(Integer shotsLeft) {
		this.shotsLeft = shotsLeft;
	}

	public Boolean getScan() {
		return scan;
	}

	public void setScan(Boolean scan) {
		this.scan = scan;
	}

	public Boolean getUnderwater() {
		return underwater;
	}

	public void setUnderwater(Boolean underwater) {
		this.underwater = underwater;
	}

	public Boolean getShield() {
		return shield;
	}

	public void setShield(Boolean shield) {
		this.shield = shield;
	}

	public Integer getUnderwaterLeft() {
		return underwaterLeft;
	}

	public void setUnderwaterLeft(Integer underwaterLeft) {
		this.underwaterLeft = underwaterLeft;
	}

	public void setShipType(ShipType shipType) {
		this.shipType = shipType;
	}

	public Integer getCooldownLeft() {
		return cooldownLeft;
	}

	public void setCooldownLeft(Integer cooldownLeft) {
		this.cooldownLeft = cooldownLeft;
	}

	public Boolean[] getHull() {
		return hull;
	}

	public Integer getMinesLeft() {
		return minesLeft;
	}

	public void setMinesLeft(Integer minesLeft) {
		this.minesLeft = minesLeft;
	}

	public Boolean getShooting() {
		return shooting;
	}

	public void setShooting(Boolean shooting) {
		this.shooting = shooting;
	}
	
	
}
