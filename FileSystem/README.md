# File System Using Scala/akka
**Name: Abdelghafour Aboukacem**

### Implementation

This repository contains the implementation of the project till day 3 part 2.
The file [StoreManagerFile](https://github.com/AbdelGhafour69/scala_cloud_programming/blob/main/FileSystem/src/main/scala/StoreManagerFile.scala) contains the code for the following actors:
  - UserFile: contains the workflow of a user running commands on the terminal, the commands are in the form of: Add/Store/Lookup Key Value
  - StoreManager: Actor responsible for responding to the user

In source files you will find:
 - CacheAgent: representation of the caching agent
 - RandomUser: representation of the user that issues 5000 requests to the store manager
 - MapAgent: representation of the key/value store
 - Main: the main file contains the first use case implemented using the in-memory key/value store.
 
The workflow of a Store operation:
  - The user issues the store command to the store manager
  - The manager sends a store message to the caching agent which stores it in its map which implements an LRU caching strategy [This](https://medium.com/@knoldus/what-is-lru-cache-and-how-to-implement-it-in-scala-76e96457d716) is the method used. 
  - The Caching Agent also sends a message to the MapAgent to append the key/value.
  - The MapAgent adds the data to the map and to the file.
  
 The workflow of a Delete operation:
  - The user issues the delete command to the store manager
  - The manager sends a delete message to the caching agent which deletes the key from its map.
  - The Caching Agent also sends a message to the MapAgent to delete the key.
  - The MapAgent deletes the key from the map and from the file.
  
  The workflow of a Lookup operation:
  - The user issues the lookup command to the store manager
  - The manager sends a lookup message to the caching agent. If the data is present in the map the caching agent sends back a response message. If not it sends the lookup request to the MapAgent
  - The MapAgent does the same thing, if it finds the key in the map it returns a response message to the user through the previous actors. If it doesn't find it, then a lookup in the file is performed and either a response or an error message is sent back to the user through the previous actors.


## To run

In order to run the project:
```bash
cd FileSystem
```
```bash
sbt run
```

By default, this will run the ```RandomUserAgent``` If you choose to run the normal UserAgent which writes commands on terminal, please make sure to change the actor nature under the entry point of [StoreManagerFile](https://github.com/AbdelGhafour69/scala_cloud_programming/blob/main/FileSystem/src/main/scala/StoreManagerFile.scala)


In order to generate the jar file:
```bash
sbt package
```

The jar file can be downloaded from [Here](https://um6p-my.sharepoint.com/:u:/g/personal/abdelghafour_aboukacem_um6p_ma/EcwpWAWLb2dIgRRW8CaXCqQBoqqYnW3m2zm8Thxus8MHzA?e=jFhEn7)


