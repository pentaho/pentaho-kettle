#!/usr/bin/env bash

export HELM_EXPERIMENTAL_OCI=1

rm webspoon-*.tgz

#cd ..
#docker build --no-cache -t hiromuhota/webspoon:latest .
#docker tag hiromuhota/webspoon:latest ldl-dev-r2d2-c7-registry.dogfood.trylumada.com/webspoon:CHANGE_ME
#docker push ldl-dev-r2d2-c7-registry.dogfood.trylumada.com/webspoon:CHANGE_ME
#cd manualInstall


helm package ../k8s/helm/src/main/resources/webspoon -d ../manualInstall
# must rev version to match solutionPackage.yaml and Chart.yaml
helm chart save ./webspoon* ldl-dev-r2d2-c7-registry.dogfood.trylumada.com/webspoon:CHANGE_ME
# must rev version to match solutionPackage.yaml and Chart.yaml
helm chart push ldl-dev-r2d2-c7-registry.dogfood.trylumada.com/webspoon:CHANGE_ME
kubectl apply -f solutionPackage.yaml -n hitachi-solutions