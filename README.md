# About this Repo

A Java Web App to fetch your Instagram's follows and followers, and see what Account  follows you but you are not following them back, and the other way around. 

It highly dependents to [instagram4j](https://github.com/instagram4j/instagram4j) for doing various Instagram API call.   

How To
------------
Build by using mvn
```
$ mvn clean package
```

Docker build
```
$ docker build -t insta-on .
```

Docker run
```
$ docker run -p 8080:8080 -e instagram.username=<your Instagram username> -e instagram.password=<your Instagram password> insta-on
```

Libraries
------------
- Java 11
- Quarkus 2.6.2.Final
- Bootstrap 4.6.1
- JQuery 3.6.0
- instagram4j 2.0.6


Limitation
------------
Im limiting to 1000 following/followers only, since requesting more than that would make Instagram block the API request and start displaying some captcha. 

Disclaimer
------------

```
This application is provided "as is" without any guarantee whatsoever. 
Feel free to fork, tinker, add, remove, change, or do whatever you want to it. 
```