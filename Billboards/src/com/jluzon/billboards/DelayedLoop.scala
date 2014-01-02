package com.jluzon.billboards

import scala.actors._

class DelayedLoop(seconds: Int, callback: () => Unit ) extends Thread {
	val done = new java.util.concurrent.atomic.AtomicBoolean(false)
	override def run {
		while (!done.get()) {
			callback();
			Thread.sleep(seconds * 1000);
		}
	}
}
