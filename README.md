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

The ShortUrl class requires a data store connector for initialisation. A MySQL connector is included. This is how you would use the
included vagrant mysql instance:

	var mysql = new MySqlDataStore("192.168.33.10", "root", "root", "shorturl")
	
	var shorturl = new ShortUrl(mysql, mysql) // Not that the datastore is supplied twice
	
	var hash = shorturl.hashUrl("http://www.google.com")
	
	var url = shorturl.urlFromHash(hash)

Alternative DB's
---
Out-of-the-box shorturl is configured to use the MySQL instance in the included vagrant. You can, however, switch to an alternative storage mechanism
to suit your needs.

Make changes in the Configuration.scala file found at src/main/scala/net/tobysullivan/shorturl/config/Configuration.scala.

The HashStore describes the mechanism for storing hash => URL mappings. The StatsStore is used to manage statistics (such as clicks).

You have the option to use either an alternative MySQL instance by specifying connection credentials or switching to the alternative In-Memory 
storage (which is implemented with immutable HashMaps). The latter is much faster but not persistent.

Using InMemoryDataStore

	# In Configuration.scala
	var HASH_STORE: HashStore = InMemoryDataStore
	var STATS_STORE: StatsStore = InMemoryDataStore

As a final option, you can write a connector for the data store of your choice by extending the HashStore and StatsStore traits. 
The HashStore and StatsStore are mutually exclusive components so you are welcome to use a different storage mechanism for each (such
as MySQL for the Hash Store and InMemory for the Stats Store).