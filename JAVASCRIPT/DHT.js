var DHT = require('bittorrent-dht')
var dht = new DHT()
var infohashReceived
var check = false

var DHTClass = Java.type('com.turn.ttorrent.client.DHTManager')
infohashReceived = DHTClass.getHash()
console.log('RECEIVED INFOHASH ' + infohashReceived)

// Start listening.
dht.listen(20000, function () {
  	console.log('\nDHT LISTENING\n')
})

// When ready, notify java to get the infohash.
dht.on('ready', function () {
	console.log('DHT LOOKUP')
	dht.lookup(infohashReceived)
})

dht.on('peer', function (addr, hash, from) {
	// Transmit results to Java.	
	DHTClass.lookupCallback(addr)
	check = DHTClass.announceCheck()

	if(check == true){
		dht.announce(infohashReceived, 55555, null)
		console.log('ANNOUNCE RENEWED');
		DHTClass.setNeedAnnounce(false);
	}
})

