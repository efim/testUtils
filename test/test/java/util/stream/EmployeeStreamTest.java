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
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.BiFunction;
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

public class EmployeeStreamTest<T extends Collection<Employee>> implements ITest {

    private final static int DATA_SIZE = 1 << 10;

    private final static Random rand = new Random(System.currentTimeMillis());

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Class[] defaultConstructorClazz = {(Class<ArrayDeque<Employee>>) (Class<?>) (ArrayDeque.class),
        (Class<ArrayList<Employee>>) (Class<?>) (ArrayList.class),
        (Class<ConcurrentLinkedDeque<Employee>>) (Class<?>) (ConcurrentLinkedDeque.class),
        (Class<ConcurrentLinkedQueue<Employee>>) (Class<?>) (ConcurrentLinkedQueue.class),
        (Class<ConcurrentSkipListSet<Employee>>) (Class<?>) (ConcurrentSkipListSet.class),
        (Class<CopyOnWriteArrayList<Employee>>) (Class<?>) (CopyOnWriteArrayList.class),
        (Class<HashSet<Employee>>) (Class<?>) (HashSet.class),
        (Class<LinkedBlockingDeque<Employee>>) (Class<?>) (LinkedBlockingDeque.class),
        (Class<LinkedBlockingQueue<Employee>>) (Class<?>) (LinkedBlockingQueue.class),
        (Class<LinkedHashSet<Employee>>) (Class<?>) (LinkedHashSet.class),
        (Class<LinkedList<Employee>>) (Class<?>) (LinkedList.class),
        (Class<LinkedTransferQueue<Employee>>) (Class<?>) (LinkedTransferQueue.class),
        (Class<PriorityBlockingQueue<Employee>>) (Class<?>) (PriorityBlockingQueue.class),
        (Class<PriorityQueue<Employee>>) (Class<?>) (PriorityQueue.class),
        (Class<Stack<Employee>>) (Class<?>) (Stack.class), (Class<TreeSet<Employee>>) (Class<?>) (TreeSet.class),
        (Class<Vector<Employee>>) (Class<?>) (Vector.class)
    };

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Class[] capacityConstructorClazz = {
        (Class<ArrayBlockingQueue<Employee>>) (Class<?>) (ArrayBlockingQueue.class),
        (Class<ArrayDeque<Employee>>) (Class<?>) (ArrayDeque.class),
        (Class<ArrayList<Employee>>) (Class<?>) (ArrayList.class),
        (Class<HashSet<Employee>>) (Class<?>) (HashSet.class),
        (Class<LinkedBlockingDeque<Employee>>) (Class<?>) (LinkedBlockingDeque.class),
        (Class<LinkedBlockingQueue<Employee>>) (Class<?>) (LinkedBlockingQueue.class),
        (Class<LinkedHashSet<Employee>>) (Class<?>) (LinkedHashSet.class),
        (Class<PriorityBlockingQueue<Employee>>) (Class<?>) (PriorityBlockingQueue.class),
        (Class<PriorityQueue<Employee>>) (Class<?>) (PriorityQueue.class),
        (Class<Vector<Employee>>) (Class<?>) (Vector.class)};

    @Factory
    @SuppressWarnings(value = {"rawtypes", "unchecked"})
    public static Object[] create() {
        List<EmployeeStreamTest> result = new ArrayList<>();
        Stream<Class> stream1 = Arrays.stream(defaultConstructorClazz);
        Stream<Class> stream2 = Arrays.stream(capacityConstructorClazz);
        stream1.forEach(clazz -> result.add(new EmployeeStreamTest(clazz)));
        stream2.forEach(clazz -> result.add(new EmployeeStreamTest(clazz,
                DATA_SIZE)));
        return result.toArray();
    }

