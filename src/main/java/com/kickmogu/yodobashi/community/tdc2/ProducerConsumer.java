package com.kickmogu.yodobashi.community.tdc2;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

public abstract class ProducerConsumer<T> {

	public abstract T produce(int threadNo, int count);
	public abstract void consume(T obj, int threadNo);

	protected int producerNum;
	protected int consumerNum;
	protected int queueCapacity;

	private List<Producer> producers = Lists.newArrayList();
	private List<Consumer> consumers = Lists.newArrayList();

	protected BlockingQueue<T> queue = new ArrayBlockingQueue<T>(1000);
	

	public ProducerConsumer() {
		this(1, 1);
	}

	public ProducerConsumer(int producerNum, int consumerNum) {
		this(producerNum, consumerNum, 100);
	}


	public ProducerConsumer(int producerNum, int consumerNum, int queueCapacity) {
		this.producerNum = producerNum;
		this.consumerNum = consumerNum;
		this.queueCapacity = queueCapacity;
	}

	public boolean allProducerFinished() {
		for (Producer producer : producers) {
			if (producer.isAlive()) return false;
		}
		return true;
	}

	public boolean allConsumerFinished() {
		for (Consumer consumer : consumers) {
			if (consumer.isAlive()) return false;
		}
		return true;
	}

	public ProducerConsumer<T> waitToFinish() {
		for (Producer producer : producers) {
			try {
				producer.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for (Consumer consumer : consumers) {
			try {
				consumer.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		onFinish();
		return this;
	}

	public void onFinish() {
	}

	public ProducerConsumer<T> start() {
		BlockingQueue<T> queue = new ArrayBlockingQueue<T>(queueCapacity);
		for (int i = 0 ; i < producerNum; i++) {
			Producer producer = new Producer(queue, i);
			producers.add(producer);
			producer.start();
		}

		for (int i = 0 ; i < consumerNum; i++) {
			Consumer consumer = new Consumer(queue, i);
			consumers.add(consumer);
			consumer.start();
		}
		return this;
	}


	public class Producer extends Thread {

		protected BlockingQueue<T> queue;
		protected int threadNo;
		Producer(BlockingQueue<T> queue, int threadNo) {
			this.queue = queue;
			this.threadNo = threadNo;
		}

		public void run() {
			try {
				int count = 0;
				while (true) {
					T data = produce(threadNo, count++);
					if (data == null) break;
					queue.put(data);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public class Consumer extends Thread {

		protected BlockingQueue<T> queue;
		protected int threadNo;
		Consumer(BlockingQueue<T> queue, int threadNo) {
			this.queue = queue;
			this.threadNo = threadNo;
		}

		public void run() {
			try {
				while (true) {
					T data = queue.poll(100, TimeUnit.MILLISECONDS);
					if (data == null) {
						if (allProducerFinished()) break;
						continue;
					}
					consume(data, threadNo);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}catch (Throwable e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}


}

