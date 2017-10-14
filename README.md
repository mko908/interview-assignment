# Interview Assignment
## Disclaimer
**_Do not attempt to commit to this repo. You should fork this repo in order to complete this assignment._**

## Introduction
This is a take-home assignment that should be completed before the in-person interview.

The program will be based in [Spring Boot](https://docs.spring.io/spring-boot/docs/1.5.7.RELEASE/reference/htmlsingle/#boot-features-integration). Take time to scrutinize the reference documentation and some of the [getting started guides](https://spring.io/guides).

Requirements:
1. The result must be executable and compile without error
2. You can leverage any resources required to accomplish the provided task
3. Documentation and/or the ability to explain all written lines of code
4. Basic unit tests

## Problem
You're going to build an inventory management system! Such a system should be capable of:
1. **User** registration/sign-up
2. Login authorization/authentication
3. Allowing users to manage their own **Shop**
4. Allowing users to modify (CRUD) **Inventory** within their _own_ **Shop**
5. Exit/logout

The end-goal is a Spring Boot application with REST APIs capable of allowing users to sign up, log-in, modify their inventories, return their inventories, and logout. The expectation will be that all input/output to the REST APIs will be `application/json` and that any endpoints requiring user authentication will restrict access to unauthorized/guest users appropriately.

You are free to:
* Use any DBMS platform you like to store information (dependencies for H2 and MySQL are provided)
* Use any ORM framework you like (such as Hibernate)
* Use any means of authentication/authorization you like, provided it is _secure-ish_
* Structure the project as you see fit
* Add/modify any existing dependencies in the Maven POM file as required
* Host the application somewhere if that is easier for you
* Make any assumptions necessary that are not explicitly defined or outlined in this document

For this particular problem, no user-interface is technically required, _but definitely a bonus_.

Ultimately, the implementation is up to you-- there is no "right" way to accomplish this task, but you should act and code as if this were an application ready for production deployment once complete.

## Time commitment
Suggested level of effort: **2 - 4 hours**.

## Compiling and Running
The included `mvnw` and `mvnw.cmd` (Windows) binaries should make it relatively easy to compile your code, although you're free to use any IDE you prefer.

As an example, for Unix/Linux type systems, you can do the following to compile/install your application from the base directory:
```bash
./mvnw clean install
```

To run the application server you can use the following command:
```bash
./mvnw spring-boot:run
```

Your app will be running on [http://localhost:5000/](http://localhost:5000/) provided you haven't changed any defaults.

You're free to use any REST client you like, but [Postman](https://www.getpostman.com/) is a good choice if you don't have one already.
