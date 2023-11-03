# ngrok - analogue of ngrok service.
Application enables any service, eg REST-api java app, which runs on local machine, to be
reached in internet.

Actors:
**Service** - is service on local machine, that we want to be reachable worldwide.
**Server** - is main app, that runs somewhere on server-machine in internet
**Client** - client app, that user has to run on local machine.
**User** - user, which requests your service from the link, given by Server.
User can be browser, postman, curl and so on.



## How to run

1. Run Server after Client and TestService in IDE.
2. type http://localhost:9001/index.html in firefox or postman
3. You got 200 Response and body "test". That response comes from TestService app on your localhost

Client accepts command-line parameter -port-number=8080. Port-number is a port of Service

Сервер принимает запросы юзеров по вайлдкарду, туда попадают все запросы, включая субдомены
перепаковывает их в свой формат, сериализаует

Сервtр общается с клиентом посредством запроса типа server.ngrok.com
