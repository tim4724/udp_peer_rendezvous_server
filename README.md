# udp_peer_rendezvous_server

Clients implementing the https://github.com/timbirdy/udp_peer_connector/tree/master can establish a direct udp peer-to-peer connection with the help of this server.

1. run main() in /src/RendezVousServer on your publicly reachable server.

<br/>
<br/>
<br/>
 
Or Implement your own Server  
What happens on the Server:

1. The Server receives DatagrammPackets:  
  Byte:      Value  
   0 - 3:   packet sender id  
   4 - 7:   id of the other client  
   8 -11:   state = 1  
  12-15:   LAN Address (Ipv4) of the sender  
  16-19:   Local Port of the sender for this session  
-> The Server caches the provided data and the public ip address and public port of the packet sender  
-> The Server just sends the packet backt to the sender  
  
2. The client send the same packets, but state = 2  
 -> The Server responds with the same packet he received BUT:  
  Byte:      Value  
  12 - 15:   the LAN ip addresses of the other client  
  16 - 19:   the Local Port of the other client  
  20 - 23:   the Public IP address of the other client  
  24 - 27:   the Public Port of the other clientb  
-> If the server doesn't have the requested data -> do nothing  
