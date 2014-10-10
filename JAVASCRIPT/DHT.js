// Variables.
var DHT = require('bittorrent-dht')
var dht = new DHT()
var infohashReceived
var check = false
var minutes = 1
var announceInterval = minutes * 60 * 1000

// Load the DHT Manager class for access via JS.
var DHTClass = Java.type('com.turn.ttorrent.client.DHTManager')

// Get the infohash from the torrent.
infohashReceived = DHTClass.getHash()
console.log('RECEIVED INFOHASH ' + infohashReceived)

// Start listening.
dht.listen(20000, function () {
  	console.log('\nDHT LISTENING\n')
})

// When connected, start to look for peers.
dht.on('ready', function () {
	dht.lookup(infohashReceived, function(){
		// Do the announce right after the lookup
		dht.announce(infohashReceived, 55555, null)
		console.log('FIRST ANNOUNCE PERFORMED')
		
		setInterval(function(){
			dht.announce(infohashReceived, 55555, null)
			console.log('ANNOUNCE PERFORMED')
		}, announceInterval)
	})
})

// Triggered each time a new peer is identified, send info to Java.
dht.on('peer', function (addr, hash, from) {
	// Transmit results to Java.	
	DHTClass.lookupCallback(addr)
})


