package com.hrms.security.config;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import java.util.function.Supplier;

/**
 * Wraps the default expression root in {@link SuperAdminExpressionRoot} so SUPER_ADMIN bypasses
 * fine-grained authority checks. Reuses the parent's evaluation context (bean resolver, type
 * converters, etc.) and only swaps the root object for our delegating wrapper.
 */
public class SuperAdminMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    @Override
    public EvaluationContext createEvaluationContext(Supplier<Authentication> authentication, MethodInvocation mi) {
        StandardEvaluationContext ctx = (StandardEvaluationContext) super.createEvaluationContext(authentication, mi);
        Object root = ctx.getRootObject().getValue();
        if (root instanceof MethodSecurityExpressionOperations ops) {
            ctx.setRootObject(new SuperAdminExpressionRoot(ops));
        }
        return ctx;
    }
}
