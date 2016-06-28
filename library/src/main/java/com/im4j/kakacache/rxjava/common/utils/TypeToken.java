/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.im4j.kakacache.rxjava.common.utils;

import com.im4j.kakacache.rxjava.common.exception.ArgumentException;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * Represents a generic type {@code T}. Java doesn't yet provide a way to
 * represent generic types, so this class does. Forces clients to create a
 * subclass of this class which enables retrieval the type information even at
 * runtime.
 *
 * <p>For example, to create a type literal for {@code List<String>}, you can
 * create an empty anonymous inner class:
 *
 * <p>
 * {@code TypeToken<List<String>> list = new TypeToken<List<String>>() {};}
 *
 * <p>This syntax cannot be used to create type literals that have wildcard
 * parameters, such as {@code Class<?>} or {@code List<? extends CharSequence>}.
 *
 * @author Bob Lee
 * @author Sven Mawson
 * @author Jesse Wilson
 */
public class TypeToken<T> {
    final Class<? super T> rawType;
    final Type type;
    final int hashCode;

    /**
     * Constructs a new type literal. Derives represented class from type
     * parameter.
     *
     * <p>Clients create an empty anonymous subclass. Doing so embeds the type
     * parameter in the anonymous class's type hierarchy so we can reconstitute it
     * at runtime despite erasure.
     */
    @SuppressWarnings("unchecked")
    protected TypeToken() {
        this.type = getSuperclassTypeParameter(getClass());
        this.rawType = (Class<? super T>) $Gson$Types.getRawType(type);
        this.hashCode = type.hashCode();
    }

    /**
     * Unsafe. Constructs a type literal manually.
     */
    @SuppressWarnings("unchecked")
    TypeToken(Type type) {
        this.type = $Gson$Types.canonicalize(Utils.checkNotNull(type));
        this.rawType = (Class<? super T>) $Gson$Types.getRawType(this.type);
        this.hashCode = this.type.hashCode();
    }

