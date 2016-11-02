package com.rltx.method;

import java.lang.reflect.Method;
import java.util.List;

public class CallHierarchyMethod {

    private Method method; // 方法

    private List<CallHierarchyMethod> calleeMethods; // 调用的方法

    private List<CallHierarchyMethod> callerMethods; // 被哪些方法调用

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public List<CallHierarchyMethod> getCalleeMethods() {
        return calleeMethods;
    }

    public void setCalleeMethods(List<CallHierarchyMethod> calleeMethods) {
        this.calleeMethods = calleeMethods;
    }

    public List<CallHierarchyMethod> getCallerMethods() {
        return callerMethods;
    }

    public void setCallerMethods(List<CallHierarchyMethod> callerMethods) {
        this.callerMethods = callerMethods;
    }
}
