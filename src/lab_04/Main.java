package lab_04;

import com.sun.corba.se.spi.ior.ObjectKey;

import java.util.Scanner;
import java.util.concurrent.*;

public class Main {


    private static int x = 0;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введи икс");
        x = scanner.nextInt();

        Callable<Object> task_f = () -> {
            System.out.println("Вычисление f(x)...");
            return x*2;
        };
        Callable<Object> task_g = () -> {
            System.out.println("вычисление g(x)...");
            return x*10;
        };

        ExecutorService es = Executors.newFixedThreadPool(2);

        Future<Object> task_f_Result = es.submit(task_f);
        Future<Object> task_g_Result = es.submit(task_g);

        try {
            System.out.println("Ожидание результата работы...");
            System.out.println("Задача f(x) завершена? - " + task_f_Result.isDone());
            System.out.println("Задача g(x) завершена? - " + task_g_Result.isDone());
            if (!task_f_Result.isDone()) {
                Object result_f = task_f_Result.get(3, TimeUnit.SECONDS);

            }

            if (!task_g_Result.isDone()) {
                Object result_g = task_g_Result.get(3, TimeUnit.SECONDS);

            }
            System.out.println("Задача f(x) завершена? - " + task_f_Result.isDone());
            System.out.println("Задача g(x) завершена? - " + task_g_Result.isDone());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        es.shutdown();
        int allResult = (int) task_f_Result.get() + (int)task_g_Result.get();
        System.out.println("Результат: " + allResult);
    }
}
