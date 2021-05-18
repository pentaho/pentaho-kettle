{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "webspoon.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "webspoon.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else -}}
{{- $release := default .Release.Name -}}
{{- $releaseLength := $release | len -}}
{{ if gt $releaseLength 29 }}
  {{- $headRelease := $release | trunc 26 -}}
  {{- $tailRelease := $release | trunc -3 -}}
  {{- printf "%s%s" $headRelease $tailRelease | trunc 63 | trimSuffix "-" -}}
{{ else }}
  {{- printf "%s" $release | trunc 63 | trimSuffix "-" -}}
{{ end }}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "webspoon.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "webspoon.labels" -}}
helm.sh/chart: {{ include "webspoon.chart" . }}
{{ include "webspoon.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Selector labels
*/}}
{{- define "webspoon.selectorLabels" -}}
app.kubernetes.io/name: {{ include "webspoon.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{/*
Helper function for sso name
*/}}
{{- define "webspoon.sso" -}}
{{- template "webspoon.name" . -}}-sso
{{- end -}}

{{/*
Helper function for gatekeeper clientId
*/}}
{{- define "webspoon.gatekeeper.clientId" -}}
{{- printf "%s-%s" (include "webspoon.sso" . ) "client" | trunc 63 | trimSuffix "-" -}}
{{- end -}}
