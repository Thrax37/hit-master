package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import players.PassiveBot;
import players.RandomBot;

public class Game {
	private static Player[] players = {
		new PassiveBot(),
		new RandomBot()
	};
	
	// Game Parameters
	private static final int ROUNDS = 100;
	private static final int GAMES = 1;
	private static final int GRID_HEIGHT = 30 + players.length;
	private static final int GRID_WIDTH = 30 + players.length;
	
	// Console
	private static final boolean DEBUG = true;
	private static final boolean GAME_MESSAGES = true;
	private static final boolean GLOBAL_MESSAGES = false;
	private static final boolean GRID_MESSAGES = true;
	
	private static final int COOLDOWN_SHIELD = 3;
	private static final int COOLDOWN_REPAIR = 3;
	private static final int COOLDOWN_PLUNGE = 3;
	
	private static final int MAX_UNDERWATER = 5;
	
	private static final String CHARACTER_EMPTY = " ";
	private static final String CHARACTER_MINE = "M";
	private static final String CHARACTER_WRECKAGE = "W";
	private static final String CHARACTER_WALL = "X";
	private static final String CHARACTER_UNKNOWN = "U";
	private static final String CHARACTER_GRID = "█";
	
	private static final String CHARACTER_SELF = "O";
	private static final String CHARACTER_ENNEMY= "A";
	private static final String CHARACTER_ENNEMY_DMG = "B";
	private static final String CHARACTER_ENNEMY_SUB  = "C";
	private static final String CHARACTER_ENNEMY_SUB_DMG  = "D";
	
	private final List<Ship> ships = new ArrayList<Ship>();
	private String[][] map = new String[GRID_HEIGHT][GRID_WIDTH];
	private Random random;
	
	private int round = 0;
	
	@SuppressWarnings("serial")
	private final Map<Integer, ShipType> shipTypes = new HashMap<Integer, ShipType>() {
		{
			put(1, ShipType.DESTROYER);
			put(2, ShipType.SUBMARINE);
			put(3, ShipType.CRUISER);
			put(4, ShipType.BATTLESHIP);
			put(5, ShipType.CARRIER);
		};
	};
	
	public Game() {
		
		random = new Random(new Date().getTime());
		
		for (int i = 0; i < players.length; i++) {
			players[i].setId(i);
		}
	}
	
	public static void main(String... args) {
		
		List<List<Score>> totalScores = new ArrayList<>();
		
		// Starting
		for (int i = 0; i < GAMES; i++) {
			totalScores.add(new Game().run());
		}
		
		// Scores
		Map<Player, List<Score>> playerScores = new HashMap<>();
		for (List<Score> scores : totalScores) {
			for (Score score : scores) {
				if (playerScores.get(score.getPlayer()) == null) {
					playerScores.put(score.getPlayer(), new ArrayList<>());
				}
				playerScores.get(score.getPlayer()).add(score);
			}
		}
		
		List<Score> finalScores = new ArrayList<>();
		for (Player player : playerScores.keySet()) {
			
			int hits = 0;
			int sunken = 0;
			int damage = 0;
			int death = 0;
			
			for (Score score : playerScores.get(player)) {
				hits += score.getHits();
				sunken += score.getSunken();
				damage += score.getDamage();
				death += score.getDeath();
			}
						
			finalScores.add(new Score(player, hits, sunken, damage, death));
		}
		
		//sort descending
		Collections.sort(finalScores, Collections.reverseOrder());
		
		System.out.println("################################");
		for (int i = 0; i < finalScores.size(); i++) {
			Score score = finalScores.get(i);
			System.out.println(i+1 + ". " + score.print());
		}
	}	
	
	public List<Score> run() {
			
		if (GLOBAL_MESSAGES) 
			System.out.println("Starting a new game...");
		
		this.initialize();
		
		if (GLOBAL_MESSAGES) 
			System.out.println("Game begins.");
							
		for (round = 1; round <= ROUNDS; round++) {
			if (GLOBAL_MESSAGES) {
				System.out.println("======== Round : " +  round + " ========");
			}
			
			Collections.shuffle(ships);
			
			if (!makeTurns()) break; //break if only no player left
		}
	
		return printResults();
	}
	
	private void initialize() {		
		
		for (int x = 0; x < GRID_WIDTH; x++) {
			for (int y = 0; y < GRID_HEIGHT; y++) {
				map[y][x] = CHARACTER_EMPTY;
			}
		}
		
		
		for (int i = 0; i < players.length; i++) {
			try {
				String shipType = players[i].prepare();

				if (isValidShipType(shipType)) {

					if (GAME_MESSAGES) System.out.println(players[i].getDisplayName() + " chose to command a " + shipTypes.get(Integer.parseInt(shipType)));
					
					int direction, x, y;
					
					do {
						x = random.nextInt(GRID_WIDTH);
						y = random.nextInt(GRID_HEIGHT);
						direction = random.nextInt(4);
						
					} while (!addShip(shipTypes.get(Integer.parseInt(shipType)), x, y, direction));
					
					Ship ship = new Ship(i, players[i], shipTypes.get(Integer.parseInt(shipType)), x, y, direction);
					ships.add(ship);
					
					updateMap();
					
				} else {
					throw new Exception("Invalid input : " + shipType);
				}
			} catch (Exception e) {
				if (DEBUG) {
					System.out.println("Exception in initialize() by " + players[i].getDisplayName());
					e.printStackTrace();
				}
			}
		}
		
	}	
	
	private boolean addShip(ShipType shipType, int x, int y, int direction) {
		
		if (direction == 0) { // Top
			for (int i = 0; i < shipType.getLength(); i++) {
				if (y + i >= GRID_HEIGHT) return false;
				if (!CHARACTER_EMPTY.equals(map[y + i][x])) return false;
			}
		} else if (direction == 1) { // Right
			for (int i = 0; i < shipType.getLength(); i++) {
				if (x - i < 0) return false;
				if (!CHARACTER_EMPTY.equals(map[y][x - i]))	return false;
			}
		} else if (direction == 2) { // Bottom
			for (int i = 0; i < shipType.getLength(); i++) {
				if (y - i < 0) return false;
				if (!CHARACTER_EMPTY.equals(map[y - i][x])) return false;
			}
		} else if (direction == 3) { // Left
			for (int i = 0; i < shipType.getLength(); i++) {
				if (x + i >= GRID_WIDTH) return false;
				if (!CHARACTER_EMPTY.equals(map[y][x + i])) return false;
			}
		}
		
		return true;
	}
	
