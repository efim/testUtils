
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
package test.java.util.stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.ITest;
import org.testng.annotations.Test;
import test.java.util.LambdaUtilities;

/**
 *
 * @author efim
 */
public class StreamTestTemplate<T> implements ITest {

    protected final static int DATA_SIZE = 1 << 10;

    protected final static Random rand = new Random(System.currentTimeMillis());

    private Class typeObject;

    protected boolean hasIni;

    protected int initSize;

    public StreamTestTemplate(Class<? extends Collection<T>> clazz, int... initSizes) {
        this.typeObject = clazz;
        assert (initSizes.length <= 1);
        if (initSizes.length == 1) {
            hasIni = true;
            this.initSize = initSizes[0];
        }
    }

    @Override
    public String getTestName() {
        return "template";
    }

    protected void simpleTestIteration(Function<Collection<T>, Consumer<ParallelType>> body) throws Exception {
        Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<T> c = generateData(DATA_SIZE);

            body.apply(c).accept(type);
        }
    }

    protected void emptyStreamTestIteration(Consumer<Stream<T>> assertions) throws Exception {

        simpleTestIteration(c -> type -> {
            try {
                @SuppressWarnings("cast")
                Collection<T> emptyCol = getEmptyCollection();
                Stream<T> emptyStream = getStreamFromCollection(emptyCol, type);
                assertions.accept(emptyStream);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
    
        protected void singleStreamTestIteration(Function<Stream<T>, Consumer<Collection<T>>> assertions) throws Exception {
        simpleTestIteration(collection -> type -> {
            try {
                Stream<T> stream = getStreamFromCollection(collection, type);
                
                assertions.apply(stream).accept(collection);
                
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            
        });
    }    

    protected void singleStreamVerifyPredicateTest(Function<Stream<T>, Function<Predicate<T>, Consumer<Boolean>>> otherDeclarationAndAssert, boolean verifyMatchForAll) throws Exception {

        //since predicates are constructed differently
        //and are intrevowen with veifyMatch
        //this should be implemented separately for different children
        throw new UnsupportedOperationException();
    }
    
    protected Predicate<T> getRandomPredicate() throws Exception {
        throw new UnsupportedOperationException();
    }


    protected Stream<T> getStreamFromCollection(Collection<T> l, ParallelType typeVar) {
        return (typeVar == ParallelType.Parallel) ? l.parallelStream()
                : (typeVar == ParallelType.Sequential) ? l.stream().sequential() : l.stream();
    }

    protected Collection<T> getEmptyCollection() throws Exception {
        return hasIni ? LambdaUtilities.create(typeObject, initSize)
                : LambdaUtilities.create(typeObject);
    }

    protected Collection<T> generateData(int size) throws Exception {

        Collection<T> col = hasIni ? LambdaUtilities.create(typeObject, initSize)
                : LambdaUtilities.create(typeObject);
        for (int i = 0; i < size; i++) {
            col.add(generateData());
        }
        return col;
    }

    protected T generateData() throws Exception {
        throw new UnsupportedOperationException("use real test cases");
    }

    private T getValueByIterator(Collection<T> col, BiPredicate<T, T> choseValue) {
        assert (!col.isEmpty());
        Iterator<T> it = col.iterator();
        T val = it.next();
        while (it.hasNext()) {
            T next = it.next();
            if (choseValue.test(val, next)) {
                val = next;
            }
        }
        return val;
    }

    protected T getMax1(Collection<T> col, Comparator<T> c) {
        return getValueByIterator(col, (max, next) -> c.compare(max, next) < 0);
    }

    /*
     * c could be reversed order, we can't use MAX_VALUE or MIN_VALUE but we can
     * use 1st element to compare with.
     */
    protected T getMin1(Collection<T> col, Comparator<T> c) {
        return getValueByIterator(col, (min, next) -> c.compare(min, next) > 0);
    }

    private T getValueByReduce(Collection<T> col, BinaryOperator<T> valueChoser) {
        assert (!col.isEmpty());
        java.util.Optional<T> val = col.stream().reduce(valueChoser);
        assert (val.isPresent());
        return val.get();
    }

    protected T getMax2(Collection<T> col, Comparator<T> c) {
        return getValueByReduce(col, LambdaUtilities.maxGenericBinaryOperator(c));
    }

    protected T getMin2(Collection<T> col, Comparator<T> c) {
        return getValueByReduce(col, LambdaUtilities.minGenericBinaryOperator(c));
    }

    private T getValueByGenericFunction(Collection<T> col, Comparator<T> c, BiFunction<T, T, T> choseValueFunction, BinaryOperator<T> choseValueBinaryOperator) {
        assert (!col.isEmpty());
        T any = col.iterator().next();
        T val = col.stream().reduce(any, choseValueFunction,
                choseValueBinaryOperator);
        return val;
    }

    protected T getMax3(Collection<T> col, Comparator<T> c) {
        return getValueByGenericFunction(col, c, LambdaUtilities.maxGenericFunction(c), LambdaUtilities.maxGenericBinaryOperator(c));
    }

    protected T getMin3(Collection<T> col, Comparator<T> c) {
        return getValueByGenericFunction(col, c, LambdaUtilities.minGenericFunction(c), LambdaUtilities.minGenericBinaryOperator(c));
    }

    protected void verifySlice(Iterator<T> itOrg, Iterator<T> itSliced, int skip, int limit) {
        int pos = 0;
        while (itOrg.hasNext() && pos++ < skip) {
            itOrg.next();
        }

        while (itOrg.hasNext() && pos++ < limit) {
            assertEquals(itOrg.next(), itSliced.next());
        }
    }
    
    @SuppressWarnings("unchecked")
    public void testAllMatch() throws Exception {
        singleStreamVerifyPredicateTest(
                stream -> p ->  verifyMatch -> {
                    assertEquals(stream.allMatch(p), (boolean) verifyMatch);
                }, true);

        emptyStreamTestIteration(stream -> {
            try {
                // Empty stream's allMatch will return true always
                Predicate<T> p = getRandomPredicate();
                assertTrue(stream.allMatch(p));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    public void testAnyMatch() throws Exception {

        singleStreamVerifyPredicateTest(
                stream -> p -> verifyMatch -> {
                    assertEquals(stream.anyMatch(p), (boolean) verifyMatch);
                }, false
        );

        emptyStreamTestIteration(
                stream -> {
                    try {
                        Predicate<T> p = getRandomPredicate();
                        // Empty stream's anyMatch, noneMatch will return false always
                        assertTrue(!stream.anyMatch(p));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }
    
    @SuppressWarnings("unchecked")
    public void testConcat(Predicate<T> p, Comparator<T> comparator, 
            Function<Integer, BiConsumer<List<T>, List<T>>> assertionOfEquality) throws Exception {
        simpleTestIteration(
                c -> type -> {
                    try {
                        Collection<T> l1 = generateData(DATA_SIZE);
                        Collection<T> l2 = generateData(DATA_SIZE);

                        Stream<T> stream11 = getStreamFromCollection(l1, type);
                        Stream<T> stream21 = getStreamFromCollection(l2, type);

                        Collection<T> result1 = stream11.filter(p).collect(Collectors.toCollection(LinkedList<T>::new));
                        Collection<T> result2 = stream21.filter(p).collect(Collectors.toCollection(LinkedList<T>::new));
                        result1.addAll(result2);

                        Stream<T> stream12 = getStreamFromCollection(l1, type);
                        Stream<T> stream22 = getStreamFromCollection(l2, type);
                        List<T> expectedList = Stream.concat(stream12, stream22).filter(p).collect(Collectors.<T>toList());
                        List<T> testList = result1.stream().collect(Collectors.<T>toList());
                        //Can't sort on unmodifiable list
                        if (expectedList.size() > 1) {
                            Collections.sort(testList, comparator);
                            Collections.sort(expectedList, comparator);
                        }
                        for (int i = 0; i < testList.size(); i++) {
                            assertionOfEquality.apply(i).accept(testList, expectedList);
                        }

                        //Concat with empty stream should not change other input
                        Collection<T> emptyList = getEmptyCollection();
                        Stream<T> stream3 = getStreamFromCollection(emptyList, type);

                        List<T> result3 = Stream.concat(l1.stream(), stream3).collect(Collectors.<T>toList());
                        List<T> list1 = new ArrayList<>(l1);
                        //Can't sort on unmodifiable list
                        if (result3.size() > 1) {
                            Collections.sort(list1, comparator);
                            Collections.sort(result3, comparator);
                        }
                        assertEquals(list1, result3);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }
    
    @SuppressWarnings("unchecked")
    public void testFilter(Predicate<T> p1, Comparator<T> comparator
            , Consumer<Collection<T>> firstAssertion) throws Exception {

        singleStreamTestIteration(stream -> c -> {
            // Filter the data, check if it works as expected.
            Collection<T> result1 = stream.filter(p1).collect(Collectors.toCollection(LinkedList<T>::new));
            firstAssertion.accept(result1);
        });

        singleStreamTestIteration(stream -> c -> {
            // filter on parallel stream can cause IllegalStateException
            Collection<T> result1 = stream.filter(p1).filter(e -> false).collect(Collectors.toCollection(LinkedList<T>::new));
            assertTrue(result1.isEmpty());
        });

        simpleTestIteration(c -> type -> {
            try {
                Predicate<T> predicate = getRandomPredicate();

                Supplier<Stream<T>> refreshStream = () -> getStreamFromCollection(c, type);

                Stream<T> stream;

                // Testing of filtering on conjunction of predicates
                stream = refreshStream.get();
                List<T> result2 = stream.filter(p1).filter(predicate).collect(Collectors.toCollection(ArrayList<T>::new));
                stream = refreshStream.get();
                List<T> result3 = stream.filter(p1.and(predicate)).collect(Collectors.toCollection(ArrayList<T>::new));
                Collections.sort(result2, comparator);
                Collections.sort(result3, comparator);
                assertEquals(result2, result3);

                //result2 could be a EmptyList, we're putting result2, result3 into
                // concatList because EmptyList doesn't support addAll
                List<T> concatList = new ArrayList<>();
                stream = refreshStream.get();
                result2 = stream.filter(p1.and(predicate)).collect(Collectors.toCollection(ArrayList<T>::new));
                stream = refreshStream.get();
                result3 = stream.filter(p1.or(predicate)).collect(Collectors.toCollection(ArrayList<T>::new));

                concatList.addAll(result2);
                concatList.addAll(result3);
                result3.clear();
                Stream<T> stream1 = refreshStream.get();
                Stream<T> stream2 = refreshStream.get();
                result3 = Stream.concat(stream1.filter(p1), stream2.filter(predicate)).collect(Collectors.toCollection(ArrayList<T>::new));
                Collections.sort(concatList, comparator);
                Collections.sort(result3, comparator);
                assertEquals(concatList, result3);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        emptyStreamTestIteration(stream -> {
            //filtering result of empty stream should be empty
            assertFalse(stream.filter(p1).iterator().hasNext());
        });
    }

    @SuppressWarnings("unchecked")
    public void testFind() throws Exception {
        simpleTestIteration(c -> type -> {
            Stream<T> stream1 = getStreamFromCollection(c, type);
            Stream<T> stream2 = getStreamFromCollection(c, type);
            java.util.Optional<T> opAny = stream1.findAny();
            java.util.Optional<T> opFirst = stream2.findFirst();
            assertTrue(opAny.isPresent());
            assertTrue(opFirst.isPresent());
            if (!stream1.isParallel()) {
                assertEquals(opAny, opFirst);

            }
        });

        emptyStreamTestIteration(stream -> {
            java.util.Optional<T> emptyAny = stream.findAny();
            assertFalse(emptyAny.isPresent());
        });

        emptyStreamTestIteration(stream -> {
            java.util.Optional<T> emptyFirst = stream.findFirst();
            assertFalse(emptyFirst.isPresent());
        });
    }
    
    @SuppressWarnings("unchecked")
    public void testForEach() throws Exception {
        singleStreamTestIteration(stream -> collection -> {
            stream.forEach(t -> {
                assertTrue(collection.contains(t));
            });
        });
    }
    
    protected enum ParallelType {

        Parallel, Sequential, Default
    }
}
