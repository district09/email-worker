# README #
The email-worker component is one of the components that are part of the email-service. The email-worker provides asynchronous communication between senders and the exchange service. It is the email-worker component that will effectively try to send the email with a third-party email exchange, i.e. MS Exchange. 

## Features ##

* Both creation and retry events are picked up from the message broker and deliverd to the exchange services.
* Retry events are created when the email could not be send initially with the exchange services. 
* Status events are created when the email is send or could not be delivered to exchange after X retry's.

## Security ##
Always keep following security rules in mind when using credentials!

### Do's ###

* Store credentials in a password manager. 
* Use environment variables to make credentials available to your applications.
* If this is not possible check for secure alternatives for your technology.
* Share credentials using point-to-point communication and only to the people that need the credentials.
* Avoid sharing credentials after initial exchange.

### Don'ts ###

* Don't hard code credentials.
* Don't communicate credentials with clients.
* Don't store credentials in public or private repositories.
* Don't store credentials in documentation.

## Dependencies

* A running SQL instance.
* A running message broker that supports the amqp protocol.
* A running email-api instance. 
* A MS Exchange (online) account. (This is optional if you put the email-worker in *mock* mode)

## Building the source code ##

### Locally ###

Package the Quarkus application.
```./mvnw package```

Run the packaged application.
```java -jar target/email-worker-3.0-SNAPSHOT-runner.jar```

### S2I ###

### Docker ###

## Running the application ##

### Environment variables ###

## Testing the application ##

Run integration tests using the [Karate framework](https://github.com/intuit/karate). See the [testing documentation](./karate/README.md) on how to run the tests locally and more information. 