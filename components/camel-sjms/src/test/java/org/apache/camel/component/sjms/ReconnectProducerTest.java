/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.sjms;

import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.TextMessage;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.sjms.support.JmsTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledIfSystemProperty(named = "activemq.instance.type", matches = "remote",
                          disabledReason = "Requires control of ActiveMQ, so it can only run locally (embedded or container)")
public class ReconnectProducerTest extends JmsTestSupport {

    private static final String TEST_DESTINATION_NAME = "sync.queue.producer.test.ReconnectProducerTest";

    @Override
    protected boolean useJmx() {
        return false;
    }

    @Test
    public void testInOnlyQueueProducer() throws Exception {
        MessageConsumer mc = createQueueConsumer(TEST_DESTINATION_NAME);
        assertNotNull(mc);
        final String expectedBody = "Hello World!";
        MockEndpoint mock = getMockEndpoint("mock:result");

        mock.expectedMessageCount(2);
        mock.expectedBodiesReceived(expectedBody, expectedBody);

        template.sendBody("direct:start", expectedBody);
        Message message = mc.receive(5000);
        assertNotNull(message);
        assertTrue(message instanceof TextMessage);

        TextMessage tm = (TextMessage) message;
        String text = tm.getText();
        assertNotNull(text);
        template.sendBody("direct:finish", text);

        reconnect(10000);

        mc = createQueueConsumer(TEST_DESTINATION_NAME);
        template.sendBody("direct:start", expectedBody);
        message = mc.receive(5000);
        assertNotNull(message);
        assertTrue(message instanceof TextMessage);

        tm = (TextMessage) message;
        text = tm.getText();
        assertNotNull(text);

        template.sendBody("direct:finish", text);

        mock.assertIsSatisfied();
        mc.close();

    }

    /**
     * @return
     * @see    org.apache.camel.test.junit5.CamelTestSupport#createRouteBuilder()
     */
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                        .to("sjms:queue:" + TEST_DESTINATION_NAME + "?concurrentConsumers=10");

                from("direct:finish")
                        .to("log:test.log.1?showBody=true", "mock:result");
            }
        };
    }

}
