/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.jbox2d.testbed.framework;

import java.util.LinkedList;

import org.apache.commons.lang3.time.StopWatch;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.callbacks.DestructionListener;
import org.jbox2d.callbacks.ParticleDestructionListener;
import org.jbox2d.callbacks.ParticleQueryCallback;
import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.Collision;
import org.jbox2d.collision.Collision.PointState;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.serialization.JbDeserializer;
import org.jbox2d.serialization.JbDeserializer.ObjectListener;
import org.jbox2d.serialization.JbSerializer;
import org.jbox2d.serialization.JbSerializer.ObjectSigner;
import org.jbox2d.serialization.UnsupportedListener;
import org.jbox2d.serialization.UnsupportedObjectException;
import org.jbox2d.serialization.pb.PbDeserializer;
import org.jbox2d.serialization.pb.PbSerializer;
import org.jbox2d.testbed.tests.CarTest;

/**
 * @author Daniel Murphy
 */
public abstract class TestbedTest implements ContactListener, ObjectListener, ObjectSigner, UnsupportedListener {
	public static final int MAX_CONTACT_POINTS = 4048;
	public static final float ZOOM_SCALE_DIFF = .05f;
	public static final int TEXT_LINE_SPACE = 13;
	public static final int TEXT_SECTION_SPACE = 3;
	public static final int MOUSE_JOINT_BUTTON = 1;

	protected static final long GROUND_BODY_TAG = 1897450239847L;
	protected static final long MOUSE_JOINT_TAG = 4567893364789L;

	public final ContactPoint[] points = new ContactPoint[MAX_CONTACT_POINTS];

	/**
	 * Only visible for compatibility. Should use {@link #getWorld()} instead.
	 */
	protected World m_world;
	protected Body groundBody;
	private MouseJoint mouseJoint;

	protected boolean mouseTracing;
	private Vec2 mouseTracerPosition = new Vec2();
	private Vec2 mouseTracerVelocity = new Vec2();

	private final Vec2 mouseWorld = new Vec2();
	private int pointCount;
	private int stepCount;

	private TestbedModel model;
	protected DestructionListener destructionListener;
	protected ParticleDestructionListener particleDestructionListener;

	private String title = null;
	protected int m_textLine;
	private final LinkedList<String> textList = new LinkedList<String>();

	private TestbedCamera camera;

	private JbSerializer serializer;
	private JbDeserializer deserializer;

	private final Transform identity = new Transform();

	public StopWatch elapsedTimer;

	public TestbedTest() {
		identity.setIdentity();
		for (int i = 0; i < MAX_CONTACT_POINTS; i++) {
			points[i] = new ContactPoint();
		}
		serializer = new PbSerializer(this, new SignerAdapter(this) {
			@Override
			public Long getTag(Body argBody) {
				if (isSaveLoadEnabled()) {
					return GROUND_BODY_TAG;
				}
				return super.getTag(argBody);
			}

			@Override
			public Long getTag(Joint argJoint) {
				if (isSaveLoadEnabled()) {
					if (argJoint == mouseJoint) {
						return MOUSE_JOINT_TAG;
					}
				}
				return super.getTag(argJoint);
			}
		});
		deserializer = new PbDeserializer(this, new ListenerAdapter(this) {
			@Override
			public void processBody(Body argBody, Long argTag) {
				if (isSaveLoadEnabled()) {
					groundBody = argBody;
					return;
				}
				super.processBody(argBody, argTag);
			}

			@Override
			public void processJoint(Joint argJoint, Long argTag) {
				if (isSaveLoadEnabled()) {
					if (argTag == MOUSE_JOINT_TAG) {
						mouseJoint = (MouseJoint) argJoint;
						return;
					}
				}
				super.processJoint(argJoint, argTag);
			}
		});
		destructionListener = new DestructionListener() {
			public void sayGoodbye(Fixture fixture) {
				fixtureDestroyed(fixture);
			}

			public void sayGoodbye(Joint joint) {
				if (mouseJoint == joint) {
					mouseJoint = null;
				} else {
					jointDestroyed(joint);
				}
			}
		};

		particleDestructionListener = new ParticleDestructionListener() {
			@Override
			public void sayGoodbye(int index) {
				particleDestroyed(index);
			}

			@Override
			public void sayGoodbye(ParticleGroup group) {
				particleGroupDestroyed(group);
			}
		};
		camera = new TestbedCamera(getDefaultCameraPos(), getDefaultCameraScale(), ZOOM_SCALE_DIFF);
	}

