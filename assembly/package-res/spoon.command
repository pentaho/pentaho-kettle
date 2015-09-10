cd `dirname $0`

# if a BASE_DIR argument has been passed to this .command, use it
if [ -n "$1" ] && [ -d "$1" ] && [ -x "$1" ]; then
    echo "DEBUG: Using value ($1) from calling script"
    cd "$1"
fi

./spoon.sh
exit