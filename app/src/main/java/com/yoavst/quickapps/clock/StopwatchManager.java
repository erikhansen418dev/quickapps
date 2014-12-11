package com.yoavst.quickapps.clock;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Yoav.
 */
public class StopwatchManager {
	private static Runnable sRunnable;
	private static Stopwatch sStopwatch;
	private static long sMilliseconds = 0;
	private static long sPeriod = 0;

	private StopwatchManager() {
	}

	public static void startTimer(final long period, Runnable callback) {
		sRunnable = callback;
		sPeriod = period;
		initStopwatch();
		startTimer();
	}

	private static void startTimer() {
		new Timer().schedule(sStopwatch, 10, sPeriod);
	}

	private static void initStopwatch() {
		sStopwatch = new Stopwatch() {
			@Override
			protected void runCode() {
				sMilliseconds += sPeriod;
				if (sRunnable != null) sRunnable.run();
			}
		};
	}

	/**
	 * Make the timer to run on background, with no callback
	 */
	public static void runOnBackground() {
		sRunnable = null;
		// If the stopwatch is paused, it will just save its data, and will not run.
		if (!(sStopwatch == null || sStopwatch.isRunning())) {
			sStopwatch.cancel();
			sStopwatch = null;
		}
	}

	public static void runOnUi(Runnable callback) {
		sRunnable = callback;
		if (hasOldData() && sStopwatch == null) {
			initStopwatch();
			startTimer();
			sStopwatch.isRunning(false);
			if (sRunnable != null) sRunnable.run();
		}
	}

	public static void stopTimer() {
		sRunnable = null;
		if (sStopwatch != null) {
			sStopwatch.cancel();
			sStopwatch = null;
		}
		sMilliseconds = 0;
		sPeriod = 0;
	}

	public static void PauseTimer() {
		if (sStopwatch != null) sStopwatch.isRunning(false);
	}

	public static void ResumeTimer() {
		if (sStopwatch != null) sStopwatch.isRunning(true);
	}

	public static long getMillis() {
		return sMilliseconds;
	}

	public static boolean isRunning() {
		return sStopwatch != null && sStopwatch.isRunning();
	}

	public static boolean hasOldData() {
		return sMilliseconds != 0 && sPeriod != 0;
	}

	public static abstract class Stopwatch extends TimerTask {
		private boolean isRunning = true;

		public synchronized void isRunning(boolean running) {
			this.isRunning = running;
		}

		public synchronized boolean isRunning() {
			return isRunning;
		}

		@Override
		public void run() {
			if (isRunning) runCode();
		}

		protected abstract void runCode();
	}
}