package com.turn.ttorrent.client;

public class AnnounceDeamon implements Runnable{
	
	// ------------------------------------------------------------------------------------
	// Variables.
	// ------------------------------------------------------------------------------------
	
	private Thread thread;
	private Boolean stop = false;
	private DHTManager dht;
	
	// ------------------------------------------------------------------------------------
	// Constructors.
	// ------------------------------------------------------------------------------------
	
	public AnnounceDeamon(DHTManager dht){
		this.dht = dht;
	}
	
	// ----------------------------------------------------------------
	// Daemon thread.
	// ----------------------------------------------------------------
	
	/**
	 * Start the Announce Daemon thread.
	 */
	public void start() {
		this.stop = false;

		if (this.thread == null || !this.thread.isAlive()) {
			this.thread = new Thread(this);
			this.thread.setDaemon(true);
			this.thread.setName("bt-dht-announce");
			this.thread.start();
		}
	}

	/**
	 * Stop the Announce Daemon thread.
	 */
	public void stop() {
		this.stop = true;

		if (this.thread != null && this.thread.isAlive()) {
			this.thread.interrupt();
		}

		this.thread = null;
	}

	@Override
	public void run() {
		System.out.println("\nANNOUNCE DEAMON STARTED\n");
		
		while(this.stop == false){
			try {
				// Wait for 1 minute, then call the announce.
				Thread.sleep(300 * 1000);
				
				DHTManager.setNeedAnnounce(true);
				System.out.println("\nANNOUNCE RENEWAL...\n");
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}