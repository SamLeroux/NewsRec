#!/usr/bin/perl -w
use strict;
use warnings;
use DBI;
use LWP::Simple;

my $url="http://www.reuters.com/tools/rss";
my $username = 'user';
my $password = 'pass';
my $database = 'news';
my $hostname = 'localhost';

# Fetch articles
my $content = get($url) or die "Could not fetch NWS CSV page.";
my @matches = $content =~ /<td class=\"xmlLink\"><a href=\"(.*?)\">/g;
foreach my $match  (@matches){
	print "$match\n";
}

# Add to database
my $dbh = DBI->connect("dbi:mysql:database=$database;" . 
  "host=$hostname;port=3306", $username, $password);

foreach my $match  (sort @matches){
	my $SQL= "insert into NewsSource (id, description, fetchinterval, lastArticleFetchTime, lastFetchTry, name, rssUrl) " .
  " values(null, null, 120, CURDATE(), CURDATE(), null, \"$match\")";
	my $InsertRecord = $dbh->do($SQL);

	if($InsertRecord){
		print "Success\n";
	}
	else{
		print "Failure\n";
	}
}
