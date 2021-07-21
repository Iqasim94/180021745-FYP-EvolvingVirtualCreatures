package org.jbox2d.testbed.framework.j2d;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.jbox2d.common.Vec2;
import org.jbox2d.testbed.framework.AbstractTestbedController;
import org.jbox2d.testbed.framework.TestbedCamera.ZoomType;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedTest;

public class AWTPanelHelper {
  static boolean screenDragButtonDown = false;
  static boolean mouseJointButtonDown = false;

  /**
   * Adds common help text and listeners for awt-based testbeds.
   */
  public static void addHelpAndPanelListeners(Component panel, final TestbedModel model,
      final AbstractTestbedController controller, final int screenDragButton) {
    final Vec2 oldDragMouse = new Vec2();
    final Vec2 mouse = new Vec2();

    panel.addMouseWheelListener(new MouseWheelListener() {
      public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        TestbedTest currTest = model.getCurrTest();
        if (currTest == null) {
          return;
        }
        ZoomType zoom = notches < 0 ? ZoomType.ZOOM_IN : ZoomType.ZOOM_OUT;
        currTest.getCamera().zoomToPoint(mouse, zoom);
      }
    });

    panel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent arg0) {
        if (arg0.getButton() == screenDragButton) {
          screenDragButtonDown = false;
        } else {
          if (arg0.getButton() == TestbedTest.MOUSE_JOINT_BUTTON) {
            mouseJointButtonDown = false;
          }
          controller.queueMouseUp(new Vec2(arg0.getX(), arg0.getY()), arg0.getButton());
        }
      }

      @Override
      public void mousePressed(MouseEvent arg0) {
        if (arg0.getButton() == screenDragButton) {
          screenDragButtonDown = true;
          oldDragMouse.set(arg0.getX(), arg0.getY());
          return;
        } else {
          if (arg0.getButton() == TestbedTest.MOUSE_JOINT_BUTTON) {
            mouseJointButtonDown = true;
          }
          controller.queueMouseDown(new Vec2(arg0.getX(), arg0.getY()), arg0.getButton());
        }
      }
    });

    panel.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent arg0) {
        mouse.set(arg0.getX(), arg0.getY());
        controller.queueMouseMove(new Vec2(mouse));
      }

      @Override
      public void mouseDragged(MouseEvent arg0) {
        mouse.set(arg0.getX(), arg0.getY());
        if (screenDragButtonDown) {
          TestbedTest currTest = model.getCurrTest();
          if (currTest == null) {
            return;
          }
          Vec2 diff = oldDragMouse.sub(mouse);
          currTest.getCamera().moveWorld(diff);
          oldDragMouse.set(mouse);
        } else if (mouseJointButtonDown) {
          controller.queueMouseDrag(new Vec2(mouse), TestbedTest.MOUSE_JOINT_BUTTON);
        } else {
          controller.queueMouseDrag(new Vec2(mouse), arg0.getButton());
        }
      }
    });

    panel.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent arg0) {
        controller.queueKeyReleased(arg0.getKeyChar(), arg0.getKeyCode());
      }

      @Override
      public void keyPressed(KeyEvent arg0) {
        char c = arg0.getKeyChar();
        controller.queueKeyPressed(c, arg0.getKeyCode());
        switch (c) {
          case 'r':
            controller.reset();
            break;
          case 'p':
            controller.queuePause();
            break;
        }
      }
    });
  }
}
