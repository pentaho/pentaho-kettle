{{/*
Expand the name of the chart.
*/}}
{{- define "dataflow-pentaho-importer.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "dataflow-pentaho-importer.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "dataflow-pentaho-importer.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a short release name.
*/}}
{{- define "dataflow-pentaho-importer.short.Release.Name" -}}
{{- .Release.Name | trunc 33 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "dataflow-pentaho-importer.labels" -}}
helm.sh/chart: {{ include "dataflow-pentaho-importer.chart" . }}
{{ include "dataflow-pentaho-importer.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "dataflow-pentaho-importer.selectorLabels" -}}
app.kubernetes.io/name: {{ include "dataflow-pentaho-importer.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Helper function for sso name
*/}}
{{- define "data-flow-studio.sso" -}}
{{- .Values.dataFlowStudio.service -}}-sso
{{- end -}}

{{/*
Helper function for gatekeeper clientId
*/}}
{{- define "data-flow-studio.gatekeeper.clientId" -}}
{{- printf "%s-%s" (include "data-flow-studio.sso" . ) "client" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Helper function for keycloak client secret
*/}}
{{- define "data-flow-studio.client.secret" -}}
{{- printf "%s-%s" "keycloak-client-secret" (include "data-flow-studio.gatekeeper.clientId" . ) -}}
{{- end -}}

{{/*
Helper function for impersonator secret
*/}}
{{- define "data-flow-studio.impersonator.secret" -}}
{{- printf "%s-%s-%s" "credential-default" .Values.keycloak.impersonator.username .Release.Namespace -}}
{{- end -}}