	public void init(TestbedModel model) {

		elapsedTimer = StopWatch.createStarted();
		this.model = model;

		Vec2 gravity = new Vec2(0, -10f);
		m_world = model.getWorldCreator().createWorld(gravity);
		m_world.setParticleGravityScale(0.4f);
		m_world.setParticleDensity(1.2f);
		mouseJoint = null;

		mouseTracing = false;
		mouseTracerPosition.setZero();
		mouseTracerVelocity.setZero();

		BodyDef bodyDef = new BodyDef();
		groundBody = m_world.createBody(bodyDef);

		init(m_world, false);
	}

	public void init(World world, boolean deserialized) {
		m_world = world;
		pointCount = 0;
		stepCount = 0;
		model.getDebugDraw().setViewportTransform(camera.getTransform());

		world.setDestructionListener(destructionListener);
		world.setParticleDestructionListener(particleDestructionListener);
		world.setContactListener(this);
		world.setDebugDraw(model.getDebugDraw());
		title = getTestName();
		initTest(deserialized);
	}

	protected JbSerializer getSerializer() {
		return serializer;
	}

	protected JbDeserializer getDeserializer() {
		return deserializer;
	}

	/**
	 * Gets the current world
	 */
	public World getWorld() {
		return m_world;
	}

	/**
	 * Gets the testbed model
	 */
	public TestbedModel getModel() {
		return model;
	}

	/**
	 * Gets the contact points for the current test
	 */
	public ContactPoint[] getContactPoints() {
		return points;
	}

	/**
	 * Gets the ground body of the world, used for some joints
	 */
	public Body getGroundBody() {
		return groundBody;
	}

	/**
	 * Gets the debug draw for the testbed
	 */
	public DebugDraw getDebugDraw() {
		return model.getDebugDraw();
	}

	/**
	 * Gets the world position of the mouse
	 */
	public Vec2 getWorldMouse() {
		return mouseWorld;
	}

	public int getStepCount() {
		return stepCount;
	}

	/**
	 * The number of contact points we're storing
	 */
	public int getPointCount() {
		return pointCount;
	}

	public TestbedCamera getCamera() {
		return camera;
	}

	/**
	 * Override for a different default camera position
	 */
	public Vec2 getDefaultCameraPos() {
		return new Vec2(0, 20);
	}

	/**
	 * Override for a different default camera scale
	 */
	public float getDefaultCameraScale() {
		return 10;
	}

	public boolean isMouseTracing() {
		return mouseTracing;
	}

	public Vec2 getMouseTracerPosition() {
		return mouseTracerPosition;
	}

	public Vec2 getMouseTracerVelocity() {
		return mouseTracerVelocity;
	}

	/**
	 * Gets the filename of the current test. Default implementation uses the test
	 * name with no spaces".
	 */
	public String getFilename() {
		return getTestName().toLowerCase().replaceAll(" ", "_") + ".box2d";
	}

	/** @deprecated use {@link #getCamera()} */
	public void setCamera(Vec2 argPos) {
		camera.setCamera(argPos);
	}

	/** @deprecated use {@link #getCamera()} */
	public void setCamera(Vec2 argPos, float scale) {
		camera.setCamera(argPos, scale);
	}

