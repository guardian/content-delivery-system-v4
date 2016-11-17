#!/usr/bin/env perl

die "cf_temp_file is not set\n" if(! $ENV{'cf_temp_file'});

open FH,'>:utf8',$ENV{'cf_temp_file'} or die "Could not open ".$ENV{'cf_temp_file'}." for writing\n";
print FH "batch=true\n";
print FH "media1,inmeta1,meta1,xml1\n";
print FH "media2,inmeta2,meta2,xml2\n";
close FH;

exit 0;
