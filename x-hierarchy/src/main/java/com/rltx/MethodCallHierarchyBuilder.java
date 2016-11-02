package com.rltx;

import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.io.PrintStream;
import java.util.*;

public class MethodCallHierarchyBuilder {
    private final CtExecutableReference executableReference;
    private final Map<CtExecutableReference, List<CtExecutableReference>> callList;
    private final Map<CtExecutableReference, List<CtExecutableReference>> callerList;
    private final Map<CtTypeReference, Set<CtTypeReference>> classHierarchy;

    private MethodCallHierarchyBuilder(CtExecutableReference executableReference,
                                       Map<CtExecutableReference, List<CtExecutableReference>> callList,
                                       Map<CtExecutableReference, List<CtExecutableReference>> callerList,
                                       Map<CtTypeReference, Set<CtTypeReference>> classHierarchy) {
        this.executableReference = executableReference;
        this.callList = callList;
        this.callerList = callerList;
        this.classHierarchy = classHierarchy;
    }

    public static CtExecutableReference forExecutableReferenceMethodName(String methodName,
                                                                         Map<CtExecutableReference, List<CtExecutableReference>> callList
    ) {
        CtExecutableReference executableReference = findExecutablesForMethodName(methodName, callList);
        return executableReference;
    }

    public static MethodCallHierarchyBuilder forMethodName(String methodName,
                                                           Map<CtExecutableReference, List<CtExecutableReference>> callList,
                                                           Map<CtExecutableReference, List<CtExecutableReference>> callerList,
                                                           Map<CtTypeReference, Set<CtTypeReference>> classHierarchy) {
        CtExecutableReference executableReference = findExecutablesForMethodName(methodName, callList);
        if (executableReference == null) {
            return null;
        }
        return new MethodCallHierarchyBuilder(executableReference, callList, callerList, classHierarchy);
    }

    public static CtExecutableReference findExecutablesForMethodName(String methodName, Map<CtExecutableReference, List<CtExecutableReference>> callList) {
        ArrayList<CtExecutableReference> result = new ArrayList<>();
        for (CtExecutableReference executableReference : callList.keySet()) {
            String executableReferenceMethodName = executableReference.getDeclaringType().getQualifiedName() + "." + executableReference.getSimpleName();
            if (executableReferenceMethodName.equals(methodName)) {
//                    || executableReference.toString().contains(methodName)
//                    || executableReference.toString().matches(methodName)) {
                return executableReference;
            }
        }
        return null;
    }

    public String getCallerHierarchy() {
        StringBuilder sb = new StringBuilder();
        getCallerHierarchy(executableReference, sb, "----", new HashSet<CtExecutableReference>());
        return sb.toString();
    }

    public void getCallerHierarchy(CtExecutableReference method, StringBuilder sb, String indents, Set<CtExecutableReference> alreadyVisited) {
        if (alreadyVisited.contains(method)) {
            return;
        }
        alreadyVisited.add(method);
        List<CtExecutableReference> callerListForMethod = callerList.get(method);
        if (callerListForMethod == null) {
            Set superInterfaces = method.getDeclaringType().getSuperInterfaces();
            for (Object o : superInterfaces) {
                CtTypeReference superclass = (CtTypeReference) o;
                Collection<CtExecutableReference> declaredExecutables = superclass.getDeclaredExecutables();
                Iterator iterator = declaredExecutables.iterator();
                while (iterator.hasNext()) {
                    CtExecutableReference superRef = (CtExecutableReference) iterator.next();
                    CtExecutableReference overridingExecutable = superRef.getOverridingExecutable(method.getDeclaringType());
                    if (overridingExecutable.equals(method)) {
                        callerListForMethod = callerList.get(superRef);
                    }
                    if (callerListForMethod != null) {
//                        printStream.println(indents + superRef.toString());
                        break;
                    }
                }

            }

            if (callerListForMethod == null) {
                return;
            }

        }
        for (CtExecutableReference eachReference : callerListForMethod) {
            if (!(eachReference.toString().contains("xtailor.controller") || eachReference.toString().contains("xtailor.service") || eachReference.toString().contains("xtailor.dao"))) {
                continue;
            }
            sb.append(indents + eachReference.toString() + "\n");

            getCallerHierarchy(eachReference, sb, indents.concat("----"), alreadyVisited);
            Set<CtTypeReference> subclasses = classHierarchy.get(eachReference.getDeclaringType());
            if (subclasses != null) {
                for (CtTypeReference subclass : subclasses) {
                    CtExecutableReference reference = eachReference.getOverridingExecutable(subclass);
                    if (reference != null) {
                        sb.append(indents + "* " + reference.toString() + "\n");
                        getCallerHierarchy(eachReference, sb, indents.concat("----"), alreadyVisited);
                    }
                }
            }
        }
    }

