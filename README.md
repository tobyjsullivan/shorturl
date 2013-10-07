shorturl
===

A Short URL library

What's in the Box
---
Scala code can be found under src/

Helper libraries are under lib/

Vagrant configuration for a MySQL server can be found under vagrant/

The SBT build definition is located in build.sbt

You can use the ./unittest script in the root directory as a quick jumpstart 

Getting Started
---
The easiest way to initialise the environment is simply to use the ./unittest script. This will launch a local MySQL Server VM using vagrant,
run the SBT install (including installing all requirements) and execute the unit tests. 

Using MySQL
---

The ShortUrl class requires a data store connector for initialisation. A MySQL connector is included. This is how you would use the
included vagrant mysql instance:

	import net.tobysullivan.shorturl._

	var mysql = new MySqlDataStore("192.168.33.10", "root", "root", "shorturl") // Only the database and user need to be defined. Tables are created automatically. 
	
	var shorturl = new ShortUrl(mysql, mysql) // Note that the datastore is supplied twice. 
											  // You can optionally use a separate database for storing statistics
	
	var hash = shorturl.hashUrl("http://www.google.com")
	
	var url = shorturl.urlFromHash(hash)

The first parameter of ShortUrl, HashStore, describes the mechanism for storing hash => URL mappings. The second, StatsStore, 
is used to manage statistics (such as clicks).

Alternative Data Stores
---
It is easiest to use shorturl with MySQL as demonstrated above. You can, however, switch to an alternative storage mechanism
to suit your needs.

One alternative provided out-of-box is an in-memory storage (which is implemented with immutable HashMaps and akka agents). This
storage is much faster but the data is not persisted between instances.

Using InMemoryDataStore

	# InMemoryDataStore is a singleton object
	var shorturl = new ShortUrl(InMemoryDataStore, InMemoryDataStore)

As a final option, you can write a connector for the data store of your choice by extending the HashStore and StatsStore traits. 
The HashStore and StatsStore are mutually exclusive components so you are welcome to use a different storage mechanism for each (such
as MySQL for the Hash Store and InMemory for the Stats Store).
