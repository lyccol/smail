package com.coordinate_demo;

public class SixShape {

		
	long num;
	String colorName;
	String shapeName;
	public long getNum() {
		return num;
	}
	public void setNum(long num) {
		this.num = num;
	}
	public String getColorName() {
		return colorName;
	}
	public void setColorName(String colorName) {
		this.colorName = colorName;
	}
	public String getShapeName() {
		return shapeName;
	}
	public void setShapeName(String shapeName) {
		this.shapeName = shapeName;
	}
	public SixShape(long num, String colorName, String shapeName) {
		super();
		this.num = num;
		this.colorName = colorName;
		this.shapeName = shapeName;
	}
	
	
	public SixShape(){
		super();
	}
	
	@Override
	public String toString() {
		return "SixShape [num=" + num + ", colorName=" + colorName
				+ ", shapeName=" + shapeName + "]";
	}
	
	
}
