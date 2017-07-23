import java.io.*;

public class Position implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int x;
	private int y;
	public Position(int x, int y){
		this.x = x;
		this.y = y;
	}
	public String getPosition(){
		return x+", "+y;
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public boolean equals(Position pos){
		if(x == pos.getX() && y == pos.getY()){
			return true;
		}else return false;
	}
}
