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
{{- define "webspoon.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a short release name.
*/}}
{{- define "webspoon.short.Release.Name" -}}
{{- .Release.Name | trunc 33 | trimSuffix "-" }}
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
{{- end }}

{{/*
Selector labels
*/}}
{{- define "webspoon.selectorLabels" -}}
app.kubernetes.io/name: {{ include "webspoon.name" . }}
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

{{- define "webspoon.basepath" -}}
{{ .Release.Namespace }}/{{ .Release.Name }}
{{- end -}}

{{- define "webspoon-path" -}}
{{ template "webspoon.basepath" . }}/{{ template "webspoon.fullname" . }}
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

