Chord is used to optimally allocate servers to store data.

Operation starts with a file insertion
get hash from server
Add node with that file this will be our node 0 and the entry point. Successor and Predecessor of Node 0 is Node 0 itself.
Next file comes
get hash from server node 0
If it is in same ID space, add file to that node
Server 1 calls join passing its successor information to the new joining server (New server who has # of the file in its ID space) and updates its own predecessor and successor
Server 0 finds # of the coming file, does not fit in its own ID range
Passes it to its successor
keeps track of whether this file visited the node or not.
If visited that means it is its new successor.
Store 


Run the root Server
