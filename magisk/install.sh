#!/system/bin/sh
MODDIR=${0%/*}
cp -f $MODDIR/bin/joycond /data/local/tmp/joycond
chmod 755 /data/local/tmp/joycond