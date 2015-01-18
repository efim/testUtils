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

/**
 * Collection of lambda utilities Numbers and Strings.
 * @version     %I%, %G%
 */
public class LambdaUtilities {

    /**
     * 
     * @param <T> type of input values
     * @param isUp boolean determining direction of comparison
     * @param value Generic value used as floor
     * @param c Comparator used in input evaluation
     * @return predicate that compares generic input with provided value.
     */
    public static <T> Predicate<T> randomGenericPredicate(boolean isUp, T value, Comparator<T> c) {
        if (isUp) {
            return (emp) -> c.compare(emp, value) >= 0;
        } else {
            return (emp) -> c.compare(emp, value) < 0;
        }
    }

    /**
     *
     * @param isUP boolean determining direction of comparison
     * @param limit value used as floor
     * @return predicate that compares integer input with provided value.
     */
    public static IntPredicate randomIntPredicate(boolean isUP, int limit) {
        return (IntPredicate) randomGenericPredicate(isUP, limit,
                (Integer v1, Integer v2) -> (v1 > v2) ? 1 : (v1 < v2) ? -1 : 0);
    }

    /**
     *
     * @param isUP boolean determining direction of comparison
     * @param limit value used as floor
     * @return predicate that compares Integer input with provided value.
     */
    public static Predicate<Integer> randomIntegerPredicate(boolean isUP, int limit) {
        return randomGenericPredicate(isUP, limit, 
                (Integer v1, Integer v2) -> (v1 > v2) ? 1 : (v1 < v2) ? -1 : 0);
    }

    /**
     *
     * @param isUP boolean determining direction of comparison
     * @param limit value used as floor
     * @return predicate that compares Long input with provided value.
     */
    public static LongPredicate randomLongPredicate(boolean isUP, long limit) {
        return (LongPredicate) randomGenericPredicate(isUP, limit, 
                (Long v1, Long v2) -> (v1 > v2) ? 1 : (v1 < v2) ? -1 : 0);
    }
    
    /**
     * Random StringBuilder predicate builder
     * 
     * @param type StringPredicateType 
     * @param value String unused
     * @return predicate over StringBuilder input
     */
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

    /**
     * Predicate over Generic extends CharSequence
     * 
     * @param startType CharType enum determining pattern over which char comparison is evaluated
     * @param first boolean determining whether first or last char should be compared with template
     * @return predicate comparing chosen char in input with template
     */
    public static Predicate<? extends CharSequence> randomSBPredicate(CharType startType,
            boolean first) {
        switch (startType) {
            case DIGIT:
                return (sb) -> Character.isDigit(firstOrLastChar(sb, first));
            case LOWERCASE:
                return (sb) -> Character.isLowerCase(firstOrLastChar(sb, first));
            case UPPERCASE:
                return (sb) -> Character.isUpperCase(firstOrLastChar(sb, first));
            default:
                return (sb) -> !Character.isLetterOrDigit(firstOrLastChar(sb, first));
        }
    }
    
    /**
     *
     * @param start Path constant against which comparison is evaluated
     * @return predicate over Path variables determining whether input path starts with provided start
     */
    public static Predicate<Path> startPathPredicate(Path start) {
        return (p) -> p.startsWith(start);
    }

    /**
     *
     * @param selected mode of work: 0 - zero elements, 1 - all input into one element;
     * 2 - input into elements of unit length, 3 - every element is full copy of input
     * @param unit length of resulting Stream elements
     * @return function that processes StringBuilder input into Stream sequence of StringBuilders
     */
    public static Function<StringBuilder, Stream<StringBuilder>> genSBFlatMapper(int selected, int unit) {
        switch (selected) {
            case 0:
                //Generate a empty collection
                return (e) -> new ArrayList<StringBuilder>().stream();
            case 1:
                return (e) -> {
                    ArrayList<StringBuilder> res = new ArrayList<>();
                    res.add(e);
                    return res.stream();
                };
            case 2:
                return (StringBuilder e) -> {
                    ArrayList<StringBuilder> res = new ArrayList<>();
                    int step = e.length() / unit + unit - 1;
                    for (int i = 0; i < e.length(); i += step) {
                        res.add(new StringBuilder(e.substring(i, i + step >= e.length()
                                ? e.length() - 1 : i + step)));
                    }
                    return res.stream();
                };
            case 3:
            default:
                //Generate 64 folded flat map
                return (StringBuilder e) -> {
                    ArrayList<StringBuilder> res = new ArrayList<>();
                    int step = e.length() / unit + unit - 1;
                    for (int i = 0; i < e.length(); i += step) {
                        res.add(e);
                    }
                    return res.stream();
                };
        }
    }

