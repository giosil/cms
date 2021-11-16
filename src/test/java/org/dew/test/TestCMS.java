package org.dew.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestCMS extends TestCase {
  
  public TestCMS(String testName) {
    super(testName);
  }
  
  public static Test suite() {
    return new TestSuite(TestCMS.class);
  }
  
  public void testApp() throws Exception {
  }
  
}
