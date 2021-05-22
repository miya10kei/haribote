haribote
---
[![dev-env container image CI](https://github.com/miya10kei/dotfiles/actions/workflows/dev-env-ci.yaml/badge.svg)](https://github.com/miya10kei/dotfiles/actions/workflows/dev-env-ci.yaml)  
haribote is mock server can be launched by CLI.

# Usage

```bash
$ cat spec.yaml

specs:
  - protocol: http
    method: get
    path: /hello
    status: 200
    content-type: application/jso[![dev-env container image CI](https://github.com/miya10kei/dotfiles/actions/workflows/dev-env-ci.yaml/badge.svg)](https://github.com/miya10kei/dotfiles/actions/workflows/dev-env-ci.yaml)n
    body: '{ "message": "Hello get" }'
    delay-mills: 1000

$ haribote -f spec.yaml
```

# Support protocol

- http

# Configuration

When you execute haribote first time, it makes haribote directory at `$HOME/.config/haribote`  
The directory has the following files

- haribote.yaml
- default-spec.yaml

## haribote.yaml

`haribote.yaml` is server configuration file, and has the following items.

| Item                    | Type    | Description                |
|-------------------------|---------|----------------------------|
| server.http.port        | Int     | http listen port           |
| server.http.log-enabled | Boolean | if true, output access log |

## default-spec.yaml

`default-spec.yaml` is mock spec configuration file. You can pass the spec file when execute haribote. You can specify
the following items.

| Item               | Type   | Description                                                      |
|--------------------|--------|------------------------------------------------------------------|
| specs.protocol     | String | protocol                                                         |
| specs.method       | String | http method                                                      |
| specs.path         | String | url pth                                                          |
| specs.status       | Int    | response status                                                  |
| specs.content-type | String | response Content-Type                                            |
| specs.body         | String | response body                                                    |
| specs.delay-mills  | Long   | waits for the specified milliseconds, and then return a response |

# Build

## Requirement

- Java 11
- GraalVM 21.0.0

## How to build and test

```bash
./gradlew clean build
```

## How to native build

Before executing the following command, set GraalVM to JAVA_HOME.

```bash
./gradlew assembly nativeImage
```
