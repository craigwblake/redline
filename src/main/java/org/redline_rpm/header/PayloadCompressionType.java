package org.redline_rpm.header;

/**
 * RPM payload compression tag values
 *
 * @author Scott stark (sstark@redhat.com) (C) 2011 Red Hat Inc.
 * @version $Revision:$
 */
public enum PayloadCompressionType {
   none,
   gzip,
   bzip2,
   xz
}
