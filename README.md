# Alumni LinkedIn Profile Searcher

A Spring Boot backend service to automate LinkedIn alumni lead extraction using the PhantomBuster API. This project provides REST endpoints for integration—no user interface included.

---

## Features

- Integrates with PhantomBuster for LinkedIn lead extraction
- REST APIs to trigger and manage extractions
- Export results as CSV/JSON

---

## Getting Started

### Prerequisites

- Java 17+
- Maven
- PhantomBuster API key

### Setup

```bash
git clone https://github.com/Devshree-06/Alumni-LinkedIn-Profile-Searcher.git
cd Alumni-LinkedIn-Profile-Searcher
mvn clean install
```

Add your PhantomBuster API key to `src/main/resources/application.properties`:
```
phantombuster.api.key=YOUR_API_KEY
```

### Run

```bash
mvn spring-boot:run
```
App runs at `http://localhost:8080`

---


## License

[MIT](LICENSE)

---

Maintainer: [Devshree-06](https://github.com/Devshree-06)
