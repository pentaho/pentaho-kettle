#!/usr/bin/env bash

export HELM_EXPERIMENTAL_OCI=1

rm webspoon-*.tgz

helm package ../k8s/helm/src/main/resources/webspoon -d ../manualInstall
# must rev version to match solutionPackage.yaml and Chart.yaml
helm chart save ./webspoon* ldl-dev-r2d2-c7-registry.dogfood.trylumada.com/webspoon:1.4.1
# must rev version to match solutionPackage.yaml and Chart.yaml
helm chart push ldl-dev-r2d2-c7-registry.dogfood.trylumada.com/webspoon:1.4.1
kubectl apply -f solutionPackage.yaml -n hitachi-solutions