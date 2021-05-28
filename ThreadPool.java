import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public class ThreadPool
{
    private volatile int workersNumber;

    private final Object threadsLock = new Object();
    private final Object tasksLock = new Object();

    private final List<Worker> threads = new LinkedList<>();
    private final List<Task> tasks = new LinkedList<>();

    public ThreadPool(int threadNumbers)
    {
        synchronized (threadsLock)
        {
            workersNumber = threadNumbers;
        }

        for (int i = 0; i < threadNumbers; i++)
        {
            Worker thread = new Worker();
            thread.start();

            synchronized (threadsLock)
            {
                threads.add(thread);
            }
        }
    }

    public int getThreadNumbers()
    {
        synchronized (threadsLock)
        {
            return workersNumber;
        }
    }

    public void setThreadNumbers(int threadNumbers)
    {
        synchronized (threadsLock)
        {
            int oldThreadNumbers = this.getThreadNumbers();
            workersNumber = threadNumbers;

            if (threadNumbers >= oldThreadNumbers)
            {
                int threadsToAdd  = threadNumbers - oldThreadNumbers;

                for (int i = 0; i < threadsToAdd; i++)
                {
                    Worker thread = new Worker();
                    thread.start();
                    threads.add(thread);
                }
            }
            else
            {
                int threadsToRemove = oldThreadNumbers - threadNumbers;

                for (int i = 0; i < threadsToRemove; i++)
                {
                    Worker removedWorker = threads.get(0);
                    threads.remove(0);
                    removedWorker.delete();

                    synchronized (tasksLock)
                    {
                        tasksLock.notifyAll();
                    }
                }
            }
        }
    }

    public void invokeLater(Runnable runnable)
    {
        synchronized (tasksLock)
        {
            tasks.add(new Task(runnable));
            tasksLock.notifyAll();
        }
    }

    public void invokeAndWait(Runnable runnable) throws InterruptedException, InvocationTargetException
    {
        Task task = new Task(runnable);

        synchronized (tasksLock)
        {
            tasks.add(task);
            tasksLock.notifyAll();
        }

        synchronized (task.getRunnable())
        {
            task.getRunnable().wait();

            if (task.getThrowable() != null)
            {
                throw new InvocationTargetException(task.getThrowable());
            }
        }
    }

    public void invokeAndWaitUninterruptible(Runnable runnable) throws InvocationTargetException
    {
        Task task = new Task(runnable);

        synchronized (tasksLock)
        {
            tasks.add(task);
            tasksLock.notifyAll();
        }

        synchronized (task.getRunnable())
        {
            while (true)
            {
                try
                {
                    task.getRunnable().wait();

                    if (task.getThrowable() != null)
                    {
                        throw new InvocationTargetException(task.getThrowable());
                    }
                    else
                    {
                        return;
                    }
                } catch (InterruptedException ignored) {}
            }
        }
    }

    public void deleteThread(Worker thread)
    {
        synchronized (threadsLock)
        {
            thread = null;
        }
    }

    private class Worker extends Thread
    {
        private volatile boolean isActive = true;

        @Override
        public void run()
        {
            while (isActive)
            {
                Task task;

                synchronized (tasksLock)
                {
                    while (tasks.isEmpty() && isActive)
                    {
                        try
                        {
                            tasksLock.wait();
                        } catch (InterruptedException ignored) {}
                    }

                    if (!isActive)
                    {
                        break;
                    }

                    task = tasks.get(0);
                    tasks.remove(0);
                }

                try
                {
                    task.getRunnable().run();
                }
                catch (Throwable e)
                {
                    synchronized (task.getRunnable())
                    {
                        task.setThrowable(e);
                        task.getRunnable().notifyAll();
                    }
                }

                synchronized (task.getRunnable())
                {
                    task.getRunnable().notifyAll();
                }

                if (!isActive)
                {
                    break;
                }
            }

            deleteThread(this);
        }

        public synchronized void delete()
        {
            isActive = false;
        }
    }

    private static class Task
    {
        private volatile Throwable throwable;
        private final Runnable runnable;

        private Task(Runnable runnable)
        {
            this.runnable = runnable;
        }

        public synchronized void setThrowable(Throwable throwable)
        {
            this.throwable = throwable;
        }

        public synchronized Throwable getThrowable()
        {
            return throwable;
        }

        public synchronized Runnable getRunnable()
        {
            return runnable;
        }
    }
}
