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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import test.java.util.LambdaUtilities;
import test.java.util.StringUtilities;

public class EmployeeStreamTest<T extends Collection<Employee>> extends StreamTestTemplate<Employee> {

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

    private static Function<Employee, Stream<Employee>> genStream(
            Function<Employee, Function<Employee, Consumer<Integer>>> action, boolean isDefault) {
        return (e) -> {
            ArrayList<Employee> res = new ArrayList<>();
            for (int i = 0; i < e.getId().length(); i += 2) {
                Employee employee = e.clone();
                if (!isDefault) {
                    action.apply(e).apply(employee).accept(i);
                }
                employee.setId(e.getId());
                res.add(employee);
            }
            return res.stream();
        };
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
                RuleAction action = actOnRule(rule);
                return action.genEmployeeFlatMapperByRule();

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
        RuleAction action = actOnRule(rule);
        return action.employeeGenericFunction();
    }

    private static RuleAction actOnRule(Employee.Rule rule) {
        switch (rule) {
            case AGE:
                return new RuleAction() {

                    @Override
                    public Function<Employee, Stream<Employee>> genEmployeeFlatMapperByRule() {
                        return genStream(e -> employee -> i -> employee.setAge(e.getAge() * (e.getAge() - 1) / 2 + i), false);
                    }

                    @Override
                    public Function<Employee, Object> employeeGenericFunction() {
                        return e -> e.getAge() / 10;
                    }

                    @Override
                    public void verifyGroubBy(Object key, List<Employee> list) {
                        verifyList(key, list,
                                (e, keyVar) -> {
                                    Integer ageKey = (Integer) keyVar;
                                    assertEquals(e.getAge() / 10, ageKey.intValue());
                                });
                    }
                };
            case SALARY:
                return new RuleAction() {

                    @Override
                    public Function<Employee, Stream<Employee>> genEmployeeFlatMapperByRule() {
                        return genStream(e -> employee -> i -> employee.setSalary(e.getSalary() * (e.getSalary() - 1) / 2 + i), false);
                    }

                    @Override
                    public Function<Employee, Object> employeeGenericFunction() {
                        return e -> e.getSalary() <= 6000 ? "LOW" : (e.getSalary() > 15000 ? "HIGH" : "MEDIUM");
                    }

                    @Override
                    public void verifyGroubBy(Object key, List<Employee> list) {
                        verifyList(
                                key, list,
                                (e, keyVar) -> assertEquals(e.getSalary() <= 6000 ? "LOW"
                                                : (e.getSalary() > 15000 ? "HIGH" : "MEDIUM"), keyVar));
                    }
                };
            case MALE:
                return new RuleAction() {

                    @Override
                    public Function<Employee, Stream<Employee>> genEmployeeFlatMapperByRule() {
                        return genStream(e -> employee -> i -> employee.setMale(!e.isMale()), false);
                    }

                    @Override
                    public Function<Employee, Object> employeeGenericFunction() {
                        return e -> e.isMale();
                    }

                    @Override
                    public void verifyGroubBy(Object key, List<Employee> list) {
                        verifyList(key, list,
                                (e, keyVar) -> assertEquals(Boolean.valueOf(e.isMale()), keyVar));
                    }
                };
            case TITLE:
                return new RuleAction() {

                    @Override
                    public Function<Employee, Stream<Employee>> genEmployeeFlatMapperByRule() {
                        return genStream(e -> employee -> i -> employee.setTitle(Employee.Title.values()[i]), false);
                    }

                    @Override
                    public Function<Employee, Object> employeeGenericFunction() {
                        return e -> e.getTitle();
                    }

                    @Override
                    public void verifyGroubBy(Object key, List<Employee> list) {
                        verifyList(
                                key, list,
                                (e, keyVar) -> assertEquals(e.getTitle(), keyVar));
                    }
                };
            case ID:
                return new RuleAction() {

                    @Override
                    public Function<Employee, Stream<Employee>> genEmployeeFlatMapperByRule() {
                        return genStream(null, true);
                    }

                    @Override
                    public Function<Employee, Object> employeeGenericFunction() {
                        return e -> e.getId().length();
                    }

                    @Override
                    public void verifyGroubBy(Object key, List<Employee> list) {
                        verifyList(
                                key, list,
                                (e, keyVar) -> {
                                    Integer idKey = (Integer) keyVar;
                                    assertEquals(e.getId().length(), idKey.intValue());
                                });
                    }
                };
            default:
                return new RuleAction() {

                    @Override
                    public Function<Employee, Stream<Employee>> genEmployeeFlatMapperByRule() {
                        return genStream(null, true);
                    }

                    @Override
                    public Function<Employee, Object> employeeGenericFunction() {
                        throw new RuntimeException("No such rule");
                    }

                    @Override
                    public void verifyGroubBy(Object key, List<Employee> list) {
                        //do nothing
                    }
                };
        }
    }

    private static void verifyList(Object key, List<Employee> list, BiConsumer<Employee, Object> assertion) {
        for (Employee e : list) {
            assertion.accept(e, key);
        }
    }

    protected Class<T> typeObject;

    public EmployeeStreamTest(Class<T> clazz, int... initSizes) {
        super(clazz, initSizes);
        this.typeObject = clazz;
    }

    @Override
    public String getTestName() {
        return typeObject.getName() + "<Employee>";
    }

