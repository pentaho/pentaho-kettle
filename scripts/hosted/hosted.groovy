#!/usr/bin/env bash
//usr/bin/env groovy -cp "$(cd $(dirname ${BASH_SOURCE[0]}); pwd)/groovy" -Dgroovy.grape.report.downloads=true "$0" "$@"; exit $?

def cli = new CLI(name: this.class.canonicalName)
cli.process(args)

new HostedArtifactsManager(cli).hostArtifacts()