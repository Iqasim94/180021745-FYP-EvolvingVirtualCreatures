package org.jbox2d.testbed.tests;

import org.apache.commons.lang3.time.StopWatch;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.WheelJoint;
import org.jbox2d.dynamics.joints.WheelJointDef;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;
import my_Code.Evolver;

/**
 * @author Ishmail Qasim
 *
 *         Making a car init
 */

public class CarTest extends TestbedTest {

//Initialization
	public Body m_car;
	public Body m_wheel1;
	public Body m_wheel2;
	public WheelJoint m_spring1;
	public WheelJoint m_spring2;
	public WheelJointDef jd;
	
	public Fixture m_sensor; // Contact sensor
	public boolean hasFin; // If contact has been made with ideal objects
	public float m_zeta = 0.7f; // Damping Ratio: Resistive force reactionary friction.
	
	public static StopWatch stopwatch;
	public static String bestTimeVisualizer = "00:02:00.000";
	
//Record/Gene instantiations
/*	public static Object[] bestGenes = new Object[12]; // Previous Best List
	public static Object[] currentGenes = new Object[12]; // Record List
	public static int run = 0; // Which iteration
	public static double currentBest = 6e+10; // Fitness to compete against - 2mins starting
	public static double recordedTime; // Fitness of genetics
	public static int evolutions = 0; // Number of beneficial evolutions
	public static float wheelSize1 = 0.4f;
	public static float wheelSize2 = 0.4f;
	// Max rotational speed of the wheel.
	public static float m_speed1 = -40.0f; // Back wheel
	public static float m_speed2 = -40.0f; // Front wheel
	public static float m_torque1 = 40.0f; // Max torque available for the wheel/Rate of Acceleration.
	public static float m_torque2 = 40.0f;
	public static float m_hz1 = 2.5f; // Suspension dampening ratio. Underdamped <1> Overdamped
	public static float m_hz2 = 2.5f;
*/
	public static Object[] currentGenes = {0, 6e+10, 0.0, 0, 0.4f, -40.0f, 40.0f, 2.5f, 0.4f, -40.0f, 40.0f, 2.5f};
	public static Object[] bestGenes = {0, 6e+10, 0.0, 0, 0.4f, -40.0f, 40.0f, 2.5f, 0.4f, -40.0f, 40.0f, 2.5f};
	
	@Override
	public void initTest(boolean deserialized) {
		if (deserialized) {
			return;
		}
		stopwatch = StopWatch.createStarted();
		hasFin = false;
		launch();
	}