    @Override
    protected Predicate<Employee> getRandomPredicate() throws Exception {
        Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
        Employee limit = generateData();
        limit.setSalary(rand.nextFloat() * (Employee.MAX_SALARY * 2));
        limit.setAge(rand.nextInt(Employee.MAX_AGE * 2));
        boolean isUP = rand.nextBoolean();
        return LambdaUtilities.randomGenericPredicate(isUP, limit, rule.getComparator());
    }

    @Override
    protected void singleStreamVerifyPredicateTest(Function<Stream<Employee>, Function<Predicate<Employee>, Consumer<Boolean>>> otherDeclarationAndAssert, boolean verifyMatchForAll) throws Exception {

        singleStreamTestIteration(stream -> collection -> {
            try {
                Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
                Employee limit = generateData();
                limit.setSalary(rand.nextFloat() * (Employee.MAX_SALARY * 2));
                limit.setAge(rand.nextInt(Employee.MAX_AGE * 2));
                boolean isUP = rand.nextBoolean();

                Predicate<Employee> p = LambdaUtilities.randomGenericPredicate(isUP, limit, rule.getComparator());

                boolean verifyMatch = verifyMatch(collection, limit, isUP, verifyMatchForAll, rule);

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
        Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
        Employee limit = generateData();
        limit.setSalary(rand.nextFloat() * (Employee.MAX_SALARY * 2));
        limit.setAge(rand.nextInt(Employee.MAX_AGE * 2));
        boolean isUP = rand.nextBoolean();

        Comparator<Employee> comparator = rule.getComparator();
        Predicate<Employee> p = LambdaUtilities.randomGenericPredicate(isUP, limit, comparator);

        super.testConcat(p, comparator, i -> (testList, expectedList) -> {
            assertEquals(rule.getValue(testList.get(i)), rule.getValue(expectedList.get(i)));
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFilter() throws Exception {
        // Create predicate with random limit and random up/down size            
        Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
        Employee limit1 = generateData();
        limit1.setSalary(rand.nextFloat() * (Employee.MAX_SALARY * 2));
        limit1.setAge(rand.nextInt(Employee.MAX_AGE * 2));
        boolean isUP1 = rand.nextBoolean();
        Predicate<Employee> p1 = LambdaUtilities.randomGenericPredicate(isUP1, limit1, rule.getComparator());

        Comparator<Employee> comparator = rule.getComparator();

        super.testFilter(p1, comparator, result1 -> assertTrue(verifyMatch(result1, limit1, isUP1, true, rule)));
    }

    @Test
    @SuppressWarnings("unchecked")
    @Override
    public void testFind() throws Exception {
        super.testFind();
    }

    @Test
    @SuppressWarnings("unchecked")
    @Override
    public void testForEach() throws Exception {
        super.testForEach();
    }

    @Test
    public void testGroupBy() throws Throwable {
        Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];

        super.testGroupBy(() -> employeeGenericFunction(rule), result -> collection -> verifyGroupBy(result, collection, rule));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLimit() throws Exception {
        int limit = rand.nextInt(DATA_SIZE * 2);

        Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
        super.testLimit(() -> genEmployeeFlatMapper(2, rule), limit);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testMax() throws Exception {
        Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
        Comparator<Employee> c1 = rule.getComparator();

        for (Comparator<Employee> c : new Comparator[]{c1, c1.reversed()}) {

            super.testMax((Object employee) -> rule.getValue((Employee) employee), c);
        }
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testMin() throws Exception {
        Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
        Comparator<Employee> c1 = rule.getComparator();

        for (Comparator<Employee> c : new Comparator[]{c1, c1.reversed()}) {

            super.testMin((Object employee) -> rule.getValue((Employee) employee), c);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNoneMatch() throws Exception {
        Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
        Employee limit = generateData();
        limit.setSalary(rand.nextFloat() * (Employee.MAX_SALARY * 2));
        limit.setAge(rand.nextInt(Employee.MAX_AGE * 2));
        boolean isUP = rand.nextBoolean();

        Function<Collection<Employee>, Boolean> getNonexpectedValue
                = stream -> verifyMatch(stream, limit, !isUP, true, rule);

        super.testNoneMatch(getNonexpectedValue, LambdaUtilities.randomGenericPredicate(isUP, limit, rule.getComparator()));
    }

    @Test
    @SuppressWarnings("unchecked")
    @Override
    public void testSubstream() throws Exception {
        super.testSubstream();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSorted() throws Exception {
        Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];
        Comparator<Employee> c = rule.getComparator();

        super.testSorted(c, employee -> rule.getValue((Employee) employee));
    }

    @Test
    @SuppressWarnings("unchecked")
    @Override
    public void testToArray() throws Exception {
        super.testToArray();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUniqueElements() throws Exception {
        Employee.Rule rule = Employee.Rule.values()[rand.nextInt(Employee.Rule.values().length)];

        super.testUniqueElements(genEmployeeFlatMapper(4, rule));
    }

    @Override
    protected Employee generateData() throws Exception {
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
            Object key = keyiter.next();
            List<Employee> list = result.get(key);
            RuleAction action = actOnRule(rule);
            action.verifyGroubBy(key, list);
        }
    }

    private interface RuleAction {

        Function<Employee, Stream<Employee>> genEmployeeFlatMapperByRule();

        Function<Employee, Object> employeeGenericFunction();

        void verifyGroubBy(Object key, List<Employee> list);
    }
}
