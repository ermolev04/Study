package queue;

import java.util.function.Predicate;

public class MyArrayQueueTest {
    public static void main(String[] args) {
        Predicate<Object> isE1 = x -> x.toString().equals("e1");

        System.out.println("Module");
        fillModule();
        while(!ArrayQueueModule.isEmpty()) {
            System.out.println(ArrayQueueModule.size() + " " + ArrayQueueModule.element()
                    + " " + ArrayQueueModule.dequeue());
        }
        System.out.println("Size before fill: " + ArrayQueueModule.size());
        fillModule();
        System.out.println("Size before clear: " + ArrayQueueModule.size());
        ArrayQueueModule.clear();
        System.out.println("Size after clear: " + ArrayQueueModule.size());
        fillModuleBack();
        while(!ArrayQueueModule.isEmpty()) {
            System.out.println(ArrayQueueModule.size() + " " + ArrayQueueModule.peek()
                    + " " + ArrayQueueModule.remove());
        }
        System.out.println("Count Pred: " + ArrayQueueModule.countIf(isE1));
        ArrayQueueModule.enqueue("e1");
        System.out.println("Count Pred after: " + ArrayQueueModule.countIf(isE1));

        System.out.println();
        System.out.println("ADT");
        ArrayQueueADT queueADT = ArrayQueueADT.create();
        fillADT(queueADT);
        while(!ArrayQueueADT.isEmpty(queueADT)) {
            System.out.println(ArrayQueueADT.size(queueADT) + " " + ArrayQueueADT.element(queueADT)
                    + " " + ArrayQueueADT.dequeue(queueADT));
        }
        System.out.println("Size before fill: " + ArrayQueueADT.size(queueADT));
        fillADT(queueADT);
        System.out.println("Size before clear: " + ArrayQueueADT.size(queueADT));
        ArrayQueueADT.clear(queueADT);
        System.out.println("Size after clear: " + ArrayQueueADT.size(queueADT));
        fillADTBack(queueADT);
        while(!ArrayQueueADT.isEmpty(queueADT)) {
            System.out.println(ArrayQueueADT.size(queueADT) + " " + ArrayQueueADT.peek(queueADT)
                    + " " + ArrayQueueADT.remove(queueADT));
        }
        System.out.println("Count Pred: " + ArrayQueueADT.countIf(queueADT, isE1));
        ArrayQueueADT.enqueue(queueADT, "e1");
        System.out.println("Count Pred after: " + ArrayQueueADT.countIf(queueADT, isE1));

        System.out.println();
        System.out.println("Classic");
        ArrayQueue queueClassic = new ArrayQueue();
        fillClassic(queueClassic);

        while(!queueClassic.isEmpty()) {
            System.out.println(queueClassic.size() + " " + queueClassic.element()
                    + " " + queueClassic.dequeue());
        }
        System.out.println("Size before fill: " + queueClassic.size());
        fillClassic(queueClassic);
        System.out.println("Size before clear: " + queueClassic.size());
        queueClassic.clear();
        System.out.println("Size after clear: " + queueClassic.size());
        fillClassic(queueClassic);
        while(!queueClassic.isEmpty()) {
            System.out.println(queueClassic.size() + " " + queueClassic.peek()
                    + " " + queueClassic.remove());
        }
        System.out.println("Count Pred: " + queueClassic.countIf(isE1));
        queueClassic.enqueue("e1");
        System.out.println("Count Pred after: " + queueClassic.countIf(isE1));
    }

    private static void fillModule () {
        for(int i = 0; i < 5; i++) {
            ArrayQueueModule.enqueue("e" + i);
        }
    }

    private static void fillModuleBack () {
        for(int i = 0; i < 5; i++) {
            ArrayQueueModule.push("e" + i);
        }
    }

    private static void fillADT (ArrayQueueADT queue) {
        for(int i = 0; i < 5; i++) {
            ArrayQueueADT.enqueue(queue, "e" + i);
        }
    }

    private static void fillADTBack (ArrayQueueADT queue) {
        for(int i = 0; i < 5; i++) {
            ArrayQueueADT.push(queue, "e" + i);
        }
    }
    private static void fillClassic (ArrayQueue queue) {
        for(int i = 0; i < 5; i++) {
            queue.enqueue("e" + i);
        }
    }

    private static void fillClassicBack (ArrayQueue queue) {
        for(int i = 0; i < 5; i++) {
            queue.push("e" + i);
        }
    }



}