	private boolean moveShip(Ship ship) {
		
		int direction = ship.getDirection();
		int x = ship.getPos().getX();
		int y = ship.getPos().getY();
		int i = 1;
		
		if (direction == 0) { // Top
			if (y - i < 0) return false;
			if (!CHARACTER_EMPTY.equals(map[y - i][x]) && !CHARACTER_MINE.equals(map[y - i][x])) return false;
			ship.getPos().setLocation(x, y - i);
			if (CHARACTER_MINE.equals(map[y - i][x])) triggerExplosion(x, y - i, ship);
		} else if (direction == 1) { // Right
			if (x + i >= GRID_WIDTH) return false;
			if (!CHARACTER_EMPTY.equals(map[y][x + i]) && !CHARACTER_MINE.equals(map[y][x + i])) return false;
			ship.getPos().setLocation(x + i, y);
			if (CHARACTER_MINE.equals(map[y][x + i])) triggerExplosion(x + i, y, ship);
		} else if (direction == 2) { // Bottom
			if (y + i >= GRID_HEIGHT) return false;
			if (!CHARACTER_EMPTY.equals(map[y + i][x]) && !CHARACTER_MINE.equals(map[y + i][x])) return false;
			ship.getPos().setLocation(x, y + i);
			if (CHARACTER_MINE.equals(map[y + i][x])) triggerExplosion(x, y + i, ship);
		} else if (direction == 3) { // Left
			if (x - i < 0) return false;
			if (!CHARACTER_EMPTY.equals(map[y][x - i]) && !CHARACTER_MINE.equals(map[y][x - i])) return false;
			ship.getPos().setLocation(x - i, y);
			if (CHARACTER_MINE.equals(map[y][x - i])) triggerExplosion(x - i, y, ship);
		}
		
		return true;
	}
	
	private boolean ramShip(Ship ship) { 
		
		int direction = ship.getDirection();
		int x = ship.getPos().getX();
		int y = ship.getPos().getY();
		int k = 1;
		
		int targetX = x;
		int targetY = y;
		
		if (direction == 0) { // Top
			if (y - k < 0) return false;
			if (!MapUtils.isNumeric(map[y - k][x])) return false;
			targetY = y - k;
		} else if (direction == 1) { // Right
			if (x + k >= GRID_WIDTH) return false;
			if (!MapUtils.isNumeric(map[y][x + k])) return false;
			targetX	= x + k;		
		} else if (direction == 2) { // Bottom
			if (y + k >= GRID_HEIGHT) return false;
			if (!MapUtils.isNumeric(map[y + k][x])) return false;
			targetY = y + k;			
		} else if (direction == 3) { // Left
			if (x - k < 0) return false;
			if (!MapUtils.isNumeric(map[y][x - k])) return false;
			targetX	= x - k;			
		}
		
		String cell = map[targetY][targetX];
		
		Integer ownerID = Integer.parseInt(cell);
		Ship targetShip = ships.stream().filter(o -> o.getOwner().getId() == ownerID).findFirst().get();
		
		if (targetShip.getShield()) {
			targetShip.setShield(false);
			
			if (GAME_MESSAGES) {
				System.out.println(targetShip.getOwner().getDisplayName() + " absorbed the hit with its shield");
			}
			
			return true;
		} else if (targetShip.getUnderwater()) {
			if (GAME_MESSAGES) {
				System.out.println(targetShip.getOwner().getDisplayName() + " dodged the hit");
			}
			
			return true;
		}
		
		int shipX = targetShip.getPos().getX();
		int shipY = targetShip.getPos().getY();
		int shipDirection = targetShip.getDirection();
		
		if (shipDirection == 0) { // Top
			for (int i = 0; i < targetShip.getShipType().getLength(); i++) {
				if (targetX == shipX && targetY == (shipY + i)) {
					if (targetShip.isAlive() && targetShip.getHull()[i]) {
						targetShip.getHull()[i] = false;
						computeHit(ship, targetShip);
					}
				}
			}
		} else if (shipDirection == 1) { // Right
			for (int i = 0; i < targetShip.getShipType().getLength(); i++) {
				if (targetX == (shipX - i) && targetY == shipY) {
					if (targetShip.isAlive() && targetShip.getHull()[i]) {
						targetShip.getHull()[i] = false;
						computeHit(ship, targetShip);
					}
				}
			}
		} else if (shipDirection == 2) { // Bottom
			for (int i = 0; i < targetShip.getShipType().getLength(); i++) {
				if (targetX == shipX && targetY == (shipY - i)) {
					if (targetShip.isAlive() && targetShip.getHull()[i]) {
						targetShip.getHull()[i] = false;
						computeHit(ship, targetShip);
					}
				}
			}
		} else if (shipDirection == 3) { // Left
			for (int i = 0; i < targetShip.getShipType().getLength(); i++) {
				if (targetX == (shipX + i) && targetY == shipY) {
					if (targetShip.isAlive() && targetShip.getHull()[i]) {
						targetShip.getHull()[i] = false;
						computeHit(ship, targetShip);
					}
				}
			}
		}
		
		return true;
	}
	
