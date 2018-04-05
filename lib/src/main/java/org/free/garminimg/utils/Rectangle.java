package org.free.garminimg.utils;

public class Rectangle {

	public int x, y, width, height;

	public Rectangle(int minLon, int minLat, int ancho, int alto) {

		x = minLon;
		y = minLat;
		this.width = ancho;
		this.height = alto;
	}

	public Rectangle() {

	}
}
