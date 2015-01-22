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
import java.util.Optional;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import test.java.util.LambdaUtilities;
import test.java.util.StringUtilities;
import static test.java.util.stream.StreamTestTemplate.rand;

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

    protected static boolean verifyMatch(Collection<StringBuilder> c,
            LambdaUtilities.CharType charType, boolean isFirst, boolean all) {
        Iterator<StringBuilder> it = c.iterator();
        while (it.hasNext()) {
            StringBuilder current = it.next();
            char checkChar = current.charAt(isFirst ? 0 : current.toString().length() - 1);
            switch (charType) {
                case DIGIT:
                    if (!all && Character.isDigit(checkChar)) {
                        return true;
                    } else if (all && !Character.isDigit(checkChar)) {
                        return false;
                    }
                    break;
                case LOWERCASE:
                    if (!all && Character.isLowerCase(checkChar)) {
                        return true;
                    } else if (all && !Character.isLowerCase(checkChar)) {
                        return false;
                    }
                    break;
                case UPPERCASE:
                    if (!all && Character.isUpperCase(checkChar)) {
                        return true;
                    } else if (all && !Character.isUpperCase(checkChar)) {
                        return false;
                    }
                    break;
                default:
                    if (!all && !Character.isLetterOrDigit(checkChar)) {
                        return true;
                    } else if (all && Character.isLetterOrDigit(checkChar)) {
                        return false;
                    }
                    break;
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

    @Override
    protected Predicate<StringBuilder> getRandomPredicate() throws Exception {
        EnumSet<LambdaUtilities.CharType> set = EnumSet.allOf(LambdaUtilities.CharType.class);
        LambdaUtilities.CharType charType
                = (LambdaUtilities.CharType) set.toArray()[rand.nextInt(4)];
        boolean isFirst = rand.nextBoolean();

        return (Predicate<StringBuilder>) LambdaUtilities.randomSBPredicate(charType, isFirst);
    }

    @Override
    protected void singleStreamVerifyPredicateTest(Function<Stream<StringBuilder>, Function<Predicate<StringBuilder>, Consumer<Boolean>>> otherDeclarationAndAssert, boolean verifyMatchForAll) throws Exception {

        simpleTestIteration(collection -> type -> {
            try {
                Stream<StringBuilder> stream = getStreamFromCollection(collection, type);

                EnumSet<LambdaUtilities.CharType> set = EnumSet.allOf(LambdaUtilities.CharType.class);
                LambdaUtilities.CharType charType
                        = (LambdaUtilities.CharType) set.toArray()[rand.nextInt(4)];
                boolean isFirst = rand.nextBoolean();

                Predicate<StringBuilder> p = (Predicate<StringBuilder>) LambdaUtilities.randomSBPredicate(charType, isFirst);

                boolean verifyMatch = verifyMatch(collection, charType, isFirst, verifyMatchForAll);

                otherDeclarationAndAssert.apply(stream).apply(p).accept(verifyMatch);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAllMatch() throws Exception {
        super.testAllMatch();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAnyMatch() throws Exception {
        super.testAnyMatch();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConcat() throws Exception {
        Predicate<StringBuilder> p = getRandomPredicate();

        super.testConcat(p, NATRUAL_ORDER_CMP, i -> (testList, expectedList) -> assertEquals(testList, expectedList));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFilter() throws Exception {
        EnumSet<LambdaUtilities.CharType> set = EnumSet.allOf(LambdaUtilities.CharType.class);

        LambdaUtilities.CharType charType
                = (LambdaUtilities.CharType) set.toArray()[rand.nextInt(4)];
        boolean isFirst = rand.nextBoolean();
        Predicate<StringBuilder> p = (Predicate<StringBuilder>) LambdaUtilities.randomSBPredicate(charType, isFirst);

        super.testFilter(p, NATRUAL_ORDER_CMP, result1 -> assertTrue(verifyMatch(result1, charType, isFirst, true)));
    }

    @Test
    @Override
    public void testFind() throws Exception {
        super.testFind();
    }

    @Test
    @Override
    public void testForEach() throws Exception {
        super.testForEach();
    }

    @Test
    protected void testLimit() throws Throwable {
        int limit = rand.nextInt(DATA_SIZE * 2);
        super.testLimit(() -> LambdaUtilities.genSBFlatMapper(2, DATA_SIZE), limit);
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
            super.testMax(e -> e, comp);
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testMin() throws Exception {
        for (Comparator comp : cmps) {
            super.testMax(e -> e, comp);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNoneMatch() throws Exception {

        EnumSet<LambdaUtilities.CharType> set = EnumSet.allOf(LambdaUtilities.CharType.class);
        LambdaUtilities.CharType charType
                = (LambdaUtilities.CharType) set.toArray()[rand.nextInt(4)];
        boolean isFirst = rand.nextBoolean();

        super.testNoneMatch(stream -> verifyMatch(stream, charType, !isFirst, true), (Predicate<StringBuilder>) LambdaUtilities.randomSBPredicate(charType, isFirst));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testReduce() throws Exception {

        Boolean[] consts = {true, false};
        for (Boolean withBase : consts) {
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
                StringBuilder total = withBase 
                        ? new StringBuilder(base.toString())
                        : new StringBuilder();

                Stream<StringBuilder> streamPlus = getStreamFromCollection(col1, type);
                Optional<StringBuilder> oiPlus = withBase 
                        ? Optional.of(streamPlus.reduce(base, LambdaUtilities.appendSBBinaryOperator()))
                        : streamPlus.reduce(LambdaUtilities.appendSBBinaryOperator());
                

                Iterator<StringBuilder> it1 = col1.iterator();
                while (it1.hasNext()) {
                    total.append(it1.next());
                }
                assertEquals(oiPlus.get().toString(), total.toString());

                ArrayList<StringBuilder> list1 = new ArrayList<>(col1);
                list1.add(0, total);
                
                Stream<StringBuilder> streamSubtract = getStreamFromCollection(list1, type);
                Optional<StringBuilder> oiSub = withBase
                        ? Optional.of(streamSubtract.reduce(base, LambdaUtilities.deleteSBBinaryOperator()))
                        : streamSubtract.reduce(LambdaUtilities.deleteSBBinaryOperator());
                assertTrue(oiSub.isPresent());


                Iterator<StringBuilder> it2 = withBase ? col1.iterator() : list1.iterator();
                StringBuilder subTotal = withBase 
                        ? total.delete(0, base.length())
                        : it2.next();                
                while (it2.hasNext()) {
                    String s = it2.next().toString();
                    int i1 = subTotal.indexOf(s);
                    int i2 = i1 + s.length();
                    subTotal.delete(i1, i2);
                }

                assertEquals(subTotal.toString(), oiSub.get().toString());

                @SuppressWarnings("cast")
                Collection<StringBuilder> emptyCol = getEmptyCollection();
                Stream<StringBuilder> stream2 = getStreamFromCollection(emptyCol, type);
                Optional<StringBuilder> emptyOp = withBase
                        ? Optional.of(stream2.reduce(base, rand.nextBoolean() 
                            ? LambdaUtilities.appendSBBinaryOperator()
                            : LambdaUtilities.deleteSBBinaryOperator()))
                        : stream2.reduce(rand.nextBoolean() 
                            ? LambdaUtilities.appendSBBinaryOperator()
                            : LambdaUtilities.deleteSBBinaryOperator());
                
                if (withBase) {
                    assertEquals(emptyOp.get(), base);
                } else {
                    assertFalse(emptyOp.isPresent());
                }
                
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    @Override
    public void testSubstream() throws Exception {
        super.testSubstream();
    }

    @Test
    public void testSorted() throws Exception {
        super.testSorted(NATRUAL_ORDER_CMP, e -> e);
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
            Stream<StringBuilder> stream1 = getStreamFromCollection(c1, type);
            Stream<StringBuilder> stream2 = getStreamFromCollection(c1, type);
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
                    Collection<StringBuilder> emptyCol = getEmptyCollection();
            Stream<StringBuilder> emptyStream = getStreamFromCollection(emptyCol, type);
            assertFalse(emptyStream.peek(LambdaUtilities.appendSBConsumer(sb1)).iterator().hasNext());
            assertEquals(sb1.toString(), expectedTotal.toString());
        }
    }


    @Test
    public void testToArray() throws Exception {
        super.testToArray();
    }

    @Test
    public void testUniqueElements() throws Exception {
        super.testUniqueElements(LambdaUtilities.genSBFlatMapper(3, DATA_SIZE));
    }

    @Test
    public void testGroupBy() throws Throwable {
        boolean isFirst = rand.nextBoolean();
        super.testGroupBy(() -> LambdaUtilities.sbGenericFunction(isFirst), result -> collection -> verifyGroupBy(result, isFirst));
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