	private boolean backShip(Ship ship) {
		
		int direction = ship.getDirection();
		int x = ship.getPos().getX();
		int y = ship.getPos().getY();
		int i = 1;
		
		switch (direction) {
			case 0: y = y + (ship.getShipType().getLength() - 1); break;
			case 1: x = x - (ship.getShipType().getLength() - 1); break;
			case 2: y = y - (ship.getShipType().getLength() - 1); break;
			case 3: x = x + (ship.getShipType().getLength() - 1); break;
		}
				
		if (direction == 0) { // Top
			if (y + i >= GRID_HEIGHT) return false;
			if (!CHARACTER_EMPTY.equals(map[y + i][x]) && !CHARACTER_MINE.equals(map[y + i][x])) return false;
			ship.getPos().setLocation(x, y + i - (ship.getShipType().getLength() - 1));
			if (CHARACTER_MINE.equals(map[y + i][x])) triggerExplosion(x, y + i, ship);
		} else if (direction == 1) { // Right
			if (x - i < 0) return false;
			if (!CHARACTER_EMPTY.equals(map[y][x - i]) && !CHARACTER_MINE.equals(map[y][x - i])) return false;
			ship.getPos().setLocation(x - i + (ship.getShipType().getLength() - 1), y);
			if (CHARACTER_MINE.equals(map[y][x - i])) triggerExplosion(x - i, y, ship);
		} else if (direction == 2) { // Bottom
			if (y - i < 0) return false;
			if (!CHARACTER_EMPTY.equals(map[y - i][x]) && !CHARACTER_MINE.equals(map[y - i][x])) return false;
			ship.getPos().setLocation(x, y - i + (ship.getShipType().getLength() - 1));
			if (CHARACTER_MINE.equals(map[y - i][x])) triggerExplosion(x, y - i, ship);
		} else if (direction == 3) { // Left
			if (x + i >= GRID_WIDTH) return false;
			if (!CHARACTER_EMPTY.equals(map[y][x + i]) && !CHARACTER_MINE.equals(map[y][x + i])) return false;
			ship.getPos().setLocation(x + i - (ship.getShipType().getLength() - 1), y);
			if (CHARACTER_MINE.equals(map[y][x + i])) triggerExplosion(x + i, y, ship);
		}
		
		return true;
		
	}
	
	private boolean turnShip(Ship ship, boolean clockwise) {
		
		int direction = ship.getDirection();
		int x = ship.getPos().getX();
		int y = ship.getPos().getY();
		
		int newDirection = (((direction + (clockwise ? 1 : -1) % 4) + 4) % 4);
		
		List<Position> minesTriggered = new ArrayList<>();

		if (newDirection == 0) { // Top
			for (int i = 0; i < ship.getShipType().getLength(); i++) {
				if (y + i >= GRID_HEIGHT) return false;
				if (!CHARACTER_EMPTY.equals(map[y + i][x]) && !CHARACTER_MINE.equals(map[y + i][x]) && !String.valueOf(ship.getOwner().getId()).equals(map[y + i][x])) return false;
				if (CHARACTER_MINE.equals(map[y + i][x])) minesTriggered.add(new Position(x, y + i));
			}
			ship.setDirection(newDirection);
		} else if (newDirection == 1) { // Right
			for (int i = 0; i < ship.getShipType().getLength(); i++) {
				if (x - i < 0) return false;
				if (!CHARACTER_EMPTY.equals(map[y][x - i]) && !CHARACTER_MINE.equals(map[y][x - i]) && !String.valueOf(ship.getOwner().getId()).equals(map[y][x - i])) return false;
				if (CHARACTER_MINE.equals(map[y][x - i])) minesTriggered.add(new Position(x - i, y));
			}
			ship.setDirection(newDirection);
		} else if (newDirection == 2) { // Bottom
			for (int i = 0; i < ship.getShipType().getLength(); i++) {
				if (y - i < 0) return false;
				if (!CHARACTER_EMPTY.equals(map[y - i][x]) && !CHARACTER_MINE.equals(map[y - i][x]) && !String.valueOf(ship.getOwner().getId()).equals(map[y - i][x])) return false;
				if (CHARACTER_MINE.equals(map[y - i][x])) minesTriggered.add(new Position(x, y - i));
			}
			ship.setDirection(newDirection);
		} else if (newDirection == 3) { // Left
			for (int i = 0; i < ship.getShipType().getLength(); i++) {
				if (x + i >= GRID_WIDTH) return false;
				if (!CHARACTER_EMPTY.equals(map[y][x + i]) && !CHARACTER_MINE.equals(map[y][x + i]) && !String.valueOf(ship.getOwner().getId()).equals(map[y][x + i])) return false;
				if (CHARACTER_MINE.equals(map[y][x + i])) minesTriggered.add(new Position(x + i, y));
			}
			ship.setDirection(newDirection);
		}
		
		for (Position pos : minesTriggered) {
			triggerExplosion(pos.getX(), pos.getY(), ship);
		}
				
		return true;
		
	}
	
