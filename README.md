# Easy Lotto API

**API & Backend for easy-lotto services.**


## Run

Start services with sbt:

```
$ sbt
> ~re-start

$ sbt jobs/run
```


## Testing

Execute tests using `test` command:

```
$ sbt
> test
```


## Heroku dev

```bash
$ heroku buildpacks:clear -a easy-loto-dev
$ heroku buildpacks:add https://github.com/heroku/heroku-buildpack-nodejs.git -a easy-loto-dev
$ heroku buildpacks:add https://github.com/heroku/heroku-buildpack-scala.git -a easy-loto-dev
$ heroku buildpacks -a easy-loto-dev
$ heroku config:set NPM_CONFIG_LOGLEVEL=verbose -a easy-loto-dev
$ heroku config:set SBT_OPTS="-Dsbt.jse.engineType=Node"
$ heroku config:set SBT_CLEAN=true

```
