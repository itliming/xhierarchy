package com.rltx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.visitor.ReferenceFilter;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.support.QueueProcessingManager;
import spoon.support.reflect.declaration.CtMethodImpl;
import spoon.support.reflect.reference.CtExecutableReferenceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodExecutionProcessor extends AbstractProcessor<CtMethodImpl> {
    private Map<CtExecutableReference, List<CtExecutableReference>> calleeList = new HashMap<>();
    private Map<CtExecutableReference, List<CtExecutableReference>> callerList = new HashMap<>();
    private static Logger logger = LoggerFactory.getLogger(MethodExecutionProcessor.class);

    @Override
    public void process(CtMethodImpl ctMethod) {

        List<CtElement> elements = ctMethod.getElements(new AbstractFilter<CtElement>(CtElement.class) {
            @Override
            public boolean matches(CtElement ctElement) {
                return ctElement instanceof CtAbstractInvocation;
            }
        });
        List<CtExecutableReference> callees = new ArrayList<>();
        for (CtElement element : elements) {
            CtAbstractInvocation invocation = (CtAbstractInvocation) element;
            callees.add(invocation.getExecutable());
            // 组装被调用的方法集合
            if (!callerList.containsKey(invocation.getExecutable())) {
                List<CtExecutableReference> callers = new ArrayList<>();
                callerList.put(invocation.getExecutable(), callers);
            }

            List<CtExecutableReference> ctExecutableReferences = callerList.get(invocation.getExecutable());
            if (!ctExecutableReferences.contains(ctMethod.getReference())) {
                ctExecutableReferences.add(ctMethod.getReference());
            }
        }

        calleeList.put(ctMethod.getReference(), callees);
    }


    void executeSpoon(QueueProcessingManager queueProcessingManager) throws Exception {
        queueProcessingManager.addProcessor(this);
        queueProcessingManager.process();
        logger.debug("Method calls: " + calleeList);
    }

    public Map<CtExecutableReference, List<CtExecutableReference>> getCalleeList() {
        return calleeList;
    }

    public Map<CtExecutableReference, List<CtExecutableReference>> getCallerList() {
        return callerList;
    }
}

