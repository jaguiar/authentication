# authentication
Project to simulate the authentication part.
It's a dummy oauth authorization server largely inspired by this project https://github.com/Baeldung/spring-security-oauth

# Build
```sh
mvn clean install
```

# Run
To launch the authorization server on port 8081 on context /shady-authorization-server 
```sh
mvn spring-boot:run
```

# Test 
To log in with other applications : 
User : john/123

Run docker-compose and
Launch the [dummy-oauth-client app](https://github.com/jaguiar/customer-api/tree/master/dummy-oauth-client) and the [customer-java-resttemplate](https://github.com/jaguiar/customer-api/tree/master/customer-java-resttemplate) app, for example, to test
