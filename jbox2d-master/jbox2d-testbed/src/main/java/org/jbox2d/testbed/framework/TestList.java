/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
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

import org.jbox2d.testbed.tests.ApplyForce;
import org.jbox2d.testbed.tests.BodyTypes;
import org.jbox2d.testbed.tests.Breakable;
import org.jbox2d.testbed.tests.BulletTest;
import org.jbox2d.testbed.tests.Car;
import org.jbox2d.testbed.tests.CarTest;
import org.jbox2d.testbed.tests.CharacterCollision;
import org.jbox2d.testbed.tests.ConvexHull;
import org.jbox2d.testbed.tests.ConveyorBelt;
import org.jbox2d.testbed.tests.DistanceTest;
import org.jbox2d.testbed.tests.EdgeShapes;
import org.jbox2d.testbed.tests.SliderCrankTest;
import org.jbox2d.testbed.tests.TheoJansen;
import org.jbox2d.testbed.tests.WaveMachine;

/**
 * @author Daniel Murphy
 */
public class TestList {

  public static void populateModel(TestbedModel model) {
	model.addTest(new CarTest());
	model.addTest(new WaveMachine());
    model.addTest(new Car());
    model.addTest(new TheoJansen());
    model.addTest(new ConveyorBelt());
    model.addTest(new Breakable());
    model.addTest(new BodyTypes());
    model.addTest(new ApplyForce());
    model.addTest(new SliderCrankTest());
    model.addTest(new EdgeShapes());
    model.addTest(new ConvexHull());
    model.addTest(new DistanceTest());
    
    model.addTest(new BulletTest());
    model.addTest(new CharacterCollision());
  }
}
