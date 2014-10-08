var DHT = require('bittorrent-dht')

var dht = new DHT()

dht.listen(20000, function () {
  console.log('now listening')
})

dht.on('ready', function () {
  // DHT is ready to use (i.e. the routing table contains at least K nodes, discovered
  // via the bootstrap nodes)

  // find peers for the given torrent info hash
  dht.lookup('f93dbaba0dbd855b37d230349e018028935995a4')
})

dht.on('peer', function (addr, hash, from) {
  console.log('found potential peer ' + addr + ' through ' + from)
})

