public class PassiveBot {

	int round;
	int playerID;
	
	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("5");
		} else {
			new PassiveBot().play(args[0].split(";"));
		}
	}

	private void play(String[] args) {

		round = Integer.parseInt(args[0]);
		playerID = Integer.parseInt(args[1]);
		
		System.out.println("W");
	}

}