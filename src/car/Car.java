package car;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import neuralNetwork.NeuralNetwork;

public class Car {

	public Vector pos, startPos;
	public float deg = 0;
	public Sensor s1, s2;
	public int round = 1;
	public int training = 0;
	public NeuralNetwork nn = new NeuralNetwork(2, 4, 2);
	public ArrayList<Boundary> walls;

	public Car(Vector pos, int deg, ArrayList<Boundary> walls) {
		this.pos = pos;
		startPos = new Vector(pos.x, pos.y);
		this.deg = deg;
		s1 = new Sensor(pos, deg - 50, 30, false);
		s2 = new Sensor(pos, deg + 50, 30, false);
		this.walls = walls;
	}

	public void update(Graphics g) {
		
		double speed = 0.1;
		double stearing = 0.2;
		
		pos.x += speed * Math.cos(Math.toRadians(deg));
		pos.y += speed * Math.sin(Math.toRadians(deg));

		Line2D l1 = new Line2D.Double(pos.x - 10, pos.y - 5, pos.x + 10, pos.y - 5);
		Line2D l2 = new Line2D.Double(pos.x + 10, pos.y - 5, pos.x + 10, pos.y + 5);
		Line2D l3 = new Line2D.Double(pos.x + 10, pos.y + 5, pos.x - 10, pos.y + 5);
		Line2D l4 = new Line2D.Double(pos.x - 10, pos.y + 5, pos.x - 10, pos.y - 5);

		AffineTransform at = 
				AffineTransform.getRotateInstance(
						Math.toRadians(deg), pos.x, pos.y);

		g.setColor(Color.blue);
		((Graphics2D) g).draw(at.createTransformedShape(l1));
		((Graphics2D) g).draw(at.createTransformedShape(l2));
		((Graphics2D) g).draw(at.createTransformedShape(l3));
		((Graphics2D) g).draw(at.createTransformedShape(l4));

		s1.update(g);
		s2.update(g);

		double s1Out = s1.cast(walls);
		double s2Out = s2.cast(walls);
		double[] inputs = new double[] {s1Out, s2Out};
		
		// fail
		if (s1Out < 0.35 || s2Out < 0.35) {
			pos.x = startPos.x;
			pos.y = startPos.y;
			deg = 0;
			
			s1.setDir(deg - 50);
			s2.setDir(deg + 50);
			
			training = 0;
			round++;
			
			try {
				nn.train(inputs, bestMoveAtTheMoment(inputs));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return;
		}

		try {
			double[] guess = nn.feedForward(inputs);
			if (guess[0] > 0.85)
				deg -= stearing;
			else if (guess[1] > 0.85)
				deg += stearing;
		} catch (Exception e) {
			e.printStackTrace();
		}

		s1.setDir(deg - 50);
		s2.setDir(deg + 50);

		if (training < 12000) {
			training++;
			g.setColor(Color.green);
			g.drawString("training...", 10, 35);

			try {
				nn.train(inputs, bestMoveAtTheMoment(inputs));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			g.setColor(Color.red);
			g.drawString("not training...", 10, 35);
		}
		g.setColor(Color.white);
		g.drawString("round: " + round, 10, 20);
	}

	public double[] bestMoveAtTheMoment(double[] inputs) {

		double[] res = new double[] {1, 1};

		if (s1.cast(walls) < 1) {
			res[0] = 0;
		}
		else if (s2.cast(walls) < 1) {
			res[1] = 0;
		}
		else
			return new double[] {0, 0};

		return res;
	}
}
