package test.java.util.stream;

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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.ITest;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import test.java.util.LambdaUtilities;
import test.java.util.StringUtilities;

public class StringBuilderStreamTest<T extends Collection<StringBuilder>> extends StreamTestTemplate<StringBuilder> {

    private final static int MIN_LEN = 1 << 2;

    private final static int MAX_LEN = 1 << 8;

    private final static Comparator<StringBuilder> NATRUAL_ORDER_CMP = (sb1, sb2) -> sb1.toString().compareTo(sb2.toString());

    private final static Comparator[] cmps = {NATRUAL_ORDER_CMP, NATRUAL_ORDER_CMP.reversed()};

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Class[] defaultConstructorClazz = {
        (Class<ArrayDeque<StringBuilder>>) (Class<?>) (ArrayDeque.class),
        (Class<ArrayList<StringBuilder>>) (Class<?>) (ArrayList.class),
        (Class<ConcurrentLinkedDeque<StringBuilder>>) (Class<?>) (ConcurrentLinkedDeque.class),
        (Class<ConcurrentLinkedQueue<StringBuilder>>) (Class<?>) (ConcurrentLinkedQueue.class),
        //        (Class<ConcurrentSkipListSet<StringBuilder>>)(Class<?>)(ConcurrentSkipListSet.class),
        (Class<CopyOnWriteArrayList<StringBuilder>>) (Class<?>) (CopyOnWriteArrayList.class),
        (Class<HashSet<StringBuilder>>) (Class<?>) (HashSet.class),
        (Class<LinkedBlockingDeque<StringBuilder>>) (Class<?>) (LinkedBlockingDeque.class),
        (Class<LinkedBlockingQueue<StringBuilder>>) (Class<?>) (LinkedBlockingQueue.class),
        (Class<LinkedHashSet<StringBuilder>>) (Class<?>) (LinkedHashSet.class),
        (Class<LinkedList<StringBuilder>>) (Class<?>) (LinkedList.class),
        (Class<LinkedTransferQueue<StringBuilder>>) (Class<?>) (LinkedTransferQueue.class),
        //        (Class<PriorityBlockingQueue<StringBuilder>>)(Class<?>)(PriorityBlockingQueue.class),
        //        (Class<PriorityQueue<StringBuilder>>)(Class<?>)(PriorityQueue.class),
        (Class<Stack<StringBuilder>>) (Class<?>) (Stack.class),
        //        (Class<TreeSet<StringBuilder>>)(Class<?>)(TreeSet.class),
        (Class<Vector<StringBuilder>>) (Class<?>) (Vector.class)
    };

    @Factory
    @SuppressWarnings(value = {"rawtypes", "unchecked"})
    public static Object[] create() {
        List<StringBuilderStreamTest> result = new ArrayList<>();
        Stream<Class> stream1 = Arrays.stream(defaultConstructorClazz);
        stream1.forEach(clazz -> result.add(new StringBuilderStreamTest(clazz)));
        return result.toArray();
    }

    protected static boolean verifyMatch(Collection<StringBuilder> c, LambdaUtilities.CharType charType, boolean isFirst, boolean all) {
        Iterator<StringBuilder> it = c.iterator();
        while (it.hasNext()) {
            StringBuilder current = it.next();
            char checkChar = current.charAt(isFirst ? 0 : current.toString().length() - 1);
            Function<Boolean, Boolean> conditional = (check) -> {
                if (!all && check) {
                    return true;
                } else if (all & !check) {
                    return false;
                }
                return all;
            };
            
            switch (charType) {
                case DIGIT:
                    return conditional.apply(Character.isDigit(checkChar));
                case LOWERCASE:
                    return conditional.apply(Character.isLowerCase(checkChar));
                case UPPERCASE:
                    return conditional.apply(Character.isUpperCase(checkChar));
                default:
                    return conditional.apply((!Character.isLetterOrDigit(checkChar)));
            }
        }
        return all;
    }

    protected Class<T> typeObject;

    public StringBuilderStreamTest(Class<T> clazz, int... initSizes) {
        super(clazz, initSizes);
        this.typeObject = clazz;
    }

