package com.tuempresa.ecommerce.orders.infrastructure.web.interceptor;

import com.tuempresa.ecommerce.orders.domain.port.in.ServiceLogUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID_ATTRIBUTE = "orders-trace-id";

    private final ServiceLogUseCase serviceLogUseCase;

    public RequestLoggingInterceptor(ServiceLogUseCase serviceLogUseCase) {
        this.serviceLogUseCase = serviceLogUseCase;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = UUID.randomUUID().toString();
        request.setAttribute(TRACE_ID_ATTRIBUTE, traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        Object traceObj = request.getAttribute(TRACE_ID_ATTRIBUTE);
        String traceId = traceObj != null ? traceObj.toString() : UUID.randomUUID().toString();
        String endpoint = request.getRequestURI();
        String method = request.getMethod();
        int status = response.getStatus();
        String message = ex != null ? ex.getMessage() : "Request completed";
        serviceLogUseCase.log(traceId, endpoint, method, status, message);
    }
}


