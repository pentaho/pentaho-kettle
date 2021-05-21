#!/usr/bin/env bash

export HELM_EXPERIMENTAL_OCI=1

rm webspoon-*.tgz

cd ..
docker build -t hiromuhota/webspoon:latest .
docker tag hiromuhota/webspoon:latest ldl-dev-r2d2-35-registry.dogfood.trylumada.com/webspoon:1.0.0
#docker push ldl-dev-r2d2-35-registry.dogfood.trylumada.com/webspoon:1.0.0
#cd manualInstall
#
#
#helm package ../../k8s/helm/src/main/resources/webspoon -d ../manualInstall
## must rev version to match solutionPackage.yaml and Chart.yaml
#helm chart save ./webspoon* ldl-dev-r2d2-35-registry.dogfood.trylumada.com/webspoon:1.0.0
## must rev version to match solutionPackage.yaml and Chart.yaml
#helm chart push ldl-dev-r2d2-35-registry.dogfood.trylumada.com/webspoon:1.0.0
#kubectl apply -f solutionPackage.yaml -n hitachi-solutions