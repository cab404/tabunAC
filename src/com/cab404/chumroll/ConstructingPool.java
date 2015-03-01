package com.cab404.chumroll;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple creator pool
 *
 * @author cab404
 */
public abstract class ConstructingPool<Type> {

    protected Map<Class<? extends Type>, Type> instances = new HashMap<Class<? extends Type>, Type>();

    /**
     * Creates a new instance of given class.
     */
    protected abstract Type makeInstance(Class<? extends Type> clazz);

    /**
     * Forcibly puts given instance as new instance of its class.
     */
    public void enforceInstance(Type instance) {
        @SuppressWarnings("unchecked")
        Class<? extends Type> clazz = (Class<? extends Type>) instance.getClass();

        instances.put(clazz, instance);
    }

    /**
     * Returns previously created instance of given class or creates new one and returns it.
     */
    public Type getInstance(Class<? extends Type> clazz) {
        if (instances.containsKey(clazz))
            return instances.get(clazz);
        Type instance = makeInstance(clazz);

        instances.put(clazz, instance);
        return instance;
    }

}