    /**
     *
     * @param selected mode of work
     * @return Stream<Integer> with elements from processed Integer 
     */
    public static Function<Integer, Stream<Integer>> genIntegerFlatMapper(int selected) {
        switch (selected) {
            case 0:
                //Generate a empty collection
                return (e) -> new ArrayList<Integer>().stream();
            case 1:
                return (e) -> {
                    ArrayList<Integer> res = new ArrayList<>();
                    res.add(e);
                    return res.stream();
                };
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

    /**
     *
     * @param selected mode of work
     * @return IntStream with elements from processed integer input
     */
    public static IntFunction<IntStream> genIntFlatMapper(int selected) {
        switch (selected) {
            case 0:
                //Generate a empty collection
                return (e) -> {
                    return IntStream.empty();
                };
            case 1:
                return (e) -> {
                    return IntStream.of(e);
                };
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

    /**
     *
     * @return predicate that evaluated characters on being digits
     */
    public static Predicate<Character> isDigitCharacterPredicate() {
        return (c) -> Character.isDigit(c);
    }

    /**
     * Returns function that extracts highest or lowest digit from input
     * 
     * @param isHighest boolean determining position from which number is extracted
     * @return function from Integer to Integer
     */
    public static Function<Integer, Integer> posIntegerFunction(boolean isHighest) {
        if (isHighest) {
            return (i) -> Integer.valueOf(new StringBuilder().append(i < 0 ? -i : i).reverse().toString()) % 10;
        } else {
            return (i) -> i % 10 < 0 ? -i % 10 : i % 10;
        }
    }

    /**
     * Returns function that extracts highest or lowest digit from input
     * 
     * @param isHighest boolean determining position from which number is extracted
     * @return function from Integer to Integer
     */
    @SuppressWarnings("unchecked")//Should be ok to ignore since it is basicly cast of Integer input to int
    public static IntFunction<Integer> posIntFunction(boolean isHighest) {
        return (IntFunction<Integer>) posIntegerFunction(isHighest);
    }

    /**
     * Returns function that extracts highest digit from input
     * 
     * @return function from Integer to Integer
     */
    @SuppressWarnings("unchecked")//Should be ok to ignore since it is basicly cast of Integer output to int
    public static ToIntFunction<Integer> highestPosValueIntFunction() {
        return (ToIntFunction<Integer>) posIntegerFunction(true);
    }

    /**
     * Returns function that extracts lowest digit from input
     * 
     * @return function from Integer to Integer
     */    
    @SuppressWarnings("unchecked")//Should be ok to ignore since it is basicly cast of Integer output to int
    public static ToIntFunction<Integer> lowestPosValueIntFunction() {
        return (ToIntFunction<Integer>) posIntegerFunction(false);
    }

    /**
     * Builds function that returns for a StringBuilder type of its first or last character
     * 
     * @param isFirst boolean determines whether first or last character is evaluated
     * @return function returns CharType of character for inputed StringBuilder
     */
    public static Function<StringBuilder, CharType> sbGenericFunction(boolean isFirst) {
        return (StringBuilder e) -> {
            int checkIndex;
            if (isFirst) {
                checkIndex = 0;
            } else {
                checkIndex = e.length() - 1;
            }

            return Character.isAlphabetic(e.charAt(checkIndex)) ? (Character.isUpperCase(e.charAt(checkIndex)) ? CharType.UPPERCASE : CharType.LOWERCASE) : (Character.isDigit(e.charAt(checkIndex)) ? CharType.DIGIT : CharType.SPECIAL);
        };
    }

    /**
     * Builds a function that for String key applies selected mapping to selected item in the map
     * 
     * @param m Map<String, Integer> which elements are operated on
     * @param op IntOp operation to be executed
     * @param value that will be second operand in selected operation
     * @return function from String key input to Integer processed values
     */
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

    /**
     *
     * @return function that for two inputted Integer values returns 
     * function that generates random Integer value between selected boundaries
     */
    public static BiFunction<Integer, Integer, Integer> randBetweenIntegerFunction() {
        return (t1, t2) -> randBetween(t1, t2);
    }

    /**
     *
     * @param low boundary
     * @param up boundary
     * @return function generating random int between boundaries
     */
    public static int randBetween(int low, int up) {
        assert (low < up && low >= 0);
        Random rand = new Random();
        int i = rand.nextInt(up - low);
        i += low;
        return i;
    }

    /**
     *
     * @param <T> type of serviced Set
     * @param set set that is being serviced
     * @return function that adds its input into selected set
     */
    public static <T> Consumer<T> reverseConsumer(Set<T> set) {
        return t -> {
            set.add(t);
        };
    }

    /**
     *
     * @param sb StringBuilder that is being serviced
     * @return function that appends its StringBuilder input to selected StringBuilder
     */
    public static Consumer<StringBuilder> appendSBConsumer(StringBuilder sb) {
        return t -> {
            sb.append(t);
        };
    }

    /**
     *
     * @param ai AtomicInteger that is being services
     * @return function that adds its Integer input to selected AtomicInteger
     */
    public static Consumer<Integer> addIntegerConsumer(AtomicInteger ai) {
        return t -> {
            ai.updateAndGet(t1 -> t1 + t);
        };
    }

    /**
     *
     * @param ai AtomicInteger that is being services
     * @return function that adds its int input to selected AtomicInteger
     */
    public static IntConsumer addIntConsumer(AtomicInteger ai) {
        return t -> {
            ai.updateAndGet(t1 -> t1 + t);
        };
    }

    /**
     *
     * @param ai AtomicLong that is being services
     * @return function that adds its integer input to selected AtomicLong
     */    
    public static IntConsumer addLongConsumer(AtomicLong ai) {
        return t -> {
            ai.updateAndGet(t1 -> t1 + t);
        };
    }

    /**
     *
     * @param <T> type of elements of list
     * @param list that is being services
     * @return function that adds its T input to selected List
     */    
    public static <T> Consumer<T> copyConsumer(List<T> list) {
        return t -> {
            list.add(t);
        };
    }

    /**
     *
     * @param <T> type of elements of list
     * @param in Collection template collection
     * @param out Collection output collection
     * @return function that checks its T input against containing in 'in' collection then adds it to 'out'
     * collection if collection 'in' contains it
     */
    public static <T> Consumer<T> existsConsumer(Collection<T> in, Collection<T> out) {
        return t -> {
            if (in.contains(t)) {
                out.add(t);
            }
        };
    }

    /**
     *
     * @param <T> type of value
     * @param value to be supplied 
     * @return function that returns selected value on invocation
     */
    public static <T> Supplier<T> genericSuppiler(T value) {
        return () -> value;
    }

    /**
     *
     * @param value to be supplied 
     * @return function that returns selected value on invocation
     */
    public static Supplier<StringBuilder> sbSupplier(StringBuilder value) {
        return genericSuppiler(value);
    }

    /**
     *
     * @param value to be supplied 
     * @return function that returns selected value on invocation
     */    
    public static Supplier<Integer> integerSupplier(int value) {
        return genericSuppiler(value);
    }

    /**
     *
     * @param value to be supplied 
     * @return function that returns selected value on invocation
     */    
    public static Supplier<AtomicInteger> atomicIntegerSupplier(int value) {
        AtomicInteger buf = new AtomicInteger(value);
        return genericSuppiler(buf);
    }

    /**
     *
     * @param <T> type of input value
     * @param value to be supplied 
     * @return function that returns selected value on invocation
     */    
    public static <T> Supplier<AtomicReference<T>> atomicGenericSupplier(T value) {
        AtomicReference<T> buf = new AtomicReference<>(value);
        return genericSuppiler(buf);
    }

    /**
     *
     * @param value to be supplied 
     * @return function that returns selected value on invocation
     */    
    public static Supplier<AtomicReference<StringBuilder>> atomicSBSupplier(StringBuilder value) {
        AtomicReference<StringBuilder> buf = new AtomicReference<>(value);
        return genericSuppiler(buf);
    }

    /**
     *
     * @param value to be supplied 
     * @return function that returns selected value on invocation
     */    
    public static IntSupplier intSupplier(int value) {
        return () -> value;
    }

    /**
     *
     * @param value to be supplied 
     * @return function that returns selected value on invocation
     */    
    public static Supplier<Long> longSupplier(long value) {
        return () -> value;
    }

    /**
     *
     * @param op IntOp - operation to be applied to input
     * @param value other operand of operation
     * @return function that applies determined operation to its input
     */
    public static Function<Integer, Integer> opIntegerFunction(IntOp op, int value) {
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

    /**
     *
     * @param op IntOp - operation to be applied to input
     * @param value other operand of operation
     * @return function that applies determined operation to its input
     */
    @SuppressWarnings("unchecked")//Safe to ignore since UnaryOperator<T> extends Function<T, T>
    public static UnaryOperator<Integer> opIntegerUnaryOperator(IntOp op, int value) {
        return (UnaryOperator) opIntegerFunction(op, value);
    }

    /**
     *
     * @param op IntOp - operation to be applied to input
     * @param value other operand of operation
     * @return function that applies determined operation to its input
     */
    public static IntUnaryOperator opIntUnaryOperator(IntOp op, int value) {
        return (IntUnaryOperator) opIntegerFunction(op, value);
    }

    /**
     *
     * @param op IntOp - operation to be applied to input
     * @param value other operand of operation
     * @return function that applies determined operation to its input
     */
    @SuppressWarnings("unchecked")//Probably safe to ignore since its just result cast from Integer to int
    public static ToIntFunction<Integer> opToIntFunction(IntOp op, int value) {
        return (ToIntFunction<Integer>) opIntegerFunction(op, value);
    }

    /**
     *
     * @param op IntOp - operation to be applied to input
     * @param value other operand of operation
     * @return function that applies determined operation to its input
     */    
    @SuppressWarnings("unchecked")//Safe to ignore since its just parameter cast from Integer to int and its int any way
    public static IntFunction<Integer> opIntFunction(IntOp op, int value) {
        return (IntFunction<Integer>) opIntegerFunction(op, value);
    }

    /**
     *
     * @param value other operand of operation
     * @return function that adds determined value to its input
     */    
    public static IntUnaryOperator addIntUnaryOperator(int value) {
        return (IntUnaryOperator) opIntegerFunction(IntOp.ADD, value);
    }

    /**
     *
     * @param value other operand of operation
     * @return function that subtracts determined value from its input
     */    
    public static IntUnaryOperator subIntUnaryOperator(int value) {
        return (IntUnaryOperator) opIntegerFunction(IntOp.SUBTRACT, value);
    }

    /**
     *
     * @param value other operand of operation
     * @return function that multiplies determined value to its input
     */    
    public static IntUnaryOperator mulIntUnaryOperator(int value) {
        return (IntUnaryOperator) opIntegerFunction(IntOp.MULTIPLY, value);
    }

    /**
     *
     * @param value other operand of operation
     * @return function that divides its input with determined value
     */    
    public static IntUnaryOperator divIntUnaryOperator(int value) {
        return (IntUnaryOperator) opIntegerFunction(IntOp.DIVIDE, value);
    }


    /**
     *
     * @param op IntOp - operation to be applied to input
     * @param value long - other operand of operation
     * @return function that applies determined operation to its input
     */
    public static Function<Long, Long> opLongFunction(IntOp op, long value) {
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
    
    /**
     *
     * @param value long - other operand of operation
     * @return function that adds determined value to its input
     */    
    public static LongUnaryOperator addLongUnaryOperator(long value) {
        return (LongUnaryOperator) opLongFunction(IntOp.ADD, value);
    }

    /**
     *
     * @param value long - other operand of operation
     * @return function that subtracts determined value from its input
     */    
    public static LongUnaryOperator subLongUnaryOperator(long value) {
        return (LongUnaryOperator) opLongFunction(IntOp.SUBTRACT, value);
    }

    /**
     *
     * @param value long - other operand of operation
     * @return function that multiplies determined value to its input
     */    
    public static LongUnaryOperator mulLongUnaryOperator(long value) {
        return (LongUnaryOperator) opLongFunction(IntOp.MULTIPLY, value);
    }

    /**
     *
     * @param value long - other operand of operation
     * @return function that divides its input with determined value
     */ 
    public static LongUnaryOperator divLongUnaryOperator(long value) {
        return (LongUnaryOperator) opLongFunction(IntOp.DIVIDE, value);
    }

    /**
     *
     * @param value StringBuilder to be appended
     * @return function that appends determined value to its input
     */ 
    public static UnaryOperator<StringBuilder> appendSBUnaryOperator(StringBuilder value) {
        return t -> t.append(value);
    }

    /**
     *
     * @param <T> type of generic inputs
     * @param c Comparator
     * @return function that returns minimal from its input
     */
    public static <T> BinaryOperator<T> minGenericBinaryOperator(Comparator<T> c) {
        return (t1, t2) -> c.compare(t1, t2) < 0 ? t1 : t2;
    }

    /**
     *
     * @return function that returns minimal from its input
     */
    public static IntBinaryOperator minIntBinaryOperator() {
        return (t1, t2) -> t1 < t2 ? t1 : t2;
    }

    /**
     *
     * @param c Comparator
     * @return function that returns minimal from its input
     */
    public static BinaryOperator<Integer> minIntegerBinaryOperator(Comparator<Integer> c) {
        return minGenericBinaryOperator(c);
    }

    /**
     *
     * @param c Comparator
     * @return function that returns minimal from its input
     */
    public static BinaryOperator<StringBuilder> minSBBinaryOperator(Comparator<StringBuilder> c) {
        return minGenericBinaryOperator(c);
    }

    /**
     *
     * @param <T> type of generic inputs
     * @param c Comparator
     * @return function that returns maximal from its input
     */
    public static <T> BinaryOperator<T> maxGenericBinaryOperator(Comparator<T> c) {
        return (t1, t2) -> c.compare(t1, t2) < 0 ? t2 : t1;
    }

    /**
     *
     * @return function that returns maximal from its input
     */
    public static IntBinaryOperator maxIntBinaryOperator() {
        return (t1, t2) -> (t1 < t2) ? t2 : t1;
    }

    /**
     *
     * @param c Comparator
     * @return function that returns maximal from its input
     */
    public static BinaryOperator<Integer> maxIntegerBinaryOperator(Comparator<Integer> c) {
        return maxGenericBinaryOperator(c);
    }

    /**
     *
     * @param c Comparator
     * @return function that returns maximal from its input
     */
    public static IntBinaryOperator maxIntBinaryOperator(Comparator<Integer> c) {
        return (IntBinaryOperator) maxGenericBinaryOperator(c);
    }

    /**
     *
     * @param c Comparator
     * @return function that returns maximal from its input
     */
    public static BinaryOperator<StringBuilder> maxSBBinaryOperator(Comparator<StringBuilder> c) {
        return maxGenericBinaryOperator(c);
    }

    /**
     *
     * @return function that returns sum of its input
     */
    public static BinaryOperator<Integer> addIntegerBinaryOperator() {
        return (t1, t2) -> t1 + t2;
    }

    /**
     *
     * @return function that returns maximal from its input
     */
    public static IntBinaryOperator addIntBinaryOperator() {
        return (t1, t2) -> t1 + t2;
    }

    /**
     *
     * @return function that returns maximal from its input
     */
    public static BinaryOperator<BigDecimal> addBigDecimalBinaryOperator() {
        return (t1, t2) -> t1.add(t2);
    }

    /**
     *
     * @return function that returns maximal from its input
     */
    public static DoubleBinaryOperator addDoubleBinaryOperator() {
        return (t1, t2) -> t1 + t2;
    }

    /**
     *
     * @return function that concatenates two inputed StringBuilders
     */
    public static BinaryOperator<StringBuilder> appendSBBinaryOperator() {
        return (t1, t2) -> new StringBuilder().append(t1).append(t2);
    }

    /**
     *
     * @return function that returns difference between its two input values
     */
    public static BinaryOperator<Integer> subIntegerBinaryOperator() {
        return (t1, t2) -> t1 - t2;
    }

    /**
     *
     * @return function that returns difference between its two input values
     */
    public static IntBinaryOperator subIntBinaryOperator() {
        return (t1, t2) -> t1 - t2;
    }

    /**
     *
     * @return function that subtracts one inputted StringBuilder from another
     */
    public static BinaryOperator<StringBuilder> deleteSBBinaryOperator() {
        return (t1, t2) -> {
            StringBuilder minuend;
            StringBuilder subtrahend;
            if (t1.length() >= t2.length()) {
                minuend = t1;
                subtrahend = t2;
            } else {
                minuend = t2;
                subtrahend = t1;
            }

            int i1 = minuend.indexOf(subtrahend.toString());
            int i2 = i1 + subtrahend.length();

            return new StringBuilder(minuend).delete(i1, i2);
        };
    }

    /**
     *
     * @return function that returns product between its two input values
     */
    public static IntBinaryOperator mulIntBinaryOperator() {
        return (t1, t2) -> t1 * t2;
    }

    /**
     *
     * @return function that returns division result between its two input values
     */
    public static IntBinaryOperator divIntBinaryOperator() {
        return (t1, t2) -> t1 / t2;
    }

    /**
     *
     * @return function that returns sum between its two long input values
     */
    public static LongBinaryOperator addLongBinaryOperator() {
        return (t1, t2) -> t1 + t2;
    }

    /**
     *
     * @return function that returns subtraction result between its two long input values
     */
    public static LongBinaryOperator subLongBinaryOperator() {
        return (t1, t2) -> t1 - t2;
    }

    /**
     *
     * @return function that returns product between its two long input values
     */
    public static LongBinaryOperator mulLongBinaryOperator() {
        return (t1, t2) -> t1 * t2;
    }

    /**
     *
     * @return function that returns division result between its two long input values
     */
    public static LongBinaryOperator divLongBinaryOperator() {
        return (t1, t2) -> t1 / t2;
    }

    /**
     *
     * @return function that adds second input Integer value to first AtomicInteger input
     */
    public static BiConsumer<AtomicInteger, Integer> addIntegerBiConsumer() {
        return (t1, t2) -> {
            t1.addAndGet(t2);
        };
    }

    /**
     *
     * @return function that adds second input AtomicInteger value to first AtomicInteger input
     */
    public static BiConsumer<AtomicInteger, AtomicInteger> addAtomicIntegerBiConsumer() {
        return (t1, t2) -> {
            t1.addAndGet(t2.get());
        };
    }

    /**
     *
     * @return function that appends its second StringBuffer input to first 
     * AtomicReference of StringBuiled input
     */
    public static BiConsumer<AtomicReference<StringBuilder>, StringBuilder> appendSBBiConsumer() {
        return (t1, t2) -> {
            t1.updateAndGet(appendSBUnaryOperator(t2));
        };
    }

    /**
     *
     * @return function that appends its second AtomicReference of StringBuffer input to first 
     * AtomicReference of StringBuiled input
     */
    public static BiConsumer<AtomicReference<StringBuilder>, AtomicReference<StringBuilder>> appendAtomicSBBiConsumer() {
        return (t1, t2) -> {
            t1.updateAndGet(appendSBUnaryOperator(t2.get()));
        };
    }

    /**
     *
     * @param c Comparator used in constructed function
     * @return function that replaces its first input with maximal value of its two inputs
     */
    public static BiConsumer<AtomicInteger, Integer> maxIntegerBiConsumer(Comparator<Integer> c) {
        return (t1, t2) -> {
            t1.getAndUpdate(t -> max(t, t2, c));
        };
    }

    /**
     *
     * @param <T> type of input values
     * @param c Comparator used in constructed function
     * @return function that replaces its first input with maximal value of its two inputs
     */
    public static <T> BiConsumer<AtomicReference<T>, T> maxGenericBiConsumer(Comparator<T> c) {
        return (t1, t2) -> {
            t1.getAndUpdate(t -> max(t, t2, c));
        };
    }

    /**
     *
     * @param c Comparator used in constructed function
     * @return function that replaces its first input with maximal value of its two inputs
     */
    public static BiConsumer<AtomicInteger, AtomicInteger> maxAtomicIntegerBiConsumer(Comparator<Integer> c) {
        return (t1, t2) -> {
            t1.getAndUpdate(t -> max(t, t2.get(), c));
        };
    }

    /**
     *
     * @param <T> type of input values
     * @param c Comparator used in constructed function
     * @return function that replaces its first input with maximal value of its two inputs
     */
    public static <T> BiConsumer<AtomicReference<T>, AtomicReference<T>> maxAtomicGenericBiConsumer(Comparator<T> c) {
        return (t1, t2) -> {
            t1.getAndUpdate(t -> max(t, t2.get(), c));
        };
    }

    /**
     *
     * @param c Comparator used in constructed function
     * @return function that replaces its first input with minimal value of its two inputs
     */
    public static BiConsumer<AtomicInteger, Integer> minIntegerBiConsumer(Comparator<Integer> c) {
        return (t1, t2) -> {
            t1.getAndUpdate(t -> min(t, t2, c));
        };
    }

    /**
     *
     * @param <T> type of input values
     * @param c Comparator used in constructed function
     * @return function that replaces its first input with minimal value of its two inputs
     */
    public static <T> BiConsumer<AtomicReference<T>, T> minGenericBiConsumer(Comparator<T> c) {
        return (t1, t2) -> {
            t1.getAndUpdate(t -> min(t, t2, c));
        };
    }

    /**
     *
     * @param c Comparator used in constructed function
     * @return function that replaces its first input with minimal value of its two inputs
     */
    public static BiConsumer<AtomicInteger, AtomicInteger> minAtomicIntegerBiConsumer(Comparator<Integer> c) {
        return (t1, t2) -> {
            t1.getAndUpdate(t -> min(t, t2.get(), c));
        };
    }

    /**
     *
     * @param <T> type of input values
     * @param c Comparator used in constructed function
     * @return function that replaces its first input with minimal value of its two inputs
     */
    public static <T> BiConsumer<AtomicReference<T>, AtomicReference<T>> minAtomicGenericBiConsumer(Comparator<T> c) {
        return (t1, t2) -> {
            t1.getAndUpdate(t -> min(t, t2.get(), c));
        };
    }

    /**
     *
     * @param c Comparator used in constructed function
     * @return function that returns maximal value of its two inputs
     */
    public static BiFunction<Integer, Integer, Integer> maxIntegerFunction(Comparator<Integer> c) {
        return (t1, t2) -> max(t1, t2, c);
    }

    public static BiFunction<BigDecimal, Integer, BigDecimal> deviationSequareFunction(double avg) {
        return (bd, t) -> bd.add(new BigDecimal(avg - t).pow(2));
    }

    /**
     *
     * @param <T> type of input and output
     * @param c Comparator used in constructed function
     * @return function that returns maximal value of its two inputs
     */
    public static <T> BiFunction<T, T, T> maxGenericFunction(Comparator<T> c) {
        return (t1, t2) -> max(t1, t2, c);
    }

    /**
     *
     * @param c Comparator used in constructed function
     * @return function that returns maximal value of its two inputs
     */
    public static BiFunction<StringBuilder, StringBuilder, StringBuilder> maxStringBuilderFunction(Comparator<StringBuilder> c) {
        return (t1, t2) -> max(t1, t2, c);
    }

    /**
     *
     * @param c Comparator used in constructed function
     * @return function that returns minimal value of its two inputs
     */
    public static BiFunction<Integer, Integer, Integer> minIntegerFunction(Comparator<Integer> c) {
        return (t1, t2) -> min(t1, t2, c);
    }

    /**
     *
     * @param <T> type of input and output values
     * @param c Comparator used in constructed function
     * @return function that returns minimal value of its two inputs
     */
    public static <T> BiFunction<T, T, T> minGenericFunction(Comparator<T> c) {
        return (t1, t2) -> min(t1, t2, c);
    }

    /**
     *
     * @param c Comparator used in constructed function
     * @return function that returns minimal value of its two inputs
     */
    public static BiFunction<StringBuilder, StringBuilder, StringBuilder> minStringBuilderFunction(Comparator<StringBuilder> c) {
        return (t1, t2) -> min(t1, t2, c);
    }

    /**
     *
     * @param op IntOp - operation to be performed
     * @param value other operand of operation
     * @return function that returns result of application of operation to 
     * determined value and its second input
     */
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

    /**
     *
     * @param op IntOp - operation to be performed
     * @return function that returns result of application of operation to 
     * its first and second input values
     */
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

    private static <T> T min(T i1, T i2, Comparator<T> c) {
        return c.compare(i1, i2) < 0 ? i1 : i2;
    }

    private static <T> T max(T i1, T i2, Comparator<T> c) {
        return c.compare(i1, i2) < 0 ? i2 : i1;
    }
    
    private static Character firstOrLastChar(CharSequence sb, boolean first) {
        return sb.charAt(first ? 0 : sb.toString().length() - 1);
    }

    /*
     * Construct a Collection C object based on a C object, using generic type
     * instead of Class type can help preventing type error in compilation
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <E, C extends Collection<E>> C create(C c, int... initSize)
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
    public static <E, T extends Collection<E>> T create(
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
    public static <K, V, M extends Map<K, V>> M createMap(M m, int... initSize)
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
    public static <K, V, M extends Map<K, V>> M createMap(Class<? extends Map<K, V>> cls,
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
