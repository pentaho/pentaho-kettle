#!/usr/bin/env bash

export HELM_EXPERIMENTAL_OCI=1

rm webspoon-*.tgz

helm package ../k8s/helm/src/main/resources/webspoon -d ../manualInstall
helm chart save ./webspoon* ldl-dev-r2d2-c7-registry.dogfood.trylumada.com/webspoon:latest
helm chart push ldl-dev-r2d2-c7-registry.dogfood.trylumada.com/webspoon:latest
kubectl apply -f solutionPackage.yaml -n hitachi-solutions