	private boolean shoot(Ship ship, int orientationX, int distanceX, int orientationY, int distanceY) {
		
		int x = ship.getPos().getX();
		int y = ship.getPos().getY();
		
		int targetX;
		int targetY;
		
		if (orientationX <= 0) { // Left
			if (x - distanceX < 0) return false;
			targetX = x - distanceX; 
		} else { // Right
			if (x + distanceX >= GRID_WIDTH) return false;
			targetX = x + distanceX;
		}
		
		if (orientationY <= 0) { // Top
			if (y - distanceY < 0) return false;
			targetY = y - distanceY;
		} else { // Bottom
			if (y + distanceY >= GRID_HEIGHT) return false;
			targetY = y + distanceY;
		}
		
		String cell = map[targetY][targetX];
		
		if (CHARACTER_MINE.equals(cell)) {
			
			triggerExplosion(targetX, targetY, ship);
			
		} else if (MapUtils.isNumeric(cell)) {
			
			Integer ownerID = Integer.parseInt(cell);
			Ship targetShip = ships.stream().filter(o -> o.getOwner().getId() == ownerID).findFirst().get();
			
			if (targetShip.getShield()) {
				targetShip.setShield(false);
				
				if (GAME_MESSAGES) {
					System.out.println(targetShip.getOwner().getDisplayName() + " absorbed the hit with its shield");
				}
				
				return true;
			} else if (targetShip.getUnderwater()) {
				if (GAME_MESSAGES) {
					System.out.println(targetShip.getOwner().getDisplayName() + " dodged the hit");
				}
				
				return true;
			}
			
			int shipX = targetShip.getPos().getX();
			int shipY = targetShip.getPos().getY();
			int shipDirection = targetShip.getDirection();
			
			if (shipDirection == 0) { // Top
				for (int i = 0; i < targetShip.getShipType().getLength(); i++) {
					if (ship.getShipType().equals(ShipType.CARRIER)) {
						if (targetX == shipX && targetY == (shipY + i - 1) && i > 0) {
							if (targetShip.isAlive() && targetShip.getHull()[i - 1]) {
								targetShip.getHull()[i - 1] = false;
								computeHit(ship, targetShip);
							}
						}
						if (targetX == shipX && targetY == (shipY + i + 1) && i < targetShip.getShipType().getLength() - 1) {
							if (targetShip.isAlive() && targetShip.getHull()[i + 1]) {
								targetShip.getHull()[i + 1] = false;
								computeHit(ship, targetShip);
							}
						}
					}
					if (targetX == shipX && targetY == (shipY + i)) {
						if (targetShip.isAlive() && targetShip.getHull()[i]) {
							targetShip.getHull()[i] = false;
							computeHit(ship, targetShip);
						}
					}
				}
			} else if (shipDirection == 1) { // Right
				for (int i = 0; i < targetShip.getShipType().getLength(); i++) {
					if (ship.getShipType().equals(ShipType.CARRIER)) {
						if (targetX == (shipX - i - 1) && targetY == shipY && i > 0) {
							if (targetShip.isAlive() && targetShip.getHull()[i - 1]) {
								targetShip.getHull()[i - 1] = false;
								computeHit(ship, targetShip);
							}
						}
						if (targetX == (shipX - i + 1) && targetY == shipY && i < targetShip.getShipType().getLength() - 1) {
							if (targetShip.isAlive() && targetShip.getHull()[i + 1]) {
								targetShip.getHull()[i + 1] = false;
								computeHit(ship, targetShip);
							}
						}
					}
					if (targetX == (shipX - i) && targetY == shipY) {
						if (targetShip.isAlive() && targetShip.getHull()[i]) {
							targetShip.getHull()[i] = false;
							computeHit(ship, targetShip);
						}
					}
				}
			} else if (shipDirection == 2) { // Bottom
				for (int i = 0; i < targetShip.getShipType().getLength(); i++) {
					if (ship.getShipType().equals(ShipType.CARRIER)) {
						if (targetX == shipX && targetY == (shipY - i - 1) && i > 0) {
							if (targetShip.isAlive() && targetShip.getHull()[i - 1]) {
								targetShip.getHull()[i - 1] = false;
								computeHit(ship, targetShip);
							}
						}
						if (targetX == shipX && targetY == (shipY - i + 1) && i < targetShip.getShipType().getLength() - 1) {
							if (targetShip.isAlive() && targetShip.getHull()[i + 1]) {
								targetShip.getHull()[i + 1] = false;
								computeHit(ship, targetShip);
							}
						}
					}
					if (targetX == shipX && targetY == (shipY - i)) {
						if (targetShip.isAlive() && targetShip.getHull()[i]) {
							targetShip.getHull()[i] = false;
							computeHit(ship, targetShip);
						} 
					}
				}
			} else if (shipDirection == 3) { // Left
				for (int i = 0; i < targetShip.getShipType().getLength(); i++) {
					if (ship.getShipType().equals(ShipType.CARRIER)) {
						if (targetX == (shipX + i - 1) && targetY == shipY && i > 0) {
							if (targetShip.isAlive() && targetShip.getHull()[i - 1]) {
								targetShip.getHull()[i - 1] = false;
								computeHit(ship, targetShip);
							}
						}
						if (targetX == (shipX + i + 1) && targetY == shipY && i < targetShip.getShipType().getLength() - 1) {
							if (targetShip.isAlive() && targetShip.getHull()[i + 1]) {
								targetShip.getHull()[i + 1] = false;
								computeHit(ship, targetShip);
							}
						}
					}
					if (targetX == (shipX + i) && targetY == shipY) {
						if (targetShip.isAlive() && targetShip.getHull()[i]) {
							targetShip.getHull()[i] = false;
							computeHit(ship, targetShip);
						}
					}
				}
			}
		}
		
		return true;
	}
	
	private boolean dropMine(Ship ship, int orientationX, int distanceX, int orientationY, int distanceY) {
		
		int x = ship.getPos().getX();
		int y = ship.getPos().getY();
		
		int targetX;
		int targetY;
		
		if (orientationX <= 0) { // Left
			if (x - distanceX < 0) return false;
			targetX = x - distanceX; 
		} else { // Right
			if (x + distanceX >= GRID_WIDTH) return false;
			targetX = x + distanceX;
		}
		
		if (orientationY <= 0) { // Top
			if (y - distanceY < 0) return false;
			targetY = y - distanceY;
		} else { // Bottom
			if (y + distanceY >= GRID_HEIGHT) return false;
			targetY = y + distanceY;
		}
		
		String cell = map[targetY][targetX];
		
		if (!CHARACTER_EMPTY.equals(cell)) {
			return false;
		} else {
			map[targetY][targetX] = CHARACTER_MINE;
			return true;
		}
		
	}
	
	private void computeHit(Ship ship, Ship targetShip) {
		
		ship.setHits(ship.getHits() + 1);
		targetShip.setDamage(targetShip.getDamage() + 1);
		
		if (!targetShip.isAlive()) {
			ship.setSunken(ship.getSunken() + 1);
		}
	}
	