    public void printCalleeHierarchy(PrintStream printStream) {
        printStream.println("Method call hierarchy callees of " + executableReference + "");
        printCalleeHierarchy(printStream, executableReference, "\t", new HashSet<CtExecutableReference>());
    }

    private void printCalleeHierarchy(PrintStream printStream, CtExecutableReference method, String indents, Set<CtExecutableReference> alreadyVisited) {
        if (alreadyVisited.contains(method)) {
            return;
        }
        alreadyVisited.add(method);
        List<CtExecutableReference> callListForMethod = callList.get(method);
        if (callListForMethod == null) {
            return;
        }
        for (CtExecutableReference eachReference : callListForMethod) {
            if (!(eachReference.toString().contains("xtailor.controller") || eachReference.toString().contains("xtailor.service") || eachReference.toString().contains("xtailor.dao"))) {
                continue;
            }
            printStream.println(indents + eachReference.toString());

            printCalleeHierarchy(printStream, eachReference, indents.concat("\t"), alreadyVisited);
            Set<CtTypeReference> subclasses = classHierarchy.get(eachReference.getDeclaringType());
            if (subclasses != null) {
                for (CtTypeReference subclass : subclasses) {
                    CtExecutableReference reference = eachReference.getOverridingExecutable(subclass);
                    if (reference != null) {
                        printStream.println(indents + "* " + reference.toString());
                        printCalleeHierarchy(printStream, reference, indents.concat("\t"), alreadyVisited);
                    }
                }
            }
        }
    }

    public void printCallerHierarchy(PrintStream printStream) {
        printStream.println("Method caller hierarchy callers of " + executableReference + "");
        printCallerHierarchy(printStream, executableReference, "\t", new HashSet<CtExecutableReference>());
    }

    private void printCallerHierarchy(PrintStream printStream, CtExecutableReference method, String indents, Set<CtExecutableReference> alreadyVisited) {
        if (alreadyVisited.contains(method)) {
            return;
        }
        alreadyVisited.add(method);
        List<CtExecutableReference> callerListForMethod = callerList.get(method);
        if (callerListForMethod == null) {
            Set superInterfaces = method.getDeclaringType().getSuperInterfaces();
            for (Object o : superInterfaces) {
                CtTypeReference superclass = (CtTypeReference) o;
                Collection<CtExecutableReference> declaredExecutables = superclass.getDeclaredExecutables();
                Iterator iterator = declaredExecutables.iterator();
                while (iterator.hasNext()) {
                    CtExecutableReference superRef = (CtExecutableReference) iterator.next();
                    CtExecutableReference overridingExecutable = superRef.getOverridingExecutable(method.getDeclaringType());
                    if (overridingExecutable.equals(method)) {
                        callerListForMethod = callerList.get(superRef);
                    }
                    if (callerListForMethod != null) {
//                        printStream.println(indents + superRef.toString());
                        break;
                    }
                }

            }

            if (callerListForMethod == null) {
                return;
            }

        }
        for (CtExecutableReference eachReference : callerListForMethod) {
            if (!(eachReference.toString().contains("xtailor.controller") || eachReference.toString().contains("xtailor.service") || eachReference.toString().contains("xtailor.dao"))) {
                continue;
            }
            printStream.println(indents + eachReference.toString());

            printCallerHierarchy(printStream, eachReference, indents.concat("\t"), alreadyVisited);
            Set<CtTypeReference> subclasses = classHierarchy.get(eachReference.getDeclaringType());
            if (subclasses != null) {
                for (CtTypeReference subclass : subclasses) {
                    CtExecutableReference reference = eachReference.getOverridingExecutable(subclass);
                    if (reference != null) {
                        printStream.println(indents + "* " + reference.toString());
                        printCallerHierarchy(printStream, reference, indents.concat("\t"), alreadyVisited);
                    }
                }
            }
        }
    }

    private boolean isParameterEq(List parameterTypes, List parameterTypes1) {
        return true;
    }


}
