/**
 * File: Consumer.java
 * Author: Justina Choi (choi.justina@gmail.com)
 * Date: 
 */

package sse;

public abstract class Consumer {
	private ConsumerType type = null;
	
	
	public Consumer(ConsumerType type) {
		this.type = type;
	}
	
	protected abstract void construct();
	
	public ConsumerType getType() {
		return this.type;
	}
	
	

}
