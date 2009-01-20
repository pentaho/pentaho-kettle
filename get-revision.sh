#!/bin/bash

  svn info \
| grep Revision \
| awk '{ print $2 }'
