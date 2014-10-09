// Variables.
var DHT = require('bittorrent-dht')
var dht = new DHT()
var infohashReceived
var check = false

// Load the DHT Manager class for access via JS.
var DHTClass = Java.type('com.turn.ttorrent.client.DHTManager')

// Get the infohash from the torrent.
infohashReceived = DHTClass.getHash()
console.log('RECEIVED INFOHASH ' + infohashReceived)

// Function to perform an announce from Java.
var callAnnounce = function() {
	dht.announce(infohashReceived, 55555, null)
	console.log('PERFORM ANNOUNCE')
};

// Start listening.
dht.listen(20000, function () {
  	console.log('\nDHT LISTENING\n')
})

// When connected, start to look for peers.
dht.on('ready', function () {
	dht.lookup(infohashReceived, function(){
		// Do the announce right after the lookup
		dht.announce(infohashReceived, 55555, null)
		DHTClass.startAnnounceTimer()
	})
})

// Triggered each time a new peer is identified, send info to Java.
dht.on('peer', function (addr, hash, from) {
	// Transmit results to Java.	
	DHTClass.lookupCallback(addr)
})