	public void launch() {
		
	// World
		BodyDef bd = new BodyDef();
		Body ground = m_world.createBody(bd);
		Body goal = m_world.createBody(bd);
		EdgeShape shape = new EdgeShape();

		FixtureDef fd = new FixtureDef();
		fd.shape = shape;
		fd.density = 0.0f;
		fd.friction = 0.7f;

/*	//Straight Map 
		shape.set(new Vec2(-20.0f, 0.0f), new Vec2(200.0f, 0.0f));
		ground.createFixture(fd);
*/	
	// TestMap
		// Start line
		shape.set(new Vec2(-5.0f, 0.0f), new Vec2(-5.0f, 10.0f));
		ground.createFixture(fd);
		// Straight
		shape.set(new Vec2(-5.0f, 0.0f), new Vec2(80.0f, 0.0f));
		ground.createFixture(fd);
		// Sudden Incline
		shape.set(new Vec2(80.0f, 0.0f), new Vec2(81.5f, 1.5f));
		ground.createFixture(fd);
		// Straight
		shape.set(new Vec2(81.5f, 1.5f), new Vec2(119.0f, 1.5f));
		ground.createFixture(fd);
		// Sudden Decline
		shape.set(new Vec2(119.0f, 1.5f), new Vec2(120.5f, 0.0f));
		ground.createFixture(fd);
		// Straight
		shape.set(new Vec2(120.5f, 0.0f), new Vec2(160.0f, 0.0f));
		ground.createFixture(fd);
		// Big Decline
		shape.set(new Vec2(160.0f, 0.0f), new Vec2(164.0f, -7.0f));
		ground.createFixture(fd);
		// Straight
		shape.set(new Vec2(164.0f, -7.0f), new Vec2(200.0f, -6.0f));
		ground.createFixture(fd);
		// Big Incline
		shape.set(new Vec2(200.0f, -6.0f), new Vec2(205.0f, 0.0f));
		ground.createFixture(fd);
		// Slight Decline
		shape.set(new Vec2(205.0f, 0.0f), new Vec2(225.0f, -1.0f));
		ground.createFixture(fd);
		// Biggest Incline
		shape.set(new Vec2(225.0f, -1.0f), new Vec2(250.0f, 20.0f));
		ground.createFixture(fd);
		// Last Stretch
		shape.set(new Vec2(250.0f, 20.0f), new Vec2(265.0f, 20.0f));
		ground.createFixture(fd);

	// "Point B" - To touch to start new iteration.
		PolygonShape box = new PolygonShape();
		box.setAsBox(0.2f, 0.2f);
		// box.setAsBox(0.2f, 0.2f, new Vec2(198.0f, 0.75f), 0); //For Straight Map
		box.setAsBox(0.2f, 0.2f, new Vec2(264.0f, 20.75f), 0);

		FixtureDef fd2 = new FixtureDef();
		fd2.shape = box;
		fd2.isSensor = true;
		m_sensor = goal.createFixture(fd2);
		m_sensor.setUserData(fd2);

	// Car body
		PolygonShape chassis = new PolygonShape();
		Vec2 vertices[] = new Vec2[8]; // Num of vectors in shape, 6 = chassis + 1 per wheel
		vertices[0] = new Vec2(-1.5f, -0.5f); // Bottom chassis 1
		vertices[1] = new Vec2(1.5f, -0.5f); // Bottom chassis 2
		vertices[2] = new Vec2(1.5f, 0.0f); // front bumper
		vertices[3] = new Vec2(0.5f, 0.5f); // Windshield
		vertices[4] = new Vec2(-0.5f, 0.5f); // roof
		vertices[5] = new Vec2(-1.5f, 0.0f); // trunk
		chassis.set(vertices, 6);

	// Wheels
		CircleShape circle1 = new CircleShape();
		circle1.m_radius = (float) currentGenes[4];
		CircleShape circle2 = new CircleShape();
		circle2.m_radius = (float) currentGenes[8];

	// Create and position car body
		bd.type = BodyType.DYNAMIC;
		bd.position.set(0.0f, 1.0f);
		m_car = m_world.createBody(bd);
		m_car.createFixture(chassis, 1.0f);
		m_car.setUserData(fd);

	// Create wheel 1
		fd.shape = circle1;
		fd.density = 1.0f;
		fd.friction = 1.0f;

	// Create wheel 2;
		fd.shape = circle2;
		fd.density = 1.0f;
		fd.friction = 1.0f;

	// position wheel 1
		bd.position.set(-1.0f, 0.35f);
		m_wheel1 = m_world.createBody(bd);
		m_wheel1.createFixture(fd);

	// position wheel 2
		bd.position.set(1.0f, 0.4f);
		m_wheel2 = m_world.createBody(bd);
		m_wheel2.createFixture(fd);

	// Create Wheel Joint
		jd = new WheelJointDef();
		Vec2 axis = new Vec2(0.0f, 1.0f);

	// Define joint 1
		jd.initialize(m_car, m_wheel1, m_wheel1.getPosition(), axis);
		jd.enableMotor = true;
		jd.motorSpeed = (float) currentGenes[5];
		jd.maxMotorTorque = (float) currentGenes[6];
		jd.frequencyHz = (float) currentGenes[7];
		jd.dampingRatio = m_zeta;
		m_spring1 = (WheelJoint) m_world.createJoint(jd);

	// Define joint 2
		jd.initialize(m_car, m_wheel2, m_wheel2.getPosition(), axis);
		jd.enableMotor = true;
		jd.motorSpeed = (float) currentGenes[9];
		jd.maxMotorTorque = (float) currentGenes[10];
		jd.frequencyHz = (float) currentGenes[7];
		jd.dampingRatio = m_zeta;
		m_spring2 = (WheelJoint) m_world.createJoint(jd);
	}

	@Override
	public float getDefaultCameraScale() {
		return 15;
	}

	@SuppressWarnings("deprecation")
	@Override
	// Call parent class to run and step forward animation.
	public synchronized void step(TestbedSettings settings) {
		super.step(settings);
		getCamera().setCamera(m_car.getPosition());
		
		// if contact has been made with the goal node or 1.5 times the currentBest time
		// has passed...
		if (hasFin == true || stopwatch.getNanoTime() > new Double(CarTest.currentGenes[1].toString()) * 1.5) {
						
			stopwatch.stop();
			
			if (stopwatch.getNanoTime() > new Double(CarTest.currentGenes[1].toString()) * 1.5) { //If did not complete course
				currentGenes[2] = new Double(CarTest.currentGenes[1].toString()) * 1.5; //Replace recorded time
			}
			else if (hasFin == true && stopwatch.getNanoTime() < new Double(CarTest.currentGenes[1].toString())) { //If higher fitness
				currentGenes[1] = stopwatch.getNanoTime(); //Replace best time
				bestTimeVisualizer = stopwatch.toString();
				
				int evolved = (int) currentGenes[3]; //iterate evolution
				currentGenes[3] = evolved+1;
				
				for (int i= 0; i < currentGenes.length - 1; i++) { //make current genes best genes
					bestGenes[i] = currentGenes[i];
				}
			}

			m_car.getWorld().destroyBody(m_car);
			m_wheel1.getWorld().destroyBody(m_wheel1);
			m_wheel2.getWorld().destroyBody(m_wheel2);
			
			int v = (int) bestGenes[0];
			bestGenes[0] = v + 1;
			//v+1 works but v++ doesn't????
			currentGenes = Evolver.evolver(bestGenes);
			
			initTest(false);
		}
	}

	// Detects collisions between car and goal
	public void beginContact(Contact cp) {
		Fixture fixtureA = cp.getFixtureA();
		Fixture fixtureB = cp.getFixtureB();

		if (fixtureA == m_sensor) {
			Object userData = fixtureB.getBody().getUserData();
			if (userData != null) {
				hasFin = true;
			}
		}

		if (fixtureB == m_sensor) {
			Object userData = fixtureA.getBody().getUserData();
			if (userData != null) {
				hasFin = true;
			}
		}
	}

	@Override
	public String getTestName() {
		return "CarTest";
	}

}