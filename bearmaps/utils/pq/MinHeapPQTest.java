package bearmaps.utils.pq;

import org.junit.Test;

public class MinHeapPQTest {

    @Test
    public void test1() {
        MinHeapPQ<String> refrigerator = new MinHeapPQ<>();
        refrigerator.insert("milk", 0);
        refrigerator.insert("bread", 1);
        refrigerator.insert("noodles", 2);
        refrigerator.insert("rice", 3);
        refrigerator.insert("fanta", 4);
        refrigerator.changePriority("rice", 2);
        System.out.println(refrigerator.toString());
    }
}
