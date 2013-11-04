package fi.solita.founder;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.transport.PropertyScope;
import org.mule.construct.Flow;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.junit4.FunctionalTestCase;

import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;

/**
 * @author jaakkosj / Solita Oy<br>
 *         Last changed by: $LastChangedBy: jaakkosj $
 */
public class ClojureExpressionTransformerTest extends FunctionalTestCase {

    public static final String MUMBOJUMBO = "Mumbojumbo!";

    private class MuleEventBuilder {
        private MuleEvent event;
        private MuleMessage message;

        private MuleEventBuilder() throws Exception {
            this("foobar");
        }

        private MuleEventBuilder(Object payload) throws Exception {
            this.event = AbstractMuleContextTestCase.getTestEvent(payload);
            this.message = event.getMessage();
        }

        private MuleEventBuilder setPayload(Object payload) {
            message.setPayload(payload);
            return this;
        }


        private MuleEvent build() {
            return event;
        }

        private MuleEventBuilder setProperties(Map<String, Object> properties, PropertyScope propertyScope) {
            for (Entry<String, Object> entry : properties.entrySet()) {
                message.setProperty(entry.getKey(), entry.getValue(), propertyScope);
            }
            return this;
        }

        private MuleEventBuilder setInvocationProperties(Map<String,Object> properties) {
            return setProperties(properties, PropertyScope.INVOCATION);
        }

        public MuleEventBuilder setSessionProperties(Map<String, Object> properties) {
            return setProperties(properties, PropertyScope.SESSION);
        }

        public MuleEventBuilder setOutboundProperties(Map<String, Object> properties) {
            return setProperties(properties, PropertyScope.OUTBOUND);
        }

        public MuleEventBuilder setInboundProperties(ImmutableMap<String, Object> properties) {
            return setProperties(properties, PropertyScope.INBOUND);
        }
    }

    private MuleEventBuilder MEB;

    @Before
    public void setUp() throws Exception {
        MEB = new MuleEventBuilder();
    }

    @Test
    public void testReturnSimpleValue() throws Exception {
        runFlowAndExpect("value-flow", MEB, 123456L);
    }

    @Test
    public void testUsingMessageObject() throws Exception {
        String expect = MUMBOJUMBO;
        runFlowAndExpect("use-message-flow", MEB.setPayload(expect), expect);
    }

    @Test
    public void testUsingInvocationVariables() throws Exception {
        String expect = MUMBOJUMBO;
        ImmutableMap<String, Object> properties = ImmutableMap.of("flow-test-property", (Object) expect);
        runFlowAndExpect("use-flow-vars-flow", MEB.setInvocationProperties(properties), expect);
    }

    @Test
    public void testUsingSessionVariable() throws Exception {
        String expect = MUMBOJUMBO;
        ImmutableMap<String, Object> properties = ImmutableMap.of("session-test-property", (Object) expect);
        runFlowAndExpect("use-session-vars-flow", MEB.setSessionProperties(properties), expect);
    }

    @Test
    public void testUsingOutboundProperty() throws Exception {
        String expect = MUMBOJUMBO;
        ImmutableMap<String, Object> properties = ImmutableMap.of("outbound-test-property", (Object) expect);
        runFlowAndExpect("use-outbound-vars-flow", MEB.setOutboundProperties(properties), expect);
    }

    @Test
    public void testUsingInboundProperty() throws Exception {
        String expect = MUMBOJUMBO;
        ImmutableMap<String, Object> properties = ImmutableMap.of("inbound-test-property", (Object) expect);
        runFlowAndExpect("use-inbound-vars-flow", MEB.setInboundProperties(properties), expect);
    }

    protected <T> void runFlowAndExpect(String flowName, MuleEventBuilder eventBuilder, T expect) throws Exception {
        Flow flow = lookupFlowConstruct(flowName);
        MuleEvent result = flow.process(eventBuilder.build());
        assertEquals(expect, result.getMessage().getPayload());
    }

    /**
     * Retrieve a flow by name from the registry
     *
     * @param name Name of the flow to retrieve
     */
    protected Flow lookupFlowConstruct(String name)
    {
        return (Flow) AbstractMuleContextTestCase.muleContext.getRegistry().lookupFlowConstruct(name);
    }


    @Override
    protected String getConfigResources() {
        return "mule-config.xml";
    }
}
