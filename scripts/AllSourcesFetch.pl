#!/usr/bin/perl -w
use strict;
use warnings;
use DBI;
use LWP::Simple;


my $username = 'user';
my $password = 'pass';
my $database = 'news';
my $hostname = 'localhost';

# Fetch articles
sub fetch{
	my $url = $_[0];
	my $regex = $_[1];
	print $regex,"\n";
	my $content = get($url) or die "Could not fetch $url";
	print $content;
	my @matches = $content =~ /$regex/g;
	foreach my $match  (@matches){
		print "$match\n";
	}
	return @matches;
}

sub add{
	# Add to database
	my $dbh = DBI->connect("dbi:mysql:database=$database;" . 
	  "host=$hostname;port=3306", $username, $password);

	foreach my $match  (sort @_){
		my $SQL= "insert into NewsSource (id, description, fetchinterval, lastArticleFetchTime, lastFetchTry, name, rssUrl) " .
	  " values(null, null, 120, CURDATE(), CURDATE(), null, \"$match\")";
		my $InsertRecord = $dbh->do($SQL);
		#print $SQL,"\n";
		if($InsertRecord){
			print "Success\n";
		}
		else{
			print "Failure\n";
		}
	}
}

sub uniq {
	my %seen;
	return grep { !$seen{$_}++ } @_;
}

#add(uniq(fetch("http://www.reuters.com/tools/rss","<td class=\"xmlLink\"><a href=\"(.*?)\">")));
#add(uniq(fetch("http://www.chicagotribune.com/services/rss/","<a href=\"(http:\/\/chicagotribune.feedsportal.com/.*?)\" target")));
#add(uniq(fetch("http://www.cnet.com/news-rss/","<a href=\"(http:\/\/news.cnet.com/.*?\.xml)\">")));
#add(uniq(fetch("http://www.cnet.com/news-rss/","<a href=\"(http:\/\/feeds.cnet.com/.*?)\">")));
