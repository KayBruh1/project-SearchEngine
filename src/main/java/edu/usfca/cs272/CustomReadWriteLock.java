package edu.usfca.cs272;

import java.util.ConcurrentModificationException;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CustomReadWriteLock{
	/** The conditional lock used for reading. */
	private final SimpleLock readerLock;

	/** The conditional lock used for writing. */
	private final SimpleLock writerLock;

	/** The number of active readers. */
	private int readers;

	/** The number of active writers; */
	private int writers;

	/** The thread that holds the write lock. */
	private Thread activeWriter;

	/** The log4j2 logger. */
	private static final Logger log = LogManager.getLogger();

	/**
	 * The lock object used for synchronized access of readers and writers. For
	 * security reasons, a separate private final lock object is used.
	 *
	 * @see <a href=
	 *      "https://wiki.sei.cmu.edu/confluence/display/java/LCK00-J.+Use+private+final+lock+objects+to+synchronize+classes+that+may+interact+with+untrusted+code">
	 *      SEI CERT Oracle Coding Standard for Java</a>
	 */
	private final Object lock;

	/**
	 * Initializes a new simple read/write lock.
	 */
	public CustomReadWriteLock() {
		readerLock = new ReadLock();
		writerLock = new WriteLock();

		lock = new Object();

		readers = 0;
		writers = 0;

		activeWriter = null;
	}

	/**
	 * Returns the reader lock.
	 *
	 * @return the reader lock
	 */
	public SimpleLock readLock() {
		return readerLock;
	}

	/**
	 * Returns the writer lock.
	 *
	 * @return the writer lock
	 */
	public SimpleLock writeLock() {
		return writerLock;
	}

	/**
	 * Returns the number of active readers.
	 *
	 * @return the number of active readers
	 */
	public int readers() {
		synchronized (lock) {
			return readers;
		}
	}

	/**
	 * Returns the number of active writers.
	 *
	 * @return the number of active writers
	 */
	public int writers() {
		synchronized (lock) {
			return writers;
		}
	}

	/**
	 * Determines whether the thread running this code and the writer thread are in
	 * fact the same thread.
	 *
	 * @return true if the thread running this code and the writer thread are not
	 *         null and are the same thread
	 *
	 * @see Thread#currentThread()
	 */
	public boolean isActiveWriter() {
		synchronized (lock) {
			return Thread.currentThread().equals(activeWriter);
		}
	}

	/**
	 * A simple lock used for conditional synchronization as an alternative to using
	 * a {@code synchronized} block.
	 *
	 * Similar but simpler than {@link Lock}.
	 *
	 * @author CS 272 Software Development (University of San Francisco)
	 * @version Spring 2024
	 */
	public static interface SimpleLock {
		/**
		 * Acquires the lock. If the lock is not available then the current thread
		 * becomes disabled for thread scheduling purposes and lies dormant until the
		 * lock has been acquired.
		 */
		public void lock();

		/**
		 * Releases the lock.
		 */
		public void unlock();
	}

	/**
	 * Used to maintain simultaneous read operations.
	 */
	private class ReadLock implements SimpleLock {
		/**
		 * Controls access to the read lock. The active thread is forced to wait while
		 * there are any active writers and it is not the active writer thread. Once
		 * safe, the thread is allowed to acquire a read lock by incrementing the number
		 * of active readers.
		 */
		@Override
		public void lock() {
			try {
				synchronized (lock) {
					while (writers > 0 && !Thread.currentThread().equals(activeWriter)) {
						lock.wait();
					}

					readers++;
				}
			} catch (InterruptedException ex) {
				log.catching(Level.DEBUG, ex);
				Thread.currentThread().interrupt();
			}
		}

		/**
		 * Will decrease the number of active readers and notify any waiting threads if
		 * necessary.
		 *
		 * @throws IllegalStateException if no readers to unlock
		 */
		@Override
		public void unlock() throws IllegalStateException {
			synchronized (lock) {
				if (readers == 0) {
					throw new IllegalStateException("No readers");
				}

				readers--;
				lock.notifyAll();
			}
		}
	}

	/**
	 * Used to maintain exclusive write operations.
	 */
	private class WriteLock implements SimpleLock {
		/**
		 * Controls access to the write lock. The active thread is forced to wait while
		 * there are any active readers or writers, and it is not the active writer
		 * thread. Once safe, the thread is allowed to acquire a write lock by
		 * incrementing the number of active writers and setting the active writer
		 * reference.
		 */
		@Override
		public void lock() {
			synchronized (lock) {
				try {
					while ((readers > 0 || writers > 0) && !Thread.currentThread().equals(activeWriter)) {
						lock.wait();
					}

					writers++;
					activeWriter = Thread.currentThread();
				} catch (InterruptedException e) {
					log.catching(Level.DEBUG, e);
					Thread.currentThread().interrupt();
				}
			}
		}

		/**
		 * Will decrease the number of active writers and notify any waiting threads if
		 * necessary. Also unsets the active writer if appropriate.
		 *
		 * @throws IllegalStateException           if no writers to unlock
		 * @throws ConcurrentModificationException if there are writers but unlock is
		 *                                         called by a thread that does not hold
		 *                                         the write lock
		 */
		@Override
		public void unlock() throws IllegalStateException, ConcurrentModificationException {
			synchronized (lock) {
				if (writers == 0) {
					throw new IllegalStateException("No writers");
				}

				if (!Thread.currentThread().equals(activeWriter)) {
					throw new ConcurrentModificationException("Thread does not hold the write lock");
				}

				writers--;
				if (writers == 0) {
					activeWriter = null;
				}
				lock.notifyAll();
			}
		}
	}
}