    /**
     * Returns the type from super class's type parameter in {@link $Gson$Types#canonicalize
     * canonical form}.
     */
    static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
    }

    /**
     * Returns the raw (non-generic) type for this type.
     */
    public final Class<? super T> getRawType() {
        return rawType;
    }

    /**
     * Gets underlying {@code Type} instance.
     */
    public final Type getType() {
        return type;
    }

    /**
     * Check if this type is assignable from the given class object.
     *
     * @deprecated this implementation may be inconsistent with javac for types
     *     with wildcards.
     */
    @Deprecated
    public boolean isAssignableFrom(Class<?> cls) {
        return isAssignableFrom((Type) cls);
    }

    /**
     * Check if this type is assignable from the given Type.
     *
     * @deprecated this implementation may be inconsistent with javac for types
     *     with wildcards.
     */
    @Deprecated
    public boolean isAssignableFrom(Type from) {
        if (from == null) {
            return false;
        }

        if (type.equals(from)) {
            return true;
        }

        if (type instanceof Class<?>) {
            return rawType.isAssignableFrom($Gson$Types.getRawType(from));
        } else if (type instanceof ParameterizedType) {
            return isAssignableFrom(from, (ParameterizedType) type,
                    new HashMap<String, Type>());
        } else if (type instanceof GenericArrayType) {
            return rawType.isAssignableFrom($Gson$Types.getRawType(from))
                    && isAssignableFrom(from, (GenericArrayType) type);
        } else {
            throw buildUnexpectedTypeError(
                    type, Class.class, ParameterizedType.class, GenericArrayType.class);
        }
    }

    /**
     * Check if this type is assignable from the given type token.
     *
     * @deprecated this implementation may be inconsistent with javac for types
     *     with wildcards.
     */
    @Deprecated
    public boolean isAssignableFrom(TypeToken<?> token) {
        return isAssignableFrom(token.getType());
    }

    /**
     * Private helper function that performs some assignability checks for
     * the provided GenericArrayType.
     */
    private static boolean isAssignableFrom(Type from, GenericArrayType to) {
        Type toGenericComponentType = to.getGenericComponentType();
        if (toGenericComponentType instanceof ParameterizedType) {
            Type t = from;
            if (from instanceof GenericArrayType) {
                t = ((GenericArrayType) from).getGenericComponentType();
            } else if (from instanceof Class<?>) {
                Class<?> classType = (Class<?>) from;
                while (classType.isArray()) {
                    classType = classType.getComponentType();
                }
                t = classType;
            }
            return isAssignableFrom(t, (ParameterizedType) toGenericComponentType,
                    new HashMap<String, Type>());
        }
        // No generic defined on "to"; therefore, return true and let other
        // checks determine assignability
        return true;
    }

    /**
     * Private recursive helper function to actually do the type-safe checking
     * of assignability.
     */
    private static boolean isAssignableFrom(Type from, ParameterizedType to,
                                            Map<String, Type> typeVarMap) {

        if (from == null) {
            return false;
        }

        if (to.equals(from)) {
            return true;
        }

        // First figure out the class and any type information.
        Class<?> clazz = $Gson$Types.getRawType(from);
        ParameterizedType ptype = null;
        if (from instanceof ParameterizedType) {
            ptype = (ParameterizedType) from;
        }

        // Load up parameterized variable info if it was parameterized.
        if (ptype != null) {
            Type[] tArgs = ptype.getActualTypeArguments();
            TypeVariable<?>[] tParams = clazz.getTypeParameters();
            for (int i = 0; i < tArgs.length; i++) {
                Type arg = tArgs[i];
                TypeVariable<?> var = tParams[i];
                while (arg instanceof TypeVariable<?>) {
                    TypeVariable<?> v = (TypeVariable<?>) arg;
                    arg = typeVarMap.get(v.getName());
                }
                typeVarMap.put(var.getName(), arg);
            }

            // check if they are equivalent under our current mapping.
            if (typeEquals(ptype, to, typeVarMap)) {
                return true;
            }
        }

        for (Type itype : clazz.getGenericInterfaces()) {
            if (isAssignableFrom(itype, to, new HashMap<String, Type>(typeVarMap))) {
                return true;
            }
        }

        // Interfaces didn't work, try the superclass.
        Type sType = clazz.getGenericSuperclass();
        return isAssignableFrom(sType, to, new HashMap<String, Type>(typeVarMap));
    }

    /**
     * Checks if two parameterized types are exactly equal, under the variable
     * replacement described in the typeVarMap.
     */
    private static boolean typeEquals(ParameterizedType from,
                                      ParameterizedType to, Map<String, Type> typeVarMap) {
        if (from.getRawType().equals(to.getRawType())) {
            Type[] fromArgs = from.getActualTypeArguments();
            Type[] toArgs = to.getActualTypeArguments();
            for (int i = 0; i < fromArgs.length; i++) {
                if (!matches(fromArgs[i], toArgs[i], typeVarMap)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static AssertionError buildUnexpectedTypeError(
            Type token, Class<?>... expected) {

        // Build exception message
        StringBuilder exceptionMessage =
                new StringBuilder("Unexpected type. Expected one of: ");
        for (Class<?> clazz : expected) {
            exceptionMessage.append(clazz.getName()).append(", ");
        }
        exceptionMessage.append("but got: ").append(token.getClass().getName())
                .append(", for type token: ").append(token.toString()).append('.');

        return new AssertionError(exceptionMessage.toString());
    }

    /**
     * Checks if two types are the same or are equivalent under a variable mapping
     * given in the type map that was provided.
     */
    private static boolean matches(Type from, Type to, Map<String, Type> typeMap) {
        return to.equals(from)
                || (from instanceof TypeVariable
                && to.equals(typeMap.get(((TypeVariable<?>) from).getName())));

    }

    @Override public final int hashCode() {
        return this.hashCode;
    }

    @Override public final boolean equals(Object o) {
        return o instanceof TypeToken<?>
                && $Gson$Types.equals(type, ((TypeToken<?>) o).type);
    }

    @Override public final String toString() {
        return $Gson$Types.typeToString(type);
    }

    /**
     * Gets type literal for the given {@code Type} instance.
     */
    public static TypeToken<?> get(Type type) {
        return new TypeToken<Object>(type);
    }

    /**
     * Gets type literal for the given {@code Class} instance.
     */
    public static <T> TypeToken<T> get(Class<T> type) {
        return new TypeToken<T>(type);
    }



    /**
     * Static methods for working with types.
     *
     * @author Bob Lee
     * @author Jesse Wilson
     */
    public static final class $Gson$Types {
        static final Type[] EMPTY_TYPE_ARRAY = new Type[] {};

        private $Gson$Types() {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns a new parameterized type, applying {@code typeArguments} to
         * {@code rawType} and enclosed by {@code ownerType}.
         *
         * @return a {@link java.io.Serializable serializable} parameterized type.
         */
        public static ParameterizedType newParameterizedTypeWithOwner(
                Type ownerType, Type rawType, Type... typeArguments) {
            return new ParameterizedTypeImpl(ownerType, rawType, typeArguments);
        }

        /**
         * Returns an array type whose elements are all instances of
         * {@code componentType}.
         *
         * @return a {@link java.io.Serializable serializable} generic array type.
         */
        public static GenericArrayType arrayOf(Type componentType) {
            return new GenericArrayTypeImpl(componentType);
        }

        /**
         * Returns a type that represents an unknown type that extends {@code bound}.
         * For example, if {@code bound} is {@code CharSequence.class}, this returns
         * {@code ? extends CharSequence}. If {@code bound} is {@code Object.class},
         * this returns {@code ?}, which is shorthand for {@code ? extends Object}.
         */
        public static WildcardType subtypeOf(Type bound) {
            return new WildcardTypeImpl(new Type[] { bound }, EMPTY_TYPE_ARRAY);
        }

        /**
         * Returns a type that represents an unknown supertype of {@code bound}. For
         * example, if {@code bound} is {@code String.class}, this returns {@code ?
         * super String}.
         */
        public static WildcardType supertypeOf(Type bound) {
            return new WildcardTypeImpl(new Type[] { Object.class }, new Type[] { bound });
        }

        /**
         * Returns a type that is functionally equal but not necessarily equal
         * according to {@link Object#equals(Object) Object.equals()}. The returned
         * type is {@link java.io.Serializable}.
         */
        public static Type canonicalize(Type type) {
            if (type instanceof Class) {
                Class<?> c = (Class<?>) type;
                return c.isArray() ? new GenericArrayTypeImpl(canonicalize(c.getComponentType())) : c;

            } else if (type instanceof ParameterizedType) {
                ParameterizedType p = (ParameterizedType) type;
                return new ParameterizedTypeImpl(p.getOwnerType(),
                        p.getRawType(), p.getActualTypeArguments());

            } else if (type instanceof GenericArrayType) {
                GenericArrayType g = (GenericArrayType) type;
                return new GenericArrayTypeImpl(g.getGenericComponentType());

            } else if (type instanceof WildcardType) {
                WildcardType w = (WildcardType) type;
                return new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());

            } else {
                // type is either serializable as-is or unsupported
                return type;
            }
        }

        public static Class<?> getRawType(Type type) {
            if (type instanceof Class<?>) {
                // type is a normal class.
                return (Class<?>) type;

            } else if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;

                // I'm not exactly sure why getRawType() returns Type instead of Class.
                // Neal isn't either but suspects some pathological case related
                // to nested classes exists.
                Type rawType = parameterizedType.getRawType();
                checkArgument(rawType instanceof Class);
                return (Class<?>) rawType;

            } else if (type instanceof GenericArrayType) {
                Type componentType = ((GenericArrayType)type).getGenericComponentType();
                return Array.newInstance(getRawType(componentType), 0).getClass();

            } else if (type instanceof TypeVariable) {
                // we could use the variable's bounds, but that won't work if there are multiple.
                // having a raw type that's more general than necessary is okay
                return Object.class;

            } else if (type instanceof WildcardType) {
                return getRawType(((WildcardType) type).getUpperBounds()[0]);

            } else {
                String className = type == null ? "null" : type.getClass().getName();
                throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                        + "GenericArrayType, but <" + type + "> is of type " + className);
            }
        }

        static boolean equal(Object a, Object b) {
            return a == b || (a != null && a.equals(b));
        }

        /**
         * Returns true if {@code a} and {@code b} are equal.
         */
        public static boolean equals(Type a, Type b) {
            if (a == b) {
                // also handles (a == null && b == null)
                return true;

            } else if (a instanceof Class) {
                // Class already specifies equals().
                return a.equals(b);

            } else if (a instanceof ParameterizedType) {
                if (!(b instanceof ParameterizedType)) {
                    return false;
                }

                // TODO: save a .clone() call
                ParameterizedType pa = (ParameterizedType) a;
                ParameterizedType pb = (ParameterizedType) b;
                return equal(pa.getOwnerType(), pb.getOwnerType())
                        && pa.getRawType().equals(pb.getRawType())
                        && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());

            } else if (a instanceof GenericArrayType) {
                if (!(b instanceof GenericArrayType)) {
                    return false;
                }

                GenericArrayType ga = (GenericArrayType) a;
                GenericArrayType gb = (GenericArrayType) b;
                return equals(ga.getGenericComponentType(), gb.getGenericComponentType());

            } else if (a instanceof WildcardType) {
                if (!(b instanceof WildcardType)) {
                    return false;
                }

                WildcardType wa = (WildcardType) a;
                WildcardType wb = (WildcardType) b;
                return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds())
                        && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());

            } else if (a instanceof TypeVariable) {
                if (!(b instanceof TypeVariable)) {
                    return false;
                }
                TypeVariable<?> va = (TypeVariable<?>) a;
                TypeVariable<?> vb = (TypeVariable<?>) b;
                return va.getGenericDeclaration() == vb.getGenericDeclaration()
                        && va.getName().equals(vb.getName());

            } else {
                // This isn't a type we support. Could be a generic array type, wildcard type, etc.
                return false;
            }
        }

        static int hashCodeOrZero(Object o) {
            return o != null ? o.hashCode() : 0;
        }

        public static String typeToString(Type type) {
            return type instanceof Class ? ((Class<?>) type).getName() : type.toString();
        }

        /**
         * Returns the generic supertype for {@code supertype}. For example, given a class {@code
         * IntegerSet}, the result for when supertype is {@code Set.class} is {@code Set<Integer>} and the
         * result when the supertype is {@code Collection.class} is {@code Collection<Integer>}.
         */
        static Type getGenericSupertype(Type context, Class<?> rawType, Class<?> toResolve) {
            if (toResolve == rawType) {
                return context;
            }

            // we skip searching through interfaces if unknown is an interface
            if (toResolve.isInterface()) {
                Class<?>[] interfaces = rawType.getInterfaces();
                for (int i = 0, length = interfaces.length; i < length; i++) {
                    if (interfaces[i] == toResolve) {
                        return rawType.getGenericInterfaces()[i];
                    } else if (toResolve.isAssignableFrom(interfaces[i])) {
                        return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
                    }
                }
            }

            // check our supertypes
            if (!rawType.isInterface()) {
                while (rawType != Object.class) {
                    Class<?> rawSupertype = rawType.getSuperclass();
                    if (rawSupertype == toResolve) {
                        return rawType.getGenericSuperclass();
                    } else if (toResolve.isAssignableFrom(rawSupertype)) {
                        return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
                    }
                    rawType = rawSupertype;
                }
            }

            // we can't resolve this further
            return toResolve;
        }

        /**
         * Returns the generic form of {@code supertype}. For example, if this is {@code
         * ArrayList<String>}, this returns {@code Iterable<String>} given the input {@code
         * Iterable.class}.
         *
         * @param supertype a superclass of, or interface implemented by, this.
         */
        static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
            checkArgument(supertype.isAssignableFrom(contextRawType));
            return resolve(context, contextRawType,
                    $Gson$Types.getGenericSupertype(context, contextRawType, supertype));
        }

        /**
         * Returns the component type of this array type.
         * @throws ClassCastException if this type is not an array.
         */
        public static Type getArrayComponentType(Type array) {
            return array instanceof GenericArrayType
                    ? ((GenericArrayType) array).getGenericComponentType()
                    : ((Class<?>) array).getComponentType();
        }

        /**
         * Returns the element type of this collection type.
         * @throws IllegalArgumentException if this type is not a collection.
         */
        public static Type getCollectionElementType(Type context, Class<?> contextRawType) {
            Type collectionType = getSupertype(context, contextRawType, Collection.class);

            if (collectionType instanceof WildcardType) {
                collectionType = ((WildcardType)collectionType).getUpperBounds()[0];
            }
            if (collectionType instanceof ParameterizedType) {
                return ((ParameterizedType) collectionType).getActualTypeArguments()[0];
            }
            return Object.class;
        }

        /**
         * Returns a two element array containing this map's key and value types in
         * positions 0 and 1 respectively.
         */
        public static Type[] getMapKeyAndValueTypes(Type context, Class<?> contextRawType) {
    /*
     * Work around a problem with the declaration of java.util.Properties. That
     * class should extend Hashtable<String, String>, but it's declared to
     * extend Hashtable<Object, Object>.
     */
            if (context == Properties.class) {
                return new Type[] { String.class, String.class }; // TODO: test subclasses of Properties!
            }

            Type mapType = getSupertype(context, contextRawType, Map.class);
            // TODO: strip wildcards?
            if (mapType instanceof ParameterizedType) {
                ParameterizedType mapParameterizedType = (ParameterizedType) mapType;
                return mapParameterizedType.getActualTypeArguments();
            }
            return new Type[] { Object.class, Object.class };
        }

        public static Type resolve(Type context, Class<?> contextRawType, Type toResolve) {
            // this implementation is made a little more complicated in an attempt to avoid object-creation
            while (true) {
                if (toResolve instanceof TypeVariable) {
                    TypeVariable<?> typeVariable = (TypeVariable<?>) toResolve;
                    toResolve = resolveTypeVariable(context, contextRawType, typeVariable);
                    if (toResolve == typeVariable) {
                        return toResolve;
                    }

                } else if (toResolve instanceof Class && ((Class<?>) toResolve).isArray()) {
                    Class<?> original = (Class<?>) toResolve;
                    Type componentType = original.getComponentType();
                    Type newComponentType = resolve(context, contextRawType, componentType);
                    return componentType == newComponentType
                            ? original
                            : arrayOf(newComponentType);

                } else if (toResolve instanceof GenericArrayType) {
                    GenericArrayType original = (GenericArrayType) toResolve;
                    Type componentType = original.getGenericComponentType();
                    Type newComponentType = resolve(context, contextRawType, componentType);
                    return componentType == newComponentType
                            ? original
                            : arrayOf(newComponentType);

                } else if (toResolve instanceof ParameterizedType) {
                    ParameterizedType original = (ParameterizedType) toResolve;
                    Type ownerType = original.getOwnerType();
                    Type newOwnerType = resolve(context, contextRawType, ownerType);
                    boolean changed = newOwnerType != ownerType;

                    Type[] args = original.getActualTypeArguments();
                    for (int t = 0, length = args.length; t < length; t++) {
                        Type resolvedTypeArgument = resolve(context, contextRawType, args[t]);
                        if (resolvedTypeArgument != args[t]) {
                            if (!changed) {
                                args = args.clone();
                                changed = true;
                            }
                            args[t] = resolvedTypeArgument;
                        }
                    }

                    return changed
                            ? newParameterizedTypeWithOwner(newOwnerType, original.getRawType(), args)
                            : original;

                } else if (toResolve instanceof WildcardType) {
                    WildcardType original = (WildcardType) toResolve;
                    Type[] originalLowerBound = original.getLowerBounds();
                    Type[] originalUpperBound = original.getUpperBounds();

                    if (originalLowerBound.length == 1) {
                        Type lowerBound = resolve(context, contextRawType, originalLowerBound[0]);
                        if (lowerBound != originalLowerBound[0]) {
                            return supertypeOf(lowerBound);
                        }
                    } else if (originalUpperBound.length == 1) {
                        Type upperBound = resolve(context, contextRawType, originalUpperBound[0]);
                        if (upperBound != originalUpperBound[0]) {
                            return subtypeOf(upperBound);
                        }
                    }
                    return original;

                } else {
                    return toResolve;
                }
            }
        }

        static Type resolveTypeVariable(Type context, Class<?> contextRawType, TypeVariable<?> unknown) {
            Class<?> declaredByRaw = declaringClassOf(unknown);

            // we can't reduce this further
            if (declaredByRaw == null) {
                return unknown;
            }

            Type declaredBy = getGenericSupertype(context, contextRawType, declaredByRaw);
            if (declaredBy instanceof ParameterizedType) {
                int index = indexOf(declaredByRaw.getTypeParameters(), unknown);
                return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
            }

            return unknown;
        }

        private static int indexOf(Object[] array, Object toFind) {
            for (int i = 0; i < array.length; i++) {
                if (toFind.equals(array[i])) {
                    return i;
                }
            }
            throw new NoSuchElementException();
        }

        /**
         * Returns the declaring class of {@code typeVariable}, or {@code null} if it was not declared by
         * a class.
         */
        private static Class<?> declaringClassOf(TypeVariable<?> typeVariable) {
            GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
            return genericDeclaration instanceof Class
                    ? (Class<?>) genericDeclaration
                    : null;
        }

        static void checkNotPrimitive(Type type) {
            checkArgument(!(type instanceof Class<?>) || !((Class<?>) type).isPrimitive());
        }

        private static final class ParameterizedTypeImpl implements ParameterizedType, Serializable {
            private final Type ownerType;
            private final Type rawType;
            private final Type[] typeArguments;

            public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
                // require an owner type if the raw type needs it
                if (rawType instanceof Class<?>) {
                    Class<?> rawTypeAsClass = (Class<?>) rawType;
                    boolean isStaticOrTopLevelClass = Modifier.isStatic(rawTypeAsClass.getModifiers())
                            || rawTypeAsClass.getEnclosingClass() == null;
                    checkArgument(ownerType != null || isStaticOrTopLevelClass);
                }

                this.ownerType = ownerType == null ? null : canonicalize(ownerType);
                this.rawType = canonicalize(rawType);
                this.typeArguments = typeArguments.clone();
                for (int t = 0; t < this.typeArguments.length; t++) {
                    Utils.checkNotNull(this.typeArguments[t]);
                    checkNotPrimitive(this.typeArguments[t]);
                    this.typeArguments[t] = canonicalize(this.typeArguments[t]);
                }
            }

            public Type[] getActualTypeArguments() {
                return typeArguments.clone();
            }

            public Type getRawType() {
                return rawType;
            }

            public Type getOwnerType() {
                return ownerType;
            }

            @Override public boolean equals(Object other) {
                return other instanceof ParameterizedType
                        && $Gson$Types.equals(this, (ParameterizedType) other);
            }

            @Override public int hashCode() {
                return Arrays.hashCode(typeArguments)
                        ^ rawType.hashCode()
                        ^ hashCodeOrZero(ownerType);
            }

            @Override public String toString() {
                StringBuilder stringBuilder = new StringBuilder(30 * (typeArguments.length + 1));
                stringBuilder.append(typeToString(rawType));

                if (typeArguments.length == 0) {
                    return stringBuilder.toString();
                }

                stringBuilder.append("<").append(typeToString(typeArguments[0]));
                for (int i = 1; i < typeArguments.length; i++) {
                    stringBuilder.append(", ").append(typeToString(typeArguments[i]));
                }
                return stringBuilder.append(">").toString();
            }

            private static final long serialVersionUID = 0;
        }

        private static final class GenericArrayTypeImpl implements GenericArrayType, Serializable {
            private final Type componentType;

            public GenericArrayTypeImpl(Type componentType) {
                this.componentType = canonicalize(componentType);
            }

            public Type getGenericComponentType() {
                return componentType;
            }

            @Override public boolean equals(Object o) {
                return o instanceof GenericArrayType
                        && $Gson$Types.equals(this, (GenericArrayType) o);
            }

            @Override public int hashCode() {
                return componentType.hashCode();
            }

            @Override public String toString() {
                return typeToString(componentType) + "[]";
            }

            private static final long serialVersionUID = 0;
        }

        /**
         * The WildcardType interface supports multiple upper bounds and multiple
         * lower bounds. We only support what the Java 6 language needs - at most one
         * bound. If a lower bound is set, the upper bound must be Object.class.
         */
        private static final class WildcardTypeImpl implements WildcardType, Serializable {
            private final Type upperBound;
            private final Type lowerBound;

            public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
                checkArgument(lowerBounds.length <= 1);
                checkArgument(upperBounds.length == 1);

                if (lowerBounds.length == 1) {
                    Utils.checkNotNull(lowerBounds[0]);
                    checkNotPrimitive(lowerBounds[0]);
                    checkArgument(upperBounds[0] == Object.class);
                    this.lowerBound = canonicalize(lowerBounds[0]);
                    this.upperBound = Object.class;

                } else {
                    Utils.checkNotNull(upperBounds[0]);
                    checkNotPrimitive(upperBounds[0]);
                    this.lowerBound = null;
                    this.upperBound = canonicalize(upperBounds[0]);
                }
            }

            public Type[] getUpperBounds() {
                return new Type[] { upperBound };
            }

            public Type[] getLowerBounds() {
                return lowerBound != null ? new Type[] { lowerBound } : EMPTY_TYPE_ARRAY;
            }

            @Override public boolean equals(Object other) {
                return other instanceof WildcardType
                        && $Gson$Types.equals(this, (WildcardType) other);
            }

            @Override public int hashCode() {
                // this equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds());
                return (lowerBound != null ? 31 + lowerBound.hashCode() : 1)
                        ^ (31 + upperBound.hashCode());
            }

            @Override public String toString() {
                if (lowerBound != null) {
                    return "? super " + typeToString(lowerBound);
                } else if (upperBound == Object.class) {
                    return "?";
                } else {
                    return "? extends " + typeToString(upperBound);
                }
            }

            private static final long serialVersionUID = 0;
        }
    }

    private static void checkArgument(boolean condition) {
        if (!condition) {
            throw new ArgumentException();
        }
    }

}
