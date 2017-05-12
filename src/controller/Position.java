package controller;

public class Position {

	private Integer x;
	private Integer y;
	
	public Position(Integer x, Integer y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	public Integer getX() {
		return x;
	}
	public Integer getY() {
		return y;
	}

	public void setLocation(Integer x, Integer y) {
		this.x = x;
		this.y = y;
	}	
}
