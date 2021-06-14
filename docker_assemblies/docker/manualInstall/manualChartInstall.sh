#!/usr/bin/env bash

export HELM_EXPERIMENTAL_OCI=1

rm data-transformation-editor-*.tgz

cd ../..
mvn clean install -DskipTests
docker tag org.pentaho.di/pentaho-kettle-docker:9.2.0.0-SNAPSHOT ldl-dev-r2d2-c7-registry.dogfood.trylumada.com/webspoon:CHANGE_ME
docker push ldl-dev-r2d2-c7-registry.dogfood.trylumada.com/webspoon:CHANGE_ME
cd docker/manualInstall


helm package ../../k8s/helm/src/main/resources/webspoon -d ../manualInstall
# must rev version to match solutionPackage.yaml and Chart.yaml
export HELM_EXPERIMENTAL_OCI=1
helm chart save ./data-transformation-editor* ldl-dev-r2d2-c7-registry.dogfood.trylumada.com/data-transformation-editor:CHANGE_ME
# must rev version to match solutionPackage.yaml and Chart.yaml
helm chart push ldl-dev-r2d2-c7-registry.dogfood.trylumada.com/data-transformation-editor:CHANGE_ME
kubectl apply -f solutionPackage.yaml -n hitachi-solutions