	/**
	 * Initializes the current test.
	 * 
	 * @param deserialized if the test was deserialized from a file. If so, all
	 *                     physics objects are already added.
	 */
	public abstract void initTest(boolean deserialized);

	/**
	 * The name of the test
	 */
	public abstract String getTestName();

	/**
	 * Adds a text line to the reporting area
	 */
	public void addTextLine(String line) {
		textList.add(line);
	}

	/**
	 * called when the tests exits
	 */
	public void exit() {
	}

	private final Color3f color1 = new Color3f(.3f, .95f, .3f);
	private final Color3f color2 = new Color3f(.3f, .3f, .95f);
	private final Color3f color3 = new Color3f(.9f, .9f, .9f);
	private final Color3f color4 = new Color3f(.6f, .61f, 1);
	private final Color3f color5 = new Color3f(.9f, .9f, .3f);
	private final Color3f mouseColor = new Color3f(0f, 1f, 0f);
	private final Vec2 p1 = new Vec2();
	private final Vec2 p2 = new Vec2();
	private final Vec2 tangent = new Vec2();
	private final Vec2 acceleration = new Vec2();
	private final CircleShape pshape = new CircleShape();
	private final ParticleVelocityQueryCallback pcallback = new ParticleVelocityQueryCallback();
	private final AABB paabb = new AABB();

