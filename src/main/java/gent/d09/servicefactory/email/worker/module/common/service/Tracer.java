package gent.d09.servicefactory.email.worker.module.common.service;

import org.slf4j.MDC;

import java.util.UUID;

public class Tracer {
    public static final String X_CORRELATION_ID = "correlationId";
    public static final String X_SPAN_ID = "X-Span-Id";

    public static void setCorrelationId(String correlationId){
        if(correlationId == null || correlationId.isEmpty()){
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(X_CORRELATION_ID, correlationId);
    }

    public static void setSpanId(){
        MDC.put(X_SPAN_ID, UUID.randomUUID().toString());
    }

    public static String getCorrelationId(){
        return MDC.get(X_CORRELATION_ID);
    }

    public static String getSpanId(){
        return MDC.get(X_SPAN_ID);
    }
}
