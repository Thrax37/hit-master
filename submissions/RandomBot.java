import java.util.Random;

public class RandomBot {

	int round;
	int playerID;
	
	public static void main(String[] args) {
		
		if (args.length == 0) {
			Random random = new Random();
			int ship = random.nextInt(5);
			String[] ships = { "1", "2", "3", "4", "5" };
			System.out.println(ships[ship]);
		} else {
			new RandomBot().play(args[0].split(";"));
		}
	}

	private void play(String[] args) {

		round = Integer.parseInt(args[0]);
		playerID = Integer.parseInt(args[1]);

		String[] actions = { "M", "B", "C", "K", "F", "S", "N", "A" };
		Random random = new Random();
		int action = random.nextInt(12);
		
		int rangeX = random.nextInt(5);
		int rangeY = random.nextInt(5);
		int mineX = random.nextInt(1);
		int mineY = random.nextInt(1);
		
		String signX = random.nextInt(1) == 1 ? "+" : "-";
		String signY = random.nextInt(1) == 1 ? "+" : "-";
		
		System.out.println(actions[action] + (action == 4 ? signX + rangeX + signY + rangeY : "") + (action == 6 ? signX + mineX + signY + mineY : ""));
	}

}