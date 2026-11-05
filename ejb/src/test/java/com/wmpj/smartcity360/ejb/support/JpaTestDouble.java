package com.wmpj.smartcity360.ejb.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JpaTestDouble {
    public final List<String> jpql = new ArrayList<>();
    public final Map<String, Object> parameters = new HashMap<>();
    public final Map<String, Object> finds = new HashMap<>();
    public List<?> results = List.of();
    public Object singleResult = 0L;
    public Object merged;
    public Object persisted;
    public Object removed;
    public int firstResult = -1;
    public int maxResults = -1;
    public int flushes;

    public EntityManager entityManager() {
        return proxy(EntityManager.class, (method, arguments, self) -> switch (method) {
            case "find" -> finds.get(((Class<?>) arguments[0]).getName() + ":" + arguments[1]);
            case "createQuery" -> {
                jpql.add((String) arguments[0]);
                yield typedQuery();
            }
            case "persist" -> {
                persisted = arguments[0];
                yield null;
            }
            case "merge" -> {
                merged = arguments[0];
                yield arguments[0];
            }
            case "remove" -> {
                removed = arguments[0];
                yield null;
            }
            case "flush" -> {
                flushes++;
                yield null;
            }
            default -> defaultValue(self.getReturnType());
        });
    }

    public void findResult(Class<?> type, Object id, Object value) {
        finds.put(type.getName() + ":" + id, value);
    }

    private TypedQuery<?> typedQuery() {
        return proxy(TypedQuery.class, (method, arguments, self) -> switch (method) {
            case "setParameter" -> {
                parameters.put(String.valueOf(arguments[0]), arguments[1]);
                yield proxySelf(self);
            }
            case "setFirstResult" -> {
                firstResult = (Integer) arguments[0];
                yield proxySelf(self);
            }
            case "setMaxResults" -> {
                maxResults = (Integer) arguments[0];
                yield proxySelf(self);
            }
            case "getResultList" -> results;
            case "getSingleResult" -> singleResult;
            default -> defaultValue(self.getReturnType());
        });
    }

    private static Object proxySelf(java.lang.reflect.Method method) {
        return CURRENT_PROXY.get();
    }

    private static final ThreadLocal<Object> CURRENT_PROXY = new ThreadLocal<>();

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (instance, method, arguments) -> {
            CURRENT_PROXY.set(instance);
            try {
                return invocation.invoke(method.getName(), arguments == null ? new Object[0] : arguments, method);
            } finally {
                CURRENT_PROXY.remove();
            }
        });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        return 0;
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(String method, Object[] arguments, java.lang.reflect.Method reflectedMethod);
    }
}
