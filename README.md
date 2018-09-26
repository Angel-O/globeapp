# globeapp
just messing around the Play! framework and Scala.js. The result? A little web app based on mobile apps. 

#### Notes
- the UI project is a Scala.js experiment to turn Biding.scala into a reactive component based library: not expecting you to understand everything :)
- this README file is still a WIP...

## Tech stack

### UI

- Scala.js
- Binding.scala
- Bulma.css

### APIs

- Scala
- Play! framework

### Persistence

- MongoDB

## Installation & Requirements

- SBT v.1.0.0 +
- Scala v. 2.12 +
- Scala.js v. 0.6.xxx
- Play! framework v. 2.6 +
- Docker

## How to run the app

- In order to run the app locally 7 services, a mongoDB instance and the UI project need to be started.

### Building services

- coming soon

### Running Services

1. from a terminal run `docker-compose up` at the root of the project: this will run all servcices simultaneously

Each service can be run (and stopped) separately using the docker CLI

Tip: by installing Kitematic you can start/restart services using a GUI rather than typing commnads on the Terminal

### Populate the MongoDb instance

1. after starting the MongoDB instance, run the command `mongorestore -h localhost:37017 -d globeapp ./mongo-data/dump/globeapp` to populate the database with some mock data

### Building and running the UI

1. run SBT and execute the command ~fastOPTJS
2. navigate to localhost:12345/index-dev.html

### Messaging

- coming soon