	@SuppressWarnings("deprecation")
	public void step(TestbedSettings settings) {
		float hz = settings.getSetting(TestbedSettings.Hz).value;
		float timeStep = hz > 0f ? 1f / hz : 0;
		if (settings.singleStep && !settings.pause) {
			settings.pause = true;
		}

		final DebugDraw debugDraw = model.getDebugDraw();
		m_textLine = 20;

		if (title != null) {
			debugDraw.drawString(camera.getTransform().getExtents().x, 15, title, Color3f.WHITE);
			m_textLine += TEXT_LINE_SPACE;
		}

		if (settings.pause) {
			if (settings.singleStep) {
				settings.singleStep = false;
			} else {
				timeStep = 0;
			}

			debugDraw.drawString(5, m_textLine, "****PAUSED****", Color3f.WHITE);
			m_textLine += TEXT_LINE_SPACE;
		}

		int flags = 0;
		flags += settings.getSetting(TestbedSettings.DrawShapes).enabled ? DebugDraw.e_shapeBit : 0;
		flags += settings.getSetting(TestbedSettings.DrawJoints).enabled ? DebugDraw.e_jointBit : 0;
		flags += settings.getSetting(TestbedSettings.DrawAABBs).enabled ? DebugDraw.e_aabbBit : 0;
		flags += settings.getSetting(TestbedSettings.DrawCOMs).enabled ? DebugDraw.e_centerOfMassBit : 0;
		flags += settings.getSetting(TestbedSettings.DrawTree).enabled ? DebugDraw.e_dynamicTreeBit : 0;
		flags += settings.getSetting(TestbedSettings.DrawWireframe).enabled ? DebugDraw.e_wireframeDrawingBit : 0;
		debugDraw.setFlags(flags);

		m_world.setAllowSleep(settings.getSetting(TestbedSettings.AllowSleep).enabled);
		m_world.setWarmStarting(settings.getSetting(TestbedSettings.WarmStarting).enabled);
		m_world.setSubStepping(settings.getSetting(TestbedSettings.SubStepping).enabled);
		m_world.setContinuousPhysics(settings.getSetting(TestbedSettings.ContinuousCollision).enabled);

		pointCount = 0;

		m_world.step(timeStep, settings.getSetting(TestbedSettings.VelocityIterations).value,
				settings.getSetting(TestbedSettings.PositionIterations).value);

		m_world.drawDebugData();

		if (timeStep > 0f) {
			++stepCount;
		}

		debugDraw.drawString(5, m_textLine, "Info", color4);
		m_textLine += TEXT_LINE_SPACE;
		
		if (settings.pause && !elapsedTimer.isSuspended()) {
			elapsedTimer.suspend();
			CarTest.stopwatch.suspend();
		}
		if (!settings.pause && elapsedTimer.isSuspended()) {
			elapsedTimer.resume();
			CarTest.stopwatch.resume();
		}
				
		// Display timer on screen.
		debugDraw.drawString(5, m_textLine, "Elapsed Time: " + elapsedTimer, Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE;
		// Display timer on screen.
		debugDraw.drawString(5, m_textLine, "Time to Beat: " + CarTest.bestTimeVisualizer, Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE; //BestTime
				
		if (CarTest.stopwatch.getNanoTime() < new Double(CarTest.currentGenes[1].toString())) {
			debugDraw.drawString(5, m_textLine, "Current Time: " + CarTest.stopwatch.toString(), Color3f.WHITE);
			m_textLine += TEXT_LINE_SPACE;
		} else {
			debugDraw.drawString(5, m_textLine, "Current Time: " + CarTest.stopwatch.toString(), Color3f.RED);
			m_textLine += TEXT_LINE_SPACE;	
		}

	//Display Genes
		debugDraw.drawString(5, m_textLine, "", Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE;
		debugDraw.drawString(5, m_textLine, "Run: " + CarTest.run, Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE; //Iteration
		debugDraw.drawString(5, m_textLine, "Evolution: " + CarTest.bestGenes[3], Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE; //Evolutions
		debugDraw.drawString(5, m_textLine, "Num of non-evolutions: " + CarTest.failedRun, Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE; //Non-evo patterns
			
		debugDraw.drawString(5, m_textLine, "", Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE;
		debugDraw.drawString(5, m_textLine, "Wheel 1: ", Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE; //Header
		debugDraw.drawString(5, m_textLine, "Wheel Radius: " + CarTest.bestGenes[4] + "f", Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE; //Wheel size 1
		debugDraw.drawString(5, m_textLine, "Wheel Speed: " + CarTest.bestGenes[5] + "f", Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE; //Speed 1
		debugDraw.drawString(5, m_textLine, "Torque: " + CarTest.bestGenes[6] + "f", Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE; //Torque 1
		debugDraw.drawString(5, m_textLine, "Suspension Stiffness: " + CarTest.bestGenes[7] + "f", Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE; //Suspension Dampening 1

		debugDraw.drawString(5, m_textLine, "", Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE;
		debugDraw.drawString(5, m_textLine, "Wheel 2: ", Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE; //Header 2
		debugDraw.drawString(5, m_textLine, "Wheel Radius: " + CarTest.bestGenes[8] + "f", Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE; //Wheel size 2
		debugDraw.drawString(5, m_textLine, "Wheel Speed: " + CarTest.bestGenes[9] + "f", Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE; //Speed 2
		debugDraw.drawString(5, m_textLine, "Torque: " + CarTest.bestGenes[10] + "f", Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE; //Torque 2
		debugDraw.drawString(5, m_textLine, "Suspension Stiffness: " + CarTest.bestGenes[11] + "f", Color3f.WHITE);
		m_textLine += TEXT_LINE_SPACE; //Suspension Dampening 2	
		
		/*
		 * debugDraw.drawString(5, m_textLine, "Best Time: " + (int) model.getBestFps(),
		 * Color3f.WHITE); m_textLine += TEXT_LINE_SPACE; debugDraw.drawString(5,
		 * m_textLine, "Variance: " + ((int) model.getBestFps() - (int)
		 * model.getCalculatedFps()),
		 * 
		 * if ((int) model.getCalculatedFps() > (int) model.getBestFps()) { Color3F.RED
		 * } else { Color3F.GREEN }); m_textLine += TEXT_LINE_SPACE;
		 */

		/*
		 * if (settings.getSetting(TestbedSettings.DrawStats).enabled) { int
		 * particleCount = m_world.getParticleCount(); int groupCount =
		 * m_world.getParticleGroupCount(); debugDraw.drawString( 5, m_textLine,
		 * "bodies/contacts/joints/proxies/particles/groups = " + m_world.getBodyCount()
		 * + "/" + m_world.getContactCount() + "/" + m_world.getJointCount() + "/" +
		 * m_world.getProxyCount() + "/" + particleCount + "/" + groupCount,
		 * Color3f.WHITE); m_textLine += TEXT_LINE_SPACE;
		 * 
		 * debugDraw.drawString(5, m_textLine, "World mouse position: " +
		 * mouseWorld.toString(), Color3f.WHITE); m_textLine += TEXT_LINE_SPACE;
		 * 
		 * 
		 * statsList.clear(); Profile p = getWorld().getProfile();
		 * p.toDebugStrings(statsList);
		 * 
		 * for (String s : statsList) { debugDraw.drawString(5, m_textLine, s,
		 * Color3f.WHITE); m_textLine += TEXT_LINE_SPACE; } m_textLine +=
		 * TEXT_SECTION_SPACE; }
		 
		if (!textList.isEmpty()) {

			debugDraw.drawString(5, m_textLine, "World mouse position: " + mouseWorld.toString(), Color3f.WHITE);
			m_textLine += TEXT_LINE_SPACE;

			for (String s : textList) {
				debugDraw.drawString(5, m_textLine, s, Color3f.WHITE);
				m_textLine += TEXT_LINE_SPACE;
			}
			textList.clear();
		}

		if (mouseTracing && mouseJoint == null) {
			float delay = 0.1f;
			acceleration.x = 2 / delay * (1 / delay * (mouseWorld.x - mouseTracerPosition.x) - mouseTracerVelocity.x);
			acceleration.y = 2 / delay * (1 / delay * (mouseWorld.y - mouseTracerPosition.y) - mouseTracerVelocity.y);
			mouseTracerVelocity.x += timeStep * acceleration.x;
			mouseTracerVelocity.y += timeStep * acceleration.y;
			mouseTracerPosition.x += timeStep * mouseTracerVelocity.x;
			mouseTracerPosition.y += timeStep * mouseTracerVelocity.y;
			pshape.m_p.set(mouseTracerPosition);
			pshape.m_radius = 2;
			pcallback.init(m_world, pshape, mouseTracerVelocity);
			pshape.computeAABB(paabb, identity, 0);
			m_world.queryAABB(pcallback, paabb);
		}

		if (mouseJoint != null) {
			mouseJoint.getAnchorB(p1);
			Vec2 p2 = mouseJoint.getTarget();

			debugDraw.drawSegment(p1, p2, mouseColor);
		}
*/
		if (settings.getSetting(TestbedSettings.DrawContactPoints).enabled) {
			final float k_impulseScale = 0.1f;
			final float axisScale = 0.3f;

			for (int i = 0; i < pointCount; i++) {

				ContactPoint point = points[i];

				if (point.state == PointState.ADD_STATE) {
					debugDraw.drawPoint(point.position, 10f, color1);
				} else if (point.state == PointState.PERSIST_STATE) {
					debugDraw.drawPoint(point.position, 5f, color2);
				}

				if (settings.getSetting(TestbedSettings.DrawContactNormals).enabled) {
					p1.set(point.position);
					p2.set(point.normal).mulLocal(axisScale).addLocal(p1);
					debugDraw.drawSegment(p1, p2, color3);

				} else if (settings.getSetting(TestbedSettings.DrawContactImpulses).enabled) {
					p1.set(point.position);
					p2.set(point.normal).mulLocal(k_impulseScale).mulLocal(point.normalImpulse).addLocal(p1);
					debugDraw.drawSegment(p1, p2, color5);
				}

				if (settings.getSetting(TestbedSettings.DrawFrictionImpulses).enabled) {
					Vec2.crossToOutUnsafe(point.normal, 1, tangent);
					p1.set(point.position);
					p2.set(tangent).mulLocal(k_impulseScale).mulLocal(point.tangentImpulse).addLocal(p1);
					debugDraw.drawSegment(p1, p2, color5);
				}
			}
		}
	}

	/************ INPUT ************/

	/**
	 * Called for mouse-up
	 */
	public void mouseUp(Vec2 p, int button) {
		mouseTracing = false;
		if (button == MOUSE_JOINT_BUTTON) {
			destroyMouseJoint();
		}
	}

	public void keyPressed(char keyChar, int keyCode) {
	}

	public void keyReleased(char keyChar, int keyCode) {
	}

	public void mouseDown(Vec2 p, int button) {
		mouseWorld.set(p);
		mouseTracing = true;
		mouseTracerVelocity.setZero();
		mouseTracerPosition.set(p);

		if (button == MOUSE_JOINT_BUTTON) {
			spawnMouseJoint(p);
		}
	}

	public void mouseMove(Vec2 p) {
		mouseWorld.set(p);
	}

	public void mouseDrag(Vec2 p, int button) {
		mouseWorld.set(p);
		if (button == MOUSE_JOINT_BUTTON) {
			updateMouseJoint(p);
		}
	}

	/************ MOUSE JOINT ************/

	private final AABB queryAABB = new AABB();
	private final TestQueryCallback callback = new TestQueryCallback();

	private void spawnMouseJoint(Vec2 p) {
		if (mouseJoint != null) {
			return;
		}
		queryAABB.lowerBound.set(p.x - .001f, p.y - .001f);
		queryAABB.upperBound.set(p.x + .001f, p.y + .001f);
		callback.point.set(p);
		callback.fixture = null;
		m_world.queryAABB(callback, queryAABB);

		if (callback.fixture != null) {
			Body body = callback.fixture.getBody();
			MouseJointDef def = new MouseJointDef();
			def.bodyA = groundBody;
			def.bodyB = body;
			def.collideConnected = true;
			def.target.set(p);
			def.maxForce = 1000f * body.getMass();
			mouseJoint = (MouseJoint) m_world.createJoint(def);
			body.setAwake(true);
		}
	}

	private void updateMouseJoint(Vec2 target) {
		if (mouseJoint != null) {
			mouseJoint.setTarget(target);
		}
	}

	private void destroyMouseJoint() {
		if (mouseJoint != null) {
			m_world.destroyJoint(mouseJoint);
			mouseJoint = null;
		}
	}

	/************ SERIALIZATION *************/

	/**
	 * Override to enable saving and loading. Remember to also override the
	 * {@link ObjectListener} and {@link ObjectSigner} methods if you need to
	 * 
	 * @return
	 */
	public boolean isSaveLoadEnabled() {
		return false;
	}

	@Override
	public Long getTag(Body body) {
		return null;
	}

	@Override
	public Long getTag(Fixture fixture) {
		return null;
	}

	@Override
	public Long getTag(Joint joint) {
		return null;
	}

	@Override
	public Long getTag(Shape shape) {
		return null;
	}

	@Override
	public Long getTag(World world) {
		return null;
	}

	@Override
	public void processBody(Body body, Long tag) {
	}

	@Override
	public void processFixture(Fixture fixture, Long tag) {
	}

	@Override
	public void processJoint(Joint joint, Long tag) {
	}

	@Override
	public void processShape(Shape shape, Long tag) {
	}

	@Override
	public void processWorld(World world, Long tag) {
	}

	@Override
	public boolean isUnsupported(UnsupportedObjectException exception) {
		return true;
	}

	public void fixtureDestroyed(Fixture fixture) {
	}

	public void jointDestroyed(Joint joint) {
	}

	public void beginContact(Contact contact) {
	}

	public void endContact(Contact contact) {
	}

	public void particleDestroyed(int particle) {
	}

	public void particleGroupDestroyed(ParticleGroup group) {
	}

	public void postSolve(Contact contact, ContactImpulse impulse) {
	}

	private final PointState[] state1 = new PointState[Settings.maxManifoldPoints];
	private final PointState[] state2 = new PointState[Settings.maxManifoldPoints];
	private final WorldManifold worldManifold = new WorldManifold();

	public void preSolve(Contact contact, Manifold oldManifold) {
		Manifold manifold = contact.getManifold();

		if (manifold.pointCount == 0) {
			return;
		}

		Fixture fixtureA = contact.getFixtureA();
		Fixture fixtureB = contact.getFixtureB();

		Collision.getPointStates(state1, state2, oldManifold, manifold);

		contact.getWorldManifold(worldManifold);

		for (int i = 0; i < manifold.pointCount && pointCount < MAX_CONTACT_POINTS; i++) {
			ContactPoint cp = points[pointCount];
			cp.fixtureA = fixtureA;
			cp.fixtureB = fixtureB;
			cp.position.set(worldManifold.points[i]);
			cp.normal.set(worldManifold.normal);
			cp.state = state2[i];
			cp.normalImpulse = manifold.points[i].normalImpulse;
			cp.tangentImpulse = manifold.points[i].tangentImpulse;
			cp.separation = worldManifold.separations[i];
			++pointCount;
		}
	}
}

class TestQueryCallback implements QueryCallback {

	public final Vec2 point;
	public Fixture fixture;

	public TestQueryCallback() {
		point = new Vec2();
		fixture = null;
	}

	public boolean reportFixture(Fixture argFixture) {
		Body body = argFixture.getBody();
		if (body.getType() == BodyType.DYNAMIC) {
			boolean inside = argFixture.testPoint(point);
			if (inside) {
				fixture = argFixture;

				return false;
			}
		}

		return true;
	}
}

class ParticleVelocityQueryCallback implements ParticleQueryCallback {
	World world;
	Shape shape;
	Vec2 velocity;
	final Transform xf = new Transform();

	public ParticleVelocityQueryCallback() {
		xf.setIdentity();
	}

	public void init(World world, Shape shape, Vec2 velocity) {
		this.world = world;
		this.shape = shape;
		this.velocity = velocity;
	}

	@Override
	public boolean reportParticle(int index) {
		Vec2 p = world.getParticlePositionBuffer()[index];
		if (shape.testPoint(xf, p)) {
			Vec2 v = world.getParticleVelocityBuffer()[index];
			v.set(velocity);
		}
		return true;
	}
}

class SignerAdapter implements ObjectSigner {
	private final ObjectSigner delegate;

	public SignerAdapter(ObjectSigner argDelegate) {
		delegate = argDelegate;
	}

	public Long getTag(World argWorld) {
		return delegate.getTag(argWorld);
	}

	public Long getTag(Body argBody) {
		return delegate.getTag(argBody);
	}

	public Long getTag(Shape argShape) {
		return delegate.getTag(argShape);
	}

	public Long getTag(Fixture argFixture) {
		return delegate.getTag(argFixture);
	}

	public Long getTag(Joint argJoint) {
		return delegate.getTag(argJoint);
	}
}

class ListenerAdapter implements ObjectListener {
	private final ObjectListener listener;

	public ListenerAdapter(ObjectListener argListener) {
		listener = argListener;
	}

	public void processWorld(World argWorld, Long argTag) {
		listener.processWorld(argWorld, argTag);
	}

	public void processBody(Body argBody, Long argTag) {
		listener.processBody(argBody, argTag);
	}

	public void processFixture(Fixture argFixture, Long argTag) {
		listener.processFixture(argFixture, argTag);
	}

	public void processShape(Shape argShape, Long argTag) {
		listener.processShape(argShape, argTag);
	}

	public void processJoint(Joint argJoint, Long argTag) {
		listener.processJoint(argJoint, argTag);
	}
}
