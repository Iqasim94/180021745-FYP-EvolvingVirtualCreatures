package org.jbox2d.testbed.tests;

import org.apache.commons.lang3.time.StopWatch;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.WheelJoint;
import org.jbox2d.dynamics.joints.WheelJointDef;
import org.jbox2d.testbed.framework.AbstractTestbedController;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;

/**
 * @author Ishmail Qasim
 *
 *         Making a car init
 */

public class CarTest extends TestbedTest {

	// Initialization
	protected Body ground;
	protected Body m_car;
	public CircleShape circle1; //Wheel shape
	public Body m_wheel1; //Wheel object
	public CircleShape circle2;
	public Body m_wheel2;
	
	public Body goal;
	
	protected float m_zeta = 0.7f; // Damping Ratio: Resistive force reactionary friction.
	
	protected WheelJoint m_spring1;
	protected WheelJoint m_spring2;

	protected PolygonShape chassis;
	protected BodyDef bd;
	protected FixtureDef fd;
	protected WheelJointDef jd;
	
	/*
	 * Record/Gene instantiations
	 */
	public Object[] Record; //Record List
	public int run; //Which iteration
//	public StopWatch oldBest = 99:59:59.999; //Fitness to compete against
	public StopWatch recordedTime; //Fitness of genetics
	public int evolutions; //Number of beneficial evolutions
	public float wheelSize1;
	public float wheelSize2;
	public boolean wheel1Enabled;
	public boolean wheel2Enabled;
	public float m_speed = -40.0f; //Max rotational speed of the wheel.
//	public float m_speed1;
//	public float m_speed2;
//	public float m_torque1; //maximum torque available for the wheel/Rate of Acceleration.
//	public float m_torque2;
	public float m_hz = 4.0f; //Suspension dampening ratio. Underdamped < 1 > Overdamped
//	public float m_hz1;
//	public float m_hz2;
	

	
	@Override
	public void initTest(boolean deserialized) {
		if (deserialized) {
			return;
		}
		
		launch();
	}

	public void launch() {

		// world
			bd = new BodyDef();
			ground = m_world.createBody(bd);
			goal = m_world.createBody(bd);

			EdgeShape shape = new EdgeShape();

			fd = new FixtureDef();
			fd.shape = shape;
			fd.density = 0.0f;
			fd.friction = 0.7f;

/*			//Straight
			shape.set(new Vec2(-20.0f, 0.0f), new Vec2(75.0f, 0.0f));
			ground.createFixture(fd);

			//Easy Incline
			shape.set(new Vec2(75.0f, 0.0f), new Vec2(100.0f, 10.0f));
			ground.createFixture(fd);

			//Decline
			shape.set(new Vec2(100.0f, 10.0f), new Vec2(125.0f, 0.0f));
			ground.createFixture(fd);

			//Straight
			shape.set(new Vec2(125.0f, 0.0f), new Vec2(200.0f, 0.0f));
			ground.createFixture(fd);

			//Wall
			//shape.set(new Vec2(200.0f, 0.0f), new Vec2(200.0f, 50.0f));
			//ground.createFixture(fd);
*/
			shape.set(new Vec2(-20.0f, 0.0f), new Vec2(200.0f, 0.0f));
			ground.createFixture(fd);
			
			
		//Contact point - To touch to start new iteration.						
			PolygonShape box = new PolygonShape();
			box.setAsBox(0.2f, 0.2f);
			goal.createFixture(box, 1.0f);
			goal.setTransform(new Vec2(10.0f, 0.75f), 0.0f); //pos of box
			
			
		// The body of the car
			chassis = new PolygonShape();
			Vec2 vertices[] = new Vec2[8]; // Num of vectors in shape, 6 = chassis + 1 per wheel
			vertices[0] = new Vec2(-1.5f, -0.5f); // Bottom chassis 1
			vertices[1] = new Vec2(1.5f, -0.5f); // Bottom chassis 2
			vertices[2] = new Vec2(1.5f, 0.0f); // front bumper
			vertices[3] = new Vec2(0.5f, 0.5f); // Windshield
			vertices[4] = new Vec2(-0.5f, 0.5f); // roof
			vertices[5] = new Vec2(-1.5f, 0.0f); // trunk
			chassis.set(vertices, 6);

		// The wheel
			circle1 = new CircleShape();
			circle1.m_radius = 0.4f;
			
			circle2 = new CircleShape();
			circle2.m_radius = 0.4f;

			// Create and position car body
			bd.type = BodyType.DYNAMIC;
			bd.position.set(0.0f, 1.0f);
			m_car = m_world.createBody(bd);
			m_car.createFixture(chassis, 1.0f);

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
			jd.motorSpeed = m_speed;
			jd.maxMotorTorque = 1.0f;
			jd.enableMotor = true;
			jd.frequencyHz = m_hz;
			jd.dampingRatio = m_zeta;
			m_spring1 = (WheelJoint) m_world.createJoint(jd);

			// Define joint 2
			jd.initialize(m_car, m_wheel2, m_wheel2.getPosition(), axis);
			jd.motorSpeed = m_speed;
			jd.maxMotorTorque = 1.0f;
			jd.enableMotor = true;
			jd.frequencyHz = m_hz;
			jd.dampingRatio = m_zeta;
			m_spring2 = (WheelJoint) m_world.createJoint(jd);
	}

	@Override
	public float getDefaultCameraScale() {
		return 15;
	}

	@Override
	public synchronized void step(TestbedSettings settings) {
		super.step(settings);
		getCamera().setCamera(m_car.getPosition());
		
//		if (m_car.getPosition() == goal.getPosition()) {
		if (getStepCount() % 200 == 0) {
			
			/*
			 * Eliminates previous car
			 */
			m_car.getWorld().destroyBody(m_car);
			m_wheel1.getWorld().destroyBody(m_wheel1);
			m_wheel2.getWorld().destroyBody(m_wheel2);
			
			initTest(false);
		}
	}

	@Override
	public String getTestName() {
		return "CarTest";
	}

}