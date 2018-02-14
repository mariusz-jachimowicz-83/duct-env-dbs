# duct-env-dbs

Alternative implementation of [Duct module.sql](https://github.com/duct-framework/module.sql).  
It allows to specify multiple db urls in one config file and initialize db pool using url based on environment name from config file.  
It allows me to easier launch 2 duct systems in parallel during development - one for dev and one for tests so I can easily run tests against db from the REPL.

[![CircleCI](https://circleci.com/gh/mariusz-jachimowicz-83/duct-env-dbs.svg?style=svg)](https://circleci.com/gh/mariusz-jachimowicz-83/duct-env-dbs)

## Installation

[![Clojars Project](https://img.shields.io/clojars/v/com.mjachimowicz/duct-env-dbs.svg)](https://clojars.org/com.mjachimowicz/duct-env-dbs)

## Usage

To add this module to your configuration, add the `:duct-env-dbs.module/sql` key with dbs configuration:

```clojure
{:duct-env-dbs.module/sql {:development {:database-url "jdbc:sqlite:db/example_dev.sqlite"}
                           :production  {:database-url "jdbc:sqlite:db/example_prod.sqlite"}
                           :testing     {:database-url "jdbc:sqlite:db/example_test.sqlite"}}}
```

or

```clojure
{:duct-env-dbs.module/sql {:development #duct/env ["DB_URL_DEV"  Str]
                           :production  #duct/env ["DB_URL_PROD" Str]
                           :testing     #duct/env ["DB_URL_TEST" Str]}}}
```

In all places when you need use db you need to reference `:duct.database/sql` same way as in [Duct module.sql](https://github.com/duct-framework/module.sql):

```clojure
{:some-component {:db (ig/ref :duct.database/sql)}}
```

## License

Copyright © 2018 Mariusz Jachimowicz

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.