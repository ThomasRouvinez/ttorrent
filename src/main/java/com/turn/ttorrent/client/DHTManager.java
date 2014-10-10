package com.turn.ttorrent.client;

import java.io.File;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import com.oracle.avatar.js.Loader;
import com.oracle.avatar.js.Server;
import com.oracle.avatar.js.eventloop.ThreadPool;
import com.oracle.avatar.js.log.Logging;
import com.turn.ttorrent.common.Peer;

public class DHTManager implements Runnable{

	// ------------------------------------------------------------------------------------
	// Variables.
	// ------------------------------------------------------------------------------------

	private Thread thread;
	private Thread announceThread;
	private boolean stop;
	private float timer;

	private static ScriptEngine se;
	final String[] options = new String[] {
			"-scripting", // shebangs in modules
			"--const-as-var" // until const is fully
	};

	private static Client client;
	private ConnectionHandler service;
	private static Server server;
	private static List<Peer> peers;

	private static String infohash;

	private static Invocable dhtScript;
	private static String dhtLibraryPath = "DHT.js";

	private static final NashornScriptEngineFactory ENGINE_FACTORY = new NashornScriptEngineFactory();

	// ------------------------------------------------------------------------------------
	// Constructors.
	// ------------------------------------------------------------------------------------

	/**
	 * Constructor for DHT Manager
	 * @param client Bittorrent client.
	 */
	public DHTManager(Client client){
		this.client = client;
		infohash = client.getTorrent().getHexInfoHash();
		peers = new ArrayList<Peer>();
		stop = true;
		timer = 0f;

		// Initialize NashHorn.
		initializeNashHorn();

		// Fetch the DHT script.
		dhtLibraryPath = new File(dhtLibraryPath).getAbsolutePath();	
	}

	// ----------------------------------------------------------------
	// NasHorn initialization.
	// ----------------------------------------------------------------

	/**
	 * Method to initialize all requirements for Nashorn.
	 */
	private void initializeNashHorn(){

		try {
			se = ENGINE_FACTORY.getScriptEngine(options);
		} catch (IllegalArgumentException iae) {
			se = ENGINE_FACTORY.getScriptEngine();
		}

		Loader loader = new Loader.Core();

		try {
			server = new Server(se, loader, new Logging(false), System.getProperty("user.dir"), se.getContext(), 0, ThreadPool.newInstance(), null, null, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ----------------------------------------------------------------
	// Lookup start and callback.
	// ----------------------------------------------------------------

	/**
	 * Getter for the Info Hash of the torrent.
	 * @return This torrent Info Hash.
	 */
	public static String getHash(){
		System.out.println("\nINFO HASH REQUESTED FROM TORRENT");
		return infohash;
	}

	/**
	 * Receive the DHT Peers data and add them to the Peer list.
	 * @param result Result of the DHT lookup in Javascript (peer address).
	 */
	public static void lookupCallback(Object result){
		// Process the new peer.
		String[] splitArray = new String[2];
		String peer = (String) result;

		splitArray[0] = peer.substring(0, peer.indexOf(":"));
		splitArray[1] = peer.substring(peer.indexOf(":")+1, peer.length());

		// Insert new peer into peer list.
		Peer newDHTPeer = new Peer(splitArray[0], Integer.parseInt(splitArray[1]));

		if(! peers.contains(newDHTPeer) && peers.size() < 200){
			peers.add(newDHTPeer);
		}

		client.handleDiscoveredPeers(peers);
	}

	// ----------------------------------------------------------------
	// Main DHT thread.
	// ----------------------------------------------------------------

	/**
	 * Start the dhtClient request thread.
	 */
	public void start() {
		this.stop = false;

		if (this.thread == null || !this.thread.isAlive()) {
			this.thread = new Thread(this);
			this.thread.setDaemon(true);
			this.thread.setName("bt-dht-client");
			this.thread.start();
		}
	}

	/**
	 * Stop the dhtClient thread.
	 */
	public void stop() {
		this.stop = true;

		if (this.thread != null && this.thread.isAlive()){
			this.thread.interrupt();
		}

		this.thread = null;
	}

	@Override
	public void run() {
		try {
			// Launch the DHT.
			server.run(dhtLibraryPath);
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
	}
}