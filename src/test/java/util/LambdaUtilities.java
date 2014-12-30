package test.java.util;
/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongPredicate;
import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LambdaUtilities {


    public static IntPredicate randomIntPredicate(boolean isUP, int limit) {
        if (isUP) {
            return (i) -> i >= limit;
        } else {
            return (i) -> i < limit;
        }
    }

    public static Predicate<Integer> randomIntegerPredicate(boolean isUP, int limit) {
        if (isUP) {
            return (i) -> i >= limit;
        } else {
            return (i) -> i < limit;
        }
    }

    public static LongPredicate randomLongPredicate(boolean isUP, long limit) {
        if (isUP) {
            return (i) -> i >= limit;
        } else {
            return (i) -> i < limit;
        }
    }

    public static Predicate<Path> startPathPredicate(Path start) {
        return (p) -> p.startsWith(start);
    }

    public static <T> Predicate<T> randomGenericPredicate(boolean isUp, T value, Comparator<T> c) {
        if (isUp) {
            return (emp) -> c.compare(emp, value) >= 0;
        }else {
            return (emp) -> c.compare(emp, value) < 0;
        }
    }

    //EFIM: value is not used!!!
    public static Predicate<StringBuilder> randomSBPredicate(StringPredicateType type, String value) {
        switch (type) {
            case START_WTIH:
                return (sb) -> Character.isDigit(sb.charAt(0));
            case NOT_START_WITH:
                return (sb) -> Character.isLowerCase(sb.charAt(0));
            case MORE_THAN_LEN:
                return (sb) -> Character.isUpperCase(sb.charAt(0));
            default:
                return (sb) -> !Character.isLetterOrDigit(sb.charAt(0));
        }
    }

    public static Predicate<? extends CharSequence> randomSBPredicate(CharType startType,
            boolean first) {
        switch (startType) {
            case DIGIT:
                return (sb) -> Character.isDigit(sb.charAt(first ? 0 : sb.toString().length() - 1));
            case LOWERCASE:
                return (sb) -> Character.isLowerCase(sb.charAt(first ? 0 : sb.toString().length() - 1));
            case UPPERCASE:
                return (sb) -> Character.isUpperCase(sb.charAt(first ? 0 : sb.toString().length() - 1));
            default:
                return (sb) -> !Character.isLetterOrDigit(sb.charAt(first ? 0 : sb.toString().length() - 1));
        }
    }
    
    public static Function<StringBuilder, Stream<StringBuilder>> genSBFlatMapper(int selected, int unit) {
        switch(selected) {
            case 0:
                //Generate a empty collection
                return (e) -> {return new ArrayList<StringBuilder>().stream();};
            case 1: 
                return (e) -> { ArrayList<StringBuilder> res = new ArrayList<>(); res.add(e); return res.stream(); };
            case 2:
                return new Function<StringBuilder, Stream<StringBuilder>>() {

                    @Override
                    public Stream<StringBuilder> apply(StringBuilder e) {
                        ArrayList<StringBuilder> res = new ArrayList<>();
                        int step = e.length() / unit + unit - 1;
                        for (int i = 0; i < e.length(); i += step) {
                            res.add(new StringBuilder(e.substring(i, i + step >= e.length()
                                    ? e.length() - 1 : i + step)));
                        }
                        return res.stream();
                    }
                };
            case 3:
            default:
                //Generate 64 folded flat map
                return new Function<StringBuilder, Stream<StringBuilder>>() {

                    @Override
                    public Stream<StringBuilder> apply(StringBuilder e) {
                        ArrayList<StringBuilder> res = new ArrayList<>();
                        int step = e.length() / unit + unit - 1;
                        for (int i = 0; i < e.length(); i += step) {
                            res.add(e);
                        }
                        return res.stream();
                    }
                };
        }
    }
    
    public static Function<Integer, Stream<Integer>> genIntegerFlatMapper(int selected) {
        switch (selected) {
            case 0:
                //Generate a empty collection
                return (e) -> {return new ArrayList<Integer>().stream();};
            case 1:
                return (e) -> { ArrayList<Integer> res = new ArrayList<>(); res.add(e); return res.stream(); };
            case 2:
                //Generate a triangle has different value
                return (e) -> {
                    ArrayList<Integer> res = new ArrayList<>(); 
                    for (int i = 0; i < e; i++) {
                        res.add(e * (e - 1) / 2 + i);
                    }
                    return res.stream();
                };
            case 3:
                //Generate a triangle has different value
                return (e) -> {
                    ArrayList<Integer> res = new ArrayList<>();
                    for (int i = 0; i < e; i++) {
                        res.add(e);
                    }
                    return res.stream();
                };
            default:
                //Generate 64 folded flat map
                return (e) -> {
                    ArrayList<Integer> res = new ArrayList<>(); 
                    for (int i = 0; i < 1 << 6; i++) {
                        res.add(e);
                    }
                    return res.stream();
                };
        }
    }
    
    public static IntFunction<IntStream> genIntFlatMapper(int selected) {
        switch (selected) {
            case 0:
                //Generate a empty collection
                return (e) -> { return IntStream.empty();};
            case 1:
                return (e) -> { return IntStream.of(e); };
            case 2:
                //Generate a triangle has different value
                return (e) -> {
                    int[] res = new int[e];
                    for (int i = 0; i < e; i++) {
                        res[i] = e * (e - 1) / 2 + i;
                    }
                    return IntStream.of(res);
                };
            case 3:
                //Generate a triangle has different value
                return (e) -> {
                    int[] res = new int[e];
                    for (int i = 0; i < e; i++) {
                        res[i] = e;
                    }
                    return IntStream.of(res);
                };
            default:
                //Generate 64 folded flat map
                return (e) -> {
                    int[] res = new int[1 << 6];
                    for (int i = 0; i < 1 << 6; i++) {
                        res[i] = e;
                    }
                    return IntStream.of(res);
                };
        }
    }
    

    public static Predicate<Character> isDigitCharacterPredicate() {
        return (c) -> Character.isDigit(c);
    }

    //EFIM: why two parameters??
    public static Function<Integer, Integer> posIntegerFunction(boolean isHighest) {
        if (isHighest) {
            return (i) -> Integer.valueOf(new StringBuilder().append(i < 0 ? -i : i).reverse().toString()) % 10;
        } else {
            return (i) -> i % 10 < 0 ? -i % 10 : i % 10;
        }
    }
    
    public static IntFunction<Integer> posIntFunction(boolean isHighest) {
        if (isHighest) {
            return i -> Integer.valueOf(new StringBuilder().append(i < 0 ? -i : i).reverse().toString()) % 10;
        } else {
            return i -> i % 10 < 0 ? -i % 10 : i % 10;
        }
    }

    //EFIM: make DRY
    public static Function<StringBuilder, CharType> sbGenericFunction(boolean isFirst) {
        if (isFirst) {
            return (e) -> Character.isAlphabetic(e.charAt(0)) ? (Character.isUpperCase(e.charAt(0)) ? CharType.UPPERCASE : CharType.LOWERCASE) : (Character.isDigit(e.charAt(0)) ? CharType.DIGIT : CharType.SPECIAL);
        } else {
            return (e) -> Character.isAlphabetic(e.charAt(e.length() - 1)) ? (Character.isUpperCase(e.charAt(e.length() - 1)) ? CharType.UPPERCASE : CharType.LOWERCASE) : (Character.isDigit(e.charAt(e.length() - 1)) ? CharType.DIGIT : CharType.SPECIAL);
        }
    }

    public static Function<String, Integer> mappingFunction(Map<String, Integer> m, IntOp op, int value) {
        switch (op) {
            case ADD:
                return k -> (value != 0) ? m.get(k) + value : m.get(k);
            case SUBTRACT:
                return k -> (value != 0) ? m.get(k) - value : m.get(k);
            case MULTIPLY:
                return k -> (value != 0) ? m.get(k) * value : m.get(k);
            case DIVIDE:
                return k -> (value != 0) ? m.get(k) / value : m.get(k);
            default:
                return k -> (value != 0) ? m.get(k) % value : m.get(k);
        }
    }

    public static BiFunction<Integer, Integer, Integer> randBetweenIntegerFunction() {
        return (t1, t2) -> randBetween(t1, t2);
    }

    //EFIM: this is definetely can be improved!
    public static int randBetween(int low, int up) {
        assert (low < up && low >= 0);
        Random rand = new Random();
        int i = rand.nextInt(up);
        while (i < low) {
            i = rand.nextInt();
        }
        return i;
    }

    public static ToIntFunction<Integer> highestPosValueIntFunction() {
        return i -> Integer.valueOf(new StringBuilder().append(i < 0 ? -i : i).reverse().toString()) % 10;
    }

    public static ToIntFunction<Integer> lowestPosValueIntFunction() {
        return i -> i % 10 < 0 ? -i % 10 : i % 10;
    }

    //EFIM research Consumer interface
    public static <T> Consumer<T> reverseConsumer(Set<T> set) {
        return t -> {
            set.add(t);
        };
    }

    public static Consumer<Integer> addIntegerConsumer(AtomicInteger ai) {
        return t -> {
            ai.updateAndGet(t1 -> t1 + t);
        };
    }

    public static Consumer<StringBuilder> appendSBConsumer(StringBuilder sb) {
        return t -> {
            sb.append(t);
        };
    }

    public static IntConsumer addIntConsumer(AtomicInteger ai) {
        return t -> {
            ai.updateAndGet(t1 -> t1 + t);
        };
    }

    public static IntConsumer addLongConsumer(AtomicLong ai) {
        return t -> {
            ai.updateAndGet(t1 -> t1 + t);
        };
    }

    public static <T> Consumer<T> copyConsumer(List<T> list) {
        return t -> {
            list.add(t);
        };
    }

    public static <T> Consumer<T> existsConsumer(Collection<T> in, Collection<T> out) {
        return t -> {
            if (in.contains(t)) {
                out.add(t);
            }
        };
    }

    public static Supplier<StringBuilder> sbSupplier(StringBuilder value) {
        return () -> value;
    }

    public static Supplier<Integer> integerSupplier(int value) {
        return () -> value;
    }

    public static <T> Supplier<T> genericSuppiler(T value) {
        return () -> value;
    }

    public static IntSupplier intSupplier(int value) {
        return () -> value;
    }

    public static Supplier<Long> longSupplier(long value) {
        return () -> value;
    }

    public static Supplier<AtomicInteger> atomicIntegerSupplier(int value) {
        return () -> new AtomicInteger(value);
    }

    public static <T> Supplier<AtomicReference<T>> atomicGenericSupplier(T value) {
        return () -> new AtomicReference<>(value);
    }

    public static Supplier<AtomicReference<StringBuilder>> atomicSBSupplier(StringBuilder value) {
        return () -> new AtomicReference<>(value);
    }

    public static UnaryOperator<Integer> opIntegerUnaryOperator(IntOp op, int value) {
        switch (op) {
            case ADD:
                return t -> t + value;
            case SUBTRACT:
                return t -> t - value;
            case MULTIPLY:
                return t -> t * value;
            case DIVIDE:
                return t -> t / value;
            default:
                return t -> t % value;
        }
    }

    public static IntUnaryOperator opIntUnaryOperator(IntOp op, int value) {
        if(value == 0) {
            return t -> t;
        }
        switch (op) {
            case ADD:
                return t -> t + value;
            case SUBTRACT:
                return t -> t - value;
            case MULTIPLY:
                return t -> t * value;
            case DIVIDE:
                return t -> t / value;
            default:
                return t -> t % value;
        }
    }

    //EFIM is Integer.valueOf is necessary here???
    //A lot of similar functors, united by high level function??
    public static Function<Integer, Integer> opIntegerFunction(IntOp op, int value) {
        if (value == 0) {
            return t -> t;
        }
        switch (op) {
            case ADD:
                return t -> Integer.valueOf(t + value);
            case SUBTRACT:
                return t -> Integer.valueOf(t - value);
            case MULTIPLY:
                return t -> Integer.valueOf(t * value);
            case DIVIDE:
                return t -> Integer.valueOf(t / value);
            default:
                return t -> Integer.valueOf(t % value);
        }
    }

    public static ToIntFunction<Integer> opToIntFunction(IntOp op, int value) {
        if (value == 0) {
            return t -> t.intValue();
        }
        switch (op) {
            case ADD:
                return t -> t.intValue() + value;
            case SUBTRACT:
                return t -> t.intValue() - value;
            case MULTIPLY:
                return t -> t.intValue() * value;
            case DIVIDE:
                return t -> t.intValue() / value;
            default:
                return t -> t.intValue() % value;
        }
    }

    public static IntFunction<Integer> opIntFunction(IntOp op, int value) {
        if (value == 0) {
            return t -> t;
        }
        switch (op) {
            case ADD:
                return t -> t + value;
            case SUBTRACT:
                return t -> t - value;
            case MULTIPLY:
                return t -> t * value;
            case DIVIDE:
                return t -> t / value;
            default:
                return t -> t % value;
        }
    }

    public static IntUnaryOperator addIntUnaryOperator(int value) {
        return t -> t + value;
    }

    public static IntUnaryOperator subIntUnaryOperator(int value) {
        return t -> t - value;
    }

    public static IntUnaryOperator mulIntUnaryOperator(int value) {
        return t -> t * value;
    }

    public static IntUnaryOperator divIntUnaryOperator(int value) {
        return t -> t / value;
    }

    public static IntBinaryOperator minIntBinaryOperator() {
        return (t1, t2) -> t1< t2 ? t1 : t2;
    }

    public static BinaryOperator<Integer> minIntegerBinaryOperator(Comparator<Integer> c) {
        return (t1, t2) -> c.compare(t1, t2) < 0 ? t1 : t2;
    }

    public static <T>BinaryOperator<T> minGenericBinaryOperator(Comparator<T> c) {
        return (t1, t2) -> c.compare(t1, t2) < 0 ? t1 : t2;
    }

    public static BinaryOperator<StringBuilder> minSBBinaryOperator(Comparator<StringBuilder>  c) {
        return ( t1, t2 ) -> c.compare(t1, t2) < 0 ? t1 : t2;
    }

    public static IntBinaryOperator maxIntBinaryOperator() {
        return ( t1, t2 ) -> (t1< t2) ? t2: t1;
    }

    public static BinaryOperator<Integer> maxIntegerBinaryOperator(Comparator<Integer>  c) {
        return (t1, t2) -> c.compare(t1, t2) < 0 ? t2 : t1;
    }

    public static <T> BinaryOperator<T> maxGenericBinaryOperator(Comparator<T>  c) {
        return (t1, t2) -> c.compare(t1, t2) < 0 ? t2 : t1;
    }

    public static IntBinaryOperator maxIntBinaryOperator(Comparator<Integer>  c) {
        return ( t1, t2 ) -> c.compare(t1, t2) < 0 ? t2 : t1;
    }

    public static BinaryOperator<StringBuilder> maxSBBinaryOperator(Comparator<StringBuilder>  c) {
        return ( t1, t2 ) -> c.compare(t1, t2) < 0 ? t2 : t1;
    }

    public static BinaryOperator<Integer> addIntegerBinaryOperator() {
        return ( t1 , t2 ) -> t1 + t2;
    }

    public static IntBinaryOperator addIntBinaryOperator() {
        return ( t1 , t2 ) -> t1 + t2;
    }

    public static BinaryOperator<BigDecimal> addBigDecimalBinaryOperator() {
        return ( t1 , t2 ) -> t1.add(t2);
    }

    public static DoubleBinaryOperator addDoubleBinaryOperator() {
        return ( t1 , t2 ) -> t1 + t2;
    }

    public static BinaryOperator<StringBuilder> appendSBBinaryOperator() {
        return (t1 , t2) -> new StringBuilder().append(t1).append(t2);
    }

    public static BinaryOperator<Integer> subIntegerBinaryOperator() {
        return (t1, t2) -> t1 - t2;
    }

    public static IntBinaryOperator subIntBinaryOperator() {
        return (t1, t2) -> t1 - t2;
    }

    public static BinaryOperator<StringBuilder> deleteSBBinaryOperator() {
        return (t1, t2) -> {if (t1.length() >= t2.length()) {
                                int i1 = t1.indexOf(t2.toString());
                                int i2 = i1 + t2.length();
                                return new StringBuilder(t1).delete(i1, i2);
                            }else {
                                int i1 = t2.indexOf(t1.toString());
                                int i2 = i1 + t1.length();
                                return new StringBuilder(t2).delete(i1, i2);
                            }
        };

    }

    public static IntBinaryOperator mulIntBinaryOperator() {
        return (t1, t2) -> t1 * t2;
    }

    public static IntBinaryOperator divIntBinaryOperator() {
        return (t1, t2) -> t1 / t2;
    }

    public static LongUnaryOperator addLongUnaryOperator(long value) {
        return t -> t + value;
    }

    public static UnaryOperator<StringBuilder> appendSBUnaryOperator(StringBuilder value) {
        return t -> t.append(value);
    }

    public static LongUnaryOperator subLongUnaryOperator(long value) {
        return t -> t - value;
    }

    public static LongUnaryOperator mulLongUnaryOperator(long value) {
        return t -> t * value;
    }

    public static LongUnaryOperator divLongUnaryOperator(long value) {
        return t -> t / value;
    }

    public static LongBinaryOperator addLongBinaryOperator() {
        return (t1, t2) -> t1 + t2;
    }

    public static LongBinaryOperator subLongBinaryOperator() {
        return (t1, t2) -> t1 - t2;
    }

    public static LongBinaryOperator mulLongBinaryOperator() {
        return (t1, t2) -> t1 * t2;
    }

    public static LongBinaryOperator divLongBinaryOperator() {
        return (t1, t2) -> t1 / t2;
    }

    public static BiConsumer<AtomicInteger, Integer> addIntegerBiConsumer() {
        return (t1 , t2 ) -> {  t1.addAndGet(t2); };
    }

    public static BiConsumer<AtomicInteger, AtomicInteger> addAtomicIntegerBiConsumer() {
        return (t1 , t2) -> {  t1.addAndGet(t2.get()); };
    }

    public static BiConsumer<AtomicReference<StringBuilder>, StringBuilder> appendSBBiConsumer() {
        return (t1, t2) -> {t1.updateAndGet(appendSBUnaryOperator(t2));};
    }

    public static BiConsumer<AtomicReference<StringBuilder>, AtomicReference<StringBuilder>> appendAtomicSBBiConsumer() {
        return (t1, t2) -> {t1.updateAndGet(appendSBUnaryOperator(t2.get()));};
    }

    public static BiConsumer<AtomicInteger, Integer> maxIntegerBiConsumer(Comparator<Integer> c) {
        return (t1 , t2 ) -> {  t1.getAndUpdate(t -> max(t, t2, c)); };
    }

    public static <T> BiConsumer<AtomicReference<T>, T> maxGenericBiConsumer(Comparator<T> c) {
        return (t1 , t2 ) -> {  t1.getAndUpdate(t -> max(t, t2, c)); };
    }

    public static BiConsumer<AtomicInteger, AtomicInteger> maxAtomicIntegerBiConsumer(Comparator<Integer> c) {
        return (t1 , t2) -> {  t1.getAndUpdate(t -> max(t, t2.get(), c)); };
    }

    public static <T> BiConsumer<AtomicReference<T>, AtomicReference<T>> maxAtomicGenericBiConsumer(Comparator<T> c) {
        return (t1 , t2) -> {  t1.getAndUpdate(t -> max(t, t2.get(), c)); };
    }

    public static BiConsumer<AtomicInteger, Integer> minIntegerBiConsumer(Comparator<Integer> c) {
        return (t1 , t2) -> {  t1.getAndUpdate(t -> min(t, t2, c)); };
    }

    public static <T> BiConsumer<AtomicReference<T>, T> minGenericBiConsumer(Comparator<T> c) {
        return (t1 , t2) -> {  t1.getAndUpdate(t -> min(t, t2, c)); };
    }

    public static BiConsumer<AtomicInteger, AtomicInteger> minAtomicIntegerBiConsumer(Comparator<Integer> c) {
        return (t1, t2) -> {  t1.getAndUpdate(t -> min(t, t2.get(), c)); };
    }

    public static <T> BiConsumer<AtomicReference<T>, AtomicReference<T>> minAtomicGenericBiConsumer(Comparator<T> c) {
        return (t1, t2) -> {  t1.getAndUpdate(t -> min(t, t2.get(), c)); };
    }

    public static BiFunction<Integer, Integer, Integer> maxIntegerFunction(Comparator<Integer> c) {
        return (t1, t2) -> max(t1, t2, c);
    }

    public static BiFunction<BigDecimal, Integer, BigDecimal> deviationSequareFunction(double avg) {
        return (bd, t) -> bd.add(new BigDecimal(avg - t).pow(2));
    }

    public static <T> BiFunction<T, T, T> maxGenericFunction(Comparator<T> c) {
        return (t1, t2) -> max(t1, t2, c);
    }

    public static BiFunction<StringBuilder, StringBuilder, StringBuilder> maxStringBuilderFunction(Comparator<StringBuilder> c) {
        return (t1, t2) -> max(t1, t2, c);
    }

    public static BiFunction<Integer, Integer, Integer> minIntegerFunction(Comparator<Integer> c) {
        return (t1, t2) -> min(t1, t2, c);
    }

    public static <T> BiFunction<T, T, T> minGenericFunction(Comparator<T> c) {
        return (t1, t2) -> min(t1, t2, c);
    }

    public static BiFunction<StringBuilder, StringBuilder, StringBuilder> minStringBuilderFunction(Comparator<StringBuilder> c) {
        return (t1, t2) -> min(t1, t2, c);
    }

    public static BiFunction<String, Integer, Integer> opBiFunction(IntOp op, int value) {
        switch (op) {
            case ADD:
                return (k, v) -> (value != 0) ? v + value : v;
            case SUBTRACT:
                return (k, v) -> (value != 0) ? v - value : v;
            case MULTIPLY:
                return (k, v) -> (value != 0) ? v * value : v;
            case DIVIDE:
                return (k, v) -> (value != 0) ? v / value : v;
            default:
                return (k, v) -> (value != 0) ? v % value : v;
        }
    }


    public static BiFunction<Integer, Integer, Integer> opBiFunction(IntOp op) {
        switch (op) {
            case ADD:
                return (oldv, v) -> (v != 0) ? oldv + v : oldv;
            case SUBTRACT:
                return (oldv, v) -> (v != 0) ? oldv - v : oldv;
            case MULTIPLY:
                return (oldv, v) -> (v != 0) ? oldv * v : oldv;
            case DIVIDE:
                return (oldv, v) -> (v != 0) ? oldv / v : oldv;
            default:
                return (oldv, v) -> (v != 0) ? oldv % v : oldv;
        }
    }

    private static Integer min(Integer i1, Integer i2, Comparator<Integer> c) {
        return c.compare(i1, i2) < 0 ? i1 : i2;
    }

    private static <T> T min(T i1, T i2, Comparator<T> c) {
        return c.compare(i1, i2) < 0 ? i1 : i2;
    }

    private static StringBuilder min(StringBuilder sb1, StringBuilder sb2, Comparator<StringBuilder> c) {
        return c.compare(sb1, sb2) < 0 ? sb1 : sb2;
    }

    private static Integer max(Integer i1, Integer i2, Comparator<Integer> c) {
        return c.compare(i1, i2) < 0 ? i2 : i1;
    }

    private static <T> T max(T i1, T i2, Comparator<T> c) {
        return c.compare(i1, i2) < 0 ? i2 : i1;
    }

    private static StringBuilder max(StringBuilder sb1, StringBuilder sb2, Comparator<StringBuilder> c) {
        return c.compare(sb1, sb2) < 0 ? sb2 : sb1;
    }
    /*
     * Construct a Collection C object based on a C object, using generic type
     * instead of Class type can help preventing type error in compilation
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <E, C extends Collection<E>>  C create(C c, int... initSize)
            throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, IllegalArgumentException,
            InvocationTargetException {
        return create((Class<C>) c.getClass(), initSize);
    }

    /*
     * Construct a Collection C object based on a C's type, using generic type
     * instead of Class type can help preventing type error in compilation
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <E, T extends Collection<E>>  T create(
            Class<? extends Collection<E>> cls, int... initSize)
            throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, IllegalArgumentException,
            InvocationTargetException {
        assert (initSize.length <= 1);
        Collection<E> c;
        if (initSize.length == 0) {
            c = cls.newInstance();
        } else {
            Constructor con = cls.getConstructor(int.class);
            c = (Collection<E>) con.newInstance(initSize[0]);
        }
        return (T) c;
    }

    /*
     * Construct a T object based on T's type, using generic type instead of
     * Class type can help preventing type error in compilation
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <K, V, M extends Map<K, V>>  M createMap(M m, int... initSize)
            throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, IllegalArgumentException,
            InvocationTargetException {
        return createMap((Class<M>) m.getClass(), initSize);
    }

    /*
     * Construct a Map M object based on M's type, using generic type instead of
     * Class type can help preventing type error in compilation
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <K, V, M extends Map<K, V>>  M createMap(Class<? extends Map<K, V>> cls,
            int... initSize) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException,
            IllegalArgumentException, InvocationTargetException {
        assert (initSize.length <= 1);
        Map<K, V> map;
        if (initSize.length == 0) {
            map = cls.newInstance();
        } else {
            Constructor con = cls.getConstructor(int.class);
            map = (Map<K, V>) con.newInstance(initSize[0]);
        }
        return (M) map;
    }

    public static enum CharType {

        DIGIT, LOWERCASE, UPPERCASE, SPECIAL
    }

    public static enum StringPredicateType {

        START_WTIH, NOT_START_WITH, MORE_THAN_LEN, LESS_THAN_LEN
    }

    public static enum IntOp {

        ADD, SUBTRACT, MULTIPLY, DIVIDE, MOD
    }
}