	private void triggerExplosion(int x, int y, Ship ship) {
		
		map[y][x] = CHARACTER_EMPTY;
		
		for (int j = -1; j <= 1; j++) {
			for (int k = -1; k <= 1; k++) {
				
				if (x + k < 0 || x + k >= GRID_WIDTH || y + j < 0 || y + j >= GRID_HEIGHT) continue;
				
				String cell = map[y + j][x + k];
				
				// Chain explosions
				if (CHARACTER_MINE.equals(cell)) {
					triggerExplosion(x + k, y + j, ship);
				} else if (MapUtils.isNumeric(cell)) {
					
					Integer ownerID = Integer.parseInt(cell);
					Ship targetShip = ships.stream().filter(o -> o.getOwner().getId() == ownerID).findFirst().get();
					
					if (targetShip.getShield()) {
						targetShip.setShield(false);
						
						if (GAME_MESSAGES) {
							System.out.println(targetShip.getOwner().getDisplayName() + " absorbed the hit with its shield");
						}
						continue;
					} 
					
					int shipX = targetShip.getPos().getX();
					int shipY = targetShip.getPos().getY();
					int shipDirection = targetShip.getDirection();
					
					if (shipDirection == 0) { // Top
						for (int i = 0; i < targetShip.getShipType().getLength(); i++) {
							if ((x + k) == shipX && (y + j) == (shipY + i)) {
								if (targetShip.isAlive() && targetShip.getHull()[i]) {
									targetShip.getHull()[i] = false;
									computeHit(ship, targetShip);
								} 
							}
						}
					} else if (shipDirection == 1) { // Right
						for (int i = 0; i < targetShip.getShipType().getLength(); i++) {
							if ((x + k) == (shipX - i) && (y + j) == shipY) {
								if (targetShip.isAlive() && targetShip.getHull()[i]) {
									targetShip.getHull()[i] = false;
									computeHit(ship, targetShip);
								}  
							}
						}
					} else if (shipDirection == 2) { // Bottom
						for (int i = 0; i < targetShip.getShipType().getLength(); i++) {
							if ((x + k) == shipX && (y + j) == (shipY - i)) {
								if (targetShip.isAlive() && targetShip.getHull()[i]) {
									targetShip.getHull()[i] = false;
									computeHit(ship, targetShip);
								}  
							}
						}
					} else if (shipDirection == 3) { // Left
						for (int i = 0; i < targetShip.getShipType().getLength(); i++) {
							if ((x + k) == (shipX + i) && (y + j) == shipY) {
								if (targetShip.isAlive() && targetShip.getHull()[i]) {
									targetShip.getHull()[i] = false;
									computeHit(ship, targetShip);
								} 
							}
						}
					}
				}
			}
		}
		
		
	}
	
