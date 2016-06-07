# Easy Lotto API

**API & Backend for easy-lotto services.**


## Run

Start services with sbt:

```
$ sbt
> ~re-start

$ sbt jobs/run
```

## Accessing the API

```
$ curl --compressed -v -X GET http://localhost:9000/api/ping
$ curl --compressed -v -X GET http://localhost:9000/api/lotofacil
```

## Testing

Execute tests using `test` command:

```
$ sbt
> test
```

## Docker 

```
$ docker pull mongo:3.0.12
$ docker run -p 27017:27017 --name mongo-3 -d mongo:3.0.12
```


## Heroku dev

```bash
# link current git folder to remote heroku project
$ heroku git:remote -a easy-lotto-api
```
