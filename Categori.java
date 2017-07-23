import java.awt.Color;
import java.io.*;


public class Categori implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private Color color;

	public Categori(String name){
		this.name = name;
		this.color = Color.black;
	}
	public Categori(String name, Color color){
		this.name = name;
		this.color = color;
	}
	public String getName(){
		return name;
	}
	public String toString(){
		return name;
	}
	public Color getColor(){
		return color;
	}
}
