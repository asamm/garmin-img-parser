package org.free.garminimg.utils;

public abstract class Point2D {

	public static class Double extends Point2D {

		public double x, y;

		public Double(double x, double y) {

			this.x = x;
			this.y = y;
		}

		public Double() {

			this.x = 0.;
			this.y = 0.;
		}

		@Override
		public double getX() {
			return x;
		}

		@Override
		public double getY() {
			return y;
		}
	}

	public abstract double getX();

	public abstract double getY();

}
