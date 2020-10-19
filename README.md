# Thorntail Circuit Breaker Example

## Purpose

This example demonstrates how to guard remote service invocations using a circuit breaker.

## Prerequisites

* Log into an OpenShift cluster of your choice: `oc login ...`.
* Select a project in which the services will be deployed: `oc project ...`.

## Modules

The `greeting-service` module serves the web interface and communicates with the `name-service`.

The `name-service` module provides an endpoint that simulates a working or failing service.

## Deployment

Run the following commands to configure and deploy the applications.

### Deployment using S2I

```bash
oc apply -f ./greeting-service/.openshiftio/application.yaml
oc new-app --template=thorntail-circuit-breaker-greeting

oc apply -f ./name-service/.openshiftio/application.yaml
oc new-app --template=thorntail-circuit-breaker-name
```

### Deployment with the JKube Maven Plugin

```bash
mvn clean oc:deploy -Popenshift
```

## Test everything

This is completely self-contained and doesn't require the application to be deployed in advance.
Note that this may delete anything and everything in the OpenShift project.

```bash
mvn clean verify -Popenshift,openshift-it
```