    @Override
    public String getTestName() {
        return typeObject.getName() + "<StringBuilder>";
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAllMatch() throws Exception {
        Iterator<ParallelType> iter
                = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<StringBuilder> c1 = generateData(DATA_SIZE);
            Stream<StringBuilder> stream1 = (type == ParallelType.Parallel) ? c1.parallelStream()
                    : (type == ParallelType.Sequential) ? c1.parallelStream().sequential()
                    : c1.stream();
            EnumSet<LambdaUtilities.CharType> set = EnumSet.allOf(LambdaUtilities.CharType.class);
            LambdaUtilities.CharType charType
                    = (LambdaUtilities.CharType) set.toArray()[rand.nextInt(4)];
            boolean isFirst = rand.nextBoolean();
            assertEquals(stream1.allMatch((Predicate<StringBuilder>) LambdaUtilities.randomSBPredicate(charType, isFirst)),
                    verifyMatch(c1, charType, isFirst, true));

            //Empty stream's allMatch  will return true always
            @SuppressWarnings("cast")
                    Collection<StringBuilder> emptyCol = hasIni
                    ? (Collection<StringBuilder>) LambdaUtilities.create(typeObject, initSize)
                    : (Collection<StringBuilder>) LambdaUtilities.create(typeObject);
            Stream<StringBuilder> stream2 = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.parallelStream().sequential()
                    : emptyCol.stream();
            assertTrue(stream2.allMatch((Predicate<StringBuilder>) LambdaUtilities.randomSBPredicate(charType, isFirst)));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAnyMatch() throws Exception {
        Iterator<ParallelType> iter
                = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<StringBuilder> c1 = generateData(DATA_SIZE);
            Stream<StringBuilder> stream1 = (type == ParallelType.Parallel) ? c1.parallelStream()
                    : (type == ParallelType.Sequential) ? c1.parallelStream().sequential()
                    : c1.stream();
            EnumSet<LambdaUtilities.CharType> set = EnumSet.allOf(LambdaUtilities.CharType.class);
            LambdaUtilities.CharType charType
                    = (LambdaUtilities.CharType) set.toArray()[rand.nextInt(4)];
            boolean isFirst = rand.nextBoolean();
            assertEquals(stream1.anyMatch((Predicate<StringBuilder>) LambdaUtilities.randomSBPredicate(charType, isFirst)),
                    verifyMatch(c1, charType, isFirst, false));

            //Empty stream's anyMatch  will return false always
            @SuppressWarnings("cast")
                    Collection<StringBuilder> emptyCol = hasIni
                    ? (Collection<StringBuilder>) LambdaUtilities.create(typeObject, initSize)
                    : (Collection<StringBuilder>) LambdaUtilities.create(typeObject);
            Stream<StringBuilder> stream2 = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.parallelStream().sequential()
                    : emptyCol.stream();
            assertTrue(!stream2.anyMatch((Predicate<StringBuilder>) LambdaUtilities.randomSBPredicate(charType, isFirst)));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConcat() throws Exception {
        Iterator<ParallelType> iter
                = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            //Concate two list's stream, then filter one predicate should be expect
            //to be same as filter two stream repectively then concate the result.
            Collection<StringBuilder> l1 = generateData(DATA_SIZE);
            Collection<StringBuilder> l2 = generateData(DATA_SIZE);
            EnumSet<LambdaUtilities.CharType> set = EnumSet.allOf(LambdaUtilities.CharType.class);
            LambdaUtilities.CharType charType
                    = (LambdaUtilities.CharType) set.toArray()[rand.nextInt(4)];
            boolean isFirst = rand.nextBoolean();
            Predicate<StringBuilder> p = (Predicate<StringBuilder>) LambdaUtilities.randomSBPredicate(charType, isFirst);

            Stream<StringBuilder> stream11 = (type == ParallelType.Parallel) ? l1.parallelStream()
                    : (type == ParallelType.Sequential) ? l1.parallelStream().sequential()
                    : l1.stream();
            Stream<StringBuilder> stream21 = (type == ParallelType.Parallel) ? l2.parallelStream()
                    : (type == ParallelType.Sequential) ? l2.parallelStream().sequential()
                    : l2.stream();
            Collection<StringBuilder> result1 = stream11.filter(p).collect(Collectors.toCollection(LinkedList<StringBuilder>::new));
            Collection<StringBuilder> result2 = stream21.filter(p).collect(Collectors.toCollection(LinkedList<StringBuilder>::new));
            result1.addAll(result2);

            Stream<StringBuilder> stream12 = (type == ParallelType.Parallel) ? l1.parallelStream()
                    : (type == ParallelType.Sequential) ? l1.parallelStream().sequential()
                    : l1.stream();
            Stream<StringBuilder> stream22 = (type == ParallelType.Parallel) ? l2.parallelStream()
                    : (type == ParallelType.Sequential) ? l2.parallelStream().sequential()
                    : l2.stream();
            List<String> expectedList = Stream.concat(stream12, stream22).filter(p).map(t -> t.toString()).collect(Collectors.<String>toList());
            List<String> testList = result1.stream().map(t -> t.toString()).collect(Collectors.<String>toList());

            Collections.sort(testList);
            Collections.sort(expectedList);
            assertEquals(testList, expectedList);

            Collection<StringBuilder> emptyList = hasIni ? LambdaUtilities.create(typeObject, initSize)
                    : LambdaUtilities.create(typeObject);
            Stream<StringBuilder> stream3 = (type == ParallelType.Parallel) ? emptyList.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyList.parallelStream().sequential()
                    : emptyList.stream();
            List<StringBuilder> result3 = Stream.concat(l1.stream(), stream3).collect(Collectors.<StringBuilder>toList());
            List<StringBuilder> list1 = new ArrayList<StringBuilder>(l1);
            Collections.sort(list1, NATRUAL_ORDER_CMP);
            Collections.sort(result3, NATRUAL_ORDER_CMP);
            assertEquals(list1, result3);
        }
    }

    @Test
    public void testFind() throws Throwable {
        Iterator<ParallelType> iter
                = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<StringBuilder> c = generateData(DATA_SIZE);
            Stream<StringBuilder> stream1 = (type == ParallelType.Parallel) ? c.parallelStream()
                    : (type == ParallelType.Sequential) ? c.stream().sequential()
                    : c.stream();
            Stream<StringBuilder> stream2 = (type == ParallelType.Parallel) ? c.parallelStream()
                    : (type == ParallelType.Sequential) ? c.stream().sequential()
                    : c.stream();
            java.util.Optional<StringBuilder> opAny = stream1.findAny();
            java.util.Optional<StringBuilder> opFirst = stream2.findFirst();
            assertTrue(opAny.isPresent());
            assertTrue(opFirst.isPresent());
            if (!stream1.isParallel()) {
                assertEquals(opAny, opFirst);
            }
            @SuppressWarnings("cast")
                    Collection<StringBuilder> emptyCol = hasIni
                    ? (Collection<StringBuilder>) LambdaUtilities.create(typeObject, initSize)
                    : (Collection<StringBuilder>) LambdaUtilities.create(typeObject);
            Stream<StringBuilder> emptyStream1 = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.stream().sequential()
                    : emptyCol.stream();
            java.util.Optional<StringBuilder> emptyAny = emptyStream1.findAny();
            assertFalse(emptyAny.isPresent());
            Stream<StringBuilder> emptyStream2 = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.stream().sequential()
                    : emptyCol.stream();
            java.util.Optional<StringBuilder> emptyFirsty = emptyStream2.findFirst();
            assertFalse(emptyFirsty.isPresent());
        }
    }

    @Test
    public void testForEach() throws Throwable {
        Iterator<ParallelType> iter
                = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<StringBuilder> c = generateData(DATA_SIZE);
            Stream<StringBuilder> stream = (type == ParallelType.Parallel) ? c.parallelStream()
                    : (type == ParallelType.Sequential) ? c.parallelStream().sequential()
                    : c.stream();
            stream.forEach(t -> {
                assertTrue(c.contains(t));
            });
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFilter() throws Exception {
        Iterator<ParallelType> iter
                = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            //Create predicate with random limit and random up/down size
            Collection<StringBuilder> l = generateData(DATA_SIZE);
            Stream<StringBuilder> stream = (type == ParallelType.Parallel) ? l.parallelStream()
                    : ((type == ParallelType.Sequential) ? l.stream().sequential()
                    : l.stream());
            
            EnumSet<LambdaUtilities.CharType> set = EnumSet.allOf(LambdaUtilities.CharType.class);
            LambdaUtilities.CharType charType
                    = (LambdaUtilities.CharType) set.toArray()[rand.nextInt(4)];
            boolean isFirst = rand.nextBoolean();
            Predicate<StringBuilder> p1 = (Predicate<StringBuilder>) LambdaUtilities.randomSBPredicate(charType, isFirst);
            //Filter the data, check if it works as expected.
            Collection<StringBuilder> result1 = stream.filter(p1).collect(Collectors.toCollection(LinkedList<StringBuilder>::new));
            assertTrue(verifyMatch(result1, charType, isFirst, true));
            //filter on parallel stream can cause IllegalStateException
            stream = (type == ParallelType.Parallel) ? l.parallelStream()
                    : ((type == ParallelType.Sequential) ? l.stream().sequential()
                    : l.stream());
            result1.clear();
            result1 = stream.filter(p1).filter(e -> true).collect(Collectors.toCollection(LinkedList<StringBuilder>::new));
            assertTrue(verifyMatch(result1, charType, isFirst, true));

            //filter with Predicates.alwaysFlase() will get nothing
            stream = (type == ParallelType.Parallel) ? l.parallelStream()
                    : ((type == ParallelType.Sequential) ? l.stream().sequential()
                    : l.stream());
            result1.clear();
            result1 = stream.filter(p1).filter(e -> false).collect(Collectors.toCollection(LinkedList<StringBuilder>::new));
            assertEquals(result1.size(), 0);

            EnumSet<LambdaUtilities.CharType> set2 = EnumSet.allOf(LambdaUtilities.CharType.class);
            LambdaUtilities.CharType charType2
                    = (LambdaUtilities.CharType) set2.toArray()[rand.nextInt(4)];
            boolean isFirst2 = rand.nextBoolean();
            Predicate<StringBuilder> p2 = (Predicate<StringBuilder>) LambdaUtilities.randomSBPredicate(charType2, isFirst2);

            EnumSet<LambdaUtilities.CharType> set3 = EnumSet.allOf(LambdaUtilities.CharType.class);
            LambdaUtilities.CharType charType3
                    = (LambdaUtilities.CharType) set3.toArray()[rand.nextInt(4)];
            boolean isFirst3 = rand.nextBoolean();
            Predicate<StringBuilder> p3 = (Predicate<StringBuilder>) LambdaUtilities.randomSBPredicate(charType3, isFirst3);
            //The reason conver l to sorted is CopyOnWriteArrayList doesn't support
            //sort, we use ArrayList sort data instead
            stream = (type == ParallelType.Parallel) ? l.parallelStream()
                    : ((type == ParallelType.Sequential) ? l.stream().sequential()
                    : l.stream());
            List<StringBuilder> result2 = stream.filter(p1).filter(p2).collect(Collectors.toCollection(ArrayList<StringBuilder>::new));
            stream = (type == ParallelType.Parallel) ? l.parallelStream()
                    : ((type == ParallelType.Sequential) ? l.stream().sequential()
                    : l.stream());
            List<StringBuilder> result3 = stream.filter(p1.and(p2)).collect(Collectors.toCollection(ArrayList<StringBuilder>::new));

            if (result2.isEmpty()) {
                assertEquals(result2.size(), result3.size());
            } else {
                System.out.println(result2.getClass().getCanonicalName());
                Collections.sort(result2, (sb1, sb2) -> sb1.toString().compareTo(sb2.toString()));
                Collections.sort(result3, (sb1, sb2) -> sb1.toString().compareTo(sb2.toString()));
                assertEquals(result2, result3);
            }

            //result2 could be a EmptyList, we're putting result2, result3 into
            // concatList because EmptyList doesn't support addAll
            stream = (type == ParallelType.Parallel) ? l.parallelStream()
                    : ((type == ParallelType.Sequential) ? l.stream().sequential()
                    : l.stream());
            result2 = stream.filter(p1.and(p2)).collect(Collectors.toCollection(ArrayList<StringBuilder>::new));
            stream = (type == ParallelType.Parallel) ? l.parallelStream()
                    : ((type == ParallelType.Sequential) ? l.stream().sequential()
                    : l.stream());
            result3 = stream.filter(p1.or(p2)).collect(Collectors.toCollection(ArrayList<StringBuilder>::new));
            result2.addAll(result3);
            result3.clear();
            stream = (type == ParallelType.Parallel) ? l.parallelStream()
                    : ((type == ParallelType.Sequential) ? l.stream().sequential()
                    : l.stream());
            Stream<StringBuilder> stream2 = (type == ParallelType.Parallel) ? l.parallelStream()
                    : ((type == ParallelType.Sequential) ? l.stream().sequential()
                    : l.stream());
            result3 = Stream.concat(stream.filter(p1), stream2.filter(p2)).collect(Collectors.toCollection(ArrayList<StringBuilder>::new));
            Collections.sort(result2, NATRUAL_ORDER_CMP);
            Collections.sort(result3, NATRUAL_ORDER_CMP);
            assertEquals(result2, result3);

            @SuppressWarnings("cast")
                    Collection<StringBuilder> emptyList = hasIni
                    ? (Collection<StringBuilder>) LambdaUtilities.create(typeObject, initSize)
                    : (Collection<StringBuilder>) LambdaUtilities.create(typeObject);
            Stream<StringBuilder> stream3 = (type == ParallelType.Parallel) ? emptyList.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyList.parallelStream().sequential()
                    : emptyList.stream();
            assertFalse(stream3.filter(p1).iterator().hasNext());
        }
    }

    @Test
    protected void testLimit() throws Throwable {
        Iterator<ParallelType> iter
                = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<StringBuilder> c = generateData(DATA_SIZE / 10);
            int limit = rand.nextInt(DATA_SIZE / 10 * 2);
            Stream<StringBuilder> stream1 = (type == ParallelType.Parallel) ? c.parallelStream()
                    : (type == ParallelType.Sequential) ? c.stream().sequential()
                    : c.stream();
            Collection<StringBuilder> result1 = stream1.flatMap(LambdaUtilities.genSBFlatMapper(2, DATA_SIZE / 10)).collect(Collectors.toCollection(LinkedList<StringBuilder>::new));

            Stream<StringBuilder> stream2 = (type == ParallelType.Parallel) ? c.parallelStream()
                    : (type == ParallelType.Sequential) ? c.stream().sequential()
                    : c.stream();
            Collection<StringBuilder> result2 = stream2.flatMap(LambdaUtilities.genSBFlatMapper(2, DATA_SIZE / 10)).limit(limit).collect(Collectors.toCollection(LinkedList<StringBuilder>::new));

            if (limit > result1.size()) {
                assertTrue(result1.size() == result2.size());
            } else {
                assertTrue(result2.size() == limit);
            }
        }
    }

