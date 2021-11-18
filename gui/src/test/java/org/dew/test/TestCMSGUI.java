package org.dew.test;

import org.dew.swingup.ResourcesMgr;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public 
class TestCMSGUI extends TestCase 
{
  public TestCMSGUI(String testName) {
    super(testName);
  }
  
  public static Test suite() {
    return new TestSuite(TestCMSGUI.class);
  }
  
  public void testApp() {
    System.out.println(ResourcesMgr.sPREFIX + " build " + ResourcesMgr.sBUILD);
  }
}
