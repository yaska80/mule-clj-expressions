package fi.solita.founder;

import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.mule.api.MuleMessage;
import org.mule.api.expression.ExpressionEvaluator;

/**
 * @author jaakkosj / Solita Oy<br>
 *         Last changed by: $LastChangedBy: jaakkosj $
 */
public class ClojureExpressionEvaluator implements ExpressionEvaluator {
    @Override
    public Object evaluate(String expression, MuleMessage message) {
        String namespace = "ClojureExpressionEvaluator-" + Math.random();

        String format = String.format("(ns %s)(defn script [message flow-vars session-vars outbound inbound] %s)", namespace, expression);
        RT.var("clojure.core", "load-string").invoke(format);

        Builder<Keyword,Object> flowVarBuilder = ImmutableMap.builder();

        for (String propName : message.getInvocationPropertyNames()) {
            flowVarBuilder.put(RT.keyword(namespace, propName), message.getInvocationProperty(propName));
        }

        Builder<Keyword,Object> sessionVarBuilder = ImmutableMap.builder();
        for (String propName : message.getSessionPropertyNames()) {
            sessionVarBuilder.put(RT.keyword(namespace, propName), message.getSessionProperty(propName));
        }

        Builder<Keyword,Object> outboundBuilder = ImmutableMap.builder();
        for (String propName : message.getOutboundPropertyNames()) {
            outboundBuilder.put(RT.keyword(namespace, propName), message.getOutboundProperty(propName));
        }

        Builder<Keyword,Object> inboundBuilder = ImmutableMap.builder();
        for (String propName : message.getInboundPropertyNames()) {
            inboundBuilder.put(RT.keyword(namespace, propName), message.getInboundProperty(propName));
        }

        return RT.var(namespace, "script")
                .invoke(
                        message,
                        PersistentArrayMap.create(flowVarBuilder.build()),
                        PersistentArrayMap.create(sessionVarBuilder.build()),
                        PersistentArrayMap.create(outboundBuilder.build()),
                        PersistentArrayMap.create(inboundBuilder.build()));
    }

    @Override
    public String getName() {
        return "clojure";
    }

}