    @Test
    public void testExplode() throws Exception {
        Iterator<ParallelType> iter
                = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<StringBuilder> c = generateData(DATA_SIZE / 10);
            int selected = rand.nextInt(4);
            Stream<StringBuilder> stream = (type == ParallelType.Parallel) ? c.parallelStream()
                    : (type == ParallelType.Sequential) ? c.stream().sequential()
                    : c.stream();
            List<StringBuilder> result = stream.flatMap(LambdaUtilities.genSBFlatMapper(selected, DATA_SIZE / 10)).collect(Collectors.<StringBuilder>toList());
            verifyFlatBiBlock(c, result, selected, DATA_SIZE / 10);
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testMax() throws Exception {
        for (Comparator comp : cmps) {
            Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
            while (iter.hasNext()) {
                ParallelType type = iter.next();
                Collection<StringBuilder> col1 = generateData(DATA_SIZE);
                Stream<StringBuilder> stream1 = (type == ParallelType.Parallel) ? col1.parallelStream()
                        : (type == ParallelType.Sequential) ? col1.parallelStream().sequential()
                        : col1.stream();
                java.util.Optional<StringBuilder> optional = stream1.max(comp);
                assertTrue(optional.isPresent());
                assertEquals(optional.get(), getMax1(col1, comp));
                assertEquals(optional.get(), getMax2(col1, comp));
                assertEquals(optional.get(), getMax3(col1, comp));

                @SuppressWarnings("cast")
                        Collection<StringBuilder> emptyCol = hasIni
                        ? (Collection<StringBuilder>) LambdaUtilities.create(typeObject, initSize)
                        : (Collection<StringBuilder>) LambdaUtilities.create(typeObject);
                Stream<StringBuilder> emptyStream = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                        : (type == ParallelType.Sequential) ? emptyCol.stream().sequential()
                        : emptyCol.stream();
                assertFalse(emptyStream.max(comp).isPresent());
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testMin() throws Exception {
        for (Comparator comp : cmps) {
            Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
            while (iter.hasNext()) {
                ParallelType type = iter.next();
                Collection<StringBuilder> col1 = generateData(DATA_SIZE);
                Stream<StringBuilder> stream1 = (type == ParallelType.Parallel) ? col1.parallelStream()
                        : (type == ParallelType.Sequential) ? col1.parallelStream().sequential()
                        : col1.stream();
                java.util.Optional<StringBuilder> optional = stream1.min(comp);
                assertTrue(optional.isPresent());
                assertEquals(optional.get(), getMin1(col1, comp));
                assertEquals(optional.get(), getMin2(col1, comp));
                assertEquals(optional.get(), getMin3(col1, comp));

                @SuppressWarnings("cast")
                        Collection<StringBuilder> emptyCol = hasIni
                        ? (Collection<StringBuilder>) LambdaUtilities.create(typeObject, initSize)
                        : (Collection<StringBuilder>) LambdaUtilities.create(typeObject);
                Stream<StringBuilder> emptyStream = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                        : (type == ParallelType.Sequential) ? emptyCol.stream().sequential()
                        : emptyCol.stream();
                assertFalse(emptyStream.min(comp).isPresent());
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNoneMatch() throws Exception {
        Iterator<ParallelType> iter
                = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<StringBuilder> c1 = generateData(DATA_SIZE);
            Stream<StringBuilder> stream1 = (type == ParallelType.Parallel) ? c1.parallelStream()
                    : (type == ParallelType.Sequential) ? c1.parallelStream().sequential()
                    : c1.stream();
            EnumSet<LambdaUtilities.CharType> set = EnumSet.allOf(LambdaUtilities.CharType.class);
            LambdaUtilities.CharType charType
                    = (LambdaUtilities.CharType) set.toArray()[rand.nextInt(4)];
            boolean isFirst = rand.nextBoolean();
            assertTrue(stream1.noneMatch((Predicate<StringBuilder>) LambdaUtilities.randomSBPredicate(charType, isFirst))
                    == verifyMatch(c1, charType, !isFirst, true));

            //Empty stream's noneMatch will return true always
            @SuppressWarnings("cast")
                    Collection<StringBuilder> emptyCol = hasIni
                    ? (Collection<StringBuilder>) LambdaUtilities.create(typeObject, initSize)
                    : (Collection<StringBuilder>) LambdaUtilities.create(typeObject);
            Stream<StringBuilder> stream2 = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.parallelStream().sequential()
                    : emptyCol.stream();
            assertTrue(stream2.noneMatch((Predicate<StringBuilder>) LambdaUtilities.randomSBPredicate(charType, !isFirst)));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testReduceWithoutBase() throws Exception {
        Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            // Can't predict Parallel Add/Substract reduce result, give up
            // testing on parallel substract test
            if (type == ParallelType.Parallel) {
                continue;
            }
            Collection<StringBuilder> col1 = generateData(DATA_SIZE / 10);
            Stream<StringBuilder> streamPlus = (type == ParallelType.Parallel) ? col1.parallelStream()
                    : (type == ParallelType.Sequential) ? col1.stream().sequential() : col1.stream();
            java.util.Optional<StringBuilder> oiPlus = streamPlus.reduce(LambdaUtilities.appendSBBinaryOperator());
            assertTrue(oiPlus.isPresent());
            StringBuilder total = new StringBuilder();
            Iterator<StringBuilder> it1 = col1.iterator();
            while (it1.hasNext()) {
                total.append(it1.next());
            }
            assertEquals(oiPlus.get().toString(), total.toString());
            ArrayList<StringBuilder> list1 = new ArrayList<>(col1);
            list1.add(0, total);
            Stream<StringBuilder> streamSubtract = (type == ParallelType.Parallel) ? list1.parallelStream()
                    : (type == ParallelType.Sequential) ? list1.stream().sequential() : list1.stream();
            java.util.Optional<StringBuilder> oiSubtract = streamSubtract.reduce(LambdaUtilities
                    .deleteSBBinaryOperator());
            assertTrue(oiSubtract.isPresent());

            Iterator<StringBuilder> it2 = list1.iterator();
            StringBuilder subTotal = it2.next();
            while (it2.hasNext()) {
                String s = it2.next().toString();
                int i1 = subTotal.indexOf(s);
                int i2 = i1 + s.length();
                subTotal.delete(i1, i2);
            }
            assertEquals(oiSubtract.get().toString(), subTotal.toString());

            @SuppressWarnings("cast")
                    Collection<StringBuilder> emptyCol = hasIni ? (Collection<StringBuilder>) LambdaUtilities.create(
                            typeObject, initSize) : (Collection<StringBuilder>) LambdaUtilities.create(typeObject);
            Stream<StringBuilder> stream2 = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.stream().sequential() : emptyCol.stream();
            java.util.Optional<StringBuilder> emptyOp = stream2.reduce(rand.nextBoolean() ? LambdaUtilities
                    .appendSBBinaryOperator() : LambdaUtilities.deleteSBBinaryOperator());
            assertFalse(emptyOp.isPresent());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testReduceWithBase() throws Exception {
        Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            // Can't predict Parallel Add/Substract reduce result, give up
            // testing on parallel substract test
            if (type == ParallelType.Parallel) {
                continue;
            }
            Collection<StringBuilder> col1 = generateData(DATA_SIZE / 10);

            StringBuilder base = (StringBuilder) col1.toArray()[rand.nextInt(col1.size())];
            StringBuilder total = new StringBuilder(base.toString());

            Stream<StringBuilder> streamPlus = (type == ParallelType.Parallel) ? col1.parallelStream()
                    : (type == ParallelType.Sequential) ? col1.stream().sequential() : col1.stream();
            StringBuilder oiPlus = streamPlus.reduce(base, LambdaUtilities.appendSBBinaryOperator());

            Iterator<StringBuilder> it1 = col1.iterator();
            while (it1.hasNext()) {
                total.append(it1.next());
            }
            assertEquals(oiPlus.toString(), total.toString());

            ArrayList<StringBuilder> list1 = new ArrayList<>(col1);
            list1.add(0, total);

            Stream<StringBuilder> streamSubtract = (type == ParallelType.Parallel) ? list1.parallelStream()
                    : (type == ParallelType.Sequential) ? list1.stream().sequential() : list1.stream();
            StringBuilder oiSub = streamSubtract.reduce(base, LambdaUtilities.deleteSBBinaryOperator());

            StringBuilder subTotal = total.delete(0, base.length());
            Iterator<StringBuilder> it2 = col1.iterator();
            while (it2.hasNext()) {
                String s = it2.next().toString();
                int i1 = subTotal.indexOf(s);
                int i2 = i1 + s.length();
                subTotal.delete(i1, i2);
            }

            assertEquals(subTotal.toString(), oiSub.toString());

            @SuppressWarnings("cast")
                    Collection<StringBuilder> emptyCol = hasIni ? (Collection<StringBuilder>) LambdaUtilities.create(
                            typeObject, initSize) : (Collection<StringBuilder>) LambdaUtilities.create(typeObject);
            Stream<StringBuilder> stream2 = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.stream().sequential() : emptyCol.stream();
            StringBuilder emptyOp = stream2.reduce(base, rand.nextBoolean() ? LambdaUtilities.appendSBBinaryOperator()
                    : LambdaUtilities.deleteSBBinaryOperator());
            assertEquals(emptyOp, base);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSubstream() throws Exception {
        Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
        BiFunction<Integer, Integer, Integer> bf = LambdaUtilities.randBetweenIntegerFunction();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<StringBuilder> col = generateData(DATA_SIZE);
            int skip = rand.nextInt(col.size());
            // limit has more than 50% chance less than col.size() - skip
            int limit = rand.nextBoolean() ? bf.apply(0, col.size() - skip) : rand.nextInt(Integer.MAX_VALUE);
            Stream<StringBuilder> stream1 = (type == ParallelType.Parallel) ? col.parallelStream()
                    : (type == ParallelType.Sequential) ? col.stream().sequential() : col.stream();
            Iterator<StringBuilder> it = stream1.skip(skip).limit(limit).iterator();
            verifySlice(col.iterator(), it, skip, limit);

            // limit=0 causes empty stream
            Stream<StringBuilder> stream2 = (type == ParallelType.Parallel) ? col.parallelStream()
                    : (type == ParallelType.Sequential) ? col.stream().sequential() : col.stream();
            assertFalse(stream2.skip(skip).limit(0).iterator().hasNext());

            // skip exceed collection size cause empty stream
            Stream<StringBuilder> stream3 = (type == ParallelType.Parallel) ? col.parallelStream()
                    : (type == ParallelType.Sequential) ? col.stream().sequential() : col.stream();
            int skipExceeded = bf.apply(col.size(), Integer.MAX_VALUE);
            assertFalse(stream3.skip(skipExceeded).limit(1).iterator().hasNext());

            @SuppressWarnings("cast")
                    Collection<StringBuilder> emptyCol = hasIni ? (Collection<StringBuilder>) LambdaUtilities.create(
                            typeObject, initSize) : (Collection<StringBuilder>) LambdaUtilities.create(typeObject);
            Stream<StringBuilder> emptyStream = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.stream().sequential() : emptyCol.stream();
            assertFalse(emptyStream.skip(skip).limit(limit).iterator().hasNext());
        }
    }

    @Test
    public void testSorted() throws Exception {
        Iterator<ParallelType> iter
                = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<StringBuilder> col = generateData(DATA_SIZE);
            Stream<StringBuilder> stream = (type == ParallelType.Parallel) ? col.parallelStream()
                    : (type == ParallelType.Sequential) ? col.parallelStream().sequential()
                    : col.stream();
            List<StringBuilder> reversed = stream.sorted(NATRUAL_ORDER_CMP.reversed()).collect(Collectors.<StringBuilder>toList());
            //The reason conver l to sorted is CopyOnWriteArrayList doesn't support
            //sort, we use ArrayList sort data instead
            List<StringBuilder> sorted = new ArrayList<>(col);
            Collections.sort(sorted, NATRUAL_ORDER_CMP);

            //SortedSet instance's stream can't be reordered
            if (!(col instanceof SortedSet)) {
                Collections.sort(sorted, NATRUAL_ORDER_CMP.reversed());
            }
            assertEquals(sorted, reversed);

            @SuppressWarnings("cast")
                    Collection<StringBuilder> emptyCol = hasIni
                    ? (Collection<StringBuilder>) LambdaUtilities.create(typeObject, initSize)
                    : (Collection<StringBuilder>) LambdaUtilities.create(typeObject);
            Stream<StringBuilder> stream2 = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.parallelStream().sequential()
                    : emptyCol.stream();
            assertFalse(stream2.sorted(NATRUAL_ORDER_CMP.reversed()).iterator().hasNext());
        }
    }

    @Test
    public void testPeek() throws Exception {
        Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            if (type == ParallelType.Parallel) {
                continue;
            }
            Collection<StringBuilder> c1 = generateData(DATA_SIZE / 10);
            Stream<StringBuilder> stream1 = (type == ParallelType.Parallel) ? c1.parallelStream()
                    : (type == ParallelType.Sequential) ? c1.stream().sequential() : c1.stream();
            Stream<StringBuilder> stream2 = (type == ParallelType.Parallel) ? c1.parallelStream()
                    : (type == ParallelType.Sequential) ? c1.stream().sequential() : c1.stream();
            StringBuilder sb1 = new StringBuilder();
            Iterator<StringBuilder> it = stream1.peek(LambdaUtilities.appendSBConsumer(sb1)).iterator();
            StringBuilder expectedTotal = new StringBuilder();
            while (it.hasNext()) {
                expectedTotal.append(it.next());
            }
            assertEquals(sb1.toString(), expectedTotal.toString());
            AtomicReference<StringBuilder> sb2 = stream2.collect(
                    LambdaUtilities.atomicSBSupplier(new StringBuilder()), LambdaUtilities.appendSBBiConsumer(),
                    LambdaUtilities.appendAtomicSBBiConsumer());
            assertEquals(sb2.get().toString(), expectedTotal.toString());

            @SuppressWarnings("cast")
                    Collection<StringBuilder> emptyCol = hasIni ? (Collection<StringBuilder>) LambdaUtilities.create(
                            typeObject, initSize) : (Collection<StringBuilder>) LambdaUtilities.create(typeObject);
            Stream<StringBuilder> emptyStream = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.stream().sequential() : emptyCol.stream();
            assertFalse(emptyStream.peek(LambdaUtilities.appendSBConsumer(sb1)).iterator().hasNext());
            assertEquals(sb1.toString(), expectedTotal.toString());
        }
    }

    @Test
    public void testToArray() throws Exception {
        Iterator<ParallelType> iter
                = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<StringBuilder> c1 = generateData(DATA_SIZE);
            Stream<StringBuilder> stream1 = (type == ParallelType.Parallel) ? c1.parallelStream()
                    : (type == ParallelType.Sequential) ? c1.parallelStream().sequential()
                    : c1.stream();
            Object[] arr1 = stream1.toArray();
            Object[] arr2 = c1.toArray();
            assertEquals(arr1, arr2);

            @SuppressWarnings("cast")
                    Collection<StringBuilder> emptyCol = hasIni
                    ? (Collection<StringBuilder>) LambdaUtilities.create(typeObject, initSize)
                    : (Collection<StringBuilder>) LambdaUtilities.create(typeObject);
            Stream<StringBuilder> stream2 = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.parallelStream().sequential()
                    : emptyCol.stream();
            assertEquals(stream2.toArray().length, 0);
        }
    }

    @Test
    public void testUniqueElements() throws Exception {
        Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<StringBuilder> c1 = generateData(DATA_SIZE / 10);
            Set<StringBuilder> set1 = new HashSet<>(c1);
            Stream<StringBuilder> stream1 = (type == ParallelType.Parallel) ? c1.parallelStream()
                    : (type == ParallelType.Sequential) ? c1.stream().sequential() : c1.stream();
            List<StringBuilder> list2 = stream1.flatMap(LambdaUtilities.genSBFlatMapper(3, DATA_SIZE / 10)).distinct().collect(Collectors.<StringBuilder>toList());
            assertEquals(set1.size(), list2.size());
            assertTrue(set1.containsAll(list2));
        }
    }

    @Test
    public void testGroupBy() throws Throwable {
        Iterator<ParallelType> iter
                = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<StringBuilder> c = generateData(DATA_SIZE);

            boolean isFirst = rand.nextBoolean();
            Stream<StringBuilder> stream = (type == ParallelType.Parallel) ? c.parallelStream()
                    : (type == ParallelType.Sequential) ? c.stream().sequential()
                    : c.stream();
            Map<LambdaUtilities.CharType, List<StringBuilder>> result
                    = stream.collect(Collectors.groupingBy(LambdaUtilities.sbGenericFunction(isFirst)));
            verifyGroupBy(result, isFirst);

            @SuppressWarnings("cast")
                    Collection<StringBuilder> emptyList = hasIni
                    ? (Collection<StringBuilder>) LambdaUtilities.create(typeObject, initSize)
                    : (Collection<StringBuilder>) LambdaUtilities.create(typeObject);
            Stream<StringBuilder> stream2 = (type == ParallelType.Parallel) ? emptyList.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyList.stream().sequential()
                    : emptyList.stream();
            assertTrue(stream2.collect(Collectors.groupingBy(LambdaUtilities.sbGenericFunction(isFirst))).isEmpty());
        }
    }

    @Override
    protected Collection<StringBuilder> generateData(int n) throws Exception {
        Collection<StringBuilder> col = LambdaUtilities.create(typeObject);
        for (int row = 0; row < n; row++) {
            col.add(new StringBuilder(StringUtilities.randomString(MAX_LEN, MIN_LEN)));
        }
        return col;
    }

    private void verifyGroupBy(Map<LambdaUtilities.CharType, List<StringBuilder>> result, boolean isFirst) {
        assertTrue(result.size() <= 4);
        Iterator<LambdaUtilities.CharType> keyiter = result.keySet().iterator();
        while (keyiter.hasNext()) {
            LambdaUtilities.CharType key = keyiter.next();
            Iterator<StringBuilder> valueiter = result.get(key).iterator();
            while (valueiter.hasNext()) {
                StringBuilder i = valueiter.next();
                Character c;
                if (isFirst) {
                    c = i.charAt(0);
                } else {
                    c = i.charAt(i.length() - 1);
                }
                LambdaUtilities.CharType t = Character.isAlphabetic(c)
                        ? (Character.isUpperCase(c) ? LambdaUtilities.CharType.UPPERCASE : LambdaUtilities.CharType.LOWERCASE)
                        : (Character.isDigit(c) ? LambdaUtilities.CharType.DIGIT : LambdaUtilities.CharType.SPECIAL);
                assertTrue(t == key);
            }
        }
    }
    
    private void verifyFlatBiBlock(Collection<StringBuilder> orig, List<StringBuilder> result, int selected, int unit) throws Exception {
        
        switch (selected) {
            case 0:
                assertEquals(result.size(), 0);
                break;
            case 1:
                List<StringBuilder> l1 = new ArrayList<>(orig);
                Collections.sort(l1, NATRUAL_ORDER_CMP);
                Collections.sort(result, NATRUAL_ORDER_CMP);
                assertEquals(l1, result);
                break;
            case 2:
                List<StringBuilder> l2 = new ArrayList<>();
                Iterator<StringBuilder> it2 = orig.iterator();
                while (it2.hasNext()) {
                    StringBuilder current = it2.next();
                    int step = current.length() / unit + unit - 1;
                    for (int i = 0; i < current.length(); i += step) {
                        l2.add(new StringBuilder(current.substring(i, i + step >= current.length()
                                ? current.length() - 1 : i + step)));
                    }
                }
                Collections.sort(l2, NATRUAL_ORDER_CMP);
                Collections.sort(result, NATRUAL_ORDER_CMP);
                for (int j = 0; j < l2.size(); j++) {
                    StringBuilder s1 = l2.get(j);
                    StringBuilder s2 = result.get(j);
                    assertEquals(s1.toString(), s2.toString());
                }
                break;
            case 3:
                List<StringBuilder> l3 = new ArrayList<>();
                Iterator<StringBuilder> it3 = orig.iterator();
                while (it3.hasNext()) {
                    StringBuilder current = it3.next();
                    int step = current.length() / unit + unit - 1;
                    for (int i = 0; i < current.length(); i += step) {
                        l3.add(current);
                    }
                }
                Collections.sort(l3, NATRUAL_ORDER_CMP);
                Collections.sort(result, NATRUAL_ORDER_CMP);
                assertEquals(l3, result);
                break;
            default:
                break;
        }
    }
}
