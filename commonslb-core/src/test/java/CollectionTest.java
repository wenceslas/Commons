
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.containers.PrefillArrayList;
import lt.lb.commons.Log;
import lt.lb.commons.benchmarking.Benchmark;
import lt.lb.commons.containers.PrefillArrayMap;
import lt.lb.commons.misc.MyRandom;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author laim0nas100
 */
public class CollectionTest {

    static {
        Log.instant = true;
    }

    public static void print(Object... args) {
        String s = "";
        for (Object o : args) {
            s += o + " ";
        }
        System.out.println(s);
    }

    public void test() throws InterruptedException {
        PrefillArrayList<Long> list = new PrefillArrayList<>(0L);
        for (int i = 0; i < 10; i++) {
            list.put(i, (long) i * 2);
        }
        Log.print(list.toString());

        ListIterator<Long> listIterator = list.listIterator();
        while (listIterator.hasNext()) {
            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
        }
        listIterator.remove();
        Log.print(list.toString());
        Log.println();
        while (listIterator.hasPrevious()) {
            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.previous());
        }
        Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex());
        listIterator.add(13L);
        Log.print(list.toString());
        Log.println();
        for (int i = 0; i < 10; i++) {
            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());
            Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.previous());
        }
        Log.println();
        Log.print("next:" + listIterator.nextIndex(), "prev:" + listIterator.previousIndex(), "Value:" + listIterator.next());

        listIterator.set(20L);
        Log.print(list.toString());
        Log.await(1, TimeUnit.HOURS);

    }

    public Runnable makeRun(Map<Integer, String> map, Random r, int times) {
        return () -> {
            map.put(0, r.nextInt() + "");
            for (int i = 0; i < times; i++) {
                int key = r.nextInt(5000) - r.nextInt(5000);
                String val = r.nextInt() + "";
                if (r.nextBoolean()) {
                    map.put(key, val);
                } else {
                    map.remove(key);
                }
                map.get(key);
                map.containsKey(key);

            }
        };
    }

//    @Test
    public void bechHash() {
        Benchmark b = new Benchmark();

        Map<Integer, String> map1 = new HashMap<>();
        Map<Integer, String> map2 = new PrefillArrayMap<>();

        b.threads = 1;
        b.useGVhintAfterFullBench = true;
        System.out.println(b.executeBench(5000, "HashMap", makeRun(map1, new MyRandom(1337), 10000)));
        System.out.println(b.executeBench(5000, "PrefillMap", makeRun(map2, new MyRandom(1337), 10000)));

        System.out.println(b.executeBench(5000, "PrefillMap", makeRun(map2, new MyRandom(1337), 10000)));
        System.out.println(b.executeBench(5000, "HashMap", makeRun(map1, new MyRandom(1337), 10000)));

        System.out.println(b.executeBench(5000, "HashMap", makeRun(map1, new MyRandom(1337), 10000)));
        System.out.println(b.executeBench(5000, "PrefillMap", makeRun(map2, new MyRandom(1337), 10000)));

        System.out.println(b.executeBench(5000, "PrefillMap", makeRun(map2, new MyRandom(1337), 10000)));
        System.out.println(b.executeBench(5000, "HashMap", makeRun(map1, new MyRandom(1337), 10000)));

    }
}
