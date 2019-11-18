package net.torommo.logspy.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TestObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestObject.class);

    public void doSomething() {
        LOGGER.info("Something was done.");
    }
}
