// Driver program to explain how to use ThreadPool for several Tasks

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        // Creating the ThreadPool
        ThreadPool pool = new ThreadPool(7);

        // invokeLater
        for (int i = 0; i < 1000; i = i + 5)
        {
            Task task = new Task(i);
            pool.invokeLater(task);
        }

        // setThreadNumbers (increase)
        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        pool.setThreadNumbers(10);
        System.out.println("Thread numbers: " + pool.getThreadNumbers());

        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        pool.setThreadNumbers(15);
        System.out.println("Thread numbers: " + pool.getThreadNumbers());

        // setThreadNumbers (decrease)
        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        pool.setThreadNumbers(5);
        System.out.println("Thread numbers: " + pool.getThreadNumbers());

        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        pool.setThreadNumbers(3);
        System.out.println("Thread numbers: " + pool.getThreadNumbers());

        // invokeLater, invokeAndWait, invokeAndWaitUninterruptible
        for (int i = 0; i < 999; i = i + 3)
        {

            Task task1 = new Task(i);
            TaskWithException task2 = new TaskWithException(i + 1);
            TaskWithException task3 = new TaskWithException(i + 2);

            Thread thread1 = new Thread(() ->
            {
                pool.invokeLater(task1);
            });

            Thread thread2 = new Thread(() ->
            {
                try
                {
                    pool.invokeAndWait(task2);
                }
                catch (InvocationTargetException | InterruptedException invocationTargetException)
                {
                    invocationTargetException.printStackTrace();
                }
            });

            Thread thread3 = new Thread(() ->
            {
                try
                {
                    pool.invokeAndWaitUninterruptible(task3);
                }
                catch (InvocationTargetException invocationTargetException)
                {
                    invocationTargetException.printStackTrace();
                }
            });

            thread1.start();
            thread2.start();
            thread3.start();

            thread1.join();
            thread2.join();
            thread3.join();

            thread1 = null;
            thread2 = null;
            thread3 = null;
        }

        // Exit
        pool.setThreadNumbers(0);
    }

    record Task(int num) implements Runnable
    {

        public void run()
        {
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException ignored){}

            System.out.println(new Date() + "- job " + num + " - " + Thread.currentThread().getName());
        }
    }

    record TaskWithException(int num) implements Runnable
    {

        public void run()
        {
            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}

            System.out.println(new Date() + "- job " + num + " - " + Thread.currentThread().getName());
            int x = 1/0;
        }
    }
}