	private void updateMap() {
		
		// Empty Ships
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				if (MapUtils.isNumeric(map[i][j])) {
					map[i][j] = CHARACTER_EMPTY;
				}
			}
		}
		
		for (Ship ship : ships) {
			
			int x = ship.getPos().getX();
			int y = ship.getPos().getY();
			int direction = ship.getDirection();
			
			String shipCharacter;
			if (ship.isAlive()) {
				shipCharacter = String.valueOf(ship.getOwner().getId());
			} else {
				shipCharacter = CHARACTER_WRECKAGE;
			}
			
			if (direction == 0) { // Top
				for (int i = 0; i < ship.getShipType().getLength(); i++) {
					map[y + i][x] = shipCharacter;
				}
			} else if (direction == 1) { // Right
				for (int i = 0; i < ship.getShipType().getLength(); i++) {
					map[y][x - i] = shipCharacter;
				}
			} else if (direction == 2) { // Bottom
				for (int i = 0; i < ship.getShipType().getLength(); i++) {
					map[y - i][x] = shipCharacter;
				}
			} else if (direction == 3) { // Left
				for (int i = 0; i < ship.getShipType().getLength(); i++) {
					map[y][x + i] = shipCharacter;
				}
			}
		}
	}
		
	private void displayGrid() {
		
		StringBuilder builder = new StringBuilder();
		
		for (int x = 0; x < GRID_WIDTH + 2; x++) {
			builder.append(CHARACTER_GRID);
		}
		
		builder.append("\r\n");
		
		for (int y = 0; y < GRID_HEIGHT; y++) {
			
			builder.append(CHARACTER_GRID);
			
			for (int x = 0; x < GRID_WIDTH; x++) {
				if (MapUtils.isNumeric(map[y][x])) {
					builder.append(Integer.parseInt(map[y][x]) % 10);
				} else {
					builder.append(map[y][x]);
				}
			}
			
			builder.append(CHARACTER_GRID + "\r\n");
		}
		
		for (int x = 0; x < GRID_WIDTH + 2; x++) {
			builder.append(CHARACTER_GRID);
		}
		
		builder.append("\r\n");
		
		System.out.println(builder.toString());
	}
	
	private boolean makeTurns() {
		
		// Player Turn
		if (onePlayerLeft()) return false;
		
		for (Ship ship : ships) {
			
			if (ship.isAlive()) {
				
				ship.setActionsLeft(1);
				ship.setMovesLeft(ship.getShipType().getMoves());
				ship.setShotsLeft(ship.getShipType().getShots());
				ship.setCooldownLeft(ship.getCooldownLeft() - 1 < 0 ? 0 : ship.getCooldownLeft() - 1);
				ship.setShooting(false);
				
				if (ship.getUnderwater() && ship.getUnderwaterLeft() - 1 < 0) {
					executePlunge(ship);
				}
				
				if (GRID_MESSAGES) {
					displayGrid();
				}
				
				
				Player owner = ship.getOwner();
				try {
					String request = round + ";" + owner.getId() + ";" + generateStatus(ship) + ";" + generateMap(ship);
					String response = ship.getCommand(request).trim();
					if (DEBUG) {
						System.out.println("Request : " + request);
						System.out.println("Response : " + response);
					}
					
					ship.setScan(false);
					ship.setShield(false);
					
					for (int i = 0; i < response.length(); i++) {
						
						switch (response.charAt(i)) {
							case 'M': executeMove(ship); break;
							case 'B': executeBack(ship); break;
							case 'F': executeFire(ship, response.subSequence(i + 1, i + 5)); i+=4; break;
							case 'N': executeMine(ship, response.subSequence(i + 1, i + 5)); i+=4; break;
							case 'A': executeRam(ship); break;
							case 'C': executeRotation(ship, false); break;
							case 'K': executeRotation(ship, true); break;
							case 'S': executeScan(ship); break;
							case 'R': executeRepair(ship); break;
							case 'P': executePlunge(ship); break;
							case 'D': executeShield(ship); break;
							case 'W': executeWait(ship); break;
							default : executeWait(ship); break;
						}
					}			
					
				} catch (Exception e) {
					if (DEBUG) {
						System.out.println("Exception in makeTurns() by " + owner.getDisplayName());
						e.printStackTrace();
					}
				}
				
				if (onePlayerLeft()) return false;
			}
		}
		
		return true;
	}
	
	private boolean onePlayerLeft() {
		
		int alive = 0;
		for (Ship ship : ships) {
			alive += ship.isAlive() ? 1 : 0;
		}
				
		return alive <= 1;
	}
	
	private boolean isValidShipType(String shipType) {
		
		try {
			return ((Integer.parseInt(shipType) > 0) && (Integer.parseInt(shipType) <= 5));
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean isInRange(Ship ship, int orientationX, int distanceX, int orientationY, int distanceY) {
		
		boolean anyMatch = false;
		
		int shipDirection = ship.getDirection();

		for (int i = 0; i < ship.getShipType().getLength(); i++) {
			if (shipDirection == 0) { // Top
				if (distanceY + i <= 1) anyMatch = true;
			} else if (shipDirection == 1) { // Right
				if (distanceX - i <= 1) anyMatch = true;
			} else if (shipDirection == 2) { // Bottom
				if (distanceY - i <= 1) anyMatch = true;
			} else if (shipDirection == 3) { // Left
				if (distanceX + i <= 1) anyMatch = true;
			}
		}	
	
		return anyMatch;
	}
	
	private void executeWait(Ship ship) {}
	
	private void executeFire(Ship ship, CharSequence target) {
		
		if (ship.getActionsLeft() > 0 || (ship.getShooting() && ship.getShotsLeft() > 0)) {
			
			ship.setShooting(true);
			ship.setShotsLeft(ship.getShotsLeft() - 1);
			ship.setActionsLeft(ship.getActionsLeft() - 1);
			
			// Format +x+y
			int orientationX = target.charAt(0) == '+' ? 1 : 0;
			int distanceX = Integer.parseInt("" + target.charAt(1));
			int orientationY = target.charAt(2) == '+' ? 1 : 0;
			int distanceY = Integer.parseInt("" + target.charAt(3));
			
			if (distanceX <= ship.getShipType().getRange() && distanceY <= ship.getShipType().getRange()) {
				
				if (shoot(ship, orientationX, distanceX, orientationY, distanceY)) {
					updateMap();
				} else {
					if (DEBUG) {
						System.out.println("Invalid fire by " + ship.getOwner().getDisplayName() + "");
					}
				}
			} else {
				if (DEBUG) {
					System.out.println("Invalid fire by " + ship.getOwner().getDisplayName() + ", out of range");
				}
			}
		} else {
			if (DEBUG) {
				System.out.println("Invalid fire by " + ship.getOwner().getDisplayName() + ", actions/shots exhausted");
			}
		}
		
	}
	
	private void executeMine(Ship ship, CharSequence target) {
		if (ship.getActionsLeft() > 0) {
			
			ship.setActionsLeft(ship.getActionsLeft() - 1);
			
			if (ship.getMinesLeft() > 0) {
			
				ship.setMinesLeft(ship.getMinesLeft() - 1);
				
				// Format +x+y
				int orientationX = target.charAt(0) == '+' ? 1 : 0;
				int distanceX = Integer.parseInt("" + target.charAt(1));
				int orientationY = target.charAt(2) == '+' ? 1 : 0;
				int distanceY = Integer.parseInt("" + target.charAt(3));
				
				if (isInRange(ship, orientationX, distanceX, orientationY, distanceY)) {
					if (dropMine(ship, orientationX, distanceX, orientationY, distanceY)) {
						updateMap();
					} else {
						if (DEBUG) {
							System.out.println("Invalid mine by " + ship.getOwner().getDisplayName() + "");
						}
					}
				} else {
					if (DEBUG) {
						System.out.println("Invalid mine by " + ship.getOwner().getDisplayName() + ", out of range");
					}	
				}
				
			} else {
				if (DEBUG) {
					System.out.println("Invalid mine by " + ship.getOwner().getDisplayName() + ", no mine left");
				}	
			}
		} else {
			if (DEBUG) {
				System.out.println("Invalid mine by " + ship.getOwner().getDisplayName() + ", actions exhausted");
			}
		}
	}
	
	private void executeRam(Ship ship) {
		if (ship.getActionsLeft() > 0) {
			ship.setActionsLeft(ship.getActionsLeft() - 1);
			
			if (ramShip(ship)) {
				updateMap();
			} else {
				if (DEBUG) {
					System.out.println("Invalid ram by " + ship.getOwner().getDisplayName() + "");
				}
			}
		} else {
			if (DEBUG) {
				System.out.println("Invalid ram by " + ship.getOwner().getDisplayName() + ", actions exhausted");
			}
		}
	}
	
	private void executeRotation(Ship ship, boolean counterClockwise) {
		
		if (ship.getMovesLeft() > 0 || ship.getShipType().equals(ShipType.DESTROYER)) {
			
			ship.setMovesLeft(ship.getMovesLeft() - 1);
			
			if (turnShip(ship, counterClockwise)) {
				updateMap();
			} else {
				if (DEBUG) {
					System.out.println("Invalid rotation by " + ship.getOwner().getDisplayName() + "");
				}
			}
		} else {
			if (DEBUG) {
				System.out.println("Invalid rotation by " + ship.getOwner().getDisplayName() + ", moves exhausted");
			}
		}
	}
 	
	private void executeRepair(Ship ship) {
		
		if (ship.getShipType().equals(ShipType.CRUISER)) {
			
			if (ship.getCooldownLeft() <= 0) {
				
				if (ship.getActionsLeft() > 0) {
					
					for (int i = 0; i < ship.getShipType().getLength(); i++) {
						if (!ship.getHull()[i]) {
							ship.getHull()[i] = true;
							break;
						}
						ship.setCooldownLeft(COOLDOWN_REPAIR);
					}
					
				} else {
					if (DEBUG) {
						System.out.println("Invalid repair by " + ship.getOwner().getDisplayName() + ", actions exhausted");
					}
				}	
			} else {
				if (DEBUG) {
					System.out.println("Invalid repair by " + ship.getOwner().getDisplayName() + ", on cooldown");
				}
			}
		} else {
			if (DEBUG) {
				System.out.println("Invalid repair by " + ship.getOwner().getDisplayName() + ", wrong ship type");
			}
		}	
	}
	
	private void executePlunge(Ship ship) {
		
		if (ship.getShipType().equals(ShipType.SUBMARINE)) {
			
			if (ship.getCooldownLeft() <= 0 && !ship.getUnderwater()) {
				
				if (ship.getActionsLeft() > 0) {
					
					ship.setActionsLeft(ship.getActionsLeft() - 1);
					ship.setUnderwater(true);
					ship.setUnderwaterLeft(MAX_UNDERWATER);
					
				} else {
					if (DEBUG) {
						System.out.println("Invalid plunge by " + ship.getOwner().getDisplayName() + ", actions exhausted");
					}
				}	
				
			} else if (ship.getUnderwater()) {
				
				if (ship.getActionsLeft() > 0) {
					
					ship.setActionsLeft(ship.getActionsLeft() - 1);
					ship.setUnderwater(false);
					ship.setCooldownLeft(COOLDOWN_PLUNGE);
					
				} else {
					if (DEBUG) {
						System.out.println("Invalid plunge by " + ship.getOwner().getDisplayName() + ", actions exhausted");
					}
				}	
			} else {
				if (DEBUG) {
					System.out.println("Invalid plunge by " + ship.getOwner().getDisplayName() + ", on cooldown");
				}
			}	
		} else {
			if (DEBUG) {
				System.out.println("Invalid plunge by " + ship.getOwner().getDisplayName() + ", wrong ship type");
			}
		}	
	}
	
	private void executeShield(Ship ship) {
		
		if (ship.getShipType().equals(ShipType.BATTLESHIP)) {
			
			if (ship.getCooldownLeft() <= 0) {
				
				if (ship.getActionsLeft() > 0) {
					
					ship.setActionsLeft(ship.getActionsLeft() - 1);
					ship.setShield(true);
					ship.setCooldownLeft(COOLDOWN_SHIELD);
					
				} else {
					if (DEBUG) {
						System.out.println("Invalid shield by " + ship.getOwner().getDisplayName() + ", actions exhausted");
					}
				}	
			} else {
				if (DEBUG) {
					System.out.println("Invalid shield by " + ship.getOwner().getDisplayName() + ", on cooldown");
				}
			}
		} else {
			if (DEBUG) {
				System.out.println("Invalid shield by " + ship.getOwner().getDisplayName() + ", wrong ship type");
			}
		}	
	}
	
	private void executeScan(Ship ship) {
		
		if (ship.getActionsLeft() > 0) {
			
			ship.setActionsLeft(ship.getActionsLeft() - 1);
			ship.setScan(true);
			
		} else {
			if (DEBUG) {
				System.out.println("Invalid scan by " + ship.getOwner().getDisplayName() + ", actions exhausted");
			}
		}
	}
	
	private void executeBack(Ship ship) {
		
		if (ship.getMovesLeft() > 0) {
			
			ship.setMovesLeft(ship.getMovesLeft() - 1);
			
			if (backShip(ship)) {
				updateMap();
			} else {
				if (DEBUG) {
					System.out.println("Invalid back by " + ship.getOwner().getDisplayName() + "");
				}
			}
		} else {
			if (DEBUG) {
				System.out.println("Invalid back by " + ship.getOwner().getDisplayName() + ", moves exhausted");
			}
		}
	}
	
	private void executeMove(Ship ship) {
		
		if (ship.getMovesLeft() > 0) {
			
			ship.setMovesLeft(ship.getMovesLeft() - 1);
			
			if (moveShip(ship)) {
				updateMap();
			} else {
				if (DEBUG) {
					System.out.println("Invalid move by " + ship.getOwner().getDisplayName() + "");
				}
			}
		} else {
			if (DEBUG) {
				System.out.println("Invalid move by " + ship.getOwner().getDisplayName() + ", moves exhausted");
			}
		}
	}
	
	private String generateStatus(Ship ship) {
		
		List<String> hulls = new ArrayList<>();
		for (int i = 0; i < ship.getHull().length; i++) {
			hulls.add(ship.getHull()[i] ? "1" : "0");
		}
		String hull = String.join("", hulls);
				
		//X,Y,direction;moves,shots,mines,cooldown;hull;hits,sunken,damage;underwater,shield,scan
		StringBuilder builder = new StringBuilder();
		builder.append(ship.getPos().getX());
		builder.append(",");
		builder.append(ship.getPos().getY());
		builder.append(",");
		builder.append(ship.getDirection());
		builder.append(";");
		builder.append(hull);
		builder.append(";");
		builder.append(ship.getMovesLeft());
		builder.append(",");
		builder.append(ship.getShotsLeft());
		builder.append(",");
		builder.append(ship.getMinesLeft());
		builder.append(",");
		builder.append(ship.getCooldownLeft());
		builder.append(";");
		builder.append(ship.getHits());
		builder.append(",");
		builder.append(ship.getSunken());
		builder.append(",");
		builder.append(ship.getDamage());
		builder.append(";");
		builder.append(ship.getUnderwater() ? "1" : "0");
		builder.append(",");
		builder.append(ship.getShield() ? "1" : "0");
		builder.append(",");
		builder.append(ship.getScan() ? "1" : "0");
		
		return builder.toString();
	}
	
	
	private String generateMap(Ship ship) {
		
		StringBuilder builder = new StringBuilder();
		
		int x = ship.getPos().getX();
		int y = ship.getPos().getY();

		for (int j = -9 ; j <= 9; j++) {
			for (int k = -9; k <= 9; k++) {
				
				if (!ship.getScan() && (k < -5 || k > 5 || j < -5 || j > 5)) {
					builder.append(CHARACTER_UNKNOWN);
				} else {
					if (x + k < 0 || x + k >= GRID_WIDTH || y + j < 0 || y + j >= GRID_HEIGHT) {
						builder.append(CHARACTER_WALL);
					} else if (MapUtils.isNumeric(map[y + j][x + k])){
						if (ship.getOwner().getId() == Integer.parseInt(map[y + j][x + k])) {
							builder.append(CHARACTER_SELF);
						} else {
							
							Integer ownerID = Integer.parseInt((map[y + j][x + k]));
							Ship targetShip = ships.stream().filter(o -> o.getOwner().getId() == ownerID).findFirst().get();
							
							int shipX = targetShip.getPos().getX();
							int shipY = targetShip.getPos().getY();
							int shipDirection = targetShip.getDirection();
							int targetX = x + k;
							int targetY = y + j;
							
							if (shipDirection == 0) { // Top
								for (int i = 0; i < targetShip.getShipType().getLength(); i++) {
									if (targetX == shipX && targetY == (shipY + i)) {
										if (targetShip.getUnderwater() && !ship.getScan()) {
											builder.append(CHARACTER_EMPTY);
										} else if (targetShip.getUnderwater() && ship.getScan()) {
											if (targetShip.isAlive()) {
												if (targetShip.getHull()[i]) {
													builder.append(CHARACTER_ENNEMY_SUB);
												} else {
													builder.append(CHARACTER_ENNEMY_SUB_DMG);
												}
											} else {
												builder.append(CHARACTER_WRECKAGE);
											}
										} else {
											if (targetShip.isAlive()) {
												if (targetShip.getHull()[i]) {
													builder.append(CHARACTER_ENNEMY);
												} else {
													builder.append(CHARACTER_ENNEMY_DMG);
												}
											} else {
												builder.append(CHARACTER_WRECKAGE);
											}
										}
									}
								}
							} else if (shipDirection == 1) { // Right
								for (int i = 0; i < targetShip.getShipType().getLength(); i++) {
									if (targetX == (shipX - i) && targetY == shipY) {
										if (targetShip.getUnderwater() && !ship.getScan()) {
											builder.append(CHARACTER_EMPTY);
										} else if (targetShip.getUnderwater() && ship.getScan()) {
											if (targetShip.isAlive()) {
												if (targetShip.getHull()[i]) {
													builder.append(CHARACTER_ENNEMY_SUB);
												} else {
													builder.append(CHARACTER_ENNEMY_SUB_DMG);
												}
											} else {
												builder.append(CHARACTER_WRECKAGE);
											}
										} else {
											if (targetShip.isAlive()) {
												if (targetShip.getHull()[i]) {
													builder.append(CHARACTER_ENNEMY);
												} else {
													builder.append(CHARACTER_ENNEMY_DMG);
												}
											} else {
												builder.append(CHARACTER_WRECKAGE);
											}
										}
									}
								}
							} else if (shipDirection == 2) { // Bottom
								for (int i = 0; i < targetShip.getShipType().getLength(); i++) {
									if (targetX == shipX && targetY == (shipY - i)) {
										if (targetShip.getUnderwater() && !ship.getScan()) {
											builder.append(CHARACTER_EMPTY);
										} else if (targetShip.getUnderwater() && ship.getScan()) {
											if (targetShip.isAlive()) {
												if (targetShip.getHull()[i]) {
													builder.append(CHARACTER_ENNEMY_SUB);
												} else {
													builder.append(CHARACTER_ENNEMY_SUB_DMG);
												}
											} else {
												builder.append(CHARACTER_WRECKAGE);
											}
										} else {
											if (targetShip.isAlive()) {
												if (targetShip.getHull()[i]) {
													builder.append(CHARACTER_ENNEMY);
												} else {
													builder.append(CHARACTER_ENNEMY_DMG);
												}
											} else {
												builder.append(CHARACTER_WRECKAGE);
											}
										}
									}
								}
							} else if (shipDirection == 3) { // Left
								for (int i = 0; i < targetShip.getShipType().getLength(); i++) {
									if (targetX == (shipX + i) && targetY == shipY) {
										if (targetShip.getUnderwater() && !ship.getScan()) {
											builder.append(CHARACTER_EMPTY);
										} else if (targetShip.getUnderwater() && ship.getScan()) {
											if (targetShip.isAlive()) {
												if (targetShip.getHull()[i]) {
													builder.append(CHARACTER_ENNEMY_SUB);
												} else {
													builder.append(CHARACTER_ENNEMY_SUB_DMG);
												}
											} else {
												builder.append(CHARACTER_WRECKAGE);
											}
										} else {
											if (targetShip.isAlive()) {
												if (targetShip.getHull()[i]) {
													builder.append(CHARACTER_ENNEMY);
												} else {
													builder.append(CHARACTER_ENNEMY_DMG);
												}
											} else {
												builder.append(CHARACTER_WRECKAGE);
											}
										}
									}
								}
							}
						}
					} else if (CHARACTER_MINE.equals(map[y + j][x + k])) { 
						builder.append(CHARACTER_MINE);
					} else {
						builder.append(CHARACTER_EMPTY);
					} 
				}
			}
		}
		
		return builder.toString();
	}
	
	private List<Score> printResults() {
		
		List<Score> scores = new ArrayList<Score>();
		
		System.out.println("********** FINISH **********");
		
		for (Player player : players) {
			
			int hits = 0;
			int sunken = 0;
			int damage = 0;
			int death = 0;
			
			for (Ship ship : ships) {
				if (player.equals(ship.getOwner())) {
					hits += ship.getHits();
					sunken += ship.getSunken();
					damage += ship.getDamage();
					death += (ship.isAlive() ? 0 : 1);
				}
			}
			
			scores.add(new Score(player, hits, sunken, damage, death));
		}
		
		//sort descending
		Collections.sort(scores, Collections.reverseOrder());
		
		for (int i = 0; i < scores.size(); i++) {
			Score score = scores.get(i);
			System.out.println(i+1 + ". " + score.print());
		}
		
		return scores;
		
	}
	
}