    private static Function<Employee, Stream<Employee>> genEmployeeFlatMapper(int selected, Employee.Rule rule) {
        switch (selected) {
            case 0:
                //Generate a empty collection
                return (e) -> {
                    return new ArrayList<Employee>(0).stream();
                };
            case 1:
                return (e) -> {
                    ArrayList<Employee> res = new ArrayList<>();
                    res.add(e);
                    return res.stream();
                };
            case 2:
                switch (rule) {
                    case AGE:
                        return (e) -> {
                            ArrayList<Employee> res = new ArrayList<>();
                            for (int i = 0; i < e.getAge(); i += e.getAge() / 10) {
                                Employee employee = e.clone();
                                employee.setId(e.getId());
                                employee.setAge(e.getAge() * (e.getAge() - 1) / 2 + i);
                                res.add(employee);
                            }
                            return res.stream();
                        };
                    case SALARY:
                        return (e) -> {
                            ArrayList<Employee> res = new ArrayList<>();
                            for (int i = 0; i < (int) e.getSalary(); i += (int) e.getSalary() / 10) {
                                Employee employee = e.clone();
                                employee.setId(e.getId());
                                employee.setSalary(e.getSalary() * (e.getSalary() - 1) / 2 + i);
                                res.add(employee);
                            }
                            return res.stream();
                        };
                    case MALE:
                        return (e) -> {
                            Employee employee = e.clone();
                            employee.setMale(!e.isMale());
                            employee.setId(e.getId());
                            ArrayList<Employee> res = new ArrayList<>();
                            res.add(employee);
                            return res.stream();
                        };
                    case TITLE:
                        return (e) -> {
                            ArrayList<Employee> res = new ArrayList<>();
                            for (int i = 0; i < e.getTitle().ordinal(); i++) {
                                Employee employee = e.clone();
                                employee.setTitle(Employee.Title.values()[i]);
                                employee.setId(e.getId());
                                res.add(employee);
                            }
                            return res.stream();
                        };
                    case ID:
                    default:
                        return (e) -> {
                            ArrayList<Employee> res = new ArrayList<>();
                            for (int i = 0; i < e.getId().length(); i += 2) {
                                Employee employee = e.clone();
                                employee.setId(e.getId());
                                res.add(employee);
                            }
                            return res.stream();
                        };
                }
            case 3:
            default:
                return (e) -> {
                    ArrayList<Employee> res = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        res.add(e);
                    }
                    return res.stream();
                };
        }
    }

    private static Function<Employee, Object> employeeGenericFunction(Employee.Rule rule) {
        switch (rule) {
            case AGE:
                return e -> e.getAge() / 10;
            case SALARY:
                return e -> e.getSalary() <= 6000 ? "LOW" : (e.getSalary() > 15000 ? "HIGH" : "MEDIUM");
            case MALE:
                return e -> e.isMale();
            case TITLE:
                return e -> e.getTitle();
            case ID:
                return e -> e.getId().length();
            default:
                throw new RuntimeException("No such rule");
        }
    }

    private Class<T> typeObject;

    private boolean hasIni;

    private int initSize;

    public EmployeeStreamTest(Class<T> clazz, int... initSizes) {
        this.typeObject = clazz;
        assert (initSizes.length <= 1);
        if (initSizes.length == 1) {
            hasIni = true;
            this.initSize = initSizes[0];
        }
    }

    @Override
    public String getTestName() {
        return typeObject.getName() + "<Employee>";
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAllMatch() throws Exception {
        Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<Employee> c1 = generateData(DATA_SIZE);
            Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
            Employee limit = generateData();
            limit.setSalary(rand.nextFloat() * (Employee.MAX_SALARY * 2));
            limit.setAge(rand.nextInt(Employee.MAX_AGE * 2));
            boolean isUP = rand.nextBoolean();
            Stream<Employee> stream1 = (type == ParallelType.Parallel) ? c1.parallelStream()
                    : (type == ParallelType.Sequential) ? c1.stream().sequential() : c1.stream();
            assertEquals(stream1.allMatch(LambdaUtilities.randomGenericPredicate(isUP, limit, rule.getComparator())),
                    verifyMatch(c1, limit, isUP, true, rule));

            // Empty stream's allMatch will return true always
            @SuppressWarnings("cast")
            Collection<Employee> emptyCol = hasIni
                    ? (Collection<Employee>) LambdaUtilities.create(typeObject, initSize)
                    : (Collection<Employee>) LambdaUtilities.create(typeObject);
            Stream<Employee> stream2 = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.stream().sequential() : emptyCol.stream();
            assertTrue(stream2.allMatch(LambdaUtilities.randomGenericPredicate(isUP, limit, rule.getComparator())));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAnyMatch() throws Exception {
        Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<Employee> c1 = generateData(DATA_SIZE);
            Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
            Employee limit = generateData();
            limit.setSalary(rand.nextFloat() * (Employee.MAX_SALARY * 2));
            limit.setAge(rand.nextInt(Employee.MAX_AGE * 2));
            boolean isUP = rand.nextBoolean();
            Stream<Employee> stream1 = (type == ParallelType.Parallel) ? c1.parallelStream()
                    : (type == ParallelType.Sequential) ? c1.stream().sequential() : c1.stream();
            assertEquals(stream1.anyMatch(LambdaUtilities.randomGenericPredicate(isUP, limit, rule.getComparator())),
                    verifyMatch(c1, limit, isUP, false, rule));

            // Empty stream's anyMatch, noneMatch will return false always
            @SuppressWarnings("cast")
            Collection<Employee> emptyCol = hasIni ? (Collection<Employee>) LambdaUtilities
                    .create(typeObject, initSize) : (Collection<Employee>) LambdaUtilities.create(typeObject);
            Stream<Employee> stream2 = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.stream().sequential() : emptyCol.stream();
            assertTrue(!stream2.anyMatch(LambdaUtilities.randomGenericPredicate(isUP, limit, rule.getComparator())));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConcat() throws Exception {
        Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<Employee> l1 = generateData(DATA_SIZE);
            Collection<Employee> l2 = generateData(DATA_SIZE);
            Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
            Employee limit = generateData();
            limit.setSalary(rand.nextFloat() * (Employee.MAX_SALARY * 2));
            limit.setAge(rand.nextInt(Employee.MAX_AGE * 2));
            boolean isUP = rand.nextBoolean();
            Predicate<Employee> p = LambdaUtilities.randomGenericPredicate(isUP, limit, rule.getComparator());

            Stream<Employee> stream11 = (type == ParallelType.Parallel) ? l1.parallelStream()
                    : (type == ParallelType.Sequential) ? l1.stream().sequential() : l1.stream();
            Stream<Employee> stream21 = (type == ParallelType.Parallel) ? l2.parallelStream()
                    : (type == ParallelType.Sequential) ? l2.stream().sequential() : l2.stream();

            Collection<Employee> result1 = stream11.filter(p).collect(Collectors.toCollection(LinkedList<Employee>::new));
            Collection<Employee> result2 = stream21.filter(p).collect(Collectors.toCollection(LinkedList<Employee>::new));
            result1.addAll(result2);

            Stream<Employee> stream12 = (type == ParallelType.Parallel) ? l1.parallelStream()
                    : (type == ParallelType.Sequential) ? l1.stream().sequential() : l1.stream();
            Stream<Employee> stream22 = (type == ParallelType.Parallel) ? l2.parallelStream()
                    : (type == ParallelType.Sequential) ? l2.stream().sequential() : l2.stream();
            List<Employee> expectedList = Stream.concat(stream12, stream22).filter(p).collect(Collectors.<Employee>toList());
            List<Employee> testList = result1.stream().collect(Collectors.<Employee>toList());
            //Can't sort on unmodifiable list
            if (expectedList.size() > 1) {
                Collections.sort(testList, rule.getComparator());
                Collections.sort(expectedList, rule.getComparator());
            }
            for (int i = 0; i < testList.size(); i++) {
                assertEquals(rule.getValue(testList.get(i)), rule.getValue(expectedList.get(i)));
            }

            //Concat with empty stream should not change other input
            Collection<Employee> emptyList = hasIni ? LambdaUtilities.create(typeObject, initSize) : LambdaUtilities
                    .create(typeObject);
            Stream<Employee> stream3 = (type == ParallelType.Parallel) ? emptyList.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyList.stream().sequential() : emptyList.stream();

            List<Employee> result3 = Stream.concat(l1.stream(), stream3).collect(Collectors.<Employee>toList());
            List<Employee> list1 = new ArrayList<>(l1);
            //Can't sort on unmodifiable list
            if (result3.size() > 1) {
                Collections.sort(list1, rule.getComparator());
                Collections.sort(result3, rule.getComparator());
            }
            assertEquals(list1, result3);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFilter() throws Exception {
        Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<Employee> l = generateData(DATA_SIZE);
            Stream<Employee> stream = (type == ParallelType.Parallel) ? l.parallelStream()
                    : ((type == ParallelType.Sequential) ? l.stream().sequential() : l.stream());
            stream = l.stream();
            // Create predicate with random limit and random up/down size            
            Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
            Employee limit1 = generateData();
            limit1.setSalary(rand.nextFloat() * (Employee.MAX_SALARY * 2));
            limit1.setAge(rand.nextInt(Employee.MAX_AGE * 2));
            boolean isUP1 = rand.nextBoolean();
            Predicate<Employee> p1 = LambdaUtilities.randomGenericPredicate(isUP1, limit1, rule.getComparator());

            // Filter the data, check if it works as expected.
            Collection<Employee> result1 = stream.filter(p1).collect(Collectors.toCollection(LinkedList<Employee>::new));
            assertTrue(verifyMatch(result1, limit1, isUP1, true, rule));

            // filter on parallel stream can cause IllegalStateException
            stream = (type == ParallelType.Parallel) ? l.parallelStream() : ((type == ParallelType.Sequential) ? l
                    .stream().sequential() : l.stream());
            result1.clear();
            result1 = stream.filter(p1).collect(Collectors.toCollection(LinkedList<Employee>::new));
            assertTrue(verifyMatch(result1, limit1, isUP1, true, rule));

            // filter with Predicates.alwaysFlase() will get nothing
            stream = (type == ParallelType.Parallel) ? l.parallelStream() : ((type == ParallelType.Sequential) ? l
                    .stream().sequential() : l.stream());
            result1.clear();

            result1 = stream.filter(p1).filter(e -> false).collect(Collectors.toCollection(LinkedList<Employee>::new));
            assertTrue(result1.isEmpty());

            // Create another predicates with random limit and random up/down size 
            Employee limit2 = generateData();
            limit2.setSalary(rand.nextFloat() * (Employee.MAX_SALARY * 2));
            limit2.setAge(rand.nextInt(Employee.MAX_AGE * 2));
            boolean isUP2 = rand.nextBoolean();
            Predicate<Employee> p2 = LambdaUtilities.randomGenericPredicate(isUP2, limit2, rule.getComparator());

            // Testing of filtering on conjunction of predicates
            stream = (type == ParallelType.Parallel) ? l.parallelStream() : ((type == ParallelType.Sequential) ? l
                    .stream().sequential() : l.stream());
            List<Employee> result2 = stream.filter(p1).filter(p2).collect(Collectors.toCollection(ArrayList<Employee>::new));
            stream = (type == ParallelType.Parallel) ? l.parallelStream() : ((type == ParallelType.Sequential) ? l
                    .stream().sequential() : l.stream());
            List<Employee> result3 = stream.filter(p1.and(p2)).collect(Collectors.toCollection(ArrayList<Employee>::new));
            Collections.sort(result2, rule.getComparator());
            Collections.sort(result3, rule.getComparator());
            assertEquals(result2, result3);

            //result2 could be a EmptyList, we're putting result2, result3 into
            // concatList because EmptyList doesn't support addAll
            List<Employee> concatList = new ArrayList<>();
            stream = (type == ParallelType.Parallel) ? l.parallelStream() : ((type == ParallelType.Sequential) ? l
                    .stream().sequential() : l.stream());
            result2 = stream.filter(p1.and(p2)).collect(Collectors.toCollection(ArrayList<Employee>::new));
            stream = (type == ParallelType.Parallel) ? l.parallelStream() : ((type == ParallelType.Sequential) ? l
                    .stream().sequential() : l.stream());
            result3 = stream.filter(p1.or(p2)).collect(Collectors.toCollection(ArrayList<Employee>::new));

            concatList.addAll(result2);
            concatList.addAll(result3);
            result3.clear();
            stream = (type == ParallelType.Parallel) ? l.parallelStream() : ((type == ParallelType.Sequential) ? l
                    .stream().sequential() : l.stream());
            Stream<Employee> stream2 = (type == ParallelType.Parallel) ? l.parallelStream()
                    : ((type == ParallelType.Sequential) ? l.stream().sequential() : l.stream());
            result3 = Stream.concat(stream.filter(p1), stream2.filter(p2)).collect(Collectors.toCollection(ArrayList<Employee>::new));
            Collections.sort(concatList, rule.getComparator());
            Collections.sort(result3, rule.getComparator());
            assertEquals(concatList, result3);

            @SuppressWarnings("cast")
            Collection<Employee> emptyList = hasIni ? (Collection<Employee>) LambdaUtilities.create(typeObject,
                    initSize) : (Collection<Employee>) LambdaUtilities.create(typeObject);
            Stream<Employee> stream3 = (type == ParallelType.Parallel) ? emptyList.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyList.stream().sequential() : emptyList.stream();
            assertFalse(stream3.filter(p1).iterator().hasNext());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFind() throws Exception {
        Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<Employee> c = generateData(DATA_SIZE);
            Stream<Employee> stream1 = (type == ParallelType.Parallel) ? c.parallelStream()
                    : (type == ParallelType.Sequential) ? c.stream().sequential() : c.stream();
            Stream<Employee> stream2 = (type == ParallelType.Parallel) ? c.parallelStream()
                    : (type == ParallelType.Sequential) ? c.stream().sequential() : c.stream();
            java.util.Optional<Employee> opAny = stream1.findAny();
            java.util.Optional<Employee> opFirst = stream2.findFirst();
            assertTrue(opAny.isPresent());
            assertTrue(opFirst.isPresent());
            if (!stream1.isParallel()) {
                assertEquals(opAny, opFirst);
            }

            @SuppressWarnings("cast")
            Collection<Employee> emptyCol = hasIni ? (Collection<Employee>) LambdaUtilities
                    .create(typeObject, initSize) : (Collection<Employee>) LambdaUtilities.create(typeObject);
            Stream<Employee> emptyStream1 = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.stream().sequential() : emptyCol.stream();
            java.util.Optional<Employee> emptyAny = emptyStream1.findAny();
            assertFalse(emptyAny.isPresent());
            Stream<Employee> emptyStream2 = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.stream().sequential() : emptyCol.stream();
            java.util.Optional<Employee> emptyFirsty = emptyStream2.findFirst();
            assertFalse(emptyFirsty.isPresent());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testForEach() throws Exception {
        Iterator<ParallelType> iter
                = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<Employee> c = generateData(DATA_SIZE);
            Stream<Employee> stream = (type == ParallelType.Parallel) ? c.parallelStream()
                    : (type == ParallelType.Sequential) ? c.stream().sequential()
                            : c.stream();
            stream.forEach(t -> {
                assertTrue(c.contains(t));
            });
        }
    }

    @Test
    public void testGroupBy() throws Throwable {
        Iterator<ParallelType> iter
                = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<Employee> c = generateData(DATA_SIZE);

            Stream<Employee> stream = (type == ParallelType.Parallel) ? c.parallelStream()
                    : (type == ParallelType.Sequential) ? c.stream().sequential()
                            : c.stream();
            Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
            Map<Object, List<Employee>> result
                    = stream.collect(Collectors.groupingBy(employeeGenericFunction(rule)));
            verifyGroupBy(result, c, rule);

            @SuppressWarnings("cast")
            Collection<Employee> emptyList = hasIni
                    ? (Collection<Employee>) LambdaUtilities.create(typeObject, initSize)
                    : (Collection<Employee>) LambdaUtilities.create(typeObject);
            Stream<Employee> emptyStream = (type == ParallelType.Parallel) ? emptyList.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyList.stream().sequential()
                            : emptyList.stream();
            assertTrue(emptyStream.collect(Collectors.groupingBy(employeeGenericFunction(rule))).isEmpty());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLimit() throws Exception {
        Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<Employee> col = generateData(10);
            int limit = rand.nextInt(10 * 2);
            Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
            Stream<Employee> stream1 = (type == ParallelType.Parallel) ? col.parallelStream()
                    : (type == ParallelType.Sequential) ? col.stream().sequential() : col.stream();
            List<Employee> result1 = stream1.flatMap(genEmployeeFlatMapper(2, rule)).collect(Collectors.<Employee>toList());

            Stream<Employee> stream2 = (type == ParallelType.Parallel) ? col.parallelStream()
                    : (type == ParallelType.Sequential) ? col.stream().sequential() : col.stream();
            List<Employee> result2 = stream2.flatMap(genEmployeeFlatMapper(2, rule)).limit(limit)
                    .collect(Collectors.<Employee>toList());

            if (col instanceof Set) {
                assertTrue(result2.size() <= (limit < result1.size() ? limit : result1.size()));
            } else {
                assertEquals(result2.size(), (limit < result1.size() ? limit : result1.size()));
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testMax() throws Exception {
        Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
        Comparator<Employee> c1 = rule.getComparator();

        for (Comparator<Employee> c : new Comparator[]{c1, c1.reversed()}) {
            Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
            while (iter.hasNext()) {

                ParallelType type = iter.next();
                Collection<Employee> col = generateData(DATA_SIZE);
                Stream<Employee> stream1 = (type == ParallelType.Parallel) ? col.parallelStream()
                        : (type == ParallelType.Sequential) ? col.stream().sequential() : col.stream();
                java.util.Optional<Employee> optional = stream1.max(c);
                assertTrue(optional.isPresent());
                assertEquals(rule.getValue(optional.get()), rule.getValue(getMax1(col, c)));
                assertEquals(rule.getValue(optional.get()), rule.getValue(getMax2(col, c)));
                assertEquals(rule.getValue(optional.get()), rule.getValue(getMax3(col, c)));

                @SuppressWarnings("cast")
                Collection<Employee> emptyCol = hasIni ? (Collection<Employee>) LambdaUtilities
                        .create(typeObject, initSize) : (Collection<Employee>) LambdaUtilities.create(typeObject);
                Stream<Employee> emptyStream = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                        : (type == ParallelType.Sequential) ? emptyCol.stream().sequential() : emptyCol.stream();
                assertFalse(emptyStream.max(c).isPresent());
            }
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testMin() throws Exception {
        Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
        Comparator<Employee> c1 = rule.getComparator();

        for (Comparator<Employee> c : new Comparator[]{c1, c1.reversed()}) {
            Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
            while (iter.hasNext()) {

                ParallelType type = iter.next();
                Collection<Employee> col = generateData(DATA_SIZE);
                Stream<Employee> stream1 = (type == ParallelType.Parallel) ? col.parallelStream()
                        : (type == ParallelType.Sequential) ? col.stream().sequential() : col.stream();
                java.util.Optional<Employee> optional = stream1.min(c);
                assertTrue(optional.isPresent());
                assertEquals(rule.getValue(optional.get()), rule.getValue(getMin1(col, c)));
                assertEquals(rule.getValue(optional.get()), rule.getValue(getMin2(col, c)));
                assertEquals(rule.getValue(optional.get()), rule.getValue(getMin3(col, c)));

                @SuppressWarnings("cast")
                Collection<Employee> emptyCol = hasIni ? (Collection<Employee>) LambdaUtilities
                        .create(typeObject, initSize) : (Collection<Employee>) LambdaUtilities.create(typeObject);
                Stream<Employee> emptyStream = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                        : (type == ParallelType.Sequential) ? emptyCol.stream().sequential() : emptyCol.stream();
                assertFalse(emptyStream.min(c).isPresent());
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNoneMatch() throws Exception {
        Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<Employee> c1 = generateData(DATA_SIZE);
            Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
            Employee limit = generateData();
            limit.setSalary(rand.nextFloat() * (Employee.MAX_SALARY * 2));
            limit.setAge(rand.nextInt(Employee.MAX_AGE * 2));
            boolean isUP = rand.nextBoolean();
            Stream<Employee> stream1 = (type == ParallelType.Parallel) ? c1.parallelStream()
                    : (type == ParallelType.Sequential) ? c1.stream().sequential() : c1.stream();
            assertEquals(stream1.noneMatch(LambdaUtilities.randomGenericPredicate(isUP, limit, rule.getComparator())),
                    verifyMatch(c1, limit, !isUP, true, rule));

            // Empty stream's noneMatch will return true always
            @SuppressWarnings("cast")
            Collection<Employee> emptyCol = hasIni ? (Collection<Employee>) LambdaUtilities
                    .create(typeObject, initSize) : (Collection<Employee>) LambdaUtilities.create(typeObject);
            Stream<Employee> stream2 = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.stream().sequential() : emptyCol.stream();
            assertTrue(stream2.noneMatch(LambdaUtilities.randomGenericPredicate(isUP, limit, rule.getComparator())));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSubstream() throws Exception {
        Iterator<ParallelType> iter
                = EnumSet.allOf(ParallelType.class).iterator();
        BiFunction<Integer, Integer, Integer> bf = LambdaUtilities.randBetweenIntegerFunction();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<Employee> col = generateData(DATA_SIZE);
            int skip = rand.nextInt(col.size());
            //limit has more than 50% chance less than col.size() - skip
            int limit = rand.nextBoolean()
                    ? bf.apply(0, col.size() - skip)
                    : rand.nextInt(Integer.MAX_VALUE);
            Stream<Employee> stream1 = (type == ParallelType.Parallel) ? col.parallelStream()
                    : (type == ParallelType.Sequential) ? col.stream().sequential()
                            : col.stream();
            Iterator<Employee> it = stream1.skip(skip).limit(limit).iterator();
            verifySlice(col.iterator(), it, skip, limit);

            //limit=0 causes empty stream
            Stream<Employee> stream2 = (type == ParallelType.Parallel) ? col.parallelStream()
                    : (type == ParallelType.Sequential) ? col.stream().sequential()
                            : col.stream();
            assertFalse(stream2.skip(skip).limit(0).iterator().hasNext());

            //skip exceed collection size cause  empty stream
            Stream<Employee> stream3 = (type == ParallelType.Parallel) ? col.parallelStream()
                    : (type == ParallelType.Sequential) ? col.stream().sequential()
                            : col.stream();
            int skipExceeded = bf.apply(col.size(), Integer.MAX_VALUE);
            assertFalse(stream3.skip(skipExceeded).limit(1).iterator().hasNext());

            @SuppressWarnings("cast")
            Collection<Employee> emptyCol = hasIni
                    ? (Collection<Employee>) LambdaUtilities.create(typeObject, initSize)
                    : (Collection<Employee>) LambdaUtilities.create(typeObject);
            Stream<Employee> emptyStream = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.stream().sequential()
                            : emptyCol.stream();
            assertFalse(emptyStream.skip(skip).limit(limit).iterator().hasNext());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSorted() throws Exception {
        Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<Employee> col = generateData(DATA_SIZE);
            Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
            Comparator<Employee> c = rule.getComparator();
            Stream<Employee> stream = (type == ParallelType.Parallel) ? col.parallelStream()
                    : (type == ParallelType.Sequential) ? col.stream().sequential() : col.stream();
            List<Employee> reversed = stream.sorted(c.reversed()).collect(Collectors.<Employee>toList());
            // The reason conver l to sorted is CopyOnWriteArrayList doesn't
            // support
            // sort, we use ArrayList sort data instead
            List<Employee> sorted = new ArrayList<>(col);
            Collections.sort(sorted, c);

            // SortedSet instance's stream can't be reordered
            if (!(col instanceof SortedSet)) {
                Collections.reverse(sorted);
                for (int i = 0; i < sorted.size(); i++) {
                    assertEquals(rule.getValue(sorted.get(i)), rule.getValue(reversed.get(i)));
                }
            }

            @SuppressWarnings("cast")
            Collection<Employee> emptyCol = hasIni
                    ? (Collection<Employee>) LambdaUtilities.create(typeObject, initSize)
                    : (Collection<Employee>) LambdaUtilities.create(typeObject);
            Stream<Employee> stream2 = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.stream().sequential() : emptyCol.stream();
            assertFalse(stream2.sorted(Collections.reverseOrder()).iterator().hasNext());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testToArray() throws Exception {
        Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<Employee> c1 = generateData(DATA_SIZE);
            Stream<Employee> stream1 = (type == ParallelType.Parallel) ? c1.parallelStream()
                    : (type == ParallelType.Sequential) ? c1.stream().sequential() : c1.stream();
            Object[] arr1 = stream1.toArray();
            Object[] arr2 = c1.toArray();
            assert (arr1.length == arr2.length);
            for (int index = 0; index < arr1.length; index++) {
                assertEquals(arr1[index], arr2[index]);
            }

            @SuppressWarnings("cast")
            Collection<Employee> emptyCol = hasIni
                    ? (Collection<Employee>) LambdaUtilities.create(typeObject, initSize)
                    : (Collection<Employee>) LambdaUtilities.create(typeObject);
            Stream<Employee> stream2 = (type == ParallelType.Parallel) ? emptyCol.parallelStream()
                    : (type == ParallelType.Sequential) ? emptyCol.stream().sequential() : emptyCol.stream();
            assertEquals(stream2.toArray().length, 0);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUniqueElements() throws Exception {
        Iterator<ParallelType> iter = EnumSet.allOf(ParallelType.class).iterator();
        while (iter.hasNext()) {
            ParallelType type = iter.next();
            Collection<Employee> c1 = generateData(DATA_SIZE);
            Set<Employee> set1 = new HashSet<>(c1);
            Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
            Stream<Employee> stream1 = (type == ParallelType.Parallel) ? c1.parallelStream()
                    : (type == ParallelType.Sequential) ? c1.stream().sequential() : c1.stream();
            List<Employee> list2 = stream1.flatMap(genEmployeeFlatMapper(4, rule)).distinct().collect(Collectors.<Employee>toList());
            assertEquals(set1.size(), list2.size());
            assertTrue(set1.containsAll(list2));
        }
    }

    private Collection<Employee> generateData(int size) throws Exception {
        Collection<Employee> col = hasIni ? LambdaUtilities.create(typeObject, initSize)
                : LambdaUtilities.create(typeObject);
        for (int i = 0; i < size; i++) {
            col.add(generateData());
        }
        return col;
    }

    private Employee generateData() throws Exception {
        Employee element = new Employee();
        element.setId(StringUtilities.randomString(Employee.MAX_ID, Employee.MIN_ID));
        element.setAge(Employee.MIN_AGE + rand.nextInt(Employee.MAX_AGE - Employee.MIN_AGE));
        element.setMale(rand.nextBoolean());
        element.setSalary(Employee.MIN_SALARY + rand.nextFloat() * (Employee.MAX_SALARY - Employee.MIN_SALARY));
        int idx = rand.nextInt(Employee.Title.values().length);
        element.setTitle(Employee.Title.values()[idx]);
        return element;
    }

    private boolean verifyMatch(Collection<Employee> c, Employee limit, boolean isUP, boolean all, Employee.Rule rule) {
        Comparator<Employee> cmp = rule.getComparator();
        Iterator<Employee> it = c.iterator();
        while (it.hasNext()) {
            Employee current = it.next();
            if (isUP) {
                if (all) {
                    if (cmp.compare(current, limit) < 0) {
                        return false;
                    }
                } else {
                    if (cmp.compare(current, limit) >= 0) {
                        return true;
                    }
                }
            } else {
                if (all) {
                    if (cmp.compare(current, limit) >= 0) {
                        return false;
                    }
                } else {
                    if (cmp.compare(current, limit) < 0) {
                        return true;
                    }
                }
            }
        }
        return all;
    }

    private void verifyGroupBy(Map<Object, List<Employee>> result, Collection<Employee> employees, Employee.Rule rule) {
        Set<Employee> hashEm = new HashSet(employees);
        Iterator<?> keyiter = result.keySet().iterator();
        while (keyiter.hasNext()) {
            switch (rule) {
                case AGE:
                    Integer ageKey = (Integer) keyiter.next();
                    List<Employee> ageEmList = result.get(ageKey);
                    for (Employee e : ageEmList) {
                        assertEquals(e.getAge() / 10, ageKey.intValue());
                    }
                    break;
                case SALARY:
                    String salaryKey = (String) keyiter.next();
                    List<Employee> salarayEmList = result.get(salaryKey);
                    for (Employee e : salarayEmList) {
                        assertEquals(e.getSalary() <= 6000 ? "LOW"
                                : (e.getSalary() > 15000 ? "HIGH" : "MEDIUM"), salaryKey);
                    }
                    break;
                case MALE:
                    Boolean genderKey = (Boolean) keyiter.next();
                    List<Employee> genderEmList = result.get(genderKey);
                    for (Employee e : genderEmList) {
                        assertEquals(Boolean.valueOf(e.isMale()), genderKey);
                    }
                    break;
                case TITLE:
                    Employee.Title titleKey = (Employee.Title) keyiter.next();
                    List<Employee> titleEmList = result.get(titleKey);
                    for (Employee e : titleEmList) {
                        assertEquals(e.getTitle(), titleKey);
                    }
                    break;
                case ID:
                    Integer idKey = (Integer) keyiter.next();
                    List<Employee> idEmList = result.get(idKey);
                    for (Employee e : idEmList) {
                        assertEquals(e.getId().length(), idKey.intValue());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void verifyMultiFunction(Collection<Employee> orig, List<Employee> result, int selected, int unit, Employee.Rule rule) throws Exception {
        Comparator<Employee> cmp = rule.getComparator();
        switch (selected) {
            case 0:
                assertEquals(result.size(), 0);
                break;
            case 1:
                List<Employee> l1 = new ArrayList<>(orig);
                Collections.sort(l1, cmp);
                Collections.sort(result, cmp);
                assertEquals(l1, result);
                break;
            case 2:
                List<Employee> l2 = new ArrayList<>();
                Iterator<Employee> it2 = orig.iterator();
                switch (rule) {
                    case AGE:
                        while (it2.hasNext()) {
                            Employee cur = it2.next();
                            for (int i = 0; i < cur.getAge(); i++) {
                                Employee employee = new Employee();
                                employee.setAge(cur.getAge() * (cur.getAge() - 1) / 2 + i);
                                l2.add(employee);
                            }
                        }
                        break;
                    case SALARY:
                        l2 = new ArrayList<>();
                        it2 = orig.iterator();
                        while (it2.hasNext()) {
                            Employee cur = it2.next();
                            for (int i = 0; i < cur.getSalary(); i++) {
                                Employee employee = new Employee();
                                employee.setSalary(cur.getSalary() * (cur.getSalary() - 1) / 2 + i);
                                l2.add(employee);
                            }
                        }
                        break;
                    case ID:
                        l2 = new ArrayList<>();
                        it2 = orig.iterator();
                        while (it2.hasNext()) {
                            Employee cur = it2.next();
                            int step = cur.getId().length() / unit + unit - 1;
                            for (int i = 0; i < cur.getId().length(); i += step) {
                                Employee employee = new Employee();
                                employee.setId(new String(cur.getId().substring(i, i + step >= cur.getId().length() ? cur.getId().length() - 1 : i + step)));
                                l2.add(employee);
                            }
                        }
                        break;
                    case MALE:
                        l2 = new ArrayList<>();
                        it2 = orig.iterator();
                        while (it2.hasNext()) {
                            Employee cur = it2.next();
                            Employee employee = new Employee();
                            employee.setMale(!cur.isMale());
                            l2.add(employee);
                        }
                        break;
                    case TITLE:
                        l2 = new ArrayList<>();
                        it2 = orig.iterator();
                        while (it2.hasNext()) {
                            Employee cur = it2.next();
                            for (int i = 0; i < cur.getTitle().ordinal(); i++) {
                                Employee employee = new Employee();
                                employee.setTitle(Employee.Title.values()[i]);
                                l2.add(employee);
                            }
                        }
                        break;
                }
                Collections.sort(l2, cmp);
                Collections.sort(result, cmp);
                assertEquals(l2, result);
                break;
            case 3:
                List<Employee> l3 = new ArrayList<>();
                Iterator<Employee> it3 = orig.iterator();
                while (it3.hasNext()) {
                    Employee current = it3.next();
                    for (int i = 0; i < unit; i++) {
                        l3.add(current);
                    }
                }
                Collections.sort(l3, cmp);
                Collections.sort(result, cmp);
                assertEquals(l3, result);
                break;
            default:
                break;
        }
    }

    private Employee getMax1(Collection<Employee> col, Comparator<Employee> c) {
        assert (!col.isEmpty());
        Iterator<Employee> it = col.iterator();
        Employee max = it.next();
        while (it.hasNext()) {
            Employee next = it.next();
            if (c.compare(max, next) < 0) {
                max = next;
            }
        }
        return max;
    }

    private Employee getMax2(Collection<Employee> col, Comparator<Employee> c) {
        assert (!col.isEmpty());
        java.util.Optional<Employee> max = col.stream().reduce(LambdaUtilities.maxGenericBinaryOperator(c));
        assert (max.isPresent());
        return max.get();
    }

    private Employee getMax3(Collection<Employee> col, Comparator<Employee> c) {
        assert (!col.isEmpty());
        Employee any = col.iterator().next();
        Employee max = col.stream().reduce(any, LambdaUtilities.maxGenericFunction(c),
                LambdaUtilities.maxGenericBinaryOperator(c));
        return max;
    }

    /*
     * c could be reversed order, we can't use MAX_VALUE or MIN_VALUE but we can
     * use 1st element to compare with.
     */
    private Employee getMin1(Collection<Employee> col, Comparator<Employee> c) {
        assert (!col.isEmpty());
        Iterator<Employee> it = col.iterator();
        Employee min = it.next();
        while (it.hasNext()) {
            Employee next = it.next();
            if (c.compare(min, next) > 0) {
                min = next;
            }
        }
        return min;
    }

    private Employee getMin2(Collection<Employee> col, Comparator<Employee> c) {
        assert (!col.isEmpty());
        java.util.Optional<Employee> min = col.stream().reduce(LambdaUtilities.minGenericBinaryOperator(c));
        assert (min.isPresent());
        return min.get();
    }

    private Employee getMin3(Collection<Employee> col, Comparator<Employee> c) {
        assert (!col.isEmpty());
        Employee any = col.iterator().next();
        Employee min = col.stream().reduce(any, LambdaUtilities.minGenericFunction(c),
                LambdaUtilities.minGenericBinaryOperator(c));
        return min;
    }

    private void verifySlice(Iterator<Employee> itOrg, Iterator<Employee> itSliced, int skip, int limit) {
        int pos = 0;
        while (itOrg.hasNext() && pos++ < skip) {
            itOrg.next();
        }

        while (itOrg.hasNext() && pos++ < limit) {
            assertEquals(itOrg.next(), itSliced.next());
        }

    }

    enum ParallelType {

        Parallel, Sequential, Default
    }
}
