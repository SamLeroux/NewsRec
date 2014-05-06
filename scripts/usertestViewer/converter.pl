#!/usr/bin/perl

use strict;
use warnings;
use GD;

my $filename="usertest.csv";

open(FILE, $filename);
my %datapoints;

my $maxNumberOfRatings = 0;
while (<FILE>){
	my @parts = split(";");
	my $user = $parts[1];
	my $type = $parts[2];
	if (! exists $datapoints{$user}){
		$datapoints{$user} = [];
	}
	push @{$datapoints{$user}}, $type;
	if (scalar @{$datapoints{$user}} > $maxNumberOfRatings){
		$maxNumberOfRatings = scalar @{$datapoints{$user}};
	}
}
close FILE;

my $numberOfUsers = scalar keys %datapoints;

my $d = 50;
my $image = new GD::Image($numberOfUsers*$d,$maxNumberOfRatings*$d);

my $background = $image -> colorAllocate(255,255,255);
my $trendingColor = $image -> colorAllocate(192,24,24);
my $personalColor = $image -> colorAllocate(48,96,192);
my $black = $image -> colorAllocate(0,0,0);

my $i = 0;

foreach my $user (keys %datapoints){
	for (my $j = 0; $j < scalar @{$datapoints{$user}};$j++){
		if ($datapoints{$user} -> [$j] eq "trending"){
			$image -> filledRectangle($i,$j*$d, $i+$d, $j*$d+$d, $trendingColor);
		}
		else{
			$image -> filledRectangle($i,$j*$d, $i+$d, $j*$d+$d, $personalColor);
		}
	$image -> rectangle($i,$j*$d, $i+$d, $j*$d+$d, $black);
	}
	$i += $d;
}

binmode STDOUT;
print $image->png;




