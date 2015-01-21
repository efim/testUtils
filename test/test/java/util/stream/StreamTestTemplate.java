
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

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import static org.testng.Assert.assertEquals;
import org.testng.ITest;
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

    protected enum ParallelType {

        Parallel, Sequential, Default
    }
}
