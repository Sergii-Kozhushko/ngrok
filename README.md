# ngrok - analogue of ngrok service.
Application enables any service, eg REST-api java app, which runs on local machine to be 
reached in internet.

**Service** - is service on local machine, that we want to be reachable worldwide.
**Server** - is main app, that runs somewhere on server-machine in internet
**Client** - client app, that user has to run on local machine.



## How to run

1. Run Server, Client and TestService in IDE or command-line.
2. type http://localhost:80/index.html in firefox
3. You got 404 Response, that response comes from TestService app on your localhost 

Client accepts command-line parameter -port-number=8080. Port-number is a port of Service
