package org.jbox2d.testbed.tests;

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
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;

/**
 * @author Ishmail Qasim
 *
 *         Making a car init
 */

public class CarTest extends TestbedTest {

	private int frame;
	//Initialization
	private static final long CAR_TAG = 100l;
	private static final long WHEEL1_TAG = 101l;
	private static final long WHEEL2_TAG = 102l;
	private static final long SPRING1_TAG = 103l;
	private static final long SPRING2_TAG = 104l;

	private Body m_car;
	private Body m_wheel1;
	private Body m_wheel2;
	
	/**
	 * 	m_hz = Suspension damping ratio: The tightness of suspension springs.
	 * 			Reduces bounce and adds stiffness.
	 * 			Underdamped < 1 > Overdamped.
	 */
	private float m_hz;
	
	private float m_zeta; //Damping Ratio: Resistive force reactionary friction.
	private float m_speed; //Rotational torque of the wheels.
	private WheelJoint m_spring1;
	private WheelJoint m_spring2;
	
	@Override
	public void processBody(Body body, Long tag) {
		if (tag == CAR_TAG) {
			m_car = body;
		} else if (tag == WHEEL1_TAG) {
			m_wheel1 = body;
		} else if (tag == WHEEL2_TAG) {
			m_wheel2 = body;
		} else {
			super.processBody(body, tag);
		}
	}

	@Override
	public void processJoint(Joint joint, Long tag) {
		if (tag == SPRING1_TAG) {
			m_spring1 = (WheelJoint) joint;
		} else if (tag == SPRING2_TAG) {
			m_spring2 = (WheelJoint) joint;
		} else {
			super.processJoint(joint, tag);
		}
	}
	
	@Override
	public String getTestName() {
		return "CarTest";
	}

	@Override
	public void initTest(boolean deserialized) {
		if (deserialized) {
			return;
		}

		m_hz = 4.0f;
		m_zeta = 0.7f;
		m_speed = 5.0f;

		//world
		Body ground = null;
		{
			BodyDef bd = new BodyDef();
			ground = m_world.createBody(bd);

			EdgeShape shape = new EdgeShape();

			FixtureDef fd = new FixtureDef();
			fd.shape = shape;
			fd.density = 0.0f;
			fd.friction = 0.6f;

			shape.set(new Vec2(-20.0f, 0.0f), new Vec2(75.0f, 0.0f));
			ground.createFixture(fd);
			
			shape.set(new Vec2(75.0f, 0.0f), new Vec2(100.0f, 10.0f));
			ground.createFixture(fd);

			shape.set(new Vec2(100.0f, 10.0f), new Vec2(125.0f, 0.0f));
			ground.createFixture(fd);
			
			shape.set(new Vec2(125.0f, 0.0f), new Vec2(200.0f, 0.0f));
			ground.createFixture(fd);
			
			shape.set(new Vec2(200.0f, 0.0f), new Vec2(200.0f, 50.0f));
			ground.createFixture(fd);
		}

		// Car
		{
			//The body of the car
			PolygonShape chassis = new PolygonShape();
			Vec2 vertices[] = new Vec2[8];			//Num of vectors in shape, 6 = chassis + 1 per wheel
			vertices[0] = new Vec2(-1.5f, -0.5f); 	//Bottom chassis 1
			vertices[1] = new Vec2(1.5f, -0.5f); 	//Bottom chassis 2
			vertices[2] = new Vec2(1.5f, 0.0f); 	//front bumper
			vertices[3] = new Vec2(0.5f, 0.5f); 	//Windshield
			vertices[4] = new Vec2(-0.5f, 0.5f); 	//roof			
			vertices[5] = new Vec2(-1.5f, 0.0f); 	//trunk
			chassis.set(vertices, 6);

			//The wheel
			CircleShape circle = new CircleShape();
			circle.m_radius = 0.4f;
						
			//Create and position car body
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DYNAMIC;
			bd.position.set(0.0f, 1.0f);
			m_car = m_world.createBody(bd);
			m_car.createFixture(chassis, 1.0f);

			//Create wheels
			FixtureDef fd = new FixtureDef();
			fd.shape = circle;
			fd.density = 1.0f;
			fd.friction = 0.9f;

			//position wheel 1
			bd.position.set(-1.0f, 0.35f);
			m_wheel1 = m_world.createBody(bd);
			m_wheel1.createFixture(fd);

			//position wheel 2
			bd.position.set(1.0f, 0.4f);
			m_wheel2 = m_world.createBody(bd);
			m_wheel2.createFixture(fd);		
			
			//Join the wheel to chassis
			WheelJointDef jd = new WheelJointDef();
			Vec2 axis = new Vec2(0.0f, 1.0f);

			//Create and define wheel 1
			jd.initialize(m_car, m_wheel1, m_wheel1.getPosition(), axis);
			jd.motorSpeed = -m_speed;
			jd.maxMotorTorque = 200.0f;
			jd.enableMotor = true;
			jd.frequencyHz = m_hz;
			jd.dampingRatio = m_zeta;
			m_spring1 = (WheelJoint) m_world.createJoint(jd);

			//Create and define wheel 2
			jd.initialize(m_car, m_wheel2, m_wheel2.getPosition(), axis);
			jd.motorSpeed = -m_speed;
			jd.maxMotorTorque = 200.0f;
			jd.enableMotor = true;
			jd.frequencyHz = m_hz;
			jd.dampingRatio = m_zeta;
			m_spring2 = (WheelJoint) m_world.createJoint(jd);
		}
	}

	@Override
	public float getDefaultCameraScale() {
		return 15;
	}

	@Override
	public synchronized void step(TestbedSettings settings) {
		super.step(settings);
		getCamera().setCamera(m_car.getPosition());
	}

}
