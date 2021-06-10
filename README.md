# ThreadPool

A simple ThreadPool written in Java with thread increase/decrease and exception handling feature. Methods:

1. getThreadNumbers(): Returns the number of available threads.
2. setThreadNumbers(int threadNumbers): Sets the number of available threads in the threadpool to an arbitrary number. Use setThreadNumbers(0); to kill the threadpool.
3. invokeLater(Runnable runnable): takes a Runnable object and puts it in the line for processing. The runnable will wun as soon as there's an available thread with no more work to do.
4. invokeAndWait(Runnable runnable): This is exactly like the previous functions, but it'll wait to see if there's an interruption or error in running the runnable.
5. invokeAndWaitUninterruptible(Runnable runnable): Same as the last function but it will not be interrupted while waitinh fir the task to be finished. It'll only return task errors.

The rest is just inner classes and functions to make code more clean and easier to read. The code also will delete removed threads (by setThreadNumbers() method) from the memory so that we don't get a memory limit.
