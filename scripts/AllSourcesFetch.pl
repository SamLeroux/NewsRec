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
	#print $regex,"\n";
	my $content = get($url) or die "Could not fetch $url";
	#print $content;
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

sub append{
	my $prefix = $_[0];
	my @items = $_[1];
	my @out;
	foreach my $item (@items){
		push @out, ($prefix . $item);
	}
	return @out;
}

sub uniq {
	my %seen;
	return grep { !$seen{$_}++ } @_;
}

#add(uniq(fetch("http://www.reuters.com/tools/rss","<td class=\"xmlLink\"><a href=\"(.*?)\">")));
#add(uniq(fetch("http://www.chicagotribune.com/services/rss/","<a href=\"(http:\/\/chicagotribune.feedsportal.com/.*?)\" target")));
#add(uniq(fetch("http://www.cnet.com/news-rss/","<a href=\"(http:\/\/news.cnet.com/.*?\.xml)\">")));
#add(uniq(fetch("http://www.cnet.com/news-rss/","<a href=\"(http:\/\/feeds.cnet.com/.*?)\">")));
#add(append("http://www.dailymail.co.uk",uniq(fetch("http://www.dailymail.co.uk/home/rssMenu.html","<a href=\"(/.*?\.rss)\">"))));
#add(("http://www.worldandmedia.com/?format=feed&type=rss"));
#add(uniq(fetch("http://seattletimes.com/flatpages/services/rss.html","<a href=\"(.*?\.xml)\"")));
#add(uniq(fetch("http://www.news.com.au/more-information/rss","<a href=\"(.*?\.xml)\"")));
#add(("http://www.hindu.com/rss/01hdline.xml","http://www.hindu.com/rss/03hdline.xml"));
#add(("http://rt.com/rss/news/"));
#add(("http://www.chinapost.com.tw/rss/front.xml","http://www.chinapost.com.tw/rss/international.xml"));
#add(("http://www.npr.org/rss/rss.php?id=1001","http://www.npr.org/rss/rss.php?id=1012","http://www.npr.org/rss/rss.php?id=1003","http://www.npr.org/rss/rss.php?id=1004"));
#add(("http://www.usnews.com/rss/news","http://www.usnews.com/rss/science"));
#add(("http://www.skynews.com.au/rss/feeds/skynews_topstories.xml"));
#add(("http://feeds.feedburner.com/NdtvNews-TopStories","http://feeds.feedburner.com/ndtv/TqgX","http://feeds.feedburner.com/NDTV-Trending"));
#add(uniq(fetch("http://content.time.com/time/rss","<a class=\"title\" href=\"(http:.*?feeds.*?)\"")));
#add(("http://feeds.foxnews.com/foxnews/latest","http://feeds.foxnews.com/foxnews/most-popular","http://feeds.foxnews.com/foxnews/politics","http://feeds.foxnews.com/foxnews/science","http://feeds.foxnews.com/foxnews/world"));
#add(uniq(fetch("http://www.theage.com.au/rssheadlines","<a href=\"(http://feeds.theage.com.au/.*?\.xml)")));
#add(("http://www.therealnews.com/rss/therealnews.rss"));
#add(("http://feeds.abcnews.com/abcnews/topstories","http://feeds.abcnews.com/abcnews/topstories","http://feeds.abcnews.com/abcnews/usheadlines","http://feeds.abcnews.com/abcnews/politicsheadlines","http://feeds.abcnews.com/abcnews/moneyheadlines","http://feeds.abcnews.com/abcnews/technologyheadlines","http://feeds.abcnews.com/abcnews/thisweekheadlines","http://feeds.abcnews.com/abcnews/mostreadstories","http://feeds.abcnews.com/technologyblog","http://feeds.abcnews.com/headlinesblog"));
#add(("http://learningenglish.voanews.com/api/epiqq"));
#add(("https://en.wikinews.org/w/index.php?title=Special:NewsFeed&feed=rss&categories=Published&notcategories=No%20publish|Archived|AutoArchived|disputed&namespace=0&count=15&ordermethod=categoryadd&stablepages=only"));
#add(uniq(fetch("http://techcrunch.com/rssfeeds/","href=\"(http://feeds.feedburner.com/T.*?)\"")));
#add(("http://rss.cbc.ca/lineup/topstories.xml","http://rss.cbc.ca/lineup/world.xml","http://rss.cbc.ca/lineup/politics.xml","http://rss.cbc.ca/lineup/technology.xml","http://rss.cbc.ca/lineup/business.xml","http://rss.cbc.ca/lineup/sports.xml"));
#add(("http://www.nydailynews.com/index_rss.xml","http://www.nydailynews.com/news/national/index_rss.xml","http://www.nydailynews.com/news/world/index_rss.xml","http://www.nydailynews.com/news/politics/index_rss.xml","http://www.nydailynews.com/news/crime/index_rss.xml","http://www.nydailynews.com/sports/index_rss.xml","http://www.nydailynews.com/entertainment/index_rss.xml"));
#add(uniq(fetch("http://www.scientificamerican.com/sitemap/","<a href=\"(http://rss.sciam.com/.*?/feed)\" class=\"rssLink\"")));
#add(("http://feeds.gawker.com/gizmodo/full","http://gigaom.com/feed/","http://feeds.feedburner.com/techdirt/feed","http://feeds.betanews.com/bn"));
#add(uniq(fetch("http://www.newscientist.com/feed/feeds","<a href=\"(http://feeds.newscientist.com/.*?)\">")));
#add(("http://www.newscientist.com/feed/magazine","http://www.newscientist.com/blogs/shortsharpscience/atom.xml","http://www.newscientist.com/feed/view?id=1&type=channel","http://www.newscientist.com/feed/view?id=2&type=channel","http://www.newscientist.com/feed/view?id=3&type=channel","http://www.newscientist.com/feed/view?id=4&type=channel","http://www.newscientist.com/feed/view?id=5&type=channel","http://www.newscientist.com/feed/view?id=6&type=channel","http://www.newscientist.com/feed/view?id=7&type=channel","http://www.newscientist.com/feed/features"));
